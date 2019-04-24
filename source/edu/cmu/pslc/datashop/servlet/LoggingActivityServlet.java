/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.LoggingActivityOverviewReport;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.helper.UserState;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.oli.dao.LogActionDao;
import edu.cmu.pslc.datashop.oli.dao.OliDaoFactory;

/**
 * This servlet responds to Logging Activity Report requests.
 * @author Alida Skogsholm
 * @version $Revision: 10744 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-06 13:06:49 -0500 (Thu, 06 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LoggingActivityServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for just the content of the report. */
    private static final String JSP_FILE = "/logging_activity.jsp";
    /** The message for the user log table when viewing report. */
    private static final String USER_LOG_VIEW_REPORT_INFO =
            "Ignore Dataset Id.  Number of minutes = ";
    /** The message for the user log table when viewing page without report. */
    private static final String USER_LOG_VIEW_PAGE_INFO = "Ignore Dataset Id.";
    /** Default for the number of minutes option. */
    private static final int NUM_MINUTES_DEFAULT = 5;
    /** Session attribute. */
    private static final String LOG_ACT_REQUESTED_FLAG = "logging_activity_requested_flag";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "LoggingActivity";

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     * @see javax.servlet.http.HttpServlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
        //no difference, so just forward the request and response to the post.
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     * @see javax.servlet.http.HttpServlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
        logDebug("doPost begin: ", getDebugParamsString(req));
        try {
            setEncoding(req, resp);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            UserItem userItem = getLoggedInUserItem(req);

            // only get the report if user has the right role
            if (hasLoggingActivityRole(req)) {
                // Get the number of minutes for the report.
                LogActionDao logActionDao = OliDaoFactory.DEFAULT.getLogActionDao();
                int numMinutes = NUM_MINUTES_DEFAULT;

                // First check for the input parameter
                String numMinutesParameter = req.getParameter("logging_activity_num_minutes");
                if (numMinutesParameter != null && numMinutesParameter.compareTo("") != 0) {
                    try {
                        numMinutes = (new Integer(numMinutesParameter)).intValue();
                    } catch (NumberFormatException exception) {
                        numMinutes = NUM_MINUTES_DEFAULT;
                    }
                // Else check for a session attribute
                } else {
                    Integer numMinutesSession =
                        (Integer)req.getSession().getAttribute("logging_activity_num_minutes");
                    if (numMinutesSession != null) {
                        numMinutes = numMinutesSession.intValue();
                    }
                }

                // Get the report.
                LoggingActivityOverviewReport report =
                    logActionDao.getLoggingActivityOverviewReportSP(numMinutes);

                // Put the report in the HTTP session for the JSP.
                req.getSession().setAttribute("logging_activity_overview_report", report);
                req.getSession().setAttribute("logging_activity_num_minutes", numMinutes);

                // Log a user action.  Ignore the dataset id in this case. And be sure to
                // log each call to the report so we can see how often it is used.
                UserLogger.log(null, userItem,
                        UserLogger.VIEW_LOG_ACT_REPORT,
                        USER_LOG_VIEW_REPORT_INFO + numMinutes, false);
            } else {
                String loggingActivityAction = req.getParameter("logging_activity_action");
                if (loggingActivityAction == null) {
                    loggingActivityAction = "";
                }
                if (loggingActivityAction.equals("request")) {
                    String reason = req.getParameter("request_reason");
                    String userEmail = userItem.getEmail();
                    String subject =
                        "Requesting permission to view DataShop Logging Activity Report";
                    StringBuffer message = new StringBuffer();
                    message.append("<br>");
                    message.append("User ");
                    message.append(userItem.getUserName());
                    if (userEmail != null) {
                        message.append(", ");
                        message.append("<a href=\"mailto:");
                        message.append(userEmail);
                        message.append("\">");
                        message.append(userEmail);
                        message.append("</a>");
                        message.append(",");
                    }
                    message.append(" is requesting permission to access the ");
                    message.append("Logging Activity report. ");
                    if (reason != null) {
                        message.append("The reason given is: ");
                        message.append("<br><br>");
                        message.append(reason);
                    }
                    message.append("<br>");

                    sendDataShopHelpEmail(null, subject, message.toString(), userEmail);

                    // Log a user action that the role was requested.
                    String info = "User '" + userItem.getId() + "', Reason '" + reason + "'";
                    UserLogger.log(userItem, UserLogger.LOG_ACT_REQUEST_ROLE_ACTION, info, false);

                    req.getSession().setAttribute(LOG_ACT_REQUESTED_FLAG, true);
                } else if (loggingActivityAction.equals("")) {
                    boolean requestedFlag = UserState.hasRequestedLoggingActivityRole(userItem);
                    req.getSession().setAttribute(LOG_ACT_REQUESTED_FLAG, requestedFlag);

                    // Log a user action that the page was viewed.
                    UserLogger.log(null, userItem,
                                   UserLogger.VIEW_LOG_ACT_PAGE, USER_LOG_VIEW_PAGE_INFO);
                } else {
                    logger.warn("User " + userItem.getId()
                            + " made an invalid request: " + loggingActivityAction);
                }
            }

            // Go to the JSP.
            redirect(req, resp, JSP_FILE);
        } catch (Exception exception) {
            logger.error("Exception occurred.", exception);
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    } // end doPost
}
