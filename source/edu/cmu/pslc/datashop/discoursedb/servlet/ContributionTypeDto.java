/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2015
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.discoursedb.servlet;

import java.util.List;
import java.util.Map;

/**
 * This is a POJO for a Discourse ContributionType.
 * For now, this is merely a type name and count.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContributionTypeDto implements java.io.Serializable {

    //----- ATTRIBUTES -----

    /** ContributionType type. */
    private String type;
    /** ContributionType count. */
    private Long count;

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public ContributionTypeDto() { }

    /**
     * Constructor.
     * @param type the ContributionType type
     * @param count the ContributionType count
     */
    public ContributionTypeDto(String type, Long count) {
        setType(type);
        setCount(count);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the ContributionType type.
     * @return the type
     */
    public String getType() { return type; }

    /**
     * Set the ContributionType type.
     * @param type
     */
    public void setType(String type) { this.type = type; }

    /**
     * Get the ContributionType count.
     * @return the count
     */
    public Long getCount() { return count; }

    /**
     * Set the ContributionType count.
     * @param count
     */
    public void setCount(Long count) { this.count = count; }
}
