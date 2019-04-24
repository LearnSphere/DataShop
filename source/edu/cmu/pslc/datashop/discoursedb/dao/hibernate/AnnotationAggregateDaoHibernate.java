/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

import edu.cmu.pslc.datashop.discoursedb.dao.AnnotationAggregateDao;
import edu.cmu.pslc.datashop.discoursedb.item.AnnotationAggregateItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import org.springframework.orm.hibernate3.HibernateCallback;

import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.LONG;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.TIMESTAMP;

/**
 * Hibernate and Spring implementation of the AnnotationAggregateDao
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AnnotationAggregateDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements AnnotationAggregateDao
{
    /**
     * Standard get for a AnnotationAggregateItem by id.
     * @param id The id of the user.
     * @return the matching AnnotationAggregateItem or null if none found
     */
    public AnnotationAggregateItem get(Long id) {
        return (AnnotationAggregateItem)get(AnnotationAggregateItem.class, id);
    }

    /**
     * Standard find for an AnnotationAggregateItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired AnnotationAggregateItem.
     * @return the matching AnnotationAggregateItem.
     */
    public AnnotationAggregateItem find(Long id) {
        return (AnnotationAggregateItem)find(AnnotationAggregateItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<AnnotationAggregateItem> findAll() {
        return findAll(AnnotationAggregateItem.class);
    }

    /** Constant SQL query for data sources. */
    private static final String ANNOTATION_AGGREGATE_QUERY =
        "SELECT DISTINCT ds FROM AnnotationAggregateItem ds";

    /**
     * Return a list of the annotation_aggregate, starting at the specified offset,
     * limited to the max count.
     * This assumes the query is on a db with only a single Discourse. Sigh.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<AnnotationAggregateItem> the batch of items
     */
    public List<AnnotationAggregateItem> getAnnotationAggregates(int offset, int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(ANNOTATION_AGGREGATE_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<AnnotationAggregateItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /** Constant SQL query for AnnotationAggregate count by Discourse. */
    private static final String COUNT_BY_DISCOURSE =
        "SELECT COUNT(aa.id_annotation) AS theCount FROM annotation_aggregate aa "
        + "WHERE aa.discourse_id = :discourseId";

    /**
     * Return a count of the annotation_aggregate in a given discourse.
     * @param discourse the Discourse item
     * @return count
     */
    public Long getCountByDiscourse(DiscourseItem discourse) {

        Long result = new Long(0);

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(COUNT_BY_DISCOURSE);
            query.addScalar("theCount", Hibernate.LONG);
            query.setParameter("discourseId", (Long)discourse.getId());

            List dbResults = query.list();
            if (dbResults.size() > 0) {
                result = (Long)dbResults.get(0);
            }
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /** Constant SQL query for list of annotation types by Discourse. */
    private static final String ANNOTATION_TYPES_BY_DISCOURSE =
        "SELECT GROUP_CONCAT(DISTINCT ai.type SEPARATOR \", \") AS types "
        + "FROM annotation_instance ai "
        + "JOIN annotation_aggregate aa ON (aa.id_annotation = ai.fk_annotation) "
        + "WHERE aa.discourse_id = :discourseId";

    /**
     * Return a string which lists the distinct annotation types in a given discourse.
     * @param discourse the Discourse item
     * @return String, comma-separated list of types
     */
    public String getAnnotationTypesByDiscourse(DiscourseItem discourse) {

        String result = "";

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(ANNOTATION_TYPES_BY_DISCOURSE);
            query.addScalar("types", Hibernate.STRING);
            query.setParameter("discourseId", (Long)discourse.getId());

            List dbResults = query.list();
            if (dbResults.size() > 0) {
                result = (String)dbResults.get(0);
            }
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /**
     * Return the AnnotationAggregateItem for the specified source id.
     * @param sourceId the id
     * @return the AnnotationAggregateItem
     */
    public AnnotationAggregateItem findBySourceId(Long sourceId) {
        AnnotationAggregateItem result = null;

        List<AnnotationAggregateItem> list = getHibernateTemplate().
            find("FROM AnnotationAggregateItem WHERE source_id = ?", sourceId);

        // Really only one of these...
        if (list.size() > 0) {
            result = list.get(0);
        }

        return result;
    }

    /**
     * Clear the source_id column now that Discourse has been imported.
     */
    public Integer clearSourceIds() {
        String updateStr = "UPDATE AnnotationAggregateItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
