/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.IrbItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * IRB Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface IrbDao extends AbstractDao {

    /**
     * Standard get for an IRB item by id.
     * @param id the id of the desired IRB item
     * @return the matching IrbItem or null if none found
     */
    IrbItem get(Integer id);

    /**
     * Standard find for an IRB item by id.
     * @param id id of the object to find
     * @return IrbItem
     */
    IrbItem find(Integer id);

    /**
     * Standard "find all" for IRB items.
     * @return a List of objects
     */
    List<IrbItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Nonstandard "find all" for irb items sorted by title.
     * @return a List of objects which are IrbItems sorted by title
     */
    List<IrbItem> findAllSortByTitle();

    /**
     * Find IRBs given a project.
     * @param projectItem the project
     * @return a collection of IrbItems
     */
    Collection<IrbItem> findByProject(ProjectItem projectItem);

    /**
     * Find all matching search string.
     * By default, this will attempt to match on title or PI.
     * @param searchBy search string
     * @param titleOnly do not include PI in search
     * @return a List of objects
     */
    List<IrbItem> findAllMatching(String searchBy, boolean titleOnly);

    /**
     * Find all matching search string for the specified user.
     * By default, this will attempt to match on title or PI.
     * @param user the usern
     * @param searchBy search string
     * @param titleOnly do not include PI in search
     * @return a List of objects
     */
    List<IrbItem> findAllMatchingByUser(UserItem user, String searchBy, boolean titleOnly);
}
