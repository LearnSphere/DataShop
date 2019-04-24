/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Hello World is our sanity check, to confirm that web services are up and running.
 *
 * @author Jim Rankin
 * @version $Revision: 10035 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-09-24 17:09:40 -0400 (Tue, 24 Sep 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HelloWorldService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public HelloWorldService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return "Hello World!" to the requestor.
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            writeSuccess("Hello World!");
        } catch (Exception exception) {
            logger.error("Unable to offer salutation.", exception);
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
