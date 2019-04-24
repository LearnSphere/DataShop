/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.OliLogDao;
import edu.cmu.pslc.datashop.item.OliLogItem;

/**
 * Hibernate and Spring implementation of the OliogDao.
 *
 * @author Hui Cheng
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliLogDaoHibernate extends AbstractDaoHibernate implements OliLogDao {

    /**
     * Standard get for a OliLogItem by id.
     * @param id The id of the OliLog.
     * @return the matching OliLogItem or null if none found
     */
    public OliLogItem get(String id) {
        return (OliLogItem)get(OliLogItem.class, id);
    }

    /**
     * Standard "find all" for OliLog items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(OliLogItem.class);
    }

    /**
     * Standard find for an OliLogItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired OliLogItem.
     * @return the matching OliLogItem.
     */
    public OliLogItem find(String id) {
        return (OliLogItem)find(OliLogItem.class, id);
    }
}
