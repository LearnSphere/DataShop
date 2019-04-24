/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the project_terms_of_use_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Mike Komisin
 * @version $Revision: 7295 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2011-11-17 15:16:22 -0500 (Thu, 17 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectTermsOfUseMapId implements Serializable, Comparable {

    /** The project id.  Project id serves as a FK into the project relation. */
    private Integer projectId;
    /** The terms of use id.  Terms of use id serves as a FK into the terms of use relation. */
    private Integer termsOfUseId;

    /**
     * Constructor.
     */
    public ProjectTermsOfUseMapId() { };

    /**
     * Parameterized constructor.
     * @param project the project item for this composite key.
     * @param termsOfUse the termsOfUse for this composite key.
     */
    public ProjectTermsOfUseMapId(ProjectItem project, TermsOfUseItem termsOfUse) {
        if (project != null) {
            this.projectId = new Integer((Integer)project.getId());
        }
        if (termsOfUse != null) {
            this.termsOfUseId = new Integer((Integer)termsOfUse.getId());
        }
    }

    /**
     * Parameterized constructor.
     * @param projectId the projectId for this project.
     * @param termsOfUseId the termsOfUseId for this termsOfUse.
     */
    public ProjectTermsOfUseMapId(Integer projectId, Integer termsOfUseId) {
        this.projectId = new Integer(projectId);
        this.termsOfUseId = new Integer(termsOfUseId);
    }

    /**
     * Get the projectId.
     * @return the projectId
     */
    public Integer getProjectId() {
        return new Integer(projectId);
    }

    /**
     * Set the projectId.
     * @param projectId the projectId to set
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Get the termsOfUseId.
     * @return the termsOfUseId.
     */
    public Integer getTermsOfUseId() {
        return new Integer(termsOfUseId);
    }

    /**
     * Set the termsOfUseId.
     * @param termsOfUseId the termsOfUseId to set.
     */
    public void setTermsOfUseId(Integer termsOfUseId) {
        this.termsOfUseId = termsOfUseId;
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
         buffer.append("termsOfUseId").append("='").append(termsOfUseId).append("' ");
         buffer.append("projectId").append("='").append(projectId).append("' ");
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
            ProjectTermsOfUseMapId otherItem = (ProjectTermsOfUseMapId)obj;

            if (!this.termsOfUseId.equals(otherItem.getTermsOfUseId())) {
                return false;
            }
            if (!this.projectId.equals(otherItem.getProjectId())) {
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
        hash = hash * UtilConstants.HASH_PRIME
            + (termsOfUseId != null ? termsOfUseId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (projectId != null ? projectId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>termsOfUse id</li>
     * <li>project id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProjectTermsOfUseMapId otherItem = (ProjectTermsOfUseMapId)obj;
        if (this.termsOfUseId.compareTo(otherItem.termsOfUseId) != 0) {
            return this.termsOfUseId.compareTo(otherItem.termsOfUseId);
        }
        if (this.projectId.compareTo(otherItem.projectId) != 0) {
            return this.projectId.compareTo(otherItem.projectId);
        }
        return 0;
    }

} // end ProjectTermsOfUseMapId.java
