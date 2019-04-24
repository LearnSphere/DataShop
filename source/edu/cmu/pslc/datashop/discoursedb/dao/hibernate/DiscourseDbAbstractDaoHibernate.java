/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbAbstractDao;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static java.util.Arrays.asList;

/**
 * The implementation of the CRUD operations for all hibernate objects
 * for the discourse database.
 *
 * @author Cindy Tipper
 * @param <T> subclass of Item that this DAO specializes on
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class DiscourseDbAbstractDaoHibernate<T extends Item>
    extends HibernateDaoSupport
    implements DiscourseDbAbstractDao<T> {

    /**
     * Default constructor.
     */
    public DiscourseDbAbstractDaoHibernate() { }

    /**
     * Standard save or update for any given object.
     * @param obj object to save
     */
    public void saveOrUpdate(T obj) {
        getHibernateTemplate().saveOrUpdate(obj);
    }

    /**
     * Standard delete for any given object.
     * @param obj the object to delete
     */
    public void delete(T obj) {
        getHibernateTemplate().delete(obj);
    }

    /**
     * Standard get for any given object by id. <br>Returns an object with all
     * fields instantiated.  Will return null if the object is not found.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    public T get(Class<T> clazz, Serializable id) {
        return (T)getHibernateTemplate().get(clazz, id);
    }

    /**
     * Standard find for any given object by id. <br><br>
     * This find will return a proxy of the object with the id field filled in.
     * The fields will throw an Hibernate LazyInitilizationException
     * if accessed outside of a session.  If an object with the given
     * id does not exist a Hibernate ObjectNotFoundException will be
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

    /**
     * Build the SQL for calling stored procedure with the specified arguments.
     * @param procedureName the stored procedure's name
     * @param args the arguments to call the procedure with
     * @return String containing the SQL for calling stored procedure procedureName with
     */
    protected String buildSPCall(String procedureName, Object... args) {
        return "call " + procedureName + "(" + join(", ", asList(args)) + ")";
    }

    //
    // Hibernate utility methods.
    //

    /**
     * Helper class to create a HibernateCallback with specific values set.
     */
    protected class CallbackCreatorHelper {

        /** The HQL query as a String. */
        private final String queryString;
        /** The offset is the first value returned. */
        private final int offset;
        /** The limit is the total number of items to return. */
        private final int limit;

        /**
         * Public constructor which sets the values at finals.
         * @param queryString HQL query as a string.
         * @param offset index of the first value to return.
         * @param limit total number of items to return.
         */
        public CallbackCreatorHelper(String queryString, int offset, int limit) {
            this.queryString = queryString;
            this.offset = offset;
            this.limit = limit;
        }

        /**
         * Create the HibernateCallback with the queryString, limit, and offset.
         * @return a HibernateCallback.
         */
        public HibernateCallback getCallback() {
            return new HibernateCallback() {
                public Object doInHibernate(Session session)
                    throws java.sql.SQLException {
                        org.hibernate.Query query = session.createQuery(queryString);
                        query.setFirstResult(offset);
                        query.setMaxResults(limit);
                       return query.list();
                }
            };
        }
    }
}
