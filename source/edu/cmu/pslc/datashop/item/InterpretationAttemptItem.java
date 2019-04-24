/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A mapping between an interpretation and a subgoal attempt.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InterpretationAttemptItem extends Item implements java.io.Serializable, Comparable  {

    /** Composite Id of a Dataset and User */
    private InterpretationAttemptId id;

    /** The user of the dataset. */
    private InterpretationItem interpretation = null;
    /** Dataset being used. */
    private SubgoalAttemptItem subgoalAttempt = null;
    /** Chosen flag. */
    private Boolean chosenFlag;

    /** Default constructor. */
    public InterpretationAttemptItem() {
        this.chosenFlag = Boolean.FALSE;
    }

    /**
     *  Constructor with id.
     *  @param id composite key object.
     */
    public InterpretationAttemptItem(InterpretationAttemptId id) {
        this.id = id;
        this.chosenFlag = Boolean.FALSE;
    }

    /**
     * Get the composite key.
     * @return the id as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the composite key for this item.
     */
    protected void setId(InterpretationAttemptId id) {
        this.id = id;
    }

    /**
     * Get the interpretation.
     * @return an interpretation item
     */
    public InterpretationItem getInterpretation() {
        return this.interpretation;
    }

    /**
     * Set the interpretation.
     * Package protected for hibernate only to prevent conflicts with the composite key.
     * @param interpretation The interpretation of the dataset.
     */
    protected void setInterpretation(InterpretationItem interpretation) {
        this.interpretation = interpretation;
    }

    /**
     * Public method to update the composite key as well.
     * @param interpretation Part of the composite key - FK to the interpretation table.
     */
    public void setInterpretationExternal(InterpretationItem interpretation) {
        setInterpretation(interpretation);
        this.id = new InterpretationAttemptId(this.interpretation, this.subgoalAttempt);
    }

    /**
     * Get the subgoal attempt.
     * @return the subgoal attempt item
     */
    public SubgoalAttemptItem getSubgoalAttempt() {
        return this.subgoalAttempt;
    }

    /**
     * Set the subgoal attempt.
     * Package protected for hibernate only to prevent conflicts with the composite key.
     * @param subgoalAttempt SubgoalAttempt being used.
     */
    protected void setSubgoalAttempt(SubgoalAttemptItem subgoalAttempt) {
        this.subgoalAttempt = subgoalAttempt;
    }

    /**
     * Public method to update the composite key as well.
     * @param subgoalAttempt Part of the composite key - FK to the subgoalAttempt table.
     */
    public void setSubgoalAttemptExternal(SubgoalAttemptItem subgoalAttempt) {
        setSubgoalAttempt(subgoalAttempt);
        this.id = new InterpretationAttemptId(this.interpretation, this.subgoalAttempt);
    }

    /**
     * Get chosen flag.
     * @return Boolean
     */
    public Boolean getChosenFlag() {
        return this.chosenFlag;
    }

    /**
     * Set chosen flag.
     * @param chosenFlag Flag indicating whether this interpretation was chosen.
     */
    public void setChosenFlag(Boolean chosenFlag) {
        this.chosenFlag = chosenFlag;
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
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToStringFK("Interpretation", getInterpretation()));
         buffer.append(objectToStringFK("SubgoalAttempt", getSubgoalAttempt()));
         buffer.append(objectToString("ChosenFlag", getChosenFlag()));

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
        if (obj instanceof InterpretationAttemptItem) {
            InterpretationAttemptItem otherItem = (InterpretationAttemptItem)obj;

            if (!Item.objectEquals(this.getChosenFlag(), otherItem.getChosenFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getId(), otherItem.getId())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getChosenFlag());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>Interpretation</li>
      *   <li>SubgoalAttempt</li>
      *   <li>Chosen Flag</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
      */
    public int compareTo(Object obj) {
        InterpretationAttemptItem otherItem = (InterpretationAttemptItem)obj;
        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getChosenFlag(), otherItem.getChosenFlag());
        if (value != 0) { return value; }

        return 0;
    }
}