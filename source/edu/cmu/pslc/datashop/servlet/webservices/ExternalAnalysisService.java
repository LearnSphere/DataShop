/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Web service for fetching the content of an external analysis.
 *
 * @author Hui Cheng
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalAnalysisService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public ExternalAnalysisService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /** Return the external analysis for the specified dataset and analysis as XML. */
    public void get(WebServiceUserLog wsUserLog) {
        try {
                logDebug("datasetId: ", datasetParam(),
                        ", external analysis Id: ", externalAnalysisParam());
                ExternalAnalysisItem eaItem = helper()
                        .externalAnalysisItemForId(
                                getAuthenticatedUser(), datasetParam(), externalAnalysisParam());
                String fileType = eaItem.getFile().getFileType();
                if (fileType == null || !fileType.startsWith("text/")) {
                        byte[] fileContent = helper().externalAnalysisBinaryFileContent(eaItem);
                        OutputStream respOut = getResp().getOutputStream();
                        respOut.write(fileContent);
                        respOut.flush();
                        respOut.close();
                } else {
                        PrintWriter respWriter = getResp().getWriter();
                        String fileContent = helper().externalAnalysisTextFileContent(eaItem);
                        respWriter.println(fileContent);
                        respWriter.close();
                }
        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode() + ": '"
                                + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong with opening external analysis file.", e);
            writeInternalError();
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
