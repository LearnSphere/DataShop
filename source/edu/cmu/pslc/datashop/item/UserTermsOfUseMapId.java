/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the user_terms_of_use relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Shanwen Yu
 * @version $Revision: 7264 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-11-11 11:03:56 -0500 (Fri, 11 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserTermsOfUseMapId implements java.io.Serializable, Comparable   {

    /** Terms of Use item associated with this record. */
    private Integer termsOfUseId;
    /** User item associated with this record. */
    private String userId;
    /**
     * Constructor!
     */
    public UserTermsOfUseMapId() { };

    /**
     * Full constructor.
     * @param termsOfUse the terms of use item for this composite key.
     * @param user the user for this composite key.
     */
    public UserTermsOfUseMapId(TermsOfUseItem termsOfUse, UserItem user) {
        if (termsOfUse != null) {
            this.termsOfUseId = new Integer((Integer)termsOfUse.getId());
        }
        if (user != null) {
            this.userId = new String((String)user.getId());
        }
    }

    /**
     * Full constructor.
     * @param termsOfUseId the terms of use id for this object.
     * @param userId the userId for this object.
     */
    public UserTermsOfUseMapId(Integer termsOfUseId, String userId) {
        this.termsOfUseId = new Integer(termsOfUseId);
        this.userId = new String(userId);
    }

    /**
     * Get the termsOfUseId.
     * @return the termsOfUseId
     */
    public Integer getTermsOfUseId() {
        return new Integer(termsOfUseId);
    }

    /**
     * Set the termsOfUseId.
     * @param termsOfUseId the termsOfUseId to set
     */
    public void setTermsOfUseId(Integer termsOfUseId) {
        this.termsOfUseId = termsOfUseId;
    }

    /**
     * Get the userId.
     * @return the userId.
     */
    public String getUserId() {
        return new String(userId);
    }

    /**
     * Set the userId.
     * @param userId the userId to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
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
         buffer.append("termsOfUseId").append("='").append(termsOfUseId).append("' ");
         buffer.append("userId").append("='").append(userId).append("' ");
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            UserTermsOfUseMapId otherItem = (UserTermsOfUseMapId)obj;

            if (!this.termsOfUseId.equals(otherItem.getTermsOfUseId())) {
                return false;
            }
            if (!this.userId.equals(otherItem.getUserId())) {
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
        hash = hash * UtilConstants.HASH_PRIME
            + (termsOfUseId != null ? termsOfUseId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>terms of use id</li>
     * <li>user id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        UserTermsOfUseMapId otherItem = (UserTermsOfUseMapId)obj;
        if (this.termsOfUseId.compareTo(otherItem.termsOfUseId) != 0) {
            return this.termsOfUseId.compareTo(otherItem.termsOfUseId);
        }
        if (this.userId.compareTo(otherItem.userId) != 0) {
            return this.userId.compareTo(otherItem.userId);
        }
        return 0;
    }
}
