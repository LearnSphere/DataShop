/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.MetricReportDao;
import edu.cmu.pslc.datashop.item.MetricReportItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * Hibernate and Spring implementation of the MetricReportDao.
 *
 * @author Shanwen
 * @version $Revision: 13032 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-31 15:31:45 -0400 (Thu, 31 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricReportDaoHibernate extends AbstractDaoHibernate
        implements MetricReportDao {

    /**
     * Standard get for a MetricReportItem by id.
     * @param id The id of the user.
     * @return the matching MetricReportItem or null if none found
     */
    public MetricReportItem get(Integer id) {
        return (MetricReportItem)get(MetricReportItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<MetricReportItem> findAll() {
        return findAll(MetricReportItem.class);
    }

    /**
     * Standard find for an MetricReportItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired MetricReportItem.
     * @return the matching MetricReportItem.
     */
    public MetricReportItem find(Integer id) {
        return (MetricReportItem)find(MetricReportItem.class, id);
    }
    //
    // Non-standard methods begin.
    //
    /**
     * Returns the most recent report time.
     * @return a date
     */
    public Date getMostRecentTime() {
        Date date = new Date();
        List dates = getHibernateTemplate().find("select max(time) from MetricReportItem");
        if (dates != null && dates.size() == 1) {
            date = (Date)dates.get(0);
        } else {
            date = null;
        }
        return date;
    }

    /** Query string for most recent local report. */
    private static final String MOST_RECENT_LOCAL_REPORT_QUERY =
        "SELECT MAX(id) FROM MetricReportItem WHERE remote_instance_id IS NULL";

    /**
     * Returns the MetricReportItem with max report id, for which remote_instance_id is null.
     * @return a MetricReportItem
     */
    public MetricReportItem getMostRecentLocalReport() {
        Integer id = 0;
        MetricReportItem metricReport = new MetricReportItem();
        List ids = getHibernateTemplate().find(MOST_RECENT_LOCAL_REPORT_QUERY);
        if (ids != null && ids.size() == 1) {
            id = (Integer)ids.get(0);
            metricReport = this.find(id);
        } else {
            metricReport = null;
        }
        return metricReport;
    }
    /**
     * Calls metrics_report_sp stored procedure to populate data into these five tables:
     * metric_report, metric_by_domain, metric_by_domain_report, metric_by_learnlab,
     * and metric_by_learnlab_report.
     * @throws SQLException SQLException
     */
    public void callMetricsReportSP() throws SQLException {
        String call = buildSPCall("generateMetricsReport");
        callSP(call);
    }

    /**
     * Query
     */
    private static final String MOST_RECENT_BY_REMOTE_HQL =
        "SELECT MAX(mr.id) FROM MetricReportItem AS mr WHERE remoteInstance.id = ?";

    /**
     * Get the most recent MetricReport by remote instance.
     * @param remoteInstance the remote instance item
     */
    public MetricReportItem getMostRecentByRemote(RemoteInstanceItem remoteInstance) {
        MetricReportItem metricReport = null;
        List<Integer> ids =
            getHibernateTemplate().find(MOST_RECENT_BY_REMOTE_HQL, remoteInstance.getId());
        if (ids != null && ids.size() == 1) {
            Integer id = (Integer)ids.get(0);
            if (id != null) { metricReport = this.find(id); }
        }
        return metricReport;
    }
}
