/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.learnsphere.analysis.moocdb.item.MOOCdbItem;

/**
 * MOOCdb Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 14214 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-07-06 14:48:39 -0400 (Thu, 06 Jul 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MOOCdbDao extends edu.cmu.pslc.datashop.dao.AbstractDao {

    /**
     * Standard get for a MOOCdb item by id.
     * @param id the id of the desired MOOCdbItem
     * @return the matching MOOCdbItem or null if none found
     */
    MOOCdbItem get(Long id);

    /**
     * Standard find for a MOOCdbItem by id.
     * @param id id of the object to find
     * @return MOOCdbItem
     */
    MOOCdbItem find(Long id);

    /**
     * Standard "find all" for MOOCdbItem.
     * @return a List of objects
     */
    List<MOOCdbItem> findAll();

    //
    // Non-standard methods begin.
    //
    MOOCdbItem getMOOCdbByName(String MOOCdbName);
    Date getEarliestSubmissionTime(String MOOCdbName);
    void deleteMOOCdb(String MOOCdbName) throws Exception;
    boolean databaseExist(String dbName);
    boolean userExist(String username);
    void createDBUser(String username, String passwrod) throws SQLException;
    void createDB(String dbName) throws SQLException;
    void addUserToDB(String dbName, String username, String accessRights) throws SQLException;
    void deleteUser(String username) throws SQLException;
    //only call this after checking database exists
    boolean isMOOCdb(String dbName);
    void restoreMOOCdb(String DBName, String backupFileName, String username, String password)
                    throws SQLException, Exception;
}
