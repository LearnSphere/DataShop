/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.datasetinfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetInfoReportDao;
import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dto.DatasetInfoReport;
import edu.cmu.pslc.datashop.dto.StepInfo;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.util.StringUtils.stripChars;

 /**
 * This class assists in the creation of the dataset info report.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8625 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInfoReportHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public DatasetInfoReportHelper() {
        logger.debug("constructor");
    }

    /**
     * Initialized the Error Report given a problem item.
     * @param datasetItem the selected dataset
     * @return an object that holds all we need to display an error report
     */
    public DatasetInfoReport getDatasetInfo(DatasetItem datasetItem) {
        logger.debug("getDatasetInfo begin");

        DatasetInfoReportDao dao = DaoFactory.DEFAULT.getDatasetInfoReportDao();
        DatasetInfoReport datasetInfo = dao.getDatasetInfoReport(datasetItem);

        logger.debug("getDatasetInfo end");
        return datasetInfo;
    }

    /**
     * Gets the set of batches used for the step summary.
     * @author Alida Skogsholm
     *
     */
    public static class StepSummaryBatcher {
        /** The Sample DAO. */
        private SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        /** Iterate through sorted map from dataset level hierarchy strings
         *  to dataset level id's. */
        private Iterator<Map.Entry<String, Integer>> levelsIter;
        /** Get steps for this sample. */
        private SampleItem sample;
        /** Results from SQL call to get unique steps. */
        private ResultSet steps;
        /** Need to keep the session open until we are done with the result set. */
        private Session session;
        /** Current dataset level hierarchy. */
        private String hierarchy;
        /** False if we have gone past the last step in the result set, true otherwise. */
        private boolean hasStep;

        /** Debug logging. */
        private static Logger logger = Logger.getLogger(StepSummaryBatcher.class.getName());

        /**
         * Constructor.
         * @param levels sorted dataset level hierarchies, mapped to their id's
         * @param sample Get steps for this sample.
         */
        public StepSummaryBatcher(SortedMap<String, Integer> levels, SampleItem sample) {
            this.levelsIter = levels.entrySet().iterator();
            this.sample = sample;
        }

        /**
         * Only log if debugging is enabled.
         * @param args concatenate all arguments into the string to be logged
         */
        private void logDebug(Object... args) {
            LogUtils.logDebug(logger, args);
        }

        /**
         * Get the unique steps for the next dataset level.
         * @param level the next dataset level
         * @param batchSize used to set the fetch-ahead limit on the result set
         * @return the unique steps for the next dataset level
         * @throws SQLException if the database explodes
         */
        private ResultSet refreshSteps(Map.Entry<String, Integer> level, int batchSize)
        throws SQLException {
            hierarchy = stripChars(level.getKey());
            cleanUp();
            session = sampleDao.session();
            logDebug("fetching steps for dataset level ", hierarchy, " and sample ", sample);
            return sampleDao.getStepsForDatasetLevel(level.getValue(), sample,
                    session, batchSize);
        }

        /**
         * Whether there is one or more steps in the current result set.
         * @return whether there is one or more steps in the current result set
         * @throws SQLException if the database explodes
         */
        private boolean hasStep() throws SQLException {
            hasStep = steps != null && steps.next();
            return hasStep;
        }

        /**
         * Get the steps for the next dataset level, if necessary.
         * @param limit used to set the fetch-ahead limit on the result set
         * @throws SQLException if the database explodes
         */
        private void checkSteps(int limit) throws SQLException {
            while (!hasStep() && levelsIter.hasNext()) {
                steps = refreshSteps(levelsIter.next(), limit);
            }
        }

        /**
         * Get the next batch of steps.
         * @param batchSize the maximum number of steps to return
         * @param numOfBatches the number of batches
         * @return the next batch of steps
         */
        public List<StepInfo> getNextBatch(int batchSize, int numOfBatches) {
            return getNextBatch(batchSize, 0, numOfBatches);
        }

        /**
         * Get a batch of at most limit steps, starting at offset.
         * @param limit the maximum number of steps to return
         * @param offset the first step in the batch
         * @param numOfBatches number of batches
         * @return a batch of at most limit steps, starting at offset
         */
        public List<StepInfo> getNextBatch(int limit, int offset, int numOfBatches) {
            List<StepInfo> stepInfos = new ArrayList<StepInfo>();
            try {
                for (int i = 0; i < offset; i++) {
                    checkSteps(limit);
                    if (done()) { break; }
                }
                for (int i = 0; i < limit; i++) {
                    checkSteps(limit);
                    if (done()) { break; }
                    stepInfos.add(new StepInfo(
                            i + 1 + offset + numOfBatches * limit,
                            hierarchy,
                            stripChars(steps.getString("problem_name")),
                            stripChars(steps.getString("subgoal_name"))));
                }
            } catch (SQLException sqle) {
                // log and just return what we have so far
                logger.error("Exception fetching steps", sqle);
            }

            return stepInfos;
        }

        /**
         * Any steps left?
         * @return true if there are no more steps left to process, false otherwise
         */
        public boolean done() {
            return !hasStep && !levelsIter.hasNext();
        }

        /**
         * Clean up the database resources.
         * @throws SQLException if the database explodes
         */
        public void cleanUp() throws SQLException {
            if (session != null) { sampleDao.release(session); }
            if (steps != null) {
                if (steps.getStatement() != null) {
                    steps.getStatement().close();
                }
                steps.close();
            }
        }
    }

    /**
     * Get a step-summary batcher for this dataset.
     * @param dataset the dataset
     * @return a step-summary batcher for this dataset
     */
    public StepSummaryBatcher getStepSummaryBatcher(DatasetItem dataset) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        DatasetLevelDao levelDao = DaoFactory.DEFAULT.getDatasetLevelDao();
        return new StepSummaryBatcher(levelDao.getLevelHierarchiesForDataset(dataset),
                sampleDao.findOrCreateDefaultSample(dataset));
    }

    /**
     * Get the step summary for steps between the offset and the limit.
     * @param dataset dataset to get steps for.
     * @param limit the limit of the number of steps to return.
     * @param offset the offset of the first step to return.
     * @return List of StepInfos
     */
    public List<StepInfo> getStepSummary(DatasetItem dataset, Integer limit, Integer offset) {
        StepSummaryBatcher batcher = getStepSummaryBatcher(dataset);
        int numOfBatches = 0;
        List<StepInfo> stepInfos = batcher.getNextBatch(limit, offset, numOfBatches);

        try {
            batcher.cleanUp();
        } catch (SQLException sql) {
            logger.error("Exception cleaning up batcher", sql);
        }

        return stepInfos;
    }
}
