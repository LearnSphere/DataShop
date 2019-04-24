/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_KCM_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_KCM_EXPORT_START;
import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.KCModelStepExportDao;
import edu.cmu.pslc.datashop.dao.ModelExportDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.KCModelStepExportItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Helper class for those actions necessary to persist KC model export stats.
 *
 * @author Cindy Tipper
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelStepExportTask {
    /** Dataset that owns the KC models. */
    private DatasetItem dataset;
    /** 'All Data' sample for the specified dataset. */
    private SampleItem sample;
    /** Time that processing for the batch started. */
    private Date start;
    /** Number of StepExport rows exported. */
    private Integer numRows;

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Time that processing for the batch started.
     * @return time that processing for the batch started.
     */
    public Date getStart() { return start; }

    /**
     * Create a KC model export task.
     * @param dataset the relevant DatasetItem
     */
    public KCModelStepExportTask(DatasetItem dataset) {
        this.dataset = dataset;

        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        this.sample = sampleDao.findOrCreateDefaultSample(dataset);

        start = new Date();
        numRows = 0;
    }

    /** Number of steps to return and process in a chunk */
    private static final int BATCH_SIZE = 500;

    /**
     * Run query to generate StepExport rows.
     */
    public void queryStepExport() {
    }

    /**
     * Export the KC model stats.
     */
    public void writeKCModelStepExport() {
        ModelExportDao modelExportDao = DaoFactory.DEFAULT.getModelExportDao();
        KCModelStepExportDao kcmeDao = DaoFactory.DEFAULT.getKCModelStepExportDao();

        List<StepExportRow> stepInfo = new ArrayList<StepExportRow>();
        int offset = 0;
        do {
            stepInfo = modelExportDao.getStepExport(dataset, BATCH_SIZE, offset);
            offset += BATCH_SIZE;

            for (StepExportRow row : stepInfo) {
                KCModelStepExportItem exportItem = exportItemFromStepRow(row, dataset);
                kcmeDao.saveOrUpdate(exportItem);
                numRows++;
            }
        } while (stepInfo.size() == BATCH_SIZE);
    }

    /**
     * Helper method to convert a StepExportRow DTO to a KCModelStepExportItem.
     * TBD: move this to DTO or item...
     *
     * @param row the StepExportRow object
     * @param dataset the DatasetItem
     * @return KCModelStepExportItem
     */
    private KCModelStepExportItem exportItemFromStepRow(StepExportRow row, DatasetItem dataset) {

        KCModelStepExportItem result = new KCModelStepExportItem(row);
        result.setDataset(dataset);
	result.setStep(DaoFactory.DEFAULT.getSubgoalDao().find(row.getStepId()));

        return result;
    }

    /**
     * Delete existing KC model export rows for specified dataset.
     * @param dataset the DatasetItem
     */
    public void deleteKCModelStepExportRows(DatasetItem dataset) {
        KCModelStepExportDao kcmeDao = DaoFactory.DEFAULT.getKCModelStepExportDao();
        Integer numDeleted = kcmeDao.clear(dataset);
        logInfo(getLogPrefix(dataset), "Deleted ", numDeleted, " KC model export rows.");
    }

    /**
     * Get string which includes dataset id.
     * @return dataset info prefix
     */
    protected String getDatasetInfoPrefix() {
        Integer datasetId = (Integer)dataset.getId();
        String dsInfo = "ds" + datasetId;
        return dsInfo;
    }

    /**
     * Return a nicely formatted string for logging purposes.
     * @param dataset the dataset.
     * @return a nicely formatted string.
     */
    public String formatForLogging(DatasetItem dataset) {
        return dataset.getDatasetName() + " (" + dataset.getId() + ")";
    }

    /**
     * Label identifying the kind of export.
     * @return label identifying the kind of export
     */
    public String getPrefixLabel() { return "KCM"; }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param content sample and/or dataset label
     * @return a string useful for logging
     */
    private String getLogPrefix(String content) {
        return getPrefixLabel() + " [" + content + "] : ";
    }

    /**
     * Utility method to consistently create a prefix of the dataset name and id logging.
     * @param dataset the dataset.
     * @return a string useful for logging
     */
    private String getLogPrefix(DatasetItem dataset) {
        return getLogPrefix(formatForLogging(dataset));
    }

    /**
     * Utility method to consistently create a prefix for logging.
     * @return a string useful for logging
     */
    protected String getLogPrefix() {
        return getLogPrefix(dataset);
    }

    /** Convenience method to get the Dataset System Log DAO.
     * @return the Dataset System Log DAO. */
    protected DatasetSystemLogDao datasetSystemLogDao() {
        return DaoFactory.DEFAULT.getDatasetSystemLogDao();
    }

    /**
     * Check for a message in the dataset system log indicating that an export for this dataset
     * and sample was already started.
     * @return whether an export for this dataset and sample was already started
     */
    public boolean isExportStarted() {
        boolean isStarted =
            datasetSystemLogDao().messageCheck(dataset, sample, exportStartAction());

        if (!isStarted) {
            logInfo("Starting export.");
            SystemLogger.log(dataset, null, sample, exportStartAction(), getLogPrefix(dataset)
                             + "Started Cached Export.", true, null);
        }

        return isStarted;
    }

    /** Don't leave a mess! */
    public void cleanup() {
        datasetSystemLogDao().removeMessage(dataset, sample, exportStartAction());
    }

    /**
     * Record completion of transaction export in the dataset system log.
     * @param success whether or not the export completed successfully
     */
    public synchronized void logExportCompleted(boolean success) {
        long elapsedTime = System.currentTimeMillis() - start.getTime();
        String msg = null;
        if (success) {
            msg = getLogPrefix() + "Cached KC model export.";
        } else {
            msg = "ERROR attempting to cache KC model export for " + getLogPrefix();
        }

        // Have to include sample bc the check for need to cache is by sample, not dataset.
        SystemLogger.log(dataset, null, sample, exportCompletedAction(),
                         msg, success, numRows, elapsedTime);
    }

    /**
     * Only log if info level is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    protected void logInfo(Object... args) {
        if (logger.isInfoEnabled()) {
            LogUtils.logInfo(logger, getLogPrefix(), concatenate(args));
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    protected void logDebug(Object... args) {
        if (logger.isDebugEnabled()) {
            LogUtils.logDebug(logger, getLogPrefix(), concatenate(args));
        }
    }

    /**
     * System log action indicating that the export is finished.
     * @return system log action indicating that the export is finished
     */
    public String exportCompletedAction() {
        return ACTION_CACHED_KCM_EXPORT;
    }

    /**
     * System log action indicating that the export started.
     * @return system log action indicating that the export is finished
     */
    public String exportStartAction() {
        return ACTION_CACHED_KCM_EXPORT_START;
    }
}