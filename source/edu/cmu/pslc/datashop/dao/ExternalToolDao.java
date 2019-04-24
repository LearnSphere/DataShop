/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ExternalToolItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * External Tool Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7880 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-08-22 11:00:53 -0400 (Wed, 22 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ExternalToolDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ExternalToolItem get(Integer id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ExternalToolItem find(Integer id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Find an external tool item by name and contributor.
     * @param name the name of the external tool
     * @param userItem the user item for the contributor
     * @return an item if found, null otherwise
     */
    ExternalToolItem findByNameAndContributor(String name, UserItem userItem);

    /**
     * Find a list of external tool items by contributor.
     * @param userItem the user item for the contributor
     * @return a List of item objects
     */
    List<ExternalToolItem> findByContributor(UserItem userItem);

}
