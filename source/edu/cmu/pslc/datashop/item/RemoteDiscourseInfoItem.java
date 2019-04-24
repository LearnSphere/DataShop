/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Meta-data for a remote DiscourseItem.
 * Very similar to a DTO in that it represents data from remote DB.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RemoteDiscourseInfoItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this item. */
    private Long id;
    /** DiscourseItem that this meta-data represents. */
    private DiscourseItem discourse;
    /** String representing date range. */
    private String dateRange;
    /** Number of users. */
    private Long numUsers;
    /** Number of discourse parts. */
    private Long numDiscourseParts;
    /** Number of contributions. */
    private Long numContributions;
    /** Number of data sources. */
    private Long numDataSources;
    /** Number of relations. */
    private Long numRelations;

    /** Default constructor. */
    public RemoteDiscourseInfoItem() {}

    /**
     * Get id.
     * @return the Integer id as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for this item
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the DiscourseItem referenced by this item.
     * @return DiscourseItem the discourse
     */
    public DiscourseItem getDiscourse() { return discourse; }

    /**
     * Set the DiscourseItem referenced by this item.
     * @param discourse the DiscourseItem
     */
    public void setDiscourse(DiscourseItem discourse) { this.discourse = discourse; }

    /**
     * Get the date range.
     * @return String dateRange
     */
    public String getDateRange() { return dateRange; }

    /**
     * Set the date range.
     * @param dateRange
     */
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    /**
     * Get the number of users.
     * @return Long numUsers
     */
    public Long getNumUsers() { return numUsers; }

    /**
     * Set the number of users.
     * @param numUsers
     */
    public void setNumUsers(Long numUsers) { this.numUsers = numUsers; }

    /**
     * Get the number of discourseParts.
     * @return Long numDiscourseParts
     */
    public Long getNumDiscourseParts() { return numDiscourseParts; }

    /**
     * Set the number of discourseParts.
     * @param numDiscourseParts
     */
    public void setNumDiscourseParts(Long numDiscourseParts) {
        this.numDiscourseParts = numDiscourseParts;
    }

    /**
     * Get the number of contributions.
     * @return Long numContributions
     */
    public Long getNumContributions() { return numContributions; }

    /**
     * Set the number of contributions.
     * @param numContributions
     */
    public void setNumContributions(Long numContributions) {
        this.numContributions = numContributions;
    }

    /**
     * Get the number of dataSources.
     * @return Long numDataSources
     */
    public Long getNumDataSources() { return numDataSources; }

    /**
     * Set the number of dataSources.
     * @param numDataSources
     */
    public void setNumDataSources(Long numDataSources) { this.numDataSources = numDataSources; }

    /**
     * Get the number of relations.
     * @return Long numRelations
     */
    public Long getNumRelations() { return numRelations; }

    /**
     * Set the number of relations.
     * @param numRelations
     */
    public void setNumRelations(Long numRelations) { this.numRelations = numRelations; }

    /**
     * Returns object name, hash code and the attributes.
     * @return String
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToStringFK("discourse", getDiscourse()));
         buffer.append(objectToString("dateRange", getDateRange()));
         buffer.append(objectToString("numUsers", getNumUsers()));
         buffer.append(objectToString("numDiscourseParts", getNumDiscourseParts()));
         buffer.append(objectToString("numContributions", getNumContributions()));
         buffer.append(objectToString("numDataSources", getNumDataSources()));
         buffer.append(objectToString("numRelations", getNumRelations()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
    * Equals function for this class.
    * @param obj Object of any type, should be an Item for equality check
    * @return boolean true if the items are equal, false if not
    */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RemoteDiscourseInfoItem) {
            RemoteDiscourseInfoItem otherItem = (RemoteDiscourseInfoItem)obj;

            if (!objectEqualsFK(this.getDiscourse(), otherItem.getDiscourse())) {
                return false;
            }
            if (!objectEquals(this.getDateRange(), otherItem.getDateRange())) {
                return false;
            }
            if (!objectEquals(this.getNumUsers(), otherItem.getNumUsers())) {
                return false;
            }
            if (!objectEquals(this.getNumDiscourseParts(), otherItem.getNumDiscourseParts())) {
                return false;
            }
            if (!objectEquals(this.getNumContributions(), otherItem.getNumContributions())) {
                return false;
            }
            if (!objectEquals(this.getNumDataSources(), otherItem.getNumDataSources())) {
                return false;
            }
            if (!objectEquals(this.getNumRelations(), otherItem.getNumRelations())) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
    * Returns the hash code for this item.
     * @return integer hash code
    */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDiscourse());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDateRange());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumUsers());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumDiscourseParts());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumContributions());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumDataSources());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumRelations());

        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        RemoteDiscourseInfoItem otherItem = (RemoteDiscourseInfoItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getDiscourse(), otherItem.getDiscourse());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDateRange(), otherItem.getDateRange());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumUsers(), otherItem.getNumUsers());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumDiscourseParts(), otherItem.getNumDiscourseParts());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumContributions(), otherItem.getNumContributions());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumDataSources(), otherItem.getNumDataSources());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumRelations(), otherItem.getNumRelations());
        if (value != 0) { return value; }

        return value;
   }
}