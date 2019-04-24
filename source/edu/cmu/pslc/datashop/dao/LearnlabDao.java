/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;

/**
 * Learnlab Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface LearnlabDao extends AbstractDao {

    /**
     * Standard get for a LearnlabItem by id.
     * @param id The id of the LearnlabItem.
     * @return the matching LearnlabItem or null if none found
     */
    LearnlabItem get(Integer id);

    /**
     * Standard find for an LearnlabItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired LearnlabItem.
     * @return the matching LearnlabItem.
     */
    LearnlabItem find(Integer id);

    /**
     * Standard "find all" for LearnlabItems.
     * @return a List of objects
     */
    List<LearnlabItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a collection of LearnlabItem given a name,
     * although it should always return one object.
     * @param name name of the LearnlabItem
     * @return a collection of LearnlabItem
     */
    LearnlabItem findByName(String name);
    /**
     * Returns a sorted list of all learnlabs.
     * @return a sorted list of all learnlabs.
     */
    List<LearnlabItem> getAll();

    /**
     * Returns a sorted list of all learnlabs given a domain.
     * @param domainItem the given domain
     * @return a sorted list of all learnlabs given a domain.
     */
    List<LearnlabItem> findByDomain(DomainItem domainItem);

    /**
     * Check if a learnlab belongs to a domain.
     * @param domainItem the given domain
     * @param learnlabItem the given learnlab
     * @return true if a learnlab does belong to a domain, false otherwise.
     */
    boolean isValidPair(DomainItem domainItem, LearnlabItem learnlabItem);

}
