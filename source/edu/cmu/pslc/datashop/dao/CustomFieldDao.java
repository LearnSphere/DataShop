/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.TransactionItem;

/**
 * CustomField Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CustomFieldDao extends AbstractDao {

    /**
     * Standard get for a CustomFieldItem by id.
     * @param id The id of the CustomFieldItem.
     * @return the matching CustomFieldItem or null if none found
     */
    CustomFieldItem get(Long id);

    /**
     * Standard find for an CustomFieldItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired CustomFieldItem.
     * @return the matching CustomFieldItem.
     */
    CustomFieldItem find(Long id);

    /**
     * Standard "find all" for CustomFieldItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Find all custom fields for a given transaction.
     * @param transactionItem the TransactionItem to get custom fields for.
     * @return List of matching CustomFieldItems.
     */
    List find(TransactionItem transactionItem);

    /**
     * Find all custom fields for a given dataset.
     * @param datasetItem the DatasetItem to get custom fields for.
     * @return List of matching CustomFieldItems.
     */
    List find(DatasetItem datasetItem);

    /**
     * Find distinct custom field names for a given dataset.  Used in export.
     * @param id - the id for the datasetItem to get custom fields for.
     * @return List of distinct CustomFieldItem names.
     */
    List getCustomFieldNames(Integer id);

    /**
     * Find a matching custom field item based on everything but the dataset id.
     * @param item the custom field to find.
     * @return matching CustomFieldItem, or NULL if none found.
     */
    CustomFieldItem find(CustomFieldItem item);

    /**
     * Given a list of custom field id, return a list of custom fields.
     * @param customFieldIdList the selected custom field ids
     * @return a list of custom field items
     */
    List find(DatasetItem dataset, List<String> customFieldIdList);

    /**
     * Clear a given customField.
     * @param customFieldItem the given custom field
     */
    void clear(CustomFieldItem customFieldItem);

    /**
     * Get the custom field for a given name and dataset.
     * @param customFieldName the custom field name
     * @param datasetItem the dataset
     * @return the CustomFieldItem
     */
    CustomFieldItem findByNameAndDataset(String customFieldName, DatasetItem datasetItem);

    /**
     * Get the total number of values for a given Custom Field, i.e., the
     * number of transactions that have a value specified for this Custom Field.
     * @param customField the custom field
     * @return the total number of values
     */
    Long getTotalCustomFieldValues(CustomFieldItem customField);

    /**
     * Return a saved item.  If the new item is not in the list, then save it.
     * If it is, then find the existing item and return that.
     * Note that this method is overridden because it doesn't use the item's equals method.
     * Custom Fields must be unique by dataset and name only. Disregard other attributes.
     * @param collection the collection to search
     * @param newItem the new item
     * @return an existing item
     */
    Item findOrCreate(Collection collection, Item newItem);

    /**
     * Get a list of custom fields in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find names in.
     * @param matchAny boolean value indicating whether to only look for names that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching name items sorted by name.
     */
    List<CustomFieldItem> findMatchingByName(String toMatch, DatasetItem dataset, boolean matchAny);
}
