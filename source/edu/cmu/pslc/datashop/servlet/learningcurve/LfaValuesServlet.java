/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AlphaScoreDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dto.StudentWithIntercept;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.servlet.ServletUtil;

/**
 * Servlet to display or export the LFA (Learning Factors Analysis) values.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14343 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-10-07 13:48:11 -0400 (Sat, 07 Oct 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LfaValuesServlet extends AbstractServlet {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** LFA JSP name. */
    private static final String LFA_JSP_NAME = "/lfa_values.jsp";

    /** Session attribute for the list of students with intercept information. */
    public static final String STUDENTS_INTERCEPT_LIST_ATTRIBUTE = "students_intercept_list";

    /** LFA parameter. */
    public static final String REQUEST_LFA_VALUES_PARAM = "requestLfaValues";
    /** LFA parameter. */
    public static final String EXPORT_LFA_VALUES_PARAM = "exportLfaValues";

    /** Description of missing student intercept values in the student values table. */
    public static final String STUDENT_INTERCEPT_CAPTION = "A student intercept value of "
        + "\"N/A\" means the student did not perform any steps associated with any of the KCs in "
        + "the selected KC model.";

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        //no difference, so just forward the request and response to the post.
        doPost(req, resp);
    }


    /**
     * Handles the HTTP post.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);

            NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            LfaValuesHelper lfaHelper = HelperFactory.DEFAULT.getLfaValuesHelper();
            DatasetContext datasetContext = getDatasetContext(req);
            LearningCurveContext lcContext = datasetContext.getLearningCurveContext();

            // Get the skill model and selected skills.
            SkillModelDao skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
            SkillModelItem primaryModel = skillModelDao.get(
                    navHelper.getSelectedSkillModel(datasetContext));
            List<SkillItem> skillList =
                lfaHelper.getSkillList(datasetContext.getDataset(), primaryModel);
            List alphaScoresAndStudents = new ArrayList();

            AlphaScoreDao alphaScoreDao = DaoFactory.DEFAULT.getAlphaScoreDao();

            // Get alpha scores if the user is requesting student data
            if (primaryModel != null) {

                List<StudentItem> studentList =
                    lfaHelper.getStudentList(datasetContext.getDataset());
                if (studentList != null && studentList.size() > 0) {
                    alphaScoresAndStudents = alphaScoreDao.find(primaryModel, studentList);
                } else {
                    alphaScoresAndStudents = new ArrayList();
                }
                lcContext.setStudentInterceptList(alphaScoresAndStudents);
            }

            // get total number of students with an alpha score in the model regardless
            // of what students are in sample and/or selected so we can calculate the
            // total number of parameters properly
            int totalNumStudentsWithAlphaScore = alphaScoreDao.find(primaryModel).size();
            lcContext.setTotalNumStudentsWithAlphaScore(totalNumStudentsWithAlphaScore);
            // The number of parameters is 2 times the number of skills plus
            // the number of students who have alpha scores (ie. a student intercept).
            Integer numParameters = null;
            if (primaryModel != null
             && SkillModelItem.LFA_STATUS_COMPLETE.equals(primaryModel.getLfaStatus())) {
                numParameters = skillList.size() * 2 + totalNumStudentsWithAlphaScore;
                lcContext.setLfaNumberOfParameters(numParameters);
            } else {
                lcContext.setLfaNumberOfParameters(null);
            }

            setInfo(req, datasetContext);

            if (req.getParameter(EXPORT_LFA_VALUES_PARAM) != null) {
                logger.debug("AJAX Response to export for LFA values to a file.");

                String fileName = getExportFileName(datasetContext.getDataset(),
                        "afm_kcm" + primaryModel.getId()) + ".txt";

                resp.setContentType("text/csv; charset=UTF-8");
                resp.addHeader("Content-Disposition", "attachment; filename=" + fileName);

                try {
                    StringBuffer results = exportLfaValues(primaryModel,
                                                           skillList, alphaScoresAndStudents,
                                                           numParameters, "\t");

                    UserLogger.log(datasetContext.getDataset(),
                            datasetContext.getUser(), UserLogger.EXPORT_LFA,
                            "KC Model " + primaryModel.getSkillModelName()
                            + " (" + primaryModel.getId() + ")");

                    int bufferLength = results.length();
                    resp.setContentLength(bufferLength);
                    out = resp.getWriter();
                    out.write(results.toString());
                } catch (Exception exception) {
                    logger.error("Exception occurred in LFA Values export", exception);
                }
            } else {
                if (req.getParameter(REQUEST_LFA_VALUES_PARAM) != null) {
                    UserLogger.log(datasetContext.getDataset(),
                            datasetContext.getUser(), UserLogger.VIEW_LFA_VALUES);
                    logDebug("AJAX Response to request for LFA values.  Forwarding to JSP");
                    redirect(req, resp, LFA_JSP_NAME);
                } else {
                    logDebug("No AJAX update. Forwarding to the base learning curve JSP");
                    redirect(req, resp, LearningCurveServlet.LC_BASE_JSP_NAME);
                }
            }
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /** Get the system line separator. */
    private static String newline = System.getProperty("line.separator");

    /** Constant. */
    private static Integer ONE_UNIQUE_STEP = new Integer(1);

    /**
     * Return a string buffer of the export data.
     * @param skillModelItem the primary skill selected
     * @param skillList the list of selected skills
     * @param studentList the list of selected students with their alpha scores
     * @param totalNumberOfParameters the number of total skills plus the number of total students
     * @param delimiter the delimiter between columns in the output file
     * @return a string buffer of the export data
     */
    private StringBuffer exportLfaValues(SkillModelItem skillModelItem,
                                         List skillList,
                                         List studentList,
                                         Integer totalNumberOfParameters,
                                         String delimiter) {
        String modelName = skillModelItem.getSkillModelName();
        StringBuffer buffer = new StringBuffer();

        buffer.append("KC Model Values for "  + modelName + " model");
        buffer.append(newline);

        buffer.append("AIC");
        buffer.append(delimiter);
        buffer.append("BIC");
        buffer.append(delimiter);
        buffer.append("Log Likelihood");
        buffer.append(delimiter);
        buffer.append("Number of Parameters");
        buffer.append(delimiter);
        buffer.append("Number of Observations");
        buffer.append(newline);

        buffer.append(ServletUtil.truncate(skillModelItem.getAic(), false));
        buffer.append(delimiter);
        buffer.append(ServletUtil.truncate(skillModelItem.getBic(), false));
        buffer.append(delimiter);
        buffer.append(ServletUtil.truncate(skillModelItem.getLogLikelihood(), false));
        buffer.append(delimiter);
        buffer.append(ServletUtil.truncate(totalNumberOfParameters, false));
        buffer.append(delimiter);
        buffer.append(ServletUtil.truncate(skillModelItem.getNumObservations(), false));
        buffer.append(newline);
        
        buffer.append(newline);
        buffer.append("Cross Validation Values (Stratified)");
        buffer.append(newline);
        
        buffer.append("Cross Validation RMSE (student stratified)");
        buffer.append(delimiter);
        //step and item are synonym here
        buffer.append("Cross Validation RMSE (item stratified)");
        buffer.append(newline);
        
        buffer.append(ServletUtil.truncate(skillModelItem.getCvStudentStratifiedRmse(), false));
        buffer.append(delimiter);
        buffer.append(ServletUtil.truncate(skillModelItem.getCvStepStratifiedRmse(), false));
        buffer.append(newline);
        
        buffer.append(newline);
        buffer.append("Cross Validation Values (Unstratified)");
        buffer.append(newline);
        
        buffer.append("Cross Validation RMSE");
        buffer.append(delimiter);
        buffer.append("Number of Parameters");
        buffer.append(delimiter);
        buffer.append("Number of Observations");
        buffer.append(delimiter);
        buffer.append(newline);
        
        buffer.append(ServletUtil.truncate(skillModelItem.getCvUnstratifiedRmse(), false));
        buffer.append(delimiter);
        buffer.append(ServletUtil.truncate(skillModelItem.getUnstratifiedNumParameters(), false));
        buffer.append(delimiter);
        buffer.append(ServletUtil.truncate(skillModelItem.getUnstratifiedNumObservations(), false));
        buffer.append(delimiter);
        buffer.append(newline);
        buffer.append(newline);

        //  Only include the 'KC Category' column if data is present.
        Boolean kcCategoryPresent = false;
        for (Iterator iter = skillList.iterator(); iter.hasNext();) {
            SkillItem skillItem = (SkillItem)iter.next();
            if (skillItem.getCategory() != null) {
                kcCategoryPresent = true;
                break;
            }
        }

        buffer.append("KC Values for "  + modelName + " model");
        buffer.append(newline);
        
        buffer.append("KC Name");
        buffer.append(delimiter);
        if (kcCategoryPresent) {
            buffer.append("KC Category");
            buffer.append(delimiter);
        }
        buffer.append("Number of Unique Steps");
        buffer.append(delimiter);
        buffer.append("Number of Observations");
        buffer.append(delimiter);
        buffer.append("Intercept (logit)");
        buffer.append(delimiter);
        buffer.append("Intercept (probability)");
        buffer.append(delimiter);
        buffer.append("Slope");
        buffer.append(newline);

        DatasetItem dataset = skillModelItem.getDataset();
        dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());

        LfaValuesHelper lfaValuesHelper = HelperFactory.DEFAULT.getLfaValuesHelper();
        HashMap<SkillItem, Long> uniqueStepsMap = null;

        // No need to get the map for Unique-Step model
        if (!skillModelItem.getSkillModelName().equals(SkillModelItem.NAME_UNIQUE_STEP_MODEL)) {
            uniqueStepsMap = lfaValuesHelper.getNumberUniqueStepsBySkillMap(dataset, skillList);
        }
        HashMap<SkillItem, Long> observationsMap
            = lfaValuesHelper.getNumberObservationsBySkillMap(dataset, skillList);

        //walk through the list of skills
        for (Iterator iter = skillList.iterator(); iter.hasNext();) {
            SkillItem skillItem = (SkillItem)iter.next();
            buffer.append(skillItem.getSkillName());
            buffer.append(delimiter);
            if (kcCategoryPresent) {
                if (skillItem.getCategory() != null) {
                    buffer.append(skillItem.getCategory());
                } else {
                    buffer.append("");
                }
                buffer.append(delimiter);
            }
            if (uniqueStepsMap == null) {
                // Null map means the model is Unique-Step
                buffer.append(ONE_UNIQUE_STEP);
            } else {
                Long numUniqueSteps = (Long)uniqueStepsMap.get(skillItem);
                if (numUniqueSteps != null) {
                    buffer.append(numUniqueSteps);
                } else {
                    buffer.append("");
                }
            }
            buffer.append(delimiter);
            Long numObservations = (Long)observationsMap.get(skillItem);
            if (numObservations != null) {
                buffer.append(numObservations);
            } else {
                buffer.append("");
            }
            buffer.append(delimiter);
            buffer.append(ServletUtil.truncate(skillItem.getBeta(), false));
            buffer.append(delimiter);
            buffer.append(ServletUtil.truncate(skillItem.getSkillInterceptAsProbability(), false));
            buffer.append(delimiter);
            buffer.append(ServletUtil.truncate(skillItem.getGamma(), false));
            buffer.append(newline);
        }
        buffer.append(newline);

        buffer.append("Student Values for "  + modelName + " model");
        buffer.append(newline);
        buffer.append(STUDENT_INTERCEPT_CAPTION);
        buffer.append(newline);
        
        buffer.append("Anon Student Id");
        buffer.append(delimiter);
        buffer.append("Intercept");
        buffer.append(newline);
        
        //walk through the list of students
        for (Iterator iter = studentList.iterator(); iter.hasNext();) {
            StudentWithIntercept studentWithIntercept = (StudentWithIntercept)iter.next();
            buffer.append(studentWithIntercept.getAnonymousUserId());
            buffer.append(delimiter);
            Double lfaIntercept = studentWithIntercept.getLfaIntercept();
            if (lfaIntercept == null) {
                buffer.append("N/A");
            } else {
                buffer.append(lfaIntercept);
            }
            buffer.append(newline);
        }

        return buffer;
    }
}
