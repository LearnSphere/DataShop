/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import edu.cmu.pslc.datashop.dto.StudentProblemRollupOptions;
import edu.cmu.pslc.datashop.dto.StudentProblemSkillInfo;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.StudentProblemRollupItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Student Problem Rollup Database Access Object Interface.
 *
 * @author Jim Rankin
 * @version $Revision: 9366 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-06-07 10:46:16 -0400 (Fri, 07 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface StudentProblemRollupDao extends AbstractDao {

    /**
     * Fetch all student problem rollup items for the given options.
     * @param options the {@link StudentProblemRollupOptions}
     * @return all student problem rollup items for the given options
     */
    List<StudentProblemRollupItem> getStudentProblemPreview(
            StudentProblemRollupOptions options);

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
    List<StudentProblemRollupItem> getStudentProblemPreview(SampleItem sample,
            StudentProblemRollupOptions options, Integer offset, Integer limit,
            int minStudentId, int maxStudentId);

    /**
     * Return the total number of student problem rows for the options.
     * @param options the StudentProblemRollupOptions
     * @return total number of student problem rows for the options.
     */
    long numberOfStudentProblems(StudentProblemRollupOptions options);

    /**
     * Return the total number of student problem rows for the sample.
     * @param sampleItem the sample item
     * @param options the options
     * @return total number of student problem rows for the sample
     */
    long numberOfStudentProblems(SampleItem sampleItem, StudentProblemRollupOptions options);

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
    PreparedStatement getStudentProblemExportRows(Session session,
            SampleItem sample, StudentProblemRollupOptions options, String batchList)
                throws SQLException;

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
    Map<SkillModelItem, Map<StudentProblemSkillInfo, StudentProblemSkillInfo>>
        getStudentProblemSkillNameMappingAllModels(SampleItem sampleItem,
            StudentProblemRollupOptions options, List<StudentItem> studentIds,
            List<ProblemItem> problemItems);

    /**
     * Get the skill information for a given sample and set of options. Pre-dates Trac #95.
     * @param sample the sample to get skill information for.
     * @param options the options to limit the results.
     * @return Map of skill info keyed to the student, problem, and problem view
     */
    Map<StudentProblemSkillInfo, StudentProblemSkillInfo> getStudentProblemSkillNameMapping(
            SampleItem sample, StudentProblemRollupOptions options);

    /**
     * Get the student problem headers.
     * @return the student problem headers
     */
    List<String> getProblemRollupHeaders();

    /**
     * Returns a student_id batch list for the student problem export based on anon user id.
     * @param sampleItem the sample
     * @param batchOffset the offset
     * @param studentBatchSize the limit
     * @param options the StudentProblemRollupOptions that contain the student list
     * @return a batch of student_id's based on the ordered anon_user_id's
     */
    String getStudentIdBatchList(SampleItem sampleItem, int batchOffset,
            Integer studentBatchSize, StudentProblemRollupOptions options);

    /** Gets the min student id for the student problem preview.
     * @param options the StudentProblemRollupOptions
     * @return the min student id
     */
    Map<String, Integer> getMinAndMaxStudentIds(final StudentProblemRollupOptions options);

    /** Gets the min student id for the student problem preview.
     * @param sampleItem the sample
     * @param options the StudentProblemRollupOptions
     * @param studentId the student id
     * @return the min student id
     */
    Integer countToMinStudentId(final SampleItem sampleItem,
            final StudentProblemRollupOptions options, final Integer studentId);

    /** Gets the min student id for the student problem preview.
     * @param sampleId the sample id
     * @param options the StudentProblemRollupOptions
     * @return the min student id
     */
    Integer getMinStudentId(Integer sampleId, final StudentProblemRollupOptions options);

    /** Gets the min student id for the student problem preview for selected samples.
     * @param options the StudentProblemRollupOptions
     * @return the min student id
     */
    Integer getMinStudentId(final StudentProblemRollupOptions options);

    /**
     * This method calls the populate_problem_hierarchy stored
     * procedure, operating on dataset_levels for the given dataset.
     * @param dataset the dataset we wish to (re)-populate the problem_hierarchy.
     * @return the number of created problem_hierarchy rows for this dataset
     */
    Integer callPopulateProblemHierarchy(DatasetItem dataset);

    /** Need to expose the getSession method so that it can be used by
     * the StudentProblemExportTask to create a PreparedStatement that
     * the export bean can cancel.
     * @return a hibernate session
     */
    Session session();

    /** Need to expose the release session method.
     * @param session the hibernate session
     */
    void release(Session session);

    /**
     * Log the student-problem export.
     * @param datasetItem the DatasetItem
     * @param userItem the UserItem or null to log with the SystemLogger
     * @param sprOptions the StudentProblemRollupOptions
     * @param skillModelItem the SkillModelItem or null to include all KC models
     * @param value the value to record in the value field
     * @param getCached whether or not to get the cached file
     */
    void logStudentProblemExport(DatasetItem datasetItem, UserItem userItem,
            StudentProblemRollupOptions sprOptions, SkillModelItem skillModelItem,
            int value, boolean getCached);
}