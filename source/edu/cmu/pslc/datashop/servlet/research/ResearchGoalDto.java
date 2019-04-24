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
public class ResearchGoalDto extends DTO {

    //----- CONSTANTS -----

    /** Constant for request attribute. */
    public static final String ATTRIB_LIST = "research_goal_list";

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Class attribute. */
    private String title;
    /** Class attribute. */
    private String description;
    /** Class attribute. */
    private Integer order;
    /** Class attribute. */
    private Integer numberOfPapers;


    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ResearchGoalDto() { }

    /**
     * Constructor.
     * @param id database generated unique id for the item
     * @param title the title
     * @param description the description
     * @param order the order
     */
    public ResearchGoalDto(Integer id,
            String title,
            String description,
            Integer order) {
        this.id = id;
        setTitle(title);
        setDescription(description);
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
     * Gets title.
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
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
     * Gets numberOfPapers.
     * @return the numberOfPapers
     */
    public Integer getNumberOfPapers() {
        return numberOfPapers;
    }

    /**
     * Sets the numberOfPapers.
     * @param numberOfPapers the numberOfPapers to set
     */
    public void setNumberOfPapers(Integer numberOfPapers) {
        this.numberOfPapers = numberOfPapers;
    }

}
