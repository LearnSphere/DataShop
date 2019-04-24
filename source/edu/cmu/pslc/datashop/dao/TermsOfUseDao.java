/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.TermsOfUseItem;

/**
 * Terms of Use Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface TermsOfUseDao extends AbstractDao {

    /**
     * Standard get for a TermsOfUseItem by id.
     * @param id The id of the TermsOfUseItem.
     * @return the matching TermsOfUseItem or null if none found
     */
    TermsOfUseItem get(Integer id);

    /**
     * Standard find for an TermsOfUseItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired TermsOfUseItem.
     * @return the matching TermsOfUseItem.
     */
    TermsOfUseItem find(Integer id);

    /**
     * Standard "find all" for TermsOfUseItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a terms of use given a name.
     * @param name name of terms of use
     * @return the matching TermsOfUseItem
     */
    TermsOfUseItem find(String name);

    /**
     * Find Terms of Use for all projects.
     *
     * @return a collection of Terms of Use items for all projects
     */
    Collection findAllProjectTermsOfUse();

}
