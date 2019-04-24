/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import org.json.JSONArray;

import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;

/**
 * WorkflowTag Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 15759 $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: 2018-12-12 14:37:38 -0500 (Wed, 12 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowTagDao extends AbstractDao<WorkflowTagItem> {

    /**
     * Standard get for a WorkflowTagItem by id.
     * @param id The id of the WorkflowTagItem.
     * @return the matching WorkflowTagItem or null if none found
     */
    WorkflowTagItem get(Long id);

    /**
     * Standard find for an WorkflowTagItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowTagItem.
     * @return the matching WorkflowTagItem.
     */
    WorkflowTagItem find(Long id);

    /**
     * Standard "find all" for WorkflowTagItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Returns a list of WorkflowTagItems for a given workflow.
     * @param workflow the WorkflowItem
     * @return list of WorkflowTagItem
     */
    List<WorkflowTagItem> findByWorkflow(WorkflowItem workflow);

    JSONArray getTagsByPopularity(int limit);
}
