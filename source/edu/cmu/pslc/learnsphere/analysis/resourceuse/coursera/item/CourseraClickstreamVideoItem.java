/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.item;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Coursera_clickstream video item.
 *
 * @author Hui Cheng
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */

public class CourseraClickstreamVideoItem extends Item implements java.io.Serializable, Comparable  {
    /** Database generated unique id. */
    private Long id;
    /** currentTime*/
    private Double currentTime;
    /** Error*/
    private String error;
    /** eventTimestamp */
    private Long eventTimestamp;
    /**fragment*/
    private String fragment;
    /** initTimestamp */
    private Long initTimestamp;
    /** lectureID */
    private Integer lectureId;
    /** networkState */
    private Integer networkState;
    /**paused*/
    private String paused;
    /** playbackRate*/
    private Double playbackRate;
    /** prevTime*/
    private Double prevTime;
    /** readyState */
    private Integer readyState;
    /**type*/
    private String type;

    /** Default constructor. */
    public CourseraClickstreamVideoItem() {
    }

    /**
     * Constructor with id.
     * @param  Database generated unique id
     */
    public CourseraClickstreamVideoItem(Long id) {
        this.id = id;
    }

    /**
     * Get id.
     * @return Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param Database generated unique id
     */
    public void setId(Long id) {
        this.id = id;
    }
 
    /**
     * Get the currentTime
     * @return the currentTime
     */
    public Double getCurrentTime() {
        return currentTime;
    }

    /**
     * Set the currentTime
     * @param currentTime 
     */
    public void setCurrentTime(Double currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * Get the error.
     * @return String error
     */
    public String getError() {
        return error;
    }

    /**
     * Set error.
     * @param error
     */
    public void setError(String error) {
        this.error = error;
    }
    
    /**
     * Get the eventTimestamp.
     * @return long eventTimestamp
     */
    public Long getEventTimestamp() {
        return eventTimestamp;
    }

    /**
     * Set eventTimestamp.
     * @param eventTimestamp
     */
    public void setEventTimestamp(Long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    /**
     * Get the initTimestamp
     * @return the initTimestamp
     */
    public Long getInitTimestamp() {
        return initTimestamp;
    }

    /**
     * Set the initTimestamp
     * @param initTimestamp 
     */
    public void setInitTimestamp(Long initTimestamp) {
        this.initTimestamp = initTimestamp;
    }
    
    /**
     * Get the fragment.
     * @return String fragment
     */
    public String getFragment() {
        return fragment;
    }

    /**
     * Set fragment.
     * @param fragment
     */
    public void setFragment(String fragment) {
        this.fragment = fragment;
    }
    
    
    /**
     * Get the lectureId.
     * @return int lectureId
     */
    public Integer getLectureId() {
        return lectureId;
    }

    /**
     * Set lectureId.
     * @param lectureId
     */
    public void setLectureId(Integer lectureId) {
        this.lectureId = lectureId;
    }
    
    /**
     * Get the networkState.
     * @return int networkState
     */
    public Integer getNetworkState() {
        return networkState;
    }

    /**
     * Set networkState.
     * @param networkState
     */
    public void setNetworkState(Integer networkState) {
        this.networkState = networkState;
    }
    
    
    /**
     * Get the readyState.
     * @return int readyState
     */
    public Integer getReadyState() {
        return readyState;
    }

    /**
     * Set readyState.
     * @param readyState
     */
    public void setReadyState(Integer readyState) {
        this.readyState = readyState;
    }
    
    /**
     * Get the playbackRate
     * @return the playbackRate
     */
    public Double getPlaybackRate() {
        return playbackRate;
    }

    /**
     * Set the playbackRate
     * @param playbackRate 
     */
    public void setPlaybackRate(Double playbackRate) {
        this.playbackRate = playbackRate;
    }
    
    /**
     * Get the prevTime
     * @return the prevTime
     */
    public Double getPrevTime() {
        return prevTime;
    }

    /**
     * Set the prevTime
     * @param prevTime 
     */
    public void setPrevTime(Double prevTime) {
        this.prevTime = prevTime;
    }
    
    /**
     * Get the paused.
     * @return String paused
     */
    public String getPaused() {
        return paused;
    }

    /**
     * Set paused.
     * @param paused
     */
    public void setPaused(String paused) {
        this.paused = paused;
    }
    
    /**
     * Get the type.
     * @return String type
     */
    public String getType() {
        return type;
    }

    /**
     * Set type.
     * @param type
     */
    public void setType(String type) {
        this.type = type;
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
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToString("currentTime",  getCurrentTime()));
        buffer.append(objectToString("error",  getError()));
        buffer.append(objectToString("eventTimestamp",  getEventTimestamp()));
        buffer.append(objectToString("fragment",  getFragment()));
        buffer.append(objectToString("initTimestamp",  getInitTimestamp()));
        buffer.append(objectToString("lectureId",  getLectureId()));
        buffer.append(objectToString("networkState",  getNetworkState()));
        buffer.append(objectToString("paused",  getPaused()));
        buffer.append(objectToString("playbackRate",  getPlaybackRate()));
        buffer.append(objectToString("prevTime",  getPrevTime()));
        buffer.append(objectToString("readyState",  getReadyState()));
        buffer.append(objectToString("type",  getType()));
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
        if (obj instanceof CourseraClickstreamVideoItem) {
            CourseraClickstreamVideoItem otherItem = (CourseraClickstreamVideoItem)obj;
            
            if (!objectEquals(this.getCurrentTime(), otherItem.getCurrentTime())) {
                return false;
            }
            if (!objectEquals(this.getError(), otherItem.getError())) {
                    return false;
            }
            if (!objectEquals(this.getEventTimestamp(), otherItem.getEventTimestamp())) {
                    return false;
                }
            if (!objectEquals(this.getFragment(), otherItem.getFragment())) {
                    return false;
                }
            if (!objectEquals(this.getInitTimestamp(), otherItem.getInitTimestamp())) {
                    return false;
                }
            if (!objectEquals(this.getLectureId(), otherItem.getLectureId())) {
                    return false;
                }
            if (!objectEquals(this.getNetworkState(), otherItem.getNetworkState())) {
                    return false;
                }
            if (!objectEquals(this.getPaused(), otherItem.getPaused())) {
                    return false;
                }
            if (!objectEquals(this.getPlaybackRate(), otherItem.getPlaybackRate())) {
                    return false;
                }
            if (!objectEquals(this.getPrevTime(), otherItem.getPrevTime())) {
                    return false;
                }
            if (!objectEquals(this.getReadyState(), otherItem.getReadyState())) {
                    return false;
                }
            if (!objectEquals(this.getType(), otherItem.getType())) {
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
        
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCurrentTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getError());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEventTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFragment());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInitTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLectureId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNetworkState());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPaused());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPlaybackRate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPrevTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getReadyState());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getType());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CourseraClickstreamVideoItem otherItem = (CourseraClickstreamVideoItem)obj;
        int value = 0;
        
        value = objectCompareTo(this.getCurrentTime(), otherItem.getCurrentTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getError(), otherItem.getError());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getEventTimestamp(), otherItem.getEventTimestamp());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getFragment(), otherItem.getFragment());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getInitTimestamp(), otherItem.getInitTimestamp());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getLectureId(), otherItem.getLectureId());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getNetworkState(), otherItem.getNetworkState());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getPlaybackRate(), otherItem.getPlaybackRate());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getPrevTime(), otherItem.getPrevTime());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getReadyState(), otherItem.getReadyState());
        if (value != 0) { return value; }
        value = objectCompareTo(this.getType(), otherItem.getType());
        if (value != 0) { return value; }
        

        return value;
    }
}