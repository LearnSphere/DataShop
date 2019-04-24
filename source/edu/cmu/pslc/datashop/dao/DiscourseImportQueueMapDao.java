/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DiscourseImportQueueMapDao;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapId;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

/**
 * DiscourseImportQueueMap Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscourseImportQueueMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    DiscourseImportQueueMapItem get(DiscourseImportQueueMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    DiscourseImportQueueMapItem find(DiscourseImportQueueMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return DiscourseImportQueueMapItem for specified Discourse.
     *  @param discourseItem the given discourse item
     *  @return a list of items
     */
    DiscourseImportQueueMapItem findByDiscourse(DiscourseItem discourseItem);

    /**
     *  Return DiscourseImportQueueMapItem for specified ImportQueue.
     *  @param iqItem the given import queue item
     *  @return a list of items
     */
    DiscourseImportQueueMapItem findByImportQueue(ImportQueueItem iqItem);

    /**
     * Helper method to determine if a Discourse is already mapped to an ImportQueue item.
     * @param discourse the DiscourseItem
     * @return boolean
     */
    Boolean isDiscourseAlreadyAttached(DiscourseItem discourse);
}
