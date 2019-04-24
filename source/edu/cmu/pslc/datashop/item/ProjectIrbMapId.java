/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the project_irb_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectIrbMapId implements Serializable, Comparable {

    /** The project id (FK). */
    private Integer projectId;
    /** The irb id (FK). */
    private Integer irbId;

    /**
     * Constructor.
     */
    public ProjectIrbMapId() { };

    /**
     * Full constructor.
     * @param projectItem the project item for this composite key.
     * @param irbItem the IRB item for this composite key.
     */
    public ProjectIrbMapId(ProjectItem projectItem, IrbItem irbItem) {
        if (projectItem != null) {
            this.projectId = (Integer)projectItem.getId();
        }
        if (irbItem != null) {
            this.irbId = (Integer)irbItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param projectId the project ID for this composite key.
     * @param irbId the IRB ID for this composite key.
     */
    public ProjectIrbMapId(Integer projectId, Integer irbId) {
        this.projectId = projectId;
        this.irbId = irbId;
    }

    /**
     * Get the projectId.
     * @return the projectId
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Set the projectId.
     * @param projectId the projectId to set
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Get the irbId.
     * @return the irbId.
     */
    public Integer getIrbId() {
        return irbId;
    }

    /**
     * Set the irbId.
     * @param irbId the irbId to set.
     */
    public void setIrbId(Integer irbId) {
        this.irbId = irbId;
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
         buffer.append("projectId").append("='").append(projectId).append("' ");
         buffer.append("irbId").append("='").append(irbId).append("' ");
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
            ProjectIrbMapId otherItem = (ProjectIrbMapId)obj;
            if (!this.projectId.equals(otherItem.getProjectId())) {
                return false;
            }
            if (!this.irbId.equals(otherItem.getIrbId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (projectId != null ? projectId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME + (irbId != null ? irbId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>project id</li>
     * <li>irb id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProjectIrbMapId otherItem = (ProjectIrbMapId)obj;
        if (this.projectId.compareTo(otherItem.projectId) != 0) {
            return this.projectId.compareTo(otherItem.projectId);
        }
        if (this.irbId.compareTo(otherItem.irbId) != 0) {
            return this.irbId.compareTo(otherItem.irbId);
        }
        return 0;
    }

}
