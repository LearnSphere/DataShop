/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet;

import java.io.Serializable;

import static edu.cmu.pslc.datashop.util.UtilConstants.HASH_INITIAL;

 /**
 *  This class represents any item which can be selected.  The item MUST
 *  be a comparable for sorting purposes.
 *
 * @author Benjamin Billings
 * @version $Revision: 5663 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2009-08-11 11:02:53 -0400 (Tue, 11 Aug 2009) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SelectableItem implements Serializable, Comparable {
    /** The unit item contained in this node */
    private Comparable item;
    /** A boolean representing whether this item is selected or not. */
    private boolean isSelected;
    /** A boolean representing whether this item has children that are selected. */
    private boolean hasSelectedChildren;
    /** A boolean indicating whether selected items should be given priority when comparing */
    private boolean selectedToTop = false;

    //TODO This node should contain the color of the item as well.

    /** Generic constructor that takes an objects and sets all booleans to false.
     * @param newItem - the Item that will be stored in this node.*/
    public SelectableItem(Comparable newItem) {
        item = newItem;
        isSelected = false;
        hasSelectedChildren = false;
    }

    /** Constructor that takes all variables at start.
     * @param newItem - the Item that will be stored in this node.
     * @param isSelected - boolean of whether the new item is selected.
     * @param hasSelectedChildren - boolean of whether the new item has selected children.
     * */
    public SelectableItem(Comparable newItem, boolean isSelected,
                      boolean hasSelectedChildren) {
        item = newItem;
        this.isSelected = isSelected;
        this.hasSelectedChildren = hasSelectedChildren;
    }

    /**
     * Sets the selectedToTop.
     * @param toTop a boolean indicating whether to prioritize the selected items when sorting.
     */
    public void setSelectedToTop(boolean toTop) {
        this.selectedToTop = toTop;
    }

    /**
     * Boolean indicating whether to prioritize the selected items when sorting.
     * @return boolean
     */
    public boolean selectedToTop() {
        return selectedToTop;
    }

    /** Returns the item.
     * @return the Comparable contained in this node. */
    public Comparable getItem() {
        return item;
    }

    /** Sets the unitItem.
     * @param newItem - the new unitItem to set this node to.
     * @return returns a boolean of success. */
    public boolean setItem(Comparable newItem) {
        try {
            item = newItem;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /** Returns whether this node is selected or not.
     * @return boolean indication whether this item is selected. */
    public boolean isSelected() {
        return isSelected;
    }

    /** Sets the isSelected status.
     * @param newStatus - a boolean of new selected status.
     */
    public void setIsSelected(boolean newStatus) {
        isSelected = newStatus;
    }

    /** Returns whether this node has children that are selected.
     * @return boolean indicating whether this node has selected children.
     */
    public boolean hasSelectedChildren() {
        return hasSelectedChildren;
    }

    /** Sets the hasSelectedChildren status.
     * @param newStatus - a boolean of new selected children status. */
    public void setHasSelectedChildren(boolean newStatus) {
        hasSelectedChildren = newStatus;
    }

    /**
     * Compare objects that forces the compare onto the object contained
     * in the selectable item unless the "force selected to top" option is on.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than.
     */
    public int compareTo(Object obj) {
        SelectableItem otherItem = (SelectableItem)obj;
        int value = 0;

        if (selectedToTop) {
            if (this.isSelected && otherItem.isSelected) {
                value = 0;
            } else if (this.isSelected && !otherItem.isSelected) {
                value = 1;
            } else if (this.isSelected && !otherItem.isSelected) {
                value = -1;
            }
        }
        if (value != 0) { return value; }

        value = this.getItem().compareTo(otherItem.getItem());

        return value;
    }

    /**
     * Determines whether another object is equal to this one.
     * For a selectableItem all we really care about is the item being
     * stored so it lets only checks those objects.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SelectableItem) {
            return getItem().equals(((SelectableItem)obj).getItem());
        }
        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
    public int hashCode() {
        return (int)((HASH_INITIAL * item.hashCode()) % Integer.MAX_VALUE);
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getClass().getName());
        buffer.append(" [");
        buffer.append("Item=" + item.toString());
        buffer.append(", isSelected=" + isSelected);
        buffer.append(", hasSelectedChildren=" + hasSelectedChildren);
        buffer.append(", selectedToTop=" + selectedToTop);
        buffer.append("]");

        return buffer.toString();
    }


} // end class SelectableItem
