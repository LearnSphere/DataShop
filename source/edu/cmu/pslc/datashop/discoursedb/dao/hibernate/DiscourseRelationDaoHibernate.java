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

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseRelationDao;

import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseRelationItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the DiscourseRelationDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseRelationDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DiscourseRelationDao
{
    /**
     * Standard get for a DiscourseRelationItem by id.
     * @param id The id of the discourse part
     * @return the matching DiscourseRelationItem or null if none found
     */
    public DiscourseRelationItem get(Long id) {
        return (DiscourseRelationItem)get(DiscourseRelationItem.class, id);
    }

    /**
     * Standard find for an DiscourseRelationItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DiscourseRelationItem.
     * @return the matching DiscourseRelationItem.
     */
    public DiscourseRelationItem find(Long id) {
        return (DiscourseRelationItem)find(DiscourseRelationItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DiscourseRelationItem> findAll() {
        return findAll(DiscourseRelationItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant SQL query for Discourse Relation by Discourse. */
    private static final String COUNT_BY_DISCOURSE =
        "SELECT COUNT(distinct dr.id_discourse_relation) AS theCount FROM discourse_relation dr "
        + "JOIN contribution c ON ((c.id_contribution = dr.fk_source) OR "
        + "(c.id_contribution = dr.fk_target)) "
        + "JOIN data_source_aggregate ds ON (ds.id_data_sources = c.fk_data_sources) "
        + "WHERE ds.discourse_id = :discourse";

    /**
     * Return a count of the discourse relations by discourse.
     * @param discourse the DiscourseItem
     * @return count
     */
    public Long getCountByDiscourse(DiscourseItem discourse) {

        Long result = new Long(0);

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(COUNT_BY_DISCOURSE);
            query.addScalar("theCount", Hibernate.LONG);
            query.setParameter("discourse", (Long)discourse.getId());

            List dbResults = query.list();
            if (dbResults.size() > 0) {
                result = (Long)dbResults.get(0);
            }
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /** Constant SQL query for contributions. */
    private static final String DISCOURSE_RELATIONS_QUERY =
        "SELECT DISTINCT discourseRelation FROM DiscourseRelationItem discourseRelation";

    /**
     * Return a list of the discourseRelations, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DiscourseRelationItem> the batch of items
     */
    public List<DiscourseRelationItem> getDiscourseRelations(int offset, int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(DISCOURSE_RELATIONS_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DiscourseRelationItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of DiscourseRelationItem objects given a source contribution.
     * @param item the given Contribution item
     * @return a list of DiscourseRelationItem objects
     */
    public List<DiscourseRelationItem> findBySource(ContributionItem item) {
        return getHibernateTemplate().find("FROM DiscourseRelationItem WHERE source = ?", item);
    }

    /**
     * Return a list of DiscourseRelationItem objects given a target contribution.
     * @param item the given Contribution item
     * @return a list of DiscourseRelationItem objects
     */
    public List<DiscourseRelationItem> findByTarget(ContributionItem item) {
        return getHibernateTemplate().find("FROM DiscourseRelationItem WHERE target = ?", item);
    }

    /** Constant SQL query for Contribution types by Discourse. */
    private static final String QUERY_TYPES_BY_DISCOURSE =
        "SELECT DISTINCT dr.type FROM discourse_relation dr "
        + "JOIN contribution c ON ((c.id_contribution = dr.fk_source) OR "
        + "(c.id_contribution = dr.fk_target)) "
        + "JOIN data_source_aggregate ds ON (ds.id_data_sources = c.fk_data_sources) "
        + "WHERE ds.discourse_id = :discourse";

    /**
     * Return a list of Contribution types given a DiscourseItem.
     * @param discourse the given Discourse
     * @return a list of Strings
     */
    public List<String> findTypesByDiscourse(DiscourseItem discourse) {
        List<String> result = null;

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(QUERY_TYPES_BY_DISCOURSE);
            query.setParameter("discourse", (Long)discourse.getId());

            result = query.list();
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /** Constant SQL query for Discourse Relation by type. */
    private static final String COUNT_BY_TYPE =
        "SELECT COUNT(distinct dr.id_discourse_relation) AS theCount FROM discourse_relation dr "
        + "JOIN contribution c ON ((c.id_contribution = dr.fk_source) OR "
        + "(c.id_contribution = dr.fk_target)) "
        + "JOIN data_source_aggregate ds ON (ds.id_data_sources = c.fk_data_sources) "
        + "WHERE dr.type = :type AND ds.discourse_id = :discourse";

    /**
     * Return a count of the discourse relations by type.
     * @param type the DiscourseRelation type
     * @param discourse the Discourse item
     * @return count
     */
    public Long getCountByType(String relationType, DiscourseItem discourse) {

        Long result = new Long(0);

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(COUNT_BY_TYPE);
            query.addScalar("theCount", Hibernate.LONG);
            query.setParameter("type", relationType);
            query.setParameter("discourse", (Long)discourse.getId());

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
        String updateStr = "UPDATE DiscourseRelationItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
