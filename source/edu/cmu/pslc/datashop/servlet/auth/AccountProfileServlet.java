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
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet responds to web application's request for the Web Services Credentials page.
 * @author Alida Skogsholm
 * @version $Revision: 14806 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-02-08 15:28:49 -0500 (Thu, 08 Feb 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccountProfileServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for just the content of the report. */
    static final String JSP_FILE = "/account_profile.jsp";

    /** Action names to decide which operation to execute. **/
    public static final String SERVLET_NAME = "AccountProfile";

    /** Account Profile Attribute. */
    public static final String ACCT_PROF_EDIT_MODE = "accountProfileEditMode";
    /** Account Profile Attribute. */
    public static final String ACCT_PROF_REG_INFO = "accountProfileRegInfo";
    /** Account Profile Attribute. */
    public static final String ACCT_PROF_SUCCESS_MSG = "accountProfileSuccessFlag";
    /** Account Profile Session Attribute. */
    public static final String ACCT_PROF_ERROR_MSG = "accountProfileErrorMsg";
    /** Constant for the ACCT_PROF_SUCCESS_MSG to show the profile success message. */
    public static final String PROFILE = "profile";
    /** Constant for the ACCT_PROF_SUCCESS_MSG to show the password success message. */
    public static final String PASSWORD = "password";

    /** Constant for the password change failure message. */
    public static final String ACCT_PROF_CHANGE_FAILED =
            "An error has occurred changing your password.";

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

            // Set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            HttpSession httpSession = req.getSession(true);
            UserItem userItem = getLoggedInUserItem(req);

            boolean editFlag = false;
            boolean editMode = false;
            String editModeParam = req.getParameter("edit_mode");
            if (editModeParam != null && editModeParam.equals("true")) {
                editMode = true;
            }

            if (!editMode) {
                logDebug("!editMode");
                String editProfileFlag = getParameter(req, "edit_profile_flag");
                if (editProfileFlag != null) {
                    logger.debug("Edit Profile Mode");
                    String firstName = getParameter(req, "firstName");
                    String lastName = getParameter(req, "lastName");
                    String email = getParameter(req, "email");
                    String institution = getParameter(req, "institution");
                    String userAlias = getParameter(req, "userAlias");
                    if (userAlias.trim().equals("")) { userAlias = null; }

                    userItem.setFirstName(firstName);
                    userItem.setLastName(lastName);
                    userItem.setEmail(email);
                    userItem.setInstitution(institution);
                    userItem.setUserAlias(userAlias);
                    editFlag = true;
                } else {
                    logger.debug("View Only Mode");
                }
            } else {
                logDebug("editMode");
                logger.debug("Edit Mode");
            }

            // create registration helper
            RegistrationInfo regInfo = new RegistrationInfo();
            regInfo.setUserItem(userItem);
            boolean isWebIsoFlag = false;

            if (OliUserServices.isOliEnabled()) {
                isWebIsoFlag = OliUserServices.isWebisoAccount(req);
            }
            regInfo.setWebIso(isWebIsoFlag);

            httpSession.setAttribute(ACCT_PROF_REG_INFO, regInfo);
            if (regInfo.isValid()) {
                logDebug("regInfo.isValid");
                httpSession.setAttribute(ACCT_PROF_EDIT_MODE, editMode);
                if (editFlag) {
                    logDebug("editFlag");
                    UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                    userDao.saveOrUpdate(userItem);
                    httpSession.setAttribute(ACCT_PROF_SUCCESS_MSG, PROFILE);
                } else {
                    logDebug("!editFlag");
                    httpSession.setAttribute(ACCT_PROF_SUCCESS_MSG, null);
                }
            } else {
                logDebug("!regInfo.isValid");
                httpSession.setAttribute(ACCT_PROF_EDIT_MODE, true);
                httpSession.setAttribute(ACCT_PROF_SUCCESS_MSG, null);
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
