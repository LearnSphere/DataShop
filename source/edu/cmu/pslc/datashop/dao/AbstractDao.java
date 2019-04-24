/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;


/**
 * The interface of the CRUD operations for all hibernate objects.
 *
 * @author Alida Skogsholm
 * @param <T> subclass of Item that this DAO specializes on
 * @version $Revision: 6049 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2010-04-23 12:16:53 -0400 (Fri, 23 Apr 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AbstractDao<T extends Item> {
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
    T find(Class<T> clazz, Serializable id);

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

    /**
     * Return a saved item.  If the new item is not in the list, then save it.
     * If it is, then find the existing item and return that.
     * @param list the list to search
     * @param newItem the new item
     * @return an existing item
     */
    T findOrCreate(Collection<T> list, T newItem);

    /**
     * Drop stored procedures and functions from the database after using them.
     * @param procsToDrop the procedures to drop.
     * @param funcsToDrop the functions to drop.
     * @return true if successful, false if an SQL Exception occurred.
     */
    boolean dropStoredProcedures(List<String> procsToDrop, List<String> funcsToDrop);

    /**
     * Drop tables from the database after using them.
     * @param tablesToDrop the names of the tables to drop
     */
    void dropTables(List<String> tablesToDrop);
}
