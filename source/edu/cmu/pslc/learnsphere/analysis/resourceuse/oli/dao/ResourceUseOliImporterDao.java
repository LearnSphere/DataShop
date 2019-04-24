/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */

package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao;

import java.sql.SQLException;

import java.util.List;

import edu.cmu.pslc.datashop.dao.AbstractDao;

import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;

/**
 * ResourceUseImporter Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 12890 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:20 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResourceUseOliImporterDao extends AbstractDao {

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
        int loadTransactionData(String importFileName, int resourceUseOliTransactionFileId, String lineTerminator, String[] columns, String[] nullableDatetimeColumns)
                        throws SQLException;
        
        /**
         * Load data into table resource_use_oli_user_sess.
         * @param importFileName the name of the user_sess input file
         * @param resourceUseOliUserSessFileId id of the resource use OLI user_sess file
         * @param lineTerminator a string used as "lines terminated by" in LOAD DATA INFILE query
         * @param columns
         * @return the number of total rows imported for the given file
         * @throws SQLException exception with useful, detailed info
         */
        int loadUserSessData(String importFileName, int resourceUseOliUserSessFileId, String lineTerminator, String[] columns)
                        throws SQLException;

}