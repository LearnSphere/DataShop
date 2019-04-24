/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueServlet;

/**
 * Handle uploading a dataset.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CreateProjectServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP file name. */
    private static final String JSP_NAME = "/jsp_project/create_project.jsp";

    /** Title for this page. */
    public static final String SERVLET_TITLE = "Create a project";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "CreateProject";

    /** Request attribute name. */
    public static final String REQ_ATTRIB_SETTINGS = "form_settings";

    /** String constant. */
    public static final String MSG_CREATE_PROJECT_ERROR =
            "Error occurred while creating this project.";

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
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);

            // Determine if user is authorized to view this page.
            if ((getLoggedInUserItem(req) == null) || !isUserAuthorized(req)) {
                req.getSession().setAttribute(ProjectServlet.OPEN_ROLE_REQUEST_ATTRIB,
                                              ProjectServlet.CREATE_PROJECT_REDIRECT);
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
                disp.forward(req, resp);
                return;
            }

            // Set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // Get current user item
            UserItem userItem = getUser(req);

            // DTO to hold data in form on UI
            CreateProjectDto dto = new CreateProjectDto();

            // check for hidden field
            String action = getParameter(req, "action");
            if (action != null && action.equals("create")) {
                // Get form data
                dto = getFormData(req);
                if (!dto.getErrorFlag()) {
                    if (!isProjectNameUnique(dto.getNewProjectName())) {
                        dto.setErrorFlag(true);
                        dto.setErrorMessage(ImportQueueServlet.MSG_PROJECT_NAME_TAKEN);
                    } else {
                        ProjectItem projectItem = createProject(userItem,
                                dto.getNewProjectName(), dto.getDataCollectionType());
                        if (projectItem != null) {
                            Integer newId = (Integer)projectItem.getId();
                            String redirectTo = ProjectPageServlet.SERVLET + "?id=" + newId;
                            logger.info("Redirecting to " + redirectTo);
                            resp.sendRedirect(redirectTo);
                            return;
                        } else {
                            String msg = MSG_CREATE_PROJECT_ERROR;
                            dto.setErrorFlag(true);
                            dto.setErrorMessage(msg);
                            logger.error(msg);
                        }
                    }
                }
            }

            UserLogger.log(userItem, UserLogger.VIEW_CREATE_PROJECT, "", true);

            // Put the data in the HTTP session for the JSP.
            req.setAttribute(REQ_ATTRIB_SETTINGS, dto);

            // forward to the JSP (view)
            RequestDispatcher disp;
            disp = getServletContext().getRequestDispatcher(JSP_NAME);
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
     * Check if the project name is unique before creating a new one.
     * @param projectName the proposed new project's name
     * @return true if it is unique and false if the name has been used already
     */
    public static Boolean isProjectNameUnique(String projectName) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        Collection projectList = projectDao.find(projectName);
        if (projectList.size() > 0) {
            return false;
        }
        return true;
    }
    /**
     * Create the project, assumes name has been checked for uniqueness.
     * @param userItem the current user
     * @param projectName the new project name
     * @param dataCollectionType the data collection type
     * @return the project item if created, null otherwise
     */
    public static ProjectItem createProject(UserItem userItem, String projectName,
            String dataCollectionType) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        Date now = new Date();

        ProjectItem projectItem = new ProjectItem();
        projectItem.setProjectName(projectName);
        projectItem.setDataCollectionType(dataCollectionType);
        projectItem.setCreatedBy(userItem);
        projectItem.setCreatedTime(now);

        // Trac 314: make the user the PI.
        projectItem.setPrimaryInvestigator(userItem);

        projectDao.saveOrUpdate(projectItem);

        Collection projectList = projectDao.find(projectName);
        if (projectList.size() > 0) {
            projectItem = (ProjectItem)(projectList.toArray())[0];

            //Make the creator of a project a Project Admin automatically
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            AuthorizationItem authItem =
                    new AuthorizationItem(userItem, projectItem);
            authItem.setLevel(AuthorizationItem.LEVEL_ADMIN);
            authDao.saveOrUpdate(authItem);

            //Log to dataset user log table
            String info = "Project '" + projectItem.getProjectName()
                    + "' (" + projectItem.getId() + ")";
            UserLogger.log(null, userItem, UserLogger.PROJECT_CREATE, info);
        } else {
            return null;
        }
        return projectItem;
    }

    /** Constant. */
    private static final int PROJECT_NAME_MAX_LEN = 255;

    /**
     * Reads the form data and sets fields in the DTO.
     * @param req {@link HttpServletRequest}
     * @return a create project DTO to send to the JSP
     */
    private CreateProjectDto getFormData(HttpServletRequest req) {
        CreateProjectDto dto = new CreateProjectDto();
        String projectName = getParameter(req, "new_project_name");
        if (projectName != null && projectName.length() > 0) {
            // Make note of value specified, even if in error.
            dto.setNewProjectName(projectName);
            if (projectName.length() > PROJECT_NAME_MAX_LEN) {
                dto.setErrorFlag(true);
                dto.setErrorMessage("Maximum length for a project name is 255.");
            }
        } else {
            dto.setErrorFlag(true);
            dto.setErrorMessage("Please enter a project name.");
        }

        String dct = getParameter(req, "dataCollectionType");
        if (dct != null) {
            dto.setDataCollectionType(dct);
        } else  {
            //set default
            dto.setDataCollectionType(ProjectItem.DATA_COLLECTION_TYPE_NOT_SPECIFIED);
        }

        return dto;
    }

    /**
     * Helper method to determine if logged in user is authorized to upload datasets.
     * @param req {@link HttpServletRequest}
     * @return boolean flag
     */
    private boolean isUserAuthorized(HttpServletRequest req) {
        UserItem user = getUser(req);

        if (user.getAdminFlag()) {
            return true;
        }

        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        return userRoleDao.hasDatashopEditRole(user);
    }
}
