/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.Date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;
import edu.cmu.pslc.datashop.item.Item;

/**
 * Output from an analysis done outside of DataShop.
 *
 * @author Peter Schaldenbrand
 * @version $Revision:  $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowAnnotationItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this annotation. */
    private Long id;
    /** ID of the Workflow that contains this annotation */
    private WorkflowItem workflow;
    /** Contents of the annotation */
    private String text;
    /** Date of the last time a user edited the annotation */
    private Date lastUpdated;

    /**
     * Get the id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param annotationId Database generated unique Id for this annotation.
     */
    public void setId(Long annotationId) {
        this.id = annotationId;
    }

    /** Get the workflow.
     * @return the workflow
     */
    public WorkflowItem getWorkflow() {
        return workflow;
    }

    /** Set the workflow.
     * @param workflow the workflow to set
     */
    public void setWorkflow(WorkflowItem workflow) {
        this.workflow = workflow;
    }

    /**
     * Returns text.
     * @return Returns the text content of the annotation.
     */
    public String getText() {
        return text;
    }

    /**
     * Set text.
     * @param text The text to set the contents of the annotation as.
     */
    public void setText(String text) {
        this.text = text;
    }

    /** Get the last updated time.
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /** Set the last updated time.
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
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
         buffer.append(objectToString("Id", getId()));
         buffer.append(objectToString("workfow_id", getWorkflow()));
         buffer.append(objectToString("text", getText()));
         buffer.append(objectToString("lastUpdated", getLastUpdated()));
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
        if (obj instanceof WorkflowAnnotationItem) {
            WorkflowAnnotationItem otherItem = (WorkflowAnnotationItem)obj;

            if (!objectEqualsFK(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!objectEqualsFK(this.getText(), otherItem.getText())) {
                return false;
            }
            if (!objectEqualsFK(this.getLastUpdated(), otherItem.getLastUpdated())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getWorkflow());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getText());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastUpdated());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>text</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowAnnotationItem otherItem = (WorkflowAnnotationItem)obj;
        int value = 0;

        value = objectCompareTo(this.getText(), otherItem.getText());
        if (value != 0) { return value; }

        return value;
    }
}