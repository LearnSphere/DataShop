/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a FeatureExtraction instance.
 *
 * @author 
 * @version $Revision: 14073 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-05-15 14:48:12 -0400 (Mon, 15 May 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FeatureExtractionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this MOOCdb. */
    private Long id;
    /** user id of the creator*/
    private String createdBy;
    /** start timestamp for this feature extraction process*/
    private Date startTimestamp;
    /** end timestamp for this feature extraction process*/
    private Date endTimestamp;
    /** start date for this feature extraction configuration*/
    private Date startDate;
    /** num_of_week for this feature extraction configuration*/
    private Integer numOfWeek;
    /** features_list for this feature extraction configuration, comma separated without space*/
    private String featuresList;
    
    /** Default constructor. */
    public FeatureExtractionItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id for this feature extraction.
     */
    public FeatureExtractionItem(Long id) {
        this.id = id;
    }

    /**
     * Get the id.
     * @return id as a Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param Database generated unique Id for this feature extraction.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns createdBy. @return Returns createdBy. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Set createdBy. @param createdBy The createdBy to set. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    
    /** Returns startTimestamp. @return Returns the startTimestamp. */
    public Date getStartTimestamp() {
        return startTimestamp;
    }

    /** Set startTimestamp. @param startTimestamp The startTimestamp to set. */
    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }
    
    /** Returns endTimestamp. @return Returns the endTimestamp. */
    public Date getEndTimestamp() {
        return endTimestamp;
    }

    /** Set endTimestamp. @param endTimestamp The endTimestamp to set. */
    public void setEndTimestamp(Date endTimestamp) {
        this.endTimestamp = endTimestamp;
    }
    
    /** Returns startDate. @return Returns the startDate. */
    public Date getStartDate() {
        return startDate;
    }

    /** Set startDate. @param startDate The startDate to set. */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    /** Returns numOfWeek. @return Returns the numOfWeek. */
    public Integer getNumOfWeek() {
        return numOfWeek;
    }
    
    /** Set numOfWeek. @param numOfWeek The numOfWeek to set. */
    public void setNumOfWeek(Integer numOfWeek) {
        this.numOfWeek = numOfWeek;
    }
    
    /** Returns featuresList. @return Returns the featuresList. */
    public String getFeaturesList() {
        return featuresList;
    }

    /** Set featuresList. @param featuresList The featuresList to set. */
    public void setFeaturesList(String featuresList) {
        this.featuresList = featuresList;
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
        buffer.append(objectToString("createdBy", getCreatedBy()));
        buffer.append(objectToString("startTimestamp", getStartTimestamp()));
        buffer.append(objectToString("endTimestamp", getEndTimestamp()));
        buffer.append(objectToString("startDate", getStartDate()));
        buffer.append(objectToString("numOfWeek", getNumOfWeek()));
        buffer.append(objectToString("featureList", getFeaturesList()));
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
        if (obj instanceof FeatureExtractionItem) {
            FeatureExtractionItem otherItem = (FeatureExtractionItem)obj;
            if (!objectEquals(this.getCreatedBy(), otherItem.getCreatedBy())) {
                return false;
            }
            if (!objectEquals(this.getStartTimestamp(), otherItem.getStartTimestamp())) {
                    return false;
            }
            if (!objectEquals(this.getEndTimestamp(), otherItem.getEndTimestamp())) {
                   return false;
            }
            if (!objectEquals(this.getStartDate(), otherItem.getStartDate())) {
                    return false;
            }
            if (!objectEquals(this.getNumOfWeek(), otherItem.getNumOfWeek())) {
                    return false;
            }
            if (!objectEquals(this.getFeaturesList(), otherItem.getFeaturesList())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreatedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumOfWeek());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFeaturesList());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        FeatureExtractionItem otherItem = (FeatureExtractionItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreatedBy(), otherItem.getCreatedBy());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getStartTimestamp(), otherItem.getStartTimestamp());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTimestamp(), otherItem.getEndTimestamp());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getStartDate(), otherItem.getStartDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumOfWeek(), otherItem.getNumOfWeek());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getFeaturesList(), otherItem.getFeaturesList());
        if (value != 0) { return value; }

        return value;
    }
}
