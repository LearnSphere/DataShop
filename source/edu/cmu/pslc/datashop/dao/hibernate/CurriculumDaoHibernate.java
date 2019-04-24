/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.CurriculumDao;
import edu.cmu.pslc.datashop.item.CurriculumItem;

/**
 * Hibernate and Spring implementation of the CurriculumDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6479 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2010-12-02 16:37:19 -0500 (Thu, 02 Dec 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CurriculumDaoHibernate extends AbstractDaoHibernate implements CurriculumDao {

    /**
     * Standard get for a CurriculumItem by id.
     * @param id The id of the user.
     * @return the matching CurriculumItem or null if none found
     */
    public CurriculumItem get(Integer id) {
        return (CurriculumItem)get(CurriculumItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(CurriculumItem.class);
    }

    /**
     * Standard find for an CurriculumItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired CurriculumItem.
     * @return the matching CurriculumItem.
     */
    public CurriculumItem find(Integer id) {
        return (CurriculumItem)find(CurriculumItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns CurriculumItem given a name.
     * @param name name of the CurriculumItem
     * @return a collection of CurriculumItems
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from CurriculumItem curriculum where curriculum.curriculumName = ?", name);
    }
}
