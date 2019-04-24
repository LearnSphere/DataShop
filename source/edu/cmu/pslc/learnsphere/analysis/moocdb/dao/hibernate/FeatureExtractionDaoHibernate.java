/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.dao.hibernate;

import static org.hibernate.Hibernate.TIMESTAMP;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dto.LoggingActivityOverviewReport;
import edu.cmu.pslc.datashop.dto.LoggingActivitySession;
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.FeatureExtractionDao;
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.MOOCdbDao;
import edu.cmu.pslc.learnsphere.analysis.moocdb.item.FeatureExtractionItem;
import edu.cmu.pslc.learnsphere.analysis.moocdb.item.LongitudinalFeatureItem;
import edu.cmu.pslc.learnsphere.analysis.moocdb.item.MOOCdbItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliTransactionItem;

/**
 * Data access object to retrieve the data from the moocdbs table via Hibernate.
 *
 * @author Hui Cheng
 * @version $Revision: 14073 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-05-15 14:48:12 -0400 (Mon, 15 May 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FeatureExtractionDaoHibernate extends edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate
        implements FeatureExtractionDao {
    /**
     * Standard get for FeatureExtractionItem by id.
     * @param id the id of the desired FeatureExtractionItem 
     * @return the matching FeatureExtractionItem or null if none found
     */
    public FeatureExtractionItem get(Long id) {
            return (FeatureExtractionItem)get(FeatureExtractionItem.class, id);
    }

    /**
     * Standard find for a FeatureExtractionItem by id.
     * @param id id of the object to find
     * @return FeatureExtractionItem
     */
    public FeatureExtractionItem find(Long id) {
            return (FeatureExtractionItem)find(FeatureExtractionItem.class, id);
    }

    /**
     * Standard "find all" for FeatureExtractionItem.
     * @return a List of objects
     */
    public List<FeatureExtractionItem> findAll() {
            return getHibernateTemplate().find("from " + MOOCdbItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //
    /**related table names*/
    private static String FEATURE_EXTRACTIONS_TABLE_NAME = "feature_extractions";
    private static String LONGITUDINAL_FEATURE_TABLE_NAME = "longitudinal_features";
    
    public Map<Integer, String> getAllFeatures(String MOOCdbName) {
            String query = "SELECT * FROM " + MOOCdbName
                                    + "." + LONGITUDINAL_FEATURE_TABLE_NAME
                                    + " ORDER BY longitudinal_feature_id ";
            logDebug("getAllFeatures, query[", query, "]");
            Map<Integer, String> allFeatures = new HashMap<Integer, String>();
            Session session = getSession();
         
            List<LongitudinalFeatureItem> dbResults = null;
            try {
                SQLQuery sqlQuery = session.createSQLQuery(query).addEntity(LongitudinalFeatureItem.class);
                dbResults = sqlQuery.list();
                for (LongitudinalFeatureItem item : dbResults) {
                        allFeatures.put((Integer)item.getId(), item.getName());
                }
            } finally {
                releaseSession(session);
            }
            
            return allFeatures;
    }
    
    public FeatureExtractionItem findAFeatureExtraction(String MOOCdbName, Date startDate, int numberWeeks, String featuresToExtract) {
            String QUERY_SELECT = "SELECT * FROM " + MOOCdbName + "." + FEATURE_EXTRACTIONS_TABLE_NAME + " " +
                            "WHERE start_date = :startDate " +
                            "AND num_of_week = :numOfWeek " + 
                            "AND features_list = :featuresList " ;
            
            Session session = getSession();
            SQLQuery query = session.createSQLQuery(QUERY_SELECT).addEntity(FeatureExtractionItem.class);
            query.setTimestamp("startDate", startDate);
            query.setInteger("numOfWeek", numberWeeks);
            query.setString("featuresList", featuresToExtract);
            logger.debug(QUERY_SELECT);
            logger.debug("startDate: " + startDate);
            logger.debug("numOfWeek: " + numberWeeks);
            logger.debug("featuresList: " + featuresToExtract);
            List<FeatureExtractionItem> dbResults = query.list();
            releaseSession(session);
            if (dbResults != null && dbResults.size() > 0)
                    return dbResults.get(0);
            else return null;
    }
    
    //can't use default save and update because database changes
    public void saveOrUpdateFeatureExtractionItem(String MOOCdbName, FeatureExtractionItem featureExtractionItem) 
                    throws Exception {
            Session session = getSession();
            try {
                    String FEATURE_EXTRACTION_TABLE_NAME = MOOCdbName + ".feature_extractions";
                    String statement = null;
                    if (featureExtractionItem.getId() == null) {
                            statement = "INSERT INTO " + FEATURE_EXTRACTION_TABLE_NAME +
                                            " (created_by, start_timestamp, start_date, num_of_week, features_list, end_timestamp) VALUES " +
                                            "(:createdBy, :startTimestamp, :startDate, :numOfWeek, :featuresList, :endTimestamp)";
                                            
                            logger.debug(statement + "; for " + featureExtractionItem);
                            SQLQuery query = session.createSQLQuery(statement);
                            query.setString("createdBy", featureExtractionItem.getCreatedBy());
                            query.setTimestamp("startTimestamp", featureExtractionItem.getStartTimestamp());
                            query.setTimestamp("startDate", featureExtractionItem.getStartDate());
                            query.setInteger("numOfWeek", featureExtractionItem.getNumOfWeek());
                            query.setString("featuresList", featureExtractionItem.getFeaturesList());
                            query.setTimestamp("endTimestamp", featureExtractionItem.getEndTimestamp());
                            
                            query.executeUpdate();
                            FeatureExtractionItem newItem = findAFeatureExtraction(MOOCdbName, 
                                                                    featureExtractionItem.getStartDate(), 
                                                                    featureExtractionItem.getNumOfWeek(), 
                                                                    featureExtractionItem.getFeaturesList());
                            featureExtractionItem.setCreatedBy(newItem.getCreatedBy());
                            featureExtractionItem.setId((Long)newItem.getId());
                            featureExtractionItem.setEndTimestamp(newItem.getEndTimestamp());
                            featureExtractionItem.setFeaturesList(newItem.getFeaturesList());
                            featureExtractionItem.setNumOfWeek(newItem.getNumOfWeek());
                            featureExtractionItem.setStartDate(newItem.getStartDate());
                            featureExtractionItem.setStartTimestamp(newItem.getStartTimestamp());
                            
                    } else {
                            statement = "UPDATE " + FEATURE_EXTRACTION_TABLE_NAME +
                                            " SET created_by = :createdBy," +
                                            " start_timestamp = :startTimestamp, " +
                                            " start_date = :startDate, " +
                                            " num_of_week = :numOfWeek, " +
                                            " features_list = :featuresList, " +
                                            " end_timestamp = :endTimestamp " +
                                            " WHERE feature_extraction_id = :featureExtractionId";
                            logger.debug(statement + "; for " + featureExtractionItem);
                            SQLQuery query = session.createSQLQuery(statement);
                            query.setString("createdBy", featureExtractionItem.getCreatedBy());
                            query.setTimestamp("startTimestamp", featureExtractionItem.getStartTimestamp());
                            query.setTimestamp("startDate", featureExtractionItem.getStartDate());
                            query.setInteger("numOfWeek", featureExtractionItem.getNumOfWeek());
                            query.setString("featuresList", featureExtractionItem.getFeaturesList());
                            query.setTimestamp("endTimestamp", featureExtractionItem.getEndTimestamp());
                            query.setLong("featureExtractionId", (Long)featureExtractionItem.getId());
                            query.executeUpdate();
                            
                    }
                    
                    
            } finally {
                    releaseSession(session);
            }
    }
}
