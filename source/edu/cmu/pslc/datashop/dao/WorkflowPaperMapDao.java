/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Paper Map Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowPaperMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    WorkflowPaperMapItem get(WorkflowPaperMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    WorkflowPaperMapItem find(WorkflowPaperMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List<WorkflowPaperMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of WorkflowPaperMapItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    List<WorkflowPaperMapItem> findByWorkflow(WorkflowItem workflowItem);

    /**
     *  Return a list of WorkflowPaperMapItems.
     *  @param irbItem the given IRB item
     *  @return a list of items
     */
    List<WorkflowPaperMapItem> findByWorkflowPaper(WorkflowPaperItem workflowPaperItem);

    /**
     *  Return a list of WorkflowPaperMapItems.
     *  @param workflowIds a list of workflow ids
     *  @return a list of workflow paper map items
     */
    List<WorkflowPaperMapItem> findByWorkflowIds(List<Long> workflowIds);

    /**
     * Return a list of workflow paper map items by criteria.
     * @param criteria the workflow paper  map item criteria
     * @param offset offset
     * @param max limit
     * @return a list of workflow paper map items by criteria
     */
    List<WorkflowPaperMapItem> findBy(WorkflowPaperMapItem criteria, int offset, int limit);

}
