/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DiscourseInstanceMapDao;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.item.DiscourseInstanceMapId;
import edu.cmu.pslc.datashop.item.DiscourseInstanceMapItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * Hibernate and Spring implementation of the DiscourseInstanceMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseInstanceMapDaoHibernate
        extends AbstractDaoHibernate implements DiscourseInstanceMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public DiscourseInstanceMapItem get(DiscourseInstanceMapId id) {
        return (DiscourseInstanceMapItem)get(DiscourseInstanceMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public DiscourseInstanceMapItem find(DiscourseInstanceMapId id) {
        return (DiscourseInstanceMapItem)find(DiscourseInstanceMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(DiscourseInstanceMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByDiscourse method. */
    private static final String FIND_BY_DISCOURSE_HQL
        = "FROM DiscourseInstanceMapItem map WHERE discourse = ?";

    /**
     *  Return a list of DiscourseInstanceMapItems.
     *  @param discourseItem the given discourse item
     *  @return a list of items
     */
    public List<DiscourseInstanceMapItem> findByDiscourse(DiscourseItem discourseItem) {
        Object[] params = { discourseItem };
        List<DiscourseInstanceMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_DISCOURSE_HQL, params);
        return itemList;
    }

    /** HQL query for the findByInstance method. */
    private static final String FIND_BY_INSTANCE_HQL
        = "FROM DiscourseInstanceMapItem map WHERE remoteInstance = ?";

    /**
     *  Return a list of DiscourseInstanceMapItems.
     *  @param instanceItem the given remote instance item
     *  @return a list of items
     */
    public List<DiscourseInstanceMapItem> findByInstance(RemoteInstanceItem instanceItem) {
        Object[] params = { instanceItem };
        List<DiscourseInstanceMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_INSTANCE_HQL, params);
        return itemList;
    }

    /**
     *  Returns whether or not the discourse is remote.
     *  @param discourse the DiscourseItem
     *  @return whether or not the discourse is remote
     */
    public boolean isDiscourseRemote(DiscourseItem discourse) {
        List<DiscourseInstanceMapItem> mapList = findByDiscourse(discourse);
        return ((mapList != null) && (mapList.size() > 0));
    }

}
