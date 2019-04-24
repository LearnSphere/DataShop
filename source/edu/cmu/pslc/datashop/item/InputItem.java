/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Correct/Expected input for a given subgoal.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class InputItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this input. */
    private Long id;
    /** The subgoal for this input. */
    private SubgoalItem subgoal;
    /** The input as a string. */
    private String input;

    /** Default constructor. */
    public InputItem() {
    }

    /**
     *  Constructor with id.
     *  @param inputId Database generated unique Id for this input.
     */
    public InputItem(Long inputId) {
        this.id = inputId;
    }

    /**
     * Get inputId.
     * @return Long
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set inputId.
     * @param inputId Database generated unique Id for this input.
     */
    public void setId(Long inputId) {
        this.id = inputId;
    }
    /**
     * Get subgoal.
     * @return the subgoal item
     */

    public SubgoalItem getSubgoal() {
        return this.subgoal;
    }

    /**
     * Set subgoal.
     * @param subgoal The subgoal for this input.
     */
    public void setSubgoal(SubgoalItem subgoal) {
        this.subgoal = subgoal;
    }
    /**
     * Get input.
     * @return string representation of what the user entered
     */

    public String getInput() {
        return this.input;
    }

    /**
     * Set input.
     * @param input The input as a string.
     */
    public void setInput(String input) {
        this.input = input;
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
         buffer.append(objectToString("inputId", getId()));
         buffer.append(objectToStringFK("subgoalId", getSubgoal()));
         buffer.append(objectToString("input", getInput()));
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
        if (obj instanceof InputItem) {
            InputItem otherItem = (InputItem)obj;

            if (!Item.objectEquals(this.getInput(), otherItem.getInput())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getSubgoal(), otherItem.getSubgoal())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInput());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSubgoal());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>Subgoal</li>
      *   <li>Input</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         InputItem otherItem = (InputItem)obj;

         int value = 0;

         value = objectCompareToFK(this.getSubgoal(), otherItem.getSubgoal());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getInput(), otherItem.getInput());
         if (value != 0) { return value; }

         return value;
     }

}