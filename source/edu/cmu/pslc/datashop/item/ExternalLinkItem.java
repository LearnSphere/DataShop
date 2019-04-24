/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;


import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * External link associated with a project.
 *
 * @author Cindy Tipper
 * @version $Revision: 8057 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-11-05 08:38:48 -0500 (Mon, 05 Nov 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalLinkItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id. */
    private Integer id;
    /** The title of this link. */
    private String title;
    /** The url of this link. */
    private String url;
    /** Project associated with this link. */
    private ProjectItem project;

    /** Default constructor. */
    public ExternalLinkItem() { }

    /**
     *  Constructor with id.
     *  @param linkId Database generated unique Id for this external link
     *  @param title title of this link
     *  @param url URL of this link
     */
    public ExternalLinkItem(Integer linkId, String title, String url) {
        this.id = linkId;
        this.title = title;
        this.url = url;
    }

    /**
     * Get the id.
     * @return the Integer id as a Comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id Database generated unique id for this link.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get project.
     * @return edu.cmu.pslc.datashop.item.ProjectItem
     */
    public ProjectItem getProject() {
        return this.project;
    }

    /**
     * Set project.
     * @param project Part of the composite key - FK to the project table.
     */
    public void setProject(ProjectItem project) {
        this.project = project;
    }

    /**
     * Get the title.
     * @return java.lang.String
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title.
     * @param title The title for this link
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the URL.
     * @return java.lang.String
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the URL.
     * @param url The URL for this link
     */
    public void setUrl(String url) {
        this.url = url;
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
         buffer.append(objectToString("Id", getId()));
         buffer.append(objectToString("Title", getTitle()));
         buffer.append(objectToString("URL", getUrl()));
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
        if (obj instanceof ExternalLinkItem) {
            ExternalLinkItem otherItem = (ExternalLinkItem)obj;

            if (!objectEqualsFK(this.getProject(), otherItem.getProject())) {
                return false;
            }
            if (!objectEquals(this.getTitle(), otherItem.getTitle())) {
                return false;
            }
            if (!objectEquals(this.getUrl(), otherItem.getUrl())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProject());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTitle());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUrl());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>Project</li>
     * <li>Title</li>
     * <li>URL</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ExternalLinkItem otherItem = (ExternalLinkItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getProject(), otherItem.getProject());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTitle(), otherItem.getTitle());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUrl(), otherItem.getUrl());
        if (value != 0) { return value; }

        return value;
    }
}