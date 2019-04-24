/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.dao.AnnotationInstanceDao;
import edu.cmu.pslc.datashop.discoursedb.item.AnnotationAggregateItem;
import edu.cmu.pslc.datashop.discoursedb.item.AnnotationInstanceItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;

import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate and Spring implementation of the AnnotationInstanceDao
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AnnotationInstanceDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements AnnotationInstanceDao
{
    /**
     * Standard get for a AnnotationInstanceItem by id.
     * @param id The id of the user.
     * @return the matching AnnotationInstanceItem or null if none found
     */
    public AnnotationInstanceItem get(Long id) {
        return (AnnotationInstanceItem)get(AnnotationInstanceItem.class, id);
    }

    /**
     * Standard find for an AnnotationInstanceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired AnnotationInstanceItem.
     * @return the matching AnnotationInstanceItem.
     */
    public AnnotationInstanceItem find(Long id) {
        return (AnnotationInstanceItem)find(AnnotationInstanceItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<AnnotationInstanceItem> findAll() {
        return findAll(AnnotationInstanceItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for batch of users. */
    private static final String ANNOTATION_INSTANCES_QUERY =
        "SELECT DISTINCT ai FROM AnnotationInstanceItem ai";

    /**
     * Return a list of the annotation instance, starting at the specified
     * offset, limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<AnnotationInstanceItem> the batch of items
     */
    public List<AnnotationInstanceItem> getAnnotationInstances(int offset, int max) {

        CallbackCreatorHelper helper =
            new CallbackCreatorHelper(ANNOTATION_INSTANCES_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<AnnotationInstanceItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of AnnotationInstanceItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of AnnotationInstanceItem objects
     */
    public List<AnnotationInstanceItem> findByDataSources(DataSourcesItem item) {
        return getHibernateTemplate().
            find("FROM AnnotationInstanceItem WHERE dataSourceAggregate = ?", item);
    }

    /**
     * Return a list of AnnotationInstanceItem objects given an AnnotationAggregateItem.
     * @param item the given data sources item
     * @return a list of AnnotationInstanceItem objects
     */
    public List<AnnotationInstanceItem> findByAnnotationAggregate(AnnotationAggregateItem item) {
        return getHibernateTemplate().
            find("FROM AnnotationInstanceItem WHERE annotationAggregate = ?", item);
    }

    /**
     * Return the AnnotationInstanceItem for the specified source id.
     * @param sourceId the id
     * @return the AnnotationInstanceItem
     */
    public AnnotationInstanceItem findBySourceId(Long sourceId) {
        AnnotationInstanceItem result = null;

        List<AnnotationInstanceItem> list =
            getHibernateTemplate().find("FROM AnnotationInstanceItem WHERE source_id = ?",
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
        String updateStr = "UPDATE AnnotationInstanceItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
