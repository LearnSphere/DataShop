/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.DatasetState;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * Generates skill models for newly created or recently modified datasets.
 *
 * @author Benjamin Billings
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SkillModelGenerator extends AbstractExtractor {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Boolean to force a reload of all data */
    private boolean forceAll;
    /** Boolean indicating to run a specific dataset. */
    private boolean datasetOnlyFlag;
    /** The dataset id to run this task on. */
    private int datasetIdToRun;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** Default constructor. */
    public SkillModelGenerator() { forceAll = false; }

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
     * Walks through each dataset and generates KC Models, unique-step and single-kc.
     */
    public void generateModels() {
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        if (datasetOnlyFlag) {
            DatasetItem datasetItem = datasetDao.get(datasetIdToRun);
            if (datasetItem == null) {
                logger.error("Dataset not found: " + datasetIdToRun);
            } else {
                String logPrefix = "MG [" + datasetItem.getDatasetName()
                        + " (" + datasetItem.getId() + ")] : ";
                logger.info(logPrefix + "Generating KC models");
                generateModels(datasetItem);
            }
        } else {
            logger.info("Generating KC models for all datasets.");
            List <DatasetItem> datasetList = datasetDao.findUndeletedDatasets();
            for (DatasetItem datasetItem : datasetList) {
                generateModels(datasetItem);
            }
        }
    }

    /**
     * Generates baseline KC models for the given dataset.
     * @param dataset the dataset item
     */
    public void generateModels(DatasetItem dataset) {
        if (dataset == null) {
            return;
        }
        String info = dataset.getDatasetName() + " (" + dataset.getId() + ")";

        try {
            dataset = DaoFactory.DEFAULT.getDatasetDao().get((Integer)dataset.getId());
            long transactionCount = DaoFactory.DEFAULT.getTransactionDao().count(dataset);

            if (transactionCount > 0
                && (forceAll || DatasetState.requiresModelGeneration(dataset))) {

                long startTime = System.currentTimeMillis();

                GeneratorTransaction ac = new GeneratorTransaction(dataset);
                Boolean success = (Boolean)transactionTemplate.execute(ac);

                long elapsedTime = System.currentTimeMillis() - startTime;

                String msg = success ? "Finished skill model generation for "
                        : "ERROR: skill model generation for ";
                msg += info + ".";

                SystemLogger.log(dataset, null,
                        SystemLogger.ACTION_GENERATE_MODELS, msg,
                        success, elapsedTime);

                // If a slave, update master DataShop instance with dataset info.
                if (success && DataShopInstance.isSlave()) {
                    String datasetName = dataset.getDatasetName();
                    Integer datasetId = (Integer)dataset.getId();
                    logger.info("Update master DataShop instance with dataset info"
                                + datasetName + " (" + datasetId + ")");
                    try {
                        DatasetDTO datasetDto =
                            HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                        DatasetCreator.INSTANCE.setDataset(datasetDto);
                    } catch (Exception e) {
                        // Failed to push Dataset info to master. Ignore?
                        logger.debug("Failed to push dataset info to master for dataset '"
                                     + datasetName + "': " + e);
                    }
                }

            }
        } catch (Throwable throwable) {
            sendErrorEmail(logger, "Error generating model for dataset " + info, throwable);
        }
    }

    /**
     * Parse the command line arguments to get the file and curriculum names.
     * @param args Command line arguments
     */
    protected void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-f")) {
                forceAll = true;
                logger.info("Force All enabled");
            } else if (args[i].equals("-d") || args[i].equals("-dataset")) {
                datasetOnlyFlag = true;
                if (++i < args.length) {
                    try {
                        datasetIdToRun = Integer.parseInt(args[i]);
                        if (datasetIdToRun < 0) {
                            throw new Exception("Invalid dataset id");
                        }
                    } catch (Exception exception) {
                        logger.error("Error while trying to parse dataset id. "
                                + "Please check the parameter for accuracy.");
                        System.exit(1);
                    }
                } else {
                    logger.error("A dataset id must be specified with the -dataset argument");
                    displayUsage();
                    System.exit(1);
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
            }
        } // end for loop
    } // end handleOptions

    /**
     * Displays the command line arguments for this program.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... "
                + "Aggregator");
        System.err.println("Option descriptions:");
        System.err.println("\t-h\t usage info");
        System.err.println("\t-e, email\t send email if major failure");
        System.err.println("\t-f\t force update on all datasets");
    }

    /**
     * Main program to run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("SkillModelGenerator.main");
        String version = VersionInformation.getReleaseString();
        logger.info("SkillModelGenerator starting (" + version + ")...");
        SkillModelGenerator generator = ExtractorFactory.DEFAULT.getSkillModelGenerator();

        // Initialize DataShop instance from the database
        DataShopInstance.initialize();

        try {
            // parse arguments to get file name and curriculum name
            generator.handleOptions(args);
            // read file,
            // create skill model (if necessary),
            // add the skills to the skill table (if necessary),
            // and associate the steps to the skills
            generator.generateModels();
        } catch (Throwable throwable) {
            logger.error("Throwable", throwable);
            // Log error and send email if this happens!
            generator.sendErrorEmail(logger, "Unknown error in main method.", throwable);
        } finally {
            logger.info("SkillModelGenerator done.");
        }
    } // end main

    /**
     * Inner class that wraps the model creation for a single dataset inside of a transaction.
     */
    private class GeneratorTransaction implements TransactionCallback  {

        /** The dataset to generate models for. */
        private DatasetItem dataset;

        /**
         * Default constructor.
         * @param dataset the dataset to generate models for.
         */
        public GeneratorTransaction(DatasetItem dataset) {
            this.dataset = dataset;
        }

        /**
         * Implement the interface.
         * Put all actions in this for transaction management.
         * @param ts TransactionStatus.
         * @return Boolean false if the transaction failed and was rolled back, true otherwise.
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                SkillModelDao modelDao = DaoFactory.DEFAULT.getSkillModelDao();

                String datasetInfo = dataset.getDatasetName() + " (" + dataset.getId() + ")";

                logger.info("Generating Single-KC Model for dataset " + datasetInfo);
                modelDao.createOrUpdateSingleKCModel(dataset);

                logger.info("Generating Unique-Step Model for dataset " + datasetInfo);
                modelDao.createOrUpdateUniqueStepModel(dataset);

                return true;
            } catch (Throwable exception) {
                logger.error(exception.toString(), exception);
                ts.setRollbackOnly();
                return false;
            }
        }
    }

} // end StepToSkillMapper class
