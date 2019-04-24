/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import org.apache.log4j.Logger;


/**
 * Data object that contains the fields of the action_evaluation element
 * for tutor message for version 2 and 4.
 *
 * @author Hui cheng
 * @version $Revision: 4312 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2007-09-28 15:52:41 -0400 (Fri, 28 Sep 2007) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ActionEvaluation {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The current_hint_number. */
    private String currentHintNumber;
    /** The total_hints_available. */
    private String totalHintsAvailable;
    /** The hing_id. */
    private String hintId;
    /** The classification. */
    private String classification;
    /** The content of the action_evaluation element. */
    private String content;


    /** Default constructor. */
    public ActionEvaluation() {
    }

    /** The constructor that sets all fields.
     *  @param currentHintNumber String.
     *  @param totalHintsAvailable String.
     *  @param hintId String.
     *  @param classification String.
     *  @param content String.
     */
    public ActionEvaluation(String currentHintNumber,
                            String totalHintsAvailable,
                            String hintId,
                            String classification,
                            String content) {
        this.currentHintNumber = currentHintNumber;
        this.totalHintsAvailable = totalHintsAvailable;
        this.hintId = hintId;
        this.classification = classification;
        this.content = content;
    }

    /** The getter for currentHintNumber in Short.
     * @return The currentHintNumber in Short or null.
     */
    public Short getCurrentHintNumber () {
        Short currentHintNumberShort = null;
        if (currentHintNumber != null) {
            try {
                currentHintNumberShort = new Short(currentHintNumber);
            } catch (NumberFormatException exception) {
                logger.warn("Current hint number in action evaluation not a number: "
                        + currentHintNumber);
                logger.debug("Caught NumberFormatException returning null.", exception);
            }
        }
        return currentHintNumberShort;
    }

    /** The setter for currentHintNumber.
     * @param currentHintNumber String.
     */
    public void setCurrentHintNumber (String currentHintNumber) {
        this.currentHintNumber = currentHintNumber;
    }

    /** The getter for totalHintsAvailable in Short.
     * @return The totalHintsAvailable in Short.
     */
    public Short getTotalHintsAvailable () {
        Short totalHintsAvailableShort  = null;

        if (totalHintsAvailable != null) {
            try {
                totalHintsAvailableShort = new Short(totalHintsAvailable);
            } catch (NumberFormatException exception) {
                logger.warn("Total Hints Available in action evaluation not a number: "
                        + totalHintsAvailable);
                logger.debug("Caught NumberFormatException returning null.", exception);
            }
        }
        return totalHintsAvailableShort;
    }

    /** The setter for totalHintsAvailable.
     * @param totalHintsAvailable String.
     */
    public void setTotalHintsAvailable (String totalHintsAvailable) {
        this.totalHintsAvailable = totalHintsAvailable;
    }

    /** The getter for hintId.
     * @return The hintId.
     */
    public String getHintId () {
        return hintId;
    }

    /** The setter for hintId.
     * @param hintId String.
     */
    public void setHintId (String hintId) {
        this.hintId = hintId;
    }

    /** The getter for classification.
     * @return The classification.
     */
    public String getClassification () {
        return classification;
    }

    /** The setter for classification.
     * @param classification String.
     */
    public void setClassification (String classification) {
        this.classification = classification;
    }

    /** The getter for content.
     * @return The content.
     */
    public String getContent () {
        return content;
    }

    /** The setter for content.
     * @param content String.
     */
    public void setContent (String content) {
        this.content = content;
    }

}
