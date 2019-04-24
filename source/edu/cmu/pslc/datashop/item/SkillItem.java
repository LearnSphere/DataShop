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

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.LfaMath;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The knowledge component (skill) that is being employed.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10342 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-11-18 13:04:53 -0500 (Mon, 18 Nov 2013) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SkillItem extends Item implements java.io.Serializable, Comparable  {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(SkillItem.class.getName());

    /** Maximum allowed length of the Category column. */
    private static final Integer MAX_CATEGORY_LENGTH = 50;

    /** Name of the skill in the single KC model. */
    public static final String SINGLE_KC_MODEL_SKILL_NAME = "Single-KC";

    /** Database generated unique Id for this selection. */
    private Long id;
    /** Skill model that this skill is associated with. */
    private SkillModelItem skillModel;
    /** Name of this skill as a string. */
    private String skillName;
    /** Category as a string that this skill is part of. */
    private String category;
    /** Beta score for this skill. */
    private Double beta;
    /** Gamma score for this skill. */
    private Double gamma;
    /** Collection of subgoals associated with this skill. */
    private Set subgoals;
    /** Collection of sets associated with this skill. */
    private Set sets;

    /** Default constructor. */
    public SkillItem() {
    }

    /**
     *  Constructor with id.
     *  @param skillId Database generated unique Id for this selection.
     */
    public SkillItem(Long skillId) {
        this.id = skillId;
    }

    /**
     * Get skillId.
     * @return the Long id as a Comparable.
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set skillId.
     * @param skillId Database generated unique Id for this selection.
     */
    public void setId(Long skillId) {
        this.id = skillId;
    }
    /**
     * Get skillModel.
     * @return edu.cmu.pslc.datashop.item.SkillModelItem
     */

    public SkillModelItem getSkillModel() {
        return this.skillModel;
    }

    /**
     * Set skillModel.
     * @param skillModel Skill model that this skill is associated with.
     */
    public void setSkillModel(SkillModelItem skillModel) {
        this.skillModel = skillModel;
    }
    /**
     * Get skillName.
     * @return java.lang.String
     */

    public String getSkillName() {
        return this.skillName;
    }

    /**
     * Set skillName.
     * @param skillName Name of this skill as a string.
     */
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    /**
     * Get category.
     * @return java.lang.String
     */

    public String getCategory() {
        return this.category;
    }

    /**
     * Set category.
     * @param category Category as a string that this skill is part of.
     */
    public void setCategory(String category) {
        this.category = this.truncateValue(logger, "Category", category, MAX_CATEGORY_LENGTH);
    }
    /**
     * Get beta (skill intercept).
     * @return Double
     */

    public Double getBeta() {
        return this.beta;
    }

    /**
     * Set beta (skill intercept).
     * @param beta Beta score for this skill.
     */
    public void setBeta(Double beta) {
        this.beta = beta;
    }
    /**
     * Get gamma (skill slope).
     * @return Double
     */

    public Double getGamma() {
        return this.gamma;
    }

    /**
     * Set gamma (skill slope).
     * @param gamma Gamma score for this skill.
     */
    public void setGamma(Double gamma) {
        this.gamma = gamma;
    }

    /**
     * Calculate the probability.  This is easier than storing it in the database.
     * @return the skill intercept as a probability
     */
    public Double getSkillInterceptAsProbability() {
        if (this.beta == null) { return null; }
        return LfaMath.inverseLogit(this.beta);
    }

    /**
     * Get subgoals.
     * @return java.util.Set
     */
    protected Set getSubgoals() {
        if (this.subgoals == null) {
            this.subgoals = new HashSet();
        }
        return this.subgoals;
    }

    /**
     * Public method to get Subgoals.
     * @return a list instead of a set
     */
    public List getSubgoalsExternal() {
        List sortedList = new ArrayList(getSubgoals());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set subgoals.
     * @param subgoals Collection of subgoals associated with this skill.
     */
    protected void setSubgoals(Set subgoals) {
        this.subgoals = subgoals;
    }

    /**
     * Add a Subgoal.
     * @param item dataset to add
     */
    public void addSubgoal(SubgoalItem item) {
        if (!getSubgoals().contains(item)) {
            getSubgoals().add(item);
            item.addSkill(this);
        }
    }

    /**
     * Remove a subgoal.
     * @param item the item to remove.
     */
    public void removeSubgoal(SubgoalItem item) {
        if (getSubgoals().contains(item)) {
            getSubgoals().remove(item);
            item.removeSkill(this);
        }
    }

    /**
     * Get sets.
     * @return java.util.Set
     */
    protected Set getSets() {
        if (this.sets == null) {
            this.sets = new HashSet();
        }
        return this.sets;
    }

    /**
     * Public method to get Subgoals.
     * @return a list instead of a set
     */
    public List getSetsExternal() {
        List sortedList = new ArrayList(getSets());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set sets.
     * @param sets Collection of sets associated with this skill.
     */
    protected void setSets(Set sets) {
        this.sets = sets;
    }

    /**
     * Add a Set.
     * @param item dataset to add
     */
    public void addSet(SetItem item) {
        if (!getSets().contains(item)) {
            getSets().add(item);
            item.addSkill(this);
        }
    }

    /**
     * Remove a set.
     * @param item the item to remove.
     */
    public void removeSet(SetItem item) {
        if (getSets().contains(item)) {
            getSets().remove(item);
            item.removeSkill(this);
        }
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
         buffer.append(objectToString("skillId", getId()));
         buffer.append(objectToString("skillName", getSkillName()));
         buffer.append(objectToStringFK("skillModelId", getSkillModel()));
         buffer.append(objectToString("category", getCategory()));
         buffer.append(objectToString("beta", getBeta()));
         buffer.append(objectToString("gamma", getGamma()));
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
          if (obj instanceof SkillItem) {
            SkillItem otherItem = (SkillItem)obj;

            if (!objectEquals(this.getSkillName(), otherItem.getSkillName())) {
                return false;
            }

            if (!objectEquals(this.getCategory(), otherItem.getCategory())) {
                return false;
            }

            if (!objectEqualsFK(this.getSkillModel(), otherItem.getSkillModel())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getSkillModel());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSkillName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getCategory());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>skill model</li>
     * <li>category</li>
     * <li>name</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SkillItem otherItem = (SkillItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getSkillModel(), otherItem.getSkillModel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCategory(), otherItem.getCategory());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSkillName(), otherItem.getSkillName());
        if (value != 0) { return value; }

        return value;
    }
}