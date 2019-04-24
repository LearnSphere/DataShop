/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.SubgoalAttemptDao;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * Hibernate and Spring implementation of the SubgoalAttemptDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SubgoalAttemptDaoHibernate extends AbstractDaoHibernate implements SubgoalAttemptDao {

    /**
     * Standard get for a SubgoalAttemptItem by id.
     * @param id The id of the user.
     * @return the matching SubgoalAttemptItem or null if none found
     */
    public SubgoalAttemptItem get(Long id) {
        return (SubgoalAttemptItem)get(SubgoalAttemptItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(SubgoalAttemptItem.class);
    }

    /**
     * Standard find for an SubgoalAttemptItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SubgoalAttemptItem.
     * @return the matching SubgoalAttemptItem.
     */
    public SubgoalAttemptItem find(Long id) {
        return (SubgoalAttemptItem)find(SubgoalAttemptItem.class, id);
    }

    /**
     * Find all subgoals that match the item.
     * @param item SubgoalAttemptItem to get.
     * @return List of matching items.
     */
    public List find(SubgoalAttemptItem item) {

        Object[] params = new Object[2];
        params[0] = item.getSubgoal().getId();
        params[1] = item.getCorrectFlag();

        String query = "select distinct sai from SubgoalAttemptItem sai"
            + " join sai.subgoal sub"
            + " where sub.id = ?"
            + " and sai.correctFlag = ?";
        return getHibernateTemplate().find(query, params);
    }

    /**
     * Find all subgoals attempts for all subgoals for a problem.
     * @param problemItem ProblemItem to get attempts for.
     * @return List of matching items.
     */
    public List find(ProblemItem problemItem) {
        String query = "select distinct sai from SubgoalAttemptItem sai"
            + " join sai.subgoal sub"
            + " join sub.problem prob"
            + " where prob.id = ?";
        return getHibernateTemplate().find(query, problemItem.getId());
    }
}
