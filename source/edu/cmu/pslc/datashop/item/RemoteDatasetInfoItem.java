/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Meta-data for a remote DatasetItem.
 * Very similar to a DTO in that it represents data from remote DB.
 *
 * @author Cindy Tipper
 * @version $Revision: 13099 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-14 12:44:17 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RemoteDatasetInfoItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this item. */
    private Long id;
    /** DatasetItem that this meta-data represents. */
    private DatasetItem dataset;

    /** Project name. */
    private String projectName;
    /** PI name. */
    private String piName;
    /** DP name. */
    private String dpName;
    /** Access level. */
    private String accessLevel;
    /** Public flag. */
    private Boolean isPublic;
    /** Citation: preferred paper. */
    private String citation;

    // Statistics
    /** Number of students. */
    private Long numStudents;
    /** Number of student hours. */
    private Double numStudentHours;
    /** Number of unique steps. */
    private Long numUniqueSteps;
    /** Number of steps. */
    private Long numSteps;
    /** Number of transactions. */
    private Long numTransactions;
    /** Number of samples. */
    private Long numSamples;

    // Skill Models
    private Set<RemoteSkillModelItem> skillModels;

    /** Default constructor. */
    public RemoteDatasetInfoItem() {}

    /**
     * Get id.
     * @return the Integer id as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for this item
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the DatasetItem referenced by this item.
     * @return DatasetItem the dataset
     */
    public DatasetItem getDataset() { return dataset; }

    /**
     * Set the DatasetItem referenced by this item.
     * @param dataset the DatasetItem
     */
    public void setDataset(DatasetItem dataset) { this.dataset = dataset; }

    /**
     * Get the project name.
     * @return String the projectName
     */
    public String getProjectName() { return projectName; }

    /**
     * Set the project name.
     * @param projectName the project name
     */
    public void setProjectName(String projectName) { this.projectName = projectName; }

    /**
     * Get the PI name.
     * @return String the piName
     */
    public String getPiName() { return piName; }

    /**
     * Set the PI name.
     * @param piName
     */
    public void setPiName(String piName) { this.piName = piName; }

    /**
     * Get the DP name.
     * @return String the dpName
     */
    public String getDpName() { return dpName; }

    /**
     * Set the DP name.
     * @param dpName
     */
    public void setDpName(String dpName) { this.dpName = dpName; }

    /**
     * Get the access level.
     * @return String the accessLevel
     */
    public String getAccessLevel() { return accessLevel; }

    /**
     * Set the access level.
     * @param accessLevel
     */
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    /**
     * Get the isPublic flag.
     * @return Boolean isPublic
     */
    public Boolean getIsPublic() { return isPublic; }

    /**
     * Set the isPublic flag.
     * @param isPublic boolean
     */
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    /**
     * Get the citation.
     * @return String the citation
     */
    public String getCitation() { return citation; }

    /**
     * Set the citation.
     * @param citation
     */
    public void setCitation(String citation) { this.citation = citation; }

    /**
     * Get the number of students.
     * @return Long numStudents
     */
    public Long getNumStudents() { return numStudents; }

    /**
     * Set the number of students.
     * @param numStudents 
     */
    public void setNumStudents(Long numStudents) { this.numStudents = numStudents; }

    /**
     * Get the number of student hours.
     * @return Double numStudentHours
     */
    public Double getNumStudentHours() { return numStudentHours; }

    /**
     * Set the number of student hours.
     * @param numStudentHours 
     */
    public void setNumStudentHours(Double numStudentHours) {
        this.numStudentHours = numStudentHours;
    }

    /**
     * Get the number of unique steps.
     * @return Long numUniqueSteps
     */
    public Long getNumUniqueSteps() { return numUniqueSteps; }

    /**
     * Set the number of unique steps.
     * @param numUniqueSteps 
     */
    public void setNumUniqueSteps(Long numUniqueSteps) { this.numUniqueSteps = numUniqueSteps; }

    /**
     * Get the number of steps.
     * @return Long numSteps
     */
    public Long getNumSteps() { return numSteps; }

    /**
     * Set the number of steps.
     * @param numSteps 
     */
    public void setNumSteps(Long numSteps) { this.numSteps = numSteps; }

    /**
     * Get the number of transactions.
     * @return Long numTransactions
     */
    public Long getNumTransactions() { return numTransactions; }

    /**
     * Set the number of transactions.
     * @param numTransactions 
     */
    public void setNumTransactions(Long numTransactions) { this.numTransactions = numTransactions; }

    /**
     * Get the number of samples.
     * @return Long numSamples
     */
    public Long getNumSamples() { return numSamples; }

    /**
     * Set the number of samples.
     * @param numSamples 
     */
    public void setNumSamples(Long numSamples) { this.numSamples = numSamples; }

    /**
     * Get SkillModels.
     * @return the skillModels
     */
    protected Set<RemoteSkillModelItem> getSkillModels() {
        if (this.skillModels == null) {
            this.skillModels = new HashSet<RemoteSkillModelItem>();
        }
        return this.skillModels;
    }

    /**
     * Public method to get SkillModels.
     * @return a list instead of a set
     */
    public List<RemoteSkillModelItem> getSkillModelsExternal() {
        List<RemoteSkillModelItem> sortedModels =
            new ArrayList<RemoteSkillModelItem>(getSkillModels());
        Collections.sort(sortedModels);
        return Collections.unmodifiableList(sortedModels);
    }

    /**
     * Set SkillModels.
     * @param models Collection of models associated with this dataset.
     */
    protected void setSkillModels(Set<RemoteSkillModelItem> models) {
        this.skillModels = models;
    }

    /**
     * Add a SkillModel.
     * @param modelItem model to add
     */
    public void addSkillModel(RemoteSkillModelItem modelItem) {
        if (!getSkillModels().contains(modelItem)) {
            getSkillModels().add(modelItem);
            modelItem.setRemoteDatasetInfo(this);
        }
    }

    /**
     * Returns object name, hash code and the attributes.
     * @return String
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToStringFK("dataset", getDataset()));
         buffer.append(objectToString("projectName", getProjectName()));
         buffer.append(objectToString("piName", getPiName()));
         buffer.append(objectToString("dpName", getDpName()));
         buffer.append(objectToString("accessLevel", getAccessLevel()));
         buffer.append(objectToString("isPublic", getIsPublic()));
         buffer.append(objectToString("citation", getCitation()));
         buffer.append(objectToString("numStudents", getNumStudents()));
         buffer.append(objectToString("numStudentHours", getNumStudentHours()));
         buffer.append(objectToString("numUniqueSteps", getNumUniqueSteps()));
         buffer.append(objectToString("numSteps", getNumSteps()));
         buffer.append(objectToString("numTransactions", getNumTransactions()));
         buffer.append(objectToString("numSamples", getNumSamples()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
    * Equals function for this class.
    * @param obj Object of any type, should be an Item for equality check
    * @return boolean true if the items are equal, false if not
    */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RemoteDatasetInfoItem) {
            RemoteDatasetInfoItem otherItem = (RemoteDatasetInfoItem)obj;

            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEquals(this.getProjectName(), otherItem.getProjectName())) {
                return false;
            }
            if (!objectEquals(this.getPiName(), otherItem.getPiName())) {
                return false;
            }
            if (!objectEquals(this.getDpName(), otherItem.getDpName())) {
                return false;
            }
            if (!objectEquals(this.getAccessLevel(), otherItem.getAccessLevel())) {
                return false;
            }
            if (!objectEquals(this.getIsPublic(), otherItem.getIsPublic())) {
                return false;
            }
            if (!objectEquals(this.getCitation(), otherItem.getCitation())) {
                return false;
            }
            if (!objectEquals(this.getNumStudents(), otherItem.getNumStudents())) {
                return false;
            }
            if (!objectEquals(this.getNumStudentHours(), otherItem.getNumStudentHours())) {
                return false;
            }
            if (!objectEquals(this.getNumUniqueSteps(), otherItem.getNumUniqueSteps())) {
                return false;
            }
            if (!objectEquals(this.getNumSteps(), otherItem.getNumSteps())) {
                return false;
            }
            if (!objectEquals(this.getNumTransactions(), otherItem.getNumTransactions())) {
                return false;
            }
            if (!objectEquals(this.getNumSamples(), otherItem.getNumSamples())) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
    * Returns the hash code for this item.
     * @return integer hash code
    */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());

        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProjectName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPiName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDpName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAccessLevel());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getIsPublic());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCitation());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumStudents());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumStudentHours());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumUniqueSteps());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumSteps());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumTransactions());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumSamples());

        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        RemoteDatasetInfoItem otherItem = (RemoteDatasetInfoItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProjectName(), otherItem.getProjectName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPiName(), otherItem.getPiName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDpName(), otherItem.getDpName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAccessLevel(), otherItem.getAccessLevel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getIsPublic(), otherItem.getIsPublic());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCitation(), otherItem.getCitation());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumStudents(), otherItem.getNumStudents());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumStudentHours(), otherItem.getNumStudentHours());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumUniqueSteps(), otherItem.getNumUniqueSteps());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumSteps(), otherItem.getNumSteps());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumTransactions(), otherItem.getNumTransactions());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumSamples(), otherItem.getNumSamples());
        if (value != 0) { return value; }

        return value;
   }
}