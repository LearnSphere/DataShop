/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
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
 * An academic publication.
 *
 * @author From Benjamin K. Billings PaperItem
 * @version $Revision: 3533 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-12-22 14:15:14 -0500 (Fri, 22 Dec 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowPaperItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this paper. */
    private Integer id;
    /** Title of this paper. */
    private String title;
    /** String listing all author names for this paper. */
    private String authorNames;
    /** String of the publication. */
    private String publication;
    /** String with html formating of the citation. */
    private String citation;
    /** The date the paper was published. */
    private Date publishDate;
    /** Paragraph abstract of the paper. */
    private String paperAbstract;
    /** Owner of this paper. */
    private UserItem owner;
    /** File path to this paper. */
    private String filePath;
    /** External URL for this paper. */
    private String url;
    /** The optional workflows associated with this workflow. */
    private Set<WorkflowItem> workflows;
    /** The timestamp the paper was added. */
    private Date addedTime;

    /**
     * Get the paper id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set paper Id.
     * @param paperId Database generated unique Id for this paper.
     */
    public void setId(Integer paperId) {
        this.id = paperId;
    }

    /**
     * Returns addedTime.
     * @return Returns the addedTime.
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set addedTime.
     * @param addedTime The addedTime to set.
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    /**
     * Returns authorNames.
     * @return Returns the authorNames.
     */
    public String getAuthorNames() {
        return authorNames;
    }

    /**
     * Set authorNames.
     * @param authorNames The authorNames to set.
     */
    public void setAuthorNames(String authorNames) {
        this.authorNames = authorNames;
    }

    /**
     * Returns citation.
     * @return Returns the citation.
     */
    public String getCitation() {
        return citation;
    }

    /**
     * Set citation.
     * @param citation The citation to set.
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * Returns owner.
     * @return Returns the owner.
     */
    public UserItem getOwner() {
        return owner;
    }

    /**
     * Set owner.
     * @param owner The owner to set.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /**
     * Returns paperAbstract.
     * @return Returns the paperAbstract.
     */
    public String getPaperAbstract() {
        return paperAbstract;
    }

    /**
     * Set paperAbstract.
     * @param paperAbstract The paperAbstract to set.
     */
    public void setPaperAbstract(String paperAbstract) {
        this.paperAbstract = paperAbstract;
    }

    /**
     * Returns publishDate.
     * @return Returns the publishDate.
     */
    public Date getPublishDate() {
        return publishDate;
    }

    /**
     * Set publishDate.
     * @param publishDate The publishDate to set.
     */
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    /**
     * Returns title.
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns publication.
     * @return Returns the publication.
     */
    public String getPublication() {
        return publication;
    }

    /**
     * Set publication.
     * @param publication The publication to set.
     */
    public void setPublication(String publication) {
        this.publication = publication;
    }

    /**
     * Returns filePath.
     * @return Returns the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set filePath.
     * @param filePath The filePath to set.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns url.
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set url.
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
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
        item.addWorkflowPaper(this);
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
         buffer.append(objectToString("Id", getId()));
         buffer.append(objectToString("Title", getTitle()));
         buffer.append(objectToString("AuthorNames", getAuthorNames()));
         buffer.append(objectToString("Citation", getCitation()));
         buffer.append(objectToString("publishDate", getPublishDate()));
         buffer.append(objectToString("paperAbstract", getPaperAbstract()));
         buffer.append(objectToStringFK("Owner", getOwner()));
         buffer.append(objectToString("AddedTime", getAddedTime()));
         buffer.append(objectToString("Publication", getPublication()));
         buffer.append(objectToString("FilePath", getPublication()));
         buffer.append(objectToString("Url", getPublication()));
         // but not paper, its a blob;
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
        if (obj instanceof WorkflowPaperItem) {
            WorkflowPaperItem otherItem = (WorkflowPaperItem)obj;

            if (!objectEquals(this.getTitle(), otherItem.getTitle())) {
                return false;
            }
            if (!objectEquals(this.getAuthorNames(), otherItem.getAuthorNames())) {
                return false;
            }
            if (!objectEquals(this.getCitation(), otherItem.getCitation())) {
                return false;
            }
            if (!objectEquals(this.getPublishDate(), otherItem.getPublishDate())) {
                return false;
            }
            if (!objectEquals(this.getPaperAbstract(), otherItem.getPaperAbstract())) {
                return false;
            }
            if (!objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }
            if (!objectEqualsFK(this.getPublication(), otherItem.getPublication())) {
                return false;
            }
            if (!objectEqualsFK(this.getFilePath(), otherItem.getFilePath())) {
                return false;
            }
            if (!objectEqualsFK(this.getUrl(), otherItem.getUrl())) {
                return false;
            }
            if (!objectEquals(this.getAddedTime(), otherItem.getAddedTime())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTitle());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAuthorNames());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCitation());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPublishDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPaperAbstract());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPublication());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFilePath());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUrl());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAddedTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>title</li>
     * <li>author names</li>
     * <li>citation</li>
     * <li>publish_date</li>
     * <li>abstract</li>
     * <li>owner</li>
     * <li>publication</li>
     * <li>file_path</li>
     * <li>url</li>
     * <li>added time</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowPaperItem otherItem = (WorkflowPaperItem)obj;
        int value = 0;

        value = objectCompareTo(this.getTitle(), otherItem.getTitle());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAuthorNames(), otherItem.getAuthorNames());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCitation(), otherItem.getCitation());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPublishDate(), otherItem.getPublishDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPaperAbstract(), otherItem.getPaperAbstract());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPublication(), otherItem.getPublication());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFilePath(), otherItem.getFilePath());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUrl(), otherItem.getUrl());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAddedTime(), otherItem.getAddedTime());
        if (value != 0) { return value; }

        return value;
    }
}
