package edu.cmu.pslc.datashop.extractors;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DBMergeDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.StringUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * The DatabaseMerge class controls the migration of one or more datasets from a source
 * database into our analysis database sharing the same schema.  Datasets in the source
 * database will either come from an upgraded import tool or from a SQL dump.
 *
 * @author
 * @version $Revision: 14730 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-01-30 10:56:22 -0500 (Tue, 30 Jan 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatabaseMerge extends AbstractExtractor {

    /** The delimiter to be used by the stored procedure to parse the datasets being merged.
     * Defaults to the comma character. */
    private String delimiter = ",";
    /** Filename of the .sql file with 'LOAD DATA INFILE' queries, read in by MySQL after stored
     * procedure returns */
    private String loadDataFilename;
    /** Filename of the .sql file to delete the transaction out files */
    private String deleteOutfilesFilename;
    /** The source database containing the datasets to merge.  Doesn't yet affect the stored
     * procedure queries but is used to verify eligible datasets in the DAO queries. */
    private String sourceDB;
    /** The mapping database, for the student ID map. */
    private String mappingDB;

    /** Types of database merge action: IMPORT_NEW_AND_MERGE, IMPORT_NEW_ONLY, MERGE_ONLY
     * and IMPORT_NEW_KEEP_ID.
     * Makes sense as an enumerator but that would introduce a dependency with the dao.
     * Integers can be checked against being null as opposed to primitive integers that have
     * unreliable defaults */
    private Integer action;
    /** Merge types: SRC_ALL_NEW, SRC_APPENDED. */
    private Integer mergeType = new Integer(0);
    /** Constant to force both new imports and merging of datasets */
    private static final Integer IMPORT_NEW_AND_MERGE = new Integer(0);
    /** Constant to import only datasets that do not exist */
    private static final Integer IMPORT_NEW_ONLY = new Integer(1);
    /** Constant to merge only datasets that already exist and add new data */
    private static final Integer MERGE_ONLY = new Integer(2);
    /** Constant to merge new datasets which already have an ID reserved. */
    private static final Integer IMPORT_NEW_KEEP_ID = new Integer(3);
    /** Constant to say that all merge datasets in the source include only new data */
    private static final Integer SRC_ALL_NEW = new Integer(0);
    /** Constant saying all merge datasets in the source includes original data and new data */
    private static final Integer SRC_APPENDED = new Integer(1);
    /** Set containing the user given dataset IDs as Integers, ignores duplicate dataset IDs */
    private HashSet<Integer> datasetsToMerge;
    /** Set containing the reserved dataset IDs to merge, ignores duplicate dataset IDs */
    private HashSet<Integer> datasetsToMergeKeep;
    /** Set containing the new dataset IDs as Integers, ignores duplicate dataset IDs */
    private HashSet<Integer> datasetsToMergeNew;
    /** Set containing the existing dataset IDs as Integers, ignores duplicate dataset IDs */
    private HashSet<Integer> datasetsToMergeExisting;
    /** Concatenated string of the reserved dataset IDs separated by the delimiter */
    private String keepDatasets;
    /** Concatenated string of the new dataset IDs separated by the delimiter */
    private String newDatasets;
    /** Concatenated string of the existing dataset IDs separated by the delimiter */
    private String existingDatasets;
    /** Local instance of DAO used by the database merge to avoid repetitive calls retrieving it */
    private DBMergeDao dbMergeDao;

    /** When not merging all datasets in the source, the dataset arguments must be verified
     * that they exist.  If they all exist this is the string returned */
    private static final String VALID = "valid";

    /** Logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Public constructor. */
    public DatabaseMerge() {
        datasetsToMerge = new HashSet<Integer>();
        datasetsToMergeKeep = new HashSet<Integer>();
        datasetsToMergeNew = new HashSet<Integer>();
        datasetsToMergeExisting = new HashSet<Integer>();
    }

    /** main function to accept arguments from build file.
     *  @param args Command line arguments */
    public static void main(String[] args) {
        DatabaseMerge dbMerge = ExtractorFactory.DEFAULT.getDBMerge();

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        Logger logger = Logger.getLogger("DatabaseMerge.main");
        String version = VersionInformation.getReleaseString();
        logger.info("Database Merge starting (" + version + ")...");
        boolean playMode = ImportQueue.isInPlayMode();
        // If an exitLevel exists, it will be used to exit
        // before the merge is run; otherwise exitLevel is null so continue.
        Integer exitLevel = null;
        try {
            // Pause the IQ
            if (playMode) {
                logger.info("main:: Pausing the ImportQueue.");
                ImportQueue.pause();
            }
            dbMerge.setDBMergeDao(null);
            // Handle the command line options:
            // This handleOptions method must be called after the ImportQueue.pause()
            // because it checks the adb_source for datasets.
            exitLevel = dbMerge.handleOptions(args);
            // If exitLevel is null, then proceed with dbmerge
            if (exitLevel == null) {
                Boolean successFlag = dbMerge.merge();
                // If the DBMerge returned a false successFlag, then
                // set the exitLevel to error
                if (!successFlag) {
                    exitLevel = 1;
                }
            }
        } catch (Throwable throwable) {
            dbMerge.sendErrorEmail(logger, "Unknown error in main method.", throwable);
            exitLevel = 1;
        } finally {
            if (playMode) {
                logger.info("main:: Unpausing the ImportQueue.");
                ImportQueue.play();
            }
            exitOnStatus(exitLevel);
            logger.info("Database Merge finished");
        }
    }

    /** Construct arguments to the db merge stored procedure and begin
     * @return returns true if successful or false, otherwise
     */
    private Boolean merge() {
        keepDatasets =
            StringUtils.wrapInDoubleQuotes(StringUtils.join(delimiter, datasetsToMergeKeep));
        newDatasets = StringUtils.wrapInDoubleQuotes(
                StringUtils.join(delimiter, datasetsToMergeNew));
        existingDatasets = StringUtils.wrapInDoubleQuotes(
                StringUtils.join(delimiter, datasetsToMergeExisting));

        boolean successFlag = dbMergeDao.runDBMerge(newDatasets, existingDatasets, keepDatasets,
                StringUtils.wrapInDoubleQuotes(delimiter), action.intValue(), mergeType.intValue(),
                loadDataFilename, deleteOutfilesFilename);

        if (!successFlag) {
            logger.info("Database Merge failed");
        }
        return successFlag;
    }

    /** Handle the command line options specified in the build file.
     * @param args command line arguments forwarded from main function
     * @return returns null if no exit is required,
     * 0 if exiting successfully (as in case of -help),
     * or any other number to exit with an error status
     */
    private Integer handleOptions(String[] args) {
        // The value is null if no exit is required,
        // 0 if exiting successfully (as in case of -help),
        // or any other number to exit with an error status
        Integer exitLevel = null;
        if (args != null && args.length != 0) {

            ArrayList<String> argsList = new ArrayList<String>();

            for (int i = 0, length = args.length; i < length; i++) {
                argsList.add(args[i]);
            }

            for (int i = 0, length = args.length; i < length; i++) {
                if (args[i].equals("-s") || args[i].equals("-sourceDB")) {
                    if (!args[i + 1].startsWith("-")) {
                        sourceDB = args[++i];
                        /* set the sourceDB in the Dao instead of passing it in multiple places */
                        DaoFactory.DEFAULT.getDBMergeDao().setSourceDB(sourceDB);
                        continue;
                    } else {
                        logger.error("No source database was specified following the source flag");
                        exitLevel = 1;
                    }
                }
                if (args[i].equals("-mappingDB")) {
                    if (!args[i + 1].startsWith("-")) {
                        mappingDB = args[++i];
                        /* set the mappingDB in the Dao instead of passing it in multiple places */
                        DaoFactory.DEFAULT.getDBMergeDao().setMappingDb(mappingDB);
                        continue;
                    } else {
                        logger.error("No mapping database was specified following the mapping flag");
                        exitLevel = 1;
                    }
                }
                if (args[i].equals("-importNewAndMerge")) {
                    action = IMPORT_NEW_AND_MERGE;
                    if (args[i + 1].equals("-srcAppended")) {
                        mergeType = SRC_APPENDED;
                        i++;
                        continue;
                    } else if (args[i + 1].equals("-srcAllNew")) {
                        mergeType = SRC_ALL_NEW;
                        i++;
                        continue;
                    } else {
                        logger.error("The import and merge flag also expects a merge type, either"
                                + " srcAppended or srcAllNew");
                        exitLevel = 1;
                    }
                }
                if (args[i].equals("-importNewKeepId")) {
                    action = IMPORT_NEW_KEEP_ID;
                    continue;
                }
                if (args[i].equals("-i") || args[i].equals("-importNewOnly")) {
                    action = IMPORT_NEW_ONLY;
                    continue;
                }
                if (args[i].equals("-m") || args[i].equals("-mergeOnly")) {
                    action = MERGE_ONLY;
                    if (args[i + 1].equals("-srcAppended")) {
                        mergeType = SRC_APPENDED;
                        i++;
                        continue;
                    } else if (args[i + 1].equals("-srcAllNew")) {
                        mergeType = SRC_ALL_NEW;
                        i++;
                        continue;
                    } else {
                        logger.error("The import and merge flag also expects a merge type, either"
                                + " srcAppended or srcAllNew");
                        exitLevel = 1;
                    }
                }
                if (args[i].equals("-a") || args[i].equals("-all")) {
                    datasetsToMerge = DaoFactory.DEFAULT.getDBMergeDao().getAllSourceDatasets();
                    if (datasetsToMerge.size() == 0) {
                        logger.error("No datasets exist in the source database.");
                        exitLevel = 1;
                    } else {
                        logger.info("Merging all available datasets.");
                        validateDatasets();
                        continue;
                    }
                }
                if (args[i].equals("-datasets")) {
                    boolean firstSample = true;
                    while (!args[++i].startsWith("-")) {
                        try {
                            datasetsToMerge.add(new Integer(args[i]));
                        } catch (NumberFormatException exception) {
                            if (firstSample) {
                                logger.error("At least one dataset id must be specified to run the "
                                        + "database merge.", exception);
                                exitLevel = 1;
                            } else {
                                break;
                            }
                        }
                        firstSample = false;
                    }
                    if (firstSample) {
                        logger.error("At least one dataset must be specified to run the "
                                + "database merge.");
                        exitLevel = 1;
                    } else {
                        validateDatasets();
                    }
                }
                if (args[i].equals("-loadDataFilename")) {
                    if (!args[i + 1].startsWith("-")) {
                        /* Shouldn't test that the filename exists because the database may be on a
                         * separate server */
                        loadDataFilename = args[++i];
                        continue;
                    } else {
                        logger.error("No filename was provided following -loadDataFilename");
                    }
                }
                if (args[i].equals("-deleteOutfilesFilename")) {
                    if (!args[i + 1].startsWith("-")) {
                        deleteOutfilesFilename = args[++i];
                        continue;
                    } else {
                        logger.error("No filename was provided following -deleteOutfilesFilename");
                    }
                }
                if (args[i].equals("-e") || args[i].equals("-email")) {
                    setEmailAddress(args[++i]);
                }

                // If the exitLevel was set, then break out of the loop
                if (exitLevel != null) {
                    break;
                }
            }  // end of for loop

            if (exitLevel == null) {
                if (datasetsToMerge.size() == 0) {
                    logger.error("Either the -datasets flag or the -all flag must be specified");
                    exitLevel = 1;
                }
                if (sourceDB == null) {
                    logger.error("The sourceDB flag is required and wasn't used.");
                    exitLevel = 1;
                }
                if (mappingDB == null) {
                    logger.error("The mappingDB flag is required.");
                    exitLevel = 1;
                }
                if (action == null) {
                    logger.error("An action flag is required of either"
                        + " -importNewOnly, -importNewAndMerge, or -mergeOnly");
                    exitLevel = 1;
                }
                if (loadDataFilename == null) {
                    logger.error("No filename was provided containing the load data queries");
                    exitLevel = 1;
                }
                if (loadDataFilename == null) {
                    logger.error("No filename was provided containing the remove outfile commands");
                    exitLevel = 1;
                }
            }
        }
        return exitLevel;
    }

    /** If we aren't merging all source datasets, only merge if all datasets exist.
     * Otherwise we abort.
     * @return returns true if successful or false, otherwise
     */
    private Boolean validateDatasets() {
        boolean successFlag = true;
        /* Pass new instance because datasetsToMerge may be modified. */
        String validationMessage = dbMergeDao.checkDatasets(
                new HashSet<Integer>(datasetsToMerge), VALID);

        if (!validationMessage.equals(VALID)) {
            logger.error(validationMessage);
            logger.error("The original dataset arguments given were: " + StringUtils.join(" ",
                    datasetsToMerge));
            logger.error("Aborting merge. Fix dataset arguments and try again.");
            successFlag = false;
        } else {

            if (action.equals(IMPORT_NEW_ONLY)) {
                logger.info("Import New Datasets Only");
                datasetsToMergeNew = dbMergeDao.findImportNewDatasets(
                        new HashSet<Integer>(datasetsToMerge));
                if (datasetsToMergeNew.size() == 0) {
                    logger.info("None of the given datasets are new and eligible to be merged"
                        + " for the first time.");
                    successFlag = false;
                }
            } else if (action.equals(IMPORT_NEW_KEEP_ID)) {
                logger.info("Import New Datasets, Keeping IDs");
                datasetsToMergeKeep = dbMergeDao.findImportKeepDatasets(
                        new HashSet<Integer>(datasetsToMerge));
                if (datasetsToMergeKeep.size() == 0) {
                    logger.info("None of the given datasets are reserved and eligible to be merged"
                                + " for the first time.");
                    successFlag = false;
                }
            } else if (action.equals(MERGE_ONLY)) {
                logger.info("Merge Datasets Only");
                datasetsToMergeExisting = dbMergeDao.findMergeDatasets(
                    new HashSet<Integer>(datasetsToMerge), mergeType.intValue());
                if (datasetsToMergeExisting.size() == 0) {
                    logger.error("None of the given datasets exist in the destination to update.");
                    successFlag = false;
                }
                /*
                if (!dbMergeDao.hasMaxTableCountEntry(datasetsToMerge)) {
                    logger.error("Not merging because there's a dataset without an entry in the "
                        + "dbm_max_table_counts table.");
                }
                */
            } else {
                logger.info("Import New And Merge Datasets");
                datasetsToMergeNew = dbMergeDao.findImportNewDatasets(
                        new HashSet<Integer>(datasetsToMerge));
                datasetsToMergeExisting = dbMergeDao.findMergeDatasets(
                        new HashSet<Integer>(datasetsToMerge), mergeType.intValue());
                if (datasetsToMergeNew.isEmpty() && datasetsToMergeExisting.isEmpty()) {
                    logger.error("None of the given datasets can be newly merged or update"
                    + " an existing dataset");
                    successFlag = false;
                } else if (datasetsToMergeNew.isEmpty()) {
                    /* Change action from merge new & merge existing to merge only */
                    action = MERGE_ONLY;
                } else if (datasetsToMergeExisting.isEmpty()) {
                    /* Change action from merge new & merge existing to merge new only */
                    action = IMPORT_NEW_ONLY;
                }
            }
        }
        return successFlag;
    }

    /** Set the DBMergeDao instance variable
     * @param dbMergeDao the DBMerge Dao
     */
    private void setDBMergeDao(DBMergeDao dbMergeDao) {
        this.dbMergeDao = (dbMergeDao == null) ? DaoFactory.DEFAULT.getDBMergeDao() : dbMergeDao;
    }
}