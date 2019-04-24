/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Composite key for the UserRoleItem.  Consists of 2 fields, where one is a
 * foreign key as well. Note, the fields are immutable.  See Josh Bloch's book,
 * pages 122-123, about defensive copying.
 * <ul>
 *    <li>userId - User Id as a 250 length varchar</li>
 *    <li>role - an enumerated type, string</li>
 * </ul>
 *
 * @author Alida Skogsholm
 * @version $Revision: 13753 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-01-17 13:15:03 -0500 (Tue, 17 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserRoleId implements java.io.Serializable, Comparable  {

    /** Part of the composite key - FK to the user table. */
    private String userId;
    /** Part of the composite key. */
    private String role;

    /** Default constructor. */
    public UserRoleId() {
    }

    /**
     * Full constructor, preferred, immutable.
     * @param userItem the user item
     * @param role the role of the user
     */
    public UserRoleId(UserItem userItem, String role) {
        if (userItem != null) {
            this.userId = new String(((String)userItem.getId()));
        }

        if (role != null) {
            this.role = role;
        }
    }

    /**
     * Full constructor.
     * @param userId the user id
     * @param role the user's role
     */
    public UserRoleId(String userId, String role) {
        this.userId = new String(userId);
        this.role = role;
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
     * Get role.
     * @return role
     */
    public String getRole() {
        return this.role;
    }

    /**
     * Set role.
     * @param role Part of the composite key.
     */
    protected void setRole(String role) {
        this.role = role;
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
         buffer.append("role").append("='").append(role).append("' ");
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
            UserRoleId otherItem = (UserRoleId)obj;

            if (!this.userId.equals(otherItem.userId)) {
                 return false;
            }
            if (!this.role.equals(otherItem.role)) {
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
        hash = hash * UtilConstants.HASH_PRIME + (role != null ? role.hashCode() : 0);
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
        UserRoleId otherItem = (UserRoleId)obj;
        if (this.userId.compareTo(otherItem.userId) != 0) {
            return this.userId.compareTo(otherItem.userId);
        }
        if (this.role.compareTo(otherItem.role) != 0) {
            return this.role.compareTo(otherItem.role);
        }
        return 0;
    }
}