/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Collection;

/**
 * Holds the data associated with an error report by skill.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2886 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-05-30 08:50:27 -0400 (Tue, 30 May 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportBySkillList implements java.io.Serializable {

    /** The list of ErrorReportBySkill objects. */
    private Collection skillList;

    /** Default constructor. */
    public ErrorReportBySkillList() {
    }

    /**
     * Returns the list of skill data.
     * @return the list of ErrorReportSkill objects
     */
    public Collection getSkillList() {
        return this.skillList;
    }

    /**
     * Sets the list of skill data.
     * @param list list of ErrorReportSkill objects
     */
    public void setSkillList(Collection list) {
        this.skillList = list;
    }
}
