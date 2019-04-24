/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the
 * researcher_type_research_goal_map relation.
 * It is necessary to use this class in order to get the
 * Hibernate interaction to work correctly.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearcherTypeResearchGoalMapId implements Serializable, Comparable {

    /** The researcher type id (FK). */
    private Integer researcherTypeId;
    /** The research goal id (FK). */
    private Integer researchGoalId;

    /**
     * Constructor.
     */
    public ResearcherTypeResearchGoalMapId() { };

    /**
     * Full constructor.
     * @param typeItem the tool item for this composite key.
     * @param goalItem the file item for this composite key.
     */
    public ResearcherTypeResearchGoalMapId(ResearcherTypeItem typeItem, ResearchGoalItem goalItem) {
        if (typeItem != null) {
            this.researcherTypeId = (Integer)typeItem.getId();
        }
        if (goalItem != null) {
            this.researchGoalId = (Integer)goalItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param toolId the tool ID for this composite key.
     * @param goalId the file ID for this composite key.
     */
    public ResearcherTypeResearchGoalMapId(Integer toolId, Integer goalId) {
        this.researcherTypeId = toolId;
        this.researchGoalId = goalId;
    }

    /**
     * Get the researcherTypeId.
     * @return the researcherTypeId
     */
    public Integer getResearcherTypeId() {
        return researcherTypeId;
    }

    /**
     * Set the researcherTypeId.
     * @param researcherTypeId the researcherTypeId to set
     */
    public void setResearcherTypeId(Integer researcherTypeId) {
        this.researcherTypeId = researcherTypeId;
    }

    /**
     * Get the goalId.
     * @return the goalId.
     */
    public Integer getResearchGoalId() {
        return researchGoalId;
    }

    /**
     * Set the goalId.
     * @param goalId the goalId to set.
     */
    public void setResearchGoalId(Integer goalId) {
        this.researchGoalId = goalId;
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
         buffer.append("researcherTypeId").append("='").
                 append(researcherTypeId).append("' ");
         buffer.append("researchGoalId").append("='").
                 append(researchGoalId).append("' ");
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            ResearcherTypeResearchGoalMapId otherItem = (ResearcherTypeResearchGoalMapId)obj;
            if (!this.researcherTypeId.equals(otherItem.getResearcherTypeId())) {
                return false;
            }
            if (!this.researchGoalId.equals(otherItem.getResearchGoalId())) {
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
        int hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME
            + (researcherTypeId != null ? researcherTypeId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (researchGoalId != null ? researchGoalId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>researcher type id</li>
     * <li>research goal id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResearcherTypeResearchGoalMapId otherItem = (ResearcherTypeResearchGoalMapId)obj;
        if (this.researcherTypeId.compareTo(otherItem.researcherTypeId) != 0) {
            return this.researcherTypeId.compareTo(otherItem.researcherTypeId);
        }
        if (this.researchGoalId.compareTo(otherItem.researchGoalId) != 0) {
            return this.researchGoalId.compareTo(otherItem.researchGoalId);
        }
        return 0;
    }

}
