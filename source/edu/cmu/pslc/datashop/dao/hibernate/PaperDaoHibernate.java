/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.PaperDao;
import edu.cmu.pslc.datashop.item.PaperItem;

/**
 * Hibernate and Spring implementation of the PaperDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10833 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-24 13:40:24 -0400 (Mon, 24 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PaperDaoHibernate extends AbstractDaoHibernate implements PaperDao {

    /**
     * Standard get for a PaperItem by id.
     * @param id The id of the user.
     * @return the matching PaperItem or null if none found
     */
    public PaperItem get(Integer id) {
        return (PaperItem)get(PaperItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(PaperItem.class);
    }

    /**
     * Standard find for an PaperItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired PaperItem.
     * @return the matching PaperItem.
     */
    public PaperItem find(Integer id) {
        return (PaperItem)find(PaperItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a paper given a title.
     * @param title title of paper
     * @return a collection of items
     */
    public Collection find(String title) {
        return getHibernateTemplate().find(
                "from PaperItem paper where paper.title = ?", title);
    }

}
