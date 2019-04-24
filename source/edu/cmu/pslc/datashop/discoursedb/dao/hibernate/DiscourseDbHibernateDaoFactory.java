/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Srcright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cmu.pslc.datashop.discoursedb.dao.AnnotationAggregateDao;
import edu.cmu.pslc.datashop.discoursedb.dao.AnnotationInstanceDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContentDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDao;
import edu.cmu.pslc.datashop.discoursedb.dao.ContributionDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourceInstanceDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DataSourcesDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDiscoursePartMapDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscoursePartRelationDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseRelationDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DUserDiscourseMapDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;

import edu.cmu.pslc.datashop.util.SpringContext;

/**
 * Factory to create hibernate discoursedb DAOs.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDbHibernateDaoFactory extends DiscourseDbDaoFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the name of the source database.
     * @return the name of the source database
     */
    public String getDiscourseDatabaseName() {
        return getDatabaseName("discourseDataSource");
    }

    /**
     * Returns the name of the source src database.
     * @return the name of the source src database
     */
    public String getDiscourseSrcDatabaseName() {
        return getDatabaseName("discourseSrcDataSource");
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
     * Get the hibernate/spring implementation of DiscourseDao.
     * @param useSrc flag indicating use src db
     * @return DiscourseDaoHibernate as DiscourseDao
     */
    public DiscourseDao getDiscourseDao(Boolean useSrc) {
        return (DiscourseDaoHibernate)ctx.
            getBean(getDaoName("discourse", useSrc), DiscourseDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DataSourcesDao
     * @param useSrc flag indicating use src db
     * @return DataSourcesDaoHibernate as DataSourcesDao
     */
    public DataSourcesDao getDataSourcesDao(Boolean useSrc) {
        return (DataSourcesDaoHibernate)ctx.
            getBean(getDaoName("dataSources", useSrc), DataSourcesDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DataSourceInstanceDao
     * @param useSrc flag indicating use src db
     * @return DataSourceInstanceDaoHibernate as DataSourceInstanceDao
     */
    public DataSourceInstanceDao getDataSourceInstanceDao(Boolean useSrc) {
        return (DataSourceInstanceDaoHibernate)ctx.
            getBean(getDaoName("dataSourceInstance", useSrc),
                    DataSourceInstanceDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DUserDao.
     * @param useSrc flag indicating use src db
     * @return DUserDaoHibernate as DUserDao
     */
    public DUserDao getDUserDao(Boolean useSrc) {
        return (DUserDaoHibernate)ctx.
            getBean(getDaoName("dUser", useSrc), DUserDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DUserDiscourseMapDao.
     * @param useSrc flag indicating use src db
     * @return DUserDiscourseMapDaoHibernate as DUserDiscourseMapDao
     */
    public DUserDiscourseMapDao getDUserDiscourseMapDao(Boolean useSrc) {
        return (DUserDiscourseMapDaoHibernate)ctx.
            getBean(getDaoName("dUserDiscourseMap", useSrc), DUserDiscourseMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DiscoursePartDao.
     * @param useSrc flag indicating use src db
     * @return DiscoursePartDaoHibernate
     */
    public DiscoursePartDao getDiscoursePartDao(Boolean useSrc) {
        return (DiscoursePartDaoHibernate)ctx.
            getBean(getDaoName("discoursePart", useSrc), DiscoursePartDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DiscourseDiscoursePartMapDao.
     * @param useSrc flag indicating use src db
     * @return DiscourseDiscoursePartMapDaoHibernate
     */
    public DiscourseDiscoursePartMapDao getDiscourseDiscoursePartMapDao(Boolean useSrc) {
        return (DiscourseDiscoursePartMapDaoHibernate)ctx.
            getBean(getDaoName("discourseDiscoursePartMap", useSrc),
                    DiscourseDiscoursePartMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ContentDao.
     * @param useSrc flag indicating use src db
     * @return ContentDaoHibernate as ContentDao
     */
    public ContentDao getContentDao(Boolean useSrc) {
        return (ContentDaoHibernate)ctx.
            getBean(getDaoName("content", useSrc), ContentDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ContributionDao.
     * @param useSrc flag indicating use src db
     * @return ContributionDaoHibernate as ContributionDao
     */
    public ContributionDao getContributionDao(Boolean useSrc) {
        return (ContributionDaoHibernate)ctx.
            getBean(getDaoName("contribution", useSrc), ContributionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of ContributionDiscoursePartMapDao.
     * @param useSrc flag indicating use src db
     * @return ContributionDiscoursePartMapDaoHibernate
     */
    public ContributionDiscoursePartMapDao getContributionDiscoursePartMapDao(Boolean useSrc) {
        return (ContributionDiscoursePartMapDaoHibernate)ctx.
            getBean(getDaoName("contributionDiscoursePartMap", useSrc),
                    ContributionDiscoursePartMapDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DiscourseRelationDao.
     * @param useSrc flag indicating use src db
     * @return DiscourseRelationDaoHibernate
     */
    public DiscourseRelationDao getDiscourseRelationDao(Boolean useSrc) {
        return (DiscourseRelationDaoHibernate)ctx.
            getBean(getDaoName("discourseRelation", useSrc), DiscourseRelationDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of DiscoursePartRelationDao.
     * @param useSrc flag indicating use src db
     * @return DiscoursePartRelationDaoHibernate
     */
    public DiscoursePartRelationDao getDiscoursePartRelationDao(Boolean useSrc) {
        return (DiscoursePartRelationDaoHibernate)ctx.
            getBean(getDaoName("discoursePartRelation", useSrc),
                    DiscoursePartRelationDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AnnotationAggregateDao
     * @param useSrc flag indicating use src db
     * @return AnnotationAggregateDaoHibernate as AnnotationAggregateDao
     */
    public AnnotationAggregateDao getAnnotationAggregateDao(Boolean useSrc) {
        return (AnnotationAggregateDaoHibernate)ctx.
            getBean(getDaoName("annotationAggregate", useSrc),
                    AnnotationAggregateDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of AnnotationInstanceDao
     * @param useSrc flag indicating use src db
     * @return AnnotationInstanceDaoHibernate as AnnotationInstanceDao
     */
    public AnnotationInstanceDao getAnnotationInstanceDao(Boolean useSrc) {
        return (AnnotationInstanceDaoHibernate)ctx.
            getBean(getDaoName("annotationInstance", useSrc),
                    AnnotationInstanceDaoHibernate.class);
    }

    //--------------------------------------------------------
    // Versions of the above that default to useSrc = false.
    //--------------------------------------------------------
    /**
     * Get the hibernate/spring implementation of DiscourseDao.
     * @return DiscourseDaoHibernate as DiscourseDao
     */
    public DiscourseDao getDiscourseDao() { return getDiscourseDao(false); }

    /**
     * Get the hibernate/spring implementation of DataSourcesDao
     * @return DataSourcesDaoHibernate as DataSourcesDao
     */
    public DataSourcesDao getDataSourcesDao() { return getDataSourcesDao(false); }

    /**
     * Get the hibernate/spring implementation of DataSourceInstanceDao
     * @return DataSourceInstanceDaoHibernate as DataSourceInstanceDao
     */
    public DataSourceInstanceDao getDataSourceInstanceDao() {
        return getDataSourceInstanceDao(false);
    }

    /**
     * Get the hibernate/spring implementation of DUserDao.
     * @return DUserDaoHibernate as DUserDao
     */
    public DUserDao getDUserDao() { return getDUserDao(false); }

    /**
     * Get the hibernate/spring implementation of DUserDiscourseMapDao.
     * @return DUserDiscourseMapDaoHibernate as DUserDiscourseMapDao
     */
    public DUserDiscourseMapDao getDUserDiscourseMapDao() { return getDUserDiscourseMapDao(false); }

    /**
     * Get the hibernate/spring implementation of DiscoursePartDao.
     * @return DiscoursePartDaoHibernate
     */
    public DiscoursePartDao getDiscoursePartDao() { return getDiscoursePartDao(false); }

    /**
     * Get the hibernate/spring implementation of DiscourseDiscoursePartMapDao.
     * @return DiscourseDiscoursePartMapDaoHibernate
     */
    public DiscourseDiscoursePartMapDao getDiscourseDiscoursePartMapDao() {
        return getDiscourseDiscoursePartMapDao(false);
    }

    /**
     * Get the hibernate/spring implementation of ContentDao.
     * @return ContentDaoHibernate as ContentDao
     */
    public ContentDao getContentDao() { return getContentDao(false); }

    /**
     * Get the hibernate/spring implementation of ContributionDao.
     * @return ContributionDaoHibernate as ContributionDao
     */
    public ContributionDao getContributionDao() { return getContributionDao(false); }

    /**
     * Get the hibernate/spring implementation of ContributionDiscoursePartMapDao.
     * @return ContributionDiscoursePartMapDaoHibernate
     */
    public ContributionDiscoursePartMapDao getContributionDiscoursePartMapDao() {
        return getContributionDiscoursePartMapDao(false);
    }

    /**
     * Get the hibernate/spring implementation of DiscourseRelationDao.
     * @return DiscourseRelationDaoHibernate
     */
    public DiscourseRelationDao getDiscourseRelationDao() { return getDiscourseRelationDao(false); }

    /**
     * Get the hibernate/spring implementation of DiscoursePartRelationDao.
     * @return DiscoursePartRelationDaoHibernate
     */
    public DiscoursePartRelationDao getDiscoursePartRelationDao() {
        return getDiscoursePartRelationDao(false);
    }

    /**
     * Get the hibernate/spring implementation of AnnotationAggregateDao
     * @return AnnotationAggregateDaoHibernate as AnnotationAggregateDao
     */
    public AnnotationAggregateDao getAnnotationAggregateDao() {
        return getAnnotationAggregateDao(false);
    }

    /**
     * Get the hibernate/spring implementation of AnnotationInstanceDao
     * @return AnnotationInstanceDaoHibernate as AnnotationInstanceDao
     */
    public AnnotationInstanceDao getAnnotationInstanceDao() {
        return getAnnotationInstanceDao(false);
    }

    /**
     * Helper method to generate DAO name from header.
     * @param startDaoName the first portion of the DAO name
     * @param useSrc flag indicating if src DB is wanted
     * @return the resulting DAO name
     */
    private String getDaoName(String startDaoName, Boolean useSrc) {
        startDaoName += (useSrc ? "SrcDao" : "Dao");
        return startDaoName;
    }
}
