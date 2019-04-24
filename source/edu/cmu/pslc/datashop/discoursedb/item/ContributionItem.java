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
 * Represents a single row in the discoursedb.contribution table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContributionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long contributionId;
    /** The time this contribution was created. */
    private Date created;
    /** The time this contribution was modified. */
    private Date modified;
    /** The type for this contribution. */
    private String contributionType;
    /** The end time for this contribution. */
    private Date endTime;
    /** The start time for this contribution. */
    private Date startTime;
    /** The version for this contribution. */
    private Long version;
    /** The downVotes for this contribution. */
    private Integer downVotes;
    /** The upVotes for this contribution. */
    private Integer upVotes;
    /** Database ID for source item. */
    private Long srcId;
    /** The DataSources for this contribution. */
    private DataSourcesItem dataSources;
    /** The currentRevision for this contribution. */
    private ContentItem currentRevision;
    /** The firstRevision for this content. */
    private ContentItem firstRevision;
    
    /** Default constructor. */
    public ContributionItem() {}

    /**
     *  Constructor with id.
     *  @param contributionId the database generated unique ID for this item.
     */
    public ContributionItem(Long contributionId) {
        this.contributionId = contributionId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.contributionId; }

    /**
     * Set the id.
     * @param contributionId Database generated unique Id for this item.
     */
    public void setId(Long contributionId) {
        this.contributionId = contributionId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this contribution was created
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
     * @param modified The time this contribution was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get contributionType
     * @return String
     */
    public String getContributionType() {
        return this.contributionType;
    }

    /**
     * Set dataSources.
     * @param contributionType type associated with this Contribution
     */
    public void setContributionType(String contributionType) {
        this.contributionType = contributionType;
    }

    /**
     * Get end time.
     * @return the end time
     */
    public Date getEndTime() { return this.endTime; }

    /**
     * Set end time.
     * @param endTime The end time for this contribution
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
     * @param startTime The start time for this contribution
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the version.
     * @return Long the contribution version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this contribution
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Get contribution downVotes.
     * @return the downVotes
     */
    public Integer getDownVotes() { return downVotes; }

    /**
     * Set contribution downVotes.
     * @param downVotes The downVotes of the contribution
     */
    public void setDownVotes(Integer downVotes) {
        this.downVotes = downVotes;
    }

    /**
     * Get contribution upVotes.
     * @return the upVotes
     */
    public Integer getUpVotes() { return upVotes; }

    /**
     * Set contribution upVotes.
     * @param upVotes The upVotes of the contribution
     */
    public void setUpVotes(Integer upVotes) {
        this.upVotes = upVotes;
    }

    /**
     * Get dataSources
     * @return DataSourcesItem
     */
    public DataSourcesItem getDataSources() {
        return this.dataSources;
    }

    /**
     * Set dataSources.
     * @param dataSources DataSources associated with this Contribution
     */
    public void setDataSources(DataSourcesItem dataSources) {
        this.dataSources = dataSources;
    }

    /**
     * Get currentRevision
     * @return ContentItem
     */
    public ContentItem getCurrentRevision() {
        return this.currentRevision;
    }

    /**
     * Set currentRevision.
     * @param currentRevision Content associated with the current revision
     */
    public void setCurrentRevision(ContentItem currentRevision) {
        this.currentRevision = currentRevision;
    }

    /**
     * Get firstRevision
     * @return ContentItem
     */
    public ContentItem getFirstRevision() {
        return this.firstRevision;
    }

    /**
     * Set firstRevision.
     * @param firstRevision Content associated with the first revision
     */
    public void setFirstRevision(ContentItem firstRevision) {
        this.firstRevision = firstRevision;
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
         buffer.append(objectToString("Created", getCreated()));
         buffer.append(objectToString("Modified", getModified()));
         buffer.append(objectToString("ContributionType", getContributionType()));
         buffer.append(objectToString("EndTime", getEndTime()));
         buffer.append(objectToString("StartTime", getStartTime()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToString("DownVotes", getDownVotes()));
         buffer.append(objectToString("UpVotes", getUpVotes()));
         buffer.append(objectToStringFK("DataSources", getDataSources()));
         buffer.append(objectToStringFK("CurrentRevision", getCurrentRevision()));
         buffer.append(objectToStringFK("FirstRevision", getFirstRevision()));
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
        if (obj instanceof ContributionItem) {
            ContributionItem otherItem = (ContributionItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getContributionType(), otherItem.getContributionType())) {
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
            if (!objectEquals(this.getDownVotes(), otherItem.getDownVotes())) {
                return false;
            }
            if (!objectEquals(this.getUpVotes(), otherItem.getUpVotes())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataSources(), otherItem.getDataSources())) {
                return false;
            }
            if (!objectEqualsFK(this.getCurrentRevision(), otherItem.getCurrentRevision())) {
                return false;
            }
            if (!objectEqualsFK(this.getFirstRevision(), otherItem.getFirstRevision())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getContributionType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDownVotes());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUpVotes());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataSources());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getCurrentRevision());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getFirstRevision());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>contributionType</li>
     * <li>endTime</li>
     * <li>startTime</li>
     * <li>version</li>
     * <li>downVotes</li>
     * <li>upVotes</li>
     * <li>dataSources</li>
     * <li>currentRevision</li>
     * <li>firstRevision</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ContributionItem otherItem = (ContributionItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getContributionType(), otherItem.getContributionType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTime(), otherItem.getEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDownVotes(), otherItem.getDownVotes());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUpVotes(), otherItem.getUpVotes());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataSources(), otherItem.getDataSources());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getCurrentRevision(), otherItem.getCurrentRevision());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getFirstRevision(), otherItem.getFirstRevision());
        if (value != 0) { return value; }

        return value;
    }
}