/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.auth;


/**
 * Contains info related to logging into the system.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14923 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-03-13 12:38:51 -0400 (Tue, 13 Mar 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LoginInfo {

    /** Login failed message. */
    private static final String LOGIN_FAILED_MSG = "Invalid username and/or password.";

    /** Login name. */
    private String accountId = "";
    
    /** First name. */
    private String firstName = null;

    /** Last name. */
    private String lastName = null;

    /** Email. */
    private String emailAddr = null;

    /** Id from login provider. */
    private String loginId = null;

    /** Login provider type. */
    private String loginType = "local";

    /** Login failed flag. */
    private boolean loginFailed = false;

    /** Indicates whether this is a WEBISO login or not. */
    private boolean webIsoFlag = false;

    /**
     * Default constructor.
     */
    public LoginInfo() {
    }

    /**
     * Returns the account id.
     * @return the account id
     */
    public String getAccountId() {
        if (accountId == null) { return ""; }
        return this.accountId;
    }

    /**
     * Sets the account id.
     * @param id the account id
     */
    public void setAccountId(String id) {
        this.accountId = id;
    }

    /**
     * Get the first name.
     * @return String firstName
     */
    public String getFirstName() { return firstName; }

    /**
     * Set the first name.
     * @param in the first name
     */
    public void setFirstName(String in) { this.firstName = in; }

    /**
     * Get the last name.
     * @return String lastName
     */
    public String getLastName() { return lastName; }

    /**
     * Set the last name.
     * @param in the last name
     */
    public void setLastName(String in) { this.lastName = in; }

    /**
     * Return the email address.
     * @return String emailAddr
     */
    public String getEmail() { return emailAddr; }

    /**
     * Set the email address.
     * @param in the email address
     */
    public void setEmail(String in) { this.emailAddr = in; }

    /**
     * Return the login id.
     * @return String loginId
     */
    public String getLoginId() { return loginId; }

    /**
     * Set the login id.
     * @param in the loginId
     */
    public void setLoginId(String in) { this.loginId = in; }

    /**
     * Return the login type.
     * @return String loginType
     */
    public String getLoginType() { return loginType; }

    /**
     * Set the login type.
     * @param in the loginType
     */
    public void setLoginType(String in) { this.loginType = in; }

    /**
     * Returns true if login has failed.
     * @return true if login has failed
     */
    public boolean hasLoginFailed() {
        return this.loginFailed;
    }

    /**
     * Sets the login failed flag.
     * @param flag indicates whether login has failed
     */
    public void setLoginFailed(boolean flag) {
        this.loginFailed = flag;
    }

    /**
     * Returns the WEBISO flag.
     * @return true if this is a WEBISO registration
     */
    public boolean isWebIso() {
        return webIsoFlag;
    }

    /**
     * Sets the WEBISO flag.
     * @param flag indicates whether this is a WEBISO registration
     */
    public void setWebIso(boolean flag) {
        this.webIsoFlag = flag;
    }

    /**
     * Returns HTML with an error message if the login failed.
     * @return the HTML with an error message if the login failed, empty string otherwise
     */
    public String getLoginFailedMessage() {
        if (loginFailed) {
            return getInvalidMessage(LOGIN_FAILED_MSG);
        }
        return "";
    }

    /**
     * Utility method.
     * @param message the error message
     * @return the HTML with the error message filled in
     */
    private String getInvalidMessage(String message) {
        return "<p class=\"errorMessage\">" + message + "</p>";
    }
}
