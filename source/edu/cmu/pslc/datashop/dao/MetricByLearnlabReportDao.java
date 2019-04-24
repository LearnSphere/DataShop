/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.MetricByLearnlabReportItem;
import edu.cmu.pslc.datashop.item.MetricReportItem;

/**
 * MetricByLearnlab Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MetricByLearnlabReportDao extends AbstractDao {

    /**
     * Standard get for a MetricByLearnlabItem by id.
     * @param id The id of the MetricByLearnlabItem.
     * @return the matching MetricByLearnlabItem or null if none found
     */
    MetricByLearnlabReportItem get(Integer id);

    /**
     * Standard find for an MetricByLearnlabItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired MetricByLearnlabItem.
     * @return the matching MetricByLearnlabItem.
     */
    MetricByLearnlabReportItem find(Integer id);

    /**
     * Standard "find all" for MetricByLearnlabItems.
     * @return a List of objects
     */
    List<MetricByLearnlabReportItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns MetricByLearnlabItem given learnlab and metricReport.
     * @param metricReport the metricReport that associated with this report
     * @param learnlab the learnlab that associated with this report
     * @return a MetricByLearnlabItem
     */
    MetricByLearnlabReportItem findByLearnlabReport(
            LearnlabItem learnlab, MetricReportItem metricReport);
    /**
     * Returns MetricByLearnlabItem.
     * @param metricReport the MetricReport item that associated with this report
     * @return a collection of MetricByLearnlabItems
     */
    Collection<MetricByLearnlabReportItem> findByMetricReport(MetricReportItem metricReport);

    /**
     * Find the latest list of items, from a remote instance, by learnlab.
     * @param domain the LearnLab item
     * @return a collection of MetricByLearnlabReportItems
     */
    List<MetricByLearnlabReportItem> findLatestRemoteByLearnlab(LearnlabItem learnlab);
}
