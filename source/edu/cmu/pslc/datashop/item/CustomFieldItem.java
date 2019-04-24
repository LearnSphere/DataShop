/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.datashop.servlet.customfield.CustomFieldDto;
import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the custom field table.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11939 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-02-01 11:24:14 -0500 (Sun, 01 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class CustomFieldItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this custom field. */
    private Long id;
    /** Name of the custom field. */
    private String customFieldName;
    /** Description of this custom field. */
    private String description;
    /** Level for this custom field. */
    private String level;
    /** Dataset associated with this custom field. */
    private DatasetItem dataset;
    /** Owner/Creator of this custom field. */
    private UserItem owner;
    /** Date the Custom Field was created/added. */
    private Date dateCreated;
    /** User that last modified this Custom Field. */
    private UserItem updatedBy;
    /** Date the Custom Field was last modified. */
    private Date lastUpdated;

    /** CF Type: number. */
    public static final String CF_TYPE_NUMBER = "number";
    /** CF Type: string. */
    public static final String CF_TYPE_STRING = "string";
    /** CF Type: date. */
    public static final String CF_TYPE_DATE = "date";

    /** CF level: transaction. */
    public static final String CF_LEVEL_TRANSACTION = "transaction";

    /** Default constructor. */
    public CustomFieldItem() {
    }

    /**
     * Constructor with id.
     * @param customFieldId Database generated unique Id for this custom field.
     */
    public CustomFieldItem(Long customFieldId) {
        this.id = customFieldId;
    }

    /**
     * Get customFieldId.
     * @return Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set customFieldId.
     * @param customFieldId Database generated unique Id for this custom field.
     */
    public void setId(Long customFieldId) {
        this.id = customFieldId;
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
     * Get customFieldName.
     * @return java.lang.String
     */
    public String getCustomFieldName() {
        return this.customFieldName;
    }

    /**
     * Set customFieldName.
     * @param customFieldName Name of the custom field.
     */
    public void setCustomFieldName(String customFieldName) {
        this.customFieldName = customFieldName;
    }

    /**
     * Get description.
     * @return java.lang.String
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description Description of this custom field.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get owner.
     * @return a UserItem object
     */

    public UserItem getOwner() {
        return this.owner;
    }

    /**
     * Set owner.
     * @param owner Owner/Creator of this custom field.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /**
     * Get the date this item was created.
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Set the date this item was last created.
     * @param dateCreated the Date this item was created
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Get user that last updated this item.
     * @return a UserItem object
     */

    public UserItem getUpdatedBy() {
        return this.updatedBy;
    }

    /**
     * Set user that last updated this item.
     * @param updatedBy User that updated this custom field.
     */
    public void setUpdatedBy(UserItem updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Get the date this item was last updated.
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set the date this item was last updated.
     * @param lastUpdated the Date this item was updated
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Get level.
     * @return java.lang.String
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * Set level.
     * @param level Level for this custom field.
     */
    public void setLevel(String level) {
        if (isValidCustomFieldLevel(level)) {
            this.level = level;
        } else {
            throw new LogException("Custom field level " + level + " is not acceptable.");
        }
    }

    /**
     * Check if a level is valid.
     * @param level Type for this custom field.
     * @return true if level is transaction
     */
    public static boolean isValidCustomFieldLevel(String level) {
        return level.equalsIgnoreCase(CF_LEVEL_TRANSACTION);
    }

    /**
     * Whether this custom field is owned by user.
     * @param user the user
     *
     * @return whether this custom field is owned by the user
     */
    public boolean isOwnedByUser(UserItem user) {
            if (getOwner() == null) {
                    return user.getAdminFlag();
            } else {
                    return user.equals(getOwner());
            }
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
        buffer.append(objectToString("CustomFieldName",  getCustomFieldName()));
        buffer.append(objectToString("Description", getDescription()));
        buffer.append(objectToString("Level", getLevel()));
        buffer.append(objectToString("Owner", getOwner()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
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
        if (obj instanceof CustomFieldItem) {
            CustomFieldItem otherItem = (CustomFieldItem)obj;

            if (!objectEquals(this.getCustomFieldName(), otherItem.getCustomFieldName())) {
                return false;
            }
            if (!objectEquals(this.getLevel(), otherItem.getLevel())) {
                return false;
            }
            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
                    return false;
                }
            if (!objectEquals(this.getOwner(), otherItem.getOwner())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCustomFieldName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLevel());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset</li>
     * <li>name</li>
     * <li>level</li>
     * <li>owner</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CustomFieldItem otherItem = (CustomFieldItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCustomFieldName(), otherItem.getCustomFieldName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLevel(), otherItem.getLevel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        return value;
    }

}