/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.ClassItem;

/**
 * Class Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 1927 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2005-11-30 15:30:36 -0500 (Wed, 30 Nov 2005) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ClassDao extends AbstractDao {

    /**
     * Standard get for a ClassItem by id.
     * @param id The id of the ClassItem.
     * @return the matching ClassItem or null if none found
     */
    ClassItem get(Long id);

    /**
     * Standard find for an ClassItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ClassItem.
     * @return the matching ClassItem.
     */
    ClassItem find(Long id);

    /**
     * Standard "find all" for ClassItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns ClassItem given a name.
     * @param name name of the ClassItem
     * @return a collection of ClassItems
     */
    Collection find(String name);
}
