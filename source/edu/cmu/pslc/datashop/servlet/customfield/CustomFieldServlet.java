/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.customfield;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportServlet;

/**
 * This servlet is for displaying a Dataset's Custom Fields.
 *
 * @author Cindy Tipper
 * @version $Revision: 11974 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-02-05 14:48:18 -0500 (Thu, 05 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/custom_fields.jsp";
    /** The JSP name for the project_page servlet. */
    public static final String PROJECT_PAGE_JSP_NAME = "/jsp_project/project_page.jsp";

    /** Constant for the servlet. */
    public static final String SERVLET = "CustomFields";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "Custom Fields";

    /** Constant for the request/session attribute. */
    public static final String CF_LIST_ATTR = "cfList_";
    /** Constant for the request/session attribute. */
    public static final String CF_DISPLAY_EDIT_ATTR = "cfDisplayEdit_";
    /** Constant for the request/session attribute. */
    public static final String CF_MESSAGE_ATTR = "cfMessage_";
    /** Constant for the request/session attribute. */
    public static final String CF_MESSAGE_LEVEL_ATTR = "cfMessageLevel_";

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

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {

            String jspName = JSP_NAME;
            Integer datasetId = getIntegerId((String)req.getParameter("datasetId"));

            // If user not logged in, go to main project page.
            UserItem userItem = getLoggedInUserItem(req);
            if (userItem == null) {
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
                disp.forward(req, resp);
                return;
            }

            if (datasetId == null) {
                logger.info("Dataset id is null, going to home page.");
                jspName = ProjectServlet.SERVLET_NAME;

                logger.info("Going to JSP: " + jspName);
                RequestDispatcher disp = getServletContext().getRequestDispatcher(jspName);
                disp.forward(req, resp);
                return;
            }

            String action = getParameter(req, "customFieldAction");
            if (action != null) {
                if (action.equals("sortCustomFields")) {
                    sortCustomFields(req);
                    redirectToDatasetInfoPage(datasetId, req, resp);
                } else if (action.equals("addCustomField")) {
                    addCustomField(req, resp, datasetId, userItem);
                } else if (action.equals("deleteCustomField")) {
                    deleteCustomField(req, resp, datasetId, userItem);
                } else if (action.equals("getCustomField")) {
                    getCustomField(req, resp, datasetId);
                } else if (action.equals("editCustomField")) {
                    editCustomField(req, resp, datasetId, userItem);
                } else {
                    logger.debug("unrecognized action: " + action);
                    redirectToDatasetInfoPage(datasetId, req, resp);
                }
            } else {
                redirectToDatasetInfoPage(datasetId, req, resp);
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
     * Helper method for sorting custom fields.
     * @param req {@link HttpServletRequest}
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void sortCustomFields(HttpServletRequest req)
        throws IOException, ServletException {

        String sortByKey = (String) req.getParameter("sortBy");

        if (sortByKey != null) {
            CustomFieldContext context = CustomFieldContext.getContext(req);
            context.setSortByColumn(sortByKey, true);
            CustomFieldContext.setContext(req, context);
        }
    }

    /**
     * Helper method for creating a new Custom Field.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param datasetId the dataset id
     * @param userItem the user
     * @throws IOException an IO Exception
     * @throws JSONException a JSON Exception
     */
    private void addCustomField(HttpServletRequest req, HttpServletResponse resp,
                                Integer datasetId, UserItem userItem)
        throws IOException, JSONException {

        String name = getParameter(req, "cfNameField");
        String description = getParameter(req, "cfDescField");
        String level = getParameter(req, "cfLevelSelect");

        String message = null;
        String messageLevel = null;

        if ((name == null) || (name.length() == 0)) {
            message = "The custom field must have a name.";
            messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            name = stripHtml(name).trim();
            if (name.length() == 0) {
                message = "The custom field must cannot contain html.";
                messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_ERROR;
            } else {
                DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                DatasetItem dsItem = dsDao.get(datasetId);
                CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
                CustomFieldItem dupItem = cfDao.findByNameAndDataset(name, dsItem);
                if (dupItem != null) {
                    message = "This name is already taken.";
                    messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_ERROR;
                } else {
                    CustomFieldItem cfItem = new CustomFieldItem();
                    cfItem.setCustomFieldName(stripHtml(name).trim());
                    cfItem.setDataset(dsItem);
                    cfItem.setOwner(userItem);
                    cfItem.setDateCreated(new Date());
                    cfItem.setLevel(level);
                    if (description != null) {
                        cfItem.setDescription(stripHtml(description).trim());
                    }
                    cfDao.saveOrUpdate(cfItem);

                    UserLogger.logCfAdd(dsItem, userItem, (Long)cfItem.getId(),
                            cfItem.getCustomFieldName(), false);

                    message = "Custom Field added.";
                    messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                }
            }
        }

        req.getSession().setAttribute(CF_MESSAGE_ATTR + datasetId, message);
        req.getSession().setAttribute(CF_MESSAGE_LEVEL_ATTR + datasetId, messageLevel);

        writeJSON(resp, json("message", message, "messageLevel", messageLevel));
    }

    /**
     * Helper method for deleting a Custom Field.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param datasetId the dataset id
     * @param userItem the user
     * @throws IOException an IO Exception
     * @throws JSONException a JSON Exception
     */
    private void deleteCustomField(HttpServletRequest req, HttpServletResponse resp,
                                   Integer datasetId, UserItem userItem)
        throws IOException, JSONException {

        String message = null;
        String messageLevel = null;

        CustomFieldItem cfItem = getCustomFieldItem(req);
        if (cfItem == null) {
            message = "Custom Field not found.";
            messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            Long cfId = (Long)cfItem.getId();
            String cfName = cfItem.getCustomFieldName();

            // Clear CustomField values...
            CfTxLevelDao cfTxLevelDao = DaoFactory.DEFAULT.getCfTxLevelDao();

            int numTxns = cfTxLevelDao.clear(cfItem);

            CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
            cfDao.clear(cfItem);

            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem dsItem = dsDao.get(datasetId);

            UserLogger.logCfDelete(dsItem, userItem, cfId, cfName, numTxns, false);

            message = "Custom Field deleted.";
            messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        req.getSession().setAttribute(CF_MESSAGE_ATTR + datasetId, message);
        req.getSession().setAttribute(CF_MESSAGE_LEVEL_ATTR + datasetId, messageLevel);

        writeJSON(resp, json("message", message, "messageLevel", messageLevel));
    }

    /**
     * Helper method to retrieve a specific Custom Field.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param datasetId the dataset id
     * @throws IOException an IO Exception
     * @throws JSONException a JSON Exception
     */
    private void getCustomField(HttpServletRequest req, HttpServletResponse resp, Integer datasetId)
        throws IOException, JSONException {

        CustomFieldItem cfItem = getCustomFieldItem(req);
        if (cfItem == null) {
            writeJSON(resp, json("message", "Failed to find Custom Field.",
                                 "messageLevel", "ERROR"));
        } else {
            // write JSON response
            writeJSON(resp, json("message", "Found Custom Field",
                                 "messageLevel", "SUCCESS",
                                 "datasetId", datasetId,
                                 "id", getIntegerId((String)req.getParameter("customFieldId")),
                                 "name", cfItem.getCustomFieldName(),
                                 "description", cfItem.getDescription(),
                                 "level", cfItem.getLevel(),
                                 "hasData", customFieldHasData(cfItem)));
        }
    }

    /**
     * Helper method for editing a Custom Field.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param datasetId the dataset id
     * @param userItem the user
     * @throws IOException an IO Exception
     * @throws JSONException a JSON Exception
     */
    private void editCustomField(HttpServletRequest req, HttpServletResponse resp,
                                 Integer datasetId, UserItem userItem)
        throws IOException, JSONException {

        String name = getParameter(req, "cfNameField");
        String description = getParameter(req, "cfDescField");

        String message = null;
        String messageLevel = null;

        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dsItem = dsDao.get(datasetId);

        CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
        CustomFieldItem cfItem = getCustomFieldItem(req);
        if (cfItem == null) {
            message = "Custom Field not found.";
            messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if ((name == null) || (name.length() == 0)) {
            message = "The custom field must have a name.";
            messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (!name.equals(cfItem.getCustomFieldName())
                   && (cfDao.findByNameAndDataset(name, dsItem) != null)) {
            message = "This name is already taken.";
            messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {

            // Make note of what has changed.
            boolean nameChanged = false;
            boolean descChanged = false;
            String prevName = cfItem.getCustomFieldName();
            String prevDesc = cfItem.getDescription();

            // Clean-up text before checking for differences...
            name = stripHtml(name).trim();
            description = stripHtml(description).trim();

            if (!name.equals(prevName)) {
                cfItem.setCustomFieldName(name);
                nameChanged = true;
            }
            if (!description.equals(prevDesc)) {
                cfItem.setDescription(description);
                descChanged = true;
            }

            cfItem.setUpdatedBy(userItem);
            cfItem.setLastUpdated(new Date());
            cfDao.saveOrUpdate(cfItem);

            Long cfId = (Long)cfItem.getId();
            if (nameChanged) {
                UserLogger.logCfModifyName(dsItem, userItem, cfId, prevName, name, false);
            }
            if (descChanged) {
                UserLogger.logCfModifyDesc(dsItem, userItem, cfId, name, false);
            }

            message = "Custom Field modified.";
            messageLevel = CustomFieldDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        req.getSession().setAttribute(CF_MESSAGE_ATTR + datasetId, message);
        req.getSession().setAttribute(CF_MESSAGE_LEVEL_ATTR + datasetId, messageLevel);

        writeJSON(resp, json("message", message, "messageLevel", messageLevel));
    }

    /**
     * Helper method for redirecting to the DatasetInfo page.
     * @param datasetId the dataset id
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToDatasetInfoPage(Integer datasetId,
                                           HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        redirectToDatasetInfoPage(datasetId, null, null, req, resp);
    }

    /**
     * Helper method for redirecting to the DatasetInfo page with message info.
     * @param datasetId the dataset id
     * @param message the message to display to the user
     * @param messageLevel the level of the message
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToDatasetInfoPage(Integer datasetId, String message, String messageLevel,
                                           HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        CustomFieldHelper cfHelper = HelperFactory.DEFAULT.getCustomFieldHelper();
        List<CustomFieldDto> cfList =
            cfHelper.getAllCustomFields(datasetId, CustomFieldContext.getContext(req));

        // With the redirect, the info has to be put in the session... request is cleared.

        // Determine if the edit/delete column will be displayed in table...
        Boolean displayEdit = cfHelper.getDisplayEditColumn(cfList, getLoggedInUserItem(req));
        req.getSession().setAttribute(CF_DISPLAY_EDIT_ATTR + datasetId, displayEdit);
        req.getSession().setAttribute(CF_LIST_ATTR + datasetId, cfList);

        if (message != null) {
            req.getSession().setAttribute(CF_MESSAGE_ATTR + datasetId, message);
            req.getSession().setAttribute(CF_MESSAGE_LEVEL_ATTR + datasetId, messageLevel);
        }

        resp.sendRedirect(DatasetInfoReportServlet.SERVLET + "?datasetId=" + datasetId);
    }

    /**
     * Get the Custom Field Item from the database using the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the custom field item from the request parameter
     */
    private CustomFieldItem getCustomFieldItem(HttpServletRequest req) {
        CustomFieldItem cfItem = null;

        Integer cfId = getIntegerId((String)req.getParameter("customFieldId"));
        if (cfId != null) {
            CustomFieldDao cfDao = DaoFactory.DEFAULT.getCustomFieldDao();
            cfItem = cfDao.get(new Long(cfId));
        }

        return cfItem;
    }

    /**
     * Helper method to determine if specified Custom Field has data
     * associated with it.
     *
     * @param cfItem the Custom Field item
     * @return boolean true iff this CustomField has data
     */
    private boolean customFieldHasData(CustomFieldItem cfItem) {
        CustomFieldHelper cfHelper = HelperFactory.DEFAULT.getCustomFieldHelper();
        Integer rowsWithValues = cfHelper.getNumberOfRowsWithValues(cfItem);
        return (rowsWithValues > 0);
    }
}
