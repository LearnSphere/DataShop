/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

/**
 * Methods implemented by attempt selection/action/input item classes.
 * @author Jim Rankin
 * @version $Revision: 6363 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-14 09:52:39 -0400 (Thu, 14 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AttemptSAI {
    /**
     * Get subgoalAttempt.
     * @return a subgoal item
     */
    SubgoalAttemptItem getSubgoalAttempt();

    /**
     * Set subgoalAttempt.
     * @param subgoalAttempt The subgoal this selection was attempted in.
     */
    void setSubgoalAttempt(SubgoalAttemptItem subgoalAttempt);

    /** Get type. @return Returns the type. */
    String getType();

    /** Set type. @param type The type of selection as a string. */
    void setType(String type);

    /** Returns xmlId. @return Returns the xmlId. */
    String getXmlId();

    /** Set xmlId. @param xmlId The xmlId to set. */
    void setXmlId(String xmlId);

    /**
     *  Set the selection, action, or input value.
     *  @param value  the selection, action, or input value
     */
    void setValue(String value);
}