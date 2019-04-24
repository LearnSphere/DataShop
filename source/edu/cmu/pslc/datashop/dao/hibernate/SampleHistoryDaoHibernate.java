/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.SampleHistoryDao;
import edu.cmu.pslc.datashop.item.SampleHistoryItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * SampleHistory Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleHistoryDaoHibernate
    extends AbstractDaoHibernate implements SampleHistoryDao {

    /**
     * Standard get for a SampleHistoryItem by id.
     * @param id The id of the SampleHistoryItem
     * @return the matching SampleHistoryItem or null if none found
     */
    public SampleHistoryItem get(Long id) {
        return (SampleHistoryItem)get(SampleHistoryItem.class, id);
    }

    /**
     * Standard "find all" for SampleHistoryItems.
     * @return a List of SampleHistoryItems
     */
    public List findAll() {
        return findAll(SampleHistoryItem.class);
    }

    /**
     * Standard find for a SampleHistoryItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SampleHistoryItem.
     * @return the matching SampleHistoryItem.
     */
    public SampleHistoryItem find(Long id) {
        return (SampleHistoryItem)find(SampleHistoryItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Find all history items for a given sample.
     * @param sampleItem the sample
     * @return a list of history items
     */
    public List find(SampleItem sampleItem) {
        Object[] params = new Object[] { sampleItem };
        String query = "from SampleHistoryItem shi"
            + " where sample = ?";
        return getHibernateTemplate().find(
                query, params);
    }
}
