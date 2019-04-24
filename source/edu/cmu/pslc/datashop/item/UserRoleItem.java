/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import java.util.ArrayList;
import java.util.List;

import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * UserRole maps a user to a role.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15454 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-08-31 13:00:06 -0400 (Fri, 31 Aug 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserRoleItem extends Item implements java.io.Serializable, Comparable  {
    /**
     * Composite key for the UserRole consisting of
     *  <ul>
     *      <li>user id</li>
     *      <li>role</li>
     *  </ul>
     */
    private UserRoleId id;

    /** Part of the composite key - FK to the user table. */
    private UserItem user;

    /** Part of the composite key. */
    private String role;

    /** Collection of all allowed items in the role enumeration. */
    private static final List ROLE_ENUM = new ArrayList();
    /** Role enumeration field value - "logging_activity". */
    public static final String ROLE_LOGGING_ACTIVITY = "logging_activity";
    /** Role enumeration field value - "web_services". */
    public static final String ROLE_WEB_SERVICES = "web_services";
    /** Role enumeration field value - "terms_of_use_manager". */
    public static final String ROLE_TERMS_OF_USE_MANAGER = "terms_of_use_manager";
    /** Role enumeration field value - "external_tools". */
    public static final String ROLE_EXTERNAL_TOOLS = "external_tools";
    /** Role enumeration field value - "research_manager". */
    public static final String ROLE_RESEARCH_MANAGER = "research_manager";
    /** Role enumeration field value - "datashop_edit". */
    public static final String ROLE_DATASHOP_EDIT = "datashop_edit";
    /** Role enumeration field value - "research_goal_edit". */
    public static final String ROLE_RESEARCH_GOAL_EDIT = "research_goal_edit";

    /**
     * Adds each role to the role enumeration list.
     */
    static {
        ROLE_ENUM.add(ROLE_LOGGING_ACTIVITY);
        ROLE_ENUM.add(ROLE_WEB_SERVICES);
        ROLE_ENUM.add(ROLE_TERMS_OF_USE_MANAGER);
        ROLE_ENUM.add(ROLE_EXTERNAL_TOOLS);
        ROLE_ENUM.add(ROLE_RESEARCH_MANAGER);
        ROLE_ENUM.add(ROLE_DATASHOP_EDIT);
        ROLE_ENUM.add(ROLE_RESEARCH_GOAL_EDIT);
    }

    /** Default constructor. */
    public UserRoleItem() {
        this.role = ROLE_LOGGING_ACTIVITY;
    }

    /**
     *  Constructor with id.
     *  @param userItem first half of the composite key
     *  @param role second half of the composite key
     *  <ul>
     *      <li>userId</li>
     *      <li>role</li>
     *  </ul>
     */
    public UserRoleItem(UserItem userItem, String role) {
        this.id = new UserRoleId(userItem, role);
        this.user = userItem;
        this.role = role;
    }

    /**
     * Get the id.
     * @return UserRoleId
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id UserRoleId
     * @see UserRoleId
     */
    protected void setId(UserRoleId id) {
        this.id = id;
        // [Bugfix] : otherwise Hibernate will not load the role (because it's composite key)
        this.role = id.getRole();
    }

    /**
     * Get user.
     * @return the user item
     */
    public UserItem getUser() {
        return this.user;
    }

    /**
     * Set user.
     * @param user Part of the composite key - FK to the user table.
     */
    protected void setUser(UserItem user) {
        this.user = user;
    }

    /**
     * Public Set user method to update the composite key as well.
     * @param user Part of the composite key - FK to the user table.
     */
    public void setUserExternal(UserItem user) {
        setUser(user);
        this.id = new UserRoleId(this.user, this.role);
    }

    /**
     * Get role.
     * @return role
     */
    public String getRole() {
        return this.role;
    }

    /**
     * Set role, which is an enumerated type that can only be view or edit.
     * @param role The role of UserRole for the user on this project.
     */
    public void setRole(String role) {
        if (ROLE_ENUM.contains(role)) {
            this.role = role;
        } else {
            throw new LogException("UserRole cannot be: " + role);
        }
    }

    /**
     * Public Set role method.
     * @param role the role to give this user
     */
    public void setRoleExternal(String role) {
        setRole(role);
        this.id = new UserRoleId(this.user, this.role);
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append(objectToString("Id", getId()));
         buffer.append(objectToString("Role", getRole()));
         buffer.append("]");
         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UserRoleItem) {
            UserRoleItem otherItem = (UserRoleItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getId());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>UserRole id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        UserRoleItem otherItem = (UserRoleItem)obj;
        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        return value;
    }
}