/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.auth;

import java.io.Serializable;
import java.util.Date;

/**
 * An object to hold the results of authentication.
 *
 * @author Jonathan Sewall
 * @version $Revision: 4926 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-06-12 12:07:40 -0400 (Thu, 12 Jun 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AuthInfo implements Serializable, Cloneable {

    /** Parameter name for object of this type. */
    public static final String AUTH = "auth";

    /** Parameter name for user identifier. */
    public static final String USER_ID = "user_guid";

    /** Parameter name for user identifier. */
    public static final String SESSION_ID = "session_id";

    /** Parameter name for user identifier. */
    public static final String AUTH_TOKEN = "auth_token";

    /** Default user identifier value when no user is specified. */
    private static final String UNSPECIFIED_USER = "UnspecifiedUser";
    /** Default session identifier value when no session is specified. */
    private static final String UNSPECIFIED_SESSION = "UnspecifiedSession";

    /** User identifier from HTTP Request object. */
    private String userId = UNSPECIFIED_USER;

    /**
     * Get the authentication/logging session ID.
     * Note: this is *NOT* the same as the servlet session ID,
     * the OLI session ID is the same across all applications
     * (i.e. single sign-on for the OLI, DataShop and TutorShop).
     */
    private String sessionId = UNSPECIFIED_SESSION;

    /**
     * Get the secure session token.
     * WARNING: Handle this carefully, this token is what proves
     * that this user has authenticated.
     */
    private String authenticationToken;

    /** Date and time user last accessed something. */
    private Date lastAccess;

    /** Date/time user authenticated. */
    private Date sessionStart;

    /** User's email address. */
    private String emailAddress = "";

    /** User's first name. */
    private String firstName = "";

    /** User's last name. */
    private String lastName = "";

    /** User's institution. */
    private String institution = "";

    /** Exception if login failed. */
    private String exception = null;

    /**
     * Constructor sets only the user id.
     * @param userId the user id
     */
    public AuthInfo(String userId) {
        if (userId != null && userId.length() > 0) {
            this.userId = userId;
        }
    }

    /**
     * Returns the email address.
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Returns the first name.
     * @return the first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name.
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the institution.
     * @return the institution
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * @param emailAddress The emailAddress to set.
     */
    void setEmailAddress(String emailAddress) {
        this.emailAddress = (emailAddress == null ? "" : emailAddress);
    }

    /**
     * @param firstName The firstName to set.
     */
    void setFirstName(String firstName) {
        this.firstName = (firstName == null ? "" : firstName);
    }

    /**
     * @param lastName The lastName to set.
     */
    void setLastName(String lastName) {
        this.lastName = (lastName == null ? "" : lastName);
    }

    /**
     * @param institution The institution to set.
     */
    void setInstitution(String institution) {
        this.institution = (institution == null ? "" : institution);
    }

    /**
     * Indicate whether the user id is unspecified.
     * @return true if the user id is UNSPECIFIED_USER
     */
    public boolean isUserUnspecified() {
        return userId == null || userId.equals(UNSPECIFIED_USER);
    }

    /**
     * Returns the date of the last access.
     * @return the date of the last access
     */
    public Date getLastAccess() {
        return lastAccess;
    }

    /**
     * Returns the user id.
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the session id.
     * @return the session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the date of the session start.
     * @return the date of the session start
     */
    public Date getSessionStart() {
        return sessionStart;
    }

    /**
     * Returns the authentication token.
     * @return the authentication token
     */
    public String getAuthenticationToken() {
        return authenticationToken;
    }

    /**
     * Sets the last access date.
     * @param lastAccess the last access date
     */
    void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    /**
     * Sets the user id.
     * @param remoteUser the user id
     */
    void setUserId(String remoteUser) {
        this.userId = remoteUser;
    }

    /**
     * Sets the session id.
     * @param sessionId the session id
     */
    void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Sets the date of the session start.
     * @param sessionStart the session start date
     */
    void setSessionStart(Date sessionStart) {
        this.sessionStart = sessionStart;
    }

    /**
     * Sets the authentication token.
     * @param authToken the authentication token
     */
    void setAuthenticationToken(String authToken) {
        this.authenticationToken = authToken;
    }

    /**
     * Returns the exception.
     * @return the exception
     */
    public String getException() {
        return exception;
    }

    /**
     * Sets the exception.
     * @param exception The exception to set.
     */
    void setException(String exception) {
        this.exception = exception;
    }
}
