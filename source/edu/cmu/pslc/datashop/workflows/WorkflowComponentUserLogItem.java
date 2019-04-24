/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.Date;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A collection of usage information for a user at the component-level.
 *
 * @author Michael Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */

public class WorkflowComponentUserLogItem extends Item implements java.io.Serializable, Comparable  {

    /** Composite Id of a Dataset and User */
    private Integer id;
    /** The user of the dataset. */
    private String user = null;
    /** The workflow. */
    private Long workflow = null;
    /** Dataset being used. */
    private Integer dataset = null;

    /** The component id, e.g. Analysis-1-x123456. */
    private String componentId = null;
    /** The human readable component id, e.g. Analysis #1. */
    private String componentIdHumanReadable = null;
    /** The component name, e.g. afm. */
    private String componentName = null;
    /** The component type, e.g. Analysis. */
    private String componentType = null;
    /** The output node index. */
    private Integer nodeIndex = null;
    /** The workflow file. */
    private Integer workflowFile = null;
    /** The dataset file. */
    private Integer datasetFile = null;

    private Date time;
    /** String of the action performed on the dataset. */
    private String action;
    /** Additional information about the dataset. */
    private String info;

    /** Default constructor. */
    public WorkflowComponentUserLogItem() {
        this.time = new Date();
    }

    public WorkflowComponentUserLogItem(String userId, Long workflowId, Integer datasetId,
            String componentId, String componentName, String componentType, String componentIdHumanReadable,
            Integer nodeIndex, Integer workflowFileId, Integer datasetFileId, String action, String info) {
        this.user = userId;
        this.workflow = workflowId;
        this.dataset = datasetId;
        this.componentId = componentId;
        this.componentName = componentName;
        this.componentType = componentType;
        this.componentIdHumanReadable = componentIdHumanReadable;
        this.nodeIndex = nodeIndex;
        this.workflowFile = workflowFileId;
        this.datasetFile = datasetFileId;
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
     * @return the dataset
     */
    public Integer getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Integer dataset) {
        this.dataset = dataset;
    }

    /**
     * @return the componentId
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * @param componentId the componentId to set
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * @return the componentIdHumanReadable
     */
    public String getComponentIdHumanReadable() {
        return componentIdHumanReadable;
    }

    /**
     * @param componentIdHumanReadable the componentIdHumanReadable to set
     */
    public void setComponentIdHumanReadable(String componentIdHumanReadable) {
        this.componentIdHumanReadable = componentIdHumanReadable;
    }

    /**
     * @return the componentName
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * @param componentName the componentName to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * @return the componentType
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * @param componentType the componentType to set
     */
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    /**
     * @return the nodeIndex
     */
    public Integer getNodeIndex() {
        return nodeIndex;
    }

    /**
     * @param nodeIndex the nodeIndex to set
     */
    public void setNodeIndex(Integer nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    /**
     * @return the workflowFile
     */
    public Integer getWorkflowFile() {
        return workflowFile;
    }

    /**
     * @param workflowFile the workflowFile to set
     */
    public void setWorkflowFile(Integer workflowFile) {
        this.workflowFile = workflowFile;
    }

    /**
     * @return the datasetFile
     */
    public Integer getDatasetFile() {
        return datasetFile;
    }

    /**
     * @param datasetFile the datasetFile to set
     */
    public void setDatasetFile(Integer datasetFile) {
        this.datasetFile = datasetFile;
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
         buffer.append(objectToString("workflowId", getWorkflow()));
         buffer.append(objectToString("datasetId", getDataset()));
         buffer.append(objectToString("workflowFileId", getWorkflowFile()));
         buffer.append(objectToString("datasetFileId", getDatasetFile()));
         buffer.append(objectToString("componentId", getComponentId()));
         buffer.append(objectToString("componentIdHumanReadable", getComponentIdHumanReadable()));
         buffer.append(objectToString("componentName", getComponentName()));
         buffer.append(objectToString("componentType", getComponentType()));
         buffer.append(objectToString("nodeIndexId", getNodeIndex()));
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
        if (obj instanceof WorkflowComponentUserLogItem) {
            WorkflowComponentUserLogItem otherItem = (WorkflowComponentUserLogItem)obj;

            if (!Item.objectEquals(this.getUser(), otherItem.getUser())) {
                return false;
            }
            if (!Item.objectEquals(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!Item.objectEquals(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!Item.objectEquals(this.getWorkflowFile(), otherItem.getWorkflowFile())) {
                return false;
            }
            if (!Item.objectEquals(this.getDatasetFile(), otherItem.getDatasetFile())) {
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
            if (!Item.objectEquals(this.getComponentId(), otherItem.getComponentId())) {
                return false;
            }
            if (!Item.objectEquals(this.getComponentIdHumanReadable(), otherItem.getComponentIdHumanReadable())) {
                return false;
            }
            if (!Item.objectEquals(this.getComponentName(), otherItem.getComponentName())) {
                return false;
            }
            if (!Item.objectEquals(this.getComponentType(), otherItem.getComponentType())) {
                return false;
            }
            if (!Item.objectEquals(this.getNodeIndex(), otherItem.getNodeIndex())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWorkflow());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDataset());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentId());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentIdHumanReadable());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentName());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentType());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNodeIndex());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWorkflowFile());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDatasetFile());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>User</li>
      *   <li>Dataset</li>
      *   <li>Workflow</li>
      *   <li>WorkflowFile</li>
      *   <li>DatasetFile</li>
      *   <li>Time</li>
      *   <li>Action</li>
      *   <li>ComponentId</li>
      *   <li>HumanReadableComponentId</li>
      *   <li>ComponentName</li>
      *   <li>ComponentType</li>
      *   <li>NodeIndex</li>
      *   <li>Info</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
      */
    public int compareTo(Object obj) {
        WorkflowComponentUserLogItem otherItem = (WorkflowComponentUserLogItem)obj;
        int value = 0;

        value = objectCompareTo(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWorkflowFile(), otherItem.getWorkflowFile());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatasetFile(), otherItem.getDatasetFile());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTime(), otherItem.getTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInfo(), otherItem.getInfo());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getComponentId(), otherItem.getComponentId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getComponentIdHumanReadable(), otherItem.getComponentIdHumanReadable());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getComponentName(), otherItem.getComponentName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getComponentType(), otherItem.getComponentType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNodeIndex(), otherItem.getNodeIndex());
        if (value != 0) { return value; }

        return 0;
    }
}