/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DatasetLevelEventDao;
import edu.cmu.pslc.datashop.item.DatasetLevelEventItem;

/**
 * Hibernate and Spring implementation of the DatasetLevelEventDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelEventDaoHibernate
    extends AbstractDaoHibernate implements DatasetLevelEventDao {

    /**
     * Standard get for a DatasetLevelEventItem by id.
     * @param id The id of the user.
     * @return the matching DatasetLevelEventItem or null if none found
     */
    public DatasetLevelEventItem get(Long id) {
        return (DatasetLevelEventItem)get(DatasetLevelEventItem.class, id);
    }

    /**
     * Standard "find all" for dataset level event items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(DatasetLevelEventItem.class);
    }

    /**
     * Standard find for an DatasetLevelEventItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelEventItem.
     * @return the matching DatasetLevelEventItem.
     */
    public DatasetLevelEventItem find(Long id) {
        return (DatasetLevelEventItem)find(DatasetLevelEventItem.class, id);
    }

}
