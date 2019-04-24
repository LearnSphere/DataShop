/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.CurriculumItem;

/**
 * Curriculum Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6479 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2010-12-02 16:37:19 -0500 (Thu, 02 Dec 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CurriculumDao extends AbstractDao {

    /**
     * Standard get for a CurriculumItem by id.
     * @param id The id of the CurriculumItem.
     * @return the matching CurriculumItem or null if none found
     */
    CurriculumItem get(Integer id);

    /**
     * Standard find for an CurriculumItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired CurriculumItem.
     * @return the matching CurriculumItem.
     */
    CurriculumItem find(Integer id);

    /**
     * Standard "find all" for CurriculumItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns CurriculumItem given a name.
     * @param name name of the CurriculumItem
     * @return a collection of CurriculumItems
     */
    Collection find(String name);

}
