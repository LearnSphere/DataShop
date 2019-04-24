/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.WfcRecentlyUsedDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentUserLogDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowFolderDao;
import edu.cmu.pslc.datashop.dao.WorkflowFolderMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowPaperMapDao;
import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowPersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowUserLogDao;
import edu.cmu.pslc.datashop.dao.hibernate.WorkflowFolderDaoHibernate;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowRowDto.SortParameter;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WfcRecentlyUsedItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentUserLogItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderMapItem;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowUserLogItem;

/**
 * This servlet handles the management and requests for workflows.
 *
 * @author Mike Komisin
 * @version $Revision: 15963 $ <BR>
 * Last modified by: $Author: mkomisin $ <BR>
 * Last modified on: $Date: 2019-03-26 15:53:52 -0400 (Tue, 26 Mar 2019) $ <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** The JSP name for editing workflows as a standalone tool. */
    private static final String DETACHED_EDIT_JSP_NAME = "/jsp_workflows/learnsphere-edit.jsp";
    /** The JSP name for editing workflows as a standalone tool. */
    private static final String DETACHED_LIST_JSP_NAME = "/jsp_workflows/learnsphere-list.jsp";
    /** The JSP for logging in to LearnSphere. */
    private static final String LOGIN_JSP_NAME = "/jsp_workflows/ls_login.jsp";

    /** The workflow helpers. */
    private WorkflowHelper workflowHelper;
    private WorkflowAccessHelper workflowAccessHelper;
    private ComponentHelper componentHelper;
    private ConnectionHelper connectionHelper;
    private WorkflowIfaceHelper workflowIfaceHelper;
    private WorkflowFileHelper workflowFileHelper;
    /** The workflow DAO. */
    private WorkflowDao workflowDao;
    /** The WFC CommonResources directory. */
    private static String commonResourcesDir;

    /** Label used for setting session attribute "recent_report". */
    public static final String SERVLET_NAME = "LearnSphere";

    /** Format for the date range method, getDateRangeString. */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMM d, yyyy HH:mm:ss");
    /** The Sort-by context attribute handle. */
    public static final String SORT_BY = "sortBy";
    /** The toggle sort context attribute handle. */
    public static final String TOGGLE_SORT = "toggleSort";
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
                && !req.getParameter("requestingMethod").equalsIgnoreCase("ManageWorkflowsServlet.getComponentStates"))) {

            logger.info("doPost params :: " + getDebugParamsString(req));
        }

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

            if (req.getParameter("requestingMethod") != null
                    && req.getParameter("requestingMethod").equalsIgnoreCase("ManageWorkflowsServlet.logComponentAction")) {

                WorkflowComponentUserLogDao wfComponentUserLogDao = DaoFactory.DEFAULT.getWorkflowComponentUserLogDao();
                Long workflowId = null;
                if (req.getParameter("workflowId") != null
                    && req.getParameter("workflowId").matches("\\d+")) {
                        workflowId = Long.parseLong(req.getParameter("workflowId"));
                }

                Integer datasetId = null;
                if (req.getParameter("datasetId") != null
                    && req.getParameter("datasetId").matches("\\d+")) {
                        datasetId = Integer.parseInt(req.getParameter("datasetId"));
                }

                Integer workflowFileId = null;
                if (req.getParameter("workflowFileId") != null
                    && req.getParameter("workflowFileId").matches("\\d+")) {
                        workflowFileId = Integer.parseInt(req.getParameter("workflowFileId"));
                }

                Integer datasetFileId = null;
                if (req.getParameter("datasetFileId") != null
                    && req.getParameter("datasetFileId").matches("\\d+")) {
                    datasetFileId = Integer.parseInt(req.getParameter("datasetFileId"));
                }

                Integer nodeIndex = null;
                if (req.getParameter("nodeIndex") != null
                    && req.getParameter("nodeIndex").matches("\\d+")) {
                    nodeIndex = Integer.parseInt(req.getParameter("nodeIndex"));
                }

                String componentId = req.getParameter("componentId");
                String componentName = req.getParameter("componentName");
                String componentType = req.getParameter("componentType");
                String humanReadableId = req.getParameter("humanReadableId");

                String action = req.getParameter("action");
                String info = req.getParameter("info");

                if (action != null && action.equals("Add component")) {
                    if (componentName != null && componentName.matches("[a-zA-Z0-9_\\-]+")
                        && componentType != null && componentType.matches("[a-zA-Z0-9_\\-]+")) {
                        WfcRecentlyUsedDao wfcRecentlyUsedDao = DaoFactory.DEFAULT.getWfcRecentlyUsedDao();
                        WfcRecentlyUsedItem wfcRecentlyUsedItem = new WfcRecentlyUsedItem(
                                loggedInUserItem.getId().toString(), componentType, componentName);
                        wfcRecentlyUsedDao.saveOrUpdate(wfcRecentlyUsedItem);
                    }
                }

                WorkflowComponentUserLogItem wfComponentUserLogItem = new WorkflowComponentUserLogItem(
                        (String) loggedInUserItem.getId(), workflowId, datasetId,
                        componentId, componentName, componentType, humanReadableId,
                        nodeIndex, workflowFileId, datasetFileId, action, info);
                wfComponentUserLogDao.saveOrUpdate(wfComponentUserLogItem);

                try {
                    writeJSON(resp, json("msg", "success"));
                } catch (JSONException exception) {
                    logger.error("Error writing to workflow_component_user_log");
                }
                return;
            }


            if (req.getParameter("requestingMethod") != null
                    && req.getParameter("requestingMethod").equalsIgnoreCase("ManageWorkflowsServlet.logWorkflowAction")) {

                WorkflowUserLogDao wfUserLogDao = DaoFactory.DEFAULT.getWorkflowUserLogDao();
                Long workflowId = null;
                if (req.getParameter("workflowId") != null
                    && req.getParameter("workflowId").matches("\\d+")) {
                        workflowId = Long.parseLong(req.getParameter("workflowId"));
                }

                Integer datasetId = null;
                if (req.getParameter("datasetId") != null
                    && req.getParameter("datasetId").matches("\\d+")) {
                        datasetId = Integer.parseInt(req.getParameter("datasetId"));
                }

                Long newWorkflowId = null;
                if (req.getParameter("newWorkflowId") != null
                    && req.getParameter("newWorkflowId").matches("\\d+")) {
                    newWorkflowId = Long.parseLong(req.getParameter("newWorkflowId"));
                }

                String action = req.getParameter("action");
                String info = req.getParameter("info");

                WorkflowUserLogItem wfUserLogItem = new WorkflowUserLogItem(
                        (String) loggedInUserItem.getId(), workflowId, datasetId,
                       action, info, newWorkflowId);
                wfUserLogDao.saveOrUpdate(wfUserLogItem);

                try {
                    writeJSON(resp, json("msg", "success"));
                } catch (JSONException exception) {
                    logger.error("Error writing to workflow_component_user_log");
                }
                return;
            }


            // Helper
            workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
            workflowAccessHelper = HelperFactory.DEFAULT.getWorkflowAccessHelper();
            componentHelper = HelperFactory.DEFAULT.getComponentHelper();
            connectionHelper = HelperFactory.DEFAULT.getConnectionHelper();
            workflowIfaceHelper = HelperFactory.DEFAULT.getWorkflowIfaceHelper();
            workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
            // DAOs
            workflowDao = DaoFactory.DEFAULT.getWorkflowDao();

            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            // Workflow and workflow context
            WorkflowItem workflowItem = null;
            WorkflowContext workflowContext = null;
            // Dataset and dataset context
            DatasetItem datasetItem = null;

            DatasetContext datasetContext = getDatasetContext(req);
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
             *  Dataset Authorization level.
             */
            String datasetAuthLevel = null;
            if (datasetContext != null) {
                // Check to see if the user has access to the dataset (if a dataset is given).
                datasetItem = datasetContext.getDataset();
                if (datasetItem != null) {  // mck3: cache me server-side
                    datasetAuthLevel = authDao.getAuthLevel(loggedInUserItem, datasetItem);
                }
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

            String minAuthLevel = null;
            if (workflowIdString != null && workflowIdString.matches("\\d+")) {
                workflowId = Long.parseLong(workflowIdString);
                workflowItem = workflowDao.get(workflowId);
                if (workflowItem != null) {

                    /**
                     *  Min auth level based on all imports for this workflow.
                     */
                    // minAuthLevel cannot be cached because it could change at any given time.
                    minAuthLevel = workflowAccessHelper.getFloorPermissions(workflowItem, loggedInUserItem);

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

            /**
             *  Workflow Row Sort request.
             */
            // Sort-by parameter for the workflows table should be set in the workflow context.
            String sortBy = null;

            if (req.getParameter(SORT_BY) != null && req.getParameter(SORT_BY).matches("[a-zA-Z0-9\\s_\\-]+")) {
                sortBy = (String) req.getParameter(SORT_BY);
            }
            if (sortBy != null) {
                boolean isGlobalSortReq = false;
                if (req.getParameter("isGlobal") != null) {
                    isGlobalSortReq = Boolean.valueOf(req.getParameter("isGlobal"));
                }
                if (isGlobalSortReq) {
                    workflowContext.setSortBy(sortBy);
                    workflowContext.toggleSortOrder(sortBy);
                } else {
                    workflowContext.setMySortBy(sortBy);
                    workflowContext.toggleMySortOrder(sortBy);
                }
                req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
            }

            // Get the requesting method.
             String requestingMethod = req.getParameter("requestingMethod");

            /**
             *  Workflow Servlet Requests.
             */

            if (isLoggedIn && requestingMethod != null
                    && requestingMethod.equals("ManageWorkflowsServlet.fetchMyFolders")) {

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
                    && (requestingMethod.equals("ManageWorkflowsServlet.cancelWorkflow")
                        && (isOwner || isAdmin))) {
                    // datasetItem is not used for anything except UI feedback in the following method.
                    doPostHandleCancelRequests(req, resp, workflowItem, datasetItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.deleteWorkflow") && (isOwner || isAdmin)) {
                    // datasetItem is not used for anything except UI feedback in the following method.
                    doPostDeleteWorkflow(req, resp, workflowItem, datasetItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.moveWorkflow") && (isOwner || isAdmin)) {
                    doPostMoveWorkflowToFolder(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.editDescription") && (isOwner || isAdmin)) {
                    doPostEditDescription(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.editShareability") && (isOwner || isAdmin)) {
                    doPostEditShareability(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.editMetaData") && (isOwner || isAdmin)) {
                    // New method to handle name, description and shareability changes atomically.
                    doPostEditMetaData(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.saveAsNewWorkflow")) {
                    // Save this user's worfklow as a new workflow.
                    // The following method includes a check to ensure the user has edit or higher access to a dataset
                    // before they can associate a workflow with that dataset.
                    workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, datasetItem,
                            WorkflowHelper.LOG_SAVE_AS_NEW_WORKFLOW, "", null);
                    doPostSaveAsNewWorkflow(req, resp, workflowItem, datasetItem, loggedInUserItem, datasetAuthLevel);
                    return;
                } else if ((requestingMethod != null
                        && requestingMethod.equals("WorkflowEditorServlet.editWorkflow"))
                        || (requestingMethod == null)) {
                    // Provide the info needed to open the workflow editor.
                    // datasetItem is not used for anything except UI feedback in the following method.

                    // Make note of dataset by which user accessed this workflow. Null if not via a dataset.
                    workflowContext.setCurrentDataset(datasetItem);
                    req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);

                    doPostEditWorkflow(req, resp, workflowItem, datasetItem, loggedInUserItem, minAuthLevel);
                    return;
                }

            } else if (workflowItem == null || (!isOwner && isShared)) {
                if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.getWorkflowStates")) {
                    doPostGetWorkflowStates(req, resp, loggedInUserItem, datasetItem);
                    return;

                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.saveAsNewWorkflow")) {
                    // Save this user's worfklow as a new workflow.
                    // IMPORTANT: Doing so removes any attached files from the workflow.
                    workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, datasetItem,
                            WorkflowHelper.LOG_CREATE_WORKFLOW, "", null);
                    doPostSaveAsNewWorkflow(req, resp, workflowItem, datasetItem, loggedInUserItem, datasetAuthLevel);
                    return;
                } else if (isLoggedIn && requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.fetchWorkflowRows")) {

                    WorkflowFolderDao wfFolderDao = DaoFactory.DEFAULT.getWorkflowFolderDao();

                    String panelId = req.getParameter("panelId");

                    JSONObject listFilters = null;

                    if (req.getParameter("filters") != null && !req.getParameter("filters").equalsIgnoreCase("null")) {
                        try {
                            listFilters = new JSONObject(req.getParameter("filters"));
                        } catch (JSONException jse) {
                            logger.error("JSONException in workflow search filter: "
                                + req.getParameter("filters").toString());
                            listFilters = null;
                        }
                    }

                    if (listFilters != null) {

                        // set the appropriate search term in the context
                        for (Iterator iterJson = listFilters.keys(); iterJson.hasNext(); ) {
                            String key = (String) iterJson.next();
                            String val = null;
                            try {
                                val = WorkflowXmlUtils.htmlDelimitQuotes(listFilters.getString(key));
                            } catch (JSONException e) {
                                logger.error("JSONException in workflow search filters: "
                                    + listFilters.toString() + " retrieving key: " + key);
                            }
                            if (val != null && !val.isEmpty()) {
                                workflowContext.setSearchAttribute(key, val);
                                logger.info("Search workflows by key (" + key + "), val (" + val + ")");
                            } else {
                                workflowContext.removeSearchAttribute(key);
                                logger.info("Remove workflows search key (" + key + "), val (" + val + ")");
                            }
                        }
                    }


                    if (req.getParameter("datasetId") != null
                            && req.getParameter("datasetId").matches("\\d+")) {
                                DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                                Integer datasetId = Integer.parseInt(req.getParameter("datasetId"));
                                datasetItem = dsDao.get(datasetId);
                                if (datasetItem != null) {
                                    workflowContext.setSearchAttribute(
                                        WorkflowContext.WF_SEARCH_DATASET, datasetItem.getId().toString());

                                    logger.info("Search workflows by key (" + WorkflowContext.WF_SEARCH_DATASET
                                        + "), val (" + datasetItem.getId().toString() + ")");
                                }
                        }


                    // keep track of pages
                    if (req.getParameter("pageNumber") != null
                            && req.getParameter("pageNumber").matches("[0-9]+")) {
                        workflowContext.setPage(panelId, Integer.parseInt(req.getParameter("pageNumber")));
                    }

                    if (req.getParameter("pageLimit") != null
                            && req.getParameter("pageLimit").matches("[0-9]+")) {
                        workflowContext.setPageLimit(Integer.parseInt(req.getParameter("pageLimit")));
                    }

                    List<WorkflowFolderItem> emptyFolders = null;

                    if (panelId != null) {
                        if (panelId.equalsIgnoreCase("my-workflows-panel")) {
                            // set owner in criteria
                            workflowContext.setSearchAttribute(WorkflowContext.WF_SEARCH_PANEL, "my-workflows-panel");
                            emptyFolders = wfFolderDao.findMyEmptyFolders(loggedInUserItem);
                        } else if (panelId.equalsIgnoreCase("shared-workflows-panel")) {
                            // set non-owner in criteria
                            workflowContext.setSearchAttribute(WorkflowContext.WF_SEARCH_PANEL, "shared-workflows-panel");
                        } else if (panelId.equalsIgnoreCase("recommended-workflows-panel")) {
                            // set recommended flag in criteria
                            workflowContext.setSearchAttribute(WorkflowContext.WF_SEARCH_PANEL, "recommended-workflows-panel");
                        }
                    }

                    req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);

                    // Workflow list data
                    JSONArray wfRowsJson = new JSONArray();

                    JSONArray accessRowsJson = new JSONArray();

                    // Include workflow counts
                    Long myWorkflowCount = extractCount(wfFolderDao.queryWorkflows(loggedInUserItem,
                        workflowContext, "my-workflows-panel",
                            WorkflowFolderDaoHibernate.QUERY_CONDITION.GET_COUNT));
                    Long sharedWorkflowCount = extractCount(wfFolderDao.queryWorkflows(loggedInUserItem,
                        workflowContext, "shared-workflows-panel",
                            WorkflowFolderDaoHibernate.QUERY_CONDITION.GET_COUNT));
                    Long recommendedWorkflowCount = extractCount(wfFolderDao.queryWorkflows(loggedInUserItem,
                        workflowContext, "recommended-workflows-panel",
                            WorkflowFolderDaoHibernate.QUERY_CONDITION.GET_COUNT));

                    if (panelId != null) {
                        Integer pageNum = workflowContext.getPage(panelId);
                        // Check that the page is valid
                        if (panelId.equalsIgnoreCase("my-workflows-panel")) {
                            if (pageNum > 1 + Math.ceil(myWorkflowCount / workflowContext.getPageLimit())) {
                                workflowContext.setPage(panelId, 1);
                            }
                        } else if (panelId.equalsIgnoreCase("shared-workflows-panel")) {
                            if (pageNum > 1 + Math.ceil(sharedWorkflowCount / workflowContext.getPageLimit())) {
                                workflowContext.setPage(panelId, 1);
                            }
                        } else if (panelId.equalsIgnoreCase("recommended-workflows-panel")) {
                            if (pageNum > 1 + Math.ceil(myWorkflowCount / workflowContext.getPageLimit())) {
                                workflowContext.setPage(panelId, 1);
                            }
                        }
                    }

                    // Include empty folders
                    if (emptyFolders != null) {
                        for (WorkflowFolderItem wfFolderItem : emptyFolders) {
                            JSONObject wfRowJson = new JSONObject();

                            if (wfFolderItem != null) {
                                try {
                                    wfRowJson.put("folderId", wfFolderItem.getId());
                                    wfRowJson.put("folderName", wfFolderItem.getWorkflowFolderName());
                                } catch (JSONException e) {
                                    break;
                                }
                            }

                            wfRowsJson.put(wfRowJson);

                        }
                    }
                    // Workflow folder and workflow item data ordered by criteria
                    List<Object[]> workflowRowResults =
                            wfFolderDao.queryWorkflows(loggedInUserItem, workflowContext, panelId,
                                WorkflowFolderDaoHibernate.QUERY_CONDITION.GET_WORKFLOWS);

                    for (Object[] objectRow : workflowRowResults) {

                        WorkflowFolderItem wfFolderItem = (WorkflowFolderItem) objectRow[0];
                        WorkflowItem wfItem = (WorkflowItem) objectRow[1];


                        JSONObject wfRowJson = new JSONObject();


                        try {

                            wfRowJson.put("id", wfItem.getId());
                            wfRowJson.put("name", wfItem.getWorkflowName());
                            wfRowJson.put("updated", wfItem.getLastUpdated().toString());
                            wfRowJson.put("ownerId", wfItem.getOwner().getId().toString());
                            wfRowJson.put("desc", wfItem.getDescription());
                            wfRowJson.put("state", wfItem.getState());
                            wfRowJson.put("globalFlag", wfItem.getGlobalFlag());
                            wfRowJson.put("paperCount", WorkflowPropertiesHelper.getWorkflowPaperCount(wfItem));
                            wfRowJson.put("tags", WorkflowPropertiesHelper.getWorkflowTagsAsString(wfItem));
                            if (wfFolderItem != null) {
                                wfRowJson.put("folderId", wfFolderItem.getId());
                                wfRowJson.put("folderName", wfFolderItem.getWorkflowFolderName());
                            }

                            wfRowsJson.put(wfRowJson);

                            JSONObject dataAccessJson = workflowIfaceHelper.initializeWorkflowsJson(
                                workflowContext, loggedInUserItem, wfRowJson, isLoggedIn);

                            accessRowsJson.put(dataAccessJson);
                        } catch (JSONException e) {
                            break;
                        }
                    }

                    try {
                        writeJSON(resp,
                            json("panelId", panelId,
                                "wfRowsJson", wfRowsJson,
                                "accessRowsJson", accessRowsJson,
                                "pageNumber", workflowContext.getPage(panelId),
                                "pageLimit", workflowContext.getPageLimit(),
                                "myWorkflowCount", myWorkflowCount,
                                "sharedWorkflowCount", sharedWorkflowCount,
                                "recommendedWorkflowCount", recommendedWorkflowCount,
                                "filters", listFilters));
                    } catch (JSONException e) {
                        writeJsonError(resp, "Could not fetch workflow list.");
                    }
                    return;

                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.createFolder")) {
                    doPostCreateFolder(req, resp, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.deleteFolder")) {
                    doPostDeleteFolder(req, resp, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.renameFolder")) {
                    doPostRenameFolder(req, resp, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("ManageWorkflowsServlet.removeSearchFilter")) {
                    doPostRemoveSearchFilter(req, resp, loggedInUserItem, workflowContext);
                    return;
                } /* Begin explicit file id or "htmlPath" handling. */
                else if (req.getParameter("visualizationId") != null || req.getParameter("downloadId") != null
                        || req.getParameter("htmlId") != null || req.getParameter("htmlPath") != null
                        || req.getParameter("debugFile") != null) {

                    RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_EDIT_JSP_NAME);

                    String parameter = null;
                    Integer fileId = null;
                    String filePath = null;
                    if (req.getParameter("visualizationId") != null
                            && req.getParameter("visualizationId").matches("\\d+")) {
                        // The visualizationId is a FileItem id for visualization image.
                        String fileIdStr = (String) req.getParameter("visualizationId");
                        if (fileIdStr != null && fileIdStr.matches("\\d+")) {
                            parameter = "visualizationId";
                            fileId = Integer.parseInt(fileIdStr);
                        }
                    } else if (req.getParameter("downloadId") != null && req.getParameter("downloadId").matches("\\d+")) {
                        // The exportId is a FileItem id for export file.
                        String fileIdStr = (String) req.getParameter("downloadId");
                        if (fileIdStr != null && fileIdStr.matches("\\d+")) {
                            parameter = "downloadId";
                            fileId = Integer.parseInt(fileIdStr);
                        }
                    } else if (req.getParameter("htmlId") != null && req.getParameter("htmlId").matches("\\d+")) {
                        // The exportId is a FileItem id for export file.
                        String fileIdStr = (String) req.getParameter("htmlId");
                        if (fileIdStr != null && fileIdStr.matches("\\d+")) {
                            parameter = "htmlId";
                            fileId = Integer.parseInt(fileIdStr);
                        }
                    } else if (req.getParameter("htmlPath") != null && !req.getParameter("htmlPath").isEmpty()) {
                        // The exportId is a FileItem id for export file.
                        String filePathStr = (String) req.getParameter("htmlPath");
                        if (filePathStr != null && !filePathStr.isEmpty()) {
                            parameter = "htmlPath";
                            filePath = filePathStr;
                        }
                    } else if (req.getParameter("debugFile") != null && !req.getParameter("debugFile").isEmpty()
                            && req.getParameter("componentId") != null && !req.getParameter("componentId").isEmpty()) {
                        // The exportId is a FileItem id for export file.
                        String filePathStr = (String) req.getParameter("debugFile");
                        if (filePathStr != null && !filePathStr.isEmpty()) {
                            parameter = "debugFile";
                            filePath = filePathStr;
                        }
                    }

                    // Workflows will not share any workflow files by design.
                    // Instead, files are copied to the workflow so that deleting or modifying
                    // files outside of a workflow will not instantly invalidate cached workflow results.
                    if (parameter != null) {

                        // Because workflowId isn't a given here, there is a caveat.
                        // IMPORTANT: Each method below has its own access check, within.
                        // Do not simply give files to any user without checking permissions
                        // and ownership of the file, first.
                        if (parameter.equalsIgnoreCase("visualizationId")) {
                            if (fileId != null) {
                                ServletContext servletCtx = getServletContext();
                                workflowFileHelper.returnImage(workflowDao, workflowHelper, loggedInUserItem, fileId, getBaseDir(), disp, req, resp, servletCtx);
                                return;
                            }
                        } else if (parameter.equalsIgnoreCase("downloadId")) {
                            if (fileId != null) {
                                workflowFileHelper.returnFile(workflowDao, workflowHelper, loggedInUserItem, fileId, getBaseDir(), disp, req, resp);
                                return;
                            }
                        } else if (parameter.equalsIgnoreCase("htmlId")) {
                            if (fileId != null) {
                                workflowFileHelper.returnHtmlFile(workflowDao, workflowHelper, loggedInUserItem, fileId, getBaseDir(), disp, req, resp);
                                return;
                            }
                        } else if (parameter.equalsIgnoreCase("htmlPath")) {
                            if (filePath != null && !filePath.isEmpty()) {
                                workflowFileHelper.returnHtmlFileFromPath(workflowDao, workflowHelper, loggedInUserItem, filePath, getBaseDir(), disp, req, resp);
                                return;
                            }
                        } else if (parameter.equalsIgnoreCase("debugFile")) {
                            if (filePath != null && !filePath.isEmpty()) {
                                if (filePath.matches("(?i)[a-zA-Z0-9\\-_\\.]*\\.wfl")
                                        || filePath.equals("WorkflowComponent.log")) {
                                    // In this case, "filePath" is the file name
                                    workflowFileHelper.returnUnregisteredFile(workflowDao, workflowHelper, loggedInUserItem, getBaseDir(), disp, req, resp, filePath);
                                    return;

                                }
                            }
                        }
                    }
                    return;
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

                    // If none of the above methods were relevant, then we shall get the workflows list
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
            } else {

                // Do the same thing if this isn't the owner of a private workflow.
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

        } else { // User not logged in

            if (req.getParameter("requestingMethod") != null
                    && ((req.getParameter("requestingMethod").equals("ManageWorkflowsServlet.displayPreviousResults")
                         && req.getParameter("editorWindow") == null))) {
                    // fall through if displaying previous results and editorWindow is null
            } else {
                // Otherwise, let the editor know the user is logged out.
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

        }

        RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_LIST_JSP_NAME);
        // The user is not logged in
        logger.debug("Forwarding to JSP " + LOGIN_JSP_NAME);
        disp = getServletContext().getRequestDispatcher(LOGIN_JSP_NAME);
        disp.forward(req, resp);
        return;
    }


    /** Workflow List functionality. */


    /**
     * Defines which sorting parameters to use for sorting workflow rows based on the user selected column; handles
     * ascending or descending.
     *
     * @param sortByString the column name to sort by
     * @param isAscending the boolean indicating ascending or descending sort
     * @return the SortParameter array
     */
    public static SortParameter[] selectSortParameters(String sortByString, Boolean isAscending) {
        return WorkflowRowDto.selectSortParameters(sortByString, isAscending);
    }

    /**
     * To avoid lots of code duplication, the workflows count query and the workflows item query
     * are the same method with an additional QUERY_CONDITION parameter.
     * @param daoResults the List of Object[2]
     *   (index [0] is the key "count", and [1] is the value)
     * @return
     */
    private Long extractCount(List<Object[]> daoResults) {
        Long count = 0L;
        for (Object[] obj : daoResults) {
            if (obj[0] != null && ((String) obj[0]).equalsIgnoreCase("count")) {
                count = (Long) obj[1];
            }
        }
        return count;
    }

    private void doPostEditWorkflow(HttpServletRequest req, HttpServletResponse resp,
            WorkflowItem workflowItem,
            DatasetItem datasetItem, UserItem loggedInUserItem, String minAuthLevel) {
        if (workflowItem != null && loggedInUserItem != null) {
            WorkflowFileHelper workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
            WorkflowPaperMapDao wfpmDao = DaoFactory.DEFAULT.getWorkflowPaperMapDao();
            String componentTypeHierarchyJson = null;
            String recentComponentsJson = null;
            RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_EDIT_JSP_NAME);

            Boolean isView = true;
            if (workflowItem.getOwner() != null && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                isView = false;
            }
            String logInfo = "access: edit";
            if (isView) {
                logInfo = "access: view";
            }

            workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, datasetItem,
                    WorkflowHelper.LOG_EDIT_WORKFLOW, logInfo, null);

            try {
                componentTypeHierarchyJson = ComponentHelper
                    .getComponentTypeHierarchy(commonResourcesDir + "ComponentTypeHierarchy.xml");

                recentComponentsJson = ComponentHelper
                        .getRecentlyUsedComponents(loggedInUserItem.getId().toString());

            } catch (IOException e1) {
                logger.error("Could not access ComponentTypeHierarchy.xml");
            } catch (JSONException e) {
                logger.error("Could not access recent components for user "
                    + loggedInUserItem.getId().toString());
            }

            String isViewXml = "<isView>" + isView.toString() + "</isView>";

            JSONObject componentOptionTypes = null;
            String componentInputEndpoints = new String();
            String componentOutputEndpoints = new String();
            String componentOptionDependencies = new String();
            String results = new String();
            String workflowXml = null;


            try {
                componentOptionTypes = ComponentHelper.getAllOptionTypes();
                req.setAttribute("wfOpt", componentOptionTypes);
            } catch (JDOMException e) {
                logger.error("Could not retrieve option types");
            } catch (IOException e) {
                logger.error("Could not retrieve option types");
            }
            try {
                componentInputEndpoints = "<componentInputEndpoints>"
                        + connectionHelper.getEndpoints(ConnectionHelper.XML_INPUT_DEFS) + "</componentInputEndpoints>";
            } catch (JDOMException e) {
                logger.error("JDOMException while retrieving component input endpoints from XSD files.");
            } catch (IOException e) {
                logger.error("IOException while retrieving component input endpoints from XSD files.");
            }
            try {
                componentOutputEndpoints = "<componentOutputEndpoints>"
                        + connectionHelper.getEndpoints(ConnectionHelper.XML_OUTPUT_DEFS) + "</componentOutputEndpoints>";
            } catch (JDOMException e) {
                logger.error("JDOMException while retrieving component output endpoints from XSD files.");
            } catch (IOException e) {
                logger.error("IOException while retrieving component output endpoints from XSD files.");
            }

            JSONObject fullOptionConstraintMap = OptionDependencyHelper.getFullOptionConstraintMap();
            if (fullOptionConstraintMap != null) {
                componentOptionDependencies = "<fullOptionConstraintMap>"
                    + fullOptionConstraintMap.toString() + "</fullOptionConstraintMap>";
            }


            try {
                Boolean hasAnyFile = false;
                // Get the previous workflow XML from the workflow persistence table
                Hibernate.initialize(workflowItem);
                if (minAuthLevel != null && minAuthLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                        && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                    workflowXml = workflowHelper.restoreSavedWorkflow(workflowItem);
                    JSONObject lastErrorMessageJson = workflowHelper.getLastErrorMessage(workflowItem);
                    ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                    List<ComponentFileItem> cfis = compFileDao.findByWorkflow(workflowItem);
                    if (!cfis.isEmpty()) {
                        hasAnyFile = true;
                    }

                    ComponentFilePersistenceDao compFilePersistenceDao =
                        DaoFactory.DEFAULT.getComponentFilePersistenceDao();
                    List<ComponentFilePersistenceItem> cfpis = compFilePersistenceDao.findByWorkflow(workflowItem);
                    if (!cfpis.isEmpty()) {
                        hasAnyFile = true;
                        for (ComponentFilePersistenceItem cfpi : cfpis) {
                            ComponentFileItem cfItem = compFileDao.findByWorkflowAndFile(workflowItem, cfpi.getFile());
                            if (cfItem == null) {
                                ComponentFileItem newCfpi = new ComponentFileItem(cfpi.getWorkflow(), cfpi.getComponentId(), cfpi.getDataset(), cfpi.getFile());
                                compFileDao.saveOrUpdate(newCfpi);
                            } else {
                                Hibernate.initialize(cfItem);
                                cfItem.setComponentId(cfpi.getComponentId());
                                cfItem.setDataset(cfpi.getDataset());
                                compFileDao.saveOrUpdate(cfItem);
                            }
                        }
                    }

                    // If they exist, load the backup XML files.
                    String baseDir = WorkflowFileUtils.getStrictDirFormat(getBaseDir());
                    File componentsDir = new File(baseDir + "/"
                        + WorkflowFileUtils.getWorkflowsDir(workflowItem.getId()) + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME);

                    File[] files = componentsDir.listFiles();
                    for (int idx = 0; files != null && idx < files.length; idx++) {
                        File fileOrDir = files[idx];
                        String backupFilePath = WorkflowFileUtils.sanitizePath(fileOrDir.getAbsolutePath());
                        if (fileOrDir.isFile()) {
                            if (backupFilePath.endsWith(".bak")) {
                                Integer indexOfBackupExt = backupFilePath.lastIndexOf(".bak");
                                String componentXmlFilePath = backupFilePath.substring(0, indexOfBackupExt);
                                File componentXmlFile = new File(componentXmlFilePath);
                                File backupFile = new File(backupFilePath);
                                if (backupFile != null && backupFile.exists()) {
                                    try {
                                        FileUtils.copyFile(backupFile, componentXmlFile);
                                    } catch (IOException e) {
                                        logger.error("Could not create non-persistent component XML file.");
                                    }
                                }
                            }
                        }
                    } // end for loop

                    if (workflowItem.getResults() != null) {
                        String modifiedResults = workflowFileHelper.filterResultsByAuth(workflowHelper, workflowItem, loggedInUserItem, getBaseDir());
                        if (modifiedResults != null) {
                            results = "<results>" + modifiedResults + "</results>";
                        }

                    }
                    if (lastErrorMessageJson != null) {
                        req.setAttribute("lastErrorMessage", lastErrorMessageJson);
                    }
                } else if (minAuthLevel != null && (minAuthLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW) || minAuthLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {

                    WorkflowPersistenceDao wpDao = DaoFactory.DEFAULT.getWorkflowPersistenceDao();
                    WorkflowPersistenceItem wpItem = wpDao.findByWorkflow(workflowItem);
                    if (wpItem != null) {
                        Hibernate.initialize(wpItem);
                        workflowXml = WorkflowAccessHelper.pruneWorkflowXml(workflowItem, loggedInUserItem, getBaseDir());

                        JSONObject lastErrorMessageJson = workflowHelper.getLastErrorMessage(workflowItem);

                        if (workflowItem.getResults() != null) {
                            String modifiedResults = workflowFileHelper.filterResultsByAuth(workflowHelper, workflowItem, loggedInUserItem, getBaseDir());
                            if (modifiedResults != null) {
                                results = "<results>" + modifiedResults + "</results>";
                            }

                        }
                        if (lastErrorMessageJson != null) {
                            req.setAttribute("lastErrorMessage", lastErrorMessageJson);
                        }
                    }

                }

                try {
                    String feedbackWorkflowIdElement = null;
                    if (workflowItem != null) {
                        feedbackWorkflowIdElement = "<feedbackId>" + workflowItem.getId() + "</feedbackId>";
                    }

                    String isLoggedIn = null;
                    if (!loggedInUserItem.getId().equals(UserItem.DEFAULT_USER)) {
                        isLoggedIn = "<isLoggedIn>true</isLoggedIn>";
                    } else {
                        isLoggedIn = "<isLoggedIn>false</isLoggedIn>";
                    }

                    if (minAuthLevel == null) {
                        // Only if the min auth level is not edit or view do we take
                        // extra steps to prune the workflow and return those results
                        // that are allowed.
                        WorkflowPersistenceDao wpDao = DaoFactory.DEFAULT.getWorkflowPersistenceDao();
                        WorkflowPersistenceItem wpItem = wpDao.findByWorkflow(workflowItem);
                        if (wpItem != null) {
                            Hibernate.initialize(wpItem);
                            workflowXml = WorkflowAccessHelper.pruneWorkflowXml(workflowItem, loggedInUserItem, getBaseDir());

                            JSONObject lastErrorMessageJson = workflowHelper.getLastErrorMessage(workflowItem);

                            if (workflowItem.getResults() != null) {
                                String modifiedResults = workflowFileHelper.filterResultsByAuth(workflowHelper, workflowItem, loggedInUserItem, getBaseDir());
                                if (modifiedResults != null) {
                                    results = "<results>" + modifiedResults + "</results>";
                                }
                            }
                            if (lastErrorMessageJson != null) {
                                req.setAttribute("lastErrorMessage", lastErrorMessageJson);
                            }
                        }
                    }

                    String isGlobalFlag = workflowItem.getGlobalFlag() ? "true" : "false";

                    String isShared = "<isShared>" + isGlobalFlag + "</isShared>";
                    workflowXml = WorkflowAccessHelper.removeDataFromPrivateOptions(workflowXml, workflowItem,
                            loggedInUserItem, getBaseDir(), false);

                    String wfDatasetsXml = WorkflowHelper.getWorkflowDatasets(workflowItem);

                    req.setAttribute(
                        "wfXml",
                            org.json.XML.toJSONObject(isViewXml + isLoggedIn + isShared
                                + workflowXml
                                    + results
                                        + feedbackWorkflowIdElement
                                            + componentInputEndpoints
                                                + componentOutputEndpoints
                                                    + componentOptionDependencies
                                                      + wfDatasetsXml));

                    JSONObject componentInfoJson = ComponentHelper.getComponentInfoDivs();
                    // Do not convert info.xml divs to json
                    req.setAttribute("wfSupportingXml", componentInfoJson);
                    req.setAttribute("wfComponentMenuJson", componentTypeHierarchyJson);
                    req.setAttribute("recentComponentsJson", recentComponentsJson);
                    JSONObject wfEditLevel = new JSONObject();

                    if (datasetItem != null) {
                        req.setAttribute("datasetId", datasetItem.getId().toString());
                    }

                    WorkflowRowDto projectRequestInfo = workflowIfaceHelper.getProjectRequestInfo(loggedInUserItem, (Long) workflowItem.getId());
                    List<ProjectItem> unrequestedProjects = Collections.synchronizedList(projectRequestInfo.getProjects());
                    List<ProjectItem> accessibleProjects = Collections.synchronizedList(projectRequestInfo.getAccessibleProjects());
                    List<ProjectItem> pendingRequestProjects = Collections.synchronizedList(projectRequestInfo.getActiveRequestProjects());
                    List<ProjectItem> reRequestProjects = Collections.synchronizedList(projectRequestInfo.getReRequestProjects());
                    List<ProjectItem> nonShareableProjects = Collections.synchronizedList(projectRequestInfo.getNonShareableProjects());
                    req.setAttribute("hasUnownedPrivateFiles", projectRequestInfo.getHasUnownedPrivateFiles());

                    Boolean hasUnrequested = false;
                    Boolean hasAccessible = false;
                    Boolean hasPending = false;
                    Boolean hasReRequest = false;
                    Boolean hasNonShareable = false;
                    if (unrequestedProjects != null && !unrequestedProjects.isEmpty()) {
                        hasUnrequested = true;
                        req.setAttribute("unrequestedProjects", unrequestedProjects);
                    }
                    if (accessibleProjects != null && !accessibleProjects.isEmpty()) {
                        hasAccessible = true;
                        req.setAttribute("accessibleProjects", accessibleProjects);
                    }
                    if (pendingRequestProjects != null && !pendingRequestProjects.isEmpty()) {
                        hasPending = true;
                        req.setAttribute("pendingRequestProjects", pendingRequestProjects);
                    }
                    if (reRequestProjects != null && !reRequestProjects.isEmpty()) {
                        hasReRequest = true;
                        req.setAttribute("reRequestProjects", reRequestProjects);
                    }
                    if (nonShareableProjects != null && !nonShareableProjects.isEmpty()) {
                        hasNonShareable = true;
                        req.setAttribute("nonShareableProjects", nonShareableProjects);
                    }

                    Integer paperCount = 0;
                    if (workflowItem != null) {
                        List<WorkflowPaperMapItem> paperMappings = wfpmDao.findByWorkflow(workflowItem);
                        if (paperMappings != null && !paperMappings.isEmpty()) {
                            paperCount = paperMappings.size();
                        }
                    }
                    req.setAttribute("paperCount", paperCount);

                    Boolean hasPrivateOrRequestableData = (hasUnrequested || hasAccessible || hasPending || hasReRequest)
                            && (hasNonShareable || projectRequestInfo.getHasUnownedPrivateFiles());
                    // The first case covers partial view data (includes generates with no predessors, too)
                    if (hasPrivateOrRequestableData) {
                        wfEditLevel.put("buttonState", "partialView");
                    // Includes private files whose access cannot be requested
                    } else if (hasAnyFile && !hasUnrequested && !hasAccessible && !hasPending && !hasNonShareable && !hasReRequest) {
                        wfEditLevel.put("buttonState", "unattachedFiles");
                    // Shows that no data for the workflow exists
                    } else if (!hasAnyFile && !hasUnrequested && !hasAccessible && !hasPending && !hasNonShareable && !hasReRequest) {
                        wfEditLevel.put("buttonState", "noData");
                    } else if (hasNonShareable) {
                        wfEditLevel.put("buttonState", "nonshareable");
                    } else if (hasUnrequested) {
                        wfEditLevel.put("buttonState", "shareable");
                    } else if (hasReRequest) {
                        wfEditLevel.put("buttonState", "reRequest");
                    } else if (hasPending) {
                        wfEditLevel.put("buttonState", "pending");
                    } else if (hasAccessible) {
                        wfEditLevel.put("buttonState", "accessible");
                    }

                    wfEditLevel.put("level", minAuthLevel);
                    req.setAttribute("wfEditLevel", wfEditLevel);

                    req.setAttribute("isDsAdmin", loggedInUserItem.getAdminFlag());
                    req.setAttribute("wfState", workflowItem.getState());

                    disp.forward(req, resp);
                    return;

                } catch (IOException e) {
                    logger.error("Error retrieving workflow.");
                } catch (ServletException e) {
                    logger.error("Error forwarding to workflows page.");
                }
            } catch (JSONException exception) {
                logger.error("JSONException while retrieving workflow.");
                return;
            }

        }
    }

    /**
     * Save the workflow item.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param datasetItem the optional dataset item
     * @param loggedInUserItem the logged in user
     */
    private void doPostSaveAsNewWorkflow(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            DatasetItem datasetItem, UserItem loggedInUserItem, String datasetAuthLevel) {

        if (loggedInUserItem != null) {
            WorkflowFileHelper workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();

            Long workflowId = null;
            Integer datasetId = null;
            if (datasetItem != null) {
                datasetId = (Integer) datasetItem.getId();
            }
            if (workflowItem != null) {
                workflowId = workflowItem.getId();

            }
            String newWorkflowName = req.getParameter("workflowName");

            String isSharedString = req.getParameter("isShared");
            Boolean isShared = false;
            if (isSharedString != null
                    && (isSharedString.equalsIgnoreCase("true") || isSharedString.equalsIgnoreCase("false"))) {
                isShared = Boolean.parseBoolean(isSharedString);
            }

            try {
                // Trigger session-caching of type hierarchy and recently used components.
                ComponentHelper
                        .getComponentTypeHierarchy(commonResourcesDir
                            + "ComponentTypeHierarchy.xml");
                ComponentHelper
                    .getRecentlyUsedComponents(loggedInUserItem.getId().toString());
            } catch (IOException e1) {
                logger.error("Could not access ComponentTypeHierarchy.xml");
            } catch (JSONException e) {
                logger.error("Could not access recent components for user "
                    + loggedInUserItem.getId().toString());
            }

            String [] workflowTags = req.getParameterValues("workflowTags");

            String workflowDescription = req.getParameter("workflowDescription") == null ? "" :
                WorkflowFileUtils.htmlEncode(req.getParameter("workflowDescription"));

            try {
                if (newWorkflowName != null && !newWorkflowName.isEmpty()) {
                    if (!loggedInUserItem.getAdminFlag()
                            && (datasetAuthLevel == null
                            || !(datasetAuthLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_EDIT)
                                || datasetAuthLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_ADMIN)))) {
                        datasetItem = null;
                    }

                    Long folderId = null;
                    try {
                        folderId = Long.parseLong(req.getParameter("folderId"));
                    } catch (NumberFormatException nfe) {
                        // do not log error here, it can be empty string
                    }
                    WorkflowItem newWorkflowItem = workflowFileHelper.createWorkflowItem(workflowDao, workflowHelper,
                        workflowId, datasetItem, loggedInUserItem,
                        newWorkflowName, workflowDescription, isShared, true, getBaseDir(), req, resp);

                    if (newWorkflowItem != null) {

                        WorkflowFolderDao wffDao = DaoFactory.DEFAULT.getWorkflowFolderDao();
                        WorkflowFolderMapDao wffMapDao = DaoFactory.DEFAULT.getWorkflowFolderMapDao();
                        if (folderId != null && folderId != 0) {
                            WorkflowFolderItem wffItem = wffDao.get(folderId);
                            if (wffItem != null && wffItem.getOwner().getId()
                                .equals(loggedInUserItem.getId())) {
                                // mck dup 9
                                WorkflowFolderMapItem wffMapItem = new WorkflowFolderMapItem();
                                wffMapItem.setWorkflowExternal(newWorkflowItem);
                                wffMapItem.setWorkflowFolderExternal(wffItem);
                                wffMapDao.saveOrUpdate(wffMapItem);
                            }
                        } else if (folderId != null && folderId == 0) {
                            String folderName = req.getParameter("newFolderName");
                            if (folderName != null) {
                                String safeFolderName = WorkflowXmlUtils.htmlDelimitQuotes(folderName);
                                if (!safeFolderName.isEmpty()) {
                                    workflowFileHelper.moveToOrCreateFolder(wffDao, loggedInUserItem, safeFolderName, newWorkflowItem);
                                }
                            }
                        }

                        // Add workflow tags
                        if (workflowTags != null) {
                            for (String tag : workflowTags) {
                                if (!WorkflowPropertiesHelper.addTag(newWorkflowItem, tag)) {
                                    logger.error("Could not add tag '" + tag + "' for Workflow (" + workflowId + ")");
                                }
                            }
                        }
                        workflowId = newWorkflowItem.getId();

                        // The getEndpoints call is still required for
                        // finalInputEndpoints and finalOutputEndpoints to be tested (and possibly set).

                        try {
                            connectionHelper.getEndpoints(ConnectionHelper.XML_INPUT_DEFS);
                        } catch (IOException e) {
                            logger.error("Could not read input endpoints for Workflow (" + workflowId + ")");
                        } catch (JDOMException e) {
                            logger.error("Could not parse JSON input endpoints for Workflow (" + workflowId + ")");
                        }
                        try {
                            connectionHelper.getEndpoints(ConnectionHelper.XML_OUTPUT_DEFS);
                        } catch (IOException e) {
                            logger.error("Could not read output endpoints for Workflow (" + workflowId + ")");
                        } catch (JDOMException e) {
                            logger.error("Could not parse JSON output endpoints for Workflow (" + workflowId + ")");
                        }


                        if (datasetId != null) {
                            req.setAttribute("datasetId", datasetId.toString());
                        }

                        if (newWorkflowItem != null) {

                            workflowHelper.restoreSavedWorkflow(newWorkflowItem);
                            //JSONObject lastErrorMessageJson = workflowHelper.getLastErrorMessage(newWorkflowItem);

                            ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();

                            ComponentFilePersistenceDao compFilePersistenceDao =
                                DaoFactory.DEFAULT.getComponentFilePersistenceDao();
                            List<ComponentFilePersistenceItem> cfpis = compFilePersistenceDao.findByWorkflow(newWorkflowItem);
                            if (!cfpis.isEmpty()) {
                                for (ComponentFilePersistenceItem cfpi : cfpis) {
                                    ComponentFileItem cfItem = compFileDao.findByWorkflowAndFile(newWorkflowItem, cfpi.getFile());
                                    if (cfItem == null) {
                                        ComponentFileItem newCfpi = new ComponentFileItem(newWorkflowItem, cfpi.getComponentId(), cfpi.getDataset(), cfpi.getFile());
                                        compFileDao.saveOrUpdate(newCfpi);
                                    } else {
                                        Hibernate.initialize(cfItem);
                                        cfItem.setComponentId(cfpi.getComponentId());
                                        cfItem.setDataset(cfpi.getDataset());
                                        compFileDao.saveOrUpdate(cfItem);
                                    }
                                }
                            }

                            // If they exist, load the backup XML files.
                            String baseDir = WorkflowFileUtils.getStrictDirFormat(getBaseDir());
                            File componentsDir = new File(baseDir + "/"
                                + WorkflowFileUtils.getWorkflowsDir(newWorkflowItem.getId()) + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME);

                            File[] files = componentsDir.listFiles();
                            for (int idx = 0; files != null && idx < files.length; idx++) {
                                File fileOrDir = files[idx];
                                String backupFilePath = WorkflowFileUtils.sanitizePath(fileOrDir.getAbsolutePath());
                                if (fileOrDir.isFile()) {
                                    if (backupFilePath.endsWith(".bak")) {
                                        Integer indexOfBackupExt = backupFilePath.lastIndexOf(".bak");
                                        String componentXmlFilePath = backupFilePath.substring(0, indexOfBackupExt);
                                        File componentXmlFile = new File(componentXmlFilePath);
                                        File backupFile = new File(backupFilePath);
                                        if (backupFile != null && backupFile.exists()) {
                                            try {
                                                FileUtils.copyFile(backupFile, componentXmlFile);
                                            } catch (IOException e) {
                                                logger.error("Could not create non-persistent component XML file.");
                                            }
                                        }
                                    }
                                }
                            } // end for loop

                            // Update wfc adjacency table.
                            ConnectionHelper.updateAdjacencyList(getBaseDir(), newWorkflowItem, loggedInUserItem);

                        }

                        Integer toDatasetId = null;
                        if (datasetItem != null) {
                            // if not  null, then user has edit or view access to project
                            toDatasetId = (Integer) datasetItem.getId();
                        }
                        writeJSON(resp, json("success", true,
                                "workflowId", newWorkflowItem.getId(),
                                "datasetId", toDatasetId, "message",
                            "Successfully created workflow (" + newWorkflowItem.getWorkflowName() + ")"));
                        return;
                    } else {
                        writeJSON(resp, json("success", false,
                            "message", "Could not create workflow."));
                    }
                } else {
                    writeJSON(resp, json("success", false,
                        "message", "Invalid workflow name."));
                }
            } catch (JSONException exception) {
                logger.error("Error saving as new workflow for new owner.");
                writeJsonError(resp, "Error saving as new workflow for new owner.");
                return;
            } catch (IOException e) {
                logger.error("Could not forward request.");
                writeJsonError(resp, "Could not forward request.");
                return;
            }
        }
    }

    /**
     * Delete the workflow item.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param datasetItem the optional dataset item
     * @param loggedInUserItem the logged in user
     */
    private void doPostDeleteWorkflow(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            DatasetItem datasetItem, UserItem loggedInUserItem) {
        ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())
                    || loggedInUserItem.getAdminFlag())) {
            logger.info("Deleting workflow (" + workflowItem.getId() + ")");
            workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, datasetItem,
                    WorkflowHelper.LOG_DELETE_WORKFLOW, "", null);

            Long workflowId = workflowItem.getId();
            Integer datasetId = null;
            if (datasetItem != null) {
                datasetId = (Integer) datasetItem.getId();
            }

            File workflowsDir = new File(WorkflowFileUtils.getStrictDirFormat(getBaseDir())
                + WorkflowFileUtils.getWorkflowsDir(workflowId));

            if (!FileUtils.deleteFile(workflowsDir)) {
                logger.error("Could not delete " + workflowsDir.getAbsolutePath());
            }
            try {
                WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                ComponentFileDao wmfDao = DaoFactory.DEFAULT.getComponentFileDao();
                logger.trace("Deleting files for workflow (" + workflowItem.getId() + ")");
                List<ComponentFileItem> cfItems = wmfDao.findByWorkflow(workflowItem);

                for (ComponentFileItem wmfItem : cfItems) {
                    WorkflowFileItem fileItem = wfFileDao.get((Integer) wmfItem.getFile().getId());
                    wfFileDao.delete(fileItem);
                }
                // These should be deleted when workflow_file records are deleted, but let's be sure.
                workflowFileHelper.deleteComponentFileItems(compFileDao, workflowItem, getBaseDir());

                // Delete associated workflow Tags
                deleteAllWorkflowTags(workflowItem);

                // Delete entire workflow directory
                try {
                    WorkflowFileUtils.deleteWorkflowDir(workflowItem, getBaseDir());
                    logger.trace("Workflow files for (" + workflowId + ") deleted.");
                } catch (IOException e) {
                    logger.error("Could not delete workflow directory for " + workflowItem.getWorkflowName() + " ("
                            + workflowItem.getId() + ")");
                }
                // Delete the workflow item from the database
                workflowDao.delete(workflowItem);
                logger.info("Workflow " + workflowId + " deleted.");

                try {
                    writeJSON(
                            resp,
                            json("workflowName", workflowItem.getWorkflowName(), "datasetId", datasetId, "lastUpdated",
                                    workflowItem.getLastUpdated()));
                    return;
                } catch (JSONException exception) {
                    writeJsonError(resp, "Error deleting workflow.");
                    return;
                }
            } catch (Exception e) {
                logger.error("Could not delete workflow. Please resolve manually. " + e.toString());
            }
        }

        writeJsonError(resp, "Not authorized to delete workflow.");
        return;
    }

    /**
     * Get state information for the user's workflows.
     *
     * @param loggedInUserItem the user item
     * @param datasetItem the optional dataset item
     */
    private void doPostGetWorkflowStates(HttpServletRequest req, HttpServletResponse resp, UserItem loggedInUserItem,
            DatasetItem datasetItem) {
        JSONObject jsonObj = new JSONObject();
        logger.trace("Getting workflow states.");

        if (loggedInUserItem != null) {
            List<WorkflowItem> workflowItems = null;
            if (datasetItem != null) {
                logger.trace("Dataset: " + datasetItem);
                workflowItems = workflowDao.find(datasetItem);
            } else {
                logger.trace("User: " + loggedInUserItem);
                workflowItems = workflowDao.findByOwner(loggedInUserItem);
            }

            if (workflowItems != null) {
                logger.trace("Number of user owned workflows = " + workflowItems.size());
                for (WorkflowItem userOwnedItem : workflowItems) {
                    Hibernate.initialize(userOwnedItem);

                    try {
                        jsonObj.put(userOwnedItem.getId().toString(), userOwnedItem.getState());
                    } catch (JSONException e) {
                        logger.error("Could not fetch workflow status for workflow (" + userOwnedItem.getId() + ")");
                    }
                }
            }
        }

        try {
            writeJSON(resp, json("success", true, "workflows", jsonObj));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error fetching workflow state.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "IOException occurred while writing workflow state information to response.");
        }
    }

    /**
     * Handles cancel requests for the workflow from the UI.
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the WorkflowItem to save
     * @param datasetItem the optional DatasetItem
     * @param loggedInUserItem the UserItem of the logged-in user
     * @see edu.cmu.pslc.datashop.workflows.WorkflowImportHelper#saveWorkflowToDatabase(WorkflowItem)
     */
    private void doPostHandleCancelRequests(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            DatasetItem datasetItem, UserItem loggedInUserItem) {

        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())
                    || loggedInUserItem.getAdminFlag())) {
            Long workflowId = workflowItem.getId();
            String requestingMethod = req.getParameter("requestingMethod");

            workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, datasetItem,
                    WorkflowHelper.LOG_CANCEL_WORKFLOW, "", null);

            if (requestingMethod.equals("ManageWorkflowsServlet.cancelWorkflow")) {
                // Or the workflow can be saved since it has been canceled.

                if (workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                    // Update workflow state to dirty. Proceed with saving workflow.
                    workflowItem.setState(WorkflowItem.WF_STATE_NEW);
                    workflowDao.saveOrUpdate(workflowItem);
                    WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();

                    List<WorkflowComponentInstanceItem> componentItems
                        = wciDao.findByWorkflow(workflowItem);
                    if (componentItems != null && !componentItems.isEmpty()) {
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
                    }

                } else if (workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                    // Proceed with saving workflow by falling through this logic block into the next.
                }
            }
            try {

                writeJSON(resp, json("success", true, "workflowName", workflowItem.getWorkflowName(), "workflowId", workflowId));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "An error occurred while trying to cancel the current workflow " + workflowId);
                return;
            } catch (IOException e) {
                writeJsonError(resp, "An error occurred while trying to save the current workflow " + workflowId);
                return;
            }
        } else {
            this.writeJsonError(resp, "No permission to cancel workflow.");
        }
    }

    /**
     * Delete all tags associated with a workflow
     * @param workflowItem
     */
    private void deleteAllWorkflowTags(WorkflowItem workflowItem) {
        try {
            WorkflowTagMapDao wfTagMapDao = DaoFactory.DEFAULT.getWorkflowTagMapDao();
            List<WorkflowTagMapItem> tagMapItems = wfTagMapDao.findByWorkflow(workflowItem);

            for (int i = 0; i < tagMapItems.size(); i++) {
                WorkflowTagMapId mapId = (WorkflowTagMapId) tagMapItems.get(i).getId();

                if (mapId != null) {
                    Long tagId = mapId.getWorkflowTagId();
                    WorkflowTagDao wfTagDao = DaoFactory.DEFAULT.getWorkflowTagDao();
                    WorkflowTagItem tagItem = wfTagDao.get(tagId);
                    if (tagItem != null) {
                        wfTagDao.delete(tagItem);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not remove all tags of the workflow being deleted: " + e.toString());
        }
    }

    /**
     * Modify the workflow's description.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostEditDescription(HttpServletRequest req, HttpServletResponse resp,
                                       WorkflowItem workflowItem,
                                       UserItem loggedInUserItem) {
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
            && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {

            Long workflowId = workflowItem.getId();
            // Get latest version of item...
            workflowItem = workflowDao.get(workflowId);

            String newDescription = WorkflowFileUtils.htmlEncode(req.getParameter("newDescription"));

            workflowItem.setDescription(newDescription);
            workflowDao.saveOrUpdate(workflowItem);

            String message = "Successfully updated the description.";
            String messageLevel = "SUCCESS";

            if (newDescription.length() == 0) {
                message = "Successfully removed the description.";
            }

            try {
                writeJSON(
                        resp,
                        json("workflowId", workflowId,
                             "description", newDescription,
                             "message", message,
                             "level", messageLevel));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error renaming workflow.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "IOException occurred while modifying workflow.");
            }
        }
    }

    /**
     * Modify the workflow's shareability.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostEditShareability(HttpServletRequest req, HttpServletResponse resp,
                                        WorkflowItem workflowItem,
                                        UserItem loggedInUserItem) {
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
            && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {

            Long workflowId = workflowItem.getId();
            // Get latest version of item...
            workflowItem = workflowDao.get(workflowId);

            String newGlobalFlag = req.getParameter("newGlobalFlag");
            Boolean globalFlag = false;
            if (newGlobalFlag != null) {
                globalFlag = newGlobalFlag.equalsIgnoreCase("true") ? true : false;
            }

            Date now = new Date();

            workflowItem.setGlobalFlag(globalFlag);
            workflowItem.setLastUpdated(now);
            workflowDao.saveOrUpdate(workflowItem);

            workflowHelper.saveWorkflowToDatabase(workflowItem, getBaseDir());

            String message = "Successfully updated the shareability.";
            String messageLevel = "SUCCESS";

            try {
                writeJSON(
                        resp,
                        json("workflowId", workflowId,
                             "isGlobal", globalFlag,
                             "message", message,
                             "level", messageLevel));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error changing workflow shareability.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "IOException occurred while modifying workflow.");
            }
        }
    }

    /**
     * Handle name, description and shareability changes atomically.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostEditMetaData(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    WorkflowItem workflowItem,
                                    UserItem loggedInUserItem) {

        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
            && (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())
                    || loggedInUserItem.getAdminFlag())) {

            String newWfName = WorkflowFileUtils.htmlEncode(req.getParameter("newWorkflowName"));

            // Ensure that the newWfName is valid xml
            if (!WorkflowPropertiesHelper.isValidWorkflowName(newWfName)) {
                return;
            }

            StringBuffer attrsModified = new StringBuffer();

            Long workflowId = workflowItem.getId();
            String workflowXml = workflowItem.getWorkflowXml();

            String currentWfName = workflowItem.getWorkflowName();
            if (!newWfName.equals(currentWfName)) {
                attrsModified.append("name");

                // Replace 'current' name with 'new' name.
                try {
                    workflowXml = WorkflowPropertiesHelper.updateWorkflowName(workflowXml, newWfName);
                } catch (IOException e) {
                    logger.error("Could not update workflow name in workflow XML.");
                }
            }

            // Update description.
            String newDescription = WorkflowFileUtils.htmlEncode(req.getParameter("newDescription"));
            String currentDesc = workflowItem.getDescription();
            if (!newDescription.equals(currentDesc)) {
                if (attrsModified.length() > 0) {
                    attrsModified.append(", ");
                }
                attrsModified.append("description");

                workflowItem.setDescription(newDescription);
            }

            // Update shareability.
            String newGlobalFlag = req.getParameter("newGlobalFlag");
            Boolean globalFlag = false;
            if (newGlobalFlag != null) {
                globalFlag = newGlobalFlag.equalsIgnoreCase("true") ? true : false;
            }
            Boolean currentGlobalFlag = workflowItem.getGlobalFlag();
            if (globalFlag != currentGlobalFlag) {
                if (attrsModified.length() > 0) {
                    attrsModified.append(" and ");
                }
                attrsModified.append("shareability");

                workflowItem.setGlobalFlag(globalFlag);
            }

            // Update tags
            String [] newTags = req.getParameterValues("tags");
            if (newTags == null) {
                newTags = new String[0];
            }
            List<String> newTagsAr = new ArrayList<String>();
            List<String> currTags = WorkflowPropertiesHelper.getWorkflowTags(workflowItem);

            // Add new Tags
            for (String newTag : newTags) {
                newTagsAr.add(newTag);
                if (!currTags.contains(newTag)) {
                    if (!WorkflowPropertiesHelper.addTag(workflowItem, newTag)) {
                        logger.error("Could not add tag '" + newTag + "' for Workflow (" + workflowId + ")");
                    }
                }
            }
            // Remove tags
            for (String oldTag : currTags) {
                if (!newTagsAr.contains(oldTag)) {
                    WorkflowPropertiesHelper.removeTag(workflowItem, oldTag);
                }
            }

            workflowDao.saveOrUpdate(workflowItem);


            String message;
            String messageLevel;

            // If name changed, ensure we don't have a duplicate.
            WorkflowItem dupItem = null;
            if (!newWfName.equals(currentWfName)) {
                dupItem = workflowDao.findByName(newWfName);
            }
            if (dupItem == null || dupItem.getId().equals(workflowItem.getId())) {
                Date now = new Date();
                workflowItem.setWorkflowName(newWfName);
                workflowItem.setWorkflowXml(workflowXml);
                workflowItem.setLastUpdated(now);
                workflowDao.saveOrUpdate(workflowItem);

                message = "Successfully updated the workflow " + attrsModified;
                messageLevel = "SUCCESS";

            } else {
                message = "A workflow by that name already exists.";
                messageLevel = "ERROR";
            }

            // Hmm... noticed that 'editShareability' makes this call but
            // 'editDescription' and 'renameWorkflow' do not.
            workflowHelper.saveWorkflowToDatabase(workflowItem, getBaseDir());

            try {
                writeJSON(
                        resp,
                        json("workflowId", workflowId,
                             "workflowName", newWfName,
                             "description", newDescription,
                             "isGlobal", globalFlag,
                             "message", message, "level",
                             messageLevel));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error updating workflow meta-data.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "IOException occurred while writing workflow meta-data change information to response.");
            }
        }
    }



    /**
     * Workflow folder functionality.
     */

    /**
     * Create a new folder.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param loggedInUserItem the logged in user
     */
    private void doPostCreateFolder(HttpServletRequest req, HttpServletResponse resp,
            UserItem loggedInUserItem) {

        String message = null;

        Boolean success = false;
        Long folderId = null;
        String folderName = req.getParameter("folderName");

        WorkflowFolderDao wffDao = DaoFactory.DEFAULT.getWorkflowFolderDao();
        List<WorkflowFolderItem> wffItems = wffDao.findByUserAndName(loggedInUserItem, folderName);
        if (wffItems != null && !wffItems.isEmpty()) {
            message = "A folder named \"" + folderName + "\" already exists.";
        } else {
            WorkflowFolderItem wffItem = new WorkflowFolderItem();

            WorkflowFolderMapDao wffMapDao = DaoFactory.DEFAULT.getWorkflowFolderMapDao();
            wffItem = new WorkflowFolderItem();
            wffItem.setLastUpdated(new Date());
            wffItem.setOwner(loggedInUserItem);
            wffItem.setWorkflowFolderName(folderName);
            wffDao.saveOrUpdate(wffItem);

            folderId = wffItem.getId();
            message = "Created new folder: " + folderName;
            success = true;

        }

        try {
            writeJSON(
                    resp,
                    json("folderId", folderId, "folderName", folderName,
                            "message", message, "success", success));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error renaming workflow.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "IOException occurred while writing workflow rename information to response.");
        }
    }

    /**
     * Move the workflow item.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowItem the workflow item
     * @param loggedInUserItem the logged in user
     */
    private void doPostMoveWorkflowToFolder(HttpServletRequest req, HttpServletResponse resp, WorkflowItem workflowItem,
            UserItem loggedInUserItem) {

        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())
                || loggedInUserItem.getAdminFlag())) {
            String message = null;
            Boolean removeFromFolder = false;
            Long workflowId = workflowItem.getId();
            String folderIdStr = req.getParameter("folderId");
            Long folderId = null;
            if (folderIdStr != null && !folderIdStr.isEmpty()) {
                try {
                    folderId = Long.parseLong(folderIdStr);
                } catch (NumberFormatException nfe) {
                    message = "Folder id is invalid!";
                }
            } else if (folderIdStr != null && folderIdStr.isEmpty()) {
                removeFromFolder = true;
            }

            String folderName = req.getParameter("newFolderName");
            Boolean success = false;

            WorkflowFolderDao wffDao = DaoFactory.DEFAULT.getWorkflowFolderDao();

            WorkflowFolderMapDao wffMapDao = DaoFactory.DEFAULT.getWorkflowFolderMapDao();

            if (!removeFromFolder && folderId != 0) {
                WorkflowFolderItem wffItem = wffDao.get(folderId);
                if (wffItem != null && (wffItem.getOwner().getId().equals(workflowItem.getOwner().getId())
                        || loggedInUserItem.getAdminFlag())) {

                    folderName = wffItem.getWorkflowFolderName();

                    // mck dup 9
                    String oldFolderName = "No Folder";
                    // Remove the workflow from its existing folder (if one exists)
                    List<WorkflowFolderItem> wffList = wffDao.findByWorkflow(workflowItem);
                    for (WorkflowFolderItem oldWffItem : wffList) {
                        oldFolderName = oldWffItem.getWorkflowFolderName();
                        WorkflowFolderMapItem wffMapItem = new WorkflowFolderMapItem();
                        wffMapItem.setWorkflowExternal(workflowItem);
                        wffMapItem.setWorkflowFolderExternal(oldWffItem);
                        wffMapDao.delete(wffMapItem);

                    }

                    WorkflowFolderMapItem wffMapItem = new WorkflowFolderMapItem();
                    wffMapItem.setWorkflowExternal(workflowItem);
                    wffMapItem.setWorkflowFolderExternal(wffItem);
                    wffMapDao.saveOrUpdate(wffMapItem);


                    message = "Moved workflow " + workflowItem.getWorkflowName()
                        + " from \"" + oldFolderName + "\" to \"" + folderName + "\"";
                    success = true;

                } else {
                    message = "Cannot move workflow \"" + workflowItem.getWorkflowName()
                        + "\" to \"" + folderName + "\". The folder is owned by " + wffItem.getOwner().getId();
                }
            } else if (folderId != null && folderId == 0 && folderName != null) {

                String safeFolderName = WorkflowXmlUtils.htmlDelimitQuotes(folderName);
                if (!safeFolderName.isEmpty()) {
                    workflowFileHelper.moveToOrCreateFolder(wffDao, loggedInUserItem, safeFolderName, workflowItem);
                }

            } else {
                // remove workflow from folder
                List<WorkflowFolderItem> wffList = wffDao.findByWorkflow(workflowItem);
                String oldFolderName = "No Folder";
                for (WorkflowFolderItem oldWffItem : wffList) {
                    oldFolderName = oldWffItem.getWorkflowFolderName();

                    WorkflowFolderMapItem wffMapItem = new WorkflowFolderMapItem();
                    wffMapItem.setWorkflowExternal(workflowItem);
                    wffMapItem.setWorkflowFolderExternal(oldWffItem);

                    wffMapDao.delete(wffMapItem);
                }

                message = "Removed workflow " + workflowItem.getWorkflowName()
                + " from \"" + oldFolderName + "\".";
                success = true;

            }

            try {
                writeJSON(
                        resp,
                        json("workflowId", workflowId, "folderId", folderId, "folderName", folderName,
                                "message", message, "success", success));
                return;
            } catch (JSONException exception) {
                writeJsonError(resp, "Error renaming workflow.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "IOException occurred while writing workflow rename information to response.");
            }
        }

    }

    /**
     * Delete a folder, returning its workflows to the root node (no folder).
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param loggedInUserItem the logged in user
     */
    private void doPostDeleteFolder(HttpServletRequest req, HttpServletResponse resp,
            UserItem loggedInUserItem) {

        String message = null;

        Boolean success = false;
        Long folderId = null;
        String folderName = req.getParameter("folderName");
        String folderIdStr = req.getParameter("folderId");
        if (folderIdStr != null) {
            try {
                folderId = Long.parseLong(folderIdStr);
            } catch (NumberFormatException nfe) {
                logger.error("Folder ID not valid: " + folderIdStr);
            }
        }

        WorkflowFolderDao wffDao = DaoFactory.DEFAULT.getWorkflowFolderDao();
        WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
        WorkflowFolderItem wffItem = wffDao.get(folderId);
        if (wffItem != null && loggedInUserItem != null
            && (loggedInUserItem.getId().equals(wffItem.getOwner().getId())
                || loggedInUserItem.getAdminFlag())) {
            WorkflowFolderMapDao wfmDao = DaoFactory.DEFAULT.getWorkflowFolderMapDao();
            List<WorkflowFolderMapItem> wffmItems = wfmDao.findByWorkflowFolder(wffItem);

            for (WorkflowFolderMapItem wffmItem : wffmItems) {
                WorkflowItem thisWf = wffmItem.getWorkflow();

                // Delete associated workflow Tags
                deleteAllWorkflowTags(thisWf);

                wfDao.delete(thisWf);

            }

            wffDao.delete(wffItem);

            message = "Deleted folder: " + folderName;
            success = true;
        } else if (wffItem != null && loggedInUserItem != null
                && !loggedInUserItem.getId().equals(wffItem.getOwner().getId())) {

            message = "You do not have permission to delete folder: " + folderName;
            success = false;
        }

        try {
            writeJSON(
                    resp,
                    json("folderId", folderId, "folderName", folderName,
                            "message", message, "success", success));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error renaming workflow.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "IOException occurred while writing workflow rename information to response.");
        }
    }

    /**
     * Rename a folder.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param loggedInUserItem the logged in user
     */
    private void doPostRenameFolder(HttpServletRequest req, HttpServletResponse resp,
            UserItem loggedInUserItem) {

        String message = null;
        String oldFolderName = null;
        String safeFolderName = null;
        Boolean success = false;
        Long folderId = null;
        String folderName = req.getParameter("folderName");
        String folderIdStr = req.getParameter("folderId");
        if (folderIdStr != null) {
            try {
                folderId = Long.parseLong(folderIdStr);
            } catch (NumberFormatException nfe) {
                logger.error("Folder ID not valid: " + folderIdStr);
            }
        }


        if (folderId != null && folderName != null) {
            WorkflowFolderDao wffDao = DaoFactory.DEFAULT.getWorkflowFolderDao();
            WorkflowFolderItem wffItem = wffDao.get(folderId);
            UserItem folderOwner = loggedInUserItem;
            if (wffItem != null
                && !wffItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                folderOwner = wffItem.getOwner();
            }

            safeFolderName = WorkflowXmlUtils.htmlDelimitQuotes(folderName);
            if (!safeFolderName.isEmpty()) {

                List<WorkflowFolderItem> existingFolder =
                    wffDao.findByUserAndName(folderOwner, safeFolderName);

                if (existingFolder == null || existingFolder.isEmpty()) {

                    if (wffItem != null && loggedInUserItem != null
                        && (loggedInUserItem.getId().equals(wffItem.getOwner().getId())
                            || loggedInUserItem.getAdminFlag())) {
                        oldFolderName = wffItem.getWorkflowFolderName();
                        wffItem.setWorkflowFolderName(safeFolderName);
                        wffDao.saveOrUpdate(wffItem);

                        message = "Renamed folder (" + oldFolderName + ") to " + safeFolderName;
                        success = true;
                    } else if (wffItem != null && loggedInUserItem != null
                            && !loggedInUserItem.getId().equals(wffItem.getOwner().getId())) {

                        message = "You do not have permission to rename folder: " + safeFolderName;
                        success = false;
                    }
                } else {
                    message = "Folder name (" + safeFolderName + ") already exists.";
                    success = false;
                }
            } else {
                message = "Folder name (" + safeFolderName + ") is invalid.";
                success = false;
            }
        } else {
            message = "Folder name (" + safeFolderName + ") or id (" + folderId + ") missing.";
            success = false;
        }

        try {
            writeJSON(
                    resp,
                    json("folderId", folderId, "folderName", safeFolderName,
                            "message", message, "success", success));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error renaming workflow.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "IOException occurred while writing workflow rename information to response.");
        }
    }


    /**
     * Remove search filter key.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param workflowContext the workflow context
     * @param loggedInUserItem the logged in user
     */
    private void doPostRemoveSearchFilter(HttpServletRequest req, HttpServletResponse resp,
            UserItem loggedInUserItem, WorkflowContext workflowContext) {

        String message = "";

        Boolean success = false;

        String filterName = req.getParameter("filterName");
        String val = workflowContext.getSearchAttribute(filterName);
        if (val != null) {
            workflowContext.removeSearchAttribute(filterName);
            logger.info("Remove workflows search key (" + filterName + "), val (" + val + ")");
            success = true;
        }


        try {
            writeJSON(
                    resp,
                    json("filter", filterName,
                            "message", message, "success", success));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error renaming workflow.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "IOException occurred while writing workflow rename information to response.");
        }
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

} // end of LearnSphereServlet class
