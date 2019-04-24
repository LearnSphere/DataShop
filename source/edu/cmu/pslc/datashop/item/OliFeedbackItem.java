/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single OLI feedback record.
 *
 * @author Alida Skogsholm
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliFeedbackItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this item. */
    private Long oliFeedbackId;
    /** The actual user ID for this item as a string. */
    private String userId;
    /** The session ID for this item as a string.  */
    private String sessionTag;
    /** The time stamp of this feedback post.  */
    private Date feedbackTime;
    /** Time zone the timestamp was taken in. */
    private String timeZone;
    /** The GUID from OLI's log action table. */
    private String actionGuid;
    /** The admit code of the course which this feedback data is associated with. */
    private String admitCode;
    /** The OLI id for this feedback page. */
    private String pageId;
    /** The OLI id for this question on the page. */
    private String questionId;
    /** The choice the student made for this question. */
    private String choice;

    /** Default constructor. */
    public OliFeedbackItem() {
    }

    /**
     * Constructor with id.
     * @param id the database id
     */
    public OliFeedbackItem(Long id) {
        this.oliFeedbackId = id;
    }

    /**
     * Returns the database id.
     * @return Long
     */
    public Comparable getId() {
        return this.oliFeedbackId;
    }

    /**
     * Set the id.
     * @param id the database id
     */
    public void setId(Long id) {
        this.oliFeedbackId = id;
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
     * Get feedbackTime.
     * @return java.util.Date
     */
    public Date getFeedbackTime() {
        return this.feedbackTime;
    }

    /**
     * Set feedbackTime.
     * @param feedbackTime The timestamp for when this feedback posting occurred.
     */
    public void setFeedbackTime(Date feedbackTime) {
        this.feedbackTime = feedbackTime;
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
     * @param admitCode The admit code for the OLI course.
     */
    public void setAdmitCode(String admitCode) {
        this.admitCode = admitCode;
    }

    /**
     * Get pageId.
     * @return java.lang.String
     */
    public String getPageId() {
        return this.pageId;
    }

    /**
     * Set pageId.
     * @param pageId The OLI id for this feedback page.
     */
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    /**
     * Get questionId.
     * @return java.lang.String
     */
    public String getQuestionId() {
        return this.questionId;
    }

    /**
     * Set questionId.
     * @param questionId The OLI id for this question on the feedback page.
     */
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    /**
     * Get choice.
     * @return java.lang.String
     */
    public String getChoice() {
        return this.choice;
    }

    /**
     * Set choice.
     * @param choice The choice the student made for this question.
     */
    public void setChoice(String choice) {
        this.choice = choice;
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
        buffer.append(objectToString("FeedbackTime", getFeedbackTime()));
        buffer.append(objectToString("TimeZone", getTimeZone()));
        buffer.append(objectToString("ActionGuid", getActionGuid()));
        buffer.append(objectToString("AdmitCode", getAdmitCode()));
        buffer.append(objectToString("PageId", getPageId()));
        buffer.append(objectToString("QuestionId", getQuestionId()));
        buffer.append(objectToString("Choice", getChoice()));
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
        if (obj instanceof OliFeedbackItem) {
            OliFeedbackItem otherItem = (OliFeedbackItem)obj;

            if (!objectEquals(this.getUserId(), otherItem.getUserId())) {
                return false;
            }
            if (!objectEquals(this.getSessionTag(), otherItem.getSessionTag())) {
                return false;
            }
            if (!objectEquals(this.getFeedbackTime(), otherItem.getFeedbackTime())) {
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
            if (!objectEquals(this.getPageId(), otherItem.getPageId())) {
                return false;
            }
            if (!objectEquals(this.getQuestionId(), otherItem.getQuestionId())) {
                return false;
            }
            if (!objectEquals(this.getChoice(), otherItem.getChoice())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFeedbackTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeZone());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getActionGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAdmitCode());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPageId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getQuestionId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getChoice());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>user id</li>
     * <li>session id</li>
     * <li>feedback time</li>
     * <li>time zone</li>
     * <li>action GUID</li>
     * <li>admit code</li>
     * <li>page id</li>
     * <li>question id</li>
     * <li>choice</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        OliFeedbackItem otherItem = (OliFeedbackItem)obj;

        int value = 0;

        value = objectCompareTo(this.getUserId(), otherItem.getUserId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSessionTag(), otherItem.getSessionTag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFeedbackTime(), otherItem.getFeedbackTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeZone(), otherItem.getTimeZone());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getActionGuid(), otherItem.getActionGuid());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAdmitCode(), otherItem.getAdmitCode());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPageId(), otherItem.getPageId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getQuestionId(), otherItem.getQuestionId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getChoice(), otherItem.getChoice());
        if (value != 0) { return value; }

        return value;
    }
}