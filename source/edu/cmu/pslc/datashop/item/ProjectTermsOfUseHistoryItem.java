/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Student Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ProjectTermsOfUseHistoryItem extends Item
        implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this project terms of use history . */
    private Integer id;

    /** Project for which this terms of use history is related. */
    private ProjectItem project;

    /** Version for which this terms of use history is related. */
    private TermsOfUseVersionItem termsOfUseVersion;

    /** Date for which the this terms of use was made effective. */
    private Date effectiveDate;

    /** Date for which the this terms of use was retired. */
    private Date expireDate;

    /** Default constructor. */
    public ProjectTermsOfUseHistoryItem() {
    }

    /**
     *  Constructor with id.
     *  @param historyId Database generated unique Id for this history.
     */
    public ProjectTermsOfUseHistoryItem(Integer historyId) {
        this.id = historyId;
    }

    /**
     * Returns the id.
     * @return the Long id as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set historyId.
     * @param historyId Database generated unique Id for this history.
     */
    public void setId(Integer historyId) {
        this.id = historyId;
    }

    /**
     * Get project.
     * @return the project item
     */
    public ProjectItem getProject() {
        return this.project;
    }

    /**
     * Set project.
     * @param project Project for which this terms of use history is associated.
     */
    public void setProject(ProjectItem project) {
        this.project = project;
    }

    /**
     * Get termsOfUseVersion.
     * @return the terms of use version item
     */
    public TermsOfUseVersionItem getTermsOfUseVersion() {
        return this.termsOfUseVersion;
    }

    /**
     * Set historyName.
     * @param termsOfUseVersion Version of the terms of use for the history.
     */
    public void setTermsOfUseVersion(TermsOfUseVersionItem termsOfUseVersion) {
        this.termsOfUseVersion = termsOfUseVersion;
    }

    /**
     * Get effectiveDate.
     * @return the effective date
     */
    public Date getEffectiveDate() {
        return this.effectiveDate;
    }

    /**
     * Set effectiveDate.
     * @param effectiveDate Effective date of the terms of use for the history.
     */
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * Get expireDate.
     * @return the expiration date
     */
    public Date getExpireDate() {
        return this.expireDate;
    }

    /**
     * Set expireDate.
     * @param expireDate Expire date of the terms of use for the history.
     */
    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
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
        buffer.append(objectToStringFK("TermsOfUseVersion", getTermsOfUseVersion()));
        buffer.append(objectToStringFK("Project", getProject()));
        buffer.append(objectToString("EffectiveDate", getEffectiveDate()));
        buffer.append(objectToString("ExpireDate", getExpireDate()));
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
        if (obj instanceof ProjectTermsOfUseHistoryItem) {
            ProjectTermsOfUseHistoryItem otherItem = (ProjectTermsOfUseHistoryItem)obj;

            if (!objectEqualsFK(this.getTermsOfUseVersion(), otherItem.getTermsOfUseVersion())) {
                return false;
            }
            if (!objectEqualsFK(this.getProject(), otherItem.getProject())) {
                return false;
            }
            if (!objectEquals(this.getEffectiveDate(), otherItem.getEffectiveDate())) {
                return false;
            }
            if (!objectEquals(this.getExpireDate(), otherItem.getExpireDate())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getTermsOfUseVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProject());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEffectiveDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getExpireDate());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>project</li>
     * <li>version</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProjectTermsOfUseHistoryItem otherItem = (ProjectTermsOfUseHistoryItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getProject(), otherItem.getProject());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTermsOfUseVersion(), otherItem.getTermsOfUseVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEffectiveDate(), otherItem.getEffectiveDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getExpireDate(), otherItem.getExpireDate());
        if (value != 0) { return value; }

        return value;
    }


}