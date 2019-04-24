/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static edu.cmu.pslc.datashop.helper.UserLogger.EXPORT_PROBLEM_STUDENT;
import static org.hibernate.Hibernate.DOUBLE;
import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.LONG;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.TIMESTAMP;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.dto.StudentProblemInfo;
import edu.cmu.pslc.datashop.dto.StudentProblemRollupOptions;
import edu.cmu.pslc.datashop.dto.StudentProblemSkillInfo;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.StudentProblemRollupItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Class for retrieving student problem rollup items from the database.
 *
 * @author Jim Rankin
 * @version $Revision: 9487 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-06-25 10:00:31 -0400 (Tue, 25 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemRollupDaoHibernate extends AbstractDaoHibernate
    implements StudentProblemRollupDao {
    /** Default limit for the preview. */
    private static final int MAX_PAGE_GRID_ROWS = 100;
    /**
     * Fetch all student problem rollup items for the given options.
     * @param options the {@link StudentProblemRollupOptions}
     * @return all student problem rollup items for the given options
     */
    public List<StudentProblemRollupItem> getStudentProblemPreview(
            StudentProblemRollupOptions options) {
        if (options.getLimit() == null) {
            options.setLimit(MAX_PAGE_GRID_ROWS);
        }
        if (options.getOffset() == null) {
            options.setOffset(0);
        }

        // Keep track of where we start saving student problem rows
        // based on the offset and batching.
        Integer offsetPointer = 0;

        // Get the student problem rows for each sample until we fulfill the offset and limit.
        List<StudentProblemRollupItem> results = new ArrayList <StudentProblemRollupItem>();
        int currentRow = 0;

        Map<String, Integer> minAndMaxStudentIds = getMinAndMaxStudentIds(options);
        int minStudentId = minAndMaxStudentIds.get("min");
        int maxStudentId = minAndMaxStudentIds.get("max");
        logTrace("Selecting min and max student Id's for the preview: min(" + minStudentId
                + "), max(" + maxStudentId + ").");
        for (SampleItem selectedSample : options.getSamples()) {


            long totalNumRowsThisSample = numberOfStudentProblems(selectedSample, options);
            logDebug("Student-Problem Rollup: " + totalNumRowsThisSample
                + " total rows in sample " + selectedSample.getNameAndId());
            if (currentRow + totalNumRowsThisSample < options.getOffset()) {
                currentRow += totalNumRowsThisSample;
            } else if (currentRow >= options.getOffset() + options.getLimit()) {
                break;
            } else {
                // get the rows from this sample that are g.t.e. the offset
                // or l.t.e. the limit
                int innerOffset = options.getOffset() > currentRow
                    ? options.getOffset() - currentRow : 0;
                long innerLimit = totalNumRowsThisSample - innerOffset;
                if (currentRow + totalNumRowsThisSample
                        >= options.getOffset() + options.getLimit()) {
                    innerLimit = options.getOffset() + options.getLimit()
                            - currentRow - innerOffset;
                }

                int offsetByStudentId = minStudentId;
                int sampleRowsBeforeMinStudentId = 0;
                if (innerOffset > 0) {
                    // Get the derived offset based on the number
                    // of rows we're skipping by using the student id range
                    // in the where clause for the preview
                    offsetByStudentId = getMinStudentId(
                            (Integer) selectedSample.getId(), options);
                    sampleRowsBeforeMinStudentId = countToMinStudentId(
                        selectedSample, options, offsetByStudentId);
                    innerOffset = innerOffset - sampleRowsBeforeMinStudentId;
                }

                List sampleResults = getStudentProblemPreview(
                        selectedSample, options, innerOffset,
                        (int) innerLimit, offsetByStudentId, maxStudentId);
                logDebug("Student-Problem Rollup: " + sampleResults.size()
                        + " rows returned for sample " + selectedSample.getNameAndId());
                results.addAll(sampleResults);
                // Increment the current row for all samples
                currentRow += totalNumRowsThisSample;
            }
        }

        // Only add KC columns and values to the preview after we've know which
        // student problem rows are being requested based on limit and offset.
        for (SampleItem selectedSample : options.getSamples()) {
            // If isDisplaySkills is true, then get the additional
            // KC columns' values from the problem / skill-name mapping
            if (options.isDisplaySkills()) {
                // Query the database for the skill info. This data structure existed
                // before Trac #95.
                Map<StudentProblemSkillInfo, StudentProblemSkillInfo> skillInfoMap =
                    getStudentProblemSkillNameMapping(selectedSample, options);
                // The key to the map is a new skill info item based on the student problem row.
                // The result from the map is the corresponding stored skill info item.
                // It's an odd way of doing things, but I didn't change their data structure
                // since it works just fine.
                for (Iterator<StudentProblemRollupItem> iterator = results.iterator();
                        iterator.hasNext();) {
                    final StudentProblemRollupItem sprItem = iterator.next();

                    // Get this filled-in skill info item from the map, using a new skill info item.
                    StudentProblemSkillInfo info = new StudentProblemSkillInfo() { {
                        setStudentId(sprItem.getStudentId());
                        setProblemId(sprItem.getProblemId());
                        setProblemView(sprItem.getProblemView());
                    } };
                    info = skillInfoMap.get(info);

                    // If the KC info exists, add it to the preview row:
                    // Number of KCs, Steps without KCs, and KC List.
                    if (info != null) {
                        //
                        sprItem.setNumberOfKCs(info.getNumSkills());
                        if (info.getSkillList() == null || info.getSkillList().isEmpty()) {
                            sprItem.setKcList(".");
                        } else {
                            sprItem.setKcList(info.getSkillList());
                        }
                        sprItem.setStepsWithoutKCs(info.getNumUnmappedSteps());
                    }
                }
            }
        }

        return Collections.unmodifiableList(((results == null)
                ? new ArrayList () : results));
    }


    /** SQL query for selecting student problem rollup data */
    private static final String STUDENT_PROBLEM_ROLLUP_QUERY =
        "SELECT * FROM (SELECT studentId, IFNULL(step_id, -1) as stepId, problemId, sample, "
        + " student, problem, problemView, sum(total_corrects) as corrects, "
        + " SUM(total_incorrects) as incorrects, 0 as latency, 0 as avgCorrect,"
        + " SUM(total_hints) as hints, COUNT(DISTINCT(step_id)) as steps,"
        + " (SUM(total_hints) + SUM(total_incorrects)) / COUNT(DISTINCT(step_id)) as avgAssistance,"
        + " SUM(IF(first_attempt = '2', 1, 0)) as correctFirstAttempts, "
        + " hierarchy as problemHierarchy,"
        + " SUM(IF((step_start_time IS NULL), 1, 0)) as numMissingStartTimes,"
        + " IF(min(step_start_time) < min(first_transaction_time),"
        + " MIN(step_start_time), min(first_transaction_time)) as startTime,"
        + " MAX(step_end_time) as endTime, conditions from"
        + " (SELECT distinct sr.student_id as studentId, step_id, sr.problem_id as problemId,"
        + " sample_name as sample, anon_user_id as student, problem_name as problem,"
        + " problem_view as problemView, first_attempt, total_corrects, total_incorrects,"
        + " total_hints, sr.conditions as conditions,"
        + " sr.dataset_id as dataset,"
        + " step_start_time, step_end_time, first_transaction_time"
        + " FROM step_rollup sr"
        + " JOIN sample ON sr.sample_id = sample.sample_id"
        + " JOIN student ON sr.student_id = student.student_id"
        + " JOIN problem ON sr.problem_id = problem.problem_id";


    /** Default SQL where clause using sample id. */
    private static final String WHERE_SAMPLE =
          " WHERE sr.sample_id = :sampleId";

    /** Batching the inner query by student_id for the student problem preview. */
    private static final String STUDENT_ID_BATCH_QUERY =
            " AND sr.student_id >= :batchOffset"
            + " AND sr.student_id <= :batchEnd";

    /** Batching the inner query by anon_user_id for the student problem export. */
    private static final String ANON_USER_ID_BATCH_QUERY =
            " AND sr.student_id IN ( :batchList )";

    /** SQL group by clause - limit and offset are appended to this */
    private static final String GROUPBY =
        " GROUP BY sr.student_id, sr.problem_id, sr.problem_view, sr.step_id) as innerResults"
        + " JOIN problem_hierarchy ph ON innerResults.problemId = ph.problem_id"
        + " AND innerResults.dataset = ph.dataset_id"
        + " GROUP BY studentId, problemId, problemView, step_id"
        + " WITH ROLLUP";
    /** SQL group by clause - limit and offset are appended to this */
    private static final String ROLLUP_WHERE_CLAUSE =
            ") as results WHERE problemView IS NOT NULL AND stepId < 0";
    /** Fast SQL clause - for student problem preview. */
    private static final String END_FAST_ORDERBY = "  ORDER BY studentId";

    /** SQL order by clause - for student problem export. */
    private static final String ORDERBY = "  ORDER BY student, startTime";

    /** SQL order by clause - for student problem export. */
    private static final String LIMIT_OFFSET = " LIMIT :limit OFFSET :offset";

    /**
     * Gets the student problem rows for a single sample, given the options.
     * @param sample the sample
     * @param options the {@link StudentProblemRollupOptions}
     * @param offset the starting number for the rows to return.
     * @param limit the number of rows to return.
     * @param minStudentId the minimum student id in the preview
     * @param maxStudentId the maximum student id in the preview
     * @return List of {@link StudentProblemRollupOption} items that match the results.
     */
    public List<StudentProblemRollupItem> getStudentProblemPreview(SampleItem sample,
            StudentProblemRollupOptions options, Integer offset, Integer limit,
            int minStudentId, int maxStudentId) {
        Session session = null;
        List<StudentProblemRollupItem> dbResults = null;
        try {
            session = getSession();

            StringBuffer queryStr = new StringBuffer(STUDENT_PROBLEM_ROLLUP_QUERY);
            queryStr.append(WHERE_SAMPLE);
            queryStr.append((options.getModel() != null)
                ? " and sr.skill_model_id = :modelId" : "");
            queryStr.append(STUDENT_ID_BATCH_QUERY);
            queryStr.append(inClauses(options));
            queryStr.append(GROUPBY);
            // Use fast order by.
            queryStr.append(ROLLUP_WHERE_CLAUSE);
            queryStr.append(END_FAST_ORDERBY);
            queryStr.append(LIMIT_OFFSET);

            // Create the query.
            SQLQuery sqlQuery = session.createSQLQuery(queryStr.toString());
            // Set attributes sample id, batch offset, batch end, and model id.
            sqlQuery.setInteger("sampleId", ((Integer)sample.getId()).intValue());
            if (offset != null && limit != null) {
                sqlQuery.setInteger("limit", limit);
                sqlQuery.setInteger("offset", offset);
            }

            sqlQuery.setInteger("batchEnd", maxStudentId);
            sqlQuery.setInteger("batchOffset", minStudentId);

            if (options.getModel() != null) {
                sqlQuery.setLong("modelId", (Long)options.getModel().getId());
            }
            // Get the results.
            addScalars(sqlQuery, "studentId", LONG, "stepId", LONG, "problemId", LONG,
                "sample", STRING, "student", STRING, "problem", STRING, "problemHierarchy", STRING,
                "problemView", INTEGER, "hints", INTEGER, "incorrects", INTEGER,
                "corrects", INTEGER, "steps", INTEGER, "avgAssistance", DOUBLE,
                "correctFirstAttempts", INTEGER, "startTime", TIMESTAMP, "endTime", TIMESTAMP,
                "numMissingStartTimes", INTEGER, "conditions", STRING);

            logTrace("querying with callback :: Sample ID: " + sample.getId()
                    + ", Min student id: " + minStudentId
                    + ", Max student id: " + maxStudentId
                    + ", Limit: " + limit
                    + ", Offset: " + offset
                        + ", query: "  + sqlQuery.getQueryString());

            sqlQuery.setResultTransformer(Transformers.aliasToBean(StudentProblemRollupItem.class));

            // Get the db results and release the session
            dbResults = (List<StudentProblemRollupItem>)sqlQuery.list();
        } finally {
            releaseSession(session);
        }

        return Collections.unmodifiableList(dbResults);
    }

    /**
     * Gets the student problem export's PreparedStatement for a single
     * sample with a given set of options. The prepared statement is returned
     * so that it can be cancelled by its caller.
     * @param session the session
     * @param sample the sample
     * @param options the {@link StudentProblemRollupOptions}
     * @param batchList a list of student_id's based on the ordered anon_user_id's
     * @return the prepared statement that can be cancelled by its caller
     * @throws SQLException the SQL Exception
     */
    public PreparedStatement getStudentProblemExportRows(Session session, SampleItem sample,
            StudentProblemRollupOptions options, String batchList)
                throws SQLException {
        PreparedStatement stmt = null;

        StringBuffer queryStr = new StringBuffer(STUDENT_PROBLEM_ROLLUP_QUERY);
        queryStr.append(WHERE_SAMPLE);
        queryStr.append((options.getModel() != null)
            ? " and sr.skill_model_id = :modelId" : "");
        queryStr.append(inClauses(options));
        // Batch the inner query by anon user id.
        if (batchList != null) {
            queryStr.append(ANON_USER_ID_BATCH_QUERY);
        }
        queryStr.append(GROUPBY);
        // Order the inner query by anon user id.
        queryStr.append(ROLLUP_WHERE_CLAUSE);
        queryStr.append(ORDERBY);

        // Select the columns for the student problem export.
        String preparedStmtQuery = queryStr.toString()
            .replaceAll(":sampleId", "" + (Integer) sample.getId())
                .replaceAll("\\*",
                    "sample, student, studentId,"
                    + " problemHierarchy, problem, problemId, problemView, startTime, endTime,"
                    + " latency, numMissingStartTimes, hints, incorrects, corrects, avgCorrect,"
                    + " steps, avgAssistance, correctFirstAttempts, conditions");

        // Use question marks instead of variable names so we can return the prepared statement.
        if (batchList != null) {
            preparedStmtQuery = preparedStmtQuery
                .replaceAll(":batchList", "" + batchList);
        }
        if (options.getModel() != null) {
            preparedStmtQuery = preparedStmtQuery
                .replaceAll(":modelId", "" + ((Long)options.getModel().getId()).longValue());
        }

        logTrace("querying with callback :: Student Batch List: ", batchList,
                " query: ", preparedStmtQuery);
        stmt = session.connection().prepareStatement(preparedStmtQuery);

        // Return the prepared statement that can be cancelled by its caller.
        return stmt;
    }

    /** Native MySql query for getting the min student id for the student problem preview. */
    private static final String COUNT_TO_MIN_STUDENT_ID = "select "
            + " count(distinct student_id, problem_id, problem_view)"
            + " from step_rollup sr"
            + " where sample_id = :sampleId"
            + " and student_id < :studentId";

    /** Gets the min student id for the student problem preview.
     * @param sampleItem the sample
     * @param options the StudentProblemRollupOptions
     * @param studentId the student id
     * @return the min student id
     */
    public Integer countToMinStudentId(final SampleItem sampleItem,
            final StudentProblemRollupOptions options, final Integer studentId) {
        StringBuffer query = new StringBuffer(COUNT_TO_MIN_STUDENT_ID
                + ((options.getModel() != null) ? " AND sr.skill_model_id = :modelId" : "")
                + inClauses(options));


        return getIntForSQL(query.toString(), new PrepareQuery() {
            @Override
             public void prepareQuery(SQLQuery query) {
                 query.setParameter("sampleId", ((Number)sampleItem.getId()).intValue());
                 query.setParameter("studentId", studentId);

                 if (options.getModel() != null) {
                     query.setParameter("modelId", options.getModel().getId());
                 }
             }
         }, 0L);
    }

    /** Native MySql query for getting the min student id for the student problem preview. */
    private static final String GET_MIN_STUDENT_ID = "select min(student_id)"
            + " from (select student_id from step_rollup sr"
            + " where sample_id in ( :sampleIds )";
    /** Native MySql query for getting the min student id for the student problem preview. */
    private static final String GET_MIN_STUDENT_GROUP_BY =
            " group by sample_id, student_id, problem_id, problem_view"
            + " order by sample_id, student_id limit :limit offset :offset) innerQuery";

    /** Gets the min student id for the student problem preview.
     * @param sampleId the sample id
     * @param options the StudentProblemRollupOptions
     * @return the min student id
     */
    public Integer getMinStudentId(Integer sampleId, final StudentProblemRollupOptions options) {
        List<Integer> sampleIds = new ArrayList<Integer>();
        sampleIds.add(sampleId);
        return getMinStudentId(sampleIds, options);
    }
    /** Gets the min student id for the student problem preview.
     * @param options the StudentProblemRollupOptions
     * @return the min student id
     */
    public Integer getMinStudentId(final StudentProblemRollupOptions options) {
        List<Integer> sampleIds = new ArrayList<Integer>();
        for (SampleItem sampleItem : options.getSamples()) {
            sampleIds.add((Integer) sampleItem.getId());
        }
        return getMinStudentId(sampleIds, options);
    }

    /** Gets the min student id for the student problem preview.
     * @param sampleIds the sample Id list
     * @param options the StudentProblemRollupOptions
     * @return the min student id
     */
    public Integer getMinStudentId(final List<Integer> sampleIds,
            final StudentProblemRollupOptions options) {

        StringBuffer query = new StringBuffer(GET_MIN_STUDENT_ID
                + ((options.getModel() != null) ? " AND sr.skill_model_id = :modelId" : "")
                + inClauses(options));

        query.append(GET_MIN_STUDENT_GROUP_BY);

        logger.info("querying with callback :: query: "  + query.toString());

        return getIntForSQL(query.toString(), new PrepareQuery() {
            @Override
             public void prepareQuery(SQLQuery query) {
                 query.setParameterList("sampleIds", sampleIds, Hibernate.INTEGER);
                 query.setParameter("limit", options.getLimit());
                 query.setParameter("offset", options.getOffset());

                 if (options.getModel() != null) {
                     query.setParameter("modelId", options.getModel().getId());
                 }
             }
         }, 0L);
    }

    /** Native MySql query for getting the min student id for the student problem preview. */
    private static final String GET_MINMAX_STUDENT_ID = "select max(student_id) as maxStudentId,"
            + " min(student_id) as minStudentId from (select student_id from step_rollup sr"
            + " where sample_id in ( :sampleIds )";
    /** Native MySql query for getting the min student id for the student problem preview. */
    private static final String GET_MINMAX_STUDENT_GROUP_BY =
            " group by sample_id, student_id, problem_id, problem_view"
            + " order by sample_id, student_id limit :limit offset :offset) innerQuery";

    /** Gets the min student id for the student problem preview.
     * @param options the StudentProblemRollupOptions
     * @return the min student id
     */
    public Map<String, Integer> getMinAndMaxStudentIds(final StudentProblemRollupOptions options) {

        final List<Integer> sampleIds = new ArrayList<Integer>();
        Map<String, Integer> exportProperties = new HashMap<String, Integer>();
        StringBuffer sampleStringBuffer = new StringBuffer("");
        for (SampleItem sampleItem : options.getSamples()) {
            sampleIds.add((Integer) sampleItem.getId());
            sampleStringBuffer.append(sampleItem.getId() + ", ");
        }

        Session session = null;
        StringBuffer queryString = new StringBuffer(GET_MINMAX_STUDENT_ID
                + ((options.getModel() != null) ? " AND sr.skill_model_id = :modelId" : "")
                + inClauses(options));
            queryString.append(GET_MINMAX_STUDENT_GROUP_BY);

            try {
                session = getSession();
                SQLQuery sqlQuery = session.createSQLQuery(queryString.toString());

                // Declare query column types
                sqlQuery.addScalar("minStudentId", Hibernate.INTEGER);
                sqlQuery.addScalar("maxStudentId", Hibernate.INTEGER);
                // Declare query parameters
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("limit", options.getLimit());
                params.put("offset", options.getOffset());
                if (options.getModel() != null) {
                    params.put("modelId", options.getModel().getId());
                }
                params.put("sampleIds", sampleIds);

                // set the parameters
                if (params != null && params.size() > 0) {
                    for (Map.Entry<String, Object> param : params.entrySet()) {
                        if (param.getKey().equals("sampleIds")) {
                            sqlQuery.setParameterList("sampleIds", sampleIds, Hibernate.INTEGER);
                        } else {
                            sqlQuery.setParameter(param.getKey(), param.getValue());
                        }
                    }
                }
                logTrace("querying with callback :: Sample(s): " + sampleStringBuffer.toString()
                        + ", Model: " + (options.getModel() != null ? options.getModel() : "null")
                        + ", Limit: " + options.getLimit()
                        + ", Offset: " + options.getOffset()
                            + ", query: "  + sqlQuery.getQueryString());

                List<Object[]> dbResults = sqlQuery.list();
                for (Object[] obj: dbResults) {
                    int colIdx = 0;
                    int minStudentId = (Integer)obj[colIdx++];
                    int maxStudentId = (Integer)obj[colIdx++];
                    exportProperties.put("min", minStudentId);
                    exportProperties.put("max", maxStudentId);
                }

            } finally {
                releaseSession(session);
            }
            return exportProperties;
    }

    /** Returns a student_id batch list for the student problem export based on anon user id. */
    private static final String STUDENT_BATCH_LIST_QUERY =
        "SELECT GROUP_CONCAT(student_id) as batch FROM"
        + " (SELECT DISTINCT student_id, anon_user_id FROM step_rollup sr"
        + " LEFT JOIN student stu using (student_id)";

    /** Returns a student_id batch list based on the ordered anon_user_id's. */
    private static final String GROUP_BY_STUDENT_BATCH_LIST_QUERY =
        " GROUP BY student_id"
        + " ORDER BY anon_user_id"
        + " LIMIT :batchLimit"
        + " OFFSET :batchOffset"
        + " ) as innerResults";

    /**
     * Returns a student_id batch list for the student problem export based on anon user id.
     * @param sampleItem the sample
     * @param batchOffset the offset
     * @param studentBatchSize the limit
     * @param options the StudentProblemRollupOptions that contain the student list
     * @return a batch of student_id's based on the ordered anon_user_id's
     */
    public String getStudentIdBatchList(SampleItem sampleItem, int batchOffset,
            Integer studentBatchSize, StudentProblemRollupOptions options) {
        Session session = null;
        List dbResults = null;
        try {
            session = getSession();

            String query = STUDENT_BATCH_LIST_QUERY
                + WHERE_SAMPLE
                + ((options.getModel() != null) ? " AND sr.skill_model_id = :modelId" : "")
                + inClauses(options)
                + GROUP_BY_STUDENT_BATCH_LIST_QUERY;

            // Get the query based on the options.
            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.setInteger("sampleId", (Integer)sampleItem.getId());
            sqlQuery.setInteger("batchOffset", (Integer)batchOffset);
            sqlQuery.setInteger("batchLimit", (Integer)studentBatchSize);
            addScalars(sqlQuery, "batch", STRING);

            if (options.getModel() != null) {
                sqlQuery.setLong("modelId", (Long)options.getModel().getId());
            }

            logTrace("StudentProblemRollupDao:: getStudentIdBatchList:: Calling query: ", query);

            dbResults = sqlQuery.list();
        } finally {
            releaseSession(session);
        }
        // Return the group-concatenated list, if it's filled in.
        if (dbResults != null && dbResults.get(0) != null) {
            return dbResults.get(0).toString();
        } else {
            return null;
        }
    }

    /**
     * Return the total number of student problem rows for the options.
     * @param options the StudentProblemRollupOptions
     * @return total number of student problem rows for the options.
     */
    public long numberOfStudentProblems(StudentProblemRollupOptions options) {
        long total = 0;
        for (SampleItem sampleItem : options.getSamples()) {
            total += numberOfStudentProblems(sampleItem, options);
        }
        return total;
    }

    /**
     * Get the student problem headers.
     * @return the student problem headers
     */
    public List<String> getProblemRollupHeaders() {
        return StudentProblemInfo.STATIC_HEADERS;
    }


    /** Generic count of the student problem rows. */
    private static final String STUDENT_PROBLEM_ROLLUP_COUNT_QUERY =
        " select count(distinct student_id, problem_id, problem_view)"
        + " from step_rollup sr";

    /**
     * Gets the count of student problem rows for a sample and set of options.
     * @param sample the sample item
     * @param options the options
     * @return the count of student problem rows for a sample and set of options
     */
    public long numberOfStudentProblems(SampleItem sample,
            StudentProblemRollupOptions options) {
        // If the options are null, create a default options object.
        if (options == null) {
            options = new StudentProblemRollupOptions();
        }
        Session session = null;
        List dbResults = null;
        try {
            session = getSession();
            String query = STUDENT_PROBLEM_ROLLUP_COUNT_QUERY
                + WHERE_SAMPLE
                + ((options.getModel() != null) ? " and sr.skill_model_id = :modelId" : "")
                + inClauses(options);

            logTrace("Getting number of StudentProblems for sample ", sample.getId(), " and model ",
                    ((options.getModel() != null) ? options.getModel().getId() : "null"),
                    " with query: ", query);

            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.setInteger("sampleId", (Integer)sample.getId());
            if (options.getModel() != null) {
                sqlQuery.setLong("modelId", (Long)options.getModel().getId());
            }

            dbResults = sqlQuery.list();
        } finally {
            releaseSession(session);
        }

        if (dbResults.size() > 0) {
            return ((BigInteger)dbResults.get(0)).longValue();
        } else {
            return 0;
        }
    }

    /**
     * Group by clause for the KC models info.
     */
    private static final String PROBLEM_SKILL_GROUPBY =
        " group by sr.student_id, sr.problem_id, sr.problem_view";

    /**
     * Get the KC models info for the desired model.
     * <strong>NOTE: </strong> startTime is important for the ordering of the results.
     */
    private static final String STUDENT_PROBLEM_SKILL_QUERY =
        "SELECT student_id as studentId, problem_id as problemId, problem_view as problemView,"
        + " group_concat("
        + "     DISTINCT sk.skill_name ORDER BY sk.skill_name SEPARATOR ', ') as skillList,"
        + " SUM(IF(sr.skill_id IS NULL, 1, 0)) as numUnmappedSteps,"
        + " COUNT(distinct sk.skill_id) as numSkills"
        + " FROM step_rollup sr"
        + " LEFT JOIN skill sk ON sr.skill_id = sk.skill_id"
        + " WHERE sr.sample_id = :sampleId AND sr.skill_model_id = :modelId";
    /** Limit the KC information to a list of students. */
    private static final String WHERE_STUDENTS_IN =
        " AND sr.student_id in ( :studentIds )";
    /** Limit the KC information to a list of problems. */
    private static final String WHERE_PROBLEMS_IN =
        " AND sr.problem_id in ( :problemIds )";
    /**
     * Returns the skill information used by the export and preview in a map
     * referenced by skill model.
     * @param sampleItem the sample item
     * @param options the options
     * @param studentIds a list of studentIds or null if all are desired
     * @param problemItems a list of problem items or null if all are desired
     * @return the skill information used by the export and preview in a map
     * referenced by skill model.
     */
    public Map<SkillModelItem, Map<StudentProblemSkillInfo, StudentProblemSkillInfo>>
        getStudentProblemSkillNameMappingAllModels(SampleItem sampleItem,
            StudentProblemRollupOptions options, List<StudentItem> studentIds,
            List<ProblemItem> problemItems) {
        // The KC model info is stored in a self-referential map that can be accessed
        // with information from the student problem export query. This method keeps
        // these self-referential maps in a parent map with the key: skill model.
        Map<SkillModelItem, Map<StudentProblemSkillInfo, StudentProblemSkillInfo>> skillInfoMap =
            new HashMap<SkillModelItem, Map<StudentProblemSkillInfo, StudentProblemSkillInfo>>();
        Session session = null;
        try {
            session = getSession();
            // First, get all skill models for this dataset.
            SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();
            List<SkillModelItem> smItems = smDao.find(sampleItem.getDataset());
            for (SkillModelItem model : smItems) {
                // Store KC model info in a map.
                Map modelSkillInfo = new HashMap<
                    StudentProblemSkillInfo, StudentProblemSkillInfo>();
                StringBuffer query = new StringBuffer(STUDENT_PROBLEM_SKILL_QUERY);
                if (studentIds != null && !studentIds.isEmpty()) {
                    query.append(WHERE_STUDENTS_IN);
                }

                if (Boolean.valueOf(options.isDisplaySkills())) {
                    inSkills(query, options);
                }
                query.append(PROBLEM_SKILL_GROUPBY);

                SQLQuery sqlQuery = session.createSQLQuery(query.toString());
                logTrace("querying student problem skill mapping: ", sqlQuery.getQueryString());
                addScalars(sqlQuery, "studentId", LONG, "problemId", LONG, "problemView", INTEGER,
                        "skillList", STRING, "numUnmappedSteps", INTEGER, "numSkills", INTEGER);
                // set the parameter(s)
                sqlQuery.setInteger("sampleId", (Integer)sampleItem.getId());
                sqlQuery.setLong("modelId", (Long)model.getId());
                if (studentIds != null && !studentIds.isEmpty()) {
                    sqlQuery.setParameterList("studentIds", studentIds);
                }
                // Set the result transformer to StudentProblemSkillInfo
                sqlQuery.setResultTransformer(
                    Transformers.aliasToBean(StudentProblemSkillInfo.class));
                List<StudentProblemSkillInfo> results = (
                    List<StudentProblemSkillInfo>)sqlQuery.list();

                for (StudentProblemSkillInfo info : results) {
                    modelSkillInfo.put(info, info);
                }

                skillInfoMap.put(model, modelSkillInfo);
            }
        } finally {
            releaseSession(session);
        }

        return skillInfoMap;
    }

    /**
     * Get the skill information for a given sample and set of options. Pre-dates Trac #95.
     * @param sample the sample to get skill information for.
     * @param options the options to limit the results.
     * @return Map of skill info keyed to the student, problem, and problem view
     */
    public Map<StudentProblemSkillInfo, StudentProblemSkillInfo> getStudentProblemSkillNameMapping(
            SampleItem sample, StudentProblemRollupOptions options) {
        Map<StudentProblemSkillInfo, StudentProblemSkillInfo> skillInfo =
            new HashMap<StudentProblemSkillInfo, StudentProblemSkillInfo>();
        if (options.getModel() == null) { return skillInfo; }

        StringBuffer query = new StringBuffer(STUDENT_PROBLEM_SKILL_QUERY
            + inClauses(options));

        query.append(PROBLEM_SKILL_GROUPBY);

        Session session = null;
        List<StudentProblemSkillInfo> results = null;
        try {
            session = getSession();

            SQLQuery sqlQuery = session.createSQLQuery(query.toString());
            logTrace("querying student problem skill mapping: ", sqlQuery.getQueryString());

            addScalars(sqlQuery, "studentId", LONG, "problemId", LONG, "problemView", INTEGER,
                    "skillList", STRING, "numUnmappedSteps", INTEGER, "numSkills", INTEGER);

            // set the parameter(s)
            sqlQuery.setInteger("sampleId", (Integer)sample.getId());
            sqlQuery.setLong("modelId", (Long)options.getModel().getId());

            // Get the KC model info.
            sqlQuery.setResultTransformer(Transformers.aliasToBean(StudentProblemSkillInfo.class));
            results = (List<StudentProblemSkillInfo>)sqlQuery.list();
        } finally {
            releaseSession(session);
        }

        // Put the KC models info into a row that will be appended to the student problem row.
        for (StudentProblemSkillInfo row : results) {
            skillInfo.put(row, row);
        }
        return skillInfo;
    }

    /**
     * Append "in" clause for selected KCs. Pre-dates Trac #95.
     * @param whStr Buffer for constructing a 'where' clause.
     * @param options the options DTO which holds the student problem parameters
     */
    private void inSkills(StringBuffer whStr, StudentProblemRollupOptions options) {
        List<SkillItem> skills = options.getSkills();
        if (skills != null && skills.size() > 0) {
            whStr.append(" and (sr.skill_id ");
            whStr.append(inItemIds(skills));
            if (options.isIncludeUnmappedSteps()) {
                whStr.append(" or sr.skill_id IS NULL");
            }
            whStr.append(")");
        } else if (options.isDisplaySkills() && options.isIncludeUnmappedSteps()) {
            whStr.append(" and sr.skill_id IS NULL");
        }
    }

    /**
     * append "in" clauses for each list to the where clause. Pre-dates Trac #95.
     * @param options the DTO which holds the parameters for the student problem tables
     * @return "in" clauses for each list appended to the where clause.
     */
    private StringBuffer inClauses(StudentProblemRollupOptions options) {
        StringBuffer whStr = new StringBuffer("");
        if (options.getStudents() != null) {
            andInItemIds(whStr, "sr.student_id", options.getStudents());
        }
        if (options.getProblems() != null) {
            andInItemIds(whStr, "sr.problem_id", options.getProblems());
        }
        inSkills(whStr, options);
        return whStr;
    }

    /** 'populate_problem_hierarchy' stored procedure name. */
    private static final String POPULATE_PROBLEM_HIERARCHY_SP_NAME
        = "populate_problem_hierarchy";
    /**
     * This method calls the populate_problem_hierarchy stored
     * procedure, operating on dataset_levels for the given dataset.
     * @param datasetItem the dataset associated with the problem hierarchy
     * @return the number of rows created in problem_hierarchy for this dataset
     */
    public Integer callPopulateProblemHierarchy(DatasetItem datasetItem) {
        if (datasetItem == null) {
            throw new IllegalArgumentException("Dataset cannot be null.");
        }
        Integer problemHierarchyRowsCreated = null;
        String query  = buildSPCall(POPULATE_PROBLEM_HIERARCHY_SP_NAME, datasetItem.getId());
        logger.info("callPopulateProblemHierarchy:: " + POPULATE_PROBLEM_HIERARCHY_SP_NAME
                + " starting");
        Session session = null;
        try {
            session = getSession();
            PreparedStatement ps = session.connection().prepareStatement(query);
            problemHierarchyRowsCreated = ps.executeUpdate();

        } catch (SQLException exception) {
            logger.error("Exception caught while executing the "
                + POPULATE_PROBLEM_HIERARCHY_SP_NAME + " stored procedure.",
                exception);
        } finally {
            releaseSession(session);
        }
        return problemHierarchyRowsCreated;
    }

    /**
     * Log the student-problem export.
     * @param datasetItem the DatasetItem
     * @param userItem the UserItem or null to log with the SystemLogger
     * @param sprOptions the StudentProblemRollupOptions
     * @param skillModelItem the SkillModelItem or null to include all KC models
     * @param value the value to record in the value field
     * @param getCached whether or not to get the cached file
     */
    public void logStudentProblemExport(DatasetItem datasetItem, UserItem userItem,
            StudentProblemRollupOptions sprOptions, SkillModelItem skillModelItem,
            int value, boolean getCached) {
        if (userItem == null) {
            return;
        }

        int totalSamplesExported = sprOptions.getSamples() != null
            ? sprOptions.getSamples().size() : 1;
        String msgHead = "Exported " + totalSamplesExported + " sample(s): ";
        String msgSamples = new String("");
        for (SampleItem sampleItem : sprOptions.getSamples()) {
            msgSamples = new String(msgSamples + sampleItem.getNameAndId() + ", ");
        }
        String cachedOrGenerated = null;
        if (!getCached) {
            cachedOrGenerated = " Generated file.";
        } else {
            cachedOrGenerated = " Cached file.";
        }
        String msgKCs = null;
        if (sprOptions.isDisplayAllModels()) {
            msgKCs = new String(" All KC Models");
        } else if (sprOptions.isDisplaySkills()) {
            String msgIncludeUnmappedSteps = sprOptions.isIncludeUnmappedSteps()
                    ? " Include steps without KCs." : " Do not include steps without KCs.";
            if (skillModelItem != null) {
                msgKCs = new String(" '" + skillModelItem.getSkillModelName() + "' KC Model"
                        + msgIncludeUnmappedSteps);
            }
        } else {
            msgKCs = new String(" No KC Models");
        }
        String msg = new String(msgHead + msgSamples + cachedOrGenerated + msgKCs);
        UserLogger.log(datasetItem, userItem, EXPORT_PROBLEM_STUDENT, msg);
        // end logging message

    }

}