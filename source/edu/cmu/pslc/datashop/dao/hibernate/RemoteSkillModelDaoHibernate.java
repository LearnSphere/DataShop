/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.RemoteSkillModelDao;

import edu.cmu.pslc.datashop.item.RemoteSkillModelItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;
import edu.cmu.pslc.datashop.item.Item;

/**
 * Hibernate and Spring implementation of the RemoteSkillModelDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RemoteSkillModelDaoHibernate
    extends AbstractDaoHibernate implements RemoteSkillModelDao
{
    /**
     * Standard get for a RemoteSkillModelItem by id.
     * @param id The id of the user.
     * @return the matching RemoteSkillModelItem or null if none found
     */
    public RemoteSkillModelItem get(Long id) {
        if (id == null) { return null; }
        return (RemoteSkillModelItem)get(RemoteSkillModelItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(RemoteSkillModelItem.class);
    }

    /**
     * Standard find for an RemoteSkillModelItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired RemoteSkillModelItem.
     * @return the matching RemoteSkillModelItem.
     */
    public RemoteSkillModelItem find(Long id) {
        return (RemoteSkillModelItem)find(RemoteSkillModelItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Find all the remote skill models for the given dataset.
     * @param remoteDatasetInfo the remote dataset info item
     * @return a list of remote skill model items
     */
    public List<RemoteSkillModelItem> find(RemoteDatasetInfoItem remoteDatasetInfo) {
        String query = "SELECT DISTINCT sm FROM RemoteSkillModelItem sm"
            + " where sm.remoteDatasetInfo = ?";
        return getHibernateTemplate().find(query, remoteDatasetInfo);
    }
}
