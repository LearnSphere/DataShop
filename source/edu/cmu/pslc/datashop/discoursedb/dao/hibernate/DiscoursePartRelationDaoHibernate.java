/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartRelationDao;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartRelationItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the DiscoursePartRelationDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscoursePartRelationDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DiscoursePartRelationDao
{
    /**
     * Standard get for a DiscoursePartRelationItem by id.
     * @param id The id of the discourse part
     * @return the matching DiscoursePartRelationItem or null if none found
     */
    public DiscoursePartRelationItem get(Long id) {
        return (DiscoursePartRelationItem)get(DiscoursePartRelationItem.class, id);
    }

    /**
     * Standard find for an DiscoursePartRelationItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DiscoursePartRelationItem.
     * @return the matching DiscoursePartRelationItem.
     */
    public DiscoursePartRelationItem find(Long id) {
        return (DiscoursePartRelationItem)find(DiscoursePartRelationItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DiscoursePartRelationItem> findAll() {
        return findAll(DiscoursePartRelationItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant SQL query for contributions. */
    private static final String DISCOURSE_RELATIONS_QUERY =
        "SELECT DISTINCT dpr FROM DiscoursePartRelationItem dpr";

    /**
     * Return a list of the discoursePartRelations, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DiscoursePartRelationItem> the batch of items
     */
    public List<DiscoursePartRelationItem> getDiscoursePartRelations(int offset, int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(DISCOURSE_RELATIONS_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DiscoursePartRelationItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of DiscoursePartRelationItem objects given a source contribution.
     * @param item the given Contribution item
     * @return a list of DiscoursePartRelationItem objects
     */
    public List<DiscoursePartRelationItem> findBySource(ContributionItem item) {
        return getHibernateTemplate().find("FROM DiscoursePartRelationItem WHERE source = ?", item);
    }

    /**
     * Return a list of DiscoursePartRelationItem objects given a target contribution.
     * @param item the given Contribution item
     * @return a list of DiscoursePartRelationItem objects
     */
    public List<DiscoursePartRelationItem> findByTarget(ContributionItem item) {
        return getHibernateTemplate().find("FROM DiscoursePartRelationItem WHERE target = ?", item);
    }

    /** Constant SQL query for Discourse Part Relation by type. */
    private static final String COUNT_BY_TYPE =
        "SELECT COUNT(dr.id_discourse_part_relation) AS theCount FROM discourse_part_relation dpr "
        + "WHERE dpr.type = :type";

    /**
     * Return a count of the discourse part relations by type.
     * @param type the DiscoursePartRelation type
     * @return count
     */
    public Long getCountByType(String relationType) {

        Long result = new Long(0);

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(COUNT_BY_TYPE);
            query.addScalar("theCount", Hibernate.LONG);
            query.setParameter("type", relationType);

            List dbResults = query.list();
            if (dbResults.size() > 0) {
                result = (Long)dbResults.get(0);
            }
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /**
     * Clear the source_id column now that Discourse has been imported.
     */
    public Integer clearSourceIds() {
        String updateStr = "UPDATE DiscoursePartRelationItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
