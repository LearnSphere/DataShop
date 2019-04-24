/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;

/**
 * Hibernate and Spring implementation of the TermsOfUseDao.
 *
 * @author Shanwen Yu
 * @version $Revision: 7397 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2011-12-07 15:37:40 -0500 (Wed, 07 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TermsOfUseDaoHibernate extends AbstractDaoHibernate implements TermsOfUseDao {

    /**
     * Standard get for a TermsOfUseItem by id.
     * @param id The id of the user.
     * @return the matching TermsOfUseItem or null if none found
     */
    public TermsOfUseItem get(Integer id) {
        return (TermsOfUseItem)get(TermsOfUseItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(TermsOfUseItem.class);
    }

    /**
     * Standard find for an TermsOfUseItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired TermsOfUseItem.
     * @return the matching TermsOfUseItem.
     */
    public TermsOfUseItem find(Integer id) {
        return (TermsOfUseItem)find(TermsOfUseItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a terms of use given a name.
     * @param name name of terms of use
     * @return the matching TermsOfUseItem
     */
    public TermsOfUseItem find(String name) {
        List<TermsOfUseItem> list = getHibernateTemplate().find(
                "from TermsOfUseItem TermsOfUse where TermsOfUse.name = ?", name);

        if (list == null || list.isEmpty()) {
            return null;
        }

        return (TermsOfUseItem) list.get(0);
    }

    /**
     * Find Terms of Use for all projects.
     *
     * @return a collection of Terms of Use items for all projects
     */
    public List findAllProjectTermsOfUse() {
        String query = "from TermsOfUseItem tou "
                     + "where tou.retiredFlag = false "
                     + "and tou.name <> ? "
                     + "order by tou.name";
        return getHibernateTemplate().find(query, TermsOfUseItem.DATASHOP_TERMS);
    }
}
