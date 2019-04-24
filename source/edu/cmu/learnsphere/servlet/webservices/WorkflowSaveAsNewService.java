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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.learnsphere.dto.WorkflowDTO;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.servlet.webservices.WebService;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.util.LogUtils;

import static java.lang.String.format;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * Web service for saving as new for a workflow.
 *
 * @author hui cheng
 * @version $Revision: 15841 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2019-02-01 14:31:08 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowSaveAsNewService extends LearnSphereWebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(GLOBAL, WORKFLOW_ID, DESCRIPTION, NEW_WORKFLOW_NAME);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES =
        map(GLOBAL, TF);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public WorkflowSaveAsNewService(HttpServletRequest req, HttpServletResponse resp,
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
            int workflowId = workflowParam();
            String newWorklfowName = newWorkflowNameParam();
            String description = descriptionParam();
            Boolean global = globalParam();
            Long newWorkflowId = learnSphereHelper().saveWorkflowAsNew(getAuthenticatedUser(), workflowId, baseDir,
                            newWorklfowName, description, global);
            if (newWorkflowId == null)
                    throw new WebServiceException(UNKNOWN_ERR, "Error found in save as new service for workflow: " + workflowId + ". Error: can't create new workflow");
            //send message on success
            WorkflowDTO dto = learnSphereHelper().workflowDTOForId(getAuthenticatedUser(), (int)newWorkflowId.longValue(),
                            true);
            List<WorkflowDTO> dtos = new ArrayList<WorkflowDTO>();
            dtos.add(dto);
            writeDTOXML(dtos, format("Success. New workflow is created. The new workflow ID is " + newWorkflowId + "."));
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
