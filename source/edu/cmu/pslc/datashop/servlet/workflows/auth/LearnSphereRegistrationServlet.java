/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2018
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.workflows.auth;

import java.io.IOException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.auth.AuthInfo;
import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.auth.LoginInfo;
import edu.cmu.pslc.datashop.servlet.auth.RegistrationInfo;
import edu.cmu.pslc.datashop.servlet.workflows.LearnSphereServlet;

/**
 * Handles the creation of a new LearnSphere account.
 *
 * @version $Revision: 15454 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-08-31 13:00:06 -0400 (Fri, 31 Aug 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereRegistrationServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET_NAME = "ls_registration";

    /** The Registration JSP. */
    public static final String REG_JSP_NAME = "/jsp_workflows/ls_registration.jsp";

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
        try {
            setEncoding(req, resp);
            HttpSession httpSession = req.getSession(true);
            String ipAddress = " [ipaddress:" + req.getRemoteAddr() + "]";

            // set the most recent servlet name for the help page
            setRecentReport(httpSession, SERVLET_NAME);

            getDataShopTermsOfUse(req, null);

            String accountId = getParameter(req, "newAccountId");

            // Get info initialized in LearnSphereLoginServlet...
            LoginInfo loginInfo = (LoginInfo)httpSession.getAttribute("loginInfo");

            if (accountId == null) {
                setUserAndCleanSession(req, null);
                // forward to the JSP (view)
                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(REG_JSP_NAME);
                disp.forward(req, resp);
            } else {
                //check if datashop terms exist at all
                boolean datashopTermsExist = false;
                TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
                TermsOfUseVersionItem versionItem = termsVersionDao.getDataShopTerms(null);
                if (versionItem != null) {
                    datashopTermsExist = true;
                }

                boolean successFlag = false;
                boolean isTouAgreeFlag = false;
                boolean isWebIsoFlag = false;

                // get form parameters
                String webIsoFlag = getParameter(req, "webIsoFlag");
                String firstName = getParameter(req, "firstName");
                String lastName = getParameter(req, "lastName");
                String email = getParameter(req, "email");
                String institution = getParameter(req, "institution");

                if (datashopTermsExist) {
                    String touAgreeFlag = getParameter(req, "agreeCheckbox");
                    if (touAgreeFlag != null && touAgreeFlag.equals("agree")) {
                        isTouAgreeFlag = true;
                    } else {
                        logUserDisagreed(accountId, ipAddress);
                    }
                }

                if (webIsoFlag.equals("true")) {
                    isWebIsoFlag = true;
                }

                // create user item
                UserItem userItem = new UserItem();
                userItem.setId(accountId);
                userItem.setFirstName(firstName);
                userItem.setLastName(lastName);
                userItem.setEmail(email);
                userItem.setInstitution(institution);
                if (loginInfo != null) {
                    userItem.setLoginId(loginInfo.getLoginId());
                    userItem.setLoginType(loginInfo.getLoginType());
                }

                // create registration helper
                RegistrationInfo regInfo = new RegistrationInfo();
                regInfo.setIsLearnSphere(true);
                if (datashopTermsExist) {
                    regInfo.setTouAgreeFlag(isTouAgreeFlag);
                }
                regInfo.setUserItem(userItem);
                regInfo.setWebIso(isWebIsoFlag);

                // check if user exists in the analysis database
                UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                UserItem existingUser = userDao.get(accountId);
                if (existingUser != null) {
                    regInfo.setInvalidAccountId(true);
                    if (logger.isDebugEnabled()) {
                        logger.debug("User account exists: " + accountId);
                    }
                }

                // if everything is valid, then create the OLI user and save the data in the ADB
                if (regInfo.isValid()) {
                    boolean loginSuccessful = false;
                    boolean userCreated = false;
                    boolean oliEnabled = OliUserServices.isOliEnabled();
                    if (oliEnabled) {
                        userCreated = createOliUserAccount(regInfo);
                        if (userCreated) {
                            AuthInfo authInfo =
                                OliUserServices.login(req, resp, accountId, null, isWebIsoFlag);
                            if (authInfo == null) {
                                logger.warn("Failed to login new user: " + accountId);
                                loginSuccessful = false;
                                regInfo.setGeneralFailure(true);
                            } else {
                                logger.debug("OliUserServices.login is successful.");
                                loginSuccessful = true;
                            }
                        } else {
                            logger.warn("Failed to create new user: " + accountId);
                            loginSuccessful = false;
                            regInfo.setGeneralFailure(true);
                        }
                    } else {
                        loginSuccessful = true;
                    }

                    if (loginSuccessful) {
                        userDao.saveOrUpdate(userItem);

                        successFlag = true;
                        setUserAndCleanSession(req, userItem);
                        logger.info("Created new user: " + accountId);

                        agreeToDataShopTerms(userItem);
                        logUserAgreed(userItem, ipAddress);
                    } else {
                        logger.warn("User not created in OLI user DB: " + accountId);
                    }
                } else {
                    logDebug("Registration Helper indicates something is invalid.");
                }

                httpSession.setAttribute("registrationInfo", regInfo);

                // forward to the JSP (view)
                RequestDispatcher disp;
                if (successFlag) {
                    StringBuffer newUrl = new StringBuffer(LearnSphereServlet.SERVLET_NAME);
                    logDebug("Redirecting to " + newUrl.toString());
                    resp.sendRedirect(newUrl.toString());
                } else {
                    disp = getServletContext().getRequestDispatcher(REG_JSP_NAME);
                    disp.forward(req, resp);
                }
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    } //end doPost()

    /**
     * Calls the OliUserServices class to create an OLI user account.
     * @param regHelper the registration helper
     * @return true if the account is created successfully, false otherwise
     */
    private boolean createOliUserAccount(RegistrationInfo regHelper) {
        return OliUserServices.createOliUserAccount(regHelper.getUserItem(),
                                                    null,  // password
                                                    regHelper.isWebIso());
    }

    /**
     * Log to the dataset user log table that the user tried to created an account
     * without agreeing to the terms of use, that is, the user did not check the box.
     * @param username the user name the user filled in
     * @param ipAddress the user's IP address
     */
    private void logUserDisagreed(String username, String ipAddress) {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.findOrCreateDefaultUser();

        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionItem versionItem = termsVersionDao.getDataShopTerms(null);

        if (userItem != null && versionItem != null) {
            UserLogger.log(null, userItem, UserLogger.CREATE_ACCOUNT,
                    "User (" + username + ") did not agree to DataShop terms, version: "
                    + versionItem.getVersion() + " (current)" + ipAddress, false);
        }
    }

    /**
     * Log to the dataset user log table that the user tried to created an account
     * and agreed to the terms of use.
     * @param userItem the user item
     * @param ipAddress the user's IP address
     */
    private void logUserAgreed(UserItem userItem, String ipAddress) {

        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionItem versionItem = termsVersionDao.getDataShopTerms(null);

        String username = (String)userItem.getId();

        if (userItem != null && versionItem != null) {
            UserLogger.log(null, userItem, UserLogger.CREATE_ACCOUNT,
                    "User (" + username + ") agreed to DataShop terms, version: "
                    + versionItem.getVersion() + " (current)" + ipAddress, false);
        }
    }

} // end class LearnSphereRegistrationServlet
