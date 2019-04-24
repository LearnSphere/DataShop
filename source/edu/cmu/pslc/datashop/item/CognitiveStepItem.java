/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
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
 * This represents a single row in the cognitive step table.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4383 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2007-10-19 12:25:49 -0400 (Fri, 19 Oct 2007) $
 * <!-- $KeyWordsOff: $ -->
 */

public class CognitiveStepItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this cognitive step */
    private Long cognitiveStepId;
    /** Problem associated with this cognitive step */
    private ProblemItem problem;
    /** Information about this step as a string. */
    private String stepInfo;
    /** Collection of skills associated with this step. */
    private Set skills;
    /** Collection of cog step sequences associated with this step. */
    private Set cogStepSeqs;

    /** Default constructor. */
    public CognitiveStepItem() {
    }

    /**
     *  Constructor with id.
     *  @param cognitiveStepId Database generated unique Id for this cognitive step
     */
    public CognitiveStepItem(Long cognitiveStepId) {
        this.cognitiveStepId = cognitiveStepId;
    }

    /**
     * Get cognitiveStepId.
     * @return Long
     */
    public Comparable getId() {
        return this.cognitiveStepId;
    }

    /**
     * Set cognitiveStepId.
     * @param cognitiveStepId Database generated unique Id for this cognitive step
     */
    public void setId(Long cognitiveStepId) {
        this.cognitiveStepId = cognitiveStepId;
    }

    /**
     * Get problem.
     * @return edu.cmu.pslc.datashop.item.ProblemItem
     */
    public ProblemItem getProblem() {
        return this.problem;
    }

    /**
     * Set problem.
     * @param problem ProblemItem associated with this cognitive step
     */
    public void setProblem(ProblemItem problem) {
        this.problem = problem;
    }

    /**
     * Get stepInfo.
     * @return java.lang.String
     */
    public String getStepInfo() {
        return this.stepInfo;
    }

    /**
     * Set stepInfo.
     * @param stepInfo Information about this step as a string.
     */
    public void setStepInfo(String stepInfo) {
        this.stepInfo = stepInfo;
    }

    /**
     * Get skills.
     * @return java.util.Set
     */
    protected Set getSkills() {
        if (this.skills == null) {
            this.skills = new HashSet();
        }
        return this.skills;
    }

    /**
     * Public method to get skills.
     * @return a list instead of a set
     */
    public List getSkillsExternal() {
        List sortedList = new ArrayList(getSkills());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a skill.
     * @param item skill to add
     */
    public void addSkill(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
        }

    }

    /**
     * Remove a skill.
     * @param item skill to add
     */
    public void removeSkill(SkillItem item) {
        if (getSkills().contains(item)) {
            getSkills().remove(item);
        }
    }

    /**
     * Set skills.
     * @param skills Collection of skills associated with this step.
     */
    protected void setSkills(Set skills) {
        this.skills = skills;
    }

    /**
     * Get cogStepSeqs.
     * @return java.util.Set
     */
    protected Set getCogStepSeqs() {
        if (this.cogStepSeqs == null) {
            this.cogStepSeqs = new HashSet();
        }
        return this.cogStepSeqs;
    }

    /**
     * Public method to get cogStepSeqs.
     * @return a list instead of a set
     */
    public List getCogStepSeqsExternal() {
        List sortedList = new ArrayList(getCogStepSeqs());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a cogStepSeqMap.
     * @param item cogStepSeqItem to add
     */
    public void addCogStepSeqMap(CogStepSeqItem item) {
        getCogStepSeqs().add(item);
        item.setCognitiveStep(this);
    }

    /**
     * Set cogStepSeqs.
     * @param items Collection of cog step sequences for this item
     */
    public void setCogStepSeqs(Set items) {
        this.cogStepSeqs = items;
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
         buffer.append(objectToStringFK("Problem", getProblem()));
         buffer.append(objectToString("StepInfo", getStepInfo()));
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
        if (obj instanceof CognitiveStepItem) {
            CognitiveStepItem otherItem = (CognitiveStepItem)obj;

            if (!objectEqualsFK(this.getProblem(), otherItem.getProblem())) {
                return false;
            }
            if (!objectEquals(this.getStepInfo(), otherItem.getStepInfo())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProblem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStepInfo());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>problem</li>
     * <li>step info</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CognitiveStepItem otherItem = (CognitiveStepItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getProblem(), otherItem.getProblem());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStepInfo(), otherItem.getStepInfo());
        if (value != 0) { return value; }

        return value;
    }
}