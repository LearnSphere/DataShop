/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.MetricByDomainReportDao;
import edu.cmu.pslc.datashop.item.MetricByDomainReportItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.MetricReportItem;

import org.hibernate.SQLQuery;

/**
 * Hibernate and Spring implementation of the MetricByDomainDao.
 *
 * @author Shanwen
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricByDomainReportDaoHibernate extends AbstractDaoHibernate
                                              implements MetricByDomainReportDao {

    /**
     * Standard get for a MetricByDomainReportItem by id.
     * @param id The id of the user.
     * @return the matching MetricByDomainReportItem or null if none found
     */
    public MetricByDomainReportItem get(Integer id) {
        return (MetricByDomainReportItem)get(MetricByDomainReportItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<MetricByDomainReportItem> findAll() {
        return findAll(MetricByDomainReportItem.class);
    }

    /**
     * Standard find for an MetricByDomainReportItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired MetricByDomainReportItem.
     * @return the matching MetricByDomainReportItem.
     */
    public MetricByDomainReportItem find(Integer id) {
        return (MetricByDomainReportItem)find(MetricByDomainReportItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns most recently MetricByDomainReportItem given a domain and metric.
     * @param domain the domain to search in
     * @param metricReport the metric to search in
     * @return a collection of MetricByDomainReportItems
     */
    public MetricByDomainReportItem findByDomainReport(
            DomainItem domain, MetricReportItem metricReport) {
        MetricByDomainReportItem item = new MetricByDomainReportItem();
        Integer domainId = (Integer)domain.getId();
        Integer metricReportId = (Integer)metricReport.getId();
        if ((domainId > 0) && (metricReportId > 0)) {
            Object[] params = {domainId, metricReportId};
            List list = getHibernateTemplate().find(
                    "FROM MetricByDomainReportItem WHERE domain.id = ?"
                    + " AND metricReport.id = ?", params);
            if (list.size() > 0) {
                item = (MetricByDomainReportItem) list.get(0);
                return item;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

    /**
     * Returns most recently MetricByDomainReportItem.
     * @param metricReport the metric to search in
     * @return a collection of MetricByDomainReportItems
     */
    public Collection<MetricByDomainReportItem> findByMetricReport(
            MetricReportItem metricReport) {
        return getHibernateTemplate().find(
                " FROM MetricByDomainReportItem WHERE metricReport.id = ?)",
                metricReport.getId());
    }

    /**
     * Query for findLatestRemoteByDomain.
     */
    private static final String FIND_LATEST_REMOTE_BY_DOMAIN_SQL =
        "SELECT mbdr.* FROM metric_by_domain_report mbdr WHERE metric_report_id IN ("
        + "SELECT mr1.metric_report_id FROM metric_report mr1 "
        + "INNER JOIN (SELECT metric_report_id, MAX(time) AS maxTime, "
        + "remote_instance_id FROM metric_report "
        + "WHERE remote_instance_id IS NOT NULL "
        + "GROUP BY remote_instance_id) mr2 "
        + "ON mr1.remote_instance_id = mr2.remote_instance_id "
        + "AND mr1.time = mr2.maxTime) AND mbdr.domain_id = :domainId";

    /**
     * Find the latest list of items, from a remote instance, by domain.
     * @param domain the domain item
     * @return a collection of MetricByDomainReportItems
     */
    public List<MetricByDomainReportItem> findLatestRemoteByDomain(final DomainItem domain)
    {
        StringBuffer sql = new StringBuffer(FIND_LATEST_REMOTE_BY_DOMAIN_SQL);

        return executeSQLQuery(sql.toString(), new PrepareQuery() {
                public void prepareQuery(SQLQuery query) {
                    query.addEntity("mbdr", MetricByDomainReportItem.class).
                        setInteger("domainId", (Integer)domain.getId());
                }
            });
    }
}
