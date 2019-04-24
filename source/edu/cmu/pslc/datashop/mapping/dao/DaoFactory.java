/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.mapping.dao;

import java.util.Map;

/**
 * Defines all mapping DAOs and the concrete factories to get the concrete DAOs.
 *
 * @author Cindy Tipper
 * @version $Revision: 14294 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-29 09:42:01 -0400 (Fri, 29 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class DaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final DaoFactory HIBERNATE =
        new edu.cmu.pslc.datashop.mapping.dao.hibernate.HibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final DaoFactory DEFAULT = HIBERNATE;

    /**
     * Returns the name of the mapping database.
     * @return the name of the mapping database
     */
    public abstract String getMappingDatabaseName();

    /**
     * Returns the mapping database login and password.
     * @return Map key-values for "user" and "password"
     */
    public abstract Map<String, String> getMappingDatabaseLogin();

    /**  Get the StudentDao. @return StudentDao */
    public abstract StudentDao getStudentDao();
}
