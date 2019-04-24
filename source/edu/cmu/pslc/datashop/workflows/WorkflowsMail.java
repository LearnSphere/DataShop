/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import edu.cmu.pslc.datashop.util.ServerNameUtils;

/**
 * Utility class for sending workflow-related emails.
 *
 * @author Cindy Tipper
 * @version $Revision: $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowsMail {

    /** Thank you message content. */
    public static final String REQUEST_ROLE_THANK_YOU_MESSAGE =
        "Thank you for requesting access to the Workflow tool. "
        + "We will review your request and notify you shortly.";

    /**
     * Helper method to create email Subject for a given Workflow.
     * @param workflowName the Workflow name
     * @return worfklowState the Workflow state
     */
    public static String generateSubject(String workflowName, String workflowState) {

        StringBuffer result = new StringBuffer();
        result.append("Workflow '");
        result.append(workflowName);
        result.append("' completed");

        if (workflowState.equals(WorkflowItem.WF_STATE_ERROR)) {
            result.append(" with errors");
        }

        return result.toString();
    }

    /**
     * Helper method to create email content for a given Workflow.
     * @param userName the user name
     * @param workflowName the name of the workflow
     * @param workflowState the state of the workflow
     * @param workflowId the unique id of the workflow
     * @return String the content
     */
    public static  String generateContent(String userName, String workflowName, String workflowState,
                                          Long workflowId, ErrorMessageMap errorMessageMap) {

        Boolean failure = workflowState.equals(WorkflowItem.WF_STATE_ERROR);

        // In the case of failure, might not be able to rely on state.
        if (!errorMessageMap.isEmpty()) { failure = true; }

        StringBuffer message = new StringBuffer();
        message.append("<!DOCTYPE html><html lang=\"en\"><body>");
        message.append("<p>Dear ");
        message.append(userName);
        message.append(", ");
        message.append("<br/>");
        message.append("<br/>");
        message.append("Workflow '");
        message.append("<a href=\"");
        message.append(ServerNameUtils.getDataShopUrl());
        message.append("/LearnSphere?workflowId=");
        message.append(workflowId);
        message.append("\">");
        message.append(workflowName);
        message.append("</a>");
        message.append("' has completed");
        if (failure) {
            message.append(" with errors. ");
        } else {
            message.append(". ");
        }
        message.append("</p>");

        ErrorMessageMap errorMap = errorMessageMap;
        if (failure && !errorMap.isEmpty()) {
            message.append("<p>");
            message.append("<table style=\"empty-cells:show\" border=\"1\" ");
            message.append("cellpadding=\"6\" cellspacing=\"0\">");
            message.append("<tr>");
            message.append("<td>Component</td>");
            message.append("<td>Errors</td>");
            message.append("</tr>");
            for (String k : errorMap.getErrorMessageMapKeyset()) {
                String idHuman = errorMap.getComponentIdHuman(k);
                message.append("<tr>");
                message.append("<td>").append(idHuman).append("</td>");
                message.append("<td>");
                for (String m : errorMap.getErrorMessageMap(k)) {
                    message.append(m);
                    message.append("<br/>");
                }
                message.append("</td>");
                message.append("</tr>");
            }
            message.append("</table>");
            message.append("</p>");
        }

        message.append("Thank you,");
        message.append("<br/>");
        message.append("The DataShop Team");
        message.append("<br/>");
        message.append("</body></html>");

        return message.toString();
    }

    /**
     * Create role request message.
     * @param userName the user id
     * @param userEmail the user email
     * @param reason the reason
     */
    public static String getRoleMessage(String userName, String userEmail, String reason) {

        StringBuffer message = new StringBuffer();
        message.append("<br>");
        message.append("User ");
        message.append(userName);
        if (userEmail != null) {
            message.append(", ");
            message.append("<a href=\"mailto:");
            message.append(userEmail);
            message.append("\">");
            message.append(userEmail);
            message.append("</a>");
            message.append(",");
        }
        message.append(" is requesting permission to use the Workflows tool. ");
        if (reason != null) {
            message.append("The reason given is: ");
            message.append("<br><br>");
            message.append(reason);
        }
        message.append("<br>");
        return message.toString();
    }

}
