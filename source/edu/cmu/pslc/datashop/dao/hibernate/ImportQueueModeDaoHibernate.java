/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ImportQueueModeDao;
import edu.cmu.pslc.datashop.item.ImportQueueModeItem;

/**
 * Hibernate and Spring implementation of the ImportQueueModeDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueModeDaoHibernate
        extends AbstractDaoHibernate implements ImportQueueModeDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ImportQueueModeItem get(Integer id) {
        return (ImportQueueModeItem)get(ImportQueueModeItem.class, id);
    }
    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ImportQueueModeItem find(Integer id) {
        return (ImportQueueModeItem)find(ImportQueueModeItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ImportQueueModeItem.class);
    }

    //
    // Non-standard methods begin.
    //

}
