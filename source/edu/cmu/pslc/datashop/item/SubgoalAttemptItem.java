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
 * An attempt at a subgoal.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SubgoalAttemptItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id for this Subgoal Attempt. */
    private Long id;
    /** Subgoal this attempt is associated with. */
    private SubgoalItem subgoal;
    /** Flag indicating correctness of this attempt. */
    private String correctFlag;
    /** Collection of selection attempts associated with this subgoal. */
    private Set attemptSelections;
    /** Collection of action attempts associated with this subgoal. */
    private Set attemptActions;
    /** Collection of input attempts associated with this subgoal. */
    private Set attemptInputs;
    /** Collection of feedbacks associated with this attempt. */
    private Set feedbacks;
    /** Collection of transactions associated with this attempt. */
    private Set tutorTransactions;
    /** Collection of interpretations associated with this attempt. */
    private Set interpretationAttempts;

    /** Collection of allowed values in the correct_flag field enumeration in the database. */
    //TODO see CorrectFlag class, maybe that can be used here instead?  seems duplicated
    private static final List CORRECT_FLAG_ENUM = new ArrayList();
    /** Correct Flag enumeration field value - "correct". */
    public static final String CORRECT_FLAG_CORRECT = "correct";
    /** Correct Flag enumeration field value - "incorrect". */
    public static final String CORRECT_FLAG_INCORRECT = "incorrect";
    /** Correct Flag enumeration field value - "hint". */
    public static final String CORRECT_FLAG_HINT = "hint";
    /** Correct Flag enumeration field value - "unknown". */
    public static final String CORRECT_FLAG_UNKNOWN = "unknown";
    /** Correct Flag enumeration field value - "untutored". */
    public static final String CORRECT_FLAG_UNTUTORED = "untutored";
    /* Adds each level to the level enumeration list */
    static {
        CORRECT_FLAG_ENUM.add(CORRECT_FLAG_CORRECT);
        CORRECT_FLAG_ENUM.add(CORRECT_FLAG_INCORRECT);
        CORRECT_FLAG_ENUM.add(CORRECT_FLAG_HINT);
        CORRECT_FLAG_ENUM.add(CORRECT_FLAG_UNKNOWN);
        CORRECT_FLAG_ENUM.add(CORRECT_FLAG_UNTUTORED);
    }

    /** Default constructor. */
    public SubgoalAttemptItem() {
    }

    /**
     *  Constructor with id.
     *  @param subgoalAttemptId Database generated unique id for this Subgoal Attempt.
     */
    public SubgoalAttemptItem(Long subgoalAttemptId) {
        this.id = subgoalAttemptId;
    }

    /**
     * Get subgoalAttemptId.
     * @return the Long id as a comparable
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set subgoalAttemptId.
     * @param subgoalAttemptId Database generated unique id for this Subgoal Attempt.
     */
    public void setId(Long subgoalAttemptId) {
        this.id = subgoalAttemptId;
    }

    /**
     * Get subgoal.
     * @return the subgoal item
     */
    public SubgoalItem getSubgoal() {
        return this.subgoal;
    }

    /**
     * Set subgoal.
     * @param subgoal Subgoal this attempt is associated with.
     */
    public void setSubgoal(SubgoalItem subgoal) {
        this.subgoal = subgoal;
    }

    /**
     * Get correctFlag.
     * @return the correct flag
     */
    public String getCorrectFlag() {
        return this.correctFlag;
    }

    /**
     * Set correctFlag, but allow a variety of strings in either upper or lower case.
     * @param correctFlag Flag indicating correctness of this attempt.
     */
    public void setCorrectFlag(String correctFlag) {
        if (CORRECT_FLAG_ENUM.contains(correctFlag)) {
            this.correctFlag = correctFlag;
            return;
        }
        String lowerCase = correctFlag.toLowerCase();
        if ((lowerCase.indexOf("hint") != -1)
         || (lowerCase.indexOf("help") != -1)) {
            this.correctFlag = CORRECT_FLAG_HINT;
        } else if ((lowerCase.equals("ok"))
                || (lowerCase.equals("correct"))) {
            this.correctFlag = CORRECT_FLAG_CORRECT;
        } else if ((lowerCase.equals("error"))
                || (lowerCase.equals("bug"))
                || (lowerCase.equals("incorrect"))) {
            this.correctFlag = CORRECT_FLAG_INCORRECT;
        } else {
            this.correctFlag = CORRECT_FLAG_UNKNOWN;
        }
    }

    /**
     * Get attemptSelections.
     * @return the set of attempt selections or an empty set
     */
    protected Set getAttemptSelections() {
        if (this.attemptSelections == null) {
            this.attemptSelections = new HashSet();
        }
        return this.attemptSelections;
    }

    /**
     * Public method to get attempt selections.
     * @return a list instead of a set
     */
    public List getAttemptSelectionsExternal() {
        List sortedList = new ArrayList(getAttemptSelections());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set attemptSelections.
     * @param attemptSelections Collection of selection attempts associated with this subgoal.
     */
    protected void setAttemptSelections(Set attemptSelections) {
        this.attemptSelections = attemptSelections;
    }

    /**
     * Add an attempt selection item.
     * @param item to add
     */
    public void addAttemptSelection(AttemptSelectionItem item) {
        if (!getAttemptSelections().contains(item)) {
            getAttemptSelections().add(item);
            item.setSubgoalAttempt(this);
        }
    }

    /**
     * Get attemptActions.
     * @return the set of attempt actions or an empty set
     */
    protected Set getAttemptActions() {
        if (this.attemptActions == null) {
            this.attemptActions = new HashSet();
        }
        return this.attemptActions;
    }

    /**
     * Public method to get attempt actions.
     * @return a list instead of a set
     */
    public List getAttemptActionsExternal() {
        List sortedList = new ArrayList(getAttemptActions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set attemptActions.
     * @param attemptActions Collection of action attempts associated with this subgoal.
     */
    protected void setAttemptActions(Set attemptActions) {
        this.attemptActions = attemptActions;
    }

    /**
     * Add an attempt action item.
     * @param item to add
     */
    public void addAttemptAction(AttemptActionItem item) {
        if (!getAttemptActions().contains(item)) {
            getAttemptActions().add(item);
            item.setSubgoalAttempt(this);
        }
    }

    /**
     * Get attemptInputs.
     * @return the set of attempt inputs or an empty set
     */
    protected Set getAttemptInputs() {
        if (this.attemptInputs == null) {
            this.attemptInputs = new HashSet();
        }
        return this.attemptInputs;
    }

    /**
     * Public method to get attempt inputs.
     * @return a list instead of a set
     */
    public List getAttemptInputsExternal() {
        List sortedList = new ArrayList(getAttemptInputs());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set attemptInputs.
     * @param attemptInputs Collection of input attempts associated with this subgoal.
     */
    protected void setAttemptInputs(Set attemptInputs) {
        this.attemptInputs = attemptInputs;
    }

    /**
     * Add an attempt input item.
     * @param item to add
     */
    public void addAttemptInput(AttemptInputItem item) {
        if (!getAttemptInputs().contains(item)) {
            getAttemptInputs().add(item);
            item.setSubgoalAttempt(this);
        }
    }

    /**
     * Get feedbacks.
     * @return the set of feedbacks or an empty set
     */

    protected Set getFeedbacks() {
        if (this.feedbacks == null) {
            this.feedbacks = new HashSet();
        }
        return this.feedbacks;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List getFeedbacksExternal() {
        List sortedList = new ArrayList(getFeedbacks());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set feedbacks.
     * @param feedbacks Collection of feedbacks associated with this attempt.
     */
    protected void setFeedbacks(Set feedbacks) {
        this.feedbacks = feedbacks;
    }

    /**
     * Add a feedback item.
     * @param item to add
     */
    public void addFeedback(FeedbackItem item) {
        if (!getFeedbacks().contains(item)) {
            getFeedbacks().add(item);
            item.setSubgoalAttempt(this);
        }
    }

    /**
     * Remove a feedback item.
     * @param item to remove
     */
    public void removeFeedback(FeedbackItem item) {
        if (getFeedbacks().contains(item)) {
            getFeedbacks().remove(item);
            item.setSubgoalAttempt(null);
        }
    }

    /**
     * Get tutorTransactions.
     * @return the set of tutor transactions or an empty set
     */
    protected Set getTutorTransactions() {
        if (this.tutorTransactions == null) {
            this.tutorTransactions = new HashSet();
        }
        return this.tutorTransactions;
    }

    /**
     * Public method to get tutor transactions.
     * @return a list instead of a set
     */
    public List getTutorTransactionsExternal() {
        List sortedList = new ArrayList(getTutorTransactions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set tutorTransactions.
     * @param tutorTransactions Collection of transactions associated with this attempt.
     */
    protected void setTutorTransactions(Set tutorTransactions) {
        this.tutorTransactions = tutorTransactions;
    }

    /**
     * Add a transaction.
     * @param item to add
     */
    public void addTutorTransaction(TransactionItem item) {
        if (!getTutorTransactions().contains(item)) {
            getTutorTransactions().add(item);
            item.setSubgoalAttempt(this);
        }
    }

    /**
     * Get interpretations.
     * @return the set of interpretation attempts or an empty set
     */
    protected Set getInterpretationAttempts() {
        if (this.interpretationAttempts == null) {
            this.interpretationAttempts = new HashSet();
        }
        return this.interpretationAttempts;
    }

    /**
     * Public method to get interpretations.
     * @return a list instead of a set
     */
    public List getInterpretationAttemptsExternal() {
        List sortedList = new ArrayList(getInterpretationAttempts());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a interpretation.
     * @param item dataset to add
     */
    public void addInterpretationAttempt(InterpretationAttemptItem item) {
        getInterpretationAttempts().add(item);
        item.setSubgoalAttempt(this);
    }

    /**
     * Set InterpretationAttempts.
     * @param items Collection of dataset usages associated with this user.
     */
    public void setInterpretationAttempts(Set items) {
        this.interpretationAttempts = items;
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
         buffer.append(objectToString("SubgoalAttemptId", getId()));
         buffer.append(objectToStringFK("SubgoalId", getSubgoal()));
         buffer.append(objectToString("CorrectFlag", getCorrectFlag()));
         buffer.append(objectToString("Id", getId()));
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
         if (obj instanceof SubgoalAttemptItem) {
            SubgoalAttemptItem otherItem = (SubgoalAttemptItem)obj;

            if (!Item.objectEqualsFK(this.getSubgoal(), otherItem.getSubgoal())) {
                return false;
            }
            if (!Item.objectEquals(this.getCorrectFlag(), otherItem.getCorrectFlag())) {
                return false;
            }
            if (!objectEquals(this.getId(), otherItem.getId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getSubgoal());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getCorrectFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getId());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>subgoal</li>
     * <li>correctFlag</li>
     * <li>attemptName</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SubgoalAttemptItem otherItem = (SubgoalAttemptItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getSubgoal(), otherItem.getSubgoal());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCorrectFlag(), otherItem.getCorrectFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        return value;
    }

}