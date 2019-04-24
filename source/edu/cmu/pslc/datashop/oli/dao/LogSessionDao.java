/*
 * Carnegie Mellon University, Human Computer InterLogSession Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao;

import java.util.List;

import edu.cmu.pslc.datashop.oli.item.LogSessionItem;

/**
 * LogSession Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 2051 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2005-12-23 14:45:04 -0500 (Fri, 23 Dec 2005) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface LogSessionDao extends OliAbstractDao {

    /**
     * Standard get for a LogSessionItem by id.
     * @param id The id of the LogSessionItem.
     * @return the matching LogSessionItem or null if none found
     */
    LogSessionItem get(String id);

    /**
     * Standard find for an LogSessionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired LogSessionItem.
     * @return the matching LogSessionItem.
     */
    LogSessionItem find(String id);

    /**
     * Standard "find all" for LogSessionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Get the distinct user/session pairs.
     * @return a list of UserSession objects
     */
    List getDistinctUserSessions();
}
