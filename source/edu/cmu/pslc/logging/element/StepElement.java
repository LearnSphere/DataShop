/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.logging.util.LogFormatUtils;

public class StepElement {

    private String stepInfo = null; //required
    private List ruleList = new ArrayList();

    public StepElement(String stepInfo, List ruleList) {
        this.stepInfo = stepInfo;
        this.ruleList = ruleList;
    }
    public StepElement(String stepInfo) {
        this.stepInfo = stepInfo;
    }

    public String getStepInfo() {
        return stepInfo;
    }
    public List getRuleList() {
        return ruleList;
    }
    public void addRule(String rule) {
        this.ruleList.add(rule);
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t\t\t<step>\n");

        if (stepInfo != null && stepInfo.length() > 0) {
            buffer.append("\t\t\t\t<step_info>");
            buffer.append(LogFormatUtils.escapeElement(stepInfo));
            buffer.append("</step_info>\n");
        }

        for (Iterator iter = ruleList.iterator(); iter.hasNext();) {
            String rule = (String)iter.next();
            if (rule != null) {
                buffer.append("\t\t\t\t<rule>");
                buffer.append(LogFormatUtils.escapeElement(rule));
                buffer.append("</rule>\n");
            }
        }

        buffer.append("\t\t\t</step>\n");

        return buffer.toString();
    }
}
