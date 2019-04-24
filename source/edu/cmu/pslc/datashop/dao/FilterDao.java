/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * Filter Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4093 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2007-05-21 13:10:24 -0400 (Mon, 21 May 2007) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface FilterDao extends AbstractDao {

    /**
     * Standard get for a FilterItem by id.
     * @param id The id of the FilterItem.
     * @return the matching FilterItem or null if none found
     */
    FilterItem get(Integer id);

    /**
     * Standard find for an FilterItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired FilterItem.
     * @return the matching FilterItem.
     */
    FilterItem find(Integer id);

    /**
     * Standard "find all" for FilterItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Get all filters for a given sample.
     * @param sample the sample to get filters for.
     * @return a List of FilterItems
     */
    List find(SampleItem sample);
}
