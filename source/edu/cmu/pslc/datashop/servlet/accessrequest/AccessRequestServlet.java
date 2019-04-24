/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.accessrequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.BreakIterator;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;
import edu.cmu.pslc.datashop.dto.ProjectRequestDTO.SortParameter;
import edu.cmu.pslc.datashop.dto.UserRequestDTO;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.FileUtils;

/**
 * This servlet is responsible for managing the access request feature.
 *
 * @author Mike Komisin
 * @version $Revision: 14097 $ <BR>
 * Last modified by: $Author: ctipper $ <BR>
 * Last modified on: $Date: 2017-06-05 15:51:25 -0400 (Mon, 05 Jun 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessRequestServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Max length of input characters for dialog reason. */
    private static final int MAX_CHAR_LEN = 255;
    /** Access Request sub-tab enumerated field value - "Not Reviewed". */
    public static final String ACCESS_REQUEST_SUBTAB_NOT_REVIEWED = "Not Reviewed";
    /** Access Request sub-tab enumerated field value - "Recent Activity". */
    public static final String ACCESS_REQUEST_SUBTAB_RECENT = "Recent Activity";
    /** Access Request sub-tab enumerated field value - "Access Report". */
    public static final String ACCESS_REQUEST_SUBTAB_REPORT = "Access Report";

    /** User Request DTO attribute handle. */
    public static final String USER_REQUEST_DTO_ATTRIB = "userRequestDTO";
    /** Project Request DTO attribute handle. */
    public static final String PROJECT_REQUEST_DTO_ATTRIB = "projectRequestDTO";
    /** Access Report count attribute handle. */
    public static final String ACCESS_REPORT_DTO_ATTRIB = "accessReportDto";
    /** Access Request sub-tab attribute handle. */
    public static final String ACCESS_REQUEST_SUBTAB_ATTRIB = "accessRequestSubtab";
    /** Access Request context attribute handle. */
    public static final String ACCESS_REQUEST_CONTEXT_ATTRIB = "accessRequestContext";
    /** Access Request export report attribute handle. */
    public static final String ACCESS_REQUEST_EXPORT_REPORT = "accessRequestExportReport";
    /** Access Request export CP report attribute handle. */
    public static final String ACCESS_REQUEST_EXPORT_CP_REPORT = "accessRequestExportCpReport";
    /** The Sort-by context attribute handle. */
    public static final String SORT_BY = "sortBy";

    /** This prefix attached to the project Id for the request access button
     * element Id. */
    public static final String REQUEST_ID_PREFIX = "requestId_";
    /** This prefix attached to the project Id for the response access button
     * element Id. */
    public static final String RESPONSE_ID_PREFIX = "responseId_";
    /** This prefix attached to the project Id for the make project public button
     * element Id. */
    public static final String PROJECT_ID_PREFIX = "projectId_";

    /** Access Request Ajax request method parameter */
    private static final String AJAX_REQUEST_METHOD = "requestingMethod";

    /** The JSP name for managing all access requests. */
    private static final String MANAGE_JSP_NAME = "/access_requests.jsp";

    /** Default page when viewing access report. */
    public static final int DEFAULT_PAGE = 1;

    /** Title for the Access Request page - "Access Requests". */
    public static final String SERVLET_LABEL = "Access Requests";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "AccessRequests";

    /** Search constant for UserItem.DEFAULT_USER which is displayed as "Public" */
    private static final String PUBLIC = "public";

    /** Maximum number of characters accepted by the servlet. */
    private static final int MAX_CHARS_REASON = 255;

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        /** The Hibernate Session wrapped helper. */
        AccessRequestHelper arHelper;
        /** The Access Request context. */
        AccessRequestContext arContext;

        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);
            UserItem userItem = getUser(req);

            // Get Access Request Helper and Context
            arHelper = HelperFactory.DEFAULT.getAccessRequestHelper();
            arContext = (AccessRequestContext) req.getSession().getAttribute(
                    ACCESS_REQUEST_CONTEXT_ATTRIB);
            if (arContext == null) {
                arContext = new AccessRequestContext();
                req.getSession().setAttribute(ACCESS_REQUEST_CONTEXT_ATTRIB,
                        arContext);
            }

            // Tell the jsp to highlight this tab
            req.getSession().setAttribute("datasets", SERVLET_LABEL);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // Get and store the activity notification counts in the session
            Map<String, Long> activityNotifications = arHelper.getActivityCountsMap(userItem);

            req.getSession().setAttribute(AccessRequestStatusItem.STATE_COUNT_MY_REQUESTS,
                activityNotifications.get(AccessRequestStatusItem.STATE_COUNT_MY_REQUESTS));

            req.getSession().setAttribute(AccessRequestStatusItem.STATE_COUNT_NOT_REVIEWED,
                activityNotifications.get(AccessRequestStatusItem.STATE_COUNT_NOT_REVIEWED));

            req.getSession().setAttribute(AccessRequestStatusItem.STATE_COUNT_RECENT_ACTIVITY,
                activityNotifications.get(AccessRequestStatusItem.STATE_COUNT_RECENT_ACTIVITY));

            req.getSession().setAttribute(AccessRequestStatusItem.STATE_COUNT_TOTAL,
                activityNotifications.get(AccessRequestStatusItem.STATE_COUNT_TOTAL));

            // Find out if this user was ever the PI or DP for any projects as best we can
            Boolean wasEverOwner = false;
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            List<ProjectItem> myProjects = (List<ProjectItem>) projectDao.findByOwner(userItem);
            if (myProjects.size() > 0) {
                wasEverOwner = true;
            }
            // If they're not currently the PI or DP, maybe they were at one time, check the history
            AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
            List<AccessRequestHistoryItem> arHistoryItems =
                    (List<AccessRequestHistoryItem>) arHistoryDao.findByUser(userItem);
            if (!wasEverOwner && arHistoryItems.size() > 0) {
                for (AccessRequestHistoryItem arHistoryItem : arHistoryItems) {
                    if (arHistoryItem.getRole().equals(
                            AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP)
                        || arHistoryItem.getRole().equals(
                            AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                        wasEverOwner = true;
                        break;
                    }
                }
            }
            arContext.setWasEverOwner(wasEverOwner);

            // Handle all HTTP POST/GET requests that aren't AJAX requests
            if (req.getParameter(AJAX_REQUEST_METHOD) == null) {

                // Export the access report
                if (req.getParameter(ACCESS_REQUEST_EXPORT_REPORT) != null) {
                    sendExportFile(userItem, arHelper, arContext, resp, true);
                    return;
                }

                // Export the Current Permissions report
                if (req.getParameter(ACCESS_REQUEST_EXPORT_CP_REPORT) != null) {
                    sendExportFile(userItem, arHelper, arContext, resp, false);
                    return;
                }

                // Get user request list
                req.getSession().setAttribute(USER_REQUEST_DTO_ATTRIB,
                        arHelper.getUserRequestList(userItem));
                String currentTabParam =
                        (String) (req.getParameter(ACCESS_REQUEST_SUBTAB_ATTRIB));
                // Sub-tab parameters
                if (currentTabParam != null) {
                    // Re-initialize non-persistent variables
                    arContext.setCurrentTab(currentTabParam);
                    arContext.setCurrentPage(DEFAULT_PAGE);
                    ////arContext.setFilterBy(AccessRequestContext.DEFAULT_FILTER_BY);
                } else if (arContext.getCurrentTab() == null) {
                    arContext.setCurrentTab(ACCESS_REQUEST_SUBTAB_NOT_REVIEWED);
                }
                // Sort-by parameter
                if (req.getParameter(SORT_BY) != null) {
                    String sortByParam = (String) (req.getParameter(SORT_BY));
                    arContext.setSortBy(sortByParam);
                    arContext.toggleSortOrder(sortByParam);
                }
                // Show administrators in Access Report parameter
                if (req.getParameter(AccessRequestContext.ACCESS_REQUEST_SHOW_ADMINS) != null) {
                    String showAdminsParam = (String)
                            req.getParameter(AccessRequestContext.ACCESS_REQUEST_SHOW_ADMINS);
                    if (showAdminsParam.equals("true")) {
                        arContext.setShowAdmins(true);
                    } else {
                        arContext.setShowAdmins(false);
                    }
                }
                // Search-by user or project name parameter
                if (req.getParameter(AccessRequestContext.ACCESS_REQUEST_SEARCH_STRING) != null) {
                    String searchStringParam = (String) (req.getParameter(
                            AccessRequestContext.ACCESS_REQUEST_SEARCH_STRING)).trim();
                    arContext.setSearchString(searchStringParam);
                }
                // Show older user requests parameter
                if (req.getParameter(AccessRequestContext.ACCESS_REQUEST_SHOW_OLDER_USER_REQ)
                        != null) {
                    Boolean showOlderUserRequestsParam = (Boolean) (req.getParameter(
                            AccessRequestContext.ACCESS_REQUEST_SHOW_OLDER_USER_REQ)
                            .equalsIgnoreCase("true"));
                    arContext.setShowOlderUserRequests(showOlderUserRequestsParam);
                }
                // Show older project requests parameter
                if (req.getParameter(AccessRequestContext.ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ)
                        != null) {

                    Boolean showOlderProjectRequestsParam = (Boolean) (req.getParameter(
                            AccessRequestContext.ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ)
                            .equalsIgnoreCase("true"));
                    arContext.setShowOlderProjectRequests(showOlderProjectRequestsParam);
                }
                // Rows Per Page parameter
                if (req.getParameter(AccessRequestContext.ACCESS_REQUEST_ROWS_PER_PAGE) != null) {
                    String numString = (String) (req.getParameter(
                            AccessRequestContext.ACCESS_REQUEST_ROWS_PER_PAGE));
                    Integer rowsPerPageParam = numString.matches("[0-9]+") ? Integer
                            .parseInt(numString)
                            : AccessRequestContext.DEFAULT_ROWS_PER_PAGE;
                    arContext.setRowsPerPage(rowsPerPageParam);
                }
                // Current Page parameter
                if (req.getParameter(AccessRequestContext.ACCESS_REQUEST_CURRENT_PAGE) != null) {
                    String numString = (String) (req.getParameter(
                            AccessRequestContext.ACCESS_REQUEST_CURRENT_PAGE));
                    Integer currentPageParam = numString.matches("[0-9]+") ? Integer
                            .parseInt(numString) : 1;
                    arContext.setCurrentPage(currentPageParam);
                }
                // Filter By parameter
                if (req.getParameter(AccessRequestContext.ACCESS_REQUEST_FILTER_BY) != null) {
                    String filterByParam = (String) (req.getParameter(
                            AccessRequestContext.ACCESS_REQUEST_FILTER_BY));
                    arContext.setFilterBy(filterByParam);
                }
                // Setup the local variables for keeping track of context information
                String currentTab = arContext.getCurrentTab();
                String searchString = arContext.getSearchString();
                String sortBy = arContext.getSortBy();
                String filterBy = arContext.getFilterBy();
                List<ProjectRequestDTO> projectRequestList = null;
                // Populate the project request list; sort parameters chosen by sub-tab
                if (currentTab.equals(ACCESS_REQUEST_SUBTAB_NOT_REVIEWED)) {
                    // Not Reviewed Sub-tab
                    // Set sort-by if specified
                    if (sortBy == null || sortBy.equals("")) {
                        // Default sort-by
                        arContext.setSortBy(ProjectRequestDTO.COLUMN_LASTACTIVITYDATE);
                        arContext.setSortOrder(ProjectRequestDTO.COLUMN_LASTACTIVITYDATE, false);
                    }
                    projectRequestList = arHelper
                            .getProjectRequestListNotReviewed(userItem, filterBy);

                    // Apply the search string filter.
                    projectRequestList = searchStringFilter(projectRequestList, searchString);
                } else if (currentTab.equals(ACCESS_REQUEST_SUBTAB_RECENT)) {
                    // Recent Activity Sub-tab
                    // Set sort-by if specified
                    if (sortBy == null || sortBy.equals("")) {
                        // Default sort-by
                        arContext.setSortBy(ProjectRequestDTO.COLUMN_LASTACTIVITYDATE);
                        arContext.setSortOrder(ProjectRequestDTO.COLUMN_LASTACTIVITYDATE, false);
                    }
                    projectRequestList = arHelper
                            .getProjectRequestListRecent(userItem, filterBy);

                    // Apply the search string filter.
                    projectRequestList = searchStringFilter(projectRequestList, searchString);
                } else if (currentTab.equals(ACCESS_REQUEST_SUBTAB_REPORT)) {
                    // Access Report Sub-tab
                    // Set sort-by if specified
                    if (sortBy == null || sortBy.equals("")) {
                        // Default sort-by
                        arContext.setSortBy(ProjectRequestDTO.COLUMN_PROJECT);
                        arContext.setSortOrder(ProjectRequestDTO.COLUMN_PROJECT, true);
                    }

                    Integer arCount = arHelper.getAccessReportCount(userItem, arContext);
                    int rowsPerPage = arContext.getRowsPerPage();
                    int numPages = (int)Math.ceil((float)arCount / (float)rowsPerPage);

                    // Trac #401: If the 'rowsPerPage' value is larger than the total
                    // number of rows ('arCount'), set the 'currentPage' to 1.
                    if (rowsPerPage > arCount) { arContext.setCurrentPage(DEFAULT_PAGE); }

                    projectRequestList = arHelper.getAccessReport(userItem, arContext);

                    AccessReportDto accessReportDto =
                        new AccessReportDto(projectRequestList, arCount, numPages);
                    accessReportDto.setHasTermsOfUse(arHelper.
                                                     getAccessReportHasTermsOfUse(userItem,
                                                                                  arContext));
                    req.getSession().setAttribute(ACCESS_REPORT_DTO_ATTRIB, accessReportDto);
                }
                // Store the project request list in the session
                req.getSession().setAttribute(PROJECT_REQUEST_DTO_ATTRIB, projectRequestList);

                // Handle Ajax requests
            } else if (req.getParameter(AJAX_REQUEST_METHOD) != null) {
                // This method deals with AJAX requests and parameters
                ajaxRequests(arHelper,
                    arContext.getCurrentTab(),
                    req, resp);
                return;
            }
            // Log when the access request page is viewed
            UserLogger.log(null, userItem,
                    UserLogger.VIEW_MANAGE_ACCESS_REQUESTS, "", false);
            logger.info("Going to JSP: " + MANAGE_JSP_NAME);
            RequestDispatcher disp;
            disp = getServletContext().getRequestDispatcher(MANAGE_JSP_NAME);
            disp.forward(req, resp);

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logDebug("doPost end");
        }
    }

    /**
     * Handles the HTTP POST Ajax Requests.
     * @param arHelper the AccessRequestHelper object
     * @param currentTab the current tab
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     */
    public void ajaxRequests(AccessRequestHelper arHelper, String currentTab,
            HttpServletRequest req, HttpServletResponse resp) {
        // Get logged in user from request
        UserItem userItem = getLoggedInUserItem(req);

        if (req.getParameter(AJAX_REQUEST_METHOD).equals(
                        "createProjectPublic")) {

            if (req.getParameter("makePublicButtonId") != null) {
                // Create the user request form
                createProjectPublic(req, resp);
            } else {
                jsonError(resp,
                       "Make Project Public unsuccessful."
                            + " Please refresh the page and try again.");
            }
            return;

        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals(
                        "submitProjectPublic")) {

            // Check the required parameters exist
            if (req.getParameter("arProjectId") != null) {
                // Submit the user's request
                submitProjectPublic(arHelper, currentTab, req, resp);
            } else {
                jsonError(resp,
                        "Make Project Public unsuccessful."
                                        + " Please refresh the page and try again.");
            }
            return;
        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals(
                        "createRequest")) {
            if (req.getParameter("requestButtonId") != null) {
                // Create the user request form
                createRequest(arHelper, req, resp);
            } else {
                jsonError(resp,
                        "Access Request unsuccessful."
                                        + " Please refresh the page and try again.");
            }
            return;

        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals(
                "createMultiRequest")) {
                if (req.getParameter("wfAccessRequestProjectList") != null) {
                    // Create the user request form
                    createMultiRequest(arHelper, req, resp);
                } else {
                    jsonError(resp,
                            "Access Request unsuccessful."
                                            + " Please refresh the page and try again.");
                }
                return;
        } else if (req.getParameter("requestingMethod") != null
                && req.getParameter("requestingMethod").equalsIgnoreCase("requestMultipleProjectsAccess")) {
            String projectIdListString = req.getParameter("projectIds");
            if (projectIdListString != null) {
                logger.info("Requesting access to projects: " + projectIdListString);
                submitRequestWf(arHelper, req, resp);
            }
        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals(
                        "submitRequest")) {
            // Check the required parameters exist
            if (req.getParameter("arProjectId") != null
                    && req.getParameter("arLevel") != null) {
                // Submit the user's request
                submitRequest(arHelper, req, resp);
            } else {
                jsonError(resp,
                        "Access Request unsuccessful."
                                        + " Please refresh the page and try again.");
            }
            return;
        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals(
                        "createResponse")) {

            if (req.getParameter("rowId") != null
                    && req.getParameter("projectId") != null
                    && req.getParameter("requestUserId") != null) {
                // Create the user response form
                createResponse(arHelper, req, resp);

            } else {
                jsonError(resp, "Access Response unsuccessful."
                                + " Please refresh the page and try again.");
            }
            return;

        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals("submitResponse")
                || req.getParameter(AJAX_REQUEST_METHOD).equals("voteProjectPublic")
                || req.getParameter(AJAX_REQUEST_METHOD).equals("newAccessRow")
                || req.getParameter(AJAX_REQUEST_METHOD).equals("respond")) {

            if (req.getParameter("arProjectId") != null
                    && req.getParameter("arLevel") != null
                    && req.getParameter("arRequestor") != null
                    && req.getParameter("rowId") != null) {

                // For new access rows, confirm user exists.
                if (req.getParameter(AJAX_REQUEST_METHOD).equals("newAccessRow")) {
                    if (!confirmRequestorExists(req)) {
                        jsonError(resp, "Specified user does not exist.");
                        return;
                    }
                }

                // Do not allow Public user (%) to ever get project admin access
                if (req.getParameter("arLevel") != null
                        && req.getParameter("arRequestor") != null) {
                    String requestorId = ((String) (req.getParameter("arRequestor"))).trim();
                    String level = ((String) req.getParameter("arLevel")).trim();
                    if (requestorId.equals(UserItem.DEFAULT_USER)
                        && req.getParameter("arLevel").equals(AuthorizationItem.LEVEL_ADMIN)) {
                        jsonError(resp, "User '" + requestorId + "' cannot be given '" + level
                                + "' access to a project.");
                        return;
                    }
                }

                // Submit the response
                submitResponse(arHelper, currentTab, req, resp);
            } else {
                jsonError(resp,
                        "Access Response unsuccessful."
                                        + " Please refresh the page and try again.");
            }
            return;
        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals("denyProjectPublic")) {
            if (req.getParameter("arProjectId") != null
                    && req.getParameter("arLevel") != null
                    && req.getParameter("arRequestor") != null
                    && req.getParameter("rowId") != null) {
                // Submit the response
                denyProjectPublic(arHelper, currentTab, req, resp);
            } else {
                jsonError(resp,
                        "Access Response unsuccessful."
                                        + " Please refresh the page and try again.");
            }
            return;
        } else if (req.getParameter(AJAX_REQUEST_METHOD).equals(
                        "createNewAccessRow")) {

            if (userItem.getAdminFlag()) {
                // Create the administrator access row form
                createNewAccessRow(req, resp);

            } else {
                jsonError(resp,
                        "Access Response unsuccessful."
                                        + " Please refresh the page and try again.");
            }
            return;

        }
    }

    /**
     * Writes out the json error.
     * @param resp the HttpServletResponse
     * @param cause the error message
     */
    private void jsonError(HttpServletResponse resp, String cause) {
        try {
            writeJSON(
                    resp,
                    json("msg", "error",
                            "cause", cause));
        } catch (IOException e) {
            logger.error("jsonError:: IOException while trying to write JSON error message.", e);
        } catch (JSONException e) {
            logger.error("jsonError:: JSONException while trying to write JSON error message.", e);
        }
    }

    /**
     * Confirm that the 'arRequestor' user exists.
     * @param req HttpServletRequest
     * @return boolean indicating if user exists
     */
    private boolean confirmRequestorExists(HttpServletRequest req) {
        String requestorId = (String) (req.getParameter("arRequestor"));
        UserItem requestorItem = DaoFactory.DEFAULT.getUserDao().get(requestorId);
        if (requestorItem == null) {
            return false;
        }
        return true;
    }

    /**
     * Returns a list of project requests filtered by search string on project and user name.
     * @param projectRequestList the project request list
     * @param searchString the search string
     * @return a list of project requests filtered by search string on project and user name
     */
    private List<ProjectRequestDTO> searchStringFilter(
            List<ProjectRequestDTO> projectRequestList, String searchString) {
        List<ProjectRequestDTO> newList = new ArrayList<ProjectRequestDTO>();
        for (ProjectRequestDTO dto : projectRequestList) {
            // Retain entries that contain the search string
            if (dto.getUserFullName().toLowerCase().contains(searchString.toLowerCase())
                    || dto.getUserId().toLowerCase().contains(searchString.toLowerCase())
                    || dto.getProjectName().toLowerCase().contains(searchString.toLowerCase())
                    || publicUserMatch(dto, searchString)
                    || searchString == null) {
                newList.add(dto);
            }
        }
        return newList;
    }

    /**
     * Determine if the specified ProjectRequest matches "Public", the default user (%).
     * @param dto the ProjectRequestDTO
     * @param searchString the string to search by
     * @return flag indicating match
     */
    private boolean publicUserMatch(ProjectRequestDTO dto, String searchString) {
        if ((PUBLIC.contains(searchString.toLowerCase()))
                && (dto.getUserId().equals(UserItem.DEFAULT_USER))) {
            return true;
        }
        return false;
    }

    /**
     * Sends the access report as an output stream.
     * @param userItem the user item
     * @param arHelper the AccessRequestHelper object
     * @param arContext the AccessRequestContext object
     * @param resp the HTTP servlet response
     * @param isAccessReport boolean indicating if export is of Access Report
     */
    private void sendExportFile(UserItem userItem, AccessRequestHelper arHelper,
                                AccessRequestContext arContext, HttpServletResponse resp,
                                Boolean isAccessReport) {

        String sortBy = arContext.getSortBy();
        String filterBy = arContext.getFilterBy();

        // Save these values to reset after querying for full list.
        int currentPage = arContext.getCurrentPage();
        int rowsPerPage = arContext.getRowsPerPage();

        if (sortBy == null || sortBy.equals("")) {
            // Default sort-by
            arContext.setSortBy(ProjectRequestDTO.COLUMN_PROJECT);
            arContext.setSortOrder(ProjectRequestDTO.COLUMN_PROJECT, true);
        }

        Integer arCount = isAccessReport ? arHelper.getAccessReportCount(userItem, arContext)
            : arHelper.getCurrentPermissionsCount(userItem, arContext);

        // Start at the beginning and get everything.
        arContext.setCurrentPage(1);
        arContext.setRowsPerPage(arCount);

        List<ProjectRequestDTO> listReport = isAccessReport
            ? arHelper.getAccessReport(userItem, arContext)
            : arHelper.getCurrentPermissions(userItem, arContext);

        // Reset original values.
        arContext.setCurrentPage(currentPage);
        arContext.setRowsPerPage(rowsPerPage);

        File arReport = arHelper.createExportFile(listReport);

        // Create the file label
        Calendar cal = Calendar.getInstance();
        int month = (int)(cal.get(Calendar.MONTH)) + 1;
        String label = isAccessReport ?  "access_report_" : "current_permissions_";
        StringBuffer sb = new StringBuffer(label);
        sb.append(cal.get(Calendar.YEAR));
        sb.append("_");
        sb.append(String.format("%02d", month));
        sb.append(String.format("%02d", cal.get(Calendar.DATE)));
        sb.append("_");
        sb.append(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
        sb.append(String.format("%02d", cal.get(Calendar.MINUTE)));
        sb.append(String.format("%02d", cal.get(Calendar.SECOND)));

        if (arReport != null) {
            try {
                // Set the header type to byte stream
                resp.setContentType("application/octet-stream");
                resp.setHeader("Content-Disposition",
                               "attachment;filename=" + sb.toString() + ".txt");

                ServletOutputStream out1 = resp.getOutputStream();

                // Get the temporary file that holds the access report
                FileInputStream in = new FileInputStream(arReport);

                FileUtils.copyStream(in, out1);

            } catch (IOException e) {
                // Note: if it fails to create the file, the end-user
                // will click on the export button and try again
                logger.debug("IOException creating export file.");
            }
        }
        return;
    }

    /**
     * Creates the request form and fills in the form information by writing to a JSON.
     * @param arHelper the AccessRequestHelper object
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void createRequest(AccessRequestHelper arHelper,
            HttpServletRequest req, HttpServletResponse resp) {
        // Get logged in user
        UserItem userItem = getLoggedInUserItem(req);
        // Get the project id from the Ajax request
        int projectId = 0;
        String projectIdString = (String) req.getParameter("requestButtonId");
        if (projectIdString.replace(REQUEST_ID_PREFIX, "").matches("[0-9]+")) {
            projectId = Integer.parseInt((String) projectIdString.replace(REQUEST_ID_PREFIX, ""));
        }

        // Get the user request and project item
        UserRequestDTO userRequest = arHelper.getUserRequest(
                (String) userItem.getId(), projectId);
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        // Return error message if item retrieval fails
        if (userRequest == null || projectItem == null) {

            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your request cannot be created."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("createRequest:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("createRequest:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
            return;
        }

        // Get the terms of use information for the project
        HashMap<String, Object> touMap = (HashMap<String, Object>) arHelper.getTouInfo(projectItem);
        String touName = null, version = null, terms = null, termsEffective = null;

        // If they exist, load the terms information
        if (touMap != null && touMap.containsKey("name")
                && touMap.containsKey("version")
                && touMap.containsKey("terms")) {
            touName = (String) touMap.get("name");
            version = (String) touMap.get("version");
            terms = (String) touMap.get("terms");
            termsEffective = longDateFormat((Date) touMap.get("termsEffective"));
        }

        // Return the userRequest in JSON
        try {
            writeJSON(
                    resp,
                    json("msg", "success",
                            "projectId", userRequest.getProjectId(),
                            "project", userRequest.getProjectName(),
                            "pi", userRequest.getPiName(),
                            "dp", userRequest.getDpName(),
                            "level", userRequest.getLevel(),
                            "reason", cleanText(userRequest.getReason()),
                            "ownership", projectItem.getOwnership(),
                            "touName", touName,
                            "version", version,
                            "termsEffective", termsEffective,
                            "terms", terms));
        } catch (IOException e) {
            logger.error("createRequest:: "
                + "IOException occurred while attempting to write success response.", e);
        } catch (JSONException e) {
            logger.error("createRequest:: "
                + "JSONException occurred while attempting to write success response.", e);
        }
        return;

    }

    /**
     * Creates the request form and fills in the form information by writing to a JSON.
     * @param arHelper the AccessRequestHelper object
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void createMultiRequest(AccessRequestHelper arHelper,
            HttpServletRequest req, HttpServletResponse resp) {
        // Get logged in user
        UserItem userItem = getLoggedInUserItem(req);
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();

        // Get the project id list from the Ajax request
        String projectIdListString = (String) req.getParameter("wfAccessRequestProjectList");
        String rowIndex = (String) req.getParameter("rowIndex");
        try {
            if (projectIdListString.matches("([0-9]+,?)+") && rowIndex != null) {
                JSONArray jsonArray = new JSONArray();
                String[] projectIds = projectIdListString.split(",");
                Boolean datashopToU = false;

                for (String projectIdString : projectIds) {
                    JSONObject jsonObject = new JSONObject();
                    Integer projectId = Integer.parseInt(projectIdString);

                    // Get auth level
                    AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
                    String authLevel = authDao.getAuthLevel(userItem.getId().toString(), projectId);

                    // Get the user request and project item
                    UserRequestDTO userRequest = arHelper.getUserRequest(
                            (String) userItem.getId(), projectId);


                    ProjectItem projectItem = projectDao.get(projectId);
                    // Return error message if item retrieval fails
                    if (userRequest == null || projectItem == null) {

                        try {
                            writeJSON(resp, json("msg", "error", "cause",
                                    "Your request cannot be created."
                                            + " Please refresh the page and try again."));
                        } catch (IOException e) {
                            logger.error("createRequest:: "
                                + "IOException occurred while attempting to write error response.", e);
                        } catch (JSONException e) {
                            logger.error("createRequest:: "
                                + "JSONException occurred while attempting to write error response.", e);
                        }
                        return;
                    }

                    jsonObject.put("project", projectItem.getProjectName());
                    jsonObject.put("projectId", projectItem.getId());

                    // Get the terms of use information for the project
                    HashMap<String, Object> touMap = (HashMap<String, Object>) arHelper.getTouInfo(projectItem);

                    // If they exist, load the terms information
                    if (touMap != null && touMap.containsKey("name")
                            && touMap.containsKey("version")
                            && touMap.containsKey("terms")) {

                        jsonObject.put("touName", (String) touMap.get("name"));
                        jsonObject.put("version", (String) touMap.get("version"));
                        jsonObject.put("terms", (String) touMap.get("terms"));
                        jsonObject.put("termsEffective", longDateFormat((Date) touMap.get("termsEffective")));
                    } else {
                        // If the specified project does not have any terms, present user with DataShop ToU.
                        touMap = (HashMap<String, Object>) arHelper.getDataShopTouInfo();
                        if (touMap != null && touMap.containsKey("name")
                            && touMap.containsKey("version")
                            && touMap.containsKey("terms")) {

                            jsonObject.put("touName", (String) touMap.get("name"));
                            jsonObject.put("version", (String) touMap.get("version"));
                            jsonObject.put("terms", (String) touMap.get("terms"));
                            jsonObject.put("termsEffective", longDateFormat((Date) touMap.get("termsEffective")));

                            datashopToU = true;
                        }
                    }

                    jsonObject.put("isButtonEnabled", userRequest.isButtonEnabled());
                    jsonObject.put("pi", userRequest.getPiName());
                    jsonObject.put("dp", userRequest.getDpName());
                    jsonObject.put("lastRequest", userRequest.getLastRequest());
                    jsonObject.put("level", userRequest.getLevel());
                    jsonObject.put("status", userRequest.getStatus());

                    jsonObject.put("ownership", projectItem.getOwnership());
                    jsonObject.put("reason", cleanText(userRequest.getReasonWithLinebreaks()));
                    jsonArray.put(jsonObject);

                }
                writeJSON(
                    resp,
                    json("msg", "success", "rowIndex", rowIndex, "jsonArray", jsonArray, "datashopToU", datashopToU));

            } else {
                writeJSON(
                    resp,
                        json("msg", "error", "message", "No projects were found."));
            }
        } catch (IOException e) {
            logger.error("createRequest:: "
                + "IOException occurred while attempting to write success response.", e);
        } catch (JSONException e) {
            logger.error("createRequest:: "
                + "JSONException occurred while attempting to write success response.", e);
        }
        return;

    }

    /**
     * Submits the request and write the results to a JSON.
     * @param arHelper the AccessRequestHelper object
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void submitRequest(
            AccessRequestHelper arHelper, HttpServletRequest req, HttpServletResponse resp) {
        UserItem userItem = null;
        String newUser = (String)req.getParameter("newUser");
        if (newUser == null) {
            // Get logged in user
            userItem = getLoggedInUserItem(req);
        } else {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            userItem = userDao.get(newUser);
            if (userItem == null) {
                try {
                    writeJSON(resp, json("msg", "error", "cause",
                                         "Specified user does not exist."));
                } catch (IOException e) {
                    logger.error("submitRequest:: "
                        + "IOException occurred while attempting to write error response.", e);
                } catch (JSONException e) {
                    logger.error("submitRequest:: "
                        + "JSONException occurred while attempting to write error response.", e);
                }
                return;
            }
        }

        // Get the projectId, level, and reason
        String projectIdString = (String) req.getParameter("arProjectId");
        int projectId = 0;
        if (projectIdString.replace(REQUEST_ID_PREFIX, "").matches("[0-9]+")) {
            projectId = Integer.parseInt(((String) req
                    .getParameter("arProjectId")).replace(REQUEST_ID_PREFIX, ""));
        }

        String level = (String) (req.getParameter("arLevel"));
        String shareReasonFlag = (String) (req.getParameter("arShareReasonFlag"));
        String reason = (String) limitText(req.getParameter("arReason"));

        // Initialize Dao's
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        // Return error message if item retrieval fails
        if (projectItem == null) {
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your request cannot be created."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("submitRequest:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("submitRequest:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
            return;
        }

        // Note if request is on behalf of the user (via Permissions 'Add New User').
        UserItem loggedInUserItem = getUser(req);
        boolean isAddNewUserRequest = !userItem.equals(loggedInUserItem) ? true : false;

        // Request access to the project based on the user dialog values
        String baseUrl = getBaseUrl(req);
        AccessRequestStatusItem arStatusItem = arHelper.requestAccess(baseUrl, userItem,
                                                                      projectItem, level,
                                                                      reason, isAddNewUserRequest);

        // If request is on behalf of the user, update requestor
        if (arStatusItem != null && isAddNewUserRequest) {
            AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
            AccessRequestHistoryItem arHistoryItem = arHistoryDao.findLastRequest(arStatusItem);
            if (arHistoryItem != null) {
                arHistoryItem.setUser(loggedInUserItem);
                arHistoryDao.saveOrUpdate(arHistoryItem);
            }
        } else if (arStatusItem == null && isAddNewUserRequest) {
            try {
                // If the status item is null, then we will not complete the request.
                String prefix = "submitRequest(" + loggedInUserItem + "): ";
                logInfo(prefix, " User ''" + userItem.getId() + "' cannot be given '"
                        + level + "' access to the project (" + projectItem.getId() + ").");

                writeJSON(resp, json("msg", "error", "cause",
                        "Your request cannot be saved."
                            + "User ''" + userItem.getId() + "' cannot be given '"
                            + level + "' access to the project."));
            } catch (IOException e) {
                logger.error("submitRequest:: "
                    + "IOException occurred while attempting to write save error response.", e);
            } catch (JSONException e) {
                logger.error("submitRequest:: "
                    + "IOException occurred while attempting to write save error response.", e);
            }
            return;
        }

        UserRequestDTO userRequest = arHelper.getUserRequest(
                (String) userItem.getId(), projectId);

        // Return the userRequest in JSON
        if (userRequest != null) {

            if (userRequest.getStatus().equals(
                    AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
                userRequest.setStatus(AccessRequestHelper.ACCESS_REQUEST_DISPLAY_APPROVED);
            } else if (userRequest.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                   || userRequest.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                   || userRequest.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
               // Set status to denied
                userRequest.setStatus(AccessRequestHelper.ACCESS_REQUEST_DISPLAY_DENIED);
           } else if (userRequest.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                   || userRequest.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)) {
               // Set status to partially approved
               userRequest.setStatus(AccessRequestHelper.ACCESS_REQUEST_DISPLAY_PARTIALLY_APPROVED);
           } else if (userRequest.getStatus()
                   .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED)) {
               // Set status to not reviewed
               userRequest.setStatus(AccessRequestHelper.ACCESS_REQUEST_DISPLAY_NOT_REVIEWED);
           }

            try {
                writeJSON(
                        resp,
                        json("msg", "success",
                                "status", (userRequest.getStatus()),
                                "projectId", userRequest.getProjectId(),
                                "project", userRequest.getProjectName(),
                                "pi", userRequest.getPiName(),
                                "level", userRequest.getLevel(),
                                "lastRequest", quickDateFormat(userRequest.getLastRequest()),
                                "reason", cleanText(userRequest.getReason()),
                                "buttonTitle", userRequest.getButtonTitle(),
                                "isButtonEnabled", userRequest.isButtonEnabled()));
            } catch (IOException e) {
                logger.error("submitRequest:: "
                    + "IOException occurred while attempting to write success response.", e);
            } catch (JSONException e) {
                logger.error("submitRequest:: "
                    + "JSONException occurred while attempting to write success response.", e);
            }
            return;
        } else {
            // Return error message if user request retrieval fails
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your request cannot be saved."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("submitRequest:: "
                    + "IOException occurred while attempting to write save error response.", e);
            } catch (JSONException e) {
                logger.error("submitRequest:: "
                    + "IOException occurred while attempting to write save error response.", e);
            }
            return;
        }
    }

    /**
     * Submits the request and write the results to a JSON.
     * @param arHelper the AccessRequestHelper object
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void submitRequestWf(
            AccessRequestHelper arHelper, HttpServletRequest req, HttpServletResponse resp) {
        UserItem userItem = getLoggedInUserItem(req);

        // Get the projectIds, level, and reason
        String projectIdString = (String) req.getParameter("projectIds");

        String level = (String) (req.getParameter("arLevel"));
        String reason = (String) limitText(req.getParameter("arReason"));
        String errorMessage = new String();
        String rowIndexStr = (String) req.getParameter("rowIndex");
        Integer rowIndex = null;
        if (rowIndexStr.matches("\\d+")) {
            rowIndex = Integer.parseInt(rowIndexStr);
        }

        List<Integer> projectIds = new ArrayList<Integer>();
        StringBuffer projectNameBuffer = new StringBuffer();
        if (projectIdString.matches("([0-9]+,?)+")) {
            String projectIdsArray[] = projectIdString.split(",");
            for (String idString : projectIdsArray) {
                if (idString.matches("[0-9]+")) {
                    projectIds.add(new Integer(idString));
                }
            }
            Integer projectCounter = 0;
            for (Integer projectId : projectIds) {
                // Initialize Dao's
                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                ProjectItem projectItem = projectDao.get(projectId);
                // Return error message if item retrieval fails
                if (projectItem == null) {
                    errorMessage = errorMessage + "The project was not found. ";
                } else {
                    projectNameBuffer.append(projectItem.getProjectName());
                    if (projectCounter < projectIds.size() - 1) {
                        projectNameBuffer.append(", ");
                    }

                    boolean isAddNewUserRequest = true;

                    // Request access to the project based on the user dialog values
                    String baseUrl = getBaseUrl(req);
                    AccessRequestStatusItem arStatusItem = arHelper.requestAccess(baseUrl, userItem,
                                                                                  projectItem, level,
                                                                                  reason, isAddNewUserRequest);
                    // If request is on behalf of the user, update requestor
                    if (arStatusItem != null && isAddNewUserRequest) {
                        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
                        AccessRequestHistoryItem arHistoryItem = arHistoryDao.findLastRequest(arStatusItem);
                        if (arHistoryItem != null) {
                            arHistoryItem.setUser(userItem);
                            arHistoryDao.saveOrUpdate(arHistoryItem);
                        }
                    }

                    UserRequestDTO userRequest = arHelper.getUserRequest(
                            (String) userItem.getId(), projectId);

                }
                projectCounter++;
            }
        }

        try {
            if (errorMessage.isEmpty()) {
                writeJSON(
                    resp,
                    json("msg", "success",
                            "message", "Your requests have been submitted. If you do not receive a response from the "
                            + "project administrators within 24 hours, you may send another request or contact "
                            + "<a href=\"mailto:"
                            + getEmailAddressDatashopHelp() + "\">DataShop Help</a>.",
                            "projectIdString", projectIdString,
                            "rowIndex", rowIndex,
                            "projectNameString", projectNameBuffer.toString()));
            } else {
                writeJSON(
                        resp,
                        json("msg", "error",
                                "message", errorMessage));
            }
        } catch (IOException e) {
            logger.error("submitRequest:: "
                + "IOException occurred while attempting to write success response.", e);
        } catch (JSONException e) {
            logger.error("submitRequest:: "
                + "JSONException occurred while attempting to write success response.", e);
        }
        return;
    }
    /**
     * Creates the make project public form and fills in the form information by
     * writing to a JSON.
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void createProjectPublic(HttpServletRequest req,
            HttpServletResponse resp) {
        // Get logged in user
        UserItem userItem = getLoggedInUserItem(req);
        // Get the project Id and user Request
        String buttonId = ((String) (req.getParameter("makePublicButtonId")))
                    .replace(PROJECT_ID_PREFIX, "")
                    .replace(RESPONSE_ID_PREFIX, "");

        if (buttonId.matches("[0-9]+_[0-9]+")) {
            String[] tmp = buttonId.split("_");
            buttonId = tmp[0];
        }
        if (!buttonId.matches("[0-9]+")) {
            buttonId = "0";
        }
        int projectId = Integer.parseInt((String) buttonId);

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        // Return error message if item retrieval fails
        if (projectItem == null) {
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your request cannot be created."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("createProjectPublic:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("createProjectPublic:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
            return;
        }

        // Determine if respondent is PA (acting for PI)
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean isPA = authDao.isProjectAdmin(userItem, projectItem);

        String role = projectItem.getRole(userItem, isPA);

        // Get the 'other party' user item (i.e., PI or DP)
        String otherParty = null;
        if (projectItem.getOwnership().equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)) {
            if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                otherParty = "data provider";
            } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP)) {
                otherParty = "PI";
            } else {
                otherParty = null;
            }
        }
        // Return the userRequest in JSON

        try {
            writeJSON(
                    resp,
                    json("msg", "success", "projectId",
                            projectId, "otherParty",
                            otherParty));
        } catch (IOException e) {
            logger.error("createProjectPublic:: "
                + "IOException occurred while attempting to write success response.", e);
        } catch (JSONException e) {
            logger.error("createProjectPublic:: "
                + "JSONException occurred while attempting to write success response.", e);
        }

        return;
    }

    /**
     * Submits the make project public request and write the results to a JSON.
     *
     * @param arHelper the AccessRequestHelper object
     * @param currentTab the current tab
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void submitProjectPublic(AccessRequestHelper arHelper, String currentTab,
            HttpServletRequest req, HttpServletResponse resp) {

        // Get logged in user
        UserItem respondentItem = getLoggedInUserItem(req);

        // The row Id is only used for the Access Request page, not the project page
        String rowId = "";
        if (req.getParameter("rowId") != null) {
            rowId = (String) (req.getParameter("rowId"));
        } else {
            rowId = "";
        }

        // Filter untrusted input
        String projectIdString = (String) req.getParameter("arProjectId");
        int projectId = 0;
        if (projectIdString.matches("[0-9]+")) {
            projectId = Integer.parseInt(projectIdString);
        }

        // Get the projectId, action, level, and reason
        String action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE;
        String level = AuthorizationItem.LEVEL_PUBLIC;
        String reason = "";
        String requestorId = UserItem.DEFAULT_USER;

        // Get the requestor
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem requestorItem = userDao.get(requestorId);

        // Initialize Dao's
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        // Failed to retrieve project item
        if (projectItem == null) {
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your request cannot be saved."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("submitProjectPublic:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("submitProjectPublic:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
            return;
        }

        // Respond to the request if one was made, or allow access to be
        // modified for users in the authorization table
        String baseUrl = getBaseUrl(req);
        arHelper.respond(baseUrl, respondentItem, requestorItem, projectItem, action,
                level, reason, false);

        // Get the user request information
        UserRequestDTO userRequest = arHelper.getUserRequest(requestorId,
                projectId);

        // Get the status item
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT
                .getAccessRequestStatusDao();
        AccessRequestStatusItem arStatusItem = arStatusDao
                .findByUserAndProject(requestorItem, projectItem);

        // We have the user request info and the status item
        if (userRequest != null && arStatusItem != null) {
            // Determine if respondent is PA (acting for PI)
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            boolean isPA = authDao.isProjectAdmin(respondentItem, projectItem);
            String role = projectItem.getRole(respondentItem, isPA);

            // The request information and items
            String activityDateString = null;
            arStatusItem = arStatusDao.get((Integer) arStatusItem.getId());
            activityDateString = quickDateFormat(arStatusItem
                    .getLastActivityDate());
            String status = userRequest.getStatus();

            // GUI info
            String isButtonVisible = "false";
            String buttonTitle = AccessRequestHelper.BUTTON_TITLE_VOTE;

            // Assign the buttonTitle and whether or not it's visible based on status
            if (status
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)
                    || status
                            .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)) {
                isButtonVisible = "false";
                buttonTitle = "";
            } else if (status
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                    || status
                            .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                isButtonVisible = "false";
                buttonTitle = "";
            } else if ((status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                            && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI))
                      || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)
                            && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP))) {
                isButtonVisible = "true";
                buttonTitle = AccessRequestHelper.BUTTON_TITLE_HAS_VOTED;
            } else if (currentTab
                    .equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED)
                    || currentTab
                            .equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT)) {
                isButtonVisible = "true";
            }
            // Return in JSON
            try {
                writeJSON(
                        resp,
                        json("msg", "success",
                                "self", respondentItem.getName(),
                                "selfEmail", (respondentItem.getEmail() == null)
                                    ? "" : respondentItem.getEmail(),
                                "piVoted", arHelper.hasVotedProjectPublic(projectId,
                                        AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI),
                                "dpVoted", arHelper.hasVotedProjectPublic(projectId,
                                        AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP),
                                "status", userRequest.getStatus(),
                                "projectId", userRequest.getProjectId(),
                                "project", userRequest.getProjectName(),
                                "pi", userRequest.getPiName(),
                                "dp", userRequest.getDpName(),
                                "role", role,
                                "action", action,
                                "level", userRequest.getLevel(),
                                "myLevel", level,
                                "reason", reason,
                                "rowId", rowId,
                                "buttonTitle", buttonTitle,
                                "isButtonVisible", isButtonVisible,
                                "lastActivityDate", activityDateString));
            } catch (IOException e) {
                logger.error("submitProjectPublic:: "
                    + "IOException occurred while attempting to write success response.", e);
            } catch (JSONException e) {
                logger.error("submitProjectPublic:: "
                    + "JSONException occurred while attempting to write success response.", e);
            }
        } else { // Failed to retrieve the status item or request info
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your response cannot be saved."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("submitProjectPublic:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("submitProjectPublic:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
        }
        return;
    }

    /** String constant. */
    private static final String VIEW_TXT = "view access (ability to view analyses, use tools, "
            + "and export data) - <strong>recommended</strong>";
    /** String constant. */
    private static final String EDIT_TXT = "edit access (view access plus the ability to add "
            + "files, add KC models, and create samples)";
    /** String constant. */
    private static final String ADMIN_TXT = "admin access (edit access plus the ability to "
        + "edit project and dataset metadata, grant/change access, upload datasets, and manage "
        + "IRB documentation)";
    /**
     * Creates the response form and fills in the form information by writing to
     * a JSON based on the selected project request table row in the jsp.
     * @param arHelper helper (not sure why this is passed in)
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void createResponse(AccessRequestHelper arHelper,
            HttpServletRequest req, HttpServletResponse resp) {
        // Each row has a unique Id in the project request table
        String rowId = (String) (req.getParameter("rowId"));
        // Get the project and user items
        // Filter untrusted input
        String projectIdString = (String) req.getParameter("projectId");
        int projectId = 0;
        if (projectIdString.matches("[0-9]+")) {
            projectId = Integer.parseInt(projectIdString);
        }
        String requestorId = (String) (req.getParameter("requestUserId"));
        String lastReason = "";
        String lastLevel = "";
        // Project items
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        if (projectItem == null) {
            try {
                writeJSON(resp, json("msg", "error",
                        "cause", "Your response cannot be created."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("createResponse:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("createResponse:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
            return;
        }

        // User items
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem requestorItem = userDao.get(requestorId);
        UserItem respondentItem = getLoggedInUserItem(req);
        // User request
        UserRequestDTO userRequest = arHelper.getUserRequest(
                (String) requestorId, projectId);

        // Access Request DAO's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        // Get the status item for this requestor and project
        AccessRequestStatusItem arStatus =
                (AccessRequestStatusItem) arStatusDao
                    .findByUserAndProject(requestorItem, projectItem);

        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean isPA = authDao.isProjectAdmin(respondentItem, projectItem);
        String role = projectItem.getRole(respondentItem, isPA);

        // The request information and items
        // Get the admin history item
        AccessRequestHistoryItem adminHistoryItem = arHistoryDao
                .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);
        // Generic history item, might even be assign adminHistoryItem to it
        AccessRequestHistoryItem arHistoryItem = null;
        // PI last response
        if (role.equals(ProjectItem.ROLE_PI)) {
            arHistoryItem = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
        // DP last response
        } else if (role.equals(ProjectItem.ROLE_DP)) {
            arHistoryItem = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
        // Administrator last response
        } else if (respondentItem.getAdminFlag()) {
            arHistoryItem = adminHistoryItem;
        }

        // Get last reason and last level if they exist
        if (arHistoryItem != null) {
            lastReason = arHistoryItem.getReason();
            lastLevel = arHistoryItem.getLevel();
        }

        // Return the request information in JSON
        if (userRequest != null && requestorItem != null) {
            boolean isMakePublic = false;
            // The last request date of the user (or administrator if no user request exists)
            Date lastRequest = null;
            // The requestor name.
            String userName = requestorItem.getName();
            if (requestorItem.getId().equals(UserItem.DEFAULT_USER)) {
                isMakePublic = true;
            }
            // Make sure to get the lastLevel from the respondent if it exists
            String selectedLevel = userRequest.getLevel();
            if (!lastLevel.equals("")) {
                selectedLevel = lastLevel;
            }

            // Need to consider if initial request was made by someone other than user
            AccessRequestHistoryItem requestedByItem = arHistoryDao
                .findLastResponse(arStatus,
                                  AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_REQUESTOR);

            String requesterName = "";

            // If a row was created by administrator and no user request exists
            if (userRequest.getLastRequest() == null && adminHistoryItem != null) {
                // Use the administrator name and date for display
                lastRequest = adminHistoryItem.getDate();
                UserItem adminUserItem = userDao.get((String) adminHistoryItem.getUser().getId());
                requesterName = adminUserItem.getName();
                if (adminUserItem.getAdminFlag()) {
                    requesterName += " (admin)";
                }
            } else if (userRequest.getLastRequest() != null) {
                // Use the user name and date for display
                lastRequest = userRequest.getLastRequest();

                if (requestedByItem != null) {
                    UserItem theUserItem = userDao.get((String) requestedByItem.getUser().getId());
                    if (!userName.equals(theUserItem.getName())) {
                        requesterName = theUserItem.getName();
                        if (theUserItem.getAdminFlag()) {
                            requesterName += " (admin)";
                        }
                    }
                }
            }

            /*
             * Trac 455:
             * If one of the parties has already voted, get their reason (if any).
             */
            String firstVoteReason = null;
            String firstVoteRole = null;
            if (arStatus != null && arStatus.getHasPiSeen()) {
                AccessRequestHistoryItem piHistoryItem = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
                if (piHistoryItem != null) {
                    firstVoteReason = piHistoryItem.getReason();
                    firstVoteRole = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI;
                    selectedLevel = piHistoryItem.getLevel();
                }
            } else if (arStatus != null && arStatus.getHasDpSeen()) {
                AccessRequestHistoryItem dpHistoryItem = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
                if (dpHistoryItem != null) {
                    firstVoteReason = dpHistoryItem.getReason();
                    firstVoteRole = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP;
                    selectedLevel = dpHistoryItem.getLevel();
                }
            }

            String currentAccessLevel =
                getAuthLevelForDisplay(authDao.getAuthorization((String)requestorItem.getId(),
                                                                (Integer)projectItem.getId()));
            /*
             * Trac 455:
             * If status is already 'approved', note that opening the response dialog
             * indicates a wish to change the level.
             *
             * If the status is not 'approved' but there was a point in the past when it
             * was (and PI and DP must both vote), the 'Change Response' dialog should be opened.
             */
            boolean isChangeRequest = false;
            boolean isChangeResponse = false;
            if (arStatus != null && arStatus.getStatus().equals(AccessRequestStatusItem
                                            .ACCESS_RESPONSE_STATUS_APPROVED)) {
                isChangeRequest = true;
            } else if ((currentAccessLevel != null) &&
                       projectItem.getOwnership().equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)) {
                isChangeResponse = true;
            }

            // The request level string
            String requestLevel = getAuthLevelForDisplay(userRequest.getLevel());

            // Return in JSON
            String institution = requestorItem.getInstitution() == null
                    ? "" : requestorItem.getInstitution();
            String email = requestorItem.getEmail() == null ? "" : requestorItem.getEmail();
            String userAdmin = "";

            try {
                writeJSON(
                        resp,
                        json("msg", "success",
                             "email", email,
                             "date", quickDateFormat(lastRequest),
                             "userName", userName,
                             "userId", requestorItem.getId(),
                             "requester", requesterName,
                             "institution", institution,
                             "projectId", projectId,
                             "project", userRequest.getProjectName(),
                             "pi", userRequest.getPiName(),
                             "dp", userRequest.getDpName(),
                             "requestLevel", requestLevel,
                             "level", selectedLevel,
                             "lastReason", (lastReason),
                             "reason", cleanText(userRequest.getReason()),
                             "rowId", rowId,
                             "userAdmin", userAdmin,
                             "firstVoteReason", firstVoteReason,
                             "firstVoteRole", firstVoteRole,
                             "isChangeRequest", isChangeRequest,
                             "currentAccessLevel", currentAccessLevel,
                             "isChangeResponse", isChangeResponse,
                             "isMakePublic", isMakePublic));
            } catch (IOException e) {
                logger.error("createResponse:: "
                    + "IOException occurred while attempting to write success response.", e);
            } catch (JSONException e) {
                logger.error("createResponse:: "
                    + "JSONException occurred while attempting to write success response.", e);
            }
        } else { // Request info or requestor not found
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your response cannot be created."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("createResponse:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("createResponse:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
        }
        return;
    }

    /**
     * Convert an authorization level to display text.
     * @param authLevel the authorization level
     * @return String display text
     */
    private String getAuthLevelForDisplay(String authLevel) {

        if (authLevel == null) { return null; }

        return authLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_EDIT) ? EDIT_TXT :
            (authLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_ADMIN) ?
             ADMIN_TXT : VIEW_TXT);
    }

    /**
     * Creates the new access row form and fills in the form information by writing to a JSON.
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void createNewAccessRow(HttpServletRequest req, HttpServletResponse resp) {
        // Get a list of all projects and users
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        List<ProjectItem> projectList = new ArrayList<ProjectItem>();
        List<UserItem> userList = new ArrayList<UserItem>();

        // Projects
        Map<String, Integer> projectMap = new HashMap<String, Integer> ();
        projectList = projectDao.findAll();
        for (ProjectItem project : projectList) {
            if (project.getProjectName() != null) {
                projectMap.put(project.getProjectName(), (Integer)project.getId());
            }
        }
        // As JSON object
        JSONObject projectJson = new JSONObject(projectMap);

        // Users
        Map<String, String> userMap = new HashMap<String, String> ();
        userList = userDao.findAll();
        for (UserItem user : userList) {
            if (user.getId().equals(UserItem.DEFAULT_USER)) {
                userMap.put("% (Public)", (String)user.getId());
            } else {
                String userIdentity = "";
                if (user.getLastName() != null && user.getFirstName() != null) {
                    userIdentity = user.getLastName() + ", " + user.getFirstName() + " ";
                }
                if (user.getId() != null && userIdentity.equals("")) {
                    userIdentity = (String) user.getId();
                    if (user.getEmail() != null) {
                        userIdentity = userIdentity + " (" + user.getEmail() + ")";
                    }
                } else {
                    userIdentity = userIdentity + "(" + user.getId();
                    if (user.getEmail() != null) {
                        userIdentity = userIdentity + ", " + user.getEmail() + ")";
                    } else {
                        userIdentity = userIdentity + ")";
                    }
                }

                userMap.put(userIdentity, (String)user.getId());
            }
        }
        // As JSON object
        JSONObject userJson = new JSONObject(userMap);

        // Return user and project maps in JSON
        try {
            writeJSON(resp,
                json("msg", "success",
                    "userList", userJson,
                    "projectList", projectJson));
        } catch (IOException e) {
            logger.error("createNewAccessRow:: "
                + "IOException occurred while attempting to write success response.", e);
        } catch (JSONException e) {
            logger.error("createNewAccessRow:: "
                + "JSONException occurred while attempting to write success response.", e);
        }
        return;
    }

    /**
     * Submits the response and writes the results in JSON.
     *
     * @param arHelper the AccessRequestHelper object
     * @param currentTab the current tab
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void submitResponse(AccessRequestHelper arHelper, String currentTab,
            HttpServletRequest req, HttpServletResponse resp) {
        // Get logged in user
        UserItem respondentItem = getLoggedInUserItem(req);
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        // Get the requestor item
        String requestorId = (String) (req.getParameter("arRequestor"));
        UserItem requestorItem = userDao.get(requestorId);
        // Get the history of the request
        String lastLevel = null;
        String lastStatus = null;
        // Get the projectId, action, level, and reason
        // Filter untrusted input
        String projectIdString = (String) req.getParameter("arProjectId");
        int projectId = 0;
        if (projectIdString.matches("[0-9]+")) {
            projectId = Integer.parseInt(projectIdString);
        }
        String action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
        String level = (String) (req.getParameter("arLevel"));
        String shareReasonFlagString = (String) (req.getParameter("arShareReasonFlag"));
        String reason = (String) limitText(req.getParameter("arReason"));
        // Each request has a unique row id in the project request table
        String rowId = (String) (req.getParameter("rowId"));

        boolean shareReasonFlag = false;
        if (shareReasonFlagString != null && shareReasonFlagString.equals("on")) {
            shareReasonFlag = true;
        }

        // Access Rows that are created by administrator have an additional flag
        String newAccessRowString = (String) req.getParameter("arCreateNewAccessRowFlag");
        boolean createNewAccessRowFlag = false;
        if (newAccessRowString != null && newAccessRowString.equals("true")) {
            createNewAccessRowFlag = true;
        }

        // Initialize Dao's
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT
                .getAccessRequestStatusDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        // Return error message if project not found
        if (projectItem == null) {
            try {
                writeJSON(resp, json("msg", "error",
                        "cause", "Your response cannot be created."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("subnmitResponse:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("subnmitResponse:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
            return;
        }

        // Set the action based on the dialog options
        if (level.equals(AuthorizationItem.LEVEL_ADMIN)
         || level.equals(AuthorizationItem.LEVEL_EDIT)
         || level.equals(AuthorizationItem.LEVEL_VIEW)) {
            action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE;
        } else if (level
                .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
            action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
        }

        String baseUrl = getBaseUrl(req);
        // Get the last level of authorization for the requestor
        AccessRequestStatusItem arStatusItem = arStatusDao
                .findByUserAndProject(requestorItem, projectItem);

        String auth = authDao.getAuthorization(
                (String)requestorItem.getId(),
                (Integer)projectItem.getId());
        lastLevel = auth;
        // Get last status for the requestor
        if (arStatusItem != null) {
            lastStatus = arStatusItem.getStatus();
        }

        boolean isPA = authDao.isProjectAdmin(respondentItem, projectItem);

        // PI, Data provider or PA response
        if (!createNewAccessRowFlag) {
            arHelper.respond(baseUrl, respondentItem, requestorItem, projectItem, action,
                level, reason, shareReasonFlag);
        // Administrator response or new access row
        } else if ((respondentItem.getAdminFlag() || isPA) && createNewAccessRowFlag) {
            arHelper.newAccess(baseUrl, respondentItem, requestorItem, projectItem, action,
                    level, reason, shareReasonFlag);
        }
        // Get the request information
        UserRequestDTO userRequest = arHelper.getUserRequest(requestorId,
                projectId);
        // Get the status item after response
        arStatusItem = arStatusDao
                .findByUserAndProject(requestorItem, projectItem);

        // Determine GUI configuration based on request status
        if (userRequest != null && arStatusItem != null) {
            String activityDateString = null;
            arStatusItem = arStatusDao.get((Integer) arStatusItem.getId());
            // Request information
            activityDateString = quickDateFormat(arStatusItem
                    .getLastActivityDate());
            String isButtonVisible = "false";
            String role = projectItem.getRole(respondentItem, isPA);

            String buttonTitle = "Respond";
            String status = userRequest.getStatus();

            // Set the GUI attributes according to the status
            if ((status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)
                      || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED))

                    || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                      && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI))

                    || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)
                      && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP))

                    || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                      || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED))) {

                isButtonVisible = "false";
                buttonTitle = status;
            // If Recent Activity or Not Reviewed sub-tab, then button is visible
            } else if (!currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
                isButtonVisible = "true";
            }

            // Get the latest activity counts
            Map<String, Long> activityCountMap = arHelper.getActivityCountsMap(respondentItem);

            // Return in JSON
            try {
                writeJSON(
                        resp,
                        json("msg", "success",
                            "self", respondentItem.getName(),
                            "requestorId", requestorItem.getId(),
                            "requestorName", requestorItem.getName(),
                            "rowId", rowId,
                            "currentTab", currentTab,
                            "status", userRequest.getStatus(),
                            "projectId", userRequest.getProjectId(),
                            "project", userRequest.getProjectName(),
                            "pi", userRequest.getPiName(),
                            "dp", userRequest.getDpName(),
                            "role", role,
                            "action", action,
                            "level", userRequest.getLevel(),
                            "myLevel", level,
                            "lastLevel", lastLevel,
                            "lastStatus", lastStatus,
                            "reason", cleanText(reason),
                            "buttonTitle", buttonTitle,
                            "isButtonVisible", isButtonVisible,
                            "lastActivityDate", activityDateString,
                            "myRequestsCount",
                            activityCountMap.get(AccessRequestStatusItem.STATE_COUNT_MY_REQUESTS),
                            "notReviewedCount",
                            activityCountMap.get(AccessRequestStatusItem.STATE_COUNT_NOT_REVIEWED),
                            "recentCount",
                            activityCountMap.get(
                                AccessRequestStatusItem.STATE_COUNT_RECENT_ACTIVITY),
                            "totalCount",
                            activityCountMap.get(AccessRequestStatusItem.STATE_COUNT_TOTAL)
                            ));
            } catch (IOException e) {
                logger.error("submitResponse:: "
                    + "IOException occurred while attempting to write success response.", e);
            } catch (JSONException e) {
                logger.error("submitResponse:: "
                    + "JSONException occurred while attempting to write success response.", e);
            }
        } else {  // Failed to retrieve the status item or requestor
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your response cannot be saved."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("submitResponse:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("submitResponse:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
        }
        return;
    }

    /**
     * Formats a Date type object as yyyy-MM-dd or returns an empty string if the date is null.
     * @param date the date
     * @return the formatted string or an empty string if the date is null
     */
    public static String quickDateFormat(Date date) {
        String dateString = "";
        if (date != null) {
            FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
            StringBuffer firstAccessSB = new StringBuffer("");
            FieldPosition fieldPos = new FieldPosition(0);
            dateFormat.format(date, firstAccessSB, fieldPos);
            dateString = firstAccessSB.toString();
        }
        return dateString;
    }

    /**
    * Formats a Date type object as MMMM dd yyyy or returns an empty string if the date is null.
    * @param date the date
    * @return the formatted string or an empty string if the date is null
    */
   public static String longDateFormat(Date date) {
       String dateString = "";
       if (date != null) {
           FastDateFormat dateFormat = FastDateFormat.getInstance("MMMM dd, yyyy");
           StringBuffer firstAccessSB = new StringBuffer("");
           FieldPosition fieldPos = new FieldPosition(0);
           dateFormat.format(date, firstAccessSB, fieldPos);
           dateString = firstAccessSB.toString();
       }
       return dateString;
   }

    /**
     * Changes Any String To Naive Title Case.
     * @param s the string
     * @return The String In Naive Title Case
     */
    public static String titleCase(String s) {
        String str = s.replaceAll("_", " ");
        BreakIterator wordBreaker = BreakIterator.getWordInstance();
        wordBreaker.setText(str);
        String titleCase = "";

        int end = 0;
        for (int start = wordBreaker.first();
                end != BreakIterator.DONE;
                    start = end) {

            end = wordBreaker.next();

            String capWord = "";
            if (start + 1 < end) {
                capWord = str.substring(start, start + 1).toUpperCase()
                        + str.substring(start + 1, end) + " ";
            } else if (start + 1 == end) {
                capWord = str.substring(start, start + 1).toUpperCase() + " ";
            }

            titleCase += capWord;
        }

        return titleCase.trim();
    }

    /**
     * Defines which sorting parameters to use for sorting UserRequestDTO's
     * based on an user selected column; handles ascending or descending.
     * @param arContext the Access Request Context
     * @return the SortParameter array
     */
    public static SortParameter[] selectSortParameters(AccessRequestContext arContext) {
        String sortByString = arContext.getSortBy();
        Boolean isAscending = arContext.getSortOrder(sortByString);
        return ProjectRequestDTO.selectSortParameters(sortByString, isAscending);
    }


    /**
     * Submits the make project public request and write the results to a JSON.
     *
     * @param arHelper the AccessRequestHelper object
     * @param currentTab the current tab
     * @param req the Http Servlet Request
     * @param resp the Http Servlet Response
     */
    public void denyProjectPublic(AccessRequestHelper arHelper, String currentTab,
            HttpServletRequest req, HttpServletResponse resp) {

        // Get logged in user
        UserItem respondentItem = getLoggedInUserItem(req);
        String requestorName = "";

        // The row Id is only used for the Access Request page, not the project page
        String rowId = "";
        if (req.getParameter("rowId") != null) {
            rowId = (String) (req.getParameter("rowId"));
        } else {
            rowId = "";
        }

        // Filter untrusted input
        String projectIdString = (String) req.getParameter("arProjectId");
        int projectId = 0;
        if (projectIdString.matches("[0-9]+")) {
            projectId = Integer.parseInt(projectIdString);
        }

        // Get the projectId, action, level, and reason
        String action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
        String level = AuthorizationItem.LEVEL_PUBLIC;
        String reason = "";
        String requestorId = UserItem.DEFAULT_USER;

        // Get the requestor
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem requestorItem = userDao.get(requestorId);

        // Initialize Dao's
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        // Failed to retrieve project item
        if (projectItem == null) {
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your request cannot be saved."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("denyProjectPublic:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("denyProjectPublic:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
            return;
        }

        // Respond to the request if one was made, or allow access to be
        // modified for users in the authorization table
        String baseUrl = getBaseUrl(req);
        arHelper.respond(baseUrl, respondentItem, requestorItem, projectItem, action,
                level, reason, false);

        // Get the user request information
        UserRequestDTO userRequest = arHelper.getUserRequest(requestorId,
                projectId);

        // Get the status item
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT
                .getAccessRequestStatusDao();
        AccessRequestStatusItem arStatusItem = arStatusDao
                .findByUserAndProject(requestorItem, projectItem);

        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean isPA = authDao.isProjectAdmin(respondentItem, projectItem);

        // We have the user request info and the status item
        if (userRequest != null && arStatusItem != null) {
            // The request information and items
            String role = projectItem.getRole(respondentItem, isPA);
            String activityDateString = null;
            arStatusItem = arStatusDao.get((Integer) arStatusItem.getId());
            activityDateString = quickDateFormat(arStatusItem
                    .getLastActivityDate());
            String status = userRequest.getStatus();

            // GUI info
            String isButtonVisible = "false";
            String buttonTitle = AccessRequestHelper.BUTTON_TITLE_VOTE;
            if (requestorItem != null) {
                requestorName = requestorItem.getName();
            }
            // Assign the buttonTitle and whether or not it's visible based on status
            if (status
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)
                    || status
                            .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)) {
                isButtonVisible = "false";
                buttonTitle = "";
            } else if (status
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                    || status
                            .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                isButtonVisible = "false"; isButtonVisible = "false";
                buttonTitle = "";
            } else if ((status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                            && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI))
                      || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)
                            && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP))) {
                isButtonVisible = "true"; isButtonVisible = "false";
                buttonTitle = AccessRequestHelper.BUTTON_TITLE_HAS_VOTED;
            } else if (currentTab
                    .equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED)
                    || currentTab
                            .equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT)) {
                isButtonVisible = "true"; isButtonVisible = "false";
            }
            // Get available counts
            Map<String, Long> activityCountMap = arHelper.getActivityCountsMap(respondentItem);

            // Return in JSON
            try {
                writeJSON(
                        resp,
                        json("msg", "success",
                                "self", respondentItem.getName(),
                                "selfEmail", (respondentItem.getEmail() == null)
                                    ? "" : respondentItem.getEmail(),
                                "piVoted", arHelper.hasVotedProjectPublic(projectId,
                                        AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI),
                                "dpVoted", arHelper.hasVotedProjectPublic(projectId,
                                        AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP),
                                "status", userRequest.getStatus(),
                                "projectId", userRequest.getProjectId(),
                                "project", userRequest.getProjectName(),
                                "pi", userRequest.getPiName(),
                                "dp", userRequest.getDpName(),
                                "requestorName", requestorName,
                                "role", role,
                                "action", action,
                                "level", userRequest.getLevel(),
                                "myLevel", level,
                                "reason", reason,
                                "rowId", rowId,
                                "buttonTitle", buttonTitle,
                                "isButtonVisible", isButtonVisible,
                                "currentTab", currentTab,
                                "myRequestsCount",
                                    activityCountMap.get(
                                            AccessRequestStatusItem.STATE_COUNT_MY_REQUESTS),
                                "notReviewedCount",
                                    activityCountMap.get(
                                            AccessRequestStatusItem.STATE_COUNT_NOT_REVIEWED),
                                "recentCount",
                                  activityCountMap.get(
                                          AccessRequestStatusItem.STATE_COUNT_RECENT_ACTIVITY),
                                "totalCount",
                                    activityCountMap.get(AccessRequestStatusItem.STATE_COUNT_TOTAL),
                                "lastActivityDate", activityDateString));
            } catch (IOException e) {
                logger.error("denyProjectPublic:: "
                    + "IOException occurred while attempting to write success response.", e);
            } catch (JSONException e) {
                logger.error("denyProjectPublic:: "
                    + "JSONException occurred while attempting to write success response.", e);
            }
        } else { // Failed to retrieve the status item or request info
            try {
                writeJSON(resp, json("msg", "error", "cause",
                        "Your response cannot be saved."
                                + " Please refresh the page and try again."));
            } catch (IOException e) {
                logger.error("denyProjectPublic:: "
                    + "IOException occurred while attempting to write error response.", e);
            } catch (JSONException e) {
                logger.error("denyProjectPublic:: "
                    + "JSONException occurred while attempting to write error response.", e);
            }
        }
        return;
    }


    /**
     * Returns a string with (&,<,>,") replaced by entity references.
     * @param input the input
     * @return a string with (&,<,>,") replaced by entity references
     */
    String cleanText(String input) {

        if (input != null) {

            String cleanText = input.replaceAll("&", "&amp;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("<", "&lt;");
            return cleanText;
        } else {
            return "";
        }
    }

    /**
     * Returns a 255 max character string or an empty string if input string is null.
     * @param input the input string
     * @return a 255 max character string or an empty string if input string is null
     */
    String limitText(String input) {
        if (input != null) {
            Integer charLimit = input.length() > MAX_CHAR_LEN
                ? MAX_CHARS_REASON : input.length();
            return input.substring(0, charLimit);
        } else {
            return "";
        }
    }

}
