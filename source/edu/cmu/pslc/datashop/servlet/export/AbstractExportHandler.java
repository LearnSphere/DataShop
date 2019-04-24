/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static edu.cmu.pslc.logging.util.DateTools.getElapsedTimeString;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * This class handles the export calls for an AbstractExport bean instantiating it,
 * making sure it's stored in the session properly, etc.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11732 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-23 14:13:18 -0500 (Sun, 23 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
/** Convenience methods for export. */
public abstract class AbstractExportHandler {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The request from the instantiating servlet. */
    private HttpServletRequest req;
    /** The response from the instantiating servlet. */
    private HttpServletResponse resp;

    /** Dataset context required for user logging. */
    private DatasetContext datasetContext;
    /** Export context object for saving items in and out of the session. */
    private ExportContext exportContext;
    /** Export type for logging. */
    private String exportType;
    /** User id for log4j logging. */
    private String userId;

    /** parameter indicating this is an export call of some type. */
    private static final String START_EXPORT_PARAM = "export_start";
    /** parameter indicating we are checking status of export. */
    private static final String CHECK_EXPORT_PARAM = "export_check";
    /** parameter indicating we are checking status of export. */
    private static final String CANCEL_EXPORT_PARAM = "export_cancel";
    /** parameter indicating to get the final export product. */
    private static final String GET_EXPORT_FILE_PARAM = "get_export_file";
    /** status key for JSONObjects. */
    private static final String STATUS_JSON_KEY = "status";
    /** message key for JSONObjects. */
    private static final String MESSAGE_JSON_KEY = "message";
    /** The type of export file to be used in the actual file name. */
    private static final String STUD_STEP_TYPE = "student_step";
    /** The type of export file to be used in the actual file name. */
    private static final String STUD_PROB_TYPE = "student_problem";

    /** Buffer size for zip file processing. */
    public static final int BUFFER_SIZE = 18024;
    /**
     * Default Constructor.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param datasetContext {@link DatasetContext}
     * @param exportType from the {@link UserLogger} for logging purposes.
     */
    public AbstractExportHandler(HttpServletRequest req, HttpServletResponse resp,
            DatasetContext datasetContext, String exportType) {
        this.req = req;
        this.resp = resp;
        this.exportContext = datasetContext.getExportContext();
        this.datasetContext = datasetContext;
        this.userId = datasetContext.getUserId();
        this.exportType = exportType;
    }

    /** Returns req. @return Returns the req. */
    public HttpServletRequest getReq() { return req; }

    /** Returns {@link HttpServletResponse}. @return Returns the {@link HttpServletResponse}. */
    public HttpServletResponse getResp() { return resp; }

    /** Returns datasetContext. @return Returns the datasetContext. */
    public DatasetContext getDatasetContext() { return datasetContext; }

    /** Returns exportContext. @return Returns the exportContext. */
    public ExportContext getExportContext() { return exportContext; }

    /**
     * Process the export request.
     * @param fileExtension either "txt" or "zip" depending on the caller and handler
     * @return boolean indicating the request was processed & results returned, false if not
     * processed or not response sent.
     * @throws IOException {@link IOException}
     */
    public boolean processRequest(String fileExtension) throws IOException {

        return processRequest("text/cvs; charset=UTF-8", fileExtension, true);
    }

    /**
     * Process the export request.
     * @param contentType the content type of the export (txt, zip, etc.).
     * @param fileExtension either "txt" or "zip" depending on the caller and handler
     * @param downloadExport whether or not to actually download the export. The Sample to Dataset
     * feature requires access to the Tx export bean but does not require the actual export.
     * @return boolean indication the request was processed and results returned, false if not
     * processed or no response sent.
     * @throws IOException {@link IOException}
     */
    public boolean processRequest(String contentType, String fileExtension,
            Boolean downloadExport)
        throws IOException {
        String paramsString = "";
        for (String param :  (Set<String>)req.getParameterMap().keySet()) {
            if (param.toLowerCase().contains("password")) {
                paramsString += param + "(******), ";
            } else if (param.toLowerCase().contains("postdata")) {
                paramsString += param + ", ";
            } else {
                String p = req.getParameter(param);
                // Don't print really long params..
                if (p.length() < 100) {
                    paramsString += param + "(" + p + "), ";
                } else {
                    paramsString += param + "(...)";
                }
            }
        }
        logger.debug(paramsString);

        String prefix = "processRequest(" + userId + "): ";
        logDebug(prefix, "Processing export request.");
        Boolean isBeanRunning = false;
        if (isStartExportRequest()) {
            // If the bean is currently in the session, get it.
            AbstractExportBean bean = getExportBean();

            // This handles the case where the export bean
            // in the session is not up to date with the system.
            if (bean != null && !bean.isRunning()) {
                bean.stop();
                bean = null;
            }

            // If the bean is null, then create it
            if (bean == null) {
                bean = createExportBean(); //create a new bean.
            }
            // If the bean is not running then start it
            if (bean != null && !bean.isRunning()) {
                new Thread(bean).start();
                logInfo(prefix, "Started new export thread.");
            } else {
                // The bean was already started by another process.
                // Piggy back it.
                isBeanRunning = true;
            }

            setExportBean(bean);
            if (downloadExport) {
            // If we are downloading the cached export,
            // then respond as the export javascript expects
                writeString(resp, "text/html", "started");
            }

            return true;
        }
        // If the bean is already running or the client
        // issues a check on the bean's status
        if (isCheckStatusRequest() || isBeanRunning) {
            logInfo(prefix, "Export check.  Export type :: ", exportType);
            AbstractExportBean bean = getExportBean();
            try {
                if (bean == null) {
                    writeJSON(resp, setupJSONResponse(-1,
                        "An error occurred while processing your request."));
                    logger.error(prefix + "AbstractExportBean was null while processing a "
                            + exportType + " request.");
                    return true;
                } else {
                    String response = null;

                    if (exportType.equals(UserLogger.EXPORT_TRANSACTIONS)) {
                        List<SampleItem> selectedSamples =
                            datasetContext.getNavContext().getSelectedSamples();
                        if (bean.isInitializing()) {
                            response = "Processing your request ...";
                        } else if (bean.isCachedFileAvailable()) {
                            if (!downloadExport) {
                                response = "Caching export file for dataset creation.";
                            } else {
                                response = "Sending cached export file ...";
                            }
                        } else if (!(selectedSamples.size() == 1)
                                   || !(selectedSamples.get(0).isAllData())) {
                            response = "Building export file for the first time."
                                + " This may take a while."
                                + " For a speedy export, try All Data.";
                        } else {
                            response = "Building export file for the first time."
                                + " This may take a while.";
                        }
                    } else if (exportType.equals(UserLogger.EXPORT_PROBLEM_STUDENT)
                            || exportType.equals(UserLogger.EXPORT_STEP_ROLLUP)) {
                        List<SampleItem> selectedSamples =
                            datasetContext.getNavContext().getSelectedSamples();

                        if (bean.isInitializing()) {
                            response = "Processing your request ...";
                        } else if (bean.isCachedFileAvailable()) {
                            response = "Sending cached export file ...";
                        } else {
                            response = "Building export file for the first time."
                                + " This may take a while.";
                        }
                    } else if (exportType.equals(UserLogger.EXPORT_STEP_LIST)) {
                        response = "Building step list export ...";
                    } else if (exportType.equals(UserLogger.MODEL_EXPORT)) {
                        response = "Building KC Model export ...";
                    }

                    int percent = bean.getPercent();
                    if (percent == AbstractExportBean.ZERO_ROWS_ERROR_CODE) {
                        response = "There are zero rows in this export.";
                    } else if (percent == AbstractExportBean.UNKNOWN_ERROR_CODE) {
                        response = "An unknown error occurred.";
                    }

                    logDebug(prefix,
                            "processRequest, Percent: ", percent, ", Response: ", response);
                    writeJSON(resp, setupJSONResponse(percent, response));
                    return true;
                }
            } catch (JSONException jsonException) {
                logger.error(prefix + "JSONException thrown in the AbstractExportBean. "
                        + jsonException.getMessage());
                return true;
            }
        }
        if (isCancelRequest()) {
            logInfo(prefix, "Export canceled.");
            AbstractExportBean bean = getExportBean();
            if (bean == null) {
                logger.warn(prefix + "Export Bean was null when canceling.");
            } else {
                bean.stop();
                try {
                    logInfo(prefix, "Calling thread.join() to wait for thread to stop.");
                    new Thread(bean).join();
                } catch (InterruptedException e) {
                    logger.warn(prefix + "AbstractExportBean thread was interrupted "
                            + "when attempting to stop.");
                }
            }
            removeExportBean();
            writeString(resp, null, "stopped");
            return true;
        }

        if (isGetFileRequest()) {
            logInfo(prefix, "Getting export file.");
            File exportFile = null;
            File readMeFile = null;
            File termsFile = null;
            AbstractExportBean bean = getExportBean();

            if (bean == null) {
                logger.warn(prefix + "Export Bean was null when getting file.");
            } else {

                exportFile = bean.getResultsFile();
                if (exportFile != null) {
                    logDebug(prefix, "Export File's absolute path is: ",
                        exportFile.getAbsolutePath());
                }
            }
            if (exportFile != null) {
                updateLastExported();
                if (downloadExport) {
                    String fileNameType = getExportFileNameType(bean);
                    String cleanedFileName =
                        AbstractServlet.getExportFileName(datasetContext.getDataset(), fileNameType);
                    String fileName = cleanedFileName + "." + fileExtension;
                    resp.setContentType(contentType);
                    resp.addHeader("Content-Disposition", "attachment; filename=" + fileName);

                    ServletOutputStream outStream = null;
                    BufferedInputStream buf = null;
                    FileInputStream input = null;
                    File tempZipFile = null;
                    try {
                        outStream = resp.getOutputStream();
                        resp.setContentLength((int)exportFile.length());
                        // README.txt has already been created and stored in the temp file
                        input = new FileInputStream(exportFile);

                        if (input != null) {
                            buf = new BufferedInputStream(input);
                            int readBytes = 0;
                            while ((readBytes = buf.read()) != -1) {
                                outStream.write(readBytes);
                            }
                            outStream.flush();
                        }
                    } catch (Exception e) {
                        logger.error(prefix
                                + "An exception occurred while writing export file to client.", e);
                    } finally {
                        if (outStream != null) { outStream.close(); }
                        if (buf != null) { buf.close(); }
                        if (input != null) { input.close(); }
                        if (tempZipFile != null) { tempZipFile.delete(); }
                        if (readMeFile != null) { readMeFile.delete(); }
                        if (termsFile != null) { termsFile.delete(); }
                    }
                }

                bean.deleteTempFile();
                bean.stop();
                removeExportBean();
                return true;
            } else {
                bean.deleteTempFile();
                bean.stop();
                removeExportBean();
                logger.warn(prefix + "Export File was null.");
            }
        }
        logDebug(prefix, "No action taken in process export request.");

        return false;
    }

    /**
     * Create a zip archive for this export
     * @param fileMap  a map of filename and  files to be zipped
     * @return File temp zip file
     * @throws IOException exception
     * */
    private File zipExportFile(
            Map<String, File> fileMap) throws IOException {
        String prefix = "zipExportFile(" + userId + "): ";

        byte[] buffer = new byte[BUFFER_SIZE];
        long zipStartTime = System.currentTimeMillis();
        BufferedInputStream inputStream = null;
        ZipOutputStream outputStream = null;
        String fileName = null;
        File file = null;
        File tempZipFile = File.createTempFile("zip_", null);
        int length = 0;
        try {
            logDebug(prefix, "attempting to build zip archive.");
            outputStream =
                new ZipOutputStream(new FileOutputStream(tempZipFile));
            outputStream.setLevel(Deflater.DEFAULT_COMPRESSION);

            // loop through the file map to zip them up
            for (Entry<String, File> entry : fileMap.entrySet()) {

                fileName = entry.getKey();
                file = entry.getValue();
                inputStream = new BufferedInputStream(new FileInputStream(file));

                outputStream.putNextEntry(new ZipEntry(fileName));
                // Transfer bytes from the current file to the ZIP file
                length = 0;

                while ((length = inputStream.read(buffer, 0, BUFFER_SIZE)) > -1) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.closeEntry();
                inputStream.close();
            }

            logInfo(prefix, "Finished creating zip file. ", getElapsedTimeString(zipStartTime));
        } catch (IllegalArgumentException iae) {
            logger.error(prefix + "IllegalArgumentException when trying to create a zip file "
                    + iae.getMessage());
            tempZipFile = null;
        } catch (FileNotFoundException fnfe) {
            logger.error(prefix + "FileNotFoundException when trying to create a zip file "
                    + fnfe.getMessage());
            tempZipFile = null;
        } catch (IOException ioe) {
            logger.error(prefix + "IOException when trying to create a zip file for "
                    + ioe.getMessage());
            tempZipFile = null;
        } finally {
            if (outputStream != null) { outputStream.close(); }
        }
        return tempZipFile;
    }

    /**
     * Convenience for writing a string with content type as the response.
     * @param resp the response
     * @param contentType the content-type
     * @param str the content
     * @throws IOException if something goes wrong, God forbid
     */
    private void writeString(HttpServletResponse resp, String contentType, String str)
    throws IOException {
        if (contentType != null) {
            resp.setContentType(contentType);
        }
        PrintWriter out = resp.getWriter();
        out.write(str);
        out.flush();
        out.close();
    }

    /**
     * Convenience method for setting up a JSONObject in response to a call to the server.
     * @param status the status to be sent back (usually a % complete).
     * @param message the corresponding message to display.
     * @return a JSONObject ready to send back to the caller.
     * @throws JSONException JSONException
     */
    private JSONObject setupJSONResponse(int status, String message) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(STATUS_JSON_KEY, status);
        json.put(MESSAGE_JSON_KEY, message);
        return json;
    }

    /**
     * Convenience method used to write a JSON object as the response.
     * @param resp the response
     * @param json the JSONObject to write
     * @throws IOException if something goes wrong, waaaaahh!
     */
    private void writeJSON(HttpServletResponse resp, JSONObject json)
            throws IOException {
        PrintWriter out;
        resp.setContentType("application/json");
        out = resp.getWriter();
        out.write(json.toString());
        out.flush();
    }

    /**
     * This updates the last exported time and number of times exported values
     * for this user on this dataset.
     */
    private void updateLastExported() {
        String datasetName = datasetContext.getDataset().getDatasetName();

        String sampleListString;
        if (exportType.equals(UserLogger.EXPORT_TRANSACTIONS)
                || exportType.equals(UserLogger.EXPORT_STEP_LIST)) {

            logInfo(exportType, " on dataset ", datasetName, " by ", userId);
            sampleListString = "Exported "
                + datasetContext.getNavContext().getSelectedSamples().size() + " sample(s): ";
            for (Iterator <SampleItem> it =
                datasetContext.getNavContext().getSelectedSamples().iterator(); it.hasNext();) {
                SampleItem sample = it.next();
                sampleListString += sample.getSampleName() + "(" + sample.getId() + ")";
                if (it.hasNext()) { sampleListString += ", "; }
            }
        } else {
            sampleListString = null;
        }
        // If this is a KCM Export, Student-Problem Export, or Student-Step Export,
        // then don't send a log message, as the KCM and SP Export Beans handle it.
        if (!exportType.equals(UserLogger.MODEL_EXPORT)
            && !exportType.equals(UserLogger.EXPORT_PROBLEM_STUDENT)
            && !exportType.equals(UserLogger.EXPORT_STEP_ROLLUP)) {
            UserLogger.log(datasetContext.getDataset(),
                    datasetContext.getUser(), exportType, sampleListString);
        }
    }

    /**
     * convenience method for checking whether a parameter has a value
     * @param param the parameter
     * @return the parameter value
     */
    private boolean checkParameter(String param) {
        return req.getParameter(param) != null;
    }

    /**
     * check if we are updating a pre-existing or creating a new.
     * @return whether we are updating a pre-existing or creating a new.
     */
    public boolean isStartExportRequest() { return checkParameter(START_EXPORT_PARAM); }

    /**
     * whether this is a check for percentage completed.
     * @return whether this is a check for percentage completed
     */
    public boolean isCheckStatusRequest() { return checkParameter(CHECK_EXPORT_PARAM); }

    /**
     * whether this is a request to cancel the export.
     * @return whether this is a request to cancel the export.
     */
    public boolean isCancelRequest() { return checkParameter(CANCEL_EXPORT_PARAM); }

    /**
     * ready to transfer the export file.
     * @return ready to transfer the export file
     */
    public boolean isGetFileRequest() { return checkParameter(GET_EXPORT_FILE_PARAM); }

    /**
     * Remove the ExportBean from the session.
     */
    public void removeExportBean() {
        setExportBean(null);
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if info is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }

    /**
     * Create a new instance of the required export bean.
     * @return {@link AbstractExportBean} a newly created export bean.
     */
    public abstract AbstractExportBean createExportBean();

    /**
     * Export Bean stored in the session.
     * @return {@link AbstractExportBean} stored in the session
     */
    public abstract AbstractExportBean getExportBean();

    /**
     * Store the ExportBean from the session.
     * @param bean the {@link AbstractExportBean}
     */
    public abstract void setExportBean(AbstractExportBean bean);

    /**
     * Get the string to include for the type of export in the export file name.
     * @param bean the bean
     * @return a string of the type in the file name
     */
    public abstract String getExportFileNameType(AbstractExportBean bean);
}