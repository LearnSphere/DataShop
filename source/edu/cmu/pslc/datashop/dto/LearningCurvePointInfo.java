/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ASSISTANCE_SCORE_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_RATE_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.AVG_INCORRECTS_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.AVG_HINTS_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.CORRECT_STEP_DURATION_TYPE;
import static edu.cmu.pslc.datashop.dto.LearningCurveOptions.ERROR_STEP_DURATION_TYPE;

import java.text.DecimalFormat;

/**
 * The subset of the data in a LearningCurvePoint to display when a point is selected.
 * @author jimbokun
 * @version $Revision: 10096 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-10-07 07:26:08 -0400 (Mon, 07 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearningCurvePointInfo extends DTO {

    /** Number format for to include commas but no decimals. */
    private static final DecimalFormat COMMA_DF = new DecimalFormat("#,###,##0");

    /** see comments on accessor methods below */
    private short skillsCount, problemsCount, stepsCount, studentsCount, observations, dropped;

    /** Formatted version of the number of observations. */
    private String obsString;

    /** see comments on accessor methods below */
    private float value, predicted, stdError, stdDeviation;

    /** see comments on accessor methods below */
    private String sampleName = null;

    /**
     * Create point info from LearningCurvePoint.
     * @param p the learning curve point
     */
    public LearningCurvePointInfo(LearningCurvePoint p) {
        setSkillsCount((short)p.getSkillsCount());
        setProblemsCount((short)p.getProblemsCount());
        setStepsCount((short)p.getStepsCount());
        setStudentsCount((short)p.getStudentsCount());
        setObservations(p.getObservations().shortValue());
        setPredicted(checkFloat(p.getPredictedErrorRate()));
    }

    /**
     * Create point info from LearningCurvePoint.
     * @param p the learning curve point
     * @param curveType String indicating the curve type
     */
    public LearningCurvePointInfo(LearningCurvePoint p, String curveType) {
        this(p);
        Integer obs = p.getObservationsForCurveType(curveType);

        if (obs != null) {
            setObservations(obs.shortValue());
        }
        setValue(selectValueForMeasure(p, curveType));
        setStdError(selectStdErrorForMeasure(p, curveType));
        setStdDeviation(selectStdDevForMeasure(p, curveType));

        Integer preCut = p.getPreCutoffObservations();

        setDropped(preCut == null ? 0 : (short)(preCut.shortValue() - observations));
    }

    /**
     * The number of skills for this point.
     * @return the number of skills for this point
     */
    public short getSkillsCount() {
        return skillsCount;
    }
    /**
     * Set the number of skills for this point.
     * @param skills the number of skills for this point
     */
    private void setSkillsCount(short skills) {
        this.skillsCount = skills;
    }

    /**
     * The number of problems for this point.
     * @return the number of problems for this point.
     */
    public short getProblemsCount() {
        return problemsCount;
    }
    /**
     * Set the number of problems for this point.
     * @param problems the number of problems for this point.
     */
    private void setProblemsCount(short problems) {
        this.problemsCount = problems;
    }

    /**
     * The number of steps for this point.
     * @return the number of steps for this point
     */
    public short getStepsCount() {
        return stepsCount;
    }
    /**
     * Set the number of steps for this point.
     * @param steps the number of steps for this point.
     */
    private void setStepsCount(short steps) {
        this.stepsCount = steps;
    }

    /**
     * The number of students for this point.
     * @return the number of students for this point.
     */
    public short getStudentsCount() {
        return studentsCount;
    }
    /**
     * Set the number of students for this point.
     * @param students the number of students for this point.
     */
    private void setStudentsCount(short students) {
        this.studentsCount = students;
    }

    /**
     * The value for the currently selected measure.
     * @return the value for the currently selected measure.
     */
    public float getValue() {
        return value;
    }
    /**
     * Set the value for the currently selected measure.
     * @param value the value for the currently selected measure.
     */
    private void setValue(float value) {
        this.value = value;
    }

    /**
     * The std error for the currently selected measure.
     * @return the std error for the currently selected measure.
     */
    public float getStdError() {
        return stdError;
    }
    /**
     * Set the std error for the currently selected measure.
     * @param value the std error for the currently selected measure.
     */
    private void setStdError(float value) {
        this.stdError = value;
    }

    /**
     * The std deviation for the currently selected measure.
     * @return the std deviation for the currently selected measure.
     */
    public float getStdDeviation() {
        return stdDeviation;
    }
    /**
     * Set the std deviation for the currently selected measure.
     * @param value the std deviation for the currently selected measure.
     */
    private void setStdDeviation(float value) {
        this.stdDeviation = value;
    }

    /**
     * We need to pass back the sample name so it can be displayed.
     * @param sampleName the sample name
     */
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    /**
     * We need to pass back the sample name so it can be displayed.
     * @return the sample name
     */
    public String getSampleName() {
        return sampleName;
    }

    /**
     * Returns observations.
     * @return Returns the observations.
     */
    public short getObservations() {
        return observations;
    }

    /** Set observations. @param observations The observations to set. */
    private void setObservations(short observations) {
        this.observations = observations;
        this.obsString = COMMA_DF.format(observations);
    }

    /**
     * Returns observations as a formatted string.
     * @return Returns the observations as a formated string.
     */
    public String getObsString() {
        return obsString;
    }

    /**
     * The predicted value for this point.
     * @return the predicted value for this point
     */
    public float getPredicted() {
        return predicted;
    }

    /**
     * Set the predicted value for this point.
     * @param predicted the predicted value for this point
     */
    public void setPredicted(float predicted) {
        this.predicted = predicted;
    }

    /**
     * The number of dropped observations.
     * @return The number of dropped observations
     */
    public short getDropped() {
        return dropped;
    }

    /**
     * The number of dropped observations.
     * @param dropped the number of dropped observations
     */
    public void setDropped(short dropped) {
        this.dropped = dropped;
    }

    /**
     * Float value of number or NaN (not a number) if null.
     * @param number the number
     * @return float value of number or NaN (not a number) if null
     */
    private static float checkFloat(Number number) {
        return number == null ? Float.NaN : number.floatValue();
    }

    /**
     * Select the appropriate measure from p for the curve measure.
     * @param point the point
     * @param measure String indicating the curve measure
     * @return the appropriate measure from p for the curve measure.
     */
    public static float selectValueForMeasure(LearningCurvePoint point, String measure) {
        Double value = null;

        if (measure.equals(ASSISTANCE_SCORE_TYPE)) {
            value = point.getAssistanceScore();
        } else if (measure.equals(ERROR_RATE_TYPE)) {
            value = point.getErrorRates();
        } else if (measure.equals(AVG_INCORRECTS_TYPE)) {
            value = point.getAvgIncorrects();
        } else if (measure.equals(AVG_HINTS_TYPE)) {
            value = point.getAvgHints();
        } else if (measure.equals(STEP_DURATION_TYPE)) {
            value = point.getStepDuration();
        } else if (measure.equals(CORRECT_STEP_DURATION_TYPE)) {
            value = point.getCorrectStepDuration();
        } else if (measure.equals(ERROR_STEP_DURATION_TYPE)) {
            value = point.getErrorStepDuration();
        } else {
            throw new IllegalStateException(measure + " is not a valid curve measure");
        }

        return checkFloat(value);
    }

    /**
     * Select the appropriate standard error from p for the specified measure.
     * @param point the point
     * @param measure String indicating the curve measure
     * @return the appropriate std error from p for the curve measure.
     */
    public static float selectStdErrorForMeasure(LearningCurvePoint point, String measure) {
        Double value = point.getStdErrorForCurveType(measure);
        return checkFloat(value);
    }

    /**
     * Select the appropriate standard deviation from p for the specified measure.
     * @param point the point
     * @param measure String indicating the curve measure
     * @return the appropriate std dev from p for the curve measure.
     */
    public static float selectStdDevForMeasure(LearningCurvePoint point, String measure) {
        Double value = point.getStdDeviationForCurveType(measure);
        return checkFloat(value);
    }
}
