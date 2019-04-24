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
 * Represents a single row in the discoursedb.discourse_part table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscoursePartItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long discoursePartId;
    /** The time this discourse part was created. */
    private Date created;
    /** The time this discourse part was modified. */
    private Date modified;
    /** The type for this discourse part. */
    private String discoursePartType;
    /** The end time for this discourse part. */
    private Date endTime;
    /** The start time for this discourse part. */
    private Date startTime;
    /** The version for this discourse part. */
    private Long version;
    /** The name for this discourse part. */
    private String name;
    /** Database ID for source item. */
    private Long srcId;
    /** The DataSources for this discourse part. */
    private DataSourcesItem dataSources;
    
    /** Default constructor. */
    public DiscoursePartItem() {}

    /**
     *  Constructor with id.
     *  @param discoursePartId the database generated unique ID for this item.
     */
    public DiscoursePartItem(Long discoursePartId) {
        this.discoursePartId = discoursePartId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.discoursePartId; }

    /**
     * Set the id.
     * @param discoursePartId Database generated unique Id for this item.
     */
    public void setId(Long discoursePartId) {
        this.discoursePartId = discoursePartId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this discourse part was created
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
     * @param modified The time this discourse part was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get discoursePartType
     * @return String
     */
    public String getDiscoursePartType() {
        return this.discoursePartType;
    }

    /**
     * Set dataSources.
     * @param discoursePartType type associated with this DiscoursePart
     */
    public void setDiscoursePartType(String discoursePartType) {
        this.discoursePartType = discoursePartType;
    }

    /**
     * Get end time.
     * @return the end time
     */
    public Date getEndTime() { return this.endTime; }

    /**
     * Set end time.
     * @param endTime The end time for this discourse part
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Get start time.
     * @return the start time
     */
    public Date getStartTime() { return this.startTime; }

    /**
     * Set start time.
     * @param startTime The start time for this discourse part
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the version.
     * @return Long the discourse part version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this discourse part
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Get discourse part name.
     * @return the name
     */
    public String getName() { return this.name; }

    /**
     * Set discourse part name.
     * @param name The name of the discourse part
     */
    public void setName(String name) {
        this.name = name;
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
     * @param dataSources DataSources associated with this DiscoursePart
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
         buffer.append(objectToString("DiscoursePartType", getDiscoursePartType()));
         buffer.append(objectToString("EndTime", getEndTime()));
         buffer.append(objectToString("StartTime", getStartTime()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToString("Name", getName()));
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
        if (obj instanceof DiscoursePartItem) {
            DiscoursePartItem otherItem = (DiscoursePartItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getDiscoursePartType(), otherItem.getDiscoursePartType())) {
                return false;
            }
            if (!objectEquals(this.getEndTime(), otherItem.getEndTime())) {
                return false;
            }
            if (!objectEquals(this.getStartTime(), otherItem.getStartTime())) {
                return false;
            }
            if (!objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!objectEquals(this.getName(), otherItem.getName())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDiscoursePartType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataSources());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>discoursePartType</li>
     * <li>endTime</li>
     * <li>startTime</li>
     * <li>version</li>
     * <li>name</li>
     * <li>dataSources</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscoursePartItem otherItem = (DiscoursePartItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDiscoursePartType(), otherItem.getDiscoursePartType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTime(), otherItem.getEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataSources(), otherItem.getDataSources());
        if (value != 0) { return value; }

        return value;
    }
}