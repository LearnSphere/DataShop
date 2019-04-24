/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.Date;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A workflow user feedback item describes user feedback on the WF tool.
 *
 * @author Cindy Tipper
 * @version $Revision: 13285 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-06-02 16:42:14 -0400 (Thu, 02 Jun 2016) $
 * <!-- $KeyWordsOff: $ -->
 */

public class WorkflowUserFeedbackItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this item. */
    private Long id;
    /** The id of the user that created this item. */
    private UserItem user;
    /** The optional dataset associated with this item. */
    private DatasetItem dataset;
    /** The optional workflow associated with this item. */
    private WorkflowItem workflow;

    /** The user feedback. */
    private String feedback;
    /** The date of the feedback. */
    private Date date;

    /**
     *  Default constructor.
     */
    public WorkflowUserFeedbackItem() { }

    /**
     *  Constructor with id.
     *  @param workflowUserFeedbackId Database generated unique Id for this item
     */
    public WorkflowUserFeedbackItem(Long workflowUserFeedbackId) {
        this.id = workflowUserFeedbackId;
    }

    /**
     * Get workflowUserFeedbackId.
     * @return the Long id as a Comparable
     */

    public Comparable getId() { return this.id; }

    /**
     * Set workflowUserFeedbackId.
     * @param workflowUserFeedbackId Database generated unique Id for this workflow.
     */
    public void setId(Long workflowUserFeedbackId) {
        this.id = workflowUserFeedbackId;
    }

    /**
     * Get user.
     * @return the user item of the user of this item
     */

    public UserItem getUser() { return this.user; }

    /**
     * Set user.
     * @param user The id of the user that created this item
     */
    public void setUser(UserItem user) { this.user = user; }

    /**
     * Get the dataset.
     * @return the dataset item, null if not associated with a dataset
     */
    public DatasetItem getDataset() { return dataset; }

    /**
     * Set the dataset.
     * @param dataset the dataset item
     */
    public void setDataset(DatasetItem dataset) { this.dataset = dataset; }

    /**
     * Get the workflow.
     * @return the workflow item, null if not associated with a dataset
     */
    public WorkflowItem getWorkflow() { return workflow; }

    /**
     * Set the workflow.
     * @param workflow the workflow item
     */
    public void setWorkflow(WorkflowItem workflow) { this.workflow = workflow; }

    /**
     * Get the user feedback.
     * @return user feedback, as a String
     */
    public String getFeedback() { return feedback; }

    /**
     * Set the user feedback.
     * @param feedback the user feedback
     */
    public void setFeedback(String feedback) { this.feedback = feedback; }

    /**
     * Get the date associated with this item.
     * @return the date
     */
    public Date getDate() { return date; }

    /**
     * Set the date associated with this item.
     * @param date the date 
     */
    public void setDate(Date date) { this.date = date; }

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
         buffer.append(objectToString("workflowUserFeedbackId", getId()));
         buffer.append(objectToStringFK("userId", getUser()));
         buffer.append(objectToStringFK("datasetId", getDataset()));
         buffer.append(objectToStringFK("workflowId", getWorkflow()));
         buffer.append(objectToString("feedback", getFeedback()));
         buffer.append(objectToString("Date", getDate()));
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
          if (obj instanceof WorkflowUserFeedbackItem) {
            WorkflowUserFeedbackItem otherItem = (WorkflowUserFeedbackItem)obj;

            if (!Item.objectEqualsFK(this.getUser(), otherItem.getUser())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }
            if (!Item.objectEquals(this.getFeedback(), otherItem.getFeedback())) {
                return false;
            }
            if (!objectEquals(this.getDate(), otherItem.getDate())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUser());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getWorkflow());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFeedback());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDate());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>User</li>
     *   <li>Dataset</li>
     *   <li>Workflow</li>
     *   <li>Feedback</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowUserFeedbackItem otherItem = (WorkflowUserFeedbackItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getWorkflow(), otherItem.getWorkflow());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFeedback(), otherItem.getFeedback());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDate(), otherItem.getDate());
        if (value != 0) { return value; }

        return value;
    }
}