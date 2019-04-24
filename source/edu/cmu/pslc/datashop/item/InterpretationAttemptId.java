/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Composite key for the interpretation attempt item.  Consists of 2 fields that are both
 * foreign keys as well. Note, the fields are immutable.  See Josh Bloch's book,
 * pages 122-123, about defensive copying.
 * <ul>
 *    <li>interpretationId - interpretation id
 *    <li>subgoalAttemptId - subgoal attempt id
 * </ul>
 *
 * @author Alida Skogsholm
 * @version $Revision: 3376 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-23 14:07:11 -0400 (Mon, 23 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InterpretationAttemptId  implements java.io.Serializable, Comparable  {

    /** The interpretation id portion of the composite key. */
    private Long interpretationId;
    /** The subgoal attempt id portion of the composite key. */
    private Long subgoalAttemptId;

    /** Default constructor. */
    public InterpretationAttemptId() {
    }

    /**
     * Full constructor, preferred.
     * Immutable.
     * @param interpretationItem the interpretation item
     * @param subgoalAttemptItem the subgoal attempt item
     */
    public InterpretationAttemptId(InterpretationItem interpretationItem,
            SubgoalAttemptItem subgoalAttemptItem) {
        if (subgoalAttemptItem != null) {
            this.subgoalAttemptId = new Long(((Long)subgoalAttemptItem.getId()).longValue());
        }

        if (interpretationItem != null) {
            this.interpretationId = new Long(((Long)interpretationItem.getId()).longValue());
        }
    }

    /**
     * Full constructor.
     * Immutable.
     * @param interpretationId the interpretation id
     * @param subgoalAttemptId the subgoal attempt id
     */
    public InterpretationAttemptId(Long interpretationId, Long subgoalAttemptId) {
        this.interpretationId = new Long(interpretationId.longValue());
        this.subgoalAttemptId = new Long(subgoalAttemptId.longValue());
    }

    /**
     * Get the interpretation id.
     * @return the interpretation id.
     */
    public Long getInterpretationId() {
        return new Long(this.interpretationId.longValue());
    }

    /**
     * Set the interpretation id.
     * @param id the interpretation id
     */
    protected void setInterpretationId(Long id) {
        this.interpretationId = id;
    }

    /**
     * Get the subgoal attempt id.
     * @return the subgoal attempt id.
     */
    protected Long getSubgoalAttemptId() {
        return new Long(subgoalAttemptId.longValue());
    }

    /**
     * Set the subgoal attempt id.
     * @param id the subgoal attempt id.
     */
    protected void setSubgoalAttemptId(Long id) {
        this.subgoalAttemptId = id;
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
        buffer.append("interpretationId='").append(interpretationId).append("' ");
        buffer.append("subgoalAttemptId='").append(subgoalAttemptId).append("' ");
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
        if (obj instanceof InterpretationAttemptId) {
            InterpretationAttemptId otherItem = (InterpretationAttemptId)obj;

            if (!this.interpretationId.equals(otherItem.interpretationId)) {
                 return false;
            }
            if (!this.subgoalAttemptId.equals(otherItem.subgoalAttemptId)) {
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
        if (interpretationId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + interpretationId.hashCode();
        }
        if (subgoalAttemptId == null) {
            hash = hash * UtilConstants.HASH_PRIME;
        } else {
            hash = hash * UtilConstants.HASH_PRIME + subgoalAttemptId.hashCode();
        }
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>subgoalAttemptId</li>
     *   <li>interpretationId</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        InterpretationAttemptId otherItem = (InterpretationAttemptId)obj;
        int value;

        if ((this.subgoalAttemptId != null) && (otherItem.subgoalAttemptId != null)) {
            value = this.subgoalAttemptId.compareTo(otherItem.subgoalAttemptId);
            if (value != 0) { return value; }
        } else if (this.subgoalAttemptId != null) {
            return 1;
        } else if (otherItem.subgoalAttemptId != null) {
            return -1;
        }

        if ((this.interpretationId != null) && (otherItem.interpretationId != null)) {
            value = this.interpretationId.compareTo(otherItem.interpretationId);
            if (value != 0) { return value; }
        } else if (this.interpretationId != null) {
            return 1;
        } else if (otherItem.interpretationId != null) {
            return -1;
        }
        return 0;
    }
}