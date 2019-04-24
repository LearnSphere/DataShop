/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The AlphaScore maps a student to a skill and contains the
 * alpha score which is a statistic from the learning factors project.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */

public class AlphaScoreItem extends Item implements java.io.Serializable, Comparable  {

    /** The composite key. */
    private AlphaScoreId id;
    /** Part of the composite PK - FK of the student Id. */
    private StudentItem student;
    /** Part of the composite PK - FK of the student Id. */
    private SkillModelItem skillModel;
    /** The alpha score for a this student for this skill model. */
    private Double alpha;

    /** Default constructor. */
    public AlphaScoreItem() {
    }

    /**
     *  Constructor with id.
     *  @param id the composite key
     */
    public AlphaScoreItem(AlphaScoreId id) {
        this.id = id;
    }

    /**
     * Returns the id.
     * @return AlphaScoreId
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set composite key.
     * @param id the composite key
     */
    protected void setId(AlphaScoreId id) {
        this.id = id;
    }

    /**
     * Get student.
     * @return edu.cmu.pslc.datashop.item.StudentItem
     */
    public StudentItem getStudent() {
        return this.student;
    }

    /**
     * Set student.
     * Package protected for hibernate only to prevent conflicts with the composite key
     * @param student Part of the composite PK - FK of the student Id.
     */
    protected void setStudent(StudentItem student) {
        this.student = student;
    }

    /**
     * Public Set Student method to update the composite key as well.
     * @param student Part of the composite key - FK to the student table.
     */
    public void setStudentExternal(StudentItem student) {
        setStudent(student);
        this.id = new AlphaScoreId(this.student, this.skillModel);
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
     * Package protected for hibernate only to prevent conflicts with the composite key
     * @param skillModel Part of the composite PK - FK of the student Id.
     */
    protected void setSkillModel(SkillModelItem skillModel) {
        this.skillModel = skillModel;
    }

    /**
     * Public Set SkillModel method to update the composite key as well.
     * @param skillModel Part of the composite key - FK to the skill model table.
     */
    public void setSkillModelExternal(SkillModelItem skillModel) {
        setSkillModel(skillModel);
        this.id = new AlphaScoreId(this.student, this.skillModel);
    }

    /**
     * Get alpha.
     * @return Double
     */
    public Double getAlpha() {
        return this.alpha;
    }

    /**
     * Set alpha.
     * @param alpha The alpha score for a this student for this skill model.
     */
    public void setAlpha(Double alpha) {
        this.alpha = alpha;
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
         buffer.append(objectToString("Alpha", getAlpha()));
         buffer.append(objectToStringFK("Student Item Id", getStudent()));
         buffer.append(objectToStringFK("SkillModel Item Id", getSkillModel()));
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
        if (obj instanceof AlphaScoreItem) {
            AlphaScoreItem otherItem = (AlphaScoreItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }

            if (!objectEquals(this.getAlpha(), otherItem.getAlpha())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAlpha());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>skill model</li>
     * <li>student</li>
     * <li>alpha</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AlphaScoreItem otherItem = (AlphaScoreItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getSkillModel(), otherItem.getSkillModel());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getStudent(), otherItem.getStudent());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAlpha(), otherItem.getAlpha());
        if (value != 0) { return value; }

        return value;
    }
}