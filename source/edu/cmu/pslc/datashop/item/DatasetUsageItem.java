/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A collection of usage information for a user on a dataset.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class DatasetUsageItem extends Item implements java.io.Serializable, Comparable  {

    /** Composite Id of a Dataset and User */
    private DatasetUsageId id;
    /** The user of the dataset. */
    private UserItem user = null;
    /** Dataset being used. */
    private DatasetItem dataset = null;
    /** Timestamp of the last time this user accessed this dataset. */
    private Date lastViewedTime;
    /** Total number of times the user has accessed the dataset. */
    private Integer numTimesViewed;
    /** Timestamp of the last time this user exported this dataset. */
    private Date lastExportedTime;
    /** Total number of times the user has exported the dataset. */
    private Integer numTimesExported;

    /** Default constructor. */
    public DatasetUsageItem() {
    }

    /**
     *  Constructor with id.
     *  @param id composite key object.
     */
    public DatasetUsageItem(DatasetUsageId id) {
        this.id = id;
    }

    /**
     * Get the composite key.
     * @return the DatasetUsageId as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the composite key for this item.
     */
    protected void setId(DatasetUsageId id) {
        this.id = id;
    }
    /**
     * Get user.
     * @return the user item
     */

    public UserItem getUser() {
        return this.user;
    }

    /**
     * Set user.
     * Package protected for hibernate only to prevent conflicts with the composite key.
     * @param user The user of the dataset.
     */
    protected void setUser(UserItem user) {
        this.user = user;
    }

    /**
     * Public Set User method to update the composite key as well.
     * @param user Part of the composite key - FK to the user table.
     */
    public void setUserExternal(UserItem user) {
        setUser(user);
        this.id = new DatasetUsageId(this.user, this.dataset);
    }

    /**
     * Get dataset.
     * @return the dataset item
     */
    public DatasetItem getDataset() {
        return this.dataset;
    }

    /**
     * Set dataset.
     * Package protected for hibernate only to prevent conflicts with the composite key.
     * @param dataset Dataset being used.
     */
    protected void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Public Set Dataset method to update the composite key as well.
     * @param dataset Part of the composite key - FK to the dataset table.
     */
    public void setDatasetExternal(DatasetItem dataset) {
        setDataset(dataset);
        this.id = new DatasetUsageId(this.user, this.dataset);
    }

    /**
     * Get lastViewedTime.
     * @return the last view date/time
     */
    public Date getLastViewedTime() {
        return this.lastViewedTime;
    }

    /**
     * Set lastViewedTime.
     * @param lastViewedTime Timestamp of the last time this user accessed this dataset.
     */
    public void setLastViewedTime(Date lastViewedTime) {
        this.lastViewedTime = lastViewedTime;
    }

    /**
     * Get numTimesViewed.
     * @return the number of times this dataset has been viewed
     */
    public Integer getNumTimesViewed() {
        return this.numTimesViewed;
    }

    /**
     * Set numTimesViewed.
     * @param numTimesViewed Total number of times the user has accessed the dataset.
     */
    public void setNumTimesViewed(Integer numTimesViewed) {
        this.numTimesViewed = numTimesViewed;
    }

    /**
     * Get lastExportedTime.
     * @return the last time this dataset was exported
     */
    public Date getLastExportedTime() {
        return this.lastExportedTime;
    }

    /**
     * Set lastExportedTime.
     * @param lastExportedTime Timestamp of the last time this user exported this dataset.
     */
    public void setLastExportedTime(Date lastExportedTime) {
        this.lastExportedTime = lastExportedTime;
    }

    /**
     * Get numTimesExported.
     * @return the number of times this dataset has been exported
     */
    public Integer getNumTimesExported() {
        return this.numTimesExported;
    }

    /**
     * Set numTimesExported.
     * @param numTimesExported Total number of times the user has exported the dataset.
     */
    public void setNumTimesExported(Integer numTimesExported) {
        this.numTimesExported = numTimesExported;
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
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToStringFK("userId", getUser()));
         buffer.append(objectToStringFK("datasetId", getDataset()));
         buffer.append(objectToString("lastViewedTime", getLastViewedTime()));
         buffer.append(objectToString("numTimesViewed", getNumTimesViewed()));
         buffer.append(objectToString("lastExportedTime", getLastExportedTime()));
         buffer.append(objectToString("numTimesExported", getNumTimesExported()));

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
        if (obj instanceof DatasetUsageItem) {
            DatasetUsageItem otherItem = (DatasetUsageItem)obj;

            if (!Item.objectEquals(this.getLastViewedTime(), otherItem.getLastViewedTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getNumTimesViewed(), otherItem.getNumTimesViewed())) {
                return false;
            }
            if (!Item.objectEquals(this.getLastExportedTime(), otherItem.getLastExportedTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getNumTimesExported(), otherItem.getNumTimesExported())) {
                return false;
            }
            if (!Item.objectEquals(this.getId(), otherItem.getId())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastViewedTime());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumTimesViewed());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastExportedTime());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumTimesExported());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>Dataset</li>
      *   <li>User</li>
      *   <li>Last Time Viewed</li>
      *   <li>Number Times Viewed</li>
      *   <li>Last Time Exported</li>
      *   <li>Number Times Exported</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
      */
    public int compareTo(Object obj) {
        DatasetUsageItem otherItem = (DatasetUsageItem)obj;
        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastViewedTime(), otherItem.getLastViewedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumTimesViewed(), otherItem.getNumTimesViewed());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastExportedTime(), otherItem.getLastExportedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumTimesExported(), otherItem.getNumTimesExported());
        if (value != 0) { return value; }

        return 0;
    }
}