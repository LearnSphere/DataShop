/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import edu.cmu.pslc.datashop.dto.StepRollupExportOptions;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StepRollupItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * StepRollup Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15747 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-12-11 09:46:48 -0500 (Tue, 11 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface StepRollupDao extends AbstractDao {
    /** The default number of sessions to aggregate in each batch. */
    int AGG_SESSION_BATCH_SIZE = 20000;

    /**
     * Standard get for a StepRollupItem by id.
     * @param id The id of the StepRollupItem.
     * @return the matching StepRollupItem or null if none found
     */
    StepRollupItem get(Long id);

    /**
     * Standard find for an StepRollupItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired StepRollupItem.
     * @return the matching StepRollupItem.
     */
    StepRollupItem find(Long id);

    /**
     * Standard "find all" for StepRollupItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Get all StepRollup Items inside of a given dataset.
     * @param datasetItem the dataset to get stepRollups for.
     * @return List of step rollup items.
     */
    List get(DatasetItem datasetItem);

    /**
     * Gets all unique Student Roll up Items in the sample.
     * @param sampleItem The sample to get all student roll ups for.
     * @return A list of student roll up items.
     */
    List getStepRollupList(SampleItem sampleItem);

    /**
     * Gets all unique Student Rollup Items in the sample.
     * @param sampleItem The sample to get all student roll ups for.
     * @param options Options for exporting.
     * @param limit A limit to the total number to return.
     * @param offset An offset for the start position of which set to return.
     * @return A list of student rollup items.
     */
    List getStepRollupList(SampleItem sampleItem,
            StepRollupExportOptions options,
            Integer limit,
            Integer offset);

    /**
     * Remove all step roll-ups inside the given dataset.
     * @param datasetItem the dataset to remove all StepRollups for.
     * @return number of step rollup items deleted.
     */
    int removeAll(DatasetItem datasetItem);

    /**
     * Remove all step roll-ups for the given sample.
     * @param sampleItem the sample to remove all StepRollups for.
     * @return number of step rollup items deleted.
     */
    int removeAll(SampleItem sampleItem);

    /**
     * Remove all step roll-ups for the given KC model.
     * @param modelItem the KC model to remove all StepRollups for.
     * @return number of step rollup items deleted.
     */
    int removeAll(SkillModelItem modelItem);

    /**
     * Gets the number of step roll-ups in a given sample given a set of export options.
     * @param sampleItem the sample to get step roll-ups for.
     * @param options the {@link StepRollupExportOptions}
     * @return an Integer of the number of step roll-ups.
     */
    Integer getNumStepRollups(SampleItem sampleItem, StepRollupExportOptions options);

    /**
     * Run a native MySQL query to get the number of observations from the step rollup
     * table for the all data sample of the given dataset and given skill model.
     * @param datasetItem the given dataset
     * @param skillModelItem the given skill model
     * @return the number of observations, or 0 if no results returned
     */
    Long getNumberObservations(DatasetItem datasetItem, SkillModelItem skillModelItem);

    /**
     * Run a native MySQL query to get the number of observations from the step rollup
     * table for the 'All Data' sample of the given dataset and skill.
     * @param datasetItem the given dataset
     * @param skillItem the given skill
     * @return the number of observations, or 0 if no results returned
     */
    Long getNumberObservations(DatasetItem datasetItem, SkillItem skillItem);

    /**
     * Run a native MySQL query to get the number of unique steps from the step rollup
     * table for the 'All Data' sample of the given dataset and skill.
     * @param datasetItem the given dataset
     * @param skillItem the given skill
     * @return the number of unique steps, or 0 if no results returned
     */
    Long getNumberUniqueSteps(DatasetItem datasetItem, SkillItem skillItem);

    /**
     * Return the SSSVS 2D array that contains studentID, success, step, problem view and skill.
     * Note that this is for the Learning Factors Analysis tool.
     * @param datasetItem the selected dataset
     * @param skillModelItem the selected skill model
     * @return String[][]
     */
    String[][] getSSSVS(DatasetItem datasetItem, SkillModelItem skillModelItem);

    /**
     * Call the stored procedure to calculate and store the new predicted error rate
     * for a given skill model.
     * This should be called after a successful call to LFA on the given skill model.
     * @param skillModelItem the skill model to be updated
     * @return the number of rows with a predicted error rate (i.e. not null)
     */
    int callLfaBackfillSP(SkillModelItem skillModelItem);

    /**
     * Call the stored procedure to calculate and store the new predicted error rate
     * for a given sample.
     * This should be called after a successful call to LFA on the given sample.
     * @param sampleItem the sample in the step rollup table to be updated
     * @param datasetItem the dataset item associated with the sample
     * @return the number of rows with a predicted error rate (i.e. not null)
     */
    int callLfaBackfillBySampleSP(final SampleItem sampleItem, final DatasetItem datasetItem);

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
    List find(Integer sampleId, Long studentId, Long stepId, Long skillId,
            Integer opportunity, Integer problemView);

    /**
     * Loads the customized (for sample name and id) aggregator_sp.sql file into the
     * database.  Since Hibernate is stupid, we have to execute the contents of the
     * stored procedure file as a list of individual queries.
     * @param aggSpFilePath The location of the aggregator stored procedure file.
     * @param toInsert the character sequence to insert into the stored procedure file.
     * @return true if statement execution was successful, false if an SQL exception
     *      was thrown.
     */
    boolean loadAggregatorStoredProcedure(String aggSpFilePath, String toInsert);

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
     Integer callAggregatorStoredProcedure(SampleItem sample, String storedProcSuffix,
            String skillModelSQLClause, int batchSize);

     /**
      * Call the set of aggregator stored procedures with the default batch size.
      * @param sample the sample to aggregate.
      * @param storedProcSuffix the unique suffix added to the stored procedure table names.
      * @param skillModelSQLClause the "in" clause to be used when aggregating.
      * @return the number of rows created in the step_rollup table.
      */
    Integer callAggregatorStoredProcedure(SampleItem sample, String storedProcSuffix,
                                          String skillModelSQLClause);

    /**
     * Call the set of aggregator stored procedures.
     * @param sample the sample to aggregate.
     * @param storedProcSuffix the unique suffix added to the stored procedure table names.
     * @return the number of rows created in the step_rollup table.
     */
    Integer callAggregatorStoredProcedure(SampleItem sample, String storedProcSuffix);

    /**
     * Call the aggregator stored procedure and run on the given sample.
     * @param sample the sample to aggregate.
     * @param batchSize the number of sessions to work with at a time in the stored procedure.
     * @return the number of rows created in the step_rollup table.
     */
    Integer callAggregatorStoredProcedure(SampleItem sample, int batchSize);

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
    boolean callSetStepRollupConditionsProcedure(SampleItem sample, Integer batchSize);

    /**
     * Drop aggregator temporary tables, stored procedures, and functions for the given sample.
     * @param sample the sample
     * @param toInsertBase indicates whether for sample, KC model, etc.
     */
    void dropAll(SampleItem sample, String toInsertBase);

    /**
     * Need to expose the "getSession" method for use with getSPTransactions.
     * @return a Session
     */
    Session session();

    /**
     * Need to expose the "releaseSession" method for use with getSPTransactions.
     * @param session the session to release
     */
    void release(Session session);

    /**
     * Step rollup headers for the given sample.
     * @param sample the sample
     * @return step rollup headers for the given sample
     */
    List<String> getStepRollupHeaders(final SampleItem sample);

    /**
     * Step rollup skill model names for the given sample.
     * @param sample the sample
     * @return step rollup skill model names for the given sample
     */
    List<String> getSkillModelNames(final SampleItem sample);

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
    ResultSet getStepRollupRows(final SampleItem sample, StepRollupExportOptions options,
        Session session, Integer limit, List<Long> studentIds)
    throws SQLException;

    /**
     * Map problem ids to their problem hierarchy string.
     * @param sample the sample
     * @return problem ids mapped to their problem hierarchy string
     */
    Map<Integer, String> getProblemHierarchy(final SampleItem sample);

    /**
     * Get the max number of skills across steps for the given sample and skill model.
     * @param sample the sample
     * @param skillModel the skill model
     * @return the max() number of skills across steps.
     */
    Long getMaxDistinctSkillsAcrossSteps(SampleItem sample, SkillModelItem skillModel);
    /**
     * Get the student batches for the step rollup.
     * @param sample the sample
     * @param batchSize the batch size
     * @return list of id's
     */
    Iterable<List<Long>> getStepRollupStudentBatches(final SampleItem sample, int batchSize);

    /**
     * Log the student-step export.
     * @param datasetItem the DatasetItem
     * @param userItem the UserItem or null to log with the SystemLogger
     * @param stepRollupExportOptions the StepRollupExportOptions
     * @param skillModelItem the SkillModelItem or null to include all KC models
     * @param value the value to record in the value field
     * @param getCached whether or not to get the cached file
     */
    void logStepRollupExport(DatasetItem datasetItem, UserItem userItem,
            StepRollupExportOptions stepRollupExportOptions, SkillModelItem skillModelItem,
            int value, boolean getCached);

    /**
     * Gets the student-step rollup items for the page grid.
     * @param sampleItem The sample to get all student roll ups for.
     * @param options Options for exporting.
     * @param currentLimit the limit for this sample
     * @param currentOffset the offset for this sample
     * @return A list of student rollup items.
     */
    List<Object> getStepRollupItems(final SampleItem sampleItem,
            StepRollupExportOptions options, Integer currentLimit, Integer currentOffset);

    /**
     * Returns a list of KCs associated with a step or problem.
     * @param measureId the step or problem id
     * @param skillModelId the skill model id
     * @param kcsForProblem flag to indicate if getting list for problem
     * @return a list of KCs associated with a step and opportunity
     */
    String getKCsForTooltip(Long stepId, Long skillModelId, Boolean kcsForProblem);
}
