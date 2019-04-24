/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DataSourceInstanceItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;

/**
 * DataSourceInstance Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DataSourceInstanceDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DataSourceInstanceItem by id.
     * @param id The id of the DataSourceInstanceItem.
     * @return the matching DataSourceInstanceItem or null if none found
     */
    DataSourceInstanceItem get(Long id);

    /**
     * Standard find for an DataSourceInstanceItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DataSourceInstanceItem.
     * @return the matching DataSourceInstanceItem.
     */
    DataSourceInstanceItem find(Long id);

    /**
     * Standard "find all" for DataSourceInstanceItem.
     * @return a List of objects
     */
    List<DataSourceInstanceItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of the data source instances, starting at the specified
     * offset, limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DataSourceInstanceItem> the batch of items
     */
    List<DataSourceInstanceItem> getDataSourceInstances(int offset, int max);

    /**
     * Return a list of DataSourceInstanceItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of DataSourceInstanceItem objects
     */
    List<DataSourceInstanceItem> findByDataSources(DataSourcesItem item);

    /**
     * Return the DataSourceInstanceItem for the specified source id.
     * @param sourceId the id
     * @return the DataSourceInstanceItem
     */
    DataSourceInstanceItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
