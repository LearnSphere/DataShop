/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.DatasetUserTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.DatasetUserTermsOfUseMapItem;

/**
 * Dataset-User-Terms Of Use Map Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetUserTermsOfUseMapDao extends AbstractDao {

    /**
     * Standard get for a DatasetUserTermsOfUseMapItem by id.
     * @param id The id of the DatasetUserTermsOfUseMapItem.
     * @return the matching DatasetUserTermsOfUseMapItem or null if none found
     */
    DatasetUserTermsOfUseMapItem get(DatasetUserTermsOfUseMapId id);

    /**
     * Standard find for an DatasetUserTermsOfUseMapItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetUserTermsOfUseMapItem.
     * @return the matching DatasetUserTermsOfUseMapItem.
     */
    DatasetUserTermsOfUseMapItem find(DatasetUserTermsOfUseMapId id);

    /**
     * Standard "find all" for DatasetUserTermsOfUseMapItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Get the item for the given dataset id, username and version item.
     * @param datasetId the dataset id
     * @param username the account id
     * @param versionItem the terms of use version to check
     * @return the item if found, null otherwise
     */
    DatasetUserTermsOfUseMapItem findByDatasetUserAndVersion(Integer datasetId,
                                                             String username,
                                                             TermsOfUseVersionItem versionItem);

    /**
     * Determine if the specified user has agreeed to the TOU for the specific dataset.
     * @param username the account id
     * @param datatsetId the dataset id
     * @param termsItem the terms of use to check
     * @return the item if found, null otherwise
     */
    boolean hasAgreedBefore(String username, Integer datasetId, TermsOfUseItem termsItem);

} // end DatasetUserTermsOfUseMapDao.java
