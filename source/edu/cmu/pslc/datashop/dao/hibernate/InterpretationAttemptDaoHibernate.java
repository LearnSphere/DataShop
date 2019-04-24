/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.InterpretationAttemptDao;
import edu.cmu.pslc.datashop.item.InterpretationAttemptId;
import edu.cmu.pslc.datashop.item.InterpretationAttemptItem;

/**
 * Hibernate and Spring implementation of the InterpretationAttemptDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InterpretationAttemptDaoHibernate
        extends AbstractDaoHibernate implements InterpretationAttemptDao {

    /**
     * Standard get for a InterpretationAttemptItem by id.
     * @param id The id of the user.
     * @return the matching InterpretationAttemptItem or null if none found
     */
    public InterpretationAttemptItem get(InterpretationAttemptId id) {
        return (InterpretationAttemptItem)get(InterpretationAttemptItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(InterpretationAttemptItem.class);
    }

    /**
     * Standard find for an InterpretationAttemptItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired InterpretationAttemptItem.
     * @return the matching InterpretationAttemptItem.
     */
    public InterpretationAttemptItem find(InterpretationAttemptId id) {
        return (InterpretationAttemptItem)find(InterpretationAttemptItem.class, id);
    }

}
