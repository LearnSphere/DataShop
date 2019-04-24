/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.ContributionItem;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionDiscoursePartMapId;
import edu.cmu.pslc.datashop.discoursedb.item.ContributionDiscoursePartMapItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;

/**
 * ContributionDiscoursePartMap Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ContributionDiscoursePartMapDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a ContributionDiscoursePartMapItem by id.
     * @param id The id of the ContributionDiscoursePartMapItem.
     * @return the matching ContributionDiscoursePartMapItem or null if none found
     */
    ContributionDiscoursePartMapItem get(ContributionDiscoursePartMapId id);

    /**
     * Standard find for an ContributionDiscoursePartMapItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ContributionDiscoursePartMapItem.
     * @return the matching ContributionDiscoursePartMapItem.
     */
    ContributionDiscoursePartMapItem find(ContributionDiscoursePartMapId id);

    /**
     * Standard "find all" for ContributionDiscoursePartMapItem.
     * @return a List of objects
     */
    List<ContributionDiscoursePartMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of the ContributionDiscoursePartMap items, starting at
     * the specified offset, limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<ContributionDiscoursePartMapItem> the batch of items
     */
    List<ContributionDiscoursePartMapItem> getContributionDiscoursePartMap(int offset, int max);

    /**
     * Return a list of ContributionDiscoursePartMapItem objects given a DataSourcesItem.
     * @param part the DiscoursePart item
     * @return a list of ContributionDiscoursePartMapItem objects
     */
    List<ContributionDiscoursePartMapItem> findByDiscoursePart(DiscoursePartItem part);

    /**
     * Return a list of ContributionDiscoursePartMapItem objects given a DataSourcesItem.
     * @param contribution the DiscourseDB contribution item
     * @return a list of ContributionDiscoursePartMapItem objects
     */
    List<ContributionDiscoursePartMapItem> findByContribution(ContributionItem user);
}
