/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.io.Serializable;

import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between a workflowPaper and a workflow.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowPaperMapItem extends Item implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private WorkflowPaperMapId id;

    /** The Workflow item associated with this item. */
    private WorkflowItem workflow;

    /** The workflowPaper item associated with this item. */
    private WorkflowPaperItem workflowPaper;

    /** Default constructor. */
    public WorkflowPaperMapItem() { };

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
    public void setId(WorkflowPaperMapId id) {
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
        this.id = new WorkflowPaperMapId(this.workflow, this.workflowPaper);
    }

    /**
     * Get the workflowPaper.
     * @return the workflowPaper
     */
    public WorkflowPaperItem getWorkflowPaper() {
        return workflowPaper;
    }

    /**
     * Set the workflowPaper.
     * @param workflowPaper the workflowPaper to set
     */
    protected void setWorkflowPaper(WorkflowPaperItem workflowPaper) {
        this.workflowPaper = workflowPaper;
    }

    /**
     * Public set workflowPaper method to update the composite key as well.
     * @param workflowPaper Part of the composite key - FK
     */
    public void setWorkflowPaperExternal(WorkflowPaperItem workflowPaper) {
        setWorkflowPaper(workflowPaper);
        this.id = new WorkflowPaperMapId(this.workflow, this.workflowPaper);
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
        buffer.append(objectToStringFK("WorkflowPaper", getWorkflowPaper()));
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
        if (obj instanceof WorkflowPaperMapItem) {
            WorkflowPaperMapItem otherItem = (WorkflowPaperMapItem)obj;

            if (!objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!objectEqualsFK(this.getWorkflowPaper(), otherItem.getWorkflowPaper())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getWorkflowPaper());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow</li>
     * <li>workflowPaper</li>
     * <li>downloads</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowPaperMapItem otherItem = (WorkflowPaperMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getWorkflowPaper(), otherItem.getWorkflowPaper());
        if (value != 0) { return value; }

        return value;
    }
}
