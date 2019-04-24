/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

import edu.cmu.pslc.datashop.discoursedb.dao.DataSourcesDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartDao;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the DiscoursePartDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscoursePartDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DiscoursePartDao
{
    /**
     * Standard get for a DiscoursePartItem by id.
     * @param id The id of the discourse part
     * @return the matching DiscoursePartItem or null if none found
     */
    public DiscoursePartItem get(Long id) {
        return (DiscoursePartItem)get(DiscoursePartItem.class, id);
    }

    /**
     * Standard find for an DiscoursePartItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DiscoursePartItem.
     * @return the matching DiscoursePartItem.
     */
    public DiscoursePartItem find(Long id) {
        return (DiscoursePartItem)find(DiscoursePartItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DiscoursePartItem> findAll() {
        return findAll(DiscoursePartItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant SQL query for discourse parts. */
    private static final String DISCOURSE_PARTS_QUERY =
        "SELECT DISTINCT parts FROM DiscoursePartItem parts";

    /**
     * Return a list of the discourseParts, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DiscoursePartItem> the batch of items
     */
    public List<DiscoursePartItem> getDiscourseParts(int offset, int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(DISCOURSE_PARTS_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DiscoursePartItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /** Constant SQL query for DiscoursePartTypes by Discourse. */
    private static final String QUERY_BY_DISCOURSE =
        "SELECT dp.id_discourse_part AS id, dp.entity_created AS created, dp.type AS partType, "
        + "dp.end_time AS endTime, dp.start_time AS startTime, dp.entity_version AS version, "
        + "dp.name AS name, dp.fk_data_sources AS dataSourcesId, "
        + "FROM discourse_part dp "
        + "JOIN discourse_has_discourse_part map ON "
        + "(map.fk_discourse_part = dp.id_discourse_part) "
        + "WHERE map.fk_discourse = :discourseId";

    /**
     * Return a list of DiscoursePartItem objects given a DiscourseItem.
     * @param discourse the given Discourse
     * @return a list of DiscoursePartItem objects
     */
    public List<DiscoursePartItem> findByDiscourse(DiscourseItem discourse) {

        List<DiscoursePartItem> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(QUERY_BY_DISCOURSE);

            SQLQuery query = session.createSQLQuery(sb.toString());
            query.addScalar("id", Hibernate.LONG);
            query.addScalar("created", Hibernate.TIMESTAMP);
            query.addScalar("partType", Hibernate.STRING);
            query.addScalar("endTime", Hibernate.TIMESTAMP);
            query.addScalar("startTime", Hibernate.TIMESTAMP);
            query.addScalar("version", Hibernate.LONG);
            query.addScalar("name", Hibernate.STRING);
            query.addScalar("dataSourcesId", Hibernate.LONG);
            query.setParameter("discourseId", (Long)discourse.getId());

            List<Object[]> dbResults = query.list();

            DataSourcesDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDataSourcesDao();

            result = new ArrayList<DiscoursePartItem>();
            for (Object[] o : dbResults) {
                int index = 0;
                DiscoursePartItem item = new DiscoursePartItem();
                item.setId((Long)o[index++]);
                item.setCreated((Date)o[index++]);
                item.setDiscoursePartType((String)o[index++]);
                item.setEndTime((Date)o[index++]);
                item.setStartTime((Date)o[index++]);
                item.setVersion((Long)o[index++]);
                item.setName((String)o[index++]);
                Long dsId = (Long)o[index++];
                if (dsId != null) {
                    item.setDataSources(dsDao.get(dsId));
                }
                result.add(item);
            }
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /** Constant SQL query for DiscoursePart count by Discourse. */
    private static final String COUNT_BY_DISCOURSE =
        "SELECT COUNT(map.id_discourse_has_discourse_part) AS theCount "
        + "FROM discourse_has_discourse_part map WHERE map.fk_discourse = :discourse";

    /**
     * Return a count of the discourseParts by discourse.
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

    /** Constant SQL query for DiscoursePart types by Discourse. */
    private static final String QUERY_TYPES_BY_DISCOURSE =
        "SELECT DISTINCT dp.type FROM discourse_part dp "
        + "JOIN discourse_has_discourse_part map ON "
        + "(map.fk_discourse_part = dp.id_discourse_part) "
        + "WHERE map.fk_discourse = :discourseId";

    /**
     * Return a list of DiscoursePart types given a DiscourseItem.
     * @param discourse the given Discourse
     * @return a list of Strings
     */
    public List<String> findTypesByDiscourse(DiscourseItem discourse) {
        List<String> result = null;

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(QUERY_TYPES_BY_DISCOURSE);
            query.setParameter("discourseId", (Long)discourse.getId());

            result = query.list();
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /** Constant SQL query for DiscoursePart count by type. */
    private static final String COUNT_BY_TYPE =
        "SELECT COUNT(dp.id_discourse_part) AS theCount FROM discourse_part dp "
        + "JOIN discourse_has_discourse_part map ON (map.fk_discourse_part = dp.id_discourse_part) "
        + "WHERE dp.type = :type AND map.fk_discourse = :discourse";

    /**
     * Return a count of the discourseParts by type.
     * @param type the DiscoursePart type
     * @param discourse the Discourse item
     * @return count
     */
    public Long getCountByType(String discoursePartType, DiscourseItem discourse) {

        Long result = new Long(0);

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(COUNT_BY_TYPE);
            query.addScalar("theCount", Hibernate.LONG);
            query.setParameter("type", discoursePartType);
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
     * Return a list of DiscoursePartItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of DiscoursePartItem objects
     */
    public List<DiscoursePartItem> findByDataSources(DataSourcesItem item) {
        return getHibernateTemplate().find("FROM DiscoursePartItem WHERE dataSources = ?", item);
    }

    /**
     * Return the DiscoursePartItem for the specified source id.
     * @param sourceId the id
     * @return the DiscoursePartItem
     */
    public DiscoursePartItem findBySourceId(Long sourceId) {
        DiscoursePartItem result = null;

        List<DiscoursePartItem> list =
            getHibernateTemplate().find("FROM DiscoursePartItem WHERE source_id = ?", sourceId);

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
        String updateStr = "UPDATE DiscoursePartItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
