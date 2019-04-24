/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ResearcherTypeItem;

/**
 * Researcher Type Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12463 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-07-02 13:28:54 -0400 (Thu, 02 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResearcherTypeDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ResearcherTypeItem get(Integer id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ResearcherTypeItem find(Integer id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return all the researcher types by type order.
     *  @param parentId the parent id, if not null
     *  @return a list of items
     */
    List<ResearcherTypeItem> findAllInOrder(Integer parentId);

    /**
     * Get the next order value for researcher types.
     * @return 1 if no types found, the next order value of the max order found
     */
    Integer getNextOrderValue();
}
