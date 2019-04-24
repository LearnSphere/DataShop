/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A terms of use version object represents terms_of_use_version table.
 *
 * @author Shanwen Yu
 * @version $Revision: 7358 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2011-11-28 14:30:11 -0500 (Mon, 28 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TermsOfUseVersionItem extends Item implements java.io.Serializable, Comparable   {

    /** Terms of use version status type enumerated field value - "saved". */
    public static final String TERMS_OF_USE_VERSION_STATUS_SAVED = "saved";
    /** Terms of use version status type enumerated field value - "applied". */
    public static final String TERMS_OF_USE_VERSION_STATUS_APPLIED = "applied";
    /** Terms of use version status type enumerated field value - "archived". */
    public static final String TERMS_OF_USE_VERSION_STATUS_ARCHIVED = "archived";
    /** Terms of use action status type enumerated field value - "applied to". */
    public static final String ACTION_APPLIED = "applied to";
    /** Terms of use action status type enumerated field value - "cleared from". */
    public static final String ACTION_CLEARED = "cleared from";
    /** Terms of use action status type enumerated field value - "updated for". */
    public static final String ACTION_UPDATED = "updated for";

    /** Collection of all allowed items in the status enumeration. */
    private static final List TERMS_OF_USE_VERSION_STATUS_ENUM = new ArrayList();

    /**
     * Adds each message type to the enumerated list.
     */
    static {
        TERMS_OF_USE_VERSION_STATUS_ENUM.add(TERMS_OF_USE_VERSION_STATUS_SAVED);
        TERMS_OF_USE_VERSION_STATUS_ENUM.add(TERMS_OF_USE_VERSION_STATUS_APPLIED);
        TERMS_OF_USE_VERSION_STATUS_ENUM.add(TERMS_OF_USE_VERSION_STATUS_ARCHIVED);
    }

    /** Database generated unique Id for this terms of use version. */
    private Integer id;
    /** Terms of Use item associated with this version. */
    private TermsOfUseItem termsOfUse;
    /** Version number. */
    private Integer version;
    /** Actual terms of use. */
    private String terms;
    /** Status of this term. */
    private String status;
    /** The timestamp the term was saved. */
    private Date savedDate;
    /** The timestamp the term was applied. */
    private Date appliedDate;
    /** The timestamp the term was archived. */
    private Date archivedDate;

    /** Default constructor. */
    public TermsOfUseVersionItem() {  }

    /**
     * Constructor that takes the termOfUseVersionId.
     * @param termOfUseVersionId the database Id of this term of use version.
     */
    public TermsOfUseVersionItem(Integer termOfUseVersionId) { this.id = termOfUseVersionId; }

    /**
     * Get the problem id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set file Id.
     * @param termOfUseVersionId Database generated unique Id for this terms of use version.
     */
    public void setId(Integer termOfUseVersionId) {
        this.id = termOfUseVersionId;
    }

    /**
     * Returns termsOfUse.
     * @return Returns the termsOfUse.
     */
    public TermsOfUseItem getTermsOfUse() {
        return termsOfUse;
    }

    /**
     * Set termsOfUse.
     * @param termsOfUse The termsOfUse to set.
     */
    public void setTermsOfUse(TermsOfUseItem termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

    /**
     * Returns version.
     * @return Returns the version.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Set version.
     * @param version The version to set.
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * Returns terms.
     * @return Returns the terms.
     */
    public String getTerms() {
        return terms;
    }

    /**
     * Set terms.
     * @param terms The terms to set.
     */
    public void setTerms(String terms) {
        this.terms = terms;
    }

    /**
     * Returns status.
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set status.
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
        if (TermsOfUseVersionItem.isValidStatus(status)) {
            this.status = status;
        } else {
            throw new LogException("Terms of Use Version status can only be "
                    + TermsOfUseVersionItem.getStatuses());
        }
    }

    /**
     * Check for valid terms of use version status.
     * @param status the terms of use version status, saved, applied or archived
     * @return true if terms of use version statuses is valid; false otherwise
     */
    public static boolean isValidStatus(String status) {
        if (TERMS_OF_USE_VERSION_STATUS_ENUM.contains(status)) {
            return true;
        }
        return false;
    }

    /**
     * Return a list of the valid terms of use version statuses.
     * @return an unmodifiable list of the terms of use version statuses
     */
    public static List getStatuses() {
        return Collections.unmodifiableList(TERMS_OF_USE_VERSION_STATUS_ENUM);
    }

    /**
     * Returns savedDate.
     * @return Returns the savedDate.
     */
    public Date getSavedDate() {
        return savedDate;
    }

    /**
     * Set savedDate.
     * @param savedDate The savedDate to set.
     */
    public void setSavedDate(Date savedDate) {
        this.savedDate = savedDate;
    }

    /**
     * Returns appliedDate.
     * @return Returns the appliedDate.
     */
    public Date getAppliedDate() {
        return appliedDate;
    }

    /**
     * Set appliedDate.
     * @param appliedDate The appliedDate to set.
     */
    public void setAppliedDate(Date appliedDate) {
        this.appliedDate = appliedDate;
    }

    /**
     * Returns archivedDate.
     * @return Returns the archivedDate.
     */
    public Date getArchivedDate() {
        return archivedDate;
    }

    /**
     * Set archivedDate.
     * @param archivedDate The archivedDate to set.
     */
    public void setArchivedDate(Date archivedDate) {
        this.archivedDate = archivedDate;
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
         buffer.append(objectToString("TermsOfUseVersionId", getId()));
         buffer.append(objectToStringFK("TermsOfUse", getTermsOfUse()));
         buffer.append(objectToString("Version", getVersion()));
         buffer.append(objectToString("Terms", getTerms()));
         buffer.append(objectToString("Status", getStatus()));
         buffer.append(objectToString("SavedDate", getSavedDate()));
         buffer.append(objectToString("AppliedDate", getAppliedDate()));
         buffer.append(objectToString("ArchivedDate", getArchivedDate()));
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
        if (obj instanceof TermsOfUseVersionItem) {
            TermsOfUseVersionItem otherItem = (TermsOfUseVersionItem)obj;

            if (!objectEqualsFK(this.getTermsOfUse(), otherItem.getTermsOfUse())) {
                return false;
            }
            if (!objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!objectEquals(this.getTerms(), otherItem.getTerms())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!objectEquals(this.getSavedDate(), otherItem.getSavedDate())) {
                return false;
            }
            if (!objectEquals(this.getAppliedDate(), otherItem.getAppliedDate())) {
                return false;
            }
            if (!objectEquals(this.getArchivedDate(), otherItem.getArchivedDate())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getTermsOfUse());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTerms());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSavedDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAppliedDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getArchivedDate());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>Terms of Use</li>
     * <li>Version</li>
     * <li>Terms</li>
     * <li>Status</li>
     * <li>Saved Date</li>
     * <li>Applied Date</li>
     * <li>Archived Date</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        TermsOfUseVersionItem otherItem = (TermsOfUseVersionItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getTermsOfUse(), otherItem.getTermsOfUse());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVersion(), otherItem.getVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTerms(), otherItem.getTerms());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSavedDate(), otherItem.getSavedDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAppliedDate(), otherItem.getAppliedDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getArchivedDate(), otherItem.getArchivedDate());
        if (value != 0) { return value; }

        return value;
    }

}
