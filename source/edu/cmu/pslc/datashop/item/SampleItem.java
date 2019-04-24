/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
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
 * A sample is a way of selecting a subset of transactions
 * from a dataset via filters.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11729 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-22 21:34:28 -0500 (Sat, 22 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SampleItem extends Item  implements java.io.Serializable, Comparable  {

    /** Default sample name for "all transactions". */
    public static final String ALL_TRANSACTIONS_SAMPLE_NAME = "All Data";

    /** Database generated unique Id for this sample. */
    private Integer id;
    /** The id of the user that created this sample. */
    private UserItem owner;
    /** Dataset this sample is associated to. */
    private DatasetItem dataset;
    /** Name of this sample as a string. */
    private String sampleName;
    /** Flag indicating whether this sample is viewable by more than just the owner. */
    private Boolean globalFlag;
    /** Description of this sample as a string. */
    private String description;
    /** Collection of filters associated with this sample. */
    private Set filters;
    /** Collection of sample history items associated with this sample. */
    private Set sampleHistory;
    /** File path for cached transaction export. */
    private String filePath;

    /** Default constructor. */
    public SampleItem() {
        this.globalFlag = Boolean.FALSE;
    }

    /**
     *  Constructor with id.
     *  @param sampleId Database generated unique Id for this sample.
     */
    public SampleItem(Integer sampleId) {
        this.id = sampleId;
        this.globalFlag = Boolean.FALSE;
    }

    /**
     * Get sampleId.
     * @return the Integer id as a Comparable
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set sampleId.
     * @param sampleId Database generated unique Id for this sample.
     */
    public void setId(Integer sampleId) {
        this.id = sampleId;
    }
    /**
     * Get owner.
     * @return the user item of the owner of this sample
     */

    public UserItem getOwner() {
        return this.owner;
    }

    /**
     * Set owner.
     * @param owner The id of the user that created this sample.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }
    /**
     * Get dataset.
     * @return the dataset item
     */

    public DatasetItem getDataset() {
        return this.dataset;
    }

    /**
     * Set dataset.
     * @param dataset Dataset this sample is associated to.
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }
    /**
     * Get sampleName.
     * @return the name of the sample
     */

    public String getSampleName() {
        return this.sampleName;
    }

    /**
     * Set sampleName.
     * @param sampleName Name of this sample as a string.
     */
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    /**
     * We often want to just show the name and id for an item.
     * @return the formatted name and id of this item
     */
    public String getNameAndId() { return getNameAndId(getSampleName()); }

    /**
     * Get globalFlag.
     * @return Boolean
     */
    public Boolean getGlobalFlag() {
        return this.globalFlag;
    }

    /**
     * Set globalFlag.
     * @param globalFlag Flag indicating whether
     * this sample is viewable by more than just the owner.
     */
    public void setGlobalFlag(Boolean globalFlag) {
        this.globalFlag = globalFlag;
    }
    /**
     * Get description.
     * @return the description of the sample
     */

    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description Description of this sample as a string.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get filters.
     * @return the set of filter items
     */
    protected Set getFilters() {
        if (this.filters == null) {
            this.filters = new HashSet();
        }
        return this.filters;
    }

    /**
     * Public method to get Filters.
     * @return a list instead of a set
     */
    public List<FilterItem> getFiltersExternal() {
        List<FilterItem> sortedList = new ArrayList<FilterItem>(getFilters());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set filters.
     * @param filters Collection of filters associated with this sample.
     */
    protected void setFilters(Set filters) {
        this.filters = filters;
    }

    /**
     * Add a filter.
     * @param item to add
     */
    public void addFilter(FilterItem item) {
        if (!getFilters().contains(item)) {
            this.filters.add(item);
            item.setSample(this);
        }
    }

    /**
     * Sets the filter set to empty<br>
     * Note: This should only be called after all filters are deleted.
     */
    public void clearFilters() {
        this.filters = null;
    }

    /**
     * Get the sample history.
     * @return the set of sample history items
     */
    protected Set getSampleHistory() {
        if (sampleHistory == null) {
            sampleHistory = new HashSet();
        }
        return sampleHistory;
    }

    /**
     * Public method to get the sample history.
     * @return a list instead of a set
     */
    public List<SampleHistoryItem> getSampleHistoryExternal() {
        List<SampleHistoryItem> sortedList = new ArrayList<SampleHistoryItem>(getSampleHistory());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set the sampleHistory.
     * @param sampleHistory Collection of sample history items associated with this sample.
     */
    protected void setSampleHistory(Set sampleHistory) {
        this.sampleHistory = sampleHistory;
    }

    /**
     * Sets the file path for this sample.
     * @param filePath the path at which cached transaction exports are stored.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Get the file path for this sample.
     * @return the file path.
     */
    public String getFilePath() {
        return this.filePath;
    }
    /**
     * whether this is the sample for "all transactions".
     * @return whether this is the sample for "all transactions". */
    public boolean isAllData() {
        return getSampleName().equals(SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME);
    }

    /**
     * Whether this sample is accessible to the user.
     * @param user the user
     * @param editOnly only return samples owned by the user, if true
     * @return whether this sample is accessible to the user
     */
    public boolean isAccessible(UserItem user, boolean editOnly) {
        return user.equals(getOwner()) || (!editOnly && (user.getAdminFlag() || getGlobalFlag()));
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
         buffer.append(objectToString("sampleId", getId()));
         buffer.append(objectToStringFK("ownerId", getOwner()));
         buffer.append(objectToStringFK("datasetId", getDataset()));
         buffer.append(objectToString("sampleName", getSampleName()));
         buffer.append(objectToString("globalFlag", getGlobalFlag()));
         buffer.append(objectToString("description", getDescription()));
         buffer.append(objectToString("filePath", getFilePath()));
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
          if (obj instanceof SampleItem) {
            SampleItem otherItem = (SampleItem)obj;

            if (!Item.objectEquals(this.getSampleName(), otherItem.getSampleName())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!Item.objectEquals(this.getGlobalFlag(), otherItem.getGlobalFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!Item.objectEquals(this.getFilePath(), otherItem.getFilePath())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSampleName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getGlobalFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFilePath());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Dataset</li>
     *   <li>Name</li>
     *   <li>Global Flag</li>
     *   <li>Owner</li>
     *   <li>Description</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SampleItem otherItem = (SampleItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSampleName(), otherItem.getSampleName());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getGlobalFlag(), otherItem.getGlobalFlag());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFilePath(), otherItem.getFilePath());
        if (value != 0) { return value; }

        return value;
    }
}