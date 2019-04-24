/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.project;

/**
 * DTO to hold the data from the fields on the Create Project page.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CreateProjectDto {

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private String newProjectName = "";
    /** Class attribute. */
    private String dataCollectionType;
    /** Class attribute. */
    private Boolean errorFlag = false;
    /** Class attribute. */
    private String errorMessage;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public CreateProjectDto() { }

    //----- GETTERS and SETTERS -----

    /**
     * Gets newProjectName.
     * @return the newProjectName
     */
    public String getNewProjectName() {
        return newProjectName;
    }

    /**
     * Sets the newProjectName.
     * @param newProjectName the newProjectName to set
     */
    public void setNewProjectName(String newProjectName) {
        this.newProjectName = newProjectName;
    }

    /**
     * Gets dataCollectionType.
     * @return the dataCollectionType
     */
    public String getDataCollectionType() {
        return dataCollectionType;
    }

    /**
     * Sets the dataCollectionType.
     * @param dataCollectionType the dataCollectionType to set
     */
    public void setDataCollectionType(String dataCollectionType) {
        this.dataCollectionType = dataCollectionType;
    }

    /**
     * Gets errorFlag.
     * @return the errorFlag
     */
    public Boolean getErrorFlag() {
        return errorFlag;
    }

    /**
     * Sets the errorFlag.
     * @param errorFlag the errorFlag to set
     */
    public void setErrorFlag(Boolean errorFlag) {
        this.errorFlag = errorFlag;
    }

    /**
     * Gets errorMessage.
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the errorMessage.
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
