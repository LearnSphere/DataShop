/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

/**
 * Discourse DataSources Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DataSourcesDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DataSourcesItem by id.
     * @param id The id of the DataSourcesItem.
     * @return the matching DataSourcesItem or null if none found
     */
    DataSourcesItem get(Long id);

    /**
     * Standard find for an DataSourcesItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DataSourcesItem.
     * @return the matching DataSourcesItem.
     */
    DataSourcesItem find(Long id);

    /**
     * Standard "find all" for DataSourcesItem.
     * @return a List of objects
     */
    List<DataSourcesItem> findAll();

    /**
     * Return a list of the data_sources, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DataSourcesItem> the batch of items
     */
    List<DataSourcesItem> getDataSources(int offset, int max);

    /**
     * Return a count of the data_sources in a given discourse.
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByDiscourse(DiscourseItem discourse);

    /**
      * Return a string which lists the distinct data source types in a given discourse.
     * @param discourse the Discourse item
     * @return String, comma-separated list of types
     */
    String getDataSourceTypesByDiscourse(DiscourseItem discourse);

    /**
     * Return a string which lists the distinct data source datasets in a given discourse.
     * @param discourse the Discourse item
     * @return String, comma-separated list of dataset names
     */
    String getDataSourceDatasetsByDiscourse(DiscourseItem discourse);

    /**
     * Return the DataSourcesItem for the specified source id.
     * @param sourceId the id
     * @return the DataSourcesItem
     */
    DataSourcesItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
