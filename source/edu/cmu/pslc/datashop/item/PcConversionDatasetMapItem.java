/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.util.UtilConstants;
import edu.cmu.pslc.datashop.util.LogException;

/**
 * Represents a mapping between a dataset and a PC conversion.
 *
 * @author Cindy Tipper
 * @version $Revision: 10992 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-05-07 13:22:53 -0400 (Wed, 07 May 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PcConversionDatasetMapItem extends Item implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private PcConversionDatasetMapId id;

    /** The PcConversion item associated with this item. */
    private PcConversionItem pcConversion;

    /** The Dataset item associated with this item. */
    private DatasetItem dataset;

    /** The status of the mapping. */
    private String status;

    /** The number of problem mapped. */
    private Long numProblemsMapped;

    /** The user that mapped this association. */
    private UserItem mappedBy;

    /** The date this association was mapped. */
    private Date mappedTime;

    /** Collection of allowed values for the status enumeration. */
    private static final List<String> STATUS_ENUM = new ArrayList();
    /** Status enum value - pending. */
    public static final String STATUS_PENDING = "pending";
    /** Status enum value - complete. */
    public static final String STATUS_COMPLETE = "complete";
    /** Status enum value - error. */
    public static final String STATUS_ERROR = "error";

    /** Add each status to the list. */
    static {
        STATUS_ENUM.add(STATUS_PENDING);
        STATUS_ENUM.add(STATUS_COMPLETE);
        STATUS_ENUM.add(STATUS_ERROR);
    }

    /** Default constructor. */
    public PcConversionDatasetMapItem() {
        this.status = STATUS_PENDING;
    };

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(PcConversionDatasetMapId id) {
        this.id = id;
    }

    /**
     * Get the pcConversion.
     * @return the pcConversion
     */
    public PcConversionItem getPcConversion() {
        return pcConversion;
    }

    /**
     * Set the pcConversion.
     * @param pcConversion the pcConversion to set
     */
    protected void setPcConversion(PcConversionItem pcConversion) {
        this.pcConversion = pcConversion;
    }

    /**
     * Public set method to update the composite key as well.
     * @param pcConversionItem Part of the composite key - FK
     */
    public void setPcConversionExternal(PcConversionItem pcConversionItem) {
        setPcConversion(pcConversionItem);
        this.id = new PcConversionDatasetMapId(this.pcConversion, this.dataset);
    }

    /**
     * Get the DATASET.
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set the DATASET.
     * @param dataset the DATASET to set
     */
    protected void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Public set DATASET method to update the composite key as well.
     * @param dataset Part of the composite key - FK
     */
    public void setDatasetExternal(DatasetItem dataset) {
        setDataset(dataset);
        this.id = new PcConversionDatasetMapId(this.pcConversion, this.dataset);
    }

    /**
     * Get the status of this mapping.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status of this mapping.
     * @param status the value
     */
    public void setStatus(String status) {
        if (STATUS_ENUM.contains(status)) {
            this.status = status;
        } else {
            throw new LogException("PcConversionDatasetMap status can not be : " + status);
        }
    }

    /**
     * Get the number of problems mapped.
     * @return the number of problems mapped
     */
    public Long getNumProblemsMapped() {
        return numProblemsMapped;
    }

    /**
     * Set the number of problems mapped.
     * @param numProblems the number of problems mapped
     */
    public void setNumProblemsMapped(Long numProblems) {
        this.numProblemsMapped = numProblems;
    }

    /**
     * Increment the number of problems mapped.
     */
    public void incrementNumProblemsMapped() {
        this.numProblemsMapped++;
    }

    /**
     * Get the user that mapped this association.
     * @return the mappedBy
     */
    public UserItem getMappedBy() {
        return mappedBy;
    }

    /**
     * Set the user that mapped this association.
     * @param mappedBy the user
     */
    public void setMappedBy(UserItem mappedBy) {
        this.mappedBy = mappedBy;
    }

    /**
     * Get the date this association was mapped.
     * @return Date mappedTime
     */
    public Date getMappedTime() {
        return mappedTime;
    }

    /**
     * Set the date this association was mapped.
     * @param mappedTime date association was mapped
     */
    public void setMappedTime(Date mappedTime) {
        this.mappedTime = mappedTime;
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
        buffer.append(objectToStringFK("PcConversion", getPcConversion()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToString("Status", getStatus()));
        buffer.append(objectToString("NumProblemsMapped", getNumProblemsMapped()));
        buffer.append(objectToStringFK("MappedBy", getMappedBy()));
        buffer.append(objectToString("MappedTime", getMappedTime()));
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
        if (obj instanceof PcConversionDatasetMapItem) {
            PcConversionDatasetMapItem otherItem = (PcConversionDatasetMapItem)obj;

            if (!objectEqualsFK(this.getPcConversion(), otherItem.getPcConversion())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!objectEquals(this.getNumProblemsMapped(), otherItem.getNumProblemsMapped())) {
                return false;
            }
            if (!objectEqualsFK(this.getMappedBy(), otherItem.getMappedBy())) {
                return false;
            }
            if (!objectEquals(this.getMappedTime(), otherItem.getMappedTime())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getPcConversion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getNumProblemsMapped());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getMappedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getMappedTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>pcConversion</li>
     * <li>dataset</li>
     * <li>status</li>
     * <li>numProblemsMapped</li>
     * <li>mappedBy</li>
     * <li>mappedTime</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        PcConversionDatasetMapItem otherItem = (PcConversionDatasetMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getPcConversion(), otherItem.getPcConversion());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumProblemsMapped(), otherItem.getNumProblemsMapped());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getMappedBy(), otherItem.getMappedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getMappedTime(), otherItem.getMappedTime());
        if (value != 0) { return value; }

        return value;
    }
}
