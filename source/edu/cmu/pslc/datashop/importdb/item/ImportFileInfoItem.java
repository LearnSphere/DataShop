/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single row in the import file info table.
 *
 * @author Shanwen Yu
 * @version $Revision: 7503 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-03-12 15:15:15 -0400 (Mon, 12 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportFileInfoItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique ID for this item. */
    private int importFileId;
    /** The import status item. */
    private ImportStatusItem importStatus;
    /** The full path of the file. */
    private String fileName;
    /** The number of the start line. */
    private int lineStart;
    /** The number of the end line. */
    private int lineEnd;
    /** The start time of the import. */
    private Date   timeStart;
    /** The end time of the import. */
    private Date   timeEnd;
    /** Status. */
    private String status;
    /** The count of the errors. */
    private int errorCount;
    /** The error message. */
    private String errorMessage;
    /** The count of the warnings. */
    private int warningCount;
    /** The warning message. */
    private String warningMessage;

    /** Collection of all allowed items in the status enumeration. */
    private static final List STATUS_ENUM = new ArrayList();
    /** Status enumeration field value - "queued". */
    public static final String STATUS_QUEUED = "queued";
    /** Status enumeration field value - "loading". */
    public static final String STATUS_LOADING = "loading";
    /** Status enumeration field value - "loaded". */
    public static final String STATUS_LOADED = "loaded";
    /** Status enumeration field value - "error". */
    public static final String STATUS_ERROR = "error";

    /**
     * Adds each status to the status enumeration list.
     */
    static {
        STATUS_ENUM.add(STATUS_QUEUED);
        STATUS_ENUM.add(STATUS_LOADING);
        STATUS_ENUM.add(STATUS_LOADED);
        STATUS_ENUM.add(STATUS_ERROR);
    }

    /** Default constructor. */
    public ImportFileInfoItem() {
        this.status = STATUS_QUEUED;
    }

    /**
     *  Constructor with id.
     *  @param importFileId the database generated unique ID for this item.
     */
    public ImportFileInfoItem(int importFileId) {
        this.importFileId = importFileId;
        this.status = STATUS_QUEUED;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() {
        return this.importFileId;
    }

    /**
     * Set the id.
     * @param importFileId Database generated unique Id for this item.
     */
    public void setId(int importFileId) {
        this.importFileId = importFileId;
    }

    /**
     * Get the import status item.
     * @return the importStatus item
     */
    public ImportStatusItem getImportStatus() {
        return this.importStatus;
    }

    /**
     * Set the import status item.
     * @param importStatus the import status item
     */
    public void setImportStatus(ImportStatusItem importStatus) {
        this.importStatus = importStatus;
    }

    /**
     * Get file name.
     * @return the file name
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Set file name.
     * @param fileName The name of the file to be imported.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get start line.
     * @return the number of start line in import_file_data
     */
    public int getLineStart() {
        return this.lineStart;
    }

    /**
     * Set action.
     * @param lineStart the number of start line in import_file_data
     */
    public void setLineStart(int lineStart) {
        this.lineStart = lineStart;
    }

    /**
     * Get end line.
     * @return the number of end line in import_file_data
     */
    public int getLineEnd() {
        return this.lineEnd;
    }

    /**
     * Get start time.
     * @return the start time
     */
    public Date getTimeStart() {
        return this.timeStart;
    }

    /**
     * Set start time.
     * @param timeStart The start time of this action.
     */
    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }

    /**
     * Get end time.
     * @return the end time
     */
    public Date getTimeEnd() {
        return this.timeEnd;
    }

    /**
     * Set end time.
     * @param timeEnd The end time of this action.
     */
    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }



    /**
     * Set end line.
     * @param lineEnd the number of end line in import_file_data
     */
    public void setLineEnd(int lineEnd) {
        this.lineEnd = lineEnd;
    }

    /**
     * Get status.
     * @return status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Set status.
     * @param status the status of this import
     */
    public void setStatus(String status) {
        if (STATUS_ENUM.contains(status)) {
            this.status = status;
        } else {
            throw new LogException(
                    "Import File Info status can only be queued, verifying headers,"
                    + " loading, loaded, error and no longer exists and not : " + status);
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
     * Get the error message.
     * @return the error message
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
     * Get the warning message.
     * @return the warning message
     */
    public String getWarningMessage() {
        return this.warningMessage;
    }

    /**
     * Set the warning message.
     * @param warningMessage warning message.
     */
    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
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
         buffer.append(objectToString("FileName", getFileName()));
         buffer.append(objectToString("LineStart", getLineStart()));
         buffer.append(objectToString("LineEnd", getLineEnd()));
         buffer.append(objectToString("TimeStart", getTimeStart()));
         buffer.append(objectToString("TimeEnd", getTimeEnd()));
         buffer.append(objectToString("Status", getStatus()));
         buffer.append(objectToString("ErrorCount", getErrorCount()));
         buffer.append(objectToString("ErrorMessage", getErrorMessage()));
         buffer.append(objectToString("WarningCount", getWarningCount()));
         buffer.append(objectToString("WarningMessage", getWarningMessage()));
         buffer.append(objectToStringFK("ImportStatus", getImportStatus()));
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
        if (obj instanceof ImportFileInfoItem) {
            ImportFileInfoItem otherItem = (ImportFileInfoItem)obj;

            if (!objectEquals(this.getFileName(), otherItem.getFileName())) {
                return false;
            }
            if (!objectEquals(this.getLineStart(), otherItem.getLineStart())) {
                return false;
            }
            if (!objectEquals(this.getLineEnd(), otherItem.getLineEnd())) {
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
            if (!objectEqualsFK(this.getImportStatus(), otherItem.getImportStatus())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFileName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLineStart());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLineEnd());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeStart());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeEnd());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getErrorCount());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getErrorMessage());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWarningCount());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWarningMessage());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getImportStatus());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>file name</li>
     * <li>line start</li>
     * <li>line end</li>
     * <li>action</li>
     * <li>time start</li>
     * <li>time end</li>
     * <li>status</li>
     * <li>error count</li>
     * <li>error message</li>
     * <li>warning count</li>
     * <li>warning message</li>
     * <li>import status</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ImportFileInfoItem otherItem = (ImportFileInfoItem)obj;
        int value = 0;

        value = objectCompareTo(this.getFileName(), otherItem.getFileName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLineStart(), otherItem.getLineStart());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLineEnd(), otherItem.getLineEnd());
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

        value = objectCompareToFK(this.getImportStatus(), otherItem.getImportStatus());
        if (value != 0) { return value; }

        return value;
    }
}