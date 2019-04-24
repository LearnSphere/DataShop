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

import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A collection of selected items, skills, students or problems.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SetItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id for this Set. */
    private Integer id;
    /** The type of the items in this set, skill, student or problem. */
    private String type;
    /** The name of the Set. */
    private String name;
    /** The description of the Set. */
    private String description;
    /** The owner/creator of the set. */
    private UserItem owner;
    /** Collection of skills (knowledge components) associated with this Set. */
    private Set skills;

    /** Set type enumerated field value - "skill". */
    public static final String SET_TYPE_SKILL = "skill";

    /** Set type enumerated field value - "student". */
    public static final String SET_TYPE_STUDENT = "student";

    /** Set type enumerated field value - "problem". */
    public static final String SET_TYPE_PROBLEM = "problem";

    /** Collection of all allowed items in the level enumeration. */
    private static final List SET_TYPE_ENUM = new ArrayList();

    /**
     * Adds each set type to the enumerated list.
     */
    static {
        SET_TYPE_ENUM.add(SET_TYPE_SKILL);
        SET_TYPE_ENUM.add(SET_TYPE_STUDENT);
        SET_TYPE_ENUM.add(SET_TYPE_PROBLEM);
    }

    /**
     * Check for valid set type.
     * @param setType the set type, skill, student or problem
     * @return true if set type is valid; false otherwise
     */
    public static boolean isValidSetType(String setType) {
        if (SET_TYPE_ENUM.contains(setType)) {
            return true;
        }
        return false;
    }

    /**
     * Return a list of the valid set types.
     * @return an unmodifiable list of the valid set types
     */
    public static List getSetTypes() {
        return Collections.unmodifiableList(SET_TYPE_ENUM);
    }

    /** Default constructor. */
    public SetItem() {
    }

    /**
     * Constructor with the identifier.
     * @param setId the id.
     */
    public SetItem(Integer setId) {
        this.id = setId;
    }

    /**
     * Get SetId.
     * @return Integer
     */

    public Integer getSetId() {
        return this.id;
    }

    /**
     * Return the unique identifier for this item.
     * @return The Integer id as a comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
    * Set SetId.
    * @param id Database generated unique id for this Set.
    */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the type.
     * @return the type
     */
    protected String getType() {
        return type;
    }

    /**
     * Set type
     * but will throw a LogException if the type is not valid.
     * @param type as String.
     */
    public void setType(String type) {
        if (isValidSetType(type)) {
            this.type = type;
        } else {
            throw new LogException("Set type can only be "
                + getSetTypes() + " and not : " + type);
        }
    }

    /**
     * Get name.
     * @return String
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set name.
     * @param name The name of the Set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the owner.
     * @return the owner
     */
    public UserItem getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     * @param owner The owner to set.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /**
     * Get skills.
     * @return a set of skill items
     */
    public Set getSkills() {
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
        List sortedItems = new ArrayList(getSkills());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Return the number of skills for this set.
     * @return the number of skills in this set
     */
    public int getNumberOfSkills() {
        return getSkills().size();
    }

    /**
     * Set skills.
     * @param skills Collection of skills (knowledge components) associated with this Set.
     */
    protected void setSkills(Set skills) {
        this.skills = skills;
    }

    /**
     * Adds a Skill to this Sets list of Skills.
     * @param item to add.
     */
    public void addSkill(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
            item.addSet(this);
        }
    }

    /**
     * Adds a Skill to this Set, but does not finish the bidirectional add..
     * use this function ONLY when doing fast inserts w/o reads.
     * @param item to add.
     */
    public void addSkillFast(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
        }
    }

    /**
     * Removes a skill.
     * @param item to remove.
     */
    public void removeSkill(SkillItem item) {
        if (getSkills().contains(item)) {
            getSkills().remove(item);
            item.removeSet(this);
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
         buffer.append(objectToString("SetId", getId()));
         buffer.append(objectToString("Type", getType()));
         buffer.append(objectToString("Name", getName()));
         buffer.append(objectToString("Description", getDescription()));
         buffer.append(objectToStringFK("Owner", getOwner()));
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
        if (obj instanceof SetItem) {
            SetItem otherItem = (SetItem)obj;

            if (!Item.objectEquals(this.getType(), otherItem.getType())) {
                return false;
            }

            if (!Item.objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }

            if (!Item.objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }

            if (!Item.objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getOwner());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>problem</li>
     * <li>name</li>
     * <li>input cell type</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SetItem otherItem = (SetItem)obj;

        int value = 0;

        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        return value;
    }
}