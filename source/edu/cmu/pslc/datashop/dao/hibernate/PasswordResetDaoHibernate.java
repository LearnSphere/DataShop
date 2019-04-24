/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.PasswordResetDao;
import edu.cmu.pslc.datashop.item.PasswordResetItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the PasswordResetDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7571 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-04-02 15:28:19 -0400 (Mon, 02 Apr 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PasswordResetDaoHibernate extends AbstractDaoHibernate implements PasswordResetDao {

    /**
     * Standard get for a PasswordResetItem by id.
     * @param id The id of the user.
     * @return the matching PasswordResetItem or null if none found
     */
    public PasswordResetItem get(String id) {
        return (PasswordResetItem)get(PasswordResetItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(PasswordResetItem.class);
    }

    /**
     * Standard find for an PasswordResetItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired PasswordResetItem.
     * @return the matching PasswordResetItem.
     */
    public PasswordResetItem find(String id) {
        return (PasswordResetItem)find(PasswordResetItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a collection of items given a user.
     * @param user the user who made the request
     * @return a collection of items
     */
    public Collection find(UserItem user) {
        return getHibernateTemplate().find(
                "from PasswordResetItem item where item.user = ?", user);
    }
}
