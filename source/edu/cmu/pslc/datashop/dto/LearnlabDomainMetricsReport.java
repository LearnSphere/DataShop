/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a DTO for MetricByLearnlab and MetricByDomain Report.
 * It contains the report data and list of MetricsReportSession objects.
 * @author Shanwen Yu
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnlabDomainMetricsReport extends DTO {

    /** The map to a list of report data items. */
    private Map<String, List> map = new LinkedHashMap<String, List>();
    /** The string value of time when the report is generated. */
    private String formattedTime;
    /** The string value of total domain files. */
    private String totalDomainFiles;
    /** The string value of total domain papers. */
    private String totalDomainPapers;
    /** The string value of total domain datasets. */
    private String totalDomainDatasets;
    /** The string value of total domain students. */
    private String totalDomainStudents;
    /** The string value of total domain actions. */
    private String totalDomainActions;
    /** The string value of total domain hours. */
    private String totalDomainHours;

    /** The string value of total learnlab files. */
    private String totalLearnlabFiles;
    /** The string value of total learnlab papers. */
    private String totalLearnlabPapers;
    /** The string value of total learnlab datasets. */
    private String totalLearnlabDatasets;
    /** The string value of total learnlab students. */
    private String totalLearnlabStudents;
    /** The string value of total learnlab actions. */
    private String totalLearnlabActions;
    /** The string value of total learnlab hours. */
    private String totalLearnlabHours;

    /** A map of reports for remote instances. */
    private Map<String, LearnlabDomainMetricsReport> remoteInstanceReports =
        new LinkedHashMap<String, LearnlabDomainMetricsReport>();

    /**
     * Default constructor.
     */
    public LearnlabDomainMetricsReport() {
        super();
    }
    /**
     * Set map.
     * @param description possible values: "Domain" or "Learnlab"
     * @param valueList list of values
     */
    public void setMap(String description, List valueList) {
        map.put(description, valueList);
    }
    /**
     * Get map.
     * @return map
     */
    public  Map<String, List> getMap() {  return map;   }
    /**
     * Set a String of formatted time.
     * @param formattedTime string of formatted time
     */
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
    /**
     * Get a String of formatted time.
     * @return a string of formatted time
     */
    public String getFormattedTime() {
        return formattedTime;
    }
    /**
     * set a total number of files by summing all the files in the domains.
     * @param totalDomainFiles number of files
     */
    public void setTotalDomainFiles(String totalDomainFiles) {
        this.totalDomainFiles = totalDomainFiles;
    }

    /**
     * get a total number of files.
     * @return total number of files
     */
    public String getTotalDomainFiles() {
        return this.totalDomainFiles;
    }

    /**
     * set a total number of papers by summing all the papers in the domains.
     * @param totalDomainPapers number of papers
     */
    public void setTotalDomainPapers(String totalDomainPapers) {
        this.totalDomainPapers = totalDomainPapers;
    }

    /**
     * get a total number of papers.
     * @return total number of papers
     */
    public String getTotalDomainPapers() {
        return this.totalDomainPapers;
    }
    /**
     * set a total number of datasets by summing all the datasets in the domains.
     * @param totalDomainDatasets number of datasets
     */
    public void setTotalDomainDatasets(String totalDomainDatasets) {
        this.totalDomainDatasets = totalDomainDatasets;
    }

    /**
     * get a total number of datasets.
     * @return total number of datasets
     */
    public String getTotalDomainDatasets() {
        return this.totalDomainDatasets;
    }

    /**
     * set a total number of students by summing all the students in the domains.
     * @param totalDomainStudents number of students
     */
    public void setTotalDomainStudents(String totalDomainStudents) {
        this.totalDomainStudents = totalDomainStudents;
    }

    /**
     * get a total number of students.
     * @return total number of students
     */
    public String getTotalDomainStudents() {
        return this.totalDomainStudents;
    }

    /**
     * set a total number of actions by summing all the actions in the domains.
     * @param totalDomainActions number of actions
     */
    public void setTotalDomainActions(String totalDomainActions) {
        this.totalDomainActions = totalDomainActions;
    }

    /**
     * get a total number of actions.
     * @return total number of actions
     */
    public String getTotalDomainActions() {
        return this.totalDomainActions;
    }

    /**
     * set a total number of hours by summing all the hours in the domains.
     * @param totalDomainHours number of hours
     */
    public void setTotalDomainHours(String totalDomainHours) {
        this.totalDomainHours = totalDomainHours;
    }

    /**
     * get a total number of hours.
     * @return total number of hours
     */
    public String getTotalDomainHours() {
        return this.totalDomainHours;
    }

    /**
     * set a total number of files by summing all the files in the learnlabs.
     * @param totalLearnlabFiles number of files
     */
    public void setTotalLearnlabFiles(String totalLearnlabFiles) {
        this.totalLearnlabFiles = totalLearnlabFiles;
    }

    /**
     * get a total number of files.
     * @return total number of files
     */
    public String getTotalLearnlabFiles() {
        return this.totalLearnlabFiles;
    }

    /**
     * set a total number of papers by summing all the papers in the learnlabs.
     * @param totalLearnlabPapers number of papers
     */
    public void setTotalLearnlabPapers(String totalLearnlabPapers) {
        this.totalLearnlabPapers = totalLearnlabPapers;
    }

    /**
     * get a total number of papers.
     * @return total number of papers
     */
    public String getTotalLearnlabPapers() {
        return this.totalLearnlabPapers;
    }

    /**
     * set a total number of datasets by summing all the datasets in the learnlabs.
     * @param totalLearnlabDatasets number of datasets
     */
    public void setTotalLearnlabDatasets(String totalLearnlabDatasets) {
        this.totalLearnlabDatasets = totalLearnlabDatasets;
    }

    /**
     * get a total number of datasets.
     * @return total number of datasets
     */
    public String getTotalLearnlabDatasets() {
        return this.totalLearnlabDatasets;
    }

    /**
     * set a total number of students by summing all the students in the learnlabs.
     * @param totalLearnlabStudents number of students
     */
    public void setTotalLearnlabStudents(String totalLearnlabStudents) {
        this.totalLearnlabStudents = totalLearnlabStudents;
    }

    /**
     * get a total number of students.
     * @return total number of students
     */
    public String getTotalLearnlabStudents() {
        return this.totalLearnlabStudents;
    }

    /**
     * set a total number of actions by summing all the actions in the learnlabs.
     * @param totalLearnlabActions number of actions
     */
    public void setTotalLearnlabActions(String totalLearnlabActions) {
        this.totalLearnlabActions = totalLearnlabActions;
    }

    /**
     * get a total number of actions.
     * @return total number of actions
     */
    public String getTotalLearnlabActions() {
        return this.totalLearnlabActions;
    }

    /**
     * set a total number of hours by summing all the hours in the learnlabs.
     * @param totalLearnlabHours number of hours
     */
    public void setTotalLearnlabHours(String totalLearnlabHours) {
        this.totalLearnlabHours = totalLearnlabHours;
    }

    /**
     * get a total number of hours.
     * @return total number of hours
     */
    public String getTotalLearnlabHours() {
        return this.totalLearnlabHours;
    }

    /**
     * Set the map of remote instance reports.
     * @param remoteInstanceReports the map
     */
    public void setRemoteInstanceReports(Map<String, LearnlabDomainMetricsReport>
                                         remoteInstanceReports) {
        this.remoteInstanceReports = remoteInstanceReports;
    }

    /**
     * Get the map of remote instance reports.
     * @return the map
     */
    public Map<String, LearnlabDomainMetricsReport> getRemoteInstanceReports() {
        return remoteInstanceReports;
    }

    /**
     * Add to the map of remote instance reports.
     * @param instanceName the remote instance name
     * @param remoteInstanceReport one report
     */
    public void addRemoteInstanceReport(String instanceName,
                                        LearnlabDomainMetricsReport remoteInstanceReport) {
        this.remoteInstanceReports.put(instanceName, remoteInstanceReport);
    }
}
