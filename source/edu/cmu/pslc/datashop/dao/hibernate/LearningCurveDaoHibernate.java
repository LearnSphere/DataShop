/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.CORRECT_STEP_DURATION;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.CORRECT_STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_BAR_TYPE_SD;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_STEP_DURATION;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.STEP_DURATION;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.util.UtilConstants.MAGIC_1000;
import static org.hibernate.Hibernate.DOUBLE;
import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.LONG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.LearningCurveDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dto.LearningCurveOptions;
import edu.cmu.pslc.datashop.dto.LearningCurvePoint;
import edu.cmu.pslc.datashop.dto.LearningCurvePointInfoDetails;
import edu.cmu.pslc.datashop.dto.NameValuePair;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * Hibernate/Spring implementation of the LearningCurveDao.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14323 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-10-05 10:26:26 -0400 (Thu, 05 Oct 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveDaoHibernate extends AbstractSampleDaoHibernate
        implements LearningCurveDao {
    /**
     * indicates a rollup of all of the types (all data sample)
     * so store in -1 slot since typeIndex is a positive DB identifier
     * for the either the skill_id or the student_id.
     */
    public static final long ROLLUP_INDEX = -1L;

    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** HQL query to see if the sample has any rows in the LC table */
    private static final String SAMPLE_READY_QUERY =
          "select curve.sample.id from LearningCurveItem curve"
        + " where curve.sample.id = ?"
        + " group by curve.sample";

    /**
     * Checks that in the learning curve table to see if this sample
     * has been calculated and stored.
     * @param sampleItem The sample to test
     * @return true if the sample has been calculated, false otherwise.
     */
    public boolean sampleReady(SampleItem sampleItem) {
        return getHibernateTemplate().find(SAMPLE_READY_QUERY, sampleItem.getId()).size() > 0;
    }

    /** First part of a Native MySQL query for getting LC data by skill from a step rollup item */
    private static final String GET_LC_BY_SKILL_MY_SQL_PART_1 =
        "SELECT sr.skill_id as typeId,";

    /** First part of a Native MySQL query for getting LC data by student from a step rollup item */
    private static final String GET_LC_BY_STUDENT_MY_SQL_PART_1 =
        "SELECT sr.student_id as typeId,";

    /** Opportunity number query component. */
    private static final String GET_OPPORTUNITY_NUM_MY_SQL =
        " sr.opportunity as oppNum,";

    /** Assistance Score query component. */
    private static final String GET_ASSISTANCE_SCORE_MY_SQL =
        " AVG(sr.total_incorrects + sr.total_hints) as assistanceScore,";

    /** Error Rate query component. */
    private static final String GET_ERROR_RATE_MY_SQL =
        " AVG(sr.error_rate) as errorRate,";

    /** HighStakes Error Rate query component. */
    private static final String GET_HIGH_STAKES_ERROR_RATE_MY_SQL =
        " AVG(sr.error_rate) as highStakesErrorRate,";

    /** Average Incorrects query component. */
    private static final String GET_AVG_INCORRECTS_MY_SQL =
        " AVG(sr.total_incorrects) as avgIncorrects,";

    /** Average Hints query component. */
    private static final String GET_AVG_HINTS_MY_SQL =
        " AVG(sr.total_hints) as avgHints,";

    /** Step Duration query component. */
    private static final String GET_STEP_DURATION_MY_SQL =
        " AVG(sr.step_duration) as stepDuration,";

    /** Correct Step Duration query component. */
    private static final String GET_CORRECT_STEP_DURATION =
        " AVG(sr.correct_step_duration) as correctStepDuration,";

    /** Error Step Duration query component. */
    private static final String GET_ERROR_STEP_DURATION =
        " AVG(sr.error_step_duration) as errorStepDuration,";

    /** Step Duration Observations query component. */
    private static final String GET_STEP_DURATION_OBS_MY_SQL =
        " SUM(IF(sr.step_duration IS NULL, 0, 1)) as stepDurationObs,";

    /** Correct Step Duration Observations query component. */
    private static final String GET_CORRECT_STEP_DURATION_OBS_MY_SQL =
        " SUM(IF(sr.correct_step_duration IS NULL, 0, 1)) as correctStepDurationObs,";

    /** Error Step Duration Observations query component. */
    private static final String GET_ERROR_STEP_DURATION_OBS_MY_SQL =
        " SUM(IF(sr.error_step_duration IS NULL, 0, 1)) as errorStepDurationObs,";

    /** Learning Curve Point Info Counts query component.
     * "XXX" will be replaced with the latency curve measure, or '' for other measure types. */
    private static final String GET_LC_MY_SQL_COUNTS =
        " COUNT(DISTINCT if(XXX is null, null, sr.step_id)) as steps,"
        + " COUNT(DISTINCT if(XXX is null, null, sr.skill_id)) as skills,"
        + " COUNT(DISTINCT if(XXX is null, null, sr.student_id)) as students,"
        + " COUNT(DISTINCT if(XXX is null, null, sr.problem_id)) as problems";

    /** From step_rollup query component. */
    private static final String FROM_STEP_ROLLUP = " FROM step_rollup sr JOIN_SRO";

    /** From step_rollup query component, when displayLowStakes = true. */
    private static final String JOIN_STEP_ROLLUP_OLI = "JOIN step_rollup_oli sro USING (step_rollup_id)";

    /** With Rollup query component. */
    private static final String WITH_ROLLUP = " WITH ROLLUP";

    /** 2nd part of a Native MySQL query for getting LC data from a step rollup item */
    private static final String GET_LC_MY_SQL_PART_2 =
	GET_OPPORTUNITY_NUM_MY_SQL
        + GET_ASSISTANCE_SCORE_MY_SQL
        + GET_ERROR_RATE_MY_SQL
        + GET_AVG_INCORRECTS_MY_SQL
        + GET_AVG_HINTS_MY_SQL
        + GET_STEP_DURATION_MY_SQL
        + GET_CORRECT_STEP_DURATION
        + GET_ERROR_STEP_DURATION
        + " AVG(sr.predicted_error_rate) * 100 as predicted,"
        + " count(*) as observations,"
        + GET_STEP_DURATION_OBS_MY_SQL
        + GET_CORRECT_STEP_DURATION_OBS_MY_SQL
        + GET_ERROR_STEP_DURATION_OBS_MY_SQL
        + GET_LC_MY_SQL_COUNTS;

    /** 2nd part of a Native MySQL query for getting LC data from a step rollup item */
    private static final String GET_LC_MY_SQL_PART_2_WITH_SECONDARY =
	GET_OPPORTUNITY_NUM_MY_SQL
        + " AVG(if(sr2.skill_model_id = :secondaryModelId,"
	+ " null, sr.total_incorrects + sr.total_hints)) as assistanceScore,"
        + " (SUM(if(sr.first_attempt='1' and sr2.skill_model_id != :secondaryModelId,1,0))"
	+ " + SUM(if(sr.first_attempt='0' and sr2.skill_model_id != :secondaryModelId,1,0)))"
	+ " / SUM(if(sr.skill_model_id != :secondaryModelId,1,0)"
	+ " - if (sr2.skill_model_id = :secondaryModelId,1,0)) * 100  as errorRate,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.total_incorrects))"
	+ " as avgIncorrects,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.total_hints))"
	+ " as avgHints,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.step_duration))"
	+ " as stepDuration,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.correct_step_duration))"
	+ " as correctStepDuration,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.error_step_duration))"
	+ " as errorStepDuration,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.predicted_error_rate))"
	+ " * 100 as predicted,"
        + " SUM(if(sr.skill_model_id != :secondaryModelId,1,0)"
	+ " - if (sr2.skill_model_id = :secondaryModelId,1,0))"
	+ " as observations,"
        + GET_STEP_DURATION_OBS_MY_SQL
        + GET_CORRECT_STEP_DURATION_OBS_MY_SQL
        + GET_ERROR_STEP_DURATION_OBS_MY_SQL
        + GET_LC_MY_SQL_COUNTS
        + ", AVG(if(sr2.skill_model_id = :secondaryModelId, sr2.predicted_error_rate, null)) * 100"
        + " as secondaryPredicted";

    /** 3rd part of a Native MySQL query for getting LC data from a step rollup item */
    private static final String GET_LC_MY_SQL_PART_3_WITH_SECONDARY =
        " left join step_rollup sr2 on ("
        + "   sr.sample_id = sr2.sample_id"
        + "   and sr.step_id = sr2.step_id"
        + "   and sr.student_id = sr2.student_id"
        + "   and sr.problem_view = sr2.problem_view"
        + "   and (sr2.skill_model_id = :secondaryModelId or sr.skill_id = sr2.skill_id)) ";

    /** 2nd part of a Native MySQL query for getting highStakes LC data from a step rollup item */
    private static final String GET_LC_MY_SQL_PART_2_HS =
	GET_OPPORTUNITY_NUM_MY_SQL
        + GET_HIGH_STAKES_ERROR_RATE_MY_SQL
        + " AVG(sr.predicted_error_rate) * 100 as predicted,"
        + " count(*) as observations,"
        + GET_LC_MY_SQL_COUNTS;

    /** 2nd part of a Native MySQL query for getting highStakes LC data from a step rollup item */
    private static final String GET_LC_MY_SQL_PART_2_HS_WITH_SECONDARY =
	GET_OPPORTUNITY_NUM_MY_SQL
        + " (SUM(if(sr.first_attempt='1' and sr2.skill_model_id != :secondaryModelId,1,0))"
	+ " + SUM(if(sr.first_attempt='0' and sr2.skill_model_id != :secondaryModelId,1,0)))"
	+ " / SUM(if(sr.skill_model_id != :secondaryModelId,1,0)"
	+ " - if (sr2.skill_model_id = :secondaryModelId,1,0)) * 100  as highStakesErrorRate,"
        + " AVG(if(sr2.skill_model_id = :secondaryModelId, null, sr.predicted_error_rate))"
	+ " * 100 as predicted,"
        + " SUM(if(sr.skill_model_id != :secondaryModelId,1,0)"
	+ " - if (sr2.skill_model_id = :secondaryModelId,1,0))"
	+ " as observations,"
        + GET_LC_MY_SQL_COUNTS
        + ", AVG(if(sr2.skill_model_id = :secondaryModelId, sr2.predicted_error_rate, null)) * 100"
        + " as secondaryPredicted";

    /** HighStakes Error Rate, from rollup, query component. */
    private static final String GET_HS_FROM_ROLLUP_PART1_MY_SQL =
        "SELECT typeId, oppNum, highStakesErrorRate, observations, predicted, "
        + "students, steps, problems, skills FROM (";

    /** HighStakes Error Rate, from rollup, query component. */
    private static final String GET_HS_FROM_ROLLUP_PART2_MY_SQL =
        ") x WHERE oppNum IS NULL";

    /** Additional SQL required to parse by opportunity cutoff */
    private static final String LIMIT_BY_OPP_CUTTOFF_MAX_MYSQL =
        " and sr.opportunity <= :oppMax";

    /** Additional SQL required to parse by opportunity cutoff */
    private static final String LIMIT_BY_OPP_CUTTOFF_MIN_MYSQL =
        " JOIN ("
        + "   select max(srMax.opportunity) as oppMax,"
        + "     srMax.student_id, srMax.skill_id, srMax.sample_id"
        + "   from step_rollup srMax"
        + "   WHERE srMax.sample_id = :sampleId"
        + "   AND srMax.skill_model_id = :modelId"
        + "   group by srMax.student_id, srMax.skill_id) as sm"
        + "    on (sm.student_id = sr.student_id"
        + "    and sm.skill_id = sr.skill_id"
        + "    and sm.sample_id = sr.sample_id) ";

    /** 3rd part of a Native MySQL query for getting LC data from a step rollup item */
    private static final String GET_LC_BY_SKILL_MY_SQL_GROUP_BY =
        " GROUP BY sr.opportunity, sr.skill_id";

    /** 3rd part of a Native MySQL query for getting LC data from a step rollup item */
    private static final String GET_LC_BY_STUDENT_MY_SQL_GROUP_BY =
        " GROUP BY sr.opportunity, sr.student_id";

    /** Index of the learning curve type
     * (-1 for all, skill_id or student_id otherwise) */

    /** SQL query to gather the maximum opportunity count */
    private static final String GET_MAX_OPP_COUNT_MYSQL =
        " MAX(sr.opportunity) as maxOpp FROM step_rollup sr "
        + " WHERE sr.sample_id = :sampleId";

    /** SQL query component for gathering max opportunity count by skill. */
    private static final String GET_MAX_OPP_COUNT_BY_SKILL_MYSQL =
        " group by sr.skill_id with rollup";

    /** SQL query component for gathering max opportunity count by student. */
    private static final String GET_MAX_OPP_COUNT_BY_STUDENT_MYSQL =
        " group by sr.student_id with rollup";

    /**
     * Correct step time observations or assistance time observations,
     * depending on the selected type.
     * @param info standard deviation information for a given opportunity
     * @param selectedType the selected learning curve type
     * @return step duration, correct step duration or error step duration observations,
     *  depending on the selected type (or null if info if null)
     */
    private Integer cutoffForStdDevInfo(StdDevInfo info, String selectedType) {
        if (info == null) {
            logDebug("null info for type:: ", selectedType, ", returning 0.");
            return 0;
        }
        if (selectedType.equals(STEP_DURATION)) {
            return info.getStepDurationObs();
        } else if (selectedType.equals(CORRECT_STEP_DURATION)) {
            return info.getCorrectStepDurationObs();
        } else if (selectedType.equals(ERROR_STEP_DURATION)) {
            return info.getErrorStepDurationObs();
        } else {
            return null;
        }
    }

    /**
     * Return the list of points for type index, or an empty list if none exists.
     * @param pointsMap maps type index to list of points
     * @param typeIndex identifies the graph type
     * @return the list of points for type index, or an empty list if none exists
     */
    private List<LearningCurvePoint> pointsForType(Map<Long, List<LearningCurvePoint>> pointsMap,
            Long typeIndex) {
        List<LearningCurvePoint> pointsList = pointsMap.get(typeIndex);

        if (pointsList == null) {
            pointsList = new ArrayList<LearningCurvePoint>();
            pointsMap.put(typeIndex, pointsList);
        }

        return pointsList;
    }

    /**
     * Get a learning curve for a given skill model and sample.
     * @param reportOptions a helper class containing all the datashop report options.
     * @return a HashMap with the key of an Long index of the type.  The value is a
     * list of learning curve points in order by opportunity.
     */
    public Map<Long, List<LearningCurvePoint>>
                getLearningCurve(LearningCurveOptions reportOptions) {
        final String selectedMeasure = reportOptions.getSelectedMeasure();
        Integer opportunityCutOffMin = reportOptions.getOpportunityCutOffMin();
        Integer opportunityCutOffMax = reportOptions.getOpportunityCutOffMax();
        Double stdDevCutoff = reportOptions.getStdDeviationCutOff();
        SkillModelItem secondaryModel = reportOptions.getSecondaryModel();
        final String errorBarType = reportOptions.getErrorBarType();
        Map<Long, Map<Integer, StdDevInfo>> stdDevInfoMap = null;
        StdDevInfo rollupStdDevInfo = null;
        final boolean hasSecondaryModel = secondaryModel != null && secondaryModel.getId() != null;

        if (reportOptions.isLatencyCurve() && stdDevCutoff != null && stdDevCutoff > 0) {
            stdDevInfoMap = getStandardDeviationInfo(reportOptions);
            Map<Integer, StdDevInfo> rollupMap = stdDevInfoMap.get(-1L);
            if (rollupMap != null) { rollupStdDevInfo = rollupMap.get(-1); }
        }

        String queryString = buildLearningCurveQuery(reportOptions, rollupStdDevInfo);

        logTrace(queryString);

        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(queryString);

        sqlQuery.setInteger("sampleId", (Integer)reportOptions.getSampleItem().getId());
        sqlQuery.setLong("modelId", (Long)reportOptions.getPrimaryModel().getId());
        if (opportunityCutOffMin != null) {
            sqlQuery.setInteger("oppMin", opportunityCutOffMin);
        }
        if (opportunityCutOffMax != null) {
            sqlQuery.setInteger("oppMax", opportunityCutOffMax);
        }
        if (hasSecondaryModel && !reportOptions.isLatencyCurve()) {
            sqlQuery.setLong("secondaryModelId", (Long)secondaryModel.getId());
        }
        addScalars(sqlQuery, "typeId", LONG, "oppNum", INTEGER, "assistanceScore", DOUBLE,
		   "errorRate", DOUBLE, "avgIncorrects", DOUBLE, "avgHints", DOUBLE,
		   "stepDuration", DOUBLE, "correctStepDuration", DOUBLE,
		   "errorStepDuration", DOUBLE, "predicted", DOUBLE, "observations", INTEGER,
		   "stepDurationObs", INTEGER, "correctStepDurationObs", INTEGER,
		   "errorStepDurationObs", INTEGER, "steps", INTEGER, "skills", INTEGER,
		   "students", INTEGER, "problems", INTEGER);
        if (errorBarType != null) {
            if (errorBarType.equals(ERROR_BAR_TYPE_SD)) {
                addScalars(sqlQuery, "stdDevErrorRate", DOUBLE,
                        "stdDevAssistanceScore", DOUBLE,
                        "stdDevIncorrects", DOUBLE, "stdDevHints", DOUBLE,
                        "stdDevStepDuration", DOUBLE,
                        "stdDevCorrectStepDuration", DOUBLE,
                        "stdDevErrorStepDuration", DOUBLE);
            } else {
                addScalars(sqlQuery, "stdErrErrorRate", DOUBLE,
                        "stdErrAssistanceScore", DOUBLE,
                        "stdErrIncorrects", DOUBLE, "stdErrHints", DOUBLE,
                        "stdErrStepDuration", DOUBLE,
                        "stdErrCorrectStepDuration", DOUBLE,
                        "stdErrErrorStepDuration", DOUBLE);
            }
        }
        if (hasSecondaryModel && !reportOptions.isLatencyCurve()) {
            sqlQuery.addScalar("secondaryPredicted", DOUBLE);
        }
        sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        List<Map<String, Object>> results = sqlQuery.list();

        releaseSession(session);

        Map<Long, List<LearningCurvePoint>> pointsMap =
                    new HashMap<Long, List<LearningCurvePoint>>();
        List<LearningCurvePoint> pointsList;
        Map<Integer, StdDevInfo> infoMap;
        int maxOppNo = 0;

        Map<Long, Integer> opportunityMap = new HashMap<Long, Integer>();

        for (final Map<String, Object> resultRow : results) {
            Long typeIndex = (Long)resultRow.get("typeId");
            Integer opportunity = (Integer)resultRow.get("oppNum");
            if (typeIndex == null && opportunity == null) {
                continue; //don't care about the total rollup
            } else if (typeIndex == null) {
                typeIndex = ROLLUP_INDEX;
            }

            // If a curve is missing opportunities (e.g., OLI low-stakes error rate
            // curves) shift the points from right to left to fill in graph.
            if (reportOptions.getDisplayLowStakesCurve() &&
                reportOptions.getSelectedMeasure().equals(LearningCurveOptions.ERROR_RATE)) {
                Integer lastValidOpp = opportunityMap.get(typeIndex);

                if ((opportunity != null) && (lastValidOpp != null)) {
                    if (opportunity != (lastValidOpp + 1)) {
                        opportunity = lastValidOpp + 1;
                        logDebug("Opportunity " + opportunity + " is missing.");
                    }
                }
            }

            infoMap = stdDevInfoMap == null ? null : stdDevInfoMap.get(typeIndex);

            if (infoMap != null) {
                for (int oppNo : infoMap.keySet()) {
                    if (oppNo > maxOppNo) {
                        maxOppNo = oppNo;
                    }
                }
            }

            pointsList = pointsForType(pointsMap, typeIndex);

            //we have missing data points probably due to a cutoff, so stick a dummy
            //point in with only the opportunity number and the pre-cutoff observations
            //if they exist.
            int addedOpportunities = pointsList.size();
            while ((addedOpportunities + 1) < opportunity) {
                addedOpportunities++;
                pointsList.add(dummyPoint(selectedMeasure, infoMap, addedOpportunities));
                logDebug("adding new dummy point :: ", pointsList.get(pointsList.size() - 1));
            }
            LearningCurvePoint p =
		createLearningCurvePoint(resultRow, errorBarType, hasSecondaryModel);
            p.setOpportunityNumber(opportunity);
            setPreCutoffObs(selectedMeasure, opportunity, infoMap, p);
            pointsList.add(p);
            logTrace(" adding new point :: ", p, " for type :: ", typeIndex);

            // Make note of current opportunity... to look for missing ones.
            opportunityMap.put(typeIndex, opportunity);
        }

        // need to add dummy points at the end, too, if missing those points due to a cutoff
        for (Long typeIndex : pointsMap.keySet()) {
            infoMap = stdDevInfoMap == null ? null : stdDevInfoMap.get(typeIndex);
            if (infoMap != null) {
                pointsList = pointsForType(pointsMap, typeIndex);
                int oppNo = pointsList.size();

                while (oppNo < maxOppNo) {
                    pointsList.add(dummyPoint(selectedMeasure, infoMap, ++oppNo));
                }
            }
        }

        // If viewing lowStakes-only curve, add the single (roll-up) highStakes point too...
        // ... only makes sense for 'Error Rate' curves.
        if (reportOptions.getDisplayLowStakesCurve() &&
            reportOptions.getSelectedMeasure().equals(LearningCurveOptions.ERROR_RATE)) {
            for (Long typeIndex : pointsMap.keySet()) {
                pointsList = pointsForType(pointsMap, typeIndex);

                LearningCurvePoint lcp = getHighStakesErrorRate(reportOptions, typeIndex, pointsList.size());
                if (lcp != null)  { 
                    pointsList.add(lcp);
                }
            }
        }

        return pointsMap;
    }

    /**
     * Helper method for creating LearningCurvePoint.
     * @param resultRow map of values for the point
     * @param errorBarType type of error bar to display
     * @param hasSecondaryModel flag indicating presence of secondary model
     * @return LearningCurvePoint with relevant attributes set
     */
    private LearningCurvePoint createLearningCurvePoint(Map<String, Object> resultRow,
                                                        String errorBarType,
                                                        boolean hasSecondaryModel) {

        LearningCurvePoint p = new LearningCurvePoint();
        p.setObservations((Integer) resultRow.get("observations"));
        p.setErrorRates((Double) resultRow.get("errorRate"));
        p.setAvgIncorrects((Double) resultRow.get("avgIncorrects"));
        p.setAvgHints((Double) resultRow.get("avgHints"));
        p.setStepDuration((Double) resultRow.get("stepDuration"));
        p.setCorrectStepDuration((Double) resultRow.get("correctStepDuration"));
        p.setErrorStepDuration((Double) resultRow.get("errorStepDuration"));
        p.setAssistanceScore((Double) resultRow.get("assistanceScore"));
        p.setPredictedErrorRate((Double) resultRow.get("predicted"));
        p.setStepDurationObservations((Integer) resultRow.get("stepDurationObs"));
        p.setCorrectStepDurationObservations((Integer) resultRow.get("correctStepDurationObs"));
        p.setErrorStepDurationObservations((Integer) resultRow.get("errorStepDurationObs"));
        p.setStudentsCount((Integer) resultRow.get("students"));
        p.setStepsCount((Integer) resultRow.get("steps"));
        p.setProblemsCount((Integer) resultRow.get("problems"));
        p.setSkillsCount((Integer) resultRow.get("skills"));
        if (hasSecondaryModel) {
            p.setSecondaryPredictedErrorRate((Double) resultRow.get("secondaryPredicted"));
        }
        if (errorBarType != null) {
            if (errorBarType.equals(ERROR_BAR_TYPE_SD)) {
                p.setStdDevErrorRate((Double) resultRow.get("stdDevErrorRate"));
                p.setStdDevAssistanceScore((Double) resultRow.get("stdDevAssistanceScore"));
                p.setStdDevIncorrects((Double) resultRow.get("stdDevIncorrects"));
                p.setStdDevHints((Double) resultRow.get("stdDevHints"));
                p.setStdDevStepDuration((Double) resultRow.get("stdDevStepDuration"));
                p.setStdDevCorrectStepDuration((Double) resultRow.
                            get("stdDevCorrectStepDuration"));
                p.setStdDevErrorStepDuration((Double) resultRow.get("stdDevErrorStepDuration"));
            } else {
                p.setStdErrErrorRate((Double) resultRow.get("stdErrErrorRate"));
                p.setStdErrAssistanceScore((Double) resultRow.get("stdErrAssistanceScore"));
                p.setStdErrIncorrects((Double) resultRow.get("stdErrIncorrects"));
                p.setStdErrHints((Double) resultRow.get("stdErrHints"));
                p.setStdErrStepDuration((Double) resultRow.get("stdErrStepDuration"));
                p.setStdErrCorrectStepDuration((Double) resultRow
                            .get("stdErrCorrectStepDuration"));
                p.setStdErrErrorStepDuration((Double) resultRow.get("stdErrErrorStepDuration"));
            }
        }

        return p;
    }

    /**
     * Get the single highStakes error rate data point for the specified curve.
     * @param reportOptions the learning curve options
     * @param typeId the itemId of interest, skill or student
     * @param maxOpportunity the place on the curve where point is displayed
     * @return the LearningCurvePoint
     */
    private LearningCurvePoint getHighStakesErrorRate(LearningCurveOptions reportOptions,
						      Long typeId, Integer maxOpportunity) {

        List skillList = reportOptions.getSkillList();
        List studentList = reportOptions.getStudentList();
        boolean isViewBySkill = reportOptions.isViewBySkill();
        Integer opportunityCutOffMin = reportOptions.getOpportunityCutOffMin();
        Integer opportunityCutOffMax = reportOptions.getOpportunityCutOffMax();
        SkillModelItem secondaryModel = reportOptions.getSecondaryModel();
        boolean viewSecondary = (secondaryModel != null
                && secondaryModel.getId() != null && !reportOptions.isLatencyCurve());

        StringBuffer query = new StringBuffer();

	query.append(addTypeID(isViewBySkill));
        if (viewSecondary) {
            query.append(GET_LC_MY_SQL_PART_2_HS_WITH_SECONDARY);
        } else {
            query.append(GET_LC_MY_SQL_PART_2_HS);
        }

        query.append(FROM_STEP_ROLLUP);

        if (viewSecondary) {
            query.append(GET_LC_MY_SQL_PART_3_WITH_SECONDARY);
        }

        if (opportunityCutOffMin != null) {
            query.append(LIMIT_BY_OPP_CUTTOFF_MIN_MYSQL);
        }

	// Add WHERE clause
        query.append(addWhereClause());

	query.append(" AND sro.high_stakes = 1 ");

        // add skill and/or student identifiers
	if (isViewBySkill) {
	    if (typeId == ROLLUP_INDEX) {
		if (skillList != null) {
		    query.append(addSkillsOrStudentsToWhereClause(skillList, "skill"));
		}
	    } else {
		query.append(" AND sr.skill_id = " + typeId);
	    }
	    if (studentList != null) {
		query.append(addSkillsOrStudentsToWhereClause(studentList, "student"));
	    }
	} else {
	    if (typeId == ROLLUP_INDEX) {
		if (studentList != null) {
		    query.append(addSkillsOrStudentsToWhereClause(studentList, "student"));
		}
	    } else {
		query.append(" AND sr.student_id = " + typeId);
	    }
	    if (skillList != null) {
		query.append(addSkillsOrStudentsToWhereClause(skillList, "skill"));
	    }
	}

        if (opportunityCutOffMin != null) { query.append(" and sm.oppMax >= :oppMin "); }
        if (opportunityCutOffMax != null) { query.append(LIMIT_BY_OPP_CUTTOFF_MAX_MYSQL); }

        query.append(addGroupByClause(isViewBySkill));
        query.append(WITH_ROLLUP);

        String measure = reportOptions.isLatencyCurve() ? reportOptions.getSelectedMeasureType()
                : "''";

        // need to specify the measure to get the correct student/step/problem/skill counts
        String queryStr = query.toString().replace(TO_REPLACE_STRING, measure);

	// Add 'JOIN step_rollup_oli' piece.
	queryStr = queryStr.replace("JOIN_SRO", JOIN_STEP_ROLLUP_OLI);

        // We only want the final, rollup, value.
        StringBuffer sb = new StringBuffer();
        sb.append(GET_HS_FROM_ROLLUP_PART1_MY_SQL);
        sb.append(queryStr);
        sb.append(GET_HS_FROM_ROLLUP_PART2_MY_SQL);
        queryStr = sb.toString();

        logTrace(queryStr);

        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(queryStr);

        sqlQuery.setInteger("sampleId", (Integer)reportOptions.getSampleItem().getId());
        sqlQuery.setLong("modelId", (Long)reportOptions.getPrimaryModel().getId());
        if (opportunityCutOffMin != null) {
            sqlQuery.setInteger("oppMin", opportunityCutOffMin);
        }
        if (opportunityCutOffMax != null) {
            sqlQuery.setInteger("oppMax", opportunityCutOffMax);
        }
        if (viewSecondary && !reportOptions.isLatencyCurve()) {
            sqlQuery.setLong("secondaryModelId", (Long)secondaryModel.getId());
        }
        addScalars(sqlQuery, "typeId", LONG, "oppNum", INTEGER, "highStakesErrorRate", DOUBLE,
		   "predicted", DOUBLE, "observations", INTEGER, "steps", INTEGER,
		   "skills", INTEGER, "students", INTEGER, "problems", INTEGER);

        if (viewSecondary && !reportOptions.isLatencyCurve()) {
            sqlQuery.addScalar("secondaryPredicted", DOUBLE);
        }
        sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        List<Map<String, Object>> results = sqlQuery.list();

        releaseSession(session);

	if (results.size() == 0) {
	    logTrace("HighStakes query returned empty result set.");
	    return null;
	}

	int count = 0;
	Double hsErrorRate = null;
	LearningCurvePoint lcp = new LearningCurvePoint();
	
	// We only need the rollup row which is the last one.
	Map<String, Object> resultRow = results.get(results.size() - 1);
	Long typeIndex = (Long)resultRow.get("typeId");
	if (typeIndex == null) {
	    // The total rollup has the highStakes error rate
	    lcp.setHighStakesErrorRate((Double) resultRow.get("highStakesErrorRate"));
	    lcp.setOpportunityNumber(maxOpportunity);

	    // The rest of these are just so the LCPID stuff works. Sigh.
	    lcp.setObservations((Integer) resultRow.get("observations"));
	    lcp.setErrorRates(lcp.getHighStakesErrorRate());
	    lcp.setPredictedErrorRate((Double) resultRow.get("predicted"));
	    lcp.setStudentsCount((Integer) resultRow.get("students"));
	    lcp.setStepsCount((Integer) resultRow.get("steps"));
	    lcp.setProblemsCount((Integer) resultRow.get("problems"));
	    lcp.setSkillsCount((Integer) resultRow.get("skills"));
	}

	logTrace("HighStakesErrorRate point = " + lcp);
	return lcp;
    }

    /**
     * Set the pre-cutoff observations for the point.
     * @param selectedMeasure Error Rate, Step Duration, etc.
     * @param oppNo the opportunity number
     * @param infoMap maps opportunity numbers to standard deviation info.
     * @param p the point
     */
    private void setPreCutoffObs(String selectedMeasure, Integer oppNo,
            Map<Integer, StdDevInfo> infoMap, LearningCurvePoint p) {
        if (infoMap != null) {
            p.setPreCutoffObservations(cutoffForStdDevInfo(infoMap.get(oppNo),
                    selectedMeasure));
        }
    }

    /**
     * Create a dummy point when all observations for an opportunity are outside the
     * standard deviation cutoff.
     * @param selectedMeasure Error Rate, Step Duration, etc.
     * @param infoMap maps opportunity numbers to standard deviation info.
     * @param dummyOppNo the opportunity number
     * @return the newly created dummy point
     */
    private LearningCurvePoint dummyPoint(String selectedMeasure,
            Map<Integer, StdDevInfo> infoMap, final int dummyOppNo) {
        logDebug("Creating a new dummy point for opp:: ", dummyOppNo);
        LearningCurvePoint p = new LearningCurvePoint() { {
            setObservations(0);
            setStepDurationObservations(0);
            setCorrectStepDurationObservations(0);
            setErrorStepDurationObservations(0);
            setOpportunityNumber(dummyOppNo);
        } };
        setPreCutoffObs(selectedMeasure, dummyOppNo, infoMap, p);
        return p;
    }

    /** Measure ID query component. */
    private static final String MEASURE_ID = "measureId";
    /** Measure Name query component. */
    private static final String MEASURE_NAME = "measureName";
    /** Curve Type Value query component. */
    private static final String CURVE_TYPE_VALUE = "curveTypeValue";
    /** Default for the direction of the LCPID sort. */
    private static final String DEFAULT_SORT_DIRECTION = "ASC";

    /** LCPI Student Details query component. */
    private static final String GET_LCPI_STUDENT_DETAILS =
        " sr.student_id as " + MEASURE_ID + ", anon_user_id as " + MEASURE_NAME
        + ", sr.problem_id as problemId, COUNT(sr.student_id) as frequency "
        + FROM_STEP_ROLLUP
        + " JOIN student USING (student_id)";

    /** LCPI Skill Details query component. */
    private static final String GET_LCPI_SKILL_DETAILS =
        " sr.skill_id as " + MEASURE_ID + ", skill_name as " + MEASURE_NAME
        + ", sr.problem_id as problemId, COUNT(sr.skill_id) as frequency "
        + FROM_STEP_ROLLUP
        + " JOIN skill USING (skill_id)";

    /** LCPI Problem Details query component. */
    private static final String GET_LCPI_PROBLEM_DETAILS =
        " sr.problem_id as " + MEASURE_ID + ", problem_name as " + MEASURE_NAME
        + ", sr.problem_id as problemId, COUNT(sr.problem_id) as frequency "
        + FROM_STEP_ROLLUP
        + " JOIN problem USING (problem_id)";

    /** LCPI Step Details query component. */
    private static final String GET_LCPI_STEP_DETAILS =
        " sr.step_id as " + MEASURE_ID + ", subgoal_name as " + MEASURE_NAME
        + ", sr.problem_id as problemId, COUNT(sr.step_id) as frequency "
        + FROM_STEP_ROLLUP
        + " JOIN subgoal ON sr.step_id = subgoal.subgoal_id";


    /**
     * Helper method to build the LCPID query.
     * @param oppNum the opportunity number to query against.
     * @param reportOptions the selections made by the user, necessary for gathering the
     * right information.
     * @param measure the measure the user wishes to view (student, skill, step or problem details).
     * @param sortBy how the results should be sorted (by measure or curveTypeValue).
     * @param sortDirection the direction in which the results should be sorted (ASC or DESC).
     * @param stdDevInfo the relevant standard deviation info, can be null.
     * @return a nicely formatted query string!
     */
    private String buildLCPIDQuery(int oppNum, LearningCurveOptions reportOptions,
            String measure, String sortBy, String sortDirection, StdDevInfo stdDevInfo) {
        List skillList = reportOptions.getSkillList();
        List studentList = reportOptions.getStudentList();
        boolean isViewBySkill = reportOptions.isViewBySkill();
        String selectedType = reportOptions.getSelectedMeasure();
        Integer opportunityCutOffMin = reportOptions.getOpportunityCutOffMin();
        Integer opportunityCutOffMax = reportOptions.getOpportunityCutOffMax();
        StringBuffer query = new StringBuffer();

        // build the query.
        query.append("SELECT");
        // add the curveType (error rate, assistance score, etc)
        query.append(addSelectedCurveType(selectedType));
        // add the measure(details) requested.
        query.append(addSelectedMeasure(selectedType, measure));
        // add opportunity cutoff min information
        if (opportunityCutOffMin != null) { query.append(LIMIT_BY_OPP_CUTTOFF_MIN_MYSQL); }
        // add the WHERE clause
        query.append(addWhereClause(oppNum));

	// If viewing lowStakes curve (e.g., OLI) then we want the normal (red)
	// error_rate curve to be lowStakes only, meaning no entry in step_rollup_oli table
	if ((reportOptions.getDisplayLowStakesCurve()) && (selectedType.equals(LearningCurveOptions.ERROR_RATE))) {
	    query.append(" AND sro.step_rollup_id IS NULL ");
	}

        // add skill and/or student identifiers
        if (skillList != null) {
            query.append(addSkillsOrStudentsToWhereClause(skillList, "skill"));
        }
        if (studentList != null) {
            query.append(addSkillsOrStudentsToWhereClause(studentList, "student"));
        }
        // add on the standard deviation cutoff SQL if selected type is a latency curve.
        // else if it is a latency curve make sure null step durations aren't tabulated.
        if (stdDevInfo != null) {
            query.append(addStdDevCutoffSQL(selectedType, stdDevInfo, reportOptions, true));
        } else {
            query.append(addLatencyDurationSQL(selectedType, reportOptions));
        }
        if (opportunityCutOffMin != null) { query.append(" AND sm.oppMax >= :oppMin "); }
        if (opportunityCutOffMax != null) { query.append(LIMIT_BY_OPP_CUTTOFF_MAX_MYSQL); }
        // check for correct or error step duration - if so, customize query to
        // examine first_attempt accordingly
        String additionalWhereClause = examineFirstAttempt(selectedType);
        if (additionalWhereClause != null) { query.append(additionalWhereClause); }
        query.append(addGroupByClause(isViewBySkill, measure));
        query.append(" ORDER BY " + sortBy + " " + sortDirection);
        query.append(" LIMIT 50");

	String queryStr = query.toString();

	if ((reportOptions.getDisplayLowStakesCurve()) && (selectedType.equals(LearningCurveOptions.ERROR_RATE))) {
	    // add the 'JOIN step_rollup_oli' bit
	    queryStr = queryStr.replace("JOIN_SRO", "LEFT " + JOIN_STEP_ROLLUP_OLI);
	} else {
	    queryStr = queryStr.replace("JOIN_SRO", "");
	}

        logTrace(queryStr);
        return queryStr;

    } // end buildLCPIDQuery()

    /**
     * Helper method to build learning curve queries.  Given the selected
     * typeID or viewBy (skill or student) param, return and appropriate
     * SQL SELECT statement (just the beginning).
     * @param isViewBySkill if the curve type is skill or not.
     * @return the appropriate SQL string.
     */
    private String addTypeID(boolean isViewBySkill) {
        if (isViewBySkill) {
            return GET_LC_BY_SKILL_MY_SQL_PART_1;
        } else {
            return GET_LC_BY_STUDENT_MY_SQL_PART_1;
        }
    } // end addTypeID()

    /**
     * Helper method to determine if we are processing a step duration graph.
     * @param selectedType the selected type of learning curve.
     * @return null if selectedType is not a duration graph, otherwise the name of the
     * corresponding column in the step_rollup table.
     */
    private String getDurationType(String selectedType) {
        String toReturn = null;
        if (selectedType.equals(LearningCurveOptions.STEP_DURATION)) {
            toReturn = "step_duration";
        } else if (selectedType.equals(LearningCurveOptions.CORRECT_STEP_DURATION)) {
            toReturn = "correct_step_duration";
        } else if (selectedType.equals(LearningCurveOptions.ERROR_STEP_DURATION)) {
            toReturn = "error_step_duration";
        }
        return toReturn;
    }

    /**
     * Helper method to build the LCPID query.  Examine the measure and
     * return the appropriate SQL string.  In order for the frequency values to be
     * correct for duration-type graphs, we must customize the COUNT() statement
     * for the corresponding duration column in the step_rollup table (this is because
     * duration values can be null, whereas other graph type values cannot).
     * @param selectedType the selected type of learning curve graph.
     * @param measure the selected measure.
     * @return the appropriate SQL string.
     */
    private String addSelectedMeasure(String selectedType, String measure) {
        String durationType = getDurationType(selectedType);
        if (measure.equals(LCPI_STUDENT_DETAILS)) {
            if (durationType != null) {
                return GET_LCPI_STUDENT_DETAILS.replace("COUNT(student_id)",
                        "COUNT(" + durationType + ")");
            } else {
                return GET_LCPI_STUDENT_DETAILS;
            }
        } else if (measure.equals(LCPI_SKILL_DETAILS)) {
            if (durationType != null) {
                return GET_LCPI_SKILL_DETAILS.replace("COUNT(skill_id)",
                        "COUNT(" + durationType + ")");
            } else {
                return GET_LCPI_SKILL_DETAILS;
            }
        } else if (measure.equals(LCPI_PROBLEM_DETAILS)) {
            if (durationType != null) {
                return GET_LCPI_PROBLEM_DETAILS.replace("COUNT(problem_id)",
                        "COUNT(" + durationType + ")");
            } else {
                return GET_LCPI_PROBLEM_DETAILS;
            }
        } else {
            if (durationType != null) {
                return GET_LCPI_STEP_DETAILS.replace("COUNT(step_id)",
                        "COUNT(" + durationType + ")");
            }
            return GET_LCPI_STEP_DETAILS;
        }
    } // end addSelectedMeasure()

    /**
     * Helper method to build the LCPID query.  Examine the selectedType
     * and return the appropriate SQL string.
     * @param selectedType the selected curve type.
     * @return the appropriate SQL string.
     */
    private String addSelectedCurveType(String selectedType) {
        if (selectedType.equals(LearningCurveOptions.ASSISTANCE_SCORE)) {
            return GET_ASSISTANCE_SCORE_MY_SQL.replace("assistanceScore", CURVE_TYPE_VALUE);
        } else if (selectedType.equals(LearningCurveOptions.ERROR_RATE)) {
            return GET_ERROR_RATE_MY_SQL.replace("errorRate", CURVE_TYPE_VALUE);
        } else if (selectedType.equals(LearningCurveOptions.NUMBER_OF_INCORRECTS)) {
            return GET_AVG_INCORRECTS_MY_SQL.replace("avgIncorrects", CURVE_TYPE_VALUE);
        } else if (selectedType.equals(LearningCurveOptions.NUMBER_OF_HINTS)) {
            return GET_AVG_HINTS_MY_SQL.replace("avgHints", CURVE_TYPE_VALUE);
        } else if (selectedType.equals(LearningCurveOptions.STEP_DURATION)) {
            return GET_STEP_DURATION_MY_SQL.replace("stepDuration", CURVE_TYPE_VALUE);
        } else if (selectedType.equals(LearningCurveOptions.CORRECT_STEP_DURATION)) {
            return GET_CORRECT_STEP_DURATION.replace("correctStepDuration", CURVE_TYPE_VALUE);
        } else {
            return GET_ERROR_STEP_DURATION.replace("errorStepDuration", CURVE_TYPE_VALUE);
        }
    } // end addSelectedCurveType()

    /**
     * Helper method to build learning curve queries.  Returns a WHERE clause
     * string, customized based on the value of opportunity number.
     * @return the appropriate SQL string.
     */
    private String addWhereClause() {
        return addWhereClause(null);
    }

    /**
     * Helper method to build learning curve queries.  Returns a WHERE clause
     * string, customized based on the value of opportunity number.
     * @param oppNum the opportunity number (can be null).
     * @return the appropriate SQL string.
     */
    private String addWhereClause(Integer oppNum) {
        StringBuffer sql = new StringBuffer();
        sql.append(" WHERE sr.sample_id = :sampleId"
                + " AND sr.skill_model_id = :modelId"
                + " AND sr.first_attempt != '3'");
        if (oppNum == null) {
            sql.append(" AND sr.opportunity IS NOT NULL");
        } else {
            sql.append(" AND sr.opportunity = " + oppNum);
        }
        return sql.toString();
    } // end addWhereClause()

    /**
     * Helper method to build learning curve queries.  Returns a formatted
     * IN clause containing the provided skill item identifiers.
     * @param itemList the list of skills or students to process.
     * @param listType indicates if the list contains skill or student items.
     * @return a nicely formatter SQL string.
     */
    private String addSkillsOrStudentsToWhereClause(List itemList, String listType) {
        StringBuffer sql = new StringBuffer();
        if (listType.equals("skill")) {
            sql.append(" AND sr.skill_id IN (");
        } else {
            sql.append(" AND sr.student_id IN (");
        }
        for (Iterator it = itemList.iterator(); it.hasNext();) {
            Item item = (Item)it.next();
            sql.append(item.getId());
            if (it.hasNext()) { sql.append(", "); }
        }
        sql.append(")");
        return sql.toString();
    } // end addSkillsToWhereClause()

    /**
     * Helper method to build learning curve queries.  Return appropriate SQL based
     * on if the selected type is a latency curve and if standard deviation information exists.
     * This method is only called if the selected type is a latency curve.
     * @param selectedType the selected curve type.
     * @param stdDevInfo the standard deviation information for the curve.
     * @param reportOptions the learning curve report options.
     * @param isLCPID if this SQL is going to be incorporated into an LCPID frequency query or not.
     * @return the appropriate SQL.
     */
    private String addStdDevCutoffSQL(String selectedType, StdDevInfo stdDevInfo,
            LearningCurveOptions reportOptions, boolean isLCPID) {
        Double stdDev, cutoff = reportOptions.getStdDeviationCutOff();
        String latencyMeasure;

        if (STEP_DURATION.equals(selectedType)) {
            stdDev = stdDevInfo.getStepDurationStdDev();
            latencyMeasure = STEP_DURATION_TYPE;
        } else if (CORRECT_STEP_DURATION.equals(selectedType)) {
            stdDev = stdDevInfo.getCorrectStepDurationStdDev();
            latencyMeasure = CORRECT_STEP_DURATION_TYPE;
        } else {
            stdDev = stdDevInfo.getErrorStepDurationStdDev();
            latencyMeasure = ERROR_STEP_DURATION_TYPE;
        }
        if (stdDev != null && stdDev != 0.0) {
            if (isLCPID) {
                return " AND (sr." + latencyMeasure + " <= ( " + cutoff + " * " + stdDev
                + ") AND sr." + latencyMeasure + " IS NOT NULL) ";
            } else {
                return " AND (sr." + latencyMeasure + " <= ( " + cutoff + " * " + stdDev
                    + ")   OR sr." + latencyMeasure + " IS NULL) ";
            }
        }
        return "";
    }

    /**
     * Helper method to build learning curve queries.  Return appropriate SQL based
     * on if the selected type is a latency curve and if standard deviation information exists.
     * This method is only called if the selected type is a latency curve.
     * @param selectedType the selected curve type.
     * @param stdDevInfo the standard deviation information for the curve.
     * @param reportOptions the learning curve report options.
     * @return the appropriate SQL.
     */
    private String addStdDevCutoffSQL(String selectedType, StdDevInfo stdDevInfo,
            LearningCurveOptions reportOptions) {
        return addStdDevCutoffSQL(selectedType, stdDevInfo, reportOptions, false);
    }

    /**
     * Helper method to build learning curve queries.  Return appropriate SQL only
     * if the selected type is a latency curve.  Only called for building LCPID instances.
     * This method is only called if the standard deviation is null or 0.0.
     * @param selectedType the selected curve type.
     * @param reportOptions the learning curve report options.
     * @return step duration SQL in the WHERE clause to exclude null durations, if the
     * selectedType is a latency curve.  For non-latency curve types, return the empty
     * string.
     */
    private String addLatencyDurationSQL(String selectedType,
            LearningCurveOptions reportOptions) {

        if (STEP_DURATION.equals(selectedType)) {
            return " AND sr." + STEP_DURATION_TYPE + " IS NOT NULL ";
        } else if (CORRECT_STEP_DURATION.equals(selectedType)) {
            return " AND sr." + CORRECT_STEP_DURATION_TYPE + " IS NOT NULL ";
        } else if (ERROR_STEP_DURATION.equals(selectedType)) {
            return " AND sr." + ERROR_STEP_DURATION_TYPE + " IS NOT NULL ";
        }

        return "";
    }

    /**
     * Helper object for storing info necessary to construct error bar SQL query.
     */
    static class ErrorBarSQLInfo {
        /** The database column to be queried. */
        private String dbColumn;
        /** The standard deviation variable name. */
        private String sdVarName;
        /** The standard error variable name. */
        private String seVarName;

        /**
         * Constructor.
         * @param dbColumn name of database column to query
         * @param sdVarName name of std dev variable
         * @param seVarName name of std err variable
         */
        ErrorBarSQLInfo(String dbColumn, String sdVarName, String seVarName) {
            this.dbColumn = dbColumn;
            this.sdVarName = sdVarName;
            this.seVarName = seVarName;
        }

        /** Getter for database column. @return Returns the dbColumn. */
        public String getDbColumn() {
            return dbColumn;
        }

        /**
         * Getter for standard deviation variable name. @return Returns the sdVarName.
         */
        public String getSdVarName() {
            return sdVarName;
        }

        /**
         * Getter for standard error variable name. @return Returns the seVarName.
         */
        public String getSeVarName() {
            return seVarName;
        }
    } // end class ErrorBarSQLInfo

    /** Map which holds instances of ErrorBarSQLInfo by curve type. */
    private static final Map<String, ErrorBarSQLInfo> EB_SQL_INFO_MAP =
            new HashMap<String, ErrorBarSQLInfo>() {
        {
            put(LearningCurveOptions.ERROR_RATE, new ErrorBarSQLInfo(
                    "sr.error_rate", "stdDevErrorRate", "stdErrErrorRate"));
            put(LearningCurveOptions.ASSISTANCE_SCORE, new ErrorBarSQLInfo(
                    "sr.total_incorrects + sr.total_hints",
                    "stdDevAssistanceScore", "stdErrAssistanceScore"));
            put(LearningCurveOptions.NUMBER_OF_INCORRECTS, new ErrorBarSQLInfo(
                    "sr.total_incorrects", "stdDevIncorrects", "stdErrIncorrects"));
            put(LearningCurveOptions.NUMBER_OF_HINTS, new ErrorBarSQLInfo(
                    "sr.total_hints", "stdDevHints", "stdErrHints"));
            put(LearningCurveOptions.STEP_DURATION, new ErrorBarSQLInfo(
                    "sr.step_duration", "stdDevStepDuration", "stdErrStepDuration"));
            put(LearningCurveOptions.CORRECT_STEP_DURATION,
                    new ErrorBarSQLInfo("sr.correct_step_duration",
                            "stdDevCorrectStepDuration", "stdErrCorrectStepDuration"));
            put(LearningCurveOptions.ERROR_STEP_DURATION, new ErrorBarSQLInfo(
                    "sr.error_step_duration", "stdDevErrorStepDuration",
                    "stdErrErrorStepDuration"));
        }
    };

    /**
     * Helper method to build learning curve queries.  Return appropriate SQL to
     * query standard deviation or standard error, based on the specified errorBarType.
     * @param errorBarType the error bar type, not null
     * @param viewSecondary flag indicating whether or not secondary model to be viewed
     * @return the appropriate SQL.
     */
    private String addErrorBarSQL(String errorBarType, boolean viewSecondary) {
        StringBuffer result = new StringBuffer();
        for (ErrorBarSQLInfo ebsi : EB_SQL_INFO_MAP.values()) {
            result.append(getErrorBarSqlForType(ebsi, errorBarType, viewSecondary));
        }
        return result.toString();
    }

    /**
     * Helper method to build learning curve query specific to curve type, error bar type and
     * whether or not user has specified a secondary skill model.
     * @param ebsi an ErrorBarSQLInfo object with relevant query strings.
     * @param errorBarType the error bar type, not null.
     * @param viewSecondary flag indicating whether or not query is for secondary skill model
     * @return the appropriate SQL.
     */
    private String getErrorBarSqlForType(ErrorBarSQLInfo ebsi, String errorBarType,
            boolean viewSecondary) {
        StringBuffer result = new StringBuffer(", ");
        result.append("(STDDEV_SAMP(");
        if (!viewSecondary) {
            result.append(ebsi.getDbColumn());
        } else {
            result.append("IF(sr2.skill_model_id = :secondaryModelId, NULL, ");
            result.append(ebsi.getDbColumn());
            result.append(")");
        }

        if (errorBarType.equals(ERROR_BAR_TYPE_SD)) {
            result.append(")) as ");
            result.append(ebsi.getSdVarName());
        } else {
            // If viewing secondary, the 'count' is doubled... adjust for that.
            String countStr = viewSecondary ? "count(*)/2" : "count(*)";

            result.append(") / SQRT(");
            result.append(countStr);
            result.append(")) as ");
            result.append(ebsi.getSeVarName());
        }
        return result.toString();
    }

    /**
     * Frequency counts are incorrect for correct and error step duration LCPID if
     * we do not limit the query based on step_rollup.first_attempt.  This method
     * examines the selectedType and returns appropriate SQL if the type is a correct
     * or error step duration graph.
     * @param selectedType the selected type of LC graph.
     * @return null if selectedType is not correct or error step duration, additional
     * WHERE clause SQL if true.
     */
    private String examineFirstAttempt(String selectedType) {
        if (selectedType.equals(LearningCurveOptions.CORRECT_STEP_DURATION)) {
            return (" AND sr.first_attempt = '2' ");
        } else if (selectedType.equals(LearningCurveOptions.ERROR_STEP_DURATION)) {
            return (" AND sr.first_attempt != '2' ");
        } else { return null; }
    }

    /**
     * Helper method to build learning curve queries.  Returns a formatted
     * GROUP BY clause dependent upon the type of query requested.
     * @param isViewBySkill if the curve type is skill or not.
     * @return the appropriate SQL group by clause.
     */
    private String addGroupByClause(boolean isViewBySkill) {
        return addGroupByClause(isViewBySkill, null);
    }

    /**
     * Helper method to build learning curve queries.  Returns a formatted
     * GROUP BY clause dependent upon the type of query requested.
     * @param isViewBySkill if the curve type is skill or not.
     * @param measure the measure the user wishes to view (student, skill, step or problem details).
     * @return the appropriate SQL group by clause.
     */
    private String addGroupByClause(boolean isViewBySkill, String measure) {
        StringBuffer sql = new StringBuffer();
        if (measure == null) {
            if (isViewBySkill) {
                sql.append(GET_LC_BY_SKILL_MY_SQL_GROUP_BY);
            } else {
                sql.append(GET_LC_BY_STUDENT_MY_SQL_GROUP_BY);
            }
        } else {
            sql.append(" GROUP BY " + MEASURE_ID);
        }
        return sql.toString();
    } // end addGroupByClause()

    /**
     * Get the LC point info details for a selected point and measure.
     *
     * Note (07-25-17): Doesn't look like this method is being used any longer, which
     * means that buildLCPIDQuery isn't either. (ctipper)
     *
     * @param oppNum the opportunity number for the selected LC point.
     * @param reportOptions the learning curve report options (selections made by user).
     * @param measure the selected measure for the point info details.
     * @param sortBy how the results should be sorted (by measure or curveTypeValue).
     * @param sortDirection the direction in which the results should be sorted (ASC or DESC).
     * @return a nice DTO object.
     */
    public LearningCurvePointInfoDetails getLCPointInfoDetails(
            int oppNum, LearningCurveOptions reportOptions, String measure, String sortBy,
            String sortDirection) {

        Double stdDevCutoff = reportOptions.getStdDeviationCutOff();
        Integer oppCutoffMin = reportOptions.getOpportunityCutOffMin();
        Integer oppCutoffMax = reportOptions.getOpportunityCutOffMax();
        Map<Long, Map<Integer, StdDevInfo>> stdDevInfoMap = null;
        StdDevInfo rollupStdDevInfo = null;

        if (sortBy == null) { sortBy = CURVE_TYPE_VALUE; }
        if (sortDirection == null) { sortDirection = DEFAULT_SORT_DIRECTION; }
        if (reportOptions.isLatencyCurve() && stdDevCutoff != null && stdDevCutoff > 0) {
            stdDevInfoMap = getStandardDeviationInfo(reportOptions);
            Map<Integer, StdDevInfo> rollupMap = stdDevInfoMap.get(Long.valueOf(-1));
            if (rollupMap != null) { rollupStdDevInfo = rollupMap.get(Integer.valueOf(-1)); }
        }

        String query = buildLCPIDQuery(oppNum, reportOptions, measure, sortBy,
                sortDirection, rollupStdDevInfo);
        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query);

        // set the sample and skill_model identifiers
        sqlQuery.setInteger("sampleId", (Integer)reportOptions.getSampleItem().getId());
        sqlQuery.setLong("modelId", (Long)reportOptions.getPrimaryModel().getId());
        if (oppCutoffMin != null) { sqlQuery.setInteger("oppMin", oppCutoffMin); }
        if (oppCutoffMax != null) { sqlQuery.setInteger("oppMax", oppCutoffMax); }
        logTrace(sqlQuery.toString());

        sqlQuery.addScalar(MEASURE_ID, LONG)
        .addScalar(MEASURE_NAME, Hibernate.STRING)
        .addScalar("problemId", LONG)
        .addScalar("frequency", LONG)
        .addScalar(CURVE_TYPE_VALUE, DOUBLE);

        sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        List<Map<String, Object>> results = sqlQuery.list();

        releaseSession(session);

        LearningCurvePointInfoDetails lcpid = new LearningCurvePointInfoDetails();
        lcpid.setSelectedMeasure(measure);
        lcpid.setCurveType(reportOptions.getSelectedMeasure());
        lcpid.setSortBy(sortBy);
        lcpid.setSortDirection(sortDirection);

        List problemIds = new ArrayList();
        // we only want to send back the list of problem IDs if the selected measure
        // was step or problem (the IDs are used to create the tool tips).
        boolean includeProblemIds = false;
        if (measure.equals("problem") || measure.equals("step")) {
            includeProblemIds = true;
        }

        int nvpCount = 0;
        for (final Map<String, Object> resultRow : results) {
            final Long measureId = (Long)resultRow.get(MEASURE_ID);
            final String measureName = (String)resultRow.get(MEASURE_NAME);
            final Long problemId = (Long)resultRow.get("problemId");
            final Long frequency = (Long)resultRow.get("frequency");
            Double curveTypeValue = (Double)resultRow.get(CURVE_TYPE_VALUE);

            if (curveTypeValue != null) {
                // latency values come back as milliseconds, so convert to seconds
                // before adding to the object.
                if (lcpid.getCurveType().equals(CORRECT_STEP_DURATION)
                        || lcpid.getCurveType().equals(STEP_DURATION)
                        || lcpid.getCurveType().equals(ERROR_STEP_DURATION)) {
                    curveTypeValue /= MAGIC_1000;
                }

                NameValuePair nvp = new NameValuePair(measureName, curveTypeValue, frequency);
                if ((measure.equals("step")) || (measure.equals("problem"))) {
                    Long modelId = (Long)reportOptions.getPrimaryModel().getId();
                    String kcs = stepRollupDao.getKCsForTooltip(measureId, modelId,
                                                                measure.equals("problem"));
                    if (kcs != null) {
                        nvp.setKCs(kcs);
                    }
                    
                    if (reportOptions.getSecondaryModel() != null) {
                        Long secondaryModelId = (Long)reportOptions.getSecondaryModel().getId();
                        if (secondaryModelId != null) {
                            String secondaryKcs =
                                stepRollupDao.getKCsForTooltip(measureId, secondaryModelId,
                                                               measure.equals("problem"));
                            if (secondaryKcs != null) {
                                nvp.setSecondaryKCs(secondaryKcs);
                            }
                        }
                    }
                }
                if (includeProblemIds) {
                    nvp.setProblemId(problemId);
                    problemIds.add(problemId);
                }
                lcpid.addNameValuePair(measureId, nvp);
                nvpCount++;
            }
        } // end for
        if (includeProblemIds) {
            lcpid.setProblemIds(problemIds);
        }

        logDebug("Added " + nvpCount + " name/value pair(s) to lcpid object.");

        return lcpid;
    } // end getLCPointInfoDetails()

    /**
     * Build the SQL String for getting a Learning Curve given a set of options.
     * @param reportOptions the set of report options.
     * @param stdDevInfo The standard deviation information (can be null)
     * @return String of native MySQL SQL.
     */
    private String buildLearningCurveQuery(LearningCurveOptions reportOptions,
            StdDevInfo stdDevInfo) {

        List skillList = reportOptions.getSkillList();
        List studentList = reportOptions.getStudentList();
        boolean isViewBySkill = reportOptions.isViewBySkill();
        String selectedType = reportOptions.getSelectedMeasure();
        Integer opportunityCutOffMin = reportOptions.getOpportunityCutOffMin();
        Integer opportunityCutOffMax = reportOptions.getOpportunityCutOffMax();
        SkillModelItem secondaryModel = reportOptions.getSecondaryModel();
        String errorBarType = reportOptions.getErrorBarType();
        StringBuffer query = new StringBuffer();

        boolean viewSecondary = (secondaryModel != null
                && secondaryModel.getId() != null && !reportOptions.isLatencyCurve());

        // build the query
        //query.append(addTypeID(viewBy));
        query.append(addTypeID(isViewBySkill));
        if (viewSecondary) {
            query.append(GET_LC_MY_SQL_PART_2_WITH_SECONDARY);
        } else {
            query.append(GET_LC_MY_SQL_PART_2);
        }
        if (errorBarType != null) {
            query.append(addErrorBarSQL(errorBarType, viewSecondary));
        }
        query.append(FROM_STEP_ROLLUP);
        if (viewSecondary) {
            query.append(GET_LC_MY_SQL_PART_3_WITH_SECONDARY);
        }
        if (opportunityCutOffMin != null) {
            query.append(LIMIT_BY_OPP_CUTTOFF_MIN_MYSQL);
        }

	// Add WHERE clause
        query.append(addWhereClause());

	// If viewing lowStakes curve (e.g., OLI) then we want the normal (red)
	// error_rate curve to be lowStakes only.
	if (reportOptions.getDisplayLowStakesCurve() && selectedType.equals(LearningCurveOptions.ERROR_RATE)) {
	    query.append(" AND sro.step_rollup_id IS NULL ");
	}

        // add on the standard deviation cutoff SQL if selected type is a latency curve.
        if (stdDevInfo != null) {
            query.append(addStdDevCutoffSQL(selectedType, stdDevInfo, reportOptions));
        }
        // add skill and/or student identifiers
        if (skillList != null) {
            query.append(addSkillsOrStudentsToWhereClause(skillList, "skill"));
        }
        if (studentList != null) {
            query.append(addSkillsOrStudentsToWhereClause(studentList, "student"));
        }
        if (opportunityCutOffMin != null) { query.append(" and sm.oppMax >= :oppMin "); }
        if (opportunityCutOffMax != null) { query.append(LIMIT_BY_OPP_CUTTOFF_MAX_MYSQL); }

        //query.append(addGroupByClause(viewBy));
        query.append(addGroupByClause(isViewBySkill));
        query.append(WITH_ROLLUP);

        String measure = reportOptions.isLatencyCurve() ? reportOptions.getSelectedMeasureType()
                : "''";

        // need to specify the measure to get the correct student/step/problem/skill counts
	String queryStr = query.toString().replace(TO_REPLACE_STRING, measure);

	if (reportOptions.getDisplayLowStakesCurve() && selectedType.equals(LearningCurveOptions.ERROR_RATE)) {
	    // add the 'JOIN step_rollup_oli' bit
	    queryStr = queryStr.replace("JOIN_SRO", "LEFT " + JOIN_STEP_ROLLUP_OLI);
	} else {
	    queryStr = queryStr.replace("JOIN_SRO", "");
	}

	return queryStr;
    }

    /** Index of the standard deviation query results. */
    private static final int STD_DEV_TYPE_INDEX = 0;
    /** Index of the standard deviation query results. */
    private static final int STD_DEV_OPP_INDEX = 1;
    /** Index of the standard deviation results for step duration. */
    private static final int STD_DEV_STEP_DURATION_INDEX = 2;
    /** Index of the standard deviation results for correct step duration. */
    private static final int STD_DEV_CORRECT_STEP_DURATION_INDEX = 3;
    /** Index of the standard deviation results for error step duration.*/
    private static final int STD_DEV_ERROR_STEP_DURATION_INDEX = 4;
    /** Index of the step duration observations within the standard deviation query results. */
    private static final int STD_DEV_STEP_DURATION_OBS_INDEX = 5;
    /** Index of the correct step duration observations
     *  within the standard deviation query results. */
    private static final int STD_DEV_CORRECT_STEP_DURATION_OBS_INDEX = 6;
    /** Index of the error step duration observations
     *  within the standard deviation query results. */
    private static final int STD_DEV_ERROR_STEP_DURATION_OBS_INDEX = 7;


    /**
     * Get the standard deviation information for a given sample and model.
     * @param options DTO of the report options.
     * @return a HashMap that is keyed by the typeId and has a map of opportunity -> StdDevInfo
     * as the value.
     */
    private Map<Long, Map<Integer, StdDevInfo>> getStandardDeviationInfo(
            LearningCurveOptions options) {

        boolean isViewBySkill = options.isViewBySkill();
        SampleItem sample = options.getSampleItem();
        SkillModelItem primaryModel = options.getPrimaryModel();
        Integer opportunityCutOffMax = options.getOpportunityCutOffMax();
        Integer opportunityCutOffMin = options.getOpportunityCutOffMin();
        List skillList = options.getSkillList();
        List studentList = options.getStudentList();

        int queryCounter = 0;

        // Create a map to hold typeIndex (-1 for rollup, otherwise the skill or
        // student_id), coupled with standard deviation information for each
        // opportunity.
        Map <Long, Map<Integer, StdDevInfo>> infoMapByTypeIndex =
            new HashMap<Long, Map<Integer, StdDevInfo>>();

        /**
         * We need to run a similar query twice - first to get the rollup standard deviation
         * values across all skills in the skill model and second, to get values
         * for only those skills selected by the user.  First time through the loop
         * calculate the rollup for all skills and store only that value in the
         * infoMapByTypeIndex.  Second time around, store all values BUT the rollup
         * (since we already have it).
         */
        while (queryCounter < 2) {
            StringBuffer query = new StringBuffer(
                    " sr.opportunity as opp,"
                    + " STD(sr.step_duration) as stepDuration,"
                    + " STD(sr.correct_step_duration) as correctStepDuration,"
                    + " STD(sr.error_step_duration) as errorStepDuration,"
                    + " SUM(IF(sr.step_duration IS NULL, 0, 1)) as stepDurationObs,"
                    + " SUM(IF(sr.correct_step_duration IS NULL, 0, 1)) as correctStepDurationObs,"
                    + " SUM(IF(sr.error_step_duration IS NULL, 0, 1)) as errorStepDurationObs"
                    + "  FROM step_rollup sr ");

            if (opportunityCutOffMin != null) { query.append(LIMIT_BY_OPP_CUTTOFF_MIN_MYSQL); }

            query.append(" WHERE sr.sample_id = :sampleId"
                    + "   AND sr.skill_model_id = :modelId"
                    + "   AND sr.skill_id IS NOT NULL");

            if (opportunityCutOffMin != null) { query.append(" and sm.oppMax >= :oppMin "); }
            if (opportunityCutOffMax != null) { query.append(LIMIT_BY_OPP_CUTTOFF_MAX_MYSQL); }

            if (skillList != null) {
                query.append(" and sr.skill_id in (");
                if (queryCounter == 0) {
                    List<SkillItem> allSkillsInModel =
                        DaoFactory.DEFAULT.getSkillDao().find(primaryModel);
                    for (Iterator it = allSkillsInModel.iterator(); it.hasNext();) {
                        SkillItem item = (SkillItem)it.next();
                        query.append(item.getId());
                        if (it.hasNext()) { query.append(", "); }
                    }
                } else {
                    for (Iterator it = skillList.iterator(); it.hasNext();) {
                        SkillItem item = (SkillItem)it.next();
                        query.append(item.getId());
                        if (it.hasNext()) { query.append(", "); }
                    }
                }
                query.append(")");
            }

            if (studentList != null) {
                query.append(" and sr.student_id in (");
                for (Iterator it = studentList.iterator(); it.hasNext();) {
                    StudentItem item = (StudentItem)it.next();
                    query.append(item.getId());
                    if (it.hasNext()) { query.append(", "); }
                }
                query.append(")");
            }

            query.append(" group by sr.opportunity, ");
            if (isViewBySkill) {
                query.insert(0, "select sr.skill_id as typeId, ");
                query.append("sr.skill_id with rollup");
            } else {
                query.insert(0, "select sr.student_id as typeId, ");
                query.append("sr.student_id with rollup");
            }

            Session session = getSession();
            SQLQuery sdQuery = session.createSQLQuery(query.toString());
            addScalars(sdQuery, "typeId", LONG, "opp", INTEGER, "stepDuration", DOUBLE,
                    "correctStepDuration", DOUBLE, "errorStepDuration", DOUBLE,
                    "stepDurationObs", INTEGER, "correctStepDurationObs", INTEGER,
                    "errorStepDurationObs", INTEGER);

            sdQuery.setInteger("sampleId", (Integer)sample.getId());
            sdQuery.setLong("modelId", (Long)primaryModel.getId());
            if (opportunityCutOffMin != null) {
                sdQuery.setInteger("oppMin", opportunityCutOffMin);
            }
            if (opportunityCutOffMax != null) {
                sdQuery.setInteger("oppMax", opportunityCutOffMax);
            }

            logTrace("STDDEV Query: ", query);
            List<Object[]> results = sdQuery.list();
            releaseSession(session);

            Map<Integer, StdDevInfo> typeInfoMap = null;

            processResults:
            for (Object[] row : results) {
                Long typeId = (Long)row[STD_DEV_TYPE_INDEX];
                typeId = (typeId == null) ? Long.valueOf(-1) : typeId;
                if (queryCounter == 0) {
                    if ((Integer)row[STD_DEV_OPP_INDEX] != null) {
                        continue;
                    }
                } else if (typeId == -1 && (Integer)row[STD_DEV_OPP_INDEX] == null) {
                    // second time through do not process entire rollup value.
                    break processResults;
                }
                typeInfoMap = infoMapByTypeIndex.get(typeId);
                typeInfoMap = (typeInfoMap != null)
                                ? typeInfoMap : new HashMap<Integer, StdDevInfo>();

                StdDevInfo info = new StdDevInfo();
                info.setOpportunity((Integer)row[STD_DEV_OPP_INDEX]);
                info.setStepDurationStdDev((Double)row[STD_DEV_STEP_DURATION_INDEX]);
                info.setCorrectStepDurationStdDev((Double)row[STD_DEV_CORRECT_STEP_DURATION_INDEX]);
                info.setErrorStepDurationStdDev((Double)row[STD_DEV_ERROR_STEP_DURATION_INDEX]);
                info.setStepDurationObs((Integer)row[STD_DEV_STEP_DURATION_OBS_INDEX]);
                info.setCorrectStepDurationObs(
                        (Integer)row[STD_DEV_CORRECT_STEP_DURATION_OBS_INDEX]);
                info.setErrorStepDurationObs((Integer)row[STD_DEV_ERROR_STEP_DURATION_OBS_INDEX]);

                typeInfoMap.put(info.getOpportunity(), info);
                infoMapByTypeIndex.put(typeId, typeInfoMap);
            }
            queryCounter++;
        } // end while
        return infoMapByTypeIndex;
    }

    /**
     * Helper class for storing standard deviation information for a given opportunity.
     */
    class StdDevInfo {
        /** The standard deviation of the step duration for the opportunity. */
        private Double stepDurationStdDev;
        /** The standard deviation of the correct step duration for the opportunity. */
        private Double correctStepDurationStdDev;
        /** The standard deviation of the error step duration for the opportunity. */
        private Double errorStepDurationStdDev;
        /** The number of step duration observations. */
        private Integer stepDurationObs;
        /** The number of correct step duration observations. */
        private Integer correctStepDurationObs;
        /** The number of error step duration observations. */
        private Integer errorStepDurationObs;

        /**
         * The opportunity number
         * (-1 if it's the standard deviations for across all opportunities.
         */
        private Integer opportunity;

        /** Default Constructor */
        StdDevInfo() { }

        /** Returns stepDurationStdDev. @return Returns the stepDurationStdDev. */
        public Double getStepDurationStdDev() {
            return stepDurationStdDev;
        }

        /** Returns correctStepDurationStdDev. @return Returns the correctStepDurationStdDev. */
        public Double getCorrectStepDurationStdDev() {
            return correctStepDurationStdDev;
        }

        /** Returns errorStepDurationStdDev. @return Returns the errorStepDurationStdDev. */
        public Double getErrorStepDurationStdDev() {
            return errorStepDurationStdDev;
        }

        /** Returns stepDurationObs.
         * @return Returns the stepDurationObs. */
        public Integer getStepDurationObs() {
            return stepDurationObs;
        }

        /** Returns correctStepDurationObs.
         * @return Returns the correctStepDurationObs. */
        public Integer getCorrectStepDurationObs() {
            return correctStepDurationObs;
        }

        /**
         * Returns errorStepDurationObs.
         * @return Returns the errorStepDurationObs.
         */
        public Integer getErrorStepDurationObs() {
            return errorStepDurationObs;
        }

        /** Returns opportunity. @return Returns the opportunity. */
        public Integer getOpportunity() {
            return opportunity;
        }

        /** Set stepDurationStdDev. @param stepDurationStdDev The stepDurationStdDev to set. */
        public void setStepDurationStdDev(Double stepDurationStdDev) {
            this.stepDurationStdDev = stepDurationStdDev;
        }

        /** Set correctStepDurationStdDev.
         * @param correctStepDurationStdDev The correctStepDurationStdDev to set.
         */
        public void setCorrectStepDurationStdDev(Double correctStepDurationStdDev) {
            this.correctStepDurationStdDev = correctStepDurationStdDev;
        }

        /** Set errorStepDurationStdDev.
         * @param errorStepDurationStdDev The error step duration standard deviation
         */
        public void setErrorStepDurationStdDev(Double errorStepDurationStdDev) {
            this.errorStepDurationStdDev = errorStepDurationStdDev;
        }

        /** Set opportunity. @param opportunity The opportunity to set. */
        public void setOpportunity(Integer opportunity) {
            this.opportunity = (opportunity == null) ? -1 : opportunity;
        }

        /** Set the stepDurationObs.
         * @param observations The stepDurationObs to set. */
        public void setStepDurationObs(Integer observations) {
            this.stepDurationObs = observations;
        }

        /** Set the correctStepDurationObs.
         * @param observations The correctStepDurationObs to set. */
        public void setCorrectStepDurationObs(Integer observations) {
            this.correctStepDurationObs = observations;
        }

        /** Set the errorStepDurationObs.
         * @param observations The stepDurationObs to set. */
        public void setErrorStepDurationObs(Integer observations) {
            this.errorStepDurationObs = observations;
        }

        /** Return a nice string representation.
         * @return a string representation of the object.
         *  */
        public String toString() {
            StringBuffer buffer = new StringBuffer();

            buffer.append(getClass().getName());
            buffer.append("[");
            buffer.append(" opp::" + this.getOpportunity());
            buffer.append(" stepDurationStdDev::" + this.getStepDurationStdDev());
            buffer.append(" stepDurationObs::" + this.getStepDurationObs());
            buffer.append(" correctStepDurationStdDev::" + this.getCorrectStepDurationStdDev());
            buffer.append(" correctStepDurationObs::" + this.getCorrectStepDurationObs());
            buffer.append(" errorStepDurationStdDev::" + this.getErrorStepDurationStdDev());
            buffer.append(" errorStepDurationObs::" + this.getErrorStepDurationObs());
            buffer.append("]");

            return buffer.toString();

        }
    } // end class StdDevInfo

    /** Index of the standard deviation query results. */
    private static final int MAX_OPP_COUNT_TYPE_INDEX = 0;
    /** Index of the standard deviation query results. */
    private static final int MAX_OPP_COUNT_VALUE_INDEX = 1;

    /**
     * Get the maximum number of opportunities for a learning curve.  Used when drawing
     * latency curves (correct_step_time and assistance_time), since we may not get points
     * for all opportunities.
     * @param reportOptions a helper class containing the learning curve report options.
     * @return an Integer holding the maximum number of opportunities.
     */
    public Map getMaxOpportunityCount(LearningCurveOptions reportOptions) {
        SampleItem sample = reportOptions.getSampleItem();
        boolean isViewBySkill = reportOptions.isViewBySkill();
        List skillList = reportOptions.getSkillList();
        List studentList = reportOptions.getStudentList();
        StringBuffer queryString = new StringBuffer();

        if (isViewBySkill) {
            queryString.append(GET_LC_BY_SKILL_MY_SQL_PART_1);
        } else {
            queryString.append(GET_LC_BY_STUDENT_MY_SQL_PART_1);
        }

        queryString.append(GET_MAX_OPP_COUNT_MYSQL);

        if (skillList != null) {
            queryString.append(" and sr.skill_id in (");
            for (Iterator it = skillList.iterator(); it.hasNext();) {
                SkillItem item = (SkillItem)it.next();
                queryString.append(item.getId());
                if (it.hasNext()) { queryString.append(", "); }
            }
            queryString.append(")");
        }

        if (studentList != null) {
            queryString.append(" and sr.student_id in (");
            for (Iterator it = studentList.iterator(); it.hasNext();) {
                StudentItem item = (StudentItem)it.next();
                queryString.append(item.getId());
                if (it.hasNext()) { queryString.append(", "); }
            }
            queryString.append(")");
        }

        if (isViewBySkill) {
            queryString.append(GET_MAX_OPP_COUNT_BY_SKILL_MYSQL);
        } else {
            queryString.append(GET_MAX_OPP_COUNT_BY_STUDENT_MYSQL);
        }

        Session session = getSession();

        SQLQuery sqlQuery = session.createSQLQuery(queryString.toString());

        sqlQuery.addScalar("typeId", LONG);
        sqlQuery.setInteger("sampleId", ((Integer)sample.getId()).intValue());
        sqlQuery.addScalar("maxOpp", INTEGER);

        logTrace(sqlQuery);

        List <Object[]> results = sqlQuery.list();
        releaseSession(session);

        HashMap <Long, Integer> typeIdMaxOppCountMap = new HashMap();
        for (Object[] row : results) {
            Long typeId = (Long)row[MAX_OPP_COUNT_TYPE_INDEX];
            typeId = (typeId == null) ? Long.valueOf(-1) : typeId;
            Integer maxOppCount = (Integer)row[MAX_OPP_COUNT_VALUE_INDEX];
            typeIdMaxOppCountMap.put(typeId, maxOppCount);
        }

        logDebug("returning typeIdOppCountMap of size :: ", typeIdMaxOppCountMap.size());

        return typeIdMaxOppCountMap;
    }
} // end LearningCurveDaoHibernate.java