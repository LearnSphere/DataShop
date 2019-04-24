/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.DatasetUsageDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUsageId;
import edu.cmu.pslc.datashop.item.DatasetUsageItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the DatasetUsageDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetUsageDaoHibernate extends AbstractDaoHibernate implements DatasetUsageDao {

    /**
     * Standard get for a DatasetUsageItem by id.
     * @param id The id of the user.
     * @return the matching DatasetUsageItem or null if none found
     */
    public DatasetUsageItem get(DatasetUsageId id) {
        return (DatasetUsageItem)get(DatasetUsageItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(DatasetUsageItem.class);
    }

    /**
     * Standard find for an DatasetUsageItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetUsageItem.
     * @return the matching DatasetUsageItem.
     */
    public DatasetUsageItem find(DatasetUsageId id) {
        return (DatasetUsageItem)find(DatasetUsageItem.class, id);
    }

    /**
     * Update the dataset usage for the given user and dataset.
     * This includes updating the last viewed time and the number times viewed fields.
     * @param userItem the user item
     * @param datasetItem the dataset item
     */
    public void updateLastViewed(UserItem userItem, DatasetItem datasetItem) {
        DatasetUsageId usageId = new DatasetUsageId(userItem, datasetItem);
        DatasetUsageItem usageItem = get(usageId);
        if (usageItem == null) {
            usageItem = new DatasetUsageItem(usageId);
            usageItem.setNumTimesViewed(new Integer(1));
        } else {
            int numTimesViews = 1;
            if (usageItem.getNumTimesViewed() != null) {
                numTimesViews = usageItem.getNumTimesViewed().intValue() + 1;
            }
            usageItem.setNumTimesViewed(new Integer(numTimesViews));
        }
        usageItem.setLastViewedTime(new Date());
        saveOrUpdate(usageItem);
    }

    /**
     * Update the dataset usage for the given user and dataset.
     * This includes updating the last exported time and the number times exported fields.
     * @param userItem the user item
     * @param datasetItem the dataset item
     */
    public void updateLastExported(UserItem userItem, DatasetItem datasetItem) {
        DatasetUsageId usageId = new DatasetUsageId(userItem, datasetItem);
        DatasetUsageItem usageItem = get(usageId);
        if (usageItem == null) {
            usageItem = new DatasetUsageItem(usageId);
            usageItem.setNumTimesExported(new Integer(1));
        } else {
            int numTimesViews = 1;
            if (usageItem.getNumTimesExported() != null) {
                numTimesViews = usageItem.getNumTimesExported().intValue() + 1;
            }
            usageItem.setNumTimesExported(new Integer(numTimesViews));
        }
        usageItem.setLastExportedTime(new Date());
        saveOrUpdate(usageItem);
    }

    /** HQL Query for recent datasets. */
    private static final String GET_RECENT_DATASETS =
        " select dataset"
        + " from DatasetItem dataset"
        + " join dataset.datasetUsages usage"
        + " join usage.user user"
        + " where user.id = ?"
        + " order by usage.lastViewedTime desc";

    /**
     * Return a list of the most recently viewed datasets for the given user id.
     * @param userId the user id
     * @return a list of dataset items
     */
    public List getRecentDatasets(String userId) {
        return getHibernateTemplate().find(GET_RECENT_DATASETS, userId);
    }
}
