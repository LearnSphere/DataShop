/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowAnnotationItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Annotation Data Access Object Interface.
 *
 * @author Peter Schaldenbrand
 * @version $Revision:  $
 * <BR>Last modified by: $Author: pls21 $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowAnnotationDao extends AbstractDao<WorkflowAnnotationItem> {

    /**
     * Standard get for a WorkflowAnnotationItem by id.
     * @param id The id of the WorkflowAnnotationItem.
     * @return the matching WorkflowAnnotationItem or null if none found
     */
    WorkflowAnnotationItem get(Long id);

    /**
     * Standard find for a WorkflowAnnotationItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowAnnotationItem.
     * @return the matching WorkflowAnnotationItem.
     */
    WorkflowAnnotationItem find(Long id);

    /**
     * Standard "find all" for WorkflowAnnotationItems.
     * @return a List of objects
     */
    List<WorkflowAnnotationItem> findAll();

    //
    // non-standard methods.
    //
    /**
     * Find all WorkflowAnnotationItems for a given workflow item.
     * @return a List of WorkflowAnnotationItems
     */
    List find(WorkflowItem workflowItem);
}
