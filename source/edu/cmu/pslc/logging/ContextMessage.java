/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.logging.element.ClassElement;
import edu.cmu.pslc.logging.element.ConditionElement;
import edu.cmu.pslc.logging.element.CustomFieldElement;
import edu.cmu.pslc.logging.element.DatasetElement;
import edu.cmu.pslc.logging.element.MetaElement;
import edu.cmu.pslc.logging.element.SkillElement;
import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * The context message class.
 * @author Alida Skogsholm
 * @version $Revision: 9303 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-05-30 11:30:09 -0400 (Thu, 30 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class ContextMessage extends Message {

    /** . */
    private static final String START_PROBLEM = "START_PROBLEM";
    /** . */
    private static final String CONTINUE_PROBLEM = "CONTINUE_PROBLEM";
    /** . */
    private String nameAttribute = null;
    /** . */
    private ClassElement classElement = null;
    /** . */
    private List dfaList = new ArrayList();
    /** . */
    private List skillList = new ArrayList();
    /** . */
    private DatasetElement datasetElement = null;
    /** . */
    private List conditionList = new ArrayList();
    /** . */
    private List customFieldList = new ArrayList();

    /**
     * Creates a new start problem event.
     *
     * @param meta
     *            the meta element
     * @return the context message
     */
    public static ContextMessage createStartProblem(MetaElement meta) {
        return create(START_PROBLEM, meta);
    }

    /**
     * Creates new continue problem event.
     *
     * @param meta
     *            the meta element
     * @return the context message
     */
    public static ContextMessage createContinueProblem(MetaElement meta) {
        return create(CONTINUE_PROBLEM, meta);
    }

    /**
     * Static public method which creates a new context message.
     * @param name the name
     * @param meta the meta element
     * @return the context message
     */
    public static ContextMessage create(String name, MetaElement meta) {
        String contextMessageId = generateGUID("C"); // C for Context Message
        ContextMessage contextMessage = new ContextMessage(contextMessageId,
                name, meta);
        return contextMessage;
    }

    /**
     * Private constructor.
     * @param contextMessageId the context message id
     * @param name the name
     * @param meta the meta element
     */
    private ContextMessage(String contextMessageId, String name, MetaElement meta) {
        super(contextMessageId, meta);
        this.nameAttribute = name;
    }

    /**
     * Set class name.
     * @param name the class name
     */
    public void setClassName(String name) {
        if (classElement == null) {
            classElement = new ClassElement();
        }
        classElement.setName(name);
    }

    /**
     * Set school name.
     * @param school the school name
     */
    public void setSchool(String school) {
        if (classElement == null) {
            classElement = new ClassElement();
        }
        classElement.setSchool(school);
    }

    /**
     * Set period.
     * @param period the period
     */
    public void setPeriod(String period) {
        if (classElement == null) {
            classElement = new ClassElement();
        }
        classElement.setPeriod(period);
    }

    /**
     * Set description.
     * @param description the description
     */
    public void setClassDescription(String description) {
        if (classElement == null) {
            classElement = new ClassElement();
        }
        classElement.setDescription(description);
    }

    /**
     * Set instructor.
     * @param instructor the instructor
     */
    public void addInstructor(String instructor) {
        if (classElement == null) {
            classElement = new ClassElement();
        }
        classElement.addInstructor(instructor);
    }

    /**
     * Add DFA.
     * @param dfa the dfa
     */
    public void addDfa(String dfa) {
        this.dfaList.add(dfa);
    }

    /**
     * Add skill element.
     * @param skillElement the skill element
     */
    public void addSkill(SkillElement skillElement) {
        skillList.add(skillElement);
    }

    /**
     * Add dataset element.
     * @param datasetElement the dataset element
     */
    public void setDataset(DatasetElement datasetElement) {
        this.datasetElement = datasetElement;
    }

    /**
     * Add condition name.
     * @param conditionName the condition name
     */
    public void addCondition(String conditionName) {
        conditionList.add(new ConditionElement(conditionName));
    }

    /**
     * Add condition element.
     * @param conditionElement the condition element
     */
    public void addCondition(ConditionElement conditionElement) {
        conditionList.add(conditionElement);
    }

    /**
     * Add custom field.
     * @param name the name
     * @param value the value
     */
    public void addCustomField(String name, String value) {
        customFieldList.add(new CustomFieldElement(name, value));
    }

    /**
     * Add custom field.
     * @param customFieldElement the custom field element
     */
    public void addCustomField(CustomFieldElement customFieldElement) {
        customFieldList.add(customFieldElement);
    }

    /**
     * To string method which displays meta element.
     * @return the string to display
     */
    public String toString() {
        return toString(true);
    }

    /**
     * To string method.
     * @param logMetaFlag whether to display the meta element
     * @return the string to display
     */
    public String toString(boolean logMetaFlag) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<context_message context_message_id=\""
                + getContextMessageId() + "\"");
        if (nameAttribute != null && nameAttribute.length() > 0) {
            buffer.append(" name=\"");
            buffer.append(LogFormatUtils.escapeAttribute(nameAttribute));
            buffer.append("\"");
        }
        buffer.append(">\n");

        if (logMetaFlag) {
            buffer.append(getMetaElement());
        }
        if (classElement != null) {
            buffer.append(classElement.toString());
        }

        for (Iterator iter = dfaList.iterator(); iter.hasNext();) {
            String dfa = (String) iter.next();
            if (dfa != null && dfa.length() > 0) {
                buffer.append("\t<dfa>");
                buffer.append(LogFormatUtils.escapeElement(dfa));
                buffer.append("</dfa>\n");
            }
        }
        if (datasetElement != null) {
            buffer.append(datasetElement.toString());
        }
        for (Iterator iter = skillList.iterator(); iter.hasNext();) {
            SkillElement skillElement = (SkillElement) iter.next();
            if (skillElement != null) {
                buffer.append(skillElement);
            }
        }

        for (Iterator iter = conditionList.iterator(); iter.hasNext();) {
            ConditionElement conditionElement = (ConditionElement) iter.next();
            if (conditionElement != null) {
                buffer.append(conditionElement);
            }
        }

        for (Iterator iter = customFieldList.iterator(); iter.hasNext();) {
            CustomFieldElement customFieldElement = (CustomFieldElement) iter
                    .next();
            if (customFieldElement != null) {
                buffer.append(customFieldElement);
            }
        }

        buffer.append("</context_message>\n");
        return buffer.toString();

    }

    /**
     * Returns the name attribute.
     *
     * @return the nameAttribute
     */
    public String getNameAttribute() {
        return nameAttribute;
    }

    /**
     * Returns the class element.
     *
     * @return the classElement
     */
    public ClassElement getClassElement() {
        return classElement;
    }

    /**
     * Returns the DFA list.
     *
     * @return the dfaList
     */
    public List getDfaList() {
        return dfaList;
    }

    /**
     * Returns the skill list.
     *
     * @return the skillList
     */
    public List getSkillList() {
        return skillList;
    }

    /**
     * Returns the dataset element.
     *
     * @return the datasetElement
     */
    public DatasetElement getDatasetElement() {
        return datasetElement;
    }

    /**
     * Returns the condition list.
     *
     * @return the conditionList
     */
    public List getConditionList() {
        return conditionList;
    }

    /**
     * Returns the custom field list.
     *
     * @return the customFieldList
     */
    public List getCustomFieldList() {
        return customFieldList;
    }
}
