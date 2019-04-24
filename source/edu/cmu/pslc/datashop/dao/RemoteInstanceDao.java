/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * RemoteInstance Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface RemoteInstanceDao extends AbstractDao<RemoteInstanceItem> {

    /**
     * Standard get for a RemoteInstanceItem by id.
     * @param id The id of the RemoteInstanceItem.
     * @return the matching RemoteInstanceItem or null if none found
     */
    RemoteInstanceItem get(Long id);

    /**
     * Standard find for an RemoteInstanceItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired RemoteInstanceItem.
     * @return the matching RemoteInstanceItem.
     */
    RemoteInstanceItem find(Long id);

    /**
     * Standard "find all" for UserItems.
     * @return a List of objects
     */
    List<RemoteInstanceItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of RemoteInstanceItem objects, by name.
     *  @param name the instance name
     *  @return a list of items
     */
    List<RemoteInstanceItem> findByName(String name);
}
