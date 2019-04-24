/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.io.Serializable;
import java.util.Date;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between a dataset and a workflow.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowDatasetMapItem extends Item implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private WorkflowDatasetMapId id;

    /** The Workflow item associated with this item. */
    private WorkflowItem workflow;

    /** The dataset item associated with this item. */
    private DatasetItem dataset;

    /** The user that added this association. */
    private UserItem addedBy;

    /** The date this association was added. */
    private Date addedTime;

    /** Whether or not to auto-display the workflow in the dataset page. */
    private Boolean autoDisplayFlag;

    /** Default constructor. */
    public WorkflowDatasetMapItem() { };

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(WorkflowDatasetMapId id) {
        this.id = id;
    }

    /**
     * Get the workflow.
     * @return the workflow
     */
    public WorkflowItem getWorkflow() {
        return workflow;
    }

    /**
     * Set the workflow.
     * @param workflow the workflow to set
     */
    protected void setWorkflow(WorkflowItem workflow) {
        this.workflow = workflow;
    }

    /**
     * Public set tool method to update the composite key as well.
     * @param workflowItem Part of the composite key - FK
     */
    public void setWorkflowExternal(WorkflowItem workflowItem) {
        setWorkflow(workflowItem);
        this.id = new WorkflowDatasetMapId(this.workflow, this.dataset);
    }

    /**
     * Get the dataset.
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set the dataset.
     * @param dataset the dataset to set
     */
    protected void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Public set dataset method to update the composite key as well.
     * @param dataset Part of the composite key - FK
     */
    public void setDatasetExternal(DatasetItem dataset) {
        setDataset(dataset);
        this.id = new WorkflowDatasetMapId(this.workflow, this.dataset);
    }

    /**
     * Get the user that added this association.
     * @return the addedBy
     */
    public UserItem getAddedBy() {
        return addedBy;
    }

    /**
     * Set the user that added this association.
     * @param addedBy the user
     */
    public void setAddedBy(UserItem addedBy) {
        this.addedBy = addedBy;
    }

    /**
     * Get the date this association was added.
     * @return Date addedTime
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set the date this association was added.
     * @param addedTime date association was added
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }


    /**
     * Get the auto-display flag.
     * @return the auto-display flag
     */
    public Boolean getAutoDisplayFlag() {
        return autoDisplayFlag;
    }

    /**
     * Set the auto-display flag.
     * @param the auto-display flag
     */
    public void setAutoDisplayFlag(Boolean autoDisplayFlag) {
        this.autoDisplayFlag = autoDisplayFlag;
    }


    /**
     * Returns a string representation of this item, includes the hash code.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToStringFK("Workflow", getWorkflow()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToStringFK("AddedBy", getAddedBy()));
        buffer.append(objectToString("AddedTime", getAddedTime()));
        buffer.append(objectToString("AutoDisplayFlag", getAutoDisplayFlag()));
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
        if (obj instanceof WorkflowDatasetMapItem) {
            WorkflowDatasetMapItem otherItem = (WorkflowDatasetMapItem)obj;

            if (!objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEqualsFK(this.getAddedBy(), otherItem.getAddedBy())) {
                return false;
            }
            if (!objectEquals(this.getAddedTime(), otherItem.getAddedTime())) {
                return false;
            }
            if (!objectEquals(this.getAutoDisplayFlag(), otherItem.getAutoDisplayFlag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getWorkflow());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getAddedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getAddedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getAutoDisplayFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow</li>
     * <li>dataset</li>
     * <li>downloads</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowDatasetMapItem otherItem = (WorkflowDatasetMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getAddedBy(), otherItem.getAddedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAddedTime(), otherItem.getAddedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAutoDisplayFlag(), otherItem.getAutoDisplayFlag());
        if (value != 0) { return value; }

        return value;
    }
}
