/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.io.Serializable;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents an aggregation of workflow, dataset, and file information
 * for import components only.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class ComponentFilePersistenceItem extends Item
    implements Serializable, Comparable {

    /** Primary key. */
    private Long id;
    /** The workflow item associated with this item. */
    private WorkflowItem workflow;
    /** The dataset item associated with this item. */
    private DatasetItem dataset;
    /** The dataset level item associated with this item. */
    private WorkflowFileItem file;
    /** The component id, e.g. Analysis-x-111111. */
    private String componentId;

    /** Default constructor. */
    public ComponentFilePersistenceItem() {

    }

    /**
     * Useful constructor.
     * @param workflow the WorkflowItem
     * @param fileItem the WorkflowFileItem
     */
    public ComponentFilePersistenceItem(WorkflowItem workflow, String componentId, DatasetItem datasetItem, WorkflowFileItem fileItem) {
        this.workflow = workflow;
        this.dataset = datasetItem;
        this.file = fileItem;
        this.componentId = componentId;
    }

    /**
     * Returns the id as a comparable.
     * @return the int id as a comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for a class.
     */
    public void setId(Long id) {
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
    public void setWorkflow(WorkflowItem workflow) {
        this.workflow = workflow;
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
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Get the file.
     * @return the file
     */
    public WorkflowFileItem getFile() {
        return file;
    }

    /**
     * Set the file.
     * @param file the file to set
     */
    public void setFile(WorkflowFileItem file) {
        this.file = file;
    }

    /**
     * Get the componentId.
     * @return the componentId
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Set the componentId.
     * @param componentId the componentId to set
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
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
        buffer.append(objectToString("ComponentId", getComponentId()));
        buffer.append(objectToStringFK("File", getFile()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
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
        if (obj instanceof ComponentFilePersistenceItem) {
            ComponentFilePersistenceItem otherItem = (ComponentFilePersistenceItem)obj;

            if (!objectEqualsFK(this.getWorkflow(),
                    otherItem.getWorkflow())) {
                return false;
            }

            if (!objectEquals(this.getComponentId(),
                    otherItem.getComponentId())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(),
                    otherItem.getDataset())) {
                return false;
            }
            if (!objectEqualsFK(this.getFile(), otherItem.getFile())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getComponentId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getFile());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>component_file_id</li>
     * <li>workflow</li>
     * <li>dataset</li>
     * <li>file</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ComponentFilePersistenceItem otherItem = (ComponentFilePersistenceItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getWorkflow(),
                otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getComponentId(),
                otherItem.getComponentId());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataset(),
                otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getFile(), otherItem.getFile());
        if (value != 0) { return value; }

        return value;
    }
}
