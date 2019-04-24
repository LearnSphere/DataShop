/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import static edu.cmu.pslc.datashop.util.UtilConstants.MAGIC_1000;

import java.text.DecimalFormat;

/**
 * This class represents all the information required for a single bar of a
 * performance profiler graph.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PerformanceProfilerBar {

    /** The Id of the sample. */
    private Integer sampleId;

    /** The name dependant on type. (Problem Name, Skill Name, etc.) */
    private String typeName;

    /** The database id of the type. (problem_id, skill_id, etc.) */
    private String typeId;

    /** This is the Id of the "parent" of the type.
     * Used to create uniqueness.  -1 if not needed */
    private Long typeParentId;

    /** Formatter for three decimal places. */
    private DecimalFormat formatter = new DecimalFormat("#.000");
    /**
     * Double of the errorRate.
     * This is the (#first attempt hints + # first attempt errors) / total # attempts
     */
    private Double errorRate;

    /** Percentage of the error rate which is accounted for by hints. */
    private Double errorRateHints;

    /** Percentage of the error rate which is accounted for by incorrects. */
    private Double errorRateIncorrects;

    /** AssistenceScore : #hints + #errors averaged by student. */
    private Double assistanceScore;

    /** average total incorrects per student. */
    private Double averageNumberIncorrects;

    /** average total hints per student */
    private Double averageNumberHints;

    /** Predicted Error Rate - Actual Error Rate */
    private Double residual;

    /** The number of unique students */
    private Integer numberStudents;

    /** The number of unique steps */
    private Integer numberSteps;

    /** The number of unique skills */
    private Integer numberSkills;

    /** The number of unique problems */
    private Integer numberProblems;

    /** Number of observations for this bar */
    private Integer observation;

    /** Predicted Error Rate of the primary model */
    private Double primaryPredicted;

    /** Predicted Error Rate of the 2ndary model. */
    private Double secondaryPredicted;

    /** The Step Duration. */
    private Double stepDuration;

    /** The Correct Step Duration. */
    private Double correctStepDuration;

    /** The Error Step Duration. */
    private Double errorStepDuration;

    /**
     * Returns assistanceScore.
     * @return Returns the assistanceScore.
     */
    public Double getAssistanceScore() {
        return assistanceScore;
    }

    /**
     * Set assistanceScore.
     * @param assistanceScore The assistanceScore to set.
     */
    public void setAssistanceScore(Double assistanceScore) {
        if (assistanceScore != null) {
            this.assistanceScore = Double.parseDouble(
                    formatter.format(assistanceScore));
        } else {
            this.assistanceScore = assistanceScore;
        }
    }

    /**
     * Returns averageNumberHints.
     * @return Returns the averageNumberHints.
     */
    public Double getAverageNumberHints() {
        return averageNumberHints;
    }

    /**
     * Set averageNumberHints.
     * @param averageNumberHints The averageNumberHints to set.
     */
    public void setAverageNumberHints(Double averageNumberHints) {
        if (averageNumberHints != null) {
            this.averageNumberHints = Double.parseDouble(
                    formatter.format(averageNumberHints));
        } else {
            this.averageNumberHints = averageNumberHints;
        }
    }

    /**
     * Returns averageNumberIncorrects.
     * @return Returns the averageNumberIncorrects.
     */
    public Double getAverageNumberIncorrects() {
        return averageNumberIncorrects;
    }

    /**
     * Set averageNumberIncorrects.
     * @param averageNumberIncorrects The averageNumberIncorrects to set.
     */
    public void setAverageNumberIncorrects(Double averageNumberIncorrects) {
        if (averageNumberIncorrects != null) {
            this.averageNumberIncorrects = Double.parseDouble(
                formatter.format(averageNumberIncorrects));
        } else {
            this.averageNumberIncorrects = averageNumberIncorrects;
        }
    }

    /**
     * Returns errorRate.
     * @return Returns the errorRate.
     */
    public Double getErrorRate() {
        return errorRate;
    }

    /**
     * Set errorRate.
     * @param errorRate The errorRate to set.
     */
    public void setErrorRate(Double errorRate) {
        if (errorRate != null) {
            this.errorRate = Double.parseDouble(
                    formatter.format(errorRate));
        } else {
            this.errorRate = errorRate;
        }
    }

    /**
     * Returns errorRateHints.
     * @return Returns the errorRateHints.
     */
    public Double getErrorRateHints() {
        return errorRateHints;
    }

    /**
     * Set errorRateHints.
     * @param errorRateHints The errorRateHints to set.
     */
    public void setErrorRateHints(Double errorRateHints) {
        if (errorRateHints != null) {
            this.errorRateHints = Double.parseDouble(
                    formatter.format(errorRateHints));
        } else {
            this.errorRateHints = errorRateHints;
        }
    }

    /**
     * Returns errorRateIncorrects.
     * @return Returns the errorRateIncorrects.
     */
    public Double getErrorRateIncorrects() {
        return errorRateIncorrects;
    }

    /**
     * Set errorRateIncorrects.
     * @param errorRateIncorrects The errorRateIncorrects to set.
     */
    public void setErrorRateIncorrects(Double errorRateIncorrects) {
        if (errorRateIncorrects != null) {
            this.errorRateIncorrects = Double.parseDouble(
                    formatter.format(errorRateIncorrects));
        } else {
            this.errorRateIncorrects = errorRateIncorrects;
        }
    }

    /** Returns numberProblems. @return Returns the numberProblems. */
    public Integer getNumberProblems() {
        return numberProblems;
    }

    /** Set numberProblems. @param numberProblems The numberProblems to set. */
    public void setNumberProblems(Integer numberProblems) {
        this.numberProblems = numberProblems;
    }

    /**
     * Returns numberSkills.
     * @return Returns the numberSkills.
     */
    public Integer getNumberSkills() {
        return numberSkills;
    }

    /**
     * Set numberSkills.
     * @param numberSkills The numberSkills to set.
     */
    public void setNumberSkills(Integer numberSkills) {
        this.numberSkills = numberSkills;
    }

    /**
     * Returns numberSteps.
     * @return Returns the numberSteps.
     */
    public Integer getNumberSteps() {
        return numberSteps;
    }

    /**
     * Set numberSteps.
     * @param numberSteps The numberSteps to set.
     */
    public void setNumberSteps(Integer numberSteps) {
        this.numberSteps = numberSteps;
    }

    /**
     * Returns numberStudents.
     * @return Returns the numberStudents.
     */
    public Integer getNumberStudents() {
        return numberStudents;
    }

    /**
     * Set numberStudents.
     * @param numberStudents The numberStudents to set.
     */
    public void setNumberStudents(Integer numberStudents) {
        this.numberStudents = numberStudents;
    }

    /**
     * Returns observation.
     * @return Returns the observation.
     */
    public Integer getObservation() {
        return observation;
    }

    /**
     * Set observation.
     * @param observation The observation to set.
     */
    public void setObservation(Integer observation) {
        this.observation = observation;
    }

    /**
     * Returns primaryPredicted.
     * @return Returns the primaryPredicted.
     */
    public Double getPrimaryPredicted() {
        return primaryPredicted;
    }

    /**
     * Set primaryPredicted.
     * @param primaryPredicted The primaryPredicted to set.
     */
    public void setPrimaryPredicted(Double primaryPredicted) {
        if (primaryPredicted != null) {
            this.primaryPredicted = Double.parseDouble(
                    formatter.format(primaryPredicted));
        } else {
            this.primaryPredicted = primaryPredicted;
        }
    }

    /**
     * Returns residual.
     * @return Returns the residual.
     */
    public Double getResidual() {
        return residual;
    }

    /**
     * Set residual.
     * @param residual The residual to set.
     */
    public void setResidual(Double residual) {
        if (residual != null) {
            this.residual = Double.parseDouble(
                    formatter.format(residual));
        } else {
            this.residual = residual;
        }
    }

    /**
     * Returns sampleId.
     * @return Returns the sampleId.
     */
    public Integer getSampleId() {
        return sampleId;
    }

    /**
     * Set sampleId.
     * @param sampleId The sampleId to set.
     */
    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }

    /**
     * Returns secondaryPredicted.
     * @return Returns the secondaryPredicted.
     */
    public Double getSecondaryPredicted() {
        return secondaryPredicted;
    }

    /**
     * Set secondaryPredicted.
     * @param secondaryPredicted The secondaryPredicted to set.
     */
    public void setSecondaryPredicted(Double secondaryPredicted) {
        if (secondaryPredicted != null) {
            this.secondaryPredicted = Double.parseDouble(
                    formatter.format(secondaryPredicted));
        } else {
            this.secondaryPredicted = secondaryPredicted;
        }
    }

    /**
     * Get the Step Duration.
     * @return Double the stepDuration
     */
    public Double getStepDuration() {
        return stepDuration;
    }

    /**
     * Set the Step Duration.
     * @param stepDuration the Step Duration (in seconds)
     */
    public void setStepDuration(Double stepDuration) {
        this.stepDuration = formatDuration(stepDuration);
    }

    /**
     * Get the Correct Step Duration.
     * @return Double the correctStepDuration
     */
    public Double getCorrectStepDuration() {
        return correctStepDuration;
    }

    /**
     * Set the Correct Step Duration.
     * @param correctStepDuration the Correct Step Duration (in seconds)
     */
    public void setCorrectStepDuration(Double correctStepDuration) {
        this.correctStepDuration = formatDuration(correctStepDuration);
    }

    /**
     * Get the Error Step Duration.
     * @return Double the errorStepDuration
     */
    public Double getErrorStepDuration() {
        return errorStepDuration;
    }

    /**
     * Set the Error Step Duration.
     * @param errorStepDuration the Error Step Duration (in seconds)
     */
    public void setErrorStepDuration(Double errorStepDuration) {
        this.errorStepDuration = formatDuration(errorStepDuration);
    }

    /**
     * Returns typeName.
     * @return Returns the typeName.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Set typeName.
     * @param typeName The typeName to set.
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /** Returns typeId. @return Returns the typeId. */
    public String getTypeId() {
        return typeId;
    }

    /** Set typeId. @param typeId The typeId to set. */
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /**
     * Returns typeParentId.
     * @return Returns the typeParentId.
     */
    public Long getTypeParentId() {
        return typeParentId;
    }

    /**
     * Set typeParentId.
     * @param typeParentId The typeParentId to set.
     */
    public void setTypeParentId(Long typeParentId) {
        this.typeParentId = typeParentId;
    }

    /**
     * DisplaySttring for this class.
     * @return String.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("TypeName: " + getTypeName());
        buffer.append("\t ParentId: " + getTypeParentId());
        buffer.append("\t SampleId: " + getSampleId());
        buffer.append("\t AssistanceScore: " + getAssistanceScore());
        buffer.append("\t AvgNumHints: " + getAverageNumberHints());
        buffer.append("\t AvgNumIncorrects: " + getAverageNumberIncorrects());

        buffer.append("\t ErrorRate: " + getErrorRate());
        buffer.append("\t ErrorRateHints: " + getErrorRateHints());
        buffer.append("\t ErrorRateIncorrects: " + getErrorRateIncorrects());

        buffer.append("\t Observations: " + getObservation());
        buffer.append("\t Residual: " + getResidual());
        buffer.append("\t Predicted: " + getPrimaryPredicted());
        buffer.append("\t 2ndPredicted: " + getSecondaryPredicted());

        buffer.append("\t StepDuration: " + getStepDuration());
        buffer.append("\t CorrectStepDuration: " + getCorrectStepDuration());
        buffer.append("\t ErrorStepDuration: " + getErrorStepDuration());

        return buffer.toString();
    }

    /**
     * Helper method to format step duration metrics,
     * converting from milliseconds (db) to seconds (UI).
     * @param duration the duration in millisecond
     * @return duration in seconds, formatted
     */
    private Double formatDuration(Double duration) {
        if (duration == null) { return duration; }
        return Double.parseDouble(formatter.format(duration / MAGIC_1000));
    }
}
