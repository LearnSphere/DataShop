/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.item.DatasetItem;

/**
 * Data Transfer Object to hold the data ready to be displayed by the JSP.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10525 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-06 14:15:45 -0500 (Thu, 06 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbDatasetDto extends DTO {

    //----- CONSTANTS -----

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer datasetId;
    /** Class attribute. */
    private String datasetName;
    /** Class attribute. */
    private String appearsAnon;
    /** Class attribute. */
    private String irbUploaded;
    /** Class attribute. */
    private String hasStudyData;
    /** Class attribute. */
    private String dates;
    /** Class attribute. */
    private String dataLastModifiedDate;
    /** Class attribute. */
    private String dataLastModifiedTime;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public IrbDatasetDto() { }

    /**
     * Create a DTO from the item.
     * @param datasetItem the item with the info
     */
    public IrbDatasetDto(DatasetItem datasetItem) {
        this.datasetId = (Integer)datasetItem.getId();
        this.datasetName = datasetItem.getDatasetName();
        this.appearsAnon = datasetItem.getAppearsAnonymousDisplayStr();
        this.irbUploaded = datasetItem.getIrbUploaded();
        this.hasStudyData = datasetItem.getStudyFlag();
        this.dates = DatasetItem.getDateRangeString(datasetItem);
        this.dataLastModifiedDate = DatasetItem.getDataLastModifiedDate(datasetItem);
        this.dataLastModifiedTime = DatasetItem.getDataLastModifiedTime(datasetItem);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Gets the datasetId.
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
     * Gets the datasetName.
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
     * Gets the appearsAnon.
     * @return the appearsAnon
     */
    public String getAppearsAnon() {
        return appearsAnon;
    }
    /**
     * Sets the appearsAnon.
     * @param appearsAnon the appearsAnon to set
     */
    public void setAppearsAnon(String appearsAnon) {
        this.appearsAnon = appearsAnon;
    }
    /**
     * Gets the irbUploaded.
     * @return the irbUploaded
     */
    public String getIrbUploaded() {
        return irbUploaded;
    }
    /**
     * Sets the irbUploaded.
     * @param irbUploaded the irbUploaded to set
     */
    public void setIrbUploaded(String irbUploaded) {
        this.irbUploaded = irbUploaded;
    }
    /**
     * Gets the hasStudyData.
     * @return the hasStudyData
     */
    public String getHasStudyData() {
        return hasStudyData;
    }
    /**
     * Sets the hasStudyData.
     * @param hasStudyData the hasStudyData to set
     */
    public void setHasStudyData(String hasStudyData) {
        this.hasStudyData = hasStudyData;
    }
    /**
     * Gets the dates.
     * @return the dates
     */
    public String getDates() {
        return dates;
    }
    /**
     * Sets the dates.
     * @param dates the dates to set
     */
    public void setDates(String dates) {
        this.dates = dates;
    }
    /**
     * Gets the dataLastModifiedDate.
     * @return the dataLastModifiedDate
     */
    public String getDataLastModifiedDate() {
        return dataLastModifiedDate;
    }
    /**
     * Sets the dataLastModifiedDate.
     * @param dataLastModifiedDate the date to set
     */
    public void setDataLastModifiedDate(String dataLastModifiedDate) {
        this.dataLastModifiedDate = dataLastModifiedDate;
    }

    /**
     * Gets the dataLastModifiedTime.
     * @return the dataLastModifiedTime
     */
    public String getDataLastModifiedTime() {
        return dataLastModifiedTime;
    }
    /**
     * Sets the dataLastModifiedTime.
     * @param dataLastModifiedTime the date including time to set
     */
    public void setDataLastModifiedTime(String dataLastModifiedTime) {
        this.dataLastModifiedTime = dataLastModifiedTime;
    }

}
