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
 * Represents a single row in the log action table.
 *
 * @author Alida Skogsholm
 * @version $Revision: 3377 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:08:08 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LogActionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private String guid;
    /** The session id. */
    private String sessionId;
    /** The time of the action. */
    private Date   time;
    /** The time zone of the time. */
    private String timeZone;
    /** The source of the action. */
    private String source;
    /** The action of the action, whatever that means. */
    private String action;
    /** External Object Id. */
    private String extObjId;
    /** Container. */
    private String container;
    /** Info Type. */
    private String infoType;
    /** Info. */
    private String info;
    /** Server receipt time */
    private Date serverReceiptTime;

    /** Default constructor. */
    public LogActionItem() {
    }

    /**
     *  Constructor with id.
     *  @param guid Database generated unique ID for this item.
     */
    public LogActionItem(String guid) {
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
     * Get source.
     * @return the source as a String
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Set source.
     * @param source the source of this action
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get action.
     * @return the action as a String
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Set action.
     * @param action the action of this action, whatever that means
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Get external object id.
     * @return the external object id as a String
     */
    public String getExtObjId() {
        return this.extObjId;
    }

    /**
     * Set external object id.
     * @param extObjId the external object id of this action
     */
    public void setExtObjId(String extObjId) {
        this.extObjId = extObjId;
    }

    /**
     * Get container.
     * @return the container as a String
     */
    public String getContainer() {
        return this.container;
    }

    /**
     * Set container.
     * @param container the container of this action
     */
    public void setContainer(String container) {
        this.container = container;
    }

    /**
     * Get infoType.
     * @return the infoType as a String
     */
    public String getInfoType() {
        return this.infoType;
    }

    /**
     * Set infoType.
     * @param infoType the infoType of this action
     */
    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    /**
     * Get info field.
     * @return the info field as a String
     */
    public String getInfo() {
        return this.info;
    }

    /**
     * Set info field.
     * @param info the info of this action
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Get server receipt time.
     * @return the server receipt time as a Date.
     */
    public Date getServerReceiptTime() {
        return this.serverReceiptTime;
    }

    /**
     * Set server receipt time.
     * @param serverReceiptTime for this action.
     */
    public void setServerReceiptTime(Date serverReceiptTime) {
        this.serverReceiptTime = serverReceiptTime;
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
         buffer.append(objectToString("SessionId", getSessionId()));
         buffer.append(objectToString("Time", getTime()));
         buffer.append(objectToString("TimeZone", getTimeZone()));
         buffer.append(objectToString("Source", getSource()));
         buffer.append(objectToString("Action", getAction()));
         buffer.append(objectToString("ExtObjId", getExtObjId()));
         buffer.append(objectToString("Container", getContainer()));
         buffer.append(objectToString("InfoType", getInfoType()));
         buffer.append(objectToString("Info", getInfo()));
         buffer.append(objectToString("SeverReceiptTime", getServerReceiptTime()));
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
        if (obj instanceof LogActionItem) {
            LogActionItem otherItem = (LogActionItem)obj;

            if (!objectEquals(this.getSessionId(), otherItem.getSessionId())) {
                return false;
            }
            if (!objectEquals(this.getTime(), otherItem.getTime())) {
                return false;
            }
            if (!objectEquals(this.getTimeZone(), otherItem.getTimeZone())) {
                return false;
            }
            if (!objectEquals(this.getSource(), otherItem.getSource())) {
                return false;
            }
            if (!objectEquals(this.getAction(), otherItem.getAction())) {
                return false;
            }
            if (!objectEquals(this.getExtObjId(), otherItem.getExtObjId())) {
                return false;
            }
            if (!objectEquals(this.getContainer(), otherItem.getContainer())) {
                return false;
            }
            if (!objectEquals(this.getInfoType(), otherItem.getInfoType())) {
                return false;
            }
            if (!objectEquals(this.getInfo(), otherItem.getInfo())) {
                return false;
            }
            if (!objectEquals(this.getServerReceiptTime(), otherItem.getServerReceiptTime())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSessionId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeZone());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSource());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getExtObjId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getContainer());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfoType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfo());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getServerReceiptTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>session id</li>
     * <li>time</li>
     * <li>time zone</li>
     * <li>source</li>
     * <li>action</li>
     * <li>external object id</li>
     * <li>container</li>
     * <li>info type</li>
     * <li>info</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        LogActionItem otherItem = (LogActionItem)obj;
        int value = 0;

        value = objectCompareTo(this.getSessionId(), otherItem.getSessionId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTime(), otherItem.getTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeZone(), otherItem.getTimeZone());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSource(), otherItem.getSource());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getExtObjId(), otherItem.getExtObjId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getContainer(), otherItem.getContainer());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInfoType(), otherItem.getInfoType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInfo(), otherItem.getInfo());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getServerReceiptTime(), otherItem.getServerReceiptTime());
        if (value != 0) { return value; }

        return value;
    }
}