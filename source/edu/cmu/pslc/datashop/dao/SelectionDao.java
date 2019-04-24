/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SelectionItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Selection Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6493 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2010-12-13 14:47:49 -0500 (Mon, 13 Dec 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SelectionDao extends AbstractDao {

    /**
     * Standard get for a selection item by selectionId.
     * @param selectionId the id of the desired selectionItem.
     * @return the matching SelectionItem or null if none found
     */
    SelectionItem get(Long selectionId);

    /**
     * Standard "find all" for selection items.
     * @return a List of objects
     */
    List findAll();

    /**
     * Standard find for a selection item by id.
     * Only guarantees the id of the item will be filled in.
     * @param selectionId the id of the desired selectionItem.
     * @return the matching SelectionItem.
     */
    SelectionItem find(Long selectionId);

    //
    // Non-standard methods begin.
    //

    /**
     * Find all selections for all subgoals for a given problem.
     * @param problem the ProblemItem to get selections for.
     * @return list of selections for the problem.
     */
    List find(ProblemItem problem);

    /**
     * Find all selections for a given subgoal and name.
     * @param subgoalItem the SubgoalItem this selection belongs to.
     * @param selection selection name.
     * @return list of selections.
     */
    List findBySubgoalAndName(SubgoalItem subgoalItem, String selection);
}
