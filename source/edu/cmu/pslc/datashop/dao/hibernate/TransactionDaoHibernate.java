/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SessionDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.type.CorrectFlag;


/**
 * Hibernate and Spring implementation of the TransactionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15863 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-02-27 13:15:42 -0500 (Wed, 27 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TransactionDaoHibernate extends AbstractDaoHibernate implements TransactionDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a TransactionItem by id.
     * @param id The id of the user.
     * @return the matching TransactionItem or null if none found
     */
    public TransactionItem get(Long id) {
        return (TransactionItem)get(TransactionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(TransactionItem.class);
    }

    /**
     * Standard find for an TransactionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired TransactionItem.
     * @return the matching TransactionItem.
     */
    public TransactionItem find(Long id) {
        return (TransactionItem)find(TransactionItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Find all the transactions for the given student and subgoal.
     * @param studentItem the student item
     * @param subgoalItem the subgoal item
     * @return a list of transaction items
     */
    public List find(StudentItem studentItem, SubgoalItem subgoalItem) {
        DetachedCriteria query = DetachedCriteria.forClass(TransactionItem.class);
        query.add(Restrictions.eq("subgoal", subgoalItem));
        query.createCriteria("session")
             .createCriteria("student")
             .add(Restrictions.eq("id", studentItem.getId()));
        query.addOrder(Property.forName("transactionTime").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /** HQL Query for getting the next attempt at a given subgoal for a given student */
    private static final String GET_NEXT_ATTEMPT_AT_SUBGOAL =
           "select max(tt.attemptAtSubgoal)"
        + " from TransactionItem tt"
        + " join tt.session sess"
        + " join sess.student stud"
        + " join tt.problem p"
        + " where stud.id = ?"
        + " and tt.subgoal.id = ?"
        + " and (p.problemEvents is empty or tt.transactionTime >="
        + "(select max(startTime) from p.problemEvents where eventFlag = 0))"
        + " group by tt.subgoal";

    /**
     * Return the max subgoal attempt for the given student and subgoal,
     * regardless of the session id.
     * Note that this is for the TutorMessageConverter.
     * @param studentItem the student item
     * @param subgoalItem the subgoal item
     * @return the max subgoal attempt number if an attempt has been made
     */
    public Integer getNextAttemptAtSubgoal(StudentItem studentItem, SubgoalItem subgoalItem) {
        List<Integer> results = getHibernateTemplate().find(GET_NEXT_ATTEMPT_AT_SUBGOAL,
                new Object[] {studentItem.getId(), subgoalItem.getId()});

        return (results.size() > 0 ? results.get(0) : 0) + 1;
    }

    /** The Native SQL to call the Fix Attempt At Subgoal stored procedure. */
    private static final String FIX_AAS_SP_NATIVE_SQL
            = "call update_tx_one_ds_aas(:datasetId)";

    /**
     * Call the stored procedure to fix the attempt at subgoal for all the transactions
     * for a given dataset.
     * @param datasetItem the dataset to be updated
     * @return the number of rows updated
     */
    public int callFixAttemptAtSubgoalSP(DatasetItem datasetItem) {
        if (datasetItem == null) {
            throw new IllegalArgumentException(
                    "callFixAttemptAtSubgoalSP(): Dataset cannot be null.");
        }
        int numRows = 0;
        Session session = null;

        try {
            //this opens a session, make sure to release
            session = getSession();

            //build the native SQL query
            SQLQuery sqlQuery = session.createSQLQuery(FIX_AAS_SP_NATIVE_SQL.toString());
            sqlQuery.setInteger("datasetId", ((Integer)datasetItem.getId()).intValue());

            //run the query
            numRows = sqlQuery.executeUpdate();

        } finally {
            releaseSession(session);
        }
        return numRows;
    }

    /**
     * Return the SSSS 2D array that contains studentID, success, step, and skill.
     * Note that this is for the Learning Factors Analysis tool.
     * @param datasetItem the selected dataset
     * @param skillModelItem the selected skill model
     * @return String[][]
     */
    public String[][] getSSSS(DatasetItem datasetItem,
                              SkillModelItem skillModelItem) {
        String[][] ssss = null;
        List ssssList = new LinkedList();

        List resultsList = getSSSSTransactions(datasetItem, skillModelItem);

        for (int i = 0, n = resultsList.size(); i < n; i++) {
            Object[] objArray = (Object[])resultsList.get(i);

            String[] ssssRow = new String[NUM_OBJECTS - 1];

            //get the student id
            ssssRow[STUDENT_IDX] = ((Long)objArray[STUDENT_IDX]).toString();

            //get the success flag from the outcome
            String outcome = (String)objArray[SUCCESS_IDX];
            CorrectFlag attemptType = CorrectFlag.getInstance(outcome);
            if (attemptType != null) {
                if (attemptType.equals(CorrectFlag.INCORRECT)
                        || attemptType.equals(CorrectFlag.HINT)) {
                    outcome = "0";
                } else if (attemptType.equals(CorrectFlag.CORRECT)) {
                    outcome = "1";
                } else {
                    outcome = "2";
                }
            } else {
                outcome = "2"; //must be "UNKNOWN", discard
                logger.warn("NULL or Unknown subgoal attempt while getting SSSS :: " + attemptType);
            }
            ssssRow[SUCCESS_IDX] = outcome;

            //get the step
            String problem = ((Long)objArray[PROBLEM_IDX]).toString();
            String stepId  = ((Long)objArray[STEP_IDX]).toString();
            ssssRow[STEP_IDX] = problem + "(" + stepId + ")";

            //get the skill
            ssssRow[SKILL_IDX] = ((Long)objArray[SKILL_IDX]).toString();

            ssssList.add(ssssRow);
        }

        ssss = new String[ssssList.size()][NUM_OBJECTS - 1];
        for (int i = 0; i < ssss.length; i++) {
          ssss[i] = (String[])ssssList.get(i);
        }
        return ssss;
    }

    /** Constant. */
    private static final int NUM_OBJECTS = 5;
    /** Constant. */
    private static final int STUDENT_IDX = 0;
    /** Constant. */
    private static final int SUCCESS_IDX = 1;
    /** Constant. */
    private static final int STEP_IDX = 2;
    /** Constant. */
    private static final int SKILL_IDX = 3;
    /** Constant. */
    private static final int PROBLEM_IDX = 4;

    /**
     * MySQL 5.x query to get the SSSS
     * @deprecated does not take into account the problem-event information.
     */
    private static final String GET_SSSS_NATIVE_MYSQL_QUERY =
        "select sess.student_id as studentId,"
        + " sa.correct_flag as firstAttempt,"
        + " sub.subgoal_id as stepId,"
        + " sk.skill_id as skillId,"
        + " sub.problem_id as problemId,"
        + " ifnull((select min(tt2.transaction_time)"
            + " from tutor_transaction tt2"
            + " join session sess2 on tt2.session_id = sess2.session_id"
            + " join subgoal_attempt sa2 on sa2.subgoal_attempt_id = tt2.subgoal_attempt_id"
            + " where tt2.subgoal_id = stepId and sess.student_id = studentId"
            + " and sa2.correct_flag='correct'"
            + " group by tt2.subgoal_id), max(tt.transaction_time)) as stepTime"
        + " from tutor_transaction tt"
        + " join subgoal sub on tt.subgoal_id = sub.subgoal_id"
        + " join subgoal_skill_map ssm on sub.subgoal_id = ssm.subgoal_id"
        + " join skill sk on ssm.skill_id = sk.skill_id"
        + " join skill_model sm on sk.skill_model_id = sm.skill_model_id"
        + " join session sess on tt.session_id = sess.session_id"
        + " join subgoal_attempt sa on tt.subgoal_attempt_id = sa.subgoal_attempt_id"
        + " where tt.dataset_id = :datasetId"
        + " and sk.skill_model_id = :skillModelId"
        + " group by tt.subgoal_id, student_id, sk.skill_id, sk.skill_model_id"
        + " order by sess.student_id, stepTime, ssm.skill_id, tt.subgoal_id, tt.attempt_at_subgoal";

    /**
     * Run an HQL query to get the necessary data for the SSSS array.
     * Note that this gets the problem id as well as the subgoal id.
     * @param datasetItem the dataset
     * @param skillModelItem the skill model
     * @return the raw SSSSP list
     * @deprecated does not take into account the problem-event information.
     */
    private List getSSSSTransactions(DatasetItem datasetItem, SkillModelItem skillModelItem) {
        Session session = getSession();

        SQLQuery sqlQuery = session.createSQLQuery(GET_SSSS_NATIVE_MYSQL_QUERY);

        sqlQuery.setInteger("datasetId", ((Integer)datasetItem.getId()).intValue());
        sqlQuery.setLong("skillModelId", ((Long)skillModelItem.getId()).longValue());

        //indicate which columns map to which types.
        sqlQuery.addScalar("studentId", Hibernate.LONG);
        sqlQuery.addScalar("firstAttempt", Hibernate.STRING);
        sqlQuery.addScalar("stepId", Hibernate.LONG);
        sqlQuery.addScalar("skillId", Hibernate.LONG);
        sqlQuery.addScalar("problemId", Hibernate.LONG);

        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);

        logTrace(sqlQuery.toString());

        List results = sqlQuery.list();
        releaseSession(session);
        return results;
    }

    /** Query for getting the number of transactions in a dataset */
    private static final String COUNT_QUERY =
        "select count(distinct trans) from TransactionItem trans"
        + " where trans.dataset.id = ?";

    /**
     * Gets the number of transactions in a dataset.
     * @param dataset the dataset to # transactions in.
     * @return Number of transactions in the dataset as an Integer.
     */
    public Long count(DatasetItem dataset) {

        logTrace("Getting Number of Transactions in Dataset with query :: " + COUNT_QUERY);

        Long numResults = (Long)getHibernateTemplate().find(
                COUNT_QUERY, dataset.getId()).get(0);
        return numResults;
    }

    /**
     * Gets the skill item identifiers for the given transaction item.
     * @param transactionItem the transaction item to get skill identifiers for.
     * @return A list of skill identifiers.
     */
    public List getSkillsForTx(TransactionItem transactionItem) {
        Session session = getSession();
        String query = "select map.skill_id as skillId from"
            + " transaction_skill_map map"
            + " where map.transaction_id = :transID";
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setLong("transID", (Long) transactionItem.getId());
        sqlQuery.addScalar("skillId", Hibernate.LONG);
        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);
        List results = sqlQuery.list();
        releaseSession(session);
        return results;
    }

    /**
     * Get a list of custom field item identifiers for the given transaction item.
     * @param transactionItem the transaction item to find custom fields for.
     * @return a list containing custom field item identifiers
     */
    public List getCustomFieldsForTx(TransactionItem transactionItem) {
        Session session = getSession();
        String query = "select custom_field_id as customFieldId from ("
                        + " select cf.custom_field_id, cf.custom_field_name, cfTx.value"
                        + " from custom_field cf, cf_tx_level cfTx"
                        + " where cf.custom_field_id = cfTx.custom_field_id and cfTx.transaction_id = :transID"
                        + " order by value, custom_field_name ) A";
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setLong("transID", (Long) transactionItem.getId());
        sqlQuery.addScalar("customFieldId", Hibernate.LONG);
        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);
        List results = sqlQuery.list();
        releaseSession(session);
        return results;
    }

    /**
     * Get a list of condition item identifiers for the given transaction item.
     * @param transactionItem the transaction item to find conditions for.
     * @return a list containing condition item identifiers.
     */
    public List getConditionsForTx(TransactionItem transactionItem) {
        Session session = getSession();
        String query = "select map.condition_id as conditionId from"
            + " transaction_condition_map map"
            + " where map.transaction_id = :transID";
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setLong("transID", (Long) transactionItem.getId());
        sqlQuery.addScalar("conditionId", Hibernate.LONG);

        sqlQuery.setCacheable(false);
        sqlQuery.setCacheMode(CacheMode.IGNORE);

        List results = sqlQuery.list();
        releaseSession(session);
        return results;
    }

    /** Find tool types query */
    private static final String FIND_TOOL_TYPES =
        "select distinct tt.transactionTypeTool from TransactionItem tt "
            + "where tt.dataset.id = ? "
            + "and upper(tt.transactionTypeTool) like ? ";

    /** Find tutor types query */
    private static final String FIND_TUTOR_TYPES =
        "select distinct tt.transactionTypeTutor from TransactionItem tt "
            + "where tt.dataset.id = ? "
            + "and upper(tt.transactionTypeTutor) like ? ";

    /** Find tool subtypes query */
    private static final String FIND_TOOL_SUBTYPES =
        "select distinct tt.transactionSubtypeTool from TransactionItem tt "
            + "where tt.dataset.id = ? "
            + "and upper(tt.transactionSubtypeTool) like ? ";

    /** Find tool subtypes query */
    private static final String FIND_TUTOR_SUBTYPES =
        "select distinct tt.transactionSubtypeTutor from TransactionItem tt "
            + "where tt.dataset.id = ? "
            + "and upper(tt.transactionSubtypeTutor) like ? ";

    /**
     * Gets a list of transactions in the dataset who's tutor type match all
     * or a portion of the toMatch parameter.
     * @param typeField the specific type field to match.
     * @param toMatch A string to match.
     * @param dataset the dataset item to search in.
     * @param matchAny boolean value indicating whether to only look for matches from
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching items sorted by Name.
     */
    public List findMatchingTypes(
            String typeField, String toMatch, DatasetItem dataset, boolean matchAny) {

        String query = null;
        if (typeField.equals("transactionTypeTool")) {
            query = FIND_TOOL_TYPES;
        } else if (typeField.equals("transactionTypeTutor")) {
            query = FIND_TUTOR_TYPES;
        } else if (typeField.equals("transactionSubtypeTool")) {
            query = FIND_TOOL_SUBTYPES;
        } else if (typeField.equals("transactionSubtypeTutor")) {
            query = FIND_TUTOR_SUBTYPES;
        } else {
            throw new IllegalArgumentException("typeField does not match any expected values. "
                    + "'" + typeField + "'");
        }

        toMatch = matchAny ? "%" + toMatch.toUpperCase() + "%" : toMatch.toUpperCase() + "%";

        Object[] paramArray = new Object[2];
        paramArray[0] = dataset.getId();
        paramArray[1] = toMatch;

        return getHibernateTemplate().find(
                query, paramArray);
    }

    /** 'calculate_tx_duration' stored procedure name. */
    private static final String CALC_TX_DURATION_SP_NAME = "calculate_tx_duration";
    /** Batch size for tx duration processing '1000'. */
    private static final int CALC_TX_DURATION_BATCH_SIZE = 1000;

    /**
     * Call the calculate_tx_duration stored procedure to fill in transaction_duration
     * values in the tutor_transaction table.  The stored procedure will take the provided
     * dataset item, find all transactions for it, and calculate durations for each tx.
     * @param dataset the dataset we wish to process.
     * @return true if successful, false otherwise.
     */
    public boolean callCalculateTxDurationSP(DatasetItem dataset) {
        return callCalculateTxDurationSP(dataset, null);
    }

    /**
     * Call the calculate_tx_duration stored procedure to fill in transaction_duration
     * values in the tutor_transaction table.  The stored procedure will take the provided
     * dataset item, find all transactions for it, and calculate durations for each tx.
     * @param dataset the dataset we wish to process.
     * @param batchSize specifies how large batches should be. If null, defaults to
     *      CALC_TX_DURATION_BATCH_SIZE.
     * @return true if successful, false otherwise.
     */
    public boolean callCalculateTxDurationSP(DatasetItem dataset, Integer batchSize) {
        boolean result = true;
        SessionDao sessionDao = DaoFactory.DEFAULT.getSessionDao();
        long totalSessions = sessionDao.getNumSessionsInDataset(dataset);
        batchSize = batchSize == null ? CALC_TX_DURATION_BATCH_SIZE : batchSize;

        for (int batchOffset = 0; batchOffset < totalSessions;
                batchOffset += batchSize) {
            String query  = buildSPCall(CALC_TX_DURATION_SP_NAME, dataset.getId(),
                    batchOffset, batchSize);
            logger.info("callCalculateTxDurationSP offset:: " + batchOffset + ", limit:: "
                    + batchSize + " out of " + totalSessions + " sessions.");
            try {
                callSP(query);
            } catch (SQLException exception) {
                result = false;
                logger.error("Exception caught while executing the "
                    + CALC_TX_DURATION_SP_NAME + " stored procedure for "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ").",
                    exception);
                break;
            }
        }
        return result;
    }

    /** Constant for name of stored procedure: 'insert_total_stud_milliseconds'. */
    private static final String CALC_TOTAL_STUDENT_MILLISECONDS_SP_NAME
                                = "insert_total_stud_milliseconds";

    /**
     * Call the calculate_tx_duration stored procedure to fill in transaction_duration
     * values in the tutor_transaction table.  The stored procedure will take the provided
     * dataset item, find all transactions for it, and calculate durations for each transaction.
     * @return true if successful, false otherwise.
     */
    public boolean callCalculateTotalStudMillisecondsSP() {
        boolean result = true;

        String query  = "call " + CALC_TOTAL_STUDENT_MILLISECONDS_SP_NAME + "()";
        logger.info("callCalculateTotalStudMillisecondsSP");
        try {
            callSP(query);
        } catch (SQLException exception) {
            result = false;
            logger.error("Exception caught while executing the "
                    + CALC_TOTAL_STUDENT_MILLISECONDS_SP_NAME + " stored procedure.",
                    exception);
        }

        return result;
    }

    /**
     * Delete the transactions for a dataset.
     * @param datasetItem the dataset item
     * @return the number of transactions deleted
     */
    public int deleteByDataset(DatasetItem datasetItem) {
        if (datasetItem == null) {
            throw new IllegalArgumentException("Dataset cannot be null.");
        }
        int rowCount = 0;
        String query = "delete from tutor_transaction where dataset_id = ?";
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.setLong(1, ((Integer)datasetItem.getId()).longValue());
            rowCount = ps.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("deleteByDataset (Dataset " + datasetItem.getId()
                        + "). Deleted " + rowCount + " rows.");
            }
        } catch (SQLException exception) {
            logger.error("deleteByDataset (Dataset " + datasetItem.getId()
                    + ") SQLException occurred.", exception);
        } finally {
            releaseSession(session);
        }
        return rowCount;
    }

    /** 'calculate_tx_duration' stored procedure name. */
    private static final String DELETE_DATASET_SP_NAME = "purge_deleted_datasets";

    /**
     * Deletes datasets marked as deleted using the purge_deleted_datasets SP.
     * @param datasetItem the dataset item
     * @return the number of deleted transactions for this datasetitem
     */
    public Integer callPurgeDeletedDatasetsSP(DatasetItem datasetItem) {
        if (datasetItem == null) {
            throw new IllegalArgumentException("Dataset cannot be null.");
        }
        Integer deletedTransactionCount = null;
        String query  = buildSPCall(DELETE_DATASET_SP_NAME, datasetItem.getId());
        logger.info("callDeleteDatasetSP:: purge datasets marked for deletion");
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            deletedTransactionCount = ps.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("callPurgeDeletedDatasetsSP (Dataset " + datasetItem.getId()
                        + "). Deleted " + deletedTransactionCount + " rows.");
            }
        } catch (SQLException exception) {
            logger.error("Exception caught while executing the "
                + DELETE_DATASET_SP_NAME + " stored procedure.",
                exception);
        } finally {
            releaseSession(session);
        }
        return deletedTransactionCount;

    } // end doQuery

    /**
     * Get a list of all transactions in a dataset.
     * @param dataset the dataset to get the transactions in.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return List of all transactions in the dataset between the offset and the limit.
     */
    public List find(DatasetItem dataset, Integer limit, Integer offset) {

        String query = "select txn from TransactionItem txn"
            + " where txn.dataset.id = " + dataset.getId();

        CallbackCreatorHelper helperCreator = new CallbackCreatorHelper(query.toString(),
                offset.intValue(), limit.intValue());

        HibernateCallback callback = helperCreator.getCallback();
        logTrace("querying with callback :: Limit: "
                 + limit + " Offset: " + offset + " query: "  + query);
        List results = getHibernateTemplate().executeFind(callback);

        return results;
    }

    /**
     * Get a list of all transactions in a dataset that are missing a GUID.
     * @param dataset the dataset to get the transactions in.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return List of all transactions in the dataset between the offset and the limit.
     */
    public List findNullGUID(DatasetItem dataset, Integer limit, Integer offset) {

        String query = "select txn from TransactionItem txn"
            + " where txn.dataset.id = " + dataset.getId()
            + " and txn.guid is null";

        CallbackCreatorHelper helperCreator = new CallbackCreatorHelper(query.toString(),
                offset.intValue(), limit.intValue());

        HibernateCallback callback = helperCreator.getCallback();
        logTrace("querying with callback :: Limit: "
                 + limit + " Offset: " + offset + " query: "  + query);
        List results = getHibernateTemplate().executeFind(callback);

        return results;
    }

    /** Get a date string to hash. */
    private static final FastDateFormat HASH_FORMAT
        = FastDateFormat.getInstance("yyyymmdd HH:mm:ss.SSS");

    /** Max length of the GUID. */
    private static final int GUID_LEN = 32;

    /**Generates a unique identifier for a transaction.
     * Use a random number and current time to the precision of milliseconds.
     * @return String that is the identifier.
     */
    public String generateGUID() {
            Random randomGenerator = new Random();
            String hash =  DigestUtils.shaHex(HASH_FORMAT.format(new Date())
                            + randomGenerator.nextInt());
            return hash.substring(0, GUID_LEN);
    }

    /**
     * Find TransactionItem given GUID.
     * @param guid the transaction GUID
     * @return the transaction item
     */
    public TransactionItem findByGUID(String guid) {
        return (TransactionItem)findWithQuery("from TransactionItem t where t.guid = ?", guid);
    }

    /** Constant SQL query for retrieving number of problems in a sample. */
    private static final String PROBLEM_SAMPLE_COUNT_QUERY =
        "SELECT COUNT(distinct tt.problem) FROM TransactionItem tt "
            + " JOIN tt.samples s WHERE s.id = ?";

    /**
     * Get the number of problems for the specified sample.
     * @param sampleId the sample id
     * @return Long problem count
     */
    public Long getNumProblems(Integer sampleId) {

        Object[] params = {sampleId};
        Long theCount =
            (Long) getHibernateTemplate().find(PROBLEM_SAMPLE_COUNT_QUERY, params).get(0);
        if (theCount == null) {
            theCount = Long.valueOf(0);
        }
        return theCount;
    }

} // end TransactionDaoHibernate.java
