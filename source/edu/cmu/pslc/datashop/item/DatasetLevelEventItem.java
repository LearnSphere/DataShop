/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.type.EventFlag;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a DatasetLevelEvent record of the system.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6372 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-15 09:41:13 -0400 (Fri, 15 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelEventItem extends Item
            implements java.io.Serializable, Comparable  {

    /** The DatasetLevelEvent_id for this item, auto_incremented. */
    private Long datasetLevelEventId;

    /** The start time  for this DatasetLevelEvent. */
    private Date startTime;

    /** The start time milliseconds. */
    private Integer startTimeMS;

    /** The numerical event flag. */
    private EventFlag eventFlag;

    /** The enumerated type of the event type*/
    private String eventType;

    /** The student's Nth view of this dataset level. */
    private Integer levelView;

    /** Session associated with this DatasetLevelEvent. */
    private SessionItem session;

    /** DatasetLevel associated with this DatasetLevelEvent. */
    private DatasetLevelItem datasetLevel;

    /** Default constructor. */
    public DatasetLevelEventItem() {
    }

    /**
     * Constructor with id.
     * @param datasetLevelEventId as long, the unique id.
     */
    public DatasetLevelEventItem(Long datasetLevelEventId) {
        this.datasetLevelEventId = datasetLevelEventId;
    }

    /**
     * Returns DatasetLevelEventId.
     * @return the Long DatasetLevelEventId as a Comparable
     */
    public Comparable getId() {
        return this.datasetLevelEventId;
    }

    /**
     * Set DatasetLevelEventId.
     * @param datasetLevelEventId Database generated unique id.
     */
    public void setId(Long datasetLevelEventId) {
        this.datasetLevelEventId = datasetLevelEventId;
    }

    /**
     * Get start time.
     * @return Date.
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * Set start time.
     * @param startTime as Date.
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /** Returns startTimeMS. @return Returns the startTimeMS. */
    public Integer getStartTimeMS() {
        return startTimeMS;
    }

    /** Set startTimeMS. @param startTimeMS The startTimeMS to set. */
    public void setStartTimeMS(Integer startTimeMS) {
        this.startTimeMS = startTimeMS;
    }

    /**
     * Get the eventFlag.
     * @return the eventFlag
     */
    public EventFlag getEventFlag() {
        return eventFlag;
    }

    /**
     * Set the eventFlag.
     * @param eventFlag the eventFlag to set
     */
    public void setEventFlag(EventFlag eventFlag) {
        this.eventFlag = eventFlag;
    }

    /**
     * Get eventType.
     * @return String.
     */
    public String getEventType() {
        return this.eventType;
    }

    /**
     * Set eventType and the eventFlag with the given string.
     * @param eventType as String.
     */
    public void setEventType(String eventType) {
        setEventFlag(EventFlag.getInstance(eventType));
        this.eventType = eventType;
    }

    /**
     * Set eventType and the event Flag with the given EventFlag.
     * @param eventFlag as EventFlag object
     */
    public void setEventType(EventFlag eventFlag) {
        setEventFlag(eventFlag);
        this.eventType = eventFlag.toString();
    }

    /** Returns the levelView. @return the levelView */
    public Integer getLevelView() { return levelView; }

    /** Sets the levelView. @param levelView the levelView to set */
    public void setLevelView(Integer levelView) { this.levelView = levelView; }

    /**
     * Get session.
     * @return a SessionItem object
     */
    public SessionItem getSession() {
        return this.session;
    }

    /**
     * Set session.
     * @param session SessionItem associated with this item
     */
    public void setSession(SessionItem session) {
        this.session = session;
    }

    /**
     * Get datasetLevel.
     * @return a dataset level item
     */
    public DatasetLevelItem getDatasetLevel() {
        return this.datasetLevel;
    }

    /**
     * Set datasetLevel.
     * @param datasetLevel DatasetLevelItem associated with this item
     */
    public void setDatasetLevel(DatasetLevelItem datasetLevel) {
        this.datasetLevel = datasetLevel;
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
        buffer.append(objectToString("DatasetLevelEventId", this.getId()));
        buffer.append(objectToString("StartTime", this.getStartTime()));
        buffer.append(objectToString("StartTimeMS", this.getStartTimeMS()));
        buffer.append(objectToString("EventFlag", this.getEventFlag()));
        buffer.append(objectToString("EventType", this.getEventType()));
        buffer.append(objectToString("LevelView", this.getLevelView()));
        buffer.append(objectToStringFK("Session", this.getSession()));
        buffer.append(objectToStringFK("DatasetLevel", this.getDatasetLevel()));
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
        if (obj instanceof DatasetLevelEventItem) {
            DatasetLevelEventItem otherItem = (DatasetLevelEventItem)obj;

            if (!objectEqualsFK(this.getSession(), otherItem.getSession())) {
                return false;
            }
            if (!objectEqualsFK(this.getDatasetLevel(), otherItem.getDatasetLevel())) {
                return false;
            }
            if (!objectEquals(this.getStartTime(), otherItem.getStartTime())) {
                return false;
            }
            if (!objectEquals(this.getStartTimeMS(), otherItem.getStartTimeMS())) {
                return false;
            }
            if (!objectEquals(this.getEventFlag(), otherItem.getEventFlag())) {
                return false;
            }
            if (!objectEquals(this.getEventType(), otherItem.getEventType())) {
                return false;
            }
            if (!objectEquals(this.getLevelView(), otherItem.getLevelView())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getStartTimeMS());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getEventFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getEventType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getLevelView());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getSession());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDatasetLevel());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>session</li>
     * <li>datasetLevel</li>
     * <li>startTime</li>
     * <li>endTime</li>
     * <li>eventType</li>
     * <li>levelView</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetLevelEventItem otherItem = (DatasetLevelEventItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getSession(), otherItem.getSession());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDatasetLevel(), otherItem.getDatasetLevel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTimeMS(), otherItem.getStartTimeMS());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEventFlag(), otherItem.getEventFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEventType(), otherItem.getEventType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLevelView(), otherItem.getLevelView());
        if (value != 0) { return value; }

        return value;
    }
} // end DatasetLevelEvent.java