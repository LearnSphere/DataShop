/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between an external tool and a file.
 *
 * @author alida
 * @version $Revision: 7819 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-09 13:40:05 -0400 (Thu, 09 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolFileMapItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private ExternalToolFileMapId id;

    /** The external tool item associated with this item. */
    private ExternalToolItem externalTool;

    /** The dataset level item associated with this item. */
    private FileItem file;

    /** The number of times this file has been downloaded. */
    private Integer downloads;

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(ExternalToolFileMapId id) {
        this.id = id;
    }

    /**
     * Get the externalTool.
     * @return the externalTool
     */
    public ExternalToolItem getExternalTool() {
        return externalTool;
    }

    /**
     * Set the externalTool.
     * @param externalTool the externalTool to set
     */
    protected void setExternalTool(ExternalToolItem externalTool) {
        this.externalTool = externalTool;
    }

    /**
     * Public set tool method to update the composite key as well.
     * @param toolItem Part of the composite key - FK
     */
    public void setExternalToolExternal(ExternalToolItem toolItem) {
        setExternalTool(toolItem);
        this.id = new ExternalToolFileMapId(this.externalTool, this.file);
    }

    /**
     * Get the file.
     * @return the file
     */
    public FileItem getFile() {
        return file;
    }

    /**
     * Set the file.
     * @param file the file to set
     */
    protected void setFile(FileItem file) {
        this.file = file;
    }

    /**
     * Public set file method to update the composite key as well.
     * @param file Part of the composite key - FK
     */
    public void setFileExternal(FileItem file) {
        setFile(file);
        this.id = new ExternalToolFileMapId(this.externalTool, this.file);
    }

    /**
     * Get the downloads.
     * @return the downloads
     */
    public Integer getDownloads() {
        return downloads;
    }

    /**
     * Set the downloads.
     * @param downloads the downloads to set
     */
    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    /**
     * Increment the downloads.
     */
    public void incrementDownloads() {
        this.downloads++;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToStringFK("ExternalTool", getExternalTool()));
        buffer.append(objectToStringFK("File", getFile()));
        buffer.append(objectToString("Downloads", this.getDownloads()));
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
        if (obj instanceof ExternalToolFileMapItem) {
            ExternalToolFileMapItem otherItem = (ExternalToolFileMapItem)obj;

            if (!objectEqualsFK(this.getExternalTool(),
                    otherItem.getExternalTool())) {
                return false;
            }
            if (!objectEqualsFK(this.getFile(), otherItem.getFile())) {
                return false;
            }
            if (!objectEquals(this.getDownloads(), otherItem.getDownloads())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getDownloads());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getExternalTool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getFile());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>externalTool</li>
     * <li>file</li>
     * <li>downloads</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ExternalToolFileMapItem otherItem = (ExternalToolFileMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getExternalTool(),
                otherItem.getExternalTool());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getFile(), otherItem.getFile());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDownloads(), otherItem.getDownloads());
        if (value != 0) { return value; }

        return value;
    }
}
