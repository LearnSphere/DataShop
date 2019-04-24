/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * This servlet handles the management and requests for workflows.
 *
 * @author Mike Komisin
 * @version $Revision: 15837 $ <BR>
 * Last modified by: $Author: mkomisin $ <BR>
 * Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $ <!-- $KeyWordsOff: $ -->
 */
public class WorkflowResultsServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for editing workflows as a standalone tool. */
    private static final String DETACHED_EDIT_JSP_NAME = "/jsp_workflows/learnsphere-edit.jsp";
    /** The JSP name for editing workflows as a standalone tool. */
    private static final String DETACHED_LIST_JSP_NAME = "/jsp_workflows/learnsphere-list.jsp";
    /** The JSP for logging in to WorkflowEditor. */
    private static final String LOGIN_JSP_NAME = "/jsp_workflows/ls_login.jsp";

    /** File preview max lines. */
    private static final Integer FILE_PREVIEW_MAX_LINES = 300;
    /** How many lines to review for guessing the delimiter in a text file. */
    private static final Integer DELIMITER_GUESS_MAX_LINES = 10;


    /** The workflow helpers. */
    private WorkflowHelper workflowHelper;

    private ComponentHelper componentHelper;
    /** The workflow DAO. */
    private WorkflowDao workflowDao;
    /** The workflow component instance DAO. */
    private WorkflowComponentInstanceDao wciDao;
    /** The WFC CommonSchemas directory. */
    private static String commonSchemasDir;
    /** The WFC CommonResources directory. */
    private static String commonResourcesDir;

    /** Label used for setting session attribute "recent_report". */
    public static final String SERVLET_NAME = "WorkflowResults";

    /** Format for the date range method, getDateRangeString. */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMM d, yyyy HH:mm:ss");


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

        commonSchemasDir = WorkflowFileUtils.getStrictDirFormat(WorkflowHelper.getWorkflowComponentsDir())
                + "CommonSchemas/";

        commonResourcesDir = WorkflowFileUtils.getStrictDirFormat(WorkflowHelper.getWorkflowComponentsDir())
                + "CommonResources/";

        // Get the logged in user or null if not logged in.
        UserItem loggedInUserItem = getLoggedInUserItem(req);
        // Forward to the workflows_detached JSP (view) unless the disp is overridden by another method.

        if (loggedInUserItem != null) {

            // Helper
            workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
            componentHelper = HelperFactory.DEFAULT.getComponentHelper();
            // DAOs
            workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();

            // Workflow and workflow context
            WorkflowItem workflowItem = null;
            WorkflowContext workflowContext = null;
            // Authorization conditions
            Boolean isAdmin = false;
            Boolean isOwner = false;
            Boolean isShared = false;
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

                }
            }

            // Get the requesting method.
             String requestingMethod = req.getParameter("requestingMethod");

            /**
             *  Workflow Servlet Requests.
             */

            if (workflowItem != null && (isAdmin || isOwner || isShared)) {
            // If the permissions criteria are adequate for the workflow, continue.

                // Location of the CommonSchemas folder.

                if (requestingMethod != null
                        && requestingMethod.equals("WorkflowResultsServlet.displayPreviousResults")) {
                    // Get previous results.
                    doPostDisplayPreviousResults(req, resp, workflowItem, loggedInUserItem);
                    return;
                } else if (requestingMethod != null
                        && requestingMethod.equals("WorkflowResultsServlet.filePreview")) {
                    // Get text file preview.
                    doPostFilePreview(req, resp, loggedInUserItem);
                    return;
                }

            } else if (workflowItem == null || (!isOwner && isShared)) {

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



    private void doPostFilePreview(HttpServletRequest req, HttpServletResponse resp,
            UserItem loggedInUserItem) {
        // Preview table in spreadsheet
        if (loggedInUserItem != null) {
            try {
                WorkflowItem actualWorkflowItem = null;
                String workflowId = null;
                String errMessage = null;
                String fileId = req.getParameter("fileId");
                String componentId = WorkflowIfaceHelper.getComponentId(req);
                // Node id can be null (for file preview in new window)
                String nodeIdStr = req.getParameter("nodeId");
                Integer nodeId = null;
                if (nodeIdStr != null && nodeIdStr.matches("\\d+")) {
                    nodeId = Integer.parseInt(nodeIdStr);
                }

                String isCompactPreviewStr = req.getParameter("isCompactPreview");
                Boolean isCompactPreview = false;
                if (isCompactPreviewStr != null && isCompactPreviewStr.equalsIgnoreCase("true")) {
                    isCompactPreview = Boolean.parseBoolean(isCompactPreviewStr);
                }

                if (fileId != null) {
                    if (fileId.matches("\\d+")) {
                        WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                        WorkflowFileItem fileItem = wfFileDao.get(Integer.parseInt(fileId));
                        if (fileItem != null) {
                            WorkflowDao wfDao = DaoFactory.DEFAULT.getWorkflowDao();
                            ComponentFilePersistenceDao cfpDao = DaoFactory.DEFAULT.getComponentFilePersistenceDao();
                            List<ComponentFilePersistenceItem> cfpItems = cfpDao.findByFile(fileItem);
                            if (cfpItems != null && !cfpItems.isEmpty()) {
                                ComponentFilePersistenceItem cfpItem = cfpDao.get((Long)cfpItems.get(0).getId());
                                if (cfpItem.getWorkflow() != null) {
                                    actualWorkflowItem = wfDao.get(cfpItem.getWorkflow().getId());
                                    workflowId = actualWorkflowItem.getId().toString();
                                }
                            }

                            // Update wfc adjacency table.
                            ConnectionHelper.updateAdjacencyList(getBaseDir(), actualWorkflowItem, loggedInUserItem);

                            String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                    loggedInUserItem, getBaseDir(), actualWorkflowItem, componentId);
                            if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {



                                String fileType = fileItem.getTitle();
                                String filePath = getBaseDir() + "/" + fileItem.getFilePath();
                                String fileName = fileItem.getFileName();
                                Boolean tabDelimited = ComponentHierarchyHelper.isDescendant(commonSchemasDir
                                        + "/TableTypes.xsd", fileType, "tab-delimited");
                                Boolean commaSeparated = false;
                                if (!tabDelimited) {
                                    commaSeparated = ComponentHierarchyHelper.isDescendant(commonSchemasDir
                                            + "/TableTypes.xsd", fileType, "csv");
                                }
                                // Order matters in this logic block because of hierarchies, i.e. tab-delimited is a
                                // type of text file..

                                // Also, we're passing a flag (isCompactPreview) through to the servlet,
                                // and it's not getting used yet, but it will for sure get used.

                                if (fileItem.getFileType() != null
                                        && fileItem.getFileType().matches("text/.*")) {
                                    if (tabDelimited) {
                                        tablePreview(resp, componentId, fileId, filePath, fileName, nodeId, isCompactPreview, "\t");
                                        return;
                                    } else if (commaSeparated) {
                                        tablePreview(resp, componentId, fileId, filePath, fileName, nodeId, isCompactPreview, ",");
                                        return;
                                    } else if (fileType.equalsIgnoreCase("inline-html")) {
                                        htmlPreview(resp, componentId, fileId, filePath, fileName, nodeId, isCompactPreview);
                                        return;
                                    } else if (ComponentHierarchyHelper.isDescendant(commonSchemasDir
                                            + "/TableTypes.xsd", fileType, "text")
                                            && !fileType.equalsIgnoreCase("html")) {
                                        textPreview(resp, componentId, fileId, filePath, fileName, nodeId, isCompactPreview);
                                        return;
                                    } else {
                                        // Not a known type, but we do know it's text
                                        textPreview(resp, componentId, fileId, filePath, fileName, nodeId, isCompactPreview);
                                        return;
                                    }
                                } else if (fileType.equalsIgnoreCase("pdf")) {
                                    htmlPreview(resp, componentId, fileId, filePath, fileName, nodeId, isCompactPreview);
                                    return;
                                } else if (fileItem.getFileType() != null
                                        && fileItem.getFileType().matches("image/.*")) {
                                    // The import type is an image file show download link
                                    showImage(resp, componentId, fileId, filePath, fileName,
                                            nodeId, isCompactPreview, (Integer) fileItem.getId());
                                    return;
                                } else {
                                    // Cannot preview, show download link
                                    showDownloadLink(resp, componentId, fileId, filePath, fileName,
                                            nodeId, isCompactPreview, (Integer) fileItem.getId());
                                    return;
                                }

                            }
                        } else {
                            errMessage = "File not found.";
                        }

                    } else {
                        errMessage = "Request is not valid.";
                    }

                    try {
                        writeJSON(
                                resp,
                                json("workflowId", workflowId,
                                        "error_flag", "true",
                                        "isCompactPreview", isCompactPreview,
                                        "message", errMessage));
                        return;
                    } catch (JSONException exception) {
                        writeJsonError(resp, "Error retrieving preview.");
                        return;
                    }
                }
            } catch (IOException e) {
                writeJsonError(resp, "Could not write to response.");
                return;
            }
        }

    }

    /**
         * Display the last known result of the workflow, and add it to the request.
         *
         * @param req the HttpServletRequest
         * @param resp the HttpServletResponse
         * @param workflowItem the workflow item
         * @param loggedInUserItem the logged in user
         */
        private void doPostDisplayPreviousResults(HttpServletRequest req, HttpServletResponse resp,
                WorkflowItem workflowItem, UserItem loggedInUserItem) {
            // Preview table in spreadsheet
            Boolean editorWindow = false;
            WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
            WorkflowFileHelper workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
            if (workflowItem != null && loggedInUserItem != null) {

                RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_EDIT_JSP_NAME);
                String workflowName = workflowItem.getWorkflowName();

                String componentId = WorkflowIfaceHelper.getComponentId(req);
                String componentType = WorkflowIfaceHelper.getComponentType(req);
                String componentName = WorkflowIfaceHelper.getComponentName(req);

                String componentTypeHierarchyJson = null;

                if (req.getParameter("editorWindow") != null) {
                    editorWindow = true;
                } else {

                    try {
                        componentTypeHierarchyJson = ComponentHelper
                                .getComponentTypeHierarchy(commonResourcesDir + "ComponentTypeHierarchy.xml");

                    } catch (IOException e1) {
                        logger.error("Could not access ComponentTypeHierarchy.xml");
                    }
                }

                String componentOptionsXml = null;
                JSONObject optionsXmlAsJson = new JSONObject();
                JSONObject componentDepths = new JSONObject();


                if (componentId != null && componentType != null && componentName != null) {

                    // Update wfc adjacency table.
                    ConnectionHelper.updateAdjacencyList(getBaseDir(), workflowItem, loggedInUserItem);

                    // Check component access level
                    String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                        loggedInUserItem, getBaseDir(), workflowItem, componentId);
                    if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                            || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                        componentOptionsXml = componentHelper.getAdvancedComponentOptions(workflowItem, getBaseDir(),
                            commonSchemasDir + "TableTypes.xsd", loggedInUserItem, componentId,
                                componentName, WorkflowHelper.getWorkflowComponentsDir() );
                    } // mck3 else if null?
                }


                try {
                    if (componentOptionsXml != null) {
                        optionsXmlAsJson = org.json.XML.toJSONObject(componentOptionsXml.replaceAll("&", "&amp;"));
                    }
                } catch (JSONException e) {
                    logger.error("Component options could not be loaded.");
                }

                List<WorkflowComponentAdjacencyItem> wfcAdjItems =
                    wfcAdjDao.findByWorkflow(workflowItem);
                try {
                    if (wfcAdjItems != null && !wfcAdjItems.isEmpty()) {
                        for (WorkflowComponentAdjacencyItem wfcAdjItem : wfcAdjItems) {
                            wfcAdjItem = wfcAdjDao.get((Long) wfcAdjItem.getId());
                            if (wfcAdjItem != null) {
                                componentDepths.put(wfcAdjItem.getComponentId(), wfcAdjItem.getDepthLevel());
                            }
                        }
                    }
                } catch (JSONException e) {
                    logger.error("Could not create componentDepths JSON object.");
                }

                try {
                    if (workflowItem.getResults() == null) {
                        logger.trace("No workflow results exist.");
                    }

                    Boolean isView = true;
                    if (workflowItem.getOwner() != null && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
                        isView = false;
                    }

                    List<WorkflowComponentInstanceItem> wciItems = wciDao.findByWorkflow(workflowItem);
                    String wfResultsMessageState = ""; // Empty string is equivalent to a succesfully completed component.

                    if (wciItems != null) {

                        for (WorkflowComponentInstanceItem wciItem : wciItems) {

                            if (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_ERROR)) {
                                wfResultsMessageState = WorkflowComponentInstanceItem.WF_STATE_ERROR;
                            } else if (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {
                                wfResultsMessageState = WorkflowComponentInstanceItem.WF_STATE_RUNNING;
                            } else if (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY)) {
                                // wfResultsMessageState = WorkflowComponentInstanceItem.WF_STATE_RUNNING;
                            } else if (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_NEW)) {
                                wfResultsMessageState = WorkflowComponentInstanceItem.WF_STATE_NEW;
                            }
                        }

                    }

                    JSONObject fullOptionConstraintMap = OptionDependencyHelper.getFullOptionConstraintMap();

                    JSONObject componentOptionTypes = ComponentHelper.getAllOptionTypes();
                    req.setAttribute(
                            "wfOpt", componentOptionTypes);
    // mckhere
                    String tempResultsString = workflowFileHelper.filterResultsByAuth(workflowHelper, workflowItem, loggedInUserItem, getBaseDir());
                    String modifiedResults = null;
                    String modifiedResultsString = WorkflowAccessHelper.removePrivateOptionMetaData(
                            tempResultsString, loggedInUserItem, workflowItem.getOwner());
                        if (modifiedResultsString != null) {
                            modifiedResults = modifiedResultsString.replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "");
                        }

                    JSONObject jsonMessage = null;
                    if (modifiedResults != null) {
                        jsonMessage = org.json.XML.toJSONObject(modifiedResults);
                    }

                    req.setAttribute("wfComponentMenuJson", componentTypeHierarchyJson);

                    req.setAttribute(
                        "wfResults",
                        (JSONObject) json("workflowId", workflowItem.getId(), "workflowName", workflowName,
                                "success", "true", "componentId", componentId,
                                "componentOptions", optionsXmlAsJson,
                                "isView", isView,
                                "componentDepths", componentDepths,
                                "componentOptionDependencies", fullOptionConstraintMap,
                                "wfResultsMessageState", wfResultsMessageState, "message",
                                jsonMessage));


                    if (editorWindow) {
                        try {
                            writeJSON(
                                    resp, json("workflowId", workflowItem.getId(), "workflowName", workflowName,
                                            "success", "true", "componentId", componentId,
                                            "componentOptions", optionsXmlAsJson,
                                            "isView", isView,
                                            "wfResultsMessageState", wfResultsMessageState,
                                            "message", jsonMessage));
                            return;
                        } catch (JSONException exception) {
                            writeJsonError(resp, "Error retrieving results data.");
                            return;
                        } catch (IOException e) {
                            writeJsonError(resp, "Error retrieving results data.");
                            return;
                        }
                    } else {

                        if (componentId != null) {
                            workflowHelper.logWorkflowComponentUserAction(loggedInUserItem, workflowItem, null,
                                    componentId, componentName, componentType, null,
                                    null, null, null, WorkflowHelper.LOG_DISPLAY_COMPONENT_RESULTS, "");
                        } else {
                            workflowHelper.logWorkflowUserAction(loggedInUserItem, workflowItem, null,
                                    WorkflowHelper.LOG_DISPLAY_RESULTS, "", null);
                        }
                        disp.forward(req, resp);
                    }

                    return;
                } catch (JSONException exception) {
                    logger.error("Error retrieving previous workflow results.");
                } catch (IOException e) {
                    logger.error("Error retrieving previous workflow results.");
                } catch (ServletException e) {
                    logger.error("Error retrieving previous workflow results.");
                } catch (JDOMException e1) {
                    logger.error("Error retrieving component default options.");
                }
            }
        }

    /**
     * Preview the tab-delimited or CSV table.
     *
     * @param resp the servlet response
     * @param fileId the file id
     * @param filePath the file path
     * @param fileName the file name
     * @param nodeId the output node index
     * @param isCompactPreview flag to determine if the preview is being displayed in the
     *   results window or when 'mousing' over an endpoint
     */
    private void tablePreview(HttpServletResponse resp, String componentId, String fileId, String filePath,
            String fileName, Integer nodeId, Boolean isCompactPreview, String delim) {
        logger.debug("Table preview for component " + componentId + " and file " + fileId);
        String fullFilePath = WorkflowFileUtils.sanitizePath(filePath + "/" + fileName);
        String errMessage = null;
        File previewFile = new File(fullFilePath);

        if (previewFile != null && previewFile.isFile()) {
            logger.trace("File found: " + previewFile.getAbsolutePath());
            BufferedReader br = null;
            String line = "";

            // Read headers
            String[] headers = null;
            JSONObject jsonHeaderData = new JSONObject();
            JSONObject jsonColumnData = new JSONObject();
            // JQX grid has issue with duplicate header names.
            // The newHeaderList makes them unique for display purposes only.
            List<String> newHeaderList = new ArrayList<String>();

            try {

                JSONArray jsonHeaderArray = new JSONArray();
                JSONArray jsonColumnArray = new JSONArray();

                br = new BufferedReader(new FileReader(previewFile));
                try {
                    Map<String, Integer> uniqueHeaderList = new HashMap<String, Integer>();
                    while ((line = br.readLine()) != null) {

                        headers = line.split(delim);

                        for (String header : headers) {
                            if (uniqueHeaderList.containsKey(header)) {
                                Integer count = uniqueHeaderList.get(header);
                                uniqueHeaderList.put(header, count + 1);
                                header = header + "#" + (count + 1);
                            } else {
                                uniqueHeaderList.put(header, 1);
                            }

                            newHeaderList.add(header);

                            JSONObject jsonHeaderObject = new JSONObject();
                            JSONObject jsonColumnObject = new JSONObject();

                            // Add new JSON header array object
                            jsonHeaderObject.put("name", header);
                            jsonHeaderArray.put(jsonHeaderObject);

                            // Estimate the column width in pixels for the
                            // header
                            Integer padding = 12;
                            Font font = new Font("Verdana", Font.PLAIN, 13);
                            FontMetrics metrics = new FontMetrics(font) {
                            };
                            Rectangle2D bounds = metrics.getStringBounds(header, null);
                            Integer initialWidth = (int) (bounds.getWidth()) + padding;
                            if (initialWidth < 40) {
                                initialWidth = 40;
                            }
                            // Add new JSON column array object
                            jsonColumnObject.put("text", escapeHtml(header));
                            jsonColumnObject.put("datafield", header);
                            jsonColumnObject.put("width", initialWidth.toString());
                            jsonColumnArray.put(jsonColumnObject);
                        }

                        break;
                    }

                    jsonHeaderData.put("datafields", jsonHeaderArray);
                    jsonColumnData.put("columns", jsonColumnArray);

                } catch (JSONException e) {
                    errMessage = "Error adding preview headers.";
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        logger.error("Error closing import file.");
                    }
                }

            } catch (FileNotFoundException e) {
                errMessage = "Error accessing import file.";
            } catch (IOException e) {
                errMessage = "Error reading import file.";
            }

            // Read fields
            if (errMessage == null && newHeaderList != null) {
                try {
                    JSONObject jsonData = new JSONObject();
                    JSONArray jsonDataRow = new JSONArray();

                    br = new BufferedReader(new FileReader(previewFile));
                    boolean isHeader = true;
                    Integer lineCount = 0;
                    while ((line = br.readLine()) != null && lineCount <= FILE_PREVIEW_MAX_LINES) {
                        if (!isHeader) {
                            // use comma as separator
                            String[] values = line.split(delim);
                            JSONObject jsonObject = new JSONObject();

                            int i = 0;
                            for (String value : values) {
                                if (i >= newHeaderList.size()) {
                                    errMessage = "The number of fields on line "
                                        + (i + 1) + " do not match the number of columns in file: "
                                        + fileName;
                                    break;
                                }

                                jsonObject.put(newHeaderList.get(i), escapeHtml(value));
                                i++;
                            }

                            jsonDataRow.put(jsonObject);
                        } else {
                            isHeader = false;
                        }
                        lineCount++;
                    }

                    jsonData.put("data", jsonDataRow);
                    if (jsonDataRow.length() > 0) {
                        writeJSON(
                                  resp,
                                  json("success", "true", "fileId", fileId,
                                       "componentId", componentId,
                                       "nodeId", nodeId,
                                       "isCompactPreview", isCompactPreview,
                                       "headerMetadata", jsonHeaderData,
                                       "columnMetadata", jsonColumnData,
                                       "data", jsonData));
                        return;
                    }

                } catch (FileNotFoundException e) {
                    errMessage = "Error accessing import file.";
                } catch (IOException e) {
                    errMessage = "Error reading import file.";
                } catch (JSONException e) {
                    errMessage = "Error adding preview data.";
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                errMessage = "Headers not found."; // headers != null
            }
        } else { // fileItems != null
            errMessage = "File not found.";
        }

        if (errMessage == null) {
            errMessage = "This preview cannot be displayed.";
        }

        try {
            writeJSON(
                    resp, json("success", "false",
                    "message", errMessage,
                    "fileId", fileId,
                    "componentId", componentId,
                    "nodeId", nodeId,
                    "isCompactPreview", isCompactPreview));
        } catch (IOException e) {
            logger.error("Could not handle table preview request.");
        } catch (JSONException e) {
            logger.error("Could not handle table preview JSON.");
        }
        return;
    }

    /**
     * Preview the text file.
     *
     * @param resp the servlet response
     * @param fileId the file id
     * @param filePath the file path
     * @param fileName the file name
     * @param isCompactPreview flag to determine if the preview is being displayed in the
     *   results window or when 'mousing' over an endpoint
     */
    private void textPreview(HttpServletResponse resp, String componentId, String fileId, String filePath,
            String fileName, Integer nodeId, Boolean isCompactPreview) {
        logger.debug("Text preview for component " + componentId + " and file " + fileId);
        String fullFilePath = WorkflowFileUtils.sanitizePath(filePath + "/" + fileName);

        File previewFile = new File(fullFilePath);

        if (previewFile != null && previewFile.isFile()) {

            logger.trace("File found: " + previewFile.getAbsolutePath());
            BufferedReader br = null;
            String line = "";
            JSONObject jsonTextData = new JSONObject();

            try {

                String[] delims = { ",", "\t", " ", ":", ";" };
                HashMap<String, Integer> delimCounts = new HashMap<String, Integer>();

                JSONArray jsonTextArray = new JSONArray();

                br = new BufferedReader(new FileReader(previewFile));
                try {
                    Integer lineCount = 0;

                    while ((line = br.readLine()) != null && lineCount <= FILE_PREVIEW_MAX_LINES) {
                        if (lineCount < DELIMITER_GUESS_MAX_LINES) {
                            for (String d : delims) {
                                Integer splitCount = line.split(d, -1).length;
                                if (delimCounts.containsKey(d) && !delimCounts.get(d).equals(splitCount)) {
                                    delimCounts.put(d, -1);
                                } else if (!delimCounts.containsKey(d)) {
                                    delimCounts.put(d, splitCount);
                                }
                            }
                        }
                        jsonTextArray.put(line);
                        lineCount++;

                    }

                    jsonTextData.put("lines", jsonTextArray);

                } catch (JSONException e) {
                    writeJsonError(resp, "Error adding text preview.");
                    return;
                }

                String guessDelim = null;
                Integer delimCount = null;
                for (String d : delims) {
                    if (delimCount == null) {
                        delimCount = delimCounts.get(d);
                        guessDelim = d;
                    } else if (delimCount < delimCounts.get(d)) {
                        delimCount = delimCounts.get(d);
                        guessDelim = d;
                    }
                }

                if (delimCount > 1) {
                    tablePreview(resp, componentId, fileId, filePath,
                        fileName, nodeId, isCompactPreview, guessDelim);
                    return;
                }

            } catch (FileNotFoundException e) {
                writeJsonError(resp, "Error accessing file.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error reading file.");
                return;
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        logger.error("Error closing file.");
                    }
                }
            }

            try {

                writeJSON(resp,
                        json("success", "true",
                                "fileId", fileId,
                                "componentId", componentId,
                                "nodeId", nodeId,
                                "isCompactPreview", isCompactPreview,
                                "textData", jsonTextData));

                return;
            } catch (JSONException e) {
                writeJsonError(resp, "Error adding preview data.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error writing to response.");
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } // headers != null
        } // fileItems != null
    }

    /**
     * Preview the html file.
     *
     * @param resp the servlet response
     * @param fileId the file id
     * @param filePath the file path
     * @param fileName the file name
     * @param nodeId the output node index
     * @param isCompactPreview flag to determine if the preview is being displayed in the
     *   results window or when 'mousing' over an endpoint
     */
    private void htmlPreview(HttpServletResponse resp, String componentId, String fileId, String filePath,
            String fileName, Integer nodeId, Boolean isCompactPreview) {
        logger.debug("Html preview for component " + componentId + " and file " + fileId);
        String fullFilePath = WorkflowFileUtils.sanitizePath(filePath + "/" + fileName);

        File previewFile = new File(fullFilePath);

        if (previewFile != null && previewFile.isFile()) {

            try {
                writeJSON(resp,
                        json("success", "true",
                                "fileId", fileId,
                                "componentId", componentId,
                                "nodeId", nodeId,
                                "isCompactPreview", isCompactPreview,
                                "htmlFile", fileId));
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error reading import file.");
                return;
            } catch (JSONException e) {
                writeJsonError(resp, "Error adding preview data.");
                return;
            }

        } // fileItems != null
    }

    /**
     * Show the file with a download link.
     * @param resp the response
     * @param componentId the component id
     * @param fileId the workflow_component_file id
     * @param filePath the parent path
     * @param fileName the file name
     * @param nodeId the node id
     * @param isCompactPreview whether or not the preview is compact (in the editor)
     * or not compact (in the results page)
     * @param downloadId
     */
    private void showDownloadLink(HttpServletResponse resp, String componentId, String fileId, String filePath,
            String fileName, Integer nodeId, Boolean isCompactPreview, Integer downloadId) {
        logger.debug("Show download link for component " + componentId + " and file " + fileId);
        String fullFilePath = WorkflowFileUtils.sanitizePath(filePath + "/" + fileName);

        File previewFile = new File(fullFilePath);

        if (previewFile != null && previewFile.isFile()) {

            logger.trace("File found: " + previewFile.getAbsolutePath());

            Long bytes = previewFile.length();

            String downloadUrl = "Download <a href=\"LearnSphere?downloadId="
                + downloadId + "\">" + previewFile.getName() + "</a>";

            try {

                writeJSON(resp,
                        json("success", "true",
                                "fileId", fileId,
                                "componentId", componentId,
                                "nodeId", nodeId,
                                "isCompactPreview", isCompactPreview,
                                "bytes", bytes,
                                "downloadUrl", downloadUrl));

                return;
            } catch (JSONException e) {
                writeJsonError(resp, "Error adding preview data.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error writing to response.");
            }
        } // fileItems != null
    }

    /**
     * Show the image file.
     * @param resp the response
     * @param componentId the component id
     * @param fileId the workflow_component_file id
     * @param filePath the parent path
     * @param fileName the file name
     * @param nodeId the node id
     * @param isCompactPreview whether or not the preview is compact (in the editor)
     * or not compact (in the results page)
     * @param downloadId
     */
    private void showImage(HttpServletResponse resp, String componentId, String fileId, String filePath,
            String fileName, Integer nodeId, Boolean isCompactPreview, Integer downloadId) {
        logger.debug("Show download link for component " + componentId + " and file " + fileId);
        String fullFilePath = WorkflowFileUtils.sanitizePath(filePath + "/" + fileName);

        File previewFile = new File(fullFilePath);

        if (previewFile != null && previewFile.isFile()) {

            logger.trace("File found: " + previewFile.getAbsolutePath());

            Long bytes = previewFile.length();

            String downloadUrl = "<a target=\"_blank\" href=\"LearnSphere?visualizationId="
                + downloadId + "\">Open in new window</a><br/>"
                + "<img src=\"LearnSphere?visualizationId="
                + downloadId + "\"><br/>" + previewFile.getName() + "";

            try {

                writeJSON(resp,
                        json("success", "true",
                                "fileId", fileId,
                                "componentId", componentId,
                                "nodeId", nodeId,
                                "isImage", true,
                                "isCompactPreview", isCompactPreview,
                                "bytes", bytes,
                                "downloadUrl", downloadUrl));

                return;
            } catch (JSONException e) {
                writeJsonError(resp, "Error adding preview data.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error writing to response.");
            }
        } // fileItems != null
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

