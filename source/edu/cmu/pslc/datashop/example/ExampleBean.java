/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.example;

import org.apache.log4j.Logger;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * This class simply demonstrates how to create data in the database
 * using a Data Access Object (DAO) package using a transaction.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2783 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-05-04 13:00:06 -0400 (Thu, 04 May 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExampleBean {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** Default constructor. */
    public ExampleBean() {
    }

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.*/
    public void setTransactionTemplate (TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Gets transactionTemplate.
     * @return TransactionTemplate*/
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    /**
     * Save data to the database.
     */
    public void save() {

        String schoolName = "Allegheny Middle School";
        String instName = "Mr. Zimmerman";
        String studentName = "Jon Smith";

        ExampleTransactionCallback cb
                = new ExampleTransactionCallback(
                        schoolName, instName, studentName);

        StudentItem studentItem =  (StudentItem)transactionTemplate.execute(cb);

        if (studentItem == null) {
            logger.error("Oh no");
        }
    }

    /**
     * Run the Tutor Message Converter.
     * USAGE: java -classpath ... TutorMessageConverter [-dataset dataset_name]
     *     [-dataset level datasetlevel_name]
     *     [-problem problem_name]
     * @param args - command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("ExampleBean.main");
        logger.info("ExampleBean starting...");
        try {
            ExampleBean exampleBean = ExampleFactory.DEFAULT.getExampleBean();
            exampleBean.save();

            // show how a custom dao can be created
            ExampleDao exampleDao = ExampleFactory.DEFAULT.getExampleDao();
            Integer datasetId = new Integer(1);
            String unitName = "ImpUnworked";
            String sectionName = "Post-Test";
            String problemName = "Questionnaire2";

            ProblemItem problemItem = exampleDao.findProblem(
                    datasetId, unitName, sectionName, problemName);

            logger.info("Problem is " + problemItem);

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        }
        logger.info("ExampleBean done.");

    } // end main

}
