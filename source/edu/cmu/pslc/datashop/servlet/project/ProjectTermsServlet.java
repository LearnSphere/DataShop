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
import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.tou.ManageTermsHelper;

/**
 * This servlet is for displaying a Project's Terms of Use.
 *
 * @author Cindy Tipper
 * @version $Revision: 10554 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-02-13 13:22:41 -0500 (Thu, 13 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectTermsServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/jsp_project/project_terms.jsp";

    /** Title for this page. */
    public static final String SERVLET_TITLE = "Project Terms";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ProjectTerms";

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

        httpSession.setAttribute(ProjectPageServlet.PROJECT_TAB_ATTRIB, "terms");

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {

            String jspName = JSP_NAME;
            Integer projectId = getIntegerId((String)req.getParameter("id"));

            if (projectId != null) {

                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                ProjectItem projectItem = projectDao.get(projectId);
                if (projectItem != null) {
                    httpSession.setAttribute(ProjectPageServlet.PROJECT_ID_ATTRIB, projectId);
                    UserItem userItem = getUser(req);
                    if (userItem == null) {
                        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                        userItem = userDao.findOrCreateDefaultUser();
                    }

                    ManageTermsHelper mtHelper = HelperFactory.DEFAULT.getManageTermsHelper();
                    List<ProjectItem> projectList = new ArrayList<ProjectItem>();
                    projectList.add(projectItem);
                    if (req.getParameter("applyTerms") != null) {
                        Integer touId = getIntegerId((String)req.getParameter("tou"));

                        TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
                        TermsOfUseItem touItem = touDao.get(touId);

                        mtHelper.updateAppliedTerms(userItem, projectList, touItem);
                        redirectToProjectTermsPage(projectId, req, resp);
                        return;
                    } else if (req.getParameter("clearTerms") != null) {
                        mtHelper.updateAppliedTerms(userItem, projectList, null);
                        redirectToProjectTermsPage(projectId, req, resp);
                        return;
                    }

                    String logInfoStr = getProjectLogInfoStr(projectItem);
                    UserLogger.log(userItem, UserLogger.VIEW_PROJECT_TERMS, logInfoStr, false);
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
     * Helper method for generating user log info for specific project.
     * @param projectItem the project
     * @return String the log info
     */
    private String getProjectLogInfoStr(ProjectItem projectItem) {
        return "Project: '" + projectItem.getProjectName() + "' (" + projectItem.getId() + ")";
    }

    /**
     * Helper method for redirecting to the Project Terms page.
     * @param projectId the project id
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToProjectTermsPage(Integer projectId,
                                            HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        resp.sendRedirect(SERVLET_NAME + "?id=" + projectId);
    }
}
