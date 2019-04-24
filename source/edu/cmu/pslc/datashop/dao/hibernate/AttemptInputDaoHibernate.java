/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.AttemptInputDao;
import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * Hibernate and Spring implementation of the AttemptInputDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12956 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-02-23 11:31:27 -0500 (Tue, 23 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AttemptInputDaoHibernate extends AbstractDaoHibernate implements AttemptInputDao {

    /**
     * Standard get for a AttemptInputItem by id.
     * @param id The id of the user.
     * @return the matching AttemptInputItem or null if none found
     */
    public AttemptInputItem get(Long id) {
        return (AttemptInputItem)get(AttemptInputItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(AttemptInputItem.class);
    }

    /**
     * Standard find for an AttemptInputItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired AttemptInputItem.
     * @return the matching AttemptInputItem.
     */
    public AttemptInputItem find(Long id) {
        return (AttemptInputItem)find(AttemptInputItem.class, id);
    }

    /** HQL Query to get all subgoal attempt inputs for a given subgoal. */
    private static final String FIND_BY_SUBGOAL_ATTEMPT =
          "select distinct aii from AttemptInputItem aii "
        + "where aii.subgoalAttempt.id = ?";

    /**
     * Find all attempt inputs for a given subgoal attempt.
     * @param subgoalAttemptItem SubgoalAttemptItem to get inputs for.
     * @return List of matching items.
     */
    public List find(SubgoalAttemptItem subgoalAttemptItem) {
        Session session = getSession();
        session.setFlushMode(FlushMode.COMMIT);
        releaseSession(session);
        return getHibernateTemplate().find(FIND_BY_SUBGOAL_ATTEMPT,
                (Long)subgoalAttemptItem.getId());
    }
}
