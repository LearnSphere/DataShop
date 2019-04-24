/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The CfTxLevel maps a transaction to a custom field and contains the
 * value of the custom field.
 *
 * @author
 * @version $Revision: 11939 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-02-01 11:24:14 -0500 (Sun, 01 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class CfTxLevelItem extends Item implements java.io.Serializable, Comparable  {

    /** The composite key. */
    private CfTxLevelId id;
    /** Part of the composite PK - FK of the custom field Id. */
    private CustomFieldItem customField;
    /** Part of the composite PK - FK of the transaction Id. */
    private TransactionItem transaction;
    /** The type of value for this custom field (date, number, string). */
    private String type;
    /** The value for a this custom field (truncated to 255 chars total). */
    private String value;
    /** The big value for a this custom field (not truncated or null if length(value) <= 255 chars. */
    private String bigValue;
    /** Flag indicating whether this value is from logging or web service */
    private Boolean loggingFlag;

    /** CF Type: number. */
    public static final String CF_TYPE_NUMBER = "number";
    /** CF Type: string. */
    public static final String CF_TYPE_STRING = "string";
    /** CF Type: date. */
    public static final String CF_TYPE_DATE = "date";

    /** Default constructor. */
    public CfTxLevelItem() {
    }

    /**
     *  Constructor with id.
     *  @param id the composite key
     */
    public CfTxLevelItem(CfTxLevelId id) {
        this.id = id;
    }

    /**
     * Returns the id.
     * @return CfTxLevelId
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set composite key.
     * @param id the composite key
     */
    protected void setId(CfTxLevelId id) {
        this.id = id;
    }

    /**
     * Get custom field.
     * @return edu.cmu.pslc.datashop.item.CustomFieldItem
     */
    public CustomFieldItem getCustomField() {
        return this.customField;
    }

    /**
     * Set custom field.
     * Package protected for hibernate only to prevent conflicts with the composite key
     * @param custom field Part of the composite PK - FK of the custom field Id.
     */
    protected void setCustomField(CustomFieldItem customField) {
        this.customField = customField;
    }

    /**
     * Public Set custom field method to update the composite key as well.
     * @param custom field Part of the composite key - FK to the custom field table.
     */
    public void setCustomFieldExternal(CustomFieldItem customField) {
        setCustomField(customField);
        this.id = new CfTxLevelId(this.customField, this.transaction);
    }
    /**
     * Get transaction.
     * @return edu.cmu.pslc.datashop.item.TransactionItem
     */
    public TransactionItem getTransaction() {
        return this.transaction;
    }

    /**
     * Set transaction.
     * Package protected for hibernate only to prevent conflicts with the composite key
     * @param transaction Part of the composite PK - FK of the transaction Id.
     */
    protected void setTransaction(TransactionItem transaction) {
        this.transaction = transaction;
    }

    /**
     * Public Set transaction method to update the composite key as well.
     * @param transaction Part of the composite key - FK to the transaction table.
     */
    public void setTransactionExternal(TransactionItem transaction) {
        setTransaction(transaction);
        this.id = new CfTxLevelId(this.customField, this.transaction);
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
     * @param type Type for this custom field.
     */
    public void setType(String type) {
        if (isValidCustomFieldType(type)) {
            this.type = type;
        } else {
            throw new LogException("Custom field type " + type + " is not acceptable.");
        }
    }


    /**
     * Check if a type is valid.
     * @param type Type for this custom field.
     * @return true if type is number, date, string or bi
     */
    public static boolean isValidCustomFieldType(String type) {
        if (type.equalsIgnoreCase(CF_TYPE_NUMBER)
                || type.equalsIgnoreCase(CF_TYPE_DATE)
                || type.equalsIgnoreCase(CF_TYPE_STRING)) {
            return true;
        }
        return false;
    }

    /**
     * Get value.
     * @return String
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set value.
     * @param value The value for a this transaction for this custom field.
     */
    public void setValue(String value) {
        if (value != null && value.length() > 255) {
            this.bigValue = value;
            this.value = value.substring(0, 255);

        } else {
            this.value = value;
        }
    }

    /**
     * Get bigValue.
     * @return String
     */
    public String getBigValue() {
        return this.bigValue;
    }

    /**
     * Set bigValue.
     * @param bigValue The big value for a this transaction for this custom field.
     */
    public void setBigValue(String bigValue) {
        this.bigValue = bigValue;
    }

    /**
     * Get loggingFlag.
     * @return Boolean
     */
    public Boolean getLoggingFlag() {
        return this.loggingFlag;
    }

    /**
     * Set loggingFlag.
     * @param loggingFlag Flag indicating if this custom field value is from logging
     */
    public void setLoggingFlag(Boolean loggingFlag) {
        this.loggingFlag = loggingFlag;
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
         buffer.append(objectToString("Type", getType()));
         buffer.append(objectToString("Value", getValue()));
         buffer.append(objectToString("Logging Flag", getLoggingFlag()));
         buffer.append(objectToStringFK("Custom Field Item Id", getCustomField()));
         buffer.append(objectToStringFK("Transaction Item Id", getTransaction()));
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
        if (obj instanceof CfTxLevelItem) {
            CfTxLevelItem otherItem = (CfTxLevelItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }

            if (!objectEquals(this.getType(), otherItem.getType())) {
                return false;
            }

            if (!objectEquals(this.getValue(), otherItem.getValue())) {
                return false;
            }

            if (!objectEquals(this.getLoggingFlag(), otherItem.getLoggingFlag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getValue());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLoggingFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>custom field</li>
     * <li>transaction</li>
     * <li>type</li>
     * <li>value</li>
     * <li>loggingFlag</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CfTxLevelItem otherItem = (CfTxLevelItem)obj;
        int returnValue = 0;

        returnValue = objectCompareToFK(this.getCustomField(), otherItem.getCustomField());
        if (returnValue != 0) { return returnValue; }

        returnValue = objectCompareToFK(this.getTransaction(), otherItem.getTransaction());
        if (returnValue != 0) { return returnValue; }

        returnValue = objectCompareTo(this.getType(), otherItem.getType());
        if (returnValue != 0) { return returnValue; }

        returnValue = objectCompareTo(this.getValue(), otherItem.getValue());
        if (returnValue != 0) { return returnValue; }

        returnValue = objectCompareTo(this.getLoggingFlag(), otherItem.getLoggingFlag());
        if (returnValue != 0) { return returnValue; }

        return returnValue;
    }
}