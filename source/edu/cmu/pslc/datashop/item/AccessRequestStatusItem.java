/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * An AccessRequestStatusItem holds the current project-based access request information for users.
 *
 * @author Mike Komisin
 * @version $Revision: 10884 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-04-08 15:31:45 -0400 (Tue, 08 Apr 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessRequestStatusItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for an AccessRequestStatus. */
    private int id;
    /** Project item associated with this Access Request Status */
    private ProjectItem project;
    /** User item associated with this Access Request Status */
    private UserItem user;
    /** Status string associated with this Access Request Status */
    private String status;
    /** Last activity date associated with this Access Request Status */
    private Date lastActivityDate;
    /** Activity notification flag for PI */
    private Boolean hasPiSeen;
    /** Activity notification flag for DP */
    private Boolean hasDpSeen;
    /** Activity notification flag for Administrator */
    private Boolean hasAdminSeen;
    /** Activity notification flag for requestor */
    private Boolean hasRequestorSeen;
    /** Email status */
    private String emailStatus;
    /** Access Request History associated with this Access Request Status */
    private Set accessRequestHistory;
    /** Access Request enumerated button title - "Request Access". */
    public static final String BUTTON_TITLE_REQUEST = "Request Access";
    /** Access Request enumerated button title - "Request Edit Access". */
    public static final String BUTTON_TITLE_REQUEST_EDIT = "Request Edit Access";
    /** Public user display string - "Public Access". */
    public static final String PUBLIC_ACCESS = "Public Access";

    /** List of Status values. */
    private static final List<String> STATUS_ENUM = new ArrayList<String>();
    /** Access Response status type enumerated field value - "not_reviewed". */
    public static final String ACCESS_RESPONSE_STATUS_NOT_REVIEWED = "not_reviewed";
    /** Access Response status type enumerated field value - "pi_approved". */
    public static final String ACCESS_RESPONSE_STATUS_PI_APPROVED = "pi_approved";
    /** Access Response status type enumerated field value - "dp_approved". */
    public static final String ACCESS_RESPONSE_STATUS_DP_APPROVED = "dp_approved";
    /** Access Response status type enumerated field value - "approved". */
    public static final String ACCESS_RESPONSE_STATUS_APPROVED = "approved";
    /** Access Response status type enumerated field value - "pi_denied". */
    public static final String ACCESS_RESPONSE_STATUS_PI_DENIED = "pi_denied";
    /** Access Response status type enumerated field value - "dp_denied". */
    public static final String ACCESS_RESPONSE_STATUS_DP_DENIED = "dp_denied";
    /** Access Response status type enumerated field value - "denied". */
    public static final String ACCESS_RESPONSE_STATUS_DENIED = "denied";

    static {
        STATUS_ENUM.add(ACCESS_RESPONSE_STATUS_NOT_REVIEWED);
        STATUS_ENUM.add(ACCESS_RESPONSE_STATUS_PI_APPROVED);
        STATUS_ENUM.add(ACCESS_RESPONSE_STATUS_DP_APPROVED);
        STATUS_ENUM.add(ACCESS_RESPONSE_STATUS_APPROVED);
        STATUS_ENUM.add(ACCESS_RESPONSE_STATUS_PI_DENIED);
        STATUS_ENUM.add(ACCESS_RESPONSE_STATUS_DP_DENIED);
        STATUS_ENUM.add(ACCESS_RESPONSE_STATUS_DENIED);
    }

    /** List of EmailStatus values. */
    private static final List<String> EMAIL_STATUS_ENUM = new ArrayList<String>();
    /** Email status 'none'. */
    public static final String EMAIL_STATUS_NONE = "none";
    /** Email status 'first_sent'. */
    public static final String EMAIL_STATUS_FIRST_SENT = "first_sent";
    /** Email status 'second_sent'. */
    public static final String EMAIL_STATUS_SECOND_SENT = "second_sent";
    /** Email status 'third_sent'. */
    public static final String EMAIL_STATUS_THIRD_SENT = "third_sent";
    /** Email status 'denied'. */
    public static final String EMAIL_STATUS_DENIED = "denied";
    /** Email status 'unable_to_send'. */
    public static final String EMAIL_STATUS_UNABLE_TO_SEND = "unable_to_send";

    static {
        EMAIL_STATUS_ENUM.add(EMAIL_STATUS_NONE);
        EMAIL_STATUS_ENUM.add(EMAIL_STATUS_FIRST_SENT);
        EMAIL_STATUS_ENUM.add(EMAIL_STATUS_SECOND_SENT);
        EMAIL_STATUS_ENUM.add(EMAIL_STATUS_THIRD_SENT);
        EMAIL_STATUS_ENUM.add(EMAIL_STATUS_DENIED);
        EMAIL_STATUS_ENUM.add(EMAIL_STATUS_UNABLE_TO_SEND);
    }

    /** Count of state changes associated with Not Reviewed requests. */
    public static final String STATE_COUNT_NOT_REVIEWED = "countNotReviewed";
    /** Count of state changes associated with Recent Activity. */
    public static final String STATE_COUNT_RECENT_ACTIVITY = "countRecentActivity";
    /** Count of state changes associated with Recent Activity. */
    public static final String STATE_COUNT_MY_REQUESTS = "countMyRequestsActivity";
    /** Count of state changes associated with Total Activity. */
    public static final String STATE_COUNT_TOTAL = "countTotalActivity";
    /** Count of days associated with one week in the past. */
    public static final int ONE_WEEK_AGO = -7;
    /** Count of months associated with one month in the past (doesn't have to be accurate). */
    public static final int ONE_MONTH_AGO = -31;

    /** Default constructor. */
    public AccessRequestStatusItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id for a class.
     */
    public AccessRequestStatusItem(int id) {
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
     * Get Access Request history.
     * @return the Set of AccessRequestHistoryItems
     */
    protected Set getAccessRequestHistory() {
        if (this.accessRequestHistory == null) {
            this.accessRequestHistory = new HashSet();
        }
        return this.accessRequestHistory;
    }

    /**
     * Public method to get Access Request history.
     * @return a list instead of a set
     */
    public List getAccessRequestHistoryExternal() {
        List sortedList = new ArrayList(getAccessRequestHistory());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add an Access Request history item.
     * @param item Access Request history item to add
     */
    public void addAccessRequestHistory(AccessRequestHistoryItem item) {
        if (!getAccessRequestHistory().contains(item)) {
            getAccessRequestHistory().add(item);
        }
    }

    /**
     * Remove an Access Request history item.
     * @param item Access Request history item to remove
     */
    public void removeAccessRequestHistory(AccessRequestHistoryItem item) {
        if (getAccessRequestHistory().contains(item)) {
            getAccessRequestHistory().remove(item);
        }
    }

    /**
     * Returns the project.
     * @return the project
     */
    public ProjectItem getProject() {
        return project;
    }

    /**
     * Sets the project.
     * @param project the project
     */
    public void setProject(ProjectItem project) {
        this.project = project;
    }

    /**
     * Returns the user.
     * @return the user
     */
    public UserItem getUser() {
        return user;
    }

    /**
     * Sets the user.
     * @param user the user
     */
    public void setUser(UserItem user) {
        this.user = user;
    }

    /**
     * Returns the status of a request.
     * @return the status of a request
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of a request.
     * @param status the status of a request
     */
    public void setStatus(String status) {
        if (STATUS_ENUM.contains(status)) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid Status value: " + status);
        }
    }

    /**
     * Returns the last activity date.
     * @return the last activity date
     */
    public Date getLastActivityDate() {
        return lastActivityDate;
    }

    /**
     * Sets the last activity date.
     * @param lastActivityDate the last activity date
     */
    public void setLastActivityDate(Date lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    /**
     * Returns the activity notification flag for the PI.
     * @return the activity notification flag for the PI
     */
    public Boolean getHasPiSeen() {
        return hasPiSeen;
    }

    /**
     * Sets the activity notification flag for the PI.
     * @param hasPiSeen the activity notification flag
     */
    public void setHasPiSeen(Boolean hasPiSeen) {
        this.hasPiSeen = hasPiSeen;
    }

    /**
     * Returns the activity notification flag for the Data Provider.
     * @return the activity notification flag for the Data Provider
     */
    public Boolean getHasDpSeen() {
        return hasDpSeen;
    }

    /**
     * Sets the activity notification flag for the Data Provider.
     * @param hasDpSeen the activity notification flag
     */
    public void setHasDpSeen(Boolean hasDpSeen) {
        this.hasDpSeen = hasDpSeen;
    }

    /**
     * Returns the activity notification flag for the Administrator.
     * @return the activity notification flag for the Administrator
     */
    public Boolean getHasAdminSeen() {
        return hasAdminSeen;
    }

    /**
     * Sets the activity notification flag for the Administrator.
     * @param hasAdminSeen the activity notification flag
     */
    public void setHasAdminSeen(Boolean hasAdminSeen) {
        this.hasAdminSeen = hasAdminSeen;
    }

    /**
     * Returns the activity notification flag for the requestor.
     * @return the activity notification flag for the requestor
     */
    public Boolean getHasRequestorSeen() {
        return hasRequestorSeen;
    }

    /**
     * Sets the activity notification flag for the requestor.
     * @param hasRequestorSeen the activity notification flag
     */
    public void setHasRequestorSeen(Boolean hasRequestorSeen) {
        this.hasRequestorSeen = hasRequestorSeen;
    }

    /**
     * Returns the email status of a request.
     * @return the email status of a request
     */
    public String getEmailStatus() {
        return emailStatus;
    }

    /**
     * Sets the email status of a request.
     * @param status the email status of a request
     */
    public void setEmailStatus(String status) {
        if (EMAIL_STATUS_ENUM.contains(status)) {
            this.emailStatus = status;
        } else {
            throw new IllegalArgumentException("Invalid EmailStatus value: " + status);
        }
    }

    /**
     * Set Access Request history items.
     * @param accessRequestHistory Collection of accessRequestHistoryItems this class contains
     */
    public void setAccessRequestHistory(Set accessRequestHistory) {
        this.accessRequestHistory = accessRequestHistory;
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
        buffer.append(objectToStringFK("Project", getProject()));
        buffer.append(objectToStringFK("User", getUser()));
        buffer.append(objectToString("Status", getStatus()));
        buffer.append(objectToString("LastActivityDate", getLastActivityDate()));
        buffer.append(objectToString("HasPiSeen", getHasPiSeen()));
        buffer.append(objectToString("HasDpSeen", getHasDpSeen()));
        buffer.append(objectToString("HasAdminSeen", getHasAdminSeen()));
        buffer.append(objectToString("HasRequestorSeen", getHasRequestorSeen()));
        buffer.append(objectToString("EmailStatus", getEmailStatus()));
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
        if (obj instanceof AccessRequestStatusItem) {
            AccessRequestStatusItem otherItem = (AccessRequestStatusItem)obj;

            if (!objectEqualsFK(this.getProject(), otherItem.getProject())) {
                return false;
            }
            if (!objectEqualsFK(this.getUser(), otherItem.getUser())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!objectEquals(this.getLastActivityDate(), otherItem.getLastActivityDate())) {
                return false;
            }
            if (!objectEquals(this.getHasPiSeen(), otherItem.getHasPiSeen())) {
                return false;
            }
            if (!objectEquals(this.getHasDpSeen(), otherItem.getHasDpSeen())) {
                return false;
            }
            if (!objectEquals(this.getHasAdminSeen(), otherItem.getHasAdminSeen())) {
                return false;
            }
            if (!objectEquals(this.getHasRequestorSeen(), otherItem.getHasRequestorSeen())) {
                return false;
            }
            if (!objectEquals(this.getEmailStatus(), otherItem.getEmailStatus())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProject());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUser());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastActivityDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHasPiSeen());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHasDpSeen());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHasAdminSeen());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHasRequestorSeen());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEmailStatus());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>project</li>
     * <li>user</li>
     * <li>status</li>
     * <li>lastActivityDate</li>
     * <li>hasPiSeen</li>
     * <li>hasDpSeen</li>
     * <li>hasAdminSeen</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        AccessRequestStatusItem otherItem = (AccessRequestStatusItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getProject(), otherItem.getProject());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        value = objectCompareTo(AccessRequestStatusItem.arStringEquivalent(this.getStatus(), false),
                AccessRequestStatusItem.arStringEquivalent(otherItem.getStatus(), false));
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastActivityDate(), otherItem.getLastActivityDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getHasPiSeen(), otherItem.getHasPiSeen());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getHasDpSeen(), otherItem.getHasDpSeen());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getHasAdminSeen(), otherItem.getHasAdminSeen());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getHasRequestorSeen(), otherItem.getHasRequestorSeen());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEmailStatus(), otherItem.getEmailStatus());
        if (value != 0) { return value; }

        return value;
    }

    /**
     * Returns a display string for a given state.
     * @param s the state
     * @param isPastTense whether or not we display a verb in the past tense.
     * @return the display string
     */
    public static String arStringEquivalent(String s, Boolean isPastTense) {
        String displayString = null;
        if (s == null) {
            displayString = "";
        } else if (s.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE)
                && isPastTense) {
            displayString = "Approved";
        } else if (s.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)
                && isPastTense) {
            displayString = "Denied";
        } else if (s.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_REQUEST)
                && isPastTense) {
            displayString = "Requested";
        } else if (s.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
            displayString = "Approved";
        } else if (s.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)) {
            displayString = "Denied";
        } else if (s.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)) {
            //displayString = "Approved by PI";
            displayString = "Pending approval by Data Provider";
        } else if (s.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)) {
            //displayString = "Approved by data provider";
            displayString = "Pending approval by PI";
        } else if (s.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)) {
            displayString = "Denied by PI";
        } else if (s.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
            displayString = "Denied by data provider";
        } else if (s.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED)) {
            displayString = "Not Reviewed";
        } else if (s.equals(AuthorizationItem.LEVEL_EDIT)) {
            displayString = "Edit";
        } else if (s.equals(AuthorizationItem.LEVEL_VIEW)) {
            displayString = "View";
        } else if (s.equals(AuthorizationItem.LEVEL_ADMIN)) {
            displayString = "Admin";
        } else if (s.equals("requestor")) {
            displayString = "Requester";
        } else if (s.equals("pi")) {
            displayString = "PI";
        } else if (s.equals("dp")) {
            displayString = "Data provider";
        } else if (s.equals("")) {
            displayString = "Public"; //"View (Public)";
        } else {
            displayString = s;
        }
        return displayString;
      }
}