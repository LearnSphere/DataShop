/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the discourse_import_queue_map relation.
 *
 * @author Cindy Tipper
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseImportQueueMapId implements Serializable, Comparable {

    /** The discourse id (FK). */
    private Long discourseId;
    /** The import queue id (FK). */
    private Integer importQueueId;

    /**
     * Constructor.
     */
    public DiscourseImportQueueMapId() { };

    /**
     * Full constructor.
     * @param discourseItem the discourse item for this composite key.
     * @param iqItem the import queue item for this composite key.
     */
    public DiscourseImportQueueMapId(DiscourseItem discourseItem, ImportQueueItem iqItem) {
        if (discourseItem != null) {
            this.discourseId = (Long)discourseItem.getId();
        }
        if (iqItem != null) {
            this.importQueueId = (Integer)iqItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param discourseId the discourse ID for this composite key.
     * @param iqId the import queue ID for this composite key.
     */
    public DiscourseImportQueueMapId(Long discourseId, Integer iqId) {
        this.discourseId = discourseId;
        this.importQueueId = iqId;
    }

    /**
     * Get the discourseId.
     * @return the discourseId
     */
    public Long getDiscourseId() {
        return discourseId;
    }

    /**
     * Set the discourseId.
     * @param discourseId the discourseId to set
     */
    public void setDiscourseId(Long discourseId) {
        this.discourseId = discourseId;
    }

    /**
     * Get the importQueueId.
     * @return the importQueueId.
     */
    public Integer getImportQueueId() {
        return importQueueId;
    }

    /**
     * Set the importQueueId.
     * @param importQueueId the importQueueId to set.
     */
    public void setImportQueueId(Integer importQueueId) {
        this.importQueueId = importQueueId;
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
         buffer.append("discourseId = ");
         buffer.append(discourseId);
         buffer.append(", importQueueId = ");
         buffer.append(importQueueId);
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
            DiscourseImportQueueMapId otherItem = (DiscourseImportQueueMapId)obj;
            if (!this.discourseId.equals(otherItem.getDiscourseId())) {
                return false;
            }
            if (!this.importQueueId.equals(otherItem.getImportQueueId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + (discourseId != null ? discourseId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (importQueueId != null ? importQueueId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>external tool id</li>
     * <li>file id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DiscourseImportQueueMapId otherItem = (DiscourseImportQueueMapId)obj;
        
        if (this.discourseId.compareTo(otherItem.discourseId) != 0) {
            return this.discourseId.compareTo(otherItem.discourseId);
        }
        if (this.importQueueId.compareTo(otherItem.importQueueId) != 0) {
            return this.importQueueId.compareTo(otherItem.importQueueId);
        }
        return 0;
    }

}
