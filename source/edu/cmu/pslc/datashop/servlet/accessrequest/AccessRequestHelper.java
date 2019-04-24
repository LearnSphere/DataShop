 /* Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.accessrequest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AccessReportDao;
import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dao.hibernate.AccessRequestHistoryDaoHibernate;
import edu.cmu.pslc.datashop.dto.AccessReportInfo;
import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;
import edu.cmu.pslc.datashop.dto.ProjectRequestHistoryDTO;
import edu.cmu.pslc.datashop.dto.UserRequestDTO;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;


/**
 * Business tier class for managing Access Requests.
 *
 * @author Mike Komisin
 * @version $Revision: 14097 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-06-05 15:51:25 -0400 (Mon, 05 Jun 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessRequestHelper extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Make project public button title - "Voted to make this project public". */
    public static final String BUTTON_TITLE_HAS_VOTED = "Voted to make this project public";
    /** Make project public button title - "Vote to make this project public". */
    public static final String BUTTON_TITLE_VOTE = "Vote to make this project public";
    /** Make project public button title - "Vote to make this project public". */
    public static final String BUTTON_TITLE_MAKE = "Make this project public";
    /** Access Request status to be displayed to end users - "Approved". */
    public static final String ACCESS_REQUEST_DISPLAY_APPROVED = "Approved";
    /** Access Request status to be displayed to end users - "Partially Approved". */
    public static final String ACCESS_REQUEST_DISPLAY_PARTIALLY_APPROVED = "Partially Approved";
    /** Access Request status to be displayed to end users - "Denied". */
    public static final String ACCESS_REQUEST_DISPLAY_DENIED = "Denied";
    /** Access Request status to be displayed to end users - "Not Reviewed". */
    public static final String ACCESS_REQUEST_DISPLAY_NOT_REVIEWED = "Not Reviewed";

    /** Access Request enumerated button title - "Re-request Access". */
    public static final String BUTTON_TITLE_REREQUEST = "Re-request Access";
    /** Access Request enumerated button title - "Access Requested". */
    public static final String BUTTON_TITLE_REQUESTED = "Access Requested";

    /** Access Request enumerated action string - "request". */
    public static final String NOTIFY_ACTION_REQUEST = "request";
    /** Access Request enumerated action string - "approved". */
    public static final String NOTIFY_ACTION_APPROVED = "approved";
    /** Access Request enumerated action string - "denied". */
    public static final String NOTIFY_ACTION_DENIED = "denied";
    /** Access Request enumerated action string - "revoked". */
    public static final String NOTIFY_ACTION_REVOKED = "revoked";
    /** Access Request enumerated action string - "modified". */
    public static final String NOTIFY_ACTION_MODIFIED = "modified";
    /** Access Request enumerated action string - "updated". */
    public static final String NOTIFY_ACTION_UPDATED = "updated";
    /** Access Request email notification string for level - "no". */
    public static final String NOTIFY_LEVEL_NO_ACCESS = "no";


    /**
     * Allows user to request access to a project.
     * @param baseUrl the base URL for DataShop
     * @param requestorItem the requestor
     * @param projectItem the project item
     * @param level the access level
     * @param reason the reason for requesting access
     * @param isAddNewUserRequest is request on behalf of user
     * @return the AccessRequestStatusItem
     */
    public AccessRequestStatusItem requestAccess(String baseUrl, UserItem requestorItem,
                                                 ProjectItem projectItem, String level,
                                                 String reason, boolean isAddNewUserRequest) {
        // Do not allow Public user (%) to ever request project dmin access
        if (requestorItem.getId().equals(UserItem.DEFAULT_USER)
            && level.equals(AuthorizationItem.LEVEL_ADMIN)) {
            logger.info("User '" + requestorItem.getId() + "' cannot be given '" + level
                    + "' access to Project '" + projectItem.getProjectName() + "'");
            return null;
        }
        // Role is requestor, and action is request
        String role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_REQUESTOR;
        String action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_REQUEST;
        // Status and History Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        // History Item
        AccessRequestHistoryItem arHistoryRequestorItem = null;
        // If this is a request, is it valid?
        boolean isRequestValid = false;
        // Notification action will be request or re-request
        String notificationAction = "";
        // Status Item
        AccessRequestStatusItem arStatus =
                (AccessRequestStatusItem) arStatusDao
                    .findByUserAndProject(requestorItem, projectItem);

        if (arStatus != null) {
            // Explicitly load the status item
            arStatus = arStatusDao.get((Integer) arStatus.getId());
            // Is the request valid
            isRequestValid = arHistoryDao.isRequestValid(arStatus);
        }

        // Found arStatus record, update arStatus for this user and project accordingly
        if (arStatus != null) {
            if (isRequestValid
                    || arStatus.getStatus()
                        .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                    || arStatus.getStatus()
                        .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                    || arStatus.getStatus()
                        .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)
                    || arStatus.getStatus()
                        .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
            Date now = (Date) Calendar.getInstance().getTime();
            // Notify DP/PI of re-request
            notificationAction = NOTIFY_ACTION_REQUEST;
            // Find out if a previous request exists that is active
            AccessRequestHistoryItem lastRequest = arHistoryDao.findLastRequest(arStatus);

            // Get last history entry for requestorItem, PI, and DP
            arHistoryRequestorItem = AccessRequestUtil.createHistoryEntry(
                    arStatus, requestorItem, role, action, level, reason, false);

            AccessRequestHistoryItem arHistoryPi = arHistoryDao
                .findLastResponse(arStatus,
                    AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);

            AccessRequestHistoryItem arHistoryDp = arHistoryDao
                .findLastResponse(arStatus,
                    AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);

            AccessRequestHistoryItem arHistoryAdmin = arHistoryDao
                .findLastResponse(arStatus,
                    AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);

            // User is re-requesting, reset last user request
            if (lastRequest != null) {
                lastRequest.setIsActive(false);
                arHistoryDao.saveOrUpdate(lastRequest);
                lastRequest = null;
            }
            // Admin voted only, reset last admin vote
            if (arHistoryAdmin != null
                    && arHistoryPi == null && arHistoryDp == null) {
                boolean adminVoteMatches = arHistoryAdmin.getLevel().equals(level);
                boolean adminDenied = !arHistoryAdmin.getAction().equals(
                        AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE);
                if ((!adminDenied && !adminVoteMatches) || adminDenied) {
                    arHistoryAdmin.setIsActive(false);
                    arHistoryDao.saveOrUpdate(arHistoryAdmin);
                    arHistoryAdmin = null;
                }

            // Admin voted, PI  or DP voted, reset all votes
            } else if (arHistoryAdmin != null
                    && (arHistoryPi != null || arHistoryDp != null)) {
                // Reset PI vote if it exists
                if (arHistoryPi != null) {
                    arHistoryPi.setIsActive(false);
                    arHistoryDao.saveOrUpdate(arHistoryPi);
                    arHistoryPi = null;
                }
                // Reset DP vote if it exists
                if (arHistoryDp != null) {
                    arHistoryDp.setIsActive(false);
                    arHistoryDao.saveOrUpdate(arHistoryDp);
                    arHistoryDp = null;
                }
                // Reset admin vote
                arHistoryAdmin.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryAdmin);
                arHistoryAdmin = null;

            } else if (arHistoryPi != null || arHistoryDp != null) {
            // PI or DP voted, reset vote if necessary

                // PI voted
                if (arHistoryPi != null) {
                    boolean piVoteMatches = arHistoryPi.getLevel().equals(level);
                     boolean piDenied = !arHistoryPi.getAction().equals(
                             AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE);
                     if ((!piDenied && !piVoteMatches) || piDenied) {
                         arHistoryPi.setIsActive(false);
                         arHistoryDao.saveOrUpdate(arHistoryPi);
                         arHistoryPi = null;
                     }
                }
                // DP voted
                if (arHistoryDp != null) {
                    boolean dpVoteMatches = arHistoryDp.getLevel().equals(level);
                    boolean dpDenied = !arHistoryDp.getAction().equals(
                            AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE);
                    if ((!dpDenied && !dpVoteMatches) || dpDenied) {
                           arHistoryDp.setIsActive(false);
                        arHistoryDao.saveOrUpdate(arHistoryDp);
                        arHistoryDp = null;
                    }
                }
            }
            // Now, set the status based on the PI/DP/Admin logic
            arStatus.setStatus(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED);
            arStatus.setLastActivityDate(now);
            arStatusDao.saveOrUpdate(arStatus);

            // Update status and authorization level if necessary
            updateStatusOnChange(baseUrl, arStatus, arHistoryPi, arHistoryDp, arHistoryAdmin,
                    arHistoryRequestorItem, null, false, role);
            }
        // Create a new arStatus for this user and project
        } else { // if (arStatus == null) {
            // Notify DP/PI of request
            notificationAction = NOTIFY_ACTION_REQUEST;
            // Create new status and history entries for this request
            String status = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED;
            arStatus = createStatusEntry(requestorItem, projectItem, status);
            arHistoryRequestorItem = AccessRequestUtil.createHistoryEntry(arStatus, requestorItem,
                                                                          role, action, level,
                                                                          reason, false);
            // isRequestValid should be set to true since a
            // new access request status item was created
            isRequestValid = true;
        }
        arStatus.setHasRequestorSeen(true);
        arStatusDao.saveOrUpdate(arStatus);

        // Use a TreeSet since they enforce distinct keys (in case PI is the DP)
        TreeSet<UserItem> recipients = new TreeSet<UserItem>();
        if (projectItem.getPrimaryInvestigator() != null) {
            recipients.add(projectItem.getPrimaryInvestigator());
        }
        if (projectItem.getDataProvider() != null) {
            recipients.add(projectItem.getDataProvider());
        }

        if (isRequestValid) {
            logger.info("Valid request. [ user (" + requestorItem.getId()
                    + "), project (" + projectItem.getId() + ") ]. User will be notified.");
            // Notify the PI and the DP of the request, or DS help, if applicable.
            // No emails sent at this point if request was made on behalf of the user,
            // i.e., from Project Permissions: Add New User.
            if (!isAddNewUserRequest) {
                notify(baseUrl, recipients, null, requestorItem, projectItem,
                       notificationAction, level, null, arHistoryRequestorItem, role);
            }
        } else {
            logger.info("Invalid request made. [ user (" + requestorItem.getId()
                    + "), project (" + projectItem.getId() + ") ]. User will not be notified.");
        }

        return arStatus;
    }

    /**
     * Allows PI/DP to respond to an access request on their project.
     * @param baseUrl the base URL for DataShop
     * @param respondent the respondent
     * @param requestor the requestor
     * @param project the project
     * @param action the action
     * @param level the access level
     * @param reason the reason for requesting access
     * @param shareReasonFlag share reason flag
     */
    public void respond(String baseUrl, UserItem respondent,
            UserItem requestor, ProjectItem project,
            String action, String level, String reason, boolean shareReasonFlag) {
        // Do not allow Public user (%) to ever get project admin access
        if (requestor.getId().equals(UserItem.DEFAULT_USER)
            && level.equals(AuthorizationItem.LEVEL_ADMIN)) {
            logger.info("User '" + requestor.getId() + "' cannot be given '" + level
                + "' access to a project.");
            return;
        }

        // Determine if respondent is PA (acting for PI)
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean isPA = authDao.isProjectAdmin(respondent, project);
        String role = project.getRole(respondent, isPA);

        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        AccessRequestStatusItem arStatus =
                (AccessRequestStatusItem) arStatusDao
                    .findByUserAndProject(requestor, project);

        // Either we found arStatus record so update arStatus for this user and project accordingly,
        // or we create a new request for this user since no status item exists
        if (arStatus == null) {
            String status = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED;
            arStatus = createStatusEntry(requestor, project, status);
        }
        if (arStatus != null) {
            // Get authorization table info
            String auth = authDao.getAuthorization(
                    (String)arStatus.getUser().getId(),
                    (Integer)project.getId());
            // Get last history entry for requestor, PI, and DP
            AccessRequestHistoryItem arHistoryRequestor = arHistoryDao
                    .findLastRequest(arStatus);
            AccessRequestHistoryItem arHistoryPi = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
            AccessRequestHistoryItem arHistoryDp = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
            AccessRequestHistoryItem arHistoryAdmin = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);

            AccessRequestHistoryItem arHistoryCurrent = null;

            // Find out which level we referenced during access denial
            if (arHistoryRequestor != null) {
                // If the PI or DP selects AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY,
                // then the requested level be the current level
                if (action.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                    level = arHistoryRequestor.getLevel();
                }
            } else if (arHistoryRequestor == null
                    && action.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                // Responding without any request (for users in the Authorization table)
                if (auth != null) {
                    level = auth;
                } else {
                    level = AuthorizationItem.LEVEL_VIEW;
                }
            }
            // Create a new history entry for the respondent and update the status if necessary
            if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                if (arHistoryPi != null && arHistoryPi.getAction().equals(action)
                        && arHistoryPi.getLevel().equals(level)) {
                    arHistoryPi.setReason(reason);
                    arHistoryDao.saveOrUpdate(arHistoryPi);
                } else {
                    arHistoryPi = AccessRequestUtil.createHistoryEntry(arStatus, respondent,
                        role, action, level, reason, shareReasonFlag);
                    arHistoryCurrent = arHistoryPi;
                }
            } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP)) {
                if (arHistoryDp != null && arHistoryDp.getAction().equals(action)
                        && arHistoryDp.getLevel().equals(level)) {
                    arHistoryDp.setReason(reason);
                    arHistoryDao.saveOrUpdate(arHistoryDp);
                } else {
                    arHistoryDp = AccessRequestUtil.createHistoryEntry(arStatus, respondent,
                        role, action, level, reason, shareReasonFlag);
                    arHistoryCurrent = arHistoryDp;
                }
            } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN)) {
                if (arHistoryAdmin != null && arHistoryAdmin.getAction().equals(action)
                        && arHistoryAdmin.getLevel().equals(level)) {
                    arHistoryAdmin.setReason(reason);
                    arHistoryDao.saveOrUpdate(arHistoryAdmin);
                } else {
                    arHistoryAdmin = AccessRequestUtil.createHistoryEntry(arStatus, respondent,
                        role, action, level, reason, shareReasonFlag);
                    arHistoryCurrent = arHistoryAdmin;
                }
            }
            // Refresh the votes to get the latest isActive states
            arHistoryRequestor = arHistoryDao.findLastRequest(arStatus);
            arHistoryPi = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
            arHistoryDp = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
            arHistoryAdmin = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);
            updateStatusOnChange(baseUrl, arStatus, arHistoryPi, arHistoryDp,
                    arHistoryAdmin, arHistoryRequestor, reason, shareReasonFlag, role);
            // Set the activity notification flag based on user role
            if (respondent.getAdminFlag()) {
                arStatus.setHasAdminSeen(true);
            }
            if (project.getPrimaryInvestigator() != null
                    && project.getPrimaryInvestigator().equals(respondent)) {
                arStatus.setHasPiSeen(true);
            }
            if (project.getDataProvider() != null
                    && project.getDataProvider().equals(respondent)) {
                arStatus.setHasDpSeen(true);
            }
            arStatusDao.saveOrUpdate(arStatus);

            // notify the PI xor DP of a status update
            TreeSet<UserItem> recipients = new TreeSet<UserItem>();
            UserItem adminItem = null;
            // Determine which user to inform of the changes (the other project owner)
            if (project.getOwnership().equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)) {
                if (project.getPrimaryInvestigator().equals(respondent)) {
                    // If the PI updates, notify the DP
                    recipients.add(project.getDataProvider());
                } else if (project.getDataProvider().equals(respondent)) {
                    // If the DP updates, notify the PI
                    recipients.add(project.getPrimaryInvestigator());
                } else if (respondent.getAdminFlag()) {
                 // If the administrator updates, then notify the PI/DP
                    if (project.getPrimaryInvestigator() != null) {
                        recipients.add(project.getPrimaryInvestigator());
                    }
                    if (project.getDataProvider() != null) {
                        recipients.add(project.getDataProvider());
                    }
                    adminItem = respondent;
                } else if (isPA) {
                    // Project Admin acts on behalf of the PI so notify DP.
                    if (project.getDataProvider() != null) {
                        recipients.add(project.getDataProvider());
                    }
                }
            } else {
                // There is only one person, a PI or DP (or PI is the DP)
                boolean isRespondentPI = project.getPrimaryInvestigator() == null ? false
                    : respondent.equals(project.getPrimaryInvestigator());
                boolean isRespondentDP = project.getDataProvider() == null ? false
                    : respondent.equals(project.getDataProvider());

                // If an admin (project or DS) updates, then notify the PI/DP. This is generally
                // a no-op as the call to notify() below requires that the status item be neither
                // approved nor denied, and a change by an admin means it's approved or denied
                if ((respondent.getAdminFlag() || isPA) && !isRespondentPI && !isRespondentDP) {
                    // Include the PI or DP in the list of recipients, if applicable
                    if (project.getPrimaryInvestigator() != null) {
                        recipients.add(project.getPrimaryInvestigator());
                    }
                    if (project.getDataProvider() != null) {
                        recipients.add(project.getDataProvider());
                    }
                    // set the admin item
                    if (respondent.getAdminFlag()) {
                        adminItem = respondent;
                    }
                }
            }
            // A status item exists
            if (arStatus != null) {
                // The status item is neither approved nor denied
                // (it's in an incomplete state)
                if (!arStatus.getStatus().equals(
                        AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)
                        && !arStatus.getStatus().equals(
                                AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                    && recipients.size() > 0) {
                    // Notify the other party (PI xor DP) that a response has occurred
                    // And they are expected to respond
                    notify(baseUrl, recipients, adminItem, requestor, project,
                            NOTIFY_ACTION_UPDATED, level, action, arHistoryCurrent, role);
                }
            }
        }
    }

    /**
     * Allows administrator to change access levels for any user, project pair.
     * @param baseUrl the base URL for DataShop
     * @param respondent the respondent
     * @param requestor the requestor
     * @param project the project
     * @param action the action
     * @param level the access level
     * @param reason the reason for requesting access
     * @param shareReasonFlag share reason flag
     */
    public void newAccess(String baseUrl, UserItem respondent, UserItem requestor,
            ProjectItem project, String action, String level,
            String reason, boolean shareReasonFlag) {
        boolean adminFlag = true;
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean isPA = authDao.isProjectAdmin(respondent, project);

        if (!respondent.getAdminFlag() && !isPA) {
            return;
        } else if (requestor.getId().equals(UserItem.DEFAULT_USER)
            && level.equals(AuthorizationItem.LEVEL_ADMIN)) {
            logger.info("User '" + requestor.getId() + "' cannot be given '" + level
                    + "' access to Project '" + project.getProjectName() + "'");
            return;
        }

        // Get project ownership info
        String role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN;
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        // Get the status
        AccessRequestStatusItem arStatus =
                (AccessRequestStatusItem) arStatusDao
                    .findByUserAndProject(requestor, project);

        // Either we found arStatus record so update arStatus for this user and project accordingly,
        // or we create a new request for this user since no status item exists
        if (arStatus == null) {

            String status = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED;
            arStatus = createStatusEntry(requestor, project, status);
        }

        if (arStatus != null) {
            // Get authorization table info
            String auth = authDao.getAuthorization(
                    (String)arStatus.getUser().getId(),
                    (Integer)project.getId());
            // Get last history entry for requestor, PI, and DP
            AccessRequestHistoryItem arHistoryRequestor = arHistoryDao
                    .findLastRequest(arStatus);

            // A request in the history was found
            if (arHistoryRequestor != null) {
                // The radio buttons can have a value of view, edit, or deny
                // If the PI or DP selects AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY,
                // then the requested level is taken to be the current level
                if (action.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                    level = arHistoryRequestor.getLevel();
                }


            } else if (arHistoryRequestor == null) {
                // Responding without any request (for users in the Authorization table)
                if (auth != null
                        && action.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                    level = auth;
                } else if (action.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                    level = AuthorizationItem.LEVEL_VIEW;
                }
            }

            // Create a new history entry for the respondent and update the status if necessary
            AccessRequestHistoryItem arHistoryAdmin = arHistoryDao.findLastResponse(arStatus,
                        AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);


            // Only update the reason if the response's action and level are the same
            if (arHistoryAdmin != null && arHistoryAdmin.getAction().equals(action)
                    && arHistoryAdmin.getLevel().equals(level)) {
                arHistoryAdmin.setReason(reason);
                arHistoryDao.saveOrUpdate(arHistoryAdmin);
            } else {
                arHistoryAdmin = AccessRequestUtil.createHistoryEntry(arStatus, respondent,
                        role, action, level, reason, shareReasonFlag);
            }

            // Get admin, PI, and DP responses if they exist
            arHistoryAdmin = arHistoryDao.findLastResponse(arStatus,
                        AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);
            AccessRequestHistoryItem arHistoryPI = arHistoryDao.findLastResponse(arStatus,
                    AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
            AccessRequestHistoryItem arHistoryDp = arHistoryDao.findLastResponse(arStatus,
                    AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
            // Update status
            updateStatusOnChange(baseUrl, arStatus, arHistoryPI, arHistoryDp,
                    arHistoryAdmin, arHistoryRequestor, reason, shareReasonFlag, role);

            arStatus.setHasAdminSeen(false);
            arStatusDao.saveOrUpdate(arStatus);
        }
    }


    /**
     * Returns true if the user already voted to make the project public.
     * @param projectId the project id
     * @param role the user role on the project
     * @return true if the user already voted to make the project public
     */
    public Boolean hasVotedProjectPublic(Integer projectId, String role) {
        Boolean votedPublic = false;
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserItem publicUser = userDao.get(UserItem.DEFAULT_USER);
        ProjectItem projectItem = projectDao.get(projectId);
        // Instantiate Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        // Get status
        AccessRequestStatusItem arStatus =
                (AccessRequestStatusItem) arStatusDao
                    .findByUserAndProject(publicUser, projectItem);

        if (arStatus != null) {
            // Find last response for the specified role
            AccessRequestHistoryItem arHistory = arHistoryDao.findLastResponse(arStatus, role);
            if (arHistory != null && arHistory.getLevel().equals(AuthorizationItem.LEVEL_PUBLIC)) {
                votedPublic = true;
            }
        }

        return votedPublic;
    }


    /**
     * Returns a UserItem for the last response for the specified project and role.
     * @param projectId the project id
     * @param role the user role on the project
     * @return response, a UserItem, null if not found.
     */
    public UserItem getLastResponseUserItem(Integer projectId, String role) {
        UserItem result = null;
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserItem publicUser = userDao.get(UserItem.DEFAULT_USER);
        ProjectItem projectItem = projectDao.get(projectId);
        // Instantiate Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        // Get status
        AccessRequestStatusItem arStatus =
                (AccessRequestStatusItem) arStatusDao
                    .findByUserAndProject(publicUser, projectItem);

        if (arStatus != null) {
            // Find last response for the specified role
            AccessRequestHistoryItem arItem = arHistoryDao.findLastResponse(arStatus, role);
            if (arItem != null) {
                result = userDao.get((String)arItem.getUser().getId());
            }
        }

        return result;
    }

   /**
     * Returns a map that includes the TOU name, version, and terms for a project.
     * @param projectItem the project item
     * @return a map that includes the TOU name, version, and terms for a project
     */
    public Map<String, Object> getTouInfo(ProjectItem projectItem) {
        Map<String, Object> touMap = new HashMap<String, Object>();
     // Get terms of use information
        TermsOfUseVersionDao termsOfUseVersionDao =
                DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseDao touDao =
                DaoFactory.DEFAULT.getTermsOfUseDao();

        if (projectItem != null) {
            // Get the applied terms of use version for this project
            TermsOfUseVersionItem termsOfUseVersion =
                termsOfUseVersionDao.getProjectTerms((Integer) projectItem.getId(), null);
            // If the Terms and version exist
            if (termsOfUseVersion != null && termsOfUseVersion.getTermsOfUse() != null) {
                Integer touId = (Integer) termsOfUseVersion.getTermsOfUse().getId();
                TermsOfUseItem touItem = touDao.get(touId);

                // Set current TOU Version for this project
                String version = termsOfUseVersion.getVersion().toString();
                String touName = touItem.getName();
                String terms = termsOfUseVersion.getTerms();
                Date termsEffective = termsOfUseVersion.getAppliedDate();
                touMap.put("name", touName);
                touMap.put("version", version);
                touMap.put("terms", terms);
                touMap.put("termsEffective", termsEffective);
            }
        }
        return touMap;
    }

   /**
     * Returns a map that includes the TOU name, version, and terms for DataShop.
     * @return a map that includes the TOU name, version, and terms for DataShop
     */
    public Map<String, Object> getDataShopTouInfo() {

        Map<String, Object> touMap = null;

        // Get terms of use information
        TermsOfUseVersionDao termsOfUseVersionDao =
                DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseDao touDao =
                DaoFactory.DEFAULT.getTermsOfUseDao();

        // Get the applied terms of use version for DataShop
        TermsOfUseVersionItem termsOfUseVersion =
            termsOfUseVersionDao.getDataShopTerms(null);

        // If the Terms and version exist
        if (termsOfUseVersion != null && termsOfUseVersion.getTermsOfUse() != null) {

            touMap = new HashMap<String, Object>();

            Integer touId = (Integer) termsOfUseVersion.getTermsOfUse().getId();
            TermsOfUseItem touItem = touDao.get(touId);

            // Set current TOU Version for DataShop
            String version = termsOfUseVersion.getVersion().toString();
            String touName = touItem.getName();
            String terms = termsOfUseVersion.getTerms();
            Date termsEffective = termsOfUseVersion.getAppliedDate();
            touMap.put("name", touName);
            touMap.put("version", version);
            touMap.put("terms", terms);
            touMap.put("termsEffective", termsEffective);
        }

        return touMap;
    }

    /**
     * Returns a DTO with the terms of use information added for the project and user.
     * @param request the ProjectRequestDTO
     * @param projectItem the project item
     * @param userItem the user item
     * @return the modified DTO
     */
    public ProjectRequestDTO setTouInformation(ProjectRequestDTO request,
            ProjectItem projectItem, UserItem userItem) {
     // Get terms of use information
        TermsOfUseVersionDao termsOfUseVersionDao =
                DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseDao touDao =
                DaoFactory.DEFAULT.getTermsOfUseDao();
        UserTermsOfUseMapDao userTermsOfUseMapDao =
                DaoFactory.DEFAULT.getUserTermsOfUseMapDao();


        if (projectItem != null) {
            // Get the applied terms of use version for this project
            TermsOfUseVersionItem termsOfUseVersion =
                termsOfUseVersionDao.getProjectTerms((Integer) projectItem.getId(), null);
            // If the Terms and version exist
            if (termsOfUseVersion != null && termsOfUseVersion.getTermsOfUse() != null) {
                TermsOfUseItem touItem = termsOfUseVersion.getTermsOfUse();
                touItem = touDao.get((Integer) touItem.getId());
                // Set current TOU Version for this project
                request.setTouVersionCurrent(termsOfUseVersion.getVersion());
                // Set TOU name
                UserTermsOfUseMapId mapId = new UserTermsOfUseMapId(touItem, userItem);
                UserTermsOfUseMapItem userTouItem = userTermsOfUseMapDao.get(mapId);
                String touName = touItem.getName();
                request.setTouName(touName);
                if (userTouItem != null && userTouItem.getTermsOfUseVersion() != null) {
                    // Set Version agreed and Date agreed
                    TermsOfUseVersionItem versionAccepted =
                            userTouItem.getTermsOfUseVersion();
                    versionAccepted = termsOfUseVersionDao.get((Integer) versionAccepted.getId());
                    Integer versionNumber = versionAccepted.getVersion();
                    Date dateAgreed = userTouItem.getDate();

                    request.setTouVersionAgreed(versionNumber);
                    request.setTouDateAgreed(dateAgreed);
                } else {
                    request.setTouVersionAgreed(0);
                    request.setTouDateAgreed(null);
                }
            } else {
                request.setTouName(null);
                request.setTouVersionAgreed(0);
                request.setTouDateAgreed(null);
            }
        }
        return request;
    }

    /**
     * Returns the correct title based on the last request.
     * @param lastRequestDate the last request date
     * @param statusItem the Access Request status item
     * @param userId the logged in user's id
     * @param projectId the displayed project's id
     * @return the title
     */
    public String getButtonTitleForRequest(Date lastRequestDate,
                                           AccessRequestStatusItem statusItem,
                                           String userId,
                                           Integer projectId) {
        AccessRequestHistoryDaoHibernate arStatusDao = (AccessRequestHistoryDaoHibernate)
            DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        String title = null;
        String status = statusItem != null
                ? statusItem.getStatus() : null;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = (Date) cal.getTime();

        if (lastRequestDate == null || status == null) {
            if (userHasViewAccess(userId, projectId)) {
                // If project is public or user had access prior to release 5.3,
                // user can request Edit access.
                title = AccessRequestStatusItem.BUTTON_TITLE_REQUEST_EDIT;
            } else {
                // Button says: Request if last request date is null
                title = AccessRequestStatusItem.BUTTON_TITLE_REQUEST;
            }
        } else if (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)
                   && userHasViewAccess(userId, projectId)) {
            // If user has 'view' access, allow them to request 'edit'
            title = AccessRequestStatusItem.BUTTON_TITLE_REQUEST_EDIT;
        } else if (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
            || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
            || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
            // Button says: Re-request if status is denied
            title = BUTTON_TITLE_REREQUEST;
        } else if (!arStatusDao.isRequestValid(statusItem)) {
            // Button says: Access Requested if 24 hours haven't passed
            title = BUTTON_TITLE_REQUESTED;
        } else if (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
            title = AccessRequestStatusItem.BUTTON_TITLE_REQUEST;
        } else if (lastRequestDate.before(yesterday)) {
            // Button says: Re-request if 24 hours since last request
            title = BUTTON_TITLE_REREQUEST;
        } else {
            // To prevent a blank button
            title = AccessRequestStatusItem.BUTTON_TITLE_REQUEST;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("getButtonTitleForRequest :: " + title);
        }

        return title;
    }

    /**
     * Returns the isEnabled flag for a button given its title.
     * @param buttonTitle the title of the button
     * @return the isEnabled flag
     */
    public boolean getButtonEnabled(String buttonTitle) {
        boolean flag = false;
        if (buttonTitle.equals(AccessRequestStatusItem.BUTTON_TITLE_REQUEST)
            || buttonTitle.equals(AccessRequestStatusItem.BUTTON_TITLE_REQUEST_EDIT)
            || buttonTitle.equals(BUTTON_TITLE_REREQUEST)) {
            flag = true;
        }
        return flag;
    }

    /**
     * Returns the isEnabled flag for the make project
     * public button given its title.
     * @param buttonTitle the title of the button
     * @return the isEnabled flag
     */
    public boolean getPublicButtonEnabled(String buttonTitle) {
        boolean flag = false;
        if (buttonTitle == null || buttonTitle.equals("")) {
            flag = false;
        } else if (!buttonTitle.equals(BUTTON_TITLE_HAS_VOTED)) {
            flag = false;
        } else if (!buttonTitle.equals(BUTTON_TITLE_VOTE)) {
            flag = true;
        }
        return flag;
    }

    /**
     * Creates a new status entry for the Access Request Status item.
     * @param user the user
     * @param project the project
     * @param status the status
     * @return the AccessRequestHistoryItem
     */
    public AccessRequestStatusItem createStatusEntry(UserItem user, ProjectItem project,
            String status) {
        Date now = (Date) Calendar.getInstance().getTime();
        // Instantiate Dao
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        AccessRequestStatusItem statusItem = null;
        // Load the status item
        statusItem = new AccessRequestStatusItem();
        statusItem.setUser(user);
        statusItem.setProject(project);
        statusItem.setStatus(status);
        statusItem.setLastActivityDate(now);
        statusItem.setHasPiSeen(false);
        statusItem.setHasDpSeen(false);
        statusItem.setHasAdminSeen(false);
        statusItem.setHasRequestorSeen(false);
        statusItem.setEmailStatus(AccessRequestStatusItem.EMAIL_STATUS_NONE);
        // Save
        arStatusDao.saveOrUpdate(statusItem);

        logger.info("Created new status entry for"
                + " ProjectId (" + project.getId() + ")"
                + " and User Id (" + user.getId() + ")");

        return statusItem;
     }  // end of createStatusEntry

    /**
     * Returns a list of UserRequestDTO's.
     * @param user the user
     * @return a list of UserRequestDTO's
     */
    public List<UserRequestDTO> getUserRequestList(UserItem user) {
        // Instantiate Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        // Instantiate array list to hold UserRequestDTO's
        List<UserRequestDTO> requestList = new ArrayList<UserRequestDTO> ();
        // Add access requests
        List<AccessRequestStatusItem> myRequests =
                (List<AccessRequestStatusItem>) arStatusDao.findByUser(user);

        for (AccessRequestStatusItem arStatus : myRequests) {
            // Get the last request for this status item

            UserRequestDTO request = getUserRequestForDisplay(
                    (String)arStatus.getUser().getId(),
                    (Integer)arStatus.getProject().getId());
            if (request != null) {
                requestList.add(request);
                // Save activity notifications
                arStatus.setHasRequestorSeen(true);
                arStatusDao.saveOrUpdate(arStatus);
            }
        }
        return requestList;
    }


    /**
     * Returns a new UserRequestDTO given an user and project, or returns null
     * if we cannot get the status or request.
     * @param userId the user Id
     * @param projectId the project Id
     * @return the new UserRequestDTO or null if we cannot get the status or request
     */
    public UserRequestDTO getUserRequest (
            String userId, Integer projectId) {
        // Make sure project and user exist
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        ProjectItem projectItem = projectDao.get(projectId);
        UserItem userItem = userDao.get(userId);
        if (projectItem == null || userId == null) {
            return null;
        }

        UserRequestDTO request = new UserRequestDTO();

        // Get current authorization level if there is one
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        String auth = authDao.getAuthorization(userId, projectId);

        // Get the AccessRequestStatus item and the AccessRequestHistory's last request
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        AccessRequestStatusItem arStatus = arStatusDao.findByUserAndProject(userItem, projectItem);
        AccessRequestHistoryItem lastRequest = null;

        // Set Project information
        request.setProjectId((Integer)(projectItem.getId()));
        request.setProjectName(projectItem.getProjectName());

        // Set Ownership Information
        UserItem piUserItem = projectItem.getPrimaryInvestigator();
        if (piUserItem != null) {
            request.setPiName(piUserItem.getName());
        }
        UserItem dpUserItem = projectItem.getDataProvider();
        if (dpUserItem != null) {
            request.setDpName(dpUserItem.getName());
        }

        if (arStatus != null) {
           // A status exists

           // Get last request
           lastRequest = arHistoryDao.findLastRequest(arStatus);

           request.setStatus(arStatus.getStatus());

           String theLevel = null;
           if (lastRequest != null) {
               theLevel = lastRequest.getLevel();

               // A previous request has been made by the requestor
               // Set user request information
               request.setLastRequest(lastRequest.getDate());
               request.setReason(lastRequest.getReason());
               // Set GUI information
               String buttonTitle = getButtonTitleForRequest(lastRequest.getDate(),
                                                             arStatus,
                                                             userId,
                                                             projectId);
               request.setButtonTitle(buttonTitle);
               request.setButtonEnabled(getButtonEnabled(buttonTitle));
           } else { // last request is null
               theLevel = getCurrentLevelShown(arStatus);

               // A previous request has NOT been made by the requestor
               // Set default request information
               request.setLastRequest(null);
               request.setReason(null);
               // Set GUI information
               String buttonTitle = getButtonTitleForRequest(null, null, userId, projectId);
               request.setButtonTitle(buttonTitle);
               request.setButtonEnabled(getButtonEnabled(buttonTitle));
           }
           request.setLevel(theLevel);
           request.setRecent(!arStatus.getHasRequestorSeen());

       // User only exists in authorization table
       } else {
           request.setStatus(null);
           request.setLevel(auth);
           request.setLastRequest(null);
           request.setReason(null);
           // Set GUI information
           request.setRecent(false);
           String buttonTitle = getButtonTitleForRequest(null, null, userId, projectId);
           request.setButtonTitle(buttonTitle);
           request.setButtonEnabled(getButtonEnabled(buttonTitle));
       }

       return request;
    }

    /**
     * Helper method to determine if a user has 'view' access to a project.
     * @param userId the user
     * @param projectId the project
     * @return boolean indication
     */
    private boolean userHasViewAccess(String userId, Integer projectId) {
        if (userId == null || projectId == null) { return false; }

        AuthorizationDao authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();
        String authLevel = authorizationDao.getAuthLevel(userId, projectId);
        if (authLevel != null && authLevel.equals(AuthorizationItem.LEVEL_VIEW)) {
            return true;
        }
        return false;
    }

    /**
     * Returns a new UserRequestDTO meant for display to the end-user
     * given an user and project, or returns null if the row should
     * not appear in the user request table. This method modifies the
     * AccessRequestStatusItem's hasRequestorSeen attributes.
     * @param userId the user Id
     * @param projectId the project Id
     * @return the new UserRequestDTO or null if the user already has access
     */
    private UserRequestDTO getUserRequestForDisplay (
            String userId, Integer projectId) {
        // Instantiate Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        // Make sure project and user exist
        UserRequestDTO request = null;
        ProjectItem projectItem = projectDao.get(projectId);
        UserItem userItem = userDao.get(userId);
        if (projectItem == null || userId == null) {
            return null;
        }

        // Get the AccessRequestStatus item and the AccessRequestHistory's last request
        AccessRequestStatusItem arStatus = arStatusDao.findByUserAndProject(userItem, projectItem);
        AccessRequestHistoryItem lastRequest = null;

        if (arStatus != null) {
            // A status item exists so get last request if one exists
            lastRequest = arHistoryDao.findLastRequest(arStatus);


            // If no requests have been made,
            // then we do not include the row in the user request
            // table unless an administrator created a new row
            Boolean usingAdminResponse = false;
            if (lastRequest == null) {
                usingAdminResponse = true;
                lastRequest = arHistoryDao.findLastResponse(arStatus,
                        AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);
            }

            // Instantiate the request object
            request = new UserRequestDTO();
            // Set Project information
            request.setProjectId((Integer)(projectItem.getId()));
            request.setProjectName(projectItem.getProjectName());

            // Set Ownership Information
            UserItem piUserItem = projectItem.getPrimaryInvestigator();
            if (piUserItem != null) {
                request.setPiName(piUserItem.getName());
            }
            UserItem dpUserItem = projectItem.getDataProvider();
            if (dpUserItem != null) {
                request.setDpName(dpUserItem.getName());
            }

            if (arStatus.getStatus().equals(
                    AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
                request.setStatus(ACCESS_REQUEST_DISPLAY_APPROVED);
            } else if (arStatus.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                   || arStatus.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                   || arStatus.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
               // Set status to denied
               request.setStatus(ACCESS_REQUEST_DISPLAY_DENIED);
           } else if (arStatus.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                   || arStatus.getStatus()
                     .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)) {
               // Set status to partially approved
               request.setStatus(ACCESS_REQUEST_DISPLAY_PARTIALLY_APPROVED);
           } else if (arStatus.getStatus()
                   .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED)) {
               // Set status to not reviewed
               request.setStatus(ACCESS_REQUEST_DISPLAY_NOT_REVIEWED);
           }

           request.setLevel(getCurrentLevelShown(arStatus));

           if (lastRequest != null) {
               // A previous request has been made by the requestor
               // Set user request information
               request.setLastRequest(lastRequest.getDate());
               request.setReason(lastRequest.getReason());
               // Set GUI information
               if (!usingAdminResponse) {
                   String buttonTitle =
                       getButtonTitleForRequest(lastRequest.getDate(), arStatus, userId, projectId);
                   request.setButtonTitle(buttonTitle);
                   request.setButtonEnabled(getButtonEnabled(buttonTitle));
               } else {
                   String buttonTitle = AccessRequestHelper.BUTTON_TITLE_REQUESTED;
                   request.setButtonTitle(buttonTitle);
                   request.setButtonEnabled(getButtonEnabled(buttonTitle));
               }
           }
           // Handle activity notifications
           request.setRecent(!arStatus.getHasRequestorSeen());
           arStatus.setHasRequestorSeen(true);
           arStatusDao.saveOrUpdate(arStatus);
       }

       return request;
    }

    /**
     * Gets the current level that should be shown for requests
     * depending on the state of the access request.
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
     * Returns a list of ProjectRequestDTO's.
     * @param owner the principal investigator or data provider
     * @param filterBy option filter parameter for administrator, cannot be null
     * @return a list of ProjectRequestDTO's
     */
    public List<ProjectRequestDTO> getProjectRequestListNotReviewed(
            UserItem owner, String filterBy) {
        // Instantiate Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        // Instantiate array list to hold ProjectRequestDTO's
        List<ProjectRequestDTO> requestList = new ArrayList<ProjectRequestDTO> ();

        // Add access requests
        List<AccessRequestStatusItem> myRequests;
        if (owner.getAdminFlag()) {
            myRequests = (List<AccessRequestStatusItem>)
                    arStatusDao.findAdminNotReviewed(owner, filterBy);
        } else {
            myRequests = (List<AccessRequestStatusItem>)
                    arStatusDao.findNotReviewed(owner);
        }

        for (AccessRequestStatusItem arStatus : myRequests) {
            ProjectItem projectItem = projectDao.get((Integer) arStatus.getProject().getId());
            UserItem userItem = userDao.get((String) arStatus.getUser().getId());

            ProjectRequestDTO request = new ProjectRequestDTO();
            List<ProjectRequestHistoryDTO> projectRequestHistory = getRequestHistoryDTO(arStatus);

            // PI / DP Info
            UserItem piUserItem = projectItem.getPrimaryInvestigator();
            UserItem dpUserItem = projectItem.getDataProvider();
            if (piUserItem != null) {
                request.setPiName(piUserItem.getName());
            }
            if (dpUserItem != null) {
                request.setDpName(dpUserItem.getName());
            }

            // Project information
            request.setProjectRequestHistory(projectRequestHistory);
            request.setProjectId((Integer)(projectItem.getId()));
            request.setProjectName(projectItem.getProjectName());

            request.setUserId((String) userItem.getId());
            request.setUserName((String) userItem.getName());
            request.setEmail(userItem.getEmail());
            request.setInstitution(userItem.getInstitution());

            // Get the level that should be shown to the end-user
            String currentLevelShown = getCurrentLevelShown(arStatus);
            request.setLevel(currentLevelShown);

            // Load the request item
            request.setStatus(arStatus.getStatus());
            request.setLastActivityDate(arStatus.getLastActivityDate());
            request.setFirstAccess(null);
            request.setLastAccess(null);
            request.setTouName(null);
            request.setTouVersionCurrent(0);
            request.setTouVersionAgreed(0);
            request.setTouDateAgreed(null);
            request.setRecent(true);
            request.setShowHistory(false);
            request.setOwnership(projectItem.getOwnership());
            request.setButtonVisible(true);

            requestList.add(request);
        }
        return requestList;
    }

    /**
     * Returns a list of ProjectRequestDTO's.
     * @param owner the principal investigator or data provider
     * @param showAdmins whether to display administrators in the Access Report
     * @return a list of ProjectRequestDTO's
     */
    public List<ProjectRequestDTO> getProjectRequestListReport(
            UserItem owner, Boolean showAdmins) {

        // Get access times for dataset user log
        List<ProjectRequestDTO> logAccessTimes = getLogAccessTimes(owner);

        // Instantiate Access Request and Authorization Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        // Instantiate array list to hold ProjectRequestDTO's
        List<ProjectRequestDTO> requestList = new ArrayList<ProjectRequestDTO> ();

        // Add access requests
        List<AccessRequestStatusItem> myRequests;
        if (owner.getAdminFlag()) {
            myRequests = (List<AccessRequestStatusItem>) arStatusDao.findAdminAccessReport(owner);
        } else {
            myRequests = (List<AccessRequestStatusItem>) arStatusDao.findAccessReport(owner);
        }

        // Add records only found in authorization table
        requestList.addAll(addProjectRequestsFromAuthTable(owner, showAdmins));
        // Keep track of user and project pairs so we can check against the dataset user log
        List<AuthorizationId> authIds = new ArrayList<AuthorizationId> ();
        // Any users in the authorization table also need to be added to authIds
        for (ProjectRequestDTO tempDTO : requestList) {
            AuthorizationId tempId =
                    new AuthorizationId(tempDTO.getUserId(), tempDTO.getProjectId());
            authIds.add(tempId);
        }

        for (AccessRequestStatusItem arStatus : myRequests) {

            ProjectItem projectItem = projectDao.get((Integer) arStatus.getProject().getId());
            UserItem userItem = userDao.get((String) arStatus.getUser().getId());
            if (!showAdmins && userItem.getAdminFlag()) {
                continue;
            }

            authIds.add(new AuthorizationId(userItem, projectItem));

            ProjectRequestDTO request = new ProjectRequestDTO();
            List<ProjectRequestHistoryDTO> projectRequestHistory = getRequestHistoryDTO(arStatus);

            // PI / DP Info
            UserItem piUserItem = projectItem.getPrimaryInvestigator();
            UserItem dpUserItem = projectItem.getDataProvider();
            if (piUserItem != null) {
                request.setPiName(piUserItem.getName());
            }
            if (dpUserItem != null) {
                request.setDpName(dpUserItem.getName());
            }

            // Load the request item
            request.setProjectRequestHistory(projectRequestHistory);
            request.setProjectId((Integer)(projectItem.getId()));
            request.setProjectName(projectItem.getProjectName());
            request.setUserId((String) userItem.getId());
            request.setUserName((String) userItem.getName());
            request.setEmail(userItem.getEmail());
            request.setInstitution(userItem.getInstitution());

            // Set request level to denied if partially denied
            if (arStatus.getStatus().equals(AccessRequestStatusItem
                        .ACCESS_RESPONSE_STATUS_DENIED)
               || arStatus.getStatus().equals(AccessRequestStatusItem
                       .ACCESS_RESPONSE_STATUS_PI_DENIED)
               || arStatus.getStatus().equals(AccessRequestStatusItem
                       .ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                request.setLevel(AccessRequestStatusItem
                        .ACCESS_RESPONSE_STATUS_DENIED);
            } else {
                request.setLevel(getCurrentLevelShown(arStatus));
            }

            request.setStatus(arStatus.getStatus());
            request.setLastActivityDate(arStatus.getLastActivityDate());

            // Get user's dataset access dates
            DatasetUserLogDao datasetUserLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
            Date firstAccess = datasetUserLogDao.getFirstAccess(userItem, projectItem);
            Date lastAccess = datasetUserLogDao.getLastAccess(userItem, projectItem);

            request.setFirstAccess(firstAccess);
            request.setLastAccess(lastAccess);
            request = setTouInformation(request, projectItem, userItem);

            request.setPending(false);
            request.setRecent(false);
            request.setShowHistory(false);
            request.setOwnership(null);
            request.setButtonVisible(false);
            // Add request to the list
            requestList.add(request);
        }

        // Append the dataset user log records that are relevant
        for (ProjectRequestDTO tempDTO : logAccessTimes) {
            UserItem userItem = userDao.get(tempDTO.getUserId());
            AuthorizationId tempId =
                    new AuthorizationId(tempDTO.getUserId(), tempDTO.getProjectId());
            if (!authIds.contains(tempId)) {
                if (!showAdmins && userItem.getAdminFlag()) {
                    continue;
                }
                requestList.add(tempDTO);
            }
        }
        return requestList;
    }

    /**
     * Returns a map of activity notification counts for the sub-tabs.
     * @param userItem the user item
     * @return a map of activity notification counts for the sub-tabs
     */
    public Map<String, Long> getActivityCountsMap(UserItem userItem) {
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        // Get the counts from the DAO methods
        Long myRequestsCount = arStatusDao.countNotificationsMyRequests(userItem);
        Long notReviewedCount = arStatusDao.countNotificationsNotReviewed(userItem);
        Long recentCount = arStatusDao.countNotificationsRecent(userItem);
        Long totalCount = myRequestsCount + notReviewedCount + recentCount;
        // Return a map of these values and their total
        Map<String, Long> map = new HashMap<String, Long> ();
        map.put(AccessRequestStatusItem.STATE_COUNT_MY_REQUESTS, myRequestsCount);
        map.put(AccessRequestStatusItem.STATE_COUNT_NOT_REVIEWED, notReviewedCount);
        map.put(AccessRequestStatusItem.STATE_COUNT_RECENT_ACTIVITY, recentCount);
        map.put(AccessRequestStatusItem.STATE_COUNT_TOTAL, totalCount);

        return map;
    }


    /**
     * Returns a list of ProjectRequestDTO's. This method modifies the
     * AccessRequestStatusItem's attributes.
     * @param ownerItem the principal investigator or data provider
     * @param filterBy option filter parameter for administrator
     * @return a list of ProjectRequestDTO's
     */
    public List<ProjectRequestDTO> getProjectRequestListRecent(
            UserItem ownerItem, String filterBy) {
        // Instantiate Dao's
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        // Instantiate array list to hold ProjectRequestDTO's
        List<ProjectRequestDTO> requestList = new ArrayList<ProjectRequestDTO>();

        // Add access requests
        List<AccessRequestStatusItem> myRequests;
        if (ownerItem.getAdminFlag()) {
            myRequests = (List<AccessRequestStatusItem>)arStatusDao
                    .findAdminRecentActivity(ownerItem, filterBy);
        } else {
            myRequests = (List<AccessRequestStatusItem>) arStatusDao.findRecentActivity(ownerItem);
        }

        // Iterate through the recent requests
        for (AccessRequestStatusItem arStatus : myRequests) {
            ProjectItem projectItem = projectDao.get((Integer) arStatus.getProject().getId());
            UserItem userItem = userDao.get((String) arStatus.getUser().getId());

            ProjectRequestDTO request = new ProjectRequestDTO();
            List<ProjectRequestHistoryDTO> projectRequestHistory = getRequestHistoryDTO(arStatus);

            // PI / DP Info
            UserItem piUserItem = projectItem.getPrimaryInvestigator();
            UserItem dpUserItem = projectItem.getDataProvider();
            if (piUserItem != null) {
                request.setPiName(piUserItem.getName());
            }
            if (dpUserItem != null) {
                request.setDpName(dpUserItem.getName());
            }

            // Project information
            request.setProjectRequestHistory(projectRequestHistory);
            request.setProjectId((Integer)(projectItem.getId()));
            request.setProjectName(projectItem.getProjectName());
            request.setUserId((String) userItem.getId());
            request.setUserName((String) userItem.getName());
            request.setEmail(userItem.getEmail());
            request.setInstitution(userItem.getInstitution());
            // Get current level to be displayed
            String currentLevelShown = getCurrentLevelShown(arStatus);
            request.setLevel(currentLevelShown);
            // Load the request item
            request.setStatus(arStatus.getStatus());
            request.setLastActivityDate(arStatus.getLastActivityDate());
            request.setFirstAccess(null);
            request.setLastAccess(null);
            request.setTouName(null);
            request.setTouVersionCurrent(0);
            request.setTouVersionAgreed(0);
            request.setTouDateAgreed(null);
            request.setPending(false);
            request.setShowHistory(false);
            request.setOwnership(null);

            String role = projectItem.getRole(ownerItem);
            String status = request.getStatus();

            // Set the activity notification flag based on user role
            if (ownerItem.getAdminFlag()) {
                request.setRecent(!arStatus.getHasAdminSeen());
                arStatus.setHasAdminSeen(true);
            }
            if (projectItem.getPrimaryInvestigator() != null
                    && projectItem.getPrimaryInvestigator().equals(ownerItem)) {
                request.setRecent(!arStatus.getHasPiSeen());
                arStatus.setHasPiSeen(true);
            }
            if (projectItem.getDataProvider() != null
                    && projectItem.getDataProvider().equals(ownerItem)) {
                request.setRecent(!arStatus.getHasDpSeen());
                arStatus.setHasDpSeen(true);
            }

            // Save activity notifications
            arStatusDao.saveOrUpdate(arStatus);

            // Show buttons only for certain statuses
            if ((status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)
                        || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED))
                    || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED)
                        && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI))
                    || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED)
                        && role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP))
                    || (status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                     || status.equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED))) {
                request.setButtonVisible(false);
            } else {
                request.setButtonVisible(true);
            }
            requestList.add(request);
        }

        return requestList;
    }

    /**
     * Returns a list of ProjectRequestDTO's found only in the Authorization table.
     * @param owner the DP or PI
     * @param showAdmins whether or not to display administrators in the report
     * @return a list of ProjectRequestDTO's found only in the Authorization table
     */
    private List<ProjectRequestDTO> addProjectRequestsFromAuthTable(
            UserItem owner, Boolean showAdmins) {
        // Instantiate array list to hold ProjectRequestDTO's
        List<ProjectRequestDTO> requestList = new ArrayList<ProjectRequestDTO>();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        // Instantiate list with user, project pairs from the authorization table
        List<AuthorizationId> myAuthIds;

        // Get users from authorization table
        if (owner.getAdminFlag()) {
            myAuthIds = (List<AuthorizationId>) authDao.findAllAuthIds();
        } else {
            myAuthIds = (List<AuthorizationId>) authDao.findAuthsByOwner((String) owner.getId());
        }
        // Iterate over all of the requests
        for (AuthorizationId authId : myAuthIds) {
            ProjectItem projectItem = projectDao.get((Integer) authId.getProjectId());
            UserItem userItem = userDao.get((String) authId.getUserId());
            if (!showAdmins && userItem.getAdminFlag()) {
                continue;
            }
            AccessRequestStatusItem arStatus =
                (AccessRequestStatusItem) arStatusDao.findByUserAndProject(userItem, projectItem);

            if (arStatus == null
                || (arStatus != null && !arStatus.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED))) {

                ProjectRequestDTO request = new ProjectRequestDTO();

                // PI / DP Info
                UserItem piUserItem = projectItem.getPrimaryInvestigator();
                UserItem dpUserItem = projectItem.getDataProvider();
                if (piUserItem != null) {
                    request.setPiName(piUserItem.getName());
                }
                if (dpUserItem != null) {
                    request.setDpName(dpUserItem.getName());
                }

                // Load the request item
                request.setProjectRequestHistory(null);
                request.setProjectId((Integer)(projectItem.getId()));
                request.setProjectName(projectItem.getProjectName());
                request.setUserId((String) userItem.getId());
                request.setUserName((String) userItem.getName());
                request.setEmail(userItem.getEmail());
                request.setInstitution(userItem.getInstitution());
                request.setLevel(authDao.getAuthorization(
                        (String) userItem.getId(), (Integer) projectItem.getId()));
                request.setStatus(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED);


                // Get user's dataset access dates
                DatasetUserLogDao datasetUserLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();

                Date firstAccess = datasetUserLogDao.getFirstAccess(userItem, projectItem);
                Date lastAccess = datasetUserLogDao.getLastAccess(userItem, projectItem);
                request.setFirstAccess(firstAccess);
                request.setLastAccess(lastAccess);

                // Add the TOU information to the request
                request = setTouInformation(request, projectItem, userItem);

                request.setPending(false);
                request.setRecent(false);
                request.setShowHistory(false);
                request.setOwnership(null);
                request.setButtonVisible(false);
                requestList.add(request);
            }
        }
        return requestList;
    }

    /**
     * Returns a list of ProjectRequestHistoryDTO's by status.
     * @param arStatus the AccessRequestStatusItem
     * @return a list of ProjectRequestHistoryDTO's
     */
    private List<ProjectRequestHistoryDTO> getRequestHistoryDTO(
            AccessRequestStatusItem arStatus) {

        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        // Instantiate array list to hold ProjectRequestHistoryDTO's
        List<ProjectRequestHistoryDTO> historyList = new ArrayList<ProjectRequestHistoryDTO> ();

        // Get the history items
        List<AccessRequestHistoryItem> historyItems =
                (List<AccessRequestHistoryItem>)arHistoryDao.findByStatus(arStatus);

        // Load the request and response history for a given status item
        for (AccessRequestHistoryItem historyItem : historyItems) {
            UserItem userItem = userDao.get((String) historyItem.getUser().getId());
            ProjectRequestHistoryDTO historyDTO = new ProjectRequestHistoryDTO();
            historyDTO.setUserName(userItem.getName());
            historyDTO.setEmail(userItem.getEmail());
            historyDTO.setRole(historyItem.getRole());
            historyDTO.setAction(historyItem.getAction());
            historyDTO.setLevel(historyItem.getLevel());
            historyDTO.setReason(historyItem.getReason());
            historyDTO.setDate(historyItem.getDate());
            historyList.add(historyDTO);
        }
        return historyList;
    }

    /**
     * Get a list of ProjectRequestDTO's for users in the dataset user log for all projects.
     * owned by the specified owner.
     * @param ownerItem the data provider or PI
     * @return a list of ProjectRequestDTO's for users in the dataset user log for all projects
     */
    private List<ProjectRequestDTO> getLogAccessTimes(UserItem ownerItem) {

        // Setup the Dao's
        DatasetUserLogDao datasetUserLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        AuthorizationDao authorizationDao = DaoFactory.DEFAULT.getAuthorizationDao();

        // The list of requests (or access times) to return
        List<ProjectRequestDTO> projectRequests = new ArrayList<ProjectRequestDTO> ();

        // If administrator, return all records from the dataset user log
        if (ownerItem.getAdminFlag()) {

            // Constants for getting access times from the Map by administrator
            final int mapUserId = 0;
            final int mapProjectId = 1;
            final int mapFirstAccessId = 2;
            final int mapLastAccessId = 3;
            final int datasetName = 4;
            List<Object> accessTimes = (List<Object>) datasetUserLogDao.getAccessTimes();

            for (Iterator<Object> iter = accessTimes.iterator(); iter.hasNext();) {
                Object[] obj = (Object[]) iter.next();


                ProjectRequestDTO tempDTO = new ProjectRequestDTO();

                // Set the user portion of the DTO
                UserItem userItem = (UserItem)(obj[mapUserId]);
                tempDTO.setUserId((String) userItem.getId());
                tempDTO.setFirstName(userItem.getFirstName());
                tempDTO.setLastName(userItem.getLastName());
                tempDTO.setUserName(userItem.getName());
                tempDTO.setEmail(userItem.getEmail());
                tempDTO.setInstitution(userItem.getInstitution());

                if (obj[mapProjectId] != null) {
                    // Set project info
                    ProjectItem projectItem = (ProjectItem)(projectDao.get(
                            (Integer) obj[mapProjectId]));

                    // PI / DP Info
                    UserItem piUserItem = projectItem.getPrimaryInvestigator();
                    UserItem dpUserItem = projectItem.getDataProvider();
                    if (piUserItem != null) {
                        tempDTO.setPiName(piUserItem.getName());
                    }
                    if (dpUserItem != null) {
                        tempDTO.setDpName(dpUserItem.getName());
                    }

                    tempDTO.setProjectId((Integer) projectItem.getId());
                    tempDTO.setProjectName(projectItem.getProjectName());

                    // Add the TOU information to the request
                    tempDTO = setTouInformation(tempDTO, projectItem, userItem);

                    Integer projectId = (Integer) projectItem.getId();
                    if (authorizationDao.isPublic(projectId)) {
                        String level = authorizationDao.getAuthorization(projectId);
                        if (level != null && level.length() > 0) {
                            if (tempDTO.getUserId().equals(UserItem.DEFAULT_USER)) {
                                String capitalized = level.substring(0, 1).toUpperCase()
                                        + level.substring(1, level.length()).toLowerCase();
                                tempDTO.setLevel(capitalized);
                            } else {
                                tempDTO.setLevel("Public");
                            }
                        }
                    } else {
                        tempDTO.setLevel("-");
                    }

                } else {
                    if (obj[datasetName] != null) {
                        tempDTO.setProjectName("-");
                        tempDTO.setLevel("-");
                    }
                }

                // Set access times
                if (obj[mapFirstAccessId] != null && obj[mapLastAccessId] != null) {
                    tempDTO.setFirstAccess((Date)(obj[mapFirstAccessId]));
                    tempDTO.setLastAccess((Date)(obj[mapLastAccessId]));
                }

                projectRequests.add(tempDTO);

            }
        } else {
            // Constants for getting access times from the Map
            final int map2UserId = 0;
            final int map2FirstAccessId = 1;
            final int map2LastAccessId = 2;
            // Get the projects for this owner
            List<ProjectItem> projectItems = (List<ProjectItem>) projectDao.findByOwner(ownerItem);

            // Get the access times of users who accessed the owner's datasets
            for (ProjectItem projectItem : projectItems) {

                // Assemble the access times from past users into a list of ProjectRequestDTO's
                List<Object> accessTimes = (List<Object>)datasetUserLogDao
                        .getAccessTimesByProject(projectItem);
                for (Iterator<Object> iter = accessTimes.iterator(); iter.hasNext();) {
                    Object[] obj = (Object[]) iter.next();
                    ProjectRequestDTO tempDTO = new ProjectRequestDTO();

                    // Set the user portion of the DTO
                    UserItem userItem = (UserItem)(obj[map2UserId]);
                    tempDTO.setUserId((String) userItem.getId());
                    tempDTO.setFirstName(userItem.getFirstName());
                    tempDTO.setLastName(userItem.getLastName());
                    tempDTO.setUserName(userItem.getName());
                    tempDTO.setEmail(userItem.getEmail());
                    tempDTO.setInstitution(userItem.getInstitution());

                    // PI / DP Info
                    UserItem piUserItem = projectItem.getPrimaryInvestigator();
                    UserItem dpUserItem = projectItem.getDataProvider();
                    if (piUserItem != null) {
                        tempDTO.setPiName(piUserItem.getName());
                    }
                    if (dpUserItem != null) {
                        tempDTO.setDpName(dpUserItem.getName());
                    }

                    Integer projectId = (Integer) projectItem.getId();
                    if (authorizationDao.isPublic(projectId)) {
                        String level = authorizationDao.getAuthorization(projectId);
                        if (level != null && level.length() > 0) {
                            if (tempDTO.getUserId().equals(UserItem.DEFAULT_USER)) {
                                String capitalized = level.substring(0, 1).toUpperCase()
                                        + level.substring(1, level.length()).toLowerCase();
                                tempDTO.setLevel(capitalized);
                            } else {
                                tempDTO.setLevel("Public");
                            }
                        }
                    } else {
                        tempDTO.setLevel("-");
                    }

                    // Set project info
                    tempDTO.setProjectId((Integer) projectItem.getId());
                    tempDTO.setProjectName(projectItem.getProjectName());

                    // Set access times
                    tempDTO.setFirstAccess((Date)(obj[map2FirstAccessId]));
                    tempDTO.setLastAccess((Date)(obj[map2LastAccessId]));

                    // Add the TOU information to the request
                    tempDTO = setTouInformation(tempDTO, projectItem, userItem);

                    projectRequests.add(tempDTO);
                }
            }
        }
        // Return the project request list
        return projectRequests;
    }

    /**
     * Updates the Access Request Status item's status based on the voting history.
     * @param baseUrl the base URL for DataShop
     * @param arStatus the Access Request Status item
     * @param arHistoryPi the PI's last Access Request History item
     * @param arHistoryDp the DP's last Access Request History item
     * @param arHistoryAdmin the admin's last Access Request History item
     * @param arHistoryRequestor the requestor's last Access Request History item
     * @param reason the optional reason
     * @param shareReasonFlag share reason flag
     * @param role the role of the user committing the action
     */
    private void updateStatusOnChange(String baseUrl,
            AccessRequestStatusItem arStatus,
            AccessRequestHistoryItem arHistoryPi,
            AccessRequestHistoryItem arHistoryDp,
            AccessRequestHistoryItem arHistoryAdmin,
            AccessRequestHistoryItem arHistoryRequestor,
            String reason, boolean shareReasonFlag, String role) {

        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        // The previously approved access level for the user and project, if one exists
        String previousLevel = null;
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        ProjectItem projectItem = projectDao.get((Integer) arStatus.getProject().getId());
        UserItem userItem = userDao.get((String) arStatus.getUser().getId());
        // Create the new status and, optionally, a new authorization level
        String newStatus = null;
        String newAuthLevel = null;
        String projectOwnership = projectItem.getOwnership();
        // Declare PI variables
        String piAction = null;
        String piLevel = null;
        boolean piApprovedEditOrView = false;    // PI approved edit or view
        boolean piApprove = false;
        boolean piDeny = false;
        // Declare DP variables
        String dpAction = null;
        String dpLevel = null;
        boolean dpApprovedEditOrView = false;    // DP approved edit or view
        boolean dpApprove = false;
        boolean dpDeny = false;

        // Trac 437.
        // Handling for changing an already-approved level is different.
        // Only an issue when pi != dp.
        boolean changingLevel = false;
        if (arStatus.getStatus().equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
            changingLevel = true;
        }

        AccessRequestHistoryItem arHistoryCurrent = arHistoryPi; // just so it isn't null

        // Get PI, DP, and administrator history
        if (arHistoryPi != null) {
            piAction = arHistoryPi.getAction();
            piLevel = arHistoryPi.getLevel();
            piApprovedEditOrView =  piLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                    || piLevel.equals(AuthorizationItem.LEVEL_EDIT)
                    || piLevel.equals(AuthorizationItem.LEVEL_VIEW);
            piApprove = piAction.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE);
            piDeny = piAction.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY);
        }
        if (arHistoryDp != null) {
            dpAction = arHistoryDp.getAction();
            dpLevel = arHistoryDp.getLevel();
            dpApprovedEditOrView = dpLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                    || dpLevel.equals(AuthorizationItem.LEVEL_EDIT)
                    || dpLevel.equals(AuthorizationItem.LEVEL_VIEW);
            dpApprove = dpAction.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE);
            dpDeny = dpAction.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("projectOwnership :: " + projectOwnership);
        }

        // Administrator over-rides any votes as long as the admin vote's isActive flag is true
        if (arHistoryAdmin != null) {
            arHistoryCurrent = arHistoryAdmin;

            newAuthLevel = arHistoryAdmin.getLevel();
            if (arHistoryAdmin.getAction()
                    .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED;
                newAuthLevel = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
            } else if (arHistoryAdmin.getAction()
                    .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE)) {
                newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                newAuthLevel = arHistoryAdmin.getLevel();
            }
        } else if (projectOwnership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)) {  // PI != DP
            // newAuthLevel is created when the authorization table should be updated,
            // newStatus is created when the AR status needs to be updated
            if (arHistoryPi != null && arHistoryDp != null) {
                //
                // Update the status based on the current state of the arStatus and arHistory
                //
                if (piLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                        && piApprove
                        && dpLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                        && dpApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_ADMIN;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;

                } else if (piLevel.equals(AuthorizationItem.LEVEL_EDIT)
                        && piApprove
                        && dpLevel.equals(AuthorizationItem.LEVEL_EDIT)
                        && dpApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_EDIT;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;

                } else if (piLevel.equals(AuthorizationItem.LEVEL_VIEW)
                        && piApprove
                        && dpLevel.equals(AuthorizationItem.LEVEL_VIEW)
                        && dpApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_VIEW;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;

                } else if (!piLevel.equals(dpLevel)
                        && piApprove
                        && dpApprove) {
                    if (changingLevel) {
                        if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                            newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED;
                        } else {
                            newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED;
                        }
                    } else {
                        // If the PI and DP votes disagree on level,
                        // then choose the lowest of the PI and DP levels being approved
                        newAuthLevel = AuthorizationItem.LEVEL_VIEW;
                        if (piLevel.equals(AuthorizationItem.LEVEL_VIEW)
                            || dpLevel.equals(AuthorizationItem.LEVEL_VIEW)) {
                            newAuthLevel = AuthorizationItem.LEVEL_VIEW;
                        } else if (piLevel.equals(AuthorizationItem.LEVEL_EDIT)
                                   || dpLevel.equals(AuthorizationItem.LEVEL_EDIT)) {
                            newAuthLevel = AuthorizationItem.LEVEL_EDIT;
                        } else if (piLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                                   || dpLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
                            if (!userItem.getId().equals(UserItem.DEFAULT_USER)) {
                                newAuthLevel = AuthorizationItem.LEVEL_ADMIN;
                            } else {
                                newAuthLevel = AuthorizationItem.LEVEL_EDIT;
                            }
                        }
                        newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                    }

                } else if (piDeny && dpDeny) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED;
                    newAuthLevel = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;

                } else if (piDeny || dpDeny) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED;
                    newAuthLevel = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;

                } else if (piApprove) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED;

                } else if (dpApprove) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED;
                }

            } else if (arHistoryPi != null) {
                if (piApprove) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED;
                } else if (piDeny) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED;
                    newAuthLevel = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
                }
            } else if (arHistoryDp != null) {
                if (dpApprove) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED;
                } else if (dpDeny) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED;
                    newAuthLevel = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
                }
            }
        } else if (projectOwnership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_EQ_DP)   // PI == DP
                   || projectOwnership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_ONLY) // OR pi_only
                   || projectOwnership.equals(ProjectItem.PROJECT_OWNERSHIP_NONE)) { // or PA as PI

            // If ownership is NONE but arHistoryPi is not null, then PA is acting as PI.
            if (arHistoryPi != null) {
                arHistoryCurrent = arHistoryPi;

                if (piLevel.equals(AuthorizationItem.LEVEL_ADMIN) && piApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_ADMIN;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                } else if (piLevel.equals(AuthorizationItem.LEVEL_EDIT) && piApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_EDIT;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                } else if (piApprovedEditOrView && piApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_VIEW;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                } else if (piDeny) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED;
                    newAuthLevel = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
                } else {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED;
                }
            } else {
                newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED;
            }
        } else if (projectOwnership.equals(ProjectItem.PROJECT_OWNERSHIP_DP_ONLY)) {    // dp_only

            if (arHistoryDp != null) {
                arHistoryCurrent = arHistoryDp;

                if (dpLevel.equals(AuthorizationItem.LEVEL_ADMIN) && dpApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_ADMIN;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                } else if (dpLevel.equals(AuthorizationItem.LEVEL_EDIT) && dpApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_EDIT;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                } else if (dpApprovedEditOrView && dpApprove) {
                    newAuthLevel = AuthorizationItem.LEVEL_VIEW;
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED;
                } else if (dpDeny) {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED;
                    newAuthLevel = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY;
                } else {
                    newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED;
                }
            } else {
                newStatus = AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED;
            }
        }

        if (newAuthLevel != null && userItem.getId().equals(UserItem.DEFAULT_USER)
            && newAuthLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
            logger.info("User '" + userItem.getId() + "' cannot be given '" + newAuthLevel
                    + "' access to Project '" + projectItem.getProjectName() + "'");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("newStatus :: " + newStatus + ", newAuthLevel :: " + newAuthLevel);
        }

        AuthorizationId authId = new AuthorizationId(
                (String)userItem.getId(), (Integer)projectItem.getId());
        AuthorizationItem authItem = authDao.get(authId);

        if (newStatus != null) {
            // Update AccessRequestStatus's status
            Date now = (Date) Calendar.getInstance().getTime();

            AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
            // If necessary, update authorization
            if (newAuthLevel != null) {

                if (projectItem != null && userItem != null) {
                    // Notification action for e-mail notification
                    String notificationAction = "";

                    // Test all 4 combinations of authItem and newAuthLevel
                    if (authItem == null && !newAuthLevel
                            .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                        // User is not in the authorization table and the action is not deny
                        previousLevel = NOTIFY_LEVEL_NO_ACCESS;
                        AuthorizationItem newAuthItem = new AuthorizationItem();
                        newAuthItem.setUserExternal(userItem);
                        newAuthItem.setProjectExternal(projectItem);
                        newAuthItem.setLevel(newAuthLevel);
                        authDao.saveOrUpdate(newAuthItem);
                        // Notification action (approved)
                        notificationAction = NOTIFY_ACTION_APPROVED;

                    } else if (authItem != null && !newAuthLevel
                            .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                        // User is in the authorization table and the action is not deny
                        previousLevel = authItem.getLevel();
                        logger.info("Updating authorization for User Id ("
                                + userItem.getId() + ") to " + newAuthLevel);
                        authItem.setLevel(newAuthLevel);
                        authDao.saveOrUpdate(authItem);
                        // Notification action (modified)
                        notificationAction = NOTIFY_ACTION_MODIFIED;
                    } else if (authItem != null && newAuthLevel
                            .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                        previousLevel = authItem.getLevel();
                        // Otherwise, remove prior entry from status
                        authDao.delete(authItem);
                        // Notification action (modified)
                        notificationAction = NOTIFY_ACTION_REVOKED;
                    } else if (authItem == null && newAuthLevel
                            .equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                        previousLevel = NOTIFY_LEVEL_NO_ACCESS;
                        notificationAction = NOTIFY_ACTION_DENIED;
                    }

                    logger.info("Updated authorization level to " + newAuthLevel
                            + " for Project (" + projectItem.getProjectName() + ")"
                            + " and User Id (" + userItem.getId() + ")");


                    // Notify the user of changes
                    TreeSet<UserItem> requestorHash = new TreeSet<UserItem>();
                    if (userItem != null) {
                        requestorHash.add(userItem);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("previousLevel :: " + previousLevel);
                    }

                    // Previous and new authorization levels are different, that's all that matters
                    if (!previousLevel.equals(newAuthLevel)
                            && projectOwnership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("SENDING email to " + userItem.getId());
                        }
                        notify(baseUrl, requestorHash, null, userItem, projectItem,
                                notificationAction, newAuthLevel, previousLevel,
                                arHistoryCurrent, arHistoryDp, role);
                    } else if (!previousLevel.equals(newAuthLevel)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("SENDING email to " + userItem.getId());
                        }
                        notify(baseUrl, requestorHash, null, userItem, projectItem,
                                notificationAction, newAuthLevel, previousLevel,
                                arHistoryCurrent, role);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("NOT sending email to " + userItem.getId());
                        }
                    }
                }
            }

            // Update the status
            arStatus.setStatus(newStatus);
            arStatus.setHasRequestorSeen(false);
            arStatus.setHasAdminSeen(false);
            arStatus.setHasPiSeen(false);
            arStatus.setHasDpSeen(false);

            arStatus.setLastActivityDate(now);
            arStatusDao.saveOrUpdate(arStatus);

            logger.info("Updated status entry for"
                    + " ProjectId (" + arStatus.getProject().getId() + ")"
                    + " and User Id (" + userItem.getId() + ")");
        } else if (newStatus == null && newAuthLevel != null) {
            // Notify the user of modification

            // Get the previous level from the authorization table
            previousLevel = authItem.getLevel() == null
                    ? NOTIFY_LEVEL_NO_ACCESS : authItem.getLevel();
            // By default, the notify method takes a tree-set of users
            TreeSet<UserItem> requestorHash = new TreeSet<UserItem>();
            // If the level has changed, then notify the user
            if (userItem != null && !previousLevel.equals(newAuthLevel)) {
                requestorHash.add(userItem);
                notify(baseUrl, requestorHash, null, userItem, projectItem,
                        NOTIFY_ACTION_MODIFIED, newAuthLevel, previousLevel,
                        arHistoryCurrent, role);
            }
        }
    }

    /**
     * Creates an export file and returns the file path or null if it cannot be created.
     * @param projectRequestList the project request list
     * @return an export file and returns the file path or null if it cannot be created
     */
    public File createExportFile(List<ProjectRequestDTO> projectRequestList) {
        try {
            boolean showTermsColumns = false;
            boolean showDpColumn = false;
            // DAO's
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
            AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();

            // Test if we should show the terms columns or DP reason column
            for (ProjectRequestDTO dto : projectRequestList) {
                if (!dto.getTouName().equals("")) {
                    showTermsColumns = true;
                }

                UserItem requestorItem = userDao.get(dto.getUserId());
                ProjectItem projectItem = projectDao.get(dto.getProjectId());
                // Get the status item
                AccessRequestStatusItem arStatus =
                    (AccessRequestStatusItem) arStatusDao
                        .findByUserAndProject(requestorItem, projectItem);
                // Get the DP's last reason
                AccessRequestHistoryItem arHistoryDp = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
                if (arHistoryDp != null) {
                    showDpColumn = true;
                }
            }

            // Create a temporary file
            File newFile = File.createTempFile("ar_export", null);
            // Write the data to the file
            FileWriter fstream = new FileWriter(newFile);
            BufferedWriter bw = new BufferedWriter(fstream);
            bw.write("Project\tUser\tEmail\tInstitution\tLevel\tFirst Access\tLast Access");

            // Only show column headers for ToU if they exist
            if (showTermsColumns) {
                bw.write("\tTerms\tVersion Accepted\tDate Accepted");
            }

            bw.write("\tUser Reason\tLast Activity Date");
            bw.write("\tLast Activity Action\tLast PI Reason");
            if (showDpColumn) {
                bw.write("\tLast DP Reason");
            }
            bw.write("\r\n");

            // Write the contents of the DTO list to file
            for (ProjectRequestDTO dto : projectRequestList) {
                // Project name
                if (!dto.getProjectName().equals("")) {
                    bw.write(dto.getProjectName());
                }
                // User name
                bw.write("\t");
                if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                    bw.write("Public");
                } else if (!dto.getUserFullName().equals("")) {
                    bw.write(dto.getUserFullName());
                }
                // Email
                bw.write("\t");
                if (!dto.getEmail().equals("")) {
                    bw.write(dto.getEmail());
                }
                // Institution
                bw.write("\t");
                if (!dto.getInstitution().equals("")) {
                    bw.write(dto.getInstitution());
                }
                // Access Level
                bw.write("\t");
                if (!dto.getLevel().equals("")) {
                    bw.write(dto.getLevel());
                }
                // First Access
                bw.write("\t");
                if (dto.getFirstAccess() != null) {
                    bw.write(AccessRequestServlet.quickDateFormat(dto.getFirstAccess()));
                }
                // Last Access
                bw.write("\t");
                if (dto.getLastAccess() != null) {
                    bw.write(AccessRequestServlet.quickDateFormat(dto.getLastAccess()));
                }

                // Terms info
                if (showTermsColumns) {
                    // ToU name
                    bw.write("\t");
                    if (!dto.getTouName().equals("")) {
                        bw.write(dto.getTouName().toString());
                    }
                    // Version agreed
                    bw.write("\t");
                    if (dto.getTouVersionAgreed() != 0) {
                        bw.write(String.valueOf(dto.getTouVersionAgreed()));
                    }
                    // ToU Date agreed
                    bw.write("\t");
                    if (dto.getTouDateAgreed() != null) {
                        bw.write(AccessRequestServlet.quickDateFormat(dto.getTouDateAgreed()));
                    }
                }

                // Determine if the Last DP Reason column is shown
                UserItem requestorItem = userDao.get(dto.getUserId());
                ProjectItem projectItem = projectDao.get(dto.getProjectId());
                // Get the status item
                AccessRequestStatusItem arStatus =
                    (AccessRequestStatusItem) arStatusDao
                        .findByUserAndProject(requestorItem, projectItem);
                // Get the last history items
                AccessRequestHistoryItem arHistoryRequestor = arHistoryDao
                        .findLastRequest(arStatus);
                AccessRequestHistoryItem arHistoryPi = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
                AccessRequestHistoryItem arHistoryDp = arHistoryDao
                    .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
                AccessRequestHistoryItem lastHistoryItem = null;

                // User Reason
                bw.write("\t");
                if (arHistoryRequestor != null && !arHistoryRequestor.getReason().equals("")) {
                    bw.write(arHistoryRequestor.getReason().replaceAll("[\r\n\t]+", " "));
                }
                // Show either last PI or last DP action and date (by latest)
                String lastActor = "PI";
                if (arHistoryPi != null) {
                    lastHistoryItem = arHistoryPi;
                    if (arHistoryDp != null) {
                        // Last action is from the DP
                        if (arHistoryPi.getDate().before(arHistoryDp.getDate())) {
                            lastHistoryItem = arHistoryDp;
                            lastActor = "DP";
                        }
                    }
                }

                // Last Activity Date
                bw.write("\t");
                if (lastHistoryItem != null && lastHistoryItem.getDate() != null) {
                    bw.write(AccessRequestServlet.quickDateFormat(lastHistoryItem.getDate()));
                }
                // Last Activity Action
                bw.write("\t");
                if (lastHistoryItem != null && !lastHistoryItem.getAction().equals("")) {
                    bw.write(lastHistoryItem.getAction() + " (" + lastActor + ")");
                }
                // PI Reason
                bw.write("\t");
                if (arHistoryPi != null && !arHistoryPi.getReason().equals("")) {
                    bw.write(arHistoryPi.getReason().replaceAll("[\r\n\t]+", " "));
                }
                // DP Reason
                if (showDpColumn) {
                    bw.write("\t");
                    if (arHistoryDp != null && !arHistoryDp.getReason().equals("")) {
                        bw.write(arHistoryDp.getReason().replaceAll("[\r\n\t]+", " "));
                    }
                }

                bw.write("\r\n");
            }

            bw.close();
            // Return the file to be exported
            return newFile;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Sends an e-mail notification to the Data Provider and Principal Investigator
     * of an arbitrary project, or sends a notification to Datashop help if the
     * e-mail addresses of both users are missing. E-mail address and
     * enabled/disabled flag (isSendMailActive) found in WEB-INF/web.xml.
     *
     * @param baseUrl the base URL for DataShop
     * @param recipients a list of recipients who shall receive e-mail notification
     * @param adminItem the administrator
     * @param requestorItem the requestor
     * @param project The project which was affected by the terms of use change
     * @param notificationAction the dynamic text (NOTIFY_ACTION* Strings in this class)
     * @param level the level associated with this notification
     * @param priorLevel the previous level (field used for action string in "updated" case)
     * @param historyItem history item for the responder
     * @param role the role of the user committing the action
     */
    private void notify(String baseUrl, TreeSet<UserItem> recipients,
            UserItem adminItem, UserItem requestorItem,
            ProjectItem project, String notificationAction,
            String level, String priorLevel,
            AccessRequestHistoryItem historyItem,
            String role) {
        notify(baseUrl, recipients,
                adminItem, requestorItem,
                project, notificationAction,
                level, priorLevel,
                historyItem, null, role);
    }
    private void notify(String baseUrl, TreeSet<UserItem> recipients,
            UserItem adminItem, UserItem requestorItem,
            ProjectItem project, String notificationAction,
            String level, String priorLevel,
            AccessRequestHistoryItem historyItem,
            AccessRequestHistoryItem otherHistoryItem,
            String role) {

        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        AccessRequestStatusItem statusItem = null;
        AccessRequestHistoryItem requestorHistoryItem = null;

        if (historyItem != null) {
            statusItem = historyItem.getAccessRequestStatusItem();
            if (statusItem != null) {
                requestorHistoryItem = arHistoryDao.findLastResponse(statusItem,
                    AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_REQUESTOR);
            }
        }

        boolean accessGrantedDirectly = false;
        if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN)) {
            accessGrantedDirectly = true;
            // This is the case where a DataShop Admin granted access. Notify PI/DP.
            notifyPiDp(project, requestorItem, level, baseUrl);
        }

        // Don't try to send mail to '%' (public user)
        if (requestorItem != null
                && !requestorItem.getId().equals(UserItem.DEFAULT_USER)) {
            String projectName = project.getProjectName();
            String projectAnchorHtml;
            if (project.getProjectName() == null) {
                projectName = "No Name";
                projectAnchorHtml = "";
            } else {
                projectAnchorHtml = getProjectLink(project, baseUrl);
            }
            String subject = "Access " + notificationAction
                    + " for project \"" + projectName + "\"";
            if (accessGrantedDirectly
                    && !notificationAction.equals(NOTIFY_ACTION_DENIED)
                    && !notificationAction.equals(NOTIFY_ACTION_REVOKED)) {
                subject = "Access granted for project \"" + projectName + "\"";
            }

            // First part of message to recipient
            StringBuffer bodyBuffer = new StringBuffer("");
            // Second part
            StringBuffer bodyBuffer2 = new StringBuffer("");
            bodyBuffer.append("<p>");
            bodyBuffer.append("We are notifying you that ");

            String actionString = "";
            if (notificationAction.equals(NOTIFY_ACTION_APPROVED)) {
                actionString =  NOTIFY_ACTION_APPROVED;
            } else if (notificationAction.equals(NOTIFY_ACTION_DENIED)) {
                actionString =  NOTIFY_ACTION_DENIED;
            } else if (notificationAction.equals(NOTIFY_ACTION_MODIFIED)) {
                actionString =  NOTIFY_ACTION_MODIFIED;
            } else if (notificationAction.equals(NOTIFY_ACTION_REQUEST)) {
                actionString =  "requested";
            } else if (notificationAction.equals(NOTIFY_ACTION_UPDATED)) {
                actionString =  "updated";
            } else if (notificationAction.equals(NOTIFY_ACTION_REVOKED)) {
                actionString =  NOTIFY_ACTION_REVOKED;
            }

            // Recipient is end user, not PI or DP
            if (notificationAction.equals(NOTIFY_ACTION_APPROVED)) {
                if (accessGrantedDirectly) {
                    if (notificationAction.equals(NOTIFY_ACTION_DENIED)) {
                        bodyBuffer.append("you have been denied ");
                    } else if (notificationAction.equals(NOTIFY_ACTION_REVOKED)) {
                        bodyBuffer.append("you have been denied ");
                    } else {
                        bodyBuffer.append("you have been granted ");
                    }
                } else {
                    bodyBuffer.append("your request for ");
                }
                bodyBuffer.append(level.toLowerCase());
                bodyBuffer.append(" access to the project \"");
                bodyBuffer.append(projectAnchorHtml);
                if (!accessGrantedDirectly) {
                    bodyBuffer.append("\" has been ");
                    bodyBuffer.append(actionString);
                } else {
                    bodyBuffer.append("\"");
                }
                bodyBuffer.append(". To access this project, log into ");
                bodyBuffer.append("<a href=\"");
                bodyBuffer.append(baseUrl);
                bodyBuffer.append("\">DataShop</a>. ");
                bodyBuffer.append("You will see this project listed on the \"My Datasets\" tab.");
                includeReason(bodyBuffer, historyItem, otherHistoryItem);
            } else if (notificationAction.equals(NOTIFY_ACTION_DENIED)) {
                bodyBuffer.append("your request for access to the project \"");
                bodyBuffer.append(projectAnchorHtml);
                bodyBuffer.append("\" has been ");
                bodyBuffer.append(actionString);
                bodyBuffer.append(
                        ". You are welcome to re-request access at any time by logging into ");
                bodyBuffer.append("<a href=\"");
                bodyBuffer.append(baseUrl);
                bodyBuffer.append("\">DataShop</a> ");
                bodyBuffer.append(
                    "and clicking the \"Re-request access\" button next to the project title. ");
                bodyBuffer.append(
                    "But please be aware that requesting access sends an email to the project ");
                bodyBuffer.append("principal investigator and/or data provider.");
                includeReason(bodyBuffer, historyItem, otherHistoryItem);
            } else if (notificationAction.equals(NOTIFY_ACTION_MODIFIED)) {
                // their access level changed, not including a revocation of access
                bodyBuffer.append("your access to the project \"");
                bodyBuffer.append(projectAnchorHtml);
                bodyBuffer.append("\" has been modified: you had ");
                bodyBuffer.append(priorLevel);
                bodyBuffer.append("");
                bodyBuffer.append(" access but now have ");
                bodyBuffer.append(level.toLowerCase());
                bodyBuffer.append(" access.");
                includeReason(bodyBuffer, historyItem, otherHistoryItem);
            } else if (notificationAction.equals(NOTIFY_ACTION_REVOKED)) {
                // their access was revoked
                bodyBuffer.append("your access to the project \"");
                bodyBuffer.append(projectAnchorHtml);
                bodyBuffer.append("\" has been ");
                bodyBuffer.append(actionString);
                bodyBuffer.append(
                        ". You are welcome to re-request access at any time by logging into ");
                bodyBuffer.append("<a href=\"");
                bodyBuffer.append(baseUrl);
                bodyBuffer.append("\">DataShop</a> ");
                bodyBuffer.append(
                    "and clicking the \"Re-request access\" button next to the project title. ");
                bodyBuffer.append(
                    "But please be aware that requesting access sends an email to the project ");
                bodyBuffer.append("principal investigator and/or data provider.");
                includeReason(bodyBuffer, historyItem, otherHistoryItem);
            } else if (notificationAction.equals(NOTIFY_ACTION_REQUEST)) {
                // Recipient(s) is DP/PI; a new request came in
                bodyBuffer.append("the user ");
                bodyBuffer.append(requestorItem.getName());
                bodyBuffer.append(" has requested ");
                bodyBuffer.append(level);
                bodyBuffer.append(" access to the project \"");
                bodyBuffer.append(projectAnchorHtml);
                bodyBuffer.append("\"");

                bodyBuffer2.append("To respond to this request, visit the ");
                bodyBuffer2.append("<a href=\"");
                bodyBuffer2.append(baseUrl);
                bodyBuffer2.append("/AccessRequests\">Access Requests</a> page in DataShop. ");
                bodyBuffer2.append("A row for responding to this request will be shown ");
                bodyBuffer2.append("under the heading \"Not Reviewed\". ");

                bodyBuffer2.append(requestorItem.getName());
                if (historyItem.getReason() == null || historyItem.getReason().isEmpty()) {
                    bodyBuffer2.append(" did not provide a reason for requesting access. ");
                } else {
                    bodyBuffer2.append(
                        " provided the following reason for requesting access:</p> ");
                    bodyBuffer2.append("<p style=\"font-style: oblique; white-space: pre-wrap\">");
                    bodyBuffer2.append(cleanText(historyItem.getReason()));
                    bodyBuffer2.append("</p><p>");
                }

                if (requestorItem.getEmail() != null && requestorItem.getEmail().length() > 0) {
                    bodyBuffer2.append("Feel free to email this user directly ");
                    bodyBuffer2.append("with any questions: ");
                    bodyBuffer2.append("<a href=\"mailto:");
                    bodyBuffer2.append(requestorItem.getEmail());
                    bodyBuffer2.append("\">");
                    bodyBuffer2.append(requestorItem.getEmail());
                    bodyBuffer2.append("</a>");
                }
                if (project.getDataProvider() != null && project.getPrimaryInvestigator() != null) {
                    bodyBuffer2.append("</p><p>");
                    bodyBuffer2.append("Note that since this project has both a PI and data "
                                     + "provider specified, both must approve access for the "
                                     + "request to be approved. If either of you denies access, "
                                     + "the request will be denied.");
                }

            } else if (notificationAction.equals(NOTIFY_ACTION_UPDATED)) {
                // Recipient(s) is DP/PI; a change was made by a PI, DP, or admin
                String name = "";
                String roleString = "";
                if (adminItem != null && adminItem.getName() != null) {
                    roleString = project.getRole(adminItem);
                    name = adminItem.getName();
                } else {
                    UserItem recipientUser = recipients.first();
                    UserItem modUser = recipientUser.equals(project.getPrimaryInvestigator())
                            ? project.getDataProvider() : project.getPrimaryInvestigator();
                    if (modUser != null && modUser.getName() != null) {
                        roleString = project.getRole(modUser).toUpperCase();
                        name = modUser.getName();
                    }
                }
                bodyBuffer.append(name + " (" + roleString + ")");
                bodyBuffer.append(" has ");
                // This field used to satisfy new requirements priorLevel is actually the action
                // i.e., approved or denied
                bodyBuffer.append(AccessRequestStatusItem.arStringEquivalent(
                        priorLevel, true).toLowerCase());
                bodyBuffer.append(" ");
                bodyBuffer.append(level.toLowerCase());
                bodyBuffer.append(" access to the project \"");
                bodyBuffer.append(projectAnchorHtml);
                bodyBuffer.append("\" for the user ");
                bodyBuffer.append(requestorItem.getName());
                bodyBuffer.append(". ");
                includeReason(bodyBuffer, historyItem, otherHistoryItem, true);
                if (priorLevel.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                    bodyBuffer.append("As only one denial is required to deny access, no further "
                                    + "action is required on your part. The original request will "
                                    + "remain on your Access Requests page, however, until you "
                                    + "respond to it. ");
                } else {
                    String stillStr = accessGrantedDirectly ? " " : " still ";
                    bodyBuffer.append("Your response is" + stillStr
                                      + "required, as both a PI and "
                                      + "data provider must agree to provide access for the "
                                      + "request to be granted. ");
                }

                String requestStr1 = accessGrantedDirectly ? "" : " to the original request";
                String requestStr2 = accessGrantedDirectly ? " " : " to this request ";
                bodyBuffer.append("To respond" + requestStr1 + ", visit the ");
                bodyBuffer.append("<a href=\"");
                bodyBuffer.append(baseUrl);
                bodyBuffer.append("/AccessRequests\">Access Requests</a> page in DataShop. ");
                bodyBuffer.append("A row for responding" + requestStr2 + "will be shown ");
                bodyBuffer.append("under the heading \"Not Reviewed\". ");
                if (requestorItem.getEmail() != null) {
                    bodyBuffer.append("Feel free to email this user directly with any questions: ");
                    bodyBuffer.append("<a href=\"mailto:");
                    bodyBuffer.append(requestorItem.getEmail());
                    bodyBuffer.append("\">");
                    bodyBuffer.append(requestorItem.getEmail());
                    bodyBuffer.append("</a>");
                }
            }

            // Send an e-mail to each unique recipient
            for (UserItem recipient : recipients) {
                // Don't e-mail null recipients though they can exist in a TreeSet
                if (recipient == null) {
                    continue;
                }
                StringBuffer messageBuffer = new StringBuffer("");
                String toAddress = "";
                String recipientRole = "";

                messageBuffer.append("<!DOCTYPE html><html lang=\"en\"><head><title>");
                messageBuffer.append("Your DataShop project's access levels have changed.");
                messageBuffer.append("</title></head><body>");

                if (recipient.getEmail() != null && recipient.getEmail().length() > 0) {
                    // Since the DP and PI are the same person, send 1 e-mail
                    toAddress = recipient.getEmail();
                } else {
                    // No e-mail found for DP or PI, send to Datashop help
                    toAddress = getEmailAddressDatashopHelp();
                    recipientRole = project.getRole(recipient);
                    messageBuffer.append("<p>This message cannot be sent to "
                            + recipient.getName() + " "
                            + "who is the " + recipientRole.toUpperCase() + " "
                            + "of the project because the email address is null or empty.</p>");
                }
                messageBuffer.append("<p>Dear ");
                messageBuffer.append(recipient.getName());
                messageBuffer.append(",</p>");
                messageBuffer.append(bodyBuffer);
                if (notificationAction.equals(NOTIFY_ACTION_REQUEST)) {
                    String roleString = project.getRole(recipient).equals(ProjectItem.ROLE_PI)
                            ? "PI" : "data provider";

                    messageBuffer.append(", for which you are the ");
                    messageBuffer.append(roleString);
                    messageBuffer.append(". ");
                    messageBuffer.append(bodyBuffer2);
                }

                //append closing
                messageBuffer.append("</p>");
                messageBuffer.append("<p>Thanks,<br />");
                messageBuffer.append("The DataShop Team</p>");
                messageBuffer.append("</body></html>");

                sendEmail(getEmailAddressDatashopHelp(), toAddress,
                          subject, messageBuffer.toString());
            }

            // No recipients in the list so send it to DataShop help
            if (recipients == null || recipients.isEmpty()) {
                StringBuffer messageBuffer = new StringBuffer("");
                messageBuffer.append("<!DOCTYPE html><html lang=\"en\"><head><title>");
                messageBuffer.append("A DataShop project's access levels have changed.");
                messageBuffer.append("</title></head><body>");
                messageBuffer.append("<p>No PI or data provider exist for the project.</p>");
                messageBuffer.append(bodyBuffer);
                messageBuffer.append(". ");
                if (notificationAction.equals(NOTIFY_ACTION_REQUEST)) {
                    messageBuffer.append(bodyBuffer2);
                }
                //append closing
                messageBuffer.append("</p>");
                messageBuffer.append("<p>Thanks,<br />");
                messageBuffer.append("The DataShop Team</p>");
                messageBuffer.append("</body></html>");

                sendEmail(getEmailAddressDatashopHelp(), getEmailAddressDatashopHelp(),
                          subject, messageBuffer.toString());
            }
        } else {
            logger.debug("Not sending email to default user.");
        }
    } // end of notify

    /**
     * Send email to PI/DP notifying them a new user has been granted access
     * to their project.
     * @param projectItem the project
     * @param userItem the user granted access
     * @param level the level of access granted
     */
    private void notifyPiDp(ProjectItem projectItem, UserItem userItem, String level, String baseUrl) {

        // If the user in question is the PI or DP, don't send this (extra) email.
        // They will be notified of the change in their access level.
        UserItem pi = projectItem.getPrimaryInvestigator();
        UserItem dp = projectItem.getDataProvider();
        if (((pi != null) && userItem.equals(pi)) || ((dp != null) && userItem.equals(dp))) {
            return;
        }

        String projectName = projectItem.getProjectName();
        String projectAnchorHtml;
        if (projectItem.getProjectName() == null) {
            projectName = "No Name";
            projectAnchorHtml = "";
        } else {
            projectAnchorHtml = getProjectLink(projectItem, baseUrl);
        }

        String subject;
        if (level.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
            subject = "Access denied for project \"" + projectName + "\"";
        } else {
            subject = "Access granted for project \"" + projectName + "\"";
        }
        StringBuffer messageBuffer = null;

        StringBuffer bodyBuffer = new StringBuffer("");

        // If user is '%' then the project has been made public.
        if (userItem.getId().equals(UserItem.DEFAULT_USER)) {
            bodyBuffer.append("We are notifying you that the project \"");
            bodyBuffer.append(projectAnchorHtml);
            bodyBuffer.append("\", for which you are the ROLE_STR, ");
            bodyBuffer.append(" has been made public.");
            bodyBuffer.append(" If you did not approve this change, please let us know.");

            // Also use modified email subject
            subject = "Your project has been made public";
        } else {
            bodyBuffer.append("We are notifying you that the user ");
            bodyBuffer.append(userItem.getName());
            if (level.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)) {
                bodyBuffer.append(" has been denied ");
                bodyBuffer.append(" access to the project \"");
            } else {
                bodyBuffer.append(" has been granted ");
                bodyBuffer.append(level.toLowerCase());
                bodyBuffer.append(" access to the project \"");
            }
            bodyBuffer.append(projectAnchorHtml);
            bodyBuffer.append("\", for which you are the ROLE_STR.");
        }

        List<UserItem> recipients = new ArrayList<UserItem>();
        recipients.add(pi);
        recipients.add(dp);

        for (UserItem recipient : recipients) {
            if (recipient == null) { continue; }

            messageBuffer = new StringBuffer("");
            messageBuffer.append("<p>Dear ");
            messageBuffer.append(recipient.getName());
            messageBuffer.append(",</p>");
            messageBuffer.append(bodyBuffer);

            String roleStr = "";
            if (projectItem.getRole(recipient).
                equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                roleStr = "PI";
            } else {
                roleStr = "data provider";
            }

            //append closing
            messageBuffer.append("</p>");
            messageBuffer.append("<p>Thanks,<br />");
            messageBuffer.append("The DataShop Team</p>");
            messageBuffer.append("</body></html>");

            String toAddress = getEmailAddressDatashopHelp();
            if (recipient.getEmail() != null && recipient.getEmail().length() > 0) {
                toAddress = recipient.getEmail();
            } else {
                messageBuffer.insert(0, "<p>This message cannot be sent to "
                                     + recipient.getName() + " "
                                     + "who is the " + roleStr + " "
                                     + "of the project because their email "
                                     + "address is null or empty.</p>");
            }

            String msg = messageBuffer.toString();
            msg = msg.replace("ROLE_STR", roleStr);

            sendEmail(getEmailAddressDatashopHelp(), toAddress, subject, msg);
        }
    }

    /**
     * Include the reason if the reason is not empty and the share reason box is checked.
     * There are two reasons if the project has both a PI and a DP.
     * Include a close and open paragraph elements before and after
     * @param bodyBuffer the string buffer to append to
     * @param historyItem the history item of the PI, DP or Admin
     * @param otherHistoryItem the history item of the DP if there is both
     */
    private void includeReason(StringBuffer bodyBuffer,
            AccessRequestHistoryItem historyItem,
            AccessRequestHistoryItem otherHistoryItem) {
        includeReason(bodyBuffer, historyItem, otherHistoryItem, false);
    }

    /**
     * Include the reason if the reason is not empty and the share reason box is checked.
     * There are two reasons if the project has both a PI and a DP.
     * Include a close and open paragraph elements before and after
     * @param bodyBuffer the string buffer to append to
     * @param historyItem the history item of the PI, DP or Admin
     * @param otherHistoryItem the history item of the DP if there is both
     * @param includeCloseOpenParagraphElements needed for one case
     */
    private void includeReason(StringBuffer bodyBuffer,
            AccessRequestHistoryItem historyItem,
            AccessRequestHistoryItem otherHistoryItem,
            boolean includeCloseOpenParagraphElements) {

        Boolean shareReasonFlag = false;
        String reason = null;
        if (historyItem != null) {
            shareReasonFlag = historyItem.getShareReasonFlag();
            if (shareReasonFlag == null) { shareReasonFlag = false; }
            reason = cleanText(historyItem.getReason());
        }

        Boolean otherShareReasonFlag = false;
        String otherReason = null;
        if (otherHistoryItem != null) {
            otherShareReasonFlag = otherHistoryItem.getShareReasonFlag();
            if (otherShareReasonFlag == null) { otherShareReasonFlag = false; }
            otherReason = cleanText(otherHistoryItem.getReason());
        }

        if ((shareReasonFlag || otherShareReasonFlag) && includeCloseOpenParagraphElements) {
            bodyBuffer.append("</p>");
        }

        if (shareReasonFlag && reason != null && reason.length() > 0) {
            bodyBuffer.append(getReasonHeader(historyItem));
            bodyBuffer.append("<p style=\"font-style: oblique; white-space: pre-wrap\">");
            bodyBuffer.append(reason);
            bodyBuffer.append("</p>");
        }
        if (otherShareReasonFlag && otherReason != null && otherReason.length() > 0) {
            bodyBuffer.append(getReasonHeader(otherHistoryItem));
            bodyBuffer.append("<p style=\"font-style: oblique; white-space: pre-wrap\">");
            bodyBuffer.append(otherReason);
            bodyBuffer.append("</p>");
        }

        if ((shareReasonFlag || otherShareReasonFlag) && includeCloseOpenParagraphElements) {
            bodyBuffer.append("<p>");
        }
    }

    /**
     * Get the heading line for the reason to include in the email message.
     * @param historyItem the history item to get the role and action from
     * @return a string buffer which includes the opening and closing paragraph elements
     */
    private StringBuffer getReasonHeader(AccessRequestHistoryItem historyItem) {
        String role = historyItem.getRole();
        String action = historyItem.getAction();
        StringBuffer header = new StringBuffer();
        header.append("<p>The ");
        if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN)) {
            header.append("DataShop Administrator");
        } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
            header.append("PI");
        } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP)) {
            header.append("Data Provider");
        }
        header.append(" provided the following reason for ");
        if (action.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_APPROVE)) {
            header.append("approving");
        } else {
            header.append("denying");
        }
        header.append(" access:</p>");
        return header;
    }

    /**
     * Returns a string with (&,<,>,") replaced by entity references.
     * @param input the input
     * @return a string with (&,<,>,") replaced by entity references
     */
    String cleanText(String input) {

        if (input != null) {

            String cleanText = input.replaceAll("&", "&amp;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("<", "&lt;");
            return cleanText;
        } else {
            return "";
        }
    }

    /**
     * Helper method for retrieving 'Access Report'.
     * @param userItem the logged in user.
     * @param arContext the AccessRequestContext object
     * @return List<ProjectRequestDTO> the contents of the 'Access Report'
     */
    public List<ProjectRequestDTO> getAccessReport(UserItem userItem, AccessRequestContext arContext) {

        String searchString = arContext.getSearchString();
        Boolean showAdmins = arContext.getShowAdmins();

        List<String> statusList = null;
        String piDpId = null;
        // If not a DS-Admin, narrow results by PI/DP.
        if (!userItem.getAdminFlag()) {
            piDpId = (String)userItem.getId();
        }
        List<Integer> projectIdList = null;

        // 'Access Requests' page supports searching by user and project name
        AccessReportInfo arInfo = new AccessReportInfo(statusList, piDpId,
                                                       projectIdList,
                                                       searchString,  // projectName
                                                       searchString,  // user
                                                       null,  // institution
                                                       showAdmins, "OR");

        String sortByColumn = arContext.getSortBy();
        Boolean isAscending = arContext.getSortOrder(sortByColumn);
        String arOrderBy = getOrderByClause(sortByColumn, isAscending);

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        int currentPage = arContext.getCurrentPage();
        int rowsPerPage = arContext.getRowsPerPage();
        List<ProjectRequestDTO> accessReport =
            arDao.getProjectRequests(arInfo, arOrderBy,
                                     calcPageOffset(currentPage, rowsPerPage), rowsPerPage);

        return postProcessResults(accessReport);
    }

    /**
     * Helper method for retrieving 'Current Permissions'.
     * @param userItem the logged in user.
     * @param arContext the AccessRequestContext object
     * @return List<ProjectRequestDTO> the contents of the 'Access Report'
     */
    public List<ProjectRequestDTO> getCurrentPermissions(UserItem userItem,
                                                         AccessRequestContext arContext) {

        String searchString = arContext.getSearchString();
        Boolean showAdmins = arContext.getShowAdmins();

        List<String> statusList = null;
        String piDpId = null;
        // If not a DS-Admin, narrow results by PI/DP.
        if (!userItem.getAdminFlag()) {
            piDpId = (String)userItem.getId();
        }
        List<Integer> projectIdList = null;

        // 'Access Requests' page supports searching by user and project name
        AccessReportInfo arInfo = new AccessReportInfo(statusList, piDpId,
                                                       projectIdList,
                                                       searchString,  // projectName
                                                       searchString,  // user
                                                       null,  // institution
                                                       showAdmins, "OR");

        String sortByColumn = arContext.getSortBy();
        Boolean isAscending = arContext.getSortOrder(sortByColumn);
        String arOrderBy = getOrderByClause(sortByColumn, isAscending);

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        int currentPage = arContext.getCurrentPage();
        int rowsPerPage = arContext.getRowsPerPage();
        List<ProjectRequestDTO> accessReport =
            arDao.getCurrentPermissions(arInfo, arOrderBy,
                                        calcPageOffset(currentPage, rowsPerPage), rowsPerPage);

        return postProcessResults(accessReport);
    }

    public Boolean getAccessReportHasTermsOfUse(UserItem userItem, AccessRequestContext arContext) {

        String searchString = arContext.getSearchString();
        String sortBy = arContext.getSortBy();
        Boolean showAdmins = arContext.getShowAdmins();

        List<String> statusList = null;
        String piDpId = null;
        // If not a DS-Admin, narrow results by PI/DP.
        if (!userItem.getAdminFlag()) {
            piDpId = (String)userItem.getId();
        }
        List<Integer> projectIdList = null;

        // 'Access Requests' page supports searching by user and project name
        AccessReportInfo arInfo = new AccessReportInfo(statusList, piDpId,
                                                       projectIdList,
                                                       searchString,  // projectName
                                                       searchString,  // user
                                                       null,  // institution
                                                       showAdmins, "OR");

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        return arDao.getHasTermsOfUse(arInfo);
    }

    /**
     * Helper method to determine 'ORDER BY' clause given sort order.
     *
     * @param sortByColumn the column by which to sort rows
     * @param isAscending flag indicating sort direction
     * @return String the 'ORDER BY' clause
     */
    public String getOrderByClause(String sortByColumn, Boolean isAscending) {

        String orderBy = null;

        if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_PROJECT)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_PROJECT_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_PROJECT_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_USER)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_USER_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_USER_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_INSTITUTION)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_INSTITUTION_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_INSTITUTION_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_LEVEL)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_LEVEL_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_LEVEL_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_FIRSTACCESS)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_FIRST_ACCESS_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_FIRST_ACCESS_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_LASTACCESS)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_LAST_ACCESS_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_LAST_ACCESS_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_TERMS)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_TOU_NAME_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_TOU_NAME_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_TERMSVERSION)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_TOU_VERSION_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_TOU_VERSION_DESC;
            }
        } else if (sortByColumn.equalsIgnoreCase(ProjectRequestDTO.COLUMN_TERMSDATE)) {
            if (isAscending) {
                orderBy = AccessReportInfo.SQL_ORDER_BY_TOU_DATE_ASC;
            } else {
                orderBy = AccessReportInfo.SQL_ORDER_BY_TOU_DATE_DESC;
            }
        }
        return orderBy;
    }

    /**
     * Helper method to calculate the page offset.
     * @param page the current page
     * @param entriesPerPage the number of items per page
     * @return int the offset
     */
    public int calcPageOffset(int page, int entriesPerPage) {
        return (page - 1) * entriesPerPage;
    }

    /**
     * Post-processing includes setting the Level and ToU fields.
     * @param projectAccessList the list of access requests
     * @return List<ProjectRequestDTO> access results
     */
    public List<ProjectRequestDTO> postProcessResults(List<ProjectRequestDTO> projectAccessList)
    {
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();

        for (ProjectRequestDTO dto: projectAccessList) {

            UserItem userItem = new UserItem();
            userItem.setId(dto.getUserId());
            ProjectItem projectItem = new ProjectItem();
            projectItem.setId(dto.getProjectId());

            if ((dto.getLevel() != null) && (!dto.getLevel().equals(""))) { continue; }

            if ((dto.getStatus() == null) || (dto.getStatus().equals(""))) {
                String tmpLevel = authDao.getAuthorization(dto.getUserId(),
                                                           dto.getProjectId());
                if (tmpLevel == null) {
                    // entry not in the authorization table.
                    dto.setLevel("-");
                } else {
                    dto.setLevel(tmpLevel);
                }
            } else {
                // Set request level to denied if partially denied
                if (dto.getStatus().equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                    || dto.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                    || dto.getStatus()
                    .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                    dto.setLevel(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED);
                } else {
                    AccessRequestStatusItem arStatus =
                            arStatusDao.findByUserAndProject(userItem, projectItem);
                    dto.setLevel(getCurrentLevelShown(arStatus));
                }
            }
        }
        return projectAccessList;
    }

    /**
     * Helper method for retrieving number of Access Report records.
     *
     * @param context the AccessRequestContext specifying query criteria
     * @param userItem the logged in user
     * @return Integer total num records in the 'Access Report'
     */
    public Integer getAccessReportCount(UserItem userItem, AccessRequestContext context) {

        Boolean showAdmin = context.getShowAdmins();
        String searchString = context.getSearchString();

        List<String> statusList = null;
        String piDpId = null;
        // If not a DS-Admin, narrow results by PI/DP.
        if (!userItem.getAdminFlag()) {
            piDpId = (String)userItem.getId();
        }
        List<Integer> projectIdList = null;

        // 'Access Requests' page supports searching by user and project name
        AccessReportInfo arInfo = new AccessReportInfo(statusList, piDpId,
                                                       projectIdList,
                                                       searchString,  // projectName
                                                       searchString,  // user
                                                       null,  // institution
                                                       showAdmin, "OR");

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        return arDao.getAccessReportCount(arInfo);
    }

    /**
     * Helper method for retrieving number of Current Permissions records.
     *
     * @param context the AccessRequestContext specifying query criteria
     * @param userItem the logged in user
     * @return Integer total num records in the 'Current Permissions'
     */
    public Integer getCurrentPermissionsCount(UserItem userItem, AccessRequestContext context) {

        Boolean showAdmin = context.getShowAdmins();
        String searchString = context.getSearchString();

        List<String> statusList = null;
        String piDpId = null;
        // If not a DS-Admin, narrow results by PI/DP.
        if (!userItem.getAdminFlag()) {
            piDpId = (String)userItem.getId();
        }
        List<Integer> projectIdList = null;

        // 'Access Requests' page supports searching by user and project name
        AccessReportInfo arInfo = new AccessReportInfo(statusList, piDpId,
                                                       projectIdList,
                                                       searchString,  // projectName
                                                       searchString,  // user
                                                       null,  // institution
                                                       showAdmin, "OR");

        AccessReportDao arDao = DaoFactory.HIBERNATE.getAccessReportDao();
        return arDao.getCurrentPermissionsCount(arInfo);
    }

    /**
     * Utility method to generate link given a ProjectItem.
     * @param project the ProjectItem
     * @param baseUrl the base DataShop URL
     * @return String specifying the link
     */
    private static String getProjectLink(ProjectItem project, String baseUrl) {
        if (project == null) { return ""; }

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(baseUrl);
        sb.append("/Project?id=");
        sb.append((Integer)project.getId());
        sb.append("\">");
        sb.append(project.getProjectName());
        sb.append("</a>");
        return sb.toString();
    }

}
