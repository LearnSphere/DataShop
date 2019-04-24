package edu.cmu.pslc.datashop.servlet.performanceprofiler;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the session saved information for the performance profiler
 * report.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4765 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-05-01 10:52:19 -0400 (Thu, 01 May 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PerformanceProfilerContext implements Serializable {

    /**
     * Servlet Session context stored in the map, this allows us to synchronize
     * the list to handle multiple thread requests.
     */
    private Map map;

    /** Map Key. */
    private static final String MIN_STUDENTS_KEY = "pp_min_students";
    /** Map Key. */
    private static final String MIN_STEPS_KEY = "pp_min_steps";
    /** Map Key. */
    private static final String MIN_PROBLEMS_KEY = "pp_min_problems";
    /** Map Key. */
    private static final String MIN_SKILLS_KEY = "pp_min_skills";

    /** Map Key. */
    private static final String VIEW_BY_CATEGORY = "pp_view_by_category";
    /** Map Key. */
    private static final String VIEW_BY_TYPE = "pp_view_by_type";
    /** Map Key. */
    private static final String SORT_BY = "pp_sort_by";
    /** Map Key. */
    private static final String SORT_BY_DIRECTION = "pp_sort_by_direction";
    /** Map Key. */
    private static final String DISPLAY_PREDICTED = "pp_dispay_predicted";
    /** Map Key. */
    private static final String DISPLAY_UNMAPPED_STEPS = "pp_display_unmapped";

    /** Map Key. */
    private static final String BOTTOM_LIMIT = "pp_bottom_limit";
    /** Map Key. */
    private static final String TOP_LIMIT = "pp_top_limit";

    /** Default Constructor. */
    public PerformanceProfilerContext() {
        map = Collections.synchronizedMap(new HashMap <Integer, Map> ());
    }

    /** Returns minStudents. @return Returns the minStudents. */
    public Integer getMinStudents() {
        return (Integer)map.get(MIN_STUDENTS_KEY);
    }

    /** Set minStudents. @param minStudents The minStudents to set. */
    public void setMinStudents(Integer minStudents) {
        map.put(MIN_STUDENTS_KEY, minStudents);
    }

    /** Returns minProblems. @return Returns the minProblems. */
    public Integer getMinProblems() {
        return (Integer)map.get(MIN_PROBLEMS_KEY);
    }

    /** Set minProblems. @param minProblems The minProblems to set. */
    public void setMinProblems(Integer minProblems) {
        map.put(MIN_PROBLEMS_KEY, minProblems);
    }

    /** Returns minSteps. @return Returns the minSteps. */
    public Integer getMinSteps() {
        return (Integer)map.get(MIN_STEPS_KEY);
    }

    /** Set minSteps. @param minSteps The minSteps to set. */
    public void setMinSteps(Integer minSteps) {
        map.put(MIN_STEPS_KEY, minSteps);
    }

    /** Returns minSkills. @return Returns the minSkills. */
    public Integer getMinSkills() {
        return (Integer)map.get(MIN_SKILLS_KEY);
    }

    /** Set minSkills. @param minSkills The minSkills to set. */
    public void setMinSkills(Integer minSkills) {
        map.put(MIN_SKILLS_KEY, minSkills);
    }

    /** Returns viewByCategory. @return the category as a String. */
    public String getViewByCategory() {
        return (String)map.get(VIEW_BY_CATEGORY);
    }

    /** Set viewByCategory. @param category the selected category to view. */
    public void setViewByCategory(String category) {
        map.put(VIEW_BY_CATEGORY, category);
    }

    /** Returns viewByType. @return the type as a String. */
    public String getViewByType() {
        return (String)map.get(VIEW_BY_TYPE);
    }

    /** Set viewByType. @param type the selected type to view. */
    public void setViewByType(String type) {
        map.put(VIEW_BY_TYPE, type);
    }

    /** Returns sort type. @return the sort as a String. */
    public String getSortBy() {
        return (String)map.get(SORT_BY);
    }

    /** Set sort type. @param sort the selected sort to view. */
    public void setSortBy(String sort) {
        map.put(SORT_BY, sort);
    }

    /** Returns the sort direction. @return true if sort ascending, false otherwise. */
    public Boolean getSortByAscendingDirection() {
        return (Boolean)map.get(SORT_BY_DIRECTION);
    }

    /** Set the sort direction. @param direction of true if the sort direction is ascending. */
    public void setSortByAscendingDirection(Boolean direction) {
        map.put(SORT_BY_DIRECTION, direction);
    }

    /** Returns if the predicted values should be displayed. @return Boolean of true to display. */
    public Boolean getDisplayPredicted() {
        return (Boolean)map.get(DISPLAY_PREDICTED);
    }

    /** Set whether to display predicted. @param display true to display or false otherwise. */
    public void setDisplayPredicted(Boolean display) {
        map.put(DISPLAY_PREDICTED, display);
    }

    /**
     * Returns if steps with no skills mapped to them should be returned.
     * @return Boolean of true to display. */
    public Boolean getDisplayUnmapped() {
        return (Boolean)map.get(DISPLAY_UNMAPPED_STEPS);
    }

    /**
     * Set whether to display steps with no skills.
     * @param display true to display or false otherwise.
     */
    public void setDisplayUnmapped(Boolean display) {
        map.put(DISPLAY_UNMAPPED_STEPS, display);
    }

    /**
     * Returns the limit on the bottom number of rows to return.
     * @return Integer value of the limit.
     */
    public Integer getBottomLimit() {
        return (Integer)map.get(BOTTOM_LIMIT);
    }

    /**
     * Sets the number of "bottom" rows to display.
     * @param limit Integer of the number of bottom rows to display
     */
    public void setBottomLimit(Integer limit) {
        map.put(BOTTOM_LIMIT, limit);
    }

    /**
     * Returns the limit on the top number of rows to return.
     * @return Integer value of the limit.
     */
    public Integer getTopLimit() {
        return (Integer)map.get(TOP_LIMIT);
    }

    /**
     * Sets the number of "top" rows to display.
     * @param limit Integer of the number of top rows to display
     */
    public void setTopLimit(Integer limit) {
        map.put(TOP_LIMIT, limit);
    }
}
