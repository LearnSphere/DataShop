/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.WorkflowFolderDao;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowContext;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
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
public class WorkflowFolderDaoHibernate
    extends AbstractDaoHibernate implements WorkflowFolderDao {

    /**
     * Standard get for a WorkflowFolderItem by id.
     * @param id The id of the WorkflowFolderItem
     * @return the matching WorkflowFolderItem or null if none found
     */
    public WorkflowFolderItem get(Long id) {
        return (WorkflowFolderItem)get(WorkflowFolderItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowFolderItems.
     * @return a List of WorkflowFolderItems
     */
    public List findAll() {
        return findAll(WorkflowFolderItem.class);
    }

    /**
     * Standard find for a WorkflowFolderItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowFolderItem.
     * @return the matching WorkflowFolderItem.
     */
    public WorkflowFolderItem find(Long id) {
        return (WorkflowFolderItem)find(WorkflowFolderItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Gets workflow folders for a given owner.
     * @param loggedInUserItem the owner
     * @return workflow folders for a given owner
     */
    public List<WorkflowFolderItem> findByOwner(UserItem loggedInUserItem) {
        Object[] params = new Object[] { loggedInUserItem };
        String query = "from WorkflowFolderItem wi"
            + " where owner = ?";
        return getHibernateTemplate().find(
                query, params);
    }

    /**
     * Gets workflow folders for a given owner and folder name.
     * @param loggedInUserItem the owner
     * @param folderName the folder name
     * @return workflow folders for a given owner
     */
    public List<WorkflowFolderItem> findByUserAndName(UserItem loggedInUserItem, String folderName) {
        Object[] params = new Object[] { loggedInUserItem, folderName };
        String query = "from WorkflowFolderItem wi"
            + " where owner = ? and workflowFolderName = ?";
        return getHibernateTemplate().find(
                query, params);
    }


    /**
     * Gets workflow folders for a given a workflow.
     * @param workflowItem the workflow
     * @return workflow folders for a given a workflow
     */
    public List<WorkflowFolderItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = new Object[] { workflowItem };
        String query = "select wff from WorkflowFolderItem wff"
            + " join wff.workflows wf "
            + " where wf = ?";
        return getHibernateTemplate().find(
                query, params);
    }

    /**
     * Gets my empty workflow folders.
     * @return my empty workflow folders
     */
    public List<WorkflowFolderItem> findMyEmptyFolders(UserItem owner) {
        Object[] params = new Object[] { owner };
        String query = "select wff from WorkflowFolderItem wff"
            + " left join wff.workflows wf "
            + " where wf is null"
            + " and wff.owner = ?";
        return getHibernateTemplate().find(
                query, params);
    }

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");


    public static enum QUERY_CONDITION {
        GET_COUNT, GET_WORKFLOWS
    }

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
            QUERY_CONDITION queryCondition) {

        List<Object[]> objectList = new ArrayList<Object[]>();

        String orderCriteria = ""; // " , :orderCriteria ";
        String showGlobalPart = " and wf.globalFlag = true";
        if (loggedInUserItem.getAdminFlag()) {
            showGlobalPart = "";
        }

        String whereQueryPart = " where ";
        StringBuffer whereQuery = new StringBuffer();
        Boolean whereQueryStarted = false;

        Boolean searchMine = false;
        if (panelId.equalsIgnoreCase("my-workflows-panel")) {
            searchMine = true;
        }

        Boolean searchShared = false;
        if (panelId.equalsIgnoreCase("shared-workflows-panel")) {
            searchShared = true;
        }

        Boolean searchRecommended = false;
        if (panelId.equalsIgnoreCase("recommended-workflows-panel")) {
            searchRecommended = true;
        }

        String ownerQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_AUTHOR);
        if (!searchMine && ownerQuery != null && !ownerQuery.trim().isEmpty()) {
            String ownerQueryPart = "lower(wf.owner.id) like lower(:owner) ";

            whereQuery.append(whereQueryPart);
            whereQueryStarted = true;

            whereQuery.append(ownerQueryPart);
            orderCriteria = ", wf.owner";
        } else if (searchMine) {
            String ownerQueryPart = null;
            ownerQueryPart = " ( wf.owner.id = :loggedInUser ";

            whereQuery.append(whereQueryPart);
            whereQueryStarted = true;

            whereQuery.append(ownerQueryPart);

        }

        if (searchShared) {
            String searchSharedQueryPart = " ( wf.owner.id != :loggedInUser "
                + showGlobalPart;
            if (!whereQueryStarted) {
                whereQuery.append(whereQueryPart);
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }
            whereQuery.append(searchSharedQueryPart);
        }

        if (searchRecommended) {
            String searchRecommendedQueryPart = " ( wf.isRecommended = true "
                + showGlobalPart;
            if (!whereQueryStarted) {
                whereQuery.append(whereQueryPart);
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }
            whereQuery.append(searchRecommendedQueryPart);
        }

        String componentQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_COMPONENT);
        if (componentQuery != null && !componentQuery.trim().isEmpty()) {
            String componentQueryPart = "(lower(wf.workflowXml) like lower(:componentNameBeginsWild) "
                + "or  lower(wf.workflowXml) like lower(:componentNameEndsWild))";
            if (!whereQueryStarted) {
                whereQuery.append(whereQueryPart);
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }
            whereQuery.append(componentQueryPart);
        }

        String generalQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_BY);
        if (generalQuery != null && !generalQuery.trim().isEmpty()) {
            String generalQueryPart = "( lower(wf.workflowXml) like lower(:searchTerm) or lower(wf.workflowName) like lower(:searchTerm) "
                    + " or lower(wf.description) like lower(:searchTerm) or lower(wf.owner) like lower(:searchTerm) "
                    + " or lower(wf.results) like lower(:searchTerm) "
                    + " or lower(wff.workflowFolderName) like lower(:searchTerm) or lower(wff.description) like lower(:searchTerm) "
                    + " or lower(tags.tag) = lower(:searchTermNotLike) "
                    + " )";

            if (!whereQueryStarted) {
                whereQuery.append(whereQueryPart);
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }
            whereQuery.append(generalQueryPart);
        }

        String dateLowerQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_DATE_LOWER);
        if (dateLowerQuery != null && !dateLowerQuery.trim().isEmpty()) {
            String dateLowerQueryPart = "wf.lastUpdated >= :dateLower ";
            if (!whereQueryStarted) {
                whereQuery.append(whereQueryPart);
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }
            whereQuery.append(dateLowerQueryPart);
        }

        String dateUpperQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_DATE_UPPER);
        if (dateUpperQuery != null && !dateUpperQuery.trim().isEmpty()) {
            String dateUpperQueryPart = "Date(wf.lastUpdated) <= :dateUpper ";
            if (!whereQueryStarted) {
                whereQuery.append(whereQueryPart);
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }
            whereQuery.append(dateUpperQueryPart);
        }

        // TAGGING
        String workflowTagsString = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_TAGS);
        // Join the tags table
        String workflowTagJoin = " left join wf.workflowTags tags ";
        List<String> workflowTagsList = null;

        if (workflowTagsString != null && workflowTagsString.length() > 0) {
            // Only get workflows that have tags in the list of tags to be searched over
            whereQuery.append(" and lower(tags.tag) IN (:tagList) ");

            String [] workflowTags = workflowTagsString.split(",");

            // Convert the tags to lower case.  Searching tags is case agnostic.
            workflowTagsList = new ArrayList<String>();
            for (String tag : workflowTags) {
                workflowTagsList.add(tag.toLowerCase());
            }
            if (generalQuery != null && !generalQuery.trim().isEmpty()) {
                // Cover the case where you search for one tag in the "Quick Search" and another tag in the tag search
                workflowTagsList.add(generalQuery);
            }
        }

        String workflowDatasetMapJoin = "";
        String datasetQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_DATASET);
        if (datasetQuery != null && !datasetQuery.trim().isEmpty()) {
            String datasetQueryPart = "wdm.id = :datasetId) ";
            workflowDatasetMapJoin = " left join wf.datasets wdm ";

            if (!whereQueryStarted) {
                whereQuery.append(whereQueryPart);
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }

            whereQuery.append(datasetQueryPart);
            orderCriteria = orderCriteria + ", wdm.dataset.id";
        }

        String workflowPaperJoin = null;
        if (criteria.getSearchAttribute(WorkflowContext.WF_HAS_PAPERS).equals("true")) {
            workflowPaperJoin = " left join wf.workflowPapers wp ";
            String paperQueryPart = "wp.id is not NULL ";

            if (!whereQueryStarted) {
                whereQueryStarted = true;
            } else {
                whereQuery.append(" and ");
            }

            whereQuery.append(paperQueryPart);

        } else {
            workflowPaperJoin = "";
        }

        String cfpTemplatesQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_NO_AUTH);
        String cfpSharedQuery = criteria.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_SHARED);
        String cfpCanRequestJoin = "";

        String cfpJoinStr = "";
        String cfpWhereStr = "";

        cfpJoinStr = " , ComponentFilePersistenceItem cfp ";

        cfpCanRequestJoin = cfpJoinStr
            + " left join cfp.dataset ds "
                + " left join ds.project proj "
                + " left join proj.authorizations auth"
                + " left join auth.user u";

        String combination = "";
        if (cfpTemplatesQuery != null && !cfpTemplatesQuery.equalsIgnoreCase("false")) {
            if (cfpSharedQuery != null && !cfpSharedQuery.equalsIgnoreCase("false")) {
                combination = "ALL";
            } else {
                // WF contains no dataset files
                combination = "NO_AUTH";
            }
        } else {
            if (cfpSharedQuery != null && !cfpSharedQuery.equalsIgnoreCase("false")) {
                // WF contains dataset files
                combination = "DATASET_FILES";
            } else {
                combination = "NOTHING_CHECKED";
            }
        }
        if (panelId.equalsIgnoreCase("my-workflows-panel")) {
            // Data Access is not part of the query for 'My Workflows' tab.
            combination = "ALL";
        }
        Boolean setLoggedInUser = false;
        // CAVEAT:
        // DATASET_FILES and NO_AUTH are NOT mutally exclusive sets; there can be overlap
        if (!searchMine) {
            if (combination.equalsIgnoreCase("DATASET_FILES")) {
                setLoggedInUser = true;
                // Show workflows that contain data that the user has access to
                cfpWhereStr = " wf.id = cfp.workflow.id and "
                        + " ( auth.user is not null and ( u.id = '%' or u.id = :loggedInUser ) "
                        + " and (auth.level = 'view' or auth.level = 'edit' or auth.level = 'admin' ) "
                         + " ) ) ";
            } else if (combination.equalsIgnoreCase("NO_AUTH")) {
                // Show workflows that contain data that the user cannot access
                String subQuery = "select auth.user.id from auth where auth.project.id = proj.id";
                cfpWhereStr = " ( cfp.componentId like 'Data-%' or cfp.componentId like 'Import-%' ) "
                        + " and wf.id = cfp.workflow.id and ( ( :loggedInUser not in ( "
                        + subQuery + " ) ) and ( '%' not in ( "
                        + subQuery + " ) ) ) ) ";
                setLoggedInUser = true;
            } else if (combination.equalsIgnoreCase("ALL")) {
                // Show all workflows (no need for fancy join)
                cfpJoinStr = "";
                cfpWhereStr = " 1 = 1 ) ";
                cfpCanRequestJoin = "";
            } else if (combination.equalsIgnoreCase("NOTHING_CHECKED")) {
                // Show all workflows if nothing checked
                cfpJoinStr = "";
                cfpWhereStr = " 1 = 1 ) ";
                cfpCanRequestJoin = "";
            }


        } else {
            if (combination.equalsIgnoreCase("DATASET_FILES")) {
                // Show workflows that contain data that the user has access to
                cfpWhereStr = " wf.globalFlag = true ) "
                         + " ) ) ";
            } else if (combination.equalsIgnoreCase("NO_AUTH")) {
                // Show workflows that contain data that the user cannot access
                cfpWhereStr = " wf.globalFlag = false ) ";
            } else if (combination.equalsIgnoreCase("ALL")) {
                // Show all workflows (no need for fancy join)
                cfpJoinStr = "";
                cfpWhereStr = " 1 = 1 ) ";
                cfpCanRequestJoin = "";
            } else if (combination.equalsIgnoreCase("NOTHING_CHECKED")) {
                // Show all workflows if nothing checked
                cfpJoinStr = "";
                cfpWhereStr = " 1 = 1 ) ";
                cfpCanRequestJoin = "";
            }
        }

        if (!whereQueryStarted) {
            whereQuery.append(whereQueryPart);
            whereQueryStarted = true;
        } else {
            whereQuery.append(" and ");
        }

        if (cfpWhereStr.length() > 0) {
            whereQuery.append(cfpWhereStr);
        }

        orderCriteria = orderCriteria + ", lower(wf.workflowName) asc";

        // getPage returns 0 if not set
        Integer offset = (criteria.getPage(panelId) - 1) * criteria.getPageLimit();
        // limit returns 10 if not set
        Integer limit = criteria.getPageLimit();

        Session session = getSession();
        String query = null;
        if (queryCondition.equals(QUERY_CONDITION.GET_COUNT)) {
            query = "select count(distinct wf) from WorkflowFolderItem wff "
                    + " right join wff.workflows wf "
                    + workflowDatasetMapJoin
                    + workflowTagJoin
                    + workflowPaperJoin
                    + cfpCanRequestJoin
                    + whereQuery;
        } else if (queryCondition.equals(QUERY_CONDITION.GET_WORKFLOWS)) {
            query = "select distinct wff, wf from WorkflowFolderItem wff "
                    + " right join wff.workflows wf "
                    + workflowDatasetMapJoin
                    + workflowTagJoin
                    + workflowPaperJoin
                    + cfpCanRequestJoin
                    + whereQuery
                    + " order by case when wff.workflowFolderName is null then 1 else 0 end, wff.workflowFolderName "
                    + orderCriteria;
        }
        Query q = session.createQuery(query);

        if (queryCondition.equals(QUERY_CONDITION.GET_WORKFLOWS)) {
            q.setFirstResult(offset);
            q.setMaxResults(limit);

        }

        if (!whereQuery.toString().isEmpty()) {
            if (generalQuery != null && !generalQuery.trim().isEmpty()) {
                q.setParameter("searchTerm", "%" + generalQuery + "%");
                q.setParameter("searchTermNotLike", generalQuery);
            }
            if (!searchMine && ownerQuery != null && !ownerQuery.trim().isEmpty()) {
                q.setParameter("owner", "%" + ownerQuery + "%");
            }
            if (componentQuery != null && !componentQuery.trim().isEmpty()) {
                q.setParameter("componentNameBeginsWild", "%component_name>%" + componentQuery + "</component_name%");
                q.setParameter("componentNameEndsWild", "%component_name>" + componentQuery + "%</component_name%");
            }
            if (dateLowerQuery != null && !dateLowerQuery.trim().isEmpty()) {
                try {
                    q.setDate("dateLower", dateFormat.parse(dateLowerQuery));
                } catch (ParseException e) {
                    logger.error("Lower Date does not match format: " + dateUpperQuery);
                }
            }
            if (dateUpperQuery != null && !dateUpperQuery.trim().isEmpty()) {
                try {
                    q.setDate("dateUpper", dateFormat.parse(dateUpperQuery));
                } catch (ParseException e) {
                    logger.error("Upper Date does not match format: " + dateUpperQuery);
                }
            }

            if (datasetQuery != null && datasetQuery.matches("[0-9]+")) {
                try {
                    Integer datasetId = Integer.parseInt(datasetQuery);
                    q.setParameter("datasetId", datasetId);
                } catch (NumberFormatException nfe) {
                    logger.error("Not an Integer (dataset ID) " + datasetQuery);
                }
            }

            // One of the following will always be true (included for readability):
            if (searchMine || searchShared || setLoggedInUser) {
                q.setParameter("loggedInUser", loggedInUserItem.getId().toString());
            }

            // Add this list of tags to be searched over
            if (workflowTagsList != null) {
                q.setParameterList("tagList", workflowTagsList);
            }
        }


        if (queryCondition.equals(QUERY_CONDITION.GET_COUNT)) {
            Long count = (Long)q.uniqueResult();
            Object[] objects = new Object[2];
            objects[0] = "count";
            objects[1] = count;
            objectList.add(objects);
        } else if (queryCondition.equals(QUERY_CONDITION.GET_WORKFLOWS)) {
            List sessionList = q.list();
            //Criteria c = session.createCriteria(WorkflowFolderItem.class);
            //c.addOrder(Order.asc("workflowFolderName"));

            for (Iterator iter = sessionList.listIterator(); iter.hasNext();) {
                Object[] objects = (Object[])iter.next();
                objectList.add(objects);
            }

        }

        releaseSession(session);
        return objectList;
    }


}
