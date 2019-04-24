/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2015
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.discoursedb.servlet;

import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This is a POJO for the Discourse Overview subtab.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDto implements java.io.Serializable {

    //----- CONSTANTS -----
    /** Constant for special-case 'Name' sort. */
    public static final String NAME_SORT_BY = "Name";

    //----- ATTRIBUTES -----

    /** Message. */
    private String message;
    /** Message level. */
    private String messageLevel;
    /** Discourse item. */
    private DiscourseItem discourse;
    /** Flag indicating if remote. */
    private Boolean isRemote = false;

    // For now, these DTOs are name/type and number only.
    /** List of DiscoursePartType DTOs. */
    private List<DiscoursePartTypeDto> parts;
    /** List of ContributionType DTOs. */
    private List<ContributionTypeDto> contributions;
    /** List of RelationType DTOs. */
    private List<RelationTypeDto> relations;

    // For now, until we have paging in place... */
    /** Number of data sources. */
    private Long numDataSources;
    /** DataSource types. */
    private String dataSourceTypes;
    /** DataSource datasets. */
    private String dataSourceDatasets;
    /** Number of annotations. */
    private Long numAnnotations;
    /** Annotation types. */
    private String annotationTypes;
    /** Number of users. */
    private Long numUsers;
    /** Date range. */
    private String dateRange;
    /** Number of DiscourseParts. */
    private Long numDiscourseParts;
    /** Number of Contributions. */
    private Long numContributions;
    /** Number of Relations. */
    private Long numRelations;

    // The following are TBD... once we have paging in place to handle numbers. */
    /** List of User DTOs. */
    //private List<UserDto> users;

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public DiscourseDto() { }

    //----- GETTERS and SETTERS -----

    /**
     * Get the Discourse item.
     * @return the discourse
     */
    public DiscourseItem getDiscourse() {
        return discourse;
    }

    /**
     * Set the Discourse item.
     * @param discourse
     */
    public void setDiscourse(DiscourseItem discourse) {
        this.discourse = discourse;
    }

    /**
     * Get isRemote flag.
     * @return isRemote
     */
    public Boolean getIsRemote() { return isRemote; }

    /**
     * Set isRemote flag.
     * @param isRemote
     */
    public void setIsRemote(Boolean isRemote) { this.isRemote = isRemote; }

    /**
     * Get the list of part DTOs.
     * @return the list
     */
    public List<DiscoursePartTypeDto> getDiscoursePartTypes() {
        return parts;
    }

    /**
     * Set the list of part DTOs.
     * @param parts the list of part DTOs
     */
    public void setDiscoursePartTypes(List<DiscoursePartTypeDto> parts) {
        this.parts = parts;
    }

    /**
     * Get the list of contribution DTOs.
     * @return the list
     */
    public List<ContributionTypeDto> getContributionTypes() {
        return contributions;
    }

    /**
     * Set the list of contribution DTOs.
     * @param contributions the list of contribution DTOs
     */
    public void setContributionTypes(List<ContributionTypeDto> contributions) {
        this.contributions = contributions;
    }

    /**
     * Get the list of relation DTOs.
     * @return the list
     */
    public List<RelationTypeDto> getRelationTypes() {
        return relations;
    }

    /**
     * Set the list of relation DTOs.
     * @param relations the list of relation DTOs
     */
    public void setRelationTypes(List<RelationTypeDto> relations) {
        this.relations = relations;
    }

    /**
     * Get the number of Data Sources in this Discourse.
     * @return the number
     */
    public Long getNumDataSources() { return numDataSources; }

    /**
     * Set the number of Data Sources in this Discourse.
     * @param numDataSources the number
     */
    public void setNumDataSources(Long numDataSources) { this.numDataSources = numDataSources; }

    /**
     * Get the number of Annotations in this Discourse.
     * @return the number
     */
    public Long getNumAnnotations() { return numAnnotations; }

    /**
     * Get the Data Source types in this Discourse.
     * @return the types, as a String
     */
    public String getDataSourceTypes() { return dataSourceTypes; }

    /**
     * Set the Data Source types in this Discourse.
     * @param dataSourceTypes the string
     */
    public void setDataSourceTypes(String dataSourceTypes) {
        this.dataSourceTypes = dataSourceTypes;
    }

    /**
     * Get the Data Source datasets in this Discourse.
     * @return the datasets, as a String
     */
    public String getDataSourceDatasets() { return dataSourceDatasets; }

    /**
     * Set the Data Source datasets in this Discourse.
     * @param dataSourceDatasets the string
     */
    public void setDataSourceDatasets(String dataSourceDatasets) {
        this.dataSourceDatasets = dataSourceDatasets;
    }

    /**
     * Set the number of Annotations in this Discourse.
     * @param numAnnotations the number
     */
    public void setNumAnnotations(Long numAnnotations) { this.numAnnotations = numAnnotations; }

    /**
     * Get the Annotation types in this Discourse.
     * @return the types, as a String
     */
    public String getAnnotationTypes() { return annotationTypes; }

    /**
     * Set the Annotation types in this Discourse.
     * @param annotationTypes the string
     */
    public void setAnnotationTypes(String annotationTypes) {
        this.annotationTypes = annotationTypes;
    }

    /**
     * Get the number of users in this Discourse.
     * @return the number
     */
    public Long getNumUsers() { return numUsers; }

    /**
     * Set the number of users in this Discourse.
     * @param numUsers the number
     */
    public void setNumUsers(Long numUsers) { this.numUsers = numUsers; }

    /**
     * Get the date range for this Discourse.
     * @return the range, as a String
     */
    public String getDateRange() { return dateRange; }

    /**
     * Set the date range string for this Discourse.
     * @param dateRange
     */
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    /**
     * Set the date range for this Discourse.
     * @param startTime starting time
     * @param endTime ending time
     */
    public void setDateRange(Date startTime, Date endTime) {
        this.dateRange = getDateRangeString(startTime, endTime);
    }

    /**
     * Get the number of DiscourseParts in this Discourse.
     * @return the number
     */
    public Long getNumDiscourseParts() { return numDiscourseParts; }

    /**
     * Set the number of DiscourseParts in this Discourse.
     * @param numDiscourseParts the number
     */
    public void setNumDiscourseParts(Long numDiscourseParts) {
        this.numDiscourseParts = numDiscourseParts;
    }

    /**
     * Get the number of Contributions in this Discourse.
     * @return the number
     */
    public Long getNumContributions() { return numContributions; }

    /**
     * Set the number of Contributions in this Discourse.
     * @param numContributions the number
     */
    public void setNumContributions(Long numContributions) {
        this.numContributions = numContributions;
    }

    /**
     * Get the number of Relations in this Discourse.
     * @return the number
     */
    public Long getNumRelations() { return numRelations; }

    /**
     * Set the number of Relations in this Discourse.
     * @param numRelations the number
     */
    public void setNumRelations(Long numRelations) {
        this.numRelations = numRelations;
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
     * Returns the relative path to the appropriate image.
     * @param columnName the column name to get an image for
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return relative path to image for given column
     */
    public static String getSortImage(String columnName, String sortByColumn, Boolean isAscending) {

        String imgIcon = "images/trans_spacer.gif";
        if (sortByColumn != null && sortByColumn.equals(columnName)) {
            imgIcon = isAscending ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /** Format for the date range method, getDateRangeString. */
    private static FastDateFormat prettyDateFormat = FastDateFormat.getInstance("MMM d, yyyy");

    /**
     * Helper method to generate a string of date ranges given start and end times.
     * @param startTime the starting time
     * @param endTime the ending time
     * @return String the range
     */
    private String getDateRangeString(Date startTime, Date endTime) {
        String dateRangeString = "-";

        if (startTime != null) {
            dateRangeString = prettyDateFormat.format(startTime);
        }
        if ((startTime != null) && (endTime != null)) {
            dateRangeString += " - ";
        }
        if (endTime != null) {
            if (dateRangeString == null) {
                dateRangeString = "";
            }
            dateRangeString += prettyDateFormat.format(endTime);
        }
        return dateRangeString;
    }
}
