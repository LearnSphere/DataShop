/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.MetricReportItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * Metric Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 13032 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-31 15:31:45 -0400 (Thu, 31 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MetricReportDao extends AbstractDao {

    /**
     * Standard get for a MetricReportItem by id.
     * @param id The id of the MetricReportItem.
     * @return the matching MetricReportItem or null if none found
     */
    MetricReportItem get(Integer id);

    /**
     * Standard find for an MetricReportItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired MetricReportItem.
     * @return the matching MetricReportItem.
     */
    MetricReportItem find(Integer id);

    /**
     * Standard "find all" for MetricReportItem.
     * @return a List of objects
     */
    List<MetricReportItem> findAll();
    //
    // Non-standard methods begin.
    //
    /**
     * Returns the most recent report time.
     * @return a date
     */
    Date getMostRecentTime();
    /**
     * Returns the MetricReportItem with max report id, for which remote_instance_id is null.
     * @return a MetricReportItem
     */
    MetricReportItem getMostRecentLocalReport();
    /**
     * Calls metrics_report stored procedure.
     */
    void callMetricsReportSP() throws SQLException;

    /**
     * Get the most recent MetricReport by remote instance.
     * @param remoteInstance the remote instance item
     */
    MetricReportItem getMostRecentByRemote(RemoteInstanceItem remoteInstance);
}
