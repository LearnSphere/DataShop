/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.UserTermsOfUseHistoryDao;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseHistoryItem;

/**
 * Hibernate and Spring implementation of the UserTermsOfUseHistoryDao.
 *
 * @author Shanwen Yu
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserTermsOfUseHistoryDaoHibernate
    extends AbstractDaoHibernate implements UserTermsOfUseHistoryDao {

    /**
     * Standard get for a UserTermsOfUseHistoryItem by id.
     * @param id The id of the user.
     * @return the matching UserTermsOfUseHistoryItem or null if none found
     */
    public UserTermsOfUseHistoryItem get(Integer id) {
        return (UserTermsOfUseHistoryItem)get(UserTermsOfUseHistoryItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(UserTermsOfUseHistoryItem.class);
    }

    /**
     * Standard find for an UserTermsOfUseHistoryItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired UserTermsOfUseHistoryItem.
     * @return the matching UserTermsOfUseHistoryItem.
     */
    public UserTermsOfUseHistoryItem find(Integer id) {
        return (UserTermsOfUseHistoryItem)find(UserTermsOfUseHistoryItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a user terms of use history given a terms of use version.
     * @param termsOfUseVersion terms of use version item
     * @return a collection of items
     */
    public Collection findByTermsOfUseVersion(TermsOfUseVersionItem termsOfUseVersion) {
        return getHibernateTemplate().find(
                "from UserTermsOfUseHistoryItem UserTermsOfUseHistory"
                + " where termsOfUseVersion.id = ?", termsOfUseVersion.getId());
    }

    /**
     * Returns a user terms of use history given a terms of use version.
     * @param user user that is associated with this history
     * @return a collection of items
     */
    public Collection findByUser(UserItem user) {
        return getHibernateTemplate().find(
                "from UserTermsOfUseHistoryItem UserTermsOfUseHistory"
                + " where user.id = ?", user.getId());
    }

}
