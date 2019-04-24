/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.hibernate;

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


import edu.cmu.pslc.datashop.util.SpringContext;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliImporterDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliTransactionDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliTransactionFileDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliUserSessDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliUserSessFileDao;

/**
 * Factory to create hibernate DAOs specific to learnsphere.analysis.resourceuse.
 *
 * @author Hui Cheng
 * @version $Revision: 12891 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:41 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HibernateDaoFactory extends DaoFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the name of the analysis database.
     * @return the name of the analysis database
     */
    public String getAnalysisDatabaseName() {
        /*
         * 'analysisDataSource' is the name of the bean. It has to
         * agree with the applicationContext.xml if this is to be
         * used from a servlet, e.g., file upload in ResourceUseServlet.
         */
        return getDatabaseName("analysisDataSource");
    }

    /**
     * Returns the username and password for the analysis database.
     * @return a Map with the keys "user" and "password"
     */
    public Map<String, String> getAnalysisDatabaseLogin() {
        return getDatabaseLogin("analysisDataSource");
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
     * Returns the name of the source database.
     * @return the name of the source database
     */
    public String getSourceDatabaseName() {
        return getDatabaseName("sourceDataSource");
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
     * Get the hibernate/spring implementation of ResourceUseOliTransactionFileDao.
     * @return ResourceUseSourceDaoHibernate as ResourceUseOliTransactionFileDao
     */
    public ResourceUseOliTransactionFileDao getResourceUseOliTransactionFileDao() {
        return (ResourceUseOliTransactionFileDaoHibernate)ctx.getBean("resourceUseOliTransactionFileDao", ResourceUseOliTransactionFileDaoHibernate.class);
    }
    
    /**
     * Get the hibernate/spring implementation of ResourceUseOliTransactionDao.
     * @return ResourceUseOliTransactionDaoHibernate as ResourceUseOliTransactionDao
     */
    public ResourceUseOliTransactionDao getResourceUseOliTransactionDao() {
        return (ResourceUseOliTransactionDaoHibernate)ctx.getBean("resourceUseOliTransactionDao", ResourceUseOliTransactionDaoHibernate.class);
    }
  
    /**
     * Get the hibernate/spring implementation of ResourceUseOliUserSessFileDao.
     * @return ResourceUseOliUserSessFileDaoHibernate as ResourceUseOliUserSessFileDao
     */
    public ResourceUseOliUserSessFileDao getResourceUseOliUserSessFileDao() {
        return (ResourceUseOliUserSessFileDaoHibernate)ctx.getBean("resourceUseOliUserSessFileDao", ResourceUseOliUserSessFileDaoHibernate.class);
    }
    
    /**
     * Get the hibernate/spring implementation of ResourceUseOliUserSessDao.
     * @return ResourceUseOliUserSessDaoHibernate as ResourceUseOliUserSessDao
     */
    public ResourceUseOliUserSessDao getResourceUseOliUserSessDao() {
        return (ResourceUseOliUserSessDaoHibernate)ctx.getBean("resourceUseOliUserSessDao", ResourceUseOliUserSessDaoHibernate.class);
    }
    
    /**
     * Get the hibernate/spring implementation of ResourceUseOliImporterDao.
     * @return ResourceUseOliImporterDaoHibernate as ResourceUseOliImporterDao
     */
    public ResourceUseOliImporterDao getResourceUseOliImporterDao() {
        return (ResourceUseOliImporterDaoHibernate)ctx.getBean("resourceUseOliImporterDao", ResourceUseOliImporterDaoHibernate.class);
    }
}
