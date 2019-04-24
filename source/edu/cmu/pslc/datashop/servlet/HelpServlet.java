/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * This servlet is for handling the help pages.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13569 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-09-28 10:50:26 -0400 (Wed, 28 Sep 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HelpServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/help.jsp";

    /** HashMap of permissible page fragments. */
    public static final Map CONTENT_FRAGMENTS = new HashMap();
    static {
        CONTENT_FRAGMENTS.put("access",
                new Page("help_access.jsp", "Accessing DataShop"));
        CONTENT_FRAGMENTS.put("contact",
                new Page("help_contact.jsp", "Contact Us"));
        CONTENT_FRAGMENTS.put("errorReport",
                new Page("help_er.jsp", "Reports > Error Report"));
        CONTENT_FRAGMENTS.put("examples",
                new Page("help_examples.jsp", "Examples"));
        CONTENT_FRAGMENTS.put("export",
                new Page("help_export.jsp", "Export"));
        CONTENT_FRAGMENTS.put("exportFormatHistory",
                new Page("help_exportFormatHistory.jsp", "Export Format History"));
        CONTENT_FRAGMENTS.put("learningCurve",
                new Page("help_lc.jsp", "Reports > Learning Curve"));
        CONTENT_FRAGMENTS.put("learningCurveExamples",
                new Page("help_lc_examples.jsp", "Reports > Learning Curve Examples"));
        CONTENT_FRAGMENTS.put("learningCurveAlgorithm",
                new Page("help_lc_algorithm.jsp", "Reports > Learning Curve Algorithm"));
        CONTENT_FRAGMENTS.put("getting-data-in",
                new Page("help_getting-data-in.jsp", "Getting Data In"));
        CONTENT_FRAGMENTS.put("logging",
                new Page("help_logging.jsp", "Getting Data In > Logging New Data"));
        CONTENT_FRAGMENTS.put("import",
                new Page("help_import.jsp", "Getting Data In > Importing New Data"));
        CONTENT_FRAGMENTS.put("reports",
                new Page("help_reports.jsp", "Reports"));
        CONTENT_FRAGMENTS.put("sampleSelector",
                new Page("help_ss.jsp", "Filtering Data > Sample Selector"));

        CONTENT_FRAGMENTS.put("terms",
                new Page("help_terms.jsp", "Terms"));
        CONTENT_FRAGMENTS.put("pivotTable",
                new Page("help_pivot_table.jspf", "Using Other Tools > Excel PivotTable Report"));
        CONTENT_FRAGMENTS.put("datasetInfo",
                new Page("help_dsinfo.jspf", "Reports > Dataset Info"));
        CONTENT_FRAGMENTS.put("samples",
                new Page("help_samples.jspf", "Reports > Dataset Info > Samples"));
        CONTENT_FRAGMENTS.put("files",
                new Page("help_files.jsp", "Reports > Files"));
        CONTENT_FRAGMENTS.put("kcm",
                new Page("help_kcm.jspf", "Reports > Dataset Info > KC Models"));
        CONTENT_FRAGMENTS.put("stepList",
                new Page("help_steplist.jspf", "Reports > Dataset Info > Step List"));
        CONTENT_FRAGMENTS.put("stepRollup",
                new Page("help_stepRollup.jsp", "Reports > Learning Curve > Step Rollup"));
        CONTENT_FRAGMENTS.put("modelValues",
                new Page("help_modelValues.jsp", "Reports > Learning Curve > Model Values"));
        CONTENT_FRAGMENTS.put("perfProfiler",
                new Page("help_perfProfiler.jspf", "Reports > Performance Profiler"));
        CONTENT_FRAGMENTS.put("filteringData",
                new Page("help_filteringData.jsp", "Filtering Data"));
        CONTENT_FRAGMENTS.put("usingOtherTools",
                new Page("help_othertools.jsp", "Using Other Tools"));
        CONTENT_FRAGMENTS.put("externalTools",
                new Page("help_externalTools.jsp", "External Tools"));
        CONTENT_FRAGMENTS.put("rSoftware",
                new Page("help_rsoftware.jsp", "Using Other Tools > R (software)"));
        CONTENT_FRAGMENTS.put("advanced",
                new Page("help_advanced.jsp", "Advanced"));
        CONTENT_FRAGMENTS.put("webServicesCredentials",
                new Page("help_web_services.jsp", "Web Services"));
        CONTENT_FRAGMENTS.put("webServicesUserAgreement",
                new Page("help_web_services_user_agreement.jsp", "Web Services User Agreement"));
        CONTENT_FRAGMENTS.put("loggingActivity",
                new Page("help_logging_activity.jsp", "Logging Activity"));
        CONTENT_FRAGMENTS.put("metricsReport",
                new Page("help_metrics_report.jsp", "Metrics Report"));
        CONTENT_FRAGMENTS.put("citing",
                new Page("help_citing.jsp", "Citing DataShop and Datasets"));
        CONTENT_FRAGMENTS.put("administration",
                new Page("help_proj_administration.jsp", "Project / Dataset Administration"));
        CONTENT_FRAGMENTS.put("sampletodataset",
                new Page("help_sample_to_dataset.jspf", "Save Sample as Dataset"));
        CONTENT_FRAGMENTS.put("permissions",
                new Page("help_permissions.jsp", "Permissions"));
        CONTENT_FRAGMENTS.put("irb",
                new Page("help_irb.jsp", "IRB &amp; Data Sharing"));
        CONTENT_FRAGMENTS.put("requesting-access",
                new Page("help_requesting_access.jsp", "Requesting Dataset Access"));
        CONTENT_FRAGMENTS.put("customFields",
                new Page("help_custom_fields.jsp", "Custom Fields"));
        CONTENT_FRAGMENTS.put("importFormatTd",
                new Page("help_importFormatTd.jsp", "Import Format (Tab-delimited)"));
        CONTENT_FRAGMENTS.put("importFormatXml",
                new Page("help_importFormatXml.jsp", "Import Format (XML)"));
        CONTENT_FRAGMENTS.put("problemList",
                new Page("help_problemlist.jspf", "Reports > Dataset Info > Problem List"));
        CONTENT_FRAGMENTS.put("problemContent",
                new Page("help_problemcontent.jspf", "Reports > Dataset Info > Problem Content"));
        CONTENT_FRAGMENTS.put("discourseInfo",
                new Page("help_discourse.jsp", "Discourse Info"));
        CONTENT_FRAGMENTS.put("workflows",
                new Page("help_workflows.jsp", "Workflows"));
    }

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
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
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logger.debug("doPost begin");
        PrintWriter out = null;
        HttpSession httpSession = req.getSession(true);
        try {
            setEncoding(req, resp);

            UserItem userItem = (getUser(req) != null)
                ? getUser(req) : new UserItem(UserItem.DEFAULT_USER);

            String ipAddress = "";
            DatasetContext datasetContext = null;
            //if not logged-in, we shouldn't get the datasetContext
            if (!isLoggedIn(req)) {
                ipAddress = " [ipaddress:" + req.getRemoteAddr() + "]";
                logDebug("User not logged in.  IP Address: ", ipAddress);
            } else {
                datasetContext = (req.getParameter("datasetId") != null)
                        ? getDatasetContext(req) : null;
                logDebug("User logged in: ", userItem.getId());
            }
            DatasetItem dataset = (datasetContext != null) ? datasetContext.getDataset() : null;

            String logUserAction = req.getParameter("logUserAction");
            if (logUserAction != null && logUserAction.compareTo("") != 0) {
                String topic = req.getParameter("topic");
                logger.info("HelpServlet.doPost logUserAction: " + logUserAction + ":" + topic);
                UserLogger.log(dataset, userItem,
                        logUserAction + UserLogger.VIEW_REPORT_HELP, topic, false);
                return;
            }

            String pageFileName = null;
            String pageTitle = null;

            String pageAttribute = (String)req.getParameter("page");
            logDebug("page = ", pageAttribute);
            boolean validPageAttribute = false;
            if (pageAttribute == null) {
                validPageAttribute = true;
                pageFileName = "";
                pageTitle = "";
                logDebug("Going to help home page.", ipAddress);
            } else if (CONTENT_FRAGMENTS.containsKey(pageAttribute)) {
                validPageAttribute = true;
                pageFileName = "help/"
                    + ((Page)CONTENT_FRAGMENTS.get(pageAttribute)).getFileName();
                pageTitle = ((Page)CONTENT_FRAGMENTS.get(pageAttribute)).getPageTitle();
                logDebug("Going to ", pageFileName, ".", ipAddress);
            } else {
                validPageAttribute = false;
                logger.info("Invalid help page requested: " + pageAttribute);
                pageTitle = "Page not found";
                logDebug("Going to ", pageTitle, ".", ipAddress);
            }

            httpSession.setAttribute("page_filename", pageFileName);
            httpSession.setAttribute("page_title", pageTitle);

            if (validPageAttribute) {
                String info = ipAddress;
                if (pageAttribute != null) {
                    info = pageAttribute + ipAddress;
                }
                UserLogger.log(dataset, userItem, UserLogger.VIEW_HELP, info, false);
            }

            // forward to the JSP (view)
            logger.info("base JSP.");
            RequestDispatcher disp;
            disp = getServletContext().getRequestDispatcher(JSP_NAME);
            disp.forward(req, resp);

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
     * Stores the name of the file fragment and its display title
     */
    static class Page {
        /** File name of help page. */
        private String fileName;
        /** Title of help page. */
        private String pageTitle;

        /**
         * @return The file's name
         */
        public String getFileName() {
            return fileName;
        }
        /**
         * @return The page's title
         */
        public String getPageTitle() {
            return pageTitle;
        }
        /**
         * @param fileName The file fragment name
         * @param pageTitle The title of the page
         */
        public Page(String fileName, String pageTitle) {
            this.fileName = fileName;
            this.pageTitle = pageTitle;
        }
    }
}
