/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.WorkflowHistoryDao;
import edu.cmu.pslc.datashop.workflows.WorkflowHistoryItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * SampleHistory Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowHistoryDaoHibernate
    extends AbstractDaoHibernate implements WorkflowHistoryDao {

    /**
     * Standard get for a WorkflowHistoryItem by id.
     * @param id The id of the WorkflowHistoryItem
     * @return the matching WorkflowHistoryItem or null if none found
     */
    public WorkflowHistoryItem get(Long id) {
        return (WorkflowHistoryItem)get(WorkflowHistoryItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowHistoryItems.
     * @return a List of WorkflowHistoryItems
     */
    public List findAll() {
        return findAll(WorkflowHistoryItem.class);
    }

    /**
     * Standard find for a WorkflowHistoryItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowHistoryItem.
     * @return the matching WorkflowHistoryItem.
     */
    public WorkflowHistoryItem find(Long id) {
        return (WorkflowHistoryItem)find(WorkflowHistoryItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Find all history items for a given workflow.
     * @param workflowItem the workflow
     * @return a list of history items
     */
    public List find(WorkflowItem workflowItem) {
        Object[] params = new Object[] { workflowItem };
        String query = "from WorkflowHistoryItem whi"
            + " where workflow = ?";
        return getHibernateTemplate().find(
                query, params);
    }
}
