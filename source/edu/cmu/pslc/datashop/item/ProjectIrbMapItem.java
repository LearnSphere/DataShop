/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between an IRB and a project.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectIrbMapItem extends Item implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private ProjectIrbMapId id;

    /** The Project item associated with this item. */
    private ProjectItem project;

    /** The IRB item associated with this item. */
    private IrbItem irb;

    /** The user that added this association. */
    private UserItem addedBy;

    /** The date this association was added. */
    private Date addedTime;

    /** Default constructor. */
    public ProjectIrbMapItem() { };

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
    public void setId(ProjectIrbMapId id) {
        this.id = id;
    }

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
     * Public set tool method to update the composite key as well.
     * @param projectItem Part of the composite key - FK
     */
    public void setProjectExternal(ProjectItem projectItem) {
        setProject(projectItem);
        this.id = new ProjectIrbMapId(this.project, this.irb);
    }

    /**
     * Get the IRB.
     * @return the irb
     */
    public IrbItem getIrb() {
        return irb;
    }

    /**
     * Set the IRB.
     * @param irb the IRB to set
     */
    protected void setIrb(IrbItem irb) {
        this.irb = irb;
    }

    /**
     * Public set IRB method to update the composite key as well.
     * @param irb Part of the composite key - FK
     */
    public void setIrbExternal(IrbItem irb) {
        setIrb(irb);
        this.id = new ProjectIrbMapId(this.project, this.irb);
    }

    /**
     * Get the user that added this association.
     * @return the addedBy
     */
    public UserItem getAddedBy() {
        return addedBy;
    }

    /**
     * Set the user that added this association.
     * @param addedBy the user
     */
    public void setAddedBy(UserItem addedBy) {
        this.addedBy = addedBy;
    }

    /**
     * Get the date this association was added.
     * @return Date addedTime
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set the date this association was added.
     * @param addedTime date association was added
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
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
        buffer.append(objectToStringFK("Project", getProject()));
        buffer.append(objectToStringFK("Irb", getIrb()));
        buffer.append(objectToStringFK("AddedBy", getAddedBy()));
        buffer.append(objectToString("AddedTime", getAddedTime()));
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
        if (obj instanceof ProjectIrbMapItem) {
            ProjectIrbMapItem otherItem = (ProjectIrbMapItem)obj;

            if (!objectEqualsFK(this.getProject(), otherItem.getProject())) {
                return false;
            }
            if (!objectEqualsFK(this.getIrb(), otherItem.getIrb())) {
                return false;
            }
            if (!objectEqualsFK(this.getAddedBy(), otherItem.getAddedBy())) {
                return false;
            }
            if (!objectEquals(this.getAddedTime(), otherItem.getAddedTime())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getProject());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getIrb());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getAddedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getAddedTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>project</li>
     * <li>irb</li>
     * <li>downloads</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProjectIrbMapItem otherItem = (ProjectIrbMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getProject(), otherItem.getProject());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getIrb(), otherItem.getIrb());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getAddedBy(), otherItem.getAddedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAddedTime(), otherItem.getAddedTime());
        if (value != 0) { return value; }

        return value;
    }
}
