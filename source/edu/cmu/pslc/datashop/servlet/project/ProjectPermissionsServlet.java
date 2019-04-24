/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;

/**
 * This servlet is for displaying a Project's Permissions.
 *
 * @author Cindy Tipper
 * @version $Revision: 10884 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-04-08 15:31:45 -0400 (Tue, 08 Apr 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectPermissionsServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/jsp_project/project_permissions.jsp";

    /** The JSP name for the page shown to admins. */
    public static final String JSP_ADMIN_NAME = "/jsp_project/project_permissions_admin.jsp";

    /** The JSP name for the project_page servlet. */
    public static final String PROJECT_PAGE_JSP_NAME = "/jsp_project/project_page.jsp";

    /** Title for this page. */
    public static final String SERVLET_TITLE = "Project Permissions";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ProjectPermissions";

    /** Constant for the servlet. */
    public static final String SERVLET = "ProjectPermissions";

    /** Constant for the request attribute. */
    public static final String PROJECT_PERMISSIONS_ATTR = "projectPermissions_";

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
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
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        HttpSession httpSession = req.getSession(true);

        httpSession.setAttribute(ProjectPageServlet.PROJECT_TAB_ATTRIB, "permissions");

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {

            String jspName = JSP_NAME;
            Integer projectId = getIntegerId((String)req.getParameter("id"));

            // If user not logged in, go to main project page.
            if (getLoggedInUserItem(req) == null) {
                if (projectId != null) {
                    httpSession.setAttribute(ProjectPageServlet.PROJECT_ID_ATTRIB, projectId);
                }
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(PROJECT_PAGE_JSP_NAME);
                disp.forward(req, resp);
                return;
            }

            if (projectId != null) {

                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                ProjectItem projectItem = projectDao.get(projectId);
                if (projectItem != null) {
                    httpSession.setAttribute(ProjectPageServlet.PROJECT_ID_ATTRIB, projectId);

                    if (getParameter(req, "currentTab") != null) {
                        setCurrentTab(req, getParameter(req, "currentTab"));
                        return;
                    }

                    UserItem userItem = getUser(req);
                    if (userItem == null) {
                        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                        userItem = userDao.findOrCreateDefaultUser();
                    }

                    String logInfoStr = getProjectLogInfoStr(projectItem);
                    UserLogger.log(userItem,
                                   UserLogger.VIEW_PROJECT_PERMISSIONS, logInfoStr, false);

                    // If admin, retrieve further information, and use different JSP page
                    if (isUserAuthorized(req, projectId)) {

                        if (getParameter(req, "sortBy") != null) {
                            setSortParam(req, getParameter(req, "sortBy"));
                        }
                        if (getParameter(req, "searchBy") != null) {
                            setSearchParam(req, getParameter(req, "searchBy"));
                        }
                        if (getParameter(req, "rowsPerPage") != null) {
                            setRowsPerPageParam(req, getParameter(req, "rowsPerPage"));
                        }
                        if (getParameter(req, "currentPage") != null) {
                            setCurrentPageParam(req, getParameter(req, "currentPage"));
                        }
                        if (getParameter(req, "showAdmins") != null) {
                            setShowAdminsParam(req, getParameter(req, "showAdmins"));
                        }

                        ProjectPermissionsContext permContext =
                            ProjectPermissionsContext.getContext(req);

                        ProjectPermissionsHelper projPermHelper =
                            HelperFactory.DEFAULT.getProjectPermissionsHelper();
                        List<ProjectRequestDTO> notReviewed =
                            projPermHelper.getRequestsForAccess(projectItem, permContext, userItem);

                        List<Integer> projectIdList = new ArrayList<Integer>(1);
                        projectIdList.add(projectId);

                        List<ProjectRequestDTO> accessReport =
                            projPermHelper.getAccessReport(projectIdList, permContext);
                        int numArRecords =
                            projPermHelper.getAccessReportCount(projectIdList, permContext);

                        List<ProjectRequestDTO> currentPermissions =
                            projPermHelper.getCurrentPermissions(projectIdList, permContext);
                        int numCpRecords =
                            projPermHelper.getCurrentPermissionsCount(projectIdList, permContext);

                        ProjectPermissionsDto dto = new ProjectPermissionsDto(notReviewed,
                                                                              currentPermissions,
                                                                              accessReport);

                        dto.setAccessReportNumRecords(numArRecords);
                        int rowsPerPage = permContext.getAccessReportRowsPerPage();
                        dto.setAccessReportNumPages(calcNumPages(numArRecords, rowsPerPage));
                        rowsPerPage = permContext.getCurrentPermissionsRowsPerPage();
                        dto.setCurrentPermissionsNumRecords(numCpRecords);
                        dto.setCurrentPermissionsNumPages(calcNumPages(numCpRecords, rowsPerPage));
                        dto.setHasTermsOfUse(projPermHelper.
                                             getHasTermsOfUse(projectIdList, permContext));

                        // Get total number of records as filtering may "falsely" return 0.
                        // Do this by querying for count with a context with no filtering.
                        ProjectPermissionsContext emptyContext = new ProjectPermissionsContext();
                        emptyContext.setShowAdmins(true);

                        int numArRecordsTotal =
                            projPermHelper.getAccessReportCount(projectIdList, emptyContext);
                        dto.setAccessReportNumRecordsTotal(numArRecordsTotal);
                        int numCpRecordsTotal =
                            projPermHelper.getCurrentPermissionsCount(projectIdList, emptyContext);
                        dto.setCurrentPermissionsNumRecordsTotal(numCpRecordsTotal);

                        req.setAttribute(PROJECT_PERMISSIONS_ATTR + projectId, dto);

                        if (req.getParameter("addNewUserAccess") != null) {
                            addNewUserAccess(req, resp, projectItem, dto);
                            return;
                        }
                        if (getParameter(req, "updateSession") != null) {
                            updateSession(req, resp, projectItem, dto);
                            return;
                        }

                        jspName = ProjectPermissionsServlet.JSP_ADMIN_NAME;
                    }

                } else {
                    jspName = ProjectServlet.SERVLET_NAME;
                }
            } else {
                logger.info("Project id is null, going to home page.");
                jspName = ProjectServlet.SERVLET_NAME;
            }

            logger.info("Going to JSP: " + jspName);
            RequestDispatcher disp = getServletContext().getRequestDispatcher(jspName);
            disp.forward(req, resp);

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /**
     * Sets the sort parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param sortByParam the column to sort the table by
     */
    private void setSortParam(HttpServletRequest req, String sortByParam) {
        if (getParameter(req, "sortRequestsForAccess") != null) {
            setRfaSortByColumn(req, sortByParam);
        } else if (getParameter(req, "sortAccessReport") != null) {
            setArSortByColumn(req, sortByParam);
        } else if (getParameter(req, "sortCurrentPermissions") != null) {
            setCpSortByColumn(req, sortByParam);
        } else {
            logger.warn("Permissions: The action parameter is unexpectedly null.");
        }
    }

    /**
     * Sets the search parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param searchParam the user to search by
     */
    private void setSearchParam(HttpServletRequest req, String searchParam) {
        if (getParameter(req, "searchAccessReport") != null) {
            setArSearch(req, searchParam);
        } else if (getParameter(req, "searchCurrentPermissions") != null) {
            setCpSearch(req, searchParam);
        } else {
            logger.warn("Permissions: The action parameter is unexpectedly null.");
        }
    }

    /**
     * Sets the rows-per-page parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param rowsPerPageParam the number of rows per page displayed
     */
    private void setRowsPerPageParam(HttpServletRequest req, String rowsPerPageParam) {
        int rowsPerPage =
            parseInt(rowsPerPageParam, ProjectPermissionsContext.DEFAULT_ROWS_PER_PAGE);
        if (getParameter(req, "rowsPerPageAccessReport") != null) {
            setArRowsPerPage(req, rowsPerPage);
        } else if (getParameter(req, "rowsPerPageCurrentPermissions") != null) {
            setCpRowsPerPage(req, rowsPerPage);
        } else {
            logger.warn("Permissions: The action parameter is unexpectedly null.");
        }
    }

    /**
     * Sets the current-page parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param currentPageParam the current page to be displayed
     */
    private void setCurrentPageParam(HttpServletRequest req, String currentPageParam) {
        int currentPage =
            parseInt(currentPageParam, ProjectPermissionsContext.DEFAULT_CURRENT_PAGE);
        if (getParameter(req, "currentPageAccessReport") != null) {
            setArCurrentPage(req, currentPage);
        } else if (getParameter(req, "currentPageCurrentPermissions") != null) {
            setCpCurrentPage(req, currentPage);
        } else {
            logger.warn("Permissions: The action parameter is unexpectedly null.");
        }
    }

    /**
     * Sets the showAdmins parameter in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param showAdminsParam flag indicating if admins are included
     */
    private void setShowAdminsParam(HttpServletRequest req, String showAdminsParam) {
        boolean showAdmins = parseBool(showAdminsParam,
                                       ProjectPermissionsContext.DEFAULT_SHOW_ADMINS);
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setShowAdmins(showAdmins);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Requests for Access' sort parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param sortByColumn the column to sort the table by
     */
    private void setRfaSortByColumn(HttpServletRequest req, String sortByColumn) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setRequestsForAccessSortByColumn(sortByColumn, true);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the current tab in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param currentTab the name of the current tab
     */
    private void setCurrentTab(HttpServletRequest req, String currentTab) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setCurrentTab(currentTab);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Access Report' sort parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param sortByColumn the column to sort the table by
     */
    private void setArSortByColumn(HttpServletRequest req, String sortByColumn) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setAccessReportSortByColumn(sortByColumn, true);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Current Permissions' sort parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param sortByColumn the column to sort the table by
     */
    private void setCpSortByColumn(HttpServletRequest req, String sortByColumn) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setCurrentPermissionsSortByColumn(sortByColumn, true);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Access Report' search parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param searchBy the search to filter table
     */
    private void setArSearch(HttpServletRequest req, String searchBy) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setAccessReportSearchBy(searchBy);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Current Permissions' search parameters in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param searchBy the search to filter table
     */
    private void setCpSearch(HttpServletRequest req, String searchBy) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setCurrentPermissionsSearchBy(searchBy);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Access Report' rows-per-page parameter in the ProjectPermissionsContext.
     * This has the side-effect of resetting the currentPage to 1.
     * @param req {@link HttpServletRequest}
     * @param rowsPerPage the number of rows per page
     */
    private void setArRowsPerPage(HttpServletRequest req, int rowsPerPage) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setAccessReportRowsPerPage(rowsPerPage);
        context.setAccessReportCurrentPage(ProjectPermissionsContext.DEFAULT_CURRENT_PAGE);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Current Permissions' rows-per-page parameter in the ProjectPermissionsContext.
     * This has the side-effect of resetting the currentPage to 1.
     * @param req {@link HttpServletRequest}
     * @param rowsPerPage the number of rows per page
     */
    private void setCpRowsPerPage(HttpServletRequest req, int rowsPerPage) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setCurrentPermissionsRowsPerPage(rowsPerPage);
        context.setCurrentPermissionsCurrentPage(ProjectPermissionsContext.DEFAULT_CURRENT_PAGE);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Access Report' current-page parameter in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param currentPage the number of the current page
     */
    private void setArCurrentPage(HttpServletRequest req, int currentPage) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setAccessReportCurrentPage(currentPage);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Sets the 'Current Permissions' current-page parameter in the ProjectPermissionsContext.
     * @param req {@link HttpServletRequest}
     * @param currentPage the number of the current page
     */
    private void setCpCurrentPage(HttpServletRequest req, int currentPage) {
        ProjectPermissionsContext context = ProjectPermissionsContext.getContext(req);
        context.setCurrentPermissionsCurrentPage(currentPage);
        ProjectPermissionsContext.setContext(req, context);
    }

    /**
     * Helper method for adding a new user authorization level.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectItem the project
     * @param dto the ProjectPermissionsDto
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void addNewUserAccess(HttpServletRequest req, HttpServletResponse resp,
                                  ProjectItem projectItem, ProjectPermissionsDto dto)
        throws IOException, ServletException {

        String message;
        String messageLevel;

        String newUser = (String)req.getParameter("newUser");
        String authLevel = (String)req.getParameter("authLevel");

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(newUser);

        if (userItem == null) {
            message = "Specified user does not exist.";
            messageLevel = ProjectPermissionsDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (userItem.getId().equals(UserItem.DEFAULT_USER)
                && authLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
            message = "User '" + userItem.getId() + "' cannot be given '"
                    + authLevel + "' access to a project.";
            messageLevel = ProjectPermissionsDto.STATUS_MESSAGE_LEVEL_WARN;
        } else {
            ProjectPermissionsHelper projPermHelper =
                HelperFactory.DEFAULT.getProjectPermissionsHelper();
            projPermHelper.addNewUserAccess(projectItem, userItem, authLevel);

            String logInfoStr = "User '" + newUser + "' given '" + authLevel
                + "' access to Project '" + projectItem.getProjectName() + "'";
            UserLogger.log(getUser(req), UserLogger.ADD_NEW_USER_ACCESS, logInfoStr, false);

            message = "New user successfully added.";
            messageLevel = ProjectPermissionsDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        dto.setMessage(message);
        dto.setMessageLevel(messageLevel);
        redirectToProjectPermissionsPage((Integer)projectItem.getId(), dto, req, resp);
    }

    /**
     * Helper method for updating the session state needed by JSPs.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectItem the project
     * @param dto the ProjectPermissionsDto
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void updateSession(HttpServletRequest req, HttpServletResponse resp,
                               ProjectItem projectItem, ProjectPermissionsDto dto)
        throws IOException, ServletException {

        String message = (String)req.getParameter("message");
        String messageLevel = (String)req.getParameter("messageLevel");

        dto.setMessage(message);
        dto.setMessageLevel(messageLevel);
        redirectToProjectPermissionsPage((Integer)projectItem.getId(), dto, req, resp);
    }

    /**
     * Helper method for redirecting to the ProjectPermissions page.
     * @param projectId the project id
     * @param dto the ProjectPermissionsDto
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToProjectPermissionsPage(Integer projectId, ProjectPermissionsDto dto,
                                                  HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(PROJECT_PERMISSIONS_ATTR + projectId, dto);

        resp.sendRedirect(SERVLET + "?id=" + projectId);
    }

    /**
     * Helper method for generating user log info for specific project.
     * @param projectItem the project
     * @return String the log info
     */
    private String getProjectLogInfoStr(ProjectItem projectItem) {
        return "Project: '" + projectItem.getProjectName() + "' (" + projectItem.getId() + ")";
    }

    /**
     * Helper method to determine if logged in user is authorized to view
     * the Project Permissions JSP.
     * @param req {@link HttpServletRequest}
     * @param projectId the relevant project
     * @return boolean flag
     */
    private boolean isUserAuthorized(HttpServletRequest req, Integer projectId) {
        UserItem user = getUser(req);

        if (user.getAdminFlag()) {
            return true;
        }

        ProjectPageHelper projectPageHelper = HelperFactory.DEFAULT.getProjectPageHelper();

        String authLevel = projectPageHelper.getAuthLevel((String)user.getId(), projectId);
        if ((authLevel != null) && (authLevel.equals(AuthorizationItem.LEVEL_ADMIN))) {
            return true;
        }

        return false;
    }

    /**
     * Helper method to parse Integer, using default if an error is encountered.
     * @param numStr the String to parse
     * @param defVal the default value
     * @return int
     */
    private int parseInt(String numStr, int defVal) {
        int retval = defVal;
        if (numStr != null) {
            try {
                retval = Integer.parseInt(numStr);
            } catch (NumberFormatException nfe) {
                // ignore error; return the default value
                retval = defVal;
            }
        }
        return retval;
    }

    /**
     * Helper method to parse Boolean, using default if an error is encountered.
     * @param numStr the String to parse
     * @param defVal the default value
     * @return boolean the result
     */
    private boolean parseBool(String numStr, boolean defVal) {
        boolean retval = defVal;
        if (numStr != null) {
            retval = Boolean.valueOf(numStr);
        }
        return retval;
    }

    /**
     * Helper method to calculate total number of pages.
     * @param numRecords number of total records
     * @param entriesPerPage the number of items per page
     * @return int the offset
     */
    private int calcNumPages(int numRecords, int entriesPerPage) {
        return (int)Math.ceil((float)numRecords / (float)entriesPerPage);
    }
}
