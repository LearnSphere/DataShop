/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.example;


/**
 * Defines all Examples and the concrete factories to get the concrete Examples.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 2783 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-05-04 13:00:06 -0400 (Thu, 04 May 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class ExampleFactory {

    /**
     * Hibernate Example factory.
     */
    public static final ExampleFactory HIBERNATE =
        new HibernateExampleFactory();

    /**
     * Default Example factory, which is hibernate.
     */
    public static final ExampleFactory DEFAULT = HIBERNATE;


    /**  Get the ErrorReportExample. @return ErrorReportExample */
    public abstract ExampleBean getExampleBean();

    /**  Get the ExampleDao. @return ExampleDao */
    public abstract ExampleDao getExampleDao();

}
