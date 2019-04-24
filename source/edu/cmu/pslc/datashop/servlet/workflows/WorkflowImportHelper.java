/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
/**
 * Helper class to handle Workflow Imports.
 * @author Mike Komisin.
 * @version $Revision:  $ <BR>
 * Last modified by: $Author: $ <BR>
 * Last modified on: $Date: $ <!-- $KeyWordsOff: $ -->
 */
public class WorkflowImportHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Debug logging. */
    private static Logger staticLogger = Logger.getLogger(WorkflowHelper.class.getName());
    /** The name of the component info file which describes inputs, outputs, and options in plain English. */
    private static final String COMPONENT_INFO_FILE_NAME = "info.xml";
    /** The maximum character length allowed for a single column header. */
    public static final Integer MAX_COLUMN_HEADER_LABEL_LENGTH = 3000;

    /** File type csv. */
    public static final String FILE_TYPE_CSV = "csv";
    /** File type tab-delimited. */
    public static final String FILE_TYPE_TAB_DELIMITED = "tab-delimited";
    /** File type text. */
    public static final String FILE_TYPE_TEXT = "text";
    /** File type "file" (most generic type). */
    public static final String FILE_TYPE_FILE = "file";

    public WorkflowImportHelper() {

    }

    /**
     * Set the component instance dirty bits.
     * @param wcInstanceItem the workflow component instance item
     * @param value the dirty bit to set to true
     */
    public void triggerDirtyBit(WorkflowComponentInstanceItem wcInstanceItem, String value) {
        if (value.equalsIgnoreCase(ComponentHelper.DIRTY_FILE)) {
            wcInstanceItem.setDirtyFile(true);
        } else if (value.equalsIgnoreCase(ComponentHelper.DIRTY_OPTION)) {
            wcInstanceItem.setDirtyOption(true);
        } else if (value.equalsIgnoreCase(ComponentHelper.DIRTY_ADD_CONNECTION)) {
            wcInstanceItem.setDirtyAddConnection(true);
        } else if (value.equalsIgnoreCase(ComponentHelper.DIRTY_DELETE_CONNECTION)) {
            wcInstanceItem.setDirtyDeleteConnection(true);
        } else if (value.equalsIgnoreCase(ComponentHelper.DIRTY_ANCESTOR)) {
            wcInstanceItem.setDirtyAncestor(true);
        } else if (value.equalsIgnoreCase(ComponentHelper.DIRTY_SELECTION)) {
            wcInstanceItem.setDirtyOption(true);
            wcInstanceItem.setDirtySelection(true);
        }
    }


    private void extractImportFileMetadata(WorkflowHelper workflowHelper,
         HttpServletRequest req, WorkflowItem workflowItem, WorkflowFileItem dsFileItem,
             String baseDir, String componentsDir) {
         if (workflowItem != null && dsFileItem != null) {
             String fileTypeString = dsFileItem.getTitle();

             req.setAttribute("fileId", dsFileItem.getId().toString());
             req.setAttribute("fileName", dsFileItem.getFileName());
             req.setAttribute("fileIndex", dsFileItem.getDescription());
             req.setAttribute("fileLabel", dsFileItem.getTitle());

             String newPath = WorkflowFileUtils.sanitizePath(dsFileItem.getFullFileName(baseDir));
             File importFile = new File(newPath);
             logger.trace("Retrieving file for workflow (" + workflowItem.getId() + ") and storing in " + newPath);

             if (importFile.exists() && importFile.isFile()) {
                 String mimeType = null;
                 try {
                     mimeType = WorkflowFileUtils.getMimeType(importFile);

                     if (mimeType != null && mimeType.matches("text/.*")) {
                         String commonSchemasDir = WorkflowFileUtils.getStrictDirFormat(componentsDir)
                                 + "CommonSchemas/";
                         // Assume it's a table if it's text,
                         // and try to get the column headers.
                         String delim = null;

                         if (fileTypeString.equalsIgnoreCase(WorkflowImportHelper.FILE_TYPE_CSV)) {
                             delim = ",";
                         } else if (fileTypeString.equalsIgnoreCase(WorkflowImportHelper.FILE_TYPE_TAB_DELIMITED)) {
                             delim = "\t";
                         } else if (ComponentHierarchyHelper.isCastable(commonSchemasDir + "TableTypes.xsd", fileTypeString,
                                 WorkflowImportHelper.FILE_TYPE_TAB_DELIMITED)) {
                             delim = "\t";
                         } else if (ComponentHierarchyHelper.isCastable(commonSchemasDir + "TableTypes.xsd", fileTypeString,
                                 WorkflowImportHelper.FILE_TYPE_CSV)) {
                             delim = ",";
                         }

                         LinkedHashMap<String, Integer> columnHeaders = WorkflowImportHelper.getColumnHeaders(importFile, delim);


                         if (!columnHeaders.isEmpty()) {
                             req.setAttribute("columnHeaders", Collections.synchronizedMap(columnHeaders));
                         }

                         ComponentFileDao cfDao = DaoFactory.DEFAULT.getComponentFileDao();
                         List<ComponentFileItem> cfItems = cfDao.findByFile(dsFileItem);
                         if (!cfItems.isEmpty()) {
                             DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                             DatasetItem datasetItem = null;
                             if (cfItems.get(0).getDataset() != null) {
                                 dsDao.get((Integer) cfItems.get(0).getDataset().getId());
                             }
                             if (datasetItem != null) {
                                 req.setAttribute("datasetLink", datasetItem.getId().toString().trim());
                                 req.setAttribute("datasetName", WorkflowFileUtils.htmlEncode(datasetItem.getDatasetName()));
                             }
                         }
                     }
                 } catch (IOException e) {
                     logger.error("Could not probe file type: " + importFile.getAbsolutePath());
                 }
             }
         } else {
             String errorMessage = "Could not retrieve file.";
             logger.error(errorMessage);
         }
     }

    /**
     * Convert the column headers to a JSONArray and return it.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @return the JSONArray of column headers
     */
    JSONArray getColumnsAsJson(HttpServletRequest req, HttpServletResponse resp) {
        Map<String, Integer> columnHeaders = (Map<String, Integer>) req.getAttribute("columnHeaders");
        // Break the header into the name and index
        Integer columnHeaderDuplicateIndex = 0;
        JSONArray columnNameArray = new JSONArray();
        if (columnHeaders != null) {
            // This file has column headers
            try {
                for (String myKey : columnHeaders.keySet()) {
                    JSONObject columnName = new JSONObject();

                    // Model names
                    columnName.put("id", "header" + columnHeaderDuplicateIndex);
                    columnName.put("name", myKey);
                    // Max Opportunity
                    columnName.put("index", columnHeaders.get(myKey) + "");
                    columnNameArray.put(columnName);

                    columnHeaderDuplicateIndex++;
                }
            } catch (JSONException e) {
                logger.error("Error retrieving header data.");
                return null;
            }
        }
        return columnNameArray;
    }

    /**
     * Returns a map of column header names and their index.
     * @param importFile the file
     * @param columnDelim the regular expression delimiter
     * @return the map of column header names and their index
     */
     public static LinkedHashMap<String, Integer> getColumnHeaders(File file, String columnDelim) {
         HashMap<String, Integer> columnHeaders = new HashMap<String, Integer>();
         FileReader fReader;
         BufferedReader bReader = null;
         if (file != null && file.exists() && file.isFile()) {
             try {
                 fReader = new FileReader(file);
                 bReader = new BufferedReader(fReader);


                 String firstLine = bReader.readLine();
                 Integer columnIndex = 0;
                 if (firstLine != null && !firstLine.isEmpty() && columnDelim != null) {
                     staticLogger.trace("First line: " + firstLine);
                     staticLogger.trace("Delim: " + columnDelim);
                     String[] regexpSplit = firstLine.split(columnDelim);
                     if (regexpSplit.length > 0) {
                         for (String header : regexpSplit) {
                             String trimmedHeader = header.trim();
                             if (!columnHeaders.containsKey(trimmedHeader)) {
                                 if (trimmedHeader.length() < MAX_COLUMN_HEADER_LABEL_LENGTH) {
                                     columnHeaders.put(trimmedHeader, columnIndex);
                                 } else {
                                     columnHeaders.put(
                                         trimmedHeader.substring(0, MAX_COLUMN_HEADER_LABEL_LENGTH),
                                             columnIndex);
                                 }
                             }
                             columnIndex++;
                         }
                     }
                 }
             } catch (FileNotFoundException e) {
                 staticLogger.error("File not found: "
                         + file.getAbsolutePath());
             } catch (IOException e) {
                 staticLogger.error("IOException for file: "
                         + file.getAbsolutePath());
             } finally {
                 if (bReader != null) {
                     try {
                         bReader.close();
                     } catch (IOException e) { }
                 }
             }
         }

         return WorkflowHelper.getSortedMapByValues(columnHeaders);
     }

     /**
      * Handle the request to upload a file and check the user's access to upload a file to the workflow.
      * Only DS admins and the workflow owner can add a file to a workflow.
      *
      * @param req the servlet request
      * @param resp the servlet response
      * @param items the multi-part form data
      * @param baseDir the directory where the file can be found
      * @param userItem the user item
      * @return whether or not the user is allowed to upload a file to the workflow
      */
     public Boolean handleUploadRequest(WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper, HttpServletRequest req, HttpServletResponse resp,
             List<org.apache.commons.fileupload.FileItem> items, String baseDir, String componentsDir,
                 UserItem userItem) {

         WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
         // Long
         Long workflowId = null;
         // Unique identifier for this workflow, e.g. Component1234
         String componentId = null;
         // Type of file being uploaded (as defined in the XML).
         String fileIndex = null;
         // Label of the file being uploaded
         String fileLabel = null;

         Boolean isOwner = false;
         Boolean isAdmin = false;

         if (items == null) {
             logger.error("File was not received.");

         } else {
             logger.debug("Handling multi-part form data.");
             WorkflowItem workflowItem = null;
             for (org.apache.commons.fileupload.FileItem uploadFileItem : items) {

                 if (uploadFileItem.isFormField()) {
                     logger.trace("Upload Form field: " + uploadFileItem.getFieldName() + ", "
                             + uploadFileItem.getString());
                     // Put text-based form fields into the request
                     req.setAttribute(uploadFileItem.getFieldName(), uploadFileItem.getString());

                     if (uploadFileItem.getFieldName().equalsIgnoreCase("workflowId")) {

                         String workflowIdString = uploadFileItem.getString();
                         if (workflowIdString.matches("\\d+")) {
                             workflowId = Long.parseLong(workflowIdString);
                             workflowItem = workflowDao.get(workflowId);
                         }
                     }

                     if (uploadFileItem.getFieldName().equalsIgnoreCase("componentId")) {

                         componentId = uploadFileItem.getString();
                     }

                     if (uploadFileItem.getFieldName().equalsIgnoreCase("fileIndex")) {
                         fileIndex = uploadFileItem.getString();
                     }

                     if (uploadFileItem.getFieldName().equalsIgnoreCase("fileLabel")) {
                         fileLabel = uploadFileItem.getString();
                     }


                 }
             } // end for loop

             if (workflowItem != null && workflowItem.getOwner() != null
                     && workflowItem.getOwner().getId().equals(userItem.getId())) {
                 isOwner = true;
             } else if (workflowItem != null && userItem.getAdminFlag()) {
                 isAdmin = true;
             }

             // The workflow is not null and the user has permission to upload a file to the workflow specified in the
             // multi-part form data
             if ((isAdmin || isOwner) && componentId != null) {
                 WorkflowFileItem newFileItem = null;
                 try {
                     for (org.apache.commons.fileupload.FileItem uploadFileItem : items) {

                         if (!uploadFileItem.isFormField()) {

                             // Process files separately
                             Boolean isValidFile = WorkflowFileUtils.checkFileIsSupported(uploadFileItem);
                             if (!isValidFile) {
                                 logger.error("Uploaded file is not valid.");
                                 continue;
                             }

                             newFileItem = saveFileUpload(workflowFileHelper, workflowHelper, workflowId, componentId, fileLabel, fileIndex, uploadFileItem,
                                     baseDir, userItem);
                             workflowHelper.logWorkflowComponentUserAction(userItem, workflowItem, null,
                                     componentId, null, null, fileIndex,
                                     null, newFileItem, null, WorkflowHelper.LOG_UPLOAD_FILE,
                                         "path : " + newFileItem.getFilePath() + ", "
                                         + "name : " + newFileItem.getFileName());
                             extractImportFileMetadata(workflowHelper, req, workflowItem, newFileItem, baseDir, componentsDir);

                             req.setAttribute("requestingMethod", "WorkflowEditorServlet.fileUpload");
                             req.setAttribute("workflowId", workflowId.toString());
                         }

                     } // end for loop
                 } catch (Exception exception) {
                     String errorMessage = "Could not retrieve file.";
                     logger.error(errorMessage);
                     logger.error(exception.toString());
                 }
             } else {
                 // The workflow is not owned by the user and the user is not an admin.
             }
         }

         return (isOwner || isAdmin);
     }

    /**
      * Saves an uploaded file to the system.
      *
      * @param workflowId the workflow id
      * @param componentId the component id
      * @param fileTitle the file title (e.g. A, file1, myFile, etc.)
      * @param fileDesc the file description (e.g. Student-step, transaction, etc)
      * @param uploadFileItem the apache file object (not a file item)
      * @param baseDir the files base directory, e.g. /datashop/files
      * @param userItem the user item
      * @return the FileItem
      * @throws Exception any exception
      */
     public WorkflowFileItem saveFileUpload(WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper,
             Long workflowId, String componentId, String fileTitle, String fileDesc,
         org.apache.commons.fileupload.FileItem uploadFileItem, String baseDir, UserItem userItem)
             throws Exception {

         Boolean successFlag = false;
         WorkflowFileItem dsFileItem = null;
         WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
         WorkflowItem workflowItem = workflowDao.get(workflowId);
         if (workflowItem != null) {
             String subPath = WorkflowFileUtils.sanitizePath(
                 WorkflowFileUtils.getWorkflowsDir(workflowId) + componentId + "/output/");
             String fieldName = uploadFileItem.getFieldName();
             String fileFullName = WorkflowFileUtils.sanitizePath(uploadFileItem.getName());

             if (fileFullName.indexOf('/') >= 0) {
                 fileFullName = fileFullName.substring(fileFullName.lastIndexOf('/') + 1);
             }

             if (fieldName.equals("file")) {

                 // Check to make sure the user has selected a file.
                 if (fileFullName != null && fileFullName.length() > 0) {
                     // Create the directory
                     String wholePath = WorkflowFileUtils.sanitizePath(baseDir + "/" + subPath);
                     File newDirectory = new File(wholePath);
                     if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                         FileUtils.makeWorldReadable(newDirectory);
                         if (logger.isDebugEnabled()) {
                             logger.trace("saveFileUpload: The directory has been created." + newDirectory.getAbsolutePath());
                         }

                         // Keep track of files at the component level
                         ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                         workflowFileHelper.updateFileMappings(compFileDao, workflowItem, componentId, false, baseDir);

                         // Save the file to the file system.
                         String newFileName = fileFullName;
                         File newFile = null;
                         File fileParent = new File(wholePath);
                         if (fileParent.exists() && fileParent.isDirectory()) {
                             List<String> fileList = Arrays.asList(fileParent.list());
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

                             while (fileList.contains(newFileName)) {
                                 i++;
                                 newFileName = fileName + "_" + i + fileExt;
                             }

                             newFile = new File(wholePath, newFileName);
                             if (!newFile.exists()) {
                                 uploadFileItem.write(newFile);
                             }
                         }

                         if (newFile != null && newFile.exists()) {

                             // Save the uploaded file info to the database.
                             Integer fileId = WorkflowFileHelper.createOrGetWorkflowFile(
                                 workflowItem, userItem, baseDir,
                                     newFile, fileTitle, "0", null, componentId, false);
                             WorkflowFileDao workflowFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                             if (fileId != null) {
                                 dsFileItem = workflowFileDao.get(fileId);

                                 if (dsFileItem != null) {
                                     ComponentFileItem cfItem = compFileDao.findByWorkflowAndFile(workflowItem, dsFileItem);

                                     if (cfItem == null) {
                                         cfItem = new ComponentFileItem(workflowItem,  componentId, null, dsFileItem);
                                         compFileDao.saveOrUpdate(cfItem);
                                     } else {
                                         cfItem = compFileDao.findByWorkflowAndFile(workflowItem, dsFileItem);
                                         Hibernate.initialize(cfItem);
                                         cfItem.setComponentId(componentId);
                                         cfItem.setDataset(null);
                                         compFileDao.saveOrUpdate(cfItem);
                                     }

                                     successFlag = true;
                                 }
                             }
                         }

                     } else {
                         logger.error("saveFileUpload: Creating directory failed " + newDirectory);
                     }
                 } else {
                     logger.error("saveFileUpload: The fileName cannot be null or empty.");
                 }
             }
         }

         if (successFlag) {
             return dsFileItem;
         } else {
             logger.error("saveFileUpload: Could not create workflow file.");
             return null;
         }
     }

     /**
      * Saves a local file to the system.
      *
      * @param workflowId the workflow id
      * @param componentId the component id
      * @param fileTitle the file title (e.g. A, file1, myFile, etc.)
      * @param fileDesc the file description (e.g. Student-step, transaction, etc)
      * @param file on the local system
      * @param baseDir the files base directory, e.g. /datashop/files
      * @param userItem the user item
      * @return the FileItem
     * @throws IOException
     * @throws Exception any exception
      */
     public WorkflowFileItem saveFileUpload(WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper,
             WorkflowItem workflowItem, String componentId, String fileTitle,
             File localFile, String baseDir, UserItem userItem) throws IOException
             {

         Boolean successFlag = false;
         WorkflowFileItem dsFileItem = null;
         if (workflowItem != null) {
             String subPath = WorkflowFileUtils.sanitizePath(
                 WorkflowFileUtils.getWorkflowsDir(workflowItem.getId()) + componentId + "/output/");
             //String fieldName = uploadFileItem.getFieldName();
             String fileFullName = WorkflowFileUtils.sanitizePath(FilenameUtils.getName(localFile.getName()));
             if (fileFullName.indexOf('/') >= 0) {
                 fileFullName = fileFullName.substring(fileFullName.lastIndexOf('/') + 1);
             }

                 // Check to make sure the user has selected a file.
                 if (fileFullName != null && fileFullName.length() > 0) {
                     // Create the directory
                     String wholePath = WorkflowFileUtils.sanitizePath(baseDir + "/" + subPath);
                     File newDirectory = new File(wholePath);
                     if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                         FileUtils.makeWorldReadable(newDirectory);
                         if (logger.isDebugEnabled()) {
                             logger.trace("saveFileUpload: The directory has been created." + newDirectory.getAbsolutePath());
                         }

                         // Keep track of files at the component level
                         ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                         workflowFileHelper.updateFileMappings(compFileDao, workflowItem, componentId, false, baseDir);

                         // Save the file to the file system.
                         String newFileName = fileFullName;
                         File newFile = null;
                         File fileParent = new File(wholePath);
                         if (fileParent.exists() && fileParent.isDirectory()) {
                             List<String> fileList = Arrays.asList(fileParent.list());
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

                             while (fileList.contains(newFileName)) {
                                 i++;
                                 newFileName = fileName + "_" + i + fileExt;
                             }

                             newFile = new File(wholePath, newFileName);
                             if (!newFile.exists()) {
                                 //open inputStream with the file passed
                                 InputStream fileInputStream = new FileInputStream(localFile);
                                 FileOutputStream fop = new FileOutputStream(newFile);
                                 IOUtils.copy(fileInputStream, fop);
                                 fileInputStream.close();
                                 fop.close();
                                 //uploadFileItem.write(newFile);
                             }
                         }

                         if (newFile != null && newFile.exists()) {

                             // Save the uploaded file info to the database.
                             Integer fileId = WorkflowFileHelper.createOrGetWorkflowFile(
                                 workflowItem, userItem, baseDir,
                                     newFile, fileTitle, "0", null, componentId, false);
                             WorkflowFileDao workflowFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                             if (fileId != null) {
                                 dsFileItem = workflowFileDao.get(fileId);

                                 if (dsFileItem != null) {
                                     ComponentFileItem cfItem = compFileDao.findByWorkflowAndFile(workflowItem, dsFileItem);

                                     if (cfItem == null) {
                                         cfItem = new ComponentFileItem(workflowItem,  componentId, null, dsFileItem);
                                         compFileDao.saveOrUpdate(cfItem);
                                     } else {
                                         cfItem = compFileDao.findByWorkflowAndFile(workflowItem, dsFileItem);
                                         Hibernate.initialize(cfItem);
                                         cfItem.setComponentId(componentId);
                                         cfItem.setDataset(null);
                                         compFileDao.saveOrUpdate(cfItem);
                                     }

                                     successFlag = true;
                                 }
                             }
                         }

                     } else {
                         logger.error("saveFileUpload: Creating directory failed " + newDirectory);
                     }
                 } else {
                     logger.error("saveFileUpload: The fileName cannot be null or empty.");
                 }

         }

         if (successFlag) {
             return dsFileItem;
         } else {
             logger.error("saveFileUpload: Could not create workflow file.");
             return null;
         }
     }

     /**
      * Saves an uploaded file to the system.
      * @param workflowId the workflow id
      * @param componentId the component id
      * @param fileTitle the file title (e.g. A, file1, myFile, etc.)
      * @param fileDesc the file description (e.g. Student-step, transaction, etc)
      * @param uploadFileItem the datashop file item
      * @param baseDir the files base directory, e.g. /datashop/files
      * @param datasetItem the dataset item
      * @param userItem the user item (file uploader/owner)
      * @return the FileItem
      * @throws Exception any exception
      */

     /**
      * Saves an uploaded file to the system.
      * @param workflowId the workflow id
      * @param componentId the component id
      * @param fileTitle the file title (e.g. A, file1, myFile, etc.)
      * @param fileDesc the file description (e.g. Student-step, transaction, etc)
      * @param uploadFileItem the datashop file item
      * @param baseDir the files base directory, e.g. /datashop/files
      * @param datasetItem the dataset item
      * @param userItem the user item (file uploader/owner)
      * @return the FileItem
      * @throws Exception any exception
      */
     public WorkflowFileItem saveDataShopFileAsWorkflowFile(WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper,
             Long workflowId, String componentId,
             String fileTitle, String fileDesc, WorkflowFileItem uploadFileItem, String datashopBaseDir,
                 UserItem userItem) {

         Boolean successFlag = false;
         WorkflowFileItem dsFileItem = null;
         if (uploadFileItem != null && componentId != null
                 && componentId.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)) {

             WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
             WorkflowItem workflowItem = workflowDao.get(workflowId);
             DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
             FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
             DatasetItem datasetItem = null;

             String subPath = WorkflowFileUtils.getWorkflowsDir(workflowId) + componentId + "/output/";

             String fullPath = WorkflowFileUtils.sanitizePath(uploadFileItem.getFullPathName(datashopBaseDir))
                  + uploadFileItem.getFileName();
             File testFile = new File(fullPath);

             String datashopPathFormat = uploadFileItem.getFilePath().replaceAll("/$", "");
             List<FileItem> datashopFileItems = fileDao.find(datashopPathFormat, uploadFileItem.getFileName());


             String pattern = datashopBaseDir + "([a-zA-Z0-9_]+)/export/"
                     + "[a-zA-Z]+/ds([0-9]+)(_[a-zA-Z]+)+_([a-zA-Z0-9_]+)_[0-9]+_[0-9]+_[0-9]+_[0-9]+\\.[a-zA-Z]+";

             // Keep track of whether the file is a DataShop file (student-step etc.) or attached to a Dataset
             Boolean isAttachment = false;

             if (datashopFileItems.isEmpty() && testFile != null) {
                 // Workaround because export zips are stored with the filename
                 // in the file_path, and the rest of the files are stored with
                 // only the file_path and no name. Exports are not stored in workflow_file with a dataset id.
                 Pattern patternExport = Pattern.compile(pattern);
                 Matcher m = patternExport.matcher(fullPath);
                 if (m.matches()) {
                     String datasetNameStr = m.group(1);
                     String datasetIdStr = m.group(2);
                     // Since DataShop doesn't link cached exports to the workflow_file table,
                     // ensure the id and name match. This is a critical test to ensure another file
                     // cannot be substituted.
                     if (datasetIdStr != null) {
                         Integer datasetId = Integer.parseInt(datasetIdStr);
                         datasetItem = datasetDao.get(datasetId);
                         String pathTest = FileUtils.cleanForFileSystem(datasetItem.getDatasetName());
                         if (pathTest.equalsIgnoreCase(datasetNameStr)) {
                             testFile = new File(fullPath);
                         }

                     }
                 }
             } else if (!datashopFileItems.isEmpty() && testFile != null) {
                 isAttachment = true;
                 FileItem datashopFileItem = fileDao.get((Integer) datashopFileItems.get(0).getId());
                 Integer datasetId = fileDao.findDatasetId(datashopFileItem);
                 if (datasetId != null) {
                     datasetItem = datasetDao.get(datasetId);
                 }
             }

             if (testFile != null && testFile.isFile() && testFile.canRead()) {

                 String fileFullName = null;
                 if (fullPath.indexOf('/') >= 0) {
                     fileFullName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
                 }
                 File newDirectory = null;

                 // Check to make sure the user has selected a file.
                 if (fileFullName != null && fileFullName.length() > 0) {
                     // Create the directory
                     String wholePath = WorkflowFileUtils.sanitizePath(datashopBaseDir + "/" + subPath);
                     newDirectory = new File(wholePath);
                     if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                         FileUtils.makeWorldReadable(newDirectory);
                     } else {
                         logger.error("saveDataShopFileAsWorkflowFile: Creating directory failed " + newDirectory);
                     }
                 } else {
                     logger.error("saveDataShopFileAsWorkflowFile: The fileName cannot be null or empty.");
                 }
                 String unzipFileName = null;
                 String fileExt = "";
                 File tmpFile = new File(fullPath);
                 Boolean moveFile = false;
                 File inputFile = null;
                 List<String> textExtensions = Arrays.asList(new String[] { ".txt", ".csv", ".tsv", ".log" });

                 if (tmpFile.getName().matches((".*\\.zip")) && !isAttachment) {
                     // Zip file and not a dataset attachment: must be student-step, transaction, etc.
                     try {
                         inputFile = File.createTempFile("wf" + workflowId, ".tmp", newDirectory);
                         moveFile = true;
                         unzipFileName = WorkflowFileUtils.unzipFile(tmpFile, inputFile);

                     } catch (IOException e) {
                         logger.error("Could not unzip file " + tmpFile.getAbsolutePath() + " to "
                                 + inputFile.getAbsolutePath());
                     }

                 } else {
                     inputFile = tmpFile;
                     unzipFileName = tmpFile.getName();
                 }

                 String mimeType = null;

                 try {
                     mimeType = WorkflowFileUtils.getMimeType(inputFile);
                 } catch (IOException e) {
                     logger.error("Tika could not retrieve mime type of file " + inputFile.getAbsolutePath());
                 }
                 // When taken as a zipEntry, some systems can mistakenly
                 // identify the mime type as application/octet-stream.
                 // This fixes that. (UPDATE: TAKING OUT PRIOR TO TESTING.. I THINK IT ISN'T NEEDED)
                 if (tmpFile.getName().matches((".*\\.zip")) && mimeType != null
                         && mimeType.equalsIgnoreCase("application/octet-stream") && fileExt != null
                         && textExtensions.contains(fileExt)) {
                     // mimeType = "text/plain";
                 }

                 if (inputFile != null && newDirectory != null) {
                     // Write the file to the directory

                     Hibernate.initialize(workflowItem);
                     if (workflowItem != null && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                         try {
                             // If we created a temporary text file
                             // while extracting from a zip, then move the file
                             ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();

                             // This method is also used for saving new files from existing workflow_file items.
                             List<ComponentFileItem> existingCfItems = compFileDao.findByFile(uploadFileItem);
                             if (!existingCfItems.isEmpty()) {
                                 ComponentFileItem existingCfItem = existingCfItems.get(0);
                                 if (datasetItem == null && existingCfItem.getDataset() != null) {
                                     datasetItem = datasetDao.get((Integer) existingCfItem.getDataset().getId());
                                 }
                             }

                             workflowFileHelper.updateFileMappings(compFileDao, workflowItem, componentId, false, datashopBaseDir);

                             // Save the file to the file system.
                             String newFileName = unzipFileName;
                             File newFile = null;

                             if (newDirectory.exists() && newDirectory.isDirectory()) {
                                 List<String> newFileList = Arrays.asList(newDirectory.list());
                                 int extensionIndex = unzipFileName.lastIndexOf(".");
                                 String newFileExt = "";
                                 String fileName = "";
                                 if (extensionIndex < 0) {
                                     fileName = unzipFileName;
                                 } else if (extensionIndex == 0) {
                                     newFileExt = unzipFileName.substring(0, unzipFileName.length());
                                 } else {
                                     newFileExt = unzipFileName.substring(extensionIndex);
                                     fileName = unzipFileName.substring(0, extensionIndex);
                                 }
                                 int i = 0;

                                 while (newFileList.contains(newFileName)) {
                                     i++;
                                     newFileName = fileName + "_" + i + newFileExt;
                                 }

                                 newFile = new File(newDirectory.getAbsoluteFile(), newFileName);
                                 if (!newFile.exists()) {
                                     if (moveFile) {
                                         Files.move(inputFile.toPath(), newFile.toPath());
                                     // Otherwise, we will copy the file.
                                     } else {
                                         FileUtils.copyFile(inputFile, newFile);
                                     }
                                 }
                             }

                             Integer fileId = WorkflowFileHelper.createOrGetWorkflowFile(
                                 workflowItem, userItem, datashopBaseDir,
                                         newFile, fileTitle, "0", datasetItem, componentId, false);
                             WorkflowFileDao workflowFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                             if (fileId != null) {
                                 dsFileItem = workflowFileDao.get(fileId);
                                 successFlag = true;
                             }
                         } catch (IOException e) {
                             logger.error("Could not copy file " + inputFile.getAbsolutePath() + " to "
                                     + unzipFileName);
                         }
                     }

                 }
             }
         }
         if (successFlag) {
             return dsFileItem;
         } else {
             return null;
         }
     }

     /**
      * Handle request to use an existing dataset file.
      *
      * @param req {@link HttpServletRequest}
      * @param resp {@link HttpServletResponse}
      * @param userItem user currently logged in
      * @throws IOException an IO exception
      * @throws ServletException an exception creating the servlet
      */
     public HttpServletRequest handleDatasetFile(WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper, HttpServletRequest req, HttpServletResponse resp,
             WorkflowItem workflowItem, WorkflowFileItem existingFileItem,
                 String baseDir, String componentsDir, UserItem userItem) {
         String workflowIdStr = req.getParameter("workflowId");
         // Unique identifier for this workflow, e.g. Component1234
         String componentId = WorkflowIfaceHelper.getComponentId(req);
         // Type of file being uploaded (as defined in the XML).
         String fileIndex = req.getParameter("fileIndex");
         // Label of the file being uploaded
         String fileLabel = req.getParameter("fileLabel");
         Long workflowId = null;
         if (workflowIdStr != null && workflowIdStr.matches("\\d+") && componentId != null) {
             workflowId = Long.parseLong(workflowIdStr);
             logger.debug("Adding file " + existingFileItem.getId() + " to request.");

             WorkflowFileItem newFileItem = saveDataShopFileAsWorkflowFile(workflowFileHelper, workflowHelper,
                     workflowId, componentId,
                 fileLabel, fileIndex, existingFileItem, baseDir, userItem);

             if (newFileItem != null) {
                 workflowHelper.logWorkflowComponentUserAction(userItem, workflowItem, null,
                     componentId, null, null, null,
                     null, newFileItem, null, WorkflowHelper.LOG_SELECT_DATASET_FILE, "original_workflow_file_id : " + existingFileItem.getId());

                 extractImportFileMetadata(workflowHelper, req, workflowItem, newFileItem, baseDir, componentsDir);
             }
         }
         return req;
     }

    JSONObject getMyEditableDatasets(WorkflowContext workflowContext, WorkflowItem workflowItem,
            UserItem loggedInUserItem, String componentName, Integer preferredDatasetId)
            throws JSONException {
        if (workflowItem != null && loggedInUserItem != null && workflowItem.getOwner() != null
                && workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {

            JSONArray projectJsonArray = new JSONArray();
            JSONArray datasetJsonArray = new JSONArray();

            List<ProjectItem> projects = new ArrayList<ProjectItem>();

            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            List<ProjectItem> myProjects = authDao.findMyEditableProjectsForImports((String) loggedInUserItem.getId(), loggedInUserItem.getAdminFlag());

            if (myProjects != null && !myProjects.isEmpty()) {
                projects.addAll(myProjects);
            }

            if (!projects.isEmpty()) {

                List<ProjectItem> tailoredList = new ArrayList<ProjectItem>();
                Integer preferredProjectId = null;
                if (preferredDatasetId != null) {
                DatasetItem dsItem = dsDao.get(preferredDatasetId);
                    if (dsItem != null && dsItem.getProject() != null) {
                        preferredProjectId = (Integer) dsItem.getProject().getId();
                    }
                }

                if (preferredProjectId != null) {


                    for (ProjectItem projectItem : projects) {
                        if (projectItem.getId().equals(preferredProjectId)) {
                            tailoredList.add(projectItem);
                        }
                    }
                    for (ProjectItem projectItem : projects) {
                        if (!projectItem.getId().equals(preferredProjectId)) {
                            tailoredList.add(projectItem);
                        }
                    }
                } else {
                    tailoredList = projects;
                }

                for (ProjectItem projectItem : tailoredList) {
                    Boolean addedProject = false;
                    Hibernate.initialize(projectItem);
                    Boolean isPublic = authDao.isPublic((Integer) projectItem.getId());
                    JSONObject projectObject = new JSONObject();
                    projectObject.put("id", projectItem.getId());
                    projectObject.put("projectName", WorkflowFileUtils.htmlEncode(projectItem.getProjectName()));
                    projectObject.put("dataCollectionType", projectItem.getDataCollectionType());
                    projectObject.put("datasetLastAdded", projectItem.getDatasetLastAdded());
                    projectObject.put("isPublic", isPublic);

                    Boolean isProjectOrDatashopAdminFlag = loggedInUserItem.getAdminFlag()
                            || authDao.isProjectAdmin(loggedInUserItem, projectItem);
                    Collection<DatasetItem> datasetItems = dsDao.findByProject(projectItem, isProjectOrDatashopAdminFlag);

                    if (datasetItems != null) {
                        for (DatasetItem datasetItem : datasetItems) {
                            Hibernate.initialize(datasetItem);
    // mck todo: tooltip with the following dataset info over samples/datasets
                            JSONObject datasetObject = new JSONObject();
                            datasetObject.put("id", datasetItem.getId());
                            datasetObject.put("projectId", projectItem.getId());
                            datasetObject.put("projectName", WorkflowFileUtils.htmlEncode(projectItem.getProjectName()));
                            datasetObject.put("datasetName", WorkflowFileUtils.htmlEncode(datasetItem.getDatasetName()));
                            datasetObject.put("tutor", datasetItem.getTutor());
                            datasetObject.put("startTime", datasetItem.getStartTime());
                            datasetObject.put("endTime", datasetItem.getEndTime());
                            datasetObject.put("description", datasetItem.getDescription());
                            datasetObject.put("hypothesis", datasetItem.getHypothesis());
                            datasetObject.put("notes", datasetItem.getNotes());
                            datasetObject.put("school", datasetItem.getSchool());
                            datasetObject.put("dataLastModified", datasetItem.getDataLastModified());

                            if (!addedProject) {
                                projectJsonArray.put(projectObject);
                                addedProject = true;
                            }

                            datasetJsonArray.put(datasetObject);

                            }
                        }
                    }
                }

                JSONObject jsonInfo = null;
                if (datasetJsonArray.length() > 0) {
                    jsonInfo = AbstractServlet.json("myProjectArray", projectJsonArray,
                        "myDatasetArray", datasetJsonArray);

                }

                return jsonInfo;
        }
        return null;
    }


}
