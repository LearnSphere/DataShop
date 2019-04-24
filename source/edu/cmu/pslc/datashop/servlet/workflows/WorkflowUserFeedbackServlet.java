/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.IOException;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.WorkflowUserFeedbackDao;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.util.ServerNameUtils;

import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowUserFeedbackItem;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * Class that supports WorkflowUserFeedback operations.
 * @author Cindy Tipper
 * @version $Revision: 15844 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-05 10:13:52 -0500 (Tue, 05 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowUserFeedbackServlet extends AbstractServlet {

    /** Serial Version UID. */
    private static final long serialVersionUID = -2L;

    /** Date format specification. */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MM/dd/yyyy");

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

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
     * Process post method.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        logDebug("doPost begin :: ", getDebugParamsString(req));

        UserItem userItem = getLoggedInUserItem(req);
        DatasetItem datasetItem = getDatasetItem(req);
        WorkflowItem workflowItem = getWorkflowItem(req);

        // Allow users not logged in to leave comments too.
        if (userItem == null) {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            userItem = userDao.get(UserItem.DEFAULT_USER);
        }

        String feedback = getFeedback(req);

        if ((feedback != null) && (feedback.length() > 0)) {

            // Create WorkflowUserResponseItem and persist.
            WorkflowUserFeedbackItem item = new WorkflowUserFeedbackItem();
            Date now = new Date();
            item.setDate(now);
            item.setUser(userItem);
            item.setFeedback(feedback);
            if (datasetItem != null) { item.setDataset(datasetItem); }
            if (workflowItem != null) { item.setWorkflow(workflowItem); }

            WorkflowUserFeedbackDao wufDao = DaoFactory.DEFAULT.getWorkflowUserFeedbackDao();
            wufDao.saveOrUpdate(item);

            String subject = "A user has submitted Tigris feedback.";
            StringBuffer message = new StringBuffer();
            message.append("<!DOCTYPE html><html lang=\"en\"><body>");
            message.append("<p>User ");
            if (userItem.getEmail() != null) {
                message.append("<a href=\"mailto:");
                message.append(userItem.getEmail());
                message.append("\">");
                message.append(userItem.getUserName());
                message.append("</a> has submitted feedback");
            } else {
                message.append(userItem.getUserName());
                message.append(" has submitted feedback");
            }
            if (workflowItem != null) {
                message.append(" for workflow '");
                message.append("<a href=\"");
                message.append(ServerNameUtils.getDataShopUrl());
                message.append("/LearnSphere?workflowId=");
                message.append(workflowItem.getId());
                message.append("\">");
                message.append(workflowItem.getWorkflowName());
                message.append("</a>'.");
            } else {
                message.append(".");
            }
            message.append("<br/>");
            message.append("<br/>");
            message.append("<span style=\"font-style:italic\">");
            message.append(feedback);
            message.append("</span>");

            message.append("</p>");
            message.append("</body></html>");

            sendEmail(getEmailAddressDatashopHelp(), getEmailAddressDatashopHelp(),
                      subject, message.toString());

            try {
                writeJSON(resp,
                          json("feedback", feedback,
                               "date", now));
                return;
            } catch (JSONException e) {
                logDebug("Error writing JSON response.");
                return;
            }
        } else {
            logDebug("Feedback empty.");
        }

        logDebug("doPost end");
    }

    /**
     * Gets the dataset from the request.
     * @param req HttpServletRequest
     * @return the DatasetItem, null if dataset not specified
     */
    private DatasetItem getDatasetItem(HttpServletRequest req) {
        String datasetIdStr = req.getParameter("datasetId");

        DatasetItem result = null;
        if ((datasetIdStr != null) && datasetIdStr.matches("\\d+")) {
            Integer datasetId = Integer.parseInt(datasetIdStr);
            result = DaoFactory.DEFAULT.getDatasetDao().get(datasetId);
        }

        return result;
    }

    /**
     * Gets the workflow from the request.
     * @param req HttpServletRequest
     * @return the WorkflowItem, null if workflow not specified
     */
    private WorkflowItem getWorkflowItem(HttpServletRequest req) {
        String workflowIdStr = req.getParameter("workflowId");

        WorkflowItem result = null;
        if ((workflowIdStr != null) && workflowIdStr.matches("\\d+")) {
            Long workflowId = Long.parseLong(workflowIdStr);
            result = DaoFactory.DEFAULT.getWorkflowDao().get(workflowId);
        }

        return result;
    }

    /**
     * Gets the feedback from the request.
     * @param req HttpServletRequest
     * @return the feedback, null if not specified
     */
    private String getFeedback(HttpServletRequest req) {
        String feedback = req.getParameter("feedback");

        if (feedback != null) { feedback = feedback.trim(); }

        return feedback;
    }
}
