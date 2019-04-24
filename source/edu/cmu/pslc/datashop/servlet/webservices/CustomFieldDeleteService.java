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

import edu.cmu.pslc.datashop.util.LogUtils;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

/**
 * Web service for adding a new external analysis.
 *
 * @author Hui Cheng <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldDeleteService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(DATASET_ID, CUSTOM_FIELD_ID);

    /**
     * Constructor.
     *
     * @param req
     *            the web service request
     * @param resp
     *            the web service response
     * @param params
     *            all parameters for this request, including path parameters
     */
    public CustomFieldDeleteService(HttpServletRequest req,
            HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Delete custom field specified.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS);
            int datasetId = datasetParam();
            long customFieldId = customFieldParam();
            logDebug("datasetId: ", datasetId, ", customFieldId: ", customFieldId);
            int txnNum = helper().customFieldDeleteForId(wsUserLog,
                    getAuthenticatedUser(), datasetId, customFieldId);
            writeSuccess("Custom field " + customFieldId
                    + " is successfully removed from " + txnNum + " transactions.");
            // UserLogger call is done in the helper call above.
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
