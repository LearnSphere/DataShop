/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.dao.DataSourceInstanceDao;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourceInstanceItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the DataSourceInstanceDao
 *
 * @author Cindy Tipper
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DataSourceInstanceDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DataSourceInstanceDao
{
    /**
     * Standard get for a DataSourceInstanceItem by id.
     * @param id The id of the user.
     * @return the matching DataSourceInstanceItem or null if none found
     */
    public DataSourceInstanceItem get(Long id) {
        return (DataSourceInstanceItem)get(DataSourceInstanceItem.class, id);
    }

    /**
     * Standard find for an DataSourceInstanceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DataSourceInstanceItem.
     * @return the matching DataSourceInstanceItem.
     */
    public DataSourceInstanceItem find(Long id) {
        return (DataSourceInstanceItem)find(DataSourceInstanceItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DataSourceInstanceItem> findAll() {
        return findAll(DataSourceInstanceItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for batch of users. */
    private static final String DATA_SOURCE_INSTANCES_QUERY =
        "SELECT DISTINCT dsi FROM DataSourceInstanceItem dsi";

    /**
     * Return a list of the data source instance, starting at the specified
     * offset, limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DataSourceInstanceItem> the batch of items
     */
    public List<DataSourceInstanceItem> getDataSourceInstances(int offset, int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(DATA_SOURCE_INSTANCES_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DataSourceInstanceItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of DataSourceInstanceItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of DataSourceInstanceItem objects
     */
    public List<DataSourceInstanceItem> findByDataSources(DataSourcesItem item) {
        return getHibernateTemplate().find("FROM DataSourceInstanceItem WHERE dataSources = ?",
                                           item);
    }

    /**
     * Return the DataSourceInstanceItem for the specified source id.
     * @param sourceId the id
     * @return the DataSourceInstanceItem
     */
    public DataSourceInstanceItem findBySourceId(Long sourceId) {
        DataSourceInstanceItem result = null;

        List<DataSourceInstanceItem> list =
            getHibernateTemplate().find("FROM DataSourceInstanceItem WHERE source_id = ?",
                                        sourceId);

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
        String updateStr = "UPDATE DataSourceInstanceItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
