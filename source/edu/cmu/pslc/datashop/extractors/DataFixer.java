/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.dao.SubgoalDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * This utility will fix data in the database.
 * <ul>
 * <li>Find any weird-null times in the tutor transaction table and
 * reset them to the beginning of time, i.e. January 1st, 1970.</li>
 * <li>Find incorrect attempt at subgoal values and reset appropriately.</li>
 * <li>Find dataset levels that don't have the nested set left/right indexes filled in</li>
 * </ul>
 *
 * @author Alida Skogsholm
 * @version $Revision: 15837 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DataFixer {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Flag indicating whether to fix the dataset levels */
    private boolean fixDatasetLevels = false;
    /** Flag indicating whether to fix the attempt at subgoal */
    private boolean fixAttemptAtSubgoal = false;
    /** NEW Flag indicating whether to fix the attempt at subgoal */
    private boolean fixAAS = false;
    /** Flag indicating whether to fix the GUIDs on the subgoals */
    private boolean fixStepGuids = false;
    /** Flag indicating whether to fix file_path values for ds_files. */
    private boolean fixFilePaths = false;
    /** Flag indicating whether to fix file_path values for samples. */
    private boolean fixSampleFilePaths = false;
    /** Flag indicating whether to calculate tx durations for datasets. */
    private boolean fixTxDurations = false;
    /** Flag indicating whether to fix the GUIDS on the transactions*/
    private boolean fixTxGuids = false;
    /** Flag indicating whether to set conditions in step_rollup table. */
    private boolean fixStepRollupConditions = false;
    /** Flag indicating whether to set total student milliseconds in sample_metric table. */
    private boolean fixTotalStudentMilliseconds = false;

    /** Default batch size: 50000. */
    private static final Integer DEFAULT_BATCH_SIZE = 50000;
    /** Batch size for processing. */
    private Integer batchSize = DEFAULT_BATCH_SIZE;

    /** Base directory where files are stored in the file system. */
    private String baseDir;

    /**
     * The dataset id of the dataset to run this algorithm on.
     * Set in the handleOptions method.
     * Optional parameter.
     */
    private String datasetIdString = null;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.
     */
    public void setTransactionTemplate (TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Gets transactionTemplate.
     * @return TransactionTemplate*/
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    /**
     * Constructor.
     */
    public DataFixer() {
    }

    /**
     * Fix the database.
     */
    public void fix() {
        if (fixAAS) { newFixAttemptAtSubgoal(); }
        /**
        if (fixAttemptAtSubgoal) { fixAttemptAtSubgoal(); }
        **/
        if (fixDatasetLevels) { fixDatasetLevels(); }
        if (fixStepGuids) { fixStepGuids(); }
        if (fixSampleFilePaths) { fixSampleFilePaths(); }
        if (fixFilePaths) { fixFilePaths(); }
        if (fixTxDurations) { fixTxDurations(); }
        if (fixStepRollupConditions) { fixStepRollupConditions(); }
        if (fixTotalStudentMilliseconds) { fixTotalStudentMilliseconds(); }
        if (fixTxGuids) { fixTxGuids(); }
    }

    /**
     * Find incorrect attempt at subgoal values and reset value sequentially.
     */
    /**
    private void fixAttemptAtSubgoal() {
        logger.debug("fixAttemptAtSubgoal begin");
        // get transaction dao
        TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
        // get all students
        List allStudents = DaoFactory.DEFAULT.getStudentDao().findAll();
        // get all subgoals
        List allSubgoals = DaoFactory.DEFAULT.getSubgoalDao().findAll();

        // for each student get all the subgoals
        for (Iterator studIter = allStudents.iterator(); studIter.hasNext();) {
            StudentItem studentItem = (StudentItem)studIter.next();

            // for each subgoal
            for (Iterator sgIter = allSubgoals.iterator(); sgIter.hasNext();) {
                SubgoalItem subgoalItem = (SubgoalItem)sgIter.next();
                // get all transactions for student/subgoal pair ordered by time
                List txs = txDao.find(studentItem, subgoalItem);
                if (txs.size() > 0) {
                    int attemptIdx = 1;
                    // loop through transactions and increment attempt at subgoal value
                    for (Iterator txIter = txs.iterator(); txIter.hasNext();) {
                        TransactionItem txItem = (TransactionItem)txIter.next();
                        // set attempt_at_subgoal field
                        txItem.setAttemptAtSubgoal(new Integer(attemptIdx));
                        // save transaction
                        txDao.saveOrUpdate(txItem);
                        attemptIdx++;
                    } // end for loop on transactions
                }
            } // end for loop on subgoals
        } // end for loop on students

        logger.debug("fixAttemptAtSubgoal done");
    }
    **/

    /**
     * Set the condition column values for the step_rollup table.  This is normally done
     * during aggregation.
     */
    private void fixStepRollupConditions() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List <DatasetItem> allDatasets;

        if (datasetIdString != null) {
            DatasetItem dataset = datasetDao.get(new Integer(datasetIdString));
            allDatasets = new ArrayList();
            if (dataset != null
                && (dataset.getDeletedFlag() == null
                    || dataset.getDeletedFlag().equals(false))) {
                allDatasets.add(dataset);
            }
        } else {
            allDatasets = datasetDao.findUndeletedDatasets();
        }

        for (DatasetItem datasetItem : allDatasets) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
            String logPrefix = "setStepRollupConditions: "
                    + datasetItem.getDatasetName() + " (" + datasetItem.getId() + ") ";
            logger.info(logPrefix + "Starting...");

            StepRollupConditionsTransactionCallback txCallback =
                new StepRollupConditionsTransactionCallback(datasetItem);
            boolean successFlag = (Boolean)transactionTemplate.execute(txCallback);
            if (successFlag) {
                logger.info(logPrefix + "Finished successfully.");
            } else {
                logger.error(logPrefix + "Failed, rolling back.");
            }
        }
    }

    /**
     * Calculate and store transaction durations for all transactions within a
     * dataset.  Note: this method can be run on individual datasets by passing a value
     * for the "datasetIdString" when calling this class.
     */
    private void fixTxDurations() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List <DatasetItem> allDatasets;

        if (datasetIdString != null) {
            DatasetItem dataset = datasetDao.get(new Integer(datasetIdString));
            allDatasets = new ArrayList();
            if (dataset != null
                && (dataset.getDeletedFlag() == null
                    || dataset.getDeletedFlag().equals(false))) {
                allDatasets.add(dataset);
            }
        } else {
            allDatasets = datasetDao.findUndeletedDatasets();
        }

        for (DatasetItem datasetItem : allDatasets) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
            String logPrefix = "fixTxDurations: "
                    + datasetItem.getDatasetName() + " (" + datasetItem.getId() + ") ";
            logger.info(logPrefix + "Starting...");

            TxDurationTransactionCallback txCallback =
                new TxDurationTransactionCallback(datasetItem);
            boolean successFlag = (Boolean)transactionTemplate.execute(txCallback);
            if (successFlag) {
                logger.info(logPrefix + "Finished successfully.");
            } else {
                logger.error(logPrefix + "Failed, rolling back.");
            }
        }
    }

    /**
     * Calculate and store transaction durations for all transactions within a
     * dataset.  Note: this method can be run on individual datasets by passing a value
     * for the "datasetIdString" when calling this class.
     */
    private void fixTotalStudentMilliseconds() {

            String logPrefix = "fixTotalStudentMilliseconds: ";
            logger.info(logPrefix + " fixTotalStudentMilliseconds Starting...");

            TotalStudentMillisecondsCallback txCallback =
                new TotalStudentMillisecondsCallback();
            boolean successFlag = (Boolean)transactionTemplate.execute(txCallback);
            if (successFlag) {
                logger.info(logPrefix + "Finished successfully.");
            } else {
                logger.error(logPrefix + "Failed, rolling back.");
            }
    }

    /**
     * Makes sure that all subgoals have a GUID.
     */
    private void newFixAttemptAtSubgoal() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List <DatasetItem> allDatasets;

        if (datasetIdString != null) {
            DatasetItem dataset = datasetDao.get(new Integer(datasetIdString));
            allDatasets = new ArrayList();
            if (dataset != null
                && (dataset.getDeletedFlag() == null
                    || dataset.getDeletedFlag().equals(false))) {
                allDatasets.add(dataset);
            }
        } else {
            allDatasets = datasetDao.findUndeletedDatasets();
        }

        for (DatasetItem datasetItem : allDatasets) {
            datasetItem = datasetDao.get((Integer)datasetItem.getId());
            String logPrefix = "newFixAttemptAtSubgoal: "
                    + datasetItem.getDatasetName() + " (" + datasetItem.getId() + ") ";
            logger.info(logPrefix + "Starting...");

            AttemptAtSubgoalTransactionCallback txCallback
                   = new AttemptAtSubgoalTransactionCallback(datasetItem);
            boolean successFlag = (Boolean)transactionTemplate.execute(txCallback);
            if (successFlag) {
                logger.info(logPrefix + "Finished successfully.");
            } else {
                logger.error(logPrefix + "Failed, rolling back.");
            }
        }
    }

    /**
     * Walks through each dataset level of each dataset and makes sure that the left/right
     * indexes are set so that the nested set functionality can be used. If the indexes are
     * not set on a level will simply call saveOrUpdate which knows how to properly set them.
     */
    private void fixDatasetLevels() {
        DatasetLevelDao levelDao = DaoFactory.DEFAULT.getDatasetLevelDao();
        List datasetList = DaoFactory.DEFAULT.getDatasetDao().findUndeletedDatasets();

        for (Iterator it = datasetList.iterator(); it.hasNext();) {
            DatasetItem dataset = (DatasetItem)it.next();

            logger.info("Checking Levels for " + dataset.getDatasetName());

            List doneNodes = new ArrayList(); //collection of all done nodes.
            LinkedList unfinishedNodes = new LinkedList();
            unfinishedNodes.addAll(levelDao.find(dataset));
            int numUpdates = 0;

            while (unfinishedNodes.size() > 0) {
                DatasetLevelItem level = (DatasetLevelItem)unfinishedNodes.removeFirst();
                logger.debug("Verifying Level: " + level);

                if (level.getRightIndex() == null
                        || level.getLeftIndex() == null
                        || level.getRightIndex() == 0
                        || level.getLeftIndex() == 0) {
                    if (level.getParent() == null) {
                        logger.debug("Root node, calling save/update.");
                        //if it's a root node, go ahead and save it to get the left/right indexes.
                        levelDao.saveOrUpdate(level);
                        doneNodes.add(level);
                        numUpdates++;
                    } else if (doneNodes.contains(level.getParent())) {
                        logger.debug("Parent is done, calling save/update.");
                        //if the parent is done we can go ahead and save the child.
                        levelDao.saveOrUpdate(level);
                        doneNodes.add(level);
                        numUpdates++;
                    } else {
                        logger.debug("Parent not done, putting to end.");
                        //if the parent isn't done then put this level back at the end of the
                        //linked list since we need to save it's parent first.
                        unfinishedNodes.addLast(level);
                    }
                }
            }

            if (numUpdates > 0) {
                logger.info(dataset.getDatasetName() + " finished.  "
                        + numUpdates + " levels updated out of " + levelDao.find(dataset).size());
            } else {
                logger.info(dataset.getDatasetName() + " finished. No updates required");
            }
        }
        logger.debug("fixDatasetLevels done");
    }

    /**
     * Makes sure that all subgoals have a GUID.
     */
    private void fixStepGuids() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        SubgoalDao subgoalDao = DaoFactory.DEFAULT.getSubgoalDao();
        List <DatasetItem> allDatasets;
        final int limit = 5000;

        if (datasetIdString != null) {
            DatasetItem dataset = datasetDao.get(new Integer(datasetIdString));
            allDatasets = new ArrayList();
            if (dataset != null
                && (dataset.getDeletedFlag() == null
                    || dataset.getDeletedFlag().equals(false))) {
                allDatasets.add(dataset);
            }
        } else {
            allDatasets = datasetDao.findUndeletedDatasets();
        }

        for (DatasetItem dataset : allDatasets) {
            logger.info("Starting guid fix on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");

            Integer offset = 0;
            boolean skipDataset = false;
            boolean successFlag = true;

            List <SubgoalItem> subgoals = subgoalDao.find(dataset, limit, offset);
            while (subgoals.size() > 0 && !skipDataset) {
                BulkTransactionCallback btc = new BulkTransactionCallback(subgoals);
                successFlag = (Boolean)transactionTemplate.execute(btc);
                skipDataset = btc.skipDataset;
                if (successFlag) {
                    if (skipDataset) {
                        logger.info("Skipping guid fix on dataset: "
                                + dataset.getDatasetName() + " (" + dataset.getId() + ")");
                    } else {
                        logger.info("Successfully added subgoal guids starting at " + offset);
                    }
                } else {
                    logger.error("Rolling back batch for dataset " + dataset.getDatasetName());
                }
                //skipDataset = btc.skipDataset;
                offset += limit;
                subgoals = subgoalDao.find(dataset, limit, offset);
            }
            logger.info("Finished guid fix on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");
        }
    }

    /**
     * Updates file_path values for sample and ds_file items (required with changes
     * made for transaction export caching.
     */
    private void fixSampleFilePaths() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        List <DatasetItem> allDatasets;

        if (datasetIdString != null) {
            DatasetItem dataset = datasetDao.get(new Integer(datasetIdString));
            allDatasets = new ArrayList();
            if (dataset != null
                && (dataset.getDeletedFlag() == null
                    || dataset.getDeletedFlag().equals(false))) {
                allDatasets.add(dataset);
            }
        } else {
            allDatasets = datasetDao.findUndeletedDatasets();
        }

        for (DatasetItem dataset : allDatasets) {
            logger.info("Starting file_path fix on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");

            List<SampleItem> samplesToFix = new ArrayList();
            samplesToFix.addAll(DaoFactory.DEFAULT.getSampleDao().find(dataset));
            String cleanedDatasetName =
                FileUtils.cleanForFileSystem(dataset.getDatasetName());

            for (SampleItem sample : samplesToFix) {
                sample.setFilePath(cleanedDatasetName);
                sampleDao.saveOrUpdate(sample);
            }
            logger.info("Finished file_path fix for samples on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");
        }
    }

    /**
     * Updates the file paths in the file_path column in the File Item.
     * Also copies and removes the files appropriately.
     * And even removes empty directories.
     */
    private void fixFilePaths() {
        logger.debug("fixFilePaths starting");
        copyRemoveAndUpdateFiles();
        removeEmptyDirectories();
        logger.debug("fixFilePaths done");
    }

    /**
     * Makes sure that all transactions have a GUID.
     */
    private void fixTxGuids() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
        List <DatasetItem> allDatasets = datasetDao.findUndeletedDatasets();
        int limit = batchSize;

        for (DatasetItem dataset : allDatasets) {
            logger.info("Starting guid fix on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");
            Integer offset = 0;
            boolean skipDataset = false;
            boolean successFlag = true;

            List <TransactionItem> txs = txDao.find(dataset, limit, offset);
            while (txs.size() > 0 && !skipDataset) {
                TxGuidTransactionCallback tgtc = new TxGuidTransactionCallback(txs);
                successFlag = (Boolean)transactionTemplate.execute(tgtc);
                skipDataset = tgtc.skipDataset;
                if (successFlag) {
                    if (skipDataset) {
                        logger.info("Skipping tx guid fix on dataset: "
                                + dataset.getDatasetName() + " (" + dataset.getId() + ")");
                    } else {
                        logger.info("Successfully added tx guids starting at " + offset);
                    }
                } else {
                    logger.error("Rolling back batch for dataset " + dataset.getDatasetName());
                }
                //skipDataset = btc.skipDataset;
                offset += limit;
                txs = txDao.find(dataset, limit, offset);
            }
            logger.info("Finished guid fix on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");
        }
    }


    /**
     * Finds existing files, copies them to a new file with the cleaned file_path, deletes
     * the old version and updates the file_path in the database.
     */
    private void copyRemoveAndUpdateFiles() {
        if (baseDir == null) {
            logger.error("Base directory for files is null.  Cannot continue.");
            System.exit(1);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("copyRemoveAndUpdateFiles : Base directory is " + baseDir);
            }
        }

        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();

        File baseDirFile = new File(baseDir);
        File[] baseDirFiles = baseDirFile.listFiles();
        int baseDirLen = baseDir.length();

        if (logger.isDebugEnabled()) {
            logger.debug("copyRemoveAndUpdateFiles : base dir has " + baseDirLen + " files.");
        }

        for (int idx = 0; idx < baseDirFiles.length; idx++) {
            File file = baseDirFiles[idx];

            if (file.isDirectory()) {
                File[] subDirFiles = file.listFiles();
                for (int jdx = 0; jdx < subDirFiles.length; jdx++) {
                    File existingFile = subDirFiles[jdx];
                    if (existingFile.isDirectory()) { continue; }
                    String path = existingFile.getParentFile().getAbsolutePath();
                    path = path.substring(baseDirLen + 1, path.length());
                    String name = existingFile.getName();

                    List fileItemList = fileDao.find(path, name);
                    FileItem fileItem = null;
                    if (fileItemList.size() > 0) {
                        fileItem = (FileItem)fileItemList.get(0);
                        String currentFilePath = fileItem.getFilePath();
                        String cleanedFilePath = FileUtils.cleanForFileSystem(currentFilePath);

                        // if new path is equal,do nothing, else copy
                        if (currentFilePath.equals(cleanedFilePath)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("New file path matches the old file path for '"
                                    + name + "' (" + fileItem.getId() + ").  No action taken.");
                            }
                        } else {
                            String newPath = baseDir + File.separator + cleanedFilePath;
                            File newDir = new File(newPath);
                            if (newDir.mkdirs()) {
                                FileUtils.makeWorldReadable(newDir);
                            }
                            String newPathToFile = newPath + File.separator + name;
                            File newFile = new File(newPathToFile);
                            try {
                                FileUtils.copyFile(existingFile, newFile);

                                fileItem.setFilePath(FileUtils.cleanForFileSystem(
                                        fileItem.getFilePath()));
                                fileDao.saveOrUpdate(fileItem);
                                if (existingFile.delete()) {
                                    logger.info("Successfully copied "
                                            + formatForLogging(existingFile, newFile)
                                            + " and removed the original file."
                                            + " File id (" + fileItem.getId() + ")");
                                } else {
                                    logger.warn("Successfully copied "
                                            + " but unable to remove " + existingFile + "."
                                            + " File id (" + fileItem.getId() + ")");
                                }
                            } catch (IOException exception) {
                                logger.error("IOException when copying "
                                        + formatForLogging(existingFile, newFile)
                                        + ". " + exception.getMessage());
                            }
                        } // end else
                    } else {
                        logger.info("FileItem not found for " + existingFile.getAbsolutePath());
                    }
                } // end inner for loop
            //end if isDirectory
            }  else {
                if (logger.isDebugEnabled()) {
                    logger.debug("File is not a directory: " + file.getName());
                }
            } //end if isDirectory
        } // end outer for loop
    } // end method

    /**
     * Remove vacant directories.  Should be called after copyRemoveAndUpdateFiles().
     */
    private void removeEmptyDirectories() {
        if (baseDir == null) {
            logger.error("Base directory for files is null.  Cannot continue.");
            System.exit(1);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("removeEmptyDirectories : Base directory is " + baseDir);
            }
        }

        File baseDirectory = new File(baseDir);
        File[] childDirectories = baseDirectory.listFiles();
        if (childDirectories.length == 0) {
            logger.info("Base directory is empty: " + baseDir);
            return;
        }
        for (int dirIndex = 0; dirIndex < childDirectories.length; dirIndex++) {
            File dirToDelete = childDirectories[dirIndex];
            File[] grandchildrenDirs = dirToDelete.listFiles();
            if (grandchildrenDirs != null && grandchildrenDirs.length == 0) {
                if (dirToDelete.delete()) {
                    logger.info("Successfully deleted directory '" + dirToDelete.getName() + "'.");
                } else {
                    logger.warn("Unable to delete directory '" + dirToDelete.getName() + "'.");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Directory not empty, do nothing: '"
                            + dirToDelete.getName() + "'.");
                }
            }
        }
    }

    /**
     * Provides a nicely formatted string for logging.
     * @param existingFile the existingFile being copied.
     * @param newFile the newFile being copied to.
     * @return a nicely formatted string.
     */
    private String formatForLogging(File existingFile, File newFile) {
        return existingFile.getAbsolutePath() + " to " + newFile.getAbsolutePath();
    }

    /**
     * Parse the command line arguments to get the file and curriculum names.
     * @param args Command line arguments
     */
    protected void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            }
            if (args[i].equals("-fixTotalStudentMilliseconds")) {
                fixTotalStudentMilliseconds = true;
            }
            if (args[i].equals("-fixStepRollupConditions")) {
                fixStepRollupConditions = true;
            }

            if (args[i].equals("-fixTxDurations")) {
                fixTxDurations = true;
            }

            if (args[i].equals("-batchSize")) {
                if (++i < args.length) {
                    batchSize = new Integer(args[i]);
                }
            }

            if (args[i].equals("-datasetLevels")) {
                fixDatasetLevels = true;
            }

            if (args[i].equals("-fixAAS")) {
                fixAAS = true;
            }

            if (args[i].equals("-attempAtSubgoals")) {
                fixAttemptAtSubgoal = true;
            }

            if (args[i].equals("-fixStepGuids")) {
                fixStepGuids = true;
            }

            if (args[i].equals("-fixSampleFilePaths")) {
                fixSampleFilePaths = true;
            }

            if (args[i].equals("-fixFilePaths")) {
                fixFilePaths = true;
                if (++i < args.length) {
                    this.baseDir = args[i];
                } else {
                    logger.error("A base directory name must be specified with the "
                            + "-fixFilePaths argument");
                    displayUsage();
                    System.exit(1);
                }
            }

            if (args[i].equals("-fixTxGuids")) {
                    fixTxGuids = true;
                }

            if (!fixAttemptAtSubgoal && !fixDatasetLevels
                    && !fixAAS
                    && !fixStepGuids && !fixFilePaths && !fixSampleFilePaths
                    && !fixTxDurations && !fixStepRollupConditions
                    && !fixTotalStudentMilliseconds
                    && !fixTxGuids) {
                displayUsage();
                System.exit(0);
            }

            if (args[i].equals("-dataset")) {
                if (++i < args.length) {
                    datasetIdString = args[i];
                }
            }

        } // end for loop
    } // end handleOptions

    /**
     * Displays the command line arguments for this program.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... "
                + "DataFixer");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\t usage info");
        System.err.println("\t-dataset\t\t set dataset");
        System.err.println("\t-datasetLevels\t\t fix dataset levels");
        System.err.println("\t-fixAAS\t\t\t new fix attempt at subgoal, uses SP");
        System.err.println("\t-attempAtSubgoals\t fix attempt at subgoal");
        System.err.println("\t-fixStepGuids\t\t fix the guids on all subgoals");
        System.err.println("\t-fixTxGuids\t\t fix the guids on all transactions");
        System.err.println("\t-fixFilePaths\t\t fix the file_paths on all files");
        System.err.println("\t-fixSampleFilePaths\t fix the file_paths on all samples");
        System.err.println("\t-fixTxDurations\t\t fix the transaction durations on all datasets");
        System.err.println("\t-fixStepRollupConditions\t fix the conditions in step_rollup");
        System.err.println("\t-fixTotalStudentMilliseconds\t"
                + " fix the total student milliseconds in sample_metric");
    }

    /**
     * Run this utility.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("DataFixer.main");
        String version = VersionInformation.getReleaseString();
        logger.info("DataFixer starting (" + version + ")...");
        try {
            DataFixer dataFixer = ExtractorFactory.DEFAULT.getDataFixer();

            // parse arguments to get file name and curriculum name
            dataFixer.handleOptions(args);
            dataFixer.fix();
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        }
        logger.info("DataFixer done.");
    }

    /**
     * Inner class created to manage the database transactions.
     * @author kcunning
     *
     */
    public class BulkTransactionCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** List of SubgoalItems to process. */
        private List <SubgoalItem> subgoals;

        /** Flag to skip the dataset. */
        private boolean skipDataset = false;

        /** Subgoal Dao */
        private SubgoalDao subgoalDao = DaoFactory.DEFAULT.getSubgoalDao();

        /**
         * Constructor.
         * @param subgoals the list of SubgoalItems to process
         */
        public BulkTransactionCallback(List <SubgoalItem> subgoals) {
            this.subgoals = subgoals;
        }

        /**
         * Do a batch of line items at a time.  Hoping to improve speed.
         * @param ts TransactionStatus.
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                for (SubgoalItem subgoalItem : subgoals) {
                    subgoalItem = subgoalDao.get((Long)subgoalItem.getId());
                    if (subgoalItem.getGuid() == null || subgoalItem.getGuid().length() == 0) {
                        subgoalItem.setGuid(subgoalDao.generateGUID(subgoalItem));
                        subgoalDao.saveOrUpdate(subgoalItem);
                    } else {
                        this.skipDataset = true;
                        break;
                    }
                }
            } catch (Throwable exception) {
                logger.error("EXCEPTION!: " + exception.toString());
                exception.printStackTrace();
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

    } // end inner class BulkTransactionCallback

    /**
     * Inner class created to manage the database transactions GUID.
     *
     */
    public class TxGuidTransactionCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** List of TransactionItems to process. */
        private List <TransactionItem> txs;

        /** Flag to skip the dataset. */
        private boolean skipDataset = false;

        /** Transaction Dao */
        private TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();

        /**
         * Constructor.
         * @param txs the list of TransactionItems to process
         */
        public TxGuidTransactionCallback(List <TransactionItem> txs) {
            this.txs = txs;
        }

        /**
         * Do a batch of line items at a time.  Hoping to improve speed.
         * @param ts TransactionStatus.
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                for (TransactionItem txItem : txs) {
                    if (txItem.getGuid() == null || txItem.getGuid().length() == 0) {
                        txItem.setGuid(txDao.generateGUID());
                        txDao.saveOrUpdate(txItem);
                    } else {
                        this.skipDataset = true;
                        break;
                    }
                }
            } catch (Throwable exception) {
                logger.error("EXCEPTION!: " + exception.toString());
                exception.printStackTrace();
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    } // end inner class TxGuidTransactionCallback

    /**
     * Inner class created to manage the database transactions for the
     * newFixAtteptAtSubogal method.
     */
    public class AttemptAtSubgoalTransactionCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** Transaction DAO. */
        private TransactionDao transactionDao = DaoFactory.DEFAULT.getTransactionDao();

        /** Dataset. */
        private DatasetItem datasetItem;

        /**
         * Constructor.
         * @param datasetItem the datasetItem to fix
         */
        public AttemptAtSubgoalTransactionCallback(DatasetItem datasetItem) {
            this.datasetItem = datasetItem;
        }

        /**
         * Run the stored procedure in a transaction.
         * @param ts TransactionStatus
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                if (datasetItem != null) {
                    long startTime = System.currentTimeMillis();
                    int numRows = transactionDao.callFixAttemptAtSubgoalSP(datasetItem);
                    long endTime = System.currentTimeMillis();
                    Long elapsedTime = endTime - startTime;

                    if (numRows > 0) {
                        String info = "Updated " + numRows
                            + " attempt_at_subgoal values for "
                            + datasetItem.getDatasetName()
                            + " (" + datasetItem.getId() + ")";

                        SystemLogger.log(datasetItem, null, null,
                                SystemLogger.ACTION_MODIFY_DATASET,
                                info,
                                Boolean.TRUE, numRows, elapsedTime);

                        logger.info(info);
                    } else {
                        if (logger.isDebugEnabled()) {
                            String info = "Zero attempt_at_subgoal values were updated for "
                                + datasetItem.getDatasetName()
                                + " (" + datasetItem.getId() + ")";
                            logger.debug(info);
                        }
                    }
                } else {
                    logger.error("DataFixer.AttemptAtSubgoalTransactionCallback.doInTransaction"
                            + " : the dataset is null");
                }
            } catch (Throwable exception) {
                logger.error("Exception occurred: " + exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

    } // end inner class BulkTransactionCallback

    /**
     * Inner class created to manage the database transactions for the
     * fixTxDurations method.
     */
    public class TxDurationTransactionCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** Transaction DAO. */
        private TransactionDao transactionDao = DaoFactory.DEFAULT.getTransactionDao();

        /** Dataset. */
        private DatasetItem datasetItem;

        /**
         * Constructor.
         * @param datasetItem the datasetItem to fix
         */
        public TxDurationTransactionCallback(DatasetItem datasetItem) {
            this.datasetItem = datasetItem;
        }

        /**
         * Run the stored procedure in a transaction.
         * @param ts TransactionStatus
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                if (datasetItem != null) {
                    long startTime = System.currentTimeMillis();
                    boolean result = transactionDao.callCalculateTxDurationSP(datasetItem,
                            batchSize);
                    long endTime = System.currentTimeMillis();
                    Long elapsedTime = endTime - startTime;

                    if (result) {
                        String info = "Updated tx duration values for "
                            + datasetItem.getDatasetName()
                            + " (" + datasetItem.getId() + "). "
                            + elapsedTime;

                        SystemLogger.log(datasetItem, null, null,
                                SystemLogger.ACTION_SET_TX_DURATION,
                                info,
                                Boolean.TRUE, elapsedTime);

                        logger.info(info);
                    } else {
                        String info = "Exception occurred while calculating tx duration values for "
                            + datasetItem.getDatasetName()
                            + " (" + datasetItem.getId() + ").";
                        logger.error(info);
                    }
                } else {
                    logger.error("DataFixer.TxDurationTransactionCallback.doInTransaction"
                            + " : the dataset is null");
                }
            } catch (Throwable exception) {
                logger.error("Exception occurred: " + exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

    } // end inner class BulkTransactionCallback

    /**
     * Inner class created to manage the database transactions for the
     * fixTotalStudentMilliseconds method.
     */
    public class TotalStudentMillisecondsCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** Transaction DAO. */
        private TransactionDao transactionDao = DaoFactory.DEFAULT.getTransactionDao();

        /**
         * Constructor.
         */
        public TotalStudentMillisecondsCallback() {
        }

        /**
         * Run the stored procedure in a transaction.
         * @param ts TransactionStatus
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                long startTime = System.currentTimeMillis();
                boolean result = transactionDao.callCalculateTotalStudMillisecondsSP();
                long endTime = System.currentTimeMillis();
                String elapsedTime = DateTools.getElapsedTimeString(startTime, endTime);

                if (result) {
                    String info = "Updated total student milliseconds values "
                        + elapsedTime;

                    logger.info(info);
                } else {
                    String info = "Exception occurred while calculating "
                        + "total student milliseconds values ";

                    logger.error(info);
                }
            } catch (Throwable exception) {
                logger.error("Exception occurred: " + exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

    } // end inner class BulkTransactionCallback

    /**
     * Inner class created to manage the database transactions for the
     * fixTxDurations method.
     */
    public class StepRollupConditionsTransactionCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** Transaction DAO. */
        private StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();

        /** Dataset. */
        private DatasetItem datasetItem;

        /**
         * Constructor.
         * @param datasetItem the datasetItem to fix
         */
        public StepRollupConditionsTransactionCallback(DatasetItem datasetItem) {
            this.datasetItem = datasetItem;
        }

        /**
         * Run the stored procedure in a transaction.
         * @param ts TransactionStatus
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                if (datasetItem != null) {
                    datasetItem = DaoFactory.DEFAULT.getDatasetDao().get(
                            (Integer)datasetItem.getId());
                    int numConditionsForDataset = datasetItem.getConditionsExternal().size();
                    if (numConditionsForDataset > 0) {
                        List<SampleItem> samples = datasetItem.getSamplesExternal();
                        for (SampleItem sample : samples) {
                            long startTime = System.currentTimeMillis();
                            boolean result = stepRollupDao.callSetStepRollupConditionsProcedure(
                                    sample, batchSize);
                            long endTime = System.currentTimeMillis();
                            Long elapsedTime = endTime - startTime;
                            if (result) {
                                String info = "Updated step rollup conditions for "
                                    + getLoggingMsg(datasetItem, sample)
                                    + ". " + elapsedTime;

                                SystemLogger.log(datasetItem, null, sample,
                                        SystemLogger.ACTION_SET_STEP_ROLLUP_CONDITIONS,
                                        info,
                                        Boolean.TRUE, elapsedTime);

                                logger.info(info);
                            } else {
                                String info = "Exception occurred while setting step rollup"
                                    + " conditions for " + getLoggingMsg(datasetItem, sample) + ".";
                                logger.error(info);
                            }
                        } // end for sample
                    } else {
                        logger.info("Skipping dataset '" + datasetItem.getDatasetName()
                                + "' (" + datasetItem.getId()
                                + ") as it has no conditions.");
                    }
                } else {
                    logger.error("The dataset item is null");
                }
            } catch (Throwable exception) {
                logger.error("Exception occurred: " + exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

    } // end inner class BulkTransactionCallback

    /**
     * Helper method for creating a log message with dataset and sample information.
     * @param dataset the dataset.
     * @param sample the sample.
     * @return a string for logging.
     */
    private String getLoggingMsg(DatasetItem dataset, SampleItem sample) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("dataset '");
        buffer.append(dataset.getDatasetName());
        buffer.append("' (");
        buffer.append(dataset.getId());
        buffer.append("), sample '");
        buffer.append(sample.getSampleName());
        buffer.append("' (");
        buffer.append(sample.getId());
        buffer.append(")");
        return buffer.toString();
    }
}
