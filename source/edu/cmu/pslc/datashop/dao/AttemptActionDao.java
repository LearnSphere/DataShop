/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * AttemptAction Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4468 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-11 11:20:11 -0500 (Mon, 11 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AttemptActionDao extends AbstractDao {

    /**
     * Standard get for a AttemptActionItem by id.
     * @param id The id of the AttemptActionItem.
     * @return the matching AttemptActionItem or null if none found
     */
    AttemptActionItem get(Long id);

    /**
     * Standard find for an AttemptActionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired AttemptActionItem.
     * @return the matching AttemptActionItem.
     */
    AttemptActionItem find(Long id);

    /**
     * Standard "find all" for AttemptActionItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Find all attempt actions for a given subgoal attempt.
     * @param subgoalAttemptItem SubgoalAttemptItem to get actions for.
     * @return List of matching items.
     */
    List find(SubgoalAttemptItem subgoalAttemptItem);

}
