/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;

/**
 * Condition Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4905 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-06-04 17:10:39 -0400 (Wed, 04 Jun 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ConditionDao extends AbstractDao {

    /**
     * Standard get for a ConditionItem by id.
     * @param id The id of the ConditionItem.
     * @return the matching ConditionItem or null if none found
     */
    ConditionItem get(Long id);

    /**
     * Standard find for an ConditionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ConditionItem.
     * @return the matching ConditionItem.
     */
    ConditionItem find(Long id);

    /**
     * Standard "find all" for ConditionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns Condition(s) given a name.
     * @param name name of condition
     * @return Collection of conditionItem
     */
    Collection find(String name);

    /**
     * Gets a list of conditions in the dataset who's Name match all
     * or a portion of the toMatch parameter.
     * @param toMatch A string to match.
     * @param dataset the dataset item to search in.
     * @param matchAny boolean value indicating whether to only look for matches from
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching items sorted by Name.
     */
    List findMatchingByName(String toMatch, DatasetItem dataset, boolean matchAny);

    /**
     * Gets a list of conditions in the dataset who's Type match all
     * or a portion of the toMatch parameter.
     * @param toMatch A string to match.
     * @param dataset the dataset item to search in.
     * @param matchAny boolean value indicating whether to only look for matches from
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching items sorted by Type.
     */
    List findMatchingByType(String toMatch, DatasetItem dataset, boolean matchAny);

    /**
     * Gets a list of conditions in the dataset.
     * @param dataset the dataset item to search in.
     * @return List of all matching items.
     */
    List find(DatasetItem dataset);

    /**
     * Return a saved item.  If the new item is not in the list, then save it.
     * If it is, then find the existing item and return that.
     * Note that this method is overridden because it doesn't use the item's equals method.
     * Conditions must be unique by dataset and name.  Disregard type and description.
     * @param collection the collection to search
     * @param newItem the new item
     * @return an existing item
     */
    Item findOrCreate(Collection collection, Item newItem);
}
