/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.hibernate;

import static org.hibernate.Hibernate.LONG;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.TIMESTAMP;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliTransactionDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dto.OliUserTransactionDTO;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliTransactionItem;

/**
 * Data access object to retrieve the data from the resource_use
 * database table via Hibernate.
 *
 * @author Hui Cheng
 * @version $Revision: 13979 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-03-07 15:56:41 -0500 (Tue, 07 Mar 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResourceUseOliTransactionDaoHibernate extends edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate
        implements ResourceUseOliTransactionDao {
    /**
     * Standard get for a resource use oli transaction item by id.
     * @param id the id of the desired resource use OLI transaction item
     * @return the matching ResourceUseOliTransactionItem or null if none found
     */
    public ResourceUseOliTransactionItem get(Long id) {
            return (ResourceUseOliTransactionItem)get(ResourceUseOliTransactionItem.class, id);
    }

    /**
     * Standard find for an resource use OLI transaction item by id.
     * @param id id of the object to find
     * @return ResourceUseOliTransactionItem
     */
    public ResourceUseOliTransactionItem find(Long id) {
            return (ResourceUseOliTransactionItem)find(ResourceUseOliTransactionItem.class, id);
    }

    /**
     * Standard "find all" for Resource Use OLI transaction item.
     * @return a List of objects
     */
    public List<ResourceUseOliTransactionItem> findAll() {
            return getHibernateTemplate().find("from " + ResourceUseOliTransactionItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //
    /**related table names*/
    private static String RESOURCE_USE_OLI_USER_SESS_TABLE_NAME = "resource_use_oli_user_sess";
    private static String RESOURCE_USE_OLI_TRANSACTION_TABLE_NAME = "resource_use_oli_transaction";
   
    public List<ResourceUseOliTransactionItem> getTransactionByAnonStudentId(Integer resourceUseOliTransactionFileId, String anonStudentId) {
            String userSessTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_USER_SESS_TABLE_NAME;
            String transactionTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_TRANSACTION_TABLE_NAME;
            String STUDENT_DATA_QUERY_SELECT = "SELECT resource_use_oli_transaction_id, " +
                                                    "resource_use_oli_transaction_file_id, " +
                                                    "guid, " +
                                                    transactionTable + ".user_sess, " +
                                                    "source, " +
                                                    "CONVERT_TZ(transaction_time, time_zone,'UTC') as transaction_time, " +
                                                    "time_zone, " +
                                                    "action, " +
                                                    "external_object_id, " +
                                                    "container, " +
                                                    "concept_this, " +
                                                    "concept_req, " +
                                                    "eastern_time, " +
                                                    "server_receipt_time, " +
                                                    "info_type, " +
                                                    "replace(replace(replace(info, '\\\\t', ''), '\\\\r', ''), '\\\\n','') as info " + 
                                                    "FROM " + transactionTable + ", " + userSessTable + " " +
                                                    "WHERE " + transactionTable + ".user_sess = " + userSessTable + ".user_sess " +
                                                    "AND " + transactionTable + ".resource_use_oli_transaction_file_id = :resourceUseOliTransactionFileId " + 
                                                    "AND " + userSessTable + ".anon_student_id = :anonStudentId " +
                                                    "ORDER BY transaction_time, resource_use_oli_transaction_id";
            Session session = getSession();
            SQLQuery query = session.createSQLQuery(STUDENT_DATA_QUERY_SELECT).addEntity(ResourceUseOliTransactionItem.class);
            query.setInteger("resourceUseOliTransactionFileId", resourceUseOliTransactionFileId);
            query.setString("anonStudentId", anonStudentId);
            logger.debug(STUDENT_DATA_QUERY_SELECT);
            logger.debug("resourceUseOliTransactionFileId: " + resourceUseOliTransactionFileId);
            logger.debug("anonStudentId: " + anonStudentId);
            List dbResults = query.list();
            releaseSession(session);
            return dbResults;
    }
    
    /** Constant */
    private static final int ANON_USER_ID_IDX = 0;
    /** Constant */
    private static final int TRANSACTION_ID_IDX = 1;
    /** Constant */
    private static final int USER_SESS_IDX = 2;
    /** Constant */
    private static final int TRANSACTION_TIME_IDX = 3;
    /** Constant */
    private static final int ACTION_IDX = 4;
    /** Constant */
    private static final int INFO_TYPE_IDX = 5;
    /** Constant */
    private static final int INFO_IDX = 6;
    
    
    public List<OliUserTransactionDTO> getAllTransactions(final Integer resourceUseOliUserSessFileId, final Integer resourceUseOliTransactionFileId) {
            String userSessTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_USER_SESS_TABLE_NAME;
            String transactionTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_TRANSACTION_TABLE_NAME;
            // SQL for fetching user transaction for a transaction file and a user_sess file. 
            String STUDENT_DATA_QUERY_SELECT = "SELECT " + userSessTable + ".anon_student_id, " +
                                                    "resource_use_oli_transaction_id, " +
                                                    transactionTable + ".user_sess, " +
                                                    "CONVERT_TZ(transaction_time, time_zone,'UTC') as transaction_time, " +
                                                    "action, " +
                                                    "info_type, " +
                                                    "replace(replace(replace(info, '\\\\t', ''), '\\\\r', ''), '\\\\n','') as info " + 
                                                    "FROM " + transactionTable + ", " + userSessTable + " " +
                                                    "WHERE " + transactionTable + ".user_sess = " + userSessTable + ".user_sess " +
                                                    "AND " + userSessTable + ".resource_use_oli_user_sess_file_id = ? " +
                                                    "AND " + transactionTable + ".resource_use_oli_transaction_file_id = ? " + 
                                                    "ORDER BY anon_student_id, transaction_time, resource_use_oli_transaction_id";
            
            final List<Object[]> results = executeSQLQuery(STUDENT_DATA_QUERY_SELECT, new PrepareQuery() {
                    public void prepareQuery(SQLQuery query) {
                        query.setInteger(0, resourceUseOliUserSessFileId);
                        query.setInteger(1, resourceUseOliTransactionFileId);
                        addScalars(query, "anon_student_id", STRING, 
                                        "resource_use_oli_transaction_id", LONG,
                                        "user_sess", STRING,
                                        "transaction_time", TIMESTAMP,
                                        "action", STRING,
                                        "info_type", STRING,
                                        "info", STRING);
                    }
            });
                
            return new ArrayList<OliUserTransactionDTO>() { {
                    for (Object[] result : results) { 
                            OliUserTransactionDTO thisOliUserTransactionDTO = new OliUserTransactionDTO();
                            thisOliUserTransactionDTO.setResourceUseTransactionId((Long)result[TRANSACTION_ID_IDX]);
                            thisOliUserTransactionDTO.setStudent((String)result[ANON_USER_ID_IDX]);
                            thisOliUserTransactionDTO.setSession((String)result[USER_SESS_IDX]);
                            thisOliUserTransactionDTO.setUTCTime((Date)result[TRANSACTION_TIME_IDX]);
                            thisOliUserTransactionDTO.setAction((String)result[ACTION_IDX]);
                            thisOliUserTransactionDTO.setInfoType((String)result[INFO_TYPE_IDX]);
                            thisOliUserTransactionDTO.setInfo((String)result[INFO_IDX]);
                            add(thisOliUserTransactionDTO); }
                } };
    }
    
    /*public List<OliUserTransactionDTO> getAllTransactions(Integer resourceUseOliUserSessFileId, Integer resourceUseOliTransactionFileId) {
            String userSessTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_USER_SESS_TABLE_NAME;
            String transactionTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_TRANSACTION_TABLE_NAME;
            String STUDENT_DATA_QUERY_SELECT = "SELECT " + userSessTable + ".anon_student_id, " +
                                                    "resource_use_oli_transaction_id, " +
                                                    transactionTable + ".user_sess, " +
                                                    "CONVERT_TZ(transaction_time, time_zone,'UTC') as transaction_time, " +
                                                    "action, " +
                                                    "info_type, " +
                                                    "replace(replace(replace(info, '\\\\t', ''), '\\\\r', ''), '\\\\n','') as info " + 
                                                    "FROM " + transactionTable + ", " + userSessTable + " " +
                                                    "WHERE " + transactionTable + ".user_sess = " + userSessTable + ".user_sess " +
                                                    "AND " + transactionTable + ".resource_use_oli_transaction_file_id = :resourceUseOliTransactionFileId " + 
                                                    "AND " + userSessTable + ".resource_use_oli_user_sess_file_id = :resourceUseOliUserSessFileId " +
                                                    "ORDER BY anon_student_id, transaction_time, resource_use_oli_transaction_id";
            Session session = getSession();
            SQLQuery query = session.createSQLQuery(STUDENT_DATA_QUERY_SELECT);
            logger.info(STUDENT_DATA_QUERY_SELECT);
            query.setInteger("resourceUseOliTransactionFileId", resourceUseOliTransactionFileId);
            query.setInteger("resourceUseOliUserSessFileId", resourceUseOliUserSessFileId);
            logger.debug("TransactionFileId: " + resourceUseOliTransactionFileId);
            logger.debug("UserSessFileId: " + resourceUseOliUserSessFileId);
            List<Object[]> dbResults = query.list();
            releaseSession(session);
            
            List<OliUserTransactionDTO> studentTransactions = new ArrayList();
            for (int i = 0; i < dbResults.size(); i++) {
                    Object[] objArray = (Object[])dbResults.get(i);
                    if (objArray[TRANSACTION_ID_IDX] != null) {
                            OliUserTransactionDTO thisOliUserTransactionDTO = new OliUserTransactionDTO();
                            thisOliUserTransactionDTO.setResourceUseTransactionId((Long)objArray[TRANSACTION_ID_IDX]);
                            thisOliUserTransactionDTO.setStudent((String)objArray[ANON_USER_ID_IDX]);
                            thisOliUserTransactionDTO.setSession((String)objArray[USER_SESS_IDX]);
                            thisOliUserTransactionDTO.setUTCTime((Date)objArray[TRANSACTION_TIME_IDX]);
                            thisOliUserTransactionDTO.setAction((String)objArray[ACTION_IDX]);
                            thisOliUserTransactionDTO.setInfoType((String)objArray[INFO_TYPE_IDX]);
                            thisOliUserTransactionDTO.setInfo((String)objArray[INFO_IDX]);
                            studentTransactions.add(thisOliUserTransactionDTO);
                    }
            }
            return studentTransactions;
    }*/
    
    public int clear(Integer resourceUseOliTransactionFileId) {
            String transactionTable = DaoFactory.HIBERNATE.getAnalysisDatabaseName() + "." + RESOURCE_USE_OLI_TRANSACTION_TABLE_NAME;
            String query = "delete from " + transactionTable + " where resource_use_oli_transaction_file_id = ?";
            int rowCnt = 0;
            Session session = getSession();
            try {
                    PreparedStatement ps = session.connection().prepareStatement(query);
                    ps.setInt(1, resourceUseOliTransactionFileId);
                    rowCnt = ps.executeUpdate();
                    if (logger.isDebugEnabled()) {
                        logger.debug("clear (" + transactionTable + " for resource use OLI transaction file id:" + resourceUseOliTransactionFileId + ")");
                    }
            } catch (SQLException exception) {
                    logger.error("clear (" + transactionTable + " for resource use OLI transaction file id:" + resourceUseOliTransactionFileId + ") SQLException occurred.", exception);
            } finally {
                    releaseSession(session);
            }
            return rowCnt;
    }
}
