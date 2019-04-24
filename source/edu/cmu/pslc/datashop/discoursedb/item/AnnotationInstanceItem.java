/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the discoursedb.annotation_instance table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AnnotationInstanceItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long annotationInstanceId;
    /** The time this annotationInstance was created. */
    private Date created;
    /** The time this annotationInstance was modified. */
    private Date modified;
    /** The version for this annotationInstance. */
    private Long version;
    /** The type of this annotationInstance. */
    private String type;
    /** The beginning offset of this annotationInstance. */
    private Integer beginOffset;
    /** The text covered by this annotationInstance. */
    private String coveredText;
    /** The ending offset of this annotationInstance. */
    private Integer endOffset;
    /** The DataSourceAggregate assocated with this annotationInstance. */
    private DataSourcesItem dataSourceAggregate;
    /** The AnnotationAggregate which 'owns' this annotationInstance. */
    private AnnotationAggregateItem annotationAggregate;
    /** Database ID for source item. */
    private Long srcId;

    /** Default constructor. */
    public AnnotationInstanceItem() {}

    /**
     *  Constructor with id.
     *  @param annotationInstanceId the database generated unique ID for this item.
     */
    public AnnotationInstanceItem(Long annotationInstanceId) {
        this.annotationInstanceId = annotationInstanceId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.annotationInstanceId; }

    /**
     * Set the id.
     * @param annotationInstanceId Database generated unique Id for this item.
     */
    public void setId(Long annotationInstanceId) {
        this.annotationInstanceId = annotationInstanceId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this annotationInstance was created
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
     * @param modified The time this annotationInstance was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get the version.
     * @return Long the annotationInstance version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this annotationInstance
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Get the type.
     * @return String the annotationInstance type
     */
    public String getType() { return this.type; }

    /**
     * Set the type.
     * @param type the type of this annotationInstance
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the beginning offset.
     * @return Integer the beginning offset of this annotationInstance
     */
    public Integer getBeginOffset() { return this.beginOffset; }

    /**
     * Set the beginning offset.
     * @param Integer the beginning offset of this annotationInstance
     */
    public void setBeginOffset(Integer beginOffset) {
        this.beginOffset = beginOffset;
    }

    /**
     * Get the coveredText.
     * @return String the annotationInstance coveredText
     */
    public String getCoveredText() { return this.coveredText; }

    /**
     * Set the coveredText.
     * @param coveredText the coveredText of this annotationInstance
     */
    public void setCoveredText(String coveredText) {
        this.coveredText = coveredText;
    }

    /**
     * Get the ending offset.
     * @return Integer the ending offset of this annotationInstance
     */
    public Integer getEndOffset() { return this.endOffset; }

    /**
     * Set the enging offset.
     * @param Integer the ending offset of this annotationInstance
     */
    public void setEndOffset(Integer endOffset) {
        this.endOffset = endOffset;
    }

    /**
     * Get dataSourceAggregate
     * @return DataSourcesItem
     */
    public DataSourcesItem getDataSourceAggregate() {
        return this.dataSourceAggregate;
    }

    /**
     * Set dataSourceAggregate.
     * @param dataSourceAggregate DataSourcesItem associated with this annotationInstance
     */
    public void setDataSourceAggregate(DataSourcesItem dataSourceAggregate) {
        this.dataSourceAggregate = dataSourceAggregate;
    }

    /**
     * Get annotationAggregate
     * @return AnnotationAggregateItem
     */
    public AnnotationAggregateItem getAnnotationAggregate() {
        return this.annotationAggregate;
    }

    /**
     * Set annotationAggregate.
     * @param annotationAggregate AnnotationAggregate associated with this annotationInstance
     */
    public void setAnnotationAggregate(AnnotationAggregateItem annotationAggregate) {
        this.annotationAggregate = annotationAggregate;
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
         buffer.append(objectToString("Type", getType()));
         buffer.append(objectToString("BeginOffset", getBeginOffset()));
         buffer.append(objectToString("CoveredText", getCoveredText()));
         buffer.append(objectToString("EndOffset", getEndOffset()));
         buffer.append(objectToStringFK("DataSourceAggregate", getDataSourceAggregate()));
         buffer.append(objectToStringFK("AnnotationAggregate", getAnnotationAggregate()));
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
        if (obj instanceof AnnotationInstanceItem) {
            AnnotationInstanceItem otherItem = (AnnotationInstanceItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!objectEquals(this.getType(), otherItem.getType())) {
                return false;
            }
            if (!objectEquals(this.getBeginOffset(), otherItem.getBeginOffset())) {
                return false;
            }
            if (!objectEquals(this.getCoveredText(), otherItem.getCoveredText())) {
                return false;
            }
            if (!objectEquals(this.getEndOffset(), otherItem.getEndOffset())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataSourceAggregate(),
                                otherItem.getDataSourceAggregate())) {
                return false;
            }
            if (!objectEqualsFK(this.getAnnotationAggregate(),
                                otherItem.getAnnotationAggregate())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getBeginOffset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCoveredText());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndOffset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataSourceAggregate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getAnnotationAggregate());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>version</li>
     * <li>type</li>
     * <li>beginOffset</li>
     * <li>coveredText</li>
     * <li>endOffset</li>
     * <li>dataSourceAggregate</li>
     * <li>annotationAggregate</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AnnotationInstanceItem otherItem = (AnnotationInstanceItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getBeginOffset(), otherItem.getBeginOffset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCoveredText(), otherItem.getCoveredText());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndOffset(), otherItem.getEndOffset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataSourceAggregate(),
                                  otherItem.getDataSourceAggregate());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getAnnotationAggregate(),
                                  otherItem.getAnnotationAggregate());
        if (value != 0) { return value; }

        return value;
    }
}