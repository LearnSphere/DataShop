/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static edu.cmu.pslc.datashop.util.CollectionUtils.keyValues;
import static edu.cmu.pslc.datashop.util.FileUtils.openAndReplaceSequence;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.cmu.pslc.datashop.dao.AbstractDao;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.CollectionUtils.KeyValue;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * The implementation of the CRUD operations for all hibernate objects.
 *
 * @author Alida Skogsholm
 * @param <T> subclass of Item that this DAO specializes on
 * @version $Revision: 11230 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-20 15:15:07 -0400 (Fri, 20 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractDaoHibernate<T extends Item> extends HibernateDaoSupport
implements AbstractDao<T> {
    /** Batch size for a transaction flush. */
    protected static final int BATCH_SIZE = 5000;

    /** Formatter to interpret time field in the format "yyyy-MM-dd HH:mm:ss". */
    private static DateFormat dateFmtStd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** String to replace when customizing functions/procedures for a sample. */
    public static final String TO_REPLACE_STRING = "XXX";

    /**
     * Default constructor.
     */
    public AbstractDaoHibernate() { }

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
     * The fields will throw a hibernate LazyInitilizationException
     * if accessed outside of a session.  If an object with the given
     * id does not exist a hibernate ObjectNotFoundException will be
     * thrown when the fields of the proxy are accessed.
     * @param clazz type of object
     * @param id id of the object to find
     * @return the object
     */
    public T find(Class<T> clazz, Serializable id) {
        return (T)getHibernateTemplate().load(clazz, id);
    }

    /**
     * Standard "find all" for a type of object.
     * @param clazz type of object
     * @return a List of objects
     */
    public List<T> findAll(Class<T> clazz) {
        return getHibernateTemplate().loadAll(clazz);
    }

    /**
     * Return a saved item.  If the new item is not in the list, then save it.
     * If it is, then find the existing item and return that.
     * @param collection the collection to search
     * @param newItem the new item
     * @return an existing item
     */
    public T findOrCreate(Collection<T> collection, T newItem)  {
        boolean found = false;

        if (collection.contains(newItem)) {
            for (T existingItem : collection) {
                found = existingItem.equals(newItem);
                if (found) {
                    newItem = existingItem;
                    break;
                }
            }
        }
        if (!found) {
            logDebug("findOrCreate: creating new item: ", newItem);
            logDebug("findOrCreate: as its not found in collection of size: ", collection.size());
            saveOrUpdate(newItem);
        }

        return newItem;
    }

    /**
     * Finds a query with the given query string and parameters.
     * @param query the query string
     * @param parameters a variable number of parameters
     * @return something nice
     */
    public T findWithQuery(String query, Object... parameters) {
        List<T> results = getHibernateTemplate().find(query, parameters);
        return results == null || results.isEmpty() ? null : results.get(0);
    }

    /**
     * Find a single object for the query with the given parameters.
     * @param query the query
     * @param parameters the query parameters
     * @return the single object returned for the query, or null if no results found
     */
    public Object findObject(String query, Object... parameters) {
        List< ? > results = getHibernateTemplate().find(query, parameters);
        return results == null || results.isEmpty() ? null : results.get(0);
    }

    /**
     * Drop SQL procedures, functions, or tables, depending on sqlObjectType.
     * @param session the currently open session
     * @param sqlObjectType PROCEDURE, FUNCTION, or TABLE
     * @param sqlObjects the names of the procedures, functions, or tables to drop
     */
    protected void dropSQLObjects(Session session, String sqlObjectType, List<String> sqlObjects) {
        String statement = "DROP " + sqlObjectType + " IF EXISTS ";
        for (String sqlObject : sqlObjects) {
            session.createSQLQuery(statement + sqlObject).executeUpdate();
        }
    }

    /**
     * Drop SQL procedures, functions, or tables, depending on sqlObjectType.
     * @param sqlObjectType PROCEDURE, FUNCTION, or TABLE
     * @param sqlObjects the names of the procedures, functions, or tables to drop
     */
    private void dropSQLObjects(String sqlObjectType, List<String> sqlObjects) {
        Session session = getSession();
        dropSQLObjects(session, sqlObjectType, sqlObjects);
        releaseSession(session);
    }

    /**
     * Drop stored procedures and functions from the database after using them.
     * @param procsToDrop the names of the procedures to drop.
     * @param funcsToDrop the names of the functions to drop.
     * @return true if successful, false if an SQL Exception occurred.
     */
    public boolean dropStoredProcedures(List<String> procsToDrop, List<String> funcsToDrop) {
        try {
            // drop the procedures first, then the functions.
            dropSQLObjects("PROCEDURE", procsToDrop);
            dropSQLObjects("FUNCTION", funcsToDrop);
            return true;
        } catch (Exception e) {
            logger.error("An exception was thrown while attempting to drop "
                    + "stored procedures and functions.", e);
            return false;
        }
    }

    /**
     * Drop tables from the database after using them.
     * @param tablesToDrop the names of the tables to drop
     */
    public void dropTables(List<String> tablesToDrop) {
        try {
            dropSQLObjects("TABLE", tablesToDrop);
        } catch (Exception e) {
            logger.error("An exception was thrown while attempting to drop tables.", e);
        }
    }

    /**
     * Drop temporary tables from the database after using them.
     * @param tablesToDrop the names of the temporary tables to drop
     */
    public void dropTemporaryTables(List<String> tablesToDrop) {
        try {
            dropSQLObjects("TEMPORARY TABLE", tablesToDrop);
        } catch (Exception e) {
            logger.error("An exception was thrown while attempting to drop tables.", e);
        }
    }

    /**
     * Quick, little date utility method to convert strings in standard database format.
     * @param dateString the string as a date
     * @return a Date object with the date in the given string
     */
    protected Date getDateStd(String dateString) {
        if (dateString == null) { return null; }
        synchronized (dateFmtStd) {
            Date timeStamp = dateFmtStd.parse(dateString, new ParsePosition(0));
            return timeStamp;
        }
    }

    /**
     * Constructs an SQL "IN" clause from a list of strings.
     * @param items list of items to add to a comma delimited "IN" clause
     * @return an SQL "IN" clause from a list of strings
     */
    public static final String in(List<Comparable> items) {
        return "IN (" + join(", ", items) + ")";
    }

    /**
     * Generate an SQL "in" clause for the item IDs.
     * @param items the items
     * @return an SQL "in" clause for the item IDs
     */
    public static final String inItemIds(final List< ? extends Item> items) {
        return in(new ArrayList<Comparable>() { {
            for (Item item : items) { add(item.getId()); }
        } });
    }

    /**
     * Generate an SQL "in" clause for the item IDs and append to the given "where" clause.
     * @param items the items
     * @param whBuf the where clause
     * @param columnId column to check against
     */
    public static final void andInItemIds(StringBuffer whBuf, String columnId,
            List< ? extends Item> items) {
        if (items != null && items.size() > 0) {
            whBuf.append(" and " + columnId + " " + inItemIds(items));
        }
    }

    /**
     * Convenience for adding scalar results to a query.  scalarKeysAndTypes must consist of
     * alternating pairs of a column alias String and its corresponding Hibernate Type.
     * @param query the query
     * @param scalarKeysAndTypes alternating Strings and Hibernate Types designating column aliases
     */
    public static void addScalars(SQLQuery query, Object... scalarKeysAndTypes) {
        List<KeyValue<String, Type>> kvs = keyValues(scalarKeysAndTypes);

        for (KeyValue<String, Type> kv : kvs) {
            query.addScalar(kv.getKey(), kv.getValue());
        }
    }

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
                    throws SQLException {
                        org.hibernate.Query query = session.createQuery(queryString);
                        query.setFirstResult(offset);
                        query.setMaxResults(limit);
                       return query.list();
                }
            };
        }
    }

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
     * @param <U> the type of thing returned by the query
     * @return the result of the query
     */
    protected <U> List<U> executeSQLQuery(String sql, PrepareQuery prepare) {
        Session session = getSession();
        List<U> results = null;

        try {
            SQLQuery query = session.createSQLQuery(sql);

            prepare.prepareQuery(query);
            results = query.list();
        } finally {
            releaseSession(session);
        }

        return unmodifiableList(results);
    }

    /**
     * Handles the "boiler plate" of calling an SQL query.
     * @param sql the query text
     * @param <U> the type of thing returned by the query
     * @return the result of the query
     */
    protected <U> List<U> executeSQLQuery(String sql) {
        return executeSQLQuery(sql, new PrepareQuery() {
            public void prepareQuery(SQLQuery query) { };
        });
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

    /** Increases the group_concat_mex_len MySQL session variable to 65535. */
    private static final String INCREASE_GROUP_CONCAT_LEN_QUERY =
            "SET SESSION group_concat_max_len = 65535;";
    /**
     * Handles the "boiler plate" of increasing the group_concat_max_len
     * and then calling an SQL query.
     * @param sql the query text
     * @param prepare prepares the statement for execution
     * @return the result of the query
     */
    protected List<Object []> executeSQLQueryMaxConcat(String sql, PrepareQuery prepare) {
        Session session = getSession();
        List<Object []> results = null;

        try {
            SQLQuery maxConcatQuery = session.createSQLQuery(INCREASE_GROUP_CONCAT_LEN_QUERY);
            maxConcatQuery.executeUpdate();
            SQLQuery query = session.createSQLQuery(sql);
            prepare.prepareQuery(query);
            results = (List<Object[]>) query.list();
        } finally {
            // Release the session (and the increased group_concat_max_len)
            releaseSession(session);
        }

        return results;
    }

    /**
     * Call an SQL query and return the single numeric result as a long.
     * @param sql the query text
     * @param prepare prepares the statement for execution
     * @param defaultValue return this if no values returned from the query
     * @param <U> the type of thing returned by the query
     * @return the single result of executing HQL, or defaultValue if no results returned
     */
    protected <U> U getUniqueResultForSQLQuery(String sql, PrepareQuery prepare, U defaultValue) {
        List<U> results = executeSQLQuery(sql, prepare);
        return results.size() == 0 ? defaultValue : results.get(0);
    }

    /**
     * Call a SQL query and return the single numeric result as a long.
     * @param sql the query text
     * @param prepare prepares the statement for execution
     * @param defaultValue return this if no values returned from the query
     * @return the single result of executing SQL, or defaultValue if no results returned
     */
    protected long getLongForSQL(String sql, PrepareQuery prepare, long defaultValue) {
        Number result = getUniqueResultForSQLQuery(sql, prepare, defaultValue);
        return result.longValue();
    }

    /**
     * Call an SQL query and return the single numeric result as an integer.
     * @param sql the query text
     * @param prepare prepares the statement for execution
     * @param defaultValue return this if no values returned from the query
     * @return the single result of executing SQL, or defaultValue if no results returned
     */
    protected int getIntForSQL(String sql, PrepareQuery prepare, long defaultValue) {
        Number result = getUniqueResultForSQLQuery(sql, prepare, defaultValue);
        if (result == null) {
            return (int) defaultValue;
        }
        return result.intValue();
    }

    /**
     * Handles the "boiler plate" of calling an SQL stored procedure.
     * @param query the SQL query
     * @throws SQLException (checked exception) Thrown if there is invalid SQL.
     */
    protected void callSP(String query) throws SQLException {
        Session session = getSession();

        try {
            session.connection()
            .prepareCall(query)
            .executeUpdate();
        } finally {
            releaseSession(session);
        }
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

    /**
     * Call the SQL stored procedure with the specified arguments.
     * @param procedureName the stored procedure's name
     * @param args the arguments to call the procedure with
     * @throws SQLException (checked exception) Thrown if there is invalid SQL.
     */
    protected void callSP(String procedureName, Object... args) throws SQLException {
        callSP(buildSPCall(procedureName, args));
    }

    /**
     * Loads a customized stored procedure file into the database.  Since Hibernate is stupid,
     * we have to execute the contents of the stored procedure file as a list of individual
     * queries.
     * @param session the currently open session
     * @param theProcedure a List containing a series of SQL statements to be executed.
     * @return true if statement execution was successful, false if an SQL exception
     * was thrown.
     */
    protected boolean loadCustomizedSP(Session session, List<String> theProcedure) {
        try {
            for (String statement : theProcedure) {
                if (statement.length() > 1) {
                    session.createSQLQuery(statement).executeUpdate();
                }
            }
            return true;
        } catch (HibernateException exception) {
            logger.error("HibernateException while loading customized stored procedure. "
                    + exception.getMessage(), exception);
            return false;
        } catch (Exception exception) {
            logger.error("Exception::" + exception.getMessage(), exception);
            return false;
        }
    }

    /**
     * Loads a customized stored procedure file into the database.  Since Hibernate is stupid,
     * we have to execute the contents of the stored procedure file as a list of individual
     * queries.
     * @param theProcedure a List containing a series of SQL statements to be executed.
     * @return true if statement execution was successful, false if an SQL exception
     * was thrown.
     */
    public boolean loadCustomizedSP(List<String> theProcedure) {
        Session session = getSession();

        try {
            return loadCustomizedSP(session, theProcedure);
        } finally {
            releaseSession(session);
        }
    }

    /**
     * Customizes the file at filePath by first replacing all instances of toReplace with toInsert,
     * then loading the result into the database.
     * @param filePath the path to the file to open and process.
     * @param toReplace the character sequence to replace.
     * @param toInsert the character sequence to insert.
     * @return true if statement execution was successful, false if an SQL exception
     *      was thrown.
     * @throws IOException (checked exception) Thrown if file at filePath does not exist.
     */
    public boolean loadCustomizedSP(String filePath, String toReplace,
            String toInsert) throws IOException {
        return loadCustomizedSP(openAndReplaceSequence(filePath, toReplace, toInsert));
    }

    /**
     * Customizes the file at filePath by first replacing all instances of the default
     * replace String ("XXX") with toInsert, then loading the result into the database.
     * @param filePath the path to the file to open and process.
     * @param toInsert the character sequence to insert.
     * @return true if statement execution was successful, false if an SQL exception
     *      was thrown.
     * @throws IOException (checked exception) Thrown if file at filePath does not exist.
     */
    public boolean loadCustomizedSP(String filePath, String toInsert)
    throws IOException {
        return loadCustomizedSP(filePath, TO_REPLACE_STRING, toInsert);
    }

    /**
     * Need to expose the "getSession" method for use with getSPTransactions.
     * @return a Session
     */
    public Session session() { return getSession(); }

    /**
     * Need to expose the "releaseSession" method for use with getSPTransactions.
     * @param session the session to release
     */
    public void release(Session session) { releaseSession(session); }
}
