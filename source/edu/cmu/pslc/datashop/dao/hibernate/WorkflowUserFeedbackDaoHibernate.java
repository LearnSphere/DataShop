/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.WorkflowUserFeedbackDao;
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
public class WorkflowUserFeedbackDaoHibernate
    extends AbstractDaoHibernate implements WorkflowUserFeedbackDao {

    /**
     * Standard get for a WorkflowUserFeedbackItem by id.
     * @param id The id of the WorkflowUserFeedbackItem
     * @return the matching WorkflowUserFeedbackItem or null if none found
     */
    public WorkflowUserFeedbackItem get(Long id) {
        return (WorkflowUserFeedbackItem)get(WorkflowUserFeedbackItem.class, id);
    }

    /**
     * Standard find for a WorkflowUserFeedbackItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowUserFeedbackItem.
     * @return the matching WorkflowUserFeedbackItem.
     */
    public WorkflowUserFeedbackItem find(Long id) {
        return (WorkflowUserFeedbackItem)find(WorkflowUserFeedbackItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowUserFeedbackItems.
     * @return a List of WorkflowUserFeedbackItems
     */
    public List<WorkflowUserFeedbackItem> findAll() {
        return findAll(WorkflowUserFeedbackItem.class);
    }

    //
    // non-standard methods.
    //

    /**
     * Find all feedback items for a given dataset.
     * @param datasetItem the dataset
     * @return a list of feedback items
     */
    public List<WorkflowUserFeedbackItem> find(DatasetItem datasetItem) {
        Object[] params = new Object[] { datasetItem };
        String query = "from WorkflowUserFeedbackItem wi where dataset = ?";
        return getHibernateTemplate().find(query, params);
    }

    /**
     * Find all feedback items for a given user.
     * @param userItem the user
     * @return a list of feedback items
     */
    public List<WorkflowUserFeedbackItem> find(UserItem userItem) {
        Object[] params = new Object[] { userItem };
        String query = "from WorkflowUserFeedbackItem wi where user = ?";
        return getHibernateTemplate().find(query, params);
    }

    /**
     * Find all feedback items for a given workflow.
     * @param workflowItem the workflow
     * @return a list of feedback items
     */
    public List<WorkflowUserFeedbackItem> find(WorkflowItem workflowItem) {
        Object[] params = new Object[] { workflowItem };
        String query = "from WorkflowUserFeedbackItem wi where workflow = ?";
        return getHibernateTemplate().find(query, params);
    }
}
