/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;

import static edu.cmu.pslc.datashop.util.FileUtils.updateFilePermissions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowDatasetMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowFolderDao;
import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowAnnotationDao;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportHelper;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.ErrorMessageMap;
import edu.cmu.pslc.datashop.workflows.Workflow;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowsMail;
import edu.cmu.pslc.datashop.workflows.WorkflowAnnotationItem;
import edu.cmu.pslc.datashop.workflows.AbstractComponent;

/**
 * This servlet handles the management and requests for workflows.
 *
 * @author Mike Komisin
 * @version $Revision: 15837 $ <BR>
 * Last modified by: $Author: mkomisin $ <BR>
 * Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $ <!-- $KeyWordsOff: $ -->
 */
public class WorkflowEditorServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for editing workflows as a standalone tool. */
    private static final String DETACHED_EDIT_JSP_NAME = "/jsp_workflows/learnsphere-edit.jsp";
    /** The JSP name for editing workflows as a standalone tool. */
    private static final String DETACHED_LIST_JSP_NAME = "/jsp_workflows/learnsphere-list.jsp";
    /** The JSP for logging in to WorkflowEditor. */
    private static final String LOGIN_JSP_NAME = "/jsp_workflows/ls_login.jsp";


    /** The workflow helpers. */
    private WorkflowHelper workflowHelper;
    private WorkflowImportHelper workflowImportHelper;
    private WorkflowAnnotationHelper workflowAnnotationHelper;
    private ComponentHelper componentHelper;
    private ConnectionHelper connectionHelper;
    private WorkflowFileHelper workflowFileHelper;
    /** The workflow DAO. */
    private WorkflowDao workflowDao;
    /** The datashop file DAO. */
    private FileDao fileDao;
    /** The workflow component instance DAO. */
    private WorkflowComponentInstanceDao wciDao;
    /** The WFC CommonSchemas directory. */
    private static String commonSchemasDir;
    /** The WFC CommonResources directory. */
    private static String commonResourcesDir;

    private static final String DS_TRANSACTION_IMPORT = "transaction";
    private static final String DS_STUDENT_STEP_IMPORT = "student-step";
    private static final String DS_STUDENT_PROBLEM_IMPORT = "student-problem";

    /** Label used for setting session attribute "recent_report". */
    public static final String SERVLET_NAME = "WorkflowEditor";

    /** Format for the date range method, getDateRangeString. */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMM d, yyyy HH:mm:ss");
    /** The Sort-by context attribute handle. */
    public static final String SORT_BY = "sortBy";
    /** The toggle sort context attribute handle. */
    public static final String TOGGLE_SORT = "toggleSort";
    /** Max debug file tail length (500KB). */
    private static final Long MAX_DEBUG_TAIL_LENGTH = new Long(1024 * 512 * 1);

    /**
     * Handles the HTTP get.
     *
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     *
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        setEncoding(req, resp);
        if ((req.getParameter("requestingMethod") == null)
            || (req.getParameter("requestingMethod") != null
                && !req.getParameter("requestingMethod").equalsIgnoreCase("WorkflowEditorServlet.getComponentStates"))) {

            logger.info("doPost params :: " + getDebugParamsString(req));
        }

        commonSchemasDir = WorkflowFileUtils.getStrictDirFormat(WorkflowHelper.getWorkflowComponentsDir())
                + "CommonSchemas/";

        commonResourcesDir = WorkflowFileUtils.getStrictDirFormat(WorkflowHelper.getWorkflowComponentsDir())
                + "CommonResources/";

        // Get the logged in user or null if not logged in.
        UserItem loggedInUserItem = getLoggedInUserItem(req);
        // Forward to the workflows_detached JSP (view) unless the disp is overridden by another method.

        Boolean isLoggedIn = false;
        if (loggedInUserItem != null) {
            isLoggedIn = true;
        }

        if (loggedInUserItem != null) {

            // Helper
            workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
            workflowImportHelper = HelperFactory.DEFAULT.getWorkflowImportHelper();
            workflowAnnotationHelper = HelperFactory.DEFAULT.getWorkflowAnnotationHelper();
            componentHelper = HelperFactory.DEFAULT.getComponentHelper();
            connectionHelper = HelperFactory.DEFAULT.getConnectionHelper();
            workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
            // DAOs
            workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            fileDao = DaoFactory.DEFAULT.getFileDao();
            wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();

            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            // Workflow and workflow context
            WorkflowItem workflowItem = null;
            WorkflowContext workflowContext = null;
            // Dataset and dataset context
            DatasetItem datasetItem = null;

            // Authorization conditions
            Boolean isAdmin = false;
            Boolean isOwner = false;
            Boolean isShared = false;
            Date lastUpdated = null;

            // Handle requests for Workflow role
            if (req.getParameter("requestRole") != null) {
                logger.info("**** where did this come from?");
                return;
            }

            if (loggedInUserItem.getAdminFlag()) {
                isAdmin = true;
            }

            /**
             *  Workflow Context.
             */
            workflowContext = (WorkflowContext) req.getSession().getAttribute("workflowContext");

            if (workflowContext == null) {
                // In this case, neither the dataset context nor session contains a
                // workflow context so we create a new workflow context.
                workflowContext = new WorkflowContext();
                req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
                req.setAttribute("workflowContext", workflowContext);
            }

            /**
             *  Workflow item initialization and get workflow permissions.
             */
            Long workflowId = null;
            String workflowIdString = req.getParameter("workflowId");
            // This could be set as an attribute by one of the intercepting methods
            if (workflowIdString == null) {
                // Only set this if workflowIdString has not been set
                workflowIdString = (String) req.getAttribute("workflowId");
            }


            if (workflowIdString != null && workflowIdString.matches("\\d+")) {
                workflowId = Long.parseLong(workflowIdString);
                workflowItem = workflowDao.get(workflowId);
                if (workflowItem != null) {

                    // Test if this is the workflow owner.
                    if (workflowItem.getOwner() != null
                                && workflowItem.getOwner().getId()
                                        .equals(loggedInUserItem.getId())) {
                        isOwner = true;

                    }
                    // Test if this workflow is shared.
                    if (workflowItem.getGlobalFlag()) {
                        isShared = true;
                    }

                    lastUpdated = workflowItem.getLastUpdated();
                }
            }

            // Get the requesting method.
             String requestingMethod = req.getParameter("requestingMethod");

            if (workflowItem == null) {
                /**
                 * Handle file uploads and file requests.
                 */
                // Handling multi-part form data (it will not have doPost params, but
                // only the owner and LearnSphere admins can upload files to the workflow (see the uploadOptionsFile
                // JavaScript function in WorkflowEditor.js).
                // If multi-part form data was submitted, then save it and set the necessary request attributes for the
                // fileUpload requestMethod.
                if (ServletFileUpload.isMultipartContent(req)) {
                    // The upload file method not only saves the file, but also extracts
                    // the necessary form field values and puts them back in the
                    // request--
                    // i.e., componentId, workflowId, requestingMethod, filePath, and fileName
                    List<org.apache.commons.fileupload.FileItem> items = getDatasetUploadItems(req);
                    if (items == null) {
                        String errorMsg = isAdmin ? "File size exceeds 1GB allowance."
                            : "File size exceeds 400MB allowance.";
                        writeJsonError(resp, errorMsg);
                        return;
                    }
                    // We may bring back a map of columnHeaders to their indices on the next method
                    Boolean fileUploaded = workflowImportHelper.handleUploadRequest(workflowFileHelper, workflowHelper,
                            req, resp, items,
                            WorkflowFileUtils.getStrictDirFormat(getBaseDir()), WorkflowHelper.getWorkflowComponentsDir(), loggedInUserItem);
                    if (fileUploaded) {
                        // If an import data file was uploaded, we have to build the upload request
                        // from the request attributes set during handleUploadRequest.
                        if (requestingMethod == null) {
                            requestingMethod = (String) req.getAttribute("requestingMethod");
                        }
                        if (requestingMethod != null && requestingMethod.equals("WorkflowEditorServlet.fileUpload")) {
                            workflowIdString = (String) req.getAttribute("workflowId");
                            if (workflowIdString != null && workflowIdString.matches("\\d+")) {
                                workflowId = Long.parseLong((String) req.getAttribute("workflowId"));
                                workflowItem = workflowDao.get(workflowId);
                                doPostFileUpload(req, resp, loggedInUserItem, workflowItem);
                            }


                            return;
                        }
                    }
                    return;
                    /* End multi-part form handling. */
                }
            }

            /**
             *  Workflow Servlet Requests.
             */

            if (isLoggedIn && requestingMethod != null
                    && requestingMethod.equals("WorkflowEditorServlet.fetchMyFolders")) {

                JSONObject myFolders = new JSONObject();
                Long folderId = null;
                try {
                    folderId = Long.parseLong(req.getParameter("folderId"));
                } catch (NumberFormatException nfe) {
                    // do not log error, it can be empty string
                }
                WorkflowFolderDao wfFolderDao = DaoFactory.DEFAULT.getWorkflowFolderDao();
                Boolean success = false;
                String message = null;
                UserItem ownerSearchTerm = loggedInUserItem;
                if (loggedInUserItem.getAdminFlag()
                    && workflowItem != null
                        && !workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                    ownerSearchTerm = workflowItem.getOwner();
                }

                List<WorkflowFolderItem> wffList = wfFolderDao.findByOwner(ownerSearchTerm);
                if (wffList != null && !wffList.isEmpty()) {
                    // Put folder info into a JSON object
                    for (WorkflowFolderItem wffItem : wffList) {
                        try {
                            myFolders.put(wffItem.getWorkflowFolderName(), wffItem.getId());
                        } catch (JSONException e) {
                            logger.error("Cannot create myFolders JSON.");
                        }
                    }

                    success = true;
                    message = "";

                } else {
                    // myFolders is empty
                    success = true;
                    message = "You must first create a folder.";

                }

                try {
                    writeJSON(resp,
                        json("workflowId", workflowId,
                                "myFolders", myFolders,
                                "folderId", folderId,
                                "isShared", isShared,
                                "lastUpdated", lastUpdated,
                                "success", success,
                                "message", message));
                    return;
                } catch (JSONException e) {
                    writeJsonError(resp, "Could not fetch workflow folder list.");
                }

            } else if (workflowItem != null && (isAdmin || isOwner || isShared)) {
            // If the permissions criteria are adequate for the workflow, continue.

                // Location of the CommonSchemas folder.

                if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.requestDebugInfo")
                        && (isOwner || isAdmin)) {
                    // Get the workflow history (unfinished).
                    this.doPostRequestDebugInfo(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.requestDebugDownload")
                        && (isOwner || isAdmin)) {
                    // Get the workflow history (unfinished).
                    this.doPostRequestDebugDownload(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.requestComponentSpecificOptions")) {
                    // Get component options.
                    // datasetItem is not used for anything except UI feedback in the following method.
                    DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                    WorkflowDatasetMapDao wfdsMapDao = DaoFactory.DEFAULT.getWorkflowDatasetMapDao();
                    List<WorkflowDatasetMapItem> wfdsMapItems = wfdsMapDao.findByWorkflow(workflowItem);
                    if (wfdsMapItems != null && !wfdsMapItems.isEmpty()) {
                        // TBD... the size of this list can be > 1. Need to change how this is done.
                        WorkflowDatasetMapId wfdsMapId = (WorkflowDatasetMapId) wfdsMapItems.get(0).getId();
                        if (wfdsMapId != null) {
                             WorkflowDatasetMapItem workflowDatasetMapItem = wfdsMapDao.get(wfdsMapId);
                             if (workflowDatasetMapItem != null && workflowDatasetMapItem.getDataset() != null) {
                                 datasetItem = datasetDao.get((Integer) workflowDatasetMapItem.getDataset().getId());
                             }
                        }
                    }

                    doPostRequestComponentSpecificOptions(req, resp, workflowContext, workflowItem, datasetItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                            && requestingMethod.equals("WorkflowEditorServlet.populateDatasetDialog")) {
                     // Get list of datasets (and their projects) that user can select in the component option panel.

                     doPostRequestProjectsAndDatasets(req, resp, workflowContext, workflowItem, loggedInUserItem);
                     return;
                 } else if (requestingMethod != null
                           && requestingMethod.equals("WorkflowEditorServlet.populateDatasetLinkDialog")) {
                    // Get list of datasets (and their projects) that user can attach workflow to.

                    doPostRequestProjectsAndDatasets(req, resp, workflowContext, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.getComponentStates")) {
                    // Get component states.
                    doPostGetComponentStates(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.updateDirtyBits") && isOwner) {
                    Boolean saveFlag = false;
                    processDirtyBits(req, resp, workflowItem, null, loggedInUserItem, saveFlag);
                    try {
                        writeJSON(resp, json("success", true));
                    } catch (JSONException e) {
                        logger.error("An error occurred during workflow: updateDirtyBits.");
                    }
                    return;
                } else if (requestingMethod != null
                        && (requestingMethod.equals("WorkflowEditorServlet.saveCurrentWorkflow")
                            || requestingMethod.equals("WorkflowEditorServlet.cancelAndSaveCurrentWorkflow"))
                        && (isOwner || isAdmin)) {
                    // datasetItem is not used for anything except UI feedback in the following method.
                    doPostHandleSaveRequests(req, resp, workflowItem, datasetItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                    && (requestingMethod.equals("WorkflowEditorServlet.preloadWorkflow"))) {
                    // datasetItem is not used for anything except UI feedback in the following method.
                    doPostHandleSaveRequests(req, resp, workflowItem, datasetItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.getComponentInput") && (isOwner || isAdmin)) {
                    doPostGetComponentInput(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.getImportDataTypeXml") && (isOwner || isAdmin)) {
                    doPostGetImportDataTypeXml(req, resp, workflowItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.getComponentMetadata") && (isOwner || isAdmin)) {
                    doPostGetComponentMetadata(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.existingFile") && isOwner) {
                    doPostUseExistingFile(req, resp, workflowItem, loggedInUserItem);
                    return;
                }else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.createAnnotation") && isOwner) {
                    doPostCreateAnnotation(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                           && requestingMethod.equals("WorkflowEditorServlet.existingDataset") && isOwner) {
                    doPostAttachFileToDataset(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.runWorkflow") && isOwner) {
                    workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, datasetItem,
                        WorkflowHelper.LOG_RUN_WORKFLOW, "", null);
                    doPostRunWorkflow(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.validateWorkflow") && isOwner) {
                    doPostValidateWorkflow(req, resp, workflowItem, loggedInUserItem);
                    return;
                }

            } else if (workflowItem == null || (!isOwner && isShared)) {
                if (requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.getComponentStates")) {
                    doPostGetComponentStates(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                           && requestingMethod.equals("WorkflowEditorServlet.refreshWorkflowComponentDefinitions")
                           && isAdmin) {
                    ComponentHelper.finalOptionTypes = null;
                    ConnectionHelper.finalInputEndpoints = null;
                    ConnectionHelper.finalOutputEndpoints = null;
                    OptionDependencyHelper.fullOptionConstraintMap = null;
                    ComponentHelper.componentInfoDivs = null;
                    ComponentHelper.componentTypeHierarchy = null;
                    ComponentHelper.componentList = new JSONArray();
                    ComponentHelper.authorList = new JSONArray();
                    try {
                        connectionHelper.getEndpoints(ConnectionHelper.XML_INPUT_DEFS);
                        connectionHelper.getEndpoints(ConnectionHelper.XML_OUTPUT_DEFS);
                        OptionDependencyHelper.getFullOptionConstraintMap();
                        ComponentHelper.getComponentTypeHierarchy(
                                commonResourcesDir + "ComponentTypeHierarchy.xml");
                    } catch (JDOMException e1) {
                        writeJsonError(resp, "Could not refresh component definitions.");
                        return;
                    }
                    try {
                        writeJSON(resp, json("success", true, "refreshed", "true"));
                        return;
                    } catch (JSONException exception) {
                        writeJsonError(resp, "Error fetching workflow state.");
                        return;
                    } catch (IOException e) {
                        writeJsonError(resp, "Error fetching workflow state.");
                        return;
                    }
                } else {



                    Integer datasetId = null;
                    Boolean canCreateWorkflow = false;
                    if (req.getParameter("datasetId") != null
                            && req.getParameter("datasetId").matches("\\d+")) {
                                datasetId = Integer.parseInt(req.getParameter("datasetId"));
                    }
                    if (datasetId != null) {

                        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                        datasetItem = dsDao.get(datasetId);
                        if (datasetItem != null) {
                            req.setAttribute("datasetId", datasetId.toString());
                            req.setAttribute("datasetName", WorkflowFileUtils.htmlEncode(datasetItem.getDatasetName()));
                            // If datasetItem is null, authLevel becomes null
                            String authLevel = authDao.getAuthLevel(loggedInUserItem, datasetItem);
                            if (loggedInUserItem.getAdminFlag() || (datasetItem != null && authLevel != null
                                    // We won't allow file attachment to public datasets unless
                                    // the user has specific edit/admin access to the project.
                                    // Leave out: authDao.isPublic((Integer) datasetItem.getProject().getId())
                                    && (authLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_EDIT)
                                            || authLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_ADMIN)))) {
                                canCreateWorkflow = true;
                            }
                        }
                    } else {
                        canCreateWorkflow = true;
                        workflowContext.removeSearchAttribute(WorkflowContext.WF_SEARCH_DATASET);
                    }

                    req.setAttribute("canCreateWorkflow", canCreateWorkflow);


                    // Ensure WorkflowHelper.componentList is populated for auto-complete
                    // Ensure data is up to date before calling get
                    ComponentHelper.getComponentTypeHierarchy(commonResourcesDir
                                + "ComponentTypeHierarchy.xml");

                    req.setAttribute("componentList", componentHelper.getComponentList());

                    // Ensure WorkflowHelper.workflowTags is populated for auto-complete
                    WorkflowPropertiesHelper.refreshWorkflowTagsList();
                    req.setAttribute("existingWorkflowTags", WorkflowPropertiesHelper.getWorkflowTags());

                    // Ensure WorkflowHelper.authorList is populated for auto-complete
                    // Here the accessor ensures it is up to date.

                    // Placeholder: Uncomment the next line to enable author search auto-complete
                    // (disabled because of privacy concerns)
                    //req.setAttribute("authorList", workflowHelper.getAuthorList());

                    // If none of the above methods were relevent, then we shall get the workflows list
                    // The enclosing conditional logic entails that the user is logged in.
                    logger.info(getBenchmarkPrefix(getClass().getSimpleName(), (String) loggedInUserItem.getId())
                            + " getting workflows page.");

                    // Set the most recent servlet name for the help page.
                    setRecentReport(req.getSession(true), SERVLET_NAME);

                    RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_LIST_JSP_NAME);

                    logger.debug("Forwarding to JSP " + DETACHED_LIST_JSP_NAME);
                    disp.forward(req, resp);
                    return;
                }
            }

        } else { // User not logged in

            // Let the editor know the user is logged out.
            if (req.getParameter("requestingMethod") != null) {
                try {
                    writeJSON(resp, json("success", true, "loggedOut", true));
                    return;
                } catch (IOException ioe) {
                    writeJsonError(resp, "Status update in error.");
                } catch (JSONException je) {
                    writeJsonError(resp, "Status update in error.");
                }
            }


        }

        RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_LIST_JSP_NAME);
        // The user is not logged in
        logger.debug("Forwarding to JSP " + LOGIN_JSP_NAME);
        disp = getServletContext().getRequestDispatcher(LOGIN_JSP_NAME);
        disp.forward(req, resp);
        return;
    }

    /**
     * Validate the workflow item.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostValidateWorkflow(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {

        logger.trace("Validating workflow.");
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            WorkflowFileHelper workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
            Long workflowId = workflowItem.getId();
            String workflowName = workflowItem.getWorkflowName();

            Boolean isWorkflowSaved = req.getParameter("isWorkflowSaved") == null ? false : Boolean.parseBoolean(req
                    .getParameter("isWorkflowSaved"));

            Boolean isSaveAsNew = req.getParameter("isSaveAsNew") == null ? false : Boolean.parseBoolean(req
                    .getParameter("isSaveAsNew"));

            String workflowXml = workflowFileHelper.getWorkflowXmlFromJson(req, resp);
            if (workflowXml == null) {
                writeJsonError(resp, "Could not parse workflow.");
                return;
            }
            // To test the workflow, we create a new workflow (extends the AbstractExtractor class).
            Workflow workflow = new Workflow((Long) workflowItem.getId());
            ErrorMessageMap errorMessageMap = null;
            Boolean isValid = false;
            // Init the extractor.
            workflow.init(WorkflowFileUtils.getStrictDirFormat(getBaseDir()),
                    WorkflowFileUtils.getWorkflowsDir(workflowId), loggedInUserItem);

            // Test the workflow and store the errors of the test in the errorMessageMap
            errorMessageMap = workflow.test(workflowItem, workflowXml, isWorkflowSaved, isSaveAsNew);
            if (errorMessageMap == null || errorMessageMap.isEmpty()) {
                // The XML is valid
                isValid = true;
            }
            // Finished validating. Set workflow to null.
            workflow = null;

            try {
                // validation results
                writeJSON(
                        resp,
                        json("workflowId", workflowId, "workflowName", workflowName, "isValid", isValid.toString(),
                                "errorMessageMap", errorMessageMap.getErrorMessageMapAsJson()));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error retrieving test results.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error creating test results.");
                return;
            }
        }

    }

    /**
     * Handles save requests for the workflow from the UI.
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the WorkflowItem to save
     * @param datasetItem the optional DatasetItem
     * @param loggedInUserItem the UserItem of the logged-in user
     * @see edu.cmu.pslc.datashop.workflows.WorkflowImportHelper#saveWorkflowToDatabase(WorkflowItem)
     */
    private void doPostHandleSaveRequests(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            DatasetItem datasetItem, UserItem loggedInUserItem) {
        Long workflowId = null;
        Integer datasetId = null;
        if (datasetItem != null) {
            datasetId = (Integer) datasetItem.getId();
        }
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {

            String requestingMethod = req.getParameter("requestingMethod");

            // Get the JavaScript object as a string.
            String jsonString = req.getParameter("digraphObject");
            String persistString = req.getParameter("persist");
            Boolean persistent = false;
            if (persistString != null && persistString.equalsIgnoreCase("true")) {
                persistent = true;
                workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, datasetItem,
                    WorkflowHelper.LOG_SAVE_WORKFLOW, "", null);
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException exception) {
                writeJsonError(resp, "Error saving workflow.");
                return;
            }

            // Either the current workflow must be canceled before it can be run
            if (!requestingMethod.equals("WorkflowEditorServlet.cancelAndSaveCurrentWorkflow")
                    && !requestingMethod.equals("WorkflowEditorServlet.preloadWorkflow")
                    && workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                logger.info("Cannot save workflow. Workflow is running.");
                try {
                    writeJSON(resp,
                            json("datasetId", datasetId, "workflowId", workflowId, "state", "running", "message",
                                    "The workflow cannot be saved until you cancel the running workflow."));
                } catch (JSONException e) {
                    writeJsonError(resp, "Unexpected json error during workflow save.");
                } catch (IOException e) {
                    writeJsonError(resp, "Error writing to response.");
                }

                return;
            } else if (requestingMethod.equals("WorkflowEditorServlet.cancelAndSaveCurrentWorkflow")) {
                // Or the workflow can be saved since it has been canceled.

                if (workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                    // Update workflow state to dirty. Proceed with saving workflow.
                    workflowItem.setState(WorkflowItem.WF_STATE_NEW);
                    workflowDao.saveOrUpdate(workflowItem);
                    WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();

                    List<WorkflowComponentInstanceItem> componentItems
                        = wciDao.findByWorkflow(workflowItem);
                    for (WorkflowComponentInstanceItem wciItem : componentItems) {
                        if (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {
                            wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY);
                            wciDao.saveOrUpdate(wciItem);
                            // Attempt to cancel and remove the local or remote process
                            //workflowHelper.removeWfProcess(workflowId.toString() + wciItem.getComponentName());
                        } else if (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_NEW)) {
                            wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY);
                            wciDao.saveOrUpdate(wciItem);
                            // Attempt to cancel and remove the local or remote process
                            //workflowHelper.removeWfProcess(workflowId.toString() + wciItem.getComponentName());
                        }
                    }

                } else if (workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                    // Proceed with saving workflow by falling through this logic block into the next.
                }
            }

            if (!requestingMethod.equals("WorkflowEditorServlet.cancelAndSaveCurrentWorkflow")) {
                // Proceed with saving workflow.
                try {
                    if (jsonObject != null && jsonObject.has("isShared") && !jsonObject.isNull("isShared")) {
                        Object jsonIsShared = jsonObject.get("isShared");
                        if (jsonIsShared != null
                                && (jsonIsShared.toString().equalsIgnoreCase("true") || jsonIsShared.toString().equalsIgnoreCase("false"))) {
                            // Set global flag
                            workflowItem.setGlobalFlag(Boolean.parseBoolean(jsonIsShared.toString()));
                        }
                    }
                } catch (JSONException exception) {
                    writeJsonError(resp, "Error saving workflow shareable property.");
                    return;
                }

                Element digraphDoc = null;
                try {
                    digraphDoc = WorkflowXmlUtils.convertJsonToXML(jsonObject);
                } catch (JSONException e) {
                    writeJsonError(resp, "Error converting workflow to XML.");
                    return;
                } catch (JDOMException e) {
                    writeJsonError(resp, "Error parsing workflow.");
                    return;
                } catch (UnsupportedEncodingException e) {
                    writeJsonError(resp, "Unsupported encoding.");
                    return;
                } catch (IOException e) {
                    writeJsonError(resp, "Error opening workflow.");
                    return;
                }

                if (digraphDoc != null) {

                    String workflowXml = null;
                    try {
                        workflowXml = WorkflowXmlUtils.getElementAsString(digraphDoc);
                    } catch (IOException e1) {
                        logger.error("Could not convert workflow XML to string.");
                    }

                    // Save workflow
                    workflowItem.setWorkflowXml(workflowXml);

                    if (persistent) {

                        // required for backwards compatibility with older workflows: try to create wfc adjacency
                        ConnectionHelper.updateAdjacencyList(getBaseDir(), workflowItem, loggedInUserItem);

                        ComponentHelper.removeDeletedComponents(workflowItem, digraphDoc, getBaseDir());

                        // Update the database with this workflow XML
                        workflowHelper.saveWorkflowToDatabase(workflowItem, getBaseDir());
                        workflowHelper.saveComponentInstances(workflowItem, getBaseDir());
                        workflowHelper.saveComponentXmlFiles(null, workflowItem, getBaseDir());
                        workflowAnnotationHelper.saveAnnotations(workflowItem, getBaseDir(), digraphDoc);




                    } else {
                        // Update the database with this workflow XML
                        workflowHelper.saveTempWorkflowToDatabase(workflowItem, workflowDao);
                    }

                     processDirtyBits(req, resp, workflowItem, digraphDoc, loggedInUserItem, persistent);
                }
            }

            String isGlobalFlag = workflowItem.getGlobalFlag() ? "true" : "false";

            try {
                JSONObject tmp = org.json.XML.toJSONObject(workflowItem.getWorkflowXml());
                writeJSON(resp, json("isShared", isGlobalFlag,
                                     "workflow", tmp.get("workflow")));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "An error occurred while trying to save the current workflow " + workflowId);
                return;
            } catch (IOException e) {
                writeJsonError(resp, "An error occurred while trying to save the current workflow " + workflowId);
                return;
            }
        }
    }

    /**
     * Execute the workflow.
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param datasetItem the optional dataset item
     * @param loggedInUserItem the logged in user
     */
    private void doPostRunWorkflow(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {
        Boolean foundErrors = false;
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            Long workflowId = workflowItem.getId();
            // Setup properties for workflow item
            String workflowName = workflowItem.getWorkflowName();
            String results = null;
            Workflow workflow = new Workflow(workflowId);
            try {
                workflow.init(WorkflowFileUtils.getStrictDirFormat(getBaseDir()),
                    WorkflowFileUtils.getWorkflowsDir(workflowId), loggedInUserItem);
            } catch (Exception e) {
                foundErrors = true;
                writeJsonError(resp, "Workflow initialization error.");
                return;
            }

            if (workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)
                    || workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                foundErrors = true;
                writeJsonError(resp, "Workflow is already running. Please wait for the process to complete.");
                return;
            }

            Date lastUpdated = null;

            ErrorMessageMap errorMessageMap = new ErrorMessageMap();
            ErrorMessageMap genericMessageMap = new ErrorMessageMap();
            // Run workflow
            try {
                logger.info("Running workflow (" + workflowId + ") for user " + loggedInUserItem.getId());

                if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                    workflowItem.setState(WorkflowItem.WF_STATE_RUNNING);
                    workflowDao.saveOrUpdate(workflowItem);

                    workflow.run();
                    results = workflow.getWorkflowResults();
                    logger.info("Finished running workflow (" + workflowId + ") for user " + loggedInUserItem.getId());

                    lastUpdated = workflowItem.getLastUpdated();

                    workflowItem.setResults(results);
                    workflowItem.setLastUpdated(new Date());
                    try {
                        workflowDao.saveOrUpdate(workflowItem);
                    } catch (Exception e) {
                        writeJsonError(resp, "Workflow results save error.");
                        return;
                    }

                    errorMessageMap = workflow.getErrorMessageMap();
                    genericMessageMap = workflow.getGenericMessageMap();
                    if (errorMessageMap.isEmpty()) {
                        workflowItem.setState(WorkflowItem.WF_STATE_SUCCESS);
                    } else {
                        workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    }

                    workflowDao.saveOrUpdate(workflowItem);

                    workflowHelper.saveComponentXmlFiles(null, workflowItem, getBaseDir());

                } else {
                    workflowItem.setState(WorkflowItem.WF_STATE_RUNNING_DIRTY);
                }
            } catch (Exception e) {
                foundErrors = true;
                String errorMsg = "Unknown component error. Please contact "
                    + "<a href=\"mailto:"
                    + this.getEmailAddressDatashopHelp() + "\">"
                    + this.getEmailAddressDatashopHelp()
                    + "</a>" + " with the Workflow Name and error you received.";
                logger.error("Unknown component error.");
                ComponentHelper.setRunningComponentsToError(workflowItem);
                // Set workflow to error state and save to persistence tables
                try {
                    workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    workflowDao.saveOrUpdate(workflowItem);
                    workflowHelper.saveComponentInstances(workflowItem, getBaseDir());

                    workflowHelper.saveComponentXmlFiles(null, workflowItem, getBaseDir());
                } catch (Exception e2) {
                    writeJsonError(resp, "Could not save workflow item.");
                    return;
                }
                writeJsonError(resp, errorMsg);
                return;
            } finally {
                if (foundErrors) {
                    // send email if long-running
                    if (WorkflowPropertiesHelper.isLongRunning(lastUpdated, workflowItem)) {
                        sendEmail(getEmailAddressDatashopHelp(),
                                  loggedInUserItem.getEmail(),
                                  WorkflowsMail.generateSubject(workflowItem.getWorkflowName(), workflowItem.getState()),
                                  WorkflowsMail.generateContent(loggedInUserItem.getName(), workflowItem.getWorkflowName(),
                                                                workflowItem.getState(), workflowItem.getId(),
                                                                errorMessageMap));
                    }
                }
            }
            logger.info("Getting workflow results.");
            // Get workflow results
            /*try {
                results = newWorkflow.getWorkflowResults();
            } catch (Exception e) {
                writeJsonError(resp, "Workflow result error.");
                return;
            }*/


            try {
                writeJSON(
                        resp,
                        json("workflowId", workflowId, "workflowName", workflowName,
                            "message", org.json.XML.toJSONObject(results.replaceAll("&", "&amp;")),
                            "errorMessageMap", errorMessageMap.getErrorMessageMapAsJson(),
                            "genericMessageMap", genericMessageMap.getErrorMessageMapAsJson()));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error retrieving workflow results.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error retrieving workflow results.");
                return;
            } finally {
                // send email if long-running...
                if (WorkflowPropertiesHelper.isLongRunning(lastUpdated, workflowItem)) {
                    sendEmail(getEmailAddressDatashopHelp(),
                              loggedInUserItem.getEmail(),
                              WorkflowsMail.generateSubject(workflowItem.getWorkflowName(), workflowItem.getState()),
                              WorkflowsMail.generateContent(loggedInUserItem.getName(), workflowItem.getWorkflowName(),
                                                            workflowItem.getState(), workflowItem.getId(),
                                                            errorMessageMap));
                }
            }
        }

    }

    /**
     * Return the input to a component
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostGetComponentInput(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {

        if (loggedInUserItem != null) {
            int fileIndex = -1;
            int nodeIndex = -1;
            int numLinesToGet = -1;
            try {
                fileIndex = Integer.parseInt(req.getParameter("fileIndex"));
                nodeIndex = Integer.parseInt(req.getParameter("nodeIndex"));
                numLinesToGet = Integer.parseInt(req.getParameter("numLinesToGet"));
            } catch (Exception e) {
                // Ignore parsing failures; use defaults;
            }

            String componentId = WorkflowIfaceHelper.getComponentId(req);

            Long workflowId = (Long) workflowItem.getId();

            String workflowDir = getBaseDir() + "/" + WorkflowFileUtils.getWorkflowsDir(workflowId);

            String componentInput = ComponentHelper.getComponentInputString(workflowDir, componentId,
                    nodeIndex, fileIndex, numLinesToGet);

            try {
                writeJSON(resp,
                        json("success", "true",
                                "workflowId", workflowId,
                                "fileIndex", fileIndex,
                                "componentId", componentId,
                                "nodeIndex", nodeIndex,
                                "componentInput", componentInput));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error retrieving input of component.");
                return;
            } catch (IOException e) {

            }
        }
    }

    /**
     * Return the types of data xml for import component
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     */
    private void doPostGetImportDataTypeXml(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem) {

        Long workflowId = (Long) workflowItem.getId();

        String workflowComponentDir = WorkflowHelper.getWorkflowComponentsDir();

        StringBuilder dataTypeXmlBuf = new StringBuilder();

        BufferedReader br = null;

        try {
            String xmlFilePath = WorkflowFileUtils.getStrictDirFormat(workflowComponentDir) +
                    "CommonResources" + File.separator + "ImportDataTypes.xml";
            logger.debug("xml data types file path: " + xmlFilePath);
            File xmlFile = new File(xmlFilePath);
            br = new BufferedReader(new FileReader(xmlFile));
            while(br.ready()) {
                dataTypeXmlBuf.append(br.readLine());
                if (br.ready()) {
                    dataTypeXmlBuf.append("\n");
                }
            }
            br.close();
        } catch (IOException e) {
            writeJsonError(resp, "Could not read from data types xml file. " + e.toString());
            return;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            writeJSON(resp,
                    json("success", "true",
                            "workflowId", workflowId,
                            "dataTypeXml", dataTypeXmlBuf.toString()));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error retrieving data types xml file.");
            return;
        } catch (IOException e) {

        }
    }

    /**
     * Return the meta data of an input to a component as JSON
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostGetComponentMetadata(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {

        if (loggedInUserItem != null) {
            int fileIndex = -1;
            int nodeIndex = -1;
            try {
                fileIndex = Integer.parseInt(req.getParameter("fileIndex"));
                nodeIndex = Integer.parseInt(req.getParameter("nodeIndex"));
            } catch (Exception e) {
                // Ignore parsing failures; use defaults;
            }

            String componentId = WorkflowIfaceHelper.getComponentId(req);

            Long workflowId = (Long) workflowItem.getId();

            String workflowDir = getBaseDir() + "/" + WorkflowFileUtils.getWorkflowsDir(workflowId);

            String componentMetaData = ComponentHelper.getComponentMetaDataJson(workflowDir, componentId,
                    nodeIndex, fileIndex);

            try {
                writeJSON(resp,
                        json("success", "true",
                                "workflowId", workflowId,
                                "fileIndex", fileIndex,
                                "componentId", componentId,
                                "nodeIndex", nodeIndex,
                                "componentMetaData", componentMetaData));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error retrieving meta data of component.");
                return;
            } catch (IOException e) {

            }
        }
    }

    /**
     * Get the component states.
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostGetComponentStates(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {
        if (workflowItem != null && loggedInUserItem != null) {
            // Get component instance states, if they exist.
            // If the workflow completed successfully, then no instances will exist in the queue.
            // The instance tables are indexed by workflow item.
            List<WorkflowComponentInstanceItem> wfComponentInstanceItems = wciDao.findByWorkflow(workflowItem);
            JSONObject jsonObj = new JSONObject();
            JSONObject warningsObj = new JSONObject();
            if (wfComponentInstanceItems != null) {
                for (WorkflowComponentInstanceItem wfciItem : wfComponentInstanceItems) {
                    Hibernate.initialize(wfciItem.getComponentName());
                    Hibernate.initialize(wfciItem.getState());
                    try {

                        // For backwards compatibility with older workflows, do not
                        // put the COMPLETED state into the json object, as >no state< is
                        // equivalent to COMPLETED only in the UI. Added new state, also: completed_warn.

                        if (wfciItem.getState() != null
                                && wfciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                            jsonObj.put(wfciItem.getComponentName().toString(), wfciItem.getState());

                            warningsObj.put(wfciItem.getComponentName().toString(), wfciItem.getWarnings());
                        } else if (wfciItem.getState() != null
                            && !wfciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)) {
                            jsonObj.put(wfciItem.getComponentName().toString(), wfciItem.getState());
                        } else {
                            // The component completed successfully without warnings.
                        }
                    } catch (JSONException e) {
                        writeJsonError(resp, "Could not fetch component status for component ("
                            + wfciItem.getId() + ")");
                    }
                }
            }

            try {
                writeJSON(resp, json("success", true, "componentStates", jsonObj, "componentWarnings", warningsObj));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error fetching workflow state.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error fetching workflow state.");
            }
        }
    }

    public void doPostCreateAnnotation(HttpServletRequest req, HttpServletResponse resp,
            WorkflowItem workflowItem, UserItem loggedInUserItem) throws IOException{

        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            // Create the new annotation
            WorkflowAnnotationItem wfAnnotation = new WorkflowAnnotationItem();
            wfAnnotation.setWorkflow(workflowItem);
            wfAnnotation.setText("");// noteText);
            wfAnnotation.setLastUpdated(new Date());

            WorkflowAnnotationDao wad = DaoFactory.DEFAULT.getWorkflowAnnotationDao();
            wad.saveOrUpdate(wfAnnotation);

            // Return the id the db created for the annotation
            try {
                JSONObject annotationIdJson = json("annotation_id", wfAnnotation.getId());
                writeJSON(resp, annotationIdJson);
            } catch (Exception e) {
                logger.error("Coudln't write response createannotation");
            }
        }
    }

    /**
     * Extract the file id from the request and use it to process the file.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostUseExistingFile(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            Long workflowId = workflowItem.getId();
            String workflowName = workflowItem.getWorkflowName();

            try {
                if (req.getParameter("wfFileId") != null && req.getParameter("componentId") != null) {
                    String fileIdStr = (String) req.getParameter("wfFileId");
                    // This uses the workflow_file table (i.e., FileItem)
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    Integer fileId = null;
                    if (fileIdStr != null && fileIdStr.matches("\\d+")) {
                        fileId = Integer.parseInt(fileIdStr);
                    }

                    if (fileId != null) {

                        JSONArray columnNameArray = new JSONArray();
                        WorkflowFileItem fileItem = wfFileDao.get(fileId);
                        String datasetLink = new String();
                        String datasetName = new String();
                        String projectId = new String();
                        String projectName = new String();

                        if (fileItem != null) {
                            req = workflowImportHelper.handleDatasetFile(workflowFileHelper, workflowHelper, req, resp, workflowItem, fileItem,
                                    WorkflowFileUtils.getStrictDirFormat(getBaseDir()), WorkflowHelper.getWorkflowComponentsDir(), loggedInUserItem);

                            if (req.getAttribute("columnHeaders") != null
                                    && !((Map<String, Integer>) req.getAttribute("columnHeaders")).isEmpty()) {
                                WorkflowFileHelper workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
                                try {
                                    columnNameArray = workflowFileHelper.getColumnsAsJson(req, resp);
                                } catch (JSONException jsonE) {
                                    writeJsonError(resp, "Error retrieving header data.");
                                    return;
                                }
                            }

                            if (req.getParameter("fileDatasetId") != null
                                    && !((String) req.getParameter("fileDatasetId")).isEmpty()) {
                                datasetLink = (String) req.getParameter("fileDatasetId");

                                if (datasetLink.trim().matches("[0-9]+")) {
                                    Integer datasetId = Integer.parseInt(datasetLink.trim());
                                    DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                                    DatasetItem dsItem = dsDao.get(datasetId);
                                    datasetName = WorkflowFileUtils.htmlEncode(dsItem.getDatasetName());
                                    if (dsItem != null && dsItem.getProject() != null) {
                                        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                                        ProjectItem projectItem = projectDao.get((Integer) dsItem.getProject().getId());
                                        projectName = projectItem.getProjectName();
                                        projectId = projectItem.getId().toString();
                                    }
                                }
                            }

                            String message = "File successfully added to workflow.";
                            writeJSON(
                                    resp,
                                    json("success", "true", "workflowId", workflowId, "workflowName", workflowName,
                                            "fileId", req.getAttribute("fileId"),
                                            "fileName",  req.getAttribute("fileName"), "fileIndex", req.getAttribute("fileIndex"),
                                            "fileLabel", req.getAttribute("fileLabel"),
                                            "datasetLink", datasetLink,
                                            "datasetName", WorkflowFileUtils.htmlEncode(datasetName),
                                            "projectName", WorkflowFileUtils.htmlEncode(projectName), "projectId", projectId,
                                            "componentId", req.getParameter("componentId"),
                                            "message", message, "columnHeaders",
                                            columnNameArray));
                        }
                    } else {
                        String message = "File id was not found.";
                        writeJSON(
                                resp,
                                json("success", "false", "workflowId", workflowId, "workflowName", workflowName, "message",
                                        message));
                    }
                    return;

                } else {
                    String message = "File could not be retrieved.";
                    writeJSON(
                            resp,
                            json("success", "false", "workflowId", workflowId, "workflowName", workflowName, "message",
                                    message));
                    return;
                }

            } catch (JSONException exception) {
                writeJsonError(resp, "Error retrieving file meta-data.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error retrieving file meta-data.");
                return;
            }
        }

    }

    /**
     * Attach the file to a dataset.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostAttachFileToDataset(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && (loggedInUserItem.getAdminFlag() || workflowItem.getOwner().getId().equals(loggedInUserItem.getId()))) {

            try {
                if (req.getParameter("wfDatasetId") != null && req.getParameter("componentId") != null) {
                    String componentId = (String) req.getParameter("componentId");
                    String datasetIdStr = (String) req.getParameter("wfDatasetId");
                    String fileIdStr = (String) req.getParameter("wfFileId");
                    Long workflowId = workflowItem.getId();
                    String workflowName = workflowItem.getWorkflowName();

                    Integer datasetId = null;
                    Integer fileId = null;

                    if (datasetIdStr != null && datasetIdStr.matches("\\d+")) {
                        datasetId = Integer.parseInt(datasetIdStr);
                    }

                    if (fileIdStr != null && fileIdStr.matches("\\d+")) {
                        fileId = Integer.parseInt(fileIdStr);
                    } else {
                        String message = "You must upload a file before you can add the file to a dataset.";
                        writeJSON(
                                resp,
                                json("success", "false", "workflowId", workflowId, "workflowName", workflowName, "message",
                                        message));
                        return;
                    }

                    // Attempt to attach dataset and respond with custom JSON object.
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                    ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                    ComponentFilePersistenceDao compFilePersistenceDao = DaoFactory.DEFAULT.getComponentFilePersistenceDao();

                    DatasetItem datasetItem = null;

                    Boolean canAttachToDataset = false;

                    // We know datasetItem isn't null because authLevel isn't null, but
                    // someone could change getAuthLevel in the future so check anyway.
                    if (datasetId != null) {
                        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
                        datasetItem = dsDao.get(datasetId);
                        // If datasetItem is null, authLevel becomes null
                        String authLevel = authDao.getAuthLevel(loggedInUserItem, datasetItem);
                        if (loggedInUserItem.getAdminFlag() || (datasetItem != null && authLevel != null
                                // We won't allow file attachment to public datasets unless
                                // the user has specific edit/admin access to the project.
                                // Leave out: authDao.isPublic((Integer) datasetItem.getProject().getId())
                                && (authLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_EDIT)
                                        || authLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_ADMIN)))) {
                            canAttachToDataset = true;
                        }
                    }

                    WorkflowFileItem fileItem = wfFileDao.get(fileId);
                    try { Boolean canAccessFile = false; if (fileItem != null && fileItem.getOwner() != null) { canAccessFile = loggedInUserItem.getAdminFlag() == true || loggedInUserItem.getId().equals(fileItem.getOwner().getId()); }
                        if (canAttachToDataset
                                && canAccessFile) {

                            String message = null;
                            // This variable is used later. Only two cases can make success = false:
                            // 1. A file exists where one should not exist   2. No ComponentFileItem found
                            Boolean success = true;
                            if (datasetItem != null) {

                                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                                String projectName = new String();
                                String projectId = new String();
                                // This first part is simply to attach the file to a dataset, if requested.
                                String attachedPath = FileUtils.cleanForFileSystem(datasetItem.getDatasetName());
                                File origFile = new File(WorkflowFileUtils.sanitizePath(
                                        getBaseDir() + "/" + fileItem.getFilePath() + "/" + fileItem.getFileName()));
                                File attachedFile = new File(WorkflowFileUtils.sanitizePath(
                                    getBaseDir() + "/" + attachedPath + "/" + fileItem.getFileName()));
                                String wholePath = WorkflowFileUtils.sanitizePath(getBaseDir() + "/" + attachedPath);

                                File fileParent = attachedFile.getParentFile();
                                String origFileName = attachedFile.getName();
                                String newFileName = origFileName;
                                if (attachedFile.exists()) {
                                    // check that file with this name does not already exist.
                                    // if it does start incrementing adding _1, _2, to the end
                                    // of the file until a name is found that doesn't exist.
                                    List<String> fileList = Arrays.asList(fileParent.list());
                                    int extensionIndex = origFileName.lastIndexOf(".");
                                    String fileExt = "";
                                    String fileName = "";
                                    if (extensionIndex < 0) {
                                        fileName = origFileName;
                                    } else if (extensionIndex == 0) {
                                        fileExt = origFileName.substring(0, origFileName.length());
                                    } else {
                                        fileExt = origFileName.substring(extensionIndex);
                                        fileName = origFileName.substring(0, extensionIndex);
                                    }
                                    int i = 0;

                                    List<FileItem> existingFiles = fileDao.find(attachedPath, newFileName);

                                    while (fileList.contains(newFileName) && !existingFiles.isEmpty()) {
                                        i++;
                                        newFileName = fileName + "_" + i + fileExt;
                                        existingFiles = fileDao.find(attachedPath, newFileName);
                                    }
                                }

                                File checkDir = new File(wholePath);
                                if (!checkDir.exists()) {
                                    if (checkDir.mkdirs()) {
                                        updateFilePermissions(checkDir, "chmod 775");
                                        logger.trace("Created " + wholePath + " with (on Linux, permissions 775)");
                                    }
                                }
                                File newFile = new File(wholePath, newFileName);

                                // Attach the file to a dataset accessible via the Dataset Info -> Files page
                                // Insert a new ds_file row. It will show up in the Dataset Info -> Files page.
                                FileItem fileItem2 = new FileItem();

                                FileUtils.copyFile(origFile, newFile);
                                fileItem2.setFileName(newFileName);
                                fileItem2.setFilePath(attachedPath);
                                fileItem2.setAddedTime(new Date());
                                fileItem2.setFileType(fileItem.getFileType());
                                fileItem2.setFileSize(newFile.length());
                                fileItem2.setOwner(loggedInUserItem);
                                fileItem2.setTitle("Workflow: " + workflowItem.getWorkflowName()
                                    + ", Component: " + componentId + ", Type: " + fileItem.getTitle());
                                fileItem2.addDataset(datasetItem);
                                fileDao.saveOrUpdate(fileItem2);

                                // Attach the file to the dataset via file_dataset_map.
                                // Because file_dataset_map has never been mapped to the DAO, initialize.
                                Hibernate.initialize(datasetItem.getFiles());

                                datasetItem.addFile(fileItem2);
                                dsDao.saveOrUpdate(datasetItem);

                                workflowHelper.logWorkflowComponentUserAction(loggedInUserItem, workflowItem, datasetItem,
                                        componentId, null, null, null,
                                        null, null, fileItem2, WorkflowHelper.LOG_ATTACH_FILE_TO_DATASET, "");


                                // Update references in the component_file table. They will be written to
                                // component_file_persistence on "save".
                                List<ComponentFileItem> compFileItems = compFileDao.findByFile(fileItem);
                                if (!compFileItems.isEmpty()) {
                                    ComponentFileItem cfItem = compFileItems.get(0);
                                    cfItem.setDataset(datasetItem);
                                    compFileDao.saveOrUpdate(cfItem);

                                    ComponentFilePersistenceItem cfpItem = compFilePersistenceDao
                                        .findByWorkflowAndFile(workflowItem, cfItem.getFile());
                                    if (cfpItem == null) {
                                        ComponentFilePersistenceItem newCfpi = new ComponentFilePersistenceItem(
                                                cfItem.getWorkflow(), cfItem.getComponentId(), cfItem.getDataset(), cfItem.getFile());
                                        compFilePersistenceDao.saveOrUpdate(newCfpi);
                                    } else {
                                        Hibernate.initialize(cfpItem);
                                        cfpItem.setComponentId(cfItem.getComponentId());
                                        cfpItem.setDataset(datasetItem);
                                        compFilePersistenceDao.saveOrUpdate(cfpItem);
                                    }

                                } else {
                                    message =  "The workflow file could not be found. Please contact help if this problem persists.";
                                    success = false;
                                }

                                if (datasetItem != null && datasetItem.getProject() != null) {
                                    ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                                    ProjectItem projectItem = projectDao.get((Integer) datasetItem.getProject().getId());
                                    projectName = projectItem.getProjectName();
                                    projectId = projectItem.getId().toString();
                                }


                                if (success) {
                                    message = "File successfully attached to the dataset " + datasetItem.getDatasetName();
                                    writeJSON(resp, json("success", "true", "workflowId", workflowId, "workflowName", workflowName,
                                                "fileId", fileItem.getId().toString(),
                                                "fileName", fileItem.getFileName(), "fileIndex", "0",
                                                "fileLabel", fileItem.getTitle(),
                                                "datasetName", WorkflowFileUtils.htmlEncode(datasetItem.getDatasetName()),
                                                "datasetId", datasetItem.getId().toString(),
                                                "projectName", WorkflowFileUtils.htmlEncode(projectName), "projectId", projectId,
                                                "componentId", componentId, "message", message));
                                } else {
                                    if (message == null) {
                                        message = "An error prevented the file from being attached. Please contact help if this problem persists.";
                                    }
                                    writeJSON(resp, json("success", "false", "workflowId", workflowId, "workflowName", workflowName, "message",
                                                    message));
                                }
                                return;
                            }

                        } else {
                            String message = "You do not have adequate permissions to attach this file. Please contact help if this problem persists";
                            writeJSON(resp, json("success", "false", "workflowId", workflowId, "workflowName", workflowName, "message", message));
                            return;
                        }

                        String message = "Could not attach upload file to dataset.";
                        writeJSON(resp, json("success", "false", "workflowId", workflowId, "workflowName", workflowName, "message", message));
                        return;

                    } catch (JSONException e) {
                        logger.error("Could not create JSON while attaching file to dataset.");
                    } catch (IOException e) {
                        logger.error("Could not copy file (" + fileId + ") to dataset.");
                    }
                }

            } catch (JSONException exception) {
                writeJsonError(resp, "An error prevented the file from being attached. Please contact help if this problem persists.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "An error prevented the file from being attached. Please contact help if this problem persists.");
                return;
            }
        }

    }


    /**
     * Handle the file upload once it has finished. This method does not check file permissions. Do not call it outside
     * a secure block of code.
     *
     * @param req the HTTP Servlet Request
     * @param resp the HTTP Servlet Response
     * @param loggedInUserItem the logged in user item
     * @param workflowItem
     */
    private void doPostFileUpload(HttpServletRequest req, HttpServletResponse resp, UserItem loggedInUserItem, WorkflowItem workflowItem) {

        if (workflowItem != null && loggedInUserItem != null) {

            String workflowName = workflowItem.getWorkflowName();
            try {
                JSONArray columnNameArray = new JSONArray();

                String preferredDatasetLink = new String();
                if (req.getAttribute("fileName") != null && !((String) req.getAttribute("fileName")).isEmpty()
                        && req.getAttribute("componentId") != null
                        && !((String) req.getAttribute("componentId")).isEmpty()) {

                    if (req.getAttribute("columnHeaders") != null
                            && !((Map<String, Integer>) req.getAttribute("columnHeaders")).isEmpty()) {

                        WorkflowFileHelper workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
                        try {
                            columnNameArray = workflowFileHelper.getColumnsAsJson(req, resp);
                        } catch (JSONException jsonE) {
                            writeJsonError(resp, "Error retrieving header data.");
                            return;
                        }
                    }

                    // We do not want to set datasetLink to the complimentary attribute
                    // at this phase because we only want to suggest a preferred dataset to attach
                    // while giving the user the option to attach the file to any (or no) dataset.
                    if (req.getAttribute("datasetLink") != null
                            && !((String) req.getAttribute("datasetLink")).isEmpty()) {
                        // Workflow is attached to dataset so pass back the dataset id
                        preferredDatasetLink = (String) req.getAttribute("datasetLink");
                    }

                    String message = "File uploaded successfully.";
                    String fileId = (String) req.getAttribute("fileId");
                    String fileName = (String) req.getAttribute("fileName");

                    writeJSON(
                            resp,
                            json("success", "true", "workflowId", workflowItem.getId(), "workflowName", workflowName,
                                    "fileId", fileId, "fileName", fileName,
                                    "fileIndex", req.getAttribute("fileIndex"),
                                    "fileLabel", req.getAttribute("fileLabel"),
                                    "datasetLink", "",
                                    "datasetName", "",
                                    "preferredDatasetLink", preferredDatasetLink,
                                    "componentId", req.getAttribute("componentId"),
                                    "message", message, "columnHeaders", columnNameArray));
                    return;
                } else {
                    String message = "File could not be uploaded.";
                    writeJSON(
                            resp,
                            json("success", "false", "workflowId", workflowItem.getId(), "workflowName", workflowName,
                                    "message", message));
                    return;
                }

            } catch (JSONException exception) {
                writeJsonError(resp, "Error importing file meta-data.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error importing file meta-data.");
                return;
            }
        }
    }

    private JSONObject processDirtyBits(HttpServletRequest req, HttpServletResponse resp,
        WorkflowItem workflowItem, Element workflowRootElement,
            UserItem loggedInUserItem, Boolean saveFlag) {

        WorkflowAnnotationHelper.removeDeletedAnnotationsFromDB(workflowItem, workflowRootElement, saveFlag);

        ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
        JSONObject dirtyBits = new JSONObject();
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            if (req.getParameter("dirtyBits") != null) {
                String dirtyBitsString = req.getParameter("dirtyBits");

                try {
                    dirtyBits = new JSONObject(dirtyBitsString.trim());

                    logger.trace("Check component instance.");
                    Iterator<String> keys = dirtyBits.keys();
                    List<String> deletedComponents = new ArrayList<String>();
                    if (dirtyBits.length() != 0 && keys != null && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                        logger.trace("Dirty bits found.");

                        List<WorkflowComponentInstanceItem> componentInstances = wciDao.findByWorkflow(workflowItem);
                        // Remove components no longer in the workflow.
                        if (componentInstances != null) {
                            for (Iterator<WorkflowComponentInstanceItem> wciiIter = componentInstances.iterator(); wciiIter
                                    .hasNext();) {
                                Integer wciiId = (Integer) ((WorkflowComponentInstanceItem) wciiIter.next()).getId();
                                WorkflowComponentInstanceItem wcii = wciDao.get(wciiId);

                                // Set all component instances to state = new,
                                // except for running instances
                                Boolean foundInLatest = false;
                                if (!wcii.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {

                                    // Does the digraphDoc (workflow in GUI) contain this component still?
                                    if (workflowRootElement != null) {
                                        // Look through all descendants of the latest workflow xml to detect
                                        // if the queued component still exists.
                                        for (Iterator<Element> iter = workflowRootElement
                                                .getDescendants(new ElementFilter()); iter.hasNext();) {
                                            Element desc = iter.next();
                                            if (desc.getName().equalsIgnoreCase("component_id")
                                                    && desc.getText().trim().equalsIgnoreCase(
                                                        wcii.getComponentName())) {
                                                foundInLatest = true;
                                            }
                                        }

                                        if (!foundInLatest && saveFlag) {

                                            // The item in the queue no longer exists
                                            // in the workflow so we can
                                            // delete it from the database.

                                            // From the component_file table
                                            workflowFileHelper.updateFileMappings(compFileDao, workflowItem, wcii.getComponentName(), true, getBaseDir());

                                            // From the workflow_component_instance table
                                            wciDao.delete(wcii);

                                        }
                                    }
                                }
                            }
                        }

                        List<ComponentFileItem> cfItems = compFileDao.findByWorkflow(workflowItem);
                        for (Iterator<ComponentFileItem> cfIterator = cfItems.iterator(); cfIterator
                                .hasNext();) {
                            Long wfiId = (Long) ((ComponentFileItem) cfIterator.next()).getId();
                            ComponentFileItem cfItem = compFileDao.get(wfiId);

                            // Set all component instances to state = new,
                            // except for running instances
                            Boolean foundInLatest = false;
                            if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {

                                // Does the digraphDoc (workflow in GUI) contain this component still?
                                if (workflowRootElement != null) {
                                    // Look through all descendants of the latest workflow xml to detect
                                    // if the queued component still exists.
                                    for (Iterator<Element> iter = workflowRootElement
                                            .getDescendants(new ElementFilter()); iter.hasNext();) {
                                        Element desc = iter.next();
                                        if (desc.getName().equalsIgnoreCase("component_id")
                                                && desc.getText().trim().equalsIgnoreCase(
                                                        cfItem.getComponentId())) {
                                            foundInLatest = true;
                                        }
                                    }

                                    if (!foundInLatest && saveFlag) {

                                        // The item in the queue no longer exists
                                        // in the workflow so we can
                                        // delete it from the database.

                                        // From the component_file table
                                        workflowFileHelper.updateFileMappings(compFileDao, workflowItem, cfItem.getComponentId(), true, getBaseDir());


                                    }
                                }
                            }
                        }

                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            if (deletedComponents.contains(key)) {
                                continue;
                            }
                            String value = (String) dirtyBits.get(key);

                            WorkflowComponentInstanceItem componentInstance = wciDao.findByWorkflowAndId(workflowItem,
                                    key);
                            if (componentInstance != null) {
                                // This component was found in queue
                                logger.trace("Found component instance: " + key);

                                Integer wcInstanceId = (Integer) componentInstance.getId();
                                WorkflowComponentInstanceItem wcInstanceItem = wciDao.get(wcInstanceId);

                                if (!wcInstanceItem.getState().equalsIgnoreCase(
                                            WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {
                                    logger.trace("Setting component to 'new' state: "
                                            + wcInstanceItem.getComponentName());
                                    wcInstanceItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);

                                    workflowImportHelper.triggerDirtyBit(wcInstanceItem, value);

                                    wciDao.saveOrUpdate(wcInstanceItem);
                                }

                                // Set remaining components to state = new,
                                // except for running or completed components
                                for (WorkflowComponentInstanceItem wcii : wciDao.findByWorkflow(workflowItem)) {
                                    Hibernate.initialize(wcii);
                                    // Set all component instances to state = new, except for running instances
                                    if (!wcii.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)
                                            && !wcii.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)
                                            && !wcii.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                                            && !wcii.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                                        // Set state to new if not running
                                        wcii.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                        logger.trace("Setting component to 'new' state: " + wcii.getComponentName());
                                        wciDao.saveOrUpdate(wcii);
                                    } else {
                                        logger.trace("Running component '" + wcii.getComponentName()
                                                + "' will be allowed to complete.");
                                    }
                                }

                            } else {
                                // This component does not exist in queue
                                logger.trace("New component instance: " + key);

                                WorkflowComponentInstanceItem wcInstanceItem = new WorkflowComponentInstanceItem();
                                workflowImportHelper.triggerDirtyBit(wcInstanceItem, value);

                                wcInstanceItem.setWorkflow(workflowItem);
                                wcInstanceItem.setComponentName(key);
                                wcInstanceItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                wciDao.saveOrUpdate(wcInstanceItem);
                            }
                        }
                        return dirtyBits;
                    }
                    return new JSONObject();
                } catch (JSONException exception) {
                    writeJsonError(resp, "Error saving workflow.");
                    return dirtyBits;
                }
            }
        }
        return dirtyBits;
    }

    /** Workflow editor functionality. */

    private void doPostRequestComponentSpecificOptions(HttpServletRequest req, HttpServletResponse resp,
            WorkflowContext workflowContext, WorkflowItem workflowItem,
                DatasetItem datasetItem, UserItem loggedInUserItem) {
        if (workflowItem != null && loggedInUserItem != null) {
            Long workflowId = workflowItem.getId();
            String componentId = WorkflowIfaceHelper.getComponentId(req);
            String componentType = WorkflowIfaceHelper.getComponentType(req);
            String componentName = WorkflowIfaceHelper.getComponentName(req);
            String projectName = new String();
            String projectId = new String();

            // Update wfc adjacency table.
            ConnectionHelper.updateAdjacencyList(getBaseDir(), workflowItem, loggedInUserItem);

            String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                loggedInUserItem, getBaseDir(), workflowItem, componentId);
            if (componentAccessLevel == null) {
                writeJsonError(resp, "Access to this component's options are not allowed.");
                return;
            }

            Integer datasetId = null;
            if (datasetItem != null) {
                datasetId = (Integer) datasetItem.getId();
            }

            // Get the JSON string.
            String jsonString = req.getParameter("digraphObject");

            if (jsonString != null && !jsonString.isEmpty()) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonString);
                } catch (JSONException exception) {
                    writeJsonError(resp, "Error getting component options.");
                    return;
                }

                Element digraphDoc = null;
                try {
                    digraphDoc = WorkflowXmlUtils.convertJsonToXML(jsonObject);
                } catch (JSONException e) {
                    writeJsonError(resp, "Error converting workflow to XML.");
                    return;
                } catch (JDOMException e) {
                    writeJsonError(resp, "Error parsing workflow.");
                    return;
                } catch (UnsupportedEncodingException e) {
                    writeJsonError(resp, "Unsupported encoding.");
                    return;
                } catch (IOException e) {
                    writeJsonError(resp, "Error opening workflow.");
                    return;
                }

                if (digraphDoc != null) {
                    String workflowXml = null;
                    try {
                        workflowXml = WorkflowXmlUtils.getElementAsString(digraphDoc);
                    } catch (IOException e) {
                        logger.error("Could not convert workflow XML to string.");
                    }
                    Boolean saveFlag = false;
                    processDirtyBits(req, resp, workflowItem, digraphDoc, loggedInUserItem, saveFlag);
                    workflowItem.setWorkflowXml(workflowXml);
                }
            }

            if (componentId != null && componentName != null && componentType != null) {

                String componentOptionsXml = componentHelper.getAdvancedComponentOptions(workflowItem, getBaseDir(),
                        commonSchemasDir + "TableTypes.xsd", loggedInUserItem, componentId,
                        componentName, WorkflowHelper.getWorkflowComponentsDir() );

                try {
                    WorkflowFileItem workflowFileItem = null;
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                    String datasetLink = "";
                    String fileId = "";
                    String datasetName = "";

                    List<ComponentFileItem> compFileItems = compFileDao.findImportByComponent(workflowItem, componentId);
                    ComponentFileItem compFileItem = null;
                    if (!compFileItems.isEmpty()) {
                        compFileItem = compFileDao.get((Long) compFileItems.get(0).getId());
                        workflowFileItem = wfFileDao.get((Integer) compFileItem.getFile().getId());
                        if (compFileItem.getDataset() != null && workflowFileItem != null) {
                            datasetLink = ((Integer)compFileItem.getDataset().getId()).toString();
                            fileId = ((Integer)workflowFileItem.getId()).toString();
                            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                            DatasetItem dsItem = dsDao.get((Integer) compFileItem.getDataset().getId());
                            if (dsItem != null) {
                                datasetName = dsItem.getDatasetName();
                            }
                        }
                    }

                    JSONArray projectArray = new JSONArray();
                    JSONArray datasetArray = new JSONArray();
                    JSONArray myProjectArray = new JSONArray();
                    JSONArray myDatasetArray = new JSONArray();
                    JSONArray fileArray = new JSONArray();

                    if (componentType.equalsIgnoreCase("import") || componentName.equalsIgnoreCase(AbstractComponent.IMPORT_COMPONENT_NAME)) {
                        // If this is an import file, the workflow_file.name value should match
                        // the component name, ignoring case-sensitivity and non-alphanumeric characters.
                        JSONObject jsonInfo = getMyViewableDatasets(req, workflowContext, workflowItem, loggedInUserItem, componentName);
                        if (jsonInfo != null) {
                            projectArray = jsonInfo.getJSONArray("projectArray");
                            datasetArray = jsonInfo.getJSONArray("datasetArray");
                            fileArray = jsonInfo.getJSONArray("fileArray");
                        }
                        // We also want the user to be allowed to attach the file to one of their datasets.
                        JSONObject jsonInfo2 = getMyEditableDatasets(req, workflowContext, loggedInUserItem, datasetId);
                        if (jsonInfo2 != null) {
                            myProjectArray = jsonInfo2.getJSONArray("myProjectArray");
                            myDatasetArray = jsonInfo2.getJSONArray("myDatasetArray");
                        }
                    }

                    String citation = "";
                    String infoDiv = "";
                    if (componentName != null) {
                        WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
                        WorkflowComponentItem workflowComponentItem = workflowComponentDao.findByName(componentName);
                        if (workflowComponentItem != null) {
                            // Get source code link (citation)
                            citation = workflowComponentItem.getCitation();
                            // Get component info (component help panel with info about options, inputs, outputs, etc)
                            infoDiv = ComponentHelper.getComponentInfoDiv(workflowComponentItem);
                        }
                    }

                    if (datasetLink.trim().matches("[0-9]+")) {
                        Integer dsLinkId = Integer.parseInt(datasetLink.trim());
                        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                        DatasetItem dsItem = dsDao.get(dsLinkId);
                        if (dsItem != null && dsItem.getProject() != null) {
                            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                            ProjectItem projectItem = projectDao.get((Integer) dsItem.getProject().getId());
                            projectName = projectItem.getProjectName();
                            projectId = projectItem.getId().toString();
                        }
                    }

                    writeJSON(
                            resp,
                            json("datasetId", datasetId,
                                    "projectArray", projectArray, "datasetArray", datasetArray,
                                    "myProjectArray", myProjectArray, "myDatasetArray", myDatasetArray, "fileArray", fileArray,
                                    "datasetLink", datasetLink, "datasetName", WorkflowFileUtils.htmlEncode(datasetName),
                                    "projectName", WorkflowFileUtils.htmlEncode(projectName), "projectId", projectId,
                                    "fileId", fileId,
                                    "workflowId", workflowId, "componentId", componentId,
                                    "componentName", componentName, "componentType", componentType,
                                    "componentCitation", citation, "componentInfo", infoDiv,
                                    "componentOptions", org.json.XML.toJSONObject(componentOptionsXml.replaceAll("&", "&amp;"))));

                    return;
                } catch (JSONException exception) {
                    writeJsonError(resp, "Error retrieving advanced options.");
                    return;
                } catch (IOException e) {
                    writeJsonError(resp, "Error retrieving advanced options.");
                }
            } else {
                writeJsonError(resp, "No component options exist for this component.");
                return;
            }
        }
    }

    /**
     * Helper method to get the list of projects and datasets that user has 'edit' access to.
     * These are datasets that the user can attach a workflow to.
     * The assumption is that this is will not be called with a null workflow or user item.
     *
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param workflowContext WorkflowContext used to determine if info is already cached
     * @param workflowItem WorkflowItem the current workflow
     * @param loggedInUserItem UserItem the current user
     */
    private void doPostRequestProjectsAndDatasets(HttpServletRequest req, HttpServletResponse resp,
                                                  WorkflowContext workflowContext, WorkflowItem workflowItem,
                                                  UserItem loggedInUserItem) {

        try {
            JSONArray myProjectArray = new JSONArray();
            JSONArray myDatasetArray = new JSONArray();

            // User can only attach WF to a dataset they have 'edit' access to.
            JSONObject jsonInfo = null;

            if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                    && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                jsonInfo = getMyEditableDatasets(req, workflowContext,
                                                        loggedInUserItem, null);
            }

            if (jsonInfo != null) {
                myProjectArray = jsonInfo.getJSONArray("myProjectArray");
                myDatasetArray = jsonInfo.getJSONArray("myDatasetArray");
            }

            writeJSON(
                      resp,
                      json("myProjectArray", myProjectArray, "myDatasetArray", myDatasetArray));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error retrieving projects and datasets.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "Error retrieving projects and datasets.");
        }
    }

    private JSONObject createOrGetWorkflowFileJSON(String fileType, String componentFileType, String filePath, Integer fileIndex,
                                                    WorkflowItem workflowItem, DatasetItem datasetItem,
                                                    ProjectItem project, UserItem userItem, Boolean isCachedExport) {
        if (userItem == null || !userItem.getId().equals(UserItem.SYSTEM_USER)) {
            return null;
        }
        WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
        JSONObject fileObject = workflowFileHelper.getExportInfoObject(filePath);
        if (fileObject != null) {
            return fileObject;
        }

        if (filePath != null) {

            File newFile = new File(WorkflowFileUtils.sanitizePath(filePath));
            Integer fileId = WorkflowFileHelper.createOrGetWorkflowFile(workflowItem, userItem, getBaseDir(), newFile, fileType, fileIndex.toString(), datasetItem, null, false);
            if (fileId != null) {
                WorkflowFileItem fileItem = wfFileDao.get(fileId);
                Boolean isFileTypeDescendant = false;
                // Extract the sample name and use it in the displayed file name (vs. actual file name).
                if (fileItem != null && fileItem.getFileName() != null) {
                    String fileName = fileItem.getFileName();
                    String actualFileName = fileName;

                    if (fileType != null) {
                        Pattern patternTx = Pattern.compile("ds[0-9]+_tx_(.*)_[0-9]+_[0-9]+_[0-9]+_[0-9]+\\.[a-z]+");

                        Pattern patternStuStep = Pattern
                                .compile("ds[0-9]+_student_step_(.*)_[0-9]+_[0-9]+_[0-9]+_[0-9]+\\.[a-z]+");
                        Pattern patternStuProb = Pattern
                                .compile("ds[0-9]+_student_problem_(.*)_[0-9]+_[0-9]+_[0-9]+_[0-9]+\\.[a-z]+");
                        Pattern pattern = null;

                        if (fileType.equalsIgnoreCase("transaction")
                                && fileName.matches("ds[0-9]+_tx_.*_[0-9]+_[0-9]+_[0-9]+_[0-9]+\\.[a-z]+")) {
                            pattern = patternTx;
                            isFileTypeDescendant = true;
                        } else if (fileType.equalsIgnoreCase("student-step")
                                && fileName.matches("ds[0-9]+_student_step_.*_[0-9]+_[0-9]+_[0-9]+_[0-9]+\\.[a-z]+")) {
                            pattern = patternStuStep;
                            isFileTypeDescendant = true;
                        } else if (fileType.equalsIgnoreCase("student-problem")
                                && fileName.matches("ds[0-9]+_student_problem_.*_[0-9]+_[0-9]+_[0-9]+_[0-9]+\\.[a-z]+")) {
                            pattern = patternStuProb;
                            isFileTypeDescendant = true;
                        } else {
                            isFileTypeDescendant = ComponentHierarchyHelper.isDescendant(commonSchemasDir
                                    + "/TableTypes.xsd", fileType, componentFileType);
                            pattern = null;
                        }

                        if (pattern != null) {

                            String sampleName = null;
                            if (fileType != null && pattern != null && isCachedExport) {
                                Matcher m = pattern.matcher(fileName);
                                if (!m.matches()) {
                                    // no matches
                                } else {
                                    sampleName = m.group(1);
                                }
                                if (sampleName != null) {
                                    fileName = "Sample: " + sampleName + " - " + fileType;
                                }
                            }
                        }
                    } else {
                        fileType = "file";
                    }


                    try {
                        fileObject = new JSONObject();
                        fileObject.put("id", fileId);
                        fileObject.put("fileName", fileName);
                        fileObject.put("actualFileName", actualFileName);
                        fileObject.put("wfFileType", fileType.toLowerCase());
                        fileObject.put("outputIndex", fileItem.getDescription());
                        fileObject.put("owner", fileItem.getOwner().getId());
                        fileObject.put("mimeType", fileItem.getFileType());
                        fileObject.put("fileSize", fileItem.getFileSize());
                        fileObject.put("datasetId", datasetItem.getId());
                        fileObject.put("projectId", project.getId());
                        // To facilitate searching...
                        fileObject.put("datasetName", WorkflowFileUtils.htmlEncode(datasetItem.getDatasetName()));
                        fileObject.put("projectName", WorkflowFileUtils.htmlEncode(project.getProjectName()));
                        fileObject.put("isFileTypeDescendant", isFileTypeDescendant.toString());

                        workflowFileHelper.addExportInfoObject(filePath, fileObject);

                    } catch (JSONException e) {
                        logger.error("Could not create JSON object from DataShop export file (" + fileId + ")");
                    }
                }
            } else {
                return null;
            }
        }

        return fileObject;
    }


    /** Data Preview functionality. */

    /**
     * Get debug info for a given workflow.
     * @param req the request item
     * @param resp the response item
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user item
     */
    private void doPostRequestDebugInfo(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {

        if (workflowItem != null && loggedInUserItem != null
                && (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())
                || loggedInUserItem.getAdminFlag())) {

            Long workflowId = workflowItem.getId();
            // To utilize existing JS functions, we need to encapsulate
            // the debug info into a specifically formatted JSON array
            JSONObject jsonTextData = new JSONObject();

            String componentId = req.getParameter("componentId");
            if (componentId != null && !componentId.matches(STRICT_CHARS)) { componentId = ""; }

            String textFilter = req.getParameter("textFilter");
            // Parent dir of log files (WorkflowComponent.log and *.wfl files)
            String wfcLogDirPath = getBaseDir() + "/" + WorkflowFileUtils.getWorkflowsDir(workflowId)
                 + "/" + componentId + "/output/";

            File wfcLogFileDir = new File(wfcLogDirPath);

            if (wfcLogFileDir.exists() && wfcLogFileDir.canRead()) {
                Integer numFiles = 0;
                for (File file : wfcLogFileDir.listFiles()) {
                    if (file.getName().matches("(?i).*\\.wfl")
                            || file.getName().equals("WorkflowComponent.log")) {

                        JSONArray jsonTextArray = new JSONArray();
                        BufferedReader br = null;
                        RandomAccessFile raFile = null;
                        // Return lines to debugInfo string
                        try {

                            logger.trace("Component log file found: " + file.getAbsolutePath());
                            String line = "";
                            Integer lineCount = 0;

                            if (file.length() > MAX_DEBUG_TAIL_LENGTH) {
                                logger.debug("Read random access file: " + file.getAbsolutePath());
                                // Use offset with random access file since file is large.
                                Long offset = file.length() - MAX_DEBUG_TAIL_LENGTH;
                                raFile = new RandomAccessFile(file, "r");
                                raFile.seek(offset); // Sets the file-pointer offset
                                // Ensure we don't start in the middle of a line
                                raFile.readLine();
                                while ((line = raFile.readLine()) != null) {
                                    if (textFilter == null || textFilter.isEmpty()) {
                                        jsonTextArray.put(line);
                                    } else if (line.matches(textFilter)) {
                                        jsonTextArray.put(line);
                                    }
                                    lineCount++;
                                }

                            } else {
                                logger.debug("Read entire file: " + file.getAbsolutePath());
                                // Get the entire file since it isn't large.
                                br = new BufferedReader(new FileReader(file));

                                while ((line = br.readLine()) != null) {
                                    if (textFilter == null || textFilter.isEmpty()) {
                                        jsonTextArray.put(lineCount + ": " + line);
                                    } else if (line.matches(textFilter)) {
                                        jsonTextArray.put(lineCount + ": " + line);
                                    }
                                    lineCount++;
                                }
                        }


                        } catch (FileNotFoundException e) {
                            writeJsonError(resp, "Error accessing component log file.");
                            return;
                        } catch (IOException e) {
                            writeJsonError(resp, "Error reading component log file.");
                            return;
                        } finally {
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    logger.error("Error closing component log file.");
                                }
                            }
                            if (raFile != null) {
                                try {
                                    raFile.close();
                                } catch (IOException e) {
                                    logger.error("Error closing component log file.");
                                }
                            }
                        }

                        jsonTextArray.put(new String(""));

                        try {
                            jsonTextData.put(file.getName(), jsonTextArray);
                            numFiles++;
                        } catch (JSONException e) {
                            logger.error("JSON error adding debug info text.");
                        }
                    }
                }

                try {
                    if (textFilter == null || textFilter.isEmpty()) {
                        writeJSON(resp,
                            json("success", "true",
                                "workflowId", workflowId,
                                "componentId", componentId,
                                "textData", jsonTextData,
                                "numFiles", numFiles
                                ));
                    } else {
                        // The textFilter exists
                        writeJSON(resp,
                                json("success", "true",
                                    "workflowId", workflowId,
                                    "componentId", componentId,
                                    "textData", jsonTextData,
                                    "textFilter", textFilter,
                                    "numFiles", numFiles
                                    ));
                    }
                    return;
                } catch (JSONException exception) {
                    writeJsonError(resp, "JSON Error retrieving debug info for " + componentId);
                    return;
                } catch (IOException e) {
                    writeJsonError(resp, "I/O error retrieving debug info for " + componentId);
                    return;
                }
            }

            writeJsonError(resp, "Component output directory for component (" + componentId + ") cannot be found.");
            return;
        } else {
            writeJsonError(resp, "Cannot request debugging info for this workflow.");
            return;
        }
    }

    /**
     * Get debug info for a given workflow.
     * @param req the request item
     * @param resp the response item
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user item
     */
    private void doPostRequestDebugDownload(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {

        if (workflowItem != null && loggedInUserItem != null
                && (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())
                || loggedInUserItem.getAdminFlag())) {

            Long workflowId = workflowItem.getId();
            // To utilize existing JS functions, we need to encapsulate
            // the debug info into a specifically formatted JSON array
            JSONObject jsonTextData = new JSONObject();

            String componentId = req.getParameter("componentId");
            if (componentId != null && !componentId.matches(STRICT_CHARS)) { componentId = ""; }

            // Parent dir of log files (WorkflowComponent.log and *.wfl files)
            String wfcLogDirPath = getBaseDir() + "/" + WorkflowFileUtils.getWorkflowsDir(workflowId)
                 + "/" + componentId + "/output/";

            File wfcLogFileDir = new File(wfcLogDirPath);

            if (wfcLogFileDir.exists() && wfcLogFileDir.canRead()) {

                for (File file : wfcLogFileDir.listFiles()) {
                    if (file.getName().matches("(?i).*\\.wfl")
                            || file.getName().equals("WorkflowComponent.log")) {

                        try {
                            writeJSON(resp,
                                    json("success", "true",
                                        "workflowId", workflowId,
                                        "componentId", componentId,
                                        "textData", jsonTextData
                                        ));
                        } catch (IOException e) {
                            logger.error("doPostRequestDebugDownload:: " + e.toString());
                        } catch (JSONException e) {
                            logger.error("doPostRequestDebugDownload:: " + e.toString());
                        }
                        return;
                    }
                }

            }

        }
        writeJsonError(resp, "Cannot request debugging info for this workflow.");
        return;
    }

    private JSONObject getMyViewableDatasets(HttpServletRequest req,
                                             WorkflowContext workflowContext, WorkflowItem workflowItem,
                                             UserItem loggedInUserItem, String componentName)
            throws JSONException {
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {

            JSONObject jsonInfo = workflowContext.getViewableProjectsAndDatasets(componentName);
            Boolean refreshCache = false;
            // If this info is already cached for this componentName, return the jsonInfo
            if (jsonInfo != null) {

                // Ensure the cached file info is up to date.
                // The following will cause refreshCache to be false
                // If the project number is different vs cached
                // If the number of datasets in each projects differs
                // If the dataset data has changed since info cached
                refreshCache = refreshViewableProjectsAndDatasets(workflowContext, workflowItem, loggedInUserItem);

                if (!refreshCache) {
                    return jsonInfo;
                }
            }
            // Using our own convention of Component_Name vs file-type,
            // an Import's component name can be converted to its file type:
            String componentFileType = componentName.replaceAll("_", "-").toLowerCase();

            JSONArray projectJsonArray = new JSONArray();
            JSONArray datasetJsonArray = new JSONArray();
            JSONArray fileJsonArray = new JSONArray();

            // If workflow is associated with a dataset, list it's files first.
            DatasetItem currentDataset = workflowContext.getCurrentDataset();
            ProjectItem currentProject = null;
            if (currentDataset != null) {
                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                currentProject = projectDao.get((Integer)currentDataset.getProject().getId());
            }

            List<ProjectItem> projects = new ArrayList<ProjectItem>();
            List<ProjectItem> publicProjects = workflowContext.getPublicProjects();
            List<ProjectItem> myProjects = workflowContext.getMyProjects();
            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            if (publicProjects == null || publicProjects.isEmpty() || refreshCache) {
                publicProjects = authDao.findPublicProjects((String) loggedInUserItem.getId());
                workflowContext.setPublicProjects(publicProjects);
                req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
            }
            if (myProjects == null || myProjects.isEmpty() || refreshCache) {
                myProjects = authDao.findMyViewableProjectsForImports((String) loggedInUserItem.getId(), loggedInUserItem.getAdminFlag());
                workflowContext.setMyProjects(myProjects);
                req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
            }

            if (publicProjects != null && !publicProjects.isEmpty()) {
                projects.addAll(publicProjects);
                projects.removeAll(myProjects);
            }
            if (myProjects != null && !myProjects.isEmpty()) {
                projects.addAll(myProjects);
            }

            if (!projects.isEmpty()) {
                // Remove current project and put in first spot.
                if (currentProject != null) {
                    projects.remove(currentProject);
                    projects.add(0, currentProject);
                }

                for (ProjectItem projectItem : projects) {
                    Boolean addedProject = false;
                    Hibernate.initialize(projectItem);
                    Boolean isPublic = authDao.isPublic((Integer) projectItem.getId());
                    JSONObject projectObject = new JSONObject();
                    projectObject.put("id", projectItem.getId());
                    projectObject.put("projectName", WorkflowFileUtils.htmlEncode(projectItem.getProjectName()));
                    projectObject.put("dataCollectionType", projectItem.getDataCollectionType());
                    projectObject.put("datasetLastAdded", projectItem.getDatasetLastAdded());
                    projectObject.put("isPublic", isPublic);

                    Boolean isProjectOrDatashopAdminFlag = loggedInUserItem.getAdminFlag()
                            || authDao.isProjectAdmin(loggedInUserItem, projectItem);
                    Collection<DatasetItem> datasetItems = dsDao.findByProject(projectItem, isProjectOrDatashopAdminFlag);
                    workflowContext.setViewableDatasetCount(projectItem, datasetItems.size());
                    req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);

                    if (datasetItems != null) {
                        for (DatasetItem datasetItem : datasetItems) {

                            Hibernate.initialize(datasetItem);

                            // Trac #969: Unreleased datasets can't be used in workflows.
                            if ((datasetItem.getReleasedFlag() == null)
                                || !datasetItem.getReleasedFlag()) {
                                continue;
                            }

                            Boolean addedDataset = false;
    // mck3 todo: tooltip with the following dataset info over samples/datasets
                            JSONObject datasetObject = new JSONObject();
                            datasetObject.put("id", datasetItem.getId());
                            datasetObject.put("projectId", projectItem.getId());
                            datasetObject.put("datasetName", WorkflowFileUtils.htmlEncode(datasetItem.getDatasetName()));
                            datasetObject.put("tutor", datasetItem.getTutor());
                            datasetObject.put("startTime", datasetItem.getStartTime());
                            datasetObject.put("endTime", datasetItem.getEndTime());
                            datasetObject.put("description", datasetItem.getDescription());
                            datasetObject.put("hypothesis", datasetItem.getHypothesis());
                            datasetObject.put("notes", datasetItem.getNotes());
                            datasetObject.put("school", datasetItem.getSchool());
                            datasetObject.put("dataLastModified", datasetItem.getDataLastModified());

                            // Get files mapped to datasets
                            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                            List<Integer> fileIds = fileDao.find(datasetItem);
                            for (Integer fileId : fileIds) {

                                FileItem fileItem = fileDao.get(fileId);

                                if (fileItem != null) {
                                    String fileType = "file";
                                    if (fileItem.getFileType() != null) {
                                        String mimeType = fileItem.getFileType();
                                        if (mimeType.matches("[a-zA-Z]+/[a-zA-Z]+")) {
                                            Integer indexOf = mimeType.indexOf("/");
                                            fileType = mimeType.substring(0, indexOf);
                                        }
                                    }
                                    Integer fileIndex = 0;
                                    if (fileItem.getDescription() != null && fileItem.getDescription().matches("\\d+")) {
                                        fileIndex = Integer.parseInt(fileItem.getDescription());
                                    }
                                    // The cached export exists so add it to the fileJsonArray
                                    UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                                    UserItem systemUser = userDao.get(UserItem.SYSTEM_USER);
                                    if (systemUser != null) {
                                        String filePath = WorkflowFileUtils.sanitizePath(
                                            fileItem.getFullFileName(WorkflowFileUtils.getStrictDirFormat(getBaseDir())));
                                        JSONObject exportFileObject = createOrGetWorkflowFileJSON(
                                            fileType, componentFileType, filePath, fileIndex, workflowItem, datasetItem,
                                                projectItem, systemUser, false);
                                        if (exportFileObject != null) {
                                            if (!addedProject) {
                                                projectJsonArray.put(projectObject);
                                                addedProject = true;
                                            }
                                            if (!addedDataset) {
                                                datasetJsonArray.put(datasetObject);
                                                addedDataset = true;
                                            }
                                            // Add file info to json array
                                            fileJsonArray.put(exportFileObject);
                                        }
                                    }
                                }
                            }

                         // Get DataShop "export" files (transaction, student-step, problem-student).
                            // As they are not recorded anywhere in the database as such, we
                            // rely on older, system-dependent helper methods to obtain them.
                            Boolean isTxExport = DS_TRANSACTION_IMPORT.equalsIgnoreCase(componentFileType);
                            Boolean isStepExport = DS_STUDENT_STEP_IMPORT.equalsIgnoreCase(componentFileType);
                            Boolean isProblemExport = DS_STUDENT_PROBLEM_IMPORT.equalsIgnoreCase(componentFileType);
                            Boolean isTabDelimited = WorkflowImportHelper.FILE_TYPE_TAB_DELIMITED.equalsIgnoreCase(componentFileType);
                            Boolean isText = WorkflowImportHelper.FILE_TYPE_TEXT.equalsIgnoreCase(componentFileType);
                            Boolean isFile = WorkflowImportHelper.FILE_TYPE_FILE.equalsIgnoreCase(componentFileType);

                            if (isTxExport || isStepExport || isProblemExport || isTabDelimited || isText || isFile) {
                                // Get the specified type of cached datashop export for each sample,
                                // and add it to the fileJsonArray
                                SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
                                List<SampleItem> sampleItems = workflowContext.getSamples(datasetItem);
                                if (sampleItems == null || refreshCache) {
                                    sampleItems = sampleDao.find(datasetItem, loggedInUserItem);
                                    workflowContext.setSamples(datasetItem, sampleItems);
                                    req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
                                }
                                TransactionExportHelper txExportHelper = HelperFactory.DEFAULT.getTransactionExportHelper();
                                StepRollupExportHelper stepExportHelper = HelperFactory.DEFAULT.getStepRollupExportHelper();
                                StudentProblemExportHelper studentProblemExportHelper = HelperFactory.DEFAULT
                                        .getStudentProblemExportHelper();

                                for (SampleItem sampleItem : sampleItems) {
                                 // The cached export exists so add it to the fileJsonArray
                                    UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                                    UserItem systemUser = userDao.get(UserItem.SYSTEM_USER);
                                    if (systemUser != null) {
                                        List<String> exportTypes = new ArrayList<String>();

                                        if (isTxExport) {
                                            exportTypes.add(DS_TRANSACTION_IMPORT);
                                        } else if (isStepExport) {
                                            exportTypes.add(DS_STUDENT_STEP_IMPORT);
                                        } else if (isProblemExport) {
                                            exportTypes.add(DS_STUDENT_PROBLEM_IMPORT);
                                        } else {
                                            exportTypes.add(DS_TRANSACTION_IMPORT);
                                            exportTypes.add(DS_STUDENT_STEP_IMPORT);
                                            exportTypes.add(DS_STUDENT_PROBLEM_IMPORT);
                                        }


                                        for (String eType : exportTypes) {
                                            String cachedExportPath = null;
                                            if (eType.equalsIgnoreCase(DS_TRANSACTION_IMPORT)) {
                                                cachedExportPath = txExportHelper.getCachedFileName(sampleItem,
                                                        getBaseDir());
                                            } else if (eType.equalsIgnoreCase(DS_STUDENT_STEP_IMPORT)) {
                                                cachedExportPath = stepExportHelper.getCachedFileName(sampleItem,
                                                        getBaseDir());
                                            } else if (eType.equalsIgnoreCase(DS_STUDENT_PROBLEM_IMPORT)) {
                                                cachedExportPath = studentProblemExportHelper.getCachedFileName(
                                                        sampleItem, getBaseDir());
                                            }
                                            if (cachedExportPath != null) {

                                                JSONObject exportFileObject = createOrGetWorkflowFileJSON(eType,
                                                        componentFileType, cachedExportPath, 0, workflowItem, datasetItem,
                                                        projectItem, systemUser, true);
                                                if (exportFileObject != null) {
                                                    if (!addedProject) {
                                                        projectJsonArray.put(projectObject);
                                                        addedProject = true;
                                                    }
                                                    if (!addedDataset) {
                                                        datasetJsonArray.put(datasetObject);
                                                        addedDataset = true;
                                                    }
                                                    // Add file info to json array
                                                    fileJsonArray.put(exportFileObject);
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (fileJsonArray.length() == 0) {
                    jsonInfo = null;
                    workflowContext.removeViewableProjectsAndDatasets(componentName);
                } else {
                    jsonInfo = json("projectArray", projectJsonArray, "datasetArray", datasetJsonArray, "fileArray", fileJsonArray);
                    workflowContext.setViewableProjectsAndDatasets(componentName, jsonInfo);
                    req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
                }
            }
            return jsonInfo;
        }
        return null;
    }



    private JSONObject getMyEditableDatasets(HttpServletRequest req, WorkflowContext workflowContext,
                                             UserItem loggedInUserItem,
                                             Integer preferredDatasetId)
            throws JSONException {

            JSONObject jsonInfo = workflowContext.getEditableProjectsAndDatasets();
            Boolean refreshCache = false;
            // If this info is already cached for this session, return the jsonInfo
            if (jsonInfo != null) {

                // Ensure the cached file info is up to date.
                // The following will cause refreshCache to be false
                // If the project number is different vs cached
                // If the number of datasets in each projects differs
                refreshCache = refreshEditableProjectsAndDatasets(workflowContext, loggedInUserItem);

                if (!refreshCache) {
                    return jsonInfo;
                }
            }

            JSONArray projectJsonArray = new JSONArray();
            JSONArray datasetJsonArray = new JSONArray();

            List<ProjectItem> projects = new ArrayList<ProjectItem>();

            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            List<ProjectItem> myProjects = authDao.findMyEditableProjectsForImports((String) loggedInUserItem.getId(), loggedInUserItem.getAdminFlag());

            if (myProjects != null && !myProjects.isEmpty()) {
                projects.addAll(myProjects);
            }

            if (!projects.isEmpty()) {

                Integer preferredProjectId = null;
                if (preferredDatasetId != null) {
                    DatasetItem dsItem = dsDao.get(preferredDatasetId);
                    if (dsItem != null && dsItem.getProject() != null) {
                        preferredProjectId = (Integer) dsItem.getProject().getId();
                    }
                }

                if (preferredProjectId != null) {

                    ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                    ProjectItem preferredProject = projectDao.get(preferredProjectId);

                    // Remove preferred project and put in first spot.
                    if (preferredProject != null) {
                        projects.remove(preferredProject);
                        projects.add(0, preferredProject);
                    }
                }

                for (ProjectItem projectItem : projects) {
                    Boolean addedProject = false;
                    Hibernate.initialize(projectItem);
                    Boolean isPublic = authDao.isPublic((Integer) projectItem.getId());
                    JSONObject projectObject = new JSONObject();
                    projectObject.put("id", projectItem.getId());
                    projectObject.put("projectName", WorkflowFileUtils.htmlEncode(projectItem.getProjectName()));
                    projectObject.put("dataCollectionType", projectItem.getDataCollectionType());
                    projectObject.put("datasetLastAdded", projectItem.getDatasetLastAdded());
                    projectObject.put("isPublic", isPublic);

                    Boolean isProjectOrDatashopAdminFlag = loggedInUserItem.getAdminFlag()
                            || authDao.isProjectAdmin(loggedInUserItem, projectItem);
                    Collection<DatasetItem> datasetItems = dsDao.findByProject(projectItem, isProjectOrDatashopAdminFlag);
                    workflowContext.setEditableDatasetCount(projectItem, datasetItems.size());
                    req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);

                    if (datasetItems != null) {
                        for (DatasetItem datasetItem : datasetItems) {
                            Hibernate.initialize(datasetItem);
    // mck3 todo: tooltip with the following dataset info over samples/datasets
                            JSONObject datasetObject = new JSONObject();
                            datasetObject.put("id", datasetItem.getId());
                            datasetObject.put("projectId", projectItem.getId());
                            datasetObject.put("projectName", WorkflowFileUtils.htmlEncode(projectItem.getProjectName()));
                            datasetObject.put("datasetName", WorkflowFileUtils.htmlEncode(datasetItem.getDatasetName()));
                            datasetObject.put("tutor", datasetItem.getTutor());
                            datasetObject.put("startTime", datasetItem.getStartTime());
                            datasetObject.put("endTime", datasetItem.getEndTime());
                            datasetObject.put("description", datasetItem.getDescription());
                            datasetObject.put("hypothesis", datasetItem.getHypothesis());
                            datasetObject.put("notes", datasetItem.getNotes());
                            datasetObject.put("school", datasetItem.getSchool());
                            datasetObject.put("dataLastModified", datasetItem.getDataLastModified());

                            if (!addedProject) {
                                projectJsonArray.put(projectObject);
                                addedProject = true;
                            }

                            datasetJsonArray.put(datasetObject);

                        }
                    }
                }
            }

            jsonInfo = null;
            if (datasetJsonArray.length() > 0) {
                jsonInfo = json("myProjectArray", projectJsonArray, "myDatasetArray", datasetJsonArray);
                workflowContext.setEditableProjectsAndDatasets(jsonInfo);
                req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
            }

            return jsonInfo;

    }


    private Boolean refreshViewableProjectsAndDatasets(WorkflowContext workflowContext, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {

        Boolean refreshCache = false;
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();

        List<ProjectItem> publicProjects = authDao.findPublicProjects((String) loggedInUserItem.getId());
        List<ProjectItem> cachedPublicProjects = workflowContext.getPublicProjects();

        List<ProjectItem> myProjects = authDao.findMyViewableProjectsForImports((String) loggedInUserItem.getId(), loggedInUserItem.getAdminFlag());
        List<ProjectItem> cachedMyProjects = workflowContext.getMyProjects();

        if ((publicProjects == null ^ cachedPublicProjects == null)
            || (cachedPublicProjects != null && publicProjects != null
                && cachedPublicProjects.size() != publicProjects.size())) {
            refreshCache = true;
            return refreshCache;
        }

        if ((myProjects == null ^ cachedMyProjects == null)
            || (cachedMyProjects != null && myProjects != null
                && cachedMyProjects.size() != myProjects.size())) {
            refreshCache = true;
            return refreshCache;
        }

        List<ProjectItem> projects = new ArrayList<ProjectItem>();
        if (publicProjects != null) {
            projects.addAll(publicProjects);
        }
        if (myProjects != null) {
            projects.addAll(myProjects);
        }
        if (!projects.isEmpty()) {

            for (ProjectItem projectItem : projects) {

                Hibernate.initialize(projectItem);

                Boolean isProjectOrDatashopAdminFlag = loggedInUserItem.getAdminFlag()
                        || authDao.isProjectAdmin(loggedInUserItem, projectItem);
                Collection<DatasetItem> datasetItems = dsDao.findByProject(projectItem, isProjectOrDatashopAdminFlag);

                // Need to refresh cache if the number of datasets in the project has changed.
                // Big hammer, but we'll also refresh if data has been modified as it affects exports.
                // TBD: need mechanism to only update cache for affected dataset(s).
                Integer cachedNumDatasets = workflowContext.getViewableDatasetCount(projectItem);

                if (datasetItems != null) {
                    if (cachedNumDatasets != datasetItems.size()) {
                        refreshCache = true;
                        break;
                    }
                    Date timeInfoCached = workflowContext.getTimeViewableInfoCached();
                    for (DatasetItem dataset : datasetItems) {
                        Date lastModTime = dataset.getDataLastModified();
                        if ((lastModTime != null) && (lastModTime.after(timeInfoCached))) {
                            refreshCache = true;
                            break;
                        }
                    }
                } else {
                    // No datasets exist in project.
                    if (cachedNumDatasets != 0) {
                        refreshCache = true;
                        break;
                    }
                }

                // Determine if files attached to datasets have changed.

                if (!refreshCache) {
                    List<Integer> newList = null;
                    List<Integer> oldList = null;
                    for (DatasetItem datasetItem : datasetItems) {
                        newList = fileDao.find(datasetItem);
                        oldList = workflowContext.getDatasetToFileIdMap((Integer) datasetItem.getId());
                        // Check the sizes, first.
                        if (oldList == null) {
                            if (newList != null) {
                                refreshCache = true;
                                workflowContext.setDatasetToFileIdMap((Integer) datasetItem.getId(), newList);
                                break;
                            } else {
                                refreshCache = false;
                                break;
                            }
                        } else if (oldList.size() != newList.size()) {
                            workflowContext.setDatasetToFileIdMap((Integer) datasetItem.getId(), newList);
                            refreshCache = true;
                            break;
                        } else {
                            // Otherwise, use the fastest method available to check the actual contents
                            // of the new and old file lists.
                            if (! CollectionUtils.isEqualCollection(newList, oldList)) {
                                workflowContext.setDatasetToFileIdMap((Integer) datasetItem.getId(), newList);
                                refreshCache = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return refreshCache;
    }

    private Boolean refreshEditableProjectsAndDatasets(WorkflowContext workflowContext,
                                                       UserItem loggedInUserItem) {

        Boolean refreshCache = false;
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();

        List<ProjectItem> publicProjects = authDao.findPublicProjects((String) loggedInUserItem.getId());
        List<ProjectItem> cachedPublicProjects = workflowContext.getPublicProjects();

        List<ProjectItem> myProjects =
            authDao.findMyEditableProjectsForImports((String) loggedInUserItem.getId(), loggedInUserItem.getAdminFlag());
        List<ProjectItem> cachedMyProjects = workflowContext.getMyProjects();

        if ((publicProjects == null ^ cachedPublicProjects == null)
            || (cachedPublicProjects != null && publicProjects != null
                && cachedPublicProjects.size() != publicProjects.size())) {
            refreshCache = true;
            return refreshCache;
        }

        if ((myProjects == null ^ cachedMyProjects == null)
            || (cachedMyProjects != null && myProjects != null
                && cachedMyProjects.size() != myProjects.size())) {
            refreshCache = true;
            return refreshCache;
        }

        List<ProjectItem> projects = new ArrayList<ProjectItem>();
        if (publicProjects != null) {
            projects.addAll(publicProjects);
        }
        if (myProjects != null) {
            projects.addAll(myProjects);
        }
        if (!projects.isEmpty()) {

            for (ProjectItem projectItem : projects) {

                Hibernate.initialize(projectItem);

                Boolean isProjectOrDatashopAdminFlag = loggedInUserItem.getAdminFlag()
                        || authDao.isProjectAdmin(loggedInUserItem, projectItem);
                Collection<DatasetItem> datasetItems = dsDao.findByProject(projectItem, isProjectOrDatashopAdminFlag);

                // Need to refresh cache if the number of datasets in the project has changed.
                // For 'editable' this is a sufficient check; no need to consider file-level changes.
                Integer cachedNumDatasets = workflowContext.getEditableDatasetCount(projectItem);

                if (datasetItems != null) {
                    if (cachedNumDatasets != datasetItems.size()) {
                        refreshCache = true;
                        break;
                    }
                } else {
                    // No datasets exist in project.
                    if (cachedNumDatasets != 0) {
                        refreshCache = true;
                        break;
                    }
                }

                if (refreshCache) {
                    break;
                }
            }
        }
        return refreshCache;
    }

    /**
     * Writes a json error message to the response.
     * @param resp the HttpServletResponse
     * @param errorMessage the error message
     */
    private void writeJsonError(HttpServletResponse resp, String errorMessage) {
        try {
            logger.error("Error relayed to user: " + errorMessage);
            writeJSON(resp, json("error_flag", "true", "message", errorMessage));
            return;
        } catch (JSONException e) {
            logger.error("LearnSphereServlet caused a JSON Exception.");
        } catch (IOException e) {
            logger.error("LearnSphereServlet caused an IO Exception.");
        }
    }

} // end of WorkflowEditorServlet class

