/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao.hibernate;

import java.io.Serializable;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.cmu.pslc.datashop.oli.dao.OliAbstractDao;

/**
 * The implementation of the CRUD operations for all hibernate objects.
 *
 * @author Alida Skogsholm
 * @version $Revision: 5736 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2009-09-21 10:22:17 -0400 (Mon, 21 Sep 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class OliHibernateAbstractDao
        extends HibernateDaoSupport implements OliAbstractDao {

    /**
     * Default constructor.
     */
    public OliHibernateAbstractDao() { }

    /**
     * Standard get for any given object by id. <br>Returns an object with all
     * fields instantiated.  Will return null if the object is not found.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    public Object get(Class clazz, Serializable id) {
        return getHibernateTemplate().get(clazz, id);
    }

    /**
     * Standard find for any given object by id. <br><br>
     * This find will return a proxy of the object with the id field filled in.
     * The fields will throw an org.hibernate.LazyInitilizationException
     * if accessed outside of a session.  If an object with the given
     * id does not exist a org.hibernate.ObjectNotFoundException will be
     * thrown when the fields of the proxy are accessed.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    public Object find(Class clazz, Serializable id) {
        return getHibernateTemplate().load(clazz, id);
    }

    /**
     * Standard "find all" for a type of object.
     * @param clazz type of object
     * @return a List of objects
     */
    public List findAll(Class clazz) {
        return getHibernateTemplate().loadAll(clazz);
    }
}
