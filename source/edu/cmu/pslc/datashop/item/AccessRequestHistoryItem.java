/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * An AccessRequestStatusItem holds the current project-based access request information for users.
 *
 * @author Mike Komisin
 * @version $Revision: 8307 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-12-05 11:10:51 -0500 (Wed, 05 Dec 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessRequestHistoryItem extends Item implements java.io.Serializable, Comparable  {

    /** The id of this AccessRequestHistoryItem. */
    private int id;
    /** The AccessRequestStatusItem associated with this project. */
    private AccessRequestStatusItem accessRequestStatusItem;
    /** User item associated with this Access Request History. */
    private UserItem user;
    /** Role associated with the user. */
    private String role;
    /** Action associated with this history item. */
    private String action;
    /** Level associated with this history item. */
    private String level;
    /** Date the history item was created. */
    private Date date;
    /** Reason associated with the history item. */
    private String reason;
    /** Active flag associated with the history item. */
    private Boolean isActive;
    /** Flag indicating whether the user responding wants the reason included
     *  in the email sent to requester. */
    private Boolean shareReasonFlag;

    /** Access Request role type enumerated field value - "requestor". */
    public static final String ACCESS_REQUEST_ROLE_REQUESTOR = "requestor";
    /** Access Request role type enumerated field value - "pi". */
    public static final String ACCESS_REQUEST_ROLE_PI = "pi";
    /** Access Request role type enumerated field value - "dp". */
    public static final String ACCESS_REQUEST_ROLE_DP = "dp";
    /** Access Request role type enumerated field value - "admin". */
    public static final String ACCESS_REQUEST_ROLE_ADMIN = "admin";

    /** Access Request action type enumerated field value - "request". */
    public static final String ACCESS_REQUEST_ACTION_REQUEST = "request";
    /** Access Request action type enumerated field value - "approve". */
    public static final String ACCESS_REQUEST_ACTION_APPROVE = "approve";
    /** Access Request action type enumerated field value - "deny". */
    public static final String ACCESS_REQUEST_ACTION_DENY = "deny";

    /** Default constructor. */
    public AccessRequestHistoryItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id for a class.
     */
    public AccessRequestHistoryItem(int id) {
        this.id = id;
    }

    /**
     * Returns the id as a comparable.
     * @return the int id as a comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for a class.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the AccessRequestStatusItem associated with this history item.
     * @return the AccessRequestStatusItem associated with this history item.
     */
    public AccessRequestStatusItem getAccessRequestStatusItem() {
        return accessRequestStatusItem;
    }

    /**
     * Sets the AccessRequestStatusItem associated with this history item.
     * @param accessRequestStatusItem the AccessRequestStatusItem associated with this history item.
     */
    public void setAccessRequestStatusItem(
            AccessRequestStatusItem accessRequestStatusItem) {
        this.accessRequestStatusItem = accessRequestStatusItem;
    }

    /**
     * Returns the user item.
     * @return the user item
     */
    public UserItem getUser() {
        return user;
    }

    /**
     * Sets the user item.
     * @param user the user item
     */
    public void setUser(UserItem user) {
        this.user = user;
    }

    /**
     * Returns the role.
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role.
     * @param role the role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the action.
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action.
     * @param action the action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns the level.
     * @return the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the level.
     * @param level the level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Returns the date associated with this history item.
     * @return the date associated with this history item
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date associated with this history item.
     * @param date the date associated with this history item
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns the reason associated with this history item.
     * @return the reason associated with this history item
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason associated with this history item.
     * @param reason the reason associated with this history item
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the active flag associated with this history item.
     * @return the active flag associated with this history item
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * Sets the active flag associated with this history item.
     * @param isActive the active flag associated with this history item
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Gets the shareReasonFlag.
     * @return the shareReasonFlag
     */
    public Boolean getShareReasonFlag() {
        return shareReasonFlag;
    }

    /**
     * Sets the shareReasonFlag.
     * @param shareReasonFlag the shareReasonFlag to set
     */
    public void setShareReasonFlag(Boolean shareReasonFlag) {
        this.shareReasonFlag = shareReasonFlag;
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
        buffer.append(objectToStringFK("AccessRequestStatusItem", getAccessRequestStatusItem()));
        buffer.append(objectToStringFK("User", getUser()));
        buffer.append(objectToString("Role", getRole()));
        buffer.append(objectToString("Action", getAction()));
        buffer.append(objectToString("Level", getLevel()));
        buffer.append(objectToString("Date", getDate()));
        buffer.append(objectToString("Reason", getReason()));
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
        if (obj instanceof AccessRequestHistoryItem) {
            AccessRequestHistoryItem otherItem = (AccessRequestHistoryItem)obj;

            if (!objectEqualsFK(this.getAccessRequestStatusItem(),
                    otherItem.getAccessRequestStatusItem())) {
                return false;
            }
            if (!objectEqualsFK(this.getUser(), otherItem.getUser())) {
                return false;
            }
            if (!objectEquals(this.getRole(), otherItem.getRole())) {
                return false;
            }
            if (!objectEquals(this.getAction(), otherItem.getAction())) {
                return false;
            }
            if (!objectEquals(this.getLevel(), otherItem.getLevel())) {
                return false;
            }
            if (!objectEquals(this.getDate(), otherItem.getDate())) {
                return false;
            }
            if (!objectEquals(this.getReason(), otherItem.getReason())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getAccessRequestStatusItem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUser());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getRole());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLevel());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getReason());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>accessRequestStatusItem</li>
     * <li>user</li>
     * <li>role</li>
     * <li>action</li>
     * <li>level</li>
     * <li>date</li>
     * <li>reason</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AccessRequestHistoryItem otherItem = (AccessRequestHistoryItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getAccessRequestStatusItem(),
                otherItem.getAccessRequestStatusItem());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getRole(), otherItem.getRole());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLevel(), otherItem.getLevel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDate(), otherItem.getDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getReason(), otherItem.getReason());
        if (value != 0) { return value; }

        return value;
    }
}