/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.io.Serializable;

import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between a workflowFolder and a workflow.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowFolderMapItem extends Item implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private WorkflowFolderMapId id;

    /** The Workflow item associated with this item. */
    private WorkflowItem workflow;

    /** The workflowFolder item associated with this item. */
    private WorkflowFolderItem workflowFolder;

    /** Default constructor. */
    public WorkflowFolderMapItem() { };

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
    public void setId(WorkflowFolderMapId id) {
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
        this.id = new WorkflowFolderMapId(this.workflow, this.workflowFolder);
    }

    /**
     * Get the workflowFolder.
     * @return the workflowFolder
     */
    public WorkflowFolderItem getWorkflowFolder() {
        return workflowFolder;
    }

    /**
     * Set the workflowFolder.
     * @param workflowFolder the workflowFolder to set
     */
    protected void setWorkflowFolder(WorkflowFolderItem workflowFolder) {
        this.workflowFolder = workflowFolder;
    }

    /**
     * Public set workflowFolder method to update the composite key as well.
     * @param workflowFolder Part of the composite key - FK
     */
    public void setWorkflowFolderExternal(WorkflowFolderItem workflowFolder) {
        setWorkflowFolder(workflowFolder);
        this.id = new WorkflowFolderMapId(this.workflow, this.workflowFolder);
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
        buffer.append(objectToStringFK("WorkflowFolder", getWorkflowFolder()));
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
        if (obj instanceof WorkflowFolderMapItem) {
            WorkflowFolderMapItem otherItem = (WorkflowFolderMapItem)obj;

            if (!objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!objectEqualsFK(this.getWorkflowFolder(), otherItem.getWorkflowFolder())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getWorkflowFolder());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow</li>
     * <li>workflowFolder</li>
     * <li>downloads</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowFolderMapItem otherItem = (WorkflowFolderMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getWorkflowFolder(), otherItem.getWorkflowFolder());
        if (value != 0) { return value; }

        return value;
    }
}
