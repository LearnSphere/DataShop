/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

/**
 * InterpretationElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InterpretationElement {

    /** Class attribute. */
    private Boolean chosenFlag = null;
    /** Class attribute. */
    private StepSequenceElement correctStepSequence = null;
    /** Class attribute. */
    private StepSequenceElement incorrectStepSequence = null;

    /**
     * Constructor.
     * @param chosenFlag chosenFlag
     * @param correctStepSequence correctStepSequence
     * @param incorrectStepSequence incorrectStepSequence
     */
    public InterpretationElement(Boolean chosenFlag,
            StepSequenceElement correctStepSequence, StepSequenceElement incorrectStepSequence) {
        this.chosenFlag = chosenFlag;
        this.correctStepSequence = correctStepSequence;
        this.incorrectStepSequence = incorrectStepSequence;
    }

    /**
     * Constructor.
     * @param chosenFlag chosenFlag
     * @param correctStepSequence correctStepSequence
     */
    public InterpretationElement(Boolean chosenFlag,
            StepSequenceElement correctStepSequence) {
        this.chosenFlag = chosenFlag;
        this.correctStepSequence = correctStepSequence;
    }

    /**
     * Gets chosenFlag.
     * @return chosenFlag
     */
    public Boolean getChosenFlag() {
        return chosenFlag;
    }
    /**
     * Sets chosenFlag.
     * @param chosenFlag chosenFlag
     */
    public void setChosenFlag(Boolean chosenFlag) {
        this.chosenFlag = chosenFlag;
    }
    /**
     * Gets correctStepSequence.
     * @return correctStepSequence
     */
    public StepSequenceElement getCorrectStepSequence() {
        return correctStepSequence;
    }
    /**
     * Sets correctStepSequence.
     * @param correctStepSequence correctStepSequence
     */
    public void setCorrectStepSequence(StepSequenceElement correctStepSequence) {
        this.correctStepSequence = correctStepSequence;
    }
    /**
     * Gets incorrectStepSequence.
     * @return incorrectStepSequence
     */
    public StepSequenceElement getIncorrectStepSequence() {
        return incorrectStepSequence;
    }
    /**
     * Sets incorrectStepSequence.
     * @param incorrectStepSequence incorrectStepSequence
     */
    public void setIncorrectStepSequence(StepSequenceElement incorrectStepSequence) {
        this.incorrectStepSequence = incorrectStepSequence;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<interpretation");
        if (chosenFlag != null) {
            buffer.append(" chosen=\"");
            buffer.append(chosenFlag);
            buffer.append("\"");
        }
        buffer.append(">\n");

        if (correctStepSequence != null) {
            buffer.append(correctStepSequence);
        }
        if (incorrectStepSequence != null) {
            buffer.append(incorrectStepSequence);
        }

        buffer.append("\t</interpretation>");
        buffer.append("\n");

        return buffer.toString();
    }
}
