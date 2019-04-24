/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.io.Serializable;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;

/**
 * The interface of the CRUD operations for all hibernate objects
 * in the discoursedb database.
 *
 * @author Cindy Tipper
 * @param <T> subclass of Item that this DAO specializes on
 * @version $Revision: 12724 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-11-05 13:30:26 -0500 (Thu, 05 Nov 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscourseDbAbstractDao<T extends Item> {

    /**
     * Standard save or update for any given object.
     * @param obj object to save
     */
    void saveOrUpdate(T obj);

    /**
     * Standard delete for any given object.
     * @param obj the object to delete
     */
    void delete(T obj);

    /**
     * Standard find for any given object by id.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    Object find(Class<T> clazz, Serializable id);

    /**
     * Standard "find all" for a type of object.
     * @param clazz type of object
     * @return a List of objects
     */
    List<T> findAll(Class<T> clazz);

    /**
     * Standard get for any given object by id.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    T get(Class<T> clazz, Serializable id);
}
