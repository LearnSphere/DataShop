/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * CognitiveStep Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3297 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-08-31 14:50:37 -0400 (Thu, 31 Aug 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CognitiveStepDao extends AbstractDao {

    /**
     * Standard get for a CognitiveStepItem by id.
     * @param id The id of the CognitiveStepItem.
     * @return the matching CognitiveStepItem or null if none found
     */
    CognitiveStepItem get(Long id);

    /**
     * Standard find for an CognitiveStepItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired CognitiveStepItem.
     * @return the matching CognitiveStepItem.
     */
    CognitiveStepItem find(Long id);

    /**
     * Standard "find all" for CognitiveStepItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns CognitiveStepItem given a name.
     * @param stepInfo step info of the CognitiveStepItem
     * @return a collection of CognitiveStepItems
     */
    Collection findByStepInfo(String stepInfo);

    /**
     * Returns a list of cognitive steps given a problem and list of steps.
     * @param problemItem the problem
     * @param steps the list of steps
     * @return a collection of cognitive steps
     */
    List find(ProblemItem problemItem, Collection steps);

    /**
     * Returns a list of cognitive steps given a problem.
     * @param problemItem the problem
     * @return a collection of cognitive steps
     */
    List find(ProblemItem problemItem);
}
