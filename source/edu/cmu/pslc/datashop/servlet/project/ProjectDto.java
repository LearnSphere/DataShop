/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2012
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.project;

import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;

import edu.cmu.pslc.datashop.item.ImportQueueItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for the Project datasets page, which is passed to the JSP from the servlet.
 *
 * @author Cindy Tipper
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectDto {

    //----- CONSTANTS -----
    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Principal Investigator name, for display */
    private String piName;
    /** Principal Investigator user id */
    private String piUserId;
    /** Data Provider name, for display */
    private String dpName;
    /** Data Provider user id */
    private String dpUserId;
    /** Description */
    private String description;
    /** Tags... comma-delimited string */
    private String tags;
    /** List of external links. */
    private List<ExternalLinkDto> externalLinks;
    /** List of datasets to display in the table. */
    private List<ProjectDatasetDto> datasets;
    /** Import queue list for this project. */
    private List<ImportQueueDto> importQueueList;

    /* For DiscourseDB project(s) only, at this point. */
    /** List of Discourses to display. */
    private List<ProjectDiscourseDto> discourses;

    /** Success/Error message */
    private String message;
    /** Level of message, success, warn error. */
    private String messageLevel;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param id the project id
     * @param piName the PI
     * @param piUserId the PI user id
     * @param dpName the DP
     * @param dpUserId the DP user id
     * @param description the description
     * @param tags the tags
     */
    public ProjectDto(Integer id, String piName, String piUserId, String dpName, String dpUserId,
                      String description, String tags) {
        this.id = id;
        setPiName(piName);
        setPiUserId(piUserId);
        setDpName(dpName);
        setDpUserId(dpUserId);
        setDescription(description);
        setTags(tags);
    }

    //----- GETTERs and SETTERs -----

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
     * Get the PI.
     * @return the piName
     */
    public String getPiName() {
        return piName;
    }
    /**
     * Set the PI.
     * @param piName the name to set
     */
    public void setPiName(String piName) {
        this.piName = piName;
    }
    /**
     * Get the PI userId.
     * @return the piUserId
     */
    public String getPiUserId() {
        return piUserId;
    }
    /**
     * Set the PI userId.
     * @param piUserId the id to set
     */
    public void setPiUserId(String piUserId) {
        this.piUserId = piUserId;
    }
    /**
     * Get the DP.
     * @return the dpName
     */
    public String getDpName() {
        return dpName;
    }
    /**
     * Set the DP.
     * @param dpName the name to set
     */
    public void setDpName(String dpName) {
        this.dpName = dpName;
    }
    /**
     * Get the DP userId.
     * @return the dpUserId
     */
    public String getDpUserId() {
        return dpUserId;
    }
    /**
     * Set the DP userId.
     * @param dpUserId the id to set
     */
    public void setDpUserId(String dpUserId) {
        this.dpUserId = dpUserId;
    }
    /**
     * Get the description.
     * @return the description
     */
    public String getDescription() {
        if (description == null) { return ""; }
        return description;
    }
    /**
     * Set the description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Get the comma-delimited list of tags.
     * @return the tags
     */
    public String getTags() {
        if (tags == null) { return ""; }
        return tags;
    }
    /**
     * Set the tags as a comma-delimited list of strings..
     * @param tags the tags to set
     */
    public void setTags(String tags) {
        this.tags = tags;
    }
    /**
     * Get the list of datasets for this project.
     * @return the datasets
     */
    public List<ProjectDatasetDto> getDatasets() {
        if (datasets == null) { return new ArrayList<ProjectDatasetDto>(); }
        return datasets;
    }
    /**
     * Set the list of datasets.
     * @param datasets the list of datasets
     */
    public void setDatasets(List<ProjectDatasetDto> datasets) {
        this.datasets = datasets;
    }
    /**
     * Get the list of external links for this project.
     * @return the externalLinks
     */
    public List<ExternalLinkDto> getExternalLinks() {
        return externalLinks;
    }
    /**
     * Set the list of external links.
     * @param externalLinks the list of external links
     */
    public void setExternalLinks(List<ExternalLinkDto> externalLinks) {
        this.externalLinks = externalLinks;
    }
    /**
     * Get the import queue list for this project.
     * @return the list
     */
    public List<ImportQueueDto> getImportQueueList() {
        if (importQueueList == null) { return new ArrayList<ImportQueueDto>(); }
        return importQueueList;
    }
    /**
     * Set the import queue list for this project.
     * @param importQueueList the list
     */
    public void setImportQueueList(List<ImportQueueDto> importQueueList) {
        this.importQueueList = importQueueList;
    }

    /**
     * Get the list of discourses for this project.
     * @return the discourses
     */
    public List<ProjectDiscourseDto> getDiscourses() {
        if (discourses == null) { return new ArrayList<ProjectDiscourseDto>(); }
        return discourses;
    }
    /**
     * Set the list of discourses.
     * @param discourses the list of discourses
     */
    public void setDiscourses(List<ProjectDiscourseDto> discourses) {
        this.discourses = discourses;
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

    //----- UTILITY METHODS -----

    /**
     * Helper method to determine if delete is allowed on this project.
     * Delete is only valid for projects with no datasets AND
     * nothing in the Import Queue with a display_flag value of 'true'.
     * @return boolean indicating if delete is allowed
     */
    public boolean isDeleteAllowed() {
        if (getDatasets().size() > 0) { return false; }
        if (getDiscourses().size() > 0) { return false; }
        for (ImportQueueDto dto : getImportQueueList()) {
            Boolean displayFlag = dto.getDisplayFlag();
            if ((displayFlag != null) && (displayFlag.equals(true))) {
                return false;
            }
        }
        return true;
    }

    /**
     * To display the string in a TD, convert HTML characters to entities
     * and add line breaks with line break (BR) elements.
     * @param str the string to clean up
     * @return the given string cleaned up for display
     */
    String cleanInputText(String str) {
        String textCleaner = (str == null) ? ""
            : str.replaceAll("&", "&amp;")
                 .replaceAll(">", "&gt;")
                 .replaceAll("<", "&lt;");
        return textCleaner.replaceAll("\n", "<br />");
    }
}
