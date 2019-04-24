/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao;

/**
 * Defines all the DAOs for OLI databases and the concrete factories to get the concrete DAOs.
 * Currently only accessing the Log database.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2050 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2005-12-23 12:03:11 -0500 (Fri, 23 Dec 2005) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class OliDaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final OliDaoFactory HIBERNATE =
        new edu.cmu.pslc.datashop.oli.dao.hibernate.OliHibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final OliDaoFactory DEFAULT = HIBERNATE;

    /**  Get the LogActionDao. @return LogActionDao */
    public abstract LogActionDao getLogActionDao();

    /**  Get the LogSessionDao. @return LogSessionDao */
    public abstract LogSessionDao getLogSessionDao();

}
