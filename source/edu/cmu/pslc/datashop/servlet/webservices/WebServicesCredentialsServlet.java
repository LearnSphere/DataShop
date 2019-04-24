/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.helper.UserState;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet responds to web application's request for the Web Services Credentials page.
 * @author Alida Skogsholm
 * @version $Revision: 10744 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-06 13:06:49 -0500 (Thu, 06 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WebServicesCredentialsServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for just the content of the report. */
    private static final String JSP_FILE = "/web_services.jsp";

    /** The message for the user log table. */
    private static final String USER_LOG_INFO = "Ignore Dataset Id.";

    /** Session attribute. */
    private static final String WEB_SERV_REQUESTED_FLAG = "web_services_requested_flag";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "WebServicesCredentials";

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
        logDebug("doPost begin :: ", getDebugParamsString(req));
        try {
            setEncoding(req, resp);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            UserItem userItem = getLoggedInUserItem(req);
            String webServicesAction = req.getParameter("web_services_action");
            if (webServicesAction == null) {
                webServicesAction = "";
            }

            // only get the report if user has the right role
            if (hasWebServicesRole(req)) {
                if (webServicesAction.equals("create_key")) {
                    userItem.updateAuthenticationCredentials();
                    UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                    userDao.saveOrUpdate(userItem);
                    req.getSession().setAttribute(USER_SESSION_KEY, userItem);

                    // Log a user action that a new key was created.
                    UserLogger.log(null, userItem,
                            UserLogger.WEB_SERV_CREATE_KEY_ACTION, USER_LOG_INFO);

                } else if (webServicesAction.equals("")) {
                    // Log a user action that the page was viewed.
                    UserLogger.log(null, userItem,
                                   UserLogger.VIEW_WEB_SERV_CRED, USER_LOG_INFO);
                } else {
                    logger.warn("User " + userItem.getId()
                            + " made an invalid request: " + webServicesAction);
                }
            } else {
                if (webServicesAction.equals("request")) {
                    String reason = req.getParameter("request_reason");
                    String userEmail = userItem.getEmail();
                    String subject = "Requesting permission to use DataShop web services";
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
                    message.append(" is requesting permission to use DataShop web services. ");
                    if (reason != null) {
                        message.append("The reason given is: ");
                        message.append("<br><br>");
                        message.append(reason);
                    }
                    message.append("<br>");

                    sendDataShopHelpEmail(null, subject, message.toString(), userEmail);

                    // Log a user action that the role was requested.
                    String info = "User '" + userItem.getId() + "', Reason '" + reason + "'";
                    UserLogger.log(userItem, UserLogger.WEB_SERV_REQUEST_ROLE_ACTION, info, false);

                    req.getSession().setAttribute(WEB_SERV_REQUESTED_FLAG, true);
                } else if (webServicesAction.equals("")) {
                    boolean requestedFlag = UserState.hasRequestedWebServicesRole(userItem);
                    req.getSession().setAttribute(WEB_SERV_REQUESTED_FLAG, requestedFlag);

                    // Log a user action that the page was viewed.
                    UserLogger.log(null, userItem,
                                   UserLogger.VIEW_WEB_SERV_CRED, USER_LOG_INFO);
                } else {
                    logger.warn("User " + userItem.getId()
                            + " made an invalid request: " + webServicesAction);
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
