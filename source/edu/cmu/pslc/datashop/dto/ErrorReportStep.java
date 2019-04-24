/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Holds the data associated with an error report for a given step.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4822 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-05-08 16:22:38 -0400 (Thu, 08 May 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportStep implements java.io.Serializable, Comparable {

    /** The sample Id. */
    private Integer sampleId;
    /** The sample name. */
    private String sampleName;
    /** Step Id. */
    private Long stepId;
    /** Step Name. */
    private String stepName;
    /** The SAI to display. */
    private String displaySAI;
    /** The SAI to show as a tool tip over the display SAI. */
    private String hoverSAI;
    /** The number of unique students who attempted this step. */
    private int numStudents;
    /** The number of observations for this step. */
    private int numObservations;
    /** The list of KCs. */
    private String kcList;
    /** The list of skill names. */
    private Collection skillNames;
    /** The list of attempts at this step. */
    private List<ErrorReportStepAttempt> attemptList;

    /** Default constructor. */
    public ErrorReportStep() {
    }

    /**
     * Returns the sampleId.
     * @return the sampleId
     */
    public Integer getSampleId() {
        return sampleId;
    }

    /**
     * Sets the sampleId.
     * @param sampleId The sampleId to set.
     */
    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
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
     * Returns the step id.
     * @return the step id
     */
    public Long getStepId() {
        return this.stepId;
    }

    /**
     * Sets the step id.
     * @param id the step id
     */
    public void setStepId(Long id) {
        this.stepId = id;
    }

    /**
     * Returns the step name.
     * @return the step name
     */
    public String getStepName() {
        return this.stepName;
    }

    /**
     * Sets the step name.
     * @param name the step name
     */
    public void setStepName(String name) {
        this.stepName = name;
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
     * @param sai the selection/action/input for this step.
     */
    public void setDisplaySAI(String sai) {
        this.displaySAI = sai;
    }

    /**
     * Returns the hover SAI.
     * @return the hover SAI
     */
    public String getHoverSAI() {
        return this.hoverSAI;
    }

    /**
     * Sets the hover SAI.
     * @param sai the selection/action/input for this step.
     */
    public void setHoverSAI(String sai) {
        this.hoverSAI = sai;
    }

    /**
     * Returns the total number of students who attempted this step.
     * @return the number of students
     */
    public int getNumStudents() {
        return this.numStudents;
    }

    /**
     * Sets the total number of students who attempted this step.
     * @param num the number of students
     */
    public void setNumStudents(int num) {
        this.numStudents = num;
    }

    /**
     * Returns the skill names list.
     * @return the list of String objects with the skill names
     */
    public Collection getSkillNames() {
        return this.skillNames;
    }

    /**
     * Sets the skill names list.
     * @param list the list of String objects with the skill names
     */
    public void setSkillNames(Collection list) {
        this.skillNames = list;
    }

    /**
     * Returns the attempt list.
     * @return the list of ErrorReportStepAttempt objects
     */
    public Collection getAttemptList() {
        return this.attemptList;
    }

    /**
     * Returns the attempt list.
     * @return the list of ErrorReportStepAttempt objects
     */
    public List<ErrorReportStepAttempt> getSortedAttemptList() {
        List<ErrorReportStepAttempt> list = this.attemptList;
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }

    /**
     * Sets the attempt list.
     * @param list the list of ErrorReportStepAttempt objects
     */
    public void setAttemptList(List<ErrorReportStepAttempt> list) {
        this.attemptList = list;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>step name</li>
     * <li>sample name</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ErrorReportStep otherItem = (ErrorReportStep)obj;

        int value = 0;

        value = this.getStepName().compareTo(otherItem.getStepName());
        if (value != 0) { return value; }

        value = this.getSampleName().compareTo(otherItem.getSampleName());
        if (value != 0) { return value; }

        return value;
    }

    /**
     * Returns the kcList.
     * @return the kcList
     */
    public String getKcList() {
        return kcList;
    }

    /**
     * Sets the kcList.
     * @param kcList The kcList to set.
     */
    public void setKcList(String kcList) {
        this.kcList = kcList;
    }

    /**
     * Returns the numObservations.
     * @return the numObservations
     */
    public int getNumObservations() {
        return numObservations;
    }

    /**
     * Sets the numObservations.
     * @param numObservations The numObservations to set.
     */
    public void setNumObservations(int numObservations) {
        this.numObservations = numObservations;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append(" [");
         buffer.append("sampleId:" + getSampleId());
         buffer.append("sampleName:" + getSampleName());
         buffer.append("stepId:" + getStepId());
         buffer.append("stepName:" + getStepName());
         buffer.append("displaySAI:" + getDisplaySAI());
         buffer.append("hoverSAI:" + getHoverSAI());
         buffer.append("numStudents" + getNumStudents());
         buffer.append("numObservations" + getNumObservations());
         buffer.append("kcList" + getKcList());
         buffer.append("]");

         return buffer.toString();
    }

}
