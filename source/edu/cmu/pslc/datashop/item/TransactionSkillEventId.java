/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the transaction_skill_event relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5516 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-06-11 13:55:15 -0400 (Thu, 11 Jun 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TransactionSkillEventId implements Serializable, Comparable {

    /** The transaction id.  Transaction id serves as a FK into the tutor transaction relation. */
    private Long transactionId;
    /** The skill id.  Skill id serves as a FK into the skill relation. */
    private Long skillId;

    /**
     * Constructor!
     */
    public TransactionSkillEventId() { };

    /**
     * Full constructor.
     * @param transaction the transaction item for this composite key.
     * @param skill the skill for this composite key.
     */
    public TransactionSkillEventId(TransactionItem transaction, SkillItem skill) {
        if (transaction != null) {
            this.transactionId = new Long((Long)transaction.getId());
        }
        if (skill != null) {
            this.skillId = new Long((Long)skill.getId());
        }
    }

    /**
     * Full constructor.
     * @param transactionId the transactionId for this object.
     * @param skillId the skillId for this object.
     */
    public TransactionSkillEventId(Long transactionId, Long skillId) {
        this.transactionId = new Long(transactionId);
        this.skillId = new Long(skillId);
    }

    /**
     * Get the skillId.
     * @return the skillId
     */
    public Long getSkillId() {
        return new Long(skillId);
    }

    /**
     * Set the skillId.
     * @param skillId the skillId to set
     */
    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    /**
     * Get the transactionId.
     * @return the transactionId.
     */
    public Long getTransactionId() {
        return new Long(transactionId);
    }

    /**
     * Set the transactionId.
     * @param transactionId the transactionId to set.
     */
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
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
         buffer.append("transactionId").append("='").append(transactionId).append("' ");
         buffer.append("skillId").append("='").append(skillId).append("' ");
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            TransactionSkillEventId otherItem = (TransactionSkillEventId)obj;

            if (!this.transactionId.equals(otherItem.getTransactionId())) {
                return false;
            }
            if (!this.skillId.equals(otherItem.getSkillId())) {
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
        int hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME
            + (transactionId != null ? transactionId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (skillId != null ? skillId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>transaction id</li>
     * <li>skill id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        TransactionSkillEventId otherItem = (TransactionSkillEventId)obj;
        if (this.transactionId.compareTo(otherItem.transactionId) != 0) {
            return this.transactionId.compareTo(otherItem.transactionId);
        }
        if (this.skillId.compareTo(otherItem.skillId) != 0) {
            return this.skillId.compareTo(otherItem.skillId);
        }
        return 0;
    }

} // end TransactionSkillEventId.java
