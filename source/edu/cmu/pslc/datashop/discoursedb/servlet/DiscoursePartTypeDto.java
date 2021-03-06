/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2015
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.discoursedb.servlet;

import java.util.List;
import java.util.Map;

/**
 * This is a POJO for a DiscoursePartType.
 * For now, this is merely a part name and type.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscoursePartTypeDto implements java.io.Serializable {

    //----- ATTRIBUTES -----

    /** DiscoursePartType type. */
    private String type;
    /** DiscoursePartType count. */
    private Long count;

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public DiscoursePartTypeDto() { }

    /**
     * Constructor.
     * @param type the DiscoursePartType type
     * @param count the DiscoursePartType count
     */
    public DiscoursePartTypeDto(String type, Long count) {
        setType(type);
        setCount(count);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the DiscoursePartType type.
     * @return the type
     */
    public String getType() { return type; }

    /**
     * Set the DiscoursePartType type.
     * @param type
     */
    public void setType(String type) { this.type = type; }

    /**
     * Get the DiscoursePartType count.
     * @return the count
     */
    public Long getCount() { return count; }

    /**
     * Set the DiscoursePartType count.
     * @param count
     */
    public void setCount(Long count) { this.count = count; }
}
