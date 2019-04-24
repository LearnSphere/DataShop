/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.util.LogUtils;

import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;
import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueTooLongException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidDataException;
import static edu.cmu.pslc.datashop.
    servlet.webservices.WebServiceException.INACCESSIBLE_EXTERNAL_ANALYSIS_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

/**
 * Web service for adding a KCM to a dataset.
 *
 * @author Hui Cheng <!-- $KeyWordsOff: $ -->
 */
public class ImportKCMService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Max post data length. */
    private static final int MAX_POSTDATA_LEN = 100000000;
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(POST_DATA, DATASET_ID);

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
    public ImportKCMService(HttpServletRequest req,
            HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * validate parameter postData
     *
     * @return validated postData
     * @throws WebServiceException
     *             when postData is null or too large
     */
    private String validatePostData() throws WebServiceException {
        String postData = stringParam(POST_DATA);
        try {
            if (postData == null || postData.equals("")) {
                throw invalidDataException("post data is empty or null.");
            } else if (postData.getBytes("UTF-8").length > MAX_POSTDATA_LEN) {
                throw invalidDataException("post data exceeds "
                                           + MAX_POSTDATA_LEN + " byte limit.");
            } else {
                return postData;
            }
        } catch (UnsupportedEncodingException ue) {
            throw new WebServiceException(
                    WebServiceException.INVALID_PARAM_VAL_ERR,
                    "UnsupportedEncodingException error for post data. Error: "
                            + ue.getMessage());
        }
    }

    /**
     * Add an external analysis.
     * @param wsUserLog web service user log
     */
    public void post(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS);
            String postData = validatePostData();
            JSONObject returned = helper().verifyAndImportKCMData(datasetParam(), getAuthenticatedUser(), postData);
            if (returned == null) {
                    WebServiceException.unknownErrorException();
            }
            JSONArray modelsArray = returned.getJSONArray("models");
            String modelNames = "";
            for (int i = 0, n = modelsArray.length(); i < n; i++) {
                JSONObject modelJSON = modelsArray.getJSONObject(i);
                if (!modelJSON.getString("action").equals("Skip")) {
                    String modelName = modelJSON.getString("name");
                    modelNames += modelName + ", ";
                }
            }
            if (modelNames.lastIndexOf(", ") == modelNames.length() - 2) {
                modelNames = modelNames.substring(0, modelNames.length() - 2);
            }
            writeSuccess("KCM(s): " + modelNames + " saved successfully."
                         + " Model values are now being computed for the new KCM(s).");
        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode()
                    + ": '" + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error(
                    "Something unexpected went wrong processing web service request.",
                    e);
            writeInternalError();
        }
    }

    /**
     * Only log if debugging is enabled.
     *
     * @param args
     *            concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
