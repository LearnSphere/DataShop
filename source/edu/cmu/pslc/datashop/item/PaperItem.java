/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * An academic publication.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3533 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-12-22 14:15:14 -0500 (Fri, 22 Dec 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PaperItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this paper. */
    private Integer id;
    /** Title of this paper. */
    private String title;
    /** String listing all author names for this paper. */
    private String authorNames;
    /** String with html formating of the citation. */
    private String citation;
    /** The year this paper was written/published. */
    private Integer paperYear;
    /** Paragraph abstract of the paper. */
    private String paperAbstract;
    /** Owner of this paper. */
    private UserItem owner;
    /** File associated with this paper. */
    private FileItem file;
    /** Datasets associated with this paper */
    private Set datasets;
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
     * Returns paperYear.
     * @return Returns the paperYear.
     */
    public Integer getPaperYear() {
        return paperYear;
    }

    /**
     * Set paperYear.
     * @param paperYear The paperYear to set.
     */
    public void setPaperYear(Integer paperYear) {
        this.paperYear = paperYear;
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
     * Returns file.
     * @return Returns the file.
     */
    public FileItem getFile() {
        return file;
    }

    /**
     * Set file.
     * @param file The file to set.
     */
    public void setFile(FileItem file) {
        this.file = file;
    }

    /**
     * Get datasets.
     * @return java.util.Set
     */
    protected Set getDatasets() {
        if (this.datasets == null) {
            this.datasets = new HashSet();
        }
        return this.datasets;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List getDatasetsExternal() {
        List sortedList = new ArrayList(getDatasets());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset.
     * @param item dataset to add
     */
    public void addDataset(DatasetItem item) {
        if (!getDatasets().contains(item)) {
            getDatasets().add(item);
            item.addPaper(this);
        }
    }

    /**
     * Remove a dataset.
     * @param item dataset to add
     */
    public void removeDataset(DatasetItem item) {
        if (getDatasets().contains(item)) {
            getDatasets().remove(item);
            item.removePaper(this);
        }
    }

    /**
     * Set datasets.
     * @param datasets Collection of datasets this paper is associated with.
     */
    public void setDatasets(Set datasets) {
        this.datasets = datasets;
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
         buffer.append(objectToString("paperYear", getPaperYear()));
         buffer.append(objectToString("paperAbstract", getPaperAbstract()));
         buffer.append(objectToStringFK("Owner", getOwner()));
         buffer.append(objectToString("AddedTime", getAddedTime()));
         buffer.append(objectToStringFK("File", getFile()));
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
        if (obj instanceof PaperItem) {
            PaperItem otherItem = (PaperItem)obj;

            if (!objectEquals(this.getTitle(), otherItem.getTitle())) {
                return false;
            }
            if (!objectEquals(this.getAuthorNames(), otherItem.getAuthorNames())) {
                return false;
            }
            if (!objectEquals(this.getCitation(), otherItem.getCitation())) {
                return false;
            }
            if (!objectEquals(this.getPaperYear(), otherItem.getPaperYear())) {
                return false;
            }
            if (!objectEquals(this.getPaperAbstract(), otherItem.getPaperAbstract())) {
                return false;
            }
            if (!objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }
            if (!objectEqualsFK(this.getFile(), otherItem.getFile())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPaperYear());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPaperAbstract());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getFile());
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
     * <li>year</li>
     * <li>abstract</li>
     * <li>owner</li>
     * <li>file</li>
     * <li>added time</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        PaperItem otherItem = (PaperItem)obj;
        int value = 0;

        value = objectCompareTo(this.getTitle(), otherItem.getTitle());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAuthorNames(), otherItem.getAuthorNames());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCitation(), otherItem.getCitation());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPaperYear(), otherItem.getPaperYear());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPaperAbstract(), otherItem.getPaperAbstract());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getFile(), otherItem.getFile());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAddedTime(), otherItem.getAddedTime());
        if (value != 0) { return value; }

        return value;
    }
}