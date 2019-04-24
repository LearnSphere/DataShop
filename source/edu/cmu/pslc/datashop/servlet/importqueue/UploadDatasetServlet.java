/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.dto.importqueue.ExistingProjectDto;
import edu.cmu.pslc.datashop.dto.importqueue.VerificationResults;
import edu.cmu.pslc.datashop.extractors.ffi.FlatFileImporter;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoEditHelper;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportServlet;
import edu.cmu.pslc.datashop.servlet.project.ProjectPageServlet;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.FileUtils;

/**
 * Handle uploading a dataset.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UploadDatasetServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP file name. */
    private static final String JSP_NAME_DESCRIBE = "/jsp_dataset/upload_dataset_describe.jsp";
    /** The JSP file name. */
    private static final String JSP_NAME_DISCOURSE_DESCRIBE =
        "/jsp_discoursedb/upload_discourse_describe.jsp";
    /** The JSP file name. */
    private static final String JSP_NAME_CHOOSE = "/jsp_dataset/upload_dataset_choose.jsp";
    /** The JSP file name. */
    private static final String JSP_NAME_DISCOURSE_CHOOSE =
        "/jsp_discoursedb/upload_discourse_choose.jsp";
    /** The JSP file name. */
    private static final String JSP_NAME_VERIFY = "/jsp_dataset/upload_dataset_verify.jsp";
    /** Option for nextPage. */
    private static final String MY_DATASETS = "mine";
    /** Option for nextPage. */
    private static final String DATASET_PAGE = "dataset";
    /** Option for nextPage. */
    private static final String PROJECT_PAGE = "project";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "UploadDataset";

    /** Request attribute name. */
    public static final String REQ_ATTRIB_SETTINGS = "form_settings";
    /** Request attribute name. */
    public static final String REQ_ATTRIB_PROJECTS = "existing_projects";
    /** Request attribute name. */
    public static final String REQ_ATTRIB_RECENT_NAMES = "recent_dataset_names";
    /** Request attribute name. */
    public static final String REQ_ATTRIB_RECENT_DESCS = "recent_dataset_descs";
    /** Request attribute name. */
    public static final String REQ_ATTRIB_DOMAIN_LEARNLAB_LIST = "domain_learnlab_list";
    /** Request attribute name. */
    public static final String REQ_ATTRIB_HAS_STUDY_DATA_LIST = "has_study_data_list";

    /** Parameter. */
    private static final String PARAM_FILE_NAME  = "fileName";

    /** Action parameter name. */
    private static final String ACTION_PARAM = "upload_dataset_action";
    /** Action parameter option. */
    private static final String ACTION_DESCRIBE = "describe";
    /** Action parameter option. */
    private static final String ACTION_CHOOSE = "choose";
    /** Action parameter option. */
    private static final String ACTION_VERIFY = "verify";
    /** Action parameter option. */
    private static final String ACTION_BACK = "back";

    /** Error message. */
    //This is in the requirements but I didn't find a place to use it, YET.
    //private static final String ERROR_MSG_WITH_TXS =
    //        "An error occurred while trying to add this new dataset to the import queue.";
    /** Error message. */
    private static final String ERROR_MSG_NO_TXS =
            "An error occurred while trying to create this new dataset.";
    /** Error message. */
    private static final String ERROR_MSG_FILE_SIZE_LIMIT_EXCEEDED =
            "File size limit (400MB) was exceeded.";

    /** Bad filename characters to replace with underscore. */
    public static final String BAD_FILEPATH_CHARS = "[:*?\"<>|\\s]+";

    /** Attribute name for errors related to lost data due to session inactivity. */
    private static final String SESSION_DATA_ERROR_ATTRIB = "session_data_error";

    /**
     * Handles the HTTP get.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
//CHECKSTYLE.OFF: MethodLength
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
//CHECKSTYLE.ON: MethodLength
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        HttpSession httpSession = req.getSession(true);
        try {
            setEncoding(req, resp);
            // Determine if user is authorized to view this page only if the
              // session_data_error attribute is null.
            if ((getLoggedInUserItem(req) == null) || !isUserAuthorized(req)
                    && req.getAttribute(SESSION_DATA_ERROR_ATTRIB) == null) {
                httpSession.setAttribute(ProjectServlet.OPEN_ROLE_REQUEST_ATTRIB,
                                          ProjectServlet.UPLOAD_DATASET_REDIRECT);
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
                disp.forward(req, resp);
                return;

            }

            UploadDatasetHelper uploadHelper = HelperFactory.DEFAULT.getUploadDatasetHelper();

            // Set the most recent servlet name for the help page
            setRecentReport(httpSession, SERVLET_NAME);

            // Where do we go next?
            String nextPage = JSP_NAME_DESCRIBE;

            // Get current user
            UserItem userItem = getUser(req);

            // DTO in the session
            boolean sessionDataErrorFlag = false;

            // Handle requests for DataShop-Edit role
            if (req.getParameter("requestRole") != null) {
                requestRole(req, resp, userItem);
                return;
            }
            String action;
            // The DTO is assumed to be in the session since the session_data_error
            // flag is null.
            if (req.getAttribute(SESSION_DATA_ERROR_ATTRIB) == null) {
                if (ServletFileUpload.isMultipartContent(req)) {
                    action = ACTION_CHOOSE;
                } else {
                    action = req.getParameter(ACTION_PARAM);
                }
                logDebug(ACTION_PARAM, " is ", action);
                if (action != null && action.equals(ACTION_VERIFY)) {
                    String buttonValue = req.getParameter("back_button");
                    logDebug("buttonValue", " is ", buttonValue);
                    buttonValue = req.getParameter("continue_button");
                    logDebug("continue_button", " is ", buttonValue);
                }

            } else {
                // The DTO is not in the session.
                sessionDataErrorFlag = true;
                action = null;
            }

            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();

            UploadDatasetDto uploadDatasetDto = null;

            if (action == null || action.equals(ACTION_DESCRIBE) || action.equals(ACTION_BACK)) {
                uploadDatasetDto = new UploadDatasetDto();

                // Get existing projects for the user who is currently logged in
                String userId = (String)userItem.getId();
                List<ExistingProjectDto> projectList = projectDao.getExistingProjects(userItem);
                int numExisting = projectList.size();

                ProjectItem projectItem = getProjectItem((String)req.getParameter("id"));
                if (projectItem == null) {
                    projectItem = getProjectItem((String)req.getParameter("projectId"));
                }

                Boolean isDiscourse = null;
                if (projectItem != null) {
                    isDiscourse = projectItem.getIsDiscourseDataset();
                }

                if (action == null) {
                    nextPage = JSP_NAME_DESCRIBE;
                    
                    if (projectItem != null) {
                        uploadDatasetDto.setProjectSelection(UploadDatasetDto.PROJ_CURRENT);
                        uploadDatasetDto.setExistingProjectId((Integer)projectItem.getId());
                        String projectName = projectItem.getProjectName();
                        uploadDatasetDto.setNewProjectName(projectName);
                        if ((isDiscourse != null) && isDiscourse) {
                            nextPage = JSP_NAME_DISCOURSE_DESCRIBE;
                        }
                    } else {
                        // Set default on the project selection radio box
                        if (numExisting > 0) {
                            uploadDatasetDto.setProjectSelection(UploadDatasetDto.PROJ_EXIST);
                        } else {
                            uploadDatasetDto.setProjectSelection(UploadDatasetDto.PROJ_LATER);
                        }
                    }

                    // If the DTO was not found
                    if (sessionDataErrorFlag) {
                        uploadDatasetDto.setErrorMessage("There was a problem with your upload."
                            + " Please try again.");
                        uploadDatasetDto.setSessionDataErrorFlag(true);
                    }

                } else if (action.equals(ACTION_DESCRIBE)) {
                    readFormDataDescribe(req, uploadDatasetDto, numExisting);
                    if (uploadHelper.checkForErrors(uploadDatasetDto)) {
                        nextPage = JSP_NAME_DESCRIBE;
                        if ((isDiscourse != null) && isDiscourse) {
                            nextPage = JSP_NAME_DISCOURSE_DESCRIBE;
                        }
                    } else {
                        if (uploadDatasetDto.getLoadDataNowFlag()) {
                            nextPage = JSP_NAME_CHOOSE;
                            if ((isDiscourse != null) && isDiscourse) {
                                nextPage = JSP_NAME_DISCOURSE_CHOOSE;
                            }
                        } else {
                            createFilesOnlyDataset(uploadDatasetDto, userItem);
                            if (uploadDatasetDto.getDatasetIdErrorFlag()) {
                                nextPage = JSP_NAME_DESCRIBE;
                            } else {
                                nextPage = DATASET_PAGE;
                            }
                        }
                    }
                } else if (action.equals(ACTION_BACK)) {
                    uploadDatasetDto = (UploadDatasetDto)
                            httpSession.getAttribute(REQ_ATTRIB_SETTINGS);
                    if (!dtoExists(req, resp, uploadDatasetDto)) {
                        return;
                    }
                    if ((isDiscourse != null) && isDiscourse) {
                        nextPage = JSP_NAME_DISCOURSE_CHOOSE;
                    } else {
                        nextPage = JSP_NAME_CHOOSE;
                    }
                }

                if (nextPage.equals(JSP_NAME_DESCRIBE) ||
                    nextPage.equals(JSP_NAME_DISCOURSE_DESCRIBE)) {
                    // Get recent dataset names and descriptions
                    ImportQueueDao importQueueDao = DaoFactory.DEFAULT.getImportQueueDao();
                    List<String> recentDatasetNames = importQueueDao.getRecentDatasetNames(userId);
                    List<String> recentDescriptions = importQueueDao.getRecentDescriptions(userId);
                    DatasetInfoEditHelper datasetInfoEditHelper =
                        HelperFactory.DEFAULT.getDatasetInfoEditHelper();
                    List<String> domainLearnlabList = datasetInfoEditHelper.getDomainLearnlabList();

                    req.setAttribute(REQ_ATTRIB_RECENT_NAMES, recentDatasetNames);
                    req.setAttribute(REQ_ATTRIB_RECENT_DESCS, recentDescriptions);
                    req.setAttribute(REQ_ATTRIB_PROJECTS, projectList);
                    req.setAttribute(REQ_ATTRIB_DOMAIN_LEARNLAB_LIST, domainLearnlabList);
                    req.setAttribute(REQ_ATTRIB_HAS_STUDY_DATA_LIST, DatasetItem.STUDY_FLAG_ENUM);
                }

            } else if (action.equals(ACTION_CHOOSE)) {
                uploadDatasetDto = (UploadDatasetDto) httpSession.getAttribute(REQ_ATTRIB_SETTINGS);
                if (!dtoExists(req, resp, uploadDatasetDto)) {
                    return;
                }
                handleUpload(req, uploadDatasetDto, userItem);
                uploadHelper.checkForErrors(uploadDatasetDto);
                if (!uploadDatasetDto.hasErrors()) {
                    //----- Tab-Delimited -----
                    if (uploadDatasetDto.getFormat().equals(ImportQueueItem.FORMAT_TAB)) {
                        ImportQueueItem iqItem = uploadHelper.createImportQueueItem(uploadDatasetDto,
                                userItem, new Date(), ImportQueueItem.STATUS_PENDING,
                                getBaseDir(), getNumFFIVerifyLines(), true);
                        // Creating file for mini-verify may cause errors.
                        if (uploadDatasetDto.getDataFileErrorFlag()) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("uploadDatasetDto.hasErrors: "
                                             + uploadDatasetDto.printErrors());
                            }
                            nextPage = JSP_NAME_CHOOSE;
                        } else {
                            // Go ahead and verify.
                            Integer statusId = runFfi(uploadDatasetDto);
                            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
                            iqItem.setImportStatusId(statusId);
                            VerificationResults results = uploadDatasetDto.getResults();
                            if (results.getStatus().equals(ImportQueueItem.STATUS_PASSED)) {
                                iqItem.setVerificationResults(
                                        ImportQueueHelper.FIRST_100_PASSED_MSG);
                            } else {
                                iqItem.setVerificationResults(results.generateHtml());
                            }
                            iqDao.saveOrUpdate(iqItem);
                            nextPage = JSP_NAME_VERIFY;
                        }
                    //----- XML or DiscourseDB -----
                    } else {
                        ImportQueueItem iqItem =
                            uploadHelper.createImportQueueItem(uploadDatasetDto,
                                                               userItem, new Date(),
                                                               ImportQueueItem.STATUS_QUEUED,
                                                               getBaseDir(),
                                                               getNumFFIVerifyLines(), false);
                        addItemToQueue(req, uploadDatasetDto, userItem, iqItem);
                        if (UploadDatasetDto.PROJ_LATER.equals(
                                uploadDatasetDto.getProjectSelection())) {
                            nextPage = MY_DATASETS;
                        } else {
                            nextPage = PROJECT_PAGE;
                        }
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("uploadDatasetDto.hasErrors: "
                            + uploadDatasetDto.printErrors());
                    }
                    nextPage = JSP_NAME_CHOOSE;
                }
            } else if (action.equals(ACTION_VERIFY)) {

                uploadDatasetDto = (UploadDatasetDto) httpSession.getAttribute(REQ_ATTRIB_SETTINGS);
                if (!dtoExists(req, resp, uploadDatasetDto)) {
                    return;
                }
                addItemToQueue(req, uploadDatasetDto, userItem, null);
                if (UploadDatasetDto.PROJ_LATER.equals(
                        uploadDatasetDto.getProjectSelection())) {
                    nextPage = MY_DATASETS;
                } else {
                    nextPage = PROJECT_PAGE;
                }
            }

            // Put the data in the HTTP session for the JSP.
            httpSession.setAttribute(REQ_ATTRIB_SETTINGS, uploadDatasetDto);
            UserLogger.log(userItem, UserLogger.VIEW_UPLOAD_DATASET, "", true);

            if (nextPage.equals(MY_DATASETS)) {
                httpSession.setAttribute(
                    UploadDatasetHelper.ATTRIB_IQ_ADDED, UploadDatasetHelper.MSG_IQ_ADDED);
                String redirectTo = ProjectServlet.REDIRECT_SERVLET_NAME + "?datasets=mine";
                logger.info("Redirecting to " + redirectTo);
                resp.sendRedirect(redirectTo);
                return;
            } else if (nextPage.equals(PROJECT_PAGE)) {
                String msg = UploadDatasetHelper.MSG_IQ_ADDED;
                // This case might be for the DiscourseDB project...
                ProjectItem projectItem = projectDao.get(uploadDatasetDto.getExistingProjectId());
                Boolean isDiscourse = projectItem.getIsDiscourseDataset();
                if ((isDiscourse != null) && isDiscourse) {
                    msg = msg.replace("Dataset", "Discourse");
                }
                httpSession.setAttribute(UploadDatasetHelper.ATTRIB_IQ_ADDED, msg);
                String redirectTo = ProjectPageServlet.SERVLET
                        + "?id=" + uploadDatasetDto.getExistingProjectId();
                logger.info("Redirecting to " + redirectTo);
                resp.sendRedirect(redirectTo);
                return;
            } else if (nextPage.equals(DATASET_PAGE)) {
                DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                Collection datasetList = datasetDao.find(uploadDatasetDto.getDatasetName());
                if (datasetList.size() > 0) {
                    DatasetItem datasetItem = (DatasetItem)(datasetList.toArray())[0];
                    Integer newDatasetId = (Integer)datasetItem.getId();

                    //Log to dataset user log table
                    String newDatasetName = datasetItem.getDatasetName();
                    String info = "Dataset '" + newDatasetName + "' (" + newDatasetId + ")";
                    UserLogger.log(datasetItem, userItem, UserLogger.DATASET_CREATE, info);

                    String redirectTo = DatasetInfoReportServlet.SERVLET
                            + "?datasetId=" + newDatasetId;
                    logger.info("Redirecting to " + redirectTo);
                    resp.sendRedirect(redirectTo);
                    return;
                } else {
                    uploadDatasetDto.setErrorMessage(ERROR_MSG_NO_TXS);
                    req.setAttribute(REQ_ATTRIB_SETTINGS, uploadDatasetDto);
                    logger.error("Dataset was not created/found: "
                            + uploadDatasetDto.getDatasetName());
                    RequestDispatcher disp;
                    disp = getServletContext().getRequestDispatcher(JSP_NAME_DESCRIBE);
                    disp.forward(req, resp);
                }
            } else {
                logger.info("Forwarding to JSP " + nextPage);
                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(nextPage);
                disp.forward(req, resp);
            }
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    }

    /**
     * If the DTO is null, then set the dispatcher
     * to reload the servlet page and return false.
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param uploadDatasetDto the UploadDatasetDto
     * @return true if the DTO is not null; false otherwise
     * @throws IOException throws an IO exception
     * @throws ServletException throws a Servlet exception
     */
    private boolean dtoExists(HttpServletRequest req, HttpServletResponse resp,
            UploadDatasetDto uploadDatasetDto) throws ServletException, IOException {
        if (uploadDatasetDto == null) {
            req.setAttribute(SESSION_DATA_ERROR_ATTRIB, "true");
            RequestDispatcher disp =
                    getServletContext().getRequestDispatcher("/UploadDataset");
            disp.forward(req, resp);
            return false;
        }

        return true;
    }

    /** Max file size for ImportQueue uploads is 400MB. */
    private static final Integer MAX_UPLOAD_FILE_SIZE = 400 * 1024 * 1024;

    /**
     * Handles uploading the file and adding a row to the file table in the database.
     * @param req the HTTP servlet request
     * @param dto The Upload Dataset DTO which contains all the info on the upload and the results
     * @param owner the current user and owner/creator/up-loader of the file
     * @throws Exception an ServletException, IOException, FileUploadException or...
     */
    private void handleUpload(HttpServletRequest req, UploadDatasetDto dto, UserItem owner)
            throws Exception {

        // Get the FileUpload items, includes other fields
        List <org.apache.commons.fileupload.FileItem> items = getDatasetUploadItems(req);
        if (items == null) {
            dto.setDataFileErrorFlag(true);
            dto.setErrorMessage(ERROR_MSG_FILE_SIZE_LIMIT_EXCEEDED);
            return;
        }
        dto.setDataFileErrorFlag(false);
        dto.setErrorMessage("");

        // If fileItemId already set in DTO, must clean-up as that file
        // did not pass verification and user went "back".
        if ((dto.getFileItemId() != null) && (dto.getImportQueueItemId() != null)) {
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            ImportQueueItem iqItem = iqDao.get((Integer)dto.getImportQueueItemId());
            iqItem.setFile(null);
            iqDao.saveOrUpdate(iqItem);
            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
            FileItem fileItem = fileDao.get(dto.getFileItemId());
            ImportQueueServlet.removeImportQueueFile(fileItem, getBaseDir());
            dto.setFileItemId(null);
        }

        // Process the FileUpload items, includes other fields
        FileItem fileItem = null;
        for (org.apache.commons.fileupload.FileItem uploadFileItem : items) {

            if (uploadFileItem.isFormField()) {
                String name = uploadFileItem.getFieldName();
                String value = uploadFileItem.getString();
                logDebug(name, " :: ", value);

                readFormData(name, value.trim(), dto);
            } else {
                // AbstractServlet allows for larger files to be uploaded
                // by DS Admins, as part of File upload. Make sure size
                // is appropriately limited here for dataset uploads.
                if (uploadFileItem.getSize() > MAX_UPLOAD_FILE_SIZE) {
                    dto.setDataFileErrorFlag(true);
                    dto.setErrorMessage(ERROR_MSG_FILE_SIZE_LIMIT_EXCEEDED);
                    return;
                }
                fileItem = createFile(owner, uploadFileItem);
                if (fileItem == null) {
                    dto.setDataFileErrorFlag(true);
                } else {
                    dto.setDataFile(fileItem.getDisplayFileName());
                    dto.setFileItemId((Integer)fileItem.getId());
                    dto.setDataFileErrorFlag(false);
                }
            }

        } // end for loop
    }

    /**
     * Run the mini-verify in the Flat File Importer (FFI).
     * Fill in the results in the Verification Results of the UploadDatasetDto.
     * @param dto The Upload Dataset DTO which contains all the info on the upload and the results
     * @return import status id from import_db
     * @throws Exception unknown exception
     */
    private Integer runFfi(UploadDatasetDto dto) throws Exception {

        // Run FFI-mini-verify
        Integer importStatusId = verifyFile(dto);

        // Unable to verify file for some reason beyond FFI...
        if (importStatusId == null) {
            logger.error("Unable to verify file: " + dto.getDataFile());
            VerificationResults results =
                    new VerificationResults(ImportQueueItem.STATUS_ERRORS);
            VerificationResults.Messages genMsgs = results.new Messages();
            genMsgs.addError("Failed to verify file. Reason unknown.");
            results.setGeneralMessages(genMsgs);
            dto.setResults(results);
            return null;
        }

        // Add the verification results to the UploadDatasetDto
        ImportQueueHelper iqHelper = new ImportQueueHelper();
        VerificationResults results = iqHelper.getVerificationResults(importStatusId);
        dto.setResults(results);
        return importStatusId;
    }

    /**
     * When the user reviews the success message or only potential issues and then
     * clicks the 'Continue' button, add an item to the queue.
     * May also have to create a project.
     * @param req {@link HttpServletRequest}
     * @param owner the owner of the queue item
     * @param dto The Upload Dataset DTO which contains all the info on the upload and the results
     * @param iqItem the ImportQueueItem to move to 'queued'
     */
    private void addItemToQueue(HttpServletRequest req, UploadDatasetDto dto,
            UserItem owner, ImportQueueItem iqItem) {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();

        // Get the IQ item if it was created before
        if (iqItem == null) {
            iqItem = iqDao.get(dto.getImportQueueItemId());
        }

        iqItem.setStatus(ImportQueueItem.STATUS_QUEUED);

        // set queue order
        Integer queueOrder = iqDao.getMaxQueueOrder();
        iqItem.setQueueOrder(queueOrder + 1);

        // save new import queue item
        iqDao.saveOrUpdate(iqItem);
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(dto.getExistingProjectId());
        Boolean isDiscourse = null;
        if (projectItem != null) {
            isDiscourse = projectItem.getIsDiscourseDataset();
        }
        String itemStr = ((isDiscourse != null) && isDiscourse) ? "Discourse" : "Dataset";

        // user log
        String info = itemStr + " '" + dto.getDatasetName()
                + "', File " + dto.getDataFile() + " (" +  dto.getFileItemId() + ")";
        UserLogger.log(null, owner, UserLogger.DATASET_UPLOAD, info);
        logDebug(info);

        // send email
        // Trac #424: only send email for XML uploads
        // Update - added Discourse format - don't send email for tab-delimited
        if (isSendmailActive() && (!iqItem.getFormat().equals(ImportQueueItem.FORMAT_TAB))) {
            String source = getClass().getSimpleName();
            String subject = itemStr + " \""
                    + dto.getDatasetName() + "\" added to the import queue";
            String emailContent = EmailUtil.notifyDatashopHelp(iqItem,
                    getBaseUrl(req), dto.getProjectSelection());
            sendDataShopHelpEmail(source, subject, emailContent);
        }
    }


    /**
     * Create a files-only dataset and import queue item, also create project if necessary.
     * @param dto The Upload Dataset DTO which contains all the info on the upload and the results
     * @param owner the current user
     */
    private void createFilesOnlyDataset(UploadDatasetDto dto, UserItem owner) {
        // create import queue item
        UploadDatasetHelper uploadHelper = HelperFactory.DEFAULT.getUploadDatasetHelper();

        // Get the DatasetItem first as this might fail if called from a SLAVE.
        DatasetItem datasetItem = null;
        try {
            datasetItem = DatasetCreator.INSTANCE.createNewDataset(dto.getDatasetName());
        } catch (IOException ioe) {
            // Failed to create DatasetItem.
            logDebug("Failed to create files-only dataset '" + dto.getDatasetName() + "': " + ioe);
            dto.setDatasetIdErrorFlag(true);
            datasetItem = null;
            dto.setErrorMessage(ioe.getMessage());
        }

        // If unable to create the DatasetItem, we're done here.
        if (datasetItem == null) { return; }

        ImportQueueItem importQueueItem =
            uploadHelper.createImportQueueItem(dto, owner, new Date(),
                                               ImportQueueItem.STATUS_NO_DATA,
                                               getBaseDir(), getNumFFIVerifyLines(), true);

        ProjectItem projectItem = importQueueItem.getProject();
        
        // create dataset item
        datasetItem.setProject(projectItem);
        datasetItem.setDescription(dto.getDatasetDesccription());
        datasetItem.setStatus("files-only");
        datasetItem.setReleasedFlag(true);
        datasetItem.setDomain(getDomain(dto.getDomainName()));
        datasetItem.setLearnlab(getLearnlab(dto.getLearnlabName()));
        datasetItem.setStudyFlag(dto.getHasStudyData());
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        datasetDao.saveOrUpdate(datasetItem);

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        projectItem.setDatasetLastAddedToNow();
        projectDao.saveOrUpdate(projectItem);

        ImportQueueDao importQueueDao = DaoFactory.DEFAULT.getImportQueueDao();
        importQueueItem.setDataset(datasetItem);
        importQueueDao.saveOrUpdate(importQueueItem);

        // If a slave, update master DataShop instance with dataset info.
        if (DataShopInstance.isSlave()) {
            try {
                Integer datasetId = (Integer)datasetItem.getId();
                DatasetDTO datasetDto =
                    HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                DatasetCreator.INSTANCE.setDataset(datasetDto);
            } catch (Exception e) {
                // Failed to push Dataset info to master. Ignore?
                logDebug("Failed to push dataset info to master for dataset '"
                         + dto.getDatasetName() + "': " + e);
            }
        }
    }

    /**
     * Given the domain name, return the the DomainItem.
     * @param domainName the domain name
     * @return DomainItem
     */
    private DomainItem getDomain(String domainName) {
        if ((domainName == null) || (domainName.equals(""))) { return null; }

        DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
        return domainDao.findByName(domainName);
    }

    /**
     * Given the learnlab name, return the the LearnLabItem.
     * @param learnlabName the learnlab name
     * @return LearnLabItem
     */
    private LearnlabItem getLearnlab(String learnlabName) {
        if ((learnlabName == null) || (learnlabName.equals(""))) { return null; }

        LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
        return learnlabDao.findByName(learnlabName);
    }

    /**
     * Read the form data and stuff all that info into a nice little DTO.
     * @param req {@link HttpServletRequest}
     * @param dto the UploadDatasetDto to fill in
     * @param numExisting number of existing projects, affects default for project group
     */
    private void readFormDataDescribe(HttpServletRequest req,
            UploadDatasetDto dto, int numExisting) {

        // If projectId specified, then on Project Page
        String value = getParameter(req, "projectId");
        if (value != null) {
            Integer existingProjectId = Integer.parseInt(value);
            dto.setExistingProjectId(existingProjectId);
            dto.setProjectSelection(UploadDatasetDto.PROJ_CURRENT);

            value = getParameter(req, "projectName");
            if (value != null && value.length() > 0) {
                dto.setNewProjectName(value);
                dto.setProjectSelection(UploadDatasetDto.PROJ_CURRENT);
            } else {
                dto.setNewProjectName("");
            }
        // Otherwise, user selects, new project, existing project or choose later
        } else {
            value = getParameter(req, "project_group");
            if (value != null) {
                dto.setProjectSelection(value);
            } else {
                //set default
                if (numExisting > 0) {
                    dto.setProjectSelection(UploadDatasetDto.PROJ_EXIST);
                } else {
                    dto.setProjectSelection(UploadDatasetDto.PROJ_LATER);
                }
                dto.setNewProjectNameErrorFlag(false);
            }

            value = getParameter(req, "new_project_name");
            if (value != null && value.length() > 0) {
                dto.setNewProjectName(value);
            } else {
                dto.setNewProjectName("");
            }

            value = getParameter(req, "dataCollectionType");
            if (value != null) {
                dto.setDataCollectionType(value);
            } else  {
                //set default
                dto.setDataCollectionType(ProjectItem.DATA_COLLECTION_TYPE_NOT_SPECIFIED);
            }

            value = getParameter(req, "existing_project_select");
            if (value != null) {
                Integer existingProjectId = Integer.parseInt(value);
                dto.setExistingProjectId(existingProjectId);
            } else {
                dto.setExistingProjectId(null);
            }
        }

        value = getParameter(req, "datasetName");
        if (value != null) {
            // Fix funky characters:
            //   Replace backslash with a space
            //   Remove any spaces at the beginning and the end of the name
            String fixedDatasetName = value
                    .replaceAll("\\\\", " ")
                    .trim();

            // If a slave, append id to datasetName.
            if (DataShopInstance.isSlave()) {
                fixedDatasetName += DataShopInstance.getSlaveIdStr();
            }

            dto.setDatasetName(fixedDatasetName);
        } else {
            dto.setDatasetName("");
        }

        value = getParameter(req, "datasetDesc");
        if (value != null) {
            dto.setDatasetDesccription(value);
        } else {
            dto.setDatasetDesccription("");
        }

        value = getParameter(req, "txToUploadGroup");
        if (value != null) {
            if (value.equals("yes")) {
                dto.setLoadDataNowFlag(true);
            } else {
                dto.setLoadDataNowFlag(false);
            }
        }

        value = getParameter(req, "domainLearnlab");
        if (value != null) {
            parseDomainLearnLab(value, dto);
        }

        value = getParameter(req, "hasStudyData");
        if (value != null) {
            dto.setHasStudyData(value);
        }
    }

    /**
     * Parse Domain/LearnLab info and update DTO.
     * @param value the value of the form field
     * @param dto the UploadDatasetDto object
     */
    private void parseDomainLearnLab(String value, UploadDatasetDto dto) {
        String domain = "Other";
        String learnlab = "Other";
        int slashIndex = value.indexOf("/");
        if (slashIndex > 0) {
            domain = value.substring(0, slashIndex);
            learnlab = value.substring(slashIndex + 1);
        }
        dto.setDomainName(domain);
        dto.setLearnlabName(learnlab);
    }

    /**
     * Read the form data and stuff all that info into a nice little DTO.
     * @param name name of the form field
     * @param value value of the form field
     * @param dto the UploadDatasetDto to fill in
     */
    private void readFormData(String name, String value, UploadDatasetDto dto) {

        if (name.equals("format_select")) {
            if (value != null) {
                dto.setFormat(value);
            } else {
                dto.setFormat(ImportQueueItem.FORMAT_TAB);
            }
        } else if (name.equals("anonOptionsGroup")) {
            if (value != null && value.equals("including")) {
                dto.setAnonymizedFlag(true);
            } else {
                dto.setAnonymizedFlag(false);
            }
        } else if (name.equals("dataFromExistingDataset")) {
            dto.setFromExistingFlag(true);
        }
    }



    /**
     * Helper function to handle file creation for handleUpload.
     * @param owner the current user and owner/creator/up-loader of the file
     * @param uploadFileItem the FileItem to parse
     * @return file item with data
     * @throws Exception a FileUploadException or...
     */
    private FileItem createFile(UserItem owner,
            org.apache.commons.fileupload.FileItem uploadFileItem) throws Exception {
        FileItem fileItem = new FileItem();
        if (!uploadFileItem.getFieldName().equals(PARAM_FILE_NAME)) {
            logger.error("uploadFile item is not a file.");
        }
        String fileFullName = uploadFileItem.getName();

        if (fileFullName.indexOf('\\') >= 0) {
            fileFullName = fileFullName.substring(fileFullName.lastIndexOf('\\') + 1);
        }
        if (fileFullName.indexOf('/') >= 0) {
            fileFullName = fileFullName.substring(fileFullName.lastIndexOf('/') + 1);
        }
        String contentType = uploadFileItem.getContentType();
        long sizeInBytes = uploadFileItem.getSize();

        String wholePath = getBaseDir() + File.separator + UploadDatasetHelper.SUB_PATH;
        fileItem.setFilePath(UploadDatasetHelper.SUB_PATH);
        fileItem.setAddedTime(new Date());

        if (contentType == null) {
            contentType = "";
        }
        fileItem.setFileType(contentType);
        fileItem.setOwner(owner);
        fileItem.setFileSize(new Long(sizeInBytes));
        logDebug("File: ", fileItem);

        //Check to make sure the user has selected a file.
        if (fileFullName != null && fileFullName.length() > 0) {
            // Create the directory
            File newDirectory = new File(wholePath);
            if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                FileUtils.updateFilePermissions(newDirectory, "chmod 775");
                logDebug("The directory has been created.", newDirectory.getAbsolutePath());

                //check that file with this name does not already exist.
                //if it does start incrementing adding _1, _2, to the end
                //of the file until a name is found that doesn't exist.
                List<String> fileList = Arrays.asList(newDirectory.list());
                int extensionIndex = fileFullName.lastIndexOf(".");
                String fileExt = "";
                String fileName = "";
                if (extensionIndex < 0) {
                    fileName = fileFullName;
                } else if (extensionIndex == 0) {
                    fileExt = fileFullName.substring(0, fileFullName.length());
                } else {
                    fileExt = fileFullName.substring(extensionIndex);
                    fileName = fileFullName.substring(0, extensionIndex);
                }
                int i = 0;
                // Replace one or more undesirable characters with a single underscore
                String fileNameToSave = fileFullName.replaceAll(
                    UploadDatasetServlet.BAD_FILEPATH_CHARS, "_");
                while (fileList.contains(fileNameToSave)) {
                    i++;
                    fileNameToSave = fileName + "_" + i + fileExt;
                }
                fileItem.setFileName(fileNameToSave);

                // Write the file to the directory
                File newFile = new File(wholePath, fileNameToSave);
                logDebug("Absolute path is ", newFile.getAbsolutePath());
                uploadFileItem.write(newFile);

                // Create a new file item in the database
                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                fileDao.saveOrUpdate(fileItem);

                FileUtils.updateFilePermissions(newFile, "chmod 664");
            } else {
                logger.error("unable to create the new file directory: " + wholePath);
                return null;
            }
        } else {
            logger.error("No file selected.");
            return null;
        }
        return fileItem;
    } // end createFile

    /**
     * Utility method to get ProjectItem from the given string which might be null.
     * @param projectIdAttribute String value of id attribute in URL
     * @return a projectItem object if one found, null otherwise
     */
    private ProjectItem getProjectItem(String projectIdAttribute) {
        Integer projectId = getIntegerId(projectIdAttribute);
        if (projectId != null) {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            return projectDao.get(projectId);
        }
        return null;
    }

    /** Thank you message content. */
    private static final String REQUEST_ROLE_THANK_YOU_MESSAGE =
        "Thank you for requesting access to add datasets and projects. "
        + "We will review your request and notify you shortly.";

    /**
     * Handle request for DataShop-Edit privilege.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON Exception
     */
    private void requestRole(HttpServletRequest req, HttpServletResponse resp,
                             UserItem userItem)
        throws IOException, JSONException {
        // Log a user action that the role was requested.
        String reason = req.getParameter("requestReason");
        String info = "User '" + userItem.getId() + "', Reason '" + reason + "'";
        UserLogger.log(userItem, UserLogger.REQUEST_DS_EDIT_ROLE, info, false);

        if (isSendmailActive()) {
            String userEmail = userItem.getEmail();
            String subject =
                "Requesting access to upload datasets and create projects in DataShop";
            StringBuffer message = new StringBuffer();
            message.append("<br>");
            message.append("User ");
            message.append(userItem.getUserName());
            if (userEmail != null) {
                message.append(", ");
                message.append("<a href=\"mailto:");
                message.append(userEmail);
                message.append("\">");
                message.append(userEmail);
                message.append("</a>");
                message.append(",");
            }
            message.append(" is requesting permission to upload datasets and create projects. ");
            if (reason != null) {
                message.append("The reason given is: ");
                message.append("<br><br>");
                message.append(reason);
            }
            message.append("<br>");

            sendDataShopHelpEmail(null, subject, message.toString(), userEmail);
        }

        // write JSON response
        writeJSON(resp, json("message", REQUEST_ROLE_THANK_YOU_MESSAGE));
    }

    /**
     * Helper method to determine if logged in user is authorized to upload datasets.
     * @param req {@link HttpServletRequest}
     * @return boolean flag
     */
    private boolean isUserAuthorized(HttpServletRequest req) {
        // If the ServletRequest is to request a role, allow it.
        if (req.getParameter("requestRole") != null) {
            return true;
        }

        UserItem user = getUser(req);

        if (user.getAdminFlag()) {
            return true;
        }

        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        return userRoleDao.hasDatashopEditRole(user);
    }


    /**
     * Use FlatFileImporter to verify the file specified in DTO.
     * @param dto the UploadDatasetDto which specifies file and dataset
     * @return the id of the row created in the import_db.import_status table
     * @throws IOException indicating an error dealing with file to be verified
     */
    private Integer verifyFile(UploadDatasetDto dto)
        throws IOException {

        Integer iqId = dto.getImportQueueItemId();
        Integer fileItemId = dto.getFileItemId();

        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        FileItem fileItem = fileDao.get(fileItemId);

        String filePath = getBaseDir() + File.separator + UploadDatasetHelper.SUB_PATH + File.separator + iqId;
        File[] files = new File(filePath).listFiles();
        File tmpFile = null;
        for (File f : files) {
            if (f.getName().startsWith(ImportQueueHelper.HEAD_FILE_PREFIX)) {
                tmpFile = f;
            }
        }
        String fullFileName = filePath + File.separator + ImportQueueHelper.HEAD_FILE_PREFIX
            + fileItem.getFileName();
        File head = new File(fullFileName);
        if (!head.exists()) {
            fullFileName = tmpFile.getCanonicalPath();
        }

        FlatFileImporter ffi = new FlatFileImporter();
        Integer importStatusId = ffi.verifyOnlyFile(fullFileName, dto.getDatasetName(),
                                                 dto.getDomainName(), dto.getLearnlabName(), iqId);

        return importStatusId;
    }
}
