/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;


/**
 * This class represents all the information required for a single bar of a
 * performance profiler graph. To both create the bar and view options.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10284 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-11-04 13:20:51 -0500 (Mon, 04 Nov 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProfilerOptions implements Serializable {

    /** PerformanceProfilerType :: Aggregate by Skill. */
    public static final String TYPE_SKILL = "Knowledge Component";
    /** PerformanceProfilerType :: Aggregate by Problem. */
    public static final String TYPE_PROBLEM = "Problem";
    /** PerformanceProfilerType :: Aggregate by Step. */
    public static final String TYPE_STEP = "Step";
    /** PerformanceProfilerType :: Aggregate by Student. */
    public static final String TYPE_STUDENT = "Student";
    /** Ordered collection of all Types.  */
    public static final List TYPE_OPTIONS;
    static {
        TYPE_OPTIONS = new ArrayList();
        TYPE_OPTIONS.add(TYPE_PROBLEM);
        TYPE_OPTIONS.add(TYPE_STEP);
        TYPE_OPTIONS.add(TYPE_STUDENT);
        TYPE_OPTIONS.add(TYPE_SKILL);
    }

    /** Option to sort the resulting data. */
    public static final String SORT_BY_NAME = "Name";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_ERROR_RATE = "Error Rate";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_PREDICTED_ERROR_RATE = "Predicted Error Rate";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_RESIDUAL = "Residuals";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_ASSISTANCE_SCORE = "Assistance Score";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_TOTAL_HINTS = "Total Hints";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_TOTAL_INCORRECTS = "Total Incorrects";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_FIRST_ATTEMPT_HINTS = "First Attempt Hints";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_FIRST_ATTEMPT_INCORRECTS = "First Attempt Incorrects";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_NUMBER_OF_PROBLEMS = "Number of Problems";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_NUMBER_OF_STEPS = "Number of Steps";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_NUMBER_OF_STUDENTS = "Number of Students";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_NUMBER_OF_SKILLS = "Number of KCs";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_STEP_DURATION = "Step Duration";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_CORRECT_STEP_DURATION = "Correct Step Duration";
    /** Option to sort the resulting data. */
    public static final String SORT_BY_ERROR_STEP_DURATION = "Error Step Duration";

    /** Ordered collection of all Sorting Options.  */
    public static final List SORT_OPTIONS;
    static {
        SORT_OPTIONS = new ArrayList();
        SORT_OPTIONS.add(SORT_BY_ERROR_RATE);
        SORT_OPTIONS.add(SORT_BY_PREDICTED_ERROR_RATE);
        SORT_OPTIONS.add(SORT_BY_RESIDUAL);
        SORT_OPTIONS.add(SORT_BY_ASSISTANCE_SCORE);
        SORT_OPTIONS.add(SORT_BY_TOTAL_HINTS);
        SORT_OPTIONS.add(SORT_BY_TOTAL_INCORRECTS);
        SORT_OPTIONS.add(SORT_BY_FIRST_ATTEMPT_HINTS);
        SORT_OPTIONS.add(SORT_BY_FIRST_ATTEMPT_INCORRECTS);
        SORT_OPTIONS.add(SORT_BY_NAME);
        SORT_OPTIONS.add(SORT_BY_NUMBER_OF_PROBLEMS);
        SORT_OPTIONS.add(SORT_BY_NUMBER_OF_SKILLS);
        SORT_OPTIONS.add(SORT_BY_NUMBER_OF_STUDENTS);
        SORT_OPTIONS.add(SORT_BY_NUMBER_OF_STEPS);
        SORT_OPTIONS.add(SORT_BY_STEP_DURATION);
        SORT_OPTIONS.add(SORT_BY_CORRECT_STEP_DURATION);
        SORT_OPTIONS.add(SORT_BY_ERROR_STEP_DURATION);
    }

    /** Option on how to view the resulting data. */
    public static final String VIEW_ERROR_RATE = "Error Rate (%)";
    /** Option on how to view the resulting data. */
    public static final String VIEW_ASSISTANCE_SCORE = "Assistance Score";
    /** Option on how to view the resulting data. */
    public static final String VIEW_NUM_INCORRECTS = "Average Number Incorrects";
    /** Option on how to view the resulting data. */
    public static final String VIEW_NUM_HINTS = "Average Number Hints";
    /** Option on how to view the resulting data. */
    public static final String VIEW_RESIDUALS = "Residual Error Rate % (Predicted - Actual)";
    /** Option on how to view the resulting data. */
    public static final String VIEW_STEP_DURATION = "Step Duration (seconds)";
    /** Option on how to view the resulting data. */
    public static final String VIEW_CORRECT_STEP_DURATION = "Correct Step Duration (seconds)";
    /** Option on how to view the resulting data. */
    public static final String VIEW_ERROR_STEP_DURATION = "Error Step Duration (seconds)";
    /** Ordered collection of all Views.  */
    public static final List VIEW_OPTIONS;
    static {
        VIEW_OPTIONS = new ArrayList();
        VIEW_OPTIONS.add(VIEW_ERROR_RATE);
        VIEW_OPTIONS.add(VIEW_ASSISTANCE_SCORE);
        VIEW_OPTIONS.add(VIEW_NUM_INCORRECTS);
        VIEW_OPTIONS.add(VIEW_NUM_HINTS);
        VIEW_OPTIONS.add(VIEW_RESIDUALS);
        VIEW_OPTIONS.add(VIEW_STEP_DURATION);
        VIEW_OPTIONS.add(VIEW_CORRECT_STEP_DURATION);
        VIEW_OPTIONS.add(VIEW_ERROR_STEP_DURATION);
    }

    /** Sample to create everything for */
    private SampleItem sampleItem;

    /** List of SkillItems that represents all selected skills. */
    private Collection skillList;

    /** List of StudentItems that represents all selected students. */
    private Collection studentList;

    /** List of ProblemItems that represents all selected problems. */
    private Collection problemList;

    /** The type to aggregate the data along (Student, Step, Problem etc.)*/
    private String aggregateType;

    /** How to sort the results.  Default(by name). */
    private String sortBy = SORT_BY_NAME;

    /** The primary SkillModel. */
    private SkillModelItem primarySkillModel = null;
    /** The secondary SkillModel to get prediction from.  Default(null) */
    private SkillModelItem secondarySkillModel = null;

    /** Indicated whether to sort by ascending or descending.  Default(TRUE): ascending */
    private boolean sortByAscending = true;

    /** The string to indicate how to view the profiler */
    private String viewByType = VIEW_ERROR_RATE;

    /** Flag indicating whether or not to view predicted scores */
    private boolean viewPredicted = false;

    /** Number of upper limit items. */
    private Integer upperLimit = null;

    /** Number of lower limit items. */
    private Integer lowerLimit = null;

    /** Flag indicating whether or not to display steps w/o skills */
    private boolean displayUnmappedFlag = true;

    /** Integer indicating the total number of graphs that will be displayed for scaling */
    private int numberOfGraphs = 1;

    /** Integer indicating the minimum number of problems required */
    private Integer minProblems = null;

    /** Integer indicating the minimum number of steps required */
    private Integer minSteps = null;

    /** Integer indicating the minimum number of students required */
    private Integer minStudents = null;

    /** Integer indicating the minimum number of skills required */
    private Integer minSkills = null;

    /** Default Constructor. */
    public ProfilerOptions() { }

    /**
     * Full constructor (Does not allow List setting to avoid errors).
     * @param sampleItem the sample to create a performance profiler for.
     * @param primarySkillModel the primary skill model
     * @param aggregateType the type of performance profiler to view/create.
     * @param viewByType the type of view to display for the performance profiler.
     * @param sortBy how to sort the results.
     * @param sortByAscending whether to sort the results ascending or descending.
     * @param secondarySkillModel the secondary skill model (can be null).
     */
    public ProfilerOptions(SampleItem sampleItem,
            SkillModelItem primarySkillModel,
            String aggregateType, String viewByType, String sortBy, boolean sortByAscending,
            SkillModelItem secondarySkillModel) {
        if (sampleItem == null) {
            throw new IllegalArgumentException(
                "SampleItem cannot be null for the performance profiler");
        }
        this.sampleItem = sampleItem;
        this.primarySkillModel = primarySkillModel;
        setAggregateType(aggregateType);
        setSortBy(sortBy);
        setViewByType(viewByType);
        this.sortByAscending = sortByAscending;
        this.secondarySkillModel = secondarySkillModel;
    }

    /**
     * Returns aggregateType.
     * @return Returns the aggregateType.
     */
    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * Set aggregateType.
     * @param aggregateType The aggregateType to set.
     */
    public void setAggregateType(String aggregateType) {
        if (TYPE_PROBLEM.equals(aggregateType)) {
            this.aggregateType = TYPE_PROBLEM;
        } else if (TYPE_SKILL.equals(aggregateType)) {
            this.aggregateType = TYPE_SKILL;
        } else if (TYPE_STUDENT.equals(aggregateType)) {
            this.aggregateType = TYPE_STUDENT;
        } else if (TYPE_STEP.equals(aggregateType)) {
            this.aggregateType = TYPE_STEP;
        } else {
            //In this case we are assuming it is a custom dataset level to aggregate by.
            this.aggregateType = aggregateType;
        }
    }

    /**
     * Returns problemList.
     * @return Returns the problemList.
     */
    public Collection getProblemList() {
        return problemList;
    }

    /**
     * Set problemList.
     * @param problemList The problemList to set.
     */
    public void setProblemList(Collection problemList) {
        this.problemList = problemList;
    }

    /**
     * Returns sampleItem.
     * @return Returns the sampleItem.
     */
    public SampleItem getSampleItem() {
        return sampleItem;
    }

    /**
     * Set sampleItem.
     * @param sampleItem The sampleItem to set.
     */
    public void setSampleItem(SampleItem sampleItem) {
        this.sampleItem = sampleItem;
    }

    /**
     * Gets primarySkillModel.
     * @return the primarySkillModel
     */
    public SkillModelItem getPrimarySkillModel() {
        return primarySkillModel;
    }

    /**
     * Sets the primarySkillModel.
     * @param primarySkillModel the primarySkillModel to set
     */
    public void setPrimarySkillModel(SkillModelItem primarySkillModel) {
        this.primarySkillModel = primarySkillModel;
    }

    /**
     * Returns secondarySkillModel.
     * @return Returns the secondarySkillModel.
     */
    public SkillModelItem getSecondarySkillModel() {
        return secondarySkillModel;
    }

    /**
     * Set secondarySkillModel.
     * @param secondarySkillModel The secondarySkillModel to set.
     */
    public void setSecondarySkillModel(SkillModelItem secondarySkillModel) {
        this.secondarySkillModel = secondarySkillModel;
    }

    /**
     * Returns skillList.
     * @return Returns the skillList.
     */
    public Collection getSkillList() {
        return skillList;
    }

    /**
     * Set skillList.
     * @param skillList The skillList to set.
     */
    public void setSkillList(Collection skillList) {
        this.skillList = skillList;
    }

    /**
     * Returns sortBy.
     * @return Returns the sortBy.
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Set sortBy.
     * @param sortBy The sortBy to set.
     */
    public void setSortBy(String sortBy) {
        if (SORT_OPTIONS.contains(sortBy)) {
            this.sortBy = sortBy;
        } else {
            throw new IllegalArgumentException("'" + sortBy
                    + "' is not a valid value for sorting the performance profiler.");
        }
    }

    /**
     * Returns sortByAscending.
     * @return Returns the sortByAscending.
     */
    public boolean getSortByAscending() {
        return sortByAscending;
    }

    /**
     * Set sortByAscending.
     * @param sortByAscending The sortByAscending to set.
     */
    public void setSortByAscending(boolean sortByAscending) {
        this.sortByAscending = sortByAscending;
    }

    /**
     * Returns studentList.
     * @return Returns the studentList.
     */
    public Collection getStudentList() {
        return studentList;
    }

    /**
     * Set studentList.
     * @param studentList The studentList to set.
     */
    public void setStudentList(Collection studentList) {
        this.studentList = studentList;
    }

    /**
     * Returns viewByType.
     * @return Returns the viewByType.
     */
    public String getViewByType() {
        return viewByType;
    }

    /**
     * Set viewByType.
     * @param viewByType The viewByType to set.
     */
    public void setViewByType(String viewByType) {
        if (VIEW_ASSISTANCE_SCORE.equals(viewByType)) {
            this.viewByType = VIEW_ASSISTANCE_SCORE;
        } else if (VIEW_ERROR_RATE.equals(viewByType)) {
            this.viewByType = VIEW_ERROR_RATE;
        } else if (VIEW_NUM_HINTS.equals(viewByType)) {
            this.viewByType = VIEW_NUM_HINTS;
        } else if (VIEW_NUM_INCORRECTS.equals(viewByType)) {
            this.viewByType = VIEW_NUM_INCORRECTS;
        } else if (VIEW_RESIDUALS.equals(viewByType)) {
            this.viewByType = VIEW_RESIDUALS;
        } else if (VIEW_STEP_DURATION.equals(viewByType)) {
            this.viewByType = VIEW_STEP_DURATION;
        } else if (VIEW_CORRECT_STEP_DURATION.equals(viewByType)) {
            this.viewByType = VIEW_CORRECT_STEP_DURATION;
        } else if (VIEW_ERROR_STEP_DURATION.equals(viewByType)) {
            this.viewByType = VIEW_ERROR_STEP_DURATION;
        } else {
            throw new IllegalArgumentException("'" + viewByType
                    + "' is not a valid value for viewing the performance profiler.");
        }
    }

    /**
     * Returns viewPredicted.
     * @return Returns the viewPredicted.
     */
    public boolean viewPredicted() {
        return viewPredicted;
    }

    /**
     * Set viewPredicted.
     * @param viewPredicted The viewPredicted to set.
     */
    public void setViewPredicted(boolean viewPredicted) {
        this.viewPredicted = viewPredicted;
    }

    /**
     * Returns lowerLimit.
     * @return Returns the lowerLimit.
     */
    public Integer getLowerLimit() {
        return lowerLimit;
    }

    /**
     * Set lowerLimit.
     * @param lowerLimit The lowerLimit to set.
     */
    public void setLowerLimit(Integer lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    /**
     * Returns upperLimit.
     * @return Returns the upperLimit.
     */
    public Integer getUpperLimit() {
        return upperLimit;
    }

    /**
     * Set upperLimit.
     * @param upperLimit The upperLimit to set.
     */
    public void setUpperLimit(Integer upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * Returns displayUnmappedFlag.
     * @return Returns the displayUnmappedFlag.
     */
    public boolean displayUnmapped() {
        return displayUnmappedFlag;
    }

    /**
     * Set displayUnmappedFlag.
     * @param displayUnmappedFlag The displayUnmappedFlag to set.
     */
    public void setDisplayUnmappedFlag(boolean displayUnmappedFlag) {
        this.displayUnmappedFlag = displayUnmappedFlag;
    }

    /**
     * Returns numberOfGraphs.
     * @return Returns the numberOfGraphs.
     */
    public int getNumberOfGraphs() {
        return numberOfGraphs;
    }

    /**
     * Set numberOfGraphs.
     * @param numberOfGraphs The numberOfGraphs to set.
     */
    public void setNumberOfGraphs(int numberOfGraphs) {
        this.numberOfGraphs = numberOfGraphs;
    }

    /** Returns minProblems. @return Returns the minProblems. */
    public Integer getMinProblems() {
        return minProblems;
    }

    /** Set minProblems. @param minProblems The minProblems to set. */
    public void setMinProblems(Integer minProblems) {
        this.minProblems = minProblems;
    }

    /** Returns minSteps. @return Returns the minSteps. */
    public Integer getMinSteps() {
        return minSteps;
    }

    /** Set minSteps. @param minSteps The minSteps to set. */
    public void setMinSteps(Integer minSteps) {
        this.minSteps = minSteps;
    }

    /** Returns minStudents. @return Returns the minStudents. */
    public Integer getMinStudents() {
        return minStudents;
    }

    /** Set minStudents. @param minStudents The minStudents to set. */
    public void setMinStudents(Integer minStudents) {
        this.minStudents = minStudents;
    }

    /** Returns minSkills. @return Returns the minSkills. */
    public Integer getMinSkills() {
        return minSkills;
    }

    /** Set minSkills. @param minSkills The minSkills to set. */
    public void setMinSkills(Integer minSkills) {
        this.minSkills = minSkills;
    }
}
