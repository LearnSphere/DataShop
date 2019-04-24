/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO Object for holding problem hierarchy information.
 * @author kcunning
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
  */

public class ProblemHierarchy extends DTO {

    /** The set of problem hierarchies (as HTML), keyed by problem_id (database identifier). */
    private Map<String, String> hierarchiesById;

    /**
     * Default Constructor!
     */
    public ProblemHierarchy() {
        this.hierarchiesById = new HashMap();
    }

    /**
     * Constructor.
     * @param hierarchies the map of hierarchies.
     */
    public ProblemHierarchy(Map hierarchies) {
        this.hierarchiesById = hierarchies;
    }

    /**
     * Get the problem hierarchy map.
     * @return the hierarchyAsHTML
     */
    public Map getHierarchiesById() {
        return hierarchiesById;
    }

    /**
     * Set the problem hierarchy map.
     * @param hierarchies the map of problem hierarchies.
     */
    public void setHierarchiesById(Map hierarchies) {
        this.hierarchiesById = hierarchies;
    }

} // end ProblemHierarchy.java
