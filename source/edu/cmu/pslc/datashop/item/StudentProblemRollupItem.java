/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.UtilConstants;

import static edu.cmu.pslc.datashop.util.StringUtils.join;

/**
 * Abstract class for all items to ensure that the standard methods
 * such as equals, hashCode and compareTo are correct.
 *
 * @author Jim Rankin
 * @version $Revision: 9862 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-08-28 12:14:27 -0400 (Wed, 28 Aug 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemRollupItem implements Serializable, Comparable {
    /** Primary key for the student. */
    private Long studentId;
    /** Primary key for the step. */
    private Long stepId;
    /** Primary key for the problem. */
    private Long problemId;
    /** Name of the sample */
    private String sample;
    /** Anonymous student identifier */
    private String student;
    /** The name of the problem */
    private String problemName;
    /** Dataset Levels for the problem */
    private String problemHierarchy;
    /** The attempt number for the student at this problem */
    private Integer problemView;
    /** Total number of hints for this problem and student */
    private Integer hints;
    /** Total number of incorrects for this problem and student */
    private Integer incorrects;
    /** Total number of corrects for this problem and student */
    private Integer corrects;
    /** Condition names for this problem and student */
    private String conditions;
    /** Total number of steps the student took while working on the problem. */
    private Integer steps;
    /** (total hints + total incorrects) / total steps. */
    private Double avgAssistance;
    /** Number of correct first attempts */
    private Integer correctFirstAttempts;
    /** Total number of KCs for this problem. */
    private Integer numberOfKCs;
    /** Comma-separate list of skills for this problem. */
    private String kcList;
    /** Total number of steps in this problem without an assigned KC. */
    private Integer stepsWithoutKCs;
    /** Timestamp of when this problem was started. */
    private Date startTime;
    /** Timestamp of when this problem was completed. */
    private Date endTime;
    /** The number of step start times that were null. */
    private Integer numMissingStartTimes;
    /** Number format for latency, avg assistance, and avg correct. */
    public static final String NUMBER_FORMAT = "#########0.000";
    /** Formatter for problem latency and average correct. */
    private DecimalFormat format = new DecimalFormat(NUMBER_FORMAT);
    /** Magic One Thousand */
    private static final Integer MAGIC_ONE_THOUSAND = new Integer(1000);
    /** Format for the start and end times. Used in exports. */
    private static final FastDateFormat EXPORT_DATE_FORMAT
        = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Needed to implement interface comparable.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @param arg0 not used
     * @return 0
     */
    public int compareTo(Object arg0) {
        return 0;
    }

    /**
     * Name of the sample.
     * @param sampleName the sampleName to set
     */
    public void setSample(String sampleName) {
        this.sample = sampleName;
    }

    /**
     * Name of the sample.
     * @return the sampleName
     */
    public String getSampleName() {
        return sample;
    }

    /**
     * Anonymous student identifier.
     * @param studentName the studentName to set
     */
    public void setStudent(String studentName) {
        this.student = studentName;
    }

    /**
     * Anonymous student identifier.
     * @return the studentName
     */
    public String getStudent() {
        return student;
    }

    /**
     * The name of the problem.
     * @param problemName the problemName to set
     */
    public void setProblem(String problemName) {
        this.problemName = problemName;
    }

    /**
     * The name of the problem.
     * @return the problemName
     */
    public String getProblem() {
        return problemName;
    }

    /**
     * Dataset Levels for the problem.
     * @param problemHierarchy the problem hierarchy
     */
    public void setProblemHierarchy(String problemHierarchy) {
        this.problemHierarchy = problemHierarchy;
    }

    /**
     * Dataset Levels for the problem.
     * @return Dataset Levels for the problem.
     */
    public String getProblemHierarchy() {
        return problemHierarchy;
    }

    /** Returns problemView. @return Returns the problemView. */
    public Integer getProblemView() {
        return problemView;
    }

    /** Set problemView. @param problemView The problemView to set. */
    public void setProblemView(Integer problemView) {
        this.problemView = problemView;
    }

    /**
     * Total number of hints for this problem and student.
     * @param hints the hints to set
     */
    public void setHints(Integer hints) {
        this.hints = hints;
    }

    /**
     * Total number of hints for this problem and student.
     * @return the hints
     */
    public Integer getHints() {
        return hints;
    }

    /**
     * Total number of incorrects for this problem and student.
     * @param incorrects the incorrects to set
     */
    public void setIncorrects(Integer incorrects) {
        this.incorrects = incorrects;
    }

    /**
     * Total number of incorrects for this problem and student.
     * @return the incorrects
     */
    public Integer getIncorrects() {
        return this.incorrects;
    }

    /**
     * Total number of corrects for this problem and student.
     * @param corrects the corrects to set
     */
    public void setCorrects(Integer corrects) {
        this.corrects = corrects;
    }

    /**
     * Total number of corrects for this problem and student.
     * @return the corrects
     */
    public Integer getCorrects() {
        return corrects;
    }

    /**
     * Condition name for this problem and student.
     * @param conditions the condition name
     */
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    /**
     * Condition names for this problem and student.
     * @return the condition names
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * Number of correct first attempts.
     * @param correctFirstAttempts the correctFirstAttempts to set
     */
    public void setCorrectFirstAttempts(Integer correctFirstAttempts) {
        this.correctFirstAttempts = correctFirstAttempts;
    }

    /**
     * Number of correct first attempts.
     * @return the correctFirstAttempts
     */
    public Integer getCorrectFirstAttempts() {
        return correctFirstAttempts;
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
     * Total number of steps the student took while working on the problem.
     * @param steps the steps to set
     */
    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    /**
     * Total number of steps the student took while working on the problem.
     * @return the steps
     */
    public Integer getSteps() {
        return steps;
    }

    /**
     * (total hints + total incorrects) / total steps.
     * @param avgAssistance the avgAssistance to set
     */
    public void setAvgAssistance(Double avgAssistance) {
        this.avgAssistance = avgAssistance;
    }

    /**
     * (total hints + total incorrects) / total steps.
     * @return the avgAssistance
     */
    public Double getAvgAssistance() {
        return avgAssistance;
    }

    /**
     * Timestamp of when this problem was started.
     * @param startTime when this problem was started.
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Timestamp of when this problem was started.
     * @return when this problem was started.
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Formatted timestamp of when this problem was started.
     * @return when this problem was started.
     */
    public String getStartTimeStr() {
        if (startTime != null) {
            return EXPORT_DATE_FORMAT.format(startTime);
        } else {
            return "";
        }
    }

    /**
     * Timestamp of when this problem was completed.
     * @param endTime Timestamp of when this problem was completed.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Timestamp of when this problem was completed.
     * @return Timestamp of when this problem was completed.
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Formatted timestamp of when this problem was completed.
     * @return when this problem was completed
     */
    public String getEndTimeStr() {
        if (endTime != null) {
            return EXPORT_DATE_FORMAT.format(endTime);
        } else {
            return "";
        }
    }

    /** Returns numMissingStartTimes. @return Returns the numMissingStartTimes. */
    public Integer getNumMissingStartTimes() {
        return numMissingStartTimes;
    }

    /** Set numMissingStartTimes. @param numMissingStartTimes The numMissingStartTimes to set. */
    public void setNumMissingStartTimes(Integer numMissingStartTimes) {
        this.numMissingStartTimes = numMissingStartTimes;
    }

    /**
     * Primary key for the student.
     * @param studentId Primary key for the student.
     */
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    /**
     * Primary key for the student.
     * @return Primary key for the student.
     */
    public Long getStudentId() {
        return studentId;
    }

    /** Returns stepId. @return Returns the stepId. */
    public Long getStepId() {
        return stepId;
    }

    /** Set stepId. @param stepId The stepId to set. */
    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    /**
     * Primary key for the problem.
     * @param problemId Primary key for the problem.
     */
    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    /**
     * Primary key for the problem.
     * @return Primary key for the problem.
     */
    public Long getProblemId() {
        return problemId;
    }

    /**
     * Append two objects arrays.
     * @param arr1 first array
     * @param arr2 second array
     * @return the combined array
     */
    private static Object[] appendArrays(Object[] arr1, Object[] arr2) {
        Object[] combined = new Object[arr1.length + arr2.length];

        for (int i = 0; i < combined.length; i++) {
            combined[i] = i < arr1.length ? arr1[i] : arr2[i - arr1.length];
        }

        return combined;
    }

    /**
     * The amount of time the student spent on this problem.
     * @return The amount of time the student spent on this problem.
     */
    public String getProblemLatency() {
        if (getStartTime() == null || getEndTime() == null) {
            return null;
        }
        long latency =
            ((getEndTime().getTime() - getStartTime().getTime()) / MAGIC_ONE_THOUSAND);
        return format.format(latency);
    }

    /** The average number of correct responses. @return The average number of correct responses. */
    public String getAvgCorrect() {
        return (getCorrects() == null || getIncorrects() == null) ? null
                : format.format((double)getCorrects() / getSteps());
   }

    /**
     * Generate comma separated list of KCs from skillsMap.
     * @param skillsMap maps pair of student, problem IDs to list of skills (KCs)
     */
    public void updateKcList(MultiKeyMap skillsMap) {
        List skillNames = (List)skillsMap.get(getStudentId().longValue(),
                                              getProblemId().longValue());
        Collections.sort(skillNames);
        logger.debug("skillNames for " + getStudentId() + ", "
                     + getProblemId() + ": " + skillNames);
        setKcList(join(", ", skillNames));
        logger.debug("updated kcList: " + getKcList());
    }

    /** Export headers excluding KCs. */
    private static final String[] HEADERS = new String[] {
        "Row", "Sample", "Anon Student Id", "Problem Hierarchy", "Problem Name", "Problem View",
        "Problem Start Time", "Problem End Time", "Latency (sec)", "Steps Missing Start Times",
        "Hints", "Incorrects", "Corrects", "Avg Corrects",
        "Steps", "Avg Assistance Score", "Correct First Attempts", "Condition"
    };

    /** KC export headers. */
    private static final String[] KC_HEADERS = new String[] {
        "KCs", "Steps without KCs", "KC List"
    };

    /**
     * Headers for export.
     * @param includeKCs include KCs?
     * @return Headers for export.
     */
    public static List headers(boolean includeKCs) {
        List<String> headers = new ArrayList<String>();

        for (String header : HEADERS) {
            headers.add(header);
        }
        if (includeKCs) {
            for (String header : KC_HEADERS) {
                headers.add(header);
            }
        }

        return headers;
    }

    /**
     * Tab delimited list of headers.
     * @param includeKCs include KCs?
     * @return Tab delimited list of headers.
     */
    public static String joinHeaders(boolean includeKCs) {
        return join("\t", headers(includeKCs));
    }

    /**
     * Fields to include in export.
     * @param includeKCs include KCs?
     * @return Fields to include in export.
     */
    public Object[] exportFields(boolean includeKCs) {
        Object[] fields = new Object[] {
            getSampleName(), getStudent(), getProblemHierarchy(), getProblem(), getProblemView(),
            getStartTimeStr(), getEndTimeStr(), getProblemLatency(), getNumMissingStartTimes(),
            getHints(), getIncorrects(), getCorrects(), getAvgCorrect(),
            getSteps(), getAvgAssistance(), getCorrectFirstAttempts(), getConditions()
        };
        if (includeKCs) {
            Object[] kcFields = new Object[] {
                getNumberOfKCs(), getStepsWithoutKCs(), getKcList()
            };
            return appendArrays(fields, kcFields);
        }
        return fields;
    }

    /**
     * Tab delimited list of export fields.
     * @param includeKCs include KCs?
     * @return Tab delimited list of export fields.
     */
    public String tabDelimited(boolean includeKCs) {
        return join("\t", Arrays.asList(exportFields(includeKCs)));

    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj instanceof StudentProblemRollupItem) {
            StudentProblemRollupItem otherItem = (StudentProblemRollupItem)obj;

            //hierarchy
            if (!objectEquals(this.getSampleName(), otherItem.getSampleName())) {
                return false;
            }
            if (!objectEquals(this.getStudent(), otherItem.getStudent())) {
                return false;
            }
            if (!objectEquals(this.getProblemHierarchy(), otherItem.getProblemHierarchy())) {
                return false;
            }
            if (!objectEquals(this.getProblem(), otherItem.getProblem())) {
                return false;
            }
            if (!objectEquals(this.getProblemView(), otherItem.getProblemView())) {
                return false;
            }

            //corrects, hints, incorrects.
            if (!objectEquals(this.getCorrects(), otherItem.getCorrects())) {
                return false;
            }
            if (!objectEquals(this.getIncorrects(), otherItem.getIncorrects())) {
                return false;
            }
            if (!objectEquals(this.getHints(), otherItem.getHints())) {
                return false;
            }
            if (!objectEquals(this.getCorrectFirstAttempts(),
                    otherItem.getCorrectFirstAttempts())) {
                return false;
            }
            if (!objectEquals(this.getConditions(),
                    otherItem.getConditions())) {
                return false;
            }
            if (!objectEquals(this.getAvgAssistance(), otherItem.getAvgAssistance())) {
                return false;
            }

            //times
            if (!objectEquals(this.getStartTime(), otherItem.getStartTime())) {
                return false;
            }
            if (!objectEquals(this.getEndTime(), otherItem.getEndTime())) {
                return false;
            }

            //skill information
            if (!objectEquals(this.getNumberOfKCs(), otherItem.getNumberOfKCs())) {
                return false;
            }
            if (!objectEquals(this.getStepsWithoutKCs(), otherItem.getStepsWithoutKCs())) {
                return false;
            }
            if (!objectEquals(this.getKcList(), otherItem.getKcList())) {
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
       hash = hash * UtilConstants.HASH_PRIME + this.getSampleName().hashCode();
       return (int)(hash % Integer.MAX_VALUE);
   }

    /**
     * Determines whether another object is equal to another,
     * taking null into account.
     * @param one the first object to compare to
     * @param two the other object to compare to
     * @return true if the items are equal, false otherwise
     */
    private boolean objectEquals(Comparable one, Comparable two) {
        if (one != null && two != null) {
            if (one.compareTo(two) != 0) {
                return false;
            }
        } else if (two != null) {
            return false;
        } else if (one != null) {
            return false;
        }
        return true;
    }

    /**
     * Tab delimited list of export fields, KCs included.
     * @return Tab delimited list of export fields, KCs included.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        List headers = headers(true);
        Object[] fields = exportFields(true);
        for (int i = 1, n = headers.size(); i < n; i++) {
            buffer.append(headers.get(i) + "='" + fields[i - 1] + "' ");
        }
        return buffer.toString();
    }
}
