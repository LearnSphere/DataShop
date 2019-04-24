/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.MetricByDomainDao;
import edu.cmu.pslc.datashop.item.MetricByDomainItem;
import edu.cmu.pslc.datashop.item.DomainItem;

/**
 * Hibernate and Spring implementation of the MetricByDomainDao.
 *
 * @author Shanwen
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricByDomainDaoHibernate extends AbstractDaoHibernate implements MetricByDomainDao {

    /**
     * Standard get for a MetricByDomainItem by id.
     * @param id The id of the user.
     * @return the matching MetricByDomainItem or null if none found
     */
    public MetricByDomainItem get(Integer id) {
        return (MetricByDomainItem)get(MetricByDomainItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<MetricByDomainItem> findAll() {
        return findAll(MetricByDomainItem.class);
    }

    /**
     * Standard find for an MetricByDomainItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired MetricByDomainItem.
     * @return the matching MetricByDomainItem.
     */
    public MetricByDomainItem find(Integer id) {
        return (MetricByDomainItem)find(MetricByDomainItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns MetricByDomainItem given a domain.
     * @param domain the domain to search in
     * @return a MetricByDomainItem
     */
    public MetricByDomainItem findByDomain(DomainItem domain) {
                MetricByDomainItem metricByDomainItem = new MetricByDomainItem();
                List<MetricByDomainItem> itemList = getHibernateTemplate().find(
                        "FROM MetricByDomainItem WHERE domain.id = ?", domain.getId());
                if (itemList != null && itemList.size() == 1) {
                    metricByDomainItem = itemList.get(0);
                    return metricByDomainItem;
                } else {
                    return null;
                }
    }
}
