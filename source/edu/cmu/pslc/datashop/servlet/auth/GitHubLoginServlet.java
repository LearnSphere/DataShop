/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2017
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import edu.cmu.pslc.datashop.servlet.workflows.LearnSphereServlet;
import edu.cmu.pslc.datashop.servlet.workflows.auth.LearnSphereRegistrationServlet;
import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * Handles GitHub login, both DataShop and LearnSphere.
 * This is necessary because GitHub OAuth has strange restrictions on
 * the redirect URL. Without this servlet, we'd need two separate OAuth
 * apps on GitHub for each DataShop instance. Bad enough we need one
 * for each instance...
 *
 * @author Cindy Tipper
 * @version $Revision: 16047 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-04-22 15:53:43 -0400 (Mon, 22 Apr 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class GitHubLoginServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** DataShop login jsp. */
    private static final String DATASHOP_LOGIN_JSP = "/login.jsp";

    /** Tigris login jsp. */
    private static final String LEARNSPHERE_LOGIN_JSP = "/jsp_workflows/ls_login.jsp";

    /** DataShop registration jsp. */
    private static final String DATASHOP_REG_JSP = RegistrationServlet.REG_JSP_NAME;

    /** Tigris registration jsp. */
    private static final String LEARNSPHERE_REG_JSP = LearnSphereRegistrationServlet.REG_JSP_NAME;

    /** Session attribute to indicate type of login. */
    public static final String IS_TIGRIS_ATTR = "is_tigris";

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
        throws IOException, ServletException
    {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        try {

            Boolean isTigris = false;

            HttpSession httpSession = req.getSession(true);
            if (httpSession.getAttribute(IS_TIGRIS_ATTR) != null) {
                String isTigrisStr = (String)httpSession.getAttribute(IS_TIGRIS_ATTR);
                if (isTigrisStr.equalsIgnoreCase("true")) {
                    isTigris = true;
                }
            }
            String jspName = isTigris ? LEARNSPHERE_LOGIN_JSP : DATASHOP_LOGIN_JSP;
            String regJspName = isTigris ? LEARNSPHERE_REG_JSP : DATASHOP_REG_JSP;
            String loginInfoAttr = "loginInfo";

            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            boolean loginSuccessful = false;
            String password = null;
            LoginInfo loginInfo = gitHubLogin(req.getParameter("code"));
            String accountId = loginInfo.getAccountId();

            // If null, the GitHub login failed.
            if (accountId == null) {
                logDebug("accountId is null; GitHub login failed");

                loginInfo.setWebIso(true);
                loginInfo.setLoginFailed(true);
                httpSession.setAttribute(loginInfoAttr, loginInfo);
                setUserAndCleanSession(req, null);

                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(jspName);
                disp.forward(req, resp);
        return;
            }

            // account id entered
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
                // We might have guessed wrong about the accountId...
                accountId = (String)userItem.getId();
            }

            if (userItem == null) { // account does not exist, go to registration page
                logger.info("GitHub user logged in, user needs to create account: "
                            + accountId);
                getDataShopTermsOfUse(req, null);
                httpSession.setAttribute(loginInfoAttr, loginInfo);
                setUserAndCleanSession(req, null);
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(regJspName);
                disp.forward(req, resp);
                return;
            } else {
                // User exists... log in.
                if (OliUserServices.isOliEnabled()) {
                    AuthInfo authInfo =
                        OliUserServices.login(req, resp, accountId, password, true);
                    String loginException = authInfo.getException();

                    if (loginException != null
                        && loginException.indexOf("UserNotFoundException") >= 0) {
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
            }

            loginInfo.setLoginFailed(!loginSuccessful);
            httpSession.setAttribute(loginInfoAttr, loginInfo);

            if (loginSuccessful) {
                logger.info("Login successful for user: " + accountId);

                // For backward compatibility, update users' login info
                // now that we have it and track it.
                if (userItem.getLoginType() == null) {
                    userItem.setLoginType(loginInfo.getLoginType());
                    userItem.setLoginId(loginInfo.getLoginId());
                    userDao.saveOrUpdate(userItem);
                }

                StringBuffer newUrl = null;
                if (isTigris) {
                    // log that the user logged in in the dataset_user_log table
                    UserLogger.log(userItem, UserLogger.WORKFLOWS_LOGIN);

                    // forward to appropriate LearnSphere...
                    Long workflowId =
                        (Long)req.getSession().getAttribute("workflowId");

                    newUrl = new StringBuffer(LearnSphereServlet.SERVLET_NAME);
                    if (workflowId != null) {
                        newUrl.append("?workflowId=" + workflowId);
                    }

                } else {
                    String requestUrl = req.getRequestURL().toString();
                    String servlet = req.getServletPath();
                    newUrl = new StringBuffer(requestUrl.substring(0, requestUrl.indexOf(servlet)));
                    newUrl.append(ProjectServlet.SERVLET_NAME);
                }

                // Redirecting to the prior page before logging in.
                String lastVisitUrl = (String) req.getSession().getAttribute(AccessFilter.LAST_VISIT_URL_KEY);

                String redirectUrl = null;
                if (lastVisitUrl != null) {
                    redirectUrl = lastVisitUrl;
                } else {
                    redirectUrl = newUrl.toString();
                }

                logDebug("Redirecting to " + redirectUrl);
                resp.sendRedirect(redirectUrl);

            } else {
                logger.info("Login failed! for user: " + accountId);
                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(jspName);
                disp.forward(req, resp);
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    } //end doPost()

    // Default GitHub info. Used if DataShopInstance not initialized.
    private static final String GITHUB_CLIENT_ID = "22cdd8a6fc50622cf71c";
    private static final String GITHUB_CLIENT_SECRET
        = "d3ae23706512605f4bb51f7b5c98952cf55d1791";


    /**
     * Helper method to start OAuth handshake with GitHub for login.
     * @param code the GitHub response to initiating login
     * @return the user's LoginInfo
     */
    private LoginInfo gitHubLogin(String code) {

        logger.debug("GitHub login: code = " + code);
        LoginInfo result = new LoginInfo();

        String datashopUrl = getDatashopUrl();
        String githubClientId = DataShopInstance.getGithubClientId();
        String githubClientSecret = DataShopInstance.getGithubClientSecret();

        // Sigh. If not specified, default to localhost values.
        if (githubClientId == null) { githubClientId = GITHUB_CLIENT_ID; }
        if (githubClientSecret == null) { githubClientSecret = GITHUB_CLIENT_SECRET; }
        if (!githubClientId.matches(STRICT_CHARS)) { githubClientId = ""; }
        if (!githubClientSecret.matches(STRICT_CHARS)) { githubClientSecret = ""; }
        if (code != null && !code.matches(STRICT_CHARS)) { code = ""; }
        if (!isValidUrl(datashopUrl)) { datashopUrl = ""; }

        try {
            URL authUrl = new URL("https://github.com/login/oauth/access_token?"
                                  + "client_id=" + githubClientId
                                  + "&client_secret=" + githubClientSecret
                                  + "&code=" + code
                                  + "&redirect_url=" + datashopUrl + "/GitHubLogin");

            HttpURLConnection authConn = (HttpURLConnection)authUrl.openConnection();
            authConn.setRequestMethod("POST");
            authConn.setConnectTimeout(20000);

            String outputString = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(authConn.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                outputString = outputString + line;
            }

            String accessToken = null;
            if (outputString.indexOf("access_token") != -1) {
                accessToken = outputString.substring(13, outputString.indexOf("&"));
            }

            URL userUrl = new URL("https://api.github.com/user");

            HttpURLConnection userConn = (HttpURLConnection) userUrl.openConnection();
            userConn.setRequestProperty("Authorization", "token " + accessToken);
            userConn.setRequestProperty("User-Agent", "LearnSphere");
            userConn.setRequestMethod("GET");
            userConn.setUseCaches(false);
            userConn.setDoInput(true);
            userConn.setDoOutput(true);
            userConn.setConnectTimeout(7000);

            outputString = "";
            reader = new BufferedReader(new InputStreamReader(userConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                outputString = outputString + line;
            }
            reader.close();

            JSONObject json = new JSONObject(outputString);

            result.setAccountId((String)json.get("login"));
            result.setLoginId(String.valueOf(json.get("id")));
            result.setLoginType("GitHub");
            result.setWebIso(true);  // always for this servlet

            Object email = json.get("email");
            // If null, user has chosen to keep email private. Sigh.
            if (email != JSONObject.NULL) {
                result.setEmail((String)email);
            }

        } catch (IOException ioe) {
            logger.info("Failed to get GitHubAuth: " + ioe);
        } catch (JSONException je) {
            logger.info("Failed to parse JSON response: " + je);
            result = null;
        }

        return result;
    }

    /**
     * Helper method to get DataShop URL.
     * @return String DataShop URL, instance specific.
     */
    private String getDatashopUrl() {

        String datashopUrl = DataShopInstance.getDatashopUrl();

        // The redirect_uri cannot have a path, e.g., the "/datashop"
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

} // end class GitHubLoginServlet
