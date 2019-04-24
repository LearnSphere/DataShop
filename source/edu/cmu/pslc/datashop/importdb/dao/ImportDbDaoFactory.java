/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.dao;

/**
 * Defines all the DAOs for import_db databases and the concrete factories to get the concrete DAOs.
 *
 * @author Shanwen Yu
 * @version $Revision: 6771 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2011-03-29 09:55:06 -0400 (Tue, 29 Mar 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class ImportDbDaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final ImportDbDaoFactory HIBERNATE =
        new edu.cmu.pslc.datashop.importdb.dao.hibernate.ImportDbHibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final ImportDbDaoFactory DEFAULT = HIBERNATE;

    /**
     * Returns the name of the import_db database.
     * @return the name of the import_db database
     */
    public abstract String getImportDatabaseName();

    /**  Get the ImportFileInfoDao. @return ImportFileInfoDao */
    public abstract ImportFileInfoDao getImportFileInfoDao();

    /**  Get the ImportStatusDao. @return ImportStatusDao */
    public abstract ImportStatusDao getImportStatusDao();

}
