/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.LearningCurveDao;
import edu.cmu.pslc.datashop.dto.LearningCurveOptions;
import edu.cmu.pslc.datashop.dto.LearningCurvePointInfoDetails;
import edu.cmu.pslc.datashop.dto.NameValuePair;
import edu.cmu.pslc.datashop.dto.ProblemHierarchy;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.servlet.AJAXResponseServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;

import static java.util.Collections.singletonList;

/**
 * Servlet for handling LearningCurvePointInfoDetail requests.  Process the
 * request parameters and hand off to the helper to do the dirty work.  Then
 * write the LCPID object to the client.
 *
 * NOTE:  This servlet makes direct use of the LearningCurveDao, and does not
 * have a corresponding helper class.  This is OK, since the results returned from
 * the DAO are simple DTO objects (we don't have to traverse through them to access
 * other objects within the hibernate session).
 *
 * @author kcunning
 * @version $Revision: 12410 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-06-11 14:20:04 -0400 (Thu, 11 Jun 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LCPIDServlet extends AJAXResponseServlet {

    /** Logger! */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** LCPID info request parameter. */
    private static final String LCPID_REQUEST = "lcpid";
    /** Problem Hierarchy information request parameter. */
    private static final String PROBLEM_HIERARCHY_REQUEST = "hierarchy";

    /** Type of information requested parameter. */
    private static final String REQUEST_TYPE_PARAM = "type";
    /** Problem Ids request parameter. */
    private static final String PROBLEM_IDS_PARAM = "problemIds";
    /** Sort by request parameter. */
    private static final String SORT_BY_PARAM = "sortBy";
    /** Sort by direction request parameter. */
    private static final String SORT_DIRECTION_PARAM = "direction";
    /** The selected measure request parameter. */
    private static final String SELECTED_MEASURE_PARAM = "selectedMeasure";

    /** Default sort condition. */
    private static final String DEFAULT_SORT = "curveTypeValue";
    /** Default sort direction condition. */
    private static final String DEFAULT_SORT_DIRECTION = "DESC";
    /** Default selected measure. */
    private static final String DEFAULT_MEASURE = "blah";
    /** Empty string. */
    private static final String EMPTY_STRING = "";

    /** Redirect URL if a point has not been selected. */
    private static final String REDIRECT_URL = "PointInfo";

    /** mostly methods reflecting the state of user selections */
    private final NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();

    /**
     * Handles the AJAX request.  This method overrides doAjax() in AJAXResponseServlet.
     * Determine what type of request was received and dispatch to the appropriate handler method.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @throws Exception a servlet exception
     * @see javax.servlet.http.HttpServlet
     */
    @Override
    protected void doAJAX(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        String requestType = request.getParameter(REQUEST_TYPE_PARAM);
        requestType = requestType == null ? LCPID_REQUEST : requestType;

        if (requestType.equals(LCPID_REQUEST)) {
            processLCPIDRequest(request, response);
        } else if (requestType.equals(PROBLEM_HIERARCHY_REQUEST)) {
            processProblemHierarchyRequest(request, response);
        } else {
            // select only/remove these skills or students from navigation request.
            processNavigationRequest(request, response);
        }
    } // end doAJAX()

    /**
     * Process the LCPID Request.  Return a LearningCurvePointInfoDetails object for
     * the provided parameters.  Accesses the LearningCurveDao to accomplish the task.
     * @param request the http servlet request.
     * @param response the http servlet response.
     * @throws IOException response.sendRedirect can result in an IOException.
     * @throws JSONException writeJSON can result in a JSONException.
     */
    private void processLCPIDRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, JSONException {
        DatasetContext datasetContext = getDatasetContext(request);
        LearningCurveContext lcContext = datasetContext.getLearningCurveContext();
        boolean hasPoint = lcContext.isPointSelected();
        if (!hasPoint) {
            logger.warn("No LC point selected, but reached the LCPID servlet anyway. "
                    + "Forwarding to PointInfo.");
            response.sendRedirect(REDIRECT_URL);
            return;
        }

        int oppNum = 0, seriesIndex = 0, sampleIndex = 0;

        seriesIndex = lcContext.getSeriesIndex();
        // oppNumIndex is stored as opportunity number in the context (it is an index into
        // the list of points) so to get the opportunity number we increment by 1.
        oppNum = lcContext.getOpportunityNumber() + 1;

        // for error rate chart LFA series is shown for each sample,
        // so only even indexes indicate a sample series
        if (lcContext.isPredictedVisible()) {
            sampleIndex = seriesIndex / 2;
        } else {
            sampleIndex = seriesIndex;
        }

        // First check the context to see if sort and measure are already set
        // then check the incoming parameters and set to defaults if necessary
        String sortBy = request.getParameter(SORT_BY_PARAM);
        String sortDirection = request.getParameter(SORT_DIRECTION_PARAM);
        String selectedMeasure = request.getParameter(SELECTED_MEASURE_PARAM);

        if (sortBy.equals(EMPTY_STRING)) {
            sortBy = lcContext.getSortBy();
            if (sortBy == null) {
                sortBy = DEFAULT_SORT;
            }
        }

        if (sortDirection.equals(EMPTY_STRING)) {
            sortDirection = lcContext.getSortDirection();
            if (sortDirection == null) {
                sortDirection = DEFAULT_SORT_DIRECTION;
            }
        }

        if (selectedMeasure.equals(EMPTY_STRING)) {
            selectedMeasure = lcContext.getSelectedMeasure();
            if (selectedMeasure == null) {
                selectedMeasure = DEFAULT_MEASURE;
            }
        }

        logDebug("sortBy::", sortBy, ", direction::", sortDirection, ", selectedMeasure::",
                selectedMeasure);

        SampleItem sample = getSelectedSample(sampleIndex, datasetContext);
        LearningCurveOptions reportOptions = new LearningCurveOptions();
        reportOptions = new LearningCurveOptions();

        reportOptions.setIsViewBySkill(lcContext.isViewBySkill());
        reportOptions.setSkillList(getSkillList(reportOptions, datasetContext));
        reportOptions.setStudentList(getStudentList(reportOptions, datasetContext));
        reportOptions.setPrimaryModel(primaryModel(datasetContext));
        reportOptions.setSecondaryModel(secondaryModel(datasetContext));
        reportOptions.setSelectedMeasure(lcContext.getGraphType());
        reportOptions.setSampleItem(sample);
        reportOptions.setStdDeviationCutOff(lcContext.getStdDeviationCutoff());
        reportOptions.setOpportunityCutOffMin(lcContext.getMinOpportunityNumber());
        reportOptions.setOpportunityCutOffMax(lcContext.getMaxOpportunityNumber());

        // update the LCContext
        lcContext.setSelectedMeasure(selectedMeasure);
        lcContext.setSortBy(sortBy);
        lcContext.setSortDirection(sortDirection);

        LearningCurvePointInfoDetails lcpid;
        LearningCurveDao dao = DaoFactory.DEFAULT.getLearningCurveDao();
        lcpid = dao.getLCPointInfoDetails(oppNum, reportOptions, selectedMeasure,
                sortBy, sortDirection);
        writeJSON(response, lcpid);
    } // end processLCPIDRequest()

    /**
     * Process the problem hierarchy request.  A problem hierarchy request involves getting a set
     * of tooltip information from the navigation helper for the provided set of problem ids.
     * @param request the http servlet request.
     * @param response the http servlet response.
     * @throws IOException writeJSON can throw an IOException.
     * @throws JSONException writeJSON can throw a JSONException.
     */
    private void processProblemHierarchyRequest(HttpServletRequest request,
            HttpServletResponse response) throws IOException, JSONException {
        String[] problemIds = request.getParameterValues(PROBLEM_IDS_PARAM);
        Map<String, String> hierarchies = new HashMap();
        for (String problemId : problemIds) {
            Long id = new Long(problemId);
            hierarchies.put(problemId, navHelper.getProblemInfo(id, request));
        }
        ProblemHierarchy hierarchy = new ProblemHierarchy(hierarchies);
        writeJSON(response, hierarchy);
    } // end processProblemHierarchyRequest()

    /**
     * Process the navigation update request.  A navigation update could include
     * the user choosing to select only or deselect all skills or students in the lcpi
     * details area.
     * @param request the http servlet request.
     * @param response the http servlet response.
     * @throws IOException writeString can cause an IOException.
     */
    private void processNavigationRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String result = navHelper.updateNav(request, getDatasetContext(request));
        writeString(response, result);
    } // end processNavigationRequest()

    /**
     * Helper method to get the selected sample id.  Makes use of the selectedSamples list
     * from the NavigationHelper.
     * @param sampleIndex the location of the sample we want (within the selectedSamples list)
     * @param datasetContext the dataset context.
     * @return a sample item with the id we require.
     */
    private SampleItem getSelectedSample(int sampleIndex, DatasetContext datasetContext) {
        return (SampleItem)navHelper.getSelectedSamples(datasetContext).get(sampleIndex);
    }

    /**
     * Helper method to get the primary skill model selected.
     * @param datasetContext the dataset context.
     * @return primary skill model selected for generating the learning curve report
     */
    private SkillModelItem primaryModel(DatasetContext datasetContext) {
        return DaoFactory.DEFAULT.getSkillModelDao()
                .get(navHelper.getSelectedSkillModel(datasetContext));
    }

    /**
     * Helper method to get the secondary skill model selected.
     * @param datasetContext the dataset context.
     * @return secondary skill model selected for generating the learning curve report
     */
    private SkillModelItem secondaryModel(DatasetContext datasetContext) {
        return DaoFactory.DEFAULT.getSkillModelDao()
            .get(navHelper.getSecondarySelectedSkillModel(datasetContext));
    }

    /**
     * Helper method to get the the selected students.
     * @param reportOptions the learning curve report options.
     * @param info the dataset context.
     * @return the selected students
     */
    private List<StudentItem> getStudentList(LearningCurveOptions reportOptions,
            DatasetContext info) {
        StudentItem topStudent = !reportOptions.isViewBySkill()
            ? navHelper.getTopStudent(info) : null;
            // return a list containing just the topStudent, if there is one, or all the selected
            // students, otherwise
        return topStudent == null ? navHelper.getSelectedStudents(info) : singletonList(topStudent);
    }

    /**
     * Helper method to get the selected skills.
     * @param reportOptions the learning curve report options.
     * @param info the datasetContext.
     * @return the selected skills
     */
    private List<SkillItem> getSkillList(LearningCurveOptions reportOptions, DatasetContext info) {
        SkillItem topSkill = reportOptions.isViewBySkill()
            ? navHelper.getTopSkill(info) : null;
        // return a list containing just the topSkill, if there is one, or all the selected
        // skills , otherwise
        return topSkill == null ? navHelper.getSelectedSkills(info) : singletonList(topSkill);
    }
} // end LCPIDServlet.java
