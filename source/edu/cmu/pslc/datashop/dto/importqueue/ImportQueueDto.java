/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto.importqueue;

import java.util.Comparator;
import java.text.DecimalFormat;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * Data transfer object of items in the Import Queue.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueDto {

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private Integer importQueueId;
    /** Class attribute. */
    private Integer queueOrder;
    /** Class attribute. */
    private String queuePosition;
    /** Class attribute. */
    private Integer projectId;
    /** Class attribute. */
    private String projectName;
    /** Class attribute. */
    private Integer datasetId;
    /** Class attribute. */
    private String datasetName;
    /** Class attribute. */
    private Boolean releasedFlag;
    /** Class attribute. */
    private Integer fileId;
    /** Class attribute. */
    private String fileName;
    /** Class attribute. */
    private Long fileSize;
    /** Class attribute. */
    private String description;
    /** Class attribute. */
    private String domainName;
    /** Class attribute. */
    private String learnlabName;
    /** Class attribute. */
    private String uploadedByName;
    /** Class attribute. */
    private String email;
    /** Class attribute. */
    private String uploadedByUserName;
    /** Class attribute. */
    private Date uploadedTime;
    /** Class attribute. */
    private String format;
    /** Class attribute. */
    private String status;
    /** Class attribute. */
    private String statusMessage;
    /** Class attribute. */
    private String imageName;
    /** Class attribute. */
    private Date estImportDate;
    /** Class attribute. */
    private Integer numErrors;
    /** Class attribute. */
    private Integer numIssues;
    /** Class attribute. */
    private String verificationResults;
    /** Class attribute. */
    private Long numTransactions;
    /** Class attribute. */
    private Date lastUpdatedTime;
    /** Class attribute. */
    private Boolean anonFlag;
    /** Class attribute. */
    private Boolean displayFlag = false;
    /** Class attribute. */
    private Boolean showUndoFlag = false;
    /** Class attribute. */
    private Boolean accessedFlag;
    /** Class attribute. */
    private Boolean fromSampleWithUserKCs;
    /** Class attribute. */
    private Integer srcDatasetId;
    /** Class attribute. */
    private String srcDatasetName;
    /** Class attribute. */
    private Integer srcSampleId;
    /** Class attribute. */
    private String srcSampleName;
    /** Class attribute. */
    private Long discourseId = null;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ImportQueueDto() { }

    //----- TO STRING -----

    /**
     * Returns a string representation of the import queue id.
     * @return a string representation of this object
     */
     public String idToString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("IQ id(");
         buffer.append(importQueueId);
         buffer.append(").");
         return buffer.toString();
     }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("IQ id(");
         buffer.append(importQueueId);
         buffer.append("), uploaded by ");
         buffer.append(uploadedByName);
         buffer.append(" on ");
         buffer.append(uploadedTime);
         buffer.append(", last updated ");
         buffer.append(getLastUpdateString());
         buffer.append(".");
         return buffer.toString();
     }

    //----- GETTERS and SETTERS -----

    /**
     * Gets importQueueId.
     * @return the importQueueId
     */
    public Integer getImportQueueId() {
        return importQueueId;
    }

    /**
     * Sets the importQueueId.
     * @param importQueueId the importQueueId to set
     */
    public void setImportQueueId(Integer importQueueId) {
        this.importQueueId = importQueueId;
    }

    /**
     * Gets order.
     * @return the order
     */
    public Integer getOrder() {
        return queueOrder;
    }

    /**
     * Sets the order.
     * @param order the order to set
     */
    public void setOrder(Integer order) {
        this.queueOrder = order;
    }

    /**
     * Gets queuePosition.
     * @return the queuePosition
     */
    public String getQueuePosition() {
        return queuePosition;
    }

    /**
     * Sets the queuePosition.
     * @param queuePosition the queuePosition to set
     */
    public void setQueuePosition(String queuePosition) {
        this.queuePosition = queuePosition;
    }

    /**
     * Gets projectId.
     * @return the projectId
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Sets the projectId.
     * @param projectId the projectId to set
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Gets projectName.
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the projectName.
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Gets datasetId.
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }

    /**
     * Sets the datasetId.
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Gets datasetName.
     * @return the datasetName
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * Sets the datasetName.
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Gets releasedFlag.
     * @return the releasedFlag
     */
    public Boolean getReleasedFlag() {
        return releasedFlag;
    }

    /**
     * Sets the releasedFlag.
     * @param releasedFlag the releasedFlag to set
     */
    public void setReleasedFlag(Boolean releasedFlag) {
        this.releasedFlag = releasedFlag;
    }

    /**
     * Gets accessedFlag.
     * @return the accessedFlag
     */
    public Boolean getAccessedFlag() {
        return accessedFlag;
    }

    /**
     * Sets the accessedFlag.
     * @param accessedFlag the accessedFlag to set
     */
    public void setAccessedFlag(Boolean accessedFlag) {
        this.accessedFlag = accessedFlag;
    }

    /**
     * Whether or not the import queue item includes user-created KCMs.
     * If null, then the import queue item was not created from an existing sample.
     * @return the accessedFlag
     */
    public Boolean getFromSampleWithUserKCs() {
        return fromSampleWithUserKCs;
    }

    /**
     * Set whether or not the import queue item includes user-created KCMss.
     * @param fromSampleWithUserKCs whether or not the import queue item includes user-created KCMs;
     * If null, then the import queue item was not created from an existing sample.
     */
    public void setFromSampleWithUserKCs(Boolean fromSampleWithUserKCs) {
        this.fromSampleWithUserKCs = fromSampleWithUserKCs;
    }



    /** Get the source dataset id.
     * @return the srcDatasetId
     */
    public Integer getSrcDatasetId() {
        return srcDatasetId;
    }

    /** Set the source dataset id.
     * @param srcDatasetId the srcDatasetId to set
     */
    public void setSrcDatasetId(Integer srcDatasetId) {
        this.srcDatasetId = srcDatasetId;
    }

    /** Get the source dataset name.
     * @return the srcDatasetName
     */
    public String getSrcDatasetName() {
        return srcDatasetName;
    }

    /** Set the source dataset name.
     * @param srcDatasetName the srcDatasetName to set
     */
    public void setSrcDatasetName(String srcDatasetName) {
        this.srcDatasetName = srcDatasetName;
    }

    /** Get the source sample id.
     * @return the srcSampleId
     */
    public Integer getSrcSampleId() {
        return srcSampleId;
    }

    /** Set the source sample id.
     * @param srcSampleId the srcSampleId to set
     */
    public void setSrcSampleId(Integer srcSampleId) {
        this.srcSampleId = srcSampleId;
    }

    /** Get the source sample name.
     * @return the srcSampleName
     */
    public String getSrcSampleName() {
        return srcSampleName;
    }

    /** Set the source sample name.
     * @param srcSampleName the srcSampleName to set
     */
    public void setSrcSampleName(String srcSampleName) {
        this.srcSampleName = srcSampleName;
    }

    /**
     * Gets fileId.
     * @return the fileId
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * Sets the fileId.
     * @param fileId the fileId to set
     */
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    /**
     * Gets fileName.
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the fileName.
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets fileSize.
     * @return the fileSize
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the fileSize.
     * @param fileSize the fileSize to set
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Gets a nice displayable version of the file name including the file size.
     * @return a displayable string
     */
    public String getDisplayFileName() {
        return fileName + " (" + FileItem.getDisplayFileSize(fileSize) + ")";
    }

    /**
     * Gets description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets domainName.
     * @return the domainName
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Sets the domainName.
     * @param domainName the domainName to set
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Gets learnlabName.
     * @return the learnlabName
     */
    public String getLearnlabName() {
        return learnlabName;
    }

    /**
     * Sets the learnlabName.
     * @param learnlabName the learnlabName to set
     */
    public void setLearnlabName(String learnlabName) {
        this.learnlabName = learnlabName;
    }

    /**
     * Gets uploadedByName.
     * @return the uploadedByName
     */
    public String getUploadedByName() {
        return uploadedByName;
    }

    /**
     * Sets the uploadedByName.
     * @param uploadedByName the uploadedByName to set
     */
    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    /**
     * Gets email.
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets uploadedByUserName.
     * @return the uploadedByUserName
     */
    public String getUploadedByUserName() {
        return uploadedByUserName;
    }

    /**
     * Sets the uploadedByUserName.
     * @param uploadedByUserName the uploadedByUserName to set
     */
    public void setUploadedByUserName(String uploadedByUserName) {
        this.uploadedByUserName = uploadedByUserName;
    }

    /**
     * Gets uploadedTime.
     * @return the uploadedTime
     */
    public Date getUploadedTime() {
        return uploadedTime;
    }

    /**
     * Sets the uploadedTime.
     * @param uploadedTime the uploadedTime to set
     */
    public void setUploadedTime(Date uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    /**
     * Gets format.
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format.
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /** Constant for display purposes. */
    private static final String FORMAT_TAB_DISPLAY = "tab-delimited format";
    /** Constant for display purposes. */
    private static final String FORMAT_XML_DISPLAY = "tutor message xml format";
    /** Constant for display purposes. */
    private static final String FORMAT_DISCOURSE_DISPLAY = "discourse db format";

    /**
     * Gets format, formatted for display.
     * @return the format
     */
    public String getFormatString() {
        if (format == null) { return ""; }
        if (format.equals(ImportQueueItem.FORMAT_TAB)) {
            return FORMAT_TAB_DISPLAY;
        } else if (format.equals(ImportQueueItem.FORMAT_XML)) {
            return FORMAT_XML_DISPLAY;
        } else if (format.equals(ImportQueueItem.FORMAT_DISCOURSE)) {
            return FORMAT_DISCOURSE_DISPLAY;
        } else {
            return format + " format";
        }
    }

    /**
     * Gets status.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;

        if (status.equals(ImportQueueItem.STATUS_LOADING)
           || status.equals(ImportQueueItem.STATUS_GENING)
           || status.equals(ImportQueueItem.STATUS_AGGING)) {
            imageName = "images/iq_spinner.gif";
            statusMessage = ImportQueueItem.STATUS_TXT_MAP.get(status);
        } else if (status.equals(ImportQueueItem.STATUS_QUEUED)) {
            imageName = "images/clock.png";
            statusMessage = ImportQueueItem.STATUS_TXT_MAP.get(status);
        } else if (status.equals("passed")) {
            imageName = "images/tick.png";
            statusMessage = ImportQueueItem.STATUS_TXT_MAP.get(status);
        } else if (status.equals("errors")) {
            imageName = "images/cross.png";
            statusMessage = numErrors + " errors found";
        } else if (status.equals("issues")) {
            imageName = "images/help.gif";
            statusMessage = numIssues + " issues found";
        } else if (status.equals(ImportQueueItem.STATUS_LOADED)) {
            imageName = "images/tick.png";
            statusMessage = ImportQueueItem.STATUS_TXT_MAP.get(status);
            if (format.equals(ImportQueueItem.FORMAT_DISCOURSE)) {
                statusMessage = ImportQueueItem.STATUS_TXT_MAP_DISCOURSE.get(status);
            }
        } else if (status.equals("canceled")) {
            imageName = "images/cross.png";
            statusMessage = ImportQueueItem.STATUS_TXT_MAP.get(status);
        } else {
            imageName = "images/help.gif";
            statusMessage = "Unknown state";
        }
    }

    /**
     * Gets statusMessage.
     * @return the statusMessage
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Gets imageName.
     * @return the imageName
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Gets estImportDate.
     * @return the estImportDate
     */
    public Date getEstImportDate() {
        return estImportDate;
    }

    /** Date format for estimated import date. */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMMMM d, yyyy");

    /**
     * Gets estImportDate.
     * @return the estImportDate
     */
    public String getEstImportDateFormatted() {
        String dateFormatted = "";
        if (estImportDate == null) {
            dateFormatted += "unknown";
        } else {
            dateFormatted += DATE_FORMAT.format(estImportDate);
        }
        return dateFormatted;
    }

    /**
     * Sets the estImportDate.
     * @param estImportDate the estImportDate to set
     */
    public void setEstImportDate(Date estImportDate) {
        this.estImportDate = estImportDate;
    }

    /**
     * Gets numErrors.
     * @return the numErrors
     */
    public Integer getNumErrors() {
        return numErrors;
    }

    /**
     * Sets the numErrors.
     * @param numErrors the numErrors to set
     */
    public void setNumErrors(Integer numErrors) {
        this.numErrors = numErrors;
        if (status.equals("errors")) {
            if (numErrors == null) {
                statusMessage = "Errors found";
            } else {
                statusMessage = numErrors + " errors found";
            }
        }
    }

    /**
     * Gets numIssues.
     * @return the numIssues
     */
    public Integer getNumIssues() {
        return numIssues;
    }

    /**
     * Sets the numIssues.
     * @param numIssues the numIssues to set
     */
    public void setNumIssues(Integer numIssues) {
        this.numIssues = numIssues;
        if (status.equals("issues")) {
            if (numIssues == null) {
                statusMessage = "Issues found";
            } else {
                statusMessage = numIssues + " potential issues found";
            }
        }
    }

    /**
     * Gets verificationResults.
     * @return the verificationResults
     */
    public String getVerificationResults() {
        return verificationResults;
    }

    /**
     * Sets the verificationResults.
     * @param verificationResults the verificationResults to set
     */
    public void setVerificationResults(String verificationResults) {
        this.verificationResults = verificationResults;
    }

    /**
     * Gets numTransactions.
     * @return the numTransactions
     */
    public Long getNumTransactions() {
        return numTransactions;
    }

    /**
     * Sets the numTransactions.
     * @param numTransactions the numTransactions to set
     */
    public void setNumTransactions(Long numTransactions) {
        this.numTransactions = numTransactions;
    }

    /**
     * Get the number of transactions, nicely formatted.
     * @return the numTransactions as a string
     */
    public String getNumTransactionsFormatted() {
        if (numTransactions == null) { return ""; }
        return new DecimalFormat("##,###,##0").format(numTransactions);
    }

    /**
     * Gets lastUpdatedTime.
     * @return the lastUpdatedTime
     */
    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    /** Constant. */
    private static final int NUM_MILLI = 1000;
    /** Constant. */
    private static final int NUM_SECS = 60;
    /** Constant. */
    private static final int NUM_MINS = 60;
    /** Constant. */
    private static final int NUM_HOURS = 24;
    /** Constant. */
    private static final int NUM_DAYS = 7;

    /**
     * Get display string for the last updated time in the number of
     * seconds/minutes/etc ago.
     * @return a user friendly string of when item was last updated
     */
    public String getLastUpdateString() {
        return getLastUpdateString(lastUpdatedTime);
    }

    /**
     * Get display string for the last updated time in the number of
     * seconds/minutes/etc ago.
     * This method should maybe be moved to a utility class.
     * @param time the time to get a number of time-units ago for
     * @return a user friendly string of when item was last updated
     */
    public static String getLastUpdateString(Date time) {
        if (time == null) {
           return "";
        }
        return getAmountOfTime(time) + " ago";
    }

    /**
     * Get display string for an amount of time from now
     * since the given date, in seconds/minutes/etc.
     * This method should maybe be moved to a utility class.
     * @param time the time to get a number of time-units ago for
     * @return a user friendly string for an amount of time
     */
    public static String getAmountOfTime(Date time) {
        if (time == null) {
           return "";
        }

        String agoString;
        Date now = new Date();
        long diffMilli = now.getTime() - time.getTime();

        long diffSecs = diffMilli / NUM_MILLI;
        if (diffSecs == 1) {
            agoString = diffSecs + " second";
        } else if (diffSecs < NUM_SECS) {
            agoString = diffSecs + " seconds";
        } else {
            long diffMins = diffSecs / NUM_SECS;
            if (diffMins == 1) {
                agoString = diffMins + " minute";
            } else if (diffMins < NUM_MINS) {
                agoString = diffMins + " minutes";
            } else {
                long diffHours = diffMins / NUM_MINS;
                if (diffHours == 1) {
                    agoString = diffHours + " hour";
                } else if (diffHours < NUM_HOURS) {
                    agoString = diffHours + " hours";
                } else {
                    long diffDays = diffHours / NUM_HOURS;
                    if (diffDays == 1) {
                        agoString = diffDays + " day";
                    } else if (diffDays < NUM_DAYS) {
                        agoString = diffDays + " days";
                    } else {
                        long diffWeeks = diffDays / NUM_DAYS;
                        if (diffWeeks == 1) {
                            agoString = diffWeeks + " week";
                        } else {
                            agoString = diffWeeks + " weeks";
                        }
                    }
                }
            }
        }

        return agoString;
    }

    /**
     * Sets the lastUpdatedTime.
     * @param lastUpdatedTime the lastUpdatedTime to set
     */
    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    /**
     * Gets anonFlag.
     * @return the anonFlag
     */
    public Boolean getAnonFlag() {
        return anonFlag;
    }

    /**
     * Sets the anonFlag.
     * @param anonFlag the anonFlag to set
     */
    public void setAnonFlag(Boolean anonFlag) {
        this.anonFlag = anonFlag;
    }

    /**
     * Gets displayFlag.
     * @return the displayFlag
     */
    public Boolean getDisplayFlag() {
        return displayFlag;
    }

    /**
     * Sets the displayFlag.
     * @param displayFlag the displayFlag to set
     */
    public void setDisplayFlag(Boolean displayFlag) {
        this.displayFlag = displayFlag;
    }

    /**
     * Gets showUndoFlag. Not from DB, set in Servlet from context info in HttpSession.
     * @return the showUndoFlag
     */
    public Boolean getShowUndoFlag() {
        return showUndoFlag;
    }

    /**
     * Sets the showUndoFlag.
     * @param showUndoFlag the showUndoFlag to set
     */
    public void setShowUndoFlag(Boolean showUndoFlag) {
        this.showUndoFlag = showUndoFlag;
    }

    /**
     * Get the discourseId, iff IQ item is mapped to a Discourse.
     * @return Long id for Discourse
     */
    public Long getDiscourseId() { return discourseId; }

    /**
     * Set the discourseId, iff IQ item is mapped to a Discourse.
     * @param discourseId the Discourse id
     */
    public void setDiscourseId(Long discourseId) { this.discourseId = discourseId; }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the 'Dataset' column. */
    public static final String COLUMN_DATASET = "Dataset";
    /** Constant for the 'User' column. */
    public static final String COLUMN_USER = "User";
    /** Constant for the 'Status' column. */
    public static final String COLUMN_STATUS = "Status";
    /** Constant for the 'Last Update' column. */
    public static final String COLUMN_LAST_UPDATE = "Last Update";

    /** Sort parameters array constant for sorting by dataset ascending. */
    private static final SortParameter[] ASC_DATASET_PARAMS = {
                         SortParameter.SORT_BY_DATASET_ASC };
    /** Sort parameters array constant for sorting by dataset descending. */
    private static final SortParameter[] DESC_DATASET_PARAMS = {
                         SortParameter.SORT_BY_DATASET_DESC };
    /** Sort parameters array constant for sorting by user ascending. */
    private static final SortParameter[] ASC_USER_PARAMS = {
                         SortParameter.SORT_BY_USER_ASC,
                         SortParameter.SORT_BY_LAST_UPDATE_ASC,
                         SortParameter.SORT_BY_DATASET_ASC };
    /** Sort parameters array constant for sorting by user descending. */
    private static final SortParameter[] DESC_USER_PARAMS = {
                         SortParameter.SORT_BY_USER_DESC,
                         SortParameter.SORT_BY_LAST_UPDATE_ASC,
                         SortParameter.SORT_BY_DATASET_ASC };
    /** Sort parameters array constant for sorting by status ascending. */
    private static final SortParameter[] ASC_STATUS_PARAMS = {
                         SortParameter.SORT_BY_STATUS_ASC,
                         SortParameter.SORT_BY_LAST_UPDATE_ASC,
                         SortParameter.SORT_BY_DATASET_ASC };
    /** Sort parameters array constant for sorting by status descending. */
    private static final SortParameter[] DESC_STATUS_PARAMS = {
                         SortParameter.SORT_BY_STATUS_DESC,
                         SortParameter.SORT_BY_LAST_UPDATE_ASC,
                         SortParameter.SORT_BY_DATASET_ASC };
    /** Sort parameters array constant for sorting by last update ascending. */
    private static final SortParameter[] ASC_LAST_UPDATE_PARAMS = {
                         SortParameter.SORT_BY_LAST_UPDATE_ASC,
                         SortParameter.SORT_BY_DATASET_ASC };
    /** Sort parameters array constant for sorting by last update descending. */
    private static final SortParameter[] DESC_LAST_UPDATE_PARAMS = {
                         SortParameter.SORT_BY_LAST_UPDATE_DESC,
                         SortParameter.SORT_BY_DATASET_ASC };

    //----- METHODS FOR SORTING -----

    /**
     * Returns the relative path to the appropriate image.
     * @param columnName the column name to get an image for
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return relative path to image for given column
     */
    public static String getSortImage(String columnName,
                                      String sortByColumn, Boolean isAscending) {

        String imgIcon = "images/trans_spacer.gif";
        if (sortByColumn != null && sortByColumn.equals(columnName)) {
            imgIcon = isAscending ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /**
     * Gets the current sortBy parameters.
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return the current sortBy parameters.
     */
    public static SortParameter[] getSortByParameters(String sortByColumn, Boolean isAscending) {

        SortParameter[] sortParams = ASC_DATASET_PARAMS;

        if (sortByColumn.equals(COLUMN_DATASET)) {
            if (isAscending) {
                sortParams = ASC_DATASET_PARAMS;
            } else {
                sortParams = DESC_DATASET_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_USER)) {
            if (isAscending) {
                sortParams = ASC_USER_PARAMS;
            } else {
                sortParams = DESC_USER_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_STATUS)) {
            if (isAscending) {
                sortParams = ASC_STATUS_PARAMS;
            } else {
                sortParams = DESC_STATUS_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_LAST_UPDATE)) {
            if (isAscending) {
                sortParams = ASC_LAST_UPDATE_PARAMS;
            } else {
                sortParams = DESC_LAST_UPDATE_PARAMS;
            }
        }
        return sortParams;
    }

    //----- SORTING COMPARATOR -----

    /**
     * Comparator object used for sorting.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<ImportQueueDto> getComparator(SortParameter... sortParameters) {
        return new ImportQueueDtoComparator(sortParameters);
    }

    public enum SortParameter {
        /** Dataset Ascending. */
        SORT_BY_DATASET_ASC,
        /** Dataset Descending. */
        SORT_BY_DATASET_DESC,
        /** User Ascending. */
        SORT_BY_USER_ASC,
        /** User Descending. */
        SORT_BY_USER_DESC,
        /** Status Ascending. */
        SORT_BY_STATUS_ASC,
        /** Status Descending. */
        SORT_BY_STATUS_DESC,
        /** Last update Ascending. */
        SORT_BY_LAST_UPDATE_ASC,
        /** Last update Descending. */
        SORT_BY_LAST_UPDATE_DESC,
    }

    /**
     * Comparator for ImportQueueDto objects.
     */
    private static final class ImportQueueDtoComparator implements Comparator<ImportQueueDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private ImportQueueDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(ImportQueueDto o1, ImportQueueDto o2) {
            if (parameters == null) {
                SortParameter[] params = {SortParameter.SORT_BY_LAST_UPDATE_ASC};
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                //--- Ascending ---
                case SORT_BY_DATASET_ASC:
                    result = o1.getDatasetName().compareToIgnoreCase(o2.getDatasetName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_USER_ASC:
                    result = o1.getUploadedByName().compareToIgnoreCase(o2.getUploadedByName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_STATUS_ASC:
                    result = o1.getStatus().compareToIgnoreCase(o2.getStatus());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LAST_UPDATE_ASC:
                    result = dateComparison(o1.getLastUpdatedTime(),
                                            o2.getLastUpdatedTime(), true, false);
                    if (result != 0) { return result; }
                    break;
                //--- Descending ---
                case SORT_BY_DATASET_DESC:
                    result = o2.getDatasetName().compareToIgnoreCase(o1.getDatasetName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_USER_DESC:
                    result = o2.getUploadedByName().compareToIgnoreCase(o1.getUploadedByName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_STATUS_DESC:
                    result = o2.getStatus().compareToIgnoreCase(o1.getStatus());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LAST_UPDATE_DESC:
                    result = dateComparison(o1.getLastUpdatedTime(),
                                            o2.getLastUpdatedTime(), false, false);
                    if (result != 0) { return result; }
                    break;
                default:
                    // No-op
                }
            }
            return 0;
        }
    }
}
