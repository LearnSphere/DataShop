/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2018
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
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
 * Handles LinkedIn login, both DataShop and LearnSphere.
 *
 * @author Cindy Tipper
 * @version $Revision: 16047 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-04-22 15:53:43 -0400 (Mon, 22 Apr 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LinkedInLoginServlet extends AbstractServlet {

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
            LoginInfo loginInfo = linkedInLogin(req.getParameter("code"), req.getParameter("state"));
            String accountId = loginInfo.getAccountId();

            // If null, the LinkedIn login failed.
            if (accountId == null) {
                logDebug("accountId is null; LinkedIn login failed");

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
                // They might have chosen to not use email address as id...
                accountId = (String)userItem.getId();
            }

            if (userItem == null) { // account does not exist, go to registration page
                logger.info("LinkedIn user logged in, user needs to create account: "
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

    private static final String LINKEDIN_CLIENT_ID = "77sq8ieis6nm9y";
    private static final String LINKEDIN_CLIENT_SECRET = "";

    /**
     * Helper method to start OAuth handshake with LinkedIn for login.
     * @param code the LinkedIn response to initiating login
     * @return the user's LoginInfo
     */
    private LoginInfo linkedInLogin(String code, String state) {

        /// TBD. Compare state with 'oiwiwaomwtiwitw2b'
        LoginInfo result = new LoginInfo();

        String datashopUrl = DataShopInstance.getDatashopUrl();

        try {
            if (!isValidUrl(datashopUrl)) { datashopUrl = ""; }

            String encodedUri = URLEncoder.encode(datashopUrl + "/LinkedInLogin", "UTF-8");

            if (code != null && !code.matches(STRICT_CHARS)) { code = ""; }

            URL authUrl = new URL("https://www.linkedin.com/oauth/v2/accessToken?"
                                  + "grant_type=authorization_code"
                                  + "&code=" + code
                                  + "&redirect_uri=" + encodedUri
                                  + "&client_id=" + LINKEDIN_CLIENT_ID
                                  + "&client_secret=" + LINKEDIN_CLIENT_SECRET);

            HttpURLConnection authConn = (HttpURLConnection)authUrl.openConnection();
            authConn.setRequestProperty("Host", "www.linkedin.com");
            authConn.setRequestProperty("Content-Type", "application/json");
            authConn.setRequestProperty("Content-Length", "500");
            authConn.setRequestMethod("GET");
            authConn.setUseCaches(false);
            authConn.setDoInput(true);
            authConn.setDoOutput(true);
            authConn.setConnectTimeout(7000);

            Integer respCode = authConn.getResponseCode();
            InputStream is = null;
            if (respCode != 200) {
                is = authConn.getErrorStream();
            } else {
                is = authConn.getInputStream();
            }

            String outputString = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                outputString = outputString + line;
            }

            String accessToken = null;
            if (outputString.indexOf("access_token") != -1) {
                JSONObject json = new JSONObject(outputString);
                accessToken = (String)json.get("access_token");
            }

            result = getProfileInfo(accessToken);
            String emailAddr = getEmailAddress(accessToken);
            if (emailAddr != null) {
                result.setEmail(emailAddr);
                result.setAccountId(emailAddr);
            }

            result.setLoginType("LinkedIn");
            result.setWebIso(true);  // always for this servlet

        } catch (IOException ioe) {
            logger.info("Failed to get LinkedInAuth: " + ioe);
            result.setAccountId(null);
        } catch (JSONException je) {
            logger.info("Failed to parse JSON response: " + je);
            result.setAccountId(null);
        }

        return result;
    }

    /**
     * Helper method to query for user's profile info and parse resulting JSON.
     *
     * @param accessToken the token used to sign requests
     * @return LoginInfo with null accountId if unsuccessful
     */
    private LoginInfo getProfileInfo(String accessToken) {

        LoginInfo result = new LoginInfo();

        try {
            StringBuffer urlSB = new StringBuffer("https://api.linkedin.com/v2/me");
            urlSB.append("?projection=(id,firstName,lastName)");
            URL userUrl = new URL(urlSB.toString());
            JSONObject json = getJsonResult(userUrl, accessToken);

            if (json == null) {
                result.setAccountId(null);
                return result;
            }

            result.setLoginId((String)json.get("id"));

            Iterator<Object> iter = json.keys();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                if (key.equals("lastName")) {
                    String lname = parseJsonName(json.get(key));
                    result.setLastName(lname);
                } else if (key.equals("firstName")) {
                    String fname = parseJsonName(json.get(key));
                    result.setFirstName(fname);
                }
            }

        } catch (IOException ioe) {
            logger.info("Failed to get LinkedInAuth: " + ioe);
            result.setAccountId(null);
        } catch (JSONException je) {
            logger.info("Failed to parse JSON response: " + je);
            result.setAccountId(null);
        }

        return result;
    }

    /**
     * Helper method to query for user's email and parse resulting JSON, in the format:
     * {"elements":[{"handle~":{"emailAddress":"joe.smith@gmail.com"},"handle":"urn:li:emailAddress:135549162"}]}
     *
     * @param accessToken the token used to sign requests
     * @return String the user's email address, null if not found
     */
    private String getEmailAddress(String accessToken) {

        String result = null;

        try {
            StringBuffer urlSB = new StringBuffer("https://api.linkedin.com/v2/emailAddress");
            urlSB.append("?q=members&projection=(elements*(handle~))");
            URL userUrl = new URL(urlSB.toString());

            JSONObject json = getJsonResult(userUrl, accessToken);

            if (json == null) {
                return result;
            }

            JSONArray elements = (JSONArray)json.get("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject ele = (JSONObject)elements.get(i);
                JSONObject handle = (JSONObject)ele.get("handle~");
                result = (String)handle.get("emailAddress");
            }

        } catch (IOException ioe) {
            logger.info("Failed to get LinkedInAuth: " + ioe);
        } catch (JSONException je) {
            logger.info("Failed to parse JSON response: " + je);
        }

        return result;
    }

    /**
     * Helper method to invoke specified URL and return result string as a JSONObject.
     *
     * @param URL the url to connect to
     * @param accessToken the token used to sign requests
     * @return JSONObject result
     */
    private JSONObject getJsonResult(URL theUrl, String accessToken) {

        JSONObject result = null;

        try {
            HttpURLConnection userConn = (HttpURLConnection) theUrl.openConnection();
            userConn.setRequestProperty("Host", "api.linkedin.com");
            userConn.setRequestProperty("Authorization", "Bearer " + accessToken);
            userConn.setRequestProperty("Content-Length", "500");
            userConn.setRequestProperty("Content-Type", "application/json");
            userConn.setRequestMethod("GET");
            userConn.setUseCaches(false);
            userConn.setDoInput(true);
            userConn.setDoOutput(true);
            userConn.setConnectTimeout(7000);

            Integer respCode = userConn.getResponseCode();

            InputStream is = null;
            if (respCode != 200) {
                is = userConn.getErrorStream();
            } else {
                is = userConn.getInputStream();
            }

            String outputString = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                outputString = outputString + line;
            }
            reader.close();

            result = new JSONObject(outputString);

        } catch (IOException ioe) {
            logger.info("Failed to get LinkedInAuth: " + ioe);
            result = null;
        } catch (JSONException je) {
            logger.info("Failed to parse JSON response: " + je);
            result = null;
        }

        return result;
    }

    /** Constants. */
    private final static String PREFERRED_LOCALE_KEY = "preferredLocale";
    private final static String LOCALIZED_KEY = "localized";

    /**
     * Helper method to parse names in returned JSON. For LinkedIn, the firstName
     * and lastName JSON values are of the form:
     *
     * {"preferredLocale":{"language":"en","country":"US"},"localized":{"en_US":"Abe"}}
     * {"preferredLocale":{"language":"en","country":"US"},"localized":{"en_US":"Lincoln"}}
     *
     * @param objToParse the Object to be parsed
     * @return String the localized version of the name
     */
    private String parseJsonName(Object objToParse)
        throws JSONException
    {
        String result = "";

        // If not expected format, return empty string.
        if (!(objToParse instanceof JSONObject)) { return result; }

        String lang = null;
        String country = null;
        JSONObject localized = null;

        JSONObject jsonObj = (JSONObject)objToParse;
        Iterator<Object> iter = jsonObj.keys();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Object val = jsonObj.get(key);

            if (key.equals(PREFERRED_LOCALE_KEY)) {
                JSONObject locale = (JSONObject)val;
                Iterator<Object> iter2 = locale.keys();
                while (iter2.hasNext()) {
                    String key2 = (String)iter2.next();

                    if (key2.equals("language")) {
                        lang = (String)locale.get("language");
                    } else if (key2.equals("country")) {
                        country = (String)locale.get("country");
                    }
                }
            } else if (key.equals(LOCALIZED_KEY)) {
                localized = (JSONObject)val;
            } else {
                logger.debug("Ignoring object with key: " + key);
            }
        }

        if ((lang != null) && (country != null) && (localized != null)) {
            StringBuffer localizedKey = new StringBuffer();
            localizedKey.append(lang);
            localizedKey.append("_");
            localizedKey.append(country);
            result = (String)localized.get(localizedKey.toString());
        }

        return result;
    }

} // end class LinkedInLoginServlet
