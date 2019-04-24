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
 * The instructor/teacher of a class.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */

public class InstructorItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this instructor. */
    private Long instructorId;
    /** School which this instructor is part of. */
    private SchoolItem school;
    /** Name of the instructor as a string. */
    private String instructorName;
    /** Classes for which this instructor is a teacher. */
    private Set classes;

    /** Default constructor. */
    public InstructorItem() {
    }

    /**
     *  Constructor with id.
     *  @param instructorId Database generated unique Id for this instructor.
     */
    public InstructorItem(Long instructorId) {
        this.instructorId = instructorId;
    }

    /**
     * Returns the id.
     * @return the Long id as a Comparable
     */
    public Comparable getId() {
        return this.instructorId;
    }

    /**
     * Set instructorId.
     * @param instructorId Database generated unique Id for this instructor.
     */
    public void setId(Long instructorId) {
        this.instructorId = instructorId;
    }

    /**
     * Get school.
     * @return edu.cmu.pslc.datashop.item.SchoolItem
     */
    public SchoolItem getSchool() {
        return this.school;
    }

    /**
     * Set school.
     * @param school School which this instructor is part of.
     */
    public void setSchool(SchoolItem school) {
        this.school = school;
    }

    /**
     * Get instructorName.
     * @return java.lang.String
     */
    public String getInstructorName() {
        return this.instructorName;
    }

    /**
     * Set instructorName.
     * @param instructorName Name of the instructor as a string.
     */
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    /**
     * Get classes.
     * @return java.util.Set
     */
    protected Set getClasses() {
        if (this.classes == null) {
            this.classes = new HashSet();
        }
        return this.classes;
    }

    /**
     * Public method to get Classes.
     * @return a list instead of a set
     */
    public List getClassesExternal() {
        List sortedList = new ArrayList(getClasses());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a class.
     * @param item class to add
     */
    public void addClass(ClassItem item) {
        getClasses().add(item);
        item.setInstructor(this);
    }

    /**
     * Remove a class.
     * @param item class to remove
     */
    public void removeClass(ClassItem item) {
        getClasses().remove(item);
        item.setInstructor(null);
    }

    /**
     * Set classes.
     * @param classes Classes for which this instructor is a teacher.
     */
    public void setClasses(Set classes) {
        this.classes = classes;
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
        buffer.append(objectToString("InstructorName", getInstructorName()));
        buffer.append(objectToStringFK("School", getSchool()));
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
        if (obj instanceof InstructorItem) {
            InstructorItem otherItem = (InstructorItem)obj;

            if (!objectEquals(this.getInstructorName(), otherItem.getInstructorName())) {
                return false;
            }
            if (!objectEqualsFK(this.getSchool(), otherItem.getSchool())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInstructorName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSchool());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>school</li>
     * <li>name</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        InstructorItem otherItem = (InstructorItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getSchool(), otherItem.getSchool());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInstructorName(), otherItem.getInstructorName());
        if (value != 0) { return value; }

        return value;
    }
}