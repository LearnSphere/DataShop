/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A selection for a given attempt by a student.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 7245 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-11-09 10:12:24 -0500 (Wed, 09 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AttemptSelectionItem extends Item
        implements java.io.Serializable, Comparable, AttemptSAI  {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(AttemptSelectionItem.class.getName());

    /** Database generated unique Id for this selection. */
    private Long id;
    /** The subgoal this selection was attempted in. */
    private SubgoalAttemptItem subgoalAttempt;
    /** The selection as a string. */
    private String selection;
    /** The type of selection as a string. */
    private String type;
    /** The xml_id of the selection as a string */
    private String xmlId;

    /** Default constructor. */
    public AttemptSelectionItem() {
    }

    /**
     *  Constructor with id.
     *  @param attemptSelectionId Database generated unique Id for this selection.
     */
    public AttemptSelectionItem(Long attemptSelectionId) {
        this.id = attemptSelectionId;
    }

    /**
     * Get attemptSelectionId.
     * @return the Long is as a comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set attemptSelectionId.
     * @param attemptSelectionId Database generated unique Id for this selection.
     */
    public void setId(Long attemptSelectionId) {
        this.id = attemptSelectionId;
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
     * @param subgoalAttempt The subgoal this selection was attempted in.
     */
    public void setSubgoalAttempt(SubgoalAttemptItem subgoalAttempt) {
        this.subgoalAttempt = subgoalAttempt;
    }

    /**
     * Get selection.
     * @return java.lang.String
     */
    public String getSelection() {
        return this.selection;
    }

    /**
     * Set selection.
     * @param selection The selection as a string.
     */
    public void setSelection(String selection) {
        this.selection = truncateTinyText(logger, "selection", selection);
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
        setSelection(value);
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
         buffer.append(objectToString("attemptSelectionId", getId()));
         buffer.append(objectToStringFK("subgoalAttempt", getSubgoalAttempt()));
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
          if (obj instanceof AttemptSelectionItem) {
              AttemptSelectionItem otherItem = (AttemptSelectionItem)obj;

              if (!objectEqualsFK(this.getSubgoalAttempt(),
                      otherItem.getSubgoalAttempt())) {
                  return false;
              }

              if (!objectEquals(this.getSelection(), otherItem.getSelection())) {
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
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSelection());
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
        * <li>selection</li>
        * </ul>
        * @param obj the object to compare this to.
        * @return the value 0 if equal; a value less than 0 if it is less than;
        * a value greater than 0 if it is greater than
        */
       public int compareTo(Object obj) {
           AttemptSelectionItem otherItem = (AttemptSelectionItem)obj;

           int value = 0;

           value = objectCompareToFK(this.getSubgoalAttempt(), otherItem.getSubgoalAttempt());
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
