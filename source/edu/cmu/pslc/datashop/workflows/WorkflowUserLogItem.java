/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.Date;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A collection of usage information for a user at the workflow-level.
 *
 * @author Michael Komisin
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */

public class WorkflowUserLogItem extends Item implements java.io.Serializable, Comparable  {

    /** Composite Id of a Dataset and User */
    private Integer id;
    /** The user of the dataset. */
    private String user = null;
    /** The workflow. */
    private Long workflow = null;
    /** The new workflow. */
    private Long newWorkflow = null;
    /** Dataset being used. */
    private Integer dataset = null;
    /** Timestamp of the action. */
    private Date time;
    /** String of the action performed on the dataset. */
    private String action;
    /** Additional information about the dataset. */
    private String info;

    /** Default constructor. */
    public WorkflowUserLogItem() {
        this.time = new Date();
    }

    /** Constructor. */
    public WorkflowUserLogItem(String userId,
        Long workflowId, Integer datasetId,
            String action, String info, Long newLong) {
        this.user = userId;
        this.workflow = workflowId;
        this.dataset = datasetId;
        this.newWorkflow = newLong;
        this.action = action;
        this.info = info;
        this.time = new Date();
    }

    /** Returns id. @return Returns the id. */
    public Integer getId() { return id; }

    /** Set id. @param id The id to set. */
    public void setId(Integer id) { this.id = id; }

    /** Returns user. @return Returns the user. */
    public String getUser() { return user; }

    /** Set user. @param user The user to set. */
    public void setUser(String user) { this.user = user; }

    /** Returns dataset. @return Returns the dataset. */
    public Integer getDataset() { return dataset; }

    /** Set dataset. @param dataset The dataset to set. */
    public void setDataset(Integer dataset) { this.dataset = dataset; }

    /**
     * @return the workflow
     */
    public Long getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(Long workflow) {
        this.workflow = workflow;
    }

    /**
     * @return the newWorkflow
     */
    public Long getNewWorkflow() {
        return newWorkflow;
    }

    /**
     * @param newWorkflow the newWorkflow to set
     */
    public void setNewWorkflow(Long newWorkflow) {
        this.newWorkflow = newWorkflow;
    }

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
         buffer.append(objectToString("userId", getUser()));
         buffer.append(objectToString("datasetId", getDataset()));
         buffer.append(objectToString("workflowId", getWorkflow()));
         buffer.append(objectToString("newWorkflowId", getNewWorkflow()));
         buffer.append(objectToString("time", getTime()));
         buffer.append(objectToString("action", getAction()));
         buffer.append(objectToString("info", getInfo()));

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
        if (obj instanceof WorkflowUserLogItem) {
            WorkflowUserLogItem otherItem = (WorkflowUserLogItem)obj;

            if (!Item.objectEquals(this.getTime(), otherItem.getTime())) {
                return false;
            }
            if (!Item.objectEquals(this.getAction(), otherItem.getAction())) {
                return false;
            }
            if (!Item.objectEquals(this.getInfo(), otherItem.getInfo())) {
                return false;
            }
            if (!Item.objectEquals(this.getUser(), otherItem.getUser())) {
                return false;
            }
            if (!Item.objectEquals(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!Item.objectEquals(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!Item.objectEquals(this.getNewWorkflow(), otherItem.getNewWorkflow())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUser());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDataset());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWorkflow());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNewWorkflow());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>User</li>
      *   <li>Dataset</li>
      *   <li>Time</li>
      *   <li>Action</li>
      *   <li>Info</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
      */
    public int compareTo(Object obj) {
        WorkflowUserLogItem otherItem = (WorkflowUserLogItem)obj;
        int value = 0;

        value = objectCompareTo(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNewWorkflow(), otherItem.getNewWorkflow());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTime(), otherItem.getTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInfo(), otherItem.getInfo());
        if (value != 0) { return value; }

        return 0;
    }
}