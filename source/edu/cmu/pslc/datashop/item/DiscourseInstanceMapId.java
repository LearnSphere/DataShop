/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the discourse_instance_map relation.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseInstanceMapId implements Serializable, Comparable {

    /** The discourse id (FK). */
    private Long discourseId;
    /** The remote instance id (FK). */
    private Long remoteInstanceId;

    /**
     * Constructor.
     */
    public DiscourseInstanceMapId() { };

    /**
     * Full constructor.
     * @param discourseItem the discourse item for this composite key.
     * @param instanceItem the remote instance item for this composite key.
     */
    public DiscourseInstanceMapId(DiscourseItem discourseItem, RemoteInstanceItem instanceItem) {
        if (discourseItem != null) {
            this.discourseId = (Long)discourseItem.getId();
        }
        if (instanceItem != null) {
            this.remoteInstanceId = (Long)instanceItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param discourseId the discourse ID for this composite key.
     * @param instanceId the remote instance ID for this composite key.
     */
    public DiscourseInstanceMapId(Long discourseId, Long instanceId) {
        this.discourseId = discourseId;
        this.remoteInstanceId = instanceId;
    }

    /**
     * Get the discourseId.
     * @return the discourseId
     */
    public Long getDiscourseId() {
        return discourseId;
    }

    /**
     * Set the discourseId.
     * @param discourseId the discourseId to set
     */
    public void setDiscourseId(Long discourseId) {
        this.discourseId = discourseId;
    }

    /**
     * Get the remoteInstanceId.
     * @return the remoteInstanceId.
     */
    public Long getRemoteInstanceId() {
        return remoteInstanceId;
    }

    /**
     * Set the remoteInstanceId.
     * @param remoteInstanceId the remoteInstanceId to set.
     */
    public void setRemoteInstanceId(Long remoteInstanceId) {
        this.remoteInstanceId = remoteInstanceId;
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
         buffer.append("discourseId = ");
         buffer.append(discourseId);
         buffer.append(", remoteInstanceId = ");
         buffer.append(remoteInstanceId);
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
            DiscourseInstanceMapId otherItem = (DiscourseInstanceMapId)obj;
            if (!this.discourseId.equals(otherItem.getDiscourseId())) {
                return false;
            }
            if (!this.remoteInstanceId.equals(otherItem.getRemoteInstanceId())) {
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
        hash = hash * UtilConstants.HASH_PRIME
            + (remoteInstanceId != null ? remoteInstanceId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>external tool id</li>
     * <li>file id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscourseInstanceMapId otherItem = (DiscourseInstanceMapId)obj;
        
        if (this.discourseId.compareTo(otherItem.discourseId) != 0) {
            return this.discourseId.compareTo(otherItem.discourseId);
        }
        if (this.remoteInstanceId.compareTo(otherItem.remoteInstanceId) != 0) {
            return this.remoteInstanceId.compareTo(otherItem.remoteInstanceId);
        }
        return 0;
    }

}
