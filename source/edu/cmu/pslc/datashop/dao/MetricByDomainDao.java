/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.MetricByDomainItem;
import edu.cmu.pslc.datashop.item.DomainItem;
/**
 * MetricByDomainDao Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 6101 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2010-05-21 15:42:54 -0400 (Fri, 21 May 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MetricByDomainDao extends AbstractDao {

    /**
     * Standard get for a MetricByDomainItem by id.
     * @param id The id of the MetricByDomainItem.
     * @return the matching MetricByDomainItem or null if none found
     */
    MetricByDomainItem get(Integer id);

    /**
     * Standard find for an MetricByDomainItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired MetricByDomainItem.
     * @return the matching MetricByDomainItem.
     */
    MetricByDomainItem find(Integer id);

    /**
     * Standard "find all" for MetricByDomainItems.
     * @return a List of objects
     */
    List<MetricByDomainItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns MetricByDomainItem given domain.
     * @param  domain the domain item that associated with this report
     * @return a MetricByDomainItem
     */
    MetricByDomainItem findByDomain(DomainItem domain);
}
