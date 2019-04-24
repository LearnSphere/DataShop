/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;


import java.util.List;

import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.MetricByLearnlabItem;


/**
 * MetricByLearnlab Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 6101 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2010-05-21 15:42:54 -0400 (Fri, 21 May 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MetricByLearnlabDao extends AbstractDao {

    /**
     * Standard get for a MetricByLearnlabItem by id.
     * @param id The id of the MetricByLearnlabItem.
     * @return the matching MetricByLearnlabItem or null if none found
     */
    MetricByLearnlabItem get(Integer id);

    /**
     * Standard find for an MetricByLearnlabItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired MetricByLearnlabItem.
     * @return the matching MetricByLearnlabItem.
     */
    MetricByLearnlabItem find(Integer id);

    /**
     * Standard "find all" for MetricByLearnlabItems.
     * @return a List of objects
     */
    List<MetricByLearnlabItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a MetricByLearnlabItem given learnlab.
     * @param learnlab the learnlab that associated with this report
     * @return a MetricByLearnlabItems
     */
    MetricByLearnlabItem findByLearnlab(LearnlabItem learnlab);
}
