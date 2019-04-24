package edu.cmu.pslc.datashop.dao.hibernate;
/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * AccessRequestHistory Data Access Object interface.
 *
 * @author Mike Komisin
 * @version $Revision: 8425 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2012-12-14 13:38:35 -0500 (Fri, 14 Dec 2012) $
 * <!-- $KeyWordsOff: $ -->
 */

public class AccessRequestHistoryDaoHibernate
    extends AbstractDaoHibernate implements AccessRequestHistoryDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** The maximum number of allowed requests for a user-project pair. */
    public static final Integer MAX_REQUESTS = 4;
    /**
     * Standard get for an AccessRequestHistoryItem by id.
     * @param id The id of the AccessRequestHistoryItem
     * @return the matching AccessRequestHistoryItem or null if none found
     */
    public AccessRequestHistoryItem get(int id) {
        return (AccessRequestHistoryItem)get(AccessRequestHistoryItem.class, id);
    }

    /**
     * Standard find for an AccessRequestHistoryItem by id.
     * Only guarantees the id of the AccessRequestHistoryItemItem will be filled in.
     * @param id the id of the desired AccessRequestHistoryItem.
     * @return the matching AccessRequestHistoryItem.
     */
    public AccessRequestHistoryItem find(int id) {
        return (AccessRequestHistoryItem)find(AccessRequestHistoryItem.class, id);
    }

    /**
     * Standard "find all" for AccessRequestHistoryItems.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(AccessRequestHistoryItem.class);
    }

    /* Begin non-standard HQL queries */

    /**
     * Returns a list of AccessRequestHistoryItems by user.
     * @param userItem the userItem
     * @return a list of AccessRequestHistoryItems
     */
    public List<AccessRequestHistoryItem> findByUser(UserItem userItem) {
        Object[] params = {userItem};
        String query = "from AccessRequestHistoryItem history where history.user = ?"
                + " order by history.date DESC";
        return getHibernateTemplate().find(query, params);
    }

    /**
     * Returns a list of AccessRequestHistoryItems by Access Request Status.
     * @param arStatusItem the AccessRequestStatusItem object
     * @return a list of AccessRequestHistoryItems
     */
    public List<AccessRequestHistoryItem> findByStatus(AccessRequestStatusItem arStatusItem) {
        Object[] params = {arStatusItem};
        String query = "from AccessRequestHistoryItem history"
                + " where history.accessRequestStatusItem = ?"
                + " order by history.date DESC";
        return getHibernateTemplate().find(query, params);
    }

    /**
     * Returns true if a request has not been made, today.
     * @param arStatusItem the AccessRequestStatusItem object
     * @return true if a request has not yet been made, today
     */
    public Boolean isRequestValid(AccessRequestStatusItem arStatusItem) {
        // Allow MAX_REQUESTS requests per user-project pair
        // but only count requests that occur after last response
        Object[] maxDateParams = {arStatusItem,
                AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_REQUESTOR};
        String dateQuery = "select max(history.date) from AccessRequestHistoryItem history"
                + " where history.accessRequestStatusItem = ?"
                + " and history.role <> ?";
        // The date of the last approval or denial
        Date lastDate = (Date) getHibernateTemplate()
                .find(dateQuery, maxDateParams).get(0);

        Object[] countParams = {arStatusItem,
                AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_REQUESTOR,
                lastDate };
        String countQuery = "select count(history.id) from AccessRequestHistoryItem history"
                + " where history.accessRequestStatusItem = ?"
                + " and history.role = ?"
                + " and history.date > ifnull(?, 0)";

        Long count = (Long)getHibernateTemplate()
                .find(countQuery, countParams).get(0);
        if (count <= MAX_REQUESTS) {
            Date now = (Date) Calendar.getInstance().getTime();
            String action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_REQUEST;
            Object[] params = {arStatusItem, action, now};
            String query = "from AccessRequestHistoryItem history"
                    + " where history.accessRequestStatusItem = ?"
                    + " and history.action = ?"
                    + " and Date(history.date) = ?";
            List<AccessRequestHistoryItem> arHistory =
                    getHibernateTemplate().find(query, params);
            if (arHistory == null || arHistory.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the last request associated with the Access Request Status or null if not found.
     * @param arStatusItem the Access Request Status
     * @return the last request or null if not found
     */
    public AccessRequestHistoryItem findLastRequest(AccessRequestStatusItem arStatusItem) {
        String action = AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_REQUEST;
        Object[] params = {arStatusItem, action};
        String query = "from AccessRequestHistoryItem history"
                + " where history.accessRequestStatusItem = ?"
                + " and history.action = ?"
                + " and history.isActive = true"
                + " order by history.date DESC";
        List<AccessRequestHistoryItem> arHistory = getHibernateTemplate().find(query, params);
        if (arHistory == null || arHistory.isEmpty()) {
            return null;
        } else {
            return arHistory.get(0);
        }
    }

    /**
     * Returns the last response associated with the Access Request Status
     * for the given role or null if not found.
     * @param arStatusItem the Access Request Status
     * @param role the role
     * @return the last response given the role or null if not found
     */
    public AccessRequestHistoryItem findLastResponse(AccessRequestStatusItem arStatusItem,
            String role) {
        Object[] params = {arStatusItem, role};
        String query = "from AccessRequestHistoryItem history"
                + " where history.accessRequestStatusItem = ?"
                + " and history.role = ?"
                + " and history.isActive = true"
                + " order by history.date DESC";
        List<AccessRequestHistoryItem> arHistory = getHibernateTemplate().find(query, params);
        if (arHistory == null || arHistory.isEmpty()) {
            return null;
        } else {
            return arHistory.get(0);
        }
    }
}
