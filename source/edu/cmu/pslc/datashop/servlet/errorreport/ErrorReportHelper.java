/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.errorreport;

import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ErrorReportDao;
import edu.cmu.pslc.datashop.dto.ErrorReportByProblem;
import edu.cmu.pslc.datashop.dto.ErrorReportBySkillList;
import edu.cmu.pslc.datashop.item.ProblemItem;

 /**
 * This class assists in the creation of error reports.
 *
 * @author Benjamin Billings
 * @version $Revision: 4765 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-05-01 10:52:19 -0400 (Thu, 01 May 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportHelper {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(ErrorReportHelper.class);

    /**
     * Returns the private logger.
     * @return logger an instance of the logger for this class
     */
    public Logger getLogger() { return logger; }

    /** Default constructor. */
    public ErrorReportHelper() {
        logger.debug("constructor");
    }

    /**
     * Return an Error Report given a skill model and list of selected skills.
     * @param sampleList the currently selected samples
     * @param skillModelId the id of the selected skill model
     * @param skillList the list of selected skills
     * @return an object that holds all we need to display an error report
     */
    public ErrorReportBySkillList getErrorReportBySkillList(
            List sampleList, Comparable skillModelId, List skillList) {

        logger.debug("getErrorReportBySkillList begin");

        ErrorReportDao dao = DaoFactory.DEFAULT.getErrorReportDao();

        ErrorReportBySkillList errorReport =
            dao.getErrorReportBySkillList(sampleList, skillModelId, skillList);

        logger.debug("getErrorReportBySkillList end");
        return errorReport;
    }

    /**
     * Initialized the Error Report given a problem item.
     * @param sampleList the list of selected samples
     * @param problemItem the problem to give an error report
     * @param skillModelId the id of the selected skill model
     * @return an object that holds all we need to display an error report
     */
    public ErrorReportByProblem getErrorReportByProblem(
            List sampleList, ProblemItem problemItem, Comparable skillModelId) {
        logger.debug("getErrorReportByProblem begin");

        ErrorReportDao dao = DaoFactory.DEFAULT.getErrorReportDao();

        ErrorReportByProblem errorReport =
            dao.getErrorReportByProblem(sampleList, problemItem, skillModelId);

        logger.debug("getErrorReportByProblem end");
        return errorReport;
    }

} // end class ErrorReportHelper
