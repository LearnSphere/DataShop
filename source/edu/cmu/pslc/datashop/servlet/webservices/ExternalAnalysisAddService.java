/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.FileUtils;
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
 * Web service for adding a new external analysis.
 *
 * @author Hui Cheng <!-- $KeyWordsOff: $ -->
 */
public class ExternalAnalysisAddService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Max title length. */
    private static final int MAX_TITLE_LEN = 255;
    /** Max description length. */
    private static final int MAX_DESC_LEN = 500;
    /** Max statistical model title length. */
    private static final int MAX_STATMODEL_LEN = 100;
    /** Max post data length. */
    private static final int MAX_POSTDATA_LEN = 100000000;
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(TITLE, DESCRIPTION,
            STATISTICAL_MODEL, KC_MODEL, POST_DATA, DATASET_ID, VERBOSE);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES = map(
            VERBOSE, TF);

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
    public ExternalAnalysisAddService(HttpServletRequest req,
            HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * validate parameter title
     *
     * @return validated parameter title
     * @throws WebServiceException
     *             when title is empty or size is bigger than 255
     */
    private String validateTitle() throws WebServiceException {
        String title = stringParam(TITLE);
        if (title == null || title.equals("")) {
            throw paramValueMissingException(TITLE);
        } else if (title.length() > MAX_TITLE_LEN) {
            throw paramValueTooLongException(TITLE, "255");
        } else {
            return title;
        }
    }

    /**
     * validate parameter description
     *
     * @return validated description
     * @throws WebServiceException
     *             when description is too long
     */
    private String validateDescription() throws WebServiceException {
        String description = stringParam(DESCRIPTION);
        if (description != null && description.length() > MAX_DESC_LEN) {
            throw paramValueTooLongException(DESCRIPTION, "500");
        } else {
            return description;
        }
    }

    /**
     * validate parameter statisticalModel
     *
     * @return validated statisticalModel
     * @throws WebServiceException
     *             when statisticalModel is too long
     */
    private String validateStatisticalModel() throws WebServiceException {
        String statisticalModel = stringParam(STATISTICAL_MODEL);
        if (statisticalModel != null && statisticalModel.length() > MAX_STATMODEL_LEN) {
            throw paramValueTooLongException(STATISTICAL_MODEL, "100");
        } else {
            return statisticalModel;
        }
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
            validateParameters(wsUserLog, VALID_GET_PARAMS, VALID_GET_PARAM_VALUES);
            // get parameters, and dataset item
            String title = validateTitle();
            UserItem user = getAuthenticatedUser();
            // If dataset isn't public or user doesn't have at least VIEW access,
            // this operation will fail with INACCESSIBLE_DATASET_ERR.
            DatasetItem dataset = helper().findDataset(user, datasetParam(),
                                                       AccessParam.VIEWABLE);

            // Now determine if user has permission to add an External Analysis.
            String authLevel = helper().getAuth(user, dataset.getProject());
            if (!authLevel.equals("edit") && !authLevel.equals("admin") && !user.getAdminFlag()) {
                throw new WebServiceException(INACCESSIBLE_EXTERNAL_ANALYSIS_ERR,
                                              "Insufficient privileges to add "
                                              + "an external analysis.");
            }

            SkillModelItem kcModel = null;
            if (stringParam(KC_MODEL) != null) {
                kcModel = helper().kcModelForId(user, datasetParam(), intParam(KC_MODEL));
            }
            String description = validateDescription();
            String statisticalModel = validateStatisticalModel();
            String postData = validatePostData();
            // set all related objects
            FileItem dsFileItem = new FileItem();
            dsFileItem.setTitle(title);
            String subPath = FileUtils.cleanForFileSystem(dataset.getDatasetName());
            dsFileItem.setFilePath(subPath);
            Date now = new Date();
            dsFileItem.setAddedTime(now);
            if (description != null) {
                dsFileItem.setDescription(description);
            }
            // what is the content type?
            dsFileItem.setFileType("text/plain");
            dsFileItem.setOwner(user);

            ExternalAnalysisItem eaItem = new ExternalAnalysisItem();
            if (kcModel != null) {
                eaItem.setSkillModelId(new Long(kcModel.getId().toString())
                        .longValue());
                eaItem.setSkillModelName(kcModel.getSkillModelName());
            }
            if (statisticalModel != null) {
                eaItem.setStatisticalModel(statisticalModel);
            }
            eaItem.setOwner(user);
            eaItem.setFile(dsFileItem);
            helper().addExternalAnalysis(dataset, dsFileItem, eaItem, postData,
                    now);
            writeSuccessWithCustomField("", map("analysis_id", eaItem.getId()));
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
