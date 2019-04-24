/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.importdb.item.ImportFileInfoItem;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;

/**
 * ImportFileInfo Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 6703 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-03-11 14:33:42 -0500 (Fri, 11 Mar 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ImportFileInfoDao extends ImportDbAbstractDao {

    /**
     * Standard get for a ImportFileInfoItem by id.
     * @param id The id of the ImportFileInfoItem.
     * @return the matching ImportFileInfoItem or null if none found
     */
    ImportFileInfoItem get(int id);

    /**
     * Standard find for an ImportFileInfoItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ImportFileInfoItem.
     * @return the matching ImportFileInfoItem.
     */
    ImportFileInfoItem find(int id);

    /**
     * Standard "find all" for ImportFileInfoItem.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of Import File Info Item objects given a Import Status Item.
     * @param statusItem the given import status item
     * @return a list of ImportFileInfoItem objects
     */
    List<ImportFileInfoItem> findByStatusItem(ImportStatusItem statusItem);

    /**
     * Set status to 'error', increase the error_count by one and append message to error_message.
     * @param importFileInfoItem the import file info item.
     * @param message the content of message.
     */
    void saveErrorMessage(ImportFileInfoItem importFileInfoItem, String message);

    /**
     * Increase the warning_count by one and append message to warning_message.
     * @param importFileInfoItem the import file info item.
     * @param message the content of message.
     */
    void saveWarningMessage(ImportFileInfoItem importFileInfoItem, String message);

}
