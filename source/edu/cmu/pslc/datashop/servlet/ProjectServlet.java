/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;

import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.UserItem;

 /**
 * This servlet takes and groups all curriculums under their respective problems,
 * and displays the curriculums for each.
 *
 * @author Benjamin Billings
 * @version $Revision: 12865 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-19 16:01:55 -0500 (Tue, 19 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name for redirect. */
    public static final String REDIRECT_SERVLET_NAME = "index.jsp";
    /** The Servlet name. */
    public static final String SERVLET_NAME = "/index.jsp";

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/project.jsp";

    /** Session Parameter. */
    public static final String OPEN_ROLE_REQUEST_ATTRIB = "OPEN_ROLE_REQUEST";
    /** Value when redirect issued from 'Upload Dataset'. */
    public static final String UPLOAD_DATASET_REDIRECT = "UploadDataset";
    /** Value when redirect issued from 'Create Project'. */
    public static final String CREATE_PROJECT_REDIRECT = "CreateProject";

    /**
     * Handles the HTTP get.
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
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logger.debug("doPost begin");
        HttpSession httpSession = req.getSession(true);
        try {
            setEncoding(req, resp);

            // reset the most recent servlet name for the help page
            setRecentReport(req.getSession(true), null);

            if (!redirectToSecureUrl(req, resp)) {
                checkLogin(httpSession, req);
                // Set the highlighted major tab to Mine if user is logged in
                req.getSession().setAttribute("datasets", ProjectHelper.DATASETS_MINE);
                selectMajorTab(req, resp);

                ImportQueueHelper iqHelper = HelperFactory.DEFAULT.getImportQueueHelper();
                iqHelper.handleCancelActions(req, resp, getUser(req));

                // forward to the JSP (view)
                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(JSP_NAME);
                disp.forward(req, resp);
                return;
            }

			// selectMajorTab should be called after the redirectToSecureUrl conditional
			// to prevent it from being called twice
            selectMajorTab(req, resp);

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    } //end doPost()

    /**
     * Sets the appropriate session values based on the user-selected major tab.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     */
    private void selectMajorTab(HttpServletRequest req, HttpServletResponse resp) {
     // Determine which datasets group was selected (Mine, Public, or Other)
        // and return the appropriate Dataset content as an html string
        String datasetsPage = "";
        // Get the user Item
        String userId = UserItem.DEFAULT_USER;
        UserItem userItem = getLoggedInUserItem(req);
        Boolean isDataShopAdmin = false;
        if (userItem != null) {
            userId = (String) userItem.getId();
            isDataShopAdmin = userItem.getAdminFlag();
        }
        // Get the project helper
        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();

        String defaultDataset = "";
        // Set default major tab based on the user item
        if (!userId.equals(UserItem.DEFAULT_USER)) {
            defaultDataset = ProjectHelper.DATASETS_MINE;
        } else {
            defaultDataset = ProjectHelper.DATASETS_PUBLIC;
        }

        if (!userId.equals(UserItem.DEFAULT_USER)) {
            // User is a member and datasets tab is selected
            if (req.getParameter("datasets") != null) {
                String selectedDataset = req.getParameter("datasets");

                // Make sure the datasets tab is an expected value
                if (selectedDataset.equals(ProjectHelper.DATASETS_MINE)
                        || selectedDataset.equals(ProjectHelper.DATASETS_PUBLIC)
                        || selectedDataset.equals(ProjectHelper.DATASETS_OTHER)) {
                    defaultDataset = selectedDataset;
                }
            }
        } else {
            // User is not a member
            if (req.getParameter("datasets") != null) {
                String selectedDataset = req.getParameter("datasets");
                // Make sure the datasets tab is an expected value
                if (selectedDataset.equals(ProjectHelper.DATASETS_PUBLIC)
                        || selectedDataset.equals(ProjectHelper.DATASETS_OTHER)) {
                    defaultDataset = selectedDataset;
                }
            }
        }
        // For public user, %
        if (userId.equals(UserItem.DEFAULT_USER)) {
            if (defaultDataset.equals(ProjectHelper.DATASETS_PUBLIC)) {
                // My datasets
                datasetsPage += "<h1 id=\"datasets-header\">Public Datasets</h1>";
                datasetsPage += projHelper.getMyDatasets(userId, false);
            } else if (defaultDataset.equals(ProjectHelper.DATASETS_OTHER)) {
                // Other datasets
                datasetsPage += "<h1 id=\"datasets-header\">Private Datasets</h1>";
                datasetsPage += projHelper.getAvailableDatasets(userId, isDataShopAdmin);
            }
        } else {
            // For registered users
            if (defaultDataset.equals(ProjectHelper.DATASETS_MINE)) {
                // My datasets
                datasetsPage += "<h1 id=\"datasets-header\">My Datasets</h1>";
                datasetsPage += projHelper.getMyDatasets(userId, isDataShopAdmin);

            } else if (defaultDataset.equals(ProjectHelper.DATASETS_OTHER)) {
                // Other datasets
                datasetsPage += "<h1 id=\"datasets-header\">Private Datasets</h1>";
                datasetsPage += projHelper.getAvailableDatasets(userId, isDataShopAdmin);

            } else if (defaultDataset.equals(ProjectHelper.DATASETS_PUBLIC)) {
                // Public datasets
                defaultDataset = ProjectHelper.DATASETS_PUBLIC;
                datasetsPage += "<h1 id=\"datasets-header\">Public Datasets</h1>";
                datasetsPage += projHelper.getPublicDatasets(userId, isDataShopAdmin);

            }
        }
        // Now return the appropriate Dataset content as html

        // Set the session attributes
        req.getSession().setAttribute("datasets", defaultDataset);
        req.getSession().setAttribute("datasetsPage", datasetsPage);
    }
} // end class ProjectServlet
