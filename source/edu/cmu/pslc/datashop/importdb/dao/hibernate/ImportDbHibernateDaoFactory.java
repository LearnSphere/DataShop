/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.importdb.dao.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cmu.pslc.datashop.importdb.dao.ImportFileInfoDao;
import edu.cmu.pslc.datashop.importdb.dao.ImportStatusDao;
import edu.cmu.pslc.datashop.importdb.dao.ImportDbDaoFactory;
import edu.cmu.pslc.datashop.util.SpringContext;

/**
 * Factory to create hibernate FFI DAO's.
 *
 * @author Shanwen Yu
 * @version $Revision: 12862 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-01-15 12:21:40 -0500 (Fri, 15 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportDbHibernateDaoFactory extends ImportDbDaoFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the name of the source database.
     * @return the name of the source database
     */
    public String getImportDatabaseName() {
        return getDatabaseName("importDataSource");
    }

    /**
     * Returns the database name for use in Native SQL queries.
     * @param beanName name of a data source which has a URL property
     * @return the database name
     */
    private String getDatabaseName(String beanName) {
        DataSource dataSource =
            (DataSource)ctx.getBean(beanName, DataSource.class);
        String url = null;

        if (dataSource instanceof ComboPooledDataSource) {
            ComboPooledDataSource cpds = (ComboPooledDataSource)dataSource;
            url = cpds.getJdbcUrl();
        } else if (dataSource instanceof BasicDataSource) {
            BasicDataSource bds = (BasicDataSource)dataSource;
            url = bds.getUrl();
        } else if (dataSource != null) {
            try {
                Connection c = dataSource.getConnection();
                url = c.getMetaData().getURL();
                if (c != null) { c.close(); }

                // Strip off any trailing connection args from URL.
                int lastIndex = url.indexOf('?');
                url = (lastIndex > 0) ? url.substring(0, lastIndex) : url;
            } catch (SQLException se) {
                logger.error("Failed to query Datasource URL", se);
            }
        }

        if (url != null) {
            return url.substring(url.lastIndexOf('/') + 1);
        } else {
            return null;
        }
    }

    /**
     * Get the hibernate/spring implementation of ImportFileInfoDao.
     * @return ImportFileInfoDaoHibernate as ImportFileInfoDao
     */
    public ImportFileInfoDao getImportFileInfoDao() {
        return (ImportFileInfoDaoHibernate)ctx.getBean("importFileInfoDao",
                ImportFileInfoDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ImportStatusDao.
     * @return ImportStatusDaoHibernate as ImportStatusDao
     */
    public ImportStatusDao getImportStatusDao() {
        return (ImportStatusDaoHibernate)ctx.getBean("importStatusDao",
               ImportStatusDaoHibernate.class);
    }

}
