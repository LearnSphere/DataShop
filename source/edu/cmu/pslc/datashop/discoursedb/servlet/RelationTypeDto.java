/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2015
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.discoursedb.servlet;

import java.util.List;
import java.util.Map;

/**
 * This is a POJO for a Discourse RelationType.
 * For now, this is merely a type name and count.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RelationTypeDto implements java.io.Serializable {

    //----- ATTRIBUTES -----

    /** Discourse RelationType type. */
    private String type;
    /** Discourse RelationType count. */
    private Long count;

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public RelationTypeDto() { }

    /**
     * Constructor.
     * @param type the DiscourseRelationType type
     * @param count the DiscourseRelationType count
     */
    public RelationTypeDto(String type, Long count) {
        setType(type);
        setCount(count);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the DiscourseRelationType type.
     * @return the type
     */
    public String getType() { return type; }

    /**
     * Set the DiscourseRelationType type.
     * @param type
     */
    public void setType(String type) { this.type = type; }

    /**
     * Get the DiscourseRelationType count.
     * @return the count
     */
    public Long getCount() { return count; }

    /**
     * Set the DiscourseRelationType count.
     * @param count
     */
    public void setCount(Long count) { this.count = count; }
}
