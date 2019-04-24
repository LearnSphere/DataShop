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
 * Represents a single row in the discoursedb.discourse_relation table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseRelationItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long discourseRelationId;
    /** The time this discourseRelation was created. */
    private Date created;
    /** The time this discourseRelation was modified. */
    private Date modified;
    /** The type for this discourseRelation. */
    private String discourseRelationType;
    /** The end time for this discourseRelation. */
    private Date endTime;
    /** The start time for this discourseRelation. */
    private Date startTime;
    /** The version for this discourseRelation. */
    private Long version;
    /** Database ID for source item. */
    private Long srcId;
    /** The source for this discourseRelation. */
    private ContributionItem source;
    /** The target for this content. */
    private ContributionItem target;
    
    /** Default constructor. */
    public DiscourseRelationItem() {}

    /**
     *  Constructor with id.
     *  @param discourseRelationId the database generated unique ID for this item.
     */
    public DiscourseRelationItem(Long discourseRelationId) {
        this.discourseRelationId = discourseRelationId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.discourseRelationId; }

    /**
     * Set the id.
     * @param discourseRelationId Database generated unique Id for this item.
     */
    public void setId(Long discourseRelationId) {
        this.discourseRelationId = discourseRelationId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this discourseRelation was created
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
     * @param modified The time this discourseRelation was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get discourseRelationType
     * @return String
     */
    public String getDiscourseRelationType() {
        return this.discourseRelationType;
    }

    /**
     * Set dataSources.
     * @param discourseRelationType type associated with this DiscourseRelation
     */
    public void setDiscourseRelationType(String discourseRelationType) {
        this.discourseRelationType = discourseRelationType;
    }

    /**
     * Get end time.
     * @return the end time
     */
    public Date getEndTime() { return this.endTime; }

    /**
     * Set end time.
     * @param endTime The end time for this discourseRelation
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
     * @param startTime The start time for this discourseRelation
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the version.
     * @return Long the discourseRelation version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this discourseRelation
     */
    public void setVersion(Long version) {
        this.version = version;
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
     * Get source
     * @return ContributionItem
     */
    public ContributionItem getSource() {
        return this.source;
    }

    /**
     * Set source.
     * @param source Contribution associated with the source
     */
    public void setSource(ContributionItem source) {
        this.source = source;
    }

    /**
     * Get target
     * @return ContributionItem
     */
    public ContributionItem getTarget() {
        return this.target;
    }

    /**
     * Set target.
     * @param target Contribution associated with the target
     */
    public void setTarget(ContributionItem target) {
        this.target = target;
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
         buffer.append(objectToString("DiscourseRelationType", getDiscourseRelationType()));
         buffer.append(objectToString("EndTime", getEndTime()));
         buffer.append(objectToString("StartTime", getStartTime()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToStringFK("Source", getSource()));
         buffer.append(objectToStringFK("Target", getTarget()));
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
        if (obj instanceof DiscourseRelationItem) {
            DiscourseRelationItem otherItem = (DiscourseRelationItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getDiscourseRelationType(),
                              otherItem.getDiscourseRelationType())) {
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
            if (!objectEqualsFK(this.getSource(), otherItem.getSource())) {
                return false;
            }
            if (!objectEqualsFK(this.getTarget(), otherItem.getTarget())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDiscourseRelationType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSource());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getTarget());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>discourseRelationType</li>
     * <li>endTime</li>
     * <li>startTime</li>
     * <li>version</li>
     * <li>source</li>
     * <li>target</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscourseRelationItem otherItem = (DiscourseRelationItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDiscourseRelationType(),
                                otherItem.getDiscourseRelationType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTime(), otherItem.getEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSource(), otherItem.getSource());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getTarget(), otherItem.getTarget());
        if (value != 0) { return value; }

        return value;
    }
}