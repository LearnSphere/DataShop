/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.FeedbackDao;
import edu.cmu.pslc.datashop.item.FeedbackItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * Hibernate and Spring implementation of the FeedbackDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FeedbackDaoHibernate extends AbstractDaoHibernate implements FeedbackDao {

    /**
     * Standard get for a FeedbackItem by id.
     * @param id The id of the user.
     * @return the matching FeedbackItem or null if none found
     */
    public FeedbackItem get(Long id) {
        return (FeedbackItem)get(FeedbackItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(FeedbackItem.class);
    }

    /**
     * Standard find for an FeedbackItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired FeedbackItem.
     * @return the matching FeedbackItem.
     */
    public FeedbackItem find(Long id) {
        return (FeedbackItem)find(FeedbackItem.class, id);
    }

    /**
     * Finds a list of all feedback items for a given problem.
     * @param problemItem the problem to get feedbacks for.
     * @return a List of items.
     */
    public List find(ProblemItem problemItem) {
        String query = "select distinct fee from FeedbackItem fee "
            + "join fee.subgoalAttempt attempt "
            + "join attempt.subgoal sub "
            + "where sub.problem.id = ?";
        return getHibernateTemplate().find(query, problemItem.getId());
    }
}
