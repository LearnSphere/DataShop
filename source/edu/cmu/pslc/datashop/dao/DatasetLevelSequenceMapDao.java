/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetLevelSequenceMapId;
import edu.cmu.pslc.datashop.item.DatasetLevelSequenceMapItem;

/**
 * Dataset Level Sequence Map Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetLevelSequenceMapDao extends AbstractDao {

    /**
     * Standard get for a DatasetLevelSequenceMapItem by id.
     * @param id The id of the DatasetLevelSequenceMapItem.
     * @return the matching DatasetLevelSequenceMapItem or null if none found
     */
    DatasetLevelSequenceMapItem get(DatasetLevelSequenceMapId id);

    /**
     * Standard find for an DatasetLevelSequenceMapItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelSequenceMapItem.
     * @return the matching DatasetLevelSequenceMapItem.
     */
    DatasetLevelSequenceMapItem find(DatasetLevelSequenceMapId id);

    /**
     * Standard "find all" for DatasetLevelSequenceMapItem.
     * @return a List of objects
     */
    List findAll();
} // end DatasetLevelSequenceMapDao.java
