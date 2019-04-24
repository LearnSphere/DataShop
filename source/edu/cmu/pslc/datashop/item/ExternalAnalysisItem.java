/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Output from an analysis done outside of DataShop.
 *
 * @author Cindy Tipper
 * @version $Revision: 7569 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-03-30 12:40:09 -0400 (Fri, 30 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalAnalysisItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this analysis file. */
    private Integer id;
    /** File associated with this analysis. */
    private FileItem file;
    /** Owner of this analysis. */
    private UserItem owner;
    /** Name of the corresponding KC (skill) model. */
    private String skillModelName;
    /** Id of the corresponding KC (skill) model. */
    private Long skillModelId;
    /** Name of the statistical model. */
    private String statisticalModel;
    /** Datasets associated with this analysis. */
    private Set<DatasetItem> datasets;

    /**
     * Get the id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param externalAnalysisId Database generated unique Id for this analysis.
     */
    public void setId(Integer externalAnalysisId) {
        this.id = externalAnalysisId;
    }

    /**
     * Returns file.
     * @return Returns the file.
     */
    public FileItem getFile() {
        return file;
    }

    /**
     * Set file.
     * @param file The file to set.
     */
    public void setFile(FileItem file) {
        this.file = file;
    }

    /**
     * Returns owner.
     * @return Returns the owner.
     */
    public UserItem getOwner() {
        return owner;
    }

    /**
     * Set owner.
     * @param owner The owner to set.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /**
     * Returns skillModelName.
     * @return Returns the citation.
     */
    public String getSkillModelName() {
        return this.skillModelName;
    }

    /**
     * Set skillModelName.
     * @param skillModelName The skillModelName to set.
     */
    public void setSkillModelName(String skillModelName) {
        this.skillModelName = skillModelName;
    }

    /**
     * Returns skillModelId.
     * @return Returns the citation.
     */
    public Long getSkillModelId() {
        return this.skillModelId;
    }

    /**
     * Set skillModelId.
     * @param skillModelId The skillModelId to set.
     */
    public void setSkillModelId(Long skillModelId) {
        this.skillModelId = skillModelId;
    }

    /**
     * Returns statisticalModel.
     * @return Returns the citation.
     */
    public String getStatisticalModel() {
        return this.statisticalModel;
    }

    /**
     * Set statisticalModel.
     * @param statisticalModel The statisticalModel to set.
     */
    public void setStatisticalModel(String statisticalModel) {
        this.statisticalModel = statisticalModel;
    }

    /**
     * Get datasets.
     * @return java.util.Set
     */
    protected Set<DatasetItem> getDatasets() {
        if (this.datasets == null) {
            this.datasets = new HashSet<DatasetItem>();
        }
        return this.datasets;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List<DatasetItem> getDatasetsExternal() {
        List<DatasetItem> sortedList = new ArrayList<DatasetItem>(getDatasets());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset.
     * @param item dataset to add
     */
    public void addDataset(DatasetItem item) {
        if (!getDatasets().contains(item)) {
            getDatasets().add(item);
            item.addExternalAnalysis(this);
        }
    }

    /**
     * Remove a dataset.
     * @param item dataset to add
     */
    public void removeDataset(DatasetItem item) {
        if (getDatasets().contains(item)) {
            getDatasets().remove(item);
            item.removeExternalAnalysis(this);
        }
    }

    /**
     * Set datasets.
     * @param datasets Collection of datasets this analysis is associated with.
     */
    public void setDatasets(Set<DatasetItem> datasets) {
        this.datasets = datasets;
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
         buffer.append(objectToString("Id", getId()));
         buffer.append(objectToStringFK("File", getFile()));
         buffer.append(objectToStringFK("Owner", getOwner()));
         buffer.append(objectToString("skillModelName", getSkillModelName()));
         buffer.append(objectToString("skillModelId", getSkillModelId()));
         buffer.append(objectToString("statisticalModel", getStatisticalModel()));
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
        if (obj instanceof ExternalAnalysisItem) {
            ExternalAnalysisItem otherItem = (ExternalAnalysisItem)obj;

            if (!objectEqualsFK(this.getFile(), otherItem.getFile())) {
                return false;
            }
            if (!objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }
            if (!objectEquals(this.getSkillModelName(), otherItem.getSkillModelName())) {
                return false;
            }
            if (!objectEquals(this.getSkillModelId(), otherItem.getSkillModelId())) {
                return false;
            }
            if (!objectEquals(this.getStatisticalModel(), otherItem.getStatisticalModel())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getFile());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSkillModelName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSkillModelId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatisticalModel());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>skill model name</li>
     * <li>skill model id</li>
     * <li>statistical model</li>
     * <li>owner</li>
     * <li>file</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ExternalAnalysisItem otherItem = (ExternalAnalysisItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getFile(), otherItem.getFile());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSkillModelName(), otherItem.getSkillModelName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSkillModelId(), otherItem.getSkillModelId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatisticalModel(), otherItem.getStatisticalModel());
        if (value != 0) { return value; }

        return value;
    }
}