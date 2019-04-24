/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowHistoryItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;


/**
 * WorkflowHistory Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowHistoryDao extends AbstractDao {

    /**
     * Standard get for a WorkflowHistoryItem by id.
     * @param id The id of the WorkflowHistoryItem.
     * @return the matching WorkflowHistoryItem or null if none found
     */
    WorkflowHistoryItem get(Long id);

    /**
     * Standard find for an WorkflowHistoryItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowHistoryItem.
     * @return the matching WorkflowHistoryItem.
     */
    WorkflowHistoryItem find(Long id);

    /**
     * Standard "find all" for WorkflowHistoryItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //
    /**
     * Find all WorkflowHistoryItems for a given workflow item.
     * @return a List of WorkflowHistoryItems
     */
    List find(WorkflowItem workflowItem);
}
