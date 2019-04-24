/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

/**
 * Defines all the DAOs for discoursedb database and the concrete factories
 * to get the concrete DAOs.
 *
 * @author Cindy Tipper
 * @version $Revision: 13055 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-06 12:14:07 -0400 (Wed, 06 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class DiscourseDbDaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final DiscourseDbDaoFactory HIBERNATE =
        new edu.cmu.pslc.datashop.discoursedb.dao.hibernate.DiscourseDbHibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final DiscourseDbDaoFactory DEFAULT = HIBERNATE;

    /**
     * Returns the name of the discoursedb database.
     * @return the name of the discoursedb database
     */
    public abstract String getDiscourseDatabaseName();

    /**
     * Returns the name of the discoursedb src database.
     * @return the name of the discoursedb src database
     */
    public abstract String getDiscourseSrcDatabaseName();

    /**
     * Get the DiscourseDao.
     * @return DiscourseDao
     */
    public abstract DiscourseDao getDiscourseDao();

    /**
     * Get the DataSourcesDao
     * @return DataSourcesDao
     */
    public abstract DataSourcesDao getDataSourcesDao();

    /**
     * Get the DataSourceInstanceDao
     * @return DataSourceInstanceDao
     */
    public abstract DataSourceInstanceDao getDataSourceInstanceDao();

    /**
     * Get the DUserDao
     * @return DUserDao
     */
    public abstract DUserDao getDUserDao();

    /**
     * Get the DUserDiscourseMapDao.
     * @return DUserDiscourseMapDao
     */
    public abstract DUserDiscourseMapDao getDUserDiscourseMapDao();

    /**
     * Get the DiscoursePartDao.
     * @return DiscoursePartDao
     */
    public abstract DiscoursePartDao getDiscoursePartDao();

    /**
     * Get the DiscourseDiscoursePartMapDao.
     * @return DiscourseDiscoursePartMapDao
     */
    public abstract DiscourseDiscoursePartMapDao getDiscourseDiscoursePartMapDao();

    /**
     * Get the ContentDao.
     * @return ContentDao
     */
    public abstract ContentDao getContentDao();

    /**
     * Get the ContributionDao.
     * @return ContributionDao
     */
    public abstract ContributionDao getContributionDao();

    /**
     * Get the ContributionDiscoursePartMapDao.
     * @return ContributionDiscoursePartMapDao
     */
    public abstract ContributionDiscoursePartMapDao getContributionDiscoursePartMapDao();

    /**
     * Get the DiscourseRelationDao.
     * @return DiscourseRelationDao
     */
    public abstract DiscourseRelationDao getDiscourseRelationDao();

    /**
     * Get the DiscoursePartRelationDao.
     * @return DiscoursePartRelationDao
     */
    public abstract DiscoursePartRelationDao getDiscoursePartRelationDao();

    /**
     * Get the AnnotationAggregateDao
     * @return AnnotationAggregateDao
     */
    public abstract AnnotationAggregateDao getAnnotationAggregateDao();

    /**
     * Get the AnnotationInstanceDao
     * @return AnnotationInstanceDao
     */
    public abstract AnnotationInstanceDao getAnnotationInstanceDao();

    //----------------------------------------------
    // Versions of the above that take a flag,
    // allowing users to request 'copy' db be used.
    //----------------------------------------------

    /**
     * Get the DiscourseDao.
     * @param useSrc flag indicating use copy db
     * @return DiscourseDao
     */
    public abstract DiscourseDao getDiscourseDao(Boolean useSrc);

    /**
     * Get the DataSourcesDao
     * @param useSrc flag indicating use copy db
     * @return DataSourcesDao
     */
    public abstract DataSourcesDao getDataSourcesDao(Boolean useSrc);

    /**
     * Get the DataSourceInstanceDao
     * @param useSrc flag indicating use copy db
     * @return DataSourceInstanceDao
     */
    public abstract DataSourceInstanceDao getDataSourceInstanceDao(Boolean useSrc);

    /**
     * Get the DUserDao
     * @param useSrc flag indicating use copy db
     * @return DUserDao
     */
    public abstract DUserDao getDUserDao(Boolean useSrc);

    /**
     * Get the DUserDiscourseMapDao.
     * @param useSrc flag indicating use copy db
     * @return DUserDiscourseMapDao
     */
    public abstract DUserDiscourseMapDao getDUserDiscourseMapDao(Boolean useSrc);

    /**
     * Get the DiscoursePartDao.
     * @param useSrc flag indicating use copy db
     * @return DiscoursePartDao
     */
    public abstract DiscoursePartDao getDiscoursePartDao(Boolean useSrc);

    /**
     * Get the DiscourseDiscoursePartMapDao.
     * @param useSrc flag indicating use copy db
     * @return DiscourseDiscoursePartMapDao
     */
    public abstract DiscourseDiscoursePartMapDao getDiscourseDiscoursePartMapDao(Boolean useSrc);

    /**
     * Get the ContentDao.
     * @param useSrc flag indicating use copy db
     * @return ContentDao
     */
    public abstract ContentDao getContentDao(Boolean useSrc);

    /**
     * Get the ContributionDao.
     * @param useSrc flag indicating use copy db
     * @return ContributionDao
     */
    public abstract ContributionDao getContributionDao(Boolean useSrc);

    /**
     * Get the ContributionDiscoursePartMapDao.
     * @param useSrc flag indicating use copy db
     * @return ContributionDiscoursePartMapDao
     */
    public abstract ContributionDiscoursePartMapDao getContributionDiscoursePartMapDao(Boolean useSrc);

    /**
     * Get the DiscourseRelationDao.
     * @param useSrc flag indicating use copy db
     * @return DiscourseRelationDao
     */
    public abstract DiscourseRelationDao getDiscourseRelationDao(Boolean useSrc);

    /**
     * Get the DiscoursePartRelationDao.
     * @param useSrc flag indicating use copy db
     * @return DiscoursePartRelationDao
     */
    public abstract DiscoursePartRelationDao getDiscoursePartRelationDao(Boolean useSrc);

    /**
     * Get the AnnotationAggregateDao
     * @param useSrc flag indicating use copy db
     * @return AnnotationAggregateDao
     */
    public abstract AnnotationAggregateDao getAnnotationAggregateDao(Boolean useSrc);

    /**
     * Get the AnnotationInstanceDao
     * @param useSrc flag indicating use copy db
     * @return AnnotationInstanceDao
     */
    public abstract AnnotationInstanceDao getAnnotationInstanceDao(Boolean useSrc);
}
