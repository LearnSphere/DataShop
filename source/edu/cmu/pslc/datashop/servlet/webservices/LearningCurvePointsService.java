/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.LearningCurvePoint;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Web service to get learning curve points for specific skill and skill model.
 *
 * @author Cindy Tipper
 * @version $Revision: 13697 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-11-17 10:49:12 -0500 (Thu, 17 Nov 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurvePointsService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(DATASET_ID, KC_MODEL, SKILL);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public LearningCurvePointsService(HttpServletRequest req, HttpServletResponse resp,
                                      Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return the learning curve points
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS);
            String modelName = stringParam(KC_MODEL);
            String skillName = stringParam(SKILL);
            logDebug("datasetId: ", datasetParam());
            logDebug("kcModel: ", modelName);
            logDebug("skill: ", skillName);

            List<LearningCurvePoint> graphPoints =
                helper().getLearningCurvePoints(getAuthenticatedUser(), datasetParam(),
                                                stringParam(KC_MODEL), stringParam(SKILL));
            writeDTOXML(graphPoints);

        } catch (WebServiceException wse) {
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong with the XML message.", e);
            writeInternalError();
        }
    }
}
