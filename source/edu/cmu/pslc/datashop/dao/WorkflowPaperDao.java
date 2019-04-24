/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Paper Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowPaperDao extends AbstractDao {

    /**
     * Standard get for a WorkflowPaperItem by id.
     * @param id The id of the WorkflowPaperItem.
     * @return the matching WorkflowPaperItem or null if none found
     */
    WorkflowPaperItem get(Integer id);

    /**
     * Standard find for an WorkflowPaperItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowPaperItem.
     * @return the matching WorkflowPaperItem.
     */
    WorkflowPaperItem find(Integer id);

    /**
     * Standard "find all" for WorkflowPaperItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Gets workflow papers for a given owner.
     * @param loggedInUserItem the owner
     * @return workflow papers for a given owner
     */
    public List<WorkflowPaperItem> findByOwner(UserItem loggedInUserItem);


    /**
     * Gets workflow papers for a given owner and paper name.
     * @param loggedInUserItem the owner
     * @param paperName the paper name
     * @return workflow papers for a given owner
     */
    public List<WorkflowPaperItem> findByUserAndName(UserItem loggedInUserItem, String paperName);

    /**
     * Gets workflow papers for a given a workflow.
     * @param workflowItem the workflow
     * @return workflow papers for a given a workflow
     */
    public List<WorkflowPaperItem> findByWorkflow(WorkflowItem workflowItem);


}
