/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.filesinfo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.FileItem.SortParameter;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.auth.filter.LoadDatsetCommand;

/**
 * This servlet is for handling the Files Info report.
 * There are two major session parameters expected.  One for the type of request
 * and the other for the type of content if content is requested.
 * The Request can be:
 * <ul>
 * <li>null (which implies the base JSP page)</li>
 * <li>content</li>
 * </ul>
 * The Content can be:
 * <ul>
 * <li>default (which is 'papers')</li>
 * <li>papers</li>
 * <li>files</li>
 * <li>externalAnalyses</li>
 * </ul>
 *
 * @author Cindy Tipper
 * @version $ $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-02-17 11:09:46 -0500 (Sat, 17 Feb 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FilesInfoServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET = "Files";
    /** The JSP name for the base content. */
    private static final String BASE_JSP_NAME = "/files_info.jsp";
    /** The JSP name for the papers sub content. */
    private static final String PAPERS_JSP_NAME = "/files_info_papers.jsp";
    /** The JSP name for the files sub content. */
    private static final String FILES_JSP_NAME = "/files_info_files.jsp";
    /** The JSP name for the externalAnalyses sub content. */
    private static final String EXT_ANALYSES_JSP_NAME = "/files_info_ext_analyses.jsp";

    /** Session Parameter. */
    private static final String FILES_REQUEST_PARAM = "files_request";
    /** Possible value for session parameter. */
    private static final String FILES_REQUEST_CONTENT_VALUE = "content";
    /** Session Parameter. */
    private static final String FILES_CONTENT_PARAM = "files_content";
    /** Possible value for session parameter. */
    protected static final String FILES_CONTENT_INITIALIZE_VALUE = "initialize";
    /** Possible value for session parameter. */
    public static final String FILES_CONTENT_PAPERS_VALUE = "papers";
    /** Possible value for session parameter. */
    public static final String FILES_CONTENT_FILES_VALUE = "files";
    /** Possible value for session parameter. */
    public static final String FILES_CONTENT_EXT_ANALYSES_VALUE = "externalAnalyses";

    /** Session Parameter. */
    public static final String FILES_REQUEST_SORT_BY = "sortBy";
    /** Session Parameter. */
    public static final String FILES_REQUEST_TOGGLE_FLAG = "toggle";
    /** Title column header. */
    public static final String TITLE_COLUMN = "Title";
    /** File Name column header. */
    public static final String FILE_NAME_COLUMN = "File Name";
    /** Uploaded By column header. */
    public static final String UPLOADED_BY_COLUMN = "Uploaded By";
    /** Date column header. */
    public static final String DATE_COLUMN = "Date";
    /** Preferred Citation column header. */
    public static final String PREF_CITATION_COLUMN = "Preferred Citation";
    /** Citation column header. */
    public static final String CITATION_COLUMN = "Citation";
    /* Paper Uploaded By column header. Leading and trailing whitespace is intentional. */
    /** Paper Uploaded By column header.*/
    public static final String PAPER_UPLOADED_BY_COLUMN = " Uploaded By ";
    /** KC Model column header. */
    public static final String KC_MODEL_COLUMN = "KC Model";
    /** Statistical Model column header. */
    public static final String STATISTICAL_MODEL_COLUMN = "Statistical Model";

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
        logDebug("doPost begin: ", getDebugParamsString(req));
        PrintWriter out = null;
        DatasetContext datasetContext = null;
        try {
            setEncoding(req, resp);
            datasetContext = getDatasetContext(req);
            UserItem userItem = getLoggedInUserItem(req);
            boolean isLoggedIn = (userItem == null) ? false : true;
            updateAccessFlag(datasetContext, userItem, "Files");

            if (userItem == null) {
                userItem = DaoFactory.DEFAULT.getUserDao().findOrCreateDefaultUser();
            }
            if (datasetContext == null) {
                datasetContext = LoadDatsetCommand.getDatasetContextStatic(userItem, req, resp);
            }
            FilesInfoContext infoContext = datasetContext.getFilesInfoContext();
            DatasetItem datasetItem = datasetContext.getDataset();
            logger.info(getInfoPrefix(datasetContext) + " dataset " + datasetItem.getId());

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET);

            // Get numbers of attachments, for the sub-tabs
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            Long numPapers = datasetDao.countPapers(datasetItem);
            Long numExternalAnalyses = datasetDao.countExternalAnalyses(datasetItem);
            Long numFiles = datasetDao.countFiles(datasetItem);
            req.setAttribute("numPapers", numPapers);
            req.setAttribute("numExternalAnalyses", numExternalAnalyses);
            req.setAttribute("numFiles", numFiles);

            if (req.getParameter(FILES_REQUEST_SORT_BY) != null) {
                String sortByParam = (String) req.getParameter(FILES_REQUEST_SORT_BY);
                Boolean toggleFlag =
                        new Boolean((String) req.getParameter(FILES_REQUEST_TOGGLE_FLAG));
                String contentType = req.getParameter(FILES_CONTENT_PARAM);
                req.getSession().setAttribute(FILES_REQUEST_SORT_BY
                        + "_" + contentType, sortByParam);
                sortParameters(infoContext, sortByParam, toggleFlag);
            }

            // Find out what kind of request it is.
            String fRequest = req.getParameter(FILES_REQUEST_PARAM);

            // If the request is null, then its a request of the base JSP.
            if (fRequest == null) {
                logger.info(getBenchmarkPrefix(datasetContext) + " accessing base JSP.");

                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(BASE_JSP_NAME);
                disp.forward(req, resp);
                return;
            }

            if (datasetContext != null) { setInfo(req, datasetContext); }

            // AJAX request for the content
            if (fRequest.equals(FILES_REQUEST_CONTENT_VALUE)) {
                String contentType = getContentType(req, infoContext, isLoggedIn);
                setInfo(req, datasetContext);

                //if user is not logged in then go to the papers sub-tab only
                if (contentType.equals(FILES_CONTENT_PAPERS_VALUE)) {
                    // forward to the JSP (view)
                    logger.debug("Ajax request: Forwarding to the papers sub content JSP");
                    RequestDispatcher disp =
                            getServletContext().getRequestDispatcher(PAPERS_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), userItem,
                            UserLogger.VIEW_PAPERS);
                    disp.forward(req, resp);
                } else if (contentType.equals(FILES_CONTENT_FILES_VALUE)) {
                    // forward to the JSP (view)
                    logger.debug("Ajax request: Forwarding to the files sub content JSP");
                    RequestDispatcher disp =
                            getServletContext().getRequestDispatcher(FILES_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), userItem,
                            UserLogger.VIEW_FILES);
                    disp.forward(req, resp);
                } else if (contentType.equals(FILES_CONTENT_EXT_ANALYSES_VALUE)) {
                    // forward to the JSP (view)
                    logger.debug("Ajax request: Forwarding to the extAnalyses sub content JSP");
                    RequestDispatcher disp =
                            getServletContext().getRequestDispatcher(EXT_ANALYSES_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), userItem,
                            UserLogger.VIEW_EXTERNAL_ANALYSES);
                    disp.forward(req, resp);
                } else {
                    logger.warn("Invalid dataset info request type specified: " + contentType);
                }
            } else {
                logger.warn("Invalid dataset info request type specified: " + fRequest);
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /**
     * Retrieve the content type for the Dataset Info Report from the session parameter.
     * @param req the HTTP servlet request
     * @param context the current {@link FilesInfoContext}
     * @param isLoggedIn true if a real user is logged in, false otherwise
     * @return a string representation of the content type requested
     */
    private String getContentType(HttpServletRequest req, FilesInfoContext context,
            boolean isLoggedIn) {
        String contentType = FILES_CONTENT_PAPERS_VALUE;
        if (isLoggedIn) {
            contentType = req.getParameter(FILES_CONTENT_PARAM);
        }
        if (contentType != null) {
            if (contentType.compareTo(FILES_CONTENT_INITIALIZE_VALUE) == 0) {
                contentType = context.getCurrentTab();
                if (contentType == null) {
                    contentType = FILES_CONTENT_FILES_VALUE;
                    context.setCurrentTab(contentType);
                    logDebug("defaulting contentType to: ", contentType);
                } else {
                    logDebug("contentType: ", contentType);
                }
            } else {
                context.setCurrentTab(contentType);
                logDebug("setting contentType to: ", contentType);
            }
        }
        logDebug("contentType: ", contentType);
        return contentType;
    }

    /**
     * Returns the relative path to the appropriate image.
     * @param sortByParam parameter sorting by
     * @param column column sorting by
     * @param descFlag descending flag
     * @return relative path to image
     */
    public static String showSortOrder(String sortByParam, String column, Boolean descFlag) {
        String imgIcon = "images/trans_spacer.gif";
        if (sortByParam != null && sortByParam.equals(column)) {
            imgIcon = descFlag
                    ? "images/grid/down.gif" : "images/grid/up.gif";
        }
        return imgIcon;
    }


    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param infoContext the FilesInfoContext
     * @param sortByString the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    private void sortParameters(FilesInfoContext infoContext, String sortByString,
            Boolean toggleFlag) {
        // If not toggling, ignore last toggle, i.e., invert 'sortOrder'.
        if (sortByString.equals(TITLE_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(TITLE_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(TITLE_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_TITLE_ASC,
                        FileItem.SortParameter.SORT_BY_FILE_NAME_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_TITLE_DESC,
                        FileItem.SortParameter.SORT_BY_FILE_NAME_ASC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(FILE_NAME_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(FILE_NAME_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(FILE_NAME_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_FILE_NAME_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_FILE_NAME_DESC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(DATE_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(DATE_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(DATE_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_DATE_ASC,
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_DATE_DESC,
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(UPLOADED_BY_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(UPLOADED_BY_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(UPLOADED_BY_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC,
                        FileItem.SortParameter.SORT_BY_TITLE_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_DESC,
                        FileItem.SortParameter.SORT_BY_TITLE_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(PREF_CITATION_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(PREF_CITATION_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(PREF_CITATION_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_PREF_CITATION_ASC,
                        FileItem.SortParameter.SORT_BY_CITATION_ASC,
                        FileItem.SortParameter.SORT_BY_FILE_NAME_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_PREF_CITATION_DESC,
                        FileItem.SortParameter.SORT_BY_CITATION_ASC,
                        FileItem.SortParameter.SORT_BY_FILE_NAME_ASC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(CITATION_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(CITATION_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(CITATION_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_CITATION_ASC,
                        FileItem.SortParameter.SORT_BY_FILE_NAME_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_CITATION_DESC,
                        FileItem.SortParameter.SORT_BY_FILE_NAME_ASC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(PAPER_UPLOADED_BY_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(PAPER_UPLOADED_BY_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(PAPER_UPLOADED_BY_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC,
                        FileItem.SortParameter.SORT_BY_CITATION_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_DESC,
                        FileItem.SortParameter.SORT_BY_CITATION_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(KC_MODEL_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(KC_MODEL_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(KC_MODEL_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_KC_MODEL_ASC,
                        FileItem.SortParameter.SORT_BY_STATISTICAL_MODEL_ASC,
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_KC_MODEL_DESC,
                        FileItem.SortParameter.SORT_BY_STATISTICAL_MODEL_ASC,
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            }
        } else if (sortByString.equals(STATISTICAL_MODEL_COLUMN)) {
            Boolean isAscending = infoContext.getSortOrder(STATISTICAL_MODEL_COLUMN);
            if (toggleFlag) {
                infoContext.toggleSortOrder(STATISTICAL_MODEL_COLUMN);
            } else {
                isAscending = !isAscending;
            }
            if (isAscending) {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_STATISTICAL_MODEL_ASC,
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            } else {
                SortParameter[] sortParams = {
                        FileItem.SortParameter.SORT_BY_STATISTICAL_MODEL_DESC,
                        FileItem.SortParameter.SORT_BY_UPLOADED_BY_ASC,
                        FileItem.SortParameter.SORT_BY_DATE_ASC };
                infoContext.setSortByParameters(sortParams);
            }
       }
    }
}
