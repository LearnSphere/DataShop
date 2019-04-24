/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.KCModelStepExportDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.StepRollupExportOptions;
import edu.cmu.pslc.datashop.dto.StudentProblemRollupOptions;
import edu.cmu.pslc.datashop.dto.TxExportInfo;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper.TxExportTask;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * The CachedExportFileGenerator is responsible for examining datasets and creating
 * zip files containing a cached export for each sample within a dataset.
 *
 * @author Kyle A Cunningham
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 *
 */
public class CachedExportFileGenerator extends AbstractExtractor {
    /** Logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** TransactionExportHelper for creating cached files. */
    private TransactionExportHelper helper;
    /** Boolean to force a reload of all data. */
    private boolean forceFlag;
    /** Run on a specific dataset. */
    private boolean datasetOnlyFlag;
    /** Specific dataset to run the generator on. */
    private Integer datasetIdToRun;
    /** Run on a specific sample. */
    private boolean sampleOnlyFlag;
    /** Specific sample to run the generator on. */
    private Integer sampleIdToRun;
    /** maximum number of students to process at one time. */
    private int studentBatchSize;
    /** Whether or not the -sb flag was specified for Student-Problem export. */
    private boolean isBatchSpecified = false;
    /** Base directory for the files associated with a dataset. */
    private String baseDir;
    /** Where to find the transaction export stored procedure template. */
    private String txExportSPFilePath;
    /** Flag indicating if the CFG should look for a 'log conversion done' message. */
    private boolean waitForLogConversionDone;
    /** Whether to perform step exports, or not. */
    private boolean doStep = false;
    /** Whether to perform transaction exports, or not. */
    private boolean doTx = false;
    /** Whether to perform student-problem exports, or not. */
    private boolean doProblem = false;
    /** Whether to perform KC model exports, or not. */
    private boolean doKcm = false;
    /** Flag indicating whether to skip datasets marked as junk.  Default is false.*/
    private boolean skipJunkFlag = false;

    /**
     * Whether to perform step exports, or not.
     * @return whether to perform step exports, or not
     */
    public boolean isDoStep() { return doStep; }

    /**
     * Whether to perform transaction exports, or not.
     * @return the doStep whether to perform transaction exports, or not
     */
    public boolean isDoTx() { return doTx; }

    /**
     * Whether to perform student-problem exports, or not.
     * @return the doStep whether to perform student-problem exports, or not
     */
    public boolean isDoProblem() { return doProblem; }

    /**
     * Whether to perform KC model exports, or not.
     * @return to doKcm flag
     */
    public boolean isDoKcm() { return doKcm; }

    /** Number of minutes to sleep while waiting for log conversion to finish.
     * Default is 10 minutes in milliseconds. */
    private Integer minutesToSleep = TEN_MINUTES;

    /** Initial size of the string buffer. */
    protected static final int STRING_BUFFER_SIZE = 262144;
    /** Default value for studentBatchSize. */
    public static final int DEFAULT_STUDENT_BATCH_SIZE = 500;
    /** 10 Minutes in milliseconds. */
    private static final int TEN_MINUTES = 600000;
    /** Factor to convert minutes to milliseconds "60000". */
    private static final int MILLISECOND_FACTOR = 60000;

    /** Constructor! */
    public CachedExportFileGenerator() {
        forceFlag = false;
        datasetOnlyFlag = false;
        sampleOnlyFlag = false;
        studentBatchSize = DEFAULT_STUDENT_BATCH_SIZE;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if info is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }

    /**
     * Create a cached transaction export file for each sample within a dataset.
     * The following options are available:
     * <ul>
     *    <li>datasetOnly</li>
     *    <li>datasetOnly and force</li>
     *    <li>sampleOnly</li>
     *    <li>sampleOnly and force</li>
     *    <li>force all</li>
     *    <li>transactions only</li>
     *    <li>student steps only</li>
     *    <li>student problems only</li>
     *    <li>kc models only</li>
     *    <li>run in everyday mode (transaction and student-step)</li>
     * </ul>
     * Running in everyday mode will cache a dataset if one of the following is true:
     * <ul>
     *     <li>Dataset has been modified since the last cached export
     *     generation (data added recently)</li>
     *     <li>KC Model import has occurred for the dataset</li>
     *     <li>User has created a sample since last cached export generation.</li>
     * </ul>
     * @throws SQLException if something goes wrong, God forbid
     */
    private void cacheExportFiles() throws SQLException {
        helper = HibernateExtractorFactory.DEFAULT.getTransactionExportHelper();

        List<DatasetItem> datasetList = new ArrayList<DatasetItem>();
        List<SampleItem> sampleList = new ArrayList<SampleItem>();

        if (datasetOnlyFlag && !forceFlag) {
            logger.info("Running in Dataset Only mode.");
            DatasetItem dataset = DaoFactory.DEFAULT.getDatasetDao().get(datasetIdToRun);
            if (dataset != null
                    && (dataset.getDeletedFlag() == null
                            || dataset.getDeletedFlag().equals(false))) {
                datasetList.add(dataset);
                logger.info("Running cached export file generation for "
                        + helper.formatForLogging(dataset));
            } else {
                logErrorRetrievingDataset(datasetIdToRun);
                return;
            }
        }
        if (sampleOnlyFlag && !forceFlag) {
            logger.info("Running in Sample Only mode.");
            SampleItem sample = helper.sampleDao().get(sampleIdToRun);
            if (sample != null) {
                sampleList.add(sample);
                logger.info("Running cached export file generation for "
                        + helper.formatForLogging(sample));
                DatasetItem dataset =
                    DaoFactory.DEFAULT.getDatasetDao().get((Integer)sample.getDataset().getId());
                datasetList.add(dataset);
            } else {
                logErrorRetrievingSample(sampleIdToRun);
                return;
            }
        }
        if (datasetOnlyFlag && forceFlag) {
            logger.info("Running in Force and Dataset Only mode.");
            DatasetItem dataset = DaoFactory.DEFAULT.getDatasetDao().get(datasetIdToRun);
            if (dataset != null
                    && (dataset.getDeletedFlag() == null
                        || dataset.getDeletedFlag().equals(false))) {
                logger.info("Forcing cached export file creation on "
                        + helper.formatForLogging(dataset) + ", all samples.");
                datasetList.add(dataset);
                sampleList = helper.sampleDao().find(dataset);
                List<SampleItem> sampleListStep = new ArrayList<SampleItem>();
                sampleListStep.addAll(sampleList);
                processSamples(dataset, sampleList, sampleListStep,
                               sampleList, false, true, true, true, true);
                return;
            } else {
                logErrorRetrievingDataset(datasetIdToRun);
            }
        }
        if (sampleOnlyFlag && forceFlag) {
            logger.info("Running in Force and Sample Only mode.");
            SampleItem sample = helper.sampleDao().get(sampleIdToRun);
            if (sample != null) {
                sampleList.add(sample);
                DatasetItem dataset =
                    DaoFactory.DEFAULT.getDatasetDao().get((Integer)sample.getDataset().getId());
                logger.info("Forcing cached export file creation on "
                        + helper.formatForLogging(dataset, sample)
                        + " ONLY.");
                List<SampleItem> sampleListStep = new ArrayList<SampleItem>();
                sampleListStep.add(sample);
                processSamples(dataset, sampleList, sampleListStep, sampleList,
                               false, true, true, true, true);
                return;
            } else {
                logErrorRetrievingSample(sampleIdToRun);
            }
        }
        if (forceFlag && !datasetOnlyFlag && !sampleOnlyFlag) {
            logger.info("Forcing cached export file creation on all datasets, all samples.");
            datasetList = DaoFactory.DEFAULT.getDatasetDao().findUndeletedDatasets();
            List<SampleItem> sampleListStep = new ArrayList<SampleItem>();
            for (DatasetItem dataset : datasetList) {
                sampleList = helper.sampleDao().find(dataset);
                sampleListStep.addAll(sampleList);
                processSamples(dataset, sampleList, sampleListStep, sampleList,
                               false, true, true, true, true);
            }
            return;
        }

        processDatasetsAndSamples(datasetList, sampleList);
        return;
    } // end cacheExportFiles()


    /**
     * This method loops through the given datasets and/or samples.  Used when running the CFG
     * in normal, datasetOnly or sampleOnly modes (force not included).  If running in datasetOnly
     * mode, the datasetList should not be empty when passed in.  Similarly, when running
     * in sampleOnly mode the sampleList should not be empty when passed in.
     * If either of these are empty, then the CFG will run in "normal" mode.
     * @param datasetList the list of datasets to process
     * @param sampleList the list of samples to process
     * @throws SQLException if something goes wrong, God forbid
     */
    private void processDatasetsAndSamples(List<DatasetItem> datasetList,
            List<SampleItem> sampleList) throws SQLException {
        boolean samplesQueuedToCacheTxFlag = false,
                samplesQueuedToCacheStepFlag = false,
                samplesQueuedToCacheProblemFlag = false,
                datasetQueuedToCacheKcmFlag = false;
        boolean datasetModifiedFlag = false;
        long transactionCount = 0;

        if (datasetList.isEmpty() && sampleList.isEmpty()) {
            datasetList = DaoFactory.DEFAULT.getDatasetDao().findUndeletedDatasets();
            logger.info("Running in everyday mode.");
        } else {
            logger.info("Not in everyday mode.");
        }

        for (DatasetItem dataset : datasetList) {
            List<SampleItem> samplesQueuedToCacheTx = null;
            List<SampleItem> samplesQueuedToCacheStep = null;
            List<SampleItem> samplesQueuedToCacheProblem = null;

            // Unless forceFlag is specified, skip junk datasets.
            if (skipJunkFlag && ((dataset.getJunkFlag() != null) && dataset.getJunkFlag())) {
                logger.debug("Skipping cached export creation for "
                             + helper.formatForLogging(dataset)
                             + " as it has a junkFlag of TRUE.");
                continue;
            }

            if (doTx) {
                samplesQueuedToCacheTx = helper.getSamplesQueuedToCacheTx(dataset);
                if (samplesQueuedToCacheTx.size() > 0) {
                    logDebug("Samples returned to transaction cache :: ",
                            samplesQueuedToCacheTx.size());
                    samplesQueuedToCacheTxFlag = true;
                    datasetModifiedFlag = true;
                }
            }
            if (doStep) {
                samplesQueuedToCacheStep = helper.getSamplesQueuedToCacheStep(dataset);
                if (samplesQueuedToCacheStep.size() > 0) {
                    logDebug("Samples returned to step cache :: ",
                            samplesQueuedToCacheStep.size());
                    samplesQueuedToCacheStepFlag = true;
                    datasetModifiedFlag = true;
                }
            }
            if (doProblem) {
                samplesQueuedToCacheProblem = helper.getSamplesQueuedToCacheProblem(dataset);
                if (samplesQueuedToCacheProblem.size() > 0) {
                    logDebug("Samples returned to problem cache :: ",
                            samplesQueuedToCacheProblem.size());
                    samplesQueuedToCacheProblemFlag = true;
                    datasetModifiedFlag = true;
                }
            }
            if (doKcm) {
                DatasetSystemLogDao dslDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
                if (dslDao.requiresCachedKcmStepExportGeneration(dataset)) {
                    logDebug("KCModel step export required.");
                    datasetQueuedToCacheKcmFlag = true;
                    datasetModifiedFlag = true;
                }
            }

            if (logger.isDebugEnabled()) {
                StringBuffer traceMsg = new StringBuffer();
                traceMsg.append(", doTx: "); traceMsg.append(doTx);
                traceMsg.append(", doStep: "); traceMsg.append(doStep);
                traceMsg.append(", doProblem: "); traceMsg.append(doProblem);
                traceMsg.append(", doKcm: "); traceMsg.append(doKcm);
                traceMsg.append(", datasetModifiedFlag: "); traceMsg.append(datasetModifiedFlag);
                traceMsg.append(", samplesQueuedToCacheTxFlag: ");
                traceMsg.append(samplesQueuedToCacheTxFlag);
                traceMsg.append(", samplesQueuedToCacheStepFlag: ");
                traceMsg.append(samplesQueuedToCacheStepFlag);
                traceMsg.append(", samplesQueuedToCacheProblemFlag: ");
                traceMsg.append(samplesQueuedToCacheProblemFlag);
                traceMsg.append(", datasetQueuedToCacheKcmFlag: ");
                traceMsg.append(datasetQueuedToCacheKcmFlag);
                logger.debug(traceMsg);
            }

            if (datasetModifiedFlag
                || samplesQueuedToCacheTxFlag
                || samplesQueuedToCacheStepFlag
                || samplesQueuedToCacheProblemFlag
                || datasetQueuedToCacheKcmFlag) {

                transactionCount = DaoFactory.DEFAULT.getTransactionDao().count(dataset);
                if (transactionCount > 0) {
                    logger.info("Creating cached exports for "
                            + helper.formatForLogging(dataset));

                    if (sampleOnlyFlag) {
                        if (doTx) {
                            samplesQueuedToCacheTx.clear();
                            samplesQueuedToCacheTx.add(sampleList.get(0));
                        }
                        if (doStep) {
                            samplesQueuedToCacheStep.clear();
                            samplesQueuedToCacheStep.add(sampleList.get(0));
                        }
                        if (doProblem) {
                            samplesQueuedToCacheProblem.clear();
                            samplesQueuedToCacheProblem.add(sampleList.get(0));
                        }
                        // KC model export uses dataset, not samples
                    }
                    processSamples(dataset,
                                   samplesQueuedToCacheTx,
                                   samplesQueuedToCacheStep,
                                   samplesQueuedToCacheProblem,
                                   datasetModifiedFlag,
                                   samplesQueuedToCacheTxFlag,
                                   samplesQueuedToCacheStepFlag,
                                   samplesQueuedToCacheProblemFlag,
                                   datasetQueuedToCacheKcmFlag);
                } else {
                    logger.debug("Skipping cached export creation for "
                            + helper.formatForLogging(dataset)
                            + " as it contains no transactions.");
                }
            } else {
                logger.debug("Skipping cached export creation for "
                        + helper.formatForLogging(dataset)
                        + " as it has not been modified recently.");
            }
        }
    }

    /**
     * Create or update the cached export files for a list of samples.
     * @param dataset the dataset currently being processed.
     * @param samplesToCacheTx a list of samples to TX cache.
     * @param samplesToCacheStep a list of samples to step cache.
     * @param samplesToCacheProblem  a list of samples to student-problem cache.
     * @param datasetModifiedFlag indicates if the dataset was modified (KC Model import
     * or data added to the dataset).
     * @param samplesQueuedToCacheTxFlag indicates whether any samples require a TX cache
     * @param samplesQueuedToCacheStepFlag indicates whether any samples require a step cache
     * @param samplesQueuedToCacheProblemFlag indicates whether any samples require a problem cache
     * @param datasetQueuedToCacheKcmFlag indicates whether the dataset requires a KC model cache
     * @throws SQLException if something goes wrong, God forbid
     */
    private void processSamples(DatasetItem dataset,
                                List<SampleItem> samplesToCacheTx,
                                List<SampleItem> samplesToCacheStep,
                                List<SampleItem> samplesToCacheProblem,
                                boolean datasetModifiedFlag,
                                boolean samplesQueuedToCacheTxFlag,
                                boolean samplesQueuedToCacheStepFlag,
                                boolean samplesQueuedToCacheProblemFlag,
                                boolean datasetQueuedToCacheKcmFlag)
        throws SQLException
    {
        if (doTx && samplesQueuedToCacheTxFlag && samplesToCacheTx != null) {
            for (SampleItem sample : samplesToCacheTx) {
                TxExportTask exportTask = null;
                try {
                    TxExportInfo info = helper.initTxExportInfo(dataset, sample);

                    info.setBaseDir(baseDir);
                    info.setStudentBatchSize(studentBatchSize);
                    info.setTxExportSPFilePath(txExportSPFilePath);

                    exportTask = helper.txExportTask(info);

                    helper.logInfo(info, "Starting transactions.");
                    if (exportTask.isExportStarted() || !exportTask.loadSPs()) { continue; }

                    String existingCachedFileName = exportTask.getCachedFileName();
                    logDebug("Existing cached file name :: ", existingCachedFileName);

                    if (forceFlag || samplesQueuedToCacheTxFlag) {
                        try {
                            exportTask.writeHeaders();
                        } catch (IOException ioe) {
                            logger.error("Unable to write column headings to temp file. "
                                    + ioe);
                        }
                    }
                    if (exportTask.cacheSample() != null) {
                        exportTask.logExportCompleted();
                        if (existingCachedFileName != null) {
                            // delete the old cached version
                            helper.deleteFile(existingCachedFileName);
                        }
                    }
                } catch (Throwable throwable) {
                    sendErrorEmail(logger, "Error occurred processing transaction sample "
                            + sample.getNameAndId(sample.getSampleName()), throwable);
                } finally {
                    if (exportTask != null) { exportTask.cleanup(); }
                }
            }
        }

        if (doStep && samplesQueuedToCacheStepFlag && samplesToCacheStep != null) {
            for (SampleItem sample : samplesToCacheStep) {
                StepRollupExportTask stepTask =  null;
                try {
                    CachedFileInfo info = new CachedFileInfo(dataset, sample, baseDir);
                    String existingCachedFileName = null;

                    helper.logInfo(info, "Starting student step.");
                    StepRollupExportOptions options =
                            new StepRollupExportOptions(sample);
                        options.setDisplayAllModels(true);
                        options.setExportCachedVersion(true);
                    stepTask = new StepRollupExportTask(info, options);
                    stepTask.setStudentBatchSize(studentBatchSize);
                    if (stepTask.getNumSteps() == 0) {
                        logInfo("Skipping step export for sample ", sample.getSampleName(),
                                ", no steps found.");
                    } else {
                        if (stepTask.isExportStarted()) { continue; }
                        existingCachedFileName = stepTask.getCachedFileName();

                        stepTask.writeStepRollupExport(null, options, null, null);
                        stepTask.createCachedFile();
                        stepTask.logExportCompleted(true);
                        if (existingCachedFileName != null) {
                            // delete the old cached version
                            helper.deleteFile(existingCachedFileName);
                        }
                    }
                } catch (Throwable throwable) {
                    sendErrorEmail(logger, "Error occurred processing step sample "
                            + sample.getNameAndId(sample.getSampleName()), throwable);
                } finally {
                    if (stepTask != null) { stepTask.cleanup(); }
                }
            }
        }
        if (doProblem && samplesQueuedToCacheProblemFlag && samplesToCacheProblem != null) {
            StudentProblemRollupDao sprDao = DaoFactory.DEFAULT.getStudentProblemRollupDao();

            for (SampleItem sample : samplesToCacheProblem) {
                StudentProblemExportTask problemTask =  null;
                try {
                    CachedFileInfo info =
                        new CachedFileInfo(dataset, sample, baseDir);
                    String existingCachedFileName = null;

                    helper.logInfo(info, "Starting student problem.");
                    StudentProblemRollupOptions options =
                            new StudentProblemRollupOptions(sample);
                        options.setDisplayAllModels(true);
                        options.setExportCachedVersion(true);
                    problemTask = new StudentProblemExportTask(info, options);
                    // writeProblemRollupExport calls getStudentProblemRollupRows
                    // which uses its own student batch size for an inner query
                    if (isBatchSpecified) {
                        problemTask.setStudentBatchSize(studentBatchSize);
                    } else {
                        problemTask.setStudentBatchSize(
                            StudentProblemExportTask.STUDENTPROBLEM_EXPORT_BATCH_SIZE);
                    }

                    if (sprDao.numberOfStudentProblems(sample, null) == 0) {
                        logInfo("Skipping problem export for sample ", sample.getSampleName(),
                                ", no problems found.");
                    } else {
                        if (problemTask.isExportStarted()) { continue; }
                        long startTime = System.currentTimeMillis();
                        existingCachedFileName = problemTask.getCachedFileName();
                        problemTask.writeProblemRollupExport(null, options, null, null);
                        problemTask.createCachedFile();
                        problemTask.logExportCompleted(true);

                        if (existingCachedFileName != null) {
                            // delete the old cached version
                            helper.deleteFile(existingCachedFileName);
                        }
                    }
                } catch (Throwable throwable) {
                    problemTask.logExportCompleted(false);
                    sendErrorEmail(logger, "Error occurred processing student-problem export for"
                            + " sample: " + sample.getNameAndId(sample.getSampleName()), throwable);
                } finally {
                    if (problemTask != null) { problemTask.cleanup(); }
                }
            }
        }
        if (doKcm && datasetQueuedToCacheKcmFlag && dataset != null) {

            KCModelStepExportDao kcmeDao = DaoFactory.DEFAULT.getKCModelStepExportDao();

            KCModelStepExportTask kcmTask = null;
            try {
                kcmTask = new KCModelStepExportTask(dataset);
                kcmTask.logInfo("Starting KC model export");

                if (kcmTask.isExportStarted()) { return; }

                // delete the old cached version
                kcmTask.deleteKCModelStepExportRows(dataset);

                kcmTask.writeKCModelStepExport();

                kcmTask.logExportCompleted(true);
            } catch (Throwable throwable) {
                kcmTask.logExportCompleted(false);
                sendErrorEmail(logger, "Error occurred processing KC model export for"
                               + " dataset: " + dataset.getDatasetName(), throwable);
            } finally {
                if (kcmTask != null) { kcmTask.cleanup(); }
            }
        }
    }

    /**
     * Write an error message to the log when unable to retrieve a DatasetItem
     * for the given datasetId.
     * @param datasetId the troublesome datasetId.
     */
    private void logErrorRetrievingDataset(Integer datasetId) {
        logger.error("Cannot retrieve a DatasetItem for datasetId :: "
                + datasetId);
    }

    /**
     * Write an error message to the log when unable to retrieve a SampleItem
     * for the given sampleId.
     * @param sampleId the troublesome sampleId.
     */
    private void logErrorRetrievingSample(Integer sampleId) {
        logger.error("Cannot retrieve a SampleItem for sampleId :: "
                + sampleId);
    }

    /**
     * Check to see if the log conversion has finished.
     * @return true if it has, false otherwise.
     */
    private boolean isLogConversionDone() {
        DatasetSystemLogDao dao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
        return dao.isLogConversionDone();
    }

    /**
     * Parse the command line arguments to get the base directory and any other
     * parameters we may need.
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

            // loop through the arguments
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-h") || args[i].equals("-help")) {
                    displayUsage();
                    exitLevel = 0;
                } else if (args[i].equals("-dataset")) {
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
                } else if (args[i].equals("-directory")) {
                    if (++i < args.length) {
                        this.baseDir = args[i];
                    } else {
                        logger.error("A file name must be specified with the -d argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-f") || args[i].equals("-force")) {
                    this.forceFlag = true;
                } else if (args[i].equals("-e") || args[i].equals("-email")) {
                    setSendEmailFlag(true);
                    if (++i < args.length) {
                        setEmailAddress(args[i]);
                    } else {
                        System.err.println(
                            "Error: a email address must be specified with this argument");
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
                } else if (args[i].equals("-sp") || args[i].equals("-storedProc")) {
                    logger.info("Obsolete argument: " + args[i]);
                } else if (args[i].equals("-step")) {
                    doStep = true;
                } else if (args[i].equals("-tx") || args[i].equals("-transactions")) {
                    doTx = true;
                } else if (args[i].equals("-problem") || args[i].equals("-prob")) {
                    doProblem = true;
                } else if (args[i].equals("-kcm")) {
                    doKcm = true;
                } else if (args[i].equals("-sb") || args[i].equals("-studentBatchSize")) {
                    if (++i < args.length) {
                        try {
                            studentBatchSize = Integer.parseInt(args[i]);
                            isBatchSpecified = true;
                            if (studentBatchSize < 0) {
                                throw new Exception();
                            }
                        } catch (Exception exception) {
                            logger.error("Error while trying to parse student batch size. "
                                    + "Please check the parameter for accuracy.");
                            exitLevel = 1;
                        }
                    } else {
                        logger.error("The student batch size must be specified with the "
                                + "-studentBatchSize argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-spPath") || args[i].equals("-spFilePath")) {
                    if (++i < args.length) {
                        txExportSPFilePath = args[i];
                    } else {
                        logger.error("The transaction export stored procedure file location "
                            + "must be specified with the -spFilePath argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else if (args[i].equals("-waitForLogConversionDone")) {
                    waitForLogConversionDone = true;
                    if (++i < args.length) {
                        try {
                            Integer minutes = Integer.parseInt(args[i]);
                            this.minutesToSleep = minutes * MILLISECOND_FACTOR;
                            if (minutesToSleep < 0) {
                                throw new Exception();
                            }
                        } catch (Exception exception) {
                            logger.info("Minutes to sleep unparseable.  Using default of "
                                    + minutesToSleep / MILLISECOND_FACTOR + ".");
                        }
                    }
                } else if (args[i].equals("-skipJunk")) {
                    this.skipJunkFlag = true;
                }
                // If the exitLevel was set, then break out of the loop
                if (exitLevel != null) {
                    break;
                }
            } // end for loop
            if (!doStep && !doTx && !doProblem && !doKcm) {
                doStep = true;
                doTx = true;
                doProblem = true;
                doKcm = true;
            }
            // If -force arg specified, ignore -skipJunk arg.
            if (forceFlag) { skipJunkFlag = false; }
        }
        return exitLevel;
    } // end handleOptions

    /**
     * Displays the command line arguments for this program.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... "
            + "CachedExportFileGenerator"
            + " [-waitForLogConversionDone minutes_to_wait] [-directory base_dir_path]"
            + " [-dataset dataset_id] [-sample sample_id] [-storedProc] [-spFilePath sp_file_path]"
            + " [-tx] [-step] [-problem] [-kcm] [-force] [-email]");
        System.err.println("Option descriptions:");
        System.err.println("\t-waitForLogConversionDone\t minutes to wait for log conversion to"
                + " finish prior to the start of caching");
        System.err.println("\t-directory\t\t\t base directory where cached files will be stored");
        System.err.println("\t-dataset\t\t\t dataset id to run the generator on");
        System.err.println("\t-sample\t\t\t\t sample id to run the generator on");
        System.err.println("\t-storedProc\t\t\t use the tx_export stored procedure");
        System.err.println("\t-spFilePath\t\t\t location of the tx_export stored procedure file");
        System.err.println("\t-tx, -transactions\t\t create a transaction record cache.");
        System.err.println("\t-step\t\t\t\t create a student-step cache.");
        System.err.println("\t-problem\t\t\t\t create a student-problem cache.");
        System.err.println("\t-kcm\t\t\t\t populate KC model export table.");
        System.err.println("\t-f, -force\t\t\t force a cached file generation");
        System.err.println("\t-e, -email\t\t\t send email if major failure");
        System.err.println("\t-skipJunk\t do not run LFA backfill");
        System.err.println("\t-h\t\t\t\t usage info");
    }

    /**
     * main.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("CachedExportFileGenerator.main");
        String version = VersionInformation.getReleaseString();
        logger.info("CachedExportFileGenerator starting (" + version + ")...");
        CachedExportFileGenerator generator = ExtractorFactory.DEFAULT.getCFG();

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        boolean playMode = ImportQueue.isInPlayMode();
        // If an exitLevel exists, it will be used to exit
        // before the cfg is run; otherwise exitLevel is null so continue.
        Integer exitLevel = null;
        // Handle the command line options:
        // The handleOptions method is called before entering the try-catch block
        // because it isn't affected by the ImportQueue.
        exitLevel = generator.handleOptions(args);
        try {
            // If exitLevel is null, then proceed with caching
            if (exitLevel == null) {
                // Pause the IQ
                if (playMode) {
                    logger.info("main:: Pausing the ImportQueue.");
                    ImportQueue.pause();
                }
                // Process options
                if (generator.isSendEmail()) {
                    LogUtils.logDebug(logger, "Will send email to ", generator.getEmailAddress(),
                            " if an unknown error occurs.");
                }
                if (generator.waitForLogConversionDone) {
                    logger.info("Checking for state of log conversion process.");
                    boolean isLogConversionDone = generator.isLogConversionDone();
                    if (isLogConversionDone) {
                        logger.info("Log conversion is finished.  Proceeding with caching.");
                    } else {
                        while (!isLogConversionDone) {
                            logger.info("Log conversion not quite finished.  Sleeping for "
                                    + generator.minutesToSleep / MILLISECOND_FACTOR + " minutes.");
                            Thread.sleep(generator.minutesToSleep);
                            isLogConversionDone = generator.isLogConversionDone();
                        }
                    }
                }
                generator.cacheExportFiles();
            }
        } catch (Throwable throwable) {
            // Log an error and send email if this happens!
            generator.sendErrorEmail(logger, "Unknown error in main method.", throwable);
            exitLevel = 1;
        } finally {
            if (playMode) {
                logger.info("main:: Unpausing the ImportQueue.");
                ImportQueue.play();
            }
            exitOnStatus(exitLevel);
            logger.info("CachedExportFileGenerator done!");
        }
    }
} // end CachedExportFileGenerator.java
