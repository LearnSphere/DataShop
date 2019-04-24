/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.ArrayList;
import java.util.List;
import static java.util.Arrays.asList;
import static edu.cmu.pslc.datashop.util.UtilConstants.MAGIC_1000;
import static edu.cmu.pslc.datashop.util.StringUtils.stripChars;

/**
 * Data transfer object to hold the information generated for a step export.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepExportRow extends DTO {

    /** Long of database step id */
    private Long stepId;
    /** String of the step GUID */
    private String stepGuid;
    /** The problem hierarchy as a string */
    private String problemHierarchy;
    /** The problem name */
    private String problemName;
    /** The name of the step */
    private String stepName;
    /** Average problem view for this step */
    private Double maxProblemView;
    /** Average number of incorrects for this step. */
    private Double avgIncorrects;
    /** Average number of hints for this step. */
    private Double avgHints;
    /** Average number of corrects for this step. */
    private Double avgCorrects;
    /** Percentage of incorrects on the first attempt for this step. */
    private Double pctIncorrectFirstAttempts;
    /** Percentage of hints on the first attempt for this step. */
    private Double pctHintFirstAttempts;
    /** Percentage of corrects on the first attempt for this step. */
    private Double pctCorrectFirstAttempts;
    /** Average Step Duration in seconds. */
    private Double avgStepDuration;
    /** Average Correct Step Duration in seconds. */
    private Double avgCorrectStepDuration;
    /** Average Error Step Duration in seconds. */
    private Double avgErrorStepDuration;
    /**  number of students that performed this step. */
    private Integer totalStudents;
    /**  opportunities at this step. */
    private Integer totalOpportunities;

    /** Problem rollup headers that are the same for every preview. */
    public static final List<String> STATIC_HEADERS = asList("Row", "Sample",
            "Anon Student Id", "Problem Hierarchy", "Problem Name",
            "Problem View", "Step Name", "Step Start Time", "First Transaction Time",
            "Correct Transaction Time", "Step End Time", "Step Duration (sec)",
            "Correct Step Duration (sec)", "Error Step Duration (sec)", "First Attempt",
            "Incorrects", "Hints", "Corrects", "Condition");

    /** Problem rollup headers that are the same for every export. */
    public static final List<String> STATIC_EXPORT_HEADERS = asList("Row", "Sample",
            "Anon Student Id", "Problem Hierarchy", "Problem Name",
            "Problem View", "Step Name", "Step Start Time", "First Transaction Time",
            "Correct Transaction Time", "Step End Time", "Step Duration (sec)",
            "Correct Step Duration (sec)", "Error Step Duration (sec)", "First Attempt",
            "Incorrects", "Hints", "Corrects", "Condition");

    /** Default Constructor. */
    public StepExportRow() { }

    /** Returns stepId. @return Returns the stepId. */
    public Long getStepId() {
        return stepId;
    }
    /** Set stepId. @param stepId The stepId to set. */
    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }
    /** Returns stepGuid. @return Returns the stepGuid. */
    public String getStepGuid() {
        return stepGuid;
    }
    /** Set stepGuid. @param stepGuid The stepGuid to set. */
    public void setStepGuid(String stepGuid) {
        this.stepGuid = stepGuid;
    }
    /** Returns problemHierarchy. @return Returns the problemHierarchy. */
    public String getProblemHierarchy() {
        return problemHierarchy;
    }
    /** Set problemHierarchy. @param problemHierarchy The problemHierarchy to set. */
    public void setProblemHierarchy(String problemHierarchy) {
        this.problemHierarchy = problemHierarchy;
    }
    /** Returns problemName. @return Returns the problemName. */
    public String getProblemName() {
        return problemName;
    }
    /** Set problemName. @param problemName The problemName to set. */
    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }
    /** Returns stepName. @return Returns the stepName. */
    public String getStepName() {
        return stepName;
    }
    /** Set stepName. @param stepName The stepName to set. */
    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
    /** Returns maxProblemView. @return Returns the maxProblemView. */
    public Double getMaxProblemView() {
        return maxProblemView;
    }
    /** Set maxProblemView. @param maxProblemView The maxProblemView to set. */
    public void setMaxProblemView(Double maxProblemView) {
        this.maxProblemView = maxProblemView;
    }
    /** Returns avgIncorrects. @return Returns the avgIncorrects. */
    public Double getAvgIncorrects() {
        return avgIncorrects;
    }
    /** Set avgIncorrects. @param avgIncorrects The avgIncorrects to set. */
    public void setAvgIncorrects(Double avgIncorrects) {
        this.avgIncorrects = avgIncorrects;
    }
    /** Returns avgHints. @return Returns the avgHints. */
    public Double getAvgHints() {
        return avgHints;
    }
    /** Set avgHints. @param avgHints The avgHints to set. */
    public void setAvgHints(Double avgHints) {
        this.avgHints = avgHints;
    }
    /** Returns avgCorrects. @return Returns the avgCorrects. */
    public Double getAvgCorrects() {
        return avgCorrects;
    }
    /** Set avgCorrects. @param avgCorrects The avgCorrects to set. */
    public void setAvgCorrects(Double avgCorrects) {
        this.avgCorrects = avgCorrects;
    }
    /** Returns pctIncorrectFirstAttempts. @return Returns the pctIncorrectFirstAttempts. */
    public Double getPctIncorrectFirstAttempts() {
        return pctIncorrectFirstAttempts;
    }
    /**
     * Set pctIncorrectFirstAttempts.
     * @param pctIncorrectFirstAttempts The pctIncorrectFirstAttempts to set.
     */
    public void setPctIncorrectFirstAttempts(Double pctIncorrectFirstAttempts) {
        this.pctIncorrectFirstAttempts = pctIncorrectFirstAttempts;
    }
    /** Returns pctHintFirstAttempts. @return Returns the pctHintFirstAttempts. */
    public Double getPctHintFirstAttempts() {
        return pctHintFirstAttempts;
    }
    /** Set pctHintFirstAttempts. @param pctHintFirstAttempts The pctHintFirstAttempts to set. */
    public void setPctHintFirstAttempts(Double pctHintFirstAttempts) {
        this.pctHintFirstAttempts = pctHintFirstAttempts;
    }
    /** Returns pctCorrectFirstAttempts. @return Returns the pctCorrectFirstAttempts. */
    public Double getPctCorrectFirstAttempts() {
        return pctCorrectFirstAttempts;
    }
    /**
     * Set pctCorrectFirstAttempts.
     * @param pctCorrectFirstAttempts The pctCorrectFirstAttempts to set.
     */
    public void setPctCorrectFirstAttempts(Double pctCorrectFirstAttempts) {
        this.pctCorrectFirstAttempts = pctCorrectFirstAttempts;
    }

    /** Returns avgStepDuration. @return Returns the avgStepDuration. */
    public Double getAvgStepDuration() {
        return avgStepDuration;
    }

    /** Set avgStepDuration.
     * @param avgStepDuration The avgStepDuration to set.
     */
    public void setAvgStepDuration(Double avgStepDuration) {
        if (avgStepDuration != null) {
            this.avgStepDuration = formatMe(avgStepDuration / MAGIC_1000);
        } else {
            this.avgStepDuration = avgStepDuration;
        }
    }

    /** Set avgStepDuration, without converting.
     * @param avgStepDuration The avgStepDuration to set.
     */
    public void setAvgStepDurationNoConvert(Double avgStepDuration) {
        this.avgStepDuration = avgStepDuration;
    }

    /** Returns avgCorrectStepDuration. @return Returns the avgCorrectStepDuration. */
    public Double getAvgCorrectStepDuration() {
        return avgCorrectStepDuration;
    }

    /** Set avgCorrectStepDuration.
     * @param avgCorrectStepDuration The avgCorrectStepDuration to set.
     */
    public void setAvgCorrectStepDuration(Double avgCorrectStepDuration) {
        if (avgCorrectStepDuration != null) {
            this.avgCorrectStepDuration = formatMe(avgCorrectStepDuration / MAGIC_1000);
        } else {
            this.avgCorrectStepDuration = avgCorrectStepDuration;
        }
    }

    /** Set avgCorrectStepDuration, without converting.
     * @param avgCorrectStepDuration The avgCorrectStepDuration to set.
     */
    public void setAvgCorrectStepDurationNoConvert(Double avgCorrectStepDuration) {
        this.avgCorrectStepDuration = avgCorrectStepDuration;
    }

    /**
     * Get the avgErrorStepDuration.
     * @return the avgErrorStepDuration
     */
    public Double getAvgErrorStepDuration() {
        return avgErrorStepDuration;
    }

    /**
     * Set the avgErrorStepDuration.
     * @param avgErrorStepDuration the avgErrorStepDuration to set
     */
    public void setAvgErrorStepDuration(Double avgErrorStepDuration) {
        if (avgErrorStepDuration != null) {
            this.avgErrorStepDuration = formatMe(avgErrorStepDuration / MAGIC_1000);
        } else {
            this.avgErrorStepDuration = avgErrorStepDuration;
        }
    }

    /**
     * Set the avgErrorStepDuration, without converting.
     * @param avgErrorStepDuration the avgErrorStepDuration to set
     */
    public void setAvgErrorStepDurationNoConvert(Double avgErrorStepDuration) {
        this.avgErrorStepDuration = avgErrorStepDuration;
    }

    /** Returns totalStudents. @return Returns the totalStudents. */
    public Integer getTotalStudents() {
        return totalStudents;
    }
    /** Set totalStudents. @param totalStudents The totalStudents to set. */
    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }
    /** Returns totalOpportunities. @return Returns the totalOpportunities. */
    public Integer getTotalOpportunities() {
        return totalOpportunities;
    }
    /** Set totalOpportunities. @param totalOpportunities The totalOpportunities to set. */
    public void setTotalOpportunities(Integer totalOpportunities) {
        this.totalOpportunities = totalOpportunities;
    }


    /**
     * Gets the fields in this row as a List of columns.
     * @return List of String for the columns.
     */
    public List <String> getColumns() {
        List <String> columns = new ArrayList <String> ();

        columns.add(displayObject(getStepGuid()));
        columns.add(displayObject(getProblemHierarchy()));
        columns.add(displayObject(getProblemName()));
        columns.add(displayObject(getMaxProblemView()));
        columns.add(displayObject(getStepName()));
        columns.add(displayObject(getAvgIncorrects()));
        columns.add(displayObject(getAvgHints()));
        columns.add(displayObject(getAvgCorrects()));
        columns.add(displayObject(getPctIncorrectFirstAttempts()));
        columns.add(displayObject(getPctHintFirstAttempts()));
        columns.add(displayObject(getPctCorrectFirstAttempts()));
        columns.add(displayObject(getAvgStepDuration()));
        columns.add(displayObject(getAvgCorrectStepDuration()));
        columns.add(displayObject(getAvgErrorStepDuration()));
        columns.add(displayObject(getTotalStudents()));
        columns.add(displayObject(getTotalOpportunities()));

        return columns;
    }

    /**
     * Helper function to properly display NULL items as blanks rather than NULL.
     * @param obj the object to display
     * @return String of the display.
     */
    private static String displayObject(Object obj) {
        if (obj == null) {
            return "";
        } else {
            return stripChars(obj.toString());
        }
    }
} // end StepExportRow.java
