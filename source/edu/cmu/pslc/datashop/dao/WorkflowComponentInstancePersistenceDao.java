/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstancePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * WorkflowComponentInstancePersistence Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 15351 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-07-17 07:42:05 -0400 (Tue, 17 Jul 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowComponentInstancePersistenceDao extends AbstractDao {

    /**
     * Standard get for a WorkflowComponentInstancePersistenceItem by id.
     * @param id The id of the WorkflowComponentInstancePersistenceItem.
     * @return the matching WorkflowComponentInstancePersistenceItem or null if none found
     */
    WorkflowComponentInstancePersistenceItem get(Integer id);

    /**
     * Standard find for an WorkflowComponentInstancePersistenceItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentInstancePersistenceItem.
     * @return the matching WorkflowComponentInstancePersistenceItem.
     */
    WorkflowComponentInstancePersistenceItem find(Integer id);

    /**
     * Standard "find all" for WorkflowComponentInstancePersistenceItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Returns all WorkflowComponentInstancePersistenceItems given the workflow and componentName.
     * @param workflowItem the workflow item
     * @param componentName the component name
     * @return the WorkflowComponentInstancePersistenceItem
     */
    WorkflowComponentInstancePersistenceItem findByWorkflowAndId(WorkflowItem workflowItem, String componentName);

    /**
     * Returns the incomplete WorkflowComponentInstanceItem given the workflow and componentName.
     * @param workflowItem the workflow item
     * @param componentName the component name
     * @return the WorkflowComponentInstanceItem
     */
    WorkflowComponentInstancePersistenceItem findIncompleteByWorkflowAndId(
            WorkflowItem workflowItem, String componentName);

    /**
     * Returns the WorkflowComponentInstancePersistenceItems given the workflow.
     * @param workflowItem the workflow item
     * @return the WorkflowComponentInstancePersistenceItem
     */
    List<WorkflowComponentInstancePersistenceItem> findByWorkflow(WorkflowItem workflowItem);

}
