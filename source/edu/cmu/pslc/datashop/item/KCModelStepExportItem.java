/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import edu.cmu.pslc.datashop.dto.StepExportRow;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Knowledge Component Model step export stats.
 * Everything needed to complete one line of a KCModel export,
 * except for the model columns.
 *
 * @author Cindy Tipper
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelStepExportItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this KC model export. */
    private Long id;
    /** Dataset associated with these stats. */
    private DatasetItem dataset;
    /** Subgoal (step) associated with these stats. */
    private SubgoalItem step;
    /** Step GUID. */
    private String stepGuid;
    /** Problem hierarchy. */
    private String problemHierarchy;
    /** Problem name. */
    private String problemName;
    /** Step name. */
    private String stepName;
    /** Max problem view. */
    private Double maxProblemView;
    /** Average number of incorrects. */
    private Double avgIncorrects;
    /** Average number of hints. */
    private Double avgHints;
    /** Average number of corrects. */
    private Double avgCorrects;
    /** Percentage of first attempts that are incorrect. */
    private Double pctIncorrectFirstAttempts;
    /** Percentage of first attempts that are hints. */
    private Double pctHintFirstAttempts;
    /** Percentage of first attempts that are correct. */
    private Double pctCorrectFirstAttempts;
    /** Average step duration (in seconds). */
    private Double avgStepDuration;
    /** Average correct step duration (in seconds). */
    private Double avgCorrectStepDuration;
    /** Average error step duration (in seconds). */
    private Double avgErrorStepDuration;
    /** Total number of students. */
    private Integer totalStudents;
    /** Total number of opportunities. */
    private Integer totalOpportunities;

    /** Default constructor. */
    public KCModelStepExportItem() {}

    /**
     *  Constructor with id.
     *  @param kcModelStepExportId Database generated unique Id for this KC model step export.
     */
    public KCModelStepExportItem(Long kcModelStepExportId) {
        this.id = kcModelStepExportId;
    }

    /**
     * Constructor, given a StepExportRow object.
     * @param row the StepExportRow
     */
    public KCModelStepExportItem(StepExportRow row) {
	this.setStepGuid(row.getStepGuid());
	this.setProblemHierarchy(row.getProblemHierarchy());
	this.setProblemName(row.getProblemName());
	this.setStepName(row.getStepName());
	this.setMaxProblemView(row.getMaxProblemView());
	this.setAvgIncorrects(row.getAvgIncorrects());
	this.setAvgHints(row.getAvgHints());
	this.setAvgCorrects(row.getAvgCorrects());
	this.setPctIncorrectFirstAttempts(row.getPctIncorrectFirstAttempts());
	this.setPctHintFirstAttempts(row.getPctHintFirstAttempts());
	this.setPctCorrectFirstAttempts(row.getPctCorrectFirstAttempts());
	this.setAvgStepDuration(row.getAvgStepDuration());
	this.setAvgCorrectStepDuration(row.getAvgCorrectStepDuration());
	this.setAvgErrorStepDuration(row.getAvgErrorStepDuration());
	this.setTotalStudents(row.getTotalStudents());
	this.setTotalOpportunities(row.getTotalOpportunities());
    }

    /**
     * Get kcModelStepExportId.
     * @return the Long id as a comparable.
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set kcModelStepExportId.
     * @param kcModelStepExportId Database generated unique Id for this KC model export.
     */
    public void setId(Long kcModelStepExportId) {
        this.id = kcModelStepExportId;
    }

    /**
     * Get dataset.
     * @return a DatasetItem object
     */
    public DatasetItem getDataset() { return this.dataset; }

    /**
     * Set dataset.
     * @param dataset DatasetItem associated with this item
     */
    public void setDataset(DatasetItem dataset) { this.dataset = dataset; }

    /**
     * Get subgoal (step).
     * @return a SubgoalItem object
     */
    public SubgoalItem getStep() { return this.step; }

    /**
     * Set subgoal (step).
     * @param step SubgoalItem associated with this item
     */
    public void setStep(SubgoalItem step) { this.step = step; }

    /**
     * Get step GUID.
     * @return String the subgoal GUID
     */
    public String getStepGuid() { return stepGuid; }

    /**
     * Set the step GUID.
     * @param guid
     */
    public void setStepGuid(String guid) { this.stepGuid = guid; }

    /**
     * Get the problem hierarchy for this step.
     * @return String problemHierarchy
     */
    public String getProblemHierarchy() { return problemHierarchy; }

    /**
     * Set the problem hierarchy for this step.
     * @param hierarchy
     */
    public void setProblemHierarchy(String hierarchy) { this.problemHierarchy = hierarchy; }

    /**
     * Get the problem name for this step.
     * @return String problemName
     */
    public String getProblemName() { return problemName; }

    /**
     * Set the problem name for this step.
     * @param name
     */
    public void setProblemName(String name) { this.problemName = name; }

    /**
     * Get the step name for this step.
     * @return String stepName
     */
    public String getStepName() { return stepName; }

    /**
     * Set the step name for this step.
     * @param name
     */
    public void setStepName(String name) { this.stepName = name; }

    /**
     * Get the max number of problem views for this step.
     * @return Double problemViews
     */
    public Double getMaxProblemView() { return maxProblemView; }

    /**
     * Set the max number of problem views for this step.
     * @param problemView
     */
    public void setMaxProblemView(Double problemView) { this.maxProblemView = problemView; }

    /**
     * Get the average of incorrects for this step.
     * @return Double avgIncorrects
     */
    public Double getAvgIncorrects() { return avgIncorrects; }

    /**
     * Set the average of incorrects for this stepe.
     * @param avgIncorrects
     */
    public void setAvgIncorrects(Double avgIncorrects) { this.avgIncorrects = avgIncorrects; }

    /**
     * Get the average of hints for this step.
     * @return Double avgHints
     */
    public Double getAvgHints() { return avgHints; }

    /**
     * Set the average of hints for this stepe.
     * @param avgHints
     */
    public void setAvgHints(Double avgHints) { this.avgHints = avgHints; }

    /**
     * Get the average of corrects for this step.
     * @return Double avgCorrects
     */
    public Double getAvgCorrects() { return avgCorrects; }

    /**
     * Set the average of corrects for this stepe.
     * @param avgCorrects
     */
    public void setAvgCorrects(Double avgCorrects) { this.avgCorrects = avgCorrects; }

    /**
     * Get the percentage of first attempts that are incorrect for this step.
     * @return Double pctIncorrectFirstAttempts
     */
    public Double getPctIncorrectFirstAttempts() { return pctIncorrectFirstAttempts; }

    /**
     * Set the percentage of first attempts that are incorrect for this step.
     * @param pct
     */
    public void setPctIncorrectFirstAttempts(Double pct) { this.pctIncorrectFirstAttempts = pct; }

    /**
     * Get the percentage of first attempts that are hints for this step.
     * @return Double pctHintFirstAttempts
     */
    public Double getPctHintFirstAttempts() { return pctHintFirstAttempts; }

    /**
     * Set the percentage of first attempts that are hints for this step.
     * @param pct
     */
    public void setPctHintFirstAttempts(Double pct) { this.pctHintFirstAttempts = pct; }

    /**
     * Get the percentage of first attempts that are correct for this step.
     * @return Double pctCorrectFirstAttempts
     */
    public Double getPctCorrectFirstAttempts() { return pctCorrectFirstAttempts; }

    /**
     * Set the percentage of first attempts that are correct for this step.
     * @param pct
     */
    public void setPctCorrectFirstAttempts(Double pct) { this.pctCorrectFirstAttempts = pct; }

    /**
     * Get the average step duration for this step.
     * @return Double avgStepDuration
     */
    public Double getAvgStepDuration() { return avgStepDuration; }

    /**
     * Set the average step duration for this stepe.
     * @param stepDuration
     */
    public void setAvgStepDuration(Double stepDuration) { this.avgStepDuration = stepDuration; }

    /**
     * Get the average correct step duration for this step.
     * @return Double avgCorrectStepDuration
     */
    public Double getAvgCorrectStepDuration() { return avgCorrectStepDuration; }

    /**
     * Set the average correct step duration for this stepe.
     * @param stepDuration
     */
    public void setAvgCorrectStepDuration(Double stepDuration) {
        this.avgCorrectStepDuration = stepDuration;
    }

    /**
     * Get the average error step duration for this step.
     * @return Double avgErrorStepDuration
     */
    public Double getAvgErrorStepDuration() { return avgErrorStepDuration; }

    /**
     * Set the average error step duration for this stepe.
     * @param stepDuration
     */
    public void setAvgErrorStepDuration(Double stepDuration) {
        this.avgErrorStepDuration = stepDuration;
    }

    /**
     * Get the total number of students for this step.
     * @return Integer the totalStudents
     */
    public Integer getTotalStudents() { return totalStudents; }

    /**
     * Set the total number of students for this step.
     * @param students
     */
    public void setTotalStudents(Integer students) { this.totalStudents = students; }

    /**
     * Get the total number of opportunities for this step.
     * @return Integer the totalOpportunities
     */
    public Integer getTotalOpportunities() { return totalOpportunities; }

    /**
     * Set the total number of opportunities for this step.
     * @param opportunities
     */
    public void setTotalOpportunities(Integer opportunities) {
        this.totalOpportunities = opportunities;
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
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToStringFK("Step", getStep()));
        buffer.append(objectToString("stepGuid", getStepGuid()));
        buffer.append(objectToString("problemHierarchy", getProblemHierarchy()));
        buffer.append(objectToString("problemName", getProblemName()));
        buffer.append(objectToString("stepName", getStepName()));
        buffer.append(objectToString("maxProblemView", getMaxProblemView()));
        buffer.append(objectToString("avgIncorrects", getAvgIncorrects()));
        buffer.append(objectToString("avgHints", getAvgHints()));
        buffer.append(objectToString("avgCorrects", getAvgCorrects()));
        buffer.append(objectToString("pctIncorrectFirstAttempts", getPctIncorrectFirstAttempts()));
        buffer.append(objectToString("pctHintFirstAttempts", getPctHintFirstAttempts()));
        buffer.append(objectToString("pctCorrectFirstAttempts", getPctCorrectFirstAttempts()));
        buffer.append(objectToString("avgStepDuration", getAvgStepDuration()));
        buffer.append(objectToString("avgCorrectStepDuration", getAvgCorrectStepDuration()));
        buffer.append(objectToString("avgErrorStepDuration", getAvgErrorStepDuration()));
        buffer.append(objectToString("totalStudents", getTotalStudents()));
        buffer.append(objectToString("totalOpportunities", getTotalOpportunities()));
        buffer.append("]");

        return buffer.toString();
    }

    /**
     * Equals function for this class.
     * @param obj Object of any type, should be an Item for equality check
     * @return boolean true if the items are equal, false if not
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof KCModelStepExportItem) {
            KCModelStepExportItem otherItem = (KCModelStepExportItem)obj;

            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEqualsFK(this.getStep(), otherItem.getStep())) {
                return false;
            }
            if (!objectEquals(this.getStepGuid(), otherItem.getStepGuid())) {
                return false;
            }
            if (!objectEquals(this.getProblemHierarchy(), otherItem.getProblemHierarchy())) {
                return false;
            }
            if (!objectEquals(this.getProblemName(), otherItem.getProblemName())) {
                return false;
            }
            if (!objectEquals(this.getStepName(), otherItem.getStepName())) {
                return false;
            }
            if (!objectEquals(this.getMaxProblemView(), otherItem.getMaxProblemView())) {
                return false;
            }
            if (!objectEquals(this.getAvgIncorrects(), otherItem.getAvgIncorrects())) {
                return false;
            }
            if (!objectEquals(this.getAvgHints(), otherItem.getAvgHints())) {
                return false;
            }
            if (!objectEquals(this.getAvgCorrects(), otherItem.getAvgCorrects())) {
                return false;
            }
            if (!objectEquals(this.getPctIncorrectFirstAttempts(),
                              otherItem.getPctIncorrectFirstAttempts())) {
                return false;
            }
            if (!objectEquals(this.getPctHintFirstAttempts(),
                              otherItem.getPctHintFirstAttempts())) {
                return false;
            }
            if (!objectEquals(this.getPctCorrectFirstAttempts(),
                              otherItem.getPctCorrectFirstAttempts())) {
                return false;
            }
            if (!objectEquals(this.getAvgStepDuration(), otherItem.getAvgStepDuration())) {
                return false;
            }
            if (!objectEquals(this.getAvgCorrectStepDuration(),
                              otherItem.getAvgCorrectStepDuration())) {
                return false;
            }
            if (!objectEquals(this.getAvgErrorStepDuration(),
                              otherItem.getAvgErrorStepDuration())) {
                return false;
            }
            if (!objectEquals(this.getTotalStudents(), otherItem.getTotalStudents())) {
                return false;
            }
            if (!objectEquals(this.getTotalOpportunities(), otherItem.getTotalOpportunities())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getStep());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStepGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProblemHierarchy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProblemName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStepName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getMaxProblemView());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAvgIncorrects());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAvgHints());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAvgCorrects());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPctIncorrectFirstAttempts());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPctHintFirstAttempts());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPctCorrectFirstAttempts());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAvgStepDuration());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAvgCorrectStepDuration());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAvgErrorStepDuration());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTotalStudents());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTotalOpportunities());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset</li>
     * <li>step</li>
     * <li>step_guid</li>
     * <li>problemHierarchy</li>
     * <li>problemName</li>
     * <li>stepName</li>
     * <li>maxProblemView</li>
     * <li>avgIncorrects</li>
     * <li>avgHints</li>
     * <li>avgCorrects</li>
     * <li>pctIncorrectFirstAttempts</li>
     * <li>pctHintFirstAttempts</li>
     * <li>pctCorrectFirstAttempts</li>
     * <li>avgStepDuration</li>
     * <li>avgCorrectStepDuration</li>
     * <li>avgErrorStepDuration</li>
     * <li>totalStudents</li>
     * <li>totalOpportunitiess</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        KCModelStepExportItem otherItem = (KCModelStepExportItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getStep(), otherItem.getStep());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStepGuid(), otherItem.getStepGuid());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProblemHierarchy(), otherItem.getProblemHierarchy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProblemName(), otherItem.getProblemName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStepName(), otherItem.getStepName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getMaxProblemView(), otherItem.getMaxProblemView());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAvgIncorrects(), otherItem.getAvgIncorrects());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAvgHints(), otherItem.getAvgHints());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAvgCorrects(), otherItem.getAvgCorrects());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPctIncorrectFirstAttempts(),
                                otherItem.getPctIncorrectFirstAttempts());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPctHintFirstAttempts(),
                                otherItem.getPctHintFirstAttempts());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPctCorrectFirstAttempts(),
                                otherItem.getPctCorrectFirstAttempts());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAvgStepDuration(), otherItem.getAvgStepDuration());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAvgCorrectStepDuration(),
                                otherItem.getAvgCorrectStepDuration());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAvgErrorStepDuration(),
                                otherItem.getAvgErrorStepDuration());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTotalStudents(), otherItem.getTotalStudents());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTotalOpportunities(), otherItem.getTotalOpportunities());
        if (value != 0) { return value; }

        return 0;
    }
}