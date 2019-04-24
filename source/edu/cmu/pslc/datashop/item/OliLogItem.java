/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a OliLog of the system.
 *
 * @author Hui Cheng
 * @version $Revision: 7507 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-03-13 13:36:42 -0400 (Tue, 13 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */

public class OliLogItem extends Item implements java.io.Serializable, Comparable  {

    /** GUID for this Oli_log record. */
    private String guid;
    /** imported_time as Date */
    private Date importedTime;
    /** Indicates whether this record was imported to message table or if an error occurred. */
    private String importedFlag;
    /** imported_time as Date */
    private Date serverReceiptTime;

    /** Default constructor. */
    public OliLogItem() {
    }

    /**
     * Constructor with GUID.
     * @param guid the id for this table
     */
    public OliLogItem(String guid) {
        this.guid = guid;
    }

    /**
     * Returns the GUID.
     * @return String
     */
    public Comparable getId() {
        return this.guid;
    }

    /**
     * Set GUID.
     * @param guid the id
     */
    public void setId(String guid) {
        this.guid = guid;
    }

    /**
     * Get importedTime.
     * @return Date
     */
    public Date getImportedTime() {
        return this.importedTime;
    }

    /**
     * Set importedTime.
     * @param importedTime as Date
     */
    public void setImportedTime(Date importedTime) {
        this.importedTime = importedTime;
    }

    /**
     * Get importedFlag.
     * @return String.
     */
    public String getImportedFlag() {
        return this.importedFlag;
    }

    /**
     * Set importedFlag.
     * @param importedFlag as String.
     */
    public void setImportedFlag(String importedFlag) {
        this.importedFlag = importedFlag;
    }

    /**
     * Get serverReceiptTime.
     * @return Date
     */
    public Date getServerReceiptTime() {
        return this.serverReceiptTime;
    }

    /**
     * Set serverReceiptTime.
     * @param serverReceiptTime as Date
     */
    public void setServerReceiptTime(Date serverReceiptTime) {
        this.serverReceiptTime = serverReceiptTime;
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
        buffer.append(objectToString("GUID", getId()));
        buffer.append(objectToString("ImportedTime", getImportedTime()));
        buffer.append(objectToString("ImportedFlag", getImportedFlag()));
        buffer.append(objectToString("ServerReceiptTime", getServerReceiptTime()));
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
        if (obj instanceof OliLogItem) {
            OliLogItem otherItem = (OliLogItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEquals(this.getImportedTime(), otherItem.getImportedTime())) {
                return false;
            }
            if (!objectEquals(this.getImportedFlag(), otherItem.getImportedFlag())) {
                return false;
            }
            if (!objectEquals(this.getServerReceiptTime(), otherItem.getServerReceiptTime())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getImportedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getImportedFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getServerReceiptTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>GUID</li>
     * <li>importedTime</li>
     * <li>importedFlag</li>
     * <li>serverReceiptTime</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        OliLogItem otherItem = (OliLogItem)obj;

        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getImportedTime(), otherItem.getImportedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getImportedFlag(), otherItem.getImportedFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getServerReceiptTime(), otherItem.getServerReceiptTime());
        if (value != 0) { return value; }

        return value;
    }
}