/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.sourcedb.dao;

import java.util.Map;

import edu.cmu.datalab.dao.AnalysisImporterDao;

/**
 * Defines all the DAOs for source_db databases and the concrete factories to get the concrete DAOs.
 *
 * @author Shanwen Yu
 * @version $Revision: 11323 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-07-11 16:56:09 -0400 (Fri, 11 Jul 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class SourceDbDaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final SourceDbDaoFactory HIBERNATE =
        new edu.cmu.pslc.datashop.sourcedb.dao.hibernate.SourceDbHibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final SourceDbDaoFactory DEFAULT = HIBERNATE;

    /**
     * Returns the name of the source database.
     * @return the name of the source database
     */
    public abstract String getSourceDatabaseName();

    /**
     * Returns the analysis database login and password.
     * @return Map key-values for "user" and "password"
     */
    public abstract Map<String, String> getSourceDatabaseLogin();

    /**  Get the FlatFileImporterDao. @return FlatFileImporterDao */
    public abstract FlatFileImporterDao getFlatFileImporterDao();

    //--------------------------------------------
    // DataLab DAOs.
    // Likely these will move into their own
    // factory at some point.
    //--------------------------------------------

    /**  Get the AnalysisImporterDao. @return AnalysisImporterDao */
    public abstract AnalysisImporterDao getAnalysisImporterDao();
}
