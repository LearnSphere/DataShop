/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * Used to transfer discourse data as XML, JSON, etc.
 * Liberal copying of DatasetDTO, by Jim Rankin.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "discourse",
                properties = { "id", "name", "dateRange", "numberOfUsers",
                               "numberOfDiscourseParts", "numberOfContributions",
                               "numberOfDataSources", "numberOfRelations" })
public class DiscourseDTO extends DTO {
    /** the id */
    private Long id;
    /** the discourse name */
    private String name;
    /** the date range */
    private String dateRange;
    /** the number of users */
    private Long numberOfUsers;
    /** the number of discourse parts */
    private Long numberOfDiscourseParts;
    /** the number of contributions */
    private Long numberOfContributions;
    /** the number of data sources */
    private Long numberOfDataSources;
    /** the number of relations */
    private Long numberOfRelations;

    /** the id. @return the id */
    public Long getId() { return id; }

    /** the id. @param id the id */
    public void setId(Long id) { this.id = id; }

    /** the discourse name. @return the discourse name */
    public String getName() { return name; }

    /** the discourse name. @param name the discourse name */
    public void setName(String name) { this.name = name; }

    /** the date range. @return the dateRange */
    public String getDateRange() { return dateRange; }

    /** the date range. @param dateRange the date range */
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    /** the number of users. @return the numberOfUsers */
    public Long getNumberOfUsers() { return numberOfUsers; }

    /** the number of users. @param numUsers the number of users */
    public void setNumberOfUsers(Long numUsers) { this.numberOfUsers = numUsers; }

    /** the number of discourseParts. @return the numberOfDiscourseParts */
    public Long getNumberOfDiscourseParts() { return numberOfDiscourseParts; }

    /** the number of discourseParts. @param numDiscourseParts the number of discourseParts */
    public void setNumberOfDiscourseParts(Long numDiscourseParts) {
        this.numberOfDiscourseParts = numDiscourseParts;
    }

    /** the number of contributions. @return the numberOfContributions */
    public Long getNumberOfContributions() { return numberOfContributions; }

    /** the number of contributions. @param numContributions the number of contributions */
    public void setNumberOfContributions(Long numContributions) {
        this.numberOfContributions = numContributions;
    }

    /** the number of dataSources. @return the numberOfDataSources */
    public Long getNumberOfDataSources() { return numberOfDataSources; }

    /** the number of dataSources. @param numDataSources the number of dataSources */
    public void setNumberOfDataSources(Long numDataSources) {
        this.numberOfDataSources = numDataSources;
    }

    /** the number of relations. @return the numberOfRelations */
    public Long getNumberOfRelations() { return numberOfRelations; }

    /** the number of relations. @param numRelations the number of relations */
    public void setNumberOfRelations(Long numRelations) { this.numberOfRelations = numRelations; }
}
