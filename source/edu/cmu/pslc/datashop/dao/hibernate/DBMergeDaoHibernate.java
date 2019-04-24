/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.dao.hibernate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.util.StringUtils;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DBMergeDao;

/**
 * Dao class to interface between the DatabaseMerge.java class and the db_merge_sp.sql stored
 * procedure.  This class also provides a way to invoke the .sql file containing the
 * 'LOAD DATA INFILE' queries for the larger transaction inserts through the java runtime.
 *
 * @author
 * @version $Revision: 15160 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-06-01 13:46:05 -0400 (Fri, 01 Jun 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DBMergeDaoHibernate extends AbstractDaoHibernate implements DBMergeDao {

    /** Name of the main procedure to start a database merge */
    private static final String RUN_MERGE_PROC = "dbm_run_merge";
    /** Name of the procedure to insert modify status messages in the dataset_system_log table */
    private static final String MODIFY_STATUS_PROC = "dbm_modify_merge_status";
    /** Name of the procedure to insert a complete status message in the dataset_system_log table */
    private static final String COMPLETE_STATUS_PROC = "dbm_completed_merge_status";
    /** Name of the procedure to drop mapping tables of source IDs to destination IDs */
    private static final String DROP_MAP_TABLES_PROC = "dbm_drop_mapping_tables";
    /** Name of the procedure to drop mapping tables of source IDs to destination IDs */
    private static final String DROP_HELPER_TABLES_PROC = "dbm_drop_helper_tables";
    /** Name of the procedure to delete the transactions on a SQLExcpetion */
    private static final String ROLLBACK_TRANSACTION_PROC = "dbm_rollback_transactions";
    /** Name of the procedure to delete the datasets on a SQLExcpetion */
    private static final String ROLLBACK_DATASET_PROC = "dbm_rollback_dataset";
    /** Name of the procedure to delete the subject data on a SQLExcpetion */
    private static final String ROLLBACK_SUBJECT_DATA_PROC = "dbm_rollback_subject_data";
    /** Name of the procedure to delete the transactions on a SQLExcpetion */
    private static final String ROLLBACK_MERGE_TRANSACTION_PROC = "dbm_rollback_merge_transactions";
    /** Name of the procedure to delete the dataset data on a SQLExcpetion */
    private static final String ROLLBACK_MERGE_DATASET_LEVEL_PROC =
        "dbm_rollback_merge_dataset_level_sequence";
    /** Name of the procedure to delete the subject data on a SQLExcpetion */
    private static final String ROLLBACK_MERGE_SUBJECT_DATA_PROC = "dbm_rollback_merge_classes";
    /** Designated directory to hold transaction out files */
    private static final String OUTFILE_DIRECTORY = "/merge_files/";
    /** Query to get the MySQL data directory */
    private static final String DATA_DIRECTORY_QUERY = "SELECT @@datadir";

    /** Outfile/Infile name of the tutor_transactions file so we can delete it when finished */
    private static final String TUTOR_TX_FILE = "tt_merge.txt";
    /** Outfile/Infile name of the transaction_condition_map file so we can delete it when done */
    private static final String TX_COND_MAP_FILE = "tcm_merge.txt";
    /** Outfile/Infile name of the transaction_skill_map file so we can delete it when finished */
    private static final String TX_SKILL_MAP_FILE = "tskm_merge.txt";
    /** Outfile/Infile name of the transaction_skill_event_map file so we can delete it when done */
    private static final String TX_SKILL_EVT_MAP_FILE = "tse_merge.txt";

    /** Table name containing the source datasets, needed if using the -mergeAll option  */
    private static final String DATASET_TABLE_NAME = "ds_dataset";
    /** Holds the name of the sourceDB parameter.  Does not update the sp with this information. */
    private String sourceDB;
    /** Holds the name of the mapping database. */
    private String mappingDb;

    /** Query to obtain all datasets available to merge from the source database */
    private static final String ALL_SRC_DATASETS_QUERY = "SELECT DISTINCT dataset_id FROM ";

    /** Select query used to verify that the individual datasets specified actually exist */
    private static final String DATASETS_EXIST_SELECT = "SELECT dataset_id FROM ";
    /** Where clause used to verify that the individual datasets specified actually exist */
    private static final String DATASETS_EXIST_WHERE = " WHERE dataset_id IN ";

    /** The destination analysis database used to invoke MySQL from the java runtime */
    private static String mysqlDb;
    /** Username for destination analysis database from the application context */
    private static String mysqlUser;
    /** Password for the destination analysis database from the application context */
    private static String mysqlPwd;

    /** Container to retrieve login information */
    private Map<String, String> login;

    /** Name of MySQL executable **/
    private static final String MYSQL = "mysql";

    /** Flag preceding the username */
    private String userFlag = "--user=";
    /** Flag preceding the password */
    private String pwdFlag = "--password=";
    /** Flag telling MySQL to execute the following command */
    private String executeFlag = "-e";
    /** Source file tells MySQL to run the following .sql file */
    private String sourceCommand = "source ";

    /**
     * Begins the database merge by calling the stored procedure followed by the invocation of the
     * MySQL process on the .sql file with our 'LOAD DATA INFILE' statements because it is an
     * illegal command to use in a stored procedure.  Calls to drop mapping tables and deleting
     * the out files concludes the method.
     * @param newDatasets joined string of the datasets being merged for the first time
     * @param existingDatasets joined string of the datasets to be added to existing data
     * @param keepDatasets joined string of the reserved datasets being merged for the first time
     * @param delimiter tells the stored procedure how to parse the joined datasets
     * @param action informs the stored procedure whether we should import new, merge only, or both
     * @param mergeType If that action involves merging what kind of merge data do we have
     * @param loadDatafilename the .sql file that handles the largest inserts using
     * 'LOAD DATA INFILE' statements
     * @param deleteOutfilesFilename the .sql file that handles the deletion of the out files
     * on unix servers.
     * @return return true if no errors were encountered otherwise return false
     * */
    public boolean runDBMerge(String newDatasets, String existingDatasets, String keepDatasets,
                              String delimiter, int action, int mergeType,
                              String loadDatafilename, String deleteOutfilesFilename) {

        logger.info(buildSPCall(RUN_MERGE_PROC, newDatasets, existingDatasets, keepDatasets,
                                 delimiter, action, mergeType, "\"" + mappingDb + "\""));
        try {
            callSP(buildSPCall(RUN_MERGE_PROC, newDatasets, existingDatasets, keepDatasets,
                               delimiter, action, mergeType, "\"" + mappingDb + "\""));
        } catch (SQLException exception) {
            logger.error("Error calling db merge stored procedure with parameters"
                    + " new datasets: " + newDatasets + " existing datasets: " + existingDatasets
                    + " keep datasets: " + keepDatasets
                    + " delimeter: " + delimiter + " action: " + action
                    + " filename: " + loadDatafilename
                    + " mappingDb: " + mappingDb, exception);
            updateStatus(false);
            rollbackMerge(action);
            dropMergeTables();
            return false;
        }

        /* Invoke the load data .sql file if a new dataset is being imported */
        if (action == 0 || action == 1 || action == 3) {
            boolean success = insertTransactions(loadDatafilename);
            cleanupTransactionFiles(deleteOutfilesFilename);
            if (!success) {
                updateStatus(false);
                rollbackMerge(action);
                dropMergeTables();
                return false;
            }
        }

        updateStatus(true);
        dropMergeTables();

        return true;
    }

    /**
     * Called if the merge all datasets option is specified.  Retrieves all datasets in the source
     * database
     * @return A HashSet of all the dataset IDs in the source database
     */
    public HashSet<Integer> getAllSourceDatasets() {
        HashSet<Integer> allDatasets = null;
        Session session = getSession();
        try {
            SQLQuery allDatasetsQuery
                = session.createSQLQuery(ALL_SRC_DATASETS_QUERY
                        + sourceDB + "." + DATASET_TABLE_NAME);
            allDatasets = new HashSet<Integer>(allDatasetsQuery.list());
        } finally {
            releaseSession(session);
        }
        return allDatasets;
    }

    /**
     * Verifies the existence of the datasets passed to the command line in the source database.
     * @param datasets a HashSet of potential database IDs
     * @param onSuccess string to return if all dataset IDs provided are valid
     * @return onSuccess string if all datasets exist otherwise a detail message about those that
     * don't.
     */
    public String checkDatasets(HashSet<Integer> datasets, String onSuccess) {
        Session session = getSession();
        SQLQuery existingDatasets;
        List<Integer> ids;
        try {
            existingDatasets = session.createSQLQuery(DATASETS_EXIST_SELECT
                + getQualifiedSrcDsTableName() + DATASETS_EXIST_WHERE + populateInClause(datasets));
            ids = existingDatasets.list();
        } finally {
            releaseSession(session);
        }

        String errorWithDatasetIDs = "These dataset IDs don't exist in the source database: ";
        if (datasets.removeAll(ids) && datasets.size() == 0) {
            return onSuccess;
        } else {
            return errorWithDatasetIDs + StringUtils.join(" ", datasets);
        }
    }

    /**
     * Verify the existence of datasets in the source database requiring import for the first time.
     * @param datasets a HashSet of potential database IDs
     * @return a new instance of the intersection of dataset IDs in datasets that are eligible
     * for a new import.
     */
    public HashSet<Integer> findImportNewDatasets(HashSet<Integer> datasets) {
        String importNewDatasetsQuery = "SELECT dataset_id FROM " + getQualifiedSrcDsTableName()
            + " WHERE dataset_name NOT IN (SELECT dataset_name FROM " + DATASET_TABLE_NAME
            + ") AND dataset_id IN " + populateInClause(datasets);

        Session session = getSession();
        HashSet<Integer> ids;

        try {
            ids = new HashSet<Integer>(
                session.createSQLQuery(importNewDatasetsQuery).list());
        } finally {
            releaseSession(session);
        }
        datasets.retainAll(ids);
        return new HashSet<Integer>(datasets);
    }

    /**
     * Verify the existence of datasets in the source database requiring import
     * as reserved datasets. Both the dataset name and id must be the same and
     * the reserved dataset must have no transactions.
     * both databases.
     * @param datasets a hashset of potential database IDs
     * @return a new instance of the intersection of reserved dataset IDs that are eligible
     * for a new import.
     */
    public HashSet<Integer> findImportKeepDatasets(HashSet<Integer> datasets) {
        String matchingDatasetsQuery =
            "SELECT ds.dataset_id, count(transaction_id) AS txnCount FROM "
            + getQualifiedSrcDsTableName() + " ds JOIN " + DATASET_TABLE_NAME
            + " USING (dataset_name, dataset_id)"
            + " JOIN tutor_transaction USING (dataset_id)"
            + " WHERE ds.dataset_id IN " + populateInClause(datasets);

        Session session = getSession();
        HashSet<Integer> ids = new HashSet<Integer>();

        try {
            List<Object[]> results = session.createSQLQuery(matchingDatasetsQuery).list();
            for (Object[] row : results) {
                Integer datasetId = (Integer)row[0];
                int txnCount = ((BigInteger)row[1]).intValue();
                if (txnCount == 0) { ids.add(datasetId); }
            }
        } finally {
            releaseSession(session);
        }
        datasets.retainAll(ids);
        return new HashSet<Integer>(datasets);
    }

    /**
     * Verify the existence of datasets in the source database requiring a merge because the
     * dataset name is present in the destination database.
     * @param datasets a HashSet of potential database IDs
     * @param mergeType the merge type
     * @return a new instance of the intersection of dataset IDs in datasets that are eligible
     * to be merged.
     */
    public HashSet<Integer> findMergeDatasets(HashSet<Integer> datasets, int mergeType) {
        String existingDatasetsQuery = "SELECT ds.dataset_id FROM "
            + getQualifiedSrcDsTableName() + " ds JOIN " + DATASET_TABLE_NAME
            + " USING (dataset_name)"
            + " WHERE ds.dataset_id IN " + populateInClause(datasets);

        Session session = getSession();
        HashSet<Integer> ids;

        try {
            ids = new HashSet<Integer>(
                session.createSQLQuery(existingDatasetsQuery).list());

            /* Check to make sure the transaction count is greater than what we already have. */
            if (mergeType == 1 && datasets.size() > 0 && ids.size() > 0) {
                Iterator<Integer> it = datasets.iterator();
                Integer sourceId;
                /* Must use iterator if we're removing from the list */
                while (it.hasNext()) {
                    sourceId = (Integer) it.next();
                    logger.debug("Testing merge with source appended dataset id " + sourceId
                        + " for " + "new transactions.");
                    String existingIdQuery = "SELECT ds.dataset_id FROM " + DATASET_TABLE_NAME
                        + " ds JOIN " + getQualifiedSrcDsTableName()
                        + " srcds USING (dataset_name) "
                        + "WHERE srcds.dataset_id = " + sourceId.intValue();
                    Integer existingId = (Integer) session.createSQLQuery(
                        existingIdQuery).list().get(0);
                    logger.debug(existingId);

                    logger.debug("Retrieving existing transaction count");
                    String existingTxCountQuery = "SELECT count(transaction_id) "
                        + "FROM tutor_transaction "
                        + "WHERE dataset_id = "
                        + existingId.intValue();
                    BigInteger existingTxCount = (BigInteger) session.createSQLQuery(
                        existingTxCountQuery).list().get(0);
                    logger.debug(existingTxCount);

                    logger.debug("Retrieving new transaction count");
                    String newTxCountQuery = "SELECT count(transaction_id) FROM " + sourceDB
                        + ".tutor_transaction WHERE dataset_id = " + sourceId.intValue();
                    BigInteger difference = ((BigInteger) session.createSQLQuery(
                        newTxCountQuery).list().get(0)).subtract(existingTxCount);
                    logger.debug("Difference between source tx count and existing tx count = "
                        + difference.longValue());

                    if (difference.longValue() <= 0) {
                        logger.info("Removing dataset id " + sourceId
                            + " because the transaction count is the same.");
                        it.remove();
                    }
                }
            }
        } finally {
            releaseSession(session);
        }
        datasets.retainAll(ids);
        return new HashSet<Integer>(datasets);
    }

    /**
     * Set the source database for the purpose of verifying datasets and merging all datasets.
     * @param sourceDB the name of the source database
     */
    public void setSourceDB(String sourceDB) {
        this.sourceDB = sourceDB;
    }

    /**
     * Set the name of the mapping database, used for student ID map.
     * @param mappingDb the name of the database
     */
    public void setMappingDb(String mappingDb) {
        this.mappingDb = mappingDb;
    }

    /**
     * Helper method to concatenate the source database name with the dataset table name.
     * @return The combined database and table name.
     */
    private String getQualifiedSrcDsTableName() {
        return sourceDB + "." + DATASET_TABLE_NAME;
    }

    /**
     * Necessary to provide multiple variables inside of a SQL IN clause when not using hibernate.
     * @param <T> Generic type of element
     * @param elements The collection containing the elements to include
     * @return A joined string of the where clause
     */
    private <T> String populateInClause(Collection<T> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Iterator<T> it = elements.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) { sb.append(", "); }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Method to launch the MySQL process calling our .sql file with the 'LOAD DATA INFILE'
     * commands.
     * @param filename full path to the .sql file
     * @return true if no errors were encountered otherwise return false
     */
    private boolean insertTransactions(String filename) {

        login = HibernateDaoFactory.DEFAULT.getAnalysisDatabaseLogin();

        mysqlDb = HibernateDaoFactory.DEFAULT.getAnalysisDatabaseName();
        mysqlUser = login.get("user");
        mysqlPwd = login.get("password");

        Runtime runTime = Runtime.getRuntime();

        String[] commands = new String[]{MYSQL, mysqlDb, userFlag + mysqlUser, pwdFlag + mysqlPwd,
                executeFlag, sourceCommand + filename};
        String[] commandsToPrint = new String[]{MYSQL, mysqlDb,
                userFlag + mysqlUser, pwdFlag + "*****",
                executeFlag, sourceCommand + filename};
        try {
            logger.info("Beginning insert of transactions. Command Params:"
                    + Arrays.toString(commandsToPrint));

            Process proc = runTime.exec(commands);

            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            errorGobbler.start();
            outputGobbler.start();

            try {
                if (proc.waitFor() == 1) {
                    logger.error("Return code indicates error in running the DBMerge mysql "
                            + "process");
                    return false;
                }
            } catch (InterruptedException exception) {
                logger.error("Interrupted Exception while running the DBMerge mysql process.",
                        exception);
                return false;
            }

        } catch (IOException exception) {
            logger.error("Attempt to spawn a DBMerge mysql process failed for file: "
                    + filename, exception);
            return false;
        }
        return true;
    }

    /**
     * Delete the newly created out files.
     * @param filename the file name
     */
    private void cleanupTransactionFiles(String filename) {

        logger.debug("os.name property: " + System.getProperty("os.name"));
        if (System.getProperty("os.name").toLowerCase().indexOf("win") == -1) {
            deleteOutfiles(filename);
        } else {
            File file;
            String directory = getDataDirectory() + OUTFILE_DIRECTORY;

            try {
                file = new File(directory + TUTOR_TX_FILE);
                if (!file.delete()) {
                    logger.error("Could not delete tutor transaction outfile: "
                            + directory + TUTOR_TX_FILE);
                }
            } catch (NullPointerException exception) {
                logger.error("Error creating file object for tutor_transaction outfile: "
                        + directory + TUTOR_TX_FILE, exception);
            }

            try {
                file = new File(directory + TX_COND_MAP_FILE);
                if (!file.delete()) {
                    logger.error("Could not delete transaction condition map outfile: "
                            + directory + TX_COND_MAP_FILE);
                }
            } catch (NullPointerException exception) {
                logger.error("Error creating file object for transaction_condition_map outfile: "
                        + directory + TX_COND_MAP_FILE, exception);
            }

            try {
                file = new File(directory + TX_SKILL_MAP_FILE);
                if (!file.delete()) {
                    logger.error("Could not delete transaction skill map outfile: "
                            + directory + TX_SKILL_MAP_FILE);
                }
            } catch (NullPointerException exception) {
                logger.error("Error creating file object for transaction_skill_map outfile: "
                        + directory + TX_SKILL_MAP_FILE, exception);
            }

            try {
                file = new File(directory + TX_SKILL_EVT_MAP_FILE);
                if (!file.delete()) {
                    logger.error("Could not delete transaction skill event map outfile: "
                            + directory + TX_SKILL_EVT_MAP_FILE);
                }
            } catch (NullPointerException exception) {
                logger.error("Error creating file object for transaction_skill_event_map outfile: "
                        + directory + TX_SKILL_EVT_MAP_FILE, exception);
            }
        }
    }

    /**
     * Try to delete the out files if we are running on a *nix server.
     * @param filename the file name
     */
    public void deleteOutfiles(String filename) {
        Runtime runTime = Runtime.getRuntime();

        String[] commands = new String[]{MYSQL, mysqlDb, userFlag + mysqlUser, pwdFlag + mysqlPwd,
                executeFlag, sourceCommand + filename};

        try {
            Process proc = runTime.exec(commands);

            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            errorGobbler.start();
            outputGobbler.start();

            try {
                if (proc.waitFor() == 1) {
                    logger.error("Return code indicates error in deleting the load data outfiles "
                            + "process");
                }
            } catch (InterruptedException exception) {
                logger.error("Interrupted Exception while running the delete load data outfiles "
                        + "process", exception);
            }

        } catch (IOException exception) {
            logger.error("Attempt to spawn a DBMerge mysql process failed for file: "
                    + filename, exception);
        }
    }

    /**
     * Rollback inserted data because of a SQLException.
     * @param action the merge action number
     */
    private void rollbackMerge(int action) {

        /* mergeOnly ran so only remove new inserts not what already existed */
        if (action == 2) {
            try {
                logger.info("Rolling back merge cascading from the tutor transaction table");
                callSP(buildSPCall(ROLLBACK_MERGE_TRANSACTION_PROC));
            } catch (SQLException exception) {
                logger.error("Error trying to rollback from the tutor_transaction table.",
                        exception);
            }

            try {
                logger.info("Rolling back subject data");
                callSP(buildSPCall(ROLLBACK_MERGE_SUBJECT_DATA_PROC));
            } catch (SQLException exception) {
                logger.error("Error trying to rollback the subject data.",
                        exception);
            }

            try {
                logger.info("Rolling back merge cascading from the dataset level table");
                callSP(buildSPCall(ROLLBACK_MERGE_DATASET_LEVEL_PROC));
            } catch (SQLException exception) {
                logger.error("Error trying to rollback from the dataset_level table.",
                        exception);
            }
        } else {
            try {
                logger.info("Rolling back merge cascading from the tutor transaction table");
                callSP(buildSPCall(ROLLBACK_TRANSACTION_PROC));
            } catch (SQLException exception) {
                logger.error("Error trying to rollback from the tutor_transaction table.",
                        exception);
            }

            try {
                logger.info("Rolling back subject data");
                callSP(buildSPCall(ROLLBACK_SUBJECT_DATA_PROC, "\"" + mappingDb + "\""));
            } catch (SQLException exception) {
                logger.error("Error trying to rollback the subject data.", exception);
            }

            try {
                logger.info("Rolling back merge cascading from ds_dataset table");
                callSP(buildSPCall(ROLLBACK_DATASET_PROC));
            } catch (SQLException exception) {
                logger.error("Error trying to rollback from the ds_dataset table.", exception);
            }
        }
    }

    /**
     * Drop id mapping tables and tables that track duplicate records between the source and
     * destination databases.
     */
    private void dropMergeTables() {
        // [2012/4/30 - ysahn] Comment this when testing, if you want to leave the temp tables

        try {
            callSP(buildSPCall(DROP_MAP_TABLES_PROC));
        } catch (SQLException exception) {
            logger.error("Error dropping id mapping tables. ", exception);
        }

        try {
            callSP(buildSPCall(DROP_HELPER_TABLES_PROC));
        } catch (SQLException exception) {
            logger.error("Error dropping id mapping tables. ", exception);
        }
    }

    /**
     * When the db merge finishes, successful or not, add modify (on success) and merge completed
     * statements to the dataset_system_log.
     * @param success boolean indicating if there were any errors.
     */
    private void updateStatus(boolean success) {
        if (success) {
            try {
                callSP(buildSPCall(MODIFY_STATUS_PROC));
            } catch (SQLException exception) {
                logger.error("Error updating dataset_system_log with modify entries. ", exception);
            }

            try {
                callSP(buildSPCall(COMPLETE_STATUS_PROC, success));
            } catch (SQLException exception) {
                logger.error("Error updating dataset_system_log with for merge completion. ",
                        exception);
            }
        } else {
            try {
                callSP(buildSPCall(COMPLETE_STATUS_PROC, success));
            } catch (SQLException exception) {
                logger.error("Error updating dataset_system_log with for merge completion. ",
                        exception);
            }
        }
    }

    /**
     * Perform the data directory query on the system variable.
     * @return The data directory file path
     */
    private String getDataDirectory() {
        Session session = getSession();
        String dataDirectory;
        try {
            dataDirectory = (String) session.createSQLQuery(DATA_DIRECTORY_QUERY).list().get(0);
        } finally {
            releaseSession(session);
        }
        return dataDirectory;
    }

    /**
     * Gets the count from dbm_max_table_counts.
     * @param datasets a HashSet of dataset ids
     * @return true if the table count has reached the max table count
     */
    public boolean hasMaxTableCountEntry(HashSet<Integer> datasets) {
        String maxTableCountsQuery = "SELECT count(*) FROM dbm_max_table_counts mtc"
            + "JOIN ds_dataset dest ON mtc.dataset_id = dest.dataset_id"
            + "JOIN " + getQualifiedSrcDsTableName() + " src USING (dataset_name)";
        Session session = getSession();
        Integer count;
        try {
            count = (Integer) session.createQuery(maxTableCountsQuery).list().get(0);
        } finally {
            releaseSession(session);
        }
        return (count == datasets.size());
    }

    /**
     * Class to get output from the stream of the MySQL process we spawn
     * @author dspencer
     *
     */
    class StreamGobbler extends Thread {
        /** The InputStream instance */
        private InputStream is;
        /** The type of the stream: ERROR, INPUT, OUTPUT */
        private String type;

        /**
         * Constructor
         * @param is the input stream to buffer
         * @param type identifies the kind of InputStream
         */
        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        /** Starts the thread */
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (type.equals("ERROR")) {
                        logger.error(line);
                    } else {
                        logger.info(line);
                    }
                }
             } catch (IOException ioe) {
                    logger.error("IOException in capturing DBMerge Output Stream", ioe);
             }
        }
    }
}
