/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.StringUtils;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Helper class for holding a custom field name/value pair, value is string.
 * @author
 * @version $Revision: 11943 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-02-04 10:10:01 -0500 (Wed, 04 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldNameValueItem extends Item implements java.io.Serializable, Comparable   {

    /** The name we'd like to store. */
    private String name;
    /** The corresponding value for the name. */
    private String value;
    /** The type of value. */
    private String type;
    /** The corresponding bigValue for the name. */
    private String bigValue;
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /**
     * Constructor.
     */
    public CustomFieldNameValueItem() {
    }

    public Comparable getId() {
            // TODO Auto-generated method stub
            return getName();
    }

    /**
     * Constructor.
     * @param name the name for this pair.
     * @param value the value for this pair.
     */
    public CustomFieldNameValueItem(String name, String value) {
        this.name = name;

        setValue(value);

    }

    /**
     * Get the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value.
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value.
     * @param value the value to set
     */
    public void setValue(String value) {
        if (StringUtils.isDate(value)) {
            setType(CustomFieldItem.CF_TYPE_DATE);
        } else if (StringUtils.isNumeric(value)) {
            setType(CustomFieldItem.CF_TYPE_NUMBER);
        } else {
            setType(CustomFieldItem.CF_TYPE_STRING);
        }

        if (value.length() > 255) {
            this.bigValue = value;
            this.value = value.substring(0, 255);
        } else {
            this.value = value;
        }
    }

    /**
     * Get the bigValue.
     * @return the bigValue
     */
    public String getBigValue() {
        return bigValue;
    }

    /**
     * Get the type.
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type.
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToString("Name", getName()));
        buffer.append(objectToString("Value",  getValue()));
        buffer.append(objectToString("BigValue",  getBigValue()));
        buffer.append(objectToString("Type",  getType()));
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
        if (obj instanceof CustomFieldNameValueItem) {
            CustomFieldNameValueItem otherItem = (CustomFieldNameValueItem)obj;

            if (!objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }
            if (!objectEquals(this.getValue(), otherItem.getValue())) {
                return false;
            }
            if (!objectEquals(this.getValue(), otherItem.getBigValue())) {
                return false;
            }
            if (!objectEquals(this.getType(), otherItem.getType())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getValue());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getType());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class
     * <ul>
     * <li>name</li>
     * <li>value</li>
     * <li>bigValue</li>
     * <li>type</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CustomFieldNameValueItem otherItem = (CustomFieldNameValueItem)obj;
        int val = 0;

        val = objectCompareTo(this.getValue(), otherItem.getValue());
        if (val != 0) { return val; }

        val = objectCompareTo(this.getBigValue(), otherItem.getBigValue());
        if (val != 0) { return val; }

        val = objectCompareTo(this.getType(), otherItem.getType());
        if (val != 0) { return val; }

        return val;
    }

    /**
     * Create a CustomFieldItem based on name and value with name populated.
     *
     * @return a custom field item
     */
    public CustomFieldItem makeACustomFieldItem() {
        CustomFieldItem customFieldItem = new CustomFieldItem();
        customFieldItem.setCustomFieldName(name);

        customFieldItem.setDateCreated(new Date());
        return customFieldItem;
    }

    /**
     * Create a CfTxLevelItem with value, id populated
     *
     * @return a CfTxLevelItem
     */
    public CfTxLevelItem makeACfTxLevelItem(CustomFieldItem customFieldItem,
                    TransactionItem txItem) {
    CfTxLevelId cfTxLevelId = new CfTxLevelId(customFieldItem, txItem);
    CfTxLevelItem cfTxLevelItem = null;

    // Create new CfTxLevelItem
    cfTxLevelItem = new CfTxLevelItem();
    cfTxLevelItem.setId(cfTxLevelId);
    cfTxLevelItem.setCustomFieldExternal(customFieldItem);
    cfTxLevelItem.setTransactionExternal(txItem);

    // Set the truncated value (value) and the actual value (bigValue) or null
    // if not more than 255 chars.
    cfTxLevelItem.setValue(value);
    cfTxLevelItem.setBigValue(bigValue);
    cfTxLevelItem.setType(type);

    return cfTxLevelItem;
    }

} // end CustomFieldNameValueItem class
