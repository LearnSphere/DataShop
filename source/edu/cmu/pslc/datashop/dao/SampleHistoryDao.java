/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.SampleHistoryItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * SampleHistory Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SampleHistoryDao extends AbstractDao {

    /**
     * Standard get for a SampleHistoryItem by id.
     * @param id The id of the SampleHistoryItem.
     * @return the matching SampleHistoryItem or null if none found
     */
    SampleHistoryItem get(Long id);

    /**
     * Standard find for an SampleHistoryItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SampleHistoryItem.
     * @return the matching SampleHistoryItem.
     */
    SampleHistoryItem find(Long id);

    /**
     * Standard "find all" for SampleHistoryItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //
    /**
     * Find all SampleHistoryItems for a given sample item.
     * @return a List of SampleHistoryItems
     */
    List find(SampleItem sampleItem);
}
