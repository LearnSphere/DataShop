/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.pslc.datashop.dto.importqueue.VerificationResults;
import edu.cmu.pslc.datashop.item.ImportQueueItem;

/**
 * DTO to hold the data from the fields on the Upload Dataset page.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13540 $ <BR>
 *          Last modified by: $Author: ctipper $ <BR>
 *          Last modified on: $Date: 2016-09-22 14:16:36 -0400 (Thu, 22 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UploadDatasetDto {

    // ----- ATTRIBUTES -----

    /** Class attribute. */
    private String projectSelection;
    /** Class attribute. */
    private String newProjectName = "";
    /** Class attribute. */
    private Boolean newProjectNameErrorFlag = false;
    /** Class attribute. */
    private String dataCollectionType;
    /** Class attribute. */
    private Integer existingProjectId;
    /** Class attribute. */
    private Boolean existingProjectIdErrorFlag = false;
    /** Class attribute. */
    private String datasetName = "";
    /** Class attribute. */
    private Boolean datasetNameErrorFlag = false;
    /** Class attribute. */
    private Boolean datasetIdErrorFlag = false;
    /** Class attribute. */
    private String datasetDesccription = "";
    /** Class attribute. */
    private Boolean loadDataNowFlag = true;
    /** Class attribute. */
    private String dataFile;
    /** Class attribute. */
    private Integer fileItemId;
    /** Class attribute. */
    private Boolean dataFileErrorFlag = false;
    /** Class attribute. */
    private String format = ImportQueueItem.FORMAT_TAB;
    /** Class attribute. */
    private Boolean anonymizedFlag = false;
    /** Class attribute. */
    private String errorMessage;
    /** Class attribute. */
    private VerificationResults results;
    /** Class attribute. */
    private Integer importQueueItemId;
    /** Class attribute. */
    private String domainName;
    /** Class attribute. */
    private String learnlabName;
    /** Class attribute. */
    private String hasStudyData;
    /** Whether or not the data is missing from the session. */
    private boolean sessionDataErrorFlag;
    /** Whether or not the upload is from existing dataset. */
    private Boolean fromExistingFlag = false;

    // ----- ENUM : project group -----

    /** Enumerated type list of valid values. */
    private static final List<String> PROJ_GROUP_ENUM = new ArrayList<String>();
    /** Enumerated type constant. */
    public static final String PROJ_NEW = "new";
    /** Enumerated type constant. */
    public static final String PROJ_EXIST = "exist";
    /** Enumerated type constant. */
    public static final String PROJ_LATER = "later";
    /** Enumerated type constant. */
    public static final String PROJ_CURRENT = "current";

    static {
        PROJ_GROUP_ENUM.add(PROJ_NEW);
        PROJ_GROUP_ENUM.add(PROJ_EXIST);
        PROJ_GROUP_ENUM.add(PROJ_LATER);
        PROJ_GROUP_ENUM.add(PROJ_CURRENT);
    }

    // ----- CONSTRUCTOR -----

    /** Default constructor. */
    public UploadDatasetDto() {
    }

    // ----- GETTERS and SETTERS -----

    /**
     * Gets projectSelection.
     * @return the projectSelection
     */
    public String getProjectSelection() {
        return projectSelection;
    }

    /**
     * Sets the projectSelection.
     * @param projectSelection
     *            the projectSelection to set
     */
    public void setProjectSelection(String projectSelection) {
        this.projectSelection = projectSelection;
        if (PROJ_GROUP_ENUM.contains(projectSelection)) {
            this.projectSelection = projectSelection;
        } else {
            throw new IllegalArgumentException(
                    "Invalid projectSelection value: " + projectSelection);
        }
    }

    /**
     * Gets newProjectName.
     * @return the newProjectName
     */
    public String getNewProjectName() {
        return newProjectName;
    }

    /**
     * Sets the newProjectName.
     * @param newProjectName
     *            the newProjectName to set
     */
    public void setNewProjectName(String newProjectName) {
        if (newProjectName == null) {
            this.newProjectName = "";
        } else {
            this.newProjectName = newProjectName;
        }
    }

    /**
     * Gets newProjectNameErrorFlag.
     * @return the newProjectNameErrorFlag
     */
    public Boolean getNewProjectNameErrorFlag() {
        return newProjectNameErrorFlag;
    }

    /**
     * Sets the newProjectNameErrorFlag.
     * @param newProjectNameErrorFlag the newProjectNameErrorFlag to set
     */
    public void setNewProjectNameErrorFlag(Boolean newProjectNameErrorFlag) {
        this.newProjectNameErrorFlag = newProjectNameErrorFlag;
    }

    /**
     * Gets dataCollectionType.
     * @return the dataCollectionType
     */
    public String getDataCollectionType() {
        return dataCollectionType;
    }

    /**
     * Sets the dataCollectionType.
     * @param dataCollectionType the dataCollectionType to set
     */
    public void setDataCollectionType(String dataCollectionType) {
        this.dataCollectionType = dataCollectionType;
    }

    /**
     * Gets existingProjectId.
     * @return the existingProjectId
     */
    public Integer getExistingProjectId() {
        return existingProjectId;
    }

    /**
     * Sets the existingProjectId.
     * @param existingProjectId the existingProjectId to set
     */
    public void setExistingProjectId(Integer existingProjectId) {
        this.existingProjectId = existingProjectId;
    }

    /**
     * Gets existingProjectIdErrorFlag.
     * @return the existingProjectIdErrorFlag
     */
    public Boolean getExistingProjectIdErrorFlag() {
        return existingProjectIdErrorFlag;
    }

    /**
     * Sets the existingProjectIdErrorFlag.
     * @param existingProjectIdErrorFlag the existingProjectIdErrorFlag to set
     */
    public void setExistingProjectIdErrorFlag(Boolean existingProjectIdErrorFlag) {
        this.existingProjectIdErrorFlag = existingProjectIdErrorFlag;
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
     * Gets datasetNameErrorFlag.
     * @return the datasetNameErrorFlag
     */
    public Boolean getDatasetNameErrorFlag() {
        return datasetNameErrorFlag;
    }

    /**
     * Sets the datasetNameErrorFlag.
     * @param datasetNameErrorFlag the datasetNameErrorFlag to set
     */
    public void setDatasetNameErrorFlag(Boolean datasetNameErrorFlag) {
        this.datasetNameErrorFlag = datasetNameErrorFlag;
    }

    /**
     * Gets datasetIdErrorFlag.
     * @return the datasetIdErrorFlag
     */
    public Boolean getDatasetIdErrorFlag() {
        return datasetIdErrorFlag;
    }

    /**
     * Sets the datasetIdErrorFlag.
     * @param datasetIdErrorFlag the datasetIdErrorFlag to set
     */
    public void setDatasetIdErrorFlag(Boolean datasetIdErrorFlag) {
        this.datasetIdErrorFlag = datasetIdErrorFlag;
    }

    /**
     * Gets datasetDesccription.
     * @return the datasetDesccription
     */
    public String getDatasetDesccription() {
        return datasetDesccription;
    }

    /**
     * Sets the datasetDesccription.
     * @param datasetDesccription the datasetDesccription to set
     */
    public void setDatasetDesccription(String datasetDesccription) {
        this.datasetDesccription = datasetDesccription;
    }

    /**
     * Gets loadDataNowFlag.
     * @return the loadDataNowFlag
     */
    public Boolean getLoadDataNowFlag() {
        return loadDataNowFlag;
    }

    /**
     * Sets the loadDataNowFlag.
     * @param loadDataNowFlag the loadDataNowFlag to set
     */
    public void setLoadDataNowFlag(Boolean loadDataNowFlag) {
        this.loadDataNowFlag = loadDataNowFlag;
    }

    /**
     * Gets dataFile.
     * @return the dataFile
     */
    public String getDataFile() {
        return dataFile;
    }

    /**
     * Sets the dataFile.
     * @param dataFile the dataFile to set
     */
    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Gets fileItemId.
     * @return the fileItemId
     */
    public Integer getFileItemId() {
        return fileItemId;
    }

    /**
     * Sets the fileItemId.
     * @param fileItemId the fileItemId to set
     */
    public void setFileItemId(Integer fileItemId) {
        this.fileItemId = fileItemId;
    }

    /**
     * Gets dataFileErrorFlag.
     * @return the dataFileErrorFlag
     */
    public Boolean getDataFileErrorFlag() {
        return dataFileErrorFlag;
    }

    /**
     * Sets the dataFileErrorFlag.
     * @param dataFileErrorFlag the dataFileErrorFlag to set
     */
    public void setDataFileErrorFlag(Boolean dataFileErrorFlag) {
        this.dataFileErrorFlag = dataFileErrorFlag;
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

    /**
     * Gets anonymizedFlag.
     * @return the anonymizedFlag
     */
    public Boolean getAnonymizedFlag() {
        return anonymizedFlag;
    }

    /**
     * Sets the anonymizedFlag.
     * @param anonymizedFlag the anonymizedFlag to set
     */
    public void setAnonymizedFlag(Boolean anonymizedFlag) {
        this.anonymizedFlag = anonymizedFlag;
    }

    /**
     * Gets errorMessage.
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the errorMessage.
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets results.
     * @return results
     */
    public VerificationResults getResults() {
        return this.results;
    }

    /**
     * Sets results.
     * @param results the verification results
     */
    public void setResults(VerificationResults results) {
        this.results = results;
    }

    /**
     * Gets importQueueItemId.
     * @return the importQueueItemId
     */
    public Integer getImportQueueItemId() {
        return importQueueItemId;
    }

    /**
     * Sets the importQueueItemId.
     * @param importQueueItemId the importQueueItemId to set
     */
    public void setImportQueueItemId(Integer importQueueItemId) {
        this.importQueueItemId = importQueueItemId;
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
     * Get the hasStudyData flag.
     * @return the hasStudyData flag
     */
    public String getHasStudyData() {
        return hasStudyData;
    }

    /**
     * Set the hasStudyData flag.
     * @param hasStudyData the flag
     */
    public void setHasStudyData(String hasStudyData) {
        this.hasStudyData = hasStudyData;
    }

    // ----- OTHER methods -----

    /**
     * Returns true if any fields have an error.
     * @return true if any errors, false otherwise
     */
    public boolean hasErrors() {
        boolean flag = false;
        if (getErrorMessage() != null && getErrorMessage().length() > 0) {
            return true;
        }
        if (getDatasetNameErrorFlag()) {
            return true;
        }
        if (getDatasetIdErrorFlag()) {
            return true;
        }
        if (getProjectSelection().equals(UploadDatasetDto.PROJ_NEW)) {
            if (getNewProjectNameErrorFlag()) {
                flag = true;
            }
        } else if (getProjectSelection().equals(UploadDatasetDto.PROJ_EXIST)) {
            if (getExistingProjectIdErrorFlag()) {
                flag = true;
            }
        }
        if (getLoadDataNowFlag() && getDataFileErrorFlag()) {
            flag = true;
        }
        return flag;
    }

    /**
     * Returns a string with all the errors in DTO.
     * @return simple string of error messages for use in log4j messages
     */
    public String printErrors() {
        String errorMsgs = "";
        if (getErrorMessage() != null && getErrorMessage().length() > 0) {
            errorMsgs += getErrorMessage() + " ";
        }
        if (getDatasetNameErrorFlag()) {
            errorMsgs += "Dataset name error. ";
        }
        if (getDatasetIdErrorFlag()) {
            errorMsgs += "Dataset id error. ";
        }
        if (getProjectSelection().equals(UploadDatasetDto.PROJ_NEW)) {
            if (getNewProjectNameErrorFlag()) {
                errorMsgs += "New project name error. ";
            }
        } else if (getProjectSelection().equals(UploadDatasetDto.PROJ_EXIST)) {
            if (getExistingProjectIdErrorFlag()) {
                errorMsgs += "Existing project id error. ";
            }
        }
        if (getLoadDataNowFlag() && getDataFileErrorFlag()) {
            errorMsgs += "Get data file error.";
        }
        return errorMsgs;
    }

    /**
     * Sets the sessionDataErrorFlag flag.
     * @param sessionDataErrorFlag the dtoMissing flag
     */
    public void setSessionDataErrorFlag(boolean sessionDataErrorFlag) {
        this.sessionDataErrorFlag = sessionDataErrorFlag;
    }
    /**
     * Gets the sessionDataErrorFlag flag.
     * @return the sessionDataErrorFlag flag
     */
    public boolean getSessionDataErrorFlag() {
        return sessionDataErrorFlag;
    }

    /**
     * Gets fromExistingFlag.
     * @return the fromExistingFlag
     */
    public Boolean getFromExistingFlag() {
        return fromExistingFlag;
    }

    /**
     * Sets the fromExistingFlag.
     * @param fromExistingFlag the fromExistingFlag to set
     */
    public void setFromExistingFlag(Boolean fromExistingFlag) {
        this.fromExistingFlag = fromExistingFlag;
    }
}
