/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * SubgoalAttempt Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3297 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-08-31 14:50:37 -0400 (Thu, 31 Aug 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SubgoalAttemptDao extends AbstractDao {

    /**
     * Standard get for a SubgoalAttemptItem by id.
     * @param id The id of the SubgoalAttemptItem.
     * @return the matching SubgoalAttemptItem or null if none found
     */
    SubgoalAttemptItem get(Long id);

    /**
     * Standard find for an SubgoalAttemptItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SubgoalAttemptItem.
     * @return the matching SubgoalAttemptItem.
     */
    SubgoalAttemptItem find(Long id);

    /**
     * Standard "find all" for SubgoalAttemptItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Find all subgoals that match the item.
     * @param item SubgoalAttemptItem to get.
     * @return List of matching items.
     */
    List find(SubgoalAttemptItem item);

    /**
     * Find all subgoals attempts for all subgoals for a problem.
     * @param problemItem ProblemItem to get attempts for.
     * @return List of matching items.
     */
    List find(ProblemItem problemItem);
}
