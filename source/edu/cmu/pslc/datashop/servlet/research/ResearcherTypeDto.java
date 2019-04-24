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
 * @version $Revision: 12463 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-07-02 13:28:54 -0400 (Thu, 02 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearcherTypeDto extends DTO {

    //----- CONSTANTS -----

    /** Constant for request attribute. */
    public static final String ATTRIB_ID    = "researcher_type_id";
    /** Constant for request attribute. */
    public static final String ATTRIB_LABEL = "researcher_type_label";
    /** Constant for request attribute. */
    public static final String ATTRIB_LIST  = "researcher_type_list";

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Class attribute. */
    private String label;
    /** Class attribute. */
    private Integer order;
    /** Class attribute. */
    private Integer parentId;
    /** Class attribute. */
    private Integer numberOfGoals;
    /** Class attribute. */
    private List<ResearchGoalDto> goalList;
    /** Class attribute. */
    private List<ResearcherTypeDto> subTypeList;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param id database generated unique id for the item
     * @param label the label
     * @param parentId the parent id
     * @param order the order
     */
    public ResearcherTypeDto(Integer id, String label, Integer parentId, Integer order) {
        this.id = id;
        setLabel(label);
        setParentId(parentId);
        setOrder(order);
    }

    //----- GETTERS and SETTERS -----

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
     * Gets label.
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets order.
     * @return the order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the order.
     * @param order the order to set
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * Get the parent id.
     * @return the parentId
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * Set the parent id.
     * @param parentId the parent id
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * Gets numberOfGoals.
     * @return the numberOfGoals
     */
    public Integer getNumberOfGoals() {
        return numberOfGoals;
    }

    /**
     * Sets the numberOfGoals.
     * @param numberOfGoals the numberOfGoals to set
     */
    public void setNumberOfGoals(Integer numberOfGoals) {
        this.numberOfGoals = numberOfGoals;
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

    /**
     * Get subTypeList.
     * @return the subTypeList
     */
    public List<ResearcherTypeDto> getSubTypeList() {
        return subTypeList;
    }

    /**
     * Sets the subType list.
     * @param subTypeList the subType list to set
     */
    public void setSubTypeList(List<ResearcherTypeDto> subTypeList) {
        this.subTypeList = subTypeList;
    }

}
