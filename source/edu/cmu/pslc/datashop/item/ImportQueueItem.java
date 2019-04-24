/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * An item in the import queue.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13540 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-09-22 14:16:36 -0400 (Thu, 22 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueItem extends Item implements java.io.Serializable, Comparable  {

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Class attribute. */
    private Integer queueOrder;
    /** Class attribute. */
    private ProjectItem project;
    /** Class attribute. */
    private DatasetItem dataset;
    /** Class attribute. */
    private String datasetName;
    /** Class attribute. */
    private String description;
    /** Class attribute. */
    private FileItem file;
    /** Class attribute. */
    private UserItem uploadedBy;
    /** Class attribute. */
    private Date uploadedTime;
    /** Class attribute. */
    private String format;
    /** Class attribute. */
    private String status;
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
    private Boolean displayFlag = true;
    /** Class attribute. */
    private Integer importStatusId;
    /** Class attribute. */
    private String domainName;
    /** Class attribute. */
    private String learnlabName;
    /** Flag indicating whether this dataset was a study. */
    private String studyFlag = DatasetItem.STUDY_FLAG_NOT_SPEC;
    /** Sample to Dataset attribute. */
    private Boolean includeUserKCMs;
    /** Sample to Dataset attribute. */
    private Integer srcDatasetId;
    /** Sample to Dataset attribute. */
    private String srcDatasetName;
    /** Sample to Dataset attribute. */
    private Integer srcSampleId;
    /** Sample to Dataset attribute. */
    private String srcSampleName;
    /** Additional dataset notes. */
    private String adtlDatasetNotes;
    /** Flag indicating data came from existing dataset. */
    private Boolean fromExistingDatasetFlag = false;

    //----- ENUM : format -----



    /** Enumerated type list of valid values. */
    private static final List<String> FORMAT_ENUM = new ArrayList<String>();
    /** Enumerated type constant for format, "tab_delimited". */
    public static final String FORMAT_TAB = "tab_delimited";
    /** Enumerated type constant for format, "xml". */
    public static final String FORMAT_XML = "xml";
    /** Enumerated type constant for format: "discourse_db". */
    public static final String FORMAT_DISCOURSE = "discourse_db";

    static {
        FORMAT_ENUM.add(FORMAT_TAB);
        FORMAT_ENUM.add(FORMAT_XML);
        FORMAT_ENUM.add(FORMAT_DISCOURSE);
    }

    //----- ENUM : status -----

    /** Enumerated type list of valid values. */
    static final List<String> STATUS_ENUM = new ArrayList<String>();
    /** Enumerated type constant. */
    public static final String STATUS_QUEUED = "queued";
    /** Enumerated type constant. */
    public static final String STATUS_PASSED = "passed";
    /** Enumerated type constant. */
    public static final String STATUS_ERRORS = "errors";
    /** Enumerated type constant. */
    public static final String STATUS_ISSUES = "issues";
    /** Enumerated type constant. */
    public static final String STATUS_LOADED = "loaded";
    /** Enumerated type constant. */
    public static final String STATUS_CANCELED = "canceled";
    /** Enumerated type constant. */
    public static final String STATUS_NO_DATA  = "no_data";
    /** Enumerated type constant, state before queued. */
    public static final String STATUS_PENDING  = "pending";
    /** Enumerated type constant, state while running FFI. */
    public static final String STATUS_LOADING  = "loading";
    /** Enumerated type constant, state while generating KC models. */
    public static final String STATUS_GENING  = "generating";
    /** Enumerated type constant, state while aggregating. */
    public static final String STATUS_AGGING  = "aggregating";

    static {
        STATUS_ENUM.add(STATUS_PENDING);
        STATUS_ENUM.add(STATUS_QUEUED);
        STATUS_ENUM.add(STATUS_PASSED);
        STATUS_ENUM.add(STATUS_ERRORS);
        STATUS_ENUM.add(STATUS_ISSUES);
        STATUS_ENUM.add(STATUS_LOADED);
        STATUS_ENUM.add(STATUS_CANCELED);
        STATUS_ENUM.add(STATUS_NO_DATA);
        STATUS_ENUM.add(STATUS_LOADING);
        STATUS_ENUM.add(STATUS_GENING);
        STATUS_ENUM.add(STATUS_AGGING);
    }

    /** Display text for enumerated type. */
    public static final String TXT_QUEUED = "Queued for verification";
    /** Display text for enumerated type. */
    public static final String TXT_PASSED = "Passed verification";
    /** Display text for enumerated type. */
    public static final String TXT_ERRORS = "Errors and issues found";
    /** Display text for enumerated type. */
    public static final String TXT_ISSUES = "Potential issues found";
    /** Display text for enumerated type. */
    public static final String TXT_LOADED = "Dataset loaded";
    /** Display text for enumerated type. */
    public static final String TXT_CANCELED = "Import canceled";
    /** Display text for enumerated type. */
    public static final String TXT_LOADING =
            "Importing...<br>Loading your data (process 1 of 3)";
    /** Display text for enumerated type. */
    public static final String TXT_GENING =
            "Importing...<br>Generating baseline KC models (process 2 of 3)";
    /** Display text for enumerated type. */
    public static final String TXT_AGGING =
            "Importing...<br>Preparing your data (process 3 of 3)";

    /** Map from enumerated type to display text. */
    public static final Map<String, String> STATUS_TXT_MAP = new LinkedHashMap<String, String>();
    static {
        STATUS_TXT_MAP.put(STATUS_LOADING, TXT_LOADING);
        STATUS_TXT_MAP.put(STATUS_GENING, TXT_GENING);
        STATUS_TXT_MAP.put(STATUS_AGGING, TXT_AGGING);
        STATUS_TXT_MAP.put(STATUS_QUEUED, TXT_QUEUED);
        STATUS_TXT_MAP.put(STATUS_PASSED, TXT_PASSED);
        STATUS_TXT_MAP.put(STATUS_ERRORS, TXT_ERRORS);
        STATUS_TXT_MAP.put(STATUS_ISSUES, TXT_ISSUES);
        STATUS_TXT_MAP.put(STATUS_LOADED, TXT_LOADED);
        STATUS_TXT_MAP.put(STATUS_CANCELED, TXT_CANCELED);
    }
    /** Map from enumerated type to display text for edit dialog for tab-delimited files. */
    public static final Map<String, String> STATUS_TXT_MAP_TAB =
            new LinkedHashMap<String, String>();
    static {
        STATUS_TXT_MAP_TAB.put(STATUS_CANCELED, TXT_CANCELED);
    }

    /** Map from enumerated type to display text for edit dialog for xml files. */
    public static final Map<String, String> STATUS_TXT_MAP_XML =
            new LinkedHashMap<String, String>();
    static {
        STATUS_TXT_MAP_XML.put(STATUS_QUEUED, TXT_QUEUED);
        STATUS_TXT_MAP_XML.put(STATUS_PASSED, TXT_PASSED);
        STATUS_TXT_MAP_XML.put(STATUS_ERRORS, TXT_ERRORS);
        STATUS_TXT_MAP_XML.put(STATUS_ISSUES, TXT_ISSUES);
        STATUS_TXT_MAP_XML.put(STATUS_LOADED, TXT_LOADED);
        STATUS_TXT_MAP_XML.put(STATUS_CANCELED, TXT_CANCELED);
    }

    /** Display text for enumerated type. */
    public static final String TXT_DISCOURSE_LOADED = "Discourse loaded";

    /** Map from enumerated type to display text for edit dialog for discourse_db files. */
    /** Note: most of the same status options as XML files. */
    public static final Map<String, String> STATUS_TXT_MAP_DISCOURSE =
        new LinkedHashMap<String, String>();
    static {
        STATUS_TXT_MAP_DISCOURSE.put(STATUS_QUEUED, TXT_QUEUED);
        STATUS_TXT_MAP_DISCOURSE.put(STATUS_PASSED, TXT_PASSED);
        STATUS_TXT_MAP_DISCOURSE.put(STATUS_ERRORS, TXT_ERRORS);
        STATUS_TXT_MAP_DISCOURSE.put(STATUS_ISSUES, TXT_ISSUES);
        STATUS_TXT_MAP_DISCOURSE.put(STATUS_LOADED, TXT_DISCOURSE_LOADED);
        STATUS_TXT_MAP_DISCOURSE.put(STATUS_CANCELED, TXT_CANCELED);
    }

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ImportQueueItem() {
    }

    /**
     *  Constructor with id.
     *  @param importQueueId Database generated unique Id for this object.
     */
    public ImportQueueItem(Integer importQueueId) {
        this.id = importQueueId;
    }

    //----- GETTERS and SETTERS -----

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
     * Gets queueOrder.
     * @return the queueOrder
     */
    public Integer getQueueOrder() {
        return queueOrder;
    }

    /**
     * Sets the queueOrder.
     * @param queueOrder the queueOrder to set
     */
    public void setQueueOrder(Integer queueOrder) {
        this.queueOrder = queueOrder;
    }

    /**
     * Gets project.
     * @return the project
     */
    public ProjectItem getProject() {
        return project;
    }

    /**
     * Sets the project.
     * @param project the project to set
     */
    public void setProject(ProjectItem project) {
        this.project = project;
    }

    /**
     * Gets dataset.
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Sets the dataset.
     * @param dataset the dataset to set
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
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
     * Gets file.
     * @return the file
     */
    public FileItem getFile() {
        return file;
    }

    /**
     * Sets the file.
     * @param file the file to set
     */
    public void setFile(FileItem file) {
        this.file = file;
    }

    /**
     * Gets uploadedBy.
     * @return the uploadedBy
     */
    public UserItem getUploadedBy() {
        return uploadedBy;
    }

    /**
     * Sets the uploadedBy.
     * @param uploadedBy the uploadedBy to set
     */
    public void setUploadedBy(UserItem uploadedBy) {
        this.uploadedBy = uploadedBy;
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
        if (format == null) {
            this.format = null;
        } else if (FORMAT_ENUM.contains(format)) {
            this.format = format;
        } else {
            throw new IllegalArgumentException("Invalid format value: " + format);
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
        if (STATUS_ENUM.contains(status)) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    /**
     * Gets estImportDate.
     * @return the estImportDate
     */
    public Date getEstImportDate() {
        return estImportDate;
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
     * Gets lastUpdatedTime.
     * @return the lastUpdatedTime
     */
    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
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
     * Gets importStatusId.
     * @return the importStatusId
     */
    public Integer getImportStatusId() {
        return importStatusId;
    }

    /**
     * Sets the importStatusId.
     * @param id the importStatusId to set
     */
    public void setImportStatusId(Integer id) {
        this.importStatusId = id;
    }

    /**
     * Get the domain name.
     * @return the domain name
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Set the domain name.
     * @param domainName the name
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Get the learnlab name.
     * @return the learnlab name
     */
    public String getLearnlabName() {
        return learnlabName;
    }

    /**
     * Set the learnlab name.
     * @param learnlabName the name
     */
    public void setLearnlabName(String learnlabName) {
        this.learnlabName = learnlabName;
    }

    /**
     * Get studyFlag.
     * @return string for enumerated type
     */
    public String getStudyFlag() {
        return this.studyFlag;
    }

    /**
     * Set studyFlag.
     * @param studyFlag Flag indicating whether this dataset was a study.
     */
    public void setStudyFlag(String studyFlag) {
        this.studyFlag = studyFlag;
    }

    /** Whether or not to include user KCMs during import.
     * @return the includeUserKCMs
     */
    public Boolean getIncludeUserKCMs() {
        return includeUserKCMs;
    }

    /** Set whether or not to include user KCMs during import.
     * @param includeUserKCMs the includeUserKCMs to set
     */
    public void setIncludeUserKCMs(Boolean includeUserKCMs) {
        this.includeUserKCMs = includeUserKCMs;
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

    /** Gets the additional dataset notes.
     * @return the additional dataset notes
     */
    public String getAdtlDatasetNotes() {
        return adtlDatasetNotes;
    }

    /** Sets the additional dataset notes
     * @param the additional dataset notes
     */
    public void setAdtlDatasetNotes(String adtlDatasetNotes) {
        this.adtlDatasetNotes = adtlDatasetNotes;
    }

    /**
     * Gets fromExistingDatasetFlag.
     * @return the fromExistingDatasetFlag
     */
    public Boolean getFromExistingDatasetFlag() {
        return fromExistingDatasetFlag;
    }

    /**
     * Sets the fromExistingDatasetFlag.
     * @param fromExistingDatasetFlag the fromExistingDatasetFlag to set
     */
    public void setFromExistingDatasetFlag(Boolean fromExistingDatasetFlag) {
        this.fromExistingDatasetFlag = fromExistingDatasetFlag;
    }

    //----- STANDARD ITEM METHODS :: toString, equals, hashCode, compareTo -----

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
         buffer.append(objectToStringFK("Project", getProject()));
         buffer.append(objectToStringFK("Dataset", getDataset()));
         buffer.append(objectToStringFK("File", getFile()));
         buffer.append(objectToStringFK("UploadedBy", getUploadedBy()));
         buffer.append(objectToString("queueOrder", getQueueOrder()));
         buffer.append(objectToString("DatasetName", getDatasetName()));
         buffer.append(objectToString("Description", getDescription()));
         buffer.append(objectToString("UploadedTime", getUploadedTime()));
         buffer.append(objectToString("Format", getFormat()));
         buffer.append(objectToString("Status", getStatus()));
         buffer.append(objectToString("EstImportDate", getEstImportDate()));
         buffer.append(objectToString("NumErrors", getNumErrors()));
         buffer.append(objectToString("NumIssues", getNumIssues()));
         buffer.append(objectToString("VerificationResults", getVerificationResults()));
         buffer.append(objectToString("NumTransactions", getNumTransactions()));
         buffer.append(objectToString("LastUpdatedTime", getLastUpdatedTime()));
         buffer.append(objectToString("AnonFlag", getAnonFlag()));
         buffer.append(objectToString("DisplayFlag", getDisplayFlag()));
         buffer.append(objectToString("ImportStatusId", getImportStatusId()));
         buffer.append(objectToString("DomainName", getDomainName()));
         buffer.append(objectToString("LearnlabName", getLearnlabName()));
         buffer.append(objectToString("studyFlag", getStudyFlag()));
         buffer.append(objectToString("includeUserKCMs", getIncludeUserKCMs()));
         buffer.append(objectToString("srcDatasetId", getSrcDatasetId()));
         buffer.append(objectToString("srcDatasetName", getSrcDatasetName()));
         buffer.append(objectToString("srcSampleId", getSrcSampleId()));
         buffer.append(objectToString("srcSampleName", getSrcSampleName()));
         buffer.append(objectToString("adtlDatasetNotes", getAdtlDatasetNotes()));
         buffer.append(objectToString("fromExistingDatasetFlag", getFromExistingDatasetFlag()));
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
        if (obj instanceof ImportQueueItem) {
            ImportQueueItem otherItem = (ImportQueueItem)obj;

            if (!objectEqualsFK(this.getProject(), otherItem.getProject())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEqualsFK(this.getFile(), otherItem.getFile())) {
                return false;
            }
            if (!objectEqualsFK(this.getUploadedBy(), otherItem.getUploadedBy())) {
                return false;
            }
            if (!objectEquals(this.getQueueOrder(), otherItem.getQueueOrder())) {
                return false;
            }
            if (!objectEquals(this.getDatasetName(), otherItem.getDatasetName())) {
                return false;
            }
            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!objectEquals(this.getUploadedTime(), otherItem.getUploadedTime())) {
                return false;
            }
            if (!objectEquals(this.getFormat(), otherItem.getFormat())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!objectEquals(this.getEstImportDate(), otherItem.getEstImportDate())) {
                return false;
            }
            if (!objectEquals(this.getNumErrors(), otherItem.getNumErrors())) {
                return false;
            }
            if (!objectEquals(this.getNumIssues(), otherItem.getNumIssues())) {
                return false;
            }
            if (!objectEquals(this.getVerificationResults(), otherItem.getVerificationResults())) {
                return false;
            }
            if (!objectEquals(this.getNumTransactions(), otherItem.getNumTransactions())) {
                return false;
            }
            if (!objectEquals(this.getLastUpdatedTime(), otherItem.getLastUpdatedTime())) {
                return false;
            }
            if (!objectEquals(this.getAnonFlag(), otherItem.getAnonFlag())) {
                return false;
            }
            if (!objectEquals(this.getDisplayFlag(), otherItem.getDisplayFlag())) {
                return false;
            }
            if (!objectEquals(this.getImportStatusId(), otherItem.getImportStatusId())) {
                return false;
            }
            if (!objectEquals(this.getDomainName(), otherItem.getDomainName())) {
                return false;
            }
            if (!objectEquals(this.getLearnlabName(), otherItem.getLearnlabName())) {
                return false;
            }
            if (!objectEquals(this.getStudyFlag(), otherItem.getStudyFlag())) {
                return false;
            }
            if (!objectEquals(this.getIncludeUserKCMs(), otherItem.getIncludeUserKCMs())) {
                return false;
            }
            if (!objectEquals(this.getSrcDatasetId(), otherItem.getSrcDatasetId())) {
                return false;
            }
            if (!objectEquals(this.getSrcDatasetName(), otherItem.getSrcDatasetName())) {
                return false;
            }
            if (!objectEquals(this.getSrcSampleId(), otherItem.getSrcSampleId())) {
                return false;
            }
            if (!objectEquals(this.getSrcSampleName(), otherItem.getSrcSampleName())) {
                return false;
            }
            if (!objectEquals(this.getAdtlDatasetNotes(), otherItem.getAdtlDatasetNotes())) {
                return false;
            }
            if (!objectEquals(this.getFromExistingDatasetFlag(),
                              otherItem.getFromExistingDatasetFlag())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getFile());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUploadedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getQueueOrder());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDatasetName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUploadedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFormat());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEstImportDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumErrors());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumIssues());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVerificationResults());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumTransactions());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastUpdatedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAnonFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDisplayFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getImportStatusId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDomainName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLearnlabName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStudyFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getIncludeUserKCMs());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSrcDatasetId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSrcDatasetName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSrcSampleId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSrcSampleName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAdtlDatasetNotes());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFromExistingDatasetFlag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>queueOrder</li>
     * <li>dataset_name</li>
     * <li>project</li>
     * <li>dataset</li>
     * <li>uploaded_by</li>
     * <li>uploaded_time</li>
     * <li>file</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ImportQueueItem otherItem = (ImportQueueItem)obj;
        int value = 0;

        value = objectCompareTo(this.getQueueOrder(), otherItem.getQueueOrder());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatasetName(), otherItem.getDatasetName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getProject(), otherItem.getProject());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUploadedBy(), otherItem.getUploadedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUploadedTime(), otherItem.getUploadedTime());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getFile(), otherItem.getFile());
        if (value != 0) { return value; }

        //---- the rest of the fields -----

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFormat(), otherItem.getFormat());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEstImportDate(), otherItem.getEstImportDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumErrors(), otherItem.getNumErrors());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumIssues(), otherItem.getNumIssues());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getVerificationResults(), otherItem.getVerificationResults());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumTransactions(), otherItem.getNumTransactions());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastUpdatedTime(), otherItem.getLastUpdatedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAnonFlag(), otherItem.getAnonFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDisplayFlag(), otherItem.getDisplayFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getImportStatusId(), otherItem.getImportStatusId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDomainName(), otherItem.getDomainName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLearnlabName(), otherItem.getLearnlabName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStudyFlag(), otherItem.getStudyFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getIncludeUserKCMs(), otherItem.getIncludeUserKCMs());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSrcDatasetId(), otherItem.getSrcDatasetId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSrcDatasetName(), otherItem.getSrcDatasetName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSrcSampleId(), otherItem.getSrcSampleId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSrcSampleName(), otherItem.getSrcSampleName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAdtlDatasetNotes(), otherItem.getAdtlDatasetNotes());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFromExistingDatasetFlag(),
                                otherItem.getFromExistingDatasetFlag());
        if (value != 0) { return value; }

        return value;
    }
}