/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dto.ErrorReportByProblem;
import edu.cmu.pslc.datashop.dto.ErrorReportBySkillList;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * This DAO gets the information for the error report.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2886 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-05-30 08:50:27 -0400 (Tue, 30 May 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ErrorReportDao {

    /**
     * Return an Error Report given a skill model and list of selected skills.
     * @param sampleList the currently selected samples
     * @param skillModelId the id of the selected skill model
     * @param skillList the list of selected skills
     * @return an object that holds all we need to display an error report
     */
    ErrorReportBySkillList getErrorReportBySkillList(
            List sampleList, Comparable skillModelId, List skillList);

    /**
     * Return an Error Report given a problem item and a skill model.
     * @param sampleList the currently selected list of samples
     * @param problemItem the problem item
     * @param skillModelId the id of the selected skill model
     * @return an object that holds all we need to display an error report
     */
    ErrorReportByProblem getErrorReportByProblem(
            List sampleList, ProblemItem problemItem, Comparable skillModelId);
}
