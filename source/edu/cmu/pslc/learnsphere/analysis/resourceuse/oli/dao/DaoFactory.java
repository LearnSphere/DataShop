/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao;

import java.util.Map;

/**
 * Defines all learnsphere.analysis.resourceuse.oli DAOs and the concrete factories to get the concrete DAOs.
 *
 * @author Hui Cheng
 * @version $Revision: 12890 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:20 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class DaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final DaoFactory HIBERNATE =
        new edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.hibernate.HibernateDaoFactory();

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

    /**  Get the Dao. @return ResourceUseOliTransactionFileDao */
    public abstract ResourceUseOliTransactionFileDao getResourceUseOliTransactionFileDao();

    /**  Get the ResourceUseOliTransactionDao. @return ResourceUseOliTransactionDao */
    public abstract ResourceUseOliTransactionDao getResourceUseOliTransactionDao();

    /**  Get the ResourceUseOliUserSessFileDao. @return ResourceUseOliUserSessFileDao */
    public abstract ResourceUseOliUserSessFileDao getResourceUseOliUserSessFileDao();

    /**  Get the ResourceUseOliUserSessDao. @return ResourceUseOliUserSessDao */
    public abstract ResourceUseOliUserSessDao getResourceUseOliUserSessDao();
    
    /**  Get the ResourceUseOliImporterDao. @return ResourceUseOliImporterDao */
    public abstract ResourceUseOliImporterDao getResourceUseOliImporterDao();
}
