/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.datasetinfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ExternalAnalysisDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.PaperDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dto.ExternalAnalysisFile;
import edu.cmu.pslc.datashop.dto.PaperFile;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper;
import edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoServlet;
import edu.cmu.pslc.datashop.util.FileUtils;

/**
 * This servlet is for downloading files from the server.
 * It was important to serve files from a servlet so that they could
 * be protected behind the application server and not publicly accessible
 * without logging in to the DataShop.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15837 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FileServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Parameter. */
    public static final String FILE_ID_PARAM    = "fileId";
    /** Parameter. */
    public static final String FILE_NAME_PARAM  = "fileName";
    /** Parameter. */
    private static final String FILE_TITLE_PARAM = "fileTitle";
    /** Parameter. */
    private static final String FILE_DESC_PARAM  = "fileDescription";
    /** Parameter. */
    private static final String PAPER_ID_PARAM    = "paperId";
    /** Parameter. */
    private static final String PREFERRED_CITATION_PARAM  = "preferredCitation";
    /** Parameter. */
    private static final String PAPER_CITATION_PARAM = "paperCitation";
    /** Parameter. */
    private static final String PAPER_ABSTRACT_PARAM  = "paperAbstract";
    /** Parameter. */
    private static final String EXTERNAL_ANALYSIS_ID_PARAM = "externalAnalysisId";
    /** Parameter. */
    private static final String EXTERNAL_ANALYSIS_TITLE_PARAM = "externalAnalysisTitle";
    /** Parameter. */
    private static final String EXTERNAL_ANALYSIS_SKILL_MODEL_PARAM = "externalAnalysisSkillModel";
    /** Parameter. */
    private static final String EXTERNAL_ANALYSIS_STAT_MODEL_PARAM = "externalAnalysisStatModel";

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

            DatasetContext datasetContext = getDatasetContext(req);

            // Ensure specified file is part of the specified dataset.
            if (!fileIsPartOfDataset(req, resp, datasetContext)) {
                redirectToFilesInfo(resp, datasetContext);
                return;
            }

            if (req.getServletPath().indexOf("Download") >= 0) {
                handleDownload(req, resp, datasetContext);
                return;
            }

            if (req.getServletPath().indexOf("Display") >= 0) {
                JSONObject returnJSON = handleDisplay(req, resp, datasetContext);
                if (returnJSON != null) {
                    resp.setContentType("application/json");
                    outWriter = resp.getWriter();
                    outWriter.write(returnJSON.toString());
                    logger.debug("returnJSON.toString(): " + returnJSON.toString());
                    outWriter.flush();
                }
            }

            //check that the user actually has edit/administrator authorization for
            //the upload/edit calls.
            if (!hasAdminAuthorization(req, datasetContext)
                    && !hasEditAuthorization(req, datasetContext)) {
                logger.warn("unauthorized user attempted up upload/edit a file."
                        + "\n\t UserId: " + datasetContext.getUserId()
                        + "\n\t DatasetId: " + datasetContext.getDataset().getId());
                return;
            }

            String fileSizeExceededMsg = "File size exceeds 400MB allowance";
            if (hasAdminAuthorization(req, datasetContext)) {
                fileSizeExceededMsg = "File size exceeds 1GB allowance";
            }
            if (req.getServletPath().indexOf("Upload") >= 0) {
                List <org.apache.commons.fileupload.FileItem> items = getDatasetUploadItems(req);
                if (items == null) {
                    logger.error(fileSizeExceededMsg);
                    datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                    datasetContext.getFilesInfoContext().setFileMessage(fileSizeExceededMsg);
                    setInfo(req, datasetContext);
                    redirectToFilesInfo(resp, datasetContext);
                    return;
                } else {
                    datasetContext.setUploadItems(items);
                    handleUpload(req, resp, datasetContext);
                }
            } else if (req.getServletPath().indexOf("Delete") >= 0) {
                if (req.getParameter(FILE_ID_PARAM) != null) {
                   handleDeleteFile(req, resp, datasetContext);
                } else if (req.getParameter(PAPER_ID_PARAM) != null) {
                   handleDeletePaper(req, resp, datasetContext);
                } else if (req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM) != null) {
                    handleDeleteExternalAnalysis(req, resp, datasetContext);
                } else {
                    logger.debug("Delete command received without a file, "
                            + " externalAnalysis or paper id.");
                    redirectToFilesInfo(resp, datasetContext);
                }
            } else if (req.getServletPath().indexOf("Edit") >= 0) {
                if (req.getParameter(FILE_ID_PARAM) != null) {
                    JSONObject returnJSON = handleEditFile(req, resp, datasetContext);
                    if (returnJSON != null) {
                        resp.setContentType("application/json");
                        outWriter = resp.getWriter();
                        outWriter.write(returnJSON.toString());
                        logger.debug("returnJSON.toString(): " + returnJSON.toString());
                        outWriter.flush();
                    }
                } else if (req.getParameter(PAPER_ID_PARAM) != null) {
                    JSONObject returnJSON = handleEditPaper(req, resp, datasetContext);
                    if (returnJSON != null) {
                        resp.setContentType("application/json");
                        outWriter = resp.getWriter();
                        outWriter.write(returnJSON.toString());
                        logger.debug("returnJSON.toString(): " + returnJSON.toString());
                        outWriter.flush();
                    }
                } else if (req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM) != null) {
                    JSONObject returnJSON = handleEditExternalAnalysis(req, resp, datasetContext);
                    if (returnJSON != null) {
                        resp.setContentType("application/json");
                        outWriter = resp.getWriter();
                        outWriter.write(returnJSON.toString());
                        logger.debug("returnJSON.toString(): " + returnJSON.toString());
                        outWriter.flush();
                    }
                } else {
                    redirectToFilesInfo(resp, datasetContext);
                }
            }
            return;

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
     * @param datasetContext the Dataset context
     * @return boolean flag indicating success or failure
     */
    private boolean fileIsPartOfDataset(HttpServletRequest req, HttpServletResponse resp,
            DatasetContext datasetContext) {

        // Always true for 'Upload'
        if (req.getServletPath().indexOf("Upload") >= 0) {
            return true;
        }

        DatasetItem datasetItem = datasetContext.getDataset();
        FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();

        if (req.getParameter(FILE_ID_PARAM) != null) {
            String fileIdStr = req.getParameter(FILE_ID_PARAM);
            Integer fileId = new Integer(fileIdStr);

            List<FileItem> files = filesInfoHelper.getFileList(datasetItem);
            for (FileItem fi : files) {
                if (fi.getId().equals(fileId)) { return true; }
            }

            // If user request is a 'Download' then the fileId may refer to
            // a paper or external analysis... must check those.
            List<PaperFile> papers = filesInfoHelper.getPaperList(datasetItem);
            for (PaperFile pf : papers) {
                if (pf.getPaperItem().getFile().getId().equals(fileId)) {
                    return true;
                }
            }
            List<ExternalAnalysisFile> eaFiles =
                    filesInfoHelper.getExternalAnalysisList(datasetItem);
            for (ExternalAnalysisFile eaf : eaFiles) {
                if (eaf.getExternalAnalysisItem().getFile().getId().equals(fileId)) {
                    return true;
                }
            }

        } else if (req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM) != null) {
            String eaIdStr = req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM);
            Integer eaId = new Integer(eaIdStr);

            List<ExternalAnalysisFile> eaFiles =
                    filesInfoHelper.getExternalAnalysisList(datasetItem);
            for (ExternalAnalysisFile eaf : eaFiles) {
                if (eaf.getExternalAnalysisItem().getId().equals(eaId)) {
                    return true;
                }
            }
        } else if (req.getParameter(PAPER_ID_PARAM) != null) {
            String paperIdStr = req.getParameter(PAPER_ID_PARAM);
            Integer paperId = new Integer(paperIdStr);

            List<PaperFile> papers = filesInfoHelper.getPaperList(datasetItem);
            for (PaperFile pf : papers) {
                if (pf.getPaperItem().getId().equals(paperId)) {
                    return true;
                }
            }
        }

        datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
        datasetContext.getFilesInfoContext().setFileMessage(
                "The specified file is not part of the specified Dataset. "
                + "Please refresh the page and try again.");
        return false;
    }

    /**
     * Handles downloading the file.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     */
    private void handleDownload(HttpServletRequest req, HttpServletResponse resp,
            DatasetContext datasetContext) throws ServletException, IOException {
        //get parameters
        if (req.getParameter(FILE_ID_PARAM) != null
         && req.getParameter(FILE_NAME_PARAM) != null) {

            try {
                String fileIdStr = req.getParameter(FILE_ID_PARAM);
                Integer fileId = new Integer(fileIdStr);

                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                FileItem fileItem = fileDao.get(fileId);

                String actualFileName = fileItem.getUrl(getBaseDir());
                String fileName = actualFileName.substring(actualFileName.lastIndexOf('/') + 1);

                logger.debug("Downloading file " + actualFileName);

                resp.setContentType("application/x-download");
                resp.setHeader("Content-Length", Long.toString(fileItem.getFileSize()));
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

                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
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
        } else {
            logger.debug("File id or name is missing from parameters.");
            redirectToFilesInfo(resp, datasetContext);
        }
    }

    /**
     * Handles downloading the file for display purposes.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     * @throws JSONException an exception occurred creating the JSON objects.
     * @return String of success.
     */
    private JSONObject handleDisplay(HttpServletRequest req, HttpServletResponse resp,
            DatasetContext datasetContext) throws ServletException, IOException, JSONException {

        JSONObject message = null;

        //get parameters
        if (req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM) != null) {

            try {
                String eaIdStr = req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM);
                Integer eaId = new Integer(eaIdStr);

                ExternalAnalysisDao eaDao = DaoFactory.DEFAULT.getExternalAnalysisDao();
                ExternalAnalysisItem eaItem = eaDao.get(eaId);

                Integer fileId = 0;
                if (eaItem.getFile() != null) {
                    fileId = (Integer)eaItem.getFile().getId();
                }

                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                FileItem fileItem = fileDao.get(fileId);

                // Guard against hacks of the javascript; make sure file is eligible for display.
                String fileType = fileItem.getFileType();
                if (!fileType.startsWith("text")) {
                    message = buildJSONMessage("ERROR",
                            "This file of type " + fileType + " cannot be displayed.", null);
                    return message;
                }

                String actualFileName = fileItem.getUrl(getBaseDir());

                BufferedInputStream inputStream = null;

                try {
                    File theFile = new File(actualFileName);

                    message = buildJSONMessage("SUCCESS",
                            "The file has been downloaded for display.",
                            org.apache.commons.io.FileUtils.readFileToString(theFile, Charset.defaultCharset().toString()));
                    message.append("fileId", fileId);
                    message.append("fileName", fileItem.getFileName());
                    message.append("fileTitle", fileItem.getTitle());
                    message.append("datasetId", datasetContext.getDataset().getId());

                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.FILE_DOWNLOAD, actualFileName + " (" + fileId + ")");

                } catch (FileNotFoundException exception) {
                    logger.debug("FileNotFoundException occurred in handleDisplay",
                            exception);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();  // very important
                    }
                }

            } catch (Exception exception) {
                logger.error("Exception occurred in handleDisplay", exception);
            }
        } else {
            logger.debug("ExternalAnalysis id is missing from parameters.");
            return buildJSONMessage("ERROR", "You must specify ExternalAnalysis id.", null);
        }

        return message;
    }

    /**
     * Handles uploading the file and inserting necessary information into the database.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @throws Exception an ServletException, IOException, FileUploadException or...
     */
    private void handleUpload(HttpServletRequest req,
                              HttpServletResponse resp,
                              DatasetContext datasetContext) throws Exception {

        // Check that we have a file upload request
        //This is deprecated as a result of changes to the API that they were unable to
        //implement and not break the API.  It's correct, but comes up as deprecated
        //because the base class has to have the static method.  Should be fixed in future
        //releases, but for the mean time is the correct implementation.
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if (logger.isDebugEnabled()) { logger.debug("isMultipart: " + isMultipart); }

        if (isMultipart) {
            List <org.apache.commons.fileupload.FileItem> items = datasetContext.getUploadItems();

            boolean isPaper = false;
            boolean isPreferredPaper = false;
            boolean isExternalAnalysis = false;

            PaperItem dsPaperItem = new PaperItem();
            //TODO these fields should probably go away (v3.0)
            dsPaperItem.setCitation(" "); //obtaining from user, but optional
            dsPaperItem.setTitle(" ");
            dsPaperItem.setAuthorNames(" ");
            dsPaperItem.setPaperYear(new Integer(0));

            ExternalAnalysisItem eaItem = new ExternalAnalysisItem();

            FileItem dsFileItem = new FileItem();

            // Process the uploaded items
            for (org.apache.commons.fileupload.FileItem uploadFileItem : items) {

                if (uploadFileItem.isFormField()) {
                    String name = uploadFileItem.getFieldName();
                    String value = uploadFileItem.getString();

                    if ((name.equals(FILE_TITLE_PARAM))
                            || (name.equals(EXTERNAL_ANALYSIS_TITLE_PARAM))) {
                        dsFileItem.setTitle(value);
                        if (name.equals(EXTERNAL_ANALYSIS_TITLE_PARAM)) {
                            isExternalAnalysis = true;
                        }
                    } else if (name.equals(FILE_DESC_PARAM)) {
                        dsFileItem.setDescription(value);
                    } else if (name.equals(PAPER_CITATION_PARAM)) {
                        isPaper = true;
                        dsPaperItem.setCitation(value);
                    } else if (name.equals(PAPER_ABSTRACT_PARAM)) {
                        isPaper = true;
                        dsPaperItem.setPaperAbstract(value);
                    } else if (name.equals(EXTERNAL_ANALYSIS_SKILL_MODEL_PARAM)) {
                        isExternalAnalysis = true;
                        Long skillModelId = null;
                        if (!value.equals("")) {
                            skillModelId = new Long(value).longValue();
                        }
                        eaItem.setSkillModelId(skillModelId);
                        eaItem.setSkillModelName(findSkillModelName(skillModelId));
                    } else if (name.equals(EXTERNAL_ANALYSIS_STAT_MODEL_PARAM)) {
                        isExternalAnalysis = true;
                        eaItem.setStatisticalModel(value);
                    } else if (name.equals("Submit")) {
                        //do nothing
                        logger.debug("Submit button");
                    } else if (name.equals("datasetId")) {
                        //do nothing
                        logger.debug("dataset id " + value);
                    } else if (name.equals(PREFERRED_CITATION_PARAM)) {
                       if (value.equals("on")) {
                           isPreferredPaper = true;
                        }
                    }  else {
                        logger.warn("Unexpected parameter:" + name + " :: " + value);
                    }
                } else {
                    // not a form field, this means it is time to create the file.
                    Item specificItemToCreate = null;
                    if (isPaper) {
                        specificItemToCreate = dsPaperItem;
                    }
                    if (isExternalAnalysis) {
                        specificItemToCreate = eaItem;
                    }
                    createFile(uploadFileItem, datasetContext, dsFileItem,
                            specificItemToCreate, isPreferredPaper);
                } // end else on actual file item
            } // end for loop

            //don't actually save the upload file to the session, null it out instead.
            datasetContext.setUploadItems(null);
        } else {
            logger.warn("Request is not a multi-part.");
            datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
            datasetContext.getFilesInfoContext().setFileMessage(
                    "Request is not multi-part.  Please contact DataShop help.");
        }

        // Once the file has been uploaded, redirect back to the Files Info servlet
        redirectToFilesInfo(resp, datasetContext);

    }

    /**
     * Helper function to determine skillModel name give skillModel id.
     * @param skillModelId the skill model id.
     * @return String skill model name.
     */

    private String findSkillModelName(Long skillModelId) {
        if (skillModelId == null) { return ""; }

        SkillModelDao smDao = DaoFactory.DEFAULT.getSkillModelDao();

        SkillModelItem smItem = smDao.get(skillModelId);

        return smItem.getSkillModelName();
    }

    /**
     * Helper function to handle file creation for handleUpload.
     * @param uploadFileItem the FileItem to parse
     * @param datasetContext the {@link DatasetInfoContext}
     * @param dsFileItem the Dataset file to persist
     * @param specificItemToCreate the Paper or ExternalAnalysis to persist
     * @param isPreferredPaper flag indicating if Paper is preferred citation
     * @throws Exception a FileUploadException or...
     */

    private void createFile(org.apache.commons.fileupload.FileItem uploadFileItem,
            DatasetContext datasetContext, FileItem dsFileItem,
            Item specificItemToCreate, boolean isPreferredPaper) throws Exception {

        String fieldName = uploadFileItem.getFieldName();
        String fileFullName = uploadFileItem.getName();

        if (fileFullName.indexOf('\\') >= 0) {
            fileFullName = fileFullName.substring(fileFullName.lastIndexOf('\\') + 1);
        }
        if (fileFullName.indexOf('/') >= 0) {
            fileFullName = fileFullName.substring(fileFullName.lastIndexOf('/') + 1);
        }
        String contentType = uploadFileItem.getContentType();
        long sizeInBytes = uploadFileItem.getSize();

        if (fieldName.equals(FILE_NAME_PARAM)) {
            DatasetItem datasetItem = datasetContext.getDataset();
            String subPath = FileUtils.cleanForFileSystem(datasetItem.getDatasetName());
            String wholePath = getBaseDir() + "/" + subPath;

            dsFileItem.setFilePath(subPath);
            dsFileItem.setAddedTime(new Date());
            if (contentType == null) {
                contentType = "";
            }
            dsFileItem.setFileType(contentType);
            dsFileItem.setOwner(datasetContext.getUser());
            dsFileItem.setFileSize(new Long(sizeInBytes));

            if (logger.isDebugEnabled()) {
                logger.debug("File: "  + dsFileItem);
            }

            boolean isPaper = false;
            boolean isExternalAnalysis = false;

            if (specificItemToCreate != null) {
                if (specificItemToCreate instanceof PaperItem) {
                    PaperItem dsPaperItem = (PaperItem)specificItemToCreate;
                    dsPaperItem.setOwner(datasetContext.getUser());
                    dsPaperItem.setAddedTime(new Date());
                    dsPaperItem.setFile(dsFileItem);
                    logger.debug("Paper: " + dsPaperItem);
                    isPaper = true;
                } else if (specificItemToCreate instanceof ExternalAnalysisItem) {
                    ExternalAnalysisItem eaItem = (ExternalAnalysisItem)specificItemToCreate;
                    eaItem.setOwner(datasetContext.getUser());
                    eaItem.setFile(dsFileItem);
                    logger.debug("ExternalAnalysis: " + eaItem);
                    isExternalAnalysis = true;
                }
            }

            //Check to make sure the user has selected a filed.
            if (fileFullName != null && fileFullName.length() > 0) {
                // Create the directory
                File newDirectory = new File(wholePath);
                if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                    FileUtils.makeWorldReadable(newDirectory);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The directory has been created."
                            + newDirectory.getAbsolutePath());
                    }

                    //check that file with this name does not already exist.
                    //if it does start incrementing adding _1, _2, to the end
                    //of the file until a name is found that doesn't exist.
                    List<String> fileList = Arrays.asList(newDirectory.list());
                    int extensionIndex = fileFullName.lastIndexOf(".");
                    String fileExt = "";
                    String fileName = "";
                    if (extensionIndex < 0) {
                        fileName = fileFullName;
                    } else if (extensionIndex == 0) {
                        fileExt = fileFullName.substring(0, fileFullName.length());
                    } else {
                        fileExt = fileFullName.substring(extensionIndex);
                        fileName = fileFullName.substring(0, extensionIndex);
                    }
                    int i = 0;
                    String fileNameToSave = fileFullName;
                    while (fileList.contains(fileNameToSave)) {
                        i++;
                        fileNameToSave = fileName + "_" + i + fileExt;
                    }
                    dsFileItem.setFileName(fileNameToSave);

                    // Write the file to the directory
                    File newFile = new File(wholePath, fileNameToSave);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Absolute path is " + newFile.getAbsolutePath());
                    }
                    uploadFileItem.write(newFile);

                    // Create a new item in the database
                    FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();
                    if (isPaper) {
                        PaperItem dsPaperItem = (PaperItem)specificItemToCreate;
                        filesInfoHelper.addPaperItem(datasetItem, dsPaperItem);
                        if (isPreferredPaper) {
                            if ((dsPaperItem.getCitation() != null)
                                    && (!dsPaperItem.getCitation().equals(""))) {
                                filesInfoHelper.addPreferredPaper(datasetItem, dsPaperItem);
                                datasetContext.getFilesInfoContext().setFileMessageType("SUCCESS");
                                datasetContext.getFilesInfoContext().setFileMessage(
                                        "Paper upload successful.");
                                UserLogger.log(datasetContext.getDataset(),
                                        datasetContext.getUser(), UserLogger.PAPER_ADD,
                                        "Adding paper (" + dsPaperItem.getId() + ") for file "
                                        + fileNameToSave + " (" + dsFileItem.getId() + ")");
                            } else {
                                logger.error(
                                        "Was unable to add this paper as preferred citation: "
                                        + "citation is empty ");
                                datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                                datasetContext.getFilesInfoContext().setFileMessage(
                                        "Adding a preferred paper failed "
                                        + "due to empty citation of the paper.");
                            }
                        } else {
                            datasetContext.getFilesInfoContext().setFileMessageType("SUCCESS");
                            datasetContext.getFilesInfoContext().setFileMessage(
                                    "Paper upload successful.");
                            UserLogger.log(datasetContext.getDataset(),
                                datasetContext.getUser(), UserLogger.PAPER_ADD,
                                "Adding paper (" + dsPaperItem.getId() + ") for file "
                                + fileNameToSave + " (" + dsFileItem.getId() + ")");
                        }

                    } else if (isExternalAnalysis) {
                        ExternalAnalysisItem eaItem = (ExternalAnalysisItem)specificItemToCreate;
                        filesInfoHelper.addExternalAnalysisItem(datasetItem, eaItem);
                        datasetContext.getFilesInfoContext().setFileMessageType("SUCCESS");
                        datasetContext.getFilesInfoContext().setFileMessage(
                                "External Analysis upload successful.");
                        UserLogger.log(datasetContext.getDataset(),
                                datasetContext.getUser(), UserLogger.EXTERNAL_ANALYSIS_ADD,
                                "Adding externalAnalysis (" + eaItem.getId() + ") for file "
                                + fileNameToSave + " (" + dsFileItem.getId() + ")");
                    } else {
                        datasetContext.getFilesInfoContext().setFileMessageType("SUCCESS");
                        datasetContext.getFilesInfoContext().setFileMessage(
                                "File upload successful.");
                        filesInfoHelper.addFileItem(datasetItem, dsFileItem);
                        UserLogger.log(datasetContext.getDataset(),
                                datasetContext.getUser(), UserLogger.FILE_ADD,
                                "Adding file " + fileNameToSave + " (" + dsFileItem.getId() + ")");
                    }
                } else {
                    logger.error("Was unable to create the new file directory: " + wholePath);
                    datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                    datasetContext.getFilesInfoContext().setFileMessage(
                            "File upload failed.  Please contact DataShop help.");
                }
            } else {
                logger.debug("The fileName cannot be null or empty.");
                datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                datasetContext.getFilesInfoContext().setFileMessage(
                        "Please specify a valid file name.");
            }
        }
    }

    /**
     * Helper function to handle the redirect to files info w/o losing the dataset id.
     * @param resp the {@link HttpServletResponse}
     * @param datasetContext the {@link DatasetInfoContext}
     * @throws IOException an IOException attempting to redirect.
     */
    private void redirectToFilesInfo(HttpServletResponse resp, DatasetContext datasetContext)
        throws IOException {
        resp.sendRedirect(FilesInfoServlet.SERVLET
                + "?datasetId=" + datasetContext.getDataset().getId());
    }

    /**
     * Handles deleting the file.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext Session information for the current dataset
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     */
    private void handleDeleteFile(HttpServletRequest req,
                              HttpServletResponse resp,
                              DatasetContext datasetContext)
            throws ServletException, IOException {

        //get parameters
        if (req.getParameter(FILE_ID_PARAM) != null
         && req.getParameter(FILE_NAME_PARAM) != null) {

            try {

                String fileName = req.getParameter(FILE_NAME_PARAM);
                String fileIdStr = req.getParameter(FILE_ID_PARAM);
                Integer fileId = new Integer(fileIdStr);

                if (logger.isDebugEnabled()) {
                    logger.debug("Deleting file " + fileName + " (" + fileId + ")");
                }

                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                FileItem dsFileItem = fileDao.get(fileId);

                DatasetItem datasetItem = datasetContext.getDataset();

                String subPath = dsFileItem.getFilePath();
                String wholePath = getBaseDir() + "/" + subPath;

                FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();
                // Delete the file from the file system
                File newFile = new File(wholePath, fileName);
                if (newFile.exists()) {
                    if (newFile.delete()) {
                        // Delete the file from the database

                        filesInfoHelper.deleteFileItem(datasetItem, dsFileItem);
                        datasetContext.getFilesInfoContext().setFileMessageType("SUCCESS");
                        datasetContext.getFilesInfoContext().setFileMessage(
                                    "The file has been deleted successfully.");
                        UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                UserLogger.FILE_DELETE,
                                "Deleting file " + fileName + " (" + fileId + ")");

                    } else {
                        logger.error("Unable to delete " + newFile.getAbsoluteFile());
                        datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                        datasetContext.getFilesInfoContext().setFileMessage("File Delete Failed.");
                    }
                } else {
                    filesInfoHelper.deleteFileItem(datasetItem, dsFileItem);
                    logger.warn("Attempting to delete " + newFile.getAbsoluteFile()
                            + " failed as file does not exist");
                    datasetContext.getFilesInfoContext().setFileMessageType("WARN");
                    datasetContext.getFilesInfoContext().setFileMessage(
                            "File Not Found. Entry removed from list.");
                }

                // Once the file has been deleted, redirect back to the Dataset Info Report servlet
                redirectToFilesInfo(resp, datasetContext);
                return;

            } catch (Exception exception) {
                logger.error("Exception occurred in handleDelete", exception);
                datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                datasetContext.getFilesInfoContext().setFileMessage("An Unknown Error Occurred.");
            }

        } else {
            logger.debug("File id or name is missing from parameters.");
            redirectToFilesInfo(resp, datasetContext);
        }
    }


    /**
     * Handles deleting the paper.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     */
    private void handleDeletePaper(HttpServletRequest req,
                              HttpServletResponse resp,
                              DatasetContext datasetContext)
            throws ServletException, IOException {

        //get parameters
        if (req.getParameter(PAPER_ID_PARAM) != null) {

            try {
                String paperIdStr = req.getParameter(PAPER_ID_PARAM);
                Integer paperId = new Integer(paperIdStr);

                PaperDao paperDao = DaoFactory.DEFAULT.getPaperDao();
                PaperItem dsPaperItem = paperDao.get(paperId);

                Integer fileId = 0;
                if (dsPaperItem.getFile() != null) {
                    fileId = (Integer)dsPaperItem.getFile().getId();
                }
                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                FileItem dsFileItem = fileDao.get(fileId);
                if (dsFileItem != null) {
                    String fileName = dsFileItem.getFileName();

                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleting Paper " + paperId + " for file "
                                + dsFileItem.getFileName() + " (" + fileId + ")");
                    }

                    DatasetItem datasetItem = datasetContext.getDataset();

                    String subPath = dsFileItem.getFilePath();
                    String wholePath = getBaseDir() + "/" + subPath;

                    // Delete the file from the file system
                    File newFile = new File(wholePath, fileName);
                    FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();
                    if (newFile.exists()) {
                        if (newFile.delete()) {
                            // Delete the file from the database
                            filesInfoHelper.deletePaperItem(datasetItem, dsPaperItem);
                            datasetContext.getFilesInfoContext().setFileMessageType("SUCCESS");
                            datasetContext.getFilesInfoContext().setFileMessage(
                                    "The paper has been deleted successfully.");
                            UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                    UserLogger.PAPER_DELETE,
                                    "Deleting paper (" + paperId + ") for file "
                                    + fileName + " (" + fileId + ")");

                        } else {
                            logger.error("Unable to delete " + newFile.getAbsoluteFile());
                            datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                            datasetContext.getFilesInfoContext().setFileMessage(
                                    "File Delete Failed.");
                        }
                    } else {
                        filesInfoHelper.deletePaperItem(datasetItem, dsPaperItem);
                        logger.warn("Attempting to delete " + newFile.getAbsoluteFile()
                                + " failed as file does not exist");
                        datasetContext.getFilesInfoContext().setFileMessageType("WARN");
                        datasetContext.getFilesInfoContext().setFileMessage(
                                "File Not Found. Entry removed from list.");
                    }
                }
                // Once the file has been deleted, redirect back to the Dataset Info Report servlet
                redirectToFilesInfo(resp, datasetContext);

            } catch (Exception exception) {
                logger.error("Exception occurred in handleDelete", exception);
            }
        } else {
            logger.debug("File id or name is missing from parameters.");
            redirectToFilesInfo(resp, datasetContext);
        }
    }

    /**
     * Handles deleting the external analysis.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     */
    private void handleDeleteExternalAnalysis(HttpServletRequest req,
            HttpServletResponse resp,
            DatasetContext datasetContext)
                    throws ServletException, IOException {

        //get parameters
        if (req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM) != null) {

            try {
                Integer externalAnalysisId =
                        new Integer(req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM));

                ExternalAnalysisDao eaDao = DaoFactory.DEFAULT.getExternalAnalysisDao();
                ExternalAnalysisItem eaItem = eaDao.get(externalAnalysisId);

                Integer fileId = 0;
                if (eaItem.getFile() != null) {
                    fileId = (Integer)eaItem.getFile().getId();
                }
                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                FileItem dsFileItem = fileDao.get(fileId);
                if (dsFileItem != null) {
                    String fileName = dsFileItem.getFileName();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleting ExternalAnalysis " + externalAnalysisId
                                + " for file " + dsFileItem.getFileName() + " (" + fileId + ")");
                    }

                    DatasetItem datasetItem = datasetContext.getDataset();

                    String subPath = dsFileItem.getFilePath();
                    String wholePath = getBaseDir() + "/" + subPath;

                    // Delete the file from the file system
                    File newFile = new File(wholePath, fileName);
                    FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();

                    if (newFile.exists()) {
                        if (newFile.delete()) {
                            // Delete the file from the database
                            filesInfoHelper.deleteExternalAnalysisItem(datasetItem, eaItem);
                            datasetContext.getFilesInfoContext().setFileMessageType("SUCCESS");
                            datasetContext.getFilesInfoContext().setFileMessage(
                                    "The external analysis has been deleted successfully.");
                            UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                    UserLogger.EXTERNAL_ANALYSIS_DELETE,
                                    "Deleting externalAnalysis (" + externalAnalysisId
                                    + ") for file " + fileName + " (" + fileId + ")");

                        } else {
                            logger.error("Unable to delete " + newFile.getAbsoluteFile());
                            datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                            datasetContext.getFilesInfoContext().setFileMessage(
                                    "File Delete Failed.");
                        }
                    } else {
                        filesInfoHelper.deleteExternalAnalysisItem(datasetItem, eaItem);
                        logger.warn("Attempting to delete " + newFile.getAbsoluteFile()
                                + " failed as file does not exist");
                        datasetContext.getFilesInfoContext().setFileMessageType("WARN");
                        datasetContext.getFilesInfoContext().setFileMessage(
                                "File Not Found. Entry removed from list.");
                    }
                }
                // Once the file has been deleted, redirect back to the Dataset Info Report servlet
                redirectToFilesInfo(resp, datasetContext);

            } catch (Exception exception) {
                logger.error("Exception occurred in handleDelete", exception);
            }
        } else {
            logger.debug("File id or name is missing from parameters.");
            redirectToFilesInfo(resp, datasetContext);
        }
    }

    /**
     * Modifies the given file's data.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @return JSONObject indicating success or failure
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     */
    private JSONObject handleEditFile(HttpServletRequest req,
                              HttpServletResponse resp,
                              DatasetContext datasetContext)
            throws ServletException, IOException {

        JSONObject message = new JSONObject();

        //get parameters
        if (req.getParameter(FILE_ID_PARAM) != null) {

            try {
                String fileIdStr = req.getParameter(FILE_ID_PARAM);
                Integer fileId = new Integer(fileIdStr);
                StringBuffer loggingText = new StringBuffer();

                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                FileItem dsFileItem = fileDao.get(fileId);

                if (logger.isDebugEnabled()) {
                    logger.debug("Modifying file "
                            + dsFileItem.getFileName() + " (" + fileId + ")");
                }

                boolean modified = false;

                String fileTitle = req.getParameter(FILE_TITLE_PARAM);
                if (fileTitle != null) {
                    loggingText.append("Changed title from '" + dsFileItem.getTitle() + "'"
                            + " to '" + fileTitle + "'  ");
                    dsFileItem.setTitle(fileTitle);
                    modified = true;
                }

                String fileDesc = req.getParameter(FILE_DESC_PARAM);
                if (fileDesc != null) {
                    loggingText.append("Changed description from '"
                            + dsFileItem.getDescription() + "'"
                            + " to '" + fileDesc + "'  ");
                    dsFileItem.setDescription(fileDesc);
                    modified = true;
                }

                if (modified) {
                    fileDao.saveOrUpdate(dsFileItem);
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.FILE_MODIFY,
                            loggingText.toString());
                }

                message = buildJSONMessage("SUCCESS",
                        "The File has been modified successfully.", fileIdStr);
           } catch (Exception exception) {
                logger.error("Exception occurred in handleEdit", exception);
                datasetContext.getFilesInfoContext().setFileMessageType("ERROR");
                datasetContext.getFilesInfoContext().setFileMessage(
                        "An unknown error occurred during update.");
            }
        } else {
            logger.debug("File id or name is missing from parameters.");
            redirectToFilesInfo(resp, datasetContext);
        }

        return message;
    }

    /**
     * Modifies the given file's data.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @return String of success.
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     * @throws JSONException an exception occurred creating the JSON objects.
     */
    private JSONObject handleEditPaper(HttpServletRequest req,
                                 HttpServletResponse resp,
                                 DatasetContext datasetContext)
            throws ServletException, IOException, JSONException {
        JSONObject message = null;
        String paperIdStr = req.getParameter(PAPER_ID_PARAM);
        try {
            Integer paperId = new Integer(paperIdStr);

            PaperDao paperDao = DaoFactory.DEFAULT.getPaperDao();
            PaperItem dsPaperItem = paperDao.get(paperId);

            Integer fileId = (Integer)dsPaperItem.getFile().getId();
            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
            FileItem dsFileItem = fileDao.get(fileId);

            if (logger.isDebugEnabled()) {
                logger.debug("Modifying Paper " + paperId + " for file "
                        + dsFileItem.getFileName() + " (" + fileId + ")");
            }

            boolean modified = false;
            boolean modifiedDataset = false;
            StringBuffer loggingText = new StringBuffer("Paper (" + paperId + ") ");

            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem datasetItem = (DatasetItem)datasetDao.get(
                    (Integer)(datasetContext.getDataset().getId()));
            String preferredCitation = req.getParameter(PREFERRED_CITATION_PARAM);
            // modify dataset if preferredCitation is checked
            if (preferredCitation.equals("on")) {
                if ((req.getParameter(PAPER_CITATION_PARAM) == null)
                        || (req.getParameter(PAPER_CITATION_PARAM).equals(""))) {
                    return buildJSONMessage("ERROR",
                            "You cannot specify a paper as the preferred citation "
                            + "if there is no citation text. "
                            + "Please either enter a citation, "
                            + "uncheck the checkbox or click cancel.", paperIdStr);
                } else {
                    if ((datasetItem.getPreferredPaper() == null)
                       || (!(datasetItem.getPreferredPaper().getId().equals(
                               dsPaperItem.getId())))) {
                                datasetItem.setPreferredPaper(dsPaperItem);
                                modifiedDataset = true;
                        }
                }
            } else {
                if ((datasetItem.getPreferredPaper() != null)
                        && (datasetItem.getPreferredPaper().getId()
                                .equals(dsPaperItem.getId()))) {
                    datasetItem.setPreferredPaper(null);
                    modifiedDataset = true;
                }
            }
            if (modifiedDataset) {
                UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                        UserLogger.PAPER_MODIFY,
                        loggingText.toString());
                datasetDao.saveOrUpdate(datasetItem);
            }
            String paperCitation = req.getParameter(PAPER_CITATION_PARAM);
            if (paperCitation != null) {
                loggingText.append("changed citation from '" + dsPaperItem.getCitation() + "'"
                        + " to '" + paperCitation + "'  ");
                dsPaperItem.setCitation(paperCitation);
                modified = true;
            }

            String paperAbstract = req.getParameter(PAPER_ABSTRACT_PARAM);
            if (paperAbstract != null) {
                loggingText.append("changed abstract from '"
                        + dsPaperItem.getPaperAbstract() + "'"
                        + " to '" + paperAbstract + "'  ");
                dsPaperItem.setPaperAbstract(paperAbstract);
                modified = true;
            }

            if (modified) {
                UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                        UserLogger.PAPER_MODIFY,
                        loggingText.toString());
                paperDao.saveOrUpdate(dsPaperItem);
            }
            message = buildJSONMessage("SUCCESS",
                    "The paper has been modified successfully.", paperIdStr);

            message.append("paperCitation", dsPaperItem.getCitation());
            message.append("paperAbstract", dsPaperItem.getPaperAbstract());
            if (datasetItem.getPreferredPaper() != null) {
                message.append("preferredCitation",
                    datasetItem.getPreferredPaper().getId()
                    .equals(dsPaperItem.getId()) ? "true" : "false");
            } else {
                message.append("preferredCitation", "false");
            }
        } catch (Exception exception) {
            logger.error("Exception occurred in handleEdit", exception);
        }

        return message;
    }

    /**
     * Create a message as a JSON object.
     * @param messageType The type of message ('ERROR', 'SUCCESS', 'UNAUTHORIZED', 'MESSAGE');
     * @param message The text of the message.
     * @param value The value of message, usually refers to an update/delete/save.
     * @return The message as a JSON object.
     * @throws JSONException a JSON exception building the JSONObject
     */
    private JSONObject buildJSONMessage(String messageType, String message, String value)
                throws JSONException {
        logger.debug("Generating a return message as a JSON object.");

        JSONObject messageJSON = new JSONObject();
        messageJSON.append("messageType", messageType);
        messageJSON.append("message", message);
        messageJSON.append("value", value);
        return messageJSON;
    }

    /**
     * Modifies the given file's data.
     * @param req the HTTP servlet request
     * @param resp the HTTP servlet response
     * @param datasetContext the session context information
     * @return String of success.
     * @throws ServletException a possible exception
     * @throws IOException an IO exception
     * @throws JSONException an exception occurred creating the JSON objects.
     */
    private JSONObject handleEditExternalAnalysis(HttpServletRequest req,
            HttpServletResponse resp, DatasetContext datasetContext)
                    throws ServletException, IOException, JSONException {

        JSONObject message = null;

        try {
            String externalAnalysisIdStr = req.getParameter(EXTERNAL_ANALYSIS_ID_PARAM);
            Integer externalAnalysisId = new Integer(externalAnalysisIdStr);

            ExternalAnalysisDao eaDao = DaoFactory.DEFAULT.getExternalAnalysisDao();
            ExternalAnalysisItem eaItem = eaDao.get(externalAnalysisId);

            Integer fileId = (Integer)eaItem.getFile().getId();
            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
            FileItem dsFileItem = fileDao.get(fileId);

            if (logger.isDebugEnabled()) {
                logger.debug("Modifying ExternalAnalysis " + externalAnalysisId + " for file "
                        + dsFileItem.getFileName() + " (" + fileId + ")");
            }

            boolean modified = false;
            StringBuffer loggingText =
                    new StringBuffer("ExternalAnalysis (" + externalAnalysisId + ") ");

            String fileTitle = req.getParameter(EXTERNAL_ANALYSIS_TITLE_PARAM);
            if (fileTitle != null) {
                loggingText.append("Changed title from '" + dsFileItem.getTitle() + "'"
            + " to '" + fileTitle + "'  ");
                dsFileItem.setTitle(fileTitle);
                modified = true;
            }

            String fileDesc = req.getParameter(FILE_DESC_PARAM);
            if (fileDesc != null) {
                loggingText.append("Changed description from '"
                            + dsFileItem.getDescription() + "'"
                            + " to '" + fileDesc + "'  ");
                dsFileItem.setDescription(fileDesc);
                modified = true;
            }

            if (modified) {
                fileDao.saveOrUpdate(dsFileItem);
                UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                        UserLogger.FILE_MODIFY, loggingText.toString());
            }

            modified = false;
            String skillModelName = req.getParameter(EXTERNAL_ANALYSIS_SKILL_MODEL_PARAM);
            if (skillModelName != null) {
                loggingText.append("changed skillModelName from '" + eaItem.getSkillModelName()
                        + "'" + " to '" + skillModelName + "'  ");
                eaItem.setSkillModelName(skillModelName);
                modified = true;
            }

            String statModel = req.getParameter(EXTERNAL_ANALYSIS_STAT_MODEL_PARAM);
            if (statModel != null) {
                loggingText.append("changed statisticalModel from '"
                        + eaItem.getStatisticalModel() + "'"
                        + " to '" + statModel + "'  ");
                eaItem.setStatisticalModel(statModel);
                modified = true;
            }

            if (modified) {
                UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                        UserLogger.EXTERNAL_ANALYSIS_MODIFY,
                        loggingText.toString());
                eaDao.saveOrUpdate(eaItem);
            }
            message = buildJSONMessage("SUCCESS",
                    "The ExternalAnalysis has been modified successfully.", externalAnalysisIdStr);

            message.append("skillModelName", eaItem.getSkillModelName());
            message.append("statisticalModel", eaItem.getStatisticalModel());

        } catch (Exception exception) {
            logger.error("Exception occurred in handleEdit", exception);
        }

        return message;
    }

}
