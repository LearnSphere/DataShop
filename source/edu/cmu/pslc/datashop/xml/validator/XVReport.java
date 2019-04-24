/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.xml.validator;

import org.apache.log4j.Logger;

/**
 * Represents the final report created by the XV.
 * @author kcunning
 * @version $Revision: 7671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-04-24 14:02:16 -0400 (Tue, 24 Apr 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class XVReport {

    /** Number of files processed */
    private int numFilesProcessed;
    /** Number of valid files */
    private int numValidFiles;
    /** Number of files containing errors */
    private int numErrorFiles;
    /** Total number of errors found */
    private int totalErrors;
    /** Total number of warnings found */
    private int totalWarnings;
    /** Number of HTML warnings found */
    private int htmlWarnings;
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Empty Constructor.
     */
    public XVReport() { };

    /**
     * Full Constructor.
     * @param numFilesProcessed - the number of files processed
     * @param numValidFiles - the number of valid files
     * @param numErrorFiles - the number of files containing errors
     * @param totalErrors - the total number of errors found
     * @param totalWarnings - the total number of warnings found
     */
    public XVReport(int numFilesProcessed, int numValidFiles, int numErrorFiles,
            int totalErrors, int totalWarnings) {
        this.numFilesProcessed = numFilesProcessed;
        this.numValidFiles = numValidFiles;
        this.numErrorFiles = numErrorFiles;
        this.totalErrors = totalErrors;
        this.totalWarnings = totalWarnings;
    }

    /**
     * Get the number of error files.
     * @return the numErrorFiles
     */
    public int getNumErrorFiles() {
        return numErrorFiles;
    }

    /**
     * Set the number of error files.
     * @param numErrorFiles the numErrorFiles to set
     */
    public void setNumErrorFiles(int numErrorFiles) {
        this.numErrorFiles = numErrorFiles;
    }

    /**
     * Get the number of files processed.
     * @return the numFilesProcessed
     */
    public int getNumFilesProcessed() {
        return numFilesProcessed;
    }

    /**
     * Set the number of files processed.
     * @param numFilesProcessed the numFilesProcessed to set
     */
    public void setNumFilesProcessed(int numFilesProcessed) {
        this.numFilesProcessed = numFilesProcessed;
    }

    /**
     * Get the number of valid files.
     * @return the numValidFiles
     */
    public int getNumValidFiles() {
        return numValidFiles;
    }

    /**
     * Set the number of valid files.
     * @param numValidFiles the numValidFiles to set
     */
    public void setNumValidFiles(int numValidFiles) {
        this.numValidFiles = numValidFiles;
    }

    /**
     * Get the total number of errors.
     * @return the totalErrors
     */
    public int getTotalErrors() {
        return totalErrors;
    }

    /**
     * Set the total number of errors.
     * @param totalErrors the totalErrors to set
     */
    public void setTotalErrors(int totalErrors) {
        this.totalErrors = totalErrors;
    }

    /**
     * Get the total number of warnings.
     * @return the totalWarnings
     */
    public int getTotalWarnings() {
        return totalWarnings;
    }

    /**
     * Set the total number of warnings.
     * @param totalWarnings the totalWarnings to set
     */
    public void setTotalWarnings(int totalWarnings) {
        this.totalWarnings = totalWarnings;
    }

    /**
     * Get the number of HTML warnings.
     * @return the htmlWarnings
     */
    public int getHTMLWarnings() {
        return htmlWarnings;
    }

    /**
     * Set the number of HTML warnings.
     * @param htmlWarnings the htmlWarnings to set
     */
    public void setHTMLWarnings(int htmlWarnings) {
        this.htmlWarnings = htmlWarnings;
    }
    /**
     * Prints a formatted report for the XV.
     */
    public void printReport() {
        if (htmlWarnings > 0) {
            printHTMLWarningMsg();
        }
        logger.info("- - - - - - - - - - - - - - - - ");
        logger.info("* * *    R E S U L T S    * * *");
        logger.info("- - - - - - - - - - - - - - - - ");
        logger.info("Files processed: " + this.getNumFilesProcessed());
        logger.info("Valid files: " + this.getNumValidFiles());
        logger.info("Invalid files: " + this.getNumErrorFiles());
        logger.info("Errors found: " + this.getTotalErrors());
        logger.info("Warnings found: " + this.getTotalWarnings());

        if (this.getNumFilesProcessed() == 0) {
            logger.info("No files found to validate.");
        } else if (this.getTotalErrors() == 0) {
            if (this.getTotalWarnings() == 0) {
                logger.info("PERFECTO!  No errors or warnings found.");
            } else {
                logger.info("All files are valid, but there are some warnings.");
            }
        } else {
            logger.info("Some errors were identified.");
        }
    }

    /**
     * Warning message if HTML content discovered by XMLValidator
     */
    private static final String HTML_WARNING_MSG = "\n\n"
            + "One or more lines contain special characters (<, > or &). Re-run the XMLValidator\n"
            + "in verbose mode (using the '-v' switch) to see which one(s)."
            + "\n\nAn easy way to avoid having to escape all special characters in XML (which \n"
            + "includes <, >, and &) is to use a CDATA section. A CDATA section is a section \n"
            + "of element content that is marked for the parser to interpret as only character \n"
            + "data, not markup. It starts with the following sequence: <![CDATA[ and ends \n"
            + "with the first occurrence of the sequence: ]]>\n";

    /**
     * Prints the HTML warning message.
     */
    public void printHTMLWarningMsg() {
        logger.info(HTML_WARNING_MSG);
    }
    /**
     * Increment numFilesProcessed by one.
     */
    public void increaseNumFilesProcessed() {
        this.numFilesProcessed++;
    }

    /**
     * Increment numValidFiles by one.
     */
    public void increaseNumValidFiles() {
        this.numValidFiles++;
    }
    /**
     * Increment numErrorFiles by one.
     */
    public void increaseNumErrorFiles() {
        this.numErrorFiles++;
    }
    /**
     * Increment totalErrors by one.
     */
    public void increaseTotalErrors() {
        this.totalErrors++;
    }
    /**
     * Increment totalWarnings by one.
     */
    public void increaseTotalWarnings() {
        this.totalWarnings++;
    }
    /**
     * Increment htmlWarnings by one.
     */
    public void increaseHTMLWarnings() {
        this.htmlWarnings++;
        this.totalWarnings++;
    }

} // end class XVReport
