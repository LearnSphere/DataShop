/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single password reset request for a given user.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13267 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-05-26 14:29:02 -0400 (Thu, 26 May 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PasswordResetItem extends Item implements java.io.Serializable, Comparable  {

    /** GUID for this Oli_log record. */
    private String guid;
    /** The id of the user that created the password reset. */
    private UserItem user;
    /** The random salt used to generate the guid. */
    private Long salt;
    /** Time the password reset was requested. */
    private Date requestedTime;
    /** Time the password reset expires. */
    private Date expirationTime;
    /** Time the password reset was consumed. */
    private Date consumedTime;

    /** Default constructor. */
    public PasswordResetItem() {
    }

    /**
     * Constructor with GUID.
     * @param guid the id for this table
     */
    public PasswordResetItem(String guid) {
        this.guid = guid;
    }

    /**
     * Returns the GUID.
     * @return String
     */
    public Comparable getId() {
        return this.guid;
    }

    /**
     * Set GUID.
     * @param guid the id
     */
    public void setId(String guid) {
        this.guid = guid;
    }

    /**
     * Get user.
     * @return the user item of the owner of this sample
     */
    public UserItem getUser() {
        return this.user;
    }

    /**
     * Set user.
     * @param user The UserItem for the user making the request.
     */
    public void setUser(UserItem user) {
        this.user = user;
    }

    /**
     * Get the salt.
     * @return Long
     */
    public Long getSalt() { return this.salt; }

    /**
     * Set the salt.
     * @param salt the Long value
     */
    public void setSalt(Long salt) { this.salt = salt; }

    /**
     * Get requestedTime.
     * @return Date
     */
    public Date getRequestedTime() {
        return this.requestedTime;
    }

    /**
     * Set requestedTime.
     * @param requestedTime as Date
     */
    public void setRequestedTime(Date requestedTime) {
        this.requestedTime = requestedTime;
    }

    /**
     * Get expirationTime.
     * @return Date
     */
    public Date getExpirationTime() {
        return this.expirationTime;
    }

    /**
     * Set expirationTime.
     * @param expirationTime as Date
     */
    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Get consumedTime.
     * @return Date
     */
    public Date getConsumedTime() {
        return this.consumedTime;
    }

    /**
     * Set consumedTime.
     * @param consumedTime as Date
     */
    public void setConsumedTime(Date consumedTime) {
        this.consumedTime = consumedTime;
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
        buffer.append(objectToString("GUID", getId()));
        buffer.append(objectToStringFK("User", getUser()));
        buffer.append(objectToString("Salt", getSalt()));
        buffer.append(objectToString("RequestedTime", getRequestedTime()));
        buffer.append(objectToString("ExpirationTime", getExpirationTime()));
        buffer.append(objectToString("ConsumedTime", getConsumedTime()));
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
        if (obj instanceof PasswordResetItem) {
            PasswordResetItem otherItem = (PasswordResetItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getUser(), otherItem.getUser())) {
                return false;
            }
            if (!objectEquals(this.getSalt(), otherItem.getSalt())) {
                return false;
            }
            if (!objectEquals(this.getRequestedTime(), otherItem.getRequestedTime())) {
                return false;
            }
            if (!objectEquals(this.getExpirationTime(), otherItem.getExpirationTime())) {
                return false;
            }
            if (!objectEquals(this.getConsumedTime(), otherItem.getConsumedTime())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUser());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSalt());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getRequestedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getExpirationTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getConsumedTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>GUID</li>
     * <li>User</li>
     * <li>Salt</li>
     * <li>requestedTime</li>
     * <li>expirationTime</li>
     * <li>consumedTime</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        PasswordResetItem otherItem = (PasswordResetItem)obj;

        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSalt(), otherItem.getSalt());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getRequestedTime(), otherItem.getRequestedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getExpirationTime(), otherItem.getExpirationTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getConsumedTime(), otherItem.getConsumedTime());
        if (value != 0) { return value; }

        return value;
    }
}