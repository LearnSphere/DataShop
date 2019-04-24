/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DatasetLevelSequenceDao;
import edu.cmu.pslc.datashop.item.DatasetLevelSequenceItem;

/**
 * Hibernate and Spring implementation of the DatasetLevelSequenceDao.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelSequenceDaoHibernate extends AbstractDaoHibernate implements
        DatasetLevelSequenceDao {

    /**
     * Standard find for an DatasetLevelSequenceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelSequenceItem.
     * @return the matching DatasetLevelSequenceItem.
     */
    public DatasetLevelSequenceItem find(Integer id) {
        return (DatasetLevelSequenceItem)find(DatasetLevelSequenceItem.class, id);
    }

    /**
     * Standard "find all" for DatasetLevelSequenceItem items.
     * @return a List of objects
     */
    public List<DatasetLevelSequenceItem> findAll() {
        return findAll(DatasetLevelSequenceItem.class);
    }

    /**
     * Standard get for a DatasetLevelSequenceItem by id.
     * @param id The id of the DatasetLevelSequenceItem.
     * @return the matching DatasetLevelSequenceItem or null if none found
     */
    public DatasetLevelSequenceItem get(Integer id) {
        return (DatasetLevelSequenceItem)get(DatasetLevelSequenceItem.class, id);
    }
} // end DatasetLevelSequenceSequenceDaoHibernate.java
