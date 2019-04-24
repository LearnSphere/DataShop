/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * Update (re-hash) the anon_user_id for students in a given dataset.
 *
 * @author Cindy Tipper
 * @version $Revision: 14294 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-29 09:42:01 -0400 (Fri, 29 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UpdateAnonStudentIds {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Flag indicating whether or not to output. */
    private boolean verbose = false;

    /**
     * write student actual id and anonymized id to a file
     * @param filename file name of the input file
     */
    private void doUpdate(Integer datasetId) {

        try {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem dataset = datasetDao.find(datasetId);

            // Get list of students for specified dataset.
            StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
            edu.cmu.pslc.datashop.mapping.dao.StudentDao mappedStudentDao
                = edu.cmu.pslc.datashop.mapping.dao.DaoFactory.DEFAULT.getStudentDao();
            List<StudentItem> studentList = studentDao.find(dataset);
            if (verbose) {
                logger.info("Found " + studentList.size() + " students to update.");
            }

            int count = 0;
            for (StudentItem stu : studentList) {
                stu = encryptUserId(stu);
                String actualUserId = stu.getActualUserId();

                // Put anonUserId in place of actualUserId because:
                // (1) we don't want to persist actualUserId in analysis_db
                // (2) the actualUserId must be unique so it cannot be empty
                stu.setActualUserId(null);
                studentDao.saveOrUpdate(stu);

                edu.cmu.pslc.datashop.mapping.item.StudentItem mappedStudentItem =
                    getMappedStudentItem(stu);
                mappedStudentItem.setActualUserId(actualUserId);
                mappedStudentDao.saveOrUpdate(mappedStudentItem);

                if (verbose) {
                    logger.debug(count + ". Anonymized student id '"
                                 + stu.getActualUserId() + "' -> " + stu.getAnonymousUserId());
                }
                count++;
            }

            if (verbose) {
                logger.info("Finished updating students for dataset id: " + datasetId);
            }
         } catch (Exception e) {
              logger.error("Error: " + e.getMessage());
         }
    }

    /**
     * Helper method to encrypt student id and check for duplicates.
     *
     * @param student the StudentItem
     * @return StudentItem
     */
    private StudentItem encryptUserId(StudentItem student) {

        student.encryptUserId();

        // Ensure the generate anon_user_id doesn't already exist. Rare but possible.
        StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
        List<StudentItem> matches = (List<StudentItem>)studentDao.findByAnonId(student.getAnonymousUserId());
        while (matches.size() > 0) {
            student.encryptUserId();
            matches = (List<StudentItem>)studentDao.findByAnonId(student.getAnonymousUserId());
        }

        return student;
    }

    /**
     * Convert original StudentItem to one suitable for the mapping_db.
     * @param item the original student item
     * @return edu.cmu.pslc.datashop.mapping.item.StudentItem
     */
    private edu.cmu.pslc.datashop.mapping.item.StudentItem getMappedStudentItem(StudentItem item) {
        edu.cmu.pslc.datashop.mapping.item.StudentItem result = 
            new edu.cmu.pslc.datashop.mapping.item.StudentItem();

        result.setActualUserId(item.getActualUserId());
        result.setAnonymousUserId(item.getAnonymousUserId());

        return result;
    }

    /**
     * Handle the command line arguments.
     * @param args - command line arguments passed into main
     */
    protected Integer handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("Error: the dataset ID must be specified.");
            displayUsage();
            System.exit(1);
        }

        Integer datasetId = null;

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("-h")) || (args[i].equals("-help"))) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-d") || (args[i].equals("-dataset"))) {
                if (++i < args.length) {
                    if (args[i] != null) {
                        datasetId = Integer.parseInt(args[i]);
                    }
                } else {
                    System.err.println("Error: a dataset ID must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if ((args[i].equals("-v")) || (args[i].equals("-verbose"))) {
                verbose = true;
            } else {
                System.err.println("Error: unsupported command line argument: " + args[i]);
                displayUsage();
                System.exit(1);
            }
        }

        if (datasetId == null) {
            System.err.println("Error: a dataset ID must be specified.");
            displayUsage();
            System.exit(1);
        }

        return datasetId;
    }

    /**
     * Display the usage of this utility.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..." + " UpdateAnonStudentIds ");
        System.err.println("Option descriptions:");
        System.err.println("\t-v       \t verbose");
        System.err.println("\t-h       \t usage info");
        System.err.println("\t-dataset       \t ID of dataset to update");
    }

    /**
     * Run the Anonymizer.
     * USAGE: java -classpath ... Anonymizer
     * @param args command line arguments.
     */
    public static void main(String[] args) {

        Logger logger = Logger.getLogger("UpdateAnonStudentIds.main");

        logger.info("UpdateAnonStudentIds starting...");

        try {
            UpdateAnonStudentIds updater = new UpdateAnonStudentIds();
            // Handle the command line options
            Integer datasetId = updater.handleOptions(args);
            updater.doUpdate(datasetId);
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        }

        logger.info("UpdateAnonStudentIds done.");
    }

}
