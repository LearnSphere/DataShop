/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_AGGREGATE;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_AGGREGATE_KC_MODEL;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_AGGREGATE_SAMPLE;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_KCM_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_PROBLEM_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_STEP_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_TX_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_TX_EXPORT_START;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CREATE_DATASET;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CV;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_GENERATE_MODELS;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_LFA;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_LOG_CONVERSION_DONE;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_MODIFY_DATASET;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_PE_GEN;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_SET_TX_DURATION;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_UPDATED_PROBLEM_HIERARCHY;
import static edu.cmu.pslc.datashop.helper.UserLogger.CF_MODIFY;
import static edu.cmu.pslc.datashop.helper.UserLogger.MODEL_DELETE;
import static edu.cmu.pslc.datashop.helper.UserLogger.MODEL_RENAME;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetSystemLogItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * Hibernate and Spring implementation of the DatasetSystemLogDao.
 *
 * @author Benjamin Billings
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetSystemLogDaoHibernate
        extends AbstractDaoHibernate<DatasetSystemLogItem> implements DatasetSystemLogDao {
    /** HQL where clause for specifying dataset, sample, and action. */
    private static final String WHERE_DATASET_SAMPLE_ACTION =
        " where dsl.dataset = ? and dsl.sample = ? and dsl.action = ?";
    /** HQL query to fetch messages for sample and action. */
    private static final String SAMPLE_MESSAGES_QUERY =
        "select dsl from DatasetSystemLogItem dsl" + WHERE_DATASET_SAMPLE_ACTION;
    /** HQL query to select most recent message . */
    private static final String SELECT_MAX_TIME =
        "select max(dsl.time) from DatasetSystemLogItem dsl";
    /** HQL where clause specifying dataset and action. */
    private static final String WHERE_DATASET_AND_ACTION =
        " where dsl.dataset = ? and dsl.action = ?";
    /** HQL query for selecting most recent entry for dataset and action. */
    private static final String LAST_MODIFIED_QUERY =
            SELECT_MAX_TIME + WHERE_DATASET_AND_ACTION;
    /** HQL query to select most recent message for dataset, sample, and action. */
    private static final String LAST_MODIFIED_SAMPLE_QUERY =
            SELECT_MAX_TIME + WHERE_DATASET_SAMPLE_ACTION;
    /** Common query used to find the MAX dataset_system_log time for a given
     *  action and dataset that was successful. */
    private static final String GET_MAX_TIME_FOR_ACTION_QUERY =
            SELECT_MAX_TIME + WHERE_DATASET_AND_ACTION + " and dsl.successFlag = true";

    /** Common query used to find the MAX dataset_system_log time for a given
     * action and sample. */
    private static final String GET_MAX_TIME_FOR_ACTION_BY_SAMPLE_QUERY = SELECT_MAX_TIME
    + " where dsl.sample = ? and dsl.action = ? and dsl.successFlag = true";

    /**
     * Standard get for a DatasetSystemLogItem by id.
     * @param id The id of the user.
     * @return the matching DatasetSystemLogItem or null if none found
     */
    public DatasetSystemLogItem get(Integer id) {
        return (DatasetSystemLogItem)get(DatasetSystemLogItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DatasetSystemLogItem> findAll() {
        return findAll(DatasetSystemLogItem.class);
    }

    /**
     * Standard find for an DatasetSystemLogItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetSystemLogItem.
     * @return the matching DatasetSystemLogItem.
     */
    public DatasetSystemLogItem find(Integer id) {
        return find(DatasetSystemLogItem.class, id);
    }

    /**
     * Get the date of the most recent 'set tx duration' action for a given
     * dataset.
     * @param dataset the dataset to examine
     * @return the date of the last calc tx duration (can be null).
     */
    private Date getLastTxDurCalcTime(DatasetItem dataset) {
        return (Date)findObject(GET_MAX_TIME_FOR_ACTION_QUERY, dataset, ACTION_SET_TX_DURATION);
    }

    /**
     * Get the date of the most recent cached file action for the given
     * sample and export type.
     * @param sample the sample to examine.
     * @param selection the export type string (byStudentStep, byTransaction, or byProblem)
     * @return the date of the last requested action (can be null).
     */
    public Date getLastCachedTime(SampleItem sample, String selection) {
        String cacheAction = null;
        if ("byTransaction".equals(selection)) {
            cacheAction = ACTION_CACHED_TX_EXPORT;
        } else if ("byStudentStep".equals(selection)) {
            cacheAction = ACTION_CACHED_STEP_EXPORT;
        } else if ("byProblem".equals(selection)) {
            cacheAction = ACTION_CACHED_PROBLEM_EXPORT;
        } else if ("byKcm".equals(selection)) {
            cacheAction = ACTION_CACHED_KCM_EXPORT;
        } else {
            return null;
        }
        return (Date)findObject(GET_MAX_TIME_FOR_ACTION_BY_SAMPLE_QUERY, sample, cacheAction);
    }

    /**
     * Determine if a dataset requires transaction duration calculation.
     * @param dataset the dataset to examine.
     * @return true if the dataset requires transaction duration calculation, false otherwise.
     */
    public Boolean requiresTxDurationCalculation(DatasetItem dataset) {
        Date calcTxDurationTime = getLastTxDurCalcTime(dataset);
        if (calcTxDurationTime == null) { return true; }

        Date modifiedTime = getLastModified(dataset);
        if (modifiedTime == null) { return false; }
        if (calcTxDurationTime.getTime() < modifiedTime.getTime()) { return true; }

        return false;
    }

    /**
     * Find out if a dataset has been modified since the last aggregation.
     * @param dataset the dataset to check.
     * @return whether or not aggregation is required.
     */
    public Boolean requiresAggregation(DatasetItem dataset) {
        Date aggregateTime = (Date)findObject(
                GET_MAX_TIME_FOR_ACTION_QUERY, dataset, ACTION_AGGREGATE);
        if (aggregateTime == null) { return true; }

        Date modifiedTime = getLastModified(dataset);
        Date calcTxDurationTime = getLastTxDurCalcTime(dataset);
        Date modelsGeneratedTime = getLastModelGenerated(dataset);
        Date peGenTime = getLastPeGen(dataset);

        if (modifiedTime == null) { modifiedTime = aggregateTime; }
        if (calcTxDurationTime == null) { calcTxDurationTime = aggregateTime; }
        if (modelsGeneratedTime == null) { modelsGeneratedTime = aggregateTime; }
        if (peGenTime == null) { peGenTime = aggregateTime; }

        if (aggregateTime.getTime() < modifiedTime.getTime()
         || aggregateTime.getTime() < calcTxDurationTime.getTime()
         || aggregateTime.getTime() < modelsGeneratedTime.getTime()
         || aggregateTime.getTime() < peGenTime.getTime()) {
            return true;
        }

        return false;
    }

    /**
     * Find out if a dataset has been modified since the last problem hierarchy update.
     * @param dataset the dataset to check.
     * @return whether or not the problem hierarchy update is required.
     */
    public Boolean requiresProblemHierarchyUpdate(DatasetItem dataset) {
        Boolean requiresUpdate = false;
        Date updateTime = (Date)findObject(
                GET_MAX_TIME_FOR_ACTION_QUERY, dataset, ACTION_UPDATED_PROBLEM_HIERARCHY);
        if (updateTime == null) {
            requiresUpdate = true;
        }
        Date modifiedTime = getLastModified(dataset);
        if (modifiedTime == null) {
            requiresUpdate = false;
        }

        if (updateTime != null && modifiedTime != null
                && updateTime.getTime() < modifiedTime.getTime()) {
            requiresUpdate = true;
        }
        return requiresUpdate;
    }

    /**
     * Find out if a sample needs to be aggregated because
     * the dataset has been modified since the last aggregation of the sample.
     * @param datasetItem the given dataset
     * @param sampleItem the given sample
     * @return true if aggregation is required, false otherwise
     */
    public Boolean requiresAggregation(DatasetItem datasetItem, SampleItem sampleItem) {
        Date aggregateTime = (Date)findObject(GET_MAX_TIME_FOR_ACTION_BY_SAMPLE_QUERY,
                                              sampleItem, ACTION_AGGREGATE_SAMPLE);
        if (aggregateTime == null) { return true; }

        Date modifiedTime = getLastModified(datasetItem);
        Date calcTxDurationTime = getLastTxDurCalcTime(datasetItem);
        Date modelsGeneratedTime = getLastModelGenerated(datasetItem);
        Date peGenTime = getLastPeGen(datasetItem);

        if (modifiedTime == null) { modifiedTime = aggregateTime; }
        if (calcTxDurationTime == null) { calcTxDurationTime = aggregateTime; }
        if (modelsGeneratedTime == null) { modelsGeneratedTime = aggregateTime; }
        if (peGenTime == null) { peGenTime = aggregateTime; }

        if (aggregateTime.getTime() < modifiedTime.getTime()
         || aggregateTime.getTime() < calcTxDurationTime.getTime()
         || aggregateTime.getTime() < modelsGeneratedTime.getTime()
         || aggregateTime.getTime() < peGenTime.getTime()) {
            return true;
        }

        return false;
    }

    /**
     * Query to find samples that require caching by comparing the aggregate
     * sample action against the cached export transaction action.
     * If the max(time) for a sample's aggregate sample is greater than the
     * max(time) for that sample's last cached transaction export action, then return
     * that sample id.
     */
    private static final String FIND_SAMPLES_TO_CACHE_AGG_SAMPLE_VS_CACHED_EXPORT =
        "SELECT dsl.sample_id FROM sample sam"
        + " LEFT JOIN dataset_system_log dsl using (sample_id)"
        + " WHERE sam.dataset_id = :datasetId"
        + " AND dsl.action = :aggSampleAction"
        + " AND dsl.success_flag = true"
        + " and time > "
            + "(SELECT IFNULL(max(dsl2.time), 0)"
            + " FROM sample sam2"
            + " LEFT JOIN dataset_system_log dsl2 using (sample_id)"
            + " WHERE dsl2.dataset_id = :datasetId"
            + " AND dsl2.success_flag = true"
            + " AND dsl2.action = :cachedExportAction"
            + " AND sam.sample_id = dsl2.sample_id)"
        + " GROUP BY sample_id";

    /**
     * Query to find samples that require step or problem caching by comparing
     * the cached export action against the modify dataset action.
     * If the max(time) for a sample's cached export action is less than the
     * max(time) for the dataset's modify action, then return that sample id.
     */
    private static final String FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_MODIFIED_DATASET =
        "SELECT outer_query.sample_id FROM"
        + " (select distinct temp.sample_id,"
            + " Max(temp.time) AS time"
            + " FROM sample sam1"
            + " LEFT JOIN dataset_system_log temp using (sample_id)"
            + " WHERE sam1.dataset_id = :datasetId"
                + " AND temp.action = :cachedExportAction"
                + " AND temp.success_flag = true"
                + " GROUP BY temp.sample_id)"
        + " AS outer_query"
        + " WHERE time < (SELECT IFNULL(max(dsl2.time), 0)"
        + " FROM sample sam2"
        + " LEFT JOIN dataset_system_log dsl2 using (dataset_id)"
                + " WHERE sam2.dataset_id = :datasetId"
                + " AND (dsl2.action  = :modifyDatasetAction"
                      + " OR dsl2.action = '" + ACTION_PE_GEN + "')"
                + " AND dsl2.dataset_id = :datasetId)"
        + " GROUP BY outer_query.sample_id";

    /**
     * Query to find samples that require transaction caching by comparing the cached
     * transaction export action against the modify dataset and modify CF actions.
     * If the max(time) for a sample's cached transaction export action is less than the
     * max(time) for the dataset's modify action, then return that sample id.
     */
    private static final String FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_MODIFIED_DATASET_VS_CF =
        "SELECT outer_query.sample_id FROM"
        + " (select distinct temp.sample_id,"
            + " Max(temp.time) AS time"
            + " FROM sample sam1"
            + " LEFT JOIN dataset_system_log temp using (sample_id)"
            + " WHERE sam1.dataset_id = :datasetId"
            + " AND temp.action = :cachedExportAction"
                + " AND temp.success_flag = true"
                + " GROUP BY temp.sample_id)"
        + " AS outer_query"
        + " WHERE time < (SELECT IFNULL(max(dsl2.time), 0)"
        + " FROM sample sam2"
        + " LEFT JOIN dataset_system_log dsl2 using (dataset_id)"
                + " WHERE sam2.dataset_id = :datasetId"
                + " AND (dsl2.action  = :modifyDatasetAction"
                      + " OR dsl2.action = '" + CF_MODIFY + "'"
                      + " OR dsl2.action = '" + ACTION_PE_GEN + "')"
                + " AND dsl2.dataset_id = :datasetId)"
        + " GROUP BY outer_query.sample_id";

    /** "IN" clause query component used when determining if a cached file generation is
     * required.
     */
    private static final String KCM_ACTIONS_IN_CLAUSE =
        " ('" + ACTION_AGGREGATE_KC_MODEL + "', '" + MODEL_RENAME
        + "', '" + MODEL_DELETE + "')";

    /**
     * Query to find samples that require caching by comparing the cached transaction
     * export action against the aggregate KC model action for a dataset.
     * If the max(time) for a sample's cached transaction export action is not in the set
     * of sample_ids whose cached transaction export time is greater than the aggregate KC
     * model action time, then return that sample_id.
     */
    private static final String FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_AGG_KCM =
        "SELECT dsl.sample_id FROM sample sam1"
        + " LEFT JOIN dataset_system_log dsl using (sample_id)"
            + " LEFT JOIN "
                + "(SELECT DISTINCT dsl.sample_id FROM sample sam2"
                + " LEFT JOIN dataset_system_log dsl using (sample_id)"
                + " WHERE sam2.dataset_id = :datasetId"
                + " AND dsl.time > "
                + " (SELECT IFNULL(max(dsl2.time), 0) FROM sample sam3"
                + " LEFT JOIN dataset_system_log dsl2 using (dataset_id)"
                + " WHERE sam3.dataset_id = :datasetId"
                + " AND dsl2.success_flag = true"
                + " AND dsl2.action IN " + KCM_ACTIONS_IN_CLAUSE
                + ")"
                + " AND dsl.dataset_id = :datasetId"
                + " AND dsl.success_flag = true"
                + " AND dsl.action = :cachedExportAction"
                + ") exportAfterModify using (sample_id)"
        + " WHERE sam1.dataset_id = :datasetId"
        + " AND exportAfterModify.sample_id IS NULL"
        + " AND dsl.success_flag = true"
        + " AND dsl.action = :cachedExportAction"
        + " GROUP BY sample_id";

    /**
     * Return only those samples whose 'aggregate sample' occurred after the
     * latest 'cached transaction export' action for the given dataset.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    public List<SampleItem> getSamplesToCacheAggSampleOnly(DatasetItem dataset) {
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
        int datasetId = ((Integer)dataset.getId()).intValue();
        String prefix = "getSamplesToCacheAggSampleOnly(" + datasetId + "): ";
        StringBuffer query = new StringBuffer();
        query.append(FIND_SAMPLES_TO_CACHE_AGG_SAMPLE_VS_CACHED_EXPORT);
        query.append(" ORDER BY sample_id");

        // build the native SQL query.
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setInteger("datasetId", ((Integer)dataset.getId()).intValue());
        sqlQuery.setString("aggSampleAction", ACTION_AGGREGATE_SAMPLE);
        sqlQuery.setString("cachedExportAction", ACTION_CACHED_TX_EXPORT);

        sqlQuery.addScalar("sample_id", Hibernate.INTEGER);

        List dbResults = sqlQuery.list();
        releaseSession(session);

        logTrace(prefix, sqlQuery);
        if (dbResults.size() == 0) {
            logDebug(prefix, "Results from DB were empty.");
            return dbResults;
        }
        List samplesToCache = new ArrayList();
        for (int i = 0; i < dbResults.size(); i++) {
            Integer sampleId = (Integer)dbResults.get(i);
            logDebug(prefix, "Adding :: ", sampleId, " to list of samples to cache.");
            if (sampleId == null) {
                logger.warn(prefix + "Query returned a null sample_id.");
            } else {
                SampleItem sample = DaoFactory.DEFAULT.getSampleDao().get(sampleId);
                if (!samplesToCache.contains(sample)) {
                    samplesToCache.add(sample);
                } else {
                    logger.info(prefix + "Sample '" + sample.getSampleName()
                            + "' already added to the list of samples to cache.");
                }
            }
        }

        return samplesToCache;
    }

    /**
     * Return only those samples whose 'aggregate sample' occurred after the
     * latest 'cached transaction export' action for the given dataset or whose
     * dataset has been modified after the cached transaction export.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    public List<SampleItem> getSamplesToCacheAggAndModifyActionsOnly(DatasetItem dataset) {
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
        int datasetId = ((Integer)dataset.getId()).intValue();
        String prefix = "getSamplesToCacheAggAndModifyActionsOnly(" + datasetId +  "): ";

        StringBuffer query = new StringBuffer();
        query.append(FIND_SAMPLES_TO_CACHE_AGG_SAMPLE_VS_CACHED_EXPORT);
        query.append(" union ");
        query.append(FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_MODIFIED_DATASET);
        query.append(" ORDER BY sample_id");

        // build the native SQL query.
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setInteger("datasetId", datasetId);
        sqlQuery.setString("aggSampleAction", ACTION_AGGREGATE_SAMPLE);
        sqlQuery.setString("modifyDatasetAction", ACTION_MODIFY_DATASET);
        sqlQuery.setString("cachedExportAction", ACTION_CACHED_TX_EXPORT);

        sqlQuery.addScalar("sample_id", Hibernate.INTEGER);

        List dbResults = sqlQuery.list();
        releaseSession(session);

        logTrace(prefix, sqlQuery);
        if (dbResults.size() == 0) {
            logDebug(prefix, "Results from DB were empty.");
            return dbResults;
        }
        List samplesToCache = new ArrayList();
        for (int i = 0; i < dbResults.size(); i++) {
            Integer sampleId = (Integer)dbResults.get(i);
            logDebug(prefix, "Adding :: ", sampleId, " to list of samples to cache.");
            if (sampleId == null) {
                logger.warn(prefix + "Query returned a null sample_id.");
            } else {
                SampleItem sample = DaoFactory.DEFAULT.getSampleDao().get(sampleId);
                if (sample != null) {
                    if (!samplesToCache.contains(sample)) {
                        samplesToCache.add(sample);
                    } else {
                        logger.info(prefix
                                + "Sample '" + sample.getSampleName()
                                + "' already added to the list of samples to cache.");
                    }
                }
            }
        }

        return samplesToCache;
    }

    /**
     * Determine if there are any samples that require export caching.
     * First look for samples whose ACTION_AGGREGATE_SAMPLE timestamp is greater than
     * that sample's cacheAction timestamp.  Next, look for samples whose
     * cacheAction timestamp is less than a ACATION_AGGREGATE_KC_MODEL
     * timestamp.  Union these two queries and return a list of SampleItems returned
     * from the queries.
     * @param cacheAction ACTION_CACHED_TX_EXPORT or ACTION_CACHED_STEP_EXPORT
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    private List<SampleItem> getSamplesToCache(DatasetItem dataset, String cacheAction) {
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
        int datasetId = ((Integer)dataset.getId()).intValue();
        String prefix = "getSamplesToCache(" + datasetId + ", " + cacheAction + "): ";
        logDebug(prefix, "Starting");

        String findSamplesQuery = FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_MODIFIED_DATASET_VS_CF;
        if (!cacheAction.equals(SystemLogger.ACTION_CACHED_TX_EXPORT)) {
            findSamplesQuery = FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_MODIFIED_DATASET;
        }

        String query = join(" ", FIND_SAMPLES_TO_CACHE_AGG_SAMPLE_VS_CACHED_EXPORT, "union",
                findSamplesQuery, "union",
                FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_AGG_KCM, "ORDER BY sample_id");

        // build the native SQL query.
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("datasetId", datasetId);
        sqlQuery.setString("aggSampleAction", ACTION_AGGREGATE_SAMPLE);
        sqlQuery.setString("cachedExportAction", cacheAction);
        sqlQuery.setString("modifyDatasetAction", ACTION_MODIFY_DATASET);

        sqlQuery.addScalar("sample_id", Hibernate.INTEGER);

        List dbResults = sqlQuery.list();
        releaseSession(session);

        logTrace(prefix, sqlQuery);
        if (dbResults.size() == 0) {
            logDebug(prefix, "Results from DB were empty.");
            return dbResults;
        }
        List samplesToCache = new ArrayList();
        for (int i = 0; i < dbResults.size(); i++) {
            Integer sampleId = (Integer)dbResults.get(i);
            logDebug(prefix, "Adding :: ", sampleId, " to list of samples to cache.");
            if (sampleId == null) {
                logger.warn(prefix + "Query returned a null sample_id.");
            } else {
                SampleItem sample = DaoFactory.DEFAULT.getSampleDao().get(sampleId);
                if (sample != null) {
                    if (!samplesToCache.contains(sample)) {
                        samplesToCache.add(sample);
                    } else {
                        logger.info(prefix
                                + "Sample '" + sample.getSampleName() +
                                "' already added to the list of samples to cache.");
                    }
                }
            }
        }

        return samplesToCache;
    }

    /**
     * Determine if there are any samples that require transaction export caching.
     * First look for samples whose ACTION_AGGREGATE_SAMPLE timestamp is greater than
     * that sample's ACTION_CACHED_TX_EXPORT timestamp.  Next, look for samples whose
     * ACTION_CACHED_TX_EXPORT timestamp is less than a ACTION_AGGREGATE_KC_MODEL
     * timestamp.  Union these two queries and return a list of SampleItems returned
     * from the queries.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    public List<SampleItem> getSamplesToCacheTx(DatasetItem dataset) {
        return getSamplesToCache(dataset, ACTION_CACHED_TX_EXPORT);
    }

    /**
     * Determine if there are any samples that require student-step export caching.
     * First look for samples whose ACTION_AGGREGATE_SAMPLE timestamp is greater than
     * that sample's ACTION_CACHED_STEP_EXPORT timestamp.  Next, look for samples whose
     * ACTION_CACHED_STEP_EXPORT timestamp is less than a ACTION_AGGREGATE_KC_MODEL
     * timestamp.  Union these two queries and return a list of SampleItems returned
     * from the queries.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    public List<SampleItem> getSamplesToCacheStep(DatasetItem dataset) {
        return getSamplesToCache(dataset, ACTION_CACHED_STEP_EXPORT);
    }

    /**
     * Determine if there are any samples that require student problem export caching.
     * First look for samples whose ACTION_AGGREGATE_SAMPLE timestamp is greater than
     * that sample's ACTION_CACHED_PROBLEM_EXPORT timestamp.  Next, look for samples whose
     * ACTION_CACHED_PROBLEM_EXPORT timestamp is less than a ACTION_AGGREGATE_KC_MODEL
     * timestamp.  Union these two queries and return a list of SampleItems returned
     * from the queries.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    public List<SampleItem> getSamplesToCacheProblem(DatasetItem dataset) {
        return getSamplesToCache(dataset, ACTION_CACHED_PROBLEM_EXPORT);
    }

    /**
     * Find out if a dataset has been modified since the last time cached export
     * files were created.
     * @param dataset the dataset to check.
     * @param cacheAction ACTION_CACHED_TX_EXPORT or ACTION_CACHED_STEP_EXPORT
     * @return Boolean of true if cachedExportFileGenerator should run, false otherwise.
     */
    private Boolean requiresCachedExportFileGeneration(
            DatasetItem dataset, String cacheAction) {
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
        int datasetId = ((Integer)dataset.getId()).intValue();
        String prefix = "requiresCachedExportFileGeneration(" + datasetId + ", " + cacheAction + "): ";
        Date cachedTxExportTime = (Date)findObject(GET_MAX_TIME_FOR_ACTION_QUERY, dataset,
                cacheAction);

        if (cachedTxExportTime == null) { return true; }
        String findSamplesQuery = FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_MODIFIED_DATASET_VS_CF;
        if (!cacheAction.equals(SystemLogger.ACTION_CACHED_TX_EXPORT)) {
            findSamplesQuery = FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_MODIFIED_DATASET;
        }
        String sampleQuery = join(" ", findSamplesQuery,
                " union ", FIND_SAMPLES_TO_CACHE_CACHED_EXPORT_VS_AGG_KCM, " ORDER BY sample_id");

        // build the native SQL query.
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(sampleQuery);
        sqlQuery.setInteger("datasetId", ((Integer)dataset.getId()).intValue());
        sqlQuery.setString("cachedExportAction", cacheAction);
        sqlQuery.setString("modifyDatasetAction", ACTION_MODIFY_DATASET);

        logTrace(prefix, sqlQuery);

        List dbResults = sqlQuery.list();
        releaseSession(session);

        if (dbResults.size() == 0) {
            logDebug(prefix, "No samples to cache.");
            return false;
        }
        return true;
    }

    /**
     * Find out if a dataset has been modified since the last cached export file
     * generation.
     * @param dataset the dataset to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    public Boolean requiresCachedTxExportFileGeneration(DatasetItem dataset) {
        return requiresCachedExportFileGeneration(dataset, ACTION_CACHED_TX_EXPORT);
    }

    /**
     * Find out if a dataset has been modified since the last step export file
     * generation.
     * @param dataset the dataset to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    public Boolean requiresCachedStepExportFileGeneration(DatasetItem dataset) {
        return requiresCachedExportFileGeneration(dataset, ACTION_CACHED_STEP_EXPORT);
    }

    /**
     * Find out if a dataset has been modified since the last problem export file
     * generation.
     * @param dataset the dataset to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    public Boolean requiresCachedProblemExportFileGeneration(DatasetItem dataset) {
        return requiresCachedExportFileGeneration(dataset, ACTION_CACHED_PROBLEM_EXPORT);
    }

    /**
     * Find out if a dataset has been modified since the last KC model step export.
     * @param dataset the dataset to check.
     * @return Boolean of true if export/cache required, false otherwise.
     */
    public Boolean requiresCachedKcmStepExportGeneration(DatasetItem dataset) {
        return requiresCachedExportFileGeneration(dataset, ACTION_CACHED_KCM_EXPORT);
    }

    /**
     * Query to find samples that require caching by comparing the cached transaction
     * export action against the aggregate KC model action for a dataset.
     * If the max(time) for a sample's cached transaction export action is not in the set
     * of sample_ids whose cached transaction export time is greater than the aggregate KC
     * model action time, then return that sample_id.
     */
    private static final String FIND_SAMPLES_REQUIRE_CACHE_CACHED_EXPORT_VS_AGG_KCM =
        "SELECT dsl.sample_id as sample_id, dsl.time as time from dataset_system_log dsl"
        + " WHERE dsl.dataset_id = :datasetId"
        + " AND dsl.success_flag = true"
        + " AND dsl.action = :cachedExportAction"
        + " AND dsl.sample_id NOT IN (:samplesToSkipList)";

    /**
     * Query to find the set of sample_ids whose cached transaction export time is
     * greater than the aggregate KC model action time, then return that sample_id.
     * Part one of query for sample ids to be cached.
     */
    private static final String FIND_SAMPLES_NOT_REQUIRING_CACHED_EXPORT =
        "SELECT DISTINCT dsl.sample_id from dataset_system_log dsl"
        + " WHERE dsl.time > "
        + " (SELECT IFNULL(max(dsl2.time), 0) from dataset_system_log dsl2"
        + " WHERE dsl2.dataset_id = :datasetId"
        + " AND dsl2.success_flag = true"
        + " AND dsl2.action IN " + KCM_ACTIONS_IN_CLAUSE
        + ")"
        + " AND dataset_id = :datasetId"
        + " AND success_flag = true"
        + " AND action = :cachedExportAction";

    /** Constant */
    private static final int SAMPLE_ID_IDX = 0;
    /** Constant */
    private static final int CACHED_EXPORT_TIME_IDX = 1;

    /**
     * Get all the sample IDs and cached transaction export time given a dataset.
     * @param dataset the dataset to check
     * @param cacheAction the string which describes the export type
     * @return sampleList a map that includes the sample id and its cached transaction export time
     * */
    public Map<Integer, Date> getSamplesNeedToBeCached(DatasetItem dataset, String cacheAction) {
        Map<Integer, Date> sampleList = new HashMap<Integer, Date>();

        Session session = getSession();

        // Doing this in two parts. First, find the samples that do not need to be cached.
        StringBuffer query = new StringBuffer();
        query.append(FIND_SAMPLES_NOT_REQUIRING_CACHED_EXPORT);

        int datasetId = ((Integer)dataset.getId()).intValue();

        String prefix = "getSamplesNeedToBeCached(" + datasetId + ", " + cacheAction + "): ";

        // build the native SQL query.
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setInteger("datasetId", datasetId);
        sqlQuery.setString("cachedExportAction", cacheAction);

        sqlQuery.addScalar("sample_id", Hibernate.INTEGER);

        String samplesToSkipStr = join(", ", sqlQuery.list());

        // Second, find the samples that do need to be cached.
        query = new StringBuffer();
        query.append(FIND_SAMPLES_REQUIRE_CACHE_CACHED_EXPORT_VS_AGG_KCM);

        sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setInteger("datasetId", datasetId);
        sqlQuery.setString("cachedExportAction", cacheAction);
        sqlQuery.setString("samplesToSkipList", samplesToSkipStr);

        sqlQuery.addScalar("sample_id", Hibernate.INTEGER);
        sqlQuery.addScalar("time", Hibernate.TIMESTAMP);

        logTrace(prefix, sqlQuery);

        List dbResults = sqlQuery.list();

        releaseSession(session);

        int sampleId;
        Date cachedTxExportTime = null;
        for (int i = 0, n = dbResults.size(); i < n; i++) {
            Object[] objArray = (Object[])dbResults.get(i);
            if (objArray[SAMPLE_ID_IDX] != null) {
                sampleId = new Integer((Integer)objArray[SAMPLE_ID_IDX]);
                cachedTxExportTime = (Date)objArray[CACHED_EXPORT_TIME_IDX];

                sampleList.put(sampleId, cachedTxExportTime);
                logDebug(prefix, sampleId, " :: ", cachedTxExportTime);
            }
        }

        if (sampleList.size() == 0) {
            logDebug(prefix, "All samples have been recached since the last",
                    ACTION_AGGREGATE_KC_MODEL, " action.");
        } else {
            logDebug(prefix, "There are ", dbResults.size(), " samples that require caching.");
        }

        return sampleList;
    }

    /**
     * Find out if a dataset has skill models that have been added but not yet cached.
     * @param dataset the dataset to check.
     * @param sample the sample
     * @param cacheAction the string which describes the export type
     * @return a list of SkillModelItems that are not yet cached (if none then the list is empty).
     */
    public List<SkillModelItem> getSkillModelsNotCached(DatasetItem dataset, SampleItem sample,
            String cacheAction) {
        List<SkillModelItem> skillModels = new ArrayList();
        int datasetId = ((Integer)dataset.getId()).intValue();
        String prefix = "getSkillModelsNotCached(" + datasetId + ", " + cacheAction + "): ";

        Map<Integer, Date> sampleToCacheList = getSamplesNeedToBeCached(dataset, cacheAction);

        Date cachedExportTime = sampleToCacheList.get(sample.getId());

        if (cachedExportTime != null) {

            String kcmQuery = "select id from SkillModelItem where (creationTime > ?"
                + " OR modifiedTime > ?) AND dataset = ?";
            Object[] kcmParams = {cachedExportTime, cachedExportTime, dataset};

            List<Long> skillModelIds = getHibernateTemplate().find(kcmQuery, kcmParams);
            if (skillModelIds.isEmpty()) {
                logger.error("List of skill model ids from the DB is empty - should not be!");
            }
            for (Long skillModelId : skillModelIds) {
                SkillModelItem item = DaoFactory.DEFAULT.getSkillModelDao().get(skillModelId);
                String name = item.getSkillModelName();
                logDebug(prefix, "Adding :: ", name, " to skill models not cached list.");
                skillModels.add(item);
            }
        }
        return skillModels;

    }

    /**
     * Find out if a dataset has been modified since the last LFA run.
     * @param dataset the dataset to check.
     * @param skillModel the skill model to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    public Boolean requiresLFA(DatasetItem dataset, SkillModelItem skillModel) {
        Date lfaTime = getLastLFATime(dataset, skillModel);
        if (lfaTime == null) { return true; }

        Date modifiedTime = getLastModified(dataset);
        if (modifiedTime == null) { return false; }

        if (lfaTime.getTime() < modifiedTime.getTime()) { return true; }
        return false;
    }

    /**
     * Find out if a dataset has been modified since the last CV run.
     * @param dataset the dataset to check.
     * @param skillModel the skill model to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    public Boolean requiresCV(DatasetItem dataset, SkillModelItem skillModel) {
        Date cvTime = getLastCVTime(dataset, skillModel);
        if (cvTime == null) { return true; }

        Date modifiedTime = getLastModified(dataset);
        if (modifiedTime == null) { return false; }

        if (cvTime.getTime() < modifiedTime.getTime()) { return true; }
        return false;
    }

    /**
     * Query to get the last time LFA was run on a given dataset and skill model.
     * Note that the success flag doesn't matter, as there is no need to try
     * again if data has not been modified.
     * @param dataset the dataset to check
     * @param skillModel the skill model to check
     * @return Date of the last time it was run, null if never run.
     */
    private Date getLastLFATime(DatasetItem dataset, SkillModelItem skillModel) {
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
        skillModel = DaoFactory.DEFAULT.getSkillModelDao().get((Long)skillModel.getId());

        String query = SELECT_MAX_TIME + WHERE_DATASET_AND_ACTION + " and dsl.skillModel = ?";

        return (Date)findObject(query, dataset, ACTION_LFA, skillModel);
    }

    /**
     * Query to get the last time CV was run on a given dataset and skill model.
     * Note that the success flag doesn't matter, as there is no need to try
     * again if data has not been modified.
     * @param dataset the dataset to check
     * @param skillModel the skill model to check
     * @return Date of the last time it was run, null if never run.
     */
    private Date getLastCVTime(DatasetItem dataset, SkillModelItem skillModel) {
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
        skillModel = DaoFactory.DEFAULT.getSkillModelDao().get((Long)skillModel.getId());

        String query = SELECT_MAX_TIME + WHERE_DATASET_AND_ACTION + " and dsl.skillModel = ?";
        return (Date)findObject(query, dataset, ACTION_CV, skillModel);
    }

    /**
     * Find out if a dataset has been modified since the last KC model generation.
     * @param dataset the dataset to check
     * @return Boolean of true if model generation required, false otherwise.
     */
    public Boolean requiresModelGeneration(DatasetItem dataset) {
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());

        Date modelGenTime = (Date)findObject(GET_MAX_TIME_FOR_ACTION_QUERY, dataset,
                ACTION_GENERATE_MODELS);
        if (modelGenTime == null) {
            return true;
        }

        Date modifiedTime = getLastModified(dataset);
        if (modifiedTime == null) { return false; }

        if (modelGenTime.getTime() < modifiedTime.getTime()) { return true; }
        return false;
    }

    /**
     * Get the time of the most recent message for dataset and action.
     * @param dataset the dataset
     * @param action the action
     * @return time of the most recent message for dataset and action
     */
    private Date getLastSuccessful(DatasetItem dataset, String action) {
        return (Date)findObject(GET_MAX_TIME_FOR_ACTION_QUERY, dataset, action);
    }

    /**
     * Get the time of the most recent message for dataset and action.
     * @param dataset the dataset
     * @param action the action
     * @return time of the most recent message for dataset and action
     */
    private Date getLast(DatasetItem dataset, String action) {
        return (Date)findObject(LAST_MODIFIED_QUERY, dataset, action);
    }

    /**
     * Gets the last date and time that the given dataset had KC models generated.
     * @param dataset the given dataset
     * @return the last date models were generated, null if models not generated or not logged
     */
    public Date getLastModelGenerated(DatasetItem dataset) {
        return getLast(dataset, ACTION_GENERATE_MODELS);
    }

    /**
     * Gets the last Date and time that the given dataset was last modified.
     * @param dataset the dataset you are getting info on.
     * @return Date of the last date modified, null if never modified or not logged.
     */
    public Date getLastModified(DatasetItem dataset) {
        return getLast(dataset, ACTION_MODIFY_DATASET);
    }

    /**
     * Gets the last date and time that the given dataset had Problem Events generated.
     * @param dataset the dataset you are getting info on.
     * @return the last date of given action, null if it never occurred or not logged.
     */
    public Date getLastPeGen(DatasetItem dataset) {
        return getLast(dataset, ACTION_PE_GEN);
    }

    /**
     * Gets the last Date and time that the given dataset was last created,
     * though in theory it wouldn't be created more than once, but you never know.
     * @param dataset the dataset you are getting info on.
     * @return Date of the last date created, null if never created or creation not logged.
     */
    public Date getLastCreated(DatasetItem dataset) {
        return getLast(dataset, ACTION_CREATE_DATASET);
    }

    /**
     * Find messages in the dataset system log for this dataset, sample, and action.
     * @param dataset the dataset
     * @param sample the sample
     * @param action the action message
     * @return the export start messages for this sample, if any
     */
    private List<DatasetSystemLogItem> getMessagesForSample(DatasetItem dataset,
            SampleItem sample, String action) {
        Object[] params = {dataset, sample, action};
        return getHibernateTemplate().find(SAMPLE_MESSAGES_QUERY, params);
    }

    /**
     * Find messages in the dataset system log indicating that an export for this dataset
     * and sample was already started.
     * @param dataset the dataset
     * @param sample the sample
     * @return the export start messages for this sample, if any
     */
    private List<DatasetSystemLogItem> getStartMessagesForSample(DatasetItem dataset,
            SampleItem sample) {
        return getMessagesForSample(dataset, sample, ACTION_CACHED_TX_EXPORT_START);
    }

    /**
     * Check for a matching message in the dataset system log.
     * @param dataset the dataset
     * @param sample the sample
     * @param action the action to check
     * @return whether an export for this dataset and sample was already started
     */
    public boolean messageCheck(DatasetItem dataset, SampleItem sample, String action) {
        return getMessagesForSample(dataset, sample, action).size() > 0;
    }

    /**
     * Remove matching messages.  Current use case is removing messages
     * indicating that an export for this dataset and sample was already started.
     * Only call this after the export has successfully completed or failed.
     * @param dataset the dataset
     * @param sample the sample
     * @param action the action message to remove, generally transaction or step export started
     * message.
     */
    public void removeMessage(DatasetItem dataset, SampleItem sample, String action) {
        for (DatasetSystemLogItem logItem : getMessagesForSample(dataset, sample, action)) {
            getHibernateTemplate().delete(logItem);
        }
    }

    /**
     * Get the time of the most recent message for dataset, sample, and action.
     * @param dataset the dataset
     * @param sample the sample
     * @param action the action
     * @return time of the most recent message for dataset, sample, and action
     */
    private Date getLast(DatasetItem dataset, SampleItem sample, String action) {
        return (Date)findObject(LAST_MODIFIED_SAMPLE_QUERY, dataset, sample, action);
    }

    /**
     * Get the time of the most recent transaction export started message for dataset and sample.
     * @param dataset the dataset
     * @param sample the sample
     * @return time of the most recent transaction export started message for dataset and sample
     */
    private Date getLastTxStart(DatasetItem dataset, SampleItem sample) {
        return getLast(dataset, sample, ACTION_CACHED_TX_EXPORT_START);
    }

    /**
     * Get the time of the most recent transaction export finished message for dataset and sample.
     * @param dataset the dataset
     * @param sample the sample
     * @return time of the most recent transaction export finished message for dataset and sample
     */
    private Date getLastTxFinish(DatasetItem dataset, SampleItem sample) {
        return getLast(dataset, sample, ACTION_CACHED_TX_EXPORT);
    }

    /**
     * True if d1 is not null and d2 is null or d2 is before d1.
     * @param d1 first date
     * @param d2 second date
     * @return True if d1 is not null and d2 is null or d1 is after d2, false otherwise
     */
    private boolean isAfter(Date d1, Date d2) {
        return d1 != null && (d2 == null || d2.compareTo(d1) < 0);
    }

    /**
     * Check for a message in the dataset system log indicating that an export for this dataset
     * and sample was already started.
     * @param dataset the dataset
     * @param sample the sample
     * @return whether an export for this dataset and sample was already started
     */
    public boolean isTransactionExportStarted(DatasetItem dataset, SampleItem sample) {
        return isAfter(getLastTxStart(dataset, sample), getLastTxFinish(dataset, sample));
    }

    /**
     * Check for a message in the dataset system log indicating that an export for this dataset
     * and sample was completed.
     * @param dataset the dataset
     * @param sample the sample
     * @return whether an export for this dataset and sample was completed
     */
    public boolean isTransactionExportCompleted(DatasetItem dataset, SampleItem sample) {
        return isAfter(getLastTxFinish(dataset, sample), getLastTxStart(dataset, sample));
    }

    /**
     * Remove messages indicating that an export for this dataset and sample was already started.
     * Only call this after the export has successfully completed or failed.
     * @param dataset the dataset
     * @param sample the sample
     */
    public void removeTransactionExportStartedMessage(DatasetItem dataset, SampleItem sample) {
        for (DatasetSystemLogItem logItem : getStartMessagesForSample(dataset, sample)) {
            getHibernateTemplate().delete(logItem);
        }
    }

    /**
     * Determine if aggregation for the given sample is complete.
     * @param sample the SampleItem whose aggregation status we are inquiring about.
     * @return OK if aggregation is complete and successful, ERROR if aggregation is complete
     * but not successful, WAIT if no record in the dataset_system_log table exists yet.
     */
    public String sampleAggComplete(SampleItem sample) {
        String query = "SELECT dsl.successFlag FROM DatasetSystemLogItem dsl"
            + " WHERE dsl.sample = ? AND dsl.action = ?"
            + " ORDER BY dsl.time DESC LIMIT 1";
        Boolean successFlag = (Boolean)findObject(query, sample, ACTION_AGGREGATE_SAMPLE);

        if (successFlag == null) {
            return STATUS_WAIT;
        } else {
            return successFlag ? STATUS_OK : STATUS_ERROR;
        }
    }

    /**
     * Check for a "log conversion done" message in the dataset_system_log table for the
     * current date.  If present, return true, otherwise return false.
     * @return true if log conversion has finished for the current date, false otherwise.
     */
    public boolean isLogConversionDone() {
        boolean result = true;

        String myQuery = "SELECT MAX(time) FROM dataset_system_log"
            + " WHERE action = :logConversionDoneAction"
            + " AND success_flag = true"
            + " AND DATE_FORMAT(time, '%Y-%m-%e') LIKE DATE_FORMAT(NOW(), '%Y-%m-%e')";

        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(myQuery);
        sqlQuery.setString("logConversionDoneAction", ACTION_LOG_CONVERSION_DONE);
        List dbResults = sqlQuery.list();
        releaseSession(session);
        Date lastLogConvert = (Date)dbResults.get(0);
        result = lastLogConvert == null ? false : true;

        return result;
    }

    /**
     * Check if a CF Modify has been logged since the last run of CFG-transaction.
     * @param datasetItem the dataset item
     * @return true if the CF Modify needs to be logged
     */
    public boolean cfModifyNeedsLogged(DatasetItem datasetItem) {
        Date lastCfModify = getLast(datasetItem, UserLogger.CF_MODIFY);
        Date lastTxCfg = getLastSuccessful(datasetItem, ACTION_CACHED_TX_EXPORT);
        if (lastCfModify == null || lastTxCfg == null) {
            return true;
        }
        if (lastCfModify.getTime() < lastTxCfg.getTime()) {
            return true;
        }
        return false;
    }

} // end DatasetSystemLogDaoHibernate.java