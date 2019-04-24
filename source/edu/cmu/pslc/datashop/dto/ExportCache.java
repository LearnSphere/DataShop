/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.cmu.pslc.datashop.item.ClassItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CfTxLevelId;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.InstructorItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.SessionItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * This class is used in the export process to store information particular
 * to each export thread.  Created to move the variables out of the ExportHelper
 * class, since it is a singleton.
 * @author Kyle Cunningham
 * @version $Revision: 9631 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2013-07-18 11:28:05 -0400 (Thu, 18 Jul 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExportCache implements Serializable {

    /** Max number of students for transactions in the dataset.*/
    private Long maxStudentCount;
    /** Max number of ConditionItems for transactions in the dataset.*/
    private Long maxConditionCount;
    /** Max number of AttemptInputSelections for transactions in the dataset.*/
    private Long maxSelectionCount;
    /** Max number of AttemptInputActions for transactions in the dataset.*/
    private Long maxActionCount;
    /** Max number of AttemptInputItems for transactions in the dataset.*/
    private Long maxInputCount;
    /** Number of custom fields in the dataset */
    private Long numCustomFields;
    /** Level Titles for use in listHeader().*/
    private List datasetLevelTitles;
    /** Inverted list of titles for the dataset */
    private List invertedDatasetLevelTitles;
    /** List of unique names for the custom fields */
    private List customFieldNames;

    /** Holds the classes keyed by their database ID. */
    private HashMap classes;
    /** Map of ConditionItems keyed by condition id for the dataset*/
    private HashMap conditions;
    /** Holds the CfTxLevelItems keyed by their CfTxLevelId.*/
    private HashMap cfTxLevels;
    /** Map of DatasetLevels keyed by level id for the dataset */
    private HashMap datasetLevels;
    /** Map of InstructorItems keyed by their database ID. */
    private HashMap instructors;
    /** Holds the ProblemItems keyed by their database ID.*/
    private HashMap problems;
    /** Holds of the SchoolItems keyed by their database ID. */
    private HashMap schools;
    /** Holds the identifiers of skills mapped to each transaction id.*/
    private HashMap skills;
    /** Holds the studentItems keyed by their database ID. */
    private HashMap students;
    /** Holds the SessionItems keyed by their database ID. */
    private HashMap sessions;
    /** Holds SkillModelNames and the max skill count per model. */
    private HashMap skillCounts;
    /** Holds the number of delimiters before problem. */
    private Integer numDelimitersBeforeProblem;

    /** SkillModel name.*/
    private List skillModelNames;

    /** Merged skill model names. */
    private List<String> mergedSkillModelNames;

    /** Merged skill counts. */
    private List<Long> mergedSkillCounts;

    /** Default Constructor. */
    public ExportCache() {
        // we only want to create the skillCounts hash once
        this.skillCounts = new HashMap <String, Long> ();
        this.skillModelNames = new ArrayList();
        this.mergedSkillModelNames = new ArrayList<String>();
        this.mergedSkillCounts = new ArrayList<Long>();
        init();
    }

    /** Initialize the hash maps. */
    public void init() {
        this.datasetLevels = new HashMap <Integer, DatasetLevelItem>();
        this.conditions = new HashMap <Long, ConditionItem>();
        this.skills = new HashMap <Long, SkillItem>();
        this.classes = new HashMap <Long, ClassItem>();
        this.cfTxLevels = new HashMap <CfTxLevelId, CfTxLevelItem>();
        this.instructors = new HashMap <Long, InstructorItem>();
        this.problems = new HashMap <Long, ProblemItem>();
        this.schools = new HashMap <Integer, SchoolItem>();
        this.sessions = new HashMap <Long, SessionItem>();
        this.students = new HashMap <Long, StudentItem>();
        this.numDelimitersBeforeProblem = new Integer(0);
    }

    /** Method to clear the cache. */
    public void clearTheCache() {
        this.datasetLevels.clear();
        this.conditions.clear();
        this.skills.clear();
        this.classes.clear();
        this.cfTxLevels.clear();
        this.instructors.clear();
        this.problems.clear();
        this.schools.clear();
        this.sessions.clear();
        this.students.clear();
        this.numDelimitersBeforeProblem = 0;
    }

    /**
     * Returns the list of cfTxLevelItem.
     * @return the cfTxlevels
     */
    public HashMap getCfTxLevelsMap() {
        return cfTxLevels;
    }

    /**
     * Returns the list of custom field names.
     * @param key the CfTxLevelId id.
     * @return the CfTxLevelItem.
     */
    public CfTxLevelItem getCfTxLevel(CfTxLevelId key) {
        return (CfTxLevelItem)cfTxLevels.get(key);
    }

    /**
     * Sets the list of cfTxLevels.
     * @param cfTxLevels the cfTxLevels to set
     */
    public void setCfTxLevels(HashMap cfTxLevels) {
        this.cfTxLevels = cfTxLevels;
    }

    /**
     * Inserts a (CfTxLevel.id, CfTxlevelItem) pair into
     * the cfTxLevel hash map.
     * @param item the CfTxLevelItem to add
     */
    public void addToCfTxLevels(CfTxLevelItem item) {
        this.cfTxLevels.put(item.getId(), item);
    }


    /** Returns customFieldNames. @return Returns the customFieldNames. */
    public List getCustomFieldNames() {
        return customFieldNames;
    }

    /** Set customFieldNames. @param customFieldNames The customFieldNames to set. */
    public void setCustomFieldNames(List customFieldNames) {
        this.customFieldNames = customFieldNames;
        setNumCustomFields(new Long(this.customFieldNames.size()));
    }

    /** Returns the map of datasetLevels. @return the datasetLevels HashMap */
    public HashMap getDatasetLevelsMap() { return datasetLevels; }

    /**
     * Returns the map of datasetLevels.
     * @param key the datasetLevelItem id.
     * @return the DatasetLevelItem.
     */
    public DatasetLevelItem getDatasetLevel(Integer key) {
        return (DatasetLevelItem)datasetLevels.get(key);
    }

    /**
     * Sets the list of custom field names.
     * @param datasetLevelsMap the datasetLevelsMap to set
     */
    public void setDatasetLevels(HashMap datasetLevelsMap) {
        this.datasetLevels = datasetLevelsMap;
    }

    /**
     * Inserts a (DatasetLevelItem.id, DatasetLevelItem) pair into
     * the datasetLevels hash map.
     * @param item the DatasetLevelItem to add
     */
    public void addToDatasetLevels(DatasetLevelItem item) {
        this.datasetLevels.put(item.getId(), item);
    }

    /** Returns the map of problems. @return the problems HashMap */
    public HashMap getProblemsMap() { return problems; }

    /**
     * Returns the map of problems.
     * @param key the problemItem id.
     * @return the ProblemItem.
     */
    public ProblemItem getProblem(Long key) { return (ProblemItem)problems.get(key); }

    /**
     * Sets the list of custom field names.
     * @param problemsMap the problemsMap to set
     */
    public void setProblems(HashMap problemsMap) { this.problems = problemsMap; }

    /**
     * Inserts a (ProblemItem.id, ProblemItem) pair into
     * the problems hash map.
     * @param item the ProblemItem to add
     */
    public void addToProblems(ProblemItem item) { this.problems.put(item.getId(), item); }

    /**
     * Returns the list of dataset level titles.
     * @return the datasetLevelTitles
     */
    public List getDatasetLevelTitles() { return datasetLevelTitles; }

    /**
     * Sets the list of dataset level titles.
     * @param datasetLevelTitles the datasetLevelTitles to set
     */
    public void setDatasetLevelTitles(List datasetLevelTitles) {
        this.datasetLevelTitles = datasetLevelTitles;
    }

    /**
     * Returns the maximum action count.
     * @return the maxActionCount
     */
    public Long getMaxActionCount() { return maxActionCount; }

    /**
     * Sets the maximum action count.
     * @param maxActionCount the maxActionCount to set
     */
    public void setMaxActionCount(Long maxActionCount) {
        if (maxActionCount == 0) { maxActionCount = new Long(1); }
        this.maxActionCount = maxActionCount;
    }

    /**
     * Returns the maximum condition count.
     * @return the maxConditionCount
     */
    public Long getMaxConditionCount() { return maxConditionCount; }

    /**
     * Sets the maximum condition count.
     * @param maxConditionCount the maxConditionCount to set
     */
    public void setMaxConditionCount(Long maxConditionCount) {
        this.maxConditionCount = maxConditionCount;
    }

    /**
     * Returns the hash of conditions for this dataset.  Condition_id
     * serves at the key for each ConditionItem.
     * @return the conditions
     */
    public HashMap getConditionsMap() { return conditions; }

    /**
     * Sets the condition map.
     * @param conditions the conditions to set
     */
    public void setConditions(HashMap conditions) { this.conditions = conditions; }

    /**
     * Add a ConditionItem mapping to the conditions map.
     * @param item the ConditionItem to store in the hash.
     */
    public void addToConditions(ConditionItem item) {
        this.conditions.put((Long)item.getId(), item);
    }

    /**
     * Returns the condition item for a given condition id.
     * @param id Long of the condition Id.
     * @return ConditionItem for the id.
     */
    public ConditionItem getCondition(Long id) { return (ConditionItem)conditions.get(id); }

    /** Returns the maximum input count. @return the maxInputCount */
    public Long getMaxInputCount() { return maxInputCount; }

    /**
     * Sets the maximum input count.
     * @param maxInputCount the maxInputCount to set
     */
    public void setMaxInputCount(Long maxInputCount) {
        if (maxInputCount == 0) { maxInputCount = new Long(1); }
        this.maxInputCount = maxInputCount;
    }

    /** Sets the maximum selection count. @return the maxSelectionCount */
    public Long getMaxSelectionCount() { return maxSelectionCount; }

    /**
     * Returns the maximum selection count.
     * @param maxSelectionCount the maxSelectionCount to set
     */
    public void setMaxSelectionCount(Long maxSelectionCount) {
        if (maxSelectionCount == 0) { maxSelectionCount = new Long(1); }
        this.maxSelectionCount = maxSelectionCount;
    }

    /**
     * Add a skillModelName, maxSkillCount pair to the skillCount map.
     * @param skillModelName the skillModelName to serve as the key.
     * @param maxSkillCount the maxSkillCount for the given skillModelName.
     */
    public void addToSkillCounts(String skillModelName, Long maxSkillCount) {
        this.skillCounts.put(skillModelName, maxSkillCount);
    }

    /**
     * Returns the skill count hash map.
     * @return the skillCounts hash map.
     */
    public HashMap getSkillCounts() {
        return this.skillCounts;
    }

    /**
     * Returns the maximum student count.
     * @return the maxStudentCount
     */
    public Long getMaxStudentCount() { return maxStudentCount; }

    /**
     * Sets the maximum student count.
     * @param maxStudentCount the maxStudentCount to set
     */
    public void setMaxStudentCount(Long maxStudentCount) {
        this.maxStudentCount = maxStudentCount;
    }

    /**
     * Returns the mapping of skills for a dataset and skill model.
     * SkillItems are keyed by their skill_id.
     * @return the skills
     */
    public HashMap getSkillsMap() { return skills; }

    /**
     * Returns the skill item for the given id.
     * @param key the SkillItem id.
     * @return the SkillItem.
     */
    public SkillItem getSkill(Long key) { return (SkillItem)skills.get(key); }

    /** Sets the skill map. @param skills the skills to set */
    public void setSkills(HashMap skills) { this.skills = skills; }

    /**
     * Add a SkillItem mapping to the skills map.
     * @param item the SkillItem to store in the hash.
     */
    public void addToSkills(SkillItem item) {
        this.skills.put((Long)item.getId(), item);
    }

    /**
     * Add a skill model name to the list of skill model names.
     * @param skillModelName the skill model name to add.
     */
    public void addToSkillModelNames(String skillModelName) {
        this.skillModelNames.add(skillModelName);
    }

    /** Returns the skill model name. @return the skillModelName */
    public List getSkillModelNames() { return skillModelNames; }

    /**
     * Sets the skill model names.
     * @param skillModelNames the skillModelName to set
     */
    public void setSkillModelNames(List skillModelNames) {
        this.skillModelNames = skillModelNames;
    }

    /** Returns numCustomFields. @return Returns the numCustomFields. */
    public Long getNumCustomFields() {
        return numCustomFields;
    }

    /** Set numCustomFields. @param numCustomFields The numCustomFields to set. */
    public void setNumCustomFields(Long numCustomFields) {
        this.numCustomFields = numCustomFields;
    }

    /**
     * Returns the mapping of students for a dataset and student model.
     * StudentItems are keyed by their student_id.
     * @return the students
     */
    public HashMap getStudentsMap() { return students; }

    /**
     * Returns the student item for the given id.
     * @param key the StudentItem id.
     * @return the StudentItem.
     */
    public StudentItem getStudent(Long key) { return (StudentItem)students.get(key); }

    /** Sets the student map. @param students the students to set */
    public void setStudents(HashMap students) { this.students = students; }

    /**
     * Add a StudentItem mapping to the students map.
     * @param item the StudentItem to store in the hash.
     */
    public void addToStudents(StudentItem item) {
        this.students.put((Long)item.getId(), item);
    }

    /**
     * Returns the mapping of sessions for a dataset and session model.
     * SessionItems are keyed by their session_id.
     * @return the sessions
     */
    public HashMap getSessionsMap() { return sessions; }

    /**
     * Returns the session item for the given id.
     * @param key the SessionItem id.
     * @return the SessionItem.
     */
    public SessionItem getSession(Long key) { return (SessionItem)sessions.get(key); }

    /** Sets the session map. @param sessions the sessions to set */
    public void setSessions(HashMap sessions) { this.sessions = sessions; }

    /**
     * Add a SessionItem mapping to the sessions map.
     * @param item the SessionItem to store in the hash.
     */
    public void addToSessions(SessionItem item) {
        this.sessions.put((Long)item.getId(), item);
    }

    /**
     * Returns the mapping of classes for a dataset and class model.
     * ClassItems are keyed by their class_id.
     * @return the classes
     */
    public HashMap getClassesMap() { return classes; }

    /**
     * Returns the class item for the given id.
     * @param key the ClasseItem id.
     * @return the ClasseItem.
     */
    public ClassItem getClassItem(Long key) { return (ClassItem)classes.get(key); }

    /** Sets the class map. @param classes the classes to set */
    public void setClasses(HashMap classes) { this.classes = classes; }

    /**
     * Add a ClassItem mapping to the classes map.
     * @param item the ClassItem to store in the hash.
     */
    public void addToClasses(ClassItem item) {
        this.classes.put((Long)item.getId(), item);
    }

    /**
     * Returns the mapping of school for a dataset and school model.
     * SchoolItems are keyed by their school_id.
     * @return the school
     */
    public HashMap getSchoolMap() { return schools; }

    /**
     * Returns the school item for the given id.
     * @param key the SchooleItem id.
     * @return the SchooleItem.
     */
    public SchoolItem getSchool(Integer key) { return (SchoolItem)schools.get(key); }

    /** Sets the school map. @param school the school to set */
    public void setSchool(HashMap school) { this.schools = school; }

    /**
     * Add a SchoolItem mapping to the schools map.
     * @param item the SchoolItem to store in the hash.
     */
    public void addToSchool(SchoolItem item) {
        this.schools.put((Integer)item.getId(), item);
    }

    /**
     * Returns the mapping of instructor for a dataset and instructor model.
     * InstructorItems are keyed by their instructor_id.
     * @return the instructor
     */
    public HashMap getInstructorMap() { return instructors; }

    /**
     * Returns the instructor item for the given id.
     * @param key the InstructoreItem id.
     * @return the InstructoreItem.
     */
    public InstructorItem getInstructor(Long key) {
        return (InstructorItem)instructors.get(key);
    }

    /** Sets the instructor map. @param instructor the instructor to set */
    public void setInstructor(HashMap instructor) { this.instructors = instructor; }

    /**
     * Add a InstructorItem mapping to the instructors map.
     * @param item the InstructorItem to store in the hash.
     */
    public void addToInstructor(InstructorItem item) {
        this.instructors.put((Long)item.getId(), item);
    }

    /**
     * Creates a list of the dataset level titles in reverse order.
     * Used during the processing of dataset levels.
     * @return the inverted list of dataset level titles
     */
    public List getInvertedDatasetLevelTitles() {
        if (invertedDatasetLevelTitles == null) {
            invertedDatasetLevelTitles = new ArrayList ();
            invertedDatasetLevelTitles.addAll(datasetLevelTitles);
            Collections.reverse(invertedDatasetLevelTitles);
        }
        return invertedDatasetLevelTitles;
    }

    /** Returns numDelimitersBeforeProblem. @return Returns the numDelimitersBeforeProblem. */
    public Integer getNumDelimitersBeforeProblem() {
        return numDelimitersBeforeProblem;
    }

    /**
     * Set numDelimitersBeforeProblem.
     * @param numDelimitersBeforeProblem The numDelimitersBeforeProblem to set.
     */
    public void setNumDelimitersBeforeProblem(Integer numDelimitersBeforeProblem) {
        this.numDelimitersBeforeProblem = numDelimitersBeforeProblem;
    }

    /**
     * Set the merged max skill counts sorted by skill model id ascending.
     * @param mergedSkillCounts ordered by skill model id
     */
    public void setMergedSkillCounts(List<Long> mergedSkillCounts) {
        this.mergedSkillCounts = mergedSkillCounts;
    }
    /**
     * Get the sorted merged skill counts of the selected samples.
     * @return List List of max of value column in sample_metric
     * for each skill model.
     */
    public List<Long> getMergedSkillCounts() {
        return this.mergedSkillCounts;
    }

    /**
     * Set the merged skill model names.
     * @param mergedSkillModelNames ordered by skill model id
     */
    public void setMergedSkillModelNames(List<String> mergedSkillModelNames) {
        this.mergedSkillModelNames = mergedSkillModelNames;
    }

    /**
     * Get the merged skill model names.
     * @return List Skill Model Names of the SampleMetricItems with the maximum
     * value for that skill model id.
     */
    public List<String> getMergedSkillModelNames() {
        return this.mergedSkillModelNames;
    }
}
