/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;
import java.io.Serializable;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the workflow_dataset_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowDatasetMapId implements Serializable, Comparable {

    /** The workflow id (FK). */
    private Long workflowId;
    /** The dataset id (FK). */
    private Integer datasetId;

    /**
     * Constructor.
     */
    public WorkflowDatasetMapId() { };

    /**
     * Full constructor.
     * @param workflowItem the workflow item for this composite key.
     * @param datasetItem the dataset item for this composite key.
     */
    public WorkflowDatasetMapId(WorkflowItem workflowItem, DatasetItem datasetItem) {
        if (workflowItem != null) {
            this.workflowId = (Long)workflowItem.getId();
        }
        if (datasetItem != null) {
            this.datasetId = (Integer)datasetItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param workflowId the workflow ID for this composite key.
     * @param datasetId the dataset ID for this composite key.
     */
    public WorkflowDatasetMapId(Long workflowId, Integer datasetId) {
        this.workflowId = workflowId;
        this.datasetId = datasetId;
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
     * Get the datasetId.
     * @return the datasetId.
     */
    public Integer getDatasetId() {
        return datasetId;
    }

    /**
     * Set the datasetId.
     * @param datasetId the datasetId to set.
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
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
         buffer.append("datasetId").append("='").append(datasetId).append("' ");
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
            WorkflowDatasetMapId otherItem = (WorkflowDatasetMapId)obj;
            if (!this.workflowId.equals(otherItem.getWorkflowId())) {
                return false;
            }
            if (!this.datasetId.equals(otherItem.getDatasetId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (datasetId != null ? datasetId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>workflow id</li>
     * <li>dataset id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowDatasetMapId otherItem = (WorkflowDatasetMapId)obj;
        if (this.workflowId.compareTo(otherItem.workflowId) != 0) {
            return this.workflowId.compareTo(otherItem.workflowId);
        }
        if (this.datasetId.compareTo(otherItem.datasetId) != 0) {
            return this.datasetId.compareTo(otherItem.datasetId);
        }
        return 0;
    }

}
