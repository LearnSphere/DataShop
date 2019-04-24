/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.MetricByDomainReportItem;
import edu.cmu.pslc.datashop.item.DomainItem;

import edu.cmu.pslc.datashop.item.MetricReportItem;

/**
 * MetricByDomainDao Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MetricByDomainReportDao extends AbstractDao {

    /**
     * Standard get for a MetricByDomainReportItem by id.
     * @param id The id of the MetricByDomainReportItem.
     * @return the matching MetricByDomainReportItem or null if none found
     */
    MetricByDomainReportItem get(Integer id);

    /**
     * Standard find for an MetricByDomainReportItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired MetricByDomainReportItem.
     * @return the matching MetricByDomainReportItem.
     */
    MetricByDomainReportItem find(Integer id);

    /**
     * Standard "find all" for MetricByDomainReportItems.
     * @return a List of objects
     */
    List<MetricByDomainReportItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns MetricByDomainReportItem given domain and metricReport.
     * @param domain the domain item associated with this report
     * @param  metricReport the metric report item that associated with this report
     * @return a collection of MetricByDomainReportItems
     */
    MetricByDomainReportItem findByDomainReport(DomainItem domain,
            MetricReportItem metricReport);
    /**
     * Returns MetricByDomainReportItem.
     * @param metricReport the MetricReport item that associated with this report
     * @return a collection of MetricByDomainReportItems
     */
    Collection<MetricByDomainReportItem> findByMetricReport(MetricReportItem metricReport);

    /**
     * Find the latest list of items, from a remote instance, by domain.
     * @param domain the domain item
     * @return a collection of MetricByDomainReportItems
     */
    List<MetricByDomainReportItem> findLatestRemoteByDomain(DomainItem domain);
}
