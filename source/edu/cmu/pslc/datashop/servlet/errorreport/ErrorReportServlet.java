/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.errorreport;

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
 * This servlet serves the ErrorReport page.  It uses helper for all navigation help.
 *
 * @author Benjamin Billings
 * @version $Revision: 11888 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-12-12 14:58:11 -0500 (Fri, 12 Dec 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportServlet extends AbstractServlet {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET_NAME = "ErrorReport";

    /** The JSP name for this servlet. */
    private static final String JSP_NAME = "/error_report.jsp";

    /** The JSP name for just the content of the error report. */
    private static final String BY_PROBLEM_JSP = "/error_report_content.jsp";

    /** The JSP name for just the content of the error report. */
    private static final String BY_SKILL_JSP = "/error_report_by_skill.jsp";

    /** HTML parameter that indicates a desire for only the ER content. */
    private static final String ER_AJAX_UPDATE_PARAM = "getErrorReport";

    /** View By Form Parameter Name. */
    public static final String VIEW_BY_PARAM = "error_report_view_by";
    /** View By Option. */
    public static final String VIEW_BY_PROBLEM = "problem";
    /** View By Option. */
    public static final String VIEW_BY_SKILL = "skill";

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
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

            DatasetContext datasetContext = getDatasetContext(req);

            UserItem userItem = getLoggedInUserItem(req);
            updateAccessFlag(datasetContext, userItem, "ErrorReport");

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // set the most recent servlet name for the 'S2D' page
            req.getSession(true).setAttribute("recent_ds_page", SERVLET_NAME);

            String errorReportViewBy = req.getParameter(VIEW_BY_PARAM); //form parameter
            if (errorReportViewBy != null) {
                datasetContext.getErrorReportContext().setViewBy(errorReportViewBy);
                logger.debug("ErrorReportServlet.doPost errorReportViewBy(if): "
                        + errorReportViewBy);
            } else {
                errorReportViewBy = datasetContext.getErrorReportContext().getViewBy();
                logger.debug("ErrorReportServlet.doPost errorReportViewBy(else): "
                        + errorReportViewBy);
            }

            if (updateNavigationOptions(req, resp)) {
                setInfo(req, datasetContext);
            } else {
                String jspName;
                if (errorReportViewBy == null || errorReportViewBy.equals(VIEW_BY_PROBLEM)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("VIEW BY PROBLEM");
                    }

                    jspName = BY_PROBLEM_JSP;
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_ER_BY_PROBLEM);
                } else {
                    if (logger.isDebugEnabled()) { logger.debug("VIEW BY SKILL"); }

                    jspName = BY_SKILL_JSP;
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_ER_BY_KC);
                }

                logger.info(getBenchmarkPrefix(datasetContext)
                        + " No AJAX update. Forwarding to base JSP");
                setInfo(req, datasetContext);
                // forward to the JSP (view)
                RequestDispatcher disp;
                if (req.getParameter(ER_AJAX_UPDATE_PARAM) != null) {
                    logger.debug("Forwarding content only");
                    disp = getServletContext().getRequestDispatcher(jspName);
                    disp.forward(req, resp);
                } else {
                    logger.info(getBenchmarkPrefix(datasetContext) + " accessing base JSP.");
                    disp = getServletContext().getRequestDispatcher(JSP_NAME);
                    disp.forward(req, resp);
                    logger.info(getBenchmarkPrefix(datasetContext) + " after forward to base JSP.");
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
} //end ErrorReportServlet Class
