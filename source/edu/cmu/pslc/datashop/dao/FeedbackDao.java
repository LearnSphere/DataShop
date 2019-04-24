/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.FeedbackItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * Feedback Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3297 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-08-31 14:50:37 -0400 (Thu, 31 Aug 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface FeedbackDao extends AbstractDao {

    /**
     * Standard get for a FeedbackItem by id.
     * @param id The id of the FeedbackItem.
     * @return the matching FeedbackItem or null if none found
     */
    FeedbackItem get(Long id);

    /**
     * Standard find for an FeedbackItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired FeedbackItem.
     * @return the matching FeedbackItem.
     */
    FeedbackItem find(Long id);

    /**
     * Standard "find all" for FeedbackItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Finds a list of all feedback items for a given problem.
     * @param problemItem the problem to get feedbacks for.
     * @return a List of items.
     */
    List find(ProblemItem problemItem);
}
