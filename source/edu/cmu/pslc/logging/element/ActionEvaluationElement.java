/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

public class ActionEvaluationElement {

    public static final String CORRECT = "CORRECT";
    public static final String INCORRECT = "INCORRECT";
    public static final String HINT = "HINT";

    private String currentHintNumber = null;
    private String totalHintsAvailable = null;
    private String hintId = null;
    private String classification = null;
    private String evaluation = null;

    public static ActionEvaluationElement createCorrect() {
        return new ActionEvaluationElement(CORRECT);
    }
    public static ActionEvaluationElement createIncorrect() {
        return new ActionEvaluationElement(INCORRECT);
    }
    public static ActionEvaluationElement createHint() {
        return new ActionEvaluationElement(HINT);
    }

    public ActionEvaluationElement(String evaluation) {
        this.evaluation = evaluation;
    }
    public ActionEvaluationElement(String currentHintNumber, String totalHintsAvailable,
            String hintId, String classification, String evaluation) {
        this.currentHintNumber = currentHintNumber;
        this.totalHintsAvailable = totalHintsAvailable;
        this.hintId = hintId;
        this.classification = classification;
        this.evaluation = evaluation;
    }

    public String getClassification() {
        return classification;
    }
    public void setClassification(String classification) {
        this.classification = classification;
    }
    public String getCurrentHintNumber() {
        return currentHintNumber;
    }
    public void setCurrentHintNumber(String currentHintNumber) {
        this.currentHintNumber = currentHintNumber;
    }
    public String getEvaluation() {
        return evaluation;
    }
    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }
    public String getHintId() {
        return hintId;
    }
    public void setHintId(String hintId) {
        this.hintId = hintId;
    }
    public String getTotalHintsAvailable() {
        return totalHintsAvailable;
    }
    public void setTotalHintsAvailable(String totalHintsAvailable) {
        this.totalHintsAvailable = totalHintsAvailable;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<action_evaluation");

        if (currentHintNumber != null) {
            buffer.append(" current_hint_number=\"");
            buffer.append(LogFormatUtils.escapeAttribute(currentHintNumber));
            buffer.append("\"");
        }

        if (totalHintsAvailable != null) {
            buffer.append(" total_hints_available=\"");
            buffer.append(LogFormatUtils.escapeAttribute(totalHintsAvailable));
            buffer.append("\"");
        }

        if (hintId != null) {
            buffer.append(" hint_id=\"");
            buffer.append(LogFormatUtils.escapeAttribute(hintId));
            buffer.append("\"");
        }

        if (classification != null) {
            buffer.append(" classification=\"");
            buffer.append(LogFormatUtils.escapeAttribute(classification));
            buffer.append("\"");
        }

        buffer.append(">");

        buffer.append(LogFormatUtils.escapeElement(evaluation));
        buffer.append("</action_evaluation>\n");

        return buffer.toString();
    }
}
