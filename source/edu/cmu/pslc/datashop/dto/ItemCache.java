/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.pslc.datashop.item.ActionItem;
import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.ClassItem;
import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.FeedbackItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.SelectionItem;
import edu.cmu.pslc.datashop.item.SessionItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalAttemptItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * This class acts as a cache of known items for processing of bulk transactions.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ItemCache {

    /** Set of known existing skills. */
    private Map<Comparable, SkillItem> existingSkills;

    /** Set of known existing skills. */
    private Set<SkillModelItem> existingModels;

    /** Set of known existing attempts. */
    private Set<SubgoalItem> existingSubgoals;

    /** Set of known existing selections. */
    private Set<SelectionItem> existingSelections;

    /** Set of known existing actions. */
    private Set<ActionItem> existingActions;

    /** Set of known existing attempt attempts. */
    private List<SubgoalAttemptItem> existingAttempts;

    /** Map of known existing attempt selections.
     * Key = SubgoalAttemptItem Value = Collection*/
    private Map<Comparable, Collection<AttemptSelectionItem>> existingAttemptSelections;

    /** HashMap of known existing attempt actions.
     * Key = SubgoalAttemptItem Value = Collection*/
    private Map<Comparable, Collection<AttemptActionItem>> existingAttemptActions;

    /** HashMap of known existing attempt Input.
     * Key = SubgoalAttemptItem Value = Collection*/
    private Map<Comparable, Collection<AttemptInputItem>> existingAttemptInputs;

    /** Set of known existing conditions. */
    private Set<ConditionItem> existingConditions;

    /** The problem Item currently being processed */
    private ProblemItem existingProblem;

    /** Map of known attempts with the SubgoalItem as a key. */
    private Map<Comparable, Integer> attemptNumbers;

    /** Set of known existing feedbacks.*/
    private Set<FeedbackItem> existingFeedbacks;

    /** Set of known existing customFields.*/
    private Set<CustomFieldItem> existingCustomFields;

    /** Set of known existing cognitive steps.*/
    private Set<CognitiveStepItem> existingCognitiveSteps;

    /** Set of known existing sessions */
    private Set<SessionItem> existingSessions;

    /** Set of known existing students */
    private Set<StudentItem> existingStudents;

    /** Set of known existing dataset levels */
    private Set<DatasetLevelItem> existingDatasetLevels;

    /** Set of known existing problems */
    private Set<ProblemItem> existingProblems;

    /** Set of known existing schools */
    private Set<SchoolItem> existingSchools;

    /** Set of known existing classes */
    private Set<ClassItem> existingClasses;

    /** Default Constructor. */
    public ItemCache() { }

    /**
     * Clears out all objects from this cache.
     */
    public void clearAll() {
        existingSkills = null;
        existingModels = null;
        existingSubgoals = null;
        existingSelections = null;
        existingActions = null;
        existingAttempts = null;
        existingAttemptActions = null;
        existingAttemptSelections = null;
        existingAttemptInputs = null;
        existingConditions = null;
        attemptNumbers = null;
        existingFeedbacks = null;
        existingProblem = null;
        existingProblems = null;
    }

    /* Problem */

    /**
     * Get the existing problem.
     * @return the existing problem or null if non set.s
     */
    public ProblemItem getProblem() {
        return existingProblem;
    }

    /**
     * Set the existing problem to the passed in problem.
     * @param problem the existing problem to set.
     */
    public void setProblem(ProblemItem problem) {
        existingProblem = problem;
    }

    /* Actions */

    /** Get Existing Actions. @return Set of existing Actions */
    public Set<ActionItem> getExistingActions() {
        existingActions = checkNull(existingActions);
        return existingActions;
    }

    /** Add an ActionItem to the list of known actions. @param item the new ActionItem */
    public void addAction(ActionItem item) {
        getExistingActions().add(item);
    }

    /**
     * Add an ActionItem to the list of known actions.
     * @param actions Collection of all actions to add.
     */
    public void setExistingActions(Collection<ActionItem> actions) {
        getExistingActions().addAll(actions);
    }

    /* SkillModels */

    /** Get Existing SkillModels. @return Set of existing SkillModels */
    public Set<SkillModelItem> getExistingModels() {
        existingModels = checkNull(existingModels);
        return existingModels;
    }

    /** Add an SkillModelItem to the list of known models. @param item the new SkillModelItem */
    public void addModel(SkillModelItem item) {
        getExistingModels().add(item);
    }

    /**
     * Add an ModelItem to the list of known models.
     * @param models Collection of all models to add.
     */
    public void setExistingModels(Collection<SkillModelItem> models) {
        getExistingModels().addAll(models);
    }

    /* Selections */

    /** Get Existing Selections. @return Set of existing Selections */
    public Set<SelectionItem> getExistingSelections() {
        existingSelections = checkNull(existingSelections);
        return existingSelections;
    }

    /** Add an SelectionItem to the list of known selections. @param item the new SelectionItem */
    public void addSelection(SelectionItem item) {
        getExistingSelections().add(item);
    }

    /**
     * Add an SelectionItem to the list of known selections.
     * @param selections Collection of all selections to add.
     */
    public void setExistingSelections(Collection<SelectionItem> selections) {
        getExistingSelections().addAll(selections);
    }

    /* Skills */

    private Map<Comparable, SkillItem> getExistingSkillsMap() {
        existingSkills = checkNull(existingSkills);
        return existingSkills;
    }

    /** Get Existing Skills. @return Set of existing Skills */
    public Collection <SkillItem> getExistingSkills() {
        return getExistingSkillsMap().values();
    }

    /** Add an SkillItem to the list of known skills. @param item the new SkillItem */
    public void addSkill(SkillItem item) {
        getExistingSkillsMap().put(item.getId(), item);
    }

    /**
     * Add an SkillItem to the list of known skills.
     * @param skills Collection of all skills to add.
     */
    public void setExistingSkills(Collection<SkillItem> skills) {
        for (SkillItem skill : skills) {
            getExistingSkillsMap().put(skill.getId(), skill);
        }
    }

    /* Subgoals */

    /** Get Existing Subgoals. @return Set of existing Subgoals */
    public Set<SubgoalItem> getExistingSubgoals() {
        existingSubgoals = checkNull(existingSubgoals);
        return existingSubgoals;
    }

    /** Add an SubgoalItem to the list of known attempts.
     * @param item the new SubgoalItem
     * @return true if the set did not already contain the item. */
    public boolean addSubgoal(SubgoalItem item) {
        return getExistingSubgoals().add(item);
    }

    /**
     * Add an SubgoalItem to the list of known attempts.
     * @param attempts Collection of all attempts to add.
     */
    public void setExistingSubgoals(Collection<SubgoalItem> attempts) {
        getExistingSubgoals().addAll(attempts);
    }


    /* Subgoal Attempts */

    /** Get Existing Attempts. @return Set of existing SubgoalAttemptItems */
    public List<SubgoalAttemptItem> getExistingAttempts() {
        existingAttempts = checkNull(existingAttempts);
        return existingAttempts;
    }

    /** Add an SubgoalAttemptItem to the list of known attempts.
     * @param item the new AttemptItem
     * @return true if the set did not already contain the item. */
    public boolean addAttempt(SubgoalAttemptItem item) {
        return getExistingAttempts().add(item);
    }

    /**
     * Add a list of know attempts to the list of known attempts.
     * @param attempts Collection of all attempts to add.
     */
    public void setExistingAttempts(Collection<SubgoalAttemptItem> attempts) {
        getExistingAttempts().addAll(attempts);
    }

    /* Attempt Selections */

    /**
     * Map of known existing attempt selections.
     * @return map of known existing attempt selections
     */
    private Map<Comparable, Collection<AttemptSelectionItem>> getExistingAttemptSelectionsMap() {
        existingAttemptSelections = checkNull(existingAttemptSelections);
        return existingAttemptSelections;
    }

    /**
     * Get the values mapped to the item identifier, or an empty collection if there are none.
     * Ensures that there is an empty list mapped to item if one has not been created yet.
     * @param <V> the value of things we are storing
     * @param existing to-many map from item identifiers to a collection of values
     * @param item an item
     * @return a collection of...
     */
    private <V> Collection<V> getExistingItems(Map<Comparable, Collection<V>> existing, Item item) {
        Collection<V> items = existing.get(item.getId());
        if (items == null) {
            items = new ArrayList<V>();
            existing.put(item.getId(), items);
        }
        return items;
    }
    /**
     * Get Existing AttemptSelections for a given attempt.
     * @param attempt SubgoalAttemptItem to get selections for.
     * @return Set of existing AttemptSelections */
    public Collection<AttemptSelectionItem> getExistingAttemptSelections(
            SubgoalAttemptItem attempt) {
        return getExistingItems(getExistingAttemptSelectionsMap(), attempt);
    }

    /**
     * Add an collections of known attemptSelections for a given attempt.
     * @param attempt The SubgoalAttemptItem we are adding selections for.
     * @param attemptSelections Collection of all attemptSelections to add.
     */
    public void setExistingAttemptSelections(SubgoalAttemptItem attempt,
            Collection<AttemptSelectionItem> attemptSelections) {
        getExistingAttemptSelectionsMap().put(attempt.getId(), attemptSelections);
    }

    /* Attempt Actions */

    /**
     * Returns a map of attempt actions.
     * @return map of attempt actions
     */
    private Map<Comparable, Collection<AttemptActionItem>> getExistingAttemptActionsMap() {
        existingAttemptActions = checkNull(existingAttemptActions);
        return existingAttemptActions;
    }

    /**
     * Get Existing AttemptActions for a given attempt.
     * @param attempt SubgoalAttemptItem to get actions for.
     * @return Set of existing AttemptActions */
    public Collection<AttemptActionItem> getExistingAttemptActions(SubgoalAttemptItem attempt) {
        return getExistingItems(getExistingAttemptActionsMap(), attempt);
    }

    /**
     * Add an collections of known attemptActions for a given attempt.
     * @param attempt The SubgoalAttemptItem we are adding actions for.
     * @param attemptActions Collection of all attemptActions to add.
     */
    public void setExistingAttemptActions(
            SubgoalAttemptItem attempt, Collection<AttemptActionItem> attemptActions) {
        getExistingAttemptActionsMap().put(attempt.getId(), attemptActions);
    }

    /* Attempt Inputs */

    /**
     * Returns a map of attempt inputs
     * @return map of attempt inputs
     */
    private Map<Comparable, Collection<AttemptInputItem>> getExistingAttemptInputsMap() {
        existingAttemptInputs = checkNull(existingAttemptInputs);
        return existingAttemptInputs;
    }

    /**
     * Get Existing AttemptInputs for a given attempt.
     * @param attempt SubgoalAttemptItem to get inputs for.
     * @return Set of existing AttemptInputs */
    public Collection<AttemptInputItem> getExistingAttemptInputs(SubgoalAttemptItem attempt) {
        return getExistingItems(getExistingAttemptInputsMap(), attempt);
    }

    /**
     * Add an collections of known attemptInputs for a given attempt.
     * @param attempt The SubgoalAttemptItem we are adding inputs for.
     * @param attemptInputs Collection of all attemptInputs to add.
     */
    public void setExistingAttemptInputs(
            SubgoalAttemptItem attempt, Collection<AttemptInputItem> attemptInputs) {
        getExistingAttemptInputsMap().put(attempt.getId(), attemptInputs);
    }

    /* Conditions */

    /** Get Existing Conditions. @return Set of existing Conditions */
    public Set<ConditionItem> getExistingConditions() {
        existingConditions = checkNull(existingConditions);
        return existingConditions;
    }

    /** Add an ConditionItem to the list of known conditions.
     * @param item the new ConditionItem */
    public void addCondition(ConditionItem item) {
        getExistingConditions().add(item);
    }

    /**
     * Add an ConditionItem to the list of known conditions.
     * @param conditions Collection of all conditions to add.
     */
    public void setExistingConditions(Collection<ConditionItem> conditions) {
        getExistingConditions().addAll(conditions);
    }

    /* Attempt Numbers */

    /**
     * Returns a map of attempt numbers.
     * @return map of attempt numbers
     */
    private Map<Comparable, Integer> getAttemptNumbersMap() {
        attemptNumbers = checkNull(attemptNumbers);
        return attemptNumbers;
    }

    /**
     * Sets the attempt number for the given subgoal.
     * @param subgoal The SubgoalItem to set an attempt number for.
     * @param number the attempt number as an Integer.
     */
    public void setAttemptNumber(SubgoalItem subgoal, Integer number) {
        getAttemptNumbersMap().put(subgoal, number);
    }

    /**
     * Gets the attempt number for a given subgoal.
     * @param subgoal the SubgoalItem to get an attempt number for.
     * @return the attempt number as an Integer or null if none is known.
     */
    public Integer getAttemptNumber(SubgoalItem subgoal) {
        return getAttemptNumbersMap().get(subgoal);
    }

    /* Feedback */

    /** Get Existing Feedbacks. @return Set of existing Feedbacks */
    public Set<FeedbackItem> getExistingFeedbacks() {
        existingFeedbacks = checkNull(existingFeedbacks);
        return existingFeedbacks;
    }

    /** Add an FeedbackItem to the list of known feedbacks.
     * @param item the new FeedbackItem */
    public void addFeedback(FeedbackItem item) {
        getExistingFeedbacks().add(item);
    }

    /**
     * Add an FeedbackItem to the list of known feedbacks.
     * @param feedbacks Collection of all feedbacks to add.
     */
    public void setExistingFeedbacks(Collection<FeedbackItem> feedbacks) {
        getExistingFeedbacks().addAll(feedbacks);
    }

    /* Custom Field */

    /** Get Existing CustomFields. @return HashSet of existing CustomFields */
    public Set<CustomFieldItem> getExistingCustomFields() {
        existingCustomFields = checkNull(existingCustomFields);
        return existingCustomFields;
    }

    /** Add an CustomFieldItem to the list of known customFields.
     * @param item the new CustomFieldItem */
    public void addCustomField(CustomFieldItem item) {
        getExistingCustomFields().add(item);
    }

    /**
     * Add an CustomFieldItem to the list of known customFields.
     * @param customFields Collection of all customFields to add.
     */
    public void setExistingCustomFields(Collection<CustomFieldItem> customFields) {
        getExistingCustomFields().addAll(customFields);
    }

    /* Cognitive Steps */

    /** Get Existing CognitiveSteps. @return Set of existing CognitiveSteps */
    public Set<CognitiveStepItem> getExistingCognitiveSteps() {
        existingCognitiveSteps = checkNull(existingCognitiveSteps);
        return existingCognitiveSteps;
    }

    /** Add an CognitiveStepItem to the list of known cognitiveSteps.
     * @param item the new CognitiveStepItem */
    public void addCognitiveStep(CognitiveStepItem item) {
        getExistingCognitiveSteps().add(item);
    }

    /**
     * Add an CognitiveStepItem to the list of known cognitiveSteps.
     * @param cognitiveSteps Collection of all cognitiveSteps to add.
     */
    public void setExistingCognitiveSteps(Collection cognitiveSteps) {
        getExistingCognitiveSteps().addAll(cognitiveSteps);
    }

    /**
     * Get the existing sessions.
     * @return the existingSessions
     */
    public Set<SessionItem> getExistingSessions() {
        existingSessions = checkNull(existingSessions);
        return existingSessions;
    }

    /**
     * Add a SessionItem to the existing sessions.
     * @param item the session item to add
     */
    public void addSession(SessionItem item) {
        getExistingSessions().add(item);
    }

    /**
     * Get the existing students.
     * @return the existingStudents
     */
    public Set<StudentItem> getExistingStudents() {
        existingStudents = checkNull(existingStudents);
        return existingStudents;
    }

    /**
     * Add a StudentItem to the existing students.
     * @param item the student item to add
     */
    public void addStudent(StudentItem item) {
        getExistingStudents().add(item);
    }

    /**
     * Get the existing dataset levels.
     * @return the existingDatasetLevels
     */
    public Set<DatasetLevelItem> getExistingDatasetLevels() {
        existingDatasetLevels = checkNull(existingDatasetLevels);
        return existingDatasetLevels;
    }

    /**
     * Add a dataset level item to the existing dataset levels.
     * @param item the dataset level to add
     */
    public void addDatasetLevel(DatasetLevelItem item) {
        getExistingDatasetLevels().add(item);
    }

    /**
     * Get the existing problems.
     * @return the existingProblems
     */
    public Set<ProblemItem> getExistingProblems() {
        existingProblems = checkNull(existingProblems);
        return existingProblems;
    }

    /**
     * Add a new Problem to the set of existing problems.
     * @param item the problem item to add
     */
    public void addProblem(ProblemItem item) {
        getExistingProblems().add(item);
    }

    /**
     * Get the set of existing schools.
     * @return the existingSchools
     */
    public Set<SchoolItem> getExistingSchools() {
        existingSchools = checkNull(existingSchools);
        return existingSchools;
    }

    /**
     * Add a new school item to the set of existing schools.
     * @param item the school item to add
     */
    public void addSchool(SchoolItem item) {
        getExistingSchools().add(item);
    }

    /**
     * Get the existing classes.
     * @return the existingClasses
     */
    public Set<ClassItem> getExistingClasses() {
        existingClasses = checkNull(existingClasses);
        return existingClasses;
    }

    /**
     * Add a new class item to the set of existing classes.
     * @param item the class item to add
     */
    public void addClass(ClassItem item) {
        getExistingClasses().add(item);
    }
}
