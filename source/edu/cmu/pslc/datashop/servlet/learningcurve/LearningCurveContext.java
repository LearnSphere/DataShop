/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.learningcurve;

import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_BAR_TYPE_SE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_RATE_TYPE;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.AlphaScoreItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Context object to store in the session the current settings for the LearningCurve
 * for a given dataset.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14234 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-08-03 12:09:39 -0400 (Thu, 03 Aug 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveContext implements Serializable {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Servlet Session context stored in the map, this allows us to synchronize
     * the list to handle multiple thread requests.
     */
    private Map<String, Object> map;

    /**
     * Context for the starting index of thumbnails displayed, a map by category.
     */
    private Map<String, Integer> rangeMap;

    /** Map Key. */
    private static final String GRAPH_TYPE = "lc_graph_type";
    /** Map Key for is_view_by_skill. */
    private static final String IS_VIEW_BY_SKILL = "is_view_by_skill";

    /** Map Key. */
    private static final String STD_DEV_KEY = "lc_std_dev_cutoff";
    /** Map Key. */
    private static final String MAX_OPP_KEY = "lc_max_opp_cutoff";
    /** Map Key. */
    private static final String MIN_OPP_KEY = "lc_min_opp_cutoff";

    /** Map Key. */
    private static final String CONTENT_TYPE = "ls_content_type";
    /** Map Key. */
    private static final String DISPLAY_PREDICTED = "lc_view_predicted";
    /** Map Key. */
    private static final String NUMBER_OF_THUMBS = "lc_number_of_thumbs";
    /** Map Key. */
    private static final String DISPLAY_ERROR_BARS = "lc_view_error_bars";
    /** Map Key. */
    private static final String ERROR_BAR_TYPE = "lc_error_bar_type";
    /** Map Key. */
    private static final String INCLUDE_HIGH_STAKES = "lc_include_high_stakes";

    /** Map Key. */
    private static final String CLASSIFY_THUMBNAILS = "lc_classify_thumbs";
    /** Map Key. */
    private static final String STUDENT_THRESHOLD = "lc_student_threshold";
    /** Map Key. */
    private static final String OPPORTUNITY_THRESHOLD = "lc_opportunity_threshold";
    /** Map Key. */
    private static final String AFM_SLOPE_THRESHOLD = "lc_afm_threshold";
    /** Map Key. */
    private static final String LOW_ERROR_THRESHOLD = "lc_low_error_threshold";
    /** Map Key. */
    private static final String HIGH_ERROR_THRESHOLD = "lc_high_error_threshold";

    /** Map Key. */
    private static final String LFA_DISPLAY_MODEL_VALUES_KEY = "lfa_display_model_values";
    /** Map Key. */
    private static final String LFA_DISPLAY_SKILL_VALUES_KEY = "lfa_show_skill_values";
    /** Map Key. */
    private static final String LFA_DISPLAY_STUDENT_VALUES_KEY = "lfa_show_model_values";
    /** Map Key. */
    private static final String STUDENT_INTERCEPT_LIST_KEY = "student_intercept_list_key";
    /** Map Key. */
    private static final String LFA_NUM_PARAMATERS = "lfa_num_parameters";
    /** Map Key. */
    private static final String LFA_TOTAL_NUM_WITH_ALPHA_SCORE = "lfa_total_alpha_score";
    /** key for series index */
    private static final String LCPI_SERIES_INDEX = "series_index";
    /** key for opportunity number */
    private static final String LCPI_OPP_NO = "opportunity_number";
    /** key for selected LCPID measure (student, problem, skill or step). */
    private static final String LCPID_SELECTED_MEASURE = "selected_measure";
    /** key for LCPID sortBy. */
    private static final String LCPID_SORT_BY = "sort_by";
    /** key for LCPID sortByDirection. */
    private static final String LCPID_SORT_DIRECTION = "sort_direction";

    /** key for LFA Values sortAndTag flag. */
    private static final String LFA_SORT_AND_TAG_VALUES = "lfa_sort_and_tag_values";

    /** Default Constructor. */
    public LearningCurveContext() {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());
        // need to default the view to skill because we are no longer storing the actual
        // view by string.
        this.setIsViewBySkill(true);
        this.setSortAndTag(true);
        rangeMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    }

    /**
     * Reveals if this graphs is a view by skill or not.
     * @return true if view by skill, false otherwise.
     */
    public boolean isViewBySkill() {
        return (Boolean)map.get(IS_VIEW_BY_SKILL);
    }

    /**
     * Set "is_view_by_skill".
     * @param flag the value to set.
     */
    public void setIsViewBySkill(boolean flag) {
        map.put(IS_VIEW_BY_SKILL, flag);
    }

    /**
     * Returns the stdDeviationCutoff.
     * @return the stdDeviationCutoff
     */
    public Double getStdDeviationCutoff() {
        return (Double)map.get(STD_DEV_KEY);
    }

    /**
     * Sets the stdDeviationCutoff.
     * @param stdDeviationCutoff the stdDeviationCutoff to set
     */
    public void setStdDeviationCutoff(Double stdDeviationCutoff) {
        map.put(STD_DEV_KEY, stdDeviationCutoff);
    }

    /**
     * Returns the maximum number of opportunities to display.
     * @return max number of opportunities as an Integer.
     */
    public Integer getMaxOpportunityNumber() {
        return (Integer)map.get(MAX_OPP_KEY);
    }

    /**
     * Sets the maximum number of opportunities to display.
     * @param maxOpp Integer of the max number of opportunities.
     */
    public void setMaxOpportunityNumber(Integer maxOpp) {
        map.put(MAX_OPP_KEY, maxOpp);
    }

    /**
     * Returns minOpportunityNumber.
     * @return Returns the minOpportunityNumber.
     */
    public Integer getMinOpportunityNumber() {
        return (Integer)map.get(MIN_OPP_KEY);
    }

    /**
     * Set minOpportunityNumber.
     * @param minOpportunityNumber The minOpportunityNumber to set.
     */
    public void setMinOpportunityNumber(Integer minOpportunityNumber) {
        map.put(MIN_OPP_KEY, minOpportunityNumber);
    }

    /** the learning curve content type */
    private static final String LEARNING_CURVE = "learningCurve";

    /** content type must be one of these values */
    private static final Set<String> VALID_CONTENT_TYPES =
        set(LEARNING_CURVE, "byStudentStep", "lfaValues", "kcModels");

    /**
     * Returns ContentType for the learning curve servlet,
     * learning curve, step rollup, model values (lfaValues), and KC models.
     * @return Returns the context type.
     */
    public String getContentType() {
        String contentType = (String)map.get(CONTENT_TYPE);
        // learningCurve is the default value
        return contentType == null ? LEARNING_CURVE : contentType;
    }

    /**
     * Set ContentType.
     * @param contentType The content type to set.
     */
    public void setContentType(String contentType) {
        if (VALID_CONTENT_TYPES.contains(contentType)) {
            map.put(CONTENT_TYPE, contentType);
        } else {
            logger.warn("Tried to set invalid content type: " + contentType);
        }
    }

    /**
     * Return the value for key if it exists, defaultValue otherwise.
     * @param <T> the type to return
     * @param key the key
     * @param defaultValue the default value
     * @return the value for key if it exists, defaultValue otherwise
     */
    private <T> T getOrDefault(String key, T defaultValue) {
        return map.containsKey(key) ? (T)map.get(key) : defaultValue;
    }

    /**
     * Returns true if no value for key in map, boolean value for key otherwise.
     * @param key the key
     * @return true if no value for key in map, boolean value for key otherwise
     */
    private Boolean getBoolean(String key) {
        return getOrDefault(key, true);
    }

    /** Returns if the predicted values should be displayed. @return Boolean of true to display. */
    public Boolean getDisplayPredicted() {
        return getBoolean(DISPLAY_PREDICTED);
    }

    /** Set whether to display predicted. @param display true to display or false otherwise. */
    public void setDisplayPredicted(Boolean display) {
        updateSeriesIndex(display != getDisplayPredicted());
        map.put(DISPLAY_PREDICTED, display);
    }

    /**
     * Returns the number of thumbs to view.
     * @return the Number of thumbs as an Integer
     */
    public Integer getNumberOfThumbs() {
        return (Integer)map.get(NUMBER_OF_THUMBS);
    }

    /**
     * Set the number of thumbs to view.
     * @param numThumbs The number of thumbs.
     */
    public void setNumberOfThumbs(Integer numThumbs) {
        map.put(NUMBER_OF_THUMBS, numThumbs);
    }

    /**
     * Returns the current range as a string.
     * @param category the category
     * @return the range.
     */
    public Integer getLearningCurveRange(String category) {
        return rangeMap.get(category);
    }

    /**
     * Sets the current "range" where range is the set of thumbs currently being viewed.
     * @param category the category
     * @param range the current range
     */
    public void setLearningCurveRange(String category, Integer range) {
        rangeMap.put(category, range);
    }

    /** Returns if the error bars should be displayed. @return Boolean of true to display. */
    public Boolean getDisplayErrorBars() {
        return getOrDefault(DISPLAY_ERROR_BARS, false);
    }

    /** Set whether to display error bars. @param display true to display or false otherwise. */
    public void setDisplayErrorBars(Boolean display) {
        map.put(DISPLAY_ERROR_BARS, display);
    }


    /** Returns the error bar type to be used. @return String value. */
    public String getErrorBarType() {
        return getOrDefault(ERROR_BAR_TYPE, ERROR_BAR_TYPE_SE);
    }

    /** Sets the error bar type to be used. @param value error bar type. */
    public void setErrorBarType(String value) {
        map.put(ERROR_BAR_TYPE, value);
    }

    /**
     *  Returns true iff the highStakes error rate should be included.
     *  @return Boolean of true to include.
     */
    public Boolean getIncludeHighStakes() {
        return getOrDefault(INCLUDE_HIGH_STAKES, true);
    }

    /**
     *  Set whether to include highStakes error rate.
     *  @param includeHs true to include or false otherwise.
     */
    public void setIncludeHighStakes(Boolean includeHS) {
        map.put(INCLUDE_HIGH_STAKES, includeHS);
    }

    /**
     * Sets the current "range" where range is the set of thumbs currently being viewed.
     * @param type the type of graph as a string.
     */
    public void setGraphType(String type) {
        updateSeriesIndex(!getGraphType().equals(type));
        map.put(GRAPH_TYPE, type);
    }

    /**
     * Returns the type of graph currently requested.
     * @return String of the type.
     */
    public String getGraphType() {
        return getOrDefault(GRAPH_TYPE, ERROR_RATE_TYPE);
    }

    /**
     * Get the flag indicating if thumbnails are to be classified.
     * @return flag
     */
    public Boolean getClassifyThumbnails() {
        return getOrDefault(CLASSIFY_THUMBNAILS, true);
    }

    /**
     * Set the flag indicating if thumbnails are to be classified.
     * @param flag the value
     */
    public void setClassifyThumbnails(Boolean flag) {
        map.put(CLASSIFY_THUMBNAILS, flag);
    }

    /**
     * Default value for student threshold.
     */
    private static final Integer STUDENT_THRESHOLD_DEFAULT = 10;

    /**
     * Get the student threshold used for classifying curves.
     * @return student threshold value
     */
    public Integer getStudentThreshold() {
        return getOrDefault(STUDENT_THRESHOLD, STUDENT_THRESHOLD_DEFAULT);
    }

    /**
     * Set the student threshold value used for classifying curves.
     * @param threshold the value
     */
    public void setStudentThreshold(Integer threshold) {
        map.put(STUDENT_THRESHOLD, threshold);
    }

    /**
     * Default value for opportunity threshold.
     */
    private static final Integer OPPORTUNITY_THRESHOLD_DEFAULT = 3;

    /**
     * Get the opportunity threshold used for classifying curves.
     * @return opportunity threshold value
     */
    public Integer getOpportunityThreshold() {
        return getOrDefault(OPPORTUNITY_THRESHOLD, OPPORTUNITY_THRESHOLD_DEFAULT);
    }

    /**
     * Set the opportunity threshold value used for classifying curves.
     * @param threshold the value
     */
    public void setOpportunityThreshold(Integer threshold) {
        map.put(OPPORTUNITY_THRESHOLD, threshold);
    }

    /**
     * Default value for AFM slope threshold.
     */
    private static final Double AFM_SLOPE_THRESHOLD_DEFAULT = 0.001;

    /**
     * Get the AFM slope threshold used for classifying curves.
     * @return AFM slope threshold value
     */
    public Double getAfmSlopeThreshold() {
        return getOrDefault(AFM_SLOPE_THRESHOLD, AFM_SLOPE_THRESHOLD_DEFAULT);
    }

    /**
     * Set the AFM slope threshold value used for classifying curves.
     * @param threshold the value
     */
    public void setAfmSlopeThreshold(Double threshold) {
        map.put(AFM_SLOPE_THRESHOLD, threshold);
    }

    /**
     * Default value for low error threshold.
     */
    private static final Double LOW_ERROR_THRESHOLD_DEFAULT = 20.0;

    /**
     * Get the low error threshold used for classifying curves.
     * @return low error threshold value
     */
    public Double getLowErrorThreshold() {
        return getOrDefault(LOW_ERROR_THRESHOLD, LOW_ERROR_THRESHOLD_DEFAULT);
    }

    /**
     * Set the low error threshold value used for classifying curves.
     * @param threshold the value
     */
    public void setLowErrorThreshold(Double threshold) {
        map.put(LOW_ERROR_THRESHOLD, threshold);
    }

    /**
     * Default value for high error threshold.
     */
    private static final Double HIGH_ERROR_THRESHOLD_DEFAULT = 40.0;

    /**
     * Get the high error threshold used for classifying curves.
     * @return high error threshold value
     */
    public Double getHighErrorThreshold() {
        return getOrDefault(HIGH_ERROR_THRESHOLD, HIGH_ERROR_THRESHOLD_DEFAULT);
    }

    /**
     * Set the high error threshold value used for classifying curves.
     * @param threshold the value
     */
    public void setHighErrorThreshold(Double threshold) {
        map.put(HIGH_ERROR_THRESHOLD, threshold);
    }

    /**
     * sets whether to view the LFA skill model parameters.
     * @param display true to show, false otherwise.
     */
    public void setDisplayLFAModelValues(Boolean display) {
        map.put(LFA_DISPLAY_MODEL_VALUES_KEY, display);
    }

    /**
     * Returns whether to view the LFA skill model parameters.
     * @return true to show, false otherwise.
     */
    public Boolean getDisplayLFAModelValues() {
        return getBoolean(LFA_DISPLAY_MODEL_VALUES_KEY);
    }

    /**
     * sets whether to view the LFA skill parameters.
     * @param display true to show, false otherwise.
     */
    public void setDisplayLFASkillValues(Boolean display) {
        map.put(LFA_DISPLAY_SKILL_VALUES_KEY, display);
    }

    /**
     * Returns whether to view the LFA skill parameters.
     * @return true to show, false otherwise.
     */
    public Boolean getDisplayLFASkillValues() {
        return getBoolean(LFA_DISPLAY_SKILL_VALUES_KEY);
    }

    /**
     * sets whether to view the LFA student parameters.
     * @param display true to show, false otherwise.
     */
    public void setDisplayLFAStudentValues(Boolean display) {
        map.put(LFA_DISPLAY_STUDENT_VALUES_KEY, display);
    }

    /**
     * Returns whether to view the LFA student parameters.
     * @return true to show, false otherwise.
     */
    public Boolean getDisplayLFAStudentValues() {
        return getBoolean(LFA_DISPLAY_STUDENT_VALUES_KEY);
    }

    /**
     * Sets the list of alpha score items which are the student intercepts.
     * @param interceptList list of AlphaScoreItems.
     */
    public void setStudentInterceptList(List <AlphaScoreItem> interceptList) {
        map.put(STUDENT_INTERCEPT_LIST_KEY, interceptList);
    }

    /**
     * Gets the list of student intercepts.
     * @return List of AlphaScoreItems which is the student LFA intercepts.
     */
    public List <AlphaScoreItem> getStudentInterceptList() {
        return (List)map.get(STUDENT_INTERCEPT_LIST_KEY);
    }

    /**
     * Returns the number of parameters in LFA run.
     * @return the number of parameters in LFA run
     */
    public Integer getLfaNumberOfParameters() {
        return (Integer)map.get(LFA_NUM_PARAMATERS);
    }

    /**
     * Set the number of parameters in LFA run.
     * @param num The number of parameters in LFA run
     */
    public void setLfaNumberOfParameters(Integer num) {
        map.put(LFA_NUM_PARAMATERS, num);
    }

    /**
     * Returns the total number of students with alpha score (student intercept).
     * @return the total number of students with alpha score
     */
    public Integer getTotalNumStudentsWithAlphaScore() {
        return (Integer)map.get(LFA_TOTAL_NUM_WITH_ALPHA_SCORE);
    }

    /**
     * Set the number of parameters in LFA run.
     * @param num The number of parameters in LFA run
     */
    public void setTotalNumStudentsWithAlphaScore(Integer num) {
        map.put(LFA_TOTAL_NUM_WITH_ALPHA_SCORE, num);
    }

    /**
     * True if graph type is equal to type false otherwise.
     * @param type the type
     * @return whether the graph type is equal to type
     */
    public boolean graphTypeIs(String type) {
        return getGraphType().equals(type);
    }

    /**
     * True if graph type is Error Rate, false otherwise.
     * @return whether graph type is Error Rate
     */
    public boolean graphTypeIsErrorRate() {
        return graphTypeIs(ERROR_RATE_TYPE);
    }

    /**
     * Predicted curves are visible if the graph type is Error Rate
     * and View Predicted is selected.
     * @return whether predicted curves are visible
     */
    public boolean isPredictedVisible() {
        return graphTypeIsErrorRate() && getDisplayPredicted();
    }

    /**
     * Whether a point has been selected.
     * @return whether a point has been selected
     */
    public boolean isPointSelected() {
        return getOpportunityNumber() != null;
    }

    /**
     * The opportunity number for the selected point.
     * @return the opportunity number for the selected point
     */
    public Integer getOpportunityNumber() {
        return (Integer)map.get(LCPI_OPP_NO);
    }

    /**
     * Set the opportunity number for the selected point.
     * @param oppNo the opportunity number for the selected point
     */
    private void setOpportunityNumber(Integer oppNo) {
        map.put(LCPI_OPP_NO, oppNo);
    }

    /**
     * The series index for the selected point.
     * @return the series index for the selected point
     */
    public Integer getSeriesIndex() {
        return (Integer)map.get(LCPI_SERIES_INDEX);
    }

    /**
     * Set the series index for the selected point.
     * This is the series on the learning curve chart that corresponds to the sample for the
     * selected point.  Because predicted values are sometimes shown, there may or may not
     * be a one to one correspondence between series and samples.  See logic in
     * LearningCurvePointInfoServlet for details on how to map a series index to a sample.
     * @param seriesIndex the series index for the selected point
     */
    private void setSeriesIndex(Integer seriesIndex) {
        map.put(LCPI_SERIES_INDEX, seriesIndex);
    }

    /**
     * Set the series index and opportunity number for the selected point.
     * This is the series on the learning curve chart that corresponds to the sample for the
     * selected point.  Because predicted values are sometimes shown, there may or may not
     * be a one to one correspondence between series and samples.  See logic in
     * LearningCurvePointInfoServlet for details on how to map a series index to a sample.
     * @param seriesIndex the series index for the selected point
     * @param oppNo the opportunity number for the selected point
     */
    public void setPointInfoSelection(int seriesIndex, int oppNo) {
        setSeriesIndex(seriesIndex);
        setOpportunityNumber(oppNo);
    }

    /** Set the series index and opportunity number to null because no point is selected. */
    public void deselectPoint() {
        setSeriesIndex(null);
        setOpportunityNumber(null);
    }

    /**
     * See DS774.
     * If the graph type changes from Error Rate to something else, or the View Predicted setting
     * is un-selected, any previous series index for the selected point will be off by a factor of
     * two.  Update the series index to reflect this.
     * @param predictedVisibleChanged whether the new value (graph type or view predicted)
     * is the same as the previous one
     */
    private void updateSeriesIndex(boolean predictedVisibleChanged) {
        if (isPointSelected() && predictedVisibleChanged) {
            setSeriesIndex(isPredictedVisible() ? getSeriesIndex() / 2 : getSeriesIndex() * 2);
        }
    }

    /**
     * Set the selected LC point info details measure.
     * @param measure the selected measure.
     */
    public void setSelectedMeasure(String measure) {
        map.put(LCPID_SELECTED_MEASURE, measure);
    }

    /**
     * Get the selected LC point info details measure.
     * @return the selected measure.
     */
    public String getSelectedMeasure() {
        return (String)map.get(LCPID_SELECTED_MEASURE);
    }

    /**
     * Set the LCPID sort by param.
     * @param sortBy the way the data is currently sorted.
     */
    public void setSortBy(String sortBy) {
        map.put(LCPID_SORT_BY, sortBy);
    }

    /**
     * Get the LCPID sort by param.
     * @return the sort by param.
     */
    public String getSortBy() {
        return (String)map.get(LCPID_SORT_BY);
    }

    /**
     * Set the LCPID sort direction.
     * @param sortDirection the direction to sort the results.
     */
    public void setSortDirection(String sortDirection) {
        map.put(LCPID_SORT_DIRECTION, sortDirection);
    }

    /**
     * Get the LCPID sort direction.
     * @return the sort direction.
     */
    public String getSortDirection() {
        return (String)map.get(LCPID_SORT_DIRECTION);
    }

    /**
     * Map from div IDs on Learning Curve page to "hide" or "show" according to whether
     * they should be hidden or not.
     * @return map from div IDs on Learning Curve page to "hide" or "show" according to whether
     * they should be hidden or not
     */
    public Map<String, String> hideShow() {
        Map<String, String> hideShow = (Map<String, String>)map.get("hideShow");

        if (hideShow == null) {
            // default to showing some things but not others.
            hideShow = map("hideGraphInfoLink", "show", "hidePointInfoLink", "hide",
                    "pointInfoDetailsClose", "hide", "hideObservationTableLink", "show");
            map.put("hideShow", hideShow);
        }

        return hideShow;
    }

    /**
     * Update whether to hide or show div with id id.
     * @param id the div id
     * @param value "hide" or "show"
     */
    public void setHideShow(String id, String value) {
        hideShow().put(id, value);
    }

    /**
     * Set the Model Values 'Sort & Tag' flag.
     * @param sortAndTag true iff KC Values are to be sorted and tagged
     */
    public void setSortAndTag(Boolean sortAndTag) {
        map.put(LFA_SORT_AND_TAG_VALUES, sortAndTag);
    }

    /**
     * Get the 'Sort & Tag' flag for the Model Values tab.
     * @return true iff KC Values are to be sorted and tagged
     */
    public Boolean getSortAndTag() {
        return getBoolean(LFA_SORT_AND_TAG_VALUES);
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
