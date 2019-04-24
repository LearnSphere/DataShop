/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2015
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.project;

import java.text.DecimalFormat;

/**
 * This is a POJO for the discourses/datasets within a DiscourseDB project.
 * It is similar to a subset of DiscourseItems but with extra for displaying
 * meta data.
 *
 * @author Cindy Tipper
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectDiscourseDto {

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Long id;
    /** Discourse name. */
    private String name;
    /** Date range. */
    private String dateRange;
    /** Number of users. */
    private Long numUsers;
    /** Number of contributions. */
    private Long numContributions;
    /** Number of data sources. */
    private Long numDataSources;
    /** If null then this discourse is not in the IQ table. */
    private Integer importQueueId = null;
    /** If discourse was uploaded by user. */
    private String uploaderName = null;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param id database generated unique id for the discourse item
     * @param name the name of the discourse
     * @param dateRange the range of dates for the discourse
     * @param numUsers the number of users in this discourse
     * @param numContributions number of contributions in the discourse
     * @param numDataSources number of data sources in this dicourse
     */
    public ProjectDiscourseDto(Long id, String name, String dateRange,
                               Long numUsers, Long numContributions, Long numDataSources) {
        this.id = id;
        setName(name);
        setDateRange(dateRange);
        setNumUsers(numUsers);
        setNumContributions(numContributions);
        setNumDataSources(numDataSources);
    }

    /**
     * Constructor.
     * @param id database generated unique id for the discourse item
     * @param name the name of the discourse
     */
    public ProjectDiscourseDto(Long id, String name) {
        this.id = id;
        setName(name);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the id.
     * @return the id
     */
    public Long getId() { return id; }

    /**
     * Set the id.
     * @param id the id to set
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Get the name.
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Set the name.
     * @param discourseName the discourse name
     */
    public void setName(String discourseName) { this.name = discourseName; }

    /**
     * Get the date range.
     * @return the dateRange
     */
    public String getDateRange() { return dateRange; }

    /**
     * Set the date range.
     * @param dateRange the date range
     */
    public void setDateRange(String dateRange) {
        this.dateRange = (dateRange == null) ? "" : dateRange;
    }

    /**
     * Gets the numUsers.
     * @return the numUsers
     */
    public Long getNumUsers() {
        return numUsers;
    }

    /**
     * Sets the numUsers.
     * @param numUsers the numUsers to set
     */
    public void setNumUsers(Long numUsers) {
        this.numUsers = numUsers;
    }

    /**
     * Get the number of contributions.
     * @return the numContributions
     */
    public Long getNumContributions() { return numContributions; }

    /**
     * Set the number of contributions.
     * @param numContributions the number of contributions
     */
    public void setNumContributions(Long numContributions) {
        this.numContributions = numContributions;
    }

    /**
     * Get the number of contributions, nicely formatted.
     * @return the numContributions
     */
    public String getNumContributionsFormatted() {
        return new DecimalFormat("##,###,##0").format(getNumContributions());
    }

    /**
     * Gets the numDataSources.
     * @return the numDataSources
     */
    public Long getNumDataSources() {
        return numDataSources;
    }

    /**
     * Sets the numDataSources.
     * @param numDataSources the numDataSources to set
     */
    public void setNumDataSources(Long numDataSources) {
        this.numDataSources = numDataSources;
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
}
