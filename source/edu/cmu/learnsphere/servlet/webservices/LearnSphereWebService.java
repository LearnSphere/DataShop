/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.learnsphere.servlet.webservices.LearnSphereWebServiceUserLog;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.webservices.WebService;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceHelper;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceUserLog;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceXMLMessage;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
import edu.cmu.pslc.datashop.servlet.webservices.WebService.AccessParam;
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
import static edu.cmu.pslc.datashop.servlet.
              webservices.WebServiceException.INVALID_PARAM_ERR;
import static edu.cmu.pslc.datashop.servlet.
                webservices.WebServiceException.INVALID_PARAM_VAL_ERR;
import static edu.cmu.pslc.datashop.servlet.
              webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.servlet.
              webservices.WebServiceException.invalidParamValueException;
import static java.util.Collections.emptyList;
import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

/**
 * Represents a LearnSphere web service.
 *
 * @author hui cheng
 * @version $Revision: 15843 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2019-02-01 14:32:54 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereWebService extends WebService{
     /** workflow id param */
    protected static final String WORKFLOW_ID = "workflow_id";
    /** global param already exists in WebService*/
    protected static final String GLOBAL = "global";
    /** data_access param */
    protected static final String DATA_ACCESS = "data_access";
    /** data_access param value shared */
    protected static final String SHARED = "shared";
    /** data_access param value private */
    protected static final String PRIVATE = "private";
    /** data_access param value locked */
    protected static final String LOCKED = "locked";
    /** data_access param value locked* */
    protected static final String LOCKED_STAR = "locked*";
    /** data_access param value request */
    protected static final String REQUEST = "request";
    /** data_access param value locked* */
    protected static final String REQUEST_STAR = "request*";
    /** file_type param */
    protected static final String FILE_TYPE = "file_type";
    /** file_type param input value*/
    protected static final String INPUT = "input";
    /** file_type param output value*/
    protected static final String OUTPUT = "output";
    /** new_workflow_name param*/
    protected static final String NEW_WORKFLOW_NAME = "new_workflow_name";
    /** description param*/
    protected static final String DESCRIPTION = "description";
    /** valid values for the mine parameter */
    protected static final Set<String> MINE_PARAMS = set(TRUE, FALSE);
    /** valid values for the GLOBAL parameter */
    protected static final Set<String> GLOBAL_PARAMS = set(TRUE, FALSE);
    /** valid values for the FILE_TYPE parameter */
    protected static final Set<String> FILE_TYPE_PARAMS = set(INPUT, OUTPUT);
    /** valid param values for the data_access parameter */
    protected static final Set<String> DATA_ACCESS_PARAMS = set(ALL, SHARED, PRIVATE, LOCKED, LOCKED_STAR, REQUEST, REQUEST_STAR);
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /** Data_access parameters. */
    public enum DataAccessParam {
        /** all, shared, locked, locked*, request, request*
         */
        ALL(WebService.ALL), SHARED(LearnSphereWebService.SHARED), PRIVATE(LearnSphereWebService.PRIVATE), 
        LOCKED(LearnSphereWebService.LOCKED), LOCKED_STAR(LearnSphereWebService.LOCKED_STAR),
        REQUEST(LearnSphereWebService.REQUEST), REQUEST_STAR(LearnSphereWebService.REQUEST_STAR);

        /** The string value of this parameter. */
        private String paramVal;

        /**
         * Create a data_access parameter for the given value.
         * @param param the string representation of the access parameter
         */
        DataAccessParam(String param) { 
                this.paramVal = param; 
                if (this.paramVal == null || this.paramVal.equalsIgnoreCase(""))
                        this.paramVal = "all";  
        }

        /**
         * The string value of this parameter.
         * @return the string value of this parameter
         */
        public String getParamVal() { return paramVal; }

        /**
         * Get the data_access param for the string.
         * @param data_param string identifying the data_access param
         * @return the data_access param corresponding to the string
         */
        public static DataAccessParam getParam(String param) {
            for (DataAccessParam access : asList(ALL, SHARED, PRIVATE, LOCKED, LOCKED_STAR, REQUEST, REQUEST_STAR)) {
                if (access.getParamVal().equals(param)) { return access; }
                else if ("locked*".equals(param)) {return LOCKED_STAR;}
                else if ("request*".equals(param)) {return REQUEST_STAR;}
            }
            return ALL;
        }
    };
    
    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public LearnSphereWebService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }
    
    /**
     * Override for the HTTP POST method.  Default returns a "405 method not allowed" status.
     * @param wsUserLog LearnSphere web service user log
     */
    protected void post(LearnSphereWebServiceUserLog wsUserLog) { writeMethodNotAllowedError(); }

    /**
     * Override for the HTTP GET method.  Default returns a "405 method not allowed" status.
     * @param wsUserLog LearnSphere web service user log
     */
    protected void get(LearnSphereWebServiceUserLog wsUserLog) { 
            writeMethodNotAllowedError();
    }

    /**
     * Throws a WebServiceException if any parameters are not valid.
     * @param validParams valid parameter keys
     * @param validParamValues valid parameter values for parameter keys
     * @throws WebServiceException indicates either an invalid parameter or invalid parameter
     * value.
     */
    protected void validateParameters(LearnSphereWebServiceUserLog wsUserLog,
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
                    throw new WebServiceException(INVALID_PARAM_VAL_ERR, "Invalid value for parameter "
                                    + param + ": " + value + ".");
                }
                if (key.equals(WORKFLOW_ID)) {
                        wsUserLog.setWorkflow(longParam(WORKFLOW_ID));
                }
            }
        }
    }
    
    /**
     * Write one or more DTOs as XML in the response. same as WebService excpet using learnsphere_message
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

                WebServiceXMLMessage msg = new WebServiceXMLMessage(this.getEnvironment(), "learnsphere_message");
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
     * Get the data_access parameter.
     * @return the data_access parameter, default is all
     */
    protected DataAccessParam dataAccessParam() { 
            return DataAccessParam.getParam(stringParam("data_access")); 
    }
    
    /**
     * Get the mine parameter.
     * @return the mine parameter
     */
    protected Boolean lsMineParam() {
            String stringParam = stringParam(MINE);
            if (stringParam == null)
                    return null;
            else if (stringParam.equalsIgnoreCase("true"))
                    return new Boolean(true);
            else if (stringParam.equalsIgnoreCase("false"))
                    return new Boolean(false);
            return null;
    }

    /**
     * Get the global parameter.
     * @return the global parameter
     */
    protected Boolean globalParam() {
            String stringParam = stringParam(GLOBAL);
            if (stringParam == null)
                    return null;
            else if (stringParam.equalsIgnoreCase("true"))
                    return new Boolean(true);
            else if (stringParam.equalsIgnoreCase("false"))
                    return new Boolean(false);
            return null;
    }
    
    /**
     * Integer parameter indicating a workflow id.
     * @return integer parameter indicating a workflow id
     * @throws WebServiceException if the parameter is not a valid integer
     */
    protected int workflowParam() throws WebServiceException { return intParam(WORKFLOW_ID); }
    
    /**
     * String parameter indicating type of file.
     * @return string parameter indicating a file type
     */
    protected String fileTypeParam() throws WebServiceException { return stringParam(FILE_TYPE); }
    
    /**
     * String parameter new_workflow_name.
     * @return string parameter indicating new workflow name
     */
    protected String newWorkflowNameParam() throws WebServiceException { return stringParam(NEW_WORKFLOW_NAME); }
    
    /**
     * String parameter description.
     * @return string parameter description
     */
    protected String descriptionParam() throws WebServiceException { return stringParam(DESCRIPTION); }
    
    
    /**
     * The helper is used for anything requiring database access.
     * @return the web services helper
     */
    protected LearnSphereWebServiceHelper learnSphereHelper() { return HelperFactory.DEFAULT.getLearnSphereWebServiceHelper(); }

}
