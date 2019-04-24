/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.MetricByLearnlabReportDao;
import edu.cmu.pslc.datashop.item.MetricByLearnlabReportItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.MetricReportItem;

import org.hibernate.SQLQuery;

/**
 * Hibernate and Spring implementation of the MetricByLearnlabReportDao.
 *
 * @author Shanwen
 * @version $Revision: 13127 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-19 09:54:50 -0400 (Tue, 19 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricByLearnlabReportDaoHibernate extends AbstractDaoHibernate
                                            implements MetricByLearnlabReportDao {

    /**
     * Standard get for a MetricByLearnlabReportItem by id.
     * @param id The id of the user.
     * @return the matching MetricByLearnlabReportItem or null if none found
     */
    public MetricByLearnlabReportItem get(Integer id) {
        return (MetricByLearnlabReportItem)get(MetricByLearnlabReportItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<MetricByLearnlabReportItem> findAll() {
        return findAll(MetricByLearnlabReportItem.class);
    }

    /**
     * Standard find for an MetricByLearnlabReportItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired MetricByLearnlabReportItem.
     * @return the matching MetricByLearnlabReportItem.
     */
    public MetricByLearnlabReportItem find(Integer id) {
        return (MetricByLearnlabReportItem)find(MetricByLearnlabReportItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a MetricByLearnlabReportItem given a Learnlab and metric.
     * @param learnlab the Learnlab to search in
     * @param metricReport the metricReport to search in
     * @return a MetricByLearnlabReportItem
     */
    public MetricByLearnlabReportItem findByLearnlabReport(
                                        LearnlabItem learnlab,
                                        MetricReportItem metricReport) {
            MetricByLearnlabReportItem item = new MetricByLearnlabReportItem();
            Integer learnlabId = (Integer)learnlab.getId();

            Integer metricReportId = (Integer)metricReport.getId();
            if ((learnlabId > 0) && (metricReportId > 0)) {
                Object[] params = {learnlabId, metricReportId};
                List list =  getHibernateTemplate().find(
                        "FROM MetricByLearnlabReportItem WHERE learnlab.id = ?"
                        + " AND metricReport.id = ?", params);
                if (list.size() > 0) {
                    item = (MetricByLearnlabReportItem) list.get(0);
                    return item;
                } else {
                    return null;
                }
            } else {
                return null;
            }
    }

    /**
     * Returns most recently MetricByLearnlabReportItem.
     * @param metricReport the metric report item
     * @return a collection of MetricByLearnlabReportItems
     */
    public Collection<MetricByLearnlabReportItem> findByMetricReport(
                            MetricReportItem metricReport) {

           return getHibernateTemplate().find(
                   " FROM MetricByLearnlabReportItem WHERE metricReport.id = ?)",
                                   metricReport.getId());
    }

    /**
     * Query for findLatestRemoteByLearnlab.
     */
    private static final String FIND_LATEST_REMOTE_BY_LEARNLAB_SQL =
        "SELECT mblr.* FROM metric_by_learnlab_report mblr WHERE metric_report_id IN ("
        + "SELECT mr1.metric_report_id FROM metric_report mr1 "
        + "INNER JOIN (SELECT metric_report_id, MAX(time) AS maxTime, "
        + "remote_instance_id FROM metric_report "
        + "WHERE remote_instance_id IS NOT NULL "
        + "GROUP BY remote_instance_id) mr2 "
        + "ON mr1.remote_instance_id = mr2.remote_instance_id "
        + "AND mr1.time = mr2.maxTime) AND mblr.learnlab_id = :learnlabId";

    /**
     * Find the latest list of items, from a remote instance, by learnlab.
     * @param learnlab the LearnLab item
     * @return a collection of MetricByLearnlabReportItems
     */
    public List<MetricByLearnlabReportItem> findLatestRemoteByLearnlab(final LearnlabItem learnlab)
    {
        StringBuffer sql = new StringBuffer(FIND_LATEST_REMOTE_BY_LEARNLAB_SQL);

        return executeSQLQuery(sql.toString(), new PrepareQuery() {
                public void prepareQuery(SQLQuery query) {
                    query.addEntity("mblr", MetricByLearnlabReportItem.class).
                        setInteger("learnlabId", (Integer)learnlab.getId());
                }
            });
    }
}
