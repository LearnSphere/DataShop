package edu.cmu.pslc.datashop.workflows;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The 'WorkflowTag' item.
 * @author Cindy Tipper
 * @version $Revision: 15733 $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: 2018-11-21 12:05:34 -0500 (Wed, 21 Nov 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowTagItem extends Item implements java.io.Serializable, Comparable {

    /** Database generated unique Id for this item. */
    private Long id;
    /** The tag. */
    private String tag;
    /** The workflows with this tag. */
    private Set<WorkflowItem> workflows;

    /** Public constructor. */
    public WorkflowTagItem() { }

    /** Constructor. */
    public WorkflowTagItem(String tag) {
        this.tag = tag;
    }

    /**
     * Get id.
     * @return the id
     */
    public Comparable getId() { return id; }

    /**
     * Set the id.
     * @param id Database generated unique Id for this WorkflowHistoryItem.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the comma-separated list of tag.
     * @return String tag
     */
    public String getTag() { return tag; }

    /**
     * Set the comma-separated list of tag.
     * @param tag
     */
    public void setTag(String tag) { this.tag = tag; }

    /**
     * Get the workflows that use this tag
     * @return Set of WorkflowItem
     */
    public Set<WorkflowItem> getWorkflows() {
        if (this.workflows == null) {
            this.workflows = new HashSet<WorkflowItem>();
        }
        return this.workflows;
    }

    /**
     * Set the workflows that use this tag
     * @param workflows - set of WorkflowItem
     */
    public void setWorkflows(Set<WorkflowItem> workflows) {
        this.workflows = workflows;
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
         buffer.append(objectToString("tag", getTag()));

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
        if (obj instanceof WorkflowTagItem) {
            WorkflowTagItem otherItem = (WorkflowTagItem)obj;

            if (!Item.objectEquals(this.getTag(), otherItem.getTag())) {
                return false;
            }
        }
        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
     public int hashCode() {
         long hash = UtilConstants.HASH_INITIAL;
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTag());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two WorkflowTagItem objects.
      *
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         WorkflowTagItem otherItem = (WorkflowTagItem)obj;

         int value = 0;

         value = objectCompareTo(this.getTag(), otherItem.getTag());
         if (value != 0) { return value; }

         return value;
     }

}
