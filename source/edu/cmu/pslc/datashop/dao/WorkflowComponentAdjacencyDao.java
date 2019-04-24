/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * WorkflowComponentAdjacency Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowComponentAdjacencyDao extends AbstractDao {

    /**
     * Standard get for a WorkflowComponentAdjacencyItem by id.
     * @param id The id of the WorkflowComponentAdjacencyItem.
     * @return the matching WorkflowComponentAdjacencyItem or null if none found
     */
    WorkflowComponentAdjacencyItem get(Long id);

    /**
     * Standard find for an WorkflowComponentAdjacencyItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentAdjacencyItem.
     * @return the matching WorkflowComponentAdjacencyItem.
     */
    WorkflowComponentAdjacencyItem find(Long id);

    /**
     * Standard "find all" for WorkflowComponentAdjacencyItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Returns the WorkflowComponentAdjacencyItem given the workflow and componentId.
     * @param workflowItem the workflow item
     * @param componentId the component id
     * @return the WorkflowComponentAdjacencyItem
     */
    List<WorkflowComponentAdjacencyItem> findByWorkflowAndId(WorkflowItem workflowItem, String componentId);

    /**
     * Returns the WorkflowComponentAdjacencyItem given the workflow and childId.
     * @param workflowItem the workflow item
     * @param componentId the child id
     * @return the WorkflowComponentAdjacencyItem
     */
    List<WorkflowComponentAdjacencyItem> findByWorkflowAndChild(WorkflowItem workflowItem, String childId);

    /**
     * Returns the WorkflowComponentAdjacencyItem List given the unique attributes of the row.
     * @param workflowItem the workflow item
     * @param componentId the component id
     * @param componentIndex the component output index
     * @param childId the child id
     * @param childIndex the child input index
     * @return
     */
    WorkflowComponentAdjacencyItem findUnique(WorkflowItem workflowItem, String componentId,
    		Integer componentIndex, String childId, Integer childIndex);

    /**
     * Returns the WorkflowComponentAdjacencyItems given the workflow.
     * @param workflowItem the workflow item
     * @return the WorkflowComponentAdjacencyItem
     */
    List<WorkflowComponentAdjacencyItem> findByWorkflow(WorkflowItem workflowItem);

}
