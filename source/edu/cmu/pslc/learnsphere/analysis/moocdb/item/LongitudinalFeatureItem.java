/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a LongitudinalFeature instance.
 *
 * @author 
 * @version $Revision: 14073 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-05-15 14:48:12 -0400 (Mon, 15 May 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LongitudinalFeatureItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this LongitudinalFeature. */
    private Integer id;
    /** name of LongitudinalFeature*/
    private String name;
    /** description of LongitudinalFeature*/
    private String description;
    
    /** Default constructor. */
    public LongitudinalFeatureItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id for this LongitudinalFeature.
     */
    public LongitudinalFeatureItem(Integer id) {
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
     * @param Database generated unique Id for this LongitudinalFeature.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /** Returns name. @return Returns name. */
    public String getName() {
        return name;
    }

    /** Set name. @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }
    
    /** Returns description. @return Returns description. */
    public String getDescription() {
        return description;
    }

    /** Set description. @param description The description to set. */
    public void setDescription(String description) {
        this.description = description;
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
        buffer.append(objectToString("name", getName()));
        buffer.append(objectToString("description", getDescription()));
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
        if (obj instanceof LongitudinalFeatureItem) {
            LongitudinalFeatureItem otherItem = (LongitudinalFeatureItem)obj;
            if (!objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }
            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
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
        LongitudinalFeatureItem otherItem = (LongitudinalFeatureItem)obj;
        int value = 0;

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        return value;
    }
}
