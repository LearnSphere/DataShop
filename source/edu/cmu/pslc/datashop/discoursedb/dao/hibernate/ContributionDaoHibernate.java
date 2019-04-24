/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDao;
import edu.cmu.pslc.datashop.discoursedb.item.ContentItem;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the ContributionDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContributionDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements ContributionDao
{
    /**
     * Standard get for a ContributionItem by id.
     * @param id The id of the discourse part
     * @return the matching ContributionItem or null if none found
     */
    public ContributionItem get(Long id) {
        return (ContributionItem)get(ContributionItem.class, id);
    }

    /**
     * Standard find for an ContributionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ContributionItem.
     * @return the matching ContributionItem.
     */
    public ContributionItem find(Long id) {
        return (ContributionItem)find(ContributionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<ContributionItem> findAll() {
        return findAll(ContributionItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant SQL query for contributions. */
    private static final String CONTRIBUTIONS_QUERY =
        "SELECT DISTINCT contribution FROM ContributionItem contribution";

    /**
     * Return a list of the contributions, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<ContributionItem> the batch of items
     */
    public List<ContributionItem> getContributions(int offset, int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(CONTRIBUTIONS_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<ContributionItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of ContributionItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of ContributionItem objects
     */
    public List<ContributionItem> findByDataSources(DataSourcesItem item) {
        return getHibernateTemplate().find("FROM ContributionItem WHERE dataSources = ?", item);
    }

    /**
     * Return a list of ContributionItem objects given a current revision.
     * @param item the given Content item
     * @return a list of ContributionItem objects
     */
    public List<ContributionItem> findByCurrentRevision(ContentItem item) {
        return getHibernateTemplate().find("FROM ContributionItem WHERE currentRevision = ?", item);
    }

    /**
     * Return a list of ContributionItem objects given a first revision.
     * @param item the given Content item
     * @return a list of ContributionItem objects
     */
    public List<ContributionItem> findByFirstRevision(ContentItem item) {
        return getHibernateTemplate().find("FROM ContributionItem WHERE firstRevision = ?", item);
    }

    /** Constant SQL query for Contribution count by Discourse. */
    private static final String COUNT_BY_DISCOURSE =
        "SELECT COUNT(c.id_contribution) AS theCount FROM contribution c "
        + "JOIN contribution_partof_discourse_part map ON "
        + "(c.id_contribution = map.fk_contribution) "
        + "JOIN discourse_has_discourse_part map2 ON "
        + "(map2.fk_discourse_part = map.fk_discourse_part) "
        + "WHERE map2.fk_discourse = :discourseId";

    /**
     * Return a count of the contributions in a given discourse.
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

    /** Constant SQL query for Contribution types by Discourse. */
    private static final String QUERY_TYPES_BY_DISCOURSE =
        "SELECT DISTINCT c.type FROM contribution c "
        + "JOIN contribution_partof_discourse_part map ON "
        + "(c.id_contribution = map.fk_contribution) "
        + "JOIN discourse_has_discourse_part map2 ON "
        + "(map2.fk_discourse_part = map.fk_discourse_part) "
        + "WHERE map2.fk_discourse = :discourse";

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

    /** Constant SQL query for Contribution count by type. */
    private static final String COUNT_BY_TYPE =
        "SELECT COUNT(c.id_contribution) AS theCount FROM contribution c "
        + "JOIN contribution_partof_discourse_part map ON "
        + "(c.id_contribution = map.fk_contribution) "
        + "JOIN discourse_has_discourse_part map2 ON "
        + "(map2.fk_discourse_part = map.fk_discourse_part) "
        + "WHERE c.type = :type AND map2.fk_discourse = :discourse";

    /**
     * Return a count of the contributions by type.
     * @param contributionType the Contribution type
     * @param discourse the Discourse item
     * @return count
     */
    public Long getCountByType(String contributionType, DiscourseItem discourse) {

        Long result = new Long(0);

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(COUNT_BY_TYPE);
            query.addScalar("theCount", Hibernate.LONG);
            query.setParameter("type", contributionType);
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
     * Return the ContributionItem for the specified source id.
     * @param sourceId the id
     * @return the ContributionItem
     */
    public ContributionItem findBySourceId(Long sourceId) {
        ContributionItem result = null;

        List<ContributionItem> list =
            getHibernateTemplate().find("FROM ContributionItem WHERE source_id = ?", sourceId);

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
        String updateStr = "UPDATE ContributionItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
