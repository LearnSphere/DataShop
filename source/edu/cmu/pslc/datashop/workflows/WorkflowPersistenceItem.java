/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.Date;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A workflow describes an user-defined process that a sample undergoes.
 *
 * @author Mike Komisin
 * @version $Revision: 13726 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2017-01-03 11:14:07 -0500 (Tue, 03 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */

public class WorkflowPersistenceItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this workflow persistence object. */
    private Long id;
    /** Database generated unique Id for this workflow persistence object. */
    private WorkflowItem workflow;
    /** Database generated unique Id for this workflow persistence object. */
    private String workflowXml;
    /** The last updated time. */
    private Date lastUpdated;

    /** Default constructor. */
    public WorkflowPersistenceItem() {

    }

    /**
     * Get workflowPersistenceId.
     * @return the Integer id as a Comparable
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set workflowPersistenceId.
     * @param workflowPersistenceId Database generated unique Id for this workflow persistence object.
     */
    public void setId(Long workflowPersistenceId) {
        this.id = workflowPersistenceId;
    }
    /**
     * Get owner.
     * @return the user item of the owner of this workflow persistence object
     */

    /** Get the workflow XML.
     * @return the workflowXml
     */
    public String getWorkflowXml() {
        return workflowXml;
    }

    /** Set the workflow XML.
     * @param workflowXml the workflowXml to set
     */
    public void setWorkflowXml(String workflowXml) {
        this.workflowXml = workflowXml;
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

    public WorkflowItem getWorkflow() {
       return workflow;
    }

    public void setWorkflow(WorkflowItem workflow) {
        this.workflow = workflow;
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
         buffer.append(objectToString("WorkflowPersistenceId", getId()));
         buffer.append(objectToString("Workflow", getWorkflow().getId()));
         buffer.append(objectToString("WorkflowXml", getWorkflowXml()));
         buffer.append(objectToString("LastUpdated", getLastUpdated()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
      * Equals function for this class.
      * @param obj Object of any type, should be an Item for equality check
      * @return boolean true if the items are equal, false if not
      */
      public boolean equals(Object obj) {
          if (this == obj) {
              return true;
          }
          if (obj instanceof WorkflowPersistenceItem) {
            WorkflowPersistenceItem otherItem = (WorkflowPersistenceItem)obj;

            if (!Item.objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!Item.objectEquals(this.getWorkflowXml(), otherItem.getWorkflowXml())) {
                return false;
            }
            if (!Item.objectEquals(this.getLastUpdated(), otherItem.getLastUpdated())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getWorkflow());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWorkflowXml());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastUpdated());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Workflow</li>
     *   <li>Workflow XML</li>
     *   <li>Last Updated</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowPersistenceItem otherItem = (WorkflowPersistenceItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWorkflowXml(), otherItem.getWorkflowXml());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastUpdated(), otherItem.getLastUpdated());
        if (value != 0) { return value; }

        return value;
    }

}