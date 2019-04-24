/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowRowDto;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
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
public class WorkflowDaoHibernate
    extends AbstractDaoHibernate implements WorkflowDao {

    /**
     * Standard get for a WorkflowItem by id.
     * @param id The id of the WorkflowItem
     * @return the matching WorkflowItem or null if none found
     */
    public WorkflowItem get(Long id) {
        return (WorkflowItem)get(WorkflowItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowItems.
     * @return a List of WorkflowItems
     */
    public List findAll() {
        return findAll(WorkflowItem.class);
    }

    /**
     * Standard find for a WorkflowItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowItem.
     * @return the matching WorkflowItem.
     */
    public WorkflowItem find(Long id) {
        return (WorkflowItem)find(WorkflowItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Find by name.
     * @param workflowName the name of the workflow
     * @return WorkflowItem null if doesn't exist
     */
    public WorkflowItem findByName(String workflowName) {
        return (WorkflowItem)findWithQuery("from WorkflowItem wi where workflow_name = ?",
                                           workflowName);
    }

    /**
     * Find all history items for a given dataset.
     * @param datasetItem the dataset
     * @return a list of history items
     */
    public List find(DatasetItem datasetItem) {
        Object[] params = new Object[] { datasetItem };
        String query = "from WorkflowItem wi"
            + " where dataset = ?";
        return getHibernateTemplate().find(
                query, params);
    }

    /** The DataShop user query for the workflows page rows identified with an owner. */
    private static final String WORKFLOWS_PAGE_QUERY =
        "SELECT wf.owner as owner_id, u.email as owner_email,"
        + " wf.global_flag, wf.workflow_id, wf.workflow_name, wf.workflow_xml,"
        + " wf.description, wf.last_updated, wf.state, ds.dataset_id, ds.dataset_name"
        + " FROM workflow wf"
        + " LEFT JOIN `user` u ON wf.owner = u.user_id";

    // Constant. Query for dataset-specific workflows.
    private static final String WORKFLOWS_DATASET_FILTER =
        " LEFT JOIN workflow_dataset_map wfdsm ON wfdsm.workflow_id = wf.workflow_id"
        + " LEFT JOIN ds_dataset ds ON ds.dataset_id = wfdsm.dataset_id"
        + " WHERE wfdsm.dataset_id = :dataset";

    // Constant. Query part for adding dataset info.
    private static final String WORKFLOWS_DATASET_JOIN =
        " LEFT JOIN workflow_dataset_map wfdsm ON wfdsm.workflow_id = wf.workflow_id"
        + " LEFT JOIN ds_dataset ds ON ds.dataset_id = wfdsm.dataset_id";

    // Constant. Query for owner-specific workflows.
    private static final String WORKFLOWS_OWNER_QUERY = " wf.owner = :ownerId";

    // Constant. Query for NOT owner-specific workflows.
    private static final String WORKFLOWS_NOT_OWNER_QUERY =
        " wf.owner != :ownerId AND wf.global_flag = 1";

    // Constant. Query for NOT owner-specific workflows, for DS admins.
    private static final String WORKFLOWS_ADMIN_QUERY = " wf.owner != :ownerId";

    // Constant. Query for owner-specific or global.
    private static final String WORKFLOWS_MINE_OR_GLOBAL =
        " (wf.owner = :ownerId OR wf.global_flag = 1)";

    /// Queries necessary for 'search' functionality. ///
    // Constant. Search workflow_xml.
    private static final String WORKFLOW_XML_SEARCH =
        "LOWER(wf.workflow_xml) LIKE :componentName";

    // Constant. Search workflow_name.
    private static final String WORKFLOW_NAME_SEARCH =
        "LOWER(wf.workflow_name) LIKE :workflowName";

    // Constant. Search owner
    private static final String WORKFLOW_OWNER_SEARCH =
        "LOWER(wf.owner) LIKE :workflowOwner";

    // Constant. Search description.
    private static final String WORKFLOW_DESC_SEARCH =
        "LOWER(wf.description) LIKE :workflowDescription";

    // Constant. Query for search-by.
    private static final String WORKFLOWS_SEARCH_BY = "("
        + WORKFLOW_XML_SEARCH + " OR " + WORKFLOW_NAME_SEARCH
        + " OR " + WORKFLOW_OWNER_SEARCH
        + " OR " + WORKFLOW_DESC_SEARCH + ")";

    // Query for recently edited.
    private static final String WORKFLOWS_RECENT_QUERY =
        " wf.last_updated > :recentCutoff"
        + " ORDER BY wf.last_updated DESC"
        + " LIMIT 5";

    // Query for last updated.
    private static final String WORKFLOWS_LAST_UPDATED_QUERY = " LIMIT 3";

    /** Query for the recommended workflows. */
    private static final String WORKFLOWS_RECOMMENDED_QUERY =
        "SELECT wf.owner as owner_id, u.email as owner_email,"
        + " wf.global_flag, wf.workflow_id, wf.workflow_name, wf.workflow_xml,"
        + " wf.description, wf.last_updated, wf.state, ds.dataset_id, ds.dataset_name"
        + " FROM workflow wf"
        + " LEFT JOIN `user` u ON wf.owner = u.user_id"
        + " LEFT JOIN workflow_dataset_map wfdsm ON wfdsm.workflow_id = wf.workflow_id"
        + " LEFT JOIN ds_dataset ds ON ds.dataset_id = wfdsm.dataset_id"
        + " WHERE wf.is_recommended = 1 AND wf.global_flag = 1";


    
    /**
     * Helper method to initialize query and generate results.
     * @param query the SQLQuery to be executed
     * @return list of WorkflowRowDto objects
     */
    private List<WorkflowRowDto> runWorkflowQuery(SQLQuery query) {

        List<WorkflowRowDto> result = null;

        query.addScalar("owner_id", Hibernate.STRING);
        query.addScalar("owner_email", Hibernate.STRING);
        query.addScalar("global_flag", Hibernate.BOOLEAN);
        query.addScalar("workflow_id", Hibernate.LONG);
        query.addScalar("workflow_name", Hibernate.STRING);
        query.addScalar("workflow_xml", Hibernate.STRING);
        query.addScalar("description", Hibernate.STRING);
        query.addScalar("last_updated", Hibernate.TIMESTAMP);
        query.addScalar("state", Hibernate.STRING);
        query.addScalar("dataset_id", Hibernate.INTEGER);
        query.addScalar("dataset_name", Hibernate.STRING);

        List<Object[]> dbResults = query.list();

        result = new ArrayList<WorkflowRowDto>();

        for (Object[] o : dbResults) {
            WorkflowRowDto dto = createWorkflowRowDto(o);
            result.add(dto);
        }

        return result;
    }

    /**
     * Helper method to create WorkflowRowDto from db result.
     */
    private WorkflowRowDto createWorkflowRowDto(Object[] o) {

        WorkflowRowDto item = new WorkflowRowDto();

        int index = 0;
        item.setOwnerId((String)o[index++]);
        item.setOwnerEmail((String)o[index++]);
        item.setIsGlobal((Boolean)o[index++]);
        item.setWorkflowId((Long)o[index++]);
        item.setWorkflowName((String)o[index++]);
        item.setWorkflowXml((String)o[index++]);
        item.setDescription((String)o[index++]);
        item.setLastUpdated((Date)o[index++]);
        item.setState((String)o[index++]);
        item.setDatasetId((Integer)o[index++]);
        item.setDatasetName((String)o[index++]);

        return item;
    }

    /**
     * Gets workflows for a given owner.
     * @param loggedInUserItem the owner
     * @return workflows for a given owner
     */
    public List<WorkflowItem> findByOwner(UserItem loggedInUserItem) {
        Object[] params = new Object[] { loggedInUserItem };
        String query = "from WorkflowItem wi"
            + " where owner = ?";
        return getHibernateTemplate().find(
                query, params);
    }


    /**
     * Return a list of workflow items by criteria.
     * @param criteria the workflow item criteria
     * @param offset offset
     * @param max limit
     * @return a list of workflow items by criteria
     */
    public List<WorkflowItem> findBy(WorkflowItem criteria, int offset, int max) {
        DetachedCriteria hbCriteria = DetachedCriteria.forClass(WorkflowItem.class);

        if (criteria.getWorkflowName() != null) {
            hbCriteria.add(Restrictions.ilike("workflowName", (String)criteria.getWorkflowName(), MatchMode.ANYWHERE));
        }
        if (StringUtils.isNotEmpty(criteria.getWorkflowXml())) {
            hbCriteria.add(Restrictions.or(Restrictions.ilike("workflowXml",
                                                              criteria.getWorkflowXml(),
                                                              MatchMode.ANYWHERE),
                                           Restrictions.ilike("results",
                                                              criteria.getWorkflowXml(),
                                                              MatchMode.ANYWHERE)
                                           )
            );
        }
        if (StringUtils.isNotEmpty(criteria.getState())) {
            hbCriteria.add(Restrictions.ilike("state", criteria.getState(), MatchMode.ANYWHERE));
        }

        if (criteria.getLastUpdated() != null) {
            hbCriteria.add(Restrictions.ge("lastUpdated", criteria.getLastUpdated()));
        }
        if (criteria.getOwner() != null) {
            hbCriteria.add(Restrictions.eq("owner", criteria.getOwner()));
        }

        return getHibernateTemplate().findByCriteria(hbCriteria, offset, max);
    }


    /**
     * Get list of workflow owners.
     * @return a list of workflow owners
     */
    public List<UserItem> findOwners() {
        List<UserItem> owners = new ArrayList<UserItem>();
        Object[] params = new Object[] { };
        String query = "select distinct u from WorkflowItem wi"
            + " join wi.owner u";
        return getHibernateTemplate().find(
                query, params);

    }

    /** The DataShop user query for the workflows page rows identified with an owner. For web service*/
    private static final String WORKFLOWS_PAGE_QUERY_WEB_SERVICE =
        "SELECT wf.owner as owner_id, u.email as owner_email,"
        + " wf.global_flag, wf.workflow_id, wf.workflow_name, wf.workflow_xml,"
        + " wf.description, wf.last_updated, wf.state"
        + " FROM workflow wf"
        + " LEFT JOIN `user` u ON wf.owner = u.user_id";

    /**
     * Helper method to initialize query and generate results for only the two getWorkflowRowInfo func. web service
     * @param query the SQLQuery to be executed
     * @return list of WorkflowRowDto objects
     */
    private List<WorkflowRowDto> runWorkflowQueryForWebService(SQLQuery query) {

        List<WorkflowRowDto> result = null;

        query.addScalar("owner_id", Hibernate.STRING);
        query.addScalar("owner_email", Hibernate.STRING);
        query.addScalar("global_flag", Hibernate.BOOLEAN);
        query.addScalar("workflow_id", Hibernate.LONG);
        query.addScalar("workflow_name", Hibernate.STRING);
        query.addScalar("workflow_xml", Hibernate.STRING);
        query.addScalar("description", Hibernate.STRING);
        query.addScalar("last_updated", Hibernate.TIMESTAMP);
        query.addScalar("state", Hibernate.STRING);

        List<Object[]> dbResults = query.list();

        result = new ArrayList<WorkflowRowDto>();

        for (Object[] o : dbResults) {
            WorkflowRowDto dto = createWorkflowRowDtoWebService(o);
            result.add(dto);
        }

        return result;
    }

    /**
     * Helper method to create WorkflowRowDto from db result. for web service
     */
    private WorkflowRowDto createWorkflowRowDtoWebService(Object[] o) {

        WorkflowRowDto item = new WorkflowRowDto();

        int index = 0;
        item.setOwnerId((String)o[index++]);
        item.setOwnerEmail((String)o[index++]);
        item.setIsGlobal((Boolean)o[index++]);
        item.setWorkflowId((Long)o[index++]);
        item.setWorkflowName((String)o[index++]);
        item.setWorkflowXml((String)o[index++]);
        item.setDescription((String)o[index++]);
        item.setLastUpdated((Date)o[index++]);
        item.setState((String)o[index++]);

        return item;
    }

    /**
     * Find the workflow row info for all workflows associated with an user, for web serivce
     * and NOT matching the 'searchBy' criteria, if specified.
     * @param owner the owner's UserItem
     * @param datasetItem the optional dataset item
     * @param userMatch flag indicating if query is for owner's workflows or all
     *        shared workflows not matching the specified owner.
     * @return the WorkflowRowDto's associated with this owner for DataShop Admins.
     */
    public List<WorkflowRowDto> getWorkflowRowInfo(UserItem owner,
                                                   DatasetItem datasetItem,
                                                   Boolean userMatch)
    {
        List<WorkflowRowDto> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(WORKFLOWS_PAGE_QUERY_WEB_SERVICE);
            /*datasetItem is never used in current version
            if (datasetItem != null) {
                sb.append(WORKFLOWS_DATASET_FILTER);
                sb.append(" AND ");
            } else {
                //sb.append(WORKFLOWS_DATASET_JOIN);    
                sb.append(" WHERE ");
            }*/
            sb.append(" WHERE ");

            if (userMatch) {
                // mine
                sb.append(WORKFLOWS_OWNER_QUERY);
            } else {
                // not mine
                if (owner.getAdminFlag()) {
                    // all
                    sb.append(WORKFLOWS_ADMIN_QUERY);
                } else {
                    // shared/global only
                    sb.append(WORKFLOWS_NOT_OWNER_QUERY);
                }
            }
            SQLQuery query = session.createSQLQuery(sb.toString());
            query.setParameter("ownerId", (String)owner.getId());
            //datasetItem is always null in current version
            if (datasetItem != null) {
                query.setParameter("dataset", datasetItem.getId());
            }

            result = runWorkflowQueryForWebService(query);
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /**
     * Find the workflow row info by workflow id. for web service
     * @param workflowId workflow id
     * @return the WorkflowRowDto associated with this workflow id.
     */
    public WorkflowRowDto getWorkflowRowInfo(int workflowId){
        List<WorkflowRowDto> result = null;
        WorkflowRowDto returnDto = null;
        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(WORKFLOWS_PAGE_QUERY_WEB_SERVICE);
            
            sb.append(" WHERE wf.workflow_id = :workflowId");
            SQLQuery query = session.createSQLQuery(sb.toString());
            query.setParameter("workflowId", workflowId);

            result = runWorkflowQueryForWebService(query);
            if (result != null && result.size() > 0)
                    returnDto = result.get(0);
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return returnDto;
    }
}
