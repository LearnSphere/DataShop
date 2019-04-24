/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A portion of a problem.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4592 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-03-28 15:58:16 -0400 (Fri, 28 Mar 2008) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SubgoalItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id for this subgoal. */
    private Long id;
    /** The name of the subgoal. */
    private String subgoalName;
    /** Identifier guaranteed to be unique within a given dataset */
    private String guid;
    /** String of the subgoal input cell type. */
    private String inputCellType;
    /** Problem associated with this subgoal. */
    private ProblemItem problem;
    /** Interpretation associated with this subgoal. */
    private InterpretationItem interpretation;
    /** Collection of selections associated with this subgoal. */
    private Set selections;
    /** Collection of actions associated with this subgoal. */
    private Set actions;
    /** Collection of inputs associated with this subgoal. */
    private Set inputs;
    /** Collection of attempts at this subgoal. */
    private Set subgoalAttempts;
    /** Collection of transactions associated with this attempt. */
    private Set transactions;
    /** Collection of skills (knowledge components) associated with this subgoal. */
    private Set skills;

    /** Default constructor. */
    public SubgoalItem() {
    }

    /**
     * Constructor with the identifier.
     * @param subgoalId the id.
     */
    public SubgoalItem(Long subgoalId) {
        this.id = subgoalId;
    }

    /**
     * Get subgoalId.
     * @return Long
     */

    public Long getSubgoalId() {
        return this.id;
    }

    /**
     * Return the unique identifier for this item.
     * @return The Long id as a comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
    * Set subgoalId.
    * @param id Database generated unique id for this subgoal.
    */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get subgoalName.
     * @return java.lang.String
     */

    public String getSubgoalName() {
        return this.subgoalName;
    }

    /**
     * Set subgoalName.
     * @param subgoalName The name of the subgoal.
     */
    public void setSubgoalName(String subgoalName) {
        this.subgoalName = subgoalName;
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
     * Get inputCellType.
     * @return java.lang.String
     */

    public String getInputCellType() {
        return this.inputCellType;
    }

    /**
     * Set inputCellType.
     * @param inputCellType String of the subgoal input cell type.
     */
    public void setInputCellType(String inputCellType) {
        this.inputCellType = inputCellType;
    }

    /**
     * Get problem.
     * @return ProblemItem the problem associated with this subgoal.
     */
    public ProblemItem getProblem() {
        return this.problem;
    }

    /**
     * Set problem.
     * @param problem the problem associated with this subgoal.
     */
    public void setProblem(ProblemItem problem) {
        this.problem = problem;
    }

    /**
     * Get interpretation.
     * @return InterpretationItem the interpretation associated with this subgoal.
     */
    public InterpretationItem getInterpretation() {
        return this.interpretation;
    }

    /**
     * Set interpretation.
     * @param interpretation the interpretation associated with this subgoal.
     */
    public void setInterpretation(InterpretationItem interpretation) {
        this.interpretation = interpretation;
    }

    /**
     * Get selections.
     * @return java.util.Set
     */

    protected Set getSelections() {
        if (this.selections == null) {
            this.selections = new HashSet();
        }
        return this.selections;
    }

    /**
     * Public method to get selections.
     * @return a list instead of a set
     */
    public List getSelectionsExternal() {
        List sortedItems = new ArrayList(getSelections());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set selections.
     * @param selections Collection of selections associated with this subgoal.
     */
    protected void setSelections(Set selections) {
        this.selections = selections;
    }

    /**
     * Adds a selection to this subgoals list of selections.
     * @param item the SelectionItem to add.
     */
    public void addSelection(SelectionItem item) {
        if (!getSelections().contains(item)) {
            getSelections().add(item);
            item.setSubgoal(this);
        }
    }

    /**
     * Get actions.
     * @return java.util.Set
     */
    protected Set getActions() {
        if (this.actions == null) {
            this.actions = new HashSet();
        }
        return this.actions;
    }

    /**
     * Public method to get selections.
     * @return a list instead of a set
     */
    public List getActionsExternal() {
        List sortedItems = new ArrayList(getActions());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set actions.
     * @param actions Collection of actions associated with this subgoal.
     */
    protected void setActions(Set actions) {
        this.actions = actions;
    }

    /**
     * Adds a action to this subgoals list of actions.
     * @param item to add.
     */
    public void addAction(ActionItem item) {
        if (!getActions().contains(item)) {
            getActions().add(item);
            item.setSubgoal(this);
        }
    }

    /**
     * Get inputs.
     * @return java.util.Set
     */

    protected Set getInputs() {
        if (this.inputs == null) {
            this.inputs = new HashSet();
        }
        return this.inputs;
    }

    /**
     * Public method to get inputs.
     * @return a list instead of a set
     */
    public List getInputsExternal() {
        List sortedItems = new ArrayList(getInputs());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set inputs.
     * @param inputs Collection of inputs associated with this subgoal.
     */
    protected void setInputs(Set inputs) {
        this.inputs = inputs;
    }

    /**
     * Adds an input to this subgoals list of inputs.
     * @param item to add.
     */
    public void addInput(InputItem item) {
        if (!getInputs().contains(item)) {
            getInputs().add(item);
            item.setSubgoal(this);
        }
    }

    /**
     * Get subgoalAttempts.
     * @return java.util.Set
     */

    protected Set getSubgoalAttempts() {
        if (this.subgoalAttempts == null) {
            this.subgoalAttempts = new HashSet();
        }
        return this.subgoalAttempts;
    }

    /**
     * Public method to get selections.
     * @return a list instead of a set
     */
    public List getSubgoalAttemptsExternal() {
        List sortedItems = new ArrayList(getSubgoalAttempts());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set subgoalAttempts.
     * @param subgoalAttempts Collection of attempts at this subgoal.
     */
    protected void setSubgoalAttempts(Set subgoalAttempts) {
        this.subgoalAttempts = subgoalAttempts;
    }

    /**
     * Adds a SubgoalAttempt to this subgoals list of SubgoalAttempts.
     * @param item to add.
     */
    public void addSubgoalAttempt(SubgoalAttemptItem item) {
        if (!getSubgoalAttempts().contains(item)) {
            getSubgoalAttempts().add(item);
            item.setSubgoal(this);
        }
    }

    /**
     * Get transactions.
     * @return java.util.Set
     */
    protected Set getTransactions() {
        if (this.transactions == null) {
            this.transactions = new HashSet();
        }
        return this.transactions;
    }

    /**
     * Public method to get transactions.
     * @return a list instead of a set
     */
    public List getTransactionsExternal() {
        List sortedItems = new ArrayList(getTransactions());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set transactions.
     * @param transactions Collection of transactions associated with this attempt.
     */
    protected void setTransactions(Set transactions) {
        this.transactions = transactions;
    }

    /**
     * Adds a Transaction to this subgoals list of Transactions.
     * @param item to add.
     */
    public void addTransaction(TransactionItem item) {
        if (!getTransactions().contains(item)) {
            getTransactions().add(item);
            item.setSubgoal(this);
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
        List sortedItems = new ArrayList(getSkills());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set skills.
     * @param skills Collection of skills (knowledge components) associated with this subgoal.
     */
    protected void setSkills(Set skills) {
        this.skills = skills;
    }

    /**
     * Adds a Skill to this subgoals list of Skills.
     * @param item to add.
     */
    public void addSkill(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
            item.addSubgoal(this);
        }
    }

    /**
     * Adds a Skill to this subgoal, but does not finish the bi-directional add..
     * use this function ONLY when doing fast inserts w/o reads.
     * @param item to add.
     */
    public void addSkillFast(SkillItem item) {
        if (!getSkills().contains(item)) {
            getSkills().add(item);
        }
    }

    /**
     * Removes a skill.
     * @param item to remove.
     */
    public void removeSkill(SkillItem item) {
        if (getSkills().contains(item)) {
            getSkills().remove(item);
            item.removeSubgoal(this);
        }
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
         buffer.append(objectToString("subgoalId", getId()));
         buffer.append(objectToString("guid", getGuid()));
         buffer.append(objectToString("subgoalName", getSubgoalName()));
         buffer.append(objectToString("inputCellType", getInputCellType()));
         buffer.append(objectToStringFK("problemId", getProblem()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
    * Equals function for this class.
    * @param obj Object of any type, should be an Item for equality check
    * @return boolean true if the items are equal, false if not
    */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SubgoalItem) {
            SubgoalItem otherItem = (SubgoalItem)obj;

            if (!objectEqualsFK(this.getProblem(), otherItem.getProblem())) {
                return false;
            }

            if (!objectEquals(this.getSubgoalName(), otherItem.getSubgoalName())) {
                return false;
            }

            if (!objectEquals(this.getGuid(), otherItem.getGuid())) {
                return false;
            }

            if (!objectEquals(this.getInputCellType(), otherItem.getInputCellType())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getProblem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSubgoalName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getInputCellType());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>problem</li>
     * <li>name</li>
     * <li>input cell type</li>
     * <li>GUID</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SubgoalItem otherItem = (SubgoalItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getProblem(), otherItem.getProblem());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSubgoalName(), otherItem.getSubgoalName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInputCellType(), otherItem.getInputCellType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getGuid(), otherItem.getGuid());
        if (value != 0) { return value; }

        return value;
    }

}