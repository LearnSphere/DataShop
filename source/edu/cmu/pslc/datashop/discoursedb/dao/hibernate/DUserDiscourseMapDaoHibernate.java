/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.dao.DUserDiscourseMapDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserDiscourseMapId;
import edu.cmu.pslc.datashop.discoursedb.item.DUserDiscourseMapItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the DUserDiscourseMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DUserDiscourseMapDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DUserDiscourseMapDao
{
    /**
     * Standard get for a DUserDiscourseMapItem by id.
     * @param id The id of the map
     * @return the matching DUserDiscourseMapItem or null if none found
     */
    public DUserDiscourseMapItem get(DUserDiscourseMapId id) {
        return (DUserDiscourseMapItem)get(DUserDiscourseMapItem.class, id);
    }

    /**
     * Standard find for an DUserDiscourseMapItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DUserDiscourseMapItem
     * @return the matching DUserDiscourseMapItem.
     */
    public DUserDiscourseMapItem find(DUserDiscourseMapId id) {
        return (DUserDiscourseMapItem)find(DUserDiscourseMapItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DUserDiscourseMapItem> findAll() {
        return findAll(DUserDiscourseMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of DUserDiscourseMapItem objects given a DUserItem.
     * @param user the DiscourseDB user item
     * @return a list of DUserDiscourseMapItem objects
     */
    public List<DUserDiscourseMapItem> findByUser(DUserItem user) {
        return getHibernateTemplate().find("FROM DUserDiscourseMapItem WHERE user = ?", user);
    }

    /** HQL query for batch of users. */
    private static final String USERS_BY_DISCOURSE_QUERY =
        "SELECT DISTINCT user FROM DUserDiscourseMapItem user WHERE user.discourse.id = ";

    /**
     * Return a list of DUserDiscourseMapItem objects given a DiscourseItem.
     * @param discourse the DiscourseDB discourse item
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return a list of DUserDiscourseMapItem objects
     */
    public List<DUserDiscourseMapItem> findByDiscourse(DiscourseItem discourse,
                                                       int offset, int max)
    {
        String query = USERS_BY_DISCOURSE_QUERY + discourse.getId();

        CallbackCreatorHelper helper = new CallbackCreatorHelper(query, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DUserDiscourseMapItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }
}
