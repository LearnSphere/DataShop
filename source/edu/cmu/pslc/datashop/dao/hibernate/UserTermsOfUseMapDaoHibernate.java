/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.UserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapItem;

/**
 * Hibernate and Spring implementation of the UserTermsOfUseMapDao.
 *
 * @author Shanwen Yu
 * @version $Revision: 7294 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-11-17 14:47:29 -0500 (Thu, 17 Nov 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserTermsOfUseMapDaoHibernate extends AbstractDaoHibernate
    implements UserTermsOfUseMapDao {

    /**
     * Standard get for a UserTermsOfUseMapItem by id.
     * @param id The id of the UserTermsOfUseMapItem.
     * @return the matching UserTermsOfUseMapItem or null if none found
     */
    public UserTermsOfUseMapItem get(UserTermsOfUseMapId id) {
        return (UserTermsOfUseMapItem)get(UserTermsOfUseMapItem.class, id);
    }

    /**
     * Standard find for an UserTermsOfUseMapItem.
     * Only the id of the item will be filled in.
     * @param id the UserTermsOfUseMapId for this item.
     * @return the matching UserTermsOfUseMapItem.
     */
    public UserTermsOfUseMapItem find(UserTermsOfUseMapId id) {
        return (UserTermsOfUseMapItem)find(UserTermsOfUseMapItem.class, id);
    }

    /**
     * Standard "find all" for UserTermsOfUseMapItem items.
     * @return a List of objects
     */
    public List<UserTermsOfUseMapItem> findAll() {
        return findAll(UserTermsOfUseMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByUserAndVersion method. */
    private static final String FIND_BY_USER_AND_VERSION_QUERY =
            "from UserTermsOfUseMapItem map "
          + "where user.id = ? "
          + "and termsOfUseVersion = ?";

    /**
     * Get the item for the given username and version item.
     * @param username the account id
     * @param versionItem the terms of use version to check
     * @return the item if found, null otherwise
     */
    public UserTermsOfUseMapItem findByUserAndVersion(
            String username, TermsOfUseVersionItem versionItem) {

        //pre-condition, inputs cannot be null
        if (username == null || versionItem == null) {
            return null;
        }

        UserTermsOfUseMapItem foundItem = null;

        Object[] params = {username, versionItem};
        List<UserTermsOfUseMapItem> itemList =
                getHibernateTemplate().find(FIND_BY_USER_AND_VERSION_QUERY, params);
        int numItems = itemList.size();
        if (numItems >= 1) {
            foundItem = itemList.get(0);
            if (numItems > 1) {
                logger.warn("More than one row found: " + numItems);
            }
        }

        return foundItem;
    }

    /** HQL query for the hasAgreedBefore method. */
    private static final String HAS_AGREED_BEFORE_QUERY =
            "from UserTermsOfUseMapItem map "
          + "where user.id = ? "
          + "and termsOfUse = ?";

    /**
     * Get the item for the given username and version item.
     * @param username the account id
     * @param termsItem the terms of use to check
     * @return the item if found, null otherwise
     */
    public boolean hasAgreedBefore(String username, TermsOfUseItem termsItem) {

        //pre-condition, inputs cannot be null
        if (username == null || termsItem == null) {
            return false;
        }

        boolean agreedBefore = false;

        Object[] params = {username, termsItem};
        List<UserTermsOfUseMapItem> itemList =
                getHibernateTemplate().find(HAS_AGREED_BEFORE_QUERY, params);
        if (itemList.size() >= 1) {
            agreedBefore = true;
        }

        return agreedBefore;
    }

} // end UserTermsOfUseMapDaoHibernate.java
