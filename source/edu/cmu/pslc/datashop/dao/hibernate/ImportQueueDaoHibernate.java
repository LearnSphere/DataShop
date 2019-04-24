/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static org.hibernate.Hibernate.INTEGER;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DiscourseImportQueueMapDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * Hibernate and Spring implementation of the ImportQueueDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueDaoHibernate extends AbstractDaoHibernate implements ImportQueueDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ImportQueueItem get(Integer id) {
        return (ImportQueueItem)get(ImportQueueItem.class, id);
    }
    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ImportQueueItem find(Integer id) {
        return (ImportQueueItem)find(ImportQueueItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ImportQueueItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Native SQL to get total number of rows in the queue. */
    private static final String TOTAL_IMPORT_QUEUE_SQL =
            "SELECT count(*) as numItems"
          + " FROM import_queue queue"
          + " LEFT JOIN ds_dataset dataset ON queue.dataset_id = dataset.dataset_id";

    /** Native SQL to get Import Queue DTOs. */
    private static final String IMPORT_QUEUE_SQL =
            "SELECT queue.import_queue_id as importQueueId,"
          + " queue.queue_order as queueOrder,"
          + " project.project_id as projectId,"
          + " project.project_name as projectName,"
          + " dataset.dataset_id as datasetId,"
          + " ifnull(dataset.dataset_name, queue.dataset_name) as datasetName,"
          + " dataset.released_flag as releasedFlag,"
          + " file.file_id as fileId,"
          + " file.actual_file_name as fileName,"
          + " file.file_size as fileSize,"
          + " queue.description as description,"
          + " queue.domain_name as domain_name,"
          + " queue.learnlab_name as learnlab_name,"
          + " concat(user.first_name, ' ', user.last_name) as uploadedByName,"
          + " user.email as emailAddress,"
          + " queue.uploaded_by as uploadedByUserName,"
          + " queue.uploaded_time as uploadedTime,"
          + " queue.format as format,"
          + " queue.status as status,"
          + " queue.est_import_date as estImportDate,"
          + " queue.num_errors as numErrors,"
          + " queue.num_issues as numIssues,"
          + " queue.verification_results as verificationResults,"
          + " queue.num_transactions as numTransactions,"
          + " queue.last_updated_time as lastUpdatedTime,"
          + " queue.anon_flag as anonFlag,"
          + " queue.display_flag as displayFlag,"
          + " queue.s2d_include_user_kcms as includeUserKcms,"
          + " queue.s2d_src_dataset_id as srcDatasetId,"
          + " queue.s2d_src_dataset_name as srcDatasetName,"
          + " queue.s2d_src_sample_id as srcSampleId,"
          + " queue.s2d_src_sample_name as srcSampleName,"
          + " dataset.accessed_flag as accessedFlag,"
          + " map.discourse_id as discourseId"
          + " FROM import_queue queue"
          + " LEFT JOIN project project ON queue.project_id = project.project_id"
          + " LEFT JOIN ds_dataset dataset ON queue.dataset_id = dataset.dataset_id"
          + " LEFT JOIN ds_file file ON queue.file_id = file.file_id"
          + " JOIN user user ON queue.uploaded_by = user.user_id"
          + " LEFT JOIN discourse_import_queue_map map"
          + " ON queue.import_queue_id = map.import_queue_id";

    /** Native SQL where clause for the current user. */
    private static final String WHERE_UPLOADED_BY =
            " WHERE user.user_id = :uploadedBy"
          + " AND queue.status NOT IN ('no_data', 'pending')"
          + " AND (queue.display_flag IS NULL OR queue.display_flag = true)"
          + " AND (dataset.deleted_flag IS NULL OR dataset.deleted_flag = false)"
          + " AND (dataset.released_flag IS NULL OR dataset.released_flag = false)"
          + " ORDER BY queue.uploaded_time";
    /** Native SQL where clause for the given project. */
    private static final String WHERE_PROJECT =
            " WHERE project.project_id = :projectId"
          + " AND queue.status NOT IN ('no_data', 'pending')"
          + " AND (queue.display_flag IS NULL OR queue.display_flag = true)"
          + " AND (dataset.deleted_flag IS NULL OR dataset.deleted_flag = false)"
          + " AND (dataset.released_flag IS NULL OR dataset.released_flag = false)"
          + " ORDER BY queue.uploaded_time";
    /** Native SQL where clause for the administrator's view of the whole import queue. */
    private static final String WHERE_ADMIN_QUEUE =
            " WHERE queue.status IN ('queued', 'passed', 'issues',"
          + " 'loading', 'generating', 'aggregating')"
          + " AND (dataset.deleted_flag IS NULL OR dataset.deleted_flag = false)"
          + " AND (dataset.released_flag IS NULL OR dataset.released_flag = false)"
          + " ORDER BY queue.queue_order";
    /** Native SQL where clause for the administrator's view of the recent items. */
    private static final String WHERE_ADMIN_RECENT =
            " WHERE queue.status IN ('errors', 'loaded', 'canceled')"
          + " AND queue.last_updated_time > :cutoffDate"
          + " ORDER BY queue.last_updated_time";
    /** Native SQL where clause for the administrator's view of the recent no data items. */
    private static final String WHERE_ADMIN_NO_DATA =
            " WHERE queue.status IN ('no_data')"
          + " AND queue.last_updated_time > :cutoffDate"
          + " ORDER BY queue.last_updated_time";
    /** Native SQL where clause for a specific item. */
    private static final String WHERE_ID =
        " WHERE queue.import_queue_id = :importQueueId";
    /** Native SQL where clause for the administrator's view of the whole import queue. */
    private static final String WHERE_NEXT_QUEUED =
            " WHERE queue.status IN ('queued')"
          + " AND (queue.format IN ('tab_delimited'))"
          + " AND (dataset.deleted_flag IS NULL OR dataset.deleted_flag = false)"
          + " AND (dataset.released_flag IS NULL OR dataset.released_flag = false)"
          + " ORDER BY queue.queue_order"
          + " LIMIT 1";
    /** Native SQL where clause for the DiscourseDB project. */
    private static final String WHERE_DISCOURSE_PROJECT =
            " WHERE project.project_id = :projectId"
          + " AND queue.status NOT IN ('no_data', 'pending')"
          + " AND (queue.display_flag IS NULL OR queue.display_flag = true)"
          + " AND (map.discourse_id IS NULL)"
          + " ORDER BY queue.uploaded_time";

    /**
     * Return the total number of rows in the
     * administrator's view of the whole import queue.
     * @return a total number of rows in queue
     */
    private Integer getTotalImportQueueForAdmin() {
        Session session = null;
        String query = TOTAL_IMPORT_QUEUE_SQL + WHERE_ADMIN_QUEUE;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(query.toString());
            sqlQuery.addScalar("numItems", INTEGER);
            List dbResults = sqlQuery.list();
            if (dbResults.size() > 0) {
                return (Integer)dbResults.get(0);
            } else {
                return Integer.valueOf(0);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
    }

    /**
     * Return a list of Import Queue DTO objects given a project id
     * for the Project Datasets page.
     * @param projectId the project id
     * @return a list of Import Queue DTO objects
     */
    public List<ImportQueueDto> getImportQueueByProject(Integer projectId) {
        String query = IMPORT_QUEUE_SQL + WHERE_PROJECT;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("projectId", projectId);
        return runImportQueueQuery(query, params);
    }

    /**
     * Return a list of Import Queue DTO objects for the DiscourseDB project.
     * @param projectId the project id
     * @return a list of Import Queue DTO objects
     */
    public List<ImportQueueDto> getImportQueueByDiscourseProject(Integer projectId) {
        String query = IMPORT_QUEUE_SQL + WHERE_DISCOURSE_PROJECT;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("projectId", projectId);
        return runImportQueueQuery(query, params);
    }

    /**
     * Return a list of Import Queue DTO objects given a username
     * for the My Datasets page.
     * @param username the account/user of the current user
     * @return a list of DTOs
     */
    public List<ImportQueueDto> getImportQueueByUploader(String username) {
        String query = IMPORT_QUEUE_SQL + WHERE_UPLOADED_BY;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("uploadedBy", username);
        return runImportQueueQuery(query, params);
    }

    /**
     * Return a list of Import Queue DTO objects for the
     * administrator's view of the whole import queue.
     * @return a list of Import Queue DTO objects
     */
    public List<ImportQueueDto> getImportQueueForAdmin() {
        String query = IMPORT_QUEUE_SQL + WHERE_ADMIN_QUEUE;
        return runImportQueueQuery(query, null);
    }

    /**
     * Return a list of Import Queue DTO objects for the
     * administrator's view of the recent items not in the queue.
     * @param cutoffDate the cut off date
     * @return a list of Import Queue DTO objects
     */
    public List<ImportQueueDto> getRecentItemsForAdmin(Date cutoffDate) {
        String query = IMPORT_QUEUE_SQL + WHERE_ADMIN_RECENT;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("cutoffDate", cutoffDate);
        return runImportQueueQuery(query, params);
    }

    /**
     * Return a list of Import Queue DTO objects for the
     * administrator's view of the no data items.
     * @param cutoffDate the cut off date
     * @return a list of Import Queue DTO objects
     */
    public List<ImportQueueDto> getRecentNoDataItemsForAdmin(Date cutoffDate) {
        String query = IMPORT_QUEUE_SQL + WHERE_ADMIN_NO_DATA;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("cutoffDate", cutoffDate);
        return runImportQueueQuery(query, params);
    }

    /**
     * Get the Import Queue DTO object for the specified id.
     * @param importQueueId the id
     * @return an Import Queue DTO object
     */
    public ImportQueueDto getImportQueueById(Integer importQueueId) {
        String query = IMPORT_QUEUE_SQL + WHERE_ID;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("importQueueId", importQueueId);
        List<ImportQueueDto> result = runImportQueueQuery(query, params);
        if (result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * Return null if nothing in the queue, otherwise return the next queued item.
     * @return the next queued item, null otherwise
     */
    public ImportQueueDto getNextQueuedItem() {
        String query = IMPORT_QUEUE_SQL + WHERE_NEXT_QUEUED;
        List<ImportQueueDto> list = runImportQueueQuery(query, null);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Return a list of Import Queue DTO objects given a username
     * for the My Datasets page.
     * @param queryString native SQL query string
     * @param paramMap map of parameters to pass into sqlQuery
     * @return a list of DTOs
     */
    public List<ImportQueueDto> runImportQueueQuery(String queryString,
            Map<String, Object> paramMap) {
        List<ImportQueueDto> dtoList = new ArrayList<ImportQueueDto>();

        Session session = null;
        try {
            Integer totalItemsInQueue = getTotalImportQueueForAdmin();

            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(queryString);

            //indicate which columns map to which types.
            sqlQuery.addScalar("importQueueId", Hibernate.INTEGER);
            sqlQuery.addScalar("queueOrder", Hibernate.INTEGER);
            sqlQuery.addScalar("projectId", Hibernate.INTEGER);
            sqlQuery.addScalar("projectName", Hibernate.STRING);
            sqlQuery.addScalar("datasetId", Hibernate.INTEGER);
            sqlQuery.addScalar("datasetName", Hibernate.STRING);
            sqlQuery.addScalar("releasedFlag", Hibernate.BOOLEAN);
            sqlQuery.addScalar("fileId", Hibernate.INTEGER);
            sqlQuery.addScalar("fileName", Hibernate.STRING);
            sqlQuery.addScalar("fileSize", Hibernate.LONG);
            sqlQuery.addScalar("description", Hibernate.STRING);
            sqlQuery.addScalar("domain_name", Hibernate.STRING);
            sqlQuery.addScalar("learnlab_name", Hibernate.STRING);
            sqlQuery.addScalar("uploadedByName", Hibernate.STRING);
            sqlQuery.addScalar("emailAddress", Hibernate.STRING);
            sqlQuery.addScalar("uploadedByUserName", Hibernate.STRING);
            sqlQuery.addScalar("uploadedTime", Hibernate.TIMESTAMP);
            sqlQuery.addScalar("format", Hibernate.STRING);
            sqlQuery.addScalar("status", Hibernate.STRING);
            sqlQuery.addScalar("estImportDate", Hibernate.TIMESTAMP);
            sqlQuery.addScalar("numErrors", Hibernate.INTEGER);
            sqlQuery.addScalar("numIssues", Hibernate.INTEGER);
            sqlQuery.addScalar("verificationResults", Hibernate.STRING);
            sqlQuery.addScalar("numTransactions", Hibernate.LONG);
            sqlQuery.addScalar("lastUpdatedTime", Hibernate.TIMESTAMP);
            sqlQuery.addScalar("anonFlag", Hibernate.BOOLEAN);
            sqlQuery.addScalar("displayFlag", Hibernate.BOOLEAN);
            sqlQuery.addScalar("includeUserKcms", Hibernate.BOOLEAN);
            sqlQuery.addScalar("srcDatasetId", Hibernate.INTEGER);
            sqlQuery.addScalar("srcDatasetName", Hibernate.STRING);
            sqlQuery.addScalar("srcSampleId", Hibernate.INTEGER);
            sqlQuery.addScalar("srcSampleName", Hibernate.STRING);
            sqlQuery.addScalar("accessedFlag", Hibernate.BOOLEAN);
            sqlQuery.addScalar("discourseId", Hibernate.LONG);

            //set the parameters
            if (paramMap != null && paramMap.size() > 0) {
                for (Map.Entry<String, Object> param : paramMap.entrySet()) {
                    sqlQuery.setParameter(param.getKey(), param.getValue());
                }
            }

            if (logger.isTraceEnabled()) {
                logger.trace("runImportQueueQuery sqlQuery [" + sqlQuery + "]");
            }


            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                ImportQueueDto dto = new ImportQueueDto();
                dto.setImportQueueId((Integer)obj[colIdx++]);
                dto.setOrder((Integer)obj[colIdx++]);
                dto.setProjectId((Integer)obj[colIdx++]);
                dto.setProjectName((String)obj[colIdx++]);
                dto.setDatasetId((Integer)obj[colIdx++]);
                dto.setDatasetName((String)obj[colIdx++]);
                dto.setReleasedFlag((Boolean)obj[colIdx++]);
                dto.setFileId((Integer)obj[colIdx++]);
                dto.setFileName((String)obj[colIdx++]);
                dto.setFileSize((Long)obj[colIdx++]);
                dto.setDescription((String)obj[colIdx++]);
                dto.setDomainName((String)obj[colIdx++]);
                dto.setLearnlabName((String)obj[colIdx++]);
                dto.setUploadedByName((String)obj[colIdx++]);
                dto.setEmail((String)obj[colIdx++]);
                dto.setUploadedByUserName((String)obj[colIdx++]);
                dto.setUploadedTime((Date)obj[colIdx++]);
                dto.setFormat((String)obj[colIdx++]);
                dto.setStatus((String)obj[colIdx++]);
                dto.setEstImportDate((Date)obj[colIdx++]);
                dto.setNumErrors((Integer)obj[colIdx++]);
                dto.setNumIssues((Integer)obj[colIdx++]);
                dto.setVerificationResults((String)obj[colIdx++]);
                dto.setNumTransactions((Long)obj[colIdx++]);
                dto.setLastUpdatedTime((Date)obj[colIdx++]);
                dto.setAnonFlag((Boolean)obj[colIdx++]);
                dto.setDisplayFlag((Boolean)obj[colIdx++]);
                dto.setFromSampleWithUserKCs((Boolean)obj[colIdx++]);
                dto.setSrcDatasetId((Integer)obj[colIdx++]);
                dto.setSrcDatasetName((String)obj[colIdx++]);
                dto.setSrcSampleId((Integer)obj[colIdx++]);
                dto.setSrcSampleName((String)obj[colIdx++]);
                dto.setAccessedFlag((Boolean)obj[colIdx++]);
                dto.setDiscourseId((Long)obj[colIdx++]);

                if (StringUtils.isBlank(dto.getUploadedByName())) {
                    dto.setUploadedByName(dto.getUploadedByUserName());
                }

                // For display purposes, if project not set, use empty string.
                if (dto.getProjectId() == null) {
                    dto.setProjectName("");
                }

                dto.setQueuePosition("(" + dto.getOrder() + " of " + totalItemsInQueue + ")");

                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Constant. */
    private static final int MAX_ITEMS = 10;

    /** Recent dataset names HQL. */
    private static final String RECENT_NAMES_HQL =
            "SELECT DISTINCT datasetName"
            + " FROM ImportQueueItem queue"
            + " WHERE queue.uploadedBy.id = ";

    /** Order by clause to tack onto the recent name/descriptions HQL queries. */
    private static final String RECENT_ORDER_BY =
            " ORDER BY queue.uploadedTime DESC";

    /**
     * Get a limited list of the recent dataset names that the
     * given user has used.
     * @param username the current user's id
     * @return a list of dataset names
     */
    public List<String> getRecentDatasetNames(String username) {
        String query = RECENT_NAMES_HQL + "'" + username + "'" + RECENT_ORDER_BY;
        CallbackCreatorHelper helperCreator =
                new CallbackCreatorHelper(query, 0, MAX_ITEMS);
        HibernateCallback callback = helperCreator.getCallback();
        List<String> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /** Recent dataset names HQL. */
    private static final String RECENT_DESCS_HQL =
            "SELECT DISTINCT description"
            + " FROM ImportQueueItem queue"
            + " WHERE LENGTH(description) > 0 "
            + " AND queue.uploadedBy.id = ";

    /**
     * Get a limited list of the recent dataset names that the
     * given user has used.
     * @param username the current user's id
     * @return a list of dataset names
     */
    public List<String> getRecentDescriptions(String username) {
        String query = RECENT_DESCS_HQL + "'" + username + "'" + RECENT_ORDER_BY;
        CallbackCreatorHelper helperCreator =
                new CallbackCreatorHelper(query, 0, MAX_ITEMS);
        HibernateCallback callback = helperCreator.getCallback();
        List<String> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Returns a Dataset given a name.
     * @param name name of Dataset
     * @return list of dataset items
     */
    public List<ImportQueueItem> find(String name) {
        return getHibernateTemplate().find(
                "from ImportQueueItem iq where datasetName = ?", name);
    }

    /** Native SQL. */
    private static final String MAX_ORDER_SQL =
            "SELECT max(queue_order) as maxQueueOrder"
          + " FROM import_queue queue"
          + " WHERE queue.status IN ('queued', 'passed', 'issues',"
          + " 'loading', 'generating', 'aggregating')";

    /**
     * Get the max queue order for new import queue items or return 0
     * if no items are in the import queue.
     * @return the max queue order found in the database
     */
    public Integer getMaxQueueOrder() {
        Integer maxOrder = 0;
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(MAX_ORDER_SQL);
            sqlQuery.addScalar("maxQueueOrder", Hibernate.INTEGER);
            List<Object> dbResults = sqlQuery.list();
            for (Object obj: dbResults) {
                maxOrder = (Integer)obj;
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        if (maxOrder == null) {
            return 0;
        } else {
            return maxOrder;
        }
    }

    /**
     * Find a list of items which are in the queue waiting to be loaded,
     * ie, status in (queued, passed, issues).
     * @return a list of ImportQueueItems
     */
    public List<ImportQueueItem> findItemsInQueue() {
        return getHibernateTemplate().find(
                "FROM ImportQueueItem queue"
              + " WHERE queue.status IN ('queued', 'passed', 'issues')"
              + " ORDER BY queueOrder");
    }

    private static final String BY_ORDER_FOR_ADMIN_HQL =
        "FROM ImportQueueItem WHERE queueOrder = ? "
        + "AND status IN ('queued', 'passed', 'issues', 'loading', 'generating', 'aggregating')";

    /**
     * Find the item with the given queue order.
     * Only items in one of the following states are considered as this
     * method is used for changing the order of items in the admin queue:
     * 'queued', 'passed', 'issues', 'loading', 'generating', or 'aggregating'.
     * As such, only a single item should be found, but if multiple are found
     * only the first is returned.
     * @param queueOrder the given queue order
     * @return an ImportQueueItem if order exists, null otherwise
     */
    public ImportQueueItem findByQueueOrder(Integer queueOrder) {
        ImportQueueItem item = null;
        Object[] params = {queueOrder};
        List<ImportQueueItem> list = getHibernateTemplate().find(BY_ORDER_FOR_ADMIN_HQL, params);
        if (list.size() > 0) {
            item = list.get(0);
        }
        return item;
    }

    /**
     * Find the item with the given dataset item.
     * @param datasetItem the given dataset item
     * @return an ImportQueueItem if dataset attached, null otherwise
     */
    public ImportQueueItem findByDataset(DatasetItem datasetItem) {
        ImportQueueItem item = null;
        Object[] params = {datasetItem};
        List<ImportQueueItem> list = getHibernateTemplate().find(
               "FROM ImportQueueItem WHERE dataset = ?", params);
        if (list.size() > 0) {
            item = list.get(0);
        }
        return item;
    }

    /**
     * Find the item with the given discourse item.
     * @param discourseItem the given discourse item
     * @return an ImportQueueItem if discourse attached, null otherwise
     */
    public ImportQueueItem findByDiscourse(DiscourseItem discourseItem) {
        DiscourseImportQueueMapDao mapDao = DaoFactory.DEFAULT.getDiscourseImportQueueMapDao();
        DiscourseImportQueueMapItem mapItem = mapDao.findByDiscourse(discourseItem);

        ImportQueueItem result = null;
        if (mapItem != null) {
            result = mapItem.getImportQueue();
        }

        return result;
    }

    /**
     * Find a list of items by project and display_flag.
     * @param projectItem the project
     * @param displayFlag the display_flag
     * @return a list of ImportQueueItems
     */
    public List<ImportQueueItem> findByProjectAndDisplayFlag(ProjectItem projectItem,
                                                             Boolean displayFlag) {
        Object[] params = {projectItem, displayFlag};
        return getHibernateTemplate().find(
                "FROM ImportQueueItem WHERE project = ? AND displayFlag = ?", params);
    }

    /** Count by project and display_flag HQL. */
    private static final String COUNT_BY_PROJECT_HQL =
        "SELECT COUNT(*) FROM ImportQueueItem "
      + "WHERE status != 'pending' "
      + "AND project = ? AND displayFlag = ?";

    /**
     * Get count of items by project and display_flag.
     * @param projectItem the project
     * @param displayFlag the display_flag
     * @return a list of ImportQueueItems
     */
    public Long getCountByProjectAndDisplayFlag(ProjectItem projectItem,
                                                Boolean displayFlag) {

        Object[] params = {projectItem, displayFlag};
        Long result = (Long) getHibernateTemplate().find(COUNT_BY_PROJECT_HQL, params).get(0);
        if (result == null) {
            result = Long.valueOf(0);
        }
        return result;
    }

    /** Native SQL for isDatasetAlreadyAttached method. */
    private static final String DATASET_ALREADY_ATTACHED_SQL =
            "SELECT count(*) as count"
          + " FROM import_queue queue"
          + " JOIN ds_dataset dataset"
          + " WHERE queue.dataset_id = :datasetId";

    /**
     * Figure out if the given dataset is already attached to an
     * item in the import queue.
     * @param datasetId dataset id
     * @return true if it is already attached, false otherwise
     */
    public boolean isDatasetAlreadyAttached(Integer datasetId) {
        boolean alreadyAttached = false;
        Session session = null;
        Integer count = 0;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(DATASET_ALREADY_ATTACHED_SQL);
            sqlQuery.addScalar("count", Hibernate.INTEGER);
            sqlQuery.setParameter("datasetId", datasetId);
            List<Object> dbResults = sqlQuery.list();
            for (Object obj: dbResults) {
                count = (Integer)obj;
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        if (count > 0) {
            alreadyAttached = true;
        }
        return alreadyAttached;
    }

    /** HQL query. */
    private static final String OLD_PENDING_HQL = "FROM ImportQueueItem "
            + "WHERE status = 'pending'"
            + "AND uploadedTime < ?";

    /**
     * Return a list of pending import queue items which are over one day old.
     * @return list of import queue items
     */
    public List<ImportQueueItem> findOldPending() {
        Calendar recentCal = Calendar.getInstance();
        recentCal.add(Calendar.DATE, -1);
        Date oneDayAgo = recentCal.getTime();
        Object[] params = {oneDayAgo};
        return getHibernateTemplate().find(OLD_PENDING_HQL, params);
    }

    /**
     * Return a list of IQ items which are pending for a given project.
     * @param projectItem the project
     * @return list of import queue items
     */
    public List<ImportQueueItem> findPendingByProject(ProjectItem projectItem) {
        Object[] params = {projectItem };
        return getHibernateTemplate().find(
                "FROM ImportQueueItem WHERE project = ?", params);
    }

    /** HQL query for the findDatasetsFromSample method. */
    private static final String FIND_DATASETS_FROM_SAMPLE_HQL =
        "select dataset from ImportQueueItem iq where iq.srcSampleId = ?";

    /**
     *  Returns a list of datasets already created from a given sample.
     *  @param sampleId the sample id
     *  @return a list of datasets already created from a given sample
     */
    public List<DatasetItem> findDatasetsFromSample(Integer sampleId) {
        Object[] params = {sampleId};
        // Return any datasets that have mappings to this PcConversionItem
        List<DatasetItem> itemList =
            getHibernateTemplate().find(FIND_DATASETS_FROM_SAMPLE_HQL, params);
        return itemList;
    }
}
