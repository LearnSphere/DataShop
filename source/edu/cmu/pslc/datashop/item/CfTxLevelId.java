/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This is the CfTxLevel Id type for the CfTxLevelItem.
 *
 * @author
 * @version $Revision: 11939 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-02-01 11:24:14 -0500 (Sun, 01 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CfTxLevelId implements java.io.Serializable, Comparable  {

    /** Part of the composite PK - FK of the custom field Id. */
    private Long customFieldId;
    /** Part of the composite PK - FK of the transaction Id. */
    private Long transactionId;

    /** Default constructor. */
    public CfTxLevelId() {
    }

    /**
     * Full constructor, preferred.
     * immutable
     * @param customFieldItem the CustomFieldItem.
     * @param transactionItem the TransactionItem.
     */
    public CfTxLevelId(CustomFieldItem customFieldItem, TransactionItem transactionItem) {
        if (customFieldItem != null) {
            this.customFieldId = Long.valueOf(((Long)customFieldItem.getId()).longValue());
        }

        if (transactionItem != null) {
            this.transactionId = Long.valueOf(((Long)transactionItem.getId()).longValue());
        }
    }

    /**
     * Full constructor.
     * immutable
     * @param customFieldId the id of the CustomFieldItem.
     * @param transactionId the id of the TransactionItem.
     */
    public CfTxLevelId(Long customFieldId, Long transactionId) {
        this.customFieldId = Long.valueOf(customFieldId.longValue());
        this.transactionId = Long.valueOf(transactionId.longValue());
    }

    /**
     * Get customFieldId.
     * @return Long
     */
    protected Long getCustomFieldId() {
        return Long.valueOf(this.customFieldId.longValue());
    }

    /**
     * Set customFieldId.
     * @param customFieldId Part of the composite PK - FK of the customField Id.
     */
    protected void setCustomFieldId(Long customFieldId) {
        this.customFieldId = customFieldId;
    }

    /**
     * Get transactionId.
     * @return Long
     */
    protected Long getTransactionId() {
        return Long.valueOf(this.transactionId.longValue());
    }

    /**
     * Set transactionId.
     * @param transactionId Part of the composite PK - FK of the transaction Id.
     */
    protected void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
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
         buffer.append("customFieldId='").append(customFieldId).append("' ");
         buffer.append("transactionId='").append(transactionId).append("' ");
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
        if (obj instanceof CfTxLevelId) {
            CfTxLevelId otherItem = (CfTxLevelId)obj;

            if (!this.customFieldId.equals(otherItem.customFieldId)) {
                 return false;
            }
            if (!this.transactionId.equals(otherItem.transactionId)) {
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
        if (customFieldId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + customFieldId.hashCode();
        }
        if (transactionId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + transactionId.hashCode();
        }
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>custom field id</li>
     * <li>transaction id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CfTxLevelId otherItem = (CfTxLevelId)obj;
        int value;

        if ((this.customFieldId != null) && (otherItem.customFieldId != null)) {
            value = this.customFieldId.compareTo(otherItem.customFieldId);
            if (value != 0) { return value; }
        } else if (this.customFieldId != null) {
            return 1;
        } else if (otherItem.customFieldId != null) {
            return -1;
        }

        if ((this.transactionId != null) && (otherItem.transactionId != null)) {
            value = this.transactionId.compareTo(otherItem.transactionId);
            if (value != 0) { return value; }
        } else if (this.transactionId != null) {
            return 1;
        } else if (otherItem.transactionId != null) {
            return -1;
        }
        return 0;
    }
}