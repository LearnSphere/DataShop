/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscoursePartItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseDiscoursePartMapId;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseDiscoursePartMapItem;

/**
 * DiscourseDiscoursePartMap Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscourseDiscoursePartMapDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DiscourseDiscoursePartMapItem by id.
     * @param id The id of the DiscourseDiscoursePartMapItem.
     * @return the matching DiscourseDiscoursePartMapItem or null if none found
     */
    DiscourseDiscoursePartMapItem get(DiscourseDiscoursePartMapId id);

    /**
     * Standard find for an DiscourseDiscoursePartMapItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DiscourseDiscoursePartMapItem.
     * @return the matching DiscourseDiscoursePartMapItem.
     */
    DiscourseDiscoursePartMapItem find(DiscourseDiscoursePartMapId id);

    /**
     * Standard "find all" for DiscourseDiscoursePartMapItem.
     * @return a List of objects
     */
    List<DiscourseDiscoursePartMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of DiscourseDiscoursePartMapItem objects given a DataSourcesItem.
     * @param part the DiscoursePart item
     * @return a list of DiscourseDiscoursePartMapItem objects
     */
    List<DiscourseDiscoursePartMapItem> findByDiscoursePart(DiscoursePartItem part);

    /**
     * Return a list of DiscourseDiscoursePartMapItem objects given a DataSourcesItem.
     * @param discourse the DiscourseDB discourse item
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return a list of DiscourseDiscoursePartMapItem objects
     */
    List<DiscourseDiscoursePartMapItem> findByDiscourse(DiscourseItem user, int offset, int max);
}
