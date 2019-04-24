/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between an researcher type and a research goal.
 *
 * @author alida
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearcherTypeResearchGoalMapItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private ResearcherTypeResearchGoalMapId id;

    /** The researcher type item associated with this item. */
    private ResearcherTypeItem researcherType;

    /** The research goal item associated with this item. */
    private ResearchGoalItem researchGoal;

    /** The order to show this goal for this researcher type. */
    private Integer goalOrder;

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(ResearcherTypeResearchGoalMapId id) {
        this.id = id;
    }

    /**
     * Get the researcherType.
     * @return the researcherType
     */
    public ResearcherTypeItem getResearcherType() {
        return researcherType;
    }

    /**
     * Set the researcherType.
     * @param researcherType the researcherType to set
     */
    protected void setResearcherType(ResearcherTypeItem researcherType) {
        this.researcherType = researcherType;
    }

    /**
     * Public set type method to update the composite key as well.
     * @param typeItem Part of the composite key - FK
     */
    public void setResearcherTypeExternal(ResearcherTypeItem typeItem) {
        setResearcherType(typeItem);
        this.id = new ResearcherTypeResearchGoalMapId(this.researcherType, this.researchGoal);
    }

    /**
     * Get the researchGoal.
     * @return the researchGoal
     */
    public ResearchGoalItem getResearchGoal() {
        return researchGoal;
    }

    /**
     * Set the researchGoal.
     * @param researchGoal the researchGoal to set
     */
    protected void setResearchGoal(ResearchGoalItem researchGoal) {
        this.researchGoal = researchGoal;
    }

    /**
     * Public set researchGoal method to update the composite key as well.
     * @param researchGoal Part of the composite key - FK
     */
    public void setResearchGoalExternal(ResearchGoalItem researchGoal) {
        setResearchGoal(researchGoal);
        this.id = new ResearcherTypeResearchGoalMapId(this.researcherType, this.researchGoal);
    }

    /**
     * Get the goalOrder.
     * @return the goalOrder
     */
    public Integer getGoalOrder() {
        return goalOrder;
    }

    /**
     * Set the goalOrder.
     * @param goalOrder the goalOrder to set
     */
    public void setGoalOrder(Integer goalOrder) {
        this.goalOrder = goalOrder;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToStringFK("ResearcherType", getResearcherType()));
        buffer.append(objectToStringFK("ResearchGoal", getResearchGoal()));
        buffer.append(objectToString("GoalOrder", this.getGoalOrder()));
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
        if (obj instanceof ResearcherTypeResearchGoalMapItem) {
            ResearcherTypeResearchGoalMapItem otherItem = (ResearcherTypeResearchGoalMapItem)obj;

            if (!objectEqualsFK(this.getResearcherType(),
                    otherItem.getResearcherType())) {
                return false;
            }
            if (!objectEqualsFK(this.getResearchGoal(), otherItem.getResearchGoal())) {
                return false;
            }
            if (!objectEquals(this.getGoalOrder(), otherItem.getGoalOrder())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getResearcherType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getResearchGoal());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getGoalOrder());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>researcherType</li>
     * <li>researchGoal</li>
     * <li>goalOrder</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResearcherTypeResearchGoalMapItem otherItem = (ResearcherTypeResearchGoalMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getResearcherType(),
                otherItem.getResearcherType());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getResearchGoal(), otherItem.getResearchGoal());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getGoalOrder(), otherItem.getGoalOrder());
        if (value != 0) { return value; }

        return value;
    }
}
