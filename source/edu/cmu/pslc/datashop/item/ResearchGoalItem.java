/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A researcher type is something like, 'Cognitive Scientist', and will
 * be used for a help-like interface to get users started using DataShop.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalItem extends Item implements java.io.Serializable, Comparable  {

    /*---------- CLASS ATTRIBUTES ----------*/

    /** Database generated unique ID. */
    private Integer id;
    /** Class attribute. */
    private String title;
    /** Class attribute. */
    private String description;
    /** Class attribute. */
    private Integer goalOrder;
    /** Research Goals associated with this item. */
    private Set<ResearcherTypeResearchGoalMapItem> researcherTypeResearchGoalMaps;
    /** Research Papers associated with this item. */
    private Set<ResearchGoalDatasetPaperMapItem> researchGoalDatasetPaperMaps;

    /*---------- CONSTRUCTOR ----------*/

    /** Default constructor. */
    public ResearchGoalItem() { }

    /*---------- GETTERS and SETTERS ----------*/

    /**
     * Get the id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for this item.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets goalOrder.
     * @return the goalOrder
     */
    public Integer getGoalOrder() {
        return goalOrder;
    }

    /**
     * Sets the goalOrder.
     * @param goalOrder the goalOrder to set
     */
    public void setGoalOrder(Integer goalOrder) {
        this.goalOrder = goalOrder;
    }

    /*---------- researcherTypeResearchGoalMaps ----------*/

    /**
     * Get researcherTypeResearchGoalMaps.
     * @return the set of researcherTypeResearchGoalMaps
     */
    public Set getResearcherTypeResearchGoalMaps() {
        if (this.researcherTypeResearchGoalMaps == null) {
            this.researcherTypeResearchGoalMaps = new HashSet();
        }
        return this.researcherTypeResearchGoalMaps;
    }

    /**
     * Public method to get ResearcherTypeResearchGoalMaps.
     * @return a list instead of a set
     */
    public List getResearcherTypeResearchGoalMapsExternal() {
        List sortedItems = new ArrayList(getResearcherTypeResearchGoalMaps());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set researcherTypeResearchGoalMaps.
     * @param researcherTypeResearchGoalMaps
     *     Collection of researcherTypeResearchGoalMaps associated with this item.
     */
    protected void setResearcherTypeResearchGoalMaps(
            Set researcherTypeResearchGoalMaps) {
        this.researcherTypeResearchGoalMaps = researcherTypeResearchGoalMaps;
    }

    /**
     * Add a researcherTypeResearchGoalMap.
     * @param researcherTypeResearchGoalMap researcherTypeResearchGoalMap to add
     */
    public void addResearcherTypeResearchGoalMap(
            ResearcherTypeResearchGoalMapItem researcherTypeResearchGoalMap) {
        if (!getResearcherTypeResearchGoalMaps().contains(researcherTypeResearchGoalMap)) {
            getResearcherTypeResearchGoalMaps().add(researcherTypeResearchGoalMap);
        }
    }

    /**
     * Remove the ResearcherTypeResearchGoalMap Item.
     * @param item researcherTypeResearchGoalMap item.
     */
    public void removeResearcherTypeResearchGoalMap(ResearcherTypeResearchGoalMapItem item) {
        if (getResearcherTypeResearchGoalMaps().contains(item)) {
            getResearcherTypeResearchGoalMaps().remove(item);
        }
    }

    /*---------- ResearchGoalDatasetPaperMaps ----------*/

    /**
     * Get researchGoalDatasetPaperMaps.
     * @return the set of researchGoalDatasetPaperMaps
     */
    public Set getResearchGoalDatasetPaperMaps() {
        if (this.researchGoalDatasetPaperMaps == null) {
            this.researchGoalDatasetPaperMaps = new HashSet();
        }
        return this.researchGoalDatasetPaperMaps;
    }

    /**
     * Public method to get researchGoalDatasetPaperMaps.
     * @return a list instead of a set
     */
    public List getResearchGoalDatasetPaperMapsExternal() {
        List sortedItems = new ArrayList(getResearchGoalDatasetPaperMaps());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set researchGoalDatasetPaperMaps.
     * @param researchGoalDatasetPaperMaps
     *     Collection of ResearchGoalDatasetPaperMaps associated with this item.
     */
    protected void setResearchGoalDatasetPaperMaps(
            Set researchGoalDatasetPaperMaps) {
        this.researchGoalDatasetPaperMaps = researchGoalDatasetPaperMaps;
    }

    /**
     * Add a researchGoalDatasetPaperMap.
     * @param researchGoalDatasetPaperMap ResearchGoalDatasetPaperMap to add
     */
    public void addResearchGoalDatasetPaperMap(
            ResearchGoalDatasetPaperMapItem researchGoalDatasetPaperMap) {
        if (!getResearchGoalDatasetPaperMaps().contains(researchGoalDatasetPaperMap)) {
            getResearchGoalDatasetPaperMaps().add(researchGoalDatasetPaperMap);
        }
    }

    /**
     * Remove the ResearchGoalDatasetPaperMapItem.
     * @param item ResearchGoalDatasetPaperMapitem.
     */
    public void removeResearchGoalDatasetPaperMap(ResearchGoalDatasetPaperMapItem item) {
        if (getResearchGoalDatasetPaperMaps().contains(item)) {
            getResearchGoalDatasetPaperMaps().remove(item);
        }
    }

    /*---------- STANDARD ITEM METHODS :: toString, equals, hashCode, comareTo ----------*/

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
         buffer.append(objectToString("Title", getTitle()));
         buffer.append(objectToString("Description", getDescription()));
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
        if (obj instanceof ResearchGoalItem) {
            ResearchGoalItem otherItem = (ResearchGoalItem)obj;

            if (!objectEquals(this.getTitle(), otherItem.getTitle())) {
                return false;
            }

            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTitle());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>label</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResearchGoalItem otherItem = (ResearchGoalItem)obj;
        int value = 0;

        value = objectCompareTo(this.getTitle(), otherItem.getTitle());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        return value;
    }

}
