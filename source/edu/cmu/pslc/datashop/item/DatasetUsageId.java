/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Composite key for the datasetUsageItem.  Consists of 2 fields that are both
 * foreign keys as well. Note, the fields are immutable.  See Josh Bloch's book,
 * pages 122-123, about defensive copying.
 * <ul>
 *    <li>userId - User Id as a 250 length varchar</li>
 *    <li>datasetId - database generated id of a dataset</li>
 * </ul>
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13753 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-01-17 13:15:03 -0500 (Tue, 17 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetUsageId  implements java.io.Serializable, Comparable  {

    /** the User ID portion of the composite key - FK to the user table. */
    private String userId;
    /** the Dataset ID portion of the composite key - FK to the dataset table. */
    private Integer datasetId;

    /** Default constructor. */
    public DatasetUsageId() {
    }

    /**
     * Full constructor, preferred.
     * immutable
     * @param userItem the user item.
     * @param datasetItem the dataset item.
     */
    public DatasetUsageId(UserItem userItem, DatasetItem datasetItem) {
        if (datasetItem != null) {
            this.datasetId = new Integer(((Integer)datasetItem.getId()).intValue());
        }

        if (userItem != null) {
            this.userId = new String(((String)userItem.getId()));
        }
    }

    /**
     * Full constructor.
     * immutable
     * @param userId the UserID.
     * @param datasetId the datasetId.
     */
    public DatasetUsageId(String userId, Integer datasetId) {
        this.userId = new String(userId);
        this.datasetId = new Integer(datasetId.intValue());
    }

    /**
     * Get the user id.
     * @return the user id.
     */
    public String getUserId() {
        return new String(this.userId);
    }

    /**
     * Set the user Id.
     * @param userId a FK userId.
     */
    protected void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the dataset id.
     * @return the dataset id.
     */
    protected Integer getDatasetId() {
        return new Integer(datasetId.intValue());
    }

    /**
     * Set the dataset id.
     * @param datasetId the dataset id.
     */
    protected void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }


    /**
     * Returns object name, hash code and the attributes.
     * @return String
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append("userId='").append(userId).append("' ");
         buffer.append("datasetId='").append(datasetId).append("' ");
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
        if (obj instanceof DatasetUsageId) {
            DatasetUsageId otherItem = (DatasetUsageId)obj;

            if (!this.userId.equals(otherItem.userId)) {
                 return false;
            }
            if (!this.datasetId.equals(otherItem.datasetId)) {
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
        int hash = UtilConstants.HASH_INITIAL;
        if (userId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + userId.hashCode();
        }
        if (datasetId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + datasetId.hashCode();
        }
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>datasetId</li>
     *   <li>userId</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetUsageId otherItem = (DatasetUsageId)obj;
        int value;

        if ((this.datasetId != null) && (otherItem.datasetId != null)) {
            value = this.datasetId.compareTo(otherItem.datasetId);
            if (value != 0) { return value; }
        } else if (this.datasetId != null) {
            return 1;
        } else if (otherItem.datasetId != null) {
            return -1;
        }

        if ((this.userId != null) && (otherItem.userId != null)) {
            value = this.userId.compareTo(otherItem.userId);
            if (value != 0) { return value; }
        } else if (this.userId != null) {
            return 1;
        } else if (otherItem.userId != null) {
            return -1;
        }
        return 0;
    }
}