/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.auth;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PasswordResetDao;
import edu.cmu.pslc.datashop.item.PasswordResetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet responds to the change password request.
 *
 * @author Alida Skogsholm
 * @version $Revision: 16047 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-04-22 15:53:43 -0400 (Mon, 22 Apr 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PasswordResetServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for the Select Password page. */
    private static final String SELECT_PASSWORD_JSP_FILE = "/password_reset_select_new.jsp";
    /** The JSP name for the Password Changed page. */
    private static final String PASSWORD_CHANGED_JSP_FILE = "/password_changed.jsp";

    /** Change Password session attribute. */
    public static final String GUID = "passwordResetGuid";
    /** Change Password session attribute. */
    public static final String ACCOUNT_ID = "passwordResetAccountId";
    /** Password Reset session attribute. */
    public static final String ERROR_MSG = "passwordResetErrorMsg";

    /** Error message for password reset failure. */
    public static final String ERROR_MSG_RESET_FAILED =
        "An error has occurred resetting your password.";
    /** Error message if GUID has expired (or is not in the URL). */
    public static final String ERROR_MSG_ID_EXPIRED =
        "Your password reset link has expired. Please submit a new request.";
    /** Error message if user-specified id doesn't match db. */
    public static final String ERROR_MSG_ID_MISMATCH =
        "The username you specified does not match your Password Reset request.";
    /** Error message if GUID doesn't match db. */
    public static final String ERROR_MSG_GUID_INVALID =
        "This password reset link is not valid. Please submit a new request.";

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

            boolean changePasswordFlag = false;
            String changePasswordParam = req.getParameter("change_password_flag");
            if (changePasswordParam != null && changePasswordParam.equals("true")) {
                changePasswordFlag = true;
            }

            String guid = (String)req.getParameter("id");
            if (guid != null && guid.trim().length() > 0) {

                String userNameGiven = getParameter(req, "accountID");
                if (userNameGiven == null) {
                    httpSession.setAttribute(GUID, guid);
                    httpSession.setAttribute(AccountProfileServlet.ACCT_PROF_SUCCESS_MSG, null);
                    logDebug("Going to Select a New Password page.");
                    redirect(req, resp, SELECT_PASSWORD_JSP_FILE);
                    return;
                }

                // First, check that GUID is valid.
                PasswordResetItem passwordResetItem = getValidGuid(guid, userNameGiven);
                if (passwordResetItem == null) {
                    httpSession.setAttribute(GUID, null);
                    httpSession.setAttribute(ACCOUNT_ID, null);
                    httpSession.setAttribute(ForgotPasswordServlet.ERROR_MSG,
                                             ERROR_MSG_GUID_INVALID);
                    logDebug("Going to Forgot Password page.");
                    redirect(req, resp, ForgotPasswordServlet.JSP_FILE);
                    return;
                }

                UserItem userItem = passwordResetItem.getUser();
                String accountId = (String)userItem.getId();
                logDebug("accountId: ", accountId);

                // Now, check the username they provided against that in the DB.
                if (!userNameGiven.equals(accountId)) {
                    httpSession.setAttribute(GUID, guid);
                    httpSession.setAttribute(ACCOUNT_ID, null);
                    req.setAttribute(ERROR_MSG, ERROR_MSG_ID_MISMATCH);
                    logDebug("Going to Select a New Password page.");
                    redirect(req, resp, SELECT_PASSWORD_JSP_FILE);
                    return;
                }

                // If request is not used up already and its not expired then allow them to change
                if (passwordResetItem.getConsumedTime() == null
                        && passwordResetItem.getExpirationTime().after(new Date())) {

                    httpSession.setAttribute(GUID, guid);
                    httpSession.setAttribute(ACCOUNT_ID, accountId);

                    if (changePasswordFlag) {
                        String password1 = getParameter(req, "password1");
                        String password2 = getParameter(req, "password2");

                        // Check if passwords match
                        boolean passwordsMatch = true;
                        if (!password1.equals(password2)) {
                            passwordsMatch = false;
                            httpSession.setAttribute(ChangePasswordServlet.NEW_PASSWORD_MSG,
                                    RegistrationInfo.PASSWORD_MISMATCH_MSG);
                        }

                        // Check if new password is valid
                        boolean newPasswordValid =
                                RegistrationInfo.isValidPassword(accountId, password1);
                        if (!newPasswordValid) {
                            httpSession.setAttribute(ChangePasswordServlet.NEW_PASSWORD_MSG,
                                    RegistrationInfo.INVALID_PASSWORD_MSG);
                        }

                        if (passwordsMatch && newPasswordValid) {
                            if (OliUserServices.isOliEnabled()) {
                                if (OliUserServices.resetPassword(accountId, password1)) {
                                    logInfo("Password changed for user ", accountId);
                                } else {
                                    logInfo("Password reset failed for user ", accountId);
                                    req.setAttribute(ERROR_MSG, ERROR_MSG_RESET_FAILED);
                                    logDebug("Going to Select a New Password page with error.");
                                    redirect(req, resp, SELECT_PASSWORD_JSP_FILE);
                                    return;
                                }
                            } else {
                                logInfo("OLI services not available. User: ", accountId);
                            }

                            passwordResetItem.setConsumedTime(new Date());
                            DaoFactory.DEFAULT.getPasswordResetDao()
                                .saveOrUpdate(passwordResetItem);

                            logDebug("Password changed going to Password Changed page.");
                            redirect(req, resp, PASSWORD_CHANGED_JSP_FILE);

                        } else {
                            httpSession.setAttribute(
                                    AccountProfileServlet.ACCT_PROF_SUCCESS_MSG, null);
                            logDebug("Going to Select a New Password page with error message.");
                            redirect(req, resp, SELECT_PASSWORD_JSP_FILE);
                        }
                    } else {
                        httpSession.setAttribute(
                                AccountProfileServlet.ACCT_PROF_SUCCESS_MSG, null);
                        logDebug("Going to Select a New Password page.");
                        redirect(req, resp, SELECT_PASSWORD_JSP_FILE);
                    }
                } else {
                    httpSession.setAttribute(GUID, null);
                    httpSession.setAttribute(ACCOUNT_ID, null);
                    httpSession.setAttribute(ForgotPasswordServlet.ERROR_MSG, ERROR_MSG_ID_EXPIRED);
                    logDebug("Going to Forgot Password page.");
                    redirect(req, resp, ForgotPasswordServlet.JSP_FILE);
                }
            } else {
                httpSession.setAttribute(GUID, null);
                httpSession.setAttribute(ACCOUNT_ID, null);
                httpSession.setAttribute(ForgotPasswordServlet.ERROR_MSG, ERROR_MSG_ID_EXPIRED);
                logDebug("Going to Forgot Password page.");
                redirect(req, resp, ForgotPasswordServlet.JSP_FILE);
            }

        } catch (Exception exception) {
            logger.error("Exception occurred.", exception);
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    } // end doPost

    /**
     * Helper method to determine if the specified GUID can be recreated
     * from the specified user_id and salt and if so, to return the
     * corresponding PasswordResetItem.
     * @param guid the GUID in the URL
     * @param userNameGiven the username specified in password reset
     * @return PasswordResetItem null if not found or invalid GUID
     */
    private PasswordResetItem getValidGuid(String guid, String userNameGiven) {

        PasswordResetDao passwordResetDao = DaoFactory.DEFAULT.getPasswordResetDao();
        PasswordResetItem passwordResetItem = passwordResetDao.get(guid);

        if (passwordResetItem == null) { return null; }

        String userId = (String)passwordResetItem.getUser().getId();
        Long salt = passwordResetItem.getSalt();

        // For backward compatibility...
        if (salt == null) {
            // No way to confirm this. Assume it's valid.
            return passwordResetItem;
        }

        String hash =  DigestUtils.shaHex(userId + salt.toString());
        if(MessageDigest.isEqual(hash.getBytes(), guid.getBytes())) {
            return passwordResetItem;
        } else {
            return null;
        }
    }
}
