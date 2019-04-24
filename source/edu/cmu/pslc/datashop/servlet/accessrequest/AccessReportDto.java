/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2013
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.accessrequest;

import java.util.List;

import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;

/**
 * This is a POJO for the Access Report on the Access Requests page.
 *
 * @author Cindy Tipper
 * @version $Revision: 10830 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-21 13:12:28 -0400 (Fri, 21 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessReportDto {

    //----- ATTRIBUTES -----

    /** Access report. */
    private List<ProjectRequestDTO> accessReport;
    /** Number of total records for 'Access Report'. */
    private Integer numRecords;
    /** Number of total pages for 'Access Report'. */
    private Integer numPages;
    /** Flag indicating whether or not to display 'Terms of Use' columns. */
    private Boolean hasTermsOfUse;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param accessReport list making 'Access Report'
     * @param numRecords number of total records
     * @param numPages number of pages in report
     */
    public AccessReportDto(List<ProjectRequestDTO> accessReport, int numRecords, int numPages) {
        setAccessReport(accessReport);
        setNumRecords(numRecords);
        setNumPages(numPages);
    }

    //----- GETTERS and SETTERS -----

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
     * Get the number of 'Access Report' records.
     * @return Integer num records
     */
    public Integer getNumRecords() {
        return numRecords;
    }

    /**
     * Set the number of 'Access Report' records.
     * @param numRecords the num records
     */
    public void setNumRecords(Integer numRecords) {
        this.numRecords = numRecords;
    }

    /**
     * Get the number of 'Access Report' pages.
     * @return Integer num pages.
     */
    public Integer getNumPages() {
        return numPages;
    }

    /**
     * Set the number of 'Access Report' pages.
     * @param numPages the num pages
     */
    public void setNumPages(Integer numPages) {
        this.numPages = numPages;
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
