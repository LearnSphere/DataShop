/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.tou;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;

/**
 * This servlet is for displaying the DataShop Terms of Use.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8625 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TermsServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/terms.jsp";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_TYPE_ATTRIB = "terms_type";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_TEXT_ATTRIB = "terms_text";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_STATUS_ATTRIB = "terms_status";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_EFFECTIVE_DATE_ATTRIB = "terms_effective_date";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_ARCHIVED_DATE_ATTRIB = "terms_archived_date";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_VERSION_ATTRIB = "terms_version";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_ARCHIVED_ATTRIB = "terms_archived";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_PROJECT_NAME_ATTRIB = "terms_project_name";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_UPDATE_FLAG_ATTRIB = "terms_update_flag";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_URL_ATTRIB = "terms_url";
    /** HTTP Servlet attribute name, used here an in JSP. */
    public static final String TERMS_DATASET_ID_ATTRIB = "terms_dataset_id";
    /** HTTP Servlet attribute. Values: current, version, list. */
    public static final String TERMS_SHOW_ATTRIB = "terms_show_id";

    /** Date formate for effective date. */
    public static final FastDateFormat DISPLAY_DATE_FORMAT =
            FastDateFormat.getInstance("MMMMM d, yyyy");

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
        logDebug("doPost begin :: ", getDebugParamsString(req));
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

            TermsOfUseDao termsOfUseDao = DaoFactory.DEFAULT.getTermsOfUseDao();
            TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
            Collection<TermsOfUseVersionItem>  archivedVersionList = null;
            TermsOfUseItem dsTerms = termsOfUseDao.find(TermsOfUseItem.DATASHOP_TERMS);
            archivedVersionList = termsVersionDao
                    .findVersionsByTermsAndStatus(dsTerms,
                            TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED);

            // view previous item list
            String actionAttribute = (String)req.getParameter("action");
            if ((actionAttribute != null) && (actionAttribute.equals("viewPrevList"))) {
                // get the html for archived items
                ArrayList<String> archivedList = new ArrayList<String>();
                TermsOfUseVersionItem archivedVersionItem = null;
                int archivedVersionNum = 0;
                for (Iterator<TermsOfUseVersionItem> iter
                        = archivedVersionList.iterator(); iter.hasNext();) {
                    archivedVersionItem = (TermsOfUseVersionItem)(
                            termsVersionDao.get((Integer) iter.next().getId()));
                    archivedVersionNum = archivedVersionItem.getVersion();
                    String appliedDateString = "", archivedDateString = "";
                    if (archivedVersionItem.getAppliedDate() != null) {
                        appliedDateString = DISPLAY_DATE_FORMAT.format(
                                archivedVersionItem.getAppliedDate());
                    }
                    if (archivedVersionItem.getArchivedDate() != null) {
                        archivedDateString = DISPLAY_DATE_FORMAT.format(
                                archivedVersionItem.getArchivedDate());
                    }
                    archivedList.add("<li><a href=\"Terms?version="
                            + archivedVersionNum + "\">Version "
                            + archivedVersionNum + ": "
                            + appliedDateString + " - " + archivedDateString
                            + "</li>");
                }
                httpSession.setAttribute(TERMS_ARCHIVED_ATTRIB, archivedList);
                httpSession.setAttribute(TERMS_EFFECTIVE_DATE_ATTRIB, null);
                httpSession.setAttribute(TERMS_ARCHIVED_DATE_ATTRIB, null);
                httpSession.setAttribute(TERMS_TEXT_ATTRIB, null);
                httpSession.setAttribute(TERMS_STATUS_ATTRIB, null);
                httpSession.setAttribute(TERMS_VERSION_ATTRIB, null);
                httpSession.setAttribute(TERMS_SHOW_ATTRIB, "list");
            } else { // Get the current DataShop terms of use
                TermsOfUseVersionItem versionItem = null;
                Integer version = null;
                String termsEffectiveDate = "", termsArchivedDate = "";
                String termsText = "", termsStatus = "";
                String versionAttribute = (String)req.getParameter("version");
                boolean validVersionAttribute = false;

                if (versionAttribute == null) {
                    versionItem = termsVersionDao.getDataShopTerms(version);
                    logDebug("Going to current datashop terms page.", ipAddress);
                    if (versionItem == null) {
                        termsText = "There are no terms associated with DataShop at this time.";
                        httpSession.setAttribute(TERMS_SHOW_ATTRIB, "no_terms");
                    }
                } else {
                    try {
                        version = Integer.parseInt(versionAttribute);
                        versionItem = termsVersionDao.getDataShopTerms(version);
                    } catch (NumberFormatException exception) {
                        version = null;
                    }
                    if (versionItem == null) {
                        validVersionAttribute = false;
                        termsText = "There are no terms of use with version " + versionAttribute;
                        logger.warn("Invalid version requested: " + versionAttribute);
                        httpSession.setAttribute(TERMS_SHOW_ATTRIB, "invalid_version");
                    } else {
                        validVersionAttribute = true;
                    }
                    logger.info("Version requested: " + version);
                }

                if (versionItem != null) {
                    termsEffectiveDate = DISPLAY_DATE_FORMAT.format(versionItem.getAppliedDate());
                    // if it is the current version, archived date is null
                    termsArchivedDate = versionItem.getArchivedDate() != null
                        ? DISPLAY_DATE_FORMAT.format(versionItem.getArchivedDate()) : "";
                    termsText = versionItem.getTerms();
                    termsStatus = versionItem.getStatus();
                    if (versionAttribute == null
                            || termsStatus.equals(TermsOfUseVersionItem.
                                    TERMS_OF_USE_VERSION_STATUS_APPLIED)) {
                        httpSession.setAttribute(TERMS_SHOW_ATTRIB, "current");
                    } else {
                        httpSession.setAttribute(TERMS_SHOW_ATTRIB, "version");
                    }
                }
                if (versionAttribute == null) {
                    if (versionItem != null) {
                        UserLogger.log(dataset, userItem, UserLogger.VIEW_TERMS,
                                "DataShop terms, version: " + versionItem.getVersion()
                                + " (current)" + ipAddress, false);
                    }
                } else if (validVersionAttribute) {
                    UserLogger.log(dataset, userItem, UserLogger.VIEW_TERMS,
                            "DataShop terms, version: " + versionAttribute
                            + " (" + termsStatus + ")" + ipAddress, false);
                } else {
                    UserLogger.log(dataset, userItem, UserLogger.VIEW_TERMS,
                            "DataShop terms, invalid version: "
                            + versionAttribute + ipAddress, false);
                }
                httpSession.setAttribute(TERMS_ARCHIVED_ATTRIB, archivedVersionList);
                httpSession.setAttribute(TERMS_EFFECTIVE_DATE_ATTRIB, termsEffectiveDate);
                httpSession.setAttribute(TERMS_ARCHIVED_DATE_ATTRIB, termsArchivedDate);
                httpSession.setAttribute(TERMS_TEXT_ATTRIB, termsText);
                httpSession.setAttribute(TERMS_STATUS_ATTRIB, termsStatus);
                httpSession.setAttribute(TERMS_VERSION_ATTRIB, version);
            }
            httpSession.setAttribute(TERMS_TYPE_ATTRIB, TermsOfUseItem.DATASHOP_TERMS);
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

}
