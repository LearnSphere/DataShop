/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao;

import java.io.Serializable;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;

/**
 * The interface of the CRUD operations for all hibernate objects
 * in the coursera database.
 *
 * @author Hui Cheng
 * @param <T> subclass of Item that this DAO specializes on
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CourseraDbAbstractDao<T extends Item> {

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
