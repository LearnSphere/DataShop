/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;

import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the RemoteInstanceDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RemoteInstanceDaoHibernate extends AbstractDaoHibernate<RemoteInstanceItem>
    implements RemoteInstanceDao
{
    /**
     * Standard get for a RemoteInstanceItem by id.
     * @param id The id of the user.
     * @return the matching RemoteInstanceItem or null if none found
     */
    public RemoteInstanceItem get(Long id) { return get(RemoteInstanceItem.class, id); }

    /**
     * Standard find for an RemoteInstanceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired RemoteInstanceItem.
     * @return the matching RemoteInstanceItem.
     */
    public RemoteInstanceItem find(Long id) { return find(RemoteInstanceItem.class, id); }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<RemoteInstanceItem> findAll() { return findAll(RemoteInstanceItem.class); }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a list of RemoteInstance objects for a given name.
     * @param name the instance name
     * @return a List of objects
     */
    public List<RemoteInstanceItem> findByName(String name) {
        return getHibernateTemplate().find(
                        "FROM RemoteInstanceItem instance WHERE instance.name = ?", name);
    }
}
