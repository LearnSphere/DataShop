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
 * Represents a single row in the discoursedb.content table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContentItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long contentId;
    /** The time this content was created. */
    private Date created;
    /** The time this content was modified. */
    private Date modified;
    /** The end time for this content. */
    private Date endTime;
    /** The start time for this content. */
    private Date startTime;
    /** The version for this content. */
    private Long version;
    /** The data for this content. */
    private String data;
    /** The text for this content. */
    private String text;
    /** The title for this content. */
    private String title;
    /** Database ID for source item. */
    private Long srcId;
    /** The DataSources for this content. */
    private DataSourcesItem dataSources;
    /** The User for this content. */
    private DUserItem user;
    /** The nextRevision for this content. */
    private ContentItem nextRevision;
    /** The previousRevision for this content. */
    private ContentItem previousRevision;
    
    /** Default constructor. */
    public ContentItem() {}

    /**
     *  Constructor with id.
     *  @param contentId the database generated unique ID for this item.
     */
    public ContentItem(Long contentId) {
        this.contentId = contentId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.contentId; }

    /**
     * Set the id.
     * @param contentId Database generated unique Id for this item.
     */
    public void setId(Long contentId) {
        this.contentId = contentId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this content was created
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
     * @param modified The time this content was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get end time.
     * @return the end time
     */
    public Date getEndTime() { return this.endTime; }

    /**
     * Set end time.
     * @param endTime The end time for this content
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
     * @param startTime The start time for this content
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the version.
     * @return Long the content version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this content
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Get content data.
     * @return the data
     */
    public String getData() { return this.data; }

    /**
     * Set content data.
     * @param data The data of the content
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get content text.
     * @return the text
     */
    public String getText() { return this.text; }

    /**
     * Set content text.
     * @param text The text of the content
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get content title.
     * @return the title
     */
    public String getTitle() { return this.title; }

    /**
     * Set content title.
     * @param title The title of the content
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @param dataSources DataSources associated with this Content
     */
    public void setDataSources(DataSourcesItem dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * Get user
     * @return DUserItem
     */
    public DUserItem getUser() {
        return this.user;
    }

    /**
     * Set user.
     * @param user User associated with this Content
     */
    public void setUser(DUserItem user) {
        this.user = user;
    }

    /**
     * Get nextRevision
     * @return ContentItem
     */
    public ContentItem getNextRevision() {
        return this.nextRevision;
    }

    /**
     * Set nextRevision.
     * @param nextRevision Content associated with the next revision
     */
    public void setNextRevision(ContentItem nextRevision) {
        this.nextRevision = nextRevision;
    }

    /**
     * Get previousRevision
     * @return ContentItem
     */
    public ContentItem getPreviousRevision() {
        return this.previousRevision;
    }

    /**
     * Set previousRevision.
     * @param previousRevision Content associated with the previous revision
     */
    public void setPreviousRevision(ContentItem previousRevision) {
        this.previousRevision = previousRevision;
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
         buffer.append(objectToString("EndTime", getEndTime()));
         buffer.append(objectToString("StartTime", getStartTime()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToString("Data", getData()));
         buffer.append(objectToString("Text", getText()));
         buffer.append(objectToString("Title", getTitle()));
         buffer.append(objectToStringFK("DataSources", getDataSources()));
         buffer.append(objectToStringFK("User", getUser()));
         buffer.append(objectToStringFK("NextRevision", getNextRevision()));
         buffer.append(objectToStringFK("PreviousRevision", getPreviousRevision()));
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
        if (obj instanceof ContentItem) {
            ContentItem otherItem = (ContentItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
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
            if (!objectEquals(this.getData(), otherItem.getData())) {
                return false;
            }
            if (!objectEquals(this.getText(), otherItem.getText())) {
                return false;
            }
            if (!objectEquals(this.getTitle(), otherItem.getTitle())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataSources(), otherItem.getDataSources())) {
                return false;
            }
            if (!objectEqualsFK(this.getUser(), otherItem.getUser())) {
                return false;
            }
            if (!objectEqualsFK(this.getNextRevision(), otherItem.getNextRevision())) {
                return false;
            }
            if (!objectEqualsFK(this.getPreviousRevision(), otherItem.getPreviousRevision())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getData());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getText());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTitle());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataSources());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUser());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getNextRevision());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getPreviousRevision());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>endTime</li>
     * <li>startTime</li>
     * <li>version</li>
     * <li>data</li>
     * <li>text</li>
     * <li>title</li>
     * <li>dataSources</li>
     * <li>user</li>
     * <li>nextRevision</li>
     * <li>previousRevision</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ContentItem otherItem = (ContentItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTime(), otherItem.getEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getData(), otherItem.getData());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getText(), otherItem.getText());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTitle(), otherItem.getTitle());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataSources(), otherItem.getDataSources());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getNextRevision(), otherItem.getNextRevision());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getPreviousRevision(), otherItem.getPreviousRevision());
        if (value != 0) { return value; }

        return value;
    }
}