/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ActionItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Action Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ActionDao extends AbstractDao {

    /**
     * Standard get for a ActionItem by id.
     * @param id The id of the ActionItem.
     * @return the matching ActionItem or null if none found
     */
    ActionItem get(Long id);

    /**
     * Standard find for an ActionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ActionItem.
     * @return the matching ActionItem.
     */
    ActionItem find(Long id);

    /**
     * Standard "find all" for ActionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Find all actions for all subgoals for a given problem.
     * @param problem the ProblemItem to get actions for.
     * @return list of actions for the problem.
     */
    List find(ProblemItem problem);

    /**
     * Find all actions for a given subgoal and name.
     * @param subgoalItem the SubgoalItem this action belongs to.
     * @param action action name.
     * @return list of actions.
     */
    List findBySubgoalAndName(SubgoalItem subgoalItem, String action);
}
