/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The import queue mode.  There is only one row in this table.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueModeItem extends Item implements java.io.Serializable, Comparable  {

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private Integer id;
    /** Class attribute. */
    private String mode;
    /** Class attribute. */
    private UserItem updatedBy;
    /** Class attribute. */
    private Date updatedTime;
    /** Class attribute. */
    private String status;
    /** Class attribute. */
    private Date statusTime;
    /** Class attribute. */
    private ImportQueueItem importQueue;
    /** Class attribute. */
    private Boolean exitFlag;

    //----- ID -----
    /** Constant for the id of the only and only mode item. */
    public static final Integer ID = 1;

    //----- ENUM : format -----

    /** Enumerated type list of valid values. */
    private static final List<String> MODE_ENUM = new ArrayList<String>();
    /** Enumerated type constant for mode,  "pause". */
    public static final String MODE_PAUSE = "pause";
    /** Enumerated type constant for mode,  "play". */
    public static final String MODE_PLAY  = "play";
    /** Enumerated type constant for mode,  "error". */
    public static final String MODE_ERROR = "error";

    static {
        MODE_ENUM.add(MODE_PAUSE);
        MODE_ENUM.add(MODE_PLAY);
        MODE_ENUM.add(MODE_ERROR);
    }

    //----- ENUM : status -----

    /** Enumerated type list of valid values. */
    static final List<String> STATUS_ENUM = new ArrayList<String>();
    /** Enumerated type constant. */
    public static final String STATUS_WAITING = "waiting";
    /** Enumerated type constant. */
    public static final String STATUS_VERIFYING = "verifying";
    /** Enumerated type constant. */
    public static final String STATUS_IMPORTING = "importing";
    /** Enumerated type constant. */
    public static final String STATUS_ERROR = "error";

    static {
        STATUS_ENUM.add(STATUS_WAITING);
        STATUS_ENUM.add(STATUS_VERIFYING);
        STATUS_ENUM.add(STATUS_IMPORTING);
        STATUS_ENUM.add(STATUS_ERROR);
    }

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ImportQueueModeItem() {
    }

    /**
     *  Constructor with id.
     *  @param importQueueModeId Set to 1.
     */
    public ImportQueueModeItem(Integer importQueueModeId) {
        this.id = importQueueModeId;
    }

    //----- GETTERS and SETTERS -----

    /**
     * Required method which makes no sense for this item, but whatever.
     * @return the id
     */
    public Comparable getId() {
        return ID;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for this item.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets mode.
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the mode.
     * @param mode the mode to set
     */
    public void setMode(String mode) {
        if (mode == null) {
            this.mode = null;
        } else if (MODE_ENUM.contains(mode)) {
            this.mode = mode;
        } else {
            throw new IllegalArgumentException("Invalid mode value: " + mode);
        }
    }

    /**
     * Check if the Import Queue is in play mode.
     * @return true if it is, false otherwise
     */
    public boolean inPlayMode() {
        if (mode.equals(ImportQueueModeItem.MODE_PLAY)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the Import Queue is paused.
     * @return true if it is, false otherwise
     */
    public boolean isPaused() {
        if (mode.equals(ImportQueueModeItem.MODE_PAUSE)) {
            return true;
        }
        return false;
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
     * Return true if currently verifying.
     * @return true if verifying status
     */
    public boolean isVerifying() {
        if (status.equals(STATUS_VERIFYING)) {
            return true;
        }
        return false;
    }

    /**
     * Sets the status.
     * @param status the status to set
     */
    public void setStatus(String status) {
        if (status == null) {
            this.status = null;
        } else if (STATUS_ENUM.contains(status)) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    /**
     * Gets statusTime.
     * @return the statusTime
     */
    public Date getStatusTime() {
        return statusTime;
    }

    /**
     * Sets the statusTime.
     * @param statusTime the statusTime to set
     */
    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
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
     * Gets exitFlag.
     * @return the exit flag
     */
    public Boolean getExitFlag() { 
        return exitFlag;
    }

    /**
     * Sets the exitFlag.
     * @param exitFlag the exit flag value
     */
    public void setExitFlag(Boolean exitFlag) {
        this.exitFlag = exitFlag;
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
         buffer.append(objectToString("Mode", getMode()));
         buffer.append(objectToStringFK("UpdatedBy", getUpdatedBy()));
         buffer.append(objectToString("UpdatedTime", getUpdatedTime()));
         buffer.append(objectToString("Status", getStatus()));
         buffer.append(objectToString("StatusTime", getStatusTime()));
         buffer.append(objectToStringFK("ImportQueue", getImportQueue()));
         buffer.append(objectToString("ExitFlag", getExitFlag()));
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
        if (obj instanceof ImportQueueModeItem) {
            ImportQueueModeItem otherItem = (ImportQueueModeItem)obj;

            if (!objectEquals(this.getMode(), otherItem.getMode())) {
                return false;
            }
            if (!objectEqualsFK(this.getUpdatedBy(), otherItem.getUpdatedBy())) {
                return false;
            }
            if (!objectEquals(this.getUpdatedTime(), otherItem.getUpdatedTime())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!objectEquals(this.getStatusTime(), otherItem.getStatusTime())) {
                return false;
            }
            if (!objectEqualsFK(this.getImportQueue(), otherItem.getImportQueue())) {
                return false;
            }
            if (!objectEquals(this.getExitFlag(), otherItem.getExitFlag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getMode());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUpdatedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUpdatedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatusTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getImportQueue());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getExitFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>mode</li>
     * <li>updated by</li>
     * <li>updated time</li>
     * <li>status</li>
     * <li>status time</li>
     * <li>import queue</li>
     * <li>exit flag</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ImportQueueModeItem otherItem = (ImportQueueModeItem)obj;
        int value = 0;

        value = objectCompareTo(this.getMode(), otherItem.getMode());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUpdatedBy(), otherItem.getUpdatedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUpdatedTime(), otherItem.getUpdatedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatusTime(), otherItem.getStatusTime());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getImportQueue(), otherItem.getImportQueue());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getExitFlag(), otherItem.getExitFlag());
        if (value != 0) { return value; }

        return value;
    }
}