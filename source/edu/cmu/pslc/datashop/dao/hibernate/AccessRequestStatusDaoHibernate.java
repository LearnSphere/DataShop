package edu.cmu.pslc.datashop.dao.hibernate;
/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestContext;

/**
 * AccessRequestStatus Data Access Object interface.
 *
 * @author Mike Komisin
 * @version $Revision: 9489 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-06-25 14:52:45 -0400 (Tue, 25 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */

public class AccessRequestStatusDaoHibernate
    extends AbstractDaoHibernate implements AccessRequestStatusDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for an AccessRequestStatusItem by id.
     * @param id The id of the AccessRequestStatusItem
     * @return the matching AccessRequestStatusItem or null if none found
     */
    public AccessRequestStatusItem get(int id) {
        return (AccessRequestStatusItem)get(AccessRequestStatusItem.class, id);
    }

    /**
     * Standard find for an AccessRequestStatusItem by id.
     * Only guarantees the id of the AccessRequestStatusItemItem will be filled in.
     * @param id the id of the desired AccessRequestStatusItem.
     * @return the matching AccessRequestStatusItem.
     */
    public AccessRequestStatusItem find(int id) {
        return (AccessRequestStatusItem)find(AccessRequestStatusItem.class, id);
    }

    /**
     * Standard "find all" for AccessRequestStatusItems.
     * @return a List of objects
     */
    public List<AccessRequestStatusItem> findAll() {
        return findAll(AccessRequestStatusItem.class);
    }

    /* Begin non-standard HQL queries */

    /**
     * Returns a list of AccessRequestStatusItems by user or null if not found.
     * @param userItem the user item
     * @return a list of AccessRequestStatusItems or null if not found
     */
    public List<AccessRequestStatusItem> findByUser(UserItem userItem) {
        Object[] params = {userItem};
        String query = "from AccessRequestStatusItem status where status.user = ?";
        return getHibernateTemplate().find(query, params);
    }

    /**
     * Returns a single AccessRequestStatusItem by user and project or null if not found.
     * @param userItem the user item
     * @param projectItem the project
     * @return an AccessRequestStatusItem or null if not found.
     */
    public AccessRequestStatusItem findByUserAndProject(
            UserItem userItem, ProjectItem projectItem) {

        Object[] params = {userItem, projectItem};
        String query = "from AccessRequestStatusItem status where status.user = ?"
                + " and status.project = ?";
        List<AccessRequestStatusItem> result = (List<AccessRequestStatusItem>)
                getHibernateTemplate().find(query, params);

        if (result == null || result.isEmpty()) {
            return null;
        }

        return (AccessRequestStatusItem) result.get(0);
    }

    /**
    * Returns a list of pending AccessRequestStatusItems by owner or null if not found.
    * @param userItem the owner, e.g. the PI or DP
    * @return a list of pending AccessRequestStatusItems by owner or null if not found
    */
   public List<AccessRequestStatusItem> findNotReviewed(UserItem userItem) {
       Object[] params = {AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED,
               userItem,

               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED,
               userItem };

       String query = "select status from AccessRequestStatusItem status"
           + " left join status.project project"
           + " where ((status.status = ? or status.status = ? or status.status = ?)"
                   + " and project.primaryInvestigator = ?)"
           + " or ((status.status = ? or status.status = ? or status.status = ?)"
                   + " and project.dataProvider = ?)"
           + " order by status.lastActivityDate DESC";

       return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
   }

   /**
    * Returns a list of pending AccessRequestStatusItems for all users or null if not administrator.
    * @param userItem the administrator
    * @param filterBy parameter to filter by status for administrator, cannot be null
    * @return a list of pending AccessRequestStatusItems or null if not administrator
    */
   public List<AccessRequestStatusItem> findAdminNotReviewed(UserItem userItem, String filterBy) {
           if (!userItem.getAdminFlag()) {
               return null;
           }
           // Filter returns all
           if (filterBy.equals(AccessRequestContext.DEFAULT_FILTER_BY)) {
               Object[] params = {AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
                       AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                       AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED };
               String query = "select status from AccessRequestStatusItem status"
                       + " where status.status = ? or status.status = ? or status.status = ?";
               return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
           } else {
               String query = "select status from AccessRequestStatusItem status"
                       + " where status.status = ?";
               return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, filterBy);
           }
   }

    /**
     * Returns a list of pending AccessRequestStatusItems for all users by project.
     * @param projectItem the project
     * @return a list of pending AccessRequestStatusItems
     */
    public List<AccessRequestStatusItem> findAllNotReviewedByProject(ProjectItem projectItem) {
        Object[] params = {projectItem,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED };

        String query = "from AccessRequestStatusItem ars where ars.project = ? and"
            + " (ars.status = ? or ars.status = ? or ars.status = ?)";

        return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
   }

   /**
    * Returns a list of recent AccessRequestStatusItems by owner or null if not found.
    * @param userItem the owner, e.g. the PI or DP
    * @return a list of recent AccessRequestStatusItems by owner or null if not found
    */
   public List<AccessRequestStatusItem> findRecentActivity(UserItem userItem) {
       Object[] params = {userItem, userItem};
       String query = "select status from AccessRequestStatusItem status"
               + " left join status.project project"
               + " where project.primaryInvestigator = ? "
               + " or project.dataProvider = ?"
               + " order by status.lastActivityDate DESC";
       return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
   }

   /**
    * Returns a list of recent AccessRequestStatusItems or null if not administrator.
    * @param userItem the administrator
    * @param filterBy an optional parameter to filter by status for administrator
    * @return a list of recent AccessRequestStatusItems or null if not administrator
    */
   public List<AccessRequestStatusItem> findAdminRecentActivity(UserItem userItem,
           String filterBy) {
           if (!userItem.getAdminFlag()) {
               return null;
           }
           // Filter returns all
           if (filterBy.equals(AccessRequestContext.DEFAULT_FILTER_BY)) {
               String query = "select status from AccessRequestStatusItem status";
               return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query);
           } else {
           // Filter returns pi approved, dp approved, or not reviewed
           String query = "select status from AccessRequestStatusItem status"
                   + " where status.status = ?";
               return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, filterBy);
           }
   }

   /**
    * Returns a list of approved or denied AccessRequestStatusItems by owner or null if not found.
    * @param userItem the owner, e.g. the PI or DP
    * @return a list of approved or denied AccessRequestStatusItems by owner or null if not found
    */
   public List<AccessRequestStatusItem> findAccessReport(UserItem userItem) {
       Object[] params = {userItem, userItem,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED};
       String query = "select status from AccessRequestStatusItem status"
           + " left join status.project project"
           + " where (project.primaryInvestigator = ? or project.dataProvider = ?)"
           + " and (status.status = ? or status.status = ?"
           + " or status.status = ? or status.status = ?)";
       return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
   }

   /**
    * Returns a list of approved or denied AccessRequestStatusItems or null if not administrator.
    * @param userItem the administrator
    * @return a list of approved or denied AccessRequestStatusItems or null if not administrator
    */
   public List<AccessRequestStatusItem> findAdminAccessReport(UserItem userItem) {
       if (!userItem.getAdminFlag()) {
           return null;
       }
       Object[] params = {AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED};
       String query = "select status from AccessRequestStatusItem status"
           + " where (status.status = ? or status.status = ?"
           + " or status.status = ? or status.status = ?)";
       return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
   }

   /**
    * Returns a count of activity notifications in the My Requests table for this user.
    * @param userItem the user item
    * @return a count of activity notifications in the My Requests table for this user
    */
   public Long countNotificationsMyRequests(UserItem userItem) {

       Object[] params = {userItem};
       String query = "select count(*) from AccessRequestStatusItem status where status.user = ?"
               + " and status.hasRequestorSeen = false";

       Long count = (Long)getHibernateTemplate().find(query, params).get(0);

       return count;
   }

   /**
    * Returns a count of activity notifications in Not Reviewed requests for this user.
    * @param ownerItem the user item
    * @return a count of activity notifications in Not Reviewed requests for this user
    */
   public Long countNotificationsNotReviewed(UserItem ownerItem) {
       Long count = new Long(0);

       if (ownerItem.getAdminFlag()) {
        // Administrator count
           Object[] params = {AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED};

           String query = "select count(status) from AccessRequestStatusItem status"
               + " left join status.project project"
               + " where status.status = ? or status.status = ?"
               + " or status.status = ? or status.status = ?";
           count = (Long)getHibernateTemplate().find(query, params).get(0);
       } else {
           // Owner count
           Object[] params = {AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED,
                   ownerItem,

                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED,
                   AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED,
                   ownerItem};

           String query = "select count(status) from AccessRequestStatusItem status"
           + " left join status.project project"
           + " where ((status.status = ? or status.status = ? or status.status = ?)"
                   + " and project.primaryInvestigator = ?)"
           + " or ((status.status = ? or status.status = ? or status.status = ?)"
                   + " and project.dataProvider = ?)";
           count = (Long)getHibernateTemplate().find(query, params).get(0);
       }

       return count;
   }

   /**
    * Returns a count of activity notifications in Recent Activity requests for this user.
    * @param ownerItem the user item
    * @return a count of activity notifications in Recent Activity requests for this user
    */
   public Long countNotificationsRecent(UserItem ownerItem) {
       Long count = new Long(0);
       if (ownerItem.getAdminFlag()) {

           String query = "select count(status) from AccessRequestStatusItem status"
               + " left join status.project project"
               + " where status.hasAdminSeen = false";
           count = (Long)getHibernateTemplate().find(query).get(0);

       } else {
           Object[] params = {ownerItem, ownerItem};
           String query = "select count(status) from AccessRequestStatusItem status"
               + " left join status.project project"
               + " where (project.primaryInvestigator = ? and status.hasPiSeen = false)"
               + " or (project.dataProvider = ? and status.hasDpSeen = false)";
           count = (Long)getHibernateTemplate().find(query, params).get(0);

       }

       return count;
   }

   /**
    * Returns a list of pending AccessRequestStatusItems for all users or null
    * if none exist.
    * @return a list of pending AccessRequestStatusItems or null if none exist
    */
   public List<AccessRequestStatusItem> findAllPending() {
       Object[] params = {AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
               AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED };
       String query = "select status from AccessRequestStatusItem status"
               + " where status.status = ? or status.status = ? or status.status = ?";
       return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);

   }

    /**
     * Returns a list of pending AccessRequestStatusItems for all users, with an
     * active request date in the specified range. Return null if none exist.
     * @param fromDate beginning of date range
     * @param toDate end of date range
     * @return a list of pending AccessRequestStatusItems or null if none exist
     */
    public List<AccessRequestStatusItem> findAllPendingByDateRange(Date fromDate, Date toDate) {
        Object[] params = {AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_REQUEST, true,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED,
                           fromDate, toDate};
        String query = "select distinct status from AccessRequestStatusItem status"
            + " join status.accessRequestHistory history"
            + " where (history.action = ?) and (history.isActive = ?)"
            + " and (status.status = ? or status.status = ? or status.status = ?)"
            + " and (history.date < ? and history.date > ?)";
        return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
    }

    /**
     * Returns a list of pending AccessRequestStatusItems for all users, with an
     * an active request date past the specified date. Return null if none exist.
     * @param theDate the day
     * @return a list of pending AccessRequestStatusItems or null if none exist
     */
    public List<AccessRequestStatusItem> findAllPendingOlderThan(Date theDate) {
        Object[] params = {AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_REQUEST, true,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                           AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED,
                           theDate};
        String query = "select distinct status from AccessRequestStatusItem status"
            + " join status.accessRequestHistory history"
            + " where (history.action = ?) and (history.isActive = ?)"
            + " and (status.status = ? or status.status = ? or status.status = ?)"
            + " and (history.date < ?)";
        return (List<AccessRequestStatusItem>)getHibernateTemplate().find(query, params);
    }
}
