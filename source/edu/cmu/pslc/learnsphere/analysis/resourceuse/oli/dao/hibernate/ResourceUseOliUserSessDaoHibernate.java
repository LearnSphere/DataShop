/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliUserSessDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessFileItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessItem;

/**
 * Data access object to retrieve the data from the resource_use_oli_user_sess
 * database table via Hibernate.
 *
 * @author Hui Cheng
 * @version $Revision: 12891 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:41 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResourceUseOliUserSessDaoHibernate extends edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate implements ResourceUseOliUserSessDao {
     /**
     * Standard get for a Resource Use OLI User Sess item by id.
     * @param id the id of the desired ResourceUseUserSessItem
     * @return the matching ResourceUseUserSessItem or null if none found
     */
     public ResourceUseOliUserSessItem get(Long id) {
             return (ResourceUseOliUserSessItem)get(ResourceUseOliUserSessItem.class, id);
     }

    /**
     * Standard find for a ResourceUseOliUserSessItem by id.
     * @param id id of the object to find
     * @return ResourceUseOliUserSessItem
     */
     public ResourceUseOliUserSessItem find(Long id) {
             return (ResourceUseOliUserSessItem)find(ResourceUseOliUserSessItem.class, id);
     }

    /**
     * Standard "find all" for ResourceUseOliUserSessItem.
     * @return a List of objects
     */
    public List<ResourceUseOliUserSessItem> findAll() {
            return getHibernateTemplate().find("from " + ResourceUseOliUserSessItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //
    /**related table names*/
    private static String RESOURCE_USE_OLI_USER_SESS_TABLE_NAME = "resource_use_oli_user_sess";
    private static String RESOURCE_USE_OLI_TRANSACTION_TABLE_NAME = "resource_use_oli_transaction";
   
    /** HQL query for the getViaResourceUseOliUserSessFile method. */
    private static final String FIND_BY_USE_SESS_HQL
            = "from ResourceUseOliUserSessItem userSess"
            + " where resourceUseOliUserSessFileItem = ?";
    
    /**
     * Return a list of ResourceUseOliUserSessItem for a given resourceUseOliUserSessFileItem
     * @param ResourceUseOliUserSessFileItem resourceUseOliUserSessFileItem resource use OLI user_sess file item
     * @return list of ResourceUseOliUserSessItem*/
    public List<ResourceUseOliUserSessItem> findByResourceUseOliUserSessFile(ResourceUseOliUserSessFileItem resourceUseOliUserSessFileItem) {
            Object[] params = {resourceUseOliUserSessFileItem};
            return getHibernateTemplate().find(FIND_BY_USE_SESS_HQL, params);
    }
   
    /**
     * Return a list of anon student id for a given resourceUseOliTransactionFileItem
     * @param ResourceUseOliTransactionFileItem resourceUseOliTransactionFileItem resource use OLI transaction file item
     * @return list of String for anon student id*/
    public List<String> findAnonStudentByResourceUseOliTransactionFile(Integer resourceUseOliTransactionFileId) {
            Session session = getSession();
            String userSessTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_USER_SESS_TABLE_NAME;
            String transactionTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_TRANSACTION_TABLE_NAME;
            String distinctStudentQuery = "SELECT distinct anon_student_id " +
                                            "FROM " + userSessTable + ", " + transactionTable + " " +
                                            "WHERE " + userSessTable + ".user_sess = " + transactionTable + ".user_sess " +
                                            "AND " + transactionTable + ".resource_use_oli_transaction_file_id = " + resourceUseOliTransactionFileId;
            logDebug("get distinct student_id, query[", distinctStudentQuery, "]");
            List<String> anonStudentIds = new ArrayList<String>();
            try {
                SQLQuery sqlQuery = session.createSQLQuery(distinctStudentQuery);
                List<String> dbResults = sqlQuery.list();
                for (String studentId : dbResults) {
                        anonStudentIds.add(studentId);
                }
            } finally {
                releaseSession(session);
            }
            return anonStudentIds;
    }

    public int clear(Integer resourceUseOliUserSessFileId) {
            String userSessTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_USER_SESS_TABLE_NAME;
            String query = "delete from " + userSessTable + " where resource_use_oli_user_sess_file_id = ?";
            int rowCnt = 0;
            Session session = getSession();
            try {
                    PreparedStatement ps = session.connection().prepareStatement(query);
                    ps.setInt(1, resourceUseOliUserSessFileId);
                    rowCnt = ps.executeUpdate();
                    if (logger.isDebugEnabled()) {
                        logger.debug("clear (" + userSessTable + " for resource use OLI user sess file id:" + resourceUseOliUserSessFileId + ")");
                    }
            } catch (SQLException exception) {
                    logger.error("clear (" + userSessTable + " for resource use OLI user sess file id:" + resourceUseOliUserSessFileId + ") SQLException occurred.", exception);
            } finally {
                    releaseSession(session);
            }  
            return rowCnt;
    }
}
