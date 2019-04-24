/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.StudentProblemRollupOptions;
import edu.cmu.pslc.datashop.extractors.ErrorEmailSender;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.extractors.StudentProblemExportTask;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.MailUtils;
import static java.lang.System.currentTimeMillis;

/**
 * Export bean class for exporting student-problem tables.
 *
 * @author Jim Rankin
 * @version $Revision: 9855 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-08-27 11:53:04 -0400 (Tue, 27 Aug 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemExportBean extends AbstractExportBean
implements Serializable, ExportThread, ErrorEmailSender {

    /** The logger. */
    private Logger logger = Logger.getLogger(StudentProblemExportBean.class.getName());

    /** User id. */
    private String userId;

    /** The option for exporting. */
    private StudentProblemRollupOptions options;
    /** Hibernate Session wrapped helper. */
    private StudentProblemExportHelper helper;
    /** Sample dao for getting items by sample */
    private DatasetSystemLogDao datasetSystemLogDao;
    /** StudentProblemRollupDao. */
    private StudentProblemRollupDao studentProblemRollupDao;
    /** SampleDao. */
    private SampleDao sampleDao;
    /**
     * Getter for the StudentProblemRollupDao.
     * @return the StudentProblemRollupDao
     */
    public StudentProblemRollupDao getStudentProblemRollupDao() {
        return studentProblemRollupDao;
    }
    /**
     * Setter for the StudentProblemRollupDao.
     * @param studentProblemRollupDao the StudentProblemRollupDao
     */
    public void setStudentProblemRollupDao(
            StudentProblemRollupDao studentProblemRollupDao) {
        this.studentProblemRollupDao = studentProblemRollupDao;
    }
    /**
     * Getter for the SampleDao.
     * @return the SampleDao
     */
    public SampleDao getSampleDao() {
        return sampleDao;
    }
    /**
     * Setter for the SampleDao.
     * @param sampleDao the SampleDao
     */
    public void setSampleDao(SampleDao sampleDao) {
        this.sampleDao = sampleDao;
    }

    /** An interface for each step of caching a sample export file. */
    private StudentProblemExportTask exportTask = null;

    /** Dataset to export. */
    private DatasetItem dataset;
    /** Samples for this export. */
    private List <SampleItem> sampleList;

    /** Base directory in the file system for storing cached transaction export files. */
    private String baseDir;

    /** The export start time. */
    private long startTime;
    /** flag indicating that an export for this sample was started in another thread and
     * we are waiting for it to finish. */
   private boolean isWaiting = false;

   /** Default send email flag value is false. */
   private boolean sendEmail = false;
   /** Send to default email address. */
   private String emailAddress = null;
   /** Time to wait between tries when attempting to delete the temporary files. */
   private static final Integer TIME_BETWEEN_DELETE_TEMPFILE_TRIES = 250;

    /** Returns studentProblemExportHelper. @return Returns the studentProblemExportHelper. */
    public StudentProblemExportHelper getStudentProblemExportHelper() {
        return helper;
    }

    /**
     * Set studentProblemExportHelper.
     * @param studentProblemExportHelper The studentProblemExportHelper to set.
     */
    public void setStudentProblemExportHelper(
            StudentProblemExportHelper studentProblemExportHelper) {
        this.helper = studentProblemExportHelper;
    }

    /**
     * Sets all the attributes needed to create the export file.
     * @param dataset DatasetItem to export.
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Sets all the attributes needed to create the export file.
     * @param options the StudentProblemRollupOptions
     * @param userId the requesting user Id
     */
    public void setAttributes(StudentProblemRollupOptions options, String userId) {
        this.options = options;
        this.userId = userId;
    }

    /**
     * Starts this bean running.
     */
    public void run() {
        List<String> tempFilesToDelete = new ArrayList<String>();
        try {
            boolean gettingCached = true;
            setInitializing(true); //used to prevent divide by zeros.
            helper = HelperFactory.DEFAULT.getStudentProblemExportHelper();
            init();
            logDebug("Performing Student-Problem Export... ", getTemporaryFileName());
            startTime = currentTimeMillis();
            setCachedFileAvailable(true);
            setRunning(true);
            sampleList = options.getSamples();
            datasetSystemLogDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
            if (sampleList == null || sampleList.size() < 1) {
                setRunning(false);
                return;
            }
            // Create a map by Sample of StudentProblemExportInfo items.
            Map<SampleItem, CachedFileInfo> sampleExportInfoMap =
                    new HashMap<SampleItem, CachedFileInfo>();
            // Keep track of the number of student problem rows for each sample.
            Map<Comparable, Long> sampleSizeMap = new HashMap<Comparable, Long>();
            Map<SampleItem, String> cachedSampleExports = new HashMap<SampleItem, String>();
            //The next line used to call this: helper.getSamplesToCacheAggSampleOnly(dataset);
            List<SampleItem> samplesQueuedToCache = helper.getSamplesQueuedToCacheProblem(dataset);
            List<String> cachedFilesToDelete = new ArrayList<String>();
            for (SampleItem sample : sampleList) {
                if (isCancelFlag()) { break; }
                sampleExportInfoMap.put(sample, studentProblemExportInfo(sample));
                String cachedFileName = helper.getCachedFileName(sampleExportInfoMap.get(sample));
                if (!options.getExportCachedVersion()
                        && (options.hasUserOptions()
                                || options.isDisplaySkills())
                            ) {
                    Long numStudentProblems = studentProblemRollupDao
                            .numberOfStudentProblems(sample, options);
                    sampleSizeMap.put(sample.getId(), numStudentProblems);
                    setCachedFileAvailable(false);
                    incrementNumTotalRows(numStudentProblems.intValue());
                    logDebug(sample, "User-specific export requested.");
                    gettingCached = false;
                } else if (!samplesQueuedToCache.contains(sample) && cachedFileName != null) {
                    // Sample does not require caching and a cached file exists, so return it.
                    cachedSampleExports.put(sample, cachedFileName);
                    logDebug(sample, "Found a cached export for the sample.");
                    setCachedFileAvailable(true);
                    incrementNumTotalRows(1);
                } else {
                    // Samples needs cached/re-cached or a cache already exists.
                    // Re-cache the sample then delete the existing file (if applicable).
                    if (samplesQueuedToCache.contains(sample) && cachedFileName != null) {
                        cachedFilesToDelete.add(cachedFileName);
                        logDebug(sample,
                            "Found a cached export but the sample needs to be recached.");
                    }
                    Long numStudentProblems = studentProblemRollupDao
                        .numberOfStudentProblems(sample, options);
                    sampleSizeMap.put(sample.getId(), numStudentProblems);
                    setCachedFileAvailable(false);
                    incrementNumTotalRows(1);
                    logDebug(sample, "Cached export not available");
                }
            }
            // The sample info keyset
            Set<SampleItem> keySet = null;
            setInitializing(false); //used to prevent divide by zeros.
            // if everything was cached then there is nothing left to process.
            if (sampleSizeMap.size() != 0) {
                StudentProblemRollupOptions sampleOptions = null;
                // Get cached version of the export, if available
                if (options.getExportCachedVersion()) {
                    // Apply only some of the options. Disregard students, problems, etc.
                    sampleOptions = new StudentProblemRollupOptions();
                    sampleOptions.setSamples(sampleList);
                    sampleOptions.setExportCachedVersion(true);
                    sampleOptions.setDisplayAllModels(true);
                    sampleOptions.setDisplaySkills(false);
                    sampleOptions.setIncludeUnmappedSteps(false);

                } else {
                    // Apply user-selected navHelper options
                    sampleOptions = options;
                }
                for (SampleItem sample : sampleList) {
                    sample = sampleDao.get((Integer) sample.getId());
                    if (sample == null) {
                        logger.warn("SampleItem came back as null");
                    // The user doesn't want the cached version so export the sample
                    } else if (!options.getExportCachedVersion()) {
                        exportTask = new StudentProblemExportTask(
                            sampleExportInfoMap.get(sample), sampleOptions);
                        exportSample(exportTask,
                            sample, sampleExportInfoMap.get(sample), sampleOptions,
                            sampleOptions.getStudents(), sampleOptions.getProblems());
                        cachedSampleExports.put(sample,
                                sampleExportInfoMap.get(sample)
                                    .getTempFile().getAbsolutePath());
                        // Mark these temporary files to be deleted
                        tempFilesToDelete.add(sampleExportInfoMap.get(sample)
                                    .getTempFile().getAbsolutePath());
                    // The user requests the cached version
                    } else if (!cachedSampleExports.containsKey(sample)) {
                        exportTask = new StudentProblemExportTask(
                            sampleExportInfoMap.get(sample), sampleOptions);
                        isWaiting = exportTask.isExportStarted();
                        if (isWaiting) {
                            logDebug(sample, "Waiting on the export to finish...");
                            while (!isCancelFlag()
                                    && !exportTask.isExportCompleted()) {
                                Thread.sleep(MILLISECONDS_PER_SECOND);
                            }
                            isWaiting = false;
                            if (!isCancelFlag()) {
                                if (exportTask.getCachedFileName() == null) {
                                    logInfo(sample, "Finished waiting for sample ",
                                        sample.getSampleName(),
                                            " but no cached file found, so recaching export.");
                                    exportSample(exportTask,
                                        sample, sampleExportInfoMap.get(sample), sampleOptions,
                                        null, null);
                                } else {
                                    logInfo(sample, "Finished waiting for sample ",
                                            sample.getSampleName());
                                }
                            }
                        } else {
                            // Otherwise, export the sample
                            exportSample(exportTask,
                                sample, sampleExportInfoMap.get(sample), sampleOptions, null, null);

                        }
                        if (!isCancelFlag()) {
                            cachedSampleExports.put(sample,
                                    exportTask.getCachedFileName());
                        }
                    } else {
                        logInfo(sample, "Cached file used for sample.");

                    }
                    datasetSystemLogDao.removeMessage(sample.getDataset(), sample,
                            SystemLogger.ACTION_CACHED_PROBLEM_EXPORT_START);
                }
                // User Logger
                UserItem userItem = DaoFactory.DEFAULT.getUserDao().get(userId);
                SkillModelItem skillModelItem = sampleOptions.getModel();
                studentProblemRollupDao.logStudentProblemExport(
                    dataset, userItem, sampleOptions,
                        skillModelItem, (int) getNumTotalRows(),
                        gettingCached);
            } else {
                // User Logger
                UserItem userItem = DaoFactory.DEFAULT.getUserDao().get(userId);
                studentProblemRollupDao.logStudentProblemExport(
                    dataset, userItem, options,
                        null, (int) getNumTotalRows(),
                        gettingCached);
            }

            // grab the cached export files and add to a zip file
            keySet = cachedSampleExports.keySet();
            for (SampleItem sample : keySet) {
                copyCachedFile(dataset, sample, cachedSampleExports.get(sample),
                    keySet.size(), PROBLEM_EXPORT);
            }
            // delete any out-of-date cached files
            for (String cachedFileName : cachedFilesToDelete) {
                helper.deleteFile(cachedFileName);
            }
            if (isCancelFlag()) {
                deleteTempFile();
                logDebug("Canceling export file build");
                setRunning(false);
                setInitializing(false);
            } else {
                int numTotalRows = getNumTotalRows();
                logDebug("Done building export file for ", numTotalRows,
                        " rows.");
                setNumCompletedRows(numTotalRows);
                // moving this close is a bad idea; make sure the zip file is
                // closed before sending the file to the browser.
                closeZipStream();
                // keep the thread alive while a call come back for the item.
                logDebug("keeping the thread alive.");
                long timeToDie = currentTimeMillis() + KEEP_ALIVE_SECONDS
                        * MILLISECONDS_PER_SECOND;
                while (!isExported() && currentTimeMillis() < timeToDie) {
                    Thread.sleep(MILLISECONDS_PER_SECOND);
                }
                logDebug("finished keeping the thread alive.");
            }
            System.gc();
            // Delete the temp files used to create the zip
            for (String fileName : tempFilesToDelete) {
                 File delThisFile = new File(fileName);
                 while (!delThisFile.delete()) {
                     synchronized (this) {
                         try {
                             this.wait(TIME_BETWEEN_DELETE_TEMPFILE_TRIES);
                         } catch (InterruptedException e) {
                             logger.error(
                                 "StudentProblemExportBean:: Error waiting on file deletion.");
                         }
                     }
                 }
            }
         } catch (Exception exception) {
             logger.error("Caught Exception: ", exception);
             setNumTotalRows(-1);
             setHasError(true);
             setRunning(false);
             setInitializing(false);
             deleteTempFile();
         } finally {
             if (exportTask != null) {
                 exportTask.cleanup();
             }
             setRunning(false);
             String benchmarkPrefix = AbstractServlet.getBenchmarkPrefix(
                     getClass().getSimpleName(), userId);
             logger.info(benchmarkPrefix + " Thread stop.");
         }
     }

    /**
     * Generate TxExportInfo instance for the sample, using values from this bean.
     * @param sample the sample
     * @return a TxExportInfo instance for the sample, using values from this bean.
     * @throws IOException thrown by TxExportInfo constructor
     */
    private synchronized CachedFileInfo studentProblemExportInfo(
            SampleItem sample) throws IOException {
        CachedFileInfo info = helper.initStudentProblemExportInfo(dataset, sample);
        info.setBaseDir(baseDir);
        info.setStudentBatchSize(StudentProblemExportTask.STUDENTPROBLEM_EXPORT_BATCH_SIZE);
        return info;
    }




    /** 1000 milliseconds in a second. */
    private static final int MILLISECONDS_PER_SECOND = 1000;
    /** 2000 milliseconds keep-alive period. */
    private static final int KEEP_ALIVE_SECONDS = 2000;
    /** Estimated number of student-problem rows written to file per second. */
    private static final double ESTIMATION_FACTOR = 111.0;
    /**
     * Estimate percentage done using the total number of transactions and an estimation factor.
     * This is because we do not have progress updates from the transaction export
     * stored procedures.
     * @return estimated percentage of cached file export completed
     */
    @Override
    public synchronized int getPercent() {
        // no need to estimate if export is finished
        if (getNumCompletedRows() < getNumTotalRows()) {
            long elapsedSeconds =
                (currentTimeMillis() - startTime) / MILLISECONDS_PER_SECOND;
            // estimate number of rows completed with elapsed seconds * estimation factor
            setNumCompletedRows((int)(ESTIMATION_FACTOR * elapsedSeconds));
            // keep under total rows, if taking longer than estimated
            if (getNumCompletedRows() >= getNumTotalRows()) {
                setNumCompletedRows(getNumTotalRows() - 1);
            }
        }
        // super method will use our estimated completed rows to calculate percentage done.
        return super.getPercent();
    }



    /**
     * Create the cached export file for sample.
     * @param exportTask the StudentProblemExportTask
     * @param sample the sample
     * @param info the StudentProblemExportInfo
     * @param sampleOptions the problem rollup options for a sample
     * @param studentIds a list of student ids or null if all are desired
     * @param problemItems a list of problem items or null if all are desired
     * @throws SQLException if something goes wrong with the transaction export stored procedures.
     * @throws IOException if something goes wrong with writing the transaction export files
     */
    private void exportSample(StudentProblemExportTask exportTask,
            final SampleItem sample, CachedFileInfo info,
            StudentProblemRollupOptions sampleOptions, List<StudentItem> studentIds,
            List<ProblemItem> problemItems)
            throws SQLException, IOException {
            try {
                String temporaryFileName = null;

                logInfo(sample, "Preparing to export student problem tables for sample '"
                        + sample.getSampleName() + "' (" + sample.getId() + ").");
                temporaryFileName = exportTask.getCachedFileName();
                SkillModelItem skillModelItem = null;
                if (!sampleOptions.getExportCachedVersion()
                        && sampleOptions.isDisplaySkills()) {
                    skillModelItem = sampleOptions.getModel();
                }

                UserItem userItem = DaoFactory.DEFAULT.getUserDao().get(userId);
                exportTask.writeProblemRollupExport(skillModelItem, sampleOptions,
                    studentIds, problemItems);

                if (exportTask.getNumProblems() > 0
                        && sampleOptions.getExportCachedVersion()) {
                    exportTask.createCachedFile();
                    exportTask.logExportCompleted(true);
                   if (temporaryFileName != null) {
                        // delete the old cached version
                        helper.deleteFile(temporaryFileName);
                    }
                }

            } catch (Throwable throwable) {
                exportTask.logExportCompleted(false);
                if (sendEmail) {
                    String msg = "Error caching sample " + sample.getNameAndId();
                    logger.error(msg, throwable);
                    String source = StudentProblemExportBean.this.getClass().getSimpleName();
                    MailUtils.sendErrorEmail(source, emailAddress, emailAddress, msg, throwable);
                }
                logger.error("Error occurred processing sample for student problem export: "
                        + sample.getNameAndId(sample.getSampleName()));
            }
    }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param content sample and/or dataset label
     * @return a string useful for logging
     */
    private String getLogPrefix(String content) {
        return "StudentProblemExportBean [" + content + "]: ";
    }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param sample the sample
     * @return a string useful for logging
     */
    private String getLogPrefix(SampleItem sample) {
        return getLogPrefix(helper.formatForLogging(dataset) + "/"
            + helper.formatForLogging(sample));
    }

    /**
     * Only log if debugging is enabled.
     * @param sample the sample
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(SampleItem sample, Object... args) {
        if (logger.isDebugEnabled()) {
            LogUtils.logDebug(logger, getLogPrefix(sample), concatenate(args));
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param sample the sample
     * @param args concatenate all arguments into the string to be logged
     */
    private void logInfo(SampleItem sample, Object... args) {
        if (logger.isInfoEnabled()) {
            LogUtils.logInfo(logger, getLogPrefix(sample), concatenate(args));
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        if (logger.isInfoEnabled()) {
            LogUtils.logDebug(logger, getLogPrefix(helper.formatForLogging(dataset)),
                    concatenate(args));
        }
    }

    /** Get the user id.
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /** Get the base file directory for cached files.
     * @return the base file directory for cached files
     */
    public String getBaseDir() {
        return baseDir;
    }

    /** Set the user id.
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** Set the base file directory for cached files.
     * @param baseDir the base file directory for cached files
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Returns the sendEmailFlag.
     * @return the sendEmailFlag
     */
    public boolean isSendEmail() { return sendEmail; }

    /**
     * Sets the sendEmailFlag.
     * @param sendEmail The sendEmailFlag to set.
     */
    public void setSendEmail(boolean sendEmail) { this.sendEmail = sendEmail; }

    /**
     * Returns the emailAddress.
     * @return the emailAddress
     */
    public String getEmailAddress() { return emailAddress; }

    /**
     * Sets the emailAddress.
     * @param emailAddress The emailAddress to set.
     */
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
}
