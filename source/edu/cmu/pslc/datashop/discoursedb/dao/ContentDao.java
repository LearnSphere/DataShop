/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.ContentItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;

/**
 * DiscourseDB Content Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ContentDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a ContentItem by id.
     * @param id The id of the ContentItem.
     * @return the matching ContentItem or null if none found
     */
    ContentItem get(Long id);

    /**
     * Standard find for an ContentItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ContentItem.
     * @return the matching ContentItem.
     */
    ContentItem find(Long id);

    /**
     * Standard "find all" for ContentItem.
     * @return a List of objects
     */
    List<ContentItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of the content, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<ContentItem> the batch of items
     */
    List<ContentItem> getContents(int offset, int max);

    /**
     * Return a list of ContentItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of ContentItem objects
     */
    List<ContentItem> findByDataSources(DataSourcesItem item);

    /**
     * Return a list of ContentItem objects given a DUserItem
     * @param item the given user item
     * @return a list of ContentItem objects
     */
    List<ContentItem> findByUser(DUserItem item);

    /**
     * Return the ContentItem for the specified source id.
     * @param sourceId the id
     * @return the ContentItem
     */
    ContentItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
