/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.TransactionSkillEventItem;
import edu.cmu.pslc.datashop.item.TransactionSkillEventId;

/**
 * Transaction Skill Event Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface TransactionSkillEventDao extends AbstractDao {

    /**
     * Standard get for a TransactionSkillEventItem by id.
     * @param id the transaction skill event id for this TransactionSkillEvent item.
     * @return the matching TransactionSkillEventItem or null if none found
     */
    TransactionSkillEventItem get(TransactionSkillEventId id);

    /**
     * Standard find for an TransactionSkillEventItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the transaction skill event id for this TransactionSkillEvent item.
     * @return the matching TransactionSkillEventItem.
     */
    TransactionSkillEventItem find(TransactionSkillEventId id);

    /**
     * Standard "find all" for TransactionSkillEventItem.
     * @return a List of objects
     */
    List findAll();
} // end TransactionSkillEventDao.java
