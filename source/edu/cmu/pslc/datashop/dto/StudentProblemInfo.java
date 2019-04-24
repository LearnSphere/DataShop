/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static java.util.Arrays.asList;

import java.util.Date;
import java.util.List;

/**
 * Holds the student-problem information needed for the export task and helper.
 *
 * @author Mike Komisin
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemInfo implements java.io.Serializable {
    /** The row number within the entire list. */
    private int row;
    /** The sample name. */
    private String sample;
    /** The student anon_user_id. */
    private String student;
    /** The problem hierarchy. */
    private String problemHierarchy;
    /** The problem name. */
    private String problem;
    /** The problem view. */
    private int problemView;
    /** The problem start time. */
    private Date startTime;
    /** The problem end time. */
    private Date endTime;
    /** The latency. */
    private long latency;
    /** The number of missing start times. */
    private int numMissingStartTimes;
    /** The number of hints. */
    private int hints;
    /** The number of incorrect steps. */
    private int incorrects;
    /** The number of correct steps. */
    private int corrects;
    /** The number of corrects divided by number of steps for this problem. */
    private double avgCorrect;
    /** The number of steps. */
    private int steps;
    /** The average assistance score. */
    private double avgAssistance;
    /** The number of correct first attempts. */
    private int correctFirstAttempts;
    /** The comma-delimited string of conditions. */
    private String conditions;
    /** Total number of KCs for this problem. */
    private Integer numberOfKCs;
    /** Comma-separate list of skills for this problem. */
    private String kcList;
    /** Total number of steps in this problem without an assigned KC. */
    private Integer stepsWithoutKCs;
    /** Problem rollup headers that are the same for every sample. */
    public static final List<String> STATIC_HEADERS = asList("Row", "Sample", "Anon Student Id",
        "Problem Hierarchy", "Problem Name", "Problem View", "Problem Start Time",
        "Problem End Time", "Latency (sec)",
        "Steps Missing Start Times", "Hints", "Incorrects",
        "Corrects", "Avg Corrects",
        "Steps", "Avg Assistance Score", "Correct First Attempts", "Condition");
    /**
     * The StudentProblemInfo DTO is used for the student-problem export.
     * @param row the student-problem export row
     * @param sample the sample name
     * @param student the student anon_user_id
     * @param problemHierarchy the problem hierarchy
     * @param problem the problem name
     * @param problemView the problem view
     * @param startTime the problem start time
     * @param endTime the problem end time
     * @param latency the latency
     * @param numMissingStartTimes number of missing start times per problem
     * @param hints number of hints per problem
     * @param incorrects number of incorrect steps per problem
     * @param corrects number of correct steps per problem
     * @param avgCorrect average steps correct per problem
     * @param steps number of steps per problem
     * @param avgAssistance average assistance score
     * @param correctFirstAttempts correct first attempts
     * @param conditions comma-delimited string of conditions
     */
    public StudentProblemInfo(int row, String sample, String student,
            String problemHierarchy, String problem, int problemView,
            Date startTime, Date endTime, long latency, int numMissingStartTimes,
            int hints, int incorrects, int corrects, double avgCorrect,
            int steps, double avgAssistance, int correctFirstAttempts,
            String conditions) {
        this.row = row;
        this.sample = sample;
        this.student = student;
        this.problemHierarchy = problemHierarchy;
        this.problem = problem;
        this.problemView = problemView;
        this.startTime = startTime;
        this.endTime = endTime;
        this.latency = latency;
        this.numMissingStartTimes = numMissingStartTimes;
        this.hints = hints;
        this.incorrects = incorrects;
        this.corrects = corrects;
        this.avgCorrect = avgCorrect;
        this.steps = steps;
        this.avgAssistance = avgAssistance;
        this.correctFirstAttempts = correctFirstAttempts;
        this.conditions = conditions;
    }

    /**
     * Get the row.
     * @return Returns the row.
     */
    public String getRow() {
        return new Integer(row).toString();
    }

    /**
     * Get the sample name.
     * @return the sample
     */
    public String getSample() {
        return sample;
    }

    /**
     * Get the student anon_user_id.
     * @return the student anon_user_id
     */
    public String getStudent() {
        return student;
    }

    /**
     * Get the problem hierarchy.
     * @return the problemHierarchy
     */
    public String getProblemHierarchy() {
        return problemHierarchy;
    }

    /**
     * Get the problem name.
     * @return the problem
     */
    public String getProblem() {
        return problem;
    }

    /**
     * Get the problem view.
     * @return the problemView
     */
    public int getProblemView() {
        return problemView;
    }

    /**
     * Get the problem start time.
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Get the problem end time.
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Get the latency.
     * @return the latency
     */
    public long getLatency() {
        return latency;
    }

    /**
     * Get the number of missing start times.
     * @return the numMissingStartTimes
     */
    public int getNumMissingStartTimes() {
        return numMissingStartTimes;
    }

    /**
     * Get the number of hints.
     * @return the hints
     */
    public int getHints() {
        return hints;
    }

    /**
     * Get the number of incorrect steps.
     * @return the incorrects
     */
    public int getIncorrects() {
        return incorrects;
    }

    /**
     * Get the number of correct steps.
     * @return the corrects
     */
    public int getCorrects() {
        return corrects;
    }

    /**
     * Get the average correct.
     * @return the avgCorrect
     */
    public double getAvgCorrect() {
        return avgCorrect;
    }

    /**
     * Get the number of steps.
     * @return the steps
     */
    public int getSteps() {
        return steps;
    }

    /**
     * Get the average assistance score.
     * @return the avgAssistance
     */
    public double getAvgAssistance() {
        return avgAssistance;
    }

    /**
     * Get the number of correct first attempts.
     * @return the correctFirstAttempts
     */
    public int getCorrectFirstAttempts() {
        return correctFirstAttempts;
    }

    /**
     * Get the comma-delimited string of conditions.
     * @return the conditions
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * Total number of KCs for this problem.
     * @param numberOfKCs the numberOfKCs to set
     */
    public void setNumberOfKCs(Integer numberOfKCs) {
        this.numberOfKCs = numberOfKCs;
    }

    /**
     * Total number of KCs for this problem.
     * @return the numberOfKCs
     */
    public Integer getNumberOfKCs() {
        return numberOfKCs;
    }

    /**
     * Comma-separate list of skills for this problem.
     * @param kcList the kcList to set
     */
    public void setKcList(String kcList) {
        this.kcList = kcList;
    }

    /**
     * Comma-separate list of skills for this problem.
     * @return the kcList
     */
    public String getKcList() {
        return kcList;
    }

    /**
     * Total number of steps in this problem without an assigned KC.
     * @param stepsWithoutKcs the stepsWithoutKcs to set
     */
    public void setStepsWithoutKCs(Integer stepsWithoutKcs) {
        this.stepsWithoutKCs = stepsWithoutKcs;
    }

    /**
     * Total number of steps in this problem without an assigned KC.
     * @return the stepsWithoutKcs
     */
    public Integer getStepsWithoutKCs() {
        return stepsWithoutKCs;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer infoRow = new StringBuffer(join("\t", getRow(), getSample(),
                getStudent(), getProblemHierarchy(), getProblem(), getProblemView(),
                getStartTime(), getEndTime(), getLatency(),
                getNumMissingStartTimes(), getHints(), getIncorrects(), getCorrects(),
                getAvgCorrect(), getSteps(), getAvgAssistance(), getCorrectFirstAttempts(),
                getConditions()));

        if (stepsWithoutKCs != null && numberOfKCs != null && kcList != null) {
            infoRow.append(join("\t", getNumberOfKCs(), getStepsWithoutKCs(), getKcList()));
        } else if (kcList != null) {
            infoRow.append(join("\t", getNumberOfKCs(), getKcList()));
        }
        return infoRow.toString();
    }
}
