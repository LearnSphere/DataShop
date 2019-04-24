/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PasswordResetDao;
import edu.cmu.pslc.datashop.item.PasswordResetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.MailUtils;

/**
 * Class that was factored out from PasswordResetServlet in order to provide
 * password reset notification functionality to the Manage User (DS1430).
 * @author Young Suk Ahn
 * @version $Revision: 13267 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-05-26 14:29:02 -0400 (Thu, 26 May 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PasswordUtil {
    /** WebISO exception string. */
    public static final String WEBISO_USER_EXCEPTION = "WebISO user.";
    /** Google exception string. */
    public static final String GOOGLE_USER_EXCEPTION = "Google user.";
    /** No e-mail exception string. */
    public static final String NO_EMAIL_EXCEPTION = "User has no associated email.";


    /** The subject of the email message. */
    public static final String EMAIL_SUBJECT =
        "PSLC DataShop Account - Password Reset";

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(PasswordUtil.class.getName());

    /**
     * Creates a password reset item.
     * @param userItem the user item
     * @return the password reset item
     */
    public static PasswordResetItem createPasswordResetItem(UserItem userItem) {
        if (userItem == null) {
            LogUtils.logDebug(logger, "userItem is null ");
            return null;
        }
        LogUtils.logDebug(logger, "userItem is not null: ",
                (String) userItem.getId());
        if (OliUserServices.isOliEnabled()) {
            if (OliUserServices.isWebisoUser((String) userItem.getId())) {
                LogUtils.logDebug(logger,
                        "Unable to reset password for WebISO user: "
                                + (String) userItem.getId());
                throw new IllegalArgumentException(WEBISO_USER_EXCEPTION);
            } else if (isGoogleUser(userItem)) {
                LogUtils.logDebug(logger, "Unable to reset password for Google user: "
                                  + (String)userItem.getId());
                throw new IllegalArgumentException(GOOGLE_USER_EXCEPTION);
            }
        }

        Calendar now = Calendar.getInstance();
        Calendar tomorrow = now;
        tomorrow.add(Calendar.DATE, 1);

        PasswordResetDao passwordResetDao = DaoFactory.DEFAULT
                .getPasswordResetDao();
        String guid = PasswordUtil.generateGuid((String) userItem.getId());
        PasswordResetItem passwordResetItem = new PasswordResetItem(guid);
        passwordResetItem.setUser(userItem);
        passwordResetItem.setRequestedTime(now.getTime());
        passwordResetItem.setExpirationTime(tomorrow.getTime());
        passwordResetDao.saveOrUpdate(passwordResetItem);
        return passwordResetItem;
    }

    /**
     * Notifies a user with the password reset item.
     * @param userItem the user item
     * @param passwordResetItem obtained from passwordReset
     * @param senderEmail AbstractServlet.getEmailAddressDatashopHelp()
     * @param bccEmail AbstractServlet.getEmailAddressDatashopBucket()
     * @param baseUrl AbstractServlet.getBaseUrl
     */
    public static void notifyEmail(UserItem userItem, PasswordResetItem passwordResetItem,
                                   String senderEmail, String bccEmail, String baseUrl) {
        String toEmail = userItem.getEmail();
        if (toEmail != null && toEmail.length() > 0) {
            String fromEmail = senderEmail; // getEmailAddressDatashopHelp();
            String message = PasswordUtil.buildMessage(baseUrl,
                    (String) passwordResetItem.getId(), userItem,
                    passwordResetItem.getExpirationTime(), fromEmail);

            List<String> bccList = new ArrayList<String>();
            bccList.add(bccEmail);

            // Use null "Reply To" address.
            MailUtils.sendEmail(fromEmail, toEmail, null, bccList, EMAIL_SUBJECT, message);

            LogUtils.logDebug(logger, "Email Sent to ", toEmail);
        } else {
            LogUtils.logDebug(logger, "No Email found.");
            throw new IllegalArgumentException(NO_EMAIL_EXCEPTION);
        }
    }

    /**
     * Create the body of th e email message to send to the user requesting a password reset.
     * @param baseUrl the base url
     * @param guid the unique id for this request
     * @param userItem the user item of the requesting user
     * @param expirationDate the date the request expires
     * @param fromEmailAddress the email address of the sender, most likely datashop-help
     * @return the body of the email message
     */
    public static String buildMessage(String baseUrl, String guid, UserItem userItem,
            Date expirationDate, String fromEmailAddress) {
        String url = baseUrl + "/PasswordReset?id=" + guid;

        StringBuffer message = new StringBuffer();
        message.append("<!DOCTYPE html><html lang=\"en\">");
        message.append("<body>");
        message.append("<p>");
        message.append("Dear ");
        message.append(userItem.getName());
        message.append(",");
        message.append("</p><p>");
        message.append("You recently requested a new password for your PSLC DataShop account.");
        message.append("</p><p>");
        message.append("Please click this link to select a new password for your account.");
        message.append("<br>");
        message.append("<a href=\"");
        message.append(url);
        message.append("\">");
        message.append(url);
        message.append("</a>");
        message.append("</p><p>");
        message.append("To protect your privacy, this link will expire on ");
        message.append(getExpirationDateString(expirationDate));
        message.append(".");
        message.append("</p><p>");
        message.append("If you did not request this information, please notify us immediately at ");
        message.append("<a href=\"mailto:");
        message.append(fromEmailAddress);
        message.append("\">");
        message.append(fromEmailAddress);
        message.append("</a>");
        message.append(". We will never ask you for your password via email.");
        message.append("</p><p>");
        message.append("Thanks,");
        message.append("<br>");
        message.append("The DataShop Team");
        message.append("</p>");
        message.append("</body></html>");
        return message.toString();
    }

    /** Format of expiration date string. */
    private static final FastDateFormat DATE_FORMAT =
            FastDateFormat.getInstance("EEEEE, MMMMM dd, yyyy HH:mm a z");

    /**
     * Return the expiration date as a string.
     * Used by both JSP and here for the email message
     * @param expirationDate the date
     * @return a nicely formatted string
     */
    private static String getExpirationDateString(Date expirationDate) {

        String expirationString = " in 24 hours";
        if (expirationDate != null) {
            expirationString = DATE_FORMAT.format(expirationDate);
        }
        return expirationString;
    }

    /** Get a date string to hash. */
    private static final FastDateFormat HASH_FORMAT
        = FastDateFormat.getInstance("yyyymmdd HH:mm:ss");

    /** Max length of the GUID. */
    private static final int GUID_LEN = 40;

    /**
     * Generate a guaranteed unique identifier using the user name and current time.
     * @param username the username of the current user
     * @return the GUID
     */
    private static String generateGuid(String username) {
        String hash =  DigestUtils.shaHex(username + HASH_FORMAT.format(new Date()));
        return hash.substring(0, GUID_LEN);
    }

    /**
     * Return the base URL as a string without the original servlet name.
     * @param req HttpServletRequest.
     * @return the base URL
     */
    protected String getBaseUrl(HttpServletRequest req) {
        String requestUrl = req.getRequestURL().toString();
        String servlet = req.getServletPath();
        return requestUrl.substring(0, requestUrl.indexOf(servlet));
    }

    /** Constant. */
    private static final String GMAIL_DOMAIN = "gmail.com";

    /**
     * Helper function to determine if current user is a Google user.
     * @param currentUser the logged in user
     * @return flag indicating if user is a Google user
     */
    private static Boolean isGoogleUser(UserItem currentUser) {
        if (currentUser == null) { return false; }

        if (((String)currentUser.getId()).indexOf(GMAIL_DOMAIN) > 0) {
            return true;
        }

        return false;
    }
}
