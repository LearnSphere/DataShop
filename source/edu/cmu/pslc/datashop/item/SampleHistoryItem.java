package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The sample history actions for a given sample.
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleHistoryItem extends Item implements java.io.Serializable, Comparable {

    /** Create sample action for the dataset system log. */
    public static final String ACTION_CREATE_SAMPLE = "create sample";
    /** Rename sample action for the dataset system log. */
    public static final String ACTION_RENAME_SAMPLE = "rename sample";
    /** Modify filters in sample action for the dataset system log. */
    public static final String ACTION_MODIFY_FILTERS = "modify filters";
    /** Generic modify sample action to represent previous modify actions in dataset system log. */
    public static final String ACTION_GENERIC_MODIFY = "modify";
    /** Create dataset from sample action for the dataset system log. */
    public static final String ACTION_CREATE_DATASET_FROM_SAMPLE = "create dataset from sample";
    /** Delete sample action for the dataset system log. */
    public static final String ACTION_DELETE_SAMPLE = "delete sample";

    /** Database generated unique Id for this sample history item. */
    private Long id;
    /** Dataset. */
    private DatasetItem dataset;
    /** Sample. */
    private SampleItem sample;
    /** To-be-created dataset's import queue id. */
    private Integer importQueueId;
    /** User id. */
    private String userId;
    /** Time. */
    private Date time;
    /** Action field. */
    private String action;
    /** Info field. */
    private String info;
    /** Filters as text. */
    private String filtersText;


    /**
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * @return the sample
     */
    public SampleItem getSample() {
        return sample;
    }

    /**
     * @param sample the sample to set
     */
    public void setSample(SampleItem sample) {
        this.sample = sample;
    }

    /**
     * @return the importQueueId
     */
    public Integer getImportQueueId() {
        return importQueueId;
    }

    /**
     * @param newDataset the newDataset to set
     */
    public void setImportQueueId(Integer importQueueId) {
        this.importQueueId = importQueueId;
    }

    /**
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the user id to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * @return the filtersText
     */
    public String getFiltersText() {
        return filtersText;
    }

    /**
     * @param filtersText the filtersText to set
     */
    public void setFiltersText(String filtersText) {
        this.filtersText = filtersText;
    }

    /** Default constructor. */
    public SampleHistoryItem() {
    }

    /**
     * Get id.
     * @return the id
     */
    public Comparable getId() {
        return id;
    }

    /**
     * Set the id.
     * @param id Database generated unique Id for this SampleHistoryItem.
     */
    public void setId(Long id) {
        this.id = id;
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
         buffer.append(objectToStringFK("dataset", getDataset()));
         buffer.append(objectToStringFK("sample", getSample()));
         buffer.append(objectToString("importQueueId", getImportQueueId()));
         buffer.append(objectToString("user", getUserId()));
         buffer.append(objectToString("time", getTime()));
         buffer.append(objectToString("action", getAction()));
         buffer.append(objectToString("info", getInfo()));
         buffer.append(objectToString("filtersText", getFiltersText()));

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
        if (obj instanceof SampleHistoryItem) {
            SampleHistoryItem otherItem = (SampleHistoryItem)obj;


            if (!Item.objectEqualsFK(this.getSample(), otherItem.getSample())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!Item.objectEquals(this.getImportQueueId(), otherItem.getImportQueueId())) {
                return false;
            }
            if (!Item.objectEquals(this.getUserId(), otherItem.getUserId())) {
                return false;
            }
            if (!Item.objectEquals(this.getTime(), otherItem.getTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getAction(), otherItem.getAction())) {
                return false;
            }
            if (!Item.objectEquals(this.getInfo(), otherItem.getInfo())) {
                return false;
            }
            if (!Item.objectEquals(this.getFiltersText(), otherItem.getFiltersText())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSample());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getImportQueueId());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserId());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTime());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfo());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFiltersText());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>Subgoal</li>
      *   <li>Input</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         SampleHistoryItem otherItem = (SampleHistoryItem)obj;

         int value = 0;

         value = objectCompareToFK(this.getSample(), otherItem.getSample());
         if (value != 0) { return value; }

         value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getImportQueueId(), otherItem.getImportQueueId());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getUserId(), otherItem.getUserId());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getTime(), otherItem.getTime());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getAction(), otherItem.getAction());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getInfo(), otherItem.getInfo());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getFiltersText(), otherItem.getFiltersText());
         if (value != 0) { return value; }

         return value;
     }

}
