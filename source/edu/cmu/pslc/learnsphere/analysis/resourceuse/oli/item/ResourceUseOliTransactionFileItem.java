/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * OLI resource use transaction file.
 *
 * @author Hui Cheng
 * @version $Revision: 13980 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-03-07 15:57:59 -0500 (Tue, 07 Mar 2017) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ResourceUseOliTransactionFileItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id. */
    private Integer id;
    /** file_name*/
    private String fileName;

    /** Default constructor. */
    public ResourceUseOliTransactionFileItem() {
    }

    /**
     * Constructor with id.
     * @param resourceUseOliTransactionFileId Database generated unique id
     */
    public ResourceUseOliTransactionFileItem(Integer resourceUseOliTransactionFileId) {
        this.id = resourceUseOliTransactionFileId;
    }

    /**
     * Get resourceUseOliTransactionFileId.
     * @return Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set resourceUseOliTransactionFileId.
     * @param id Database generated unique id
     */
    public void setId(Integer resourceUseOliTransactionFileId) {
        this.id = resourceUseOliTransactionFileId;
    }
    
    /** Returns fileName. @return Returns the fileName. */
    public String getFileName() {
        return fileName;
    }

    /** Set fileName. @param fileName The fileName to set. */
    public void setFileName(String fileName) {
        this.fileName = fileName;
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
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToString("FileName", getFileName()));
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
        if (obj instanceof ResourceUseOliTransactionFileItem) {
            ResourceUseOliTransactionFileItem otherItem = (ResourceUseOliTransactionFileItem)obj;
            if (!objectEquals(this.getFileName(), otherItem.getFileName())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFileName());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResourceUseOliTransactionFileItem otherItem = (ResourceUseOliTransactionFileItem)obj;
        int value = 0;
        
        value = objectCompareTo(this.getFileName(), otherItem.getFileName());
        if (value != 0) { return value; }
        return value;
    }
}