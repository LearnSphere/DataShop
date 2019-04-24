/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.AnnotationAggregateItem;
import edu.cmu.pslc.datashop.discoursedb.item.AnnotationInstanceItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;

/**
 * AnnotationInstance Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AnnotationInstanceDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a AnnotationInstanceItem by id.
     * @param id The id of the AnnotationInstanceItem.
     * @return the matching AnnotationInstanceItem or null if none found
     */
    AnnotationInstanceItem get(Long id);

    /**
     * Standard find for an AnnotationInstanceItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired AnnotationInstanceItem.
     * @return the matching AnnotationInstanceItem.
     */
    AnnotationInstanceItem find(Long id);

    /**
     * Standard "find all" for AnnotationInstanceItem.
     * @return a List of objects
     */
    List<AnnotationInstanceItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of the annotation instances, starting at the specified
     * offset, limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<AnnotationInstanceItem> the batch of items
     */
    List<AnnotationInstanceItem> getAnnotationInstances(int offset, int max);

    /**
     * Return a list of AnnotationInstanceItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of AnnotationInstanceItem objects
     */
    List<AnnotationInstanceItem> findByDataSources(DataSourcesItem item);

    /**
     * Return a list of AnnotationInstanceItem objects given an AnnotationAggregateItem.
     * @param item the given annotation aggregate item
     * @return a list of AnnotationInstanceItem objects
     */
    List<AnnotationInstanceItem> findByAnnotationAggregate(AnnotationAggregateItem item);

    /**
     * Return the AnnotationInstanceItem for the specified source id.
     * @param sourceId the id
     * @return the AnnotationInstanceItem
     */
    AnnotationInstanceItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
