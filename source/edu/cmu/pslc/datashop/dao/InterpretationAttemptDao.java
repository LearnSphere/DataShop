/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.InterpretationAttemptId;
import edu.cmu.pslc.datashop.item.InterpretationAttemptItem;

/**
 * Interpretation Attempt Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2960 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-06-19 16:21:21 -0400 (Mon, 19 Jun 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface InterpretationAttemptDao extends AbstractDao {

    /**
     * Standard get for a InterpretationAttemptItem by id.
     * @param id The id of the InterpretationAttemptItem.
     * @return the matching InterpretationAttemptItem or null if none found
     */
    InterpretationAttemptItem get(InterpretationAttemptId id);

    /**
     * Standard find for an InterpretationAttemptItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired InterpretationAttemptItem.
     * @return the matching InterpretationAttemptItem.
     */
    InterpretationAttemptItem find(InterpretationAttemptId id);

    /**
     * Standard "find all" for InterpretationAttemptItems.
     * @return a List of objects
     */
    List findAll();

}
