/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.web.util.HtmlUtils;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.ErrorReportDao;
import edu.cmu.pslc.datashop.dto.ErrorReportByProblem;
import edu.cmu.pslc.datashop.dto.ErrorReportBySkill;
import edu.cmu.pslc.datashop.dto.ErrorReportBySkillList;
import edu.cmu.pslc.datashop.dto.ErrorReportStep;
import edu.cmu.pslc.datashop.dto.ErrorReportStepAttempt;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FeedbackItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * Hibernate/Spring implementation of the ErrorReportDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15103 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-05-03 11:51:38 -0400 (Thu, 03 May 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportDaoHibernate extends AbstractSampleDaoHibernate implements ErrorReportDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Flag to turn on logging for the timing for this class */
    private static final boolean LOG_TIME_FLAG = false;

    /**
     * Return an Error Report given a problem item and a skill model.
     * @param sampleList the currently selected samples
     * @param problemItem the problem item
     * @param skillModelId the id of the selected skill model
     * @return an object that holds all we need to display an error report
     */
    public ErrorReportByProblem getErrorReportByProblem(
            List sampleList, ProblemItem problemItem, Comparable skillModelId) {

        Date timer = new Date();

        Session session = getSession();

        if (sampleList.size() <= 0) {
            logger.warn("Cannot create an error report without any samples selected.");
            return null;
        }

        //re-attach the problem
        if (problemItem.getId() != null) {
            problemItem = (ProblemItem)session.get(ProblemItem.class, (Long)problemItem.getId());
        } else {
            logger.warn("Cannot create an error report for an invalid problem: " + problemItem);
            return null;
        }

        timer = logTime("initialized in ", timer);

        MultiKeyMap stepListMap = getStepList(
                sampleList, problemItem, skillModelId);

        timer = logTime("after first query : get list of steps", timer);

        MultiKeyMap stepDetailsMap = getStepDetails(
                sampleList, problemItem, skillModelId);

        timer = logTime("after second query : get step details", timer);

        MultiKeyMap feedbackMap = getFeedbackItemsForAttempts(
                sampleList, problemItem, skillModelId);

        timer = logTime("after third query : get feedback items", timer);

        for (Iterator stepIter = stepListMap.values().iterator(); stepIter.hasNext();) {
            ErrorReportStep erStep = (ErrorReportStep)stepIter.next();
            List detailList = (List)stepDetailsMap.get(erStep.getSampleId(), erStep.getStepId());
            if (logger.isDebugEnabled()) {
                logger.debug("Sample " + erStep.getSampleId()
                        + " Step " + erStep.getStepId()
                        + " List " + detailList);
            }

            if (detailList != null) {
                for (Iterator detailIter = detailList.iterator(); detailIter.hasNext();) {
                    ErrorReportStepAttempt erAttempt = (ErrorReportStepAttempt)detailIter.next();
                    List feedbackList = (List)feedbackMap.get(erStep.getSampleId(),
                            erStep.getStepId(), erAttempt.getAttemptId());
                    if (feedbackList != null) {
                        for (Iterator feedIter = feedbackList.iterator(); feedIter.hasNext();) {
                            FeedbackItem feedbackItem = (FeedbackItem)feedIter.next();
                            erAttempt.addFeedback(feedbackItem);
                        }
                    }
                    erAttempt.setPercentage(erStep.getNumObservations());
                }

                erStep.setAttemptList(detailList);
            }
        }

        ErrorReportByProblem errorReport = new ErrorReportByProblem();
        errorReport.setProblemId(problemItem.getId().toString());
        errorReport.setProblemName(problemItem.getProblemName());
        errorReport.setProblemDesc(problemItem.getProblemDescription());
        for (Iterator iter = stepListMap.values().iterator(); iter.hasNext();) {
            errorReport.addStep((ErrorReportStep)iter.next());
        }

        timer = logTime("created error report by problem in : ", timer);

        releaseSession(session);
        return errorReport;
    }

    /**
     * Return an Error Report given a skill model and list of selected skills.
     * @param sampleList the currently selected samples
     * @param skillModelId the id of the selected skill model
     * @param skillList the list of selected skills
     * @return an object that holds all we need to display an error report
     */
    public ErrorReportBySkillList getErrorReportBySkillList(
            List sampleList, Comparable skillModelId, List skillList) {

        Date timer = new Date();

        Session session = getSession();

        if (sampleList.size() <= 0) {
            logger.info("Cannot create an error report without any samples selected.");
            return null;
        }

        List skillRollUpList = getSkillDetails(sampleList, skillModelId, skillList);

        ErrorReportBySkillList errorReportBySkillList = new ErrorReportBySkillList();
        errorReportBySkillList.setSkillList(skillRollUpList);

        timer = logTime("created error report by skill in : ", timer);

        releaseSession(session);
        return errorReportBySkillList;
    }

    /**
     * Log timer level debugging messages.
     * @param msg the debug message
     * @param startTime the previous time stamp
     * @return the new start time
     */
    public Date logTime(String msg, Date startTime) {
        if (LOG_TIME_FLAG) {
            if (logger.isDebugEnabled()) {
                Date now = new Date();
                long time = now.getTime() - startTime.getTime();
                startTime = now;
                logger.debug("TIME: " + msg + time);
            }
        }
        return startTime;
    }

    //
    // Queries for the Error Report by Problem
    //

    /** Index into the result for the STEP_LIST_QUERY. */
    private static final int SIDX_SAMPLE_ID = 0;
    /** Index into the result for the STEP_LIST_QUERY. */
    private static final int SIDX_SAMPLE_NAME = 1;
    /** Index into the result for the STEP_LIST_QUERY. */
    private static final int SIDX_STEP_ID = 2;
    /** Index into the result for the STEP_LIST_QUERY. */
    private static final int SIDX_STEP_NAME = 3;
    /** Index into the result for the STEP_LIST_QUERY. */
    private static final int SIDX_STUDENTS = 4;
    /** Index into the result for the STEP_LIST_QUERY. */
    private static final int SIDX_OBSERVATIONS = 5;
    /** Index into the result for the STEP_LIST_QUERY. */
    private static final int SIDX_KC_LIST = 6;

    /**
     * Native SQL Query to get the list of steps and step-level data,
     * rolled up by sample and step.
     */
    private static final String STEP_LIST_QUERY =
        "select sr.sample_id as sampleId, sample_name as sampleName, "
        + " step_id as stepId, subgoal_name as stepName, "
        + " count(distinct student_id) as uniqueStudents, "
        + " count(distinct step_rollup_id) as observations, "
        + " (select group_concat(distinct skill_name order by skill_name) "
        + "  from step_rollup sr2 "
        + "  join skill sk on sk.skill_id = sr2.skill_id "
        + "  where sr2.step_id = sr.step_id "
        + "  and sr2.sample_id = sr.sample_id "
        + "  and sr2.skill_model_id = sr.skill_model_id "
        + "  group by sr.skill_model_id "
        + " ) as kcList "
        + " from step_rollup sr "
        + " join transaction_sample_map tsm on tsm.sample_id = sr.sample_id "
        + " join tutor_transaction tt on (tt.subgoal_id = sr.step_id "
        + " and tt.transaction_id = tsm.transaction_id "
        + " and tt.transaction_time = sr.first_transaction_time) "
        + " join sample s on s.sample_id = sr.sample_id "
        + " join subgoal sg on sg.subgoal_id = sr.step_id "
        + " where sr.problem_id = :problemId "
        + " SKILL_MODEL_WHERE "
        + " and sampleIdParam "
        + " and tt.attempt_at_subgoal = 1 "
        + " group by sr.sample_id, step_id";

    /** Constant for part of 3 SQL query strings. */
    private static final String SKILL_MODEL_WHERE_SQL = " and sr.skill_model_id = :skillModelId ";
    /** Constant for a temporary part of 3 SQL strings that needs to be replaced. */
    private static final String SKILL_MODEL_WHERE_STR = "SKILL_MODEL_WHERE";
    /**
     * Returns a multi-key map, where the keys are sample Id and step Id
     * and the values are ErrorReportStep objects.
     * @param sampleList a list of sample items
     * @param problemItem the given problem item
     * @param skillModelId the given KCM id
     * @return a multi-key map with ErrorReportStep objects
     */
    private MultiKeyMap getStepList(
            List sampleList, ProblemItem problemItem, Comparable skillModelId) {

        MultiKeyMap multiKeyMap = new MultiKeyMap();

        //
        // Query the database for the unique number of students, number of observations
        // and the list of KCs for each sample/step pair.
        //
        Session session = getSession();
        String query = STEP_LIST_QUERY;
        query = query.replace("sampleIdParam",
                "sr.sample_id " + inItemIds(sampleList));
        if (skillModelId != null) {
            query = query.replace(SKILL_MODEL_WHERE_STR, SKILL_MODEL_WHERE_SQL);
        } else {
            query = query.replace(SKILL_MODEL_WHERE_STR, "");
        }
        SQLQuery sqlQuery = session.createSQLQuery(query);

        //indicate which columns map to which types.
        sqlQuery.addScalar("sampleId", Hibernate.INTEGER);
        sqlQuery.addScalar("sampleName", Hibernate.STRING);
        sqlQuery.addScalar("stepId", Hibernate.LONG);
        sqlQuery.addScalar("stepName", Hibernate.STRING);
        sqlQuery.addScalar("uniqueStudents", Hibernate.INTEGER);
        sqlQuery.addScalar("observations", Hibernate.DOUBLE);
        sqlQuery.addScalar("kcList", Hibernate.STRING);

        //set the parameter(s)
        sqlQuery.setLong("problemId", ((Long) problemItem.getId()));
        if (skillModelId != null) {
            sqlQuery.setLong("skillModelId", ((Long) skillModelId));
        }

        List <Object[]> results = sqlQuery.list();
        releaseSession(session);

        //
        // Fill in the multi-key map with the results of the query.
        //
        for (Object[] row : results) {
            ErrorReportStep errorReportStep = new ErrorReportStep();
            Integer sampleId = (Integer)row[SIDX_SAMPLE_ID];
            String sampleName = (String)row[SIDX_SAMPLE_NAME];
            Long stepId = (Long)row[SIDX_STEP_ID];
            String stepName = (String)row[SIDX_STEP_NAME];
            Integer numStudents = (Integer)row[SIDX_STUDENTS];
            Double observations = (Double)row[SIDX_OBSERVATIONS];
            String kcList = (String)row[SIDX_KC_LIST];

            errorReportStep.setSampleId(sampleId);
            errorReportStep.setSampleName(sampleName);
            errorReportStep.setStepId(stepId);
            errorReportStep.setStepName(stepName);
            errorReportStep.setDisplaySAI(stepName);
            errorReportStep.setHoverSAI(stepName + " (" + stepId + ")");
            errorReportStep.setNumStudents(numStudents);
            if (observations == null) {
                errorReportStep.setNumObservations(0);
            } else {
                errorReportStep.setNumObservations(observations.intValue());
            }
            errorReportStep.setKcList(kcList);

            multiKeyMap.put(sampleId, stepId, errorReportStep);
        }

        return multiKeyMap;
    }

    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_SAMPLE_ID = 0;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_STEP_ID = 1;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_ATTEMPT_ID = 2;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_CORRECT_FLAG = 3;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_ATT_NUM_STUDS = 4;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_ATT_NUM_OBS = 5;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_SELECTION_LIST = 6;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_ACTION_LIST = 7;
    /** Index into the result for the STEP_DETAILS_QUERY. */
    private static final int DIDX_INPUT_LIST = 8;

    /**
     * Native SQL Query to get the details of the step for the error report.
     */
    private static final String STEP_DETAILS_QUERY =
        " select sr.sample_id as sampleId, "
        + "  sr.step_id as stepId, "
        + "  sga.subgoal_attempt_id as attemptId, "
        + "  sga.correct_flag as correctFlag, "
        + "  count(distinct sr.student_id) as attemptNumStudents, "
        + "  count(distinct step_rollup_id) as attemptNumObservations, "
        + "  (select group_concat(selection order by attSel.attempt_selection_id separator ' ') "
        + "       from attempt_selection attSel "
        + "       where attSel.subgoal_attempt_id = sga.subgoal_attempt_id "
        + "       group by sga.subgoal_attempt_id "
        + "  ) as selectionList, "
        + "  (select group_concat(action order by attAct.attempt_action_id separator ' ') "
        + "       from attempt_action attAct "
        + "       where attAct.subgoal_attempt_id = sga.subgoal_attempt_id "
        + "       group by sga.subgoal_attempt_id "
        + "  ) as actionList, "
        + "  (select group_concat(input order by attInp.attempt_input_id separator ' ') "
        + "       from attempt_input attInp "
        + "       where attInp.subgoal_attempt_id = sga.subgoal_attempt_id "
        + "       group by sga.subgoal_attempt_id "
        + "  ) as inputList "
        + " from step_rollup sr "
        + " join subgoal sg on sg.subgoal_id = sr.step_id "
        + " join subgoal_attempt sga on sga.subgoal_id = sg.subgoal_id "
        + " join transaction_sample_map tsm on tsm.sample_id = sr.sample_id "
        + " join tutor_transaction tt on tt.subgoal_attempt_id = sga.subgoal_attempt_id "
        + " and tt.transaction_id = tsm.transaction_id "
        + " and tt.transaction_time = sr.first_transaction_time "
        + " join session sess "
        + "      on (sess.session_id = tt.session_id and sr.student_id = sess.student_id) "
        + " where sr.problem_id = :problemId "
        + " SKILL_MODEL_WHERE "
        + " and sr.sampleIdParam "
        + " and tt.attempt_at_subgoal = 1 "
        + " group by sr.sample_id, sga.subgoal_attempt_id "
        + " order by sr.sample_id, sr.step_id, sga.subgoal_attempt_id";

    /**
     * Returns a multi-key map, where the keys are sample Id and step Id
     * and the values are ErrorReportStep objects.
     * @param sampleList a list of sample items
     * @param problemItem the given problem item
     * @param skillModelId the given KCM id
     * @return a multi-key map with ErrorReportStep objects
     */
    private MultiKeyMap getStepDetails(List sampleList,
                                       final ProblemItem problemItem,
                                       final Comparable skillModelId) {

        MultiKeyMap multiKeyMap = new MultiKeyMap();

        //
        // Query the database for the step attempt details.
        //
        String query = STEP_DETAILS_QUERY;
        query = query.replace("sampleIdParam",
                "sample_id " + AbstractDaoHibernate.inItemIds(sampleList));
        if (skillModelId != null) {
            query = query.replace(SKILL_MODEL_WHERE_STR, SKILL_MODEL_WHERE_SQL);
        } else {
            query = query.replace(SKILL_MODEL_WHERE_STR, "");
        }
        final String queryStr = query;
        List<Object[]> dbResults = executeSQLQueryMaxConcat(queryStr, new PrepareQuery() {
                @Override
                public void prepareQuery(final SQLQuery query) {
                    query.addScalar("sampleId", Hibernate.INTEGER);
                    query.addScalar("stepId", Hibernate.LONG);
                    query.addScalar("attemptId", Hibernate.LONG);
                    query.addScalar("correctFlag", Hibernate.STRING);
                    query.addScalar("attemptNumStudents", Hibernate.INTEGER);
                    query.addScalar("attemptNumObservations", Hibernate.DOUBLE);
                    query.addScalar("selectionList", Hibernate.STRING);
                    query.addScalar("actionList", Hibernate.STRING);
                    query.addScalar("inputList", Hibernate.STRING);
                    query.setLong("problemId", ((Long) problemItem.getId()));
                    if (skillModelId != null) {
                        query.setLong("skillModelId", ((Long) skillModelId));
                    }
                }
            });

        //
        // Fill in the multi-key map with the results of the query.
        //
        for (Object[] row : dbResults) {
            Integer sampleId = (Integer)row[DIDX_SAMPLE_ID];
            Long stepId = (Long)row[DIDX_STEP_ID];
            Long attemptId = (Long)row[DIDX_ATTEMPT_ID];
            String correctFlag = (String)row[DIDX_CORRECT_FLAG];
            Integer numStudents = (Integer)row[DIDX_ATT_NUM_STUDS];
            Double numObservations = (Double)row[DIDX_ATT_NUM_OBS];
            String selectionList = (String)row[DIDX_SELECTION_LIST];
            String actionList = (String)row[DIDX_ACTION_LIST];
            String inputList = (String)row[DIDX_INPUT_LIST];

            ErrorReportStepAttempt errorReportStepAttempt = new ErrorReportStepAttempt();

            errorReportStepAttempt.setAttemptId(attemptId);
            errorReportStepAttempt.setCorrectFlag(correctFlag);
            errorReportStepAttempt.setNumStudents(numStudents);
            if (numObservations == null) {
                errorReportStepAttempt.setNumObservations(0);
            } else {
                errorReportStepAttempt.setNumObservations(numObservations.intValue());
            }
            if (inputList != null) {
                // delimit html entities
                String delimHtml = new String(HtmlUtils.htmlEscape(inputList));
                // restore line breaks
                String restoreBreaks = new String(delimHtml.replaceAll("&lt;br/&gt;", "<br/>"));
                inputList = new String(restoreBreaks);
                errorReportStepAttempt.setDisplaySAI(inputList);
            }
            String hoverSAI = "";
            if (selectionList != null) { hoverSAI += selectionList; }
            if (actionList != null) { hoverSAI += " " + actionList; }
            if (inputList != null) { hoverSAI += " " + inputList; }
            hoverSAI = hoverSAI.trim();
            errorReportStepAttempt.setHoverSAI(hoverSAI);

            List list = null;
            if (multiKeyMap.containsKey(sampleId, stepId)) {
                list = (List)multiKeyMap.get(sampleId, stepId);
            } else {
                list = new ArrayList();
            }

            list.add(errorReportStepAttempt);
            multiKeyMap.put(sampleId, stepId, list);
        }

        return multiKeyMap;
    }

    /** Index into the result for the ATTEMPT_FEEDBACK_QUERY. */
    private static final int FIDX_SAMPLE_ID = 0;
    /** Index into the result for the ATTEMPT_FEEDBACK_QUERY. */
    private static final int FIDX_STEP_ID = 1;
    /** Index into the result for the ATTEMPT_FEEDBACK_QUERY. */
    private static final int FIDX_ATTEMPT_ID = 2;
    /** Index into the result for the ATTEMPT_FEEDBACK_QUERY. */
    private static final int FIDX_FEEDBACK = 3;
    /** Index into the result for the ATTEMPT_FEEDBACK_QUERY. */
    private static final int FIDX_CLASSIFICATION = 4;

    /**
     * Native SQL Query to get the details of the step for the error report.
     */
    private static final String ATTEMPT_FEEDBACK_QUERY =
        "select sr.sample_id as sampleId, "
        + "   sr.step_id as stepId, "
        + "   sga.subgoal_attempt_id as attemptId, "
        + "   f.feedback_text as feedbackText, "
        + "   f.classification as classification "
        + " from step_rollup sr "
        + " join subgoal_attempt sga on sga.subgoal_id = sr.step_id "
        + " join transaction_sample_map tsm on tsm.sample_id = sr.sample_id "
        + " join tutor_transaction tt on tt.subgoal_attempt_id = sga.subgoal_attempt_id "
        + " and tt.transaction_id = tsm.transaction_id "
        + " join session sess on "
        +        " (sess.session_id = tt.session_id and sr.student_id = sess.student_id) "
        + " right join feedback f on f.feedback_id = tt.feedback_id "
        + " where sr.problem_id = :problemId "
        + " SKILL_MODEL_WHERE "
        + " and sr.sampleIdParam "
        + " and tt.attempt_at_subgoal = 1 "
        + " group by sr.sample_id, sr.step_id, sga.subgoal_attempt_id, f.feedback_id";

    /**
     * Returns a multi-key map, where the keys are sample Id, step Id, attemptId
     * and the value is a list of feedback items.
     * @param sampleList a list of sample items
     * @param problemItem the given problem item
     * @param skillModelId the given KCM id
     * @return a multi-key map with FeedbackItem objects
     */
    private MultiKeyMap getFeedbackItemsForAttempts(
            List sampleList, ProblemItem problemItem, Comparable skillModelId) {

        MultiKeyMap multiKeyMap = new MultiKeyMap();

        //
        // Query the database for the feedback text and classification.
        //
        Session session = getSession();
        String query = ATTEMPT_FEEDBACK_QUERY;
        query = query.replace("sampleIdParam",
                "sample_id " + AbstractDaoHibernate.inItemIds(sampleList));
        if (skillModelId != null) {
            query = query.replace(SKILL_MODEL_WHERE_STR, SKILL_MODEL_WHERE_SQL);
        } else {
            query = query.replace(SKILL_MODEL_WHERE_STR, "");
        }
        SQLQuery sqlQuery = session.createSQLQuery(query);

        //indicate which columns map to which types.
        sqlQuery.addScalar("sampleId", Hibernate.INTEGER);
        sqlQuery.addScalar("stepId", Hibernate.LONG);
        sqlQuery.addScalar("attemptId", Hibernate.LONG);
        sqlQuery.addScalar("feedbackText", Hibernate.STRING);
        sqlQuery.addScalar("classification", Hibernate.STRING);

        //set the parameter(s)
        sqlQuery.setLong("problemId", ((Long) problemItem.getId()));
        if (skillModelId != null) {
            sqlQuery.setLong("skillModelId", ((Long) skillModelId));
        }

        List <Object[]> results = sqlQuery.list();
        releaseSession(session);

        //
        // Fill in the multi-key map with the results of the query.
        //
        for (Object[] row : results) {
            Integer sampleId = (Integer)row[FIDX_SAMPLE_ID];
            Long stepId = (Long)row[FIDX_STEP_ID];
            Long attemptId = (Long)row[FIDX_ATTEMPT_ID];
            String feedbackText = (String)row[FIDX_FEEDBACK];
            String classification = (String)row[FIDX_CLASSIFICATION];

            if (logger.isDebugEnabled()) {
                logger.debug("Getting feedback: " + attemptId + " " + feedbackText);
            }

            // delimit html entities
            String delimHtml = new String(HtmlUtils.htmlEscape(feedbackText));
            // restore line breaks
            String restoreBreaks = new String(delimHtml.replaceAll("&lt;br/&gt;", "<br/>"));
            feedbackText = new String(restoreBreaks);

            FeedbackItem feedbackItem = new FeedbackItem();
            feedbackItem.setFeedbackText(feedbackText);
            feedbackItem.setClassification(classification);

            List list = null;
            if (multiKeyMap.containsKey(sampleId, stepId, attemptId)) {
                list = (List)multiKeyMap.get(sampleId, stepId, attemptId);
            } else {
                list = new ArrayList();
            }

            list.add(feedbackItem);
            multiKeyMap.put(sampleId, stepId, attemptId, list);
        }

        return multiKeyMap;
    }

    /** Index into the result for the BY_SKILL_QUERY. */
    //private static final int BIDX_SAMPLE_ID = 0;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_SAMPLE_NAME = 1;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_SKILL_NAME = 2;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_NUM_INCORRECTS = 3;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_NUM_HINTS = 4;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_NUM_CORRECTS = 5;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_NUM_UNKNOWNS = 6;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_NUM_STUDENTS = 7;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_NUM_OBS = 8;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_PROBLEM_NAME_LIST = 9;
    /** Index into the result for the BY_SKILL_QUERY. */
    private static final int BIDX_PROBLEM_ID_LIST = 10;

    /**
     * Native SQL Query to get data for the Error Report by KC(Skill).
     * Note the cast in the group_concat:  The cast is needed as it is a well
     * known bug in MySQL that concat will mess up utf8 characters.  I happened
     * to test this with French characters to find out about the issue.
* http://stackoverflow.com/questions/6397156/why-concat-does-not-default-to-default-charset-in-mysql
     */
    private static final String BY_SKILL_QUERY =
        "select sr.sample_id as sampleId, "
        + " s.sample_name as sampleName, "
        + " sk.skill_name as skillName, "
        + " sum(if (sr.first_attempt = '0', 1, 0)) as numObsIncorrects, "
        + " sum(if (sr.first_attempt = '1', 1, 0)) as numObsHints, "
        + " sum(if (sr.first_attempt = '2', 1, 0)) as numObsCorrects, "
        + " sum(if (sr.first_attempt = '3', 1, 0)) as numObsUnknowns, "
        + " count(distinct sr.student_id) as numStudents, "
        + " count(*) numObservations, "

        + " group_concat(distinct p.problem_name order by p.problem_name separator "
        + "'" + ErrorReportBySkill.SEPARATOR + "') as problemNameList, "

        + " group_concat(distinct concat(p.problem_name, '~~', cast(p.problem_id as char))"
        + " order by p.problem_name, p.problem_id separator "
        + "'" + ErrorReportBySkill.SEPARATOR + "') as problemIdList "

        + " from step_rollup sr "
        + " join skill sk on sk.skill_id = sr.skill_id "
        + " join problem p on p.problem_id = sr.problem_id "
        + " join sample s on s.sample_id = sr.sample_id "
        + " where sr.skill_model_id = :skillModelId "
        + " and sr.sampleIdParam "
        + " and sr.skillIdParam "
        + " group by sr.sample_id, sr.skill_id "
        + " order by sampleName, skillName";

    /**
     * Returns a multi-key map, where the keys ...
     * and the value is a list of feedback items.
     * @param sampleList a list of sample items
     * @param skillModelId the given KCM id
     * @param skillList a list of skill items
     * @return a multi-key map with ...
     */
    private List getSkillDetails(List sampleList, final Comparable skillModelId, List skillList) {

        List erSkillList = new ArrayList();

        //
        // Query the database for the error report by skill data
        //
        Session session = getSession();
        String query = BY_SKILL_QUERY;
        query = query.replace("sampleIdParam",
                "sample_id " + AbstractDaoHibernate.inItemIds(sampleList));
        query = query.replace("skillIdParam",
                "skill_id " + AbstractDaoHibernate.inItemIds(skillList));
        SQLQuery sqlQuery = session.createSQLQuery(query);

        //indicate which columns map to which types.
        sqlQuery.addScalar("sampleId", Hibernate.INTEGER);
        sqlQuery.addScalar("sampleName", Hibernate.STRING);
        sqlQuery.addScalar("skillName", Hibernate.STRING);
        sqlQuery.addScalar("numObsIncorrects", Hibernate.INTEGER);
        sqlQuery.addScalar("numObsHints", Hibernate.INTEGER);
        sqlQuery.addScalar("numObsCorrects", Hibernate.INTEGER);
        sqlQuery.addScalar("numObsUnknowns", Hibernate.INTEGER);
        sqlQuery.addScalar("numStudents", Hibernate.INTEGER);
        sqlQuery.addScalar("numObservations", Hibernate.INTEGER);
        sqlQuery.addScalar("problemNameList", Hibernate.STRING);
        sqlQuery.addScalar("problemIdList", Hibernate.STRING);

        //set the parameter(s)
        sqlQuery.setLong("skillModelId", ((Long) skillModelId));

        List <Object[]> results = sqlQuery.list();
        releaseSession(session);

        //
        // Fill in the multi-key map with the results of the query.
        //
        for (Object[] row : results) {
            //Integer sampleId = (Integer)row[BIDX_SAMPLE_ID];
            String sampleName = (String)row[BIDX_SAMPLE_NAME];
            String skillName = (String)row[BIDX_SKILL_NAME];
            Integer numIncorrects = (Integer)row[BIDX_NUM_INCORRECTS];
            Integer numHints = (Integer)row[BIDX_NUM_HINTS];
            Integer numCorrects = (Integer)row[BIDX_NUM_CORRECTS];
            Integer numUnknowns = (Integer)row[BIDX_NUM_UNKNOWNS];
            Integer numStudents = (Integer)row[BIDX_NUM_STUDENTS];
            Integer numObservations = (Integer)row[BIDX_NUM_OBS];
            String problemNameList = (String)row[BIDX_PROBLEM_NAME_LIST];
            String problemIdList = (String)row[BIDX_PROBLEM_ID_LIST];

            ErrorReportBySkill erBySkill = new ErrorReportBySkill();
            erBySkill.setSampleName(sampleName);
            erBySkill.setSkillName(skillName);
            erBySkill.setNumObsCorrect(numCorrects);
            erBySkill.setNumObsIncorrect(numIncorrects);
            erBySkill.setNumObsHint(numHints);
            erBySkill.setNumObsUnknown(numUnknowns);
            erBySkill.setNumStudentsTotal(numStudents);
            erBySkill.setNumObsTotal(numObservations);
            erBySkill.setProblemNameList(problemNameList);

            // If Problem Content isn't available for this dataset,
            // don't set the problemIdList.
            if (isProblemContentAvailable(getDatasetItem(sampleList))) {
                erBySkill.setProblemIdList(problemIdList);
            }

            erSkillList.add(erBySkill);
        }

        return erSkillList;
    }

    /**
     * Helper method to get dataset item from a list of SampleItems.
     * Assumption is that all of the samples in the list are for the
     * same dataset. Get dataset from first sample in the list.
     * @param sampleList list of SampleItems
     * @return the dataset item
     */
    private DatasetItem getDatasetItem(List<SampleItem> sampleList) {
        SampleItem s = sampleList.get(0);
        return s.getDataset();
    }

    /**
     * Helper method to determine if Problem Content is available
     * for the specified dataset.
     * @param dataset the dataset item
     * @return flag indicating result
     */
    private boolean isProblemContentAvailable(DatasetItem dataset) {
        PcConversionDatasetMapDao dao = DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
        boolean isMapped = dao.isDatasetMapped(dataset);
        return isMapped;
    }
}