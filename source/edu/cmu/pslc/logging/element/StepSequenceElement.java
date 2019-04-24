/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StepSequenceElement {

    private Boolean correctFlag = Boolean.TRUE;
    private Boolean orderedFlag = Boolean.FALSE;
    private List stepList = new ArrayList();

    public static StepSequenceElement createCorrectSequence() {
        return new StepSequenceElement(Boolean.TRUE);
    }
    public static StepSequenceElement createIncorrectSequence() {
        return new StepSequenceElement(Boolean.FALSE);
    }

    public StepSequenceElement(Boolean correctFlag) {
        this.correctFlag = correctFlag;
    }
    public StepSequenceElement(Boolean correctFlag, List stepList) {
        this.correctFlag = correctFlag;
        this.stepList = stepList;
    }

    public Boolean getCorrectFlag() {
        return correctFlag;
    }
    public void setCorrectFlag(Boolean correctFlag) {
        this.correctFlag = correctFlag;
    }
    public Boolean getOrderedFlag() {
        return orderedFlag;
    }
    public void setOrderedFlag(Boolean orderedFlag) {
        this.orderedFlag = orderedFlag;
    }

    public List getStepList() {
        return stepList;
    }
    public void addStep(StepElement stepElement) {
        this.stepList.add(stepElement);
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        String elementName = null;
        if (correctFlag.booleanValue()) {
            elementName = "correct_step_sequence";
        } else {
            elementName = "incorrect_step_sequence";
        }
        StringBuffer buffer = new StringBuffer("\t\t<" + elementName);
        if (orderedFlag.booleanValue()) {
            buffer.append(" ordered=\"true\"");
        }
        buffer.append(">\n");

        for (Iterator iter = stepList.iterator(); iter.hasNext();) {
            StepElement step = (StepElement)iter.next();
            if (step != null) {
                buffer.append(step);
            }
        }

        buffer.append("\t\t</");
        buffer.append(elementName);
        buffer.append(">\n");

        return buffer.toString();
    }

}
