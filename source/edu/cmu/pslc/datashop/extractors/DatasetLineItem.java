/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.SkillItem;

import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * Simple container class used to hold import dataset line data.
 * Used by the DataShop import dataset tools.
 *
 * @author Kyle A Cunningham
 * @version $Revision: 9850 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-08-23 16:16:37 -0400 (Fri, 23 Aug 2013) $
 * <!-- $KeyWordsOff: $ -->
 */

public class DatasetLineItem {

    /** Student(s). */
    private String studentId;
    /** Session ID. */
    private String sessionID;
    /** Time */
    private String time;
    /** Time Zone */
    private String timeZone;
    /** Student Response Type*/
    private String studentResponseType;
    /** Student Response Subtype */
    private String studentResponseSubtype;
    /** Tutor Response Type */
    private String tutorResponseType;
    /** Tutor Response Subtype */
    private String tutorResponseSubtype;
    /** Dataset Level(s) */
    private ArrayList datasetLevels;
    /** Problem Name */
    private String problemName;
    /** Step Name */
    private String stepName;
    /** Attempt at Step */
    private String attemptAtStep;
    /** Outcome */
    private String outcome;
    /** Selection(s) */
    private Set<String> selections;
    /** Action(s) */
    private Set actions;
    /** Input(s) */
    private Set inputs;
    /** Feedback Text */
    private String feedbackText;
    /** Feedback Classification */
    private String feedbackClassification;
    /** Help Level */
    private Integer helpLevel;
    /** Total Number of Hints */
    private Integer totalNumberHints;
    /** Condition(s) */
    private Set conditions;
    /** Knowledge Components */
    private ArrayList knowledgeComponents;
    /** School */
    private String school;
    /** Class */
    private String classID;
    /** NameStringValuePairs for Custom Fields. Map to avoid duplicates. */
    private Map customFields;
    /** FIXME Need Javadoc comment. */
    private String firstStudent;

    /** Default constructor. */
    public DatasetLineItem() {
        datasetLevels = new ArrayList();
        selections = new HashSet<String>();
        actions  = new HashSet();
        inputs = new HashSet();
        conditions = new HashSet();
        knowledgeComponents = new ArrayList();
        customFields = new HashMap<String, CustomFieldNameValueItem>();
    }

    /**
     * Returns the student id.
     * @return the student id
     */
    public String getStudentId() {
        return sessionID;
    }
    /**
     * Sets student id.
     * @param studentId the student id for this line
     */
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    /**
     * Returns the session id.
     * @return the session id
     */
    public String getSessionID() {
        return sessionID;
    }
    /**
     * Sets session id.
     * @param sessionID the session id for this line
     */
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
    /**
     * Returns the time.
     * @return the time
     */
    public String getTime() {
        return time;
    }
    /**
     * Sets time.
     * @param time the time for this line
     */
    public void setTime(String time) {
        this.time = time;
    }
    /**
     * Returns the time zone.
     * @return the time zone
     */
    public String getTimeZone() {
        return timeZone;
    }
    /**
     * Sets the time zone.
     * @param timeZone the time zone for this line
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    /**
     * Returns the student response type.
     * @return the student response type
     */
    public String getStudentResponseType() {
        return studentResponseType;
    }
    /**
     * Sets the student response type.
     * @param studentResponseType the student response type for this line
     */
    public void setStudentResponseType(String studentResponseType) {
        this.studentResponseType = studentResponseType;
    }
    /**
     * Returns the student response subtype.
     * @return the student response subtype
     */
    public String getStudentResponseSubtype() {
        return studentResponseSubtype;
    }
    /**
     * Sets the student response subtype.
     * @param studentResponseSubtype the student response subtype for this line
     */
    public void setStudentResponseSubtype(String studentResponseSubtype) {
        this.studentResponseSubtype = studentResponseSubtype;
    }
    /**
     * Returns the tutor response type.
     * @return the tutor response type
     */
    public String getTutorResponseType() {
        return tutorResponseType;
    }
    /**
     * Sets the tutor response type.
     * @param tutorResponseType the tutor response type for this line
     */
    public void setTutorResponseType(String tutorResponseType) {
        this.tutorResponseType = tutorResponseType;
    }
    /**
     * Returns the tutor response subtype.
     * @return the tutor response subtype
     */
    public String getTutorResponseSubtype() {
        return tutorResponseSubtype;
    }
    /**
     * Sets the tutor response subtype.
     * @param tutorResponseSubtype the tutor response subtype for this line
     */
    public void setTutorResponseSubtype(String tutorResponseSubtype) {
        this.tutorResponseSubtype = tutorResponseSubtype;
    }
    /**
     * Returns the set of dataset levels.
     * @return the set of dataset levels
     */
    public ArrayList getDatasetLevels() {
        return datasetLevels;
    }
    /**
     * Sets the dataset levels.
     * @param datasetLevels the set of dataset levels for this line
     */
    public void setDatasetLevels(ArrayList datasetLevels) {
        this.datasetLevels = datasetLevels;
    }
    /**
     * Adds a new dataset level to the set of dataset levels.
     * @param datasetLevel - id for the datasetLevel to be added
     */
    public void addDatasetLevel(DatasetLevelItem datasetLevel) {
        this.datasetLevels.add(datasetLevel);
    }
    /**
     * Returns the problem name.
     * @return the problem name
     */
    public String getProblemName() {
        return problemName;
    }
    /**
     * Sets the problem name.
     * @param problemName the problem name for this line
     */
    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }
    /**
     * Returns the step name.
     * @return the step name
     */
    public String getStepName() {
        return stepName;
    }
    /**
     * Sets the step name.
     * @param stepName the step name for this line
     */
    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
    /**
     * Returns the attempt at step.
     * @return the attempt at step
     */
    public String getAttemptAtStep() {
        return attemptAtStep;
    }
    /**
     * Sets the attempt at step.
     * @param attemptAtStep the attempt at step for this line
     */
    public void setAttemptAtStep(String attemptAtStep) {
        this.attemptAtStep = attemptAtStep;
    }
    /**
     * Returns the outcome.
     * @return the outcome
     */
    public String getOutcome() {
        return outcome;
    }
    /**
     * Sets the outcome.
     * @param outcome the outcome for this line
     */
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
    /**
     * Returns the set of selections.
     * @return the set of selections
     */
    public Set<String> getSelections() {
        return selections;
    }
    /**
     * Adds a new selections.
     * @param selection the selections to add
     */
    public void addSelection(String selection) {
        this.selections.add(selection);
    }
    /**
     * Returns the set of actions.
     * @return the set of actions
     */
    public Set getActions() {
        return actions;
    }
    /**
     * Sets the actions.
     * @param actions the actions for this line
     */
    public void setActions(HashSet actions) {
        this.actions = actions;
    }
    /**
     * Adds a new action.
     * @param action the action to add
     */
    public void addAction(String action) {
        this.actions.add(action);
    }
    /**
     * Returns the set of inputs.
     * @return the set of inputs
     */
    public Set getInputs() {
        return inputs;
    }
    /**
     * Sets the inputs.
     * @param inputs the inputs for this line
     */
    public void setInputs(HashSet inputs) {
        this.inputs = inputs;
    }
    /**
     * Adds a new inputs.
     * @param input the inputs to add
     */
    public void addInput(String input) {
        this.inputs.add(input);
    }
    /**
     * Returns the feedback text.
     * @return the feedback text
     */
    public String getFeedbackText() {
        return feedbackText;
    }
    /**
     * Sets the feedback text.
     * @param feedbackText the feedback text for this line
     */
    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }
    /**
     * Returns the feedback classification.
     * @return the feedback classification
     */
    public String getFeedbackClassification() {
        return feedbackClassification;
    }
    /**
     * Sets the feedback classification.
     * @param feedbackClassification the feedback classification for this line
     */
    public void setFeedbackClassification(String feedbackClassification) {
        this.feedbackClassification = feedbackClassification;
    }
    /**
     * Returns the help level.
     * @return the help level
     */
    public Integer getHelpLevel() {
        return helpLevel;
    }
    /**
     * Sets the help level.
     * @param helpLevel the help level for this line
     */
    public void setHelpLevel(Integer helpLevel) {
        this.helpLevel = helpLevel;
    }
    /**
     * Returns the total number of hints.
     * @return the total number of hints
     */
    public Integer getTotalNumberHints() {
        return totalNumberHints;
    }
    /**
     * Sets the total number of hints.
     * @param totalNumberHints the total number of hints for this line
     */
    public void setTotalNumberHints(Integer totalNumberHints) {
        this.totalNumberHints = totalNumberHints;
    }
    /**
     * Returns the set of conditions.
     * @return the set of conditions
     */
    public Set getConditions() {
        return conditions;
    }
    /**
     * Sets the conditions.
     * @param conditions the conditions for this line
     */
    public void setConditions(HashSet conditions) {
        this.conditions = conditions;
    }
    /**
     * Adds a new condition.
     * @param conditionItem the condition to add
     */
    public void addCondition(ConditionItem conditionItem) {
        this.conditions.add(conditionItem);
    }
    /**
     * Returns the set of knowledge components.
     * @return the set of knowledge components
     */
    public ArrayList getKnowledgeComponents() {
        return knowledgeComponents;
    }
    /**
     * Sets the knowledge components.
     * @param knowledgeComponents the knowledge components for this line
     */
    public void setKnowledgeComponent(ArrayList knowledgeComponents) {
        this.knowledgeComponents = knowledgeComponents;
    }
    /**
     * Adds a new knowledge component.
     * @param skillItem the knowledge component to add
     */
    public void addKnowledgeComponent(SkillItem skillItem) {
        this.knowledgeComponents.add(skillItem);
    }
    /**
     * Returns the school.
     * @return the school
     */
    public String getSchool() {
        return school;
    }
    /**
     * Sets the school.
     * @param school the school for this line
     */
    public void setSchool(String school) {
        this.school = school;
    }
    /**
     * Returns the class id.
     * @return the class id
     */
    public String getClassID() {
        return classID;
    }
    /**
     * Sets the class id.
     * @param classID the class id for this line
     */
    public void setClassID(String classID) {
        this.classID = classID;
    }
    /**
     * Returns the set of NameStringValuePairs for custom fields.
     * @return the set of NameStringValuePairs for custom fields
     */
    public Map<String, CustomFieldNameValueItem> getCustomFields() {
        return customFields;
    }
    /**
     * Sets the nameStringValuePairs for custom fields.
     * @param customFields the nameStringValuePairs of custom fields for this line
     */
    public void setCustomField(HashMap customFields) {
        this.customFields = customFields;
    }
    /**
     * Adds a new nameStringValuePair for custom field.
     * First one in wins. Don't insert if 'name' already present in map.
     * @param customFieldItem the custom field to add
     */
    public void addCustomField(CustomFieldNameValueItem customFieldNameValuePair) {
        String cfName = customFieldNameValuePair.getName();
        if (!customFields.containsKey(cfName)) {
            customFields.put(cfName, customFieldNameValuePair);
        }
    }

} // end DatasetLineItem class
