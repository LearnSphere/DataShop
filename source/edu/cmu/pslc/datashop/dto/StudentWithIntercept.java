/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.io.Serializable;

/**
 * Holds the student's anonymous user id plus its LFA intercept value for the given skill model.
 *
 * @author Alida Skogsholm
 * @version $$Revision: 3474 $$
 * <BR>Last modified by: $$Author: alida $$
 * <BR>Last modified on: $$Date: 2006-12-13 15:54:48 -0500 (Wed, 13 Dec 2006) $$
 * <!-- $$KeyWordsOff: $$ -->
 */
public class StudentWithIntercept implements Serializable {
    /** Anonymized user id for this student. */
    private String anonymousUserId;
    /** The LFA student intercept (alpha score) for a this student for this skill model. */
    private Double lfaIntercept;
    /** The skill model the alpha score is associated with. */
    private long skillModelId;
    /**
     * Constructor.
     * @param anonymousUserId the student's anonymous id
     * @param lfaIntercept the student's LFA intercept (alpha score)
     * @param skillModelId the selected skill model id
     */
    public StudentWithIntercept(String anonymousUserId, Double lfaIntercept, long skillModelId) {
        this.anonymousUserId = anonymousUserId;
        this.lfaIntercept = lfaIntercept;
        this.skillModelId = skillModelId;
    }
    /**
     * Returns the anonymousUserId.
     * @return the anonymousUserId
     */
    public String getAnonymousUserId() {
        return anonymousUserId;
    }
    /**
     * Returns the lfaIntercept.
     * @return the lfaIntercept
     */
    public Double getLfaIntercept() {
        return lfaIntercept;
    }
    /**
     * Returns the skillModelId.
     * @return the skillModelId
     */
    public long getSkillModelId() {
        return skillModelId;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append(getClass().getName());
         buffer.append(" [");
         buffer.append("anonymousUserId:" + anonymousUserId);
         buffer.append(", lfaIntercept:" + lfaIntercept);
         buffer.append(", skillModelId:" + skillModelId);
         buffer.append("]");
         return buffer.toString();
    }
}
