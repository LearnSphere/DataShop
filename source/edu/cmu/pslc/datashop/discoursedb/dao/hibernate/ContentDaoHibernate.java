/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.dao.ContentDao;
import edu.cmu.pslc.datashop.discoursedb.item.ContentItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the ContentDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContentDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements ContentDao
{
    /**
     * Standard get for a ContentItem by id.
     * @param id The id of the discourse part
     * @return the matching ContentItem or null if none found
     */
    public ContentItem get(Long id) {
        return (ContentItem)get(ContentItem.class, id);
    }

    /**
     * Standard find for an ContentItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ContentItem.
     * @return the matching ContentItem.
     */
    public ContentItem find(Long id) {
        return (ContentItem)find(ContentItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<ContentItem> findAll() {
        return findAll(ContentItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for batch of users. */
    private static final String CONTENT_QUERY =
        "SELECT DISTINCT content FROM ContentItem content";

    /**
     * Return a list of the content, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<ContentItem> the batch of items
     */
    public List<ContentItem> getContents(int offset, int max) {

        CallbackCreatorHelper helper = new CallbackCreatorHelper(CONTENT_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<ContentItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of ContentItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of ContentItem objects
     */
    public List<ContentItem> findByDataSources(DataSourcesItem item) {
        return getHibernateTemplate().find("FROM ContentItem WHERE dataSources = ?", item);
    }

    /**
     * Return a list of ContentItem objects given a DUserItem
     * @param item the given user item
     * @return a list of ContentItem objects
     */
    public List<ContentItem> findByUser(DUserItem item) {
        return getHibernateTemplate().find("FROM ContentItem WHERE user = ?", item);
    }

    /**
     * Return the ContentItem for the specified source id.
     * @param sourceId the id
     * @return the ContentItem
     */
    public ContentItem findBySourceId(Long sourceId) {
        ContentItem result = null;

        List<ContentItem> list =
            getHibernateTemplate().find("FROM ContentItem WHERE source_id = ?", sourceId);

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
        String updateStr = "UPDATE ContentItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
