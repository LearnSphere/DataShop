/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import static edu.cmu.pslc.datashop.util.FileUtils.cleanForFileSystem;
import org.apache.log4j.Logger;
import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetInfoReportDao;
import edu.cmu.pslc.datashop.dao.PaperDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dto.DatasetInfoReport;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.ServerNameUtils;

/**
 * An abstract bean class to hold the common attributes and methods for the
 * different types of export beans.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10513 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-03 12:56:21 -0500 (Mon, 03 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractExportBean implements Runnable {

    /** Debug logging. */
    private Logger logger
    = Logger.getLogger(AbstractExportBean.class.getName());

    /** Number of transactions to return and process in a chunk */
    protected static final int BATCH_SIZE = 500;

    /** Student-step export type. */
    protected static final String STEP_EXPORT = "student_step";

    /** Student-problem export type. */
    protected static final String PROBLEM_EXPORT = "student_problem";

    /** Student-problem export type. */
    protected static final String TX_EXPORT = "tx";

    /** Initial size of the string buffer */
    protected static final int BUFFER_SIZE = 262144;
    /**
     * String Constant of the file name for README.txt
     * */
    static final String README_FILE_NAME = "README.txt";
    /**
     * String Constant of the file name for TERMS.html
     * */
    static final String TERMS_FILE_NAME = "TERMS.html";

    /** Flag indicating the data for this thread are initializing. */
    private boolean initializing;
    /** Flag indicating this thread should cancel operations and exit. */
    private boolean cancelFlag;
    /** Flag indicating the final result has been exported. */
    private boolean exportedFlag;
    /** Flag indicating if one of more selected samples are not cached. */
    private boolean cachedFileAvailable;

    /** Temporary File name to store the export */
    private File tempFile;
    /** Zip output stream used for transaction exports. */
    private ZipOutputStream outputStream = null;

    /** Flag indicating whether this thread encountered an error */
    private boolean hasError;
    /** Flag indicating this thread is running. */
    private boolean running;
    /** Total number of transactions to export */
    private int numTotalRows;
    /** # of processed transactions */
    private int numCompletedRows;

    /** Default constructor. */
    public AbstractExportBean() { }

    /** Constant for one hundred used in percentage. */
    private static final int ONE_HUNDRED = 100;

    /** Buffer size for zip file processing. */
    private static final int ZIP_BUFFER_SIZE = 18024;

    /** Time stamp format. */
    private static final FastDateFormat TIME_STAMP_FMT
            = FastDateFormat.getInstance("yyyy_MMdd_HHmmss");

    /** Initialize the bean. */
    public void init() {
        hasError = false;
        running = false;
        cancelFlag = false;
        exportedFlag = false;
        this.numTotalRows = 0;
        this.numCompletedRows = 0;

        try {
            tempFile = FileUtils.createTemporaryFile();
        } catch (IOException ioException) {
            logger.error("IOException in AbstractExportBean thread: ", ioException);
            stop();
            numTotalRows = -1;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("New AbstractExportBean Create :: temp filename :"
                + tempFile.getAbsolutePath());
        }
    }

    /** Constant for the error code when something totally unexpected happens. */
    public static final int UNKNOWN_ERROR_CODE = -1;
    /** Constant for the error code which indicates that there are zero rows to return. */
    public static final int ZERO_ROWS_ERROR_CODE = -2;

    /**
     * Get the percentage done on the build.
     * @return the percentage complete
     */
    public synchronized int getPercent() {
        if (isInitializing()) {
            return 0;
        } else if (numTotalRows > 0) {
            return (numCompletedRows * ONE_HUNDRED) / numTotalRows;
        } else if (numTotalRows == 0 && numCompletedRows == 0) {
            return ZERO_ROWS_ERROR_CODE;
        } else {
            return UNKNOWN_ERROR_CODE;
        }
    }

    /**
     * Flag indicating if this build has completed.
     * @return boolean
     */
    public synchronized boolean isCompleted() {
        return numCompletedRows == numTotalRows;
    }

    /**
     * Flag indicating if this build is initializing.
     * @return boolean
     */
    public synchronized boolean isInitializing() {
        return initializing;
    }

    /**
     * set the flag indicating if this build is initializing.
     * @param init whether or not this is initializing.
     */
    public synchronized void setInitializing(boolean init) {
        this.initializing = init;
    }

    /**
     * Set the flag indicating if a cached export file is available.
     * @param available true if available, false otherwise.
     */
    public synchronized void setCachedFileAvailable(boolean available) {
        this.cachedFileAvailable = available;
    }

    /**
     * Returns the flag indicating if a cached export file is available for
     * the export bean.  If multiple samples are selected for export and at least
     * one of them is not cached then this value will be false.
     * @return true if one is available, false otherwise.
     */
    public synchronized boolean isCachedFileAvailable() {
        return cachedFileAvailable;
    }

    /**
     * Flag indicating if this build is running.
     * @return boolean
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * set the flag indicating if this build is running.
     * @param running whether or not this is running.
     */
    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Get the results of the build if it is complete.
     * @return String that is the results.
     */
    public synchronized File getResultsFile() {
        if (isCompleted()) {
            logger.debug("Retrieving export file for export");
            exportedFlag = true;
            return tempFile;
        } else {
            logger.debug("Export builder not finished. Returning null");
            return null;
        }
    }

    /**
     * Get the temporary export file.
     * @return File the temporary export file.
     */
    public synchronized File getTempFile() {
        return tempFile;
    }

    /**
     *  Dumps the current textBuffer to a temporary file to save on space.
     *  @param textBuffer the text to write to the file.
     *  @param overwriteFlag false to overwrite, true to append.
     */
    protected void dumpToFile(CharSequence textBuffer, boolean overwriteFlag) {
        try {
            FileUtils.dumpToFile(textBuffer, getTempFile(), overwriteFlag);
        } catch (IOException ioe) {
            String path = tempFile.getAbsolutePath();
            logger.error("IOException in AbstractExportBean thread: " + path, ioe);
            stop();
            numTotalRows = -1;
        }
    }

    /**
     * Takes cached export files and copies them into a single export zip file.
     * @param datasetItem the current dataset, needed for a better path in zip file
     * @param fileName the cached export file to copy
     * @param numOfSamples the number of samples
     * @param exportType the type of export, this.TX_EXPORT, this.STEP_EXPORT or this.PROBLEM_EXPORT
     */
    public void copyCachedFile(DatasetItem datasetItem,
            String fileName, int numOfSamples, String exportType) {
        copyCachedFile(datasetItem, null, fileName, numOfSamples, exportType);
    }

    /**
     * int Constant of the maximum number of bytes the ZipInputStream reads
     * */
    static final int ZIP_INPUT_STREAM_MAX_BYTES = 1024;
    /**
     * Takes cached export files and copies them into a single export zip file.
     * if multiple samples are selected, then
     * 1. unzip the zip file and extract all the files
     * 2. copy all the files to the tempFile
     * 3. add the README file
     * 4. re-zip all the files.
     * if single sample is selected, then
     * 1. copy the zip file
     * 2. inject the README file
     * the README.txt file will be deleted from the cached file folder afterwards.
     * @param datasetItem the current dataset, needed for a better path in zip file
     * @param sampleItem the sample item
     * a temporary file to include in the zip
     * @param fileName the cached export file to copy
     * @param numOfSamples the number of samples
     * @param exportType the type of export, this.TX_EXPORT, this.STEP_EXPORT or this.PROBLEM_EXPORT
     */
    public void copyCachedFile(DatasetItem datasetItem, SampleItem sampleItem,
            String fileName, int numOfSamples, String exportType) {

        File newFile = null, readMeFile = null, termsFile = null;
        File spExportFile = null;
        ZipInputStream zin = null;
        ZipEntry zipEntry = null;
        FileOutputStream fout = null;
        FileInputStream inputStream = null;
        if (datasetItem == null) {
            String msg = "copyCachedFile: datasetItem cannot be null.";
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
        if (fileName == null) {
            String msg = "copyCachedFile: fileName cannot be null.";
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
        logDebug("Attempting to copy ", fileName, " to zipped export.");

        byte[] buffer = new byte[ZIP_BUFFER_SIZE];

        if (outputStream == null) {
            openZipStream();
        }
        try {
            // create README.txt and TERMS.html
            int index = fileName.lastIndexOf("/");
            if (index < 0) {
                index = fileName.lastIndexOf("\\");
            }
            String filePath = fileName.substring(0, index);
            readMeFile = createReadMeFile(filePath, datasetItem).getAbsoluteFile();
            termsFile = createTermsFile(filePath, datasetItem).getAbsoluteFile();
            // re-create zip
            if (numOfSamples == 1) {
                zin = new ZipInputStream(new FileInputStream(fileName));
                zipEntry = zin.getNextEntry();

                int length;
                if (zipEntry != null) {
                    String entryName = zipEntry.getName();
                    // copy stream from zip to tempFile
                    outputStream.putNextEntry(new ZipEntry(entryName));

                    while ((length = zin.read(buffer, 0, ZIP_INPUT_STREAM_MAX_BYTES)) > -1) {
                        outputStream.write(buffer, 0, length);
                    }

                } else {
                    zipEntry = new ZipEntry(
                        "ds" + datasetItem.getId() + "_" + exportType + "_"
                            + cleanForFileSystem(sampleItem.getSampleName())
                            + "_" + sampleItem.getId() + "_"
                            + TIME_STAMP_FMT.format(new Date()) + ".txt");
                    inputStream =  new FileInputStream(fileName);
                    outputStream.putNextEntry(zipEntry);
                    length = 0;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    inputStream.close();
                }
                if (zin != null) {
                    zin.closeEntry();
                }

                // add README.txt to tempFile
                // Note: can't use function addFileToTempZipFile,
                // using it causes "unexpected end of ZLIB input stream" exception
                zipEntry = new ZipEntry(README_FILE_NAME);
                inputStream =  new FileInputStream(readMeFile);
                outputStream.putNextEntry(zipEntry);
                length = 0;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();

                // add TERMS.html to tempFile
                // Note: can't use function addFileToTempZipFile,
                // using it causes "unexpected end of ZLIB input stream" exception
                boolean termsExist = termsFile.exists();
                if (termsExist) {
                    zipEntry = new ZipEntry(TERMS_FILE_NAME);
                    inputStream =  new FileInputStream(termsFile);
                    outputStream.putNextEntry(zipEntry);
                    length = 0;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    inputStream.close();
                }

                outputStream.closeEntry();

            } else {
                // extract text file from cached zip file
                // the text file is stored in temp folder and will be deleted after export
                zin = new ZipInputStream(
                        new FileInputStream(fileName));

                zipEntry = zin.getNextEntry();
                if (zipEntry != null) {
                    String entryName = zipEntry.getName();

                    newFile = File.createTempFile("tx_export_", null);
                    fout = new FileOutputStream(newFile);
                    int length;
                    while ((length = zin.read(buffer, 0, ZIP_INPUT_STREAM_MAX_BYTES)) > -1) {
                        fout.write(buffer, 0, length);
                    }

                    fout.close();
                    zin.closeEntry();
                    addFileToTempZipFile(newFile, entryName);
                } else {
                    spExportFile = new File(fileName);
                    addFileToTempZipFile(spExportFile,
                        "ds" + datasetItem.getId() + "_" + exportType + "_"
                            + cleanForFileSystem(sampleItem.getSampleName())
                            + "_" + sampleItem.getId() + "_"
                            + TIME_STAMP_FMT.format(new Date()) + ".txt");
                }
                if (zin != null) {
                    zin.closeEntry();
                }
                addFileToTempZipFile(readMeFile, README_FILE_NAME);
                boolean termsExist = termsFile.exists();
                if (termsExist) {
                    addFileToTempZipFile(termsFile, TERMS_FILE_NAME);
                }
            }

        } catch (ZipException zipe) {
            String path = tempFile.getAbsolutePath();
            logger.error("ZipException in AbstractExportBean thread: " + path, zipe);
            stop();
            numTotalRows = -1;
            closeZipStream();
        } catch (IOException ioe) {
            String path = tempFile.getAbsolutePath();
            logger.error("IOException in AbstractExportBean thread: " + path, ioe);
            stop();
            numTotalRows = -1;
            closeZipStream();
        } finally {
            if (spExportFile != null && spExportFile.exists()) { spExportFile.delete(); }
            if (readMeFile != null && readMeFile.exists()) { readMeFile.delete(); }
            if (termsFile != null && termsFile.exists()) { termsFile.delete(); }
            if (newFile != null && newFile.exists()) { newFile.delete(); }
        }
    }

    /**
     * Add a file to the temp file given the assumption that the temp file is a zip format.
     * @param file the file to be added
     * @param fileNameInZip the name of the file in the zip,
     *        it can be different than the given file name.
     * */
    public void addFileToTempZipFile(File file, String fileNameInZip) {

        ZipInputStream zin = null;
        ZipEntry zipEntry = null;
        FileInputStream inputStream = null;
        byte[] buffer = new byte[ZIP_BUFFER_SIZE];

        try {
            boolean foundZipEntry = false;
            inputStream =
                    new FileInputStream(file);
            zipEntry = new ZipEntry(fileNameInZip);
            zin = new ZipInputStream(new FileInputStream(tempFile));
            ZipEntry entry = zin.getNextEntry();

            // loop through the entry to find if readme.txt is already a zipEntry
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals(fileNameInZip)) {
                    foundZipEntry = true;
                    break;
                }
            }
            zin.close();

            // if no zipEntry found for README.txt, then add it to the zip.
            if (!foundZipEntry) {
                outputStream.putNextEntry(zipEntry);
                int length = 0;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.closeEntry();
            }

        } catch (IOException ioe) {
            logger.error("error occurs when adding file to the zip: " + ioe.getMessage());
            closeZipStream();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("Could not close input stream from file: "
                        + file.getAbsolutePath());
                }
            }
        }
    }

    /** Definition of single newline for formatting output. */
    static final String SINGLE_NEW_LINE = "\r\n";
    /** Definition of double newlines for formatting output. */
    static final String DOUBLE_NEW_LINES = "\r\n\r\n";


    /** Constant of max number of characters in one line. */
    static final int MAX_LINE_LENGTH = 75;

    /**
     * Create a README.txt file.
     * @param path the path where the file resides
     * @param dataset the dataset item
     * @return README.txt file
     * */
    public File createReadMeFile(String path, DatasetItem dataset) {
        logDebug("createReadMeFile begin, path ", path, " for dataset ", dataset.getId());
        File file = new File(path + File.separator + README_FILE_NAME);

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        dataset = (DatasetItem)datasetDao.get((Integer) dataset.getId());
        DatasetInfoReportDao datasetInfoReportDao = DaoFactory.DEFAULT.getDatasetInfoReportDao();
        DatasetInfoReport datasetInfoReport = datasetInfoReportDao.getDatasetInfoReport(dataset);
        PaperDao paperDao = DaoFactory.DEFAULT.getPaperDao();
        PaperItem preferredPaper = new PaperItem();
        String acknowledgment = null;
        String preferredCitation = null;
        boolean hasAcknowledgment = false;
        boolean hasCitation = false;

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            StringBuffer content = new StringBuffer();
            content.append("'" + dataset.getDatasetName() + "'" + DOUBLE_NEW_LINES);
            appendOverviewInfo(content, dataset, datasetInfoReport);

            content.append("STATISTICS" + SINGLE_NEW_LINE);
            appendStatisticsInfo(content, datasetInfoReport);

            content.append("CITATION/ACKNOWLEDGMENT" + DOUBLE_NEW_LINES);
            // get acknowledgment
            if (dataset.getAcknowledgment() != null) {
                acknowledgment = dataset.getAcknowledgment().replaceAll("\n", SINGLE_NEW_LINE);
            }
            hasAcknowledgment =
                    (acknowledgment == null || acknowledgment.equals("")) ? false : true;

            // get preferred citation
            if (dataset.getPreferredPaper() != null) {
                preferredPaper = paperDao.get((Integer)dataset.getPreferredPaper().getId());
                if (preferredPaper != null && preferredPaper.getCitation() != null) {
                    preferredCitation = preferredPaper.getCitation()
                            .replaceAll("\n", SINGLE_NEW_LINE);
                }
            }
            hasCitation =
                    (preferredCitation == null || preferredCitation.equals("")) ? false : true;

            content.append(
                    "If you publish research based on this dataset, "
                            + "please include the following " + SINGLE_NEW_LINE);

            if (hasCitation) {
                content.append("citation and ");
            }
            content.append("acknowledgement: " + DOUBLE_NEW_LINES);

            if (hasCitation) {
                content.append("Citation:" + DOUBLE_NEW_LINES);
                content.append(formatString(preferredCitation, MAX_LINE_LENGTH) + DOUBLE_NEW_LINES);
            }
            content.append("Acknowledgment:" + DOUBLE_NEW_LINES);

            if (hasAcknowledgment) {
                content.append(formatString(acknowledgment
                        + " We used the '" + dataset.getDatasetName()
                        + "' dataset accessed via DataShop (Koedinger et al., 2010).",
                        MAX_LINE_LENGTH)
                        + DOUBLE_NEW_LINES);
                content.append("or" + DOUBLE_NEW_LINES);
                content.append(formatString(acknowledgment
                        + " We used the '" + dataset.getDatasetName()
                        + "' dataset accessed via DataShop (pslcdatashop.org).",
                        MAX_LINE_LENGTH)
                        + DOUBLE_NEW_LINES);
            } else {
                content.append(formatString("We used the '" + dataset.getDatasetName()
                        + "' dataset accessed via DataShop (Koedinger et al., 2010).",
                        MAX_LINE_LENGTH)
                        + DOUBLE_NEW_LINES);
                content.append("or" + DOUBLE_NEW_LINES);
                content.append(formatString("We used the '" + dataset.getDatasetName()
                        + "' dataset accessed via DataShop (pslcdatashop.org).",
                        MAX_LINE_LENGTH)
                        + DOUBLE_NEW_LINES);
            }

            content.append("To cite the DataShop web application and repository, "
                    + "please include the " + SINGLE_NEW_LINE
                    + "following reference in your publication:" + DOUBLE_NEW_LINES
                    + formatString("Koedinger, K.R., Baker, R.S.J.d., Cunningham, K., "
                        + "Skogsholm, A., Leber, B., "
                        + "Stamper, J. (2010) A Data Repository for the EDM community: "
                        + "The PSLC DataShop. "
                        + "In Romero, C., Ventura, S., Pechenizkiy, M., Baker, "
                        + "R.S.J.d. (Eds.) Handbook "
                        + "of Educational Data Mining. Boca Raton, FL: CRC Press.", MAX_LINE_LENGTH)
                        + DOUBLE_NEW_LINES
                        + "You might also cite the DataShop URL in the text of your paper: "
                        + DOUBLE_NEW_LINES
                        + formatString(
                           "For exploratory analysis, I used the PSLC DataShop, available at "
                            + "http://pslcdatashop.org (Koedinger et al., 2010).", MAX_LINE_LENGTH)
                            + DOUBLE_NEW_LINES
                            + "Additional information on citing DataShop is available here: "
                            + SINGLE_NEW_LINE
                            + "http://www.pslcdatashop.org/help?page=citing");
            out.write(content.toString());
            out.close();
        } catch  (IOException e) {
            logger.error("error occurs when creating README.txt: " + e.getMessage());
        }
        return file;
    }

    /** Constant for the Additional Notes header, which is the longest in the overview section. */
    private static final String ADDL_NOTES_HEADER = "Additional Notes: ";
    /** Constant to line up the data in the overview section. */
    private static final int OVERVIEW_MAX_LEN = ADDL_NOTES_HEADER.length();
    /** Constant for the KC Models header, which is the longest in the statistics section. */
    private static final String KCM_HEADER = "Knowledge Component Model(s): ";
    /** Constant to line up the data in the statistics section. */
    private static final int STATS_MAX_LEN = KCM_HEADER.length();

    /**
     * Simple pad left function.
     * @param s the string to pad
     * @param n the number of spaces to pad
     * @return the padded string
     */
    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }
    /**
     * Simple pad right function.
     * @param s the string to pad
     * @param n the number of spaces to pad
     * @return the padded string
     */
    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }
    /**
     * Simple function to pad the overview headers.
     * @param s the string to pad
     * @return the padded string
     */
    public static String padOverview(String s) {
        return padRight(s, OVERVIEW_MAX_LEN);
    }
    /**
     * Simple function to pad the statistics headers.
     * @param s the string to pad
     * @return the padded string
     */
    public static String padStats(String s) {
        return padRight(s, STATS_MAX_LEN);
    }

    /**
     * Append the OVERVIEW info to the README file content.
     * @param content StringBuffer containing README file content
     * @param dataset the dataset item
     * @param datasetInfoReport the DatasetInfoReport item
     * */
    private void appendOverviewInfo(StringBuffer content,
            DatasetItem dataset, DatasetInfoReport datasetInfoReport) {

        String projectName = datasetInfoReport.getProjectName();
        String piName = (datasetInfoReport.getPiName() == null)
                ? "" : datasetInfoReport.getPiName();
        String dpName = (datasetInfoReport.getDpName() == null)
                ? null : datasetInfoReport.getDpName();
        String curriculum = (datasetInfoReport.getCurriculumName() == null)
                ? "" : datasetInfoReport.getCurriculumName();
        String dates = DatasetItem.getDateRangeString(dataset);
        String domain = (datasetInfoReport.getDomainName() == null)
                ? "" : datasetInfoReport.getDomainName();
        String learnLab = (datasetInfoReport.getLearnlabName() == null)
                ? "" : datasetInfoReport.getLearnlabName();
        String tutor = (dataset.getTutor() == null)
                ? "" : dataset.getTutor();
        String description = (dataset.getDescription() == null)
                ? "" : dataset.getDescription();
        String hasStudyData = dataset.getStudyFlag();
        String hypothesis = (dataset.getHypothesis() == null) ? "" : dataset.getHypothesis();
        String status = (dataset.getStatus() == null)
                ? "" : dataset.getStatus();
        String school = (dataset.getSchool() == null)
                ? "" : dataset.getSchool();
        String additionalNotes = (dataset.getNotes() == null)
                ? "" : dataset.getNotes();

        content.append("OVERVIEW");
        content.append(SINGLE_NEW_LINE);
        content.append(padOverview("Project: "));
        content.append(projectName);
        content.append(SINGLE_NEW_LINE);

        content.append(padOverview("PI: "));
        content.append(piName);
        content.append(SINGLE_NEW_LINE);

        if (dpName != null && dpName.length() > 0) {
            content.append(padOverview("Data Provider: "));
            content.append(dpName);
            content.append(SINGLE_NEW_LINE);
        }
        content.append(padOverview("Curriculum: "));
        content.append(formatStringForOverview(curriculum, MAX_LINE_LENGTH));
        content.append(SINGLE_NEW_LINE);

        content.append(padOverview("Dates: "));
        content.append(dates);
        content.append(SINGLE_NEW_LINE);

        content.append(padOverview("Domain/LearnLab: "));
        content.append(domain);
        if (!learnLab.equals("") && !domain.equals("Other")) {
            content.append("/" + learnLab);
        }
        content.append(SINGLE_NEW_LINE);

        content.append(padOverview("Tutor: "));
        content.append(formatStringForOverview(tutor, MAX_LINE_LENGTH));
        content.append(SINGLE_NEW_LINE);
        content.append(padOverview("Description: "));
        content.append(formatStringForOverview(description, MAX_LINE_LENGTH));
        content.append(SINGLE_NEW_LINE);
        content.append(padOverview("Has Study Data: ") + hasStudyData);
        content.append(SINGLE_NEW_LINE);
        if (dataset.getStudyFlag().equals(DatasetItem.STUDY_FLAG_YES)) {
            content.append(padOverview("Hypothesis: "));
            content.append(formatStringForOverview(hypothesis, MAX_LINE_LENGTH));
            content.append(SINGLE_NEW_LINE);
        }
        content.append(padOverview("Status: "));
        content.append(formatStringForOverview(status, MAX_LINE_LENGTH));
        content.append(SINGLE_NEW_LINE);
        content.append(padOverview("School(s): "));
        content.append(formatStringForOverview(school, MAX_LINE_LENGTH));
        content.append(SINGLE_NEW_LINE);
        content.append(padOverview(ADDL_NOTES_HEADER));
        content.append(formatStringForOverview(additionalNotes, MAX_LINE_LENGTH));
        content.append(DOUBLE_NEW_LINES);
    }

    /** Constant for the number of characters to cover the decimal point plus 2 places. */
    private static final int THREE = 3;

    /**
     * Append the STATISTICS info to the README file content.
     * @param content StringBuffer containing README file content
     * @param datasetInfoReport the DatasetInfoReport item
     * */
    private void appendStatisticsInfo(StringBuffer content, DatasetInfoReport datasetInfoReport) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String totalStudHours = df.format(datasetInfoReport.getTotalStudentHours());

        DecimalFormat commaDf = new DecimalFormat("#,###,###");
        String numberOfStudents = commaDf.format(datasetInfoReport.getNumberOfStudents());
        String totalNumberOfTxns = commaDf.format(datasetInfoReport.getNumberOfTransactions());
        String numberOfUniqueSteps = commaDf.format(datasetInfoReport.getNumberOfSteps());
        String totalNumberOfSteps = commaDf.format(datasetInfoReport.getTotalNumberOfSteps());

        //determine the number of characters before the decimal point for the 5 values
        //in the statistics section
        int len = 1;
        if (numberOfStudents.length() > len)  { len = numberOfStudents.length(); }
        if (numberOfUniqueSteps.length() > len)  { len = numberOfUniqueSteps.length(); }
        if (totalNumberOfSteps.length() > len)  { len = totalNumberOfSteps.length(); }
        if (totalNumberOfTxns.length() > len)  { len = totalNumberOfTxns.length(); }
        if ((totalStudHours.length() - THREE) > len) { len = totalStudHours.length() - THREE; }

        content.append(padStats("Number of Students: ")
                + padLeft(numberOfStudents, len) + SINGLE_NEW_LINE);
        content.append(padStats("Number of Unique Steps: ")
                + padLeft(numberOfUniqueSteps, len) + SINGLE_NEW_LINE);
        content.append(padStats("Total Number of Steps: ")
                + padLeft(totalNumberOfSteps, len) + SINGLE_NEW_LINE);
        content.append(padStats("Total Number of Transactions: ")
                + padLeft(totalNumberOfTxns, len) + SINGLE_NEW_LINE);
        content.append(padStats("Total Student Hours: ")
                + padLeft(totalStudHours, len + THREE) + SINGLE_NEW_LINE);

        HashMap<String, Integer> skillModels = datasetInfoReport.getSkillModels();

        TreeSet skillModelNames = new TreeSet();
        skillModelNames.addAll(skillModels.keySet());
        int kcmCount = 0;
        for (Iterator<String> it = skillModelNames.iterator(); it.hasNext();) {
             String skillModelName = it.next();
             int skillCount = 0;
             Integer skillCountInt = (Integer) skillModels.get(skillModelName);
             if (skillCountInt != null) {
                 skillCount = skillCountInt.intValue();
             }

             if (kcmCount == 0) {
                 content.append(padStats(KCM_HEADER)
                         + skillModelName + " (" + skillCount + " ");
             } else {
                 content.append(padStats("") + skillModelName + " (" + skillCount + " ");
             }
             if (skillCount > 1) {
                 content.append("knowledge components)" + SINGLE_NEW_LINE);
             } else {
                 content.append("knowledge component)" + SINGLE_NEW_LINE);
             }
             kcmCount++;
        }

        if (skillModels.size() == 0) {
            content.append(padStats(KCM_HEADER) + "None" + DOUBLE_NEW_LINES);

        } else {
            // Add the extra line at the end...
            content.append(SINGLE_NEW_LINE);
        }
    }

    /** Constant of max number of characters in one line. */
    static final int TERMS_MAX_LINE_LENGTH = 80;
    /** Links to the DataShop reference tag in Terms.html. */
    static final String DS_REF = "<a name=\"dsTerms\">";
    /** Actual link for the DataShop Terms of Use in Terms.html. */
    static final String DS_LINK = "<a href=\"#dsTerms\">";
    /** Links to the project-specific reference tag in Terms.html. */
    static final String PROJECT_REF = "<a name=\"projectTerms\">";
    /** Actual link for the project-specific Terms of Use in Terms.html. */
    static final String PROJECT_LINK = "<a href=\"#projectTerms\">";
    /** The page title for Terms.html. */
    static final String PAGE_TITLE = "Terms of Use";

    /** Date formate for createTermsFile method. */
    static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("MMMMMMMMMMMMMM dd, yyyy");
    /** Field position of zero. */
    static final FieldPosition FIELD_POS_ZERO = new FieldPosition(0);

    /**
     * Create a TERMS.html file.
     * @param path the path where the file resides
     * @param dataset the dataset item
     * @return TERMS.html file
     * */
    public File createTermsFile(String path, DatasetItem dataset) {
        logDebug("createTermsFile begin, path ", path, " for dataset ", dataset.getId());
        File file = new File(path + File.separator + TERMS_FILE_NAME);
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        dataset = (DatasetItem)datasetDao.get((Integer) dataset.getId());
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        ProjectItem project = null;
        String datashopUrl = ServerNameUtils.getDataShopUrl();

        try {
            StringBuffer content = new StringBuffer();
            project = projectDao.get((Integer)dataset.getProject().getId());

            if (project != null && project.getProjectName() != null) {
                Integer projectId = (Integer) project.getId();

                // Get the applied ToU version for this project if one exists
                TermsOfUseVersionItem projectVersion =
                    touVersionDao.getProjectTerms(projectId, null);
                // Get the applied version of the Datashop Terms of Use
                TermsOfUseVersionItem datashopVersion = (TermsOfUseVersionItem)
                    (touVersionDao.findAppliedVersion(TermsOfUseItem.DATASHOP_TERMS));

                boolean bothTermsExist = false;
                boolean atLeastOneExists = false;
                if (projectVersion != null && datashopVersion != null) {
                    bothTermsExist = true;
                    atLeastOneExists = true;
                } else if (projectVersion != null || datashopVersion != null) {
                    atLeastOneExists = true;
                }

                if (atLeastOneExists) {
                    content.append("<!DOCTYPE html><html lang=\"en\"><head><title>");
                    content.append(PAGE_TITLE);
                    content.append("</title></head><body>");
                }

                // Create ToC if both project DS terms exist
                if (bothTermsExist) {
                    content.append("<p>");
                    content.append(PROJECT_LINK);
                    content.append(TermsOfUseItem.PROJECT_TERMS);
                    content.append(" Terms of Use</a></p><p>");
                    content.append(DS_LINK);
                    content.append(TermsOfUseItem.DATASHOP_TERMS);
                    content.append(" Terms of Use</a></p>");
                }

                if (projectVersion != null) {
                    // Get and format effective project ToU date
                    StringBuffer empty = new StringBuffer();
                    String effectiveDateString = DATE_FMT.format(
                            projectVersion.getAppliedDate(), empty, FIELD_POS_ZERO
                            ).toString();

                    content.append("<p>");
                    content.append("<div id=\"terms-of-use-div\">");
                    content.append("<h1>");
                    content.append(PROJECT_REF);
                    content.append(TermsOfUseItem.PROJECT_TERMS);
                    content.append(" Terms of Use</h1>");
                    content.append("</p>");
                    content.append("<p>");
                    content.append("Effective ");
                    content.append(effectiveDateString);
                    content.append("</p>");
                    content.append("<p>");
                    content.append("For the current project terms of use, ");
                    content.append("which may differ from these terms, see ");
                    content.append("<a href=\"");
                    content.append(datashopUrl);
                    content.append("/Project?id=");
                    content.append(projectId);
                    content.append("\">");
                    content.append(datashopUrl);
                    content.append("/Project?id=");
                    content.append(projectId);
                    content.append("</a></div>");
                    content.append("</p>");
                    content.append("<p>");
                    content.append(projectVersion.getTerms());
                    content.append("</p>");
                }

                if (datashopVersion != null) {
                    // Get and format effective DS ToU date
                    StringBuffer empty = new StringBuffer();
                    String effectiveDateString = DATE_FMT.format(
                            datashopVersion.getAppliedDate(), empty, FIELD_POS_ZERO
                            ).toString();

                    content.append("<p>");
                    content.append("<div id=\"terms-of-use-div\">");
                    content.append("<h1>");
                    content.append(DS_REF);
                    content.append(TermsOfUseItem.DATASHOP_TERMS);
                    content.append(" Terms of Use</h1>");
                    content.append("<p>");
                    content.append("Effective ");
                    content.append(effectiveDateString);
                    content.append("</p>");
                    content.append("<p>");
                    content.append("For the current terms of use, ");
                    content.append("which may differ from these terms, see ");
                    content.append("<a href=\"");
                    content.append(datashopUrl);
                    content.append("/Terms");
                    content.append("\">");
                    content.append(datashopUrl);
                    content.append("/Terms");
                    content.append("</a></div>");
                    content.append("</p>");
                    content.append("<p>");
                    content.append(datashopVersion.getTerms());
                    content.append("</p>");
                }

                if (atLeastOneExists) {
                    content.append("</body></html>");
                }

                // Only write Terms.html to file if content was found
                if (atLeastOneExists && !content.toString().equals("")) {
                    logDebug("createTermsFile writing terms file");
                    BufferedWriter out = new BufferedWriter(new FileWriter(file));
                    out.write(content.toString());
                    out.close();
                } else {
                    logDebug("createTermsFile NOT writing terms file");
                }
            } else {
                logDebug("createTermsFile NO project");
            }
        } catch  (IOException e) {
            logger.error("error occurs when creating " + TERMS_FILE_NAME + ": " + e.getMessage());
        }
        return file;
    }

    /**
     * Formats the string by adding newline character
     * and three spaces in the beginning of each line.
     * @param input the input string to format
     * @param lineLength the max length of the line
     * @return string a formatted result
     * */
    public String formatString(String input, int lineLength) {
        String result = "", temp = "",  tempSection = "", currentLine = "";
        int currentLength = 0;
        int numOfCurrentLines = 0;

        String[] lines = input.split("\n");

        for (int c = 0; c < lines.length; c++) {
            currentLine = lines[c].trim();
            currentLength = currentLine.length();

            if (currentLength > lineLength) {
                // calculate how many lines it needs
                numOfCurrentLines = currentLength / lineLength;
                temp = currentLine;
                // loop through each line
                for (int i = 0; i < numOfCurrentLines; i++) {
                    if (temp.length() > lineLength) {
                        tempSection = temp.substring(0, lineLength);
                        char nextChar = temp.charAt(lineLength);
                        // if the next character is not a space
                        // use position of the previous space to split the line
                        if (nextChar != ' ') {
                            int index = tempSection.lastIndexOf(' ');
                            tempSection = temp.substring(0, index + 1);
                            temp = temp.substring(index + 1);
                            numOfCurrentLines++;
                        } else {
                            temp = temp.substring(tempSection.length());
                        }
                    } else {
                        tempSection = temp;
                        temp = "";
                    }
                    result += "   " + tempSection.trim();
                    if (i < numOfCurrentLines - 1 && !tempSection.equals("")) {
                        result += "\r\n";
                    }
                }
            } else {
                if (currentLength > 0) {
                    result += "   " + currentLine + "\r\n";
                } else {
                    result += "\r\n";
                }
            }
        }
        result = "   " + result.trim();
        return result;
    }

    /**
     * Formats the string by adding newline character
     * and tabs in the beginning of each line.
     * @param input the input string to format
     * @param lineLength the max length of the line
     * @return string a formatted result
     * */
    public String formatStringForOverview(String input, int lineLength) {
        String result = "", temp = "",  tempSection = "", currentLine = "";
        int currentLength = 0;
        int numOfCurrentLines = 0;

        String prefix = padOverview("");

        String[] lines = input.split("\n");

        for (int c = 0; c < lines.length; c++) {
            currentLine = lines[c].trim();
            currentLength = currentLine.length();
            // handle case where line cannot be split, i.e., a URL
            if ((currentLine.lastIndexOf(' ') == -1) && (currentLength > 0)) {
                result += prefix + currentLine + "\r\n";
            } else if (currentLength > lineLength) {
                // calculate how many lines it needs
                numOfCurrentLines = currentLength / lineLength;
                temp = currentLine;
                // loop through each line
                for (int i = 0; i < numOfCurrentLines; i++) {
                    if (temp.length() > lineLength) {
                        tempSection = temp.substring(0, lineLength);
                        char nextChar = temp.charAt(lineLength);
                        // if the next character is not a space
                        // use position of the previous space to split the line
                        if (nextChar != ' ') {
                            int index = tempSection.lastIndexOf(' ');
                            if (index == -1) {
                                // this portion of the line cannot be split
                                tempSection = temp;
                                i = numOfCurrentLines;
                            } else {
                                tempSection = temp.substring(0, index + 1);
                                temp = temp.substring(index + 1);
                                numOfCurrentLines++;
                            }
                        } else {
                            temp = temp.substring(tempSection.length());
                        }
                    } else {
                        tempSection = temp;
                        temp = "";
                    }
                    if (tempSection.trim().length() > 0) {
                        result += prefix + tempSection.trim();
                    }
                    if (i < numOfCurrentLines - 1 && !tempSection.equals("")) {
                        result += "\r\n";
                    }
                }
            } else {
                if (currentLength > 0) {
                    result += prefix + currentLine + "\r\n";
                } else {
                    result += "\r\n";
                }
            }
        }
        result = result.trim();
        return result;
    }

    /** The amount of time to sleep when deleting a temp file, one minute in milliseconds. */
    protected static final int SLEEP_TIME = 1000;
    /** The amount of time to keep trying to delete the temp file, six minutes in milliseconds. */
    private static final int KEEP_TRYING_TIME = 6000;

    /**
     * Delete the temporary file.
     */
    public void deleteTempFile() {
        try {
            if (tempFile.exists()) {
                boolean success = tempFile.delete();
                int totalTime = 0;
                //if there is an error keep trying to delete the time for a given amount of time
                //or if there is no error keep trying until successful.
                while ((!success && totalTime < KEEP_TRYING_TIME && hasError)
                        || (!success && !hasError)) {
                    Thread.sleep(SLEEP_TIME);
                    totalTime += SLEEP_TIME;
                    success = tempFile.delete();
                }
                if (!success) {
                    logger.warn("Unable to delete temporary export file "
                            + tempFile.getAbsolutePath() + " set to deleteOnExit");
                    tempFile.deleteOnExit();
                } else {
                    logDebug("Deleted temporary export file ", tempFile.getAbsolutePath());
                }
            } else {
                logDebug("deleteTempFile: Temp file not found: ", tempFile.getName());
            }
        } catch (Exception exception) {
            logger.error("Caught exception trying to delete file", exception);
            hasError = true;
        }
    }

    /**
     * Open/initialize the zip output stream for transaction exports.
     */
    public void openZipStream() {
        logDebug("attempting to initialize zip output stream.");
        try {
            outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
            logDebug("Opening the zip output stream.");
        } catch (FileNotFoundException e) {
            logger.error("Caught an exception while trying to open the zip output stream.", e);
        }
        outputStream.setLevel(Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * Close the zip output stream for transaction exports.
     */
    public void closeZipStream() {
        try {
            if (outputStream != null) {
                outputStream.close();
                logger.debug("Closing the zip output stream.");
            }
        } catch (IOException e) {
            logger.error("Caught an exception while trying to close the zip output stream.", e);
        }
    }

    /**
     * Stops running this bean.
     */
    public void stop() {
        cancelFlag = true;
    }

    /**
     * Returns the temporary file name generated in the constructor.
     * @return the temporary file name
     */
    protected String getTemporaryFileName() {
        return tempFile.getName();
    }

    /**
     * Returns the exportedFlag.
     * @return the exportedFlag
     */
    public boolean isExported() {
        return exportedFlag;
    }

    /**
     * Returns the cancelFlag.
     * @return the cancelFlag
     */
    public boolean isCancelFlag() {
        return cancelFlag;
    }

    /**
     * Returns the hasError.
     * @return the hasError
     */
    public boolean isHasError() {
        return hasError;
    }

    /**
     * Sets the hasError.
     * @param hasError The hasError to set.
     */
    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    /**
     * Returns the numCompletedRows.
     * @return the numCompletedRows
     */
    public int getNumCompletedRows() {
        return numCompletedRows;
    }

    /**
     * Sets the numCompletedRows.
     * @param numCompletedRows The numCompletedRows to set.
     */
    public void setNumCompletedRows(int numCompletedRows) {
        this.numCompletedRows = numCompletedRows;
    }

    /**
     * Increment the numCompletedRows by 1.
     */
    public void incrementNumCompletedRows() {
        this.numCompletedRows++;
    }

    /**
     * Increment the number of completed rows.
     * @param rows the number of rows to increment by
     */
    public void incrementNumCompletedRows(int rows) {
        numCompletedRows += rows;
    }

    /**
     * Returns the numTotalRows.
     * @return the numTotalRows
     */
    public int getNumTotalRows() {
        return numTotalRows;
    }

    /**
     * Sets the numTotalRows.
     * @param numTotalRows The numTotalRows to set.
     */
    public void setNumTotalRows(int numTotalRows) {
        this.numTotalRows = numTotalRows;
        logger.debug("total rows now " + numTotalRows);
    }

    /**
     * Increment the numCompletedRows by 1.
     * @param rows number of rows to increment
     */
    public void incrementNumTotalRows(int rows) {
        numTotalRows += rows;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Abstract run method.
     */
    public abstract void run();
}
