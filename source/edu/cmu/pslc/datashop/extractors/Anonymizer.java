/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * Utility to check make sure all the anonymized fields in the DB are filled in.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14294 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-29 09:42:01 -0400 (Fri, 29 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class Anonymizer {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Flag indicating whether or not to output. */
    private boolean verbose = false;
    /** File name of the file to be anonymized. */
    private static String fileToAnonymize = "";
    /** A list students. */
    private List<StudentItem> anonymizedStudentList;
    /**
     * Checks each student to make sure the anonymous id has been filled in.
     */
    private void anonymize() {
        StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
        edu.cmu.pslc.datashop.mapping.dao.StudentDao mappedStudentDao
            = edu.cmu.pslc.datashop.mapping.dao.DaoFactory.DEFAULT.getStudentDao();

        List studentList = studentDao.findAll();

        int count = 0;
        logger.info("Total number of students: " + studentList.size());

        for (Iterator it = studentList.iterator(); it.hasNext();) {
            StudentItem studentItem = (StudentItem)it.next();
            studentItem = studentDao.get((Long)studentItem.getId());
            String anonUserId = studentItem.getAnonymousUserId();
            if (anonUserId == null || anonUserId.length() == 0) {
                studentItem = encryptUserId(studentItem);
                // The anon_user_id is only set in mapping_db
                mappedStudentDao.saveOrUpdate(getMappedStudentItem(studentItem));
                if (verbose) {
                    logger.info(count + ". Anonymized student id '"
                            + studentItem.getActualUserId()
                            + "' -> " + studentItem.getAnonymousUserId());
                }
                count++;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(count + ". Not anonymizing student id "
                        + studentItem.getActualUserId());
                }
            }
        }

        logger.info("Anonymized students: " + count);
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
        result.setOriginalId((Long)item.getId());

        return result;
    }

    /**
     * Anonymize a certain group of students given a file.
     * @param filename file name which includes all the actual_user_id to be anonymized
     */
    private void anonymizeInFile(String filename) {

        StudentDao studentDao = DaoFactory.DEFAULT.getStudentDao();
        edu.cmu.pslc.datashop.mapping.dao.StudentDao mappedStudentDao
            = edu.cmu.pslc.datashop.mapping.dao.DaoFactory.DEFAULT.getStudentDao();


        StudentItem studentItem = null;
        Collection<StudentItem> unAnonStudList = null;
        List<StudentItem> anonStudList = new ArrayList();
        try {
            File inputFile = new File(filename);
            if (!(inputFile.exists())) {
                logger.info("inputfile " + filename + " does not exist");
            } else {
                FileInputStream fstream = new FileInputStream(filename);
                DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                //Read File Line By Line
                while ((strLine = br.readLine()) != null)   {
                    // Find match student in the table, one student per line
                    unAnonStudList = studentDao.find(strLine.trim());
                    // Check if the student already exists
                    if ((unAnonStudList != null) && (unAnonStudList.size() == 1)) {
                        studentItem = unAnonStudList.iterator().next();
                        edu.cmu.pslc.datashop.mapping.item.StudentItem mappedStudentItem
                            = mappedStudentDao.findByOriginalId((Long)studentItem.getId());
                        mappedStudentItem = mappedStudentDao.get((Long)mappedStudentItem.getId());
                        // If anon_user_id is null, encrypt id and save the student item
                        if (mappedStudentItem.getAnonymousUserId() == null) {
                            mappedStudentItem.encryptUserId();
                            mappedStudentDao.saveOrUpdate(mappedStudentItem);
                            if (verbose) {
                                logger.info("Anonymized student id '" + studentItem.getActualUserId()
                                            + "' -> " + studentItem.getAnonymousUserId());
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Not anonymizing student " + strLine.trim());
                            }
                        }
                    } else {
                        // If the student doesn't exist in the table, create a new student item
                        if (!strLine.equals("")) {
                            studentItem = new StudentItem();
                            studentItem.setActualUserId(strLine);
                            studentItem = encryptUserId(studentItem);

                            // Put anonUserId in place of actualUserId because:
                            // (1) we don't want to persist actualUserId in analysis_db
                            // (2) the actualUserId must be unique so it cannot be empty
                            studentItem.setActualUserId(studentItem.getAnonymousUserId());
                            studentDao.saveOrUpdate(studentItem);

                            edu.cmu.pslc.datashop.mapping.item.StudentItem mappedStudentItem =
                                getMappedStudentItem(studentItem);
                            mappedStudentItem.setActualUserId(strLine);
                            mappedStudentDao.saveOrUpdate(mappedStudentItem);

                            if (verbose) {
                                logger.info("Anonymized student id '" + studentItem.getActualUserId()
                                            + "' -> " + studentItem.getAnonymousUserId());
                            }
                        }
                    }
                    
                    if (studentItem != null) {
                        anonStudList.add(studentItem);
                    }
                }
                setAnonStudList(anonStudList);
                in.close();
            }
         } catch (Exception e) {
              logger.error("Error: " + e.getMessage());
         }
    }

    /**
     * set an anonymized StudentItem list
     * @param anonymizedStudentList list contains student with anonymized id and actual id
     */
    private void setAnonStudList(List<StudentItem> anonymizedStudentList) {
        this.anonymizedStudentList = anonymizedStudentList;
    }

    /**
     * @return an anonymized StudentItem list
     */
    private List<StudentItem> getAnonStudList() {
        return this.anonymizedStudentList;
    }

    /**
     * set a filename of the file contains student actual ids to be anonymized
     * @param filename string contains student actual id
     */
    private void setFileToAnonymize(String filename) {
        fileToAnonymize = filename;
    }

    /**
     * @return a file name
     */
    private String getFileToAnonymize() {
        return fileToAnonymize;
    }

    /**
     * returns an output file name by appending "anon_" to a given filename
     * @param filename string value of an input file name
     * @return string value of an output file name
     */
    private String getOutputFilename(String filename) {
        String outputFilename = "";
        int lastOccurrenceOfSlash = 0;
        lastOccurrenceOfSlash = filename.lastIndexOf("/");
        /* change the output file name by adding "anon_" as a prefix to the input file*/
        outputFilename =  filename.substring(0, lastOccurrenceOfSlash)
                + "/anon_" + filename.substring(lastOccurrenceOfSlash + 1);
        return outputFilename;
    }

    /**
     * write student actual id and anonymized id to a file
     * @param filename file name of the input file
     */
    private void writeToAnonymizedStudentList(String filename) {

        edu.cmu.pslc.datashop.mapping.dao.StudentDao mappedStudentDao
            = edu.cmu.pslc.datashop.mapping.dao.DaoFactory.DEFAULT.getStudentDao();

        edu.cmu.pslc.datashop.mapping.item.StudentItem studentItem =
            new edu.cmu.pslc.datashop.mapping.item.StudentItem();

        boolean fileExists = false;
        String outputFilename = getOutputFilename(filename);
        try {
            File outputFile = new File(outputFilename);
            if (!(outputFile.exists())) {
                fileExists = outputFile.createNewFile();
            } else {
                fileExists = true;
            }

            if (fileExists) {
                FileWriter fstream = new FileWriter(outputFilename);
                BufferedWriter out = new BufferedWriter(fstream);
                List<StudentItem> anonStudList = getAnonStudList();
                String strLine = "";
                if (anonStudList != null) {
                    for (Iterator<StudentItem> it
                            = anonStudList.iterator(); it.hasNext();) {
                        studentItem =
                            (edu.cmu.pslc.datashop.mapping.item.StudentItem)
                            mappedStudentDao.get((Long)((StudentItem) it.next())
                                                 .getId());
                        if (studentItem != null) {
                            strLine = strLine + studentItem.getActualUserId()
                                + "\t" + studentItem.getAnonymousUserId()
                                + "\r\n";
                        }
                    }
                    anonStudList.clear();
                    setAnonStudList(null);
                }
                if (!strLine.equals("")) {
                    out.write(strLine);
                    if (verbose) {
                        logger.info("Write to File " + strLine);
                    }
                }
                out.close();
            }
         } catch (Exception e) {
              logger.error("Error: " + e.getMessage());
         }
    }

    /**
     * Handle the command line arguments.
     * @param args - command line arguments passed into main
     */
    protected void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-inputFile")) {
                if (++i < args.length) {
                    setFileToAnonymize(args[i].trim());
                } else {
                    System.err.println(
                        "Error: a filename must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-v")) {
                verbose = true;
            } else {
                System.err.println("Error: improper command line arguments: " + argsList);
                displayUsage();
                System.exit(1);
            } // end if then else
        } // end for loop
    } // end handleOptions

    /**
     * Display the usage of this utility.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..."
            + " Anonymizer ");
        System.err.println("Option descriptions:");
        System.err.println("\t-v       \t verbose");
        System.err.println("\t-h       \t usage info");
        System.err.println("\t-inputFile       \t usage info");
    }

    /**
     * Run the Anonymizer.
     * USAGE: java -classpath ... Anonymizer
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("Anonymizer.main");
        logger.info("Anonymizer starting...");
        try {
            Anonymizer theAnonymizer = new Anonymizer();
            // Handle the command line options
            theAnonymizer.handleOptions(args);
            if (theAnonymizer.getFileToAnonymize() != "") {
                String inputFilename = theAnonymizer.getFileToAnonymize();
                theAnonymizer.anonymizeInFile(inputFilename);
                theAnonymizer.writeToAnonymizedStudentList(inputFilename);
                theAnonymizer.setFileToAnonymize("");
            } else {
                theAnonymizer.anonymize();
            }
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        }
        logger.info("Anonymizer done.");

    }

}
