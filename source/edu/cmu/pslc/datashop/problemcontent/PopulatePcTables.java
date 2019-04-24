/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.problemcontent;

import edu.cmu.pslc.datashop.extractors.AbstractExtractor;
import edu.cmu.pslc.datashop.extractors.ExtractorFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.PcConversionDao;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.PcProblemDao;
import edu.cmu.pslc.datashop.dao.UserDao;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * Reads a problem conversion text file and populates the appropriate database tables.
 *
 * @author Cindy Tipper
 * @version $Revision: 11483 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-14 14:38:11 -0400 (Thu, 14 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PopulatePcTables extends AbstractExtractor {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Name of directory where Problem Content exists. */
    private String inputDir = null;

    /** Constant for the conversion tool label. */
    public static final String SEPARATOR = "\t";

    /** Constant for the format of dates. */
    public static final String DATE_FMT_STR = "MMMM dd, yyyy";

    /** Constant for the output directory base. */
    public static final String OUTPUT_DIRECTORY_STR = "problem_content";
    /** Constant for the PC conversion directory and file name. */
    public static final String PC_CONVERSION_STR = "pc_conversion";

    /** Constant for the conversion tool label. */
    public static final String CONVERSION_TOOL_LABEL = "Conversion Tool";
    /** Constant for the tool version label. */
    public static final String TOOL_VERSION_LABEL = "Tool Version";
    /** Constant for the Datashop version label. */
    public static final String DATASHOP_VERSION_LABEL = "DataShop Version";
    /** Constant for the conversion date label. */
    public static final String CONVERSION_DATE_LABEL = "Conversion Date";
    /** Constant for the content version label. */
    public static final String CONTENT_VERSION_LABEL = "Content Version";
    /** Constant for the content date label. */
    public static final String CONTENT_DATE_LABEL = "Content Date";
    /** Constant for the content description label. */
    public static final String CONTENT_DESCRIPTION_LABEL = "Content Description";
    /** Constant for the content skills label. */
    public static final String CONTENT_SKILLS_LABEL = "Skills";
    /** Constant for the content path label. */
    public static final String CONTENT_PATH_LABEL = "Path";
    /** Constant for the content problem label. */
    public static final String CONTENT_PROBLEM_LABEL = "Problem";

    /** Constant for the date formatter. */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FMT_STR);


    /**
     * The constructor.
     */
    public PopulatePcTables() { }

    /**
     * The main method of this converter.
     */
    private void run() {
        try {
            logger.info("Getting files...");
            List<File> fileList = getFilenameList(logger, inputDir);

            for (File f : fileList) {
                PcConversionItem pci = readMetaData(f);
                if (pci != null) {
                    readProblems(f, pci, true);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Unknown error: ", t);
        }
    }

    /** Constant for number of lines of meta-data. */
    private static final int META_DATA_NUM_LINES = 8;
    /** Constant for the number of columns in a line of meta-data. */
    private static final int META_DATA_NUM_COLS = 2;

    /**
     * Read conversion meta-data.
     * @param file the File
     * @return the PcConversionItem
     * @throws Exception failure to read file
     */
    private PcConversionItem readMetaData(File file)
        throws Exception {

        PcConversionItem result = new PcConversionItem();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            int numLinesRead = 0;
            while (((line = br.readLine()) != null) && (numLinesRead < META_DATA_NUM_LINES)) {
                String[] fields = line.split(SEPARATOR);
                if (fields.length != META_DATA_NUM_COLS) {
                    logger.info("Invalid number of columns in meta-data: " + line);
                    return null;
                }
                if (fields[0].equals(CONVERSION_TOOL_LABEL)) {
                    result.setConversionTool(fields[1]);
                } else if (fields[0].equals(TOOL_VERSION_LABEL)) {
                    result.setToolVersion(fields[1]);
                } else if (fields[0].equals(DATASHOP_VERSION_LABEL)) {
                    result.setDatashopVersion(fields[1]);
                } else if (fields[0].equals(CONVERSION_DATE_LABEL)) {
                    result.setConversionDate(getDate(fields[1]));
                } else if (fields[0].equals(CONTENT_VERSION_LABEL)) {
                    result.setContentVersion(fields[1]);
                } else if (fields[0].equals(CONTENT_DATE_LABEL)) {
                    result.setContentDate(getDate(fields[1]));
                } else if (fields[0].equals(CONTENT_DESCRIPTION_LABEL)) {
                    result.setContentDescription(fields[1]);
                } else if (fields[0].equals(CONTENT_PATH_LABEL)) {
                    result.setPath(fields[1]);
                } else {
                    logger.info("Ignoring invalid label: " + fields[0]);
                }
                numLinesRead++;
            }
        } catch (IOException e) {
            logger.info("Failed to read meta-data from file: " + file.getName());
            throw e;
        } finally {
            if (br != null) { br.close(); }
        }

        PcConversionDao pcConversionDao = DaoFactory.DEFAULT.getPcConversionDao();

        // Make sure we haven't read this conversion file before...
        PcConversionItem tmp = pcConversionDao.getByToolDateAndVersion(result.getConversionTool(),
                    result.getContentDate(), result.getContentVersion());

        if (tmp != null) {
            boolean hasNewProblems = false;
            boolean isMissingProblems = false;

            logger.info("Comparing previously existing version against new version.");
            PcProblemDao pcProblemDao = DaoFactory.DEFAULT.getPcProblemDao();
            List<PcProblemItem> prevProblemList = pcProblemDao.findProblemsByConversion(tmp);

            // Compare problem_name and html_file in the old conversion with the new conversion
            List<PcProblemItem> newProblemList = readProblems(file, result, false);

            List<String> prevProblemNames = new ArrayList();
            List<String> newProblemNames = new ArrayList();

            // Compare problem_name in the old conversion with the new conversion
            for (PcProblemItem prevProblem : prevProblemList) {
                prevProblemNames.add(prevProblem.getProblemName());
            }

            // Since the PcConversion and html_file will differ, only compare problem name
            for (PcProblemItem newProblem : newProblemList) {
                newProblemNames.add(newProblem.getProblemName());
            }

            List<String> newProblems = new ArrayList<String>();
            newProblems.addAll(newProblemNames);
            newProblems.removeAll(prevProblemNames);
            if (newProblems != null && newProblems.size() > 0) {
                // The pc conversion contains new problems
                hasNewProblems = true;
                logger.info("Content contains new problems.");
            }

            List<String> missingProblems = new ArrayList<String>();
            missingProblems.addAll(prevProblemNames);
            missingProblems.removeAll(newProblemNames);
            if (missingProblems != null && missingProblems.size() > 0) {
                // The pc conversion is missing problems
                isMissingProblems = true;
                logger.info("Content does not cover all existing problems.");
            }

            if (hasNewProblems || isMissingProblems) {
                // Get the version id to delete
                Long conversionId = (Long) tmp.getId();
                // mck what is versionId and conversionId

                // Use PcHelper and Dao's to unmap and delete the old conversion
                ProblemContentHelper pcHelper = new ProblemContentHelper();
                PcConversionDatasetMapDao pcdmDao =
                    DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
                // Log as the System user
                UserDao userDao = DaoFactory.DEFAULT.getUserDao();
                UserItem userItem = userDao.find(UserItem.SYSTEM_USER);
                // Datasets being unmapped
                List<DatasetItem> datasetItems = pcdmDao.findDatasets(tmp);
                for (DatasetItem datasetItem : datasetItems) {
                    Integer datasetId = (Integer) datasetItem.getId();
                    logger.info("Unmapping problem content for PcConversionItem (" + conversionId
                        + ") and dataset (" + datasetId + ")");
                    // unmap the old pc conversion
                    pcHelper.deleteContentVersionMapping(conversionId, datasetId, userItem);

                }
                // delete the existing PcConversionItem
                logger.info("Deleting PcConversionItem (" + conversionId + ")");
                pcHelper.deletePcConversion(conversionId, userItem);
                // Remove reference to old content by setting tmp to null
                tmp = null;

            } else {
                String tmpDate = DATE_FORMAT.format(tmp.getConversionDate());
                String resultDate = DATE_FORMAT.format(result.getConversionDate());
                if (!tmpDate.equals(resultDate)) {
                    logger.info("Updating conversion date to reflect changes in conversion file: "
                        + file.getName());

                    tmp.setConversionDate(result.getConversionDate());
                    pcConversionDao.saveOrUpdate(tmp);
                } else {
                    logger.info("Ignoring: conversion file has not changed: " + file.getName());
                }
                // Ignore new content since existing content matches
                result = null;
            }
        }

        if (tmp == null) {
            // Write the new PcConversionItem to the database.
            pcConversionDao.saveOrUpdate(result);
        }

        return result;
    }

    /** Constant for the number of columns in a line describing a problem. */
    private static final int PROBLEM_CONTENT_NUM_COLS = 3;

    /**
     * Read problem content problems.
     * @param file the File
     * @param pci the PcConversionItem
     * @param saveToDatabase whether or not to save the data to a database
     * @return the list of PcProblemItems
     * @throws Exception failure to read file
     */
    private List<PcProblemItem> readProblems(
        File file, PcConversionItem pci, Boolean saveToDatabase)
            throws Exception {

        List<PcProblemItem> pcProblemList = new ArrayList<PcProblemItem>();

        BufferedReader br = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            br = new BufferedReader(
                new InputStreamReader(inputStream, Constants.UTF8), Constants.IS_READER_BUFFER);
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(SEPARATOR);
                // Skip the meta-data and poorly-formed problem lines.
                if (!fields[0].equals(CONTENT_PROBLEM_LABEL)) { continue; }
                if (fields.length != PROBLEM_CONTENT_NUM_COLS) {
                    logger.info("Invalid number of columns in problem content: " + line);
                    continue;
                }

                PcProblemItem item = new PcProblemItem();
                item.setProblemName(fields[1]);
                item.setHtmlFile(findOrCreateFile(pci.getPath(), fields[2]));
                item.setPcConversion(pci);

                pcProblemList.add(item);
            }
        } catch (IOException e) {
            logger.info("Failed to read meta-data from file: " + file.getName());
            throw e;
        } finally {
            if (br != null) { br.close(); }
        }

        logger.info(file.getName() + ": found " + pcProblemList.size() + " problems.");

        // Write the new PcConversionItem to the database.
        if (saveToDatabase) {
            logger.info("Populating PC Tables with new content.");

            PcProblemDao pcProblemDao = DaoFactory.DEFAULT.getPcProblemDao();

            for (PcProblemItem i : pcProblemList) {

                pcProblemDao.saveOrUpdate(i);

            }
        }
        return pcProblemList;
    }

    /**
     * Helper method to parse a string into a Date.
     * @param dateStr the string
     * @return a java.util.Date object
     */
    private Date getDate(String dateStr) {

        Date result = null;

        if ((dateStr == null) || (dateStr.trim().length() == 0)) { return result; }
        try {
            synchronized (DATE_FORMAT) {
                result = DATE_FORMAT.parse(dateStr);
            }
        } catch (ParseException e) {
            logger.info("Failed to parse: " + dateStr);
            result = null;
        }

        return result;
    }

    /**
     * Find or create a FileItem for the named file.
     * @param path the file path
     * @param fileName the file name
     * @return FileItem
     */
    private FileItem findOrCreateFile(String path, String fileName) {

        // Ensure the path doesn't have a trailing slash.
        if (path.endsWith("/")) { path = path.substring(0, path.length() - 1); }
        if (path.endsWith("\\")) { path = path.substring(0, path.length() - 1); }

        int lastIndex = fileName.lastIndexOf("/");
        if (lastIndex == -1) { lastIndex = fileName.lastIndexOf("\\"); }

        // Allow for the fact that addtl path might be specified in fileName.
        if (lastIndex > 0) {
            String newFileName = fileName.substring(lastIndex + 1);
            String newPath = path + "/" + fileName.substring(0, lastIndex);
            return findOrCreateFile(newPath, newFileName);
        }

        // This returns a list though only a single file is expected. Sigh.
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        List<FileItem> fileList = fileDao.find(path, fileName);

        if (fileList.size() > 0) { return fileList.get(0); }

        // No matching file found... create.
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        FileItem fileItem = new FileItem();
        fileItem.setFileName(fileName);
        fileItem.setFilePath(path);
        fileItem.setAddedTime(new Date());
        fileItem.setFileType("text/plain");
        fileItem.setFileSize(0L);   // can't be determined
        fileItem.setOwner(userDao.findOrCreateSystemUser());
        fileDao.saveOrUpdate(fileItem);
        return fileItem;
    }

    /**
     * Parse command line arguments.
     * @param args Command line arguments
     * @throws IOException failure to create output directory
     */
    private void handleArgs(String[] args)
        throws IOException {

        if (args == null || args.length == 0) {
            displayUsage();
            System.exit(0);
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-i")) {
                if (++i < args.length) {
                    inputDir = args[i];
                } else {
                    logger.error("Error: a directory name must be specified with the -i argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if ((args[i].equals("-e")) || (args[i].equals("-email"))) {
                setSendEmailFlag(true);
                if (++i < args.length) {
                    setEmailAddress(args[i]);
                } else {
                    logger.error("Error: a email address must be specified with the -e argument");
                    displayUsage();
                    System.exit(1);
                }
            }
        }

        if (inputDir == null) {
            displayUsage();
            System.exit(0);
        }
    }

    /**
     * Display usage for this converter.
     */
    private void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ... PopulatePcTables -i <inputDir>");
        System.err.println("Optional args:");
        System.err.println("\t-h\t usage info... this message");
        System.err.println("\t-e\t email: user to notify in case of failure");
    }

    /**
     * Run the Info Field Converter.
     * @param args command line arguments
     */
    public static void main(String[] args) {

        Logger logger = Logger.getLogger("PopulatePcTables.main");

        String version = VersionInformation.getReleaseString();
        logger.info("PopulatePcTables starting (" + version + ")...");
        try {
            PopulatePcTables ppt = ExtractorFactory.DEFAULT.getPopulatePcTables();
            ppt.handleArgs(args);
            ppt.run();
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("PopulatePcTables done.");
        }
    }

} // end class
