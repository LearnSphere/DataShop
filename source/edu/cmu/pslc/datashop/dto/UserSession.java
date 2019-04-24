/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

/**
 * Inner class to hold distinct user/session pairs.
 */
public class UserSession {
    /** User id. */
    private String userId;
    /** Session id. */
    private String sessionId;
    /** Default constructor. */
    public UserSession() {
    }
    /**
     * Constructor.
     * @param userId user id
     * @param sessionId session id
     */
    public UserSession(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }
    /**
     * Returns the user id.
     * @return the user id
     */
    public String getUserId() { return this.userId; }
    /**
     * Returns the session id.
     * @return the session id
     */
    public String getSessionId() { return this.sessionId; }
    /**
     * Sets the user id.
     * @param userId the user id
     */
    public void setUserId(String userId) { this.userId = userId; }
    /**
     * Sets the session id.
     * @param sessionId the session id
     */
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    public String toString() {
        return userId + ", " + sessionId;
    }
}
