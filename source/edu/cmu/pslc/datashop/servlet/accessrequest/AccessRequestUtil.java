/* Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.accessrequest;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.UserItem;


/**
 * Utility methods for managing AccessRequests.
 * Moved out of AccessRequestHelper so that they can be used by
 * the AccessRequestsPending client.
 *
 * @author Mike Komisin
 * @version $Revision: 10816 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-03-18 09:58:25 -0400 (Tue, 18 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class AccessRequestUtil {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(AccessRequestUtil.class);

    /** Private constructor as this is a utility class. */
    private AccessRequestUtil() { }

    /**
     * Creates a new history entry for the Access Request History item.
     * @param arStatus the AccessRequestStatusItem
     * @param user the user
     * @param role the role
     * @param action the action
     * @param level the level
     * @param reason the reason
     * @param shareReasonFlag share reason flag
     * @return the AccessRequestHistoryItem
     */
    public static AccessRequestHistoryItem createHistoryEntry(AccessRequestStatusItem arStatus,
                                                              UserItem user, String role,
                                                              String action, String level,
                                                              String reason,
                                                              boolean shareReasonFlag) {

        if (user.getId().equals(UserItem.DEFAULT_USER)
            && level.equals(AuthorizationItem.LEVEL_ADMIN)) {
            logger.info("User '" + user.getId() + "' cannot be given '" + level
                    + "' access to a project.");
            return null;
        }
        // Get last status and history entries for Requestor, PI, DP, and Admin
        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        AccessRequestHistoryItem arHistoryPi = arHistoryDao
                .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI);
        AccessRequestHistoryItem arHistoryDp = arHistoryDao
                .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP);
        AccessRequestHistoryItem arHistoryAdmin = arHistoryDao
                .findLastResponse(arStatus, AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN);
        // Dates of the corresponding votes
        Date piDate = null, dpDate = null, adminDate = null;

        if (arHistoryPi != null && arHistoryPi.getIsActive()) {
            piDate = arHistoryPi.getDate();
        }
        if (arHistoryDp != null && arHistoryDp.getIsActive()) {
            dpDate = arHistoryDp.getDate();
        }
        if (arHistoryAdmin != null && arHistoryAdmin.getIsActive()) {
            adminDate = arHistoryAdmin.getDate();
        }

        // Both the PI and DP have an active vote
        if (piDate != null && dpDate != null) {
            // The admin also has an active vote so nullify admin vote
            if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN)) {
                arHistoryPi.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryPi);
                arHistoryDp.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryDp);
            } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                arHistoryPi.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryPi);
            } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP)) {
                arHistoryDp.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryDp);
            }
            // The PI has an active vote
        } else if (piDate != null) {
            if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN)) {
                arHistoryPi.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryPi);
            } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI)) {
                arHistoryPi.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryPi);
            }
            // The DP has an active vote
        } else if (dpDate != null) {
            if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN)) {
                arHistoryDp.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryDp);
            } else if (role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP)) {
                arHistoryDp.setIsActive(false);
                arHistoryDao.saveOrUpdate(arHistoryDp);
            }
        }
        // The admin has an active vote
        if (adminDate != null) {
            // Set isActive flag to false for the admin's history item
            arHistoryAdmin.setIsActive(false);
            arHistoryDao.saveOrUpdate(arHistoryAdmin);
            // If the role is PI or DP, then set status to not reviewed
            if (!role.equals(AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN)) {
                arStatus.setStatus(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED);
                arStatusDao.saveOrUpdate(arStatus);
            }
        }
        Date now = (Date) Calendar.getInstance().getTime();

        // If the request is valid or if it was denied, then load the item
        AccessRequestHistoryItem historyItem = null;

        historyItem = new AccessRequestHistoryItem();
        historyItem.setAccessRequestStatusItem(arStatus);
        historyItem.setUser(user);
        historyItem.setRole(role);
        historyItem.setAction(action);
        historyItem.setLevel(level);
        historyItem.setDate(now);
        historyItem.setIsActive(true);
        historyItem.setReason(reason);
        historyItem.setShareReasonFlag(shareReasonFlag);
        // Save
        arHistoryDao.saveOrUpdate(historyItem);

        logger.info("Created new history entry for ProjectId ("
                    + arStatus.getProject().getId() + ")"
                    + " and User Id (" + user.getId() + ")");

        return historyItem;
     }
}
