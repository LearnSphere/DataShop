/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;

/**
 * This servlet serves the Learning Curve page. It uses the CurriculumHelper
 * to retrieve the data.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14234 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-08-03 12:09:39 -0400 (Thu, 03 Aug 2017) $
 * @see edu.cmu.pslc.datashop.servlet#CurriculumHelper
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveServlet extends AbstractServlet {
    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET_NAME = "LearningCurve";
    /** The JSP name for this servlet. */
    public static final String LC_BASE_JSP_NAME = "/learning_curve.jsp";
    /** "view_by_kc" type value for form parameter. */
    public static final String VIEW_BY_KC = "view_by_kc";
    /** "view_by_student" type value for form parameter. */
    public static final String VIEW_BY_STUDENT = "view_by_student";

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
        // [12/2/2 - ysahn] Added for performance metrics
        long startTm = System.currentTimeMillis();
        req.setAttribute("startTm", startTm);

        logDebug("doPost begin :: ", getDebugParamsString(req));
        try {
            setEncoding(req, resp);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // set the most recent servlet name for the 'S2D' page
            req.getSession(true).setAttribute("recent_ds_page", SERVLET_NAME);

            DatasetContext datasetContext = getDatasetContext(req);

            UserItem userItem = getLoggedInUserItem(req);
            updateAccessFlag(datasetContext, userItem, "LearningCurve");

            LearningCurveContext lcContext = datasetContext.getLearningCurveContext();

            String ajaxUpdate = checkAJAXUpdate(req); // [ysahn] This is slow

            if (ajaxUpdate == null) {
                ajaxUpdate = processLearningCurveOptions(req, lcContext);
            }
            setInfo(req, datasetContext);
            if (ajaxUpdate != null) {
                logDebug("writeAJAXUpdate");
                writeAJAXUpdate(resp, ajaxUpdate);
            } else {
                logDebug("accessing base JSP");
                redirect(req, resp, LC_BASE_JSP_NAME);
            }
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    } // end doPost

    /**
     * Helper function for parsing the majority of the Learning Curve options.
     * @param req the HttpServletRequset object.
     * @param lcContext the LearningCurveContext
     * @return Possible AJAX update string (null if no update)
     */
    private String processLearningCurveOptions(HttpServletRequest req,
            LearningCurveContext lcContext) {
        String ajaxUpdate = null;

        // AJAX calls. Make sure 'ajaxUpdate' is not null if you add another one.
        String contentType = req.getParameter("content_type");
        if (contentType != null) {
            if (contentType.equals("get")) {
                logDebug("LearningCurveServlet.doPost get contentType: ", contentType);
            } else {
                lcContext.setContentType(contentType);
            }
            // call getContentType to protect against invalid content_type values
            contentType = lcContext.getContentType();
            ajaxUpdate = contentType;
        }

        String viewPredictedParam = req.getParameter("view_predicted");
        if (viewPredictedParam != null) {
            Boolean viewPredicted = new Boolean(viewPredictedParam);
            lcContext.setDisplayPredicted(viewPredicted);
            logDebug("LearningCurveServlet.doPost view_predicted: ", viewPredicted);
            ajaxUpdate = "type selected AOK.";
        }

        String viewErrorBarsParam = req.getParameter("view_error_bars");
        if (viewErrorBarsParam != null) {
            Boolean viewErrorBars = new Boolean(viewErrorBarsParam);
            lcContext.setDisplayErrorBars(viewErrorBars);
            logDebug("LearningCurveServlet.doPost view_error_bars: ", viewErrorBars);
            ajaxUpdate = "true";
        }

        String errorBarTypeParam = req.getParameter("error_bar_type");
        if (errorBarTypeParam != null) {
            lcContext.setErrorBarType(errorBarTypeParam);
            logDebug("LearningCurveServlet.doPost error_bar_type: ", errorBarTypeParam);
            ajaxUpdate = "true";
        }

        String includeHighStakesParam = req.getParameter("include_high_stakes");
        if (includeHighStakesParam != null) {
            Boolean includeHighStakes = new Boolean(includeHighStakesParam);
            lcContext.setIncludeHighStakes(includeHighStakes);
            logDebug("LearningCurveServlet.doPost include_high_stakes: ", includeHighStakes);
            ajaxUpdate = "true";
        }

        // Non-Ajax calls.
        String learningCurveView = req.getParameter("learning_curve_view");
        if (learningCurveView != null) {
            lcContext.setIsViewBySkill(learningCurveView.equals(VIEW_BY_KC));
            logDebug("LearningCurveServlet.doPost selLearningCurveView: ", learningCurveView,
                    ", isViewBySkill is ", lcContext.isViewBySkill());
        }

        String category = LearningCurveImage.NOT_CLASSIFIED;
        if (req.getParameter("category") != null) {
            String categoryStr = req.getParameter("category");
            if (categoryStr.equals("too_little_data")) {
                category = LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA;
            } else if (categoryStr.equals("low_and_flat")) {
                category = LearningCurveImage.CLASSIFIED_LOW_AND_FLAT;
            } else if (categoryStr.equals("still_high")) {
                category = LearningCurveImage.CLASSIFIED_STILL_HIGH;
            } else if (categoryStr.equals("no_learning")) {
                category = LearningCurveImage.CLASSIFIED_NO_LEARNING;
            } else if (categoryStr.equals("other")) {
                category = LearningCurveImage.CLASSIFIED_OTHER;
            }
        }

        String range = req.getParameter("range");
        if (range != null) {
            lcContext.setLearningCurveRange(category, new Integer(range));
            logDebug("LearningCurveServlet.doPost selLearningCurveRange: ",
                     range, "(", category, ")");
        }

        String numberThumbsParam = req.getParameter("numberThumbs");
        if (numberThumbsParam != null) {
            Integer numThumbs = new Integer(numberThumbsParam);
            lcContext.setNumberOfThumbs(numThumbs);
            logDebug("LearningCurveServlet.doPost setNumberThumbs: ", numThumbs);
        }

        String maxOppsParam = req.getParameter("maxOpportunities");
        logDebug("LearningCurveServlet.doPost maxOpportunities: ", maxOppsParam);
        if (maxOppsParam != null) {
            lcContext.setMaxOpportunityNumber(intForParam(maxOppsParam));
        }

        String minOppsParam = req.getParameter("minOpportunities");
        logDebug("LearningCurveServlet.doPost minOppsString: ", minOppsParam);
        if (minOppsParam != null) {
            lcContext.setMinOpportunityNumber(intForParam(minOppsParam));
        }

        String stdDevCutoffParam = req.getParameter("stdDevCutoff");
        logDebug("LearningCurveServlet.doPost stdDevCutoff: ", stdDevCutoffParam);
        if (stdDevCutoffParam != null) {
            lcContext.setStdDeviationCutoff(stdDevCutoffParam.equals("") ? null
                    : new Double(stdDevCutoffParam));
        }

        String classifyParam = req.getParameter("classify_lcs");
        if (classifyParam != null) {
            lcContext.setClassifyThumbnails(new Boolean(classifyParam));
        }

        String studentThresholdParam = req.getParameter("student_threshold");
        if (studentThresholdParam != null) {
            lcContext.setStudentThreshold(new Integer(studentThresholdParam));
            logDebug("LearningCurveServlet.doPost student_threshold: ", studentThresholdParam);
        }

        String opportunityThresholdParam = req.getParameter("opportunity_threshold");
        if (opportunityThresholdParam != null) {
            lcContext.setOpportunityThreshold(new Integer(opportunityThresholdParam));
            logDebug("LearningCurveServlet.doPost opportunity_threshold: ",
                     opportunityThresholdParam);
        }

        String afmSlopeThresholdParam = req.getParameter("afm_slope_threshold");
        if (afmSlopeThresholdParam != null) {
            lcContext.setAfmSlopeThreshold(new Double(afmSlopeThresholdParam));
            logDebug("LearningCurveServlet.doPost afm_slope_threshold: ", afmSlopeThresholdParam);
        }

        String lowErrorThresholdParam = req.getParameter("low_error_threshold");
        if (lowErrorThresholdParam != null) {
            lcContext.setLowErrorThreshold(new Double(lowErrorThresholdParam));
            logDebug("LearningCurveServlet.doPost low_error_threshold: ", lowErrorThresholdParam);
        }

        String highErrorThresholdParam = req.getParameter("high_error_threshold");
        if (highErrorThresholdParam != null) {
            lcContext.setHighErrorThreshold(new Double(highErrorThresholdParam));
            logDebug("LearningCurveServlet.doPost high_error_threshold: ", highErrorThresholdParam);
        }

        // This is being set via the "Model Values" page.
        String learningRateThresholdParam = getParameter(req, "learning_rate_threshold");
        if (learningRateThresholdParam != null) {
            // Guard against non-numeric values.
            Double threshold = null;
            try {
                threshold = new Double(learningRateThresholdParam);
            } catch (Exception e) {
                logDebug("Ignoring non-numeric value specified for threshold: "
                         + learningRateThresholdParam);
            }
            if (threshold != null) {
                // Same attribute, different name
                lcContext.setAfmSlopeThreshold(threshold);
            }

            String sortAndTagParam = req.getParameter("sort_kcs");
            // Input is a checkbox... if present, value is true, otherwise is false.
            if (sortAndTagParam != null) {
                lcContext.setSortAndTag(true);
            } else {
                lcContext.setSortAndTag(false);
            }
        }

        return ajaxUpdate;
    }

    /**
     * Convert a parameter string into an integer.
     * @param param the parameter string
     * @return the integer expressed by the parameter
     */
    private Integer intForParam(String param) {
        Integer val = null;
        if (!param.equals("") && !param.equals("0")) {
            val = new Integer(param);
            if (val < 1) { val = null; }
        }
        return val;
    }

    /**
     * Check the servlet request to determine if user has selected
     * the Student-Step Rollup subtab.
     * @param req the HttpServletRequest
     * @return flag indicating true or false
     */
    private boolean isStudentStepRollupSubtabRequest(HttpServletRequest req) {
        String contentType = req.getParameter("content_type");
        if (contentType != null) {
            if (contentType.equals("byStudentStep")) {
                return true;
            }
        }
        return false;
    }

} // end class LearningCurveServlet
