/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * Used to transfer dataset data as XML, JSON, etc.
 *
 * @author Jim Rankin
 * @version $Revision: 13099 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-14 12:44:17 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "dataset",
                properties = { "id", "name", "project", "domain", "learnlab", "pi", "piName",
                               "dataProvider", "dataProviderName", "startDate", "endDate",
                               "status", "access", "public", "released", "curriculum", "tutor",
                               "description", "hasStudyData", "hypothesis", "school",
                               "additionalNotes", "acknowledgment", "citation",
                               "numberOfStudents", "numberOfStudentHours",
                               "numberOfUniqueSteps", "numberOfSteps",
                               "numberOfTransactions", "numberOfSamples",
                               "numberOfAccessibleSamples", "numberOfKcModels", "kcModels" })
public class DatasetDTO extends DTO {
    /** the id */
    private Integer id;
    /** the dataset name */
    private String name;

    /** the description */
    private String description;
    /** the domain */
    private String domain;
    /** the learn lab */
    private String learnlab;
    /** tutor */
    private String tutor;

    /** the date on which collection began for this dataset */
    private Date startDate;
    /** the date on which this dataset finished collecting data */
    private Date endDate;
    /** the primary investigator ID */
    private String pi;
    /** the primary investigator name */
    private String piName;
    /** the data provider ID */
    private String dataProvider;
    /** the data provider name */
    private String dataProviderName;
    /** the current status */
    private String status;
    /** project associated with this dataset */
    private String project;
    /** the curriculum */
    private String curriculum;
    /** the school */
    private String school;
    /** public, view, edit or private */
    private String access;
    /** notes on this dataset */
    private String additionalNotes;
    /** Hypothesis that this dataset was gathered to explore. */
    private String hypothesis;
    /** the acknowledgment for this dataset. */
    private String acknowledgment;
    /** the citation for this dataset. */
    private String citation;

    /** whether the dataset is public */
    private Boolean isPublic = null;
    /** whether the dataset has been released */
    private Boolean isReleased = null;
    /** whether the dataset has study data */
    private String hasStudyData = null;

    /** the number of students in this dataset */
    private Long numberOfStudents;
    /** the number of student hours */
    private Double numberOfStudentHours;
    /** the number of transactions in this dataset */
    private Long numberOfTransactions;
    /** the number of unique steps in this dataset */
    private Long numberOfUniqueSteps;
    /** the number of steps in this dataset */
    private Long numberOfSteps;
    /** the number of samples in this dataset */
    private Integer numberOfSamples;
    /** the number of accessible samples in this dataset */
    private Integer numberOfAccessibleSamples;
    /** the number of KC models in this dataset */
    private Integer numberOfKcModels;

    /** this dataset's KC models */
    private List<KcModelDTO> kcModels;

    /** the id. @return the id */
    public Integer getId() { return id; }

    /** the id. @param id the id */
    public void setId(Integer id) { this.id = id; }

    /** the dataset name. @return the dataset name */
    public String getName() { return name; }

    /** the dataset name. @param name the dataset name */
    public void setName(String name) { this.name = name; }

    /** the description. @return the description */
    public String getDescription() { return description; }

    /** the description. @param description the description */
    public void setDescription(String description) { this.description = description; }

//    /** the learn lab. @return the learn lab */
//    public String getLearnlab() { return learnlab; }
//
//    /** the learn lab. @param learnLab the learn lab */
//    public void setLearnlab(String learnlab) { this.learnlab = learnlab; }

    /** tutor.  @return tutor */
    public String getTutor() { return tutor; }

    /** tutor. @param tutor */
    public void setTutor(String tutor) { this.tutor = tutor; }

    /**
     * the date on which collection began for this dataset.
     * @return the startDate the date on which collection began for this dataset
     */
    public Date getStartDate() { return startDate; }

    /**
     * the date on which collection began for this dataset.
     * @param startDate the date on which collection began for this dataset
     */
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    /**
     * the date on which this dataset finished collecting data.
     * @return the date on which this dataset finished collecting data
     */
    public Date getEndDate() { return endDate; }

    /**
     * the date on which this dataset finished collecting data.
     * @param endDate the date on which this dataset finished collecting data
     */
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    /** the primary investigator id. @return the primary investigator id */
    public String getPi() { return pi; }

    /** the primary investigator id. @param pi the primary investigator id */
    public void setPi(String pi) { this.pi = pi; }

    /** the primary investigator name. @return the primary investigator name */
    public String getPiName() { return piName; }

    /** the primary investigator name. @param piName the primary investigator name */
    public void setPiName(String piName) { this.piName = piName; }

    /** the data provider id. @return the data provider id */
    public String getDataProvider() { return dataProvider; }

    /** the data provider id. @param dataProvider the data provider id */
    public void setDataProvider(String dataProvider) { this.dataProvider = dataProvider; }

    /** the data provider name. @return the data provider name */
    public String getDataProviderName() { return dataProviderName; }

    /** the data provider name. @param dataProviderName the data provider name */
    public void setDataProviderName(String dataProviderName) {
        this.dataProviderName = dataProviderName;
    }

    /** the current status. @return the current status */
    public String getStatus() { return status; }

    /** the current status. @param status the current status */
    public void setStatus(String status) { this.status = status; }

    /** whether the dataset is public. @return whether the dataset is public */
    public Boolean isPublic() { return isPublic; }

    /** whether the dataset is public. @param isPublic whether the dataset is public */
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    /** whether the dataset has been released. @return whether the dataset has been released */
    public Boolean isReleased() { return isReleased; }

    /** whether the dataset has been released. @return whether the dataset has been released */
    public void setReleased(boolean isReleased) { this.isReleased = isReleased; }

    /**
     * the number of transactions in this dataset.
     * @return the number of transactions in this dataset
     */
    public Long getNumberOfTransactions() { return numberOfTransactions; }

    /**
     * the number of transactions in this dataset.
     * @param numberOfTransactions the number of transactions in this dataset
     */
    public void setNumberOfTransactions(Long numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    /**
     * the number of unique steps in this dataset.
     * @return the number of unique steps in this dataset
     */
    public Long getNumberOfUniqueSteps() { return numberOfUniqueSteps; }

    /**
     * the number of unique steps in this dataset.
     * @param numberOfUniqueSteps the number of unique steps in this dataset
     */
    public void setNumberOfUniqueSteps(Long numberOfUniqueSteps) {
        this.numberOfUniqueSteps = numberOfUniqueSteps == -1 ? 0 : numberOfUniqueSteps;
    }
    /**
     * the number of samples in this dataset.
     * @return the number of samples in this dataset
     */
    public Integer getNumberOfSamples() { return numberOfSamples; }

    /**
     * the number of samples in this dataset.
     * @param numberOfSamples the number of samples in this dataset
     */
    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    /**
     * the number of accessible samples in this dataset.
     * @return the number of accessible samples in this dataset
     */
    public Integer getNumberOfAccessibleSamples() { return numberOfAccessibleSamples; }

    /**
     * the number of accessible samples in this dataset.
     * @param numberOfAccessibleSamples the number of accessible samples in this dataset
     */
    public void setNumberOfAccessibleSamples(Integer numberOfAccessibleSamples) {
        this.numberOfAccessibleSamples = numberOfAccessibleSamples;
    }

    /**
     * the number of KC models in this dataset.
     * @return the number of KC models in this dataset
     */
    public Integer getNumberOfKcModels() { return numberOfKcModels; }

    /**
     * the number of KC models in this dataset.
     * @param numberOfKcModels the number of KC models in this dataset
     */
    public void setNumberOfKcModels(Integer numberOfKcModels) {
        this.numberOfKcModels = numberOfKcModels;
    }

    /** project associated with this dataset. @return project associated with this dataset */
    public String getProject() { return project; }

    /**
     * project associated with this dataset.
     * @param project project associated with this dataset
     */
    public void setProject(String project) { this.project = project; }

    /** domain that this dataset falls under.
     * @return domain that this dataset falls under.
     * */
    public String getDomain() { return domain; }

    /**
     * domain that this dataset falls under.
     * @param domain domain that this dataset falls under.
     */
    public void setDomain(String domain) { this.domain = domain; }

    /** learnlab that this dataset falls under.
     * @return domain that this dataset falls under.
     * */
    public String getLearnlab() { return learnlab; }

    /**
     * learnlab that this dataset falls under.
     * @param learnlab learnlab that this dataset falls under.
     */
    public void setLearnlab(String learnlab) { this.learnlab = learnlab; }

    /** the curriculum. @return the curriculum */
    public String getCurriculum() { return curriculum; }

    /** the curriculum. @param curriculum the curriculum */
    public void setCurriculum(String curriculum) { this.curriculum = curriculum; }

    /** the school. @return the school */
    public String getSchool() { return school; }

    /** the school. @param school the school */
    public void setSchool(String school) { this.school = school; }

    /**
     * public, view, edit or private.
     * @return public, view, edit or private
     */
    public String getAccess() { return access; }

    /**
     * public, view, edit or private.
     * @param access public, view, edit or private
     */
    public void setAccess(String access) { this.access = access; }

    /** notes on this dataset. @return notes on this dataset */
    public String getAdditionalNotes() {
        return additionalNotes;
    }

    /** notes on this dataset. @param additionalNotes notes on this dataset */
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    /**
     * Hypothesis that this dataset was gathered to explore.
     * @return the hypothesis that this dataset was gathered to explore
     */
    public String getHypothesis() {
        return hypothesis;
    }

    /**
     * Hypothesis that this dataset was gathered to explore.
     * @param hypothesis the hypothesis that this dataset was gathered to explore
     */
    public void setHypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
    }

    /**
     * Acknowledgment for this dataset.
     * @param acknowledgment the acknowledgment for this dataset
     */
    public void setAcknowledgment(String acknowledgment) {
        this.acknowledgment = acknowledgment;
    }

    /**
     * Acknowledgment for this dataset.
     * @return the acknowledgment for this dataset
     */
    public String getAcknowledgment() {
        return acknowledgment;
    }

    /**
     * Citation for this dataset.
     * @param citation the citation for this dataset
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * Citation for this dataset.
     * @return the citation for this dataset
     */
    public String getCitation() {
        return citation;
    }

    /**
     * whether the dataset has study data.
     * @return hasStudyData whether the dataset has study data
     */
    public String getHasStudyData() { return this.hasStudyData; }

    /**
     * whether the dataset has study data.
     * @param hasStudyData whether the dataset has study data
     */
    public void setHasStudyData(String hasStudyData) { this.hasStudyData = hasStudyData; }

    /**
     * the number of students in this dataset.
     * @return the number of students in this dataset
     */
    public Long getNumberOfStudents() { return numberOfStudents; }

    /**
     * the number of students in this dataset.
     * @param numberOfStudents the number of students in this dataset
     */
    public void setNumberOfStudents(Long numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    /**
     * the number of student hours in this dataset.
     * @return the number of student hours in this dataset
     */
    public Double getNumberOfStudentHours() { return numberOfStudentHours; }

    /**
     * the number of student hours in this dataset.
     * @param numberOfStudentHours the number of student hours in this dataset
     */
    public void setNumberOfStudentHours(Double numberOfStudentHours) {
        this.numberOfStudentHours = numberOfStudentHours;
    }

    /**
     * the number of students in this dataset.
     * @return the number of students in this dataset
     */
    public Long getNumberOfSteps() { return numberOfSteps; }

    /**
     * the number of students in this dataset.
     * @param numberOfSteps the number of students in this dataset
     */
    public void setNumberOfSteps(Long numberOfSteps) {
        this.numberOfSteps = numberOfSteps == -1 ? 0 : numberOfSteps; }

    /** this dataset's KC models. @param kcModels this dataset's KC models */
    public void setKcModels(List<KcModelDTO> kcModels) { this.kcModels = kcModels; }

    /** this dataset's KC models. @return the kcModel */
    public List<KcModelDTO> getKcModels() {
        kcModels = checkNull(kcModels);
        return kcModels;
    }

    /** Add a KC model. @param kcModel */
    public void addKcModel(KcModelDTO kcModel) { getKcModels().add(kcModel); }
}
