/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
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
public class WorkflowComponentInstanceDaoHibernate
    extends AbstractDaoHibernate implements WorkflowComponentInstanceDao {

    /**
     * Standard get for a WorkflowComponentInstanceItem by id.
     * @param id The id of the WorkflowComponentInstanceItem
     * @return the matching WorkflowComponentInstanceItem or null if none found
     */
    public WorkflowComponentInstanceItem get(Integer id) {
        return (WorkflowComponentInstanceItem)get(WorkflowComponentInstanceItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowComponentInstanceItems.
     * @return a List of WorkflowComponentInstanceItems
     */
    public List findAll() {
        return findAll(WorkflowComponentInstanceItem.class);
    }

    /**
     * Standard find for a WorkflowComponentInstanceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentInstanceItem.
     * @return the matching WorkflowComponentInstanceItem.
     */
    public WorkflowComponentInstanceItem find(Integer id) {
        return (WorkflowComponentInstanceItem)find(WorkflowComponentInstanceItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Returns all WorkflowComponentInstanceItems given the workflow and componentName.
     * @param workflowItem the workflow item
     * @param componentName the component name
     * @return the WorkflowComponentInstanceItem
     */
    @Override
    public WorkflowComponentInstanceItem findByWorkflowAndId(WorkflowItem workflowItem, String componentName) {
        if (workflowItem == null || componentName == null) {
            return null;
        }
        Object[] params = {workflowItem, componentName.toUpperCase()};
        String query = "from WorkflowComponentInstanceItem wfcii where"
                + " workflow = ? and upper(wfcii.componentName) = ?";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (WorkflowComponentInstanceItem) components.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the incomplete WorkflowComponentInstanceItem given the workflow and componentName.
     * @param workflowItem the workflow item
     * @param componentName the component name
     * @return the WorkflowComponentInstanceItem
     */
    public WorkflowComponentInstanceItem findIncompleteByWorkflowAndId(WorkflowItem workflowItem, String componentName) {
        if (workflowItem == null || componentName == null) {
            return null;
        }
        Object[] params = {workflowItem, componentName.toUpperCase()};
        String query = "from WorkflowComponentInstanceItem wfcii where"
                + " workflow = ? and upper(wfcii.componentName) = ? and state != 'completed'";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (WorkflowComponentInstanceItem) components.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the WorkflowComponentInstanceItems given the workflow.
     * @param workflowItem the workflow item
     * @return the WorkflowComponentInstanceItem
     */
    public List<WorkflowComponentInstanceItem> findByWorkflow(WorkflowItem workflowItem) {
        if (workflowItem == null) {
            return null;
        }

        Object[] params = {workflowItem};
        String query = "from WorkflowComponentInstanceItem wfcii where"
                + " workflow = ?";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (List<WorkflowComponentInstanceItem>) components;
        } else {
            return null;
        }
    }

    @Override
    public void delete(Item obj) {

        Transaction tx = null;
        Session session = null;

        try {
            session = getSession();
            tx = session.getTransaction();
            if (tx == null || !tx.isActive()) {
                tx = session.beginTransaction();
            }
            session.delete(obj);
            session.flush();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!tx.wasCommitted()) {
               tx.rollback();
            }
            if (session != null) {
                releaseSession(session);
            }
        }
    }


}
