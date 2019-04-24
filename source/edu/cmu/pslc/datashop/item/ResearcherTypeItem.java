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
 * @version $Revision: 12463 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-07-02 13:28:54 -0400 (Thu, 02 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearcherTypeItem extends Item implements java.io.Serializable, Comparable  {

    /*---------- CLASS ATTRIBUTES ----------*/

    /** Database generated unique ID. */
    private Integer id;
    /** Class attribute. */
    private String label;
    /** Class attribute. */
    private Integer typeOrder;
    /**  Class attribute. */
    private Integer parentTypeId;
    /** Research Goals associated with this item. */
    private Set<ResearcherTypeResearchGoalMapItem> researcherTypeResearchGoalMaps;

    /*---------- CONSTRUCTOR ----------*/

    /** Default constructor. */
    public ResearcherTypeItem() { }

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
     * Gets label.
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets typeOrder.
     * @return the typeOrder
     */
    public Integer getTypeOrder() {
        return typeOrder;
    }

    /**
     * Sets the typeOrder.
     * @param typeOrder the typeOrder to set
     */
    public void setTypeOrder(Integer typeOrder) {
        this.typeOrder = typeOrder;
    }

    /**
     * Get the parentTypeId.
     * @return the parentTypeId
     */
    public Integer getParentTypeId() {
        return parentTypeId;
    }

    /**
     * Set the parentTypeId.
     * @param parentTypeId the parent id to set
     */
    public void setParentTypeId(Integer parentTypeId) {
        this.parentTypeId = parentTypeId;
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
     *         Collection of researcherTypeResearchGoalMaps associated with this item.
     */
    protected void setResearcherTypeResearchGoalMaps(Set researcherTypeResearchGoalMaps) {
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
         buffer.append(objectToString("Label", getLabel()));
         buffer.append(objectToString("Order", getTypeOrder()));
         buffer.append(objectToString("Parent", getParentTypeId()));
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
        if (obj instanceof ResearcherTypeItem) {
            ResearcherTypeItem otherItem = (ResearcherTypeItem)obj;

            if (!objectEquals(this.getLabel(), otherItem.getLabel())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLabel());
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
        ResearcherTypeItem otherItem = (ResearcherTypeItem)obj;
        int value = 0;

        value = objectCompareTo(this.getLabel(), otherItem.getLabel());
        if (value != 0) { return value; }

        return value;
    }

}
