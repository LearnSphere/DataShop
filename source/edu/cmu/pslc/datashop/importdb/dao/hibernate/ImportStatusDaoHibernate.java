/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.dao.hibernate;

import java.sql.SQLException;
import java.util.List;

import edu.cmu.pslc.datashop.importdb.dao.ImportDbDaoFactory;
import edu.cmu.pslc.datashop.importdb.dao.ImportStatusDao;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
import edu.cmu.pslc.datashop.sourcedb.dao.SourceDbDaoFactory;

/**
 * Hibernate and Spring implementation of the ImportStatusDao.
 *
 * @author Shanwen Yu
 * @version $Revision: 9336 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-06-04 15:18:38 -0400 (Tue, 04 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportStatusDaoHibernate extends ImportDbHibernateAbstractDao
                                      implements ImportStatusDao {

    /**
     * Standard get for a ImportStatusItem by id.
     * @param id The id of the user.
     * @return the matching ImportStatusItem or null if none found
     */
    public ImportStatusItem get(int id) {
        return (ImportStatusItem)get(ImportStatusItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(ImportStatusItem.class);
    }

    /**
     * Standard find for an ImportStatusItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ImportStatusItem.
     * @return the matching ImportStatusItem.
     */
    public ImportStatusItem find(int id) {
        return (ImportStatusItem)find(ImportStatusItem.class, id);
    }

    /**
     * Find all ImportStatusItems by their dataset name.
     * @param name datasetName
     * @return List of ImportStatusItems
     */
    public List find(String name) {
        return getHibernateTemplate().find(
                "from ImportStatusItem status where status.datasetName = ?", name);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for isAnyDatasetLoading method. */
    private static final String ANY_DATASET_LOADING_HQL =
        "FROM ImportStatusItem WHERE status NOT IN ('error', 'imported', 'verified only')";

    /**
     * Checks if any dataset is in a state which indicates that it could currently
     * be in the process of being imported, i.e. any state except ERROR and
     * IMPORTED which are final states.
     * @return true if any dataset is loading, false otherwise
     */
    public boolean isAnyDatasetLoading() {
        List<ImportStatusItem> itemList = getHibernateTemplate().find(ANY_DATASET_LOADING_HQL);
        if (itemList != null && itemList.size() == 1) {
           return true;
        }

        return false;
    }

    /**
     * Set status to 'error', increase the error_count by one and append message to error_message.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     */
    public void saveErrorMessage(ImportStatusItem importStatusItem, String message) {
          ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
          importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());
          importStatusItem.setStatus(ImportStatusItem.STATUS_ERROR);

          int errorCount = importStatusItem.getErrorCount() + 1;
          importStatusItem.setErrorCount(errorCount);

          String completeMessage = importStatusItem.getErrorMessage();
          if (completeMessage == null) {
              completeMessage = message;
          } else {
              completeMessage += "\n" + message;
          }
          importStatusItem.setErrorMessage(completeMessage);
          saveOrUpdate(importStatusItem);
    }

    /** Max length of bytes that is allowed in a message field. */
    private static final int MAX_MSG_BYTES = 65535;
    /**
     * Check total length of message and error_message to see whether they exceeds max length.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     * @return true if total length exceeds max length, false otherwise
     */
    public boolean checkErrorMessageLength(ImportStatusItem importStatusItem, String message) {
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());

        boolean exceedMaxLengthFlag = false;
        int msgLength = 0;
        String existingMessage = importStatusItem.getErrorMessage();
        if (existingMessage != null) {
            msgLength = message.getBytes().length + existingMessage.getBytes().length;
        } else {
            msgLength = message.getBytes().length;
        }

        if (msgLength > MAX_MSG_BYTES) {
            exceedMaxLengthFlag = true;
        }
        return exceedMaxLengthFlag;
    }

    /**
     * Increase the warning_count by one and append message to warning_message.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     */
    public void saveWarningMessage(ImportStatusItem importStatusItem, String message) {
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());

        int warningCount = importStatusItem.getWarningCount() + 1;
        importStatusItem.setWarningCount(warningCount);
        String completeMessage = importStatusItem.getWarningMessage();
        if (completeMessage == null) {
            completeMessage = message;
        } else {
            completeMessage += "\n" + message;
        }
        importStatusItem.setWarningMessage(completeMessage);
        saveOrUpdate(importStatusItem);
    }

    /**
     * Check total length of message and warning_message to see whether they exceeds max length.
     * @param importStatusItem the import status item.
     * @param message the content of message.
     * @return true if total length exceeds max length, false otherwise
     */
    public boolean checkWarningMessageLength(ImportStatusItem importStatusItem, String message) {
        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        importStatusItem = importStatusDao.get((Integer)importStatusItem.getId());

        boolean exceedMaxLengthFlag = false;
        int msgLength = 0;
        String existingMessage = importStatusItem.getWarningMessage();
        if (existingMessage != null) {
            msgLength = message.getBytes().length + existingMessage.getBytes().length;
        } else {
            msgLength = message.getBytes().length;
        }

        if (msgLength > MAX_MSG_BYTES) {
            exceedMaxLengthFlag = true;
        }

        return exceedMaxLengthFlag;
    }

    /** HQL query for getTotalWarningCount method. */
    private static final String GET_TOTAL_WARNING_COUNT_HQL =
        "SELECT SUM(fileInfo.warningCount)"
        + " FROM ImportFileInfoItem fileInfo"
        + " WHERE fileInfo.importStatus = ?";

    /**
     * Get the total number of warnings from the given item and all of
     * the import file info rows associated with it.
     * @param importStatusItem the given import status item
     * @return the grand total of warnings
     */
    public long getTotalWarningCount(ImportStatusItem importStatusItem) {
        long statusWarningCount = importStatusItem.getWarningCount();
        long fileWarningCount = 0;

        List results = getHibernateTemplate().find(GET_TOTAL_WARNING_COUNT_HQL, importStatusItem);
        if (results.size() > 0) {
            fileWarningCount = (Long)results.get(0);
        }

        return statusWarningCount + fileWarningCount;
    }

    /** HQL query for getTotalErrorCount method. */
    private static final String GET_TOTAL_ERROR_COUNT_HQL =
        "SELECT SUM(fileInfo.errorCount)"
        + " FROM ImportFileInfoItem fileInfo"
        + " WHERE fileInfo.importStatus = ?";

    /**
     * Get the total number of errors from the given item and all of
     * the import file info rows associated with it.
     * @param importStatusItem the given import status item
     * @return the grand total of errors
     */
    public long getTotalErrorCount(ImportStatusItem importStatusItem) {
        long statusErrorCount = importStatusItem.getErrorCount();
        long fileErrorCount = 0;

        List results = getHibernateTemplate().find(GET_TOTAL_ERROR_COUNT_HQL, importStatusItem);
        if (results.size() > 0) {
            fileErrorCount = (Long)results.get(0);
        }

        return statusErrorCount + fileErrorCount;
    }

    /**
     * Update the line start and line end values in the import file info table.
     * @param importStatusItem the given import status item
     * @param fileInfoTableName the db table where the file info is temporarily stored
     * @return true if successful, false otherwise
     */
    public boolean updateLineStartEndValues(
            ImportStatusItem importStatusItem, String fileInfoTableName) {
        logDebug("updateLineStartEndValues");

        String query =
            "UPDATE import_file_info dst "
          + "JOIN (SELECT MIN(line_num) as min_line, "
          +              "MAX(line_num) as max_line, "
          +              "import_file_id "
          +      "FROM " + SourceDbDaoFactory.HIBERNATE.getSourceDatabaseName()
                         + "." + fileInfoTableName + " src "
          +      "GROUP BY import_file_id) src USING (import_file_id) "
          + "SET dst.line_start = src.min_line, "
          +     "dst.line_end = src.max_line;";

        try {
            logTrace("updateLineStartEndValues, query[", query, "]");
            executeSQLUpdate(query);
        } catch (SQLException exception) {
            logger.error("updateLineStartEndValues: Error executing "
                    + query, exception);
            return false;
        }

        return true;
    }
}
