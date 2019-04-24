/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the user_memberof_discourse relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Cindy Tipper
 * @version $Revision: 12725 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-11-05 13:36:46 -0500 (Thu, 05 Nov 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DUserDiscourseMapId implements Serializable, Comparable {

    /** The discourse id (FK). */
    private Long discourseId;
    /** The user id (FK). */
    private Long userId;

    /**
     * Constructor.
     */
    public DUserDiscourseMapId() { };

    /**
     * Full constructor.
     * @param discourseItem the discourse item for this composite key.
     * @param userItem the user item for this composite key.
     */
    public DUserDiscourseMapId(DiscourseItem discourseItem, DUserItem userItem) {
        if (discourseItem != null) {
            this.discourseId = (Long)discourseItem.getId();
        }
        if (userItem != null) {
            this.userId = (Long)userItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param discourseId the discourse ID for this composite key.
     * @param userId the user ID for this composite key.
     */
    public DUserDiscourseMapId(Long discourseId, Long userId) {
        this.discourseId = discourseId;
        this.userId = userId;
    }

    /**
     * Get the discourseId.
     * @return the discourseId
     */
    public Long getDiscourseId() { return this.discourseId; }

    /**
     * Set the discourseId.
     * @param discourseId the discourseId to set
     */
    public void setDiscourseId(Long discourseId) {
        this.discourseId = discourseId;
    }

    /**
     * Get the userId.
     * @return the userId.
     */
    public Long getUserId() { return userId; }

    /**
     * Set the userId.
     * @param userId the userId to set.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * @return a string representation of this item
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append("discourseId").append("='").append(discourseId).append("' ");
         buffer.append("userId").append("='").append(userId).append("' ");
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
            DUserDiscourseMapId otherItem = (DUserDiscourseMapId)obj;
            if (!this.discourseId.equals(otherItem.getDiscourseId())) {
                return false;
            }
            if (!this.userId.equals(otherItem.getUserId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (discourseId != null ? discourseId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME + (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>discourse id</li>
     * <li>user id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DUserDiscourseMapId otherItem = (DUserDiscourseMapId)obj;
        int value = 0;

        if ((getDiscourseId() != null) && (otherItem.getDiscourseId() != null)) {
            value = getDiscourseId().compareTo(otherItem.getDiscourseId());
        } else if (getDiscourseId() != null) {
            value = 1;
        } else if (otherItem.getDiscourseId() != null) {
            value = -1;
        }
        if (value != 0) { return value; }

        if ((getUserId() != null) && (otherItem.getUserId() != null)) {
            value = getUserId().compareTo(otherItem.getUserId());
        } else if (getUserId() != null) {
            value = 1;
        } else if (otherItem.getUserId() != null) {
            value = -1;
        }

        return value;
    }

}
