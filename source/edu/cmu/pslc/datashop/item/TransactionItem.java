/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single transaction by the tutor.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12092 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-03-20 15:22:38 -0400 (Fri, 20 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TransactionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this tutor transaction. */
    private Long transactionId;
    /** Identifier guaranteed to be unique */
    private String guid;
    /** Subgoal Attempt associated with this transaction. */
    private SubgoalAttemptItem subgoalAttempt;
    /** Session associated with this transaction. */
    private SessionItem session;
    /** Student associated with this transaction. */
    private StudentItem student;
    /** Feedback associated with this transaction. */
    private FeedbackItem feedback;
    /** School associated with this transaction. */
    private SchoolItem school;
    /** Class associated with this transaction. */
    private ClassItem classItem;
    /** Subgoal associated with this transaction. */
    private SubgoalItem subgoal;
    /** Problem associated with this transaction. */
    private ProblemItem problem;
    /** Problem Event associated with this transaction */
    private ProblemEventItem problemEvent;
    /** Dataset associated with this transaction. */
    private DatasetItem dataset;
    /** The timestamp for when this transaction occurred. */
    private Date transactionTime;
    /** The transactionTimeMS of the timestamp saved as an integer. */
    private Integer transactionTimeMS;
    /** Time zone the timestamp was taken in. */
    private String timeZone;
    /** Conditions for this transaction. */
    private Set conditions;

    /**
     * The type of tutor transaction that was logged
     * (CYCLE, HINT, GLOSSARY, START_TUTOR, RESULT, HINT_MSG).
     */
    private String transactionTypeTutor;
    /**
     * The type of tool transaction that was logged
     * (CYCLE, HINT, GLOSSARY, START_TUTOR, ATTEMPT, HINT_REQUEST).
     */
    private String transactionTypeTool;
    /**
     * The subtype of tutor transaction that was logged.
     */
    private String transactionSubtypeTutor;
    /**
     * The subtype of tool transaction that was logged.
     */
    private String transactionSubtypeTool;
    /**
     * Outcome for this transaction
     * (OK, ERROR, BUG, INITIAL_HINT, NEXT_HINT, GLOSSARY_ITEM, CORRECT, INCORRECT).
     */
    private String outcome;
    /** Integer of which attempt at a subgoal this transaction recorded. <br>
     * Note: This is a count not an index, so the it will range from 1...n. */
    private Integer attemptAtSubgoal;
    /** Boolean indicating if transaction is the last attempt on a step for a student. */
    private Boolean isLastAttempt;
    /** Level of help this transaction recorded. */
    private Short helpLevel;
    /** Total number of hint requests. */
    private Short totalNumHints;
    /** The logged duration of the transaction. */
    private Integer duration;
    /** The sequence number logged for sequential problems. */
    private Integer probSolvingSequence;

    /** Collection of Samples associated with this transaction. */
    private Set samples;
    /** Collection of Skills (Knowledge Components) associated with this transaction. */
    private Set skills;
    /** Collection of CfTxLevel associated with this transaction. */
    private Set cfTxLevels;

    /** Default constructor. */
    public TransactionItem() {
    }

    /**
     *  Constructor with id.
     *  @param transactionId Database generated unique Id for this tutor transaction.
     */
    public TransactionItem(Long transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Get the id.
     * @return id as a Long
     */
    public Comparable getId() {
        return this.transactionId;
    }

    /**
     * Set the id.
     * @param transactionId Database generated unique Id for this tutor transaction.
     */
    public void setId(Long transactionId) {
        this.transactionId = transactionId;
    }

    /** Returns GUID. @return Returns the GUID. */
    public String getGuid() {
        return guid;
    }

    /** Set GUID. @param guid The GUID to set. */
    public void setGuid(String guid) {
        this.guid = guid;
    }
    /**
     * Get subgoalAttempt.
     * @return edu.cmu.pslc.datashop.item.SubgoalAttemptItem
     */
    public SubgoalAttemptItem getSubgoalAttempt() {
        return this.subgoalAttempt;
    }

    /**
     * Set subgoalAttempt.
     * @param subgoalAttempt Subgoal Attempt associated with this transaction.
     */
    public void setSubgoalAttempt(SubgoalAttemptItem subgoalAttempt) {
        this.subgoalAttempt = subgoalAttempt;
    }

    /**
     * Get session.
     * @return edu.cmu.pslc.datashop.item.SessionItem
     */
    public SessionItem getSession() {
        return this.session;
    }

    /**
     * Set session.
     * @param session Session associated with this transaction.
     */
    public void setSession(SessionItem session) {
        this.session = session;
    }

    /**
     * Get student.
     * @return edu.cmu.pslc.datashop.item.StudentItem
     */
    public StudentItem getStudent() {
        return this.student;
    }

    /**
     * Set student.
     * @param student Student associated with this transaction.
     */
    public void setStudent(StudentItem student) {
        this.student = student;
    }

    /**
     * Get feedback.
     * @return edu.cmu.pslc.datashop.item.FeedbackItem
     */
    public FeedbackItem getFeedback() {
        return this.feedback;
    }

    /**
     * Set feedback.
     * @param feedback Feedback associated with this transaction.
     */
    public void setFeedback(FeedbackItem feedback) {
        this.feedback = feedback;
    }

    /**
     * Get school.
     * @return edu.cmu.pslc.datashop.item.SchoolItem
     */
    public SchoolItem getSchool() {
        return this.school;
    }

    /**
     * Set school.
     * @param school School associated with this transaction.
     */
    public void setSchool(SchoolItem school) {
        this.school = school;
    }

    /**
     * Get classItem.
     * @return edu.cmu.pslc.datashop.item.ClassItem
     */
    public ClassItem getClassItem() {
        return this.classItem;
    }

    /**
     * Set classItem.
     * @param classItem Class associated with this transaction.
     */
    public void setClassItem(ClassItem classItem) {
        this.classItem = classItem;
    }

    /**
     * Get subgoal.
     * @return edu.cmu.pslc.datashop.item.SubgoalItem
     */
    public SubgoalItem getSubgoal() {
        return this.subgoal;
    }

    /**
     * Set subgoal.
     * @param subgoal Subgoal associated with this transaction.
     */
    public void setSubgoal(SubgoalItem subgoal) {
        this.subgoal = subgoal;
    }

    /**
     * Get problem.
     * @return edu.cmu.pslc.datashop.item.ProblemItem
     */
    public ProblemItem getProblem() {
        return this.problem;
    }

    /**
     * Set problem.
     * @param problem Problem associated with this transaction.
     */
    public void setProblem(ProblemItem problem) {
        this.problem = problem;
    }

    /**
     * Get Problem Event.
     * @return edu.cmu.pslc.datashop.item.ProblemEventItem
     */
    public ProblemEventItem getProblemEvent() {
        return this.problemEvent;
    }

    /**
     * Set problem event.
     * @param problemEvent Problem Event associated with this transaction
     */
    public void setProblemEvent (ProblemEventItem problemEvent) {
        this.problemEvent = problemEvent;
    }

    /**
     * Get dataset.
     * @return edu.cmu.pslc.datashop.item.DatasetItem
     */
    public DatasetItem getDataset() {
        return this.dataset;
    }

    /**
     * Set dataset.
     * @param dataset Dataset associated with this transaction.
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Get transactionTime.
     * @return java.util.Date
     */
    public Date getTransactionTime() {
        return this.transactionTime;
    }

    /**
     * Set transactionTime.
     * @param transactionTime The timestamp for when this transaction occurred.
     */
    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    /** Returns transactionTimeMS. @return Returns the transactionTimeMS. */
    public Integer getTransactionTimeMS() {
        return transactionTimeMS;
    }

    /** Set transactionTimeMS. @param transactionTimeMS The transactionTimeMS to set. */
    public void setTransactionTimeMS(Integer transactionTimeMS) {
        this.transactionTimeMS = transactionTimeMS;
    }

    /**
     * Get time zone.
     * @return java.lang.String
     */
    public String getTimeZone() {
        return this.timeZone;
    }

    /**
     * Set time zone.
     * @param timeZone Time zone the timestamp was taken in.
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Get transactionTypeTutor.
     * @return java.lang.String
     */
    public String getTransactionTypeTutor() {
        return this.transactionTypeTutor;
    }

    /**
     * Set transactionTypeTutor.
     * @param transactionTypeTutor The type of tutor transaction that was logged
     * (CYCLE, HINT, GLOSSARY, START_TUTOR, RESULT, HINT_MSG).
     */
    public void setTransactionTypeTutor(String transactionTypeTutor) {
        this.transactionTypeTutor = transactionTypeTutor;
    }

    /**
     * Get transactionTypeTool.
     * @return java.lang.String
     */
    public String getTransactionTypeTool() {
        return this.transactionTypeTool;
    }

    /**
     * Set transactionTypeTool.
     * @param transactionTypeTool The type of tool transaction that was logged
     * (CYCLE, HINT, GLOSSARY, START_TUTOR, ATTEMPT, HINT_REQUEST).
     */
    public void setTransactionTypeTool(String transactionTypeTool) {
        this.transactionTypeTool = transactionTypeTool;
    }

    /**
     * Get transactionSubtypeTutor.
     * @return java.lang.String
     */
    public String getTransactionSubtypeTutor() {
        return this.transactionSubtypeTutor;
    }

    /**
     * Set transactionSubtypeTutor.
     * @param transactionSubtypeTutor The subtype of tutor transaction that was logged.
     */
    public void setTransactionSubtypeTutor(String transactionSubtypeTutor) {
        this.transactionSubtypeTutor = transactionSubtypeTutor;
    }

    /**
     * Get transactionSubtypeTool.
     * @return java.lang.String
     */
    public String getTransactionSubtypeTool() {
        return this.transactionSubtypeTool;
    }

    /**
     * Set transactionSubtypeTool.
     * @param transactionSubtypeTool The subtype of tool transaction that was logged.
     */
    public void setTransactionSubtypeTool(String transactionSubtypeTool) {
        this.transactionSubtypeTool = transactionSubtypeTool;
    }

    /**
     * Get outcome.
     * @return java.lang.String
     */
    public String getOutcome() {
        return this.outcome;
    }

    /**
     * Set outcome.
     * @param outcome Outcome for this transaction.
     * (OK, ERROR, BUG, INITIAL_HINT, NEXT_HINT, GLOSSARY_ITEM, CORRECT, INCORRECT)
     */
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    /**
     * Get attemptAtSubgoal.
     * @return java.lang.Integer
     */
    public Integer getAttemptAtSubgoal() {
        return this.attemptAtSubgoal;
    }

    /**
     * Set attemptAtSubgoal.
     * @param attemptAtSubgoal Integer of which attempt at a subgoal this transaction recorded.
     */
    public void setAttemptAtSubgoal(Integer attemptAtSubgoal) {
        this.attemptAtSubgoal = attemptAtSubgoal;
    }

    /**
     * Get isLastAttempt.
     * @return Boolean
     */
    public Boolean getIsLastAttempt() {
        return this.isLastAttempt;
    }

    /**
     * Set isLastAttempt.
     * @param flag Boolean indicating if txn is last attempt on a step for a student.
     */
    public void setIsLastAttempt(Boolean flag) {
        this.isLastAttempt = flag;
    }

    /**
     * Get helpLevel.
     * @return Short
     */
    public Short getHelpLevel() {
        return this.helpLevel;
    }

    /**
     * Set helpLevel.
     * @param helpLevel Level of help this transaction recorded.
     */
    public void setHelpLevel(Short helpLevel) {
        this.helpLevel = helpLevel;
    }

    /**
     * Get totalNumHints.
     * @return Short
     */
    public Short getTotalNumHints() {
        return this.totalNumHints;
    }

    /**
     * Set totalNumHints.
     * @param totalNumHints Total number of hint requests.
     */
    public void setTotalNumHints(Short totalNumHints) {
        this.totalNumHints = totalNumHints;
    }

    /**
     * Get duration.
     * @return java.lang.Integer
     */
    public Integer getDuration() {
        return this.duration;
    }

    /**
     * Set duration.
     * @param duration The logged duration of the transaction.
     */
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     * Get probSolvingSequence.
     * @return java.lang.Integer
     */
    public Integer getProbSolvingSequence() {
        return this.probSolvingSequence;
    }

    /**
     * Set probSolvingSequence.
     * @param probSolvingSequence The sequence number logged for sequential problems.
     */
    public void setProbSolvingSequence(Integer probSolvingSequence) {
        this.probSolvingSequence = probSolvingSequence;
    }

    /**
     * Get samples.
     * @return Set
     */
    protected Set getSamples() {
        if (this.samples == null) {
            this.samples = new HashSet();
        }
        return this.samples;
    }

    /**
     * Public method to get sample.
     * @return a list instead of a set
     */
    public List getSamplesExternal() {
        List sortedList = new ArrayList(getSamples());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a sample.
     * @param item sample to add
     */
    public void addSample(SampleItem item) {
        if (!getSamples().contains(item)) {
            getSamples().add(item);
            //Note: this link is now uni-directional.
        }
    }

    /**
     * Remove a sample.
     * @param item sample to remove
     */
    public void removeSample(SampleItem item) {
        if (getSamples().contains(item)) {
            getSamples().remove(item);
            //item.removeTransaction(this);
        }
    }

    /**
     * Set sample.
     * @param samples Collection of Samples associated with this transaction.
     */
    public void setSamples(Set samples) {
        this.samples = samples;
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
        Collections.sort(sortedList, new SkillModelIdSort());
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a skill.
     * @param item skill to add
     */
    public void addSkill(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
            //Note: this link is now uni-directional.
        }
    }

    /**
     * Comparator class that is used to sort the skills for a given
     * transaction in order of the appearance of the headers (skill model id)
     * and then secondarily in alphabetical order.  This allows the transaction
     * export to not cycle over every skill item in the list for ever skill
     * model.
     *
     * Note: this comparator imposes orderings that are inconsistent with equals.
     * @author dspencer
     *
     */
    private class SkillModelIdSort implements Comparator<SkillItem> {
        /**
         * Inner class to compare two skill items.
         * @param s1 first skill item
         * @param s2 second skill item
         * @return the comparison
         */
        public int compare(SkillItem s1, SkillItem s2) {
            Long s1Id = (Long) s1.getSkillModel().getId();
            Long s2Id = (Long) s2.getSkillModel().getId();
            if (s1Id.longValue() < s2Id.longValue()) {
                return -1;
            } else if (s1Id.longValue() > s2Id.longValue()) {
                return 1;
            } else {
                return s1.getSkillName().compareTo(s2.getSkillName());
            }
        }
    }

    /**
     * Remove a skill.
     * @param item skill to remove
     */
    public void removeSkill(SkillItem item) {
        if (getSkills().contains(item)) {
            getSkills().remove(item);
            //item.removeTransaction(this);
        }
    }

    /**
     * Set skills.
     * @param skills Collection of Skills (Knowledge Components) associated with this transaction.
     */
    public void setSkills(Set skills) {
        this.skills = skills;
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
     * @param conditions Conditions for this problem.
     */
    protected void setConditions(Set conditions) {
        this.conditions = conditions;
    }

    /**
     * Add a condition.
     * @param item to add
     */
    public void addCondition(ConditionItem item) {
        if (!getConditions().contains(item)) {
            getConditions().add(item);
            //item.addTransaction(this);
        }
    }

    /**
     * remove a condition.
     * @param item to add
     */
    public void removeCondition(ConditionItem item) {
        if (getConditions().contains(item)) {
            getConditions().remove(item);
            item.removeTransaction(this);
        }
    }

    /**
     * Get cfTxLevel.
     * @return java.util.Set
     */
    protected Set getCfTxLevels() {
        if (this.cfTxLevels == null) {
            this.cfTxLevels = new HashSet();
        }
        return this.cfTxLevels;
    }

    /**
     * Public method to get cfTxLevel.
     * @return a list instead of a set
     */
    public List getCfTxLevelsExternal() {
        List sortedList = new ArrayList(getCfTxLevels());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a CfTxLevel.
     * @param item cfTxLevel to add
     */
    public void addCfTxLevel(CfTxLevelItem item) {
        if (!getCfTxLevels().contains(item)) {
            getCfTxLevels().add(item);
            item.setTransaction(this);
        }
    }

    /**
     * Set cfTxLevels.
     * @param cfTxLevels Collection of CfTxLevels associated with this transaction.
     */
    public void setCfTxLevels(Set cfTxLevels) {
        this.cfTxLevels = cfTxLevels;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToStringFK("SubgoalAttempt", getSubgoalAttempt()));
        buffer.append(objectToStringFK("Session", getSession()));
        buffer.append(objectToStringFK("Feedback", getFeedback()));
        buffer.append(objectToStringFK("School", getSchool()));
        buffer.append(objectToStringFK("Class", getClassItem()));
        buffer.append(objectToStringFK("Problem", getProblem()));
        buffer.append(objectToStringFK("Subgoal", getSubgoal()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToString("TransactionTime", getTransactionTime()));
        buffer.append(objectToString("TransactionTimeMS", getTransactionTimeMS()));
        buffer.append(objectToString("TimeZone", getTimeZone()));
        buffer.append(objectToString("TransactionTypeTutor", getTransactionTypeTutor()));
        buffer.append(objectToString("TransactionTypeTool", getTransactionTypeTool()));
        buffer.append(objectToString("TransactionSubtypeTutor", getTransactionSubtypeTutor()));
        buffer.append(objectToString("TransactionSubtypeTool", getTransactionSubtypeTool()));
        buffer.append(objectToString("Outcome", getOutcome()));
        buffer.append(objectToString("AttemptAtSubgoal", getAttemptAtSubgoal()));
        buffer.append(objectToString("IsLastAttempt", getIsLastAttempt()));
        buffer.append(objectToString("HelpLevel", getHelpLevel()));
        buffer.append(objectToString("TotalNumHints", getTotalNumHints()));
        buffer.append(objectToString("Duration", getDuration()));
        buffer.append(objectToString("ProbSolvingSequence", getProbSolvingSequence()));
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TransactionItem) {
            TransactionItem otherItem = (TransactionItem)obj;

            if (!objectEqualsFK(this.getSubgoalAttempt(), otherItem.getSubgoalAttempt())) {
                return false;
            }
            if (!objectEqualsFK(this.getSession(), otherItem.getSession())) {
                return false;
            }
            if (!objectEqualsFK(this.getFeedback(), otherItem.getFeedback())) {
                return false;
            }
            if (!objectEqualsFK(this.getSchool(), otherItem.getSchool())) {
                return false;
            }
            if (!objectEqualsFK(this.getClassItem(), otherItem.getClassItem())) {
                return false;
            }
            if (!objectEqualsFK(this.getProblem(), otherItem.getProblem())) {
                return false;
            }
            if (!objectEqualsFK(this.getSubgoal(), otherItem.getSubgoal())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                 return false;
            }
            if (!objectEquals(this.getTransactionTime(), otherItem.getTransactionTime())) {
                 return false;
            }
            if (!objectEquals(this.getTransactionTimeMS(), otherItem.getTransactionTimeMS())) {
                return false;
            }
            if (!objectEquals(this.getTimeZone(), otherItem.getTimeZone())) {
                 return false;
            }
            if (!objectEquals(this.getTransactionTypeTutor(),
                         otherItem.getTransactionTypeTutor())) {
                 return false;
            }
            if (!objectEquals(this.getTransactionTypeTool(), otherItem.getTransactionTypeTool())) {
                 return false;
            }
            if (!objectEquals(this.getTransactionSubtypeTutor(),
                        otherItem.getTransactionSubtypeTutor())) {
                return false;
           }
           if (!objectEquals(this.getTransactionSubtypeTool(),
                        otherItem.getTransactionSubtypeTool())) {
                return false;
           }
            if (!objectEquals(this.getOutcome(), otherItem.getOutcome())) {
                 return false;
            }
            if (!objectEquals(this.getAttemptAtSubgoal(), otherItem.getAttemptAtSubgoal())) {
                 return false;
            }
            if (!objectEquals(this.getIsLastAttempt(), otherItem.getIsLastAttempt())) {
                return false;
            }
            if (!objectEquals(this.getHelpLevel(), otherItem.getHelpLevel())) {
                 return false;
            }
            if (!objectEquals(this.getTotalNumHints(), otherItem.getTotalNumHints())) {
                 return false;
            }
            if (!objectEquals(this.getDuration(), otherItem.getDuration())) {
                 return false;
            }
            if (!objectEquals(this.getProbSolvingSequence(), otherItem.getProbSolvingSequence())) {
                 return false;
            }

            return true;
        }
        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSubgoalAttempt());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSession());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getFeedback());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSchool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getClassItem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSubgoal());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProblem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTransactionTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTransactionTimeMS());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeZone());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTransactionTypeTutor());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTransactionTypeTool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTransactionSubtypeTutor());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTransactionSubtypeTool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getOutcome());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAttemptAtSubgoal());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getIsLastAttempt());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHelpLevel());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTotalNumHints());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDuration());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProbSolvingSequence());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        TransactionItem otherItem = (TransactionItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getSubgoalAttempt(), otherItem.getSubgoalAttempt());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSession(), otherItem.getSession());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getFeedback(), otherItem.getFeedback());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSchool(), otherItem.getSchool());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getClassItem(), otherItem.getClassItem());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getProblem(), otherItem.getProblem());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSubgoal(), otherItem.getSubgoal());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionTime(), otherItem.getTransactionTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionTimeMS(), otherItem.getTransactionTimeMS());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeZone(), otherItem.getTimeZone());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionTypeTutor(),
                           otherItem.getTransactionTypeTutor());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionTypeTool(), otherItem.getTransactionTypeTool());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionSubtypeTutor(),
                           otherItem.getTransactionSubtypeTutor());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionSubtypeTool(),
                           otherItem.getTransactionSubtypeTool());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getOutcome(), otherItem.getOutcome());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAttemptAtSubgoal(), otherItem.getAttemptAtSubgoal());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getIsLastAttempt(), otherItem.getIsLastAttempt());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getHelpLevel(), otherItem.getHelpLevel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTotalNumHints(), otherItem.getTotalNumHints());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDuration(), otherItem.getDuration());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProbSolvingSequence(), otherItem.getProbSolvingSequence());
        if (value != 0) { return value; }

        return value;
    }
}
