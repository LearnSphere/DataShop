/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.OliLogItem;

/**
 * Oli_Log Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 2102 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-01-10 17:00:55 -0500 (Tue, 10 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface OliLogDao extends AbstractDao {

    /**
     * Standard get for a OliLogItem by id.
     * @param id The id of the OliLogItem.
     * @return the matching OliLogItem or null if none found
     */
    OliLogItem get(String id);

    /**
     * Standard find for an OliLogItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired OliLogItem.
     * @return the matching OliLogItem.
     */
    OliLogItem find(String id);

    /**
     * Standard "find all" for OliLogItems.
     * @return a List of objects
     */
    List findAll();
}
