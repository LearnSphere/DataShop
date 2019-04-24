/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * An object that represents an item in the dataset-user-TOU map.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetUserTermsOfUseMapItem extends Item
    implements java.io.Serializable, Comparable   {

    /** The class the serves as the primary key (composite-key) for this item. */
    private DatasetUserTermsOfUseMapId id;

    /** The terms of use for this item. */
    private TermsOfUseItem termsOfUse;
    /** The user for this item. */
    private UserItem user;
    /** The dataset for this item. */
    private DatasetItem dataset;
    /** The terms of use version for this item. */
    private TermsOfUseVersionItem termsOfUseVersion;
    /** The date for this item. */
    private Date date;

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the TransactionSkilleventId.
     * @param id the id to set
     */
    public void setId(DatasetUserTermsOfUseMapId id) {
        this.id = id;
    }

    /**
     * Get the termsOfUse.
     * @return the termsOfUse
     */
    public TermsOfUseItem getTermsOfUse() {
        return termsOfUse;
    }

    /**
     * Set the termsOfUse.
     * @param termsOfUse the termsOfUse to set
     */
    protected void setTermsOfUse(TermsOfUseItem termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

    /**
     * Public Set termsOfUseVersion method to update the composite key as well.
     * @param termsOfUse Part of the composite key - FK to the terms of use table.
     */
    public void setTermsOfUseExternal(TermsOfUseItem termsOfUse) {
        setTermsOfUse(termsOfUse);
        this.id = new DatasetUserTermsOfUseMapId(this.termsOfUse, this.user, this.dataset);
    }

    /**
     * Get the user.
     * @return the user
     */
    public UserItem getUser() {
        return user;
    }

    /**
     * Set the user.
     * @param user the user to set
     */
    protected void setUser(UserItem user) {
        this.user = user;
    }

    /**
     * Public Set user method to update the composite key as well.
     * @param user Part of the composite key - FK to the user table.
     */
    public void setUserExternal(UserItem user) {
        setUser(user);
        this.id = new DatasetUserTermsOfUseMapId(this.termsOfUse, this.user, this.dataset);
    }

    /**
     * Get the dataset.
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set the dataset.
     * @param dataset the dataset to set
     */
    protected void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Public set the dataset method which also updates the composite key.
     * @param dataset the dataset to set
     */
    public void setDatasetExternal(DatasetItem dataset) {
        setDataset(dataset);
        this.id = new DatasetUserTermsOfUseMapId(this.termsOfUse, this.user, this.dataset);
    }

    /**
     * Get the termsOfUseVersion.
     * @return the termsOfUseVersion
     */
    public TermsOfUseVersionItem getTermsOfUseVersion() {
        return termsOfUseVersion;
    }

    /**
     * Set the termsOfUseVersion.
     * @param termsOfUseVersion the termsOfUseVersion to set
     */
    public void setTermsOfUseVersion(TermsOfUseVersionItem termsOfUseVersion) {
        this.termsOfUseVersion = termsOfUseVersion;
    }

    /**
     * Get the date.
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the date.
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
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
        buffer.append(objectToStringFK("TermsOfUse", this.getTermsOfUse()));
        buffer.append(objectToStringFK("User", this.getUser()));
        buffer.append(objectToStringFK("Dataset", this.getDataset()));
        buffer.append(objectToStringFK("TermsOfUseVersion", this.getTermsOfUseVersion()));
        buffer.append(objectToString("Date", this.getDate()));
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
        if (obj instanceof DatasetUserTermsOfUseMapItem) {
            DatasetUserTermsOfUseMapItem otherItem = (DatasetUserTermsOfUseMapItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEqualsFK(this.getTermsOfUseVersion(), otherItem.getTermsOfUseVersion())) {
                return false;
            }
            if (!objectEquals(this.getDate(), otherItem.getDate())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getTermsOfUseVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getDate());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>termsOfUse</li>
     * <li>user</li>
     * <li>dataset</li>
     * <li>termsOfUseVersion</li>
     * <li>date</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetUserTermsOfUseMapItem otherItem = (DatasetUserTermsOfUseMapItem)obj;

        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getTermsOfUseVersion(), otherItem.getTermsOfUseVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDate(), otherItem.getDate());
        if (value != 0) { return value; }

        return value;
    }
}
