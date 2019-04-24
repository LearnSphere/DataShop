/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.project;

import java.text.DecimalFormat;

/**
 * This is a POJO for the datasets within a project. It is similar to a subset of DatasetItem
 * but with differences to display the data.
 *
 * @author Cindy Tipper
 * @version $Revision: 12678 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-13 15:55:53 -0400 (Tue, 13 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectDatasetDto {

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Dataset name. */
    private String name;
    /** Domain name. */
    private String domainName;
    /** Learnlab name. */
    private String learnlabName;
    /** Date range. */
    private String dateRange;
    /** Status. */
    private String status;
    /** Number of transactions. */
    private Long numTransactions;
    /** Number of papers. */
    private Long numPapers;
    /** Appears anonymous flag. */
    private String appearsAnonymous;
    /** IRB Uploaded flag. */
    private String irbUploaded;
    /** Has Study Data flag. */
    private String hasStudyData;
    /** If null then this dataset is not in the IQ table. */
    private Integer importQueueId = null;
    /** If dataset was uploaded by user. */
    private String uploaderName = null;
    /** If true, then this dataset has been accessed. */
    private Boolean accessedFlag = null;
    /** Data Last Modified Date. */
    private String dataLastModifiedDate;
    /** Data Last Modified Date including Time. */
    private String dataLastModifiedTime;
    /** Number of KC Models for this dataset. */
    private Long numSkillModels;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param id database generated unique id for the dataset item
     * @param name the name of the dataset
     * @param domainName the name of the Domain
     * @param learnlabName the name of the Learnlab
     * @param dateRange the range of dates for the dataset
     * @param status the status of the dataset
     * @param numTransactions number of transactions for the dataset
     * @param appearsAnonymous flag indicating whether or not data seems to be anonymous
     */
    public ProjectDatasetDto(Integer id, String name, String domainName,
            String learnlabName, String dateRange, String status,
            Long numTransactions, String appearsAnonymous) {
        this.id = id;
        setName(name);
        setDomainName(domainName);
        setLearnlabName(learnlabName);
        setDateRange(dateRange);
        setStatus(status);
        setNumTransactions(numTransactions);
        setAppearsAnonymous(appearsAnonymous);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the id.
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    /**
     * Set the id.
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * Get the name.
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * Set the name.
     * @param datasetName the dataset name
     */
    public void setName(String datasetName) {
        this.name = datasetName;
    }
    /**
     * Get the domain name.
     * @return the domainName
     */
    public String getDomainName() {
        return domainName;
    }
    /**
     * Set the domain name.
     * @param domainName the domain name
     */
    public void setDomainName(String domainName) {
        this.domainName = (domainName == null) ? "" : domainName;
    }
    /**
     * Get the domain display string.
     * @return the display-ready domain information
     */
    public String getDomainDisplayString() {
        String result = "";
        if (!domainName.equals("")) {
            result = domainName;
            if (!learnlabName.equals("") && !domainName.equals("Other")) {
                result = domainName + "/" + learnlabName;
            }
        }
        return result;
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
     * @param learnlabName the learnlab name
     */
    public void setLearnlabName(String learnlabName) {
        this.learnlabName = (learnlabName == null) ? "" : learnlabName;
    }
    /**
     * Get the date range.
     * @return the dateRange
     */
    public String getDateRange() {
        return dateRange;
    }
    /**
     * Set the date range.
     * @param dateRange the date range
     */
    public void setDateRange(String dateRange) {
        this.dateRange = (dateRange == null) ? "" : dateRange;
    }
    /**
     * Get the status.
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    /**
     * Set the status.
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = (status == null) ? "" : status;
    }
    /**
     * Get the number of transactions.
     * @return the numTransactions
     */
    public Long getNumTransactions() {
        return numTransactions;
    }
    /**
     * Set the number of transactions.
     * @param numTransactions the number of transactions
     */
    public void setNumTransactions(Long numTransactions) {
        this.numTransactions = numTransactions;
    }
    /**
     * Get the number of transactions, nicely formatted.
     * @return the numTransactions
     */
    public String getNumTransactionsFormatted() {
        return new DecimalFormat("##,###,##0").format(getNumTransactions());
    }

    /**
     * Gets the numPapers.
     * @return the numPapers
     */
    public Long getNumPapers() {
        return numPapers;
    }

    /**
     * Sets the numPapers.
     * @param numPapers the numPapers to set
     */
    public void setNumPapers(Long numPapers) {
        this.numPapers = numPapers;
    }

    /**
     * Get the appears anonymous flag.
     * @return appearsAnonymous
     */
    public String getAppearsAnonymous() {
        return appearsAnonymous;
    }
    /**
     * Set the appears anonymous flag.
     * @param appearsAnonymous the appearsAnonymous flag.
     */
    public void setAppearsAnonymous(String appearsAnonymous) {
        this.appearsAnonymous = appearsAnonymous;
    }

    /**
     * Gets the irbUploaded.
     * @return the irbUploaded
     */
    public String getIrbUploaded() {
        return irbUploaded;
    }

    /**
     * Sets the irbUploaded.
     * @param irbUploaded the irbUploaded to set
     */
    public void setIrbUploaded(String irbUploaded) {
        this.irbUploaded = irbUploaded;
    }

    /**
     * Gets the hasStudyData.
     * @return the hasStudyData
     */
    public String getHasStudyData() {
        return hasStudyData;
    }

    /**
     * Sets the hasStudyData.
     * @param hasStudyData the hasStudyData to set
     */
    public void setHasStudyData(String hasStudyData) {
        this.hasStudyData = hasStudyData;
    }

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
     * Gets uploaderName.
     * @return the uploaderName
     */
    public String getUploaderName() {
        return uploaderName;
    }

    /**
     * Sets the uploaderName.
     * @param uploaderName the uploaderName to set
     */
    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
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
     * Gets the dataLastModifiedDate.
     * @return the dataLastModifiedDate
     */
    public String getDataLastModifiedDate() {
        return dataLastModifiedDate;
    }

    /**
     * Sets the dataLastModifiedDate.
     * @param dataLastModifiedDate the dataLastModifiedDate to set
     */
    public void setDataLastModifiedDate(String dataLastModifiedDate) {
        this.dataLastModifiedDate = dataLastModifiedDate;
    }

    /**
     * Gets the dataLastModifiedTime.
     * @return the dataLastModifiedTime
     */
    public String getDataLastModifiedTime() {
        return dataLastModifiedTime;
    }

    /**
     * Sets the dataLastModifiedTime.
     * @param dataLastModifiedTime the dataLastModifiedTime to set
     */
    public void setDataLastModifiedTime(String dataLastModifiedTime) {
        this.dataLastModifiedTime = dataLastModifiedTime;
    }

    /**
     * Get the number of KC Models.
     * @return Long numSkillModels
     */
    public Long getNumSkillModels() { return numSkillModels; }

    /**
     * Set the number of KC Models.
     * @param numSkillModels the number
     */
    public void setNumSkillModels(Long numSkillModels) { this.numSkillModels = numSkillModels; }

    /**
     * Get the number of skill models, nicely formatted.
     * @return the numSkillModels
     */
    public String getNumSkillModelsFormatted() {
        return new DecimalFormat("##,###,##0").format(getNumSkillModels());
    }
}
