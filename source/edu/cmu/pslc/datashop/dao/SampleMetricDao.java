/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SampleMetricItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * SampleMetric Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11660 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-10-15 10:42:10 -0400 (Wed, 15 Oct 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SampleMetricDao extends AbstractDao<SampleMetricItem> {

    /**
     * Override of the standard saveOrUpdate to make sure that duplicate metrics for the same
     * item do not show up.
     * @param object to update
     */
    void saveOrUpdate(Object object);

    /**
     * Standard get for a SampleMetricItem by id.
     * @param id The id of the SampleMetricItem.
     * @return the matching SampleMetricItem or null if none found
     */
    SampleMetricItem get(Integer id);

    /**
     * Standard find for an SampleMetricItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SampleMetricItem.
     * @return the matching SampleMetricItem.
     */
    SampleMetricItem find(Integer id);

    /**
     * Standard "find all" for SampleMetricItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Deletes all metrics for a given sample.
     * @param sample the sample to delete metrics for.
     */
    void deleteAll(SampleItem sample);

    /**
     * Get all sampleMetrics for a given sample.
     * @param sample the sample to get sampleMetrics for.
     * @return a List of SampleMetricItems
     */
    List find(SampleItem sample);

    /**
     * Get the most recent SampleMetricItem for a given sample and metric name.
     * @param sampleId the sample id
     * @param metric the metric name
     * @return the most recent SampleMetricItem
     */
    SampleMetricItem findLatestMetricBySample(Integer sampleId, String metric);

    /**
     * Get the maximum number of skills for a given sample and skill model.
     * @param sample the sample to get maximum number of skills for
     * @param skillModel the skill model to check.
     * @return Integer of the maximum number of skills.
     */
    Long getMaximumSkills(SampleItem sample, SkillModelItem skillModel);

    /**
     * Get the maximum number of skills for a given dataset and skill model.
     * @param dataset the dataset to get maximum number of skills for
     * @param skillModel the skill model to check.
     * @return Integer of the maximum number of skills.
     */
    Long getMaximumSkills(DatasetItem dataset, SkillModelItem skillModel);

    /**
     * Get the skill model names for the selected samples with skills for that model.
     * @param samples list of sample items
     * @param skillModelName given skill model name
     * @return List of skill model names
     */
    List getMaxModelsWithSkills(List<SampleItem> samples, List<String> skillModelName);

    /**
     * Get the maximum number of conditions for a given sample.
     * @param sample the sample to get maximum number of conditions for
     * @return Integer of the maximum number of conditions.
     */
    Long getMaximumConditions(SampleItem sample);

    /**
     * Get the maximum number of conditions for a given dataset.
     * @param dataset the dataset to get maximum number of conditions for
     * @return Integer of the maximum number of conditions.
     */
    Long getMaximumConditions(DatasetItem dataset);

    /**
     * Get the maximum number of students for a given sample.
     * @param sample the sample to get maximum number of students for
     * @return Integer of the maximum number of students.
     */
    Long getMaximumStudents(SampleItem sample);

    /**
     * Get the maximum number of students for a given dataset.
     * @param dataset the dataset to get maximum number of students for
     * @return Integer of the maximum number of students.
     */
    Long getMaximumStudents(DatasetItem dataset);

    /**
     * Get the maximum number of selections for a given sample.
     * @param sample the sample to get maximum number of selections for
     * @return Integer of the maximum number of selections.
     */
    Long getMaximumSelections(SampleItem sample);

    /**
     * Get the maximum number of selections for a given dataset.
     * @param dataset the dataset to get maximum number of selections for
     * @return Integer of the maximum number of selections.
     */
    Long getMaximumSelections(DatasetItem dataset);

    /**
     * Get the maximum number of actions for a given sample.
     * @param sample the sample to get maximum number of actions for
     * @return Integer of the maximum number of actions.
     */
    Long getMaximumActions(SampleItem sample);

    /**
     * Get the maximum number of actions for a given dataset.
     * @param dataset the dataset to get maximum number of actions for
     * @return Integer of the maximum number of actions.
     */
    Long getMaximumActions(DatasetItem dataset);

    /**
     * Get the maximum number of actions for a given sample.
     * @param sample the sample to get maximum number of actions for
     * @return Integer of the maximum number of actions.
     */
    Long getMaximumInputs(SampleItem sample);

    /**
     * Get the maximum number of actions for a given dataset.
     * @param dataset the dataset to get maximum number of actions for
     * @return Integer of the maximum number of actions.
     */
    Long getMaximumInputs(DatasetItem dataset);

    /**
     * Get the total number of students for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of students.
     */
    Long getTotalTransactions(DatasetItem dataset);

    /**
     * Get the total number of transactions for a given sample.
     * @param sample the sample to check
     * @return Integer of the total number of transactions.
     */
    Long getTotalTransactions(SampleItem sample);

    /**
     * Get the total number of students for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of students.
     */
    Long getTotalStudents(DatasetItem dataset);

    /**
     * Get the total number of students for a given sample.
     * @param sample the sample to check
     * @return Integer of the total number of students.
     */
    Long getTotalStudents(SampleItem sample);

    /**
     * Get the total number of student milliseconds for a given sample.
     * @param dataset the dataset to check
     * @return Integer of the total number of student milliseconds.
     */
    Long getTotalStudentMilliseconds(DatasetItem dataset);

    /**
     * Return the total number of student hours for the given dataset as a double.
     * @param datasetItem the given dataset
     * @return a double of the total student hours
     */
    Double getTotalStudentHours(DatasetItem datasetItem);

    /**
     * Return the total number of student hours of the given dataset as a string.
     * @param datasetItem the given dataset
     * @return a string with two decimal places of the total number of student hours
     */
    String getTotalStudentHoursAsString(DatasetItem datasetItem);

    /**
     * Return the total number of student hours for the given sample as a double.
     * @param sampleItem the given sample
     * @return a double of the total student hours
     */
    Double getTotalStudentHours(SampleItem sampleItem);

    /**
     * Get the total number of uniqueSteps for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of uniqueSteps.
     */
    Long getTotalUniqueSteps(DatasetItem dataset);

    /**
     * Get the total number of uniqueSteps for a given sample.
     * @param sample the sample to check
     * @return Integer of the total number of uniqueSteps.
     */
    Long getTotalUniqueSteps(SampleItem sample);

    /**
     * Get the total number of performedSteps for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of performedSteps.
     */
    Long getTotalPerformedSteps(DatasetItem dataset);

    /**
     * Get the total number of performedSteps for a given sample.
     * @param sample the sample to check
     * @return Long of the total number of performedSteps.
     */
    Long getTotalPerformedSteps(SampleItem sample);

    /**
     * Get the maximum number of distinct skills across steps for a given dataset and skill model.
     * @param dataset the dataset to get the metric for.
     * @param skillModel the skill model to check.
     * @return Integer of the maximum number of skills.
     */
    Long getMaxDistinctSkillsAcrossSteps(DatasetItem dataset, SkillModelItem skillModel);

    /**
     * Get the maximum number of distinct skills across steps for a given sample and skill model.
     * @param sample the sample to get the metric for.
     * @param skillModel the skill model to check.
     * @return Integer of the maximum number of skills.
     */
    Long getMaxDistinctSkillsAcrossSteps(SampleItem sample, SkillModelItem skillModel);

}
