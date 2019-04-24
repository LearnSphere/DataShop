/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.FilterDao;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * Hibernate and Spring implementation of the FilterDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FilterDaoHibernate extends AbstractDaoHibernate implements FilterDao {

    /**
     * Standard get for a FilterItem by id.
     * @param id The id of the user.
     * @return the matching FilterItem or null if none found
     */
    public FilterItem get(Integer id) {
        return (FilterItem)get(FilterItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(FilterItem.class);
    }

    /**
     * Standard find for an FilterItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired FilterItem.
     * @return the matching FilterItem.
     */
    public FilterItem find(Integer id) {
        return (FilterItem)find(FilterItem.class, id);
    }

    /**
     * Get all filters for a given sample.
     * @param sample the sample to get filters for.
     * @return a List of FilterItems
     */
    public List find(SampleItem sample) {
        return getHibernateTemplate().find(
                "from FilterItem filter where filter.sample.id = ?", sample.getId());
    }
}
