/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.InstructorDao;
import edu.cmu.pslc.datashop.item.InstructorItem;

/**
 * Hibernate and Spring implementation of the InstructorDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InstructorDaoHibernate extends AbstractDaoHibernate implements InstructorDao {

    /**
     * Standard get for a InstructorItem by id.
     * @param id The id of the user.
     * @return the matching InstructorItem or null if none found
     */
    public InstructorItem get(Long id) {
        return (InstructorItem)get(InstructorItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(InstructorItem.class);
    }

    /**
     * Standard find for an InstructorItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired InstructorItem.
     * @return the matching InstructorItem.
     */
    public InstructorItem find(Long id) {
        return (InstructorItem)find(InstructorItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns InstructorItem given a name.
     * @param name name of the InstructorItem
     * @return a collection of InstructorItems
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from InstructorItem instructor where instructor.instructorName = ?", name);
    }
}
