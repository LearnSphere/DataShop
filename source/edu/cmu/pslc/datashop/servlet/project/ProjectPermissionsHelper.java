/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2013
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.commons.collections.comparators.NullComparator;

import edu.cmu.pslc.datashop.dao.AccessReportDao;
import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dto.AccessReportInfo;
import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestHelper;

 /**
 * Helper class for ProjectPermissionsServlet.
 *
 * @author Cindy Tipper
 * @version $Revision: 10830 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-21 13:12:28 -0400 (Fri, 21 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectPermissionsHelper {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the debug logger.
     * @return logger - an instance of the logger for this class
     */
    public Logger getLogger() { return logger; }

    /** Default constructor. */
    public ProjectPermissionsHelper() {
        logger.info("ProjectPermissionsHelper.constructor");
    }

    /**
     * Helper method to retrieve list of access requests for a given project.
     * @param projectItem the project
     * @param context the ProjectPermissionsContext specifying query criteria
     * @param currentUser the logged in user
     * @return List<ProjectRequestDTO>
     */
    public List<ProjectRequestDTO> getRequestsForAccess(ProjectItem projectItem,
                                                        ProjectPermissionsContext context,
                                                        UserItem currentUser) {

        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        List<ProjectRequestDTO> result = new ArrayList<ProjectRequestDTO> ();

        // Add access requests
        List<AccessRequestStatusItem> notReviewed = (List<AccessRequestStatusItem>)
            arStatusDao.findAllNotReviewedByProject(projectItem);

        // Determine if currentUser is PA (acting for PI)
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean isPA = authDao.isProjectAdmin(currentUser, projectItem);
        String role = projectItem.getRole(currentUser, isPA);

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        for (AccessRequestStatusItem arStatus : notReviewed) {
            UserItem userItem = userDao.get((String) arStatus.getUser().getId());

            ProjectRequestDTO request = new ProjectRequestDTO();

            // Fill-in only relevant info.
            request.setUserId((String) userItem.getId());
            request.setUserName((String) userItem.getName());
            request.setEmail(userItem.getEmail());
            request.setInstitution(userItem.getInstitution());

            // Get the level that should be shown to the end-user
            String currentLevelShown = getCurrentLevelShown(arStatus);
            request.setLevel(currentLevelShown);

            String status = arStatus.getStatus();

            request.setStatus(status);
            request.setLastActivityDate(arStatus.getLastActivityDate());

            boolean isButtonVisible = true;

            // If approved or denied (even partially) not further voting required.
            if (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)
                || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                isButtonVisible = false;
            } else {
                // If user has already voted, do not show button.
                if ((status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                     && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI))
                    || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)
                        && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP))) {
                    isButtonVisible = false;
                }
            }
            request.setButtonVisible(isButtonVisible);

            result.add(request);
        }

        String sortByColumn = context.getRequestsForAccessSortByColumn();
        Boolean isAscending = context.isRequestsForAccessAscending(sortByColumn);
        Comparator<ProjectRequestDTO> comparator = ProjectRequestDTO.
            getComparator(ProjectRequestDTO.selectSortParameters(sortByColumn, isAscending));
        Comparator<ProjectRequestDTO> nullComparator = new NullComparator(comparator, false);
        Collections.sort(result, nullComparator);

        return result;
    }

    /**
     * Gets the current level that should be shown for requests
     * depending on the state of the access request.
     * [from: AccessRequestHelper.getCurrentLevelShown() [1163]]
     * @param arStatusItem the status item
     * @return the current level that should be displayed
     */
    public String getCurrentLevelShown(AccessRequestStatusItem arStatusItem) {
        // Set the default level as view
        String currentLevelShown = AuthorizationItem.LEVEL_VIEW;
        String authLevel = null;
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        // Get the previous access request history records
        AccessRequestHistoryItem lastRequest = arHistoryDao.findLastRequest(arStatusItem);
        AccessRequestHistoryItem arHistoryPi = arHistoryDao
                .findLastResponse(arStatusItem, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
        AccessRequestHistoryItem arHistoryDp = arHistoryDao
                .findLastResponse(arStatusItem, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);

        // Get the authorization level for this user if an access request status item exists
        if (arStatusItem != null) {
            authLevel = authDao.getAuthorization((String)arStatusItem.getUser().getId(),
                    (Integer)arStatusItem.getProject().getId());

            // Get the current status and history items for the access request
            // Show authorization level if Approved
            if (arStatusItem.getStatus()
                .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
                currentLevelShown = authLevel;
            } else if (arStatusItem.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED)
                    && lastRequest != null) {
                // Show last request if Not Reviewed
                currentLevelShown = lastRequest.getLevel();
             // Last level from PI action
            } else if (arStatusItem.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)) {
                if (lastRequest != null) {
                    // If request exists, show level as last request
                    currentLevelShown = lastRequest.getLevel();
                } else {
                    // If no prior request, show level as view if doubly-Denied
                    currentLevelShown = AuthorizationItem.LEVEL_VIEW;
                }
            } else if (arStatusItem.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                    || arStatusItem.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)) {
                if (arHistoryPi != null) {
                    // If request exists, show level as last request
                    currentLevelShown = arHistoryPi.getLevel();
                } else {
                    // If no prior response, show level as view if doubly-Denied
                    currentLevelShown = AuthorizationItem.LEVEL_VIEW;
                }
            // Last level from DP action
            } else if (arStatusItem.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)
                    || arStatusItem.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                if (arHistoryDp != null) {
                    // If request exists, show level as last request
                    currentLevelShown = arHistoryDp.getLevel();
                } else {
                    // If no prior request, show level as view if doubly-Denied
                    currentLevelShown = AuthorizationItem.LEVEL_VIEW;
                }
            } else {
                currentLevelShown = AuthorizationItem.LEVEL_VIEW;
            }
        }
        return currentLevelShown;
    }

    /**
     * Helper method for granting authorization to a user.
     *
     * @param projectItem the project
     * @param userItem the user
     * @param authLevel the authorization level
     */
    public void addNewUserAccess(ProjectItem projectItem, UserItem userItem, String authLevel) {

        AuthorizationItem authItem = new AuthorizationItem(userItem, projectItem);
        authItem.setLevel(authLevel);

        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        authDao.saveOrUpdate(authItem);
    }

    /**
     * Helper method for retrieving project Access Report.
     *
     * @param projectIdList list of projects to query
     * @param context the ProjectPermissionsContext specifying query criteria
     * @return List<ProjectRequestDTO> list of objects specifying the 'Access Report'
     */
    public List<ProjectRequestDTO> getAccessReport(List<Integer> projectIdList,
                                                   ProjectPermissionsContext context) {

        AccessRequestHelper arHelper = HelperFactory.DEFAULT.getAccessRequestHelper();

        List<String> arsStatuses = null;
        String piDpId = null;
        Boolean showAdmin = context.getShowAdmins();
        String arSearchBy = context.getAccessReportSearchBy();
        // Permissions page supports only searching by user name and id
        AccessReportInfo arInfo = new AccessReportInfo(arsStatuses, piDpId, projectIdList,
                                                       null,  // projectName
                                                       arSearchBy,  // user
                                                       null,  // institution
                                                       showAdmin, "OR");

        String sortByColumn = context.getAccessReportSortByColumn();
        Boolean isAscending = context.isAccessReportAscending(sortByColumn);

        String arOrderBy = arHelper.getOrderByClause(sortByColumn, isAscending);

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        int currentPage = context.getAccessReportCurrentPage();
        int rowsPerPage = context.getAccessReportRowsPerPage();
        List<ProjectRequestDTO> accessReport =
            arDao.getProjectRequests(arInfo, arOrderBy,
                                     arHelper.calcPageOffset(currentPage, rowsPerPage),
                                     rowsPerPage);

        return arHelper.postProcessResults(accessReport);
    }

    /**
     * Helper method for retrieving number of Access Report records.
     *
     * @param projectIdList list of projects to query
     * @param context the ProjectPermissionsContext specifying query criteria
     * @return Integer total num records in the 'Access Report'
     */
    public Integer getAccessReportCount(List<Integer> projectIdList,
                                        ProjectPermissionsContext context) {

        List<String> arsStatuses = null;
        String piDpId = null;
        Boolean showAdmin = context.getShowAdmins();
        String arSearchBy = context.getAccessReportSearchBy();
        AccessReportInfo arInfo = new AccessReportInfo(arsStatuses, piDpId, projectIdList,
                                                       null,  // projectName
                                                       arSearchBy,  // user
                                                       null,  // institution
                                                       showAdmin, "OR");

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        return arDao.getAccessReportCount(arInfo);
    }

    /**
     * Helper method for retrieving project Current Permissions.
     *
     * @param projectIdList list of projects to query
     * @param context the ProjectPermissionsContext specifying query criteria
     * @return List<ProjectRequestDTO> list of objects specifying the 'Current Permissions'
     */
    public List<ProjectRequestDTO> getCurrentPermissions(List<Integer> projectIdList,
                                                         ProjectPermissionsContext context) {

        AccessRequestHelper arHelper = HelperFactory.DEFAULT.getAccessRequestHelper();

        List<String> arsStatuses = null;
        String piDpId = null;
        Boolean showAdmin = context.getShowAdmins();
        String cpSearchBy = context.getCurrentPermissionsSearchBy();
        // Permissions page supports only searching by user name and id
        AccessReportInfo arInfo = new AccessReportInfo(arsStatuses, piDpId, projectIdList,
                                                       null,  // projectName
                                                       cpSearchBy,  // user
                                                       null,  // institution
                                                       showAdmin, "OR");

        String sortByColumn = context.getCurrentPermissionsSortByColumn();
        Boolean isAscending = context.isCurrentPermissionsAscending(sortByColumn);

        String cpOrderBy = arHelper.getOrderByClause(sortByColumn, isAscending);

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        int currentPage = context.getCurrentPermissionsCurrentPage();
        int rowsPerPage = context.getCurrentPermissionsRowsPerPage();
        List<ProjectRequestDTO> currentPermissions =
            arDao.getCurrentPermissions(arInfo, cpOrderBy,
                                        arHelper.calcPageOffset(currentPage, rowsPerPage),
                                        rowsPerPage);

        return arHelper.postProcessResults(currentPermissions);
    }

    /**
     * Helper method for retrieving number of CurrentPermissions records.
     *
     * @param projectIdList list of projects to query
     * @param context the ProjectPermissionsContext specifying query criteria
     * @return Integer total num records in the 'Current Permissions'
     */
    public Integer getCurrentPermissionsCount(List<Integer> projectIdList,
                                              ProjectPermissionsContext context) {

        List<String> arsStatuses = null;
        String piDpId = null;
        Boolean showAdmin = context.getShowAdmins();
        String cpSearchBy = context.getCurrentPermissionsSearchBy();
        AccessReportInfo arInfo = new AccessReportInfo(arsStatuses, piDpId, projectIdList,
                                                       null,  // projectName
                                                       cpSearchBy,  // user
                                                       null,  // institution
                                                       showAdmin, "OR");

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        return arDao.getCurrentPermissionsCount(arInfo);
    }

    /**
     * Helper method for retrieving flag to indicate if any records
     * have Terms of Use applied.
     *
     * @param projectIdList list of projects to query
     * @param context the ProjectPermissionsContext specifying query criteria
     * @return Boolean indicating whether or not ToU are present
     */
    public Boolean getHasTermsOfUse(List<Integer> projectIdList,
                                    ProjectPermissionsContext context) {
        
        List<String> arsStatuses = null;
        String piDpId = null;
        Boolean showAdmin = context.getShowAdmins();
        AccessReportInfo arInfo = new AccessReportInfo(arsStatuses, piDpId, projectIdList,
                                                       null,  // projectName
                                                       null,  // user
                                                       null,  // institution
                                                       showAdmin, "OR");

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        return arDao.getHasTermsOfUse(arInfo);
    }

} //end class ProjectPermissionsHelper
