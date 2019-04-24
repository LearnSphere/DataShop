package edu.cmu.pslc.datashop.workflows;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;


/**
 * The workflow component adjacency item.
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentAdjacencyItem extends Item implements java.io.Serializable, Comparable {


    /** Database generated unique Id for this workflow history item. */
    private Long id;
    /** The workflow. */
    private WorkflowItem workflow;
    /** The component name. */
    private String componentId;
    /** The component's index. */
    private Integer componentIndex;
    /** The component child. */
    private String childId;
    /** The component child's index. */
    private Integer childIndex;
    /** The depth of the node. */
    private Integer depthLevel;


    public WorkflowComponentAdjacencyItem() {

    }

    /**
     * @return the workflow
     */
    public WorkflowItem getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(WorkflowItem workflow) {
        this.workflow = workflow;
    }

    /**
     * @return the depthLevel
     */
    public Integer getDepthLevel() {
        return depthLevel;
    }

    /**
     * @param depthLevel the depthLevel to set
     */
    public void setDepthLevel(Integer depthLevel) {
        this.depthLevel = depthLevel;
    }

    /**
     * Get id.
     * @return the id
     */
    public Comparable getId() {
        return id;
    }

    /**
     * Set the id.
     * @param id Database generated unique Id for this WorkflowHistoryItem.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the componentId
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * @param componentId the componentId to set
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    public Integer getComponentIndex() {
		return componentIndex;
	}

	public void setComponentIndex(Integer componentIndex) {
		this.componentIndex = componentIndex;
	}

	public String getChildId() {
		return childId;
	}

	public void setChildId(String childId) {
		this.childId = childId;
	}

	public Integer getChildIndex() {
		return childIndex;
	}

	public void setChildIndex(Integer childIndex) {
		this.childIndex = childIndex;
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
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToString("workflow", getWorkflow()));
         buffer.append(objectToString("componentId", getComponentId()));
         buffer.append(objectToString("parentId", getComponentIndex()));
         buffer.append(objectToString("childId", getChildId()));
         buffer.append(objectToString("childId", getChildIndex()));
         buffer.append(objectToString("depthLevel", getDepthLevel()));

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
        if (obj instanceof WorkflowComponentAdjacencyItem) {
            WorkflowComponentAdjacencyItem otherItem = (WorkflowComponentAdjacencyItem)obj;

            if (!Item.objectEquals(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }

            if (!Item.objectEquals(this.getComponentId(), otherItem.getComponentId())) {
                return false;
            }
            if (!Item.objectEquals(this.getComponentIndex(), otherItem.getComponentIndex())) {
                return false;
            }
            if (!Item.objectEquals(this.getChildId(), otherItem.getChildId())) {
                return false;
            }
            if (!Item.objectEquals(this.getChildIndex(), otherItem.getChildIndex())) {
                return false;
            }
            if (!Item.objectEquals(this.getDepthLevel(), otherItem.getDepthLevel())) {
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
         hash = hash * UtilConstants.HASH_PRIME + this.objectHashCode(getWorkflow());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentId());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentIndex());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getChildId());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getChildIndex());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDepthLevel());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>Workflow</li>
      *   <li>ComponentId</li>
      *   <li>ComponentIndex</li>
      *   <li>ChildId</li>
      *   <li>ChildIndex</li>
      *   <li>DepthLevel</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         WorkflowComponentAdjacencyItem otherItem = (WorkflowComponentAdjacencyItem)obj;

         int value = 0;

         value = objectCompareTo(this.getWorkflow(), otherItem.getWorkflow());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getComponentId(), otherItem.getComponentId());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getComponentIndex(), otherItem.getComponentIndex());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getChildId(), otherItem.getChildId());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getChildIndex(), otherItem.getChildIndex());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDepthLevel(), otherItem.getDepthLevel());
         if (value != 0) { return value; }

         return value;
     }



}
