/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;
import java.io.Serializable;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the workflow_folder_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowFolderMapId implements Serializable, Comparable {

    /** The workflow id (FK). */
    private Long workflowId;
    /** The workflow folder id (FK). */
    private Long workflowFolderId;

    /**
     * Constructor.
     */
    public WorkflowFolderMapId() { };

    /**
     * Full constructor.
     * @param workflowItem the workflow item for this composite key.
     * @param workflowFolderItem the folder item for this composite key.
     */
    public WorkflowFolderMapId(WorkflowItem workflowItem, WorkflowFolderItem workflowFolderItem) {
        if (workflowItem != null) {
            this.workflowId = (Long)workflowItem.getId();
        }
        if (workflowFolderItem != null) {
            this.workflowFolderId = (Long)workflowFolderItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param workflowId the workflow ID for this composite key.
     * @param workflowFolderId the workflow folder ID for this composite key.
     */
    public WorkflowFolderMapId(Long workflowId, Long workflowFolderId) {
        this.workflowId = workflowId;
        this.workflowFolderId = workflowFolderId;
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
     * Get the workflowFolderId.
     * @return the workflowFolderId.
     */
    public Long getWorkflowFolderId() {
        return workflowFolderId;
    }

    /**
     * Set the workflowFolderId.
     * @param workflowFolderId the workflowFolderId to set.
     */
    public void setWorkflowFolderId(Long workflowFolderId) {
        this.workflowFolderId = workflowFolderId;
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
         buffer.append("workflowFolderId").append("='").append(workflowFolderId).append("' ");
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
            WorkflowFolderMapId otherItem = (WorkflowFolderMapId)obj;
            if (!this.workflowId.equals(otherItem.getWorkflowId())) {
                return false;
            }
            if (!this.workflowFolderId.equals(otherItem.getWorkflowFolderId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (workflowFolderId != null ? workflowFolderId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow id</li>
     * <li>workflow folder id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowFolderMapId otherItem = (WorkflowFolderMapId)obj;
        if (this.workflowId.compareTo(otherItem.workflowId) != 0) {
            return this.workflowId.compareTo(otherItem.workflowId);
        }
        if (this.workflowFolderId.compareTo(otherItem.workflowFolderId) != 0) {
            return this.workflowFolderId.compareTo(otherItem.workflowFolderId);
        }
        return 0;
    }

}
