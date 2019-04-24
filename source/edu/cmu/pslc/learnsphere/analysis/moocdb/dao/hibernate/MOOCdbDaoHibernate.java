/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.dao.hibernate;

import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.TIMESTAMP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.MOOCdbDao;
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.hibernate.CourseraDbsRestoreDaoHibernate.StreamGobbler;
import edu.cmu.pslc.learnsphere.analysis.moocdb.item.MOOCdbItem;

/**
 * Data access object to retrieve the data from the moocdbs table via Hibernate.
 *
 * @author Hui Cheng
 * @version $Revision: 14215 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2017-07-10 14:09:28 -0400 (Mon, 10 Jul 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MOOCdbDaoHibernate extends edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate
        implements MOOCdbDao {
    /**
     * Standard get for MOOCdb item by id.
     * @param id the id of the desired MOOCdb item
     * @return the matching MOOCdbItem or null if none found
     */
    public MOOCdbItem get(Long id) {
            return (MOOCdbItem)get(MOOCdbItem.class, id);
    }

    /**
     * Standard find for a MOOCdb item by id.
     * @param id id of the object to find
     * @return MOOCdbItem
     */
    public MOOCdbItem find(Long id) {
            return (MOOCdbItem)find(MOOCdbItem.class, id);
    }

    /**
     * Standard "find all" for MOOCdb item.
     * @return a List of objects
     */
    public List<MOOCdbItem> findAll() {
            return getHibernateTemplate().find("from " + MOOCdbItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //
    /**related table names*/
    private static String MOOCDB_TABLE_NAME = "moocdbs";
    private static String MOOCDB_CLEAN_TABLE_NAME = "moocdb_clean";
    private static String DROP_DB_QUERY = "DROP DATABASE IF EXISTS ";
    private static String CREATE_DB_QUERY = "CREATE DATABASE IF NOT EXISTS ";
    private static String CREATE_USER_QUERY = "CREATE USER ";
    private static String CREATE_USER_IDENTIFIED = "IDENTIFIED BY ";
    private static String DROP_USER = "DROP USER ";
    private static String SQL_AT_HOST = "@'%' ";
    private static String SQL_USER_EXIST = "SELECT COUNT(*) as user_cnt FROM information_schema.USER_PRIVILEGES WHERE grantee = ";
    private static String SQL_DATABASE_EXIST = "SELECT COUNT(*) as tab_cnt FROM information_schema.TABLES where table_schema = ";
    private static String SQL_DATABASE_TABLES = "SELECT table_name FROM information_schema.TABLES where table_schema = ";
    private static String[] EXTRA_MOOCDB_TABLES = {"experiments", "feature_extractions", "longitudinal_features", "models", "user_longitudinal_feature_values"};

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

    public MOOCdbItem getMOOCdbByName(String MOOCdbName){
            Object[] params = {MOOCdbName};
            String query = "from MOOCdbItem where MOOCdbName = ?";

            List<MOOCdbItem> items = (List<MOOCdbItem>)getHibernateTemplate().find(query, params);
            if (items != null && items.size() > 0) {
                    return items.get(0);
            } else {
                    return null;
            }

    }

    public Date getEarliestSubmissionTime(String MOOCdbName) {
            String submissionTable = MOOCdbName + ".submissions";
            String QUERY_SELECT = "SELECT min(submission_timestamp) as min_sub_time " +
                                                    "FROM " + submissionTable;
            logger.info(QUERY_SELECT);

            final List<Object> results = executeSQLQuery(QUERY_SELECT, new PrepareQuery() {
                    public void prepareQuery(SQLQuery query) {
                        addScalars(query, "min_sub_time", TIMESTAMP);
                    }
            });

            if (results == null || results.size() == 0)
                    return null;
            else {
                    return (Date)(results.get(0));
            }
    }

    public void restoreMOOCdb(String MOOCdbName, String backupFileName, String username, String password)
                    throws SQLException, Exception {
            try {
                    restoreDB(MOOCdbName, backupFileName, username, password);
            } catch (Exception exception) {
                    deleteMOOCdb(MOOCdbName);
                    throw exception;
            }

    }

    public void deleteMOOCdb(String MOOCdbName) throws Exception{
            Session session = getSession();
            try {
                    String statement = DROP_DB_QUERY + MOOCdbName;
                    logger.info(statement);
                    session.createSQLQuery(statement).executeUpdate();
            } finally {
                    releaseSession(session);
            }
    }

    public boolean databaseExist(String dbName) {

            String QUERY_SELECT = SQL_DATABASE_EXIST + "'" + dbName + "'";
            logger.info(QUERY_SELECT);

            final List<Object> results = executeSQLQuery(QUERY_SELECT, new PrepareQuery() {
                    public void prepareQuery(SQLQuery query) {
                        addScalars(query, "tab_cnt", INTEGER);
                    }
            });
            if (results == null || results.size() == 0 || (Integer)results.get(0) == 0) {
                    return false;
            } else {
                    return true;
            }
    }

    private String[] getDatabaseTables(String dbName) {

            String QUERY_SELECT = SQL_DATABASE_TABLES + "'" + dbName + "'";
            logger.info(QUERY_SELECT);

            final List<Object> results = executeSQLQuery(QUERY_SELECT, new PrepareQuery() {
                    public void prepareQuery(SQLQuery query) {
                        addScalars(query, "table_name", STRING);
                    }
            });

            if (results == null || results.size() == 0)
                    return null;
            else {
                    String[] tables = new String[results.size()];
                    for (int i = 0; i < results.size(); i++) {
                            tables[i] = (String)results.get(i);
                    }
                    return tables;
            }
    }

    //only call this after checking database exists
    public boolean isMOOCdb(String dbName) {
            String[] moocdbTables = getDatabaseTables(dbName);
            String[] moocdbCleanTables = getDatabaseTables(MOOCDB_CLEAN_TABLE_NAME);
            String[] moocdbCleanWithExtraTables = new String[moocdbCleanTables.length + EXTRA_MOOCDB_TABLES.length];
            for (int i = 0; i < moocdbCleanTables.length; i++)
                    moocdbCleanWithExtraTables[i] = moocdbCleanTables[i];
            for (int i = 0; i < EXTRA_MOOCDB_TABLES.length; i++)
                    moocdbCleanWithExtraTables[moocdbCleanTables.length + i] = EXTRA_MOOCDB_TABLES[i];
            if (moocdbTables == null || moocdbTables.length == 0)
                    return false;
            else if (moocdbTables.length != moocdbCleanWithExtraTables.length)
                    return false;
            else {
                    Arrays.sort(moocdbTables);
                    Arrays.sort(moocdbCleanWithExtraTables);
                    return Arrays.equals(moocdbTables, moocdbCleanWithExtraTables);
            }
    }

    public boolean userExist(String username) {

            String QUERY_SELECT = SQL_USER_EXIST + "'\\'" + username + "\\'@\\'%\\''";
            logger.info(QUERY_SELECT);

            final List<Object> results = executeSQLQuery(QUERY_SELECT, new PrepareQuery() {
                    public void prepareQuery(SQLQuery query) {
                        addScalars(query, "user_cnt", INTEGER);
                    }
            });

            if (results == null || results.size() == 0 || (Integer)results.get(0) == 0) {
                    return false;
            } else {
                    logger.info("user exsits: " + username);
                    return true;
            }
    }


    public void createDBUser(String username, String password) throws SQLException {
            if (userExist(username))
                    return;
            Session session = getSession();
            try {
                    String statement = CREATE_USER_QUERY + "'" +username + "'" +
                                    SQL_AT_HOST + CREATE_USER_IDENTIFIED + "'" + password + "'";
                    logger.info(statement);
                    session.createSQLQuery(statement).executeUpdate();
            } finally {
                    releaseSession(session);
            }
    }


    public void createDB(String dbName) throws SQLException {
            Session session = getSession();
            try {
                    String statement = CREATE_DB_QUERY + dbName;
                    logger.info(statement);
                    session.createSQLQuery(statement).executeUpdate();

            } finally {
                    releaseSession(session);
            }
    }

    public void addUserToDB(String dbName, String username, String accessRights) throws SQLException {
            Session session = getSession();
            try {
                    String statement = "GRANT " + accessRights +
                                            " ON " + dbName + ".*" +
                                            " TO '" + username + "'" + SQL_AT_HOST;
                    logger.info(statement);
                    session.createSQLQuery(statement).executeUpdate();

            } finally {
                    releaseSession(session);
            }
    }

    public void deleteUser(String username) throws SQLException {
            if (!userExist(username))
                    return;
            Session session = getSession();
            try {
                    String statement = DROP_USER + username;
                    logger.info(statement);
                    session.createSQLQuery(statement).executeUpdate();

            } finally {
                    releaseSession(session);
            }
    }

    //code is borowed from DBMergeDaoHibernate insertTransactions
    private void restoreDB(String dbName, String backupFile, String username, String password) throws Exception {
            Runtime runTime = Runtime.getRuntime();

            String[] commands = new String[]{MYSQL, dbName, userFlag + username, pwdFlag + password,
                    executeFlag, "\"" + sourceCommand + backupFile + "\""};
            String[] commandsToPrint = new String[]{MYSQL, dbName,
                    userFlag + username, pwdFlag + "*****",
                    executeFlag, "\"" + sourceCommand + backupFile + "\""};
            try {
                    logger.info("Beginning backup DB: " + dbName + ". Command Params: " + Arrays.toString(commandsToPrint));
                    Process proc = runTime.exec(commands);
                    StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
                    StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

                    errorGobbler.start();
                    outputGobbler.start();

                    try {
                            if (proc.waitFor() == 1) {
                                    logger.error("Return code indicates error in running MOOCdb restore mysql process.");
                                    throw new Exception("Return code indicates error in running MOOCdb restore mysql process.");
                            }
                    } catch (InterruptedException exception) {
                            logger.error("Interrupted Exception while running MOOCdb restore mysql process.", exception);
                            throw exception;
                    }
            } catch (IOException exception) {
                logger.error("Attempt to spawn a MOOCdb restore mysql process failed for file: " + backupFile, exception);
                throw exception;
            }
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
                    logger.error("IOException in capturing CourseraDbsrestore Output Stream", ioe);
             }
        }
    }
}
