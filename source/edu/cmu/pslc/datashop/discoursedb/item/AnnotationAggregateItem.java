/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
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
 * Represents a single row in the discoursedb.annotation_aggregate table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AnnotationAggregateItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long annotationAggregateId;
    /** The time this AnnotationAggregate was created. */
    private Date created;
    /** The time this AnnotationAggregate was modified. */
    private Date modified;
    /** The version for this AnnotationAggregate. */
    private Long version;
    /** The Discourse that 'owns' this AnnotationAggregate. */
    private DiscourseItem discourse;
    /** Database ID for source item. */
    private Long srcId;

    /** Default constructor. */
    public AnnotationAggregateItem() {}

    /**
     *  Constructor with id.
     *  @param annotationAggregateId the database generated unique ID for this item.
     */
    public AnnotationAggregateItem(Long annotationAggregateId) {
        this.annotationAggregateId = annotationAggregateId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.annotationAggregateId; }

    /**
     * Set the id.
     * @param annotationAggregateId Database generated unique Id for this item.
     */
    public void setId(Long annotationAggregateId) {
        this.annotationAggregateId = annotationAggregateId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this AnnotationAggregate was created
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
     * @param modified The time this AnnotationAggregate was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get the version.
     * @return Long the AnnotationAggregate version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this AnnotationAggregate
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
     * @param discourse Discourse associated with this AnnotationAggregate
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
        if (obj instanceof AnnotationAggregateItem) {
            AnnotationAggregateItem otherItem = (AnnotationAggregateItem)obj;

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
        AnnotationAggregateItem otherItem = (AnnotationAggregateItem)obj;
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