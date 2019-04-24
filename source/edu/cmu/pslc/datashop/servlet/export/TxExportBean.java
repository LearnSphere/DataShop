/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dto.ExportCache;
import edu.cmu.pslc.datashop.dto.TxExportInfo;
import edu.cmu.pslc.datashop.extractors.ErrorEmailSender;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper.TxExportTask;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.MailUtils;

import static edu.cmu.pslc.datashop.extractors.CachedExportFileGenerator.DEFAULT_STUDENT_BATCH_SIZE;
import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;
import static java.lang.System.currentTimeMillis;

/**
 *  Export Bean that implements runnable.  This bean will be spawned in
 *  a separate thread to build the export file.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11729 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-22 21:34:28 -0500 (Sat, 22 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TxExportBean extends AbstractExportBean implements Serializable, ExportThread,
ErrorEmailSender {
    /** 1000 milliseconds in a second. */
    private static final int MILLISECONDS_PER_SECOND = 1000;
    /** magic 1000! */
    private static final int KEEP_ALIVE_SECONDS = 2000;

    /** Debug logging. */
    private Logger logger = Logger.getLogger(TxExportBean.class.getName());
    /** Estimated number of transactions processed per second. */
    private static final double ESTIMATION_FACTOR = 850.11;
    /** Samples to export. */
    private List <SampleItem> sampleList;
    /** DatasetItem for this export. */
    private DatasetItem dataset;

    /** Base directory in the file system for storing cached transaction export files. */
    private String baseDir;

    /** User id. */
    private String userId;

    /** Hibernate Session wrapped helper. */
    private TransactionExportHelper helper;
    /** Sample dao for getting items by sample. */
    private SampleDao sampleDao;
    /** Sample Metric dao for getting metrics about a sample. */
    private SampleMetricDao sampleMetricDao;
    /** flag indicating that an export for this sample was started in another thread and
      * we are waiting for it to finish. */
    private boolean isWaiting = false;
    /** when the export started. */
    private long startTime;
    /** where to find the transaction export stored procedure template. */
    private String txExportSPFilePath;
    /** an interface for each step of caching a sample export file. */
    private TxExportTask exportTask = null;
    /** Default send email flag value is false. */
    private boolean sendEmail = false;
    /** Send to default email address. */
    private String emailAddress = null;
    /** Default downloadExport flag value is true. */
    private boolean downloadExport = true;
    /** Used to wrap export in a callback.*/
    private TransactionTemplate transactionTemplate;

    /**
     * Used to wrap export in a callback.
     * @param transactionTemplate used to wrap export in a callback
     */
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Flag indicating that an export for this sample was started in another thread and
     * we are waiting for it to finish.
     * @return whether we are waiting for the export to finish
     */
    public boolean isWaiting() { return isWaiting; }

    /**
     * where to find the transaction export stored procedure template.
     * @return where to find the transaction export stored procedure template
     */
    public String getTxExportSPFilePath() { return txExportSPFilePath; }

    /**
     * where to find the transaction export stored procedure template.
     * @param txExportSPFilePath where to find the transaction export stored procedure template
     */
    public void setTxExportSPFilePath(String txExportSPFilePath) {
        this.txExportSPFilePath = txExportSPFilePath;
    }

    /**
     * Sets the TransactionExportHelper. @param helper the TransactionExportHelper to be set to. */
    public void setTransactionExportHelper(TransactionExportHelper helper) {
        this.helper = helper;
    }

    /** Gets the TransactionExportHelper. @return TransactionExportHelper */
    public TransactionExportHelper getExportHelper() { return this.helper; }

    /** Returns sampleDao. @return Returns the sampleDao. */
    public SampleDao getSampleDao() { return sampleDao; }

    /** Set sampleDao. @param sampleDao The sampleDao to set. */
    public void setSampleDao(SampleDao sampleDao) { this.sampleDao = sampleDao; }

    /** Returns sampleMetricDao. @return Returns the sampleMetricDao. */
    public SampleMetricDao getSampleMetricDao() { return sampleMetricDao; }

    /** Set sampleMetricDao. @param sampleMetricDao The sampleMetricDao to set. */
    public void setSampleMetricDao(SampleMetricDao sampleMetricDao) {
        this.sampleMetricDao = sampleMetricDao;
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

    /** Default constructor. */
    public TxExportBean() { }

    /**
     * Sets all the attributes need to create the export file.
     * @param sampleList List of samples to export.
     * @param datasetItem the dataset being exported
     * @param baseDir the base directory of files
     * @param userId the user who initiated this export
     * @param txExportSPFilePath where to find the transaction export stored procedure template
     */
    public void setAttributes(List<SampleItem> sampleList, DatasetItem datasetItem,
            String baseDir, String userId, String txExportSPFilePath) {
        this.sampleList = sampleList;
        this.dataset = datasetItem;
        this.baseDir = baseDir;
        this.userId = userId;
        this.txExportSPFilePath = txExportSPFilePath;
    }

    /**
     * Generate TxExportInfo instance for the sample, using values from this bean.
     * @param sample the sample
     * @return a TxExportInfo instance for the sample, using values from this bean.
     * @throws IOException thrown by TxExportInfo constructor
     */
    private synchronized TxExportInfo txExportInfo(SampleItem sample) throws IOException {
        TxExportInfo info = helper.initTxExportInfo(dataset, sample);

        info.setBaseDir(baseDir);
        info.setStudentBatchSize(DEFAULT_STUDENT_BATCH_SIZE);
        info.setTxExportSPFilePath(txExportSPFilePath);

        return info;
    }

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
            if (getNumCompletedRows() > getNumTotalRows()) {
                setNumCompletedRows(getNumTotalRows() - 1);
            }
        }
        // super method will use our estimated completed rows to calculate percentage done.
        return super.getPercent();
    }

    /**
     * Starts this bean running.  For each sample, first determine if an existing cached export
     * exists.  If no cached file exists, prepare an export for the user, cache it, then zip it up.
     * Make sure to add in any existing cached files (if multiple samples were selected).  Result
     * is a zip file that contains one to many individual cached export zips.
     * The AbstractExportBean takes care of the final zipping of the file.
     */
    public void run() {
        try {
            init();
            startTime = currentTimeMillis();
            setInitializing(true);
            setCachedFileAvailable(true);
            setRunning(true);
            logDebug("Performing Data Export... ", getTemporaryFileName());
            if (sampleList == null || sampleList.size() < 1) {
                setRunning(false);
                return;
            }

            Map<Comparable, Long> sampleSizeMap = new HashMap<Comparable, Long>();
            Map<String, String> cachedSampleExports = new HashMap<String, String>();
            //DS1234: (Web Services: Can't get transactions for All Data sample
            //         after importing new KC models)
            //The next line used to call this: helper.getSamplesToCacheAggSampleOnly(dataset);
            List<SampleItem> samplesQueuedToCache = helper.getSamplesQueuedToCacheTx(dataset);
            List<String> cachedFilesToDelete = new ArrayList<String>();
            for (SampleItem theSample : sampleList) {
                if (isCancelFlag()) { break; }

                String cachedFileName = helper.getCachedFileName(txExportInfo(theSample));
                if (!samplesQueuedToCache.contains(theSample) && cachedFileName != null) {
                    // Sample does not require caching and a cached file exists, so return it.
                    cachedSampleExports.put(theSample.getSampleName(), cachedFileName);
                    logDebug(theSample, "Found a cached export for the sample.");
                    incrementNumTotalRows(1);
                } else {
                    // Samples needs cached/re-cached or a cache already exists.
                    // Re-cache the sample then delete the existing file (if applicable).
                    if (samplesQueuedToCache.contains(theSample) && cachedFileName != null) {
                        cachedFilesToDelete.add(cachedFileName);
                        logDebug(theSample,
                            "Found a cached export but the sample needs to be recached.");
                    }
                    Long numTrans = sampleMetricDao.getTotalTransactions(theSample);
                    incrementNumTotalRows(numTrans.intValue());
                    sampleSizeMap.put(theSample.getId(), numTrans);
                    setCachedFileAvailable(false);
                    logDebug(theSample, "Cached export not available");
                }
            }
            logDebug("Total Transactions : ", getNumTotalRows());

            setInitializing(false); //used to prevent divide by zeros.

            // if everything was cached then there is nothing left to process.
            if (sampleSizeMap.size() != 0) {
                helper.initExportCache(sampleList.get(0), new ExportCache());
                for (SampleItem sample : sampleList) {
                    sample = sampleDao.get((Integer)sample.getId());
                    if (sample == null) {
                        logger.warn("SampleItem came back as null");
                    } else if (cachedSampleExports.containsKey(sample.getSampleName())) {
                        logInfo(sample, "Sample had a cached file.");
                    } else {
                        exportTask = helper.txExportTask(txExportInfo(sample));
                        isWaiting = exportTask.isExportStarted();
                        if (isWaiting) {
                            logDebug(sample, "Waiting on the export to finish...");
                            while (!isCancelFlag() && !exportTask.isExportCompleted()) {
                                Thread.sleep(MILLISECONDS_PER_SECOND);
                            }
                            isWaiting = false;
                            if (!isCancelFlag()) {
                                if (exportTask.getCachedFileName() == null) {
                                    logInfo(sample, "Finished waiting for sample ",
                                            sample.getSampleName(),
                                            " but no cached file found, so recaching export.");
                                    exportSample(sample);
                                } else {
                                    logInfo(sample, "Finished waiting for sample ",
                                            sample.getSampleName());
                                }
                            }
                        } else {
                            exportSample(sample);
                        }
                        if (!isCancelFlag()) {
                            cachedSampleExports.put(sample.getSampleName(),
                                    exportTask.getCachedFileName());
                        }
                    }
                    if (exportTask != null) { exportTask.cleanup(); }
                }
            }
            // grab the cached export files and add to a zip file
            Set<String> keySet = cachedSampleExports.keySet();
            for (String sampleName : keySet) {
                copyCachedFile(dataset,
                    cachedSampleExports.get(sampleName), keySet.size(), TX_EXPORT);
                // Trac 319: Removed incrementNumCompletedRows() which caused
                // the export to be sent prematurely, i.e. before the cached
                // file had been written to the zipstream.
            }
            // delete any out-of-date cached files
            for (String cachedFileName : cachedFilesToDelete) {
                helper.deleteFile(cachedFileName);
            }
            if (isCancelFlag()) {
                logDebug("Canceling export file build");
                setRunning(false);
                setInitializing(false);
            } else {
                int numTotalRows = getNumTotalRows();
                logDebug("Done building export file for ", numTotalRows, " rows.");
                setNumCompletedRows(numTotalRows);

                // moving this close is a bad idea; make sure the zip file is closed before
                // sending the file to the browser.
                closeZipStream();

                // keep the thread alive while a call come back for the item.
                logDebug("keeping the thread alive.");

                if (downloadExport) {
                    // The user elected to download the cached export
                    long timeToDie = currentTimeMillis()
                        + KEEP_ALIVE_SECONDS * MILLISECONDS_PER_SECOND;

                    while (!isExported() && currentTimeMillis() < timeToDie) {
                        Thread.sleep(MILLISECONDS_PER_SECOND);
                    }
                } else {
                    // The system elected to use a cached tx export to create a new dataset.
                    setCachedFileAvailable(true);
                }
                logDebug("finished keeping the thread alive.");
            }
        } catch (Exception exception) {
            logger.error("Caught Exception: ", exception);
            setNumTotalRows(-1);
            setHasError(true);
            setRunning(false);
            setInitializing(false);
        } finally {
            setRunning(false);
            // ALWAYS delete the temp file
            // deleteTempFile();
            String benchmarkPrefix = AbstractServlet.getBenchmarkPrefix(
                    getClass().getSimpleName(), userId);
            logger.info(benchmarkPrefix + " Thread stop.");
            try {
                if (exportTask != null) { exportTask.cleanup(); }
            } catch (SQLException sqle) {
                logger.error(CLEANUP_ERR_MSG, sqle);
            }
        }
    }

    /** Error message if cleanup fails. */
    private static final String CLEANUP_ERR_MSG = "SQL exception while cleaning up export task.";

    /**
     * Create the cached export file for sample.
     * @param sample the sample
     * @throws SQLException if something goes wrong with the transaction export stored procedures.
     * @throws IOException if something goes wrong with writing the transaction export files
     */
    private void exportSample(final SampleItem sample) throws SQLException, IOException {
        logDebug(sample, "Exporting.");
        if (exportTask.loadSPs()) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus ts) {
                    try {
                        if (!isCancelFlag()) { exportTask.writeHeaders(); }
                        if (!isCancelFlag()) { exportTask.cacheSample(); }
                        if (!isCancelFlag()) {
                            exportTask.logExportCompleted();
                            long txsCount = exportTask.getTxExportCount();
                            logInfo(sample, "Exported ", txsCount, " rows for sample");
                        }
                    } catch (Exception e) {
                        String msg = "Error caching sample " + sample.getNameAndId();
                        logger.error(msg, e);
                        if (sendEmail) {
                            String source = TxExportBean.this.getClass().getSimpleName();
                            MailUtils.sendErrorEmail(source, emailAddress, emailAddress, msg, e);
                        }
                        throw new RuntimeException("Error caching sample.", e);
                    }
                }
            });
        } else {
            logger.error("Loading SPs failed");
            throw new RuntimeException("Loading SPs failed.");
        }
    }

    /**
     * Stops running this bean.
     */
    public void stop() {
        if (exportTask != null) { exportTask.cancel(); }
        super.stop();
    }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param content sample and/or dataset label
     * @return a string useful for logging
     */
    private String getLogPrefix(String content) {
        return "TxExportBean [" + content + "]: ";
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

    public Boolean getDownloadExport() {
        return downloadExport;

    }

    public void setDownloadExport(Boolean downloadExport) {
        this.downloadExport = downloadExport;

    }
}
