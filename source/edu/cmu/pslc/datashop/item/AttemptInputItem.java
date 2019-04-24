/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A input for a given attempt by a student.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 7405 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2011-12-12 09:45:45 -0500 (Mon, 12 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class AttemptInputItem extends Item implements java.io.Serializable, Comparable, AttemptSAI {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(AttemptInputItem.class.getName());

    /** Database generated unique Id for this input. */
    private Long id;
    /** The subgoal this input was attempted in. */
    private SubgoalAttemptItem subgoalAttempt;
    /** The Input as a string. */
    private String input;
    /** The string of the input which has been corrected by the tutor. */
    private String correctedInput;
    /** The type of input as a string. */
    private String type;
    /** The xml_id of the input as a string */
    private String xmlId;

    /** Default constructor. */
    public AttemptInputItem() {
    }

    /**
     *  Constructor with id.
     *  @param attemptInputId Database generated unique Id for this input.
     */
    public AttemptInputItem(Long attemptInputId) {
        this.id = attemptInputId;
    }

    /**
     * Get attemptInputId.
     * @return the Long id as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set attemptInputId.
     * @param attemptInputId Database generated unique Id for this input.
     */
    public void setId(Long attemptInputId) {
        this.id = attemptInputId;
    }

    /**
     * Get subgoalAttempt.
     * @return edu.cmu.pslc.datashop.item.SubgoalAttemptItem
     */
    public SubgoalAttemptItem getSubgoalAttempt() {
        return this.subgoalAttempt;
    }

    /**
     * Set subgoalAttempt.
     * @param subgoalAttempt The subgoal this input was attempted in.
     */
    public void setSubgoalAttempt(SubgoalAttemptItem subgoalAttempt) {
        this.subgoalAttempt = subgoalAttempt;
    }

    /**
     * Get input.
     * @return java.lang.String
     */
    public String getInput() {
        return this.input;
    }

    /**
     * Set input.
     * @param input The Input as a string.
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Get corrected input.
     * @return java.lang.String
     */
    public String getCorrectedInput() {
        return this.correctedInput;
    }

    /**
     * Set corrected input.
     * @param correctedInput The corrected input as a string.
     */
    public void setCorrectedInput(String correctedInput) {
        this.correctedInput = correctedInput;
    }

    /** Get type. @return Returns the type. */
    public String getType() {
        return this.type;
    }

    /** Set type. @param type The type of selection as a string. */
    public void setType(String type) {
        this.type = type;
    }

    /** Returns xmlId. @return Returns the xmlId. */
    public String getXmlId() {
        return xmlId;
    }

    /** Set xmlId. @param xmlId The xmlId to set. */
    public void setXmlId(String xmlId) {
        this.xmlId = xmlId;
    }

    /**
     *  Set the selection, action, or input value.
     *  @param value  the selection, action, or input value
     */
    public void setValue(String value) {
        setInput(value);
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
         buffer.append(objectToString("attemptInputId", getId()));
         buffer.append(objectToStringFK("subgoalAttempt", getSubgoalAttempt()));
         buffer.append(objectToString("type", getType()));
         buffer.append(objectToString("xml_id", getXmlId()));
         buffer.append(objectToString("Input", getInput()));
         buffer.append(objectToString("CorrectedInput", getCorrectedInput()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
    * Equals function for this class.
    * This method checks the following attributes:
    * <ul>
    * <li>subgoal attempt</li>
    * <li>input</li>
    * <li>corrected input</li>
    * </ul>
    * @param obj Object of any type, should be an Item for equality check
    * @return boolean true if the items are equal, false if not
    */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AttemptInputItem) {
            AttemptInputItem otherItem = (AttemptInputItem)obj;

            if (!objectEqualsFK(this.getSubgoalAttempt(),
                    otherItem.getSubgoalAttempt())) {
                return false;
            }
            if (!objectEquals(this.getInput(), otherItem.getInput())) {
                return false;
            }
            if (!objectEquals(this.getCorrectedInput(), otherItem.getCorrectedInput())) {
                return false;
            }

            if (!objectEquals(this.getType(), otherItem.getType())) {
                return false;
            }

            if (!objectEquals(this.getXmlId(), otherItem.getXmlId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getSubgoalAttempt());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getInput());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getCorrectedInput());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getXmlId());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>subgoalAttempt</li>
     * <li>type</li>
     * <li>xmlId</li>
     * <li>input</li>
     * <li>corrected input</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AttemptInputItem otherItem = (AttemptInputItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getSubgoalAttempt(), otherItem.getSubgoalAttempt());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getXmlId(), otherItem.getXmlId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInput(), otherItem.getInput());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCorrectedInput(), otherItem.getCorrectedInput());
        if (value != 0) { return value; }

        return value;
    }

}