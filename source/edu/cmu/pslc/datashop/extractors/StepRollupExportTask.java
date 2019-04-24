/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_STEP_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_STEP_EXPORT_START;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import java.sql.ResultSetMetaData;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.dto.StepRollupExportOptions;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;

import edu.cmu.pslc.datashop.util.CollectionUtils.Factory;
import edu.cmu.pslc.datashop.util.CollectionUtils.DefaultMap;
import edu.cmu.pslc.datashop.util.CollectionUtils.DefaultValueMap;

import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.FormattingUtils.displayObject;
import static edu.cmu.pslc.datashop.util.FormattingUtils.formatForSeconds;
import static edu.cmu.pslc.datashop.type.CorrectFlag.getInstance;
import static java.util.Arrays.asList;

/**
 * Handles the export of the student step rollup to a cached file.
 *
 * @author Jim Rankin
 * @version $Revision: 12416 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-06-24 16:06:24 -0400 (Wed, 24 Jun 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepRollupExportTask extends ExportTask {
    /** Default value for studentBatchSize. */
    public static final int DEFAULT_STUDENT_BATCH_SIZE = 500;
    /** maximum number of students to process at one time. */
    private int studentBatchSize = DEFAULT_STUDENT_BATCH_SIZE;
    /** format these columns to display seconds. */
    private static final Set<String> DURATION_COLS =
        set("step_duration", "correct_step_duration", "error_step_duration");
    /** group_concat separator for skill columns in the step rollup query. */
    private static final String SKILL_MODEL_SEP = "~~";
    /** log an info progress message for every INFO_BATCH_SIZE rows. */
    private static final int INFO_BATCH_SIZE = 10000;
    /** number of step rollup rows to process at a time. */
    private static final int BATCH_SIZE = 1000;
    /** separator used when multiple values in a single column. */
    private static final String SUBSEP = "~~";
    /** column separator. */
    private static final String SEP = "\t";
    /** The StepRollupExportOptions. */
    private StepRollupExportOptions options;

    /**
     * Create a step rollup export task.
     * @param info holds information relevant to cached files
     * @param options the StepRollupExportOptions
     */
    public StepRollupExportTask(final CachedFileInfo info,
            StepRollupExportOptions options) {
        super(info);
        this.options = options;
    }

    /**
     * maximum number of students to process at one time.
     * @param studentBatchSize maximum number of students to process at one time
     */
    public void setStudentBatchSize(int studentBatchSize) {
        this.studentBatchSize = studentBatchSize;
    }

    /**
     * Subdirectory of the main export directory for the sample.
     * @return subdirectory of the main export directory for the sample
     */
    @Override
    protected String getExportSubdirectory() { return "step"; }

    /** The step rollup dao. @return the step rollup dao */
    private StepRollupDao stepRollupDao() { return DaoFactory.DEFAULT.getStepRollupDao(); }

    /** skill model columns. */
    private static final List<String> SKILL_MODEL_COLS = asList("skill_models", "skills",
            "opportunities", "predicted_error_rates");

    /**
     * Need this to build skill model columns correctly.
     * @author jimbokun
     */
    private static class SkillModelColumns {
        /** the 3 skill model columns. */
        private List<String> skills, opportunities;
        private Set<String> predictedErrorRates;     // no duplicates allowed

        /** Skills column values. @return skills column values */
        private List<String> getSkills() {
            skills = checkNull(skills);
            return skills;
        }

        /** Opportunities column values. @return opportunities column values */
        private List<String> getOpportunities() {
            opportunities = checkNull(opportunities);
            return opportunities;
        }

        /** Predicted Error Rates column values. @return Predicted Error Rates column values */
        private Set<String> getPredictedErrorRates() {
            predictedErrorRates = checkNull(predictedErrorRates);
            return predictedErrorRates;
        }

        /**
         * Always add skill column values in tandem.
         * @param skill skill value
         * @param opportunity opportunity value
         * @param predictedErrorRate predicted error rate value
         */
        public void add(String skill, String opportunity, String predictedErrorRate) {
            getSkills().add(skill);
            getOpportunities().add(opportunity);
            getPredictedErrorRates().add(predictedErrorRate);
        }

        /**
         * Join values for a single column.
         * @param col the column values
         * @return the column values joined with ~~
         */
        private String subjoin(Iterable<String> col) { return join(SUBSEP, col); }

        /**
         * Join the columns with tabs, and the column values with ~~.
         * @return the columns joined with tabs, and the column values joined with ~~.
         */
        public String toString() {
            return join(SEP, subjoin(getSkills()), subjoin(getOpportunities()),
                    subjoin(getPredictedErrorRates()));
        }
    }

    /**
     * Default value is a SkillModelColumns object.
     * @author jimbokun
     */
    private class SkillModelColumnsMap extends DefaultMap<String, SkillModelColumns> {
        /** Create a new skill model columns map. */
        public SkillModelColumnsMap() {
            super(new Factory<SkillModelColumns>() {
                public SkillModelColumns create() { return new SkillModelColumns(); }
            });
        }
    }

    /** Constant. */
    private static final int PER_COL = 3;

    /**
     * Construct the skill model columns.
     * @param rows results of the step rollup query
     * @return the skill model columns
     * @throws SQLException if something goes wrong
     */
    private Map<String, SkillModelColumns> getSkillModelColumns(ResultSet rows)
    throws SQLException {
        List<List<String>> skillCols = new ArrayList<List<String>>();
        int maxSize = 0;

        // pad column lists to all be the same length
        for (String col : SKILL_MODEL_COLS) {
            String skillModelColStr = rows.getString(col);
            if (skillModelColStr == null) { skillModelColStr = ""; }
            skillCols.add(new ArrayList<String>(asList(skillModelColStr.split(SKILL_MODEL_SEP))));
        }
        for (List<String> col : skillCols) {
            if (col.size() > maxSize) { maxSize = col.size(); }
        }
        for (List<String> col : skillCols) {
            while (col.size() < maxSize) { col.add(""); }
        }

        List<String> skillModels = skillCols.get(0), skills = skillCols.get(1),
            opportunities = skillCols.get(2), predictedErrorRates = skillCols.get(PER_COL);
        SkillModelColumnsMap modelsToSkills = new SkillModelColumnsMap();

        for (int i = 0; i < skillModels.size(); i++) {
            modelsToSkills.getDefault(skillModels.get(i)).add(skills.get(i), opportunities.get(i),
                    predictedErrorRates.get(i));
        }

        return modelsToSkills;
    }

    /** columns returned by the step rollup query. */
    private static final List<String> STEP_ROLLUP_COLUMNS = asList("sample_name", "anon_user_id",
            "problem_hierarchy", "problem_name", "problem_view", "subgoal_name",
            "step_start_time", "first_transaction_time", "correct_transaction_time",
            "step_end_time", "step_duration", "correct_step_duration", "error_step_duration",
            "first_attempt", "total_incorrects", "total_hints", "total_corrects", "conditions");

    /**
     * Map from column names to values for the current row.
     * @param rs results of the step rollup query
     * @param probHier maps problem IDs to their problem hierarchy
     * @return a map from column names to values for the current row
     * @throws SQLException if something goes wrong
     */
    private DefaultMap<String, Object> resultSetMap(final ResultSet rs,
            Map<Integer, String> probHier) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        DefaultMap<String, Object> rsMap = new DefaultValueMap<String, Object>("");

        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String col = meta.getColumnName(i);
            Object val = rs.getObject(i);

            if ("conditions".equals(col) && val != null) {
                val = ((String)val).replace(", ", SUBSEP);
            } else if ("problem_id".equals(col)) {
                col = "problem_hierarchy";
                val = probHier.get(((Number)val).intValue());
            } else if (DURATION_COLS.contains(col)) {
                val = formatForSeconds((Long)val);
            } else if ("first_attempt".equals(col)) {
                val = getInstance(Integer.valueOf(val.toString()));
            } else if (val instanceof Date) {
                val = displayObject(val);
            }

            rsMap.put(col, val);
        }

        return  rsMap;
    }

    /**
     * Ordered values for the current step rollup row.
     * @param rows results of the step rollup query
     * @param rowno number of the current row
     * @param smItems the SkillModelItems
     * @param probHier maps problem IDs to their problem hierarchy
     * @return ordered values for the current step rollup rows
     * @throws SQLException if something goes wrong
     */
    private List<Object> stepRow(ResultSet rows, int rowno, List<SkillModelItem> smItems,
            Map<Integer, String> probHier) throws SQLException {
        List<Object> row = new ArrayList<Object>();

        final DefaultMap<String, Object> rsMap = resultSetMap(rows, probHier);
        Map<String, SkillModelColumns> skillModelCols = getSkillModelColumns(rows);

        row.add(rowno);

        for (String col : STEP_ROLLUP_COLUMNS) {
            if (col.matches(".*_duration")) {
                String dur = (String) rsMap.getDefault(col);
                row.add(dur == null ? "." : dur);
            } else {
                row.add(rsMap.getDefault(col));
            }
        }
        SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();

        if (options.isDisplayAllModels() || options.isDisplaySkills()) {
            for (SkillModelItem skillModelItem : smItems) {
                SkillModelColumns cols = skillModelCols.get(skillModelItem.getSkillModelName());
                row.add(cols == null ? "\t\t" : cols);
            }
        }

        return row;
    }

    /**
     * Write the step-rollup export to the temporary file.
     * @param skillModelItem the SkillModelItem or null to include all skill models
     * @param options the local StepRollupExportOptions to use
     * @param studentIds a list of student Ids or null if all are desired
     * @param problemItems a list of problem items or null if all are desired
     * @throws IOException if something goes wrong
     * @throws SQLException if something goes wrong
     */
    public void writeStepRollupExport(SkillModelItem skillModelItem,
            StepRollupExportOptions options, List<StudentItem> studentIds,
            List<ProblemItem> problemItems)
            throws IOException, SQLException {
        logInfo("writeStepollupExport");
        SampleItem sample = info.getSample();
        // For logging
        DatasetItem datasetItem = null;
        StepRollupDao stepRollupDao =
            DaoFactory.DEFAULT.getStepRollupDao();
        Session session = null;
        try {
            session = stepRollupDao.session();

            logInfo("Writing headers to temp file. ");

            List<String> headers = new ArrayList();

            headers.addAll(StepExportRow.STATIC_EXPORT_HEADERS);
            SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();
            List<SkillModelItem> smItems = null;
            if (skillModelItem != null) {
                smItems = new ArrayList();
                smItems.add(skillModelItem);
            } else {
                smItems = smDao.findOrderByName(sample.getDataset());
            }
            if (options.isDisplayAllModels() || options.isDisplaySkills()) {
                for (SkillModelItem smItem : smItems) {
                    headers.add("KC (" + smItem.getSkillModelName() + ")");
                    headers.add("Opportunity (" + smItem.getSkillModelName() + ")");
                    headers.add("Predicted Error Rate (" + smItem.getSkillModelName() + ")");
                }
            }

            logDebug("writeStepRollupExport: headers: ", headers);

            writeToTempFile(join(SEP, headers));

            int rowno = 0;
            final Map<Integer, String> probHier = stepRollupDao().getProblemHierarchy(sample);
            final Iterable<List<Long>> studentBatches =
                stepRollupDao().getStepRollupStudentBatches(sample, studentBatchSize);

            for (List<Long> studentBatch : studentBatches) {
                logInfo("Fetching step rollup data.");
                ResultSet rows = stepRollupDao().getStepRollupRows(
                    sample, options, session, BATCH_SIZE, studentBatch);
                logInfo("Fetched step rollup data.");

                logInfo("Writing rows to temp file.");
                while (rows.next()) {
                    writeToTempFile(join(SEP, stepRow(rows, rowno + 1, smItems, probHier)));
                    if ((rowno + 1) % INFO_BATCH_SIZE == 0) {
                        logInfo("Finished writing ", rowno, " rows to temp file.");
                    }
                    rowno++;
                }
            }
            info.incrementNumCompletedRows(rowno);

        } finally {
            stepRollupDao.release(session);
            closeTempFileWriter();
            logInfo("Finished writing to temp file.");
        }
    }

    /** Number of steps for the sample. @return number of steps for the sample */
    public Long getNumSteps() {
        Long numSteps = (stepRollupDao().getNumStepRollups(info.getSample(), options)).longValue();
        return numSteps;
    }

    /** Constant for Student-Step Export file names. */
    private static final String STU_STEP_EXPORT_PREFIX = "_student_step_";

    /**
     * Get string to prepend to the name of cached files.
     * @result file name prefix
     * @return the filename prefix
     */
    @Override
    protected String getFileNamePrefix() {
        return getDatasetInfoPrefix() + STU_STEP_EXPORT_PREFIX;
    }

    /**
     * System log action indicating that the export is finished.
     * @return system log action indicating that the export is finished
     */
    @Override
    protected String exportCompletedAction() { return ACTION_CACHED_STEP_EXPORT; }

    /**
     * System log action indicating that the export started.
     * @return system log action indicating that the export is finished
     */
    @Override
    protected String exportStartAction() { return ACTION_CACHED_STEP_EXPORT_START; }

    /**
     * Check for a message in the dataset system log indicating that an export for this dataset
     * and sample was completed.
     * @return whether an export for this dataset and sample was completed
     */
    public synchronized boolean isExportCompleted() {
        return datasetSystemLogDao().messageCheck(info.getDataset(), info.getSample(),
                exportCompletedAction());
    }

    /**
     * Record completion of transaction export in the dataset system log.
     * @param success whether or not the export completed successfully
     */
    public synchronized void logExportCompleted(boolean success) {
        long elapsedTime = System.currentTimeMillis() - info.getStart().getTime();
        int value = (new Long(info.getNumCompletedRows())).intValue();
        String msg = null;
        if (success) {

            msg = getLogPrefix() + "Cached Student-Step Export."
                    + " Student batch size: " + studentBatchSize;
        } else {
            msg = "ERROR: "
                + getLogPrefix() + " Student Batch Size: " + studentBatchSize;
        }
        SystemLogger.log(info.getDataset(), null, info.getSample(), exportCompletedAction(),
                msg, success, value, elapsedTime);
    }

    /**
     * Whether the step export needs to be re-cached.
     * @return false if the sample needs to be re-cached, true otherwise
     */
    public boolean isUpToDate() {
        List<SampleItem> samplesToCache = datasetSystemLogDao()
            .getSamplesToCacheStep(info.getDataset());
        for (SampleItem sampleToCache : samplesToCache) {
            if (sampleToCache.getId().equals(info.getSample().getId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Label identifying the kind of export.
     * @return label identifying the kind of export
     */
    @Override
    protected String getPrefixLabel() { return "CFG"; }
}
