/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the
 * contribution_partof_discourse_part relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Cindy Tipper
 * @version $Revision: 12724 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-11-05 13:30:26 -0500 (Thu, 05 Nov 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContributionDiscoursePartMapId implements Serializable, Comparable {

    /** The contribution id (FK). */
    private Long contributionId;
    /** The discourse discoursePart id (FK). */
    private Long discoursePartId;

    /**
     * Constructor.
     */
    public ContributionDiscoursePartMapId() { };

    /**
     * Full constructor.
     * @param contributionItem the contribution item for this composite key.
     * @param discoursePartItem the discourse discoursePart item for this composite key.
     */
    public ContributionDiscoursePartMapId(ContributionItem contributionItem,
                                          DiscoursePartItem discoursePartItem) {
        if (contributionItem != null) {
            this.contributionId = (Long)contributionItem.getId();
        }
        if (discoursePartItem != null) {
            this.discoursePartId = (Long)discoursePartItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param contributionId the contribution ID for this composite key.
     * @param discoursePartId the contribution part ID for this composite key.
     */
    public ContributionDiscoursePartMapId(Long contributionId, Long discoursePartId) {
        this.contributionId = contributionId;
        this.discoursePartId = discoursePartId;
    }

    /**
     * Get the contributionId.
     * @return the contributionId
     */
    public Long getContributionId() { return this.contributionId; }

    /**
     * Set the contributionId.
     * @param contributionId the contributionId to set
     */
    public void setContributionId(Long contributionId) {
        this.contributionId = contributionId;
    }

    /**
     * Get the discoursePartId.
     * @return the discoursePartId.
     */
    public Long getDiscoursePartId() { return discoursePartId; }

    /**
     * Set the discoursePartId.
     * @param discoursePartId the discoursePartId to set.
     */
    public void setDiscoursePartId(Long discoursePartId) {
        this.discoursePartId = discoursePartId;
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
         buffer.append("contributionId").append("='").append(contributionId).append("' ");
         buffer.append("discoursePartId").append("='").append(discoursePartId).append("' ");
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            ContributionDiscoursePartMapId otherItem = (ContributionDiscoursePartMapId)obj;
            if (!this.contributionId.equals(otherItem.getContributionId())) {
                return false;
            }
            if (!this.discoursePartId.equals(otherItem.getDiscoursePartId())) {
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
        int hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME +
            (contributionId != null ? contributionId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME +
            (discoursePartId != null ? discoursePartId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>contribution id</li>
     * <li>discoursePart id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ContributionDiscoursePartMapId otherItem = (ContributionDiscoursePartMapId)obj;
        int value = 0;

        if ((getContributionId() != null) && (otherItem.getContributionId() != null)) {
            value = getContributionId().compareTo(otherItem.getContributionId());
        } else if (getContributionId() != null) {
            value = 1;
        } else if (otherItem.getContributionId() != null) {
            value = -1;
        }
        if (value != 0) { return value; }

        if ((getDiscoursePartId() != null) && (otherItem.getDiscoursePartId() != null)) {
            value = getDiscoursePartId().compareTo(otherItem.getDiscoursePartId());
        } else if (getDiscoursePartId() != null) {
            value = 1;
        } else if (otherItem.getDiscoursePartId() != null) {
            value = -1;
        }

        return value;
    }

}
