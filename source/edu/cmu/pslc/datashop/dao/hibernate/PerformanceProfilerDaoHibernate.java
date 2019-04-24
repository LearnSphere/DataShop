/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PerformanceProfilerDao;
import edu.cmu.pslc.datashop.dto.PerformanceProfilerBar;
import edu.cmu.pslc.datashop.dto.ProfilerOptions;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Hibernate/Spring implementation of the LearningCurveDao.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PerformanceProfilerDaoHibernate extends HibernateDaoSupport
    implements PerformanceProfilerDao {

    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** SQL of the order by option */
    private static final String ORDER_BY_PROBLEM_NAME = "p.problem_name";
    /** SQL of the order by option */
    private static final String ORDER_BY_LEVEL_NAME = "dl.level_name";
    /** SQL of the order by option */
    private static final String ORDER_BY_SKILL_NAME = "s.skill_name";
    /** SQL of the order by option */
    private static final String ORDER_BY_STEP_NAME = "sub.subgoal_name";
    /** SQL of the order by option */
    private static final String ORDER_BY_STUDENT_NAME = "stud.anon_user_id";
    /** SQL of the order by option */
    private static final String ORDER_BY_ERROR_RATE = "errorRate";
    /** SQL of the order by option */
    private static final String ORDER_BY_PREDICTED = "predicted";
    /** SQL of the order by option */
    private static final String ORDER_BY_RESIDUAL = "residual";
    /** SQL of the order by option */
    private static final String ORDER_BY_ASSISTANCE_SCORE = "assistanceScore";
    /** SQL of the order by option */
    private static final String ORDER_BY_NUM_HINTS = "numHints";
    /** SQL of the order by option */
    private static final String ORDER_BY_NUM_INCORRECTS = "numIncorrects";
    /** SQL of the order by option */
    private static final String ORDER_BY_FA_HINTS = "errorRateHints";
    /** SQL of the order by option */
    private static final String ORDER_BY_FA_INCORRECTS = "errorRateIncorrects";
    /** SQL of the order by option */
    private static final String ORDER_BY_NUMBER_OF_PROBLEMS = "numProblems";
    /** SQL of the order by option */
    private static final String ORDER_BY_NUMBER_OF_SKILLS = "numSkills";
    /** SQL of the order by option */
    private static final String ORDER_BY_NUMBER_OF_STEPS = "numSteps";
    /** SQL of the order by option */
    private static final String ORDER_BY_NUMBER_OF_STUDENTS = "numStudents";
    /** SQL of the order by option */
    private static final String ORDER_BY_STEP_DURATION = "stepDuration";
    /** SQL of the order by option */
    private static final String ORDER_BY_CORRECT_STEP_DURATION = "correctStepDuration";
    /** SQL of the order by option */
    private static final String ORDER_BY_ERROR_STEP_DURATION = "errorStepDuration";


    /** Select portion of the query by Problem */
    private static final String SELECT_BY_PROBLEM =
        "select sr.problem_id as typeId, p.problem_name as typeName, "
        + " p.dataset_level_id as typeParent,";

    /** Select portion of the query by Level */
    private static final String SELECT_BY_LEVEL =
        "select dl.dataset_level_id as typeId, dl.level_name as typeName, "
        + " dl.parent_id as typeParent, ";

    /** Select portion of the query by Skill */
    private static final String SELECT_BY_SKILL =
        "select sr.skill_id as typeId, s.skill_name as typeName,"
        + " -1 as typeParent,";

    /** Select portion of the query by Step */
    private static final String SELECT_BY_STEP =
        "select sr.step_id as typeId, sub.subgoal_name as typeName,"
        + " sub.problem_id as typeParent,";

    /** Select portion of the query by Student */
    private static final String SELECT_BY_STUDENT =
        "select sr.student_id as typeId, stud.anon_user_id as typeName,"
        + " -1 as typeParent,";

    /** The selects for all queries. */
    private static final String SELECTS =
        " avg(sr.total_incorrects) as numIncorrects,"
        + " avg(sr.total_hints) as numHints,"
        + " avg(sr.total_incorrects + sr.total_hints) as assistanceScore,"
        + " 100 * ((sum(if(sr.first_attempt = '1', 1,0)"
        +    " + if(sr.first_attempt = '0', 1,0))) / count(*)) as errorRate,"
        + " 100 * (sum(if(sr.first_attempt = '1',1,0)) / count(*)) as errorRateHints,"
        + " 100 * (sum(if(sr.first_attempt = '0',1,0)) / count(*)) as errorRateIncorrects,"
        + " avg(sr.predicted_error_rate) * 100 as predicted,"
        + " (avg(sr.predicted_error_rate) * 100)"
            + " - (100 * ((sum(if(sr.first_attempt = '1', 1,0)"
            + " + if(sr.first_attempt = '0', 1,0))) / count(*))) as residual,"
        + " AVG(sr.step_duration) as stepDuration,"
        + " AVG(sr.correct_step_duration) as correctStepDuration,"
        + " AVG(sr.error_step_duration) as errorStepDuration,"
        + " count(distinct sr.student_id) as numStudents,"
        + " count(distinct sr.step_id) as numSteps,"
        + " count(distinct sr.skill_id) as numSkills,"
        + " count(distinct sr.problem_id) as numProblems,"
        + " count(*) as observations"; //TODO fix this number of observations.

    /** 2nd part of a Native MySQL query for getting LC data from a step rollup item */
    private static final String SELECTS_WITH_SECONDARY =
        " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.total_incorrects))"
              + " as numIncorrects,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.total_hints)) as numHints,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId,"
              + " null, sr.total_incorrects + sr.total_hints))"
              + " as assistanceScore,"
        + " (SUM(if(sr.first_attempt='1' and sr2.skill_model_id != :secondaryModelId,1,0))"
          + " + SUM(if(sr.first_attempt='0' and sr2.skill_model_id != :secondaryModelId,1,0)))"
          + " / SUM(if(sr2.skill_model_id != :secondaryModelId,1,0)) * 100  as errorRate,"
        + " (SUM(if(sr.first_attempt='1' and sr2.skill_model_id != :secondaryModelId,1,0)))"
          + " / SUM(if(sr2.skill_model_id != :secondaryModelId,1,0)) * 100  as errorRateHints,"
        + " (SUM(if(sr.first_attempt='0' and sr2.skill_model_id != :secondaryModelId,1,0)))"
          + " / SUM(if(sr2.skill_model_id != :secondaryModelId,1,0)) * 100  as errorRateIncorrects,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.predicted_error_rate))"
          + " * 100 as predicted,"
        + " (AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.predicted_error_rate))"
            + " * 100) - "
            + " ((SUM(if(sr.first_attempt='1' and sr2.skill_model_id != :secondaryModelId,1,0))"
            + " + SUM(if(sr.first_attempt='0' and sr2.skill_model_id != :secondaryModelId,1,0)))"
            + " / SUM(if(sr2.skill_model_id != :secondaryModelId,1,0)) * 100) as residual,"
        + " AVG(sr.step_duration) as stepDuration,"
        + " AVG(sr.correct_step_duration) as correctStepDuration,"
        + " AVG(sr.error_step_duration) as errorStepDuration,"
        + " count(distinct sr.student_id) as numStudents,"
        + " count(distinct sr.step_id) as numSteps,"
        + " count(distinct sr.skill_id) as numSkills,"
        + " count(distinct sr.problem_id) as numProblems,"
        //TODO fix the number of observations
        + " SUM(if(sr.skill_model_id != :secondaryModelId,1,0)"
          + "  + if(sr.skill_id IS NULL,1,0)"
          + " - if (sr2.skill_model_id = :secondaryModelId,1,0))"
          + " as observations,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, sr2.predicted_error_rate, null)) * 100"
          + " as secondaryPredicted";

    /** FROM, JOIN and initial WHERE of by Problem */
    private static final String FROM_BY_PROBLEM =
        " from step_rollup sr"
        + " join problem p on p.problem_id = sr.problem_id"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and sr.first_attempt != '3'";

    /** FROM, JOIN and initial WHERE of by Problem with joins for secondary prediction. */
    private static final String FROM_BY_PROBLEM_WITH_SECONDARY =
        " from step_rollup sr, step_rollup sr2, problem p"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and p.problem_id = sr.problem_id"
        + " and sr.first_attempt != '3'"
        + " and sr.sample_id = sr2.sample_id"
        + " and sr.step_id = sr2.step_id"
        + " and sr.student_id = sr2.student_id";

    /** FROM, JOIN and initial WHERE of by Dataset Level */
    private static final String FROM_BY_LEVEL =
        " from step_rollup sr"
        + " join problem p on p.problem_id = sr.problem_id"
        + " join dataset_level dl on dl.dataset_id = sr.dataset_id"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and sr.first_attempt != '3'"
        + " and p.dataset_level_id in"
            + " (select dl2.dataset_level_id from dataset_level dl2"
            + " where dl2.lft >= dl.lft and dl2.rgt <= dl.rgt "
            + " and dl2.dataset_id = dl.dataset_id )";

    /** FROM, JOIN and initial WHERE of by Problem with joins for secondary prediction. */
    private static final String FROM_BY_LEVEL_WITH_SECONDARY =
        " from step_rollup sr, step_rollup sr2, problem p, dataset_level dl"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and p.problem_id = sr.problem_id"
        + " and dl.dataset_id = sr.dataset_id"
        + " and p.dataset_level_id in"
            + " (select dl2.dataset_level_id from dataset_level dl2"
            + " where dl2.lft >= dl.lft and dl2.rgt <= dl.rgt "
            + " and dl2.dataset_id = dl.dataset_id)"
        + " and sr.first_attempt != '3'"
        + " and sr.sample_id = sr2.sample_id"
        + " and sr.step_id = sr2.step_id"
        + " and sr.student_id = sr2.student_id";

    /** FROM, JOIN and initial WHERE of by Skill */
    private static final String FROM_BY_SKILL =
        " from step_rollup sr"
        + " join skill s on s.skill_id = sr.skill_id"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and sr.first_attempt != '3'";

    /** FROM, JOIN and initial WHERE of by Skill with joins for secondary prediction. */
    private static final String FROM_BY_SKILL_WITH_SECONDARY =
        " from step_rollup sr, step_rollup sr2, skill s"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and s.skill_id = sr.skill_id"
        + " and sr.first_attempt != '3'"
        + " and sr.sample_id = sr2.sample_id"
        + " and sr.step_id = sr2.step_id"
        + " and sr.student_id = sr2.student_id";

    /** FROM, JOIN and initial WHERE of by Step */
    private static final String FROM_BY_STEP =
        " from step_rollup sr"
        + " join subgoal sub on sub.subgoal_id = sr.step_id"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and sr.first_attempt != '3'";

    /** FROM, JOIN and initial WHERE of by Step with joins for secondary prediction. */
    private static final String FROM_BY_STEP_WITH_SECONDARY =
        " from step_rollup sr, step_rollup sr2, subgoal sub"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and sub.subgoal_id = sr.step_id"
        + " and sr.first_attempt != '3'"
        + " and sr.sample_id = sr2.sample_id"
        + " and sr.step_id = sr2.step_id"
        + " and sr.student_id = sr2.student_id";

    /** FROM, JOIN and initial WHERE of by Student */
    private static final String FROM_BY_STUDENT =
        " from step_rollup sr"
        + " join student stud on stud.student_id = sr.student_id"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and sr.first_attempt != '3'";

    /** FROM, JOIN and initial WHERE of by Step with joins for secondary prediction. */
    private static final String FROM_BY_STUDENT_WITH_SECONDARY =
        " from step_rollup sr, step_rollup sr2, student stud"
        + " where sr.sample_id = :sampleId"
        + " and sr.skill_model_id = :primarySkillModelId"
        + " and stud.student_id = sr.student_id"
        + " and sr.first_attempt != '3'"
        + " and sr.sample_id = sr2.sample_id"
        + " and sr.step_id = sr2.step_id"
        + " and sr.student_id = sr2.student_id";

    /** Group by portion of the clause */
    private static final String GROUP_BY_PROBLEM =
        " group by sr.problem_id";

    /** Group by portion of the clause */
    private static final String GROUP_BY_LEVEL =
        " group by dl.dataset_level_id";

    /** Group by portion of the clause */
    private static final String GROUP_BY_SKILL =
        " group by sr.skill_id";

    /** Group by portion of the clause */
    private static final String GROUP_BY_STEP =
        " group by sr.step_id";

    /** Group by portion of the clause */
    private static final String GROUP_BY_STUDENT =
        " group by sr.student_id";


    /** Index counter, allows the indexes to be put in order and then just indexed. */
    private static int indexCounter = 0;

    /** Index of the performance profiler results */
    private static final int TYPE_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int TYPE_NAME_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int TYPE_PARENT_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int NUM_INCORRECT_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int NUM_HINTS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int ASSISTANCE_SCORE_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int ERROR_RATE_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int ERROR_RATE_HINTS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int ERROR_RATE_INCORRECTS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int PREDICTED_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int RESIDUAL_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int STEP_DURATION_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int CORRECT_STEP_DURATION_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int ERROR_STEP_DURATION_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int NUM_STUDENTS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int NUM_STEPS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int NUM_SKILLS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int NUM_PROBLEMS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int OBSERVATIONS_INDEX = indexCounter++;
    /** Index of the performance profiler results */
    private static final int SECONDARY_PREDICTED_INDEX = indexCounter++;

    /**
     * Get an ErrorRate performance profiler info.
     * @param options the ProfilerOptions.
     * @return a List with the index the key of the Y axis ID, and the value a PerformanceProfile.
     */
    public List getPerformanceProfiler(ProfilerOptions options) {
        //If these are empty then we will have no results. (Skill can be empty)
        if (options.getStudentList().size() == 0 || options.getProblemList().size() == 0) {
            return new ArrayList();
        }

        StringBuffer queryString = buildQuery(options);
        if (queryString == null) { return new ArrayList(); }

        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(queryString.toString());

        SkillModelItem secondaryModel = options.getSecondarySkillModel();
        sqlQuery.setInteger("sampleId", ((Integer)options.getSampleItem().getId()).intValue());
        sqlQuery.setLong("primarySkillModelId",
                ((Long)options.getPrimarySkillModel().getId()).longValue());
        if (secondaryModel != null && secondaryModel.getId() != null) {
            sqlQuery.setLong("secondaryModelId", ((Long)secondaryModel.getId()).longValue());
        }

        //indicate which columns map to which types.
        sqlQuery.addScalar("typeId", Hibernate.STRING);
        sqlQuery.addScalar("typeName", Hibernate.STRING);
        sqlQuery.addScalar("typeParent", Hibernate.LONG);
        sqlQuery.addScalar("numIncorrects", Hibernate.DOUBLE);
        sqlQuery.addScalar("numHints", Hibernate.DOUBLE);
        sqlQuery.addScalar("assistanceScore", Hibernate.DOUBLE);
        sqlQuery.addScalar("errorRate", Hibernate.DOUBLE);
        sqlQuery.addScalar("errorRateHints", Hibernate.DOUBLE);
        sqlQuery.addScalar("errorRateIncorrects", Hibernate.DOUBLE);
        sqlQuery.addScalar("predicted", Hibernate.DOUBLE);
        sqlQuery.addScalar("residual", Hibernate.DOUBLE);
        sqlQuery.addScalar("stepDuration", Hibernate.DOUBLE);
        sqlQuery.addScalar("correctStepDuration", Hibernate.DOUBLE);
        sqlQuery.addScalar("errorStepDuration", Hibernate.DOUBLE);
        sqlQuery.addScalar("numStudents", Hibernate.INTEGER);
        sqlQuery.addScalar("numSteps", Hibernate.INTEGER);
        sqlQuery.addScalar("numSkills", Hibernate.INTEGER);
        sqlQuery.addScalar("numProblems", Hibernate.INTEGER);
        sqlQuery.addScalar("observations", Hibernate.INTEGER);

        if (secondaryModel != null && secondaryModel.getId() != null) {
            sqlQuery.addScalar("secondaryPredicted", Hibernate.DOUBLE);
        }

        LogUtils.logTrace(logger, sqlQuery.toString());

        List results = sqlQuery.list();
        releaseSession(session);

        List pointsList = new ArrayList();
        for (Iterator it = results.listIterator(); it.hasNext();) {
            Object[] resultRow = (Object[])it.next();

            PerformanceProfilerBar ppBar = new PerformanceProfilerBar();
            ppBar.setTypeId((String)resultRow[TYPE_INDEX]);
            ppBar.setTypeName((String)resultRow[TYPE_NAME_INDEX]);
            ppBar.setTypeParentId((Long)resultRow[TYPE_PARENT_INDEX]);
            ppBar.setAverageNumberHints((Double)resultRow[NUM_HINTS_INDEX]);
            ppBar.setAverageNumberIncorrects((Double)resultRow[NUM_INCORRECT_INDEX]);
            ppBar.setAssistanceScore((Double)resultRow[ASSISTANCE_SCORE_INDEX]);
            ppBar.setErrorRate((Double)resultRow[ERROR_RATE_INDEX]);
            ppBar.setErrorRateHints((Double)resultRow[ERROR_RATE_HINTS_INDEX]);
            ppBar.setErrorRateIncorrects((Double)resultRow[ERROR_RATE_INCORRECTS_INDEX]);
            ppBar.setNumberStudents((Integer)resultRow[NUM_STUDENTS_INDEX]);
            ppBar.setNumberSteps((Integer)resultRow[NUM_STEPS_INDEX]);
            ppBar.setNumberSkills((Integer)resultRow[NUM_SKILLS_INDEX]);
            ppBar.setNumberProblems((Integer)resultRow[NUM_PROBLEMS_INDEX]);
            ppBar.setObservation((Integer)resultRow[OBSERVATIONS_INDEX]);
            ppBar.setPrimaryPredicted((Double)resultRow[PREDICTED_INDEX]);
            ppBar.setResidual((Double)resultRow[RESIDUAL_INDEX]);
            ppBar.setStepDuration((Double)resultRow[STEP_DURATION_INDEX]);
            ppBar.setCorrectStepDuration((Double)resultRow[CORRECT_STEP_DURATION_INDEX]);
            ppBar.setErrorStepDuration((Double)resultRow[ERROR_STEP_DURATION_INDEX]);
            if (secondaryModel != null && secondaryModel.getId() != null) {
                ppBar.setSecondaryPredicted(
                        (Double)resultRow[SECONDARY_PREDICTED_INDEX]);
            }
            pointsList.add(ppBar);
        }
        return pointsList;
    }

    /**
     * Creates the proper SQL string given a the parameter options.
     * @param sortOption the type of sort requested by the ProfilerOptions.
     * @param aggregateType the aggregation type for this profiler.
     * @param isAscending whether to sort by ascending or descending.
     * @return a String of SQL that is properly formated order by.
     */
    private String getOrderBy(String sortOption, String aggregateType, boolean isAscending) {
        String ascendingString;
        if (isAscending) {
            ascendingString = " ASC";
        } else {
            ascendingString = " DESC";
        }

        String orderByString;
        if (ProfilerOptions.SORT_BY_NAME.equals(sortOption)) {
            if (ProfilerOptions.TYPE_PROBLEM.equals(aggregateType)) {
                orderByString = ORDER_BY_PROBLEM_NAME;
            } else if (ProfilerOptions.TYPE_SKILL.equals(aggregateType)) {
                orderByString = ORDER_BY_SKILL_NAME;
            } else if (ProfilerOptions.TYPE_STUDENT.equals(aggregateType)) {
                orderByString = ORDER_BY_STUDENT_NAME;
            } else if (ProfilerOptions.TYPE_STEP.equals(aggregateType)) {
                orderByString = ORDER_BY_STEP_NAME;
            } else {
                //assume at this point the type is a custom problem
                orderByString = ORDER_BY_LEVEL_NAME;
            }
        } else if (ProfilerOptions.SORT_BY_ASSISTANCE_SCORE.equals(sortOption)) {
            orderByString = ORDER_BY_ASSISTANCE_SCORE;
        } else if (ProfilerOptions.SORT_BY_ERROR_RATE.equals(sortOption)) {
            orderByString = ORDER_BY_ERROR_RATE;
        } else if (ProfilerOptions.SORT_BY_RESIDUAL.equals(sortOption)) {
            orderByString = ORDER_BY_RESIDUAL;
        } else if (ProfilerOptions.SORT_BY_TOTAL_INCORRECTS.equals(sortOption)) {
            orderByString = ORDER_BY_NUM_INCORRECTS;
        } else if (ProfilerOptions.SORT_BY_TOTAL_HINTS.equals(sortOption)) {
            orderByString = ORDER_BY_NUM_HINTS;
        } else if (ProfilerOptions.SORT_BY_FIRST_ATTEMPT_HINTS.equals(sortOption)) {
            orderByString = ORDER_BY_FA_HINTS;
        } else if (ProfilerOptions.SORT_BY_FIRST_ATTEMPT_INCORRECTS.equals(sortOption)) {
            orderByString = ORDER_BY_FA_INCORRECTS;
        } else if (ProfilerOptions.SORT_BY_PREDICTED_ERROR_RATE.equals(sortOption)) {
            orderByString = ORDER_BY_PREDICTED;
        } else if (ProfilerOptions.SORT_BY_NUMBER_OF_PROBLEMS.equals(sortOption)) {
            orderByString = ORDER_BY_NUMBER_OF_PROBLEMS;
        } else if (ProfilerOptions.SORT_BY_NUMBER_OF_SKILLS.equals(sortOption)) {
            orderByString = ORDER_BY_NUMBER_OF_SKILLS;
        } else if (ProfilerOptions.SORT_BY_NUMBER_OF_STEPS.equals(sortOption)) {
            orderByString = ORDER_BY_NUMBER_OF_STEPS;
        } else if (ProfilerOptions.SORT_BY_NUMBER_OF_STUDENTS.equals(sortOption)) {
            orderByString = ORDER_BY_NUMBER_OF_STUDENTS;
        } else if (ProfilerOptions.SORT_BY_STEP_DURATION.equals(sortOption)) {
            orderByString = ORDER_BY_STEP_DURATION;
        } else if (ProfilerOptions.SORT_BY_CORRECT_STEP_DURATION.equals(sortOption)) {
            orderByString = ORDER_BY_CORRECT_STEP_DURATION;
        } else if (ProfilerOptions.SORT_BY_ERROR_STEP_DURATION.equals(sortOption)) {
            orderByString = ORDER_BY_ERROR_STEP_DURATION;
        } else {
            throw new IllegalArgumentException(sortOption + " is not a valid sort option");
        }

        orderByString += ascendingString;

        //If it's not a sort by name, add a secondary sort by name in order to make
        //sure that we get a consistent order for sorts with the same value.
        if (!ProfilerOptions.SORT_BY_NAME.equals(sortOption)) {
            orderByString += ", ";
            if (ProfilerOptions.TYPE_PROBLEM.equals(aggregateType)) {
                orderByString += ORDER_BY_PROBLEM_NAME;
            } else if (ProfilerOptions.TYPE_SKILL.equals(aggregateType)) {
                orderByString += ORDER_BY_SKILL_NAME;
            } else if (ProfilerOptions.TYPE_STUDENT.equals(aggregateType)) {
                orderByString += ORDER_BY_STUDENT_NAME;
            } else if (ProfilerOptions.TYPE_STEP.equals(aggregateType)) {
                orderByString += ORDER_BY_STEP_NAME;
            } else {
                //assume at this point the type is a custom problem
                orderByString += ORDER_BY_LEVEL_NAME;
            }
        }

        LogUtils.logDebug(logger, "Sort By chosen: " + orderByString);
        return " order by " + orderByString;
    }

    /**
     * Builds the query string given the set of options.
     * @param options the set of options for generating a performance profiler graph
     * @return StringBuffer of the query string. returns null if the options to not allow
     * for any results.
     */
    private StringBuffer buildQuery(ProfilerOptions options) {
        StringBuffer queryString = new StringBuffer();
        String type = options.getAggregateType();
        SkillModelItem secondaryModel = options.getSecondarySkillModel();
        boolean hasSecondary = false;
        if (secondaryModel != null) { hasSecondary = true; }

        if (type.compareTo(ProfilerOptions.TYPE_PROBLEM) == 0) {
            queryString.append(SELECT_BY_PROBLEM);
            if (hasSecondary) {
                queryString.append(SELECTS_WITH_SECONDARY);
                queryString.append(FROM_BY_PROBLEM_WITH_SECONDARY);
            } else {
                queryString.append(SELECTS);
                queryString.append(FROM_BY_PROBLEM);
            }
            queryString.append(addStudentSelections(options.getStudentList()));
            queryString.append(addSkillSelections(options.getSkillList(),
                    options.displayUnmapped(), hasSecondary));
            queryString.append(addProblemSelections(options.getProblemList()));
            queryString.append(GROUP_BY_PROBLEM);
        } else if (type.compareTo(ProfilerOptions.TYPE_SKILL) == 0) {

            queryString.append(SELECT_BY_SKILL);
            if (hasSecondary) {
                queryString.append(SELECTS_WITH_SECONDARY);
                queryString.append(FROM_BY_SKILL_WITH_SECONDARY);
            } else {
                queryString.append(SELECTS);
                queryString.append(FROM_BY_SKILL);
            }
            queryString.append(addStudentSelections(options.getStudentList()));
            queryString.append(addSkillSelections(options.getSkillList(),
                    options.displayUnmapped(), hasSecondary));
            queryString.append(addProblemSelections(options.getProblemList()));
            queryString.append(GROUP_BY_SKILL);
        } else if (type.compareTo(ProfilerOptions.TYPE_STEP) == 0) {
            queryString.append(SELECT_BY_STEP);
            if (hasSecondary) {
                queryString.append(SELECTS_WITH_SECONDARY);
                queryString.append(FROM_BY_STEP_WITH_SECONDARY);
            } else {
                queryString.append(SELECTS);
                queryString.append(FROM_BY_STEP);
            }
            queryString.append(addStudentSelections(options.getStudentList()));
            queryString.append(addSkillSelections(options.getSkillList(),
                    options.displayUnmapped(), hasSecondary));
            queryString.append(addProblemSelections(options.getProblemList()));
            queryString.append(GROUP_BY_STEP);
        } else if (type.compareTo(ProfilerOptions.TYPE_STUDENT) == 0) {
            queryString.append(SELECT_BY_STUDENT);
            if (hasSecondary) {
                queryString.append(SELECTS_WITH_SECONDARY);
                queryString.append(FROM_BY_STUDENT_WITH_SECONDARY);
            } else {
                queryString.append(SELECTS);
                queryString.append(FROM_BY_STUDENT);
            }
            queryString.append(addStudentSelections(options.getStudentList()));
            queryString.append(addSkillSelections(options.getSkillList(),
                    options.displayUnmapped(), hasSecondary));
            queryString.append(addProblemSelections(options.getProblemList()));
            queryString.append(GROUP_BY_STUDENT);
        } else {
            //we assume that the type is a custom dataset level.
            queryString.append(SELECT_BY_LEVEL);
            if (hasSecondary) {
                queryString.append(SELECTS_WITH_SECONDARY);
                queryString.append(FROM_BY_LEVEL_WITH_SECONDARY);
            } else {
                queryString.append(SELECTS);
                queryString.append(FROM_BY_LEVEL);
            }

            List levels = DaoFactory.DEFAULT.getDatasetLevelDao().findMatchingByTitle(
                    type, options.getSampleItem().getDataset(), false);
            if (levels.isEmpty()) { return null; }
            queryString.append(addLevelRestrictions(levels));
            queryString.append(addStudentSelections(options.getStudentList()));
            queryString.append(addSkillSelections(options.getSkillList(),
                    options.displayUnmapped(), hasSecondary));
            queryString.append(addProblemSelections(options.getProblemList()));
            queryString.append(GROUP_BY_LEVEL);
        }

        if (options.getMinSteps() != null || options.getMinStudents() != null
                || options.getMinProblems() != null || options.getMinSkills() != null) {
            Integer minStuds = options.getMinStudents();
            Integer minSteps = options.getMinSteps();
            Integer minProbs = options.getMinProblems();
            Integer minSkils = options.getMinSkills();

            queryString.append(" having");
            //add the having for for min number of steps
            queryString.append((minSteps != null) ? " numSteps >= " + minSteps : "");

            //if we added a having for minSteps and we are adding minStuds, include an "and"
            queryString.append((minSteps != null && minStuds != null) ? " and" : "");
            queryString.append((minStuds != null) ? " numStudents >= " + minStuds : "");

            //if we added minSteps or minStuds and are adding minProblems, include an "and";
            queryString.append(((minSteps != null || minStuds != null) && minProbs != null)
                    ? " and" : "");
            queryString.append((minProbs != null) ? " numProblems >= " + minProbs : "");

            //if we added minSteps, minStuds or minSteps and are adding minSkills,
            //include an "and";
            queryString.append(
                ((minSteps != null || minStuds != null || minProbs != null) && minSkils != null)
                    ? " and" : "");
            queryString.append((minSkils != null) ? " numSkills >= " + minSkils : "");
        }

        queryString.append(getOrderBy(options.getSortBy(), options.getAggregateType(),
                options.getSortByAscending()));

        return queryString;
    }

    /**
     * Builds the student list portion of the query.
     * @param studentList a Collection of StudentItems.
     * @return string buffer that is the native SQL.
     */
    private StringBuffer addStudentSelections(Collection studentList) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(" and sr.student_id in (");
        for (Iterator it = studentList.iterator(); it.hasNext();) {
            queryString.append(((StudentItem)it.next()).getId());
            if (it.hasNext()) { queryString.append(", "); }
        }
        queryString.append(")");
        return queryString;
    }

    /**
     * Builds the skill list portion of the query.
     * @param skillList a Collection of SkillItems.
     * @param displayUnmappedFlag a boolean indicating whether or not to display unmapped steps.
     * @param hasSecondaryModel a boolean indicating whether or not a secondary model is selected.
     * @return string buffer that is the native SQL.
     */
    private StringBuffer addSkillSelections(Collection skillList, boolean displayUnmappedFlag,
            boolean hasSecondaryModel) {
        StringBuffer queryString = new StringBuffer();

        if (hasSecondaryModel && displayUnmappedFlag) {
            queryString.append("and (sr2.skill_model_id = :secondaryModelId"
                    + " or sr.skill_id = sr2.skill_id or sr.skill_id IS NULL) ");
        } else if (hasSecondaryModel) {
            queryString.append("and (sr2.skill_model_id = :secondaryModelId"
                    + " or sr.skill_id = sr2.skill_id) ");
        }

        if (skillList.size() < 1 && displayUnmappedFlag) {
                queryString.append(" and sr.skill_id IS NULL");
        } else if (skillList.size() < 1) {
            //this is done because we do not want any unmapped skills
            //and no skills were selected.  So don't return anything with this graph
            queryString.append(" and sr.skill_id = -1");
        } else {
            if (displayUnmappedFlag) {
                queryString.append(" and ( sr.skill_id in (");
            } else {
                queryString.append(" and sr.skill_id in (");
            }
            for (Iterator it = skillList.iterator(); it.hasNext();) {
                queryString.append(((SkillItem)it.next()).getId());
                if (it.hasNext()) { queryString.append(", "); }
            }
            queryString.append(")");

            if (displayUnmappedFlag) {
                queryString.append(" or sr.skill_id IS NULL)");
            }
        }
        return queryString;
    }

    /**
     * Builds the problem list portion of the query.
     * @param problemList a Collection of ProblemItems.
     * @return string buffer that is the native SQL.
     */
    private StringBuffer addProblemSelections(Collection problemList) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(" and sr.problem_id in (");
        for (Iterator it = problemList.iterator(); it.hasNext();) {
            queryString.append(((ProblemItem)it.next()).getId());
            if (it.hasNext()) { queryString.append(", "); }
        }
        queryString.append(")");
        return queryString;
    }

    /**
     * Builds the dataset level portion of the query.
     * @param levels List of DatasetLevels to aggregate on.
     * @return string buffer this is the native SQL
     */
    private StringBuffer addLevelRestrictions(List levels) {
        StringBuffer queryString = new StringBuffer();
        queryString.append(" and dl.dataset_level_id in (");
        for (Iterator it = levels.iterator(); it.hasNext();) {
            queryString.append(((DatasetLevelItem)it.next()).getId());
            if (it.hasNext()) { queryString.append(", "); }
        }
        queryString.append(")");
        return queryString;
    }
}