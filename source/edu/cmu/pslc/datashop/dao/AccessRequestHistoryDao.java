/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.UserItem;


/**
 * AccessRequestHistory Data Access Object interface.
 *
 * @author Mike Komisin
 * @version $Revision: 10812 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-17 14:37:35 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AccessRequestHistoryDao extends AbstractDao {

    /**
     * Standard get for an AccessRequestHistoryItem by id.
     * @param id The id of the AccessRequestHistoryItem
     * @return the matching AccessRequestHistoryItem or null if none found
     */
    AccessRequestHistoryItem get(int id);

    /**
     * Standard find for an AccessRequestHistoryItem by id.
     * Only guarantees the id of the AccessRequestHistoryItemItem will be filled in.
     * @param id the id of the desired AccessRequestHistoryItem.
     * @return the matching AccessRequestHistoryItem.
     */
    AccessRequestHistoryItem find(int id);

    /**
     * Standard "find all" for AccessRequestHistoryItems.
     * @return a List of objects
     */
    List findAll();

    /* Begin non-standard HQL queries */

    /**
     * Returns a list of AccessRequestHistoryItems by user.
     * @param userItem the user item
     * @return a list of AccessRequestHistoryItems
     */
    Collection findByUser(UserItem userItem);

    /**
     * Returns a list of AccessRequestHistoryItems by Access Request Status.
     * @param arStatusItem the AccessRequestStatusItem object
     * @return a list of AccessRequestHistoryItems
     */
    Collection findByStatus(AccessRequestStatusItem arStatusItem);

    /**
     * Returns true if a request has not been made, today.
     * @param arStatusItem the AccessRequestStatusItem object
     * @return true if a request has not yet been made, today
     */
    Boolean isRequestValid(AccessRequestStatusItem arStatusItem);

    /**
     * Returns the last request associated with the Access Request Status.
     * @param arStatusItem the Access Request Status item
     * @return the last request
     */
    AccessRequestHistoryItem findLastRequest(AccessRequestStatusItem arStatusItem);

    /**
     * Returns the last response associated with the Access Request Status for the given role.
     * @param arStatusItem the Access Request Status item
     * @param role the role
     * @return the last response given the role
     */
    AccessRequestHistoryItem findLastResponse(AccessRequestStatusItem arStatusItem, String role);
}
