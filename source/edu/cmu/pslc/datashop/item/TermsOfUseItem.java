/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A Terms of Use.
 *
 * @author Shanwen Yu
 * @version $Revision: 7423 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-16 15:32:17 -0500 (Fri, 16 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TermsOfUseItem extends Item implements java.io.Serializable, Comparable   {

    /** Constant for the name of the DataShop terms of use. */
    public static final String DATASHOP_TERMS = "DataShop";
    /** Constant for the name of the DataShop terms of use. */
    public static final String PROJECT_TERMS = "Project";

    /** Database generated unique Id for this term. */
    private Integer id;
    /** Name of the term */
    private String name;
    /** The timestamp the term was added. */
    private Date createdDate;
    /** The flag indicates whether the terms is retired. */
    private Boolean retiredFlag;
    /** Collection of ProjectTermsOfUseMap associated with this project. */
    private Set projectTermsOfUseMap;
    /** Collection of versions for this terms of use. */
    private Set termsOfUseVersions;

    /** Default constructor. */
    public TermsOfUseItem() {  }

    /**
     * Constructor that takes the termId.
     * @param termId the term Id of this term.
     */
    public TermsOfUseItem(Integer termId) { this.id = termId; }

    /**
     * Get the problem id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set term Id.
     * @param termId Database generated unique Id for this term.
     */
    public void setId(Integer termId) {
        this.id = termId;
    }

    /**
     * Returns createdDate.
     * @return Returns the createdDate.
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Set createdDate.
     * @param createdDate The createdDate to set.
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Returns name.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set name.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set retiredFlag.
     * @param retiredFlag The retiredFlag to set.
     */
    public void setRetiredFlag(Boolean retiredFlag) {
        this.retiredFlag = retiredFlag;
    }

    /**
     * Returns retiredFlag.
     * @return Returns the retiredFlag.
     */
    public Boolean getRetiredFlag() {
        return retiredFlag;
    }


    /**
     * Get projectTermsOfUseMap.
     * @return a set of projectTermsOfUseMap
     */
    protected Set getProjectTermsOfUseMap() {
        if (this.projectTermsOfUseMap == null) {
            this.projectTermsOfUseMap = new HashSet();
        }
        return this.projectTermsOfUseMap;
    }

    /**
     * Public method to get terms of use.
     * @return a list of terms of use
     */
    public List<ProjectTermsOfUseMapItem> getProjectTermsOfUseMapExternal() {
        List sortedList = new ArrayList(getProjectTermsOfUseMap());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a project terms of use mapping.
     * @param item project terms of use mapping to add
     */
    public void addProjectTermsOfUseMap(ProjectTermsOfUseMapItem item) {
        if (!getProjectTermsOfUseMap().contains(item)) {
            getProjectTermsOfUseMap().add(item);
        }
    }

    /**
     * Remove a terms of use.
     * @param item terms of use to remove
     */
    public void removeProjectTermsOfUseMap(ProjectTermsOfUseMapItem item) {
        if (getProjectTermsOfUseMap().contains(item)) {
            getProjectTermsOfUseMap().remove(item);
        }
    }

    /**
     * Set projectTermsOfUseMap.
     * @param projectTermsOfUseMap Collection of ProjectTermsOfUseMap items
     *  associated with this project.
     */
    public void setProjectTermsOfUseMap(Set projectTermsOfUseMap) {
        this.projectTermsOfUseMap = projectTermsOfUseMap;
    }

    /**
     * Get TermsOfUseVersions.
     * @return set of term of use version items
     */
    protected Set getTermsOfUseVersions() {
        if (this.termsOfUseVersions == null) {
            this.termsOfUseVersions = new HashSet();
        }
        return this.termsOfUseVersions;
    }

    /**
     * Public method to get TermsOfUseVersions.
     * @return a list instead of a set
     */
    public List getTermsOfUseVersionsExternal() {
        List sortedList = new ArrayList<TermsOfUseVersionItem>(getTermsOfUseVersions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a termsOfUseVersion.
     * @param item termsOfUseVersion to add
     */
    public void addTermsOfUseVersion(TermsOfUseVersionItem item) {
        getTermsOfUseVersions().add(item);
        item.setTermsOfUse(this);
    }

    /**
     * Remove a termsOfUseVersion.
     * @param item termsOfUseVersion to remove
     */
    public void removeTermsOfUseVersion(TermsOfUseVersionItem item) {
        getTermsOfUseVersions().remove(item);
        item.setTermsOfUse(null);
    }

    /**
     * Set TermsOfUseVersions.
     * @param termsOfUseVersions Collection of termsOfUseVersions for this termsOfUse.
     */
    public void setTermsOfUseVersions(Set termsOfUseVersions) {
        this.termsOfUseVersions = termsOfUseVersions;
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
         buffer.append(objectToString("TermsOfUseId", getId()));
         buffer.append(objectToString("Name", getName()));
         buffer.append(objectToString("CreatedDate", getCreatedDate()));
         buffer.append(objectToString("RetiredFlag", getRetiredFlag()));
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
        if (obj instanceof TermsOfUseItem) {
            TermsOfUseItem otherItem = (TermsOfUseItem)obj;

            if (!objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }
            if (!objectEquals(this.getCreatedDate(), otherItem.getCreatedDate())) {
                return false;
            }
            if (!objectEquals(this.getRetiredFlag(), otherItem.getRetiredFlag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreatedDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getRetiredFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>Name</li>
     * <li>Created Date</li>
     * <li>Retired Flag</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        TermsOfUseItem otherItem = (TermsOfUseItem)obj;
        int value = 0;

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCreatedDate(), otherItem.getCreatedDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getRetiredFlag(), otherItem.getRetiredFlag());
        if (value != 0) { return value; }
        return value;
    }
}
