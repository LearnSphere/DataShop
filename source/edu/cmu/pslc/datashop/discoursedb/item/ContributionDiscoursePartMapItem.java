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
 * Represents a mapping between a Contribution and a DiscoursePart.
 *
 * @author Cindy Tipper
 * @version $Revision: 12724 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-11-05 13:30:26 -0500 (Thu, 05 Nov 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContributionDiscoursePartMapItem
    extends Item
    implements java.io.Serializable, Comparable
{
    /** The primary (composite) key for this item. */
    private ContributionDiscoursePartMapId id;
    /** The Contribution item associated with this key. */
    private ContributionItem contribution;
    /** The DiscoursePart item associated with this key. */
    private DiscoursePartItem discoursePart;

    /** Default constructor. */
    public ContributionDiscoursePartMapItem() {}

    /**
     * Get the id.
     * @return the id
     */
    public Comparable getId() { return this.id; }

    /**
     * Set the id.
     * @param id the id
     */
    public void setId(ContributionDiscoursePartMapId id) {
        this.id = id;
    }

    /**
     * Get the contribution.
     * @return the contribution
     */
    public ContributionItem getContribution() {
        return this.contribution;
    }

    /**
     * Set the contribution.
     * @param contribution the ContributionItem
     */
    protected void setContribution(ContributionItem contribution) {
        this.contribution = contribution;
    }

    /**
     * Public set method for the Contribution that will also update the composite key.
     * @param contribution the ContributionItem
     */
    public void setContributionExternal(ContributionItem contribution) {
        setContribution(contribution);
        this.id = new ContributionDiscoursePartMapId(this.contribution, this.discoursePart);
    }

    /**
     * Get the discoursePart
     * @return the discoursePart
     */
    public DiscoursePartItem getDiscoursePart() {
        return this.discoursePart;
    }

    /**
     * Set the discoursePart.
     * @param user the DiscoursePartItem
     */
    protected void setDiscoursePart(DiscoursePartItem discoursePart) {
        this.discoursePart = discoursePart;
    }

    /**
     * Public set method for the DiscoursePart that will also update the composite key.
     * @param discoursePart the DiscoursePartItem
     */
    public void setDiscoursePartExternal(DiscoursePartItem discoursePart) {
        setDiscoursePart(discoursePart);
        this.id = new ContributionDiscoursePartMapId(this.contribution, this.discoursePart);
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually discoursePart of this class.
     * @return a string representation of this item
     */
    public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append(objectToString("Contribution", getContribution()));
         buffer.append(objectToString("DiscoursePart", getDiscoursePart()));
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
        if (obj instanceof ContributionDiscoursePartMapItem) {
            ContributionDiscoursePartMapItem otherItem = (ContributionDiscoursePartMapItem)obj;

            if (!objectEqualsFK(this.getContribution(), otherItem.getContribution())) {
                return false;
            }
            if (!objectEqualsFK(this.getDiscoursePart(), otherItem.getDiscoursePart())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getContribution());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDiscoursePart());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>contribution</li>
     * <li>discoursePart</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ContributionDiscoursePartMapItem otherItem = (ContributionDiscoursePartMapItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getContribution(), otherItem.getContribution());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDiscoursePart(), otherItem.getDiscoursePart());
        if (value != 0) { return value; }

        return value;
    }
}