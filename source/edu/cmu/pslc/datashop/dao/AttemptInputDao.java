/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * AttemptInput Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4468 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-11 11:20:11 -0500 (Mon, 11 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AttemptInputDao extends AbstractDao {

    /**
     * Standard get for a AttemptInputItem by id.
     * @param id The id of the AttemptInputItem.
     * @return the matching AttemptInputItem or null if none found
     */
    AttemptInputItem get(Long id);

    /**
     * Standard find for an AttemptInputItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired AttemptInputItem.
     * @return the matching AttemptInputItem.
     */
    AttemptInputItem find(Long id);

    /**
     * Standard "find all" for AttemptInputItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Find all attempt inputs for a given subgoal attempt.
     * @param subgoalAttemptItem SubgoalAttemptItem to get inputs for.
     * @return List of matching items.
     */
    List find(SubgoalAttemptItem subgoalAttemptItem);

}
