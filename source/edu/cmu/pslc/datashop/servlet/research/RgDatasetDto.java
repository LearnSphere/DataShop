/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.research;

import java.util.List;

import edu.cmu.pslc.datashop.dto.DTO;

/**
 * Data Transfer Object to hold the data ready to be displayed by the JSP.
 *
 * @author alida
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RgDatasetDto extends DTO {

    //----- CONSTANTS -----

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer datasetId;
    /** Class attribute. */
    private String datasetName;
    /** Class attribute. */
    private Boolean isPublicFlag;
    /** Class attribute. */
    private List<RgPaperDto> paperList;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public RgDatasetDto() { }

    //----- GETTERS and SETTERS -----

    /**
     * Gets datasetId.
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }


    /**
     * Sets the datasetId.
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }


    /**
     * Gets datasetName.
     * @return the datasetName
     */
    public String getDatasetName() {
        return datasetName;
    }


    /**
     * Sets the datasetName.
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * Gets isPublicFlag.
     * @return the isPublicFlag
     */
    public Boolean getIsPublicFlag() {
        return isPublicFlag;
    }

    /**
     * Sets isPublicFlag.
     * @param isPublicFlag the isPublicFlag to set
     */
    public void setIsPublicFlag(Boolean isPublicFlag) {
        this.isPublicFlag = isPublicFlag;
    }

    /**
     * Gets paperList.
     * @return the paperList
     */
    public List<RgPaperDto> getPaperList() {
        return paperList;
    }


    /**
     * Sets the paperList.
     * @param paperList the paperList to set
     */
    public void setPaperList(List<RgPaperDto> paperList) {
        this.paperList = paperList;
    }
}
