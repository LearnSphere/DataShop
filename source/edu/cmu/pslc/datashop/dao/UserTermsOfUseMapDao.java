/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapItem;

/**
 * User Terms Of Use Map Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 7294 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-11-17 14:47:29 -0500 (Thu, 17 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface UserTermsOfUseMapDao extends AbstractDao {

    /**
     * Standard get for a UserTermsOfUseMapItem by id.
     * @param id The id of the UserTermsOfUseMapItem.
     * @return the matching UserTermsOfUseMapItem or null if none found
     */
    UserTermsOfUseMapItem get(UserTermsOfUseMapId id);

    /**
     * Standard find for an UserTermsOfUseMapItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired UserTermsOfUseMapItem.
     * @return the matching UserTermsOfUseMapItem.
     */
    UserTermsOfUseMapItem find(UserTermsOfUseMapId id);

    /**
     * Standard "find all" for UserTermsOfUseMapItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Get the item for the given username and version item.
     * @param username the account id
     * @param versionItem the terms of use version to check
     * @return the item if found, null otherwise
     */
    UserTermsOfUseMapItem findByUserAndVersion(
            String username, TermsOfUseVersionItem versionItem);

    /**
     * Get the item for the given username and version item.
     * @param username the account id
     * @param termsItem the terms of use to check
     * @return the item if found, null otherwise
     */
    boolean hasAgreedBefore(String username, TermsOfUseItem termsItem);

} // end UserTermsOfUseMapDao.java
