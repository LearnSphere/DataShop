/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.RemoteDiscourseInfoDao;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.item.RemoteDiscourseInfoItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the RemoteDiscourseInfoDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RemoteDiscourseInfoDaoHibernate extends AbstractDaoHibernate<RemoteDiscourseInfoItem>
    implements RemoteDiscourseInfoDao
{
    /**
     * Standard get for a RemoteDiscourseInfoItem by id.
     * @param id The id of the user.
     * @return the matching RemoteDiscourseInfoItem or null if none found
     */
    public RemoteDiscourseInfoItem get(Long id) { return get(RemoteDiscourseInfoItem.class, id); }

    /**
     * Standard find for an RemoteDiscourseInfoItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired RemoteDiscourseInfoItem.
     * @return the matching RemoteDiscourseInfoItem.
     */
    public RemoteDiscourseInfoItem find(Long id) { return find(RemoteDiscourseInfoItem.class, id); }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<RemoteDiscourseInfoItem> findAll() {
        return findAll(RemoteDiscourseInfoItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a list of RemoteDiscourseInfo objects for a given discourse.
     * @param discourse the DiscourseItem
     * @return a List of objects
     */
    public List<RemoteDiscourseInfoItem> findByDiscourse(DiscourseItem discourse) {
        return getHibernateTemplate().find(
                        "FROM RemoteDiscourseInfoItem rdi WHERE rdi.discourse = ?", discourse);
    }
}
