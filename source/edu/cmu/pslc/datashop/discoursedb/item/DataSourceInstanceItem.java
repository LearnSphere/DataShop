/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the discoursedb.data_source_instance table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DataSourceInstanceItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long dataSourceInstanceId;
    /** The time this dataSourceInstance was created. */
    private Date created;
    /** The time this dataSourceInstance was modified. */
    private Date modified;
    /** The version for this dataSourceInstance. */
    private Long version;
    /** The dataset name of this dataSourceInstance. */
    private String datasetName;
    /** The entitySourceDescriptor for this dataSourceInstance. */
    private String entitySourceDescriptor;
    /** The entitySourceId for this dataSourceInstance. */
    private String entitySourceId;
    /** The sourceType for this dataSourceInstance. */
    private String sourceType;
    /** Database ID for source item. */
    private Long srcId;
    /** The DataSources which 'owns' this dataSourceInstance. */
    private DataSourcesItem dataSources;

    /** Default constructor. */
    public DataSourceInstanceItem() {}

    /**
     *  Constructor with id.
     *  @param dataSourceInstanceId the database generated unique ID for this item.
     */
    public DataSourceInstanceItem(Long dataSourceInstanceId) {
        this.dataSourceInstanceId = dataSourceInstanceId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.dataSourceInstanceId; }

    /**
     * Set the id.
     * @param dataSourceInstanceId Database generated unique Id for this item.
     */
    public void setId(Long dataSourceInstanceId) {
        this.dataSourceInstanceId = dataSourceInstanceId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this dataSourceInstance was created
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Get time modified.
     * @return the time modified
     */
    public Date getModified() { return this.modified; }

    /**
     * Set modified time.
     * @param modified The time this dataSourceInstance was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get the version.
     * @return Long the dataSourceInstance version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this dataSourceInstance
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Get dataSourceInstance datasetName.
     * @return the dataSourceInstance datasetName
     */
    public String getDatasetName() { return this.datasetName; }

    /**
     * Set dataSourceInstance datasetName.
     * @param datasetName The datasetName of the dataSourceInstance.
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Get dataSourceInstance entitySourceDescriptor.
     * @return the dataSourceInstance entitySourceDescriptor
     */
    public String getEntitySourceDescriptor() { return this.entitySourceDescriptor; }

    /**
     * Set dataSourceInstance entitySourceDescriptor.
     * @param descriptor The entitySourceDescriptor of the dataSourceInstance.
     */
    public void setEntitySourceDescriptor(String descriptor) {
        this.entitySourceDescriptor = descriptor;
    }

    /**
     * Get dataSourceInstance entitySourceId.
     * @return the dataSourceInstance entitySourceId
     */
    public String getEntitySourceId() { return this.entitySourceId; }

    /**
     * Set dataSourceInstance entitySourceId.
     * @param id The entitySourceId of the dataSourceInstance.
     */
    public void setEntitySourceId(String id) {
        this.entitySourceId = id;
    }

    /**
     * Get dataSourceInstance sourceType
     * @return the dataSourceInstance sourceType
     */
    public String getSourceType() { return this.sourceType; }

    /**
     * Set dataSourceInstance sourceType
     * @param sourceType The sourceType of the dataSourceInstance.
     */
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * Get the id of the source object.
     * @return Long the id
     */
    public Long getSourceId() { return srcId; }

    /**
     * Set the id of the source object.
     * @param srcId
     */
    public void setSourceId(Long srcId) { this.srcId = srcId; }

    /**
     * Get dataSources
     * @return DataSourcesItem
     */
    public DataSourcesItem getDataSources() {
        return this.dataSources;
    }

    /**
     * Set dataSources.
     * @param dataSources DataSources associated with this dataSourceInstance
     */
    public void setDataSources(DataSourcesItem dataSources) {
        this.dataSources = dataSources;
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
         buffer.append(objectToString("Id", getId()));
         buffer.append(objectToString("Created", getCreated()));
         buffer.append(objectToString("Modified", getModified()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToString("DatasetName", getDatasetName()));
         buffer.append(objectToString("EntitySourceDescriptor", getEntitySourceDescriptor()));
         buffer.append(objectToString("EntitySourceId", getEntitySourceId()));
         buffer.append(objectToString("SourceType", getSourceType()));
         buffer.append(objectToStringFK("DataSources", getDataSources()));
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
        if (obj instanceof DataSourceInstanceItem) {
            DataSourceInstanceItem otherItem = (DataSourceInstanceItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!objectEquals(this.getDatasetName(), otherItem.getDatasetName())) {
                return false;
            }
            if (!objectEquals(this.getEntitySourceDescriptor(),
                              otherItem.getEntitySourceDescriptor())) {
                return false;
            }
            if (!objectEquals(this.getEntitySourceId(), otherItem.getEntitySourceId())) {
                return false;
            }
            if (!objectEquals(this.getSourceType(), otherItem.getSourceType())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataSources(), otherItem.getDataSources())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreated());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getModified());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDatasetName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEntitySourceDescriptor());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEntitySourceId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSourceType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataSources());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>version</li>
     * <li>datasetName</li>
     * <li>entitySourceDescriptor</li>
     * <li>entitySourceId</li>
     * <li>sourceType</li>
     * <li>dataSources</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DataSourceInstanceItem otherItem = (DataSourceInstanceItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatasetName(), otherItem.getDatasetName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEntitySourceDescriptor(),
                                otherItem.getEntitySourceDescriptor());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEntitySourceId(), otherItem.getEntitySourceId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSourceType(), otherItem.getSourceType());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataSources(), otherItem.getDataSources());
        if (value != 0) { return value; }

        return value;
    }
}