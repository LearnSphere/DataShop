/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * Data object that contains all fields for tool message for version 2 and 4.
 *
 * @author Hui cheng
 * @version $Revision: 9850 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-08-23 16:16:37 -0400 (Fri, 23 Aug 2013) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ToolMessage {
    /** The context_message_id, previously attempt_id. */
    private String contextId;
    /** The problem_name converted to ProblemItem. */
    private ProblemItem problemItem;
    /** Collection of Semantic Event associated with this tool message. */
    private Set semanticEvents;
    /** Collection of UI Event associated with this tool message. */
    private Set uiEvents;
    /** Collection of Event Descriptor associated with this tool message. */
    private Set eventDescriptors;
    /**
     * Collection of custom fields associated with this tool message.
     * Using a Map to avoid duplicate names. First one in wins.
     */
    private Map customFields;

    /** Default constructor. */
    public ToolMessage() {
    }

    /** The constructor that sets all fields.
     *  @param contextId String.
     *  @param problemItem ProblemItem.
     *  @param semanticEvents Set.
     *  @param uiEvents Set.
     *  @param eventDescriptors Set.
     *  @param customFields Set.
     */
    public ToolMessage(String contextId,
                       ProblemItem problemItem,
                       Set semanticEvents,
                       Set uiEvents,
                       Set eventDescriptors,
                       Map customFields) {
        this.contextId = contextId;
        this.problemItem = problemItem;
        this.semanticEvents = semanticEvents;
        this.uiEvents = uiEvents;
        this.eventDescriptors = eventDescriptors;
        this.customFields = customFields;
    }

    /** The getter for contextId.
     * @return The contextId.
     */
    public String getContextId () {
        return contextId;
    }

    /** The setter for contextId.
     * @param contextId String.
     */
    public void setContextId (String contextId) {
        this.contextId = contextId;
    }

    /** The getter for problemItem.
     * @return The problemItem.
     */
    public ProblemItem getProblemItem () {
        return problemItem;
    }

    /** The setter for problemItem.
     * @param problemItem ProblemItem.
     */
    public void setProblemItem (ProblemItem problemItem) {
        this.problemItem = problemItem;
    }

    /**
     * Get eventDescriptors.
     * @return java.util.Set
     */
    protected Set getEventDescriptors() {
        if (this.eventDescriptors == null) {
            this.eventDescriptors = new HashSet();
        }
        return this.eventDescriptors;
    }

    /**
     * Public method to get eventDescriptors.
     * @return a list instead of a set
     */
    public List getEventDescriptorsExternal() {
        List sortedList = new ArrayList(getEventDescriptors());
        //Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set eventDescriptors.
     * @param eventDescriptors Collection of eventDescriptors associated with this tool message.
     */
    protected void setEventDescriptors(Set eventDescriptors) {
        this.eventDescriptors = eventDescriptors;
    }

    /**
     * Add a eventDescriptor.
     * @param item to add
     */
    public void addEventDescriptor(EventDescriptor item) {
        if (!getEventDescriptors().contains(item)) {
            getEventDescriptors().add(item);
        }
    }

    /**
     * Get semanticEvents.
     * @return java.util.Set
     */
    protected Set getSemanticEvents() {
        if (this.semanticEvents == null) {
            this.semanticEvents = new HashSet();
        }
        return this.semanticEvents;
    }

    /**
     * Public method to get semanticEvents.
     * @return a list instead of a set
     */
    public List getSemanticEventsExternal() {
        List sortedList = new ArrayList(getSemanticEvents());
        //Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set semanticEvents.
     * @param semanticEvents Collection of semanticEvents associated with tool message.
     */
    protected void setSemanticEvents(Set semanticEvents) {
        this.semanticEvents = semanticEvents;
    }

    /**
     * Add a semanticEvent.
     * @param item to add
     */
    public void addSemanticEvent(SemanticEvent item) {
        if (!getSemanticEvents().contains(item)) {
            getSemanticEvents().add(item);
        }
    }

    /**
     * Get uiEvents.
     * @return java.util.Set
     */
    protected Set getUiEvents() {
        if (this.uiEvents == null) {
            this.uiEvents = new HashSet();
        }
        return this.uiEvents;
    }

    /**
     * Public method to get uiEvents.
     * @return a list instead of a set
     */
    public List getUiEventsExternal() {
        List sortedList = new ArrayList(getUiEvents());
        //Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set uiEvents.
     * @param uiEvents Collection of uiEvents associated with tool message.
     */
    protected void setUiEvents(Set uiEvents) {
        this.uiEvents = uiEvents;
    }

    /**
     * Add a uiEvent.
     * @param item to add
     */
    public void addUiEvent(UiEvent item) {
        if (!getUiEvents().contains(item)) {
            getUiEvents().add(item);
        }
    }

    /**
     * Get customFields.
     * @return java.util.Map
     */
    protected Map<String, CustomFieldNameValueItem> getCustomFields() {
        if (this.customFields == null) {
            this.customFields = new HashMap<String, CustomFieldNameValueItem>();
        }
        return this.customFields;
    }

    /**
     * Public method to get customFields.
     * @return a list instead of a map
     */
    public List getCustomFieldsExternal() {
        List sortedList = new ArrayList(getCustomFields().values());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set customFields.
     * @param customFields Collection of problems associated with this tool message.
     */
    protected void setCustomFields(Map customFields) {
        this.customFields = customFields;
    }

    /**
     * Add a customField.
     * @param item to add
     */
    public void addCustomField(CustomFieldNameValueItem item) {
        if (!getCustomFields().containsKey(item.getName())) {
            getCustomFields().put(item.getName(), item);
        }
    }

    /**
     * Loop through the event descriptors and concatenate the selections,
     * actions, together into one nice little string.
     * @return a string of all the SAIs
     */
    public String getSAI() {
        String sai = "";
        for (Iterator edIter = getEventDescriptorsExternal().iterator(); edIter.hasNext();) {
            EventDescriptor eventDesc = (EventDescriptor)edIter.next();

            // get selections
            List selList = eventDesc.getSelectionsExternal();
            for (Iterator selIter = selList.iterator(); selIter.hasNext();) {
                AttemptSelectionItem selItem = (AttemptSelectionItem)selIter.next();
                sai += selItem.getSelection() + " ";
            }

            // get actions
            List actList = eventDesc.getActionsExternal();
            for (Iterator actIter = actList.iterator(); actIter.hasNext();) {
                AttemptActionItem actItem = (AttemptActionItem)actIter.next();
                sai += actItem.getAction() + " ";
            }

            // input is part of subgoal name
            /*
            List inputList = eventDesc.getInputsExternal();
            for (Iterator inputIter = inputList.iterator(); inputIter.hasNext();) {
                AttemptInputItem inputItem = (AttemptInputItem)inputIter.next();
                sai += inputItem.getInput() + " ";
            }*/
        }
        sai = sai.trim();
        return sai;
    }

}