/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.TransactionSkillEventDao;
import edu.cmu.pslc.datashop.item.TransactionSkillEventItem;
import edu.cmu.pslc.datashop.item.TransactionSkillEventId;

/**
 * Hibernate and Spring implementation of the TransactionSkillEventDao.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TransactionSkillEventDaoHibernate extends AbstractDaoHibernate
    implements TransactionSkillEventDao {

    /**
     * Standard find for an TransactionSkillEventItem.
     * Only the id of the item will be filled in.
     * @param id the TransactionSkillEventId for this item.
     * @return the matching TransactionSkillEventItem.
     */
    public TransactionSkillEventItem find(TransactionSkillEventId id) {
        return (TransactionSkillEventItem)find(TransactionSkillEventItem.class, id);
    }

    /**
     * Standard "find all" for TransactionSkillEventItem items.
     * @return a List of objects
     */
    public List<TransactionSkillEventItem> findAll() {
        return findAll(TransactionSkillEventItem.class);
    }

    /**
     * Standard get for a TransactionSkillEventItem.
     * @param id the TransactionSkillEventId for this item.
     * @return the matching TransactionSkillEventItem or null if none found
     */
    public TransactionSkillEventItem get(TransactionSkillEventId id) {
        return (TransactionSkillEventItem)get(TransactionSkillEventItem.class, id);
    }

} // end TransactionSkillEventDaoHibernate.java
