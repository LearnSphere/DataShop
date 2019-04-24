/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between a dataset and a remote instance.
 *
 * @author Cindy Tipper
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInstanceMapItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private DatasetInstanceMapId id;

    /** The dataset item associated with this item. */
    private DatasetItem dataset;

    /** The remote instance item associated with this item. */
    private RemoteInstanceItem remoteInstance;

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
    public void setId(DatasetInstanceMapId id) {
        this.id = id;
    }

    /**
     * Get the dataset
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set the dataset.
     * @param dataset the dataset to set
     */
    protected void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Public set dataset method to update the composite key as well.
     * @param datasetItem Part of the composite key - FK
     */
    public void setDatasetExternal(DatasetItem datasetItem) {
        setDataset(datasetItem);
        this.id = new DatasetInstanceMapId(this.dataset, this.remoteInstance);
    }

    /**
     * Get the remoteInstance.
     * @return the remoteInstance
     */
    public RemoteInstanceItem getRemoteInstance() {
        return remoteInstance;
    }

    /**
     * Set the remoteInstance.
     * @param remoteInstance the remoteInstance to set
     */
    protected void setRemoteInstance(RemoteInstanceItem remoteInstance) {
        this.remoteInstance = remoteInstance;
    }

    /**
     * Public set remoteInstance method to update the composite key as well.
     * @param remoteInstance Part of the composite key - FK
     */
    public void setRemoteInstanceExternal(RemoteInstanceItem remoteInstance) {
        setRemoteInstance(remoteInstance);
        this.id = new DatasetInstanceMapId(this.dataset, this.remoteInstance);
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
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToStringFK("RemoteInstance", getRemoteInstance()));
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
        if (obj instanceof DatasetInstanceMapItem) {
            DatasetInstanceMapItem otherItem = (DatasetInstanceMapItem)obj;

            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEqualsFK(this.getRemoteInstance(), otherItem.getRemoteInstance())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getRemoteInstance());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset</li>
     * <li>remoteInstance</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetInstanceMapItem otherItem = (DatasetInstanceMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getRemoteInstance(), otherItem.getRemoteInstance());
        if (value != 0) { return value; }

        return value;
    }
}
