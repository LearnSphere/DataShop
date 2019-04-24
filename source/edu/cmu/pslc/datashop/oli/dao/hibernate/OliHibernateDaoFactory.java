/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao.hibernate;

import org.springframework.context.ApplicationContext;

import edu.cmu.pslc.datashop.oli.dao.LogActionDao;
import edu.cmu.pslc.datashop.oli.dao.LogSessionDao;
import edu.cmu.pslc.datashop.oli.dao.OliDaoFactory;
import edu.cmu.pslc.datashop.util.SpringContext;

/**
 * Factory to create hibernate OLI DAO's.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12862 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-01-15 12:21:40 -0500 (Fri, 15 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliHibernateDaoFactory extends OliDaoFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /**
     * Get the hibernate/spring implementation of LogActionDao.
     * @return LogActionDaoHibernate as LogActionDao
     */
    public LogActionDao getLogActionDao() {
        return (LogActionDaoHibernate)ctx.getBean("logActionDao",
                LogActionDaoHibernate.class);
    }

    /**
     * Get the hibernate/spring implementation of LogSessionDao.
     * @return LogSessionDaoHibernate as LogSessionDao
     */
    public LogSessionDao getLogSessionDao() {
        return (LogSessionDaoHibernate)ctx.getBean("logSessionDao",
               LogSessionDaoHibernate.class);
    }

}
