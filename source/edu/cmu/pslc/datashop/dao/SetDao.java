/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.SetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * Set Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4520 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-02-28 16:39:28 -0500 (Thu, 28 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SetDao extends AbstractDao {

    /**
     * Standard get for a SetItem by id.
     * @param id The id of the SetItem.
     * @return the matching SetItem or null if none found
     */
    SetItem get(Integer id);

    /**
     * Standard find for an SetItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SetItem.
     * @return the matching SetItem.
     */
    SetItem find(Integer id);

    /**
     * Standard "find all" for SetItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of SetItems for the given skill model and set name, though
     * there should not be more than one.
     * @param skillModelItem the given skill model
     * @param setName the given set name
     * @return a SetItem if it is found, null otherwise
     */
    List find(SkillModelItem skillModelItem, String setName);

    /**
     * Finds a list of skill sets for the given skill model.
     * @param skillModelItem the given skill model
     * @return a list of SetItems
     */
    List findSkillSets(SkillModelItem skillModelItem);

}
