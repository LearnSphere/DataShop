/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;
import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a termsOfUse project map record of the system.
 *
 * @author Mike Komisin
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectTermsOfUseMapItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private ProjectTermsOfUseMapId id;

    /** The termsOfUse for this item. */
    private TermsOfUseItem termsOfUse;

    /** The project for this item. */
    private ProjectItem project;

    /** The effective date for this item. */
    private Date effectiveDate;

    /**
     * Get the project.
     * @return the project
     */
    public ProjectItem getProject() {
        return project;
    }

    /**
     * Set the project.
     * @param project the project to set
     */
    protected void setProject(ProjectItem project) {
        this.project = project;
    }

    /**
     * Public Set project method to update the composite key as well.
     * @param project Part of the composite key - FK to the project table.
     */
    public void setProjectExternal(ProjectItem project) {
        setProject(project);
        this.id = new ProjectTermsOfUseMapId(this.project, this.termsOfUse);
    }

    /**
     * Get the termsOfUse.
     * @return the termsOfUse
     */
    public TermsOfUseItem getTermsOfUse() {
        return termsOfUse;
    }

    /**
     * Set the termsOfUse.
     * @param termsOfUse the termsOfUse to set
     */
    protected void setTermsOfUse(TermsOfUseItem termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

    /**
     * Set the effectiveDate.
     * @return the effective date
     */
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * Set the effectiveDate.
     * @param effectiveDate the effectiveDate to set
     */
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * Public Set termsOfUse method to update the composite key as well.
     * @param termsOfUse Part of the composite key - FK to the tutor_termsOfUse table.
     */
    public void setTermsOfUseExternal(TermsOfUseItem termsOfUse) {
        setTermsOfUse(termsOfUse);
        this.id = new ProjectTermsOfUseMapId(this.project, this.termsOfUse);
    }

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the TermsOfUseProjectMapId.
     * @param id the id to set
     */
    public void setId(ProjectTermsOfUseMapId id) {
        this.id = id;
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
        buffer.append(objectToStringFK("TermsOfUse", this.getTermsOfUse()));
        buffer.append(objectToStringFK("Project", this.getProject()));
        buffer.append(objectToString("EffectiveDate", this.getEffectiveDate()));
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
        if (obj instanceof ProjectTermsOfUseMapItem) {
            ProjectTermsOfUseMapItem otherItem = (ProjectTermsOfUseMapItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEquals(this.getEffectiveDate(), otherItem.getEffectiveDate())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getEffectiveDate());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>termsOfUse</li>
     * <li>project</li>
     * <li>initialPKnown</li>
     * <li>resultingPKnown</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProjectTermsOfUseMapItem otherItem = (ProjectTermsOfUseMapItem)obj;

        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEffectiveDate(), otherItem.getEffectiveDate());
        if (value != 0) { return value; }

        return value;
    }

} // end TermsOfUseProjectEventItem.java
