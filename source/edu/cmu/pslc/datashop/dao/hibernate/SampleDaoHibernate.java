/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CONDITION;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CONDITION_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CONDITION_TYPE;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD_VALUE;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DATASET_LEVEL;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DATASET_LEVEL_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DATASET_LEVEL_TITLE;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DB;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.HQL;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.HQL_ABBREV;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.PROBLEM;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.PROBLEM_DESCRIPTION;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.PROBLEM_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.SCHOOL;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.SCHOOL_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.SESSION;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.STUDENT;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.STUDENT_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.TRANSACTION;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.TRANSACTION_SUBGOAL_ATTEMPT;
import static edu.cmu.pslc.datashop.util.FileUtils.openAndReplaceSequence;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hibernate.Hibernate.LONG;
import static org.hibernate.Hibernate.STRING;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateQueryException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.TxExportDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.DatasetState;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleHistoryItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SampleRowDto;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogException;

/**
 * Hibernate and Spring implementation of the SampleDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleDaoHibernate extends AbstractSampleDaoHibernate implements SampleDao {
    /** Name of transaction headers table, stored procedure. */
    private static final String TX_HEADERS = "tx_headers";
    /** select number of distinct student steps. */
    private static final String NUM_PERFORMED_STEPS_QUERY =
        "select count(distinct student_id, step_id) from step_rollup "
        + "where sample_id = :sample_id ";
    /** Logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a SampleItem by id.
     * @param id The id of the user.
     * @return the matching SampleItem or null if none found
     */
    public SampleItem get(Integer id) {
        return (SampleItem)get(SampleItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<SampleItem> findAll() {
        return findAll(SampleItem.class);
    }

    /**
     * Standard find for an SampleItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SampleItem.
     * @return the matching SampleItem.
     */
    public SampleItem find(Integer id) {
        return (SampleItem)find(SampleItem.class, id);
    }

    /**
     * Get a list of samples for the dataset.
     * @param dataset The datasetItem to get all samples for.
     * @return a List of SampleItems that meet the criteria
     */
    public List<SampleItem> find(DatasetItem dataset) {
        String query = "select sam from SampleItem sam where sam.dataset.id = ? ";

        logDebug("getItemList query: ", query);

        return getHibernateTemplate().find(query, dataset.getId());
    }

    /**
     * Get a list of samples for the dataset that are either
     * flagged as global or owned by the user.
     * @param dataset The datasetItem to get all samples for.
     * @param user The user to get all samples for.
     * @return a List of SampleItems that meet the criteria
     */
    public List<SampleItem> find(DatasetItem dataset, UserItem user) {
        String query = "select sam from SampleItem sam where sam.dataset.id = ? "
          + "and (sam.globalFlag = ? or owner.id = ?) ";

        logDebug("getItemList query: ", query);
        return getHibernateTemplate().find(query, new Object[] {
                dataset.getId(), true, user.getId() });
    }

    /**
     * Get a list of samples for the dataset that are
     * owned by the user and have the given name.
     * @param dataset The datasetItem to get all samples for.
     * @param user The user to get all samples for.
     * @param sampleName the sample name
     * @return a List of SampleItems that meet the criteria
     */
    public List<SampleItem> find(DatasetItem dataset, UserItem user, String sampleName) {
        String query = "select sam from SampleItem sam where sam.dataset.id = ? "
          + "and sam.sampleName = ? and owner.id = ? ";

        logDebug("getItemList query: ", query);
        return getHibernateTemplate().find(query, new Object[] {
                dataset.getId(), sampleName, user.getId() });
    }

    /**
     * Get a list of samples for the dataset that are
     * flagged as global and have the given name.
     * @param dataset The datasetItem to get all samples for.
     * @param globalFlag the global flag
     * @param sampleName the sample name
     * @return a List of SampleItems that meet the criteria
     */
    public List<SampleItem> find(DatasetItem dataset, Boolean globalFlag, String sampleName) {
        String query = "select sam from SampleItem sam where sam.dataset.id = ? "
          + "and sam.sampleName = ? and sam.globalFlag = ? ";

        logDebug("getItemList query: ", query);
        return getHibernateTemplate().find(query, new Object[] {
                dataset.getId(), sampleName, globalFlag });
    }


    /**
     * Creates a default sample which is all transactions for the given dataset.
     * @param dataset to create the default sample for.
     * @return the All Data sample for the given dataset, or null if the dataset
     * has no transactions.
     */
    public SampleItem findOrCreateDefaultSample(DatasetItem dataset) {
        Integer datasetId = (Integer)dataset.getId();
        SampleItem newSample = null;
        List<SampleItem> matches =
            find(dataset, Boolean.TRUE, SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME);
        if (matches.size() > 0) {
            if (matches.size() > 1) {
                logger.error("More than one default sample found for dataset "
                        + datasetId + ". That's bad.");
            }
            newSample = matches.get(0);
            logDebug("Dataset ", datasetId,
                    " contains ", matches.size(),
                    " sample(s). Returning sample id ", newSample.getId());
        } else if (getNumTransactions(dataset) <= 0) {
            logDebug("Dataset ", datasetId,
                    " contains no transactions, skipping Default sample creation.");
        } else {
            logDebug("Dataset ", datasetId,
                     " contains no samples. Creating the default sample.");
            newSample = new SampleItem();
            newSample.setDataset(dataset);
            newSample.setGlobalFlag(new Boolean(true));
            newSample.setDescription("Default Sample that contains all transactions.");
            newSample.setSampleName(SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME);
            newSample.setFilePath(FileUtils.cleanForFileSystem(dataset.getDatasetName()));
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            newSample.setOwner(userDao.findOrCreateDefaultUser());
            saveOrUpdate(newSample);
            newSample = get((Integer)newSample.getId());

            // Create sample history item
            UserItem userItem = userDao.get(UserItem.SYSTEM_USER);
            String infoString = "Created sample '" + newSample.getSampleName()
                + "'";
            String infoStringDSL = "Created sample '" + newSample.getSampleName()
                    + "' [" + newSample.getId() + "]";
            SamplesHelper samplesHelper = new SamplesHelper();
            samplesHelper.saveSampleHistory(userItem, newSample,
                SampleHistoryItem.ACTION_CREATE_SAMPLE, infoString, null);
            // Log create sample action to dataset system log
            SystemLogger.log(newSample.getDataset(), null, newSample,
                    SystemLogger.ACTION_CREATE_SAMPLE,
                    infoStringDSL,
                    Boolean.TRUE,
                    null);
        }
        return newSample;
    }

    /**
     * Gets a list of skills associated with this sample.
     * @param sampleItem the sample item
     * @param skillModelItem the skillModel to get skills for.
     * @return a list of SkillItems that fall under this sample/skillModel.
     */
    public List<SkillItem> getSkillList(final SampleItem sampleItem,
            final SkillModelItem skillModelItem) {
        String query = "select distinct(sri.skill) from StepRollupItem sri"
            + " where sri.sample.id = ? and sri.skillModel.id = ?";

        return getHibernateTemplate().find(query, new Object[] {
                sampleItem.getId(), skillModelItem.getId() });
    }

    /**
     * Gets a list of problems associated with this sample.
     * @param sampleItem the sample item
     * @return a list of ProblemItems that fall under this sample.
     */
    public List<ProblemItem> getProblemList(SampleItem sampleItem) {
        String query = "select distinct(sri.problem) from StepRollupItem sri"
                + " where sri.sample.id = ?";

        return getHibernateTemplate().find(query, sampleItem.getId());
    }

    /**
     * Gets a list of students associated with this sample from the
     * aggregate step rollup table.
     * @param sampleItem the sample item
     * @return a list of StudentItems that fall under this sample.
     */
    public List<StudentItem> getStudentListFromAggregate(SampleItem sampleItem) {
        String query = "select distinct(sri.student) from StepRollupItem sri"
            + " where sri.sample.id = ?";

        // [ysahn] check SQL here
        // SELECT DISTINCT(student_id) FROM step_rollup sri WHERE sri.sample_id = 1;
        return getHibernateTemplate().find(query, sampleItem.getId());
    }

    /**
     * Gets a list of students associated with this sample.
     * @param sample the sample item
     * @return a list of StudentItems that fall under this sample.
     */
    public List<StudentItem> getStudentList(SampleItem sample) {
        String loggerPrefix = "getStudentList : Sample (" + sample.getId() + ")";
        logDebug(loggerPrefix, " start.");
        StringBuffer query = new StringBuffer();

        List<StudentItem> results = null;
        Session session = getSession();
        try {
            String stud = STUDENT.get(HQL_ABBREV);
            //re-attach the sample if it has an ID.
            if (sample.getId() != null) {
                sample = (SampleItem)session.get(SampleItem.class, (Integer)sample.getId());
            }
            //select is the problem.
            query.append("select distinct " + stud + " from "
                    + SampleItem.class.getName() + " sam join sam.dataset dat "
                    + buildFilterJoinClause(sample.getFiltersExternal()) + " join "
                    + SESSION.get(HQL_ABBREV) + ".student " + stud);
            logDebug("getStudentList: Join Clause Finished :: \"", query, "\"");
            //add the "WHERE" information starting with the sample id.
            query.append(" where sam.id = " + sample.getId() + " "
                    + buildFilterWhereClause(sample.getFiltersExternal()));

            logDebug(loggerPrefix, " querying with : ", query);
            logDebug("hibernateTemplate maxResultsLimit: "
                    + getHibernateTemplate().getMaxResults());

            results = getHibernateTemplate().find(query.toString());
        } finally {
            releaseSession(session);
        }

        return results;
    }

    /**
     * Returns the number of transactions that fall under this sample.
     * @param sampleItem the sampleItem to get number of transactions for.
     * @return Integer of the number of transactions.
     */
    public Long getNumTransactions(SampleItem sampleItem) {
        String query = "select new java.lang.Long(count(distinct " + TRANSACTION.get(HQL_ABBREV)
            + ")) from " + DatasetItem.class.getName() + " dat ";
        Long result = null;
        Session session = getSession();
        try {

            //re-attach the sample if it has an ID.
            if (sampleItem.getId() != null) {
                sampleItem = (SampleItem)session.get(SampleItem.class, (Integer)sampleItem.getId());
            }
            List<FilterItem> filterList = sampleItem.getFiltersExternal();
            //first pass adds all the joins
            query += buildFilterJoinClause(filterList);
            //second pass adds the where clause conditionals.
            if (sampleItem != null && sampleItem.getDataset() != null
                    && sampleItem.getDataset().getId() != null) {
                query += "where dat.id = " + sampleItem.getDataset().getId() + " ";
            } else {
                logger.warn("getNumTransaction called with either "
                        + "a null dataset or a null datasetId.");
                return null;
            }
            query += buildFilterWhereClause(filterList);
            logDebug("Getting number of transactions with query:\"", query, "\"");

            List<Long> intList;

            try {
                intList = getHibernateTemplate().find(query);
            } catch (BadSqlGrammarException grammerException) {

                logger.warn("SampleDaoHibernate caught the following exception :: "
                        + grammerException.getMessage());

                logDebugStacktrace(grammerException);
                releaseSession(session);
                return null;
            } catch (HibernateQueryException queryException) {
                logger.warn("SampleDaoHibernate caught the following exception :: "
                        + queryException.getMessage());
                logDebugStacktrace(queryException);
                releaseSession(session);
                return null;
            }
            if (intList.size() != 1) {
                logger.warn("Returned List from getNumTransactions"
                        + " contains the incorrect number of elements");
            }
            result = intList.get(0);
        } finally {
            releaseSession(session);
        }

        return result;
    }

    /**
     * Returns the total number of steps attempted by students in this sample.
     * @param sample the sampleItem get the count for.
     * @return Long of the number of total steps performed.
     */
    public Long getNumPerformedSteps(final SampleItem sample) {
        //should only happen on a dataset with no transactions.
        if (sample == null) { return 0L; }

        return getLongForSQL(NUM_PERFORMED_STEPS_QUERY, new PrepareQuery() {
            @Override
            public void prepareQuery(SQLQuery query) {
                query.setInteger("sample_id", (Integer)sample.getId());
            }
        }, 0L);
    }

    /**
     * Returns the number of unique steps that fall under this sample.
     * @param sample the sampleItem to get information for.
     * @return Long of the number of unique steps.
     */
    public Long getNumUniqueSteps(SampleItem sample) {
        Session session = getSession();
        Long maxSelectionCount = new Long(0);
        String query = "select count(distinct tt.subgoal_id) as count"
            + " from tutor_transaction tt"
            + " join transaction_sample_map tsm on tt.transaction_id = tsm.transaction_id"
            + " where tsm.sample_id = :sampleId";

        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("sampleId", (Integer)sample.getId());
        sqlQuery.addScalar("count", LONG);
        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);
        List selectionList = sqlQuery.list();
        releaseSession(session);

        // get the first count from the list and return it - it's the highest skill count
        if (selectionList.size() == 0) {
            maxSelectionCount = new Long(1);
        } else {
            maxSelectionCount = (Long) selectionList.get(0);
        }
        return maxSelectionCount;
    }

    /**
     * Returns the unique steps that are in this sample,
     * ordered by problem hierarchy, problem name and then step name.
     * @param sample the sampleItem to get information for.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return list of subgoal items
     */
    public List getUniqueSteps(SampleItem sample, Integer limit, Integer offset) {
        // HQL
        String query = "select distinct sg"
            + " from TransactionItem tt"
            + " join tt.subgoal sg"
            + " join tt.samples sample"
            + " join sg.problem problem"
            + " join problem.datasetLevel level"
            + " where sample.id = " + sample.getId()
            + " order by problem.datasetLevel.levelName, problem.problemName, sg.subgoalName";

        CallbackCreatorHelper helperCreator = new CallbackCreatorHelper(query.toString(),
                offset.intValue(), limit.intValue());

        HibernateCallback callback = helperCreator.getCallback();
        if (logger.isDebugEnabled()) {
            logger.debug("getUniqueSteps: querying with callback :: Limit: "
                    + limit + " Offset: " + offset + " query: "  + query);
        }
        List results = getHibernateTemplate().executeFind(callback);

        return results;
    }

    /**
     * Find the number of students for the given sample.
     * @param sampleItem the given sample
     * @return the number of students for the given sample
     */
    public Long getNumStudents(SampleItem sampleItem) {
        Long numStudents = new Long(0);
        String loggerPrefix = "getNumStudents() : Sample (" + sampleItem.getId() + ")";
        if (logger.isDebugEnabled()) {
            logger.debug(loggerPrefix);
        }

        if (DatasetState.requiresAggregation(sampleItem.getDataset())) {
            numStudents = getNumStudentsSlowWay(sampleItem);
        } else {
            numStudents = getNumStudentsUsingTransactionSampleMap(sampleItem);
        }

        if (numStudents <= 0) {
            numStudents = new Long(0);
            logger.warn(loggerPrefix + " Could not find number of students.");
        }

        return numStudents;
    }

    /**
     * Find the number of students for the given sample
     * using the Transaction Sample Map (faster).
     * @param sampleItem the given sample
     * @return the number of students for the given sample
     */
    private long getNumStudentsUsingTransactionSampleMap(SampleItem sampleItem) {
        Long numStudents = new Long(-1);

        Session session = getSession();
        String query = "select count(distinct sess.student_id) as count"
            + " from transaction_sample_map tsm"
            + " join tutor_transaction tt on tt.transaction_id = tsm.transaction_id"
            + " join session sess on sess.session_id = tt.session_id"
            + " and tsm.sample_id = :sample_id";
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("sample_id", (Integer)sampleItem.getId());
        sqlQuery.addScalar("count", LONG);
        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);

        List queryResults = sqlQuery.list();
        if (queryResults.size() >= 0) {
            numStudents = ((Long)queryResults.get(0));
        }
        releaseSession(session);

        return numStudents;
    }

    /**
     * Find the number of students for the given sample
     * not using the Transaction Sample Map (slower).
     * @param sampleItem the given sample
     * @return the number of students for the given sample
     */
    private Long getNumStudentsSlowWay(SampleItem sampleItem) {
        return new Long(getStudentList(sampleItem).size());
    }

    /**
     * Find the number of sessions for the given sample
     * using the Transaction Sample Map (faster).
     * @param sampleItem the given sample
     * @return the number of sessions for the given sample
     */
    public Long getNumSessions(SampleItem sampleItem) {
        Long numSessions = new Long(0);

        Session session = getSession();
        String query = "SELECT COUNT(DISTINCT tt.session_id) AS count"
            + " FROM tutor_transaction tt"
            + " JOIN transaction_sample_map map USING (transaction_id)"
            + " WHERE map.sample_id = :sample_id";
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("sample_id", (Integer)sampleItem.getId());
        sqlQuery.addScalar("count", LONG);
        //sqlQuery.setCacheable(false);
        //sqlQuery.setCacheMode(CacheMode.IGNORE);

        List queryResults = sqlQuery.list();
        if (queryResults.size() >= 0) {
            numSessions = ((Long)queryResults.get(0));
        }
        releaseSession(session);

        return numSessions;
    }

    /**
     * Returns the number of transactions that fall under this sample.
     * @param datasetItem the Dataset Item to get number of transactions for.
     * @return Integer of the number of transactions.
     */
    private Long getNumTransactions(DatasetItem datasetItem) {
        String query =
            "select new java.lang.Long(count(tt))"
                + " from TransactionItem tt where tt.dataset.id = ?"
                + " and (tt.dataset.deletedFlag is NULL OR tt.dataset.deletedFlag = false)";

        List<Long> results = getHibernateTemplate().find(query, (Integer)datasetItem.getId());
        return results.get(0);
    }

    /**
     * Returns a list of all transactions associated with this sample.
     * @param sampleItem the sample item to get transactions for.
     * @return a List of TransactionItems
     */
    public List getTransactions(SampleItem sampleItem) {
        return getTransactions(sampleItem, null, null, null);
    }

    /**
     * Returns a list of all transactions associated with this sample and student.
     * @param sampleItem the sample item to get transactions for.
     * @param student StudentItem to get transactions for.
     * @return a List of TransactionItems
     */
    public List getTransactions(SampleItem sampleItem, StudentItem student) {
        return getTransactions(sampleItem, null, null, student);
    }

    /**
     * Returns preview list of transactions associated with this sample.
     * @param sampleItem the sample item to get transactions for.
     * @param limit The max number of items to return.
     * @param offset The offset for the start position on number returned.
     * @return a List of TransactionItems
     */
    public List getTransactions(SampleItem sampleItem, Integer limit, Integer offset) {
        return getTransactions(sampleItem, limit, offset, null);
    }

    /**
     * Returns preview list of transactions associated with this sample.
     * @param sampleItem the sample item to get transactions for.
     * @param limit The max number of items to return.
     * @param offset The offset for the start position on number returned.
     * @param student to get transactions for.  NULL will return all students.
     * @return a List of TransactionItems
     */
    private List getTransactions(SampleItem sampleItem,
            Integer limit, Integer offset, StudentItem student) {
        getHibernateTemplate().flush();
        getHibernateTemplate().clear();

        Session session = getSession();
        //re-attach the sample if it has an ID.
        if (sampleItem.getId() != null) {
            sampleItem = (SampleItem)session.get(SampleItem.class, (Integer)sampleItem.getId());
        }
        releaseSession(session);

        String query =
              "select distinct(tt) from TransactionItem tt "
            + "  join tt.samples samps "
            + "  join tt.session sess "
            + "  join sess.student studs"
            + " where samps.id = " + sampleItem.getId();

        if (student != null) {
            query += " and studs.id = " + student.getId();
        }

        // not sorting makes preview much faster, so we will risk it
        // query += " order by studs.anonymousUserId, tt.transactionTime, "
        //        + " tt.subgoalAttempt, tt.attemptAtSubgoal, tt.id";
        logDebug("Calling getTransactions with query:\"", query, "\"");

        List results;
        //if the offset and limit exist, use a hibernateCallback object.
        if (offset != null && limit != null) {
            CallbackCreatorHelper helperCreator = new CallbackCreatorHelper(
                    query.toString(), offset.intValue(), limit.intValue());

            HibernateCallback callback = helperCreator.getCallback();
            logDebug("getTransactions: querying with callback : ", query);
            results = getHibernateTemplate().executeFind(callback);
        } else {
            logDebug("getTransactions: querying with : ", query);
            results = getHibernateTemplate().find(query.toString());
        }

        return results;
    }

    /**
     * Builds the query string for getting transactions for a given sample and student.
     * @param sampleItem the sample to get transactions for.
     * @param student the student to get transactions for.
     * @return String of the HQL query to get transactions.
     */
    private String buildGetTransactionsQuery(SampleItem sampleItem, StudentItem student) {
        String query = "select distinct " + TRANSACTION.get(HQL_ABBREV)
            + ".id from " + SampleItem.class.getName() + " sam join sam.dataset dat ";

        List<FilterItem> filtersToProccess =
            new ArrayList(DaoFactory.DEFAULT.getFilterDao().find(sampleItem));

        //add an extra filter for the single student.
        FilterItem studentFilter = new FilterItem();
        studentFilter.setClazz(STUDENT.get(DB));
        studentFilter.setAttribute(STUDENT_NAME.get(DB));
        if (student != null && student.getId() != null) {
            studentFilter.setOperator("=");
            // Since concatenating, make sure no single quotes can hijack the query
            studentFilter.setFilterString("'"
                + student.getAnonymousUserId().replace("'", "") + "'");
        }
        filtersToProccess.add(studentFilter);

        //first pass adds all the joins
        query += buildFilterJoinClause(filtersToProccess);
        //second pass adds the where clause conditionals.
        query += "where sam.id = " + sampleItem.getId() + " ";
        query += buildFilterWhereClause(filtersToProccess);
        query += " order by stud.anonymousUserId, trans.transactionTime";
        logDebug("get transactions query is ", query);

        return query;
    }

    /**
     * Returns a list of object arrays (Object[]) representing rows and columns in a
     * data preview with columns determined by the ordered set of filters.
     * @param sampleItem the sample to get a preview for
     * @param offset the offset
     * @param limit indicates how many rows to return
     * @return a List of Lists with objects in rows and columns for the preview.
     */
    public List getTransactionsPreview(SampleItem sampleItem, Integer offset, Integer limit) {
        Session session = getSession();
        //re-attach the sample if it has an ID.
        if (sampleItem.getId() != null) {
            sampleItem = (SampleItem)session.get(SampleItem.class, (Integer)sampleItem.getId());
        }
        List filterList = sampleItem.getFiltersExternal();
        releaseSession(session);

        //create the select statement.
        String query = buildFilterPreviewSelectClause(filterList);
        //add the from and the join to the dataset table.
        query += " from " + DatasetItem.class.getName() + " dat ";
        //add all the joins
        query += buildFilterJoinClause(filterList);

        if (sampleItem != null
                && sampleItem.getDataset() != null
                && sampleItem.getDataset().getId() != null) {
            query += "where dat.id = " + sampleItem.getDataset().getId() + " ";
        } else {
            logger.warn("getTransactionsPreview called with"
                    + " either a null dataset or a null datasetId.");
            return null;
        }

        //second pass adds the where clause conditionals.
        query += buildFilterWhereClause(filterList);
        query += " group by trans.id"; //prevents multiple rows.

        logDebug("Setting limit to :: ", limit);

        //set the limit in a hibernate callback that won't affect future queries
        CallbackCreatorHelper helperCreator = new CallbackCreatorHelper(
                query.toString(), offset == null ? 0 : offset.intValue(),
                limit == null ? 0 : limit.intValue());
        HibernateCallback callback = helperCreator.getCallback();

        logDebug("Getting transaction preview with query:\"", query, "\"");
        List results = null;

        try {
            results = getHibernateTemplate().executeFind(callback);
        } catch (BadSqlGrammarException exception) {
            logger.warn("BadSqlGrammarException caught :: "
                    + exception.getMessage(), exception);
            results = null;
        } catch (HibernateQueryException exception) {
            logger.warn("HibernateQueryException caught :: "
                    + exception.getMessage(), exception);
            results = null;
        }

        return results;
    }

    /**
     * Builds the select portion of the the preview HQL from the ordered list of filters.
     * @param filterItemList the list of filter items to parse.
     * @return string of query.
     */
    private String buildFilterPreviewSelectClause(List filterItemList) {
        String query = "select trans.id, "
            + TRANSACTION.get(HQL_ABBREV) + ".transactionTime";

        HashSet columns = new HashSet();

        for (int i = 0, n = filterItemList.size(); i < n; i++) {
            FilterItem filterItem = (FilterItem)filterItemList.get(i);
            columns.add(filterItem.getAttribute());

            if (filterItem.getAttribute().equals(
                    CONDITION_NAME.get(DB))) {
                query += ", " + CONDITION.get(HQL_ABBREV) + i + ".";
                query += CONDITION_NAME.get(HQL);
            }

            if (filterItem.getAttribute().equals(
                    CONDITION_TYPE.get(DB))) {
                query += ", " + CONDITION.get(HQL_ABBREV) + i + ".";
                query += CONDITION_TYPE.get(HQL);
            }

        }

        if (columns.contains(DATASET_LEVEL_TITLE.get(DB))) {
            query += ", " + DATASET_LEVEL.get(HQL_ABBREV) + ".";
            query += DATASET_LEVEL_TITLE.get(HQL);
        }
        if (columns.contains(DATASET_LEVEL_NAME.get(DB))) {
            query += ", " + DATASET_LEVEL.get(HQL_ABBREV) + ".";
            query += DATASET_LEVEL_NAME.get(HQL);
        }
        if (columns.contains(PROBLEM_NAME.get(DB))) {
            query += ", " + PROBLEM.get(HQL_ABBREV) + ".";
            query += PROBLEM_NAME.get(HQL);
        }
        if (columns.contains(PROBLEM_DESCRIPTION.get(DB))) {
            query += ", " + PROBLEM.get(HQL_ABBREV) + ".";
            query += PROBLEM_DESCRIPTION.get(HQL);
        }
        if (columns.contains(SCHOOL_NAME.get(DB))) {
            query += ", " + SCHOOL.get(HQL_ABBREV) + ".";
            query += SCHOOL_NAME.get(HQL);
        }
        if (columns.contains(STUDENT_NAME.get(DB))) {
            query += ", " + STUDENT.get(HQL_ABBREV) + ".";
            query += STUDENT_NAME.get(HQL);
        }
        if (columns.contains(TRANSACTION_SUBGOAL_ATTEMPT.get(DB))) {
            query += ", " + TRANSACTION.get(HQL_ABBREV) + ".";
            query += TRANSACTION_SUBGOAL_ATTEMPT.get(HQL);

        }
        if (columns.contains("transactionTypeTutor")) {
            query += ", trans.transactionTypeTutor";
        }
        if (columns.contains("transactionTypeTool")) {
            query += ", trans.transactionTypeTool";
        }
        if (columns.contains("transactionSubtypeTutor")) {
            query += ", trans.transactionSubtypeTutor";
        }
        if (columns.contains("transactionSubtypeTool")) {
            query += ", trans.transactionSubtypeTool";
        }
        if (columns.contains(CUSTOM_FIELD_NAME.get(DB))) {
            query += ", " + CUSTOM_FIELD_NAME.get(HQL_ABBREV) + ".";
            query += CUSTOM_FIELD_NAME.get(HQL);
        }
        if (columns.contains(CUSTOM_FIELD_VALUE.get(DB))) {
            query += ", " + CUSTOM_FIELD_VALUE.get(HQL_ABBREV) + ".";
            query += CUSTOM_FIELD_VALUE.get(HQL);
        }

        query += " ";
        logger.debug("Select Clause Finished :: \"" + query + "\"");

        return query;
    }

    /**
     * This function will walk through each filter in turn attempting to determine
     * which one caused the problem.
     * @param sampleItem The sample that has a potential problem
     * @return string of the error or null if no error found. <br>
     * If an error is found it will be of the format...<br>
     * FILTER_ERROR|(filter class)|(filter attribute)|(filter operator)|(filter string)<br>
     * or ERROR|(error string)
     */
    public String determineError(SampleItem sampleItem) {
        Session session = getSession();
        //re-attach the sample if it has an ID.
        if (sampleItem.getId() != null) {
            sampleItem = (SampleItem)session.get(SampleItem.class, (Integer)sampleItem.getId());
        }
        List testfilterList = sampleItem.getFiltersExternal();

        for (Iterator it = testfilterList.iterator(); it.hasNext();) {
            FilterItem filterItem = (FilterItem)it.next();
            SampleItem testSample = new SampleItem();
            testSample.setDataset(sampleItem.getDataset());
            testSample.addFilter(filterItem);
            List filterList = testSample.getFiltersExternal();

            StringBuffer query = new StringBuffer();
            query.append("select new java.lang.Long(count(" + TRANSACTION.get(HQL_ABBREV)
                + ")) from " + DatasetItem.class.getName() + " dat ");

            //first pass adds all the joins
            query.append(buildFilterJoinClause(filterList));
            //second pass adds the where clause conditionals.
            if (sampleItem != null
                    && sampleItem.getDataset() != null
                    && sampleItem.getDataset().getId() != null) {
                query.append("where dat.id = " + sampleItem.getDataset().getId() + " ");
            } else {
                logger.warn("determineError called with either a null dataset "
                        + "or a null datasetId.");
                return new String("ERROR|Unable to determine error with sample.");
            }
            query.append(buildFilterWhereClause(filterList));
            logDebug("Getting number of transactions with query:\"", query, "\"");

            try {
                getHibernateTemplate().find(query.toString());
            } catch (BadSqlGrammarException grammerException) {
                logger.warn("SampleDaoHibernate caught the following exception :: "
                        + grammerException.getMessage());
                logDebugStacktrace(grammerException);
                return "FILTER_ERROR|" + filterItem.getClazz() + "|"
                        + filterItem.getAttribute() + "|"
                        + filterItem.getOperator() + "|" + filterItem.getFilterString();
            } catch (HibernateQueryException queryException) {
                logger.warn("SampleDaoHibernate caught the following exception :: "
                        + queryException.getMessage());
                logDebugStacktrace(queryException);
                return "FILTER_ERROR|" + filterItem.getClazz() + "|"
                        + filterItem.getAttribute() + "|" + filterItem.getOperator() + "|"
                        + filterItem.getFilterString() + "|";
            }

        }
        releaseSession(session);

        return null;
    }

    /**
     * Builds the center part of the query with the join statements.
     * @param filters the list of filter items to parse.
     * @return string of query.
     */
    private StringBuffer buildFilterJoinClause(List<FilterItem> filters) {
        StringBuffer query = new StringBuffer();
        StringBuffer conditionJoin = new StringBuffer();
        String trans = TRANSACTION.get(HQL_ABBREV);

        //the following conditions require special joins.
        boolean conditionFilter = false;
        boolean problemFilter = false;
        boolean datasetLevelFilter = false;
        boolean schoolFilter = false;
        boolean schoolOuterJoin = false;
        boolean studentFilter = false;
        boolean customFieldFilter = false;

        //figure out what special joins are required.
        for (int index = 0, n = filters.size(); index < n; index++) {
            FilterItem filter = (FilterItem)filters.get(index);

            logDebug("Filter Item found: ", filter);
            if (filter.checkClass(CONDITION.get(DB))) {
                logDebug("Condition Filter Found");
                conditionFilter = true;
                if (filter.isNotOperator() || filter.isNullOperator()
                        || filter.isEmptyFilterString()) {
                    conditionJoin.append("left outer ");
                }
                conditionJoin.append("join " + trans
                        + ".conditions " + CONDITION.get(HQL_ABBREV) + index + " ");
            }
            if (filter.checkClass(PROBLEM.get(DB))) {
                logDebug("Problem Filter Found");
                problemFilter = true;
            }
            if (filter.checkClass(DATASET_LEVEL.get(DB))) {
                logDebug("Dataset Level Filter Found");
                datasetLevelFilter = true;
            }
            if (filter.checkClass(SCHOOL.get(DB))) {
                logger.debug("School Filter Found");
                schoolFilter = true;
                if (filter.isNotOperator() || filter.isNullOperator()
                        || filter.isEmptyFilterString()) {
                    schoolOuterJoin = true;
                }
            }
            if (filter.checkClass(STUDENT.get(DB))) {
                logDebug("Student Filter Found");
                studentFilter = true;
            }
            if (filter.checkClass(CUSTOM_FIELD.get(DB))) {
                logDebug("Custom Field Filter Found");
                customFieldFilter = true;
            }
        }

        //build the join depending on which filters are required.
        final String sess = SESSION.get(HQL_ABBREV);
        String prob = PROBLEM.get(HQL_ABBREV);
        if (problemFilter || datasetLevelFilter) {
            query.append("join dat.tutorTransactions " + trans
                + " left join " + trans + ".problem " + prob
                + " join " + prob + ".datasetLevel lev"
                + " left join " + trans + ".subgoal sub"
                + " left join " + trans + ".session " + sess + " ");
            logDebug("Using Problem/datasetLevel join: \"" + query + "\"");
        } else {
            query.append("join dat.tutorTransactions " + trans + " "
                + "join " + trans + ".session " + sess + " ");
            logDebug("Using Default join: \"" + query + "\"");
        }
        if (conditionFilter) {
            query.append(conditionJoin);
            logDebug("Using condition join");
        }
        if (schoolFilter) {
            if (schoolOuterJoin) {
                query.append("left outer ");
            }
            logDebug("Using School ", (schoolOuterJoin ? "outer" : "inner"), " join");
            query.append("join " + trans + ".school " + SCHOOL.get(HQL_ABBREV) + " ");
        }
        if (studentFilter) {
            query.append("join " + sess + ".student stud ");
            logDebug("Using Student join: \"", query, "\"");
        }
        if (customFieldFilter) {
            String cf = CUSTOM_FIELD.get(HQL_ABBREV);
            String cfTxLev = CUSTOM_FIELD_VALUE.get(HQL_ABBREV);
            query.append(" join trans.cfTxLevels " + cfTxLev 
                         + " join " + cfTxLev + ".customField " + cf + " ");
            logDebug("Using Custom Field join: \"" + query + "\"");
        }
        logDebug("Join Clause Finished :: \"", query, "\"");

        return query;
    }

    /**
     * Inserts transaction/sample pairs into the transaction_sample_map.
     * <strong>Note: </strong> this function is transactional by student.
     * @param sample the sample to save.
     * @return number of mappings inserted.
     */
    public int populateTransactionSampleMap(SampleItem sample) {
        int count = 0;
        List<StudentItem> studentList = getStudentList(sample);
        logger.info("populateTransactionSampleMap: number of students = " + studentList.size());
        for (StudentItem student : studentList) {
            count += populateTransactionSampleMap(sample, student);
        }
        logger.info("populateTransactionSampleMap: number of mappings = " + count);
        return count;
    }

    /** Native SQL insert query. */
    private static final String SAMPLE_TRANS_MAP_UPDATE =
        "insert ignore into transaction_sample_map (transaction_id, sample_id) values ";

    /**
     * Inserts transaction/sample pairs into the transaction_sample_map.
     * <strong>Note: </strong> this function is not transaction based.  It is intended to be
     * used inside of a transaction.
     * @param sample the sample to save.
     * @param student the student to save.
     * @return number of mappings inserted.
     */
    public int populateTransactionSampleMap(SampleItem sample, StudentItem student) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }
        if (student == null) { throw new IllegalArgumentException("Student cannot be null."); }
        Integer sampleId = (Integer)sample.getId();
        logDebug("Adding sample/transaction mappings for student ", student.getId(),
                " and sample ", sample.getId());
        Session session = getSession();
        int count = 0;

        String query = buildGetTransactionsQuery(sample, student);

        StringBuffer insertQuery = new StringBuffer(SAMPLE_TRANS_MAP_UPDATE);

        ScrollableResults results = session
            .createQuery(query)
            .setCacheMode(CacheMode.IGNORE)
            .scroll();

        boolean inserted = true;
        while (results.next()) {
            if (!inserted) { insertQuery.append(", "); }
            inserted = false;
            Long transId = (Long)results.get(0);
            insertQuery.append("(" + transId + ", " + sampleId + ")");
            if (++count % BATCH_SIZE == 0) {
                try {
                    session.connection().createStatement().execute(insertQuery.toString());
                } catch (SQLException sqlException) {
                    logger.error("Exception occurred adding transaction_sample_map items",
                            sqlException);
                    throw new LogException(
                            "Exception occurred adding transaction_sample_map items", sqlException);
                }
                insertQuery = new StringBuffer(SAMPLE_TRANS_MAP_UPDATE);
                inserted = true;
            }
        }

        if (!inserted) {
            try {
                session.connection().createStatement().execute(insertQuery.toString());
            } catch (SQLException sqlException) {
                logger.error("Exception occurred adding transaction_sample_map items",
                        sqlException);
                throw new LogException(
                        "Exception occurred adding transaction_sample_map items", sqlException);
            }
        }

        //final flush and clear to make sure any transaction properly picks up the changes.
        session.flush();
        session.clear();
        releaseSession(session);
        return count;
    }

    /**
     * Remove all step transaction <--> sample mappings for the given sample.
     * @param sample the sample to remove all mappings for for.
     * @return number of items deleted.
     */
    public int removeAllTransactionMappings(SampleItem sample) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }
        String query = "delete from transaction_sample_map where sample_id = ?";
        Session session = getSession();
        int numRemoved = 0;
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.setInt(1, ((Integer)sample.getId()).intValue());
            numRemoved = ps.executeUpdate();
        } catch (SQLException sqlException) {
            logger.error("Caught SQL exception attempting to removed transaction->sample mappings"
                    + "for sample " + sample, sqlException);
        }
        releaseSession(session);
        return numRemoved;
    }

    /**
     * Return the max number of skills for transactions in a given sample and skillModel.
     * Used in export.
     * @param sample the sample to test.
     * @param skillModel the skillModel to count against
     * @return Long (max skill count)
     */
    public Long getMaxSkillCount(SampleItem sample, SkillModelItem skillModel) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }
        if (skillModel == null) {
            throw new IllegalArgumentException("SkillModel cannot be null.");
        }

        Session session = getSession();
        String query = "select count(skm.skill_id) as count"
            + " from transaction_skill_map skm"
            + " join transaction_sample_map spm on skm.transaction_id = spm.transaction_id"
            + " join skill sk on skm.skill_id = sk.skill_id"
            + " and sk.skill_model_id = :skill_model_id"
            + " and spm.sample_id = :sample_id"
            + " group by skm.transaction_id order by count desc";
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setLong("skill_model_id", (Long)skillModel.getId());
        sqlQuery.setLong("sample_id", (Integer)sample.getId());
        sqlQuery.addScalar("count", LONG);

        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);

        List skillList = sqlQuery.list();
        releaseSession(session);
        // get the first count from the list and return it - it's the highest skill count
        if (skillList.size() == 0) {
            return new Long(0);
        } else {
            return ((Long)skillList.get(0));
        }
    }

    /**
     * Return the max number of conditions for single transactions in a given sample.
     * Used in export.
     * @param sample the sample to test.
     * @return Long (max condition count)
     */
    public Long getMaxConditionCount(SampleItem sample) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }
        Session session = getSession();
        String query = "select count(tcm.condition_id) as count"
            + " from transaction_sample_map tsm"
            + " join transaction_condition_map tcm on tsm.transaction_id = tcm.transaction_id"
            + " and tsm.sample_id = :sample_id"
            + " group by tcm.transaction_id order by count desc";
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setLong("sample_id", (Integer)sample.getId());
        sqlQuery.addScalar("count", LONG);

        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);

        List conditionList = sqlQuery.list();
        releaseSession(session);
        // get the first count from the list and return it - it's the highest skill count
        if (conditionList.size() == 0) {
            return new Long(0);
        } else {
            return ((Long)conditionList.get(0));
        }
    }

    /**
     * Return the max number of students for single transactions in a given sample.
     * Used in export.
     * @param sample the sample to test.
     * @return Long (max student count)
     */
    public Long getMaxStudentCount(SampleItem sample) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }

        Session session = getSession();
        String query = "select count(distinct sess.student_id) as count"
            + " from transaction_sample_map tsm"
            + " join tutor_transaction tt on tt.transaction_id = tsm.transaction_id"
            + " join session sess on sess.session_id = tt.session_id"
            + " and tsm.sample_id = :sample_id"
            + " group by tsm.transaction_id order by count desc";
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("sample_id", (Integer)sample.getId());
        sqlQuery.addScalar("count", LONG);

        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);

        List studentList = sqlQuery.list();
        releaseSession(session);

        // get the first count from the list and return it - it's the highest student count
        if (studentList.size() == 0) {
            return new Long(0);
        } else {
            return ((Long)studentList.get(0));
        }
    }

    /**
     * Return the max number of attempt selections for a given sample.  Used in export.
     * @param sample the sample to test.
     * @return an Integer (count).
     */
    public Long getMaxAttemptSelectionCount(SampleItem sample) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }
        Session session = getSession();
        Long maxSelectionCount = new Long(0);
        String query = "select count(*) as count from attempt_selection selection"
            + " join tutor_transaction tt on selection.subgoal_attempt_id = tt.subgoal_attempt_id"
            + " join transaction_sample_map tsm on tt.transaction_id = tsm.transaction_id"
            + " where tsm.sample_id = :sampleId"
            + " group by selection.subgoal_attempt_id, tt.transaction_id"
            + " order by count(*) desc";

        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("sampleId", (Integer)sample.getId());
        sqlQuery.addScalar("count", LONG);
        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);
        List selectionList = sqlQuery.list();
        releaseSession(session);

        // get the first count from the list and return it - it's the highest skill count
        if (selectionList.size() == 0) {
            maxSelectionCount = new Long(1);
        } else {
            maxSelectionCount = (Long) selectionList.get(0);
        }
        return maxSelectionCount;
    }

    /**
     * Return the max number of attempt inputs for a given dataset.  Used in export.
     * @param sample the sample to count max inputs for
     * @return an Long (count).
     */
    public Long getMaxAttemptInputCount(SampleItem sample) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }
        Session session = getSession();
        Long maxInputCount = new Long(0);
        String query = "select count(*) as count from attempt_input input"
            + " join tutor_transaction tt on input.subgoal_attempt_id = tt.subgoal_attempt_id"
            + " join transaction_sample_map tsm on tt.transaction_id = tsm.transaction_id"
            + " where tsm.sample_id = :sampleId"
            + " group by input.subgoal_attempt_id, tt.transaction_id"
            + " order by count(*) desc";

        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("sampleId", (Integer)sample.getId());
        sqlQuery.addScalar("count", LONG);
        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);
        List inputList = sqlQuery.list();
        releaseSession(session);

        if (inputList.size() == 0) {
            maxInputCount = new Long(1);
        } else {
            maxInputCount = (Long) inputList.get(0);
        }
        return maxInputCount;
    }

    /**
     * Return the max number of attempt actions for a given dataset.  Used in export.
     * @param sample the sample to count max inputs for
     * @return an Long (count).
     */
    public Long getMaxAttemptActionCount(SampleItem sample) {
        if (sample == null) { throw new IllegalArgumentException("Sample cannot be null."); }
        Session session = getSession();
        Long maxActionCount = new Long(0);
        String query = "select count(*) as count from attempt_action action"
            + " join tutor_transaction tt on action.subgoal_attempt_id = tt.subgoal_attempt_id"
            + " join transaction_sample_map tsm on tt.transaction_id = tsm.transaction_id"
            + " where tsm.sample_id = :sampleId"
            + " group by action.subgoal_attempt_id, tt.transaction_id"
            + " order by count(*) desc";

        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setInteger("sampleId", (Integer)sample.getId());
        sqlQuery.addScalar("count", LONG);
        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);
        List actionList = sqlQuery.list();
        releaseSession(session);
        // get the first count from the list and return it - it's the highest skill count
        if (actionList.size() == 0) {
            maxActionCount = new Long(1);
        } else {
            maxActionCount = (Long) actionList.get(0);
        }
        return maxActionCount;
    }

    /**
     * Builds the end part of the query which contains the conditional information
     * on the requested attributes.
     * @param filterItemList the list of filter items to parse.
     * @return string of query.
     */
    private StringBuffer buildFilterWhereClause(List filterItemList) {
        return getCommonWhereClause(filterItemList, new StringBuffer(""));
    }

    /**
     * Log the stack trace if debugging is enabled.
     * @param throwable an exception
     */
    private void logDebugStacktrace(Throwable throwable) {
        if (logger.isDebugEnabled()) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new java.io.PrintWriter(sw));
            logDebug(sw);
        }
    }

    /** SQL for selecting the unique steps for dataset level and sample. */
    private static final String STEPS_FOR_DATASET_LEVEL_SQL =
        "select distinct(s.subgoal_id), p.problem_name as problem_name, "
        + "s.subgoal_name as subgoal_name "
        + "from tutor_transaction tt "
        + "join transaction_sample_map using (transaction_id) "
        + "join subgoal s using (subgoal_id) "
        + "join problem p on tt.problem_id = p.problem_id "
        + "where sample_id = ? and dataset_level_id = ? "
        + "order by p.problem_name, s.subgoal_name";

    /**
     * Get the unique steps for the dataset level and sample.
     * @param datasetLevelId the id of the dataset level
     * @param sample the sample
     * @param session pass in the session so it can be retained for the life of the result set
     * @param limit used to set the fetch-ahead limit
     * @return result set for the unique steps for the dataset level and sample
     * @throws SQLException if the database explodes
     */
    public ResultSet getStepsForDatasetLevel(Integer datasetLevelId, SampleItem sample,
            Session session, int limit) throws SQLException {
        PreparedStatement stmt =
            session.connection().prepareStatement(STEPS_FOR_DATASET_LEVEL_SQL);

        stmt.setInt(1, (Integer)sample.getId());
        stmt.setInt(2, datasetLevelId);
        stmt.setFetchSize(limit);

        return stmt.executeQuery();
    }

    /** Ordered column names for transaction export. */
    private static final String[] TX_FIELDS = new String[] {
        "row_num",
        "sample_name", "tx_id", "students", "session_tag",
        "transaction_time", "time_zone", "duration",
        "transaction_type_tool", "transaction_subtype_tool", "transaction_type_tutor",
        "transaction_subtype_tutor", "levels", "problem_name", "problem_view", "problem_start_time",
        "subgoal_name", "attempt_at_subgoal", "is_last_attempt", "outcome", "selections",
        "actions", "inputs",
        "feedback_text", "classification", "help_level", "total_num_hints", "conditions", "skills",
        "school_name", "class_name", "custom_fields"
    };

    private static final String[] SAMPLES_PAGE_FIELDS = new String[] { "sample_id", "sample_name" };

    /** Order rows from tx_export using these columns. */
    private static final String TX_ORDER_BY =
        "students, transaction_time, subgoal_attempt_id, attempt_at_subgoal, transaction_id";

    /** all of the stored procedures that we parameterize when performing a transaction export. */
    private static final String[] TX_EXPORT_PROCEDURES = new String[] {
        "updateSampleMetric", "createExportTables", "setTxHeaders", TX_HEADERS, "studentExport",
        "studentExportWithStudent", "studentExportWithBatch", "datasetLevelExport",
        "attemptSAIExport", "conditionExport", "skillExport", "customFieldExport",
        "tx_export_core", "tx_export", "tx_export_with_student", "tx_export_with_student_batch",
        "drop_temporary_export_tables"
    };

    /** all of the SQL functions that we parameterize when performing a transaction export. */
    private static final String[] TX_EXPORT_FUNCTIONS = new String[] {
        "get_version_tx_export_sp", "substr_count", "starts_with", "repeatCols", "delimPad",
        "capitalize", "cleanCFValue", "datasetIdForSample", "maxDatasetLevels", "getSampleMetric",
        "getSampleMetricWithSkillModel", "maxStudentCols", "maxSelectionCols",
        "maxActionCols", "maxInputCols", "maxConditionCols", "maxSkillCols",
        "maxSkillsForModel", "maxCustomFieldCols", "SAICellValue", "hasCustomFields"
    };

    /** delete these temporary tables when the export is finished. */
    private static final String[] SUPPORT_TABLES = {TX_HEADERS, "dataset_level_export",
        "max_skills_export", "custom_field_name_export", "temp_tt_tsm"};

    /** SQL query to get the number of rows in the tx_export table. */
    private static final String TX_EXPORT_COUNT_QUERY =
        "select count(*) as count from tx_export_XXX";

    /**
     * Handles data access for the transaction export.  Maintains a single connection throughout
     * transaction export processing in order to ensure access to temporary tables.
     * @return a transaction export DAO
     */
    public TxExportDao getTxExportDao() { return new TxExportDaoImpl(); }


    /**
     * Get a list of the number of accessible samples by user and dataset.
     * @param userId the userId to check.
     * @param datasetId the datasetId
     * @return a count of the number of accessible samples in the given dataset
     * for the provided user.
     */
    public List getMySampleRows(String userId, Integer datasetId) {

        StringBuffer query = new StringBuffer();
        query.append("SELECT sample_id, sample_name");
        query.append(" FROM sample samp");
        query.append(" JOIN ds_dataset ds USING (dataset_id)");
        query.append(" JOIN project USING (project_id)");
        query.append(" JOIN authorization auth USING (project_id)");
        query.append(" WHERE (auth.user_id = :userId OR auth.user_id='%')");
        query.append(" AND (ds.deleted_flag = false OR ds.deleted_flag is NULL)");
        query.append(" AND ds.dataset_id = :datasetId");
        query.append(" AND (samp.global_flag = 1 OR samp.owner = :userId)");
        if (logger.isTraceEnabled()) {
            logger.trace("findMySamples(" + userId + ") query:" + query.toString());
        }
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setString("userId", userId);
        sqlQuery.setInteger("datasetId", datasetId);

        for (String samplesPageField : SAMPLES_PAGE_FIELDS) {
            sqlQuery.addScalar(samplesPageField, Hibernate.STRING);
        }

        List queryResults = sqlQuery.list();

        releaseSession(session);
        return queryResults;
    }

    /** The query for the Samples page rows. */
    private static final String SAMPLES_PAGE_QUERY =
        "SELECT samp.owner as owner_id, u.email as owner_email, "
            + " samp.global_flag, samp.sample_id, samp.sample_name,"
            + " auth.`level` as auth_level, samp.description as description"
         + " FROM sample samp"
         + " LEFT JOIN ds_dataset ds USING (dataset_id)"
         + " LEFT JOIN project p USING (project_id)"
         + " LEFT JOIN authorization auth ON p.project_id = auth.project_id"
             + " AND samp.owner = auth.user_id "
         + " LEFT JOIN `user` u ON samp.owner = u.user_id"
         + " WHERE (auth.user_id = :userId OR samp.global_flag = 1)"
             + " AND (ds.deleted_flag = false OR ds.deleted_flag is NULL)"
             + " AND (samp.global_flag = 1 OR samp.owner = :userId)"
             + " AND ds.dataset_id = :datasetId";

    /** The DataShop Admin query for the Samples page rows. */
    private static final String DS_ADMIN_SAMPLES_PAGE_QUERY =
        "SELECT samp.owner as owner_id, samp.global_flag, samp.sample_id, samp.sample_name,"
            + " u.email as owner_email, '' as auth_level, samp.description as description"
         + " FROM sample samp"
         + " LEFT JOIN ds_dataset ds USING (dataset_id)"
         + " LEFT JOIN `user` u ON samp.owner = u.user_id"
         + " WHERE (ds.deleted_flag = false OR ds.deleted_flag is NULL)"
         + " AND ds.dataset_id = :datasetId";

    /** Order by clause for special-case 'Student' sorting. */
    private static final String ORDER_BY_STUDENT = " ORDER BY dls.student_name * 1";

    /** Order by clause for special-case 'Overall Score' sorting. */
    private static final String ORDER_BY_OVERALL_SCORE = " ORDER BY dls.computed_overall_score";

    /** Order by clause for special-case 'Row Id' sorting. */
    private static final String ORDER_BY_ROW_ID = " ORDER BY dls.student_index";

    /** The limit and offset portion of the the query. */
    private static final String LIMIT_AND_OFFSET_QUERY = " limit :limit offset :offset";

    /** The query for struggling students, part 1. */
    private static final String STRUGGLING_STUDENT_QUERY =
        "SELECT dls.dl_student_id AS id, student_name AS studentName,"
        + " dls.average AS avg, dls.std_deviation AS stdDev,"
        + " student_index AS studentIndex, dls.computed_overall_score AS overallScore,"
        + " final_grade AS finalGrade"
        + " FROM dl_student dls ";

    /** The WHERE clause for struggling students (provided). */
    private static final String STRUGGLING_STUDENT_WHERE_FINAL =
        " WHERE dls.dl_analysis_id = :analysisId AND dls.final_grade < :scoreThreshold";

    /** The WHERE clause for struggling students (computed). */
    private static final String STRUGGLING_STUDENT_WHERE_OVERALL =
        " WHERE dls.dl_analysis_id = :analysisId AND dls.computed_overall_score < :scoreThreshold";

    /**
     * Find the sample row info for all samples in a dataset except deleted ones.
     * @param datasetId the dataset id
     * @return a List of objects
     */
    public List<SampleRowDto> getDsAdminSampleRowInfo(Long datasetId) {

        List<SampleRowDto> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(DS_ADMIN_SAMPLES_PAGE_QUERY);

            SQLQuery query = session.createSQLQuery(sb.toString());

            query.setParameter("datasetId", datasetId);

            query.addScalar("owner_id", Hibernate.STRING);
            query.addScalar("owner_email", Hibernate.STRING);
            query.addScalar("global_flag", Hibernate.STRING);
            query.addScalar("sample_id", Hibernate.INTEGER);
            query.addScalar("sample_name", Hibernate.STRING);
            query.addScalar("auth_level", Hibernate.STRING);
            query.addScalar("description", Hibernate.STRING);

            List<Object[]> dbResults = query.list();

            result = new ArrayList<SampleRowDto>();


            for (Object[] o : dbResults) {
                SampleRowDto dto = createSampleRowDto(o);
                result.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /**
     * Find the sample row info for global samples and those owned by the user.
     * @param userId the user id
     * @param datasetId the dataset id
     * @return a List of objects
     */
    public List<SampleRowDto> getSampleRowInfo(String userId, Long datasetId) {

        List<SampleRowDto> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(SAMPLES_PAGE_QUERY);

            SQLQuery query = session.createSQLQuery(sb.toString());

            query.setParameter("userId", userId);
            query.setParameter("datasetId", datasetId);

            query.addScalar("owner_id", Hibernate.STRING);
            query.addScalar("owner_email", Hibernate.STRING);
            query.addScalar("global_flag", Hibernate.STRING);
            query.addScalar("sample_id", Hibernate.INTEGER);
            query.addScalar("sample_name", Hibernate.STRING);
            query.addScalar("auth_level", Hibernate.STRING);
            query.addScalar("description", Hibernate.STRING);

            List<Object[]> dbResults = query.list();

            result = new ArrayList<SampleRowDto>();


            for (Object[] o : dbResults) {
                SampleRowDto dto = createSampleRowDto(o);
                result.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /**
     * Helper method to create SampleRowDto from db result.
     */
    private SampleRowDto createSampleRowDto(Object[] o) {

        SampleRowDto item = new SampleRowDto();

        int index = 0;
        item.setOwnerId((String)o[index++]);
        item.setOwnerEmail((String)o[index++]);
        item.setIsGlobal(Integer.parseInt((String)o[index++]));
        item.setSampleId((Integer)o[index++]);
        item.setSampleName((String)o[index++]);
        item.setAuthLevel((String)o[index++]);
        item.setDescription((String)o[index++]);

        return item;
    }



    /**
     * Handles data access for the transaction export.  Maintains a single connection throughout
     * transaction export processing in order to ensure access to temporary tables.
     * @author jimbokun
     */
    public class TxExportDaoImpl implements TxExportDao {
        /** Template for selecting transaction headers row. */
        private static final String SELECT_TX_HDRS = "select %s from tx_headers_XXX";
        /** Template for selecting from transaction export table. */
        private static final String SELECT_TX_EXPORT =
            "select %s from tx_export_XXX order by " + TX_ORDER_BY;

        /**
         * Parameterize the stored procedure name with the sample id.
         * @param procedureName the stored procedure name
         * @param sample the sample
         * @return the stored procedure name parameterized with the sample id
         */
        private String parameterizeSP(String procedureName, SampleItem sample) {
            return procedureName + "_" + sample.getId();
        }

        /**
         * Return the result of appending the sample id to each name.
         * @param names function or stored procedure names
         * @param sample a sample
         * @return the result of appending the sample id to each name
         */
        private List<String> appendSampleId(final String[] names, final SampleItem sample) {
            return new ArrayList<String>() { {
                for (String name : names) { add(parameterizeSP(name, sample)); }
            } };
        }

        /**
         * Replace the secret template string (XXX) in SQL with the sample id.
         * @param sample the sample
         * @param sql some SQL code
         * @return SQL with the secret template string replaced with the sample id
         */
        private String parameterizeSampleId(SampleItem sample, String sql) {
            return sql.replace(TO_REPLACE_STRING, sample.getId().toString());
        }

        /**
         * Parameterize transaction headers or transaction export query with the sample
         * and the columns to select.
         * @param sql the query
         * @param sample the sample
         * @param txCols the columns to select
         * @return the parameterized query
         */
        private String parameterizeTxColumns(String sql, SampleItem sample, List<String> txCols) {
            return parameterizeSampleId(sample, format(sql, join(", ", txCols)));
        }

        /**
         * The column names from the tx_headers table.
         * @param sample the sample to parameterize on
         * @return the column names from the tx_headers table.
         */
        private List<String> getTransactionColumns(final SampleItem sample) {
            String txColsQuery = parameterizeTxColumns(SELECT_TX_HDRS, sample, asList(TX_FIELDS));
            final Object[] cols = getUniqueResultForSQLQuery(txColsQuery, new PrepareQuery() {
                @Override
                public void prepareQuery(SQLQuery query) {
                    for (String txField : TX_FIELDS) { query.addScalar(txField, STRING); }
                }
            }, new Object[] {});
            return new ArrayList<String>() { {
                for (int i = 0; i < TX_FIELDS.length; i++) {
                    if (cols[i] != null) { add(TX_FIELDS[i]); }
                }
            } };
        }

        /**
         * Prepare a call to the SQL stored procedure to generate transaction export data for the
         * given sample and the batch of students specified by limit and offset.
         * We need this so the user has the option to cancel the export.
         * canceled or finishes.
         * @param sample the sample
         * @param limit the maximum number of students to process in this batch
         * @param offset the index of the start of this batch of students
         * @return the prepared call
         * @throws SQLException if something goes wrong, heaven forbid
         */
        public CallableStatement prepareTxExportStatement(SampleItem sample,
                int limit, int offset) throws SQLException {
            String call = buildSPCall("tx_export_with_student_batch_" + sample.getId(),
                    sample.getId(), limit, offset);
            return getSession().connection().prepareCall(call);
        }

        /**
         * Call the SQL stored procedure to generate transaction export headers for the
         * given sample.
         * @param sample the sample
         * @throws SQLException if something goes wrong, heaven forbid
         */
        public void callTxHeadersSP(SampleItem sample)
        throws SQLException {
            callSP(parameterizeSP(TX_HEADERS, sample), sample.getId());
        }

        /**
         * Load the transaction export stored procedure file, customized for this sample.
         * @param filePath path to the transaction export stored procedure file
         * @param sample the sample for which we want to customize the SP file
         * @return whether execution was successful
         * @throws IOException if file at filePath does not exist
         */
        public boolean loadTxExportSP(String filePath, SampleItem sample) throws IOException {
            logDebug("Loading customized stored procedures for:", sample.getNameAndId(),
                    " from file ", filePath);
            return loadCustomizedSP(openAndReplaceSequence(filePath, TO_REPLACE_STRING,
                    sample.getId().toString()));
        }

        /**
         * Return results of the tx_export table created by the tx_export stored procedure.
         * (ResultSet allows us to page through the results as needed, instead of
         * fetching all the results at once.)
         * @param sample parameterize query with the sample id
         * created for transaction export
         * @param limit maximum number of results to pull from the database at a time.
         * @return ResultSet representing the contents of the tx_export table.
         * @throws SQLException thrown by methods on PreparedStatement
         */
        public ResultSet getSPTxs(SampleItem sample, Integer limit)
        throws SQLException {
            String sql = parameterizeTxColumns(SELECT_TX_EXPORT, sample,
                    getTransactionColumns(sample));
            PreparedStatement stmt = getSession().connection().prepareStatement(sql);
            stmt.setFetchSize(limit);
            return stmt.executeQuery();
        }

        /**
         * Headers to display in the transaction export, taken from the tx_headers table.
         * @param sample parameterize query with the sample id
         * created for transaction export
         * @return headers to display in the transaction export, taken from the tx_headers table.
         */
        public Object[] getSPTxHeaders(final SampleItem sample) {
            final List<String> txCols = getTransactionColumns(sample);
            final String sql = parameterizeTxColumns(SELECT_TX_HDRS, sample, txCols);

            return getUniqueResultForSQLQuery(sql, new PrepareQuery() {
                public void prepareQuery(SQLQuery query) {
                    for (String txCol : txCols) { query.addScalar(txCol, STRING); }
                }
            }, new Object[] {});
        }

        /**
         * Get the number of rows in the tx_export table.
         * @param sample the sample to parameterize on
         * @return the number of rows in the tx_export table
         */
        public long getTxExportCount(SampleItem sample) {
            String sql = parameterizeSampleId(sample, TX_EXPORT_COUNT_QUERY);

            return getLongForSQL(sql, new PrepareQuery() {
                public void prepareQuery(SQLQuery query) {
                    query.addScalar("count", LONG).setCacheable(false);
                };
            }, -1L);
        }

        /**
         * Drop customized tables and procedures for export, and delete the
         * "started transaction export" message.
         * @param sample the sample
         * @param dataset the dataset
         * @throws SQLException if something goes wrong deleting the procedures and tables
         */
        public void cleanupExport(SampleItem sample, DatasetItem dataset)
        throws SQLException {
            DaoFactory.DEFAULT.getDatasetSystemLogDao().
            removeTransactionExportStartedMessage(dataset, sample);
            dropStoredProcedures(appendSampleId(TX_EXPORT_PROCEDURES, sample),
                    appendSampleId(TX_EXPORT_FUNCTIONS, sample));
            dropTables(appendSampleId(SUPPORT_TABLES, sample));
        }
    }

    /**
     * Find by sample name.
     * @param sampleName the sample name
     */
    public List<SampleItem> find(String sampleName) {
        return getHibernateTemplate().find(
                "from SampleItem sample where sample.sampleName = ?", sampleName);
    }
}
