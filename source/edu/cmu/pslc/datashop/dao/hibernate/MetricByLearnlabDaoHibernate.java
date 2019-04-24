/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.MetricByLearnlabDao;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.MetricByLearnlabItem;

/**
 * Hibernate and Spring implementation of the MetricByLearnlabDao.
 *
 * @author Shanwen
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricByLearnlabDaoHibernate extends AbstractDaoHibernate
                implements MetricByLearnlabDao {

    /**
     * Standard get for a MetricByLearnlabItem by id.
     * @param id The id of the user.
     * @return the matching MetricByLearnlabItem or null if none found
     */
    public MetricByLearnlabItem get(Integer id) {
        return (MetricByLearnlabItem)get(MetricByLearnlabItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<MetricByLearnlabItem> findAll() {
        return findAll(MetricByLearnlabItem.class);
    }

    /**
     * Standard find for an MetricByLearnlabItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired MetricByLearnlabItem.
     * @return the matching MetricByLearnlabItem.
     */
    public MetricByLearnlabItem find(Integer id) {
        return (MetricByLearnlabItem)find(MetricByLearnlabItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns  MetricByLearnlabItem given a learnlab and metric.
     * @param learnlab the learnlab to search in
     * @return MetricByLearnlabItem
     */
    public MetricByLearnlabItem findByLearnlab(
                                   LearnlabItem learnlab) {

        MetricByLearnlabItem metricByLearnlabItem = new MetricByLearnlabItem();
        List<MetricByLearnlabItem> itemList = getHibernateTemplate().find(
                "FROM MetricByLearnlabItem WHERE learnlab.id = ?", learnlab.getId());
        if (itemList != null && itemList.size() == 1) {
            metricByLearnlabItem = itemList.get(0);
            return metricByLearnlabItem;
        } else {
            return null;
        }
    }

}
