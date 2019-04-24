/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.sourcedb.dao.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cmu.datalab.dao.AnalysisImporterDao;
import edu.cmu.datalab.dao.hibernate.AnalysisImporterDaoHibernate;

import edu.cmu.pslc.datashop.sourcedb.dao.SourceDbDaoFactory;
import edu.cmu.pslc.datashop.sourcedb.dao.FlatFileImporterDao;
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
public class SourceDbHibernateDaoFactory extends SourceDbDaoFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the name of the source database.
     * @return the name of the source database
     */
    public String getSourceDatabaseName() {
        return getDatabaseName("sourceDataSource");
    }

    /**
     * Returns the username and password for the analysis database.
     * @return a Map with the keys "user" and "password"
     */
    public Map<String, String> getSourceDatabaseLogin() {
        return getDatabaseLogin("sourceDataSource");
    }

    /**
     * Returns the username and password properties for the specified beanName.
     * @param beanName The name of the bean in the ComboPooledDataSource in the
     * application context.
     * @return a Map with the keys "user" and "password"
     */
    private Map<String, String> getDatabaseLogin(String beanName) {
        DataSource dataSource =
            (DataSource)ctx.getBean(beanName, DataSource.class);
        Map<String, String> login = new HashMap<String, String>();

        if (dataSource instanceof ComboPooledDataSource) {
            ComboPooledDataSource cpds = (ComboPooledDataSource)dataSource;
            Properties props = cpds.getProperties();
            login.put("user", props.getProperty("user"));
            login.put("password", props.getProperty("password"));
        } else if (dataSource instanceof BasicDataSource) {
            BasicDataSource bds = (BasicDataSource)dataSource;
            login.put("user", bds.getUsername());
            login.put("password", bds.getPassword());
        }

        return login;
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
                logger.debug("DataSource URL = " + url);
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
     * @return FlatFileImporterDaoHibernate as FlatFileImporterDao
     */
    public FlatFileImporterDao getFlatFileImporterDao() {
        return (FlatFileImporterDaoHibernate)ctx.getBean("flatFileImporterDao",
                FlatFileImporterDaoHibernate.class);
    }

    //--------------------------------------------
    // DataLab DAOs.
    // Likely these will move into their own
    // factory at some point.
    //--------------------------------------------

    /**
     * Get the hibernate/spring implementation of AnalysisImporterDao.
     * @return AnalysisImporterDaoHibernate as AnalysisImporterDao
     */
    public AnalysisImporterDao getAnalysisImporterDao() {
        return (AnalysisImporterDaoHibernate)ctx.
            getBean("analysisImporterDao", AnalysisImporterDaoHibernate.class);
    }
}
