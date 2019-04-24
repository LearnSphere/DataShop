/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * Student Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14294 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-29 09:42:01 -0400 (Fri, 29 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface StudentDao extends AbstractDao {

    /**
     * Standard get for a StudentItem by id.
     * @param id The id of the StudentItem.
     * @return the matching StudentItem or null if none found
     */
    StudentItem get(Long id);

    /**
     * Standard find for an StudentItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired StudentItem.
     * @return the matching StudentItem.
     */
    StudentItem find(Long id);

    /**
     * Standard "find all" for StudentItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns StudentItem given a name.
     * @param name name of the StudentItem
     * @return a collection of StudentItems
     */
    Collection find(String name);
    
    /**
     * Find all students in a dataset.
     * @param dataset the DatasetItem to get students for.
     * @return a Collection of all students in the dataset.
     */
    List find(DatasetItem dataset);

    /**
     * Find student with specified anon_user_id.
     *
     * @param anonUserId the string to match
     * @return the matching student item
     */
    List findByAnonId(String anonUserId);

    /**
     * Gets a list of students in the dataset that match all or a portion of the
     * AnonId parameter.
     * @param toMatch A string to match the Anon Id too.
     * @param dataset the dataset item to find students in.
     * @param matchAny boolean value indicating whether to only look for students that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching student items sorted by Anon Id.
     */
    List findMatchingByAnonId(String toMatch, DatasetItem dataset, boolean matchAny);

    /**
     * Returns a bunch of students where the anonymous user id is blank, which are
     * created by the FFI when anonFlag is true.
     * @return a collection of StudentItems
     */
    List<StudentItem> findByEmptyAnonUserId();
}
