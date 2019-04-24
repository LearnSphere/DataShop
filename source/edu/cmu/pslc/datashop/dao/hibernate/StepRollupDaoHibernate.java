/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static edu.cmu.pslc.datashop.helper.UserLogger.EXPORT_STEP_ROLLUP;
import static edu.cmu.pslc.datashop.servlet.AggregatorBeanAssistant.getFunctions;
import static edu.cmu.pslc.datashop.servlet.AggregatorBeanAssistant.getProcedures;
import static edu.cmu.pslc.datashop.servlet.AggregatorBeanAssistant.getTemporaryTables;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.web.util.HtmlUtils;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate.PrepareQuery;
import edu.cmu.pslc.datashop.dto.StepRollupExportOptions;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StepRollupItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.type.CorrectFlag;
import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.util.CollectionUtils.append;
import static edu.cmu.pslc.datashop.util.CollectionUtils.partition;
import static edu.cmu.pslc.datashop.type.CorrectFlag.CORRECT;
import static edu.cmu.pslc.datashop.type.CorrectFlag.INCORRECT;
import static edu.cmu.pslc.datashop.type.CorrectFlag.HINT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.LONG;

/**
 * Hibernate and Spring implementation of the StepRollupDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15747 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-12-11 09:46:48 -0500 (Tue, 11 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepRollupDaoHibernate extends AbstractDaoHibernate implements StepRollupDao {
    /** Number of objects in a SSSS row (one for each S). */
    private static final int SSSS_NUM_OBJECTS = 4;

    /** Number of objects in a SSSVS row (one for each S). */
    private static final int SSSVS_NUM_OBJECTS = 5;

    /** Logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a StepRollupItem by id.
     * @param id The id of the step rollup.
     * @return the matching StepRollupItem or null if none found
     */
    public StepRollupItem get(Long id) {
        return (StepRollupItem)get(StepRollupItem.class, id);
    }

    /**
     * Standard "find all" for step rollup items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(StepRollupItem.class);
    }

    /**
     * Standard find for an StepRollupItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired StepRollupItem.
     * @return the matching StepRollupItem.
     */
    public StepRollupItem find(Long id) {
        return (StepRollupItem)find(StepRollupItem.class, id);
    }

    /** HQL query for finding distinct by dataset. */
    private static final String FIND_BY_DATASET =
        "select distinct sri from StepRollupItem sri"
        + " where sri.dataset.id = ?";

    /**
     * Get all StepRollup Items inside of a given dataset.
     * @param datasetItem the dataset to get stepRollups for.
     * @return List of step rollup items.
     */
    public List get(DatasetItem datasetItem) {
        return getHibernateTemplate().find(FIND_BY_DATASET, datasetItem.getId());
    }

    /** HQL string for a bulk delete.     */
    private static final String DELETE_ALL_SRI_BY_DATASET =
        "delete StepRollupItem where dataset.id = ?";
    /**
     * Remove all step roll-ups inside the given dataset.
     * @param datasetItem the dataset to remove all StepRollups for.
     * @return number of step rollup items deleted.
     */
    public int removeAll(DatasetItem datasetItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing all step roll-ups in dataset " + datasetItem.getId());
        }
        return getHibernateTemplate().bulkUpdate(DELETE_ALL_SRI_BY_DATASET, datasetItem.getId());
    }

    /** HQL string for a bulk delete.     */
    private static final String DELETE_ALL_SRI_BY_SAMPLE =
        "delete StepRollupItem where sample.id = ?";
    /**
     * Remove all step roll-ups for the given sample.
     * @param sampleItem the sample to remove all StepRollups for.
     * @return number of step rollup items deleted.
     */
    public int removeAll(SampleItem sampleItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing all step roll-ups for sample " + sampleItem);
        }
        int numRollups = getHibernateTemplate().bulkUpdate(
                DELETE_ALL_SRI_BY_SAMPLE, sampleItem.getId());
        if (logger.isDebugEnabled()) {
            logger.debug("Removed " + numRollups + " step roll-ups for sample " + sampleItem);
        }
        return numRollups;
    }

    /** HQL string for a bulk delete.     */
    private static final String DELETE_ALL_SRI_BY_MODEL = "delete StepRollupItem where skillModel.id = ?";

    /**
     * Remove all step roll-ups for the given KC model.
     * @param modelItem the KC model to remove all StepRollups for.
     * @return number of step rollup items deleted.
     */
    public int removeAll(SkillModelItem modelItem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing all step roll-ups for KC model " + modelItem);
        }
        int numRollups = getHibernateTemplate().bulkUpdate(DELETE_ALL_SRI_BY_MODEL,
                                                           modelItem.getId());
        if (logger.isDebugEnabled()) {
            logger.debug("Removed " + numRollups + " step roll-ups for model " + modelItem);
        }
        return numRollups;
    }

    /**
     * Native MySql query for finding all step
     * for a given sample.
     */
    private static final String GET_NUM_WITH_NO_SKILL_MODELS_MYSQL =
        "select count(distinct sri.sample_id, sri.student_id, sri.step_id, sri.problem_view)"
        + " as numRollups from step_rollup sri"
        + " where sri.sample_id = :sampleId";

    /** {@inheritDoc} */
    public Integer getNumStepRollups(SampleItem sampleItem, StepRollupExportOptions options) {

        if (sampleItem == null) {
            throw new IllegalArgumentException("null is not an allowed value for the sampleItem");
        }

        options = (options == null) ? new StepRollupExportOptions() : options;
        SkillModelItem primaryModel = options.getModel();

        StringBuffer query = new StringBuffer();
        query.append(GET_NUM_WITH_NO_SKILL_MODELS_MYSQL);

        boolean countFlag = true;
        StringBuffer whereClause = buildWhereOptionsForPreview(sampleItem, options, countFlag);
        if (whereClause == null) { return 0; }

        query.append(whereClause);

        if (logger.isTraceEnabled()) {
            logger.trace("Native SQL Query: " + query);
        }
        //build the native SQL query.
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setInteger("sampleId", ((Integer)sampleItem.getId()).intValue());

        sqlQuery.addScalar("numRollups", INTEGER);

        if (logger.isTraceEnabled()) {
            logger.trace("getNumStepRollups sqlQuery [" + sqlQuery + "]");
        }

        List dbResults = sqlQuery.list();
        releaseSession(session);
        if (dbResults.size() > 0) {
            return (Integer)dbResults.get(0);
        } else {
            return Integer.valueOf(0);
        }
    }

    /**
     * Native MySql query for counting the number of observations of a skill model
     * for a given sample.
     */
    private static final String GET_NUM_OBSERVATIONS =
        "select count(distinct student_id, step_id, problem_view) as numObs from step_rollup sri"
        + " where sri.sample_id = :sampleId"
        + " and sri.skill_model_id = :modelId"
        + " and sri.skill_id is not null";
    /**
     * Run a native MySQL query to get the number of observations from the step rollup
     * table for the all data sample of the given dataset and given skill model.
     * @param datasetItem the given dataset
     * @param skillModelItem the given skill model
     * @return the number of observations, or 0 if no results returned
     */
    public Long getNumberObservations(DatasetItem datasetItem, SkillModelItem skillModelItem) {

        Long numObservations = new Long(0);

        if (skillModelItem == null) {
            throw new IllegalArgumentException("the skill model item cannot be null");
        }

        StringBuffer query = new StringBuffer();
        query.append(GET_NUM_OBSERVATIONS);

        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem allDataSample = sampleDao.findOrCreateDefaultSample(datasetItem);

        // Check that an All Data sample exists because there could be zero transactions
        // and return zero observations if not.
        if (allDataSample == null) {
            logger.error("This dataset has no " + SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME
                    + " sample and therefore no observations, cannot run LFA.");
            return numObservations;
        } else {
            logger.debug(SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME + " sample found.");
        }

        Session session = null;
        try {
            session = getSession();
            //build the native SQL query.
            SQLQuery sqlQuery = session.createSQLQuery(query.toString());
            sqlQuery.setInteger("sampleId", ((Integer)allDataSample.getId()).intValue());
            sqlQuery.setLong("modelId", ((Long)skillModelItem.getId()).longValue());
            sqlQuery.addScalar("numObs", LONG);

            if (logger.isTraceEnabled()) {
                logger.trace("getNumberObservations sqlQuery [" + sqlQuery + "]");
            }

            List dbResults = sqlQuery.list();

            if (dbResults.size() > 0) {
                numObservations = (Long)dbResults.get(0);
            }
        } finally {
            releaseSession(session);
        }

        return numObservations;
    }

    /**
     * Native MySql query for counting the number of observations in a skill
     * for a given sample.
     */
    private static final String GET_NUM_OBSERVATIONS_BY_SKILL =
        "select count(distinct student_id, step_id, problem_view) as numObs from step_rollup sri"
        + " where sri.sample_id = :sampleId"
        + " and sri.skill_id = :skillId";

    /**
     * Run a native MySQL query to get the number of observations from the step rollup
     * table for the 'All Data' sample of the given dataset and skill.
     * @param datasetItem the given dataset
     * @param skillItem the given skill
     * @return the number of observations, or 0 if no results returned
     */
    public Long getNumberObservations(DatasetItem datasetItem, SkillItem skillItem) {

        Long numObs = new Long(0);

        if (skillItem == null) {
            throw new IllegalArgumentException("the skill item cannot be null");
        }

        StringBuffer query = new StringBuffer();
        query.append(GET_NUM_OBSERVATIONS_BY_SKILL);

        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem allDataSample = sampleDao.findOrCreateDefaultSample(datasetItem);

        // Check that an All Data sample exists because there could be zero transactions
        // and return zero observations if not.
        if (allDataSample == null) {
            logger.error("This dataset has no " + SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME
                         + " sample and therefore no unique steps.");
            return numObs;
        } else {
            logger.debug(SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME + " sample found.");
        }

        Session session = null;
        try {
            session = getSession();
            //build the native SQL query.
            SQLQuery sqlQuery = session.createSQLQuery(query.toString());
            sqlQuery.setInteger("sampleId", ((Integer)allDataSample.getId()).intValue());
            sqlQuery.setLong("skillId", ((Long)skillItem.getId()).longValue());
            sqlQuery.addScalar("numObs", LONG);

            if (logger.isTraceEnabled()) {
                logger.trace("getNumberObsBySkill sqlQuery [" + sqlQuery + "]");
            }

            List dbResults = sqlQuery.list();

            if (dbResults.size() > 0) {
                numObs = (Long)dbResults.get(0);
            }
        } finally {
            releaseSession(session);
        }

        return numObs;
    }

    /**
     * Native MySql query for counting the number of unique steps in a skill
     * for a given sample.
     */
    private static final String GET_NUM_UNIQUE_STEPS =
        "select count(distinct step_id) as numUniqSteps from step_rollup sri"
        + " where sri.sample_id = :sampleId"
        + " and sri.skill_id = :skillId";

    /**
     * Run a native MySQL query to get the number of unique steps from the step rollup
      * table for the 'All Data' sample of the given dataset and skill.
     * @param datasetItem the given dataset
     * @param skillItem the given skill
     * @return the number of unique steps, or 0 if no results returned
     */
    public Long getNumberUniqueSteps(DatasetItem datasetItem, SkillItem skillItem) {

        Long numUniqueSteps = new Long(0);

        if (skillItem == null) {
            throw new IllegalArgumentException("the skill item cannot be null");
        }

        StringBuffer query = new StringBuffer();
        query.append(GET_NUM_UNIQUE_STEPS);

        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem allDataSample = sampleDao.findOrCreateDefaultSample(datasetItem);

        // Check that an All Data sample exists because there could be zero transactions
        // and return zero observations if not.
        if (allDataSample == null) {
            logger.error("This dataset has no " + SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME
                         + " sample and therefore no unique steps.");
            return numUniqueSteps;
        } else {
            logger.debug(SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME + " sample found.");
        }

        Session session = null;
        try {
            session = getSession();
            //build the native SQL query.
            SQLQuery sqlQuery = session.createSQLQuery(query.toString());
            sqlQuery.setInteger("sampleId", ((Integer)allDataSample.getId()).intValue());
            sqlQuery.setLong("skillId", ((Long)skillItem.getId()).longValue());
            sqlQuery.addScalar("numUniqSteps", LONG);

            if (logger.isTraceEnabled()) {
                logger.trace("getNumberUniqueSteps sqlQuery [" + sqlQuery + "]");
            }

            List dbResults = sqlQuery.list();

            if (dbResults.size() > 0) {
                numUniqueSteps = (Long)dbResults.get(0);
            }
        } finally {
            releaseSession(session);
        }

        return numUniqueSteps;
    }

    /**
     * Native MySql query for finding all step
     * for a given sample.
     */
    private static final String FIND_BY_SAMPLE_NATIVE_MYSQL =
        "select sri.* from step_rollup sri"
        + " join student stud using (student_id)"
        + " where sri.sample_id = :sampleId"
        + " and sri.skill_model_id = :modelId";

    /**
     * Native MySql query for finding all step
     * for a given sample.
     */
    private static final String FIND_BY_SAMPLE_WITH_NO_SKILL_MODELS_MYSQL =
        "select sri.* from step_rollup sri"
        + " join student stud using (student_id)"
        + " where sri.sample_id = :sampleId";

    /** The "order by" clause for getting step roll-ups. */
    private static final String FIND_BY_SAMPLE_ORDER_BY =
        " order by stud.anon_user_id, sri.step_time, sri.problem_id, sri.step_id";

    /** The limit and offset portion of the the MySql for getting step roll-ups. */
    private static final String FIND_BY_SAMPLE_LIMIT =
        " limit :limit offset :offset";

    /**
     * Gets all unique Student Rollup Items in the sample.
     * @param sampleItem The sample to get all student roll ups for.
     * @return A list of student rollup items.
     */
    public List getStepRollupList(SampleItem sampleItem) {
        return getStepRollupList(sampleItem, null, null, null);
    }

    /**
     * Gets all unique Student Rollup Items in the sample.
     * @param sampleItem The sample to get all student roll ups for.
     * @param limit A limit to the total number to return.
     * @param offset An offset for the start position of which set to return.
     * @param options Options for exporting.
     * @return A list of student rollup items.
     */
    public List getStepRollupList(SampleItem sampleItem, StepRollupExportOptions options,
            Integer limit, Integer offset) {
        if (sampleItem == null) {
            throw new IllegalArgumentException("null is not an allowed value for a sample");
        }

        options = (options == null) ? new StepRollupExportOptions(sampleItem) : options;
        SkillModelItem modelItem = options.getModel();


        StringBuffer query = new StringBuffer();
        if (modelItem == null) {
            query.append(FIND_BY_SAMPLE_WITH_NO_SKILL_MODELS_MYSQL);
        } else {
            query.append(FIND_BY_SAMPLE_NATIVE_MYSQL);
        }

        StringBuffer whereClause = buildWhereOptionsForPreview(sampleItem, options, false);
        if (whereClause == null) { return new ArrayList (); }

        query.append(whereClause);

        query.append(FIND_BY_SAMPLE_ORDER_BY);
        if (limit != null && offset != null) { query.append(FIND_BY_SAMPLE_LIMIT); }

        if (logger.isTraceEnabled()) { logger.trace("Native SQL Query: " + query); }

        //build the native SQL query.
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());

        sqlQuery.setInteger("sampleId", (Integer)sampleItem.getId());
        if (modelItem != null) {
            sqlQuery.setLong("modelId", (Long)modelItem.getId());
        }
        if (limit != null && offset != null) {
            sqlQuery.setInteger("limit", limit).setInteger("offset", offset);
        }
        sqlQuery.addEntity("sri", StepRollupItem.class);
        List dbResults = sqlQuery.list();
        releaseSession(session);
        return unmodifiableList(dbResults);
    }

    /**
     * Gets all unique Student Rollup Items in the sample.
     * @param sampleItem The sample to get all student roll ups for.
     * @param options Options for exporting.
     * @return A list of student rollup items.
     */
    public List<StepRollupItem> getSSSSStepRollups(final SampleItem sampleItem,
            StepRollupExportOptions options) {
        if (sampleItem == null) {
            throw new IllegalArgumentException("null is not an allowed value for a sample");
        }

        final SkillModelItem modelItem = options.getModel();
        StringBuffer sql = new StringBuffer(FIND_BY_SAMPLE_NATIVE_MYSQL);
        StringBuffer whereClause = buildWhereFromOptions(sampleItem, options);
        sql.append(whereClause == null ? "" : whereClause);
        sql.append(FIND_BY_SAMPLE_ORDER_BY);

        if (logger.isTraceEnabled()) { logger.trace("Native SQL Query: " + sql); }

        return executeSQLQuery(sql.toString(), new PrepareQuery() {
            @Override
            public void prepareQuery(SQLQuery query) {
                query.addEntity("sri", StepRollupItem.class)
                .setInteger("sampleId", (Integer)sampleItem.getId())
                    .setLong("modelId", (Long)modelItem.getId());
            }
        });
    }

    /**
     * Builds the where clause from a set of export options and a sample.
     * @param sampleItem the sample
     * @param options the {@link StepRollupExportOptions}
     * @return a StringBuffer with the SQL.
     */
    private StringBuffer buildWhereFromOptions(SampleItem sampleItem,
            StepRollupExportOptions options) {

        StringBuffer query = new StringBuffer();

        List<StudentItem> students = options.getSelectedStudents();
        List<ProblemItem> problems = options.getSelectedProblems();
        List<SkillItem> skills = options.getSelectedSkills();
        SkillModelItem modelItem = options.getModel();

        if ((students == null) || (students.size() == 0)) {
            students = DaoFactory.DEFAULT.getStudentDao().find(sampleItem.getDataset());
        }
        if ((problems == null) || (problems.size() == 0)) {
            problems = DaoFactory.DEFAULT.getProblemDao().find(sampleItem.getDataset());
        }

        if (problems.size() > 0) {
            query.append(format(" and sri.problem_id in (%s)", getIds(problems)));
        } else {
            return null;
        }

        if (students.size() > 0) {
            query.append(format(" and sri.student_id in (%s)", getIds(students)));
        } else {
            return null;
        }

        if (skills != null && skills.size() > 0) {
            query.append(format(" and (sri.skill_id in (%s) or sri.skill_id IS NULL )",
                    getIds(skills)));
        } else if (modelItem != null) {
            if (options.isDisplaySkills()) {
                query.append(" and sri.skill_id IS NULL ");
            }
        }

        if (modelItem == null) {
            query.append(" group by sri.student_id, sri.step_id, sri.problem_view");
        }

        return query;
    }

    /**
     * Builds the where clause from a set of export options and a sample.
     * @param sampleItem the sample
     * @param options the {@link StepRollupExportOptions}
     * @param countFlag whether or not we are merely counting the rows
     * @return a StringBuffer with the SQL.
     */
    private StringBuffer buildWhereOptionsForPreview(SampleItem sampleItem,
            StepRollupExportOptions options, boolean countFlag) {
        StringBuffer query = new StringBuffer();

        List<StudentItem> students = options.getSelectedStudents();
        List<ProblemItem> problems = options.getSelectedProblems();
        List<SkillItem> skills = options.getSelectedSkills();

        if ((students == null) || (students.size() == 0)) {
            students = DaoFactory.DEFAULT.getStudentDao().find(sampleItem.getDataset());
        }
        if ((problems == null) || (problems.size() == 0)) {
            problems = DaoFactory.DEFAULT.getProblemDao().find(sampleItem.getDataset());
        }

        if (problems.size() > 0) {
            query.append(format(" and sri.problem_id in (%s)", getIds(problems)));
        }

        if (students.size() > 0) {
            query.append(format(" and sri.student_id in (%s)", getIds(students)));
        }

        if (options.isDisplaySkills() && (skills == null || skills.size() == 0)) {
            query.append(" and sri.skill_id IS NULL ");
        } else if (options.isDisplaySkills() && skills != null && skills.size() > 0) {
            query.append(format(" and (sri.skill_id in (%s) or sri.skill_id IS NULL )",
                    getIds(skills)));
        }


        if (countFlag) {
            if (options.getModel() != null) {
                query.append(" and sri.skill_model_id = " + options.getModel().getId());
            }
            query.append(" group by sri.skill_model_id");
            if (options.getModel() == null) {
                query.append(" having sri.skill_model_id = min(sri.skill_model_id)");
            }
        } else {
            if (options.getModel() != null) {
                query.append(" and sri.skill_model_id = " + options.getModel().getId());
            }
            query.append(" group by sri.sample_id, sri.student_id, sri.step_id, sri.problem_view");
         //   query.append(" having sri.skill_model_id = min(sri.skill_model_id)");
        }
        return query;
    }

    /**
     * Comma separated list of the item IDs.
     * @param items Hibernate DataShop objects
     * @return comma separated list of the item IDs
     */
    private String getIds(final List< ? extends Item> items) {
        return join(", ", new ArrayList<Comparable>() { {
            for (Item item : items) { add(item.getId()); }
        } });
    }

    /**
     * Loads the customized (for sample name and id) aggregator_sp SQL file into the
     * database.  Since Hibernate is stupid, we have to execute the contents of the
     * stored procedure file as a list of individual queries.
     * @param aggSpFilePath The location of the aggregator stored procedure file.
     * @param toInsert the character sequence to insert into the stored procedure file.
     * @return true if statement execution was successful, false if an SQL exception
     *      was thrown.
     */
    public boolean loadAggregatorStoredProcedure(String aggSpFilePath, String toInsert) {
        try {
            return loadCustomizedSP(aggSpFilePath, toInsert);
        } catch (IOException ioe) {
            logger.error("Unable to open " + aggSpFilePath + " for reading and replacement.", ioe);
            return false;
        }
    }

    /** SQL query to return a list of student IDs and the number of sessions for each student. */
    private static final String STUDENT_SESSION_COUNTS_SQL =
        "SELECT sess.student_id, COUNT(DISTINCT sess.session_id) AS num_sessions"
        + " FROM session sess"
        + " JOIN tutor_transaction USING (session_id)"
        + " JOIN transaction_sample_map tx_map USING (transaction_id)"
        + " WHERE tx_map.sample_id = ?"
        + " GROUP by sess.student_id";

    /**
     * Get a result set containing student IDs and corresponding session counts.  For use in
     * aggregator batching.
     * @param sample the sample we are processing.
     * @param session the Session!
     * @return a result set containing the results from the STUDENT_SESSION_COUNTS_SQL query.
     * @throws SQLException if something bad happened!
     */
    private ResultSet getStudentSessionCounts(final SampleItem sample, Session session)
    throws SQLException {
        PreparedStatement stmt = session.connection()
            .prepareStatement(STUDENT_SESSION_COUNTS_SQL);
        stmt.setInt(1, (Integer)sample.getId());
        return stmt.executeQuery();
    }

    /** The name of the aggregator_sp to call. */
    private static final String RUN_AGGREGATOR_SP_NAME_BASE = "run_aggregator";
    /** The default suffix used within the aggregator_sp file. */
    private static final String RUN_AGGREGATOR_SP_XXX_SUFFIX = "XXX";
    /** SQL parameter index for the sampleID. */
    private static final int SAMPLE_ID_INDEX = 1;
    /** SQL parameter index for studentSQLClause. */
    private static final int STUDENT_SQL_CLAUSE_INDEX = 2;
    /** SQL parameter index for skillModelSQLClause. */
    private static final int SKILL_MODEL_SQL_CLAUSE_INDEX = 3;
    /** SQL parameter index for numRowsCreated. */
    private static final int NUM_ROWS_CREATED_INDEX = 4;
    /** ResultSet index for student column. */
    private static final int STUDENT_ID_INDEX = 1;
    /** ResultSet index for num_sessions column. */
    private static final int NUM_SESSIONS_INDEX = 2;
    /**
     * Call the aggregator stored procedure, using the given sample, stored procedure suffix
     * and generated SQL "in" clause for a set of skill models.  This method is used by
     * the KCModel import feature.
     * @param sample the sample to aggregate.
     * @param storedProcSuffix the unique suffix added to the stored procedure table names.
     * @param skillModelSQLClause the "in" clause to be used when aggregating.
     * @param batchSize the number of sessions to work with at a time in the stored procedure.
     * @return the number of rows created in the step_rollup table.
     */
    public Integer callAggregatorStoredProcedure(SampleItem sample, String storedProcSuffix,
            String skillModelSQLClause, int batchSize) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        sample = sampleDao.get((Integer)sample.getId());
        DatasetItem dataset = sample.getDataset();
        String logPrefix = getLogPrefix(dataset, sample);

        Session session = getSession();

        // if skillModelSQLClause is null, then we need to aggregate for all skill models.
        if (skillModelSQLClause == null) {
            skillModelSQLClause = " sk.skill_model_id in"
                    + " (SELECT distinct skill_model_id from skill_model"
                    + " WHERE dataset_id = " + dataset.getId() + ")";
        }

        long totalSessions = sampleDao.getNumSessions(sample);
        Integer numRowsCreated = new Integer(0);

        try {
            // get the list of students and session counts
            ResultSet rs = getStudentSessionCounts(sample, session);
            boolean aggregationError = false;
            Long sessCount = 0L, numSessionsProcessed = 0L;
            StringBuffer studentInClause = new StringBuffer();
            ArrayList<Long> studentIds = new ArrayList<Long>();

            while (rs.next()) {
                // build the list of students for this batch by summing the number of sessions and
                // comparing this against the batchSize
                Long studentId = rs.getLong(STUDENT_ID_INDEX);
                Long numSessions = rs.getLong(NUM_SESSIONS_INDEX);
                boolean isLast = rs.isLast();

                if (sessCount + numSessions > batchSize || isLast) {
                    if (sessCount == 0) {
                        // special case where this student has more sessions than the batchSize
                        // so it has go on its own.
                        studentInClause.append(format("(%s)", studentId));
                        sessCount = numSessions;
                    } else {
                        // do this so that we don't lose the very last student in the RS.
                        if (isLast) {
                            studentIds.add(studentId);
                            sessCount += numSessions;
                        } else {
                            // push back one row so we don't skip this student
                            rs.previous();
                        }
                        studentInClause.append(format("(%s)", join(", ", studentIds)));
                    }
                    logDebug("Batch consists of ", studentInClause, " students.");
                    // bring out the big guns!
                    try {
                        logger.info(logPrefix + "batch size: " + batchSize
                                + ", actual batch size: " + sessCount
                                + ", processed " + numSessionsProcessed + "/" + totalSessions
                                + " total sessions in sample.");
                        String procToCall = RUN_AGGREGATOR_SP_NAME_BASE + "_"
                        + (storedProcSuffix == null
                                ? RUN_AGGREGATOR_SP_XXX_SUFFIX : storedProcSuffix);
                        CallableStatement callableStmt = session.connection().prepareCall(
                                format("{CALL %s (?,?,?,?)}", procToCall));
                        callableStmt.setInt(SAMPLE_ID_INDEX, (Integer)sample.getId());
                        callableStmt.setString(STUDENT_SQL_CLAUSE_INDEX,
                                studentInClause.toString());
                        callableStmt.setString(SKILL_MODEL_SQL_CLAUSE_INDEX, skillModelSQLClause);
                        callableStmt.registerOutParameter(NUM_ROWS_CREATED_INDEX, Types.INTEGER);
                        callableStmt.executeUpdate();
                        numRowsCreated += callableStmt.getInt(NUM_ROWS_CREATED_INDEX);
                        numSessionsProcessed += sessCount;
                    } catch (SQLException exception) {
                        logger.error(logPrefix
                                + "SQLException while trying to run agg stored procedure."
                                + " Attempting to clean up. " + exception.getMessage(), exception);
                        numRowsCreated = -1;
                        aggregationError = true;
                        break; //out of for loop, no reason to keep going
                    }
                    if (aggregationError) { break; } //out of the while loop.
                    sessCount = new Long(0);
                    studentInClause.setLength(0);
                    studentIds.clear();
                } else {
                    sessCount += numSessions;
                    studentIds.add(studentId);
                }
            } // end while loop
            logger.info("Processed " + numSessionsProcessed + "/" + totalSessions);
        } catch (SQLException exception) {
            logger.error(logPrefix
                    + "SQLException while trying to get the list of students and session counts."
                    + " Halting aggregator processing. " + exception.getMessage(), exception);
            numRowsCreated = -1;
        } finally {
            releaseSession(session);
        }
        return numRowsCreated;
    }

    /**
     * Call the set of aggregator stored procedures with the default batch size.
     * @param sample the sample to aggregate.
     * @param storedProcSuffix the unique suffix added to the stored procedure table names.
     * @param skillModelSQLClause the "in" clause to be used when aggregating.
     * @return the number of rows created in the step_rollup table.
     */
    public Integer callAggregatorStoredProcedure(SampleItem sample, String storedProcSuffix,
            String skillModelSQLClause) {
      return callAggregatorStoredProcedure(sample, storedProcSuffix,
                                           skillModelSQLClause, AGG_SESSION_BATCH_SIZE);
    }

    /**
     * Call the set of aggregator stored procedures.
     * @param sample the sample to aggregate.
     * @param storedProcSuffix the unique suffix added to the stored procedure table names.
     * @return the number of rows created in the step_rollup table.
     */
    public Integer callAggregatorStoredProcedure(SampleItem sample, String storedProcSuffix) {
      return callAggregatorStoredProcedure(sample, storedProcSuffix, null, AGG_SESSION_BATCH_SIZE);
    }

    /**
     * Call the aggregator stored procedure and run on the given sample.
     * @param sample the sample to aggregate.
     * @param batchSize the number of sessions to work with at a time in the stored procedure.
     * @return the number of rows created in the step_rollup table.
     */
    public Integer callAggregatorStoredProcedure(SampleItem sample, int batchSize) {
        return callAggregatorStoredProcedure(sample, null, null, batchSize);
    }

    /**
     * Return the SSSVS 2D array that contains studentID, success, step, problem view and skill.
     * Note that this is for the Learning Factors Analysis tool.
     * @param dataset the selected dataset
     * @param skillModel the selected skill model
     * @return String[][] the student, success, step, and skill
     */
    public String[][] getSSSVS(DatasetItem dataset, SkillModelItem skillModel) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem allData = sampleDao.findOrCreateDefaultSample(dataset);
        List<String[]> sssvsList = new ArrayList<String[]>();

        String logPrefix = "getSSSVS [" + dataset.getDatasetName()
                + " (" + dataset.getId() + ") / "
                + skillModel.getSkillModelName()
                + " (" + skillModel.getId() + ")] : ";

        if (allData == null) { return new String[0][SSSS_NUM_OBJECTS]; }

        List<StudentItem> students = DaoFactory.DEFAULT.getStudentDao().find(dataset);

        logDebug(logPrefix, "Found ", students.size(), " students.");

        for (StudentItem student : students) {
            List<StepRollupItem> stepRollupItems = null;
            StepRollupExportOptions options = new StepRollupExportOptions();
            String studentIdString = student.getId().toString();

            options.setModel(skillModel);
            options.setSelectedStudents(singletonList(student));
            stepRollupItems =  getSSSSStepRollups(allData, options);
            logDebug(logPrefix, "Found ", stepRollupItems.size(),
                     " step rollup items creating SSSVS for student ", studentIdString);
            for (int i = 0; i < stepRollupItems.size(); i++) {
                StepRollupItem sri = stepRollupItems.get(i);

                //get the success flag from the outcome
                CorrectFlag attemptType = sri.getFirstAttempt();
                String outcome;

                if (attemptType != null) {
                    if (attemptType.equals(INCORRECT) || attemptType.equals(HINT)) {
                        outcome = "0";
                    } else if (attemptType.equals(CORRECT)) {
                        outcome = "1";
                    } else {
                        outcome = "2";
                    }
                } else {
                    outcome = "2"; //must be "UNKNOWN", discard
                    logger.warn(logPrefix
                            + "NULL or Unknown subgoal attempt while getting SSSS :: "
                            + attemptType);
                }

                //get the step
                String step = format("%s(%s)", sri.getProblem().getId(), sri.getStep().getId());

               //get problem view
                String problemView = "" + sri.getProblemView();

                //get the skill
                SkillItem skill = sri.getSkill();
                if (skill == null) { continue; }

                sssvsList.add(new String[] {studentIdString, outcome, step, problemView,
                        skill.getId().toString() });
            } // end for loop on SRIs for a given student
            logTrace(logPrefix, "In for loop on students: sssvsList.size() = " + sssvsList.size());

        } // end for loop on students
        logTrace(logPrefix, "After for loop on students: sssvsList.size() = " + sssvsList.size());

        return sssvsList.toArray(new String[sssvsList.size()][SSSVS_NUM_OBJECTS]);
    } // end method getSSSVS

    /** The Native SQL to call the LFA backfill stored procedure. */
    private static final String LFA_BACKFILL_SP_NATIVE_SQL
            = "call lfa_backfill_sp(:skillModelId)";

    /**
     * Call the stored procedure to calculate and store the new predicted error rate
     * for a given skill model.
     * This should be called after a successful call to LFA on the given skill model.
     * @param skillModelItem the skill model to be updated
     * @return the number of rows with a predicted error rate (i.e. not null)
     */
    public int callLfaBackfillSP(final SkillModelItem skillModelItem) {
        if (skillModelItem == null) {
            throw new IllegalArgumentException("callLfaBackfillSP(): Skill Model cannot be null.");
        }
        return getIntForSQL(LFA_BACKFILL_SP_NATIVE_SQL, new PrepareQuery() {
            public void prepareQuery(SQLQuery query) {
                query.setLong("skillModelId", (Long)skillModelItem.getId());
            }
        }, 0);
    }

    /** The Native SQL to call the LFA backfill stored procedure. */
    private static final String LFA_BACKFILL_BY_SAMPLE_SP_NATIVE_SQL
            = "call lfa_backfill_by_sample_sp(:sampleId, :datasetId)";

    /**
     * Call the stored procedure to calculate and store the new predicted error rate
     * for a given sample.
     * This should be called after a successful call to LFA on the given sample.
     * @param sampleItem the sample in the step rollup table to be updated
     * @return the number of rows with a predicted error rate (i.e. not null)
     */
    public int callLfaBackfillBySampleSP(final SampleItem sampleItem, final DatasetItem datasetItem) {
        if (sampleItem == null || datasetItem == null) {
            throw new IllegalArgumentException(
                    "callLfaBackfillBySampleSP(): Sample and Dataset cannot be null.");
        }
        return getIntForSQL(LFA_BACKFILL_BY_SAMPLE_SP_NATIVE_SQL, new PrepareQuery() {
            public void prepareQuery(SQLQuery query) {
                query.setLong("sampleId", ((Number)sampleItem.getId()).longValue());
                query.setInteger("datasetId", (Integer) datasetItem.getId());
            }
        }, 0);
    }

    /** HQL query for returning the Predicted Error Rate
     * given the sample, student, step, skill, opportunity and problem view. */
    private static final String QUERY_PER =
        "select distinct sri from StepRollupItem sri"
        + " where sri.sample.id = ?"
        + " and sri.student.id = ?"
        + " and sri.step.id = ?"
        + " and sri.skill.id = ?"
        + " and sri.opportunity = ?"
        + " and sri.problemView = ?";

    /** Number of parameters in query. */
    private static final int QUERY_PER_NUM_PARAMS = 6;

    /** Constant. */
    private static final int PARAM_SKILL = 3;
    /** Constant. */
    private static final int PARAM_OPP = 4;
    /** Constant. */
    private static final int PARAM_PROB_VIEW = 5;

    /**
     * Get all StepRollup Items for the given sample, student, step, skill,
     * opportunity and problem view.
     * @param sampleId the sample id
     * @param studentId the student id
     * @param stepId the step id
     * @param skillId the skill id
     * @param opportunity the opportunity
     * @param problemView the problem view
     * @return List of step rollup items.
     */
    public List find(Integer sampleId, Long studentId, Long stepId, Long skillId,
            Integer opportunity, Integer problemView) {

        Object [] params = new Object [QUERY_PER_NUM_PARAMS];
        params[0] = sampleId;
        params[1] = studentId;
        params[2] = stepId;
        params[PARAM_SKILL] = skillId;
        params[PARAM_OPP] = opportunity;
        params[PARAM_PROB_VIEW] = problemView;

        return getHibernateTemplate().find(QUERY_PER, params);
    }

    /**
     * Drop aggregator temporary tables, stored procedures, and functions for the given sample.
     * @param sample the sample
     * @param toInsertBase indicates whether for sample, KC model, etc.
     */
    public void dropAll(SampleItem sample, String toInsertBase) {
        String toInsert = toInsertBase + sample.getId();
        dropStoredProcedures(getProcedures(toInsert), getFunctions(toInsert));
        dropTables(getTemporaryTables(toInsert));
    }

    /**
     * Little helper method to consistently create a prefix of the dataset name
     * and id and the sample name and id for the log4j and system logging.
     * @param datasetItem the dataset item
     * @param sampleItem the sample item
     * @return a string useful for logging
     */
    private String getLogPrefix(DatasetItem datasetItem, SampleItem sampleItem) {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        datasetItem = datasetDao.get((Integer)datasetItem.getId());
        return datasetItem.getDatasetName() + " (" + datasetItem.getId() + ") / "
            + sampleItem.getSampleName() + " (" + sampleItem.getId() + "): ";
    }

    /**
     * Set the single query parameter to the sample id.
     * @param sample the sample
     * @return a PrepareQuery object that sets the sample parameter
     */
    private PrepareQuery setSample(final SampleItem sample) {
        return new PrepareQuery() {
            public void prepareQuery(SQLQuery query) {
                query.setInteger(0, (Integer)sample.getId());
            }
        };
    }

    /** query for step rollup headers. */
    private static final String STEP_ROLLUP_HDR_SQL =
          " select distinct kc_columns(skill_model_name) as kcs"
        + " from step_rollup sr"
        + " join skill_model using (skill_model_id)"
        + " where sample_id = ?"
        + " order by sr.skill_model_id";

    /** Step rollup headers that are the same for every sample. */
    private static final List<String> STATIC_HEADERS = asList("Row", "Sample",
        "Anon Student Id", "Problem Hierarchy", "Problem Name", "Problem View", "Step Name",
        "Step Start Time", "First Transaction Time",
        "Correct Transaction Time", "Step End Time", "Step Duration (sec)",
        "Correct Step Duration (sec)", "Error Step Duration (sec)",
        "First Attempt", "Incorrects", "Hints", "Corrects", "Condition");

    /**
     * Step rollup headers for the given sample.
     * @param sample the sample
     * @return step rollup headers for the given sample
     */
    public List<String> getStepRollupHeaders(final SampleItem sample) {

        final List<String> results = executeSQLQuery(STEP_ROLLUP_HDR_SQL, new PrepareQuery() {
            public void prepareQuery(final SQLQuery query) {
                query.setInteger(0, (Integer)sample.getId());
                query.addScalar("kcs", STRING);
            }
        });
        if (results == null) { return null; }

        return append(STATIC_HEADERS, results);
    }

    /** Select the unique student IDs for the sample. */
    private static final String STEP_ROLLUP_STUDENTS_SQL = "select distinct student_id from"
        + " step_rollup join student using (student_id)"
        + " where sample_id = ? order by anon_user_id";

    /**
     * Select the unique student IDs for the sample, partitioned into batches.
     * @param sample the sample
     * @param batchSize the student batch size
     * @return the student IDs for the sample, partitioned into batches.
     */
    public Iterable<List<Long>> getStepRollupStudentBatches(final SampleItem sample,
            int batchSize) {
        return partition(batchSize, executeSQLQuery(STEP_ROLLUP_STUDENTS_SQL, new PrepareQuery() {
            public void prepareQuery(SQLQuery query) {
                query.setInteger(0, (Integer)sample.getId());
            }
        }));
    }

    /**
     * Native MySql query for finding all step
     * roll-ups along with a secondary model
     * for a given sample.
     */
    private static final String FIND_BY_SAMPLE_WITH_ALL_MODELS =
        "select sri.step_rollup_id as step_rollup_id, "
        + " group_concat(ifnull(skill_model_name, '') SEPARATOR '~~') as skill_models,"
        + " group_concat(ifnull(skill_name, '') SEPARATOR '~~') as skills,"
        + " group_concat(ifnull(convert(opportunity, char), '') SEPARATOR '~~') as opportunities,"
        + " group_concat(ifnull(format(predicted_error_rate, 4), '') SEPARATOR '~~')"
        + " as predicted_error_rates"
        + " from step_rollup sri"
        + " join student stud on sri.student_id = stud.student_id"
        + " join problem p using (problem_id)"
        + " join sample using (sample_id)"
        + " join subgoal step on sri.step_id = step.subgoal_id"
        + " left outer join skill sk using (skill_id)"
        + " left outer join skill_model skm on sk.skill_model_id = skm.skill_model_id"
        + " where sri.sample_id = ";

    /** Group by for return SRI and all model info. */
    private static final String STEP_ROLLUP_PREVIEW_ORDERBY =
        " order by anon_user_id, step_time";

    /**
     * Native MySql query for returning results from above query.
     */
    private static final String SELECT_STEP_ROLLUP_ITEM_RESULTS =
        "SELECT step_rollup_id, skill_models, skills, opportunities, predicted_error_rates FROM ";

    /**
     * Gets the student-step rollup items for the page grid.
     * @param sampleItem The sample to get all student roll ups for.
     * @param options Options for exporting.
     * @param currentLimit the limit for this sample
     * @param currentOffset the offset for this sample
     * @return A list of student rollup items.
     */
    public List<Object> getStepRollupItems(final SampleItem sampleItem,
            StepRollupExportOptions options, Integer currentLimit, Integer currentOffset) {
        if (sampleItem == null) {
            throw new IllegalArgumentException("null is not an allowed value for a sample");
        }
        if (options == null) {
            throw new IllegalArgumentException("null is not an allowed value for options");
        }

        // Trac 388. This needs to happen as a single work unit.
        synchronized(sampleItem) {
            StringBuffer sql = new StringBuffer(FIND_BY_SAMPLE_WITH_ALL_MODELS);
            sql.append((Integer)sampleItem.getId());

            boolean countFlag = false;
            sql.append(buildWhereOptionsForPreview(sampleItem, options, countFlag));
            sql.append(STEP_ROLLUP_PREVIEW_ORDERBY);
            if (currentLimit != null) {
                sql.append(" limit " + currentLimit);
            }
            if (currentOffset != null) {
                sql.append(" offset " + currentOffset);
            }

            if (logger.isTraceEnabled()) { logger.trace("Native SQL Query: " + sql); }

            StringBuffer tableName = new StringBuffer("step_rollup_items_");
            tableName.append(sampleItem.getId());

            // Use stored procedure to query for results and write to temporary table.
            Session session = null;
            try {
                session = getSession();
                Object[] args = {'?', '?'};
                String sp = "get_step_rollup_items";
                CallableStatement callableStmt =
                    session.connection().prepareCall(buildSPCall(sp, args));
                callableStmt.setString(1, sql.toString());
                callableStmt.setString(2, tableName.toString());
                callableStmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("SQLException while trying to get StepRollupItems for sample: "
                             + sampleItem.getId() + ": " + e.getMessage(), e);
            } finally {
                releaseSession(session);
            }

            // Now read the results.
            sql = new StringBuffer(SELECT_STEP_ROLLUP_ITEM_RESULTS);
            sql.append(tableName.toString());

            List<Object> result = executeSQLQuery(sql.toString(), new PrepareQuery() {
                    @Override
                        public void prepareQuery(SQLQuery query) {
                        query.addScalar("step_rollup_id", LONG)
                            .addScalar("skill_models", STRING)
                            .addScalar("skills", STRING)
                            .addScalar("opportunities", STRING)
                            .addScalar("predicted_error_rates", STRING);
                    }
                });

            // Drop temporary table used to hold results.
            dropTables(asList(tableName.toString()));

            return result;
        }
    }

    /** query for the step rollup export, batching by student IDs. */
    private static final String STEP_ROLLUP_BATCH_SQL = "select p.problem_id as problem_id,"
        + " sample_name, anon_user_id, problem_name,"
        + " problem_view, subgoal_name, step_time, step_start_time, first_transaction_time,"
        + " correct_transaction_time, step_end_time, step_duration, correct_step_duration,"
        + " error_step_duration, first_attempt, total_incorrects, total_hints, total_corrects,"
        + " IFNULL(conditions, '.') as conditions,"
        + " group_concat(ifnull(skill_model_name, '') SEPARATOR '~~') as skill_models,"
        + " group_concat(ifnull(skill_name, '') SEPARATOR '~~') as skills,"
        + " group_concat(ifnull(convert(opportunity, char), '') SEPARATOR '~~') as opportunities,"
        + " group_concat(ifnull(format(predicted_error_rate, 4), '') SEPARATOR '~~')"
        + " as predicted_error_rates"
        + " from step_rollup sri"
        + " join student using (student_id)"
        + " join problem p using (problem_id)"
        + " join sample using (sample_id)"
        + " join subgoal step on sri.step_id = step.subgoal_id"
        + " left outer join skill sk using (skill_id)"
        + " left outer join skill_model skm on sk.skill_model_id = skm.skill_model_id"
        + " where student_id in (%s) and sample_id = ";
    /** query for the step rollup export, batching by student IDs. */
    private static final String STEP_ROLLUP_BATCH_SQL_GROUPBY =
        " group by sample_id, student_id, step_id, problem_view"
        + " order by anon_user_id, step_time";

    /**
     * Native MySql query for returning results from above query.
     */
    private static final String SELECT_STEP_ROLLUP_ROW_RESULTS =
        "SELECT problem_id, sample_name, anon_user_id, problem_name, problem_view,"
        + " subgoal_name, step_time, step_start_time, first_transaction_time,"
        + " correct_transaction_time, step_end_time, step_duration, correct_step_duration,"
        + " error_step_duration, first_attempt, total_incorrects, total_hints, total_corrects,"
        + " conditions, skill_models, skills, opportunities, predicted_error_rates FROM ";

    /**
     * Results of calling the step rollup query.
     * @param sample the sample
     * @param options the StepRollupExportOptions
     * @param session keep this open until we are finished with the results.
     * @param limit number of rows to fetch at a time
     * @param studentIds the batch of students to fetch
     * @return the result set for the step rollup query
     * @throws SQLException if something goes wrong
     */
    public ResultSet getStepRollupRows(final SampleItem sample, StepRollupExportOptions options,
            Session session, Integer limit, List<Long> studentIds)
    throws SQLException {
        StringBuffer sql = new StringBuffer("");
        // Get or created cached export
        if (options.getExportCachedVersion()
            || (!options.hasUserOptions() && options.isDisplayAllModels())) {
            sql.append(String.format(STEP_ROLLUP_BATCH_SQL, join(", ", studentIds)));
            sql.append((Integer)sample.getId());
            sql.append(STEP_ROLLUP_BATCH_SQL_GROUPBY);
        // Generate uncached export
        } else {
            sql.append(String.format(STEP_ROLLUP_BATCH_SQL, join(", ", studentIds)));
            sql.append((Integer)sample.getId());
            boolean countFlag = false;
            sql.append(buildWhereOptionsForPreview(sample, options, countFlag));
        }

        if (logger.isTraceEnabled()) { logger.trace("Native SQL Query: " + sql); }

        StringBuffer tableName = new StringBuffer("step_rollup_rows_");
        tableName.append(sample.getId());

        // Use stored procedure to query for results and write to temporary table.
        try {
            Object[] args = {'?', '?'};
            String sp = "get_step_rollup_rows";
            CallableStatement callableStmt =
                session.connection().prepareCall(buildSPCall(sp, args));
            callableStmt.setString(1, sql.toString());
            callableStmt.setString(2, tableName.toString());
            callableStmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQLException while trying to get StepRollupRows for sample: "
                         + sample.getId() + ": " + e.getMessage(), e);
        }

        // Now read the results.
        sql = new StringBuffer(SELECT_STEP_ROLLUP_ROW_RESULTS);
        sql.append(tableName.toString());

        PreparedStatement stmt = session.connection().prepareStatement(sql.toString());
        stmt.setFetchSize(limit);

        ResultSet result = stmt.executeQuery();

        // Drop temporary table used to hold results.
        dropTables(asList(tableName.toString()));

        return result;
    }

    /** query for step rollup skill model names. */
    private static final String STEP_SKILL_MODEL_NAME_SQL = "select distinct skill_model_name"
        + " from step_rollup sr"
        + " join skill_model using (skill_model_id)"
        + " where sample_id = ?"
        + " order by sr.skill_model_id";

    /**
     * Step rollup skill model names for the given sample.
     * @param sample the sample
     * @return step rollup skill model names for the given sample
     */
    public List<String> getSkillModelNames(final SampleItem sample) {
        return executeSQLQuery(STEP_SKILL_MODEL_NAME_SQL, setSample(sample));
    }

    /** query for the step rollup problem IDs. */
    private static final String PROBLEM_ID_SQL =
        "select distinct problem_id from step_rollup where sample_id = ?";

    /**
     * Get the distinct step rollup problem IDs for the sample.
     * @param sample the sample
     * @return the distinct step rollup problem IDs for the sample
     */
    private List<String> getProblemIds(final SampleItem sample) {
        return new ArrayList<String>() { {
            List<Object> probids = executeSQLQuery(PROBLEM_ID_SQL, setSample(sample));
            for (Object probid : probids) { add(probid.toString()); }
        } };
    }

    /** query for mapping problem IDs to hierarchy strings. */
    private static final String PROBLEM_HIERARCHY_SQL =
        "select distinct p.problem_id as problem_id,"
        + " (select group_concat(concat(dl2.level_title, ' ', dl2.level_name)"
        + " order by dl2.lft separator ', ')"
        + " from dataset_level dl2"
        + " where dl2.lft <= dl.lft and dl2.rgt >= dl.rgt"
        + " and dl.dataset_id = dl2.dataset_id"
        + " group by dl2.dataset_id) as problem_hierarchy"
        + " from dataset_level dl"
        + " join problem p using (dataset_level_id)"
        + " where problem_id in ";

    /**
     * Map problem IDs to their problem hierarchy string.
     * @param sample the sample
     * @return problem IDs mapped to their problem hierarchy string
     */
    public Map<Integer, String> getProblemHierarchy(final SampleItem sample) {
        final String query = PROBLEM_HIERARCHY_SQL + "(" + join(", ", getProblemIds(sample))
            + ")";
        return new HashMap<Integer, String>() { {
            List<Object[]> results = executeSQLQueryMaxConcat(query, new PrepareQuery() {
                @Override
                public void prepareQuery(final SQLQuery query) {
                    addScalars(query, "problem_id", INTEGER, "problem_hierarchy", STRING);
                }
            });
            for (Object[] result : results) {
                put((Integer)result[0], (String)result[1]);
            }
        } };
    }

    /** Default batch size. */
    private static final Integer DEFAULT_BATCH_SIZE = new Integer(1000);
    /** Name of the update step rollup conditions stored procedure. */
    private static final String SET_STEP_ROLLUP_COND_SP_NAME
        = "set_step_rollup_conditions";

    /**
     * This method is a shortcut for setting condition data in the step_rollup table without having
     * to completely re-aggregate a sample (used by the DataFixer, for example).
     * If you need to properly aggregate a dataset/sample you
     * SHOULD NOT use this method, as it depends on data for the sample already being present in the
     * step_rollup table.
     * @param sample the sample to process.
     * @param batchSize the size of the batch.
     * @return true if successful, false otherwise.
     */
    public boolean callSetStepRollupConditionsProcedure(SampleItem sample, Integer batchSize) {
        boolean result = true;
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        long totalSessions = sampleDao.getNumSessions(sample);
        batchSize = batchSize == null ? DEFAULT_BATCH_SIZE : batchSize;

        for (int batchOffset = 0; batchOffset < totalSessions;
                batchOffset += batchSize) {
            String query  = buildSPCall(SET_STEP_ROLLUP_COND_SP_NAME, sample.getId(),
                    batchOffset, batchSize);
            logger.info("callSetStepRollupConditionsProcedure offset:: "
                    + batchOffset + ", limit:: " + batchSize);
            try {
                callSP(query);
            } catch (SQLException exception) {
                result = false;
                logger.error("Exception caught while executing the "
                    + SET_STEP_ROLLUP_COND_SP_NAME + " stored procedure for "
                    + sample.getSampleName() + " (" + sample.getId() + ").",
                    exception);
                break;
            }
        }
        return result;
    }

    /** query for mapping problem IDs to hierarchy strings. */
    private static final String MAX_DISTINCT_SKILLS_ACROSS_STEPS_SQL =
        " SELECT COUNT(DISTINCT skill_id) as count FROM step_rollup"
        + " WHERE sample_id = :sampleId AND skill_model_id = :skillModelId"
        + " GROUP BY step_id ORDER BY count DESC";

    /**
     * Get the max number of skills across steps for the given sample and skill model,
     * using a Native SQL Query.
     * @param sample the sample
     * @param skillModel the skill model
     * @return the max() number of skills across steps.
     */
    public Long getMaxDistinctSkillsAcrossSteps(SampleItem sample, SkillModelItem skillModel) {
        Long maxDistinctSkills = new Long(0);
        Session session = getSession();
        try {
            SQLQuery sqlQuery = session.createSQLQuery(MAX_DISTINCT_SKILLS_ACROSS_STEPS_SQL);
            sqlQuery.setInteger("sampleId", ((Integer)sample.getId()).intValue());
            sqlQuery.setLong("skillModelId", ((Long)skillModel.getId()).longValue());
            sqlQuery.addScalar("count", LONG);

            List dbResults = sqlQuery.list();
            if (dbResults.size() > 0) {
                maxDistinctSkills = (Long)dbResults.get(0);
            }
        } finally {
            releaseSession(session);
        }

        return maxDistinctSkills;
    }

    /**
     * Log the student-step export.
     * @param datasetItem the DatasetItem
     * @param userItem the UserItem or null to log with the SystemLogger
     * @param stepRollupExportOptions the StepRollupExportOptions
     * @param skillModelItem the SkillModelItem or null to include all KC models
     * @param value the value to record in the value field
     * @param getCached whether or not to get the cached file
     */
    public void logStepRollupExport(DatasetItem datasetItem, UserItem userItem,
            StepRollupExportOptions stepRollupExportOptions, SkillModelItem skillModelItem,
            int value, boolean getCached) {
        if (userItem == null) {
            return;
        }

        int totalSamplesExported = stepRollupExportOptions.getSamples() != null
            ? stepRollupExportOptions.getSamples().size() : 1;
        String msgHead = "Exported " + totalSamplesExported + " sample(s): ";
        String msgSamples = new String("");
        for (SampleItem sampleItem : stepRollupExportOptions.getSamples()) {
            msgSamples = new String(msgSamples + sampleItem.getNameAndId() + ", ");
        }
        String cachedOrGenerated = null;
        if (!getCached) {
            cachedOrGenerated = " Generated file.";
        } else {
            cachedOrGenerated = " Cached file.";
        }
        String msgKCs = null;
        if (stepRollupExportOptions.isDisplayAllModels() || getCached) {
            msgKCs = new String(" All KC Models");
        } else if (stepRollupExportOptions.isDisplaySkills()) {
            if (skillModelItem != null) {
                msgKCs = new String(" '" + skillModelItem.getSkillModelName() + "' KC Model");
            }
        } else {
            msgKCs = new String(" No KC Models");
        }
        String msg = new String(msgHead + msgSamples + cachedOrGenerated + msgKCs);
        UserLogger.log(datasetItem, userItem, EXPORT_STEP_ROLLUP, msg);
        // end logging message

    }

    /** Returns a list of KCs associated with a step. */
    private static final String KCS_FOR_STEP = "select ifnull(group_concat("
        + " distinct skill_name ORDER BY skill_name SEPARATOR '<br/>'), '')"
        + " from step_rollup sr"
        + " join skill using (skill_id)"
        + " where step_id = :stepId"
        + " and sr.skill_model_id = :skillModelId"
        + " order by skill_name";

    /** Returns a list of KCs associated with a problem. */
    private static final String KCS_FOR_PROBLEM = "select ifnull(group_concat("
        + " distinct skill_name ORDER BY skill_name SEPARATOR '<br/>'), '')"
        + " from step_rollup sr"
        + " join skill using (skill_id)"
        + " where problem_id = :problemId"
        + " and sr.skill_model_id = :skillModelId"
        + " order by skill_name";

    /**
     * Returns a list of KCs associated with a step or problem and opportunity.
     * @param measureId the step or problem id
     * @param skillModelId the skill model id
     * @param kcsForProblem flag to indicate if getting list for problem
     * @return a list of KCs associated with a step or problem
     */
    public String getKCsForTooltip(Long measureId, Long skillModelId, Boolean kcsForProblem) {
        Session session = getSession();
        String kcString = new String("");
        try {
            String query = KCS_FOR_STEP;
            if (kcsForProblem) { query = KCS_FOR_PROBLEM; }

            SQLQuery sqlQuery = session.createSQLQuery(query.toString());
            if (kcsForProblem) {
                sqlQuery.setLong("problemId", measureId);
            } else {
                sqlQuery.setLong("stepId", measureId);
            }
            sqlQuery.setLong("skillModelId", skillModelId);
            List dbResults = sqlQuery.list();
            if (dbResults.size() > 0) {
                String kcList = (String)dbResults.get(0);
                // delimit html entities
                String delimHtml = new String(HtmlUtils.htmlEscape(kcList));
                // restore line breaks
                String restoreBreaks = new String(delimHtml.replaceAll("&lt;br/&gt;", "<br/>"));
                kcString = new String(restoreBreaks);
            }
        } catch (Exception e) {
            // Failure to get the KCs should not be propagated.
            StringBuffer sb = new StringBuffer();
            if (kcsForProblem) {
                sb.append("problemId = ");
            } else {
                sb.append("stepId = ");
            }
            sb.append(measureId);
            sb.append(", skillModelId = ").append(skillModelId);
            logDebug("Failed to get KCs [ ", sb.toString(), "] ", e);
            kcString = null;
        } finally {
            releaseSession(session);
        }

        return kcString;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(final Object... args) { LogUtils.logDebug(logger, args); }

} // end StepRollupDaoHibernate.java
