/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;

/**
 * DiscourseDB DiscoursePart Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscoursePartDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DiscoursePartItem by id.
     * @param id The id of the DiscoursePartItem.
     * @return the matching DiscoursePartItem or null if none found
     */
    DiscoursePartItem get(Long id);

    /**
     * Standard find for an DiscoursePartItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DiscoursePartItem.
     * @return the matching DiscoursePartItem.
     */
    DiscoursePartItem find(Long id);

    /**
     * Standard "find all" for DiscoursePartItem.
     * @return a List of objects
     */
    List<DiscoursePartItem> findAll();

    //
    // Non-standard methods begin.
    //


    /**
     * Return a list of the discourseParts, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DiscoursePartItem> the batch of items
     */
    List<DiscoursePartItem> getDiscourseParts(int offset, int max);

    /**
     * Return a list of DiscoursePartItem objects given a DiscourseItem.
     * @param discourse the given Discourse
     * @return a list of DiscoursePartItem objects
     */
    List<DiscoursePartItem> findByDiscourse(DiscourseItem discourse);

    /**
     * Return a count of DiscoursePartItem objects given a DiscourseItem.
     * @param discourse the given Discourse
     * @return count
     */
    Long getCountByDiscourse(DiscourseItem discourse);

    /**
     * Return a list of DiscoursePart types given a DiscourseItem.
     * @param discourse the given Discourse
     * @return a list of Strings
     */
    List<String> findTypesByDiscourse(DiscourseItem discourse);

    /**
     * Return a count of the discourse parts by type.
     * @param type the DiscoursePart type
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByType(String discoursePartType, DiscourseItem discourse);

    /**
     * Return a list of DiscoursePartItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of DiscoursePartItem objects
     */
    List<DiscoursePartItem> findByDataSources(DataSourcesItem item);

    /**
     * Return the DiscoursePartItem for the specified source id.
     * @param sourceId the id
     * @return the DiscoursePartItem
     */
    DiscoursePartItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
