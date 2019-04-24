/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPersistenceItem;

/**
 * Workflow Persistence Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowPersistenceDao extends AbstractDao {

    /**
     * Standard get for a WorkflowItem by id.
     * @param id The id of the WorkflowItem.
     * @return the matching WorkflowItem or null if none found
     */
    WorkflowPersistenceItem get(Long id);

    /**
     * Standard find for an WorkflowItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowItem.
     * @return the matching WorkflowItem.
     */
    WorkflowPersistenceItem find(Long id);

    /**
     * Standard "find all" for WorkflowItems.
     * @return a List of objects
     */
    List<WorkflowPersistenceItem> findAll();

    //
    // non-standard methods.
    //

    /**
     * Find by name.
     * @param workflowItem the workflow
     * @return the WorkflowPersistenceItem or null if it doesn't exist
     */
    WorkflowPersistenceItem findByWorkflow(WorkflowItem workflowItem);

}
