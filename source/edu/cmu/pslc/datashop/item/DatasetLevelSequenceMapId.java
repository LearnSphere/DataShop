/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the dataset_level_sequence_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelSequenceMapId implements Serializable, Comparable {

    /** The dataset level sequence id.  Dataset level sequence id serves as a
      * FK into the dataset_level_sequence relation. */
    private Integer datasetLevelSequenceId;
    /** The dataset level id.  Dataset level id serves as a FK into the
      * dataset_level relation. */
    private Integer datasetLevelId;

    /**
     * Constructor!
     */
    public DatasetLevelSequenceMapId() { };

    /**
     * Full constructor.
     * @param datasetLevelSequence the datasetLevelSequence item for this composite key.
     * @param datasetLevel the datasetLevel for this composite key.
     */
    public DatasetLevelSequenceMapId(DatasetLevelSequenceItem datasetLevelSequence,
            DatasetLevelItem datasetLevel) {
        if (datasetLevelSequence != null) {
            this.datasetLevelSequenceId = (Integer)datasetLevelSequence.getId();
        }
        if (datasetLevel != null) {
            this.datasetLevelId = (Integer)datasetLevel.getId();
        }
    }

    /**
     * Full constructor.
     * @param datasetLevelSequenceId the datasetLevelSequenceId for this composite key.
     * @param datasetLevelId the datasetLevelId for this composite key.
     */
    public DatasetLevelSequenceMapId(Integer datasetLevelSequenceId, Integer datasetLevelId) {
        this.datasetLevelSequenceId = datasetLevelSequenceId;
        this.datasetLevelId = datasetLevelId;
    }

    /**
     * Get the datasetLevelSequenceId.
     * @return the datasetLevelSequenceId
     */
    public Integer getDatasetLevelSequenceId() {
        return datasetLevelSequenceId;
    }

    /**
     * Set the datasetLevelSequenceId.
     * @param datasetLevelSequenceId the datasetLevelSequenceId to set
     */
    public void setDatasetLevelSequenceId(Integer datasetLevelSequenceId) {
        this.datasetLevelSequenceId = datasetLevelSequenceId;
    }

    /**
     * Get the datasetLevelId.
     * @return the datasetLevelId.
     */
    public Integer getDatasetLevelId() {
        return datasetLevelId;
    }

    /**
     * Set the datasetLevelId.
     * @param datasetLevelId the datasetLevelId to set.
     */
    public void setDatasetLevelId(Integer datasetLevelId) {
        this.datasetLevelId = datasetLevelId;
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
         buffer.append("datasetLevelSequenceId").append("='")
             .append(datasetLevelSequenceId).append("' ");
         buffer.append("datasetLevelId").append("='").append(datasetLevelId).append("' ");
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
            DatasetLevelSequenceMapId otherItem = (DatasetLevelSequenceMapId)obj;

            if (!this.datasetLevelSequenceId.equals(otherItem.getDatasetLevelSequenceId())) {
                return false;
            }
            if (!this.datasetLevelId.equals(otherItem.getDatasetLevelId())) {
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
        hash = hash * UtilConstants.HASH_PRIME
            + (datasetLevelSequenceId != null ? datasetLevelSequenceId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (datasetLevelId != null ? datasetLevelId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset level sequence id</li>
     * <li>dataset level id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetLevelSequenceMapId otherItem = (DatasetLevelSequenceMapId)obj;
        if (this.datasetLevelSequenceId.compareTo(otherItem.datasetLevelSequenceId) != 0) {
            return this.datasetLevelSequenceId.compareTo(otherItem.datasetLevelSequenceId);
        }
        if (this.datasetLevelId.compareTo(otherItem.datasetLevelId) != 0) {
            return this.datasetLevelId.compareTo(otherItem.datasetLevelId);
        }
        return 0;
    }

} // end DatasetLevelSequenceMapId.java
