/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */

package edu.cmu.pslc.learnsphere.analysis.moocdb.dao;

import java.sql.SQLException;

import java.util.List;

import edu.cmu.pslc.datashop.dao.AbstractDao;

import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;

/**
 *  Restore Coursera Spark backup SQL files.
 *
 * @author Hui Cheng
 * @version $Revision: 14073 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-05-15 14:48:12 -0400 (Mon, 15 May 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CourseraDbsRestoreDao extends AbstractDao {

        /**
         * Restore Coursera SQL backup files.
         * @param hashMappingFileName the name of the hash mapping db backup SQL file
         * @param forumFileName the name of the forum db backup SQL file
         * @param generalFileName the name of the general db backup SQL file
         * @throws SQLException exception with useful, detailed info
         */
        void restoreCourseraDBs(String hashMappingDBName, String hashMappingFileName, 
                        String forumDBName, String forumFileName, 
                        String generalDBName, String generalFileName)
                        throws SQLException, Exception;
        /**
         * Delete Coursera Dbs.
         * @param hashMappingDbName the name of the hash mapping db
         * @param forumDbName the name of the forum db
         * @param generalDbName the name of the general db
         * @throws SQLException exception with useful, detailed info
         */
        void deleteCourseraDBs(String hashMappingDBName, String forumDBName, String generalDBName)
                        throws SQLException, Exception;
        
}