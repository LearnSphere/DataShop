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
 * Represents a ProblemEvent record of the system.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6372 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-15 09:41:13 -0400 (Fri, 15 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemEventItem extends Item
            implements java.io.Serializable, Comparable  {

    /** The ProblemEvent_id for this item, auto_incremented. */
    private Long problemEventId;

    /** The start time  for this ProblemEvent. */
    private Date startTime;

    /** The start time milliseconds. */
    private Integer startTimeMS;

    /** The enumerated type of the event type. */
    private EventFlag eventFlag;

    /** The actual value of the event type. */
    private String eventType;

    /** The student's Nth view of this problem. */
    private Integer problemView;

    /** Session associated with this ProblemEvent. */
    private SessionItem session;

    /** Problem associated with this ProblemEvent. */
    private ProblemItem problem;

    /** Default constructor. */
    public ProblemEventItem() {
    }

    /**
     * Constructor with id.
     * @param problemEventId as long, the unique id.
     */
    public ProblemEventItem(Long problemEventId) {
        this.problemEventId = problemEventId;
    }

    /**
     * Returns ProblemEventId.
     * @return the Long ProblemEventId as a Comparable
     */
    public Comparable getId() {
        return this.problemEventId;
    }

    /**
     * Set ProblemEventId.
     * @param problemEventId Database generated unique id.
     */
    public void setId(Long problemEventId) {
        this.problemEventId = problemEventId;
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
     * Set eventType.
     * @return EventType.
     */
    public EventFlag getEventFlag() {
        return this.eventFlag;
    }

    /**
     * Set eventType.
     * @param eventType as String.
     */
    public void setEventFlag(EventFlag eventType) {
        this.eventFlag = eventType;
    }

    /**
     * Get eventType.
     * @return String.
     */
    public String getEventType() {
        return this.eventType;
    }

    /**
     * Set eventType.
     * @param eventType as String.
     */
    public void setEventType(String eventType) {
        setEventFlag(EventFlag.getInstance(eventType));
        this.eventType = eventType;
    }

    /** Returns the problemView. @return the problemView */
    public Integer getProblemView() { return problemView; }

    /** Sets the problemView. @param problemView the problemView to set */
    public void setProblemView(Integer problemView) { this.problemView = problemView; }

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
     * Get problem.
     * @return a problem item
     */
    public ProblemItem getProblem() {
        return this.problem;
    }

    /**
     * Set problem.
     * @param problem ProblemItem associated with this item
     */
    public void setProblem(ProblemItem problem) {
        this.problem = problem;
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
        buffer.append(objectToString("ProblemEventId", this.getId()));
        buffer.append(objectToString("StartTime", this.getStartTime()));
        buffer.append(objectToString("StartTimeMS", this.getStartTimeMS()));
        buffer.append(objectToString("EventFlag", this.getEventFlag()));
        buffer.append(objectToString("EventType", this.getEventType()));
        buffer.append(objectToString("ProblemView", this.getProblemView()));
        buffer.append(objectToStringFK("Session", this.getSession()));
        buffer.append(objectToStringFK("Problem", this.getProblem()));
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
        if (obj instanceof ProblemEventItem) {
            ProblemEventItem otherItem = (ProblemEventItem)obj;

            if (!objectEqualsFK(this.getSession(), otherItem.getSession())) {
                return false;
            }
            if (!objectEqualsFK(this.getProblem(), otherItem.getProblem())) {
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
            if (!objectEquals(this.getProblemView(), otherItem.getProblemView())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getProblemView());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getSession());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getProblem());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>session</li>
     * <li>problem</li>
     * <li>startTime</li>
     * <li>endTime</li>
     * <li>eventFlag</li>
     * <li>eventType</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProblemEventItem otherItem = (ProblemEventItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getSession(), otherItem.getSession());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getProblem(), otherItem.getProblem());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTimeMS(), otherItem.getStartTimeMS());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEventFlag(), otherItem.getEventFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEventType(), otherItem.getEventType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProblemView(), otherItem.getProblemView());
        if (value != 0) { return value; }

        return value;
    }
}