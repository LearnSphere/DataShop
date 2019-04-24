/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A status history item of an import queue item.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueStatusHistoryItem extends Item
        implements java.io.Serializable, Comparable  {

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Class attribute. */
    private ImportQueueItem importQueue;
    /** Class attribute. */
    private UserItem updatedBy;
    /** Class attribute. */
    private Date updatedTime;
    /** Class attribute. */
    private String status;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ImportQueueStatusHistoryItem() {
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for this item.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets importQueue.
     * @return the importQueue
     */
    public ImportQueueItem getImportQueue() {
        return importQueue;
    }

    /**
     * Sets the importQueue.
     * @param importQueue the importQueue to set
     */
    public void setImportQueue(ImportQueueItem importQueue) {
        this.importQueue = importQueue;
    }

    /**
     * Gets updatedBy.
     * @return the updatedBy
     */
    public UserItem getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the updatedBy.
     * @param updatedBy the updatedBy to set
     */
    public void setUpdatedBy(UserItem updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Gets updatedTime.
     * @return the updatedTime
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Sets the updatedTime.
     * @param updatedTime the updatedTime to set
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Gets status.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * @param status the status to set
     */
    public void setStatus(String status) {
        if (ImportQueueItem.STATUS_ENUM.contains(status)) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    //----- STANDARD ITEM METHODS :: toString, equals, hashCode, compareTo -----

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
         buffer.append(objectToStringFK("ImportQueue", getImportQueue()));
         buffer.append(objectToString("UpdatedTime", getUpdatedTime()));
         buffer.append(objectToStringFK("UpdatedBy", getUpdatedBy()));
         buffer.append(objectToString("Status", getStatus()));
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
        if (obj instanceof ImportQueueStatusHistoryItem) {
            ImportQueueStatusHistoryItem otherItem = (ImportQueueStatusHistoryItem)obj;

            if (!objectEqualsFK(this.getImportQueue(), otherItem.getImportQueue())) {
                return false;
            }
            if (!objectEquals(this.getUpdatedTime(), otherItem.getUpdatedTime())) {
                return false;
            }
            if (!objectEqualsFK(this.getUpdatedBy(), otherItem.getUpdatedBy())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getImportQueue());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUpdatedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUpdatedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>import_queue</li>
     * <li>updated_time</li>
     * <li>updated_by</li>
     * <li>status</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ImportQueueStatusHistoryItem otherItem = (ImportQueueStatusHistoryItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getImportQueue(), otherItem.getImportQueue());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUpdatedTime(), otherItem.getUpdatedTime());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUpdatedBy(), otherItem.getUpdatedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        return value;
    }
}