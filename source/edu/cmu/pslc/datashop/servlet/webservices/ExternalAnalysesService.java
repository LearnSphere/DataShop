/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.ExternalAnalysisDTO;
import edu.cmu.pslc.datashop.util.LogUtils;

import static java.lang.String.format;

/**
 * Web service for fetching multiple external analyses.
 *
 * @author Hui Cheng
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalAnalysesService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public ExternalAnalysesService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /** Return the external analyses for the specified dataset as XML. */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            logDebug("datasetId: ", datasetParam());
            List<ExternalAnalysisDTO> dtos =
                helper().externalAnalysisDTOs(datasetParam(), getAuthenticatedUser());

            writeDTOXML(dtos, format("Success. %d external analyses found.", dtos.size()));
        } catch (WebServiceException wse) {
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
