/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.WfcRecentlyUsedDao;

import edu.cmu.pslc.datashop.servlet.HelperFactory;

import edu.cmu.pslc.datashop.workflows.WfcRecentlyUsedItem;

/**
 * WfcRecentlyUsed Data Access Object Implementation.
 *
 * @author Cindy Tipper
 * @version $Revision: 15130 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-05-15 14:11:06 -0400 (Tue, 15 May 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WfcRecentlyUsedDaoHibernate
    extends AbstractDaoHibernate<WfcRecentlyUsedItem> implements WfcRecentlyUsedDao {

    /**
     * Override of the standard saveOrUpdate to make sure that only the
     * 5 most recently used components exist for any specific user and component type.
     * @param newItem object to create or update
     */
    public void saveOrUpdate(WfcRecentlyUsedItem newItem) {

        String userId = newItem.getUserId();
        String componentType = newItem.getComponentType();
        String componentName = newItem.getComponentName();

        // If the (userId, componentName)-pair already exists, update the
        // 'lastUsed' timestamp. List size should never be > 1.
        List<WfcRecentlyUsedItem> wruList =
            findByComponentNameAndUserId(componentName, userId);
        if ((wruList != null) && (wruList.size() > 0)) {
            WfcRecentlyUsedItem wru = wruList.get(0);
            wru.setLastUsed(newItem.getLastUsed());
            super.saveOrUpdate(wru);
            return;
        }

        // If not, add the new row and, if necessary, delete the oldest row
        // for this user. Keep the per-user-component list size capped at 5.
        List<WfcRecentlyUsedItem> checkTypeList = findByComponentTypeAndUserId(componentType, userId);
        if (checkTypeList.size() >= 5) { deleteOldest(checkTypeList); }

        super.saveOrUpdate(newItem);
    }

    /**
     * Helper method to delete the last item in the list.
     * @param wruList list of rows
     */
    private void deleteOldest(List<WfcRecentlyUsedItem> wruList) {

        WfcRecentlyUsedItem oldest = wruList.get(wruList.size() - 1);

        logDebug("oldest WFC to delete: ", oldest);
        delete(oldest);
    }

    /**
     * Standard get for a WfcRecentlyUsedItem by id.
     * @param id The id of the WfcRecentlyUsedItem
     * @return the matching WfcRecentlyUsedItem or null if none found
     */
    public WfcRecentlyUsedItem get(Long id) {
        return (WfcRecentlyUsedItem)get(WfcRecentlyUsedItem.class, id);
    }

    /**
     * Standard find for a WfcRecentlyUsedItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WfcRecentlyUsedItem.
     * @return the matching WfcRecentlyUsedItem.
     */
    public WfcRecentlyUsedItem find(Long id) {
        return (WfcRecentlyUsedItem)find(WfcRecentlyUsedItem.class, id);
    }

    /**
     * Standard "find all" for WfcRecentlyUsedItems.
     * @return a List of WfcRecentlyUsedItems
     */
    public List findAll() {
        return findAll(WfcRecentlyUsedItem.class);
    }

    //
    // non-standard methods.
    //

    /**
     * Returns a list of WfcRecentlyUsedItems for a given user.
     * @param userId
     * @return list of WfcRecentlyUsedItem
     */
    public List<WfcRecentlyUsedItem> findByUserId(String userId) {
        Object[] params = {userId};
        return getHibernateTemplate().find("from WfcRecentlyUsedItem wru "
                                           + "where wru.userId = ?",
                                           params);
    }

    /**
     * Returns a list WfcRecentlyUsedItems for the component name and user.
     * @param componentName the component name
     * @param userId the user
     * @return the WfcRecentlyUsedItems based on the component name and user
     */
    public List<WfcRecentlyUsedItem> findByComponentNameAndUserId(String componentName,
                                                                  String userId) {
        Object[] params = {componentName, userId};
        return getHibernateTemplate().find("from WfcRecentlyUsedItem wru "
                                           + "where wru.componentName = ? "
                                           + "and wru.userId = ?",
                                           params);
    }


    /**
     * Returns a list WfcRecentlyUsedItems for the component type and use ordered by time descending.
     * @param componentType the component type
     * @param userId the user
     * @return the WfcRecentlyUsedItems based on the component type and user  ordered by time descending
     */
    public List<WfcRecentlyUsedItem> findByComponentTypeAndUserId(String componentType,
                                                                  String userId) {
        Object[] params = {componentType.toLowerCase(), userId};
        return getHibernateTemplate().find("from WfcRecentlyUsedItem wru"
                                           + " where lower(wru.componentType) = ?"
                                           + " and wru.userId = ?"
                                           + " order by wru.lastUsed desc"
                                           + " limit 5",
                                           params);
    }
}
