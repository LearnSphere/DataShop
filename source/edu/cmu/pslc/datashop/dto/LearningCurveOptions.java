/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Arrays;
import java.util.List;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * This class represents all the information required for a learning curve report.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14234 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-08-03 12:09:39 -0400 (Thu, 03 Aug 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurveOptions {

    /** Learning Curve Type :: Assistance Score. */
    public static final String ASSISTANCE_SCORE = "Assistance Score";
    /** Learning Curve Type :: Error Rate. */
    public static final String ERROR_RATE = "Error Rate";
    /** Learning Curve Type :: Number of Incorrects. */
    public static final String NUMBER_OF_INCORRECTS = "Number of Incorrects";
    /** Learning Curve Type :: Number of Hints. */
    public static final String NUMBER_OF_HINTS = "Number of Hints";
    /** Learning Curve Type :: Step Duration. */
    public static final String STEP_DURATION = "Step Duration";
    /** Learning Curve Type :: Correct Step Duration. */
    public static final String CORRECT_STEP_DURATION = "Correct Step Duration";
    /** Learning Curve Type :: Error Step Duration. */
    public static final String ERROR_STEP_DURATION = "Error Step Duration";
    /** error_bar_type value for Standard Error. */
    public static final String ERROR_BAR_TYPE_SE = "std_err";
    /** error_bar_type value for Standard Deviation. */
    public static final String ERROR_BAR_TYPE_SD = "std_dev";

    /** List to hold all LC Types. */
    public static final List<String> LC_TYPE_OPTIONS = Arrays.asList(ERROR_RATE, ASSISTANCE_SCORE,
            NUMBER_OF_INCORRECTS, NUMBER_OF_HINTS, STEP_DURATION, CORRECT_STEP_DURATION,
            ERROR_STEP_DURATION);

    /** Curve type :: assistance_score. */
    public static final String ASSISTANCE_SCORE_TYPE = "assistance_score";
    /** Curve type :: error_rate. */
    public static final String ERROR_RATE_TYPE = "error_rate";
    /** Curve type :: average_incorrects. */
    public static final String AVG_INCORRECTS_TYPE = "average_incorrects";
    /** Curve Type :: average_hints. */
    public static final String AVG_HINTS_TYPE = "average_hints";
    /** Curve Type :: step_duration. */
    public static final String STEP_DURATION_TYPE = "step_duration";
    /** Curve Type :: correct_step_duration. */
    public static final String CORRECT_STEP_DURATION_TYPE = "correct_step_duration";
    /** Curve Type :: error_step_duration. */
    public static final String ERROR_STEP_DURATION_TYPE = "error_step_duration";

    /** Magic 2.5 for the stdDeviationCutoff default value. */
    private static final Double MAGIC_TWO_POINT_FIVE = new Double(2.5);

    /** The selected learning curve type. */
    private String selectedMeasure;

    /** The sample. */
    private SampleItem sampleItem;

    /** List of skill items to draw the curve for. */
    private List skillList;

    /** List of student items to draw the curve for. */
    private List studentList;

    /** Indicates if this graph is a view by skill.  If false it is a view by student. */
    private boolean isViewBySkill;

    /**
     * The min opportunity to allow.
     * Will remove student/skill pairs w/o this number of opportunities.
     */
    private Integer opportunityCutOffMin;

    /**
     * The maximum opportunity to allow.
     * Will remove any opportunities above this number.
     */
    private Integer opportunityCutOffMax;

    /**
     * The standard deviation cutoff for latency curves.
     * Will remove any opportunities above this number.
     */
    private Double stdDeviationCutOff = MAGIC_TWO_POINT_FIVE;

    /** Skill model being using to draw the primary curve. */
    private SkillModelItem primaryModel;
    /** Skill model to draw a secondary predicted curve. */
    private SkillModelItem secondaryModel;

    /** Type of error bar being displayed. */
    private String errorBarType = null;

    /** Flag indicating if the curve to be displayed is low-stakes only. */
    private Boolean displayLowStakesCurve = false;

    /**
     * Returns opportunityCutOffMax.
     * @return Returns the opportunityCutOffMax.
     */
    public Integer getOpportunityCutOffMax() {
        return opportunityCutOffMax;
    }

    /**
     * Set opportunityCutOffMax.
     * @param opportunityCutOffMax The opportunityCutOffMax to set.
     */
    public void setOpportunityCutOffMax(Integer opportunityCutOffMax) {
        this.opportunityCutOffMax = opportunityCutOffMax;
    }

    /**
     * Returns opportunityCutOffMin.
     * @return Returns the opportunityCutOffMin.
     */
    public Integer getOpportunityCutOffMin() {
        return opportunityCutOffMin;
    }

    /**
     * Set opportunityCutOffMin.
     * @param opportunityCutOffMin The opportunityCutOffMin to set.
     */
    public void setOpportunityCutOffMin(Integer opportunityCutOffMin) {
        this.opportunityCutOffMin = opportunityCutOffMin;
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

    /** Returns primaryModel. @return Returns the primaryModel. */
    public SkillModelItem getPrimaryModel() {
        return primaryModel;
    }

    /** Set primaryModel. @param primaryModel The primaryModel to set. */
    public void setPrimaryModel(SkillModelItem primaryModel) {
        this.primaryModel = primaryModel;
    }

    /**
     * Returns secondaryModel.
     * @return Returns the secondaryModel.
     */
    public SkillModelItem getSecondaryModel() {
        return secondaryModel;
    }

    /**
     * Set secondaryModel.
     * @param secondaryModel The secondaryModel to set.
     */
    public void setSecondaryModel(SkillModelItem secondaryModel) {
        this.secondaryModel = secondaryModel;
    }

    /**
     * Returns skillList.
     * @return Returns the skillList.
     */
    public List getSkillList() {
        return skillList;
    }

    /**
     * Set skillList.
     * @param skillList The skillList to set.
     */
    public void setSkillList(List skillList) {
        this.skillList = skillList;
    }

    /**
     * Returns studentList.
     * @return Returns the studentList.
     */
    public List getStudentList() {
        return studentList;
    }

    /**
     * Set studentList.
     * @param studentList The studentList to set.
     */
    public void setStudentList(List studentList) {
        this.studentList = studentList;
    }

    /**
     * Returns if this graph is a view by skill or not.
     * @return true if viewing by skill, false otherwise.
     */
    public boolean isViewBySkill() {
        return isViewBySkill;
    }

    /**
     * Set isViewBySkill.
     * @param viewBySkillFlag the value to set.
     */
    public void setIsViewBySkill(boolean viewBySkillFlag) {
        this.isViewBySkill = viewBySkillFlag;
    }

    /**
     * Returns the selectedType (Assistance Score, Error Rate, etc).
     * @return the selectedType
     */
    public String getSelectedMeasure() {
        return selectedMeasure;
    }

    /** possible curve types */
    private static final String[] MEASURE_TYPES = new String[] {
        ASSISTANCE_SCORE_TYPE, ERROR_RATE_TYPE, AVG_INCORRECTS_TYPE, AVG_HINTS_TYPE,
        STEP_DURATION_TYPE, CORRECT_STEP_DURATION_TYPE, ERROR_STEP_DURATION_TYPE
    };

    /** labels for the corresponding curve types */
    private static final String[] MEASURE_LABELS = new String[] {
        ASSISTANCE_SCORE, ERROR_RATE, NUMBER_OF_INCORRECTS, NUMBER_OF_HINTS, STEP_DURATION,
        CORRECT_STEP_DURATION, ERROR_STEP_DURATION
    };

    /**
     * Sets the selectedType.
     * @param selectedMeasure the selectedType to set
     */
    public void setSelectedMeasure(String selectedMeasure) {
        for (int i = 0; i < MEASURE_TYPES.length; i++) {
            if (selectedMeasure.equals(MEASURE_TYPES[i])) {
                this.selectedMeasure = MEASURE_LABELS[i];
            }
        }
    }

    /**
     * Look up the measure type for the selected measure.
     * @return the measure type for the selected measure
     */
    public String getSelectedMeasureType() {
        for (int i = 0; i < MEASURE_LABELS.length; i++) {
            if (selectedMeasure.equals(MEASURE_LABELS[i])) {
                return MEASURE_TYPES[i];
            }
        }
        return null;
    }

    /**
     * Returns the stdDeviationCutOff.
     * @return the stdDeviationCutOff
     */
    public Double getStdDeviationCutOff() {
        return stdDeviationCutOff;
    }

    /**
     * Sets the stdDeviationCutOff.  Defaults to 2.5.
     * @param stdDeviationCutOff the stdDeviationCutOff to set
     */
    public void setStdDeviationCutOff(Double stdDeviationCutOff) {
        if (stdDeviationCutOff != null) {
            this.stdDeviationCutOff = stdDeviationCutOff;
        }
    }

    /**
     * Helper method to determine if the selectedType is a latency curve.
     * @return true if selectedType is Assistance Time or Correct Step Time, false otherwise.
     */
    public boolean isLatencyCurve() {
        return selectedMeasure.equals(STEP_DURATION)
            || selectedMeasure.equals(CORRECT_STEP_DURATION)
            || selectedMeasure.equals(ERROR_STEP_DURATION);
    }

    /**
     * Returns the error bar type: ERROR_BAR_TYPE_SE or ERROR_BAR_TYPE_SD.
     * @return Returns the errorBarType
     */
    public String getErrorBarType() {
        return errorBarType;
    }

    /**
     * Set error bar type.
     * @param in The error bar type.
     */
    public void setErrorBarType(String in) {
        this.errorBarType = in;
    }

    /**
     * Returns the error bar type, in a user-friendly format.
     * @return Returns the errorBarType
     */
    public String getErrorBarTypeStr() {
        if (errorBarType == null) {
            return "";
        }
        if (errorBarType.equals(LearningCurveOptions.ERROR_BAR_TYPE_SE)) {
            return "Standard Error";
        } else {
            return "Standard Deviation";
        }
    }

    /**
     * Returns the point info upper bound label, in a user-friendly format.
     * @return Returns the upperBoundLabel
     */
    public String getUpperBoundLabelStr() {
        if (errorBarType == null) {
            return "";
        }
        if (errorBarType.equals(LearningCurveOptions.ERROR_BAR_TYPE_SE)) {
            return "Upper Bound (M+1SE): ";
        } else {
            return "Upper Bound (M+1SD): ";
        }
    }

    /**
     * Returns the point info lower bound label, in a user-friendly format.
     * @return Returns the lowerBoundLabel
     */
    public String getLowerBoundLabelStr() {
        if (errorBarType == null) {
            return "";
        }
        if (errorBarType.equals(LearningCurveOptions.ERROR_BAR_TYPE_SE)) {
            return "Lower Bound (M+1SE): ";
        } else {
            return "Lower Bound (M+1SD): ";
        }
    }

    /**
     * Returns if displaying lowStakes curve only.
     * @return true if lowStakes-only curve
     */
    public Boolean getDisplayLowStakesCurve() {
        return displayLowStakesCurve;
    }

    /**
     * Set displayLowStakesCurve flag.
     * @param displayLowStakesCurve the value to set.
     */
    public void setDisplayLowStakesCurve(Boolean displayLowStakesCurve) {
        this.displayLowStakesCurve = displayLowStakesCurve;
    }

}
