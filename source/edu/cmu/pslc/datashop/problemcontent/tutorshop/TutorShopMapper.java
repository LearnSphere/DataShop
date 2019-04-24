/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.problemcontent.tutorshop;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.PcConversionDao;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.PcProblemDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.extractors.ExtractorFactory;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapId;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 *  Given a dataset id, try to map TutorShop problem content to it.
 *
 * @author Alida Skogsholm
 * @version $Revision: 11358 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-07-22 07:42:00 -0400 (Tue, 22 Jul 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TutorShopMapper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Given dataset id. */
    private String datasetIdStr = null;

    /**
     * Main method.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("TutorShopMapper.main");
        String version = VersionInformation.getReleaseString();
        logger.info("TutorShopMapper starting (" + version + ")...");
        TutorShopMapper mapper = ExtractorFactory.DEFAULT.getTutorShopMapper();
        try {
            mapper.handleOptions(args);
            mapper.doTheMapping();
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("TutorShopMapper done.");
        }
    }

    /**
     * For each TutorShop conversion item not already connected to the given dataset,
     * get the problems and try to map it.
     */
    public void doTheMapping() {
        long totalMappedForDataset = 0;

        DatasetItem datasetItem = getDatasetItem(datasetIdStr);
        if (datasetItem == null) {
            return;
        }

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = userDao.findOrCreateSystemUser();

        ProblemDao problemDao = DaoFactory.DEFAULT.getProblemDao();
        PcConversionDao pcConvDao = DaoFactory.DEFAULT.getPcConversionDao();
        PcProblemDao pcProblemDao = DaoFactory.DEFAULT.getPcProblemDao();
        PcConversionDatasetMapDao pcDsMapDao = DaoFactory.DEFAULT.getPcConversionDatasetMapDao();

        List<PcConversionItem> pccList = pcConvDao.getContentVersionsByTool(
                PcConversionItem.TUTORSHOP_CONVERTER, datasetItem, null);
        for (PcConversionItem pccItem : pccList) {
            long numMappedForPcc = 0;

            List<ProblemItem> unmappedProblems = problemDao.getUnmappedProblems(datasetItem);
            for (ProblemItem problemItem : unmappedProblems) {
                PcProblemItem ppi = pcProblemDao.findByNameAndConversion(
                        problemItem.getProblemName(), pccItem);
                if (ppi == null) {
                    continue;
                }

                String contentVersion = pccItem.getContentVersion();
                String hierarchy = problemDao.getHierarchy(problemItem);

                if (hierarchy.contains(contentVersion + ",")
                 || hierarchy.endsWith(contentVersion)) {
                    problemItem.setPcProblem(ppi);
                    problemDao.saveOrUpdate(problemItem);
                    totalMappedForDataset++;
                    numMappedForPcc++;
                }

            } //end for loop on problem items

            if (numMappedForPcc > 0) {
                PcConversionDatasetMapItem pcDsMapItem = new PcConversionDatasetMapItem();
                pcDsMapItem.setId(new PcConversionDatasetMapId(pccItem, datasetItem));
                pcDsMapItem.setNumProblemsMapped(numMappedForPcc);
                pcDsMapItem.setMappedBy(userItem);
                pcDsMapItem.setMappedTime(new Date());
                pcDsMapItem.setStatus(PcConversionDatasetMapItem.STATUS_COMPLETE);
                pcDsMapDao.saveOrUpdate(pcDsMapItem);
                logger.info("Mapped " + numMappedForPcc + " problems for "
                        + pccItem.getContentVersion());
            }
        } //end for loop in PC Conversion items

        logger.info("Mapped " + totalMappedForDataset + " problems for dataset "
                + datasetItem.getDatasetName() + " [" + datasetItem.getId() + "]");
    }

    /**
     * Return a DatasetItem if the given string is a valid integer and a dataset id.
     * @param idStr the dataset id string
     * @return a dataset item if valid, null otherwise
     */
    private DatasetItem getDatasetItem(String idStr) {
        if (idStr == null) {
            logger.error("DatasetId should not be null");
            return null;
        }

        DatasetItem item = null;
        Integer itemId = parseInteger(idStr);
        if (itemId != null) {
            DatasetDao dao = DaoFactory.DEFAULT.getDatasetDao();
            item = dao.get(itemId);
        }

        if (item == null) {
            logger.error("Dataset not found with id: " + idStr);
            return null;
        }
        return item;
    }

    /**
     * Parse the given string for an integer returning null if it is not valid.
     * @param value the string to parse
     * @return an integer if valid, null otherwise
     */
    private Integer parseInteger(String value) {
        Integer newInt = null;
        try {
            newInt = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            logger.warn("Not a valid integer: " + value);
        }
        return newInt;
    }

    /**
     * Display the usage of this utility.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..."
            + " TutorShopMapper [-dataset id]");
        System.err.println("Option descriptions:");
        System.err.println("\t-dataset  \t dataset id");
        System.err.println("\t-h, help  \t show help");
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
            } else if (args[i].equals("-dataset")) {
                if (++i < args.length) {
                    datasetIdStr = args[i];
                } else {
                    System.err.println(
                        "Error: a name must be specified with this argument");
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
}
