/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.LogUtils;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidDataException;
import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;
import static
    edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueTooLongException;
import static
edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidParamValueException;
import static edu.cmu.pslc.datashop.
    servlet.webservices.WebServiceException.INACCESSIBLE_CUSTOM_FIELD_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

/**
 * Web service for adding a new external analysis.
 *
 * @author Hui Cheng <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldAddService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(DATASET_ID, POST_DATA);

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
    public CustomFieldAddService(HttpServletRequest req,
            HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * validate level
     *
     * @param cfItem CustomFieldItem to be validated
     * @throws WebServiceException
     *             when level is empty or is not allowed level
     */
    private void validateLevel(CustomFieldItem cfItem) throws WebServiceException {
        String level = cfItem.getLevel();
        if (level == null || level.equals("")) {
            throw paramValueMissingException(LEVEL);
        } else if (!CustomFieldItem.isValidCustomFieldLevel(level)) {
            throw invalidParamValueException(LEVEL, level);
        }
    }

    /**
     * validate name
     *
     * @param cfItem CustomFieldItem to be validated
     * @throws WebServiceException
     *             when name is empty, too long or already exists in this dataset
     */
    private void validateName(CustomFieldItem cfItem)
    throws WebServiceException {
        String name = cfItem.getCustomFieldName();
        if (name == null || name.equals("")) {
            throw paramValueMissingException(NAME);
        } else if (cfItem.getCustomFieldName().length() > CF_MAX_NAME_LEN) {
            throw paramValueTooLongException(NAME, "" + CF_MAX_NAME_LEN);
        }
    }

    /**
     * validate description
     *
     * @param cfItem CustomFieldItem to be validated
     * @throws WebServiceException
     *             when description size is bigger than 255
     */
    private void validateDescription(CustomFieldItem cfItem) throws WebServiceException {
        if (cfItem.getDescription() != null
                        && cfItem.getDescription().length() > CF_MAX_DESCRIPTION_LEN) {
            throw paramValueTooLongException(DESCRIPTION, "" + CF_MAX_DESCRIPTION_LEN);
        }
    }

    /**
     * Validate custom field. Id should be null for new custom field
     *
     * @param cfItem CustomFieldItem to be validated.
     * @throws WebServiceException when custom field id is non-null
     */
    private void validateId(CustomFieldItem cfItem) throws WebServiceException {
        if (cfItem.getId() != null) {
            throw invalidDataException("Unexpected CustomField id [" + cfItem.getId() + "].");
        }
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
            throw invalidDataException("post data is empty or null.");
        }
    }

    /**
     * Add an custom field.
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

                // Now determine if user has permission to add a Custom Field.
                String authLevel = helper().getAuth(user, dataset.getProject());
                if (!authLevel.equals("edit") && !authLevel.equals("admin")) {
                    throw new WebServiceException(INACCESSIBLE_CUSTOM_FIELD_ERR,
                                                  "Insufficient privileges to add a custom field.");
                }
            }

            CustomFieldItem cfItem = helper().getCustomFieldItemFromXML(stringParam(POST_DATA));
            validateName(cfItem);
            validateLevel(cfItem);
            validateDescription(cfItem);
            validateId(cfItem);

            cfItem.setOwner(getAuthenticatedUser());
            cfItem.setDateCreated(new Date());
            helper().addOrModifyCustomField(datasetParam(), getAuthenticatedUser(), cfItem);
            writeSuccessWithCustomField("", map("custom_field_id", cfItem.getId()));

            UserLogger.logCfAdd(wsUserLog.getDataset(), wsUserLog.getUser(),
                    (Long)cfItem.getId(), cfItem.getCustomFieldName(), true);
        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode()
                    + ": '" + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong processing web service request.", e);
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
