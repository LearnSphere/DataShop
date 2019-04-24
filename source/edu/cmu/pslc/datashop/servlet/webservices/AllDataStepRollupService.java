/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Web service for fetching student-step rollup for a dataset's All Data sample.
 * @author jimbokun
 * @version $Revision: 5897 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2009-11-25 18:23:09 -0500 (Wed, 25 Nov 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AllDataStepRollupService extends StepRollupService {
    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public AllDataStepRollupService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Always return the sample id of the All Data sample.
     * @return the sample id of the All Data sample
     * @throws WebServiceException for invalid, inaccessible dataset id,
     * or no All Data sample found.
     */
    protected int sampleParam() throws WebServiceException { return allDataSampleId(); }
}
