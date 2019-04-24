/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single OLI discussion record.
 *
 * @author Alida Skogsholm
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliDiscussionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this item. */
    private Long oliDiscussionId;
    /** The actual user ID for this item as a string. */
    private String userId;
    /** The session ID for this item as a string.  */
    private String sessionTag;
    /** The time stamp of this discussion post.  */
    private Date discussionTime;
    /** Time zone the timestamp was taken in. */
    private String timeZone;
    /** The GUID from OLI's log action table. */
    private String actionGuid;
    /** The admit code of the course which this feedback data is associated with. */
    private String admitCode;
    /** The OLI GUID for the post itself. */
    private String postGuid;
    /** The OLI GUID for original post that this post is a reply to. */
    private String threadGuid;
    /** The star rating the instructor gave this post. */
    private Integer stars;
    /** A flag indicating whether to hide this post. */
    private Boolean hiddenFlag;
    /** The date/time this post was accepted. */
    private Date acceptedDate;
    /** The subject of this post. */
    private String subject;
    /** The body of this post. */
    private String body;

    /** Default constructor. */
    public OliDiscussionItem() {
        this.hiddenFlag = Boolean.FALSE;
    }

    /**
     * Constructor with id.
     * @param id the database id
     */
    public OliDiscussionItem(Long id) {
        this.oliDiscussionId = id;
        this.hiddenFlag = Boolean.FALSE;
    }

    /**
     * Returns the database id.
     * @return Long
     */
    public Comparable getId() {
        return this.oliDiscussionId;
    }

    /**
     * Set the id.
     * @param id the database id
     */
    public void setId(Long id) {
        this.oliDiscussionId = id;
    }

    /**
     * Get userId.
     * @return java.lang.String
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Set userId.
     * @param userId the identifiable user login
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    /**
     * Get sessionTag.
     * @return java.lang.String
     */

    public String getSessionTag() {
        return this.sessionTag;
    }

    /**
     * Set sessionTag.
     * @param sessionTag Last name of the user as a string
     */
    public void setSessionTag(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    /**
     * Get discussionTime.
     * @return java.util.Date
     */
    public Date getDiscussionTime() {
        return this.discussionTime;
    }

    /**
     * Set discussionTime.
     * @param discussionTime The timestamp for when this discussion posting occurred.
     */
    public void setDiscussionTime(Date discussionTime) {
        this.discussionTime = discussionTime;
    }

    /**
     * Get time zone.
     * @return java.lang.String
     */
    public String getTimeZone() {
        return this.timeZone;
    }

    /**
     * Set time zone.
     * @param timezone Time zone the timestamp was taken in.
     */
    public void setTimeZone(String timezone) {
        this.timeZone = timezone;
    }

    /**
     * Get actionGuid.
     * @return java.lang.String
     */
    public String getActionGuid() {
        return this.actionGuid;
    }

    /**
     * Set actionGuid.
     * @param actionGuid actionGuid of the user as a string
     */
    public void setActionGuid(String actionGuid) {
        this.actionGuid = actionGuid;
    }

    /**
     * Get admitCode.
     * @return java.lang.String
     */
    public String getAdmitCode() {
        return this.admitCode;
    }

    /**
     * Set admitCode.
     * @param admitCode the admit code of the OLI course
     */
    public void setAdmitCode(String admitCode) {
        this.admitCode = admitCode;
    }

    /**
     * Get postGuid.
     * @return java.lang.String
     */
    public String getPostGuid() {
        return this.postGuid;
    }

    /**
     * Set postGuid.
     * @param postGuid the post's GUID
     */
    public void setPostGuid(String postGuid) {
        this.postGuid = postGuid;
    }

    /**
     * Get threadGuid.
     * @return java.lang.String
     */
    public String getThreadGuid() {
        return this.threadGuid;
    }

    /**
     * Set threadGuid.
     * @param threadGuid the GUID of the previous post
     */
    public void setThreadGuid(String threadGuid) {
        this.threadGuid = threadGuid;
    }

    /**
     * Get stars.
     * @return java.lang.String
     */
    public Integer getStars() {
        return this.stars;
    }

    /**
     * Set stars.
     * @param stars the star rating given by instructor
     */
    public void setStars(Integer stars) {
        this.stars = stars;
    }

    /**
     * Get hiddenFlag.
     * @return Boolean
     */
    public Boolean getHiddenFlag() {
        return this.hiddenFlag;
    }

    /**
     * Set hiddenFlag.
     * @param hiddenFlag Flag indicating whether the user is a system administrator (true) or
     * not (false)
     */
    public void setHiddenFlag(Boolean hiddenFlag) {
        this.hiddenFlag = hiddenFlag;
    }

    /**
     * Get acceptedDate.
     * @return java.lang.String
     */
    public Date getAcceptedDate() {
        return this.acceptedDate;
    }

    /**
     * Set acceptedDate.
     * @param acceptedDate the date this post was accepted by the instructor
     */
    public void setAcceptedDate(Date acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    /**
     * Get subject.
     * @return java.lang.String
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Set subject.
     * @param subject the subject of the post given by student
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Get body.
     * @return java.lang.String
     */
    public String getBody() {
        return this.body;
    }

    /**
     * Set body.
     * @param body the text of the post
     */
    public void setBody(String body) {
        this.body = body;
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
        buffer.append(objectToString("UserId", getUserId()));
        buffer.append(objectToString("SessionId", getSessionTag()));
        buffer.append(objectToString("DiscussionTime", getDiscussionTime()));
        buffer.append(objectToString("TimeZone", getTimeZone()));
        buffer.append(objectToString("ActionGuid", getActionGuid()));
        buffer.append(objectToString("AdmitCode", getAdmitCode()));
        buffer.append(objectToString("PostGuid", getPostGuid()));
        buffer.append(objectToString("ThreadGuid", getThreadGuid()));
        buffer.append(objectToString("Stars", getStars()));
        buffer.append(objectToString("HiddenFlag", getHiddenFlag()));
        buffer.append(objectToString("AcceptedDate", getAcceptedDate()));
        buffer.append(objectToString("Subject", getSubject()));
        buffer.append(objectToString("Body", getBody()));
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
        if (obj instanceof OliDiscussionItem) {
            OliDiscussionItem otherItem = (OliDiscussionItem)obj;

            if (!objectEquals(this.getUserId(), otherItem.getUserId())) {
                return false;
            }
            if (!objectEquals(this.getSessionTag(), otherItem.getSessionTag())) {
                return false;
            }
            if (!objectEquals(this.getDiscussionTime(), otherItem.getDiscussionTime())) {
                return false;
            }
            if (!objectEquals(this.getTimeZone(), otherItem.getTimeZone())) {
                return false;
            }
            if (!objectEquals(this.getActionGuid(), otherItem.getActionGuid())) {
                return false;
            }
            if (!objectEquals(this.getAdmitCode(), otherItem.getAdmitCode())) {
                return false;
            }
            if (!objectEquals(this.getPostGuid(), otherItem.getPostGuid())) {
                return false;
            }
            if (!objectEquals(this.getThreadGuid(), otherItem.getThreadGuid())) {
                return false;
            }
            if (!objectEquals(this.getStars(), otherItem.getStars())) {
                return false;
            }
            if (!objectEquals(this.getHiddenFlag(), otherItem.getHiddenFlag())) {
                return false;
            }
            if (!objectEquals(this.getAcceptedDate(), otherItem.getAcceptedDate())) {
                return false;
            }
            if (!objectEquals(this.getSubject(), otherItem.getSubject())) {
                return false;
            }
            if (!objectEquals(this.getBody(), otherItem.getBody())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSessionTag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDiscussionTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeZone());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getActionGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAdmitCode());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPostGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getThreadGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStars());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHiddenFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAcceptedDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSubject());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getBody());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>user id</li>
     * <li>session id</li>
     * <li>discussion time</li>
     * <li>time zone</li>
     * <li>action GUID</li>
     * <li>admit code</li>
     * <li>post GUID</li>
     * <li>thread GUID</li>
     * <li>stars</li>
     * <li>hidden flag</li>
     * <li>accepted date</li>
     * <li>subject</li>
     * <li>body</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        OliDiscussionItem otherItem = (OliDiscussionItem)obj;

        int value = 0;

        value = objectCompareTo(this.getUserId(), otherItem.getUserId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSessionTag(), otherItem.getSessionTag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDiscussionTime(), otherItem.getDiscussionTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeZone(), otherItem.getTimeZone());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getActionGuid(), otherItem.getActionGuid());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAdmitCode(), otherItem.getAdmitCode());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPostGuid(), otherItem.getPostGuid());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getThreadGuid(), otherItem.getThreadGuid());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStars(), otherItem.getStars());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getHiddenFlag(), otherItem.getHiddenFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAcceptedDate(), otherItem.getAcceptedDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSubject(), otherItem.getSubject());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getBody(), otherItem.getBody());
        if (value != 0) { return value; }

        return value;
    }
}