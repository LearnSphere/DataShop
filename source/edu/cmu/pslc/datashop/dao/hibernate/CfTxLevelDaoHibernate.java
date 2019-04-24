/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.CfTxLevelDao;
import edu.cmu.pslc.datashop.item.CfTxLevelId;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.TransactionItem;

/**
 * Hibernate and Spring implementation of the CfTxLevelDao.
 *
 * @author
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CfTxLevelDaoHibernate extends AbstractDaoHibernate implements CfTxLevelDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a CfTxLevelItem by id.
     * @param id The id of CfTxLevel.
     * @return the matching CfTxLevelItem or null if none found
     */
    public CfTxLevelItem get(CfTxLevelId id) {
        return (CfTxLevelItem)get(CfTxLevelItem.class, id);
    }

    /**
     * Standard "find all" for CfTxLevel items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(CfTxLevelItem.class);
    }

    /**
     * Standard find for an CfTxLevelItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired CfTxLevelItem.
     * @return the matching CfTxLevelItem.
     */
    public CfTxLevelItem find(CfTxLevelId id) {
        return (CfTxLevelItem)find(CfTxLevelItem.class, id);
    }

    /**
     * Given a transaction and a list of custom fields, return a list of CfTxLevel items.
     * @param transactionItem the selected transaction
     * @param customFieldList the selected custom fields
     * @return a list of CfTxLevelItems
     */
    public List find(TransactionItem transactionItem, List customFieldList) {
        //build query
        String query =  "select cfTxLevel from CfTxLevelItem as cfTxLevel"
            + " where transaction.id = ? and customField.id in (";
        for (Iterator iter = customFieldList.iterator(); iter.hasNext();) {
            CustomFieldItem cfItem = (CustomFieldItem)iter.next();
            query += cfItem.getId().toString() + ", ";
        }
        query = query.substring(0, query.length() - 2);
        query += ")";
        query += " order by customField.id";

        //log query
        if (logger.isDebugEnabled()) {
            logger.debug("find by transaction and cuctom field list, query: " + query);
        }

        //run query
        return getHibernateTemplate().find(query, transactionItem.getId());
    }

    public List find(TransactionItem transactionItem) {
        //build query
        String query = "from CfTxLevelItem as cfTxLevel"
            + " where transaction.id = ?"
            + " order by customField.id";

        //log query
        if (logger.isDebugEnabled()) {
            logger.debug("find by model, query: " + query);
        }

        //run query
        return getHibernateTemplate().find(query, transactionItem.getId());
    }

    /**
     * Given a custom field, return a list of CfTxLevel items.
     * @param customFieldItem the selected customField
     * @return a list of transaction with cfTxLevel objects
     */
    public List find(CustomFieldItem customFieldItem) {
            //build query
            String query = "from CfTxLevelItem as cfTxLevel"
                + " where customField.id = ?"
                + " order by transaction.id";

            //log query
            if (logger.isDebugEnabled()) {
                logger.debug("find by model, query: " + query);
            }

            //run query
            return getHibernateTemplate().find(query, customFieldItem.getId());
    }

    /**
     * Clear the cfTxLevels for a given customField.
     * @param customFieldItem the given custom field
     * @return the number of rows deleted
     */
    public int clear(CustomFieldItem customFieldItem) {
        if (customFieldItem == null) {
            throw new IllegalArgumentException("custom field cannot be null.");
        }
        int rowCount = 0;
        String query = "delete from cf_tx_level where custom_field_id = ?";
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.setLong(1, ((Long)customFieldItem.getId()).longValue());
            rowCount = ps.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("clear (CustomField " + customFieldItem.getId()
                        + ") Deleted " + rowCount + " rows.");
            }
        } catch (SQLException exception) {
            logger.error("clear (SkillModel " + customFieldItem.getId()
                    + ") SQLException occurred.", exception);
        } finally {
            releaseSession(session);
        }
        return rowCount;
    }

    /**
     * Get a list of CfTxLevel values in the dataset that match all or a portion of the
     * value parameter.
     * Note: This differs from the others used in filtering in that it returns a list
     * of the values (as Strings) not the list of matching items.
     * @param toMatch A string to match a value too.
     * @param dataset the dataset item to find values in.
     * @param matchAny boolean value indicating whether to only look for values that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching value items sorted by value
     */
    public List<String> findMatchingByValue(String toMatch, DatasetItem dataset,
                                                   boolean matchAny)
    {
        DetachedCriteria query = DetachedCriteria.forClass(CfTxLevelItem.class);
        if (matchAny) {
            query.add(Restrictions.ilike("value", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("value", toMatch, MatchMode.START));
        }
        // Only return distinct values since they are often repeated w/in dataset.
        query.setProjection(Projections.projectionList()
                            .add(Projections.distinct(Projections.property("value"))));
        query.createAlias("customField", "cf");
        query.add(Restrictions.eq("cf.dataset", dataset));
        query.addOrder(Property.forName("value").asc());

        return getHibernateTemplate().findByCriteria(query);
    }
}
