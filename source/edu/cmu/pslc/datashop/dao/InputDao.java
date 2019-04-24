/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.InputItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Input Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6493 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2010-12-13 14:47:49 -0500 (Mon, 13 Dec 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface InputDao extends AbstractDao {

    /**
     * Standard get for a InputItem by id.
     * @param id The id of the InputItem.
     * @return the matching InputItem or null if none found
     */
    InputItem get(Long id);

    /**
     * Standard find for an InputItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired InputItem.
     * @return the matching InputItem.
     */
    InputItem find(Long id);

    /**
     * Standard "find all" for InputItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Find all inputs for a given subgoal and name.
     * @param subgoalItem the SubgoalItem this input belongs to.
     * @param input input name.
     * @return list of inputs.
     */
    List findBySubgoalAndName(SubgoalItem subgoalItem, String input);
}
