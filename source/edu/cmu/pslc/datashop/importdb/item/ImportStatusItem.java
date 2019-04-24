/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the import status table.
 *
 * @author Alida Skogsholm
 * @version $Revision: 9267 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-05-14 16:46:12 -0400 (Tue, 14 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportStatusItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private int importStatusId;
    /** The dataset name. */
    private String datasetName;
    /** The domain name. */
    private String domainName;
    /** The learnlab name. */
    private String learnlabName;
    /** The time of the action. */
    private Date   timeStart;
    /** The time of the action. */
    private Date   timeEnd;
    /** The status of the import. */
    private String status;
    /** The count of the errors. */
    private int errorCount;
    /** The error message. */
    private String errorMessage;
    /** The count of the warnings. */
    private int warningCount;
    /** The warning message. */
    private String warningMessage;
    /** Collection of importFiles in this school. */
    private Set importFiles;

    /** Collection of all allowed items in the status enumeration. */
    private static final List STATUS_ENUM = new ArrayList();
    /** Status enumeration field value - "queued". */
    public static final String STATUS_QUEUED = "queued";
    /** Status enumeration field value - "verifying". */
    public static final String STATUS_VERIFYING_HEADERS = "verifying headers";
    /** Status enumeration field value - "loading". */
    public static final String STATUS_LOADING = "loading";
    /** Status enumeration field value - "verifying". */
    public static final String STATUS_VERIFYING_DATA = "verifying data";
    /** Status enumeration field value - "processing". */
    public static final String STATUS_PROCESSING = "processing";
    /** Status enumeration field value - "merging". */
    public static final String STATUS_MERGING = "merging";
    /** Status enumeration field value - "imported". */
    public static final String STATUS_IMPORTED = "imported";
    /** Status enumeration field value - "error". */
    public static final String STATUS_ERROR = "error";
    /** Status enumeration field value - "verified only". */
    public static final String STATUS_VERIFIED_ONLY = "verified only";

    /**
     * Adds each status to the status enumeration list.
     */
    static {
        STATUS_ENUM.add(STATUS_QUEUED);
        STATUS_ENUM.add(STATUS_VERIFYING_HEADERS);
        STATUS_ENUM.add(STATUS_LOADING);
        STATUS_ENUM.add(STATUS_VERIFYING_DATA);
        STATUS_ENUM.add(STATUS_PROCESSING);
        STATUS_ENUM.add(STATUS_MERGING);
        STATUS_ENUM.add(STATUS_IMPORTED);
        STATUS_ENUM.add(STATUS_ERROR);
        STATUS_ENUM.add(STATUS_VERIFIED_ONLY);
    }

    /** Default constructor. */
    public ImportStatusItem() {
        this.status = STATUS_QUEUED;
    }

    /**
     *  Constructor with id.
     *  @param importStatusId Database generated unique ID for this item.
     */
    public ImportStatusItem(int importStatusId) {
        this.importStatusId = importStatusId;
        this.status = STATUS_QUEUED;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() {
        return this.importStatusId;
    }

    /**
     * Set the id.
     * @param importStatusId Database generated unique Id for this item.
     */
    public void setId(int importStatusId) {
        this.importStatusId = importStatusId;
    }

    /**
     * Get the dataset name.
     * @return the dataset name as a String
     */
    public String getDatasetName() {
        return this.datasetName;
    }

    /**
     * Set the dataset name.
     * @param datasetName the dataset name
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Get the domain name.
     * @return the domain name as a String
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * Set the domain name.
     * @param domainName the domain name
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Get the learnlab name.
     * @return the learnlab name as a String
     */
    public String getLearnlabName() {
        return this.learnlabName;
    }

    /**
     * Set the learnlab name.
     * @param learnlabName the learnlab name
     */
    public void setLearnlabName(String learnlabName) {
        this.learnlabName = learnlabName;
    }

    /**
     * Get time start.
     * @return java.util.Date
     */
    public Date getTimeStart() {
        return this.timeStart;
    }

    /**
     * Set time start.
     * @param timeStart The start time of this import.
     */
    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }

    /**
     * Get time end.
     * @return java.util.Date
     */
    public Date getTimeEnd() {
        return this.timeEnd;
    }

    /**
     * Set time end.
     * @param timeEnd The end time of this import.
     */
    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }

    /**
     * Get status.
     * @return java.lang.String
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Set status.
     * @param status Status of the import.
     */
    public void setStatus(String status) {
        if (STATUS_ENUM.contains(status)) {
            this.status = status;
        } else {
            throw new LogException(
                    "Import Status status can only be queued, loading,"
                    + " verifying, processing, merging, imported, error"
                    + " and no longer exists and not : " + status);
        }
    }

    /**
     * Get number of errors.
     * @return number of errors
     */
    public int getErrorCount() {
        return this.errorCount;
    }

    /**
     * Set number of errors.
     * @param errorCount number of errors.
     */
    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    /**
     * Get errorMessage.
     * @return java.lang.String
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Set errorMessage.
     * @param errorMessage error message.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get number of warnings.
     * @return number of warnings
     */
    public int getWarningCount() {
        return this.warningCount;
    }

    /**
     * Set number of warnings.
     * @param warningCount number of warnings.
     */
    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    /**
     * Get warningMessage.
     * @return java.lang.String
     */
    public String getWarningMessage() {
        return this.warningMessage;
    }

    /**
     * Set warningMessage.
     * @param warningMessage warning message.
     */
    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    /**
     * Get importFiles.
     * @return java.util.Set
     */
    protected Set getImportFiles() {
        if (this.importFiles == null) {
            this.importFiles = new HashSet();
        }
        return this.importFiles;
    }

    /**
     * Public method to get Import Files.
     * @return a list instead of a set
     */
    public List getImportFilesExternal() {
        List sortedList = new ArrayList(getImportFiles());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a importFiles.
     * @param item importFiles to add
     */
    public void addImportFiles(ImportFileInfoItem item) {
        getImportFiles().add(item);
        item.setImportStatus(this);
    }

    /**
     * Remove a importFiles.
     * @param item importFiles to remove
     */
    public void removeImportFile(ImportFileInfoItem item) {
        getImportFiles().remove(item);
        item.setImportStatus(null);
    }

    /**
     * Set importFiles.
     * @param importFiles Collection of importFiles belongs to this status.
     */
    public void setImportFiles(Set importFiles) {
        this.importFiles = importFiles;
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
         buffer.append(objectToString("DatasetName", getDatasetName()));
         buffer.append(objectToString("DomainName", getDomainName()));
         buffer.append(objectToString("LearnlabName", getLearnlabName()));
         buffer.append(objectToString("TimeStart", getTimeStart()));
         buffer.append(objectToString("TimeEnd", getTimeEnd()));
         buffer.append(objectToString("Status", getStatus()));
         buffer.append(objectToString("ErrorCount", getErrorCount()));
         buffer.append(objectToString("ErrorMessage", getErrorMessage()));
         buffer.append(objectToString("WarningCount", getWarningCount()));
         buffer.append(objectToString("WarningMessage", getWarningMessage()));
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
        if (obj instanceof ImportStatusItem) {
            ImportStatusItem otherItem = (ImportStatusItem)obj;

            if (!objectEquals(this.getDatasetName(), otherItem.getDatasetName())) {
                return false;
            }
            if (!objectEquals(this.getDomainName(), otherItem.getDomainName())) {
                return false;
            }
            if (!objectEquals(this.getLearnlabName(), otherItem.getLearnlabName())) {
                return false;
            }
            if (!objectEquals(this.getTimeStart(), otherItem.getTimeStart())) {
                return false;
            }
            if (!objectEquals(this.getTimeEnd(), otherItem.getTimeEnd())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!objectEquals(this.getErrorCount(), otherItem.getErrorCount())) {
                return false;
            }
            if (!objectEquals(this.getErrorMessage(), otherItem.getErrorMessage())) {
                return false;
            }
            if (!objectEquals(this.getWarningCount(), otherItem.getWarningCount())) {
                return false;
            }
            if (!objectEquals(this.getWarningMessage(), otherItem.getWarningMessage())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDatasetName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDomainName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLearnlabName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeStart());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeEnd());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getErrorCount());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getErrorMessage());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWarningCount());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWarningMessage());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset name</li>
     * <li>domain name</li>
     * <li>learnlab name</li>
     * <li>time start</li>
     * <li>time end</li>
     * <li>status</li>
     * <li>error count</li>
     * <li>error message</li>
     * <li>warning count</li>
     * <li>warning message</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ImportStatusItem otherItem = (ImportStatusItem)obj;
        int value = 0;

        value = objectCompareTo(this.getDatasetName(), otherItem.getDatasetName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDomainName(), otherItem.getDomainName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLearnlabName(), otherItem.getLearnlabName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeStart(), otherItem.getTimeStart());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeEnd(), otherItem.getTimeEnd());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getErrorCount(), otherItem.getErrorCount());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getErrorMessage(), otherItem.getErrorMessage());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWarningCount(), otherItem.getWarningCount());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWarningMessage(), otherItem.getWarningMessage());
        if (value != 0) { return value; }

        return value;
    }
}