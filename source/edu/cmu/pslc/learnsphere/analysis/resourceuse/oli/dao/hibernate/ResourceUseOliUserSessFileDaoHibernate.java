/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import edu.cmu.datalab.item.AnalysisItem;
import edu.cmu.datalab.item.CorrelationValueId;
import edu.cmu.datalab.item.CorrelationValueItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliUserSessFileDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessFileItem;

/**
 * Data access object to retrieve the data from the resource_use_user_sess_file
 * database table via Hibernate.
 *
 * @author Hui Cheng
 * @version $Revision: 12891 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:41 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ResourceUseOliUserSessFileDaoHibernate extends edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate
        implements ResourceUseOliUserSessFileDao {

    /**
     * Standard get for a Resource Use OLI user_sess File item by id.
     * @param id the id of the desired ResourceUseOliUserSessFileItem
     * @return the matching ResourceUseOliUserSessFileItem or null if none found
     */
    public ResourceUseOliUserSessFileItem get(Integer id) {
            return (ResourceUseOliUserSessFileItem)get(ResourceUseOliUserSessFileItem.class, id);
    }

    /**
     * Standard find for a ResourceUseOliUserSessFileItem by id.
     * @param id id of the object to find
     * @return ResourceUseOliUserSessFileItem
     */
    public ResourceUseOliUserSessFileItem find(Integer id) {
            return (ResourceUseOliUserSessFileItem)find(ResourceUseOliUserSessFileItem.class, id);
    }

    /**
     * Standard "find all" for ResourceUseOliUserSessFileItem.
     * @return a List of objects
     */
    public List<ResourceUseOliUserSessFileItem> findAll() {
            return getHibernateTemplate().find("from " + ResourceUseOliUserSessFileItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //
    public int clear(Integer resourceUseOliUserSessFileId) {
            String userSessFileTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + ".resource_use_oli_user_sess_file";
            String query = "delete from " + userSessFileTable + " where resource_use_oli_user_sess_file_id = ?";
            int rowCnt = 0;
            Session session = getSession();
            try {
                    PreparedStatement ps = session.connection().prepareStatement(query);
                    ps.setInt(1, resourceUseOliUserSessFileId);
                    rowCnt = ps.executeUpdate();
                    if (logger.isDebugEnabled()) {
                        logger.debug("clear (" + userSessFileTable + " for resource use OLI transaction file id:" + resourceUseOliUserSessFileId + ")");
                    }
            } catch (SQLException exception) {
                    logger.error("clear (" + userSessFileTable + " for resource use OLI transaction file id:" + resourceUseOliUserSessFileId + ") SQLException occurred.", exception);
            } finally {
                    releaseSession(session);
            }
            return rowCnt;
    }
}
