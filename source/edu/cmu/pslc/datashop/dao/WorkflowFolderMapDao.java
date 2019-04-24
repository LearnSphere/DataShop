/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Folder Map Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowFolderMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    WorkflowFolderMapItem get(WorkflowFolderMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    WorkflowFolderMapItem find(WorkflowFolderMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List<WorkflowFolderMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of WorkflowFolderMapItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    List<WorkflowFolderMapItem> findByWorkflow(WorkflowItem workflowItem);

    /**
     *  Return a list of WorkflowFolderMapItems.
     *  @param irbItem the given IRB item
     *  @return a list of items
     */
    List<WorkflowFolderMapItem> findByWorkflowFolder(WorkflowFolderItem workflowFolderItem);

    /**
     *  Return a list of WorkflowFolderMapItems.
     *  @param workflowIds a list of workflow ids
     *  @return a list of workflow folder map items
     */
    List<WorkflowFolderMapItem> findByWorkflowIds(List<Long> workflowIds);

    /**
     * Return a list of workflow folder map items by criteria.
     * @param criteria the workflow folder  map item criteria
     * @param offset offset
     * @param max limit
     * @return a list of workflow folder map items by criteria
     */
    List<WorkflowFolderMapItem> findBy(WorkflowFolderMapItem criteria, int offset, int limit);

}
