//CHECKSTYLE.OFF: FileLength
/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
//CHECKSTYLE.ON: FileLength
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.dao.DiscourseImportQueueMapDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.ImportQueueModeDao;
import edu.cmu.pslc.datashop.dao.ImportQueueStatusHistoryDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.dto.DiscourseDTO;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.dto.importqueue.ExistingProjectDto;
import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.DiscourseCreator;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ImportQueueModeItem;
import edu.cmu.pslc.datashop.item.ImportQueueStatusHistoryItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.ServletDateUtil;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportHelper;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper;
import edu.cmu.pslc.datashop.servlet.project.CreateProjectServlet;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.FileUtils;


/**
 * TBD fill in description.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueServlet extends AbstractServlet {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(ImportQueueServlet.class.getName());

    /** The JSP file name. */
    private static final String JSP_NAME = "/jsp_dataset/import_queue.jsp";

    /** Title for this page - "Import Queue". */
    public static final String SERVLET_TITLE = "Import Queue";

    /** Label used for setting session attribute "recent_report". */
    public static final String SERVLET_NAME = "ImportQueue";

    /** String constant. */
    public static final String REQ_ATTRIB_IQ_MSG_TEXT = "iq_msg_text";
    /** String constant. */
    public static final String REQ_ATTRIB_IQ_MSG_LEVEL = "iq_msg_level";

    /** String constant. */
    public static final String MSG_LEVEL_ERROR = "error";
    /** String constant. */
    public static final String MSG_LEVEL_SUCCESS = "success";
    /** String constant. */
    public static final String MSG_NO_AUTH =
            "You do not have authorization to view or modify this item.";

    /** String constant. */
    public static final String MSG_MOVE_UP_ERROR =
            "Error occurred while moving item up in the queue.";
    /** String constant. */
    public static final String MSG_MOVE_UP_SUCCESS =
            "Successfully moved item up in the queue.";
    /** String constant. */
    public static final String MSG_MOVE_DOWN_ERROR =
            "Error occurred while moving item down in the queue.";
    /** String constant. */
    public static final String MSG_MOVE_DOWN_SUCCESS =
            "Successfully moved item down in the queue.";
    /** String constant. */
    public static final String MSG_DOWNLOAD_FILE_ERROR =
            "Error occurred while downloading this file.";
    /** String constant. */
    public static final String MSG_RENAME_DATASET_ERROR =
            "Error occurred while renaming this dataset.";
    /** String constant. */
    public static final String MSG_RELEASE_DATASET_ERROR =
            "Error occurred while releasing this dataset.";
    /** String constant. */
    public static final String MSG_MOVE_DATASET_ERROR =
            "Error occurred while moving this dataset.";
    /** String constant. */
    public static final String MSG_DELETE_DATASET_ERROR =
            "Error occurred while deleting this dataset.";
    /** String constant. */
    public static final String MSG_PROJECT_NAME_TAKEN =
            "This project name is already taken.";
    /** String constant. */
    public static final String MSG_DATASET_NAME_TAKEN =
            "This dataset name is already taken.";

    /** String constant. */
    public static final String MSG_MOVE_DATASET_ERROR_WHILE_IMPORTING =
            "Cannot move dataset while import is in progress.";
    /** String constant. */
    public static final String MSG_RENAME_DATASET_ERROR_WHILE_IMPORTING =
            "Cannot rename dataset while import is in progress.";

    /** String constant. */
    public static final String MSG_PAUSE_QUEUE_ERROR =
            "Error occurred while pausing the queue.";
    /** String constant. */
    public static final String MSG_PAUSE_QUEUE_SUCCESS =
            "Successfully paused the queue.";

    /** String constant. */
    public static final String MSG_START_QUEUE_ERROR =
            "Error occurred while starting the queue.";
    /** String constant. */
    public static final String MSG_START_QUEUE_SUCCESS =
            "Successfully started the queue.";

    /** Constant. */
    public static final String FIRST_100_PASSED_MSG = ImportQueueHelper.FIRST_100_PASSED_MSG;

    /** String constant. */
    public static final String REQ_ATTRIB_IMPORT_QUEUE = "import_queue";
    /** String constant. */
    public static final String REQ_ATTRIB_RECENT_ITEMS = "recent_items";
    /** String constant. */
    public static final String REQ_ATTRIB_RECENT_NO_DATA = "no_data_items";
    /** String constant. */
    public static final String REQ_ATTRIB_MODE = "iq_mode_html";
    /** String constant. */
    public static final String EMAIL_SENT_ATTRIB = "Email sent to uploader.";
    /** Ajax name for requesting method. */
    public static final String AJAX_REQUEST_METHOD = "requestingMethod";

    /** The default combo box size for project lists. */
    private static final int DEFAULT_PROJECT_COMBO_BOX_SIZE = 10;
    /** Status values for AJAX responses. */
    private enum DS_RENAME_STATUS {
        /** Dataset renamed. */
            RENAMED,
        /** Dataset exists. */
            EXISTS,
        /** Error. */
            ERROR };
    private enum DS_RELEASE_STATUS {
        /** Dataset released. */
            RELEASED,
        /** Error. */
            ERROR };
    private enum DS_MOVE_STATUS {
        /** Dataset moved. */
            MOVED,
        /** Project name exists. */
            EXISTS,
        /** Error. */
            ERROR };
    private enum DS_DELETE_STATUS {
        /** Dataset deleted from import_queue and ds_dataset tables. */
            DELETED,
        /** Dataset not deleted from remote DataShop instance. */
            WARNING,
        /** Error. */
            ERROR };
    private enum DS_MOVE_ATTRIB {
        /** New project creation. */
            NEW,
        /** Add to existing project. */
            EXISTING };

    //----- ENUM : Show items from last X days -----

    /** Enumerated type list of valid values. */
    static final List<String> LAST_DAYS_ENUM = new ArrayList<String>();
    /** Enumerated type constant. */
    public static final String VALUE_3_MONTHS = "90";
    /** Enumerated type constant. */
    public static final String VALUE_6_MONTHS = "180";
    /** Enumerated type constant. */
    public static final String VALUE_1_YEAR = "365";
    /** Default value. */
    public static final String VALUE_DEFAULT = VALUE_3_MONTHS;
    static {
        LAST_DAYS_ENUM.add(VALUE_3_MONTHS);
        LAST_DAYS_ENUM.add(VALUE_6_MONTHS);
        LAST_DAYS_ENUM.add(VALUE_1_YEAR);
    }
    /** Display text for enumerated type. */
    public static final String TXT_3_MONTHS = "3 months";
    /** Display text for enumerated type. */
    public static final String TXT_6_MONTHS = "6 months";
    /** Display text for enumerated type. */
    public static final String TXT_1_YEAR = "1 year";
    /** Map from enumerated type to display text. */
    public static final Map<String, String> LD_VALUE_TXT_MAP
        = new LinkedHashMap<String, String>();
    static {
        LD_VALUE_TXT_MAP.put(VALUE_3_MONTHS, TXT_3_MONTHS);
        LD_VALUE_TXT_MAP.put(VALUE_6_MONTHS, TXT_6_MONTHS);
        LD_VALUE_TXT_MAP.put(VALUE_1_YEAR, TXT_1_YEAR);
    }

    /** ImportQueueHelper. */
    private ImportQueueHelper iqHelper;

    /**
     * Handles the HTTP GET.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        try {
            setEncoding(req, resp);

            // Set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // Get current user
            UserItem userItem = getUser(req);

            iqHelper = HelperFactory.DEFAULT.getImportQueueHelper();

            // Handle request based on parameter
            String actionParam = getParameter(req, AJAX_REQUEST_METHOD);
            if (actionParam == null) {
                goToImportQueuePage(req, resp, userItem);
            } else if (actionParam.equals("downloadFile")) {
                downloadFile(req, resp, userItem);
                return;
            } else if (actionParam.equals("renameDataset")) {
                renameDataset(req, resp, userItem);
                return;
            } else if (actionParam.equals("createReleaseDatasetDialog")) {
                createReleaseDialog(req, resp, userItem);
                return;
            } else if (actionParam.equals("releaseDataset")) {
                releaseDataset(req, resp, userItem);
                return;
            } else if (actionParam.equals("createMoveDatasetDialog")) {
                createMoveDialog(req, resp, userItem);
                return;
            } else if (actionParam.equals("moveDataset")) {
                moveDataset(req, resp, userItem);
                return;
            } else if (actionParam.equals("createDeleteDatasetDialog")) {
                createDeleteDialog(req, resp, userItem);
                return;
            } else if (actionParam.equals("deleteDataset")) {
                deleteDataset(req, resp, userItem);
                return;
            } else if (actionParam.equals("getProjectMetaData")) {
                getProjectMetaData(req, resp);
                return;
            } else if (actionParam.equals("saveStatusChangesAjaxRequest")) {
                saveStatusChangesAjaxRequest(req, resp, userItem);
                return;
            } else if (actionParam.equals("checkDatasetNameAjaxRequest")) {
                checkDatasetNameAjaxRequest(req, resp);
                return;
            } else if (actionParam.equals("pauseQueue")) { //Not Ajax
                pauseQueue(req, resp, userItem);
                goToImportQueuePage(req, resp, userItem);
            } else if (actionParam.equals("startQueue")) { //Not Ajax
                startQueue(req, resp, userItem);
                goToImportQueuePage(req, resp, userItem);
            } else if (actionParam.equals("moveItemUp")) { //Not Ajax
                moveItemUp(req, resp, userItem);
            } else if (actionParam.equals("moveItemDown")) { //Not Ajax
                moveItemDown(req, resp, userItem);
            } else if (actionParam.equals("hideRow")) { //Not Ajax
                iqHelper.hideRow(req, resp, userItem);
                goToImportQueuePage(req, resp, userItem);
            } else if (actionParam.equals("cancelImport")) { //Not Ajax
                iqHelper.cancelImport(req, resp, userItem);
                goToImportQueuePage(req, resp, userItem);
            } else if (actionParam.equals("undoCancel")) { //Not Ajax
                iqHelper.undoCancel(req, resp, userItem);
                goToImportQueuePage(req, resp, userItem);
            } else if (actionParam.equals("sortRecentlyLoaded")) { //Not Ajax
                sortRecentlyLoaded(req, resp, userItem);
            } else if (actionParam.equals("sortNoData")) { //Not Ajax
                sortNoData(req, resp, userItem);
            } else {
                goToImportQueuePage(req, resp, userItem);
            }
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    }

    /** String constant. */
    public static final String PARAM_LAST_DAYS_RECENT = "lastDaysRecentSelect";
    /** String constant. */
    public static final String ATTRIB_LAST_DAYS_RECENT = "iqLastDaysRecent";
    /** String constant. */
    public static final String PARAM_LAST_DAYS_NODATA = "lastDaysNoDataSelect";
    /** String constant. */
    public static final String ATTRIB_LAST_DAYS_NODATA = "iqLastDaysNoData";

    /**
     * Go to the home page if user. Used if user is not a DA (DataShop Administrator)
     * or tries to access a page without authorization.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void goToHomePage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher disp =
                getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
        disp.forward(req, resp);
    }

    /**
     * This is the default action of the servlet if its not an AJAX request.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void goToImportQueuePage(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws ServletException, IOException {
        // if user is not a DA, go to home page
        if (!userItem.getAdminFlag()) { goToHomePage(req, resp); return; }

        HttpSession httpSession = req.getSession(true);
        // Regular page request
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        List<ImportQueueDto> importQueueList = iqDao.getImportQueueForAdmin();

        //Get the time frame for the 'Recently loaded...' table
        Date recentCutoffDate = calcCutoffDate(req, httpSession,
                PARAM_LAST_DAYS_RECENT, ATTRIB_LAST_DAYS_RECENT, VALUE_DEFAULT);

        //Get the time frame for the 'Recently created with no data...' table
        Date nodataCutoffDate = calcCutoffDate(req, httpSession,
                PARAM_LAST_DAYS_NODATA, ATTRIB_LAST_DAYS_NODATA, VALUE_DEFAULT);

        ImportQueueContext context = ImportQueueContext.getContext(req);
        String sortByColumn = context.getLoadedSortByColumn();
        Boolean isAscending = context.isLoadedAscending(sortByColumn);

        // Now get the list of recent items ...
        List<ImportQueueDto> recentItemsList =
                iqDao.getRecentItemsForAdmin(recentCutoffDate);
        Comparator<ImportQueueDto> comparator =
            ImportQueueDto.getComparator(ImportQueueDto.getSortByParameters(sortByColumn,
                                                                            isAscending));
        Comparator<ImportQueueDto> nullComparator = new NullComparator(comparator, false);
        Collections.sort(recentItemsList, nullComparator);

        // ...and the list of items with no data
        List<ImportQueueDto> noDataItemsList =
                iqDao.getRecentNoDataItemsForAdmin(nodataCutoffDate);
        sortByColumn = context.getNoDataSortByColumn();
        isAscending = context.isNoDataAscending(sortByColumn);
        comparator =
            ImportQueueDto.getComparator(ImportQueueDto.getSortByParameters(sortByColumn,
                                                                            isAscending));
        nullComparator = new NullComparator(comparator, false);
        Collections.sort(noDataItemsList, nullComparator);

        // Figure out which items have a status change that can be 'undone'.
        importQueueList = iqHelper.setShowUndoFlag(context, importQueueList);
        recentItemsList = iqHelper.setShowUndoFlag(context, recentItemsList);

        // Get the Import Queue Mode
        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        ImportQueueModeItem modeItem = modeDao.get(ImportQueueModeItem.ID);
        if (modeItem == null) {
            Date now = new Date();
            modeItem = new ImportQueueModeItem(ImportQueueModeItem.ID);
            modeItem.setMode(ImportQueueModeItem.MODE_PAUSE);
            modeItem.setUpdatedBy(userItem);
            modeItem.setUpdatedTime(now);
            modeItem.setStatus(ImportQueueModeItem.STATUS_WAITING);
            modeItem.setStatusTime(now);
            modeItem.setImportQueue(null);
            modeItem.setExitFlag(false);
            modeDao.saveOrUpdate(modeItem);
        }
        String modeHtml = getModeHtml(modeItem);

        // Put the data in the HTTP session for the JSP.
        req.setAttribute(REQ_ATTRIB_IMPORT_QUEUE, importQueueList);
        req.setAttribute(REQ_ATTRIB_RECENT_ITEMS, recentItemsList);
        req.setAttribute(REQ_ATTRIB_RECENT_NO_DATA, noDataItemsList);
        req.setAttribute(REQ_ATTRIB_MODE, modeHtml);

        UserLogger.log(userItem, UserLogger.VIEW_IMPORT_QUEUE, "", true);

        // forward to the JSP (view)
        RequestDispatcher disp;
        disp = getServletContext().getRequestDispatcher(JSP_NAME);
        disp.forward(req, resp);
    }

    /** UI String constant. */
    private static final String STARTED = "started by ";
    /** UI String constant. */
    private static final String STOPPED = "stopped by ";
    /** UI String constant. */
    private static final String CURRENT = "current time on task: ";

    /**
     * Return the html for the import queue mode section of the display.
     * Decided that this was easier than creating a DTO and putting all the java in the jsp.
     * @param modeItem the ImportQueueModeItem
     * @return an html string
     */
    private String getModeHtml(ImportQueueModeItem modeItem) {
        String html;
        String mode = modeItem.getMode();
        String status = modeItem.getStatus();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem updatedBy = userDao.get((String)modeItem.getUpdatedBy().getId());
        String whoWhen = updatedBy.getName() + " "
                       + ImportQueueDto.getLastUpdateString(modeItem.getUpdatedTime());

        if (mode.equals(ImportQueueModeItem.MODE_ERROR)) {
            html  = "<img src=\"images/cross.png\">";
            html += "<div class=\"status\">&nbsp;error - stopped</div>";

        } else if (status.equals(ImportQueueModeItem.STATUS_VERIFYING)
                || status.equals(ImportQueueModeItem.STATUS_IMPORTING)) {
            html  = "<img src=\"images/iq_verifying.png\">";
            html += "<div class=\"status\">&nbsp;" + status + "&nbsp;&middot;&nbsp;";
            if (mode.equals(ImportQueueModeItem.MODE_PLAY)) {
                html += "<a href=\"javascript:pauseQueue()\">pause</a></div>";
            } else {
                html += "stopping after current task&nbsp;";
                html += "<a href=\"javascript:startQueue()\">don't stop</a></div>";
            }
            html += "<div class=\"by\">" + STARTED + whoWhen + "</div>";
            html += "<div class=\"by\">" + CURRENT + " "
                 + ImportQueueDto.getAmountOfTime(modeItem.getStatusTime()) + "</div>";

        } else if (status.equals(ImportQueueModeItem.STATUS_WAITING)) {
            if (mode.equals(ImportQueueModeItem.MODE_PLAY)) {
                html  = "<img src=\"images/iq_waiting.png\">";
                html += "<div class=\"status\">&nbsp;waiting&nbsp;&middot;&nbsp;";
                html += "<a href=\"javascript:pauseQueue()\">pause</a></div>";
                html += "<div class=\"by\">" + STARTED + whoWhen + "</div>";
            } else {
                html  = "<img src=\"images/iq_paused.png\">";
                html += "<div class=\"status\">&nbsp;stopped&nbsp;&middot;&nbsp;";
                html += "<a href=\"javascript:startQueue()\">start</a></div>";
                html += "<div class=\"by\">" + STOPPED + whoWhen + "</div>";
            }
        } else {
            html = "<img src=\"images/cross.png\">&nbsp;error - stopped";
        }
        return html;
    }

    /**
     * Handle request to download a file for a given IRB.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void downloadFile(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {
        ImportQueueItem iqItem = iqHelper.getImportQueueItemRefresh(req, MSG_DOWNLOAD_FILE_ERROR);
        if (iqItem == null) { goToHomePage(req, resp); return; }
        if (!iqHelper.userHasAccess(req, userItem, iqItem)) { goToHomePage(req, resp); return; }

        FileItem fileItem = getFileItem(iqItem);
        if (fileItem == null) {
            Integer fileId = getIntegerId((String)req.getParameter("fileId"));
            logger.error("Invalid fileId specified: " + fileId);
        } else {
            try {
                Integer fileId = (Integer)fileItem.getId();
                String fileName = fileItem.getFileName();
                String actualFileName = fileItem.getUrl(getBaseDir());

                String info = fileName + " (" + fileId + ")";

                if (logger.isDebugEnabled()) {
                    logger.debug("downloadFile: " + info);
                }

                resp.setContentType("application/x-download");
                resp.setHeader("Content-Disposition",
                               "attachment; filename=\"" + fileName + "\"");

                BufferedInputStream inStream = null;
                OutputStream outStream = resp.getOutputStream();
                try {
                    inStream = new BufferedInputStream(new FileInputStream(actualFileName));

                    int ch;
                    while ((ch = inStream.read()) != -1) {
                        outStream.write(ch);
                    }

                    UserLogger.log(userItem, UserLogger.FILE_DOWNLOAD, info, false);

                } catch (FileNotFoundException exception) {
                    logger.error("downloadFile: FileNotFoundException occurred: "
                                 + info + " :: " + actualFileName,  exception);
                } finally {
                    // very important
                    if (inStream != null) { inStream.close(); }
                    if (outStream != null) { outStream.close(); }
                }
            } catch (Exception exception) {
                logger.error("downloadFile: Exception occurred.", exception);
            }
        }
    }

    /**
     * Get the File Item from the database using the request parameter.
     * @param iqItem the import queue item
     * @return the file item
     */
    private FileItem getFileItem(ImportQueueItem iqItem) {
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        Integer fileId = (Integer)iqItem.getFile().getId();
        FileItem fileItem = fileDao.get(fileId);
        if (fileItem == null) {
            logger.error("Invalid file id: " + fileId);
        }
        return fileItem;
    }

    /**
     * Saves the status changes including other fields to the database unless
     * a field value is invalid.  Returns JSON to caller.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws JSONException JSON exception
     */
//CHECKSTYLE.OFF: MethodLength
    private void saveStatusChangesAjaxRequest(HttpServletRequest req,
//CHECKSTYLE.ON: MethodLength
            HttpServletResponse resp, UserItem userItem)
                    throws JSONException, IOException {
        boolean errorFlag = false, fixQueueOrderFlag = false;
        String generalErrorMsg = "";
        String status = getParameter(req, "status");
        String statusErrorMsg = "";
        String estImportDateString = getParameter(req, "estImportDate");
        String estImportDateErrorMsg = "";
        String errorsString = getParameter(req, "errors");
        String errorsErrorMsg = "";
        String issuesString = getParameter(req, "issues");
        String issuesErrorMsg = "";
        String results = getParameter(req, "results");
        String resultsErrorMsg = "";
        String datasetIdString = getParameter(req, "datasetId");
        String datasetIdErrorMsg = "";
        String isDiscourseStr = getParameter(req, "isDiscourse");
        Boolean isDiscourse = false;
        if ((isDiscourseStr != null) && isDiscourseStr.equals("true")) { isDiscourse = true; }

        boolean estImportDateUpdated = false;
        Date origEstImportDate = null, estImportDate = null;
        ImportQueueItem iqItem = null;
        IqStatus prevIqStatus = null;

        if (!userItem.getAdminFlag()) {
            errorFlag = true;
            generalErrorMsg = MSG_NO_AUTH;
        } else {
            iqItem = iqHelper.getImportQueueItemJson(req);
        }
        if (iqItem == null) {
            errorFlag = true;
            generalErrorMsg = "Unable to update status.";
        } else {
            prevIqStatus = new IqStatus(iqItem);
            origEstImportDate = iqItem.getEstImportDate();

            // Get Estimated Import Date
            if (status.equals(ImportQueueItem.STATUS_QUEUED)
                    || status.equals(ImportQueueItem.STATUS_PASSED)
                    || status.equals(ImportQueueItem.STATUS_ISSUES)) {
                estImportDate = parseDate("estImportDate", estImportDateString);
                if (estImportDateString != null && !estImportDateString.isEmpty()) {
                    if (estImportDate == null) {
                        errorFlag = true;
                        estImportDateErrorMsg = "Please enter a valid date string, yyyy-mm-dd.";
                    } else {
                        iqItem.setEstImportDate(estImportDate);
                        estImportDateUpdated = true;
                    }
                } else {
                    if (iqItem.getEstImportDate() != null) {
                        iqItem.setEstImportDate(null);
                        // we do not need to send email in this case,
                        // which is what the flag, estImportDateUpdated, means
                    }
                }
            }
            // Get Issues and Verification Results
            if (status.equals(ImportQueueItem.STATUS_ERRORS)
                    || status.equals(ImportQueueItem.STATUS_ISSUES)) {
                //issues
                Integer issues = parseInteger("issues", issuesString);
                if (issues == null || issues < 0) {
                    errorFlag = true;
                    issuesErrorMsg = "Please enter a positive integer.";
                } else {
                    iqItem.setNumIssues(issues);
                }
                //results
                if (iqItem.getFormat().equals(ImportQueueItem.FORMAT_XML)
                        && (results == null || results.length() == 0)) {
                    errorFlag = true;
                    resultsErrorMsg = "Please enter the verification results.";
                } else {
                    iqItem.setVerificationResults(results);
                }
            }
            // Get Errors
            if (status.equals(ImportQueueItem.STATUS_ERRORS)) {
                Integer errors = parseInteger("errors", errorsString);
                if (errors == null || errors < 0) {
                    errorFlag = true;
                    errorsErrorMsg = "Please enter a positive integer.";
                } else {
                    iqItem.setNumErrors(errors);
                }
            }
            // Get Dataset Id
            if (status.equals(ImportQueueItem.STATUS_LOADED)) {
                if (!isDiscourse) {
                    DatasetItem datasetItem = getDatasetItem(datasetIdString);
                    datasetIdErrorMsg = isDatasetAvailable(datasetItem);
                    if (datasetIdErrorMsg != null) {
                        errorFlag = true;
                    } else {
                        // check that the dataset name is unique, create new one if not
                        iqItem.setDataset(datasetItem);
                        if (iqItem.getProject() != null) {
                            // Set the project on the dataset
                            ProjectItem projectItem = iqItem.getProject();
                            datasetItem.setProject(projectItem);
                            
                            // Set the dataset last added value on the project
                            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                            projectItem = projectDao.get((Integer)projectItem.getId());
                            projectItem.setDatasetLastAddedToNow();
                            projectDao.saveOrUpdate(projectItem);
                        } else if (datasetItem.getProject() != null) {
                            iqItem.setProject(datasetItem.getProject());
                        }
                        if (iqItem.getDescription() != null
                            && !iqItem.getDescription().isEmpty()) {
                            String newDesc = "";
                            if (datasetItem.getDescription() != null
                                && !datasetItem.getDescription().isEmpty()) {
                                newDesc = datasetItem.getDescription()
                                    + System.getProperty("line.separator");
                            }
                            newDesc += iqItem.getDescription();
                            datasetItem.setDescription(newDesc);
                        }
                        if (iqItem.getAdtlDatasetNotes() != null
                            && !iqItem.getAdtlDatasetNotes().isEmpty()) {
                            String newNotes = "";
                            if (datasetItem.getNotes() != null
                                && !datasetItem.getNotes().isEmpty()) {
                                newNotes = datasetItem.getNotes()
                                    + System.getProperty("line.separator");
                            }
                            newNotes += iqItem.getAdtlDatasetNotes();
                            
                            datasetItem.setNotes(newNotes);
                        }
                        String newDatasetName = checkCreateDatasetName(iqItem.getDatasetName(),
                                                                       datasetItem);
                        datasetItem.setDatasetName(newDatasetName);
                        if (!newDatasetName.equals(iqItem.getDatasetName())) {
                            iqItem.setDatasetName(newDatasetName);
                        }
                        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                        datasetDao.saveOrUpdate(datasetItem);

                        updateMasterInstance(datasetItem);
                        
                        SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();
                        Long numTxs = metricDao.getTotalTransactions(datasetItem);
                        iqItem.setNumTransactions(numTxs);
                    }
                } else {
                    // Get Discourse
                    Long discourseId = parseInteger("discourseId", datasetIdString).longValue();
                    DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
                    DiscourseItem discourse = discourseDao.get(discourseId);
                    datasetIdErrorMsg = isDiscourseAvailable(discourse);
                    if (datasetIdErrorMsg != null) {
                        errorFlag = true;
                    } else {
                        String newDiscourseName = checkCreateDiscourseName(iqItem.getDatasetName(),
                                                                           discourse);
                        if (!newDiscourseName.equals(iqItem.getDatasetName())) {
                            iqItem.setDatasetName(newDiscourseName);
                        }

                        discourse.setName(newDiscourseName);

                        // If projectId not already set, use value in IQ.
                        if (discourse.getProjectId() == null) {
                            ProjectItem iqProject = iqItem.getProject();
                            if (iqProject != null) {
                                discourse.setProjectId((Integer)iqProject.getId());
                            }
                        }

                        discourseDao.saveOrUpdate(discourse);

                        updateMasterInstance(discourse);

                        // Update map indicating connection between Discourse and IQ item.
                        DiscourseImportQueueMapDao mapDao =
                            DaoFactory.DEFAULT.getDiscourseImportQueueMapDao();
                        DiscourseImportQueueMapItem mapItem = new DiscourseImportQueueMapItem();
                        mapItem.setDiscourseExternal(discourse);
                        mapItem.setImportQueueExternal(iqItem);
                        mapDao.saveOrUpdate(mapItem);
                    }
                }
            }
            // Clear Queue Order and Days to Load for non-queue states
            if (status.equals(ImportQueueItem.STATUS_ERRORS)
                    || status.equals(ImportQueueItem.STATUS_LOADED)
                    || status.equals(ImportQueueItem.STATUS_CANCELED)) {
                iqItem.setQueueOrder(null);
                iqItem.setEstImportDate(null);
                if (prevIqStatus.getStatus() == null
                        || prevIqStatus.getStatus().equals(ImportQueueItem.STATUS_QUEUED)
                        || prevIqStatus.getStatus().equals(ImportQueueItem.STATUS_PASSED)
                        || prevIqStatus.getStatus().equals(ImportQueueItem.STATUS_ISSUES)) {
                    fixQueueOrderFlag = true;
                }
            } else if (status.equals(ImportQueueItem.STATUS_QUEUED)
                    || status.equals(ImportQueueItem.STATUS_PASSED)
                    || status.equals(ImportQueueItem.STATUS_ISSUES)) {
                if (prevIqStatus.getStatus() == null
                        || prevIqStatus.getStatus().equals(ImportQueueItem.STATUS_ERRORS)
                        || prevIqStatus.getStatus().equals(ImportQueueItem.STATUS_LOADED)
                        || prevIqStatus.getStatus().equals(ImportQueueItem.STATUS_CANCELED)) {
                    fixQueueOrderFlag = true;
                }
            }
            iqItem.setStatus(status);
        }
        if (!errorFlag) {
            ImportQueueDao dao = DaoFactory.DEFAULT.getImportQueueDao();
            iqItem.setLastUpdatedTime(new Date());
            dao.saveOrUpdate(iqItem);

            iqHelper.logUpdateStatusInQueue(userItem, prevIqStatus.getStatus(), iqItem);

            ImportQueueContext context = ImportQueueContext.getContext(req);
            context.setIqStatus((Integer)iqItem.getId(), prevIqStatus);

            // Fix the queue order for all the other import queue items
            // after saving the changes for the current one.
            if (fixQueueOrderFlag) {
                iqHelper.fixQueueOrder();
            }

            // Now get the new 'estimated days to load'...
            // If 'estImportDate' has been set, get formatted date.
            ImportQueueDto iqDto = dao.getImportQueueById((Integer)iqItem.getId());
            String estImportDateStr = (iqItem.getEstImportDate() != null)
                ? iqDto.getEstImportDateFormatted() : null;

            String dsHelpEmail = getEmailAddressDatashopHelp();
            String emailContent = null;
            // If status also changed, skip this first email.
            if (estImportDateUpdated && (prevIqStatus.getStatus().equals(status))) {
                if (isSendmailActive()
                    && ((origEstImportDate == null)
                        || (!origEstImportDate.equals(iqItem.getEstImportDate())))) {
                    emailContent = EmailUtil.notifyUserOfStatusChange(origEstImportDate, status,
                                                                      estImportDateStr,
                                                                      iqItem, dsHelpEmail);
                }
                if (emailContent != null) {
                    // send email and update session
                    String emailSubject =
                        EmailUtil.getStatusChangeSubject(origEstImportDate, status, iqItem);
                    sendEmail(dsHelpEmail, ImportQueueHelper.getToAddress(iqItem, dsHelpEmail),
                              emailSubject, emailContent);
                    req.getSession().setAttribute(EMAIL_SENT_ATTRIB, EMAIL_SENT_ATTRIB);
                }
            }
            if (isSendmailActive() && (!prevIqStatus.getStatus().equals(status))) {
                emailContent = EmailUtil.notifyUserOfStatusChange(status, estImportDateStr, iqItem,
                                                                  getBaseUrl(req), dsHelpEmail,
                                                                  false);
                if (emailContent != null) {
                    // send email and update session
                    String emailSubject = EmailUtil.getStatusChangeSubject(status, iqItem);
                    sendEmail(dsHelpEmail, ImportQueueHelper.getToAddress(iqItem, dsHelpEmail),
                              emailSubject, emailContent);
                    req.getSession().setAttribute(EMAIL_SENT_ATTRIB, EMAIL_SENT_ATTRIB);
                }
            }

            // Check that the status changed, before creating a history item
            if (!prevIqStatus.getStatus().equals(status)) {
                ImportQueueStatusHistoryItem historyItem = new ImportQueueStatusHistoryItem();
                historyItem.setImportQueue(iqItem);
                historyItem.setUpdatedBy(userItem);
                historyItem.setUpdatedTime(new Date());
                historyItem.setStatus(status);
                ImportQueueStatusHistoryDao historyDao =
                        DaoFactory.DEFAULT.getImportQueueStatusHistoryDao();
                historyDao.saveOrUpdate(historyItem);
            }

            writeJSON(resp, json(
                    "msg", "success",
                    "importQueueId", (Integer)iqItem.getId(),
                    "status", status,
                    "estImportDate", estImportDateString,
                    "errors", errorsString,
                    "issues", issuesString,
                    "results", results,
                    "datasetId", datasetIdString
                    ));
        } else {
            writeJSON(resp, json(
                    "msg", "error",
                    "generalErrorMsg", generalErrorMsg,
                    "statusErrorMsg", statusErrorMsg,
                    "estImportDateErrorMsg", estImportDateErrorMsg,
                    "errorsErrorMsg", errorsErrorMsg,
                    "issuesErrorMsg", issuesErrorMsg,
                    "resultsErrorMsg", resultsErrorMsg,
                    "datasetIdErrorMsg", datasetIdErrorMsg
                    ));
        }
    }

    /**
     * Check if the given name is unique among ALL datasets in the dataset table.
     * If not, then try to generate one.
     * @param iqDatasetName the dataset name the uploader entered
     * @param dsItem the dataset item
     * @return the given dataset name if unique, or the same name with a number appended
     */
    private String checkCreateDatasetName(String iqDatasetName, DatasetItem dsItem) {
        String newDatasetName = iqDatasetName;
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        List<DatasetItem> dsList = dsDao.find(newDatasetName);
        int idx = 1;
        while (!dsList.isEmpty()) {
            if (dsItem.getId().equals(dsList.get(0).getId())) {
                break;
            }
            idx++;
            newDatasetName = iqDatasetName + "_" + idx;
            dsList = dsDao.find(newDatasetName);
        }
        return newDatasetName;
    }

    /**
     * Check if the given name is unique among ALL discourses.
     * If not, then try to generate one.
     * @param iqDiscourseName the discourse name the uploader entered
     * @param discourse the DiscourseItem
     * @return the given discourse name if unique, or the same name with a number appended
     */
    private String checkCreateDiscourseName(String iqDiscourseName, DiscourseItem discourse) {
        String newName = iqDiscourseName;
        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscourseItem found = discourseDao.findByName(newName);
        int idx = 1;
        while (found != null) {
            if (discourse.getId().equals(found.getId())) {
                break;
            }
            idx++;
            newName = iqDiscourseName + "_" + idx;
            found = discourseDao.findByName(newName);
        }

        return newName;
    }

    /**
     * Get an integer from the string.  Pass in a key for the error message.
     * @param key the field name
     * @param value the field value
     * @return a valid integer if string is parse-able, null otherwise
     */
    private static Integer parseInteger(String key, String value) {
        Integer newInt = null;
        try {
            newInt = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            logger.warn(key + " is not a valid integer: " + value);
        }
        return newInt;
    }

    /**
     * Returns an DatasetItem if one can be found.
     * @param idString a string of the user-entered dataset id
     * @return an DatasetItem if found, null otherwise
     */
    private DatasetItem getDatasetItem(String idString) {
        if (idString == null) {
            logDebug("datasetId should not be null");
            return null;
        }

        DatasetItem item = null;
        Integer itemId = parseInteger("datasetId", idString);
        if (itemId != null) {
            DatasetDao dao = DaoFactory.DEFAULT.getDatasetDao();
            item = dao.get(itemId);
        }

        if (item == null) {
            logDebug("DatasetItem is not found: ", idString);
        }

        return item;
    }

    /**
     * Get a date from the string.  Pass in a key for the error message.
     * @param key the field name
     * @param value the field value
     * @return a valid date if string is parse-able, null otherwise
     */
    private static Date parseDate(String key, String value) {
        Date newDate = ServletDateUtil.getDateFromString(value);
        if (newDate == null) {
            logger.warn(key + " is not a valid date: " + value);
        }
        return newDate;
    }

    /** Date format for dataset creation time. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    /**
     * Saves the status changes including other fields to the database unless
     * a field value is invalid.  Returns JSON to caller.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void checkDatasetNameAjaxRequest(HttpServletRequest req, HttpServletResponse resp)
            throws JSONException, IOException {
        boolean errorFlag = false;
        String projectName = "";
        String datasetName = "";
        String creationDateString = "";
        String datasetIdString = getParameter(req, "datasetId");
        String datasetIdErrorMsg = "";

        // See if the ImportQueue item is for a Discourse...
        String isDiscourseStr = getParameter(req, "isDiscourse");
        Boolean isDiscourse = false;
        if ((isDiscourseStr != null) && isDiscourseStr.equals("true")) { isDiscourse = true; }

        if (!isDiscourse) {
            // Get Dataset Id
            DatasetItem datasetItem = getDatasetItem(datasetIdString);
            datasetIdErrorMsg = isDatasetAvailable(datasetItem);
            if (datasetIdErrorMsg != null) {
                errorFlag = true;
            } else {
                datasetName = datasetItem.getDatasetName();
                //Get the creation date from the dataset system log table.
                DatasetSystemLogDao logDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
                Date creationDate = logDao.getLastCreated(datasetItem);
                if (creationDate == null) {
                    creationDate = logDao.getLastModified(datasetItem);
                }
                if (creationDate == null) {
                    creationDateString = "unknown";
                } else {
                    creationDateString = DATE_FMT.format(creationDate);
                }
                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                ProjectItem projectItem = datasetItem.getProject();
                if (projectItem == null) {
                    projectName = "not set";
                } else {
                    projectItem = projectDao.get((Integer)projectItem.getId());
                    if (projectItem != null) {
                        projectName = projectItem.getProjectName();
                    } else {
                        projectName = "not set";
                    }
                }
            }
        } else {
            // Get Discourse
            Long discourseId = parseInteger("discourseId", datasetIdString).longValue();
            DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
            DiscourseItem discourse = discourseDao.get(discourseId);
            datasetIdErrorMsg = isDiscourseAvailable(discourse);
            if (datasetIdErrorMsg != null) {
                errorFlag = true;
            } else {
                datasetName = discourse.getName();
                Date creationDate = discourse.getCreated();
                if (creationDate == null) {
                    creationDateString = "unknown";
                } else {
                    creationDateString = DATE_FMT.format(creationDate);
                }
                Integer projectId = discourse.getProjectId();
                if (projectId == null) {
                    projectName = "not set";
                } else {
                    ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                    ProjectItem projectItem = projectDao.get(projectId);
                    if (projectItem != null) {
                        projectName = projectItem.getProjectName();
                    } else {
                        projectName = "not set";
                    }
                }
            }
        }

        if (!errorFlag) {
            writeJSON(resp, json(
                    "msg", "success",
                    "projectName", projectName,
                    "datasetName", datasetName,
                    "creationDate", creationDateString,
                    "datasetId", datasetIdString
                    ));
        } else {
            writeJSON(resp, json(
                    "msg", "error",
                    "datasetIdErrorMsg", datasetIdErrorMsg
                    ));
        }
    }

    /**
     * Pause the queue.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void pauseQueue(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
            throws JSONException, IOException {
        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        ImportQueueModeItem modeItem = modeDao.get(ImportQueueModeItem.ID);
        if (modeItem == null) {
            iqHelper.displayTransientMessage(req, MSG_PAUSE_QUEUE_ERROR, MSG_LEVEL_ERROR);
        } else {
            modeItem.setMode(ImportQueueModeItem.MODE_PAUSE);
            modeItem.setUpdatedBy(userItem);
            modeItem.setUpdatedTime(new Date());
            modeDao.saveOrUpdate(modeItem);
            iqHelper.displayTransientMessage(req, MSG_PAUSE_QUEUE_SUCCESS, MSG_LEVEL_SUCCESS);
        }
    }

    /**
     * Start the queue.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void startQueue(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
            throws JSONException, IOException {
        ImportQueueModeDao modeDao = DaoFactory.DEFAULT.getImportQueueModeDao();
        ImportQueueModeItem modeItem = modeDao.get(ImportQueueModeItem.ID);
        if (modeItem == null) {
            iqHelper.displayTransientMessage(req, MSG_START_QUEUE_ERROR, MSG_LEVEL_ERROR);
        } else {
            modeItem.setMode(ImportQueueModeItem.MODE_PLAY);
            modeItem.setUpdatedBy(userItem);
            modeItem.setUpdatedTime(new Date());
            modeDao.saveOrUpdate(modeItem);
            iqHelper.displayTransientMessage(req, MSG_START_QUEUE_SUCCESS, MSG_LEVEL_SUCCESS);
        }
    }

    /**
     * Check if a dataset is available to be attached to an import queue item.
     * Check if the dataset id is valid, whether the dataset has been released or deleted,
     * and that its not already attached to an import queue item.
     * @param datasetItem the dataset to check
     * @return an empty string if it is available, an error message otherwise
     */
    private String isDatasetAvailable(DatasetItem datasetItem) {
        String errorMsg = null;
        if (datasetItem == null) {
            errorMsg = "Please enter a valid dataset id.";
        } else if (datasetItem.getDeletedFlag() != null && datasetItem.getDeletedFlag()) {
            errorMsg = "Please enter a dataset which is not already deleted.";
        } else if (datasetItem.getReleasedFlag() != null && datasetItem.getReleasedFlag()) {
            errorMsg = "Please enter a dataset which is not already released.";
        } else {
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            boolean alreadyAttached = iqDao.isDatasetAlreadyAttached((Integer)datasetItem.getId());
            if (alreadyAttached) {
                errorMsg = "Please enter a dataset which is not already used.";
            }
        }
        return errorMsg;
    }

    /**
     * Check if a discourse is available to be attached to an import queue item.
     * Check if the discourse id is valid and that its not already attached
     * to an import queue item.
     * @param discourseItem the discourse to check
     * @return an empty string if it is available, an error message otherwise
     */
    private String isDiscourseAvailable(DiscourseItem discourseItem) {
        String errorMsg = null;
        if (discourseItem == null) {
            errorMsg = "Please enter a valid discourse id.";
        } else {
            DiscourseImportQueueMapDao mapDao = DaoFactory.DEFAULT.getDiscourseImportQueueMapDao();
            boolean alreadyAttached = mapDao.isDiscourseAlreadyAttached(discourseItem);
            if (alreadyAttached) {
                errorMsg = "Please enter a discourse which is not already used.";
            }
        }
        return errorMsg;
    }

    /**
     * Handle the Move-Item-Up action.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void moveItemUp(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws ServletException, IOException {
        ImportQueueItem iqItem = iqHelper.getImportQueueItemRefresh(req, MSG_MOVE_UP_ERROR);
        if (iqItem == null) { return; }
        if (!iqHelper.userHasAccess(req, userItem, iqItem)) { return; }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        Integer prevOrder = iqItem.getQueueOrder();
        Integer thisOrder = iqItem.getQueueOrder() - 1;
        ImportQueueItem prevItem = iqDao.findByQueueOrder(thisOrder);
        if (prevItem != null) {
            prevItem.setQueueOrder(prevOrder);
            iqItem.setQueueOrder(thisOrder);
            iqDao.saveOrUpdate(iqItem);
            iqDao.saveOrUpdate(prevItem);
            // Transient message
            iqHelper.displayTransientMessage(req, MSG_MOVE_UP_SUCCESS, MSG_LEVEL_SUCCESS);
            // Dataset User Log
            String msg = " Changed order from " + prevOrder + " to " + thisOrder + ".";
            iqHelper.logUserAction(userItem, iqItem, UserLogger.IQ_MOVE_UP, msg);
        } else {
            //Don't do anything if this happens, probably first item in list
            logDebug("No previous item found with order: ", thisOrder);
        }
        goToImportQueuePage(req, resp, userItem);
    }

    /**
     * Handle the Move-Item-Down action.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void moveItemDown(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws ServletException, IOException {
        ImportQueueItem iqItem = iqHelper.getImportQueueItemRefresh(req, MSG_MOVE_DOWN_ERROR);
        if (iqItem == null) { return; }
        if (!iqHelper.userHasAccess(req, userItem, iqItem)) { return; }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        Integer nextOrder = iqItem.getQueueOrder();
        Integer thisOrder = iqItem.getQueueOrder() + 1;
        ImportQueueItem nextItem = iqDao.findByQueueOrder(thisOrder);
        if (nextItem != null) {
            nextItem.setQueueOrder(nextOrder);
            iqItem.setQueueOrder(thisOrder);
            iqDao.saveOrUpdate(iqItem);
            iqDao.saveOrUpdate(nextItem);
            // Transient message
            iqHelper.displayTransientMessage(req, MSG_MOVE_DOWN_SUCCESS, MSG_LEVEL_SUCCESS);
            // Dataset User Log
            String msg = " Changed order from " + nextOrder + " to " + thisOrder + ".";
            iqHelper.logUserAction(userItem, iqItem, UserLogger.IQ_MOVE_DOWN, msg);
        } else {
            //Don't do anything if this happens, probably last item in list
            logDebug("No next item found with order: ", thisOrder);
        }
        goToImportQueuePage(req, resp, userItem);
    }

    /**
     * Calculate the cut off date.
     * @param req {@link HttpServletRequest}
     * @param httpSession {@link HttpSession}
     * @param paramName the name of the parameter to get from the form
     * @param attribName the name of the attribute to get from the session
     * @param defaultValue the default value if parameter and attribute are both null
     * @return a date no matter what
     */
    private Date calcCutoffDate(HttpServletRequest req, HttpSession httpSession,
            String paramName, String attribName, String defaultValue) {
        Integer lastDays = null;
        String value = getParameter(req, paramName);
        if (value == null) {
            value = (String)httpSession.getAttribute(attribName);
        }
        lastDays = parseInteger(attribName, value);

        if (lastDays == null) {
            value = defaultValue;
            lastDays = Integer.parseInt(defaultValue);
            logDebug(paramName + " set to default :: ", lastDays);
        } else {
            value = lastDays.toString();
            logDebug(paramName + " :: ", lastDays);
        }

        httpSession.setAttribute(attribName, value);

        Calendar recentCal = Calendar.getInstance();
        recentCal.add(Calendar.DATE, lastDays * -1);
        return recentCal.getTime();
    }

    /**
     * Renames the dataset, if possible, or returns an error message.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void renameDataset(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
            throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;
        String newDatasetName = null;
        ImportQueueItem iqItem = iqHelper.getImportQueueItemJson(req);
        if (iqItem == null) {
            errorFlag = true;
            errorMsg = MSG_RENAME_DATASET_ERROR;
        } else {
            if (!iqHelper.userHasAccess(req, userItem, iqItem)) {
                errorFlag = true;
                errorMsg = MSG_NO_AUTH;
            } else if (iqItem.getStatus().equals(ImportQueueItem.STATUS_LOADING)
                    || iqItem.getStatus().equals(ImportQueueItem.STATUS_GENING)
                    || iqItem.getStatus().equals(ImportQueueItem.STATUS_AGGING)) {
                errorFlag = true;
                errorMsg = MSG_RENAME_DATASET_ERROR_WHILE_IMPORTING;
            } else {
                newDatasetName = getParameter(req, "newDatasetName");
                if (newDatasetName == null) {
                    logger.error("renameDataset: newDatsetName should not be null");
                    errorFlag = true;
                    errorMsg = MSG_RENAME_DATASET_ERROR;
                }
            }
        }

        if (!errorFlag) {
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            // Status (renamed, error, or exists)
            DS_RENAME_STATUS renameStatus = DS_RENAME_STATUS.ERROR;
            // dataset Id (if the dataset was loaded)
            int datasetId = -1;
            // whether or not the dataset exists in ds_dataset
            boolean isLoaded = false;
            // Discourse id, if loaded
            Long discourseId = null;

            // Check constraints
            if (newDatasetName.length() > DatasetItem.DATASET_NAME_MAX_LEN    // name l.t. 100 chars
                || newDatasetName.isEmpty()) {
                writeJSON(resp, json(
                        "msg", "error",
                        "cause", getRenameDatasetResponse(iqItem, renameStatus, newDatasetName)));
                return;
            }

            // Update dataset name if the name doesn't already exist
            List<ImportQueueItem> iqList = iqDao.find(newDatasetName);
            List<DatasetItem> dsList = dsDao.find(newDatasetName);
            if (iqList.isEmpty() && dsList.isEmpty()) {
                String oldIqDatasetName = iqItem.getDatasetName();
                iqItem.setDatasetName(newDatasetName);
                iqItem.setLastUpdatedTime(new Date());
                iqDao.saveOrUpdate(iqItem);

                String info;
                if (iqItem.getDataset() != null) {
                    isLoaded = true;
                    datasetId = (Integer) iqItem.getDataset().getId();
                    DatasetItem dsItem = dsDao.get(datasetId);
                    String oldDatasetName = dsItem.getDatasetName();
                    dsItem.setDatasetName(newDatasetName);
                    dsDao.saveOrUpdate(dsItem);
                    updateDatasetContext(req, dsItem);

                    info = "Dataset: '" + dsItem.getDatasetName()
                            + "' (" + dsItem.getId()
                            + "): Changed name from '" + oldDatasetName + "'.";
                    UserLogger.log(dsItem, userItem, UserLogger.DATASET_RENAME, info, false);
                } else if (iqHelper.isDiscourse(iqItem)) {
                    DiscourseItem discourse = iqHelper.getDiscourseFromIQ(iqItem);
                    if (discourse != null) {
                        // Overloading some vars needed by Javascript...
                        isLoaded = true;
                        datasetId = ((Long)discourse.getId()).intValue();
                        String oldDiscourseName = discourse.getName();
                        discourse.setName(newDatasetName);
                        DiscourseDbDaoFactory.DEFAULT.getDiscourseDao().saveOrUpdate(discourse);

                        info = "Discourse: '" + discourse.getName()
                            + "' (" + discourse.getId()
                            + "): Changed name from '" + oldDiscourseName + "'.";
                        UserLogger.log(userItem, UserLogger.DATASET_RENAME, info, false);
                    }
                } else {
                    info = "IqItem: '" + iqItem.getDatasetName()
                            + "' (" + iqItem.getId()
                            + "): Changed name from '" + oldIqDatasetName + "'.";
                    UserLogger.log(userItem, UserLogger.DATASET_RENAME, info, false);
                }
                renameStatus = DS_RENAME_STATUS.RENAMED;


            } else {
                renameStatus = DS_RENAME_STATUS.EXISTS;
            }
            String lastUpdatedString =
                    ImportQueueDto.getLastUpdateString(iqItem.getLastUpdatedTime());
            writeJSON(resp, json(
                    "msg", "success",
                    "importQueueId", iqItem.getId(),
                    "datasetName", iqItem.getDatasetName(),
                    "status", renameStatus,
                    "isLoaded", isLoaded,
                    "datasetId", datasetId,
                    "lastUpdatedString", lastUpdatedString,
                    "response", getRenameDatasetResponse(iqItem, renameStatus, newDatasetName)));
        } else {
            writeJSON(resp, json(
                    "msg", "error",
                    "cause", errorMsg));
        }
        return;
    }

    /**
     * AJAX response messages for dataset rename.
     * @param iqItem the ImportQueue item
     * @param status the status of the rename action (error, exists, or renamed)
     * @param newDatasetName the desired dataset name
     * @return the response message
     */
    private String getRenameDatasetResponse(ImportQueueItem iqItem,
                                            DS_RENAME_STATUS status, String newDatasetName) {
        String response;
        if (DS_RENAME_STATUS.RENAMED.equals(status)) {
            response = "Dataset renamed.";
        } else if (DS_RENAME_STATUS.EXISTS.equals(status)) {
            response = MSG_DATASET_NAME_TAKEN;
        } else {
            response = MSG_RENAME_DATASET_ERROR;
        }

        // If ImportQueue item is for a Discourse, change references to "dataset".
        if ((iqItem != null) && (iqHelper.isDiscourse(iqItem))) {
            response = response.replace("dataset", "discourse");
            response = response.replace("Dataset", "Discourse");
        }

        return response;
    }

    /**
     * Creates the release dataset dialog.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void createReleaseDialog(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String existingProjectComboBox = null;
        if (req.getParameter("datasetId") != null) {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            Integer datasetId = null;
            if (req.getParameter("datasetId") != null
                    && req.getParameter("datasetId").matches("\\d+")) {
                datasetId = Integer.parseInt(req.getParameter("datasetId"));
            }
            Integer importQueueId = null;
            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem dsItem = null;
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            ImportQueueItem iqItem = null;
            if (datasetId != null) {
                dsItem = dsDao.get(datasetId);
                // Get import queue item if it exists
                importQueueId = dsDao.getImportQueueId(dsItem);
                if (importQueueId != null) {
                    iqItem = iqDao.get(importQueueId);
                }

            }

            if (dsItem != null) {
                // Release dataset to project (iqItem's project has priority)
                if (iqItem != null && iqItem.getProject() != null) {
                    // IQ item has project set so release dataset to import queue item's project
                    ProjectItem projectItem =
                            projectDao.get((Integer) iqItem.getProject().getId());
                    String projectName = projectItem.getProjectName();
                    writeJSON(resp, json(
                            "msg", "success",
                            "datasetId", req.getParameter("datasetId"),
                            "projectName", projectName,
                            "projectId", (Integer) iqItem.getProject().getId()));
                    return;
                } else if (iqItem != null) {
                    // IQ item has no project set so move and release to project
                    existingProjectComboBox = createProjectComboBox(req, resp,
                            userItem, iqItem.getProject());
                    writeJSON(resp, json(
                            "msg", "success",
                            "datasetId", req.getParameter("datasetId"),
                            "importQueueId", iqItem.getId(),
                            "existingProjectComboBox", existingProjectComboBox));
                    return;

                } else if (iqItem == null) {
                    if (dsItem.getProject() == null) {
                        // Move dataset dialog
                        existingProjectComboBox = createProjectComboBox(req, resp,
                                userItem, dsItem.getProject());
                        writeJSON(resp, json(
                                "msg", "success",
                                "datasetId", req.getParameter("datasetId"),
                                "existingProjectComboBox", existingProjectComboBox));
                        return;
                    } else {
                        // release dataset to specified project
                        ProjectItem projectItem =
                                projectDao.get((Integer)dsItem.getProject().getId());
                        writeJSON(resp, json(
                                "msg", "success",
                                "datasetId", req.getParameter("datasetId"),
                                "projectName", projectItem.getProjectName(),
                                "projectId", (Integer) projectItem.getId()));
                        return;
                    }
                }
            }
        } else if (req.getParameter("importQueueId") != null
                && req.getParameter("importQueueId").matches("\\d+")) {
            // IQ item has no project set so move and release to project
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            ImportQueueItem iqItem = iqDao.get(Integer.parseInt(req.getParameter("importQueueId")));
            existingProjectComboBox = createProjectComboBox(req, resp,
                    userItem, iqItem.getProject());
            writeJSON(resp, json(
                    "msg", "success",
                    "importQueueId", req.getParameter("importQueueId"),
                    "existingProjectComboBox", existingProjectComboBox));
            return;
        }

        writeJSON(resp, json(
                "msg", "error",
                "cause", getReleaseDatasetResponse(DS_RELEASE_STATUS.ERROR)));
        return;
    }

    /** Date format for the additional notes field. */
    private static final FastDateFormat NOTES_DATE_FMT = FastDateFormat.getInstance("MM/dd/yyyy");
    /** Date format for the delete dataset confirmation dialog. */
    private static final FastDateFormat HUMAN_DATE_FMT = FastDateFormat.getInstance("MMM dd, yyyy");

    /**
     * Releases the dataset to a project or returns an error message.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void releaseDataset(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem)
            throws IOException, JSONException {
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        // Status (released or error)
        DS_RELEASE_STATUS releaseStatus = DS_RELEASE_STATUS.ERROR;

        Integer datasetId = null;
        // Check to see if parameters exist
        String datasetIdString = req.getParameter("datasetId");
        if (datasetIdString != null && datasetIdString.matches("\\d+")) {
            // Get dataset Id and item
            datasetId = Integer.parseInt(datasetIdString);
            DatasetItem dsItem = dsDao.get(datasetId);
            if (dsItem == null) {
                writeJSON(resp, json(
                        "msg", "success",
                        "datasetId", datasetId,
                        "status", releaseStatus,
                        "datasetId", datasetId,
                        "response", getReleaseDatasetResponse(releaseStatus)));
            }
            if (!iqHelper.userHasAccess(req, userItem, dsItem)) {
                writeJSON(resp, json(
                        "msg", "success",
                        "datasetId", datasetId,
                        "status", releaseStatus,
                        "datasetId", datasetId,
                        "response", MSG_NO_AUTH));
                return;
            }
            // Get import queue item if it exists
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            Integer importQueueId = dsDao.getImportQueueId(dsItem);
            ImportQueueItem iqItem = null;
            boolean isStatusLoaded = true;
            if (importQueueId != null) {
                iqItem = iqDao.get(importQueueId);
                if (iqItem.getStatus() != null
                    && !iqItem.getStatus().equals(ImportQueueItem.STATUS_LOADED)) {
                    isStatusLoaded = false;
                }
            }

            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = null;
            if (iqItem != null && iqItem.getProject() != null) {
                projectItem = projectDao.get((Integer) iqItem.getProject().getId());
            } else {
                projectItem = projectDao.get((Integer) dsItem.getProject().getId());
            }

            if (projectItem != null) {
                // Update dataset project to import queue item's project
                if (iqItem != null && iqItem.getProject() != null) {
                    dsItem.setProject(projectItem);
                    projectItem.setDatasetLastAddedToNow();
                    projectDao.saveOrUpdate(projectItem);
                }
                // Update dataset to released
                if (isStatusLoaded) {
                    dsItem.setReleasedFlag(true);
                    if (iqItem != null) {
                        UserItem uploadedBy = iqItem.getUploadedBy();
                        uploadedBy = DaoFactory.DEFAULT.getUserDao().
                                get((String)uploadedBy.getId());
                        String addlNotes = NOTES_DATE_FMT.format(new Date())
                                + ": Dataset uploaded by "
                                + uploadedBy.getName() + ".\n";
                        if (dsItem.getNotes() != null) {
                            addlNotes += dsItem.getNotes();
                        }
                        dsItem.setNotes(addlNotes);
                    }
                    dsDao.saveOrUpdate(dsItem);
                    updateDatasetContext(req, dsItem);

                    updateMasterInstance(dsItem);

                    projectItem.setNeedsAttention(true);
                    projectDao.saveOrUpdate(projectItem);

                    String info = "Project '" + projectItem.getProjectName() + "' ("
                            + projectItem.getId()
                            + "), Dataset '" + dsItem.getDatasetName() + "' (" + datasetId + ")";

                    UserLogger.log(dsItem, userItem, UserLogger.DATASET_RELEASE, info, false);

                    String logMsg = "User " + userItem.getId() + " released dataset ";
                    logMsg += dsItem.getDatasetName() + " (" + dsItem.getId() + ") to project ";
                    logMsg += projectItem.getProjectName() + " (" + projectItem.getId() + "). ";
                    logMsg += "Needs Attention: Yes.";
                    logger.info(logMsg);

                    releaseStatus = DS_RELEASE_STATUS.RELEASED;
                    writeJSON(resp, json(
                            "msg", "success",
                            "datasetId", datasetId,
                            "status", releaseStatus,
                            "projectName", projectItem.getProjectName(),
                            "response", getReleaseDatasetResponse(releaseStatus)));
                    return;
                }
            }

            writeJSON(resp, json(
                    "msg", "success",
                    "datasetId", datasetId,
                    "status", releaseStatus,
                    "response", getReleaseDatasetResponse(releaseStatus)));
            return;
        }
        writeJSON(resp, json(
                "msg", "error",
                "cause", getReleaseDatasetResponse(releaseStatus)));
        return;
    }

    /**
     * Helper method to push dataset change out to master DataShop instance.
     * @param dataset the DatasetItem
     */
    private void updateMasterInstance(DatasetItem dataset) {

        // If a slave, update master DataShop instance with dataset info.
        if (DataShopInstance.isSlave()) {
            try {
                Integer datasetId = (Integer)dataset.getId();
                DatasetDTO datasetDto =
                    HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                DatasetCreator.INSTANCE.setDataset(datasetDto);
            } catch (Exception e) {
                // Failed to push Dataset info to master. Ignore?
                logDebug("Failed to push dataset info to master for dataset '"
                         + dataset.getDatasetName() + "': " + e);
            }
        }
    }

    /**
     * Helper method to push discourse change out to master DataShop instance.
     * @param dataset the DiscourseItem
     */
    private void updateMasterInstance(DiscourseItem discourse) {

        // If a slave, update master DataShop instance with discourse info.
        if (DataShopInstance.isSlave()) {
            try {
                Long discourseId = (Long)discourse.getId();
                DiscourseDTO discourseDto =
                    HelperFactory.DEFAULT.getWebServiceHelper().discourseDTOForId(discourseId);
                DiscourseCreator.INSTANCE.setDiscourse(discourseDto);
            } catch (Exception e) {
                // Failed to push Dataset info to master. Ignore?
                logDebug("Failed to push discourse info to master for discourse '"
                         + discourse.getName() + "': " + e);
            }
        }
    }

    /**
     * AJAX response messages for dataset release.
     * @param status the status of the release action (error or released)
     * @return the response message
     */
    private String getReleaseDatasetResponse(DS_RELEASE_STATUS status) {
        String response;
        if (status.equals(DS_RELEASE_STATUS.RELEASED)) {
            response = "Dataset released to project!";
        } else {
            response = MSG_RELEASE_DATASET_ERROR;
        }
        return response;
    }

    /**
     * AJAX response messages for dataset move.
     * @param status the status of the move action (error or moved)
     * @return the response message
     */
    private String getMoveDatasetResponse(DS_MOVE_STATUS status) {
        String response;
        if (status.equals(DS_MOVE_STATUS.MOVED)) {
            response = "Dataset moved to project.";
        } else if (status.equals(DS_MOVE_STATUS.EXISTS)) {
            response = MSG_PROJECT_NAME_TAKEN;
        } else {
            response = MSG_MOVE_DATASET_ERROR;
        }
        return response;
    }

    /**
     * Creates the move dataset dialog.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void createMoveDialog(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String existingProjectComboBox = null;
        if (req.getParameter("importQueueId") != null
                && req.getParameter("importQueueId").matches("\\d+")) {
            int importQueueId = Integer.parseInt(req.getParameter("importQueueId"));
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            ImportQueueItem iqItem = iqDao.get(importQueueId);
            if (iqItem != null) {
                existingProjectComboBox = createProjectComboBox(req, resp,
                        userItem, iqItem.getProject());
                // Move dataset to project
                writeJSON(resp, json(
                        "msg", "success",
                        "importQueueId", req.getParameter("importQueueId"),
                        "datasetId", req.getParameter("datasetId"),
                        "existingProjectComboBox", existingProjectComboBox)
                        );
            }
        } else {
            writeJSON(resp, json(
                "msg", "error",
                "cause", getMoveDatasetResponse(DS_MOVE_STATUS.ERROR)));
        }
    }

    /**
     * Creates a combo box for projects owned by the user
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @param existingProject the existing project item for the import queue item, if one exists
     * @return a combo box for projects owned by the user
     */
    private String createProjectComboBox(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem,
            ProjectItem existingProject) {
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        // Get all datasets if DS Admin or a subset, otherwise
        List<ProjectItem> projectList = userItem.getAdminFlag()
                ? projectDao.findAllSortByName() // is DS Admin
                : authDao.findProjectsByAdmin(userItem); // is Project Admin
        StringBuffer sbuffer = new StringBuffer(
                "<select id=\"projectComboBox\" size=\""
                        + DEFAULT_PROJECT_COMBO_BOX_SIZE + "\">");

        if (userItem != null) {
            if (!projectList.isEmpty()) {
                if (existingProject != null) {
                    existingProject = projectDao.get((Integer) existingProject.getId());
                    projectList.remove(existingProject);
                }
            }

            for (ProjectItem projectItem : projectList) {
                sbuffer.append("<option class=\"projectOption\" name=\""
                        + projectItem.getProjectName() + "\" value=\""
                        + projectItem.getId() + "\">"
                        + projectItem.getProjectName() + "</option>");
            }

            // Store the permissions in a HashMap for use in method: getProjectMetaData
            List<ExistingProjectDto> existingProjectList = projectDao
                    .getExistingProjects(userItem);
            Map<Integer, String> permissionMap = new HashMap<Integer, String>();
            for (ExistingProjectDto dto : existingProjectList) {
                permissionMap.put(dto.getProjectId(), dto.getPermissions());
            }
            if (!permissionMap.isEmpty() && existingProject != null) {
                permissionMap.remove(existingProject.getId());
            }
            if (!permissionMap.isEmpty()) {
                HttpSession httpSession = req.getSession(true);
                httpSession.setAttribute("ProjectPermissions", permissionMap);
            }

            sbuffer.append("</select>");
        }

        if (projectList == null || projectList.isEmpty()) {
            return null;
        } else {
            return sbuffer.toString();
        }
    }

    /**
     * Moves the dataset and returns an AJAX response.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void moveDataset(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws JSONException, IOException {
        boolean errorFlag = false;
        String errorMsg = null;
        // Dataset Id is always passed in
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        // Import Queue Id is also passed in
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqHelper.getImportQueueItemJson(req);
        if (iqItem == null) {
            errorFlag = true;
            errorMsg = MSG_MOVE_DATASET_ERROR;
        } else if (!iqHelper.userHasAccess(req, userItem, iqItem)) {
            errorFlag = true;
            errorMsg = MSG_NO_AUTH;
        } else if (iqItem.getStatus().equals(ImportQueueItem.STATUS_LOADING)
                || iqItem.getStatus().equals(ImportQueueItem.STATUS_GENING)
                || iqItem.getStatus().equals(ImportQueueItem.STATUS_AGGING)) {
            errorFlag = true;
            errorMsg = MSG_MOVE_DATASET_ERROR_WHILE_IMPORTING;
        }

        if (!errorFlag) {
            // Until things valid, initialize to the ERROR state
            DS_MOVE_STATUS moveStatus = DS_MOVE_STATUS.ERROR;
            // Project
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = null;
            Integer projectId = null;
            ProjectItem lastProjectItem = null;
            // Dataset
            DatasetItem dsItem = iqItem.getDataset();
            if (dsItem != null) {
                dsItem = dsDao.get((Integer)dsItem.getId());
            }

            // Released flag
            Boolean releasedFlag = null;
            try {
                if (req.getParameter("releasedFlag") != null) {
                    releasedFlag = Boolean.parseBoolean(req.getParameter("releasedFlag"));
                }
            } catch (Exception e) { //TBD which exception is expected?
                logger.error("releasedFlag exception", e);
                releasedFlag = null;
            }

            // Get the last project item for logging purposes
            if (iqItem != null && iqItem.getProject() != null) {
                lastProjectItem = projectDao.get((Integer) iqItem.getProject().getId());
            } else if (dsItem != null && dsItem.getProject() != null) {
                lastProjectItem = projectDao.get((Integer) dsItem.getProject().getId());
            }

            String projectName = getParameter(req, "newProjectName");
            // Create new project if name is unique
            if (projectName != null
                && !projectName.isEmpty()
                && req.getParameter("newOrExisting") != null
                && req.getParameter("newOrExisting").equals(DS_MOVE_ATTRIB.NEW.toString())) {
                // Check if project name is unique first
                if (CreateProjectServlet.isProjectNameUnique(projectName)) {
                    // The project name has not been taken so create it
                    projectItem = CreateProjectServlet.createProject(userItem,
                            projectName, ProjectItem.DATA_COLLECTION_TYPE_NOT_SPECIFIED);
                    if (projectItem != null) {
                        projectId = (Integer) projectItem.getId();
                    }
                } else {
                    // Project name already exists
                    moveStatus = DS_MOVE_STATUS.EXISTS;
                    writeJSON(resp, json(
                            "msg", "success",
                            "status", moveStatus,
                            "importQueueId", req.getParameter("importQueueId"),
                            "datasetId", req.getParameter("datasetId"),
                            "response", getMoveDatasetResponse(moveStatus)));
                    return;
                }
                // Use existing project
            } else if (req.getParameter("projectId") != null
                    && req.getParameter("projectId").matches("\\d+")
                    && req.getParameter("newOrExisting") != null
                    && req.getParameter("newOrExisting")
                    .equals(DS_MOVE_ATTRIB.EXISTING.toString())) {

                projectId = Integer.parseInt(req.getParameter("projectId"));
                projectItem = projectDao.get(projectId);
                // Make sure the new project is different than the previous
                if (projectItem != null && lastProjectItem != null
                        && projectItem.getId().equals(lastProjectItem.getId())) {
                    moveStatus = DS_MOVE_STATUS.ERROR;
                    projectItem = null;
                }
            // Unable to create new project or use existing
            } else {
                writeJSON(resp, json(
                        "msg", "success",
                        "status", moveStatus,
                        "importQueueId", req.getParameter("importQueueId"),
                        "datasetId", req.getParameter("datasetId"),
                        "response", getMoveDatasetResponse(moveStatus)));
                return;
            }
            // If project was created or found, then move dataset
            if (projectItem != null) {
                String info;
                // Change project referred in import queue table
                if (iqItem != null) {
                    iqItem.setProject(projectItem);
                    iqItem.setLastUpdatedTime(new Date());
                    iqDao.saveOrUpdate(iqItem);
                    moveStatus = DS_MOVE_STATUS.MOVED;
                    if (dsItem == null) {
                        if (lastProjectItem != null) {
                            info = "IqItem '" + iqItem.getDatasetName()
                                    + "' (" + iqItem.getId() + "): Changed project from '"
                                    + lastProjectItem.getProjectName() + "' ("
                                    + lastProjectItem.getId()
                                    + ") to '" + projectItem.getProjectName() + "' ("
                                    + projectItem.getId()
                                    + ")";
                        } else {
                            info = "IqItem '" + iqItem.getDatasetName()
                                    + "' (" + iqItem.getId() + "): Changed project to '"
                                    + projectItem.getProjectName() + "' ("
                                    + projectItem.getId()
                                    + ")";
                        }
                        UserLogger.log(userItem, UserLogger.DATASET_MOVE, info, false);
                    }
                }

                if (dsItem != null) {
                    boolean isStatusLoaded = true;
                    if (iqItem != null
                            && iqItem.getStatus() != null
                            && !iqItem.getStatus().equals(ImportQueueItem.STATUS_LOADED)) {
                        isStatusLoaded = false;
                    }
                    // Change project referred in dataset table
                    dsItem.setProject(projectItem);
                    moveStatus = DS_MOVE_STATUS.MOVED;
                    if (releasedFlag != null && releasedFlag && isStatusLoaded) {
                        dsItem.setReleasedFlag(releasedFlag);
                    }

                    dsDao.saveOrUpdate(dsItem);
                    updateDatasetContext(req, dsItem);
                    if (lastProjectItem != null) {
                        info = "Dataset '" + dsItem.getDatasetName()
                                + "' (" + dsItem.getId() + "): Changed project from '"
                                + lastProjectItem.getProjectName() + "' ("
                                + lastProjectItem.getId()
                                + ") to '" + projectItem.getProjectName() + "' ("
                                + projectItem.getId()
                                + ")";
                    } else {
                        info = "Dataset '" + dsItem.getDatasetName()
                                + "' (" + dsItem.getId() + "): Changed project to '"
                                + projectItem.getProjectName() + "' ("
                                + projectItem.getId()
                                + ")";
                    }

                    projectItem.setNeedsAttention(true);
                    projectItem.setDatasetLastAddedToNow();
                    DaoFactory.DEFAULT.getProjectDao().saveOrUpdate(projectItem);

                    UserLogger.log(dsItem, userItem, UserLogger.DATASET_MOVE, info, false);

                    String logMsg = "User " + userItem.getId() + " changed project for dataset ";
                    logMsg += dsItem.getDatasetName() + " (" + dsItem.getId() + ") to project ";
                    logMsg += projectItem.getProjectName() + " (" + projectItem.getId() + "). ";
                    logMsg += "Needs Attention: Yes.";
                    logger.info(logMsg);
                }
            }
            // AJAX response
            if (moveStatus.equals(DS_MOVE_STATUS.MOVED)) {
                String lastUpdatedString =
                    ImportQueueDto.getLastUpdateString(iqItem.getLastUpdatedTime());
                writeJSON(resp, json(
                        "msg", "success",
                        "status", moveStatus,
                        "importQueueId", req.getParameter("importQueueId"),
                        "projectId", projectId,
                        "projectName", projectItem.getProjectName(),
                        "datasetId", req.getParameter("datasetId"),
                        "lastUpdatedString", lastUpdatedString,
                        "response", getMoveDatasetResponse(moveStatus)));
                return;
            } else {
                writeJSON(resp, json(
                        "msg", "success",
                        "status", moveStatus,
                        "importQueueId", req.getParameter("importQueueId"),
                        "projectId", projectId,
                        "datasetId", req.getParameter("datasetId"),
                        "response", getMoveDatasetResponse(moveStatus)));
                return;
            }
        }

        // Default error
        writeJSON(resp, json(
            "msg", "error",
            "cause", errorMsg));
        return;
    }

    /**
     * Update the DatasetItem held in the session.
     * @param req {@link HttpServletRequest}
     * @param dataset the new DatasetItem
     */
    private void updateDatasetContext(HttpServletRequest req, DatasetItem dataset) {
        DatasetContext dsContext = getDatasetContext(req);
        if (dsContext != null) {
            dsContext.setDataset(dataset);
            setInfo(req, dsContext);
        }
    }

    /**
     * Gets the project meta data to provide an AJAX response.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void getProjectMetaData(HttpServletRequest req, HttpServletResponse resp)
        throws JSONException, IOException {
        if (req.getParameter("projectId") != null
                && req.getParameter("projectId").matches("\\d+")) {
            String permissions = "";
            Integer projectId = null;
            projectId = Integer.parseInt(req.getParameter("projectId"));
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = projectDao.get(projectId);
            if (projectItem != null) {
                // Get permissions for this project
                Map<Integer, String> permissionMap = null;
                HttpSession httpSession = req.getSession(true);
                if (httpSession.getAttribute("ProjectPermissions") != null) {
                    permissionMap = (Map<Integer, String>)
                            httpSession.getAttribute("ProjectPermissions");
                    // Get permissions if they were created correctly during combo box creation
                    if (!permissionMap.isEmpty()
                            && permissionMap.containsKey(projectItem.getId())) {
                        permissions = permissionMap.get(projectItem.getId());
                    }
                }
                // Set PI and DP names if they exist
                String piName = null;
                String dpName = null;
                if (projectItem.getPrimaryInvestigator() != null) {
                    piName = projectItem.getPrimaryInvestigator().getName();
                } else {
                    piName = "";
                }
                if (projectItem.getDataProvider() != null) {
                    dpName = projectItem.getDataProvider().getName();
                } else {
                    dpName = "";
                }
                // Write AJAX response
                writeJSON(resp, json(
                        "msg", "success",
                        "projectId", projectId,
                        "projectName", projectItem.getProjectName(),
                        "piName", piName,
                        "dpName", dpName,
                        "permissions", permissions
                        ));
                    return;
            }
        }
    }

    /**
     * Sort the 'Recently Loaded' table on the ImportQueue page.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void sortRecentlyLoaded(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {

        String sortByParam = getParameter(req, "sortBy");
        if (sortByParam != null) {
            ImportQueueContext context = ImportQueueContext.getContext(req);
            context.setLoadedSortByColumn(sortByParam, true);
            ImportQueueContext.setContext(req, context);
        } else {
            logger.warn("ImportQueue: The 'recently loaded' sort parameter is unexpectedly null.");
        }

        goToImportQueuePage(req, resp, userItem);
    }

    /**
     * Sort the 'Recently Created... No Data' table on the ImportQueue page.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void sortNoData(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {

        String sortByParam = getParameter(req, "sortBy");
        if (sortByParam != null) {
            ImportQueueContext context = ImportQueueContext.getContext(req);
            context.setNoDataSortByColumn(sortByParam, true);
            ImportQueueContext.setContext(req, context);
        } else {
            logger.warn("ImportQueue: The 'no data' sort parameter is unexpectedly null.");
        }

        goToImportQueuePage(req, resp, userItem);
    }

    private final static String ACCESS_LIST_TABLE = "accessListTable";
    private final static String ADDITIONAL_INFO_TABLE = "additionalInfoTable";
    private final static int DS_INFO_COLSPAN = 3;
    /**
     * Creates the delete dataset dialog.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user item
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void createDeleteDialog(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem)
            throws IOException, JSONException {
        if (req.getParameter("importQueueId") != null
                && req.getParameter("importQueueId").matches("\\d+")) {
            int importQueueId = Integer.parseInt(req.getParameter("importQueueId"));
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            ImportQueueItem iqItem = iqDao.get(importQueueId);
            if (iqItem != null) {

                boolean isAuthorizedForDelete = false;
                // Dataset
                DatasetItem dsItem = null;
                DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                if (iqItem.getDataset() != null) {
                    dsItem = dsDao.get((Integer) iqItem.getDataset().getId());
                }
                // Check if this user authorized to delete this dataset
                UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                UserItem uploadedBy = iqItem.getUploadedBy() != null
                    ? userDao.get((String)iqItem.getUploadedBy().getId()) : null;
                if (userItem.getAdminFlag()
                        || (uploadedBy != null && uploadedBy.equals(userItem))) {
                    isAuthorizedForDelete = true;
                }

                if (dsItem != null && isAuthorizedForDelete) {
                    DecimalFormat commaDf = new DecimalFormat("#,###,###");
                    // Whether or not users (other than oneself) accessed the dataset.
                    Boolean foundUsers = false;
                    // Allow the user to delete the dataset, but if it has been accessed
                    // by a regular user, then prompt the owner to ensure they
                    // want to delete it.
                    if (iqItem.getStatus() != null
                            && (iqItem.getStatus().equals(ImportQueueItem.STATUS_LOADED)
                                    || iqItem.getStatus().equals(ImportQueueItem.STATUS_NO_DATA))) {
                        // The dataset has been created so the user cannot
                        // delete the dataset without being prompted.
                        // Prompt the user if they are a project admin or ds
                        // admin, asking if they wish to delete the dataset
                        // Return a list of users who have accessed the dataset
                        // and their last access time only if the access_flag is true
                        // for the dataset.
                        StringBuffer accessListTable = new StringBuffer();
                        // Create a table to hold additional info useful for DS Admins.
                        StringBuffer additionalInfo = new StringBuffer();

                        if (dsItem.getAccessedFlag()
                                || userItem.getAdminFlag()) {
                            DatasetUserLogDao dulDao = DaoFactory.DEFAULT
                                    .getDatasetUserLogDao();
                            List<Object> accessTimesByDataset = dulDao
                                    .getAccessTimes(dsItem);

                            final int mapUserId = 0;
                            final int mapDatasetId = 1;
                            final int mapLastAccessId = 2;
                            final int isDatashopAdminId = 3;

                            StringBuffer accessList = new StringBuffer();

                            Pattern pattern = Pattern
                                    .compile(SamplesHelper.EMAIL_PATTERN);
                            for (Iterator<Object> iter = accessTimesByDataset
                                    .iterator(); iter.hasNext();) {
                                Object[] obj = (Object[]) iter.next();
                                // Set last access time
                                if (obj[mapLastAccessId] != null
                                        && obj[mapUserId] != null) {
                                    UserItem accessedByUser = (UserItem) (obj[mapUserId]);

                                    // Do not include the current user in the list.
                                    if (!accessedByUser.equals(userItem)) {
                                        String userInfo = null;
                                        String nameAndId = SamplesHelper
                                                .getFormattedUserName(accessedByUser, true);
                                        foundUsers = true;
                                        if (nameAndId == null) {
                                            nameAndId = new String();
                                        }

                                        // If email matches pattern, then display the
                                        // username with mail-to link, instead.
                                        if (accessedByUser != null
                                                && accessedByUser.getEmail() != null) {
                                            Matcher matcher = pattern
                                                    .matcher(accessedByUser.getEmail());
                                            if (matcher.matches()) {
                                                userInfo = "<a href=\"mailto:"
                                                    + accessedByUser.getEmail()
                                                    + "\" >"
                                                    + SamplesHelper.getFormattedUserName(
                                                        accessedByUser, true) + "</a>";

                                            }
                                        } else if (accessedByUser != null) {
                                            userInfo = nameAndId;
                                        }

                                        // If users have accessed this dataset, show their access times
                                        // and identify DataShopAdmins as simply "DataShop Admin"
                                        Boolean isDatashopAdmin = (Boolean)(obj[isDatashopAdminId]);
                                        Date lastAccessDate = (Date) (obj[mapLastAccessId]);

                                        if (userInfo != null) {
                                            accessList.append("<tr><td>");
                                            accessList.append(HUMAN_DATE_FMT.format(lastAccessDate));
                                            accessList.append("</td><td>");

                                            if (isDatashopAdmin) {
                                                accessList.append("DataShop Admin");
                                            } else {
                                                accessList.append(userInfo);
                                            }
                                            accessList.append("</td></tr>");
                                        }
                                    }
                                }
                            }


                            if (userItem.getAdminFlag()) {
                                additionalInfo.append("<table id=\"" + ADDITIONAL_INFO_TABLE+ "\">");
                                // Helper and DAO to get dataset info
                                SampleMetricDao metricDao =
                                        DaoFactory.DEFAULT.getSampleMetricDao();
                                FilesInfoHelper filesInfoHelper =
                                        HelperFactory.DEFAULT.getFilesInfoHelper();

                                additionalInfo.append("<tr><td>");
                                // Whether it is marked as junk

                                if (dsItem.getJunkFlag() != null && dsItem.getJunkFlag()) {
                                    additionalInfo.append("<img src=\"images/tick.png\" />");
                                } else {
                                    additionalInfo.append("<img src=\"images/trans_spacer.gif\" />");
                                }
                                additionalInfo.append(" Junk</td><td>");
                                // Whether it has been released

                                if (dsItem.getReleasedFlag() != null && dsItem.getReleasedFlag()) {
                                    additionalInfo.append("<img src=\"images/tick.png\" />");
                                } else {
                                    additionalInfo.append("<img src=\"images/trans_spacer.gif\" />");
                                }
                                additionalInfo.append(" Released</td><td>");

                                // Whether it has already been marked for deletion

                                if (dsItem.getDeletedFlag() != null && dsItem.getDeletedFlag()) {
                                    additionalInfo.append("<img src=\"images/tick.png\" />");
                                } else {
                                    additionalInfo.append("<img src=\"images/trans_spacer.gif\" />");
                                }
                                additionalInfo.append(" Marked for Deletion</td><td>");
                                additionalInfo.append("</td></tr>");
                                additionalInfo.append("<tr><td colspan=\"" + DS_INFO_COLSPAN + "\">Uploaded by ");
                                // Who created it (tool, user)
                                String nameAndId = SamplesHelper
                                        .getFormattedUserName(uploadedBy, true);
                                if (uploadedBy != null
                                        && uploadedBy.getEmail() != null) {
                                    Matcher matcher = pattern
                                            .matcher(uploadedBy.getEmail());
                                    if (matcher.matches()) {
                                        additionalInfo.append("<a href=\"mailto:"
                                            + uploadedBy.getEmail() + "\" >"
                                            + SamplesHelper.getFormattedUserName(
                                                    uploadedBy, true) + "</a>");

                                    }
                                } else if (uploadedBy != null) {
                                    additionalInfo.append(nameAndId);
                                }
                                // When it was created
                                if (iqItem.getUploadedTime() != null) {
                                    additionalInfo.append(" on "
                                        + HUMAN_DATE_FMT.format(iqItem.getUploadedTime()));
                                }
                                additionalInfo.append("</td></tr>");
                                additionalInfo.append("<tr><td colspan=\"" + DS_INFO_COLSPAN + "\">Domain/Learnlab: ");
                                // Domain/Learnlab:

                                if (dsItem.getDomain() != null) {
                                    Integer domainId = (Integer)(dsItem.getDomain().getId());
                                    String domainName = DaoFactory.DEFAULT.getDomainDao()
                                            .get(domainId).getName();
                                    additionalInfo.append(domainName);
                                } else {
                                    additionalInfo.append("n/a");
                                }
                                if (dsItem.getLearnlab() != null) {
                                    Integer learnlabId = (Integer)(dsItem.getLearnlab().getId());
                                    String learnlabName = DaoFactory.DEFAULT.getLearnlabDao()
                                        .get(learnlabId).getName();
                                    additionalInfo.append(", " + learnlabName);
                                } else {
                                    additionalInfo.append(", n/a");
                                }
                                additionalInfo.append("</td></tr>");
                                additionalInfo.append("<tr><td colspan=\"" + DS_INFO_COLSPAN + "\">Transactions: ");
                                // Number of transactions
                                Long totalTransactions = metricDao.getTotalTransactions(dsItem);
                                if (totalTransactions != null) {
                                    additionalInfo.append(commaDf.format(totalTransactions));
                                } else {
                                    additionalInfo.append(0);
                                }
                                additionalInfo.append("</td></tr><tr><td>");

                                // Number of attachments
                                additionalInfo.append("Files ("
                                        + filesInfoHelper.getFileList(dsItem).size());
                                additionalInfo.append(") </td><td>Papers ("
                                        + filesInfoHelper.getPaperList(dsItem).size());
                                additionalInfo.append(") </td><td>Analyses ("
                                        + filesInfoHelper.getExternalAnalysisList(dsItem).size());
                                additionalInfo.append(")");
                                additionalInfo.append("</td></tr>");
                                if (foundUsers) {
                                additionalInfo.append("<tr><td colspan=\"" + DS_INFO_COLSPAN
                                    + "\">Scroll down to see users and access times</td></tr>");
                                }
                                additionalInfo.append("<table>");


                            }

                            // DataShop admins see additional info.
                            // Everyone else sees only the users who have accessed the dataset
                            // if the `accessed_flag` of ds_dataset is true.
                            if (dsItem.getAccessedFlag()) {
                                accessListTable.append("<table id=\"" + ACCESS_LIST_TABLE + "\">");
                                accessListTable.append(accessList.toString());
                                accessListTable.append("</table>");
                            }

                        }
                        writeJSON(
                                resp,
                                json("msg", "requestForInfo",
                                        "importQueueId", importQueueId,
                                        "datasetName", iqItem.getDatasetName(),
                                        "accessListTable", accessListTable,
                                        "additionalInfo", additionalInfo.toString()));
                        return;

                    }

                    writeJSON(
                            resp,
                            json("msg", "success", "importQueueId",
                                    req.getParameter("importQueueId"),
                                    "datasetName", iqItem.getDatasetName()));
                    return;
                } else if (isAuthorizedForDelete) {
                    // If iqItem.dataset is null, might be a Discourse...
                    DiscourseItem discourse = iqHelper.getDiscourseFromIQ(iqItem);
                    if (discourse != null) {
                        writeJSON(resp,
                                  json("msg", "success", "importQueueId",
                                       req.getParameter("importQueueId"),
                                       "datasetName", discourse.getName()));
                    }
                }

            }
        } else {
            writeJSON(
                    resp,
                    json("msg", "error", "cause",
                            getDeleteDatasetResponse(DS_DELETE_STATUS.ERROR)));
            return;
        }
    }

    /**
     * Removes the item from import queue and marks its dataset as deleted.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void deleteDataset(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        DS_DELETE_STATUS deleteStatus = DS_DELETE_STATUS.ERROR;
        ImportQueueItem iqItem = iqHelper.getImportQueueItemJson(req);
        if (iqItem == null) {
            writeJSON(resp, json(
                    "msg", "error",
                    "status", deleteStatus,
                    "cause", MSG_DELETE_DATASET_ERROR));
        } else {
            Integer idToDelete = null;
            boolean isAuthorizedForDelete = false;
            // Dataset
            DatasetItem dsItem = null;
            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            if (iqItem.getDataset() != null) {
                idToDelete = (Integer) iqItem.getDataset().getId();
                dsItem = dsDao.get(idToDelete);
            }
            // Check if this user authorized to delete this dataset
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem uploadedBy = userDao.get((String)iqItem.getUploadedBy().getId());

            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
            ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
            if (userItem.getAdminFlag()
                    || (uploadedBy != null && uploadedBy.equals(userItem))) {
                isAuthorizedForDelete = true;
            }

            DiscourseItem discourse = null;

            if (dsItem == null && isAuthorizedForDelete) {
                // Log delete action
                String info = "IqItem: '" + iqItem.getDatasetName()
                        + "' (" + iqItem.getId() + ")";
                UserLogger.log(userItem, UserLogger.DATASET_DELETE, info, false);
                FileItem fileItem = iqItem.getFile();

                // If Discourse, mark as 'deleted' for offline deletion.
                Boolean isDiscourse = iqHelper.isDiscourse(iqItem);
                if (isDiscourse) {
                    discourse = iqHelper.getDiscourseFromIQ(iqItem);
                    discourse.setDeletedFlag(true);
                    DiscourseDbDaoFactory.DEFAULT.getDiscourseDao().saveOrUpdate(discourse);
                }

                // Delete import queue row
                iqDao.delete(iqItem);
                deleteStatus = DS_DELETE_STATUS.DELETED;

                if (fileItem != null) {
                    fileItem = fileDao.get((Integer) fileItem.getId());
                    String fullFileName = fileItem.getFullFileName(getBaseDir());
                    if (!removeImportQueueFile(fileItem, getBaseDir())) {
                        logger.error("Error occurred while attempting to remove"
                                     + " import queue file for " + info);
                    }
                }

            } else if (dsItem != null && isAuthorizedForDelete) {
                idToDelete = (Integer)dsItem.getId();
                boolean isProjectAdmin = iqHelper.userHasAccess(req, userItem, dsItem.getProject());

                // Only delete actual datasets if the DataShop administrator
                // or the Project administrator (who uploaded the dataset originally)
                // requests the deletion.
                if ((isProjectAdmin || userItem.getAdminFlag())
                        && iqItem.getStatus() != null
                        && (iqItem.getStatus().equals(ImportQueueItem.STATUS_LOADED)
                                || iqItem.getStatus().equals(ImportQueueItem.STATUS_NO_DATA))) {
                    // Log delete action before deleting because we need the dataset item
                    String info = "Dataset: '" + dsItem.getDatasetName()
                            + "' (" + dsItem.getId() + ")";
                    UserLogger.log(userItem, UserLogger.DATASET_DELETE, info, false);
                    FileItem fileItem = iqItem.getFile();

                    // Delete dataset item
                    // Delete operations
                    iqDao.delete(iqItem);
                    dsItem.setDeletedFlag(true);

                    if (fileItem != null) {
                        fileItem = fileDao.get((Integer) fileItem.getId());
                        String fullFileName = fileItem.getFullFileName(getBaseDir());
                        if (removeImportQueueFile(fileItem, getBaseDir())) {
                            SystemLogger.log(dsItem, "Delete import queue file",
                                             "Successfully deleted import queue file: "
                                             + fullFileName);
                        } else {
                            logger.error("Error occurred while attempting to remove"
                                         + " import queue file for " + info);
                        }
                    }
                    // Remove reference to project.
                    dsItem.setProject(null);

                    // Remove files associated with dataset
                    TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
                    Long numTransactions = txDao.count(dsItem);
                    if (numTransactions == null
                            || numTransactions < getNumTransactionsImmediateDelete()) {
                        String cfgDir = getBaseDir();
                        List<String> filePaths = fileDao.getDistinctFilePaths(dsItem);
                        // Delete the directories associated with files and papers for this dataset
                        for (String filePath : filePaths) {
                            // Ensure that the file path is not the base directory
                            // before deleting the directory recursively
                            if (cfgDir != null && !cfgDir.isEmpty()) {
                                File fileToDelete = new File(cfgDir + File.separator + filePath);
                                File baseDir = new File(cfgDir);
                                if (fileToDelete != null && baseDir != null
                                        && !fileToDelete.equals(baseDir)) {
                                    fileToDelete.delete();
                                    SystemLogger.log(dsItem, "Delete dataset file",
                                        "Successfully deleted dataset file: " + filePath);
                                }
                            }
                        }
                        // Next, delete any cached files for the dataset
                        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
                        List<SampleItem> sampleItems = sampleDao.find(dsItem);
                        TransactionExportHelper txExportHelper =
                                HelperFactory.DEFAULT.getTransactionExportHelper();
                        StepRollupExportHelper stepExportHelper =
                                HelperFactory.DEFAULT.getStepRollupExportHelper();
                        StudentProblemExportHelper studentProblemExportHelper =
                                HelperFactory.DEFAULT.getStudentProblemExportHelper();
                        for (SampleItem sampleItem : sampleItems) {
                            // Delete tx export
                            String cachedTxFileName =
                                    txExportHelper.getCachedFileName(sampleItem, cfgDir);
                            if (cachedTxFileName != null) {
                                if (txExportHelper.deleteFile(cachedTxFileName)) {
                                   SystemLogger.log(dsItem, "Delete cached tx export",
                                           "Successfully deleted cached tx export file '"
                                    + cachedTxFileName + "'.");
                                } else {
                                    logger.error("Error occurred while attempting to remove"
                                            + " cached transaction export "
                                            + " for '" + sampleItem.getSampleName()
                                            + "' (" + sampleItem.getId() + ").");
                                }
                            }
                            // Delete student step export
                            String cachedStepFileName =
                                    stepExportHelper.getCachedFileName(sampleItem, cfgDir);
                            if (cachedStepFileName != null) {
                                if (stepExportHelper.deleteFile(cachedStepFileName)) {
                                    SystemLogger.log(dsItem, "Delete cached step export",
                                            "Successfully deleted cached step export file '"
                                     + cachedStepFileName + "'.");
                                 } else {
                                     logger.error("Error occurred while attempting to remove"
                                             + " cached step export "
                                             + " for '" + sampleItem.getSampleName()
                                             + "' (" + sampleItem.getId() + ").");
                                 }
                            }
                            // Delete student problem export
                            String cachedSProblemFileName = studentProblemExportHelper
                                .getCachedFileName(sampleItem, cfgDir);
                            if (cachedSProblemFileName != null) {
                                if (studentProblemExportHelper.deleteFile(cachedSProblemFileName)) {
                                    SystemLogger.log(dsItem, "Delete cached student problem export",
                                        "Successfully deleted cached student problem export file '"
                                     + cachedSProblemFileName + "'.");
                                 } else {
                                     logger.error("Error occurred while attempting to remove"
                                             + " cached student problem export "
                                             + " for '" + sampleItem.getSampleName()
                                             + "' (" + sampleItem.getId() + ").");
                                 }
                            }
                        }
                        // delete dataset immediately
                        txDao.deleteByDataset(dsItem);
                        dsDao.delete(dsItem);
                        deleteStatus = DS_DELETE_STATUS.DELETED;

                    } else {
                        // Create a unique dataset name using the generate GUID method
                        String uuid = generateGuid(dsItem.getDatasetName());
                        dsItem.setDatasetName(uuid);

                        dsDao.saveOrUpdate(dsItem);
                        deleteStatus = DS_DELETE_STATUS.DELETED;
                    }
                } else {
                    writeJSON(resp, json(
                        "msg", "error",
                        "cause", "Only the original uploader "
                        + "can delete this dataset, provided they are a Project Administrator."));
                }
            }

            // Delete remote place-holder instance if this is a slave.
            if (DataShopInstance.isSlave() && (deleteStatus == DS_DELETE_STATUS.DELETED)) {
                if (discourse != null) {
                    Long discourseId = (Long)discourse.getId();
                    if (DiscourseCreator.INSTANCE.deleteDiscourse(discourseId) == null) {
                        deleteStatus = DS_DELETE_STATUS.WARNING;
                    }
                } else {
                    if (DatasetCreator.INSTANCE.deleteDataset(idToDelete) == null) {
                        deleteStatus = DS_DELETE_STATUS.WARNING;
                    }
                }
            }

            writeJSON(resp, json(
                    "msg", "success",
                    "importQueueId", req.getParameter("importQueueId"),
                    "status", deleteStatus,
                    "response", getDeleteDatasetResponse(deleteStatus)));
        }
    }

    /** Max length of the GUID. */
    private static final int GUID_LEN = 40;
    /** Max length of the appended tag (truncated dataset name). */
    private static final int APPENDED_TAG_LEN = 58;

    /**
     * Generate a guaranteed unique identifier using the dataset name and a salt.
     * @param datasetName the dataset name
     * @return the GUID
     */
    private String generateGuid(String datasetName) {
        boolean isNameUnique = false;
        String guid = null;
        // Try to create a unique dataset name using the
        // MD5 of the dataset name concatenated with current date
        // until a unique identifier is found
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();

        Random randomGenerator = new Random();
        do {
            Integer salt = randomGenerator.nextInt();
            String hash =  DigestUtils.shaHex(datasetName + salt);
            // Truncate the dataset name if it's greater than APPENDED_TAG_LEN chars
            String appendedTag = datasetName.length() > APPENDED_TAG_LEN
                    ? datasetName.substring(0, APPENDED_TAG_LEN)
                        : datasetName;
            // This GUID is the hash tag followed by the dataset name (truncated if necessary)
            guid = hash.substring(0, GUID_LEN)
                    + "[" + appendedTag + "]";
            // Make sure this name is unique
            List<DatasetItem> existingDataset =
                    (List<DatasetItem>) dsDao.find(guid);
                if (existingDataset == null || existingDataset.size() == 0) {
                    isNameUnique = true;
                }
        } while (!isNameUnique);
        return guid;
    }

    /**
     * AJAX response messages for dataset delete.
     * @param status the status of the release action (error or deleted)
     * @return the response message
     */
    private String getDeleteDatasetResponse(DS_DELETE_STATUS status) {
        String response;
        if (status.equals(DS_DELETE_STATUS.DELETED)) {
            response = "Dataset deleted.";
        } else if (status.equals(DS_DELETE_STATUS.WARNING)) {
            response = "Unable to delete dataset on remote DataShop instance.";
        } else {
            response = MSG_DELETE_DATASET_ERROR;
        }
        return response;
    }

    /**
     * Utility to remove file associated with ImportQueueItem.
     * @param fileItem the FileItem
     * @param baseDir the DataShop base directory
     * @return flag indicating success
     */
    public static boolean removeImportQueueFile(FileItem fileItem, String baseDir) {

        boolean result = false;

        if (fileItem.deleteFile(baseDir)) {
            result = true;
            // Remove the directory where the file was stored.
            String fullPathName = fileItem.getFullPathName(baseDir);
            // Make sure we don't delete the 'user_uploads' directory.
            if (!fileItem.getFilePath().equals(UploadDatasetHelper.SUB_PATH)) {
                File iqFileDir = new File(fullPathName);
                boolean flag = FileUtils.deleteFile(iqFileDir);
                if (flag) {
                    logger.info("Deleted file: + " + fullPathName);
                } else {
                    logger.info("Failed to delete file: + " + fullPathName);
                }
            } else {
                logger.warn("Not deleting directory: " + fullPathName);
            }
        }

        DaoFactory.DEFAULT.getFileDao().delete(fileItem);

        return result;
    }
}
