/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * WorkflowComponentInstance Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowComponentInstanceDao extends AbstractDao {

    /**
     * Standard get for a WorkflowComponentInstanceItem by id.
     * @param id The id of the WorkflowComponentInstanceItem.
     * @return the matching WorkflowComponentInstanceItem or null if none found
     */
    WorkflowComponentInstanceItem get(Integer id);

    /**
     * Standard find for an WorkflowComponentInstanceItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentInstanceItem.
     * @return the matching WorkflowComponentInstanceItem.
     */
    WorkflowComponentInstanceItem find(Integer id);

    /**
     * Standard "find all" for WorkflowComponentInstanceItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Returns all WorkflowComponentInstanceItems given the workflow and componentName.
     * @param workflowItem the workflow item
     * @param componentName the component name
     * @return the WorkflowComponentInstanceItem
     */
    WorkflowComponentInstanceItem findByWorkflowAndId(WorkflowItem workflowItem, String componentName);

    /**
     * Returns the incomplete WorkflowComponentInstanceItem given the workflow and componentName.
     * @param workflowItem the workflow item
     * @param componentName the component name
     * @return the WorkflowComponentInstanceItem
     */
    WorkflowComponentInstanceItem findIncompleteByWorkflowAndId(WorkflowItem workflowItem, String componentName);

    /**
     * Returns the WorkflowComponentInstanceItems given the workflow.
     * @param workflowItem the workflow item
     * @return the WorkflowComponentInstanceItem
     */
    List<WorkflowComponentInstanceItem> findByWorkflow(WorkflowItem workflowItem);



}
