/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateQueryException;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.DatasetState;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * The Aggregator is responsible for aggregating transaction and step data and storing
 * it in the de-normalized step_rollup table.
 *
 * @author Benjamin Billings
 * @version $Revision: 15747 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-12-11 09:46:48 -0500 (Tue, 11 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class Aggregator extends AbstractExtractor {

    /** Logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Boolean to force a reload of all data. */
    private boolean forceAll;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** List of all dataset reports. */
    private List<DatasetReport> datasetReports = new LinkedList();

    /** Date representation to record speed of program per student. */
    private Date timeTracker = new Date();

    /** Run on a specific dataset. */
    private boolean datasetOnlyFlag;
    /** Run on a specific model. */
    private boolean modelOnlyFlag;
    /** The dataset to aggregate. */
    private int datasetIdToRun;
    /** The model to aggregate. */
    private long modelIdToRun;
    /** Run on a specific sample. */
    private boolean sampleOnlyFlag;
    /** The sample to aggregate. */
    private int sampleIdToRun;
    /** Flag indicating if LFA Backfill should be run.  Default is true.*/
    private boolean runLfaBackfillFlag = true;
    /** Flag indicating whether to skip datasets marked as junk.  Default is false.*/
    private boolean skipJunkFlag = false;
    /** The number of sessions to aggregate in each batch, initialized to default size. */
    private int batchSize = StepRollupDao.AGG_SESSION_BATCH_SIZE;
    /** Where to find the aggregator stored procedure template. */
    private String aggSPFilePath;

    /** Magic Number. */
    private static final int ONE_THOUSAND = 1000;

    /** Default constructor. */
    public Aggregator() {
        forceAll = false;
        datasetOnlyFlag = false;
        sampleOnlyFlag = false;
        modelOnlyFlag = false;
    }

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.*/
    public void setTransactionTemplate (TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Gets transactionTemplate.
     * @return TransactionTemplate*/
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    /**
     * Helper method for logging nicely.
     * @param datasetItem the dataset we care about.
     * @return a nicely formatted string for logging.
     */
    public String getLogPrefix(DatasetItem datasetItem) {
        return getLogPrefix(datasetItem, null);
    }

    /**
     * Helper method for logging nicely.
     * @param datasetItem the dataset we care about.
     * @param sampleItem the sample we care about.
     * @return a nicely formatted string for logging.
     */
    public String getLogPrefix(DatasetItem datasetItem, SampleItem sampleItem) {
        String prefix = "Agg [";
        if (datasetItem != null) {
            prefix += datasetItem.getDatasetName() + " (" + datasetItem.getId() + ")";
        }
        if (sampleItem != null) {
            prefix += " / " + sampleItem.getSampleName() + " (" + sampleItem.getId() + ")";
        }
        prefix += "] ";
        return prefix;
    }

    /**
     * Helper method to format sample name and id for logging purposes.
     * @param sample the sample
     * @return a nicely formatted string.
     */
    private String formatForLogging(SampleItem sample) {
        return ("'" + sample.getSampleName() + "' (" + sample.getId() + ")");
    }

    /**
     * Helper method to format dataset name and id for logging purposes.
     * @param dataset the dataset
     * @return a nicely formatted string.
     */
    private String formatForLogging(DatasetItem dataset) {
        return (dataset.getDatasetName() + " (" + dataset.getId() + ")");
    }

    /**
     * Helper method to format model name and id for logging purposes.
     * @param model the skill model
     * @return a nicely formatted string.
     */
    private String formatForLogging(SkillModelItem model) {
        return (model.getSkillModelName() + " (" + model.getId() + ")");
    }

    /**
     * Helper method to print out the results of aggregation.
     */
    private void printResults() {
        StringBuffer aggregatorResults = new StringBuffer();
        if (datasetReports.size() == 0) {
            aggregatorResults.append("No datasets or samples were aggregated.");
        } else {
            for (DatasetReport report : datasetReports) {
                aggregatorResults.append(report);
            }
        }
        logger.info(aggregatorResults);
    }

    /** Used to customize the set of procedures for sample creation. */
    private static final String TO_INSERT_BASE = "KCM_";

    /**
     * Walks through a dataset or sample to make sure the step_rollup table is
     * properly populated.  The aggregator can be run in the following modes:
     * <ul>
     *    <li>datasetOnly</li>
     *    <li>datasetOnly and force</li>
     *    <li>sampleOnly</li>
     *    <li>sampleOnly and force</li>
     *    <li>force all</li>
     *    <li>run in everyday mode</li>
     * </ul>
     * Running in everyday mode will aggregate a dataset if one of the following is true:
     * <ul>
     *      <li>it has been modified</li>
     *      <li>the force flag has been set</li>
     * </ul>
     *
     * If an error occurs during the aggregation of a sample or dataset, the sample is
     * removed and an appropriate message is logged.
     * @return returns true if the method was successful or false, otherwise
     */
    public Boolean populateStepRollup() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetSystemLogDao datasetSystemLogDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        boolean everydayModeFlag = false;
        List<DatasetItem> datasetList = new ArrayList<DatasetItem>();
        SampleItem sampleToAgg = null;
        DatasetItem datasetToAgg = null;
        SkillModelItem modelToAgg = null;

        // set up how the Aggregator will run.
        if (datasetOnlyFlag) {
            datasetToAgg = datasetDao.get(datasetIdToRun);
            if (datasetToAgg == null
                    || (datasetToAgg.getDeletedFlag() != null
                        && datasetToAgg.getDeletedFlag().equals(true))) {
                logger.error("Unable to find a dataset item for dataset id '"
                        + datasetIdToRun + "'."
                        + " Please verify your input parameter.");
                return false;
            }
            datasetList.add(datasetToAgg);
            logger.info(getRunModeMsg(datasetToAgg));
        } else if (sampleOnlyFlag) {
            sampleToAgg = sampleDao.get(sampleIdToRun);
            if (sampleToAgg == null) {
                logger.error("Unable to find a sample item for sample id '" + sampleIdToRun + "'."
                        + " Please verify your input parameter.");
                 return false;
            }
            logger.info(getRunModeMsg(sampleToAgg));
        } else if (modelOnlyFlag) {
            modelToAgg = skillModelDao.get(modelIdToRun);
            if (modelToAgg == null) {
                logger.error("Unable to find a Skill Model item for model id '" + modelIdToRun + "'."
                             + " Please verify your input parameter.");
                return false;
            }
            if (aggSPFilePath == null) {
                logger.error("The aggregator stored procedure path must be specified when "
                             + "running in model-only mode. Specify this with the -spFilePath arg.");
                return false;
            }
            logger.info(getRunModeMsg(modelToAgg));
        } else {
            // everyday mode
            datasetList = datasetDao.findUndeletedDatasets();
            everydayModeFlag = true;
            logger.info(getRunModeMsg());
        }

        if (everydayModeFlag || datasetOnlyFlag) {
            for (DatasetItem dataset : datasetList) {
                if (dataset.getJunkFlag() != null && dataset.getJunkFlag() && this.skipJunkFlag) {
                    logger.info("Skipping Junk dataset " + formatForLogging(dataset));
                } else {
                    // check the need for tx_duration calculation before aggregating the data.
                    boolean requiresTxDurationCalc =
                        datasetSystemLogDao.requiresTxDurationCalculation(dataset);
                    if (requiresTxDurationCalc) {
                        SampleItem defaultSample = sampleDao.findOrCreateDefaultSample(dataset);
                        logger.info("populateStepRollup: created sample:" + defaultSample
                                + " for dataset " + dataset);
                        if (defaultSample != null) {
                            runTxDurationSP(dataset);
                        }
                    }
                    DatasetReport datasetReport = aggregateDataset(dataset, requiresTxDurationCalc);
                    if (datasetReport != null) {
                        // if dataset is not agg'd we don't get a report.
                        datasetReports.add(datasetReport);

                        // If a slave, update master DataShop instance with dataset info.
                        if (datasetReport.getSuccess() && DataShopInstance.isSlave()) {
                            String datasetName = dataset.getDatasetName();
                            Integer datasetId = (Integer)dataset.getId();
                            logger.info("Update master DataShop instance with dataset info "
                                        + datasetName + " (" + datasetId + ")");
                            try {
                                DatasetDTO datasetDto = HelperFactory.DEFAULT.
                                    getWebServiceHelper().datasetDTOForId(datasetId);
                                DatasetCreator.INSTANCE.setDataset(datasetDto);
                            } catch (Exception e) {
                                // Failed to push Dataset info to master. Ignore?
                                logger.debug("Failed to push dataset info to master for dataset '"
                                             + datasetName + "': " + e);
                            }
                        }
                    }
                }
            }
        } else if (modelOnlyFlag) {
            DatasetItem dataset = modelToAgg.getDataset();
            dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());

            DatasetReport datasetReport = new DatasetReport(dataset);

            // Create a list of all the samples for the given dataset.
            // Be sure the list has the default (All Data) sample and that that sample
            // is first in the list.  This is important in case we need to generate
            // problem events.
            List<SampleItem> allSamples = sampleDao.find(dataset);
            List<SampleItem> samplesToAgg = new ArrayList();
            SampleItem allDataSample = sampleDao.findOrCreateDefaultSample(dataset);
            samplesToAgg.add(allDataSample);
            if (allSamples.contains(allDataSample)) {
                allSamples.remove(allDataSample);
            }
            samplesToAgg.addAll(allSamples);

            int numRowsCreated = 0;
            boolean loadSuccess = false;

            StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
            String prefix = "Agg [" + formatForLogging(modelToAgg) + "]";

            logger.info(prefix + " Removing all step rollups for model.");
            int numRemoved = stepRollupDao.removeAll(modelToAgg);
            logger.info(prefix + " Removed " + numRemoved + " rollups.");

            for (SampleItem sample : samplesToAgg) {
                SampleReport sampleReport = new SampleReport(sample);

                String toInsert = TO_INSERT_BASE + sample.getId();
                loadSuccess =
                    stepRollupDao.loadAggregatorStoredProcedure(aggSPFilePath, toInsert);
                if (loadSuccess) {
                    numRowsCreated = stepRollupDao.callAggregatorStoredProcedure(sample, toInsert,
                                                                                 createSkillModelSQLClause());
                    stepRollupDao.dropAll(sample, TO_INSERT_BASE);
                    logger.debug(prefix + " Finished aggregation of sample " + sample.getSampleName() +
                                 " for KC Model '" + modelToAgg.getSkillModelName() + "'.");
                }
                if (numRowsCreated < 0) {
                    sampleReport.setSuccess(false);
                    logger.error(prefix + " There was an error processing the sample, "
                                 + sample.getSampleName() + " while aggregating for new"
                                 + " KC Model(s)");
                    break;
                } else {
                    sampleReport.setSuccess(true);
                    logger.debug(prefix + " Created " + numRowsCreated + " stepRollups for sample " +
                                 sample.getSampleName() + "(" + sample.getId() + ")");
                }

                datasetReport.addSampleReport(sampleReport);
                if (!sampleReport.successFlag) {
                    // default is true.
                    datasetReport.setSuccess(Boolean.FALSE);
                }

            } // end for (sample)

            datasetReport.setDone();
            datasetReports.add(datasetReport);

        } else {
            // we are running in sampleOnly mode.
            // check the state of the dataset to see if it requires aggregation
            DatasetItem dataset = sampleToAgg.getDataset();
            dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
            // check the need for tx_duration calculation before aggregating the data.
            boolean requiresTxDurationCalc =
                datasetSystemLogDao.requiresTxDurationCalculation(dataset);
            if (requiresTxDurationCalc) {
                runTxDurationSP(dataset);
            }
            if (datasetRequiresAgg(dataset, requiresTxDurationCalc)) {
                DatasetReport datasetReport = new DatasetReport(dataset);
                datasetReport.setWasAggregated(true);
                SampleReport sampleReport = aggregateSample(dataset, sampleToAgg);
                datasetReport.addSampleReport(sampleReport);
                if (!sampleReport.successFlag) {
                    // default is true.
                    datasetReport.setSuccess(Boolean.FALSE);
                }
                datasetReport.setDone();
                datasetReports.add(datasetReport);
                datasetReport.setRollupFlagSet(true);
            } else {
                logger.info("Aggregation not required for sample " + formatForLogging(sampleToAgg));
            }
        }

        // print out the results.
        printResults();
        // now clear out the list of reports.
        datasetReports.clear();
        sampleOnlyFlag = false;
        datasetOnlyFlag = false;
        forceAll = false;

        // If we have completed the populating the step rollup, then return true
        return true;
    } // end populateStepRollup()

    /**
     * Populate the step rollup table for the given dataset.
     * @param datasetItem the dataset item
     */
    public void populateStepRollup(DatasetItem datasetItem) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem defaultSample = sampleDao.findOrCreateDefaultSample(datasetItem);
        logger.info("populateStepRollup: created sample:" + defaultSample
                + " for dataset " + datasetItem);
        if (defaultSample != null) {
            runTxDurationSP(datasetItem);
        }
        aggregateDataset(datasetItem, true);
        //DatasetReport datasetReport = aggregateDataset(datasetItem, true);
    }

    /**
     * Generate the "in" clause needed to aggregate the new skill models.  This is used by the
     * aggregator stored procedure.
     * @return a partial SQL statement to pass to the stored procedure.
     */
    private String createSkillModelSQLClause() {
        return " sk.skill_model_id IN (" + modelIdToRun + ")";
    }

    /**
     * Update the problem hierarchy table prior to aggregation for a dataset
     * only if the dataset has been modified.
     * @param datasetItem the dataset item
     * @return true if the update was executed, false otherwise
     */
    private boolean updateProblemHierarchyTable(DatasetItem datasetItem) {
        DatasetSystemLogDao datasetSystemLogDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
        String logPrefix = "updateProblemHierarchyTable:: ";
        long startTime = System.currentTimeMillis();

        // Update the problem hierarchy table if the dataset has been modified
        boolean requiresUpdate = datasetSystemLogDao.requiresProblemHierarchyUpdate(datasetItem);
        if (requiresUpdate) {
            StudentProblemRollupDao spDao = DaoFactory.DEFAULT.getStudentProblemRollupDao();
            spDao.callPopulateProblemHierarchy(datasetItem);
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            String elapsedTimeString = DateTools.getElapsedTimeString(startTime, endTime);

            SystemLogger.log(datasetItem, null, null,
                    SystemLogger.ACTION_UPDATED_PROBLEM_HIERARCHY,
                    logPrefix + "Updated Proble Hierarchy table. ",
                    Boolean.TRUE,
                    null, elapsedTime);
            logger.info("Updated the Problem Hierarchy table for dataset ("
                    + datasetItem.getId() + "), elapsed time: " + elapsedTimeString);

            return true;
        }

        return false;
    }

    /**
     * Process a dataset for aggregation.  A dataset is aggregated if it has been modified
     * or if the forceFlag has been set.  Aggregation is conducted on a sample by sample
     * basis within a dataset, so this method makes use of aggregateSample().
     * @param dataset the dataset to process.
     * @param requiredTxDurationCalc flag indicating if the aggregator just calculated
     * transaction durations for this dataset.
     * @return a dataset report.
     */
    private DatasetReport aggregateDataset(DatasetItem dataset, boolean requiredTxDurationCalc) {
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
        DatasetReport datasetReport = null;
        if (dataset != null
                && (dataset.getDeletedFlag() == null || !dataset.getDeletedFlag())) {
            // Only check and aggregate if dataset is not marked as deleted
            if (datasetRequiresAgg(dataset, requiredTxDurationCalc)) {
                //DS1295: KC Model Sort - set number of skills in a model
                List skillModelList = skillModelDao.find(dataset);
                for (Iterator kcmIter = skillModelList.iterator(); kcmIter.hasNext();) {
                    SkillModelItem skillModelItem = (SkillModelItem)kcmIter.next();
                    int numSkills = skillDao.getNumSkills(skillModelItem);
                    skillModelItem.setNumSkills(numSkills);
                    skillModelDao.saveOrUpdate(skillModelItem);
                }

                //Start regular aggregation activities
                datasetReport = new DatasetReport(dataset);
                datasetReport.setWasAggregated(true);

                //Create a list of all the samples for the given dataset.
                //Be sure the list has the default (All Data) sample and that that sample
                //is first in the list.  This is important in case we need to generate
                //problem events.
                List<SampleItem> allSamples = sampleDao.find(dataset);
                List<SampleItem> samplesToAgg = new ArrayList();
                SampleItem allDataSample = sampleDao.findOrCreateDefaultSample(dataset);
                samplesToAgg.add(allDataSample);
                if (allSamples.contains(allDataSample)) {
                    allSamples.remove(allDataSample);
                }
                samplesToAgg.addAll(allSamples);

                //Aggregate the samples.
                boolean datasetErrorOccurred = false;
                for (SampleItem sample : samplesToAgg) {
                    SampleReport sampleReport = aggregateSample(dataset, sample);
                    // DS978: a sample could be deleted while
                    // cron aggregator is running, causing a null
                    // sampleReport to be returned.
                    if (sampleReport != null) {
                        datasetReport.addSampleReport(sampleReport);
                        if (!sampleReport.successFlag) {
                            // default is true.
                            datasetReport.setSuccess(Boolean.FALSE);
                            datasetErrorOccurred = true;
                        }
                    }
                }

                datasetReport.setDone(); //sets finish time
                Long elapsedTime = datasetReport.getElapsedTime();
                if (!datasetErrorOccurred) {
                    DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                    dataset = datasetDao.get((Integer)dataset.getId());
                    DaoFactory.DEFAULT.getSkillModelDao().setAllReady(dataset);
                    String aggMsg = "Agg [" + formatForLogging(dataset) + "] "
                            + "Aggregation successful. "
                            + "Batch size: " + batchSize + " sessions. ";
                    logger.info(aggMsg);
                    SystemLogger.log(dataset, null, null, SystemLogger.ACTION_AGGREGATE,
                            aggMsg, Boolean.TRUE, elapsedTime);
                    datasetDao.saveOrUpdate(dataset);
                    datasetReport.setRollupFlagSet(true);
                } else {
                    datasetReport.setSuccess(false);
                    String aggMsg = "Agg [" + formatForLogging(dataset) + "] "
                            + "Aggregation failed. "
                            + "Batch size: " + batchSize + " sessions. ";
                    logger.error(aggMsg);
                    SystemLogger.log(dataset, null, null, SystemLogger.ACTION_AGGREGATE,
                            aggMsg, Boolean.FALSE, elapsedTime);
                }
                datasetReport.displayResults(false);
            } else {
                if (datasetOnlyFlag) {
                    logger.info("Aggregation not required for dataset "
                        + formatForLogging(dataset));
                }
            }
        }
        return datasetReport;
    } // end aggregateDataset()

    /**
     * Process a sample for aggregation.  First remove any existing transaction/sample mappings.
     * Then, re-populate the transaction/sample map and aggregate the data.
     * @param dataset the dataset in which this sample is contained.
     * @param sample the sample to process.
     * @return a sample report.
     */
    private SampleReport aggregateSample(DatasetItem dataset, SampleItem sample) {
        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
        // Only update the problem hierarchy if the dataset was modified
        if (updateProblemHierarchyTable(dataset)) {
            String logPrefixProblem = getLogPrefix(dataset);
            logger.info(logPrefixProblem + "Updated problem_hierarchy table for dataset "
            + dataset.getNameAndId(dataset.getDatasetName()) + ".");
        }
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        sample = sampleDao.get((Integer)sample.getId());
        boolean sampleErrorOccured = false;
        if (sample == null) {
            logger.error("Sample cannot be null.  Moving on to the next sample or dataset.");
            return null;
        }
        SampleReport sampleReport = new SampleReport(sample);
        String logPrefix = getLogPrefix(dataset, sample);
        try {
            logger.info(logPrefix + "Performing roll-up.");
            try {
                logger.info(logPrefix + "Removing any sample/transaction mappings for sample.");
                int numRemoved = sampleDao.removeAllTransactionMappings(sample);
                logger.info(logPrefix + "Removed " + numRemoved + " mappings.");

                sampleDao.populateTransactionSampleMap(sample);

                logger.info(logPrefix + "Removing all step rollups for sample.");
                numRemoved = stepRollupDao.removeAll(sample);
                logger.info(logPrefix + "Removed " + numRemoved + " rollups.");
            } catch (HibernateQueryException queryException) {
                logger.error(logPrefix
                        + "A hibernate query exception was thrown when trying to use sample.",
                        queryException);
                sampleReport.setDeleted(false);
                sampleReport.setException(queryException);
                sampleReport.setSuccess(false);
                return sampleReport;
            } // end catch(HibernateQueryException)

            int numRowsCreated = 0;
            numRowsCreated = stepRollupDao.callAggregatorStoredProcedure(sample, batchSize);
            long totalTime = new Date().getTime() - timeTracker.getTime();
            if (numRowsCreated < 0) {
                logger.error(logPrefix + "There was an error during aggregation. "
                        + "Removing all step roll-up data for sample.");
                stepRollupDao.removeAll(sample);
                sampleErrorOccured = true;
            } else {
                logger.info(logPrefix + "Created " + numRowsCreated + " stepRollups "
                   + "at the rate of "
                   + (int)((float)numRowsCreated / ((float)totalTime
                           / (float)ONE_THOUSAND)) + " per second");

                //update the sample_metric
                SampleMetricDao sampleMetricDao = DaoFactory.DEFAULT.getSampleMetricDao();
                sampleMetricDao.getTotalStudents(sample);
                sampleMetricDao.getTotalTransactions(sample);
            }

            if (sampleErrorOccured) {
                sampleReport.setSuccess(false);
                String msg = logPrefix + "Failed to aggregate new/modified sample. "
                                       + "Batch size: " + batchSize + " sessions. ";
                logger.error(msg);
                SystemLogger.log(dataset, null, sample,
                        SystemLogger.ACTION_AGGREGATE_SAMPLE, msg,
                        Boolean.FALSE,
                        sampleReport.getElapsedTime());
            } else {
                sampleReport.setSuccess(true);
                SystemLogger.log(dataset, null, sample,
                        SystemLogger.ACTION_AGGREGATE_SAMPLE,
                        logPrefix + "Aggregated new/modified sample. "
                                  + "Batch size: " + batchSize + " sessions. ",
                        Boolean.TRUE,
                        numRowsCreated, sampleReport.getElapsedTime());

                long lfaBackfillStartTime = System.currentTimeMillis();
                int numRowsBackfilled = runLfaBackfillSP(logPrefix, sample, dataset);
                long lfaBackfillEndTime = System.currentTimeMillis();
                if (numRowsBackfilled > 0) {
                    SystemLogger.log(dataset, null, sample,
                        SystemLogger.ACTION_LFA_BACKFILL,
                        logPrefix + "Ran the LFA backfill stored procedure.",
                        Boolean.TRUE,
                        numRowsBackfilled, lfaBackfillEndTime - lfaBackfillStartTime);
                }

            } // end else (sampleErrorOccurred)
        } catch (Exception exception) {
            String msg = logPrefix
                    + "An unknown error occurred while trying to aggregate sample.";
            logger.error(msg, exception);
            sendErrorEmail(logger, msg, exception);
            sampleReport.setSuccess(false);
            sampleReport.setException(exception);
        } // end catch(Exception)
        return sampleReport;
    } // end aggregateSample()

    /**
     * Determine if the LFA Backfill Stored Procedure should be run on the given sample.
     * If so, run it, otherwise return.
     * @param logPrefix a string of the dataset name, dataset id, sample name and sample id
     * @param sample the sample to consider.
     * @param datasetItem the dataset item
     * @return the number of rows where the predicted error rate was filled in
     */
    private int runLfaBackfillSP(String logPrefix, SampleItem sample, DatasetItem datasetItem) {
        int numRows = 0;
        if (runLfaBackfillFlag) {
            logger.info(logPrefix + "Running LFA Backfill stored procedure.");
            StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
            numRows = stepRollupDao.callLfaBackfillBySampleSP(sample, datasetItem);
            if (logger.isDebugEnabled()) {
                logger.debug(logPrefix + " " + numRows
                        + " rows have a predicted error rate");
            }
        } else {
            logger.info(logPrefix + "Skipping LFA Backfill, as runLfaBackfill flag is false.");
        }
        return numRows;
    }

    /**
     * This method is responsible for calling the calculate_tx_duration stored procedure
     * for the given dataset.  It is assumed that the need for calling this procedure on
     * the given dataset has been verified prior to calling this method.
     * @param dataset the dataset to process.
     */
    private void runTxDurationSP(DatasetItem dataset) {
        String logPrefix = getLogPrefix(dataset);
        logger.info(logPrefix + "Calculating tx durations as this dataset was modified.");
        TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
        long startTime = System.currentTimeMillis();
        boolean calcResult = txDao.callCalculateTxDurationSP(dataset);
        long endTime = System.currentTimeMillis();
        Long elapsedTime = endTime - startTime;

        if (calcResult) {
            String info = logPrefix + "Set tx duration values.";
            SystemLogger.log(dataset, null, null,
                    SystemLogger.ACTION_SET_TX_DURATION,
                    info,
                    Boolean.TRUE, elapsedTime);
            logger.info(info);
        } else {
            String info = logPrefix
                + "Exception occurred while calculating tx duration values";
            logger.error(info);
        }
    }

    /**
     * Helper method to indicate if a dataset should be aggregated.  If the dataset has been
     * modified or the force flag has been set, the aggregator will run on the dataset.
     * @param dataset the dataset to check.
     * @param requiredTxDurationCalc flag indicating if the aggregator just calculated
     * transaction durations for this dataset.
     * @return true if the number of transactions in the dataset is > 0 AND
     *      the forceFlag is set or the dataset has been modified, false otherwise.
     */
    private boolean datasetRequiresAgg(DatasetItem dataset, boolean requiredTxDurationCalc) {
        boolean result = false;
        boolean requiresAgg = DatasetState.requiresAggregation(dataset);
        long transactionCount = DaoFactory.DEFAULT.getTransactionDao().count(dataset);
        if (transactionCount > 0) {
            if (requiresAgg || requiredTxDurationCalc || forceAll) {
                result = true;
            }
        }
        return result;
    } // end datasetRequiresAgg()

    /**
     * Create an appropriate run mode message based on the various run mode flags.
     * @param dataset the dataset we are processing.
     * @return a nicely formatted message.
     */
    private String getRunModeMsg(DatasetItem dataset) {
        return getRunModeMsg(dataset, null, null);
    }

    /**
     * Create an appropriate run mode message based on the various run mode flags.
     * @param sample the sample we are processing.
     * @return a nicely formatted message.
     */
    private String getRunModeMsg(SampleItem sample) {
        return getRunModeMsg(null, sample, null);
    }

    /**
     * Create an appropriate run mode message based on the various run mode flags.
     * @param model the skill model we are processing.
     * @return a nicely formatted message.
     */
    private String getRunModeMsg(SkillModelItem model) {
        return getRunModeMsg(null, null, model);
    }

    /**
     * Create an appropriate run mode message based on the various run mode flags.
     * @return a nicely formatted message.
     */
    private String getRunModeMsg() {
        return getRunModeMsg(null, null, null);
    }

    /**
     * Create an appropriate run mode message based on the various run mode flags.
     * @param sample the sample we are processing.
     * @param dataset the dataset we are processing
     * @return a nicely formatted message.
     */
    private String getRunModeMsg(DatasetItem dataset, SampleItem sample, SkillModelItem model) {
        StringBuffer msg = new StringBuffer();
        msg.append("Aggregator run mode: ");
        if (sampleOnlyFlag) {
            msg.append("sample only");
            msg.append(" on sample " + formatForLogging(sample));
        } else if (datasetOnlyFlag) {
            msg.append("dataset only");
            msg.append(" on dataset " + formatForLogging(dataset));
        } else if (modelOnlyFlag) {
            msg.append("model only,");
            msg.append(" on model " + formatForLogging(model));
        } else {
            // everyday mode
            msg.append("everyday mode");
        }

        if (forceAll) {
            msg.append(", force");
        }

        if (runLfaBackfillFlag) {
            msg.append(", lfaBackFill");
        } else {
            msg.append(", no lfaBackfill");
        }
        msg.append(".");
        return msg.toString();
    } // end getRunModeMsg()

    /**
     * Inner class DatasetReport - report object for a dataset.
     */
    private class DatasetReport {
        /** The dataset aggregated */
        private DatasetItem dataset;
        /** flag indicating success of the aggregation */
        private Boolean successFlag = Boolean.TRUE;
        /** Flag indicating that aggregation occurred for this dataset. Default = false */
        private Boolean aggregatedFlag;
        /** List of all sample reports for this dataset */
        private List sampleReports;
        /** Start time. Set at instantiation. */
        private long startTime;
        /** Finish time. Set when the success flag is set */
        private long finishTime;
        /** Flag indicating that the rollup time was set for this dataset. */
        private Boolean rollupTimeSetFlag = Boolean.FALSE;


        /**
         * Constructor.
         * @param dataset the dataset set item aggregated.
         */
        public DatasetReport(DatasetItem dataset) {
            if (dataset == null) {
                throw new IllegalArgumentException("Dataset cannot be null");
            }
            this.dataset = dataset;
            sampleReports = new LinkedList();
            startTime = System.currentTimeMillis();
            aggregatedFlag = Boolean.FALSE;
        }

        /**
         * Sets the success/failure of this dataset aggregation.
         * @param successFlag boolean of success.
         */
        public void setSuccess(boolean successFlag) {
            this.successFlag = Boolean.valueOf(successFlag);
        }

        /**
         * Gets the success/failure of this dataset aggregation.
         * @return Boolean successFlag
         */
        public Boolean getSuccess() { return this.successFlag; }

        /** Sets the finished time for recording the total amount of item on the dataset */
        public void setDone() {
            finishTime = System.currentTimeMillis();
        }

        /**
         * Sets the flag indicating whether the rollup time was set for this dataset
         * @param flag boolean to set*/
        public void setRollupFlagSet(boolean flag) {
            this.rollupTimeSetFlag = Boolean.valueOf(flag);
        }

        /**
         * Sets whether of not this dataset was aggregated.
         * @param aggregatedFlag true if dataset was aggregated.
         */
        public void setWasAggregated(boolean aggregatedFlag) {
            this.aggregatedFlag = Boolean.valueOf(aggregatedFlag);
        }

        /**
         * Add a sample report to the list of samples for this dataset report.
         * @param report SampleReport to add.
         */
        public void addSampleReport(SampleReport report) {
            sampleReports.add(report);
            if (!report.wasSuccessful()) { successFlag = false; }
        }

        /**
         * Creates a string of the cumulated success/errors for this dataset.
         * Default call that will display sample reports as well.
         * @return String report for this dataset.
         */
        public String displayResults() {
            return displayResults(true);
        }

        /**
         * Returns a string with the total amount of time the aggregation took.
         * @return String of the total time.
         */
        public String getTotalTimeString() {
            return DateTools.getElapsedTimeString(startTime, finishTime);
        }

        /**
         * Returns the elapsed time in milliseconds.
         * @return the elapsed time
         */
        public long getElapsedTime() {
            return finishTime - startTime;
        }

        /**
         * Creates a string of the cumulated success/errors for this dataset.
         * @param displaySamples flag indicating whether or not to display samples.
         * @return String report for this dataset.
         */
        public String displayResults(boolean displaySamples) {
            String toDisplay = "\nDataset: " + dataset.getDatasetName()
                + " (" + dataset.getId() + ")";
            if (aggregatedFlag) {
                if (successFlag == null) {
                    toDisplay += " WARNING SuccessFlag not set, unknown results";
                    return toDisplay;
                }

                if (successFlag) {
                    toDisplay += " Results: SUCCESS";
                } else {
                    toDisplay += " Results: ERROR";
                }

                toDisplay += " " + getTotalTimeString();

                if (rollupTimeSetFlag) {
                    toDisplay += " Rollup-Time: SET";
                } else {
                    toDisplay += " Rollup-Time: NOT SET";
                }

                if (displaySamples) {
                    for (Iterator it = sampleReports.iterator(); it.hasNext();) {
                        SampleReport report = (SampleReport)it.next();
                        toDisplay += "\n" + report.displayResults();
                    }
                }
                toDisplay += "\n";

            } else {
                if (logger.isDebugEnabled()) {
                    toDisplay += " Results: SKIPPED\n";
                } else {
                    toDisplay = "";
                }
            }
            return toDisplay;
        }

        /**
         * To string method for easy printing.
         * @return String to display
         */
        public String toString() {
            return displayResults();
        }

    }

    /**
     * Inner class SampleReport - report object for a sample.
     */
    private class SampleReport {
        /** The sample aggregated */
        private SampleItem sample;
        /** Flag indicating success of the aggregation for the named sample */
        private Boolean successFlag = null;
        /** Any exception thrown */
        private Exception exception = null;
        /** Start time. Set at instantiation. */
        private long startTime;
        /** Finish time. Set when the success flag is set */
        private long finishTime;
        /** Flag indicating that this sample was deleted due to error. */
        private Boolean deletedFlag = Boolean.FALSE;

        /**
         * Constructor.
         * @param sample the sample aggregated.
         */
        public SampleReport(SampleItem sample) {
            this.sample = sample;
            this.startTime = System.currentTimeMillis();
        }

        /**
         * Sets the success/failure of this sample aggregation.
         * @param successFlag boolean of success.
         */
        public void setSuccess(boolean successFlag) {
            this.successFlag = Boolean.valueOf(successFlag);
            finishTime = System.currentTimeMillis();
        }

        /**
         * Indicates whether the aggregate on this sample was successful.
         * @return boolean of success.
         */
        public boolean wasSuccessful() {
            return successFlag;
        }

        /**
         * Sets the the flag indicating whether or not this sample was deleted.
         * @param deletedFlag boolean of deleted.
         */
        public void setDeleted(boolean deletedFlag) {
            this.deletedFlag = Boolean.valueOf(deletedFlag);
        }

        /**
         * Sets the exception if any exception occurred.
         * @param exception The exception that occurred during aggregation.
         */
        public void setException(Exception exception) {
            this.exception = exception;
        }

        /**
         * Returns a string with the total amount of time the aggregation took.
         * @return String of the total time.
         */
        public String getTotalTimeString() {
            return DateTools.getElapsedTimeString(startTime, finishTime);
        }

        /**
         * Returns the difference between the finishTime and the startTime.
         * @return the amount of elapsed time in milliseconds
         */
        public Long getElapsedTime() {
            return new Long(finishTime - startTime);
        }

        /**
         * Create a string of the results of this aggregation
         * @return String of the results of this samples aggregation.
         */
        public String displayResults() {
            String toDisplay = "\tSample: " + sample.getSampleName() + " (" + sample.getId() + ")";
            if (successFlag == null) {
                toDisplay += " Results: Unknown";
                return toDisplay;
            }
            if (successFlag) {
                toDisplay += " Results: SUCCESS ";
                toDisplay += getTotalTimeString();
            } else {
                toDisplay += " Results: ERRROR";
                if (deletedFlag) {
                    toDisplay += " Sample DELETED!";
                }
                if (exception != null) {
                    toDisplay += "\n\t\tException: " + exception;
                }
            }
            return toDisplay;
        }

        /**
         * To string method for easy printing.
         * @return String to display
         */
        public String toString() {
            return displayResults();
        }
    }

    /**
     * Parse the command line arguments to get the file and curriculum names.
     * @param args Command line arguments
     * @return returns null if no exit is required,
     * 0 if exiting successfully (as in case of -help),
     * or any other number to exit with an error status
     */
    protected Integer handleOptions(String[] args) {
        // The value is null if no exit is required,
        // 0 if exiting successfully (as in case of -help),
        // or any other number to exit with an error status
        Integer exitLevel = null;

        if (args != null && args.length != 0) {

            java.util.ArrayList argsList = new java.util.ArrayList();
            for (int i = 0; i < args.length; i++) {
                argsList.add(args[i]);
            }

            // loop through the arguments
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-h")) {
                    displayUsage();
                    exitLevel = 0;
                } else if (args[i].equals("-f") || args[i].equals("-force")) {
                    this.forceAll = true;
                } else if (args[i].equals("-noLFA")) {
                    this.runLfaBackfillFlag = false;
                } else if (args[i].equals("-skipJunk")) {
                    this.skipJunkFlag = true;
                } else if (args[i].equals("-e") || args[i].equals("-email")) {
                    setSendEmailFlag(true);
                    if (++i < args.length) {
                        setEmailAddress(args[i]);
                    } else {
                        System.err.println(
                            "Error: an email address must be specified with this argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-d") || args[i].equals("-dataset")) {
                    this.datasetOnlyFlag = true;
                    if (++i < args.length) {
                        try {
                            this.datasetIdToRun = Integer.parseInt(args[i]);
                            if (datasetIdToRun < 0) {
                                throw new Exception();
                            }
                        } catch (Exception exception) {
                            logger.error("Error while trying to parse dataset id. "
                                    + "Please check the parameter for accuracy.");
                            exitLevel = 1;
                        }
                    } else {
                        logger.error("A dataset id must be specified with the -dataset argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-m") || args[i].equals("-model")) {
                    this.modelOnlyFlag = true;
                    if (++i < args.length) {
                        try {
                            this.modelIdToRun = Long.parseLong(args[i]);
                            if (modelIdToRun < 0) {
                                throw new Exception();
                            }
                        } catch (Exception exception) {
                            logger.error("Error while trying to parse model id. "
                                         + "Please check the parameter for accuracy.");
                            exitLevel = 1;
                        }
                    } else {
                        logger.error("A model id must be specified with the -model argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-spPath") || args[i].equals("-spFilePath")) {
                    if (++i < args.length) {
                        aggSPFilePath = args[i];
                    } else {
                        logger.error("The aggregator stored procedure file location "
                                     + "must be specified with the -spFilePath argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-s") || args[i].equals("-sample")) {
                    this.sampleOnlyFlag = true;
                    if (++i < args.length) {
                        try {
                            this.sampleIdToRun = Integer.parseInt(args[i]);
                            if (sampleIdToRun < 0) {
                                throw new Exception();
                            }
                        } catch (Exception exception) {
                            logger.error("Error while trying to parse sample id. "
                                    + "Please check the parameter for accuracy.");
                            exitLevel = 1;
                        }
                    } else {
                        logger.error("A sample id must be specified with the -sample argument");
                        displayUsage();
                        exitLevel = 1;
                    }

                } else if (args[i].equals("-b") || args[i].equals("-batchSize")) {
                    if (++i < args.length) {
                        try {
                            this.batchSize = Integer.parseInt(args[i]);
                            if (batchSize <= 0) {
                                logger.error("The batch size must be greater than zero.");
                                displayUsage();
                                exitLevel = 1;
                            }
                        } catch (NumberFormatException exception) {
                            logger.error("Error while trying to parse batch size: " + args[i]);
                            exitLevel = 1;
                        }
                    } else {
                        logger.error("A batch size must be specified with the -batchSize argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                }
                // If the exitLevel was set, then break out of the loop
                if (exitLevel != null) {
                    break;
                }
            } // end for loop
        }
        return exitLevel;
    } // end handleOptions

    /**
     * Displays the command line arguments for this program.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... " + "Aggregator");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\t usage info");
        System.err.println("\t-e, email\t send email if major failure");
        System.err.println("\t-f\t force update on all datasets");
        System.err.println("\t-b\t batchSize number of sessions to batch in SP");
        System.err.println("\t-dataset\t dataset id to run the generator on");
        System.err.println("\t-model\t model id to run the generator on");
        System.err.println("\t-sample\t sample id to run the generator on");
        System.err.println("\t-noLFA\t do not run LFA backfill");
        System.err.println("\t-skipJunk\t do not run LFA backfill");
        System.err.println("\t-spFilePath\t\t\t location of the aggregator stored procedure file");
    }

    /**
     * Main program to run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("Aggregator.main");
        String version = VersionInformation.getReleaseString();
        logger.info("Aggregator starting (" + version + ")...");
        Aggregator aggregator = ExtractorFactory.DEFAULT.getAggregator();

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        boolean playMode = ImportQueue.isInPlayMode();
        // If an exitLevel exists, it will be used to exit
        // before the agg is run; otherwise exitLevel is null so continue.
        Integer exitLevel = null;
        // Handle the command line options:
        // The handleOptions method is called before entering the try-catch block
        // because it isn't affected by the ImportQueue.
        exitLevel = aggregator.handleOptions(args);
        try {
            // If exitLevel is null, then proceed with aggregation
            if (exitLevel == null) {
                // Pause the IQ
                if (playMode) {
                    logger.info("main:: Pausing the ImportQueue.");
                    ImportQueue.pause();
                }
                // Process options
                if (aggregator.isSendEmail()) {
                    logger.info("Will send email to " + aggregator.getEmailAddress()
                            + " if an unknown error occurs.");
                } else {
                    logger.info("Will NOT send email to " + aggregator.getEmailAddress()
                            + " if an unknown error occurs.");
                }
                // Now do the dirty work ...

                Boolean successFlag = aggregator.populateStepRollup();
                // If the aggregator returned a false successFlag, then
                // set the exitLevel to error
                if (!successFlag) {
                    exitLevel = 1;
                }
            }
        } catch (Throwable throwable) {
            // Log error and send email if this happens!
            aggregator.sendErrorEmail(logger, "Unknown error in main method.", throwable);
            exitLevel = 1;
        } finally {
            if (playMode) {
                logger.info("main:: Unpausing the ImportQueue.");
                ImportQueue.play();
            }
            exitOnStatus(exitLevel);
            logger.info("Aggregator done.");
        }
    } // end main

    /**
     * Sets the forceAll.
     * @param forceAll The forceAll to set.
     */
    public void setForceAll(boolean forceAll) {
        this.forceAll = forceAll;
    }

} // end Aggregator.java
