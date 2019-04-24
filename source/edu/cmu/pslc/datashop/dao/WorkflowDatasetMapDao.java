/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.item.DatasetItem;

/**
 * Workflow/Dataset Map Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowDatasetMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    WorkflowDatasetMapItem get(WorkflowDatasetMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    WorkflowDatasetMapItem find(WorkflowDatasetMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List<WorkflowDatasetMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of WorkflowDatasetMapItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    List<WorkflowDatasetMapItem> findByWorkflow(WorkflowItem workflowItem);

    /**
     *  Return a list of WorkflowDatasetMapItems.
     *  @param datasetItem the given dataset item
     *  @return a list of items
     */
    List<WorkflowDatasetMapItem> findByDataset(DatasetItem datasetItem);

}
