/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ActionDao;
import edu.cmu.pslc.datashop.item.ActionItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Hibernate and Spring implementation of the ActionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7239 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-11-08 10:29:55 -0500 (Tue, 08 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ActionDaoHibernate extends AbstractDaoHibernate implements ActionDao {

    /**
     * Standard get for a ActionItem by id.
     * @param id The id of the user.
     * @return the matching ActionItem or null if none found
     */
    public ActionItem get(Long id) {
        return (ActionItem)get(ActionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(ActionItem.class);
    }

    /**
     * Standard find for an ActionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ActionItem.
     * @return the matching ActionItem.
     */
    public ActionItem find(Long id) {
        return (ActionItem)find(ActionItem.class, id);
    }

    /**
     * Find all actions for all subgoals for a given problem.
     * @param problem the ProblemItem to get actions for.
     * @return list of actions for the problem.
     */
    public List find(ProblemItem problem) {
        String query = "select distinct act from ActionItem act"
            + " join act.subgoal sub"
            + " join sub.problem prob"
            + " where prob.id = ?";
        return getHibernateTemplate().find(query, problem.getId());
    }

    /**
     * Find all actions for a given subgoal and name.
     * @param subgoalItem the SubgoalItem this action belongs to.
     * @param action action name
     * @return list of actions.
     */
    public List findBySubgoalAndName(SubgoalItem subgoalItem, String action) {
        String query = "select distinct act from ActionItem act"
            + " join act.subgoal sub"
            + " where sub.id = ? and action = ?";
        return getHibernateTemplate().find(
                query,  new Object[] {subgoalItem.getId(), action});
    }

}
