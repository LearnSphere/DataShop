/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_PROBLEM_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_PROBLEM_EXPORT_START;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.FormattingUtils.displayObject;
import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static java.util.Arrays.asList;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.StudentProblemInfo;
import edu.cmu.pslc.datashop.dto.StudentProblemRollupOptions;
import edu.cmu.pslc.datashop.dto.StudentProblemSkillInfo;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.StudentProblemRollupItem;
import edu.cmu.pslc.datashop.util.CollectionUtils.DefaultMap;
import edu.cmu.pslc.datashop.util.CollectionUtils.DefaultValueMap;

/**
 * Presents an interface for each step of caching a student-problem export file.
 * Necessary for canceling an export, because we need to hold a reference to the
 * CallableStatement object for the export stored procedure, and StudentProblemExportHelper
 * is stateless by design.
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemExportTask extends ExportTask {
    /** Data needed for this export. */
    private CachedFileInfo info;
    /** Did the user cancel the export? */
    private boolean canceled = false;
    /** Hold on to this in case we need to cancel. */
    private PreparedStatement statement;
    /** The StudentProblemRollupOptions. */
    private StudentProblemRollupOptions options;

    /** Default value for studentBatchSize for export preview. */
    public static final int STUDENTPROBLEM_PREVIEW_BATCH_SIZE = 10;
    /** Default value for studentBatchSize for writing export. */
    public static final int STUDENTPROBLEM_EXPORT_BATCH_SIZE = 50;
    /** maximum number of students to process at one time. */
    private int studentBatchSize = STUDENTPROBLEM_EXPORT_BATCH_SIZE;

    /** Directory structure for student-problem export files. */
    public static final String PROBLEM_EXPORT_DIR = "export/problem";
    /** Default base directory for the files associated with a dataset. */
    public static final String BASE_DIR_DEFAULT = "/datashop/dataset_files";

    /** columns returned by the problem rollup query. */
    public static final List<String> PROBLEM_ROLLUP_COLUMNS = asList(
            "sample", "student", "problemHierarchy", "problem", "problemView",
            "startTime", "endTime", "latency", "numMissingStartTimes",
            "hints", "incorrects", "corrects", "avgCorrect",
            "steps", "avgAssistance", "correctFirstAttempts", "conditions");

    /** The logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** group_concat separator for skill columns in the problem rollup query. */
    private static final String SKILL_MODEL_SEP = "~~";
    /** separator used when multiple values in a single column. */
    private static final String SUBSEP = "~~";
    /** column separator. */
    private static final String SEP = "\t";
    /** Time stamp format. */
    public static final FastDateFormat TIME_STAMP_FMT
            = FastDateFormat.getInstance("yyyy_MMdd_HHmmss");
    /** Regular Expression to identify timestamps in cached file names. */
    public static final String TIME_STAMP_REGEX =
            Pattern.compile("_[0-9]{4}_[0-9]{2}[0-9]{2}_[0-9]{2}[0-9]{2}[0-9]{2}.zip",
            Pattern.UNICODE_CASE).toString();
    /** format these columns to display seconds. */
    private static final Set<String> DURATION_COLS =
            set("latency");
    /** avg corrects = # corrects / # steps */
    private static final Set<String> AVG_CORRECT_COLS =
            set("avgCorrect");
    /** avg assistance */
    private static final Set<String> AVG_ASSIST_COLS =
            set("avgAssistance");
    /** The number of rows in problem rollup export. */
    private int numProblems = 0;
    /** Whether we're export the cached or generated export. */
    private boolean isCached = false;
    /** Magic number for ms in a second. */
    private static final double MAGIC_1000 = 1000.;

    /**
     * Create a new transaction export task.
     * @param info data needed for this export
     * @param options the optional studentProblemRollupOptions
     */
    public StudentProblemExportTask(final CachedFileInfo info,
            StudentProblemRollupOptions options) {
        super(info);
        this.info = info;
        this.options = options;
    }

    /**
     * maximum number of students to process at one time.
     * @param studentBatchSize maximum number of students to process at one time
     */
    public void setStudentBatchSize(int studentBatchSize) {
        this.studentBatchSize = studentBatchSize;
        if (info != null) {
            info.setStudentBatchSize(studentBatchSize);
        }
    }

    /**
     * Subdirectory of the main export directory for the sample.
     * @return subdirectory of the main export directory for the sample
     */
    @Override
    protected String getExportSubdirectory() { return "problem"; }

    /**
     * Map from column names to values for the current row.
     * @param rs results of the problem rollup query
     * @return a map from column names to values for the current row
     * @throws SQLException if something goes wrong
     */
    public static DefaultMap<String, Object> resultSetMap(final ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        DefaultMap<String, Object> rsMap = new DefaultValueMap<String, Object>("");
        NumberFormat nf = new DecimalFormat(StudentProblemRollupItem.NUMBER_FORMAT);
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String col = meta.getColumnName(i);
            Object val = rs.getObject(i);

            if ("conditions".equals(col) && val != null) {
                val = ((String)val).replace(", ", SUBSEP);
            } else if (DURATION_COLS.contains(col)) {
                Date startTime = (Date) rs.getObject("startTime");
                Date endTime = (Date) rs.getObject("endTime");
                if (startTime != null && endTime != null) {
                    val = nf.format(Math.abs(
                        startTime.getTime() - endTime.getTime()) / MAGIC_1000);
                } else {
                    val = 0;
                }

            } else if (AVG_CORRECT_COLS.contains(col)) {
                Integer corrects =  ((Number) rs.getObject("corrects")).intValue();
                Double steps =  ((Number) rs.getObject("steps")).doubleValue();
                Double avgCorrect = corrects / steps;
                val = (String) nf.format(avgCorrect);
            } else if (AVG_ASSIST_COLS.contains(col)) {
                Double avgAssistance =  ((Number) rs.getObject("avgAssistance")).doubleValue();
                val = (String) nf.format(avgAssistance);
            } else if (val instanceof Date) {
                val = displayObject(val);
            }

            rsMap.put(col, val);
        }

        return  rsMap;
    }

    /**
     * Ordered values for the current problem rollup row.
     * @param sampleItem the sample
     * @param rows results of the problem rollup query
     * @param rowno number of the current row
     * @param skillModelItem the SkillModelItem or null to include all skill models
     * @param skillModelInfoMap the skill info map
     * @return ordered values for the current problem rollup rows
     * @throws SQLException if something goes wrong
     */
    private List<Object> problemRow(SampleItem sampleItem,
        ResultSet rows, int rowno, SkillModelItem skillModelItem,
            Map<SkillModelItem, Map<StudentProblemSkillInfo,
                StudentProblemSkillInfo>> skillModelInfoMap)
                        throws SQLException {
        List<Object> row = new ArrayList<Object>();

        final DefaultMap<String, Object> rsMap = resultSetMap(rows);
        row.add(rowno);

        for (String col : PROBLEM_ROLLUP_COLUMNS) {
            row.add(rsMap.getDefault(col));
        }
        SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();
        List<SkillModelItem> smItems = null;
        if (skillModelItem != null) {
            smItems = new ArrayList();
            smItems.add(skillModelItem);
        } else {
            smItems = smDao.findOrderByName(sampleItem.getDataset());
        }
        for (SkillModelItem smItem : smItems) {
            Map<StudentProblemSkillInfo, StudentProblemSkillInfo> skillInfoMap =
                skillModelInfoMap.get(smItem);

            StudentProblemSkillInfo skillInfo = new StudentProblemSkillInfo() { {

                setStudentId(((Number)(rsMap).getDefault("studentId")).longValue());
                setProblemId(((Number)rsMap.getDefault("problemId")).longValue());
                setProblemView(((Number)
                        rsMap.getDefault("problemView")).intValue());
            } };

            if (options.isDisplayAllModels() || options.isDisplaySkills()) {
                skillInfo = skillInfoMap.get(skillInfo);
                if (skillInfo != null) {
                    row.add(skillInfo.getNumSkills());
                    row.add(skillInfo.getNumUnmappedSteps());
                    row.add(skillInfo.getSkillList() == null
                        ? "." : skillInfo.getSkillList());
                }
            }
        }

        return row;
    }

    /**
     * Write the problem rollup export to the temporary file.
     * @param skillModelItem the SkillModelItem or null to include all skill models
     * @param options the local StudentProblemRollupOptions to use
     * @param studentIds a list of student Ids or null if all are desired
     * @param problemItems a list of problem items or null if all are desired
     * @throws IOException if something goes wrong
     * @throws SQLException if something goes wrong
     */
    public void writeProblemRollupExport(SkillModelItem skillModelItem,
            StudentProblemRollupOptions options, List<StudentItem> studentIds,
            List<ProblemItem> problemItems)
            throws IOException, SQLException {
        logInfo("writeProblemRollupExport");
        SampleItem sample = info.getSample();
        // For logging
        DatasetItem datasetItem = null;
        StudentProblemRollupDao studentProblemRollupDao =
            DaoFactory.DEFAULT.getStudentProblemRollupDao();
        Session session = null;
        try {
            session = studentProblemRollupDao.session();
            // Initialize problem skill-name mapping
            Map<SkillModelItem,
                Map<StudentProblemSkillInfo, StudentProblemSkillInfo>> skillModelInfoMap =
                        studentProblemRollupDao.getStudentProblemSkillNameMappingAllModels(
                            info.getSample(), options, studentIds, problemItems);

            logInfo("Writing headers to temp file: ");

            List<String> headers = new ArrayList();

            headers.addAll(StudentProblemInfo.STATIC_HEADERS);
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
                    headers.add("KCs (" + smItem.getSkillModelName() + ")");
                    headers.add("Steps without KCs (" + smItem.getSkillModelName() + ")");
                    headers.add("KC List (" + smItem.getSkillModelName() + ")");
                }
            }

            logDebug("writeProblemRollupExport: headers: ", headers);

            writeToTempFile(join(SEP, headers));

            int rowno = 0;

            // Use batching in the query to limit the amount of
            // temporary tables written to disk from the query using 'with rollup'
            long numberOfStudentProblems = studentProblemRollupDao
                .numberOfStudentProblems(sample, options);

            logInfo("Fetching " + numberOfStudentProblems + " problem rollup rows.");
            numProblems = (int) numberOfStudentProblems;
            for (int batchOffset = 0;
                    rowno < numberOfStudentProblems;
                    batchOffset += studentBatchSize) {
                String batchList = studentProblemRollupDao.getStudentIdBatchList(
                        sample, batchOffset, studentBatchSize, options);
                logInfo("Student id batch list: " + batchList);
                ResultSet innerResults = null;

                // Get next batch of student problem rows (based on student id batching)
                PreparedStatement ps = studentProblemRollupDao
                    .getStudentProblemExportRows(session, sample, options, batchList);
                innerResults = ps.executeQuery();

                // Use a string buffer to write to the file in batches
                StringBuffer sbuffer = new StringBuffer("");
                if (innerResults != null && batchList != null) {
                    while (innerResults.next()) {
                        // Append row to string buffer
                        String line = join(SEP,
                                problemRow(sample, innerResults, rowno + 1,
                                        skillModelItem, skillModelInfoMap));
                        sbuffer.append(line);
                        sbuffer.append("\n");
                        info.incrementNumCompletedRows(1);
                        rowno++;
                    }
                    // Write batch to temp file
                    writeToTempFileSameLine(sbuffer.toString());
                } else {
                    break;
                }

            } // end of batching for loop

        } finally {
            studentProblemRollupDao.release(session);
            closeTempFileWriter();
            logInfo("Finished writing to temp file.");
        }
    }



    /** Number of problems for the sample. @return number of problems for the sample */
    public Integer getNumProblems() {
        return numProblems;
    }

    /**
     * Whether the problem export needs to be re-cached.
     * @return false if the sample needs to be re-cached, true otherwise
     */
    public boolean isUpToDate() {
        List<SampleItem> samplesToCache = datasetSystemLogDao()
            .getSamplesToCacheProblem(info.getDataset());
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

    /** Create a current time stamp and format it. @return formatted time stamp of now */
    public String formattedNow() { return TIME_STAMP_FMT.format(new Date()); }

    /**
     * Returns the directory path for the given dataset and student-problem export directory.
     * @param sample the sample.
     * @param baseDir the base directory where cached export files should be stored.
     * @return the path to where the cached file should be stored.
     */
    private String getDirectoryPath(SampleItem sample, String baseDir) {
        if (baseDir == null) { baseDir = BASE_DIR_DEFAULT; }
        String result = baseDir + "/" + sample.getFilePath() + "/" + PROBLEM_EXPORT_DIR;
        return result;
    }

    /**
     * Cancel the export.
     */
    public synchronized void cancel() {
        canceled = true;
        if (statement != null) {
            try {
                statement.cancel();
            } catch (SQLException sql) {
                logger.error(getLogPrefix(info) + "Error canceling sample export.", sql);
            }
        }
    }

    /**
     * Check for a message in the dataset system log indicating that
     * an export for this dataset and sample was already started.
     * @return whether an export for this dataset and sample was already started
     */
    public synchronized boolean isExportStarted() {
        return isStudentProblemExportStarted(info.getDataset(), info.getSample());
    }

    /**
     * Check for a message in the dataset system log indicating that
     * an export for this dataset and sample was already started.
     * @param dataset the dataset
     * @param sample the sample
     * @return whether an export for this dataset and sample was already started
     */
    private boolean isStudentProblemExportStarted(DatasetItem dataset, SampleItem sample) {
        boolean isStarted = datasetSystemLogDao().messageCheck(dataset, sample,
                exportStartAction());

        if (!isStarted) {
            SystemLogger.log(dataset, null, sample, exportStartAction(),
                getLogPrefix(dataset, sample) + "Started Cached Student-Problem Export.",
                true, null);
        }

        return isStarted;
    }

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

            msg = getLogPrefix() + "Cached Student-Problem Export."
                + " Student batch size: " + studentBatchSize;
        } else {
            msg = "ERROR attempting a cached student-problem export for "
                + getLogPrefix() + " Batch size: " + studentBatchSize;
        }
        SystemLogger.log(info.getDataset(), null, info.getSample(), exportCompletedAction(),
                msg, success, value, elapsedTime);
    }

    /**
     * Delete the given file from the cached export directory for the given dataset.
     * @param fileName the name of the file to be deleted.
     * @return true if the file is successfully deleted, false otherwise.
     */
    public Boolean deleteFile(String fileName) {
        logDebug("Path to file is ", fileName);
        return new File(fileName).delete();
    }

    /** Constant for Student-Problem Export file names. */
    private static final String STU_PROBLEM_EXPORT_PREFIX = "_student_problem_";

    /**
     * Get string to prepend to the name of cached files.
     * @return file name prefix
     */
    @Override
    protected String getFileNamePrefix() {
        return getDatasetInfoPrefix() + STU_PROBLEM_EXPORT_PREFIX;
    }

    /**
     * Return a nicely formatted string for logging purposes.
     * @param dataset the dataset.
     * @return a nicely formatted string.
     */
    public String formatForLogging(DatasetItem dataset) {
        return (dataset.getDatasetName() + " (" + dataset.getId() + ")");
    }

    /**
     * Return a nicely formatted string for logging purposes.
     * @param sample the sample.
     * @return a nicely formatted string.
     */
    public String formatForLogging(SampleItem sample) {
        return (sample.getSampleName() + " (" + sample.getId() + ")");
    }

    /**
     * Returns a nicely formatted string (including dataset and sample)
     * logging purposes.
     * @param dataset the dataset.
     * @param sample the sample
     * @return a nicely formatted string.
     */
    public String formatForLogging(DatasetItem dataset, SampleItem sample) {
        return "Dataset " + formatForLogging(dataset) + " Sample " + formatForLogging(sample);
    }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param content sample and/or dataset label
     * @return a string useful for logging
     */
    private String getLogPrefix(String content) {
        return "CFG [" + content + "] : ";
    }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param dataset the dataset.
     * @param sample the sample
     * @return a string useful for logging
     */
    private String getLogPrefix(DatasetItem dataset, SampleItem sample) {
        return getLogPrefix(formatForLogging(dataset) + " / " + formatForLogging(sample));
    }

    /**
     * Utility method to consistently create a prefix of the dataset name
     * and id and the sample name and id for log4j logging.
     * @param info to get the dataset and sample
     * @return a string useful for logging
     */
    private String getLogPrefix(CachedFileInfo info) {
        return getLogPrefix(info.getDataset(), info.getSample());
    }

    /**
     * Utility method to consistently create a prefix of the
     * sample name and id for log4j logging.
     * @param sample the sample
     * @return a string useful for logging
     */
    private String getLogPrefix(SampleItem sample) {
        return getLogPrefix(formatForLogging(sample));
    }

    /**
     * Only log if info is enabled.  Prepend the log prefix to the message.
     * @param info to get the dataset and sample
     * @param args concatenate all arguments into the string to be logged
     */
    public void logInfo(CachedFileInfo info, Object... args) {
        if (logger.isInfoEnabled()) {
            logInfo(getLogPrefix(info), concatenate(args));
        }
    }

    /**
     * System log action indicating that the export is finished.
     * @return system log action indicating that the export is finished
     */
    @Override
    protected String exportCompletedAction() {
        return ACTION_CACHED_PROBLEM_EXPORT;
    }

    /**
     * System log action indicating that the export started.
     * @return system log action indicating that the export is finished
     */
    @Override
    protected String exportStartAction() {
        return ACTION_CACHED_PROBLEM_EXPORT_START;
    }

} // end StudentProblemExportTask inner class