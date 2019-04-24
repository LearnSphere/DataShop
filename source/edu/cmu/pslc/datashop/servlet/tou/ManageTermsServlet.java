/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.tou;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;

import edu.cmu.pslc.datashop.helper.UserLogger;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.ProjectTermsOfUseHistoryDao;
import edu.cmu.pslc.datashop.dao.ProjectTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;

import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseHistoryItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * This servlet is for managing the Datashop and project-specific terms of use.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10554 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-02-13 13:22:41 -0500 (Thu, 13 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageTermsServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for managing all terms as administrator. */
    public static final String MANAGE_JSP_NAME = "/terms_admin_manage.jsp";
    /** The JSP name for editing DataShop terms. */
    public static final String EDIT_JSP_NAME = "/terms_datashop_edit.jsp";
    /** The JSP name for editing project-specific terms. */
    public static final String EDIT_PROJECT_JSP_NAME = "/terms_project_edit.jsp";
    /** The JSP name for managing project-specific terms as ToU manager. */
    public static final String MANAGE_PROVIDER_JSP_NAME = "/terms_dp_manage.jsp";

    // Primary execution path parameters
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String EDIT_DS_TERMS_PARAM = "edit_ds_terms";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String EDIT_PROJECT_TERMS_PARAM = "edit_project_terms";

    // ToU Management parameters
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String SELECTED_TOU_PARAM = "selectTou";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String CREATE_TERMS_BUTTON_PARAM = "create_terms_button";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String DELETE_TERMS_BUTTON_PARAM = "delete_terms_button";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String RETIRE_TERMS_BUTTON_PARAM = "retire_terms_button";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String EDIT_DS_TERMS_BUTTON_PARAM = "edit_ds_terms_button";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String CREATE_NAME_TEXT_PARAM = "create_name_text";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String TOU_EXISTS_ATTRIB = "termsExistParam";

    // User selection specific parameters
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String SELECTED_VERSION_PARAM = "select_terms";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String TOU_CURRENT_VERSION_ATTRIB = "touCurrentVersion";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String VERSION_ID_PARAM = "version_id";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String NOT_HEAD_ATTRIB = "notHeadVersion";

    // Save parameters
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String PUBLISH_TERMS_PARAM = "publish_terms";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String TERMS_TEXT_PARAM = "terms-text";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String SAVE_TERMS_ATTRIB = "save_terms_button";

    // Project-ToU Management parameters
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String SELECTED_PROVIDER_ATTRIB = "selectedDataProvider";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String PROVIDERS_ATTRIB = "dataProviders";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String SELECTED_PROJECT_IDS_TERMS_PARAMVALUES = "selectedProjectIds";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String PROJECTS_ATTRIB = "projects";

    // Project specific parameters
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String APPLY_TERMS_PARAM = "apply_terms";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String CLEAR_TERMS_PARAM = "clear_terms";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String FILTER_PROJECTS_PARAM = "filter_projects";

    // General parameters (high usage)
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String TOU_ITEM_ATTRIB = "touItem";
    /** HTTP Servlet attribute name used in this class and in JSP. */
    public static final String TOU_LIST_ATTRIB = "touList";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ManageTerms";

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
        logger.debug("doPost begin");

        PrintWriter out = null;
        HttpSession httpSession = req.getSession(true);
        try {
            setEncoding(req, resp);

            UserItem userItem = getLoggedInUserItem(req);
            UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
            boolean hasTermsOfUseManagerRole = userRoleDao.hasTermsManagerRole(userItem);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            if (!userItem.getAdminFlag() && !hasTermsOfUseManagerRole) {
                redirectHome(req, resp);
                return; //must return after redirect home
            }

            // State options for the Terms of Use Management, Administrator display
            boolean isEditDataShopTermsFlag = false;
            boolean isEditProjectTermsFlag = false;
            String editDsTermsParam = req.getParameter(EDIT_DS_TERMS_PARAM);
            String editProjectTermsParam = req.getParameter(EDIT_PROJECT_TERMS_PARAM);

            // Set State options
            if (editDsTermsParam != null) {
                isEditDataShopTermsFlag = true;

            } else if (editProjectTermsParam != null) {
                isEditProjectTermsFlag = true;

            } else {
                httpSession.setAttribute(SELECTED_PROVIDER_ATTRIB, "");
            }

            // If request comes from the terms_manage, terms_edit, or terms_project_edit view
            if ((isEditDataShopTermsFlag || isEditProjectTermsFlag)) {

                // Edit DS Terms
                if (isEditDataShopTermsFlag && userItem.getAdminFlag()) {

                    // If DS ToU do not exist, then they are created by ensureDatashopTermsExist
                    ensureDatashopTermsExist(userItem);

                    // Handle selects, saves, or DS management actions
                    datashopDoPost(req, resp);

                // Edit Project-specific Terms
                } else if (isEditProjectTermsFlag) {
                    // Handle selects, saves, or terms management actions
                    projectDoPost(req, resp);
                }

            } else {
                // There are no requests so take the administrator to ToU management view.

                if (userItem.getAdminFlag()) {
                    // Display ToU Management panel (Administrator)
                    TermsOfUseVersionDao touVersionDao =
                        DaoFactory.DEFAULT.getTermsOfUseVersionDao();
                    TermsOfUseVersionItem touVersionItem
                        = touVersionDao.findAppliedVersion(TermsOfUseItem.DATASHOP_TERMS);
                    req.setAttribute(TOU_CURRENT_VERSION_ATTRIB, touVersionItem);

                    // forward to the terms_manage view
                    UserLogger.log(null, userItem, UserLogger.VIEW_MANAGE_TERMS, "", true);
                    logger.info("Going to JSP: " + MANAGE_JSP_NAME);
                    RequestDispatcher disp;
                    disp = getServletContext().getRequestDispatcher(MANAGE_JSP_NAME);
                    disp.forward(req, resp);

                } else if (hasTermsOfUseManagerRole) {
                    // Display ToU Management panel (Provider)
                    UserLogger.log(null, userItem, UserLogger.VIEW_MANAGE_TERMS, "", true);
                    logger.info("Going to JSP: " + MANAGE_PROVIDER_JSP_NAME);
                    RequestDispatcher disp;
                    disp = getServletContext().getRequestDispatcher(MANAGE_PROVIDER_JSP_NAME);
                    disp.forward(req, resp);
                }
            }

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
     * Handles requests from administrator to create and edit Datashop terms of use.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void datashopDoPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // Sub-state options for the Terms of Use Management, Administrator display
        boolean isSelectTermsFlag = false;
        boolean isPublishTermsFlag = false;
        // Setup Sub-state options
        String selectTermsParam = req.getParameter(SELECTED_VERSION_PARAM);
        String publishTermsParam = req.getParameter(PUBLISH_TERMS_PARAM);
        if (selectTermsParam != null) {
            isSelectTermsFlag = true;
        } else if (publishTermsParam != null) {
            isPublishTermsFlag = true;
        }

        // Selected ToU version and terms text
        String versionIdParam = req.getParameter(VERSION_ID_PARAM);
        String termsText = req.getParameter(TERMS_TEXT_PARAM);

        UserItem userItem = getLoggedInUserItem(req);

        // Setup Dao's
        TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        TermsOfUseVersionDao touVersionDao
            = DaoFactory.DEFAULT.getTermsOfUseVersionDao();

        // Get a list of archived DS ToU versions (none should hold a status of saved)
        TermsOfUseItem dsTerms = touDao.find(TermsOfUseItem.DATASHOP_TERMS);

        List<TermsOfUseVersionItem> touVersionList
            = (List<TermsOfUseVersionItem>) touVersionDao
                    .findVersionsByTermsAndStatus(dsTerms,
                    TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED);

        // Get the applied DS ToU version for later use (there should be 1 or none)
        TermsOfUseVersionItem appliedVersionItem
            = (TermsOfUseVersionItem)(
                    touVersionDao.findAppliedVersion(TermsOfUseItem.DATASHOP_TERMS));

        TermsOfUseVersionItem headVersionItem
            = (TermsOfUseVersionItem)(
                touVersionDao.findLastVersion(TermsOfUseItem.DATASHOP_TERMS));

        TermsOfUseItem touItem = touDao.find(TermsOfUseItem.DATASHOP_TERMS);
        req.setAttribute(TOU_ITEM_ATTRIB, touItem);

        // User select DS ToU from right-hand menu of the DS Edit Terms view
        if (isSelectTermsFlag) {
            TermsOfUseVersionItem touVersionItem = null;

            // If version selected is an integer, then get the selected version
            if (versionIdParam != null && versionIdParam.matches("[0-9]+")) {
                Integer versionId = Integer.parseInt(versionIdParam);
                touVersionItem = touVersionDao.get(versionId);
            }

            // If it exists, set the current display to the selected version
            if (touVersionItem != null) {
                req.setAttribute(TOU_CURRENT_VERSION_ATTRIB, touVersionItem);

                // Whether or not this item is the head version
                if (!touVersionItem.getId().equals(headVersionItem.getId())) {
                    req.setAttribute(NOT_HEAD_ATTRIB, "yes");
                }

                // Add the applied Datashop ToU to the end of the list of archived ToU
                if (appliedVersionItem != null) {
                    touVersionList.add(0, appliedVersionItem);
                }
            }
            // Refresh and add Archived versions
            if (!touVersionList.isEmpty()) {
                req.setAttribute(TOU_LIST_ATTRIB, touVersionList);
            }

        }  else if (isPublishTermsFlag
                && termsText != null && !termsText.trim().equals("")) {
            // If isPublishTermsFlag, then mark current DataShop ToU
            // as archived and create the new applied DS ToU version
            logger.info("Publishing New Terms and refreshing JSP: " + EDIT_JSP_NAME);

            // Get the head version of the Datashop ToU version
            TermsOfUseVersionItem headItem
                = touVersionDao.findLastVersion(TermsOfUseItem.DATASHOP_TERMS);
            // Get the latest version number or zero
            Integer lastVersion
                = (headItem == null) ? 0 : (Integer)(headItem.getVersion());

            // Setup the new Datashop ToU for publishing
            TermsOfUseVersionItem newVersionItem = new TermsOfUseVersionItem();
            Date now = (Date) Calendar.getInstance().getTime();
            newVersionItem.setTermsOfUse(touItem);
            newVersionItem.setAppliedDate(now);
            newVersionItem.setSavedDate(now);
            newVersionItem.setStatus(
                    TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED
            );
            newVersionItem.setTerms(termsText.trim());
            // increment lastVersion by 1
            newVersionItem.setVersion(lastVersion + 1);

            // Mark last applied version as archived (if an applied version exists)
            if (appliedVersionItem != null) {
                updateStatusTermsOfUseVersion(
                        userItem, touItem, appliedVersionItem,
                        TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED);
            }

            // Save the new and update the old Datashop ToU version
            touVersionDao.saveOrUpdate(newVersionItem);

            logger.info("Saved Terms of Use Version (" + newVersionItem.getId()
                    + ") with Version Value: " + newVersionItem.getVersion());

            // Get the newest applied version back
            // from database and refresh the archived versions list
            TermsOfUseVersionItem newAppliedVersionItem
                = (TermsOfUseVersionItem)
                    (touVersionDao.findAppliedVersion(TermsOfUseItem.DATASHOP_TERMS));

            touVersionList.clear();
            // Add new head version
            if (newAppliedVersionItem != null) {
                req.setAttribute(TOU_CURRENT_VERSION_ATTRIB, newAppliedVersionItem);
                touVersionList.add(newAppliedVersionItem);
            }
            // Add all archived versions
            touVersionList.addAll((List<TermsOfUseVersionItem>)
                    touVersionDao.findVersionsByTermsAndStatus(dsTerms,
                    TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED));
            if (!touVersionList.isEmpty()) {
                req.setAttribute(TOU_LIST_ATTRIB, touVersionList);
            }

            UserLogger.log(null, userItem, UserLogger.SAVE_TERMS,
                    touItem.getName()
                    + ", version: " + newVersionItem.getVersion(), false);

        } else {
            // This is the default view for editing DS Terms of Use

            // Add applied version to new list
            String viewingVersion;
            if (appliedVersionItem != null) {
                touVersionList.add(0, appliedVersionItem);
                req.setAttribute(TOU_CURRENT_VERSION_ATTRIB, appliedVersionItem);
                viewingVersion = appliedVersionItem.getVersion().toString();
            } else {
                viewingVersion = "null";
            }
            // Add archived versions to new list
            if (!touVersionList.isEmpty()) {
                req.setAttribute(TOU_LIST_ATTRIB, touVersionList);
            }

            // Forward response and request to terms_edit
            UserLogger.log(null, userItem, UserLogger.VIEW_EDIT_TERMS,
                touItem.getName()
                + " terms, version: " + viewingVersion, false);
        }

        logger.info("Going to JSP: " + EDIT_JSP_NAME);
        RequestDispatcher disp;
        disp = getServletContext().getRequestDispatcher(EDIT_JSP_NAME);
        try {
            disp.forward(req, resp);
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    // end of datashopDoPost
    }

    /**
     * Handles requests from administrator to create, delete, retire, edit,
     * and assign project-specific terms of use. Handles requests from the
     * data provider to assign project-specific terms of use for their projects.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void projectDoPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        // Get user and session information
        HttpSession httpSession = req.getSession(true);
        UserItem userItem = getLoggedInUserItem(req);
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        boolean hasTermsOfUseManagerRole = userRoleDao.hasTermsManagerRole(userItem);

        // Whether the user selects a version to view or publishes a version
        boolean isSelectTermsFlag = false;
        boolean isPublishTermsFlag = false;
        String selectTermsParam = req.getParameter(SELECTED_VERSION_PARAM);
        String publishTermsParam = req.getParameter(PUBLISH_TERMS_PARAM);

        // Setup booleans per parameters
        if (selectTermsParam != null) {
            isSelectTermsFlag = true;
        } else if (publishTermsParam != null) {
            isPublishTermsFlag = true;
        }

        // Sub-state options for the Terms of Use Management
        boolean isApplyTermsFlag = false;
        boolean isClearTermsFlag = false;
        boolean isFilteredByDataProvider = false;
        String applyTermsParam = req.getParameter(APPLY_TERMS_PARAM);
        String clearTermsParam = req.getParameter(CLEAR_TERMS_PARAM);
        String filterProjectsParam = req.getParameter(FILTER_PROJECTS_PARAM);
        // Whether the user applies new terms configurations or filters by DP
        if (applyTermsParam != null) {
            isApplyTermsFlag = true;
            if (clearTermsParam != null) {
                isClearTermsFlag = true;
            }
        } else if (filterProjectsParam != null) {
            isFilteredByDataProvider = true;
            httpSession.setAttribute(SELECTED_PROVIDER_ATTRIB, filterProjectsParam);
        }

        // Optional valued parameters
        String touIdParam = req.getParameter(SELECTED_TOU_PARAM);
        String versionIdParam = req.getParameter(VERSION_ID_PARAM);
        String termsText = req.getParameter(TERMS_TEXT_PARAM);

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        TermsOfUseVersionDao touVersionDao =
            DaoFactory.DEFAULT.getTermsOfUseVersionDao();

        // Get a list of data providers and projects
        List<UserItem> dataProviders
            = (List<UserItem>) projectDao.findAllDataProviders();

        List<ProjectItem> projects
            = (List<ProjectItem>) projectDao.findAllSortByName();

        TermsOfUseItem touItem = null;
        TermsOfUseVersionItem headVersionItem;

        // Check for project-specific terms: create, delete, or retire
        String createPressed = req.getParameter(CREATE_TERMS_BUTTON_PARAM);
        String deletePressed = req.getParameter(DELETE_TERMS_BUTTON_PARAM);
        String retirePressed = req.getParameter(RETIRE_TERMS_BUTTON_PARAM);

        // Create, Delete, or Retire Project-Specific Terms
        if (createPressed != null || deletePressed != null || retirePressed != null) {

            // Create Project-Specific Terms
            if  (createPressed != null) {
                String newProjectName = req.getParameter(CREATE_NAME_TEXT_PARAM);

                // Attempt to create the project ToU if a valid name is given
                if (newProjectName != null && !newProjectName.trim().equals("")) {
                    // Store created project ToU in touItem
                    touItem = createProjectTerms(userItem, newProjectName.trim());

                    if (touItem == null) {
                     // If the project ToU creation was unsuccessful
                        TermsOfUseVersionItem appliedVersionItem
                            = (TermsOfUseVersionItem)(touVersionDao.
                                    findAppliedVersion(TermsOfUseItem.DATASHOP_TERMS)
                        );
                        req.setAttribute(TOU_CURRENT_VERSION_ATTRIB, appliedVersionItem);
                        req.setAttribute(TOU_EXISTS_ATTRIB, newProjectName);

                        logger.info("Going to JSP: " + MANAGE_JSP_NAME);
                        RequestDispatcher disp;
                        disp = getServletContext()
                                .getRequestDispatcher(MANAGE_JSP_NAME);
                        try {
                            disp.forward(req, resp);
                        } catch (Exception exception) {
                            forwardError(req, resp, logger, exception);
                        } finally {
                            logger.debug("doPost end");
                        }
                    } else {
                        // If creation successful, go to project-specific terms edit view
                        req.setAttribute(TOU_ITEM_ATTRIB, touItem);   // the new terms
                        req.setAttribute(PROJECTS_ATTRIB, projects);
                        req.setAttribute(PROVIDERS_ATTRIB, dataProviders);

                        UserLogger.log(null, userItem, UserLogger.VIEW_EDIT_TERMS,
                                "Created " + touItem.getName()
                                + " terms, version: n/a", false);
                        logger.info("Going to JSP: " + EDIT_PROJECT_JSP_NAME);
                        RequestDispatcher disp;
                        disp = getServletContext()
                                .getRequestDispatcher(EDIT_PROJECT_JSP_NAME);
                        try {
                            disp.forward(req, resp);
                        } catch (Exception exception) {
                            forwardError(req, resp, logger, exception);
                        } finally {
                            logger.debug("doPost end");
                        }
                    }
                }

            } else if (touIdParam != null) {
                // Retire or delete if touIdParam has value
                TermsOfUseItem touSelectedItem = touDao.get(Integer.parseInt(touIdParam));

                // Delete the Project's Terms of Use (if no versions applied)
                if  (deletePressed != null) {
                    deleteTerms(userItem, touSelectedItem);
                } else if  (retirePressed != null) {
                    // Retire the Project's Terms of Use (if no versions applied)
                    retireTerms(userItem, touSelectedItem);
                }

                // Get current datashop ToU version for management view
                TermsOfUseVersionItem appliedVersionItem
                    = (TermsOfUseVersionItem)(touVersionDao.
                            findAppliedVersion(TermsOfUseItem.DATASHOP_TERMS)
                );
                req.setAttribute(TOU_CURRENT_VERSION_ATTRIB, appliedVersionItem);

                //  Forward our response and request to the Manage Terms page
                logger.info("Going to JSP: " + MANAGE_JSP_NAME);
                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(MANAGE_JSP_NAME);
                try {
                    disp.forward(req, resp);
                } catch (Exception exception) {
                    forwardError(req, resp, logger, exception);
                } finally {
                    logger.debug("doPost end");
                }
            }
        } else if (touIdParam != null && touIdParam.matches("[0-9]+")) {
           // If touIdParam is set, then use the edit view for project-specific ToU

            // Filter the projects by data provider--enforce this if Data Provider
            String dataProviderId = null;
            if (userItem.getAdminFlag()) {
                dataProviderId = (String) httpSession
                        .getAttribute(SELECTED_PROVIDER_ATTRIB);
                if (dataProviderId != null && !dataProviderId.equals("")) {
                    UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                    UserItem dataProvider = userDao.find(dataProviderId);
                    projects.clear();
                    projects.addAll((List<ProjectItem>)projectDao.findByDataProvider(dataProvider));
                }
            } else if (hasTermsOfUseManagerRole) {
                dataProviderId = (String) userItem.getId();
                projects.clear();
                projects.addAll((List<ProjectItem>)projectDao.findByDataProvider(userItem));
            }

            // Set the selected project ToU
            touItem = touDao.get(Integer.parseInt(touIdParam));

            // The item to be displayed
            TermsOfUseVersionItem currentItem = null;

            // Create a generic ToU version List
            List<TermsOfUseVersionItem> touList
                = new ArrayList<TermsOfUseVersionItem>();

            if (isPublishTermsFlag && userItem.getAdminFlag()
                    && termsText != null && !termsText.trim().equals("")) {
             // Publish a new applied project ToU or update a project ToU
                logger.info("Publishing New Terms and refreshing JSP: " + EDIT_JSP_NAME);

                // Save this ToU version and update the history tables accordingly
                saveNewVersion(userItem, touItem, termsText);

                // Get all project ToU versions associated with this ToU
                List<TermsOfUseVersionItem> touNewList
                    = (List<TermsOfUseVersionItem>)
                        touVersionDao.findAllByTermsOfUse(touItem);

                currentItem = touVersionDao.findLastVersion(touItem.getName());

                if (!touNewList.isEmpty()) {
                    touList.clear();
                    touList.addAll(touNewList);
                }

            } else if (isSelectTermsFlag) {
                // From the edit page, the user selects an arbitrary version to view

                // Get the list and the head of this ToU
                headVersionItem = (TermsOfUseVersionItem)
                        (touVersionDao.findLastVersion(touItem.getName()));
                touList = (List<TermsOfUseVersionItem>)
                        touVersionDao.findAllByTermsOfUse(touItem);

                // If the version selected is an integer, then get the selected version
                if (versionIdParam != null && versionIdParam.matches("[0-9]+")) {
                    Integer versionId = Integer.parseInt(versionIdParam);
                    currentItem = touVersionDao.get(versionId);
                }
                if (!currentItem.getId().equals(headVersionItem.getId())) {
                    req.setAttribute(NOT_HEAD_ATTRIB, "yes");
                }

            } else {
                // This is the default edit view for project terms

                // Has user applied new project terms?
                if (isApplyTermsFlag) {
                    ManageTermsHelper mtHelper = HelperFactory.DEFAULT.getManageTermsHelper();
                    String[] selectedProjectIds =
                            req.getParameterValues(SELECTED_PROJECT_IDS_TERMS_PARAMVALUES);
                    List<ProjectItem> selectedProjects =
                            getUserSpecificProjects(userItem, selectedProjectIds);
                    // clear or set ToU for each selected project
                    if (isClearTermsFlag) {
                        mtHelper.updateAppliedTerms(userItem, selectedProjects, null);
                    } else {
                        mtHelper.updateAppliedTerms(userItem, selectedProjects, touItem);
                    }
                }

                // Refresh the versions associated with this ToU
                touList = (List<TermsOfUseVersionItem>)
                        touVersionDao.findAllByTermsOfUse(touItem);

                currentItem = touVersionDao.findLastVersion(touItem.getName());

                // Forward response/request to the edit view
                StringBuffer logAction = new StringBuffer("");
                if (touItem != null) {
                   logAction.append(touItem.getName() + " terms");
                   if (currentItem != null) {
                       logAction.append(", version: " + currentItem.getVersion());
                   }
                }
                // Only log the user viewing if they're not still on the same page
                if (!isApplyTermsFlag && !isFilteredByDataProvider) {
                    UserLogger.log(null, userItem, UserLogger.VIEW_EDIT_TERMS,
                        logAction.toString(), false);
                }
            }

            // Pass list and current version as HTTP request parameters to edit view
            if (currentItem != null) {
                req.setAttribute(TOU_CURRENT_VERSION_ATTRIB, currentItem);
            }
            if (touItem != null) {
                req.setAttribute(TOU_ITEM_ATTRIB, touItem);
            }
            if (!touList.isEmpty()) {
                req.setAttribute(TOU_LIST_ATTRIB, touList);
            }

            // Pass dataProviders and projects to edit view
            if (userItem.getAdminFlag()) {
                req.setAttribute(PROVIDERS_ATTRIB, dataProviders);
                req.setAttribute(PROJECTS_ATTRIB, projects);
            } else if (hasTermsOfUseManagerRole) {
                List<ProjectItem> thisUsersProjects =
                    (List<ProjectItem>) projectDao.findByDataProvider(userItem);
                req.setAttribute(PROJECTS_ATTRIB, thisUsersProjects);
            }

            RequestDispatcher disp;
            disp = getServletContext().getRequestDispatcher(EDIT_PROJECT_JSP_NAME);
            try {
                disp.forward(req, resp);
            } catch (Exception exception) {
                forwardError(req, resp, logger, exception);
            } finally {
                logger.debug("doPost end");
            }
        }
    }   // end of projectDoPost

    /**
     * If the DataShop Terms of Use item does not exist, then one
     * is created and added to the database.
     * @param userItem the user deleting the terms
     * @return return true if the DataShop Terms exist
     * or were successfully created, or false, if otherwise
     */
    private Boolean ensureDatashopTermsExist(UserItem userItem) {
        TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        // Does a datashop terms of use exist?
        if (touDao.find(TermsOfUseItem.DATASHOP_TERMS) == null) {
            TermsOfUseItem touItem = new TermsOfUseItem();
            // If none exist, then try to create the DataShop Terms of Use

            Date now = (Date) Calendar.getInstance().getTime();
            touItem.setName(TermsOfUseItem.DATASHOP_TERMS);
            touItem.setRetiredFlag(false);
            touItem.setCreatedDate(now);

            touDao.saveOrUpdate(touItem);
            UserLogger.log(null, userItem, UserLogger.CREATE_TERMS,
                    touItem.getName(), false);
            logger.info("Create New Datashop Terms of Use (" + touItem.getId()
                    + ") with name: " + touItem.getName());
        }
        return true;
    }   // end of ensureDatashopTermsExist

    /**
     * If the Project Terms of Use item does not exist, then
     * one is created and added to the database.
     * @param userItem the user deleting the terms
     * @param touName the new project terms of use name to create
     * @return return a TermsOfUseItem if the project
     * terms were successfully created, and return null if
     * the project terms already exist or failed in creation
     */
    private TermsOfUseItem createProjectTerms(UserItem userItem, String touName) {
        TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        // Does a project terms of use exist?
        if (touDao.find(touName) == null) {
            TermsOfUseItem touItem = new TermsOfUseItem();
            // If not, then try to add the project Terms of Use
            Date now = (Date) Calendar.getInstance().getTime();
            touItem.setName(touName);
            touItem.setRetiredFlag(false);
            touItem.setCreatedDate(now);

            touDao.saveOrUpdate(touItem);
            UserLogger.log(null, userItem, UserLogger.CREATE_TERMS,
                    touItem.getName(), false);
            logger.info("Create New Terms of Use (" + touItem.getId()
                    + ") with name: " + touItem.getName());

            // Project ToU successfully created
            return touItem;
        }
        // Project ToU already exists
        return null;
    }   // end of createProjectTerms

    /**
     * Deletes a Terms of Use as long as no versions hold a status of applied
     * @param userItem the user deleting the terms
     * @param touItem the terms of use item
     */
    private void deleteTerms(UserItem userItem, TermsOfUseItem touItem) {
        TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        Boolean isApplied = touVersionDao.hasStatus(touItem,
                TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED);
        Boolean isArchived = touVersionDao.hasStatus(touItem,
                TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED);

        if (!isApplied && !isArchived && touItem != null) {
            List<TermsOfUseVersionItem> touVersionList
                = (List<TermsOfUseVersionItem>) touVersionDao.findAllByTermsOfUse(touItem);
            for (TermsOfUseVersionItem version : touVersionList) {
                touVersionDao.delete(version);
            }

            UserLogger.log(null, userItem, UserLogger.DELETE_TERMS,
                    touItem.getName(), false);
            logger.info("Deleted Terms of Use (" + touItem.getId()
                    + ") with name: " + touItem.getName());

            touDao.delete(touItem);
        }
    }   // end of deleteTerms

    /**
     * Retires a Terms of Use as long as no versions hold a status of applied
     * @param userItem the user deleting the terms
     * @param touItem the terms of use item
     */
    private void retireTerms(UserItem userItem, TermsOfUseItem touItem) {
        TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        Boolean isApplied = touVersionDao.hasStatus(touItem,
                TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED);
        Date now = (Date) Calendar.getInstance().getTime();

        if (!isApplied && touItem != null) {
            List<TermsOfUseVersionItem> touVersionList
                = (List<TermsOfUseVersionItem>) touVersionDao.findAllByTermsOfUse(touItem);
            for (TermsOfUseVersionItem version : touVersionList) {
                version.setArchivedDate(now);
                version.setStatus(TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED);
                touVersionDao.saveOrUpdate(version);
            }
            touItem.setRetiredFlag(true);
            touDao.saveOrUpdate(touItem);
            UserLogger.log(null, userItem, UserLogger.RETIRE_TERMS,
                    touItem.getName(), false);
            logger.info("Retired Terms of Use (" + touItem.getId()
                    + ") with name: " + touItem.getName());
        }
    }   // end of retireTerms

    /**
     * Update the status and date for a terms of use version item.
     * @param userItem the user deleting the terms
     * @param touItem terms of use item
     * @param termsOfUseVersionItem the terms of use version item
     * @param status the status string (constants defined in TermsOfUseVersionItem.java)
     */
    private void updateStatusTermsOfUseVersion(UserItem userItem, TermsOfUseItem touItem,
            TermsOfUseVersionItem termsOfUseVersionItem, String status) {
        if (!status.equals(TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED)
                && !status.equals(TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_SAVED)
                && !status.equals(TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED)) {
            return;
        }

        Date now = (Date) Calendar.getInstance().getTime();

        if (status.equals(TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_SAVED)) {
            termsOfUseVersionItem.setSavedDate(now);
        } else if (status.equals(TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED)) {
            termsOfUseVersionItem.setAppliedDate(now);
        } else if (status.equals(TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED)) {
            termsOfUseVersionItem.setArchivedDate(now);
        }

        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        termsOfUseVersionItem.setStatus(status);
        termsVersionDao.saveOrUpdate(termsOfUseVersionItem);


        logger.info("Update Terms of Use Version Item (" + termsOfUseVersionItem.getId()
                + ") with Version Value: " + termsOfUseVersionItem.getVersion());

    }   // end of updateStatusTermsOfUseVersion

    /**
     * Saves a new terms of use version item.
     * @param userItem The user requesting the save
     * @param touItem The terms of use item to save to
     * @param termsText The text
     */
    private void saveNewVersion(UserItem userItem, TermsOfUseItem touItem, String termsText) {

        TermsOfUseVersionDao touVersionDao =
                DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        ProjectTermsOfUseMapDao mapDao =
                DaoFactory.DEFAULT.getProjectTermsOfUseMapDao();

        ManageTermsHelper mtHelper = HelperFactory.DEFAULT.getManageTermsHelper();

        // Get the head project ToU version
        TermsOfUseVersionItem headItem
            = touVersionDao.findLastVersion((touItem.getName()));

        // Get the last version number if it exists or use zero
        Integer lastVersion = (headItem == null)
                ? 0 : (Integer)(headItem.getVersion());

        // Get applied, saved, and archived Versions to set the current version
        List<TermsOfUseVersionItem> appliedVersions = (List<TermsOfUseVersionItem>)
                touVersionDao.findVersionsByTermsAndStatus(touItem,
                        TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED);
        List<TermsOfUseVersionItem> savedVersions = (List<TermsOfUseVersionItem>)
                touVersionDao.findVersionsByTermsAndStatus(touItem,
                        TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_SAVED);
        // Set any applied versions in this ToU to archived
        for (TermsOfUseVersionItem appliedVersionItem : appliedVersions) {
            if (appliedVersionItem != null) {

                // Additionally, update history entries (expire date)
                List<ProjectItem> historyProjectList = (List<ProjectItem>)
                        mapDao.getProjectsForTermsOfUse(touItem);

                for (ProjectItem project : historyProjectList) {
                    mtHelper.setHistoryExpireDate(project, appliedVersionItem);
                }

                // Change the status of previously applied version
                updateStatusTermsOfUseVersion(
                    userItem, touItem, appliedVersionItem,
                    TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED);
            }
        }

        if (savedVersions != null
                && !savedVersions.isEmpty()
                && headItem != null) {

            // Overwrite last saved version
            Date now = (Date) Calendar.getInstance().getTime();
            headItem.setStatus(TermsOfUseVersionItem
                    .TERMS_OF_USE_VERSION_STATUS_SAVED);
            headItem.setTerms(termsText.trim());
            headItem.setSavedDate(now);
            touVersionDao.saveOrUpdate(headItem);

            // Log when a saved ToU version is overwritten
            UserLogger.log(null, userItem, UserLogger.SAVE_TERMS,
                touItem.getName()
                + ", version: "
                + headItem.getVersion(), false);
            logger.info("Saved Terms of Use Version (" + headItem.getId()
                    + ") with Version Value: " + headItem.getVersion());
        } else {
            // Setup new terms of use version
            TermsOfUseVersionItem newVersionItem
                = new TermsOfUseVersionItem();
            Date now = (Date) Calendar.getInstance().getTime();

            newVersionItem.setTermsOfUse(touItem);
            newVersionItem.setSavedDate(now);
            newVersionItem.setTerms(termsText.trim());
            newVersionItem.setVersion(lastVersion + 1);

            // Mark it applied (if an applied version exists)
            // or as saved if no applied versions exist
            if (appliedVersions != null && !appliedVersions.isEmpty()) {
                updateStatusTermsOfUseVersion(
                    userItem, touItem, newVersionItem,
                    TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED);
            } else {
                updateStatusTermsOfUseVersion(
                    userItem, touItem, newVersionItem,
                    TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_SAVED);
            }

            // save the version with its saved or applied status
            touVersionDao.saveOrUpdate(newVersionItem);
            logger.info("Saved Terms of Use Version ("
                    + newVersionItem.getId()
                    + ") with Version Value: "
                    + newVersionItem.getVersion());

            // update the project-ToU history item
            if (appliedVersions != null && !appliedVersions.isEmpty()) {
                List<ProjectItem> historyProjectList
                = (List<ProjectItem>) mapDao
                    .getProjectsForTermsOfUse(touItem);

                String actionString = TermsOfUseVersionItem.ACTION_UPDATED;
                ArrayList<String> projectNames = new ArrayList<String>();
                for (ProjectItem project : historyProjectList) {
                    // Update history table
                    mtHelper.createHistoryEntry(project, newVersionItem);
                    // Add to a list of project names for logger
                    projectNames.add(project.getProjectName());
                    // Send an e-mail to project owners for each changed project ToU
                    mtHelper.notifyProjectLeads(project, touItem, newVersionItem, actionString);
                }

                if (!projectNames.isEmpty()) {
                UserLogger.log(null, userItem, UserLogger.APPLY_TERMS,
                        touItem.getName() + " " + actionString + " "
                        + projectNames.toString(), false);
                }
            } else {
                // Log when a saved ToU version is created
                UserLogger.log(null, userItem, UserLogger.SAVE_TERMS,
                    touItem.getName()
                        + ", version: " + newVersionItem.getVersion(), false);
                logger.info("Saved Terms of Use Version (" + newVersionItem.getId()
                        + ") with Version Value: " + newVersionItem.getVersion());
            }
        }
    }   // end of saveNewVersion

    /**
     * Filter a list of projects based on the user's authorization role.
     * @param userItem the user to get the projects for
     * @param selectedProjectIds an array of project id's
     * @return a filtered list of projects
     */
    private List<ProjectItem> getUserSpecificProjects(UserItem userItem,
            String[] selectedProjectIds) {

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        boolean hasTermsOfUseManagerRole = userRoleDao.hasTermsManagerRole(userItem);

        List<ProjectItem> selectedProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> thisUsersProjects =
            (List<ProjectItem>) projectDao.findByDataProvider(userItem);

        if (selectedProjectIds != null) {
            for (int i = 0; i < selectedProjectIds.length; i++) {
                // Project Id
                Integer projectId
                    = Integer.parseInt(selectedProjectIds[i]);
                // Initialized ProjectItem
                ProjectItem acceptedProject
                    = (ProjectItem) projectDao.get(projectId);

                // Whether administrator or Data provider
                if (userItem.getAdminFlag()) {
                    // Administrator gets all projects
                    selectedProjects.add(acceptedProject);
                } else if (hasTermsOfUseManagerRole) {
                    // Data provider gets their projects
                    UserItem dataProvider
                        = (UserItem)acceptedProject.getDataProvider();

                    if (dataProvider != null
                           && userItem.getId().equals(dataProvider.getId())) {
                        selectedProjects.add(acceptedProject);
                    }
                }
            }
        }

        if (hasTermsOfUseManagerRole) {
            if (thisUsersProjects != null) {
                selectedProjects.retainAll(thisUsersProjects);
            } else {
                selectedProjects.clear();
            }
        }
        return selectedProjects;
    }   // end of getUserSpecificProjects

}   // end of ManageTermsServlet class
