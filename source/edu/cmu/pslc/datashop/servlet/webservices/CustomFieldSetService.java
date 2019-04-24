/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.LogUtils;

import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidDataException;
import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.unknownErrorException;
import static edu.cmu.pslc.datashop.servlet.
    webservices.WebServiceException.INACCESSIBLE_CUSTOM_FIELD_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

/**
 * Web service for adding a new external analysis.
 *
 * @author Hui Cheng <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldSetService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(DATASET_ID, CUSTOM_FIELD_ID, POST_DATA);
    /** The first header of set post data*/
    private static final String TRANSACTION_ID = "Transaction Id";
    /** The second header of set post data*/
    private static final String VALUE = "value";
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
    public CustomFieldSetService(HttpServletRequest req,
            HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * validate parameter postData
     *
     * @throws WebServiceException
     *             when postData is null
     */
    private void validatePostData() throws WebServiceException {
        String postData = stringParam(POST_DATA);
        if (postData == null || postData.equals("")) {
            logger.info("Invalid post data");
            throw invalidDataException("post data is empty or null.");
        }
    }

    /**
     * Parse postData into list of CustomFieldNameValueItem
     *
     * @return list of CustomFieldNameValueItem
     * @throws WebServiceException when postData is null
     */
    private List<CustomFieldNameValueItem> parsePostData() throws WebServiceException {
        HashMap<String, CustomFieldNameValueItem> map =
            new HashMap<String, CustomFieldNameValueItem>();
        BufferedReader br = new BufferedReader(
                new StringReader(stringParam(POST_DATA)));
        String strLine;
        int cnt = 0;
        //Read post data Line By Line
        try {
            while ((strLine = br.readLine()) != null) {
                if (strLine.lastIndexOf("\r") >= 0) {
                    strLine = strLine.substring(0, strLine.length() - 1);
                }
                String[] cols = strLine.split("\t");
                //check headers and number of columns
                if (cols.length != 2) {
                    String errorStr = "Wrong number of columns in data; "
                        + "expected 2 but there were " + cols.length + ".";
                    logger.info(errorStr);
                    throw invalidDataException(errorStr);
                }
                if (cnt == 0 && (!cols[0].equals(TRANSACTION_ID) || !cols[1].equals(VALUE))) {
                    String errorStr = "Column headers [" + cols[0]
                        + ", " + cols[1] + "] are not valid.";
                    logger.info(errorStr);
                    throw invalidDataException(errorStr);
                }
                if (cnt > 0) {
                    // First one in wins; no duplicates.
                    if (!map.containsKey(cols[0])) {
                        map.put(cols[0], new CustomFieldNameValueItem(cols[0], cols[1]));
                    }
                }
                cnt++;
            }
        } catch (IOException ex) {
            throw unknownErrorException();
        }
        return new ArrayList(map.values());
    }

    /**
     * Set custom field values.
     * @param wsUserLog web service user log
     */
    public void post(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS);
            validatePostData();

            UserItem user = getAuthenticatedUser();
            if (!user.getAdminFlag()) {
                // If dataset isn't public or user doesn't have at least VIEW access,
                // this operation will fail with INACCESSIBLE_DATASET_ERR.
                DatasetItem dataset = helper().findDataset(user, datasetParam(),
                                                           AccessParam.VIEWABLE);

                // Now determine if user has permission to set a Custom Field.
                String authLevel = helper().getAuth(user, dataset.getProject());
                if ((authLevel != null) &&
                    !(authLevel.equals("edit") || authLevel.equals("admin"))) {
                    throw new WebServiceException(INACCESSIBLE_CUSTOM_FIELD_ERR,
                                                  "Insufficient privileges to set custom field "
                                                  + customFieldParam() + ".");
                }
            }

            List<CustomFieldNameValueItem> cfNameValues = parsePostData();

            CustomFieldItem cfItem =
                helper().setCustomFieldValues(user, datasetParam(),
                                              customFieldParam(), cfNameValues);
            writeSuccess("Annotated " + cfNameValues.size() + " transactions.");

            // Make note of update to CF
            cfItem.setUpdatedBy(user);
            cfItem.setLastUpdated(new Date());
            helper().addOrModifyCustomField(datasetParam(), user, cfItem);

            UserLogger.logCfSet(wsUserLog.getDataset(), wsUserLog.getUser(),
                    (Long)cfItem.getId(), cfItem.getCustomFieldName(), true);
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
