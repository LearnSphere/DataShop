/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;


import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate.HibernateQueryException;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.servlet.AggregatorBeanAssistant;

/**
 *  Aggregator Bean that implements runnable.  This bean will be spawned in
 *  a separate thread to keep alive the aggregation process.  Spring injects
 *  the transactionTemplate on creation, and wraps the bean in a hibernate interceptor.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15747 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-12-11 09:46:48 -0500 (Tue, 11 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AggregatorBean implements Runnable, Serializable {

    /** Debug logging. */
    private static Logger logger =
        Logger.getLogger("edu.cmu.pslc.datashop.servlet.AggregatorBean");

    /** Used to customize the set of procedures for sample creation. */
    private static final String TO_INSERT_BASE = "SAMPLE_";

    /** Flag indicating this thread is running. */
    private volatile boolean running;
    /** Flag indicating the data for this thread are initializing. */
    private volatile boolean initializing;
    /** Flag indicating this thread should cancel operations and exit. */
    private volatile boolean cancel;
    /** Flag indicating the final result has been reached. */
    private volatile boolean confirmedDone;

    /** # of rows inserted into the step_rollup table. */
    private volatile int numRowsCreated;
    /** Estimated time to complete the aggregation. */
    private volatile long estimatedTime;
    /** Time at which the aggregation started. */
    private Date startTime;

    /** Sample to aggregate. */
    private SampleItem sample;

    /** Visibility of sample */
    private Boolean isGlobal;

    /** The location of the aggregator stored procedure file. */
    private String aggSpFilePath;

    /** Sample dao for getting items by sample */
    private SampleDao sampleDao;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

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

    /** magic 1000! */
    private static final int MILLISECONDS_PER_SEC = 1000;
    /** magic 99! */
    private static final int ALMOST_BUT_NOT_QUITE_DONE = 99;
    /** magic 100! */
    private static final int COMPLETELY_DONE = 100;

    /** Default constructor. */
    public AggregatorBean() {
        running = false;
        cancel = false;
        confirmedDone = false;
        this.numRowsCreated = 0;
    }

    /**
     * Sets all the attributes need to aggregate.
     * @param sample the sample we wish to aggregate data for.
     * @param isGlobal visibility of the sample
     * @param aggSpFilePath the path to the aggregator_sp.sql file on disk.
     */
    public void setAttributes(SampleItem sample, Boolean isGlobal, String aggSpFilePath) {
        this.sample = sample;
        this.isGlobal = isGlobal;
        this.aggSpFilePath = aggSpFilePath;
    }

    /**
     * Get the percentage done on the build.
     * @return the percent (0-100);
     */
    public synchronized int getPercent() {
        if (isInitializing()) {
            logDebug("getPercent :: still initializing, returning 0");
            return 0;
        }

        if (estimatedTime == 0 && numRowsCreated > 0) {
            logger.info("Estimated time is 0, returning 100.");
            SampleItem theSample = sampleDao.get((Integer)sample.getId());
            theSample.setGlobalFlag(isGlobal);
            sampleDao.saveOrUpdate(theSample);
            return COMPLETELY_DONE;
        } else if (numRowsCreated != -1) {
            // calculate the percent complete. If it is 100, check to see if the bean is
            // confirmed done.  If yes, return 100, otherwise return 99.  If percent is not
            // at 100, return its actual value.

            if (confirmedDone) {
                SampleItem theSample = sampleDao.get((Integer)sample.getId());
                theSample.setGlobalFlag(isGlobal);
                sampleDao.saveOrUpdate(theSample);
                return COMPLETELY_DONE;
            } else {
                int percent = calculatePercent();
                logDebug("getPercent :: current percent = ", percent);
                if (percent > COMPLETELY_DONE) {
                    percent = ALMOST_BUT_NOT_QUITE_DONE;
                }

                DatasetSystemLogDao dslDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
                String status = dslDao.sampleAggComplete(sample);
                logDebug("agg status from db :: " + status);

                if (status.equals(DatasetSystemLogDao.STATUS_ERROR)) {
                    logger.error(
                            "There was an error when checking for aggregate sample log action.");
                    return -1;
                } else if (status.equals(DatasetSystemLogDao.STATUS_OK)) {
                    confirmedDone = true;
                    // return 99 because we only want one piece of code to return 100.
                    return ALMOST_BUT_NOT_QUITE_DONE;
                } else {
                    logDebug("waiting for complete.");
                    if (percent >= COMPLETELY_DONE) {
                        percent = ALMOST_BUT_NOT_QUITE_DONE;
                    }
                    return percent;
                }
            }
        } else {
            confirmedDone = true;
            logDebug("getPercent :: There was an error in the thread.");
            return -1;
        }
    }

    /**
     * Cancel the aggregation process and remove the newly created sample.
     */
    public synchronized void cancel() { cancel = true; }

    /**
     * Flag indicating if this build has completed.
     * @return boolean
     */
    public synchronized boolean isCompleted() {
        return confirmedDone;
    }

    /**
     * Flag indicating if this build is initializing.
     * @return boolean
     */
    public synchronized boolean isInitializing() {
        return initializing;
    }

    /**
     * set the flag indicating if this build is initializing.
     * @param init whether or not this is initializing.
     */
    public synchronized void setIntitializing(boolean init) {
        this.initializing = init;
    }

    /**
     * Flag indicating if this build is running.
     * @return boolean
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * set the flag indicating if this build is running.
     * @param running whether or not this is running.
     */
    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Stops this bean running.
     */
    public void stop() {
        cancel = true;
    }

    /**
     * Run the bean.  First check to make sure the sample is not null.  Next, remove any existing
     * transaction/sample mappings.  Customize the aggregator_sp.sql file for this sample,
     * then call it.
     */
    public void run() {
        try {
            logger.info("Performing Data Aggregation on sample " + formatForLogging(sample) + ".");
            sampleDao = DaoFactory.DEFAULT.getSampleDao();
            setIntitializing(true);
            setRunning(true);

            if (sample == null || sample.getId() == null) {
                setRunning(false);
                return;
            }
            this.startTime = new Date();

            //setIntitializing(false); //used to prevent divide by zeros.
            int numMappingsInserted = 0;
            try {
                logDebug("Removing any sample/transaction mappings for sample ",
                        formatForLogging(sample), ".");
                sampleDao.removeAllTransactionMappings(sample);
                logDebug("removeAllTransactionMappings called");
                numMappingsInserted = sampleDao.populateTransactionSampleMap(sample);
                logDebug("populateTransactionSampleMap called");

                this.estimatedTime = getEstimatedTime(numMappingsInserted);
                logDebug("Estimated time is :: ", estimatedTime);
            } catch (HibernateQueryException queryException) {
                String sampleNameForLogging = formatForLogging(sample);
                logger.error("A hibernate query exception was thrown when trying"
                        + " to use sample " + sampleNameForLogging, queryException);
            }
            setIntitializing(false); //used to prevent divide by zeros.
            /**
             * We can't aggregate a sample that has no transaction mappings,
             * so something must have gone wrong.
             */
            if (numMappingsInserted == 0) {
                logger.error("Populating the tx_sample map for sample "
                        + formatForLogging(sample) + " was unsuccessful.");
                numRowsCreated = -1;
                cancel = true;
            } else {
               updateProblemHierarchyTable(sample.getDataset());
               numRowsCreated = populateStepRollup();
            }
        } catch (Throwable throwable) {
            if (sample != null) {
                sample = sampleDao.get((Integer)sample.getId());
                if (sample != null) {
                    logger.error("Throwable occurred while aggregating, "
                            + "exiting on sample "
                            + formatForLogging(sample), throwable);
                    SystemLogger.log(sample.getDataset(), SystemLogger.ACTION_AGGREGATE_SAMPLE,
                            "Aggregated failed for new/modified sample " + formatForLogging(sample),
                            Boolean.FALSE);
                } else {
                    logger.error("Throwable occurred while aggregating, "
                            + "exiting, sample is null.", throwable);
                }
            } else {
                logger.error("Throwable occurred while aggregating, "
                        + "exiting, sample is null.", throwable);
            }
            numRowsCreated = -1;
            setRunning(false);
            setIntitializing(false);
        } finally {
            setRunning(false);
            transactionTemplate.setTimeout(-1);
            if (sample != null) {
                logger.info("Aggregator Bean thread stop :: sample: " + formatForLogging(sample));
                /* Defer making global until aggregation has finished. */
                // now drop the customized procedures and functions
                logDebug("Dropping aggregator stored procedures for ", sample);
                DaoFactory.DEFAULT.getStepRollupDao().dropAll(sample, TO_INSERT_BASE);
            } else {
                logger.info("Aggregator Bean thread stop :: sample is null");
            }
        } // end finally

        try {
            if (cancel) {
                handleCancel();
            } else {
                finishUp();
            }
        } catch (InterruptedException e) {
            logger.error("Error occurred while finishing up", e);
        }
    } // end run

    /** Magic 10! */
    private static final int MAGIC_TEN = 10;

    /**
     * Update the problem hierarchy table prior to aggregation for a dataset.
     * @param datasetItem the dataset item
     */
    private void updateProblemHierarchyTable(DatasetItem datasetItem) {
        StudentProblemRollupDao spDao = DaoFactory.DEFAULT.getStudentProblemRollupDao();
        spDao.callPopulateProblemHierarchy(datasetItem);
    }
    /**
     * Calculate the percent complete.
     * @return the percent value.
     */
    private int calculatePercent() {
        Date currentTime = new Date();
        //to avoid a divide by zero...
        if (estimatedTime == 0) {
            return COMPLETELY_DONE;
        }
        int percent =
            (int)(((currentTime.getTime() - startTime.getTime()) / (estimatedTime))
                    / MAGIC_TEN);
        return percent;
    }

    /**
     * Given a number of mappings inserted, calculate an estimated time of
     * completion for the current aggregation process.  This involves using a predetermined
     * estimation factor and multiplying that by the number of skill models in the dataset.
     * @param numMappings the number of sample/transaction mappings.
     * @return an estimated time to completion.
     */
    private long getEstimatedTime(int numMappings) {
        SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
        DatasetItem dataset = sample.getDataset();
        int numSkillModels = skillModelDao.find(dataset).size();
        long time = Math.round((numMappings
                / AggregatorBeanAssistant.ESTIMATION_FACTOR * numSkillModels));
        return time;
    }

    /**
     * Populate the step rollup table for the new sample.  First, customize the
     * aggregator_sp.sql file for this sample and sample_id.  Load the file to the DB
     * and call the stored procedure.  Finally, clean up after ourselves and remove
     * the customized procedures from the DB.
     * @return the number of step_rollup rows created.
     */
    private int populateStepRollup() {
        boolean loadSuccess = false;
        int rowsCreated = 0;
        String toInsert = TO_INSERT_BASE + sample.getId();
        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();

        loadSuccess = stepRollupDao.loadAggregatorStoredProcedure(aggSpFilePath, toInsert);
        if (loadSuccess) {
            rowsCreated =
                stepRollupDao.callAggregatorStoredProcedure(sample, toInsert);
        }
        if (rowsCreated < 0) {
            // there was a problem
            logger.error("An error occurred while attempting to aggregate sample "
                    + formatForLogging(sample) + ".");
            cancel = true;
        }
        return rowsCreated;
    }

    /**
     * Cancel the current aggregation.  Since a user cannot cancel a sample creation
     * once it has started to aggregate, cancels can only occur from within the code.
     * Set the bean.running to false.
     */
    private void handleCancel() {
        logDebug("Canceling Aggregator bean thread. Removing all step roll-up data for sample ",
                formatForLogging(sample), ".");
        setRunning(false);
        setIntitializing(false);
        sample = sampleDao.get((Integer)sample.getId());
        SystemLogger.log(sample.getDataset(), SystemLogger.ACTION_AGGREGATE_SAMPLE,
                "Aggregated canceled for new/modified sample " + formatForLogging(sample));

    }

    /**
     * Finish up the sample creation.  Get the sample id and enter a system log
     * indicating the process has finished.  Finally, sleep for a little bit.
     * @throws InterruptedException exception
     */
    private void finishUp() throws InterruptedException {
        String logPrefix = getLogPrefix(null, null);
        if (sample != null) {
             logPrefix = getLogPrefix(sample.getDataset(), sample);
        }
        SampleMetricDao sampleMetricDao = DaoFactory.DEFAULT.getSampleMetricDao();
        Long numTxs = sampleMetricDao.getTotalTransactions(sample);
        logger.info(logPrefix + "Done aggregating sample "
                + " with " + numTxs + " transactions");
        long endTime = System.currentTimeMillis();
        if (sample != null) {
            sample = sampleDao.get((Integer)sample.getId());
        }
        if (sample != null) {
            DatasetItem datasetItem = sample.getDataset();
            SystemLogger.log(datasetItem, null, sample,
                    SystemLogger.ACTION_AGGREGATE_SAMPLE,
                    logPrefix +  "Aggregated new/modified sample "
                    + "with " + numTxs + " transactions. ",
                    Boolean.TRUE,
                    numRowsCreated,
                    endTime - startTime.getTime());

            StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
            long lfaBackfillStartTime = System.currentTimeMillis();
            int numRowsBackfilled = stepRollupDao.callLfaBackfillBySampleSP(sample, datasetItem);
            long lfaBackfillEndTime = System.currentTimeMillis();
            logDebug(logPrefix, numRowsBackfilled, " rows have a predicted error rate in sample.");
            if (numRowsBackfilled > 0) {
                SystemLogger.log(datasetItem, null, sample,
                    SystemLogger.ACTION_LFA_BACKFILL,
                    logPrefix + "Ran the LFA backfill stored procedure.",
                    Boolean.TRUE,
                    numRowsBackfilled, lfaBackfillEndTime - lfaBackfillStartTime);
            }
        } else {
            logger.error("Sample is null, cannot make SystemLogger call.");
        }
        //keep the thread alive while a call come back for the item.
        for (int i = 0; i < COMPLETELY_DONE && !confirmedDone; i++) {
            Thread.sleep(MILLISECONDS_PER_SEC);
        }
    }

    /**
     * Helper method for logging nicely.
     * @param datasetItem the dataset we care about.
     * @param sampleItem the sample we care about.
     * @return a nicely formatted string for logging
     */
    public String getLogPrefix(DatasetItem datasetItem, SampleItem sampleItem) {
        String prefix = "Agg [";
        if (datasetItem != null) {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
            prefix += datasetItem.getDatasetName() + " (" + datasetItem.getId() + ")";
        } else {
            prefix += "dataset is null";
        }
        if (sampleItem != null) {
            prefix += " / " + sampleItem.getSampleName() + " (" + sampleItem.getId() + ")";
        } else {
            prefix += "sample is null";
        }
        prefix += "] ";
        return prefix;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Helper method to format sample name and id for logging purposes.
     * @param sample the sample
     * @return a nicely formatted string.
     */
    private String formatForLogging(SampleItem sample) {
        return ("'" + sample.getSampleName() + "' (" + sample.getId() + ")");
    }

} // end AggregatorBean.java
