/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseHistoryItem;

/**
 * User Terms of Use History Data Access Object Interface.
 *
 * @author Shanwen Yu
 * @version $Revision: 7339 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2011-11-22 16:33:49 -0500 (Tue, 22 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface UserTermsOfUseHistoryDao extends AbstractDao {

    /**
     * Standard get for a UserTermsOfUseHistoryItem by id.
     * @param id The id of the UserTermsOfUseHistoryItem.
     * @return the matching UserTermsOfUseHistoryItem or null if none found
     */
    UserTermsOfUseHistoryItem get(Integer id);

    /**
     * Standard find for an UserTermsOfUseHistoryItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired UserTermsOfUseHistoryItem.
     * @return the matching UserTermsOfUseHistoryItem.
     */
    UserTermsOfUseHistoryItem find(Integer id);

    /**
     * Standard "find all" for UserTermsOfUseHistoryItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a user terms of use history given a terms of use version.
     * @param termsOfUseVersion terms of use version item
     * @return a collection of items
     */
    Collection findByTermsOfUseVersion(TermsOfUseVersionItem termsOfUseVersion);

    /**
     * Returns a user terms of use history given a terms of use version.
     * @param user user that is associated with this history
     * @return a collection of items
     */
    Collection findByUser(UserItem user);

}
