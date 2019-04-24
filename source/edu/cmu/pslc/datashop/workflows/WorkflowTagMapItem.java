/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.io.Serializable;
import java.util.Date;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between a tag and a workflow.
 *
 * @author Cindy Tipper
 * @version $Revision: 15470 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-09-12 11:19:05 -0400 (Wed, 12 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowTagMapItem extends Item implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private WorkflowTagMapId id;

    /** The Workflow item associated with this item. */
    private WorkflowItem workflow;

    /** The tag item associated with this item. */
    private WorkflowTagItem tag;

    /** Default constructor. */
    public WorkflowTagMapItem() { };

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
    public void setId(WorkflowTagMapId id) {
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
        this.id = new WorkflowTagMapId(this.workflow, this.tag);
    }

    /**
     * Get the tag.
     * @return the tag
     */
    public WorkflowTagItem getTag() {
        return tag;
    }

    /**
     * Set the tag.
     * @param tag the tag to set
     */
    protected void setTag(WorkflowTagItem tag) {
        this.tag = tag;
    }

    /**
     * Public set tag method to update the composite key as well.
     * @param tag Part of the composite key - FK
     */
    public void setTagExternal(WorkflowTagItem tag) {
        setTag(tag);
        this.id = new WorkflowTagMapId(this.workflow, this.tag);
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
        buffer.append(objectToStringFK("Tag", getTag()));
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
        if (obj instanceof WorkflowTagMapItem) {
            WorkflowTagMapItem otherItem = (WorkflowTagMapItem)obj;

            if (!objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!objectEqualsFK(this.getTag(), otherItem.getTag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getTag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow</li>
     * <li>tag</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowTagMapItem otherItem = (WorkflowTagMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getTag(), otherItem.getTag());
        if (value != 0) { return value; }

        return value;
    }
}
