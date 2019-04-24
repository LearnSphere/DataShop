/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;


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
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * Web service for fetching a multiple workflows.
 *
 * @author hui cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowsService extends LearnSphereWebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(MINE, GLOBAL, DATA_ACCESS, VERBOSE);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES =
        map(MINE, TF, GLOBAL, TF, DATA_ACCESS, DATA_ACCESS_PARAMS, VERBOSE, TF);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public WorkflowsService(HttpServletRequest req, HttpServletResponse resp,
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
            List<WorkflowDTO> dtos = learnSphereHelper().workflowDTOsForUser(getAuthenticatedUser(), lsMineParam(), globalParam(), dataAccessParam(),
                    verboseParam());
            writeDTOXML(dtos, format("Success. %d workflows found.", dtos.size()));
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
