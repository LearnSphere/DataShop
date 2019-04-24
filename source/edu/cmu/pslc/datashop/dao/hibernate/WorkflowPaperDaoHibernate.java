/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.WorkflowPaperDao;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
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
public class WorkflowPaperDaoHibernate
    extends AbstractDaoHibernate implements WorkflowPaperDao {

    /**
     * Standard get for a WorkflowPaperItem by id.
     * @param id The id of the WorkflowPaperItem
     * @return the matching WorkflowPaperItem or null if none found
     */
    public WorkflowPaperItem get(Integer id) {
        return (WorkflowPaperItem)get(WorkflowPaperItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowPaperItems.
     * @return a List of WorkflowPaperItems
     */
    public List findAll() {
        return findAll(WorkflowPaperItem.class);
    }

    /**
     * Standard find for a WorkflowPaperItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowPaperItem.
     * @return the matching WorkflowPaperItem.
     */
    public WorkflowPaperItem find(Integer id) {
        return (WorkflowPaperItem)find(WorkflowPaperItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Gets workflow papers for a given owner.
     * @param loggedInUserItem the owner
     * @return workflow papers for a given owner
     */
    public List<WorkflowPaperItem> findByOwner(UserItem loggedInUserItem) {
        Object[] params = new Object[] { loggedInUserItem };
        String query = "from WorkflowPaperItem wpi"
            + " where owner = ?";
        return getHibernateTemplate().find(
                query, params);
    }

    /**
     * Gets workflow papers for a given owner and paper name.
     * @param loggedInUserItem the owner
     * @param paperName the paper name
     * @return workflow papers for a given owner
     */
    public List<WorkflowPaperItem> findByUserAndName(UserItem loggedInUserItem, String paperName) {
        Object[] params = new Object[] { loggedInUserItem, paperName };
        String query = "from WorkflowPaperItem wpi"
            + " where owner = ? and title = ?";
        return getHibernateTemplate().find(
                query, params);
    }


    /**
     * Gets workflow papers for a given a workflow.
     * @param workflowItem the workflow
     * @return workflow papers for a given a workflow
     */
    public List<WorkflowPaperItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = new Object[] { workflowItem };
        String query = "select wpi from WorkflowPaperItem wpi"
            + " join wpi.workflows wf "
            + " where wf = ?";
        return getHibernateTemplate().find(
                query, params);
    }


}
