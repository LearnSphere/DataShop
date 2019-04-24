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
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.ClassItem;
import edu.cmu.pslc.datashop.item.SessionItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.InstructorItem;

/**
 * Data object that contains all fields for context message for version 4
 * and curriculum message for version 2.
 *
 * @author Hui cheng
 * @version $Revision: 12083 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-03-13 10:55:45 -0400 (Fri, 13 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ContextMessage extends DTO {
    /** The context_id, previously attempt_id. */
    private String contextId;
    /** The name. */
    private String name;
    /** The anonFlag. */
    private Boolean anonFlag;
    /** The dataset which contains set of dataset level and name. */
    private DatasetItem datasetItem;
    /** The session. */
    private SessionItem sessionItem;
    /** The school.*/
    private SchoolItem schoolItem;
    /** The class which contains name, period and description.*/
    private ClassItem classItem;
    /** The instructor. This later may need to permit multiple instructors*/
    private InstructorItem instructorItem;
    /** The student.*/
    private StudentItem studentItem;
    /** The problem.  There can't be more than one, according to the DTD. */
    private ProblemItem problemItem;
    /**
     * Collection of custom fields associated with this context message.
     * Using a Map to avoid duplicate names. First one in wins.
     */
    private Map customFields;
    /** Collection of conditions associated with this contextMessage. */
    private Set conditions;
    /** Collection of skills associated with this contextMessage. */
    private Set skills;
    /** Flag used for setting 'Appears Anonymous' to N/A. */
    private Boolean appearsAnonIsNA = null;

    /** Default constructor. */
    public ContextMessage() {
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

    /** The getter for name.
     * @return The name.
     */
    public String getName () {
        return name;
    }

    /** The setter for name.
     * @param name String.
     */
    public void setName (String name) {
        this.name = name;
    }

    /**
     * Returns the anonFlag.
     * @return the anonFlag
     */
    public Boolean getAnonFlag() {
        return anonFlag;
    }

    /**
     * Sets the anonFlag.
     * @param anonFlag The anonFlag to set.
     */
    public void setAnonFlag(Boolean anonFlag) {
        this.anonFlag = anonFlag;
    }

    /** The getter for datasetItem.
     * @return The datasetItem.
     */
    public DatasetItem getDatasetItem () {
        return datasetItem;
    }

    /** The setter for datasetItem.
     * @param datasetItem DatasetItem.
     */
    public void setDatasetItem (DatasetItem datasetItem) {
        this.datasetItem = datasetItem;
    }

    /** The getter for sessionItem.
     * @return The sessionItem.
     */
    public SessionItem getSessionItem () {
        return sessionItem;
    }

    /** The setter for sessionItem.
     * @param sessionItem SessionItem.
     */
    public void setSessionItem (SessionItem sessionItem) {
        this.sessionItem = sessionItem;
    }

    /** The getter for schoolItem.
     * @return The schoolItem.
     */
    public SchoolItem getSchoolItem () {
        return schoolItem;
    }

    /** The setter for schoolItem.
     * @param schoolItem SchoolItem.
     * */
    public void setSchoolItem (SchoolItem schoolItem) {
        this.schoolItem = schoolItem;
    }

    /** The getter for classItem.
     * @return The classItem.
     */
    public ClassItem getClassItem () {
        return classItem;
    }

    /** The setter for classItem.
     *  @param classItem ClassItem.
     * */
    public void setClassItem (ClassItem classItem) {
        this.classItem = classItem;
    }

    /** The getter for instructorItem.
     * @return instructorItem.
     */
    public InstructorItem getInstructorItem () {
        return instructorItem;
    }

    /** The setter for instructorItem.
     * @param instructorItem InstructorItem.
     */
    public void setInstructorItem (InstructorItem instructorItem) {
        this.instructorItem = instructorItem;
    }

    /** The getter for studentItem.
     * @return StudentItem.
     */
    public StudentItem getStudentItem () {
        return studentItem;
    }

    /** The setter for studentItem.
     * @param studentItem studentItem.
     */
    public void setStudentItem(StudentItem studentItem) {
        this.studentItem = studentItem;
    }

    /** The getter for problemItem.
     * @return ProblemItem.
     */
    public ProblemItem getProblemItem () {
        return problemItem;
    }

    /** The setter for problemItem.
     * @param problemItem problemItem.
     */
    public void setProblemItem(ProblemItem problemItem) {
        this.problemItem = problemItem;
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
     * @param customFields Collection of problems associated with this level.
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
     * Get conditions.
     * @return java.util.Set
     */
    protected Set getConditions() {
        if (this.conditions == null) {
            this.conditions = new HashSet();
        }
        return this.conditions;
    }

    /**
     * Public method to get conditions.
     * @return a list instead of a set
     */
    public List getConditionsExternal() {
        List sortedList = new ArrayList(getConditions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set conditions.
     * @param conditions Collection of conditions associated with this message.
     */
    protected void setConditions(Set conditions) {
        this.conditions = conditions;
    }

    /**
     * Add a conditions.
     * @param item to add
     */
    public void addCondition(ConditionItem item) {
        if (!getConditions().contains(item)) {
            getConditions().add(item);
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
     * @return a list instead of a set
     */
    public List getSkillsExternal() {
        List sortedList = new ArrayList(getSkills());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set skills.
     * @param skills Collection of skills associated with this message.
     */
    protected void setSkills(Set skills) {
        this.skills = skills;
    }

    /**
     * Add a skills.
     * @param item to add
     */
    public void addSkill(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
        }
    }

    /**
     * Returns the appearsAnonIsNA flag.
     * @return appearsAnonIsNA
     */
    public Boolean getAppearsAnonIsNA() {
        return appearsAnonIsNA;
    }

    /**
     * Sets the appearsAnonIsNA flag.
     * @param value to value to set.
     */
    public void setAppearsAnonIsNA(Boolean value) {
        this.appearsAnonIsNA = value;
    }
}
