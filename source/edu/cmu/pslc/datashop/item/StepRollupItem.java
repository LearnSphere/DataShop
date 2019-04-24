/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.text.DecimalFormat;
import java.util.Date;

import edu.cmu.pslc.datashop.type.CorrectFlag;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents an aggregation of data for a single step, student, sample, skill.
 *
 * @author Benjamin Billings
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepRollupItem extends Item implements java.io.Serializable, Comparable  {

    /** Composite key of student id, skill id and opportunity number. */
    private Long id;

    /** The sample portion of the composite key */
    private SampleItem sample;
    /** The student portion of the composite key */
    private StudentItem student;
    /** The step portion of the composite key */
    private SubgoalItem step;
    /** The skill model portion of the composite key. */
    private SkillModelItem skillModel;
    /** The sample portion of the composite key */
    private SkillItem skill;
    /** The opportunity number of the skill for the sample/student */
    private Integer opportunity;

    /** Dataset this item is associated with. */
    private DatasetItem dataset;
    /** Step associated with this rollup. */
    private ProblemItem problem;
    /** Which particular attempt/view/opportunity at this problem */
    private Integer problemView;

    /** Total number of incorrects for this step/student/sample. */
    private Integer incorrects;
    /** Total number of hints for this step/student/sample. */
    private Integer hints;
    /** Total number of corrects for this step/student/sample. */
    private Integer corrects;
    /** The predicted error rate for this point. */
    private Double predicted;
    /** The enumerated type of the first attempt */
    private CorrectFlag firstAttempt;
    /** The conditions assigned to this step. */
    private String conditions;
    /** The timestamp for when the step occurred. */
    private Date stepTime;
    /** The length of time consumed on this step. */
    private Long stepDuration;
    /** The length of time consumed on this step when first attempt is 'correct'. */
    private Long correctStepDuration;
    /** The length of time consume on this step when first attempt is NOT 'correct'. */
    private Long errorStepDuration;
    /** The time of the first transaction. */
    private Date firstTransactionTime;
    /** The time of the first correct transaction. */
    private Date correctTransactionTime;
    /** The time at which this step began. */
    private Date stepStartTime;
    /** The time at which this step ended. */
    private Date stepEndTime;
    /** Formatter to round predicted values to 2 decimal places. */
    private DecimalFormat formatter = new DecimalFormat("#0.0000000000");

    /** Default constructor. */
    public StepRollupItem() {
        incorrects = new Integer(0);
        hints = new Integer(0);
        corrects = new Integer(0);
    }

    /**
     * Returns id.
     * @return Returns the Long id.
     */
    public Comparable getId() { return id; }

    /**
     * Set id.
     * @param id The id to set.
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Returns opportunity.
     * @return Returns the opportunity.
     */
    public Integer getOpportunity() {
        return opportunity;
    }

    /**
     * Set opportunity.
     * @param opportunity The opportunity to set.
     */
    public void setOpportunity(Integer opportunity) {
        this.opportunity = opportunity;
    }

    /**
     * Returns sample.
     * @return Returns the sample.
     */
    public SampleItem getSample() {
        return sample;
    }

    /**
     * Set sample.
     * @param sample The sample to set.
     */
    public void setSample(SampleItem sample) {
        this.sample = sample;
    }

    /**
     * Returns skill.
     * @return Returns the skill.
     */
    public SkillItem getSkill() {
        return skill;
    }

    /**
     * Set skill.
     * @param skill The skill to set.
     */
    public void setSkill(SkillItem skill) {
        this.skill = skill;
    }

    /**
     * Returns skillModel.
     * @return Returns the skillModel.
     */
    public SkillModelItem getSkillModel() {
        return skillModel;
    }

    /**
     * Set skillModel.
     * @param skillModel The skillModel to set.
     */
    public void setSkillModel(SkillModelItem skillModel) {
        this.skillModel = skillModel;
    }

    /**
     * Returns step.
     * @return Returns the step.
     */
    public SubgoalItem getStep() {
        return step;
    }

    /**
     * Set step.
     * @param step The step to set.
     */
    public void setStep(SubgoalItem step) {
        this.step = step;
    }

    /**
     * Returns student.
     * @return Returns the student.
     */
    public StudentItem getStudent() {
        return student;
    }

    /**
     * Set student.
     * @param student The student to set.
     */
    public void setStudent(StudentItem student) {
        this.student = student;
    }

    /**
     * Returns dataset.
     * @return Returns the dataset.
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set dataset.
     * @param dataset The dataset to set.
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Returns problem.
     * @return Returns the problem.
     */
    public ProblemItem getProblem() {
        return problem;
    }

    /**
     * Set problem.
     * @param problem The problem to set.
     */
    public void setProblem(ProblemItem problem) {
        this.problem = problem;
    }

    /**
     * Returns problemView.
     * @return Returns the problemView.
     */
    public Integer getProblemView() {
        return problemView;
    }

    /**
     * Set problemView.
     * @param problemView The problemView to set.
     */
    public void setProblemView(Integer problemView) {
        this.problemView = problemView;
    }

    /**
     * Returns correct.
     * @return Returns the correct.
     */
    public Integer getCorrects() { return corrects; }

    /**
     * Set correct.
     * @param corrects The corrects to set.
     */
    public void setCorrects(Integer corrects) { this.corrects = corrects; }

    /**
     * Returns incorrects.
     * @return Returns the incorrects.
     */
    public Integer getIncorrects() { return incorrects; }

    /**
     * Set incorrects.
     * @param incorrects The incorrects to set.
     */
    public void setIncorrects(Integer incorrects) { this.incorrects = incorrects; }

    /**
     * Returns firstAttempt.
     * @return Returns the firstAttempt.
     */
    public CorrectFlag getFirstAttempt() { return firstAttempt; }

    /**
     * Set firstAttempt.
     * @param firstAttempt The firstAttempt to set.
     */
    public void setFirstAttempt(CorrectFlag firstAttempt) { this.firstAttempt = firstAttempt; }

    /**
     * Returns hints.
     * @return Returns the hints.
     */
    public Integer getHints() { return hints; }

    /**
     * Set hints.
     * @param hints The hints to set.
     */
    public void setHints(Integer hints) { this.hints = hints; }

    /**
     * Return conditions.
     * @return returns the conditions for this item.
     */
    public String getConditions() { return conditions; }

    /**
     * Set the conditions.
     * @param conditions the conditions to set.
     */
    public void setConditions(String conditions) { this.conditions = conditions; }

    /**
     * Returns predicted.
     * @return Returns the predicted.
     */
    public Double getPredicted() { return predicted; }

    /** The format for the predicted values. */
    private DecimalFormat predictedFormat = new DecimalFormat("0.0000000000");

    /**
     * Returns predicted value as a string formatted to two decimal places..
     * @return Returns the predicted value as a string
     */
    public String getPredictedFormatted() {
        return predicted == null ? "" : predictedFormat.format(predicted);
    }

    /**
     * Set predicted.
     * @param predicted The predicted to set.
     */
    public void setPredicted(Double predicted) {
        this.predicted = (predicted != null)
            ? Double.parseDouble(formatter.format(predicted)) : null;
    }

    /**
     * Returns stepTime.
     * @return Returns the stepTime.
     */
    public Date getStepTime() {
        return stepTime;
    }

    /**
     * Set stepTime.
     * @param stepTime The stepTime to set.
     */
    public void setStepTime(Date stepTime) {
        this.stepTime = stepTime;
    }

    /**
     * Returns errorStepDuration.
     * @return Returns the errorStepDuration.
     */
    public Long getErrorStepDuration() {
        return errorStepDuration;
    }

    /**
     * Set errorStepDuration.
     * @param errorStepDuration The errorStepDuration to set.
     */
    public void setErrorStepDuration(Long errorStepDuration) {
        this.errorStepDuration = errorStepDuration;
    }

    /**
     * Returns correctStepDuration.
     * @return Returns the correctStepDuration.
     */
    public Long getCorrectStepDuration() {
        return correctStepDuration;
    }

    /**
     * Set correctStepDuration.
     * @param correctStepDuration The correctStepDuration to set.
     */
    public void setCorrectStepDuration(Long correctStepDuration) {
        this.correctStepDuration = correctStepDuration;
    }

    /**
     * Returns stepDuration.
     * @return Returns the stepDuration.
     */
    public Long getStepDuration() {
        return stepDuration;
    }

    /**
     * Set stepDuration.
     * @param stepDuration The stepDuration to set.
     */
    public void setStepDuration(Long stepDuration) {
        this.stepDuration = stepDuration;
    }

    /**
     * Returns the correctTransactionTime.
     * @return the correctTransactionTime.
     */
    public Date getCorrectTransactionTime() {
        return correctTransactionTime;
    }

    /**
     * Sets the correctTransactionTime.
     * @param correctTransactionTime the correctTransactionTime to set.
     */
    public void setCorrectTransactionTime(Date correctTransactionTime) {
        this.correctTransactionTime = correctTransactionTime;
    }

    /** Returns firstTransactionTime.
     * @return Returns the firstTransactionTime.
     */
    public Date getFirstTransactionTime() {
        return firstTransactionTime;
    }

    /** Set firstTransactionTime.
     * @param firstTransactionTime The firstTransactionTime to set.
     */
    public void setFirstTransactionTime(Date firstTransactionTime) {
        this.firstTransactionTime = firstTransactionTime;
    }

    /**
     * Set the step start time.
     * @param stepStartTime the stepStartTime to set
     */
    public void setStepStartTime(Date stepStartTime) {
            this.stepStartTime = stepStartTime;
    }

    /**
     * Get the step start time.
     * @return the stepStartTime
     */
    public Date getStepStartTime() {
            return stepStartTime;
    }

    /**
     * Set the step end time.
     * @param stepEndTime the stepEndTime to set
     */
    public void setStepEndTime(Date stepEndTime) {
            this.stepEndTime = stepEndTime;
    }

    /**
     * Get the step end time.
     * @return the stepEndTime
     */
    public Date getStepEndTime() {
            return stepEndTime;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append((hashCode()));
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToStringFK("sample", getSample()));
         buffer.append(objectToStringFK("student", getStudent()));
         buffer.append(objectToStringFK("step", getStep()));
         buffer.append(objectToString("opportunity", getOpportunity()));
         buffer.append(objectToStringFK("skill", getSkill()));
         buffer.append(objectToStringFK("model", getSkillModel()));
         buffer.append(objectToStringFK("dataset", getDataset()));
         buffer.append(objectToStringFK("problem", getProblem()));
         buffer.append(objectToString("problemView", getProblemView()));
         buffer.append(objectToString("firstAttempt", getFirstAttempt()));
         buffer.append(objectToString("incorrects", getIncorrects()));
         buffer.append(objectToString("hints", getHints()));
         buffer.append(objectToString("corrects", getCorrects()));
         buffer.append(objectToString("stepTime", getStepTime()));
         buffer.append(objectToString("predicted", getPredicted()));
         buffer.append(objectToString("stepDuration", getStepDuration()));
         buffer.append(objectToString("errorStepDuration", getErrorStepDuration()));
         buffer.append(objectToString("correctStepDuration", getCorrectStepDuration()));
         buffer.append(objectToString("firstTxnTime", getFirstTransactionTime()));
         buffer.append(objectToString("correctTxTime", getCorrectTransactionTime()));
         buffer.append(objectToString("startTime", getStepStartTime()));
         buffer.append(objectToString("endTime", getStepEndTime()));
         buffer.append(objectToString("conditions", getConditions()));
         buffer.append("]");
         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj instanceof StepRollupItem) {
            StepRollupItem otherItem = (StepRollupItem)obj;

            if (!objectEqualsFK(getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEqualsFK(getSample(), otherItem.getSample())) {
                return false;
            }
            if (!objectEqualsFK(getStudent(), otherItem.getStudent())) {
                return false;
            }

            if (!objectEqualsFK(getProblem(), otherItem.getProblem())) {
                return false;
            }
            if (!objectEquals(getProblemView(), otherItem.getProblemView())) {
                return false;
            }
            if (!objectEqualsFK(getStep(), otherItem.getStep())) {
                return false;
            }

            if (!objectEqualsFK(getSkillModel(), otherItem.getSkillModel())) {
                return false;
            }
            if (!objectEqualsFK(getSkill(), otherItem.getSkill())) {
                return false;
            }
            if (!objectEquals(getOpportunity(), otherItem.getOpportunity())) {
                return false;
            }

            if (!objectEquals(getFirstAttempt(), otherItem.getFirstAttempt())) {
                return false;
            }
            if (!objectEquals(getIncorrects(), otherItem.getIncorrects())) {
                return false;
            }
            if (!objectEquals(getHints(), otherItem.getHints())) {
                return false;
            }
            if (!objectEquals(getCorrects(), otherItem.getCorrects())) {
                return false;
            }

            if (!objectEquals(getErrorStepDuration(), otherItem.getErrorStepDuration())) {
                return false;
            }
            if (!objectEquals(getCorrectStepDuration(), otherItem.getCorrectStepDuration())) {
                return false;
            }
            if (!objectEquals(getStepDuration(), otherItem.getStepDuration())) {
                return false;
            }

            if (!objectEquals(getStepTime(), otherItem.getStepTime())) {
                return false;
            }
            if (!objectEquals(getFirstTransactionTime(), otherItem.getFirstTransactionTime())) {
                return false;
            }
            if (!objectEquals(getCorrectTransactionTime(), otherItem.getCorrectTransactionTime())) {
                return false;
            }
            if (!objectEquals(getStepStartTime(), otherItem.getStepStartTime())) {
                return false;
            }
            if (!objectEquals(getStepEndTime(), otherItem.getStepEndTime())) {
                return false;
            }

            if (!objectEquals(getPredicted(), otherItem.getPredicted())) {
                return false;
            }
            if (!objectEquals(getConditions(), otherItem.getConditions())) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;

        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSample());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getStudent());

        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProblem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProblemView());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getStep());

        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSkillModel());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSkill());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getOpportunity());

        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFirstAttempt());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getIncorrects());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHints());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCorrects());

        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStepDuration());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCorrectStepDuration());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getErrorStepDuration());

        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStepTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFirstTransactionTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCorrectTransactionTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStepStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStepEndTime());

        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPredicted());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getConditions());

        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Sample</li>
     *   <li>Student</li>
     *   <li>SkillModel</li>
     *   <li>Skill</li>
     *   <li>Opportunity</li>
     *   <li>Step</li>
     *   <li>Dataset</li>
     *   <li>Problem</li>
     *   <li>Step Time<li>
     *   <li>First Attempt</li>
     *   <li>Incorrects</li>
     *   <li>Hints</li>
     *   <li>Corrects</li>
     *   <li>Predicted</li>
     *   <li>Step Duration</li>
     *   <li>Correct Step Duration</li>
     *   <li>Error Step Duration</li>
     *   <li>Correct Transaction Time</li>
     *   <li>Conditions</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        StepRollupItem otherItem = (StepRollupItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSample(), otherItem.getSample());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getStudent(), otherItem.getStudent());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getProblem(), otherItem.getProblem());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProblemView(), otherItem.getProblemView());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStep(), otherItem.getStep());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSkillModel(), otherItem.getSkillModel());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSkill(), otherItem.getSkill());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getOpportunity(), otherItem.getOpportunity());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFirstAttempt(), otherItem.getFirstAttempt());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getIncorrects(), otherItem.getIncorrects());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getHints(), otherItem.getHints());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCorrects(), otherItem.getCorrects());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStepDuration(), otherItem.getStepDuration());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCorrectStepDuration(), otherItem.getCorrectStepDuration());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getErrorStepDuration(), otherItem.getErrorStepDuration());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStepTime(), otherItem.getStepTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFirstTransactionTime(),
                otherItem.getFirstTransactionTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCorrectTransactionTime(),
                otherItem.getCorrectTransactionTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStepStartTime(), otherItem.getStepStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStepEndTime(), otherItem.getStepEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPredicted(), otherItem.getPredicted());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getConditions(), otherItem.getConditions());
        if (value != 0) { return value; }

        return value;
    }
} // end StepRollupItem.java
