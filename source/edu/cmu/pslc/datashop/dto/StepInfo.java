/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import static edu.cmu.pslc.datashop.util.StringUtils.join;

/**
 * Holds the step information needed for the Dataset Info Report.
 *
 * @author Alida Skogsholm
 * @version $Revision: 5886 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2009-11-13 14:15:23 -0500 (Fri, 13 Nov 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepInfo implements java.io.Serializable {
    /** The row number within the entire list. */
    private int row;
    /** String of the problem hierarchy */
    private String problemHierarchy;
    /** String of the problem name */
    private String problemName;
    /** String of the step name */
    private String stepName;

    /**
     * Constructor which takes the fields.
     * @param row the row number
     * @param problemHierarchy the problem hierarchy (dataset levels)
     * @param problemName the problem name
     * @param stepName the step name
     */
    public StepInfo(int row, String problemHierarchy, String problemName, String stepName) {
        this.row = row;
        this.problemHierarchy = problemHierarchy;
        this.problemName = problemName;
        this.stepName = stepName;
    }

    /**
     * Get the row.
     * @return Returns the row.
     */
    public String getRow() {
        return new Integer(row).toString();
    }

    /**
     * Get the problem hierarchy.
     * @return Returns the problemHierarchy.
     */
    public String getProblemHierarchy() {
        return problemHierarchy;
    }

    /**
     * Get the problem name.
     * @return Returns the problemName.
     */
    public String getProblemName() {
        return problemName;
    }

    /**
     * Get the step name.
     * @return Returns the stepName.
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    public String toString() {
        return join("\t", getRow(), getProblemHierarchy(), getProblemName(), getStepName());
    }
}
