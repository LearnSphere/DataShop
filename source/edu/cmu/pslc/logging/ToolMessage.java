/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.logging.element.CustomFieldElement;
import edu.cmu.pslc.logging.element.EventDescriptorElement;
import edu.cmu.pslc.logging.element.EventElement;
import edu.cmu.pslc.logging.element.SemanticEventElement;
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
public final class ToolMessage extends Message {
    /** The problem name. */
    private String problemName = null;
    /** The event element. */
    private EventElement eventElement = null;
    /** The event descriptor element. */
    private EventDescriptorElement eventDescriptorElement = null;
    /** The custom field list. */
    private List customFieldList = new ArrayList();
    /** The replay text. */
    private String replayText = null;
    /**
     * Creates a new tool message given a context message.
     * @param contextMessage the context message
     * @return the new tool message
     */
    public static ToolMessage create(ContextMessage contextMessage) {
        ToolMessage toolMessage = new ToolMessage(contextMessage);
        return toolMessage;
    }
    /**
     * Private constructor (creates a new tool message).
     * @param contextMessage a context message
     */
    private ToolMessage(ContextMessage contextMessage) {
        super(contextMessage.getContextMessageId(), contextMessage.getMetaElement());
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
     * Set event as an attempt event.
     */
    public void setAsAttempt() {
        setAsAttempt(null);
    }
    /**
     * Set event as an attempt event given the subtype.
     * @param subtype a subtype
     */
    public void setAsAttempt(String subtype) {
        String transactionId = generateGUID("T"); //T for Transaction Id
        this.eventElement = new SemanticEventElement(transactionId, ATTEMPT, null, subtype);
    }
    /**
     * Set event as hint request.
     */
    public void setAsHintRequest() {
        setAsHintRequest(null);
    }
    /**
     * Set event as hint request given the subtype.
     * @param subtype the subtype
     */
    public void setAsHintRequest(String subtype) {
        String transactionId = generateGUID("T"); //T for TransactionId
        this.eventElement = new SemanticEventElement(transactionId, HINT_REQUEST, null, subtype);
    }
    /**
     * Get event descriptor element.
     * @return the event descriptor element
     */
    public EventDescriptorElement getEventDescriptorElement() {
        return eventDescriptorElement;
    }
    /**
     * Add selection, action, and input.
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
     * Add selection to event descriptor element.
     * @param selection the selection
     */
    public void addSelection(String selection) {
        if (eventDescriptorElement == null) {
            eventDescriptorElement = new EventDescriptorElement();
        }
        eventDescriptorElement.addSelection(selection);
    }
    /**
     * Add action to event descriptor element.
     * @param action the action
     */
    public void addAction(String action) {
        if (eventDescriptorElement == null) {
            eventDescriptorElement = new EventDescriptorElement();
        }
        eventDescriptorElement.addAction(action);
    }
    /**
     * Add input to event descriptor element.
     * @param input the input
     */
    public void addInput(String input) {
        if (eventDescriptorElement == null) {
            eventDescriptorElement = new EventDescriptorElement();
        }
        eventDescriptorElement.addInput(input);
    }
    /**
     * Get custom field list.
     * @return the custom field list
     */
    public List getCustomFieldList() {
        return customFieldList;
    }
    /**
     * Add custom field given the name and value.
     * @param name the name
     * @param value the value
     */
    public void addCustomField(String name, String value) {
        customFieldList.add(new CustomFieldElement(name, value));
    }
    /**
     * Add custom field given the custom field element.
     * @param customFieldElement the custom field element
     */
    public void addCustomField(CustomFieldElement customFieldElement) {
        customFieldList.add(customFieldElement);
    }
    /**
     * Set transaction id.
     * @param id the transaction id
     */
    public void setTransactionId(String id) {
        eventElement.setId(id);
    }
    /**
     * Get replay text.
     * @return the replay text
     */
    public String getReplayText() {
        return replayText;
    }
    /**
     * Set replay text.
     * @param replayText the replay text
     */
    public void setReplayElement(String replayText) {
        this.replayText = replayText;
    }
    /**
     * To string method calls toString(boolean).
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
        buffer.append("<tool_message context_message_id=\""
                + LogFormatUtils.escapeAttribute(getContextMessageId()) + "\">\n");

        if (logMetaFlag) { buffer.append(getMetaElement()); }

        if (problemName != null && problemName.length() > 0) {
            buffer.append("\t<problem_name>");
            buffer.append(LogFormatUtils.escapeElement(problemName));
            buffer.append("</problem_name>\n");
        }

        if (eventElement != null) { buffer.append(eventElement); }

        if (eventDescriptorElement != null) { buffer.append(eventDescriptorElement); }

        for (Iterator iter = customFieldList.iterator(); iter.hasNext();) {
            CustomFieldElement customFieldElement = (CustomFieldElement)iter.next();
            if (customFieldElement != null) {
                buffer.append(customFieldElement);
            }
        }

        if (replayText != null && !replayText.equals("")) {
            buffer.append("\t<replay>");
            buffer.append(LogFormatUtils.escapeAttribute(replayText));
            buffer.append("</replay>\n");
        }

        buffer.append("</tool_message>\n");
        return buffer.toString();
    }
}
