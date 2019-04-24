/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Composite key for the authorizationItem.  Consists of 2 fields that are both
 * foreign keys as well. Note, the fields are immutable.  See Josh Bloch's book,
 * pages 122-123, about defensive copying.
 * <ul>
 *    <li>userId - User Id as a 250 length varchar</li>
 *    <li>projectId - database generated id of a project</li>
 * </ul>
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13753 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-01-17 13:15:03 -0500 (Tue, 17 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AuthorizationId implements java.io.Serializable, Comparable  {

    /** Part of the composite key - FK to the user table. */
    private String userId;
    /** Part of the composite key - FK to the project table. */
    private Integer projectId;

    /** Default constructor. */
    public AuthorizationId() {
    }

    /**
     * Full constructor, preferred.
     * immutable.
     * @param userItem the user item
     * @param projectItem the project item
     */
    public AuthorizationId(UserItem userItem, ProjectItem projectItem) {
        if (userItem != null) {
            this.userId = new String(((String)userItem.getId()));
        }

        if (projectItem != null) {
            this.projectId = new Integer(((Integer)projectItem.getId()).intValue());
        }
    }

    /**
     * Full constructor.
     * @param userId the user id
     * @param projectId the project id
     */
    public AuthorizationId(String userId, Integer projectId) {
        this.userId = new String(userId);
        this.projectId = new Integer(projectId.intValue());
    }

    /**
     * Get userId.
     * @return java.lang.String
     */
    public Comparable getUserId() {
        return new String(this.userId);
    }

    /**
     * Set userId.
     * @param userId Part of the composite key - FK to the user table.
     */
    protected void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get projectId.
     * @return java.lang.Integer
     */
    public Integer getProjectId() {
        return new Integer(this.projectId.intValue());
    }

    /**
     * Set projectId.
     * @param projectId Part of the composite key - FK to the project table.
     */
    protected void setProjectId(Integer projectId) {
        this.projectId = projectId;
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
         buffer.append("userId").append("='").append(userId).append("' ");
         buffer.append("projectId").append("='").append(projectId).append("' ");
         buffer.append("]");

         return buffer.toString();
    }

     /**
      * Determines whether another object is equal to this one.
      * @param obj the object to test equality with this one
      * @return true if the items are equal, false otherwise
      */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            AuthorizationId otherItem = (AuthorizationId)obj;

            if (!this.userId.equals(otherItem.userId)) {
                 return false;
            }
            if (!this.projectId.equals(otherItem.projectId)) {
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
        int hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + (userId != null ? userId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME + (projectId != null ? projectId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>user id</li>
     * <li>project id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AuthorizationId otherItem = (AuthorizationId)obj;
        if (this.userId.compareTo(otherItem.userId) != 0) {
            return this.userId.compareTo(otherItem.userId);
        }
        if (this.projectId.compareTo(otherItem.projectId) != 0) {
            return this.projectId.compareTo(otherItem.projectId);
        }
        return 0;
    }
}