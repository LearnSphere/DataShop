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
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import org.springframework.orm.hibernate3.HibernateCallback;

import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.LONG;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.TIMESTAMP;

/**
 * Hibernate and Spring implementation of the DataSourcesDao
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DataSourcesDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DataSourcesDao
{
    /**
     * Standard get for a DataSourcesItem by id.
     * @param id The id of the user.
     * @return the matching DataSourcesItem or null if none found
     */
    public DataSourcesItem get(Long id) {
        return (DataSourcesItem)get(DataSourcesItem.class, id);
    }

    /**
     * Standard find for an DataSourcesItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DataSourcesItem.
     * @return the matching DataSourcesItem.
     */
    public DataSourcesItem find(Long id) {
        return (DataSourcesItem)find(DataSourcesItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DataSourcesItem> findAll() {
        return findAll(DataSourcesItem.class);
    }

    /** Constant SQL query for data sources. */
    private static final String DATA_SOURCES_QUERY =
        "SELECT DISTINCT ds FROM DataSourcesItem ds";

    /**
     * Return a list of the data_sources, starting at the specified offset,
     * limited to the max count.
     * This assumes the query is on a db with only a single Discourse. Sigh.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DataSourcesItem> the batch of items
     */
    public List<DataSourcesItem> getDataSources(int offset, int max) {

        CallbackCreatorHelper helper = new CallbackCreatorHelper(DATA_SOURCES_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DataSourcesItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /** Constant SQL query for DataSource count by Discourse. */
    private static final String COUNT_BY_DISCOURSE =
        "SELECT COUNT(ds.id_data_sources) AS theCount FROM data_source_aggregate ds "
        + "WHERE ds.discourse_id = :discourseId";

    /**
     * Return a count of the data_sources in a given discourse.
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

    /** Constant SQL query for list of data source types by Discourse. */
    private static final String DATA_SOURCE_TYPES_BY_DISCOURSE =
        "SELECT GROUP_CONCAT(DISTINCT dsi.source_type SEPARATOR \", \") AS types "
        + "FROM data_source_instance dsi "
        + "JOIN data_source_aggregate ds ON (ds.id_data_sources = dsi.fk_sources) "
        + "WHERE ds.discourse_id = :discourseId";

    /**
     * Return a string which lists the distinct data source types in a given discourse.
     * @param discourse the Discourse item
     * @return String, comma-separated list of types
     */
    public String getDataSourceTypesByDiscourse(DiscourseItem discourse) {

        String result = "";

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(DATA_SOURCE_TYPES_BY_DISCOURSE);
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

    /** Constant SQL query for list of data source datasets by Discourse. */
    private static final String DATA_SOURCE_DATASETS_BY_DISCOURSE =
        "SELECT GROUP_CONCAT(DISTINCT dsi.dataset_name SEPARATOR \", \") AS datasets "
        + "FROM data_source_instance dsi "
        + "JOIN data_source_aggregate ds ON (ds.id_data_sources = dsi.fk_sources) "
        + "WHERE ds.discourse_id = :discourseId";

    /**
     * Return a string which lists the distinct data source datasets in a given discourse.
     * @param discourse the Discourse item
     * @return String, comma-separated list of dataset names
     */
    public String getDataSourceDatasetsByDiscourse(DiscourseItem discourse) {

        String result = "";

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(DATA_SOURCE_DATASETS_BY_DISCOURSE);
            query.addScalar("datasets", Hibernate.STRING);
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
     * Return the DataSourcesItem for the specified source id.
     * @param sourceId the id
     * @return the DataSourcesItem
     */
    public DataSourcesItem findBySourceId(Long sourceId) {
        DataSourcesItem result = null;

        List<DataSourcesItem> list =
            getHibernateTemplate().find("FROM DataSourcesItem WHERE source_id = ?", sourceId);

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
        String updateStr = "UPDATE DataSourcesItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
