/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.item.DiscourseInstanceMapId;
import edu.cmu.pslc.datashop.item.DiscourseInstanceMapItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * DiscourseRemoteInstanceMap Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscourseInstanceMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    DiscourseInstanceMapItem get(DiscourseInstanceMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    DiscourseInstanceMapItem find(DiscourseInstanceMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of DiscourseInstanceMapItems.
     *  @param discourseItem the given discourse item
     *  @return a list of items
     */
    List<DiscourseInstanceMapItem> findByDiscourse(DiscourseItem discourseItem);

    /**
     *  Return a list of DiscourseInstanceMapItems.
     *  @param instanceItem the given remote instance item
     *  @return a list of items
     */
    List<DiscourseInstanceMapItem> findByInstance(RemoteInstanceItem instanceItem);

    /**
     *  Returns whether or not the discourse is remote.
     *  @param discourse the DiscourseItem
     *  @return whether or not the discourse is remote
     */
    boolean isDiscourseRemote(DiscourseItem discourse);
}
