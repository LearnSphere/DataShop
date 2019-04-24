/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import edu.cmu.learnsphere.dto.WorkflowDTO;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.servlet.webservices.WebService;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceUserLog;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * Web service for modifying a workflow.
 *
 * @author hui cheng
 * @version $Revision: 16023 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-04-16 09:54:20 -0400 (Tue, 16 Apr 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowModifyService extends LearnSphereWebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(WORKFLOW_ID);
    
    /** the temp folder to unzip zip file */
    private static final String UNZIP_DESTINATION_PATH = "/workflows/webservices/";
    
    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public WorkflowModifyService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return the requested datasets as XML.
     * @param wsUserLog web service user log
     */
    public void post(LearnSphereWebServiceUserLog wsUserLog) {
            InputStream fileContent = null;
           try {
                   Map<String, InputStream> attachedFiles = getAttachedFileForPost();
                   Map.Entry<String, InputStream> entry = attachedFiles.entrySet().iterator().next();
                   String fileName = entry.getKey();
                   fileContent = entry.getValue();
                   //get the baseDir for datashop related files
                   String baseDir = (String)getReq().getAttribute("baseDir");
                   int workflowId = workflowParam();
                   String unzipFilesDir = baseDir + this.UNZIP_DESTINATION_PATH + getReq().getSession().getId() + File.separator;
                   (new File(unzipFilesDir)).mkdirs();
                   File tempFile = new File(unzipFilesDir + fileName);
                   FileOutputStream fop = new FileOutputStream(tempFile);
                   IOUtils.copy(fileContent, fop);
                   fop.close();
                   File unzipDir = WorkflowFileUtils.unzipFileToDirectory(tempFile, unzipFilesDir);
                   //delete the original zip file
                   tempFile.delete();
                   //process workflow output xml file, save files in the right folder and save workflow
                   WorkflowItem workflowItem = learnSphereHelper().parseWorkflowDefinitionFile(getAuthenticatedUser(), (long)workflowId, unzipDir, baseDir);
                   //delete <session_id> 
                   FileUtils.deleteDirectory(new File(unzipFilesDir));
                   //send success message
                   //send message on success
                   int i_workfowId = (int)(long)workflowItem.getId();
                   //also need to write to log wsUserLog!!!!
                   WorkflowDTO dto = learnSphereHelper().workflowDTOForId(getAuthenticatedUser(), i_workfowId,
                                   true);
                   List<WorkflowDTO> dtos = new ArrayList<WorkflowDTO>();
                   dtos.add(dto);
                   writeDTOXML(dtos, format("Success. Workflow " + i_workfowId + " was modified successfully."));
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
                } finally {
                      if (fileContent != null) {
                        try {
                                fileContent.close();
                        } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                      }
                }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
