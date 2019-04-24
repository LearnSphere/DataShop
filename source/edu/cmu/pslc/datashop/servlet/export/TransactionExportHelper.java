/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_TX_EXPORT;
import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_CACHED_TX_EXPORT_START;
import static edu.cmu.pslc.datashop.util.FileUtils.cleanForFileSystem;
import static edu.cmu.pslc.datashop.util.FileUtils.countLines;
import static edu.cmu.pslc.datashop.util.FileUtils.dumpToFile;
import static edu.cmu.pslc.datashop.util.FormattingUtils.displayObject;
import static edu.cmu.pslc.datashop.util.FormattingUtils.formatForSeconds;
import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.util.StringUtils.stripChars;
import static edu.cmu.pslc.datashop.util.UtilConstants.MAGIC_1000;
import static edu.cmu.pslc.logging.util.DateTools.getElapsedTimeString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TxExportDao;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.dto.ExportCache;
import edu.cmu.pslc.datashop.dto.TxExportInfo;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.CfTxLevelId;
import edu.cmu.pslc.datashop.item.ClassItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.FeedbackItem;
import edu.cmu.pslc.datashop.item.ProblemEventItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.SessionItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Helper class used for assisting with transaction export file creation and file
 * caching(not related to ExportCache.java). This class is a singleton.
 *
 * @author Kyle A Cunningham
 * @version $Revision: 12107 $
 * <BR>Last modified by: $Author: epennin $
 * <BR>Last modified on: $Date: 2015-03-24 20:30:09 -0400 (Tue, 24 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class TransactionExportHelper {
    /** Time stamp format. */
    public static final FastDateFormat TIME_STAMP_FMT
            = FastDateFormat.getInstance("yyyy_MMdd_HHmmss");

    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Directory structure for transaction export files. */
    private static final String TXN_EXPORT_DIR = "export/tx";
    /** Default base directory for the files associated with a dataset. */
    private static final String BASE_DIR_DEFAULT = "/datashop/dataset_files";
    /** Default delimiter. */
    private static final String DEFAULT_DELIMITER = "\t";
    /** Initial string buffer size for a single line. */
    private static final int LINE_BUFFER_SIZE = 2048;
    /** Initial size of the string buffer. */
    protected static final int STRING_BUFFER_SIZE = 262144;
    /** Empty string representation. */
    private static final String EMPTY_STRING = "";
    /** Buffer size for zip file processing. */
    public static final int BUFFER_SIZE = 18024;
    /** Number of transactions to process at one time. */
    private static final int BATCH_SIZE = 10000;
    /** Number of milliseconds in a second. */
    private static final int MILLISECONDS_PER_SECOND = 1000;
    /** Used to wrap export in a callback.*/
    private TransactionTemplate transactionTemplate;
    /** Regular Expression to identify timestamps in cached file names. */
    public static final String TIME_STAMP_REGEX =
        Pattern.compile("_[0-9]{4}+_[0-9]{2}+[0-9]{2}+_[0-9]{2}+[0-9]{2}+[0-9]{2}+.zip",
            Pattern.UNICODE_CASE).toString();

    /** Hibernate Session factory for fine grain session control. */
    private SessionFactory sessionFactory;

    /** Default Constructor. */
    public TransactionExportHelper() { };

    /**
     *  Returns sessionFactory.
     *  @return Returns the sessionFactory.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Set sessionFactory.
     * @param sessionFactory The sessionFactory to set.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Used to wrap export in a callback.
     * @param transactionTemplate used to wrap export in a callback
     */
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Convenience method to get the Sample Metric DAO.
     * @return the Sample Metric DAO
     */
    public SampleMetricDao sampleMetricDao() {
        return DaoFactory.DEFAULT.getSampleMetricDao();
    }

    /**
     * Set up the ExportCache for the export.
     * @param theSample the sample being exported.
     * @param cache the cache item
     * @return a processed export cache for the sample
     */
    public ExportCache initExportCache(SampleItem theSample, ExportCache cache) {
        DatasetItem datasetItem = theSample.getDataset();
        Integer datasetID = (Integer)datasetItem.getId();
        datasetItem = DaoFactory.DEFAULT.getDatasetDao().get(datasetID);
        cache.setMaxStudentCount(sampleMetricDao().getMaximumStudents(theSample));

        List datasetLevelTitles = DaoFactory.DEFAULT.getDatasetLevelDao()
            .getDatasetLevelTitles(datasetID);
        datasetLevelTitles = cleanDatasetLevelTitles(datasetLevelTitles);
        if (datasetLevelTitles == null || datasetLevelTitles.size() == 0) {
            datasetLevelTitles.add("Default");
        } else {
            if (datasetLevelTitles.size() == 1 && datasetLevelTitles.get(0) == null) {
                datasetLevelTitles.clear();
                datasetLevelTitles.add("Default");
            }
        }
        cache.setDatasetLevelTitles(datasetLevelTitles);

        cache.setMaxConditionCount(sampleMetricDao().getMaximumConditions(theSample));
        cache.setMaxSelectionCount(sampleMetricDao().getMaximumSelections(theSample));
        cache.setMaxActionCount(sampleMetricDao().getMaximumActions(theSample));
        cache.setMaxInputCount(sampleMetricDao().getMaximumInputs(theSample));
        cache.setCustomFieldNames(DaoFactory.DEFAULT.getCustomFieldDao()
                .getCustomFieldNames(datasetID));

        List<SkillModelItem> skillModels = DaoFactory.DEFAULT.getSkillModelDao().find(datasetItem);
        for (SkillModelItem skillModel : skillModels) {
            Long maxSkillCount = sampleMetricDao().getMaximumSkills(theSample, skillModel);
            String skillModelName = skillModel.getSkillModelName();
            cache.addToSkillCounts(skillModelName, maxSkillCount);
            cache.addToSkillModelNames(skillModelName);
        }
        return cache;
    }

    /**
     * Creates a List of the column headers.
     * @param cache the export cache for this sample (can be null)
     * @param sampleList the list of samples
     * @param maxSkillValues a list of something or another
     * @return a StringBuffer of the list of headers.
     */
    public List<String> getHeaderColumns(ExportCache cache, List<SampleItem> sampleList,
                            List<SkillModelWithMaxSkills> maxSkillValues) {

        int numDelimitersBeforeProblem = 0;
        List<String> headers = new ArrayList<String>();

        headers.add("Row");
        headers.add("Sample Name");
        numDelimitersBeforeProblem++;
        headers.add("Transaction Id");
        numDelimitersBeforeProblem++;

        Integer tempCounter = new Integer(0);
        while (tempCounter < cache.getMaxStudentCount()) {
            headers.add("Anon Student Id");
            tempCounter++;
            numDelimitersBeforeProblem++;
        }
        tempCounter = 0;
        headers.add("Session Id");
        numDelimitersBeforeProblem++;
        headers.add("Time");
        numDelimitersBeforeProblem++;
        headers.add("Time Zone");
        numDelimitersBeforeProblem++;
        headers.add("Duration (sec)");
        numDelimitersBeforeProblem++;
        headers.add("Student Response Type");
        numDelimitersBeforeProblem++;
        headers.add("Student Response Subtype");
        numDelimitersBeforeProblem++;
        headers.add("Tutor Response Type");
        numDelimitersBeforeProblem++;
        headers.add("Tutor Response Subtype");
        numDelimitersBeforeProblem++;

        List datasetLevelTitles = cache.getDatasetLevelTitles();
        for (Iterator it = datasetLevelTitles.iterator(); it.hasNext();) {
            String title = (String) it.next();
            headers.add("Level (" + title + ")");
            numDelimitersBeforeProblem++;
        }

        cache.setNumDelimitersBeforeProblem(numDelimitersBeforeProblem);
        headers.add("Problem Name");
        headers.add("Problem View");
        headers.add("Problem Start Time");
        headers.add("Step Name");
        headers.add("Attempt At Step");
        headers.add("Is Last Attempt");
        headers.add("Outcome");
        while (tempCounter < cache.getMaxSelectionCount()) {
            headers.add("Selection");
            tempCounter++;
        }
        tempCounter = 0;
        while (tempCounter < cache.getMaxActionCount()) {
            headers.add("Action");
            tempCounter++;
        }
        tempCounter = 0;
        while (tempCounter < cache.getMaxInputCount()) {
            headers.add("Input");
            tempCounter++;
        }
        tempCounter = 0;
        headers.add("Feedback Text");
        headers.add("Feedback Classification");
        headers.add("Help Level");
        headers.add("Total Num Hints");
        while (tempCounter < cache.getMaxConditionCount()) {
            headers.add("Condition Name");
            headers.add("Condition Type");
            tempCounter++;
        }

        // All skill model names belonging to the dataset
        List<String> skillModelNames = cache.getSkillModelNames();
        // Skill counts for the cached sample only
        HashMap<String, Long> skillCounts = cache.getSkillCounts();

        List<String> mergedSkillModelNames = new ArrayList<String>();
        List<Long> mergedMaxSkillCounts = new ArrayList<Long>();

        if (maxSkillValues != null) {
            for (SkillModelWithMaxSkills wrapper : maxSkillValues) {
                tempCounter = 0;
                Long maximumSkills = wrapper.getMaxSkillValue();
                String skillModelName = wrapper.getSkillModelName();
                mergedSkillModelNames.add(skillModelName);
                mergedMaxSkillCounts.add(maximumSkills);
                while (tempCounter < maximumSkills) {
                    headers.add("KC (" + skillModelName + ")");
                    headers.add("KC Category (" + skillModelName + ")");
                    tempCounter++;
                }
            }
            // Bind to export cache so they are available to the getTransactionData function
            cache.setMergedSkillCounts(mergedMaxSkillCounts);
            cache.setMergedSkillModelNames(mergedSkillModelNames);
        } else {
            for (String skillModelName : skillModelNames) {
                tempCounter = 0;
                Long maximumSkills = skillCounts.get(skillModelName);
                while (tempCounter < maximumSkills) {
                    headers.add("KC (" + skillModelName + ")");
                    headers.add("KC Category (" + skillModelName + ")");
                    tempCounter++;
                }
            }
        }

        tempCounter = 0;
        headers.add("School");
        headers.add("Class");
        List customFieldNames = cache.getCustomFieldNames();
        for (int i = 0; i < customFieldNames.size(); i++) {
            String cfDomId = "cf_hash_" + ((String)customFieldNames.get(i))
                    .hashCode();
            headers.add("<span class=\"cf_header\" id=\""
                    + cfDomId
                    + "\">CF (" + (String) customFieldNames.get(i) + ")");
        }
        return headers;
    }

    /**
     * Clean up the dataset level titles by making sure there are no discrepancies
     * in capitalization (like unit vs Unit).
     * @param titleList - the list of dataset level titles
     * @return the cleaned list of dataset level titles
     */
    private ArrayList cleanDatasetLevelTitles(List <String> titleList) {
        ArrayList <String> cleanedTitles = new ArrayList();
        if (titleList.size() == 0) {
            return cleanedTitles;
        } else {
            for (String title : titleList) {
                // do a little cleanup and then see if we already have this title
                title = formatLevelTitle(title);
                if (!cleanedTitles.contains(title)) {
                    cleanedTitles.add(title);
                }
            }
        return cleanedTitles;
        }
    } // end cleanDatasetLevelTitles

    /**
     * Capitalizes the first letter of each dataset level title.
     * @param title - the title to format
     * @return the formatted level title
     */
    private String formatLevelTitle(String title) {
        if (title != null && title.length() > 0) {
            title = title.trim();
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        return title;
    }

    /**
     * Generate TxExportInfo and initialize with dataset, sample, and newly created ExportCache.
     * @param dataset the dataset
     * @param sample the sample
     * @return TxExportInfo initialized with dataset, sample, and newly created ExportCache
     * @throws IOException thrown by TxExportInfo constructor
     */
    public TxExportInfo initTxExportInfo(final DatasetItem dataset, final SampleItem sample)
    throws IOException {
        return new TxExportInfo() { {
            setDataset(dataset);
            setSample(sample);
            setExportCache(initExportCache(sample, new ExportCache()));
        } };
    }

    /** Convenience method for getting the Sample DAO.
     * @return the Sample DAO. */
    public SampleDao sampleDao() {
        return DaoFactory.DEFAULT.getSampleDao();
    }

    /**
     * Number of transactions for sample.
     * @param sample the sample
     * @return number of transactions for sample
     */
    private long transactionsCount(SampleItem sample) {
        return sampleMetricDao().getTotalTransactions(sample);
    }

    /**
     * Calculate and log how many transactions per second were handled in the last batch.
     * @param batch number of transactions in the last batch batch
     * @param numCompletedRows total number of transactions processed so far
     */
    private void logTransactionsCompleted(long batch, long numCompletedRows) {
        logInfo("Completed ", numCompletedRows, " transactions total, recent batch of ", batch);
    }

    /**
     * Create a sample export task with info.
     * @param info carries all the data we need to perform a cached file export
     * @return a sample export task with info
     */
    public TxExportTask txExportTask(TxExportInfo info) {
        return new TxExportTask(info);
    }

    /**
     * Convenience method for logging a cached transaction export message to the dataset system
     * log.
     * @param info carries all the data we need to perform a cached file export
     * @param msg the message to log
     * @param success indicates whether the action completed successfully
     * @param elapsedTime the elapsed time in milliseconds
     */
    private void systemLog(TxExportInfo info, String msg, boolean success, Long elapsedTime) {
        int value = (new Long(info.getNumCompletedRows())).intValue();
        SystemLogger.log(info.getDataset(), null, info.getSample(), ACTION_CACHED_TX_EXPORT,
                getLogPrefix(info) + msg + " Student batch size: " + info.getStudentBatchSize(),
                success, value, elapsedTime);
    }

    /**
     * Process a single batch of transactions.
     * @param sample the sample to process.
     * @param offset the current offset.
     * @param batchSize the size of the batch to get.
     * @return List of transaction to process.
     */
    public List<TransactionItem> getBatch(SampleItem sample, int offset, int batchSize) {
        Date start = null;
        if (logger.isDebugEnabled()) { start = new Date(); }

        List<TransactionItem> transactions = sampleDao().getTransactions(sample, batchSize,
                offset);
        if (logger.isDebugEnabled()) {
            Long time = (new Date()).getTime() - start.getTime();
            logDebug(sample, "Got transaction list of size ", transactions.size(), " for sample ",
                    sample.getId(), " starting from number ", offset, " in ",
                    (int)(time / MAGIC_1000), " seconds");
            logDebug(sample, "offset was: ", offset, ", batchSize was: ", batchSize);
        }
        return transactions;
    }

    /**
     * Process a single batch of transactions and return the number of transactions processed.
     * @param tempFile the temporary file to write to.
     * @param offset get transactions starting from here
     * @param batchSize get this many transactions
     * @param transactions read transactions from here, if not null
     * (used only when using the stored procedure)
     * @return the number of transactions processed
     * @throws SQLException thrown by methods on transactions ResultSet
     * @throws IOException thrown by dumpToFile
     */
    public int processBatch(long offset, int batchSize, File tempFile, ResultSet transactions)
    throws SQLException, IOException {
        StringBuffer textBuffer = new StringBuffer(STRING_BUFFER_SIZE);
        int rowno = 0;

        for (int i = 0; !transactions.isLast() && i < batchSize; i++) {
            transactions.next();

            final int numCols = transactions.getMetaData().getColumnCount();
            Object[] cols = new Object[numCols];

            cols[0] = offset + (++rowno);
            for (int col = 1; col < numCols; col++) {
                Object obj = transactions.getObject(col + 1);
                if (obj instanceof Date) {
                    cols[col] = displayObject(obj);
                } else if (obj instanceof Boolean) {
                    Boolean b = (Boolean)obj;
                    cols[col] = (b ? "1" : "0");
                } else {
                    cols[col] = obj;
                }
            }

            textBuffer.append("\n");
            textBuffer.append(join("\t", cols));
        }
        dumpToFile(textBuffer, tempFile, true);

        return rowno;
    }

    /**
     * Will list all transaction information as a single delimited row.
     * @param sample the sample we are getting transaction for.
     * @param transaction the TransactionItem to process.
     * @param delimiter a string of the desired delimiter.
     * @param cache the export cache for this sample.
     * @param calledFromBean flag indicating if this method is called from the export bean.
     * @return a StringBuffer of the row.
     */
    public StringBuffer processTransaction(SampleItem sample, TransactionItem transaction,
                    String delimiter, ExportCache cache, boolean calledFromBean) {

        List columns = getTransactionData(sample, transaction, cache, calledFromBean);
        delimiter = (delimiter == null) ? DEFAULT_DELIMITER : delimiter;

        StringBuffer buffer = new StringBuffer(LINE_BUFFER_SIZE);
        int n = columns.size(), i = 0;
        for (Object object : columns) {
            buffer.append(displayObject(object) + ((i < n) ? delimiter : ""));
            i++;
        }
        return buffer;
    }

    /**
     * Gets the transaction export as a List of the columns.
     * @param sample the same we are getting transactions for.
     * @param trans the transaction we are getting columns for
     * @param cache the {@link ExportCache}
     * @param calledFromBean flag indicating if this method was called from the export bean.
     * @return a List of columns.
     */
    public List<String> getTransactionData(SampleItem sample,
            TransactionItem trans, ExportCache cache, boolean calledFromBean) {

        Integer numDelimitersUsed = new Integer(0);
        Session session = sessionFactory.openSession();
        trans = (TransactionItem)session.get(TransactionItem.class, (Long)trans.getId());

        List<String> columns = new ArrayList<String>();
        SessionItem sessionItem = trans.getSession();
        SessionItem cachedSessionItem = cache.getSession((Long)sessionItem.getId());
        if (cachedSessionItem != null) {
            sessionItem = cachedSessionItem;
        } else {
            cache.addToSessions(sessionItem);
        }
        columns.add(displayObject(sample.getSampleName()));
        numDelimitersUsed++;

        columns.add(displayObject(trans.getGuid()));
        numDelimitersUsed++;
        //student

        StudentItem studentItem = sessionItem.getStudent();
        StudentItem cachedStudentItem = cache.getStudent((Long)studentItem.getId());
        if (cachedStudentItem != null) {
            studentItem = cachedStudentItem;
        } else {
            cache.addToStudents(studentItem);
        }

        columns.add(stripChars(studentItem.getAnonymousUserId()));
        numDelimitersUsed++;
        columns.add(displayObject(sessionItem.getSessionTag()));
        numDelimitersUsed++;
        columns.add(displayObject(trans.getTransactionTime()));
        numDelimitersUsed++;
        columns.add(displayObject(trans.getTimeZone()));
        numDelimitersUsed++;
        columns.add(displayObject(formatForSeconds(trans.getDuration()), true));
        numDelimitersUsed++;
        columns.add(displayObject(trans.getTransactionTypeTool()));
        numDelimitersUsed++;
        columns.add(displayObject(trans.getTransactionSubtypeTool()));
        numDelimitersUsed++;
        columns.add(displayObject(trans.getTransactionTypeTutor()));
        numDelimitersUsed++;
        columns.add(displayObject(trans.getTransactionSubtypeTutor()));
        numDelimitersUsed++;

        ProblemItem problem = trans.getProblem();
        ProblemItem cachedProblem = cache.getProblem((Long)problem.getId());
        if (cachedProblem != null) {
            problem = cachedProblem;
        } else {
            cache.addToProblems(problem);
        }

        DatasetLevelItem level = problem.getDatasetLevel();
        List levelsList = getDatasetLevelColumns(level, cache);

        columns.addAll(levelsList);
        numDelimitersUsed += levelsList.size();

        // it may be the case that the dataset levels are funky,
        // so make sure we are at the correct spot before we write the problem name.
        while (numDelimitersUsed < cache.getNumDelimitersBeforeProblem()) {
            columns.add("");
            numDelimitersUsed++;
        }

        columns.add(stripChars(problem.getProblemName()));

        ProblemEventItem problemEventItem = trans.getProblemEvent();
        if (problemEventItem != null) {
            columns.add(displayObject(problemEventItem.getProblemView()));
            columns.add(displayObject(problemEventItem.getStartTime()));
        } else {
            columns.add("");
            columns.add("");
        }

        SubgoalItem subgoal = trans.getSubgoal();
        if (subgoal != null) {
            columns.add(stripChars(subgoal.getSubgoalName()));
        } else {
            columns.add("");
        }

        Integer attemptAtSubgoal = trans.getAttemptAtSubgoal();
        if (attemptAtSubgoal == null) {
            columns.add("");  // 'Attempt At Step' value
            columns.add("");  // 'Is Last Attempt' value
        } else {
            columns.add(attemptAtSubgoal.toString());
            columns.add(trans.getIsLastAttempt() ? "1" : "0");
        }

        columns.add(displayObject(trans.getOutcome()));

        SubgoalAttemptItem subgoalAttempt = trans.getSubgoalAttempt();

        //attempt selection
        Integer tempSelectionCount = new Integer(0);
        List subgoalAttemptSelectionList = subgoalAttempt.getAttemptSelectionsExternal();

        if (subgoalAttemptSelectionList.size() == 0) {
            while (tempSelectionCount < cache.getMaxSelectionCount()) {
                columns.add(""); // Condition Name
                tempSelectionCount++;
            }
        } else {
            for (Iterator saIt = subgoalAttemptSelectionList.iterator();
                saIt.hasNext();) {
                AttemptSelectionItem attemptSelection = (AttemptSelectionItem) saIt.next();
                String selectionCellValue = getSelectionCellValue(attemptSelection);
                columns.add(stripChars(selectionCellValue));
                tempSelectionCount++;
                if (!saIt.hasNext()) {
                    while (tempSelectionCount < cache.getMaxSelectionCount()) {
                        columns.add("");
                        tempSelectionCount++;
                    }
                }
            }
        } // end else

        //attempt action
        Integer tempActionCount = new Integer(0);
        List subgoalAttemptActionsList = subgoalAttempt.getAttemptActionsExternal();
        if (subgoalAttemptActionsList.size() == 0) {
            while (tempActionCount < cache.getMaxActionCount()) {
                columns.add("");
                tempActionCount++;
            }
        } else {
            for (Iterator aaIt = subgoalAttemptActionsList.iterator();
                aaIt.hasNext();) {
                AttemptActionItem attemptAction = (AttemptActionItem) aaIt.next();
                String actionCellValue = getActionCellValue(attemptAction);
                columns.add(stripChars(actionCellValue));
                tempActionCount++;
                if (!aaIt.hasNext()) {
                    while (tempActionCount < cache.getMaxActionCount()) {
                        columns.add("");
                        tempActionCount++;
                    }
                }
            }
        } // end else

        //attempt input
        Integer tempInputCount = new Integer(0);
        List subgoalAttemptInputsList = subgoalAttempt.getAttemptInputsExternal();
        if (subgoalAttemptInputsList.size() == 0) {
            while (tempInputCount < cache.getMaxInputCount()) {
                columns.add("");
                tempInputCount++;
            }
        } else {
            for (Iterator aiIt = subgoalAttemptInputsList.iterator();
                aiIt.hasNext();) {
                AttemptInputItem attemptInput = (AttemptInputItem) aiIt.next();
                String inputCellValue = getInputCellValue(attemptInput);
                columns.add(stripChars(inputCellValue));
                tempInputCount++;
                if (!aiIt.hasNext()) {
                    while (tempInputCount < cache.getMaxInputCount()) {
                        columns.add("");
                        tempInputCount++;
                    }
                }
            }
        } // end else

        FeedbackItem feedback = trans.getFeedback();
        if (feedback == null) {
            columns.add(""); // feedback text column
            columns.add(""); // feedback classification column
        } else {
            columns.add(displayObject(feedback.getFeedbackText()));
            columns.add(displayObject(feedback.getClassification()));
        }
        columns.add(displayObject(trans.getHelpLevel()));
        columns.add(displayObject(trans.getTotalNumHints()));

        Integer tempConditionCount = new Integer(0);
        if (cache.getMaxConditionCount() != 0) {
            List conditionList = trans.getConditionsExternal();
            if (conditionList.size() == 0) {
                while (tempConditionCount < cache.getMaxConditionCount()) {
                    columns.add(""); // Condition Name
                    columns.add(""); // Condition Type
                    tempConditionCount++;
                }
            } else {
                for (Iterator cIt = conditionList.iterator(); cIt.hasNext();) {
                    ConditionItem condition = (ConditionItem) cIt.next();

                    ConditionItem cachedCondition =
                        cache.getCondition((Long)condition.getId());
                    if (cachedCondition != null) {
                        condition = cachedCondition;
                    } else {
                        cache.addToConditions(condition);
                    }

                    columns.add(stripChars(condition.getConditionName()));
                    String conditionType = stripChars(condition.getType());
                    if (conditionType == null) {
                        columns.add("");
                    } else {
                        columns.add(conditionType);
                    }
                    tempConditionCount++;
                    if (!cIt.hasNext()) {
                        while (tempConditionCount < cache.getMaxConditionCount()) {
                            columns.add(""); // Condition Name
                            columns.add(""); // Condition Type
                            tempConditionCount++;
                        }
                    }
                }
            }
        }

        // maxSkillCounts doesn't need to be a map because we are preserving
        // the ordering by skill model id and can do constant O(1) lookups
        List<Long> maxSkillCounts = cache.getMergedSkillCounts();
        int skillHeadersSize = maxSkillCounts.size();

        // A merge of KC headers is necessary.
        if (skillHeadersSize != 0 && !calledFromBean) {
            List<String> skillModelNames = cache.getMergedSkillModelNames();
            // Changed to not loop through every skill, for every model, trusting the sorted
            // skillList and don't look at previously used skills.
            int lastSkillPos = 0;
            // Retrieve this transaction's sorted skill list
            List<SkillItem> skillList = trans.getSkillsExternal();
            int skillListSize = skillList.size();
            // Loop through all of the skill model headers
            for (int count = 0; count < skillHeadersSize; count++) {
                String currentSkillModelName = skillModelNames.get(count);
                Long currentMaxSkillCount = maxSkillCounts.get(count);
                int skillCount = 0;
                // Iterate through skills starting at lastSkillPos offset
                for (; skillCount < currentMaxSkillCount
                        && lastSkillPos < skillListSize; skillCount++) {
                    SkillItem currentSkill = skillList.get(lastSkillPos);
                    SkillItem cachedSkill = cache.getSkill((Long) currentSkill.getId());
                    if (cachedSkill != null) {
                        currentSkill = cachedSkill;
                    } else {
                        cache.addToSkills(currentSkill);
                    }
                    String skillModelName = currentSkill.getSkillModel().getSkillModelName();
                    if (skillModelName.equals(currentSkillModelName)) {
                        String skillName = stripChars(currentSkill.getSkillName());
                        String skillCategory = stripChars(currentSkill.getCategory());
                        columns.add((skillName == null) ? "" : skillName);
                        columns.add((skillCategory == null) ? "" : skillCategory);
                        lastSkillPos++;
                    } else {
                        while (skillCount < currentMaxSkillCount) {
                            columns.add(""); // KC Area
                            columns.add(""); // KC Category
                            skillCount++;
                        }
                    }
                }
                // All skills for this transaction have been reached, only blanks left for
                // this model.
                while (skillCount < currentMaxSkillCount) {
                    columns.add(""); // KC Area
                    columns.add(""); // KC Category
                    skillCount++;
                }
            }
        } else {
            HashMap<String, Long> cachedSkillCounts = cache.getSkillCounts();
            int lastPos = 0;
            if (cachedSkillCounts.size() != 0) {
                List<SkillItem> skillList = trans.getSkillsExternal();
                List<String> cachedSkillModelNames = cache.getSkillModelNames();
                int skillListSize = skillList.size();
                for (int count = 0, numSkillModels = cachedSkillModelNames.size();
                        count < numSkillModels; count++) {
                    String currentSkillModelName = cachedSkillModelNames.get(count);
                    Long currentMaxSkillCount = cachedSkillCounts.get(currentSkillModelName);
                    int skillCount = 0;
                    for (; skillCount < currentMaxSkillCount
                            && lastPos < skillListSize; skillCount++) {
                        SkillItem skill = skillList.get(lastPos);
                        SkillItem cachedSkill = cache.getSkill((Long) skill.getId());
                        if (cachedSkill != null) {
                            skill = cachedSkill;
                        } else {
                            cache.addToSkills(skill);
                        }
                        String skillModelName = skill.getSkillModel().getSkillModelName();
                        if (skillModelName.equals(currentSkillModelName)) {
                            String skillName = stripChars(skill.getSkillName());
                            String skillCategory = stripChars(skill.getCategory());
                            columns.add((skillName == null) ? "" : skillName);
                            columns.add((skillCategory == null) ? "" : skillCategory);
                            lastPos++;
                        } else {
                            while (skillCount < currentMaxSkillCount) {
                                columns.add(""); // KC Area
                                columns.add(""); // KC Category
                                skillCount++;
                            }
                        }
                    }
                    while (skillCount < currentMaxSkillCount) {
                        columns.add(""); // KC Area
                        columns.add(""); // KC Category
                        skillCount++;
                    }
                } // end for
            } // end if (skillCounts.size())
        }

        SchoolItem school = trans.getSchool();
        if (school != null) {
            SchoolItem cachedSchool = cache.getSchool((Integer)school.getId());
            if (cachedSchool != null) {
                school = cachedSchool;
            } else {
                cache.addToSchool(school);
            }
            columns.add(stripChars(trans.getSchool().getSchoolName()));
        } else {
            columns.add("");
        }

        ClassItem classItem = trans.getClassItem();
        if (classItem != null) {
            ClassItem cachedClass = cache.getClassItem((Long)classItem.getId());
            if (cachedClass != null) {
                classItem = cachedClass;
            } else {
                cache.addToClasses(classItem);
            }
            columns.add(stripChars(classItem.getClassName()));
        } else {
            columns.add("");
        }

        List cacheCustomFieldNames = cache.getCustomFieldNames();
        if (cacheCustomFieldNames.size() != 0) {
            List cfTxLevels = trans.getCfTxLevelsExternal();

            if (cfTxLevels.size() == 0) {
                for (int i = 0, n = cacheCustomFieldNames.size(); i < n; i++) {
                    columns.add("");
                }
            } else {
                for (int i = 0, n = cacheCustomFieldNames.size(); i < n; i++) {
                    String customFieldName = (String) cacheCustomFieldNames.get(i);
                    boolean found = false;
                    for (Iterator cfIt = cfTxLevels.iterator(); cfIt.hasNext();) {
                        CfTxLevelItem cfLevel = (CfTxLevelItem)cfIt.next();
                        CfTxLevelItem cachedCF = cache.getCfTxLevel((CfTxLevelId)cfLevel.getId());
                        if (cachedCF != null) {
                                cfLevel = cachedCF;
                        } else {
                            cache.addToCfTxLevels(cfLevel);
                        }

                        String cfName = cfLevel.getCustomField().getCustomFieldName();
                        // add the data as-is if the call is from the bean,
                        // otherwise escape for page grid display.
                        if (cfName.equals(customFieldName)) {
                            if (calledFromBean) {
                                columns.add(stripChars(cfLevel.getValue()));
                            } else {
                                columns.add(StringEscapeUtils.escapeHtml(
                                    (stripChars(cfLevel.getValue()))));
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found && i < n) {
                        columns.add("");
                    }
                }
            }
        }

        //close the session to release the objects for garbage collection.
        session.close();
        return columns;
    }

    /**
     * Create a CachedExportFileReader for the sample.
     * @param sample the sample
     * @param baseDir directory containing all of the cached export files
     * @return a CachedExportFileReader for the sample
     */
    public CachedExportFileReader cachedFileReader(SampleItem sample, String baseDir) {
        // call to DatasetSystemLog to determine if sample is list in to cache.
        String fileName = getCachedFileName(sample, baseDir);
        return fileName == null ? null : new CachedExportFileReader(fileName);
    }

    /**
     * Create a CachedExportFileReader for the sample for web services.
     * @param sample the sample
     * @param baseDir directory containing all of the cached export files
     * @return a CachedExportFileReader for the sample for web services
     */
    public CachedExportFileReader cachedFileReaderRaw(SampleItem sample, String baseDir) {
        // call to DatasetSystemLog to determine if sample is list in to cache.
        String fileName = getCachedFileName(sample, baseDir);
        return fileName == null ? null : new CachedExportFileReader(fileName, true);
    }

    /**
     * Processes a dataset level in order to get the hierarchy as a List in the
     * appropriate columns.
     * @param level the base level from the transaction problem to process.
     * @param cache the {@link ExportCache}
     * @return List of dataset level columns
     */
    private List getDatasetLevelColumns(DatasetLevelItem level, ExportCache cache) {
        DatasetLevelItem cachedDatasetLevel = cache.getDatasetLevel((Integer)level.getId());
        if (cachedDatasetLevel != null) {
            level = cachedDatasetLevel;
        } else {
            cache.addToDatasetLevels(level);
        }

        // build the level hierarchy back to front.
        // check to make sure that the titles match otherwise insert a delimiter
        List levelList = new ArrayList();
        int invertedIndex = 0;
        List invertedLevelTitles = cache.getInvertedDatasetLevelTitles();
        do {
            String levelTitle = level.getLevelTitle();
            if (levelTitle == null) {
                levelTitle = "Default";
            } else {
                levelTitle = formatLevelTitle(levelTitle);
            }

            if (levelTitle.equals(invertedLevelTitles.get(invertedIndex))) {
                levelList.add(0, stripChars(level.getLevelName()));
                level = level.getParent();
            } else {
                levelList.add(0, "");
            }

            invertedIndex++;
            if (invertedIndex == invertedLevelTitles.size()) {
                break;
            }
        } while (level != null);

        while (levelList.size() != invertedLevelTitles.size()) {
            // pad with spacer...
            logDebug("levelList.size = ", levelList.size(), "... adding spacer...");
            levelList.add(0, "");
        }

        return levelList;
    }

    /**
     * Takes the attempt selection value and formats a string for output
     * including the selection value, xml_id (if available) and type (if
     * available).
     * @param item - the selection
     * @return the formatted string
     */
    private String getSelectionCellValue(AttemptSelectionItem item) {
        String result = "";
        result = item.getSelection();
        String xmlId = item.getXmlId();
        String type = item.getType();
        boolean idFound = false;
        boolean typeFound = false;

        if (xmlId != null) {
            if (xmlId != EMPTY_STRING) {
                result += " (" + xmlId;
                idFound = true;
            }
        }
        if (type != null) {
            if (type != EMPTY_STRING) {
                if (idFound) {
                    result += ", " + type + ")";
                } else {
                    result += " (" + type + ")";
                }
                typeFound = true;
            }
        }
        if (idFound && !typeFound) {
            result += ")";
        }
        return result;
    }

    /**
     * Takes the attempt action value and formats a string for output
     * including the action value, xml_id (if available) and type (if
     * available).
     * @param item - the action
     * @return the formatted string
     */
    private String getActionCellValue(AttemptActionItem item) {
        String result = "";
        result = item.getAction();
        String xmlId = item.getXmlId();
        String type = item.getType();
        boolean idFound = false;
        boolean typeFound = false;

        if (xmlId != null) {
            if (xmlId != EMPTY_STRING) {
                result += " (" + xmlId;
                idFound = true;
            }
        }
        if (type != null) {
            if (type != EMPTY_STRING) {
                if (idFound) {
                    result += ", " + type + ")";
                } else {
                    result += " (" + type + ")";
                }
                typeFound = true;
            }
        }
        if (idFound && !typeFound) {
            result += ")";
        }
        return result;
    }

    /**
     * Takes the input selection value and formats a string for output
     * including the input value, xml_id (if available) and type (if
     * available).
     * @param item - the input
     * @return the formatted string
     */
    private String getInputCellValue(AttemptInputItem item) {
        String result = "";
        result = item.getInput();
        String xmlId = item.getXmlId();
        String type = item.getType();
        boolean idFound = false;
        boolean typeFound = false;

        if (xmlId != null) {
            if (xmlId != EMPTY_STRING) {
                result += " (" + xmlId;
                idFound = true;
            }
        }
        if (type != null) {
            if (type != EMPTY_STRING) {
                if (idFound) {
                    result += ", " + type + ")";
                } else {
                    result += " (" + type + ")";
                }
                typeFound = true;
            }
        }
        if (idFound && !typeFound) {
            result += ")";
        }
        return result;
    }

    /** Convenience method to get the Dataset System Log DAO.
     * @return the Dataset System Log DAO. */
    private DatasetSystemLogDao datasetSystemLogDao() {
        return DaoFactory.DEFAULT.getDatasetSystemLogDao();
    }

    /**
     * Gets the samples queued to cache transactions.
     * @param dataset the dataset
     * @return a list of sample items
     */
    public List<SampleItem> getSamplesQueuedToCacheTx(DatasetItem dataset) {
            return datasetSystemLogDao().getSamplesToCacheTx(dataset);
    }

    /**
     * Gets the samples queued to cache steps.
     * @param dataset the dataset
     * @return a list of sample items
     */
    public List<SampleItem> getSamplesQueuedToCacheStep(DatasetItem dataset) {
            return datasetSystemLogDao().getSamplesToCacheStep(dataset);
    }

    /**
     * Gets the samples queued to cache problems.
     * @param dataset the dataset
     * @return a list of sample items
     */
    public List<SampleItem> getSamplesQueuedToCacheProblem(DatasetItem dataset) {
            return datasetSystemLogDao().getSamplesToCacheProblem(dataset);
    }

    /**
     * It is possible there are user-created samples that need to be cached (in the case they
     * created one but did not export it.)  Find these samples and return them for processing.
     * @param dataset the dataset we are caching.
     * @return an empty list if no samples queued, otherwise a list containing SampleItems to
     *      be cached.
     */
    public List<SampleItem> getSamplesToCacheAggSampleOnly(DatasetItem dataset) {
        return datasetSystemLogDao().getSamplesToCacheAggSampleOnly(dataset);
    }

    /**
     * Check for samples that need to be re-cached because (1) they have been modified after
     * caching or (2) the dataset has been modified.
     * @param dataset the dataset.
     * @return an empty list of no samples queued for the criteria listed above, otherwise
     *      a list containing SampleItems to be cached.
     */
    public List<SampleItem> getSamplesToCacheAggAndModifyActionsOnly(DatasetItem dataset) {
        return datasetSystemLogDao().getSamplesToCacheAggAndModifyActionsOnly(dataset);
    }

    /**
     * Check for a message in the dataset system log indicating that an export for this dataset
     * and sample was already started.
     * @param dataset the dataset
     * @param sample the sample
     * @return whether an export for this dataset and sample was already started
     */
    private boolean isTransactionExportStarted(DatasetItem dataset, SampleItem sample) {
        boolean isStarted = datasetSystemLogDao().isTransactionExportStarted(dataset, sample);

        if (!isStarted) {
            SystemLogger.log(dataset, null, sample, ACTION_CACHED_TX_EXPORT_START,
                    getLogPrefix(dataset, sample) + "Started Cached Transaction Export.",
                    true, null);
        }

        return isStarted;
    }

    /**
     * Whether the transaction export needs to be re-cached.
     * @param dataset the dataset to which the sample belongs
     * @param sample the sample to check
     * @return false if the sample needs to be re-cached, true otherwise
     */
    public boolean isUpToDate(DatasetItem dataset, SampleItem sample) {
        List<SampleItem> samplesToCache = datasetSystemLogDao().getSamplesToCacheTx(dataset);
        for (SampleItem sampleToCache : samplesToCache) {
            if (sampleToCache.getId().equals(sample.getId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Process the sample's name to make it suitable as a file name.
     * @param sample the sample
     * @return the sample's name processed to be suitable as a file name
     */
    private String cleanedSampleName(SampleItem sample) {
        // add the sample_id to the cleanedSampleName to make it unique across users
        return cleanForFileSystem(sample.getSampleName()) + "_" + sample.getId();
    }

    /** Constant for Transaction Export file names. */
    private static final String TX_EXPORT_PREFIX = "_tx_";

    /**
     * Given a SampleItem, generate the prefix for the cached export file.
     * @param dataset the dataset
     * @return the prefix containing dataset id
     */
    private String getFileNamePrefix(DatasetItem dataset) {
        String dsInfo = "ds" + (Integer)dataset.getId();
        return dsInfo + TX_EXPORT_PREFIX;
    }

    /**
     * Given a sample, checks the file system for an existing cached export file.
     * If one exists, return the file name, otherwise null.
     * @param sample the sample
     * @param baseDir base directory for the files associated with a dataset
     * @return a cached export file or null if one does not exist.
     */
    public String getCachedFileName(SampleItem sample, String baseDir) {
        String wholePath = getDirectoryPath(sample, baseDir);
        File newDirectory = new File(wholePath);

        String mostRecent = null;
        
        // Create the directory if necessary
        try {
            FileUtils.createDirectoriesWithPermissions(Paths.get(wholePath));
        } catch (IOException e) {
            logger.error("IOException when attempting to create the directory " + wholePath, e);
        }

        if (newDirectory.isDirectory()) {
            List<String> fileList = Arrays.asList(newDirectory.list());
            logDebug(sample, "directory ", newDirectory.getAbsolutePath(), " has ",
                    fileList.size(), " files");
            String cleanedSampleName = cleanedSampleName(sample);
            for (String fileName : fileList) {
                String[] split = fileName.split(TIME_STAMP_REGEX);
                if (split.length == 0) {
                    logger.warn("Encountered a problem while splitting '" + fileName + "'.");
                    return null;
                }
                if ((split[0]).contains(cleanedSampleName) && fileName.endsWith(".zip")) {
                    String actualFileName = wholePath + "/" + fileName;
                    if (mostRecent == null
                            || actualFileName.compareTo(mostRecent) > 0) {
                        if (mostRecent != null) {
                            logger.warn("Deleting duplicate/old cached file '"
                                    + mostRecent + "'");
                            deleteFile(mostRecent);
                        }
                        mostRecent = actualFileName;
                    }
                }
            }
        }
        return mostRecent;
    }

    /**
     * Given a sample, checks the file system for an existing cached export file.
     * If one exists, return the file name, otherwise null.
     * @param info carries all the data we need to perform a cached file export
     * @return a cached export file or null if one does not exist.
     */
    public String getCachedFileName(TxExportInfo info) {
        return getCachedFileName(info.getSample(), info.getBaseDir());
    }

    /**
     * Given a dataset, sample and newly created export file, create a cached version.
     * @param info carries all the data we need to perform a cached file export
     * @param tempFile the temporary file for the export
     * @return the cached export file name.
     * @throws IOException an IO exception when tempFile does not exist
     */
    public String createCachedFile(CachedFileInfo info, File tempFile) throws IOException {

        String cleanedSampleName = cleanedSampleName(info.getSample());

        if (tempFile != null) {
            // create a new zip archive for this export
            byte[] buffer = new byte[BUFFER_SIZE];
            long zipStartTime = System.currentTimeMillis();
            String wholePath = getDirectoryPath(info.getSample(), info.getBaseDir());
            String baseFileName = wholePath
                + "/" + getFileNamePrefix(info.getDataset())
                + cleanedSampleName + "_" + formattedNow();

            String zipFileName = baseFileName + ".zip";
            String txtFileName = baseFileName + ".txt";

            String justFileName = "";
            try {
                logInfo(info, "Attempting to build zip archive.");
                ZipOutputStream outputStream =
                    new ZipOutputStream(new FileOutputStream(zipFileName));
                outputStream.setLevel(Deflater.DEFAULT_COMPRESSION);
                logInfo(info, countLines(info.getTempFile()), " lines in ", info.getTempFile());
                FileInputStream inputStream = new FileInputStream(tempFile);

                int index = txtFileName.lastIndexOf("/");
                if (index < 0) {
                    index = txtFileName.lastIndexOf("\\");
                }
                if (index > 0 && txtFileName.length() >= index + 1) {
                    justFileName = txtFileName.substring(index + 1);
                }

                outputStream.putNextEntry(new ZipEntry(justFileName));

                // Transfer bytes from the current file to the ZIP file
                int length;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.closeEntry();
                inputStream.close();
                outputStream.close();
                
                FileUtils.applyDataShopPermissions(Paths.get(zipFileName));

                logInfo(info, "Finished creating zip file . ",
                        getElapsedTimeString(zipStartTime));
            } catch (IllegalArgumentException iae) {
                logErr(info, "IllegalArgumentException when trying to create a cached file ",
                        iae.getMessage());
                txtFileName =  null;
            } catch (FileNotFoundException fnfe) {
                logErr(info, "FileNotFoundException when trying to create a cached file ",
                        fnfe.getMessage());
                txtFileName =  null;
            } catch (SecurityException se) {
                logErr(info, "SecurityException when trying to create a cached file directory for ",
                        se.getMessage());
                txtFileName =  null;
            } catch (IOException ioe) {
                logErr(info, "IOException when trying to create a cached file for ",
                        ioe.getMessage());
                txtFileName =  null;
            }

            return zipFileName;
        } else {
            logErr(info, "export file is null.");
            return null;
        }
    }

    /** Create a current time stamp and format it. @return formatted time stamp of now */
    public static String formattedNow() { return TIME_STAMP_FMT.format(new Date()); }

    /**
     * Returns the directory path for the given dataset and transaction export directory.
     * @param sample the sample.
     * @param baseDir the base directory where cached export files should be stored.
     * @return the path to where the cached file should be stored.
     */
    public String getDirectoryPath(SampleItem sample, String baseDir) {
        if (baseDir == null) { baseDir = BASE_DIR_DEFAULT; }
        String result = baseDir + "/" + sample.getFilePath() + "/" + TXN_EXPORT_DIR;
        logDebug(sample, "directory path is :: ", result);
        return result;
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
     * Utility method to consistently create a prefix of the sample name and id for log4j logging.
     * @param sample the sample
     * @return a string useful for logging
     */
    private String getLogPrefix(SampleItem sample) {
        return getLogPrefix(formatForLogging(sample));
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if info is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }

    /**
     * Only log if debugging is enabled.  Prepend the log prefix to the message.
     * @param sample the sample
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(SampleItem sample, Object... args) {
        if (logger.isDebugEnabled()) {
            logDebug(getLogPrefix(sample), concatenate(args));
        }
    }

    /**
     * Only log if debugging is enabled.  Prepend the log prefix to the message.
     * @param info to get the dataset and sample
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(TxExportInfo info, Object... args) {
        if (logger.isDebugEnabled()) {
            logDebug(getLogPrefix(info), concatenate(args));
        }
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
     * Log an error message with log prefix prepended.
     * @param info to get the dataset and sample
     * @param args concatenate all arguments into the string to be logged
     */
    private void logErr(CachedFileInfo info, Object... args) {
        LogUtils.logErr(logger, getLogPrefix(info), concatenate(args));
    }

    /**
     * Presents an interface for each step of caching a transaction export file.
     * Inner class, so we can access all TransactionExportHelper methods.
     * Necessary for canceling an export, because we need to hold a reference to the
     * CallableStatement object for the export stored procedure, and TransactionExportHelper
     * is stateless by design.
     */
    public class TxExportTask {
        /** Data needed for this export. */
        private TxExportInfo info;
        /** Did the user cancel the export? */
        private boolean canceled = false;
        /** Hold on to this in case we need to cancel. */
        private CallableStatement statement;
        /** Maintains a single session to ensure access to temporary tables throughout
         *  the export process. */
        private TxExportDao txExportDao = sampleDao().getTxExportDao();

        /**
         * Create a new transaction export task.
         * @param info data needed for this export
         */
        public TxExportTask(TxExportInfo info) { this.info = info; }

        /**
         * Process all of the transactions for sample and the batch of students starting at
         * batchOffset containing at most studentBatchSize students.
         * @param batchOffset the index of the start of this batch of students
         * @throws SQLException if something goes wrong, God forbid
         * @throws IOException if something goes wrong, God forbid
         */
        private void processSample(int batchOffset) throws SQLException, IOException {
            if (canceled) { return; }

            ResultSet transactions = null;
            long batchEnd = info.getNumCompletedRows();

            try {
                // call the tx_export stored procedure and fetch the results
                String txMsg = " transaction export stored procedure with limit::"
                    + info.getStudentBatchSize() + " offset::" + batchOffset;
                logInfo(info, "Starting", txMsg);
                statement = txExportDao.prepareTxExportStatement(info.getSample(),
                        info.getStudentBatchSize(), batchOffset);
                statement.executeUpdate();
                statement = null;
                if (canceled) { return; }
                logInfo(info, "Finished", txMsg);
                batchEnd += getTxExportCount();
                transactions = txExportDao.getSPTxs(info.getSample(), BATCH_SIZE);
                // chunks the process up into BATCH_SIZE chunks to help memory handling.
                while (!canceled && info.getNumCompletedRows() < batchEnd) {
                    int lastBatch = processBatch(info.getNumCompletedRows(), BATCH_SIZE,
                            info.getTempFile(), transactions);
                    info.incrementNumCompletedRows(lastBatch);
                    logTransactionsCompleted(lastBatch, info.getNumCompletedRows());
                }
                if (canceled) { return; }
            } finally {
                if (transactions != null) {
                    if (transactions.getStatement() != null) {
                        transactions.getStatement().close();
                    }
                    transactions.close();
                }
            }
        }

        /**
         * Get the number of rows in the tx_export table.
         * @return the number of rows in the tx_export table
         */
        public long getTxExportCount() {
            return txExportDao.getTxExportCount(info.getSample());
        }

        /**
         * Load the transaction export stored procedure file, customized for this sample.
         * @return whether execution was successful
         * @throws IOException if file at filePath does not exist
         */
        public boolean loadSPs() throws IOException {
            try {
                return txExportDao.loadTxExportSP(info.getTxExportSPFilePath(), info.getSample());
            } catch (FileNotFoundException fnf) {
                throw new FileNotFoundException(
                        "Invalid path for transaction export stored procedure file: "
                        + info.getTxExportSPFilePath());
            }
        }

        /**
         * Gets the transaction export headers as a StringBuffer with the columns
         * separated by a delimiter.
         * @param delimeter string to demarcate the columns, if null uses the default delimeter
         * @return a StringBuffer of the headers.
         */
        private String buildHeadersString(String delimeter) {
            if (delimeter == null) { delimeter = DEFAULT_DELIMITER; }
            return join(delimeter, txExportDao.getSPTxHeaders(info.getSample()));
        }

        /**
         * Write the transaction headers before writing any of the export data.
         * @throws Exception if something goes wrong calling the stored procedure
         * or dumping results to file
         */
        public void writeHeaders() throws Exception {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus ts) {
                    try {
                        logInfo(info, "Starting tx_headers stored procedure.");
                        txExportDao.callTxHeadersSP(info.getSample());
                        logInfo(info, "Finished tx_headers stored procedure.");
                        dumpToFile(buildHeadersString(null), info.getTempFile(), false);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        /**
         * Wrap call to processSample in a transaction.
         * @param batchOffset the index of the start of this batch of students
         */
        private void processSampleInTransaction(final int batchOffset) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus ts) {
                    try {
                        processSample(batchOffset);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        /**
         * After calling initForSample and writeTransactionHeaders, call this to export the
         * sample's transaction data.
         * @return the cached export file name
         */
        public String cacheSample() {
            long cacheStartTime = info.getStart().getTime();
            SampleItem sample = info.getSample();
            Long numTxnInSample = transactionsCount(sample);
            long maxStudents = sampleMetricDao().getTotalStudents(sample);
            String wholePath = getDirectoryPath(info.getSample(), info.getBaseDir());
            File newDirectory = new File(wholePath);
            String result = null;
            
            // Ensure the directory can be created before processing transactions.
            try {
                FileUtils.createDirectoriesWithPermissions(Paths.get(wholePath));
            } catch (IOException e) {
                logger.error("IOException when attempting to create the directory " + wholePath, e);
            }
            
            if (newDirectory.isDirectory()) {
                File tempFile = info.getTempFile();
                // Ensure the file can be created before processing transactions.
                if (tempFile != null) {
                    logDebug(info, "number of txns in sample ", numTxnInSample);
                    for (int batchOffset = 0; batchOffset < maxStudents;
                        batchOffset += info.getStudentBatchSize()) {
                        processSampleInTransaction(batchOffset);
                    }

                    if (canceled) { return null; }
                    try {
                        result = createCachedFile(info, tempFile);
                    } catch (IOException ioe) {
                        logErr(info, "IOException when trying to create a cached file for ",
                                ioe.getMessage());
                    }

                } else {
                    logErr(info, "Error attempting to create the file: " + tempFile);
                }

            } else {
                logErr(info, "Error attempting to create the directory: " + wholePath);
            }

            if (result == null) {
                // we were unable to cache the export
                logErr(info, "Unable to create cached export file.");
                long elapsedTime = System.currentTimeMillis() - cacheStartTime;
                systemLog(info, "ERROR attempting a Cached Transaction Export.",
                        false, elapsedTime);
            } else {
                Long time = (new Date()).getTime() - cacheStartTime;
                int seconds = (int)(time / MILLISECONDS_PER_SECOND);
                logInfo(info, "Created cached transaction export in ", seconds, " seconds");
            }
            logInfo(info, "Done. ", getElapsedTimeString(cacheStartTime));

            return result;
        }

        /**
         * Given a sample, checks the file system for an existing cached export file.
         * If one exists, return the file name, otherwise null.
         * @return a cached export file or null if one does not exist.
         */
        public String getCachedFileName() {
            return TransactionExportHelper.this.getCachedFileName(info);
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
         * Check for a message in the dataset system log indicating that an export for this dataset
         * and sample was already started.
         * @return whether an export for this dataset and sample was already started
         */
        public synchronized boolean isExportStarted() {
            return isTransactionExportStarted(info.getDataset(), info.getSample());
        }

        /**
         * Check for a message in the dataset system log indicating that an export for this dataset
         * and sample was completed.
         * @return whether an export for this dataset and sample was completed
         */
        public synchronized boolean isExportCompleted() {
            return datasetSystemLogDao().
                isTransactionExportCompleted(info.getDataset(), info.getSample());
        }

        /**
         * Drop customized tables and procedures for export, delete the transaction started
         * message, and release the current DB session.  Always call in a "finally" clause!
         * @throws SQLException if something goes wrong deleting the procedures and tables
         */
        public synchronized void cleanup() throws SQLException {
            logDebug(info, "Dropping transaction export procedures and tables.");
            txExportDao.cleanupExport(info.getSample(), info.getDataset());
            if (info.getTempFile() != null) { info.getTempFile().delete(); }
        }

        /**
         * Record completion of transaction export in the dataset system log.
         */
        public synchronized void logExportCompleted() {
            long elapsedTime = System.currentTimeMillis() - info.getStart().getTime();
            systemLog(info, "Cached Transaction Export.", true, elapsedTime);
        }
    } // end TxExportTask inner class
} // end TransactionExportHelper.java
