/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.LogUtils;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static java.util.Arrays.asList;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServicesServlet.writeErrorResponse;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServicesServlet.writeSuccessResponse;
import static edu.cmu.pslc.datashop.servlet.webservices
        .WebServicesServlet.writeSuccessResponseWithCustomField;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_POST_REQUEST_BODY_ERR;
import static edu.cmu.pslc.datashop.servlet.
              webservices.WebServiceException.INVALID_PARAM_ERR;
import static edu.cmu.pslc.datashop.servlet.
              webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.servlet.
              webservices.WebServiceException.invalidParamValueException;
import static java.util.Collections.emptyList;
import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

/**
 * Represents a Datashop web service.
 *
 * @author Jim Rankin
 * @version $Revision: 15905 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-03-14 11:12:09 -0400 (Thu, 14 Mar 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WebService {
    /** sample id param */
    protected static final String SAMPLE_ID = "sampleId";
    /** external analysis id param */
    protected static final String EXTERNAL_ANALYSIS_ID = "externalAnalysisId";
    /** custom field id param */
    protected static final String CUSTOM_FIELD_ID = "customFieldId";
    /** dataset id param */
    protected static final String DATASET_ID = "datasetId";
    /** dataset name param */
    protected static final String DATASET_NAME = "name";
    /** instance id param */
    protected static final String INSTANCE_ID = "instanceId";
    /** param false value */
    protected static final String FALSE = "false";
    /** param true value */
    protected static final String TRUE = "true";
    /** verbose param */
    protected static final String VERBOSE = "verbose";
    /** access param */
    protected static final String ACCESS = "access";
    /** access param value viewable */
    protected static final String VIEWABLE = "viewable";
    /** access param value editable */
    protected static final String EDITABLE = "editable";
    /** access param value all */
    protected static final String ALL = "all";
    /** access param value all */
    protected static final String NONE = "none";
    /** param mine value*/
    protected static final String MINE = "mine";
    /** param values true and false */
    protected static final Set<String> TF = set("true", "false");
    /** valid param values for the access parameter */
    protected static final Set<String> ACCESS_PARAMS = set(ALL, VIEWABLE, EDITABLE);
    /** access param value all */
    protected static final Set<String> CFS_PARAMS = set(ALL, NONE);
    /** separator character for transaction export columns */
    protected static final String SEP = "\t";
    /** the offset parameter */
    protected static final String OFFSET = "offset";
    /** the limit parameter */
    protected static final String LIMIT = "limit";
    /** default value for the offset parameter */
    static final int DEFAULT_OFFSET = 0;
    /** default value for the limit parameter */
    static final int DEFAULT_LIMIT = 100;
    /** the maximum value for the limit parameter */
    static final int MAX_LIMIT = 5000;
    /** the columns parameter */
    protected static final String COLS = "cols";
    /** the zip parameter */
    protected static final String ZIP = "zip";
    /** the custom fields parameter */
    protected static final String CFS_COL = "cfs";
    /** problem name column parameter */
    protected static final String PROBLEM_COL = "problem_name";
    /** student column parameter */
    protected static final String STUDENT_COL = "anon_student_id";
    /** row column parameter */
    protected static final String ROW_COL = "row";
    /** problem hierarchy column parameter */
    protected static final String PROBLEM_HIERARCHY_COL = "problem_hierarchy";
    /** step name column parameter */
    protected static final String STEP_COL = "step_name";
    /** knowledge components column parameter */
    protected static final String KCS_COL = "kcs";
    /** condition column parameter */
    protected static final String CONDITION_COL = "condition";
    /** row column heading */
    protected static final String ROW_HEADING = "Row";
    /** problem hierarchy column header */
    protected static final String PROBLEM_HIERARCHY_HEADING = "Problem Hierarchy";
    /** headers column parameter */
    protected static final String HEADERS = "headers";
    /** title parameter for external analysis*/
    protected static final String TITLE = "title";
    /** description parameter for external analysis*/
    protected static final String DESCRIPTION = "description";
    /** kc_model parameter for external analysis*/
    protected static final String KC_MODEL = "kc_model";
    /** statistical_model parameter for external analysis*/
    protected static final String STATISTICAL_MODEL = "statistical_model";
    /** type parameter for custom field*/
    protected static final String TYPE = "type";
    /** level parameter for custom field*/
    protected static final String LEVEL = "level";
    /** name parameter for custom field*/
    protected static final String NAME = "name";
    /** URL parameter for custom field*/
    protected static final String URL = "url";
    /** Max cusotm field description length. */
    protected static final int CF_MAX_DESCRIPTION_LEN = 255;
    /** Max custom field name length. */
    protected static final int CF_MAX_NAME_LEN = 255;
    /** post data parameter for POST*/
    protected static final String POST_DATA = "postData";
    /** post data parameter for POST*/
    protected static final String BODY_REQUEST = "the body of the request";
    /** what to put in empty columns */
    private static final String EMPTY_COL = ".";
    /** discourse id param */
    protected static final String DISCOURSE_ID = "discourseId";
    /** discourse name param */
    protected static final String DISCOURSE_NAME = "name";
    /** skill parameter for learning curve points*/
    protected static final String SKILL = "skill";
    /** User id for authorization requests. */
    protected static final String USER_ID = "userId";
    /** Project name for authorization requests. */
    protected static final String PROJECT_NAME = "projectName";
    /** Authorization action. */
    protected static final String ACTION = "action";
    /** the web service request */
    private final HttpServletRequest req;
    /** the web service response */
    private final HttpServletResponse resp;
    /** all parameters for this request, including path parameters */
    private final Map<String, Object> params;
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
   /** variable to represents which environment the application is running on */
    private String environment = "";
    /** Access parameters. */
    public enum AccessParam {
        /** all: public, view, edit or private.
         *  view: view or public access.
         *  editable: edit privileges. */
        ALL(WebService.ALL), VIEWABLE(WebService.VIEWABLE), EDITABLE(WebService.EDITABLE);

        /** The string value of this parameter. */
        private String paramVal;

        /**
         * Create an access parameter for the given value.
         * @param param the string representation of the access parameter
         */
        AccessParam(String param) { this.paramVal = param; }

        /**
         * The string value of this parameter.
         * @return the string value of this parameter
         */
        public String getParamVal() { return paramVal; }

        /**
         * Get the access param for the string.
         * @param param string identifying the access param
         * @return the access param corresponding to the string
         */
        public static AccessParam getParam(String param) {
            for (AccessParam access : asList(ALL, VIEWABLE, EDITABLE)) {
                if (access.getParamVal().equals(param)) { return access; }
            }
            return VIEWABLE;
        }
    };

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public WebService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        this.req = req; this.resp = resp; this.params = params;
        setEnvironmentByHTTPRequest(req);
    }

    /**
     * The web service request.
     * @return the web service request
     */
    protected HttpServletRequest getReq() { return req; }

    /**
     * The web service response.
     * @return the web service response
     */
    protected HttpServletResponse getResp() { return resp; }

    /**
     * All parameters for this request, including path parameters.
     * @return all parameters for this request, including path parameters
     */
    protected Map<String, Object> getParams() { return params; }

    /**
     * Write the string as the response content.
     * @param str the string to write
     * @throws IOException if something goes wrong
     */
    protected void writeString(String str) throws IOException {
        AbstractServlet.writeString(getResp(), "", str);
    }

    /**
     * Override for the HTTP GET method.  Default returns a "405 method not allowed" status.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) { writeMethodNotAllowedError(); }

    /**
     * Override for the HTTP PUT method.  Default returns a "405 method not allowed" status.
     */
    public void put() { writeMethodNotAllowedError(); }

    /**
     * Override for the HTTP POST method.  Default returns a "405 method not allowed" status.
     * @param wsUserLog web service user log
     */
    public void post(WebServiceUserLog wsUserLog) { writeMethodNotAllowedError(); }

    /**
     * Override for the HTTP DELETE method.  Default returns a "405 method not allowed" status.
     */
    public void delete() { writeMethodNotAllowedError(); }

    /** Write an error message indicating that the HTTP method in the request is not supported. */
    protected void writeMethodNotAllowedError() {
        writeError(UNKNOWN_ERR, SC_METHOD_NOT_ALLOWED,
                "Operation not supported.");
    }

    /**
     * Check whether "acceptable" as one of the requestor's acceptable MIME formats.
     * @param acceptable a MIME type
     * @return whether "acceptable" as one of the requestor's acceptable MIME formats.
     */
    protected boolean acceptable(String acceptable) {
        String accepts = getReq().getHeader("accept");
        boolean canAccept = accepts != null && accepts.contains(acceptable);

        if (!canAccept) {
            writeError(UNKNOWN_ERR, SC_NOT_ACCEPTABLE,
                    "This content is available only as ", acceptable, ".");
        }

        return canAccept;
    }

    /**
     * The helper is used for anything requiring database access.
     * @return the web services helper
     */
    protected WebServiceHelper helper() { return HelperFactory.DEFAULT.getWebServiceHelper(); }

    /**
     * Parse the parameter as an integer.
     * @param param key for a parameter
     * @return the parameter parsed into an integer
     * @throws WebServiceException if the parameter is not a valid integer
     */
    protected int intParam(String param) throws WebServiceException {
        try {
            return parseInt(stringParam(param));
        } catch (NumberFormatException nfe) {
            throw invalidParamValueException(param, getParams().get(param));
        }
    }

    /**
     * Parse the parameter as a long.
     * @param param key for a parameter
     * @return the parameter parsed into a long
     * @throws WebServiceException if the parameter is not a valid long
     */
    protected long longParam(String param) throws WebServiceException {
        try {
            return parseLong(stringParam(param));
        } catch (NumberFormatException nfe) {
            throw invalidParamValueException(param, getParams().get(param));
        }
    }

    /**
     * Parse the parameter as a double.
     * @param param key for a parameter
     * @return the parameter parsed into a double
     * @throws WebServiceException if the parameter is not a valid double
     */
    protected double doubleParam(String param) throws WebServiceException {
        try {
            return parseDouble(stringParam(param));
        } catch (NumberFormatException nfe) {
            throw invalidParamValueException(param, getParams().get(param));
        }
    }

    /**
     * Get the parameter as a String.
     * @param param the parameter name
     * @return the parameter as a String
     */
    protected String stringParam(String param) { return (String)getParams().get(param); }

    /**
     * Parse the parameter as an boolean.
     * If the parameter is "true", return true, otherwise return the default value.
     * @param param key for a parameter
     * @param defaultValue the value to return if the parameter is not specified
     * @return true if the parameter value is "true", false otherwise
     */
    protected boolean booleanParam(String param, boolean defaultValue) {
        String stringParam = stringParam(param);
        return stringParam == null ? defaultValue : "true".equals(stringParam);
    }

    /**
     * Parse the parameter as an boolean.
     * If the parameter is "true", return true, otherwise return false.
     * @param param key for a parameter
     * @return true if the parameter value is "true", false otherwise
     */
    protected boolean booleanParam(String param) { return booleanParam(param, false); }

    /**
     * Get the access parameter.
     * @return the access parameter
     */
    protected AccessParam accessParam() { return AccessParam.getParam(stringParam("access")); }

    /**
     * Get the verbose parameter.
     * @return the verbose parameter
     */
    protected boolean verboseParam() { return booleanParam("verbose"); }

    /**
     * Integer parameter indicating a dataset id.
     * @return integer parameter indicating a dataset id
     * @throws WebServiceException if the parameter is not a valid integer
     */
    protected int datasetParam() throws WebServiceException { return intParam(DATASET_ID); }

    /**
     * Integer parameter indicating a sample id.
     * @return integer parameter indicating a sample id
     * @throws WebServiceException if the parameter is not a valid integer
     */
    protected int sampleParam() throws WebServiceException { return intParam(SAMPLE_ID); }

    /**
     * Integer parameter indicating a sample id.
     * @return integer parameter indicating a sample id
     * @throws WebServiceException if the parameter is not a valid integer
     */
    protected int externalAnalysisParam()
        throws WebServiceException { return intParam(EXTERNAL_ANALYSIS_ID); }

    /**
     * Get the mine parameter.
     * @return the mine parameter
     */
    protected boolean mineParam() { return booleanParam("mine"); }

    /**
     * Long parameter indicating a custom field id.
     * @return long parameter indicating a custom field id
     * @throws WebServiceException if the parameter is not a valid long
     */
    protected long customFieldParam()
        throws WebServiceException { return longParam(CUSTOM_FIELD_ID); }

    /**
     * Long parameter indicating a discourse id.
     * @return long parameter indicating a discourse id
     * @throws WebServiceException if the parameter is not a valid long
     */
    protected long discourseParam() throws WebServiceException { return longParam(DISCOURSE_ID); }

    /**
     * Parse a comma separated list of parameter values.
     * @param param the parameter
     * @return the list of values for the parameter
     */
    protected List<String> multipleStringParam(String param) {
        String paramValStr = stringParam(param);
        List<String> paramVals = emptyList();

        if (paramValStr != null) { paramVals = asList(paramValStr.split(",")); }

        return paramVals;
    }

    /**
     * Validate the list of values for the parameters.
     * @param param the parameters
     * @param validValues valid possible values for the parameter
     * @throws WebServiceException thrown if any of the values are invalid
     */
    protected void validateMultipleParam(String param, Set<String> validValues)
    throws WebServiceException {
        for (String paramVal : multipleStringParam(param)) {
            if (!validValues.contains(paramVal)) {
                throw invalidParamValueException(param, paramVal);
            }
        }
    }
    /**
     * Set environment variable to determine which XSD to use based on server name
     * @param req HTTPServletRequest
     */
    protected void setEnvironmentByHTTPRequest(HttpServletRequest req) {
        if (req.getServerName().indexOf(WebServiceXMLMessage.QA_SERVER_NAME) >= 0) {
            environment = WebServiceXMLMessage.QA_ENV;
        } else if (req.getServerName().indexOf(WebServiceXMLMessage.PROD_SERVER_NAME) >= 0) {
            environment = WebServiceXMLMessage.PROD_ENV;
        } else if (req.getServerName().indexOf(WebServiceXMLMessage.LOCAL_SERVER_NAME) >= 0) {
            environment = WebServiceXMLMessage.LOCAL_ENV;
        } else if (req.getServerName().indexOf(WebServiceXMLMessage.DEMO_SERVER_NAME) >= 0) {
            environment = WebServiceXMLMessage.DEMO_ENV;
        } else if (req.getServerName().indexOf(WebServiceXMLMessage.FOSSIL_SERVER_NAME) >= 0) {
            environment = WebServiceXMLMessage.FOSSIL_ENV;
        }
    }

    /**
     * Use this to get regular form field (input type="text|radio|checkbox|etc", select, etc)
     * for post that has file upload 
     * @return Map with field as key
     */
    protected Map<String, String> getFormFieldsForFileUploadPost() throws WebServiceException {
            Map<String, String> formFieldValues = new HashMap<String, String>();
            try {
                    List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
                    for (FileItem item : items) {
                        if (item.isFormField()) {
                            String fieldName = item.getFieldName();
                            String fieldValue = item.getString();
                            formFieldValues.put(fieldName, fieldValue);
                        } 
                    }
                } catch (FileUploadException ex) {
                        throw new WebServiceException(INVALID_POST_REQUEST_BODY_ERR,
                                        "Cannot parse values of post form that has multipart: " + ex.getMessage() + ".");
                } 
            return formFieldValues;
    }
    
    /**
     * Get the attached files as InputStreams. can be multiple files
     * @return Map<String, InputStream> file name is the key and file content (InputStream) as value
     */
    protected Map<String, InputStream> getAttachedFileForPost() throws WebServiceException{
            Map<String, InputStream> fileNameContent = null;
            try {
                    List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
                    for (FileItem item : items) {
                        if (!item.isFormField()) {
                            // Process form file field (input type="file").
                            //String fieldName = item.getFieldName();
                            String fileName = FilenameUtils.getName(item.getName());
                            fileNameContent = new HashMap<String, InputStream>();
                            fileNameContent.put(fileName, item.getInputStream());
                        }
                    }
                } catch (FileUploadException ex) {
                        throw new WebServiceException(INVALID_POST_REQUEST_BODY_ERR,
                                        "Cannot parse multipart request,  " + ex.getMessage() + ".");
                } catch (IOException ex) {
                        throw new WebServiceException(INVALID_POST_REQUEST_BODY_ERR,
                                        "Cannot parse multipart request,  " + ex.getMessage() + ".");
                } 
            return fileNameContent;
    }
    
    /**
     * Get the environment variable
     * @return environment
     */
    protected String getEnvironment() {
        return this.environment;
    }
    
    /**
     * Write one or more DTOs as XML in the response.
     * @param <T> the kind of DTO
     * @param dtos the DTOs
     * @param resultMsg the result_message field to be included in the message
     * (defaults to "Success." if null)
     * @throws Exception for non IO problems (IO exception is handled, other exceptions
     * probably XML related)
     */
    protected <T extends DTO> void writeDTOXML(List<T> dtos, String resultMsg) throws Exception {
        PrintWriter writer = null;

        try {
            if (acceptable("text/xml")) {

                WebServiceXMLMessage msg = new WebServiceXMLMessage(this.getEnvironment());

                if (resultMsg != null) { msg.setResultMessage(resultMsg); }
                for (DTO dto : dtos) { msg.addDTO(dto); }
                // check whether debugging is on before constructing the XML string
                if (logger.isDebugEnabled()) {
                    logDebug("dto XML is ", msg.xmlString());
                }
                msg.writeMessage(getResp().getWriter());
            }
        } catch (IOException ioe) {
            logger.error("Unable to deliver dataset.", ioe);
            writeInternalError();
        } finally {
            if (writer != null) { writer.close(); }
        }
    }

    /**
     * Write one or more DTOs as XML in the response.
     * @param <T> the kind of DTO
     * @param dtos the DTOs
     * @throws Exception for non IO problems (IO exception is handled, other exceptions
     * probably XML related)
     */
    protected <T extends DTO> void writeDTOXML(List<T> dtos) throws Exception {
        writeDTOXML(dtos, null);
    }

    /**
     * Write one or more DTOs as XML in the response.
     * This is a convenience method for the signature that takes a list.
     * @param dtos the DTOs
     * @throws Exception for non IO problems (IO exception is handled, other exceptions
     * probably XML related)
     */
    protected void writeDTOXML(DTO... dtos) throws Exception { writeDTOXML(asList(dtos)); }

    /**
     * Write an error message indicating what went wrong, in the form of a pslc_datashop_message
     * XML element.
     * @param successMessage the success message
     */
    protected void writeSuccess(String successMessage) {
        try {
            writeSuccessResponse(getResp(), successMessage);
        } catch (Exception exception) {
            logger.error("Exception in writeSuccess.", exception);
        }
    }

    /**
     * Write an error message indicating what went wrong, in the form of a pslc_datashop_message
     * XML element.
     * @param successMessage the success message
     * @param customFields custom fields map
     */
    protected void writeSuccessWithCustomField(String successMessage,
            Map<Object, Object> customFields) {
        try {
                writeSuccessResponseWithCustomField(getResp(), successMessage, customFields);
        } catch (Exception exception) {
            logger.error("Exception in writeSuccess.", exception);
        }
    }

    /**
     * Write an error message indicating what went wrong, in the form of a pslc_datashop_message
     * XML element.
     * @param errStatus error code from API documentation
     * @param responseCode HTTP error code
     * @param errArgs concatenate the String representations of these to create the error message
     */
    protected void writeError(int errStatus, int responseCode, Object... errArgs) {
        try {
            writeErrorResponse(getResp(), errStatus, responseCode, concatenate(errArgs));
        } catch (Exception e) {
            logger.error("An error in our error handling code!  We just can't win!", e);
        }
    }

    /**
     * Create an error message from the exception error code and message.
     * @param exception indicates the problem that occurred
     */
    protected void writeError(WebServiceException exception) {
        writeError(exception.getErrorCode(), exception.getResponseCode(),
                exception.getErrorMessage());
    }

    /** Catch all for any error whose cause we don't know. */
    protected void writeInternalError() {
        writeError(UNKNOWN_ERR, SC_INTERNAL_SERVER_ERROR, "Unknown error.");
    }

    /**
     * Throws a WebServiceException if any parameters are not valid.
     * @param validParams valid parameter keys
     * @throws WebServiceException indicates either an invalid parameter.
     */
    protected void validateParameters(WebServiceUserLog wsUserLog,
            Set<String> validParams) throws WebServiceException {
        for (Map.Entry<String, Object> param : getParams().entrySet()) {
            String key = param.getKey();
            if (!validParams.contains(key)) {
                throw new WebServiceException(INVALID_PARAM_ERR, "Invalid request parameter: "
                        + key + ".");
            }
            checkForDataset(wsUserLog, key);
        }
    }

    /**
     * Throws a WebServiceException if any parameters are not valid.
     * @param validParams valid parameter keys
     * @param validParamValues valid parameter values for parameter keys
     * @throws WebServiceException indicates either an invalid parameter or invalid parameter
     * value.
     */
    protected void validateParameters(WebServiceUserLog wsUserLog,
            Set<String> validParams, Map<String, Set<String>> validParamValues)
            throws WebServiceException {
        for (Map.Entry<String, Object> param : getParams().entrySet()) {
            String key = param.getKey();
            if (!validParams.contains(key)) {
                throw new WebServiceException(INVALID_PARAM_ERR, "Invalid request parameter: "
                        + key + ".");
            } else {
                Set<String> validValues = validParamValues.get(key);
                Object value = param.getValue();
                if (validValues != null && !validValues.contains(value)) {
                    throw invalidParamValueException(key, value);
                }
                checkForDataset(wsUserLog, key);
            }
        }
    }

    /**
     * Throws a WebServiceException if any parameters are not valid.
     * @param validParams valid parameter keys
     * @param validParamValues valid parameter values for parameter keys
     * @param excludeParams exclude from checking values
     * @throws WebServiceException indicates either an invalid parameter or invalid parameter
     * value.
     */
    protected void validateParameters(WebServiceUserLog wsUserLog, Set<String> validParams,
            Map<String, Set<String>> validParamValues,
            Set<String> excludeParams) throws WebServiceException {
        for (Map.Entry<String, Object> param : getParams().entrySet()) {
            String key = param.getKey();
            if (!validParams.contains(key)) {
                throw new WebServiceException(INVALID_PARAM_ERR,
                        "Invalid request parameter: " + key + ".");
            } else {
                Set<String> validValues = validParamValues.get(key);
                Object value = param.getValue();
                if (!excludeParams.contains(key)
                        && (validValues != null && !validValues.contains(value))) {
                    throw invalidParamValueException(key, value);
                }
                checkForDataset(wsUserLog, key);
            }
        }
    }

    /**
     * Check the key to see if its the dataset id and if so, set it in the user log.
     * @param wsUserLog web service user log
     * @param key the key for the parameter
     * @throws WebServiceException exception
     */
    private void checkForDataset(WebServiceUserLog wsUserLog, String key)
            throws WebServiceException {
        if (key.equals(DATASET_ID)) {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem datasetItem = datasetDao.get(intParam(DATASET_ID));
            wsUserLog.setDataset(datasetItem);
        }
    }

    /**
     * The user requesting this service.
     * (Which we stash in the request when performing authentication.)
     * @return the user requesting this service
     */
    protected UserItem getAuthenticatedUser() {
        return (UserItem)getReq().getAttribute("authenticatedUser");
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }

    /**
     * Value for the offset parameter, or the default value if not specified.
     * @return the offset parameter, or the default value if not specified
     * @throws WebServiceException if invalid (non-integer) offset parameter value
     */
    protected int offsetParam() throws WebServiceException {
        if (stringParam(OFFSET) == null) { return DEFAULT_OFFSET; }
        int offset = intParam(OFFSET);
        if (offset < 0) { throw invalidParamValueException(OFFSET, offset); }
        return offset;
    }

    /**
     * Check for the cfs parameter, all/none/comma delimited numbers.
     * @throws WebServiceException if invalid
     */
    protected void validateCFParam() throws WebServiceException {
        List<String> cfs = multipleStringParam(CFS_COL);
        if (cfs.size() == 1 && !NONE.equals(cfs.get(0)) && !ALL.equals(cfs.get(0))) {
            boolean acceptableNumber = false;
            try {
                Integer.parseInt(cfs.get(0));
                acceptableNumber = true;
            } catch (NumberFormatException nfe) {
                //FIXME do something
            }
            if (!acceptableNumber) {
                throw invalidParamValueException(CFS_COL, cfs.get(0));
            }
        } else if (cfs.size() > 1) {
            for (String cf : cfs) {
                try {
                    parseInt(cf);
                } catch (NumberFormatException nfe) {
                    throw invalidParamValueException(CFS_COL, cf);
                }
            }
        }
    }

    /**
     * Value for the limit parameter, or the default value if not specified.
     * @return the limit parameter, or the default value if not specified
     * @throws WebServiceException if limit parameter is invalid or exceeds the maximum value
     */
    protected int limitParam() throws WebServiceException {
        if (stringParam(LIMIT) == null) { return DEFAULT_LIMIT; }
        int limit = intParam(LIMIT);
        if (limit > MAX_LIMIT || limit < 0) { throw invalidParamValueException(LIMIT, limit); }
        return limit;
    }

    /** Get the values for the col parameter. @return the values for the col parameter */
    protected List<String> colsParam() { return multipleStringParam(COLS); }

    /** Get the zip parameter. @return the zip parameter */
    protected boolean zipParam() { return booleanParam(ZIP); }

    /**
     * Create and initialize a zip output stream on the response,
     * and set the appropriate response values.
     * @param entryName the entry name
     * @return an initialized zip output stream
     * @throws IOException if something goes wrong
     */
    protected ZipOutputStream initZip(String entryName) throws IOException {
        getResp().setContentType("application/zip");
        getResp().setHeader("Content-Disposition", "filename=" + entryName + ".zip");

        ZipOutputStream zip = new ZipOutputStream(getResp().getOutputStream());

        zip.setLevel(DEFAULT_COMPRESSION);
        zip.setMethod(ZipOutputStream.DEFLATED);
        zip.putNextEntry(new ZipEntry(entryName + ".txt"));

        return zip;
    }

    /**
     * Close the current zip entry and the stream.
     * @param zip a zip output stream
     * @throws IOException if something goes wrong
     */
    protected void finishZip(ZipOutputStream zip) throws IOException {
        zip.closeEntry();
        zip.finish();
        zip.close();
    }

    /**
     * Get the All Data sample id for the dataset parameter.
     * @return the All Data sample id for the dataset parameter
     * @throws WebServiceException if something goes wrong
     */
    protected int allDataSampleId() throws WebServiceException {
        return helper().allDataSampleId(getAuthenticatedUser(), datasetParam());
    }

    /** Headers parameter. @return headers parameter */
    protected boolean headerParam() { return booleanParam(HEADERS, true); }

    /**
     * Concatenate values in row with tabs, and append tabs at the end to insure that each
     * exported row has the same number of columns.
     * @param row the current row
     * @param colIndices indices of the columns to include
     * (used to determine the number of tabs to append)
     * @return the string to export for row
     */
    protected String buildRow(List<String> row, List<Integer> colIndices) {
        // Replace all empty values in row with "."
        for (int i = 0; i < row.size(); i++) {
            if (row.get(i).isEmpty()) { row.set(i, EMPTY_COL); }
        }

        StringBuffer buf = new StringBuffer(join(SEP, row));

        for (int i = row.size(); i < colIndices.size(); i++) { buf.append(SEP + EMPTY_COL); }
        return buf.toString();
    }
}
