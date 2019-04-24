/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.dao;

import java.util.Map;

/**
 * Defines all learnsphere.analysis.moocdb DAOs and the concrete factories to get the concrete DAOs.
 *
 * @author Hui Cheng
 * @version $Revision: 14113 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-06-13 16:29:09 -0400 (Tue, 13 Jun 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class DaoFactory {
        
    /**
     * Hibernate DAO factory.
     */
    public static final DaoFactory HIBERNATE =
        new edu.cmu.pslc.learnsphere.analysis.moocdb.dao.hibernate.HibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final DaoFactory DEFAULT = HIBERNATE;

    /**
     * Returns the name of the analysis database.
     * @return the name of the analysis database
     */
    public abstract String getAnalysisDatabaseName();

    /**
     * Returns the analysis database login and password.
     * @return Map key-values for "user" and "password"
     */
    public abstract Map<String, String> getAnalysisDatabaseLogin();
    
    /**
     * Returns the analysis database port and host.
     * @return Map key-values for "host" and "port"
     */
    public abstract Map<String, String> getAnalysisDatabaseHostPort();
    
    /**  Get the Dao. @return CourseraDbsRestoreDao 
     * @param <CourseraDbsRestoreDao>*/
    public abstract CourseraDbsRestoreDao getCourseraDbsRestoreDao();

    /**  Get the MOOCdbDao. @return MOOCdbDao */
    public abstract MOOCdbDao getMOOCdbDao();
    
    /**  Get the FeatureExtractionDao. @return FeatureExtractionDao */
    public abstract FeatureExtractionDao getFeatureExtractionDao();
}
