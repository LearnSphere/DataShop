/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between a discourse and a remote instance.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseInstanceMapItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private DiscourseInstanceMapId id;

    /** The discourse item associated with this item. */
    private DiscourseItem discourse;

    /** The remote instance item associated with this item. */
    private RemoteInstanceItem remoteInstance;

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(DiscourseInstanceMapId id) {
        this.id = id;
    }

    /**
     * Get the discourse
     * @return the discourse
     */
    public DiscourseItem getDiscourse() {
        return discourse;
    }

    /**
     * Set the discourse.
     * @param discourse the discourse to set
     */
    protected void setDiscourse(DiscourseItem discourse) {
        this.discourse = discourse;
    }

    /**
     * Public set discourse method to update the composite key as well.
     * @param discourseItem Part of the composite key - FK
     */
    public void setDiscourseExternal(DiscourseItem discourseItem) {
        setDiscourse(discourseItem);
        this.id = new DiscourseInstanceMapId(this.discourse, this.remoteInstance);
    }

    /**
     * Get the remoteInstance.
     * @return the remoteInstance
     */
    public RemoteInstanceItem getRemoteInstance() {
        return remoteInstance;
    }

    /**
     * Set the remoteInstance.
     * @param remoteInstance the remoteInstance to set
     */
    protected void setRemoteInstance(RemoteInstanceItem remoteInstance) {
        this.remoteInstance = remoteInstance;
    }

    /**
     * Public set remoteInstance method to update the composite key as well.
     * @param remoteInstance Part of the composite key - FK
     */
    public void setRemoteInstanceExternal(RemoteInstanceItem remoteInstance) {
        setRemoteInstance(remoteInstance);
        this.id = new DiscourseInstanceMapId(this.discourse, this.remoteInstance);
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
        buffer.append(objectToStringFK("Discourse", getDiscourse()));
        buffer.append(objectToStringFK("RemoteInstance", getRemoteInstance()));
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
        if (obj instanceof DiscourseInstanceMapItem) {
            DiscourseInstanceMapItem otherItem = (DiscourseInstanceMapItem)obj;

            if (!objectEqualsFK(this.getDiscourse(), otherItem.getDiscourse())) {
                return false;
            }
            if (!objectEqualsFK(this.getRemoteInstance(), otherItem.getRemoteInstance())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDiscourse());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getRemoteInstance());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>discourse</li>
     * <li>remoteInstance</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscourseInstanceMapItem otherItem = (DiscourseInstanceMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDiscourse(), otherItem.getDiscourse());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getRemoteInstance(), otherItem.getRemoteInstance());
        if (value != 0) { return value; }

        return value;
    }
}
