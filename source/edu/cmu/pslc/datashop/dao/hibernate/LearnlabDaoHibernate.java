/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;

/**
 * Hibernate and Spring implementation of the LearnlabDao.
 *
 * @author Shanwen
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnlabDaoHibernate extends AbstractDaoHibernate implements LearnlabDao {

    /**
     * Standard get for a LearnlabItem by id.
     * @param id The id of the user.
     * @return the matching LearnlabItem or null if none found
     */
    public LearnlabItem get(Integer id) {
        return (LearnlabItem)get(LearnlabItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<LearnlabItem> findAll() {
        return findAll(LearnlabItem.class);
    }

    /**
     * Standard find for an LearnlabItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired LearnlabItem.
     * @return the matching LearnlabItem.
     */
    public LearnlabItem find(Integer id) {
        return (LearnlabItem)find(LearnlabItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns LearnlabItem given a name.
     * @param name name of the LearnlabItem
     * @return a collection of LearnlabItems
     */
    public LearnlabItem findByName(String name) {
        return (LearnlabItem)findWithQuery("from LearnlabItem learnlab where name = ?", name);
    }
    /**
     * Returns a sorted list of all learnlabs.
     * @return a sorted list of all learnlabs.
     */
    public List<LearnlabItem> getAll() {
        List<LearnlabItem> fullItems = new ArrayList<LearnlabItem>();
        List<LearnlabItem> itemList = this.findAll();
        for (LearnlabItem learnlab : itemList) {
            fullItems.add(this.get((Integer)learnlab.getId()));
        }
        Collections.sort(fullItems);
        return fullItems;
    }

    /**
     * Returns a sorted list of all learnlabs given a domain.
     * @param domainItem the given domain
     * @return a sorted list of all learnlabs given a domain.
     */
    public List<LearnlabItem> findByDomain(DomainItem domainItem) {
        return getHibernateTemplate().find(
                "SELECT learnlabs FROM DomainItem domain WHERE domain = ? ", domainItem);
    }
    /**
     * Check if a learnlab belongs to a domain.
     * @param domainItem the given domain
     * @param learnlabItem the given learnlab
     * @return true if a learnlab does belong to a domain, false otherwise.
     */
    public boolean isValidPair(DomainItem domainItem, LearnlabItem learnlabItem) {
        return (this.findByDomain(domainItem).contains(learnlabItem));
    }
}
