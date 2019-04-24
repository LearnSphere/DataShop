/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.ffi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DBMergeDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.ImportQueueModeDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper;
import edu.cmu.pslc.datashop.sourcedb.dao.FlatFileImporterDao;
import edu.cmu.pslc.datashop.extractors.AbstractExtractor;
import edu.cmu.pslc.datashop.extractors.ExtractorFactory;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.importdb.dao.ImportDbDaoFactory;
import edu.cmu.pslc.datashop.importdb.dao.ImportFileInfoDao;
import edu.cmu.pslc.datashop.importdb.dao.ImportStatusDao;
import edu.cmu.pslc.datashop.importdb.item.ImportFileInfoItem;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ImportQueueModeItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.sourcedb.dao.SourceDbDaoFactory;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.importdata.ImportConstants;

/**
 * This tool is used to read a tab-delimited import file to create a new dataset.
 * It uses a new, temporary database, stored procedures and the DB Merge tool to do this.
 *
 * @author Shanwen Yu
 * @version $Revision: 15953 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-03-22 09:18:41 -0400 (Fri, 22 Mar 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FlatFileImporter extends AbstractExtractor {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The name of this tool, used in displayUsage method. */
    private static final String TOOL_NAME = FlatFileImporter.class.getSimpleName();

    /** Success prefix string. */
    private static final String SUCCESS_PREFIX = TOOL_NAME + " - ";

    /** Warning prefix string. */
    private static final String WARN_PREFIX = "WARN " + TOOL_NAME + " - ";

    /** Error prefix string. */
    private static final String ERROR_PREFIX = "ERROR " + TOOL_NAME + " - ";

    /** Default duplicate message threshold. */
    private static final int DEFAULT_DUPLICATE_MESSAGE_THRESHOLD = 10;

    /** Default file info table name. */
    private static final String DEFAULT_FILE_INFO_TABLE_NAME = "ffi_import_file_data";

    /** Name of the directory to look for input file from the command line. */
    private String inputDirectoryName = null;

    /** Name of file to import from the command line. */
    private String inputFileName = null;

    /** Dataset name from the command line. */
    private String inputDatasetName = null;

    /** Domain from the command line. */
    private String inputDomain = null;

    /** Learnlab from the command line. */
    private String inputLearnlab = null;

    /** Command-line option whether to include KCMs from the tab-delim file.
     *  Using the values null or true includes them. False excludes all models. */
    private Boolean includeUserKCMs = null;

    /** Create Tables SQL Filename. */
    private String inputCreateTablesFilename = null;

    /** Run in 'verify-only' mode. */
    private boolean verifyOnlyMode = false;

    /** Id for ImportStatusItem created as part of verify. Null if verify failed. */
    private Integer importStatusId = null;

    /** ImportQueue id for naming temporary db table. */
    private Integer importQueueId = null;

    /** Indicates whether to anonymize the Anon Student Id column. */
    private boolean inputAnonFlag = false;

    /** Threshold from the command line. Default is 10. */
    private int dupMsgThreshold = DEFAULT_DUPLICATE_MESSAGE_THRESHOLD;

    /** Default batch size: 50000. */
    private static final Integer DEFAULT_BATCH_SIZE = 50000;

    /** Batch size for processing the Transaction GUIDs. */
    private Integer batchSize = DEFAULT_BATCH_SIZE;

    /** Properties file key-values needed for the DBMerge */
    private Properties properties;

    /** The build properties file */
    private static final String BUILD_PROPERTIES = "build.properties";

    /** File path of the file with the "LOAD DATA INFILE" calls made by the DBMerge. */
    private static final String LOAD_DATA_INFILE_KEY = "dbMergeLoadDataFile";

    /** File path of the file with the script to delete the "SELECT INTO OUTFILE" files. */
    private static final String DELETE_OUTFILES_KEY = "dbMergeDeleteOutfilesFile";

    /**
     * Flag indicating dataset is to be merged and file has only new data.
     * Default is 'false', original behavior.
     */
    private Boolean mergingDataset = false;

    /**
     * Flag indicating dataset is to be merged and file includes previous data plus new.
     * Default is 'false', original behavior.
     */
    private Boolean appendingDataset = false;

    /** Constructor. */
    public FlatFileImporter() {
        try {
            properties = new Properties();
            InputStream is = loadFromClasspath(BUILD_PROPERTIES);
            if (is != null) {
                properties.load(is);
            }
        } catch (Exception exception) {
            logger.error("Couldn't load properties file.", exception);
        }
    }

    /**
     * Sets input variables to null.
     */
    public void resetInputs() {
        this.inputAnonFlag = false;
        this.inputCreateTablesFilename = null;
        this.inputDatasetName = null;
        this.inputDirectoryName = null;
        this.inputDomain = null;
        this.inputFileName = null;
        this.inputLearnlab = null;

        DataShopInstance.refreshIfDirty();
    }

    /**
     * Check command line arguments and System.exit if necessary.
     * @param args command line arguments passed into main
     */
    public void handleOptions(String[] args) {
        int exitCode = checkOptions(args);
        if (exitCode == EXIT_OKAY) {
            System.exit(exitCode);
        } else if (exitCode == EXIT_ERROR) {
            displayUsage();
            System.exit(exitCode);
        }
    }

    /**
     * If running via commandline, from a slave, we need to append the
     * slave id to the dataset name.
     */
    public void updateDatasetName() {
        inputDatasetName = inputDatasetName.trim();
        if (DataShopInstance.isSlave()) {
            inputDatasetName += DataShopInstance.getSlaveIdStr();
        }
    }

    /** Continue flag. */
    private static final int CONTINUE = -1;
    /** Exit okay flag. */
    private static final int EXIT_OKAY = 0;
    /** Exit error flag. */
    private static final int EXIT_ERROR = 1;

    /**
     * Handle the command line arguments.
     * @param args command line arguments passed into main
     * @return an exit flag, see constants above
     */
    public int checkOptions(String[] args) {
        if (args.length == 0 || args == null) {
            return EXIT_ERROR;
        }

        ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        String argument;

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            argument = args[i].trim().toLowerCase();

            if (argument.equals("-h")
             || argument.equals("-help")) {
                displayUsage();
                return EXIT_OKAY;
            } else if (argument.equals("-v")
                    || argument.equals("-version")) {
                logInfo(VersionInformation.getReleaseString());
                return EXIT_OKAY;
            } else if (argument.equals("-dir")
                    || argument.equals("-directory")) {
                if (++i < args.length) {
                    inputDirectoryName = args[i];
                } else {
                    logger.error("A directory must be specified with this argument");
                    return EXIT_ERROR;
                }
            } else if (argument.equals("-f")
                    || argument.equals("-file")) {
                if (++i < args.length) {
                    inputFileName = args[i];
                } else {
                    logger.error("A file name must be specified with this argument");
                    return EXIT_ERROR;
                }
            } else if (argument.equals("-d")
                    || argument.equals("-dataset")) {
                if (++i < args.length) {
                    inputDatasetName = args[i];
                    logInfo("Dataset name: ", inputDatasetName);
                } else {
                    logger.error("A dataset name must be specified with this argument");
                    return EXIT_ERROR;
                }
            } else if (argument.equals("-dom")
                    || argument.equals("-domain")) {
                if (++i < args.length) {
                    inputDomain = args[i];
                    logInfo("Domain: ", inputDomain);
                } else {
                    logger.error("A domain must be specified with this argument");
                    return EXIT_ERROR;
                }
            } else if (argument.equals("-l")
                    || argument.equals("-learnlab")) {
                if (++i < args.length) {
                    inputLearnlab = args[i];
                    logInfo("Learnlab: ", inputLearnlab);
                } else {
                    logger.error("A learnlab must be specified with this argument");
                    return EXIT_ERROR;
                }
            } else if (args[i].equals("-t")
                    || args[i].equals("-threshold")) {
                if (++i < args.length) {
                    if (args[i] != null) {
                      dupMsgThreshold = Integer.parseInt(args[i]);
                    }
                } else {
                    logger.error("A threshold must be specified with this argument");
                    return EXIT_ERROR;
                }
            } else if (args[i].equals("-batchSize")) {
                if (++i < args.length) {
                    batchSize = new Integer(args[i]);
                }
            } else if (args[i].equals("-c")
                    || args[i].equals("-createTablesFilename")) {
                if (!args[i + 1].startsWith("-")) {
                    inputCreateTablesFilename = args[++i];
                    continue;
                } else {
                    logger.error("A filename must be specified with createTablesFilename");
                }
            } else if (args[i].equals("-a")
                    || args[i].equals("-anon")) {
                inputAnonFlag = true;
                logInfo("anonymize");
            } else if (args[i].equals("-o")
                    || args[i].equals("-onlyVerify")) {
                verifyOnlyMode = true;
            } else if (args[i].equals("-i")
                    || args[i].equals("-importQueueId")) {
                if (++i < args.length) {
                    if (args[i] != null) {
                        importQueueId = Integer.parseInt(args[i]);
                    } else {
                        logger.error("A queue id must be specified with this argument");
                        return EXIT_ERROR;
                    }
                } else {
                    logger.error("A queue id must be specified with this argument");
                    return EXIT_ERROR;
                }
            } else if (args[i].equals("-mergeNewData")) {
                // Maps to the DBMerge "-srcAllNew" commandline arg.
                mergingDataset = true;
            } else if (args[i].equals("-mergeAppendedData")) {
                // Maps to the DBMerge "-srcAppended" commandline arg.
                appendingDataset = true;
                mergingDataset = true;
            }
        } // end for loop

        // check for the required arguments
        boolean requiredArguments = true;
        if (inputFileName == null && inputDirectoryName == null) {
            requiredArguments = false;
            logger.error("Either a file or directory is required.");
        }
        if (inputDatasetName == null) {
            requiredArguments = false;
            logger.error("Dataset name is required.");
        }
        if (inputDomain == null) {
            requiredArguments = false;
            logger.error("Domain is required.");
        }
        if (inputLearnlab == null) {
            requiredArguments = false;
            logger.error("Learnlab is required.");
        }
        if ((inputCreateTablesFilename == null) && (!verifyOnlyMode)) {
            requiredArguments = false;
            logger.error("CreateTablesFilename is required unless running with -onlyVerify.");
        }
        if (!requiredArguments) {
            return EXIT_ERROR;
        }
        return CONTINUE;
    } // end handleOptions

    /**
     * Display the usage of this utility.
     */
    public void displayUsage() {
        StringBuffer usageMessage = new StringBuffer();
        usageMessage.append("\nUSAGE: java -classpath ... "
                + TOOL_NAME
                + " [-help] [-version]"
                + " [-file input_file_name]"
                + " [-directory input_directory]"
                + " [-dataset dataset_name]"
                + " [-domain domain_name]"
                + " [-learnlab learnlab_name]"
                + " [-threshold threshold_number]"
                + " [-createTablesFilename file_name]"
                + " [-anon]"
                + " [-onlyVerify]"
                + " [-importQueueId import_queue_id]");
        usageMessage.append("Options:");
        usageMessage.append("\t-h, -help        \t Display this help and exit");
        usageMessage.append("\t-v, -version     \t Display the version and exit");
        usageMessage.append("\t-f, -file        \t The tab-delimited file to import");
        usageMessage.append("\t-dir, -directory \t Import the files in the given directory");
        usageMessage.append("\t-d, -dataset     \t The dataset name");
        usageMessage.append("\t-dom, -domain    \t The domain name");
        usageMessage.append("\t-l, -learnlab    \t The learnlab name");
        usageMessage.append("\t-t, -threshold   \t The threshold number");
        usageMessage.append("\t-c, -createTablesFilename   \t Name of file to create adb tables");
        usageMessage.append("\t-a, -anon        \t Forces anonymization of student id column");
        usageMessage.append("\t-o, -onlyVerify  \t Run in verify-only mode");
        usageMessage.append("\t-i, -importQueueId  \t The ImportQueue id");
        logInfo(usageMessage.toString());
        System.exit(-1);
    }

    /**
     * Main program to run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("FlatFileImporter.main");
        String version = VersionInformation.getReleaseString();
        logger.info("FlatFileImporter starting (" + version + ")...");

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        FlatFileImporter ffImporter = ExtractorFactory.DEFAULT.getFlatFileImporter();

        try {

            // handle command line arguments, exit if something is amiss
            ffImporter.handleOptions(args);

            // If necessary, append slave id. Done by UploadDatasetServlet if via UI.
            ffImporter.updateDatasetName();

            // do the work
            ffImporter.run();

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("FlatFileImporter done.");
        }
    } // end main

    /**
     * This is the where the control of the overall process to import a file or set of files is.
     */
    private void run() {

        // check if the dataset name is unique/new and that another dataset is not already loading.
        if (!runCheckBasics(verifyOnlyMode)) {
            return;
        }

        // create a row in the import_db.import_status table
        ImportStatusItem importStatusItem = createEntryInImportStatusTable(
                inputDatasetName, inputDomain, inputLearnlab);

        importStatusId = (Integer)importStatusItem.getId();

        // get a list of files from the given file name or the given directory
        List<ImportFileInfoItem> importFileInfoList = getFilesAndCheckHeadings(importStatusItem);
        if (importFileInfoList == null) {
            return;
        }

        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();

        if (importQueueId != null) {
            // Sample to dataset - whether or not to include KCMs
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            ImportQueueItem iqItem = iqDao.get(importQueueId);
            if (iqItem != null) {
                includeUserKCMs = iqItem.getIncludeUserKCMs() == null
                    ? true : iqItem.getIncludeUserKCMs();
            }
        }
        if (includeUserKCMs == null) {
            includeUserKCMs = true;
        }

        // check if the column headings are valid: if not, then report error, exit
        HeadingReport headingReport;
        try {
            String headings = getHeadings(importFileInfoList);
            headingReport = HeadingReport.create(importStatusItem, headings, dupMsgThreshold, includeUserKCMs);
            if (!headingReport.areHeadingsValid()) {
                importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());
                if (importStatusItem.getErrorMessage() != null) {
                    reportWarningsAndErrors(importStatusItem);
                }
                return;
            } else {
                logInfo(SUCCESS_PREFIX, "Heading verification completed with no errors.");
            }
        } catch (IOException exception) {
            reportError(importStatusItem,
                    "IOException occurred during heading verification.",
                    exception);
            return;
        }

        // get the FFI DAO
        FlatFileImporterDao ffiDao = SourceDbDaoFactory.DEFAULT.getFlatFileImporterDao();

        // In verify-only mode we can safely assume adb_source and necessary tables exist
        if (!verifyOnlyMode) {
            // create the adb_source database
            if (!ffiDao.createDatabase()) {
                reportError(importStatusItem, "Failed to create the adb_source database.");
                return;
            } else {
                logDebug("Successfully created the adb_source database");
            }

            // create the ADB tables in the adb_source database
            if (!ffiDao.createTables(inputCreateTablesFilename)) {
                reportError(importStatusItem,
                        "Failed to create tables for the adb_source database.");
                return;
            } else {
                logDebug("Successfully created tables for the adb_source database");
            }
        }

        // convert headings into column names and populate the ffi_heading_column_map table
        ArrayList<String> columns = headingReport.convertHeadingsToColumnNames(includeUserKCMs);

        String fileInfoTableName = DEFAULT_FILE_INFO_TABLE_NAME;
        if (verifyOnlyMode) {
            if (importQueueId != null) {
                fileInfoTableName += "_" + importQueueId;
            } else {
                fileInfoTableName += "_tmp";
            }
        }

        // create a temporary table, adb_xxx.import_file_data
        if (!ffiDao.createTableImportFileData(columns, fileInfoTableName)) {
            reportError(importStatusItem, "Failed to create the import_file_data table.");
            return;
        } else {
            logDebug("successfully created the import_file_data table");
        }


        // load the data from the files into a temporary table
        if (!runLoadData(importStatusItem, importFileInfoList, columns, fileInfoTableName,
            headingReport.getDefaultKcColumnId())) {
                reportWarningsAndErrors(importStatusItem);
            return;
        }

        // run the verify data stored procedures
        if (!runVerifyData(importStatusItem, fileInfoTableName)) {
            reportWarningsAndErrors(importStatusItem);
            return;
        }

        if (verifyOnlyMode) {
            // Successfully verified file. Not importing it.
            importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());
            importStatusItem.setTimeEnd(new Date());
            importStatusItem.setStatus(ImportStatusItem.STATUS_VERIFIED_ONLY);
            importStatusDao.saveOrUpdate(importStatusItem);
            reportWarningsAndErrors(importStatusItem);
            // FIXME TBD: delete temp fileInfoTableName table
            return;
        }

        changeToImporting(importQueueId);

        // If merging data, confirm dataset exists. if not, create new (original behavior)
        if (mergingDataset) {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            List<DatasetItem> datasets = datasetDao.find(inputDatasetName);
            if ((datasets == null) || (datasets.size() == 0)) {
                // Can't find dataset
                reportError(importStatusItem, "Failed to find the specified dataset: " + inputDatasetName);
                return;
            }
        }

        Integer datasetId;
        boolean srcDatasetCreated = true;
        if (!mergingDataset) {
            // Request a new dataset id
            try {
                String datasetName = importStatusItem.getDatasetName();

                DatasetItem dsItem = DatasetCreator.INSTANCE.createNewDataset(datasetName);
                if (dsItem != null) {
                    datasetId = (Integer)dsItem.getId();
                } else {
                    String remoteStr = "";
                    if (DataShopInstance.isSlave() != null && DataShopInstance.isSlave()) {
                        remoteStr = "remote";
                    }
                    reportError(importStatusItem, "Failed to create the " + remoteStr + " dataset.");
                    return;
                }
            } catch (IOException exception) {
                reportError(importStatusItem, exception.getMessage(), exception);
                return;
            }

            // create the Dataset Item
            srcDatasetCreated = ffiDao.createDatasetItem(datasetId, importStatusItem);
        } else {
            // Merging new data into an existing dataset.
            datasetId = ffiDao.createDatasetItem(importStatusItem);
            if (datasetId == null) { srcDatasetCreated = false; }
        }

        if (!srcDatasetCreated) {
            reportError(importStatusItem, "Failed to create the dataset.");
            if (DataShopInstance.isSlave() != null && DataShopInstance.isSlave()) {
                logger.error("Please contact DataShop Help to request that the dataset "
                             + importStatusItem.getDatasetName() + " be removed from the master.");
            }
            return;
        }

        // populate the analysis database in the source database
        if (!runPopulateDatabase(importStatusItem, datasetId)) {
            reportWarningsAndErrors(importStatusItem);
            return;
        }

        // merge the new dataset from source to the destination database
        runDbMerge(importStatusItem, datasetId);
        if (inputAnonFlag) {
            fixAnonymousStudentIds();
        }

        //fixTransactionGuid
        fixTxGuids(inputDatasetName);

        // If a slave, update master DataShop instance with dataset info.
        if (DataShopInstance.isSlave()) {
            logger.info("Update master DataShop instance with dataset info"
                        + inputDatasetName + " (" + datasetId + ")");
            try {
                DatasetDTO datasetDto =
                    HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                DatasetCreator.INSTANCE.setDataset(datasetDto);
            } catch (Exception e) {
                // Failed to push Dataset info to master. Ignore?
                logger.debug("Failed to push dataset info to master for dataset '"
                             + inputDatasetName + "': " + e);
            }
        }
        
        reportWarningsAndErrors(importStatusItem);
        return;
    }

    /**
     * Change the mode status to importing for the given import queue item,
     * but don't change mode if item is null.
     * @param importQueueId the id of the given import queue item
     */
    private void changeToImporting(Integer importQueueId) {
        if (importQueueId == null) { return; }

        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        ImportQueueModeItem modeItem = modeDao.get(ImportQueueModeItem.ID);

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqDao.get(importQueueId);
        iqItem.setStatus(ImportQueueItem.STATUS_LOADING);
        iqDao.saveOrUpdate(iqItem);

        Date now = new Date();
        modeItem.setStatus(ImportQueueModeItem.STATUS_IMPORTING);
        modeItem.setStatusTime(now);
        modeItem.setImportQueue(iqItem);
        modeDao.saveOrUpdate(modeItem);

        logInfo(SUCCESS_PREFIX, "Successfully changed mode to ",
                ImportQueueModeItem.STATUS_IMPORTING);
    }

    /**
     * Find all the students which have a blank anonymous user id
     * and encrypt the actual user id, then save the changes to the database.
     * These are generated by the SQL code which imports a flat file
     * when the anon flag is set to true which means to anonymize the
     * Anon Student Id column.
     */
    private void fixAnonymousStudentIds() {
        StudentDao dao = DaoFactory.DEFAULT.getStudentDao();
        edu.cmu.pslc.datashop.mapping.dao.StudentDao mappedDao =
            edu.cmu.pslc.datashop.mapping.dao.DaoFactory.HIBERNATE.getStudentDao();
        List<StudentItem> list = dao.findByEmptyAnonUserId();
        for (StudentItem item : list) {

            // value to encrypt is only in mapping_db
            edu.cmu.pslc.datashop.mapping.item.StudentItem mappedStu =
                mappedDao.findByOriginalId((Long)item.getId());
            mappedStu = mappedDao.get((Long)mappedStu.getId());
            mappedStu.encryptUserId();
            mappedDao.saveOrUpdate(mappedStu);

            // Now, write the anon_user_id into the analysis_db
            item.setAnonymousUserId(mappedStu.getAnonymousUserId());
            dao.saveOrUpdate(item);
        }
    }

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.*/
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
     * Find all transactions for this dataset, set GUID and then save.
     * @param dsName dataset name
     */
    private void fixTxGuids(String dsName) {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
        int limit = batchSize;

        List<DatasetItem> datasets = datasetDao.find(dsName);
        for (DatasetItem dataset : datasets) {
            logger.info("Starting guid fix on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");
            Integer offset = 0;
            boolean skipDataset = false;
            boolean successFlag = true;

            List <TransactionItem> txs = txDao.findNullGUID(dataset, limit, offset);
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
                offset += limit;
                txs = txDao.find(dataset, limit, offset);
            }
            logger.info("Finished guid fix on dataset: "
                    + dataset.getDatasetName() + " (" + dataset.getId() + ")");
        }
    }

    /**
     * Check if the dataset name is unique/new and that another dataset is not already loading.
     * @param verifyOnlyMode indicates whether we are loading or just verifying
     * @return true if everything is okay, false otherwise
     */
    private boolean runCheckBasics(boolean verifyOnlyMode) {
        boolean validFlag = true;

        // check if the dataset name already exists in analysis_db
        if (!mergingDataset && !verifyDatasetNameIsNew(inputDatasetName)) {
            logError(ERROR_PREFIX,
                    "Dataset name, ", inputDatasetName, ", has already been used.");
            validFlag = false;
        }

        // check that domain, learnlab, and its mapping are valid in analysis_db
        inputDomain = inputDomain.trim();
        inputLearnlab = inputLearnlab.trim();
        if (!verifyDomainAndLearnlab(inputDomain, inputLearnlab)) {
            validFlag = false;
        }

        if (!verifyOnlyMode) {
            // check that another dataset is not already loading
            ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
            if (importStatusDao.isAnyDatasetLoading()) {
                logError(ERROR_PREFIX, "Another dataset is currently loading.");
                validFlag = false;
            }
        }

        logDebug("runCheckBasics " + validFlag);
        return validFlag;
    }

    /**
     * Load the data from the files into a temporary table.
     * @param importStatusItem the given import status item
     * @param importFileInfoList the list of import file info items to import
     * @param columns the list of columns
     * @param fileInfoTableName the db table name to hold file info
     * @param the default KC Column Id
     * @return true if successful, false otherwise
     */
    private boolean runLoadData(ImportStatusItem importStatusItem,
                                List<ImportFileInfoItem> importFileInfoList,
                                ArrayList<String> columns,
                                String fileInfoTableName, Integer defaultColumnId) {
        boolean successFlag = true;
        String lineTerminator = "";
        // change status to LOADING
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());
        importStatusItem.setStatus(ImportStatusItem.STATUS_LOADING);
        importStatusDao.saveOrUpdate(importStatusItem);

        // loop on the input files: load data into adb_xxx.import_file_data
        ImportFileInfoDao importFileInfoDao = ImportDbDaoFactory.DEFAULT.getImportFileInfoDao();
        for (int j = 0, m = importFileInfoList.size(); j < m; j++) {
            ImportFileInfoItem importFileInfoItem = (ImportFileInfoItem)importFileInfoList.get(j);
            importFileInfoItem = importFileInfoDao.get((Integer)importFileInfoItem.getId());
            importFileInfoItem.setStatus(ImportFileInfoItem.STATUS_LOADING);
            importFileInfoDao.saveOrUpdate(importFileInfoItem);

            String filename = importFileInfoItem.getFileName();
            successFlag = processBackslashInFile(filename);
            if (!successFlag) {
                logError(ERROR_PREFIX, "Failed to process backslash for file ", filename);
                String errorMessage = "Failed to process backslash.";
                importFileInfoDao.saveErrorMessage(importFileInfoItem, errorMessage);
            } else {
                logInfo(SUCCESS_PREFIX,
                        "Loading data in file ", filename, " (", (j + 1), "/", m, ")");

                lineTerminator = getLineTerminator(importFileInfoItem);
                int numRows = loadImportFileDataTable(columns, importFileInfoItem,
                                                      lineTerminator, fileInfoTableName,
                                                      defaultColumnId);
                if (numRows <= 0) {
                    logError(ERROR_PREFIX, "Zero rows loaded for file ", filename);
                    String errorMessage = "Zero rows loaded.";
                    importFileInfoDao.saveErrorMessage(importFileInfoItem, errorMessage);
                    successFlag = false;
                } else {
                    importFileInfoItem.setTimeEnd(new Date());
                    importFileInfoItem.setStatus(ImportFileInfoItem.STATUS_LOADED);
                    importFileInfoDao.saveOrUpdate(importFileInfoItem);
                }
            }
        }

        // update the line_start and line_end fields in the import_db.import_file_info table
        if ((successFlag)
                &&
            (importStatusDao.updateLineStartEndValues(importStatusItem, fileInfoTableName))) {
            logInfo(SUCCESS_PREFIX, "Data loaded into temporary table successfully.");
        } else {
            importStatusDao.saveErrorMessage(importStatusItem,
                    "Data failed to load into temporary table.");
        }

        logDebug("runLoadData ", successFlag);
        return successFlag;
    }

    /**
     * Run the verify data stored procedures.
     * @param importStatusItem the given import status item
     * @param fileInfoTableName the db table name to hold file info
     * @return true if successful, false otherwise
     */
    private boolean runVerifyData(ImportStatusItem importStatusItem, String fileInfoTableName) {
        boolean successFlag = true;

        FlatFileImporterDao ffiDao = SourceDbDaoFactory.DEFAULT.getFlatFileImporterDao();
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());

        // change status to VERIFYING
        importStatusItem.setStatus(ImportStatusItem.STATUS_VERIFYING_DATA);
        importStatusDao.saveOrUpdate(importStatusItem);

        String importDbName = ImportDbDaoFactory.HIBERNATE.getImportDatabaseName();

        // verify the data
        successFlag = ffiDao.verifyData(dupMsgThreshold, importDbName, fileInfoTableName);
        if (successFlag) {
            logInfo(SUCCESS_PREFIX, "Data verification completed with no errors.");
        } else {
            String errorMsg = "Data verification completed with errors.";
            logInfo(SUCCESS_PREFIX, errorMsg);
            importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());
            // only insert error message if verifyData returns false
            // and there is no existing message generated during the verify data process
            if (importStatusItem.getErrorMessage() == null) {
                long totalErrorCount = importStatusDao.getTotalErrorCount(importStatusItem);
                if (totalErrorCount > 0) {
                    importStatusItem.setStatus(ImportStatusItem.STATUS_ERROR);
                    importStatusDao.saveOrUpdate(importStatusItem);
                } else {
                    importStatusDao.saveErrorMessage(importStatusItem, errorMsg);
                }
            }
        }

        logDebug("runVerifyData ", successFlag);
        return successFlag;
    }

    /**
     * Populate the analysis database in the source database.
     * And drop all the helper tables including the ffi_import_file_data table.
     * @param importStatusItem the given import status item
     * @param datasetId the dataset to load
     * @return true if successful, false otherwise
     */
    private boolean runPopulateDatabase(ImportStatusItem importStatusItem, Integer datasetId) {
        boolean successFlag;

        FlatFileImporterDao ffiDao = SourceDbDaoFactory.DEFAULT.getFlatFileImporterDao();
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());

        // change status to PROCESSING
        importStatusItem.setStatus(ImportStatusItem.STATUS_PROCESSING);
        importStatusDao.saveOrUpdate(importStatusItem);

        // import data from temp table into analysis database tables
        successFlag = ffiDao.populateDatabase(datasetId, inputAnonFlag);
        if (successFlag) {
            logInfo(SUCCESS_PREFIX,
                    "Successfully populated the analysis database tables.");
        } else {
            importStatusDao.saveErrorMessage(importStatusItem,
                    "Failed to populate the analysis database tables.");
        }

        logDebug("runPopulateDatabase ", successFlag);
        return successFlag;
    }

    /** Default delimiter when passing a list of dataset IDs */
    private static final String DBMERGE_DEFAULT_DELIMITER = "\",\"";
    /** Default action, -importNewKeepId since the id will already exist */
    private static final int DBMERGE_DEFAULT_ACTION = 3;
    /** Default merge type is inconsequential for -importNewOnly run */
    private static final int DBMERGE_DEFAULT_MERGETYPE = 0;
    /** Default mapping database */
    private static final String DBMERGE_DEFAULT_MAPPING_DB = "mapping_db";

    /**
     * Merge the new dataset from source to the destination database.
     * @param importStatusItem the given import status item
     * @param datasetId the dataset_id of the new dataset created in the source
     * @return true if successful, false otherwise
     */
    private boolean runDbMerge(ImportStatusItem importStatusItem, Integer datasetId) {
        boolean successFlag;

        DBMergeDao dbMergeDao = DaoFactory.DEFAULT.getDBMergeDao();
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());

        // change status to MERGING
        importStatusItem.setStatus(ImportStatusItem.STATUS_MERGING);
        importStatusDao.saveOrUpdate(importStatusItem);

        // db merge
        logInfo(SUCCESS_PREFIX, "Starting DB Merge.");
        dbMergeDao.setMappingDb(DBMERGE_DEFAULT_MAPPING_DB);
        String newDatasets = "\"\"";
        String existingDatasets = "\"\"";
        String keepDatasets = "\"\"";
        int action = DBMERGE_DEFAULT_ACTION;
        int mergeType = DBMERGE_DEFAULT_MERGETYPE;
        if (mergingDataset) {
            // Merge data into existing dataset, if it exists. New otherwise.
            existingDatasets = "\"" + datasetId + "\"";
            action = 2;
            if (appendingDataset) {
                mergeType = 1;
            }
        } else {
            // Original FFI behavior. Dataset must be new.
            keepDatasets = "\"" + datasetId + "\"";
        }

        successFlag = dbMergeDao.runDBMerge(newDatasets, existingDatasets, keepDatasets,
                                            DBMERGE_DEFAULT_DELIMITER, action, mergeType,
                                            properties.getProperty(LOAD_DATA_INFILE_KEY),
                                            properties.getProperty(DELETE_OUTFILES_KEY));

        if (successFlag) {
            // change status to IMPORTED and set time_end to now
            importStatusItem.setStatus(ImportStatusItem.STATUS_IMPORTED);
            importStatusItem.setTimeEnd(new Date());
            importStatusDao.saveOrUpdate(importStatusItem);
            logInfo(SUCCESS_PREFIX, "Successfully ran DB Merge.");
        } else {
            importStatusDao.saveErrorMessage(importStatusItem,
                    "Failed to run the DB Merge.");
        }

        return successFlag;
    }

    /**
     * Get a list of files from the given file name or the given directory.
     * If just one file the list has just one item.
     * If multiple files then check if all the headings are the same.
     * @param importStatusItem the given status item
     * @return a list with at least one item or null if something doesn't check out
     */
    private List<ImportFileInfoItem> getFilesAndCheckHeadings(ImportStatusItem importStatusItem) {
        List<ImportFileInfoItem> importFileInfoList = null;

        // create a list of file(s)
        if (inputFileName != null) {
            importFileInfoList = getFileListFromFile(importStatusItem, inputFileName);
        } else if (inputDirectoryName != null) {
            importFileInfoList = getFileListFromDirectory(importStatusItem, inputDirectoryName);
        } else {
            logError("File or directory must be specified.");
        }

        // if multiple files, check if headings are all the same
        if (importFileInfoList != null &&  !areHeadingsAllTheSame(importFileInfoList)) {
            reportError(importStatusItem, "The headings are not the same across the files.");
            importFileInfoList = null;
        }

        return importFileInfoList;
    }

    /**
     * Check if a dataset by the given name already exists or not.
     * @param datasetName the dataset name given on the command line
     * @return true if the dataset does not already exist, false otherwise
     */
    private boolean verifyDatasetNameIsNew(String datasetName) {
        boolean newFlag = false;
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List<DatasetItem> datasetList = datasetDao.find(datasetName);
        if (datasetList.size() == 0) {
            newFlag = true;
        }
        logDebug("verifyDatasetNameIsNew ", newFlag);
        return newFlag;
    }



    /**
     * Check if the domain, learnlab, and their mapping are valid.
     * @param domainName the domain name given on the command line
     * @param learnlabName the learnlab name given on the command line
     * @return true if the domain, learnlab and their mapping are valid, false otherwise
     */
    public boolean verifyDomainAndLearnlab(String domainName, String learnlabName) {
        boolean newFlag = true;

        DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
        LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();

        DomainItem domainItem = domainDao.findByName(domainName);
        if (domainItem == null) {
            logError(ERROR_PREFIX,
                    "The Domain \"", inputDomain, "\" is not valid.");
            newFlag = false;
        }

        LearnlabItem learnlabItem = learnlabDao.findByName(learnlabName);
        if (learnlabItem == null) {
            logError(ERROR_PREFIX,
                    "The Learnlab \"", inputLearnlab, "\" is not valid.");
            newFlag = false;
        }

        if (newFlag && !learnlabDao.isValidPair(domainItem, learnlabItem)) {
            logError(ERROR_PREFIX,
                    "The Domain \"", inputDomain,
                    "\" and the Learnlab \"", inputLearnlab,
                    "\" combination is not valid.");
            newFlag = false;
        }
        return newFlag;
    }

    /**
     * Create a row in the import_db.import_status table for this run.
     * Set the dataset name and temporary analysis database name, as well as,
     * the time_start to now and the status to 'QUEUED'.
     * @param datasetName the dataset name from the command line
     * @param domain the domain from the command line
     * @param learnlab the learnlab from the command line
     * @return the status item just created if success, exception otherwise
     */
    private ImportStatusItem createEntryInImportStatusTable(String datasetName,
            String domain, String learnlab) {
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        ImportStatusItem statusItem = new ImportStatusItem();
        statusItem.setDatasetName(datasetName);
        statusItem.setDomainName(domain);
        statusItem.setLearnlabName(learnlab);
        statusItem.setTimeStart(new Date());
        statusItem.setStatus(ImportStatusItem.STATUS_QUEUED);
        importStatusDao.saveOrUpdate(statusItem);
        return statusItem;
    }

    /**
     * Create a row in the import_db.import_file table for the given file.
     * Set the directory, file name, status, as well as,
     * the time_start to now.
     * @param importStatusItem the high level status row in the database (FK)
     * @param fileName the name of one of the files to load
     * @param status expecting ERROR or QUEUED
     * @return the status item just created if success, exception otherwise
     */
    private ImportFileInfoItem createEntryInImportFileInfoTable(
            ImportStatusItem importStatusItem, String fileName, String status) {
        ImportFileInfoDao importFileInfoDao = ImportDbDaoFactory.DEFAULT.getImportFileInfoDao();
        ImportFileInfoItem fileInfoItem = new ImportFileInfoItem();
        fileInfoItem.setImportStatus(importStatusItem);
        fileInfoItem.setFileName(fileName);
        Date now = new Date();
        fileInfoItem.setTimeStart(now);
        if (status.equals(ImportFileInfoItem.STATUS_ERROR)) {
            fileInfoItem.setTimeEnd(now);
        }
        fileInfoItem.setStatus(status);
        importFileInfoDao.saveOrUpdate(fileInfoItem);
        return fileInfoItem;
    }

    /**
     * Returns a list with the one file in it, if it exists and all that.
     * @param importStatusItem the high level status row in the database (FK)
     * @param fileName the file name from the command line
     * @return a list if the file exists, null otherwise
     */
    private List getFileListFromFile(ImportStatusItem importStatusItem, String fileName) {
        List<ImportFileInfoItem> importFileInfoList = null;

        // if windows operation system, replace slashes in path
        boolean winFlag = false;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("win") >= 0) {
            winFlag = true;
            logInfo(SUCCESS_PREFIX,
                    "Replacing slashes path of file name, os.name: ",
                    osName);
        } else {
            logInfo(SUCCESS_PREFIX,
                    "NOT Replacing slashes path of file name, os.name: ",
                    osName);
        }

        if (fileName != null) {
            if (winFlag) {
                fileName = fileName.replaceAll("\\\\", "\\/");
            }

            File theFile =  new File(fileName);
            ImportFileInfoItem importFileInfoItem = null;

            if (!theFile.exists()) {
                String fixedName = ImportQueueHelper.cleanupFileName(theFile.getName());
                reportError(importStatusItem, "File " + fixedName + " does not exist.");
                importFileInfoItem = createEntryInImportFileInfoTable(importStatusItem,
                        fileName, ImportFileInfoItem.STATUS_ERROR);
            } else {
                if (theFile.getName().endsWith(".txt")) {
                    logInfo(SUCCESS_PREFIX, "File ", fileName, " found.");
                    importFileInfoItem = createEntryInImportFileInfoTable(importStatusItem,
                            fileName, ImportFileInfoItem.STATUS_QUEUED);
                    importFileInfoList = new ArrayList<ImportFileInfoItem>();
                    importFileInfoList.add(importFileInfoItem);
                } else {
                    String fixedName = ImportQueueHelper.cleanupFileName(theFile.getName());
                    reportError(importStatusItem, "File " + fixedName
                            + " does not have a .txt extension.");
                    importFileInfoItem = createEntryInImportFileInfoTable(importStatusItem,
                            fileName, ImportFileInfoItem.STATUS_ERROR);
                }
            }
        } else {
            reportError(importStatusItem, "File name is null");
        }
        return importFileInfoList; // can be null if something goes wrong
    }

    /**
     * Returns a list with all the files in the given directory in it.
     * @param importStatusItem the high level status row in the database (FK)
     * @param dirName the directory from the command line
     * @return a list if the directory exists, null otherwise
     */
    private List getFileListFromDirectory(ImportStatusItem importStatusItem, String dirName) {
        List<ImportFileInfoItem> importFileInfoList = null;

        if (dirName != null) {
            File fileObject =  new File(dirName);
            if (fileObject.exists()) {
                logInfo(SUCCESS_PREFIX, "Directory ", dirName, " found.");

                List<String> fileNameList;
                try {
                    fileNameList = getFilenameList(dirName);
                    if (fileNameList.size() <= 0) {
                        reportError(importStatusItem,
                                "No files found in the directory " + dirName);
                    } else {
                        // if windows operation system, replace slashes in path
                        boolean winFlag = false;
                        String osName = System.getProperty("os.name").toLowerCase();
                        if (osName.indexOf("win") >= 0) {
                            winFlag = true;
                            logInfo(SUCCESS_PREFIX,
                                    "Replacing slashes path of file name, os.name: ",
                                    osName);
                        } else {
                            logInfo(SUCCESS_PREFIX,
                                    "NOT Replacing slashes path of file name, os.name: ",
                                    osName);
                        }
                        // create a row for each file in the import_db.import_file_info table
                        // check if each file exists: if not, then report error, exit
                        importFileInfoList = new ArrayList<ImportFileInfoItem>();
                        for (Iterator fileIter = fileNameList.iterator(); fileIter.hasNext();) {
                            String fileName = new File((String)fileIter.next()).getAbsolutePath();
                            if (winFlag) {
                                fileName = fileName.replaceAll("\\\\", "\\/");
                            }
                            File theFile = new File(fileName);
                            ImportFileInfoItem importFileInfoItem = null;

                            if (!theFile.exists()) {
                                reportError(importStatusItem,
                                        "File " + fileName + " does not exist.");
                                importFileInfoItem = createEntryInImportFileInfoTable(
                                        importStatusItem,
                                        fileName, ImportFileInfoItem.STATUS_ERROR);
                            } else {
                                logDebug("file exists: ", fileName);
                                importFileInfoItem = createEntryInImportFileInfoTable(
                                        importStatusItem,
                                        fileName, ImportFileInfoItem.STATUS_QUEUED);
                                importFileInfoList.add(importFileInfoItem);
                            }
                        }
                    }
                } catch (IOException exception) {
                    reportError(importStatusItem,
                            "IOException occurred." + dirName, exception);
                    fileNameList = new ArrayList<String>();
                }
            } else {
                reportError(importStatusItem, "Directory " + dirName + " not found.");
            }
        } else {
            reportError(importStatusItem, "File name is null");
        }

        return importFileInfoList; // can be null if something goes wrong
    }

    /**
     * Check if all the headings in the list of files is the same.
     * Requirement 23. Ignore case and blank spaces in the column headings in the file.
     * @param importFileInfoList the list of import file info items
     * @return true if all the headings in the files are the same, false otherwise
     */
    private boolean areHeadingsAllTheSame(List<ImportFileInfoItem> importFileInfoList) {
        boolean headingsEqualFlag = true;

        if (importFileInfoList.size() <= 1) {
            return headingsEqualFlag;
        }

        BufferedReader in = null;
        try {
            String prevHeadings = null;
            for (Iterator fileIter = importFileInfoList.iterator(); fileIter.hasNext();) {
                ImportFileInfoItem importFileInfoItem = (ImportFileInfoItem)fileIter.next();
                String fileName = importFileInfoItem.getFileName();
                File file = new File(fileName);
                in = new BufferedReader(new FileReader(file));
                String headings = in.readLine().toLowerCase();
                headings = removeSpaces(headings);
                if (prevHeadings != null) {
                    if (!headings.equals(prevHeadings)) {
                        headingsEqualFlag = false;
                        break;
                    }
                } else {
                    prevHeadings = headings;
                }
            }
        } catch (FileNotFoundException exception) {
            logger.error("areHeadingsAllTheSame:FileNotFoundException occurred.", exception);
            return false;
        } catch (IOException exception) {
            logger.error("areHeadingsAllTheSame:IOException occurred.", exception);
            return false;
        } finally {
            try {
            if (in != null) { in.close(); }
            } catch (IOException exception) {
                logger.error("areHeadingsAllTheSame:IOException occurred "
                        + "closing the buffered reader.", exception);
            }
        }

        logDebug("areHeadingsAllTheSame ", headingsEqualFlag);
        return headingsEqualFlag;
    }

    /**
     * Remove all spaces from a string.
     * @param s the given string
     * @return the string without any spaces
     */
    private String removeSpaces(String s) {
        StringTokenizer st = new StringTokenizer(s, " ", false);
        String t = "";
        while (st.hasMoreElements()) { t += st.nextElement(); }
        return t;
    }

    /**
     * Given a list of files, return the first line of the first file which are the headings.
     * @param importFileInfoList the list of import file info items
     * @return the first line of the first file which is just the column headings
     * @throws IOException could occur while opening the file
     */
    public String getHeadings(List<ImportFileInfoItem> importFileInfoList) throws IOException {
        String headings = null;
        for (Iterator fileIter = importFileInfoList.iterator(); fileIter.hasNext();) {
            ImportFileInfoItem importFileInfoItem = (ImportFileInfoItem)fileIter.next();
            String fileName = importFileInfoItem.getFileName();
            File theFile = new File(fileName);
            headings = getHeadingsFromFile(theFile);
            break;
        }
        return headings;
    }

    /**
     * Given a File, read and return the first line.
     * @param file the File to read
     * @return the first line of the file which is just the column headings
     * @throws IOException could occur while opening the file
     */
    public String getHeadingsFromFile(File file) throws IOException {
        String line;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            line = in.readLine();
        } finally {
            if (in != null) { in.close(); }
        }
        return line;
    }


    /**
     * Given a File, find the line terminator.
     * @param importFileInfo the import file info items
     * @return the line terminator
     */
    public String getLineTerminator(ImportFileInfoItem importFileInfo) {
        String lineTerminator = "";
        String fileName = importFileInfo.getFileName();
        File theFile = new File(fileName);
        BufferedReader bufferReader = null;
        FileReader reader = null;
        try {
            reader = new FileReader(theFile);
            bufferReader = new BufferedReader(reader);
            int intChar;
            while (((intChar = bufferReader.read()) != -1)) {
                char lastChar = (char) intChar;
                if (lastChar == '\r') {
                    intChar = bufferReader.read();
                    if (intChar != -1) {
                        lastChar = (char) intChar;
                        if (lastChar == '\n') {
                            lineTerminator = "\r\n";
                            break;
                        }
                    }
                    lineTerminator = "\r";
                    break;
                } else  if (lastChar == '\n') {
                    lineTerminator = "\n";
                    break;
                }
            }
        } catch (IOException exception) {
            logger.error("getLineTerminator:IOException occurred.", exception);
        } finally {
            try {
                if (bufferReader != null) {
                    bufferReader.close();
                    bufferReader = null;
                }
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException exception) {
                logger.error("getLineTerminator:IOException occurred.", exception);
            } finally {
                System.gc();
            }
        }
        return lineTerminator;
    }

    /**
     * Replace all single backslash in a file to prepare the loading.
     * @param fileName the name of the file to be processed
     * @return true if process is successful, otherwise false
     */
    private boolean processBackslashInFile(String fileName) {
        String backUpFileName = fileName + ".bk";
        File file = new File(fileName);
        String[] grepCmd = {"grep", "\\\\", fileName};
        String[] sedCmd = {"sed", "-e", "s/\\\\/\\\\\\\\/g", backUpFileName};
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("win") >= 0) {
            grepCmd[1] = "\"\\\\\\\\\"";
            grepCmd[2] = "\"" + fileName + "\"";
            sedCmd[2] = "\'s/\\\\\\\\/\\\\\\\\\\\\\\\\/g\'";
            sedCmd[3] = "\"" + backUpFileName + "\"";
        }
        InputStream ins = null;
        InputStreamReader isr = null;
        BufferedReader bufferReader = null;
        BufferedWriter output = null;
        try {
            logDebug("Running GREP:", Arrays.toString(grepCmd));
            Process process = new ProcessBuilder(grepCmd).start();
            ins = process.getInputStream();
            isr = new InputStreamReader(ins);
            bufferReader = new BufferedReader(isr);
            String line;
            boolean found = false;
            while ((line = bufferReader.readLine()) != null) {
              found = true;
              logger.debug("Found single back slash in the file.");
              break;
            }
            process.destroy();
            if (bufferReader != null) {
               bufferReader.close();
            }
            if (isr != null) {
               isr.close();
            }
            if (ins != null) {
                ins.close();
            }
            if (found) {
              if (FileUtils.copyFile(file, new File(backUpFileName))) {
                    logDebug("Running SED:", Arrays.toString(sedCmd));
                    process = new ProcessBuilder(sedCmd).start();
                    ins = process.getInputStream();
                    isr = new InputStreamReader(ins);
                    bufferReader = new BufferedReader(isr);

                    output = new BufferedWriter(new FileWriter(file));

                    line = "";
                    output.write("");
                    while ((line = bufferReader.readLine()) != null) {
                      output.append(line + "\n");
                    }
                    process.waitFor();
                }
            }
        } catch (IOException exception) {
            logger.error("processBackslashInFile:IOException occurred.", exception);
            return false;
        } catch (InterruptedException exception) {
            logger.error("processBackslashInFile:IOException occurred.", exception);
            return false;
        } finally {
            try {
                if (bufferReader != null) {
                  bufferReader.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (ins != null) {
                    ins.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException exception) {
                logger.error("processBackslashInFile:IOException occurred.", exception);
                return false;
            }

            // call garbage collection explicitly
            // so that rename and delete file would work later in the process.
            System.gc();
        }
        return true;
    }

    /**
     * Load a single file into the import_file_data table in the temporary analysis database.
     * @param columns the column names for the valid headings
     * @param importFileInfoItem the importFileInfoItem
     * @param lineTerminator the expected line terminator characters
     * @param fileInfoTableName the db table name to hold file info
     * @return the number of rows imported
     */
    private int loadImportFileDataTable(ArrayList<String> columns,
            ImportFileInfoItem importFileInfoItem, String lineTerminator,
                String fileInfoTableName, Integer defaultColumnId) {
        FlatFileImporterDao ffiDao = SourceDbDaoFactory.DEFAULT.getFlatFileImporterDao();
        String fileName = importFileInfoItem.getFileName();
        int rowId = (Integer)importFileInfoItem.getId();
        int numRows = ffiDao.loadDataIntoImportFileData(fileName, columns, rowId, lineTerminator,
            fileInfoTableName, includeUserKCMs, defaultColumnId);
        try {
            File file = new File(fileName);
            File backUpFile = new File(fileName + ".bk");
            FileUtils.renameFile(backUpFile, file);
        } catch (IOException exception) {
            logger.error("loadImportFileDataTable:IOException occurred.", exception);
        }
        return numRows;
    }

    /**
     * Report the current error on the import this run of the flat file importer.
     *  19. If the file specified in the argument does not exist in the file system:
     *   a.      Change import_file_info.status to "ERROR" in the database.
     *   b.      Insert a message into the "import_file_info.message" field in the database.
     *           For example, "File xxx does not exist".
     *   c.      Display this message with the prefix "ERROR FlatFileImporter -".
     *   d.      Exit the program.
     * @param importStatusItem the high level status row in the database (FK)
     * @param message the new message
     * @param exception if there is one so that it can be put in the debug logging
     */
    private void reportError(ImportStatusItem importStatusItem, String message,
            Exception exception) {
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusDao.saveErrorMessage(importStatusItem, message);

        if (exception == null) {
            logError(ERROR_PREFIX, message);
        } else {
            logger.error(ERROR_PREFIX + message, exception);
        }
    }

    /**
     * Calls the other report error method without an exception.
     * @param importStatusItem the high level status row in the database (FK)
     * @param message the new message
     */
    private void reportError(ImportStatusItem importStatusItem, String message) {
        reportError(importStatusItem, message, null);
    }

    /**
     * Report the warnings and errors for this run of the flat file importer.
     * It is expected that this is run only once.
     * Requirement:
     * 33. Include the following information when creating error messages:
     *  a.      Total Number of Errors Found
     *  b.      File name if multiple files
     *  c.      Line number
     *  d.      Column heading
     *  e.      Reason why data is not valid
     * @param importStatusItem the given import status item
     */
    private void reportWarningsAndErrors(ImportStatusItem importStatusItem) {
        StringBuffer report = new StringBuffer();

        // Re-get the item to make sure it is filled in properly
        ImportFileInfoDao importFileInfoDao = ImportDbDaoFactory.DEFAULT.getImportFileInfoDao();
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());
        // get the list of importFileInfoItems for the given status item
        // for each importFileInfoItem
        //     report the warning count
        //     report the messages in the warning_message field
        List<ImportFileInfoItem> fileInfoItemList
                = importFileInfoDao.findByStatusItem(importStatusItem);
        report.append("\n");

        String runningMode = "load";
        String runningModePast = "loaded";
        if (verifyOnlyMode) {
            runningMode = "verify";
            runningModePast = "verified";
        }
        // get the sum of errors (sum error_count from all the import file info rows
        //    plus the error count in the import status item)
        long totalErrorCount = importStatusDao.getTotalErrorCount(importStatusItem);
        if (totalErrorCount == 0) {
            report.append("*** Report *** Successfully " + runningModePast + " dataset ***\n");
        } else {
            report.append("*** Report *** Failed to " + runningMode + " dataset ***\n");
            report.append("Errors found: " + totalErrorCount + "\n");
            report.append("\t");
            if (importStatusItem.getErrorMessage() != null) {
                report.append(importStatusItem.getErrorMessage().replace("\n", "\n\t"));
            }
            report.append("\n");
        }

        for (ImportFileInfoItem importFileInfoItem : fileInfoItemList) {
            if (importFileInfoItem.getErrorCount() > 0) {
                report.append("\t");
                report.append(importFileInfoItem.getFileName());
                report.append(", ");
                report.append(importFileInfoItem.getErrorMessage().replace("\n", "\n\t"));
                report.append("\n");
            }
        }

        report.append("\n");

        // get the sum of warnings (sum warning_count from all the import file info rows
        //    plus the warning count in the import status item)
        long totalWarningCount = importStatusDao.getTotalWarningCount(importStatusItem);
        if (totalWarningCount == 0) {
            report.append("No warnings.\n");
        } else {
            report.append("Warnings found: " + totalWarningCount + "\n");
            String warningFromStatus = importStatusItem.getWarningMessage();
            if (warningFromStatus != null) {
                report.append("\t");
                report.append(warningFromStatus.replace("\n", "\n\t"));
                report.append("\n");
            }
        }
        for (ImportFileInfoItem importFileInfoItem : fileInfoItemList) {
            if (importFileInfoItem.getWarningCount() > 0) {
                report.append("\t");
                report.append(importFileInfoItem.getFileName());
                report.append(", ");
                String warningMsg = importFileInfoItem.getWarningMessage();
                if (warningMsg == null) {
                    report.append("No warning message(s).");
                } else {
                    report.append(importFileInfoItem.getWarningMessage().replace("\n", "\n\t"));
                }
                report.append("\n");
            }
        }
        if ((totalErrorCount > 0) || (totalWarningCount > 0)) {
            report.append(ImportConstants.MSG_DOCUMENT_LINK + "\n");
        }
        report.append("**************\n");
        logInfo(report);
    }

    /** Only log if debugging is enabled. @param args concatenate objects into one string */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /** Only log if info is enabled. @param args concatenate objects into one string */
    private void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }
    /** Log warning message. @param args concatenate objects into one string */
    private void logWarn(Object... args) {
        LogUtils.logWarn(logger, args);
    }
    /** Log error message. @param args concatenate objects into one string */
    private void logError(Object... args) {
        LogUtils.logErr(logger, args);
    }

    /**
     * A recursive function to return a list of all the *.txt file names
     * in a top level directory, including all the subdirectories.
     * This method will skip CVS directories.
     * @param theFile a file or directory
     * @return a complete list of the files in this directory
     * @throws IOException if reading the file fails
     */
    private List<String> getTxtFiles(File theFile) throws IOException {
        List<String> fileList = new ArrayList<String>();

        if (theFile.isFile()) {
            if (theFile.getName().endsWith(".txt")) {
                logInfo(SUCCESS_PREFIX, "Adding file ", theFile.getName());
                fileList.add(theFile.getAbsolutePath());
            } else {
                logWarn(WARN_PREFIX, "The file ",
                        theFile.getName(),
                        " will not be imported because it does not have a .txt extension.");
            }
        } else if (theFile.isDirectory()) {
            // Ignore directories for cvs, svn, and directories that match the macosx pattern
            if (theFile.getName().equals("CVS")
                    || theFile.getName().matches("__.*")
                    || theFile.getName().matches("\\.svn")) {
                logDebug("skipping directory ", theFile.getName());
            } else {
                logDebug("found directory ", theFile.getName());
                File[] files = theFile.listFiles();
                for (int idx = 0; idx < files.length; idx++) {
                    File fileOrDir = files[idx];
                    if (fileOrDir.isFile()) {
                        if (fileOrDir.getName().endsWith(".txt")) {
                            logInfo(SUCCESS_PREFIX, "Adding file ", fileOrDir.getName());
                            fileList.add(fileOrDir.getAbsolutePath());
                        } else {
                            logWarn(WARN_PREFIX, "The file ",
                                    fileOrDir.getName(), " will not be imported.");
                        }
                    } else if (fileOrDir.isDirectory()) {
                        List<String> moreFiles = getTxtFiles(fileOrDir);
                        fileList.addAll(moreFiles);
                    }
                } // end for loop
            } // end else
        } // end else if isDirectory

        return fileList;
    }

    /**
     * Returns an list of file names given a directory.
     * @param directoryName the directory path
     * @return an list of file names
     * @throws IOException possible from getFiles method
     */
    private List<String> getFilenameList(String directoryName) throws IOException {
        File topLevelDirectory = new File(directoryName);

        if (!topLevelDirectory.isDirectory()) {
            logWarn("Not a directory: ", directoryName);
        }

        logDebug("top level directory is ", topLevelDirectory.getName());

        return getTxtFiles(topLevelDirectory);
    }



    /**
     * Loads a properties file found in the context of the classpath.
     * @param fileName name of properties file
     * @return InputStream that can be loaded into a Properties object.
     * @throws Exception */
    private InputStream loadFromClasspath(String fileName) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(fileName);
    }

    /**
     * Utility method to run FFI in verify-only mode from servlet.
     * @param fileName the name of the file to verify
     * @param datasetName the name of the new dataset
     * @param domain the name of the domain the dataset belongs to
     * @param learnlab the name of the learnlab the dataset belongs to
     * @param iqId the id of the ImportQueue for the upload
     * @return the id of the row created in the import_db.import_status table
     */
    public Integer verifyOnlyFile(String fileName, String datasetName,
                                  String domain, String learnlab, Integer iqId) {

        String[] ffiArgs = {
            "-file", fileName,
            "-dataset", datasetName,
            "-domain", domain,
            "-learnlab", learnlab,
            "-onlyVerify",
            "-importQueueId", String.valueOf(iqId)};

        if (checkOptions(ffiArgs) < 0) {
            logInfo("Running verify, options are checked out okay.");
            run();
        } else {
            logger.error("Not running verify, options not okay.");
            // Setting this to null indicates an error
            importStatusId = null;
        }

        return importStatusId;
    }

    /**
     * Utility method to run FFI in verify-only mode from servlet.
     * @param dirName the name of the directory to verify
     * @param datasetName the name of the new dataset
     * @param domain the name of the domain the dataset belongs to
     * @param learnlab the name of the learnlab the dataset belongs to
     * @param iqId the id of the ImportQueue for the upload
     * @return the id of the row created in the import_db.import_status table
     */
    public Integer verifyOnlyDir(String dirName, String datasetName,
                                 String domain, String learnlab, Integer iqId) {

        String[] ffiArgs = {
            "-dir", dirName,
            "-dataset", datasetName,
            "-domain", domain,
            "-learnlab", learnlab,
            "-onlyVerify",
            "-importQueueId", String.valueOf(iqId)};

        if (checkOptions(ffiArgs) < 0) {
            logInfo("Running verify, options are checked out okay.");
            run();
        } else {
            logger.error("Not running verify, options not okay.");
            // Setting this to null indicates an error
            importStatusId = null;
        }

        return importStatusId;
    }

    /**
     * Utility method to run FFI in verify-only mode from servlet.
     * @param fileName the name of the file to verify
     * @param datasetName the name of the new dataset
     * @param domain the name of the domain the dataset belongs to
     * @param learnlab the name of the learnlab the dataset belongs to
     * @param iqId the id of the ImportQueue for the upload
     * @param createTablesFilename the create tables filename
     * @param dataAnon whether or not the data is anonymous
     * @return the id of the row created in the import_db.import_status table
     */
    public Integer importFile(String fileName, String datasetName,
                              String domain, String learnlab, Integer iqId,
                              String createTablesFilename,
                              Boolean dataAnon) {

        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add("-file"); argsList.add(fileName);
        argsList.add("-dataset"); argsList.add(datasetName);
        argsList.add("-domain"); argsList.add(domain);
        argsList.add("-learnlab"); argsList.add(learnlab);
        argsList.add("-importQueueId"); argsList.add(String.valueOf(iqId));
        argsList.add("-createTablesFilename"); argsList.add(createTablesFilename);
        if (!dataAnon) {
            argsList.add("-anon");
        }
        String[] ffiArgs = argsList.toArray(new String[argsList.size()]);

        if (checkOptions(ffiArgs) < 0) {
            if (!dataAnon) {
                logInfo("Data not anonymous, passing in -anon, " + argsList);
            } else {
                logInfo("Data is anonymous, not passing in -anon, " + argsList);
            }
            logInfo("Running verify, options are checked out okay.");
            run();
        } else {
            logger.error("Not running verify, options not okay.");
            // Setting this to null indicates an error
            importStatusId = null;
        }

        return importStatusId;
    }

    /**
     * Utility method to run FFI in verify-only mode from servlet.
     * @param dirName the name of the directory to verify
     * @param datasetName the name of the new dataset
     * @param domain the name of the domain the dataset belongs to
     * @param learnlab the name of the learnlab the dataset belongs to
     * @param iqId the id of the ImportQueue for the upload
     * @param createTablesFilename the create tables filename
     * @param dataAnon whether or not the data is anonymous
     * @return the id of the row created in the import_db.import_status table
     */
    public Integer importDir(String dirName, String datasetName,
                             String domain, String learnlab, Integer iqId,
                             String createTablesFilename,
                             Boolean dataAnon) {

        ArrayList<String> argsList = new ArrayList<String>();
        argsList.add("-dir"); argsList.add(dirName);
        argsList.add("-dataset"); argsList.add(datasetName);
        argsList.add("-domain"); argsList.add(domain);
        argsList.add("-learnlab"); argsList.add(learnlab);
        argsList.add("-importQueueId"); argsList.add(String.valueOf(iqId));
        argsList.add("-createTablesFilename"); argsList.add(createTablesFilename);
        if (!dataAnon) {
            argsList.add("-anon");
        }
        String[] ffiArgs = argsList.toArray(new String[argsList.size()]);

        if (checkOptions(ffiArgs) < 0) {
            logInfo("Running verify, options are checked out okay.");
            if (!dataAnon) {
                logInfo("Data not anonymous, passing in -anon, " + argsList);
            } else {
                logInfo("Data is anonymous, not passing in -anon, " + argsList);
            }
            run();
        } else {
            logger.error("Not running verify, options not okay.");
            // Setting this to null indicates an error
            importStatusId = null;
        }

        return importStatusId;
    }

    /**
     * Class to get output from the stream of the MySQL process we spawn
     * @author Duncan Spencer
     */
    class StreamGobbler extends Thread {
        /** The InputStream instance */
        private InputStream inputStream;
        /** The type of the stream: ERROR, INPUT, OUTPUT */
        private String type;

        /**
         * Constructor.
         * @param inputStream is the input stream to buffer
         * @param type identifies the kind of InputStream
         */
        StreamGobbler(InputStream inputStream, String type) {
            this.inputStream = inputStream;
            this.type = type;
        }

        /** Starts the thread */
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (type.equals("ERROR")) {
                        logError(type, ">", line);
                    } else {
                        logDebug(type, ">", line);
                    }
                }
             } catch (IOException ioe) {
                    logError("IOException: ", ioe);
             }
        }
    } // end inner class StreamGobbler

    /**
     * Inner class put the setting of the TxGuid in a transaction to help with speed.
     */
    public class TxGuidTransactionCallback implements TransactionCallback {
        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** List of TransactionItems to process. */
        private List <TransactionItem> txs;

        /** Flag to skip the dataset. */
        private boolean skipDataset = false;

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
                TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
                for (TransactionItem txItem : txs) {
                    if (txItem.getGuid() == null || txItem.getGuid().trim().length() == 0) {
                        txItem.setGuid(txDao.generateGUID());
                        txDao.saveOrUpdate(txItem);
                    } else {
                        this.skipDataset = true;
                        break;
                    }
                }
            } catch (Throwable exception) {
                logger.error("Exception occurred: ", exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    } // end inner class TxGuidTransactionCallback

} // end class
