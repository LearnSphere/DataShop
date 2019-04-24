/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;
import edu.cmu.pslc.datashop.util.LogException;
import java.util.ArrayList;
import java.util.List;

/**
 * Authorization maps a user to a project and contains the
 * authorization level which indicates the amount of
 * access the user has to a project.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10810 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-17 14:07:52 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AuthorizationItem extends Item implements java.io.Serializable, Comparable  {
    /**
     * Composite key for the authorization consisting of
     *  <ul>
     *      <li>user id</li>
     *      <li>project id</li>
     *  </ul>
     */
    private AuthorizationId id;
    /** Part of the composite key - FK to the user table. */
    private UserItem user;
    /** Part of the composite key - FK to the project table. */
    private ProjectItem project;
    /** The level of authorization for the user on this project. */
    private String level;

    /** Collection of all allowed items in the level enumeration. */
    private static final List LEVEL_ENUM = new ArrayList();
    /** Level enumeration field value - "admin". */
    public static final String LEVEL_ADMIN = "admin";
    /** Level enumeration field value - "edit". */
    public static final String LEVEL_EDIT = "edit";
    /** Level enumeration field value - "edit". */
    public static final String LEVEL_VIEW = "view";
    /** Default Level for Public Projects - "edit". */
    public static final String LEVEL_PUBLIC = LEVEL_EDIT;

    /**
     * Adds each level to the level enumeration list.
     */
    static {
        LEVEL_ENUM.add(LEVEL_VIEW);
        LEVEL_ENUM.add(LEVEL_EDIT);
        LEVEL_ENUM.add(LEVEL_ADMIN);
    }

    /** Default constructor. */
    public AuthorizationItem() {
        this.level = LEVEL_VIEW;
    }

    /**
     *  Constructor with id.
     *  @param userItem first half of the composite key
     *  @param projectItem second half of the composite key
     *  <ul>
     *      <li>userId</li>
     *      <li>projectId</li>
     *  </ul>
     */
    public AuthorizationItem(UserItem userItem, ProjectItem projectItem) {
        this.id = new AuthorizationId(userItem, projectItem);
        this.level = LEVEL_VIEW;
    }

    /**
     * Get the id.
     * @return AuthorizationId
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id AuthorizationId
     * @see AuthorizationId
     */
    protected void setId(AuthorizationId id) {
        this.id = id;
    }

    /**
     * Get user.
     * @return edu.cmu.pslc.datashop.item.UserItem
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
        this.id = new AuthorizationId(this.user, this.project);
    }

    /**
     * Get project.
     * @return edu.cmu.pslc.datashop.item.ProjectItem
     */
    public ProjectItem getProject() {
        return this.project;
    }

    /**
     * Set project.
     * @param project Part of the composite key - FK to the project table.
     */
    protected void setProject(ProjectItem project) {
        this.project = project;
    }

    /**
     * Public Set project method to update the composite key as well.
     * @param project Part of the composite key - FK to the project table.
     */
    public void setProjectExternal(ProjectItem project) {
        setProject(project);
        this.id = new AuthorizationId(this.user, this.project);
    }

    /**
     * Get level.
     * @return java.lang.String
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * Set level, which is an enumerated type that can only be view or edit.
     * @param level The level of authorization for the user on this project.
     */
    public void setLevel(String level) {
        if (LEVEL_ENUM.contains(level)) {
            this.level = level;
        } else {
            throw new LogException(
                    "Authorization level can only be view or edit and not : " + level);
        }
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
         buffer.append(objectToString("Level", getLevel()));
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
        if (obj instanceof AuthorizationItem) {
            AuthorizationItem otherItem = (AuthorizationItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEquals(this.getLevel(), otherItem.getLevel())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLevel());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>authorization id</li>
     * <li>level</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AuthorizationItem otherItem = (AuthorizationItem)obj;
        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLevel(), otherItem.getLevel());
        if (value != 0) { return value; }

        return value;
    }
}