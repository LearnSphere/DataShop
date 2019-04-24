/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import java.util.List;

/**
 * DTO for the IRB page for a single project. This object contains
 * portions of the various IRB and Project items necessary for display
 * and is passed to the JSP from the servlet.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbProjectDto {

    //----- CONSTANTS -----

    /** Constant for the request attribute. */
    public static final String ATTRIB_NAME = "irbProjectDto";
    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";


    //----- ATTRIBUTES -----

    /** Success/Error message */
    private String message;
    /** Level of message, success, warn error. */
    private String messageLevel;
    /** Project review information. */
    private ProjectReviewDto projectReviewDto;
    /** List of IRB document DTOs. */
    private List<IrbDto> irbList;
    /** List of Project Shareability History DTOs. */
    private List<ProjectShareabilityHistoryDto> pshList;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param projectDto the project review info
     * @param irbList the list of IRB documents for this project
     * @param pshList the list of Project Shareability History items for this project
     */
    public IrbProjectDto(ProjectReviewDto projectDto,
                         List<IrbDto> irbList,
                         List<ProjectShareabilityHistoryDto> pshList) {
        this.projectReviewDto = projectDto;
        this.irbList = irbList;
        this.pshList = pshList;
    }

    //----- GETTERs and SETTERs -----

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
     * Get the ProjectReview DTO.
     * @return ProjectReviewDto project review info
     */
    public ProjectReviewDto getProjectReviewDto() {
        return projectReviewDto;
    }

    /**
     * Set the ProjectReview DTO.
     * @param theDto the DTO
     */
    public void setProjectReviewDto(ProjectReviewDto theDto) {
        this.projectReviewDto = theDto;
    }

    /**
     * Get the list of IRB DTOs.
     * @return the irbList
     */
    public List<IrbDto> getIrbList() {
        return irbList;
    }

    /**
     * Set the list of IRB DTOs.
     * @param irbList the list
     */
    public void setIrbList(List<IrbDto> irbList) {
        this.irbList = irbList;
    }

    /**
     * Get the Project Shareability History list.
     * @return the pshList
     */
    public List<ProjectShareabilityHistoryDto> getProjectShareabilityHistoryList() {
        return pshList;
    }

    /**
     * Set the Project Shareability History list.
     * @param pshList the list
     */
    public void setProjectShareabilityHistoryList(List<ProjectShareabilityHistoryDto> pshList) {
        this.pshList = pshList;
    }
}
