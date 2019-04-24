/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.AnnotationAggregateItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

/**
 * Discourse AnnotationAggregate Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AnnotationAggregateDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a AnnotationAggregateItem by id.
     * @param id The id of the AnnotationAggregateItem.
     * @return the matching AnnotationAggregateItem or null if none found
     */
    AnnotationAggregateItem get(Long id);

    /**
     * Standard find for an AnnotationAggregateItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired AnnotationAggregateItem.
     * @return the matching AnnotationAggregateItem.
     */
    AnnotationAggregateItem find(Long id);

    /**
     * Standard "find all" for AnnotationAggregateItem.
     * @return a List of objects
     */
    List<AnnotationAggregateItem> findAll();

    /**
     * Return a list of the data_sources, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<AnnotationAggregateItem> the batch of items
     */
    List<AnnotationAggregateItem> getAnnotationAggregates(int offset, int max);

    /**
     * Return a count of the annotation_aggregate in a given discourse.
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByDiscourse(DiscourseItem discourse);

    /**
      * Return a string which lists the distinct annotation types in a given discourse.
     * @param discourse the Discourse item
     * @return String, comma-separated list of types
     */
    String getAnnotationTypesByDiscourse(DiscourseItem discourse);

    /**
     * Return the AnnotationAggregateItem for the specified source id.
     * @param sourceId the id
     * @return the AnnotationAggregateItem
     */
    AnnotationAggregateItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
