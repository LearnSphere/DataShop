/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This is the Alpha Score Id type for the AlphaScoreItem.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AlphaScoreId implements java.io.Serializable, Comparable  {

    /** Part of the composite PK - FK of the student Id. */
    private Long studentId;
    /** Part of the composite PK - FK of the skill model Id. */
    private Long skillModelId;

    /** Default constructor. */
    public AlphaScoreId() {
    }

    /**
     * Full constructor, preferred.
     * immutable
     * @param studentItem the StudentItem.
     * @param skillModelItem the SkillModelItem.
     */
    public AlphaScoreId(StudentItem studentItem, SkillModelItem skillModelItem) {
        if (studentItem != null) {
            this.studentId = Long.valueOf(((Long)studentItem.getId()).longValue());
        }

        if (skillModelItem != null) {
            this.skillModelId = Long.valueOf(((Long)skillModelItem.getId()).longValue());
        }
    }

    /**
     * Full constructor.
     * immutable
     * @param studentId the id of the StudentItem.
     * @param skillModelId the id of the SkillModelItem.
     */
    public AlphaScoreId(Long studentId, Long skillModelId) {
        this.studentId = Long.valueOf(studentId.longValue());
        this.skillModelId = Long.valueOf(skillModelId.longValue());
    }

    /**
     * Get studentId.
     * @return Long
     */
    protected Long getStudentId() {
        return Long.valueOf(this.studentId.longValue());
    }

    /**
     * Set studentId.
     * @param studentId Part of the composite PK - FK of the student Id.
     */
    protected void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /**
     * Get skillModelId.
     * @return Long
     */
    protected Long getSkillModelId() {
        return Long.valueOf(this.skillModelId.longValue());
    }

    /**
     * Set skillModelId.
     * @param skillModelId Part of the composite PK - FK of the skill model Id.
     */
    protected void setSkillModelId(Long skillModelId) {
        this.skillModelId = skillModelId;
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
         buffer.append("studentId='").append(studentId).append("' ");
         buffer.append("skillModelId='").append(skillModelId).append("' ");
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
        if (obj instanceof AlphaScoreId) {
            AlphaScoreId otherItem = (AlphaScoreId)obj;

            if (!this.studentId.equals(otherItem.studentId)) {
                 return false;
            }
            if (!this.skillModelId.equals(otherItem.skillModelId)) {
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
        if (studentId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + studentId.hashCode();
        }
        if (skillModelId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + skillModelId.hashCode();
        }
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>student id</li>
     * <li>skill model id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AlphaScoreId otherItem = (AlphaScoreId)obj;
        int value;

        if ((this.studentId != null) && (otherItem.studentId != null)) {
            value = this.studentId.compareTo(otherItem.studentId);
            if (value != 0) { return value; }
        } else if (this.studentId != null) {
            return 1;
        } else if (otherItem.studentId != null) {
            return -1;
        }

        if ((this.skillModelId != null) && (otherItem.skillModelId != null)) {
            value = this.skillModelId.compareTo(otherItem.skillModelId);
            if (value != 0) { return value; }
        } else if (this.skillModelId != null) {
            return 1;
        } else if (otherItem.skillModelId != null) {
            return -1;
        }
        return 0;
    }
}