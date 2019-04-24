/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;

/**
 * Transaction Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15863 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-02-27 13:15:42 -0500 (Wed, 27 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface TransactionDao extends AbstractDao {

    /**
     * Standard get for a TransactionItem by id.
     * @param id The id of the TransactionItem.
     * @return the matching TransactionItem or null if none found
     */
    TransactionItem get(Long id);

    /**
     * Standard find for an TransactionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired TransactionItem.
     * @return the matching TransactionItem.
     */
    TransactionItem find(Long id);

    /**
     * Standard "find all" for TransactionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Find all the transactions for the given student and subgoal.
     * @param studentItem the student item
     * @param subgoalItem the subgoal item
     * @return a list of transaction items
     */
    List find(StudentItem studentItem, SubgoalItem subgoalItem);

    /**
     * Get a list of all transactions in a dataset.
     * @param dataset the dataset to get the transactions in.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return List of all transactions in the dataset between the offset and the limit.
     */
    List find(DatasetItem dataset, Integer limit, Integer offset);

    /**
     * Get a list of all transactions in a dataset that are missing a GUID.
     * @param dataset the dataset to get the transactions in.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return List of all transactions in the dataset between the offset and the limit.
     */
    List findNullGUID(DatasetItem dataset, Integer limit, Integer offset);

    /**
     * Return the max subgoal attempt for the given student and subgoal,
     * regardless of the session id.
     * Note that this is for the TutorMessageConverter.
     * @param studentItem the student item
     * @param subgoalItem the subgoal item
     * @return the max subgoal attempt number if an attempt has been made, null otherwise
     */
    Integer getNextAttemptAtSubgoal(StudentItem studentItem, SubgoalItem subgoalItem);

    /**
     * Call the stored procedure to fix the attempt at subgoal for all the transactions
     * for a given dataset.
     * @param datasetItem the dataset to be updated
     * @return the number of rows updated
     */
     int callFixAttemptAtSubgoalSP(DatasetItem datasetItem);

    /**
     * Return the SSSS 2D array that contains studentID, success, step, and skill.
     * Note that this is for the Learning Factors Analysis tool.
     * @param datasetItem the selected dataset
     * @param skillModelItem the selected skill model
     * @return String[][]
     */
    String[][] getSSSS(DatasetItem datasetItem, SkillModelItem skillModelItem);

    /**
     * Gets the number of transactions in a dataset.
     * @param dataset the dataset to # transactions in.
     * @return Number of transactions in the dataset as an Long.
     */
    Long count(DatasetItem dataset);

    /**
     * Gets the skill item identifiers for the given transaction item.
     * @param transactionItem the transaction item to get skill identifiers for.
     * @return A list of skill identifiers.
     */
    List getSkillsForTx(TransactionItem transactionItem);

    /**
     * Get a listing of custom field identifiers for a given TransactionItem.
     * @param transactionItem the transaction to get custom field identifiers for.
     * @return A list of custom field identifiers.
     */
    List getCustomFieldsForTx(TransactionItem transactionItem);

    /**
     * Get a listing of condition item identifiers for a given TransactionItem.
     * @param transactionItem the transaction to get condition identifiers for.
     * @return A list of condition item identifiers.
     */
    List getConditionsForTx(TransactionItem transactionItem);

    /**
     * Gets a list of transactions in the dataset who's tutor type match all
     * or a portion of the toMatch parameter.
     * @param typeField the specific type field to match.
     * @param toMatch A string to match.
     * @param dataset the dataset item to search in.
     * @param matchAny boolean value indicating whether to only look for matches from
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching items sorted by Name.
     */
    List findMatchingTypes(
            String typeField, String toMatch, DatasetItem dataset, boolean matchAny);

    /**
     * Deletes datasets marked as deleted using the purge_deleted_datasets SP.
     * @param datasetItem the dataset item
     * @return the number of deleted transactions for this datasetitem
     */
    Integer callPurgeDeletedDatasetsSP(DatasetItem datasetItem);

    /**
     * This method calls the calculate_tx_duration stored
     * procedure, operating on transaction contained within the given dataset.
     * @param dataset the dataset we wish to process.
     * @return true if SP completed without error, false otherwise.
     */
    boolean callCalculateTxDurationSP(DatasetItem dataset);

    /**
     * This method calls the calculate_tx_duration stored
     * procedure, operating on transaction contained within the given dataset.
     * @param dataset the dataset we wish to process.
     * @param batchSize the batch size to use during processing.
     * @return true if SP completed without error, false otherwise.
     */
    boolean callCalculateTxDurationSP(DatasetItem dataset, Integer batchSize);

    /**
     * Call the calculate_tx_duration stored procedure to fill in transaction_duration
     * values in the tutor_transaction table.  The stored procedure will take the provided
     * dataset item, find all transactions for it, and calculate durations for each transaction.
     * @return true if successful, false otherwise.
     */
    boolean callCalculateTotalStudMillisecondsSP();

    /**
     * Delete the transactions for a dataset.
     * @param datasetItem the dataset item
     * @return the number of transactions deleted
     */
    int deleteByDataset(DatasetItem datasetItem);

    /**
     * Generates a unique identifier for a transaction based on current time and a random number.
     * @return String that is the identifier.
     */
    String generateGUID();

    /**
     * Find TransactionItem given GUID.
     * @param guid the transaction GUID
     * @return the transaction item
     */
    TransactionItem findByGUID(String guid);

    /**
     * Get the number of problems for the specified sample.
     * @param sampleId the sample id
     * @return Long problem count
     */
    Long getNumProblems(Integer sampleId);

} // end TransactionDao.java
