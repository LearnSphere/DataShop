/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.mapping.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.AbstractDao;

import edu.cmu.pslc.datashop.mapping.item.StudentItem;

/**
 * Student Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14382 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-10-17 12:46:02 -0400 (Tue, 17 Oct 2017) $
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
     * Returns StudentItem given a name, case insensitive.
     * @param name name of the StudentItem
     * @return a collection of StudentItems
     */
    Collection findIgnoreCase(String name);

    /**
     * Standard find for an StudentItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired StudentItem.
     * @return the matching StudentItem.
     */
    StudentItem findByOriginalId(Long id);
}
