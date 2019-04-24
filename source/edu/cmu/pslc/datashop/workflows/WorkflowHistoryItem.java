package edu.cmu.pslc.datashop.workflows;

import java.util.Date;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The workflow history actions for a given workflow.
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowHistoryItem extends Item implements java.io.Serializable, Comparable {

    /** Create a new workflow. */
    public static final String ACTION_CREATE_WORKFLOW = "create workflow";
    /** Rename workflow. */
    public static final String ACTION_RENAME_WORKFLOW = "rename workflow";
    /** Modify workflow XML. */
    public static final String ACTION_MODIFY_WORKFLOW = "modify workflow";
    /** Create new workflow from an existing workflow. */
    public static final String ACTION_SAVE_AS_NEW_WORKFLOW = "save as new workflow";
    /** Delete workflow action for the dataset system log. */
    public static final String ACTION_DELETE_WORKFLOW = "delete workflow";

    /** Database generated unique Id for this workflow history item. */
    private Long id;
    /** Workflow. */
    private WorkflowItem workflow;
    /** Dataset. */
    private DatasetItem dataset;
    /** Sample. */
    private SampleItem sample;
    /** To-be-created dataset's import queue item. */
    private ImportQueueItem importQueue;

    /** User id. */
    private String userId;
    /** Time. */
    private Date time;
    /** Action field. */
    private String action;
    /** Info field. */
    private String info;
    /** Filters as text. */
    private String sampleFilters;


    /** Get the workflow.
     * @return the workflow
     */
    public WorkflowItem getWorkflow() {
        return workflow;
    }

    /** Set the workflow.
     * @param workflow the workflow to set
     */
    public void setWorkflow(WorkflowItem workflow) {
        this.workflow = workflow;
    }

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
    public ImportQueueItem getImportQueue() {
        return importQueue;
    }

    /**
     * @param importQueue the import queue to set
     */
    public void setImportQueue(ImportQueueItem importQueue) {
        this.importQueue = importQueue;
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
     * @return the sampleFilters
     */
    public String getSampleFilters() {
        return sampleFilters;
    }

    /**
     * @param sampleFilters the sampleFilters to set
     */
    public void setSampleFilters(String sampleFilters) {
        this.sampleFilters = sampleFilters;
    }

    /** Default constructor. */
    public WorkflowHistoryItem() {
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
     * @param id Database generated unique Id for this WorkflowHistoryItem.
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
         buffer.append(objectToStringFK("workflow", getWorkflow()));
         buffer.append(objectToStringFK("dataset", getDataset()));
         buffer.append(objectToStringFK("sample", getSample()));
         buffer.append(objectToStringFK("importQueue", getImportQueue()));
         buffer.append(objectToString("user", getUserId()));
         buffer.append(objectToString("time", getTime()));
         buffer.append(objectToString("action", getAction()));
         buffer.append(objectToString("info", getInfo()));
         buffer.append(objectToString("sampleFilters", getSampleFilters()));

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
        if (obj instanceof WorkflowHistoryItem) {
            WorkflowHistoryItem otherItem = (WorkflowHistoryItem)obj;


            if (!Item.objectEqualsFK(this.getSample(), otherItem.getSample())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getImportQueue(), otherItem.getImportQueue())) {
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
            if (!Item.objectEquals(this.getSampleFilters(), otherItem.getSampleFilters())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getWorkflow());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getImportQueue());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserId());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTime());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfo());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSampleFilters());
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
         WorkflowHistoryItem otherItem = (WorkflowHistoryItem)obj;

         int value = 0;

         value = objectCompareToFK(this.getSample(), otherItem.getSample());
         if (value != 0) { return value; }

         value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
         if (value != 0) { return value; }

         value = objectCompareToFK(this.getWorkflow(), otherItem.getWorkflow());
         if (value != 0) { return value; }

         value = objectCompareToFK(this.getImportQueue(), otherItem.getImportQueue());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getUserId(), otherItem.getUserId());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getTime(), otherItem.getTime());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getAction(), otherItem.getAction());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getInfo(), otherItem.getInfo());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getSampleFilters(), otherItem.getSampleFilters());
         if (value != 0) { return value; }

         return value;
     }

}
