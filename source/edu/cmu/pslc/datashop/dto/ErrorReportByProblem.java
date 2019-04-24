/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Holds the data associated with an error report by problem.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4822 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-05-08 16:22:38 -0400 (Thu, 08 May 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportByProblem implements java.io.Serializable {

    /** Problem Id. */
    private String problemId;
    /** Problem Name. */
    private String problemName;
    /** Problem Description. */
    private String problemDesc = null;
    /** The list of steps (subgoals). */
    private Collection<ErrorReportStep> stepList = new TreeSet();

    /** Default constructor. */
    public ErrorReportByProblem() {
    }

    /**
     * Returns the problem id.
     * @return the problem id
     */
    public String getProblemId() {
        return this.problemId;
    }

    /**
     * Sets the problem id.
     * @param id the problem id
     */
    public void setProblemId(String id) {
        this.problemId = id;
    }

    /**
     * Returns the problem name.
     * @return the problem name
     */
    public String getProblemName() {
        return this.problemName;
    }

    /**
     * Sets the problem name.
     * @param name the problem name
     */
    public void setProblemName(String name) {
        this.problemName = name;
    }

    /**
     * Returns the problem description.
     * @return the problem description
     */
    public String getProblemDesc() {
        return this.problemDesc;
    }

    /**
     * Sets the problem description.
     * @param description the problem description
     */
    public void setProblemDesc(String description) {
        this.problemDesc = description;
    }

    /**
     * Returns the list of steps.
     * @return the list of ErrorReportStep objects
     */
    public Collection getStepList() {
        return this.stepList;
    }

    /**
     * Sets the step list.
     * @param item ErrorReportStep object
     */
    public void addStep(ErrorReportStep item) {
        this.stepList.add(item);
    }
}
