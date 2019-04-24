/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Import Persistence Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ComponentFilePersistenceDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ComponentFilePersistenceItem get(Long id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ComponentFilePersistenceItem find(Long id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of ComponentFilePersistenceItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    List<ComponentFilePersistenceItem> findByWorkflow(WorkflowItem workflowItem);

    /**
     *  Return a list of ComponentFilePersistenceItems.
     *  @param workflowItem the given file item
     *  @return a list of items
     */
    List<ComponentFilePersistenceItem> findByFile(WorkflowFileItem fileItem);

    /**
     *  Return a list of ComponentFilePersistenceItems.
     *  @param workflowItem the given dataset item
     *  @return a list of items
     */
    List<ComponentFilePersistenceItem> findByDataset(DatasetItem datasetItem);

    /**
     * Return the ComponentFilePersistenceItems associated with the given workflow and componentId
     * @param workflowItem the workflow item
     * @param componentId the component ID
     * @return the ComponentFilePersistenceItems associated with the given workflow and componentId
     */
    List<ComponentFilePersistenceItem> findImportByComponent(WorkflowItem workflowItem, String componentId);

    /**
     *  Return a unique ComponentFilePersistenceItem.
     *  @param workflowItem the given workflow item
     *  @param wfFileItem the WorkflowFileItem
     *  @return the unique row
     */
    ComponentFilePersistenceItem findByWorkflowAndFile(WorkflowItem workflowItem, WorkflowFileItem wfFileItem);

    /**
     * Delete items associated with a given workflow.
     * @param workflowItem the workflow item
     * @return
     */
    void deleteByWorkflow(WorkflowItem workflowItem);


}
