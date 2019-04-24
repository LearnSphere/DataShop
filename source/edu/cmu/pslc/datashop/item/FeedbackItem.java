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
 * Feedback that the tutor gave the student on a given attempt.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */

public class FeedbackItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this feedback. */
    private Long id;
    /** The attempt that this feedback occurred with. */
    private SubgoalAttemptItem subgoalAttempt;
    /** The text of the feedback as a string. */
    private String feedbackText;
    /** Classification of this feedback as a string. */
    private String classification;
    /** Template Tag (formerly hint id) of this feedback as a string. */
    private String templateTag;
    /** Collection of tutor transactions associated with this feedback. */
    private Set tutorTransactions;

    /** Default constructor. */
    public FeedbackItem() {
    }

    /**
     *  Constructor with id.
     *  @param feedbackId Database generated unique Id for this feedback.
     */
    public FeedbackItem(Long feedbackId) {
        this.id = feedbackId;
    }

    /**
     * Get feedbackId.
     * @return the Long is as a comparable.
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set feedbackId.
     * @param feedbackId Database generated unique Id for this feedback.
     */
    public void setId(Long feedbackId) {
        this.id = feedbackId;
    }
    /**
     * Get subgoalAttempt.
     * @return the subgoal attempt item
     */

    public SubgoalAttemptItem getSubgoalAttempt() {
        return this.subgoalAttempt;
    }

    /**
     * Set subgoalAttempt.
     * @param subgoalAttempt The attempt that this feedback occurred with.
     */
    public void setSubgoalAttempt(SubgoalAttemptItem subgoalAttempt) {
        this.subgoalAttempt = subgoalAttempt;
    }
    /**
     * Get feedbackText.
     * @return the feedback text as a string
     */

    public String getFeedbackText() {
        return this.feedbackText;
    }

    /**
     * Set feedbackText.
     * @param feedbackText The text of the feedback as a string.
     */
    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }
    /**
     * Get classification.
     * @return the classification
     */

    public String getClassification() {
        return this.classification;
    }

    /**
     * Set classification.
     * @param classification Classification of this feedback as a string.
     */
    public void setClassification(String classification) {
        this.classification = classification;
    }

    /**
     * Get templateTag.
     * @return the template tag
     */

    public String getTemplateTag() {
        return this.templateTag;
    }

    /**
     * Set templateTag.
     * @param templateTag of this feedback as a string.
     */
    public void setTemplateTag(String templateTag) {
        this.templateTag = templateTag;
    }


    /**
     * Get tutorTransactions.
     * @return a set of transactions associated with this feedback
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
            item.setFeedback(this);
        }
    }

    /**
     * Remove a transaction.
     * @param item to add
     */
    public void removeTutorTransaction(TransactionItem item) {
        if (getTutorTransactions().contains(item)) {
            getTutorTransactions().remove(item);
            item.setFeedback(null);
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
         buffer.append(objectToString("feedbackId", getId()));
         buffer.append(objectToStringFK("subgoalAttempt", getSubgoalAttempt()));
         buffer.append(objectToString("feedbackText", getFeedbackText()));
         buffer.append(objectToString("classification", getClassification()));
         buffer.append(objectToString("templateTag", getTemplateTag()));
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
          if (obj instanceof FeedbackItem) {
            FeedbackItem otherItem = (FeedbackItem)obj;

            if (!Item.objectEquals(this.getClassification(), otherItem.getClassification())) {
                return false;
            }

            if (!Item.objectEquals(this.getTemplateTag(), otherItem.getTemplateTag())) {
                return false;
            }

            if (!Item.objectEquals(this.getFeedbackText(), otherItem.getFeedbackText())) {
                return false;
            }

            if (!Item.objectEqualsFK(this.getSubgoalAttempt(), otherItem.getSubgoalAttempt())) {
                return false;
            }

            return true;
        }
        return false;
    }


    /**
    * Returns the hash code for this item.
    * @return int the hash code as an int.
    */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSubgoalAttempt());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFeedbackText());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getClassification());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTemplateTag());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>subgoal attempt</li>
     * <li>classification</li>
     * <li>feedback_text</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        FeedbackItem otherItem = (FeedbackItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getSubgoalAttempt(), otherItem.getSubgoalAttempt());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getClassification(), otherItem.getClassification());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTemplateTag(), otherItem.getTemplateTag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFeedbackText(), otherItem.getFeedbackText());
        if (value != 0) { return value; }

        return value;
    }

}