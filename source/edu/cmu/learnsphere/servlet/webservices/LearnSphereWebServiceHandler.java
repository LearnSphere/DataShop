/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.servlet.webservices.WebServicesServlet.HttpMethod;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.servlet.webservices.WebService;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceHandler;

import static java.util.Arrays.asList;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static edu.cmu.pslc.datashop.util.CollectionUtils.iter;

/**
 * Routes a web service request matching a pattern to the corresponding web service.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereWebServiceHandler extends WebServiceHandler{
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * For example, if the path is like  /workflows/[workflowId]
     * then "/workflows/(.*)" is the pattern and [ "workflowId" ]
     * are the parameters.  Requests might then be dispatched to a newly created WorkflowService
     * instance.
     * @param pattern path pattern identifying requests for this service
     * @param serviceClass the web service class for handling matching requests
     * @param params keys identifying parameters in the path
     */
    public LearnSphereWebServiceHandler(String pattern, Class< ? extends WebService> serviceClass,
            String... params) {
            super(pattern, serviceClass, params);
    }

    /**
     * If the request path matches the pattern, dispatch the request to a newly created
     * web service instance, and return true.  Otherwise return false.
     * @param wsUserLog Web Service User Log
     * @param servicePath path identifying the incoming web service request
     * @param method HTTP method
     * @param req the web service request
     * @param resp the web service response
     * @return LearnSphereWebServiceUserLog containing handled field, true if we handled the request, false otherwise
     */
    public LearnSphereWebServiceUserLog handle(LearnSphereWebServiceUserLog wsUserLog, String servicePath,
            HttpMethod method, final HttpServletRequest req, HttpServletResponse resp) {
        final Matcher m = pathPattern.matcher(servicePath);

        if (m.matches()) {
            logDebug("path ", servicePath, " matches ", pathPattern);
            if (pathParams.size() != m.groupCount()) {
                throw new IllegalStateException("Wrong number of path parameters! Expected "
                        + pathParams.size() + " but found " + m.groupCount());
            }

            // build up the parameters from the path parameters and the request parameters
            Map<String, Object> params = new HashMap<String, Object>() { {
                for (int i = 0; i < pathParams.size(); i++) {
                    put(pathParams.get(i), m.group(i + 1));
                }

                Iterable<String> names = iter(req.getParameterNames());

                for (String name : names) {
                    put(name, req.getParameter(name));
                }
            } };
            
            
            // instantiate the web service and call the method corresponding to the HTTP method
            try {
                    LearnSphereWebService service = (LearnSphereWebService)getServiceConstructor().newInstance(req, resp, params);
                    logDebug("calling ", method, " on ", service);
                        switch (method) {
                        case GET:
                            ((LearnSphereWebService)service).get(wsUserLog);
                            break;
                        case PUT:
                            ((LearnSphereWebService)service).put();
                            break;
                        case POST:
                            ((LearnSphereWebService)service).post(wsUserLog);
                            break;
                        case DELETE:
                            ((LearnSphereWebService)service).delete(); 
                            break;
                        default:
                            resp.setStatus(SC_METHOD_NOT_ALLOWED);
                    } 
                        wsUserLog.setHandled(true);
                        return wsUserLog;
            } catch (Exception e) {
                logger.error("Looks like we couldn't create a new LearnSphere web service instance.", e);
            }
        }
        wsUserLog.setHandled(false);
        return wsUserLog;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
