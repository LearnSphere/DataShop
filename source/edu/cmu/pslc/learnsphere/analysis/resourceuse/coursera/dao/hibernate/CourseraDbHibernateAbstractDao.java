/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.hibernate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.CourseraDbAbstractDao;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * The implementation of the CRUD operations for all hibernate objects
 * for the coursera database.
 *
 * @author Hui Cheng
 * @param <T> subclass of Item that this DAO specializes on
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class CourseraDbHibernateAbstractDao<T extends Item>
        extends HibernateDaoSupport
        implements CourseraDbAbstractDao<T> {

    /**
     * Default constructor.
     */
    public CourseraDbHibernateAbstractDao() { }

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

    //
    // Hibernate utility methods.
    //

    /**
     * @author jimbokun
     * Callback for preparing an SQLQuery (set scalars, parameters, etc.) before execution.
     */
    protected interface PrepareQuery {
        /**
         * Prepare the query (set scalars, parameters, etc.) before execution.
         * @param query the query
         */
        void prepareQuery(SQLQuery query);
    }

    /**
     * Handles the "boiler plate" of calling an SQL query.
     * @param sql the query text
     * @param prepare prepares the statement for execution
     * @throws SQLException if something goes wrong running query
     */
    protected void executeSQLUpdate(String sql, PrepareQuery prepare) throws SQLException {
        Session session = getSession();

        try {
            SQLQuery query = session.createSQLQuery(sql);

            prepare.prepareQuery(query);
            query.executeUpdate();
        } finally {
            releaseSession(session);
        }
    }

    /**
     * Handles the "boiler plate" of calling an SQL query.
     * @param sql the query text
     * @throws SQLException if something goes wrong running query
     */
    protected void executeSQLUpdate(String sql) throws SQLException {
         executeSQLUpdate(sql, new PrepareQuery() {
            public void prepareQuery(SQLQuery query) { };
        });
    }

    //
    // Logging utility methods.
    //

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    protected void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if trace is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    protected void logTrace(Object... args) {
        LogUtils.logTrace(logger, args);
    }
}
