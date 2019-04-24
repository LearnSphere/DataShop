/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.ConditionDao;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;

/**
 * Hibernate and Spring implementation of the ConditionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6363 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-14 09:52:39 -0400 (Thu, 14 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ConditionDaoHibernate extends AbstractDaoHibernate implements ConditionDao {

    /**
     * Standard get for a ConditionItem by id.
     * @param id The id of the user.
     * @return the matching ConditionItem or null if none found
     */
    public ConditionItem get(Long id) {
        return (ConditionItem)get(ConditionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(ConditionItem.class);
    }

    /**
     * Standard find for an ConditionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ConditionItem.
     * @return the matching ConditionItem.
     */
    public ConditionItem find(Long id) {
        return (ConditionItem)find(ConditionItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns Condition(s) given a name.
     * @param name name of condition
     * @return Collection of conditionItem
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from ConditionItem condition where condition.conditionName = ?", name);
    }

    /**
     * Gets a list of conditions in the dataset who's Name match all
     * or a portion of the toMatch parameter.
     * @param toMatch A string to match.
     * @param dataset the dataset item to search in.
     * @param matchAny boolean value indicating whether to only look for matches from
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching items sorted by Name.
     */
    public List findMatchingByName(String toMatch, DatasetItem dataset, boolean matchAny) {
        DetachedCriteria query = DetachedCriteria.forClass(ConditionItem.class);

        if (matchAny) {
            query.add(Restrictions.ilike("conditionName", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("conditionName", toMatch, MatchMode.START));
        }
        query.add(Restrictions.eq("dataset", dataset));
        query.addOrder(Property.forName("conditionName").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets a list of conditions in the dataset who's Type match all
     * or a portion of the toMatch parameter.
     * @param toMatch A string to match.
     * @param dataset the dataset item to search in.
     * @param matchAny boolean value indicating whether to only look for matches from
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching items sorted by Type.
     */
    public List findMatchingByType(String toMatch, DatasetItem dataset, boolean matchAny) {
        DetachedCriteria query = DetachedCriteria.forClass(ConditionItem.class);
        if (matchAny) {
            query.add(Restrictions.ilike("type", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("type", toMatch, MatchMode.START));
        }
        query.add(Restrictions.eq("dataset", dataset));
        query.addOrder(Property.forName("type").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets a list of conditions in the dataset.
     * @param dataset the dataset item to search in.
     * @return List of all matching items.
     */
    public List find(DatasetItem dataset) {
        String query = "select distinct con from ConditionItem con "
            + "join con.dataset dat "
            + "where dat.id = " + dataset.getId();

        return getHibernateTemplate().find(query);
    }

    /**
     * Return a saved item.  If the new item is not in the list, then save it.
     * If it is, then find the existing item and return that.
     * Note that this method is overridden because it doesn't use the item's equals method.
     * Conditions must be unique by dataset and name.  Disregard type and description.
     * @param collection the collection to search
     * @param newItem the new item
     * @return an existing item
     */
    public Item findOrCreate(Collection collection, Item newItem)  {
        boolean found = false;

        ConditionItem newCondition = (ConditionItem)newItem;

        for (Object existingItem : collection) {
            ConditionItem existingCondition = (ConditionItem)existingItem;

            //check only the fields we care about, dataset and name, not type and description
            if (Item.objectEqualsFK(existingCondition.getDataset(), newCondition.getDataset())
                    && Item.objectEquals(existingCondition.getConditionName(),
                                         newCondition.getConditionName())) {
                found = true;
                newCondition = existingCondition;
                break;
            }
        }

        if (!found) {
            if (logger.isDebugEnabled()) {
                logger.debug("findOrCreate: creating new item: " + newCondition);
                logger.debug("findOrCreate: as its not found in collection of size: "
                        + collection.size());
            }
            saveOrUpdate(newCondition);
            newItem = newCondition;
        } else {
            newItem = newCondition;
        }
        return newItem;
    }
}
