/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.SelectionDao;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SelectionItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Authorization Dao Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SelectionDaoHibernate extends AbstractDaoHibernate
     implements SelectionDao {

    /**
     * Hibernate/Spring get for a selection item by id.
     * @param id the id of the desired project item
     * @return the matching ProjectItem or null if none found
     */
    public SelectionItem get(Long id) {
        return (SelectionItem)get(SelectionItem.class, id);
    }
    /**
     * Hibernate/Spring find for a selection item by id.
     * @param id id of the object to find
     * @return ProjectItem
     */
    public SelectionItem find(Long id) {
        return (SelectionItem)find(SelectionItem.class, id);
    }

    /**
     * Hibernate/Spring "find all" for selection items.
     * @return a List of objects which are ProjectItems
     */
    public List findAll() {
        return getHibernateTemplate().find("from " + SelectionItem.class.getName());
    }

    /**
     * Find all selections for all subgoals for a given problem.
     * @param problem the ProblemItem to get selections for.
     * @return list of selections for the problem.
     */
    public List find(ProblemItem problem) {
        String query = "select distinct sel from SelectionItem sel"
            + " join sel.subgoal sub"
            + " join sub.problem prob"
            + " where prob.id = ?";
        return getHibernateTemplate().find(query, problem.getId());
    }

    /**
     * Find all selections for a given subgoal and name.
     * @param subgoalItem the SubgoalItem this selection belongs to.
     * @param selection selection name
     * @return list of selections.
     */
    public List findBySubgoalAndName(SubgoalItem subgoalItem, String selection) {
        String query = "select distinct sel from SelectionItem sel"
            + " join sel.subgoal sub"
            + " where sub.id = ? and selection = ?";
        return getHibernateTemplate().find(
                query,  new Object[] {subgoalItem.getId(), selection});
    }

}
