/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.cmu.pslc.datashop.dao.WorkflowComponentInstancePersistenceDao;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstancePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Workflow Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 15351 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-07-17 07:42:05 -0400 (Tue, 17 Jul 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentInstancePersistenceDaoHibernate
    extends AbstractDaoHibernate implements WorkflowComponentInstancePersistenceDao {

    /**
     * Standard get for a WorkflowComponentInstancePersistenceItem by id.
     * @param id The id of the WorkflowComponentInstancePersistenceItem
     * @return the matching WorkflowComponentInstancePersistenceItem or null if none found
     */
    public WorkflowComponentInstancePersistenceItem get(Integer id) {
        return (WorkflowComponentInstancePersistenceItem)get(WorkflowComponentInstancePersistenceItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowComponentInstancePersistenceItems.
     * @return a List of WorkflowComponentInstancePersistenceItems
     */
    public List findAll() {
        return findAll(WorkflowComponentInstancePersistenceItem.class);
    }

    /**
     * Standard find for a WorkflowComponentInstancePersistenceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentInstancePersistenceItem.
     * @return the matching WorkflowComponentInstancePersistenceItem.
     */
    public WorkflowComponentInstancePersistenceItem find(Integer id) {
        return (WorkflowComponentInstancePersistenceItem)find(WorkflowComponentInstancePersistenceItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Returns the WorkflowComponentInstancePersistenceItem given the workflow and componentName.
     * @param workflowItem the workflow item
     * @param componentName the component name
     * @return the WorkflowComponentInstancePersistenceItem
     */
    @Override
    public WorkflowComponentInstancePersistenceItem findByWorkflowAndId(WorkflowItem workflowItem, String componentName) {
        if (workflowItem == null || componentName == null) {
            return null;
        }
        Object[] params = {workflowItem, componentName.toUpperCase()};
        String query = "from WorkflowComponentInstancePersistenceItem wfcii where"
                + " workflow = ? and upper(wfcii.componentName) = ?";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (WorkflowComponentInstancePersistenceItem) components.get(0);
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
    public WorkflowComponentInstancePersistenceItem findIncompleteByWorkflowAndId(
            WorkflowItem workflowItem, String componentName) {
        if (workflowItem == null || componentName == null) {
            return null;
        }
        Object[] params = {workflowItem, componentName.toUpperCase()};
        String query = "from WorkflowComponentInstancePersistenceItem wfcii where"
                + " workflow = ? and upper(wfcii.componentName) = ? and state != 'completed'";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (WorkflowComponentInstancePersistenceItem) components.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns the WorkflowComponentInstancePersistenceItems given the workflow.
     * @param workflowItem the workflow item
     * @return the WorkflowComponentInstancePersistenceItem
     */
    public List<WorkflowComponentInstancePersistenceItem> findByWorkflow(WorkflowItem workflowItem) {
        if (workflowItem == null) {
            return null;
        }

        Object[] params = {workflowItem};
        String query = "from WorkflowComponentInstancePersistenceItem wfcii where"
                + " workflow = ?";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (List<WorkflowComponentInstancePersistenceItem>) components;
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
