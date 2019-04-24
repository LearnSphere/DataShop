/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dto.AdminDomainLearnLabDTO;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet responds to web application's request for the Web Services Credentials page.
 * @author Alida Skogsholm
 * @version $Revision: 8796 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-03-04 14:45:01 -0500 (Mon, 04 Mar 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AdminDomainLearnLabServlet extends AbstractServlet {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for just the content of the report. */
    private static final String JSP_FILE = "/admin_domain_learnlab.jsp";

    /** Session attribute. */
    public static final String ADMIN_DOMAIN_LEARNLAB_DATA = "admin_domain_learnlab_data";
    /** Session attribute. */
    public static final String ADMIN_DOMAIN_LEARNLAB_EXCLUDED_FLAG
        = "admin_domain_learnlab_excluded_flag";

    /** Title for the Admin page - "Admin". */
    public static final String SERVLET_LABEL = "Admin";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "AdminDomainLearnLab";
    /** String delimiter */
    private static final String SLASH_DELIM = "/";
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
        try {
            setEncoding(req, resp);

            UserItem userItem = getLoggedInUserItem(req);
            if (!userItem.getAdminFlag()) {
                logger.warn("Non-admin user attempted to access an adminstrator-only page."
                        + " User ID is '" + userItem.getId() + "'.");
                redirectHome(req, resp);
                return; //must return after redirect home
            }

            // Tell the jsp to highlight this tab
            req.getSession().setAttribute("datasets", SERVLET_LABEL);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
            LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
            SampleMetricDao sampleMetricDao = DaoFactory.DEFAULT.getSampleMetricDao();

            String adminAction = req.getParameter("admin_action");
            if (adminAction == null) {
                adminAction = "";
            }

            if (adminAction.equals("save_domain_learnlab")) {
                String datasetIdString = req.getParameter("dataset_id");
                String domainLearnLab = req.getParameter("domain_learnlab");

                if (datasetIdString == null /*|| domainLearnLab == null*/) {
                    logger.error("Unexpected null dataset id from JSP.");
                } else {
                    //Get the dataset id and item.
                    Integer datasetId = Integer.parseInt(datasetIdString);
                    DatasetItem datasetItem = datasetDao.get(datasetId);

                    String newDomainName = null;
                    String newLearnLabName = null;

                    DomainItem newDomainItem = new DomainItem();
                    LearnlabItem newLearnLabItem = new LearnlabItem();
                    int slash =  0;
                    //Get the domain and learnlab.
                    if (domainLearnLab.contains(SLASH_DELIM)) {
                        //Get the domain and learnlab.
                        slash = domainLearnLab.indexOf(SLASH_DELIM);
                        newDomainName = domainLearnLab.substring(0, slash);
                        newLearnLabName = domainLearnLab.substring(slash + 1);
                    } else {
                        if (domainLearnLab.equals("Other")) {
                            newDomainName = domainLearnLab;
                            newLearnLabName = "Other";
                        }
                    }

                    newDomainItem = (DomainItem)domainDao.findByName(newDomainName);
                    newLearnLabItem = (LearnlabItem)learnlabDao.findByName(newLearnLabName);

                    //Save.
                    String oldDomain =  null;
                    if (datasetItem.getDomain() != null) {
                        oldDomain = DaoFactory.DEFAULT.getDomainDao().get(
                                (Integer)datasetItem.getDomain().getId()).getName();
                    }
                    String oldLearnLab = null;
                    if (datasetItem.getLearnlab() != null) {
                        oldLearnLab = DaoFactory.DEFAULT.getLearnlabDao().get(
                                (Integer)datasetItem.getLearnlab().getId()).getName();
                    }
                    datasetItem.setDomain(newDomainItem);
                    datasetItem.setLearnlab(newLearnLabItem);
                    datasetDao.saveOrUpdate(datasetItem);

                    String newLearnLab = (newLearnLabItem == null)
                            ? "null" : newLearnLabItem.getName();
                    String newDomain = (newDomainItem == null)
                            ? "null" : newDomainItem.getName();

                    //Log a user action that the domain and learnlab was saved.
                    String info = "Changed domain/learnlab "
                        + "from '" + oldDomain + SLASH_DELIM + oldLearnLab
                        + "', to '" + newDomain + SLASH_DELIM + newLearnLab + "'"
                        + " for " + datasetItem.getDatasetName() + " (" + datasetId + ").";
                    UserLogger.log(datasetItem, userItem,
                            UserLogger.SAVE_DOMAIN_LEARNLAB, info);
                }
            } else if (adminAction.equals("save_junk_flag")) {
                String datasetIdString = req.getParameter("dataset_id");
                String junkFlagString = req.getParameter("junk_flag");
                if (datasetIdString == null || junkFlagString == null) {
                    logger.error("Unexpected null dataset id or junk flag string from JSP.");
                } else {
                    //Get the dataset id and item.
                    Integer datasetId = Integer.parseInt(datasetIdString);
                    DatasetItem datasetItem = datasetDao.get(datasetId);

                    //Get the new junk flag.
                    boolean newJunkFlag = Boolean.parseBoolean(junkFlagString);

                    //Save.
                    Boolean oldJunkFlag = datasetItem.getJunkFlag();
                    datasetItem.setJunkFlag(newJunkFlag);
                    datasetDao.saveOrUpdate(datasetItem);

                    //Log a user action that the junk flag was saved.
                    String info = "Changed junk flag "
                        + "from '" + oldJunkFlag
                        + "', to '" + newJunkFlag + "'"
                        + " for " + datasetItem.getDatasetName() + " (" + datasetId + ").";
                    UserLogger.log(datasetItem, userItem,
                            UserLogger.SAVE_JUNK_FLAG, info);
                }
            }
            String excludedFlag = req.getParameter("excluded_flag");
            if (excludedFlag == null) {
                excludedFlag = "true";
            }
            req.getSession().setAttribute(ADMIN_DOMAIN_LEARNLAB_EXCLUDED_FLAG, excludedFlag);

            //
            // Need to get the list again whether saving something or not.
            //
            List<AdminDomainLearnLabDTO> data = new ArrayList<AdminDomainLearnLabDTO>();
            List<DatasetItem> datasetList = null;
            if (excludedFlag.equals("false")) {
                datasetList = datasetDao.findUndeletedDatasets();
            } else {
                datasetList = datasetDao.findDatasetsWhereLearnLabIsNull();
            }

            String domainName = null;
            String learnlabName = null;
            ProjectItem projectItem = new ProjectItem();
            for (DatasetItem datasetItem : datasetList) {

                domainName = null;
                learnlabName = null;

                if (datasetItem.getDomain() != null) {
                    domainName = DaoFactory.DEFAULT.getDomainDao()
                            .get((Integer)datasetItem.getDomain().getId()).getName();
                }

                if (datasetItem.getLearnlab() != null) {
                     learnlabName = DaoFactory.DEFAULT.getLearnlabDao()
                            .get((Integer)datasetItem.getLearnlab().getId()).getName();
                }

                String pi = "";
                projectItem = datasetItem.getProject();
                if (projectItem != null) {
                    ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                    projectItem = projectDao.get((Integer)projectItem.getId());
                    pi = (projectItem.getPrimaryInvestigator() == null)
                        ? "" : (String)projectItem.getPrimaryInvestigator().getId();
                }
                String studentHours = sampleMetricDao.getTotalStudentHoursAsString(datasetItem);

                AdminDomainLearnLabDTO dto = new AdminDomainLearnLabDTO(
                            (Integer)datasetItem.getId(),
                            datasetItem.getDatasetName(),
                            pi,
                            studentHours,
                            domainName,
                            learnlabName,
                            datasetItem.getJunkFlag());

                 data.add(dto);
            } // end for loop on datasets

            req.getSession().setAttribute(ADMIN_DOMAIN_LEARNLAB_DATA, data);

            // Go to the JSP.
            redirect(req, resp, JSP_FILE);
        } catch (Exception exception) {
            logger.error("Exception occurred.", exception);
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    } // end doPost
}
