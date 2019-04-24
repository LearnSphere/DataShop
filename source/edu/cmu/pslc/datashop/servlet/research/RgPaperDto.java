/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.research;

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
public class RgPaperDto extends DTO {

    //----- CONSTANTS -----

    /** Constant for request attribute. */
    public static final String ATTRIB_LIST = "rg_paper_list";

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private Integer paperId;
    /** Class attribute. */
    private String citation;
    /** Class attribute. */
    private String truncatedCitation;
    /** Class attribute. */
    private String fileNameWithSize;
    /** Class attribute. */
    private Integer fileId;
    /** Class attribute. */
    private String fileName;
    /** Class attribute. */
    private Integer datasetId;
    /** Class attribute. */
    private String datasetName;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public RgPaperDto() { }

    //----- GETTERS and SETTERS -----

    /**
     * Gets paperId.
     * @return the paperId
     */
    public Integer getPaperId() {
        return paperId;
    }

    /**
     * Sets the paperId.
     * @param paperId the paperId to set
     */
    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    /**
     * Gets citation.
     * @return the citation
     */
    public String getCitation() {
        return citation;
    }

    /**
     * Sets the citation and the truncated citation.
     * @param citation the citation to set
     */
    public void setCitation(String citation) {
        final int maxLength = 50;
        this.citation = citation;
        if (citation.length() == 0) {
            truncatedCitation = "[blank citation]";
        } else {
            int idx = citation.indexOf(')');
            if (idx > 0) {
                truncatedCitation = citation.substring(idx + 1).trim();
                if (truncatedCitation.startsWith(".")) {
                    truncatedCitation = truncatedCitation.substring(1).trim();
                }
            } else {
                this.truncatedCitation = citation;
            }
            if (truncatedCitation.length() > maxLength) {
                truncatedCitation = truncatedCitation.substring(0, maxLength) + "...";
            }
        }
    }

    /**
     * Gets the truncated citation.
     * @return the truncated citation
     */
    public String getTruncatedCitation() {
        return truncatedCitation;
    }

    /**
     * Gets fileNameWithSize.
     * @return the fileNameWithSize
     */
    public String getFileNameWithSize() {
        return fileNameWithSize;
    }

    /**
     * Sets the fileNameWithSize.
     * @param fileNameWithSize the fileNameWithSize to set
     */
    public void setFileNameWithSize(String fileNameWithSize) {
        this.fileNameWithSize = fileNameWithSize;
    }

    /**
     * Gets fileId.
     * @return the fileId
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * Sets the fileId.
     * @param fileId the fileId to set
     */
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    /**
     * Gets fileName.
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the fileName.
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

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
}
