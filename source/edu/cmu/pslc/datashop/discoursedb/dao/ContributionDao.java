/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.ContentItem;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;

/**
 * DiscourseDB Contribution Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 13000 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-23 14:46:19 -0400 (Wed, 23 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ContributionDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a ContributionItem by id.
     * @param id The id of the ContributionItem.
     * @return the matching ContributionItem or null if none found
     */
    ContributionItem get(Long id);

    /**
     * Standard find for an ContributionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ContributionItem.
     * @return the matching ContributionItem.
     */
    ContributionItem find(Long id);

    /**
     * Standard "find all" for ContributionItem.
     * @return a List of objects
     */
    List<ContributionItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of the contributions, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<ContributionsItem> the batch of items
     */
    List<ContributionItem> getContributions(int offset, int max);

    /**
     * Return a list of ContributionItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of ContributionItem objects
     */
    List<ContributionItem> findByDataSources(DataSourcesItem item);

    /**
     * Return a list of ContributionItem objects given a current revision.
     * @param item the given Content item
     * @return a list of ContributionItem objects
     */
    List<ContributionItem> findByCurrentRevision(ContentItem item);

    /**
     * Return a list of ContributionItem objects given a first revision.
     * @param item the given Content item
     * @return a list of ContributionItem objects
     */
    List<ContributionItem> findByFirstRevision(ContentItem item);

    /**
     * Return a count of the contributions in a given discourse.
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByDiscourse(DiscourseItem discourse);

    /**
     * Return a list of Contribution types given a DiscourseItem.
     * @param discourse the given Discourse
     * @return a list of Strings
     */
    List<String> findTypesByDiscourse(DiscourseItem discourse);

    /**
     * Return a count of the contributions by type and discourse.
     * @param contributionType the Contribution type
     * @param discourse the Discourse item
     * @return count
     */
    Long getCountByType(String contributionType, DiscourseItem discourse);

    /**
     * Return the ContributionItem for the specified source id.
     * @param sourceId the id
     * @return the ContributionItem
     */
    ContributionItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();
}
