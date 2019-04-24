/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.util.List;

import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserRoleItem;

/**
 * Hold the data for each Problem Content Conversion displayed on
 * the Manage Problem Content page.
 *
 * @author Cindy Tipper
 * @version $Revision: 11126 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-05 16:25:50 -0400 (Thu, 05 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageProblemContentDto {

    //----- ATTRIBUTES -----

    /** Message. */
    private String message;
    /** Message level. */
    private String messageLevel;
    /** List of PC conversions. */
    private List<PcConversionDto> pcConversions;
    /** Flag for 'Conversion Tool' column. */
    private boolean displayConversionToolColumn = true;

    /** Constant for the mapped problem content. */
    public static final String PROBLEM_CONTENT_MAPPED = "Mapped";
    /** Constant for the un-mapped problem content. */
    public static final String PROBLEM_CONTENT_UNMAPPED = "Not Mapped";
    /** Constant for both mapped and un-mapped problem content. */
    public static final String PROBLEM_CONTENT_BOTH = "";

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ManageProblemContentDto() { }

    /**
     * Constructor.
     * @param pcConversions list of PcConversionDtos available
     */
    public ManageProblemContentDto(List<PcConversionDto> pcConversions) {
        setPcConversions(pcConversions);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the list of PC conversions, as PcConversionDtos.
     * @return the list
     */
    public List<PcConversionDto> getPcConversions() {
        return pcConversions;
    }

    /**
     * Set the list of PC conversions, as PcConversionDtos.
     * @param pcConversions the list of PC conversions matching the filter
     */
    public void setPcConversions(List<PcConversionDto> pcConversions) {
        this.pcConversions = pcConversions;
    }

    /**
     * Get the flag indicating whether or not the 'Conversion Tool'
     * column is to be displayed.
     * @return the flag
     */
    public boolean getDisplayConversionToolColumn() {
        return displayConversionToolColumn;
    }

    /**
     * Set the flag indicating whether or not the 'Conversion Tool'
     * column is to be displayed.
     * @param displayColumn the flag
     */
    public void setDisplayConversionToolColumn(boolean displayColumn) {
        this.displayConversionToolColumn = displayColumn;
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
