/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.hibernate;

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
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.CourseraDbDaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.CourseraClickstreamDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.CourseraClickstreamVideoDao;
/**
 * Factory to create hibernate DAOs specific to learnsphere.analysis.resourceuse.
 *
 * @author Hui Cheng
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CourseraDbHibernateDaoFactory extends CourseraDbDaoFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the name of the analysis database.
     * @return the name of the analysis database
     */
    public String getCourseraDatabaseName() {
        /*
         * 'courseraDataSource' is the name of the bean. It has to
         * agree with the applicationContext.xml if this is to be
         * used from a servlet
         */
        return getDatabaseName("courseraDataSource");
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
     * Get the hibernate/spring implementation of CourseraClickstreamDao.
     * @return CourseraClickstreamDaoHibernate as CourseraClickstreamDao
     */
    public CourseraClickstreamDao getCourseraClickstreamDao() {
        return (CourseraClickstreamDaoHibernate)ctx.getBean("courseraClickstreamDao", CourseraClickstreamDaoHibernate.class);
    }
    
    /**
     * Get the hibernate/spring implementation of courseraClickstreamVideoDao.
     * @return courseraClickstreamVideoDaoHibernate as courseraClickstreamVideoDao
     */
    public CourseraClickstreamVideoDao getCourseraClickstreamVideoDao() {
        return (CourseraClickstreamVideoDaoHibernate)ctx.getBean("courseraClickstreamVideoDao", CourseraClickstreamVideoDaoHibernate.class);
    }

}
