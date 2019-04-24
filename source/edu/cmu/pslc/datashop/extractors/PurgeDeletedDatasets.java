/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.TransactionDao;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportHelper;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.VersionInformation;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

/**
 * This utility is used to purge deleted datasets from the ds_dataset table.
 * Piggyback on this and also purge deleted discourses from discoursedb db.
 *
 * @author Mike Komisin
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PurgeDeletedDatasets extends AbstractLoader {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** The cfg base directory. */
    private String cfgDir = null;

    /** Default constructor. */
    public PurgeDeletedDatasets() {
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
     * Queries the ds_dataset table for datasets marked with deleted_flag and deletes them.
     */
    public void purgeDeletedDatasets() {
        try {
            // Get a distinct set of user/session pairs and then get the
            // associated log action data.
            logger.info("PurgeDeletedDatasets Starting stored procedure.");

            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
            List<DatasetItem> itemList = dsDao.findDeletedDatasets();
            for (DatasetItem dsItem : itemList) {
                // Remove files associated with dataset
                FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                List<String> filePaths = fileDao.getDistinctFilePaths(dsItem);

                // Delete the directories associated with files and papers for this dataset
                for (String filePath : filePaths) {
                    // Ensure that the file path is not the base directory
                    // before deleting the directory recursively
                    if (cfgDir != null && !cfgDir.isEmpty()) {
                        File fileToDelete = new File(cfgDir + File.separator + filePath);
                        File baseDir = new File(cfgDir);
                        if (fileToDelete != null && baseDir != null
                                && !fileToDelete.equals(baseDir)) {
                            if (fileToDelete.delete()) {
                                SystemLogger.log(dsItem, "Delete dataset file",
                                    "Successfully deleted dataset file: " + filePath);
                            } else {
                                logger.error("Error occurred while attempting to remove "
                                    + fileToDelete);
                            }
                        }
                    }
                }

                // Next, delete any cached files for the dataset
                SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
                List<SampleItem> sampleItems = sampleDao.find(dsItem);
                TransactionExportHelper txExportHelper =
                        HelperFactory.DEFAULT.getTransactionExportHelper();
                StepRollupExportHelper stepExportHelper =
                        HelperFactory.DEFAULT.getStepRollupExportHelper();
                StudentProblemExportHelper studentProblemExportHelper =
                        HelperFactory.DEFAULT.getStudentProblemExportHelper();
                for (SampleItem sampleItem : sampleItems) {
                    // Delete tx export
                    String cachedTxFileName = txExportHelper.getCachedFileName(sampleItem, cfgDir);
                    if (cachedTxFileName != null) {
                        if (txExportHelper.deleteFile(cachedTxFileName)) {
                               SystemLogger.log(dsItem, "Delete cached tx export",
                                   "Successfully deleted cached transaction export file '"
                                   + cachedTxFileName + "'.");
                        } else {
                            logger.error("Error occurred while attempting to remove"
                                + " cached transaction export "
                                + " for " + sampleItem.getSampleName()
                                + "(" + sampleItem.getId() + ").");
                        }
                    }
                    // Delete student step export
                    String cachedStepFileName = stepExportHelper
                        .getCachedFileName(sampleItem, cfgDir);
                    if (cachedStepFileName != null) {
                        if (stepExportHelper.deleteFile(cachedStepFileName)) {
                            SystemLogger.log(dsItem, "Delete cached step export",
                                    "Successfully deleted cached step export file '"
                             + cachedStepFileName + "'.");
                         } else {
                             logger.error("Error occurred while attempting to remove"
                                     + " cached step export "
                                     + " for " + sampleItem.getSampleName() + "("
                                     + sampleItem.getId() + ").");
                         }
                    }

                    // Delete student problem export
                    String cachedSProblemFileName = studentProblemExportHelper
                        .getCachedFileName(sampleItem, cfgDir);
                    if (cachedSProblemFileName != null) {
                        if (studentProblemExportHelper.deleteFile(cachedSProblemFileName)) {
                            SystemLogger.log(dsItem, "Delete cached student problem export",
                                "Successfully deleted cached student problem export file '"
                             + cachedSProblemFileName + "'.");
                         } else {
                             logger.error("Error occurred while attempting to remove"
                                     + " cached student problem export "
                                     + " for '" + sampleItem.getSampleName()
                                     + "' (" + sampleItem.getId() + ").");
                         }
                    }
                }

                logger.info("Deleting Dataset '" + dsItem.getDatasetName()
                        + "' (" + dsItem.getId() + ")");
                // Remove transactions and dataset, and log the deletion
                purgeDeletedDatasets(dsItem);
            }

        } catch (Exception e) {
            // Log error and send email if this happens!
            String msg = "Unknown error in purgeDeletedDatasets method.";
            logger.error(msg, e);
            sendErrorEmail(logger, msg, e);
        }
    }

    /**
     * Calls an SP to delete the datasets.
     * @param dataset the dataset item
     */
    private void purgeDeletedDatasets(DatasetItem dataset) {
        if (dataset == null) { return; }

        TransactionDao txDao = DaoFactory.DEFAULT.getTransactionDao();
        // Call the SP to unlink import queue items and delete the transactions
        Integer deletedTransactions = txDao.callPurgeDeletedDatasetsSP(dataset);

        // If everything up to this point was successful, delete the
        // remote instance of this dataset if this is a slave.
        if (DataShopInstance.isSlave()) {
            try {
                DatasetCreator.INSTANCE.deleteDataset((Integer)dataset.getId());
            } catch (Exception e) {
                // Send email if error happens.
                String msg = "Failed to delete remote dataset instance.";
                logger.error(msg, e);
                sendErrorEmail(logger, msg, e);
            }
        }

    } // end purgeDeletedDatasets

    /**
     * Queries the discoursedb.discourse table for discourses marked for 
     * deletion (deleted_flag = true) and deletes them.
     */
    public void purgeDeletedDiscourses() {
        try {
            logger.info("PurgeDeletedDiscourses: starting stored procedure.");

            DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
            List<DiscourseItem> discourses = discourseDao.findDeletedDiscourses();
            for (DiscourseItem di : discourses) {
                // The file (upload) for this discourse was already deleted. ???

                logger.info("Deleting Discourse '" + di.getName() + "' (" + di.getId() + ")");

                purgeDeletedDiscourse(di);
            }

        } catch (Exception e) {
            // Log error and send email if this happens!
            String msg = "Unknown error in purgeDeletedDiscourses method.";
            logger.error(msg, e);
            sendErrorEmail(logger, msg, e);
        }
    }

    /**
     * Calls an SP to delete the specified discourse.
     * @param discourse the Discourse item
     */
    private void purgeDeletedDiscourse(DiscourseItem discourse) {
        if (discourse == null) { return; }

        DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
        discourseDao.callDeleteDiscourseSP(discourse);

        // For now, assuming there are no discourses on remote instances...

    }

    /**
     * Display the usage of this utility.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..."
            + " PurgeDeletedDatasets "
            + " [-email address]");
        System.err.println("Option descriptions:");
        System.err.println("\t-e, email           \t send email if major failure");
        System.err.println("\t-dir, cached files dir"
            + "           \t cached files are deleted with datasets");
        System.err.println("\t-h, help           \t this help message ");
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
            } else if (args[i].equals("-help")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-dir")) {
                 if (++i < args.length) {
                     cfgDir = new String(args[i]);
                 }
            } else if (args[i].equals("-e") || args[i].equals("-email")) {
                setSendEmailFlag(true);
                if (++i < args.length) {
                    setEmailAddress(args[i]);
                } else {
                    System.err.println(
                        "Error: a email address must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else {
                System.err.println("Error: improper command line arguments: " + argsList);
                displayUsage();
                System.exit(1);
            } // end if then else
        } // end for loop
    } // end handleOptions

    /**
     * Main.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("PurgeDeletedDatasets.main");
        String version = VersionInformation.getReleaseString();
        logger.info("PurgeDeletedDatasets starting (" + version + ")...");
        PurgeDeletedDatasets purgeDeletedDatasets =
            ExtractorFactory.DEFAULT.getPurgeDeletedDatasets();

        try {
            // Handle the command line options
            purgeDeletedDatasets.handleOptions(args);

            // Initialize DataShop instance from the database
            DataShopInstance.initialize();

            // Delete those datasets that have been marked for deletion.
            purgeDeletedDatasets.purgeDeletedDatasets();

            // Delete those discourses that have been marked for deletion.
            purgeDeletedDatasets.purgeDeletedDiscourses();

        } catch (Throwable throwable) {
            // Log error and send email if this happens!
            purgeDeletedDatasets.sendErrorEmail(logger, "Unknown error in main method.", throwable);
        }
        logger.info("PurgeDeletedDatasets done.");
    }

} // end class PurgeDeletedDatasets
