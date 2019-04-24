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
 * Represents a mapping between a Discourse and a DiscoursePart.
 *
 * @author Cindy Tipper
 * @version $Revision: 12724 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-11-05 13:30:26 -0500 (Thu, 05 Nov 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDiscoursePartMapItem
    extends Item
    implements java.io.Serializable, Comparable
{
    /** The primary (composite) key for this item. */
    private DiscourseDiscoursePartMapId id;
    /** The Discourse item associated with this key. */
    private DiscourseItem discourse;
    /** The DiscoursePart item associated with this key. */
    private DiscoursePartItem part;

    /** Default constructor. */
    public DiscourseDiscoursePartMapItem() {}

    /**
     * Get the id.
     * @return the id
     */
    public Comparable getId() { return this.id; }

    /**
     * Set the id.
     * @param id the id
     */
    public void setId(DiscourseDiscoursePartMapId id) {
        this.id = id;
    }

    /**
     * Get the discourse.
     * @return the discourse
     */
    public DiscourseItem getDiscourse() {
        return this.discourse;
    }

    /**
     * Set the discourse.
     * @param discourse the DiscourseItem
     */
    protected void setDiscourse(DiscourseItem discourse) { this.discourse = discourse; }

    /**
     * Public set method for the Discourse that will also update the composite key.
     * @param discourse the DiscourseItem
     */
    public void setDiscourseExternal(DiscourseItem discourse) {
        setDiscourse(discourse);
        this.id = new DiscourseDiscoursePartMapId(this.discourse, this.part);
    }

    /**
     * Get the part
     * @return the part
     */
    public DiscoursePartItem getPart() {
        return this.part;
    }

    /**
     * Set the part.
     * @param user the DiscoursePartItem
     */
    protected void setPart(DiscoursePartItem part) { this.part = part; }

    /**
     * Public set method for the DiscoursePart that will also update the composite key.
     * @param part the DiscoursePartItem
     */
    public void setPartExternal(DiscoursePartItem part) {
        setPart(part);
        this.id = new DiscourseDiscoursePartMapId(this.discourse, this.part);
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
         buffer.append(objectToString("Discourse", getDiscourse()));
         buffer.append(objectToString("Part", getPart()));
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
        if (obj instanceof DiscourseDiscoursePartMapItem) {
            DiscourseDiscoursePartMapItem otherItem = (DiscourseDiscoursePartMapItem)obj;

            if (!objectEqualsFK(this.getDiscourse(), otherItem.getDiscourse())) {
                return false;
            }
            if (!objectEqualsFK(this.getPart(), otherItem.getPart())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getPart());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>discourse</li>
     * <li>part</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscourseDiscoursePartMapItem otherItem = (DiscourseDiscoursePartMapItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getDiscourse(), otherItem.getDiscourse());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getPart(), otherItem.getPart());
        if (value != 0) { return value; }

        return value;
    }
}