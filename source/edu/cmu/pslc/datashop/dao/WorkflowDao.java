/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowRowDto;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowDao extends AbstractDao {

    /**
     * Standard get for a WorkflowItem by id.
     * @param id The id of the WorkflowItem.
     * @return the matching WorkflowItem or null if none found
     */
    WorkflowItem get(Long id);

    /**
     * Standard find for an WorkflowItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowItem.
     * @return the matching WorkflowItem.
     */
    WorkflowItem find(Long id);

    /**
     * Standard "find all" for WorkflowItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Find by name.
     * @param workflowName the name of the workflow
     * @return WorkflowItem null if doesn't exist
     */
    WorkflowItem findByName(String workflowName);

    /**
     * Find all WorkflowItems for a given dataset item.
     * @return a List of WorkflowItems
     */
    List find(DatasetItem datasetItem);

    /**
     * Gets workflows for a given owner.
     * @param loggedInUserItem the owner
     * @return workflows for a given owner
     */
    public List<WorkflowItem> findByOwner(UserItem loggedInUserItem);

    /**
     * Return a list of workflow items by criteria.
     * @param criteria the workflow item criteria
     * @param offset offset
     * @param max limit
     * @return a list of workflow items by criteria
     */
    public List<WorkflowItem> findBy(WorkflowItem criteria, int offset, int max);

    /**
     * Get list of workflow owners.
     * @return a list of workflow owners
     */
    public List<UserItem> findOwners();
    
	/**
     * Find the workflow row info for all workflows associated with an user
     * and NOT matching the 'searchBy' criteria, if specified.
     * @param userId the user id
     * @param datasetItem the optional dataset item
     * @param userMatch flag indicating if query is for owner's workflows or all
     *        shared workflows not matching the specified owner.
     * @return the WorkflowRowDto's associated with this dataset for regular users.
     */
    public List<WorkflowRowDto> getWorkflowRowInfo(UserItem owner,
                                                   DatasetItem datasetItem,
                                                   Boolean userMatch);
                                                   
    /**
     * Find the workflow row info by workflow id.
     * @param workflowId workflow id
     * @return the WorkflowRowDto associated with this workflow id.
     */
    public WorkflowRowDto getWorkflowRowInfo(int workflowId);

}
