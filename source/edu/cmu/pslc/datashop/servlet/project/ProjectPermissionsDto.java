/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2013
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.project;

import java.util.List;

import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;

/**
 * This is a POJO for the project permissions page.
 *
 * @author Cindy Tipper
 * @version $Revision: 10830 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-21 13:12:28 -0400 (Fri, 21 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectPermissionsDto {

    //----- ATTRIBUTES -----

    /** Message. */
    private String message;
    /** Message level. */
    private String messageLevel;
    /** List of access requests for this project. */
    private List<ProjectRequestDTO> requestsForAccess;
    /** List of current permissions for this project. */
    private List<ProjectRequestDTO> currentPermissions;
    /** Access report for this project. */
    private List<ProjectRequestDTO> accessReport;
    /** Number of records for 'Current Permissions' display. */
    private Integer currentPermissionsNumRecords;
    /** Number of total records for 'Current Permissions'. */
    private Integer currentPermissionsNumRecordsTotal;
    /** Number of total pages for 'Current Permissions'. */
    private Integer currentPermissionsNumPages;
    /** Number of records for 'Access Report' display. */
    private Integer accessReportNumRecords;
    /** Number of total records for 'Access Report'. */
    private Integer accessReportNumRecordsTotal;
    /** Number of total pages for 'Access Report'. */
    private Integer accessReportNumPages;
    /** Flag indicating whether or not to display 'Terms of Use' columns. */
    private Boolean hasTermsOfUse;

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param accessRequests list making up 'Requests for Access'
     * @param currentPermissions list making up 'Current Permissions'
     * @param accessReport list making 'Access Report'
     */
    public ProjectPermissionsDto(List<ProjectRequestDTO> accessRequests,
                                 List<ProjectRequestDTO> currentPermissions,
                                 List<ProjectRequestDTO> accessReport) {
        setRequestsForAccess(accessRequests);
        setCurrentPermissions(currentPermissions);
        setAccessReport(accessReport);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the list of access requests.
     * @return the list
     */
    public List<ProjectRequestDTO> getRequestsForAccess() {
        return requestsForAccess;
    }

    /**
     * Set the list of access requests.
     * @param requests list making up 'Requests for Access'
     */
    public void setRequestsForAccess(List<ProjectRequestDTO> requests) {
        this.requestsForAccess = requests;
    }

    /**
     * Get the list of current permissions.
     * @return the list
     */
    public List<ProjectRequestDTO> getCurrentPermissions() {
        return currentPermissions;
    }

    /**
     * Set the list of current permissions.
     * @param currentPerms list making up 'Current Permissions'
     */
    public void setCurrentPermissions(List<ProjectRequestDTO> currentPerms) {
        this.currentPermissions = currentPerms;
    }

    /**
     * Get the access report.
     * @return the list
     */
    public List<ProjectRequestDTO> getAccessReport() {
        return accessReport;
    }

    /**
     * Set the access report.
     * @param report list making up the 'Access Report'
     */
    public void setAccessReport(List<ProjectRequestDTO> report) {
        this.accessReport = report;
    }

    /**
     * Get the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message.
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the messageLevel.
     * @return the messageLevel
     */
    public String getMessageLevel() {
        return messageLevel;
    }

    /**
     * Set the messageLevel.
     * @param messageLevel the messageLevel to set
     */
    public void setMessageLevel(String messageLevel) {
        this.messageLevel = messageLevel;
    }

    /**
     * Get the number of 'Current Permissions' records.
     * @return Integer num records
     */
    public Integer getCurrentPermissionsNumRecords() {
        return currentPermissionsNumRecords;
    }

    /**
     * Set the number of 'Current Permissions' records.
     * @param numRecords the num records
     */
    public void setCurrentPermissionsNumRecords(Integer numRecords) {
        this.currentPermissionsNumRecords = numRecords;
    }

    /**
     * Get the total number of 'Current Permissions' records.
     * @return Integer num records
     */
    public Integer getCurrentPermissionsNumRecordsTotal() {
        return currentPermissionsNumRecordsTotal;
    }

    /**
     * Set the total number of 'Current Permissions' records.
     * @param numRecords the num records
     */
    public void setCurrentPermissionsNumRecordsTotal(Integer numRecords) {
        this.currentPermissionsNumRecordsTotal = numRecords;
    }

    /**
     * Get the number of 'Current Permissions' pages.
     * @return Integer num pages.
     */
    public Integer getCurrentPermissionsNumPages() {
        return currentPermissionsNumPages;
    }

    /**
     * Set the number of 'Current Permissions' pages.
     * @param numPages the num pages
     */
    public void setCurrentPermissionsNumPages(Integer numPages) {
        this.currentPermissionsNumPages = numPages;
    }

    /**
     * Get the number of 'Access Report' records.
     * @return Integer num records
     */
    public Integer getAccessReportNumRecords() {
        return accessReportNumRecords;
    }

    /**
     * Set the number of 'Access Report' records.
     * @param numRecords the num records
     */
    public void setAccessReportNumRecords(Integer numRecords) {
        this.accessReportNumRecords = numRecords;
    }

    /**
     * Get the total number of 'Access Report' records.
     * @return Integer num records
     */
    public Integer getAccessReportNumRecordsTotal() {
        return accessReportNumRecordsTotal;
    }

    /**
     * Set the total number of 'Access Report' records.
     * @param numRecords the num records
     */
    public void setAccessReportNumRecordsTotal(Integer numRecords) {
        this.accessReportNumRecordsTotal = numRecords;
    }

    /**
     * Get the number of 'Access Report' pages.
     * @return Integer num pages.
     */
    public Integer getAccessReportNumPages() {
        return accessReportNumPages;
    }

    /**
     * Set the number of 'Access Report' pages.
     * @param numPages the num pages
     */
    public void setAccessReportNumPages(Integer numPages) {
        this.accessReportNumPages = numPages;
    }

    /**
     * Get the flag indicating whether or not to display 'Terms of Use' columns.
     * @return Boolean hasTermsOfUse
     */
    public Boolean getHasTermsOfUse() {
        return hasTermsOfUse;
    }

    /**
     * Set the flag indicating whether or not to display 'Terms of Use' columns.
     * @param hasToU flag
     */
    public void setHasTermsOfUse(Boolean hasToU) {
        this.hasTermsOfUse = hasToU;
    }
}
