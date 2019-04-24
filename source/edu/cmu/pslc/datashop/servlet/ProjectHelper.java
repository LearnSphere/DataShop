/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetInstanceMapDao;
import edu.cmu.pslc.datashop.dao.DatasetUserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dao.DiscourseInstanceMapDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.ProjectInfoReportDao;
import edu.cmu.pslc.datashop.dao.ProjectTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dao.RemoteDatasetInfoDao;
import edu.cmu.pslc.datashop.dao.RemoteDiscourseInfoDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.dao.UserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dto.ProjectInfoReport;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetInstanceMapItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUserTermsOfUseMapItem;
import edu.cmu.pslc.datashop.item.DiscourseInstanceMapItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;
import edu.cmu.pslc.datashop.item.RemoteDiscourseInfoItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapItem;
import edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestHelper;
import edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestServlet;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourcesDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDao;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;

 /**
 * This class is for the index of projects and they respective
 * datasets.
 *
 * @author Benjamin Billings
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectHelper {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Authorization Item dao */
    private AuthorizationDao authorizationDao;
    /** Dataset Item DAO */
    private DatasetDao datasetDao;
    /** Project Item DAO */
    private ProjectDao projectDao;
    /** User Item DAO */
    private UserDao userDao;
    /** Terms of Use Version Item DAO */
    private TermsOfUseVersionDao termsOfUseVersionDao;
    /** Project Terms of Use Map Item DAO */
    private ProjectTermsOfUseMapDao projectTermsOfUseMapDao;
    /** User Terms of Use Map Item DAO */
    private UserTermsOfUseMapDao userTermsOfUseMapDao;
    /** Dataset-User-TOU Map Item DAO */
    private DatasetUserTermsOfUseMapDao datasetUserTermsOfUseMapDao;
    /** Sample Metric DAO */
    private SampleMetricDao sampleMetricDao;

    /** String of the first servlet to go to when selecting a dataset */
    private static final String ENTRANCE_SERVLET_STRING = "DatasetInfo";
    /** String of the servlet for displaying DiscourseDB meta-info. */
    private static final String DISCOURSE_SERVLET_STRING = "DiscourseInfo";

    /** Number formatter for the number of transactions. */
    private static final DecimalFormat TX_FORMATTER = new DecimalFormat("##,###,##0");

    /** Datasets tab enumerated field value - "mine". */
    public static final String DATASETS_MINE = "mine";
    /** Datasets tab enumerated field value - "public". */
    public static final String DATASETS_PUBLIC = "public";
    /** Datasets tab enumerated field value - "other". */
    public static final String DATASETS_OTHER = "other";

    /**
     * Returns the debug logger.
     * @return logger - an instance of the logger for this class
     */
    public Logger getLogger() { return logger; }

    /** Default constructor. */
    public ProjectHelper() {
        logger.info("ProjectHelper.constructor");
    }

    /**
     * Get project dao.
     * @return the project dao
     */
    private ProjectDao getProjectDao() {
        if (projectDao == null)  {
            projectDao = DaoFactory.DEFAULT.getProjectDao();
        }
        return projectDao;
    }

    /**
     * Get terms of use dao.
     * @return the terms of use dao
     */
    private TermsOfUseVersionDao getTermsOfUseVersionDao() {
        if (termsOfUseVersionDao == null)  {
            termsOfUseVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        }
        return termsOfUseVersionDao;
    }

    /**
     * Indicates whether the given dataset is part of a public project.
     * @param datasetId the id of the dataset
     * @return true if the dataset is part of a public project, false otherwise
     */
    public boolean isPublic(Integer datasetId) {
        boolean isPublic = false;
        datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();

        DatasetItem datasetItem = datasetDao.get(datasetId);
        if (datasetItem != null) {
            ProjectItem projectItem = datasetItem.getProject();
            if (projectItem != null) {
                isPublic = authorizationDao.isPublic((Integer)projectItem.getId());
            }
        }

        logDebug("isPublic(", datasetId, "): ", isPublic);
        return isPublic;
    }

    /**
     * Simple converter function.
     * @param userId the user id as a string
     * @param datasetId the datasetId as an integer.
     * @return a boolean of true or false.
     */
    public boolean isAuthorized(String userId, int datasetId) {
        return isAuthorized(userId, new Integer(datasetId));
    }

    /**
     * Simple converter function.
     * @param userId the user id as a string
     * @param datasetId the datasetId as an comparable.
     * @return a boolean of true or false.
     */
    public boolean isAuthorized(String userId, Comparable datasetId) {
        return isAuthorized(userId, (Integer)datasetId);
    }

    /**
     * Indicates whether the given user is allowed to view the given curriculum.
     * @param userId the user's login
     * @param datasetId the id of the dataset
     * @return true if the user is authorized
     */
    public boolean isAuthorized(String userId, Integer datasetId) {
        boolean authorized = false;
        String level = null;
        datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        DatasetItem datasetItem = datasetDao.get(datasetId);

        //check to see if the user is an administrator.
        if (userId != null) {
            userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem userItem = userDao.find(userId);
            if (userItem != null && userItem.getAdminFlag()) {
                authorized = true;
            }
        }

        if (datasetItem != null && !authorized) {
            //No one but administrators can have authorization to a dataset
            //without a project.
            ProjectItem projectItem = datasetItem.getProject();
            if (projectItem != null) {
                authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();

                // First check if this user is authorized for this project
                if (userId != null) {
                    level = authorizationDao.getAuthorization(userId,
                            (Integer)projectItem.getId());
                }

                // If not authorized yet, maybe anyone can access this project.
                if (level == null) {
                    level = authorizationDao.getAuthorization(
                            (Integer)projectItem.getId());
                }

                if (level != null) {
                    authorized = true;
                }
            } else if (getIsUploader(datasetItem, userId)) {
                // Uploader is authorized.
                authorized = true;
            }
        }

        logDebug("isAuthorized(", userId, ", ", datasetId, "): ", authorized);
        return authorized;
    }

    /**
     * Indicates whether the given user is allowed to view the given discourse.
     * @param userId the user's login
     * @param discourseId the id of the discourse
     * @return true if the user is authorized
     */
    public boolean isAuthorizedForDiscourseDb(String userId, Long discourseId) {
        Boolean authorized = false;

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();

        DiscourseItem discourseItem = discourseDao.get(discourseId);

        //check to see if the user is an administrator.
        if (userId != null) {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem userItem = userDao.find(userId);
            if (userItem != null && userItem.getAdminFlag()) {
                authorized = true;
            }
        }

        String level = null;
        if ((discourseItem != null) && !authorized) {
            ProjectItem projectItem = DaoFactory.DEFAULT.getProjectDao().get(discourseItem.getProjectId());
            AuthorizationDao authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();

            // First check if this user is authorized for this project
            if (userId != null) {
                level = authorizationDao.getAuthorization(userId, (Integer)projectItem.getId());
            }

            // If not authorized yet, maybe anyone can access this project.
            if (level == null) {
                level = authorizationDao.getAuthorization((Integer)projectItem.getId());
            }

            if (level != null) {
                authorized = true;
            }
        }

        logger.debug("isAuthorizedForDiscourseDb(" + userId + ", "
                     + discourseId + "): " + authorized);
        return authorized;
    }

    /**
     * Indicates whether the given user is allowed to edit the given dataset.
     * @param userId the user's login
     * @param datasetId the id of the dataset
     * @param datasetContext the dataset information saved in the session.
     * @return true if the user is authorized to edit
     */
    public boolean hasEditAuthorization(String userId, Integer datasetId,
            DatasetContext datasetContext) {
        boolean authorized = false;
        datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem datasetItem = datasetDao.get(datasetId);

        if (datasetItem != null && datasetItem.getProject() != null
                && datasetItem.getProject().getId() != null) {
            authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();

            if (userId == null && datasetContext != null) {
                UserItem userItem = datasetContext.getUser();
                if (userItem != null) {
                    userId = (String)userItem.getId();
                }
            }

            // First check if this user is authorized for this project
            if (userId != null) {
                String userLevel = authorizationDao.getAuthorization(userId,
                        (Integer)datasetItem.getProject().getId());
                if (userLevel != null
                        && (userLevel.equals(AuthorizationItem.LEVEL_EDIT)
                                || userLevel.equals(AuthorizationItem.LEVEL_ADMIN))) {
                    authorized = true;
                }
            }

            // If not authorized yet, maybe anyone can access this project.
            if (!authorized) {
                String publicLevel = authorizationDao.getAuthorization(
                        (Integer)datasetItem.getProject().getId());
                if (publicLevel != null
                        && (publicLevel.equals(AuthorizationItem.LEVEL_EDIT)
                                //though public-admin should not be possible, check doesn't hurt
                            || publicLevel.equals(AuthorizationItem.LEVEL_ADMIN))) {
                    authorized = true;
                }

            }
        }

        logDebug("hasEditAuthorization(", userId, ", ", datasetId, "): ", authorized);
        return authorized;
    }

    /**
     * Determine if a given user is a system administrator.
     * @param userId the ID of the user to check.
     * @param datasetContext the dataset context information for getting the user Id.
     * @return boolean value, true if user is an administrator.
     */
    public boolean isDataShopAdmin(String userId, DatasetContext datasetContext) {
        boolean authorized = false;

        if (userId == null && datasetContext != null) {
            UserItem userItem = datasetContext.getUser();
            if (userItem != null) {
                userId = (String)userItem.getId();
            }
        }

        if (userId != null) {
            userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem user = userDao.get(userId);
            if (user != null) {
                return user.getAdminFlag().booleanValue();
            } else {
                logger.warn("User " + userId + " not found when looking up administrator rights.");
            }
        }
        logDebug("isDataShopAdmin(", userId, "): ", authorized);

        return authorized;
    }

    /**
     * Determine if a given user is a system administrator.
     * @param username the account ID
     * @return , true if user is an administrator, false otherwise
     */
    public boolean hasAdminAuthorization(String username) {
        boolean authorized = false;

        if (username != null) {
            userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem userItem = userDao.get(username);
            if (userItem != null) {
                authorized = userItem.getAdminFlag();
            } else {
                logger.warn("hasAdminAuthorization(" + username
                        + "): User not found when looking up administrator rights.");
            }
        }
        logDebug("hasAdminAuthorization(", username, "): ", authorized);

        return authorized;
    }

    /**
     * Return the html with my datasets.
     * @param remoteUser the user id
     * @param isAdminFlag indicates whether user is an administrator
     * @return an html string
     */
    public String getMyDatasets(String remoteUser, boolean isAdminFlag) {
        authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();
        datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List projectList = authorizationDao.findMyProjects(remoteUser);
        if (logger.isTraceEnabled()) {
            logger.trace("getMyDatasets for " + remoteUser
                    + " found " + projectList.size() + " projects");
        }

        userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.get(remoteUser);
        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        boolean hasDatashopEditRole = false;
        if (userItem != null) {
            hasDatashopEditRole = userRoleDao.hasDatashopEditRole(userItem);
        }

        // User has no data sets of their own, so display a helpful message.
        if (projectList == null || projectList.size() == 0) {
            if (hasDatashopEditRole) {
                return "<div id=\"my-data-sets\"><div id=\"noDatasets\">"
                    + "<p>You have no datasets of your own yet. You can explore "
                    + "<a href=\"index.jsp?datasets=public\">public datasets</a>, request "
                    + "access to a <a href=\"index.jsp?datasets=other\">private dataset</a> "
                    + "by clicking on the \"Request Access\" button next to a project name, "
                    + "or <a href=\"UploadDataset\">add your own</a>."
                    + "</p></div></div>";
            } else {
                return "<div id=\"my-data-sets\"><div id=\"noDatasets\">"
                    + "<p>You have no datasets of your own yet. You can explore "
                    + "<a href=\"index.jsp?datasets=public\">public datasets</a>, or request "
                    + "access to a <a href=\"index.jsp?datasets=other\">private dataset</a> "
                    + "by clicking on the \"Request Access\" button next to a project name."
                    + "</p></div></div>";
            }
        }
        return getDatasetSection(projectList, "my-data-sets", remoteUser,
                true, false, isAdminFlag);
    }

    /**
     * Return the html with the public datasets.
     * @param remoteUser the user id
     * @param isDataShopAdmin indicates whether user is an DataShop Administrator
     * @return an html string
     */
    public String getPublicDatasets(String remoteUser, boolean isDataShopAdmin) {
        authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();
        datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        List projectList = authorizationDao.findPublicProjects(remoteUser);
        if (logger.isTraceEnabled()) {
            logger.trace("getPublicDatasets for " + remoteUser
                    + " found " + projectList.size() + " projects");
        }

        return getDatasetSection(projectList, "public-data-sets", remoteUser,
                false, true, isDataShopAdmin);
    }

    /**
     * Return the html with available datasets--not mine, and not public.
     * @param remoteUser the user id
     * @param isDataShopAdmin true if current user is a DataShop Administrator
     * @return an html string
     */
    public String getAvailableDatasets(String remoteUser, boolean isDataShopAdmin) {
        authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();
        datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        List projectList = authorizationDao.findAvailableProjects(remoteUser);
        if (logger.isTraceEnabled()) {
            logger.trace("getAvailableDatasets for " + remoteUser
                    + " found " + projectList.size() + " projects");
        }
        String html = "<div id=\"available-data-sets\">";
        html += getDatasetSection(projectList,
                "available-data-sets-with-projects", remoteUser,
                false, false, isDataShopAdmin);

        // Get datasets that have no project assignment
        boolean isProjectAdmin = false;
        Collection datasetList = datasetDao.findByProject(null, isDataShopAdmin);
        if (datasetList == null || datasetList.size() == 0) {
            html += "</div>";
            return html;
        }

        boolean publicFlag = false; // available datasets are not public
        Long numPapers = new Long(0); // leave at 0 if dataset has no project

        html += "<table id=\"available-data-sets-with-no-project\">";
        html += getDatasetTableOpen(null, "Other Datasets", remoteUser, publicFlag,
                numPapers, isDataShopAdmin, null);
        html += getDatasetHtml(datasetList, remoteUser, isProjectAdmin,
                               isDataShopAdmin, numPapers, null);
        html += getDatasetTableClose();
        html += getSectionClose();
        html += "</div>";
        return html;
    }

    /**
     * Method to determine if Problem Content is available for specified dataset.
     * @param datasetId the Dataset id
     * @return boolean flag indicating presence of Problem Content
     */
    public boolean getIsProblemContentAvailable(Integer datasetId) {
        DatasetDao dao = DaoFactory.DEFAULT.getDatasetDao();
        PcConversionDatasetMapDao pcConversionDatasetMapDao =
            DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
        boolean isAvailable = pcConversionDatasetMapDao.isDatasetMapped(dao.find(datasetId));
        return isAvailable;
    }

    /**
     * Method to determine number of datasets in the specified project
     * that have Problem Content available.
     * @param projectId the project id
     * @return Integer number of datasets with Problem Content
     */
    public Integer getNumDatasetsWithProblemContent(Integer projectId) {
        ProjectDao dao = DaoFactory.DEFAULT.getProjectDao();
        PcConversionDatasetMapDao pcConversionDatasetMapDao =
                DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
        return pcConversionDatasetMapDao.getNumDatasetsWithProblemContent(dao.find(projectId));
    }

    /**
     * Return the html with my datasets.
     * @param projectList list of project items
     * @param datasetType the dataset type (e.g., my, public, available)
     * @param remoteUser the user id
     * @param publicFlagCheck indicates whether to use the publicFlag parameter or not
     * @param prevPublicFlag indicates whether the datasets are public or not
     * @param isDataShopAdmin true if current user is a DataShop Administrator
     * @return an html string
     */
    private String getDatasetSection(List projectList, String datasetType,
            String remoteUser,
            boolean publicFlagCheck, boolean prevPublicFlag, boolean isDataShopAdmin) {
        PcConversionDatasetMapDao pcConversionDatasetMapDao =
                DaoFactory.DEFAULT.getPcConversionDatasetMapDao();

        String htmlStr = getSectionOpen(datasetType, remoteUser);
        projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean publicFlag = true;
        Long numPapers = new Long(0);

        for (Iterator projIter = projectList.iterator(); projIter.hasNext();) {
            ProjectItem projectItem = (ProjectItem)projIter.next();

            // The 'Remote Datasets' project is only visible to DataShop Admins.
            if (!isDataShopAdmin &&
                projectItem.getProjectName().equals(ProjectItem.REMOTE_DATASETS)) {
                continue;
            }

            Boolean isDiscourse = projectItem.getIsDiscourseDataset();
            if ((isDiscourse != null) && isDiscourse) {
                htmlStr += getDiscourseDbTable(projectItem, remoteUser);
                continue;
            }

            boolean isProjectAdmin =
                    authDao.isProjectAdmin(remoteUser, (Integer)projectItem.getId());
            boolean isProjectOrDatashopAdminFlag = isProjectAdmin || isDataShopAdmin;

            if (logger.isTraceEnabled()) {
                logger.trace("User(" + remoteUser + ") getDatasetSection: Project "
                        + projectItem.getProjectName());
            }

            numPapers = projectDao.countPapers(projectItem);
            if (!publicFlagCheck) {
                publicFlag = prevPublicFlag;
            } else {
                publicFlag = authorizationDao.isPublic((Integer)projectItem.getId());
            }

            List<DatasetItem> datasetList = (List<DatasetItem>)
                    dsDao.findByProject(projectItem, isProjectOrDatashopAdminFlag);

            Integer projectId = (Integer)projectItem.getId();
            String projectName = projectItem.getProjectName();
            if (datasetList.isEmpty()) {
                isProjectOrDatashopAdminFlag = false; //to turn off the gear column
            }

            Integer numDatasetsWithProblemContent =
                pcConversionDatasetMapDao.getNumDatasetsWithProblemContent(projectItem);
            htmlStr += getDatasetTableOpen(projectId, projectName,
                remoteUser, publicFlag, numPapers, isProjectOrDatashopAdminFlag,
                    numDatasetsWithProblemContent);
            htmlStr += getDatasetHtml(datasetList, remoteUser, isProjectAdmin, isDataShopAdmin,
                                      numPapers, numDatasetsWithProblemContent);
            htmlStr += getDatasetTableClose();
        }

        numPapers = new Long(0);
        if (projectList.size() < 1) {
            htmlStr += getDatasetTableOpen(null, "", remoteUser, false, numPapers, false, null);
            htmlStr += getDatasetHtml(null, remoteUser, false, false, numPapers, null);
            htmlStr += getDatasetTableClose();
        }

        htmlStr += getSectionClose();
        return htmlStr;
    }

    /**
     * Gets the opening html.
     * @param datasetType The type of datasets being displayed
     * @param remoteUser the user id.
     * @return String of html.
     */
    private String getSectionOpen(String datasetType, String remoteUser) {
        if (datasetType != null) {
            String html = "\n<table id=\"" + datasetType + "\">";
            return html;
        } else {
            return "\n<table>";
        }
    }

    /**
     * The close of the section
     * @return html of the close.
     */
    private String getSectionClose() {
        return "\n</table>";
    }

    /**
     * Gets the beginning html for a project table.
     * @param projectId the id of the project, null otherwise
     * @param projectName the name of the project.
     * @param remoteUser the user_id of the remote user.
     * @param publicFlag indicates whether this project is public
     * @param numPapers the number of papers associated with the datasets of this project
     * @param isProjectOrDatashopAdminFlag either project administrator or datashop administrator
     * @param numDatasetsWithProblemContent the number of datasets with problem content
     * @return string of html.
     */
    private String getDatasetTableOpen(Integer projectId, String projectName,
            String remoteUser, boolean publicFlag, Long numPapers,
            boolean isProjectOrDatashopAdminFlag, Integer numDatasetsWithProblemContent) {
        boolean termsApplied = false;
        boolean termsAccepted = false;
        UserItem user = null;

        userDao = DaoFactory.DEFAULT.getUserDao();
        termsOfUseVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        projectTermsOfUseMapDao = DaoFactory.DEFAULT.getProjectTermsOfUseMapDao();
        userTermsOfUseMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();
        sampleMetricDao = DaoFactory.DEFAULT.getSampleMetricDao();

        String html = "\n<tr><td>";
        String accessRequestHtml = getAccessRequestButton(remoteUser, projectId);
        html += accessRequestHtml;
        html += "<h2 class=\"projectname\">";

        Integer numDatasetsWithPC = 0;
        if (numDatasetsWithProblemContent != null) {
            numDatasetsWithPC = numDatasetsWithProblemContent;
        }

        if (projectId == null) {
            html += projectName;
        } else {
            html += "<a href=\"Project?id=" + projectId + "\" class=\"project-name\">"
            + projectName + "</a>";
        }
        if (projectId != null) {
            ProjectItem projectItem = projectDao.get(projectId);

            // set termsApplied
            TermsOfUseVersionItem termsOfUseVersion =
                termsOfUseVersionDao.getProjectTerms(projectId, null);
            if (termsOfUseVersion != null) {
                TermsOfUseItem termsOfUse = termsOfUseVersion.getTermsOfUse();
                termsApplied = true;

                // set termsAccepted value
                if (remoteUser != null) {
                    user = userDao.find(remoteUser);
                    if (user != null) {
                        UserTermsOfUseMapId mapId = new UserTermsOfUseMapId(termsOfUse, user);
                        termsAccepted = userTermsOfUseMapDao.get(mapId) != null ? true : false;
                    }
                }
            }

            // Only display PI name, public/private icon and number of papers if part of a project.
            UserItem pi = null;
            String piName = null;
            pi = (projectItem != null) ? projectItem.getPrimaryInvestigator() : null;
            if (pi != null) {
                piName = pi.getName();
                if (piName != null) {
                    html += "&nbsp;<span class=\"pi-name\">PI: " + piName + "</span>&nbsp;";
                }
            }
            if (publicFlag) {
                html += " "
                    + "<img src=\"images/users.gif\" "
                    + "alt=\"(public)\" title=\"This is a public project.\" />";
            } else {
                html += " "
                    + "<img src=\"images/lock.png\" "
                    + "alt=\"(private)\" title=\"This is a private project.\" />";
            }
            if (numPapers > 0) {
                html += " "
                    + "<img src=\"images/star.png\" "
                    + "alt=\"(gold star)\" title=\"There ";
                if (numPapers > 1) {
                    html += "are " + numPapers + " papers ";
                } else {
                    html += "is " + numPapers + " paper ";
                }
                html += "associated with this project.\" />";
            }
            if (numDatasetsWithPC > 0) {
                html += " "
                    + "<img src=\"images/brick.png\" "
                    + "alt=\"(problem content)\" title=\""
                    + numDatasetsWithPC;
                if (numDatasetsWithPC > 1) {
                    html += " datasets in this project contain";
                } else {
                    html += " dataset in this project contains";
                }
                html += " the problem content that students saw.\" />";
            }
        }
        if (termsApplied) {
            String termsTitle = "There are terms of use associated with this project";
            String termsImage;

            if (remoteUser != null) {
                if (termsAccepted) {
                    termsTitle += " which you have agreed to already.";
                    termsImage = "images/script_go.png";
                } else {
                    termsTitle += " which you have not agreed to yet.";
                    termsImage = "images/script_delete.png";
                }
            } else {
                termsTitle += ".";
                termsImage = "images/script.png";
            }

            html += " "
             + "<img src=\"" + termsImage + "\" "
             + "alt=\"(terms of use)\" title=\"" + termsTitle +  "\" />";
        }

        html += " "
             + "</h2>"
             + "</td></tr>"
             + "\n<tr><td><table class=\"dataset-box\">"
             + "<thead><tr>"
             + "<th class=\"name\">Dataset</th>"
             + "<th class=\"domain_learnlab\">Domain/LearnLab</th>"
             + "<th class=\"dates\">Dates</th>"
             + "<th class=\"status\">Status</th>"
             + "<th class=\"transactions\">Transactions</th>";

        if ((numPapers > 0) || (numDatasetsWithPC > 0)) {
            html += "<th class=\"papersAndPCImg\"></th>";
        } else if (numPapers > 0) {
            html += "<th class=\"papersImg\"></th>";
        } else if (numDatasetsWithPC > 0) {
            html += "<th class=\"pcImg\"></th>";
        }
        if (isProjectOrDatashopAdminFlag) {
            html += "<th></th>";
        }
        html += "</tr></thead>";
        return html;
    }

    /**
     * Returns an html string of an Access Request Button or an empty string
     * if the user cannot request access to the given project.
     * @param userId the user Id
     * @param projectId the project Id
     * @return an html string of an Access Request Button or an empty string
     */
    public String getAccessRequestButton(String userId, Integer projectId) {
        return getAccessRequestButton(userId, projectId, false);
    }

    /**
     * Returns an html string of an Access Request Button or an empty string
     * if the user cannot request access to the given project.
     * @param userId the user Id
     * @param projectId the project Id
     * @param checkView indicates whether to show button if user has view access
     * @return an html string of an Access Request Button or an empty string
     */
    public String getAccessRequestButton(String userId, Integer projectId, boolean checkView) {
        String accessRequestHtml = "";
        String buttonTitle = null;
        if (userId != null && projectId != null) {
            // Get project and user item
            ProjectItem projectItem = getProjectDao().get(projectId);
            userDao = DaoFactory.DEFAULT.getUserDao();
            UserItem userItem = userDao.get(userId);

            if (userItem != null && !userItem.getId().equals(UserItem.DEFAULT_USER)
                    && projectItem != null) {
                authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();
                // If it's not the default public user, '%', then check user's authorization level
                String authLevel = authorizationDao.getAuthLevel(userId, projectId);
                if ((authLevel == null)
                    || (checkView
                         && authLevel != null
                         && authLevel.equals(AuthorizationItem.LEVEL_VIEW))) {

                    // We need the arHelper to get more information
                    AccessRequestHelper arHelper = new AccessRequestHelper();
                    // The requestButtonClass identifies the button to jQuery for modal dialogs
                    String requestButtonClass = "";
                    // If relevant, initialize project access request button
                    String buttonId = AccessRequestServlet.REQUEST_ID_PREFIX + projectId;
                    // Build an html string to insert into the project page if request permitted

                    // Instantiate Dao's
                    AccessRequestStatusDao arStatusDao =
                            DaoFactory.DEFAULT.getAccessRequestStatusDao();
                    AccessRequestHistoryDao arHistoryDao =
                            DaoFactory.DEFAULT.getAccessRequestHistoryDao();

                    // Get the status and last request
                    AccessRequestStatusItem arStatusItem =
                            arStatusDao.findByUserAndProject(userItem, projectItem);
                    // Last user request date for this project
                    Date lastRequestDate = null;
                    if (arStatusItem != null) {
                        AccessRequestHistoryItem lastRequest =
                            arHistoryDao.findLastRequest(arStatusItem);
                        if (lastRequest != null) {
                            lastRequestDate = lastRequest.getDate();
                        }
                    }
                    // Configure the button based on lastRequestDate
                    buttonTitle = arHelper.
                        getButtonTitleForRequest(lastRequestDate, arStatusItem, userId, projectId);

                    // Is the button enabled?
                    String tooltipStr = "";
                    Boolean buttonEnabled = arHelper.getButtonEnabled(buttonTitle);
                    //If button enabled, check project's shareability status
                    if (buttonEnabled) {
                        String srs = projectItem.getShareableStatus();
                        if (!(srs.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE)
                                ||
                              srs.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC))) {
                            buttonEnabled = false;
                            tooltipStr = "This project cannot be shared.";
                        }
                    }

                    if (buttonEnabled) {
                        requestButtonClass = "request_link";
                    } else if (!buttonEnabled) {
                        requestButtonClass = "dead_link ui-state-disabled";
                    }

                    accessRequestHtml = "<div id=\"" + buttonId + "\" name=\"requestButtonDiv\""
                            + " class=\"accessRequestButton\">"
                            + "<p><a href=\"#\" id=\"" + buttonId
                            + "\" class=\"" + requestButtonClass
                            + " ui-state-default ui-corner-all\""
                            + " name=\"" + buttonId + "\" "
                            + " title=\"" + tooltipStr + "\" >"
                            + "<span class=\"ui-icon ui-icon-newwin\"></span>"
                            + buttonTitle + "</a></div></p>";

                }
            } else {
                logDebug("No access request button, both user and project items are null");
            }
        } else {
            logDebug("No access request button, both user and project ids are null");
        }
        return accessRequestHtml;
    }

    /**
     * Returns an html string of a Remote Access Button or an empty string
     * if the dataset is not remote.
     * @param dataset the DatasetItem
     * @return an html string of a Remote Access Button or an empty string
     */
    public String getRemoteAccessButton(DatasetItem dataset) {

        RemoteDatasetInfoItem remoteDataset = getRemoteDataset(dataset);

        StringBuffer result = new StringBuffer();

        // If not a remote dataset, return empty string.
        if (remoteDataset == null) return result.toString();

        RemoteInstanceItem remoteInstance = getRemoteInstance(dataset);

        Integer datasetId = (Integer)dataset.getId();

        result.append("<div id=\"remoteAccess_").append(datasetId).append("\" ");
        result.append("name=\"remoteButtonDiv\" ");
        result.append("class=\"remoteAccessButton\">");
        result.append("<p><a target=\"_blank\" href=\"").append(remoteInstance.getDatashopUrl());
        result.append("/DatasetInfo?datasetId=").append(datasetId).append("\" ");
        result.append("id=\"remoteAccess_").append(datasetId).append("\" ");
        result.append("class=\"remote_link ui-state-default ui-corner-all\" ");
        result.append("title=\"This dataset is on a remote DataShop instance.\" >");
        result.append("<span class=\"ui-icon ui-icon-newwin\"></span>");
        result.append("Access Remote Dataset");
        result.append("</a></div></p>");

        return result.toString();
    }

    /**
     * Returns an html string of a Remote Access Button or an empty string
     * if the discourse is not remote.
     * @param discourse the DiscourseItem
     * @return an html string of a Remote Access Button or an empty string
     */
    public String getRemoteAccessButton(DiscourseItem discourse) {

        Boolean isRemote = isDiscourseRemote(discourse);

        StringBuffer result = new StringBuffer();

        // If not a remote discourse, return empty string.
        if (!isRemote) { return result.toString(); }

        RemoteInstanceItem remoteInstance = getRemoteInstance(discourse);

        Long discourseId = (Long)discourse.getId();

        result.append("<div id=\"remoteAccess_").append(discourseId).append("\" ");
        result.append("name=\"remoteButtonDiv\" ");
        result.append("class=\"remoteAccessButton\">");
        result.append("<p><a target=\"_blank\" href=\"").append(remoteInstance.getDatashopUrl());
        result.append("/DiscourseInfo?discourseId=").append(discourseId).append("\" ");
        result.append("id=\"remoteAccess_").append(discourseId).append("\" ");
        result.append("class=\"remote_link ui-state-default ui-corner-all\" ");
        result.append("title=\"This discourse is on a remote DataShop instance.\" >");
        result.append("<span class=\"ui-icon ui-icon-newwin\"></span>");
        result.append("Access Remote Discourse");
        result.append("</a></div></p>");

        return result.toString();
    }

    /** Constant. Name of class for enabled button. */
    private static final String BUTTON_ENABLED_CLASS = "project_public_link";
    /** Constant. Name of class for disabled button. */
    private static final String BUTTON_DISABLED_CLASS = "dead_link ui-state-disabled";
    /** Constant. Tooltip for button when neither PI nor DP is set. */
    private static final String BUTTON_NO_PI_DP_TOOLTIP = "The PI or data provider must "
        + "be set on this project before it can be made public.";


    /**
     * Returns an html string of an button to make a project public or an empty string
     * if the project is already public.
     * @param userId the user Id of the PI or DP
     * @param projectId the project Id
     * @return an html string of an button to make a project public or an empty string
     */
    public String getMakeProjectPublicButton(String userId, Integer projectId) {
        String projectPublicButtonHtml = "";
        if (userId != null && projectId != null) {
            ProjectItem projectItem = projectDao.get(projectId);
            UserItem userItem = userDao.get(userId);
            if (userItem != null && projectItem != null) {
                if (authorizationDao == null) {
                    authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();
                }

                // Determine if user is PA (acting for PI)
                boolean isPA = authorizationDao.isProjectAdmin(userItem, projectItem);

                AccessRequestStatusDao arStatusDao =
                        DaoFactory.DEFAULT.getAccessRequestStatusDao();
                AccessRequestHistoryDao arHistoryDao =
                        DaoFactory.DEFAULT.getAccessRequestHistoryDao();

                if (!authorizationDao.isPublic(projectId)) {
                    String role = projectItem.getRole(userItem, isPA);
                    String ownership = projectItem.getOwnership();

                    // Whether or not the response button is enabled
                    Boolean buttonEnabled = true;
                    // Whether or not to show AccessRequestHelper.BUTTON_TITLE_VOTE (true)
                    // or AccessRequestHelper.BUTTON_TITLE_MAKE string
                    Boolean showVoteButton = false;
                    // Whether or not the PI or DP voted to make project public
                    Boolean piVoted = false, dpVoted = false;

                    String tooltipStr = null;

                    // Check the Shareability Review Status
                    String srs = projectItem.getShareableStatus();
                    if (srs.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE)) {
                        // Button disabled if neither PI nor DP is set.
                        if ((projectItem.getPrimaryInvestigator() == null)
                            && (projectItem.getDataProvider() == null)) {
                            buttonEnabled = false;
                            tooltipStr = BUTTON_NO_PI_DP_TOOLTIP;
                        } else {
                            buttonEnabled = true;
                            //but no tooltip
                        }
                    } else {
                        buttonEnabled = false;
                        tooltipStr = "This project cannot be made public - see above.";
                    }

                    if (ownership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)
                            && !userItem.getAdminFlag()) {
                        // voting is required to make public...

                        UserItem publicUser = userDao.find(UserItem.DEFAULT_USER);
                        AccessRequestStatusItem arStatus = arStatusDao
                                .findByUserAndProject(publicUser, projectItem);

                        // Get the status item if it exists
                        if (arStatus != null) {
                            arStatus = arStatusDao.get((Integer) arStatus.getId());
                        }

                        if (arStatus != null) {
                            // Determine which path to execute based on Role and previous votes
                            AccessRequestHistoryItem arHistoryPi = arHistoryDao.findLastResponse(
                                arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
                            AccessRequestHistoryItem arHistoryDp = arHistoryDao.findLastResponse(
                                arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
                            // Find out if the PI and DP voted to make project public
                            if (arHistoryPi != null && arHistoryPi.getAction()
                                .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE)) {
                                piVoted = true;
                            }
                            if (arHistoryDp != null && arHistoryDp.getAction()
                                .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE)) {
                                dpVoted = true;
                            }
                            // Determine whether or not to show the button and if it's enabled
                            if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                                if (piVoted) {
                                    buttonEnabled = false;
                                    showVoteButton = true;
                                } else if (arHistoryPi == null) {
                                    showVoteButton = true;
                                }
                            } else if (role.
                                    equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP)) {
                                if (dpVoted) {
                                    buttonEnabled = false;
                                    showVoteButton = true;
                                } else if (arHistoryDp == null) {
                                    showVoteButton = true;
                                }
                            }
                        } else {
                            showVoteButton = true;
                        }

                        // Create the jQuery button
                        String buttonId = AccessRequestServlet.PROJECT_ID_PREFIX + projectId;
                        String buttonTitle = "";
                        String buttonClass = "";
                        if (buttonEnabled) {
                            // This means the link is enabled
                            buttonClass = BUTTON_ENABLED_CLASS;
                            if (ownership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)
                                && !(piVoted ^ dpVoted)) {
                                buttonTitle = AccessRequestHelper.BUTTON_TITLE_VOTE;
                            } else {
                                buttonTitle = AccessRequestHelper.BUTTON_TITLE_MAKE;
                            }
                        } else if (!buttonEnabled) {
                            // This means the link cannot be clicked
                            buttonClass = BUTTON_DISABLED_CLASS;
                            buttonTitle = AccessRequestHelper.BUTTON_TITLE_HAS_VOTED;
                        }

                        if (showVoteButton) {
                            // Actually display button (vote or make project public)
                            projectPublicButtonHtml =
                                "<div id=\"project-public-button-div\"><a href=\"#\" id=\""
                                + buttonId + "\" class=\"" + buttonClass
                                + " ui-state-default ui-corner-all\""
                                + " name=\"" + buttonId + "\"";

                            if (tooltipStr != null) {
                                projectPublicButtonHtml += " title=\"" + tooltipStr + "\"";
                            }

                            projectPublicButtonHtml += ">"
                                + "<span class=\"ui-icon ui-icon-newwin\"></span>"
                                + buttonTitle + "</a></div>";
                        }
                    } else if (!ownership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)
                               || userItem.getAdminFlag()) {
                        // No voting required.
                        String buttonId = AccessRequestServlet.PROJECT_ID_PREFIX + projectId;
                        String buttonTitle = AccessRequestHelper.BUTTON_TITLE_MAKE;
                        String buttonClass = buttonEnabled ? BUTTON_ENABLED_CLASS
                            : BUTTON_DISABLED_CLASS;
                        // Actually display button (vote or make project public)
                        projectPublicButtonHtml =
                            "<div id=\"project-public-button-div\"><a href=\"#\" id=\""
                            + buttonId + "\" class=\"" + buttonClass
                            + " ui-state-default ui-corner-all\""
                            + " name=\"" + buttonId + "\"";

                        if (tooltipStr != null) {
                            projectPublicButtonHtml += " title=\"" + tooltipStr + "\"";
                        }

                        projectPublicButtonHtml += ">"
                            + "<span class=\"ui-icon ui-icon-newwin\"></span>"
                            + buttonTitle + "</a></div>";
                    }
                }
            }
        }
        return projectPublicButtonHtml;
    }

    /** String constant for Shareability Review Status message on Project Permissions page. */
    public static final String SRS_MSG_PUBLIC =
        "This project is public.";
    /** String constant for Shareability Review Status message on Project Permissions page. */
    private static final String SRS_MSG_SHAREABLE =
        "This project has been marked as <strong>Shareable</strong> by the Research Manager. "
      + "If you wish, you may share it with people outside your research team "
      + "and/or make it public.";
    /** String constant for Shareability Review Status message on Project Permissions page. */
    private static final String SRS_MSG_SHAREABLE_NOT_PUBLIC =
        "This project has been marked as <strong>Shareable, but cannot be public</strong> "
      + "by the Research Manager. If you wish, you may share it with people outside your "
      + "research team, but you cannot make it public to everyone. This is likely because "
      + "your IRB or consent form wording prohibits the data from being made public.";
    /** String constant for Shareability Review Status message on Project Permissions page. */
    private static final String SRS_MSG_NOT_SHAREABLE =
        "This project has been marked as <strong>Not shareable</strong> by the Research Manager. "
      + "Please note that you may only grant access to members of your research team.<br>"
      + "Your project is <strong>Not shareable</strong> likely because your consent form "
      + "wording prohibits sharing outside your research team, as well as making the data public.";
    /** String constant for Shareability Review Status message on Project Permissions page. */
    private static final String SRS_MSG_NOT_DETERMINED =
        "This project's shareability has not been determined by the Research Manager yet. "
      + "Please note that you may grant access only to members of your research team at this time.";

    /**
     * Returns a message about the shareability status.
     * @param projectId the project Id
     * @return an html string of an button to make a project public or an empty string
     */
    public String getSrsMessage(Integer projectId) {
        String srsMessage = "";
        if (projectId != null) {
            ProjectItem projectItem = projectDao.get(projectId);
            if (projectItem != null) {
                AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
                if (authDao.isPublic(projectId)) {
                    srsMessage = SRS_MSG_PUBLIC;
                } else {
                    String srs = projectItem.getShareableStatus();
                    if (srs.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE)) {
                        srsMessage = SRS_MSG_SHAREABLE;
                        //check if either/both PI and DP are set
                        if ((projectItem.getPrimaryInvestigator() == null)
                                && (projectItem.getDataProvider() == null)) {
                            srsMessage += "<p>" + BUTTON_NO_PI_DP_TOOLTIP + "</p>";
                        }
                    } else if (srs.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)) {
                        srsMessage = SRS_MSG_SHAREABLE_NOT_PUBLIC;
                    } else if (srs.equals(ProjectItem.SHAREABLE_STATUS_NOT_SHAREABLE)) {
                        srsMessage = SRS_MSG_NOT_SHAREABLE;
                    } else { // not determined (not submitted, submitted, waiting)
                        srsMessage = SRS_MSG_NOT_DETERMINED;
                    }
                }
            }
        }
        return srsMessage;
    }

    /**
     * Gets the closing of a project table.
     * @return string of html.
     */
    private String getDatasetTableClose() {
        return "</table></td></tr>";
    }

    /**
     * Used internally to turn a list of datasets into HTML with or
     * without links depending on the flag passed in.
     * @param datasetList a list of dataset items
     * @param remoteUser the user's login (for debug output only)
     * @param isProjectAdmin true if current user is a project administrator of the given datasets
     * @param isDataShopAdmin true if current user is a DataShop Administrator
     * @param projectNumPapers the number of papers associated with the project
     * @param numDatasetsWithProblemContent the number of datasets with problem content
     * @return HTML
     */
    private String getDatasetHtml(Collection datasetList, String remoteUser,
                                  boolean isProjectAdmin, boolean isDataShopAdmin,
                                  Long projectNumPapers, Integer numDatasetsWithProblemContent) {
        String htmlStr = "";
        int count = 0;
        if (projectNumPapers == null) { projectNumPapers = new Long(0); }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        // Get the PC Helper
        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();

        if (datasetList == null || datasetList.size() < 1) {
            if (logger.isTraceEnabled()) {
                logger.trace("User(" + remoteUser + ") "
                        + "getDatasetHtml: # datasets = null or less than 1");
            }
            return "<tbody><tr><td><p>No Datasets</p></td>"
                + "<td class=\"domain_learnlab\"></td>"
                + "<td class=\"dates\"></td>"
                + "<td class=\"status\"></td>"
                + "<td class=\"transactions\"></td>"
                + "</tr></tbody>";
        }

        if (logger.isTraceEnabled()) {
            logger.trace("User(" + remoteUser + ") "
                    + "getDatasetHtml: # datasets = " + datasetList.size());
        }

        htmlStr += "<tbody>";

        for (Iterator iter = datasetList.iterator(); iter.hasNext();) {
            DatasetItem datasetItem = (DatasetItem)iter.next();

            if (count % 2 != 0) {
                htmlStr += "\n<tr class=\"odd\">";
            } else {
                htmlStr += "\n<tr>";
            }
            count++;

            ImportQueueItem iqItem = null;
            if (isProjectAdmin || isDataShopAdmin) {
                iqItem = iqDao.findByDataset(datasetItem);
            }

            // (DS1427) Always allow the display of links
            // get the dataset name
                htmlStr += "<td class=\"name\">";
                if (iqItem != null) {
                    htmlStr += "<span id=\"datasetNameSpan_" + iqItem.getId()
                            + "\" class=\"dataset-name\">";
                }
                htmlStr += "<a href=\"" + ENTRANCE_SERVLET_STRING + "?datasetId="
                        + datasetItem.getId() + "\">"
                        + datasetItem.getDatasetName() + "</a>";
                if (iqItem != null) {
                    htmlStr += "</span>";
                }
                htmlStr += "</td>";

            // get the domain and learnlab
            DomainItem domain = datasetItem.getDomain();
            LearnlabItem learnlab = datasetItem.getLearnlab();
            String domainName = (domain != null) ? domain.getName() : null;
            String learnlabName = (learnlab != null) ? learnlab.getName() : null;
            if (domainName != null) {
                if (learnlabName != null) {

                    if (domainName.equals("Other")) {
                        htmlStr += getOneCell(domainName, "domain_learnlab");
                    } else {
                        htmlStr += getOneCell(domainName + "/" + learnlabName, "domain_learnlab");
                    }
                } else {
                    htmlStr += getOneCell(domainName, "domain_learnlab");
                }
            } else {
                    htmlStr += getOneCell("", "domain_learnlab");
            }
            // get the dates
            htmlStr += getOneCell(DatasetItem.getDateRangeString(datasetItem), "dates");
            // get the status
            htmlStr += getOneCell(datasetItem.getStatus(), "status");

            // get the number of transactions
            Long numTxs = getNumTransactions(datasetItem);
            htmlStr += getOneCell(TX_FORMATTER.format(numTxs), "transactions");

            PcConversionDatasetMapDao pcConversionDatasetMapDao =
                    DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
            boolean problemContentAvailable =
                pcConversionDatasetMapDao.isDatasetMapped(datasetItem);

            if ((projectNumPapers > 0) && problemContentAvailable) {     // both

                String theTdValue = "";

                String pcTitle = "This dataset contains the problem content that students saw.";
                Long numPapers = datasetDao.countPapers(datasetItem);
                if (numPapers > 0) {
                    String paperTitle;
                    if (numPapers == 1) {
                        paperTitle = "There is 1 paper attached to this dataset.";
                    } else {
                        paperTitle = "There are " + numPapers + " papers attached to this dataset.";
                    }
                    theTdValue =
                          "<a href=\"Files?datasetId=" + datasetItem.getId() + "\">"
                        + "<img title=\"" + paperTitle
                        + "\" alt=\"papers\" src=\"images/page_white_text.png\">"
                        + "</a>"
                        + "&nbsp;"
                        + "<a href=\"javascript:goToDatasetProblemList("
                            + datasetItem.getId() + ")\">"
                        + "<img title=\"" + pcTitle
                        + "\" alt=\"problem content\" class=\"brickImg\" src=\"images/brick.png\">"
                        + "</a>";
                } else {
                    theTdValue =
                          "<a href=\"javascript:goToDatasetProblemList("
                                  + datasetItem.getId() + ")\">"
                        + "<img title=\"" + pcTitle
                        + "\" alt=\"problem content\" class=\"brickImg\" src=\"images/brick.png\">"
                        + "</a>";
                }

                htmlStr += getOneCell(theTdValue, "papersAndPCImg");

            } else if (projectNumPapers > 0) {      // papers only

                Long numPapers = datasetDao.countPapers(datasetItem);
                if (numPapers > 0) {
                    String title;
                    if (numPapers == 1) {
                        title = "There is 1 paper attached to this dataset.";
                    } else {
                        title = "There are " + numPapers + " papers attached to this dataset.";
                    }
                    String papersTdValue =
                        "<a href=\"Files?datasetId=" + datasetItem.getId() + "\">"
                        + "<img title=\"" + title
                        + "\" alt=\"papers\" src=\"images/page_white_text.png\">"
                        + "</a>";
                    htmlStr += getOneCell(papersTdValue, "papersImg");
                } else {
                    htmlStr += getOneCell("", "papersImg");
                }

            } else if (problemContentAvailable) {   // pc only

                String pcTdValue =
                    "<img title=\"This dataset contains the problem content that students saw.\""
                    + " alt=\"problem content\" class=\"brickImg\" src=\"images/brick.png\">";
                htmlStr += getOneCell(pcTdValue, "pcImg");

            } else if (numDatasetsWithProblemContent != null
                    && numDatasetsWithProblemContent > 0) {   // another dataset has PC

                String tdValue = "";
                htmlStr += getOneCell(tdValue, "pcImg");

            }

            //gear column, iqItem null means to show disabled gear
            if (isProjectAdmin || isDataShopAdmin) {
                htmlStr += getOneCell(getGearIcon(remoteUser, datasetItem, iqItem, isDataShopAdmin),
                                      "gear");
            }

            htmlStr += "</tr>";
        }
        htmlStr += "</tbody>";
        return htmlStr;
    } // end method getCurriculumHtml

    /**
     * A single cell of the the database with class name and value inserted properly.
     * @param value the content of the cell
     * @param cssClassName CSS class name for the cell.
     * @return string of HTML.
     */
    private String getOneCell(String value, String cssClassName) {
        if (value != null && cssClassName != null) {
            return "<td class=\"" + cssClassName + "\">" + value + "</td>";
        } else if (cssClassName != null) {
            return "<td class=\"" + cssClassName + "\"></td>";
        } else {
            return "<td></td>";
        }
    }

    /**
     * Determine number of transactions for a given dataset.
     * @param dataset the dataset item
     * @return numTransactions
     */
    private Long getNumTransactions(DatasetItem dataset) {
        RemoteDatasetInfoItem remoteDataset = getRemoteDataset(dataset);
        if (remoteDataset == null) {
            return DaoFactory.DEFAULT.getSampleMetricDao().getTotalTransactions(dataset);
        } else {
            return remoteDataset.getNumTransactions();
        }
    }

    /**
     * Helper method to determine if a dataset is remote.
     * @param dataset the DatasetItem
     * @return boolean flag indicating dataset is remote
     */
    private boolean isDatasetRemote(DatasetItem dataset) {
        boolean result = false;

        DatasetInstanceMapDao dimDao = DaoFactory.DEFAULT.getDatasetInstanceMapDao();
        if (dimDao != null) {
            result = dimDao.isDatasetRemote(dataset);
        }

        return result;
    }

    /**
     * Helper method to determine if a discourse is remote.
     * @param discourse the DiscourseItem
     * @return boolean flag indicating discourse is remote
     */
    private boolean isDiscourseRemote(DiscourseItem discourse) {
        boolean result = false;

        DiscourseInstanceMapDao dimDao = DaoFactory.DEFAULT.getDiscourseInstanceMapDao();
        if (dimDao != null) {
            result = dimDao.isDiscourseRemote(discourse);
        }

        return result;
    }

    /**
     * Helper method to get RemoteDatasetInfoItem if specified dataset
     * is remote. Returns null if dataset is not remote.
     * @param dataset the DatasetItem
     * @return RemoteDatasetInfoItem the remote dataset
     */
    private RemoteDatasetInfoItem getRemoteDataset(DatasetItem dataset) {
        if (isDatasetRemote(dataset)) {
            RemoteDatasetInfoDao rdiDao = DaoFactory.DEFAULT.getRemoteDatasetInfoDao();
            List<RemoteDatasetInfoItem> remoteList = rdiDao.findByDataset(dataset);
            if ((remoteList != null) && (remoteList.size() > 0)) {
                return remoteList.get(0);
            }
        }

        return null;
    }

    /**
     * Helper method to get RemoteDiscourseInfoItem if specified discourse
     * is remote. Returns null if discourse is not remote.
     * @param discourse the DiscourseItem
     * @return RemoteDiscourseInfoItem the remote discourse
     */
    private RemoteDiscourseInfoItem getRemoteDiscourse(DiscourseItem discourse) {
        if (isDiscourseRemote(discourse)) {
            RemoteDiscourseInfoDao rdiDao = DaoFactory.DEFAULT.getRemoteDiscourseInfoDao();
            List<RemoteDiscourseInfoItem> remoteList = rdiDao.findByDiscourse(discourse);
            if ((remoteList != null) && (remoteList.size() > 0)) {
                return remoteList.get(0);
            }
        }

        return null;
    }

    /**
     * Helper method to get RemoteInstanceItem if specified dataset
     * is remote. Returns null if dataset is not remote.
     * @param dataset the DatasetItem
     * @return RemoteInstanceItem the remote instance
     */
    private RemoteInstanceItem getRemoteInstance(DatasetItem dataset) {

        RemoteInstanceItem result = null;

        DatasetInstanceMapDao dimDao = DaoFactory.DEFAULT.getDatasetInstanceMapDao();
        if (dimDao == null) return result;

        List<DatasetInstanceMapItem> mapList = dimDao.findByDataset(dataset);
        if ((mapList != null) && (mapList.size() > 0)) {
            DatasetInstanceMapItem mapItem = mapList.get(0);
            result = mapItem.getRemoteInstance();
        }

        return result;
    }

    /**
     * Helper method to get RemoteInstanceItem if specified discourse
     * is remote. Returns null if discourse is not remote.
     * @param discourse the DiscourseItem
     * @return RemoteInstanceItem the remote instance
     */
    private RemoteInstanceItem getRemoteInstance(DiscourseItem discourse) {

        RemoteInstanceItem result = null;

        DiscourseInstanceMapDao dimDao = DaoFactory.DEFAULT.getDiscourseInstanceMapDao();
        if (dimDao == null) return result;

        List<DiscourseInstanceMapItem> mapList = dimDao.findByDiscourse(discourse);
        if ((mapList != null) && (mapList.size() > 0)) {
            DiscourseInstanceMapItem mapItem = mapList.get(0);
            result = mapItem.getRemoteInstance();
        }

        return result;
    }

    /** String constant. */
    private static final String TOOLTIP1 =
              "Only datasets created through the web application can be moved, renamed or deleted.";
    /** String constant. */
    private static final String TOOLTIP2 =
              "Only the uploader of a dataset can delete it.";
    /** String constant. */
    /**
     * Get the gear icon HTML for the last column on projects
     * where the user is a project administrator
     * or the user is a DataShop administrator.
     * @param remoteUser the user's login
     * @param datasetItem the current dataset
     * @param iqItem and import queue item
     * @param isDataShopAdmin true if current user is a DataShop Administrator
     * @return HTML
     */
    private String getGearIcon(String remoteUser, DatasetItem datasetItem,
            ImportQueueItem iqItem, boolean isDataShopAdmin) {

        if (iqItem == null) {
            return "<span class=\"disabledGear\">"
            + "<img id=\"gearImage\" alt=\"Gear\""
                + "src=\"images/gear-arrow-down-disabled.png\""
              + "title=\"" + TOOLTIP1 + "\">"
            + "</span>";
        }

        String iqId = iqItem.getId().toString();

        String disabledString = "_disabled ui-state-disabled";
        String deleteDisabledString = "";
        String disabledDeleteToolTip = "";

        if (!remoteUser.equals(iqItem.getUploadedBy().getId()) && !isDataShopAdmin) {
            deleteDisabledString = disabledString;
            disabledDeleteToolTip = TOOLTIP2;
        }

        return "<div id=\"gearDropdown_" + iqId + "\" class=\"gearDropdown\">"
        + "<a id=\"gearAnchor_" + iqId + "\" class=\"gearAnchor\">"
            + "<img id=\"gearImage\" alt=\"Gear\" src=\"images/gear-arrow-down.png\">"
        + "</a>"
        + "<div id=\"gearSubmenu_" + iqId + "\" class=\"gearSubmenu\">"
        + "<ul class=\"gearUl\">"
            + "<li><a id=\"moveDatasetLink_" + iqId + "\""
                + "class=\"move_dataset_link\">Move to another project</a></li>"
            + "<li><a id=\"renameDatasetLink_" + iqId + "\""
                + "class=\"rename_dataset_link\">Rename</a></li>"
            + "<li><a id=\"deleteDatasetLink_" + iqId + "\""
                + "class=\"delete_dataset_link" + deleteDisabledString  + "\""
                + "title=\"" + disabledDeleteToolTip + "\">Delete</a></li>"
        + "</ul>"
        + "</div>"
        + "<input type=\"hidden\" id=\"datasetName_" + iqId
            + "\" value=\"" + datasetItem.getDatasetName() + "\">"
        + "</div>";
    }

    /**
     * Returns a String representing the Bread Crumb for this page.
     * @return String of HTML for the bread crumb NAV.
     */
    public String displayBreadCrumb() {
        String htmlStr = "<ul><li>Home</li></ul>";
        return htmlStr;
    }

    /**
     * Initialized the Project info given a dataset item.
     * @param datasetItem the selected dataset
     * @return an object that holds all we need to display a project
     */
    public ProjectInfoReport getProjectInfoReport(DatasetItem datasetItem) {
        logger.debug("getProjectInfoReport(datasetItem)");

        ProjectInfoReportDao dao = DaoFactory.DEFAULT.getProjectInfoReportDao();
        ProjectInfoReport projectInfo = dao.getProjectInfoReport(datasetItem);

        return projectInfo;
    }

    /**
     * Initialized the Project info given a project id.
     * @param projectId the id of selected project
     * @return an object that holds all we need to display a project
     */
    public ProjectInfoReport getProjectInfoReport(Integer projectId) {
        logger.debug("getProjectInfoReport(projectId)");
        return getProjectInfoReport(projectId, null);
    }

    /**
     * Initialized the Project info given a project id.
     * @param projectId the id of selected project
     * @param username the account Id
     * @return an object that holds all we need to display a project
     */
    public ProjectInfoReport getProjectInfoReport(Integer projectId, String username) {
        logger.debug("getProjectInfoReport(projectId, username)");
        userDao = DaoFactory.DEFAULT.getUserDao();
        userTermsOfUseMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();
        authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();
        ProjectInfoReportDao dao = DaoFactory.DEFAULT.getProjectInfoReportDao();

        ProjectInfoReport projectInfo = dao.getProjectInfoReport(projectId);
        projectInfo.setPublicFlag(authorizationDao.isPublic(projectId));
        // Use getProjectDao() instead of projectDao
        projectInfo.setNumPapers(getProjectDao().countPapers(new ProjectItem(projectId)));

        boolean termsAccepted = false;

        // Use getProjectDao() instead of projectDao
        TermsOfUseVersionItem termsOfUseVersion =
                getTermsOfUseVersionDao().getProjectTerms(projectId, null);
        if (termsOfUseVersion != null) {
            TermsOfUseItem termsOfUse = termsOfUseVersion.getTermsOfUse();

            if (username != null) {
                UserItem userItem = null;
                userItem = userDao.find(username);
                if (userItem != null) {
                    UserTermsOfUseMapId mapId = new UserTermsOfUseMapId(termsOfUse, userItem);
                    termsAccepted = userTermsOfUseMapDao.get(mapId) != null ? true : false;
                }
            }
        }
        projectInfo.setTermsAcceptedFlag(termsAccepted);

        return projectInfo;
    }

    /**
     * Check if the user has agreed to the latest DataShop terms of use.
     * @param username the account ID
     * @return true if the user has agreed to the latest DataShop terms of use, false otherwise
     */
    public boolean hasAgreedToDataShopTerms(String username) {
        boolean agreedFlag = false;
        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionItem versionItem = termsVersionDao.getDataShopTerms(null);

        if (versionItem == null) {
            agreedFlag = true;
        } else {
            UserTermsOfUseMapDao userTouMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();
            UserTermsOfUseMapItem userTouMapItem =
                userTouMapDao.findByUserAndVersion(username, versionItem);
            if (userTouMapItem != null) {
                agreedFlag = true;
            }
        }

        return agreedFlag;
    }

    /**
     * Figure out if the project has a terms of use associated with it,
     * if it does, then figure out if the user has agreed to the latest version of those terms.
     * @param username the ID of the user
     * @param projectId the ID of the project
     * @return true if user has agreed or no terms associated, false otherwise
     */
    public boolean hasAgreedToProjectTerms(String username, Integer projectId) {
        boolean flag = true;

        projectTermsOfUseMapDao = DaoFactory.DEFAULT.getProjectTermsOfUseMapDao();
        userTermsOfUseMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();
        termsOfUseVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();

        List<TermsOfUseItem> termsOfUseList =
            (List<TermsOfUseItem>)projectTermsOfUseMapDao.findDistinctTermsOfUse(projectId);
        if (termsOfUseList.size() > 0) {
            TermsOfUseVersionItem versionItem =
                termsOfUseVersionDao.getProjectTerms(projectId, null);
            UserTermsOfUseMapItem userMapItem =
                userTermsOfUseMapDao.findByUserAndVersion(username, versionItem);
            if (userMapItem != null) {
                flag = true;
            } else {
                flag = false;
            }

        } else {
            flag = true;
        }

        return flag;
    }

    /**
     * Figure out if the project has a terms of use associated with it,
     * if it does, then figure out if the user has agreed to the latest version of those
     * terms for the specified dataset in the project.
     * @param username the ID of the user
     * @param projectId the ID of the project
     * @param datasetId the ID of the dataset
     * @return true if user has agreed or no terms associated, false otherwise
     */
    public boolean hasAgreedToDatasetTerms(String username, Integer projectId, Integer datasetId) {
        boolean flag = true;

        projectTermsOfUseMapDao = DaoFactory.DEFAULT.getProjectTermsOfUseMapDao();
        datasetUserTermsOfUseMapDao = DaoFactory.DEFAULT.getDatasetUserTermsOfUseMapDao();
        termsOfUseVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();

        List<TermsOfUseItem> termsOfUseList =
            (List<TermsOfUseItem>)projectTermsOfUseMapDao.findDistinctTermsOfUse(projectId);
        if (termsOfUseList.size() > 0) {
            TermsOfUseVersionItem versionItem =
                termsOfUseVersionDao.getProjectTerms(projectId, null);
            DatasetUserTermsOfUseMapItem dsUserTOUMapItem =
                datasetUserTermsOfUseMapDao.findByDatasetUserAndVersion(datasetId,
                                                                        username, versionItem);
            if (dsUserTOUMapItem != null) {
                flag = true;
            } else {
                flag = false;
            }

        } else {
            flag = true;
        }

        return flag;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Helper method to determine if a dataset was uploaded by the
     * specified user.
     * @param datasetItem the dataset
     * @param userId the user
     * @return flag indicating if a dataset was uploaded by the user
     */
    private boolean getIsUploader(DatasetItem datasetItem, String userId) {
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqDao.findByDataset(datasetItem);
        if (iqItem == null) { return false; }

        iqItem = iqDao.get((Integer)iqItem.getId());
        UserItem uploader = iqItem.getUploadedBy();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        uploader = userDao.get((String)uploader.getId());
        return uploader.equals(userDao.get(userId));
    }

    /**
     * Gets the beginning html for a project table for DiscourseDB.
     * @param projectItem the ProjectItem
     * @param remoteUser the user_id of the remote user.
     * @return string of html.
     */
    private String getDiscourseDbTable(ProjectItem projectItem, String remoteUser) {

        if (projectItem == null) { return ""; }

        Integer projectId = (Integer)projectItem.getId();
        String projectName = projectItem.getProjectName();

        String html = "\n<tr><td>";
        String accessRequestHtml = getAccessRequestButton(remoteUser, projectId);
        html += accessRequestHtml;
        html += "<h2 class=\"projectname\">";

        html += "<a href=\"Project?id=" + projectId + "\" class=\"project-name\">"
            + projectName + "</a>";

        userDao = DaoFactory.DEFAULT.getUserDao();

        String piName = null;
        UserItem pi = projectItem.getPrimaryInvestigator();
        if (pi != null) {
            piName = pi.getName();
            if (piName != null) {
                html += "&nbsp;<span class=\"pi-name\">PI: " + piName + "</span>&nbsp;";
            }
        }
        if (authorizationDao.isPublic(projectId)) {
            html += " "
                + "<img src=\"images/users.gif\" "
                + "alt=\"(public)\" title=\"This is a public project.\" />";
        } else {
            html += " "
                + "<img src=\"images/lock.png\" "
                + "alt=\"(private)\" title=\"This is a private project.\" />";
        }

        // Open the table...
        html += " "
             + "</h2>"
             + "</td></tr>"
             + "\n<tr><td><table class=\"dataset-box discourse-table\">"
             + "<thead><tr>"
             + "<th class=\"discourse_name\">Discourse</th>"
             + "<th class=\"discourse_dates\">Dates</th>"
             + "<th class=\"users\">Users</th>"
             + "<th class=\"contributions\">Contributions</th>"
             + "<th class=\"data_sources\">Data Sources</th>";

        html += "</tr></thead>";

        // Fill the table...
        html += getDiscoursePartHtml(projectId);

        // Close the table...
        html += "</table></td></tr>";

        return html;
    }

    /**
     * Helper method to populate the DiscourseDB project table. For now, all
     * of the DiscourseDB datasets are in this single project so no input
     * args are required.
     * @param projectId the project id
     * @return the HTML describing the table contents
     */
    private String getDiscoursePartHtml(Integer projectId) {

        String htmlStr = "<tbody>";
        int count = 0;

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        DiscoursePartDao discoursePartDao = DiscourseDbDaoFactory.DEFAULT.getDiscoursePartDao();
        ContributionDao contributionDao = DiscourseDbDaoFactory.DEFAULT.getContributionDao();
        DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();

        List<DiscourseItem> discourseList = discourseDao.findByProject(projectId);

        if (discourseList == null || discourseList.size() < 1) {
            htmlStr += "<tr><td><p>No Discourses</p></td>"
                + "<td class=\"discourse_dates\"></td>"
                + "<td class=\"users\"></td>"
                + "<td class=\"contributions\"></td>"
                + "<td class=\"data_sources\"></td>"
                + "</tr>";
        }

        for (DiscourseItem discourse : discourseList) {
            if (count % 2 != 0) {
                htmlStr += "\n<tr class=\"odd\">";
            } else {
                htmlStr += "\n<tr>";
            }
            count++;

            htmlStr += "<td class=\"discourse_name\">";
            htmlStr += "<a href=\"" + DISCOURSE_SERVLET_STRING + "?discourseId="
                + discourse.getId() + "\">"
                + discourse.getName() + "</a>";
            htmlStr += "</td>";

            String dateRange = "";
            String numUsers = "";
            String numContributions = "";
            String numDataSources = "";

            RemoteDiscourseInfoItem rdii = getRemoteDiscourse(discourse);
            if (rdii == null) {
                // A local discourse...
                Date startTime = discourseDao.getStartTimeByDiscourse(discourse);
                Date endTime = discourseDao.getEndTimeByDiscourse(discourse);
                dateRange = getDateRangeString(startTime, endTime);
                DUserDao userDao = DiscourseDbDaoFactory.DEFAULT.getDUserDao();
                numUsers = Long.toString(userDao.getCountByDiscourse(discourse));
                numContributions = Long.toString(contributionDao.getCountByDiscourse(discourse));
                numDataSources = Long.toString(dsDao.getCountByDiscourse(discourse));
            } else {
                // A remote discourse...
                dateRange = rdii.getDateRange();
                if (rdii.getNumUsers() != null) { numUsers = Long.toString(rdii.getNumUsers()); }
                if (rdii.getNumContributions() != null) {
                    numContributions = Long.toString(rdii.getNumContributions());
                }
                if (rdii.getNumDataSources() != null) {
                    numDataSources = Long.toString(rdii.getNumDataSources());
                }
            }

            htmlStr += getOneCell(dateRange, "discourse_dates");
            htmlStr += getOneCell(numUsers, "users");
            htmlStr += getOneCell(numContributions, "contributions");
            htmlStr += getOneCell(numDataSources, "data_sources");
            htmlStr += "</tr>";
        }

        htmlStr += "</tbody>";

        return htmlStr;
    }

    /** Format for the date range method, getDateRangeString. */
    private static FastDateFormat prettyDateFormat = FastDateFormat.getInstance("MMM d, yyyy");

    /**
     * Helper method to generate a string of date ranges given start and end times.
     * @param startTime the starting time
     * @param endTime the ending time
     * @return String the range
     */
    private String getDateRangeString(Date startTime, Date endTime) {
        String dateRangeString = "-";

        if (startTime != null) {
            dateRangeString = prettyDateFormat.format(startTime);
        }
        if ((startTime != null) && (endTime != null)) {
            dateRangeString += " - ";
        }
        if (endTime != null) {
            if (dateRangeString == null) {
                dateRangeString = "";
            }
            dateRangeString += prettyDateFormat.format(endTime);
        }
        return dateRangeString;
    }

} //end class ProjectHelper
