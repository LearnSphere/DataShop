/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao;

import java.util.Map;

/**
 * Defines all learnsphere.analysis.resourceuse.coursera DAOs and the concrete factories to get the concrete DAOs.
 *
 * @author Hui Cheng
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class CourseraDbDaoFactory {

    /**
     * Hibernate DAO factory.
     */
    public static final CourseraDbDaoFactory HIBERNATE =
        new edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.hibernate.CourseraDbHibernateDaoFactory();

    /**
     * Default DAO factory, which is hibernate.
     */
    public static final CourseraDbDaoFactory DEFAULT = HIBERNATE;

    /**
     * Returns the name of the coursera database.
     * @return the name of the coursera database
     */
    public abstract String getCourseraDatabaseName();

    /**  Get the Dao. @return CourseraClickstreamDao */
    public abstract CourseraClickstreamDao getCourseraClickstreamDao();

    /**  Get the CourseraClickstreamVideoDao. @return CourseraClickstreamVideoDao */
    public abstract CourseraClickstreamVideoDao getCourseraClickstreamVideoDao();

}
