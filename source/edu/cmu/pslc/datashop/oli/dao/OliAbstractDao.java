/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao;

import java.io.Serializable;
import java.util.List;


/**
 * The interface of the CRUD operations for all hibernate objects
 * in the OLI database.  Right now only accessing the Log database.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2050 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2005-12-23 12:03:11 -0500 (Fri, 23 Dec 2005) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface OliAbstractDao {

    /**
     * Standard find for any given object by id.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    Object find(Class clazz, Serializable id);

    /**
     * Standard "find all" for a type of object.
     * @param clazz type of object
     * @return a List of objects
     */
    List findAll(Class clazz);

    /**
     * Standard get for any given object by id.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    Object get(Class clazz, Serializable id);
}
