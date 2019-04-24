/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.PaperItem;

/**
 * Paper Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10833 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-24 13:40:24 -0400 (Mon, 24 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface PaperDao extends AbstractDao {

    /**
     * Standard get for a PaperItem by id.
     * @param id The id of the PaperItem.
     * @return the matching PaperItem or null if none found
     */
    PaperItem get(Integer id);

    /**
     * Standard find for an PaperItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired PaperItem.
     * @return the matching PaperItem.
     */
    PaperItem find(Integer id);

    /**
     * Standard "find all" for PaperItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a paper given a name.
     * @param name name of paper
     * @return a collection of items
     */
    Collection find(String name);

}
