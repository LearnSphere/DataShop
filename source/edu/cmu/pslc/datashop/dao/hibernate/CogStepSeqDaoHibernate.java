/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.CogStepSeqDao;
import edu.cmu.pslc.datashop.item.CogStepSeqItem;

/**
 * Hibernate and Spring implementation of the CogStepSeqDao.
 *
 * @author Hui Cheng
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CogStepSeqDaoHibernate extends AbstractDaoHibernate
        implements CogStepSeqDao {

    /**
     * Standard get for a CogStepSeqItem by id.
     * @param id The id of sequence.
     * @return the matching CognitiveStepItem or null if none found
     */
    public CogStepSeqItem get(Long id) {
        return (CogStepSeqItem)get(CogStepSeqItem.class, id);
    }

    /**
     * Standard "find all" for CogStepSequence items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(CogStepSeqItem.class);
    }

    /**
     * Standard find for an CogStepSeqItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired CogStepSeqItem.
     * @return the matching CogStepSeqItem.
     */
    public CogStepSeqItem find(Long id) {
        return (CogStepSeqItem)find(CogStepSeqItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

}

