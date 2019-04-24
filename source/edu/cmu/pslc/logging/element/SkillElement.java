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

public class SkillElement {

    private String probability = null;
    private String name = null; //required
    private String category = null; //required
    private List modelNameList = new ArrayList();
    private boolean buggyFlag = false;

    public SkillElement(String probability, String name, String category,
            String modelName, boolean buggyFlag) {
        this.probability = probability;
        this.name = name;
        this.category = category;
        this.addModelName(modelName);
        this.buggyFlag = buggyFlag;
    }
    public SkillElement(String probability, String name, String category, String modelName) {
        this.probability = probability;
        this.name = name;
        this.category = category;
        this.addModelName(modelName);
    }
    public SkillElement(String name, String category, String modelName, boolean buggyFlag) {
        this.name = name;
        this.category = category;
        this.addModelName(modelName);
        this.buggyFlag = buggyFlag;
    }
    public SkillElement(String name, String category, String modelName) {
        this.name = name;
        this.category = category;
        this.addModelName(modelName);
    }
    public SkillElement(String name, String category, boolean buggyFlag) {
        this.name = name;
        this.category = category;
        this.buggyFlag = buggyFlag;
    }
    public SkillElement(String name, String category) {
        this.name = name;
        this.category = category;
    }
    public String getProbability() {
        return probability;
    }
    public void setProbability(String probability) {
        this.probability = probability;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public List getModelNameList() {
        return modelNameList;
    }
    public void addModelName(String modelName) {
        if (modelName != null && modelName.length() > 0) {
            this.modelNameList.add(modelName);
        }
    }
    public boolean getBuggyFlag() {
        return buggyFlag;
    }
    public void setBuggyFlag(boolean buggyFlag) {
        this.buggyFlag = buggyFlag;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<skill");
        if (probability != null) {
            buffer.append(" probability=\"");
            buffer.append(LogFormatUtils.escapeAttribute(probability));
            buffer.append("\"");
        }
        if (buggyFlag) {
            buffer.append(" buggy=\"");
            buffer.append(buggyFlag);
            buffer.append("\"");
        }
        buffer.append(">\n");

        if (name != null && name.length() > 0) {
            buffer.append("\t\t<name>");
            buffer.append(LogFormatUtils.escapeElement(name));
            buffer.append("</name>\n");
        }
        if (category != null && category.length() > 0) {
            buffer.append("\t\t<category>");
            buffer.append(LogFormatUtils.escapeElement(category));
            buffer.append("</category>\n");
        }

        for (Iterator iter = modelNameList.iterator(); iter.hasNext();) {
            String modelName = (String)iter.next();
            if (modelName != null && modelName.length() > 0) {
                buffer.append("\t\t<model_name>");
                buffer.append(LogFormatUtils.escapeElement(modelName));
                buffer.append("</model_name>\n");
            }
        }

        buffer.append("\t</skill>");
        buffer.append("\n");

        return buffer.toString();
    }
}
