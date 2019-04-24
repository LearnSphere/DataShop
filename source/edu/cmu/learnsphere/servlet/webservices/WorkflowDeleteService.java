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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.learnsphere.dto.WorkflowDTO;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.servlet.webservices.WebService;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;

import static java.lang.String.format;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * Web service for deleteing a workflow.
 *
 * @author hui cheng
 * @version $Revision: 15842 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2019-02-01 14:31:24 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowDeleteService extends LearnSphereWebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(WORKFLOW_ID);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public WorkflowDeleteService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return the requested datasets as XML.
     * @param wsUserLog web service user log
     */
    public void get(LearnSphereWebServiceUserLog wsUserLog) {
        try {
                String baseDir = (String)getReq().getAttribute("baseDir");
                int workflowId = workflowParam();
                learnSphereHelper().deleteAWorkflow(getAuthenticatedUser(), workflowId, baseDir);
                //send message on success
                writeSuccess("Workflow " + workflowId + " is successfully deleted.");        
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
