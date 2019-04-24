/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DatasetUserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.DatasetUserTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.DatasetUserTermsOfUseMapItem;

/**
 * Hibernate and Spring implementation of the DatasetUserTermsOfUseMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetUserTermsOfUseMapDaoHibernate extends AbstractDaoHibernate
    implements DatasetUserTermsOfUseMapDao {

    /**
     * Standard get for a DatasetUserTermsOfUseMapItem by id.
     * @param id The id of the DatasetUserTermsOfUseMapItem.
     * @return the matching DatasetUserTermsOfUseMapItem or null if none found
     */
    public DatasetUserTermsOfUseMapItem get(DatasetUserTermsOfUseMapId id) {
        return (DatasetUserTermsOfUseMapItem)get(DatasetUserTermsOfUseMapItem.class, id);
    }

    /**
     * Standard find for an DatasetUserTermsOfUseMapItem.
     * Only the id of the item will be filled in.
     * @param id the DatasetUserTermsOfUseMapId for this item.
     * @return the matching DatasetUserTermsOfUseMapItem.
     */
    public DatasetUserTermsOfUseMapItem find(DatasetUserTermsOfUseMapId id) {
        return (DatasetUserTermsOfUseMapItem)find(DatasetUserTermsOfUseMapItem.class, id);
    }

    /**
     * Standard "find all" for DatasetUserTermsOfUseMapItem items.
     * @return a List of objects
     */
    public List<DatasetUserTermsOfUseMapItem> findAll() {
        return findAll(DatasetUserTermsOfUseMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByDatasetUserAndVersion method. */
    private static final String FIND_BY_DATASET_USER_AND_VERSION_QUERY =
        "from DatasetUserTermsOfUseMapItem map "
        + "where dataset.id = ? and user.id = ? and termsOfUseVersion = ?";

    /**
     * Get the item for the given dataset, username and version item.
     * @param datasetId the dataset id
     * @param username the account id
     * @param versionItem the terms of use version to check
     * @return the item if found, null otherwise
     */
    public DatasetUserTermsOfUseMapItem
        findByDatasetUserAndVersion(Integer datasetId, String username,
                                    TermsOfUseVersionItem versionItem) {

        if (datasetId == null || username == null || versionItem == null) {
            return null;
        }

        DatasetUserTermsOfUseMapItem foundItem = null;

        Object[] params = {datasetId, username, versionItem};
        List<DatasetUserTermsOfUseMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_DATASET_USER_AND_VERSION_QUERY, params);
        int numItems = itemList.size();
        if (numItems >= 1) {
            foundItem = itemList.get(0);
            if (numItems > 1) {
                logger.warn("More than one row found: " + numItems);
            }
        }

        return foundItem;
    }

    /** HQL query for the hasAgreedBefore method. */
    private static final String HAS_AGREED_BEFORE_QUERY =
            "from DatasetUserTermsOfUseMapItem map "
          + "where user.id = ? and dataset.id = ? and termsOfUse = ?";

    /**
     * Get the item for the given user, dataset and version.
     * @param username the account id
     * @param datasetId the dataset id
     * @param termsItem the terms of use to check
     * @return the item if found, null otherwise
     */
    public boolean hasAgreedBefore(String username, Integer datasetId, TermsOfUseItem termsItem) {

        if (username == null || datasetId == null || termsItem == null) {
            return false;
        }

        boolean agreedBefore = false;

        Object[] params = {username, datasetId, termsItem};
        List<DatasetUserTermsOfUseMapItem> itemList =
            getHibernateTemplate().find(HAS_AGREED_BEFORE_QUERY, params);
        if (itemList.size() >= 1) {
            agreedBefore = true;
        }

        return agreedBefore;
    }

} // end DatasetUserTermsOfUseMapDaoHibernate.java
