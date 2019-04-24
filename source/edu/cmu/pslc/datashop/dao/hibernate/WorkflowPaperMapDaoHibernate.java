/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.List;


import edu.cmu.pslc.datashop.dao.WorkflowPaperMapDao;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Hibernate and Spring implementation of the WorkflowPaperMapDao.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowPaperMapDaoHibernate
        extends AbstractDaoHibernate implements WorkflowPaperMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public WorkflowPaperMapItem get(WorkflowPaperMapId id) {
        return (WorkflowPaperMapItem)get(WorkflowPaperMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public WorkflowPaperMapItem find(WorkflowPaperMapId id) {
        return (WorkflowPaperMapItem)find(WorkflowPaperMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List<WorkflowPaperMapItem> findAll() {
        return findAll(WorkflowPaperMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByProject method. */
    private static final String FIND_BY_WORKFLOW_HQL
            = "from WorkflowPaperMapItem map"
            + " where workflow = ?";

    /**
     *  Return a list of WorkflowPaperMapItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    public List<WorkflowPaperMapItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = {workflowItem};
        List<WorkflowPaperMapItem> itemList = getHibernateTemplate().find(FIND_BY_WORKFLOW_HQL, params);
        return itemList;
    }

    /** HQL query for the findByIrb method. */
    private static final String FIND_BY_FOLDER_HQL
            = "from WorkflowPaperMapItem map"
            + " where workflowPaper = ?";

    /**
     *  Return a list of WorkflowPaperMapItems.
     *  @param workflowPaperItem the given workflow paper item
     *  @return a list of items
     */
    public List<WorkflowPaperMapItem> findByWorkflowPaper(WorkflowPaperItem workflowPaperItem) {
        Object[] params = {workflowPaperItem};
        List<WorkflowPaperMapItem> itemList = getHibernateTemplate().find(FIND_BY_FOLDER_HQL, params);
        return itemList;
    }

    /**
     *  Return a list of WorkflowPaperMapItems.
     *  @param workflowIds a list of workflow ids
     *  @return a list of workflow paper map items
     */
    public List<WorkflowPaperMapItem> findByWorkflowIds(List<Long> workflowIds) {
        Object[] params = {workflowIds};
        List<WorkflowPaperMapItem> itemList = getHibernateTemplate().find("from WorkflowPaperMapItem wfmi where wfmi.workflowItem.id in ( ? )", params);
        return itemList;
    }


    /**
     * Return a list of workflow paper map items by criteria.
     * @param criteria the workflow paper  map item criteria
     * @param offset offset
     * @param max limit
     * @return a list of workflow paper map items by criteria
     */
    public List<WorkflowPaperMapItem> findBy(WorkflowPaperMapItem criteria, int offset, int limit) {

        List<WorkflowPaperMapItem> wfPaperList = new ArrayList<WorkflowPaperMapItem>();
        /*String searchCriteria = ""; // " where workflowPaperName = :criteria ";
        Session session = getSession();
        String query = "from WorkflowPaperMapItem wff"
            + " join wff.workflow wf"
            + searchCriteria
            + " order by workflowPaperName ";
        Query q = session.createQuery(query);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        if (!searchCriteria.isEmpty()) {
            q.setParameter("criteria", criteria);
        }
        List sessionList = q.list();
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            WorkflowPaperMapItem wfPaperItem = (WorkflowPaperMapItem)iter.next();
            wfPaperList.add(wfPaperItem);
        }
        releaseSession(session);*/
        return wfPaperList;
    }

}
