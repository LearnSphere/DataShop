/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import java.util.Map;

/**
 * A human readable web services error message to be sent to the client.
 *
 * @author Jim Rankin
 * @version $Revision: 15895 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2019-03-12 17:50:09 -0400 (Tue, 12 Mar 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WebServiceException extends Exception {
    /** Invalid dataset. */
    public static final int INVALID_DATASET_ERR = -1;
    /** Inaccessible dataset. */
    public static final int INACCESSIBLE_DATASET_ERR = -2;
    /** Invalid sample. */
    public static final int INVALID_SAMPLE_ERR = -3;
    /** Inaccessible sample. */
    public static final int INACCESSIBLE_SAMPLE_ERR = -4;
    /** Invalid parameter. */
    public static final int INVALID_PARAM_ERR = -5;
    /** Invalid parameter value. */
    public static final int INVALID_PARAM_VAL_ERR = -6;
    /** Dataset has not been released so it is unavailable for exports. */
    public static final int UNRELEASED_DATASET_ERR = -7;
    /** Required field missing. */
    public static final int REQUIRED_FIELD_MISSING_ERR = -8;
    /** Invalid external analysis id. */
    public static final int INVALID_EXTERNAL_ANALYSIS_ERR = -9;
    /** Invalid custom field for dataset. */
    public static final int INVALID_CUSTOM_FIELD_FOR_DATASET_ERR = -9;
    /** Invalid post request body: too large or empty. */
    public static final int INVALID_POST_REQUEST_BODY_ERR = -10;
    /** Invalid custom field. */
    public static final int INVALID_CUSTOM_FIELD_ERR = -11;
    /** Inaccessible external analysis. */
    public static final int INACCESSIBLE_EXTERNAL_ANALYSIS_ERR = -12;
    /** Inaccessible custom field. */
    public static final int INACCESSIBLE_CUSTOM_FIELD_ERR = -12;
    /** CustomField by the same name already exists for this dataset. */
    public static final int CONFLICT_ERR = -13;
    /** Invalid parameter value. */
    public static final int NO_CACHED_FILE_ERR = -14;
    /** Invalid parameter length. */
    public static final int PARAM_LENGTH_ERR = -15;
    /** Invalid value for element. */
    public static final int INVALID_VALUE_FOR_ELEMENT_ERR = -16;
    /** Invalid XML. */
    public static final int INVALID_XML_ERR = -17;
    /** Invalid skill model name. */
    public static final int INVALID_SKILL_MODEL = -18;
    /** Invalid KCM input format. */
    public static final int INVALID_KCM_INPUT_FORMAT = -19;
    /** Invalid step id. */
    public static final int INVALID_STEP_ID = -20;
    /** KCM input service is already running for the same dataset. */
    public static final int KCM_INPUT_SERVICE_BUSY = -22;
    /** Dataset name is already in use. */
    public static final int DATASET_NAME_CONFLICT_ERR = -23;
    /** Unauthorized user account. */
    public static final int UNAUTHORIZED_USER_ERR = -24;
    /** Remote DataShop requests are not meant to be handled by this server. */
    public static final int REMOTE_REQUESTS_NOT_ALLOWED = -25;
    /** Remote instance already exists. */
    public static final int INSTANCE_NAME_CONFLICT_ERR = -26;
    /** Remote instance id not valid. */
    public static final int INVALID_INSTANCE_ID_ERR = -27;
    /** Invalid Metrics Report data. */
    public static final int INVALID_METRICS_REPORT_DATA = -28;
    /** Invalid skill name. */
    public static final int INVALID_SKILL = -29;
    /** Invalid user id. */
    public static final int INVALID_USER_ID_ERR = -30;
    /** Invalid project id. */
    public static final int INVALID_PROJECT_NAME_ERR = -31;
    /** Invalid authorization request. */
    public static final int INVALID_AUTHORIZATION_REQUEST_ERR = -32;
    /** Inaccessible workflow. */
    public static final int INACCESSIBLE_WORKFLOW_ERR = -33;
    /** Invalid workflow. */
    public static final int INVALID_WORKFLOW_ERR = -34;
    /** workflow initialization. */
    public static final int WORKFLOW_INITIALIZATION_ERR = -35;
    /** workflow initialization. */
    public static final int WORKFLOW_ALREADY_RUNNING_ERR = -36;
    /** workflow xml definition error. */
    public static final int WORKFLOW_DEFINITION_ERR = -37;
    /** workflow xml definition error. */
    public static final int WORKFLOW_UPLOAD_FILE_OVERSIZE = -38;
    /** workflow internal file error. */
    public static final int WORKFLOW_INTERNAL_FILE_ERR = -39;
    /** No matching URL found. */
    public static final int NO_MATCHING_URL_ERR = -99;
    /** Unknown error. */
    public static final int UNKNOWN_ERR = -100;

    /** Look up HTTP response code corresponding to error code. */
    private static final Map<Integer, Integer> ERROR_TO_RESPONSE_CODE =
        map(INVALID_DATASET_ERR, SC_NOT_FOUND, INACCESSIBLE_DATASET_ERR, SC_UNAUTHORIZED,
            UNRELEASED_DATASET_ERR, SC_UNAUTHORIZED,
            INVALID_SAMPLE_ERR, SC_NOT_FOUND, INACCESSIBLE_SAMPLE_ERR, SC_UNAUTHORIZED,
            INVALID_EXTERNAL_ANALYSIS_ERR, SC_NOT_FOUND,
            INVALID_POST_REQUEST_BODY_ERR, SC_BAD_REQUEST,
            INACCESSIBLE_EXTERNAL_ANALYSIS_ERR, SC_UNAUTHORIZED,
            REQUIRED_FIELD_MISSING_ERR, SC_BAD_REQUEST,
            INVALID_PARAM_ERR, SC_BAD_REQUEST, INVALID_PARAM_VAL_ERR, SC_BAD_REQUEST,
            NO_CACHED_FILE_ERR, SC_NOT_FOUND, UNKNOWN_ERR, SC_INTERNAL_SERVER_ERROR,
            PARAM_LENGTH_ERR, SC_BAD_REQUEST, INVALID_CUSTOM_FIELD_ERR, SC_NOT_FOUND,
            INVALID_CUSTOM_FIELD_FOR_DATASET_ERR, SC_NOT_FOUND,
            CONFLICT_ERR, SC_CONFLICT,
            INVALID_VALUE_FOR_ELEMENT_ERR, SC_BAD_REQUEST, INVALID_XML_ERR, SC_BAD_REQUEST,
            INACCESSIBLE_CUSTOM_FIELD_ERR, SC_UNAUTHORIZED, INVALID_SKILL_MODEL, SC_NOT_FOUND,
            INVALID_KCM_INPUT_FORMAT, SC_BAD_REQUEST, INVALID_STEP_ID, SC_BAD_REQUEST,
            KCM_INPUT_SERVICE_BUSY, SC_SERVICE_UNAVAILABLE,
            DATASET_NAME_CONFLICT_ERR, SC_CONFLICT, UNAUTHORIZED_USER_ERR, SC_UNAUTHORIZED,
            REMOTE_REQUESTS_NOT_ALLOWED, SC_FORBIDDEN,
            INSTANCE_NAME_CONFLICT_ERR, SC_CONFLICT, INVALID_INSTANCE_ID_ERR, SC_NOT_FOUND,
            INVALID_METRICS_REPORT_DATA, SC_BAD_REQUEST,
            INVALID_SKILL, SC_NOT_FOUND, INVALID_USER_ID_ERR, SC_NOT_FOUND,
            INACCESSIBLE_WORKFLOW_ERR, SC_UNAUTHORIZED,
            INVALID_WORKFLOW_ERR, SC_NOT_FOUND,
            WORKFLOW_INITIALIZATION_ERR, SC_INTERNAL_SERVER_ERROR,
            WORKFLOW_ALREADY_RUNNING_ERR, SC_INTERNAL_SERVER_ERROR,
            INVALID_PROJECT_NAME_ERR, SC_NOT_FOUND,
            WORKFLOW_DEFINITION_ERR, SC_BAD_REQUEST,
            WORKFLOW_UPLOAD_FILE_OVERSIZE, SC_BAD_REQUEST,
            WORKFLOW_INTERNAL_FILE_ERR, SC_INTERNAL_SERVER_ERROR,
            INVALID_AUTHORIZATION_REQUEST_ERR, SC_BAD_REQUEST);

    /** The error code from the web services API documentation. */
    private int errorCode;
    /** a description of the problem. */
    private String errorMessage;

    /**
     * Exception indicating an invalid value for the parameter.
     * @param param the parameter
     * @param value the value
     * @return exception indicating an invalid value for the parameter
     */
    public static WebServiceException invalidParamValueException(String param, Object value) {
        return new WebServiceException(INVALID_PARAM_VAL_ERR, "Invalid value for parameter "
                + param + ": " + value + ".");
    }

    /**
     * Exception indicating missing value for a required parameter.
     * @param param the parameter
     * @return exception indicating missing value for a required parameter
     */
    public static WebServiceException paramValueMissingException(String param) {
        return new WebServiceException(REQUIRED_FIELD_MISSING_ERR,
                "Required field(s) missing: " + param + ".");
    }

    /**
     * Exception indicating a parameter value is too long.
     * @param param the parameter
     * @param sizeLimit the size limit in characters
     * @return exception indicating a parameter value is too long
     */
    public static WebServiceException paramValueTooLongException(String param, String sizeLimit) {
        return new WebServiceException(PARAM_LENGTH_ERR, "Parameter " + param
                        + " must be no more than " + sizeLimit + " characters.");
    }

    /**
     * Exception indicating a parameter value has a conflict.
     * @param errorMsg error message
     * @return exception indicating a parameter value has conflict
     */
    public static WebServiceException paramValueConflictException(String errorMsg) {
        return new WebServiceException(CONFLICT_ERR, errorMsg);
    }

    /**
     * Exception indicating post data is invalid.
     * @param errorMsg error message indicating problem with post data
     * @return exception indicating post data is invalid.
     */
    public static WebServiceException invalidDataException(String errorMsg) {
        StringBuffer sb = new StringBuffer("Invalid data");
        if ((errorMsg != null) && (errorMsg.trim().length() > 0)) {
            sb.append(": ").append(errorMsg);
        } else {
            sb.append(".");
        }
        return new WebServiceException(INVALID_POST_REQUEST_BODY_ERR, sb.toString());
    }

    /**
     * Exception indicating unknown error.
     * @return exception indicating unknown error.
     */
    public static WebServiceException unknownErrorException() {
        return new WebServiceException(UNKNOWN_ERR, "Unknown error.");
    }

    /**
     * Create a new WebServiceException.
     * @param errorCode an HTTP error code
     * @param errorMessage a description of the problem.
     */
    public WebServiceException(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     *  The error code from the web services API documentation.
     *  @return the error code from the web services API documentation
     */
    public int getErrorCode() { return errorCode; }

    /**
     * An HTTP response code.
     * @return an HTTP response code
     */
    public int getResponseCode() {
        Integer responseCode = ERROR_TO_RESPONSE_CODE.get(errorCode);
        if (responseCode == null) {
            throw new IllegalStateException("No response code for " + errorCode);
        }
        return responseCode;
    }

    /** A description of the problem. @return a description of the problem */
    public String getErrorMessage() { return errorMessage; }
}
