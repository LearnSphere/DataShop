/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A notable condition on a problem. Most commonly used
 * to differentiate between experimental conditions.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6363 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-14 09:52:39 -0400 (Thu, 14 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ConditionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this condition. */
    private Long id;
    /** The name of this condition. */
    private String conditionName;
    /** The type of this condition as a string. */
    private String type;
    /** Description of this condition as a string. */
    private String description;
    /** Dataset associated with this condition. */
    private DatasetItem dataset;
    /** Collection of transactions associated with this condition */
    //private Set transactions;

    /** Default constructor. */
    public ConditionItem() {
    }

    /**
     *  Constructor with id.
     *  @param conditionId Database generated unique Id for this condition.
     */
    public ConditionItem(Long conditionId) {
        this.id = conditionId;
    }

    /**
     * Get conditionId.
     * @return the Long id as a comparable.
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set conditionId.
     * @param conditionId Database generated unique Id for this condition.
     */
    public void setId(Long conditionId) {
        this.id = conditionId;
    }
    /**
     * Get conditionName.
     * @return java.lang.String
     */

    public String getConditionName() {
        return this.conditionName;
    }

    /**
     * Set conditionName.
     * @param conditionName The name of this condition.
     */
    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }
    /**
     * Get type.
     * @return java.lang.String
     */

    public String getType() {
        return this.type;
    }

    /**
     * Set type.
     * @param type The type of this condition as a string.
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * Get description.
     * @return java.lang.Integer
     */

    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description Description of this condition as a string.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get dataset.
     * @return edu.cmu.pslc.datashop.item.DatasetItem
     */
    public DatasetItem getDataset() {
        return this.dataset;
    }

    /**
     * Set dataset.
     * @param dataset DatasetItem associated with this item
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Add a transaction.
     * @param item to add
     */
    public void addTransaction(TransactionItem item) {
        item.addCondition(this);
    }

    /**
     * remove a transaction.
     * @param item to add
     */
    public void removeTransaction(TransactionItem item) {
        item.removeCondition(this);
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
         buffer.append(objectToString("conditionId", getId()));
         buffer.append(objectToString("conditionName", getConditionName()));
         buffer.append(objectToString("type", getType()));
         buffer.append(objectToString("description", getDescription()));
         buffer.append(objectToStringFK("dataset", getDataset()));
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
        if (obj instanceof ConditionItem) {
            ConditionItem otherItem = (ConditionItem)obj;

            if (!objectEquals(this.getConditionName(), otherItem.getConditionName())) {
                return false;
            }
            if (!objectEquals(this.getType(), otherItem.getType())) {
                return false;
            }
            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getConditionName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Dataset</li>
     *   <li>Type</li>
     *   <li>Name</li>
     *   <li>Description</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        ConditionItem otherItem = (ConditionItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getConditionName(), otherItem.getConditionName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        return value;
    }

}