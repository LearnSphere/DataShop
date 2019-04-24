/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;

/**
 * TermsOfUseVersion Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface TermsOfUseVersionDao extends AbstractDao {

    /**
     * Standard get for a TermsOfUseVersionItem by id.
     * @param id The id of the TermsOfUseVersionItem.
     * @return the matching TermsOfUseVersionItem or null if none found
     */
    TermsOfUseVersionItem get(Integer id);

    /**
     * Standard find for an TermsOfUseVersionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired TermsOfUseVersionItem.
     * @return the matching TermsOfUseVersionItem.
     */
    TermsOfUseVersionItem find(Integer id);

    /**
     * Standard "find all" for TermsOfUseVersionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //


    /**
     * Returns a list of terms of use version items by terms of use item.
     * @param touItem the terms of use to which the versions are associated
     * @return a list of terms of use version items
     */
    Collection findAllByTermsOfUse(TermsOfUseItem touItem);

    /**
     * Returns the terms of use versions as a list given a terms of use item and the status.
     * @param touItem the terms of use to which the versions are associated
     * @param status the string status of items to return
     * @return a list of terms of use versions matching the status
     */
    Collection findVersionsByTermsAndStatus(TermsOfUseItem touItem, String status);

    /**
     * Get the current terms of use if given version is null, return the
     * given version or null if version is invalid.
     * @param version the version of the terms to return
     * @return current terms if version is null, the given version if not, null if not found
     */
    TermsOfUseVersionItem getDataShopTerms(Integer version);

    /**
     * Get the current terms of use if given version is null, return the
     * given version or null if version is invalid.
     * @param projectId the project id
     * @param version the version of the terms to return
     * @return current terms if version is null, the given version if not, null if not found
     */
    TermsOfUseVersionItem getProjectTerms(Integer projectId, Integer version);

    /**
     * Returns the applied terms of use version for a given terms of use name.
     * @param touName the terms of use item name
     * @return The applied terms of use version or null if none found
     */
    TermsOfUseVersionItem findAppliedVersion(String touName);

    /**
     * Returns a boolean that says whether or not
     * this terms of use has a specified status.
     * @param touItem the terms of use to which the changes are applied
     * @param status the status
     * @return Whether or not the terms has the specified status
     */
    Boolean hasStatus(TermsOfUseItem touItem, String status);

    /**
     * Returns the head version of the specified terms of use.
     * @param touName the unique terms of use name
     * @return The head version of the specified terms of use
     */
    TermsOfUseVersionItem findLastVersion(String touName);

}
