/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.RemoteDatasetInfoDao;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the RemoteDatasetInfoDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RemoteDatasetInfoDaoHibernate extends AbstractDaoHibernate<RemoteDatasetInfoItem>
    implements RemoteDatasetInfoDao
{
    /**
     * Standard get for a RemoteDatasetInfoItem by id.
     * @param id The id of the user.
     * @return the matching RemoteDatasetInfoItem or null if none found
     */
    public RemoteDatasetInfoItem get(Long id) { return get(RemoteDatasetInfoItem.class, id); }

    /**
     * Standard find for an RemoteDatasetInfoItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired RemoteDatasetInfoItem.
     * @return the matching RemoteDatasetInfoItem.
     */
    public RemoteDatasetInfoItem find(Long id) { return find(RemoteDatasetInfoItem.class, id); }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<RemoteDatasetInfoItem> findAll() { return findAll(RemoteDatasetInfoItem.class); }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a list of RemoteDatasetInfo objects for a given dataset.
     * @param dataset the DatasetItem
     * @return a List of objects
     */
    public List<RemoteDatasetInfoItem> findByDataset(DatasetItem dataset) {
        return getHibernateTemplate().find(
                        "FROM RemoteDatasetInfoItem rdi WHERE rdi.dataset = ?", dataset);
    }
}
