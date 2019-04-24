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
 * A school in which a dataset was recorded.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SchoolItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this school. */
    private Integer schoolId;
    /** The name of this school as a string. */
    private String schoolName;
    /** Collection of instructors in this school. */
    private Set instructors;
    /** Collection of classes taught at this school. */
    private Set classes;
    /** Collection of transactions associated with this school. */
    private Set tutorTransactions;

    /** Default constructor. */
    public SchoolItem() {
    }

    /**
     *  Constructor with id.
     *  @param schoolId Database generated unique Id for this school.
     */
    public SchoolItem(Integer schoolId) {
        this.schoolId = schoolId;
    }

    /**
     * Returns the id.
     * @return Integer
     */
    public Comparable getId() {
        return this.schoolId;
    }

    /**
     * Set schoolId.
     * @param schoolId Database generated unique Id for this school.
     */
    public void setId(Integer schoolId) {
        this.schoolId = schoolId;
    }

    /**
     * Get schoolName.
     * @return java.lang.String
     */

    public String getSchoolName() {
        return this.schoolName;
    }

    /**
     * Set schoolName.
     * @param schoolName The name of this school as a string.
     */
    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    /**
     * Get instructors.
     * @return java.util.Set
     */
    protected Set getInstructors() {
        if (this.instructors == null) {
            this.instructors = new HashSet();
        }
        return this.instructors;
    }

    /**
     * Public method to get Instructors.
     * @return a list instead of a set
     */
    public List getInstructorsExternal() {
        List sortedList = new ArrayList(getInstructors());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a instructor.
     * @param item instructor to add
     */
    public void addInstructor(InstructorItem item) {
        getInstructors().add(item);
        item.setSchool(this);
    }

    /**
     * Remove a instructor.
     * @param item instructor to remove
     */
    public void removeInstructor(InstructorItem item) {
        getInstructors().remove(item);
        item.setSchool(null);
    }

    /**
     * Set instructors.
     * @param instructors Collection of instructors in this school.
     */
    public void setInstructors(Set instructors) {
        this.instructors = instructors;
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
        item.setSchool(this);
    }

    /**
     * Remove a class.
     * @param item class to remove
     */
    public void removeClass(ClassItem item) {
        getClasses().remove(item);
        item.setSchool(null);
    }

    /**
     * Set classes.
     * @param classes Collection of classes taught at this school.
     */
    public void setClasses(Set classes) {
        this.classes = classes;
    }

    /**
     * Get tutorTransactions.
     * @return java.util.Set
     */
    protected Set getTutorTransactions() {
        if (this.tutorTransactions == null) {
            this.tutorTransactions = new HashSet();
        }
        return this.tutorTransactions;
    }

    /**
     * Public method to get tutorTransactions.
     * @return a list instead of a set
     */
    public List getTutorTransactionsExternal() {
        List sortedList = new ArrayList(getTutorTransactions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a tutorTransaction.
     * @param item tutorTransactios to add
     */
    public void addTutorTransaction(TransactionItem item) {
        getTutorTransactions().add(item);
        item.setSchool(this);
    }

    /**
     * Remove a tutorTransaction.
     * @param item tutorTransactios to remove
     */
    public void removeTutorTransaction(TransactionItem item) {
        getTutorTransactions().remove(item);
        item.setSchool(null);
    }

    /**
     * Set tutorTransactions.
     * @param tutorTransactions Collection of transactions associated with this school.
     */
    public void setTutorTransactions(Set tutorTransactions) {
        this.tutorTransactions = tutorTransactions;
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
        buffer.append(objectToString("SchoolName", getSchoolName()));
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
        if (obj instanceof SchoolItem) {
            SchoolItem otherItem = (SchoolItem)obj;

            if (!objectEquals(this.getSchoolName(), otherItem.getSchoolName())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSchoolName());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>name</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SchoolItem otherItem = (SchoolItem)obj;
        int value = 0;

        value = objectCompareTo(this.getSchoolName(), otherItem.getSchoolName());
        if (value != 0) { return value; }

        return value;
    }
}