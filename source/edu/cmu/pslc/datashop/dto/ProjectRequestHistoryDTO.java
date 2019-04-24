/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Date;

/**
 * Used to transfer Project Access Request History data.
 *
 * @author Mike Komisin
 * @version $Revision: 7529 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2012-03-20 09:51:25 -0400 (Tue, 20 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectRequestHistoryDTO {

    /** User name */
    private String userName;
    /** User email */
    private String email;
    /** Access Request role */
    private String role;
    /** Access Request action */
    private String action;
    /** Access Request level */
    private String level;
    /** Access Request reason */
    private String reason;
    /** Date */
    private Date date;

    /** Default constructor. */
    public ProjectRequestHistoryDTO() {
    }

    /**
     * Returns the user full name or the user Id if none exists.
     * @return the user full name or the user Id if none exists
     */
    public String getUserFullName() {
        if (userName == null) {
            return "";
        }
        return userName;
    }

    /**
     * Sets the user full name.
     * @param userName the user full name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the email address.
     * @return the email address
     */
    public String getEmail() {
        if (email == null) {
            return "";
        }
        return email;
    }

    /**
     * Sets the email address.
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the Access Request role.
     * @return the Access Request role
     */
    public String getRole() {
        if (role == null) {
            return "";
        }
        return role;
    }

    /**
     * Sets the Access Request role.
     * @param role the Access Request role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the Access Request action.
     * @return the Access Request action
     */
    public String getAction() {
        if (action == null) {
            return "";
        }
        return action;
    }

    /**
     * Sets the Access Request action.
     * @param action the Access Request action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns the Access Request action formatted.
     * @return the Access Request action formatted
     */
    public String getActionFormatted() {
        if (action == null || action.equals("")) {
            return "";
        }
        String capitalized = action.substring(0, 1).toUpperCase();
        if (action.length() > 1) {
            capitalized = capitalized + action.substring(1, action.length()).toLowerCase();
        }
        return capitalized;
    }

    /**
     * Returns the Access Request level.
     * @return the Access Request level
     */
    public String getLevel() {
        if (level == null) {
            return "";
        }
        return level;
    }

    /**
     * Returns the Access Request level capitalized.
     * @return the Access Request level capitalized
     */
    public String getLevelFormatted() {
        if (level == null || level.equals("")) {
            return "";
        }
        String capitalized = level.substring(0, 1).toUpperCase();
        if (level.length() > 1) {
            capitalized = capitalized + level.substring(1, level.length()).toLowerCase();
        }
        return capitalized;
    }

    /**
     * Sets the Access Request level.
     * @param level the Access Request level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Returns the Access Request reason.
     * @return the Access Request reason
     */
    public String getReason() {
        if (reason != null) {
            return reason.replaceAll("[\t\r\n]+", " ");
        } else {
            return "";
        }
    }

    /**
     * Sets the Access Request reason.
     * @param reason the Access Request reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Returns the Access Request date.
     * @return the Access Request date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the Access Request date.
     * @param date the Access Request date
     */
    public void setDate(Date date) {
        this.date = date;
    }

}
