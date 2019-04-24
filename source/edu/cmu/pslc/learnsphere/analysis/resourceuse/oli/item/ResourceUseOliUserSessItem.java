/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * OLI resource use user sess data.
 *
 * @author Hui Cheng
 * @version $Revision: 12889 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:56:51 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ResourceUseOliUserSessItem extends Item implements java.io.Serializable, Comparable  {
    /** Database generated unique id. */
    private Long id;
    /** userSess. This should connect student with resource_use_transaction*/
    private String userSess;
    /** Anonymized student id for this transaction. */
    private String anonStudentId;
    /** User_sess associated to this*/
    private ResourceUseOliUserSessFileItem resourceUseOliUserSessFileItem;

    /** Default constructor. */
    public ResourceUseOliUserSessItem() {
    }

    /**
     * Constructor with id.
     * @param resourceUseUserSessId Database generated unique id
     */
    public ResourceUseOliUserSessItem(Long resourceUseOliUserSessId) {
        this.id = resourceUseOliUserSessId;
    }

    /**
     * Get resourceUseOliUserSessId.
     * @return Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set resourceUseOliUserSessId.
     * @param resourceUseOliUserSessId Database generated unique id
     */
    public void setId(Long resourceUseOliUserSessId) {
        this.id = resourceUseOliUserSessId;
    }

    /**
     * Get the userSess
     * @return the userSess
     */
    public String getUserSess() {
        return userSess;
    }

    /**
     * Set the userSess
     * @param userSess the user sess
     */
    public void setUserSess(String userSess) {
        this.userSess = userSess;
    }

    /**
     * Get the anon student id.
     * @return String the anon student id
     */
    public String getAnonStudentId() {
        return anonStudentId;
    }

    /**
     * Set the anon student for this userSess.
     * @param student the anon student id
     */
    public void setAnonStudentId(String anonStudentId) {
        this.anonStudentId = anonStudentId;
    }
    
    /**
     * Get the ResourceUseOliUserSessFileItem that is associated to this user sess.
     * @return ResourceUseOliUserSessFileItem the resourceUseOliUserSessFileItem
     */
    public ResourceUseOliUserSessFileItem getResourceUseOliUserSessFileItem() {
        return resourceUseOliUserSessFileItem;
    }

    /**
     * Set the ResourceUseOliUserSessFileItem for this userSess.
     * @param ResourceUseOliUserSessFileItem the resource use user-sess file item
     */
    public void setResourceUseOliUserSessFileItem(ResourceUseOliUserSessFileItem resourceUseOliUserSessFileItem) {
        this.resourceUseOliUserSessFileItem = resourceUseOliUserSessFileItem;
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
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToString("UserSess",  getUserSess()));
        buffer.append(objectToString("AnonStudentId",  getAnonStudentId()));
        buffer.append(objectToStringFK("ResourceUseOliUserSessFileItem", getResourceUseOliUserSessFileItem()));
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
        if (obj instanceof ResourceUseOliUserSessItem) {
            ResourceUseOliUserSessItem otherItem = (ResourceUseOliUserSessItem)obj;

            if (!objectEquals(this.getUserSess(), otherItem.getUserSess())) {
                return false;
            }
            if (!objectEqualsFK(this.getAnonStudentId(), otherItem.getAnonStudentId())) {
                return false;
            }
            if (!objectEqualsFK(this.getResourceUseOliUserSessFileItem(), otherItem.getResourceUseOliUserSessFileItem())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserSess());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAnonStudentId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getResourceUseOliUserSessFileItem());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResourceUseOliUserSessItem otherItem = (ResourceUseOliUserSessItem)obj;
        int value = 0;

        value = objectCompareTo(this.getUserSess(), otherItem.getUserSess());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAnonStudentId(), otherItem.getAnonStudentId());
        if (value != 0) { return value; }
        
        value = objectCompareToFK(this.getResourceUseOliUserSessFileItem(), otherItem.getResourceUseOliUserSessFileItem());
        if (value != 0) { return value; }

        return value;
    }
}