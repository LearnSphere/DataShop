/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SchoolItem;

/**
 * School Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3638 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2007-01-23 11:18:30 -0500 (Tue, 23 Jan 2007) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SchoolDao extends AbstractDao {

    /**
     * Standard get for a SchoolItem by id.
     * @param id The id of the SchoolItem.
     * @return the matching SchoolItem or null if none found
     */
    SchoolItem get(Integer id);

    /**
     * Standard find for an SchoolItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SchoolItem.
     * @return the matching SchoolItem.
     */
    SchoolItem find(Integer id);

    /**
     * Standard "find all" for SchoolItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns SchoolItem given a name.
     * @param name name of the SchoolItem
     * @return a collection of SchoolItems
     */
    Collection find(String name);

    /**
     * Gets a list of schools in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match the Anon Id too.
     * @param datasetItem the dataset item to find schools in.
     * @param matchAny boolean value indicating whether to only look for schools that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching school items sorted by name.
     */
    List findMatchingByName(String toMatch, DatasetItem datasetItem, boolean matchAny);
}
