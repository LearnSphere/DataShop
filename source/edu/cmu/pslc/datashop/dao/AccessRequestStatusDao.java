/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * AccessRequestStatus Data Access Object interface.
 *
 * @author Mike Komisin
 * @version $Revision: 9489 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-06-25 14:52:45 -0400 (Tue, 25 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AccessRequestStatusDao extends AbstractDao {

    /**
     * Standard get for an AccessRequestStatusItem by id.
     * @param id The id of the AccessRequestStatusItem
     * @return the matching AccessRequestStatusItem or null if none found
     */
    AccessRequestStatusItem get(int id);

    /**
     * Standard find for an AccessRequestStatusItem by id.
     * Only guarantees the id of the AccessRequestStatusItemItem will be filled in.
     * @param id the id of the desired AccessRequestStatusItem.
     * @return the matching AccessRequestStatusItem.
     */
    AccessRequestStatusItem find(int id);

    /**
     * Standard "find all" for AccessRequestStatusItems.
     * @return a List of objects
     */
    List findAll();

    /* Begin non-standard HQL queries */

    /**
     * Returns a list of AccessRequestStatusItems by user.
     * @param userItem the user item
     * @return a list of AccessRequestStatusItems
     */
    Collection findByUser(UserItem userItem);

    /**
     * Returns a single AccessRequestStatusItem by user and project or null if not found.
     * @param userItem the user item
     * @param projectItem the project
     * @return an AccessRequestStatusItem or null if not found.
     */
    AccessRequestStatusItem findByUserAndProject(UserItem userItem, ProjectItem projectItem);

    /**
     * Returns a list of pending AccessRequestStatusItems by owner or null if not found.
     * @param userItem the owner, e.g. the PI or DP
     * @return a list of pending AccessRequestStatusItems by owner or null if not found
     */
    Collection findNotReviewed(UserItem userItem);

    /**
     * Returns a list of pending AccessRequestStatusItems for all users or null if not found.
     * @param userItem the administrator
     * @param filterBy a parameter to filter by status for administrator, cannot be null
     * @return a list of pending AccessRequestStatusItems or null if not found
     */
    Collection findAdminNotReviewed(UserItem userItem, String filterBy);

    /**
     * Returns a list of pending AccessRequestStatusItems for all users by project.
     * @param projectItem the project
     * @return a list of pending AccessRequestStatusItems
     */
    Collection findAllNotReviewedByProject(ProjectItem projectItem);

    /**
     * Returns a list of recent AccessRequestStatusItems by owner or null if not found.
     * @param userItem the owner, e.g. the PI or DP
     * @return a list of recent AccessRequestStatusItems by owner or null if not found
     */
    Collection findRecentActivity(UserItem userItem);

    /**
     * Returns a list of recent AccessRequestStatusItems or null if not found.
     * @param userItem the administrator
     * @param filterBy an optional parameter to filter by status for administrator
     * @return a list of recent AccessRequestStatusItems or null if not found
     */
    Collection findAdminRecentActivity(UserItem userItem, String filterBy);

    /**
     * Returns a list of approved or denied AccessRequestStatusItems by owner or null if not found.
     * @param userItem the owner, e.g. the PI or DP
     * @return a list of approved or denied AccessRequestStatusItems by owner or null if not found
     */
    Collection findAccessReport(UserItem userItem);

    /**
     * Returns a list of approved or denied AccessRequestStatusItems or null if not found.
     * @param userItem the administrator
     * @return a list of approved or denied AccessRequestStatusItems or null if not found
     */
    Collection findAdminAccessReport(UserItem userItem);

    /**
     * Returns a count of activity notifications in the My Requests table for this user.
     * @param userItem the user item
     * @return a count of activity notifications in the My Requests table for this user
     */
    Long countNotificationsMyRequests(UserItem userItem);

    /**
     * Returns a count of activity notifications in Not Reviewed requests for this user.
     * @param ownerItem the user item
     * @return a count of activity notifications in Not Reviewed requests for this user
     */
    Long countNotificationsNotReviewed(UserItem ownerItem);

    /**
     * Returns a count of activity notifications in Recent Activity requests for this user.
     * @param ownerItem the user item
     * @return a count of activity notifications in Recent Activity requests for this user
     */
    Long countNotificationsRecent(UserItem ownerItem);

    /**
     * Returns a list of pending AccessRequestStatusItems for all users or null
     * if none exist.
     * @return a list of pending AccessRequestStatusItems or null if none exist
     */
    Collection findAllPending();

    /**
     * Returns a list of pending AccessRequestStatusItems for all users, with an
     * an active request date in the specified range. Return null if none exist.
     * @param fromDate beginning of date range
     * @param toDate end of date range
     * @return a list of pending AccessRequestStatusItems or null if none exist
     */
    Collection findAllPendingByDateRange(Date fromDate, Date toDate);

    /**
     * Returns a list of pending AccessRequestStatusItems for all users, with an
     * an active request date past the specified date. Return null if none exist.
     * @param theDate the day
     * @return a list of pending AccessRequestStatusItems or null if none exist
     */
    Collection findAllPendingOlderThan(Date theDate);
}
