/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dao.hibernate.WorkflowFolderDaoHibernate.QUERY_CONDITION;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowContext;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Folder Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowFolderDao extends AbstractDao {

    /**
     * Standard get for a WorkflowFolderItem by id.
     * @param id The id of the WorkflowFolderItem.
     * @return the matching WorkflowFolderItem or null if none found
     */
    WorkflowFolderItem get(Long id);

    /**
     * Standard find for an WorkflowFolderItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowFolderItem.
     * @return the matching WorkflowFolderItem.
     */
    WorkflowFolderItem find(Long id);

    /**
     * Standard "find all" for WorkflowFolderItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Gets workflow folders for a given owner.
     * @param loggedInUserItem the owner
     * @return workflow folders for a given owner
     */
    public List<WorkflowFolderItem> findByOwner(UserItem loggedInUserItem);


    /**
     * Gets workflow folders for a given owner and folder name.
     * @param loggedInUserItem the owner
     * @param folderName the folder name
     * @return workflow folders for a given owner
     */
    public List<WorkflowFolderItem> findByUserAndName(UserItem loggedInUserItem, String folderName);

    /**
     * Gets workflow folders for a given a workflow.
     * @param workflowItem the workflow
     * @return workflow folders for a given a workflow
     */
    public List<WorkflowFolderItem> findByWorkflow(WorkflowItem workflowItem);

    /**
     * Gets my empty workflow folders.
     * @return my empty workflow folders
     */
    public List<WorkflowFolderItem> findMyEmptyFolders(UserItem owner);

    /**
     * Return a list of workflow folder items by criteria.
     * @param criteria the workflow context containing the search criteria
     * @param panelId the workflow panel
     * @param queryCondition either GET_COUNT or GET_WORKFLOWS
     * @return a list of workflow folder items by criteria
     */
    public List<Object[]> queryWorkflows(UserItem loggedInUserItem,
            WorkflowContext criteria,
            String panelId,
            QUERY_CONDITION queryCondition);

}
