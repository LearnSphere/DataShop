/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * A collection of usage information for a dataset performed by the system.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class DatasetSystemLogItem extends Item implements java.io.Serializable, Comparable  {

    /** Composite Id of a Dataset and SuccessFlag */
    private Integer id;
    /** Dataset being used. */
    private DatasetItem dataset = null;
    /** SkillModel corresponding to the action. */
    private SkillModelItem skillModel;
    /** Sample corresponding to the action. */
    private SampleItem sample;
    /** Timestamp of the action. */
    private Date time;
    /** String of the action performed on the dataset. */
    private String action;
    /** Additional information about the dataset. */
    private String info;
    /** Whether the action performed was successful. */
    private Boolean successFlag;
    /** The number of items processed, e.g. number transactions cached. */
    private Integer value;
    /** The amount of time that has elapsed to perform this action. */
    private Long elapsedTime;
    /** The release number of DataShop when this action was performed. */
    private String datashopVersion;

    /** Default constructor. */
    public DatasetSystemLogItem() {
        datashopVersion = VersionInformation.RELEASE_NUMBER;
    }

    /** Returns id. @return Returns the id. */
    public Integer getId() { return id; }

    /** Set id. @param id The id to set. */
    public void setId(Integer id) { this.id = id; }

    /** Returns dataset. @return Returns the dataset. */
    public DatasetItem getDataset() { return dataset; }

    /** Set dataset. @param dataset The dataset to set. */
    public void setDataset(DatasetItem dataset) { this.dataset = dataset; }

    /** Returns time. @return Returns the time. */
    public Date getTime() { return time; }

    /** Set time. @param time The time to set. */
    public void setTime(Date time) { this.time = time; }

    /** Returns action. @return Returns the action. */
    public String getAction() { return action; }

    /** Set action. @param action The action to set. */
    public void setAction(String action) { this.action = action; }

    /** Returns info. @return Returns the info. */
    public String getInfo() { return info; }

    /** Set info. @param info The info to set. */
    public void setInfo(String info) { this.info = info; }

    /** Returns successFlag. @return Returns the successFlag. */
    public Boolean getSuccessFlag() { return successFlag; }

    /** Set successFlag. @param successFlag The successFlag to set. */
    public void setSuccessFlag(Boolean successFlag) { this.successFlag = successFlag; }

    /** Set the skillModel. @return Returns the skillModel. */
    public SkillModelItem getSkillModel() { return skillModel; }

    /** Set the skillModel. @param skillModel the skillModel to set. */
    public void setSkillModel(SkillModelItem skillModel) {
        this.skillModel = skillModel;
    }

    /** Get the sample. @return Returns the sample.*/
    public SampleItem getSample() { return this.sample; }

    /** Set the sample. @param sample The sample to set. */
    public void setSample(SampleItem sample) {
        this.sample = sample;
    }

    /** Returns the value. @return the value */
    public Integer getValue() {
        return value;
    }

    /** Sets the value. @param value the value to set */
    public void setValue(Integer value) {
        this.value = value;
    }

    /** Returns the elapsed time. @return the elapsedTime */
    public Long getElapsedTime() {
        return elapsedTime;
    }

    /** Sets the elapsed time. @param elapsedTime the elapsedTime to set */
    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /** Returns the DataShop version. @return the datashopVersion */
    public String getDatashopVersion() {
        return datashopVersion;
    }

    /** Sets the DataShop version. @param datashopVersion the datashopVersion to set */
    public void setDatashopVersion(String datashopVersion) {
        this.datashopVersion = datashopVersion;
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
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToStringFK("datasetId", getDataset()));
         buffer.append(objectToStringFK("skillModel", getSkillModel()));
         buffer.append(objectToStringFK("sample", getSample()));
         buffer.append(objectToString("time", getTime()));
         buffer.append(objectToString("action", getAction()));
         buffer.append(objectToString("info", getInfo()));
         buffer.append(objectToString("successFlagId", getSuccessFlag()));
         buffer.append(objectToString("value", getValue()));
         buffer.append(objectToString("elapsedTime", getElapsedTime()));
         buffer.append(objectToString("datashopVersion", getDatashopVersion()));

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
        if (obj instanceof DatasetSystemLogItem) {
            DatasetSystemLogItem otherItem = (DatasetSystemLogItem)obj;

            if (!Item.objectEquals(this.getTime(), otherItem.getTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getAction(), otherItem.getAction())) {
                return false;
            }
            if (!Item.objectEquals(this.getInfo(), otherItem.getInfo())) {
                return false;
            }
            if (!Item.objectEquals(this.getSuccessFlag(), otherItem.getSuccessFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getValue(), otherItem.getValue())) {
                return false;
            }
            if (!Item.objectEquals(this.getElapsedTime(), otherItem.getElapsedTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getDatashopVersion(), otherItem.getDatashopVersion())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getSkillModel(), otherItem.getSkillModel())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getSample(), otherItem.getSample())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTime());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfo());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSuccessFlag());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getValue());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getElapsedTime());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDatashopVersion());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSkillModel());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSample());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>SuccessFlag</li>
      *   <li>Dataset</li>
      *   <li>Time</li>
      *   <li>Action</li>
      *   <li>Info</li>
      *   <li>Value</li>
      *   <li>Elapsed Time</li>
      *   <li>Datashop Version</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
      */
    public int compareTo(Object obj) {
        DatasetSystemLogItem otherItem = (DatasetSystemLogItem)obj;
        int retValue = 0;

        retValue = objectCompareTo(this.getSuccessFlag(), otherItem.getSuccessFlag());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareToFK(this.getSkillModel(), otherItem.getSkillModel());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareToFK(this.getSample(), otherItem.getSample());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareTo(this.getTime(), otherItem.getTime());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareTo(this.getAction(), otherItem.getAction());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareTo(this.getInfo(), otherItem.getInfo());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareTo(this.getValue(), otherItem.getValue());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareTo(this.getElapsedTime(), otherItem.getElapsedTime());
        if (retValue != 0) { return retValue; }

        retValue = objectCompareTo(this.getDatashopVersion(), otherItem.getDatashopVersion());
        if (retValue != 0) { return retValue; }

        return 0;
    }
}