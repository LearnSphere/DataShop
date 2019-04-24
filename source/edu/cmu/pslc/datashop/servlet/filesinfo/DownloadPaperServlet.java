/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.filesinfo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dto.PaperFile;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.datasetinfo.FileServlet;

/**
 * This servlet is for downloading the file associated with a paper from the server.
 * These are accessible to all users, including those not logged in.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12467 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-07-10 11:46:23 -0400 (Fri, 10 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DownloadPaperServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

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
        PrintWriter outWriter = null;
        try {
            setEncoding(req, resp);

            UserItem userItem = getLoggedInUserItem(req);
            if (userItem == null) {
                userItem = DaoFactory.DEFAULT.getUserDao().findOrCreateDefaultUser();
            }

            DatasetItem datasetItem = getDatasetItemFromParam(req);

            String fileIdStr = req.getParameter(FileServlet.FILE_ID_PARAM);
            if (fileIdStr != null) {
                // Make sure specified file is part of the specified dataset.
                if (!fileIsPartOfDataset(req, resp, datasetItem, fileIdStr)) {
                    redirectToFilesInfo(resp, datasetItem);
                    return;
                }

                if (req.getServletPath().indexOf("Download") >= 0) {
                    handleDownload(req, resp, datasetItem, userItem, fileIdStr);
                    return;
                } else {
                    redirectToFilesInfo(resp, datasetItem);
                }
            } else {
                logger.debug("File id or name is missing from parameters.");
                redirectToFilesInfo(resp, datasetItem);
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);

        } finally {
            if (outWriter != null) {
                outWriter.close();
            }
            logger.debug("doPost end");
        }
    } // end doPost

    /**
     * Check to ensure that the specified file belongs to the specified dataset.
     * This method is a no-op for 'Upload'.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetItem the Dataset Item
     * @param fileIdStr the file id string from the URL
     * @return boolean flag indicating success or failure
     */
    private boolean fileIsPartOfDataset(HttpServletRequest req, HttpServletResponse resp,
            DatasetItem datasetItem, String fileIdStr) {

        FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();

        Integer fileId = new Integer(fileIdStr);

        // Check the files for papers.
        List<PaperFile> papers = filesInfoHelper.getPaperList(datasetItem);
        for (PaperFile pf : papers) {
            if (pf.getPaperItem().getFile().getId().equals(fileId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handles downloading the file.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetItem the dataset item
     * @param userItem the user item
     * @param fileIdStr the file id string from the URL
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     */
    private void handleDownload(HttpServletRequest req, HttpServletResponse resp,
            DatasetItem datasetItem, UserItem userItem, String fileIdStr)
                    throws ServletException, IOException {
        try {
            Integer fileId = new Integer(fileIdStr);

            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
            FileItem fileItem = fileDao.get(fileId);

            String actualFileName = fileItem.getUrl(getBaseDir());
            String fileName = actualFileName.substring(actualFileName.lastIndexOf('/') + 1);

            logger.debug("Downloading file " + actualFileName);

            resp.setContentType("application/x-download");
            resp.setContentLength(fileItem.getFileSize().intValue());
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            BufferedInputStream inputStream = null;

            try {
                inputStream =
                        new BufferedInputStream(new FileInputStream(actualFileName));

                OutputStream outStream = resp.getOutputStream();

                int ch;
                while ((ch = inputStream.read()) != -1) {
                    outStream.write(ch);
                }

                UserLogger.log(datasetItem, userItem,
                        UserLogger.FILE_DOWNLOAD, fileName + " (" + fileId + ")");

            } catch (FileNotFoundException exception) {
                logger.debug("FileNotFoundException occurred in handleDownload",
                        exception);
            } finally {
                if (inputStream != null) {
                    inputStream.close();  // very important
                }
            }

        } catch (Exception exception) {
            logger.error("Exception occurred in handleDownload", exception);
        }
    }

    /**
     * Helper function to handle the redirect to files info w/o losing the dataset id.
     * @param resp the {@link HttpServletResponse}
     * @param datasetItem the dataset item
     * @throws IOException an IOException attempting to redirect.
     */
    private void redirectToFilesInfo(HttpServletResponse resp, DatasetItem datasetItem)
            throws IOException {
        resp.sendRedirect(FilesInfoServlet.SERVLET + "?datasetId=" + datasetItem.getId());
    }

    /**
     * Get the dataset item.
     * @param req HttpServletRequest.
     * @return the dataset item if found, null otherwise
     */
    private DatasetItem getDatasetItemFromParam(HttpServletRequest req) {
        DatasetItem item = null;
        String itemIdString = req.getParameter("datasetId");
        if (itemIdString == null) {
            itemIdString = (String)req.getAttribute("datasetId");
        }

        if (itemIdString == null || itemIdString.equals("")
                || itemIdString.equals("null")) {
            logger.debug("datasetId parameter is blank");
        } else {
            try {
                Integer itemId = Integer.parseInt(itemIdString);
                DatasetDao dao = DaoFactory.DEFAULT.getDatasetDao();
                item = dao.get(itemId);
                logger.debug("datasetId parameter is valid: " + itemIdString);
            } catch (NumberFormatException exception) {
                logger.debug("datasetId parameter is not a valid id: " + itemIdString);
                item = null;
            }
        }

        return item;
    }
}
