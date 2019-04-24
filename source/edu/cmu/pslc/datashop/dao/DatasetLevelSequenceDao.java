/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetLevelSequenceItem;

/**
 * Dataset Level Sequence Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetLevelSequenceDao extends AbstractDao {

    /**
     * Standard get for a DatasetLevelSequenceItem by id.
     * @param id The id of the DatasetLevelSequenceItem.
     * @return the matching DatasetLevelSequenceItem or null if none found
     */
    DatasetLevelSequenceItem get(Integer id);

    /**
     * Standard find for an DatasetLevelSequenceItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelSequenceItem.
     * @return the matching DatasetLevelSequenceItem.
     */
    DatasetLevelSequenceItem find(Integer id);

    /**
     * Standard "find all" for DatasetLevelSequenceItem.
     * @return a List of objects
     */
    List findAll();

} // end DatasetLevelSequenceDao.java
