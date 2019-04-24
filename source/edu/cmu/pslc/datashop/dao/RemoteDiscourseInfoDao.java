/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.item.RemoteDiscourseInfoItem;

/**
 * RemoteDiscourseInfo Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface RemoteDiscourseInfoDao extends AbstractDao<RemoteDiscourseInfoItem> {

    /**
     * Standard get for a RemoteDiscourseInfoItem by id.
     * @param id The id of the RemoteDiscourseInfoItem.
     * @return the matching RemoteDiscourseInfoItem or null if none found
     */
    RemoteDiscourseInfoItem get(Long id);

    /**
     * Standard find for an RemoteDiscourseInfoItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired RemoteDiscourseInfoItem.
     * @return the matching RemoteDiscourseInfoItem.
     */
    RemoteDiscourseInfoItem find(Long id);

    /**
     * Standard "find all" for UserItems.
     * @return a List of objects
     */
    List<RemoteDiscourseInfoItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of RemoteDiscourseInfoItem objects, by discourse.
     *  @param discourse the DiscourseItem
     *  @return a list of items
     */
    List<RemoteDiscourseInfoItem> findByDiscourse(DiscourseItem discourse);
}
