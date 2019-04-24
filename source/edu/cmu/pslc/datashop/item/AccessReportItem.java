/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Class defining access report information.
 *
 * @author Young Suk Ahn
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessReportItem extends Item implements java.io.Serializable  {
    /**
     * Composite key consisting of
     *  <ul>
     *      <li>user id</li>
     *      <li>project id</li>
     *  </ul>
     */
    private AuthorizationId id;
    /** User item associated with this Access Request History. */
    private UserItem user;
    /** Project associated with this dataset. */
    private ProjectItem project;

    /** First access timestamp **/
    private Date firstAccess;
    /** Last access timestamp **/
    private Date lastAccess;

    /**
     * Default constructor.
     */
    public AccessReportItem() { }

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
     * Gets the User Item.
     * @return UserItem
     */
    public UserItem getUser() {
        return user;
    }

    /**
     * Sets the user item.
     * @param user the user item
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
     * Gets the project item.
     * @return ProjectItem
     */
    public ProjectItem getProject() {
        return project;
    }

    /**
     * Set's the project item.
     * @param project the Project
     */
    public void setProject(ProjectItem project) {
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
     * Gets first access timestamp.
     * @return Date first access
     */
    public Date getFirstAccess() {
        return firstAccess;
    }

    /**
     * Sets first access timestamp.
     * @param firstAccess Date
     */
    public void setFirstAccess(Date firstAccess) {
        this.firstAccess = firstAccess;
    }

    /**
     * Gets last access timestamp.
     * @return Date last access
     */
    public Date getLastAccess() {
        return lastAccess;
    }

    /**
     * Sets first access timestamp.
     * @param lastAccess Date
     */
    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
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
        if (obj instanceof AccessReportItem) {
            AccessReportItem otherItem = (AccessReportItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEquals(this.getFirstAccess(), otherItem.getFirstAccess())) {
                return false;
            }
            if (!objectEquals(this.getLastAccess(), otherItem.getLastAccess())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFirstAccess());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastAccess());
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
        AccessReportItem otherItem = (AccessReportItem)obj;
        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFirstAccess(), otherItem.getFirstAccess());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastAccess(), otherItem.getLastAccess());
        if (value != 0) { return value; }

        return value;
    }
}