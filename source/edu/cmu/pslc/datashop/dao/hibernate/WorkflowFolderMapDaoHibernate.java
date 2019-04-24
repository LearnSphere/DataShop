/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.WorkflowFolderMapDao;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Hibernate and Spring implementation of the WorkflowFolderMapDao.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowFolderMapDaoHibernate
        extends AbstractDaoHibernate implements WorkflowFolderMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public WorkflowFolderMapItem get(WorkflowFolderMapId id) {
        return (WorkflowFolderMapItem)get(WorkflowFolderMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public WorkflowFolderMapItem find(WorkflowFolderMapId id) {
        return (WorkflowFolderMapItem)find(WorkflowFolderMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List<WorkflowFolderMapItem> findAll() {
        return findAll(WorkflowFolderMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByProject method. */
    private static final String FIND_BY_WORKFLOW_HQL
            = "from WorkflowFolderMapItem map"
            + " where workflow = ?";

    /**
     *  Return a list of WorkflowFolderMapItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    public List<WorkflowFolderMapItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = {workflowItem};
        List<WorkflowFolderMapItem> itemList = getHibernateTemplate().find(FIND_BY_WORKFLOW_HQL, params);
        return itemList;
    }

    /** HQL query for the findByIrb method. */
    private static final String FIND_BY_FOLDER_HQL
            = "from WorkflowFolderMapItem map"
            + " where workflowFolder = ?";

    /**
     *  Return a list of WorkflowFolderMapItems.
     *  @param workflowFolderItem the given workflow folder item
     *  @return a list of items
     */
    public List<WorkflowFolderMapItem> findByWorkflowFolder(WorkflowFolderItem workflowFolderItem) {
        Object[] params = {workflowFolderItem};
        List<WorkflowFolderMapItem> itemList = getHibernateTemplate().find(FIND_BY_FOLDER_HQL, params);
        return itemList;
    }

    /**
     *  Return a list of WorkflowFolderMapItems.
     *  @param workflowIds a list of workflow ids
     *  @return a list of workflow folder map items
     */
    public List<WorkflowFolderMapItem> findByWorkflowIds(List<Long> workflowIds) {
        Object[] params = {workflowIds};
        List<WorkflowFolderMapItem> itemList = getHibernateTemplate().find("from WorkflowFolderMapItem wfmi where wfmi.workflowItem.id in ( ? )", params);
        return itemList;
    }


    /**
     * Return a list of workflow folder map items by criteria.
     * @param criteria the workflow folder  map item criteria
     * @param offset offset
     * @param max limit
     * @return a list of workflow folder map items by criteria
     */
    public List<WorkflowFolderMapItem> findBy(WorkflowFolderMapItem criteria, int offset, int limit) {

        List<WorkflowFolderMapItem> wfFolderList = new ArrayList<WorkflowFolderMapItem>();
        /*String searchCriteria = ""; // " where workflowFolderName = :criteria ";
        Session session = getSession();
        String query = "from WorkflowFolderMapItem wff"
            + " join wff.workflow wf"
            + searchCriteria
            + " order by workflowFolderName ";
        Query q = session.createQuery(query);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        if (!searchCriteria.isEmpty()) {
            q.setParameter("criteria", criteria);
        }
        List sessionList = q.list();
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            WorkflowFolderMapItem wfFolderItem = (WorkflowFolderMapItem)iter.next();
            wfFolderList.add(wfFolderItem);
        }
        releaseSession(session);*/
        return wfFolderList;
    }

}
