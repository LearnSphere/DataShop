/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet is for editing the Project page contents.
 *
 * @author Cindy Tipper
 * @version $ $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-17 14:07:52 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectPageEditServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** PI parameter */
    private static final String PI_PARAM = "primaryInvestigator";
    /** DP parameter */
    private static final String DP_PARAM = "dataProvider";
    /** Get User List parameter */
    private static final String GET_USER_LIST_PARAM = "getUserList";

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/jsp_admin/project_page.jsp";
    /** The JSP name for this servlet. */
    public static final String PROJECT_ID_ATTRIB = "PROJECT_ID";

    /** Title for this page. */
    public static final String SERVLET_TITLE = "Project Page Edit";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ProjectPageEdit";

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

    /** Message for the user. */
    private static final String LOGIN_AGAIN_MSG =
        "You are no longer logged in.  Backup your current changes and then log in again.";

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

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {
            setEncoding(req, resp);

            boolean isLoggedIn = isLoggedIn(req);
            if (!isLoggedIn) {
                out = resp.getWriter();
                out.write(buildJSONMessage("UNAUTHENTICATED", LOGIN_AGAIN_MSG).toString());
                out.flush();
                out.close();
                return;
            }

            UserItem userItem = getUser(req);
            boolean hasAdminAuthorization = userItem.getAdminFlag();

            if (!hasAdminAuthorization) {
                boolean hasEditAuthorization = hasEditAuthorization(req, getDatasetContext(req));
                if (!hasEditAuthorization) {
                    out = resp.getWriter();
                    out.write(buildJSONMessage("UNAUTHORIZED",
                    "You are not authorized to edit this dataset.").toString());
                    out.flush();
                    out.close();
                    return;
                }
            }

            JSONObject returnJSON = processProjectParameters(req, hasAdminAuthorization, userItem);

            if (returnJSON == null) {
                returnJSON = processUserListParameter(req, hasAdminAuthorization);
            }

            if (returnJSON == null) {
                returnJSON = buildJSONMessage("ERROR", "Unknown parameters.");
            }

            resp.setContentType("application/json");
            out = resp.getWriter();
            out.write(returnJSON.toString());
            out.flush();
        } catch (Throwable throwable) {
            logger.error("Exception occurred editing dataset info.", throwable);
            resp.setContentType("text/html");
            try {
                out = resp.getWriter();
                out.write(buildJSONMessage("ERROR",
                        "An unexpected error occurred, please try again and/or "
                        + "contact the datashop team and describe the error.").toString());
                out.flush();
            } catch (JSONException jsonException) {
                logger.warn("First an Exception occurred.", throwable);
                logger.warn("Then an JSONException occurred.", jsonException);
            } catch (IOException ioException) {
                logger.warn("First an Exception occurred.", throwable);
                logger.warn("Then an IOException occurred.", ioException);
            }
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /**
     * Process the parameters from the project page, which are only
     * the primary investigator and the data provider fields.
     * @param req the HttpServletRequest
     * @param hasAdminAuthorization boolean indicating administrator level authorization.
     * @param user the UserItem of the user making the request (for logging purposes)
     * @return JSONObject return message
     * @throws JSONException an exception occurred creating the JSON objects.
     */
    private JSONObject processProjectParameters(HttpServletRequest req,
            Boolean hasAdminAuthorization, UserItem user) throws JSONException {
        JSONObject returnJSON = null;

        boolean flag = false;

        String fieldParam = req.getParameter("field");
        String value = req.getParameter("value");

        if (PI_PARAM.equals(fieldParam) && hasAdminAuthorization) {
            String projectIdParam = req.getParameter("projectId");
            Integer projectId = Integer.parseInt(projectIdParam);
            ProjectItem projectItem = DaoFactory.DEFAULT.getProjectDao().get(projectId);
            returnJSON = proccessPrimaryInvestgatorSave(projectItem, value, user);
            flag = true;
        } else if (PI_PARAM.equals(fieldParam)) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
            "You are not authorized to change the Primary Investigator.");
            flag = true;
        }

        if (DP_PARAM.equals(fieldParam) && hasAdminAuthorization) {
            String projectIdParam = req.getParameter("projectId");
            Integer projectId = Integer.parseInt(projectIdParam);
            ProjectItem projectItem = DaoFactory.DEFAULT.getProjectDao().get(projectId);
            returnJSON = processDataProviderSave(projectItem, value, user);
            flag = true;
        } else if (DP_PARAM.equals(fieldParam)) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
            "You are not authorized to change the Primary Investigator.");
            flag = true;
        }

        if (flag) {
            String getUserList = req.getParameter(GET_USER_LIST_PARAM);
            if (getUserList != null && hasAdminAuthorization) {
                returnJSON = getUserList();
            } else if (getUserList != null) {
                returnJSON = buildJSONMessage("UNAUTHORIZED",
                "You are not authorized to view the list of users.");

            }
        }
        return returnJSON;
    }

    /**
     * Save the primary Investigator for the dataset.
     * @param projectItem the project item
     * @param newPiUserName the user Id of the PI
     * @param remoteUser the user requesting the change (for logging purposes)
     * @return String of success.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject proccessPrimaryInvestgatorSave(
            ProjectItem projectItem,
            String newPiUserName,
            UserItem remoteUser) throws JSONException {
        if (newPiUserName == null) {
            throw new IllegalArgumentException("Primary Investigator cannot be null.");
        }
        if (projectItem == null) {
            throw new IllegalArgumentException("projectItem cannot be null.");
        }

        String displayString = "";
        String oldPI = null;

        if (!newPiUserName.equals("")) {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem primaryInvestigator = userDao.get(newPiUserName);
            if (primaryInvestigator == null) {
                return buildJSONMessage("ERROR",
                        "The selected user (" + primaryInvestigator + ") was not found.");
            }

            oldPI = (projectItem.getPrimaryInvestigator() != null)
                ? (String)projectItem.getPrimaryInvestigator().getId() : null;
            projectItem.setPrimaryInvestigator(primaryInvestigator);
            displayString = (String)primaryInvestigator.getId();
        } else {
            oldPI = (projectItem.getPrimaryInvestigator() != null)
                ? (String)projectItem.getPrimaryInvestigator().getId() : null;
            projectItem.setPrimaryInvestigator(null);
        }

        DaoFactory.DEFAULT.getProjectDao().saveOrUpdate(projectItem);

        String prefix = "Project '" + projectItem.getProjectName()
                      + "' (" + projectItem.getId() + "): ";
        String msg = (oldPI != null && oldPI.equals(newPiUserName)) ? "No change to PI"
                        : "Changed PI from '" + oldPI + "' to '" + newPiUserName + "'";
        UserLogger.log(null, remoteUser, UserLogger.PROJECT_INFO_MODIFY, prefix + msg);

        return buildJSONMessage("SUCCESS", "Primary Investigator set.", displayString);
    }

    /**
     * Save the Data Provider for the dataset.
     * @param projectItem the project item
     * @param newDpUserName the user Id of the Data Provider
     * @param remoteUser the user requesting the change (for logging purposes)
     * @return String of success.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject processDataProviderSave(
            ProjectItem projectItem,
            String newDpUserName,
            UserItem remoteUser) throws JSONException {
        if (newDpUserName == null) {
            throw new IllegalArgumentException("Data Provider cannot be null.");
        }
        if (projectItem == null) {
            throw new IllegalArgumentException("projectItem cannot be null.");
        }

        String displayString = "";
        String oldDP = null;


        if (!newDpUserName.equals("")) {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem dataProvider = userDao.get(newDpUserName);
            if (dataProvider == null) {
                return buildJSONMessage("ERROR",
                        "The selected user (" + dataProvider + ") was not found.");
            }

            oldDP = (projectItem.getDataProvider() != null)
                ? (String)projectItem.getDataProvider().getId() : null;
            projectItem.setDataProvider(dataProvider);
            displayString = (String)dataProvider.getId();
        } else {
            oldDP = (projectItem.getDataProvider() != null)
                ? (String)projectItem.getDataProvider().getId() : null;
            projectItem.setDataProvider(null);
        }

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        projectDao.saveOrUpdate(projectItem);

        String prefix = "Project '" + projectItem.getProjectName()
                      + "' (" + projectItem.getId() + "): ";
        String msg = (oldDP != null && oldDP.equals(newDpUserName)) ? "No change to DP"
                        : "Changed DP from '" + oldDP + "' to '" + newDpUserName + "'";
        UserLogger.log(null, remoteUser, UserLogger.PROJECT_INFO_MODIFY, prefix + msg);

        return buildJSONMessage("SUCCESS", "Data Provider set.", displayString);
    }

    /**
     * Get the user list.
     * @param req the HttpServletRequest
     * @param hasAdminAuthorization boolean indicating administrator level authorization.
     * @return JSONObject return message
     * @throws JSONException an exception occurred creating the JSON objects.
     */
    private JSONObject processUserListParameter(HttpServletRequest req,
            Boolean hasAdminAuthorization) throws JSONException {
        JSONObject returnJSON = null;
        String getUserList = req.getParameter(GET_USER_LIST_PARAM);
        if (getUserList != null && hasAdminAuthorization) {
            returnJSON = getUserList();
        } else if (getUserList != null) {
            returnJSON = buildJSONMessage("UNAUTHORIZED",
            "You are not authorized to view the list of users.");
        }
        return returnJSON;
    }

    /**
     * Get a list of all datashop users for display.
     * @return a JSONObject of the data.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject getUserList() throws JSONException {
        logger.debug("Getting list of users as a JSON string.");

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        List<UserItem> userList = userDao.findAll();
        Collections.sort(userList);
        List <JSONObject> jsonUserList = new ArrayList <JSONObject>();

        //add a blank option.
        JSONObject userJSON = new JSONObject();
        userJSON.append("value", "");
        userJSON.append("text", "");
        jsonUserList.add(userJSON);

        for (Iterator<UserItem> it = userList.iterator(); it.hasNext();) {
                UserItem userItem = it.next();
                if (userItem.getId().equals(UserItem.DEFAULT_USER)) { continue; }

                String firstName = (userItem.getFirstName() != null)
                                        ? userItem.getFirstName() : "-";
                String lastName = (userItem.getLastName() != null)
                                        ? userItem.getLastName() : "-";
                String email = (userItem.getEmail() != null)
                                        ? userItem.getEmail() : "-";

                String displayString = lastName + ", " + firstName
                    + " (" + userItem.getId() + ", " + email + ")";

                userJSON = new JSONObject();
                userJSON.append("text", displayString);
                userJSON.append("value", userItem.getId());
                jsonUserList.add(userJSON);
        }

        JSONObject projectListJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray(jsonUserList);
        projectListJSON.put("suggestions", jsonArray);
        return projectListJSON;
    }

    /**
     * Create a message as a JSON object.
     * @param messageType The type of message ('ERROR', 'SUCCESS', 'UNAUTHORIZED', 'MESSAGE');
     * @param message The text of the message.
     * @return The message as a JSON object.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject buildJSONMessage(String messageType, String message)
            throws JSONException {
        return buildJSONMessage(messageType, message, null);
    }

    /**
     * Create a message as a JSON object.
     * @param messageType The type of message ('ERROR', 'SUCCESS', 'UNAUTHORIZED', 'MESSAGE');
     * @param message The text of the message.
     * @param value The value of message, usually refers to an update/delete/save.
     * @return The message as a JSON object.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject buildJSONMessage(String messageType, String message, String value)
                throws JSONException {
        logger.debug("Generating a return message as a JSON object.");
        logger.debug("messageType = " + messageType);
        logger.debug("message = " + message);
        logger.debug("value = " + value);

        JSONObject messageJSON = new JSONObject();
        messageJSON.append("messageType", messageType);
        messageJSON.append("message", message);
        messageJSON.append("value", value);
        return messageJSON;
    }
}
