/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.AttemptActionDao;
import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * Hibernate and Spring implementation of the AttemptActionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 5744 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2009-09-22 11:28:56 -0400 (Tue, 22 Sep 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AttemptActionDaoHibernate extends AbstractDaoHibernate implements AttemptActionDao {

    /**
     * Standard get for a AttemptActionItem by id.
     * @param id The id of the user.
     * @return the matching AttemptActionItem or null if none found
     */
    public AttemptActionItem get(Long id) {
        return (AttemptActionItem)get(AttemptActionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(AttemptActionItem.class);
    }

    /**
     * Standard find for an AttemptActionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired AttemptActionItem.
     * @return the matching AttemptActionItem.
     */
    public AttemptActionItem find(Long id) {
        return (AttemptActionItem)find(AttemptActionItem.class, id);
    }

    /**
     * Find all attempt actions for a given subgoal attempt.
     * @param subgoalAttemptItem SubgoalAttemptItem to get actions for.
     * @return List of matching items.
     */
    public List find(SubgoalAttemptItem subgoalAttemptItem) {
        Session session = getSession();
        session.setFlushMode(FlushMode.COMMIT);
        releaseSession(session);
        return getHibernateTemplate().findByNamedQuery(
                "attemptAction.by.attempt", subgoalAttemptItem.getId());
    }
}
