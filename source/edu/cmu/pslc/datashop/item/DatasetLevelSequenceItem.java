/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a Session record of the system.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelSequenceItem extends Item
        implements Serializable, Comparable {

    /** The dataset_level_sequence_id for this item, auto-incremented. */
    private Comparable datasetLevelSequenceId;

    /** The name for this sequence. */
    private String name;

    /** The dataset to which this sequence belongs. */
    private DatasetItem dataset;

    /**
     * Get the dataset for this sequence.
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set the dataset for this sequence.
     * @param dataset the dataset to set
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Get the datasetLevelSequenceId.
     * @return the datasetLevelSequenceId
     */
    public Comparable getId() {
        return getDatasetLevelSequenceId();
    }

    /**
     * Set the datasetLevelSequenceId.
     * @param id the id to set
     */
    public void setId(Integer id) {
        setDatasetLevelSequenceId(id);
    }

    /**
     * Get the datasetLevelSequenceId.
     * @return the datasetLevelSequenceId
     */
    private Comparable getDatasetLevelSequenceId() {
        return datasetLevelSequenceId;
    }

    /**
     * Set the datasetLevelSequenceId.
     * @param datasetLevelSequenceId the datasetLevelSequenceId to set
     */
    private void setDatasetLevelSequenceId(Integer datasetLevelSequenceId) {
        this.datasetLevelSequenceId = datasetLevelSequenceId;
    }

    /**
     * Get the datasetLevelSequenceName.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the sequence name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
        buffer.append(objectToString("DatasetLevelSequenceId", getId()));
        buffer.append(objectToString("DatasetLevelSequenceName", getName()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
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
        if (obj instanceof DatasetLevelSequenceItem) {
            DatasetLevelSequenceItem otherItem = (DatasetLevelSequenceItem)obj;

            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEquals(this.getName(), otherItem.getName())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset</li>
     * <li>name</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetLevelSequenceItem otherItem = (DatasetLevelSequenceItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }

        return value;
    }

} // end DatasetLevelSequenceItem.java
