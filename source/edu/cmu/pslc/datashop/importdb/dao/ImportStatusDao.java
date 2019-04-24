/*
 * Carnegie Mellon University, Human Computer InterLogSession Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
/**
 * ImportStatus Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 9267 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-05-14 16:46:12 -0400 (Tue, 14 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ImportStatusDao extends ImportDbAbstractDao {

    /**
     * Standard get for a ImportStatusItem by id.
     * @param id The id of the ImportStatusItem.
     * @return the matching ImportStatusItem or null if none found
     */
    ImportStatusItem get(int id);

    /**
     * Standard find for an ImportStatusItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ImportStatusItem.
     * @return the matching ImportStatusItem.
     */
    ImportStatusItem find(int id);

    /**
     * Standard "find all" for ImportStatusItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Find all ImportStatusItems by their dataset name.
     * @param name datasetName
     * @return List of ImportStatusItems
     */
    List find(String name);

    /**
     * Checks if any dataset is in a state which indicates that it could currently
     * be in the process of being imported, i.e. any state except ERROR and
     * IMPORTED which are final states.
     * @return true if any dataset is loading, false otherwise
     */
    boolean isAnyDatasetLoading();

    /**
     * Set status to 'error', increase the error_count by one and append message to error_message.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     */
    void saveErrorMessage(ImportStatusItem importStatusItem, String message);

    /**
     * Check total length of message and error_message to see whether they exceeds max length.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     * @return true if total length exceeds max length, false otherwise
     */
    boolean checkErrorMessageLength(ImportStatusItem importStatusItem, String message);

    /**
     * Increase the warning_count by one and append message to warning_message.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     */
    void saveWarningMessage(ImportStatusItem importStatusItem, String message);

    /**
     * Check total length of message and warning_message to see whether they exceeds max length.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     * @return true if total length exceeds max length, false otherwise
     */
    boolean checkWarningMessageLength(ImportStatusItem importStatusItem, String message);

    /**
     * Get the total number of warnings from the given item and all of
     * the import file info rows associated with it.
     * @param importStatusItem the given import status item
     * @return the grand total of warnings
     */
    long getTotalWarningCount(ImportStatusItem importStatusItem);

    /**
     * Get the total number of errors from the given item and all of
     * the import file info rows associated with it.
     * @param importStatusItem the given import status item
     * @return the grand total of errors
     */
    long getTotalErrorCount(ImportStatusItem importStatusItem);

    /**
     * Update the line start and line end values in the import file info table.
     * @param importStatusItem the given import status item
     * @param fileInfoTableName the db table where the file info is temporarily stored
     * @return true if successful, false otherwise
     */
    boolean updateLineStartEndValues(ImportStatusItem importStatusItem, String fileInfoTableName);
}
