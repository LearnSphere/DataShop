/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the discoursedb.data_sources table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DataSourcesItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long dataSourcesId;
    /** The time this DataSource was created. */
    private Date created;
    /** The time this DataSource was modified. */
    private Date modified;
    /** The version for this DataSource. */
    private Long version;
    /** The Discourse that 'owns' this DataSource. */
    private DiscourseItem discourse;
    /** Database ID for source item. */
    private Long srcId;
    /** The collection of DataSourceInstances associated with this DataSources. */
    private Set<DataSourceInstanceItem> dataSourceInstances;

    /** Default constructor. */
    public DataSourcesItem() {}

    /**
     *  Constructor with id.
     *  @param dataSourcesId the database generated unique ID for this item.
     */
    public DataSourcesItem(Long dataSourcesId) {
        this.dataSourcesId = dataSourcesId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.dataSourcesId; }

    /**
     * Set the id.
     * @param dataSourcesId Database generated unique Id for this item.
     */
    public void setId(Long dataSourcesId) {
        this.dataSourcesId = dataSourcesId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this DataSource was created
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
     * @param modified The time this DataSource was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get the version.
     * @return Long the DataSource version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this DataSource
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Get Discourse.
     * @return DiscourseItem
     */
    public DiscourseItem getDiscourse() {
        return this.discourse;
    }

    /**
     * Set Discourse.
     * @param discourse Discourse associated with this DataSources
     */
    public void setDiscourse(DiscourseItem discourse) {
        this.discourse = discourse;
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
     * Get dataSourceInstances.
     * @return the set of dataSourceInstances associated with this DataSources
     */
    protected Set<DataSourceInstanceItem> getDataSourceInstances() {
        if (this.dataSourceInstances == null) {
            this.dataSourceInstances = new HashSet<DataSourceInstanceItem>();
        }
        return this.dataSourceInstances;
    }

    /**
     * Public method to get dataSourceInstances
     * @return a list instead of a set
     */
    public List<DataSourceInstanceItem> getDataSourceInstancesExternal() {
        List<DataSourceInstanceItem> sortedList = new ArrayList(getDataSourceInstances());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataSourceInstance
     * @param item dataSourceInstance to add
     */
    public void addDataSourceInstance(DataSourceInstanceItem item) {
        getDataSourceInstances().add(item);
        item.setDataSources(this);
    }

    /**
     * Remove a dataSourceInstance.
     * @param item dataSourceInstance to remove
     */
    public void removeDataSourceInstance(DataSourceInstanceItem item) {
        getDataSourceInstances().remove(item);
        item.setDataSources(null);
    }

    /**
     * Set dataSourceInstances
     * @param items DataSourceInstances associated with this DataSources
     */
    protected void setDataSourceInstances(Set<DataSourceInstanceItem> items) {
        this.dataSourceInstances = items;
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
         buffer.append(objectToStringFK("Discourse", getDiscourse()));
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
        if (obj instanceof DataSourcesItem) {
            DataSourcesItem otherItem = (DataSourcesItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!objectEqualsFK(this.getDiscourse(), otherItem.getDiscourse())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDiscourse());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>version</li>
     * <li>discourse</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DataSourcesItem otherItem = (DataSourcesItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDiscourse(), otherItem.getDiscourse());
        if (value != 0) { return value; }

        return value;
    }
}