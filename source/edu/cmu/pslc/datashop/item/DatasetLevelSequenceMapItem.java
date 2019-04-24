/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a dataset level sequence map record of the system.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelSequenceMapItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private DatasetLevelSequenceMapId id;

    /** The dataset level sequence item associated with this item. */
    private DatasetLevelSequenceItem datasetLevelSequence;

    /** The dataset level item associated with this item. */
    private DatasetLevelItem datasetLevel;

    /** The sequence for this item. */
    private Integer sequence;

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
    public void setId(DatasetLevelSequenceMapId id) {
        this.id = id;
    }

    /**
     * Get the datasetLevel.
     * @return the datasetLevel
     */
    public DatasetLevelItem getDatasetLevel() {
        return datasetLevel;
    }

    /**
     * Set the datasetLevel.
     * @param datasetLevel the datasetLevel to set
     */
    protected void setDatasetLevel(DatasetLevelItem datasetLevel) {
        this.datasetLevel = datasetLevel;
    }

    /**
     * Public Set dataset level method to update the composite key as well.
     * @param datasetLevel Part of the composite key - FK to the dataset_level table.
     */
    public void setDatasetLevelExternal(DatasetLevelItem datasetLevel) {
        setDatasetLevel(datasetLevel);
        this.id = new DatasetLevelSequenceMapId(this.datasetLevelSequence, this.datasetLevel);
    }

    /**
     * Get the datasetLevelSequence.
     * @return the datasetLevelSequence
     */
    public DatasetLevelSequenceItem getDatasetLevelSequence() {
        return datasetLevelSequence;
    }

    /**
     * Set the datasetLevelSequence.
     * @param datasetLevelSequence the datasetLevelSequence to set
     */
    protected void setDatasetLevelSequence(
            DatasetLevelSequenceItem datasetLevelSequence) {
        this.datasetLevelSequence = datasetLevelSequence;
    }

    /**
     * Public Set dataset level sequence method to update the composite key as well.
     * @param datasetLevelSequence Part of the composite key -
     *      FK to the dataset_level_sequence table.
     */
    public void setDatasetLevelSequenceExternal(DatasetLevelSequenceItem datasetLevelSequence) {
        setDatasetLevelSequence(datasetLevelSequence);
        this.id = new DatasetLevelSequenceMapId(this.datasetLevelSequence, this.datasetLevel);
    }

    /**
     * Get the sequence.
     * @return the sequence
     */
    public Integer getSequence() {
        return sequence;
    }

    /**
     * Set the sequence.
     * @param sequence the sequence to set
     */
    public void setSequence(Integer sequence) {
        this.sequence = sequence;
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
        buffer.append(objectToStringFK("DatasetLevelSequence", getDatasetLevelSequence()));
        buffer.append(objectToStringFK("DatasetlLevel", getDatasetLevel()));
        buffer.append(objectToString("Sequence", this.getSequence()));
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
        if (obj instanceof DatasetLevelSequenceMapItem) {
            DatasetLevelSequenceMapItem otherItem = (DatasetLevelSequenceMapItem)obj;

            if (!objectEqualsFK(this.getDatasetLevelSequence(),
                    otherItem.getDatasetLevelSequence())) {
                return false;
            }
            if (!objectEqualsFK(this.getDatasetLevel(), otherItem.getDatasetLevel())) {
                return false;
            }
            if (!objectEquals(this.getSequence(), otherItem.getSequence())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSequence());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDatasetLevelSequence());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDatasetLevel());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>datasetLevelSequence</li>
     * <li>datasetLevel</li>
     * <li>sequence</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetLevelSequenceMapItem otherItem = (DatasetLevelSequenceMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDatasetLevelSequence(),
                otherItem.getDatasetLevelSequence());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDatasetLevel(), otherItem.getDatasetLevel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSequence(), otherItem.getSequence());
        if (value != 0) { return value; }

        return value;
    }
} // end DatasetLevelSequenceMapItem.java
