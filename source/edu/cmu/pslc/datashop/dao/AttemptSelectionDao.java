/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * AttemptSelection Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4468 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-11 11:20:11 -0500 (Mon, 11 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AttemptSelectionDao extends AbstractDao {

    /**
     * Standard get for a AttemptSelectionItem by id.
     * @param id The id of the AttemptSelectionItem.
     * @return the matching AttemptSelectionItem or null if none found
     */
    AttemptSelectionItem get(Long id);

    /**
     * Standard find for an AttemptSelectionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired AttemptSelectionItem.
     * @return the matching AttemptSelectionItem.
     */
    AttemptSelectionItem find(Long id);

    /**
     * Standard "find all" for AttemptSelectionItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Find all attempt selections for a given subgoal attempt.
     * @param subgoalAttemptItem SubgoalAttemptItem to get selections for.
     * @return List of matching items.
    */
    List find(SubgoalAttemptItem subgoalAttemptItem);

}
