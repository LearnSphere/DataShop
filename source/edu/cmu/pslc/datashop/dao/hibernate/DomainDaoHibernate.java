/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.item.DomainItem;

/**
 * Hibernate and Spring implementation of the DomainDao.
 *
 * @author Shanwen
 * @version $Revision: 6823 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2011-04-06 16:45:37 -0400 (Wed, 06 Apr 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DomainDaoHibernate extends AbstractDaoHibernate implements DomainDao {

    /**
     * Standard get for a DomainItem by id.
     * @param id The id of the user.
     * @return the matching DomainItem or null if none found
     */
    public DomainItem get(Integer id) {
        return (DomainItem)get(DomainItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DomainItem> findAll() {
        return findAll(DomainItem.class);
    }

    /**
     * Standard find for an DomainItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DomainItem.
     * @return the matching DomainItem.
     */
    public DomainItem find(Integer id) {
        return (DomainItem)find(DomainItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns DomainItem given a name.
     * @param name name of the DomainItem
     * @return a collection of DomainItems
     */
    public DomainItem findByName(String name) {
        return (DomainItem)findWithQuery("from DomainItem domain where name = ?", name);
    }

    /**
     * Returns a sorted list of all domains.
     * @return a sorted list of all domains.
     */
    public List<DomainItem> getAll() {
        List<DomainItem> fullItems = new ArrayList<DomainItem>();
        List<DomainItem> itemList = this.findAll();
        for (DomainItem domain : itemList) {
            fullItems.add(this.get((Integer)domain.getId()));
        }
        Collections.sort(fullItems);
        return fullItems;
    };

}
