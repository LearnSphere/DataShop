/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 *
 * @author Mike Komisin
 * @version $Revision: $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */

public class WorkflowFolderItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this workflow. */
    private Long id;
    /** The id of the user that created this workflow. */
    private UserItem owner;
    /** The optional workflows associated with this workflow. */
    private Set<WorkflowItem> workflows;

    /** Name of this workflow. */
    private String workflowFolderName;
    /** Flag indicating whether this workflow is viewable by more than just the owner. */
    private Boolean globalFlag;
    /** Description of this workflow as a string. */
    private String description;
    /** The last updated time. */
    private Date lastUpdated;

    /** Default constructor. */
    public WorkflowFolderItem() {
        this.globalFlag = Boolean.FALSE;
    }

    /**
     *  Constructor with id.
     *  @param workflowId Database generated unique Id for this workflow.
     */
    public WorkflowFolderItem(Long workflowId) {
        this.id = workflowId;
        this.globalFlag = Boolean.FALSE;
    }

    /**
     * Get workflowId.
     * @return the Integer id as a Comparable
     */

    public Long getId() {
        return this.id;
    }

    /**
     * Set workflowId.
     * @param workflowId Database generated unique Id for this workflow.
     */
    public void setId(Long workflowId) {
        this.id = workflowId;
    }
    /**
     * Get owner.
     * @return the user item of the owner of this workflow
     */

    public UserItem getOwner() {
        return this.owner;
    }

    /**
     * Set owner.
     * @param owner The id of the user that created this workflow.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /** Get the workflow name.
     * @return the workflowFolderName
     */
    public String getWorkflowFolderName() {
        return workflowFolderName;
    }

    /** Set the workflow name.
     * @param workflowFolderName the workflowFolderName to set
     */
    public void setWorkflowFolderName(String workflowFolderName) {
        this.workflowFolderName = workflowFolderName;
    }


    /**
     * We often want to just show the name and id for an item.
     * @return the formatted name and id of this item
     */
    public String getNameAndId() { return getNameAndId(getWorkflowFolderName()); }

    /**
     * Get globalFlag.
     * @return Boolean
     */
    public Boolean getGlobalFlag() {
        return this.globalFlag;
    }

    /**
     * Set globalFlag.
     * @param globalFlag Flag indicating whether
     * this workflow is viewable by more than just the owner.
     */
    public void setGlobalFlag(Boolean globalFlag) {
        this.globalFlag = globalFlag;
    }
    /**
     * Get description.
     * @return the description of the workflow
     */

    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description Description of this workflow as a string.
     */
    public void setDescription(String description) {
        this.description = description;
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
         buffer.append(objectToString("workflowId", getId()));
         buffer.append(objectToStringFK("ownerId", getOwner()));
         buffer.append(objectToString("workflowFolderName", getWorkflowFolderName()));
         buffer.append(objectToString("globalFlag", getGlobalFlag()));
         buffer.append(objectToString("description", getDescription()));
         buffer.append(objectToString("lastUpdated", getLastUpdated()));
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
          if (obj instanceof WorkflowFolderItem) {
            WorkflowFolderItem otherItem = (WorkflowFolderItem)obj;

            if (!Item.objectEquals(this.getWorkflowFolderName(), otherItem.getWorkflowFolderName())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }
            if (!Item.objectEquals(this.getGlobalFlag(), otherItem.getGlobalFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getDescription(), otherItem.getDescription())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWorkflowFolderName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getGlobalFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastUpdated());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Workflow Name</li>
     *   <li>Workflow XML</li>
     *   <li>Global Flag</li>
     *   <li>Owner</li>
     *   <li>Description</li>
     *   <li>Last Updated</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowFolderItem otherItem = (WorkflowFolderItem)obj;
        int value = 0;

        value = objectCompareTo(this.getWorkflowFolderName(), otherItem.getWorkflowFolderName());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getGlobalFlag(), otherItem.getGlobalFlag());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastUpdated(), otherItem.getLastUpdated());
        if (value != 0) { return value; }

        return value;
    }

    /**
     * Get workflows.
     * @return java.util.Set
     */
    protected Set<WorkflowItem> getWorkflows() {
        if (this.workflows == null) {
            this.workflows = new HashSet<WorkflowItem>();
        }
        return this.workflows;
    }

    /**
     * Public method to get workflows.
     * @return a list instead of a set
     */
    public List<WorkflowItem> getWorkflowsExternal() {
        List<WorkflowItem> sortedList = new ArrayList<WorkflowItem>(getWorkflows());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a workflow.
     * @param item workflow to add
     */
    public void addWorkflow(WorkflowItem item) {
        getWorkflows().add(item);
        item.addWorkflowFolder(this);
    }

    /**
     * Remove a workflow.
     * @param item workflow to add
     */
    public void removeWorkflow(WorkflowItem item) {
        if (getWorkflows().contains(item)) {
            getWorkflows().remove(item);
        }
    }

    /**
     * Set workflows.
     * @param workflows Collection of workflows this analysis is associated with.
     */
    public void setWorkflows(Set<WorkflowItem> workflows) {
        this.workflows = workflows;
    }

}
