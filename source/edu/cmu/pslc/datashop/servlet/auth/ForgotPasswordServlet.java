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

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PasswordResetDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.item.PasswordResetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This servlet responds to the forgot password request.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13267 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-05-26 14:29:02 -0400 (Thu, 26 May 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ForgotPasswordServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for just the content of the report. */
    static final String JSP_FILE = "/forgot_password.jsp";

    /** The JSP name for the Email Sent page. */
    private static final String EMAIL_SENT_JSP_FILE = "/password_reset_email_sent.jsp";

    /** The JSP name for the failure to reset WebISO password page. */
    private static final String RESET_WEBISO_JSP_FILE = "/password_reset_webiso.jsp";

    /** The JSP name for the failure to reset Google password page. */
    private static final String RESET_GOOGLE_JSP_FILE = "/password_reset_google.jsp";

    /** Change Password session attribute. */
    public static final String EXPIRATION_DATE = "passwordResetExpirationDate";

    /** Change Password session attribute. */
    public static final String ERROR_MSG = "forgotPasswordErrorMsg";

    /** Error message for no accounts found. */
    public static final String ERROR_MSG_NO_ACCTS =
        "No accounts match the information you entered.";

    /** Error message for multiple accounts found. */
    public static final String ERROR_MSG_MULT_ACCTS =
        "More than one account matches the information you entered. Please "
        + "<a href=\"help?page=contact\">contact us</a> to reset your password.";

    /** Error message for entering just the last name. */
    public static final String ERROR_MSG_JUST_LAST_NAME =
        "Unable to locate your account with just the last name. "
        + "Please enter your email address as well.";

    /** Error message for entering just the last name. */
    public static final String ERROR_MSG_JUST_EMAIL =
        "Unable to locate your account with just the email address. "
        + "Please enter your last name as well.";

    /** Error message for multiple accounts found. */
    public static final String ERROR_MSG_NO_EMAIL =
        "There is no email address associated with your account.";

    /** The subject of the email message. */
    public static final String EMAIL_SUBJECT =
        "PSLC DataShop Account - Password Reset";

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
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem userItem = null;

            String username = req.getParameter("username");
            if (username != null && username.trim().length() > 0) {
                userItem = userDao.get(username);
                if (userItem == null) {
                    httpSession.setAttribute(ERROR_MSG, ERROR_MSG_NO_ACCTS);
                }
            } else {
                String lastName = req.getParameter("lastName");
                String email = req.getParameter("email");
                if (lastName != null && lastName.trim().length() > 0
                        && email != null && email.trim().length() > 0) {
                    List<UserItem> userItemList = userDao.findByLastNameAndEmail(lastName, email);
                    int numUserItemsFound = userItemList.size();
                    if (numUserItemsFound == 0) {
                        httpSession.setAttribute(ERROR_MSG, ERROR_MSG_NO_ACCTS);
                    } else if (numUserItemsFound > 1) {
                        httpSession.setAttribute(ERROR_MSG, ERROR_MSG_MULT_ACCTS);
                    } else {
                        httpSession.setAttribute(ERROR_MSG, null);
                        userItem = userItemList.get(0);
                    }
                } else if (lastName != null && lastName.trim().length() > 0
                        && email != null && email.trim().length() == 0) {
                    httpSession.setAttribute(ERROR_MSG, ERROR_MSG_JUST_LAST_NAME);
                } else if (lastName != null && lastName.trim().length() == 0
                        && email != null && email.trim().length() > 0) {
                    httpSession.setAttribute(ERROR_MSG, ERROR_MSG_JUST_EMAIL);
                } else {
                    httpSession.setAttribute(ERROR_MSG, null);
                }
            }

            if (userItem != null) {
                logDebug("userItem is not null: ", (String)userItem.getId());
                if (OliUserServices.isOliEnabled()) {
                    if (isGoogleUser(userItem)) {
                        logDebug("Unable to reset password for Google user: "
                                + (String)userItem.getId());
                        logDebug("Going to Reset Google page.");
                        redirect(req, resp, RESET_GOOGLE_JSP_FILE);
                        return;
                    } else if (OliUserServices.isWebisoUser((String)userItem.getId())) {
                        logDebug("Unable to reset password for WebISO user: "
                                + (String)userItem.getId());
                        logDebug("Going to Reset WebISO page.");
                        redirect(req, resp, RESET_WEBISO_JSP_FILE);
                        return;
                    }
                }

                Calendar now = Calendar.getInstance();
                Calendar tomorrow = now;
                tomorrow.add(Calendar.DATE, 1);
                Date expirationDate = tomorrow.getTime();
                httpSession.setAttribute(EXPIRATION_DATE, expirationDate);

                PasswordResetDao passwordResetDao = DaoFactory.DEFAULT.getPasswordResetDao();
                PasswordResetItem passwordResetItem =
                    generatePasswordResetItem(userItem, now, tomorrow);
                passwordResetDao.saveOrUpdate(passwordResetItem);

                String guid = (String)passwordResetItem.getId();

                if (isSendmailActive()) {
                    String toEmail = userItem.getEmail();
                    if (toEmail != null && toEmail.length() > 0) {
                        String fromEmail = getEmailAddressDatashopHelp();
                        String message = PasswordUtil.buildMessage(getBaseUrl(req), guid, userItem,
                                                                   expirationDate, fromEmail);
                        sendEmail(fromEmail, toEmail, EMAIL_SUBJECT, message);
                        logDebug("Going to Email Sent page.");
                        redirect(req, resp, EMAIL_SENT_JSP_FILE);
                    } else {
                        httpSession.setAttribute(ERROR_MSG, ERROR_MSG_NO_EMAIL);
                        logDebug("Going to Forgot Password page.");
                        redirect(req, resp, JSP_FILE);
                    }
                } else {
                    logger.info("Not sending email as isSendmailActive is false.");
                    logDebug("Going to Email Sent page.");
                    redirect(req, resp, EMAIL_SENT_JSP_FILE);
                }
            } else {
                logDebug("userItem is null: " + username, ". Going to Forgot Password page.");
                redirect(req, resp, JSP_FILE);
            }
        } catch (Exception exception) {
            logger.error("Exception occurred.", exception);
        } finally {
            logDebug("doPost end");
        }
    } // end doPost

    /** Format of expiration date string. */
    private static final FastDateFormat DATE_FORMAT =
            FastDateFormat.getInstance("EEEEE, MMMMM dd, yyyy HH:mm a z");

    /**
     * Return the expiration date as a string.
     * Used by both JSP and here for the email message
     * @param expirationDate the date
     * @return a nicely formatted string
     */
    private String getExpirationDateString(Date expirationDate) {

        String expirationString = " in 24 hours";
        if (expirationDate != null) {
            expirationString = DATE_FORMAT.format(expirationDate);
        }
        return expirationString;
    }

    /** Get a date string to hash. */
    private static final FastDateFormat HASH_FORMAT =
            FastDateFormat.getInstance("yyyymmdd HH:mm:ss");

    /** Max length of the GUID. */
    private static final int GUID_LEN = 40;

    /**
     * Generate a PasswordResetItem, with a guaranteed unique identifier using
     * the user name and a random salt.
     * @param userItem the UserItem of the current user
     * @param now the current Calendar instance
     * @param tomorrow 24 hours from now
     * @return the item
     */
    private PasswordResetItem generatePasswordResetItem(UserItem userItem,
                                                        Calendar now, Calendar tomorrow) {

        Random rand = new Random();
        Long salt = rand.nextLong();
        String hash =  DigestUtils.shaHex((String)userItem.getId() + salt.toString());
        String guid = hash.substring(0, GUID_LEN);

        PasswordResetItem result = new PasswordResetItem(guid);
        result.setUser(userItem);
        result.setSalt(salt);
        result.setRequestedTime(now.getTime());
        result.setExpirationTime(tomorrow.getTime());

        return result;
    }

    /** Constant. */
    private static final String GMAIL_DOMAIN = "gmail.com";

    /**
     * Helper function to determine if current user is a Google user.
     * @param currentUser the logged in user
     * @return flag indicating if user is a Google user
     */
    private Boolean isGoogleUser(UserItem currentUser) {
        if (currentUser == null) { return false; }

        if (((String)currentUser.getId()).indexOf(GMAIL_DOMAIN) > 0) {
            return true;
        }

        return false;
    }
}

