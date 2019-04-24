/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;
import java.io.Serializable;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the workflow_tag_map relation.
 *
 * @author Cindy Tipper
 * @version $Revision: 15470 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-09-12 11:19:05 -0400 (Wed, 12 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowTagMapId implements Serializable, Comparable {

    /** The workflow id (FK). */
    private Long workflowId;
    /** The workflow_tag id (FK). */
    private Long workflowTagId;

    /**
     * Constructor.
     */
    public WorkflowTagMapId() { };

    /**
     * Full constructor.
     * @param workflowItem the workflow item for this composite key.
     * @param tagItem the workflow tag item for this composite key.
     */
    public WorkflowTagMapId(WorkflowItem workflowItem, WorkflowTagItem tagItem) {
        if (workflowItem != null) {
            this.workflowId = (Long)workflowItem.getId();
        }
        if (tagItem != null) {
            this.workflowTagId = (Long)tagItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param workflowId the workflow ID for this composite key.
     * @param tagId the workflow tag ID for this composite key.
     */
    public WorkflowTagMapId(Long workflowId, Long tagId) {
        this.workflowId = workflowId;
        this.workflowTagId = tagId;
    }

    /**
     * Get the workflowId.
     * @return the workflowId
     */
    public Long getWorkflowId() {
        return workflowId;
    }

    /**
     * Set the workflowId.
     * @param workflowId the workflowId to set
     */
    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Get the workflowTagId
     * @return the workflowTagId.
     */
    public Long getWorkflowTagId() {
        return workflowTagId;
    }

    /**
     * Set the workflowTagId.
     * @param tagId the workflowTagId to set.
     */
    public void setWorkflowTagId(Long tagId) {
        this.workflowTagId = tagId;
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
         buffer.append("workflowId").append("='").append(workflowId).append("' ");
         buffer.append("workflowTagId").append("='").append(workflowTagId).append("' ");
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            WorkflowTagMapId otherItem = (WorkflowTagMapId)obj;
            if (!this.workflowId.equals(otherItem.getWorkflowId())) {
                return false;
            }
            if (!this.workflowTagId.equals(otherItem.getWorkflowTagId())) {
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
        int hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + (workflowId != null ? workflowId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME + (workflowTagId != null ? workflowTagId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow id</li>
     * <li>workflow tag id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowTagMapId otherItem = (WorkflowTagMapId)obj;
        if (this.workflowId.compareTo(otherItem.workflowId) != 0) {
            return this.workflowId.compareTo(otherItem.workflowId);
        }
        if (this.workflowTagId.compareTo(otherItem.workflowTagId) != 0) {
            return this.workflowTagId.compareTo(otherItem.workflowTagId);
        }
        return 0;
    }

}
