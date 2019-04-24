/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.HashMap;

import edu.cmu.pslc.datashop.item.DatasetItem;

/**
 * Holds the data needed for the Dataset Info page.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10833 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-24 13:40:24 -0400 (Mon, 24 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInfoReport implements java.io.Serializable {
    /** Dataset Item. */
    private DatasetItem datasetItem;
    /** Project Name. */
    private String projectName = null;
    /** Principal Investigator Name. */
    private String piName = null;
    /** Data Provider Name. */
    private String dpName = null;
    /** Domain Name. */
    private String domainName = null;
    /** Learnlab Name. */
    private String learnlabName = null;
    /** Curriculum Name. */
    private String curriculumName = null;
    /** Acknowledgment. */
    private String acknowledgment = null;
    /** Citation. */
    private String citation = null;
    /** File Name. */
    private String fileName = null;
    /** File Id. */
    private int fileId = 0;
    /** File Size. */
    private long fileSize = 0;
    /** Number of Papers. */
    private Long numberOfPapers;
    /** Number of Students. */
    private long numberOfStudents;
    /** Number of Transactions. */
    private int numberOfTransactions;
    /** Number of Steps. */
    private int numberOfSteps;
    /** Total Number of Steps */
    private int totalNumberOfSteps;
    /** Total Number of Student Hours */
    private Double totalStudentHours;
    /** HashMap of Skill Models and their respective skill counts */
    private HashMap skillModels;

    /** Default constructor. */
    public DatasetInfoReport() {
    }

    /**
     * Returns the dataset item.
     * @return the dataset item.
     */
    public DatasetItem getDatasetItem() {
        return this.datasetItem;
    }

    /**
     * Sets the dataset item.
     * @param item the dataset item
     */
    public void setDatasetItem(DatasetItem item) {
        this.datasetItem = item;
    }

    /**
     * Returns the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return this.projectName;
    }
    /**
     * Sets the project name.
     * @param name the project name
     */
    public void setProjectName(String name) {
        this.projectName = name;
    }

    /**
     * Returns the PI name.
     * @return the PI name.
     */
    public String getPiName() {
        return this.piName;
    }
    /**
     * Sets the PI name.
     * @param name the PI name
     */
    public void setPiName(String name) {
        this.piName = name;
    }

    /**
     * Returns the DP name.
     * @return the DP name.
     */
    public String getDpName() {
        return this.dpName;
    }
    /**
     * Sets the DP name.
     * @param name the DP name
     */
    public void setDpName(String name) {
        this.dpName = name;
    }

    /**
     * Returns the domain name.
     * @return the domain name.
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * Sets the domain name.
     * @param name the domain name
     */
    public void setDomainName(String name) {
        this.domainName = name;
    }

    /** Returns the learnlab name.
     * @return the learnlab name.
     */
    public String getLearnlabName() {
        return this.learnlabName;
    }
    /**
     * Sets the learnlab name.
     * @param name the learnlab name
     */
    public void setLearnlabName(String name) {
        this.learnlabName = name;
    }

    /**
     * Returns the curriculum name.
     * @return the curriculum name.
     */
    public String getCurriculumName() {
        return this.curriculumName;
    }

    /**
     * Sets the curriculum name.
     * @param name the curriculum name
     */
    public void setCurriculumName(String name) {
        this.curriculumName = name;
    }

    /**
     * Returns the acknowledgment.
     * @return the acknowledgment.
     */
    public String getAcknowledgment() {
        return this.acknowledgment;
    }

    /**
     * Sets the acknowledgment.
     * @param acknowledgment the acknowledgment
     */
    public void setAcknowledgment(String acknowledgment) {
        this.acknowledgment = acknowledgment;
    }

    /**
     * Returns the citation.
     * @return the citation.
     */
    public String getCitation() {
        return this.citation;
    }

    /**
     * Sets the citation.
     * @param citation the citation
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * Returns the fileId.
     * @return the fileId.
     */
    public int getFileId() {
        return this.fileId;
    }

    /**
     * Sets the fileId.
     * @param fileId the file id
     */
    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    /**
     * Returns the fileName.
     * @return the fileName.
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Sets the fileName.
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the fileSize.
     * @return the fileSize.
     */
    public long getFileSize() {
        return this.fileSize;
    }

    /**
     * Sets the fileSize.
     * @param fileSize the file name
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Gets the numberOfPapers.
     * @return the numberOfPapers
     */
    public long getNumberOfPapers() {
        return numberOfPapers;
    }

    /**
     * Sets the numberOfPapers.
     * @param numberOfPapers the numberOfPapers to set
     */
    public void setNumberOfPapers(long numberOfPapers) {
        this.numberOfPapers = numberOfPapers;
    }

    /**
     * Returns the number of students in this dataset.
     * @return the number of students
     */
    public long getNumberOfStudents() {
        return this.numberOfStudents;
    }

    /**
     * Sets the number of students in this dataset.
     * @param number of students
     */
    public void setNumberOfStudents(long number) {
        this.numberOfStudents = number;
    }

    /**
     * Returns the number of transactions in this dataset.
     * @return the number of transactions
     */
    public int getNumberOfTransactions() {
        return this.numberOfTransactions;
    }

    /**
     * Sets the number of transactions in this dataset.
     * @param number of transactions
     */
    public void setNumberOfTransactions(int number) {
        this.numberOfTransactions = number;
    }

    /**
     * Get the dates for the dataset as a formatted string.
     * @return the date range string
     */
    public String getDates() {
        return DatasetItem.getDateRangeString(datasetItem);
    }

    /**
     * Get the number of steps in this dataset.
     * @return Returns the numberOfSteps.
     */
    public int getNumberOfSteps() {
        return numberOfSteps;
    }

    /**
     * Sets the number of steps in this dataset.
     * @param numberOfSteps The numberOfSteps to set.
     */
    public void setNumberOfSteps(int numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }

    /**
     * Gets the skill models items for this dataset.
     * @return the skillModels
     */
    public HashMap getSkillModels() {
        return skillModels;
    }

    /**
     * Sets the skill model items for this dataset.
     * @param skillModels the skillModels to set
     */
    public void setSkillModels(HashMap skillModels) {
        this.skillModels = skillModels;
    }

    /**
     * Gets the total number of steps.
     * @return the totalNumberOfSteps
     */
    public int getTotalNumberOfSteps() {
        return totalNumberOfSteps;
    }

    /**
     * Sets the total number of steps.
     * @param totalNumberOfSteps the totalNumberOfSteps to set
     */
    public void setTotalNumberOfSteps(int totalNumberOfSteps) {
        this.totalNumberOfSteps = totalNumberOfSteps;
    }

    /**
     * Returns the number of total student hours in this dataset.
     * @return the number of total student hours
     */
    public Double getTotalStudentHours() {
        return this.totalStudentHours;
    }

    /**
     * Sets the number of total student hours  in this dataset.
     * @param totalStudentHours number of total student hours
     */
    public void setTotalStudentHours(Double totalStudentHours) {
        this.totalStudentHours = totalStudentHours;
    }

}
