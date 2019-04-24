/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DiscourseImportQueueMapDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;

import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DiscourseImportQueueMapItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;

/**
 * This servlet is for viewing meta-data for a DiscourseDb discourse.
 *
 * @author Cindy Tipper
 * @version $Revision: 13128 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-19 12:46:09 -0400 (Tue, 19 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDbServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String INFO_SERVLET = "/DiscourseInfo";
    /** The Servlet name. */
    public static final String EXPORT_SERVLET = "/DiscourseExport";

    /** Constant for the session attribute. */
    public static final String DISCOURSE_ID_ATTR = "discourseId";
    /** Constant for the session attribute. */
    public static final String DISCOURSE_OVERVIEW_ATTR = "discourse_overview_";

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARNING";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    /** The JSP name for the Info version of this servlet. */
    private static final String INFO_JSP_NAME = "/jsp_discoursedb/discourse_overview.jsp";

    /** The JSP name for the Export version of this servlet. */
    private static final String EXPORT_JSP_NAME = "/jsp_discoursedb/discourse_export.jsp";

    /** Constant for the DiscourseDb actions. */
    private static final String PARAM_ACTION = "discourseDbAction";

    /** Label used for setting session attribute "recent_report". */
    private static final String INFO_SERVLET_NAME = "DiscourseInfo";

    /** Label used for setting session attribute "recent_report". */
    private static final String EXPORT_SERVLET_NAME = "DiscourseExport";

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
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), INFO_SERVLET_NAME);

        try {
            Long discourseId = getLongId((String)req.getParameter("discourseId"));

            req.setAttribute(DISCOURSE_ID_ATTR, discourseId);

            UserItem userItem = getLoggedInUserItem(req);
            boolean userLoggedIn = true;

            // If user not logged in, use 'default'.
            if (userItem == null) {
                userLoggedIn = false;
                userItem = edu.cmu.pslc.datashop.dao.DaoFactory.
                    DEFAULT.getUserDao().findOrCreateDefaultUser();
            }

            DiscourseDbHelper helper = HelperFactory.DEFAULT.getDiscourseDbHelper();

            if (req.getParameter("importDiscourse") != null) {
                importDiscourse(req, resp);
                return;
            }

            DiscourseDto dto = helper.getDiscourseDto(discourseId);
            req.setAttribute(DISCOURSE_OVERVIEW_ATTR + discourseId, dto);

            RequestDispatcher disp = getServletContext().getRequestDispatcher(INFO_JSP_NAME);

            // If user not logged in, all they get is meta-data page.
            if (!userLoggedIn) {
                logger.debug("User not logged in. Forwarding to Discourse Info page.");
                disp.forward(req, resp);
                return;
            }

            String servletPath = req.getServletPath();
            if (servletPath.equals(EXPORT_SERVLET)) {

                // Reset the most recent servlet name for the help page
                setRecentReport(req.getSession(true), EXPORT_SERVLET_NAME);

                String actionParam = getParameter(req, PARAM_ACTION);
                if (actionParam == null) {
                    logger.debug("Forwarding user to Discourse Export page: " + EXPORT_JSP_NAME);
                    disp = getServletContext().getRequestDispatcher(EXPORT_JSP_NAME);
                    disp.forward(req, resp);
                } else if ((actionParam != null) && (actionParam.equals("ExportDiscourse"))) {
                    exportDiscourse(req, resp, userItem, discourseId);
                }
            } else {
                disp.forward(req, resp);
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
     * Helper method for importing a discourse.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void importDiscourse(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, JSONException
    {
        String message;
        String messageLevel;

        try {
            DiscourseDbHelper helper = HelperFactory.DEFAULT.getDiscourseDbHelper();

            // The assumption (for now) is that there is only one discourse in the
            // source database and it has a primary key of 1.
            helper.importDiscourse(new Long(1));
            message = "Discourse imported.";
            messageLevel = "SUCCESS";
        } catch (Exception e) {
            message = "Failed to import new discourse: " + e;
            messageLevel = "ERROR";
        }

        writeJSON(resp, json("message", message, "level", messageLevel));
    }

    /**
     * Helper method to export a discourse.
     * For now, zip up the discourse schema sql provided at Upload.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param discourseId the discourse db id
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void exportDiscourse(HttpServletRequest req, HttpServletResponse resp,
                                 UserItem userItem, Long discourseId)
        throws ServletException, IOException
    {
        Boolean successFlag = false;
        String errorMessage = "Invalid export.";

        DiscourseDbHelper helper = HelperFactory.DEFAULT.getDiscourseDbHelper();

        DiscourseItem discourse = helper.getDiscourseItem(discourseId);
        ImportQueueItem iqItem = getImportQueueItemForDiscourse(discourse);
        if (discourse == null) {
            errorMessage = "Invalid discourse id: " + discourseId;
        } else if (iqItem == null) {
            errorMessage = "Discourse not available for export.";
        } else {

            String zipFileName = discourse.getZipFileName();

            File zipFile = createZipFile(zipFileName, discourse, iqItem);
            String zipFilePath = zipFile.getAbsolutePath();

            String info = "Discourse '" + discourse.getName() + "', "
                + "File '" + zipFileName + "'";

            resp.setContentType("application/zip; charset=UTF-8");
            resp.setContentLength((int)zipFile.length());
            resp.setHeader("Content-Disposition",
                           "attachment; filename=\"" + zipFileName + "\"");

            BufferedInputStream inStream = null;
            OutputStream outStream = resp.getOutputStream();
            try {
                inStream = new BufferedInputStream(new FileInputStream(zipFilePath));
                int ch;
                while ((ch = inStream.read()) != -1) {
                    outStream.write(ch);
                }
                UserLogger.log(null, userItem, UserLogger.EXPORT_DISCOURSE, info);
                successFlag = true;
            } catch (FileNotFoundException exception) {
                logger.error("exportDiscourse: FileNotFoundException occurred: "
                             + info + " :: " + zipFilePath,  exception);
                successFlag = false;
            } finally {
                // very important
                if (inStream != null) { inStream.close();   }
                if (outStream != null) { outStream.close();   }
            }
        }

        if (!successFlag) {
            DiscourseDto dto = helper.getDiscourseDto(discourseId);
            dto.setMessage(errorMessage);
            dto.setMessageLevel(STATUS_MESSAGE_LEVEL_WARN);
            req.setAttribute(DISCOURSE_OVERVIEW_ATTR + discourseId, dto);

            logger.debug("Forwarding user to Discourse Export page: " + EXPORT_JSP_NAME);
            RequestDispatcher disp = getServletContext().getRequestDispatcher(EXPORT_JSP_NAME);
            disp.forward(req, resp);
        }
    }

    /** Buffer size for zip file processing. */
    private static final int ZIP_BUFFER_SIZE = 18024;

    /**
     * Discourses can be very large. Zip the file we received for Upload.
     * @param zipFileName the name of the zip file
     * @param discourse the discourse item
     * @param iqItem the ImportQueue item
     * @return a File
     */
    private File createZipFile(String zipFileName,
                               DiscourseItem discourse,
                               ImportQueueItem iqItem) {

        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        FileItem fileItem = fileDao.get((Integer)iqItem.getFile().getId());

        String zipFilePath = getZipFilePath((Integer)iqItem.getId(), zipFileName);
        File zipFile = new File(zipFilePath);

        ZipOutputStream outStream = null;
        FileInputStream inStream = null;
        ZipEntry zipEntry = null;
        byte[] buffer = new byte[ZIP_BUFFER_SIZE];

        try {
            outStream = new ZipOutputStream(new FileOutputStream(zipFile));
            outStream.setLevel(Deflater.DEFAULT_COMPRESSION);

            String fileName = fileItem.getFileName();
            String filePath = fileItem.getUrl(getBaseDir());
            File file = new File(filePath);
            zipEntry = new ZipEntry(fileName);
            inStream =  new FileInputStream(file);
            outStream.putNextEntry(zipEntry);
            int length = 0;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            inStream.close();

        } catch (FileNotFoundException exception) {
            logger.error("createZipFile: FileNotFoundException.", exception);
        } catch (IOException exception) {
            logger.error("createZipFile: IOException.", exception);
        } finally {
            // very important
            try {
                if (inStream != null) {  inStream.close(); }
                if (outStream != null) { outStream.close(); }
            } catch (IOException exception) {
                logger.error("createZipFile: IOException in finally block.", exception);
            }
        }

        return zipFile;
    }

    /** Constant for the location of the user upload files. */
    private static final String USER_UPLOADS_SUB_PATH = "user_uploads";

    /**
     * Get the path where the files for the discourse are to be stored on the server.
     * @param iqId the ImportQueue id
     * @param zipFileName the name of the Zip file
     * @return the whole path for this tool
     */
    private String getZipFilePath(Integer iqId, String zipFileName) {

        StringBuffer result = new StringBuffer();
        result.append(getBaseDir());
        result.append(File.separator);
        result.append(USER_UPLOADS_SUB_PATH);
        result.append(File.separator);
        result.append(iqId);
        result.append(File.separator);
        result.append(zipFileName);

        return result.toString();
    }

    /**
     * Helper method to get ImportQueueItem for a specified Discourse.
     * @param discourse the DiscourseItem
     * @return ImportQueueItem null if none exists
     */
    private ImportQueueItem getImportQueueItemForDiscourse(DiscourseItem discourse) {
        if (discourse == null) { return null; }

        DiscourseImportQueueMapDao mapDao = DaoFactory.DEFAULT.getDiscourseImportQueueMapDao();
        DiscourseImportQueueMapItem map = mapDao.findByDiscourse(discourse);
        if (map == null) { return null; }
        if (map.getImportQueue() == null) { return null; }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        return iqDao.get((Integer)map.getImportQueue().getId());
    }
}
