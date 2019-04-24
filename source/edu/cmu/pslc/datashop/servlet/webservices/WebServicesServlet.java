/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.LogUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetUserLogItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.logging.util.DateTools;
import static java.net.URLDecoder.decode;
import static java.util.Arrays.asList;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static edu.cmu.pslc.datashop.util.CollectionUtils.iter;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.NO_MATCHING_URL_ERR;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;;

/**
 * Entry point for all web services requests.
 *
 * @author Jim Rankin
 * @version $Revision: 15746 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-10 16:19:23 -0500 (Mon, 10 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WebServicesServlet extends AbstractServlet {
    /** display this when no service matches the incoming URL */
    protected static final String NO_MATCHING_URL_MSG =
        "No web service found matching the URL."
        + " For a list of valid URLs, see http://pslcdatashop.org/api";
    /** handles requests for multiple samples */
    private static final WebServiceHandler SAMPLES_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/samples/?", SamplesService.class, "datasetId");
    /** handles requests for a single sample */
    private static final WebServiceHandler SAMPLE_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/samples/(\\d+)/?", SampleService.class,
                "datasetId", "sampleId");
    /** handles requests for multiple datasets */
    private static final WebServiceHandler DATASETS_HANDLER =
        new WebServiceHandler("/datasets/?", DatasetsService.class);
    /** handles requests for a single dataset */
    private static final WebServiceHandler DATASET_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/?", DatasetService.class, "datasetId");
    /** handles requests for adding a new dataset */
    private static final WebServiceHandler DATASET_ADD_HANDLER =
            new WebServiceHandler("/datasets/add/?", DatasetAddService.class);
    /** handles requests for deleting remote dataset */
    private static final WebServiceHandler DATASET_DELETE_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/delete/?",
                                  DatasetDeleteService.class, "datasetId");
    /** handles requests for setting meta-data on remote dataset */
    private static final WebServiceHandler DATASET_SET_INFO_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/set?",
                                  DatasetSetInfoService.class, "datasetId");
    /** handles requests for transactions */
    private static final WebServiceHandler TRANSACTIONS_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/samples/(\\d+)/transactions/?",
                TransactionsService.class, "datasetId", "sampleId");
    /** handles requests for transactions for the All Data sample */
    private static final WebServiceHandler ALL_DATA_TRANSACTIONS_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/transactions/?",
                AllDataTransactionsService.class, "datasetId");
    /** handles requests for steps */
    private static final WebServiceHandler STEPS_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/samples/(\\d+)/steps/?",
                    StepRollupService.class, "datasetId", "sampleId");
    /** handles requests for steps for the All Data sample */
    private static final WebServiceHandler ALL_DATA_STEPS_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/steps/?",
                AllDataStepRollupService.class, "datasetId");
    /** handles requests for multiple analyses */
    private static final WebServiceHandler ANALYSES_HANDLER =
        new WebServiceHandler("/datasets/(\\d+)/analyses/?",
                ExternalAnalysesService.class, "datasetId");
    /** handles requests for one analysis */
    private static final WebServiceHandler ANALYSIS_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/analyses/(\\d+)/?",
                    ExternalAnalysisService.class, "datasetId", "externalAnalysisId");
    /** handles requests for one analysis */
    private static final WebServiceHandler ANALYSIS_DELETE_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/analyses/(\\d+)/delete/?",
                    ExternalAnalysisDeleteService.class, "datasetId", "externalAnalysisId");
    /** handles requests for one analysis */
    private static final WebServiceHandler ANALYSIS_ADD_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/analyses/add?",
                    ExternalAnalysisAddService.class, "datasetId");
    /** handles requests for a single custom field meta data */
    private static final WebServiceHandler CUSTOM_FIELD_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/customfields/(\\d+)/?",
                    CustomFieldService.class, "datasetId", "customFieldId");
    /** handles requests for the meta data for all custom fields of dataset */
    private static final WebServiceHandler CUSTOM_FIELDS_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/customfields/?",
                    CustomFieldsService.class, "datasetId");
    /** handles requests for adding new custom field */
    private static final WebServiceHandler CUSTOM_FIELD_ADD_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/customfields/add?",
                    CustomFieldAddService.class, "datasetId");
    /** handles requests for deleting custom field description*/
    private static final WebServiceHandler CUSTOM_FIELD_DELETE_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/customfields/(\\d+)/delete?",
                    CustomFieldDeleteService.class, "datasetId", "customFieldId");
    /** handles requests for set custom field values*/
    private static final WebServiceHandler CUSTOM_FIELD_SET_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/customfields/(\\d+)/set?",
                    CustomFieldSetService.class, "datasetId", "customFieldId");
    /** handles requests for learning curve classify*/
    private static final WebServiceHandler LEARNING_CURVE_CLASSIFY_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/learningcurves/classify?",
                    LearningCurveClassifyService.class, "datasetId");
    /** handles requests for learning curve points*/
    private static final WebServiceHandler LEARNING_CURVE_POINTS_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/learningcurves/points?",
                                  LearningCurvePointsService.class, "datasetId");
    /** handles requests for kcm import*/
    private static final WebServiceHandler KCM_IMPORT_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/importkcm/?",
                    ImportKCMService.class, "datasetId");
    /** handles requests for kcm export*/
    private static final WebServiceHandler KCM_EXPORT_HANDLER =
            new WebServiceHandler("/datasets/(\\d+)/exportkcm/?",
                                  ExportKCMService.class, "datasetId");
    /** handles requests for a single remote instance */
    private static final WebServiceHandler INSTANCE_HANDLER =
        new WebServiceHandler("/instances/(\\d+)/?", InstanceService.class, "instanceId");
    /** handles requests for adding a remote instance */
    private static final WebServiceHandler INSTANCE_ADD_HANDLER =
            new WebServiceHandler("/instances/add/?", InstanceAddService.class);
    /** handles requests for setting attrs on a remote instance */
    private static final WebServiceHandler INSTANCE_SET_HANDLER =
        new WebServiceHandler("/instances/(\\d+)/set?", InstanceSetService.class, "instanceId");
    /** handles requests for set metrics values*/
    private static final WebServiceHandler METRICS_SET_HANDLER =
            new WebServiceHandler("/instances/(\\d+)/metrics/set?",
                                  MetricsSetService.class, "instanceId");
    /** handles requests for the Hello World service, to confirm that services are accessible */
    private static final WebServiceHandler HELLO_WORLD_HANDLER =
        new WebServiceHandler("/helloworld", HelloWorldService.class);
    /** handles requests for adding a new discourse */
    private static final WebServiceHandler DISCOURSE_ADD_HANDLER =
            new WebServiceHandler("/discourses/add/?", DiscourseAddService.class);
    /** handles requests for deleting remote discourse */
    private static final WebServiceHandler DISCOURSE_DELETE_HANDLER =
            new WebServiceHandler("/discourses/(\\d+)/delete/?",
                                  DiscourseDeleteService.class, "discourseId");
    /** handles requests for setting meta-data on remote discourse */
    private static final WebServiceHandler DISCOURSE_SET_INFO_HANDLER =
            new WebServiceHandler("/discourses/(\\d+)/set?",
                                  DiscourseSetInfoService.class, "discourseId");
    /** handles requests for setting authorization info */
    private static final WebServiceHandler AUTHORIZATION_SET_HANDLER =
            new WebServiceHandler("/auth/set/?", AuthorizationSetService.class);
    /** handles requests for querying authorization info */
    private static final WebServiceHandler AUTHORIZATION_HANDLER =
            new WebServiceHandler("/auth/?", AuthorizationService.class);

    /** handlers for routing incoming requests to the correct service */
    private static List<WebServiceHandler> handlers =
        asList(HELLO_WORLD_HANDLER, DATASETS_HANDLER, DATASET_HANDLER, DATASET_ADD_HANDLER,
               SAMPLES_HANDLER, SAMPLE_HANDLER, TRANSACTIONS_HANDLER, ALL_DATA_TRANSACTIONS_HANDLER,
               STEPS_HANDLER, ALL_DATA_STEPS_HANDLER, ANALYSES_HANDLER, ANALYSIS_HANDLER,
               ANALYSIS_DELETE_HANDLER, ANALYSIS_ADD_HANDLER, CUSTOM_FIELDS_HANDLER,
               CUSTOM_FIELD_HANDLER, CUSTOM_FIELD_ADD_HANDLER,
               CUSTOM_FIELD_SET_HANDLER, CUSTOM_FIELD_DELETE_HANDLER,
               LEARNING_CURVE_CLASSIFY_HANDLER, LEARNING_CURVE_POINTS_HANDLER,
               KCM_IMPORT_HANDLER, KCM_EXPORT_HANDLER, DATASET_DELETE_HANDLER,
               INSTANCE_HANDLER, INSTANCE_ADD_HANDLER, INSTANCE_SET_HANDLER, METRICS_SET_HANDLER,
               DATASET_SET_INFO_HANDLER, DISCOURSE_ADD_HANDLER, DISCOURSE_DELETE_HANDLER,
               DISCOURSE_SET_INFO_HANDLER, AUTHORIZATION_SET_HANDLER, AUTHORIZATION_HANDLER);
    /** pattern for parsing the authentication header */
    protected static final Pattern AUTH_HEADER_PATTERN = Pattern.compile("DATASHOP (.*):(.*)");

    /** HTTP methods. */
    public enum HttpMethod { GET, PUT, POST, DELETE };

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Variable to represents which environment the application is running on */
    private static String environment = "";

    /**
     * Confirm that the request was signed by the user identified by the API token in the
     * authentication header.
     * @param req the web service request
     * @return whether authentication succeeded or not
     */
    protected boolean authenticate(HttpServletRequest req) {
        String authHeader = req.getHeader("authorization");

        if (authHeader == null) { return false; }
        logDebug("auth header: ", authHeader);

        // parse authorization header
        Matcher m = AUTH_HEADER_PATTERN.matcher(authHeader);

        if (!m.matches()) { return false; }

        String apiToken = m.group(1);
        logDebug("api token is ", apiToken);
        String encrypted = null;

        try {
            encrypted = decode(m.group(2), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logDebug("UTF-8 is supported, so how did we get here?");
        }
        // look up the user for the API token
        UserDao dao = DaoFactory.DEFAULT.getUserDao();
        UserItem user = null;
        try {
            user = dao.findUserWithApiToken(apiToken);
        } catch (Exception e) {
            logDebug("Failed to find user with API token: ", apiToken);
            return false;
        }
        req.setAttribute("authenticatedUser", user);

        if (user == null) { return false; }
        logDebug("found user: ", user);

        // recreate the string used to sign this request
        // TODO supply a real content type and content MD5 for PUT/POST requests
        String reqDate = req.getHeader("date"), path = req.getPathInfo(),
            method = req.getMethod(), contentMD5 = "", contentType = "";
        logDebug("path is ", path);
        String toSign = join("\n", method, contentMD5, contentType, reqDate, path);

        return user.authenticate(toSign, encrypted);
    }

    /**
     * Set environment variable to determine which XSD to use based on server name
     * @param req HTTPServletRequest
     */
    protected static void setEnvironmentByHTTPRequest(HttpServletRequest req) {
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
     * Get the environment variable
     * @return environment
     */
    protected static String getEnvironment() {
        return environment;
    }

    /**
     * Find a handler matching the request, then delegate to the corresponding web service.
     * @param req the web service request
     * @param resp the web service response
     * @param method the HTTP method
     */
    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp,
                               HttpMethod method) {
        try {
            HelperFactory.DEFAULT.getWebServiceHelper().init(getBaseDir(),
                                                             getTxExportSpFilePath(),
                                                             getAggSpFilePath(),
                                                             isSendmailActive(),
                                                             getEmailAddressDatashopHelp());
            // dump the headers for debugging
            @SuppressWarnings("unchecked")
            Enumeration<String> hdrNames = req.getHeaderNames();
            for (String hdr : iter(hdrNames)) {
                logDebug("HEADER ", hdr, " : ", req.getHeader(hdr));
            }

            if (!authenticate(req)) {
                logDebug("authentication failed!");
                writeError(resp, SC_UNAUTHORIZED, "Authorization failed.  Check your credentials.");
                return;
            }
            logDebug("authentication succeeded!");
            boolean handled = false;

            UserItem currentUser = (UserItem)req.getAttribute("authenticatedUser");
            String action = UserLogger.WEB_SERV_ACTION;
            String info = "Path: " + req.getPathInfo() + " :: "
                + "Params: " + getDebugParamsString(req) + " :: ";
            WebServiceUserLog wsUserLog = new WebServiceUserLog();
            wsUserLog.setUser(currentUser);
            wsUserLog.setAction(action);
            wsUserLog.setInfo(action);

            setEnvironmentByHTTPRequest(req);
            for (WebServiceHandler handler : handlers) {
                long startTime = System.currentTimeMillis();

                wsUserLog = handler.handle(wsUserLog, req.getPathInfo(), method, req, resp);
                handled = wsUserLog.getHandled();

                if (handled) {
                    info += DateTools.getElapsedTimeString(startTime);
                    DatasetUserLogItem uLog = wsUserLog.getUserLogItem();
                    UserLogger.log(uLog.getDataset(), currentUser, action, info);
                    break;
                }
            }
            if (!handled) {
                writeError(resp, NO_MATCHING_URL_ERR, SC_NOT_FOUND, NO_MATCHING_URL_MSG);
            }
        } catch (Exception e) {
            logger.error("Failed to handle request: ", e);
            writeError(resp, SC_INTERNAL_SERVER_ERROR,
                       "Unknown error while handling request: " + e.getMessage());
        }
    }

    /**
     * Write a pslc_datashop_message with a success message.
     * @param resp
     * @param resp the HTTP response
     * @param successMsg the success message
     * @throws Exception most likely an IO or XML construction problem
     */
    public static void writeSuccessResponse(HttpServletResponse resp, String successMsg)
            throws Exception {
        WebServiceXMLMessage msg = new WebServiceXMLMessage(getEnvironment());
        msg.setResultCode(0);
        msg.setResultMessage("Success. " + successMsg);
        msg.writeMessage(resp.getWriter());
        resp.getWriter().close();
    }

    /**
     * Write a pslc_datashop_message with a success message.
     * @param resp the HTTP response
     * @param successMsg the success message
     * @param customFields custom field with field names and values
     * @throws Exception most likely an IO or XML construction problem
     */
    public static void writeSuccessResponseWithCustomField(
            HttpServletResponse resp, String successMsg,
            Map<Object, Object> customFields) throws Exception {
    WebServiceXMLMessage msg = new WebServiceXMLMessage(getEnvironment());
        msg.setResultCode(0);
        msg.setResultMessage("Success. " + successMsg);
        //process the custom fields
        for (Map.Entry<Object, Object> field : customFields.entrySet()) {
                msg.setCustomField(field.getKey().toString(), field.getValue().toString());
        }
        msg.writeMessage(resp.getWriter());
        resp.getWriter().close();
    }

    /**
     * Write a pslc_datashop_message encoding the error status code and message.
     * @param resp the HTTP response
     * @param errStatus the error code from the web services API documentation
     * @param responseCode the HTTP error status code
     * @param errMsg human readable error message
     * @throws Exception most likely an IO or XML construction problem
     */
    public static void writeErrorResponse(HttpServletResponse resp, int errStatus,
            int responseCode, String errMsg) throws Exception {
        resp.setStatus(responseCode);
        WebServiceXMLMessage msg = new WebServiceXMLMessage(getEnvironment());
        msg.setResultCode(errStatus);
        msg.setResultMessage("Error. " + errMsg);
        msg.writeMessage(resp.getWriter());

        resp.getWriter().close();
    }

    /**
     * Write a pslc_datashop_message encoding the error status code and message.
     * @param resp the web service response
     * @param errStatus the error code from the web services API documentation
     * @param responseCode the HTTP error status code
     * @param errMsg human readable error message
     */
    protected void writeError(HttpServletResponse resp, int errStatus, int responseCode,
            String errMsg) {
        try {
            logInfo("error ", errStatus, " ", errMsg);
            writeErrorResponse(resp, errStatus, responseCode, errMsg);
        } catch (Exception e) {
            logger.error("An error in our error handling code!  We just can't win!", e);
        }
    }

    /**
     * Write a pslc_datashop_message encoding the error status code and message.
     * Error code defaults to Unknown Error.
     * @param resp the web service response
     * @param errStatus the error code from the web services API documentation
     * @param errMsg human readable error message
     */
    protected void writeError(HttpServletResponse resp, int errStatus, String errMsg) {
        writeError(resp, UNKNOWN_ERR, errStatus, errMsg);
    }

    /**
     * Handle web service GET request.
     * @param req the web service request
     * @param resp the web service response
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logDebug("doGet");
        handleRequest(req, resp, HttpMethod.GET);
    }

    /**
     * Handle web service PUT request.
     * @param req the web service request
     * @param resp the web service response
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logDebug("doPut");
        handleRequest(req, resp, HttpMethod.PUT);
    }

    /**
     * Handle web service POST request.
     * @param req the web service request
     * @param resp the web service response
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logDebug("doPost");
        handleRequest(req, resp, HttpMethod.POST);
    }

    /**
     * Handle web service DELETE request.
     * @param req the web service request
     * @param resp the web service response
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        logDebug("doDelete");
        handleRequest(req, resp, HttpMethod.DELETE);
    }

    /**
     * We don't support HEAD requests.
     * @param req the web service request
     * @param resp the web service response
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doHead(req, resp);
        writeError(resp, SC_METHOD_NOT_ALLOWED, "Head requests not supported.");
    }

    /**
     * We don't support OPTIONS requests.
     * @param req the web service request
     * @param resp the web service response
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doOptions(req, resp);
        writeError(resp, SC_METHOD_NOT_ALLOWED, "Options requests not supported.");
    }

    /**
     * We don't support TRACE requests.
     * @param req the web service request
     * @param resp the web service response
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        super.doTrace(req, resp);
        writeError(resp, SC_METHOD_NOT_ALLOWED, "Trace requests are not supported");
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }
}
