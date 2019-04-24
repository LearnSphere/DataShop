/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.example;

import org.springframework.context.ApplicationContext;

import edu.cmu.pslc.datashop.util.SpringContext;

/**
 *  Factory to create hibernate/spring implementation of the Examples.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12862 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2016-01-15 12:21:40 -0500 (Fri, 15 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class HibernateExampleFactory extends ExampleFactory {

    /** Spring Framework application context. */
    private static ApplicationContext ctx = SpringContext.getApplicationContext(null);

    /**  Get the ExampleBean. @return ExampleBean */
    public ExampleBean getExampleBean() {
        return (ExampleBean)ctx.getBean("exampleBean", ExampleBean.class);
    }

    /**  Get the ExampleDao. @return ExampleDao */
    public ExampleDao getExampleDao() {
        return (ExampleDao)ctx.getBean("exampleDao", ExampleDao.class);
    }

}
