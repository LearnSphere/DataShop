/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.DataShopInstanceDao;

import edu.cmu.pslc.datashop.item.DataShopInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the DataShopInstanceDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12503 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-08-11 15:26:37 -0400 (Tue, 11 Aug 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DataShopInstanceDaoHibernate extends AbstractDaoHibernate<DataShopInstanceItem>
    implements DataShopInstanceDao
{
    /**
     * Standard get for a DataShopInstanceItem by id.
     * @param id The id of the user.
     * @return the matching DataShopInstanceItem or null if none found
     */
    public DataShopInstanceItem get(Long id) { return get(DataShopInstanceItem.class, id); }

    /**
     * Standard find for an DataShopInstanceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DataShopInstanceItem.
     * @return the matching DataShopInstanceItem.
     */
    public DataShopInstanceItem find(Long id) { return find(DataShopInstanceItem.class, id); }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DataShopInstanceItem> findAll() { return findAll(DataShopInstanceItem.class); }
}
