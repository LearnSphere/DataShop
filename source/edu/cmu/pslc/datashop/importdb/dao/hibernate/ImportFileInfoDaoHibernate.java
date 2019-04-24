/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.importdb.dao.ImportFileInfoDao;
import edu.cmu.pslc.datashop.importdb.item.ImportFileInfoItem;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;

/**
 * Hibernate and Spring implementation of the LogActionDao.
 *
 * @author Shanwen Yu
 * @version $Revision: 6703 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-03-11 14:33:42 -0500 (Fri, 11 Mar 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportFileInfoDaoHibernate
        extends ImportDbHibernateAbstractDao
        implements ImportFileInfoDao {

    /**
     * Standard get for a ImportFileInfoItem by id.
     * @param id The id of the user.
     * @return the matching ImportFileInfoItem or null if none found
     */
    public ImportFileInfoItem get(int id) {
        return (ImportFileInfoItem)get(ImportFileInfoItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(ImportFileInfoItem.class);
    }

    /**
     * Standard find for an ImportFileInfoItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ImportFileInfoItem.
     * @return the matching ImportFileInfoItem.
     */
    public ImportFileInfoItem find(int id) {
        return (ImportFileInfoItem)find(ImportFileInfoItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of Import File Info Item objects given a Import Status Item.
     * @param statusItem the given import status item
     * @return a list of ImportFileInfoItem objects
     */
    public List<ImportFileInfoItem> findByStatusItem(ImportStatusItem statusItem) {
        return getHibernateTemplate().find(
                "FROM ImportFileInfoItem WHERE importStatus.id = ?",
                statusItem.getId());
    }

    /**
     * Set status to 'error', increase the error_count by one and append message to error_message.
     * @param importFileInfoItem the import file info item.
     * @param message the content of message.
     */
    public void saveErrorMessage(ImportFileInfoItem importFileInfoItem, String message) {
        importFileInfoItem.setStatus(ImportStatusItem.STATUS_ERROR);
        int errorCount = importFileInfoItem.getErrorCount() + 1;
        importFileInfoItem.setErrorCount(errorCount);
        String completeMessage = importFileInfoItem.getErrorMessage();
        if (completeMessage == null) {
            completeMessage = message;
        } else {
            completeMessage += ", " + message;
        }
        importFileInfoItem.setErrorMessage(completeMessage);
        saveOrUpdate(importFileInfoItem);
    }

    /**
     * Increase the warning_count by one and append message to warning_message.
     * @param importFileInfoItem the import file info item.
     * @param message the content of message.
     */
    public void saveWarningMessage(ImportFileInfoItem importFileInfoItem, String message) {
        int warningCount = importFileInfoItem.getWarningCount() + 1;
        importFileInfoItem.setWarningCount(warningCount);
        String completeMessage = importFileInfoItem.getWarningMessage();
        if (completeMessage == null) {
            completeMessage = message;
        } else {
            completeMessage += ", " + message;
        }
        importFileInfoItem.setWarningMessage(completeMessage);
        saveOrUpdate(importFileInfoItem);
    }

}
