/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.io.IOException;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DiscourseImportQueueMapDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;
import edu.cmu.pslc.datashop.dto.importqueue.VerificationResults;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.importdb.dao.ImportDbDaoFactory;
import edu.cmu.pslc.datashop.importdb.dao.ImportFileInfoDao;
import edu.cmu.pslc.datashop.importdb.dao.ImportStatusDao;
import edu.cmu.pslc.datashop.importdb.item.ImportFileInfoItem;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper to get the Import Queue DTO list for each possible location
 * on the UI.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public ImportQueueHelper() { }

    /** First 100 lines passed success message. */
    public static final String FIRST_100_PASSED_MSG =
            "First 100 lines passed initial verification.";

    /** Success message string constant. */
    private static final String SUCCESS_MINI_VERIFY = "Success! " + FIRST_100_PASSED_MSG;
    /** String constant. */
    private static final String MSG_CANCEL_ERROR =
        "Error occurred while trying to cancel import of dataset.";
    /** String constant. */
    private static final String MSG_CANCEL_SUCCESS =
        "Import of dataset canceled.";
    /** String constant. */
    private static final String MSG_UNDO_CANCEL_ERROR =
        "Error occurred while trying to undo this action.";
    /** String constant. */
    private static final String MSG_UNDO_CANCEL_SUCCESS =
        "Dataset is now back in the import queue.";
    /** String constant. */
    private static final String MSG_CANCEL_NO_OP = "Status is unchanged.";
    /** String constant. */
    private static final String MSG_IMPORT_ANYWAY_ERROR = "Error occurred while updating dataset.";
    /** String constant. */
    private static final String MSG_IMPORT_ANYWAY_SUCCESS = "Successfully updated dataset.";
    /** String constant. */
    private static final String MSG_UNDO_IMPORT_ERROR = MSG_UNDO_CANCEL_ERROR;
    /** String constant. */
    private static final String MSG_UNDO_IMPORT_SUCCESS = "Successfully updated dataset.";
    /** String constant. */
    private static final String MSG_HIDE_ROW_ERROR = "Error occurred while deleting item.";
    /** String constant. */
    private static final String MSG_HIDE_ROW_SUCCESS = "Successfully deleted item.";
    /** String constant. */
    private static final String MSG_CANCEL_DATASET_ERROR_WHILE_IMPORTING =
        "Cannot cancel while import is in progress.";

    /**
     * Build a VerificationResults object from the Import Database's items.
     * @param importStatusId the id of the ImportStatusItem
     * @return a new instance of VerificationResults
     */
    public VerificationResults getVerificationResults(Integer importStatusId) {
        logger.debug("getVerificationResults(" + importStatusId + ")");
        VerificationResults results;

        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        ImportStatusItem importStatusItem = importStatusDao.get(importStatusId);

        ImportFileInfoDao importFileInfoDao = ImportDbDaoFactory.DEFAULT.getImportFileInfoDao();
        List<ImportFileInfoItem> fileInfoItemList
            = importFileInfoDao.findByStatusItem(importStatusItem);

        String status = (importStatusItem.getStatus().equals(
                ImportStatusItem.STATUS_VERIFIED_ONLY))
                ? ImportQueueItem.STATUS_PASSED : ImportQueueItem.STATUS_ERRORS;

        Long totalErrorCount = importStatusDao.getTotalErrorCount(importStatusItem);
        Long totalWarningCount = importStatusDao.getTotalWarningCount(importStatusItem);

        if ((totalWarningCount > 0) && (totalErrorCount == 0)) {
            status = ImportQueueItem.STATUS_ISSUES;
        }

        if (status.equals(ImportQueueItem.STATUS_PASSED)) {
            results = new VerificationResults(status);
            results.setSuccessMessage(SUCCESS_MINI_VERIFY);

        } else if (status.equals(ImportQueueItem.STATUS_ISSUES)) {
            results = new VerificationResults(status);

            if (importStatusItem.getWarningMessage() != null) {
                VerificationResults.Messages genMsgs = results.new Messages();
                genMsgs.addIssue(importStatusItem.getWarningMessage().trim());
                results.setGeneralMessages(genMsgs);
            }

            for (ImportFileInfoItem ifi : fileInfoItemList) {
                if (ifi.getWarningCount() > 0) {
                    VerificationResults.Messages fileMsgs = results.new Messages();
                    String fileName = cleanupFileName(ifi.getFileName());
                    fileMsgs.setFileName(fileName);
                    fileMsgs.addIssue(ifi.getWarningMessage().trim());
                    results.addFileMessages(fileMsgs);
                }
            }

            results.setTotalIssues(totalWarningCount.intValue());

        } else {
            results = new VerificationResults(status);

            VerificationResults.Messages genMsgs = results.new Messages();
            if (importStatusItem.getErrorMessage() != null) {
                genMsgs.addError(importStatusItem.getErrorMessage().trim());
            }

            for (ImportFileInfoItem ifi : fileInfoItemList) {
                if (ifi.getErrorCount() > 0) {
                    VerificationResults.Messages fileMsgs = results.new Messages();
                    String fileName = cleanupFileName(ifi.getFileName());
                    fileMsgs.setFileName(fileName);
                    fileMsgs.addError(ifi.getErrorMessage().trim());
                    results.addFileMessages(fileMsgs);
                }
            }

            if (importStatusItem.getWarningMessage() != null) {
                genMsgs.addIssue(importStatusItem.getWarningMessage().trim());
            }

            results.setGeneralMessages(genMsgs);

            for (ImportFileInfoItem ifi : fileInfoItemList) {
                if (ifi.getWarningCount() > 0) {
                    VerificationResults.Messages fileMsgs = results.new Messages();
                    String fileName = cleanupFileName(ifi.getFileName());
                    fileMsgs.setFileName(fileName);
                    fileMsgs.addIssue(ifi.getWarningMessage().trim());
                    results.addFileMessages(fileMsgs);
                }
            }

            results.setTotalErrors(totalErrorCount.intValue());
            results.setTotalIssues(totalWarningCount.intValue());
        }
        return results;
    }

    /** Copied from upload dataset servlet, but this should be in just one place. */
    public static final String HEAD_FILE_PREFIX = "head_";

    /**
     * Utility to strip path and leading "head_" from file name.
     * @param fileName the full file name
     * @return the stripped-down version of the file name
     */
    public static String cleanupFileName(String fileName) {
        String fileNameNoPath = fileName;
        int lastIndex = fileName.lastIndexOf("/");
        if (lastIndex == -1) { lastIndex = fileName.lastIndexOf("\\"); }
        if (lastIndex > 0) {
            fileNameNoPath = fileName.substring(lastIndex + 1);
        }

        if (fileNameNoPath.startsWith(HEAD_FILE_PREFIX)) {
            return fileNameNoPath.substring(HEAD_FILE_PREFIX.length());
        } else {
            return fileNameNoPath;
        }
    }

    /**
     * Gets the 'TO' address for the import queue item.
     * @param iqItem the import queue item
     * @param defaultAddress the default if user email not configured
     * @return the 'TO' address for the import queue item
     */

    public static String getToAddress(ImportQueueItem iqItem, String defaultAddress) {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem uploadedBy = userDao.get((String)iqItem.getUploadedBy().getId());
        String toAddress = uploadedBy.getEmail();

        // If email address not defined, sent notice to DataShop help.
        if ((toAddress == null) || (toAddress.length() == 0)) {
            toAddress = defaultAddress;
        }

        return toAddress;
    }

    /**
     * Used by the project/home page.
     * @param req {@link HttpServletRequest}
     * @param username the account/user id of the uploader
     * @return a list of items the given user uploaded
     */
    public List<ImportQueueDto> getImportQueueByUploader(ImportQueueContext ctx, String username) {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        List<ImportQueueDto> list = iqDao.getImportQueueByUploader(username);
        list = setShowUndoFlag(ctx, list);
        return list;
    }

    /**
     * Figure out which items have a status change that can be 'undone',
     * given the context and a list.
     * @param context ImportQueueContext
     * @param list a list of ImportQueueDto objects to be updated
     * @return the modified list
     */
    public List<ImportQueueDto> setShowUndoFlag(ImportQueueContext context,
                                                List<ImportQueueDto> list)
    {
        for (ImportQueueDto dto : list) {
            IqStatus iqStatus = context.getIqStatus(dto.getImportQueueId());
            Boolean displayFlag = dto.getDisplayFlag();
            if (iqStatus != null && (displayFlag == null || displayFlag)) {
                dto.setShowUndoFlag(true);
            }
        }
        return list;
    }

    /**
     * Handle cancel actions for the My Datasets page and the Project Page where these
     * actions are also available.  None of these actions are AJAX requests, all require
     * a page reload. The page reload is necessary as these actions affect other rows, in
     * there queue order and their estimated import date.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void handleCancelActions(HttpServletRequest req, HttpServletResponse resp,
                                    UserItem userItem)
        throws ServletException, IOException
    {
        String actionParam = req.getParameter(ImportQueueServlet.AJAX_REQUEST_METHOD);
        if (actionParam != null) {
            if (actionParam.equals("cancelImport")) { //Not Ajax
                cancelImport(req, resp, userItem);
            } else if (actionParam.equals("undoCancel")) { //Not Ajax
                undoCancel(req, resp, userItem);
            } else if (actionParam.equals("hideRow")) { //Not Ajax
                hideRow(req, resp, userItem);
            }
        }
    }

    /**
     * Handle the canceling of an import item.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void cancelImport(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws ServletException, IOException
    {
        ImportQueueItem iqItem = getImportQueueItemRefresh(req, MSG_CANCEL_ERROR);

        if (iqItem == null) { return; }

        if (!userHasAccess(req, userItem, iqItem)) { return; }

        if (iqItem.getStatus().equals(ImportQueueItem.STATUS_LOADING)
                || iqItem.getStatus().equals(ImportQueueItem.STATUS_GENING)
                || iqItem.getStatus().equals(ImportQueueItem.STATUS_AGGING)) {
            displayTransientMessage(req, iqItem, MSG_CANCEL_DATASET_ERROR_WHILE_IMPORTING,
                                    ImportQueueServlet.MSG_LEVEL_ERROR);
            return;
        }

        IqStatus prevIqStatus = new IqStatus(iqItem);
        String prevStatus = prevIqStatus.getStatus();
        String newStatus = ImportQueueItem.STATUS_CANCELED;

        if (prevStatus != null && !prevStatus.equals(newStatus)) {
            //status changed
            ImportQueueContext context = ImportQueueContext.getContext(req);
            context.setIqStatus((Integer)iqItem.getId(), prevIqStatus);
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            iqItem.setStatus(newStatus);
            iqItem.setQueueOrder(null);
            iqItem.setLastUpdatedTime(new Date());
            iqDao.saveOrUpdate(iqItem);
            // Fix queue order after saving item
            fixQueueOrder();
            // Transient message
            displayTransientMessage(req, iqItem, MSG_CANCEL_SUCCESS,
                                    ImportQueueServlet.MSG_LEVEL_SUCCESS);
            // Dataset User Log
            logUpdateStatusInQueue(userItem, prevStatus, iqItem);
        } else {
            //nothing changed
            displayTransientMessage(req, iqItem, MSG_CANCEL_NO_OP,
                                    ImportQueueServlet.MSG_LEVEL_SUCCESS);
        }
    }

    /**
     * Handle the UNDO of the cancel import.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void undoCancel(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws ServletException, IOException
    {
        ImportQueueItem iqItem =
            getImportQueueItemRefresh(req, MSG_UNDO_CANCEL_ERROR);
        if (iqItem == null) { return; }
        if (!userHasAccess(req, userItem, iqItem)) { return; }

        Integer importQueueId = (Integer)iqItem.getId();
        ImportQueueContext context = ImportQueueContext.getContext(req);
        IqStatus prevIqStatus = context.getIqStatus(importQueueId);
        if (prevIqStatus == null) {
            //This shouldn't happen, but you never know.
            logger.error("undoCancel: previous IQ status not found: " + importQueueId);
            displayTransientMessage(req, iqItem, MSG_UNDO_CANCEL_ERROR,
                                    ImportQueueServlet.MSG_LEVEL_ERROR);
        } else {
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            iqItem.setStatus(prevIqStatus.getStatus());
            iqItem.setQueueOrder(prevIqStatus.getQueueOrder());
            iqItem.setLastUpdatedTime(new Date());
            iqItem.setDisplayFlag(true);
            iqDao.saveOrUpdate(iqItem);
            // Fix queue order after saving item
            fixQueueOrder();
            // Transient message
            displayTransientMessage(req, iqItem, MSG_UNDO_CANCEL_SUCCESS,
                                    ImportQueueServlet.MSG_LEVEL_SUCCESS);
            // Dataset User Log
            logUpdateStatusInQueue(userItem, prevIqStatus.getStatus(), iqItem);
        }
    }

    /**
     * Handle the Delete/Hide action.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void hideRow(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws ServletException, IOException
    {
        ImportQueueItem iqItem = getImportQueueItemRefresh(req, MSG_HIDE_ROW_ERROR);
        if (iqItem == null) { return; }
        if (!userHasAccess(req, userItem, iqItem)) { return; }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        iqItem.setDisplayFlag(false);
        iqItem.setLastUpdatedTime(new Date());
        iqDao.saveOrUpdate(iqItem);
        // Transient message
        displayTransientMessage(req, iqItem, MSG_HIDE_ROW_SUCCESS,
                                ImportQueueServlet.MSG_LEVEL_SUCCESS);
        // Dataset User Log
        logUserAction(userItem, iqItem, UserLogger.REMOVE_FROM_QUEUE, null);
    }

    /**
     * Calls the getImportQueueItem method for a method that refreshes the page.
     * @param req {@link HttpServletRequest}
     * @param errorMessage action specific message in case the id is null or not found
     * @return an ImportQueueItem if found, null otherwise
     */
    public ImportQueueItem getImportQueueItemRefresh(HttpServletRequest req,
                                                     String errorMessage) {
        return getImportQueueItem(req, true, errorMessage);
    }

    /**
     * Calls the getImportQueueItem method for a method that returns JSON.
     * @param req {@link HttpServletRequest}
     * @return an ImportQueueItem if found, null otherwise
     */
    public ImportQueueItem getImportQueueItemJson(HttpServletRequest req) {
        return getImportQueueItem(req, false, null);
    }

    /**
     * Returns an ImportQueueItem if one can be found, which it should be in all
     * cases as the id should come from the existing items in the database and is
     * not user entered, but checks are in place just in case.
     * Only write an error message to the session if its a Page Refresh and not a JSON response.
     * @param req {@link HttpServletRequest}
     * @param refreshPageFlag if true, write transient error message to session, do not otherwise
     * @param errorMessage action specific message in case the id is null or not found
     * @return an ImportQueueItem if found, null otherwise
     */
    private ImportQueueItem getImportQueueItem(HttpServletRequest req,
                                               boolean refreshPageFlag,
                                               String errorMessage)
    {
        String importQueueIdString = req.getParameter("importQueueId");
        if (importQueueIdString == null) {
            logger.error("importQueueId should not be null");
            if (refreshPageFlag) {
                displayTransientMessage(req, errorMessage, ImportQueueServlet.MSG_LEVEL_ERROR);
            }
            return null;
        }

        ImportQueueItem item = null;
        Integer itemId = parseInteger("importQueueId", importQueueIdString);
        if (itemId != null) {
            ImportQueueDao dao = DaoFactory.DEFAULT.getImportQueueDao();
            item = dao.get(itemId);
        }

        if (item == null) {
            logger.error("importQueueItem is not found: " + importQueueIdString);
            if (refreshPageFlag) {
                displayTransientMessage(req, errorMessage, ImportQueueServlet.MSG_LEVEL_ERROR);
            }
            return null;
        }
        return item;
    }

    /**
     * Write to the dataset user log table what has changed.
     * @param userItem the current user
     * @param prevStatus the previous status
     * @param iqItem the updated import queue item
     */
    public void logUpdateStatusInQueue(UserItem userItem,
                                       String prevStatus, ImportQueueItem iqItem) {
        DatasetItem datasetItem = null;
        String newStatus = iqItem.getStatus();
        String info = getUserInfoString(iqItem);
        if (prevStatus != null && !prevStatus.equals(iqItem.getStatus())) {
            info += " Changed status from '" + prevStatus
                  + "' to '" + iqItem.getStatus() + "'.";
        } else {
            info += " Status '" + iqItem.getStatus() + "'.";
        }

        if (newStatus.equals(ImportQueueItem.STATUS_QUEUED)) {
            info += " Days To Load: " + iqItem.getEstImportDate() + ".";
        } else if (newStatus.equals(ImportQueueItem.STATUS_PASSED)) {
            info += " Days To Load: " + iqItem.getEstImportDate() + ".";
        } else if (newStatus.equals(ImportQueueItem.STATUS_ERRORS)) {
            info += " Errors: " + iqItem.getNumErrors() + ".";
            info += " Issues: " + iqItem.getNumIssues() + ".";
            info += getResultsForUserLogging(iqItem.getVerificationResults());
        } else if (newStatus.equals(ImportQueueItem.STATUS_ISSUES)) {
            info += " Days To Load: " + iqItem.getEstImportDate() + ".";
            info += " Issues: " + iqItem.getNumIssues() + ".";
            info += getResultsForUserLogging(iqItem.getVerificationResults());
        } else if (newStatus.equals(ImportQueueItem.STATUS_LOADED)) {
            datasetItem = iqItem.getDataset();
            if (datasetItem != null) {
                info += " Dataset: " + iqItem.getDatasetName()
                    + " (" + iqItem.getDataset().getId() + ")" + ".";
            } else {
                // Likely a Discourse is mapped to this IQ item...
                DiscourseItem discourse = getDiscourseFromIQ(iqItem);
                if (discourse != null) {
                    info += " Discourse: " + discourse.getName()
                        + " (" + discourse.getId() + ")" + ".";
                }
            }

        } // no info to add for canceled status

        UserLogger.log(datasetItem, userItem, UserLogger.UPDATE_STATUS_IN_QUEUE, info, false);
    }

    /**
     * Maximum number of chars to show of the verification results
     * in the dataset user log info field.
     */
    private static final Integer MAX_RESULTS = 100;

    /**
     * Check the length of the results and return at most 100 characters of it.
     * @param results the results string
     * @return the results string for the user logging info field
     */
    private String getResultsForUserLogging(String results) {
        if (results == null) { return ""; }
        String msg = " Results: '";
        if (results.length() < MAX_RESULTS) {
            msg += results;
        } else {
            Integer maxIndex = MAX_RESULTS > results.length() ? results.length() : MAX_RESULTS;
            msg += results.substring(0, maxIndex) + "...";
        }
        msg += "'.";
        return msg;
    }

    /**
     * Get the prefix of a user logger info string consistently.
     * @param iqItem the import queue item
     * @return string to use in the info field
     */
    private String getUserInfoString(ImportQueueItem iqItem) {
        String info;
        DatasetItem datasetItem = iqItem.getDataset();
        if (datasetItem != null) {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            datasetItem = datasetDao.get((Integer)iqItem.getDataset().getId());
            info = "Dataset '" + datasetItem.getDatasetName() + "'";
            info += " (" + datasetItem.getId() + "):";
        } else {
            info = "IqItem '" + iqItem.getDatasetName() + "'";
            info += " (" + iqItem.getId() + "):";
        }
        return info;
    }

    /**
     * Write to the dataset user log table what has changed.
     * @param userItem the current user
     * @param action the UserLogger constant
     * @param msg the message to tack onto the info
     * @param iqItem the updated import queue item
     */
    public void logUserAction(UserItem userItem, ImportQueueItem iqItem,
                              String action, String msg) {
        String info = getUserInfoString(iqItem);
        if (msg == null || msg.length() == 0) {
            info = info.replace(":", "");
        } else {
            info += msg;
        }
        UserLogger.log(userItem, action, info, false);
    }

    /**
     * Check if the user has access to this dataset item.
     * @param req {@link HttpServletRequest}
     * @param userItem the current user
     * @param datasetItem the dataset item
     * @return true if the user is a DA or PA of the dataset's project
     */
    public boolean userHasAccess(HttpServletRequest req,
                                 UserItem userItem, DatasetItem datasetItem) {
        // Check if the user is a DataShop administrator
        if (userItem.getAdminFlag()) {
            return true;
        }
        boolean flag = userHasAccess(req, userItem, datasetItem.getProject());
        if (!flag) {
            logger.error("User [" + userItem.getId()
                + "] does not have access to this dataset item ("
                + datasetItem.getId() + ").");
            displayTransientMessage(req, ImportQueueServlet.MSG_NO_AUTH,
                                    ImportQueueServlet.MSG_LEVEL_ERROR);
        }
        return flag;
    }

    /**
     * Check if the user has access to this import queue item.
     * @param req {@link HttpServletRequest}
     * @param userItem the current user
     * @param iqItem the import queue item
     * @return true if the user is a DA, PA, or is the uploader
     */
    public boolean userHasAccess(HttpServletRequest req,
                                 UserItem userItem, ImportQueueItem iqItem) {
        // Check if the user is a DataShop administrator
        if (userItem.getAdminFlag()) {
            return true;
        }
        // Check if the user is the uploader
        if (userItem.getId().equals(iqItem.getUploadedBy().getId())) {
            return true;
        }
        boolean flag = userHasAccess(req, userItem, iqItem.getProject());
        if (!flag) {
            logger.error("User [" + userItem.getId()
                + "] does not have access to this import queue item ("
                + iqItem.getId() + ").");
            displayTransientMessage(req, ImportQueueServlet.MSG_NO_AUTH,
                                    ImportQueueServlet.MSG_LEVEL_ERROR);
        }
        return flag;
    }

    /**
     * Check if the user has admin authorization for the given project.j
     * @param req {@link HttpServletRequest}
     * @param userItem the current user
     * @param projectItem the project item
     * @return true if the user is a project admin, false otherwise
     */
    public boolean userHasAccess(HttpServletRequest req,
                                 UserItem userItem, ProjectItem projectItem) {
        // Check if the user is a Project Administrator
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        if (authDao.isProjectAdmin(userItem, projectItem)) {
            return true;
        }
        return false;
    }

    /**
     * Set request attributes to get a transient message to show up on the UI.
     * If the ImportQueueItem is specified, parse message if necessary for Discourse.
     * @param req {@link HttpServletRequest}
     * @param iqItem the ImportQueue item
     * @param message the message to display to the user
     * @param messageLevel the level of the message
     */
    public void displayTransientMessage(HttpServletRequest req, ImportQueueItem iqItem,
                                        String message, String messageLevel)
    {
        // If ImportQueue item is for a Discourse, change references to "dataset".
        if ((iqItem != null) && (isDiscourse(iqItem))) {
            message = message.replace("dataset", "discourse");
            message = message.replace("Dataset", "Discourse");
        }

        displayTransientMessage(req, message, messageLevel);
    }


    /**
     * A version of the above method without the ImportQueueItem.
     * @param req {@link HttpServletRequest}
     * @param message the message to display to the user
     * @param messageLevel the level of the message
     */
    public void displayTransientMessage(HttpServletRequest req, String message,
                                        String messageLevel)
    {
        req.setAttribute(ImportQueueServlet.REQ_ATTRIB_IQ_MSG_LEVEL, messageLevel);
        req.setAttribute(ImportQueueServlet.REQ_ATTRIB_IQ_MSG_TEXT, message);
    }

    /**
     * Fix the queue order for the remaining items in the list.
     */
    public void fixQueueOrder() {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        List<ImportQueueItem> iqList = iqDao.findItemsInQueue();
        int idx = 1;
        for (ImportQueueItem item : iqList) {
            item.setQueueOrder(idx++);
            iqDao.saveOrUpdate(item);
        }
    }

    /**
     * Get an integer from the string.  Pass in a key for the error message.
     * @param key the field name
     * @param value the field value
     * @return a valid integer if string is parse-able, null otherwise
     */
    private Integer parseInteger(String key, String value) {
        Integer newInt = null;
        try {
            newInt = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            logger.warn(key + " is not a valid integer: " + value);
        }
        return newInt;
    }

    /**
     * Helper method to determine if an ImportQueue item is for a Discourse.
     * @param iqItem the ImportQueueItem
     * @return flag
     */
    public Boolean isDiscourse(ImportQueueItem iqItem) {
        if (iqItem == null) { return false; }
        if (iqItem.getFormat() == null) { return false; }

        return iqItem.getFormat().equals(ImportQueueItem.FORMAT_DISCOURSE);
    }

    /**
     * Helper method to get Discourse mapped to specified ImportQueue.
     * @param iqItem the ImportQueueItem
     * @return the Discourse
     */
    public DiscourseItem getDiscourseFromIQ(ImportQueueItem iqItem) {

        DiscourseImportQueueMapDao mapDao = DaoFactory.DEFAULT.getDiscourseImportQueueMapDao();
        DiscourseImportQueueMapItem mapItem = mapDao.findByImportQueue(iqItem);
        if (mapItem == null) { return null; }

        DiscourseItem discourse = mapItem.getDiscourse();

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscourseItem result = discourseDao.get((Long)discourse.getId());

        return result;
    }
}
