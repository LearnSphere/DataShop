/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.ffi;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.ImportQueueModeDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;
import edu.cmu.pslc.datashop.dto.importqueue.VerificationResults;
import edu.cmu.pslc.datashop.extractors.AbstractExtractor;
import edu.cmu.pslc.datashop.extractors.Aggregator;
import edu.cmu.pslc.datashop.extractors.ExtractorFactory;
import edu.cmu.pslc.datashop.extractors.SkillModelGenerator;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.importdb.dao.ImportDbDaoFactory;
import edu.cmu.pslc.datashop.importdb.dao.ImportStatusDao;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ImportQueueModeItem;
import edu.cmu.pslc.datashop.item.SampleHistoryItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.importqueue.EmailUtil;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.ServerNameUtils;

/**
 * This extractor gets the next item from the import queue to take care of it.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13540 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-09-22 14:16:36 -0400 (Thu, 22 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */

public final class ImportQueue extends AbstractExtractor {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(ImportQueue.class);

    //----- CONSTANTS -----

    /** Constant. */
    private static final Integer SLEEP_SECS = 60;
    /** Constant. */
    private static final Integer MILLISECS = 1000;
    /** Constant. */
    private static final String BASE_DIR_DEFAULT = "/datashop/dataset_files";
    /** Constant. */
    private static final String BASE_DIR_PROP_NOT_SET = "${base.files.dir}";

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private Integer sleepSeconds = SLEEP_SECS;
    /** Base directory for the datashop files. */
    private String baseDir = BASE_DIR_DEFAULT;
    /** Datashop email bucket address. */
    private String emailAddressDatashopBucket = null;
    /** Create Tables SQL Filename. */
    private String inputCreateTablesFilename = null;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     */
    private ImportQueue() { }

    //----- METHODS -----

    /**
     * Get the next ImportQueueItem and verify it.
     */
    public void run() {
        // Check base directory
        if (!checkBaseDir()) {
            return;
        }
        try {
            // Initially, we run
            setExitFlag(false);
            ImportQueueModeItem modeItem = getMode();
            changeToWaiting(modeItem);

            // Start a loop to verify queued items
            modeItem = getMode();
            boolean runFlag = true;
            while (runFlag && (!modeItem.getExitFlag())) {
                // Delete old pending import queue items.
                deleteOldPendingItems();

                if (modeItem.inPlayMode()) {
                    ImportQueueDto iqDto = getNextQueuedItem();
                    if (iqDto != null) {
                        changeToVerifiying(modeItem, iqDto);
                        importNextItem(iqDto);

                        changeToWaiting(modeItem);
                    } else {
                        sleep(sleepSeconds, "Waiting");
                    }
                } else if (modeItem.isPaused()) {
                    sleep(sleepSeconds, "Paused");
                } else {
                    runFlag = false;
                    logger.error("ImportQueue is now in ERROR mode.");
                }
                modeItem = getMode();
            }
            if (modeItem.getExitFlag()) {
                changeToStopped(modeItem);
                logger.info("exit flag true");
            }
        } catch (InterruptedException e) {
            logger.info("Exception caught: " + e);
            ImportQueueModeItem modeItem = getMode();
            changeToStopped(modeItem);
        }
    }



    /**
     * Change the mode status to verifying.
     * @param modeItem the mode item to change
     * @param iqDto the import queue DTO to start verifying
     */
    private void changeToVerifiying(ImportQueueModeItem modeItem, ImportQueueDto iqDto) {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqDao.get((Integer)iqDto.getImportQueueId());

        Date now = new Date();
        modeItem.setStatus(ImportQueueModeItem.STATUS_VERIFYING);
        modeItem.setStatusTime(now);
        modeItem.setImportQueue(iqItem);

        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        modeDao.saveOrUpdate(modeItem);
        logger.info(ImportQueueModeItem.STATUS_VERIFYING);
    }

    /**
     * Change the mode status to waiting.
     * @param modeItem the mode item to change
     */
    private void changeToWaiting(ImportQueueModeItem modeItem) {
        Date now = new Date();
        modeItem.setStatus(ImportQueueModeItem.STATUS_WAITING);
        modeItem.setStatusTime(now);
        modeItem.setImportQueue(null);

        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        modeDao.saveOrUpdate(modeItem);
        logger.info(ImportQueueModeItem.STATUS_WAITING);
    }

    /**
     * Change the mode status to waiting.
     * @param modeItem the mode item to change
     */
    private void changeToStopped(ImportQueueModeItem modeItem) {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem systemUser = userDao.findOrCreateSystemUser();
        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        Date now = new Date();
        modeItem.setStatus(ImportQueueModeItem.STATUS_WAITING);
        modeItem.setStatusTime(now);
        modeItem.setMode(ImportQueueModeItem.MODE_PAUSE);
        modeItem.setUpdatedBy(systemUser);
        modeItem.setUpdatedTime(now);
        modeDao.saveOrUpdate(modeItem);
    }

    /**
     * Get the next item to verify from the import queue table.
     * @return the next import queue item to verify
     */
    private ImportQueueDto getNextQueuedItem() {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueDto iqDto = iqDao.getNextQueuedItem();
        return iqDto;
    }
    /**
     * Run the FFI verification on the given item.
     * @param iqDto the next import queue item to verify
     */
    private void importNextItem(ImportQueueDto iqDto) {
        logger.info("Starting verify and import of " + iqDto.toString());

        Integer iqId = iqDto.getImportQueueId();
        Integer fileItemId = iqDto.getFileId();

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqDao.get(iqId);

        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        FileItem fileItem = fileDao.get(fileItemId);
        String fullFileName = fileItem.getFullFileName(baseDir);

        String datasetName = iqDto.getDatasetName();
        String domain = iqItem.getDomainName();
        String learnlab = iqItem.getLearnlabName();

        if (domain == null || domain.length() <= 0) {
            domain = "Other";
        }
        if (learnlab == null || learnlab.length() <= 0) {
            learnlab = "Other";
        }

        //Note: IQ item status (LOADING) and IQ mode status (IMPORTING) both set in FFI.

        FlatFileImporter ffi = ExtractorFactory.DEFAULT.getFlatFileImporter();
        ffi.resetInputs();
        Integer importStatusId = null;
        if (edu.cmu.pslc.datashop.util.FileUtils.isZipFile(fullFileName)) {
            try {
                String folder = edu.cmu.pslc.datashop.util.FileUtils.extractFolder(fullFileName);
                importStatusId = ffi.importDir(folder, datasetName,
                                               domain, learnlab, iqId,
                                               inputCreateTablesFilename,
                                               iqItem.getAnonFlag());
            } catch (Exception exception) {
                logger.error("Exception occurred.", exception);
                importStatusId = null;
            }
        } else {
            try {
                importStatusId = ffi.importFile(fullFileName, datasetName,
                                                domain, learnlab, iqId,
                                                inputCreateTablesFilename,
                                                iqItem.getAnonFlag());
            } catch (Exception exception) {
                logger.error("Exception occurred.", exception);
                importStatusId = null;
            }
        }

        // Reload item as non-verify-related fields may have changed, i.e., queueOrder.
        iqItem = iqDao.get(iqId);

        if (importStatusId == null) {
            logger.error("FFI failed to create ImportStatusItem.");
            iqItem.setStatus(ImportQueueItem.STATUS_ERRORS);
            iqItem.setVerificationResults("Unknown error occurred.");
            iqItem.setLastUpdatedTime(new Date());
            iqDao.saveOrUpdate(iqItem);
        } else {
            logger.info("FFI created ImportStatusItem: " + importStatusId);

            // Get the Verification Results
            ImportQueueHelper iqHelper = new ImportQueueHelper();
            VerificationResults results = iqHelper.getVerificationResults(importStatusId);
            String resultsHtml = results.generateHtml();

            // Set the import status item on ImportQueueItem
            iqItem.setImportStatusId(importStatusId);
            iqItem.setVerificationResults(resultsHtml);
            iqItem.setNumErrors(results.getTotalErrors());
            iqItem.setNumIssues(results.getTotalIssues());
            iqDao.saveOrUpdate(iqItem);

            // Update status of ImportQueueItem
            ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
            ImportStatusItem importStatusItem = importStatusDao.get(importStatusId);

            String status = ImportQueueItem.STATUS_ERRORS;

            boolean includeResults = false;

            if (importStatusItem.getStatus().equals(ImportStatusItem.STATUS_IMPORTED)) {
                status = ImportQueueItem.STATUS_PASSED;

                Long totalErrorCount = importStatusDao.getTotalErrorCount(importStatusItem);
                Long totalWarningCount = importStatusDao.getTotalWarningCount(importStatusItem);
                if ((totalWarningCount > 0) && (totalErrorCount == 0)) {
                    status = ImportQueueItem.STATUS_ISSUES;
                    includeResults = true;
                }
                DatasetItem datasetItem = getDataset(datasetName);
                if (datasetItem != null) {
                    if (iqItem.getSrcSampleId() != null && iqItem.getSrcSampleName() != null) {
                        String withKCMs = iqItem.getIncludeUserKCMs() ? "with" : "without";

                        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
                        SampleItem sampleItem = sampleDao.get(iqItem.getSrcSampleId());
                        // Create sample history item
                        String infoString = "Created dataset '" + datasetItem.getDatasetName()
                                + "' [" + datasetItem.getId() + "] " + "from dataset '"
                                + iqItem.getSrcDatasetName() + "' [" + iqItem.getSrcDatasetId()
                                + "] and sample '"
                                + iqItem.getSrcSampleName() + "' "
                                + withKCMs + " user created KC Models";
                        String infoStringDSL = "Created dataset '" + datasetItem.getDatasetName()
                                + "' [" + datasetItem.getId() + "] " + "from dataset '"
                                + iqItem.getSrcDatasetName() + "' [" + iqItem.getSrcDatasetId()
                                + "] and sample '"
                                + iqItem.getSrcSampleName() + "' [" + iqItem.getSrcSampleId() + "] "
                                + withKCMs + " user created KC Models";

                        if (sampleItem != null) {
                            SamplesHelper samplesHelper = new SamplesHelper();
                            samplesHelper.saveSampleHistory(iqItem.getUploadedBy(), sampleItem,
                                SampleHistoryItem.ACTION_CREATE_DATASET_FROM_SAMPLE,
                                    infoString, (Integer) iqItem.getId());
                        } else {
                            logger.info("Sample used to create the dataset no longer exists."
                                + " SampleHistoryItem cannot be saved.");
                        }
                        // Log modify filters action to dataset system log

                        SystemLogger.log(datasetItem, null, null,
                            SystemLogger.ACTION_CREATE_DATASET_FROM_SAMPLE,
                            infoStringDSL,
                            Boolean.TRUE,
                            null);
                    }
                    //Run KC Model Generator
                    status = ImportQueueItem.STATUS_GENING;
                    iqItemSetStatus(iqDao, iqItem, status);
                    runModelGenerator(datasetItem);

                    //Run Aggregator
                    status = ImportQueueItem.STATUS_AGGING;
                    iqItemSetStatus(iqDao, iqItem, status);
                    runAggregator(datasetItem);

                    //Set number of transactions
                    SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();
                    Long numTxs = metricDao.getTotalTransactions(datasetItem);
                    iqItem.setNumTransactions(numTxs);

                    // Set Project
                    if (iqItem.getProject() != null) {
                        datasetItem.setProject(iqItem.getProject());
                    } else {
                        // Trac 397:
                        // User specified 'choose later' so undo the fact that
                        // DBMerge set the project to 'Unclassified'.
                        datasetItem.setProject(null);
                    }

                    // Set Description
                    if (iqItem.getDescription() != null
                            && !iqItem.getDescription().isEmpty()) {
                        String newDesc = "";
                        if (datasetItem.getDescription() != null
                                && !datasetItem.getDescription().isEmpty()) {
                            newDesc = datasetItem.getDescription()
                                    + System.getProperty("line.separator");
                        }
                        newDesc += iqItem.getDescription();
                        datasetItem.setDescription(newDesc);
                    }

                 // Set Additional Notes
                    if (iqItem.getAdtlDatasetNotes() != null
                            && !iqItem.getAdtlDatasetNotes().isEmpty()) {

                        String newNotes = "";
                        if (datasetItem.getNotes() != null
                                && !datasetItem.getNotes().isEmpty()) {
                            newNotes = datasetItem.getDescription()
                                    + System.getProperty("line.separator");
                        }
                        newNotes += iqItem.getAdtlDatasetNotes();
                        datasetItem.setNotes(newNotes);
                    }

                    // Set Study Flag
                    datasetItem.setStudyFlag(iqItem.getStudyFlag());

                    // Set 'From Existing Dataset' Flag
                    datasetItem.setFromExistingDatasetFlag(iqItem.getFromExistingDatasetFlag());

                    //Save Dataset Item
                    DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                    datasetDao.saveOrUpdate(datasetItem);

                    //Mark item as loaded and save IQ Item
                    status = ImportQueueItem.STATUS_LOADED;
                    iqItem.setDataset(datasetItem);
                    iqItemSetStatus(iqDao, iqItem, status);

                    // If a slave, update master DataShop instance with dataset info.
                    if (DataShopInstance.isSlave()) {
                        Integer datasetId = (Integer)datasetItem.getId();
                        logger.info("Update master DataShop instance with dataset info"
                                    + datasetName + " (" + datasetId + ")");
                        try {
                            DatasetDTO datasetDto = HelperFactory.DEFAULT.
                                getWebServiceHelper().datasetDTOForId(datasetId);
                            DatasetCreator.INSTANCE.setDataset(datasetDto);
                        } catch (Exception e) {
                            // Failed to push Dataset info to master. Ignore?
                            logger.debug("Failed to push dataset info to master for dataset '"
                                         + datasetName + "': " + e);
                        }
                    }
                } else {
                    //This shouldn't happen, but it could. Issue #481.
                    String logErrorMsg = "Dataset Item not found for dataset name " + datasetName;
                    String userErrorMsg = "Dataset not found for " + datasetName;
                    logger.error(logErrorMsg);

                    String newHtml = "<p class=\"errorHeading\">Error:</p>";
                    newHtml += "<div class=\"uploadVerifyResults\">" + userErrorMsg + "</div>";
                    iqItem.setVerificationResults(newHtml + resultsHtml);
                    iqItem.setNumErrors(1);

                    status = ImportQueueItem.STATUS_ERRORS;
                    iqItemSetStatus(iqDao, iqItem, status);
                }
            } else {
                iqItemSetStatus(iqDao, iqItem, status);
            }

            notifyUploader(iqDto, iqItem, status, includeResults);
        }

        logger.info("Finished verify and import of " + iqDto.toString());
    }

    /**
     * Utility method to set the state, last updated time and save the item.
     * @param iqDao the dao
     * @param iqItem the item
     * @param status the new status
     */
    private void iqItemSetStatus(ImportQueueDao iqDao, ImportQueueItem iqItem, String status) {
        iqItem.setStatus(status);
        iqItem.setLastUpdatedTime(new Date());
        iqDao.saveOrUpdate(iqItem);
        logger.info("Changed status to " + status);
    }

    /**
     * Get the dataset item just created.  Name is unique across the database so only
     * one item should be found.
     * @param name the dataset name
     * @return the dataset item if found, null otherwise
     */
    private DatasetItem getDataset(String name) {
        DatasetItem datasetItem = null;
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List<DatasetItem> list = datasetDao.find(name);
        if (list.size() > 0) {
            datasetItem = list.get(0);
        }
        return datasetItem;
    }

    /**
     * Send email to the uploader when we are done with this item.
     * @param iqDto the DTO
     * @param iqItem the item
     * @param status the status
     * @param includeResults indicates whether to include the verification results in the email
     */
    private void notifyUploader(ImportQueueDto iqDto, ImportQueueItem iqItem,
            String status, boolean includeResults) {
        // Send email notifying user that dataset has been verified.
        String estImportDateStr = (iqItem.getEstImportDate() != null)
            ? iqDto.getEstImportDateFormatted() : null;

        if (isSendEmail()) {
            String dsHelpEmail = getEmailAddress();
            String emailContent = EmailUtil.notifyUserOfStatusChange(status, estImportDateStr,
                                                                     iqItem, getDatashopUrl(),
                                                                     dsHelpEmail, includeResults);
            if (emailContent != null) {
                String emailSubject = EmailUtil.getStatusChangeSubject(status, iqItem);
                List<String> bccList = null;
                if (emailAddressDatashopBucket != null) {
                    bccList = new ArrayList<String>();
                    bccList.add(emailAddressDatashopBucket);
                }
                if (bccList != null) {
                    sendEmail(dsHelpEmail, ImportQueueHelper.getToAddress(iqItem, dsHelpEmail),
                          bccList, emailSubject, emailContent);
                } else {
                    logger.info("No email addresses in the bucket list.");
                }
            }
        } else {
            logger.info("Will NOT send email to " + getEmailAddress());
        }
    }

    /**
     * Get the URL for DataShop.
     * @return the url
     */
    private String getDatashopUrl() {
        return ServerNameUtils.getDataShopUrl();
    }

    /**
     * Find and delete and pending import queue items which are over 24 hours old.
     */
    private void deleteOldPendingItems() {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        List<ImportQueueItem> pendingList = iqDao.findOldPending();
        for (ImportQueueItem item : pendingList) {
            iqDao.delete(item);
        }
        if (pendingList.size() > 0) {
            logger.info("Deleted " + pendingList.size() + " old pending import queue items.");
        } else {
            logger.debug("No old pending import queue items found.");
        }
    }

    //----- MAIN -----
    /** Properties file key-values needed for the DBMerge */
    private static Properties properties;

    /** The build properties file */
    private static final String BUILD_PROPERTIES = "build.properties";

    /**
     * Loads a properties file found in the context of the classpath.
     * @param fileName name of properties file
     * @return InputStream that can be loaded into a Properties object.
     * @throws Exception */
    private static InputStream loadFromClasspath(String fileName) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(fileName);
    }

    /**
     * Main.
     * @param args command line arguments.
     */
    public static void main(String ...args) {

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        Logger mainLogger = Logger.getLogger("ImportQueue.main");

        ImportQueue queue = ExtractorFactory.DEFAULT.getImportQueue();
        try {
            queue.handleOptions(args);
            // handleOptions will exit if process is stopping...
            mainLogger.info("ImportQueue starting...");
            if (queue.isSendEmail()) {
                mainLogger.info("Will send email to " + queue.getEmailAddress()
                            + " if an error occurs.");
            }
            queue.run();
        } catch (Throwable throwable) {
            mainLogger.error("Unknown error in main method.", throwable);
            queue.sendErrorEmail(mainLogger, "error in ImportQueue", throwable);
        } finally {
            mainLogger.info("ImportQueue done.");
        }
    }

    /**
     * Parse the command line arguments.
     * @param args Command line arguments
     */
    private void handleOptions(String[] args) {
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
            } else if (args[i].equals("-b") || args[i].equals("-baseDir")) {
                if (++i < args.length) {
                    baseDir = args[i];
                } else {
                    System.err.println("Error: a directory must be specified with -b argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-e") || args[i].equals("-email")) {
                setSendEmailFlag(true);

                if (++i < args.length) {
                    setEmailAddress(args[i]);
                } else {
                    System.err.println("Error: a email address must be specified with -e argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-d") || args[i].equals("-dsBucket")) {
                if (++i < args.length) {
                    if (!args[i].equals("${datashop.bucket.email}")) {
                        emailAddressDatashopBucket = args[i];
                    } else {
                        emailAddressDatashopBucket = DataShopInstance.getDatashopBucketEmail();
                    }
                } else {
                    System.err.println("Error: a email address must be specified with -d argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-s")
                    || args[i].equals("-sleep")) {
                if ((++i < args.length) && (args[i] != null)) {
                    sleepSeconds = Integer.parseInt(args[i]);
                } else {
                    logger.error("Num seconds to sleep must be specified with -s argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-c")
                    || args[i].equals("-createTablesFilename")) {
                if (!args[i + 1].startsWith("-")) {
                    inputCreateTablesFilename = args[++i];
                    continue;
                } else {
                    logger.error("A filename must be specified with createTablesFilename");
                }
            } else if (args[i].equals("-q") || args[i].equals("-quit")) {
                logger.info("quit");
                setExitFlag(true);
                System.exit(0);
            }
        }

        // Email arg is required.
        if (getEmailAddress() == null) {
            System.err.println("The -email arg is required.");
            displayUsage();
            System.exit(1);
        }
    }

    /**
     * Displays the command line arguments for this program.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... " + "ImportQueue");
        System.err.println("\t-e, email\t datashop-help email address, for sending FFI results");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\t\t usage info [this message]");
        System.err.println("\t-b, baseDir\t directory where DataShop files are found");
        System.err.println("\t-s, sleep\t num seconds to sleep between queries");
        System.err.println("\t-d, dsBucket\t datashop bucket email address");
        System.err.println("\t-q, quit\t used to set the exitFlag for the ImportQueueMode");
    }

    /**
     * Method to update ImportQueueMode exitFlag. Can be used to stop another ImportQueue process.
     * @param exitFlag the exit flag value
     */
    private void setExitFlag(Boolean exitFlag) {
        ImportQueueModeItem modeItem = getMode();
        modeItem.setExitFlag(exitFlag);
        DaoFactory.DEFAULT.getImportQueueModeDao().saveOrUpdate(modeItem);
    }

    /**
     * Check if the base directory is set properly.
     * @return true if the directory exists, false if its not set or doesn't exist
     */
    private boolean checkBaseDir() {
        boolean valid = false;
        if (baseDir.equals(BASE_DIR_DEFAULT)) {
            File baseDirFile = new File(baseDir);
            if (baseDirFile.exists()) {
                valid = true;
                logger.info("Base dir is the default: " + baseDir + " and it exists.");
            } else {
                valid = false;
                logger.error("Base dir is the default: " + baseDir + " and it doesn't exist.");
            }
        } else if (baseDir.equals(BASE_DIR_PROP_NOT_SET)) {
            valid = false;
            logger.error("Base dir property is not set");
        } else {
            File baseDirFile = new File(baseDir);
            if (baseDirFile.exists()) {
                valid = true;
                logger.info("Base dir: " + baseDir + " and it exists.");
            } else {
                valid = false;
                logger.error("Base dir: " + baseDir + " and it doesn't exist.");
            }
        }
        return valid;
    }

    //----- STATIC HELPER METHODS FOR OTHER EXTRACTORS -----

    /**
     * Use Thread.sleep to sleep the given amount in seconds.
     * @param seconds the number of seconds to sleep
     * @param msg the message for the logger
     * @throws InterruptedException thrown by Thread.sleep
     */
    private static void sleep(Integer seconds, String msg)
            throws InterruptedException {
        logger.info(msg + " sleeping " + seconds + " seconds");
        Thread.sleep(seconds * MILLISECS);
    }

    /**
     * Get the UserItem for the system user.
     * @return the system user item
     */
    private static UserItem getSystemUser() {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem systemUser = userDao.findOrCreateSystemUser();
        return systemUser;
    }

    /**
     * Get the ImportQueueModeItem if it exists, create it if it does not.
     * @return the one and only mode item
     */
    private static ImportQueueModeItem getMode() {
        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        ImportQueueModeItem modeItem = modeDao.get(ImportQueueModeItem.ID);
        if (modeItem == null) {
            UserItem systemUser = getSystemUser();
            Date now = new Date();

            modeItem = new ImportQueueModeItem(ImportQueueModeItem.ID);
            modeItem.setMode(ImportQueueModeItem.MODE_PLAY);
            modeItem.setUpdatedBy(systemUser);
            modeItem.setUpdatedTime(now);
            modeItem.setStatus(ImportQueueModeItem.STATUS_WAITING);
            modeItem.setStatusTime(now);
            modeItem.setImportQueue(null);
            modeItem.setExitFlag(false);
            modeDao.saveOrUpdate(modeItem);
        }
        return modeItem;
    }

    /**
     * Change the mode of the import queue.
     * @param mode the new mode
     */
    private static void changeMode(String mode) {
        UserItem systemUser = getSystemUser();
        Date now = new Date();
        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        ImportQueueModeItem modeItem = getMode();
        modeItem.setMode(mode);
        modeItem.setUpdatedBy(systemUser);
        modeItem.setUpdatedTime(now);
        modeDao.saveOrUpdate(modeItem);
    }

    /**
     * Check if the queue is in play mode now.
     * @return true if it is in play mode, false otherwise
     */
    public static boolean isInPlayMode() {
        ImportQueueModeItem item = getMode();
        if (item.getMode().equals(ImportQueueModeItem.MODE_PLAY)) {
            while (item.isVerifying()) {
                try {
                     sleep(SLEEP_SECS, "Verifying");
                } catch (InterruptedException exception) {
                    //do nothing
                    logger.debug("InterruptedException");
                }
                item = getMode();
            }
            return true;
        }
        return false;
    }

    /**
     * Pause the queue.
     */
    public static void pause() {
        changeMode(ImportQueueModeItem.MODE_PAUSE);
    }

    /**
     * Start the queue.
     */
    public static void play() {
        changeMode(ImportQueueModeItem.MODE_PLAY);
    }

    //----- Run KC Model Generator -----

    /**
     * Run the aggregator for the given dataset.
     * Log an error if something goes wrong and send an email.
     * @param datasetItem the dataset to aggregate.
     */
    private void runModelGenerator(DatasetItem datasetItem) {
        String info = formatForLogging(datasetItem);
        try {
            logger.info("SkillModelGenerator starting for dataset " + info);
            SkillModelGenerator generator = ExtractorFactory.DEFAULT.getSkillModelGenerator();
            generator.setSendEmailFlag(isSendEmail());
            generator.setEmailAddress(getEmailAddress());
            generator.generateModels(datasetItem);
        } catch (Throwable throwable) {
            String msg = "Unknown error in SkillModelGenerator for dataset " + info;
            sendErrorEmail(logger, msg, throwable);
        } finally {
            logger.info("SkillModelGenerator done for dataset " + info);
        }
    }

    //----- Run Aggregator -----

    /**
     * Run the aggregator for the given dataset.
     * Log an error if something goes wrong and send an email.
     * @param datasetItem the dataset to aggregate.
     */
    private void runAggregator(DatasetItem datasetItem) {
        String info = formatForLogging(datasetItem);
        try {
            logger.info("Aggregator starting for dataset " + info);
            Aggregator aggregator = ExtractorFactory.DEFAULT.getAggregator();
            aggregator.setSendEmailFlag(isSendEmail());
            aggregator.setEmailAddress(getEmailAddress());
            aggregator.populateStepRollup(datasetItem);
        } catch (Throwable throwable) {
            String msg = "Unknown error in Aggregator for dataset " + info;
            sendErrorEmail(logger, msg, throwable);
        } finally {
            logger.info("Aggregator done for dataset " + info);
        }
    }

    /**
     * Helper method to format dataset name and id for logging purposes.
     * @param dataset the dataset
     * @return a nicely formatted string.
     */
    private String formatForLogging(DatasetItem dataset) {
        return (dataset.getDatasetName() + " (" + dataset.getId() + ")");
    }
}
