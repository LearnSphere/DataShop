/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DataShopInstanceItem;

/**
 * DataShopInstance Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12503 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-08-11 15:26:37 -0400 (Tue, 11 Aug 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DataShopInstanceDao extends AbstractDao<DataShopInstanceItem> {

    /**
     * Standard get for a DataShopInstanceItem by id.
     * @param id The id of the DataShopInstanceItem.
     * @return the matching DataShopInstanceItem or null if none found
     */
    DataShopInstanceItem get(Long id);

    /**
     * Standard find for an DataShopInstanceItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DataShopInstanceItem.
     * @return the matching DataShopInstanceItem.
     */
    DataShopInstanceItem find(Long id);

    /**
     * Standard "find all" for UserItems.
     * @return a List of objects
     */
    List<DataShopInstanceItem> findAll();
}
