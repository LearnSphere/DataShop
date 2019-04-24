/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DatasetLevelSequenceMapDao;
import edu.cmu.pslc.datashop.item.DatasetLevelSequenceMapId;
import edu.cmu.pslc.datashop.item.DatasetLevelSequenceMapItem;

/**
 * Hibernate and Spring implementation of the DatasetLevelSequenceMapDao.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelSequenceMapDaoHibernate extends AbstractDaoHibernate
    implements DatasetLevelSequenceMapDao {

    /**
     * Standard find for an DatasetLevelSequenceMapItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelSequenceMapItem.
     * @return the matching DatasetLevelSequenceMapItem.
     */
    public DatasetLevelSequenceMapItem find(DatasetLevelSequenceMapId id) {
        return (DatasetLevelSequenceMapItem)find(DatasetLevelSequenceMapItem.class, id);
    }

    /**
     * Standard "find all" for DatasetLevelSequenceMapItem items.
     * @return a List of objects
     */
    public List<DatasetLevelSequenceMapItem> findAll() {
        return findAll(DatasetLevelSequenceMapItem.class);
    }

    /**
     * Standard get for a DatasetLevelSequenceMapItem by id.
     * @param id The id of the DatasetLevelSequenceMapItem.
     * @return the matching DatasetLevelSequenceMapItem or null if none found
     */
    public DatasetLevelSequenceMapItem get(DatasetLevelSequenceMapId id) {
        return (DatasetLevelSequenceMapItem)get(DatasetLevelSequenceMapItem.class, id);
    }
} // end DatasetLevelSequenceMapDaoHibernate.java
