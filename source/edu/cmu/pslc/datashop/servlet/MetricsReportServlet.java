/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dto.LearnlabDomainMetricsReport;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * This servlet responds to Logging Activity Report requests.
 * @author Alida Skogsholm
 * @version $Revision: 8426 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-12-14 14:26:17 -0500 (Fri, 14 Dec 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricsReportServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Label used for setting session attribute "recent_report". */
    private static final String TRUE_SERVLET_NAME = "MetricsReport";

    // Is this value in use? Really it should be "MetricsReport"...
    /** The Servlet name. */
    public static final String SERVLET_NAME = "index.jsp";

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/metrics_report.jsp";

    /** Title for the Metrics Report page - "Advanced". */
    public static final String SERVLET_LABEL = "Advanced";

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
        logger.debug("doPost begin");
        HttpSession httpSession = req.getSession(true);
        try {
            setEncoding(req, resp);

            // Tell the jsp to highlight this tab
            req.getSession().setAttribute("datasets", SERVLET_LABEL);

            // set the most recent servlet name for the help page
            setRecentReport(httpSession, TRUE_SERVLET_NAME);

            if (!redirectToSecureUrl(req, resp)) {
                checkLogin(httpSession, req);
                MetricsReportHelper metricsReportHelper = new MetricsReportHelper();
                LearnlabDomainMetricsReport report = metricsReportHelper.getReport();

                // Log the user action.  Ignore the dataset id in this case.
                UserItem userItem = getLoggedInUserItem(req);
                UserLogger.log(null, userItem, UserLogger.VIEW_METRICS);

                // Put the report in the HTTP session for the JSP.
                req.getSession().setAttribute("metrics_report", report);
                // forward to the JSP (view)
                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(JSP_NAME);
                disp.forward(req, resp);
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    } //end doPost()

}
