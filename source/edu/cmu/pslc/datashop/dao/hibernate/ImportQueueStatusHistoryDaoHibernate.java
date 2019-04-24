/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ImportQueueStatusHistoryDao;
import edu.cmu.pslc.datashop.item.ImportQueueStatusHistoryItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the ImportQueueStatusHistoryDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueStatusHistoryDaoHibernate
        extends AbstractDaoHibernate implements ImportQueueStatusHistoryDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ImportQueueStatusHistoryItem get(Integer id) {
        return (ImportQueueStatusHistoryItem)get(ImportQueueStatusHistoryItem.class, id);
    }
    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ImportQueueStatusHistoryItem find(Integer id) {
        return (ImportQueueStatusHistoryItem)find(ImportQueueStatusHistoryItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ImportQueueStatusHistoryItem.class);
    }

    //
    // Non-standard methods begin.
    //

}
