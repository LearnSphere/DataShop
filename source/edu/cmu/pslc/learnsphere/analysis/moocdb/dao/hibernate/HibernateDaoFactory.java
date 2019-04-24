/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.dao.hibernate;

import java.net.URL;
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
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.CourseraDbsRestoreDao;
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.MOOCdbDao;
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.FeatureExtractionDao;
import edu.cmu.pslc.learnsphere.analysis.moocdb.dao.DaoFactory;

/**
 * Factory to create hibernate DAOs specific to learnsphere.analysis.moocdb.
 *
 * @author Hui Cheng
 * @version $Revision: 14112 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-06-13 16:28:42 -0400 (Tue, 13 Jun 2017) $
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
     * Returns the host/port of the analysis database.
     * @return the name of the analysis database
     */
    public Map<String, String> getAnalysisDatabaseHostPort() {
        /*
         * 'analysisDataSource' is the name of the bean. It has to
         * agree with the applicationContext.xml if this is to be
         * used from a servlet, e.g., file upload in ResourceUseServlet.
         */
        return getDatabaseHostPort("analysisDataSource");
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
     * Returns the host and port for the specified beanName.
     * @param beanName The name of a data source which has a URL property
     * @return a Map with the keys "host" and "port"
     * 
     */
    private Map<String, String> getDatabaseHostPort(String beanName) {
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
                    } catch (SQLException se) {
                            logger.error("Failed to query Datasource URL", se);
                    }
            }
            logger.debug("DataSource URL = " + url);
            if (url != null) {
                    String partialUrl = url.substring(url.indexOf("//") + 2);
                    int indComma = partialUrl.indexOf(":");
                    int indSlash = partialUrl.indexOf("/");
                    Map<String, String> hostPortMap = new HashMap<String, String>();
                    String host = partialUrl.substring(0, indComma);
                    String port = partialUrl.substring(indComma + 1, indSlash);
                    if (host.equalsIgnoreCase("localhost"));
                            host = "127.0.0.1";
                    hostPortMap.put("host", host);
                    hostPortMap.put("port", port);
                    logger.debug("DataSource port and host: " + hostPortMap);
                    return hostPortMap;
            } 
            return null;
    }

    
    /**  Get the Dao. @return CourseraDbsRestoreDao 
     * @param <CourseraDbsRestoreDao>*/
    public CourseraDbsRestoreDao getCourseraDbsRestoreDao() {
            return (CourseraDbsRestoreDaoHibernate)ctx.getBean("courseraDbsRestoreDao", CourseraDbsRestoreDaoHibernate.class);
    }

    /**  Get the MoocdbDao. @return MoocdbDao */
    public MOOCdbDao getMOOCdbDao() {
            return (MOOCdbDaoHibernate)ctx.getBean("moocdbDao", MOOCdbDaoHibernate.class);
    }
    
    /**  Get the FeatureExtractionDao. @return FeatureExtractionDao */
    public FeatureExtractionDao getFeatureExtractionDao() {
            return (FeatureExtractionDaoHibernate)ctx.getBean("featureExtractionDao", FeatureExtractionDaoHibernate.class);
    }

}
