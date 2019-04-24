/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the cog_step_sequence table.
 *
 * @author Hui Cheng
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CogStepSeqItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this cogStepSequence. */
    private Long cogStepSeqId;

    /** CognitiveStep associated with this item. */
    private CognitiveStepItem cognitiveStep;
    /** Interpretation associated with this item. */
    private InterpretationItem interpretation;
    /** The position of this step within the sequence. */
    private Integer position;
    /** Flag indicating whether this sequence is a correct sequence or not. */
    private Boolean correctFlag;

    /** Default constructor. */
    public CogStepSeqItem() {
        this.correctFlag = Boolean.FALSE;
    }

    /**
     *  Constructor with id.
     *  @param cogStepSeqId Database generated unique Id for this cog step sequence.
     */
    public CogStepSeqItem(Long cogStepSeqId) {
        this.cogStepSeqId = cogStepSeqId;
        this.correctFlag = Boolean.FALSE;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() {
        return this.cogStepSeqId;
    }

    /**
     * Set the id.
     * @param cogStepSeqId Database generated unique Id for this cog step sequence.
     */
    public void setId(Long cogStepSeqId) {
        this.cogStepSeqId = cogStepSeqId;
    }

    /**
     * Get cognitive step.
     * @return edu.cmu.pslc.datashop.item.CognitiveStepItem
     */
    public CognitiveStepItem getCognitiveStep() {
        return this.cognitiveStep;
    }

    /**
     * Set cognitive step.
     * @param cognitiveStep Cognitive step associated with this item.
     */
    public void setCognitiveStep(CognitiveStepItem cognitiveStep) {
        this.cognitiveStep = cognitiveStep;
    }

    /**
     * Get interpretation.
     * @return edu.cmu.pslc.datashop.item.InterpretationItem
     */
    public InterpretationItem getInterpretation() {
        return this.interpretation;
    }

    /**
     * Set interpretation.
     * @param interpretation Interpretation associated with this cog step sequence.
     */
    public void setInterpretation(InterpretationItem interpretation) {
        this.interpretation = interpretation;
    }

    /**
     * Get position.
     * @return java.lang.Integer
     */
    public Integer getPosition() {
        return this.position;
    }

    /**
     * Set position.
     * @param position The order of step for the sequence on this map.
     */
    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * Get correct flag.
     * @return java.lang.Boolean
     */
    public Boolean getCorrectFlag() {
        return correctFlag;
    }

    /**
     * Set the correct flag.
     * @param correctFlag Indicates whether this sequence is correct or not.
     */
    public void setCorrectFlag(Boolean correctFlag) {
        this.correctFlag = correctFlag;
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
         buffer.append(objectToStringFK("CognitiveStep", getCognitiveStep()));
         buffer.append(objectToStringFK("Interpretation", getInterpretation()));
         buffer.append(objectToString("Position", getPosition()));
         buffer.append(objectToString("CorrectFlag", getCorrectFlag()));
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
        if (obj instanceof CogStepSeqItem) {
            CogStepSeqItem otherItem = (CogStepSeqItem)obj;

            if (!objectEqualsFK(this.getCognitiveStep(), otherItem.getCognitiveStep())) {
                return false;
            }
            if (!objectEqualsFK(this.getInterpretation(), otherItem.getInterpretation())) {
                return false;
            }
            if (!objectEquals(this.getPosition(), otherItem.getPosition())) {
                return false;
            }
            if (!objectEquals(this.getCorrectFlag(), otherItem.getCorrectFlag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getCognitiveStep());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getInterpretation());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPosition());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCorrectFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>cognitive step id</li>
     * <li>interpretation id</li>
     * <li>position</li>
     * <li>correct flag</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CogStepSeqItem otherItem = (CogStepSeqItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getCognitiveStep(), otherItem.getCognitiveStep());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getInterpretation(), otherItem.getInterpretation());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPosition(), otherItem.getPosition());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCorrectFlag(), otherItem.getCorrectFlag());
        if (value != 0) { return value; }

        return value;
    }
}
