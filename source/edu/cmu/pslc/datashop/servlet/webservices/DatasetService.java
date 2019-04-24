/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * Web service for fetching a single dataset.
 *
 * @author Jim Rankin
 * @version $Revision: 9733 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-08-02 13:36:54 -0400 (Fri, 02 Aug 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());


    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(ACCESS, VERBOSE, DATASET_ID);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES =
        map(ACCESS, ACCESS_PARAMS, VERBOSE, TF);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public DatasetService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return the requested dataset as XML.
     * @param wsUserLog web service user lgo
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS, VALID_GET_PARAM_VALUES);

            DatasetDTO dto = helper().datasetDTOForId(getAuthenticatedUser(), datasetParam(),
                    verboseParam(), accessParam());
            writeDTOXML(dto);
        } catch (WebServiceException wse) {
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something unexpected went wrong with the web service request.", e);
            writeInternalError();
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
