/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * Data access object to retrieve the data from the Project database table
 * via Hibernate.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12341 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-05-13 14:05:35 -0400 (Wed, 13 May 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetDaoHibernate extends AbstractDaoHibernate
    implements DatasetDao {

    /**
     * Standard get for a project item by id.
     * @param id the id of the desired project item
     * @return the matching DatasetItem or null if none found
     */
    public DatasetItem get(Integer id) {
        return (DatasetItem)get(DatasetItem.class, id);
    }
    /**
     * Standard find for a project item by id.
     * @param id id of the object to find
     * @return DatasetItem
     */
    public DatasetItem find(Integer id) {
        return (DatasetItem)find(DatasetItem.class, id);
    }

    /**
     * Standard "find all" for project items.
     * @return a List of objects which are DatasetItems
     */
    public List findAll() {
        return getHibernateTemplate().find("from " + DatasetItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a list of datasets marked as deleted.
     * @return List of all datasets marked as deleted
     */
    public List<DatasetItem> findDeletedDatasets() {
        return getHibernateTemplate().find(
            "FROM DatasetItem WHERE deletedFlag = true");
    }

    /**
     * Returns a list of datasets marked as deleted.
     * @return List of all datasets marked as deleted
     */
    public List<DatasetItem> findUndeletedDatasets() {
        return getHibernateTemplate().find("FROM DatasetItem WHERE"
                + " (deletedFlag is NULL OR deletedFlag = false)");
    }

    /**
     * Returns a Dataset given a name.
     * @param name name of Dataset
     * @return list of dataset items
     */
    public List find(String name) {
        return getHibernateTemplate().find(
                "from DatasetItem dataset where dataset.datasetName = ?", name);
    }

    /**
     * Returns a Dataset given a name.
     * @param name name of Dataset
     * @return list of dataset items
     */
    public List findIgnoreDeleted(String name) {
        return getHibernateTemplate().find(
                "from DatasetItem dataset where dataset.datasetName = ?"
                        + " AND (deletedFlag is NULL OR deletedFlag = false)", name);
    }

    /** Native SQL select. */
    private static final String FIND_BY_PROJECT_NULL
        = "select dataset.*"
        + " FROM ds_dataset dataset"
        + " WHERE dataset.project_id IS NULL"
        + " AND (deleted_flag IS NULL OR deleted_flag = false)";

    /** Native SQL select. */
    private static final String FIND_BY_PROJECT_ADMIN
        = "select dataset.*"
        + " FROM ds_dataset dataset"
        + " LEFT JOIN import_queue queue ON dataset.dataset_id = queue.dataset_id"
        + " WHERE dataset.project_id = :projectId"
        + " AND ((queue.import_queue_id IS NULL)"
        + "      OR (queue.import_queue_id IS NOT NULL and released_flag = true))"
        + " AND (deleted_flag IS NULL OR deleted_flag = false)";

    /** Native SQL select. */
    private static final String FIND_BY_PROJECT_NOT_ADMIN
        = "select dataset.*"
        + " FROM ds_dataset dataset"
        + " WHERE dataset.project_id = :projectId"
        + " AND (released_flag = true)"
        + " AND (deleted_flag IS NULL OR deleted_flag = false)";

    /**
     * Returns a collection of datasets with given project, which can be null.
     * @param projectItem the project item
     * @param isProjectOrDatashopAdminFlag true if user is project or datashop administrator
     * @return collection of datasets
     */
    public Collection findByProject(ProjectItem projectItem, boolean isProjectOrDatashopAdminFlag) {
        String queryString;
        if (projectItem == null) {
            queryString = FIND_BY_PROJECT_NULL;
            logDebug("findByProject(ProjectNull):", queryString);
            return runDatasetQuery(queryString, null);
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("projectId", projectItem);
            if (isProjectOrDatashopAdminFlag) {
                queryString = FIND_BY_PROJECT_ADMIN;
                logDebug("findByProject(Admin):", queryString);
            } else {
                queryString = FIND_BY_PROJECT_NOT_ADMIN;
                logDebug("findByProject(NotAdmin):", queryString);
            }
            return runDatasetQuery(queryString, params);
        }
    }

    /**
     * Run a Native SQL query for the findByProject method.
     * @param query the query string
     * @param params the parameters for the query, can be null
     * @return a list of DatasetItem objects
     */
    public List runDatasetQuery(String query, Map<String, Object> params) {
        List<DatasetItem> dbResults = new ArrayList<DatasetItem>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.addEntity("dataset", DatasetItem.class);
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    sqlQuery.setParameter(param.getKey(), param.getValue());
                }
            }
            dbResults = sqlQuery.list();
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dbResults;
    }

    /**
     * Query string to get the min and max transactions times for a dataset.
     */
    private static final String MAX_MIN_TRANS_TIME_QUERY =
          "SELECT min(tt.transactionTime), max(tt.transactionTime)"
        + " FROM TransactionItem tt "
                  + " WHERE (tt.dataset.deletedFlag is NULL OR tt.dataset.deletedFlag = false)"
                  + " AND tt.dataset.id = ?";

    /**
     * Automatically sets the start and end times based on transaction data.
     * @param datasetItem the dataset to auto set the times for.
     * @return The dataset item with the updated times, null if a failure occurred.
     */
    public DatasetItem autoSetDates(DatasetItem datasetItem) {
        if (datasetItem == null) {
            throw new IllegalArgumentException("DatasetItem cannot be null");
        }

        if (datasetItem.getId() == null) {
            throw new IllegalArgumentException("Dataset Id cannot be null");
        }

        List results = getHibernateTemplate().find(MAX_MIN_TRANS_TIME_QUERY, datasetItem.getId());

        if (results.size() > 0) {
            Object[] row = (Object[])results.get(0);
            Date startTime = (Date)row[0];
            Date endTime = (Date)row[1];
            datasetItem.setStartTime(startTime);
            datasetItem.setEndTime(endTime);
            saveOrUpdate(datasetItem);
            return datasetItem;
        } else {
            return null;
        }
    }

    /**
     * Find the dataset with the lowest dataset_id and return it.
     * @return a DatasetItem with the lowest database identifier.
     */
    public DatasetItem findDatasetWithMinId() {
        DatasetItem dataset = new DatasetItem();
        String query = "SELECT MIN(id) FROM DatasetItem"
                + " where deletedFlag is NULL OR deletedFlag = false";
        List results = getHibernateTemplate().find(query);
        if (results.size() > 0) {
            Integer datasetId = (Integer)results.get(0);
            dataset.setId(datasetId);
            return dataset;
        } else {
            return null;
        }
    }

    /**
     * Find the list of datasets where learnlab is null and not marked as junk.
     * @return a list of DatasetItem objects where learnlab is null
     */
    public List<DatasetItem> findDatasetsWhereLearnLabIsNull() {
        String query = "FROM DatasetItem WHERE learnlab_id is null AND"
                + " (junkFlag = false OR junkFlag is null) AND"
                + " (deletedFlag is NULL OR deletedFlag = false)";
        return getHibernateTemplate().find(query);
    }

    /**
     * Gets a list of datasets with given learnlab.
     * @param learnlab the learnlab item to search in.
     * @return List of all matching items.
     */
    public List<DatasetItem> findByLearnlab(LearnlabItem learnlab) {

        return getHibernateTemplate().find(
                    "FROM DatasetItem WHERE learnlab.id = ?"
                + " and (deletedFlag is NULL OR deletedFlag = false)", learnlab.getId());
    }

    /**
     * Gets a list of datasets with given domain.
     * @param domain the domain item to search in.
     * @return List of all matching items.
     */
    public List<DatasetItem> findByDomain(DomainItem domain) {
        return getHibernateTemplate().find("FROM DatasetItem WHERE domain.id =?"
                + " and (deletedFlag is NULL OR deletedFlag = false)", domain.getId());
    }

    /**
     * Gets the import queue Id of the item associated with the provided dataset.
     * @param datasetItem the dataset item
     * @return the import queue Id of the item associated with the provided dataset
     */
    public Integer getImportQueueId(DatasetItem datasetItem) {

        List importQueueList =
                getHibernateTemplate()
                .find("SELECT id FROM ImportQueueItem WHERE dataset_id =?", datasetItem.getId());
        if (!importQueueList.isEmpty()) {
            return (Integer) importQueueList.get(0);
        }
        return null;
    }

    /**
     * Returns a list of datasets for the sitemap.xml: released, not junk
     * and not deleted.
     * @return List of all non-junk datasets marked as released and not deleted
     */
    public List<DatasetItem> findDatasetsForSiteMap() {
        return getHibernateTemplate().find(
            "FROM DatasetItem WHERE releasedFlag = true AND"
            + " (junkFlag IS NULL OR junkFlag = false) AND"
            + " (deletedFlag IS NULL OR deletedFlag = false)");
    }

    /**
     * Count the number of papers for the given dataset.
     * @param datasetItem the given dataset
     * @return the number of papers attached to the given dataset
     */
    public Long countPapers(DatasetItem datasetItem) {
        String query = "select count(*) from DatasetItem dat"
            + " join dat.papers papers"
            + " where dat.id = ?";

        if (logger.isTraceEnabled()) {
            logger.trace("Getting number of papers in dataset with query :: " + query);
        }

        Long numResults = (Long)getHibernateTemplate().find(query, datasetItem.getId()).get(0);
        if (numResults == null) {
             numResults = Long.valueOf(0);
        }
        return numResults;
    }

    /**
     * Count the number of external analyses for the given dataset.
     * @param datasetItem the given dataset
     * @return the number of external analyses attached to the given dataset
     */
    public Long countExternalAnalyses(DatasetItem datasetItem) {
        String query = "select count(*) from DatasetItem dat"
                + " join dat.externalAnalyses externalAnalyses"
                + " where dat.id = ?";

        if (logger.isTraceEnabled()) {
            logger.trace("Getting number of externalAnalyses in dataset with query :: " + query);
        }

        Long numResults = (Long)getHibernateTemplate().find(query, datasetItem.getId()).get(0);
        if (numResults == null) {
            numResults = Long.valueOf(0);
        }
        return numResults;
    }

    /**
     * Count the number of attached files for the given dataset.
     * @param datasetItem the given dataset
     * @return the number of attached files for the given dataset
     */
    public Long countFiles(DatasetItem datasetItem) {
        String query = "select count(*) from DatasetItem dat"
                + " join dat.files files"
                + " where dat.id = ?";
        
        if (logger.isTraceEnabled()) {
            logger.trace("Getting number of files in dataset with query :: " + query);
        }

        Long numResults = (Long)getHibernateTemplate().find(query, datasetItem.getId()).get(0);
        if (numResults == null) {
            numResults = Long.valueOf(0);
        }
        return numResults;
    }
} // end DatasetDaoHibernate.java
