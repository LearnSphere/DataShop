/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.performanceprofiler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;

/**
 * This servlet serves the Problem Profiler page.
 *
 * @author Benjamin Billings
 * @version $Revision: 11888 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-12-12 14:58:11 -0500 (Fri, 12 Dec 2014) $
 * @see edu.cmu.pslc.datashop.servlet#CurriculumHelper
 * <!-- $KeyWordsOff: $ -->
 */
public class PerformanceProfilerServlet extends AbstractServlet  {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET_NAME = "PerformanceProfiler";

    /** Name of the JSP. */
    private static final String JSP_NAME = "/performance_profiler.jsp";

    /** Name of the content JSP. */
    private static final String JSP_CONTENT_NAME = "/performance_profiler_content.jsp";
    /** Name of the options JSP. */
    private static final String JSP_OPTIONS_NAME = "/performance_profiler_nav.jsp";

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     * @see javax.servlet.http.HttpServlet
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
     * @see javax.servlet.http.HttpServlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // set the most recent servlet name for the 'S2D' page
            req.getSession(true).setAttribute("recent_ds_page", SERVLET_NAME);

            DatasetContext datasetContext = getDatasetContext(req);

            UserItem userItem = getLoggedInUserItem(req);
            updateAccessFlag(datasetContext, userItem, "PerformanceProfiler");

            //handle all the passed in options.
            datasetContext.setPerformanceProfilerContext(
                    setOptions(req, datasetContext.getPerformanceProfilerContext()));

            setInfo(req, datasetContext);

            if (!updateNavigationOptions(req, resp)) {
                RequestDispatcher disp;
                String contentUpdateParam = req.getParameter("getPerformanceProfiler");
                String optionsUpdateParam = req.getParameter("getProfilerOptions");
                if (contentUpdateParam != null && contentUpdateParam.compareTo("") != 0) {
                    // forward to the JSP (view)
                    logger.info(getInfoPrefix(datasetContext)
                            + " No AJAX update. Forwarding to base JSP");
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_PERFORMANCE_PROFILER);
                    disp = getServletContext().getRequestDispatcher(JSP_CONTENT_NAME);
                    disp.forward(req, resp);
                    logger.info(getBenchmarkPrefix(datasetContext) + " after forward to base JSP.");
                } else if (optionsUpdateParam != null && optionsUpdateParam.compareTo("") != 0) {
                    // forward to the JSP (view)
                    logger.debug("No AJAX update. Forwarding to the options jsp.");
                    disp = getServletContext().getRequestDispatcher(JSP_OPTIONS_NAME);
                    disp.forward(req, resp);
                } else {
                    // forward to the JSP (view)
                    logger.info(getBenchmarkPrefix(datasetContext) + " accessing base JSP.");
                    disp = getServletContext().getRequestDispatcher(JSP_NAME);
                    disp.forward(req, resp);
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

    /**
     * Helper function to handle the option parameters and save it to the context.
     * @param req {@link HttpServletRequest}
     * @param context {@link PerformanceProfilerContext}
     * @return return {@link PerformanceProfilerContext} the updated context.
     */
    private PerformanceProfilerContext setOptions(
            HttpServletRequest req, PerformanceProfilerContext context) {
        String ppViewByCategory = req.getParameter("ppViewByCategory");
        if (ppViewByCategory != null && ppViewByCategory.compareTo("") != 0) {
            logger.debug("Setting Category to session. Type: " + ppViewByCategory);
            context.setViewByCategory(ppViewByCategory);
        }

        String ppViewByType = req.getParameter("ppViewByType");
        if (ppViewByType != null && ppViewByType.compareTo("") != 0) {
            logger.debug("Setting View By type to session. Type: " + ppViewByType);
            context.setViewByType(ppViewByType);
        }
        String ppSortBy = req.getParameter("ppSortBy");
        if (ppSortBy != null && ppSortBy.compareTo("") != 0) {
            logger.debug("Setting Sort By to session. SortBy: " + ppSortBy);
            context.setSortBy(ppSortBy);
        }

        String ppDisplayPredictedParam = req.getParameter("ppDisplayPredicted");
        if (ppDisplayPredictedParam != null && ppDisplayPredictedParam.compareTo("") != 0) {
            Boolean ppDisplayPredicted = new Boolean(ppDisplayPredictedParam);
            logger.debug("Setting Display Predicted to session. ppDisplayPredicted: "
                    + ppDisplayPredicted);
            context.setDisplayPredicted(ppDisplayPredicted);
        }

        String ppDisplayUnmappedParam = req.getParameter("ppDisplayUnmapped");
        if (ppDisplayUnmappedParam != null && ppDisplayUnmappedParam.compareTo("") != 0) {
            Boolean ppDisplayUnmapped = new Boolean(ppDisplayUnmappedParam);
            logger.debug("Setting Display Unmapped to session. ppDisplayUnmapped: "
                    + ppDisplayUnmapped);
            context.setDisplayUnmapped(ppDisplayUnmapped);
        }

        String ppSortAscendingParam = req.getParameter("ppSortAscending");
        if (ppSortAscendingParam != null && ppSortAscendingParam.compareTo("") != 0) {
            Boolean ppSortAscending = new Boolean(ppSortAscendingParam);
            logger.debug("Setting Order by to session. ppSortAscending: "
                    + ppSortAscending);
            context.setSortByAscendingDirection(Boolean.valueOf(ppSortAscendingParam));
        }

        String ppBottomLimitParam = req.getParameter("ppBottomLimit");
        if (ppBottomLimitParam != null) {
            Integer ppBottomLimit;
            try {
                ppBottomLimit = (ppBottomLimitParam.compareTo("") != 0)
                    ? new Integer(ppBottomLimitParam) : new Integer(-1);
            } catch (NumberFormatException exception) {
                ppBottomLimit = context.getBottomLimit();
                logger.warn("NumberFormatException on " + ppBottomLimit
                        + " setting to " + ppBottomLimit);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Setting Bottom Limit to Session. ppBottomLimit: " + ppBottomLimit);
            }
            context.setBottomLimit(ppBottomLimit);
        }

        String ppTopLimitParam = req.getParameter("ppTopLimit");
        if (ppTopLimitParam != null) {
            Integer ppTopLimit;
            try {
                ppTopLimit = (ppTopLimitParam.compareTo("") != 0)
                    ? new Integer(ppTopLimitParam) : new Integer(-1);
            } catch (NumberFormatException exception) {
                ppTopLimit = context.getTopLimit();
                logger.warn("NumberFormatException on " + ppTopLimitParam
                        + " setting to " + ppTopLimit);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Setting Top Limit to Session. ppTopLimit: "
                    + ppTopLimit);
            }
            context.setTopLimit(ppTopLimit);
        }

        return setMinimums(req, context);
    }

    /**
     * Handles the "minimum" parameters.
     * @param req The HttpServletRequest
     * @param context The {@link PerformanceProfilerContext}
     * @return return {@link PerformanceProfilerContext} the updated context.
     */
    private PerformanceProfilerContext setMinimums(
            HttpServletRequest req, PerformanceProfilerContext context) {
        String minStudentsParam = req.getParameter("minStudents");
        if (minStudentsParam != null) {
            Integer minStudents;
            try {
                minStudents = (minStudentsParam.compareTo("") != 0)
                    ? new Integer(minStudentsParam) : null;
            } catch (NumberFormatException exception) {
                minStudents = context.getMinStudents();
                logger.warn("NumberFormatException on " + minStudentsParam
                        + " setting to " + minStudents);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Setting minStudents: " + minStudents);
            }
            context.setMinStudents(minStudents);
        }

        String minProblemsParam = req.getParameter("minProblems");
        if (minProblemsParam != null) {
            Integer minProblems;
            try {
                minProblems = (minProblemsParam.compareTo("") != 0)
                    ? new Integer(minProblemsParam) : null;
            } catch (NumberFormatException exception) {
                minProblems = context.getMinProblems();
                logger.warn("NumberFormatException on " + minStudentsParam
                        + " setting to " + minProblems);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Setting minProblems: " + minProblems);
            }
            context.setMinProblems(minProblems);
        }

        String minStepsParam = req.getParameter("minSteps");
        if (minStepsParam != null) {
            Integer minSteps;
            try {
                minSteps = (minStepsParam.compareTo("") != 0)
                    ? new Integer(minStepsParam) : null;
            } catch (NumberFormatException exception) {
                minSteps = context.getMinSteps();
                logger.warn("NumberFormatException on " + minStepsParam
                        + " setting to " + minSteps);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Setting minSteps: " + minSteps);
            }
            context.setMinSteps(minSteps);
        }

        String minSkillsParam = req.getParameter("minSkills");
        if (minSkillsParam != null) {
            Integer minSkills;
            try {
                minSkills = (minSkillsParam.compareTo("") != 0)
                    ? new Integer(minSkillsParam) : null;
            } catch (NumberFormatException exception) {
                minSkills = context.getMinSkills();
                logger.warn("NumberFormatException on " + minSkillsParam
                        + " setting to " + minSkills);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Setting minSkills: " + minSkills);
            }
            context.setMinSkills(minSkills);
        }

        return context;
    }
}
