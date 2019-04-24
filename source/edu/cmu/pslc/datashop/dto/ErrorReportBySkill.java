/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Holds the data associated with an error report by knowledge component, aggregated.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10940 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-04-25 12:59:47 -0400 (Fri, 25 Apr 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportBySkill implements java.io.Serializable, Comparable {

    /** This seems silly, but we like constants. */
    private static final int MAGIC_ONE_HUNDRED = 100;

    /** The sample name. */
    private String sampleName;

    /** The skill name. */
    private String skillName;

    /** The number of students who attempted this skill. */
    private int numStudentsTotal;

    /** The number of students who attempted this skill. */
    private int numObsTotal;

    /** The number of observations who got it right. */
    private int numObsCorrect = 0;

    /** The number of observations who got it wrong. */
    private int numObsIncorrect = 0;

    /** The number of observations who asked for a hint. */
    private int numObsHint = 0;

    /** The number of observations who we don't know what they did. */
    private int numObsUnknown = 0;

    /** The list of problem names. */
    private String problemNameList;

    /** The list of problem ids. */
    private String problemIdList;

    /** Constant for the separator used in problemName and problemId lists. */
    public static final String SEPARATOR = "<br>";

    /** Default constructor. */
    public ErrorReportBySkill() {
    }

    /**
     * Returns the sample name.
     * @return the sample name
     */
    public String getSampleName() {
        return this.sampleName;
    }

    /**
     * Sets the sample name.
     * @param name the sample name
     */
    public void setSampleName(String name) {
        this.sampleName = name;
    }

    /**
     * Sets the skill name.
     * @param skillName the skill name
     */
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    /**
     * Returns the skill name.
     * @return the skill name
     */
    public String getSkillName() {
        return this.skillName;
    }

    /**
     * Returns the total number of unique students.
     * @return the total number of unique students
     */
    public int getNumStudentsTotal() {
        return this.numStudentsTotal;
    }

    /**
     * Sets the total number of unique students.
     * @param total the total number of unique students
     */
    public void setNumStudentsTotal(int total) {
        this.numStudentsTotal = total;
    }

    /**
     * Returns the total number of observations.
     * @return the total number of observations
     */
    public int getNumObsTotal() {
        return this.numObsTotal;
    }

    /**
     * Sets the total number of observations.
     * @param total the total number of observations
     */
    public void setNumObsTotal(int total) {
        this.numObsTotal = total;
    }

    /**
     * Sets the number of students - correct.
     * @param number the number of students - correct
     */
    public void setNumObsCorrect(int number) {
        this.numObsCorrect = number;
    }

    /**
     * Returns the number of observations - correct.
     * @return the number of observations - correct
     */
    public int getNumObsCorrect() {
        return this.numObsCorrect;
    }

    /**
     * Sets the number of observations - incorrect.
     * @param number the number of observations - incorrect
     */
    public void setNumObsIncorrect(int number) {
        this.numObsIncorrect = number;
    }

    /**
     * Returns the number of observations - incorrect.
     * @return the number of observations - incorrect
     */
    public int getNumObsIncorrect() {
        return this.numObsIncorrect;
    }

    /**
     * Sets the number of observations - hint.
     * @param number the number of observations - hint
     */
    public void setNumObsHint(int number) {
        this.numObsHint = number;
    }

    /**
     * Returns the number of observations - hint.
     * @return the number of observations - hint
     */
    public int getNumObsHint() {
        return this.numObsHint;
    }

    /**
     * Sets the number of observations - Unknown.
     * @param number the number of observations - Unknown
     */
    public void setNumObsUnknown(int number) {
        this.numObsUnknown = number;
    }

    /**
     * Returns the number of observations - Unknown.
     * @return the number of observations - Unknown
     */
    public int getNumObsUnknown() {
        return this.numObsUnknown;
    }

    /**
     * Returns the list of problem names.
     * @return the list of problem names
     */
    public String getProblemNameList() {
        return this.problemNameList;
    }

    /**
     * Sets the problem name list.
     * @param problemNameList list of problem names
     */
    public void setProblemNameList(String problemNameList) {
        this.problemNameList = problemNameList;
    }

    /**
     * Returns the list of problem ids.
     * @return the list of problem ids
     */
    public String getProblemIdList() {
        return this.problemIdList;
    }

    /**
     * Sets the problem id list.
     * @param problemIdList list of problem ids
     */
    public void setProblemIdList(String problemIdList) {
        this.problemIdList = problemIdList;
    }

    /**
     * Get the percentage of students who got it right.
     * @return a string
     */
    public String getCorrectPercentage() {
        return getPercentage(numObsTotal, numObsCorrect);
    }

    /**
     * Get the percentage of students who got it wrong.
     * @return a string
     */
    public String getIncorrectPercentage() {
        return getPercentage(numObsTotal, numObsIncorrect);
    }

    /**
     * Get the percentage of students who asked for a hint.
     * @return a string
     */
    public String getHintPercentage() {
        return getPercentage(numObsTotal, numObsHint);
    }

    /**
     * Get the percentage of students who did something else.
     * @return a string
     */
    public String getUnknownPercentage() {
        return getPercentage(numObsTotal, numObsUnknown);
    }

    /**
     * Oh, how convenient.
     * @param total the total number of students or observations
     * @param num the number of students or observations
     * @return a string of the percentage including the percent sign
     */
    private String getPercentage(int total, int num) {
        double percent = MAGIC_ONE_HUNDRED * (double)num / (double)total;
        NumberFormat formatter = new DecimalFormat("###.##");
        return formatter.format(percent) + "%";
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    public String toString() {
        return "sample[" + sampleName + "]"
        + " skill[" + skillName + "]"
        + " correct[" + numObsCorrect + " (" + getCorrectPercentage() + ")]"
        + " hint[" + numObsHint + " (" + getHintPercentage() + ")]"
        + " incorrect[" + numObsIncorrect + " (" + getIncorrectPercentage() + ")]"
        + " unknown[" + numObsUnknown + " (" + getUnknownPercentage() + ")]";
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>knowledge component name</li>
     * <li>sample name</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ErrorReportBySkill otherItem = (ErrorReportBySkill)obj;

        int value = 0;

        value = this.getSkillName().compareTo(otherItem.getSkillName());
        if (value != 0) { return value; }

        value = this.getSampleName().compareTo(otherItem.getSampleName());
        if (value != 0) { return value; }

        return value;
    }
}
