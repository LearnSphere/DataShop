/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.util.ArrayList;

import edu.cmu.pslc.datashop.item.CognitiveStepItem;

/**
 * Data object that contains all fields for the XML Interpretation element.
 *
 * @author Hui Cheng
 * @version $Revision: 2152 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2006-01-20 13:56:50 -0500 (Fri, 20 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */

public class Interpretation {
    /** The chosen.*/
    private Boolean chosen;
    /** ArrayList of steps that are in correct_step_sequence */
    private ArrayList correctSteps;
    /** ArrayList of steps that are in incorrect_step_sequence */
    private ArrayList incorrectSteps;


    /** Default constructor. */
    public Interpretation() {
        this.correctSteps = new ArrayList();
        this.incorrectSteps = new ArrayList();
    }

    /** The getter for chosen.
     * @return The eventId.
     */
    public Boolean getChosen () {
        return chosen;
    }

    /** The setter for chosen.
     * @param chosen Boolean.
     */
    public void setChosen (Boolean chosen) {
        this.chosen = chosen;
    }

    /**
     * Get correctSteps.
     * @return java.util.ArrayList
     */
    public ArrayList getCorrectSteps() {
        return this.correctSteps;
    }

    /**
     * Set correctSteps.
     * @param correctSteps ArrayList of CognitiveStepItem associated with the correct sequence.
     */
    protected void setCorrectSteps(ArrayList correctSteps) {
        this.correctSteps = correctSteps;
    }


    /**
     * Add a CognitiveStepItem to the correct sequence.
     * @param item to add
     */
    public void addStepToCorrectSeq(CognitiveStepItem item) {
        if (!getCorrectSteps().contains(item)) {
            getCorrectSteps().add(item);
        }
    }

    /**
     * Get incorrectSteps.
     * @return java.util.ArrayList
     */
    public ArrayList getIncorrectSteps() {
        return this.incorrectSteps;
    }

    /**
     * Set incorrectSteps.
     * @param incorrectSteps ArrayList of CognitiveStepItem associated with the incorrect sequence.
     */
    protected void setIncorrectSteps(ArrayList incorrectSteps) {
        this.incorrectSteps = incorrectSteps;
    }


    /**
     * Add a CognitiveStepItem to incorrect sequence.
     * @param item to add
     */
    public void addStepToIncorrectSeq(CognitiveStepItem item) {
        if (!getIncorrectSteps().contains(item)) {
            getIncorrectSteps().add(item);
        }
    }

}