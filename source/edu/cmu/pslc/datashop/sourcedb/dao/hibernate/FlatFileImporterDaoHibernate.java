/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.sourcedb.dao.hibernate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.sourcedb.dao.FlatFileImporterDao;
import edu.cmu.pslc.datashop.sourcedb.dao.SourceDbDaoFactory;
import edu.cmu.pslc.importdata.ImportConstants;

/**
 * Hibernate and Spring implementation of the FlatFileImporterDao.
 *
 * @author Shanwen Yu
 * @version $Revision: 15863 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-02-27 13:15:42 -0500 (Wed, 27 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FlatFileImporterDaoHibernate extends SourceDbHibernateAbstractDao
                implements FlatFileImporterDao {

    /** Query to create the temporary analysis database. */
    private static final String CREATE_DB_QUERY = "CREATE DATABASE IF NOT EXISTS ";

    /**
     * Creates the database adb_source.
     * @return true if the database is created successfully, false otherwise
     */
    public boolean createDatabase() {
        String query = CREATE_DB_QUERY + SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName();
        try {
            logTrace("createDatabase, query[", query, "]");
            executeSQLUpdate(query);
        } catch (Exception exception) {
            logger.error("Error executing " + query, exception);
            return false;
        }
        return true;
    }

    /** Query to create the temporary analysis database. */
    private static final String DROP_DB_QUERY = "DROP DATABASE IF EXISTS ";

    /**
     * Drops the database adb_source.
     * @return true if the database is dropped successfully, false otherwise
     */
    public boolean dropDatabase() {
        String query = DROP_DB_QUERY + SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName();
        try {
            logTrace("createDatabase, query[", query, "]");
            executeSQLUpdate(query);
        } catch (Exception exception) {
            logger.error("Error executing " + query, exception);
            return false;
        }
        return true;
    }

    /**
     * Drop and create the analysis database tables in the adb_source database
     * by launching the MySQL process.
     * @param createTablesFilename the name of the SQL file to create ADB tables
     * @return true if no errors were encountered otherwise return false
     */
    public boolean createTables(String createTablesFilename) {
        return callMySQL(createTablesFilename);
    }

    /** Name of MySQL executable **/
    private static final String MYSQL = "mysql";
    /** Flag preceding the username */
    private final String userFlag = "--user=";
    /** Flag preceding the password */
    private final String pwdFlag = "--password=";
    /** Flag telling MySQL to execute the following command */
    private final String executeFlag = "-e";
    /** Source file tells MySQL to run the following SQL file */
    private final String sourceCommand = "source ";

    /**
     * Drop and create the analysis database tables in the adb_source database
     * by launching the MySQL process.
     * @param createTablesFilename the name of the SQL file to create ADB tables
     * @return true if no errors were encountered otherwise return false
     */
    private boolean callMySQL(String createTablesFilename) {

        Map<String, String> login = SourceDbHibernateDaoFactory.DEFAULT.getSourceDatabaseLogin();
        String mysqlDb = SourceDbHibernateDaoFactory.DEFAULT.getSourceDatabaseName();
        String mysqlUser = login.get("user");
        String mysqlPwd = login.get("password");

        Runtime runTime = Runtime.getRuntime();

        String[] commands = new String[] {MYSQL, mysqlDb, userFlag + mysqlUser, pwdFlag + mysqlPwd,
                executeFlag, sourceCommand + createTablesFilename};
        try {
            Process proc = runTime.exec(commands);

            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            errorGobbler.start();
            outputGobbler.start();

            try {
                if (proc.waitFor() == 1) {
                    logger.error("Return code indicates error in running mysql command");
                    return false;
                }
            } catch (InterruptedException exception) {
                logger.error("InterruptedException while running mysql command.", exception);
                return false;
            }

        } catch (IOException exception) {
            logger.error("Attempt to spawn a mysql process failed for file: "
                    + createTablesFilename, exception);
            return false;
        }
        return true;
    }

    /** Query to drop the heading column map table. */
    private static final String DROP_HCM_QUERY = "DROP TABLE IF EXISTS ";

    /** Query to create the heading column map table. */
    private static final String CREATE_HCM_QUERY = "CREATE TABLE ";

    /**
     * Create table ffi_heading_column_map.
     * @return boolean true if the process is successful, false otherwise
     */
    public boolean createHeadingColumnMap() {
        String sourceDb = SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName();
        String query = DROP_HCM_QUERY + sourceDb + ".ffi_heading_column_map;";
        try {
            logTrace("createHeadingColumnMap, query[", query, "]");
            executeSQLUpdate(query);

            query = CREATE_HCM_QUERY + sourceDb
                    + ".ffi_heading_column_map ("
                    + " standard_name VARCHAR(25) NOT NULL,"
                    + " heading VARCHAR(64) NOT NULL,"
                    + " column_name VARCHAR(64) NOT NULL,"
                    + " column_value TEXT,"
                    + " sequence TINYINT DEFAULT 1,"
                    + " CONSTRAINT ffi_hcm_key PRIMARY KEY (column_name) )"
                    + " ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin";

            logTrace("createHeadingColumnMap, query[", query, "]");
            executeSQLUpdate(query);
        } catch (HibernateException exception) {
            logger.error("Error executing " + query, exception);
            return false;
        } catch (SQLException exception) {
            logger.error("Error executing " + query, exception);
            return false;
        }
        return true;
    }

    /**
     * Insert into table ffi_heading_column_map.
     * @param standardName standard name of the heading
     * @param heading heading in the file
     * @param columnName column name
     * @param columnValue the string in parenthesis, like Default in "KC (Default)"
     * @param sequence the order the multiple columns like Level, KC, Selection, etc.
     * @return boolean true if the process is successful, false otherwise
     */
    public boolean insertIntoHeadingColumnMap(String standardName,
            String heading, String columnName, String columnValue, int sequence) {
        Session session = getSession();
        int rowCount = 0;
        String sourceDb = SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName();
        String query = "SELECT COUNT(*) FROM " + sourceDb + ".ffi_heading_column_map "
                     + " WHERE column_name = '" + columnName + "'";
        try {
            logTrace("insertIntoHeadingColumnMap, query[", query, "]");
            SQLQuery sqlQuery = session.createSQLQuery(query);

            if (sqlQuery != null) {
                rowCount = ((BigInteger)sqlQuery.list().get(0)).intValue();
            }
            if (rowCount == 0) {
                query = "INSERT INTO " + sourceDb + ".ffi_heading_column_map VALUES ("
                       + " '" + standardName + "', '" + heading + "', '" + columnName
                       + "', '" + columnValue + "', " + sequence + ")";
                logTrace("insertIntoHeadingColumnMap, query[", query, "]");
                executeSQLUpdate(query);
            }
        } catch (HibernateException exception) {
            logger.error("Error executing " + query, exception);
            return false;
        } catch (SQLException exception) {
            logger.error("Error executing " + query, exception);
            return false;
        } finally {
            releaseSession(session);
        }
        return true;
    }

    /**
     * Create table ffi_import_file_data.
     * There are two pairs of time-related columns.
     *     Transaction Time:  time &  tx_time_datetime
     *     Problem Start Time:  problem_start_time & problem_start_time_datetime
     * The first column is a string of the time information from the file.
     * The second column is the datetime version of that string that is then used
     *  1) to verify the timestamp, and
     *  2) to populate the database.
     *
     * @param columnName a list of the column names
     * @param fileInfoTableName the db table name to hold file info
     * @return boolean true if the process is successful, false otherwise
     */
    public boolean createTableImportFileData(ArrayList<String> columnName,
                                             String fileInfoTableName)  {

        String sourceDb = SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName();
        String query = "DROP TABLE IF EXISTS " + sourceDb + "." + fileInfoTableName;
        try {
            executeSQLUpdate(query);
        } catch (Exception exception) {
            logger.error("Error executing " + query, exception);
        }
        query = "CREATE TABLE IF NOT EXISTS " + sourceDb + "." + fileInfoTableName + " ( \n"
              + "line_num INT AUTO_INCREMENT, \n"
              + "import_file_id INT NOT NULL, \n"
              + "student_id BIGINT, \n";
        String myColumnName = "";
        for (int i = 0; i < columnName.size(); i++) {
            myColumnName = columnName.get(i);
            if (!myColumnName.equals("")) {
                // feedback_text, KC, and CF are TEXT
                if ((myColumnName.equals(ImportConstants.FEEDBACK_TEXT_HEADING.replace(" ", "_")
                        .toLowerCase()))
                        || (((myColumnName.startsWith(
                                ImportConstants.KC_HEADING.replace("(", "_").toLowerCase()))
                            && !(myColumnName.startsWith(
                                    ImportConstants.KC_CATEGORY_HEADING
                                    .replace("(", "_").toLowerCase()))))
                        || (myColumnName.startsWith(
                                ImportConstants.CUSTOM_FIELD_HEADING
                                .replace("(", "_").toLowerCase()))) {
                    query += myColumnName + " TEXT, \n";
                } else if ((myColumnName.equals(ImportConstants.PROBLEM_NAME_HEADING.replace(" ", "_")
                        .toLowerCase()))) {
                    query += myColumnName + " VARCHAR(2048), \n";
                } else {
                    query += myColumnName + " VARCHAR(255), \n";
                }
            }
        }
        query += "dataset_level_id INT, \n";
        query += "problem_id BIGINT, \n";
        query += "subgoal_id BIGINT, \n";
        query += "sg_hash CHAR(32), \n";
        query += "tx_time_datetime DATETIME, \n";
        query += "problem_start_time_datetime DATETIME, \n";
        query += "actual_session_id BIGINT, \n";
        query += "problem_event_id BIGINT, \n";
        query += "PRIMARY KEY (line_num) ) ENGINE=MyISAM CHARACTER SET utf8 COLLATE utf8_bin";
        try {
            logTrace("createTableImportFileData, query[", query, "]");
            executeSQLUpdate(query);
        } catch (HibernateException exception) {
            logger.error("Error executing " + query, exception);
            return false;
        } catch (SQLException exception) {
            logger.error("Error executing " + query, exception);
            return false;
        }
        return true;
    }

    /**
     * Load data into table ffi_import_file_data.
     * @param filename the name of the input file
     * @param columns a list of the column names
     * @param importFileId id of the import file
     * @param lineTerminator the character(s) that ends a line
     * @param fileInfoTableName the db table name to hold file info
     * @param includeUserKCMs whether or not to include user-created KC Models
     * @param the default KC Column Id from the HeadingReport object
     * @return the number of total rows imported for the given file
     */
    public int loadDataIntoImportFileData(String filename, ArrayList<String> columns,
                                          int importFileId, String lineTerminator,
                                          String fileInfoTableName, Boolean includeUserKCMs, Integer defaultColumnId) {
        // By default, include user-created KCMs
        includeUserKCMs = includeUserKCMs == null ? true : includeUserKCMs;

        int rowCount = 0;
        String thisColumn = "";
        String query = "LOAD DATA INFILE \'" + filename.replaceAll("'", "\\\\'") + "\'"
            + " INTO TABLE "
            + SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName() + "." + fileInfoTableName
            + " FIELDS TERMINATED BY \'\t\'"
            + " ESCAPED BY \'\\\\\'"
            + " LINES TERMINATED BY \'" + lineTerminator + "\'"
            + " IGNORE 1 LINES "
            + " ( ";

        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < columns.size(); i++) {
           thisColumn = columns.get(i).toString();

           if (includeUserKCMs || !thisColumn.matches("kc.*")
                   || thisColumn.matches("kc_*" + defaultColumnId) || thisColumn.matches("kc_+category_*" + defaultColumnId)) {
               sBuffer.append(thisColumn);
               sBuffer.append(", ");
           }
        }
        // Remove the last comma from the line.
        query += sBuffer.substring(0, sBuffer.length() - 2);
        query += "  )";
        query += " SET import_file_id = " + importFileId + ", ";
        // it is much faster to trim the value here than do it column by column in stored procedure
        StringBuffer sBuffer2 = new StringBuffer();
        for (int i = 0; i < columns.size(); i++) {
            thisColumn = columns.get(i).toString();

            if (includeUserKCMs || !thisColumn.matches("kc.*")
                    || thisColumn.matches("kc_*" + defaultColumnId) || thisColumn.matches("kc_+category_*" + defaultColumnId)) {
                sBuffer2.append(thisColumn + " = TRIM(" + thisColumn + ")");
                sBuffer2.append(", ");
            }



         }

        query += sBuffer2.substring(0, sBuffer2.length() - 2);
        try {
            logTrace("loadDataIntoImportFileData, query[", query, "]");
            executeSQLUpdate(query);
            rowCount = getRowCount(importFileId, fileInfoTableName);
        } catch (HibernateException exception) {
            logger.error("Error executing " + query, exception);
            return rowCount;
        } catch (SQLException exception) {
            logger.error("Error executing " + query, exception);
            return rowCount;
        }
        return rowCount;
    }

    /**
     * Get row count of an import file given import_file_id.
     * @param importFileId the id of the input file
     * @param fileInfoTableName the db table name to hold file info
     * @return the count of all the rows of a file in the import_db.ffi_import_file_data
     */
    public int getRowCount(int importFileId, String fileInfoTableName) {
        Session session = getSession();

        int rowCount = 0;
        String query = "SELECT COUNT(*) FROM "
            + SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName()
            + "." + fileInfoTableName
            + " WHERE import_file_id = " + importFileId;
        logTrace("getRowCount, query[", query, "]");

        try {
            SQLQuery sqlQuery = session.createSQLQuery(query);

            if (sqlQuery != null) {
                rowCount = Integer.parseInt(sqlQuery.list().get(0).toString());
            }
        } finally {
            releaseSession(session);
        }
        return rowCount;
    }

    /** Constant of parameter index */
    private static final int PARAM_INDEX_ONE = 1;
    /** Constant of parameter index */
    private static final int PARAM_INDEX_TWO = 2;
    /** Constant of parameter index */
    private static final int PARAM_INDEX_THREE = 3;
    /** Constant of parameter index */
    private static final int PARAM_INDEX_FOUR = 4;

    /** Name of the main procedure to start data verification */
    private static final String RUN_VERIFY_DATA_PROC = ".ffi_verify_data";
    /**
     * Verify the data in the import_file_data table.  Put warnings and error messages
     * in the import database.
     * Loop on each column: check each column for validity.
     * For each warning increment warning_count and append message to warning_message.
     * For each error increment error_count and append message to error_message.
     * If error count > 0, return false.
     * If error count > 100, return false and stop verifying the data.
     * @param threshold the number of threshold set in the command line
     * @param importDbName the name of the import_db database
     * @param fileInfoTableName the name of the table created to temporarily hold file info
     * @return true if there are no errors, false otherwise
     */
    public boolean verifyData(int threshold, String importDbName, String fileInfoTableName) {
        Object[] args = {'?', '?', '?', '?'};
        Session session = getSession();
        Connection conn;
        CallableStatement cs;
        boolean result = false;
        String sp = SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName() + RUN_VERIFY_DATA_PROC;
        logger.debug(buildSPCall(sp, args));
        try {
            conn = session.connection();
            cs = conn.prepareCall(buildSPCall(sp, args));
            cs.setInt(PARAM_INDEX_ONE, threshold);
            cs.setString(PARAM_INDEX_TWO, importDbName);
            cs.setString(PARAM_INDEX_THREE, fileInfoTableName);
            cs.registerOutParameter(PARAM_INDEX_FOUR, Types.BOOLEAN);
            cs.executeUpdate();
            result = cs.getBoolean(PARAM_INDEX_FOUR);
        } catch (SQLException exception) {
            logger.error("Error calling data verification stored procedure", exception);
        } finally {
            releaseSession(session);
        }
        return result;
    }

    /**
     * Completes the stub entry in the ds_dataset table for the given ImportStatusItem.
     * This keeps the populate database stored procedure from having to distinguish
     * between addressing import_db or import_db_test.  Cannot use Hibernate Items on
     * the source database.
     * @param datasetId the id to use for the dataset
     * @param importStatusItem importStatusItem of the current flat file import
     * @return whether the dataset was created
     */
    public boolean createDatasetItem(Integer datasetId, ImportStatusItem importStatusItem) {
        Session session = getSession();

        Integer domainId = (Integer) DaoFactory.DEFAULT.getDomainDao().findByName(
                importStatusItem.getDomainName()).getId();
        Integer learnlabId = (Integer) DaoFactory.DEFAULT.getLearnlabDao().findByName(
                importStatusItem.getLearnlabName()).getId();

        String sourceDb = SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName();
        try {
            logTrace("create dataset item");
            String datasetName = importStatusItem.getDatasetName().replaceAll("'", "\\\\'");
            session.createSQLQuery("INSERT INTO " + sourceDb + ".ds_dataset "
                + "(dataset_id, dataset_name, domain_id, learnlab_id, study_flag, "
                + "auto_set_school_flag, auto_set_times_flag, appears_anon_flag) "
                + " SELECT " + datasetId + ", '" + datasetName + "', " + domainId + ", "
                + learnlabId + ", '" + DatasetItem.STUDY_FLAG_NOT_SPEC
                + "', TRUE, TRUE, 'not_reviewed'").executeUpdate();
        } catch (Exception exception) {
            logger.error("Exception occurred.", exception);
            return false;
        } finally {
            releaseSession(session);
        }

        return true;
    }

    /**
     * Create a new entry in the sourceDb ds_dataset table for the given ImportStatusItem.
     * Note: this is the original version of the method, now only used when calling FFI
     * in the new 'merge' mode.
     * @param importStatusItem importStatusItem of the current flat file import
     * @return dataset_id of the newly created Dataset Item
     */
    public Integer createDatasetItem(ImportStatusItem importStatusItem) {
        Session session = getSession();

        Integer domainId = (Integer) DaoFactory.DEFAULT.getDomainDao().findByName(
                importStatusItem.getDomainName()).getId();
        Integer learnlabId = (Integer) DaoFactory.DEFAULT.getLearnlabDao().findByName(
                importStatusItem.getLearnlabName()).getId();

        Integer datasetId;
        String sourceDb = SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName();
        try {
            logTrace("create dataset item");
            String datasetName = importStatusItem.getDatasetName().replaceAll("'", "\\\\'");
            session.createSQLQuery("INSERT INTO " + sourceDb + ".ds_dataset "
                + "(dataset_name, domain_id, learnlab_id, study_flag, auto_set_school_flag, "
                + "auto_set_times_flag, appears_anon_flag) "
                + " SELECT '" + datasetName + "', " + domainId + ", "
                + learnlabId + ", '" + DatasetItem.STUDY_FLAG_NOT_SPEC
                + "', TRUE, TRUE, 'not_reviewed'").executeUpdate();

            datasetId = (Integer) session.createSQLQuery("SELECT dataset_id "
                    + "FROM " + sourceDb + ".ds_dataset "
                    + "WHERE dataset_name = '" + datasetName
                    + "'").list().get(0);

        } catch (Exception exception) {
            logger.error("Exception occurred.", exception);
            datasetId = null;
        } finally {
            releaseSession(session);
        }

        return datasetId;
    }

    /** Name of the procedure to kickoff populating the analysis database tables. */
    private static final String POPULATE_DATABASE_SP = ".ffi_populate_database";

    /**
     * Name of the procedure to drop the tables created
     * during the populate database stored procedure,
     * and the ffi_import_file_data table.
     */
    private static final String FFI_POPDB_DROP_TABLES_SP = ".ffi_drop_helper_tables";

    /**
     * Run the stored procedures which take the data from the import_file_data table
     * and populates the analysis database tables.
     * Then drop all the helper tables including the ffi_import_file_data table.
     * @param datasetId new dataset_id primary key
     * @param anonFlag true indicates to anonymize the student column
     * @return true if successful, false otherwise
     */
    public boolean populateDatabase(Integer datasetId, boolean anonFlag) {
        boolean status = true;
        try {
            callSP(buildSPCall(SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName()
                + POPULATE_DATABASE_SP, datasetId.intValue(), anonFlag));
        } catch (Exception exception) {
            logger.error("populateDatabase exception.", exception);
            status = false;
        } finally {
            // Drop the helper tables created and used by the stored procedure.
            try {
                logger.info("Dropping helper tables with SP: " + FFI_POPDB_DROP_TABLES_SP);
                callSP(buildSPCall(SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName()
                    + FFI_POPDB_DROP_TABLES_SP));
            } catch (Exception exception) {
                logger.error("populateDatabase Exception dropping tables: ", exception);
            }
        }
        return status;
    }

    /**
     * Class to get output from the stream of the MySQL process we spawn
     * @author Duncan Spencer
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
                 logger.error("IOException in capturing output stream", ioe);
             }
        }
    }

}
