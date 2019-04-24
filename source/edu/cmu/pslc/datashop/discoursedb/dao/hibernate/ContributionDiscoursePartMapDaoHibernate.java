/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionDiscoursePartMapId;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionDiscoursePartMapItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the ContributionDiscoursePartMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContributionDiscoursePartMapDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements ContributionDiscoursePartMapDao
{
    /**
     * Standard get for a ContributionDiscoursePartMapItem by id.
     * @param id The id of the map
     * @return the matching ContributionDiscoursePartMapItem or null if none found
     */
    public ContributionDiscoursePartMapItem get(ContributionDiscoursePartMapId id) {
        return (ContributionDiscoursePartMapItem)get(ContributionDiscoursePartMapItem.class, id);
    }

    /**
     * Standard find for an ContributionDiscoursePartMapItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ContributionDiscoursePartMapItem
     * @return the matching ContributionDiscoursePartMapItem.
     */
    public ContributionDiscoursePartMapItem find(ContributionDiscoursePartMapId id) {
        return (ContributionDiscoursePartMapItem)find(ContributionDiscoursePartMapItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<ContributionDiscoursePartMapItem> findAll() {
        return findAll(ContributionDiscoursePartMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant SQL query for contribution_partof_discourse_part rows. */
    private static final String CONTRIBUTION_DISCOURSE_PART_MAP_QUERY =
        "SELECT DISTINCT map FROM ContributionDiscoursePartMapItem map";

    /**
     * Return a list of the ContributionDiscoursePart types, starting at
     * the specified offset, limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<ContributionDiscoursePartMapItem> the batch of items
     */
    public List<ContributionDiscoursePartMapItem> getContributionDiscoursePartMap(int offset,
                                                                                  int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(CONTRIBUTION_DISCOURSE_PART_MAP_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<ContributionDiscoursePartMapItem> results =
            getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of ContributionDiscoursePartMapItem objects given a ContributionItem
     * @param contribution the DiscourseDB contribution item
     * @return a list of ContributionDiscoursePartMapItem objects
     */
    public List<ContributionDiscoursePartMapItem> findByContribution(ContributionItem contribution) {
        return getHibernateTemplate().
            find("FROM ContributionDiscoursePartMapItem WHERE contribution = ?", contribution);
    }

    /**
     * Return a list of ContributionDiscoursePartMapItem objects given a DiscoursePartItem
     * @param part the DiscourseDB discourse part item
     * @return a list of ContributionDiscoursePartMapItem objects
     */
    public List<ContributionDiscoursePartMapItem> findByDiscoursePart(DiscoursePartItem part) {
        return getHibernateTemplate().
            find("FROM ContributionDiscoursePartMapItem WHERE part = ?", part);
    }
}
