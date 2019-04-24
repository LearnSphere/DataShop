/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */

package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.hibernate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.datalab.item.AnalysisItem;
import edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate;

import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliImporterDao;

/**
 * Hibernate and Spring implementation of the ResourceUseImporterDao.
 *
 * @author Hui Cheng
 * @version $Revision: 13978 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-03-07 15:54:12 -0500 (Tue, 07 Mar 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResourceUseOliImporterDaoHibernate extends AbstractDaoHibernate
            implements ResourceUseOliImporterDao {
        private static String RESOURCE_USE_TRANSACTION_TABLE_NAME = "resource_use_oli_transaction";
        private static String RESOURCE_USE_USER_SESS_TABLE_NAME = "resource_use_oli_user_sess";

        /**
         * Load data into table resource_use_oli_transaction.
         * @param importFileName the name of the transaction input file
         * @param resourceUseOliTransactionFileId id of the resource use OLI transaction file
         * @param lineTerminator a string used as "lines terminated by" in LOAD DATA INFILE query
         * @param columns
         * @param nullableDatetimeColumns
         * @return the number of total rows imported for the given file
         * @throws SQLException exception with useful, detailed info
         */
        public int loadTransactionData(String importFileName, int resourceUseOliTransactionFileId, String lineTerminator, String[] columns, String[] nullableDatetimeColumns)
                        throws SQLException {
        int rowCount = 0;
        String query = "LOAD DATA INFILE \'" + importFileName.replaceAll("'", "\\\\'") + "\'"
            + " INTO TABLE "
            + DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_TRANSACTION_TABLE_NAME
            + " FIELDS TERMINATED BY \'\t\'"
            + " ESCAPED BY \'\\\\\'"
            + " LINES TERMINATED BY \'" + lineTerminator + "\'"
            + " IGNORE 1 LINES "
            + " ( ";
        for (int i = 0; i < columns.length; i++) {
           if (i == columns.length - 1) {
                   if (isNullableDateTimeColumn(nullableDatetimeColumns, columns[i]))
                           query += "@" + columns[i];
                   else
                           query += columns[i];
           } else {
                   if (isNullableDateTimeColumn(nullableDatetimeColumns, columns[i]))
                           query += "@" + columns[i] + ", ";
                   else
                           query += columns[i] + ", ";
           }
        }
        query += "  ) ";
        query += " SET resource_use_oli_transaction_file_id = " + resourceUseOliTransactionFileId + ", ";

        // it is much faster to trim the value here than do it column by column in stored procedure
        for (int i = 0; i < columns.length; i++) {
            if (i == columns.length - 1) {
                    if (isNullableDateTimeColumn(nullableDatetimeColumns, columns[i]))
                            query += columns[i] + " = NULLIF(TRIM(@" + columns[i] + "), 'NULL') ";
                    else
                            query += columns[i] + " = TRIM(" + columns[i] + ") ";
                    
                
            } else {
                    if (isNullableDateTimeColumn(nullableDatetimeColumns, columns[i]))
                            query += columns[i] + " = NULLIF(TRIM(@" + columns[i] + "), 'NULL'), ";
                    else
                            query += columns[i] + " = TRIM(" + columns[i] + "), ";
                
            }
        }
        Throwable cause = null;
        try {
            logDebug("load transaction data, query[", query, "]");
            executeSQLUpdate(query);
            rowCount = getTransactionRowCount(resourceUseOliTransactionFileId);
        } catch (HibernateException exception) {
            logger.error("Error executing " + query, exception);
            cause = exception.getCause();
        } catch (SQLException exception) {
            logger.error("Error executing " + query, exception);
            cause = exception;
        }

        // Don't lose useful, detailed info in SQLException.
        if ((cause != null) && (cause instanceof SQLException)) {
            throw (SQLException)cause;
        }

        return rowCount;
    }
    
    private boolean isNullableDateTimeColumn (String[] nullableDatetimeColumns, String columnName) {
            for (String thisCol : nullableDatetimeColumns) {
                    if (thisCol.equals(columnName))
                            return true;
            }
            return false;
    }
    
    /**
     * Load data into table resource_use_oli_user_sess.
     * @param importFileName the name of the user_sess input file
     * @param resourceUseOliUserSessFileId id of the resource use OLI user_sess file
     * @param lineTerminator a string used as "lines terminated by" in LOAD DATA INFILE query
     * @param columns
     * @return the number of total rows imported for the given file
     * @throws SQLException exception with useful, detailed info
     */
    public int loadUserSessData(String importFileName, int resourceUseOliUserSessFileId, String lineTerminator, String[] columns)
                    throws SQLException {

    int rowCount = 0;
    String query = "LOAD DATA INFILE \'" + importFileName.replaceAll("'", "\\\\'") + "\'"
        + " IGNORE INTO TABLE "
        + DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_USER_SESS_TABLE_NAME
        + " FIELDS TERMINATED BY \'\t\'"
        + " ESCAPED BY \'\\\\\'"
        + " LINES TERMINATED BY \'" + lineTerminator + "\'"
        + " IGNORE 1 LINES "
        + " ( ";
    for (int i = 0; i < columns.length; i++) {
       if (i == columns.length - 1) {
           query += columns[i];
       } else {
           query += columns[i] + ", ";
       }
    }
    query += "  ) ";
    query += " SET resource_use_oli_user_sess_file_id = " + resourceUseOliUserSessFileId + ", ";

    // it is much faster to trim the value here than do it column by column in stored procedure
    for (int i = 0; i < columns.length; i++) {
        if (i == columns.length - 1) {
            query += columns[i] + " = TRIM(" + columns[i] + ") ";
        } else {
            query += columns[i] + " = TRIM(" + columns[i] + "), ";
        }
    }
    
    Throwable cause = null;
    try {
        logDebug("load user_sess data, query[", query, "]");
        executeSQLUpdate(query);
        rowCount = getUserSessRowCount(resourceUseOliUserSessFileId);
    } catch (HibernateException exception) {
        logger.error("Error executing " + query, exception);
        cause = exception.getCause();
    } catch (SQLException exception) {
        logger.error("Error executing " + query, exception);
        cause = exception;
    }

    // Don't lose useful, detailed info in SQLException.
    if ((cause != null) && (cause instanceof SQLException)) {
        throw (SQLException)cause;
    }

    return rowCount;
}

    /**
     * Get row count of transaction.
     * @param resourceUseOliTransactionFileId 
     * @return the count of all the rows of transaction for this transaction file
     */
    private int getTransactionRowCount(long resourceUseOliTransactionFileId) {
        Session session = getSession();

        int rowCount = 0;
        String query = "SELECT COUNT(*) FROM " + DaoFactory.HIBERNATE.getAnalysisDatabaseName()
                                + "." + RESOURCE_USE_TRANSACTION_TABLE_NAME + " "
                                + "where resource_use_oli_transaction_file_id = " + resourceUseOliTransactionFileId;
        logDebug("getRowCount, query[", query, "]");

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

    /**
     * Get row count of an import file.
     * @param 
     * @return the count of all the rows of a file in the resource_use_oli_user_sess
     */
    private int getUserSessRowCount(long resourceUseOliUserSessFileId) {
        Session session = getSession();

        int rowCount = 0;
        String query = "SELECT COUNT(*) FROM " + DaoFactory.HIBERNATE.getAnalysisDatabaseName()
                                + "." + RESOURCE_USE_USER_SESS_TABLE_NAME + " "
                                + "where resource_use_oli_user_sess_file_id = " + resourceUseOliUserSessFileId;
        logDebug("getRowCount, query[", query, "]");

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

}