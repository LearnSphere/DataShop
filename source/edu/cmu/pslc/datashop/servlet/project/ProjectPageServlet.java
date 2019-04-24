/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ExternalLinkDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ExternalLinkItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueServlet;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper;
import edu.cmu.pslc.datashop.servlet.irb.IrbReviewServlet;
import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * This servlet is for displaying a Project's Datasets.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13099 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-14 12:44:17 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectPageServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Constant for the servlet. */
    public static final String SERVLET = "Project";

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/jsp_project/project_page.jsp";

    /** Title for this page. */
    public static final String SERVLET_TITLE = "Project Page";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "Project";

    /** Session Parameter. */
    public static final String PROJECT_ID_ATTRIB = "PROJECT_ID";
    /** Session Parameter. */
    public static final String PROJECT_TAB_ATTRIB = "PROJECT_TAB";
    /** Session Parameter. */
    public static final String PROJECT_DTO_ATTRIB = "ProjectDto_";
    /** Session Parameter. */
    public static final String PROJECT_DELETED_ATTRIB = "Project deleted.";

    /** String constant. */
    public static final String MSG_RENAME_PROJECT_ERROR =
            "Error occurred while renaming this project.";
    /** String constant. */
    public static final String MSG_DELETE_PROJECT_ERROR =
            "Error occurred while deleting this project.";

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

        // Set the subtab
        httpSession.setAttribute(PROJECT_TAB_ATTRIB, "datasets");

        // Set the most recent servlet name for the help page
        setRecentReport(httpSession, SERVLET_NAME);

        // Set the RM's email address in the session for the JSPs
        setEmailAddressDatashopRMinSession(httpSession);

        // Get current user
        UserItem userItem = getLoggedInUserItem(req);

        try {

            String jspName = JSP_NAME;
            Integer projectId = getIntegerId((String)req.getParameter("id"));
            if (projectId != null) {

                String actionParam = getParameter(req, "ajaxAction");
                if (actionParam != null && actionParam.equals("saveDatasetChanges")) {
                    saveChangesDatasets(req, resp, userItem);
                    return;
                }

                if (req.getParameter("editDatasetsPage") != null) {
                    editDatasetsPage(req, resp, projectId);
                    return;
                } else if (req.getParameter("verifyUserName") != null) {
                    verifyUserName(req, resp, projectId);
                    return;
                } else if (req.getParameter("deleteProject") != null) {
                    deleteProject(req, resp, projectId);
                    return;
                } else if (req.getParameter("renameProject") != null) {
                    renameProject(req, resp, projectId);
                    return;
                } else {
                    ImportQueueHelper iqHelper = HelperFactory.DEFAULT.getImportQueueHelper();
                    iqHelper.handleCancelActions(req, resp, getUser(req));

                    ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                    ProjectItem projectItem = projectDao.get(projectId);
                    if (projectItem != null) {
                        httpSession.setAttribute(PROJECT_ID_ATTRIB, projectId);
                        //UserItem userItem = getUser(req);
                        if (userItem == null) {
                            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                            userItem = userDao.findOrCreateDefaultUser();
                        }

                        String logInfoStr = getProjectLogInfoStr(projectItem);
                        UserLogger.log(userItem,
                                       UserLogger.VIEW_PROJECT_DATASETS, logInfoStr, false);
                    } else {
                        jspName = ProjectServlet.SERVLET_NAME;
                    }
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
     * Helper method for processing an edit on the Project Datasets page.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @param projectId id of relevant project
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     * @throws JSONException a JSON exception
     */
    private void editDatasetsPage(HttpServletRequest req, HttpServletResponse resp,
                                  Integer projectId)
        throws IOException, ServletException, JSONException {

        if (req.getParameter("datasetIdList") != null) {
            editAppearsAnonymous(req, resp, projectId);
        } else if (req.getParameter("piName") != null) {
            editPiName(req, resp, projectId);
        } else if (req.getParameter("dpName") != null) {
            editDpName(req, resp, projectId);
        } else if (req.getParameter("description") != null) {
            editDescription(req, resp, projectId);
        } else if (req.getParameter("tags") != null) {
            editTags(req, resp, projectId);
        } else if (req.getParameter("externalLinkId") != null) {
            editExternalLink(req, resp, projectId);
        } else if ((req.getParameter("externalLinkTitle") != null)
                || (req.getParameter("externalLinkUrl") != null)) {
            addExternalLink(req, resp, projectId);
        } else if (req.getParameter("deleteExternalLink") != null) {
            deleteExternalLink(req, resp, projectId);
        } else {
            logDebug("Invalid editDatasetsPage request.");
        }
    }

    /**
     * Helper method for handling changes to the 'Appears Anonymous' flags.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void editAppearsAnonymous(HttpServletRequest req, HttpServletResponse resp,
                                      Integer projectId)
        throws IOException, ServletException {

        String datasetIdListStr = (String) req.getParameter("datasetIdList");

        List<Integer> datasetIdList = new ArrayList<Integer>();

        StringTokenizer st = new StringTokenizer(datasetIdListStr, ",");
        while (st.hasMoreElements()) {
            datasetIdList.add(new Integer((String) st.nextElement()));
        }

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        UserItem userItem = getUser(req);

        for (Integer i : datasetIdList) {
            String appearsAnonFlag = (String) req
                    .getParameter("appearsAnonSelect_" + i);
            DatasetItem datasetItem = datasetDao.get(i);
            String oldFlagValue = datasetItem.getAppearsAnonymous();

            if ((oldFlagValue == null) || (!oldFlagValue.equals(appearsAnonFlag))) {
                datasetItem.setAppearsAnonymous(appearsAnonFlag);
                datasetDao.saveOrUpdate(datasetItem);

                logChangeToAppearsAnon(datasetItem, userItem,
                        oldFlagValue, appearsAnonFlag);
            }
        }

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(PROJECT_DTO_ATTRIB + projectId,
                getProjectDto(req, projectId,
                        "Appears Anonymous flag(s) updated.",
                        ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS));

        resp.sendRedirect(SERVLET + "?id=" + projectId);
    }

    /**
     * Helper method for handling changes to the PI.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editPiName(HttpServletRequest req, HttpServletResponse resp, Integer projectId)
            throws IOException, JSONException {

        String piName = (String) req.getParameter("piName");
        String makePiAdmin = (String) req.getParameter("makePiAdmin");

        String message;
        String messageLevel;
        String value = "";

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem piItem = userDao.get(piName);
        if ((piItem == null) && (!piName.equals(""))) {
            logDebug("editPiName: failed to find specified user.");
            message = "Invalid username.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = projectDao.get(projectId);
            if (projectItem == null) {
                message = "Invalid project id.";
                messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            } else {
                if (piItem == null) {
                    logDebug("Removing PI.");
                }
                UserItem origPi = projectItem.getPrimaryInvestigator();
                String origPiStr = (origPi != null) ? (String) origPi.getId() : "null";

                projectItem.setPrimaryInvestigator(piItem);
                projectDao.saveOrUpdate(projectItem);

                // Push PI name change out to master.
                updateMasterInstance(projectItem);

                updateUserLog(getUser(req), projectItem, "PI", origPiStr, piName);

                if (Boolean.parseBoolean(makePiAdmin)) {
                    String authLevel = AuthorizationItem.LEVEL_ADMIN;
                    ProjectPermissionsHelper projPermHelper =
                        HelperFactory.DEFAULT.getProjectPermissionsHelper();
                    projPermHelper.addNewUserAccess(projectItem, piItem, authLevel);

                    String logInfoStr = "User '" + piName + "' given '" + authLevel
                        + "' access to Project '" + projectItem.getProjectName() + "'";
                    UserLogger.log(getUser(req), UserLogger.ADD_NEW_USER_ACCESS, logInfoStr, false);
                }

                message = "PI updated.";
                messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                if (piItem != null) {
                    value = piItem.getName();
                }
            }
        }

        req.setAttribute(PROJECT_DTO_ATTRIB + projectId,
                getProjectDto(req, projectId, message, messageLevel));

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", value));
    }

    /**
     * Helper method for handling changes to the DataProvider.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editDpName(HttpServletRequest req, HttpServletResponse resp, Integer projectId)
            throws IOException, JSONException {

        String dpName = (String) req.getParameter("dpName");
        logDebug("editDpName: ", dpName);

        String message;
        String messageLevel;
        String value = "";

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(dpName);
        if ((userItem == null) && (!dpName.equals(""))) {
            logDebug("editDpName: failed to find specified user.");
            message = "Invalid username.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = projectDao.get(projectId);
            if (projectItem == null) {
                message = "Invalid project id.";
                messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            } else {
                if (userItem == null) {
                    logDebug("Removing DataProvider.");
                }
                UserItem origDp = projectItem.getDataProvider();
                String origDpStr = (origDp != null) ? (String) origDp.getId() : "null";

                projectItem.setDataProvider(userItem);
                projectDao.saveOrUpdate(projectItem);

                // Push DP name change out to master.
                updateMasterInstance(projectItem);

                updateUserLog(getUser(req), projectItem, "DP", origDpStr, dpName);

                message = "Data Provider updated.";
                messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                if (userItem != null) {
                    value = userItem.getName();
                }
            }
        }

        req.setAttribute(PROJECT_DTO_ATTRIB + projectId,
                getProjectDto(req, projectId, message, messageLevel));

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", value));
    }

    /**
     * Helper method for handling changes to the description.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editDescription(HttpServletRequest req,
            HttpServletResponse resp, Integer projectId) throws IOException, JSONException {

        String description = (String) req.getParameter("description");
        logDebug("editDescription: ", description);

        String message;
        String messageLevel;
        String value = "";

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        if (projectItem == null) {
            logDebug("editDescription: failed to find project by id");
            message = "Invalid project id.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            String origDescription = projectItem.getDescription();

            projectItem.setDescription(description);
            projectDao.saveOrUpdate(projectItem);

            updateUserLog(getUser(req), projectItem, "Description",
                    origDescription, description);

            message = "Description updated.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            value = description;
        }

        req.setAttribute(PROJECT_DTO_ATTRIB + projectId,
                getProjectDto(req, projectId, message, messageLevel));

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", value));
    }

    /**
     * Helper method for handling changes to the tags.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editTags(HttpServletRequest req, HttpServletResponse resp, Integer projectId)
            throws IOException, JSONException {

        String tags = (String) req.getParameter("tags");
        logDebug("editTags: ", tags);

        String message;
        String messageLevel;
        String value = "";

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        if (projectItem == null) {
            logDebug("editTags: failed to find project by id");
            message = "Invalid project id.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            String origTags = projectItem.getTags();

            projectItem.setTags(tags);
            projectDao.saveOrUpdate(projectItem);

            updateUserLog(getUser(req), projectItem, "Tags", origTags, tags);

            message = "Tags updated.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            value = tags;
        }

        req.setAttribute(PROJECT_DTO_ATTRIB + projectId,
                getProjectDto(req, projectId, message, messageLevel));

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", value));
    }

    /**
     * Helper method for handling changes to an external link.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editExternalLink(HttpServletRequest req,
            HttpServletResponse resp, Integer projectId) throws IOException, JSONException {

        String linkId = (String) req.getParameter("externalLinkId");
        String linkTitle = (String) req.getParameter("externalLinkTitle");
        String linkUrl = (String) req.getParameter("externalLinkUrl");
        logDebug("editExternalLink: ", linkId,
                ": new title = ", linkTitle, ", new URL = ", linkUrl);

        String message;
        String messageLevel;
        String valueTitle = "";
        String valueUrl = "";

        ExternalLinkDao externalLinkDao = DaoFactory.DEFAULT.getExternalLinkDao();
        ExternalLinkItem linkItem = externalLinkDao.get(Integer.parseInt(linkId));
        if (linkItem == null) {
            logDebug("editExternalLink: failed to find link by id");
            message = "Invalid link id.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            String origLinkStr = "title '" + linkItem.getTitle() + "', URL '"
                    + linkItem.getUrl() + "'";
            String newLinkStr = "title '" + linkTitle + "', URL '" + linkUrl + "'";

            linkItem.setTitle(linkTitle);
            linkItem.setUrl(linkUrl);
            externalLinkDao.saveOrUpdate(linkItem);

            updateUserLog(getUser(req),
                    getProjectItem(req), "external link", origLinkStr, newLinkStr);

            message = "External link updated.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            valueTitle = linkTitle;
            valueUrl = linkUrl;
        }

        req.setAttribute(PROJECT_DTO_ATTRIB + projectId,
                getProjectDto(req, projectId, message, messageLevel));

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "valueTitle",
                        valueTitle, "valueUrl", valueUrl, "linkId", linkId));
    }

    /**
     * Helper method for adding an external link.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void addExternalLink(HttpServletRequest req, HttpServletResponse resp,
                                 Integer projectId)
        throws IOException, ServletException {

        String linkTitle = (String) req.getParameter("externalLinkTitle");
        String linkUrl = (String) req.getParameter("externalLinkUrl");
        logDebug("addExternalLink: ", "title = ", linkTitle, ", URL = ",
                linkUrl);

        String message;
        String messageLevel;

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);
        if (projectItem == null) {
            logDebug("addExternalLink: failed to find project by id");
            message = "Invalid project id.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            ExternalLinkDao externalLinkDao = DaoFactory.DEFAULT.getExternalLinkDao();
            ExternalLinkItem linkItem = new ExternalLinkItem();
            linkItem.setTitle(linkTitle);
            linkItem.setUrl(linkUrl);
            linkItem.setProject(projectItem);
            externalLinkDao.saveOrUpdate(linkItem);

            String logInfoStr = getProjectLogInfoStr(projectItem);
            logInfoStr += ": Added external link '" + linkTitle + "', URL '" + linkUrl + "'";
            UserLogger.log(getUser(req), UserLogger.PROJECT_INFO_MODIFY, logInfoStr, false);

            message = "External link added.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(PROJECT_DTO_ATTRIB + projectId,
                                      getProjectDto(req, projectId, message, messageLevel));
        resp.sendRedirect(SERVLET + "?id=" + projectId);
    }

    /**
     * Helper method for deleting an external link.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void deleteExternalLink(HttpServletRequest req, HttpServletResponse resp,
                                    Integer projectId)
        throws IOException, ServletException {

        String linkId = (String) req.getParameter("deleteExternalLink");
        logDebug("deleteExternalLink: ", linkId);

        String message;
        String messageLevel;

        ExternalLinkDao externalLinkDao = DaoFactory.DEFAULT.getExternalLinkDao();
        ExternalLinkItem linkItem = externalLinkDao.get(Integer.parseInt(linkId));
        if (linkItem == null) {
            logDebug("deleteExternalLink: failed to find link by id");
            message = "Invalid link id.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            externalLinkDao.delete(linkItem);

            String logInfoStr = getProjectLogInfoStr(getProjectItem(req));
            logInfoStr += ": Deleted external link '" + linkItem.getTitle()
                    + "', URL '" + linkItem.getUrl() + "'";
            UserLogger.log(getUser(req), UserLogger.PROJECT_INFO_MODIFY, logInfoStr, false);

            message = "External link deleted.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(PROJECT_DTO_ATTRIB + projectId,
                                      getProjectDto(req, projectId, message, messageLevel));
        resp.sendRedirect(SERVLET + "?id=" + projectId);
    }

    /**
     * Helper method to verify a given userName.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void verifyUserName(HttpServletRequest req, HttpServletResponse resp, Integer projectId)
            throws IOException, JSONException {

        String userName = (String) req.getParameter("userName");

        String message;
        String messageLevel;
        String fullUserName = "";
        boolean alreadyAdmin = false;

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(userName);
        if (userItem == null) {
            logDebug("verifyUserName: failed to find specified user.");
            message = "Invalid username.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            ProjectPageHelper projectPageHelper = HelperFactory.DEFAULT.getProjectPageHelper();

            String authLevel = projectPageHelper.getAuthLevel((String)userItem.getId(), projectId);
            if ((authLevel != null) && (authLevel.equals(AuthorizationItem.LEVEL_ADMIN))) {
                alreadyAdmin = true;
            }

            message = "Specified user exists.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            fullUserName = userItem.getName();
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel,
                             "fullUserName", fullUserName, "alreadyAdmin", alreadyAdmin));
    }

    /**
     * Helper method for deleting a project.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void deleteProject(HttpServletRequest req, HttpServletResponse resp,
                               Integer projectId)
        throws IOException, ServletException {

        String message;
        String messageLevel;

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);

        boolean deleted = false;

        if (projectItem == null) {
            message = MSG_DELETE_PROJECT_ERROR;
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            logger.error("ProjectItem is null. " + message);
        } else {
            String logPrefix = "Project(" + projectId + "): ";
            try {
                // We allow project to be deleted with items in the ImportQueue
                // only if those items all have a display_flag value of 0.
                ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
                Long count = iqDao.getCountByProjectAndDisplayFlag(projectItem, true);
                boolean allowDelete = (count == 0) ? true : false;

                if (allowDelete) {
                    // All items for this project have a display_flag of 'false'.
                    List<ImportQueueItem> iqList =
                        iqDao.findByProjectAndDisplayFlag(projectItem, false);
                    deleteIqList(iqDao, iqList);

                    // All items for this project have a display_flag of 'false'.
                    List<ImportQueueItem> iqPendingList = iqDao.findPendingByProject(projectItem);
                    deleteIqList(iqDao, iqPendingList);

                    // Now that all the IQ items are deleted, the project can be deleted.
                    projectDao.delete(projectItem);

                    String logInfoStr = getProjectLogInfoStr(projectItem);
                    UserLogger.log(getUser(req), UserLogger.PROJECT_DELETE, logInfoStr, false);

                    message = "Project deleted.";
                    messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                    logger.info(logPrefix + message);

                    deleted = true;
                } else {
                    // Can't delete project with ImportQueue items...
                    message = "Cannot delete project as it's not empty.";
                    messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
                    logger.error(logPrefix + message);
                }
            } catch (Exception e) {
                // Failed to delete the project. Notify user... nicely.
                message = "Error occurred while deleting the project.";
                messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
                logger.error(logPrefix + message, e);
            }
        }

        if (!deleted) {
            req.getSession().setAttribute(PROJECT_DTO_ATTRIB + projectId,
                                          getProjectDto(req, projectId, message, messageLevel));
            resp.sendRedirect(SERVLET + "?id=" + projectId);
        } else {
            req.getSession().setAttribute(PROJECT_DELETED_ATTRIB, PROJECT_DELETED_ATTRIB);
            resp.sendRedirect(ProjectServlet.REDIRECT_SERVLET_NAME);
        }
    }

    /**
     * Delete the IQ items in the given list.
     * @param iqDao the dao
     * @param iqPendingList the list
     */
    private void deleteIqList(ImportQueueDao iqDao,
            List<ImportQueueItem> iqPendingList) {
        for (ImportQueueItem item : iqPendingList) {
            item = iqDao.get((Integer)item.getId());
            FileItem fileItem = item.getFile();

            iqDao.delete(item);

            if (fileItem != null) {
                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                fileItem = fileDao.get((Integer)fileItem.getId());
                ImportQueueServlet.removeImportQueueFile(fileItem, getBaseDir());
            }
        }
    }

    /**
     * Helper method for renaming a project.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the id for the relevant project
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void renameProject(HttpServletRequest req, HttpServletResponse resp,
                               Integer projectId)
        throws IOException, JSONException {

        String message;
        String messageLevel;
        String value = "";

        String newName = (String)req.getParameter("newProjectName");

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);

        if ((projectItem == null) || (newName == null)) {
            message = MSG_RENAME_PROJECT_ERROR;
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (!checkNameIsUnique(newName)) {
            message = ImportQueueServlet.MSG_PROJECT_NAME_TAKEN;
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            value = "duplicate";
        } else {
            String oldName = projectItem.getProjectName();

            projectItem.setProjectName(newName);
            projectDao.saveOrUpdate(projectItem);

            String logInfoStr = getProjectLogInfoStr(projectItem);
            logInfoStr += ": Changed project name from '" + oldName + "'";
            UserLogger.log(getUser(req), UserLogger.PROJECT_RENAME, logInfoStr, false);

            message = "Project renamed.";
            messageLevel = ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            value = newName;
        }

        req.setAttribute(PROJECT_DTO_ATTRIB + projectId,
                         getProjectDto(req, projectId, message, messageLevel));

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", value));
    }

    /**
     * Helper method to determine if project name is unique.
     * @param projectName name to check
     * @return boolean indicating result of check
     */
    private boolean checkNameIsUnique(String projectName) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        Collection projects = projectDao.find(projectName);
        if (projects.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Helper method for getting the Project DTO.
     * @param req HttpServletRequest
     * @param projectId id for the relevant project
     * @param message status message
     * @param messageLevel status message level
     * @return ProjectDto
     */
    private ProjectDto getProjectDto(HttpServletRequest req, Integer projectId,
            String message, String messageLevel) {

        UserItem userItem = getUser(req);
        ProjectPageHelper projPageHelper = HelperFactory.DEFAULT.getProjectPageHelper();
        ProjectDto projectDto = projPageHelper.getProjectDto(req, projectId, userItem);
        projectDto.setMessage(message);
        projectDto.setMessageLevel(messageLevel);

        return projectDto;
    }

    /**
     * Helper method for getting the Project item.
     * @param req HttpServletRequest
     * @return ProjectItem
     */
    private ProjectItem getProjectItem(HttpServletRequest req) {

        // This should never happen given how we're using this method.
        if (req.getParameter("id") == null) {
            return null;
        }

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        return projectDao.get(Integer.parseInt((String) req.getParameter("id")));
    }

    /**
     * Helper method for generating user log info for specific project.
     * @param projectItem the project
     * @return String the log info
     */
    private String getProjectLogInfoStr(ProjectItem projectItem) {
        return "Project '" + projectItem.getProjectName() + "' (" + projectItem.getId() + ")";
    }

    /**
     * Constant for maximum length of description to log.
     */
    private static final int MAX_DESC_LENGTH_LOGGED = 500;

    /**
     * Helper method for updating user log.
     * @param userItem the user
     * @param projectItem the project
     * @param attrName the attribute
     * @param oldValue the original value
     * @param newValue the new value
     */
    private void updateUserLog(UserItem userItem, ProjectItem projectItem,
            String attrName, String oldValue, String newValue) {
        if (oldValue != null && oldValue.length() > MAX_DESC_LENGTH_LOGGED) {
            oldValue = oldValue.substring(0, MAX_DESC_LENGTH_LOGGED);
        }
        if (newValue != null && newValue.length() > MAX_DESC_LENGTH_LOGGED) {
            newValue = newValue.substring(0, MAX_DESC_LENGTH_LOGGED);
        }
        String logInfoStr = getProjectLogInfoStr(projectItem);
        logInfoStr += ": Changed " + attrName + " from '" + oldValue + "' to '" + newValue + "'";
        UserLogger.log(userItem, UserLogger.PROJECT_INFO_MODIFY, logInfoStr, false);
    }

    /**
     * Get the dataset item given an id as a string.
     * @param idParam the string of the id
     * @return the item if found, null otherwise
     */
    private DatasetItem getDatasetItem(String idParam) {
        DatasetItem item = null;
        if (idParam != null) {
            try {
                Integer itemId = Integer.parseInt(idParam);
                item = DaoFactory.DEFAULT.getDatasetDao().get(itemId);
            } catch (NumberFormatException exception) {
                item = null;
            }
        }
        return item;
    }

    /** Constant for the index into the string returned from UI: Dataset. */
    private static final int DS_IDX = 0;
    /** Constant for the index into the string returned from UI: Appears Anonymous. */
    private static final int AA_IDX = 1;
    /** Constant for the index into the string returned from UI: IRB Uploaded. */
    private static final int IU_IDX = 2;
    /** Constant for the index into the string returned from UI: Study Flag. */
    private static final int SF_IDX = 3;

    /**
     * Gets a list of datasets for the given project.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    void saveChangesDatasets(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
            throws IOException, JSONException {
        boolean errorFlag = false;
        boolean dataChanged = false;

        ProjectItem projectItem = getProjectItem(req);
        if (projectItem == null) {
            errorFlag = true;
        } else {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            boolean irbUploadedChanged = false;
            boolean appearsAnonChanged = false;

            String theData = req.getParameter("theData");

            String[] arr = theData.split("-");
            for (int idx = 0; idx < arr.length; idx++) {
                boolean datasetChanged = false;
                String[] values = arr[idx].split(",");
                DatasetItem datasetItem = getDatasetItem(values[DS_IDX]);

                String oldAppearsAnon = datasetItem.getAppearsAnonymous();
                String newAppearsAnon = DatasetItem.getAppearsAnonymousEnum(values[AA_IDX]);
                if (oldAppearsAnon == null || !oldAppearsAnon.equals(newAppearsAnon)) {
                    datasetItem.setAppearsAnonymous(newAppearsAnon);
                    datasetChanged = true;
                    appearsAnonChanged = true;
                }

                String oldIrbUploaded = datasetItem.getIrbUploaded();
                String newIrbUploaded = values[IU_IDX];
                if (!oldIrbUploaded.equals(newIrbUploaded)) {
                    datasetItem.setIrbUploaded(newIrbUploaded);
                    datasetChanged = true;
                    irbUploadedChanged = true;
                }

                String oldHasStudyData = datasetItem.getStudyFlag();
                String newHasStudyData = values[SF_IDX];
                if (!oldHasStudyData.equals(newHasStudyData)) {
                    datasetItem.setStudyFlag(newHasStudyData);
                    datasetChanged = true;
                }

                if (datasetChanged) {
                    datasetDao.saveOrUpdate(datasetItem);
                    dataChanged = true;
                }
                if (oldAppearsAnon == null || !oldAppearsAnon.equals(newAppearsAnon)) {
                    logChangeToAppearsAnon(datasetItem, userItem,
                            oldAppearsAnon, newAppearsAnon);
                }
                if (!oldIrbUploaded.equals(newIrbUploaded)) {
                    logChangeToIrbUploaded(datasetItem, userItem,
                            oldIrbUploaded, newIrbUploaded);
                }
                if (!oldHasStudyData.equals(newHasStudyData)) {
                    logChangeToHasStudyData(datasetItem, userItem,
                            oldHasStudyData, newHasStudyData);
                }
            } // end for loop

            // If the IRB Uploaded value was changed for any dataset,
            // then decide if we need to change the value for the project's Needs Attention flag.
            if (irbUploadedChanged) {
                IrbReviewServlet.updateNeedsAttentionIrbUploadedChanged(logger,
                                                                        projectItem, userItem);
            }

            // If the 'Appears Anonymous' value changed for any of the datasets,
            // check to see if the 'Needs Attention' flag should change for the project.
            if (appearsAnonChanged) {
                IrbReviewServlet.updateNeedsAttentionAppearsAnonChanged(projectItem, userItem);
            }
        }

        if (!errorFlag) {
            String msg = "No changes made.";
            if (dataChanged) {
                msg = "Changes saved for project: " + projectItem.getProjectName() + ".";
            }
            writeJSON(resp, json(
                    "flag", "success",
                    "projectId", projectItem.getId(),
                    "needsAttention", projectItem.getNeedsAttention(),
                    "message", msg));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", "Unknown error occurred."));
        }
        return;
    }

    /**
     * Helper method to push project PI or DP change out to master DataShop instance.
     * @param project the ProjectItem
     */
    private void updateMasterInstance(ProjectItem project) {

        // If a slave, update master DataShop instance with dataset info.
        if (DataShopInstance.isSlave()) {
            Collection<DatasetItem> datasets =
                DaoFactory.DEFAULT.getDatasetDao().findByProject(project, true);
            for (DatasetItem dataset : datasets) {
                try {
                    Integer datasetId = (Integer)dataset.getId();
                    DatasetDTO datasetDto =
                        HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                    DatasetCreator.INSTANCE.setDataset(datasetDto);
                } catch (Exception e) {
                    // Failed to push Dataset info to master. Ignore?
                    logDebug("Failed to push dataset info to master for dataset '"
                             + dataset.getDatasetName() + "': " + e);
                }
            }
        }
    }

}
