/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.kcmodel;

import static edu.cmu.pslc.datashop.servlet.kcmodel.KCModelAggregatorBean.STATUS_FINISHED;
import static edu.cmu.pslc.datashop.servlet.kcmodel.KCModelAggregatorBean.STATUS_QUEUED;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportBean;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportHandler;

/**
 * This servlet is for handling the KC model reports, export and imports.
 *
 * @author Benjamin Billings
 * @version $Revision: 14094 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-06-05 11:26:11 -0400 (Mon, 05 Jun 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Hibernate Session wrapped helper. */
    private KCModelHelper modelHelper;

    /** Session Parameter. */
    private static final String MODEL_DELETE_PARAM = "model_delete";
    /** Session Parameter. */
    private static final String MODEL_IMPORT_PARAM = "model_import";
    /** Possible value for session parameter. */
    private static final String MODEL_START_IMPORT = "start";
    /** Possible value for session parameter. */
    private static final String MODEL_CHECK_IMPORT = "check";
    /** Possible value for session parameter. */
    private static final String MODEL_CANCEL_IMPORT_PARAM = "cancel";

    /** Date format. */
    public static final FastDateFormat DATE_FORMAT =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException and input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException and input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);

            DatasetContext datasetContext = getDatasetContext(req);
            modelHelper = HelperFactory.DEFAULT.getKCModelHelper();

            synchronized (datasetContext.getExportContext()) {
                if (new KCModelExportHandler(req, resp, datasetContext).processRequest("txt")) {
                    return;
                }
            }

            //add cross validation
            if (handleImport(req, resp, datasetContext)
                || checkAggregationAndLFAAndCVProgress(req, resp, datasetContext)) {
                return;
            }

            //Check that we have a file upload request
            //This is deprecated as a result of changes to the API that they were unable to
            //implement and not break the API.  It's correct, but comes up as deprecated
            //because the base class has to have the static method.  Should be fixed in future
            //releases, but for the mean time is the correct implementation.
            boolean isMultipart = ServletFileUpload.isMultipartContent(req);
            logDebug("isMultipart: ", isMultipart);
            if (isMultipart) {
                // Check whether there already is an import running for this dataset.
                DatasetItem datasetItem = datasetContext.getDataset();
                DatasetUserLogDao logDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
                Date lastStarted = logDao.areKcmsImporting(datasetItem);
                if (lastStarted != null) {
                    String msg = "KC Model Import already running"
                        + " (since " + DATE_FORMAT.format(lastStarted) + ")."
                        + " Please try importing your model again after it has completed.";
                    JSONObject messageJSON = new JSONObject();
                    messageJSON.append("outcome", "ERROR");
                    messageJSON.append("message", msg);
                    writeString(resp, "text/html", messageJSON.toString());
                    return;
                }

                List <org.apache.commons.fileupload.FileItem> items = getDatasetUploadItems(req);
                if (items == null) {
                    logger.error("File size exceeds 400MB allowance");
                    setInfo(req, datasetContext);

                    JSONObject messageJSON = new JSONObject();
                    messageJSON.append("outcome", "ERROR");
                    messageJSON.append("message", "Error occured trying to upload the file, "
                            + "file size exceeds 400MB allowance");
                    //write this as text/html because it's going to an iFrame window.
                    writeString(resp, "text/html", messageJSON.toString());
                    return;
                } else {
                    datasetContext.setUploadItems(items);
                    JSONObject uploadMessage = handleUpload(datasetContext);
                    if (uploadMessage != null) {
                        setInfo(req, datasetContext);
                        //write this as text/html because it's going to an iFrame window.
                        writeString(resp, "text/html", uploadMessage.toString());
                        return;
                    }
                }
            }

            if (req.getParameter("verifyModel") != null) {
                File importFile = datasetContext.getKCModelContext().getImportFile();

                writeJSON(resp, modelHelper.verifyModelFile(importFile, datasetContext.getUser(),
                        datasetContext.getDataset()));
                return;
            }

            //check if this is a model delete
            String deleteIdString = req.getParameter(MODEL_DELETE_PARAM);
            if (deleteIdString != null && deleteIdString.matches("\\d+")) {

                if (modelHelper.deleteModel(datasetContext.getUser(), Long.parseLong(deleteIdString))) {
                    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
                    navHelper.initializeAll(datasetContext);
                    setInfo(req, datasetContext);
                }
            }

            if (req.getParameter("inline_edit") != null) {
                SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
                String newName = req.getParameter("value");
                String modelIdParam = req.getParameter("id");
                Long modelId = Long.valueOf(modelIdParam);
                JSONObject returnJSON = modelHelper.renameModel(datasetContext.getUser(),
                            modelId, newName);
                if (returnJSON.getString("messageType").equals("SUCCESS")) {
                    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
                    navHelper.initializeAll(datasetContext);
                    setInfo(req, datasetContext);
                }
                writeJSON(resp, returnJSON);
                return;
            }

            //if the cancel param is set, but no beans are listed we are all sorts of annoyed
            //and have to assume the models were modified.
            if (handleCancel(req, resp, datasetContext)) { return; }

            String destination = "DatasetInfo";
            String sourceParam = req.getParameter("source");
            if (sourceParam != null) {
                destination = sourceParam;
            }
            resp.sendRedirect(destination + "?datasetId=" + datasetContext.getDataset().getId());

        } catch (JSONException exception) {
            logger.error("JSONException:: " + exception.getMessage());
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) { out.close(); }
            logDebug("doPost end");
        }
    }

    /**
     * Helper function to handle the cancel request if no beans are ready to cancel.
     * @param req the {@link HttpServletRequest} to process.
     * @param resp the {@link HttpServletResponse}
     * @param datasetContext the {@link DatasetContext}
     * @return true of the request was handled, false otherwise.
     * @throws JSONException exception building the JSON return message.
     * @throws IOException ioException getting the out writer.
     */
    private boolean handleCancel(HttpServletRequest req, HttpServletResponse resp,
            DatasetContext datasetContext) throws JSONException, IOException {

        if (req.getParameter(MODEL_CANCEL_IMPORT_PARAM) == null) { return false; }
        writeJSON(resp, json("message", "No action taken on cancel."));
        UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                UserLogger.MODEL_IMPORT, "User canceled KCM Import.");
        return true;
    }

    /**
     * Handles uploading the file and inserting necessary information into the database.
     * @param datasetContext the current session context information.
     * @return JSONObject the return message as a JSONObject
     * @throws JSONException a JSON exception building the response message.
     */
    private JSONObject handleUpload(DatasetContext datasetContext) throws JSONException {

        JSONObject messageJSON = new JSONObject();
        try {

            boolean fileFound = false;
            for (FileItem uploadFileItem : datasetContext.getUploadItems()) {
                if (!uploadFileItem.isFormField() && uploadFileItem.getSize() > 0) {
                    fileFound = true;
                    String temporaryFileName = new Integer(new Random().nextInt()).toString();
                    File tmpFile = File.createTempFile(temporaryFileName, null);
                    tmpFile.deleteOnExit();
                    uploadFileItem.write(tmpFile);
                    datasetContext.getKCModelContext().setImportFile(tmpFile);
                }
            }

            //set the upload items to null so they don't get saved to the session
            //as we are done with them now.
            datasetContext.setUploadItems(null);

            if (!fileFound) {
                messageJSON.append("outcome", "ERROR");
                messageJSON.append("message", "Error occured trying to upload the file, "
                        + "no file found, please verify your file selection and try again.");
                return messageJSON;
            }

            messageJSON.append("outcome", "SUCCESS");

        } catch (IOException ioException) {
            logger.error("IO error occured trying save the model import file to a temp file.",
                    ioException);
            messageJSON.append("outcome", "ERROR");
            messageJSON.append("message", "Error occured trying to save the file to our servers, "
                    + "please contact the datashop team with this error");
        } catch (Exception exception) {
            logger.error("Exception occured trying to write the uploaded file to the temp file.",
                    exception);
            messageJSON.append("outcome", "ERROR");
            messageJSON.append("message", "Error occured trying to write the "
                    + "import file to our servers, "
                    + "please contact the datashop team with this error");
        }
        return messageJSON;
    }


    /**
     * Helper function that handles all the export parameters/requests.
     * @param req the HttpServletRequest.
     * @param resp the HttpServletResponse.
     * @param datasetContext The DatasetContext
     * @return boolean indicating this has handled an export parameter of some kind.
     * @throws IOException thrown from writing to the response out stream.
     * @throws JSONException thrown from creating an invalid JSON object.
     */
    private boolean handleImport(HttpServletRequest req, HttpServletResponse resp,
            DatasetContext datasetContext)
        throws IOException, JSONException {
        String importParam = req.getParameter(MODEL_IMPORT_PARAM);
        logDebug("handleImport:", importParam);
        KCModelContext kcModelContext = datasetContext.getKCModelContext();

        //check if we are creating a new thread
        if (MODEL_START_IMPORT.equals(importParam)) {
            DatasetItem datasetItem = datasetContext.getDataset();
            Integer datasetId = (Integer)datasetItem.getId();
            UserItem userItem = datasetContext.getUser();
            logDebug("Importing skill model for dataset(" + datasetId + ").");

            JSONObject modelsJSON = new JSONObject(req.getParameter("models"));
            File importFile = kcModelContext.getImportFile();

            KCModelImportControllerBean importControllerBean;
            synchronized (kcModelContext) {
                importControllerBean = HelperFactory.DEFAULT.getKCModelImportControllerBean();
                kcModelContext.setImportBean(importControllerBean);
                setInfo(req, datasetContext);
            }

            logger.info("handleImport: starting new thread for file: " + importFile.getName());
            importControllerBean.setAttributes(datasetItem, userItem, importFile, modelsJSON,
                                               getAggSpFilePath(), getBaseDir() + "/ssss");
            new Thread(importControllerBean).start();
            logger.info("handleImport: started new thread for file: " + importFile.getName());
            writeString(resp, "Thread Started");
            return true;
        } else if (MODEL_CHECK_IMPORT.equals(importParam)) {
            logDebug("Checking progress.");

            KCModelImportControllerBean importControllerBean;
            synchronized (kcModelContext) {
                importControllerBean = kcModelContext.getImportBean();
            }
            if (importControllerBean == null) {
                logger.warn("KCModelImportControllerBean was null when checking percent.");
                writeJSON(resp, json("currentStatus", "error",
                        "message", "Unable to re-attach to import thread, bean was null.",
                        "percent", -1));
                return true;
            } else {
                KCModelImportBean importBean = importControllerBean.getImportBean();
                int percent = importBean.getPercent();
                JSONObject json = new JSONObject();
                if (percent < 0) {
                    json = importBean.getErrorMessage();
                    json.put("currentStatus", "error");
                } else {
                    json = json("currentStatus", "ok",
                            "totalRows", importBean.getNumCompletedRows(),
                            "numRowsNotImported", importBean.getNumRowsNotImported());
                }
                json.put("percent", percent);
                writeJSON(resp, json);
                return true;
            }

        } else if (req.getParameter(MODEL_CANCEL_IMPORT_PARAM) != null) {
            logDebug("Canceling import.");

            KCModelImportControllerBean importControllerBean;
            synchronized (kcModelContext) {
                importControllerBean = kcModelContext.getImportBean();

                if (importControllerBean == null || !importControllerBean.isRunning()) {
                    return false;
                } else {
                    importControllerBean.stop();
                }
                kcModelContext.setImportBean(null);
                setInfo(req, datasetContext);
            }

            writeJSON(resp, json("message", "Stopped the import process successfully."));
            return true;
        }
        return false;
    }

    /**
     * Checks the status of an aggregation for new skill models.
     * @param req the {@link HttpServletRequest} to process.
     * @param resp the {@link HttpServletResponse}
     * @param datasetContext the {@link DatasetContext}
     * @return a string of response for the updated sample.
     * @throws JSONException exception building the JSON return message.
     * @throws IOException ioException getting the out writer.
     */
    private Boolean checkAggregationAndLFAAndCVProgress(HttpServletRequest req,
            HttpServletResponse resp, DatasetContext datasetContext)
            throws JSONException, IOException {

        String lfaCheckParam = req.getParameter("check_lfa_and_progress");
        if (lfaCheckParam != null) {
            KCModelContext kcContext = datasetContext.getKCModelContext();
            KCModelImportControllerBean importControllerBean;
            synchronized (kcContext) {
                importControllerBean = kcContext.getImportBean();
            }
            KCModelAggregatorBean bean = importControllerBean.getAggregatorBean();
            if (bean == null) {
                logger.warn("Aggregator Model Bean was null when checking status.");
                return true;
            } else {
                if (bean.getLFAStatus() == STATUS_FINISHED
                        && bean.getAggStatus() == STATUS_FINISHED
                        && bean.getCvStatus() == STATUS_FINISHED) {
                    kcContext.setAggregatorBean(null);
                    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
                    //update the navigation with any changes
                    navHelper.initializeAll(datasetContext);
                    setInfo(req, datasetContext);
                }
                writeJSON(resp, json("lfaStatus", bean.getLFAStatus(),
                        "aggStatus", bean.getAggStatus(), "aggPercent",
                        bean.getPercent(), "cvStatus", bean.getCvStatus()));
                return true;
            }
        }
        return false;
    }

    /**
     * Helper class for handling the KCM export calls and responses.
     */
    class KCModelExportHandler extends AbstractExportHandler {

        /**
         * Default Constructor.
         * @param req {@link HttpServletRequest}
         * @param resp {@link HttpServletResponse}
         * @param datasetContext {@link DatasetContext}
         */
        public KCModelExportHandler(HttpServletRequest req, HttpServletResponse resp,
                DatasetContext datasetContext) {
            super(req, resp, datasetContext, UserLogger.MODEL_EXPORT);
        }

        /** {@inheritDoc} */
        public AbstractExportBean createExportBean() {
            DatasetContext datasetContext = getDatasetContext();
            KCModelExportBean exportBean = HelperFactory.DEFAULT.getKCModelExportBean();

            JSONArray modelJSON = null;
            try {
                modelJSON = new JSONArray(getReq().getParameter("models"));
            } catch (JSONException exception) {
                logger.error("Caught JSON exception trying to create new JSON array.", exception);
            }

            List <SkillModelItem> skillModelList = modelHelper.getModelList(modelJSON);

            exportBean.setAttributes(datasetContext.getDataset(), skillModelList,
                    datasetContext.getUser());
            return exportBean;
        }

        /** {@inheritDoc} */
        public AbstractExportBean getExportBean() {
            return getExportContext().getKCModelExportBean();
        }

        /** {@inheritDoc} */
        public void setExportBean(AbstractExportBean bean) {
            getExportContext().setKCModelExportBean((KCModelExportBean)bean);
        }

        /** The type of export file to be used in the actual file name. */
        private static final String KCM_TYPE = "kcm";

        /**
         * Get the string to include for the type of export in the export file name.
         * Include the KC model database id if one and only one KC model selected for export.
         * @param bean the bean
         * @return a string of the type in the file name
         */
        public String getExportFileNameType(AbstractExportBean bean) {
            KCModelExportBean kcBean = (KCModelExportBean)bean;
            List<SkillModelItem> skillModelItemList = kcBean.getSkillModelList();
            String fileType = KCM_TYPE;
            if (skillModelItemList.size() == 1) {
                fileType += skillModelItemList.get(0).getId();
            }
            return fileType;
        }
    }
}
