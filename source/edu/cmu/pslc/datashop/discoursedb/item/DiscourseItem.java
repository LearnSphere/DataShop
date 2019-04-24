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

import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the discoursedb.discourse table.
 *
 * @author Cindy Tipper
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long discourseId;
    /** The name of this discourse. */
    private String name;
    /** The id for the project that contains this discourse. */
    private Integer projectId;
    /** The time this discourse was created. */
    private Date created;
    /** The time this discourse was modified. */
    private Date modified;
    /** The version for this discourse. */
    private Long version;
    /** Flag indicating if discourse is to be deleted. */
    private Boolean deletedFlag = false;
    /** Database ID for source item. */
    private Long srcId;

    /** Default constructor. */
    public DiscourseItem() {}

    /**
     *  Constructor with id.
     *  @param discourseId the database generated unique ID for this item.
     */
    public DiscourseItem(Long discourseId) {
        this.discourseId = discourseId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.discourseId; }

    /**
     * Set the id.
     * @param discourseId Database generated unique Id for this item.
     */
    public void setId(Long discourseId) {
        this.discourseId = discourseId;
    }

    /**
     * Get discourse name.
     * @return the discourse name
     */
    public String getName() { return this.name; }

    /**
     * Set discourse name.
     * @param name The name of the discourse.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get project id.
     * @return Integer projectId
     */
    public Integer getProjectId() {
        return this.projectId;
    }

    /**
     * Set project id.
     * @param projectId Id for Project associated with this discourse
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this discourse was created
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
     * @param modified The time this discourse was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get the version.
     * @return Long the discourse version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this discourse
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Gets deletedFlag.
     * @return the deletedFlag
     */
    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    /**
     * Sets the deletedFlag.
     * @param deletedFlag the deletedFlag to set
     */
    public void setDeletedFlag(Boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
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
         buffer.append(objectToString("Name", getName()));
         buffer.append(objectToString("projectId", getProjectId()));
         buffer.append(objectToString("Created", getCreated()));
         buffer.append(objectToString("Modified", getModified()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToString("deletedFlag", getDeletedFlag()));
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
        if (obj instanceof DiscourseItem) {
            DiscourseItem otherItem = (DiscourseItem)obj;

            if (!objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }
            if (!Item.objectEquals(this.getProjectId(), otherItem.getProjectId())) {
                return false;
            }
            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!objectEquals(this.getDeletedFlag(), otherItem.getDeletedFlag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProjectId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreated());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getModified());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDeletedFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>name</li>
     * <li>created</li>
     * <li>modified</li>
     * <li>version</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscourseItem otherItem = (DiscourseItem)obj;
        int value = 0;

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProjectId(), otherItem.getProjectId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDeletedFlag(), otherItem.getDeletedFlag());
        if (value != 0) { return value; }

        return value;
    }

    /** Constant for the maximum length of the zip file name. */
    private static final int MAX_LEN_ZIP_FILE_NAME = 50;

    /**
     * Get zip file name from the name.
     * @return the zip file name.
     */
    public String getZipFileName() {
        String zipFileName = FileUtils.cleanForFileSystem(this.name);
        if (zipFileName.length() > MAX_LEN_ZIP_FILE_NAME) {
            return zipFileName.substring(0, MAX_LEN_ZIP_FILE_NAME) + ".zip";
        }
        return zipFileName + ".zip";
    }
}
