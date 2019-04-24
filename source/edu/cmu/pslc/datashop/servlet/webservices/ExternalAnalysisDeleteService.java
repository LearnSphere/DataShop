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
 * Web service for deleting an external analysis.
 *
 * @author Hui Cheng
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalAnalysisDeleteService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public ExternalAnalysisDeleteService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /** Return the external analysis for the specified dataset and analysis as XML. */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            logDebug("datasetId: ", datasetParam(), ", external analysis Id: ",
                    externalAnalysisParam());
            helper().externalAnalysisDeleteForId(getAuthenticatedUser(),
                    datasetParam(), externalAnalysisParam());
            writeSuccess("External analysis " + externalAnalysisParam()
                    + " successfully deleted.");
        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode()
                    + ": '" + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong with the XML message.", e);
            writeInternalError();
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
