/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.AttemptSelectionDao;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * Hibernate and Spring implementation of the AttemptSelectionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6363 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-14 09:52:39 -0400 (Thu, 14 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AttemptSelectionDaoHibernate
        extends AbstractDaoHibernate implements AttemptSelectionDao {

    /**
     * Standard get for a AttemptSelectionItem by id.
     * @param id The id of the user.
     * @return the matching AttemptSelectionItem or null if none found
     */
    public AttemptSelectionItem get(Long id) {
        return (AttemptSelectionItem)get(AttemptSelectionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(AttemptSelectionItem.class);
    }

    /**
     * Standard find for an AttemptSelectionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired AttemptSelectionItem.
     * @return the matching AttemptSelectionItem.
     */
    public AttemptSelectionItem find(Long id) {
        return (AttemptSelectionItem)find(AttemptSelectionItem.class, id);
    }

    /**
     * Find all attempt selections for a given subgoal attempt.
     * @param subgoalAttemptItem SubgoalAttemptItem to get selections for.
     * @return List of matching items.
     */
    public List find(SubgoalAttemptItem subgoalAttemptItem) {
        Session session = getSession();
        releaseSession(session);
        return getHibernateTemplate().findByNamedQuery(
                "attemptSelection.by.attempt", subgoalAttemptItem.getId());
    }
}
