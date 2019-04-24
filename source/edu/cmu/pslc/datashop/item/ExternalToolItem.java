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

import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * An external tool added to and described by one of the DataShop users.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7854 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-15 12:15:25 -0400 (Wed, 15 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID. */
    private Integer id;
    /** Creator/Owner. */
    private UserItem contributor;
    /** Name. */
    private String name;
    /** Added Time. */
    private Date addedTime;
    /** Updated Time. */
    private Date updatedTime;
    /** Description. */
    private String description;
    /** Language. */
    private String language;
    /** Web Page. */
    private String webPage;
    /** Number of downloads. */
    private Integer downloads = 0;
    /** Files associated with this item. */
    private Set<FileItem> files;

    /**
     * Get the id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set id.
     * @param id Database generated unique Id for this item.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get contributor.
     * @return Returns the contributor.
     */
    public UserItem getContributor() {
        return contributor;
    }

    /**
     * Set contributor.
     * @param contributor The contributor to set.
     */
    public void setContributor(UserItem contributor) {
        this.contributor = contributor;
    }

    /**
     * Get name.
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set name.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** Constant for the maximum length of the zip file name. */
    private static final int MAX_LEN_ZIP_FILE_NAME = 50;

    /**
     * Get zip file name from the name.
     * @return the zip file name.
     */
    public String getZipFileName() {
        String zipFileName = FileUtils.cleanForFileSystem(this.name);
        if (zipFileName.length() > MAX_LEN_ZIP_FILE_NAME) {
            return zipFileName.substring(0, MAX_LEN_ZIP_FILE_NAME) + ".zip";
        }
        return zipFileName + ".zip";
    }

    /**
     * Get the added time.
     * @return the added time
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set the added time.
     * @param addedTime The added time
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    /**
     * Get the updated time.
     * @return the updated time
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Set the updated time.
     * @param updatedTime The updated time
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Get description.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get language.
     * @return the language
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Set language.
     * @param language The language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get web page.
     * @return the web page
     */
    public String getWebPage() {
        return this.webPage;
    }

    /**
     * Set web page.
     * @param webPage The web page
     */
    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    /**
     * Get downloads.
     * @return the downloads
     */
    public Integer getDownloads() {
        return this.downloads;
    }

    /**
     * Set downloads.
     * @param downloads The downloads
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

    /*---------- FILES ----------*/

    /**
     * Get files.
     * @return the set of files
     */
    public Set getFiles() {
        if (this.files == null) {
            this.files = new HashSet();
        }
        return this.files;
    }

    /**
     * Public method to get Files.
     * @return a list instead of a set
     */
    public List getFilesExternal() {
        List sortedItems = new ArrayList(getFiles());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set files.
     * @param files Collection of files associated with this item.
     */
    protected void setFiles(Set files) {
        this.files = files;
    }

    /**
     * Add a file.
     * @param file file to add
     */
    public void addFile(FileItem file) {
        if (!getFiles().contains(file)) {
            getFiles().add(file);
        }
    }

    /**
     * Remove the File Item.
     * @param item file item.
     */
    public void removeFile(FileItem item) {
        if (getFiles().contains(item)) {
            getFiles().remove(item);
        }
    }

    /*---------- STANDARD ITEM METHODS :: toString, equals, hashCode, comareTo ----------*/

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
         buffer.append(objectToStringFK("contributor", getContributor()));
         buffer.append(objectToString("name", getName()));
         buffer.append(objectToString("addedTime", getAddedTime()));
         buffer.append(objectToString("updatedTime", getUpdatedTime()));
         buffer.append(objectToString("description", getDescription()));
         buffer.append(objectToString("language", getLanguage()));
         buffer.append(objectToString("webPage", getWebPage()));
         buffer.append(objectToString("downloads", getDownloads()));
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
        if (obj instanceof ExternalToolItem) {
            ExternalToolItem otherItem = (ExternalToolItem)obj;

            if (!objectEqualsFK(this.getContributor(), otherItem.getContributor())) {
                return false;
            }
            if (!objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }
            if (!objectEquals(this.getAddedTime(), otherItem.getAddedTime())) {
                return false;
            }
            if (!objectEquals(this.getUpdatedTime(), otherItem.getUpdatedTime())) {
                return false;
            }
            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!objectEquals(this.getLanguage(), otherItem.getLanguage())) {
                return false;
            }
            if (!objectEquals(this.getWebPage(), otherItem.getWebPage())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getContributor());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAddedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUpdatedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLanguage());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWebPage());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDownloads());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>name</li>
     * <li>contributor</li>
     * <li>updatedTime</li>
     * <li>downloads</li>
     * <li>language</li>
     * <li>addedTime</li>
     * <li>description</li>
     * <li>webPage</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ExternalToolItem otherItem = (ExternalToolItem)obj;
        int value = 0;

        value = objectCompareTo(this.getName(), otherItem.getName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getContributor(), otherItem.getContributor());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUpdatedTime(), otherItem.getUpdatedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDownloads(), otherItem.getDownloads());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLanguage(), otherItem.getLanguage());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAddedTime(), otherItem.getAddedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWebPage(), otherItem.getWebPage());
        if (value != 0) { return value; }

        return value;
    }
}