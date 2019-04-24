/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * IRB.
 *
 * @author Cindy Tipper
 * @version $Revision: 10529 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-10 11:30:51 -0500 (Mon, 10 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id. */
    private Integer id;
    /** The title of this IRB. */
    private String title;
    /** The notes for this IRB. */
    private String notes;
    /** Date this IRB was added to DataShop. */
    private Date addedTime;
    /** Date this IRB was updated in DataShop. */
    private Date updatedTime;
    /** Date this IRB was approved. */
    private Date approvalDate;
    /** Date this IRB expires. */
    private Date expirationDate;
    /** Whether expiration date should be set. */
    private Boolean expirationDateNa;
    /** User that added this IRB to DataShop. */
    private UserItem addedBy;
    /** User that last updated this IRB to DataShop. */
    private UserItem updatedBy;
    /** The name of the IRB PI. */
    private String pi;
    /** The protocol number for this IRB. */
    private String protocolNumber;
    /** The granting institution for this IRB. */
    private String grantingInstitution;
    /** Files associated with this IRB. */
    private Set<FileItem> files;
    /** Projects associated with this IRB. */
    private Set<ProjectItem> projects;

    /** Date format. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    /** Display string for "Not applicable". */
    public static final String NOT_APPLICABLE = "Not applicable";


    /** Default constructor. */
    public IrbItem() { }

    /**
     *  Constructor with id.
     *  @param irbId Database generated unique Id for this IRB
     *  @param title title of this IRB
     */
    public IrbItem(Integer irbId, String title) {
        this.id = irbId;
        this.title = title;
    }

    /**
     * Get the id.
     * @return the Integer id as a Comparable
     */
    public Comparable<Integer> getId() {
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
     * Get the notes.
     * @return String the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Set the notes.
     * @param notes the notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Get the time the IRB was added to DataShop.
     * @return Date addedTime
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set the time the IRB was added to DataShop.
     * @param addedTime the time added
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    /**
     * Get the time the IRB was updated in DataShop.
     * @return Date updatedTime
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Set the time the IRB was updated inDataShop.
     * @param updatedTime the time updated
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Get the date the IRB was approved.
     * @return Date approval date
     */
    public Date getApprovalDate() {
        return approvalDate;
    }

    /**
     * Get the date the IRB was approved as a string.
     * @return approval date string
     */
    public String getApprovalDateStr() {
        if (approvalDate == null) { return ""; }
        return DATE_FMT.format(approvalDate);
    }

    /**
     * Set the date the IRB was approved.
     * @param approvalDate the date approved
     */
    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    /**
     * Get the date the IRB expires.
     * @return Date expiration date
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * Get the date the IRB expires as a string.
     * @return expiration date string
     */
    public String getExpirationDateStr() {
        if (expirationDate == null) { return ""; }
        return DATE_FMT.format(expirationDate);
    }

    /**
     * Set the date the IRB expires.
     * @param expirationDate the expiration date
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets the expirationDateNa.
     * @return the expirationDateNa
     */
    public Boolean getExpirationDateNa() {
        return expirationDateNa;
    }

    /**
     * Sets the expirationDateNa.
     * @param expirationDateNa the expirationDateNa to set
     */
    public void setExpirationDateNa(Boolean expirationDateNa) {
        this.expirationDateNa = expirationDateNa;
    }

    /**
     * Get the user that added this IRB to DataShop.
     * @return UserItem user that added this IRB
     */
    public UserItem getAddedBy() {
        return addedBy;
    }

    /**
     * Set the user that added this IRB to DataShop.
     * @param addedBy user that added this IRB
     */
    public void setAddedBy(UserItem addedBy) {
        this.addedBy = addedBy;
    }

    /**
     * Get the user that last updated this IRB to DataShop.
     * @return UserItem user that last updated this IRB
     */
    public UserItem getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the user that last updated this IRB to DataShop.
     * @param updatedBy user that last updated this IRB
     */
    public void setUpdatedBy(UserItem updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Get the name of the PI for this IRB.
     * @return String name of the PI
     */
    public String getPi() {
        return pi;
    }

    /**
     * Set the name of the PI for this IRB.
     * @param pi name of the PI
     */
    public void setPi(String pi) {
        this.pi = pi;
    }

    /**
     * Get the protocol number for this IRB.
     * @return String protocol number
     */
    public String getProtocolNumber() {
        return protocolNumber;
    }

    /**
     * Set the protocol number for this IRB.
     * @param pNumber the protocol number
     */
    public void setProtocolNumber(String pNumber) {
        this.protocolNumber = pNumber;
    }

    /**
     * Get the granting institution for this IRB.
     * @return String granting institution
     */
    public String getGrantingInstitution() {
        return grantingInstitution;
    }

    /**
     * Set the granting institution for this IRB.
     * @param inst the granting institution
     */
    public void setGrantingInstitution(String inst) {
        this.grantingInstitution = inst;
    }

    /**
     * Get files associated with this IRB.
     * @return the set of files
     */
    public Set<FileItem> getFiles() {
        if (this.files == null) {
            this.files = new HashSet<FileItem>();
        }
        return this.files;
    }

    /**
     * Public method to get Files.
     * @return a list instead of a set
     */
    public List<FileItem> getFilesExternal() {
        List<FileItem> sortedItems = new ArrayList<FileItem>(getFiles());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set files associated with this IRB.
     * @param files Collection of files associated with this IRB
     */
    protected void setFiles(Set<FileItem> files) {
        this.files = files;
    }

    /**
     * Add a file.
     * @param file file to add
     */
    public void addFile(FileItem file) {
        if (!getFiles().contains(file)) {
            getFiles().add(file);
            file.addIrb(this);
        }
    }

    /**
     * Remove the FileItem.
     * @param item FileItem.
     */
    public void removeFile(FileItem item) {
        if (getFiles().contains(item)) {
            getFiles().remove(item);
        }
    }

    /**
     * Get projects associated with this IRB.
     * @return the set of projects
     */
    public Set<ProjectItem> getProjects() {
        if (this.projects == null) {
            this.projects = new HashSet<ProjectItem>();
        }
        return this.projects;
    }

    /**
     * Public method to get Projects.
     * @return a list instead of a set
     */
    public List<ProjectItem> getProjectsExternal() {
        List<ProjectItem> sortedItems = new ArrayList<ProjectItem>(getProjects());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set projects associated with this IRB.
     * @param projects Collection of projects associated with this IRB
     */
    protected void setProjects(Set<ProjectItem> projects) {
        this.projects = projects;
    }

    /**
     * Add a project.
     * @param project Project to add
     */
    public void addProject(ProjectItem project) {
        if (!getProjects().contains(project)) {
            getProjects().add(project);
        }
        project.addIrb(this);
    }

    /**
     * Remove the ProjectItem.
     * @param item ProjectItem.
     */
    public void removeProject(ProjectItem item) {
        if (getProjects().contains(item)) {
            getProjects().remove(item);
        }
        item.removeIrb(this);
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
         buffer.append(objectToString("Notes", getNotes()));
         buffer.append(objectToString("Added Time", getAddedTime()));
         buffer.append(objectToString("Updated Time", getUpdatedTime()));
         buffer.append(objectToString("Approval Date", getApprovalDate()));
         buffer.append(objectToString("Expiration Date", getExpirationDate()));
         buffer.append(objectToString("Exp Date NA", getExpirationDateNa()));
         buffer.append(objectToString("Added By", getAddedBy()));
         buffer.append(objectToString("Updated By", getUpdatedBy()));
         buffer.append(objectToString("PI", getPi()));
         buffer.append(objectToString("Protocol Number", getProtocolNumber()));
         buffer.append(objectToString("Granting Institution", getGrantingInstitution()));
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
        if (obj instanceof IrbItem) {
            IrbItem otherItem = (IrbItem)obj;

            if (!objectEquals(this.getTitle(), otherItem.getTitle())) {
                return false;
            }
            if (!objectEquals(this.getNotes(), otherItem.getNotes())) {
                return false;
            }
            if (!objectEquals(this.getAddedTime(), otherItem.getAddedTime())) {
                return false;
            }
            if (!objectEquals(this.getUpdatedTime(), otherItem.getUpdatedTime())) {
                return false;
            }
            if (!objectEquals(this.getApprovalDate(), otherItem.getApprovalDate())) {
                return false;
            }
            if (!objectEquals(this.getExpirationDate(), otherItem.getExpirationDate())) {
                return false;
            }
            if (!objectEquals(this.getExpirationDateNa(), otherItem.getExpirationDateNa())) {
                return false;
            }
            if (!objectEqualsFK(this.getAddedBy(), otherItem.getAddedBy())) {
                return false;
            }
            if (!objectEqualsFK(this.getUpdatedBy(), otherItem.getUpdatedBy())) {
                return false;
            }
            if (!objectEquals(this.getPi(), otherItem.getPi())) {
                return false;
            }
            if (!objectEquals(this.getProtocolNumber(), otherItem.getProtocolNumber())) {
                return false;
            }
            if (!objectEquals(this.getGrantingInstitution(), otherItem.getGrantingInstitution())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNotes());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAddedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUpdatedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getApprovalDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getExpirationDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getExpirationDateNa());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getAddedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUpdatedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPi());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProtocolNumber());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getGrantingInstitution());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>Title</li>
     * <li>Notes</li>
     * <li>Added Time</li>
     * <li>Updated Time</li>
     * <li>Approval Date</li>
     * <li>Expiration Date</li>
     * <li>Added By</li>
     * <li>Updated By</li>
     * <li>PI</li>
     * <li>Protocol Number</li>
     * <li>Granting Institution</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        IrbItem otherItem = (IrbItem)obj;
        int value = 0;

        value = objectCompareTo(this.getTitle(), otherItem.getTitle());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNotes(), otherItem.getNotes());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAddedTime(), otherItem.getAddedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUpdatedTime(), otherItem.getUpdatedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getApprovalDate(), otherItem.getApprovalDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getExpirationDate(), otherItem.getExpirationDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getExpirationDateNa(), otherItem.getExpirationDateNa());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getAddedBy(), otherItem.getAddedBy());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUpdatedBy(), otherItem.getUpdatedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPi(), otherItem.getPi());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProtocolNumber(), otherItem.getProtocolNumber());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getGrantingInstitution(), otherItem.getGrantingInstitution());
        if (value != 0) { return value; }

        return value;
    }
}