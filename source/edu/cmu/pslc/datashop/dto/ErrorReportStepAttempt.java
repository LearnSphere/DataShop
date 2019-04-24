/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.pslc.datashop.item.FeedbackItem;

/**
 * Holds the data associated with an error report for a given step.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10604 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-21 11:34:50 -0500 (Fri, 21 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportStepAttempt implements java.io.Serializable, Comparable {

    /** This seems silly, but we like constants. */
    private static final int MAGIC_ONE_HUNDRED = 100;

    /** The subgoal attempt id. */
    private Long attemptId;
    /** The display SAI. */
    private String displaySAI = "";
    /** The hover SAI. */
    private String hoverSAI = "";
    /** The number of students. */
    private Integer numStudents;
    /** The number of observations. */
    private Integer numObservations;
    /** The percentage of students. */
    private String percentage = "";
    /** The list of feedback items that students received. */
    private List feedbackItemList = new ArrayList();
    /** The string indicating whether the attempt was correct, incorrect, hint or unknown. */
    private String correctFlag = "";

    /** Default constructor. */
    public ErrorReportStepAttempt() {
    }

    /**
     * Returns the attemptId.
     * @return the attemptId
     */
    public Long getAttemptId() {
        return attemptId;
    }

    /**
     * Sets the attemptId.
     * @param attemptId The attemptId to set.
     */
    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    /**
     * Returns the display SAI.
     * @return the display SAI
     */
    public String getDisplaySAI() {
        return this.displaySAI;
    }

    /**
     * Sets the display SAI.
     * @param sai the selection, action, input string
     */
    public void setDisplaySAI(String sai) {
        this.displaySAI = sai;
    }

    /**
     * Returns the display SAI.
     * @return the display SAI
     */
    public String getHoverSAI() {
        return this.hoverSAI;
    }

    /**
     * Sets the display SAI.
     * @param sai the selection, action, input string
     */
    public void setHoverSAI(String sai) {
        this.hoverSAI = sai;
    }

    /**
     * Returns the number of students.
     * @return the number of students
     */
    public Integer getNumStudents() {
        return this.numStudents;
    }

    /**
     * Sets the number of students who made this attempt.
     * @param num the number of students who made this attempt
     */
    public void setNumStudents(Integer num) {
        this.numStudents = num;
    }

    /**
     * Returns the numObservations.
     * @return the numObservations
     */
    public Integer getNumObservations() {
        return numObservations;
    }

    /**
     * Sets the numObservations.
     * @param numObservations The numObservations to set.
     */
    public void setNumObservations(Integer numObservations) {
        this.numObservations = numObservations;
    }

    /**
     * Returns the percentage.
     * @return the percentage
     */
    public String getPercentage() {
        return this.percentage;
    }

    /**
     * Sets the percentage.
     * @param percentage the percentage of students who made this attempt
     */
    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    /**
     * Oh, how convenient.
     * @param total the total number of students who attempted this step
     */
    public void setPercentage(int total) {
        double percent = MAGIC_ONE_HUNDRED * (double)numObservations / (double)total;
        NumberFormat formatter = new DecimalFormat("###.##");
        this.percentage = formatter.format(percent) + "%";
    }

    /**
     * Returns the list of feedback items.
     * @return list of FeedbackItem objects
     */
    public List getFeedbackItemList() {
        return this.feedbackItemList;
    }

    /**
     * Adds feedback items to the list.
     * @param feedbackItem the feedback item to add to the list
     */
    public void addFeedback(FeedbackItem feedbackItem) {
        this.feedbackItemList.add(feedbackItem);
    }

    /**
     * Returns the correct flag.
     * @return the correct flat
     */
    public String getCorrectFlag() {
        return this.correctFlag;
    }

    /**
     * Sets the correct flag.
     * @param correctFlag the correct flag which is a string
     */
    public void setCorrectFlag(String correctFlag) {
        this.correctFlag = correctFlag;
    }

    /**
     * Compares two objects using the relevant attributes of this class.
     * <ul>
     * <li>correctFlag</li>
     * <li>numStudents (descending)</li>
     * <li>displaySAI</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ErrorReportStepAttempt otherObj = (ErrorReportStepAttempt)obj;
        int value = 0;

        value = objectCompareTo(correctFlag, otherObj.correctFlag);
        if (value != 0) { return value; }

        value = objectCompareTo(numStudents, otherObj.numStudents);
        if (value != 0) { return value * -1; }

        value = objectCompareTo(displaySAI, otherObj.displaySAI);
        if (value != 0) { return value; }

        return value;
    }

    /**
     * FIXME.  This method is copied from Item.java in the item package.
     *
     * Used to compare one attribute of a class, taking null into account.
     * @param one the first object to compare to
     * @param two the other object to compare to
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    private int objectCompareTo(Comparable one, Comparable two) {
        if ((one != null) && (two != null)) {
            return one.compareTo(two);
        } else if (one != null) {
            return 1;
        } else if (two != null) {
            return -1;
        }
        return 0;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append(" [");
         buffer.append("attemptId:" + getAttemptId());
         buffer.append(",correctFlag:" + getCorrectFlag());
         buffer.append(",displaySAI:" + getDisplaySAI());
         buffer.append(",hoverSAI:" + getHoverSAI());
         buffer.append(",numFeedbacItems:" + getFeedbackItemList().size());
         buffer.append(",numObservations:" + getNumObservations());
         buffer.append(",numStudents:" + getNumStudents());
         buffer.append(",percentage:" + getPercentage());
         buffer.append("]");

         return buffer.toString();
    }

}
