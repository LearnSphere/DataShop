/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet.sampletodataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.FilterDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportBean;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportHandler;
import edu.cmu.pslc.datashop.servlet.export.ExportContext;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TxExportBean;
import edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetDto;
import edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetHelper;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.util.FileUtils;

/**
 * Handle the Save as Dataset functionality of the Sample to Dataset feature.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleToDatasetServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** The JSP name for My Datasets. */
    private static final String MY_DATASETS_JSP_NAME = "/index.jsp?datasets=mine";

    /** The JSP name for managing all samples. */
    private static final String SAMPLE_TO_DATASET_JSP_NAME = "/sample_to_dataset.jsp";

    /** The ImportQueue JSP file name. */
    private static final String IQ_JSP_NAME = "/jsp_dataset/import_queue.jsp";

    /** Title for the Samples page - "Samples". */
    public static final String SERVLET_LABEL = "Save Sample as Dataset";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "SampleToDataset";

    /** Sample to Dataset context attribute handle. */
    public static final String SAMPLE_TO_DATASET_CONTEXT_ATTRIB = "sampleToDatasetContext";
    /** Sample id request attribute. */
    private static final String SAMPLE_ID_ATTRIB = "s2d_sample";
    /** If this attribute exists, then save the sample as a new dataset. */
    private static final String SAVE_ACTION_ATTRIB = "s2d_save_action";
    /** The new dataset's name. */
    private static final String DATASET_NAME_ATTRIB = "s2d_ds_name";
    /** The new dataset's description. */
    private static final String DATASET_DESC_ATTRIB = "s2d_ds_desc";
    /** If this attribute exists, then handle AJAX requests. */
    private static final String AJAX_REQUEST_ATTRIB = "ajaxRequest";

    /**
     * Handles the HTTP get.
     *
     * @see javax.servlet.http.HttpServlet
     * @param req
     *            HttpServletRequest.
     * @param resp
     *            HttpServletResponse.
     * @throws IOException
     *             an input output exception
     * @throws ServletException
     *             an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     *
     * @see javax.servlet.http.HttpServlet
     * @param req
     *            HttpServletRequest.
     * @param resp
     *            HttpServletResponse.
     * @throws IOException
     *             an input output exception
     * @throws ServletException
     *             an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));

        /** Get the http session. */
        HttpSession httpSession = req.getSession(true);
        /** The Hibernate Session wrapped helper. */

        try {
            setEncoding(req, resp);

            Integer datasetId = null;
            if (req.getParameter("datasetId").matches("[0-9]+")) {
                    datasetId = Integer.parseInt(req.getParameter("datasetId"));
            }

            // Set the most recent servlet name for the help page.
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // The sample Dao
            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();

            // Get user id if logged in or get public user id, '%'.
            UserItem userItem = getLoggedInUserItem(req);

            // Get sample item.
            SampleItem sampleItem = null;
            if (req.getParameter(SAMPLE_ID_ATTRIB) != null
                && req.getParameter(SAMPLE_ID_ATTRIB).matches("[0-9]+")) {
                Integer sampleId = Integer.parseInt(req.getParameter(SAMPLE_ID_ATTRIB));
                if (sampleId != null) {
                    sampleItem = sampleDao.get(sampleId);
                }
            }
            // Ajax requests are also handled by this server.
            String ajaxRequest = req.getParameter(AJAX_REQUEST_ATTRIB);

            // Create a dataset using the info in the sample_to_dataset_form.
            String saveAsDataset = req.getParameter(SAVE_ACTION_ATTRIB);
            String newDatasetName = req.getParameter(DATASET_NAME_ATTRIB);
            String newDatasetDesc = req.getParameter(DATASET_DESC_ATTRIB);

            if (userItem != null
                    && !userItem.getId().equals(UserItem.DEFAULT_USER)
                    && !userItem.getId().equals(UserItem.SYSTEM_USER)
                    && sampleItem != null && sampleItem.getDataset() != null) {
                String userId = (String) userItem.getId();
                RequestDispatcher disp = null;

                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                AuthorizationDao authDao = DaoFactory.DEFAULT
                        .getAuthorizationDao();
                UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();

                DatasetItem datasetItem = dsDao.get((Integer) sampleItem
                        .getDataset().getId());


                    UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                    // MCK Set the most recent servlet name for the help page.
                    setRecentReport(req.getSession(true), SERVLET_NAME);

                    // Is the sample global?
                    Boolean isSampleGlobal = sampleItem.getGlobalFlag() == null ? false
                            : sampleItem.getGlobalFlag();

                    UserItem owner = userDao.get((String) sampleItem.getOwner().getId());
                    Boolean hasSamplePerms = isSampleGlobal
                            || userItem.equals(owner);
                    ProjectItem projectItem = null;
                    // Is the project public?
                    Integer projectId = (Integer) datasetItem.getProject()
                            .getId();
                    String projectName = new String();

                    Boolean isPublicProject = false;
                    if (projectId != null) {
                        projectItem = projectDao.get(projectId);
                        if (projectItem != null) {
                            projectName = projectItem.getProjectName();
                            isPublicProject = authDao.isPublic(projectId);
                        }
                    }

                    // Get auth level for user and project via dataset id.
                    String authLevel = authDao.getAuthLevel(userItem,
                            datasetItem);

                    Boolean hasDatasetPerms =
                            authLevel != null
                            && (authLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                                || isPublicProject);

                    Boolean hasDsEditRole = userRoleDao
                            .hasDatashopEditRole(userItem);

                    // Get txHelper for caching out of date tx export files
                    TransactionExportHelper txHelper = HelperFactory.DEFAULT
                            .getTransactionExportHelper();

                    // Load dataset context
                    DatasetContext datasetContext = (DatasetContext)
                        httpSession.getAttribute("datasetContext_" + datasetItem.getId());

                    // If needed, re-cache the tx export before using
                    // it to create a new dataset from the sample
                    ExportContext exportContext = datasetContext.getExportContext();
                    TxExportBean txExportBean = exportContext.getTxnExportBean();
                    if (txExportBean != null) {
                        logger.debug("TxExportBean = { "
                                + "isCompleted: " + txExportBean.isCompleted() + ", "
                                + "isCachedFileAvailable: " + txExportBean.isCachedFileAvailable() + ", "
                                + "isHasError: " + txExportBean.isHasError() + ", "
                                + "isExported: " + txExportBean.isExported() + ", "
                                + "isInitializing: " + txExportBean.isInitializing() + ", "
                                + "isWaiting: " + txExportBean.isWaiting() + " }");
                    }

                    if (saveAsDataset != null && saveAsDataset.equals("true")) {
                        // Meet the criteria that the dataset is released and belongs to a project.
                        if (datasetItem != null
                                && datasetItem.getReleasedFlag() == null || !datasetItem.getReleasedFlag()) {
                            logDebug("Dataset not released.");
                            writeJSON(resp,
                                    json("status", "error",
                                        "message", "Cannot save dataset. Please release the dataset to the project."));
                            return;
                        } else if (datasetItem != null && datasetItem.getProject() == null) {
                            logDebug("Cannot save dataset. Dataset does not belong to a project.");
                            writeJSON(resp,
                                    json("status", "error",
                                        "message", "The dataset has not been released. Please release the dataset to the project."));
                        }
                    }

                    // Handle transaction export bean in case the sample Tx export is out of date.
                    if (req.getParameter("export_start") != null
                        || req.getParameter("export_check") != null) {

                        if (txExportBean != null && txExportBean.isRunning()) {
                            logDebug("TxExportBean is running.");
                            writeJSON(resp,
                                json("status", txExportBean.getPercent(),
                                        "message", "Updating cached transaction export."));
                            return;

                        } else {
                            logDebug("TxExportBean is not running or is null.");
                            Boolean responseSent =
                                testTxExport(datasetItem, sampleItem,
                                    txHelper, datasetContext, userId, req, resp);

                            txExportBean = exportContext.getTxnExportBean();

                            if (txExportBean != null && txExportBean.isCachedFileAvailable()
                                    || responseSent == false) {

                                if (req.getParameter("requestingMethod") != null
                                        && req.getParameter("requestingMethod")
                                                .equals("ProgressBar.update")) {
                                    logDebug("Cached tx export is caching.");
                                    writeJSON(
                                            resp,
                                            json("status",
                                                    txExportBean.getPercent(),
                                                    "message",
                                                    "Updating cached transaction export."));
                                    return;
                                }

                                logDebug("TxExportBean has CachedFileAvailable. Saving dataset.");
                                saveAsDataset = "true";
                            } else if (txExportBean == null
                                || (txExportBean != null && !txExportBean.isCachedFileAvailable())) {
                                logDebug("TxExportBean is starting.");
                                writeJSON(resp,
                                        json("exportStarted", "exportStarted"));
                                return;
                            }
                        }

                        datasetContext.setExportContext(exportContext);
                    }

                    // Now, determine whether we display the form or save the
                    // sample as a new dataset.
                    Boolean attemptSave = false;
                    Boolean badDatasetName = false;
                    if (saveAsDataset != null && saveAsDataset.equals("true")) {

                        logger.debug("Saving sample as dataset.");
                        attemptSave = true;
                        disp = getServletContext().getRequestDispatcher(IQ_JSP_NAME);

                        if (newDatasetName != null && !checkDatasetName(dsDao, newDatasetName, resp)) {
                            // The dataset name exists in the ds_dataset table or import queue.
                            badDatasetName = true;
                        }
                        if (!badDatasetName) {
                            if (datasetContext.getDataset() != null) {
                            logger.info(getInfoPrefix(datasetContext) + " dataset "
                                    + datasetContext.getDataset().getId());
                            }
                            // Get the cached tx export's file path.
                            String filePath = txHelper.getCachedFileName(
                                    sampleItem, getBaseDir());

                            String includeKcmString = req.getParameter("includeKCMs");
                            Boolean includeKCMs = true;
                            if (includeKcmString != null && !Boolean.valueOf(includeKcmString)) {
                                // Then get the cached export without user-created KCMs
                                includeKCMs = false;
                            }

                            // The file path should not be
                            // null unless the dataset has not been aggregated.
                            if (filePath != null && projectItem != null) {

                                logger.debug("Tx export found: " + filePath);
                                if ((hasDatasetPerms && hasSamplePerms && hasDsEditRole)
                                    || userItem.getAdminFlag()) {

                                    UploadDatasetHelper uploadHelper = HelperFactory.DEFAULT.getUploadDatasetHelper();
                                    logger.info("Copying tx export to user uploads.");
                                    FileDao fileDao = DaoFactory.DEFAULT.getFileDao();

                                    Integer lastIndexOfSlash = filePath.lastIndexOf('/');
                                    String uploadFileName = filePath.substring(lastIndexOfSlash + 1);
                                    String pathPrefix = getBaseDir() + File.separator + UploadDatasetHelper.SUB_PATH
                                            + File.separator;
                                    File uploadFile = new File(pathPrefix + uploadFileName);

                                    // We will choose to omit or keep KC columns during the FFI
                                    // instead of here because we do not want a bottleneck in disk I/O
                                    FileUtils.copyFile(new File(filePath), uploadFile);
                                    FileUtils.applyDataShopPermissions(Paths.get(pathPrefix + uploadFileName));

                                    FileItem fileItem = new FileItem();
                                    fileItem.setFileName(uploadFile.getName());
                                    fileItem.setFilePath(uploadFile.getParent());
                                    fileItem.setAddedTime(new Date());
                                    fileItem.setFileType("text/plain");
                                    fileItem.setFileSize(0L);   // size can't be determined yet
                                    fileItem.setOwner(userItem);
                                    fileDao.saveOrUpdate(fileItem);

                                    logger.info("Created new file item: " + fileItem.getFilePath()
                                        + "/" + fileItem.getFileName());
                                    UploadDatasetDto uploadDatasetDto =
                                        uploadHelper.getUploadDatasetDto(userItem, fileItem, datasetItem, sampleItem,
                                            newDatasetName, newDatasetDesc);
                                    uploadDatasetDto.setFromExistingFlag(true);

                                    logger.info("Checking uploadDatasetDto for errors.");
                                    uploadHelper.checkForErrors(uploadDatasetDto);

                                    if (!uploadDatasetDto.hasErrors()) {
                                        logger.info("No errors found in uploadDatasetDto.");
                                        logger.info("Creating new Import Queue item.");
                                        ImportQueueItem iqItem =
                                            uploadHelper.createImportQueueItem(uploadDatasetDto, userItem,
                                                new Date(), "pending", getBaseDir(), getNumFFIVerifyLines(),
                                                    includeKCMs);

                                        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();

                                        // Get the IQ item if it was created before
                                        if (iqItem == null) {
                                            iqItem = iqDao.get(uploadDatasetDto.getImportQueueItemId());
                                        }
                                        iqItem.setStatus(ImportQueueItem.STATUS_QUEUED);

                                        // set queue order
                                        Integer queueOrder = iqDao.getMaxQueueOrder();
                                        iqItem.setQueueOrder(queueOrder + 1);

                                        // Dataset notes info string
                                        UserItem projectPi = projectItem.getPrimaryInvestigator();
                                        UserItem projectDp = projectItem.getDataProvider();
                                        String piString = SamplesHelper.getFormattedUserName(projectPi, true) == null
                                            ? "" : SamplesHelper.getFormattedUserName(projectPi, true);
                                        String dpString = SamplesHelper.getFormattedUserName(projectDp, true) == null
                                                ? "" : SamplesHelper.getFormattedUserName(projectDp, true);
                                        String notesString = "Dataset created from dataset \""
                                                + datasetItem.getDatasetName() + " [" + datasetItem.getId()
                                                + "]\" and sample \""
                                                + sampleItem.getSampleName() + " [" + sampleItem.getId()
                                                + "]\" where "
                                                + "PI = \"" + piString + "\", "
                                                + "DP = \"" + dpString + "\"";
                                        iqItem.setAdtlDatasetNotes(notesString);
                                        iqItem.setSrcDatasetId((Integer) datasetItem.getId());
                                        iqItem.setSrcDatasetName((String) datasetItem.getDatasetName());
                                        iqItem.setSrcSampleId((Integer) sampleItem.getId());
                                        iqItem.setSrcSampleName((String) sampleItem.getSampleName());
                                        logDebug("Saving Import Queue Item: " + iqItem);
                                        // save new import queue item
                                        iqDao.saveOrUpdate(iqItem);

                                        httpSession.setAttribute(UploadDatasetHelper.ATTRIB_IQ_ADDED,
                                            UploadDatasetHelper.MSG_IQ_ADDED);
                                        String redirectTo = "index.jsp?datasets=mine";
                                        logger.info("Redirecting to " + redirectTo);

                                        writeJSON(resp,
                                                json("redirectTo", redirectTo));
                                        return;

                                    } else {
                                        logger.info("Saved dataset has errors: "
                                            + datasetItem.getDatasetName()
                                                + " (" + datasetItem.getId() + ")");
                                    }
                                }
                            } else {
                                writeJSON(resp,
                                        json("status", "error",
                                            "message", "The dataset has not yet been aggregated. "
                                                + "Please wait until log conversion finishes."));
                                return;
                            }
                        }
                    }

                    // Show Save as Dataset form.
                    if (ajaxRequest == null
                            && (!attemptSave || badDatasetName)) {
                        logDebug("Showing save dataset form.");
                        // Show the Save Sample as Dataset form
                        disp = getServletContext().getRequestDispatcher(
                                SAMPLE_TO_DATASET_JSP_NAME);
                        // Log when the access request page is viewed
                        UserLogger.log(null, userItem,
                                UserLogger.VIEW_SAMPLE_TO_DATASET_PAGE, "", false);
                        logger.info("Going to JSP: " + SAMPLE_TO_DATASET_JSP_NAME);

                        // Populate form if DsEdit or DsAdmin.
                        if ((hasDatasetPerms && hasSamplePerms && hasDsEditRole)
                            || (userItem.getAdminFlag())) {
                            logDebug("Retrieving sample to dataset table info.");
                            req.setAttribute(
                                    "tableList",
                                    populateSaveAsDatasetInfo(projectName, datasetItem,
                                            sampleItem, userId));
                        }

                        // Put the sample_id directly into the save sample as dataset form.
                        String hiddenSampleId = "<input type=\"hidden\" id=\"s2d_sample\" value=\""
                            + ((Integer)sampleItem.getId()) + "\" />";
                        req.setAttribute(SAMPLE_ID_ATTRIB, hiddenSampleId);

                    // Handle AJAX requests.
                    } else if (ajaxRequest != null) {
                        logDebug("Handling AJAX request.");
                        handleAjaxRequests(req, resp);
                        return;
                    }





                disp.forward(req, resp);
                return;
            }

            // If the sample or user items are bad and a dataset id exists,
            // redirect the user to the DatasetInfo page for the dataset.
            // It will handle how they can view the dataset.
            if (datasetId != null && datasetId > 0) {
                resp.sendRedirect("DatasetInfo?datasetId=" + datasetId);
            }


        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {

            logDebug("doPost end");
        }
    }


    /** If the dataset name is invalid or already exists, return false.
     * @param dsDao the DatasetDao
     * @param newDatasetName the new dataset name
     * @param resp the HttpServletResponse
     * @return false if the name is invalid or already exists
     * @throws JSONException the JSONException
     * @throws IOException the IOException
     */
    private Boolean checkDatasetName(DatasetDao dsDao, String newDatasetName,
            HttpServletResponse resp) throws IOException, JSONException {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();

        Boolean passed = true;
        // This dataset name is both valid and new. Allow it.
        if (newDatasetName.trim().isEmpty()) {
            passed = false;
            writeJSON(resp,
                    json("invalidName", "true"));
        } else {

            List datasetList = dsDao.find(newDatasetName);

            if (datasetList != null && !datasetList.isEmpty()) {
                passed = false;
                writeJSON(resp,
                    json("datasetNameExists", "true"));
            } else {
                List<ImportQueueItem> importQueueList = iqDao.find(newDatasetName);
                if (importQueueList != null && !importQueueList.isEmpty()) {
                passed = false;
                writeJSON(resp,
                    json("datasetNameExists", "true"));
                }
            }
        }

        return passed;
    }

    /**
     * Whether or not a call to processRequest is acknowledged.
     * @param datasetItem the dataset item
     * @param sampleItem the sample item
     * @param txHelper the transaction helper
     * @param datasetContext the dataset context
     * @param userId the user id
     * @param req the servlet request
     * @param resp the servlet response
     * @return whether or not a call to processRequest is acknowledged
     * @throws IOException the IO exception
     */
    private Boolean testTxExport(DatasetItem datasetItem, SampleItem sampleItem,
            TransactionExportHelper txHelper, DatasetContext datasetContext, String userId,
            HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ExportContext exportContext = datasetContext.getExportContext();
        Boolean responseSent = false;
        long transactionCount = 0;
        // List for convenience- contains at most one sample item.
        List<SampleItem> samplesQueuedToCacheTx = null;
        samplesQueuedToCacheTx = txHelper
                .getSamplesQueuedToCacheTx(datasetItem);
        if (samplesQueuedToCacheTx.contains(sampleItem)) {
            logDebug("Samples returned to transaction cache :: ", samplesQueuedToCacheTx.size());
            samplesQueuedToCacheTx.clear();
            samplesQueuedToCacheTx.add(sampleItem);
        } else {
            samplesQueuedToCacheTx.clear();
            return responseSent;
        }

        // If the sample is not cached, then do so.
        if (samplesQueuedToCacheTx != null && samplesQueuedToCacheTx.size() == 1) {
            logger.info("Attempting to recache transaction export.");
            transactionCount = DaoFactory.DEFAULT
                    .getTransactionDao().count(datasetItem);
            if (transactionCount > 0) {
                logger.info("Creating cached export for "
                        + txHelper.formatForLogging(datasetItem));
            } else {
                logger.info("Transaction count is zero for "
                        + txHelper.formatForLogging(datasetItem));
            }
            // Since other exports may occur at the same time,
            // we will defer control to the Transaction Export bean.
            synchronized (datasetContext.getExportContext()) {
                if (req.getParameter("export_check") != null) {
                    logInfo("Checking status of transaction export request for Sample to Dataset.");
                    req.setAttribute("export_check", "");
                } else {
                    logInfo("Processing transaction export request for Sample to Dataset.");
                    req.setAttribute("export_start", "");
                }
                TransactionCacheHandler txCache = new TransactionCacheHandler(
                    req, resp, samplesQueuedToCacheTx, datasetContext, getBaseDir());
                // Start the process request but do not download build or download the zip.
                Boolean downloadExport = false;
                responseSent =  txCache.processRequest("application/zip; charset=UTF-8", "zip", downloadExport);
                if (txCache.getExportBean() != null) {
                    exportContext.setTxnExportBean((TxExportBean) txCache.getExportBean());
                }
            }
        }

        return responseSent;
    }

    /**
     * Handle AJAX requests.
     * @param req the HttpServletRequest
     */
    private void handleAjaxRequests(HttpServletRequest req, HttpServletResponse resp) {

            // The field and value params are artifacts of the InlineEditor
            // used for the datashop description.
            String field = req.getParameter("field");
            String value = req.getParameter("value");

            // This param is to check if the dataset name exists while the user types it.
            // They may idle on the page before clicking submit so another check occurs later,
            // before the actual dataset creation.
            String dsName = req.getParameter("datasetName");

            try {
                // The requestingMethod determines how to respond.
                // InlineEditor has no requestingMethod.. so the "field" param is tested, instead.
                String requestingMethod = req.getParameter("requestingMethod");

                // InlineEditor saves new dataset description for the page.
                // Note: Handling the dataset description here
                // reduces javascript code since the description uses a custom
                // text area, capable of resizing and holding 65k+ chars.
                if (field != null && field.equals("dataset-desc")) {
                    logger.debug("Updating dataset description: " + value);
                    writeJSON(resp,
                        json("messageType", "SUCCESS",
                            "message", "Updated dataset description.",
                             "value", value));
                    return;

                // Standard AJAX requests (everything but the InlineEditor).

                // Check if the dataset name exists as the user types it.
                } else if (requestingMethod != null && requestingMethod.equals("checkDatasetNameExists")) {
                    // The dataset name is not null or empty.
                    DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                    if (checkDatasetName(dsDao, dsName, resp)) {
                        writeJSON(resp, json("datasetNameExists", "false"));
                    }
                    return;
                }
            } catch (IOException e) {
                logger.error("updateSaveDatasetForm:: "
                    + "IOException occurred while attempting to write a response.", e);
            } catch (JSONException e) {
                logger.error("updateSaveDatasetForm:: "
                    + "JSONException occurred while attempting to write a response.", e);
            }
            return;

    }

    /**
     * Populate Save Sample to Dataset info table.
     * @param projectName the project name
     * @param datasetItem the dataset item
     * @param sampleItem the sample item
     * @param userId the user id string
     * @return a string list of info values to use in the html table
     */
    private List<String> populateSaveAsDatasetInfo(String projectName,
            DatasetItem datasetItem, SampleItem sampleItem, String userId) {
        SamplesHelper samplesHelper = HelperFactory.DEFAULT.getSamplesHelper();
        SampleMetricDao smDao = DaoFactory.DEFAULT.getSampleMetricDao();
        TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
        FilterDao filterDao = DaoFactory.DEFAULT.getFilterDao();

        List<String> tableList = new ArrayList<String>();

        // Combine all info from the SampleDto and the FilterDao into a single list.
        SampleRowDto sampleRow = new SampleRowDto();
        sampleRow.setSampleId((Integer) sampleItem.getId());
        sampleRow.setDescription(sampleItem.getDescription());
        samplesHelper.addMetricInfo(sampleItem, sampleRow, smDao, txDao);

        tableList.add(projectName);
        // The dataset name is "Dataset name - Sample name". The javascript will alert
        // the user if this name is already taken and provide responsive feedback as they
        // change it.
        tableList.add(datasetItem.getDatasetName() + " - " + sampleItem.getSampleName());
        // The default description is also the string that goes into the dataset's Notes.
        // The description, however, doesn't contain the PI and DP (at the time of creation)
        // as is the case with the Notes section.
        String descString = "Dataset created from dataset <strong><i>\""
                + datasetItem.getDatasetName() + " [" + datasetItem.getId()
                + "]\"</strong></i> and sample <strong><i>\""
                + sampleItem.getSampleName() + " [" + sampleItem.getId()
                + "]\"</strong></i>";
        tableList.add(descString);
        // Metric info
        // If not aggregated, then NumSteps and NumUniqueSteps are -1.
        tableList.add(sampleRow.getNumTransactions() == null
                || sampleRow.getNumTransactions() < 0 ? ""
                : sampleRow.getNumTransactions().toString());
        tableList.add(sampleRow.getNumStudents() == null
                || sampleRow.getNumStudents() < 0 ? "" : sampleRow
                .getNumStudents().toString());
        tableList.add(sampleRow.getNumProblems() == null
                || sampleRow.getNumProblems() < 0 ? "" : sampleRow
                .getNumProblems().toString());
        // NumSteps returns -1 if not available.
        tableList.add(sampleRow.getNumSteps() == null
                || sampleRow.getNumSteps() < 0 ? "" : sampleRow
                .getNumSteps().toString());
        // NumUniqueSteps returns -1 if not available.
        tableList.add(sampleRow.getNumUniqueSteps() == null
                || sampleRow.getNumUniqueSteps() < 0 ? "" : sampleRow
                .getNumUniqueSteps().toString());

        // Filter list.
        List<FilterItem> filterList = filterDao.find(sampleItem);
        StringBuffer filterString = new StringBuffer();
        for (FilterItem filter : filterList) {
            if (filter.getFilterString() != null && !filter.getFilterString().isEmpty()) {
                filter.getId();
                String attribute = FilterItem.FilterConditions.get(filter.getAttribute());
                if (attribute == null) {
                    attribute = filter.getAttribute();
                }
                filterString.append(attribute + " "
                    + filter.getOperator() + " " + filter.getFilterString()
                    + "<br/>");
            }
        }

        tableList.add(filterString.toString());

        // Return info table values.
        return tableList;
    }

    /**
     * Returns the file path of the transaction export stored procedure.
     * @return the file path
     */
    public String getTxExportSpFilePath() {
        return getServletContext().getInitParameter("tx_export_sp_filepath");
    }

    /**
     * Helper class for handling the transaction export calls and responses
     * for the Sample to Dataset feature.
     */
    class TransactionCacheHandler extends AbstractExportHandler {

        /** Base directory where cached transaction export files belong. */
        private String baseDir;

        /** The HttpServletRequest. */
        private HttpServletRequest req = null;

        /** Sample items. */
        private List<SampleItem> sampleItems;
        /**
         * Default Constructor.
         * @param req {@link HttpServletRequest}
         * @param resp {@link HttpServletResponse}
         * @param samplesQueuedToCacheTx
         * @param datasetContext {@link DatasetContext}
         * @param baseDir base directory for file storage.
         */
        public TransactionCacheHandler(HttpServletRequest req, HttpServletResponse resp,
                List<SampleItem> samplesQueuedToCacheTx, DatasetContext datasetContext, String baseDir) {
            super(req, resp, datasetContext, UserLogger.EXPORT_TRANSACTIONS);
            this.baseDir = baseDir;
            this.req = req;
            sampleItems = new ArrayList<SampleItem>();
            if (samplesQueuedToCacheTx != null && !samplesQueuedToCacheTx.isEmpty()) {
                // The sample to be cached.
                sampleItems.add(samplesQueuedToCacheTx.get(0));
            }

        }

        /** {@inheritDoc} */
        public AbstractExportBean createExportBean() {

            DatasetContext datasetContext = getDatasetContext();
            TxExportBean txExportBean = HelperFactory.DEFAULT.getTxExportBean();
            if (sampleItems != null && !sampleItems.isEmpty()) {
                txExportBean.setAttributes(sampleItems,
                        datasetContext.getDataset(), baseDir, datasetContext.getUserId(),
                        getTxExportSpFilePath());
                txExportBean.setSendEmail(isSendmailActive());
                txExportBean.setEmailAddress(getEmailAddressDatashopHelp());
                txExportBean.setDownloadExport(false);
            } else {
                logger.debug("Sample tx export already cached.");
            }
            return txExportBean;
        }

        /** {@inheritDoc} */
        public AbstractExportBean getExportBean() {
            return getExportContext().getTxnExportBean();
        }

        /** {@inheritDoc} */
        public void setExportBean(AbstractExportBean bean) {
            getExportContext().setTxnExportBean((TxExportBean)bean);
        }

        /** The type of export file to be used in the actual file name. */
        private static final String TX_TYPE = "tx";

        /**
         * Get the string to include for the type of export in the export file name.
         * @param bean the bean
         * @return a string of the type in the file name
         */
        public String getExportFileNameType(AbstractExportBean bean) {
            return TX_TYPE;
        }
    }
}
