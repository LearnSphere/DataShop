/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.CognitiveStepDao;
import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * Hibernate and Spring implementation of the CognitiveStepDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CognitiveStepDaoHibernate extends AbstractDaoHibernate implements CognitiveStepDao {

    /**
     * Standard get for a CognitiveStepItem by id.
     * @param id The id of the user.
     * @return the matching CognitiveStepItem or null if none found
     */
    public CognitiveStepItem get(Long id) {
        return (CognitiveStepItem)get(CognitiveStepItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(CognitiveStepItem.class);
    }

    /**
     * Standard find for an CognitiveStepItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired CognitiveStepItem.
     * @return the matching CognitiveStepItem.
     */
    public CognitiveStepItem find(Long id) {
        return (CognitiveStepItem)find(CognitiveStepItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns CognitiveStepItem given a name.
     * @param stepInfo step info of the CognitiveStepItem
     * @return a collection of CognitiveStepItems
     */
    public Collection findByStepInfo(String stepInfo) {
        return getHibernateTemplate().find(
                "from CognitiveStepItem step where step.stepInfo = ?", stepInfo);
    }

    /**
     * Returns a list of cognitive steps given a problem and list of steps.
     * @param problemItem the problem
     * @param steps the list of steps
     * @return a collection of cognitive steps
     */
    public List find(ProblemItem problemItem, Collection steps) {
        if (steps.size() == 0) {
            return new ArrayList();
        }
        DetachedCriteria query = DetachedCriteria.forClass(CognitiveStepItem.class);
        query.add(Restrictions.eq("problem", problemItem));
        Disjunction disjunction = Restrictions.disjunction();
        for (Iterator iter = steps.iterator(); iter.hasNext();) {
            CognitiveStepItem cogStep = (CognitiveStepItem)iter.next();
            disjunction.add(Restrictions.eq("stepInfo", cogStep.getStepInfo()));
        }
        query.add(disjunction);
        query.addOrder(Property.forName("stepInfo").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /** Query string for finding cog steps by problem */
    private static final String FIND_BY_PROBLEM = "select distinct cs from CognitiveStepItem cs"
        + " join cs.problem prob "
        + " where prob.id = ? ";

    /**
     * Returns a list of cognitive steps given a problem.
     * @param problemItem the problem
     * @return a collection of cognitive steps
     */
    public List find(ProblemItem problemItem) {
        return getHibernateTemplate().find(FIND_BY_PROBLEM, problemItem.getId());
    }
}
