/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WfcRecentlyUsedItem;

/**
 * WfcRecentlyUsed Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 15130 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-05-15 14:11:06 -0400 (Tue, 15 May 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WfcRecentlyUsedDao extends AbstractDao<WfcRecentlyUsedItem> {

    /**
     * Override of the standard saveOrUpdate to make sure that no more than 5
     * rows exist for any specific user.
     * @param object to update
     */
    void saveOrUpdate(WfcRecentlyUsedItem object);

    /**
     * Standard get for a WfcRecentlyUsedItem by id.
     * @param id The id of the WfcRecentlyUsedItem.
     * @return the matching WfcRecentlyUsedItem or null if none found
     */
    WfcRecentlyUsedItem get(Long id);

    /**
     * Standard find for an WfcRecentlyUsedItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WfcRecentlyUsedItem.
     * @return the matching WfcRecentlyUsedItem.
     */
    WfcRecentlyUsedItem find(Long id);

    /**
     * Standard "find all" for WfcRecentlyUsedItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Returns a list of WfcRecentlyUsedItems for a given user.
     * @param userId
     * @return list of WfcRecentlyUsedItem
     */
    List<WfcRecentlyUsedItem> findByUserId(String userId);

    /**
     * Returns a list WfcRecentlyUsedItems for the component name and user.
     * @param componentName the component name
     * @param userId the user
     * @return the WfcRecentlyUsedItems based on the component name and user
     */
    List<WfcRecentlyUsedItem> findByComponentNameAndUserId(String componentName, String userId);

    /**
     * Returns a list WfcRecentlyUsedItems for the component type and user ordered by time descending.
     * @param componentType the component type
     * @param userId the user
     * @return the WfcRecentlyUsedItems based on the component type and user ordered by time descending
     */
    List<WfcRecentlyUsedItem> findByComponentTypeAndUserId(String componentType, String userId);

}
