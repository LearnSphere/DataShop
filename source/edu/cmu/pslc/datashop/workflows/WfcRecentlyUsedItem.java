package edu.cmu.pslc.datashop.workflows;

import java.util.Date;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * The 'WFC recently used' item.
 * @author Cindy Tipper
 * @version $Revision: 15130 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-05-15 14:11:06 -0400 (Tue, 15 May 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WfcRecentlyUsedItem extends Item implements java.io.Serializable, Comparable {


    /** Database generated unique Id for this item. */
    private Long id;
    /** The user's id. */
    private String userId;
    /** The component type. */
    private String componentType;
    /** The component name. */
    private String componentName;
    /** The last used time for (WFC, user)-pair. */
    private Date lastUsed;

    /** Public constructor. */
    public WfcRecentlyUsedItem() {

    }

    /** Constructor. */
    public WfcRecentlyUsedItem(String userId, String componentType, String componentName) {
        this.userId = userId;
        this.componentType = componentType;
        this.componentName = componentName;
        this.lastUsed = new Date();
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
     * @return the userId
     */
    public String getUserId() { return userId; }

    /**
     * @param userId the user id to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the componentType
     */
    public String getComponentType() { return componentType; }

    /**
     * @param componentType the componentType to set
     */
    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    /**
     * @return the componentName
     */
    public String getComponentName() { return componentName; }

    /**
     * @param componentName the componentName to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Getter for lastUsed timestamp.
     * @return the lastUsed time
     */
    public Date getLastUsed() { return lastUsed; }

    /**
     * Setter for lastUsed timestamp.
     * @param lastUsed the timestamp
     */
    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
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
         buffer.append(objectToString("userId", getUserId()));
         buffer.append(objectToString("componentType", getComponentType()));
         buffer.append(objectToString("componentName", getComponentName()));
         buffer.append(objectToString("lastUsed", getLastUsed()));

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
        if (obj instanceof WfcRecentlyUsedItem) {
            WfcRecentlyUsedItem otherItem = (WfcRecentlyUsedItem)obj;

            if (!Item.objectEquals(this.getUserId(), otherItem.getUserId())) {
                return false;
            }
            if (!Item.objectEquals(this.getComponentType(), otherItem.getComponentType())) {
                return false;
            }
            if (!Item.objectEquals(this.getComponentName(), otherItem.getComponentName())) {
                return false;
            }
            if (!Item.objectEquals(this.getLastUsed(), otherItem.getLastUsed())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserId());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentType());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentName());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastUsed());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using only the 'lastUsed' timestamp.
      * This is so that objects can easily be sorted by time.
      *
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         WfcRecentlyUsedItem otherItem = (WfcRecentlyUsedItem)obj;

         int value = 0;

         value = objectCompareTo(this.getLastUsed(), otherItem.getLastUsed());
         if (value != 0) { return value; }

         return value;
     }

}
