/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.CustomFieldDTO;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Web service for fetching meta data for a single custom fields.
 *
 * @author Hui Cheng
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS =
        set(MINE, DATASET_ID, CUSTOM_FIELD_ID);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES =
        map(MINE, TF);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public CustomFieldService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return the meta data for a custom field for the specified dataset as XML.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS, VALID_GET_PARAM_VALUES);
            logDebug("datasetId: ", datasetParam(),
                    ", customFieldId: ", customFieldParam());
            List<CustomFieldDTO> dtos = new ArrayList<CustomFieldDTO>();
            CustomFieldDTO dto =
                    helper().customFieldDTOForId(getAuthenticatedUser(),
                            datasetParam(), customFieldParam(), mineParam());
            dtos.add(dto);
            writeDTOXML(dtos, "Success.");
        } catch (WebServiceException wse) {
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong with the XML message.", e);
            writeInternalError();
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
