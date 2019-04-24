/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the external_tool_file_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7819 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-09 13:40:05 -0400 (Thu, 09 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolFileMapId implements Serializable, Comparable {

    /** The external tool id (FK). */
    private Integer externalToolId;
    /** The file id (FK). */
    private Integer fileId;

    /**
     * Constructor.
     */
    public ExternalToolFileMapId() { };

    /**
     * Full constructor.
     * @param toolItem the tool item for this composite key.
     * @param fileItem the file item for this composite key.
     */
    public ExternalToolFileMapId(ExternalToolItem toolItem, FileItem fileItem) {
        if (toolItem != null) {
            this.externalToolId = (Integer)toolItem.getId();
        }
        if (fileItem != null) {
            this.fileId = (Integer)fileItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param toolId the tool ID for this composite key.
     * @param fileId the file ID for this composite key.
     */
    public ExternalToolFileMapId(Integer toolId, Integer fileId) {
        this.externalToolId = toolId;
        this.fileId = fileId;
    }

    /**
     * Get the externalToolId.
     * @return the externalToolId
     */
    public Integer getExternalToolId() {
        return externalToolId;
    }

    /**
     * Set the externalToolId.
     * @param externalToolId the externalToolId to set
     */
    public void setExternalToolId(Integer externalToolId) {
        this.externalToolId = externalToolId;
    }

    /**
     * Get the fileId.
     * @return the fileId.
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * Set the fileId.
     * @param fileId the fileId to set.
     */
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
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
         buffer.append("externalToolId").append("='").
                 append(externalToolId).append("' ");
         buffer.append("fileId").append("='").
                 append(fileId).append("' ");
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
            ExternalToolFileMapId otherItem = (ExternalToolFileMapId)obj;
            if (!this.externalToolId.equals(otherItem.getExternalToolId())) {
                return false;
            }
            if (!this.fileId.equals(otherItem.getFileId())) {
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
        hash = hash * UtilConstants.HASH_PRIME
            + (externalToolId != null ? externalToolId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (fileId != null ? fileId.hashCode() : 0);
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
        ExternalToolFileMapId otherItem = (ExternalToolFileMapId)obj;
        if (this.externalToolId.compareTo(otherItem.externalToolId) != 0) {
            return this.externalToolId.compareTo(otherItem.externalToolId);
        }
        if (this.fileId.compareTo(otherItem.fileId) != 0) {
            return this.fileId.compareTo(otherItem.fileId);
        }
        return 0;
    }

}
