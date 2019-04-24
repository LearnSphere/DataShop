/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;
import java.io.Serializable;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the workflow_paper_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowPaperMapId implements Serializable, Comparable {

    /** The workflow id (FK). */
    private Long workflowId;
    /** The workflow paper id (FK). */
    private Integer workflowPaperId;

    /**
     * Constructor.
     */
    public WorkflowPaperMapId() { };

    /**
     * Full constructor.
     * @param workflowItem the workflow item for this composite key.
     * @param workflowPaperItem the paper item for this composite key.
     */
    public WorkflowPaperMapId(WorkflowItem workflowItem, WorkflowPaperItem workflowPaperItem) {
        if (workflowItem != null) {
            this.workflowId = (Long)workflowItem.getId();
        }
        if (workflowPaperItem != null) {
            this.workflowPaperId = (Integer)workflowPaperItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param workflowId the workflow ID for this composite key.
     * @param workflowPaperId the workflow paper ID for this composite key.
     */
    public WorkflowPaperMapId(Long workflowId, Integer workflowPaperId) {
        this.workflowId = workflowId;
        this.workflowPaperId = workflowPaperId;
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
     * Get the workflowPaperId.
     * @return the workflowPaperId.
     */
    public Integer getWorkflowPaperId() {
        return workflowPaperId;
    }

    /**
     * Set the workflowPaperId.
     * @param workflowPaperId the workflowPaperId to set.
     */
    public void setWorkflowPaperId(Integer workflowPaperId) {
        this.workflowPaperId = workflowPaperId;
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
         buffer.append("workflowPaperId").append("='").append(workflowPaperId).append("' ");
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
            WorkflowPaperMapId otherItem = (WorkflowPaperMapId)obj;
            if (!this.workflowId.equals(otherItem.getWorkflowId())) {
                return false;
            }
            if (!this.workflowPaperId.equals(otherItem.getWorkflowPaperId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (workflowPaperId != null ? workflowPaperId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow id</li>
     * <li>workflow paper id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowPaperMapId otherItem = (WorkflowPaperMapId)obj;
        if (this.workflowId.compareTo(otherItem.workflowId) != 0) {
            return this.workflowId.compareTo(otherItem.workflowId);
        }
        if (this.workflowPaperId.compareTo(otherItem.workflowPaperId) != 0) {
            return this.workflowPaperId.compareTo(otherItem.workflowPaperId);
        }
        return 0;
    }

}
