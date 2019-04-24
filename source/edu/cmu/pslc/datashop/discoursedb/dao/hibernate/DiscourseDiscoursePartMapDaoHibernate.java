/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseDiscoursePartMapId;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseDiscoursePartMapItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the DiscourseDiscoursePartMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDiscoursePartMapDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DiscourseDiscoursePartMapDao
{
    /**
     * Standard get for a DiscourseDiscoursePartMapItem by id.
     * @param id The id of the map
     * @return the matching DiscourseDiscoursePartMapItem or null if none found
     */
    public DiscourseDiscoursePartMapItem get(DiscourseDiscoursePartMapId id) {
        return (DiscourseDiscoursePartMapItem)get(DiscourseDiscoursePartMapItem.class, id);
    }

    /**
     * Standard find for an DiscourseDiscoursePartMapItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DiscourseDiscoursePartMapItem
     * @return the matching DiscourseDiscoursePartMapItem.
     */
    public DiscourseDiscoursePartMapItem find(DiscourseDiscoursePartMapId id) {
        return (DiscourseDiscoursePartMapItem)find(DiscourseDiscoursePartMapItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DiscourseDiscoursePartMapItem> findAll() {
        return findAll(DiscourseDiscoursePartMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for batch of users. */
    private static final String DISCOURSE_BY_DISCOURSE_PART_QUERY =
        "SELECT DISTINCT map FROM DiscourseDiscoursePartMapItem map WHERE map.discourse.id = ";

    /**
     * Return a list of DiscourseDiscoursePartMapItem objects given a DiscourseItem
     * @param discourse the DiscourseDB discourse item
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return a list of DiscourseDiscoursePartMapItem objects
     */
    public List<DiscourseDiscoursePartMapItem> findByDiscourse(DiscourseItem discourse,
                                                               int offset, int max)
    {
        String query = DISCOURSE_BY_DISCOURSE_PART_QUERY + discourse.getId();

        CallbackCreatorHelper helper = new CallbackCreatorHelper(query, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DiscourseDiscoursePartMapItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of DiscourseDiscoursePartMapItem objects given a DiscoursePartItem
     * @param part the DiscourseDB discourse part item
     * @return a list of DiscourseDiscoursePartMapItem objects
     */
    public List<DiscourseDiscoursePartMapItem> findByDiscoursePart(DiscoursePartItem part) {
        return getHibernateTemplate().
            find("FROM DiscourseDiscoursePartMapItem WHERE part = ?", part);
    }
}
