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
public class RgPaperWithGoalsDto extends DTO {

    //----- CONSTANTS -----

    /** Constant for request attribute. */
    public static final String ATTRIB_LIST = "rg_paper_goals_list";

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private RgPaperDto paperDto;

    /** Class attribute. */
    private Long goalCount;
    /** Class attribute. */
    private List<ResearchGoalDto> goalList;

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public RgPaperWithGoalsDto() { }

    //----- GETTERS and SETTERS -----

    /**
     * Gets paperDto.
     * @return the paperDto
     */
    public RgPaperDto getPaperDto() {
        return paperDto;
    }

    /**
     * Sets the paperDto.
     * @param paperDto the paperDto to set
     */
    public void setPaperDto(RgPaperDto paperDto) {
        this.paperDto = paperDto;
    }

    /**
     * Gets goalCount.
     * @return the goalCount
     */
    public Long getGoalCount() {
        return goalCount;
    }

    /**
     * Sets the goalCount.
     * @param goalCount the goalCount to set
     */
    public void setGoalCount(Long goalCount) {
        this.goalCount = goalCount;
    }

    /**
     * Gets goalList.
     * @return the goalList
     */
    public List<ResearchGoalDto> getGoalList() {
        return goalList;
    }

    /**
     * Sets the goalList.
     * @param goalList the goalList to set
     */
    public void setGoalList(List<ResearchGoalDto> goalList) {
        this.goalList = goalList;
    }
}
