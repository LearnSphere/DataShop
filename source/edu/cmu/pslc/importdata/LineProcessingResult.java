/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.importdata;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds processing results for a line in the file undergoing verification.
 * @author kcunning
 * @version $Revision: 4423 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2007-11-07 11:09:23 -0500 (Wed, 07 Nov 2007) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LineProcessingResult {

    /** The line number currently being processed. */
    private Integer lineNumber;

    /** Errors found while processing. */
    private Set errors;

    /** Warnings found while processing. */
    private Set warnings;

    /** Info messages from processing. */
    private Set info;

    /** Constructor. */
    public LineProcessingResult() {
        errors = new HashSet();
        warnings = new HashSet();
        info =  new HashSet();
    };

    /* --------------------------------------
     * Getters, Setters and Adders
     * ------------------------------------*/
    /**
     * Gets the errors.
     * @return the errors
     */
    public Set getErrors() {
        return errors;
    }

    /**
     * Sets the errors.
     * @param errors the errors to set
     */
    public void setErrors(Set errors) {
        this.errors = errors;
    }

    /**
     * Adds an error to the set of errors.
     * @param error - the error to add
     */
    public void addToErrors(String error) {
        this.errors.add(error);
    }

    /**
     * Gets the line number.
     * @return the lineNumber
     */
    public Integer getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the line number.
     * @param lineNumber the lineNumber to set
     */
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Gets the warnings.
     * @return the warnings
     */
    public Set getWarnings() {
        return warnings;
    }

    /**
     * Sets the warnings.
     * @param warnings the warnings to set
     */
    public void setWarnings(Set warnings) {
        this.warnings = warnings;
    }

    /**
     * Adds a warning to the set of warnings.
     * @param warning - the warning to add
     */
    public void addToWarnings(String warning) {
        this.warnings.add(warning);
    }

    /**
     * Gets the set of info messages.
     * @return the set of info messages
     */
    public Set getInfo() {
        return info;
    }

    /**
     * Sets the info messages.
     * @param info the set of info messages
     */
    public void setInfo(Set info) {
        this.info = info;
    }

    /**
     * Adds an info message to the set of info messages.
     * @param info - the info message to add
     */
    public void addToInfo(String info) {
        this.info.add(info);
    }
} // end LineProcessingResult.java
