/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.learningcurve;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cmu.pslc.datashop.servlet.AJAXResponseServlet;

/**
 * Handle AJAX requests for saving and restoring hide/show settings.
 * for learning curve DIVs.
 * @author jimbokun
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HideShowServlet extends AJAXResponseServlet {

    /** Link Id request parameter. */
    private static final String LINK_ID_PARAM = "linkId";
    /** Hide/Show request parameter. */
    private static final String HIDE_SHOW_PARAM = "hideShow";
    /** Link Id request parameter. */
    private static final String SELECTED_MEASURE_PARAM = "selectedMeasure";

    /** {@inheritDoc} */
    @Override
    protected void doAJAX(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        LearningCurveContext context = getDatasetContext(req).getLearningCurveContext();
        String linkId = req.getParameter(LINK_ID_PARAM),
            hideShow = req.getParameter(HIDE_SHOW_PARAM),
            selectedMeasure = req.getParameter(SELECTED_MEASURE_PARAM);

        if (linkId != null && hideShow != null) {
            context.setHideShow(linkId, hideShow);
            context.setSelectedMeasure(selectedMeasure);
        }
        writeJSON(resp, jsonForMap(context.hideShow()));
    }
}
