/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.auth.AuthInfo;
import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.util.DataShopInstance;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;

/**
 * Handles the basic log in functionality.
 *
 * @author Benjamin Billings
 * @version $Revision: 15822 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-01-09 16:29:53 -0500 (Wed, 09 Jan 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LoginServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Login JSP name. */
    private static final String LOGIN_JSP_NAME = "/login.jsp";

    /**
     * Handles the HTTP get.
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
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            String accountId = null, password = null;
            boolean loginSuccessful = false;

            boolean isWebIso = isWebIso(req);

            LoginInfo loginInfo = new LoginInfo();
            // Get username and password
            if (isWebIso) {
                accountId = req.getRemoteUser();
                loginInfo.setAccountId(accountId);
                loginInfo.setLoginType("InCommon");
                if (isGoogleSignIn(req)) {
                    loginInfo = validateGoogleUser(req.getParameter("googleAuthCode"));
                    accountId = loginInfo.getAccountId();
                }
                // GitHub and LinkedIn logins are handled in separate servlets.
            } else {
                // get the form parameters from login box/page
                accountId = getAccountId(req);
                password = getPassword(req);
            }

            if (accountId == null) { // If no account id entered, go to login page
                // user hasn't tried to login yet, so login has not failed yet
                logDebug("accountId is null");
                loginInfo.setWebIso(isWebIso);
                loginInfo.setLoginFailed(false);
                httpSession.setAttribute("loginInfo", loginInfo);
                setUserAndCleanSession(req, null);

                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(LOGIN_JSP_NAME);
                disp.forward(req, resp);
                return;
            } else { // account id entered
                // but does the account exist?

                UserItem userItem = null;
                List<UserItem> users =
                    userDao.findByLoginIdAndType(loginInfo.getLoginId(),
                                                 loginInfo.getLoginType());
                if (users.size() == 0) {
                    // Handle the case for existing users which won't have loginId
                    // and loginType in db. We'll update this later.
                    users = userDao.findByIdAndType(accountId, loginInfo.getLoginType());
                    if (users.size() > 0) { userItem = users.get(0); }
                } else {
                    userItem = users.get(0);
                    // They might have chosen to not use email address as id...
                    accountId = (String)userItem.getId();
                }

                loginInfo.setWebIso(isWebIso);
                if (userItem == null) { // account does not exist, go to registration page
                    if (isWebIso) {
                        logger.info("WebISO user logged in, user needs to create account: "
                                + accountId);
                        getDataShopTermsOfUse(req, null);
                        httpSession.setAttribute("loginInfo", loginInfo);
                        setUserAndCleanSession(req, null);
                        RequestDispatcher disp;
                        disp = getServletContext().getRequestDispatcher(
                                RegistrationServlet.REG_JSP_NAME);
                        disp.forward(req, resp);
                        return;
                    } else {
                        logger.info("Login failed! for user: " + accountId);
                        loginInfo.setLoginFailed(true);
                        httpSession.setAttribute("loginInfo", loginInfo);
                        RequestDispatcher disp;
                        disp = getServletContext().getRequestDispatcher(LOGIN_JSP_NAME);
                        disp.forward(req, resp);
                        return;
                    }
                } else { // account exists, so let's log in the user
                    if (OliUserServices.isOliEnabled()) {
                        AuthInfo authInfo = OliUserServices.login(
                                req, resp, accountId, password, isWebIso);
                        String loginException = authInfo.getException();

                        if (loginException != null
                                && loginException.indexOf("UserNotFoundException") >= 0
                                && isWebIso) {
                            logger.warn("Webiso user not found:" + accountId);
                            loginSuccessful = false;
                            setUserAndCleanSession(req, null);

                        } else if (authInfo.getException() != null) {
                            logger.warn("User failed to login: " + accountId);
                            loginSuccessful = false;
                            setUserAndCleanSession(req, null);
                        } else {
                            logger.info("User logged in: " + accountId);
                            loginSuccessful = true;
                            setUserAndCleanSession(req, userItem);
                        }
                    } else {
                        logger.info("OLI services not available, logging in user: "
                                + accountId);
                        loginSuccessful = true;
                        setUserAndCleanSession(req, userItem);
                    }
                    loginInfo.setLoginFailed(!loginSuccessful);
                    httpSession.setAttribute("loginInfo", loginInfo);

                    if (loginSuccessful) {
                        logger.info("Login successful for user: " + accountId);

                        // For backward compatibility, update users' login info
                        // now that we have it and track it.
                        if (userItem.getLoginType() == null) {
                            userItem.setLoginType(loginInfo.getLoginType());
                            userItem.setLoginId(loginInfo.getLoginId());
                            userDao.saveOrUpdate(userItem);
                        }

                        // get the recent datasets for the user
                        setRecentDatasets(httpSession, loginInfo.getAccountId());

                        // log that the user logged in in the dataset_user_log table
                        UserLogger.log(userItem, UserLogger.LOGIN);

                        //forward to the home page (list of projects)
                        // if you forward  to the project servlet, we don't get a new request
                        // with the authenticated user in getUserPrincipal
                        //logDebug("Forwarding to the home page", ProjectServlet.JSP_NAME);
                        //RequestDispatcher disp;
                        //disp = getServletContext().getRequestDispatcher(
                        //       ProjectServlet.SERVLET_NAME);
                        //disp.forward(req, resp);
                        //redirect to the Project Servlet instead
                        String requestUrl = req.getRequestURL().toString();
                        String servlet = req.getServletPath();
                        String newUrl = requestUrl.substring(0, requestUrl.indexOf(servlet));
                        newUrl += ProjectServlet.SERVLET_NAME;

                        // Redirecting to the prior page before logging in.
                        String lastVisitUrl = (String) req.getSession().getAttribute(
                                AccessFilter.LAST_VISIT_URL_KEY);
                        if (lastVisitUrl != null) {
                            newUrl = lastVisitUrl;
                        }

                        logDebug("Redirecting to " + newUrl);
                        resp.sendRedirect(newUrl.toString());
                    } else {
                        logger.info("Login failed! for user: " + accountId);
                        RequestDispatcher disp;
                        disp = getServletContext().getRequestDispatcher(LOGIN_JSP_NAME);
                        disp.forward(req, resp);
                    }
                } // end else account exists
            } // end else account id entered

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    } //end doPost()

    /**
     * Find out whether this is a WebIso log in attempt by looking at the URL.
     * @param req the HTTP Servlet Request
     * @return true if it is WebIso, false otherwise
     */
    private boolean isWebIso(HttpServletRequest req) {
        // Get isWebIso flag
        boolean isWebIso = false;
        StringBuffer url = req.getRequestURL();
        if (url.indexOf("webiso") >= 0) {
            isWebIso = true;
        } else if (isGoogleSignIn(req)) {
            // Overloading webiso variable...
            isWebIso = true;
        } else {
            isWebIso = false;
        }
        logDebug("isWebIso: ", isWebIso);
        return isWebIso;
    }

    /**
     * Helper method for determining if login is from Google.
     * @param req HttpServletRequest
     * @return boolean true iff Google sign-in
     */
    private boolean isGoogleSignIn(HttpServletRequest req) {
        String googleAuthCode = req.getParameter("googleAuthCode");
        if (googleAuthCode != null) return true;
        return false;
    }

    /**
     * Gets the account id from the form parameter.
     * @param req HttpServletRequest
     * @return the account id
     */
    private String getAccountId(HttpServletRequest req) {
        String accountId = req.getParameter("account");
        if (accountId != null) {
            accountId = accountId.trim();
            if (accountId.length() <= 0) {
                accountId = null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("accountId: " + accountId);
            }
        }
        return accountId;
    }

    /**
     * Gets the password from the form parameter.
     * @param req HttpServletRequest
     * @return the password
     */
    private String getPassword(HttpServletRequest req) {
        String password = req.getParameter("password");
        if (password != null) {
            password = password.trim();
        }
        return password;
    }

    private static final String GOOGLE_CLIENT_ID
        = "757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com";
    private static final String GOOGLE_CLIENT_SECRET = "";

    /**
     * Helper method to convert a one-time auth code into a valid Google id.
     * @param authCode the code
     * @return LoginInfo with google user id and email address
     */
    private LoginInfo validateGoogleUser(String authCode) {

        String datashopUrl = getDatashopUrl();

        LoginInfo result = new LoginInfo();
        try {
            GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(
                                                    new NetHttpTransport(),
                                                    JacksonFactory.getDefaultInstance(),
                                                    "https://www.googleapis.com/oauth2/v4/token",
                                                    GOOGLE_CLIENT_ID,
                                                    GOOGLE_CLIENT_SECRET,
                                                    authCode, datashopUrl).execute();

            // Get profile info from ID token
            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();

            result.setAccountId(payload.getEmail());
            result.setEmail(payload.getEmail());
            result.setLoginId(payload.getSubject());
            result.setLoginType("Google");

            logger.debug("Validated login for google user: " + result);
        } catch (IOException ioe) {
            logger.info("Failed to get GoogleAuth: " + ioe);
        }

        return result;
    }

    /**
     * Helper method to get DataShop URL.
     * @return String DataShop URL, instance specific.
     */
    private String getDatashopUrl() {

        String datashopUrl = DataShopInstance.getDatashopUrl();

        // Google API: The redirect_uri cannot have a path, e.g., the "/datashop"
        // at the end of QA URL. Remove the path if it exists.
        try {
            java.net.URI uri = new java.net.URI(datashopUrl);
            String path = uri.getPath();
            if ((path != null) && (path.trim().length() > 0)) {
                datashopUrl = datashopUrl.substring(0, datashopUrl.lastIndexOf(path));
            }
        } catch (Exception e) {
            logger.info("Failed to construct URI: " + e);
        }

        return datashopUrl;
    }
} // end class LoginServlet
