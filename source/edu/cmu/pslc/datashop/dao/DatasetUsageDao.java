/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUsageId;
import edu.cmu.pslc.datashop.item.DatasetUsageItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * DatasetUsage Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3733 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2007-02-02 16:41:50 -0500 (Fri, 02 Feb 2007) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetUsageDao extends AbstractDao {

    /**
     * Standard get for a DatasetUsageItem by id.
     * @param id The id of the DatasetUsageItem.
     * @return the matching DatasetUsageItem or null if none found
     */
    DatasetUsageItem get(DatasetUsageId id);

    /**
     * Standard find for an DatasetUsageItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DatasetUsageItem.
     * @return the matching DatasetUsageItem.
     */
    DatasetUsageItem find(DatasetUsageId id);

    /**
     * Standard "find all" for DatasetUsageItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Update the dataset usage for the given user and dataset.
     * This includes updating the last viewed time and the number times viewed fields.
     * @param userItem the user item
     * @param datasetItem the dataset item
     */
    void updateLastViewed(UserItem userItem, DatasetItem datasetItem);

    /**
     * Update the dataset usage for the given user and dataset.
     * This includes updating the last exported time and the number times exported fields.
     * @param userItem the user item
     * @param datasetItem the dataset item
     */
    void updateLastExported(UserItem userItem, DatasetItem datasetItem);

    /**
     * Return a list of the most recently viewed datasets for the given user id.
     * @param userId the user id
     * @return a list of dataset items
     */
    List getRecentDatasets(String userId);
}
