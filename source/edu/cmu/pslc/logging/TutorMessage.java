/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.logging.element.ActionEvaluationElement;
import edu.cmu.pslc.logging.element.CustomFieldElement;
import edu.cmu.pslc.logging.element.EventDescriptorElement;
import edu.cmu.pslc.logging.element.EventElement;
import edu.cmu.pslc.logging.element.InterpretationElement;
import edu.cmu.pslc.logging.element.SemanticEventElement;
import edu.cmu.pslc.logging.element.SkillElement;
import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * The tutor message represents a logging element that
 * originates from the tutor.
 * @author Alida Skogsholm
 * @version $Revision: 9303 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-05-30 11:30:09 -0400 (Thu, 30 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class TutorMessage extends Message {
    /** The tool message. */
    private ToolMessage toolMessage = null;
    /** The problem name. */
    private String problemName = null;
    /** The replay text. */
    private String replayText = null;
    /** The event element. */
    private EventElement eventElement = null; // required
    /** The event descriptor element. */
    private EventDescriptorElement eventDescriptorElement = null;
    /** The action evaluation element. */
    private ActionEvaluationElement actionEvaluationElement = null;
    /** The tutor advice list. */
    private List tutorAdviceList = new ArrayList();
    /** The skill list. */
    private List skillList = new ArrayList();
    /** The interpretation list. */
    private List interpretationList = new ArrayList();
    /** The custom fields list. */
    private List customFieldList = new ArrayList();
    /**
     * Creates a tutor message given a tool message.
     * @param toolMessage the tool message
     * @return the new tutor message
     */
    public static TutorMessage create(ToolMessage toolMessage) {
        TutorMessage tutorMessage = new TutorMessage(toolMessage);
        return tutorMessage;
    }
    /**
     * Private constructor (creates a new tutor message).
     * @param toolMessage a tool message
     */
    private TutorMessage(ToolMessage toolMessage) {
        super(toolMessage.getContextMessageId(), toolMessage.getMetaElement());
        this.toolMessage = toolMessage;
    }
    /**
     * Get problem name.
     * @return the problem name
     */
    public String getProblemName() {
        return problemName;
    }
    /**
     * Set problem name.
     * @param problemName the problem name
     */
    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }
    /**
     * Get event element.
     * @return the event element
     */
    public EventElement getEventElement() {
        return eventElement;
    }
    /**
     * Set event element.
     * @param eventElement the event element
     */
    public void setEventElement(EventElement eventElement) {
        this.eventElement = eventElement;
    }
    /**
     * Set tool message.
     * @param toolMessage the tool message
     */
    public void setToolMessage(ToolMessage toolMessage) {
        this.toolMessage = toolMessage;
        if (eventElement != null) {
            eventElement.setId(toolMessage.getEventElement().getId());
        }
    }
    /**
     * Set action evaluation element to correct.
     */
    public void setAsCorrectAttemptResponse() {
        setAsCorrectAttemptResponse(null);
    }
    /**
     * Set action evaluation element to correct given a subtype.
     * @param subtype the subtype
     */
    public void setAsCorrectAttemptResponse(String subtype) {
        String transactionId = toolMessage.getEventElement().getId();
        this.eventElement = new SemanticEventElement(transactionId, RESULT,
                null, subtype);
        this.actionEvaluationElement = ActionEvaluationElement.createCorrect();
    }
    /**
     * Set action evaluation element to incorrect.
     */
    public void setAsIncorrectAttemptResponse() {
        setAsIncorrectAttemptResponse(null);
    }
    /**
     * Set action evaluation element to incorrect given a subtype.
     * @param subtype the subtype
     */
    public void setAsIncorrectAttemptResponse(String subtype) {
        String transactionId = toolMessage.getEventElement().getId();
        this.eventElement = new SemanticEventElement(transactionId, RESULT,
                null, subtype);
        this.actionEvaluationElement = ActionEvaluationElement
                .createIncorrect();
    }
    /**
     * Set action evaluation element to hint.
     */
    public void setAsHintResponse() {
        setAsHintResponse(null);
    }
    /**
     * Set action evaluation element to hint given a subtype.
     * @param subtype the subtype
     */
    public void setAsHintResponse(String subtype) {
        String transactionId = toolMessage.getEventElement().getId();
        this.eventElement = new SemanticEventElement(transactionId,
                HINT_MESSAGE, null, subtype);
        this.actionEvaluationElement = ActionEvaluationElement.createHint();
    }
    /**
     * Get event descriptor element.
     * @return the event descriptor element
     */
    public EventDescriptorElement getEventDescriptorElement() {
        return eventDescriptorElement;
    }
    /**
     * Add selection, action, and input to the event descriptor element.
     * @param selection the selection
     * @param action the action
     * @param input the input
     */
    public void addSai(String selection, String action, String input) {
        if (eventDescriptorElement == null) {
            eventDescriptorElement = new EventDescriptorElement();
        }
        eventDescriptorElement.addSelection(selection);
        eventDescriptorElement.addAction(action);
        eventDescriptorElement.addInput(input);
    }
    /**
     * Adds a selection to the event descriptor element.
     * @param selection the selection
     */
    public void addSelection(String selection) {
        if (eventDescriptorElement == null) {
            eventDescriptorElement = new EventDescriptorElement();
        }
        eventDescriptorElement.addSelection(selection);
    }
    /**
     * Adds an action to the event descriptor element.
     * @param action the action
     */
    public void addAction(String action) {
        if (eventDescriptorElement == null) {
            eventDescriptorElement = new EventDescriptorElement();
        }
        eventDescriptorElement.addAction(action);
    }
    /**
     * Adds an input to the event descriptor element.
     * @param input the input
     */
    public void addInput(String input) {
        if (eventDescriptorElement == null) {
            eventDescriptorElement = new EventDescriptorElement();
        }
        eventDescriptorElement.addInput(input);
    }
    /**
     * Get action evaluation element.
     * @return the action evaluation element
     */
    public ActionEvaluationElement getActionEvaluationElement() {
        return actionEvaluationElement;
    }
    /**
     * Set action evaluation element.
     * @param actionEvaluationElement the action evaluation element
     */
    public void setActionEvaluationElement(
            ActionEvaluationElement actionEvaluationElement) {
        this.actionEvaluationElement = actionEvaluationElement;
    }
    /**
     * Get tutor advice list.
     * @return the tutor advice list
     */
    public List getTutorAdviceList() {
        return tutorAdviceList;
    }

    /**
     * Add tutorAdvice string to a list. Note that the DataShop only uses the
     * first item in the list.
     *
     * @param tutorAdvice
     *            the tutor advice string
     * @return true if advice added to list, false if null or zero length.
     */
    public boolean addTutorAdvice(String tutorAdvice) {
        if (tutorAdvice != null && tutorAdvice.length() > 0) {
            this.tutorAdviceList.add(tutorAdvice);
            return true;
        }
        return false;
    }
    /**
     * Get skill list.
     * @return the skill list
     */
    public List getSkillList() {
        return skillList;
    }
    /**
     * Add skill to skill list.
     * @param skillElement the skill element
     */
    public void addSkill(SkillElement skillElement) {
        skillList.add(skillElement);
    }
    /**
     * Get the interpretation list.
     * @return a list of interpretation elements
     */
    public List getInterpretationList() {
        return interpretationList;
    }
    /**
     * Add an interpretation.
     * @param interpretationElement the interpretation element
     */
    public void addInterpretation(InterpretationElement interpretationElement) {
        interpretationList.add(interpretationElement);
    }
    /**
     * Get custom field list.
     * @return a list of custom fields elements
     */
    public List getCustomFieldList() {
        return customFieldList;
    }
    /**
     * Add custom field to custom field list given the name and value.
     * @param name the name
     * @param value the value
     */
    public void addCustomField(String name, String value) {
        customFieldList.add(new CustomFieldElement(name, value));
    }
    /**
     * Add custom field to custom field list given the custom field element.
     * @param customFieldElement the custom field element
     */
    public void addCustomField(CustomFieldElement customFieldElement) {
        customFieldList.add(customFieldElement);
    }
    /**
     * Set replay element.
     * @param replayText the replay text
     */
    public void setReplayElement(String replayText) {
        this.replayText = replayText;
    }
    /**
     * To string method calls toString(boolean) method.
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
        buffer.append("<tutor_message context_message_id=\""
                + LogFormatUtils.escapeAttribute(getContextMessageId())
                + "\">\n");

        if (logMetaFlag) {
            buffer.append(getMetaElement());
        }

        if (problemName != null && problemName.length() > 0) {
            buffer.append("\t<problem_name>");
            buffer.append(LogFormatUtils.escapeElement(problemName));
            buffer.append("</problem_name>\n");
        }

        if (eventElement != null) {
            buffer.append(eventElement);
        }

        if (eventDescriptorElement != null) {
            buffer.append(eventDescriptorElement);
        }

        if (actionEvaluationElement != null) {
            buffer.append(actionEvaluationElement);
        }

        for (Iterator iter = tutorAdviceList.iterator(); iter.hasNext();) {
            String tutorAdvice = (String) iter.next();
            buffer.append("\t<tutor_advice>");
            buffer.append(LogFormatUtils.escapeElement(tutorAdvice));
            buffer.append("</tutor_advice>\n");
        }

        for (Iterator iter = skillList.iterator(); iter.hasNext();) {
            SkillElement skillElement = (SkillElement) iter.next();
            if (skillElement != null) {
                buffer.append(skillElement);
            }
        }

        for (Iterator iter = interpretationList.iterator(); iter.hasNext();) {
            InterpretationElement interpretationElement = (InterpretationElement) iter
                    .next();
            if (interpretationElement != null) {
                buffer.append(interpretationElement);
            }
        }

        for (Iterator iter = customFieldList.iterator(); iter.hasNext();) {
            CustomFieldElement customFieldElement = (CustomFieldElement) iter
                    .next();
            if (customFieldElement != null) {
                buffer.append(customFieldElement);
            }
        }

        if (replayText != null && !replayText.equals("")) {
            buffer.append("\t<replay>");
            buffer.append(LogFormatUtils.escapeElement(replayText));
            buffer.append("</replay>\n");
        }

        buffer.append("</tutor_message>\n");
        return buffer.toString();
    }
}
