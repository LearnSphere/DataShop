/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.util.List;

import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserRoleItem;

/**
 * Hold the data for each user displayed in the Manage Users report.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageUsersDto {

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private UserItem userItem;
    /** Class attribute. */
    private Boolean userEnabled;
    /** Class attribute. */
    private List<UserRoleItem> userRoles;
    /** Class attribute. */
    private String createdDate;
    /** Class attribute. */
    private String lastLoginDate;
    /** Class attribute. */
    private String lastFiveProjects;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ManageUsersDto() { }

    //----- TO STRING -----

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("UserItem(");
         buffer.append(userItem);
         buffer.append("), userRoles(");
         buffer.append(userRoles);
         buffer.append("), createdDate(");
         buffer.append(createdDate);
         buffer.append("), lastLoginDate(");
         buffer.append(lastLoginDate);
         buffer.append("), lastFiveProjects(");
         buffer.append(lastFiveProjects);
         buffer.append(".");
         return buffer.toString();
     }

    //----- GETTERS and SETTERS -----

    /**
     * Gets userItem.
     * @return the userItem
     */
    public UserItem getUserItem() {
        return userItem;
    }

    /**
     * Sets the userItem.
     * @param userItem the userItem to set
     */
    public void setUserItem(UserItem userItem) {
        this.userItem = userItem;
    }

    /**
     * Gets userEnabled.
     * @return the userEnabled
     */
    public Boolean getUserEnabled() {
        return userEnabled;
    }

    /**
     * Sets the userEnabled.
     * @param userEnabled the userEnabled to set
     */
    public void setUserEnabled(Boolean userEnabled) {
        this.userEnabled = userEnabled;
    }

    /**
     * Gets userRoles.
     * @return the userRoles
     */
    public List<UserRoleItem> getUserRoles() {
        return userRoles;
    }

    /**
     * Sets the userRoles.
     * @param userRoles the userRoles to set
     */
    public void setUserRoles(List<UserRoleItem> userRoles) {
        this.userRoles = userRoles;
    }

    /**
     * Gets createdDate.
     * @return the createdDate
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets the createdDate.
     * @param createdDate the createdDate to set
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets lastLoginDate.
     * @return the lastLoginDate
     */
    public String getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * Sets the lastLoginDate.
     * @param lastLoginDate the lastLoginDate to set
     */
    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    /**
     * Gets lastFiveProjects.
     * @return the lastFiveProjects
     */
    public String getLastFiveProjects() {
        return lastFiveProjects;
    }

    /**
     * Sets the lastFiveProjects.
     * @param lastFiveProjects the lastFiveProjects to set
     */
    public void setLastFiveProjects(String lastFiveProjects) {
        this.lastFiveProjects = lastFiveProjects;
    }
}
