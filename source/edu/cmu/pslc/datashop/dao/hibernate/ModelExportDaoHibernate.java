/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ModelExportDao;
import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * Hibernate implementation of the ModelExportDao.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15774 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-12-18 08:40:03 -0500 (Tue, 18 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ModelExportDaoHibernate extends HibernateDaoSupport implements ModelExportDao {


    /** Query to get the information necessary for a step-to-skill export. */
    private static String getStepDataQuery =

        //get the unique IDs of the step
          "SELECT sr.step_id as stepId, "
        + "sub.guid as stepGuid, "

        //get the location of the step in the tutor via the problem, hierarchy etc.
        + "(select group_concat(DISTINCT CONCAT( "
        + "     IF(dl2.level_title IS NOT NULL, CONCAT('(', dl2.level_title, ') '), ''), "
        + "     dl2.level_name) ORDER BY dl2.lft SEPARATOR ', ') "
        + "   FROM dataset_level dl2 "
        + "   WHERE dl2.lft <= dl.lft AND dl2.rgt >= dl.rgt "
        + "   AND dl.dataset_id = dl2.dataset_id "
        + "   GROUP BY dl2.dataset_id) as probHierarchy, "
        + "prob.problem_name as probName, "
        + "sub.subgoal_name as stepName, "

        //get the average totals
        + "MAX(sr.problem_view) as maxProblemView, "
        + "AVG(sr.total_incorrects) as avgIncorrects, "
        + "AVG(sr.total_hints) as avgHints, "
        + "AVG(sr.total_corrects) as avgCorrects, "

        //get the first attempt averages
        + "((SUM(if(sr.first_attempt='0',1,0))) / count(*)) * 100 as pctIncorrectFirstAttempts, "
        + "((SUM(if(sr.first_attempt='1',1,0))) / count(*)) * 100 as pctHintFirstAttempts, "
        + "((SUM(if(sr.first_attempt='2',1,0))) / count(*)) * 100 as pctCorrectFirstAttempts, "

        //get the average latencies
        + "AVG(step_duration) as avgStepDuration, "
        + "AVG(correct_step_duration)as avgCorrectStepDuration, "
        + "AVG(error_step_duration) as avgErrorStepDuration, "

        //get the extra totals
        + "COUNT(distinct sr.student_id) as totalStudents, "
        + "COUNT(sr.step_rollup_id) as totalOpportunities "

        //from and join portions of the query.
        + "FROM step_rollup sr "
        + "  JOIN subgoal sub on sr.step_id = sub.subgoal_id "
        + "  JOIN problem prob on sr.problem_id = prob.problem_id "
        + "  JOIN dataset_level dl on prob.dataset_level_id = dl.dataset_level_id "
        + "WHERE sr.sample_id = :sampleId "

        //this sub-select returns a single step rollup for each student/step/sample/problem_view
        //without this you get the same data for each skill (across all models) which screws
        //up all the averages above.
        + "AND sr.step_rollup_id in ("
        + "    SELECT MIN(sr2.step_rollup_id) "
        + "     FROM step_rollup sr2 "
        + "     WHERE sr2.step_id = sr.step_id "
        + "     AND sr2.sample_id = sr.sample_id "
        + "     AND sr2.student_id = sr.student_id "
        + "     GROUP BY sr2.student_id, sr2.problem_view) "
        + "GROUP BY sr.step_id "
        + "ORDER BY probHierarchy, probName, stepName, stepGuid "
        + "LIMIT :limit OFFSET :offset ";

    /** The initial next ordinal value. */
    private static int nextIndex = 0;

    /** Query results row index. */
    private static final int STEP_ID_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int STEP_GUID_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int PROB_HIERARCHY_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int PROB_NAME_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int STEP_NAME_INDEX = nextIndex++;

    /** Query results row index. */
    private static final int MAX_PROB_VIEW_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int AVG_INCORRECTS_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int AVG_HINTS_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int AVG_CORRECTS_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int PCT_INCORRECTS_FA_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int PCT_HINTS_FA_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int PCT_CORRECTS_FA_INDEX = nextIndex++;

    /** Query results row index. */
    private static final int AVG_STEP_DURATION = nextIndex++;
    /** Query results row index. */
    private static final int AVG_CORRECT_STEP_DURATION = nextIndex++;
    /** Row index for Error Step Duration. */
    private static final int AVG_ERROR_STEP_DURATION = nextIndex++;

    /** Query results row index. */
    private static final int TOTAL_STUDS_INDEX = nextIndex++;
    /** Query results row index. */
    private static final int TOTAL_OPPS_INDEX = nextIndex++;

    /**
     * Get a back a list of the step export data for a given dataset.
     * @param dataset dataset we are exporting from
     * @param limit the number of records to return
     * @param offset the offset of the first record to return
     * @return List of StepExportRows
     */
    public List <StepExportRow> getStepExport(DatasetItem dataset, Integer limit,
            Integer offset) {

        SampleItem allData = DaoFactory.DEFAULT.getSampleDao().findOrCreateDefaultSample(dataset);

        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(getStepDataQuery);

        //indicate which columns map to which types.
        sqlQuery.addScalar("stepId", Hibernate.LONG);
        sqlQuery.addScalar("stepGuid", Hibernate.STRING);
        sqlQuery.addScalar("probHierarchy", Hibernate.STRING);
        sqlQuery.addScalar("probName", Hibernate.STRING);
        sqlQuery.addScalar("stepName", Hibernate.STRING);
        sqlQuery.addScalar("maxProblemView", Hibernate.DOUBLE);
        sqlQuery.addScalar("avgIncorrects", Hibernate.DOUBLE);
        sqlQuery.addScalar("avgHints", Hibernate.DOUBLE);
        sqlQuery.addScalar("avgCorrects", Hibernate.DOUBLE);
        sqlQuery.addScalar("pctIncorrectFirstAttempts", Hibernate.DOUBLE);
        sqlQuery.addScalar("pctHintFirstAttempts", Hibernate.DOUBLE);
        sqlQuery.addScalar("pctCorrectFirstAttempts", Hibernate.DOUBLE);
        sqlQuery.addScalar("avgStepDuration", Hibernate.DOUBLE);
        sqlQuery.addScalar("avgCorrectStepDuration", Hibernate.DOUBLE);
        sqlQuery.addScalar("avgErrorStepDuration", Hibernate.DOUBLE);
        sqlQuery.addScalar("totalStudents", Hibernate.INTEGER);
        sqlQuery.addScalar("totalOpportunities", Hibernate.INTEGER);

        //set the parameters
        sqlQuery.setInteger("sampleId", ((Integer)allData.getId()));
        sqlQuery.setInteger("limit", limit);
        sqlQuery.setInteger("offset", offset);

        //query the database
        List <Object[]> queryResults = sqlQuery.list();
        releaseSession(session);

        //parse the results
        List <StepExportRow> steps = new ArrayList <StepExportRow> ();
        for (Object[] row : queryResults) {
            StepExportRow rowDTO = new StepExportRow();
            rowDTO.setStepId((Long)row[STEP_ID_INDEX]);
            rowDTO.setStepGuid((String)row[STEP_GUID_INDEX]);
            rowDTO.setProblemHierarchy((String)row[PROB_HIERARCHY_INDEX]);
            rowDTO.setProblemName((String)row[PROB_NAME_INDEX]);
            rowDTO.setStepName((String)row[STEP_NAME_INDEX]);
            rowDTO.setMaxProblemView((Double)row[MAX_PROB_VIEW_INDEX]);

            rowDTO.setAvgIncorrects((Double)row[AVG_INCORRECTS_INDEX]);
            rowDTO.setAvgHints((Double)row[AVG_HINTS_INDEX]);
            rowDTO.setAvgCorrects((Double)row[AVG_CORRECTS_INDEX]);

            rowDTO.setPctIncorrectFirstAttempts((Double)row[PCT_INCORRECTS_FA_INDEX]);
            rowDTO.setPctHintFirstAttempts((Double)row[PCT_HINTS_FA_INDEX]);
            rowDTO.setPctCorrectFirstAttempts((Double)row[PCT_CORRECTS_FA_INDEX]);

            rowDTO.setAvgStepDuration((Double)row[AVG_STEP_DURATION]);
            rowDTO.setAvgCorrectStepDuration((Double)row[AVG_CORRECT_STEP_DURATION]);
            rowDTO.setAvgErrorStepDuration((Double)row[AVG_ERROR_STEP_DURATION]);

            rowDTO.setTotalStudents((Integer)row[TOTAL_STUDS_INDEX]);
            rowDTO.setTotalOpportunities((Integer)row[TOTAL_OPPS_INDEX]);
            steps.add(rowDTO);
        }

        return steps;
    }

    /**
     * Get a mapping of steps to skills given a list of skill models and a dataset.
     * @param dataset dataset to get a mapping for.
     * @param skillModelList list of skill models to get skill mappings for.
     * @return a MultiMap which is a key: stepId value:Collection of skill_ids mapped to that step.
     */
    public MultiMap getStepSkillMapping(DatasetItem dataset, List<SkillModelItem> skillModelList) {

        MultiMap stepSkillMap = new MultiValueMap();
        //if no skill models, return an empty map.
        if (skillModelList == null || skillModelList.size() == 0) { return stepSkillMap; }

        SampleItem allData = DaoFactory.DEFAULT.getSampleDao().findOrCreateDefaultSample(dataset);

        String query =
                "SELECT sr.step_id as stepId, sk.skill_id as skillId "
                + "FROM step_rollup sr "
                + "JOIN skill sk on sr.skill_id = sk.skill_id "
                + "WHERE sr.sample_id = :sampleId AND sr.skill_model_id "
                + AbstractDaoHibernate.inItemIds(skillModelList)
                + " GROUP BY sr.step_id, sr.skill_id";

        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query);

        //indicate which columns map to which types.
        sqlQuery.addScalar("stepId", Hibernate.LONG);
        sqlQuery.addScalar("skillId", Hibernate.LONG);

        //set the parameter(s)
        sqlQuery.setInteger("sampleId", ((Integer)allData.getId()));

        List <Object[]> results = sqlQuery.list();
        releaseSession(session);

        for (Object[] row : results) { stepSkillMap.put(row[0], row[1]); }

        return stepSkillMap;
    }
}
