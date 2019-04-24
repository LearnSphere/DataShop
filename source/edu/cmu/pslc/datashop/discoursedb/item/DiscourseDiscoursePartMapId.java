/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the discourse_has_discourse_part relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Cindy Tipper
 * @version $Revision: 12724 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-11-05 13:30:26 -0500 (Thu, 05 Nov 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDiscoursePartMapId implements Serializable, Comparable {

    /** The discourse id (FK). */
    private Long discourseId;
    /** The discourse part id (FK). */
    private Long partId;

    /**
     * Constructor.
     */
    public DiscourseDiscoursePartMapId() { };

    /**
     * Full constructor.
     * @param discourseItem the discourse item for this composite key.
     * @param partItem the discourse part item for this composite key.
     */
    public DiscourseDiscoursePartMapId(DiscourseItem discourseItem, DiscoursePartItem partItem) {
        if (discourseItem != null) {
            this.discourseId = (Long)discourseItem.getId();
        }
        if (partItem != null) {
            this.partId = (Long)partItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param discourseId the discourse ID for this composite key.
     * @param partId the discourse part ID for this composite key.
     */
    public DiscourseDiscoursePartMapId(Long discourseId, Long partId) {
        this.discourseId = discourseId;
        this.partId = partId;
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
     * Get the partId.
     * @return the partId.
     */
    public Long getPartId() { return partId; }

    /**
     * Set the partId.
     * @param partId the partId to set.
     */
    public void setPartId(Long partId) {
        this.partId = partId;
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
         buffer.append("partId").append("='").append(partId).append("' ");
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
            DiscourseDiscoursePartMapId otherItem = (DiscourseDiscoursePartMapId)obj;
            if (!this.discourseId.equals(otherItem.getDiscourseId())) {
                return false;
            }
            if (!this.partId.equals(otherItem.getPartId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (partId != null ? partId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>discourse id</li>
     * <li>part id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscourseDiscoursePartMapId otherItem = (DiscourseDiscoursePartMapId)obj;
        int value = 0;

        if ((getDiscourseId() != null) && (otherItem.getDiscourseId() != null)) {
            value = getDiscourseId().compareTo(otherItem.getDiscourseId());
        } else if (getDiscourseId() != null) {
            value = 1;
        } else if (otherItem.getDiscourseId() != null) {
            value = -1;
        }
        if (value != 0) { return value; }

        if ((getPartId() != null) && (otherItem.getPartId() != null)) {
            value = getPartId().compareTo(otherItem.getPartId());
        } else if (getPartId() != null) {
            value = 1;
        } else if (otherItem.getPartId() != null) {
            value = -1;
        }

        return value;
    }

}
