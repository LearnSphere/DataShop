/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowUserFeedbackItem;

/**
 * WorkflowUserFeedback Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 13285 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-06-02 16:42:14 -0400 (Thu, 02 Jun 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowUserFeedbackDao extends AbstractDao {

    /**
     * Standard get for a WorkflowUserFeedbackItem by id.
     * @param id The id of the WorkflowUserFeedbackItem
     * @return the matching WorkflowUserFeedbackItem or null if none found
     */
    WorkflowUserFeedbackItem get(Long id);

    /**
     * Standard find for a WorkflowUserFeedbackItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowUserFeedbackItem.
     * @return the matching WorkflowUserFeedbackItem.
     */
    WorkflowUserFeedbackItem find(Long id);

    /**
     * Standard "find all" for WorkflowUserFeedbackItems.
     * @return a List of WorkflowUserFeedbackItems
     */
    List<WorkflowUserFeedbackItem> findAll();

    //
    // non-standard methods.
    //
    /**
     * Find all feedback items for a given dataset.
     * @param datasetItem the dataset
     * @return a list of feedback items
     */
    List<WorkflowUserFeedbackItem> find(DatasetItem datasetItem);

    /**
     * Find all feedback items for a given user.
     * @param userItem the user
     * @return a list of feedback items
     */
    List<WorkflowUserFeedbackItem> find(UserItem userItem);

    /**
     * Find all feedback items for a given workflow.
     * @param workflowItem the workflow
     * @return a list of feedback items
     */
    List<WorkflowUserFeedbackItem> find(WorkflowItem workflowItem);
}
