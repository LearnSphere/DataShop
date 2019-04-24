/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetLevelEventItem;

/**
 * DatasetLevelEvent Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3992 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2007-04-18 14:06:11 -0400 (Wed, 18 Apr 2007) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetLevelEventDao extends AbstractDao {

    /**
     * Standard get for a DatasetLevelEventItem by id.
     * @param id The id of the DatasetLevelEventItem.
     * @return the matching DatasetLevelEventItem or null if none found
     */
    DatasetLevelEventItem get(Long id);

    /**
     * Standard find for an DatasetLevelEventItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelEventItem.
     * @return the matching DatasetLevelEventItem.
     */
    DatasetLevelEventItem find(Long id);

    /**
     * Standard "find all" for DatasetLevelEventItems.
     * @return a List of objects
     */
    List findAll();

}
