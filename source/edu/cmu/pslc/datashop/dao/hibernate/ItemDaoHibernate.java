/* Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

/**
 * Implements find and get methods by referring to the class returned
 * by getItemClass().  Which returns StudentItem.class for StudentDaoHibernate,
 * for example.
 *
 * @author Jim Rankin
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 *
 * @param <T> Item type
 */
public abstract class ItemDaoHibernate<T> extends AbstractDaoHibernate {
    /**
     * The class to use in find and get methods.
     * Should be a subclass of edu.cmu.pslc.datashop.item.Item.
     * @return the class to use in find and get methods
     */
    protected abstract Class<T> getItemClass();

    /**
     * Standard find for any given object by id.
     * @param id id of the object to find
     * @return the object
     */
    public T find(Long id) {
        return (T)find(getItemClass(), id);
    }

    /**
     * Standard "find all" for a type of object.
     * @return a List of objects
     */
    public List<T> findAll() {
        return findAll(getItemClass());
    }

    /**
     * Standard get for any given object by id.
     * @param id id of the object to find
     * @return the object
     */
    public T get(Long id) {
        return (T)get(getItemClass(), id);
    }
}
