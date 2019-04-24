/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DiscourseImportQueueMapDao;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapId;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

/**
 * Hibernate and Spring implementation of the DiscourseImportQueueMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseImportQueueMapDaoHibernate
        extends AbstractDaoHibernate implements DiscourseImportQueueMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public DiscourseImportQueueMapItem get(DiscourseImportQueueMapId id) {
        return (DiscourseImportQueueMapItem)get(DiscourseImportQueueMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public DiscourseImportQueueMapItem find(DiscourseImportQueueMapId id) {
        return (DiscourseImportQueueMapItem)find(DiscourseImportQueueMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(DiscourseImportQueueMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByDiscourse method. */
    private static final String FIND_BY_DISCOURSE_HQL
        = "FROM DiscourseImportQueueMapItem map WHERE discourse = ?";

    /**
     *  Return DiscourseImportQueueMapItem for specified Discourse.
     *  @param discourseItem the given discourse item
     *  @return a list of items
     */
    public DiscourseImportQueueMapItem findByDiscourse(DiscourseItem discourseItem) {
        Object[] params = { discourseItem };
        List<DiscourseImportQueueMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_DISCOURSE_HQL, params);

        DiscourseImportQueueMapItem result = null;

        // There should never be more than 1... return it.
        if (itemList.size() > 0) {
            result = itemList.get(0);
        }

        return result;
    }

    /** HQL query for the findByImportQueue method. */
    private static final String FIND_BY_IMPORT_QUEUE_HQL
        = "FROM DiscourseImportQueueMapItem map WHERE importQueue = ?";

    /**
     *  Return DiscourseImportQueueMapItem for specified ImportQueue.
     *  @param iqItem the given import queue item
     *  @return a list of items
     */
    public DiscourseImportQueueMapItem findByImportQueue(ImportQueueItem iqItem) {
        Object[] params = { iqItem };
        List<DiscourseImportQueueMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_IMPORT_QUEUE_HQL, params);

        DiscourseImportQueueMapItem result = null;

        // There should never be more than 1... return it.
        if (itemList.size() > 0) {
            result = itemList.get(0);
        }

        return result;
    }

    /**
     * Helper method to determine if a Discourse is already mapped to an ImportQueue item.
     * @param discourse the DiscourseItem
     * @return boolean
     */
    public Boolean isDiscourseAlreadyAttached(DiscourseItem discourse) {

        Boolean result = false;

        DiscourseImportQueueMapItem item = findByDiscourse(discourse);
        if (item != null) { result = true; }
        
        return result;
    }
}
