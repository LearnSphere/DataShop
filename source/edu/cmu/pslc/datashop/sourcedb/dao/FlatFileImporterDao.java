/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.sourcedb.dao;

import java.util.ArrayList;

import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;

/**
 * Condition Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 15863 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-02-27 13:15:42 -0500 (Wed, 27 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface FlatFileImporterDao extends SourceDbAbstractDao {

    /**
     * Creates the database adb_source.
     * @return true if the database is created successfully, false otherwise
     */
    boolean createDatabase();

    /**
     * Drops the database adb_source.
     * @return true if the database is dropped successfully, false otherwise
     */
    boolean dropDatabase();

    /**
     * Drop and create the analysis database tables in the adb_source database
     * by launching the MySQL process.
     * @param createTablesFilename the name of the SQL file to create ADB tables
     * @return true if no errors were encountered otherwise return false
     */
    boolean createTables(String createTablesFilename);

    /**
     * Create table ffi_heading_column_map.
     * @return boolean true if the process is successful
     */
    boolean createHeadingColumnMap();

    /**
     * Insert into table ffi_heading_column_map.
     * @param standardName standard name of the heading
     * @param heading heading in the file
     * @param columnName column name
     * @param columnValue string value between the parenthesis
     * @param sequence number of sequence
     * @return boolean true if the process is successful, false otherwise
     */
    boolean insertIntoHeadingColumnMap(String standardName, String heading,
            String columnName, String columnValue, int sequence);

    /**
     * Create table ffi_import_file_data.
     * @param columns a list of the column names
     * @param fileInfoTableName the db table name to hold file info
     * @return boolean true if the process is successful, false otherwise
     */
    boolean createTableImportFileData(ArrayList<String> columns, String fileInfoTableName);

    /**
     * Load data into table ffi_import_file_data.
     * @param filename the name of the input file
     * @param columns a list of the column names
     * @param importFileId id of the import file
     * @param lineTerminator a string used as "lines terminated by" in LOAD DATA INFILE query
     * @param fileInfoTableName the db table name to hold file info
     * @param includeUserKCMs whether or not to include user-created KC Models
     * @param the default KC Column Id from the HeadingReport object
     * @return boolean true if the process is successful, false otherwise
     */
    int loadDataIntoImportFileData(String filename, ArrayList<String> columns,
                                   int importFileId, String lineTerminator,
                                   String fileInfoTableName, Boolean includeUserKCMs,
                                   Integer defaultColumnId);

    /**
     * Get row count of an import file given import_file_id.
     * @param importFileId the id of the input file
     * @param fileInfoTableName the db table name to hold file info
     * @return the count of all the rows of a file in the import_db.ffi_import_file_data
     */
    int getRowCount(int importFileId, String fileInfoTableName);

    /**
     * Verify the data in the import_file_data table.  Put warnings and error messages
     * in the import database.
     * Loop on each column: check each column for validity.
     * For each warning increment warning_count and append message to warning_message.
     * For each error increment error_count and append message to error_message.
     * If error count > 0, return false.
     * If error count > 100, return false and stop verifying the data.
     * @param threshold the number of threshold.
     * @param importDbName the name of the import_db database.
     * @param fileInfoTableName the name of the table created to temporarily hold file info
     * @return true if there are no errors, false otherwise
     */
    boolean verifyData(int threshold, String importDbName, String fileInfoTableName);

    /**
     * Create a new entry in the ds_dataset table for the given ImportStatusItem.
     * This keeps the populate database stored procedure from having to distinguish
     * between addressing import_db or import_db_test.
     * @param datasetId the id to use for the new dataset
     * @param importStatusItem the import status item
     * @return whether the dataset was created
     */
    boolean createDatasetItem(Integer datasetId, ImportStatusItem importStatusItem);

    /**
     * Create a new entry in the ds_dataset table for the given ImportStatusItem.
     * Note: this is the original version of the method, now only used when calling FFI
     * @param importStatusItem the import status item
     * @return dataset_id of the newly created Dataset Item
     */
    Integer createDatasetItem(ImportStatusItem importStatusItem);

    /**
     * Run the stored procedures which take the data from the import_file_data table
     * and populates the analysis database tables.
     * @param datasetId the dataset id
     * @param anonFlag true indicates to anonymize the student column
     * @return true if successful, false otherwise
     */
    boolean populateDatabase(Integer datasetId, boolean anonFlag);

}