/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.io.Files;

import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileUtils;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;

import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INACCESSIBLE_WORKFLOW_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * Web service for fetching files for a workflow.
 *
 * @author hui cheng
 * @version $Revision: 15837 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowFilesService extends LearnSphereWebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(FILE_TYPE, WORKFLOW_ID);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES =
        map(FILE_TYPE, FILE_TYPE_PARAMS);
    private static final int ZIP_BUFFER_SIZE = 2048;

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public WorkflowFilesService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return the requested datasets as XML.
     * @param wsUserLog web service user log
     */
    public void get(LearnSphereWebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS, VALID_GET_PARAM_VALUES);
            String baseDir = (String)getReq().getAttribute("baseDir");
            Hashtable<String, List<WorkflowFileItem>> workflowFileItems = learnSphereHelper().workflowFilesForId(getAuthenticatedUser(), workflowParam(),
                    fileTypeParam(), baseDir);
            if (workflowFileItems == null) {
                    this.writeString("No files found");
            }
            List<File> allFiles = new ArrayList<File>();
            String zipTempLoc = null;
            List<File> filesToDelete = new ArrayList<File>();

            for (Map.Entry<String, List<WorkflowFileItem>> entry : workflowFileItems.entrySet()) {
                    String componentId = entry.getKey();
                    List<WorkflowFileItem> componentFileItems = entry.getValue();
                    for (WorkflowFileItem workflowFileItem: componentFileItems) {
                            if (zipTempLoc == null)
                                    zipTempLoc = WorkflowFileUtils.sanitizePath(baseDir + File.separator + workflowFileItem.getFilePath());

                            String currFilePath = WorkflowFileUtils.sanitizePath(baseDir + File.separator + workflowFileItem.getFilePath() + File.separator + workflowFileItem.getFileName());
                            String newFilePath = WorkflowFileUtils.sanitizePath(baseDir + File.separator + workflowFileItem.getFilePath() + File.separator + componentId + "_" + workflowFileItem.getFileName());
                            File currFile = new File(currFilePath);
                            File newFile = new File(newFilePath);
                            if (currFile.exists()) {
                                    Files.copy(currFile, newFile);
                                    allFiles.add(newFile);
                                    filesToDelete.add(newFile);
                            }
                    }

            }
            //write zip to response
            getResp().setContentType("application/zip");
            getResp().setHeader("Content-Disposition", "filename=result.zip");
            BufferedInputStream origin = null;
            ZipOutputStream out = null;
            out = new ZipOutputStream(getResp().getOutputStream());
            // Add all input files to the export zip
            for (File inputFile : allFiles) {
                    if (inputFile != null) {
                        // out.setMethod(ZipOutputStream.DEFLATED);
                        byte data[] = new byte[ZIP_BUFFER_SIZE];

                        FileInputStream fi = new FileInputStream(inputFile);
                        origin = new BufferedInputStream(fi, ZIP_BUFFER_SIZE);
                        ZipEntry entry = new ZipEntry(inputFile.getName());
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, ZIP_BUFFER_SIZE)) != -1) {
                            out.write(data, 0, count);
                        }
                        origin.close();
                    }
            }
            out.close();

                //delete temp files
                for (File fileToDelete : filesToDelete) {
                        if (fileToDelete.exists())
                                fileToDelete.delete();
                }
                //writeSuccess("Total of " + allFiles.size() + " files are successfully zipped and transferred.");
        } catch (WebServiceException wse) {
                //update wsUserLog if error
                String newInfo = wsUserLog.getUserLogItem().getInfo() + "Exception: " + wse + " :: ";
                wsUserLog.setInfo(newInfo);
                writeError(wse);
        } catch (Exception e) {
                //update wsUserLog if error
                String newInfo = wsUserLog.getUserLogItem().getInfo() + "Exception: " + e + " :: ";
                wsUserLog.setInfo(newInfo);
                logger.error("Something unexpected went wrong with the web service request.", e);
                writeInternalError();
        }

    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
