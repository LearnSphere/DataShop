/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;

/**
 * DiscourseDB User Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DUserDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DUserItem by id.
     * @param id The id of the DUserItem.
     * @return the matching DUserItem or null if none found
     */
    DUserItem get(Long id);

    /**
     * Standard find for an DUserItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DUserItem.
     * @return the matching DUserItem.
     */
    DUserItem find(Long id);

    /**
     * Standard "find all" for DUserItem.
     * @return a List of objects
     */
    List<DUserItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of the users, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DUserItem> the batch of items
     */
    List<DUserItem> getUsers(int offset, int max);

    /**
     * Return a list of DUserItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of DUserItem objects
     */
    List<DUserItem> findByDataSources(DataSourcesItem item);

    /**
     * Return a count of the users in a given discourse.
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByDiscourse(DiscourseItem discourse);

    /**
     * Return the DUserItem for the specified source id.
     * @param sourceId the id
     * @return the DUserItem
     */
    DUserItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
