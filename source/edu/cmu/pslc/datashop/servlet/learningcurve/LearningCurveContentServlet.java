/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2009
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;

/**
 * Update LearningCurveContext values and redirect to the learning curve content JSP
 * (learning_curve_by.jsp).
 * @author Jim Rankin
 * @version $Revision: 14234 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-08-03 12:09:39 -0400 (Thu, 03 Aug 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveContentServlet extends AbstractServlet {
    /** Common action "view learning curve by KC". */
    public static final String VIEW_LC_BY_KC = "View Learning Curve by KC";
    /** Common action "view learning curve by student". */
    public static final String VIEW_LC_BY_STUDENT = "View Learning Curve by Student";
    /** The JSP name for just the content of the learning curve. */
    private static final String VIEW_BY_JSP = "/learning_curve_by.jsp";

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     * @see javax.servlet.http.HttpServlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        //no difference, so just forward the request and response to the post.
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     * @see javax.servlet.http.HttpServlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        try {
            setEncoding(req, resp);

            DatasetContext dsContext = getDatasetContext(req);
            LearningCurveContext lcContext = dsContext.getLearningCurveContext();
            String learningCurveType = req.getParameter("learning_curve_type");
            String includeHighStakes = req.getParameter("include_high_stakes");

            if (learningCurveType != null) {
                lcContext.setGraphType(learningCurveType);
                logDebug("LearningCurveServlet.doPost selLearningCurveType: ", learningCurveType);
            }

            if (includeHighStakes != null) {
                lcContext.setIncludeHighStakes(new Boolean(includeHighStakes));
                logDebug("LearningCurveContentServlet.doPost setIncludeHighStakes: ", includeHighStakes);
            }

            String kcOrStudent = lcContext.isViewBySkill() ? "KC" : "Student";

            UserLogger.log(dsContext.getDataset(), dsContext.getUser(),
                    "View Learning Curve by " + kcOrStudent);
            logInfo(getBenchmarkPrefix(dsContext), " Forwarding to View by ", kcOrStudent,
                    " JSP");
            redirect(req, resp, VIEW_BY_JSP);
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    } // end doPost
}
