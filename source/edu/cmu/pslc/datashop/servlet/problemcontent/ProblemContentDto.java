/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2014
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * This is a POJO for the project content subtab.
 *
 * @author Cindy Tipper
 * @version $Revision: 11006 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-05-12 11:45:40 -0400 (Mon, 12 May 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemContentDto {

    //----- ATTRIBUTES -----

    /** Message. */
    private String message;
    /** Message level. */
    private String messageLevel;
    /** List of content versions. */
    private List<PcConversionItem> contentVersions;
    /** List of mapped content. */
    private List<MappedContentDto> mappedContent;
    /** Number of problems in the dataset. */
    private Long numProblems;
    /** Number of problems in the dataset currently mapped. */
    private Long numProblemsMapped;

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public ProblemContentDto() { }

    /**
     * Constructor.
     * @param contentVersions list of PcConversionItems available
     * @param mappedContent list of PcConversionItems already mapped
     */
    public ProblemContentDto(List<PcConversionItem> contentVersions,
                             List<MappedContentDto> mappedContent) {
        setContentVersions(contentVersions);
        setMappedContent(mappedContent);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the list of content versions, as PcConversionItems.
     * @return the list
     */
    public List<PcConversionItem> getContentVersions() {
        return contentVersions;
    }

    /**
     * Set the list of content versions, as PcConversionItems.
     * @param contentVersions the list of content versions available
     */
    public void setContentVersions(List<PcConversionItem> contentVersions) {
        this.contentVersions = contentVersions;
    }

    /**
     * Get the list of mapped content versions, as PcConversionItems.
     * @return the list
     */
    public List<MappedContentDto> getMappedContent() {
        return mappedContent;
    }

    /**
     * Set the list of mapped content versions, as PcConversionItems.
     * @param mappedContent the list of mapped content versions
     */
    public void setMappedContent(List<MappedContentDto> mappedContent) {
        this.mappedContent = mappedContent;
    }

    /**
     * Get the number of problems in this dataset.
     * @return the number of problems
     */
    public Long getNumProblems() {
        return numProblems;
    }

    /**
     * Set the number of problems in this dataset.
     * @param numProblems the number of problems
     */
    public void setNumProblems(Long numProblems) {
        this.numProblems = numProblems;
    }

    /**
     * Get the number of problems mapped in this dataset.
     * @return the number of problems mapped
     */
    public Long getNumProblemsMapped() {
        return numProblemsMapped;
    }

    /**
     * Set the number of problems mapped in this dataset.
     * @param numProblemsMapped the number of problems mapped
     */
    public void setNumProblemsMapped(Long numProblemsMapped) {
        this.numProblemsMapped = numProblemsMapped;
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
}
