/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.dao;

import java.util.HashSet;

/**
 * The Database Merge DAO interface.
 * @author dspencer
 *
 */
public interface DBMergeDao extends AbstractDao {

    /**
     * Begins the database merge by calling the stored procedure followed by the invocation of the
     * mysql process on the .sql file with our 'LOAD DATA INFILE' statements because it is an
     * illegal command to use in a stored procedure.  Calls to drop mapping tables and deleting
     * the outfiles concludes the method.
     * @param newDatasets joined string of the datasets being merged for the first time
     * @param existingDatasets joined string of the datasets to be added to existing data
     * @param keepDatasets joined string of the reserved datasets being merged for the first time
     * @param delimiter tells the stored procedure how to parse the joined datasets
     * @param action informs the stored procedure whether we should import new, merge only, or both
     * @param mergeType If that action involves merging what kind of merge data do we have
     * @param loadDataFilename the .sql file that handles inserts employing 'LOAD DATA INFILE'
     * @param deleteOutfilesFilename the .sql file that handles the deletes
     * @return true if successful
     * */
    boolean runDBMerge(String newDatasets, String existingDatasets, String keepDatasets,
                       String delimiter, int action, int mergeType,
                       String loadDataFilename, String deleteOutfilesFilename);


    /**
     * Called if the merge all datasets option is specified.  Retrieves all datasets in the source
     * database
     * @return A HashSet of all the dataset IDs in the source database
     */
    HashSet<Integer> getAllSourceDatasets();

    /**
     * Verifies the existence of the datasets passed to the command line in the source database.
     * @param datasets a hashset of potential database IDs
     * @param onSuccess string to return if all dataset IDs provided are valid
     * @return onSuccess string if all datasets exist otherwise a detail message about those that
     * don't.
     */
    String checkDatasets(HashSet<Integer> datasets, String onSuccess);

    /**
     * Verify the existence of datasets in the source database requiring import for the first time.
     * @param datasets a hashset of potential database IDs
     * @return a new instance of the intersection of dataset IDs in datasets that are eligible
     * for a new import.
     */
    HashSet<Integer> findImportNewDatasets(HashSet<Integer> datasets);

    /**
     * Verify the existence of datasets in the source database requiring import
     * as reserved datasets. Both the dataset name and id must be the same and
     * the reserved dataset must have no transactions.
     * @param datasets a hashset of potential database IDs
     * @return a new instance of the intersection of reserved dataset IDs that are eligible
     * for a new import.
     */
    HashSet<Integer> findImportKeepDatasets(HashSet<Integer> datasets);

    /**
     * Verify the existence of datasets in the source database requiring a merge because the
     * dataset name is present in the destination database.
     * @param datasets a hashset of potential database IDs
     * @param mergeType int indicating the kind of data expected in the source database
     * @return a new instance of the intersection of dataset IDs in datasets that are eligible
     * to be merged.
     */
    HashSet<Integer> findMergeDatasets(HashSet<Integer> datasets, int mergeType);

    /**
     * Set the source database for the purpose of verifying datasets and merging all datasets.
     * @param sourceDB the name of the source database
     */
    void setSourceDB(String sourceDB);

    /**
     * Set the name of the mapping database, used for student ID map.
     * @param mappingDb the name of the database
     */
    void setMappingDb(String mappingDb);

    /**
     * Checks to see if the datasets being merged where imported the first time using the db merge
     * tool.
     * @param datasets HashSet of dataset ids
     * @return boolean true if every dataset trying to merge has an entry in dbm_max_table_counts
     */
    boolean hasMaxTableCountEntry(HashSet<Integer> datasets);
}