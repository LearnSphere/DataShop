/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PcConversionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportServlet;
import static edu.cmu.pslc.datashop.problemcontent.Constants.RESOURCES_PATH;

/**
 * This servlet is for displaying a Dataset's Problem List.
 *
 * @author Cindy Tipper
 * @version $Revision: 13150 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-04-20 15:04:07 -0400 (Wed, 20 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemListServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ProblemList";

    /** Variable for tracking directories within zip file. */
    private List<String> zipFileContents;

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/problem_list.jsp";

    /** Constant for the request attribute. */
    public static final String PROBLEM_LIST_ATTR = "problemList_";

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
        HttpSession httpSession = req.getSession(true);

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {

            String jspName = JSP_NAME;
            Integer datasetId = getIntegerId((String)req.getParameter("datasetId"));

            // If user not logged in, go to main project page.
            if (getLoggedInUserItem(req) == null) {
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
                disp.forward(req, resp);
                return;
            }

            if (datasetId == null) {
                logger.info("Dataset id is null, going to home page.");
                jspName = ProjectServlet.SERVLET_NAME;

                logger.info("Going to JSP: " + jspName);
                RequestDispatcher disp = getServletContext().getRequestDispatcher(jspName);
                disp.forward(req, resp);
                return;
            }

            UserItem userItem = getUser(req);

            // Handle 'Download All' export...
            if (getParameter(req, "downloadAll") != null) {
                downloadAll(userItem, datasetId, req, resp);
                return;
            }

            UserLogger.log(userItem, UserLogger.VIEW_PROBLEM_LIST, null, false);

            if (getParameter(req, "searchBy") != null) {
                setSearchParam(req, getParameter(req, "searchBy"));
            }
            if (getParameter(req, "rowsPerPage") != null) {
                setRowsPerPageParam(req, getParameter(req, "rowsPerPage"));
            }
            if (getParameter(req, "currentPage") != null) {
                setCurrentPageParam(req, getParameter(req, "currentPage"));
            }
            if (getParameter(req, "problemContent") != null) {
                setProblemContentParam(req, getParameter(req, "problemContent"));
            }

            redirectToDatasetInfoPage(datasetId, req, resp);

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /** Time stamp format for zip file. */
    private static final FastDateFormat TIME_STAMP_FMT
        = FastDateFormat.getInstance("yyyy_MMdd_HHmmss");

    /**
     * Handle the download of the Problem List.
     * @param userItem the logged in user
     * @param datasetId the dataset id
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void downloadAll(UserItem userItem, Integer datasetId,
                             HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        // Initial list of contents.
        zipFileContents = new ArrayList();

        boolean successFlag = false;

        String zipFileName = "ds" + datasetId + "_problem_content_"
            + TIME_STAMP_FMT.format(new Date()) + ".zip";

        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get(datasetId);

        File zipFile = createZipFile(zipFileName, dataset);
        String zipFilePath = zipFile.getAbsolutePath();

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

            UserLogger.log(dataset, userItem, UserLogger.EXPORT_PROBLEM_LIST, null, false);
            successFlag = true;
        } catch (FileNotFoundException exception) {
            logger.error("downloadAll: FileNotFoundException occurred: "
                         + zipFilePath,  exception);
            successFlag = false;
        } finally {
            if (inStream != null) { inStream.close();   }
            if (outStream != null) { outStream.close();   }
        }

        if (!successFlag) {
            redirectToDatasetInfoPage(datasetId, "Download failed.",
                                      ProblemListDto.STATUS_MESSAGE_LEVEL_WARN, req, resp);
        }

        // Delete the zip file created... it's been sent.
        if (zipFile != null) { zipFile.delete(); }
    }

    /**
     * Sets the search parameter in the ProblemListContext.
     * @param req {@link HttpServletRequest}
     * @param searchBy the string to search by
     */
    private void setSearchParam(HttpServletRequest req, String searchBy) {
        DatasetContext datasetContext = getDatasetContext(req);
        ProblemListContext context = datasetContext.getProblemListContext();
        context.setSearchBy(searchBy);
        context.setCurrentPage(ProblemListContext.DEFAULT_CURRENT_PAGE);
        req.getSession().setAttribute("datasetContext_" + datasetContext.getDataset().getId(),
                                      datasetContext);
    }

    /**
     * Sets the rows-per-page parameter in the ProblemListContext.
     * @param req {@link HttpServletRequest}
     * @param rowsPerPageParam the number of rows per page displayed
     */
    private void setRowsPerPageParam(HttpServletRequest req, String rowsPerPageParam) {
        int rowsPerPage =
            parseInt(rowsPerPageParam, ProblemListContext.DEFAULT_ROWS_PER_PAGE);

        DatasetContext datasetContext = getDatasetContext(req);
        ProblemListContext context = datasetContext.getProblemListContext();
        context.setRowsPerPage(rowsPerPage);
        context.setCurrentPage(ProblemListContext.DEFAULT_CURRENT_PAGE);
        req.getSession().setAttribute("datasetContext_" + datasetContext.getDataset().getId(),
                                      datasetContext);
    }

    /**
     * Sets the current-page parameter in the ProblemListContext.
     * @param req {@link HttpServletRequest}
     * @param currentPageParam the current page to be displayed
     */
    private void setCurrentPageParam(HttpServletRequest req, String currentPageParam) {
        int currentPage =
            parseInt(currentPageParam, ProblemListContext.DEFAULT_CURRENT_PAGE);

        DatasetContext datasetContext = getDatasetContext(req);
        ProblemListContext context = datasetContext.getProblemListContext();
        context.setCurrentPage(currentPage);
        req.getSession().setAttribute("datasetContext_" + datasetContext.getDataset().getId(),
                                      datasetContext);
    }

    /**
     * Sets the 'problem content' parameter in the ProblemListContext.
     * @param req {@link HttpServletRequest}
     * @param problemContentParam the types of problems to be displayed
     */
    private void setProblemContentParam(HttpServletRequest req, String problemContentParam) {
        DatasetContext datasetContext = getDatasetContext(req);
        ProblemListContext context = datasetContext.getProblemListContext();
        context.setProblemContent(problemContentParam);
        context.setCurrentPage(ProblemListContext.DEFAULT_CURRENT_PAGE);
        req.getSession().setAttribute("datasetContext_" + datasetContext.getDataset().getId(),
                                      datasetContext);
    }

    /**
     * Helper method for redirecting to the DatasetInfo page.
     * @param datasetId the dataset id
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToDatasetInfoPage(Integer datasetId,
                                           HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        redirectToDatasetInfoPage(datasetId, null, null, req, resp);
    }

    /**
     * Helper method for redirecting to the DatasetInfo page with message info.
     * @param datasetId the dataset id
     * @param message the message to display to the user
     * @param messageLevel the level of the message
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToDatasetInfoPage(Integer datasetId, String message, String messageLevel,
                                           HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        DatasetContext datasetContext = getDatasetContext(req);
        ProblemListContext context = datasetContext.getProblemListContext();

        ProblemListHelper plHelper = HelperFactory.DEFAULT.getProblemListHelper();
        ProblemListDto problemListDto = plHelper.getProblemListDto(datasetId, context);
        problemListDto.setMessage(message);
        problemListDto.setMessageLevel(messageLevel);

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(PROBLEM_LIST_ATTR + datasetId, problemListDto);

        resp.sendRedirect(DatasetInfoReportServlet.SERVLET + "?datasetId=" + datasetId);
    }

    /**
     * Helper method to parse Integer, using default if an error is encountered.
     * @param numStr the String to parse
     * @param defVal the default value
     * @return int
     */
    private int parseInt(String numStr, int defVal) {
        int retval = defVal;
        if (numStr != null) {
            try {
                retval = Integer.parseInt(numStr);
            } catch (NumberFormatException nfe) {
                // ignore error; return the default value
                retval = defVal;
            }
        }
        return retval;
    }

    /** Buffer size for zip file processing. */
    private static final int ZIP_BUFFER_SIZE = 18024;

    /**
     * Create the zip file with all of the dataset's problem content.
     * @param zipFileName the name of the zip file
     * @param dataset the dataset item
     * @return a File
     */
    private File createZipFile(String zipFileName, DatasetItem dataset) {

        PcConversionDao pcDao = DaoFactory.DEFAULT.getPcConversionDao();
        List<PcConversionItem> pcList = pcDao.getMappedByDataset(dataset);

        String zipFilePath = getBaseDir() + File.separator + zipFileName;
        File zipFile = new File(zipFilePath);

        ZipOutputStream outStream = null;
        try {
            outStream = new ZipOutputStream(new FileOutputStream(zipFile));
            outStream.setLevel(Deflater.DEFAULT_COMPRESSION);

            for (PcConversionItem pci : pcList) {
                addItemToZipFile(pci, outStream);
            }
        } catch (FileNotFoundException exception) {
            logger.error("createZipFile: FileNotFoundException: "
                         + "Problem Content (" + (Integer)dataset.getId() + ") -- "
                         + zipFilePath, exception);
        } catch (IOException exception) {
            logger.error("createZipFile: IOException.", exception);
        } finally {
            try {
                if (outStream != null) { outStream.close(); }
            } catch (IOException exception) {
                logger.error("createZipFile: IOException in finally block.", exception);
            }
        }
        return zipFile;
    }

    /**
     * Add the specified PcConversionItem to the zipfile.
     * @param item the PcConversionItem
     * @param outStream the zipfile output stream
     */
    private void addItemToZipFile(PcConversionItem item, ZipOutputStream outStream)
        throws FileNotFoundException, IOException {

        String fullPath = getBaseDir() + File.separator + item.getPath();
        File path = new File(fullPath);

        int len = fullPath.length() - item.getContentVersion().length();
        String initialPath = fullPath.substring(0, len);

        // For OLI, however, the initialPath is always the same. Can't parse using
        // contentVersion since it contains spaces and commas and ...
        if (item.getConversionTool().equals(PcConversionItem.OLI_CONVERTER)) {
            initialPath = getBaseDir() + File.separator
                + "problem_content" + File.separator + "oli" + File.separator;
        }

        addFileToZip(path, initialPath, outStream);

        // For OLI, the 'resources' directory is up one level. Add it now.
        if (item.getConversionTool().equals(PcConversionItem.OLI_CONVERTER)) {
            String resourcesPathStr = path.getParent() + File.separator + "resources";
            File resourcesPath = new File(resourcesPathStr);
            addFileToZip(resourcesPath, initialPath, outStream);
        }
    }

    /** Constant for HTML file extension. */
    private static final String HTML_EXT = ".html";

    /** Constant for current dir. */
    private static final String CUR_DIR = ".";

    /** Constant for temp file extension. */
    private static final String TEMP_EXT = ".tmp";

    /**
     * Recursively add file (node) to the zip file.
     * @param node the file or path
     * @param initialPath the initial part of the file path
     * @param outStream the zipfile output stream
     */
    private void addFileToZip(File node, String initialPath, ZipOutputStream outStream)
        throws FileNotFoundException, IOException {

        //add file only
    if (node.isFile()) {
            FileInputStream inStream = null;
            File tmpFile = null;
            try {
                String fileName = getFileNameOnly(node.getAbsoluteFile().toString(), initialPath);

                File theFile = new File(initialPath + File.separator + fileName);

                // Must update HTML to fix 'resources' path.
                if (fileName.endsWith(HTML_EXT)) {
                    String theFileAsString = FileUtils.readFileToString(theFile, Charset.defaultCharset().toString());
                    theFileAsString = theFileAsString.replaceAll(RESOURCES_PATH, CUR_DIR);
                    tmpFile = new File(initialPath + File.separator + fileName + TEMP_EXT);
                    FileUtils.writeStringToFile(tmpFile, theFileAsString, Charset.defaultCharset().toString());
                    theFile = tmpFile;
                }

                byte[] buffer = new byte[ZIP_BUFFER_SIZE];
                inStream =  new FileInputStream(theFile);
                ZipEntry zipEntry = new ZipEntry(fileName);
                outStream.putNextEntry(zipEntry);
                int length = 0;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
            } finally {
                try {
                    if (inStream != null) { inStream.close(); }

                    // If we created a temp file, delete it.
                    if (tmpFile != null) { tmpFile.delete(); }
                } catch (IOException ioe) {
                    logger.info("Exception closing stream: ", ioe);
                }
            }
    }

    if (node.isDirectory()) {
            if (node.getName().equals("CVS") || node.getName().equals(".svn")) {
                logger.info("Skipping directory " + node.getName());
            } else if (zipFileContents.contains(node.getAbsolutePath())) {
                logger.info("*** Ignoring duplicate: " + node.getAbsolutePath());
                // Guard against duplicate entries. Need only check at the directory level.
                return;
            } else {
                zipFileContents.add(node.getAbsolutePath());
                String[] fileNames = node.list();
                for(String name : fileNames) {
                    addFileToZip(new File(node, name), initialPath, outStream);
                }
            }
    }
    }

    /**
     * Given a fully-qualified path and file name, return only the name of the file.
     * @param fileName fully-qualified file name
     * @param pathName fully-qualified path
     */
    private String getFileNameOnly(String fileName, String pathName) {
        return fileName.substring(pathName.length(), fileName.length());
    }
}
