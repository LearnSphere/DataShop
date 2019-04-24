/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */

package edu.cmu.pslc.learnsphere.analysis.moocdb.dao.hibernate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate;

import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.CourseraDbsRestoreDao;
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.hibernate.HibernateDaoFactory;


/**
 * Hibernate and Spring implementation of the CourseraDbsRestoreDao.
 *
 * @author Hui Cheng
 * @version $Revision: 14073 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-05-15 14:48:12 -0400 (Mon, 15 May 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CourseraDbsRestoreDaoHibernate extends AbstractDaoHibernate
            implements CourseraDbsRestoreDao {
        /** The destination coursera database used to invoke MySQL from the java runtime */
        private static String mysqlDb;
        /** Username for destination coursera database from the application context */
        private static String mysqlUser;
        /** Password for the destination coursera database from the application context */
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
        
        private static String DROP_DB_QUERY = "DROP DATABASE IF EXISTS ";
        private static String CREATE_DB_QUERY = "CREATE DATABASE ";
        
        /**
         * Restore Coursera SQL backup files.
         * @param hashMappingFileName the name of the hash mapping db backup SQL file
         * @param forumFileName the name of the forum db backup SQL file
         * @param generalFileName the name of the general db backup SQL file
         * @throws SQLException exception with useful, detailed info
         */
        public void restoreCourseraDBs(String hashMappingDBName, String hashMappingFileName, 
                                        String forumDBName, String forumFileName, 
                                        String generalDBName, String generalFileName)
                        throws SQLException, Exception {
                Session session = getSession();
                try {
                        createDB(session, hashMappingDBName);
                        createDB(session, forumDBName);
                        createDB(session, generalDBName);
                        restoreDB(hashMappingDBName, hashMappingFileName);
                        restoreDB(forumDBName, forumFileName);
                        restoreDB(generalDBName, generalFileName);
                        
                } catch (Exception exception) {
                        dropAllDBs(session, hashMappingDBName, forumDBName, generalDBName);
                        throw exception;
                } finally {
                        releaseSession(session);
                }
        }
        
        /**
         * Delete Coursera Dbs.
         * @param hashMappingDbName the name of the hash mapping db
         * @param forumDbName the name of the forum db
         * @param generalDbName the name of the general db
         * @throws SQLException exception with useful, detailed info
         */
        public void deleteCourseraDBs(String hashMappingDBName, String forumDBName, String generalDBName)
                        throws SQLException, Exception {
                Session session = getSession();
                try {
                        String statement = DROP_DB_QUERY + hashMappingDBName;
                        logger.info(statement);
                        session.createSQLQuery(statement).executeUpdate();
                        statement = DROP_DB_QUERY + forumDBName;
                        logger.info(statement);
                        session.createSQLQuery(statement).executeUpdate();
                        statement = DROP_DB_QUERY + generalDBName;
                        logger.info(statement);
                        session.createSQLQuery(statement).executeUpdate();
                        
                } finally {
                        releaseSession(session);
                }
                
        }
        
        private void createDB(Session session, String dbName) throws SQLException {
                String statement = DROP_DB_QUERY + dbName;
                logger.info(statement);
                session.createSQLQuery(statement).executeUpdate();
                statement = CREATE_DB_QUERY + dbName;
                logger.info(statement);
                session.createSQLQuery(statement).executeUpdate();
        }
        
        private void dropAllDBs(Session session, String hashMappingDBName, 
                        String forumDBName, String generalDBName) 
                                throws SQLException {
                String statement = DROP_DB_QUERY + hashMappingDBName;
                logger.info(statement);
                session.createSQLQuery(statement).executeUpdate();
                statement = DROP_DB_QUERY + forumDBName;
                logger.info(statement);
                session.createSQLQuery(statement).executeUpdate();
                statement = DROP_DB_QUERY + generalDBName;
                logger.info(statement);
                session.createSQLQuery(statement).executeUpdate();
        }
        
        //code is borowed from DBMergeDaoHibernate insertTransactions
        private void restoreDB(String dbName, String backupFile) throws Exception {
                login = HibernateDaoFactory.DEFAULT.getAnalysisDatabaseLogin();
                mysqlDb = dbName;
                mysqlUser = login.get("user");
                mysqlPwd = login.get("password");

                Runtime runTime = Runtime.getRuntime();

                String[] commands = new String[]{MYSQL, mysqlDb, userFlag + mysqlUser, pwdFlag + mysqlPwd,
                        executeFlag, sourceCommand + backupFile};
                String[] commandsToPrint = new String[]{MYSQL, mysqlDb,
                        userFlag + mysqlUser, pwdFlag + "*****",
                        executeFlag, sourceCommand + backupFile};
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