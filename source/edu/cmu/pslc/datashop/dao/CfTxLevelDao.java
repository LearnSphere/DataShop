/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.CfTxLevelId;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.TransactionItem;

/**
 * CfTxLevel Data Access Object Interface.
 *
 * @author
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CfTxLevelDao extends AbstractDao {

    /**
     * Standard get for a CfTxLevelItem by id.
     * @param id The id of the CfTxLevelItem.
     * @return the matching CfTxLevelItem or null if none found
     */
    CfTxLevelItem get(CfTxLevelId id);

    /**
     * Standard find for an CfTxLevelItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired CfTxLevelItem.
     * @return the matching CfTxLevelItem.
     */
    CfTxLevelItem find(CfTxLevelId id);

    /**
     * Standard "find all" for CfTxLevelItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Given a transaction and a list of custom fields, return a list of cfTxLevel items.
     * @param transactionItem the selected transaction
     * @param customFieldList the selected custom fields
     * @return a list of CfTxLevelItems
     */
    List find(TransactionItem transactionItem, List customFieldList);

    /**
     * Given a transaction, return a list of CfTxLevel items.
     * @param transactionItem the selected transaction
     * @return a list of custom field with cfTxLevel objects
     */
    List find(TransactionItem transactionItem);

    /**
     * Given a custom field, return a list of CfTxLevel items.
     * @param customFieldItem the selected customField
     * @return a list of transaction with cfTxLevel objects
     */
    List find(CustomFieldItem customFieldItem);

    /**
     * Clear the cfTxLevels for a given customField.
     * @param customFieldItem the given custom field
     * @return the number of rows deleted
     */
    public int clear(CustomFieldItem customFieldItem);

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
    List<String> findMatchingByValue(String toMatch, DatasetItem dataset, boolean matchAny);
}
