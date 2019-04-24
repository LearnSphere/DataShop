/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a Remote Instance.
 *
 * @author Cindy Tipper
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class RemoteInstanceItem extends Item
    implements java.io.Serializable, Comparable<RemoteInstanceItem>
{
    /** Debug logging. */
    private static Logger logger = Logger.getLogger(RemoteInstanceItem.class.getName());

    /** Primary key. */
    private Long remoteInstanceId;
    /** Name of the remote instance. */
    private String name;
    /** URL of the remote DataShop instance.*/
    private String datashopUrl;

    /** Max instance name length. */
    public static final int INSTANCE_NAME_MAX_LEN = 100;

    /** Max instance URL length. */
    public static final int INSTANCE_URL_MAX_LEN = 48;

    /** Default constructor. */
    public RemoteInstanceItem() {}

    /**
     * Returns the id.
     * @return String the id, the database primary key
     */
    public Comparable getId() { return this.remoteInstanceId; }

    /**
     * Set remoteInstanceId.
     * @param remoteInstanceId the id, the primary key
     */
    public void setId(Long remoteInstanceId) { this.remoteInstanceId = remoteInstanceId; }

    /**
     * Get the name of this remote instance.
     * @return String name
     */
    public String getName() { return this.name; }

    /**
     * Set the name of this remote instance.
     * @param name the instance name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Get the URL for this remote DataShop instance.
     * @return String datashopUrl
     */
    public String getDatashopUrl() { return this.datashopUrl; }

    /**
     * Set the URL for this remote DataShop instance.
     * @param datashopUrl the URL
     */
    public void setDatashopUrl(String datashopUrl) { this.datashopUrl = datashopUrl; }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
        return toString("RemoteInstanceId", getId(),
                        "name", getName(), "DatashopUrl", getDatashopUrl());
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

        if (obj instanceof RemoteInstanceItem) {
            RemoteInstanceItem otherItem = (RemoteInstanceItem)obj;

            if (!objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }
            if (!objectEquals(this.getDatashopUrl(), otherItem.getDatashopUrl())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(name);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopUrl);
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(RemoteInstanceItem obj) {
        RemoteInstanceItem otherItem = (RemoteInstanceItem)obj;

        int value = 0;

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopUrl(), otherItem.getDatashopUrl());
        if (value != 0) { return value; }

        return value;
    }

}