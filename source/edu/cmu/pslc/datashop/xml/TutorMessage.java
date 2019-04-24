/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
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

import edu.cmu.pslc.datashop.item.ActionItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SelectionItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;

/**
 * Data object that contains all fields for tutor message for version 2 and 4.
 *
 * @author Hui cheng
 * @version $Revision: 9850 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-08-23 16:16:37 -0400 (Fri, 23 Aug 2013) $
 * <!-- $KeyWordsOff: $ -->
 */

public class TutorMessage {

    /** The context_message_id, previously attempt_id. */
    private String contextId;
    /** The problem_name converted to ProblemItem. */
    private ProblemItem problemItem;
    /** Collection of Semantic Event associated with this tutor message. */
    private Set semanticEvents;
    /** Collection of Event Descriptor associated with this tutor message. */
    private Set eventDescriptors;
    /**
     * Collection of custom fields associated with this tutor message.
     * Using a Map to avoid duplicate names. First one in wins.
     */
    private Map customFields;
    /** Collection of action evaluations, type of ActionEvaluations,
     * associated with this tutor message. */
    private Set actionEvaluations;
    /** Collection of tutor advice, type of String, associated with this tutor message. */
    private List tutorAdvices;
    /**Collection of skills, type of SkillItem, associated with this tutor message.*/
    private Set skills;
    /**Collection of interpretations, type of Interpretation,
     * associated with this tutor message.*/
    private Set interpretations;

    /** Default constructor. */
    public TutorMessage() {
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
        Collections.sort(sortedList);
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
     * Copy the event descriptors.
     * @param newMsg the new message
     */
    public void copyEventDescriptors(TutorMessage newMsg) {
        this.eventDescriptors = newMsg.eventDescriptors;
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
     * Loop through the event descriptors and concatenate the selections,
     * actions, and inputs together into one nice little string.
     * @return a string of all the SAIs
     */
    public String getSAI() {
        String sai = "";
        for (Iterator edIter = getEventDescriptorsExternal().iterator(); edIter.hasNext();) {
            EventDescriptor eventDesc = (EventDescriptor)edIter.next();

            // get selections
            List selList = eventDesc.getSelectionsExternal();
            for (Iterator selIter = selList.iterator(); selIter.hasNext();) {
                SelectionItem selItem = (SelectionItem)selIter.next();
                sai += selItem.getSelection() + " ";
            }

            // get actions
            List actList = eventDesc.getActionsExternal();
            for (Iterator actIter = actList.iterator(); actIter.hasNext();) {
                ActionItem actItem = (ActionItem)actIter.next();
                sai += actItem.getAction() + " ";
            }

            // input is not part of the subgoal or its name
        }
        sai = sai.trim();
        return sai;
    }

    /**
     * Loop through actionEvatuations to get correct flag for subgoal attempt.
     * Rules: ERROR, BUG, INCORRECT return ERROR; *HINT*, *HELP* return HINT;
     * OK, CORRECT return CORRECT, otherwise return UNKNOWN.
     *
     * @return List of strings for correct flags
     */
    public List getCorrectFlags() {
        List correctFlags = new ArrayList();
        for (Iterator eaIter = getActionEvaluationsExternal().iterator(); eaIter.hasNext();) {
            ActionEvaluation correctFlag = (ActionEvaluation)eaIter.next();
            if (correctFlag != null && correctFlag.getContent() != null) {
                String content = correctFlag.getContent().toUpperCase();
                correctFlags.add(content);
            } else {
                correctFlags.add(SubgoalAttemptItem.CORRECT_FLAG_UNKNOWN);
            }
        }
        return correctFlags;
    }

    /**
     * Get actionEvaluations.
     * @return java.util.Set
     */
    protected Set getActionEvaluations() {
        if (this.actionEvaluations == null) {
            this.actionEvaluations = new HashSet();
        }
        return this.actionEvaluations;
    }

    /**
     * Public method to get actionEvaluations.
     * @return an unmodifiable set
     */
    public Set getActionEvaluationsExternal() {
        return Collections.unmodifiableSet(getActionEvaluations());
    }

    /**
     * Set actionEvaluations.
     * @param actionEvaluations Collection of actionEvaluations associated with tutor message.
     */
    public void setActionEvaluations(Set actionEvaluations) {
        this.actionEvaluations = actionEvaluations;
    }

    /**
     * Add a actionEvaluations.
     * @param item to add
     */
    public void addActionEvaluations(ActionEvaluation item) {
        if (!getActionEvaluations().contains(item)) {
            getActionEvaluations().add(item);
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
     * Get tutorAdvices.
     * @return java.util.List
     */
    protected List getTutorAdvices() {
        if (this.tutorAdvices == null) {
            this.tutorAdvices = new ArrayList();
        }
        return this.tutorAdvices;
    }

    /**
     * Public method to get tutorAdvices.
     * @return an unmodifiable set
     */
    public List getTutorAdvicesExternal() {
        return Collections.unmodifiableList(getTutorAdvices());
    }

    /**
     * Set tutorAdvices.
     * @param tutorAdvices Collection of tutorAdvices associated with tutor message.
     */
    public void setTutorAdvices(List tutorAdvices) {
        this.tutorAdvices = tutorAdvices;
    }

    /**
     * Add a TutorAdvices.
     * @param tutorAdvice to add
     */
    public void addTutorAdvices(String tutorAdvice) {
        if (!getTutorAdvices().contains(tutorAdvice)) {
            getTutorAdvices().add(tutorAdvice);
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
     * @return a list instead of the map
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
     * Get skills.
     * @return java.util.Set
     */
    protected Set getSkills() {
        if (this.skills == null) {
            this.skills = new HashSet();
        }
        return this.skills;
    }

    /**
     * Public method to get skills.
     * @return an unmodifiable set
     */
    public Set getSkillsExternal() {
        return Collections.unmodifiableSet(getSkills());
    }

    /**
     * Set skills.
     * @param skills Collection of skills associated with this tutor message.
     */
    public void setSkills(Set skills) {
        this.skills = skills;
    }

    /**
     * Add a skill.
     * @param item to add
     */
    public void addSkill(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
        }
    }

    /**
     * Get interpretations.
     * @return java.util.Set
     */
    protected Set getInterpretations() {
        if (this.interpretations == null) {
            this.interpretations = new HashSet();
        }
        return this.interpretations;
    }

    /**
     * Public method to get interpretations.
     * @return a list instead of a set
     */
    public List getInterpretationsExternal() {
        List sortedList = new ArrayList(getInterpretations());
        //Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set interpretations.
     * @param interpretations Collection of interpretations associated with this tutor message.
     */
    protected void setInterpretations(Set interpretations) {
        this.interpretations = interpretations;
    }

    /**
     * Add a interpretation.
     * @param item to add
     */
    public void addInterpretation(Interpretation item) {
        if (!getInterpretations().contains(item)) {
            getInterpretations().add(item);
        }
    }

}