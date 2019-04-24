/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.ClassDao;
import edu.cmu.pslc.datashop.item.ClassItem;

/**
 * Hibernate and Spring implementation of the ClassDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ClassDaoHibernate extends AbstractDaoHibernate implements ClassDao {

    /**
     * Standard get for a ClassItem by id.
     * @param id The id of the user.
     * @return the matching ClassItem or null if none found
     */
    public ClassItem get(Long id) {
        return (ClassItem)get(ClassItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(ClassItem.class);
    }

    /**
     * Standard find for an ClassItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ClassItem.
     * @return the matching ClassItem.
     */
    public ClassItem find(Long id) {
        return (ClassItem)find(ClassItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns ClassItem given a name.
     * @param name name of the ClassItem
     * @return a collection of ClassItems
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from ClassItem blah where blah.className = ?", name);
    }
}
