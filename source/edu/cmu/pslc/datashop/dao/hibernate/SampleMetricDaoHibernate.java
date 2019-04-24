/* * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.helper.DatasetState;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SampleMetricItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Hibernate and Spring implementation of the SampleMetricDao.
 *
 * @author Benjamin Billings
 * @version $Revision: 11660 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-10-15 10:42:10 -0400 (Wed, 15 Oct 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleMetricDaoHibernate extends AbstractDaoHibernate<SampleMetricItem>
implements SampleMetricDao {

    /**
     * Override of the standard saveOrUpdate to make sure that duplicate metrics for the same
     * item do not show up.
     * @param object to update
     */
    public void saveOrUpdate(Object object) {

         SampleMetricItem newItem = (SampleMetricItem)object;

         SampleMetricItem updateItem = (newItem.getId() != null)
                             ? get((Integer)newItem.getId()) : null;

         if (updateItem != null) {
             int numParams = 2;
             if (newItem.getSkillModel() != null) { numParams++; }

             Object[] params = new Object[numParams];
             params[0] = newItem.getSample();
             params[1] = newItem.getMetric();

             String query = "from SampleMetricItem sm "
                 + " where sm.sample = ?"
                 + " and sm.metric = ?";

             if (newItem.getSkillModel() != null) {
                 params[2] = newItem.getSkillModel();
                 query += " and sm.skillModel = ?";
             }
             List <SampleMetricItem> existingItems = getHibernateTemplate().find(query, params);
             if (existingItems.size() > 0) {
                 SampleMetricItem existing = existingItems.remove(0);
                 newItem.setId(existing.getId());
             }

             //delete any other duplicates
             for (SampleMetricItem sampleMetric : existingItems) { delete(sampleMetric);  }
         }
         super.saveOrUpdate(newItem);
    }

    /**
     * Standard get for a SampleMetricItem by id.
     * @param id The id of the user.
     * @return the matching SampleMetricItem or null if none found
     */
    public SampleMetricItem get(Integer id) {
        return (SampleMetricItem)get(SampleMetricItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<SampleMetricItem> findAll() {
        return findAll(SampleMetricItem.class);
    }

    /**
     * Deletes all metrics for a given sample.
     * @param sample the sample to delete metrics for.
     */
    public void deleteAll(SampleItem sample) {
        for (SampleMetricItem sampleMetric : find(sample)) { delete(sampleMetric); }
    }

    /**
     * Standard find for an SampleMetricItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SampleMetricItem.
     * @return the matching SampleMetricItem.
     */
    public SampleMetricItem find(Integer id) {
        return (SampleMetricItem)find(SampleMetricItem.class, id);
    }

    /**
     * Get all sampleMetrics for a given sample.
     * @param sample the sample to get sampleMetrics for.
     * @return a List of SampleMetricItems
     */
    public List<SampleMetricItem> find(SampleItem sample) {
        return getHibernateTemplate().find(
                "from SampleMetricItem sampleMetric where sampleMetric.sample.id = ?",
                sample.getId());
    }

    /**
     * Get the most recent SampleMetricItem for a given sample and metric name.
     * @param sampleId the sample id
     * @param metric the metric name
     * @return the most recent SampleMetricItem
     */
    public SampleMetricItem findLatestMetricBySample(Integer sampleId, String metric) {
        List<SampleMetricItem> sampleMetrics = null;
        SampleMetricItem latestMetric = null;

        if (sampleId != null) {
            Object[] params = {sampleId, metric};
            sampleMetrics = getHibernateTemplate().find(
                    "from SampleMetricItem sampleMetric where sampleMetric.sample.id = ?"
                    + " and sampleMetric.metric = ? "
                    + " order by calculated_time desc"
                    + " limit 1",
                    params);
            if (sampleMetrics != null && !sampleMetrics.isEmpty()) {
                latestMetric = sampleMetrics.get(0);
            }
        }

        return latestMetric;
    }

    /**
     * Get the maximum number of skills for a given sample and skill model.
     * @param dataset the dataset to get maximum number of skills for
     * @param skillModel the skill model to check.
     * @return Integer of the maximum number of skills.
     */
    public Long getMaximumSkills(DatasetItem dataset, SkillModelItem skillModel) {
        return getMaximumSkills(getAllDataSample(dataset), skillModel);
    }

    /**
     * Get the maximum number of skills for a given sample and skill model.
     * @param sample the sample to get maximum number of skills for
     * @param skillModel the skill model to check.
     * @return Long of the maximum number of skills.
     */
    public Long getMaximumSkills(SampleItem sample, SkillModelItem skillModel) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(
                    sample, SampleMetricItem.MAX_SKILLS, skillModel);
            return getLongValue(item);
        }
    }

    /**
     * Used by transaction export page grid preview when two or more samples are
     * selected.  It is necessary to add the correct number of headers and fill
     * in enough empty columns for samples that may not have a particular skill
     * model or have multiple skills for a model.  The query returns a list of
     * SkillModelWithMaxSkills objects, which are just thin wrappers holding the
     * skill model name string and associated max value long pair.
     * @param samples List of SampleItems
     * @param skillModelNames All skill model names for a dataset
     * @return List of SkillModelWithMaxSkills that
     */
    public List getMaxModelsWithSkills(List<SampleItem> samples, List<String> skillModelNames) {
        String query = "select new SkillModelWithMaxSkills"
            + "(smi.skillModel.skillModelName, max(cast(smi.value,int))) "
            + "from SampleMetricItem smi "
            + "where smi.sample.id in (:sampleIdList) "
            + "and smi.metric = 'Max Skills' "
            + "and smi.skillModel.skillModelName in (:skillModelNameList) "
            + "and smi.value != '0' "
            + "group by smi.skillModel.skillModelName "
            + "order by smi.skillModel.id";
        List<Integer> idList = getSampleIds(samples);
        String[] keys = new String[]{"sampleIdList", "skillModelNameList"};
        Object[] values =  new Object[]{idList, skillModelNames};
        return getHibernateTemplate().findByNamedParam(query, keys, values);
    }

    /**
     * Private method for getMaxModelsWithSkills to take a list
     * of SampleItems and get back a list of their Integer IDs.
     * @param samples List of SampleItems
     * @return List<Integer> sample IDs for all SampleItems
     */
    private List<Integer> getSampleIds(List<SampleItem> samples) {
        List<Integer> idValues = new ArrayList<Integer>();
        for (SampleItem sample : samples) {
            idValues.add((Integer)sample.getId());
        }
        return idValues;
    }

    /**
     * Get the maximum number of conditions for a given dataset.
     * @param dataset the dataset to get maximum number of conditions for
     * @return Integer of the maximum number of conditions.
     */
    public Long getMaximumConditions(DatasetItem dataset) {
        return getMaximumConditions(getAllDataSample(dataset));
    }

    /**
     * Get the maximum number of conditions for a given sample.
     * @param sample the sample to get maximum number of conditions for
     * @return Integer of the maximum number of conditions.
     */
    public Long getMaximumConditions(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.MAX_CONDITIONS);
            return getLongValue(item);
        }
    }

    /**
     * Get the maximum number of students for a given dataset.
     * @param dataset the dataset to get maximum number of students for
     * @return Integer of the maximum number of students.
     */
    public Long getMaximumStudents(DatasetItem dataset) {
        return getMaximumStudents(getAllDataSample(dataset));
    }

    /**
     * Get the maximum number of students for a given sample.
     * @param sample the sample to get maximum number of students for
     * @return Integer of the maximum number of students.
     */
    public Long getMaximumStudents(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.MAX_STUDENTS);
            return getLongValue(item);
        }
    }

    /**
     * Get the maximum number of selections for a given dataset.
     * @param dataset the dataset to get maximum number of selections for
     * @return Integer of the maximum number of selections.
     */
    public Long getMaximumSelections(DatasetItem dataset) {
        return getMaximumSelections(getAllDataSample(dataset));
    }

    /**
     * Get the maximum number of selections for a given sample.
     * @param sample the sample to get maximum number of selections for
     * @return Integer of the maximum number of selections.
     */
    public Long getMaximumSelections(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.MAX_SELECTIONS);
            return getLongValue(item);
        }
    }

    /**
     * Get the maximum number of actions for a given dataset.
     * @param dataset the dataset to get maximum number of actions for
     * @return Integer of the maximum number of actions.
     */
    public Long getMaximumActions(DatasetItem dataset) {
        return getMaximumActions(getAllDataSample(dataset));
    }

    /**
     * Get the maximum number of actions for a given sample.
     * @param sample the sample to get maximum number of actions for
     * @return Integer of the maximum number of actions.
     */
    public Long getMaximumActions(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.MAX_ACTIONS);
            return getLongValue(item);
        }
    }

    /**
     * Get the maximum number of inputs for a given dataset.
     * @param dataset the dataset to get maximum number of actions for
     * @return Integer of the maximum number of actions.
     */
    public Long getMaximumInputs(DatasetItem dataset) {
        return getMaximumInputs(getAllDataSample(dataset));
    }

    /**
     * Get the maximum number of inputs for a given sample.
     * @param sample the sample to get maximum number of inputs for
     * @return Integer of the maximum number of inputs.
     */
    public Long getMaximumInputs(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.MAX_INPUTS);
            return getLongValue(item);
        }
    }

    /**
     * Get the total number of students for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of students.
     */
    public Long getTotalTransactions(DatasetItem dataset) {
        return getTotalTransactions(getAllDataSample(dataset));
    }

    /**
     * Get the total number of transactions for a given sample.
     * @param sample the sample to check
     * @return Integer of the total number of transactions.
     */
    public Long getTotalTransactions(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.TOTAL_TRANSACTIONS);
            return getLongValue(item);
        }
    }

    /**
     * Get the total number of students for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of students.
     */
    public Long getTotalStudents(DatasetItem dataset) {
        return getTotalStudents(getAllDataSample(dataset));
    }

    /**
     * Get the total number of students for a given sample.
     * @param sample the sample to check
     * @return Integer of the total number of students.
     */
    public Long getTotalStudents(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.TOTAL_STUDENTS);
            return getLongValue(item);
        }
    }

    /**
     * Get the total number of student milliseconds for a given dataset.
     * @param datasetItem the given dataset
     * @return Integer of the total number of student milliseconds.
     */
    public Long getTotalStudentMilliseconds(DatasetItem datasetItem) {

        SampleItem sampleItem = getAllDataSample(datasetItem);
        if (sampleItem == null) {
            return new Long(0);
        } else {
            Object[] params = {sampleItem.getId(), SampleMetricItem.TOTAL_STUDENT_MILLISECONDS};
            List list =  getHibernateTemplate().find("from SampleMetricItem sm "
                    + "where sm.sample.id = ? and sm.metric = ? " , params);
            if (list.size() > 0) {
                SampleMetricItem sampleMetricItem = new SampleMetricItem();
                sampleMetricItem = (SampleMetricItem) list.get(0);
                return getLongValue(sampleMetricItem);
            } else {
                return new Long(0);
            }
        }
    }

    /** Number of milliseconds in an hour, 3600000. */
    private static final Integer MILLISECONDS_TO_HOUR_CONVERSION = 3600000;

    /**
     * Return the total number of student hours for the given dataset as a double.
     * @param datasetItem the given dataset
     * @return a double of the total student hours
     */
    public Double getTotalStudentHours(DatasetItem datasetItem) {
        Long milliseconds = getTotalStudentMilliseconds(datasetItem);
        Double hours = 0.00;
        hours  = Double.valueOf(milliseconds) / Double.valueOf(MILLISECONDS_TO_HOUR_CONVERSION);
        return hours;
    }

    /** String format of student hours. */
    private static final DecimalFormat STUDENT_HOURS_FORMAT = new DecimalFormat("0.00");

    /**
     * Return the total number of student hours of the given dataset as a string.
     * @param datasetItem the given dataset
     * @return a string with two decimal places of the total number of student hours
     */
    public String getTotalStudentHoursAsString(DatasetItem datasetItem) {
        Double hours = getTotalStudentHours(datasetItem);
        return STUDENT_HOURS_FORMAT.format(hours);
    }

    /**
     * Return the total number of student hours for the given sample as a double.
     * @param sampleItem the given sample
     * @return a double of the total student hours or null if the sample does not exist
     */
    public Double getTotalStudentHours(SampleItem sampleItem) {
        Long milliseconds = null;
        Double hours = null;

        if (sampleItem != null) {
            Object[] params = {sampleItem.getId(), SampleMetricItem.TOTAL_STUDENT_MILLISECONDS};
            List list =  getHibernateTemplate().find("from SampleMetricItem sm "
                    + "where sm.sample.id = ? and sm.metric = ? " , params);
            if (list != null && !list.isEmpty()) {
                SampleMetricItem sampleMetricItem = new SampleMetricItem();
                sampleMetricItem = (SampleMetricItem) list.get(0);
                milliseconds = getLongValue(sampleMetricItem);
                hours  = Double.valueOf(milliseconds) / Double.valueOf(MILLISECONDS_TO_HOUR_CONVERSION);
            }
        }
        return hours;
    }

    /**
     * Get the total number of uniqueSteps for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of uniqueSteps.
     */
    public Long getTotalUniqueSteps(DatasetItem dataset) {
        return getTotalUniqueSteps(getAllDataSample(dataset));
    }

    /**
     * Get the total number of uniqueSteps for a given sample.
     * @param sample the sample to check
     * @return Integer of the total number of uniqueSteps.
     */
    public Long getTotalUniqueSteps(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.TOTAL_UNIQUE_STEPS);
            return getLongValue(item);
        }
     }

    /**
     * Get the total number of performedSteps for a given dataset.
     * @param dataset the dataset to check
     * @return Integer of the total number of performedSteps.
     */
    public Long getTotalPerformedSteps(DatasetItem dataset) {
        return getTotalPerformedSteps(getAllDataSample(dataset));
    }

    /**
     * Get the total number of performedSteps for a given sample.
     * @param sample the sample to check
     * @return Integer of the total number of performedSteps.
     */
    public Long getTotalPerformedSteps(SampleItem sample) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(sample, SampleMetricItem.TOTAL_STEPS);
            return getLongValue(item);
        }
    }

    /**
     * Get the maximum number of distinct skills across steps for a given dataset and skill model.
     * @param dataset the dataset to get the metric for.
     * @param skillModel the skill model to check.
     * @return Integer of the maximum number of skills.
     */
    public Long getMaxDistinctSkillsAcrossSteps(DatasetItem dataset, SkillModelItem skillModel) {
        return getMaxDistinctSkillsAcrossSteps(getAllDataSample(dataset), skillModel);
    }

    /**
     * Get the maximum number of distinct skills across steps for a given sample and skill model.
     * @param sample the sample to get the metric for.
     * @param skillModel the skill model to check.
     * @return Integer of the maximum number of skills.
     */
    public Long getMaxDistinctSkillsAcrossSteps(SampleItem sample, SkillModelItem skillModel) {
        if (sample == null) {
            return new Long(0);
        } else {
            SampleMetricItem item = getOrCreateMetric(
                    sample, SampleMetricItem.MAX_DISTINCT_SKILLS_ACROSS_STEPS, skillModel);
            return getLongValue(item);
        }
    }

    /**
     * Gets the metric item for a given sample and metric.
     * @param sample the sample to get a metric for
     * @param metric the metric to get
     * @return SampleMetricItem the resulting sample metric, null if none found.
     */
    private SampleMetricItem getOrCreateMetric(SampleItem sample, String metric) {
        return getOrCreateMetric(sample, metric, null);
    }

    /**
     * Gets the metric item for a given sample, metric, and skill model.
     * @param sample the sample to get a metric for
     * @param metric the metric to get
     * @param skillModel optional skill model (can be null)
     * @return SampleMetricItem the resulting sample metric, null if none found.
     */
    private SampleMetricItem getOrCreateMetric(SampleItem sample,
            String metric, SkillModelItem skillModel) {
        String query = "from SampleMetricItem "
                      + " where sample = ?"
                      + " and metric = ?";
        String loggerPrefix = "getOrCreateMetric : Dataset (" + sample.getDataset().getId()
            + ") Sample (" + sample.getId() + "), Metric (" + metric + ")";

        int numParams = 2;

        if (skillModel != null) {
            query += " and skillModel = ?";
            numParams++;
            loggerPrefix += " SkillModel (" + skillModel.getId() + ")";
        }

        Object[] params = new Object[numParams];

        params[0] = sample;
        params[1] = metric;
        if (skillModel != null) { params[2] = skillModel; }

        /* Check if a record with the same sample, metric and skill model exists. */
        List rst = getHibernateTemplate().find(query, params);
        SampleMetricItem existingSampleMetric = null;
        if (rst.size() > 0) {
            existingSampleMetric = (SampleMetricItem)rst.get(0);
        }

        Date lastModified = DatasetState.lastModified(sample.getDataset());
        if (lastModified == null) {
            lastModified = DatasetState.lastCreated(sample.getDataset());
        }
        lastModified = (lastModified == null) ? new Date() : lastModified;

        if (lastModified != null) {
            numParams++;
            query += " and calculated_time >= ?";
            loggerPrefix += " calculated_time (" + lastModified + ")";
        } else {
            logDebug(loggerPrefix + " No created or modified time.");
        }

        Object[] params2 = new Object[numParams];

        params2[0] = sample;
        params2[1] = metric;
        if (skillModel != null) { params2[2] = skillModel; }
        if (lastModified != null) {
            params2[numParams - 1] = lastModified;
            logDebug(loggerPrefix + " Last modified time is " + lastModified);
        }

        SampleMetricItem sampleMetric = null;
        List results = getHibernateTemplate().find(query, params2);
        if (results.size() > 0) {
            logDebug(loggerPrefix + " At least one sample found");
            if (results.size() > 1) {
                logger.info(loggerPrefix + " More than one value found for sample.");
            }
            sampleMetric = (SampleMetricItem)results.get(0);
            logDebug(loggerPrefix + " value is " + sampleMetric.getValue());
        }

        if (sampleMetric == null) {
            logDebug(loggerPrefix + " Metric is null, going to recalculate.");
            Long value = recalculateMetric(metric, sample, skillModel);

            if (DatasetState.requiresAggregation(sample.getDataset(), sample)
                    && (metric.equals(SampleMetricItem.TOTAL_STEPS)
                            || metric.equals(SampleMetricItem.TOTAL_UNIQUE_STEPS))) {
                logger.info(loggerPrefix
                        + " Aggregation required for this sample, not setting metric.");
            } else {
                if (value != null) {
                    if (existingSampleMetric != null) {
                        existingSampleMetric.setValue(value);
                        existingSampleMetric.setCalculatedTime(new Date());
                        saveOrUpdate(existingSampleMetric);
                        sampleMetric = existingSampleMetric;
                        logger.info(loggerPrefix
                                + " Update existing item : " + sampleMetric.getId()
                                + " with value " + sampleMetric.getValue());
                    } else {
                        SampleMetricItem newMetric = new SampleMetricItem();
                        newMetric.setMetric(metric);
                        newMetric.setSample(sample);
                        newMetric.setSkillModel(skillModel);
                        newMetric.setValue(value);
                        newMetric.setCalculatedTime(new Date());
                        saveOrUpdate(newMetric);
                        sampleMetric = newMetric;
                        logger.info(loggerPrefix
                                + " Inserting a new item : " + sampleMetric.getId()
                                + " with value " + sampleMetric.getValue());
                    }
                }
            }
        }
        return sampleMetric;
    }

    /**
     * Gets the "All Data" sample for a given dataset.
     * @param dataset the dataset to get the all data for.
     * @return SampleItem that is the AllData.
     */
    private SampleItem getAllDataSample(DatasetItem dataset) {
       return DaoFactory.DEFAULT.getSampleDao().findOrCreateDefaultSample(dataset);
    }

    /**
     * Gets the value from the sample metric as a Long
     * @param metric the metric to get the value of
     * @return a Long of the value, null if unable to parse.
     */
    private Long getLongValue(SampleMetricItem metric) {
        Long value = null;
        if (metric == null) {
            value = Long.valueOf(-1);
        } else {
            value = metric.getValue();
        }
        return value;
    }

    /**
     * Recalculates a metric
     * @param metric the metric to calculate.
     * @param sample the sample to recalculate values for.
     * @param skillModel options skill model (can be null)
     * @return Long of the new value.
     */
    private Long recalculateMetric(String metric, SampleItem sample, SkillModelItem skillModel) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
        if (SampleMetricItem.MAX_ACTIONS.equals(metric)) {
            return sampleDao.getMaxAttemptActionCount(sample);
        } else if (SampleMetricItem.MAX_CONDITIONS.equals(metric)) {
            return sampleDao.getMaxConditionCount(sample);
        } else if (SampleMetricItem.MAX_INPUTS.equals(metric)) {
            return sampleDao.getMaxAttemptInputCount(sample);
        } else if (SampleMetricItem.MAX_SELECTIONS.equals(metric)) {
            return sampleDao.getMaxAttemptSelectionCount(sample);
        } else if (SampleMetricItem.MAX_SKILLS.equals(metric)) {
            return sampleDao.getMaxSkillCount(sample, skillModel);
        } else if (SampleMetricItem.MAX_STUDENTS.equals(metric)) {
            return sampleDao.getMaxStudentCount(sample);
        } else if (SampleMetricItem.TOTAL_STEPS.equals(metric)) {
            return sampleDao.getNumPerformedSteps(sample);
        } else if (SampleMetricItem.TOTAL_STUDENTS.equals(metric)) {
            return sampleDao.getNumStudents(sample);
        } else if (SampleMetricItem.TOTAL_TRANSACTIONS.equals(metric)) {
            return sampleDao.getNumTransactions(sample);
        } else if (SampleMetricItem.TOTAL_UNIQUE_STEPS.equals(metric)) {
            return sampleDao.getNumUniqueSteps(sample);
        } else if (SampleMetricItem.MAX_DISTINCT_SKILLS_ACROSS_STEPS.equals(metric)) {
            return stepRollupDao.getMaxDistinctSkillsAcrossSteps(sample, skillModel);
        } else {
            logger.warn("Attempt to recalculate an unknown metric " + metric);
        }
        return null;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
