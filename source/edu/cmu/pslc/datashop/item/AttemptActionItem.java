/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The action that was attempted by a student.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6363 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-14 09:52:39 -0400 (Thu, 14 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */

public class AttemptActionItem extends Item
        implements java.io.Serializable, Comparable, AttemptSAI  {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(AttemptActionItem.class.getName());

    /** Database generated unique Id for this action. */
    private Long id;
    /** The subgoal this action was attempted in. */
    private SubgoalAttemptItem subgoalAttempt;
    /** The action as a string. */
    private String action;
    /** The type of action as a string. */
    private String type;
    /** The xml_id of the action as a string */
    private String xmlId;

    /** Default constructor. */
    public AttemptActionItem() {
    }

    /**
     *  Constructor with id.
     *  @param attemptActionId Database generated unique Id for this action.
     */
    public AttemptActionItem(Long attemptActionId) {
        this.id = attemptActionId;
    }

    /**
     * Get attemptActionId.
     * @return the Long id as a Comparable
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set attemptActionId.
     * @param attemptActionId Database generated unique Id for this action.
     */
    public void setId(Long attemptActionId) {
        this.id = attemptActionId;
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
     * @param subgoalAttempt The subgoal this action was attempted in.
     */
    public void setSubgoalAttempt(SubgoalAttemptItem subgoalAttempt) {
        this.subgoalAttempt = subgoalAttempt;
    }

    /**
     * Get action.
     * @return String
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Set action.
     * @param action The action as a string.
     */
    public void setAction(String action) {
        this.action = truncateTinyText(logger, "action", action);
    }

    /** Returns type. @return Returns the type. */
    public String getType() {
        return type;
    }

    /** Set type. @param type The type to set. */
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
        setAction(value);
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
         buffer.append(objectToString("attemptActionId", getId()));
         buffer.append(objectToStringFK("subgoalAttempt", getSubgoalAttempt()));
         buffer.append(objectToString("type", getType()));
         buffer.append(objectToString("xml_id", getXmlId()));
         buffer.append(objectToString("action", getAction()));
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
        if (obj instanceof AttemptActionItem) {
            AttemptActionItem otherItem = (AttemptActionItem)obj;

            if (!objectEqualsFK(this.getSubgoalAttempt(),
                    otherItem.getSubgoalAttempt())) {
                return false;
            }

            if (!objectEquals(this.getAction(), otherItem.getAction())) {
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
    * @return int the hash code as an int.
    */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getSubgoalAttempt());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getAction());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getXmlId());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>subgoalAttempt</li>
     * <li>action</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AttemptActionItem otherItem = (AttemptActionItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getSubgoalAttempt(), otherItem.getSubgoalAttempt());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getXmlId(), otherItem.getXmlId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        return value;
    }
}