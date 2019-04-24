/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This is the action of a subgoal.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6363 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-14 09:52:39 -0400 (Thu, 14 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ActionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id. */
    private Long id;
    /** The subgoal that this action is part of. */
    private SubgoalItem subgoal;
    /** The action logged by the tutor. */
    private String action;
    /** The type of action as a string. */
    private String type;
    /** The xml_id of the action as a string */
    private String xmlId;

    /** Default constructor. */
    public ActionItem() {
    }

    /**
     *  Constructor with id.
     *  @param actionId Database generated unique id.
     */
    public ActionItem(Long actionId) {
        this.id = actionId;
    }

    /**
     * Get actionId.
     * @return the Long id as a Comparable.
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set actionId.
     * @param actionId Database generated unique id.
     */
    public void setId(Long actionId) {
        this.id = actionId;
    }
    /**
     * Get subgoal.
     * @return edu.cmu.pslc.datashop.item.SubgoalItem
     */
    public SubgoalItem getSubgoal() {
        return this.subgoal;
    }

    /**
     * Set subgoal.
     * @param subgoal The subgoal that this action is part of.
     */
    public void setSubgoal(SubgoalItem subgoal) {
        this.subgoal = subgoal;
        //subgoal.addAction(this);
    }
    /**
     * Get action.
     * @return java.lang.String
     */

    public String getAction() {
        return this.action;
    }

    /**
     * Set action.
     * @param action The action logged by the tutor.
     */
    public void setAction(String action) {
        this.action = action;
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
         buffer.append(objectToString("actionId", getId()));
         buffer.append(objectToStringFK("subgoalId", getSubgoal()));
         buffer.append(objectToString("type", getType()));
         buffer.append(objectToString("xml_id", getXmlId()));
         buffer.append(objectToString("action", getAction()));
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
        if (obj instanceof ActionItem) {
            ActionItem otherItem = (ActionItem)obj;

            if (!objectEquals(this.getAction(), otherItem.getAction())) {
                return false;
            }
            if (!objectEqualsFK(this.getSubgoal(), otherItem.getSubgoal())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getXmlId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSubgoal());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Subgoal</li>
     *   <li>type</li>
     *   <li>xml_id</li>
     *   <li>Action</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        ActionItem otherItem = (ActionItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getSubgoal(), otherItem.getSubgoal());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getXmlId(), otherItem.getXmlId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        return value;
    }

}