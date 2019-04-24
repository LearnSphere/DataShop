/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The expected/correct selection for the given subgoal.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SelectionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this selection. */
    private Long id;
    /** The subgoal this selection is associated with. */
    private SubgoalItem subgoal;
    /** The Selection as as string. */
    private String selection;
    /** The type of selection as a string. */
    private String type;
    /** The xml_id of the selection as a string */
    private String xmlId;


    /** Default constructor. */
    public SelectionItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id for this selection.
     */
    public SelectionItem(Long id) {
        this.id = id;
    }

    /**
     * Set selection id.
     * @param id Database generated unique Id for this selection.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Return the unique identifier for this item.
     * @return The Long id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
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
     * @param subgoal The subgoal this selection is associated with.
     */
    public void setSubgoal(SubgoalItem subgoal) {
        this.subgoal = subgoal;
        //subgoal.addSelection(this);
    }

    /**
     * Get selection.
     * @return the actual selection
     */

    public String getSelection() {
        return this.selection;
    }

    /**
     * Set selection.
     * @param selection The Selection as as string.
     */
    public void setSelection(String selection) {
        this.selection = selection;
    }
    /**
     * Get type.
     * @return the type of the selection
     */

    public String getType() {
        return this.type;
    }

    /**
     * Set type.
     * @param type The type of selection as a string.
     */
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
         buffer.append(objectToString("selectionId", getId()));
         buffer.append(objectToStringFK("subgoalId", getSubgoal()));
         buffer.append(objectToString("type", getType()));
         buffer.append(objectToString("xml_id", getXmlId()));
         buffer.append(objectToString("selection", getSelection()));
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
        if (obj instanceof SelectionItem) {
            SelectionItem otherItem = (SelectionItem)obj;

            if (!Item.objectEquals(this.getSelection(), otherItem.getSelection())) {
                return false;
            }

            if (!Item.objectEquals(this.getType(), otherItem.getType())) {
                return false;
            }

            if (!Item.objectEquals(this.getXmlId(), otherItem.getXmlId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSelection());
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
    *   <li>Type</li>
    *   <li>xml_id</li>
    *   <li>Selection</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        SelectionItem otherItem = (SelectionItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getSubgoal(), otherItem.getSubgoal());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getXmlId(), otherItem.getXmlId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSelection(), otherItem.getSelection());
        if (value != 0) { return value; }

        return value;
    }

}