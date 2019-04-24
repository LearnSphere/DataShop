/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.item;

import java.util.Date;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the log session table.
 *
 * @author Alida Skogsholm
 * @version $Revision: 3377 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:08:08 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LogSessionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private String guid;
    /** The user id. */
    private String userId;
    /** The session id. */
    private String sessionId;
    /** The time of the action. */
    private Date   time;
    /** The time zone of the time. */
    private String timeZone;

    /** Default constructor. */
    public LogSessionItem() {
    }

    /**
     *  Constructor with id.
     *  @param guid Database generated unique ID for this item.
     */
    public LogSessionItem(String guid) {
        this.guid = guid;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() {
        return this.guid;
    }

    /**
     * Set the id.
     * @param guid Database generated unique Id for this item.
     */
    public void setId(String guid) {
        this.guid = guid;
    }

    /**
     * Get the user id.
     * @return the user id as a String
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Set the user id.
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the session id.
     * @return the id as a String
     */
    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * Set the session id.
     * @param sessionId the session id
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Get time.
     * @return java.util.Date
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * Set time.
     * @param time The time of this action.
     */
    public void setTime(Date time) {
        this.time = time;
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
     * @param zone Time zone the timestamp was taken in.
     */
    public void setTimeZone(String zone) {
        this.timeZone = zone;
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
         buffer.append(objectToString("SessionId", getSessionId()));
         buffer.append(objectToString("Time", getTime()));
         buffer.append(objectToString("TimeZone", getTimeZone()));
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
        if (obj instanceof LogSessionItem) {
            LogSessionItem otherItem = (LogSessionItem)obj;

            if (!objectEquals(this.getUserId(), otherItem.getUserId())) {
                return false;
            }
            if (!objectEquals(this.getSessionId(), otherItem.getSessionId())) {
                return false;
            }
            if (!objectEquals(this.getTime(), otherItem.getTime())) {
                return false;
            }
            if (!objectEquals(this.getTimeZone(), otherItem.getTimeZone())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSessionId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeZone());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>user id</li>
     * <li>session id</li>
     * <li>time</li>
     * <li>time zone</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        LogSessionItem otherItem = (LogSessionItem)obj;
        int value = 0;

        value = objectCompareTo(this.getUserId(), otherItem.getUserId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSessionId(), otherItem.getSessionId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTime(), otherItem.getTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeZone(), otherItem.getTimeZone());
        if (value != 0) { return value; }

        return value;
    }
}