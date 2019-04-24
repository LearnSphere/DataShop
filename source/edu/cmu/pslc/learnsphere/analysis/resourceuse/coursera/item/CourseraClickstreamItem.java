/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.item;

import java.util.Date;

import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a coursera_clickstream
 *
 * @author 
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CourseraClickstreamItem extends Item implements java.io.Serializable, Comparable  {

    
    /** Database generated unique Id for this resource use transaction. col: pk*/
    private Long id;
    /** col: client*/
    private String client;
    /** col: from*/
    private String from;
    /** col: id*/
    private String courseId;
    /** col: key*/
    private String key;
    /** col: language*/
    private String language;
    /** col: page_url*/
    private String pageUrl;
    /** col: session*/
    private String session;
    /** col: timestamp */
    private Long timestamp;
    /** col: user_agent */
    private String userAgent;
    /** col: user_ip */
    private String userIp;
    /** col: username */
    private String username;
    /** value associated with clickstream. */
    private CourseraClickstreamVideoItem value;
    /**time diff bw two rows*/
    private Double prevTimeDiff;
    private Double nextTimeDiff;
    
    /** Default constructor. */
    public CourseraClickstreamItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id.
     */
    public CourseraClickstreamItem(Long id) {
        this.id = id;
    }

    /**
     * Get the id.
     * @return id as a Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param Database generated unique Id for this coursera clickstream.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns client. @return Returns the client. */
    public String getClient() {
        return client;
    }

    /** Set client. @param client The client to set. */
    public void setClient(String client) {
        this.client = client;
    }
    
    /** Returns from. @return Returns the from. */
    public String getFrom() {
        return from;
    }

    /** Set from. @param from The from to set. */
    public void setFrom(String from) {
        this.from = from;
    }
    
    /** Returns courseId. @return Returns the courseId. */
    public String getCourseId() {
        return courseId;
    }

    /** Set courseId. @param  The courseId to set. */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    /** Returns key. @return Returns the key. */
    public String getKey() {
        return key;
    }

    /** Set key. @param  The key to set. */
    public void setKey(String key) {
        this.key = key;
    }
    
    /** Returns language. @return Returns the language. */
    public String getLanguage() {
        return language;
    }

    /** Set language. @param  The language to set. */
    public void setLanguage(String language) {
        this.language = language;
    }
    
    /** Returns pageUrl. @return Returns the pageUrl. */
    public String getPageUrl() {
        return pageUrl;
    }

    /** Set pageUrl. @param  The pageUrl to set. */
    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
    
    /** Returns session. @return Returns the session. */
    public String getSession() {
        return session;
    }

    /** Set session. @param  The session to set. */
    public void setSession(String session) {
        this.session = session;
    }
    
    /** Returns userAgent. @return Returns the userAgent. */
    public String getUserAgent() {
        return userAgent;
    }

    /** Set userAgent. @param  The userAgent to set. */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    /** Returns userIp. @return Returns the userIp. */
    public String getUserIp() {
        return userIp;
    }

    /** Set userIp. @param  The userIp to set. */
    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }
    
    /** Returns username. @return Returns the username. */
    public String getUsername() {
        return username;
    }

    /** Set username. @param  The username to set. */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /** Returns value. @return Returns the value. */
    public CourseraClickstreamVideoItem getValue() {
        return value;
    }

    /** Set value. @param  The value to set. */
    public void setValue(CourseraClickstreamVideoItem value) {
        this.value = value;
    }
    
    /**
     * Get timestamp.
     * @return long
     */
    public Long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Set timestamp.
     * @param timestamp .
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get prevTimeDiff.
     * @return Integer
     */
    public Double getPrevTimeDiff() {
        return this.prevTimeDiff;
    }
    
    /**
     * calculate prevTimeDiff.
     * @return Integer
     */
    public void computePrevDiff(CourseraClickstreamItem prevItem) {
            if(session != null && prevItem.getSession() != null && session.equals(prevItem.getSession())
                            && timestamp != null && prevItem.getTimestamp() != null)
                    prevTimeDiff = (timestamp - prevItem.getTimestamp())/1000.0;
            else
                    prevTimeDiff = null;
    }

    /**
     * Set prevTimeDiff.
     * @param prevTimeDiff .
     */
    public void setPrevTimeDiff(Double prevTimeDiff) {
        this.prevTimeDiff = prevTimeDiff;
    }
    
    /**
     * Get nextTimeDiff.
     * @return Integer
     */
    public Double getNextTimeDiff() {
        return this.nextTimeDiff;
    }
    
    /**
     * calculate nextTimeDiff.
     * @return Integer
     */
    public void computeNextDiff(CourseraClickstreamItem nextItem) {
            if(session != null && nextItem.getSession() != null && session.equals(nextItem.getSession())
                            && timestamp != null && nextItem.getTimestamp() != null)
                    nextTimeDiff = (nextItem.getTimestamp() - timestamp )/1000.0;
            else
                    nextTimeDiff = null;
    }

    /**
     * Set nextTimeDiff.
     * @param nextTimeDiff .
     */
    public void setNextTimeDiff(Double nextTimeDiff) {
        this.nextTimeDiff = nextTimeDiff;
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
        buffer.append(objectToString("pk", getId()));
        buffer.append(objectToString("client", getClient()));
        buffer.append(objectToString("from", getFrom()));
        buffer.append(objectToString("id", getCourseId()));
        buffer.append(objectToString("key", getKey()));
        buffer.append(objectToString("language", getLanguage()));
        buffer.append(objectToString("pageUrl", getPageUrl()));
        buffer.append(objectToString("session", getSession()));
        buffer.append(objectToString("timestamp;", getTimestamp()));
        buffer.append(objectToString("userAgent", getUserAgent()));
        buffer.append(objectToString("userIp", getUserIp()));
        buffer.append(objectToString("username", getUsername()));
        buffer.append(objectToString("value", getValue()));
        buffer.append(objectToString("prevTimeDiff", getPrevTimeDiff()));
        buffer.append(objectToString("nextTimeDiff", getNextTimeDiff()));
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
        if (obj instanceof CourseraClickstreamItem) {
            CourseraClickstreamItem otherItem = (CourseraClickstreamItem)obj;

            if (!objectEquals(this.getClient(), otherItem.getClient())) {
                return false;
            }
            if (!objectEquals(this.getFrom(), otherItem.getFrom())) {
                return false;
            }
            if (!objectEquals(this.getCourseId(), otherItem.getCourseId())) {
                 return false;
            }
            if (!objectEquals(this.getKey(), otherItem.getKey())) {
                return false;
            }
            if (!objectEquals(this.getLanguage(), otherItem.getLanguage())) {
                 return false;
            }
            if (!objectEquals(this.getSession(), otherItem.getSession())) {
                 return false;
            }
            if (!objectEquals(this.getTimestamp(), otherItem.getTimestamp())) {
                 return false;
            }
            if (!objectEquals(this.getUserAgent(), otherItem.getUserAgent())) {
                return false;
           }
           if (!objectEquals(this.getUserIp(), otherItem.getUserIp())) {
                return false;
           }
            if (!objectEquals(this.getUsername(), otherItem.getUsername())) {
                 return false;
            }
            if (!objectEqualsFK(this.getValue(), otherItem.getValue())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getClient());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFrom());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCourseId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getKey());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLanguage());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPageUrl());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSession());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserAgent());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserIp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUsername());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getValue());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        CourseraClickstreamItem otherItem = (CourseraClickstreamItem)obj;
        int value = 0;
        
        value = objectCompareTo(this.getClient(), otherItem.getClient());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFrom(), otherItem.getFrom());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCourseId(), otherItem.getCourseId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getKey(), otherItem.getKey());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLanguage(), otherItem.getLanguage());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPageUrl(), otherItem.getPageUrl());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSession(), otherItem.getSession());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimestamp(), otherItem.getTimestamp());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUserAgent(), otherItem.getUserAgent());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUserIp(), otherItem.getUserIp());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getUsername(), otherItem.getUsername());
        if (value != 0) { return value; }
        
        value = objectCompareToFK(this.getValue(), otherItem.getValue());
        if (value != 0) { return value; }

        return value;
    }
}
