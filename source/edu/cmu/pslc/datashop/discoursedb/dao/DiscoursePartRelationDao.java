/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartRelationItem;

/**
 * DiscourseDB DiscoursePartRelation Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscoursePartRelationDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DiscoursePartRelationItem by id.
     * @param id The id of the DiscoursePartRelationItem.
     * @return the matching DiscoursePartRelationItem or null if none found
     */
    DiscoursePartRelationItem get(Long id);

    /**
     * Standard find for an DiscoursePartRelationItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DiscoursePartRelationItem.
     * @return the matching DiscoursePartRelationItem.
     */
    DiscoursePartRelationItem find(Long id);

    /**
     * Standard "find all" for DiscoursePartRelationItem.
     * @return a List of objects
     */
    List<DiscoursePartRelationItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of the discourseRelations, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DiscoursePartRelationsItem> the batch of items
     */
    List<DiscoursePartRelationItem> getDiscoursePartRelations(int offset, int max);

    /**
     * Return a list of DiscoursePartRelationItem objects given a source contribution.
     * @param item the given Contribution item
     * @return a list of DiscoursePartRelationItem objects
     */
    List<DiscoursePartRelationItem> findBySource(ContributionItem item);

    /**
     * Return a list of DiscoursePartRelationItem objects given a target contribution.
     * @param item the given Contribution item
     * @return a list of DiscoursePartRelationItem objects
     */
    List<DiscoursePartRelationItem> findByTarget(ContributionItem item);

    /**
     * Return a count of the discourse part relations by type, in a given discourse.
     * @param type the DiscoursePartRelation type
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByType(String relationType);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
