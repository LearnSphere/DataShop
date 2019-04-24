/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapItem;

/**
 * Workflow/Tag Map Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 15470 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-09-12 11:19:05 -0400 (Wed, 12 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowTagMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    WorkflowTagMapItem get(WorkflowTagMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    WorkflowTagMapItem find(WorkflowTagMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List<WorkflowTagMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of WorkflowTagMapItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    List<WorkflowTagMapItem> findByWorkflow(WorkflowItem workflowItem);

    /**
     *  Return a list of WorkflowTagMapItems.
     *  @param tagItem the given workflow tag item
     *  @return a list of items
     */
    List<WorkflowTagMapItem> findByTag(WorkflowTagItem tagItem);

}
