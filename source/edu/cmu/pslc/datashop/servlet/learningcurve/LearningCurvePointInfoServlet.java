/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.learningcurve;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.cmu.pslc.datashop.dto.LearningCurvePointInfo;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.servlet.AJAXResponseServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;

/**
 * Handle AJAX requests for details about a point on the Learning Curve.
 * @author jimbokun
 * @version $Revision: 5640 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2009-08-05 17:06:41 -0400 (Wed, 05 Aug 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurvePointInfoServlet extends AJAXResponseServlet {
    /** point info label */
    private static final String POINT_INFO = "pointInfo";
    /** opportunity number label */
    private static final String OPP_NO = "oppNo";
    /** sample id label */
    private static final String SAMPLE_ID = "sampleId";
    /** session key for LearningCurvePointContext. */
    public static final String LC_POINT_INFO = "lcPointInfoContext";

    /** Constant. */
    private static final int NUM_SERIES_SEC_MODEL_SELECTED = 3;

    /** {@inheritDoc} */
    @Override
    protected void doAJAX(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        HttpSession sess = req.getSession();
        LearningCurvePointContext pointContext = (LearningCurvePointContext)
            sess.getAttribute(LC_POINT_INFO);
        DatasetContext dsContext = getDatasetContext(req);
        LearningCurveContext context = dsContext.getLearningCurveContext();
        LearningCurvePointInfo pointInfo = null;

        // point was deselected
        if (req.getParameter("deselect") != null) {
            context.deselectPoint();
            writeStatusMessage(resp, "point deselected");
            return;
        }

        LearningCurveJspAssistant helper = new LearningCurveJspAssistant(req, null);
        int seriesIndex = 0, sampleIndex = 0, oppNo = 0;
        Integer prevOppNo = context.getOpportunityNumber();
        // flag for whether or not we can look up the point
        boolean hasPoint = true;
        SampleItem sample = null;

        // check for a previously selected point
        if (req.getParameter("selected") != null) {
            hasPoint = context.isPointSelected();
            if (hasPoint) {
                seriesIndex = context.getSeriesIndex();
                oppNo = prevOppNo;
                logDebug("found selected point seriesIndex: ", seriesIndex, " oppNo: ", oppNo);
            }
        // or get the selection parameters from the request
        } else {
            if ("-".equals(req.getParameter(SAMPLE_ID))) {
                sample = helper.firstDisplayedSample();
            } else {
                seriesIndex = getIntegerParam(req, SAMPLE_ID);
            }
            oppNo = getIntegerParam(req, OPP_NO);
            context.setPointInfoSelection(seriesIndex, oppNo);
        }

        // for error rate chart LFA series is shown for each sample,
        // so only even indexes indicate a sample series
        if (hasPoint && context.isPredictedVisible()) {
            NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            boolean secondaryModelIsSelected =
                navHelper.getSecondarySelectedSkillModel(dsContext) != null;
            // if a secondary KC model is also selected, there are 3 series for each sample
            int seriesPerSample = secondaryModelIsSelected ? NUM_SERIES_SEC_MODEL_SELECTED : 2;
            hasPoint = seriesIndex % seriesPerSample == 0;
            sampleIndex = seriesIndex / seriesPerSample;
        } else {
            sampleIndex = seriesIndex;
        }
        // We are relying on the fact that the sampleList here is in the same order
        // as the sample list used to generate the chart.
        if (hasPoint && helper.sampleList().size() > sampleIndex) {
            if (sample == null) {
                sample = helper.sampleList().get(sampleIndex);
            }

            if (sample != null) {
                int maxOpp = pointContext.maxOppForSample(sample);
                if (oppNo > maxOpp) { oppNo = maxOpp; }
                pointInfo = pointContext.getPointInfo(sample, oppNo);
                // if no observation for the opportunity, check the next or previous one
                while (Float.isNaN(pointInfo.getValue())
                        && prevOppNo != null && prevOppNo != oppNo
                        && oppNo < maxOpp && oppNo >= 0) {
                    // "bounce" off 0 if no observation at first opportunity
                    if (oppNo == 0) {
                        prevOppNo = 0;
                        oppNo = 1;
                    } else {
                        // if the previous opportunity is less than this one, the user clicked
                        // the right arrow, otherwise the left arrow
                        oppNo = prevOppNo < oppNo ? oppNo + 1 : oppNo - 1;
                    }
                    pointInfo = pointContext.getPointInfo(sample, oppNo);
                }
                context.setPointInfoSelection(seriesIndex, oppNo);
                // don't display predicted if not visible
                if (!context.isPredictedVisible()) {
                    pointInfo.setPredicted(Float.NaN);
                }
            }
        }
        // write the response
        if (pointInfo == null) {
            writeStatusMessage(resp, "no point found");
        } else {
            writeJSON(resp, json(SAMPLE_ID, seriesIndex, OPP_NO, oppNo,
                    POINT_INFO, pointInfo));
        }
    }
}
