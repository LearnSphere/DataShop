/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet responds to the change password request.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8426 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-12-14 14:26:17 -0500 (Fri, 14 Dec 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ChangePasswordServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for just the content of the report. */
    private static final String JSP_FILE = "/change_password.jsp";

    /** Change Password session attribute. */
    public static final String CURRENT_PASSWORD_MSG = "changePasswordCurrentPasswordMsg";
    /** Change Password session attribute. */
    public static final String NEW_PASSWORD_MSG = "changePasswordNewPasswordMsg";

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

            HttpSession httpSession = req.getSession(true);
            UserItem userItem = getLoggedInUserItem(req);

            boolean changePasswordFlag = false;
            String changePasswordParam = req.getParameter("change_password_flag");
            if (changePasswordParam != null && changePasswordParam.equals("true")) {
                changePasswordFlag = true;
            }

            if (changePasswordFlag) {
                String accountId = (String)userItem.getId();
                String password0 = getParameter(req, "password0");
                String password1 = getParameter(req, "password1");
                String password2 = getParameter(req, "password2");

                // Check if current password is valid
                boolean currentPasswordValid = false;
                if (OliUserServices.isOliEnabled()) {
                    currentPasswordValid = OliUserServices.checkPassword(req, password0);
                    logInfo("Current password is valid for user ", accountId);
                } else {
                    currentPasswordValid = true;
                }
                if (!currentPasswordValid) {
                    httpSession.setAttribute(CURRENT_PASSWORD_MSG,
                            RegistrationInfo.PASSWORD_MISMATCH_MSG);
                }

                // Check if passwords match
                boolean passwordsMatch = true;
                if (!password1.equals(password2)) {
                    passwordsMatch = false;
                    httpSession.setAttribute(NEW_PASSWORD_MSG,
                            RegistrationInfo.PASSWORD_MISMATCH_MSG);
                }

                // Check if new password is valid
                boolean newPasswordValid = RegistrationInfo.isValidPassword(accountId, password1);
                if (!newPasswordValid) {
                    httpSession.setAttribute(NEW_PASSWORD_MSG,
                            RegistrationInfo.INVALID_PASSWORD_MSG);
                }

                if (currentPasswordValid && passwordsMatch && newPasswordValid) {
                    if (OliUserServices.isOliEnabled()) {
                        if (OliUserServices.changePassword(req, password1)) {
                            logInfo("Password changed for user ", accountId);
                        } else {
                            logInfo("Password change failed for user ", accountId);
                            req.setAttribute(AccountProfileServlet.ACCT_PROF_ERROR_MSG,
                                    AccountProfileServlet.ACCT_PROF_CHANGE_FAILED);
                            logDebug("Going to Account Profile page with error.");
                            redirect(req, resp, AccountProfileServlet.JSP_FILE);
                            return;
                        }
                    } else {
                        logInfo("OLI services not available. User: ", accountId);
                    }

                    httpSession.setAttribute(
                            AccountProfileServlet.ACCT_PROF_SUCCESS_MSG,
                            AccountProfileServlet.PASSWORD);
                    logDebug("Password changed going back to Account Profile page.");
                    redirect(req, resp, AccountProfileServlet.JSP_FILE);

                } else {
                    httpSession.setAttribute(
                            AccountProfileServlet.ACCT_PROF_SUCCESS_MSG, null);
                    logDebug("Going to Change Password page.");
                    redirect(req, resp, JSP_FILE);
                }

            } else {
                httpSession.setAttribute(CURRENT_PASSWORD_MSG, null);
                httpSession.setAttribute(NEW_PASSWORD_MSG, null);
                httpSession.setAttribute(
                        AccountProfileServlet.ACCT_PROF_SUCCESS_MSG, null);
                logDebug("Going to Change Password page.");
                redirect(req, resp, JSP_FILE);
            }

        } catch (Exception exception) {
            logger.error("Exception occurred.", exception);
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    } // end doPost
}
