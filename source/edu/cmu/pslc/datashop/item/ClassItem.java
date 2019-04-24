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
 * A single class which contains students.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ClassItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for a class. */
    private Long classId;
    /** Name of the class as a string. */
    private String className;
    /** (FK) The school this class is in. */
    private SchoolItem school;
    /** (FK) The instructor of this class. */
    private InstructorItem instructor;
    /** Period of this class. */
    private String period;
    /** Description of this class as a string. */
    private String description;
    /** Collection of datasets this class is part of. */
    private Set datasets;
    /** Collection of all students in this class. */
    private Set students;
    /** Collection of tutor transactions that this class is associated with. */
    private Set tutorTransactions;

    /** Default constructor. */
    public ClassItem() {
    }

    /**
     *  Constructor with id.
     *  @param classId Database generated unique Id for a class.
     */
    public ClassItem(Long classId) {
        this.classId = classId;
    }

    /**
     * Returns the id as a comparable.
     * @return the Long id as a comparable
     */
    public Comparable getId() {
        return this.classId;
    }

    /**
     * Set classId.
     * @param classId Database generated unique Id for a class.
     */
    public void setId(Long classId) {
        this.classId = classId;
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
     * @param school (FK) The school this class is in.
     */
    public void setSchool(SchoolItem school) {
        this.school = school;
    }

    /**
     * Get instructor.
     * @return edu.cmu.pslc.datashop.item.InstructorItem
     */
    public InstructorItem getInstructor() {
        return this.instructor;
    }

    /**
     * Set instructor.
     * @param instructor (FK) The instructor of this class.
     */
    public void setInstructor(InstructorItem instructor) {
        this.instructor = instructor;
    }

    /**
     * Get className.
     * @return java.lang.String
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Set className.
     * @param className Name of the class as a string.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Get period.
     * @return java.lang.String
     */
    public String getPeriod() {
        return this.period;
    }

    /**
     * Set period.
     * @param period Period of this class.
     */
    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * Get description.
     * @return java.lang.String
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description Description of this class as a string.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get datasets.
     * @return java.util.Set
     */
    protected Set getDatasets() {
        if (this.datasets == null) {
            this.datasets = new HashSet();
        }
        return this.datasets;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List getDatasetsExternal() {
        List sortedList = new ArrayList(getDatasets());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset.
     * @param item dataset to add
     */
    public void addDataset(DatasetItem item) {
        if (!getDatasets().contains(item)) {
            getDatasets().add(item);
            item.addClass(this);
        }
    }

    /**
     * Remove a dataset.
     * @param item dataset to remove
     */
    public void removeDataset(DatasetItem item) {
        if (getDatasets().contains(item)) {
            getDatasets().remove(item);
            item.removeClass(this);
        }
    }

    /**
     * Set datasets.
     * @param datasets Collection of datasets this class is part of.
     */
    public void setDatasets(Set datasets) {
        this.datasets = datasets;
    }

    /**
     * Get students.
     * @return the set of students
     */
    protected Set getStudents() {
        if (this.students == null) {
            this.students = new HashSet();
        }
        return this.students;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List getStudentsExternal() {
        List sortedList = new ArrayList(getStudents());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a student.
     * @param item student to add
     */
    public void addStudent(StudentItem item) {
        if (!getStudents().contains(item)) {
            getStudents().add(item);
            item.addClass(this);
        }
    }

    /**
     * Remove a student.
     * @param item student to remove
     */
    public void removeStudent(StudentItem item) {
        if (getStudents().contains(item)) {
            getStudents().remove(item);
            item.removeClass(this);
        }
    }

    /**
     * Set students.
     * @param students Collection of all students in this class.
     */
    public void setStudents(Set students) {
        this.students = students;
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
        List sortedDatasets = new ArrayList(getTutorTransactions());
        Collections.sort(sortedDatasets);
        return Collections.unmodifiableList(sortedDatasets);
    }

    /**
     * Add a tutorTransaction.
     * @param tutorTransaction tutorTransactios to add
     */
    public void addTutorTransaction(TransactionItem tutorTransaction) {
        getTutorTransactions().add(tutorTransaction);
        tutorTransaction.setClassItem(this);
    }

    /**
     * Remove a tutorTransaction.
     * @param tutorTransaction tutorTransactios to add
     */
    public void removeTutorTransaction(TransactionItem tutorTransaction) {
        getTutorTransactions().remove(tutorTransaction);
        tutorTransaction.setClassItem(null);
    }

    /**
     * Set tutorTransactions.
     * @param tutorTransactions Collection of tutor transactions that this class is associated with.
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
        buffer.append(objectToString("ClassName", getClassName()));
        buffer.append(objectToString("Period", getPeriod()));
        buffer.append(objectToString("Description", getDescription()));
        buffer.append(objectToStringFK("School", getSchool()));
        buffer.append(objectToStringFK("Instructor", getInstructor()));
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
        if (obj instanceof ClassItem) {
            ClassItem otherItem = (ClassItem)obj;

            if (!objectEquals(this.getClassName(), otherItem.getClassName())) {
                return false;
            }
            if (!objectEquals(this.getPeriod(), otherItem.getPeriod())) {
                return false;
            }
            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!objectEqualsFK(this.getSchool(), otherItem.getSchool())) {
                return false;
            }
            if (!objectEqualsFK(this.getInstructor(), otherItem.getInstructor())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getClassName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPeriod());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSchool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getInstructor());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>school</li>
     * <li>name</li>
     * <li>instructor</li>
     * <li>period</li>
     * <li>description</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ClassItem otherItem = (ClassItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getSchool(), otherItem.getSchool());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getClassName(), otherItem.getClassName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getInstructor(), otherItem.getInstructor());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPeriod(), otherItem.getPeriod());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        return value;
    }
}