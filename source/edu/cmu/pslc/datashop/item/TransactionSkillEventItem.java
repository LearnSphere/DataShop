/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a transaction skill event record of the system.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TransactionSkillEventItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private TransactionSkillEventId id;

    /** The transaction for this item. */
    private TransactionItem transaction;

    /** The skill for this item. */
    private SkillItem skill;

    /** The initial pKnown value for this item. */
    private Double initialPKnown;

    /** The resulting pKnown value for this item. */
    private Double resultingPKnown;

    /**
     * Get the initialPKnown.
     * @return the intialPKnown
     */
    public Double getInitialPKnown() {
        return initialPKnown;
    }

    /**
     * Set the initialPKnown.
     * @param intialPKnown the intialPKnown to set
     */
    public void setInitialPKnown(Double intialPKnown) {
        this.initialPKnown = intialPKnown;
    }

    /**
     * Get the resultingPKnown.
     * @return the resultingPKnown
     */
    public Double getResultingPKnown() {
        return resultingPKnown;
    }

    /**
     * Set the resultingPKnown.
     * @param resultingPKnown the resultingPKnown to set
     */
    public void setResultingPKnown(Double resultingPKnown) {
        this.resultingPKnown = resultingPKnown;
    }

    /**
     * Get the skill.
     * @return the skill
     */
    public SkillItem getSkill() {
        return skill;
    }

    /**
     * Set the skill.
     * @param skill the skill to set
     */
    protected void setSkill(SkillItem skill) {
        this.skill = skill;
    }

    /**
     * Public Set skill method to update the composite key as well.
     * @param skill Part of the composite key - FK to the skill table.
     */
    public void setSkillExternal(SkillItem skill) {
        setSkill(skill);
        this.id = new TransactionSkillEventId(this.transaction, this.skill);
    }

    /**
     * Get the transaction.
     * @return the transaction
     */
    public TransactionItem getTransaction() {
        return transaction;
    }

    /**
     * Set the transaction.
     * @param transaction the transaction to set
     */
    protected void setTransaction(TransactionItem transaction) {
        this.transaction = transaction;
    }

    /**
     * Public Set transaction method to update the composite key as well.
     * @param transaction Part of the composite key - FK to the tutor_transaction table.
     */
    public void setTransactionExternal(TransactionItem transaction) {
        setTransaction(transaction);
        this.id = new TransactionSkillEventId(this.transaction, this.skill);
    }

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the TransactionSkilleventId.
     * @param id the id to set
     */
    public void setId(TransactionSkillEventId id) {
        this.id = id;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToStringFK("Transaction", this.getTransaction()));
        buffer.append(objectToStringFK("Skill", this.getSkill()));
        buffer.append(objectToString("InitialPKnown", this.getInitialPKnown()));
        buffer.append(objectToString("ResultingPKnown", this.getResultingPKnown()));
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
        if (obj instanceof TransactionSkillEventItem) {
            TransactionSkillEventItem otherItem = (TransactionSkillEventItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEquals(this.getInitialPKnown(), otherItem.getInitialPKnown())) {
                return false;
            }
            if (!objectEquals(this.getResultingPKnown(), otherItem.getResultingPKnown())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getInitialPKnown());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getResultingPKnown());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>transaction</li>
     * <li>skill</li>
     * <li>initialPKnown</li>
     * <li>resultingPKnown</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        TransactionSkillEventItem otherItem = (TransactionSkillEventItem)obj;

        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInitialPKnown(), otherItem.getInitialPKnown());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getResultingPKnown(), otherItem.getResultingPKnown());
        if (value != 0) { return value; }

        return value;
    }

} // end TransactionSkillEventItem.java
