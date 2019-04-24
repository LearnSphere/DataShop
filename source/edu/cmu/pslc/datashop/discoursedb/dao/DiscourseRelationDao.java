/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseRelationItem;

/**
 * DiscourseDB DiscourseRelation Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscourseRelationDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DiscourseRelationItem by id.
     * @param id The id of the DiscourseRelationItem.
     * @return the matching DiscourseRelationItem or null if none found
     */
    DiscourseRelationItem get(Long id);

    /**
     * Standard find for an DiscourseRelationItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DiscourseRelationItem.
     * @return the matching DiscourseRelationItem.
     */
    DiscourseRelationItem find(Long id);

    /**
     * Standard "find all" for DiscourseRelationItem.
     * @return a List of objects
     */
    List<DiscourseRelationItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a count of DiscourseRelationItem objects given a DiscourseItem.
     * @param discourse the given Discourse
     * @return count
     */
    Long getCountByDiscourse(DiscourseItem discourse);

    /**
     * Return a list of the discourseRelations, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DiscourseRelationsItem> the batch of items
     */
    List<DiscourseRelationItem> getDiscourseRelations(int offset, int max);

    /**
     * Return a list of DiscourseRelationItem objects given a source contribution.
     * @param item the given Contribution item
     * @return a list of DiscourseRelationItem objects
     */
    List<DiscourseRelationItem> findBySource(ContributionItem item);

    /**
     * Return a list of DiscourseRelationItem objects given a target contribution.
     * @param item the given Contribution item
     * @return a list of DiscourseRelationItem objects
     */
    List<DiscourseRelationItem> findByTarget(ContributionItem item);

    /**
     * Return a list of DiscourseRelation types given a DiscourseItem.
     * @param discourse the given Discourse
     * @return a list of Strings
     */
    List<String> findTypesByDiscourse(DiscourseItem discourse);

    /**
     * Return a count of the discourse relations by type, in a given discourse.
     * @param type the DiscourseRelation type
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByType(String relationType, DiscourseItem discourse);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
