/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the discoursedb.user table.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DUserItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private Long userId;
    /** The time this user was created. */
    private Date created;
    /** The time this user was modified. */
    private Date modified;
    /** The end time for this user. */
    private Date endTime;
    /** The start time for this user. */
    private Date startTime;
    /** The version for this user. */
    private Long version;
    /** The country for this user. */
    private String country;
    /** The email for this user. */
    private String email;
    /** The IP addr for this user. */
    private String ipAddr;
    /** The language for this user. */
    private String language;
    /** The location for this user. */
    private String location;
    /** The real name of this user. */
    private String realName;
    /** The user name for this user. */
    private String userName;
    /** Database ID for source item. */
    private Long srcId;
    /** The DataSources for this user. */
    private DataSourcesItem dataSources;
    
    /** Default constructor. */
    public DUserItem() {}

    /**
     *  Constructor with id.
     *  @param userId the database generated unique ID for this item.
     */
    public DUserItem(Long userId) {
        this.userId = userId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() { return this.userId; }

    /**
     * Set the id.
     * @param userId Database generated unique Id for this item.
     */
    public void setId(Long userId) {
        this.userId = userId;
    }

    /**
     * Get time created.
     * @return the time created
     */
    public Date getCreated() { return this.created; }

    /**
     * Set created time.
     * @param created The time this user was created
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Get time modified.
     * @return the time modified
     */
    public Date getModified() { return this.modified; }

    /**
     * Set modified time.
     * @param modified The time this user was modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Get end time.
     * @return the end time
     */
    public Date getEndTime() { return this.endTime; }

    /**
     * Set end time.
     * @param endTime The end time for this user
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Get start time.
     * @return the start time
     */
    public Date getStartTime() { return this.startTime; }

    /**
     * Set start time.
     * @param startTime The start time for this user
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the version.
     * @return Long the user version
     */
    public Long getVersion() { return this.version; }

    /**
     * Set the version.
     * @param version the version of this user
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Get the country.
     * @return String the user country
     */
    public String getCountry() { return this.country; }

    /**
     * Set the country.
     * @param country the country of this user
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Get the email.
     * @return String the user email
     */
    public String getEmail() { return this.email; }

    /**
     * Set the email.
     * @param email the email of this user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the ipAddr.
     * @return String the user ipAddr
     */
    public String getIpAddr() { return this.ipAddr; }

    /**
     * Set the ipAddr.
     * @param ipAddr the ipAddr of this user
     */
    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    /**
     * Get the language.
     * @return String the user language
     */
    public String getLanguage() { return this.language; }

    /**
     * Set the language.
     * @param language the language of this user
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get the location.
     * @return String the user location
     */
    public String getLocation() { return this.location; }

    /**
     * Set the location.
     * @param location the location of this user
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Get user realName.
     * @return the user realName
     */
    public String getRealName() { return this.realName; }

    /**
     * Set user realName.
     * @param realName The realName of the user.
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * Get user userName.
     * @return the user userName
     */
    public String getUserName() { return this.userName; }

    /**
     * Set user userName.
     * @param userName The userName of the user.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get the id of the source object.
     * @return Long the id
     */
    public Long getSourceId() { return srcId; }

    /**
     * Set the id of the source object.
     * @param srcId
     */
    public void setSourceId(Long srcId) { this.srcId = srcId; }

    /**
     * Get dataSources
     * @return DataSourcesItem
     */
    public DataSourcesItem getDataSources() {
        return this.dataSources;
    }

    /**
     * Set dataSources.
     * @param dataSources DataSources associated with this dataSourceInstance
     */
    public void setDataSources(DataSourcesItem dataSources) {
        this.dataSources = dataSources;
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
         buffer.append(objectToString("Created", getCreated()));
         buffer.append(objectToString("Modified", getModified()));
         buffer.append(objectToString("EndTime", getEndTime()));
         buffer.append(objectToString("StartTime", getStartTime()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToString("Country", getCountry()));
         buffer.append(objectToString("Email", getEmail()));
         buffer.append(objectToString("IpAddr", getIpAddr()));
         buffer.append(objectToString("Language", getLanguage()));
         buffer.append(objectToString("Location", getLocation()));
         buffer.append(objectToString("RealName", getRealName()));
         buffer.append(objectToString("UserName", getUserName()));
         buffer.append(objectToStringFK("DataSources", getDataSources()));
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
        if (obj instanceof DUserItem) {
            DUserItem otherItem = (DUserItem)obj;

            if (!objectEquals(this.getCreated(), otherItem.getCreated())) {
                return false;
            }
            if (!objectEquals(this.getModified(), otherItem.getModified())) {
                return false;
            }
            if (!objectEquals(this.getEndTime(), otherItem.getEndTime())) {
                return false;
            }
            if (!objectEquals(this.getStartTime(), otherItem.getStartTime())) {
                return false;
            }
            if (!objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!objectEquals(this.getCountry(), otherItem.getCountry())) {
                return false;
            }
            if (!objectEquals(this.getEmail(), otherItem.getEmail())) {
                return false;
            }
            if (!objectEquals(this.getIpAddr(), otherItem.getIpAddr())) {
                return false;
            }
            if (!objectEquals(this.getLanguage(), otherItem.getLanguage())) {
                return false;
            }
            if (!objectEquals(this.getLocation(), otherItem.getLocation())) {
                return false;
            }
            if (!objectEquals(this.getRealName(), otherItem.getRealName())) {
                return false;
            }
            if (!objectEquals(this.getUserName(), otherItem.getUserName())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataSources(), otherItem.getDataSources())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreated());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getModified());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCountry());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEmail());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getIpAddr());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLanguage());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLocation());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getRealName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataSources());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>created</li>
     * <li>modified</li>
     * <li>endTime</li>
     * <li>startTime</li>
     * <li>version</li>
     * <li>country</li>
     * <li>email</li>
     * <li>ipAddr</li>
     * <li>language</li>
     * <li>location</li>
     * <li>realName</li>
     * <li>userName</li>
     * <li>dataSources</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DUserItem otherItem = (DUserItem)obj;
        int value = 0;

        value = objectCompareTo(this.getCreated(), otherItem.getCreated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModified(), otherItem.getModified());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTime(), otherItem.getEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCountry(), otherItem.getCountry());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEmail(), otherItem.getEmail());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getIpAddr(), otherItem.getIpAddr());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLanguage(), otherItem.getLanguage());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLocation(), otherItem.getLocation());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getRealName(), otherItem.getRealName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUserName(), otherItem.getUserName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataSources(), otherItem.getDataSources());
        if (value != 0) { return value; }

        return value;
    }
}