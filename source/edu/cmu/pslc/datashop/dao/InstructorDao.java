/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.InstructorItem;

/**
 * Instructor Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 1927 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2005-11-30 15:30:36 -0500 (Wed, 30 Nov 2005) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface InstructorDao extends AbstractDao {

    /**
     * Standard get for a InstructorItem by id.
     * @param id The id of the InstructorItem.
     * @return the matching InstructorItem or null if none found
     */
    InstructorItem get(Long id);

    /**
     * Standard find for an InstructorItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired InstructorItem.
     * @return the matching InstructorItem.
     */
    InstructorItem find(Long id);

    /**
     * Standard "find all" for InstructorItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns InstructorItem given a name.
     * @param name name of the InstructorItem
     * @return a collection of InstructorItems
     */
    Collection find(String name);
}
