/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DomainItem;

/**
 * Domain Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 6308 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-09-15 14:43:21 -0400 (Wed, 15 Sep 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DomainDao extends AbstractDao {

    /**
     * Standard get for a DomainItem by id.
     * @param id The id of the DomainItem.
     * @return the matching DomainItem or null if none found
     */
    DomainItem get(Integer id);

    /**
     * Standard find for a DomainItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DomainItem.
     * @return the matching DomainItem.
     */
    DomainItem find(Integer id);

    /**
     * Standard "find all" for DomainItems.
     * @return a List of objects
     */
    List<DomainItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns DomainItem given a name.
     * @param name name of the DomainItem
     * @return a domain that the dataset falls under
     */
    DomainItem findByName(String name);

    /**
     * Returns a sorted list of all domains.
     * @return a sorted list of all domains.
     */
    List<DomainItem> getAll();
}
