/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import static edu.cmu.pslc.datashop.helper.SystemLogger.log;
import static edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper.TIME_STAMP_REGEX;
import static edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper.formattedNow;
import static edu.cmu.pslc.datashop.util.FileUtils.copyStream;
import static edu.cmu.pslc.datashop.util.FileUtils.cleanForFileSystem;
import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.servlet.export.CachedExportFileReader;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Abstracts common functionality from the transaction and student step export tasks.
 * (TxExportTask currently does not extend this, but should be refactored to do so in the future.)
 *
 * @author Jim Rankin
 * @version $Revision: 13579 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-09-29 09:59:00 -0400 (Thu, 29 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class ExportTask {
    /** Initial size of the string buffer */
    protected static final int STRING_BUFFER_SIZE = 262144;
    /** holds information relevant to cached files */
    protected CachedFileInfo info;
    /** writes to the temporary file */
    protected PrintWriter tempFileWriter;
    /** Time that processing for the batch started. */
    private Date start;

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Time that processing for the batch started.
     * @return time that processing for the batch started.
     */
    public Date getStart() { return start; }

    /**
     * Create an export task.
     * @param info holds information relevant to cached files
     */
    public ExportTask(CachedFileInfo info) {
        this.info = info;
        start = new Date();
    }

    /**
     * The directory path for export files.
     * @return the directory path for export files
     * @throws IOException if something goes wrong
     */
    private String getDirectoryPath() throws IOException {
        return getDirectory().getCanonicalPath();
    }

    /**
     * The directory for export files.
     * @return the directory for export files
     * @throws IOException thrown if directory is not created
     */
    private File getDirectory() throws IOException {
        File dir = new File(join("/", info.getBaseDir(), info.getSample().getFilePath(), "export",
            getExportSubdirectory()));

        if (dir.mkdirs()) {
            FileUtils.makeWorldReadable(dir);
        }
        if (!dir.exists()) {
            throw new IOException("Cannot create directory.");
        }
        dir.setReadable(true, false);
        dir.setWritable(true, false);

        return dir;
    }

    /**
     * Process the sample's name to make it suitable as a file name.
     * @return the sample's name processed to be suitable as a file name
     */
    private String cleanedSampleName() {
        SampleItem sample = info.getSample();
        return cleanForFileSystem(sample.getSampleName()) + "_" + sample.getId();
    }

    /**
     * Write text as the next line in the temp file.
     * @param text the next line in the temp file
     * @throws IOException if something goes wrong
     */
    protected void writeToTempFile(CharSequence text) throws IOException {
        if (tempFileWriter == null) {

            File tempFile = info.getTempFile();
            tempFileWriter = new PrintWriter(tempFile);
        }
        tempFileWriter.println(text);
    }

    /**
     * Write text using print instead of println.
     * @param text the text to write
     * @throws IOException if something goes wrong
     */
    protected void writeToTempFileSameLine(CharSequence text) throws IOException {
        if (tempFileWriter == null) {
            File tempFile = info.getTempFile();
            tempFileWriter = new PrintWriter(tempFile);
        }
        tempFileWriter.print(text);
    }

    /** Close the temp file. */
    protected void closeTempFileWriter() {
        if (tempFileWriter != null) { tempFileWriter.close(); }
    }

    /**
     * Write the contents of the temp file to a zip file at the proper location.
     * @return the name of the new cached file
     * @throws IOException if something goes wrong
     */
    public String createCachedFile() throws IOException {
        logInfo("Creating cached file from temp file.");
        String zipFileName = null;

        String baseFileName = getFileNamePrefix() + cleanedSampleName() + "_" + formattedNow();
        zipFileName = getDirectoryPath() + "/" + baseFileName + ".zip";

        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));

        outputStream.setLevel(DEFAULT_COMPRESSION);

        outputStream.putNextEntry(new ZipEntry(baseFileName + ".txt"));

        copyStream(new FileInputStream(info.getTempFile()), outputStream);
        logInfo("Finished creating cached file.");

        File fileRef = new File(zipFileName);
        FileUtils.makeWorldReadable(fileRef);
        return zipFileName;
    }

    /**
     * Full path of the most recently created cached file.
     * @return full path of the most recently created cached file
     * @throws IOException throw exception on insufficient permissions or other IOException
     */
    public String getCachedFileName() throws IOException {
        String mostRecent = null;

        try {
            for (String fileName : getDirectory().list()) {
                String[] split = fileName.split(TIME_STAMP_REGEX);
                if ((split[0]).contains(cleanedSampleName()) && fileName.endsWith(".zip")) {
                    String actualFileName = getDirectoryPath() + "/" + fileName;
                    if (mostRecent == null
                            || actualFileName.compareTo(mostRecent) > 0) {
                        // delete old duplicate files
                        if (mostRecent != null) { new File(mostRecent).delete(); }
                        mostRecent = actualFileName;
                    }
                }
            }
        } catch (IOException ioe) {
            throw new IOException("Insufficient permissions to create cached file.");
        }

        return mostRecent;
    }

    /**
     * Create a CachedExportFileReader for the sample.
     * @return a CachedExportFileReader for the sample
     * @throws IOException throw exception on insufficient permissions or other IOException
     */
    public CachedExportFileReader cachedFileReader() throws IOException {
        String cachedFileName = getCachedFileName();
        return cachedFileName == null ? null : new CachedExportFileReader(cachedFileName);
    }

    /**
     * Create a CachedExportFileReader for the sample for web services.
     * @return a CachedExportFileReader for the sample for web services
     * @throws IOException throw exception on insufficient permissions or other IOException
     */
    public CachedExportFileReader cachedFileReaderRaw() throws IOException {
        String cachedFileName = getCachedFileName();
        return cachedFileName == null ? null : new CachedExportFileReader(cachedFileName);
    }

    /**
     * Get string which includes dataset id.
     * @return dataset info prefix
     */
    protected String getDatasetInfoPrefix() {
        Integer datasetId = (Integer)info.getDataset().getId();
        String dsInfo = "ds" + datasetId;
        return dsInfo;
    }

    /** Constant for Transaction Export file names. */
    private static final String TX_EXPORT_PREFIX = "_tx_";

    /**
     * Get string to prepend to the name of cached files.
     * @return file name prefix
     */
    protected String getFileNamePrefix() {
        return getDatasetInfoPrefix() + TX_EXPORT_PREFIX;
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
     * Return a nicely formatted string for logging purposes.
     * @param sample the sample.
     * @return a nicely formatted string.
     */
    public String formatForLogging(SampleItem sample) {
        return sample.getSampleName() + " (" + sample.getId() + ")";
    }

    /**
     * Label identifying the kind of export.
     * @return label identifying the kind of export
     */
    protected String getPrefixLabel() { return "CFG"; }

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
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param dataset the dataset.
     * @param sample the sample
     * @return a string useful for logging
     */
    private String getLogPrefix(DatasetItem dataset, SampleItem sample) {
        return getLogPrefix(formatForLogging(dataset) + " / " + formatForLogging(sample));
    }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @return a string useful for logging
     */
    protected String getLogPrefix() {
        return getLogPrefix(info.getDataset(), info.getSample());
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
        DatasetItem dataset = info.getDataset();
        SampleItem sample = info.getSample();
        boolean isStarted = datasetSystemLogDao().messageCheck(dataset, sample,
                exportStartAction());

        if (!isStarted) {
            logInfo("Starting export.");
            log(dataset, null, sample, exportStartAction(), getLogPrefix(dataset, sample)
                    + "Started Cached Export.", true, null);
        }

        return isStarted;
    }

    /**
     * Convenience method for logging a cached transaction export message to the dataset system
     * log.
     * @param msg the message to log
     * @param success indicates whether the action completed successfully
     * @param value the number of items processed
     * @param elapsedTime the elapsed time in milliseconds
     */
    protected void systemLog(String msg, boolean success, Integer value, Long elapsedTime) {
        log(info.getDataset(), null, info.getSample(), exportCompletedAction(),
                getLogPrefix() + msg, success, value, elapsedTime);
    }

    /** Don't leave a mess! */
    public void cleanup() {
        datasetSystemLogDao().removeMessage(info.getDataset(), info.getSample(),
                exportStartAction());
        if (!info.getTempFile().delete()) {
            logger.debug("Temporary file already deleted: "
                + info.getTempFile().getAbsolutePath());
        }
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
    protected abstract String exportCompletedAction();

    /**
     * System log action indicating that the export started.
     * @return system log action indicating that the export is finished
     */
    protected abstract String exportStartAction();

    /**
     * Subdirectory of the main export directory for the sample.
     * Subclasses must define this to construct the appropriate directory path.
     * @return subdirectory of the main export directory for the sample
     */
    protected abstract String getExportSubdirectory();
}