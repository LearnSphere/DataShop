/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
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
public class WorkflowComponentAdjacencyDaoHibernate
    extends AbstractDaoHibernate implements WorkflowComponentAdjacencyDao {

    /**
     * Standard get for a WorkflowComponentAdjacencyItem by id.
     * @param id The id of the WorkflowComponentAdjacencyItem
     * @return the matching WorkflowComponentAdjacencyItem or null if none found
     */
    public WorkflowComponentAdjacencyItem get(Long id) {
        return (WorkflowComponentAdjacencyItem)get(WorkflowComponentAdjacencyItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowComponentAdjacencyItems.
     * @return a List of WorkflowComponentAdjacencyItems
     */
    public List findAll() {
        return findAll(WorkflowComponentAdjacencyItem.class);
    }

    /**
     * Standard find for a WorkflowComponentAdjacencyItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentAdjacencyItem.
     * @return the matching WorkflowComponentAdjacencyItem.
     */
    public WorkflowComponentAdjacencyItem find(Long id) {
        return (WorkflowComponentAdjacencyItem)find(WorkflowComponentAdjacencyItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Returns the WorkflowComponentAdjacencyItem given the workflow and componentId.
     * @param workflowItem the workflow item
     * @param componentId the component id
     * @return the WorkflowComponentAdjacencyItem
     */
    @Override
    public List<WorkflowComponentAdjacencyItem> findByWorkflowAndId(WorkflowItem workflowItem, String componentId) {
        if (workflowItem == null || componentId == null) {
            return null;
        }
        Object[] params = {workflowItem, componentId.toUpperCase()};
        String query = "from WorkflowComponentAdjacencyItem wfcai where"
                + " workflow = ? and upper(wfcai.componentId) = ?";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (List<WorkflowComponentAdjacencyItem>) components;
        } else {
            return null;
        }
    }

    /**
     * Returns the WorkflowComponentAdjacencyItem given the workflow and childId.
     * @param workflowItem the workflow item
     * @param childId the child id
     * @return the WorkflowComponentAdjacencyItem
     */
    @Override
    public List<WorkflowComponentAdjacencyItem> findByWorkflowAndChild(WorkflowItem workflowItem, String childId) {
        if (workflowItem == null || childId == null) {
            return null;
        }
        Object[] params = {workflowItem, childId.toUpperCase()};
        String query = "from WorkflowComponentAdjacencyItem wfcai where"
                + " workflow = ? and upper(wfcai.childId) = ?";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (List<WorkflowComponentAdjacencyItem>) components;
        } else {
            return null;
        }
    }


    /**
     * Returns the WorkflowComponentAdjacencyItems given the workflow.
     * @param workflowItem the workflow item
     * @return the WorkflowComponentAdjacencyItem
     */
    public List<WorkflowComponentAdjacencyItem> findByWorkflow(WorkflowItem workflowItem) {
        if (workflowItem == null) {
            return null;
        }

        Object[] params = {workflowItem};
        String query = "from WorkflowComponentAdjacencyItem wfcai where"
                + " workflow = ?";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (List<WorkflowComponentAdjacencyItem>) components;
        } else {
            return null;
        }
    }

    /**
     * Returns the WorkflowComponentAdjacencyItem List given the unique attributes of the row.
     * @param workflowItem the workflow item
     * @param componentId the component id
     * @param componentIndex the component output index
     * @param childId the child id
     * @param childIndex the child input index
     * @return
     */
    public WorkflowComponentAdjacencyItem findUnique(WorkflowItem workflowItem, String componentId,
		Integer componentIndex, String childId, Integer childIndex) {
    	if (workflowItem == null || componentId == null) {
            return null;
        }
    	List components = null;

    	if (childId == null) {
    		Object[] params = {workflowItem,
    	    		componentId.toUpperCase() };
    	        String query = "from WorkflowComponentAdjacencyItem wfcai where"
    	                + " workflow = ? and upper(wfcai.componentId) = ? and wfcai.componentIndex is null"
    	        		+ " and wfcai.childId is null and wfcai.childIndex is null";
    	        components = getHibernateTemplate().find(query, params);
    	        if (components != null && !components.isEmpty()) {
    	            return (WorkflowComponentAdjacencyItem) components.get(0);
    	        }
    	} else {
	        Object[] params = {workflowItem,
	    		componentId.toUpperCase(), componentIndex,
	        		childId.toUpperCase(), childIndex};
	        String query = "from WorkflowComponentAdjacencyItem wfcai where"
	                + " workflow = ? and upper(wfcai.componentId) = ? and wfcai.componentIndex = ?"
	        		+ " and upper(wfcai.childId) = ? and wfcai.childIndex = ?";
	        components = getHibernateTemplate().find(query, params);
	        if (components != null && !components.isEmpty()) {
	            return (WorkflowComponentAdjacencyItem) components.get(0);
	        }
    	}
    	return null;
    }


}
