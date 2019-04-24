/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.CustomFieldDao;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.TransactionItem;

/**
 * Hibernate and Spring implementation of the CustomFieldDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldDaoHibernate extends AbstractDaoHibernate implements CustomFieldDao {

    /**
     * Standard get for a CustomFieldItem by id.
     * @param id The id of the custom field.
     * @return the matching CustomFieldItem or null if none found
     */
    public CustomFieldItem get(Long id) {
        return (CustomFieldItem)get(CustomFieldItem.class, id);
    }

    /**
     * Standard "find all" for custom field items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(CustomFieldItem.class);
    }

    /**
     * Standard find for an CustomFieldItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired CustomFieldItem.
     * @return the matching CustomFieldItem.
     */
    public CustomFieldItem find(Long id) {
        return (CustomFieldItem)find(CustomFieldItem.class, id);
    }

    /**
     * Find all cfTxLevel for a given transaction.
     * @param transactionItem the TransactionItem to get custom fields for.
     * @return List of matching CustomFieldItems.
     */
    public List find(TransactionItem transactionItem) {
        String query = "select distinct cust from CfTxLevelItem cfLevel"
                + " join cfLevel.customField cust"
                + " where cfLevel.transaction.id = ?";
        List result =  getHibernateTemplate().find(query, transactionItem.getId());

        return result;
    }

    /**
     * Find all custom fields for a given dataset.
     * @param datasetItem the DatasetItem to get custom fields for.
     * @return List of matching CustomFieldItems.
     */
    public List find(DatasetItem datasetItem) {
        String query = "select distinct cust from CustomFieldItem cust"
            + " where dataset.id = ?";
        return getHibernateTemplate().find(query, datasetItem.getId());
    }

    /**
     * Find distinct custom field names for a given dataset.  Used in export.
     * @param id - id of the DatasetItem to get custom fields for.
     * @return List of distinct CustomFieldItem names.
     */
    public List getCustomFieldNames(Integer id) {
        String query = "select distinct customFieldName from CustomFieldItem"
            + " where dataset.id = ?";
        return getHibernateTemplate().find(query, id);
    }

    /**
     * Find a matching custom field item based on everything but the dataset id.
     * @param item the custom field to find.
     * @return matching CustomFieldItem, or NULL if none found.
     */
    public CustomFieldItem find(CustomFieldItem item) {
        DetachedCriteria query = DetachedCriteria.forClass(CustomFieldItem.class);
        query.add(Example.create(item));
        if (item.getDataset() != null && item.getDataset().getId() != null) {
            query.add(Restrictions.sqlRestriction("{alias}.dataset_id = ?",
                    (Integer)item.getDataset().getId(), Hibernate.INTEGER));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("getItemList query: " + query.toString());
        }

        List results = getHibernateTemplate().findByCriteria(query);
        if (results.size() == 0) { return null; }

        if (results.size() > 1) {
            logger.warn("More than one matching custom field found, returning first.");
        }

        return (CustomFieldItem)results.get(0);
    }

    /**
     * Given a list of custom field id, return a list of custom fields.
     * @param customFieldIdList the selected custom field ids
     * @return a list of custom fields
     */
    public List find(DatasetItem dataset, List customFieldIdList) {
            //build query
            String query = "select distinct cf from CustomFieldItem cf"
                    + " where dataset.id = ? and id in (";
            for (Iterator iter = customFieldIdList.iterator(); iter.hasNext();) {
                    query += iter.next().toString() + ", ";
            }
            query = query.substring(0, query.length() - 2);
            query += ")";
            query += " order by id";
            Object [] params = new Object [1];
            params[0] = dataset.getId();
            return getHibernateTemplate().find(query, params);
    }

    /**
     * Clear a given customField.
     * @param customFieldItem the given custom field
     */
    public void clear(CustomFieldItem customFieldItem) {
        if (customFieldItem == null) {
            throw new IllegalArgumentException("custom field cannot be null.");
        }
        String query = "delete from custom_field where custom_field_id = ?";
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.setLong(1, ((Long)customFieldItem.getId()).longValue());
            ps.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("clear CustomField " + customFieldItem.getId());
            }
        } catch (SQLException exception) {
            logger.error("clear (SkillModel " + customFieldItem.getId()
                    + ") SQLException occurred.", exception);
        } finally {
            releaseSession(session);
        }
    }

    /**
     * Get the custom field for a given name and dataset.
     * @param customFieldName the custom field name
     * @param datasetItem the dataset
     * @return the CustomFieldItem
     */
    public CustomFieldItem findByNameAndDataset(String customFieldName, DatasetItem datasetItem) {

        String query = "select distinct cust from CustomFieldItem cust"
            + " where dataset.id = ? and cust.customFieldName = ? ";
        Object[] params = new Object[2];
        params[0] = datasetItem.getId();
        params[1] = customFieldName;

        List results = getHibernateTemplate().find(query, params);

        if (results.size() == 0) { return null; }

        return (CustomFieldItem)results.get(0);
    }

    /** Count of cf_tx_level values by custom field. */
    private static final String COUNT_BY_CUSTOM_FIELD_HQL =
        "SELECT COUNT(*) FROM CfTxLevelItem WHERE customField = ?";

    /**
     * Get the total number of values for a given Custom Field, i.e., the
     * number of transactions that have a value specified for this Custom Field.
     * @param customField the custom field
     * @return the total number of values
     */
    public Long getTotalCustomFieldValues(CustomFieldItem customField) {

        Object[] params = {customField};
        Long numValues =
            (Long) getHibernateTemplate().find(COUNT_BY_CUSTOM_FIELD_HQL, params).get(0);
        if (numValues == null) {
            numValues = Long.valueOf(0);
        }

        return numValues;
    }

    /**
     * Return a saved item.  If the new item is not in the list, then save it.
     * If it is, then find the existing item and return that.
     * Note that this method is overridden because it doesn't use the item's equals method.
     * Custom Fields must be unique by dataset and name only. Disregard other attributes.
     * @param collection the collection to search
     * @param newItem the new item
     * @return an existing item
     */
    public Item findOrCreate(Collection collection, Item newItem)  {
        boolean found = false;

        CustomFieldItem newCustomField = (CustomFieldItem)newItem;

        for (Object existingItem : collection) {
            CustomFieldItem existingCustomField = (CustomFieldItem)existingItem;

            //check only the fields we care about, dataset and name
            if (Item.objectEqualsFK(existingCustomField.getDataset(), newCustomField.getDataset())
                && Item.objectEquals(existingCustomField.getCustomFieldName(),
                                     newCustomField.getCustomFieldName())) {
                found = true;
                newCustomField = existingCustomField;
                break;
            }
        }

        if (!found) {
            if (logger.isDebugEnabled()) {
                logger.debug("findOrCreate: creating new item: " + newCustomField);
                logger.debug("findOrCreate: as its not found in collection of size: "
                             + collection.size());
            }
            saveOrUpdate(newCustomField);
        }

        newItem = newCustomField;
        return newItem;
    }

    /**
     * Get a list of custom fields in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find names in.
     * @param matchAny boolean value indicating whether to only look for names that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching name items sorted by name.
     */
    public List<CustomFieldItem> findMatchingByName(String toMatch, DatasetItem dataset,
                                                    boolean matchAny)
    {
        DetachedCriteria query = DetachedCriteria.forClass(CustomFieldItem.class);
        if (matchAny) {
            query.add(Restrictions.ilike("customFieldName", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("customFieldName", toMatch, MatchMode.START));
        }
        query.createCriteria("dataset").add(Restrictions.eq("id", dataset.getId()));
        query.addOrder(Property.forName("customFieldName").asc());
        return getHibernateTemplate().findByCriteria(query);
    }
}
