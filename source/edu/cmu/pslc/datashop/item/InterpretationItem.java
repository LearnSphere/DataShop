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
 * Represents a single row in the interpretation table.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InterpretationItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this interpretation. */
    private Long interpretationId;

    /** Collection of subgoals associated with this interpretation. */
    private Set subgoals;
    /** Collection of cog step sequences associated with this interpretation. */
    private Set cogStepSequences;
    /** Collection of interpretation attempt items associated with this interpretation. */
    private Set interpretationAttempts;

    /** Default constructor. */
    public InterpretationItem() {
    }

    /**
     *  Constructor with id.
     *  @param interpretationId Database generated unique Id for this interpretation.
     */
    public InterpretationItem(Long interpretationId) {
        this.interpretationId = interpretationId;
    }

    /**
     * Get the id.
     * @return the id as a Long
     */
    public Comparable getId() {
        return this.interpretationId;
    }

    /**
     * Set the id.
     * @param interpretationId Database generated unique Id for this interpretation.
     */
    public void setId(Long interpretationId) {
        this.interpretationId = interpretationId;
    }

    /**
     * Get subgoals.
     * @return java.util.Set
     */
    protected Set getSubgoals() {
        if (this.subgoals == null) {
            this.subgoals = new HashSet();
        }
        return this.subgoals;
    }

    /**
     * Public method to get subgoals.
     * @return a list instead of a set
     */
    public List getSubgoalsExternal() {
        List sortedList = new ArrayList(getSubgoals());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set subgoals.
     * @param subgoals Collection of Subgoals associated with this problem.
     */
    protected void setSubgoals(Set subgoals) {
        this.subgoals = subgoals;
    }

    /**
     * Add a Subgoal.
     * @param item to add
     */
    public void addSubgoal(SubgoalItem item) {
        if (!getSubgoals().contains(item)) {
            getSubgoals().add(item);
            item.setInterpretation(this);
        }
    }

    /**
     * Get cogStepSequences.
     * @return java.util.Set
     */
    protected Set getCogStepSequences() {
        if (this.cogStepSequences == null) {
            this.cogStepSequences = new HashSet();
        }
        return this.cogStepSequences;
    }

    /**
     * Public method to get cog step sequences.
     * @return a list instead of a set
     */
    public List getCogStepSequencesExternal() {
        List sortedList = new ArrayList(getCogStepSequences());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a cog step sequence.
     * @param item cog step sequence to add
     */
    public void addCogStepSequence(CogStepSeqItem item) {
        getCogStepSequences().add(item);
        item.setInterpretation(this);
    }

    /**
     * Remove a cog step sequence.
     * @param item cog step sequence to remove
     */
    public void removeCogStepSequence(CogStepSeqItem item) {
        getCogStepSequences().remove(item);
        item.setInterpretation(null);
    }

    /**
     * Set cogStepsequences.
     * @param cogStepSequences Collection of cog step sequences associated with this interpretation.
     */
    public void setCogStepSequences(Set cogStepSequences) {
        this.cogStepSequences = cogStepSequences;
    }

    /**
     * Get interpretationAttempts.
     * @return java.util.Set
     */
    protected Set getInterpretationAttempts() {
        if (this.interpretationAttempts == null) {
            this.interpretationAttempts = new HashSet();
        }
        return this.interpretationAttempts;
    }

    /**
     * Public method to get interpretation attempts.
     * @return a list instead of a set
     */
    public List getInterpretationAttemptsExternal() {
        List sortedList = new ArrayList(getInterpretationAttempts());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a interpretation attempt .
     * @param item dataset to add
     */
    public void addInterpretationAttempt(InterpretationAttemptItem item) {
        getInterpretationAttempts().add(item);
        item.setInterpretation(this);
    }

    /**
     * Set interpretationAttempts.
     * @param items Collection of interpretation attempts for this item.
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
        if (obj instanceof InterpretationItem) {
            InterpretationItem otherItem = (InterpretationItem)obj;

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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getId());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>transaction id</li>
     * <li>chosen</li>
     * <li>correctness</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        InterpretationItem otherItem = (InterpretationItem)obj;
        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        return value;
    }
}