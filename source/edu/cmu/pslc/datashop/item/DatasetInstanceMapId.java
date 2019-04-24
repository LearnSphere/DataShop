/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the dataset_instance_map relation.
 *
 * @author Cindy Tipper
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInstanceMapId implements Serializable, Comparable {

    /** The dataset id (FK). */
    private Integer datasetId;
    /** The remote instance id (FK). */
    private Long remoteInstanceId;

    /**
     * Constructor.
     */
    public DatasetInstanceMapId() { };

    /**
     * Full constructor.
     * @param datasetItem the dataset item for this composite key.
     * @param instanceItem the remote instance item for this composite key.
     */
    public DatasetInstanceMapId(DatasetItem datasetItem, RemoteInstanceItem instanceItem) {
        if (datasetItem != null) {
            this.datasetId = (Integer)datasetItem.getId();
        }
        if (instanceItem != null) {
            this.remoteInstanceId = (Long)instanceItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param datasetId the dataset ID for this composite key.
     * @param instanceId the remote instance ID for this composite key.
     */
    public DatasetInstanceMapId(Integer datasetId, Long instanceId) {
        this.datasetId = datasetId;
        this.remoteInstanceId = instanceId;
    }

    /**
     * Get the datasetId.
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }

    /**
     * Set the datasetId.
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
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
         buffer.append("datasetId = ");
         buffer.append(datasetId);
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
            DatasetInstanceMapId otherItem = (DatasetInstanceMapId)obj;
            if (!this.datasetId.equals(otherItem.getDatasetId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (datasetId != null ? datasetId.hashCode() : 0);
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
        DatasetInstanceMapId otherItem = (DatasetInstanceMapId)obj;
        
        if (this.datasetId.compareTo(otherItem.datasetId) != 0) {
            return this.datasetId.compareTo(otherItem.datasetId);
        }
        if (this.remoteInstanceId.compareTo(otherItem.remoteInstanceId) != 0) {
            return this.remoteInstanceId.compareTo(otherItem.remoteInstanceId);
        }
        return 0;
    }

}
