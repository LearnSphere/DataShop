/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.ExternalToolFileMapDao;
import edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapId;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapItem;
import edu.cmu.pslc.datashop.item.ExternalToolItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliTransactionFileDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliTransactionFileItem;

/**
 * Hibernate and Spring implementation of the ResourceUseFileMapDao.
 *
 * @author Hui Cheng
 * @version $Revision: 12891 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:41 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResourceUseOliTransactionFileDaoHibernate
        extends AbstractDaoHibernate implements ResourceUseOliTransactionFileDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ResourceUseOliTransactionFileItem get(Long id) {
        return (ResourceUseOliTransactionFileItem)get(ResourceUseOliTransactionFileItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ResourceUseOliTransactionFileItem find(Long id) {
        return (ResourceUseOliTransactionFileItem)find(ResourceUseOliTransactionFileItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ResourceUseOliTransactionFileItem.class);
    }

    //
    // Non-standard methods begin.
    //
    public int clear(Integer resourceUseOliTransactionFileId) {
            String transactionFileTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + ".resource_use_oli_transaction_file";
            String query = "delete from " + transactionFileTable + " where resource_use_oli_transaction_file_id = ?";
            int rowCnt = 0;
            Session session = getSession();
            try {
                    PreparedStatement ps = session.connection().prepareStatement(query);
                    ps.setInt(1, resourceUseOliTransactionFileId);
                    rowCnt = ps.executeUpdate();
                    if (logger.isDebugEnabled()) {
                        logger.debug("clear (" + transactionFileTable + " for resource use OLI transaction file id:" + resourceUseOliTransactionFileId + ")");
                    }
            } catch (SQLException exception) {
                    logger.error("clear (" + transactionFileTable + " for resource use OLI transaction file id:" + resourceUseOliTransactionFileId + ") SQLException occurred.", exception);
            } finally {
                    releaseSession(session);
            }
            return rowCnt;
    }
}
