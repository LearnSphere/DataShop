/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Import Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ComponentFileDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ComponentFileItem get(Long id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ComponentFileItem find(Long id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of ComponentFileItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    List<ComponentFileItem> findByWorkflow(WorkflowItem workflowItem);

    /**
     *  Return a list of ComponentFileItems.
     *  @param workflowItem the given file item
     *  @return a list of items
     */
    List<ComponentFileItem> findByFile(WorkflowFileItem fileItem);

    /**
     *  Return a list of ComponentFileItems.
     *  @param workflowItem the given dataset item
     *  @return a list of items
     */
    List<ComponentFileItem> findByDataset(DatasetItem datasetItem);

    /**
     * Return the ComponentFileItems associated with the given workflow and componentId
     * @param workflowItem the workflow item
     * @param componentId the component ID
     * @return the ComponentFileItems associated with the given workflow and componentId
     */
    List<ComponentFileItem> findImportByComponent(WorkflowItem workflowItem, String componentId);

    /**
     *  Return a unique ComponentFilePersistenceItem.
     *  @param workflowItem the given workflow item
     *  @param wfFileItem the WorkflowFileItem
     *  @return the unique row
     */
    ComponentFileItem findByWorkflowAndFile(WorkflowItem workflowItem, WorkflowFileItem wfFileItem);


}
