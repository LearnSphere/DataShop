/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.beginmind.login.PasswordCredentials;
import com.beginmind.login.TrustedCredentials;
import com.beginmind.login.model.AuthDomain;
import com.beginmind.login.model.LoginServicePrincipal;
import com.beginmind.login.model.LoginSession;
import com.beginmind.login.model.SystemUser;
import com.beginmind.login.service.InvalidCredentialsException;
import com.beginmind.login.service.LoginService;
import com.beginmind.login.service.LoginServiceException;
import com.beginmind.login.service.LoginServiceFactory;
import com.beginmind.login.service.SuspendedUserException;
import com.beginmind.login.service.UserManager;
import com.beginmind.login.service.UserManagerFactory;
import com.beginmind.login.service.UserNotFoundException;
import com.beginmind.login.spi.Credentials;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * The methods needed to interface to the OLI user services.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14923 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-03-13 12:38:51 -0400 (Tue, 13 Mar 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class OliUserServices {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(OliUserServices.class);

    /** Domain ID of a non-web-iso account. */
    private static final String EXTERNAL_DOMAIN_ID = "external";
    /** Domain ID of a web-iso account. */
    private static final String WEBISO_DOMAIN_ID = "webiso";

    /** Utility class, does not need a constructor. */
    private OliUserServices() { }

    /**
     * Get credentials.
     * @param accountId the account id
     * @param password the password
     * @param isWebIso indicates whether this user is a web iso user
     * @return either PasswordCredentials or TrustedCredentials (webiso)
     */
    private static Credentials getCredentials(String accountId, String password, boolean isWebIso) {

        if (isWebIso) {
            return getWebisoCredentials(accountId);
        }

        return getPasswordCredentials(accountId, password);
    }

    /**
     * Get WEBISO credentials.
     * @param accountId the account id
     * @return the credentials
     */
    private static Credentials getWebisoCredentials(String accountId) {
        return (new TrustedCredentials(WEBISO_DOMAIN_ID, accountId));
    }

    /**
     * Get regular credentials (as in not through WEBISO).
     * @param accountId the account id
     * @param password the password
     * @return the credentials
     */
    private static Credentials getPasswordCredentials(String accountId, String password) {
        return (new PasswordCredentials(EXTERNAL_DOMAIN_ID, accountId, password));
    }

    /**
     * Log the student in.
     * @param req The HTTP servlet request, needed to get the remote IP address
     * @param resp The HTTP servlet response, needed for the cookie
     * @param accountId the account id
     * @param password the password
     * @param webIsoFlag indicates whether we are logging in through WEBISO or not
     * @return an AuthInfo object completed filled in if successful, null otherwise
     */
    public static AuthInfo login(HttpServletRequest req, HttpServletResponse resp,
            String accountId, String password, boolean webIsoFlag) {
        String prefix =  "login: user [" + accountId + "]";
        logger.debug(prefix);

        AuthInfo authInfo = null;

        if (accountId == null) {
            return null;
        }

        Credentials credentials = null;
        if (webIsoFlag) {
            credentials = getWebisoCredentials(accountId);

            // see if this is a google user that should be migrated...
            handleGoogleUserMigrationIfNec(accountId, (TrustedCredentials)credentials);
        } else {
            credentials = getPasswordCredentials(accountId, password);
        }

        authInfo = new AuthInfo(accountId);

        try {
            // authenticate the user using supplied account and password
            LoginService loginSvc = LoginServiceFactory.getInstance().getLoginService();

            String ipAddress = req.getRemoteAddr();

            LoginSession authSession = loginSvc.authenticate(credentials, ipAddress);

            // authentication successful, create cookie from token
            createCookie(resp, loginSvc, authSession);

            authInfo.setSessionId(authSession.getUniqueId());
            authInfo.setAuthenticationToken(loginSvc.getSessionToken(authSession));
            authInfo.setLastAccess(authSession.getLastAccess());
            authInfo.setSessionStart(authSession.getLastAccess());

            UserItem user = getUserInfo(accountId);
            authInfo.setEmailAddress(user.getEmail());
            authInfo.setFirstName(user.getFirstName());
            authInfo.setLastName(user.getLastName());
            authInfo.setInstitution(user.getInstitution());

            if (logger.isDebugEnabled()) {
                logger.debug(prefix + " is logged in.");
            }

        } catch (UserNotFoundException exception) {
            // no such account
            if (webIsoFlag) {
                if (logger.isDebugEnabled()) {
                    logger.debug("UserNotFoundException: WebISO User not found."
                                 + " User [" + accountId + "]");
                }
            } else {
                logger.warn("UserNotFoundException: User not found."
                            + " User [" + accountId + "]");
            }
            authInfo.setException(exception.getClass().getSimpleName());
        } catch (InvalidCredentialsException exception) {
            // bad login
            logger.warn("InvalidCredentialsException: Bad login."
                            + " User [" + accountId + "]");
            authInfo.setException(exception.getClass().getSimpleName());
        } catch (SuspendedUserException exception) {
            // account disabled
            logger.warn("SuspendedUserException: Account disabled."
                            + " User [" + accountId + "]");
            authInfo.setException(exception.getClass().getSimpleName());
        } catch (LoginServiceException exception) {
            // something else bad happened, but we don't know what
            logger.warn("LoginServiceException: Something bad happened."
                            + " User [" + accountId + "]");
            authInfo.setException(exception.getClass().getSimpleName());
        }

        return authInfo;
    }

    /**
     * Helper method to handle migration of Google users. These are users
     * that already exist in the authentication db in the 'external'
     * domain but now must have an entry for the 'webiso' domain since we
     * have added support for Google SSO.
     *
     * This method, if necessary, will create a new user with appropriate domain.
     *
     * @param accountId the user id
     * @param credentials new TrustedCredentials for user
     */
    private static void handleGoogleUserMigrationIfNec(String accountId,
                                                       TrustedCredentials credentials) {

        try {
            UserManager userManager = UserManagerFactory.getInstance().getUserManager();

            // If the webiso domain user already exists, we're done.
            if (userManager.hasUser(accountId, WEBISO_DOMAIN_ID)) { return; }
            
            // If the external domain user exists, create a new webiso user.
            if (userManager.hasUser(accountId, EXTERNAL_DOMAIN_ID)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("handleGoogleUserMigrationIfNec: accountId = " + accountId);
                }
                userManager.createUser(credentials);
            }
        } catch (LoginServiceException exception) {
            // Ignore this because the rest of the login method will handle errors.
            logger.info("Unable to create new webiso account for: " + accountId);
        }
    }

    /**
     * Log the student out.
     * @param req The HTTP servlet request, needed to get the user principal
     * @return true if the user is logged out successfully, false otherwise
     */
    public static boolean logout(HttpServletRequest req) {

        if (logger.isDebugEnabled()) {
            logger.debug("OliUserServices: in logout method");
        }

        // If the login service principal is null, then user is not logged in.
        LoginServicePrincipal lsp = (LoginServicePrincipal)req.getUserPrincipal();
        if (lsp == null) {
            logger.info("User is not logged in, login service principal is null.");
            return false;
        }

        // If no login session found, then user is not logged in.
        LoginSession loginSession = lsp.getLoginSession();
        if (loginSession == null) {
            logger.info("User is not logged in, login session not found.");
            return false;
        }

        try {
            LoginService loginSvc = LoginServiceFactory.getInstance().getLoginService();
            if (loginSvc != null) {
                loginSvc.logout(loginSession);
                return true;
            }
            logger.info("User is not logged out, login service not found.");
            return false;
        } catch (LoginServiceException exception) {
            logger.warn("Something bad happened.", exception);
            return true;
        }
    }

    /**
     * Log the student out.
     * @param req The HTTP servlet request, needed to get the user principal
     * @return true if the user is logged out successfully, false otherwise
     */
    public static AuthInfo isLoggedIn(HttpServletRequest req) {
        if (logger.isDebugEnabled()) {
            logger.debug("OliUserServices: in isLoggedIn method");
        }

        AuthInfo authInfo = null;

        // If the login service principal is null, then user is not logged in.
        LoginServicePrincipal lsp = (LoginServicePrincipal)req.getUserPrincipal();
        if (lsp == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("User is not logged in, login service principal is null.");
            }
            return authInfo;
        }

        // If no login session found, then user is not logged in.
        LoginSession loginSession = lsp.getLoginSession();
        if (loginSession == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("User is not logged in, login session not found.");
            }
            return authInfo;
        }

        try {
            LoginService loginSvc = LoginServiceFactory.getInstance().getLoginService();
            if (loginSvc != null) {

                String accountId = req.getRemoteUser();

                authInfo = new AuthInfo(accountId);
                authInfo.setSessionId(loginSession.getUniqueId());
                authInfo.setAuthenticationToken(loginSvc.getSessionToken(loginSession));
                authInfo.setLastAccess(loginSession.getLastAccess());
                authInfo.setSessionStart(loginSession.getLastAccess());

                UserItem user = getUserInfo(accountId);
                if (user == null) { return null; }

                authInfo.setEmailAddress(user.getEmail());
                authInfo.setFirstName(user.getFirstName());
                authInfo.setLastName(user.getLastName());
                authInfo.setInstitution(user.getInstitution());

                return authInfo;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("User is not logged in, login service not found.");
            }
            return authInfo;
        } catch (LoginServiceException exception) {
            logger.warn("Something bad happened.", exception);
            return authInfo;
        }
    }

    /**
     * Authentication successful, create cookie from token.
     * @param res HTTP servlet response
     * @param loginSvc login service
     * @param authSession the authorized login session
     * @throws LoginServiceException if something bad happens while getting authorization token
     */
    private static void createCookie(HttpServletResponse res,
            LoginService loginSvc,
            LoginSession authSession) throws LoginServiceException {

        String authToken = loginSvc.getSessionToken(authSession);
        Cookie cookie = new Cookie("session", authToken);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        cookie.setSecure(true);
        res.addCookie(cookie);
    }

    /**
     * Create a user account with the institution.
     * @param userInfo the UserItem, with necessary fields filled in
     * @param password the password
     * @param isWebIso indicates whether this user is a web iso user
     * @return true if the account is created successfully, false otherwise
     */
    public static boolean createOliUserAccount(UserItem userInfo,
                                               String password,
                                               boolean isWebIso)
    {
        boolean successFlag = false;

        String accountId = (String)userInfo.getId();
        String firstName = userInfo.getFirstName();
        String lastName = userInfo.getLastName();
        String email = userInfo.getEmail();
        String institution = userInfo.getInstitution();

        Credentials credentials = getCredentials(accountId, password, isWebIso);

        try {
            if (!UserManager.isValidIdentifier(accountId)) {
                logger.info("Invalid identifier for " + accountId);
                return false;
            }

            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem userItem = userDao.get(accountId);
            if (userItem != null) {
                logger.info("Account Id is taken for " + accountId);
                return false;
            }
            userItem = new UserItem();
            userItem.setId(accountId);
            userItem.setFirstName(firstName);
            userItem.setLastName(lastName);
            userItem.setEmail(email);
            userItem.setInstitution(institution);
            userDao.saveOrUpdate(userItem);
            logger.info("Account created for " + accountId);

            String domainId = EXTERNAL_DOMAIN_ID;
            if (isWebIso) {
                domainId = WEBISO_DOMAIN_ID;
            }

            UserManager userManager = UserManagerFactory.getInstance().getUserManager();
            if (userManager.hasUser(accountId, domainId)) {
                userManager.setCredentials(credentials);
            } else {
                userManager.createUser(credentials);
            }

            successFlag = true;
        } catch (LoginServiceException exception) {
            successFlag = false;
            logger.info("LoginServiceException occurred for account: " + accountId
                    + ". " + exception.getMessage(), exception);
        }

        return successFlag;
    }

    /**
     * Returns an OLI user object.
     * @param accountId the account id
     * @return an OLI user object which has the first name, last name and email
     */
    public static UserItem getUserInfo(String accountId) {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(accountId);
        if (userItem == null) {
            logger.warn("Account Id not found: " + accountId);
        }
        return userItem;
    }

    /**
     * Check if the current machine is OLI enabled.
     * @return true if this machine is OLI enabled, false otherwise
     */
    public static boolean isOliEnabled() {
        boolean isEnabled = true;
        try {
            UserManagerFactory.getInstance().getUserManager();
        } catch (LoginServiceException exception) {
            logger.warn("LoginServiceException occurred. " + exception.getMessage(), exception);
            isEnabled = false;
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("OLI authentication service not enabled.  No EJB.");
            }
            isEnabled = false;
        }
        return isEnabled;
    }

    /**
     * Check if the user currently logged in is a webiso user or not.
     * @param req The HTTP servlet request, needed to get the login service principal
     * @return true if the user is a webiso user, false otherwise
     */
    public static boolean isWebisoAccount(HttpServletRequest req) {
        boolean isWebiso = false;
        try {
            LoginServicePrincipal lsp = (LoginServicePrincipal) req.getUserPrincipal();
            String domainId = lsp.getDomainId();
            if (domainId.equals(WEBISO_DOMAIN_ID)) {
                isWebiso = true;
            }
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("OLI authentication service not enabled.  No EJB.");
            }
            isWebiso = false;
        }
        return isWebiso;
    }

    /**
     * Check the username to determine if a WebISO user or not.
     * This check does not require the user to be logged in.
     * @param username The name of the user to check
     * @return true if the specified user is a WebISO user, false otherwise.
     */
    public static boolean isWebisoUser(String username) {
        boolean result = false;
        SystemUser systemUser = getWebisoSystemUser(username);
        if (systemUser != null) {
            result = true;
        }
        return result;
    }

    /**
     * Check the username to determine if a WebISO user or not.
     * This check does not require the user to be logged in.
     * @param username The name of the user to check
     * @return the SystemUser object if the user is a webiso user, null otherwise
     */
    public static SystemUser getWebisoSystemUser(String username) {
        SystemUser systemUser = null;
        try {
            UserManager userManager = UserManagerFactory.getInstance().getUserManager();
            systemUser = userManager.getUser(username, WEBISO_DOMAIN_ID);
        } catch (LoginServiceException exception) {
            logger.debug("getWebisoSystemUser: user " + username + " is not a webiso user.");
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("OLI authentication service not enabled.  No EJB.");
            }
        }
        return systemUser;
    }

    /**
     * Check the password for the user currently logged in with a call to the PSLC-OLI
     * authentication service.
     * @param req The HTTP servlet request, needed to get the login service principal and user id
     * @param oldPassword the user's current password
     * @return true if the password is valid, false otherwise
     */
    public static boolean checkPassword(HttpServletRequest req, String oldPassword) {
        boolean passwordValid = false;
        try {
            UserManager userManager = UserManagerFactory.getInstance().getUserManager();
            LoginServicePrincipal lsp = (LoginServicePrincipal) req.getUserPrincipal();
            String domainId = lsp.getDomainId();
            String userGuid = req.getRemoteUser();
            Credentials oldCred = new PasswordCredentials(domainId, userGuid, oldPassword);
            passwordValid = userManager.validCredentials(oldCred);
        } catch (LoginServiceException exception) {
            logger.warn("LoginServiceException occurred. " + exception.getMessage(), exception);
            passwordValid = false;
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("OLI authentication service not enabled.  No EJB.");
            }
            passwordValid = false;
        }
        return passwordValid;
    }

    /**
     * Change the password for the user currently logged in with a call to the PSLC-OLI
     * authentication service.
     * @param req The HTTP servlet request, needed to get the login service principal and user id
     * @param newPassword the user's new password
     * @return true if the password is changed, false otherwise
     */
    public static boolean changePassword(HttpServletRequest req, String newPassword) {
        boolean passwordChanged = false;
        try {
            UserManager userManager = UserManagerFactory.getInstance().getUserManager();
            String userGuid = req.getRemoteUser();
            Credentials newCred =
                    new PasswordCredentials(EXTERNAL_DOMAIN_ID, userGuid, newPassword);
            userManager.setCredentials(newCred);
            passwordChanged = true;
        } catch (LoginServiceException exception) {
            logger.warn("changePassword: LoginServiceException occurred. "
                    + exception.getMessage(), exception);
            passwordChanged = false;
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("changePassword: OLI authentication service not enabled.  No EJB.");
            }
            passwordChanged = false;
        }
        return passwordChanged;
    }

    /**
     * Change the password for the user currently logged in with a call to the PSLC-OLI
     * authentication service.
     * @param username the username associated with the password reset GUID
     * @param newPassword the user's new password
     * @return true if the password is changed, false otherwise
     */
    public static boolean resetPassword(String username, String newPassword) {
        boolean passwordChanged = false;
        try {
            UserManager userManager = UserManagerFactory.getInstance().getUserManager();
            Credentials newCred =
                    new PasswordCredentials(EXTERNAL_DOMAIN_ID, username, newPassword);
            userManager.setCredentials(newCred);
            passwordChanged = true;
        } catch (LoginServiceException exception) {
            logger.warn("resetPassword: LoginServiceException occurred. "
                    + exception.getMessage(), exception);
            passwordChanged = false;
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("resetPassword: OLI authentication service not enabled.  No EJB.");
            }
            passwordChanged = false;
        }
        return passwordChanged;
    }

    /**
     * Get the SystemUser checking both the external and webiso domains.
     * @param username the user id
     * @return a SystemUser object
     * @throws LoginServiceException if something bad happens while getting authorization token
     */
    private static SystemUser getSystemUser(String username) throws LoginServiceException {
        SystemUser systemUser = null;
        try {
            systemUser = getWebisoSystemUser(username);
            if (systemUser == null) {
                UserManager userManager = UserManagerFactory.getInstance().getUserManager();
                systemUser = userManager.getUser(username, EXTERNAL_DOMAIN_ID);
            }
        } catch (LoginServiceException exception) {
            logger.debug("getSystemUser: user " + username + " is not a webiso user.");
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("getSystemUser: OLI authentication service not enabled.  No EJB.");
            }
        }
        return systemUser;
    }

    /**
     * Returns true if the user is enabled, ie not suspended.
     * @param username the user id for the user to be suspended
     * @return true is user is enabled, false if suspended
     */
    public static Boolean isUserEnabled(String username)  {
        Boolean isEnabled = false;
        try {
            SystemUser systemUser = OliUserServices.getSystemUser(username);
            if (systemUser != null) {
                isEnabled = systemUser.getEnabled();
                if (logger.isDebugEnabled()) {
                    logger.debug("User " + username + " enabled: " + isEnabled);
                }
            } else {
                logger.warn("isUserEnabled: SystemUser not found for : " + username);
            }
        } catch (LoginServiceException exception) {
            logger.warn("isUserEnabled: LoginServiceException occurred. "
                    + exception.getMessage(), exception);
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("isUserEnabled: OLI authentication service not enabled.  No EJB.");
            }
        }
        return isEnabled;
    }

    /**
     * Suspend user account.
     * @param username the user to be suspended
     * @return true is successful (user found), false otherwise
     * @throws LoginServiceException
     */
    public static boolean suspendUser(String username)  {
        boolean successFlag = false;
        try {
            UserManager userManager = UserManagerFactory.getInstance().getUserManager();
            SystemUser systemUser = OliUserServices.getSystemUser(username);
            if (systemUser != null) {
                SystemUser newSystemUser = userManager.suspendUser(systemUser);
                if (newSystemUser.getEnabled()) {
                    logger.warn("Suspending user failed.  User account still enabled.");
                    successFlag = false;
                } else {
                    logger.info("Suspending user succeeded.  User account now disabled.");
                    successFlag = true;
                }
            } else {
                logger.warn("suspendUser: SystemUser not found for : " + username);
            }
        } catch (LoginServiceException exception) {
            logger.warn("suspendUser: LoginServiceException occurred. "
                    + exception.getMessage(), exception);
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("suspendUser: OLI authentication service not enabled.  No EJB.");
            }
        }
        return successFlag;
    }

    /**
     * Restore user account.
     * @param username the user to be restored
     * @return true is successful (user found), false otherwise
     * @throws LoginServiceException
     */
    public static boolean restoreUser(String username)  {
        boolean successFlag = false;
        try {
            UserManager userManager = UserManagerFactory.getInstance().getUserManager();
            SystemUser systemUser = OliUserServices.getSystemUser(username);
            if (systemUser != null) {
                SystemUser newSystemUser = userManager.restoreUser(systemUser);
                if (newSystemUser.getEnabled()) {
                    logger.warn("Enabling user succeeded.  User account enabled again.");
                    successFlag = true;
                } else {
                    logger.warn("Enabling user failed.  User account still disabled.");
                    successFlag = false;
                }
            } else {
                logger.warn("restoreUser: SystemUser not found for : " + username);
            }
        } catch (LoginServiceException exception) {
            logger.warn("restoreUser: LoginServiceException occurred. "
                    + exception.getMessage(), exception);
        } catch (NoClassDefFoundError error) {
            if (logger.isDebugEnabled()) {
                logger.debug("restoreUser: OLI authentication service not enabled.  No EJB.");
            }
        }
        return successFlag;
    }

}
